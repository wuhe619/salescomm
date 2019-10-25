package com.bdaim.smscenter.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.auth.entity.UserVerificationCode;
import com.bdaim.auth.service.UserVerificationCodeService;
import com.bdaim.common.service.PhoneService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.markettask.service.MarketTaskService;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.smscenter.dao.SmsDao;
import com.bdaim.smscenter.dto.CallBackSmsDTO;
import com.bdaim.smscenter.dto.SendSmsDTO;
import com.bdaim.smscenter.dto.YxtSmsParam;
import com.bdaim.smscenter.entity.TouchSmsQueue;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.template.dao.MarketTemplateDao;
import com.bdaim.template.dto.MarketTemplateDTO;
import com.bdaim.template.entity.MarketTemplate;
import com.bdaim.util.*;
import com.bdaim.util.http.HttpUtil;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.expression.ParseException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;

/**
 * 营销资源service服务实现类
 * 2017/2/21
 *
 * @author lich@bdcsdk.com
 */
@Service("sendSmsService")
@Transactional
public class SendSmsService{

    ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 0,
            TimeUnit.SECONDS, new LinkedBlockingDeque<Runnable>(),
            new ThreadPoolExecutor.CallerRunsPolicy());
    
    private static Logger logger = LoggerFactory.getLogger(SendSmsService.class);
    //获取短信话术URL
    private final static String QUERY_MESSAGE_URL = "http://120.52.23.243:10080/jzyxpt";
    //联通发送短信地址
    private final static String SEND_SMS_URL = "http://120.52.23.243:10080/jzyxpt";
    private static String url = "http://115.231.73.234:7602/sms.aspx";
    private static String userid = "556";
    private static String account = "bjztwjxxhy6";
    private static String password = "qweasd";
    /**
     * 短信最大发送次数
     */
    private final static int SMS_MAX_NUMBER = 99;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private RestTemplate restTemplate;
    @Resource
    private UserVerificationCodeService userVerificationCodeService;

    /**
     * 1-生产 2-dev
     */
    public static final int VC_CODE_ENV = 2;

    public static final String DEV_VC_CODE_CUSTID = "1901090555360004";

    public static final String PRD_VC_CODE_CUSTID = "1901090926395927";
    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private SupplierDao supplierDao;
    @Resource
    private PhoneService phoneService;
    @Resource
    private SmsDao smsDao;
    @Resource
    private MarketTemplateDao marketTemplateDao;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private MarketTaskService marketTaskService;
    @Resource
    private CustomGroupService customGroupService;
    
    
    public String sendSmsVcCodeByRestAPI(String phone, String templateId, String templateValue) {
        MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<>();
        urlVariables.add("interfaceID", "SaleSmsService");
        urlVariables.add("type", String.valueOf(1));
        urlVariables.add("phone", phone);
        urlVariables.add("templateId", templateId);
        urlVariables.add("templateValue", templateValue);

        String result = restTemplate.postForObject(Constant.LABEL_API
                + "/sales/rest.do", urlVariables, String.class);

        return result;
    }
    
    
    /**
     * @description 短信话术查询
     * @author:duanliying
     * @method entId-----企业Id
     * @date: 2018/10/15 10:08
     */
    public String queryMessageWord(String entId) {
        Map<String, String> params = new HashMap<>();
        params.put("entId", entId);
        String result;
        try {
            result = HttpUtil.httpPost(QUERY_MESSAGE_URL + "/callout/queryMessageWord", params, null);
            logger.info("获取短信话术码返回:" + result);
            if (StringUtil.isNotEmpty(result)) {
                return JSON.toJSONString(result);
            }
        } catch (Exception e) {
            logger.error("获取短信话术码失败:", e);
            throw new RuntimeException("获取短信话术码失败", e);
        }
        return null;
    }

    /**
     * @description
     * @author:duanliying
     * @method
     * @date: 2018/10/15 10:11
     */
    
    public String sendSmsService(SendSmsDTO sendSmsDTO, String id, String batchId) {
        String result;
        //根据id和batch_id查询活动id和省份id
        try {
            List<Map<String, Object>> batchDetail = jdbcTemplate.queryForList("SELECT * FROM nl_batch_detail WHERE batch_id = ? AND id = ?", batchId, id);
            String activityId = null, provideId = null;
            if (batchDetail.size() > 0) {
                activityId = String.valueOf(batchDetail.get(0).get("activity_id"));
                provideId = String.valueOf(batchDetail.get(0).get("provide_id"));
            }
            Map<String, String> params = new HashMap<>();
            //企业ID

            params.put("entId", sendSmsDTO.getEntId());

            //活动ID
            params.put("activityId", activityId);
            //客户ID
            params.put("customerId", id);
            //省份ID
            params.put("provideId", provideId);
            //话术码  通过entId调用联通接口查询获取
            params.put("messageCode", sendSmsDTO.getMessageWord());
            //变量标识
            params.put("variableOne", sendSmsDTO.getVariableOne());
            params.put("variableTwo", sendSmsDTO.getVariableTwo());
            params.put("variableThree", sendSmsDTO.getVariableThree());
            params.put("variableFour", sendSmsDTO.getVariableFour());
            params.put("variableFive", sendSmsDTO.getVariableFive());
            logger.info("联通发送短信返回:" + params.toString());
            result = HttpUtil.httpPost(SEND_SMS_URL + "/callout/sendMessageData", params, null);
            logger.info("联通发送短信返回:" + result);
        } catch (Exception e) {
            logger.error("联通发送短信失败:", e);
            throw new RuntimeException("联通发送短信失败", e);
        }
        return result;
    }

    public void sendVerifyCodeDeduction(String supplierTemplateCode, String value) {
        logger.info("开始记录验证码发送记录并且扣费,supplierTemplateCode:" + supplierTemplateCode);
        String custId = "1905310945000013";
        // dev环境
        if (VC_CODE_ENV == 1) {
            custId = PRD_VC_CODE_CUSTID;
        } else {
            custId = DEV_VC_CODE_CUSTID;
        }
        MarketTemplate marketTemplate = marketResourceDao.getMarketTemplate(supplierTemplateCode, custId);
        logger.info("验证码发送记录marketTemplate:" + marketTemplate);
        if (marketTemplate != null) {
            String resourceId = marketTemplate.getResourceId();
            logger.info("验证码发送记录resourceId:" + resourceId);
            if (StringUtil.isNotEmpty(resourceId)) {
                CustomerProperty sms_config = customerDao.getProperty(custId, "sms_config");
                logger.info("验证码发送记录客户售价配置:" + sms_config);
                if (sms_config != null && StringUtil.isNotEmpty(sms_config.getPropertyValue())) {
                    JSONArray jsonArray = JSON.parseArray(sms_config.getPropertyValue());
                    JSONObject custSmsConfig = null, supplierConfig = null;
                    for (int i = 0; i < jsonArray.size(); i++) {
                        if (resourceId.equals(jsonArray.getJSONObject(i).getString("resourceId"))) {
                            custSmsConfig = jsonArray.getJSONObject(i);
                            break;
                        }
                    }
                    if (custSmsConfig != null) {
                        String supplierId = "";
                        MarketResourceEntity mr = marketResourceDao.getMarketResource(NumberConvertUtil.parseInt(resourceId));
                        if (mr != null) {
                            supplierId = mr.getSupplierId();
                        }
                        ResourcePropertyEntity marketResourceProperty = marketResourceDao.getProperty(resourceId, "price_config");
                        if (marketResourceProperty != null && StringUtil.isNotEmpty(marketResourceProperty.getPropertyValue())) {
                            supplierConfig = JSONObject.parseObject(marketResourceProperty.getPropertyValue());
                        }
                        String insertSql = "insert INTO t_touch_sms_log  (touch_id, cust_id, create_time, amount, prod_amount, resource_id,templateId, sms_content, remark) VALUES(?,?,?,?,?,?,?,?,?)";
                        String requestId = Long.toString(IDHelper.getTransactionId());
                        logger.info("验证码发送记录requestId:" + requestId + ",custId:" + custId + ",custSmsConfig:" + custSmsConfig + ",supplierConfig:" + supplierConfig);
                        marketResourceDao.executeUpdateSQL(insertSql, requestId, custId, new Timestamp(System.currentTimeMillis()), NumberConvertUtil.changeY2L(custSmsConfig.getDoubleValue("price")),
                                NumberConvertUtil.changeY2L(supplierConfig.getDoubleValue("price")), marketTemplate.getResourceId(), supplierTemplateCode, marketTemplate.getMouldContent(), value);
                        try {
                            customerDao.accountDeductions(custId, new BigDecimal(NumberConvertUtil.changeY2L(custSmsConfig.getDoubleValue("price"))));
                            logger.info("验证码发送扣费custId:" + custId + "金额:" + NumberConvertUtil.changeY2L(custSmsConfig.getDoubleValue("price")));
                            supplierDao.supplierAccountDeductions(supplierId, new BigDecimal(NumberConvertUtil.changeY2L(custSmsConfig.getDoubleValue("price"))));
                            logger.info("验证码发送扣费供应商ID:" + supplierId + "金额:" + NumberConvertUtil.changeY2L(custSmsConfig.getDoubleValue("price")));
                        } catch (Exception e) {
                            logger.error("验证码发送扣费custId" + custId + "失败");
                        }
                    } else {
                        logger.warn("验证码发送记录未查询到对应的客户短信配置,resourceId:" + resourceId + ",sms_config:" + sms_config);
                    }
                }
            }
        }
    }


    /**
     * 发送短信扣除客户和供应商费用并且保存发送记录
     *
     * @param templateId
     * @param custId
     * @param custGroupId
     * @param superId
     * @param userId
     * @param sendStatus
     * @param respResult
     */
    private void sendSmsDeduction(String templateId, String custId, String custGroupId, String superId, String userId, int sendStatus, String respResult, String callId) {
        logger.info("开始发送短信并且扣费,templateId:" + templateId);
        // 查询短信模板
        MarketTemplate marketTemplate = marketResourceDao.getMarketTemplate(NumberConvertUtil.parseInt(templateId), 1, custId);
        if (marketTemplate == null) {
            logger.warn("发送短信未查询到对应的短信模板,templateId:" + templateId + ",custId:" + custId + ",custGroupId:" + custGroupId + ",superId:" + superId + ",callId:" + callId);
            return;
        }
        logger.info("发送短信marketTemplate:" + marketTemplate);
        String resourceId = marketTemplate.getResourceId();
        logger.info("发送短信resourceId:" + resourceId);
        if (StringUtil.isEmpty(resourceId)) {
            logger.warn("发送短信未查询到模板对应的resourceId,templateId:" + templateId + ",custId:" + custId + ",custGroupId:" + custGroupId + ",superId:" + superId + ",callId:" + callId);
            return;
        }

        // 查询客户配置的短信渠道
        CustomerProperty sms_config = customerDao.getProperty(custId, "sms_config");
        logger.info("发送短信客户售价配置:" + sms_config);
        if (sms_config == null || (sms_config != null && StringUtil.isEmpty(sms_config.getPropertyValue()))) {
            logger.warn("发送短信未查询客户配置的短信资源resourceId" + resourceId + ",templateId:" + templateId + ",custId:" + custId + ",custGroupId:" + custGroupId + ",superId:" + superId + ",callId:" + callId);
            return;
        }

        JSONArray jsonArray = JSON.parseArray(sms_config.getPropertyValue());
        JSONObject custSmsConfig = null, supplierConfig = null;
        for (int i = 0; i < jsonArray.size(); i++) {
            if (resourceId.equals(jsonArray.getJSONObject(i).getString("resourceId"))) {
                custSmsConfig = jsonArray.getJSONObject(i);
                break;
            }
        }
        if (custSmsConfig == null) {
            logger.warn("发送短信未查询到对应的客户短信配置,resourceId:" + resourceId + ",sms_config:" + sms_config);
            return;
        }
        ResourcePropertyEntity marketResourceProperty = marketResourceDao.getProperty(resourceId, "price_config");
        if (marketResourceProperty != null && StringUtil.isNotEmpty(marketResourceProperty.getPropertyValue())) {
            supplierConfig = JSONObject.parseObject(marketResourceProperty.getPropertyValue());
        }
        String insertSql = "insert INTO t_touch_sms_log  (touch_id, cust_id, create_time, amount, prod_amount, resource_id, templateId, " +
                " customer_group_id, sms_content, superid, user_id, status, send_data, remark) VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        String requestId = Long.toString(IDHelper.getTransactionId());
        logger.info("发送短信requestId:" + requestId + ",custId:" + custId + ",custSmsConfig:" + custSmsConfig + ",supplierConfig:" + supplierConfig);
        marketResourceDao.executeUpdateSQL(insertSql, requestId, custId, new Timestamp(System.currentTimeMillis()), NumberConvertUtil.changeY2L(custSmsConfig.getDoubleValue("price")),
                NumberConvertUtil.changeY2L(supplierConfig.getDoubleValue("price")), marketTemplate.getResourceId(), templateId,
                custGroupId, marketTemplate.getMouldContent(), superId, userId, sendStatus, respResult, callId);
        try {
            // 客户短信扣费
            customerDao.accountDeductions(custId, new BigDecimal(NumberConvertUtil.changeY2L(custSmsConfig.getDoubleValue("price"))));
            logger.info("发送短信客户扣费custId:" + custId + "金额:" + NumberConvertUtil.changeY2L(custSmsConfig.getDoubleValue("price")));
            // 供应商短信扣费
            List<Map<String, Object>> marketResource = customerDao.sqlQuery("SELECT * FROM t_market_resource WHERE resource_id = ? AND `status` = 1", resourceId);
            String supplierId = null;
            if (marketResource != null && marketResource.size() > 0) {
                supplierId = String.valueOf(marketResource.get(0).get("supplier_id"));
            }
            logger.info("发送短信供应商扣费supplierId:" + supplierId + "金额:" + NumberConvertUtil.changeY2L(supplierConfig.getDoubleValue("price")));
            supplierDao.supplierAccountDeductions(supplierId, new BigDecimal(NumberConvertUtil.changeY2L(supplierConfig.getDoubleValue("price"))));
        } catch (Exception e) {
            logger.error("发送短信扣费custId" + custId + "失败");
        }

    }

    public String sendSmsVcCodeByRestAPI(String phone, String templateId, String templateValue, int type) {
        YxtSmsParam smsParam = new YxtSmsParam();
        smsParam.setAction(SaleApiUtil.TEMPLATE_SMS_ACTION);
        smsParam.setAppid(SaleApiUtil.SMS_APP_ID);
        smsParam.setMobile(phone);
        smsParam.setTemplateId(templateId);
        smsParam.setDatas(Arrays.asList(templateValue.split(",")));
        smsParam.setSpuid(PropertiesUtil.getStringValue("ytx.spuid"));
        smsParam.setSppwd(PropertiesUtil.getStringValue("ytx.sppwd"));
        String result = SaleApiUtil.sendSms(JSON.toJSONString(smsParam), SaleApiUtil.ENV);
        LogUtil.info("模板ID:" + smsParam.getTemplateId() + "----手机号：" + smsParam.getMobile());
        //短信
        if (SaleApiUtil.SMS_TYPE == type) {
            smsParam.setAppid(SaleApiUtil.SMS_APP_ID);
            LogUtil.info("短信发送结果:" + result);
            //闪信
        } else if (SaleApiUtil.FLASH_TYPE == type) {
            smsParam.setAppid(SaleApiUtil.FLASH_APP_ID);
            LogUtil.info("闪信发送结果:" + result);
        }

        return result;
    }

    /**
     * 通过接口发送营销短信
     *
     * @param phone
     * @param templateId
     * @param type
     * @return
     */
    private String sendMarketSmsByRestAPI(String phone, String templateId, int type) {
        if (StringUtil.isEmpty(phone)) {
            logger.warn("手机号为空,phone:" + phone);
            return "";
        }
        if (StringUtil.isEmpty(templateId)) {
            logger.warn("模板为空,templateId:" + templateId);
            return "";
        }
        Map<String, Object> params = new HashMap<>();
        params.put("action", SaleApiUtil.TEMPLATE_SMS_ACTION);
        params.put("appid", SaleApiUtil.MARKET_SMS_APP_ID);
        //短信
        if (SaleApiUtil.SMS_TYPE == type) {
            params.put("appid", SaleApiUtil.MARKET_SMS_APP_ID);
            //闪信
        } else if (SaleApiUtil.FLASH_TYPE == type) {
            params.put("appid", SaleApiUtil.MARKET_SMS_APP_ID);
        }
        params.put("templateId", templateId);
        params.put("spuid", SaleApiUtil.MARKET_SP_UID);
        params.put("sppwd", SaleApiUtil.MARKET_SP_PWD);
        if (phone.contains("b")) {
            phone = phone.replace("b", "");
        }
        params.put("mobile", phone);
        logger.info("营销短信发送参数:" + params.toString());
        String result = SaleApiUtil.sendMarketSms(JSON.toJSONString(params), SaleApiUtil.ENV);
        logger.info("营销短信模板ID:" + templateId + ",phone:" + phone);
        logger.info("营销短信发送结果:" + result);
        return result;
    }

    /**
     * 验证码验证功能
     *
     * @author:duanliying
     * @description
     * @method
     * @date: 2018/8/13 15:48
     */
    public int verificationCode(String phone, int type, String code) {
        UserVerificationCode dto;
        Map<String, Object> params = new HashMap<>();
        params.put("phone", phone);
        params.put("type", type);
        //查询出一个userVerificationCode对象
        dto = userVerificationCodeService.getUserVerificationCodeByCondition(params);
        if (dto == null) return 0;
        //获取创建时间计算时间差和验证码验证
        if (System.currentTimeMillis() - dto.getSendTime() <= dto.getEffectiveTimeLength() * 60 * 1000 && code.equals(dto.getVcVode())) {
            dto.setStatus(2);
            smsDao.saveOrUpdate(dto);
            return 1;
        }
        return 0;
    }

    class YtxCallbackSmsThread implements Callable<Integer> {
        CallBackSmsDTO dto;

        public YtxCallbackSmsThread(CallBackSmsDTO dto) {
            this.dto = dto;
        }

        @Override
        public Integer call() {
            return execute(dto);
        }

        public int execute(CallBackSmsDTO dto) {
            if (dto == null || StringUtil.isEmpty(dto.getRequestid())) {
                logger.warn("云讯短信回调异常:" + dto.toString());
                return 0;
            }
            int status = 1000;
            if ("1".equals(dto.getSendSts())) {
                // 成功
                status = 1001;
            } else if ("-1".equals(dto.getSendSts())) {
                // 失败
                status = 1002;
            }
            int updateNum = 0;
            //根据requestId查询短信记录是否存在
            String querySql = "SELECT touch_id, `status` FROM t_touch_sms_log WHERE request_id = ?";
            String updateSql = "UPDATE t_touch_sms_log SET send_data= ?, status=?, send_status=? WHERE request_id = ?";
            List<Map<String, Object>> touchIdList = marketResourceDao.sqlQuery(querySql, dto.getRequestid());
            //如果记录存在根据touch_id更新数据库
            if (touchIdList.size() > 0) {
                updateNum = marketResourceDao.executeUpdateSQL(updateSql, JSON.toJSONString(dto), status, status, dto.getRequestid());
            } else {
                logger.warn("云讯短信回调未查询到发送记录,request_id:" + dto.getRequestid());
            }
            return updateNum;
        }
    }

    /**
     * @description 云讯开放平台短信结果回调
     * @author:duanliying
     * @method
     * @date: 2018/10/8 13:57
     */
    public Integer ytxCallbackSms(CallBackSmsDTO dto) {
        Future<Integer> submit = executor.submit(new YtxCallbackSmsThread(dto));
        try {
            return submit.get();
        } catch (InterruptedException e) {
            logger.error("云讯开放平台短信结果回调处理异常,", e);
        } catch (ExecutionException e) {
            logger.error("云讯开放平台短信结果回调处理异常,", e);
        }
        return 0;
    }


    /**
     * 通过云讯开放平台发送短信验证码
     *
     * @param phone
     * @param type     类型 1-注册验证码 2-登陆验证码 3-修改登录密码 4-修改手机时，（验证旧手机号）发送验证码 5-修改手机时，（验证新手机号）发送验证码 6-找回密码时，发送验证码 7-修改支付密码 8-金融超市注册/登录验证码
     * @param username
     * @return
     */
    public Object sendSmsVcCodeByCommChinaAPI(String phone, int type, String username) {
        String code, message;
        String vcCode = getRandomString();
        UserVerificationCode userVerificationCode = null;
        int sendNum = 0;
        try {
            long lastSendTime = System.currentTimeMillis();
            Map<String, Object> params = new HashMap<>();
            params.put("phone", phone);
            params.put("type", type);
            userVerificationCode = userVerificationCodeService.getUserVerificationCodeByCondition(params);
            if (userVerificationCode != null) {
                if (userVerificationCode.getSendNum() != null) {
                    sendNum = userVerificationCode.getSendNum();
                }
                if (userVerificationCode.getSendTime() != null) {
                    lastSendTime = userVerificationCode.getSendTime();
                }
            }
            logger.info("当前时间:" + LocalDateTime.now() + ",手机号:" + phone + ",类型:" + type + ",上次发送时间:" + new Date(lastSendTime));
            //计算当前时间与最后一次发送时间是否在同一天
            if (getTime(lastSendTime)) {
                sendSms(vcCode, phone, type, username, 5);
                code = "1";
                message = "发送完成";
                sendNum = 1;
            } else {
                //最后发送时间和当前时间在同一天,则判断次数
                if (sendNum <= SMS_MAX_NUMBER) {
                    logger.info("当前手机:" + phone + ",类型:" + type + ",上次发送时间:" + new Date(lastSendTime) + "已经发送次数：" + sendNum);
                    sendSms(vcCode, phone, type, username, 5);
                    code = "1";
                    message = "发送完成";
                    sendNum += 1;
                } else {
                    logger.info("当前手机:" + phone + ",类型:" + type + ",上次发送时间:" + new Date(lastSendTime) + "发送次数达到上限：" + sendNum);
                    code = "0";
                    message = "当前手机发送次数达到上限";
                }
            }
        } catch (Exception e) {
            code = "0";
            message = "发送验证码失败";
            logger.error("发送验证码失败,手机号:" + phone + ",类型:" + type, e);
        }
        // 如果验证码发送成功则保存发送验证码的手机号和次数至数据库
        if ("1".equals(code)) {
            if (userVerificationCode != null) {
                userVerificationCode.setSendNum(sendNum);
                userVerificationCode.setSendStatus(1);
                userVerificationCode.setSendTime(System.currentTimeMillis());
                userVerificationCode.setVcVode(vcCode);
                userVerificationCode.setEffectiveTimeLength(5);
                userVerificationCode.setType(type);
                userVerificationCodeService.updateUserVerificationCode(userVerificationCode);
            } else {
                userVerificationCode = new UserVerificationCode();
                userVerificationCode.setPhone(phone);
                userVerificationCode.setSendNum(1);
                userVerificationCode.setSendStatus(1);
                userVerificationCode.setVcVode(vcCode);
                userVerificationCode.setSendTime(System.currentTimeMillis());
                userVerificationCode.setEffectiveTimeLength(5);
                userVerificationCode.setCreateTime(System.currentTimeMillis());
                userVerificationCode.setStatus(1);
                userVerificationCode.setType(type);
                userVerificationCodeService.addUserVerificationCode(userVerificationCode);
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("code", code);
        resultMap.put("_message", message);
        resultMap.put("data", "");
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @param vCode    验证码
     * @param phone    手机号
     * @param type     类型 1-注册验证码 2-登陆验证码 3-修改登录密码 4-修改手机时，（验证旧手机号）发送验证码 5-修改手机时，（验证新手机号）发送验证码 6-找回密码时，发送验证码 7-修改支付密码 8-金融超市注册/登录验证码
     * @param username 用户名称 type=5修改绑定手机时验证新手机使用,其他类型验证码此字段可以为空
     * @param timeOut  超时时间(分钟)
     * @return java.lang.String
     * @author chengning@salescomm.net
     * @date 2018/7/31 14:01
     */
    private String sendSms(String vCode, String phone, int type, String username, long timeOut) {
        String name = "";
        if (StringUtil.isNotEmpty(username)) {
            if (username.getBytes().length <= 6) {
                name = username.substring(0, 1) + "*";
            } else {
                name = username.substring(0, 1) + "**" + username.substring(username.length() - 1, username.length());
            }
        }
        String result, templateId;
        List<String> values = new ArrayList<>();
        switch (type) {
            // 注册验证码
            case 1:
                templateId = "1909";
                values.add(vCode);
                values.add(String.valueOf(timeOut));
                break;
            // 登陆验证码
            case 2:
                templateId = "1927";
                values.add(vCode);
                values.add(String.valueOf(timeOut));
                break;
            //修改登录密码时，发送验证码
            case 3:
                templateId = "1910";
                values.add(vCode);
                break;
            //修改手机时，（验证旧手机号）发送验证码
            case 4:
                templateId = "1908";
                values.add(vCode);
                values.add(String.valueOf(timeOut));
                break;
            //修改手机时，（验证新手机号）发送验证码
            case 5:
                templateId = "1928";
                values.add(name);
                values.add(vCode);
                values.add(String.valueOf(timeOut));
                break;
            //找回密码时，发送验证码
            case 6:
                templateId = "1929";
                values.add(vCode);
                values.add(String.valueOf(timeOut));
                break;
            //修改支付密码
            case 7:
                templateId = "1911";
                values.add(vCode);
                values.add("5");
                break;
            //金融超市注册/登录验证码
            case 8:
                templateId = "2586";
                values.add(vCode);
                values.add("5");
                break;
            //金融超市购买商品验证码
            case 9:
                templateId = "2588";
                values.add(vCode);
                values.add("10");
                break;
            default:
                throw new RuntimeException("type match value！");
        }
        sendVerifyCodeDeduction(templateId, phone + "," + StringUtils.join(values, ","));

        logger.info("当前时间：" + LocalDateTime.now());
        result = sendSmsVcCodeByRestAPI(phone, templateId, StringUtils.join(values, ","), 1);
        logger.info("模板ID:" + templateId + "----手机号：" + phone);
        logger.info("发送结果: " + result);
        return result;
    }


    /**
     * 发送营销短信(固定内容)
     *
     * @param custId
     * @param custGroupId
     * @param superId
     * @param userId
     * @param templateId
     * @return
     */
    public String sendMarketSms(String custId, String custGroupId, String superId, String userId, String templateId, String callId) {
        //根据身份ID查询手机号
        String phone = phoneService.getPhoneBySuperId(superId);
        String result = null;
        logger.info("发送短信当前时间：" + LocalDateTime.now() + ",custId:" + custId + ",custGroupId:" + custGroupId + ",superId:" + superId
                + ",模板ID:" + templateId + ",手机号:" + phone + ",callId:" + callId);
        MarketTemplate marketTemplate = marketResourceDao.getMarketTemplate(NumberConvertUtil.parseInt(templateId), 1, custId);
        logger.info("发送短信查询到的模板信息:" + marketTemplate);
        if (marketTemplate != null) {
            logger.info("发送短信当前时间：" + LocalDateTime.now() + ",custId:" + custId + ",custGroupId:" + custGroupId + ",superId:" + superId
                    + ",第三方模板ID:" + marketTemplate.getTemplateCode() + ",手机号:" + phone + ",callId:" + callId);
            result = sendMarketSmsByRestAPI(phone, marketTemplate.getTemplateCode(), 1);
            logger.info("发送短信结果: " + result);
            //默认发送中状态
            int sendStatus = 1003;
            if (StringUtil.isNotEmpty(result)) {
                JSONObject json = JSONObject.parseObject(result);
                if (json.getIntValue("statusCode") == 0) {
                    sendStatus = 1001;
                } else {
                    sendStatus = 1002;
                }
            }
            // 短信扣费
            sendSmsDeduction(templateId, custId, custGroupId, superId, userId, sendStatus, result, callId);
        }
        return result;
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
     * 判断当前时间与最后一次发送时间的23:59:59的时间差
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
     * 查询模板信息
     *
     * @param templateId
     * @param typeCode
     * @param custId
     * @return
     */
    public MarketTemplateDTO getTemplate(String templateId, int typeCode, String custId) {
        MarketTemplate marketTemplate = marketResourceDao.getMarketTemplate(NumberConvertUtil.parseInt(templateId), 1, custId);
        if (marketTemplate != null) {
            return new MarketTemplateDTO(marketTemplate);
        }
        return null;
    }

    /**
     * 发送短信至短信队列
     *
     * @param custId
     * @param custGroupId
     * @param superIds
     * @param templateId
     * @param userId
     * @param marketTaskId
     * @return
     */
    public String sendSmsToQueue(String custId, String custGroupId, List<String> superIds, String templateId, String userId,
                                 String marketTaskId) {
        if (superIds != null && superIds.size() > 0) {
            MarketTemplate template = marketTemplateDao.findUniqueBy("id", Integer.valueOf(templateId));
            if (template == null || StringUtil.isEmpty(template.getResourceId())) return null;
            String resourceId = template.getResourceId();
            final String sql = "INSERT INTO `t_touch_sms_queue` (`template_id`, `cust_id`, `customer_group_id`, `superid`, `create_time`, batch_number,resource_id, user_id, market_task_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?);";
            Timestamp createTime = new Timestamp(System.currentTimeMillis());
            String batchNumber = String.valueOf(IDHelper.getTouchId());
            try {
                jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setString(1, templateId);
                        preparedStatement.setString(2, custId);
                        preparedStatement.setString(3, custGroupId);
                        preparedStatement.setString(4, superIds.get(i));
                        preparedStatement.setTimestamp(5, createTime);
                        preparedStatement.setString(6, batchNumber);
                        preparedStatement.setString(7, resourceId);
                        preparedStatement.setString(8, userId);
                        preparedStatement.setString(9, marketTaskId);
                    }

                    @Override
                    public int getBatchSize() {
                        return superIds.size();
                    }
                });
            } catch (DataAccessException e) {
                logger.error("批量保存至短信队列表失败,", e);
            }
            return batchNumber;
        } else {
            logger.warn("批量发送短信保存至队列custId:" + custId + ",custGroupId:" + custGroupId + ",templateId:" + templateId + ",superIds为空:" + superIds.toString());
        }
        return "";
    }

    /**
     * 发送私海短信至短信队列
     *
     * @param custId
     * @param superIds
     * @param templateId
     * @param userId
     * @param seaId
     * @param batchName
     * @return
     */
    public String sendSeaSmsToQueue(String custId, List<Map<String, String>> superIds, String templateId, String userId,
                                    String seaId, final String batchName) {
        if (superIds != null && superIds.size() > 0) {
            MarketTemplate template = marketTemplateDao.findUniqueBy("id", Integer.valueOf(templateId));
            if (template == null || StringUtil.isEmpty(template.getResourceId())) return null;
            String resourceId = template.getResourceId();
            final String sql = "INSERT INTO `t_touch_sms_queue` (`template_id`, `cust_id`, `customer_group_id`, `superid`, `create_time`, batch_number,resource_id, user_id,customer_sea_id,sms_batch_name) VALUES (?, ?, ?, ?, ?, ?, ?, ?,?,?);";
            Timestamp createTime = new Timestamp(System.currentTimeMillis());
            String batchNumber = String.valueOf(IDHelper.getTouchId());
            try {
                jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                    @Override
                    public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                        preparedStatement.setString(1, templateId);
                        preparedStatement.setString(2, custId);
                        preparedStatement.setString(3, superIds.get(i).get("customerGroupId"));
                        preparedStatement.setString(4, superIds.get(i).get("supperid"));
                        preparedStatement.setTimestamp(5, createTime);
                        preparedStatement.setString(6, batchName);
                        preparedStatement.setString(7, resourceId);
                        preparedStatement.setString(8, userId);
                        preparedStatement.setString(9, seaId);
                        preparedStatement.setString(10, batchName);
                    }

                    @Override
                    public int getBatchSize() {
                        return superIds.size();
                    }
                });
            } catch (DataAccessException e) {
                logger.error("批量保存至短信队列表失败,", e);
            }
            return batchNumber;
        } else {
            logger.warn("批量发送短信保存至队列custId:" + custId + ",seaid:" + seaId + ",templateId:" + templateId + ",superIds为空:" + superIds.toString());
        }
        return "";
    }

    /**
     * 批量发送公海短信
     *
     * @param custId
     * @param userId
     * @param templateId
     * @param list
     * @return
     */
    public String sendCusomerSeaSmsToQueue(String custId, String userId, String templateId, List<TouchSmsQueue> list) {
        if (list != null && list.size() > 0) {
            Timestamp createTime = new Timestamp(System.currentTimeMillis());
            String batchNumber = String.valueOf(IDHelper.getTouchId());
            for (int i = 0; i < list.size(); i++) {
                list.get(i).setTemplateId(NumberConvertUtil.parseInt(templateId));
                list.get(i).setBatchNumber(batchNumber);
                list.get(i).setCreateTime(createTime);
                list.get(i).setCustId(custId);
                list.get(i).setUserId(userId);
            }
            smsDao.batchSaveSmsQueue(list);
            return batchNumber;
        } else {
            logger.warn("批量发送公海短信保存至队列custId:" + custId + ",templateId:" + templateId + ",superIds为空:" + list.toString());
        }
        return "";
    }

    /**
     * 查询短信发送记录
     *
     * @param custId
     * @param batchNumber
     * @param superId
     * @return
     */
    public Map<String, Object> getSendSmsLog(String custId, String batchNumber, String superId) {
        String sql = "SELECT status, sms_content smsContent, templateId, batch_number batchNumber, create_time createTime, active_time activeTime FROM t_touch_sms_log WHERE cust_id = ? AND superid = ? AND batch_number = ?";
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(sql, custId, superId, batchNumber);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 短信回调变更状态和扣费
     *
     * @return
     */
    public void insertSmsLogBackInfo(String data) throws Exception {
        logger.info("推送数据信息是：" + data);
        Timestamp createTime = new Timestamp(System.currentTimeMillis());
        String insertSql = "INSERT INTO queue_sms_callback ( create_time, push_data ) VALUES (?,?)";
        int num = marketResourceDao.executeUpdateSQL(insertSql, createTime, data);
        logger.info("保存结果是:" + num);
    }

    /**
     * 批量发送短信
     *
     * @param custId
     * @param marketTaskId
     * @param customerGroupId
     * @param superId
     * @param templateId
     * @param loginUser
     * @return
     */
    public int sendBatchSms(String custId, String marketTaskId, String customerGroupId, String superId, String templateId, LoginUser loginUser) {
        // 判断余额
        boolean amountStatus = marketResourceService.judRemainAmount(custId);
        if (!amountStatus) {
            logger.warn("客户:[" + custId + "]余额不足");
            return 1003;
        }

        JSONObject jsonSuperId = JSONObject.parseObject(superId);
        Set<String> superIds = new HashSet<>();
        // 部分发送
        if (jsonSuperId != null && jsonSuperId.size() > 0) {
            JSONArray superIdList = jsonSuperId.getJSONArray("data");
            JSONObject jsonItem;
            for (int i = 0; i < superIdList.size(); i++) {
                jsonItem = superIdList.getJSONObject(i);
                superIds.add(jsonItem.getString("superid"));
            }
        } else {
            // 全部发送客户群短信
            List<Map<String, Object>> list = null;
            if (StringUtil.isNotEmpty(marketTaskId)) {
                list = marketTaskService.listMarketTaskData(loginUser, marketTaskId, null, null, "", "", null, "", "", "");
            } else if (StringUtil.isNotEmpty(customerGroupId)) {
                list = customGroupService.getCustomGroupDataV3(loginUser, customerGroupId, null, null, "", "", null, "", "", "");
            }

            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    if (list.get(i) != null) {
                        superIds.add(String.valueOf(list.get(i).get("id")));
                    }
                }
            } else {
                logger.warn("客户:[" + custId + "]请选择要发送的数据");
                return 1003;
            }
        }
        Integer num = superIds.size();
        // 判断客户余额是否足够用于此次发送的短信数量
        boolean flag1 = marketResourceService.checkAmount0(custId, num, templateId);
        if (!flag1) {
            logger.warn("客户:[" + custId + "]余额不足或未配置供应商售价");
            return 1003;
        } else {
            logger.info("短信发送custId:" + custId + ",custGroupId:" + customerGroupId + ",userId:" + loginUser.getId() + ",templateId:" + templateId + ",superIds:" + superIds);
            boolean sendStatus = false;
            if (superIds.size() > 0) {
                List<String> superList = new ArrayList<>(superIds);
                String batchNumber = sendSmsToQueue(custId, customerGroupId, superList, templateId, String.valueOf(loginUser.getId()), marketTaskId);
                logger.info("发送短信customerGroupId:" + customerGroupId + ",templateId:" + templateId + ",结果:" + batchNumber);
                if (StringUtil.isNotEmpty(batchNumber)) {
                    sendStatus = true;
                }
                if (sendStatus) {
                    return 1001;
                } else {
                    return 2001;
                }
            }
        }
        return 2001;
    }

    public static void main(String[] args) {
        YxtSmsParam smsParam = new YxtSmsParam();
        smsParam.setAction(SaleApiUtil.TEMPLATE_SMS_ACTION);
        smsParam.setAppid(SaleApiUtil.SMS_APP_ID);
        smsParam.setMobile("18811526913");
        smsParam.setTemplateId("1909");
        smsParam.setDatas(Arrays.asList("0000".split(",")));
        String result = SaleApiUtil.sendSms(JSON.toJSONString(smsParam), SaleApiUtil.ENV);
        LogUtil.info("模板ID:" + smsParam.getTemplateId() + "----手机号：" + smsParam.getMobile());
        //短信
        smsParam.setAppid("c217790f22634c288dd6a917dc809722");
        LogUtil.info("短信发送结果:" + result);
    }
}
