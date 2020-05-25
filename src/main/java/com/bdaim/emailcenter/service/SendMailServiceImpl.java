package com.bdaim.emailcenter.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.hazelcast.PhoneTobe;
import com.bdaim.common.hazelcast.ToBeRegisterDB;
import com.bdaim.emailcenter.dto.MailBean;
import com.bdaim.emailcenter.util.EmailUtil;
import com.bdaim.emailcenter.util.MailUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.expression.ParseException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import javax.transaction.Transactional;
import java.io.UnsupportedEncodingException;
import java.util.*;

/**
 * 营销资源service服务实现类
 */
@Service("sendMailService")
@Transactional
public class SendMailServiceImpl implements SendMailService {
    private static Logger logger = LoggerFactory.getLogger(SendMailServiceImpl.class);
    private static String subject = "触点大数据平台";
    @Autowired
    private JavaMailSender javaMailSender;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * 创建MimeMessage
     *
     * @param mailBean
     * @return
     * @throws MessagingException
     * @throws UnsupportedEncodingException
     */
    @Override
    public MimeMessage createMimeMessage(MailBean mailBean) {
        MimeMessage mimeMessage = javaMailSender.createMimeMessage();
        MimeMessageHelper messageHelper = null;
        try {
            messageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            messageHelper.setFrom(mailBean.getFrom(), mailBean.getFromName());
            messageHelper.setSubject(mailBean.getSubject());
            messageHelper.setTo(mailBean.getToEmails());
            messageHelper.setText(mailBean.getContext(), true); // html: true
        } catch (MessagingException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return mimeMessage;
    }

    @Override
    public void sendMail(MailBean mailBean) {
        MimeMessage msg = createMimeMessage(mailBean);
        javaMailSender.send(msg);
    }

    @Override
    public Object sendVerifyCode(String mail, String state) throws TouchException {
        String code = "";
        String message = "";
        String vcode = getRandomString();
        try {
            //判断验证码已发送次数
            // 保存到缓存中
            ToBeRegisterDB tobe = PhoneTobe.getInstance();
            try {
                //获取次数
                int num = tobe.getNum(mail);
                //获取创建时间
                long creatDate = tobe.getTimeStampFromMap(mail);
                logger.info("手机号上次申请时间:creatDate" + creatDate);
                // 当前时间
                logger.info("当前时间:creatDate" + System.currentTimeMillis());
                //计算当前时间与最后一次插入时间相差值
                if (getTime(creatDate)) {
                    //二十四小时候已过重新计算
                    tobe.setMap(mail, vcode, System.currentTimeMillis(), 1);
                    MailUtil.getInstance().sendout(subject, "您的邮箱验证码为：" + vcode, mail);
                    code = "1";
                    message = "发送完成";
                } else {
                    //已存在判断次数
                    if (num <= 99) {
                        logger.info("当前手机" + mail + "已经发送次数：" + num);
                        tobe.setMap(mail, vcode, System.currentTimeMillis(), (num + 1));
                        MailUtil.getInstance().sendout(subject, "您的邮箱验证码为：" + vcode, mail);
                        code = "1";
                        message = "发送完成";
                    } else {
                        logger.info("当前手机" + mail + "发送次数达到上限：" + num);
                        code = "0";
                        message = "当前手机发送次数达到上限";
                    }
                }
            } catch (Exception e) {
                logger.info("当前用户未发送过验证码插入验证码");
                //不存在插入
                try {
                    tobe.setMap(mail, vcode, System.currentTimeMillis(), 1);
                } catch (Exception e2) {
                    code = "0";
                    message = "执行插入缓存失败";
                    logger.error("执行插入缓存失败：", e2);
                }
                MailUtil.getInstance().sendout(subject, "您的邮箱验证码为：" + vcode, mail);
                code = "1";
                message = "发送完成";
            }
        } catch (Exception e) {
            code = "0";
            message = "发送验证码失败";
            logger.error("发送验证码失败：", e);
        }
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("code", code);
        resultMap.put("_message", message);
        resultMap.put("data", "");
        return JSONObject.toJSON(resultMap);
    }


    /**
     * 生成4位随机验证码
     */
    private static String getRandomString() {
        String base = "0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            int number = random.nextInt(base.length());
            sb.append(base.charAt(number));
        }
        return sb.toString();
    }

