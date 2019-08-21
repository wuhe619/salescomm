package com.bdaim.resource.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.account.dao.TransactionDao;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.batch.dao.BatchDao;
import com.bdaim.batch.dao.BatchDetailDao;
import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.bill.dto.CallBackInfoParam;
import com.bdaim.callcenter.dto.RecordVoiceQueryParam;
import com.bdaim.callcenter.dto.SeatInfoDto;
import com.bdaim.callcenter.dto.SeatsInfo;
import com.bdaim.callcenter.service.CallCenterService;
import com.bdaim.callcenter.service.impl.SeatsServiceImpl;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.*;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dao.CustomerUserPropertyDao;
import com.bdaim.customer.entity.CustomerPropertyDO;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.price.dto.ResourcesPriceDto;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.dto.CallBackParam;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.smscenter.dto.SendSmsDTO;
import com.bdaim.smscenter.dto.SmsqueryParam;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.supplier.dto.SupplierEnum;
import com.bdaim.supplier.dto.SupplierListParam;
import com.bdaim.supplier.entity.SupplierPropertyEntity;
import com.bdaim.template.dto.TemplateParam;
import com.bdaim.template.entity.MarketTemplate;
import com.github.crab2died.ExcelUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author yanls@bdaim.com
 * @Description: 营销资源service服务实现类
 * @date 2018/9/10 9:46
 */
@Service("marketResourceService")
@Transactional
public class MarketResourceServiceImpl implements MarketResourceService {


    private final static Logger LOG = LoggerFactory.getLogger(MarketResourceServiceImpl.class);

    private final static String SMS_SEND_REMARK_SPLIT = "{}";
    //发送类型
    private final static String TYPE_CODE = "2";
    /**
     * 联通包月分钟数配置key
     */
    private final static String SEAT_MONTH_PACKAGE_MINUTE_KEY = "cuc_minute";

    /**
     * 联通坐席每分钟通话费用key
     */
    private final static String SEAT_ONE_MINUTE_PRICE_KEY = "cuc_call_price";
    private final static String SUPPLIER_ID_UNICOM = "2";

    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private CallCenterService callCenterService;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private SeatsServiceImpl seatsService;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private MarketResourceDao marketResourceDao;
    //    @Resource
//    private SendSmsService sendSmsServiceImpl;
    @Resource
    private BatchDetailDao batchDetailDao;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private BatchDao batchDao;
    @Resource
    private SourceDao sourceDao;
    @Resource
    private TransactionDao transactionDao;
    @Resource
    private CustomerService customerService;
    @Resource
    private CustomerUserPropertyDao customerUserPropertyDao;
    @Resource
    private SupplierDao supplierDao;


    @Override
    public Page querySmsHistory(PageParam page, SmsqueryParam smsqueryParm) {
        StringBuffer sb = new StringBuffer();
        sb.append(" select cust_id,touch_id,remark,superid,enterprise_id,batch_id,create_time,`status`,sms_content \n" +
                "from t_touch_sms_log sms where 1=1  ");
        if (StringUtil.isNotEmpty(smsqueryParm.getCompId())) {
            sb.append(" and sms.cust_id='" + smsqueryParm.getCompId() + "'");
        }
        if (StringUtil.isNotEmpty(smsqueryParm.getRealName())) {
            sb.append(" and sms.remark like '%" + smsqueryParm.getRealName() + "%'");
        }
        if (StringUtil.isNotEmpty(smsqueryParm.getSuperId())) {
            sb.append(" and sms.superid='" + smsqueryParm.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(smsqueryParm.getEnterpriseId())) {
            sb.append(" and sms.enterprise_id='" + smsqueryParm.getEnterpriseId() + "'");
        }
        if (StringUtil.isNotEmpty(smsqueryParm.getBatchName())) {
            sb.append(" and sms.remark like '%" + smsqueryParm.getBatchName() + "%'");
        }
        if (StringUtil.isNotEmpty(smsqueryParm.getSendStartTime())) {
            sb.append(" and sms.create_time >='" + smsqueryParm.getSendStartTime() + "'");
        }
        if (StringUtil.isNotEmpty(smsqueryParm.getSendEndTime())) {
            sb.append(" and sms.create_time <='" + smsqueryParm.getSendEndTime() + "'");
        }
        if (smsqueryParm.getStatus() != null) {
            sb.append(" AND  sms.status=" + smsqueryParm.getStatus());
        }
        if (StringUtil.isNotEmpty(smsqueryParm.getTemplateName())) {
            sb.append(" and sms.remark like '%" + smsqueryParm.getTemplateName() + "%'");
        }
        if (StringUtil.isNotEmpty(smsqueryParm.getEnterpriseName())) {
            sb.append(" and sms.remark like '%" + smsqueryParm.getEnterpriseName() + "%'");
        }
        sb.append(" ORDER BY sms.create_time DESC ");
        return new Pagination().getPageData(sb.toString(), null, page, jdbcTemplate);
    }

    /**
     * 查询失联人员信息资料时查询通话记录
     */
    @Override
    public String queryCallHistory(String batchId, String superId, String custId) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<>();
        StringBuffer querySql = new StringBuffer("SELECT touch_id touchId,user_id userId,remark,create_time createTime FROM t_touch_voice_log WHERE superid=? AND batch_id=?");
        if (StringUtil.isNotEmpty(custId)) {
            querySql.append(" AND cust_id=" + custId);
        }
        querySql.append(" ORDER BY create_time DESC");
        List<Map<String, Object>> callHistory = marketResourceDao.sqlQuery(querySql.toString(), superId, batchId);
        if (callHistory.size() > 0) {
            for (int i = 0; i < callHistory.size(); i++) {
                String userId = String.valueOf(callHistory.get(i).get("userId"));
                if (!"".equals(userId) && userId != null) {
                    //通过userId查询realName
                    String realName = customerUserDao.getName(userId);
                    if (realName != null && !"".equals(realName)) {
                        callHistory.get(i).put("realName", realName);
                    }
                }
                String remark = String.valueOf(callHistory.get(i).get("remark"));
                if (remark != null && !"".equals(remark)) {
                    if (remark.contains("{}")) {
                        //截取字符串
                        List<String> remarks = Arrays.asList(remark.split("\\{}"));
                        remark = remarks.get(0);
                    } else {
                        remark = String.valueOf(remark);
                    }
                    callHistory.get(i).put("remark", remark);
                }
            }
            map.put("code", 1);
            map.put("message", "查询信息成功");
            map.put("callHistory", callHistory);
            json.put("data", map);
        }
        return json.toJSONString();
    }

    @Override
    public Page queryRecordVoicelog(PageParam page, String cust_id, Long userid, String user_type, String superId, String realName, String createTimeStart, String createTimeEnd, String enterpriseId, String batchId, int touchStatus, String enterpriseName) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        StringBuffer sb = new StringBuffer();
        //user_type 1.管理员   2.普通用户   superId为用户id(联通返回的)
        sb.append(
                "  select voicLog.touch_id,voicLog.cust_id,voicLog.batch_id, voicLog.enterprise_id, voicLog.superid, voicLog.create_time create_time," +
                        "voicLog.status,CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,backInfo.Callerduration,backInfo.Callercaller mainNumber, " +
                        "substring_index( backInfo.recordurl ,'/' , -1 ) as recordurl ")
                .append("  from t_touch_voice_log voicLog")
                .append("  LEFT JOIN t_callback_info backInfo ")
                .append("  ON voicLog.callSid = backInfo.callSid")
                .append("  where 1=1").append(" and channel IS NOT NULL ");

        if (!"".equals(realName) && null != realName) {
            sb.append(" AND   voicLog.remark LIKE '%" + realName + "%'");
        }

        if (!"".equals(enterpriseName) && null != enterpriseName) {
            sb.append(" AND   voicLog.remark LIKE '%" + enterpriseName + "%'");
        }

        if (!"".equals(enterpriseId) && null != enterpriseId) {
            sb.append(" AND  voicLog.enterprise_id= " + enterpriseId);
        }

        if (!"".equals(batchId) && null != batchId) {
            sb.append(" AND   voicLog.batch_id=" + batchId);
        }

        if (!"".equals(superId) && null != superId) {
            sb.append(" AND   voicLog.superid=" + superId);
        }

        if ("1".equals(user_type) && StringUtil.isNotEmpty(cust_id)) {
            sb.append(" AND   voicLog.cust_id=" + cust_id);
        }
        if ("2".equals(user_type) && userid != null) {
            sb.append(" AND   voicLog.user_id=" + userid);
        }
        if (touchStatus == 1001 || touchStatus == 1002) {
            sb.append(" AND   voicLog.status=" + touchStatus);
        }

        if (null != createTimeStart && !"".equals(createTimeStart) && null != createTimeEnd
                && !"".equals(createTimeEnd)) {
            sb.append(" AND voicLog.create_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
        } else {
            if (null != createTimeStart && !"".equals(createTimeStart)) {
                sb.append(" AND voicLog.create_time > '" + createTimeStart + "'");
            }
            if (null != createTimeEnd && !"".equals(createTimeEnd)) {
                sb.append(" AND voicLog.create_time < '" + createTimeEnd + "'");
            }
        }

        sb.append(" order by voicLog.create_time DESC");
        LOG.info("通过记录sql:\t" + sb.toString());
        return new Pagination().getPageData(sb.toString(), null, page, jdbcTemplate);

    }

    @Override
    public int insertLog(MarketResourceLogDTO dto) {
        String type_code = dto.getType_code();
        StringBuffer sql = new StringBuffer();
        if ("1".equals(dto.getType_code())) {
            if (StringUtil.isEmpty(dto.getRemark())) {
                dto.setRemark("");
            }
            sql.append(
                    "insert  into t_touch_voice_log (touch_id,cust_id,user_id,type_code,resname,remark,create_time,status,superId,callSid, call_owner, batch_id, activity_id, channel, enterprise_id,resource_id) values ( ");
            sql.append("'" + dto.getTouch_id() + "',");
            sql.append("'" + dto.getCust_id() + "',");
            sql.append("'" + dto.getUser_id() + "',");
            sql.append("'" + dto.getType_code() + "',");
            sql.append("'" + dto.getResname() + "',");
            sql.append("'" + dto.getRemark() + "',");
            sql.append("now(),");
            sql.append("'" + dto.getStatus() + "',");
            sql.append("'" + dto.getSuperId() + "',");
            sql.append("'" + dto.getCallSid() + "',");
            sql.append("'" + dto.getCallOwner() + "',");
            sql.append("'" + dto.getBatchId() + "',");
            sql.append("'" + dto.getActivityId() + "',");
            sql.append("'" + dto.getChannel() + "',");
            sql.append("'" + dto.getEnterpriseId() + "',");
            sql.append("'" + dto.getResourceId() + "')");
        } else if ("2".equals(type_code)) {
            sql.append(
                    "insert  into t_touch_sms_log (touch_id,cust_id,user_id,remark,create_time,status,sms_content,superId, batch_id, activity_id, channel, enterprise_id,amount,prod_amount,resource_id,send_data ) values ( ");
            sql.append("'" + dto.getTouch_id() + "',");
            sql.append("'" + dto.getCust_id() + "',");
            sql.append("'" + dto.getUser_id() + "',");
            sql.append("'" + dto.getRemark() + "',");
            sql.append("now(),");
            sql.append("'" + dto.getStatus() + "',");
            sql.append("'" + dto.getSms_content() + "',");
            sql.append("'" + dto.getSuperId() + "',");
            sql.append("'" + dto.getBatchId() + "',");
            sql.append("'" + dto.getActivityId() + "',");
            sql.append("'" + dto.getChannel() + "',");
            sql.append("'" + dto.getEnterpriseId() + "',");
            sql.append("'" + dto.getAmount() + "',");
            sql.append("'" + dto.getProdAmount() + "',");
            sql.append("'" + dto.getResourceId() + "',");
            sql.append("'" + dto.getCallBackData() + "')");
            return jdbcTemplate.update(sql.toString());
        } else if ("3".equals(type_code)) {
            sql.append(
                    "insert  into t_touch_email_log (cust_id,user_id,remark,create_time,status,email_content,superId,templateId,batch_number) values ( ");
            sql.append("'" + dto.getCust_id() + "',");
            sql.append("'" + dto.getUser_id() + "',");
            sql.append("'" + dto.getRemark() + "',");
            sql.append("now(),");
            sql.append("'" + dto.getStatus() + "',");
            sql.append("'" + dto.getEmail_content() + "',");
            sql.append("'" + dto.getSuperId() + "',");
            sql.append("'" + dto.getTemplateId() + "',");
            sql.append("'" + dto.getBatchNumber() + "')");
        }
        return jdbcTemplate.update(sql.toString());
    }

    @Override
    public Map<String, Object> seatAgentReset(String customerId, String userId, int type) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> callCenterConfigData = callCenterService.getCallCenterConfigData(customerId, userId);
        if (callCenterConfigData != null) {
            Map<String, Object> result = callCenterService.unicomAgentReset(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
            if (result != null && result.size() > 0) {
                data.put("code", Integer.parseInt(String.valueOf(result.get("code"))));
                data.put("msg", result.get("msg"));
                data.put("status", result.get("status"));
                return data;
            }
        }
        data.put("code", 0);
        data.put("msg", "失败");
        return data;
    }

