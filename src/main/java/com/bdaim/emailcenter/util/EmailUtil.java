package com.bdaim.emailcenter.util;

import com.sun.mail.util.MailSSLSocketFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.security.GeneralSecurityException;
import java.util.Properties;

public class EmailUtil {

    private static Logger log = LoggerFactory.getLogger(EmailUtil.class);

    private Properties props;
    private Session session;
    private String userName;
    private String pwd;
    private String smtpHost;
    private String smtpPort;
    private boolean sslEnable;

    public EmailUtil(String userName, String pwd, String smtpHost, String smtpPort, boolean sslEnable) {
        this.userName = userName;
        this.pwd = pwd;
        this.smtpHost = smtpHost;
        this.smtpPort = smtpPort;
        this.sslEnable = sslEnable;

        props = new Properties();
        // 判断是否需要加密
        if (sslEnable) {
            String SSL_FACTORY = "javax.net.ssl.SSLSocketFactory";
            props.put("mail.smtp.socketFactory.class", SSL_FACTORY);
            props.put("mail.smtp.socketFactory.fallback", "false");
            props.put("mail.smtp.socketFactory.port", smtpPort);
            MailSSLSocketFactory sf = null;
            try {
                sf = new MailSSLSocketFactory();
            } catch (GeneralSecurityException e) {
                e.printStackTrace();
            }
            sf.setTrustAllHosts(true);
            props.put("mail.smtp.ssl.socketFactory", sf);
        }
        props.put("mail.smtp.port", smtpPort);
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", String.valueOf(sslEnable));
        try {
            // 获得邮件会话对象
            session = Session.getDefaultInstance(props, null);
        } catch (Exception e) {
            log.error("获取邮件会话对象时发生错误！" + e);
        }
    }

    public boolean sendText(String subject, String body, String sendTo) {
        MimeMessage mimeMsg = null;
        Multipart mp = null;
        try {
            try {
                mimeMsg = new MimeMessage(session);
                mp = new MimeMultipart();
            } catch (Exception e) {
                e.printStackTrace();
                log.error("创建MIME邮件对象失败！" + e);
                return false;
            }
            try {
                mimeMsg.setSubject(subject);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("设置邮件主题发生错误！");
                return false;
            }
            try {
                BodyPart bp = new MimeBodyPart();
                bp.setContent("" + body, "text/html;charset=utf-8");
                mp.addBodyPart(bp);
            } catch (Exception e) {
                e.printStackTrace();
                log.error("设置邮件正文时发生错误！" + e);
                return false;
            }
            try {
                mimeMsg.setFrom(new InternetAddress(userName, userName));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("设置发件人发生错误！");
                return false;
            }
            try {
                mimeMsg.setRecipients(Message.RecipientType.TO,
                        InternetAddress.parse(sendTo));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("设置收件人发生错误！" + e);
                return false;
            }
            mimeMsg.setContent(mp);
            mimeMsg.saveChanges();
            Session mailSession = Session.getInstance(props, null);
            Transport transport = mailSession.getTransport("smtp");
            transport.connect(smtpHost, userName, pwd);
            transport.sendMessage(mimeMsg,
                    mimeMsg.getRecipients(Message.RecipientType.TO));
            transport.close();
        } catch (Exception e) {
            e.printStackTrace();
            log.error("邮件发送失败！" + e);
            return false;
        }
        return true;
    }

    public static void main(String[] args) {
        try {
            boolean b = new EmailUtil("chengning@bdaim.com", "giant2020tcr.", "smtp.mxhichina.com", "465", true)
                    .sendText("测试数据", "测试数据", "1005266424@qq.com");
            System.out.println(b);
            //sendAttachmentMessage("测试带附件发邮件", "wuhe@bdaim.com,xiayan@bdaim.com", "", "E:\\获客\\工作记录\\周工作情况20190426吴鹤.xlsx");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}