    /**
     * 判断当前日期与最后一次发送时间的差
     *
     * @param create
     * @throws ParseException
     */
    private static boolean getTime(long create) throws ParseException {
        Date dd = new Date(create);
        Calendar c = Calendar.getInstance();
        c.setTime(dd);
        c.set(Calendar.HOUR_OF_DAY, 23);
        c.set(Calendar.MINUTE, 59);
        c.set(Calendar.SECOND, 59);
        return System.currentTimeMillis() - c.getTimeInMillis() > 0;
    }

    /**
     * 众麦推广线索邮件通知
     *
     * @param from
     * @param toEmails
     * @param title
     * @param content
     */
    public void sendZmClueNotice(String from, String[] toEmails, String title, String content) {
        MailBean mailBean = new MailBean();
        mailBean.setFrom(from);
        mailBean.setFromName(title);
        mailBean.setSubject(title);
        mailBean.setToEmails(toEmails);
        mailBean.setContext(content);
        try {
            logger.info("发送众麦推广线索邮件通知数据:{}", JSON.toJSONString(mailBean));
            this.sendMail(mailBean);
        } catch (Exception e) {
            logger.error("发送众麦推广线索邮件通知失败", e);
        }
    }

    private static int fromEmailIndex = 1;

    public boolean sendEmailPureJava(String toEmail, String title, String content, int type, String emailFromPropertyName) {
        Assert.hasText(emailFromPropertyName, "emailFromPropertyName不能为空");
        List<Map<String, Object>> smtpList = jdbcTemplate.queryForList("SELECT property_value FROM t_system_config WHERE property_name = ? AND status = 1 ", "zm_email_source");
        if (smtpList == null || smtpList.size() == 0) {
            logger.error("smtp发件配置为空");
            return false;
        }
        JSONObject smtpConfig = new JSONObject();
        JSONArray configs = JSON.parseArray(String.valueOf(smtpList.get(0).get("property_value")));
        for (int i = 0; i < configs.size(); i++) {
            smtpConfig.put(configs.getJSONObject(i).getString("source"), configs.getJSONObject(i));
        }
        List<Map<String, Object>> fromList = jdbcTemplate.queryForList("SELECT property_value FROM t_system_config WHERE property_name = ? AND status = 1 ", emailFromPropertyName);
        if (fromList == null || fromList.size() == 0) {
            logger.error("发件人邮箱配置为空");
            return false;
        }
        String fromEmail = String.valueOf(fromList.get(0).get("property_value"));
        if (StringUtil.isEmpty(fromEmail)) {
            logger.error("发件人邮箱配置property_value为空");
            return false;
        }
        List<String> from = Arrays.asList(fromEmail.split("\\$"));
        if (from.size() == 0) {
            logger.error("发件人邮箱列表为空");
            return false;
        }
        if (from.size() < fromEmailIndex) {
            // 获取最后1个
            fromEmailIndex = 1;
        }
        String value = from.get(fromEmailIndex - 1);
        fromEmailIndex++;
        String[] values = value.split(",");
        String userName = values[0];
        String pwd = values[1];
        String source = values[2];
        logger.info("多邮件发送当前发送人{},密码:{},接收人:{},配置:{}", userName, pwd, toEmail, smtpConfig.getJSONObject(source));
        boolean b = false;
        try {
            b = new EmailUtil(userName, pwd, smtpConfig.getJSONObject(source).getString("smtp"),
                    smtpConfig.getJSONObject(source).getString("port"), smtpConfig.getJSONObject(source).getBooleanValue("ssl"))
                    .sendText(title, content, toEmail);
            logger.info("多邮件发送当前发送人{},接收人:{}发送状态:{}", userName, toEmail, b);
        } catch (Exception e) {
            logger.error("多邮件发送异常,发送人{},接收人:{}发送状态:{}", userName, toEmail, b, e);
        }
        return b;
    }
}