    @Override
    public Map<String, Object> seatGetCurrentStatus(String customerId, String userId, int type) {
        Map<String, Object> data = new HashMap<>();
        Map<String, Object> callCenterConfigData = callCenterService.getCallCenterConfigData(customerId, userId);
        if (callCenterConfigData != null) {
            Map<String, Object> result = callCenterService.unicomGetSeatStatus(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
            if (result != null && result.size() > 0) {
                data.put("code", Integer.parseInt(String.valueOf(result.get("code"))));
                data.put("msg", result.get("msg"));
                data.put("status", result.get("status"));
                return data;
            }
        }
        data.put("code", 0);
        data.put("msg", "失败");
        return data;
    }

    @Override
    public Map<String, Object> seatMakeCallEx(String custId, String userId, String idCard, String batchId) {
        LOG.info("获取联通坐席参数:custId" + custId + "userId" + userId + "idCard" + idCard + "batchId" + batchId);
        Map<String, Object> data = new HashMap<>();
        int code = 0;
        String activityId = null, enterpriseId = null, apparentNumber = null, msg = "失败";
        Map<String, Object> callCenterConfigData = null;
        // try {
        // 处理联通外呼配置信息,不满足返回坐席信息未配置
        callCenterConfigData = callCenterService.getCallCenterConfigData(custId, userId);
        boolean configSuccess = false;
        if (callCenterConfigData != null) {
            int size = 0;
            for (Map.Entry<String, Object> config : callCenterConfigData.entrySet()) {
                if (config.getValue() != null && StringUtil.isNotEmpty(String.valueOf(config.getValue()))) {
                    size++;
                }
            }
            if (size == callCenterConfigData.size()) {
                configSuccess = true;
            }
        }

        if (configSuccess) {
            List<Map<String, Object>> batchDetail = marketResourceDao.sqlQuery("SELECT * FROM nl_batch_detail WHERE batch_id = ? AND id = ?", batchId, idCard);
            //查询外显号码
            List<Map<String, Object>> apparentNumberList = marketResourceDao.sqlQuery("SELECT * FROM nl_batch WHERE id = ?", batchId);
            if (batchDetail.size() > 0) {
                activityId = String.valueOf(batchDetail.get(0).get("activity_id"));
                enterpriseId = String.valueOf(batchDetail.get(0).get("enterprise_id"));
                if (apparentNumberList.size() > 0) {
                    apparentNumber = String.valueOf(apparentNumberList.get(0).get("apparent_number"));
                }
                if (!"".equals(apparentNumber) && StringUtil.isNotEmpty(apparentNumber) && !"null".equals(apparentNumber)) {
                    List<String> apparentsList = Arrays.asList(apparentNumber.split(","));
                    if (apparentsList.size() > 0) {
                        Random random = new Random();
                        int n = random.nextInt(apparentsList.size());
                        apparentNumber = apparentsList.get(n);
                    }
                } else {
                    if (callCenterConfigData.size() > 0 && callCenterConfigData.get("apparentNumber") != null) {
                        apparentNumber = String.valueOf(callCenterConfigData.get("apparentNumber"));
                        String[] apparentByCust = apparentNumber.split(",");
                        if (apparentByCust.length > 0) {
                            List<String> apparentList = Arrays.asList(apparentNumber.split(","));
                            Random random = new Random();
                            int n = random.nextInt(apparentList.size());
                            apparentNumber = apparentList.get(n);
                        }
                    }
                }
                boolean loginSuccess = false;
                boolean loggedOutStatus;
                // 判断坐席状态
                Map<String, Object> result = callCenterService.unicomGetSeatStatus(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
                LOG.info("获取联通坐席状态:" + result);
                // 不是登出状态,则执行登出
                loggedOutStatus = (result == null || (result != null && result.get("status") != null && !"LoggedOut".equals(String.valueOf(result.get("status")))));
                if (loggedOutStatus) {
                    callCenterService.unicomSeatLogout(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
                }
                //查询主叫号码
                String workNum = selectWorkPhoneNum(Long.parseLong(userId), "cuc_seat");
                if (StringUtil.isNotEmpty(workNum)) {
                    result = callCenterService.unicomSeatLogin(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")),
                            String.valueOf(callCenterConfigData.get("seatsPassword")), workNum, 1);
                    LOG.info("调用登录返回结果:" + result.toString());
                    if (result != null && result.get("code") != null) {
                        if ("000".equals(String.valueOf(result.get("code")))) {
                            loginSuccess = true;
                        } else if ("002".equals(String.valueOf(result.get("code")))) {
                            code = 0;
                            msg = "主叫号不存在!";
                        }
                    } else {
                        code = 0;
                        msg = "坐席登录失败";
                    }
                } else {
                    code = 0;
                    msg = "坐席未配置主叫号码";
                }
                if (loginSuccess) {
                    result = callCenterService.unicomSeatMakeCallEx(String.valueOf(callCenterConfigData.get("callCenterId")),
                            String.valueOf(callCenterConfigData.get("account")),
                            String.valueOf(batchDetail.get(0).get("activity_id")), String.valueOf(batchDetail.get(0).get("provide_id")),
                            String.valueOf(batchDetail.get(0).get("id")), apparentNumber);
                    LOG.info("调用外呼返回结果:" + result.toString());
                    if (result != null && "000".equals(String.valueOf(result.get("code")))) {
                        result.put("activity_id", activityId);
                        result.put("enterprise_id", enterpriseId);
                        result.put("code", 1);
                        return result;
                    } else {
                        LOG.error("调用外呼失败,结果:" + result.toString());
                        code = 0;
                        msg = "呼叫失败";
                    }
                }
            } else {
                code = 0;
                msg = "读取失联人信息失败";
            }
        } else {
            code = 0;
            msg = "坐席配置信息未配置!";
        }
        //  } catch (Exception e) {
        //  LOG.error("联通坐席外呼失败,", e);
        code = 0;
        msg = "呼叫失败";

        // }
        //finally {
        // 坐席退出
        if (callCenterConfigData != null) {
            LOG.info("呼叫完成执行坐席退出," + callCenterConfigData.toString());
            callCenterService.unicomSeatLogout(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
        }
        data.put("code", code);
        data.put("msg", msg);
        data.put("activity_id", activityId);
        data.put("enterprise_id", enterpriseId);
        // }
        return data;
    }

    /**
     * 2.2.0坐席外呼V1版本
     */
    public Map<String, Object> seatMakeCallExV1(String custId, String userId, String idCard, String batchId, String resourceId) {
        LOG.info("获取联通坐席参数:custId" + custId + "userId" + userId + "idCard" + idCard + "batchId" + batchId);
        Map<String, Object> data = new HashMap<>();
        int code = 0;
        String activityId = null, enterpriseId = null, apparentNumber = null, msg = "失败";
        Map<String, Object> callCenterConfigData = null;
        // try {
        LOG.info("联通外呼资源id是:" + resourceId);
        callCenterConfigData = callCenterService.getCallCenterConfigDataV1(custId, userId, resourceId);
        boolean configSuccess = false;
        if (callCenterConfigData != null) {
            int size = 0;
            for (Map.Entry<String, Object> config : callCenterConfigData.entrySet()) {
                if (config.getValue() != null && StringUtil.isNotEmpty(String.valueOf(config.getValue()))) {
                    size++;
                }
            }
            if (size == callCenterConfigData.size()) {
                configSuccess = true;
            }
        }

        if (configSuccess) {
            List<Map<String, Object>> batchDetail = marketResourceDao.sqlQuery("SELECT * FROM nl_batch_detail WHERE batch_id = ? AND id = ?", batchId, idCard);
            //查询外显号码
            List<Map<String, Object>> apparentNumberList = marketResourceDao.sqlQuery("SELECT * FROM nl_batch WHERE id = ?", batchId);
            if (batchDetail.size() > 0) {
                activityId = String.valueOf(batchDetail.get(0).get("activity_id"));
                enterpriseId = String.valueOf(batchDetail.get(0).get("enterprise_id"));
                if (apparentNumberList.size() > 0) {
                    apparentNumber = String.valueOf(apparentNumberList.get(0).get("apparent_number"));
                    LOG.info("批次下配置的外线号码是" + apparentNumber);
                }
                if (!"".equals(apparentNumber) && StringUtil.isNotEmpty(apparentNumber) && !"null".equals(apparentNumber)) {
                    List<String> apparentsList = Arrays.asList(apparentNumber.split(","));
                    if (apparentsList.size() > 0) {
                        Random random = new Random();
                        int n = random.nextInt(apparentsList.size());
                        apparentNumber = apparentsList.get(n);
                        LOG.info("联通外呼使用的批次下的外显号码是" + apparentNumber);
                    }
                } else {
                    if (callCenterConfigData.size() > 0 && callCenterConfigData.get("apparentNumber") != null) {
                        apparentNumber = String.valueOf(callCenterConfigData.get("apparentNumber"));
                        String[] apparentByCust = apparentNumber.split(",");
                        if (apparentByCust.length > 0) {
                            List<String> apparentList = Arrays.asList(apparentNumber.split(","));
                            Random random = new Random();
                            int n = random.nextInt(apparentList.size());
                            apparentNumber = apparentList.get(n);
                            LOG.info("联通外呼使用的外显号码是" + apparentNumber);
                        }
                    }
                }
                boolean loginSuccess = false;
                boolean loggedOutStatus;
                // 判断坐席状态
                Map<String, Object> result = callCenterService.unicomGetSeatStatus(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
                LOG.info("获取联通坐席状态:" + result);
                // 不是登出状态,则执行登出
                loggedOutStatus = (result == null || (result != null && result.get("status") != null && !"LoggedOut".equals(String.valueOf(result.get("status")))));
                if (loggedOutStatus) {
                    callCenterService.unicomSeatLogout(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
                }
                //查询主叫号码
                String workNum = String.valueOf(callCenterConfigData.get("mainNumber"));
                LOG.info("坐席外呼使用的主叫号码是:" + workNum);
                if (StringUtil.isNotEmpty(workNum)) {
                    result = callCenterService.unicomSeatLogin(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")),
                            String.valueOf(callCenterConfigData.get("seatsPassword")), workNum, 1);
                    LOG.info("调用登录返回结果:" + result.toString());
                    if (result != null && result.get("code") != null) {
                        if ("000".equals(String.valueOf(result.get("code")))) {
                            loginSuccess = true;
                        } else if ("002".equals(String.valueOf(result.get("code")))) {
                            code = 0;
                            msg = "主叫号不存在!";
                        }
                    } else {
                        code = 0;
                        msg = "坐席登录失败";
                    }
                } else {
                    code = 0;
                    msg = "坐席未配置主叫号码";
                }
                if (loginSuccess) {
                    result = callCenterService.unicomSeatMakeCallEx(String.valueOf(callCenterConfigData.get("callCenterId")),
                            String.valueOf(callCenterConfigData.get("account")),
                            String.valueOf(batchDetail.get(0).get("activity_id")), String.valueOf(batchDetail.get(0).get("provide_id")),
                            String.valueOf(batchDetail.get(0).get("id")), apparentNumber);
                    LOG.info("调用外呼返回结果:" + result.toString());
                    if (result != null && "000".equals(String.valueOf(result.get("code")))) {
                        result.put("activity_id", activityId);
                        result.put("enterprise_id", enterpriseId);
                        result.put("code", 1);
                        return result;
                    } else {
                        LOG.error("调用外呼失败,结果:" + result.toString());
                        code = 0;
                        msg = "呼叫失败";
                    }
                }
            } else {
                code = 0;
                msg = "读取失联人信息失败";
            }
        } else {
            code = 0;
            msg = "坐席配置信息未配置!";
        }
        //  } catch (Exception e) {
        //  LOG.error("联通坐席外呼失败,", e);
        // }
        //finally {
        // 坐席退出
        if (callCenterConfigData != null) {
            LOG.info("呼叫完成执行坐席退出," + callCenterConfigData.toString());
            callCenterService.unicomSeatLogout(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
        }
        data.put("code", code);
        data.put("msg", msg);
        data.put("activity_id", activityId);
        data.put("enterprise_id", enterpriseId);
        // }
        return data;
    }

    /**
     * 发送短信接口
     *
     * @return
     */
    public Map<String, Object> sendBatchSms(String variables, String custId, String userId, int templateId, String batchId, String customerIds, int typeCode, int channel) throws Exception {
        int sendSuccessCount = 0;
        Map<String, Object> map = new HashMap<>();
        String resourceId = null;
        MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
        //查询resourceId
        MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.SMS.getType());
        if (marketResourceEntity != null) {
            resourceId = String.valueOf(marketResourceEntity.getResourceId());
            marketResourceLogDTO.setResourceId(marketResourceEntity.getResourceId());
            LOG.info("发送短信资源id是:" + resourceId);
        }
        // 判断企业是否配置短信资源
        ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, custId);
        if (resourcesPriceDto == null || StringUtil.isEmpty(resourcesPriceDto.getCallCenterId())) {
            map.put("msg", "短信资源未配置,无法发送短信");
            map.put("code", 0);
            return map;
        }
        if (resourcesPriceDto == null || StringUtil.isEmpty(resourcesPriceDto.getSmsPrice())) {
            map.put("msg", "未设置定价,无法发送短信");
            map.put("code", 0);
            return map;
        }
        // 判断是余额是否充足
        boolean judge = false;
        judge = marketResourceService.judRemainAmount(custId);
        if (!judge) {
            map.put("msg", "余额不足");
            map.put("code", 0);
            return map;
        }
        MarketTemplate marketTemplate = sourceDao.getMarketTemplate(templateId, typeCode, custId);
        if (marketTemplate != null) {
            BatchDetail batchDetail;
            //发送短信参数类
            SendSmsDTO sendSmsDTO = new SendSmsDTO();
            String sendResult, userName, batchName, templateName, custName;
            String custSmsPrice = "0", supSmsPrice = "0";
            BigDecimal custSmsAmount = null, sourceSmsAmount = null;
            boolean accountDeductionStatus, sourceSmsDeductionStatus;
            StringBuffer remark;
            //构造占位符
            List<String> variableList = null;
            if (variables != null && variables.contains(",")) {
                variableList = Arrays.asList(variables.split(","));
            }
            if (variableList != null) {
                sendSmsDTO.setVariableOne(variableList.size() > 0 ? variableList.get(0) : "");
                sendSmsDTO.setVariableTwo(variableList.size() > 1 ? variableList.get(1) : "");
                sendSmsDTO.setVariableThree(variableList.size() > 2 ? variableList.get(2) : "");
                sendSmsDTO.setVariableFour(variableList.size() > 3 ? variableList.get(3) : "");
                sendSmsDTO.setVariableFive(variableList.size() > 4 ? variableList.get(4) : "");
            }
            String[] customerIdList = customerIds.split(",");
            for (String id : customerIdList) {
                batchDetail = batchDetailDao.getBatchDetail(id, batchId);
                if (batchDetail != null) {
                    sendSmsDTO.setMessageWord(marketTemplate.getTemplateCode());
                    sendSmsDTO.setActivityId(batchDetail.getActivityId());
                    sendSmsDTO.setEntId(resourcesPriceDto.getCallCenterId());

                    marketResourceLogDTO.setTouch_id(Long.toString(IDHelper.getTransactionId()));
                    marketResourceLogDTO.setType_code("2");
                    marketResourceLogDTO.setResname("sms");
                    marketResourceLogDTO.setUser_id(Long.parseLong(userId));
                    marketResourceLogDTO.setCust_id(custId);
                    marketResourceLogDTO.setSuperId(id);
                    marketResourceLogDTO.setCallSid("");
                    marketResourceLogDTO.setBatchId(batchId);
                    marketResourceLogDTO.setChannel(channel);
//                    sendResult = sendSmsServiceImpl.sendSmsService(sendSmsDTO, id, batchId);
                    //sendResult = "{\"code\":\"000\",\"msg\":\"发送成功!\"}";
//                    LOG.info("联通短信发送结果:" + sendResult);
                    marketResourceLogDTO.setActivityId(batchDetail.getActivityId());
                    marketResourceLogDTO.setEnterpriseId(batchDetail.getEnterpriseId());
                    marketResourceLogDTO.setSms_content(marketTemplate.getMouldContent());
//                    marketResourceLogDTO.setCallBackData(sendResult);
                    // 拼装备注字段 操作人名;批次名称;模板名称;企业名称
                    userName = customerUserDao.getName(userId);
                    custName = customerDao.getEnterpriseName(custId);
                    templateName = marketTemplate.getTitle();
                    batchName = batchDao.getBatchName(batchId);

                    remark = new StringBuffer();
                    remark.append(userName);
                    remark.append(SMS_SEND_REMARK_SPLIT);
                    remark.append(batchName);
                    remark.append(SMS_SEND_REMARK_SPLIT);
                    remark.append(templateName);
                    remark.append(SMS_SEND_REMARK_SPLIT);
                    remark.append(custName);
                    marketResourceLogDTO.setRemark(remark.toString());

                    // 发送成功
//                    if (StringUtil.isNotEmpty(sendResult) && "000".equals(JSON.parseObject(sendResult, Map.class).get("code"))) {
//                        sendSuccessCount++;
//                        marketResourceLogDTO.setStatus(1001);
//                        //发送成功后进行企业和供应商进行扣费
//                        custSmsPrice = resourcesPriceDto.getSmsPrice();
//                        if (StringUtil.isNotEmpty(custSmsPrice)) {
//                            custSmsAmount = new BigDecimal(custSmsPrice).multiply(new BigDecimal(100));
//                        }
//                        LOG.info("短信扣费客户:" + custId + ",开始扣费,金额:" + custSmsAmount);
//                        accountDeductionStatus = customerDao.accountDeductions(custId, custSmsAmount);
//                        LOG.info("短信扣费客户:" + custId + ",扣费状态:" + accountDeductionStatus);
//
//
//                        //获取供应商短信价格
//                        if (StringUtil.isNotEmpty(resourceId)) {
//                            ResourcesPriceDto supResourceMessageById = supplierDao.getSupResourceMessageById(Integer.parseInt(resourceId), null);
//                            supSmsPrice = supResourceMessageById.getSmsPrice();
//                        }
//                        // 供应商余额扣款
//                        LOG.info("短信扣费供应商:" + custId + "开始短信扣费,金额(分):" + supSmsPrice);
//                        //供应商扣费需要转换为分进行扣减
//                        if (StringUtil.isNotEmpty(supSmsPrice)) {
//                            sourceSmsAmount = new BigDecimal(supSmsPrice).multiply(new BigDecimal(100));
//                            sourceSmsDeductionStatus = sourceDao.supplierAccountDuctions(SupplierEnum.CUC.getSupplierId(), sourceSmsAmount);
//                            LOG.info("短信扣费供应商:" + custId + "短信扣费状态:" + sourceSmsDeductionStatus);
//                            marketResourceLogDTO.setProdAmount(sourceSmsAmount.intValue());
//                        } else {
//                            marketResourceLogDTO.setProdAmount(0);
//                        }
//                        //短信log表，添加企业和供应商扣减金额
//                        marketResourceLogDTO.setAmount(custSmsAmount.intValue());
//                    } else {
//                        marketResourceLogDTO.setAmount(0);
//                        marketResourceLogDTO.setProdAmount(0);
//                        marketResourceLogDTO.setStatus(1002);
//                    }
                    this.insertLog(marketResourceLogDTO);
                } else {
                    map.put("msg", "客户信息不存在，短信发送失败");
                    map.put("code", 0);
                    return map;
                }
            }
            if (sendSuccessCount > 0) {
                map.put("msg", "短信发送成功");
                map.put("code", 1);
                map.put("count", sendSuccessCount);
            } else {
                map.put("msg", "短信发送失败");
                map.put("code", 0);
            }
        }
        return map;
    }

    @Override
    public boolean judRemainAmount(String cust_id) {
        boolean judge = false;
        try {
            judge = true;
            CustomerPropertyDO customerProperty = customerDao.getProperty(cust_id, "remain_amount");
            if (customerProperty == null) {
                judge = false;
            } else {
                if (StringUtil.isEmpty(customerProperty.getPropertyValue())) {
                    judge = false;
                } else {
                    double remain_amount = Double.parseDouble(customerProperty.getPropertyValue());
                    // 标准价格
                    Integer priceStand = 500;
                    if (remain_amount < priceStand) {
                        // 余额不足
                        judge = false;
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("获取余额失败,", e);
        }
        return judge;
    }

    public synchronized void saveCallCenterVoiceLogTask() {
        saveCallCenterVoiceLog("LTBD2018090701", "111111");
    }

    @Override
    public void saveCallCenterVoiceLog(String entId, String endPwd) {
        final String sql = "SELECT t_touch_voice_log.callSid FROM t_touch_voice_log LEFT JOIN t_callback_info ON t_touch_voice_log.callSid = t_callback_info.callSid WHERE t_callback_info.callSid IS NULL AND t_touch_voice_log.channel= 0";
        List<Map<String, Object>> untreatedCallLogs = jdbcTemplate.queryForList(sql);
        try {
            if (untreatedCallLogs.size() > 0) {
                final String insertCallbackInfoSql = "insert into t_callback_info(callSid, appId, flag, Callercaller, Callerstarttime, Callerendtime, Callerduration, CallerringingBeginTime, CallerringingEndTime, " +
                        "Calledcaller, Calledstarttime, Calledendtime, Calledduration, CalledringingBeginTime, CalledringingEndTime, recordurl, userData, userId, superId) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
                String callSid, voiceUrl = "", appId = "unicom", duration, startTime, endTime, localAlertStartTime, localAlertEndTime, remoteAlertStartTime, remoteAlertEndTime;
                List<Map<String, Object>> touchList;
                Map<String, Object> callLogMap, voiceUrlData;
                int flag = 0;
                for (Map<String, Object> map : untreatedCallLogs) {
                    callSid = String.valueOf(map.get("callSid"));
                    if (!"null".equals(callSid)) {
                        touchList = jdbcTemplate.queryForList("SELECT * FROM t_touch_voice_log t1 JOIN nl_batch_detail t2 ON t1.batch_id = t2.batch_id AND t1.superid = t2.id_card WHERE t1.callSid = ?", callSid);
                        if (touchList.size() > 0) {
                            callLogMap = callCenterService.unicomGetCallData(callSid, "1", "callReply", entId, String.valueOf(touchList.get(0).get("activity_id")), endPwd);
                            // 获取通话记录成功
                            if (callLogMap != null && callLogMap.get("code") != null && "000".equals(String.valueOf(callLogMap.get("code")))) {
                                LOG.info("获取联通呼叫中心通话记录返回:" + callLogMap.toString());
                                voiceUrlData = callCenterService.unicomRecordByRequestId(callSid, entId);
                                LOG.info("获取联通呼叫中心录音文件:" + voiceUrlData.toString());
                                if (voiceUrlData != null && voiceUrlData.get("result") != null && "0".equals(String.valueOf(voiceUrlData.get("result")))) {
                                    voiceUrl = String.valueOf(voiceUrlData.get("url"));
                                    if (StringUtil.isNotEmpty(voiceUrl) && voiceUrl.contains("htt")) {
                                        //保存录音文件
                                        savePhoneRecordFile(voiceUrl, String.valueOf(touchList.get(0).get("user_id")));
                                    }
                                }
                                duration = String.valueOf(callLogMap.get("callDuration"));
                                //处理时间
                                startTime = String.valueOf(callLogMap.get("startTime"));
                                endTime = String.valueOf(callLogMap.get("endTime"));
                                localAlertStartTime = String.valueOf(callLogMap.get("localAlertStartTime"));
                                localAlertEndTime = String.valueOf(callLogMap.get("localAlertEndTime"));
                                remoteAlertStartTime = String.valueOf(callLogMap.get("remoteAlertStartTime"));
                                remoteAlertEndTime = String.valueOf(callLogMap.get("remoteAlertEndTime"));

                                if (StringUtil.isNotEmpty(startTime)) {
                                    startTime = startTime.replaceAll("-", "/");
                                }
                                if (StringUtil.isNotEmpty(endTime)) {
                                    endTime = endTime.replaceAll("-", "/");
                                }
                                if (StringUtil.isNotEmpty(localAlertStartTime)) {
                                    localAlertStartTime = localAlertStartTime.replaceAll("-", "/");
                                }
                                if (StringUtil.isNotEmpty(localAlertEndTime)) {
                                    localAlertEndTime = localAlertEndTime.replaceAll("-", "/");
                                }
                                if (StringUtil.isNotEmpty(remoteAlertStartTime)) {
                                    remoteAlertStartTime = remoteAlertStartTime.replaceAll("-", "/");
                                }
                                if (StringUtil.isNotEmpty(remoteAlertEndTime)) {
                                    remoteAlertEndTime = remoteAlertEndTime.replaceAll("-", "/");
                                }
                                flag += jdbcTemplate.update(insertCallbackInfoSql, new Object[]{callSid, appId, 1,
                                        String.valueOf(callLogMap.get("localUrl")),
                                        startTime, endTime,
                                        duration, localAlertStartTime, localAlertEndTime,
                                        String.valueOf(callLogMap.get("remoteUrl")),
                                        startTime, endTime,
                                        duration, remoteAlertStartTime, remoteAlertEndTime,
                                        voiceUrl, callSid, touchList.size() > 0 ? touchList.get(0).get("user_id") : "",
                                        touchList.size() > 0 ? touchList.get(0).get("superid") : ""});
                            }
                        }
                    }
                }
                if (flag > 0) {
                    LOG.info("联通外呼插入数据库记录(条):" + flag);
                }
            }
        } catch (Exception e) {
            LOG.error("联通外呼记录表异常:", e);
        }
    }

    private void savePhoneRecordFile(String recordUrl, String userId) {
        if (StringUtil.isNotEmpty(recordUrl) && !"NoTapes".equals(recordUrl)) {
            if (StringUtil.isNotEmpty(recordUrl) && StringUtil.isNotEmpty(userId)) {
                String fileName = recordUrl.substring(recordUrl.lastIndexOf("/"));
                String filePath = PropertiesUtil.getStringValue("audiolocation") + userId;
                File file = new File(filePath);
                if (!file.exists()) {
                    LOG.info("录音文件夹不存在,开始创建:" + filePath);
                    file.mkdir();
                }
                boolean flag = FileUtil.saveFileByUrl(recordUrl, filePath + File.separator + fileName);
                LOG.info("保存通话录音文件:" + flag);
            }
        }
    }

    @Override
    public String setWorkPhoneNum(String workNum, String userId) {
        JSONObject map = new JSONObject();
        String sql = "update t_user set work_num=?,modify_time= now(),work_num_status=0 where id=? ";
        int flag = jdbcTemplate.update(sql, new Object[]{workNum, userId});
        if (flag == 1) {
            map.put("code", "0");
            map.put("message", "成功");
        } else {
            map.put("code", "1");
            map.put("message", "失败");
        }
        return map.toJSONString();
    }

    @Override
    public String selectWorkPhoneNum(long userId, String seatName) {
        String workNumSet = null;
        try {
            SeatsInfo seatsInfo = seatsService.getCallProperty(String.valueOf(userId), seatName);
            if (seatsInfo != null) {
                workNumSet = String.valueOf(seatsInfo.getMainNumber());
            }
        } catch (Exception e) {
            LOG.error("获取主叫号码失败,", e);
            workNumSet = "000";
        }
        return workNumSet;
    }

    @Override
    public Page getSupplierList(PageParam page, SupplierListParam supplierListParam) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("SELECT supplier_id source_id ,name source_name,type,contact_person person,contact_phone phone ,create_time FROM t_supplier where 1=1");
        if (StringUtil.isNotEmpty(supplierListParam.getSupplierId())) {
            sqlBuilder.append(" AND supplier_id = " + supplierListParam.getSupplierId());
        }
        if (StringUtil.isNotEmpty(supplierListParam.getName())) {
            sqlBuilder.append(" AND  name = " + supplierListParam.getName());
        }
        if (StringUtil.isNotEmpty(supplierListParam.getPerson())) {
            sqlBuilder.append(" AND contact_person = " + supplierListParam.getPerson());
        }
        if (StringUtil.isNotEmpty(supplierListParam.getPhone())) {
            sqlBuilder.append(" AND contact_phone = " + supplierListParam.getPhone());
        }
        return new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
    }

    @Override
    public void setPrice(SupplierListParam supplierListParam) throws Exception {
        String supplierId = supplierListParam.getSupplierId();
        if (StringUtil.isNotEmpty(supplierId)) {
            //修改的是供应商的成本价
            String smsPrice = supplierListParam.getSmsPrice();
            String callPrice = supplierListParam.getCallPrice();
            String seatPrice = supplierListParam.getSeatPrice();
            String fixPrice = supplierListParam.getFixPrice();
            String imeiFixPrice = supplierListParam.getImeiFixPrice();
            String macFixPrice = supplierListParam.getMacFixPrice();
            LOG.info("supplierId：" + supplierListParam.toString());
            if (ConstantsUtil.SUPPLIERID__CMC.equals(supplierId) || ConstantsUtil.SUPPLIERID__CUC.equals(supplierId) || ConstantsUtil.SUPPLIERID__CTC.equals(supplierId)) {
                //查看t_market_resource表是否已经存在type和resource_id  不存在增加一条数据
                //（1.充值，2.扣减，3.短信扣费  4.通话扣费，5.坐席 6 .修复，7 快递 10mac修复 11 imei修复  12 地址修复）
                if (StringUtil.isNotEmpty(smsPrice)) {
                    editResource(supplierId, ConstantsUtil.SMS_TYPE, ConstantsUtil.SMS_PRICE_KEY, smsPrice, "短信");
                }
                if (StringUtil.isNotEmpty(callPrice)) {
                    editResource(supplierId, ConstantsUtil.CALL_TYPE, ConstantsUtil.CALL_PRICE_KEY, callPrice, "外呼");
                }
                if (StringUtil.isNotEmpty(seatPrice)) {
                    editResource(supplierId, ConstantsUtil.SEATS_TYPE, ConstantsUtil.SEATS_PRICE_KEY, seatPrice, "坐席");
                }
                if (StringUtil.isNotEmpty(fixPrice)) {
                    editResource(supplierId, ConstantsUtil.IDCARD_FIX_TYPE, ConstantsUtil.IDCARD_FIX_PRICE_KEY, fixPrice, "身份证修复");
                }
                if (StringUtil.isNotEmpty(imeiFixPrice)) {
                    editResource(supplierId, ConstantsUtil.IMEI_FIX_TYPE, ConstantsUtil.IMEI_PRICE_KEY, imeiFixPrice, "imei修复");
                }
                if (StringUtil.isNotEmpty(macFixPrice)) {
                    editResource(supplierId, ConstantsUtil.MAC_FIX_TYPE, ConstantsUtil.MAC_PRICE_KEY, macFixPrice, "mac修复");
                }
            } else if (ConstantsUtil.SUPPLIERID__XZ.equals(supplierId)) {
                if (StringUtil.isNotEmpty(callPrice)) {
                    editResource(supplierId, ConstantsUtil.CALL_TYPE, ConstantsUtil.CALL_PRICE_KEY, callPrice, "外呼");
                }
                if (StringUtil.isNotEmpty(seatPrice)) {
                    editResource(supplierId, ConstantsUtil.SEATS_TYPE, ConstantsUtil.SEATS_PRICE_KEY, seatPrice, "坐席");
                }
            } else {
                //快递地址修复价格
                String jdFixPrice = supplierListParam.getJdFixPrice();
                if (StringUtil.isNotEmpty(jdFixPrice)) {
                    editResource(supplierId, ConstantsUtil.JD_FIX_TYPE, ConstantsUtil.JD_FIX_PRICE_KEY, jdFixPrice, "地址修复");
                }
                String expressPrice = supplierListParam.getExpressPrice();
                if (StringUtil.isNotEmpty(expressPrice)) {
                    editResource(supplierId, ConstantsUtil.EXPRESS_TYPE, ConstantsUtil.EXPRESS_PRICE_KEY, expressPrice, "快递");
                }
            }
        }

    }

    public void editResource(String supplierId, int type, String propertyKey, String propertyValue, String reaName) throws Exception {
        MarketResourceEntity sourceProperty = sourceDao.getSupplier(supplierId, type);
        Integer resourceId = null;
        if (sourceProperty == null) {
            //说明已经不存在，直接插入操作
            sourceProperty = new MarketResourceEntity();
            sourceProperty.setSupplierId(supplierId);
            sourceProperty.setTypeCode(type);
            sourceProperty.setResname(reaName);
            sourceProperty.setSalePrice(0);
            sourceProperty.setCostPrice(0);
            sourceProperty.setStatus(1);
            sourceProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            resourceId = (Integer) sourceDao.saveReturnPk(sourceProperty);
        } else {
            resourceId = sourceProperty.getResourceId();
        }
        //添加成本价t_market_resource_property  存在更新  不存在添加
        ResourcePropertyEntity resourceProperty = sourceDao.getSourceProperty(resourceId);
        if (resourceProperty != null) {
            resourceProperty.setPropertyValue(propertyValue);
            sourceDao.saveOrUpdate(resourceProperty);
        } else {
            ResourcePropertyEntity resourcePropertyEntity = new ResourcePropertyEntity();
            resourcePropertyEntity.setResourceId(resourceId);
            resourcePropertyEntity.setPropertyName(propertyKey);
            resourcePropertyEntity.setPropertyValue(propertyValue);
            resourcePropertyEntity.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            sourceDao.saveOrUpdate(resourcePropertyEntity);
        }
    }


    public int savaSourceProperty(String sourceid, Double seatprice, String seatminutes, Double callprice, Double fixpriceInsurance, Double fixpriceBank, Double fixpriceOnline, Double fixpriceCourt, Double smsprice, String person, String phone, int flag, StringBuffer sb) {

        String updateStatement = "UPDATE t_source_property set property_value=? WHERE source_id=? and property_key=?";
        String insertStatement = "INSERT into t_source_property(property_value,source_id,property_key) VALUES(?,?,?);";


        if (seatprice != null) {
            flag = jdbcTemplate.update(updateStatement, new Object[]{seatprice, sourceid, "seatprice"});
            if (flag <= 0) {
                //不存在需要被更新的数据，那就插入
                flag = jdbcTemplate.update(insertStatement, new Object[]{seatprice, sourceid, "seatprice"});
            }
        }

        if (seatminutes != null) {
            flag = jdbcTemplate.update(updateStatement, new Object[]{seatminutes, sourceid, "seatminutes"});
            if (flag <= 0) {
                //不存在需要被更新的数据，那就插入
                flag = jdbcTemplate.update(insertStatement, new Object[]{seatminutes, sourceid, "seatminutes"});
            }
        }
        if (callprice != null) {
            flag = jdbcTemplate.update(updateStatement, new Object[]{callprice, sourceid, "callprice"});
            if (flag <= 0) {
                flag = jdbcTemplate.update(insertStatement, new Object[]{callprice, sourceid, "callprice"});
            }
        }
        if (fixpriceInsurance != null) {
            flag = jdbcTemplate.update(updateStatement, new Object[]{fixpriceInsurance, sourceid, "fixpriceInsurance"});
            if (flag <= 0) {
                flag = jdbcTemplate.update(insertStatement, new Object[]{fixpriceInsurance, sourceid, "fixpriceInsurance"});
            }
        }

        if (fixpriceBank != null) {
            flag = jdbcTemplate.update(updateStatement, new Object[]{fixpriceBank, sourceid, "fixpriceBank"});
            if (flag <= 0) {
                flag = jdbcTemplate.update(insertStatement, new Object[]{fixpriceBank, sourceid, "fixpriceBank"});
            }
        }

        if (fixpriceOnline != null) {
            flag = jdbcTemplate.update(updateStatement, new Object[]{fixpriceOnline, sourceid, "fixpriceOnline"});
            if (flag <= 0) {
                flag = jdbcTemplate.update(insertStatement, new Object[]{fixpriceOnline, sourceid, "fixpriceOnline"});
            }
        }

        if (fixpriceCourt != null) {
            flag = jdbcTemplate.update(updateStatement, new Object[]{fixpriceCourt, sourceid, "fixpriceCourt"});
            if (flag <= 0) {
                flag = jdbcTemplate.update(insertStatement, new Object[]{fixpriceCourt, sourceid, "fixpriceCourt"});
            }
        }

        if (smsprice != null) {
            flag = jdbcTemplate.update(updateStatement, new Object[]{smsprice, sourceid, "smsprice"});
            if (flag <= 0) {
                flag = jdbcTemplate.update(insertStatement, new Object[]{smsprice, sourceid, "smsprice"});
            }
        }
        if (StringUtil.isNotEmpty(person)) {
            sb = new StringBuffer();
            sb.append("INSERT into t_source_property(property_key,property_value) VALUES(?,?,?);");
            flag = jdbcTemplate.update(sb.toString(), new Object[]{"person", person});
        }
        if (StringUtil.isNotEmpty(phone)) {
            sb = new StringBuffer();
            sb.append("INSERT into t_source_property(property_key,property_value) VALUES(?,?,?);");
            flag = jdbcTemplate.update(sb.toString(), new Object[]{"phone", phone});
        }
        return flag;
    }
    @Override
    public Map<String,Object> countMarketDataBackend(){
        //企业有效率 折线统计图
        String effectiveRateSql = "SELECT batch_name AS batchName,comp_name AS companyName,IFNULL(upload_num/success_num,0) AS effectiveRate FROM nl_batch ORDER BY " +
                "upload_time DESC LIMIT 10";
        List<Map<String, Object>> effectiveRate = jdbcTemplate.queryForList(effectiveRateSql);

        //企业签收率 折现统计图
        StringBuffer receiveRate = new StringBuffer("SELECT t1.id,t1.batch_name,t1.comp_name AS companyName,");
        receiveRate.append("ROUND(SUM( CASE t3.`status` WHEN '4' THEN 1 ELSE 0 END ) / SUM( CASE t3.`status` WHEN '1' THEN 0 ELSE 1 END ),2) AS receiveRate  ")
                .append(" FROM nl_batch t1").append(" LEFT JOIN nl_batch_detail t2 ON t1.id = t2.batch_id")
                .append(" LEFT JOIN t_touch_express_log t3 ON t2.touch_id = t3.touch_id ")
                .append("GROUP BY t1.id, t1.batch_name,t1.comp_name  ORDER BY t1.upload_time DESC LIMIT 10");
        List<Map<String, Object>> receiveRateList = jdbcTemplate.queryForList(receiveRate.toString());

        //客户有效数据趋势图
        StringBuffer effectiveNum = new StringBuffer("SELECT DATE_FORMAT(upload_time, '%Y-%m-%d') AS upload_time,");
        effectiveNum.append("SUM(CASE `status` WHEN '1' THEN 1 ELSE 0 END) AS effective_num  FROM nl_batch_detail ")
                .append("GROUP BY DATE_FORMAT(upload_time,'%Y-%m-%d') ").append("ORDER BY DATE_FORMAT(upload_time,'%Y-%m-%d') DESC")
                .append(" LIMIT 10");
        List<Map<String, Object>> effectiveNumMap = jdbcTemplate.queryForList(effectiveNum.toString());
        Map<String,Object> data = new HashMap<>(16);
        data.put("effectiveRate", effectiveRate);
        data.put("receiveRate", receiveRateList);
        data.put("effectiveNum", effectiveNumMap);
        return data;
    }

    @Override
    public Map<String, Object> countMarketData(String customerId) {

        Map<String, Object> data = new HashMap<>();
        //customerId 为空，则是后台首页各个企业的信息
        if (StringUtil.isEmpty(customerId)) {
            // 1 各个企业最近一周上传的客户量
            StringBuffer sqlSb = new StringBuffer();
            List<Map<String, Object>> uploadNumList;
            //todo 没数据  6--->16了
            sqlSb.append(" SELECT s.upNum,s.comp_id,s.upload_time,t.enterprise_name from \n" +
                    "(SELECT sum(upload_num)as upNum,comp_id,upload_time,id,upload_num  from nl_batch\n" +
                    "WHERE DATE_SUB(CURDATE(), INTERVAL 6 DAY) <= date(upload_time) GROUP BY date(upload_time))s \n" +
                    "LEFT JOIN t_customer t on s.comp_id=t.cust_id;");
            uploadNumList = jdbcTemplate.queryForList(sqlSb.toString(), new Object[]{});

            //2 各个企业最近上传一次上传、成功次数
            StringBuffer sqlge = new StringBuffer();
            List<Map<String, Object>> uploadgeList;
            sqlge.append(" SELECT cjc.upload_num,cjc.comp_id,cjc.batch_id,cjc.success_num,cjc.enterprise_name,cjc.upload_time from \n" +
                    "(SELECT n.upload_num,n.comp_id,n.id as batch_id,d.id as customerId,n.upload_time\n" +
                    ",u.enterprise_name,(SELECT COUNT(DISTINCT id_card) FROM nl_batch_detail WHERE batch_id = n.id AND `status` = 1 AND id IS NOT NULL) AS success_num\n" +
                    "from nl_batch n \n" +
                    "   LEFT JOIN t_customer u on n.comp_id = u.cust_id \n" +
                    "\tLEFT JOIN nl_batch_detail d on n.id = d.batch_id\n" +
                    " and d.status=0 \n" +
                    "group by n.comp_id,n.id\n" +
                    "ORDER BY n.upload_time desc ) cjc \n" +
                    "GROUP BY cjc.comp_id ORDER BY cjc.upload_time DESC LIMIT 10;");
            uploadgeList = jdbcTemplate.queryForList(sqlge.toString(), new Object[]{});

            // 3 各企业最近300次呼叫接通率
            NumberFormat numberFormat = NumberFormat.getInstance();
            numberFormat.setMaximumFractionDigits(2);
            String jietongPercent = "";//接通率
            StringBuffer sqlqi = new StringBuffer();
            List<Map<String, Object>> compIdList;
            List<Map<String, Object>> jietonglv = new ArrayList<>();
            sqlqi.append(" SELECT n.comp_id,t.enterprise_name from nl_batch n left JOIN t_customer t on n.comp_id=t.cust_id " +
                    "GROUP BY n.comp_id ORDER BY n.upload_time desc limit 10; ");
            compIdList = jdbcTemplate.queryForList(sqlqi.toString(), new Object[]{});
            DecimalFormat df1 = new DecimalFormat("0.00");
            //todo 返回的是列表，需要循环遍历各个企业
            if (compIdList != null && compIdList.size() > 0) {
                for (Map<String, Object> comIdMap : compIdList) {
                    String compId = null;
                    if (comIdMap.get("comp_id") != null) {
                        compId = comIdMap.get("comp_id").toString();
                    }
                    String compName = null;
                    if (comIdMap.get("enterprise_name") != null) {
                        compName = comIdMap.get("enterprise_name").toString();
                    }
                    //依次查出每个企业的最近通话300条记录中成功记录条数
                    StringBuffer sqlqisuc = new StringBuffer();
                    Map<String, Object> map = new HashMap<String, Object>();
                    List<Map<String, Object>> touchSuclist;
                    sqlqisuc.append("SELECT count(*) as numSuc from(SELECT t.callSid,t.status,t.cust_id,t.create_time from t_touch_voice_log t \n" +
                            " WHERE t.cust_id=? ORDER BY t.create_time desc limit 300) s WHERE s.status =1001;");
                    System.out.println("首页后台接通率  企业id:" + compId + "企业名：" + compName + "\tsql语句：" + sqlqisuc.toString());
                    touchSuclist = jdbcTemplate.queryForList(sqlqisuc.toString(), new Object[]{compId});
                    int suc = Integer.valueOf(touchSuclist.get(0).get("numSuc").toString());
                    double tempresult = suc * 100.0 / 300;
                    jietongPercent = df1.format(tempresult);
                    map.put("compName", compName);
                    map.put("jietongPercent", Double.valueOf(jietongPercent));
                    //map.put(compName,jietongPercent);
                    jietonglv.add(map);
                }
            }
            //企业有效率 折线统计图
            String effectiveRateSql = "SELECT batch_name AS batchName,comp_name AS companyName,IFNULL(upload_num/success_num,0) AS effectiveRate FROM nl_batch ORDER BY " +
                    "upload_time DESC LIMIT 10";
            List<Map<String, Object>> effectiveRate = jdbcTemplate.queryForList(effectiveRateSql);

            //企业签收率 折现统计图
            StringBuffer receiveRate = new StringBuffer("SELECT t1.id,t1.batch_name,t1.comp_name AS companyName,");
            receiveRate.append("ROUND(SUM( CASE t3.`status` WHEN '4' THEN 1 ELSE 0 END ) / SUM( CASE t3.`status` WHEN '1' THEN 0 ELSE 1 END ),2) AS receiveRate  ")
                    .append(" FROM nl_batch t1").append(" LEFT JOIN nl_batch_detail t2 ON t1.id = t2.batch_id")
                    .append(" LEFT JOIN t_touch_express_log t3 ON t2.touch_id = t3.touch_id ")
                    .append("GROUP BY t1.id, t1.batch_name,t1.comp_name  ORDER BY t1.upload_time DESC LIMIT 10");
            List<Map<String, Object>> receiveRateList = jdbcTemplate.queryForList(receiveRate.toString());

            //客户有效数据趋势图
            StringBuffer effectiveNum = new StringBuffer("SELECT DATE_FORMAT(upload_time, '%Y-%m-%d') AS upload_time,");
            effectiveNum.append("SUM(CASE `status` WHEN '1' THEN 1 ELSE 0 END) AS effective_num  FROM nl_batch_detail ")
                    .append("GROUP BY DATE_FORMAT(upload_time,'%Y-%m-%d') ").append("ORDER BY DATE_FORMAT(upload_time,'%Y-%m-%d') DESC")
                    .append(" LIMIT 10");
            List<Map<String, Object>> effectiveNumMap = jdbcTemplate.queryForList(effectiveNum.toString());
            data.put("effectiveRate", effectiveRate);
            data.put("callSuccPercent", jietonglv);
            data.put("uploadSucCompsList", uploadgeList);
            data.put("uploadNumList", uploadNumList);
            data.put("receiveRate", receiveRateList);
            data.put("effectiveNum", effectiveNumMap);
        } else {
            //4 一个企业的最近7批次 的各批次的上传数、匹配数
            StringBuffer sqlzuij = new StringBuffer();
            List<Map<String, Object>> sevenList;
            sqlzuij.append("SELECT n.batch_name,n.upload_num,n.comp_id,n.id as batch_id,n.upload_time,\n" +
                    "(SELECT COUNT(DISTINCT id_card) FROM nl_batch_detail WHERE batch_id = n.id AND `status` = 1 AND id IS NOT NULL) AS success_num\n" +
                    "from nl_batch n \n" +
                    " where n.status=0 \n" +
                    "and comp_id=?\n" +
                    "and n.certify_type !=3\n" +
                    "group by n.id\n" +
                    "ORDER BY n.upload_time desc  limit 7\n");
            sevenList = jdbcTemplate.queryForList(sqlzuij.toString(), new Object[]{customerId});

            //5 a.某企业某批次呼叫成功的各批次数据
            StringBuffer qiyejihuao = new StringBuffer();
            List<Map<String, Object>> qiyehujiao;
            /* qiyejihuao.append("SELECT count(t.callSid) seccussCallNum,n.id as batch_id,n.batch_name,n.comp_id,n.upload_time, " +
                    "t.status from t_touch_voice_log t LEFT JOIN  \n" +
                    "nl_batch n on t.batch_id = n.id  WHERE t.`status`=1001 and n.comp_id=? " +
                    "and n.id=? ;\n");*/
            qiyejihuao.append("SELECT c.callSid, COUNT(distinct c.superId) seccussCallNum ");
            qiyejihuao.append("FROM t_touch_voice_log t ");
            qiyejihuao.append("LEFT JOIN t_callback_info c ON t.callSid = c.callSid ");
            qiyejihuao.append("WHERE t.batch_id = ? AND t.cust_id= ? AND t.`status` = '1001' AND c.Calledduration >0 ");
            //5 b.某企业呼叫的各批次数据
            StringBuffer qiyejihuao1 = new StringBuffer();
            List<Map<String, Object>> qiyehujiaoall;
            qiyejihuao1.append("SELECT count(DISTINCT t.superid) callAllnum,n.id as batch_id,n.batch_name,n.upload_time, t.status " +
                    "from nl_batch n  LEFT JOIN  t_touch_voice_log t on t.batch_id = n.id  WHERE  " +
                    "n.comp_id=? AND n.`status`=0 group by n.id ORDER BY n.upload_time DESC limit 7;\n");
            qiyehujiaoall = jdbcTemplate.queryForList(qiyejihuao1.toString(), new Object[]{customerId});

            if (qiyehujiaoall != null && qiyehujiaoall.size() > 0) {
                //删除集合中callAllnum为0的数据
                for (int i = 0; i < qiyehujiaoall.size(); i++) {
                    if ("0".equals(String.valueOf(qiyehujiaoall.get(i).get("callAllnum")))) {
                        qiyehujiaoall.remove(qiyehujiaoall.get(i));
                    }
                }
                for (Map m : qiyehujiaoall) {
                    String batchId = String.valueOf(m.get("batch_id"));
                    qiyehujiao = jdbcTemplate.queryForList(qiyejihuao.toString(), new Object[]{batchId, customerId});
                    if (qiyehujiao != null && qiyehujiao.size() > 0) {
                        m.put("seccussCallNum", qiyehujiao.get(0).get("seccussCallNum"));
                    }
                }
            }

            //某企业地址修复统计图
            StringBuffer sqlsite = new StringBuffer();
            List<Map<String, Object>> siteList;
            sqlsite.append("SELECT n.batch_name,n.upload_num,n.comp_id,n.id as batch_id,n.upload_time,n.success_num,\n" +
                    " (SELECT COUNT(*) FROM nl_batch_detail WHERE batch_id = n.id AND `status` = 0) AS failNum\n" +
                    "from nl_batch n \n" +
                    " where n.status=0\n" +
                    "and comp_id=?\n" +
                    "and n.certify_type =3\n" +
                    "group by n.id\n" +
                    "ORDER BY n.upload_time desc  limit 7\n");
            siteList = jdbcTemplate.queryForList(sqlsite.toString(), new Object[]{customerId});
            if (siteList != null && siteList.size() > 0) {
                for (Map mapsevenl : siteList) {
                    if (mapsevenl.get("failNum") != null && mapsevenl.get("upload_num") != null) {
                        int failNum = Integer.valueOf(mapsevenl.get("failNum").toString());
                        int uploadNum = Integer.valueOf(mapsevenl.get("upload_num").toString());
                        int success_num = uploadNum - failNum;
                        mapsevenl.put("success_num", success_num);
                    } else if (mapsevenl.get("failNum") == null && mapsevenl.get("upload_num") != null) {
                        int uploadNum = Integer.valueOf(mapsevenl.get("upload_num").toString());
                        int failNum = Integer.valueOf("0");
                        int success_num = uploadNum - failNum;
                        mapsevenl.put("success_num", success_num);
                    }
                }
            }
            //某企业的快递签收统计
            StringBuffer sign = new StringBuffer();
            List<Map<String, Object>> signl;
            sign.append("SELECT COUNT(DISTINCT t.receive_name) qianshou ");
            sign.append("FROM t_touch_express_log t ");
            sign.append("WHERE t.cust_id=? AND t.batch_id= ? AND t.status= 4 ");

            //前端首页 校验统计图
            StringBuffer checkSql = new StringBuffer("SELECT batch_name AS batchName,IFNULL(upload_num,0) AS uploadNum,IFNULL(success_num,0) AS successNum,");
            checkSql.append("IFNULL(success_num/upload_num,0) AS effectiveRate FROM nl_batch WHERE comp_id='").append(customerId).append("' ORDER BY ")
                    .append("upload_time DESC LIMIT 10");
            List<Map<String, Object>> checkStatistics = jdbcTemplate.queryForList(checkSql.toString());
//            DecimalFormat format = new DecimalFormat("0%");
//            format.setMaximumFractionDigits(0);
//            for (Map<String, Object> e : checkStatistics) {
//                try {
//                    String number = format.format(e.get("effectiveRate"));
//                    e.put("effectiveRate", number);
//                } catch (Exception ex) {
//                    LOG.info("====>>>>>>>解析失败 " + ex.getMessage());
//                }
//            }
//            for(Map<String,Object> e:checkStatistics){
//                BigDecimal bigDecimal = new BigDecimal(String.valueOf(e.get("effectiveRate")));
//
//
//            }
            checkStatistics.stream().map(e -> e.put("effectiveRate",new BigDecimal(String.valueOf(e.get("effectiveRate")))
                    .setScale(2,BigDecimal.ROUND_HALF_UP))).collect(Collectors.toList());
            //前端首页 签收统计图
            StringBuffer signAndReceive = new StringBuffer("SELECT t1.id,t1.batch_name,SUM(CASE t3.`status` WHEN '1' THEN 0 ELSE 1 END) AS sendVal,");
            signAndReceive.append("SUM(CASE t3.`status` WHEN '4' THEN 1 ELSE 0 END) AS receiveVal,")
                    .append("SUM(CASE t3.`status` WHEN '2' THEN 1 WHEN '3' THEN 1 ELSE 0 END) AS sendingVal,")
                    .append("SUM(CASE t3.`status` WHEN '5' THEN 1 ELSE 0 END) AS rejectionVal ")
                    .append("FROM nl_batch t1 ")
                    .append("LEFT JOIN nl_batch_detail t2 ON t1.id=t2.batch_id ")
                    .append("LEFT JOIN t_touch_express_log t3 ON t2.touch_id=t3.touch_id ")
                    .append("WHERE t1.comp_id='").append(customerId).append("' ")
                    .append(" GROUP BY t1.id,t1.batch_name ")
                    .append("ORDER BY t1.upload_time DESC LIMIT 10");
            List<Map<String,Object>> signAndReceiveStatistic = jdbcTemplate.queryForList(signAndReceive.toString());

            data.put("checkStatistics", checkStatistics);
            data.put("signAndReceive",signAndReceiveStatistic);
            data.put("siteList", siteList);
            data.put("sevenList", sevenList);
            data.put("qiyehujiao", qiyehujiaoall);
        }

        return data;
    }

    @Override
    public Page getSmsTemplateList(PageParam page, TemplateParam templateParam) {
        String title = templateParam.getTemplateName();
        String sms_signatures = templateParam.getSmsSignatures();
        String templateId = templateParam.getTemplateId();
        String type_code = templateParam.getTypeCode();
        String status = templateParam.getStatus();
        String customerId = templateParam.getCompId();
        String templateCode = templateParam.getTemplateCode();
        String enterpriseName = templateParam.getEnterPriseName();
        StringBuffer sb = new StringBuffer();
        sb.append("select t.title,t.id,t.create_time,t.sms_signatures,t.template_code,c.enterprise_name,t.cust_id,\n" +
                "case t.status when 1 then '审核中' when 2 then '审批通过' when 3 then '审批未通过' when 4 then '审批通过后无效' end  status,\n" +
                "t.mould_content,t.remark \n" +
                "from t_template t LEFT JOIN t_customer c on t.cust_id=c.cust_id\n" +
                "where 1=1   ");
        if (null != title && !"".equals(title)) {
            sb.append(" and t.title like '%" + title + "%'");
        }
        if (null != templateId && !"".equals(templateId)) {
            sb.append(" and t.id=" + templateId + "");
        }
        if (null != status && !"".equals(status)) {
            sb.append(" and t.status=" + status + "");
        }
        if (null != customerId && !"".equals(customerId)) {
            sb.append(" and t.cust_id='" + customerId + "'");
        }
        if (null != sms_signatures && !"".equals(sms_signatures)) {
            sb.append(" and t.sms_signatures like '%" + sms_signatures + "%'");
        }

        if (null != templateCode && !"".equals(templateCode)) {
            sb.append(" and t.template_code like '%" + templateCode + "%'");
        }

        if (null != enterpriseName && !"".equals(enterpriseName)) {
            sb.append(" and c.enterprise_name like '%" + enterpriseName + "%'");
        }

        sb.append(" and t.type_code=" + type_code);
        sb.append(" ORDER BY t.create_time DESC");
        return new Pagination().getPageData(sb.toString(), null, page, jdbcTemplate);
    }

    @Override
    public Map<String, Object> updateSmsTemplate(TemplateParam templateParam) {

        Map<String, Object> map = new HashMap<String, Object>();
        // 增加模板信息
        String sql = "";
        int flag = 0;
        String smsContent = templateParam.getTemplateContent();
        String title = templateParam.getTemplateName();
        String sms_signatures = templateParam.getSmsSignatures();
        String templateId = templateParam.getTemplateId();
        String type_code = templateParam.getTypeCode();
        String deal_type = templateParam.getDealType();
        String customerId = templateParam.getCompId();

        if (type_code.equals("1") && deal_type.equals("2000")) {//编辑
            sql = "update t_template set mould_content=?,title=?,status=1,modify_time=now(),sms_signatures=?,cust_id=? where  ID=?";
            flag = marketResourceDao.executeUpdateSQL(sql, smsContent, title, sms_signatures, customerId, templateId);
        } else if (type_code.equals("1") && deal_type.equals("1000")) {//添加
            sql = "insert into t_template set cust_id=?,title=?,type_code=?,mould_content=?,create_time=now(),status=1,sms_signatures=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, customerId, title, 1, smsContent, sms_signatures);
        } else if (type_code.equals("1") && deal_type.equals("3000")) {
            if (StringUtil.isNotEmpty(templateParam.getTemplateCode())) {//审核通过  第三方平台模板编码
                sql = "update t_template set template_code=?,status=2,pass_time=now() where  ID=?";
                flag = this.marketResourceDao.executeUpdateSQL(sql, templateParam.getTemplateCode(), templateId);
            } else {//审核不通过
                sql = "update t_template set remark=?,status=3,modify_time=now() where  ID=?";
                flag = this.marketResourceDao.executeUpdateSQL(sql, templateParam.getRemark(), templateId);
            }
        } else if (type_code.equals("1") && deal_type.equals("4000")) {//恢复
            sql = "update t_template set status=2,pass_time=now() where  ID=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, templateId);
        } else if (type_code.equals("1") && deal_type.equals("5000")) {//无效
            sql = "update t_template set status=4,modify_time=now() where  ID=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, templateId);
        }
        if (flag == 1) {
            map.put("code", "0");
            map.put("message", "成功");
        } else {
            map.put("code", "1");
            map.put("message", "失败");
        }
        return map;
    }

    @Override
    public List<Map<String, Object>> soundUrllist(RecordVoiceQueryParam recordVoiceQueryParam) {
        StringBuffer sb = new StringBuffer();
        //user_type 1.管理员   2.普通用户   superId为用户id(联通返回的)及customerId
        sb.append("  select voicLog.create_time,voicLog.user_id,voicLog.batch_id, substring_index( backInfo.recordurl ,'/' , -1 ) as recordurl ")
                .append("  from t_touch_voice_log voicLog")
                .append("  LEFT JOIN t_callback_info backInfo ")
                .append("  ON voicLog.callSid = backInfo.callSid")
                .append("  LEFT JOIN t_customer_user  tUser ")
                .append("  ON tUser.id =  voicLog.user_id")
                .append("  where 1=1").append(" and channel IS NOT NULL ");
        if (StringUtil.isNotEmpty(recordVoiceQueryParam.getRealName())) {
            sb.append(" AND   tUser.realname LIKE '%" + recordVoiceQueryParam.getRealName() + "%'");
        }
        if (StringUtil.isNotEmpty(recordVoiceQueryParam.getEnterpriseId())) {
            sb.append(" AND  voicLog.enterprise_id= " + recordVoiceQueryParam.getEnterpriseId());
        }
        if (StringUtil.isNotEmpty(recordVoiceQueryParam.getBatchId())) {
            sb.append(" AND   voicLog.batch_id=" + recordVoiceQueryParam.getBatchId());
        }
        if (StringUtil.isNotEmpty(recordVoiceQueryParam.getSuperId())) {
            sb.append(" AND   voicLog.superid=" + recordVoiceQueryParam.getSuperId());
        }
        if (StringUtil.isNotEmpty(recordVoiceQueryParam.getCustId())) {
            sb.append(" AND   voicLog.cust_id=" + recordVoiceQueryParam.getCustId());
        }
        if (StringUtil.isNotEmpty(recordVoiceQueryParam.getUserType())) {
            if (recordVoiceQueryParam.getUserType().equals("2") && recordVoiceQueryParam.getUserId() != null) {
                sb.append(" AND   voicLog.user_id=" + recordVoiceQueryParam.getUserId());
            }
        }
        if (StringUtil.isNotEmpty(recordVoiceQueryParam.getCreateTimeStart()) && StringUtil.isNotEmpty(recordVoiceQueryParam.getCreateTimeEnd())) {
            sb.append(" AND voicLog.create_time BETWEEN '" + recordVoiceQueryParam.getCreateTimeStart() + "' and '" + recordVoiceQueryParam.getCreateTimeEnd() + "' ");
        } else {
            if (StringUtil.isNotEmpty(recordVoiceQueryParam.getCreateTimeStart())) {
                sb.append(" AND voicLog.create_time > '" + recordVoiceQueryParam.getCreateTimeStart() + "'");
            }
            if (StringUtil.isNotEmpty(recordVoiceQueryParam.getCreateTimeEnd())) {
                sb.append(" AND voicLog.create_time < '" + recordVoiceQueryParam.getCreateTimeEnd() + "'");
            }
        }
        sb.append("  AND   voicLog.status=1001");
        sb.append(" order by voicLog.create_time DESC");
        LOG.info("录音文件批量下载功能查询sql：" + sb.toString());
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString());
        return list;
    }

    @Override
    public List<Map<String, Object>> soundUrl(RecordVoiceQueryParam recordVoiceQueryParam) {
        StringBuffer sb = new StringBuffer();
        //user_type 1.管理员   2.普通用户   superId为用户id(联通返回的)及customerId
        sb.append("  SELECT voicLog.user_id,substring_index(backInfo.recordurl, '/', - 1) AS recordurl,voicLog.touch_id\n" +
                "FROM t_touch_voice_log voicLog\n" +
                "LEFT JOIN t_callback_info backInfo ON voicLog.callSid = backInfo.callSid\n" +
                "WHERE 1 = 1 ");
        if (StringUtil.isNotEmpty(recordVoiceQueryParam.getTouchId())) {
            sb.append(" AND  voicLog.touch_id= " + recordVoiceQueryParam.getTouchId());
        }
        LOG.info("录音文件单个下载功能查询sql：" + sb.toString());
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString());
        return list;
    }

    /**
     * 获取供应商短信单价
     *
     * @param custId
     * @return
     */
    public double getSourceSmsPrice(String custId, String supplierId) {
        Map<String, Object> smsPrice = sourceDao.querySupplierPrice(custId, supplierId);
        if (smsPrice != null && smsPrice.get("smsprice") != null) {
            DecimalFormat df = new DecimalFormat("0.00");
            String smsprice = df.format(Double.parseDouble(String.valueOf(smsPrice.get("smsprice"))));
            return Double.parseDouble(smsprice);
        }
        return 0;
    }

    /**
     * 获取客户短信单价
     *
     * @param custId
     * @return
     */
    public double getCustSmsPrice(String custId) {
        CustomerPropertyDO cucSaleSmsPrice = customerDao.getProperty(custId, "cuc_sms_price");
        if (cucSaleSmsPrice != null && StringUtil.isNotEmpty(cucSaleSmsPrice.getPropertyValue())) {
            return Double.parseDouble(cucSaleSmsPrice.getPropertyValue());
        }
        return 0;
    }


    /**
     * @description 坐席外呼（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/15 15:54
     */
    @Override
    public Map<String, Object> seatCallCenter(String apparentNum, String custId, String userId, String idCard, String batchId) {
        Map<String, Object> data = new HashMap<>();
        int code = 0;
        String activityId = null, enterpriseId = null, apparentNumber, msg = "失败", resourceId = null;
        Map<String, Object> callCenterConfigData = null;
        try {

            // 处理联通外呼配置信息,不满足返回坐席信息未配置
            MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType());
            if (marketResourceEntity != null) {
                resourceId = String.valueOf(marketResourceEntity.getResourceId());
            }
            LOG.info("联通外呼资源id是:" + resourceId);
            callCenterConfigData = callCenterService.getCallCenterConfigDataV1(custId, userId, resourceId);
            boolean configSuccess = false;
            if (callCenterConfigData != null) {
                int size = 0;
                for (Map.Entry<String, Object> config : callCenterConfigData.entrySet()) {
                    if (config.getValue() != null && StringUtil.isNotEmpty(String.valueOf(config.getValue()))) {
                        size++;
                    }
                }
                if (size == callCenterConfigData.size()) {
                    configSuccess = true;
                }
            }

            if (configSuccess) {
                List<Map<String, Object>> batchDetail = marketResourceDao.sqlQuery("SELECT * FROM nl_batch_detail WHERE batch_id = ? AND id = ?", batchId, idCard);

                if (batchDetail.size() > 0) {
                    activityId = String.valueOf(batchDetail.get(0).get("activity_id"));
                    enterpriseId = String.valueOf(batchDetail.get(0).get("enterprise_id"));
                    boolean loginSuccess = false;
                    boolean loggedOutStatus;
                    // 判断坐席状态
                    Map<String, Object> result = callCenterService.unicomGetSeatStatus(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
                    LOG.info("获取联通坐席状态:" + result);
                    // 不是登出状态,则执行登出
                    loggedOutStatus = (result == null || (result != null && result.get("status") != null && !"LoggedOut".equals(String.valueOf(result.get("status")))));
                    if (loggedOutStatus) {
                        callCenterService.unicomSeatLogout(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
                    }
                    //查询主叫号码
                    String workNum = String.valueOf(callCenterConfigData.get("mainNumber"));
                    if (StringUtil.isNotEmpty(workNum)) {
                        result = callCenterService.unicomSeatLogin(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")),
                                String.valueOf(callCenterConfigData.get("seatsPassword")), workNum, 1);
                        LOG.info("调用登录返回结果:" + result.toString());
                        if (result != null && result.get("code") != null) {
                            if ("000".equals(String.valueOf(result.get("code")))) {
                                loginSuccess = true;
                            } else if ("002".equals(String.valueOf(result.get("code")))) {
                                code = 0;
                                msg = "主叫号不存在!";
                            }
                        } else {
                            code = 0;
                            msg = "坐席登录失败";
                        }
                    } else {
                        code = 0;
                        msg = "坐席未配置主叫号码";
                    }
                    if (loginSuccess) {
                        result = callCenterService.unicomSeatMakeCallEx(String.valueOf(callCenterConfigData.get("callCenterId")),
                                String.valueOf(callCenterConfigData.get("account")),
                                String.valueOf(batchDetail.get(0).get("activity_id")), String.valueOf(batchDetail.get(0).get("provide_id")),
                                String.valueOf(batchDetail.get(0).get("id")), apparentNum);
                        LOG.info("调用外呼返回结果:" + result.toString());
                        if (result != null && "000".equals(String.valueOf(result.get("code")))) {
                            result.put("activity_id", activityId);
                            result.put("enterprise_id", enterpriseId);
                            result.put("code", "000");
                            return result;
                        }/* else {
                            LOG.error("调用外呼失败,结果:" + result.toString());
                            *//*code = 0;
                            msg = "呼叫失败";*//*

                        }*/
                        if (result != null && "218".equals(String.valueOf(result.get("code")))) {
                            result.put("activity_id", activityId);
                            result.put("enterprise_id", enterpriseId);
                            result.put("msg", "呼叫失败！此号码总呼叫次数达到上限！");
                            result.put("code", "007");
                            return result;
                        }
                        if (result != null && "004".equals(String.valueOf(result.get("code")))) {
                            result.put("activity_id", activityId);
                            result.put("enterprise_id", enterpriseId);
                            result.put("msg", "呼叫失败！不能呼叫自己的分机号码");
                            result.put("code", "008");
                            return result;
                        }
                    }
                } else {
                    code = 0;
                    msg = "读取失联人信息失败";
                }
            } else {
                code = 0;
                msg = "坐席配置信息未配置!";
            }
        } catch (Exception e) {
            LOG.error("联通坐席外呼失败,", e);
            code = 0;
            msg = "呼叫失败";
        } finally {
            // 坐席退出
            if (callCenterConfigData != null) {
                LOG.info("呼叫完成执行坐席退出," + callCenterConfigData.toString());
                callCenterService.unicomSeatLogout(String.valueOf(callCenterConfigData.get("callCenterId")), String.valueOf(callCenterConfigData.get("account")));
            }
            data.put("code", code);
            data.put("msg", msg);
            data.put("activity_id", activityId);
            data.put("enterprise_id", enterpriseId);
        }
        return data;
    }

    /**
     * @description 核验外显号码是否存在（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/15 18:44
     */
    @Override
    public String checkPropertyValue(String resourceId, String batchId, String userId, String mainNumber, String seatAccount, String apparentNumber, String custId, String channel) throws Exception {
        String propertyName = null;
        if (channel.equals("2")) {
            //根据渠道设置key
            propertyName = "cuc";
        }
        if (channel.equals("3")) {
            propertyName = "cmc";
        }
        if (channel.equals("4")) {
            propertyName = "ctc";
        }
        Boolean flag = true;
        String code = "0";
        //核验是否配置了坐席信息
        CustomerUserPropertyDO seatProperty = customerUserDao.getProperty(userId, propertyName + "_seat");
        if (seatProperty == null || ("").equals(seatProperty.getPropertyValue())) {
            code = "003";
            return code;
        } else {

            //核验是否设置了主叫号码跟传递参数是否一致
            com.alibaba.fastjson.JSONObject json = JSON.parseObject(seatProperty.getPropertyValue());
            if (json != null) {
                //判断传递的主叫号码是否跟数据库一致
                boolean mainNumberFlag = mainNumber.equals(json.getString("mainNumber"));
                if (mainNumberFlag == false) {
                    code = "004";
                    return code;
                }
            }
        }
        //查询企业下面配置的外显号码
        LOG.info("资源id是：" + resourceId + " custId是" + custId);
        JSONObject customerMarketResource = getCustomerMarketResource(custId, resourceId);
        if (customerMarketResource != null) {
//           String propertyValue = customerMarketResource.getString(ResourceEnum.CALL.getApparentNumber());
            String propertyValue = null;
            List<String> apparentsList = Arrays.asList(propertyValue.split(","));
            if (apparentsList.size() > 0) {
                flag = apparentsList.contains(apparentNumber);
                if (flag == false) {
                    code = "005";
                    return code;
                }
            }
        }

        //核验批次Id是否存在
        String batchName = batchDao.getBatchName(batchId);
        if (batchName == null || "".equals(batchName)) {
            code = "009";
            return code;
        }
        return code;
    }

    /**
     * @description 获取短信模板内容
     * @date: 2018/11/19 9:41
     */
    @Override
    public String getSmsTemplateMessage(int templateId, int typeCode, String custId) {
        MarketTemplate marketTemplate = sourceDao.getMarketTemplate(templateId, typeCode, custId);
        String mouldContent = null;
        if (marketTemplate != null) {
            mouldContent = marketTemplate.getMouldContent();
        }
        return mouldContent;
    }

    /**
     * @description 给批次下客户发送短信
     * @author:duanliying
     * @method
     * @date: 2018/11/19 10:20
     */
    @Override
    public Map<String, Object> sendSmsbyBatch(int channel, int templateId, String seatAccount, String custId, String variables, int i, String batchId, String customerId, int i1, int i2) {
        int sendSuccessCount = 0;
        String userId = null;
        List<String> variableList = null;
        MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
        Map<String, Object> map = new HashMap<>();
        if (variables != null && variables.contains(",")) {
            variableList = Arrays.asList(variables.split(","));
        }
        try {
            //根据custId和账号获取userId
            CustomerUserDO account = customerUserDao.getCustomer(seatAccount, custId);
            if (account != null) {
                userId = String.valueOf(account.getId());
            }
            //根据供应商id和类型查询出resourceId
            MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.SMS.getType());
            String resourceId = null;
            if (marketResourceEntity != null) {
                resourceId = String.valueOf(marketResourceEntity.getResourceId());
                marketResourceLogDTO.setResourceId(marketResourceEntity.getResourceId());
            }
            //查询模板内容
            MarketTemplate marketTemplate = sourceDao.getMarketTemplate(templateId, 1, custId);
            String touchId = String.valueOf(IDHelper.getTransactionId());
            marketResourceLogDTO.setTouch_id(touchId);
            marketResourceLogDTO.setType_code(TYPE_CODE);
            marketResourceLogDTO.setResname("sms");
            marketResourceLogDTO.setUser_id(Long.parseLong(userId));
            marketResourceLogDTO.setCust_id(custId);
            marketResourceLogDTO.setSuperId(customerId);
            marketResourceLogDTO.setCallSid("");
            marketResourceLogDTO.setBatchId(batchId);
            marketResourceLogDTO.setChannel(channel);
            marketResourceLogDTO.setAmount(0);
            marketResourceLogDTO.setProdAmount(0);
            if (marketTemplate != null) {
                marketResourceLogDTO.setSms_content(marketTemplate.getMouldContent());
                BatchDetail batchDetail;
                SendSmsDTO sendSmsDTO = new SendSmsDTO();
                String sendResult;
                int custSmsPrice = 0, supSmsPrice = 0;
                BigDecimal custSmsAmount, sourceSmsAmount;
                boolean accountDeductionStatus, sourceSmsDeductionStatus;
                JSONObject result = null;
                batchDetail = batchDetailDao.getBatchDetail(customerId, batchId);
                JSONObject smsConfigData = getCustomerMarketResource(custId, resourceId);
                if (smsConfigData != null) {
                    LOG.info("发送短信custId:" + custId + "配置参数是" + smsConfigData.toJSONString());
                    //            sendSmsDTO.setEntId(smsConfigData.getString(ResourceEnum.SMS.getCallCenterId()));
                }
                if (batchDetail != null) {
                    sendSmsDTO.setMessageWord(marketTemplate.getTemplateCode());
                    sendSmsDTO.setActivityId(batchDetail.getActivityId());

                    if (variableList != null) {
                        sendSmsDTO.setVariableOne(variableList.size() > 0 ? variableList.get(0) : "");
                        sendSmsDTO.setVariableTwo(variableList.size() > 1 ? variableList.get(1) : "");
                        sendSmsDTO.setVariableThree(variableList.size() > 2 ? variableList.get(2) : "");
                        sendSmsDTO.setVariableFour(variableList.size() > 3 ? variableList.get(3) : "");
                        sendSmsDTO.setVariableFive(variableList.size() > 4 ? variableList.get(4) : "");
                    }
//                    sendResult = sendSmsServiceImpl.sendSmsService(sendSmsDTO, customerId, batchId);
//                    if (StringUtil.isNotEmpty(sendResult)) {
//                        result = JSONObject.parseObject(sendResult);
//                        marketResourceLogDTO.setRemark(result.getString("msg"));
//                        map.put("code", result.get("code"));
//                        map.put("touchId", touchId);
//                    }
                    marketResourceLogDTO.setActivityId(batchDetail.getActivityId());
                    marketResourceLogDTO.setEnterpriseId(batchDetail.getEnterpriseId());
//                    marketResourceLogDTO.setCallBackData(sendResult);

                    // 发送成功
//                    if (StringUtil.isNotEmpty(sendResult) && "000".equals(JSON.parseObject(sendResult, Map.class).get("code"))) {
//                        sendSuccessCount++;
//                        marketResourceLogDTO.setRemark("发送成功");
//                        marketResourceLogDTO.setStatus(1001);
//                        // 账户余额扣款
//                        if (smsConfigData != null) {
//                            //获取短信的销售定价
//                            //             custSmsPrice = NumberConvertUtil.transformtionCent(smsConfigData.getDoubleValue(ResourceEnum.SMS.getPrice()));
//                        }
//                        custSmsAmount = new BigDecimal(custSmsPrice);
//                        LOG.info("短信扣费客户:" + custId + ",开始扣费,金额（分）:" + custSmsAmount.doubleValue());
//                        accountDeductionStatus = customerDao.accountDeductions(custId, custSmsAmount);
//                        LOG.info("短信扣费客户:" + custId + ",扣费状态:" + accountDeductionStatus);
//
//                        // 供应商余额扣款
//                        JSONObject supplierMarketResource = null;
//                        if (StringUtil.isNotEmpty(resourceId)) {
//                            supplierMarketResource = getSupplierMarketResource(Integer.parseInt(resourceId));
//                        }
//                        if (supplierMarketResource != null) {
//                            //获取供应商短信价格
//                            //                supSmsPrice = NumberConvertUtil.transformtionCent(supplierMarketResource.getDoubleValue(ResourceEnum.SMS.getPrice()));
//                        }
//                        // 供应商余额扣款
//                        LOG.info("短信扣费供应商:" + custId + "开始短信扣费,金额(分):" + supSmsPrice);
//                        //供应商扣费需要转换为分进行扣减
//                        sourceSmsAmount = new BigDecimal(supSmsPrice);
//                        LOG.info("短信扣费供应商:" + custId + "开始短信扣费,金额:" + sourceSmsAmount);
//                        sourceSmsDeductionStatus = sourceDao.supplierAccountDuctions(SupplierEnum.CUC.getSupplierId(), sourceSmsAmount);
//                        LOG.info("短信扣费供应商:" + custId + "短信扣费状态:" + sourceSmsDeductionStatus);
//                        //短信log表，添加企业和供应商扣减金额
//                        marketResourceLogDTO.setAmount(custSmsPrice);
//                        marketResourceLogDTO.setProdAmount(supSmsPrice);
//                    } else {
//                        marketResourceLogDTO.setRemark(result.getString("msg"));
//                        marketResourceLogDTO.setStatus(1002);
//                    }
                } else {
                    marketResourceLogDTO.setRemark("未查询到失联人员信息");
                    marketResourceLogDTO.setStatus(1002);
                    map.put("code", "006");
                    map.put("touchId", touchId);
                    LOG.info("联通短信发送id:" + customerId + ",batchId:" + batchId + "未查到批次详情数据");
                }

            }
        } catch (Exception e) {
            marketResourceLogDTO.setStatus(1002);
            LOG.error("发送短信异常,", e);
        }
        this.insertLog(marketResourceLogDTO);
        return map;
    }

    /**
     * @description 查询短信记录（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/22 19:09
     */
    @Override
    public Page openSmsHistory(PageParam page, String custId) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT t.touch_id touchId,t.remark,t.create_time createTime,t.`status`,t.sms_content smsContent,t.superid superId,t.batch_id batchId,t.channel,u.account ");
        stringBuffer.append(" FROM t_touch_sms_log t LEFT JOIN t_customer_user u ON t.user_id = u.id ");
        stringBuffer.append(" WHERE t.cust_id = " + custId);
        LOG.info("查询单条短信记录sql" + stringBuffer);
        //根据touchId和custId查询一条记录
        return new Pagination().getPageData(stringBuffer.toString(), null, page, jdbcTemplate);
    }

    /**
     * @description 话单推送信息存储
     * @author:duanliying
     * @method
     * @date: 2018/12/6 10:26
     */
    public int addCallBackInfoMessage(CallBackInfoParam callBackInfoParam) throws Exception {
        String updateTouchLogSql = "UPDATE t_touch_voice_log SET amount=?,summ_minute=?,prod_amount=? ,resource_id=? WHERE callSid = ?";
        String updateTouchSql = "UPDATE t_touch_voice_log set status =? WHERE callSid =?";
        String queryTouchSql = "SELECT touch_id touchId ,cust_id custId,user_id userId,superid superId ,batch_id batchId FROM t_touch_voice_log WHERE callSid=?";
        String queryCallInfoSql = "SELECT callSid,Calledduration FROM t_callback_info WHERE callSid=?";
        String insertCallbackInfoSql = "insert into t_callback_info(callSid, appId, flag, Callercaller, Callerstarttime, Callerendtime, Callerduration, CallerringingBeginTime, CallerringingEndTime, " +
                "Calledcaller, Calledstarttime, Calledendtime, Calledduration, CalledringingBeginTime, CalledringingEndTime, recordurl, userData, userId, superId) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        int flag = 0, callTime = 0;
        BigDecimal bigDecimal, sourceCallAmount = null;
        String userId = null, batchId = null, supplierId = null, custId = null, touchId = null, appId = "unicom";
        //扣除企业分钟数
        int summMinute = 0;
        //扣除企业金额
        int summAmount = 0;
        //扣除供应商金额
        int prodAmount = 0;
        //企业通话单价
        int saleCustPrice = 0;
        //供應商通话单价
        int saleSupPrice = 0;

        //获取话单返回的type 和通话时长
        //0：通话未完成  1：通话完成 2：通话失败
        String type = callBackInfoParam.getType();
        //通话时长
        String callDuration = callBackInfoParam.getCallDuration();
        //通话唯一标识  对应数据库的callSid
        String callSid = callBackInfoParam.getSessionId();
        LOG.info("联通推送的callSid是：" + callSid + "返回数据是：" + String.valueOf(callBackInfoParam));
        //根据callsid查询touch——log信息
        LOG.info("查询呼叫记录SQL:" + queryTouchSql);
        List<Map<String, Object>> touchMaps = marketResourceDao.sqlQuery(queryTouchSql, callSid);
        if (touchMaps.size() > 0) {
            userId = String.valueOf(touchMaps.get(0).get("userId"));
            batchId = String.valueOf(touchMaps.get(0).get("batchId"));
            supplierId = String.valueOf(touchMaps.get(0).get("superId"));
            custId = String.valueOf(touchMaps.get(0).get("custId"));
            touchId = String.valueOf(touchMaps.get(0).get("touchId"));
            //判断回调表数据是否已经更新,避免重复扣费（查询回调表数据是否存在）
            List<Map<String, Object>> callInfoMaps = marketResourceDao.sqlQuery(queryCallInfoSql, callSid);
            String resourceId = null;
            MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType());
            if (marketResourceEntity != null) {
                resourceId = String.valueOf(marketResourceEntity.getResourceId());
                LOG.info("资源id是:" + resourceId);
            }
            //判断通话是否成功
            if (callInfoMaps.size() == 0) {
                if (StringUtil.isNotEmpty(type) && StringUtil.isNotEmpty(callDuration) && Integer.parseInt(type) == 1 && Integer.parseInt(callDuration) > 0) {
                    // 更新通话日志表的通话状态为成功
                    marketResourceDao.executeUpdateSQL(updateTouchSql, 1001, callSid);
                    bigDecimal = new BigDecimal((double) Integer.parseInt(callDuration) / 60);
                    callTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();
                    LOG.info("通话分钟数:" + callTime);
                    //查询企业通话费用
                    ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, custId);
                    if (resourcesPriceDto != null && StringUtil.isNotEmpty(resourcesPriceDto.getCallPrice())) {
                        String seatPrice = resourcesPriceDto.getCallPrice();
                        //元转分
                        saleCustPrice = NumberConvertUtil.transformtionCent(Double.parseDouble(seatPrice));
                    }
                    //获取坐席基本信息
                    SeatInfoDto seatInfoDto = customerUserPropertyDao.getSeatMessageById(resourceId, userId, SupplierEnum.CUC.getSupplierId());
                    //企业分钟数
                    int custMinute = seatInfoDto.getSeatCustMinute();
                    //供应商分钟数
                    int supMinute = seatInfoDto.getSeatSupMinute();
                    if (StringUtil.isNotEmpty(userId)) {
                        //查询坐席剩余分钟数
                        LOG.info("坐席:" + userId + "剩余分钟数:" + seatInfoDto.getSeatCustMinute());
                        //通话剩余分钟大于0
                        if (custMinute > 0) {
                            // 通话剩余分钟大于等于通话分钟
                            if (custMinute >= callTime) {
                                LOG.info("坐席执行只扣除分钟数:" + userId + "扣除后剩余分钟数:" + String.valueOf(custMinute - callTime));
                                transactionDao.updateSeatMinute(userId, callTime);
                                summMinute = callTime;
                                //更新通话记录表
                            } else {
                                summMinute = custMinute;
                                LOG.info("坐席执行扣除分钟数和通话计费:" + userId + "扣除分钟数:" + callTime + ",减去分钟数后通话时长:" + (callTime - custMinute));
                                transactionDao.updateSeatMinute(userId, custMinute);
                                //通话扣费金额
                                summAmount = saleCustPrice * (callTime - custMinute);
                                //summAmount = transactionDao.querySeatsMoney(custId, SEAT_ONE_MINUTE_PRICE_KEY, callTime - custMinute);
                                LOG.info("联通坐席扣费开始从账户customerId:" + custId + "余额扣款,sale_price:" + summAmount);
                                transactionDao.accountDeductionsDev(custId, new BigDecimal(summAmount));
                            }
                        } else {
                            //不扣分钟数   直接扣除费用
                            LOG.info("坐席执行只扣除通话计费:" + userId + "通话时长:" + callTime);
                            summAmount = saleCustPrice * callTime;
                            //summAmount = transactionDao.querySeatsMoney(custId, SEAT_ONE_MINUTE_PRICE_KEY, callTime);
                            transactionDao.accountDeductionsDev(custId, new BigDecimal(summAmount));
                        }
                    }
                    // 供应商扣费
                    ResourcesPriceDto resourcesSupPriceDto = null;
                    if (StringUtil.isNotEmpty(resourceId)) {
                        resourcesSupPriceDto = supplierDao.getSupResourceMessageById(Integer.parseInt(resourceId), null);
                    }
                    if (resourcesSupPriceDto != null && StringUtil.isNotEmpty(resourcesSupPriceDto.getCallPrice())) {
                        String supPrice = resourcesSupPriceDto.getCallPrice();
                        //元转分
                        saleSupPrice = NumberConvertUtil.transformtionCent(Double.parseDouble(supPrice));
                    }
                    if (StringUtil.isNotEmpty(custId)) {
                        //通话剩余分钟大于0
                        if (supMinute > 0) {
                            // 通话剩余分钟大于等于通话分钟
                            if (supMinute >= callTime) {
                                LOG.info("供应商只扣除分钟数:" + userId + "扣除后剩余分钟数:" + String.valueOf(supMinute - callTime));
                                sourceDao.accountSupplierMinuts(resourceId, SupplierEnum.CUC.getSupplierId(), callTime, userId);
                                prodAmount = 0;
                            } else {
                                LOG.info("供应商执行扣除分钟数和通话计费:" + userId + "扣除分钟数:" + supMinute + ",减去分钟数后通话时长:" + (callTime - supMinute));
                                //先扣除分钟数
                                sourceDao.accountSupplierMinuts(resourceId, SupplierEnum.CUC.getSupplierId(), supMinute, userId);
                                //扣除通话费用
                                sourceCallAmount = new BigDecimal(saleSupPrice * (callTime - supMinute));
                                sourceDao.supplierAccountDuctions(SupplierEnum.CUC.getSupplierId(), sourceCallAmount);
                            }
                        } else {
                            sourceCallAmount = new BigDecimal(saleSupPrice * callTime);
                            LOG.info("供应商执行只扣除通话计费:" + userId + "通话时长:" + callTime);
                            sourceDao.supplierAccountDuctions(SupplierEnum.CUC.getSupplierId(), sourceCallAmount);
                        }
                    }
                    //更新log表（企业扣费和供应商扣费）
                    if (sourceCallAmount != null) {
                        prodAmount = sourceCallAmount.intValue();
                    }
                    int i = sourceDao.executeUpdateSQL(updateTouchLogSql, new Object[]{summAmount, summMinute, prodAmount, resourceId, callSid});
                    LOG.info("callSid是:" + callSid + "更新数量是:" + i);
                } else {
                    LOG.info("外呼状态失败");
                    marketResourceDao.executeUpdateSQL(updateTouchSql, 1002, callSid);
                }

                //处理时间保存回调记录信息
                String startTime = callBackInfoParam.getStartTime();
                String endTime = callBackInfoParam.getEndTime();
                String recordUrl = callBackInfoParam.getRecordUrl();
                if (StringUtil.isNotEmpty(startTime)) {
                    startTime = startTime.replaceAll("-", "/");
                }
                if (StringUtil.isNotEmpty(endTime)) {
                    endTime = endTime.replaceAll("-", "/");
                }
                //保存回调记录表通话历史参数
                LOG.info("transactionDao" + transactionDao);
                if (StringUtil.isNotEmpty(recordUrl) && !"null".equals(recordUrl)) {
                    recordUrl = recordUrl.replaceAll(".wav", ".mp3");
                }
                flag = transactionDao.executeUpdateSQL(insertCallbackInfoSql, new Object[]{callSid, appId, 1,
                        callBackInfoParam.getLocalUrl(),
                        startTime, endTime,
                        callDuration, null, null,
                        callBackInfoParam.getRemoteUrl(),
                        startTime, endTime,
                        callDuration, null, null,
                        recordUrl,
                        callSid, userId,
                        supplierId});
                if (flag > 0) {
                    LOG.info("联通外呼插入数据库记录(条):" + flag);
                }
            } else {
                LOG.info("callSid是" + callSid + "联通外呼回调记录已经存在");
            }
        } else {
            LOG.info("未查询到该条记录回调数据是:" + callBackInfoParam.toString());
        }

        return flag;
    }

    /**
     * 联通录音文件推送
     *
     * @param
     */
    @Override
    public String getUnicomRecordfile(JSONObject param) throws Exception {
        LOG.info("开始获取联通录音文件callSid是" + param.getString("sessionId"));
        LOG.info("联通推送录音接口参数" + param.toString());
        String callSid = param.getString("sessionId");
        String code = "1";
        String queryTouchSql = "SELECT touch_id touchId ,cust_id custId,user_id userId,superid superId ,batch_id batchId FROM t_touch_voice_log WHERE callSid=?";
        //根据callSid 查询是否存在通过话记录
        List<Map<String, Object>> logList = marketResourceDao.sqlQuery(queryTouchSql, callSid);
        if (logList.size() > 0) {
            //将录音地址保存到录音拉取对接表中sql
            String insertSql = "INSERT INTO t_record_queue (callSid,recordUrl) VALUES(?,?)";
            String querySql = "SELECT * FROM t_record_queue WHERE callSid = ?";
            //获取录音文件地址
            String recordUrl = param.getString("RECORD_URL");
            LOG.info("录音文件地址:" + recordUrl);
            if (StringUtil.isNotEmpty(recordUrl) && recordUrl.contains("htt")) {
                //查询当前录音队列表中是否存在根据callSid，不存在直接插入   存在返回code=0，联通不在推送
                List<Map<String, Object>> maps = marketResourceDao.sqlQuery(querySql, callSid);
                LOG.info("查询当前录音队列表中是否存在sql:" + querySql);
                if (maps != null && maps.size() > 0) {
                    //说明已经存在返回code=0，联通不进行再次推送
                    LOG.info("callSid是:" + callSid + "已经存在");
                    code = "0";
                } else {
                    int i = marketResourceDao.executeUpdateSQL(insertSql, callSid, recordUrl);
                    LOG.info("插入数据库数量:" + i);
                    if (i > 0) {
                        code = "0";
                    }
                }
            }
        } else {
            LOG.info("为查询到该条记录回调数据是:" + param);
        }
        return code;
    }

    @Override
    public Object exportreach(String cust_id, Long userid, String user_type, String superId, String realName, String createTimeStart, String createTimeEnd, String enterpriseId, String batchId, int touchStatus, String enterpriseName, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            StringBuffer sb = new StringBuffer();
            //user_type 1.管理员   2.普通用户   superId为用户id(联通返回的)
            sb.append(
                    "  select voicLog.touch_id,voicLog.cust_id,voicLog.batch_id, voicLog.enterprise_id, voicLog.superid, voicLog.create_time create_time," +
                            "voicLog.status,CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,backInfo.Callerduration,backInfo.Callercaller mainNumber, " +
                            " backInfo.recordurl ")
                    .append("  from t_touch_voice_log voicLog")
                    .append("  LEFT JOIN t_callback_info backInfo ")
                    .append("  ON voicLog.callSid = backInfo.callSid")
                    .append("  where 1=1").append(" and channel IS NOT NULL ");

            if (!"".equals(realName) && null != realName) {
                sb.append(" AND   voicLog.remark LIKE '%" + realName + "%'");
            }

            if (!"".equals(enterpriseName) && null != enterpriseName) {
                sb.append(" AND   voicLog.remark LIKE '%" + enterpriseName + "%'");
            }

            if (!"".equals(enterpriseId) && null != enterpriseId) {
                sb.append(" AND  voicLog.enterprise_id= " + enterpriseId);
            }

            if (!"".equals(batchId) && null != batchId) {
                sb.append(" AND   voicLog.batch_id=" + batchId);
            }

            if (!"".equals(superId) && null != superId) {
                sb.append(" AND   voicLog.superid=" + superId);
            }

            if ("1".equals(user_type) && StringUtil.isNotEmpty(cust_id)) {
                sb.append(" AND   voicLog.cust_id=" + cust_id);
            }
            if ("2".equals(user_type) && userid != null) {
                sb.append(" AND   voicLog.user_id=" + userid);
            }
            if (touchStatus == 1001 || touchStatus == 1002) {
                sb.append(" AND   voicLog.status=" + touchStatus);
            }

            if (null != createTimeStart && !"".equals(createTimeStart) && null != createTimeEnd
                    && !"".equals(createTimeEnd)) {
                sb.append(" AND voicLog.create_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
            } else {
                if (null != createTimeStart && !"".equals(createTimeStart)) {
                    sb.append(" AND voicLog.create_time > '" + createTimeStart + "'");
                }
                if (null != createTimeEnd && !"".equals(createTimeEnd)) {
                    sb.append(" AND voicLog.create_time < '" + createTimeEnd + "'");
                }
            }

            sb.append(" order by voicLog.create_time DESC");
            LOG.info("通过记录sql:\t" + sb.toString());

            List<Map<String, Object>> billlist = jdbcTemplate.queryForList(sb.toString());
            String audioServerUrl = ConfigUtil.getInstance().get("audio_server_url");
            Map<String, Object> mapObj;
            for (int i = 0; i < billlist.size(); i++) {
                mapObj = (Map<String, Object>) billlist.get(i);
                if (mapObj != null && mapObj.get("remark") != null) {
                    String remark = mapObj.get("remark").toString();
                    String remarkArg[] = remark.split("\\{\\}");
                    String realname = "";
                    String enterprisename = "";
                    String account = "";
                    String states = "";
                    String recordurl = "";
                    if (remarkArg != null && remarkArg.length >= 3) {
                        remark = remarkArg[0];
                        realname = remarkArg[1];
                        enterprisename = remarkArg[2];
                    }
                    if (StringUtil.isEmpty(realname)) {
                        if (mapObj.get("user_id") != null) {
                            String user_id = String.valueOf(mapObj.get("user_id"));
                            realname = customerUserDao.getName(user_id);
                            account = customerUserDao.getLoginName(user_id);
                        }
                    }
                    if (StringUtil.isEmpty(enterprisename)) {
                        if (mapObj.get("cust_id") != null) {
                            String custId = mapObj.get("cust_id").toString();
                            enterprisename = customerDao.getEnterpriseName(custId);
                        }
                    }
                    String userId = String.valueOf(mapObj.get("user_id"));
                    if ("1001".equals(String.valueOf(mapObj.get("status"))) && String.valueOf(mapObj.get("recordurl")).contains("http")) {
                        //添加地址
                        recordurl = String.valueOf(mapObj.get("recordurl"));
                        String[] url = recordurl.split("/");
                        //拼接录音地址
                        recordurl = audioServerUrl + "/" + userId + "/" + url[url.length - 1];
                        if (StringUtil.isNotEmpty(recordurl)) {
                            recordurl = recordurl.replaceAll("wav", "mp3");
                        }
                    }
                    mapObj.put("recordurl", recordurl);
                    mapObj.put("enterpriseName", enterprisename);
                    mapObj.put("account", account);
                    mapObj.put("realname", realname);
                    mapObj.put("remark", remark);
                }
            }

            List<List<Object>> data = new ArrayList<>();
            // 设置标题
            List<String> titles = new ArrayList<String>();
            titles.add("用户ID");//1
            titles.add("企业自带ID");
            titles.add("企业名称");
            titles.add("批次ID");
            titles.add("致电时间");
            titles.add("接听状态");//6
            titles.add("通话时长（秒）");
            titles.add("备注");
            titles.add("操作人");
            titles.add("录音地址");

            String fileName = "触达记录";
       /* String enterprisename = null;
        String account = null;
        if (StringUtil.isNotEmpty(cust_id)) {
            CustomerDO customer = customerDao.findUniqueBy("custId", cust_id);
            CustomerUserDO customerUserDO = customerUserDao.getPropertyByCustId(cust_id);
            if (customer != null) {
                enterprisename = customer.getEnterpriseName();
            }
            if (customerUserDO != null) {
                account = customerUserDO.getAccount();
            }
            fileName = enterprisename + "_" + account ;
        }*/
            String fileType = ".xlsx";
            List<Object> rowList;
            for (Map<String, Object> column : billlist) {
                rowList = new ArrayList<>();
                if (column.get("payType") != null) {
                    rowList.add(TransactionEnum.getName(Integer.parseInt(String.valueOf(column.get("payType")))));
                }
                rowList.add(column.get("superId") != null ? column.get("superId") : "");
                rowList.add(column.get("enterprise_id") != null ? column.get("enterprise_id") : "");
                rowList.add(column.get("enterpriseName") != null ? column.get("enterpriseName") : "");
                rowList.add(column.get("batch_id") != null ? column.get("batch_id") : "");
                rowList.add(column.get("create_time") != null ? column.get("create_time") : "");

                if ("1001".equals(String.valueOf(column.get("status")))) {
                    rowList.add("成功");
                }
                if ("1002".equals(String.valueOf(column.get("status")))) {
                    rowList.add("失败");
                }
                rowList.add(column.get("Callerduration") != null ? column.get("Callerduration") : "");
                rowList.add(column.get("remark") != null ? column.get("remark") : "");
                rowList.add(column.get("realname") != null ? column.get("realname") : "");
                rowList.add(column.get("recordurl") != null ? column.get("recordurl") : "");
                data.add(rowList);
            }
            if (data.size() > 0) {
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                OutputStream outputStream;
                outputStream = response.getOutputStream();
                ExcelUtils.getInstance().exportObjects2Excel(data, titles, outputStream);
                outputStream.flush();
                response.flushBuffer();
                outputStream.close();
                LOG.info("触达记录导出成功");
            } else {
                resultMap.put("code", "001");
                resultMap.put("_message", "触达记录无数据导出！");
                return JSON.toJSONString(resultMap);
            }
        } catch (Exception e) {
            LOG.error("触达记录导出失败\t" + e.getMessage());
            resultMap.put("code", "002");
            resultMap.put("_message", "企业账单导出失败！");
        }
        return JSON.toJSONString(resultMap);


    }

    /**
     * 查询成本价列表
     *
     * @return
     */
    @Override
    public Map<String, Object> searchSupplierPrice(SupplierListParam supplierListParam) {
        //供应商id
        String supplierId = supplierListParam.getSupplierId();
        List<Map<String, Object>> priceList = null;
        String type = null;
        HashMap<String, Object> priceMap = new HashMap<>();
        if (SupplierEnum.CMC.getSupplierId().equals(supplierId) || SupplierEnum.CUC.getSupplierId().equals(supplierId) || SupplierEnum.CTC.getSupplierId().equals(supplierId)) {
            //查询的是供应商的成本价
            type = "1";
            Map map = new HashMap<>();
            map.put("seatPrice", "");
            map.put("seatMinutes", "");

            Map fixMap = new HashMap<>();
            fixMap.put("fixpriceBank", null);
            fixMap.put("fixpriceInsurance", "");
            fixMap.put("fixpriceCourt", "");
            fixMap.put("fixpriceOnline", "");

            priceMap.put("callPrice", "");
            priceMap.put("smsPrice", "");
            priceMap.put("seatPrice", JSON.toJSONString(map));
            priceMap.put("fixPrice", JSON.toJSONString(fixMap));

        } else if (SupplierEnum.XZ.getSupplierId().equals(supplierId)) {
            type = "3";
            Map map = new HashMap<>();
            map.put("seatPrice", "");
            map.put("seatMinutes", "");
            priceMap.put("callPrice", "");
            priceMap.put("seatPrice", JSON.toJSONString(map));
        } else {
            //查询的是快递的成本价
            Map map = new HashMap<>();
            map.put("receivedPrice", "");
            map.put("rejectionPrice", "");
            type = "2";
            priceMap.put("expressPrice", JSON.toJSONString(map));
            priceMap.put("jdFixPrice", "");
        }
        StringBuilder stringBuilder = new StringBuilder();
        //根据供应商id查询供应商定价
        stringBuilder.append("SELECT p.property_name,p.property_value FROM t_market_resource r\n");
        stringBuilder.append("LEFT JOIN t_market_resource_property p ON r.resource_id = p.resource_id\n");
        stringBuilder.append("WHERE r.supplier_id =? GROUP BY p.resource_id ");
        priceList = marketResourceDao.sqlQuery(stringBuilder.toString(), supplierId);

        if (priceList.size() > 0) {
            for (int i = 0; i < priceList.size(); i++) {
                String property_name = String.valueOf(priceList.get(i).get("property_name"));
                if (String.valueOf(priceList.get(i).get("property_name")) != null && !"null".equals(String.valueOf(priceList.get(i).get("property_name")))) {
                    String property_value = String.valueOf(priceList.get(i).get("property_value"));
                    priceMap.put(property_name, property_value);
                }
            }
        }
        priceMap.put("type", type);
        return priceMap;
    }

    /**
     * 根据supplierId和type 查询resourceId
     */
    @Override
    public String queryResourceId(String supplierId, int type) {
        //根据type和supplierId查询出resourceId
        String queryResourceIdSql = "SELECT resource_id from t_market_resource WHERE supplier_id =?  AND type_code = ?";
        List<Map<String, Object>> list = sourceDao.sqlQuery(queryResourceIdSql, supplierId, type);
        String resourceId = null;
        if (list.size() > 0) {
            resourceId = String.valueOf(list.get(0).get("resource_id"));
        }
        return resourceId;
    }

    /**
     * 讯众外呼接口
     *
     * @param
     * @return
     */
    @Override
    public Map<Object, Object> xZCallResource(String userType, String batchId, Long userId, String id, String custId, int certifyType) {
        LOG.info("使用外呼方式参数" + "客户id是：" + id + "批次id" + batchId + "企业id" + custId + "修复类型" + certifyType);
        Map<Object, Object> map = new HashMap<>();
        String resourceId = null;
        String callBackResult = null;
        Boolean success = false;
        String message = "呼叫失败";
        String code = "0";
        JSONObject jsonObject = null;
        String enterpriseId = null;
        //触达Id
        String touchId = Long.toString(IDHelper.getTransactionId());
        try {
            //查看用户是否配置了主叫号码
            String workNum = marketResourceService.selectWorkPhoneNum(userId, SupplierEnum.XZ.getSeatName());
            LOG.info("讯众外呼主叫号码是" + workNum);
            if (null == workNum || "".equals(workNum) || "000".equals(workNum)) {
                map.put("msg", "未设置主叫电话或者主叫电话状态异常");
                map.put("code", 1004);
                return map;
            }
            //查看用户是否配置了外显号码
            String batchApparentNumber = null;
            // 查询企业是否设置双向呼叫外显号码
            String apparentNumber = marketResourceService.selectCustCallBackApparentNumber(custId, SupplierEnum.XZ.getApparentNumber());
            LOG.info("讯众外呼外显号码是" + apparentNumber);
            if (StringUtil.isEmpty(apparentNumber)) {
                map.put("message", "未申请外显号码");
                map.put("code", 1005);
                return map;
            }
            //查询批次下是否配置了外显号码
            List<Map<String, Object>> batchApparentNum = marketResourceDao.sqlQuery("SELECT * FROM nl_batch WHERE id = ?", batchId);
            if (batchApparentNum.size() > 0) {
                batchApparentNumber = String.valueOf(batchApparentNum.get(0).get("apparent_number"));
            }
            if (!"".equals(batchApparentNumber) && StringUtil.isNotEmpty(batchApparentNumber) && !"null".equals(batchApparentNumber)) {
                List<String> apparentsList = Arrays.asList(batchApparentNumber.split(","));
                if (apparentsList.size() > 0) {
                    Random random = new Random();
                    int n = random.nextInt(apparentsList.size());
                    apparentNumber = apparentsList.get(n);
                    LOG.info("外显号码使用的是批次下配置的外线号码" + apparentNumber);
                }
            } else if (apparentNumber != null && StringUtil.isNotEmpty(apparentNumber)) {

                String[] apparentByCust = apparentNumber.split(",");
                if (apparentByCust.length > 0) {
                    List<String> apparentList = Arrays.asList(apparentNumber.split(","));
                    Random random = new Random();
                    int n = random.nextInt(apparentList.size());
                    apparentNumber = apparentList.get(n);
                    LOG.info("外显号码使用的是企业配置的外线号码" + apparentNumber);
                }
            }
            //根据batchId和客户id查询拨手机号码
            BatchDetail batchDetail = batchDao.getBatchDetail(id, batchId);
            //查询客户手机号码

            String phoneNum = null;
            String phoneId = null;
            if (batchDetail != null) {
                phoneId = batchDetail.getPhoneId();
                enterpriseId = batchDetail.getEnterpriseId();
                //根据phoneI的查询手机号码
                String queryPhone = "SELECT * FROM u WHERE id = '" + phoneId + "'";
                List<Map<String, Object>> list = batchDao.sqlQuery(queryPhone, new Object[]{});
                if (list.size() > 0) {
                    phoneNum = String.valueOf(list.get(0).get("phone"));
                }
            }
            LOG.info("接收到调用方的参数:" + "主叫号码：" + workNum + "触达电话号码：" + phoneNum + "外显号码：" + apparentNumber + "唯一标识：" + touchId);
            if (StringUtil.isEmpty(phoneNum) || StringUtil.isEmpty(workNum) || StringUtil.isEmpty(apparentNumber)) {
                map.put("msg", "缺少必要参数");
                map.put("code", 1006);
                return map;
            }
            CallBackParam backCallParam = new CallBackParam();
            backCallParam.setAction(SaleApiUtil.DAIL_BACK_CALL_ACTION);
            backCallParam.setAppid(SaleApiUtil.CALL_BACK_APP_ID);

            backCallParam.setSrc(workNum);
            backCallParam.setDst(phoneNum);
            backCallParam.setCustomParm(touchId);
            //TODO 固定参数(后期需要平台提供)"01053502451"

            backCallParam.setSrcclid(apparentNumber);
            backCallParam.setDstclid(apparentNumber);
            LOG.info("调用双向回呼接口请求数据:" + JSON.toJSONString(backCallParam));
            callBackResult = SaleApiUtil.sendCallBack(JSON.toJSONString(backCallParam), SaleApiUtil.ENV);
            LOG.info("调用双向回呼接口返回数据:" + callBackResult);
            //根据供应商和资源类型查询resourceId
            resourceId = marketResourceService.queryResourceId(ConstantsUtil.SUPPLIERID__XZ, ConstantsUtil.CALL_TYPE);
            if (StringUtil.isNotEmpty(callBackResult)) {
                jsonObject = JSON.parseObject(callBackResult);
                // 发送双向呼叫请求成功
                if (StringUtil.isNotEmpty(jsonObject.getString("statusCode")) && "0".equals(jsonObject.getString("statusCode"))) {
                    success = true;
                    // 如果失败则把错误信息返回
                } else {
                    message = jsonObject.getString("statusMsg");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("调用讯众外呼接口异常" + e);
        }
        // 执行成功
        if (success) {
            // 唯一请求ID
            String requestId = jsonObject.getString("requestId");
            // 插入外呼日志表
            MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
            marketResourceLogDTO.setTouch_id(touchId);
            marketResourceLogDTO.setType_code("1");
            marketResourceLogDTO.setResname("voice");
            marketResourceLogDTO.setUser_id(userId);
            marketResourceLogDTO.setCust_id(custId);
            marketResourceLogDTO.setSuperId(id);
            marketResourceLogDTO.setCallSid(requestId);
            //marketResourceLogDTO.setRemark(message);
            marketResourceLogDTO.setEnterpriseId(enterpriseId);
            marketResourceLogDTO.setBatchId(batchId);
            marketResourceLogDTO.setChannel(Integer.parseInt(ConstantsUtil.SUPPLIERID__XZ));
            if (StringUtil.isNotEmpty(resourceId)) {
                marketResourceLogDTO.setResourceId(Integer.parseInt(resourceId));
            }
            // 判断是否管理员进行的外呼
            if ("1".equals(userType)) {
                marketResourceLogDTO.setCallOwner(2);
            } else {
                marketResourceLogDTO.setCallOwner(1);
            }
            // 主叫成功
            marketResourceLogDTO.setStatus(1001);
            marketResourceService.insertLog(marketResourceLogDTO);
            code = "1";
            message = "拨打成功";
        } else {
            // 异常返回输出错误码和错误信息
            LOG.error("请求发送双向呼叫失败,返回数据:" + jsonObject);
            // 插入外呼日志表
            MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
            marketResourceLogDTO.setTouch_id(touchId);
            marketResourceLogDTO.setType_code("1");
            marketResourceLogDTO.setResname("voice");
            marketResourceLogDTO.setUser_id(userId);
            marketResourceLogDTO.setCust_id(custId);
            marketResourceLogDTO.setSuperId(id);
            //marketResourceLogDTO.setRemark(message);
            marketResourceLogDTO.setEnterpriseId(enterpriseId);
            marketResourceLogDTO.setBatchId(batchId);
            marketResourceLogDTO.setChannel(Integer.parseInt(ConstantsUtil.SUPPLIERID__XZ));
            if (StringUtil.isNotEmpty(resourceId)) {
                marketResourceLogDTO.setResourceId(Integer.parseInt(resourceId));
            }
            // 判断是否管理员进行的外呼
            if ("1".equals(userType)) {
                marketResourceLogDTO.setCallOwner(2);
            } else {
                marketResourceLogDTO.setCallOwner(1);
            }
            // 主叫失败
            marketResourceLogDTO.setStatus(1002);
            marketResourceService.insertLog(marketResourceLogDTO);
        }
        map.put("touchId", touchId);
        map.put("code", code);
        map.put("msg", message);
        return map;
    }

    @Override
    public Map<Object, Object> xZCallResourceV1(String userType, String batchId, Long userId, String id, String custId) {
        LOG.info("使用外呼方式参数" + "客户id是：" + id + "批次id" + batchId + "企业id" + custId);
        Map<Object, Object> map = new HashMap<>();
        String callBackResult = null, resourceId = null, workNum = null, apparentNumber = null;
        Boolean success = false;
        String message = "呼叫失败";
        String code = "0";
        JSONObject jsonObject = null;
        String enterpriseId = null;
        Map<String, Object> callCenterConfigData = null;
        //触达Id
        String touchId = Long.toString(IDHelper.getTransactionId());
        try {
            //查询出resourceId
            MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(SupplierEnum.XZ.getSupplierId(), ResourceEnum.CALL.getType());
            if (marketResourceEntity != null) {
                resourceId = String.valueOf(marketResourceEntity.getResourceId());
            }
            LOG.info("讯众外呼资源id是:" + resourceId);
            callCenterConfigData = callCenterService.getXzConfigData(custId, String.valueOf(userId), resourceId);
            LOG.info("讯众外呼查询到配置参数是:" + String.valueOf(callCenterConfigData));
            //查看用户是否配置了主叫号码
            if (callCenterConfigData.size() > 0) {
                workNum = String.valueOf(callCenterConfigData.get("mainNumber"));
                apparentNumber = String.valueOf(callCenterConfigData.get("apparentNumber"));
            }
            LOG.info("讯众外呼主叫号码是" + workNum);
            if (null == workNum || "".equals(workNum) || "000".equals(workNum)) {
                map.put("msg", "未设置主叫电话或者主叫电话状态异常");
                map.put("code", 1004);
                return map;
            }
            //查看用户是否配置了外显号码
            String batchApparentNumber = null;
            LOG.info("讯众外呼外显号码是" + apparentNumber);
            if (StringUtil.isEmpty(apparentNumber)) {
                map.put("message", "未申请外显号码");
                map.put("code", 1005);
                return map;
            }
            //查询批次下是否配置了外显号码
            List<Map<String, Object>> batchApparentNum = marketResourceDao.sqlQuery("SELECT * FROM nl_batch WHERE id = ?", batchId);
            if (batchApparentNum.size() > 0) {
                batchApparentNumber = String.valueOf(batchApparentNum.get(0).get("apparent_number"));
            }
            if (!"".equals(batchApparentNumber) && StringUtil.isNotEmpty(batchApparentNumber) && !"null".equals(batchApparentNumber)) {
                List<String> apparentsList = Arrays.asList(batchApparentNumber.split(","));
                if (apparentsList.size() > 0) {
                    Random random = new Random();
                    int n = random.nextInt(apparentsList.size());
                    apparentNumber = apparentsList.get(n);
                    LOG.info("外显号码使用的是批次下配置的外线号码" + apparentNumber);
                }
            } else if (apparentNumber != null && StringUtil.isNotEmpty(apparentNumber)) {

                String[] apparentByCust = apparentNumber.split(",");
                if (apparentByCust.length > 0) {
                    List<String> apparentList = Arrays.asList(apparentNumber.split(","));
                    Random random = new Random();
                    int n = random.nextInt(apparentList.size());
                    apparentNumber = apparentList.get(n);
                    LOG.info("外显号码使用的是企业配置的外线号码" + apparentNumber);
                }
            }
            //根据batchId和客户id查询拨手机号码
            BatchDetail batchDetail = batchDao.getBatchDetail(id, batchId);
            //查询客户手机号码

            String phoneNum = null;
            String phoneId = null;
            if (batchDetail != null) {
                phoneId = batchDetail.getPhoneId();
                enterpriseId = batchDetail.getEnterpriseId();
                //根据phoneI的查询手机号码
                String queryPhone = "SELECT * FROM u WHERE id = '" + phoneId + "'";
                List<Map<String, Object>> list = batchDao.sqlQuery(queryPhone, new Object[]{});
                if (list.size() > 0) {
                    phoneNum = String.valueOf(list.get(0).get("phone"));
                }
            }
            LOG.info("接收到调用方的参数:" + "主叫号码：" + workNum + "触达电话号码：" + phoneNum + "外显号码：" + apparentNumber + "唯一标识：" + touchId);
            if (StringUtil.isEmpty(phoneNum) || StringUtil.isEmpty(workNum) || StringUtil.isEmpty(apparentNumber)) {
                map.put("msg", "缺少必要参数");
                map.put("code", 1006);
                return map;
            }
            CallBackParam backCallParam = new CallBackParam();
            backCallParam.setAction(SaleApiUtil.DAIL_BACK_CALL_ACTION);
            backCallParam.setAppid(SaleApiUtil.CALL_BACK_APP_ID);

            backCallParam.setSrc(workNum);
            backCallParam.setDst(phoneNum);
            backCallParam.setCustomParm(touchId);
            //TODO 固定参数(后期需要平台提供)"01053502451"

            backCallParam.setSrcclid(apparentNumber);
            backCallParam.setDstclid(apparentNumber);
            LOG.info("调用双向回呼接口请求数据:" + JSON.toJSONString(backCallParam));
            callBackResult = SaleApiUtil.sendCallBack(JSON.toJSONString(backCallParam), SaleApiUtil.ENV);
            LOG.info("调用双向回呼接口返回数据:" + callBackResult);

            if (StringUtil.isNotEmpty(callBackResult)) {
                jsonObject = JSON.parseObject(callBackResult);
                // 发送双向呼叫请求成功
                if (StringUtil.isNotEmpty(jsonObject.getString("statusCode")) && "0".equals(jsonObject.getString("statusCode"))) {
                    success = true;
                    // 如果失败则把错误信息返回
                } else {
                    message = jsonObject.getString("statusMsg");
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("调用讯众外呼接口异常" + e);
        }
        // 执行成功
        if (success) {
            // 唯一请求ID
            String requestId = jsonObject.getString("requestId");
            // 插入外呼日志表
            MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
            marketResourceLogDTO.setTouch_id(touchId);
            marketResourceLogDTO.setType_code("1");
            marketResourceLogDTO.setResname("voice");
            marketResourceLogDTO.setUser_id(userId);
            marketResourceLogDTO.setCust_id(custId);
            marketResourceLogDTO.setSuperId(id);
            marketResourceLogDTO.setCallSid(requestId);
            //marketResourceLogDTO.setRemark(message);
            marketResourceLogDTO.setEnterpriseId(enterpriseId);
            marketResourceLogDTO.setBatchId(batchId);
            marketResourceLogDTO.setChannel(Integer.parseInt(ConstantsUtil.SUPPLIERID__XZ));
            if (StringUtil.isNotEmpty(resourceId)) {
                marketResourceLogDTO.setResourceId(Integer.parseInt(resourceId));
            }

            // 判断是否管理员进行的外呼
            if ("1".equals(userType)) {
                marketResourceLogDTO.setCallOwner(2);
            } else {
                marketResourceLogDTO.setCallOwner(1);
            }
            // 主叫成功
            marketResourceLogDTO.setStatus(1001);
            marketResourceService.insertLog(marketResourceLogDTO);
            code = "1";
            message = "拨打成功";
        } else {
            // 异常返回输出错误码和错误信息
            LOG.error("请求发送双向呼叫失败,返回数据:" + jsonObject);
            // 插入外呼日志表
            MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
            marketResourceLogDTO.setTouch_id(touchId);
            marketResourceLogDTO.setType_code("1");
            marketResourceLogDTO.setResname("voice");
            marketResourceLogDTO.setUser_id(userId);
            marketResourceLogDTO.setCust_id(custId);
            marketResourceLogDTO.setSuperId(id);
            //marketResourceLogDTO.setRemark(message);
            marketResourceLogDTO.setEnterpriseId(enterpriseId);
            marketResourceLogDTO.setBatchId(batchId);
            marketResourceLogDTO.setChannel(Integer.parseInt(ConstantsUtil.SUPPLIERID__XZ));
            if (StringUtil.isNotEmpty(resourceId)) {
                marketResourceLogDTO.setResourceId(Integer.parseInt(resourceId));
            }
            // 判断是否管理员进行的外呼
            if ("1".equals(userType)) {
                marketResourceLogDTO.setCallOwner(2);
            } else {
                marketResourceLogDTO.setCallOwner(1);
            }
            // 主叫失败
            marketResourceLogDTO.setStatus(1002);
            marketResourceService.insertLog(marketResourceLogDTO);
        }
        map.put("touchId", touchId);
        map.put("code", code);
        map.put("msg", message);
        return map;
    }

    /**
     * 查询企业外显号码
     *
     * @param custId
     * @return
     */
    public String selectCustCallBackApparentNumber(String custId, String apparentNumber) {
        CustomerPropertyDO callBackApparentNumber = customerDao.getProperty(custId, apparentNumber);
        if (callBackApparentNumber != null) {
            apparentNumber = callBackApparentNumber.getPropertyValue();
        }
        return apparentNumber;
    }

    /**
     * @description 获取企业营销资源公共方法
     * @author:duanliying
     * @method
     * @date: 2019/2/21 14:25
     */
    public JSONObject getCustomerMarketResource(String custId, String resourceId) throws Exception {
        LOG.info("根据resourceId查询企业配置信息参数是：" + "企业id是：" + custId + ",资源id是：" + resourceId);
        //根据resourceId查询企业配置信息
        CustomerPropertyDO customerProperty = customerDao.getProperty(custId, resourceId + "_config");
        if (customerProperty != null) {
            //获取企业配置信息
            String propertyValue = customerProperty.getPropertyValue();
            LOG.info("根据resourceId查询出企业配置信息是：" + propertyValue);
            if (StringUtil.isNotEmpty(propertyValue)) {
                //将json串转换未json对象获取里面的具体配置信息
                JSONObject jsonObject = JSON.parseObject(propertyValue);
                return jsonObject;
            }
        }
        LOG.warn("企业id是:" + custId + "未配置资源信息!");
        return null;
    }

    /**
     * @description 获取供应商营销资源公共方法
     * @author:duanliying
     * @method
     * @date: 2019/2/21 14:25
     */
    public JSONObject getSupplierMarketResource(int resourceId) throws Exception {
        LOG.info("资源id是：" + resourceId);
        ResourcePropertyEntity sourceProperty = sourceDao.getSourceProperty(resourceId);
        if (sourceProperty != null) {
            String propertyValue = sourceProperty.getPropertyValue();
            JSONObject jsonObject = JSON.parseObject(propertyValue);
            LOG.info("资源ID:" + resourceId + "资源配置信息是:" + jsonObject);
            return jsonObject;
        }
        LOG.warn("资源ID:" + resourceId + "未配置资源信息!");
        return null;
    }

    /**
     * 联通外呼接口
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/8 20:16
     */
    public Map<Object, Object> callCustomer(Map<String, Object> params, Long userId, String customerId, String userType) {
        Map<Object, Object> map = new HashMap<>();
        //触达Id
        String touchId = Long.toString(IDHelper.getTransactionId());
        String id = String.valueOf(params.get("id"));
        String batchId = String.valueOf(params.get("batchId"));
        if (StringUtil.isEmpty(id) || StringUtil.isEmpty(batchId)) {
            map.put("msg", "请求参数异常");
            map.put("code", 0);
            return map;
        }
        // 判断是余额是否充足
        boolean judge = marketResourceService.judRemainAmount(customerId);
        if (!judge) {
            map.put("msg", "余额不足");
            map.put("code", 1003);
            return map;
        }
        int code = 0;
        String message = "";
        BatchDetail batchDetail = batchDao.getBatchDetail(id, batchId);
        int resource = batchDetail.getResourceId();
        LOG.info("批次详情下的资源id是：" + resource);
        //根据资源id查询该资源使用的外呼资源
        int callResourceId = 0, xzResourceId = 0, cmcResourceId = 0;
        ResourcePropertyEntity callConfig = sourceDao.getResourceProperty(String.valueOf(resource), "call_config");
        if (callConfig != null) {
            callResourceId = NumberConvertUtil.parseInt(callConfig.getPropertyValue());
            LOG.info("查询到当前批次详情下外呼的资源id是:" + callResourceId);
        }
        //查询联通外呼的资源id
        MarketResourceEntity cmcCallResourceId = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType());
        if (cmcCallResourceId != null) {
            cmcResourceId = cmcCallResourceId.getResourceId();
            LOG.info("联通外呼的资源id是:" + cmcResourceId);
        }
        //查询讯众外呼的资源id
        MarketResourceEntity xzCallResourceId = sourceDao.getResourceId(SupplierEnum.XZ.getSupplierId(), ResourceEnum.CALL.getType());
        if (xzCallResourceId != null) {
            xzResourceId = xzCallResourceId.getResourceId();
            LOG.info("联通外呼的资源id是:" + xzResourceId);
        }
        //根据资源属性表存储的外呼资源id判断外呼方式
        if (callResourceId == cmcResourceId) {
            //判断是否设置了销售定价
            String resourceId = marketResourceService.queryResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType());
            //查询销售定价
            ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, customerId);
            if (resourcesPriceDto == null || StringUtil.isEmpty(resourcesPriceDto.getCallPrice())) {
                map.put("msg", "未设置销售定价");
                map.put("code", 1004);
                return map;
            }
            //使用联通外呼
            boolean success = false;
            // 唯一请求ID
            String callId = null, activityId = null, enterpriseId = null;
            Map<String, Object> callResult = null;
            try {
                LOG.info("调用外呼接口参数:" + "customerId" + customerId + "userId" + String.valueOf(userId) + "id:" + id + "batchId:" + batchId);
                callResult = seatMakeCallExV1(customerId, String.valueOf(userId), id, batchId, resourceId);
            } catch (Exception e) {
                LOG.error("调用外呼接口异常:", e);
            }

            if (callResult != null) {
                LOG.info("调用外呼返回数据:" + callResult.toString());
                callId = String.valueOf(callResult.get("uuid"));
                activityId = String.valueOf(callResult.get("activity_id"));
                enterpriseId = String.valueOf(callResult.get("enterprise_id"));
                // 成功
                if ("1".equals(String.valueOf(callResult.get("code")))) {
                    success = true;
                    code = 1;
                    message = "成功";
                } else {
                    success = false;
                    code = 0;
                    LOG.info("调用联通外呼失败,返回结果:" + callResult);
                    message = String.valueOf(callResult.get("msg"));
                    LOG.info("调用外呼失败,idCard:" + id + ", batchId:" + batchId);
                }
            }

            MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
            marketResourceLogDTO.setTouch_id(touchId);
            marketResourceLogDTO.setType_code("1");
            marketResourceLogDTO.setResname("voice");
            marketResourceLogDTO.setUser_id(userId);
            marketResourceLogDTO.setCust_id(customerId);
            marketResourceLogDTO.setSuperId(id);
            marketResourceLogDTO.setCallSid(callId);
            marketResourceLogDTO.setActivityId(activityId);
            marketResourceLogDTO.setBatchId(batchId);
            marketResourceLogDTO.setChannel(2);
            marketResourceLogDTO.setEnterpriseId(enterpriseId);
            if (StringUtil.isNotEmpty(resourceId)) {
                marketResourceLogDTO.setResourceId(Integer.parseInt(resourceId));
            }
            // 拼装备注字段: 备注{}操作人姓名{}企业名称
            String userName = customerService.getUserRealName(String.valueOf(userId));
            LOG.info("打电话获取到的userName:" + userName);

            String remark = "{}" + userName + "{}" + customerService.getEnterpriseName(customerId);

            marketResourceLogDTO.setRemark(remark);
            LOG.info("通话初次保存备注touchId:" + touchId + ",remark:" + remark);

            // 判断是否管理员进行的外呼
            if ("1".equals(userType)) {
                marketResourceLogDTO.setCallOwner(2);
            } else {
                marketResourceLogDTO.setCallOwner(1);
            }
            // 成功
            if (success) {
                marketResourceLogDTO.setStatus(1001);
                marketResourceService.insertLog(marketResourceLogDTO);

            } else {
                // 失败
                marketResourceLogDTO.setStatus(1002);
                marketResourceService.insertLog(marketResourceLogDTO);
            }
            map.put("touchId", touchId);
            map.put("code", code);
            map.put("msg", message);
        } else if (callResourceId == xzResourceId) {
            LOG.info("使用外呼方式是讯众外呼" + "客户id是：" + id + "批次id" + batchId);
            map = marketResourceService.xZCallResourceV1(userType, batchId, userId, id, customerId);
        }
        return map;
    }

    /**
     * 获取录音文件（前后台获取录音）
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/12 13:41
     */
    public void getVoiceFile(String userId, String fileName, HttpServletRequest request, HttpServletResponse response) {
        // 设置响应头
        response.addHeader("Accept-Ranges", "bytes");
        response.addHeader("Content-Type", "audio/mpeg;charset=UTF-8");
        String range = request.getHeader("Range");
        FileInputStream fis = null;
        ByteArrayInputStream hBaseInputStream = null;
        try {
            // 查询本地磁盘的录音文件
            String filePath = PropertiesUtil.getStringValue("audiolocation") + userId + File.separator + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                fis = new FileInputStream(file);
                if (StringUtil.isNotEmpty(range)) {
                    long length = file.length();
                    LOG.info("文件大小: " + length);
                    String[] rs = range.split("\\=");
                    range = rs[1].split("\\-")[0];
                    length -= Integer.parseInt(range);
                    response.addHeader("Content-Length", length + "");
                    response.addHeader("Content-Range", "bytes " + range + "-" + length + "/" + length);
                }
                IOUtils.copy(fis, response.getOutputStream());
            } else {
                LOG.warn(filePath + ",音频文件不存在,穿透查询一次录音文件");
                long length;
                // 通过HBase接口读取录音文件
                Map<String, Object> param = new HashMap<>();
                Map<String, Object> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                String result = HttpUtil.httpGet(PropertiesUtil.getStringValue("hbase_audio_url") + fileName + "/f1:file", param, headers);
                if (StringUtil.isNotEmpty(result)) {
                    LOG.info("开始解析HBase返回的录音文件,userId:" + userId + ",fileName:" + fileName);
                    String base64Str = null;
                    JSONObject jsonObject;
                    try {
                        jsonObject = JSON.parseObject(result);
                    } catch (JSONException e) {
                        LOG.error("解析HBase录音文件返回Json出错,userId:" + userId + ",fileName:" + fileName + ",返回结果:" + result, e);
                        return;
                    }
                    JSONArray row = jsonObject.getJSONArray("Row");
                    if (row != null && row.size() > 0) {
                        JSONObject rowData = row.getJSONObject(0);
                        JSONArray cell = rowData.getJSONArray("Cell");
                        if (cell != null && cell.size() > 0) {
                            base64Str = cell.getJSONObject(0).getString("$");
                        }
                    }
                    if (StringUtil.isNotEmpty(base64Str)) {
                        byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64Str);
                        hBaseInputStream = new ByteArrayInputStream(bytes);
                        length = hBaseInputStream.available();
                        response.addHeader("Content-Length", length + "");
                        response.addHeader("Content-Range", "bytes " + range + "-" + length + "/" + length);
                        IOUtils.copy(hBaseInputStream, response.getOutputStream());
                    } else {
                        LOG.warn("通过HBase读取录音文件base64字符串为空,userId:" + userId + ",fileName:" + fileName + ",base64Str:" + base64Str);
                    }
                } else {
                    LOG.warn("通过HBase读取录音文件返回为空,userId:" + userId + ",fileName:" + fileName + ",result:" + result);
                }
            }
        } catch (Exception e) {
            LOG.error("获取录音文件失败,", e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (hBaseInputStream != null) {
                    hBaseInputStream.close();
                }
            } catch (IOException e) {
                LOG.error("获取录音文件失败,", e);
            }
        }
    }

    /**
     * 下载获取录音文件（返回InputStream）
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/12 13:41
     */
    public InputStream getVoiceFileInputStream(String userId, String fileName) {
        // 设置响应头
        FileInputStream fis = null;
        ByteArrayInputStream hBaseInputStream = null;
        try {
            // 查询本地磁盘的录音文件
            String filePath = PropertiesUtil.getStringValue("audiolocation") + userId + File.separator + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                fis = new FileInputStream(file);
                return fis;
            } else {
                LOG.warn(filePath + ",音频文件不存在,穿透查询一次录音文件");
                long length;
                // 通过HBase接口读取录音文件
                Map<String, Object> param = new HashMap<>();
                Map<String, Object> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                String result = HttpUtil.httpGet(PropertiesUtil.getStringValue("hbase_audio_url") + fileName + "/f1:file", param, headers);
                if (StringUtil.isNotEmpty(result)) {
                    LOG.info("开始解析HBase返回的录音文件,userId:" + userId + ",fileName:" + fileName);
                    String base64Str = null;
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = JSON.parseObject(result);
                    } catch (JSONException e) {
                        LOG.error("解析HBase录音文件返回Json出错,userId:" + userId + ",fileName:" + fileName + ",返回结果:" + result, e);
                        return null;
                    }
                    JSONArray row = jsonObject.getJSONArray("Row");
                    if (row != null && row.size() > 0) {
                        JSONObject rowData = row.getJSONObject(0);
                        JSONArray cell = rowData.getJSONArray("Cell");
                        if (cell != null && cell.size() > 0) {
                            base64Str = cell.getJSONObject(0).getString("$");
                        }
                    }
                    if (StringUtil.isNotEmpty(base64Str)) {
                        byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64Str);
                        hBaseInputStream = new ByteArrayInputStream(bytes);
                        length = hBaseInputStream.available();
                        return hBaseInputStream;
                    } else {
                        LOG.warn("通过HBase读取录音文件base64字符串为空,userId:" + userId + ",fileName:" + fileName + ",base64Str:" + base64Str);
                    }
                } else {
                    LOG.warn("通过HBase读取录音文件返回为空,userId:" + userId + ",fileName:" + fileName + ",result:" + result);
                }
            }
        } catch (Exception e) {
            LOG.error("获取录音文件失败,", e);
        }
        return null;
    }

    /**
     * 根据资源类型查询资源和供应商信息
     *
     * @param type
     */
    public List<Map<String, Object>> getResourceInfoByType(String type ,  String supplierId) throws Exception {
        StringBuffer querySql = new StringBuffer("SELECT r.resname,r.resource_id,s.`name` supplierName,s.supplier_id,r.type_code ");
        querySql.append("FROM t_market_resource r LEFT JOIN t_supplier s ON r.supplier_id = s.supplier_id ");
        querySql.append("WHERE s.`status` = 1 AND r.`status` = 1 ");
        if (StringUtil.isNotEmpty(type)) {
            querySql.append("AND r.type_code =" + type);
        }
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(querySql.toString());
        if (StringUtil.isNotEmpty(supplierId)){
            //查询供应商关联的资源信息
            SupplierPropertyEntity resourceInfo = supplierDao.getSupplierProperty(supplierId, "express_resource");
            if (resourceInfo!=null){
                String resourceId = resourceInfo.getPropertyValue();
                LOG.info("供应商id是：" +supplierId +"资源类型是：" + type + "关联的资源id是：" +  resourceId);
                for (int i= 0 ;i<list.size();i++){
                    if (resourceId.equals(String.valueOf(list.get(i).get("resource_id")))){
                        list.get(i).put("check",1);
                    }
                }
            }
        }
        return list;
    }
}