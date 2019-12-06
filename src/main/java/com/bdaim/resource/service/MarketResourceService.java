package com.bdaim.resource.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.api.dao.ApiDao;
import com.bdaim.api.entity.ApiEntity;
import com.bdaim.api.entity.ApiProperty;
import com.bdaim.auth.LoginUser;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.batch.dao.BatchDao;
import com.bdaim.batch.dao.BatchDetailDao;
import com.bdaim.batch.dto.TouchInfoDTO;
import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.bill.dto.CallBackInfoParam;
import com.bdaim.callcenter.common.CallUtil;
import com.bdaim.callcenter.dto.*;
import com.bdaim.callcenter.service.impl.CallCenterService;
import com.bdaim.callcenter.service.impl.SeatsService;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
import com.bdaim.common.service.PhoneService;
import com.bdaim.customer.account.dao.TransactionDao;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerLabelDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dao.CustomerUserPropertyDao;
import com.bdaim.customer.dto.CustomerUserDTO;
import com.bdaim.customer.entity.*;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.dto.CustomerGrpOrdParam;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.marketproject.dao.MarketProjectDao;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.markettask.entity.MarketTask;
import com.bdaim.online.unicom.service.UnicomService;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.rbac.service.UserService;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.dto.*;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.resource.price.dto.ResourcesPriceDto;
import com.bdaim.smscenter.dto.SendSmsDTO;
import com.bdaim.smscenter.dto.SmsqueryParam;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.supplier.dto.SupplierEnum;
import com.bdaim.supplier.dto.SupplierListParam;
import com.bdaim.supplier.entity.SupplierEntity;
import com.bdaim.supplier.entity.SupplierPropertyEntity;
import com.bdaim.template.dao.MarketTemplateDao;
import com.bdaim.template.dto.MarketTemplateDTO;
import com.bdaim.template.dto.TemplateParam;
import com.bdaim.template.entity.MarketTemplate;
import com.bdaim.util.*;
import com.bdaim.util.ftp.SFTPChannel;
import com.bdaim.util.http.HttpUtil;
import com.github.crab2died.ExcelUtils;
import com.jcraft.jsch.ChannelSftp;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.*;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

import static com.bdaim.common.exception.BpExceptionHandler.log;

/**
 * @author yanls@bdaim.com
 * @Description: 营销资源service服务实现类
 * @date 2018/9/10 9:46
 */
@Service("marketResourceService")
@Transactional
public class MarketResourceService {


    private final static Logger LOG = LoggerFactory.getLogger(MarketResourceService.class);

    private final static String SMS_SEND_REMARK_SPLIT = "{}";
    //发送类型
    private final static String TYPE_CODE = "2";
    //状态
    private final static int SUCCESS = 1001;
    private final static int FAIL = 1002;
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
    private SeatsService seatsService;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private SendSmsService sendSmsServiceImpl;
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
    @Resource
    private UnicomService unicomService;
    @Autowired
    private ApiDao apiDao;


    public PageList querySmsHistory(PageParam page, SmsqueryParam smsqueryParm) {
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


    public PageList queryRecordVoicelog(PageParam page, String cust_id, Long userid, String user_type, String superId, String realName, String createTimeStart, String createTimeEnd, String enterpriseId, String batchId, int touchStatus, String enterpriseName) {
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
                    "insert  into t_touch_sms_log (touch_id,cust_id,user_id,remark,create_time,status,sms_content,superId, batch_id, activity_id, channel, enterprise_id,amount,prod_amount,resource_id,request_id,send_status,send_data) values ( ");
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
            sql.append("'" + dto.getRequestId() + "',");
            sql.append(dto.getSendStatus() + ",");
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
     * 坐席外呼
     */
    public Map<String, Object> seatMakeCallEx(String custId, String userId, String idCard, String batchId, String resourceId) {
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
     * 坐席外呼V1版本
     */
    public Map<String, Object> seatMakeCallExV1(String custId, String userId, String idCard, String batchId, String resourceId) {
        LOG.info("获取联通坐席参数:custId" + custId + "userId" + userId + "idCard" + idCard + "batchId" + batchId);
        Map<String, Object> data = new HashMap<>();
        int code = 0;
        String activityId = null, enterpriseId = null, apparentNumber = null, msg = "失败";
        LOG.info("联通外呼资源id是:" + resourceId);
        Map<String, Object> callCenterConfigData = callCenterService.getCallCenterConfigDataV1(custId, userId, resourceId);
        LOG.info("获取外呼配置信息是：" + String.valueOf(callCenterConfigData));
        if ((callCenterConfigData.get("mainNumber")) != null && (callCenterConfigData.get("apparentNumber")) != null) {
            String workNum = String.valueOf(callCenterConfigData.get("mainNumber"));
            LOG.info("坐席外呼使用的主叫号码是:" + workNum);
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
                String callCenterId = String.valueOf(callCenterConfigData.get("callCenterId"));
                String entPassWord = String.valueOf(callCenterConfigData.get("entPassWord"));
                //根据企业id查询密钥
                String key = "";
                CustomerProperty customerPro = customerDao.getProperty(custId, "key");
                if (customerPro != null) {
                    key = customerPro.getPropertyValue();
                }
                //调用联通外呼接口
                LOG.info("坐席外呼接收请求參數:entId是" + callCenterId + "数据id是：" + String.valueOf(batchDetail.get(0).get("id")) + "企业密码：" + entPassWord + "主叫号：" + workNum + "外显号码是： " + apparentNumber + "密钥：" + key);
                Map<String, Object> result = UnicomUtil.unicomSeatMakeCall(callCenterId, String.valueOf(batchDetail.get(0).get("id")), entPassWord, workNum, apparentNumber, key);
                LOG.info("调用外呼返回结果:" + result.toString());
                if (result != null && "01000".equals(String.valueOf(result.get("code")))) {
                    String returnData = String.valueOf(result.get("data"));
                    JSONObject jsonObject = JSON.parseObject(returnData);
                    if (jsonObject != null) {
                        result.put("callId", jsonObject.getString("callId"));
                    }
                    result.put("activity_id", activityId);
                    result.put("enterprise_id", enterpriseId);
                    result.put("code", 1);
                    return result;
                } else {
                    LOG.error("调用外呼失败,结果:" + result.toString());
                    code = 0;
                    msg = "呼叫失败";
                }
            } else {
                code = 0;
                msg = "读取失联人信息失败";
            }
        } else {
            code = 0;
            msg = "配置信息错误!";
        }
        data.put("code", code);
        data.put("msg", msg);
        data.put("activity_id", activityId);
        data.put("enterprise_id", enterpriseId);
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


    /**
     * 发送短信接口V1
     *
     * @return
     */
    public Map<String, Object> sendBatchSmsV1(String variables, String custId, String userId, int templateId, String batchId, String customerIds, int typeCode, int channel) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String resourceId = null, resourceIdCall = null;
        ResourcesPriceDto resourcesCallDto = null;
        int sendSuccessCount = 0;
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
        if (resourcesPriceDto == null) {
            map.put("msg", "短信资源未配置,无法发送短信");
            map.put("code", 0);
            return map;
        }
        if (StringUtil.isEmpty(resourcesPriceDto.getSmsPrice())) {
            map.put("msg", "未设置定价,无法发送短信");
            map.put("code", 0);
            return map;
        }
        //查询企业呼叫中心id，产品设置将企业外呼id放在外呼资源了，新接口需要使用，后期产品原型需要重新设计
        MarketResourceEntity callResourceEntity = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType());
        if (callResourceEntity != null) {
            resourceIdCall = String.valueOf(callResourceEntity.getResourceId());
            resourcesCallDto = customerDao.getCustResourceMessageById(resourceIdCall, custId);
            if (StringUtil.isEmpty(resourcesCallDto.getCallCenterId())) {
                map.put("msg", "未设置呼叫中心id,无法发送短信");
                map.put("code", 0);
                return map;
            }
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
        if (marketTemplate == null) {
            map.put("msg", "短信模板未配置,无法发送短信");
            map.put("code", 0);
            return map;
        }
        BatchDetail batchDetail;
        //发送短信参数类
        UnicomSendSmsParam unicomSendSmsParam = new UnicomSendSmsParam();
        String userName, batchName, templateName, custName;
        StringBuffer remark;
        //构造占位符
        List<String> variableList = null;
        if (variables != null && variables.contains(",")) {
            variableList = Arrays.asList(variables.split(","));
        }
        if (variableList != null) {
            unicomSendSmsParam.setVariableOne(variableList.size() > 0 ? variableList.get(0) : "");
            unicomSendSmsParam.setVariableTwo(variableList.size() > 1 ? variableList.get(1) : "");
            unicomSendSmsParam.setVariableThree(variableList.size() > 2 ? variableList.get(2) : "");
            unicomSendSmsParam.setVariableFour(variableList.size() > 3 ? variableList.get(3) : "");
            unicomSendSmsParam.setVariableFive(variableList.size() > 4 ? variableList.get(4) : "");
        }
        if (resourcesCallDto != null) {
            unicomSendSmsParam.setEntId(resourcesCallDto.getCallCenterId());
            // 由于系统没有地方配置企业密码 现在给默认值，后期页面需要加企业密码配置项
            String entPassWord = resourcesCallDto.getEntPassWord();
            if (StringUtil.isEmpty(unicomSendSmsParam.getEntPassWord())) {
                entPassWord = "111111";
            }
            unicomSendSmsParam.setEntPassWord(entPassWord);
        }
        CustomerProperty customerPro = customerDao.getProperty(custId, "key");
        //查询企业密钥
        if (customerPro != null) {
            unicomSendSmsParam.setKey(customerPro.getPropertyValue());
        }
        //联通模板id
        unicomSendSmsParam.setWordId(marketTemplate.getTemplateCode());
        String[] customerIdList = customerIds.split(",");
        for (String id : customerIdList) {
            batchDetail = batchDetailDao.getBatchDetail(id, batchId);
            if (batchDetail != null) {
                unicomSendSmsParam.setDataId(id);
                LOG.info("联通发送短信接口 请求参数是" + unicomSendSmsParam.toString());
                Map<String, Object> sendResult = UnicomUtil.unicomSeatMakeSms(unicomSendSmsParam);
                LOG.info("联通短信发送结果:" + sendResult);

                //设置短信实际发送状态为1000 是未处理的状态  需要状态推送后更新此字段  1001 发送成功 1002 发送失败
                marketResourceLogDTO.setStatus(1000);
                //短信提交给联通状态1001成功  1002失败
                int sendStatus = 1002;
                if (sendResult != null) {
                    if ("02000".equals(sendResult.get("code"))) {
                        sendSuccessCount++;
                        sendStatus = 1001;
                    } else {
                        sendStatus = 1002;
                        //如果短信提交失败直接将发送状态设置为失败
                        marketResourceLogDTO.setStatus(sendStatus);
                    }
                    //获取联通返回的contactId 短信流水号
                    JSONObject returnJson = JSON.parseObject(String.valueOf(sendResult.get("data")));
                    if (returnJson != null) {
                        marketResourceLogDTO.setRequestId(returnJson.getString("contactId"));
                    }
                }
                marketResourceLogDTO.setTouch_id(Long.toString(IDHelper.getTransactionId()));
                marketResourceLogDTO.setType_code("2");
                marketResourceLogDTO.setSendStatus(sendStatus);
                marketResourceLogDTO.setResname("sms");
                marketResourceLogDTO.setUser_id(NumberConvertUtil.parseLong(userId));
                marketResourceLogDTO.setCust_id(custId);
                marketResourceLogDTO.setSuperId(id);
                marketResourceLogDTO.setBatchId(batchId);
                marketResourceLogDTO.setChannel(channel);
                marketResourceLogDTO.setActivityId(batchDetail.getActivityId());
                marketResourceLogDTO.setEnterpriseId(batchDetail.getEnterpriseId());
                marketResourceLogDTO.setSms_content(marketTemplate.getMouldContent());
                marketResourceLogDTO.setCallBackData(sendResult.toString());
                marketResourceLogDTO.setAmount(0);
                marketResourceLogDTO.setProdAmount(0);
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
        } else {
            map.put("msg", "短信发送失败");
            map.put("code", 0);
        }
        return map;
    }

    public boolean judRemainAmount(String cust_id) {
        boolean judge = false;
        try {
            judge = true;
            CustomerProperty customerProperty = customerDao.getProperty(cust_id, "remain_amount");
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

    /**
     * 判断企业账户余额是否存在或大于0厘
     *
     * @param cust_id
     * @return
     */
    public boolean judRemainAmount0(String cust_id) {
        boolean judge = false;
        try {
            judge = true;
            CustomerProperty customerProperty = customerDao.getProperty(cust_id, "remain_amount");
            if (customerProperty == null) {
                judge = false;
            } else {
                if (StringUtil.isEmpty(customerProperty.getPropertyValue())) {
                    judge = false;
                } else {
                    double remain_amount = NumberConvertUtil.parseDouble(customerProperty.getPropertyValue());
                    // 标准价格1厘
                    if (remain_amount <= 0) {
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


    public PageList getSupplierList(PageParam page, SupplierListParam supplierListParam) {
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

    public Map<String, Object> countMarketDataBackend() {
        //企业有效率 折线统计图
        String effectiveRateSql = "SELECT x.comp_name AS companyName,x.batch_id, SUM(CASE x.STATUS WHEN '1' THEN 1 ELSE 0 END)/SUM(CASE x.STATUS WHEN '0' THEN 1 ELSE 1 END) AS effectiveRate \n" +
                "FROM (SELECT t1.comp_name,t2.batch_id,t2.`status` FROM\tnl_batch t1\tLEFT JOIN nl_batch_detail t2 ON t1.id = t2.batch_id \n" +
                "GROUP BY t2.batch_id,t2.label_five ORDER BY t1.upload_time DESC ) x GROUP BY x.comp_name,x.batch_id \n" +
                "ORDER BY x.batch_id DESC LIMIT 10";
        List<Map<String, Object>> effectiveRate = jdbcTemplate.queryForList(effectiveRateSql);

        //企业签收率 折现统计图
        StringBuffer receiveRate = new StringBuffer("SELECT t1.id,t1.batch_name,t1.comp_name AS companyName,");
        //receiveRate.append("ROUND(SUM( CASE t3.`status` WHEN '4' THEN 1 ELSE 0 END ) / SUM( CASE t3.`status` WHEN '1' THEN 0 ELSE 1 END ),2) AS receiveRate  ")
        receiveRate.append("IFNULL(ROUND(SUM( CASE t3.`status` WHEN '4' THEN 1 ELSE 0 END ) / SUM( CASE t3.`status` WHEN '1' THEN 0 ELSE 1 END ),2) ,0) AS receiveRate  ")
                .append(" FROM nl_batch t1").append(" LEFT JOIN nl_batch_detail t2 ON t1.id = t2.batch_id")
                .append(" LEFT JOIN t_touch_express_log t3 ON t2.touch_id = t3.touch_id ")
                .append("GROUP BY t1.id, t1.batch_name,t1.comp_name  ORDER BY t1.upload_time DESC LIMIT 10");
        List<Map<String, Object>> receiveRateList = jdbcTemplate.queryForList(receiveRate.toString());

        //客户有效数据趋势图
        StringBuffer effectiveNum = new StringBuffer("SELECT t1.upload_time,SUM(CASE t1.effective_num WHEN '0' THEN 0 ELSE 1 END) AS effective_num");
        effectiveNum.append(" FROM (SELECT DATE_FORMAT(upload_time,'%Y-%m-%d') AS upload_time,batch_id,SUM(CASE `status` WHEN '1' THEN 1 ELSE 0 END) AS effective_num ")
                .append("FROM nl_batch_detail GROUP BY batch_id,label_five ").append("ORDER BY upload_time DESC) t1 GROUP BY t1.upload_time")
                .append(" ORDER BY t1.upload_time DESC LIMIT 10");
        List<Map<String, Object>> effectiveNumMap = jdbcTemplate.queryForList(effectiveNum.toString());
        Map<String, Object> data = new HashMap<>(16);
        data.put("effectiveRate", effectiveRate);
        data.put("receiveRate", receiveRateList);
        data.put("effectiveNum", effectiveNumMap);
        return data;
    }


    public Map<String, Object> countMarketData(String customerId) {

        Map<String, Object> data = new HashMap<>();
        //customerId 为空，则是后台首页各个企业的信息
        if (StringUtil.isEmpty(customerId)) {
            // 1 各个企业最近一周上传的客户量
            long time1 = System.currentTimeMillis();
            StringBuffer sqlSb = new StringBuffer();
            List<Map<String, Object>> uploadNumList;
            //todo 没数据  6--->16了
            sqlSb.append(" SELECT s.upNum,s.comp_id,s.upload_time,t.enterprise_name from \n" +
                    "(SELECT sum(upload_num)as upNum,comp_id,upload_time,id,upload_num  from nl_batch\n" +
                    "WHERE DATE_SUB(CURDATE(), INTERVAL 6 DAY) <= date(upload_time) GROUP BY date(upload_time))s \n" +
                    "LEFT JOIN t_customer t on s.comp_id=t.cust_id;");
            uploadNumList = jdbcTemplate.queryForList(sqlSb.toString(), new Object[]{});
            long time2 = System.currentTimeMillis();
            LOG.info("上传客户量 查询耗时 " + (time2 - time1) + " 毫秒");
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
            long time3 = System.currentTimeMillis();
            LOG.info("企业修复率 查询耗时 " + (time3 - time2) + " 毫秒");

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
            long time4 = System.currentTimeMillis();
            LOG.info("企业接通率 查询耗时 " + (time4 - time3) + " 毫秒");
            data.put("callSuccPercent", jietonglv);
            data.put("uploadSucCompsList", uploadgeList);
            data.put("uploadNumList", uploadNumList);
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
//            StringBuffer checkSql = new StringBuffer("SELECT batch_name AS batchName,IFNULL(upload_num,0) AS uploadNum,IFNULL(success_num,0) AS successNum,");
//            checkSql.append("IFNULL(success_num/upload_num,0) AS effectiveRate FROM nl_batch WHERE comp_id='").append(customerId).append("' ORDER BY ")
//                    .append("upload_time DESC LIMIT 10");
            StringBuffer checkSql = new StringBuffer("SELECT x.batch_id batchId,x.batchName,SUM(CASE x.upload_num WHEN '1' THEN 1 ELSE 1 END) AS uploadNum,");
            checkSql.append("SUM(CASE x.success_num WHEN '0' THEN 0 ELSE 1 END) successNum,")
                    .append("SUM(CASE x.success_num WHEN '0' THEN 0 ELSE 1 END)/SUM(CASE x.upload_num WHEN '1' THEN 1 ELSE 1 END) AS effectiveRate ")
                    .append("FROM (SELECT t1.id AS batch_id,t1.upload_time,t1.batch_name AS batchName,")
                    .append("SUM(CASE t2.STATUS WHEN '0' THEN 1 ELSE 1 END) AS upload_num,SUM(CASE t2.STATUS WHEN '1' THEN 1 ELSE 0 END) AS success_num ")
                    .append("FROM nl_batch t1 LEFT JOIN nl_batch_detail t2 ON t1.id = t2.batch_id WHERE t1.comp_id='").append(customerId).append("' ")
                    .append("GROUP BY t1.batch_name,t1.id,t2.label_five ORDER BY t1.upload_time DESC ) x ")
                    .append(" GROUP BY x.batch_id,x.batchName ORDER BY x.upload_time DESC LIMIT 10");
            List<Map<String, Object>> checkStatistics = jdbcTemplate.queryForList(checkSql.toString());
            checkStatistics.stream().map(e -> e.put("effectiveRate", new BigDecimal(String.valueOf(e.get("effectiveRate")))
                    .setScale(2, BigDecimal.ROUND_HALF_UP))).collect(Collectors.toList());
            //前端首页 签收统计图
            StringBuffer signAndReceive = new StringBuffer("SELECT t1.id,t1.batch_name,SUM(CASE t2.`label_seven` WHEN '4' THEN 1 ELSE 0 END) AS sendVal,");
            signAndReceive.append("SUM(CASE t3.`status` WHEN '4' THEN 1 ELSE 0 END) AS receiveVal,")
                    .append("SUM(CASE t3.`status` WHEN '2' THEN 1 WHEN '3' THEN 1 ELSE 0 END) AS sendingVal,")
                    .append("SUM(CASE t3.`status` WHEN '5' THEN 1 ELSE 0 END) AS rejectionVal ")
                    .append("FROM nl_batch t1 ")
                    .append("LEFT JOIN nl_batch_detail t2 ON t1.id=t2.batch_id ")
                    .append("LEFT JOIN t_touch_express_log t3 ON t2.touch_id=t3.touch_id ")
                    .append("WHERE t1.comp_id='").append(customerId).append("' ")
                    .append(" GROUP BY t1.id,t1.batch_name ")
                    .append("ORDER BY t1.upload_time DESC LIMIT 10");
            List<Map<String, Object>> signAndReceiveStatistic = jdbcTemplate.queryForList(signAndReceive.toString());

            data.put("checkStatistics", checkStatistics);
            data.put("signAndReceive", signAndReceiveStatistic);
            data.put("siteList", siteList);
            data.put("sevenList", sevenList);
            data.put("qiyehujiao", qiyehujiaoall);
        }

        return data;
    }


    public PageList getSmsTemplateList(PageParam page, TemplateParam templateParam) {
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


    public List<Map<String, Object>> soundUrl(RecordVoiceQueryParam recordVoiceQueryParam) {
        StringBuffer sb = new StringBuffer();
        //user_type 1.管理员   2.普通用户   superId为用户id(联通返回的)及customerId
        sb.append("  SELECT voicLog.user_id,substring_index(backInfo.recordurl, '/', - 1) AS recordurl,voicLog.touch_id\n" +
                "FROM t_touch_voice_log voicLog\n" +
                "LEFT JOIN t_callback_info backInfo ON voicLog.callSid = backInfo.callSid\n" +
                "WHERE 1 = 1 ");
        if (StringUtil.isNotEmpty(recordVoiceQueryParam.getTouchId())) {
            sb.append(" AND  voicLog.touch_id= '" + recordVoiceQueryParam.getTouchId() + "'");
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
        CustomerProperty cucSaleSmsPrice = customerDao.getProperty(custId, "cuc_sms_price");
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
            String propertyValue = customerMarketResource.getString(ResourceEnum.CALL.getApparentNumber());
//            String propertyValue = null;
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
            CustomerUser account = customerUserDao.getCustomer(seatAccount, custId);
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
                    sendSmsDTO.setEntId(smsConfigData.getString(ResourceEnum.SMS.getCallCenterId()));
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
                    sendResult = sendSmsServiceImpl.sendSmsService(sendSmsDTO, customerId, batchId);
                    if (StringUtil.isNotEmpty(sendResult)) {
                        result = JSONObject.parseObject(sendResult);
                        marketResourceLogDTO.setRemark(result.getString("msg"));
                        map.put("code", result.get("code"));
                        map.put("touchId", touchId);
                    }
                    marketResourceLogDTO.setActivityId(batchDetail.getActivityId());
                    marketResourceLogDTO.setEnterpriseId(batchDetail.getEnterpriseId());
                    marketResourceLogDTO.setCallBackData(sendResult);

//                     发送成功
                    if (StringUtil.isNotEmpty(sendResult) && "000".equals(JSON.parseObject(sendResult, Map.class).get("code"))) {
                        sendSuccessCount++;
                        marketResourceLogDTO.setRemark("发送成功");
                        marketResourceLogDTO.setStatus(1001);
                        // 账户余额扣款
                        if (smsConfigData != null) {
                            //获取短信的销售定价
                            //             custSmsPrice = NumberConvertUtil.transformtionCent(smsConfigData.getDoubleValue(ResourceEnum.SMS.getPrice()));
                        }
                        custSmsAmount = new BigDecimal(custSmsPrice);
                        LOG.info("短信扣费客户:" + custId + ",开始扣费,金额（分）:" + custSmsAmount.doubleValue());
                        accountDeductionStatus = customerDao.accountDeductions(custId, custSmsAmount);
                        LOG.info("短信扣费客户:" + custId + ",扣费状态:" + accountDeductionStatus);

                        // 供应商余额扣款
                        JSONObject supplierMarketResource = null;
                        if (StringUtil.isNotEmpty(resourceId)) {
                            supplierMarketResource = getSupplierMarketResource(Integer.parseInt(resourceId));
                        }
                        if (supplierMarketResource != null) {
                            //获取供应商短信价格
                            //                supSmsPrice = NumberConvertUtil.transformtionCent(supplierMarketResource.getDoubleValue(ResourceEnum.SMS.getPrice()));
                        }
                        // 供应商余额扣款
                        LOG.info("短信扣费供应商:" + custId + "开始短信扣费,金额(分):" + supSmsPrice);
                        //供应商扣费需要转换为分进行扣减
                        sourceSmsAmount = new BigDecimal(supSmsPrice);
                        LOG.info("短信扣费供应商:" + custId + "开始短信扣费,金额:" + sourceSmsAmount);
                        sourceSmsDeductionStatus = sourceDao.supplierAccountDuctions(SupplierEnum.CUC.getSupplierId(), sourceSmsAmount);
                        LOG.info("短信扣费供应商:" + custId + "短信扣费状态:" + sourceSmsDeductionStatus);
                        //短信log表，添加企业和供应商扣减金额
                        marketResourceLogDTO.setAmount(custSmsPrice);
                        marketResourceLogDTO.setProdAmount(supSmsPrice);
                    } else {
                        marketResourceLogDTO.setRemark(result.getString("msg"));
                        marketResourceLogDTO.setStatus(1002);
                    }
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

    public PageList openSmsHistory(PageParam page, String custId) {
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
     * @description 话单推送信息存储
     * @author:duanliying
     * @method
     * @date: 2018/12/6 10:26
     */
    public int callBackInfoV1(CallBackInfoParam callBackInfoParam) throws Exception {
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
        String callSid = callBackInfoParam.getUuid();
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
                        String callPrice = resourcesPriceDto.getCallPrice();
                        //元转分
                        saleCustPrice = NumberConvertUtil.transformtionCent(Double.parseDouble(callPrice));
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
                                LOG.info("联通坐席扣费开始从账户customerId:" + custId + "余额扣款,sale_price:" + summAmount);
                                transactionDao.accountDeductionsDev(custId, new BigDecimal(summAmount));
                            }
                        } else {
                            //不扣分钟数   直接扣除费用
                            LOG.info("坐席执行只扣除通话计费:" + userId + "通话时长:" + callTime);
                            summAmount = saleCustPrice * callTime;
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
                //String recordUrl = callBackInfoParam.getRecordUrl();
                if (StringUtil.isNotEmpty(startTime)) {
                    startTime = startTime.replaceAll("-", "/");
                }
                if (StringUtil.isNotEmpty(endTime)) {
                    endTime = endTime.replaceAll("-", "/");
                }
                flag = transactionDao.executeUpdateSQL(insertCallbackInfoSql, new Object[]{callSid, appId, 1,
                        callBackInfoParam.getLocalUrl(),
                        startTime, endTime,
                        callDuration, null, null,
                        callBackInfoParam.getRemoteUrl(),
                        startTime, endTime,
                        callDuration, null, null,
                        null,
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


    /**
     * 联通录音文件推送V1
     *
     * @param
     */

    public String getUnicomRecordfileV1(JSONObject param) throws Exception {
        LOG.info("开始获取联通录音文件callSid是" + param.getString("uuid"));
        LOG.info("联通推送录音接口参数" + param.toString());
        String callSid = param.getString("uuid");
        String code = "1";
        String queryTouchSql = "SELECT touch_id touchId ,cust_id custId,user_id userId,superid superId ,batch_id batchId FROM t_touch_voice_log WHERE callSid=?";
        //根据callSid 查询是否存在通过话记录
        List<Map<String, Object>> logList = marketResourceDao.sqlQuery(queryTouchSql, callSid);
        if (logList.size() > 0) {
            String querySql = "SELECT recordurl from t_callback_info WHERE callSid = ? AND recordurl is not null";
           /* //将录音地址保存到录音拉取对接表中sql
            String insertSql = "INSERT INTO t_record_queue (callSid,recordUrl) VALUES(?,?)";
            String querySql = "SELECT * FROM t_record_queue WHERE callSid = ?";*/
            //获取录音文件地址
            String recordUrl = param.getString("recordUrl");
            LOG.info("录音文件地址:" + recordUrl);
            if (StringUtil.isNotEmpty(recordUrl) && recordUrl.contains("htt")) {
              /*  //查询当前录音队列表中是否存在根据callSid，不存在直接插入   存在返回code=0，联通不在推送
                List<Map<String, Object>> maps = marketResourceDao.sqlQuery(querySql, callSid);
                LOG.info("查询当前录音队列表中是否存在sql:" + querySql);*/
                List<Map<String, Object>> maps = marketResourceDao.sqlQuery(querySql, callSid);
                LOG.info("查询当前录音队列表中是否存在sql:" + querySql);
                if (maps != null && maps.size() > 0) {
                    //说明已经存在返回code=0，联通不进行再次推送
                    LOG.info("callSid是:" + callSid + "已经存在");
                    code = "0";
                } else {
                    //保存录音文件
                    String voiceFilePath = FileUtil.savePhoneRecordFileReturnPath(recordUrl, String.valueOf(logList.get(0).get("user_id")));
                    if (StringUtil.isNotEmpty(voiceFilePath)) {
                        LOG.info("开始进行录音文件转换:" + voiceFilePath);
                        try {
                            // 文件转换
                            FileUtil.wavToMp3(voiceFilePath, voiceFilePath.replaceAll(".wav", ".mp3"));
                        } catch (Exception e) {
                            LOG.error("录音文件转换失败:", e);
                        }
                    }
                    //更改回调表录音地址
                    String updateUrlSql = "UPDATE t_callback_info SET recordurl = ? WHERE callSid = ?";
                    int i = marketResourceDao.executeUpdateSQL(updateUrlSql, recordUrl, callSid);
                    LOG.info("更新回调表数量:" + i);
                    if (i > 0) {
                        code = "0";
                    }
                }
            }
        } else {
            LOG.info("未查询到该条记录回调数据是:" + param);
        }
        return code;
    }


    /**
     * 联通短信状态推送V1
     *
     * @param
     */

    public String getUnicomSmsStatusV1(JSONObject param) {
        //String smsData = "{\"contactId\": \"0629171330100000000083\",\"isContactSuccess\": \"1 \",  \"contactCode\": \"0\",  \"contactDate\": \"1561989012030\",”cont”:”2354”}";
        LOG.info("开始获取联通短信记录的request_id是" + param.getString("contactId"));
        LOG.info("联通推送短信状态接口参数" + param.toString());
        try {
            String requestId = param.getString("contactId");
            //isContactSuccess 是否发送成功: 0:失败 1:成功
            String sendStatus = param.getString("isContactSuccess");
            String code = "1";
            String queryTouchSql = "SELECT touch_id touchId ,resource_id resourceId,cust_id custId,user_id userId,superid superId ,batch_id batchId FROM t_touch_sms_log WHERE request_id=? and status = 1000";
            //根据callSid 查询是否存在短信记录
            int status = 1002;
            List<Map<String, Object>> logList = marketResourceDao.sqlQuery(queryTouchSql, requestId);
            BigDecimal sourceSmsAmount = new BigDecimal(0), custSmsAmount = new BigDecimal(0);
            if (logList.size() > 0) {
                LOG.info("短信发送状态是：" + sendStatus + "唯一id是：" + requestId);
                if ("1".equals(sendStatus)) {
                    String resourceId = String.valueOf(logList.get(0).get("resourceId"));
                    String custId = String.valueOf(logList.get(0).get("custId"));
                    //短信发送成功进行扣费
                    status = SUCCESS;
                    //发送成功后进行企业和供应商进行扣费
                    ResourcesPriceDto resourcesPriceDto = customerDao.getCustResourceMessageById(resourceId, custId);
                    String custSmsPrice = resourcesPriceDto.getSmsPrice();
                    if (StringUtil.isNotEmpty(custSmsPrice)) {
                        custSmsAmount = new BigDecimal(custSmsPrice).multiply(new BigDecimal(100));
                    }
                    LOG.info("短信扣费客户:" + custId + ",开始扣费,金额:" + custSmsAmount);
                    boolean accountDeductionStatus = customerDao.accountDeductions(custId, custSmsAmount);
                    LOG.info("短信扣费客户:" + custId + ",扣费状态:" + accountDeductionStatus + "扣费金额是：" + custSmsAmount);

                    //获取供应商短信价格
                    String supSmsPrice = "0";
                    if (StringUtil.isNotEmpty(resourceId)) {
                        ResourcesPriceDto supResourceMessageById = supplierDao.getSupResourceMessageById(Integer.parseInt(resourceId), null);
                        if (supResourceMessageById != null) {
                            supSmsPrice = supResourceMessageById.getSmsPrice();
                        }
                    }
                    //供应商扣费需要转换为分进行扣减
                    if (StringUtil.isNotEmpty(supSmsPrice)) {
                        sourceSmsAmount = new BigDecimal(supSmsPrice).multiply(new BigDecimal(100));
                        Boolean sourceSmsDeductionStatus = sourceDao.supplierAccountDuctions(SupplierEnum.CUC.getSupplierId(), sourceSmsAmount);
                        LOG.info("短信扣费供应商:" + custId + "短信扣费状态:" + sourceSmsDeductionStatus + "扣费金额是：" + sourceSmsAmount);
                    }
                }
                String updateSql = "UPDATE t_touch_sms_log SET amount=?,prod_amount=?,`status` =?,send_data = ? WHERE request_id = ?";
                //更改短信状态记录
                LOG.info("更改批次状态唯一id是：" + requestId + "发送状态是：" + status + "客户消费金额是：" + custSmsAmount.intValue() + "供应商成本价是：" + sourceSmsAmount.intValue());
                int updateNum = marketResourceDao.executeUpdateSQL(updateSql, custSmsAmount.intValue(), sourceSmsAmount.intValue(), status, param.toJSONString(), requestId);
                if (updateNum > 0) {
                    code = "0";
                }
            } else {
                LOG.info("短信推送唯一标识是" + requestId + "的短信记录不存在");
            }
            return code;
        } catch (Exception e) {
            return null;
        }
    }


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
            backCallParam.setAppid(SaleApiUtil.NOLOSE_CALL_BACK_APP_ID);

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
            backCallParam.setAppid(SaleApiUtil.NOLOSE_CALL_BACK_APP_ID);

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
        CustomerProperty callBackApparentNumber = customerDao.getProperty(custId, apparentNumber);
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
        CustomerProperty customerProperty = customerDao.getProperty(custId, resourceId + "_config");
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
        //根据资源id查询该资源使用的外呼资源(sql入库)
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
                callId = String.valueOf(callResult.get("callId"));
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
    public void getNoloseVoiceFile(String userId, String fileName, HttpServletRequest request, HttpServletResponse response) {
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

//                String result = HttpUtil.httpGet("http://ds4:1111/voice/" + fileName + "/f1:file", param, headers);
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
    public List<Map<String, Object>> getResourceInfoByType(String type, String supplierId) throws Exception {
        StringBuffer querySql = new StringBuffer("SELECT r.resname,r.resource_id,s.`name` supplierName,s.supplier_id,r.type_code ");
        querySql.append("FROM t_market_resource r LEFT JOIN t_supplier s ON r.supplier_id = s.supplier_id ");
        querySql.append("WHERE s.`status` = 1 AND r.`status` = 1 ");
        if (StringUtil.isNotEmpty(type)) {
            querySql.append("AND r.type_code =" + type);
        }
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(querySql.toString());
        if (StringUtil.isNotEmpty(supplierId)) {
            //查询供应商关联的资源信息
            SupplierPropertyEntity resourceInfo = supplierDao.getSupplierProperty(supplierId, "express_resource");
            if (resourceInfo != null) {
                String resourceId = resourceInfo.getPropertyValue();
                LOG.info("供应商id是：" + supplierId + "资源类型是：" + type + "关联的资源id是：" + resourceId);
                for (int i = 0; i < list.size(); i++) {
                    if (resourceId.equals(String.valueOf(list.get(i).get("resource_id")))) {
                        list.get(i).put("check", 1);
                    }
                }
            }
        }
        return list;
    }

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    private final static DateTimeFormatter DFT = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");
    private final static DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    @Resource
    private UserDao userDao;
    @Resource
    private CustomGroupDao customGroupDao;
    @Resource
    private CustomGroupService customGroupService;
    @Resource
    private SendSmsService sendSmsService;
    @Resource
    private PhoneService phoneService;
    @Resource
    private MarketTemplateDao marketTemplateDao;
    @Resource
    private MarketTaskDao marketTaskDao;
    @Resource
    private MarketProjectDao marketProjectDao;
    @Resource
    private CustomerLabelDao customerLabelDao;


    public List<Map<String, Object>> queryMarketResource(String cust_id) {
        // Object[] objs = { cust_id };
        String sql = "select tt.type_code type,tt.resname name, t.remain quantity from t_resource_account t left join t_market_resource tt on t.resource_id=tt.resource_id  where cust_id=?";
        List<Map<String, Object>> groupConfigList = marketResourceDao.queryMarketResource(cust_id, sql);
        return groupConfigList;
    }

    @SuppressWarnings("unchecked")

    public List<Map<String, Object>> queryMarketResourceDetail(String groupId, String userid, String superId,
                                                               String pageNum, String pageSize) {
        StringBuffer hql = new StringBuffer();
        hql.append("from TCustomerGroupList where customergroupid=? and userid=? ");
        if (superId != null && !"".equals(superId)) {
            hql.append("and id=?");
            return marketResourceDao
                    .createQuery(hql.toString(), Integer.parseInt(groupId), Long.parseLong(userid), superId)
                    .setFirstResult(Integer.parseInt(pageNum)).setMaxResults(Integer.parseInt(pageSize)).list();

        }
        return marketResourceDao.createQuery(hql.toString(), Integer.parseInt(groupId), Long.parseLong(userid))
                .setFirstResult(Integer.parseInt(pageNum)).setMaxResults(Integer.parseInt(pageSize)).list();

    }

    public void insertLogV3(MarketResourceLogDTO dto) {
        String type_code = dto.getType_code();
        StringBuffer sql = new StringBuffer();
        if (type_code.equals("1")) {
            String nowYearMonth = DateUtil.getNowMonthToYYYYMM();
            // 检查通话记录月表是否存在
            marketResourceDao.createVoiceLogTableNotExist(nowYearMonth);

            sql.setLength(0);
            sql.append(
                    "insert  into " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowYearMonth + " (touch_id,cust_id,user_id,remark,create_time,status,superId,callSid,customer_group_id, cug_id, market_task_id, customer_sea_id) values ( ");
            sql.append("'" + dto.getTouch_id() + "',");
            sql.append("'" + dto.getCust_id() + "',");
            sql.append("'" + dto.getUser_id() + "',");
            sql.append("'" + dto.getRemark() + "',");
            sql.append("now(),");
            sql.append("'" + dto.getStatus() + "',");
            sql.append("'" + dto.getSuperId() + "',");
            sql.append("'" + dto.getCallSid() + "',");
            sql.append("'" + dto.getCustomerGroupId() + "',");
            sql.append("'" + dto.getCugId() + "',");
            sql.append("'" + dto.getMarketTaskId() + "',");
            sql.append("'" + dto.getCustomerSeaId() + "')");
        }
        if (type_code.equals("2")) {
            sql.append(
                    "insert  into t_touch_sms_log (cust_id,user_id,remark,create_time,status,sms_content,superId) values ( ");
            // sql.append("'"+dto.getTouch_id()+"',");
            sql.append("'" + dto.getCust_id() + "',");
            sql.append("'" + dto.getUser_id() + "',");
            sql.append("'" + dto.getRemark() + "',");
            sql.append("now(),");
            sql.append("'" + dto.getStatus() + "',");
            sql.append("'" + dto.getSms_content() + "',");
            sql.append("'" + dto.getSuperId() + "')");
        }
        if (type_code.equals("3")) {
            sql.append(
                    "insert  into t_touch_email_log (cust_id,user_id,remark,create_time,status,email_content,superId,templateId,batch_number) values ( ");
            // sql.append("'"+dto.getTouch_id()+"',");
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
        LOG.info("inserLog：SQL------->" + sql.toString());
        marketResourceDao.insertLog(sql.toString());
    }


    /**
     * 购买资源包
     */
    public Map<String, String> buyMarketResource(String resourceId, String typeCode, int num, String userId, String custId, String pay_type, String pay_password, String enterpriseName, String third_party_num) {
        Map<String, String> map = new HashMap<String, String>();
        // 判断密码是否正确
        try {
            CustomerProperty pp = customerDao.getProperty(custId, "pay_password"); //支付密码

            if (pp == null || "".equals(pp.getPropertyValue())) {
                map.put("code", "1");
                map.put("message", "未设密码！");
                return map;
            }
            if (!pp.getPropertyValue().equals(CipherUtil.generatePassword(pay_password))) {
                map.put("code", "2");
                map.put("message", "支付密码错误！");
                return map;
            }
        } catch (Exception e) {
            LOG.error("支付失败！" + e.getMessage());
        }

        // 购买营销资源
        // 根据资源信息计算所需要的money
        String moneySql = "SELECT resname,sale_price,cost_price,supplier_id FROM t_market_resource where  resource_id='" + resourceId + "' and type_code='" + typeCode + "' ";
        List<Map<String, Object>> groupConfigList = this.marketResourceDao.sqlQuery(moneySql);
        String resname = groupConfigList.get(0).get("resname").toString();
        Double sale_price = Double.parseDouble(groupConfigList.get(0).get("sale_price").toString());
        Double cost_price = Double.parseDouble(groupConfigList.get(0).get("cost_price").toString());
        int supplier_id = Integer.parseInt(groupConfigList.get(0).get("supplier_id").toString());
        BigDecimal money = new BigDecimal(sale_price);
        money = money.multiply(new BigDecimal(num));
        // 账户扣钱（余额支付）查询账户余额
        CustomerProperty ra = customerDao.getProperty(custId, "remain_amount");

        Double remain_amount = 0.0;
        try {
            if (ra != null)
                remain_amount = Double.parseDouble(ra.getPropertyValue());
        } catch (Exception e) {
            LOG.error("get balance error", e);
        }
        DecimalFormat df = new DecimalFormat("0.00");
        BigDecimal amount = new BigDecimal(remain_amount);
        if (amount.subtract(money).compareTo(new BigDecimal(0)) < 0) {
            map.put("code", "3");
            map.put("message", "余额不足支付！");
            return map;
        }
        String now_money = df.format(amount.subtract(money));
        // 更新账户信息（余额扣款）
        ra.setPropertyValue(now_money);
        try {
            customerDao.saveOrUpdate(ra);
        } catch (Exception e) {
            map.put("code", "4");
            map.put("message", "支付失败！");
            return map;
        }

        // 先判断以前是否买过资源（资源使用量表 是否存在）
        String sql = "SELECT remain from t_resource_account where cust_id=?  AND resource_id=? ";
        int count = marketResourceDao.queryResource(sql, custId, resourceId);
        // 查询供应商信息
        String supplierSql = "SELECT name,type from t_supplier where supplier_id='" + supplier_id + "'  ";
        List<Map<String, Object>> groupSupplierList = this.marketResourceDao.sqlQuery(supplierSql);
        String name = groupSupplierList.get(0).get("name").toString();
        String type = groupSupplierList.get(0).get("type").toString();
        // 根据返回值判断是插入或者是更新数据
        if (count == 0) {
            String insertSql = "insert  into  t_resource_account (resource_id,name,used,remain,source,cust_id,acct_id,create_time) values (?,?,0,?,?,?,?,now()) ";
            marketResourceDao.executeUpdateSQL(insertSql, resourceId, resname, num, supplier_id, custId, custId);
        } else {
            String udapteSql = " update  t_resource_account set  remain=? where cust_id=?  AND resource_id=?  ";
            marketResourceDao.executeUpdateSQL(udapteSql, count, num, custId, resourceId);
        }
        // 生成订单
        Long orderNo = IDHelper.getTransactionId();
        String orderSql = "insert into t_order set order_id=?,cust_id=?,order_type=2,pay_type=?,create_time=now(),pay_time=now(),quantity=?,amount=?,order_state=2,enpterprise_name=?,supplier_id=? ";
        marketResourceDao.executeUpdateSQL(orderSql, orderNo, custId, pay_type, num, sale_price, enterpriseName, supplier_id);
        // 记录 t_resource_order
        // 资源类型需要确定一下----------------------------->
        String insertResource_orderSql = "INSERT into  t_resource_order SET order_id=?,resource_id=?,NAME=?,quantity=?,sale_price=?,cost_price=?,create_time=NOW(),res_type=?,source_id=?,source_name=?";
        marketResourceDao.executeUpdateSQL(insertResource_orderSql, orderNo, resourceId, resname, num, sale_price, cost_price, type, groupConfigList.get(0).get("supplier_id"), name);
        /// 可能还有其他需要的表
        // t_transaction
        // dto.getTransaction_code() '交易类型（1.充值 2.扣减 3.消费）'
        // dto.getPay_type() 支付类型（1.余额 2.第三方 3.线下）'
        Long transaction_id = IDHelper.getTransactionId();
        String transactionSql = "insert into t_transaction set transaction_id=?,acct_id=?,cust_id=?,type=2,pay_mode=1,third_party_num=?,amount=?,order_id=?,create_time=now(),remark='余额支付'";
        marketResourceDao.executeUpdateSQL(transactionSql, transaction_id, userId, custId, third_party_num, amount, orderNo);
        // 购买成功
        map.put("code", "0");
        map.put("message", "支付成功！");
        return map;
    }

    /**
     * 创建短信邮件模板
     */

    public Map<String, String> createSmsEmailTemplate(String smsemailContent, String type, String Title, String cust_id, String sms_signatures, Integer marketProjectId) {
        Map<String, String> map = new HashMap<String, String>();
        // 1.短信 2.邮件 3.闪信
        int flag = 0;
        if (type.equals("1")) {
            // 增加模板信息
            String sql = "insert into t_template set cust_id=?,title=?,type_code=?,mould_content=?,create_time=now(),status=1,sms_signatures=?,market_project_id=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, cust_id, Title, type, smsemailContent, sms_signatures, marketProjectId);
        }
        if (type.equals("2")) {
            // 增加模板信息
            String sql = "insert into t_template set cust_id=?,title=?,type_code=?,email_mould_content=?,create_time=now(),status=1,market_project_id=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, cust_id, Title, type, smsemailContent, marketProjectId);
        }
        // 闪信模板
        if (type.equals("3")) {
            String sql = "insert into t_template set cust_id=?,title=?,type_code=?,mould_content=?,create_time=now(),status=1,sms_signatures=?,market_project_id=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, cust_id, Title, type, smsemailContent, sms_signatures, marketProjectId);
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


    public Map<String, String> createFlashSmsTemplate(String content, String type, String title, String customerId, String signatures) {
        Map<String, String> map = new HashMap<String, String>();
        int flag = 0;
        String sql = "insert into t_template set cust_id=?,title=?,type_code=?,mould_content=?,create_time=now(),status=1,sms_signatures=?";
        flag = this.marketResourceDao.executeUpdateSQL(sql, customerId, title, type, content, signatures);
        if (flag == 1) {
            map.put("code", "0");
            map.put("message", "成功");
        } else {
            map.put("code", "1");
            map.put("message", "失败");
        }
        return map;
    }


    public com.bdaim.common.dto.Page getSmsEmailTemplateListV1(String typeCode, String templateName, String templateId, String status, String custName, Integer pageNum, Integer pageSize, String custId, String marketProjectId) {
        com.bdaim.common.dto.Page page = marketResourceDao.pageMarketTemplate(pageNum, pageSize, templateName, templateId, custName, status, typeCode, custId, marketProjectId);
        if (page != null && page.getData() != null && page.getData().size() > 0) {
            MarketTemplate marketTemplate;
            MarketResourceEntity tmpMarketResource;
            MarketTemplateDTO marketTemplateDTO;
            List<MarketTemplateDTO> list = new ArrayList<>();
            for (int i = 0; i < page.getData().size(); i++) {
                marketTemplate = (MarketTemplate) page.getData().get(i);
                if (marketTemplate != null) {
                    marketTemplateDTO = new MarketTemplateDTO(marketTemplate);
                    //添加项目名称
                    if (marketTemplateDTO.getMarketProjectId() != null) {
                        MarketProject marketProject = marketProjectDao.selectMarketProject(marketTemplateDTO.getMarketProjectId());
                        if (marketProject != null) {
                            marketTemplateDTO.setMarketProjectName(marketProject.getName());
                        }
                    }
                    marketTemplateDTO.setCustName(customerDao.getEnterpriseName(marketTemplateDTO.getCustId()));
                    // 查询供应商和资源名称
                    if (StringUtil.isNotEmpty(marketTemplateDTO.getResourceId())) {
                        tmpMarketResource = marketResourceDao.getMarketResource(NumberConvertUtil.parseInt(marketTemplateDTO.getResourceId()));
                        marketTemplateDTO.setResourceName(tmpMarketResource != null ? tmpMarketResource.getResname() : "");
                        if (tmpMarketResource != null) {
                            marketTemplateDTO.setSupplierName(supplierDao.getSupplierName(NumberConvertUtil.parseInt(tmpMarketResource.getSupplierId())));
                        } else {
                            marketTemplateDTO.setSupplierName("");
                        }
                    } else {
                        marketTemplateDTO.setResourceName("");
                    }
                    list.add(marketTemplateDTO);
                }
            }
            page.setData(list);
        }
        return page;
    }


    public List<Map<String, Object>> getFlashSmsTemplateList(String typeCode, String templateName, String templateId, String status, String customerId, Integer pageNum, Integer pageSize) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        StringBuffer sb = new StringBuffer();
        sb.append(
                "select title,id,create_time,case status when 1 then '审核中' when 2 then '审批通过' when 3 then '审批未通过' end  status,mould_content, template_code from t_template where 1=1 ");
        if (null != templateName && !"".equals(templateName)) {
            sb.append(" and title like '%" + templateName + "%'");
        }
        if (null != templateId && !"".equals(templateId)) {
            sb.append(" and id=" + templateId + "");
        }
        if (null != status && !"".equals(status)) {
            sb.append(" and status=" + status + "");
        }
        sb.append(" and cust_id='" + customerId + "'");
        sb.append(" and type_code=" + typeCode);
        sb.append(" ORDER BY create_time DESC");
        sb.append(" LIMIT " + pageNum + ", " + pageSize);
        list = this.marketResourceDao.sqlQuery(sb.toString());
        return list;
    }

    /**
     * 查询短信模板内容
     */

    public String getSmsEmailTemplate(String type_code, String templateId, LoginUser loginUser) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        JSONObject json = new JSONObject();
        Map<Object, Object> maprReturn = new HashMap<Object, Object>();
        // 1.短信 2.邮件
        String sql = "";
        String code = "0";
        if (type_code.equals("1")) {
            if (loginUser.getUserType().equals("0")) {
                sql = "select title,mould_content,sms_signatures from t_template where id='" + templateId + "' ORDER BY create_time DESC";
            } else {
                sql = "select title,mould_content,sms_signatures from t_template where cust_id='" + loginUser.getCustId() + "'  and id='" + templateId + "' ORDER BY create_time DESC";
            }
            list = this.marketResourceDao.sqlQuery(sql);
        }
        if (type_code.equals("2")) {
            if (loginUser.getUserType().equals("0")) {
                sql = "select title,email_mould_content from t_template where id=? ORDER BY create_time DESC";
            } else {
                sql = "select title,email_mould_content from t_template where cust_id=" + loginUser.getCustId() + "' and id=? ORDER BY create_time DESC";
            }

            String title = null, email_mould_content = null;
            list = this.marketResourceDao.sqlQuery(sql, templateId);
            if (list.size() > 0) {
                title = String.valueOf(list.get(0));
                email_mould_content = String.valueOf(list.get(0));
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("title", title);
            map.put("mould_content", email_mould_content);
            list.add(map);
        }
        maprReturn.put("Code", code);
        maprReturn.put("templateList", list);
        json.put("data", maprReturn);
        return json.toJSONString();
    }

    public MarketTemplateDTO getSmsEmailTemplateV1(String typeCode, String templateId, LoginUser loginUser) {
        MarketTemplate marketTemplate = marketResourceDao.getMarketTemplate(NumberConvertUtil.parseInt(templateId));
        MarketTemplateDTO marketTemplateDTO = new MarketTemplateDTO(marketTemplate);
        marketTemplateDTO.setCustName(customerDao.getEnterpriseName(marketTemplateDTO.getCustId()));
        // 查询供应商和资源名称
        if (StringUtil.isNotEmpty(marketTemplateDTO.getResourceId())) {
            MarketResourceEntity tmpMarketResource = marketResourceDao.getMarketResource(NumberConvertUtil.parseInt(marketTemplateDTO.getResourceId()));
            marketTemplateDTO.setResourceName(tmpMarketResource != null ? tmpMarketResource.getResname() : "");
            if (tmpMarketResource != null) {
                marketTemplateDTO.setSupplierName(supplierDao.getSupplierName(NumberConvertUtil.parseInt(tmpMarketResource.getSupplierId())));
            } else {
                marketTemplateDTO.setSupplierName("");
            }
            marketTemplateDTO.setOperator(userDao.getName(marketTemplateDTO.getOperator()));
        } else {
            marketTemplateDTO.setResourceName("");
        }
        return marketTemplateDTO;
    }

    /**
     * 4.6、 修改短信模板内容
     */

    public Map<String, Object> updateSmsEmailTemplate(String type_code, String templateId, String smsemailContent, String title, String sms_signatures, Integer marketProjectId, String stauts) {
        Map<String, Object> map = new HashMap<String, Object>();
        int flag = 0;
        String sql = "";
        if (StringUtil.isNotEmpty(stauts)) {
            //修改模板状态
            sql = "update t_template set status=? where ID=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, stauts, templateId);
        } else {
            // 增加模板信息
            if (type_code.equals("1")) {
                sql = "update t_template set mould_content=?,title=?,status=1,modify_time=now(),sms_signatures=?,market_project_id=?  where  ID=?";
                flag = this.marketResourceDao.executeUpdateSQL(sql, smsemailContent, title, sms_signatures, marketProjectId, templateId);
            }
            if (type_code.equals("2")) {
                sql = "update t_template set email_mould_content=?,title=?,status=1,modify_time=now() ,market_project_id=? where  ID=?";
                flag = this.marketResourceDao.executeUpdateSQL(sql, smsemailContent, title, marketProjectId, templateId);
            }
            //活动页
            if (type_code.equals("4")) {
                sql = "update t_template set mould_content=?,title=?,status=1,modify_time=now(),sms_signatures=?,market_project_id=?  where  ID=?";
                flag = this.marketResourceDao.executeUpdateSQL(sql, smsemailContent, title, sms_signatures, marketProjectId, templateId);
            }
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


    public Map<String, Object> updateFlashSmsTemplate(String typeCode, String templateId, String content, String title, String signatures) {
        Map<String, Object> map = new HashMap<String, Object>();
        // 增加模板信息
        String sql = "";
        int flag = 0;
        if (typeCode.equals("1")) {
            sql = "update t_template set mould_content=?,title=?,status=1,modify_time=now(),sms_signatures=?  where  ID=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, content, title, signatures, templateId);
        } else if (typeCode.equals("2")) {
            sql = "update t_template set email_mould_content=?,title=?,status=1,modify_time=now()  where  ID=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, content, title, templateId);
        } else if ("3".equals(typeCode)) {
            sql = "update t_template set mould_content=?,title=?,status=1,modify_time=now(),sms_signatures=?  where  ID=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, content, title, signatures, templateId);
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

    /**
     * 4.10、 短信和邮件模板审批
     */

    public int useMarketResource(String typeCode, String templateId, String status, String remark, String templateCode, String resourceId, String operatorUserId) throws Exception {
        Map<String, Object> map = new HashMap<String, Object>();
        MarketTemplate marketTemplate = marketResourceDao.getMarketTemplate(NumberConvertUtil.parseInt(templateId));
        if (marketTemplate == null) {
            throw new RuntimeException("模板不存在,ID:" + templateId);
        }
        marketTemplate.setTemplateCode(templateCode);
        marketTemplate.setResourceId(resourceId);
        int flag = 0;
        // 增加模板信息
        // 1.通过，2未通过
        // '状态（1.审核中2.审批通过 3.审批未通过）',
        if (status.equals("1")) {
            marketTemplate.setStatus(2);
        }
        if (status.equals("2")) {
            marketTemplate.setStatus(3);
            marketTemplate.setRemark(remark);
        }
        marketTemplate.setModifyTime(new Timestamp(System.currentTimeMillis()));
        marketTemplate.setPassTime(new Timestamp(System.currentTimeMillis()));
        marketTemplate.setOperator(operatorUserId);
        marketResourceDao.saveOrUpdate(marketTemplate);
        return 1;
    }


    public Map<String, Object> approveFlashSmsTemplate(String type, String templateId, String status, String remark, String templateCode) {

        Map<String, Object> map = new HashMap<String, Object>();
        int flag = 0;
        // 增加模板信息
        // 1.通过，2未通过
        // '状态（1.审核中2.审批通过 3.审批未通过）',
        if (status.equals("1")) {
            String sql = "update t_template set status=2,modify_time=now(),pass_time=now()  where  ID=?";
            flag = this.marketResourceDao.executeUpdateSQL(sql, templateId);
        }
        if (status.equals("2")) {
            // 如果是闪信,则需要输入第三方平台的模板ID
            if ("3".equals(type)) {
                String sql = "update t_template set status=3, modify_time=now(), remark=?, template_code= ?  where  ID=?";
                flag = this.marketResourceDao.executeUpdateSQL(sql, remark, templateCode, templateId);
            } else {
                String sql = "update t_template set status=3,modify_time=now(),remark=?  where  ID=?";
                flag = this.marketResourceDao.executeUpdateSQL(sql, remark, templateId);
            }
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


    public List<Map<String, Object>> queryRecordVoicelog(String cust_id, Long userid, String user_type, String superId,
                                                         Integer pageNum, Integer pageSize, String realName, String createTimeStart, String createTimeEnd) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        StringBuffer sb = new StringBuffer();
        if ("1".equals(user_type)) {
            if ("".equals(superId)) {
                sb.append(
                        "  select voicLog.superid,voicLog.create_time create_time,voicLog.status,CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,backInfo.Callerduration, tUser.account name,tUser.realname,substring_index( backInfo.recordurl ,'/' , -1 ) as recordurl ")
                        .append("  from t_touch_voice_log voicLog")
                        .append("  LEFT JOIN t_callback_info backInfo ")
                        .append("  ON voicLog.callSid = backInfo.callSid")
                        .append("  LEFT JOIN t_customer_user  tUser ")
                        .append("  ON tUser.id =  voicLog.user_id")
                        .append("  where voicLog.cust_id='" + cust_id + "'");

                if (!"".equals(realName) && null != realName) {
                    sb.append(" AND   tUser.realname LIKE '%" + realName + "%'");
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
                sb.append(" LIMIT " + pageNum + "," + pageSize);
                list = this.marketResourceDao.sqlQuery(sb.toString());
            } else {
                sb.append(
                        "  select voicLog.superid,voicLog.create_time create_time,voicLog.status, CAST(voicLog.user_id AS CHAR) user_id, voicLog.remark,backInfo.Callerduration, tUser.account name,tUser.realname,substring_index( backInfo.recordurl ,'/' , -1 ) as recordurl ")
                        .append("  from t_touch_voice_log voicLog")
                        .append("  LEFT JOIN t_callback_info backInfo ")
                        .append("  ON voicLog.callSid = backInfo.callSid")
                        .append("  LEFT JOIN t_customer_user  tUser ")
                        .append("  ON tUser.id =  voicLog.user_id")
                        .append("  where voicLog.cust_id='" + cust_id + "'")
                        .append("  and voicLog.superid='" + superId + "'");
                sb.append(" order by voicLog.create_time DESC");
                sb.append(" LIMIT " + pageNum + "," + pageSize);

                list = this.marketResourceDao.sqlQuery(sb.toString());
            }

        }
        if ("2".equals(user_type)) {
            if ("".equals(superId)) {
                sb.append(
                        "  select voicLog.superid,voicLog.create_time create_time,voicLog.status, CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,backInfo.Callerduration, tUser.account name,tUser.realname,substring_index( backInfo.recordurl ,'/' , -1 ) as recordurl ")
                        .append("  from t_touch_voice_log voicLog")
                        .append("  LEFT JOIN t_callback_info backInfo ")
                        .append("  ON voicLog.callSid = backInfo.callSid")
                        .append("  LEFT JOIN t_customer_user  tUser ")
                        .append("  ON tUser.id =  voicLog.user_id")
                        .append("  where voicLog.user_id='" + userid + "'");
                if (!"".equals(realName) && null != realName) {
                    sb.append(" AND   tUser.realname LIKE '%" + realName + "%'");
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
                sb.append("  LIMIT " + pageNum + "," + pageSize);

                list = this.marketResourceDao.sqlQuery(sb.toString());
            } else {
                sb.append(
                        "  select voicLog.superid,voicLog.create_time create_time,voicLog.status,CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,backInfo.Callerduration,tUser.account name,tUser.realname,substring_index( backInfo.recordurl ,'/' , -1 ) as recordurl ")
                        .append("  from t_touch_voice_log voicLog")
                        .append("  LEFT JOIN t_callback_info backInfo ")
                        .append("  ON voicLog.callSid = backInfo.callSid")
                        .append("  LEFT JOIN t_customer_user  tUser ")
                        .append("  ON tUser.id =  voicLog.user_id")
                        .append("  where voicLog.user_id='" + userid + "'")
                        .append("  and voicLog.superid='" + superId + "'");
                sb.append(" order by voicLog.create_time DESC");
                sb.append(" LIMIT " + pageNum + "," + pageSize);

                list = this.marketResourceDao.sqlQuery(sb.toString());
            }
        }

        return list;
    }


    public List<Map<String, Object>> queryRecordVoiceLogV3(LoginUser loginUser, String customerGroupId, String superId, Integer pageNum, Integer pageSize, String realName,
                                                           String createTimeStart, String createTimeEnd, String remark, String callStatus, String level, String auditingStatus) {
        List<Map<String, Object>> list = null;
        int taskType = -1;
        try {
            StringBuffer sb = new StringBuffer();
            // 开始时间和结束时间为空时默认查当天
            if (StringUtil.isEmpty(createTimeStart)) {
                createTimeStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(YYYYMMDDHHMMSS);
            }
            if (StringUtil.isEmpty(createTimeEnd)) {
                createTimeEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(YYYYMMDDHHMMSS);
            }

            LocalDate endTime = LocalDate.now();
            if (StringUtil.isNotEmpty(createTimeEnd)) {
                endTime = LocalDate.parse(createTimeEnd, YYYYMMDDHHMMSS);
            }

            // 检查通话记录月表是否存在
            marketResourceDao.createVoiceLogTableNotExist(endTime.format(YYYYMM));

            String monthTableName = ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + endTime.format(YYYYMM);
            if (StringUtil.isNotEmpty(customerGroupId)) {
                // 处理任务类型
                CustomGroup customGroup = customGroupDao.get(Integer.parseInt(customerGroupId));
                if (customGroup != null && customGroup.getTaskType() != null) {
                    taskType = customGroup.getTaskType();
                }
            }

            if (3 == taskType) {
                sb.append("select voicLog.touch_id touchId, voicLog.callSid, voicLog.superid,voicLog.create_time create_time,voicLog.status, CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,")
                        .append(" voicLog.call_data, voicLog.recordurl, voicLog.clue_audit_status auditingStatus ")
                        .append("  from " + monthTableName + " voicLog WHERE 1=1 ");
            } else {
                sb.append("select voicLog.touch_id touchId, voicLog.callSid, voicLog.superid,voicLog.create_time create_time,voicLog.status, CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,")
                        .append(" voicLog.call_data, voicLog.recordurl ")
                        .append("  from " + monthTableName + " voicLog  WHERE 1=1");
            }
            if (StringUtil.isNotEmpty(loginUser.getCustId())) {
                sb.append(" AND voicLog.cust_id='" + loginUser.getCustId() + "'");
            }
            // 处理根据登陆账号或者用户姓名搜索
            CustomerUser user = null;
            if (StringUtil.isNotEmpty(realName)) {
                user = this.customerUserDao.getCustomerUserByName(realName.trim());
                if (user != null) {
                    sb.append(" AND  voicLog.user_id = '" + user.getId() + "'");
                } else {
                    // 穿透查询一次登陆名称
                    user = this.customerUserDao.getCustomerUserByLoginName(realName.trim());
                    if (user != null) {
                        sb.append(" AND  voicLog.user_id = '" + user.getId() + "'");
                    } else {
                        return list;
                    }
                }
            }
            if (StringUtil.isNotEmpty(customerGroupId)) {
                sb.append(" AND  voicLog.customer_group_id =" + customerGroupId);
            }
            if (StringUtil.isNotEmpty(remark)) {
                sb.append(" AND  voicLog.remark LIKE '%" + remark.trim() + "%'");
            }
            if (StringUtil.isNotEmpty(superId)) {
                sb.append(" AND voicLog.superid='" + superId.trim() + "'");
            }
            //　处理通话状态查询
            if (StringUtil.isNotEmpty(callStatus)) {
                // 成功
                if ("1".equals(callStatus)) {
                    sb.append(" AND voicLog.status = 1001");
                    //失败
                } else if ("2".equals(callStatus)) {
                    sb.append(" AND voicLog.status = 1002");
                } else {
                    sb.append(" AND voicLog.status = " + CallStatusEnum.getByType(NumberConvertUtil.parseInt(callStatus)).getStatus());
                }
            }
            // 处理开始和结束数据搜索
            if (StringUtil.isNotEmpty(createTimeStart) && StringUtil.isNotEmpty(createTimeEnd)) {
                sb.append(" AND voicLog.create_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
            } else {
                if (StringUtil.isNotEmpty(createTimeStart)) {
                    sb.append(" AND voicLog.create_time > '" + createTimeStart + "'");
                }
                if (StringUtil.isNotEmpty(createTimeEnd)) {
                    sb.append(" AND voicLog.create_time < '" + createTimeEnd + "'");
                }
            }

            // 处理机器人外呼任务营销记录
            if (3 == taskType) {
                // 处理机器人外呼的意向度
                if (StringUtil.isNotEmpty(level)) {
                    String levelLike = "\"level\":\"" + level + "\"";
                    sb.append(" AND voicLog.call_data LIKE '%" + levelLike + "%'");
                }
                // 处理按照操作人搜索营销记录时机器人外呼任务记录可以搜到
                if (user != null) {
                    sb.append(" AND (voicLog.user_id = '" + user.getId() + "' OR voicLog.call_data LIKE '%level%')");
                }
                // 处理人工审核搜索条件
                if (StringUtil.isNotEmpty(auditingStatus)) {
                    sb.append(" AND voicLog.clue_audit_status = " + auditingStatus);
                }
            }
            // 处理组长权限
            if (UserService.OPERATOR_USER_TYPE.equals(loginUser.getUserType())) {
                // 组长查组员列表
                if ("1".equals(loginUser.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                    // 处理组长下有员工的情况
                    if (customerUserDTOList.size() > 0) {
                        Set<String> userIds = new HashSet<>();
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sb.append(" AND (voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR voicLog.call_data LIKE '%level%')");
                            } else {
                                sb.append(" AND voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sb.append(" AND (voicLog.user_id = '" + loginUser.getId() + "' OR voicLog.call_data LIKE '%level%')");
                        } else {
                            sb.append(" AND voicLog.user_id = '" + loginUser.getId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        sb.append(" AND (voicLog.user_id = '" + loginUser.getId() + "' OR voicLog.call_data LIKE '%level%')");
                    } else {
                        sb.append(" AND voicLog.user_id = '" + loginUser.getId() + "'");
                    }
                }
            }
            sb.append(" order by voicLog.create_time DESC");
            sb.append("  LIMIT ?,? ");
            list = this.marketResourceDao.sqlQuery(sb.toString(), pageNum, pageSize);
            CustomerUser customerUser;
            if (list.size() > 0) {
                //处理用户信息和录音文件
                String recordUrl;
                VoiceLogCallDataDTO voiceLogCallDataDTO;
                for (Map<String, Object> map : list) {
                    recordUrl = String.valueOf(map.get("recordurl"));
                    if (StringUtil.isNotEmpty(String.valueOf(map.get("create_time")))) {
                        recordUrl = LocalDateTime.parse(String.valueOf(map.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM) + map.get("touchId");
                    }
                    map.put("recordurl", recordUrl);
                    // 处理通话call_data json数据
                    if (map.get("call_data") != null) {
                        voiceLogCallDataDTO = JSON.parseObject(String.valueOf(map.get("call_data")), VoiceLogCallDataDTO.class);
                        if (voiceLogCallDataDTO != null) {
                            // 处理通话时长
                            if (StringUtil.isNotEmpty(voiceLogCallDataDTO.getCalledDuration())
                                    && !"null".equals(voiceLogCallDataDTO.getCalledDuration())) {
                                map.put("Callerduration", voiceLogCallDataDTO.getCalledDuration());
                            } else {
                                map.put("Callerduration", "");
                            }

                            // 处理机器人外呼的意向度
                            if (3 == taskType) {
                                map.put("intentLevel", voiceLogCallDataDTO.getLevel());
                            } else {
                                map.put("intentLevel", "");
                            }
                        }
                    }
                    // 处理机器人外呼的操作人为robot+机器人id
                    if (3 == taskType) {
                        customerUser = customerUserDao.get(NumberConvertUtil.parseLong(String.valueOf(map.get("user_id"))));
                        if (customerUser == null) {
                            if (map.get("user_id") == null) {
                                map.put("name", "");
                                map.put("realname", "");
                            } else {
                                if ("0".equals(String.valueOf(map.get("user_id")))) {
                                    map.put("name", "");
                                    map.put("realname", "");
                                } else {
                                    map.put("name", "robot" + map.get("user_id"));
                                    map.put("realname", "robot" + map.get("user_id"));
                                }
                            }
                        } else {
                            map.put("name", customerUser.getAccount());
                            map.put("realname", customerUser.getAccount());
                        }
                    } else {
                        map.put("name", customerUserDao.getLoginName(String.valueOf(map.get("user_id"))));
                        map.put("realname", customerUserDao.getLoginName(String.valueOf(map.get("user_id"))));
                    }
                    // 处理呼叫中心外呼无意向度
                    if (map.get("intentLevel") == null) {
                        map.put("intentLevel", "");
                    }
                    //不返回通话自定义json字段
                    map.remove("call_data");
                }
            }
        } catch (Exception e) {
            LOG.error("查询通话记录失败,", e);
            list = null;
        }
        return list;
    }


    /**
     * 通话记录接口
     *
     * @param userQueryParam
     * @param customerGroupId
     * @param superId
     * @param realName
     * @param createTimeStart
     * @param createTimeEnd
     * @param remark
     * @param callStatus
     * @param level
     * @param auditingStatus
     * @return
     */
    public com.bdaim.common.dto.Page queryRecordVoiceLogV4(UserQueryParam userQueryParam, String customerGroupId, String superId, String realName,
                                                           String createTimeStart, String createTimeEnd, String remark, String callStatus, String level,
                                                           String auditingStatus, String marketTaskId, int calledDuration, String custProperty, String seaId) {
        com.bdaim.common.dto.Page page = null;
        int taskType = -1;
        try {
            StringBuffer sb = new StringBuffer();
            LocalDate endTime = LocalDate.now();
            // 开始时间和结束时间为空时默认查当天
            if (StringUtil.isEmpty(createTimeStart)) {
                createTimeStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(YYYYMMDDHHMMSS);
            }
            if (StringUtil.isEmpty(createTimeEnd)) {
                createTimeEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(YYYYMMDDHHMMSS);
            } else {
                endTime = LocalDate.parse(createTimeEnd, YYYYMMDDHHMMSS);
            }

            // 检查通话记录月表是否存在
            marketResourceDao.createVoiceLogTableNotExist(endTime.format(YYYYMM));

            String monthTableName = ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + endTime.format(YYYYMM);
            // 处理任务类型
            MarketTask marketTask = null;
            if (StringUtil.isNotEmpty(marketTaskId)) {
                marketTask = marketTaskDao.get(marketTaskId);
                if (marketTask != null && marketTask.getTaskType() != null) {
                    taskType = marketTask.getTaskType();
                }
            }
            if (marketTask == null) {
                marketTask = new MarketTask();
            }

            sb.append("select voicLog.touch_id touchId, voicLog.callSid, voicLog.superid,voicLog.create_time create_time,voicLog.status, CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,")
                    .append(" voicLog.call_data, voicLog.recordurl, voicLog.clue_audit_status auditingStatus, voicLog.market_task_id marketTaskId, voicLog.clue_audit_reason reason ")
                    .append("  from " + monthTableName + " voicLog ");
            // 处理自建属性搜索
            if (StringUtil.isNotEmpty(custProperty) && StringUtil.isNotEmpty(marketTaskId) && !"[]".equals(custProperty)) {
                // 查询所有自建属性
                List<CustomerLabel> customerLabels = customerLabelDao.listCustomerLabel(marketTask.getCustId());
                Map<String, CustomerLabel> cacheLabel = new HashMap<>();
                for (CustomerLabel c : customerLabels) {
                    cacheLabel.put(c.getLabelId(), c);
                }
                sb.append(" INNER JOIN " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " t2 ON t2.id = voicLog.superid ");
                JSONObject jsonObject;
                String labelId, optionValue, likeValue;
                JSONArray jsonArray = JSON.parseArray(custProperty);
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject != null) {
                        labelId = jsonObject.getString("labelId");
                        optionValue = jsonObject.getString("optionValue");
                        // 文本和多选支持模糊搜索
                        if (cacheLabel.get(labelId) != null && cacheLabel.get(labelId).getType() != null
                                && (cacheLabel.get(labelId).getType() == 1 || cacheLabel.get(labelId).getType() == 3)) {
                            likeValue = "%\"" + labelId + "\":\"%" + optionValue + "%";
                        } else {
                            likeValue = "%\"" + labelId + "\":\"" + optionValue + "\"%";
                        }
                        sb.append(" AND t2.super_data LIKE '" + likeValue + "' ");
                    }
                }
            }

            sb.append(" WHERE 1=1");
            if ("-1".equals(userQueryParam.getCustId())) {
                sb.append(" AND voicLog.cust_id IS NOT NULL ");
            } else {
                sb.append(" AND voicLog.cust_id='" + userQueryParam.getCustId() + "'");
            }
            // 处理根据登陆账号或者用户姓名搜索
            CustomerUser user = null;
            if (StringUtil.isNotEmpty(realName)) {
                user = this.customerUserDao.getCustomerUserByName(realName.trim());
                if (user != null) {
                    sb.append(" AND  voicLog.user_id = '" + user.getId() + "'");
                } else {
                    // 穿透查询一次登陆名称
                    user = this.customerUserDao.getCustomerUserByLoginName(realName.trim());
                    if (user != null) {
                        sb.append(" AND  voicLog.user_id = '" + user.getId() + "'");
                    } else {
                        return new com.bdaim.common.dto.Page();
                    }
                }
            }
            if (StringUtil.isNotEmpty(customerGroupId)) {
                sb.append(" AND voicLog.customer_group_id =" + customerGroupId.trim());
            }
            if (StringUtil.isNotEmpty(remark)) {
                sb.append(" AND voicLog.remark LIKE '%" + remark.trim() + "%'");
            }
            if (StringUtil.isNotEmpty(superId)) {
                sb.append(" AND voicLog.superid='" + superId.trim() + "'");
            }
            if (StringUtil.isNotEmpty(marketTaskId)) {
                sb.append(" AND voicLog.market_task_id='" + marketTaskId.trim() + "'");
            }
            //　处理通话状态查询
            if (StringUtil.isNotEmpty(callStatus)) {
                // 成功
                if ("1".equals(callStatus)) {
                    sb.append(" AND voicLog.status = 1001");
                    //失败
                } else if ("2".equals(callStatus)) {
                    sb.append(" AND voicLog.status = 1002");
                } else {
                    sb.append(" AND voicLog.status = " + CallStatusEnum.getByType(NumberConvertUtil.parseInt(callStatus)).getStatus());
                }
            }
            // 处理开始和结束数据搜索
            if (StringUtil.isNotEmpty(createTimeStart) && StringUtil.isNotEmpty(createTimeEnd)) {
                sb.append(" AND voicLog.create_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
            } else {
                if (StringUtil.isNotEmpty(createTimeStart)) {
                    sb.append(" AND voicLog.create_time > '" + createTimeStart + "'");
                }
                if (StringUtil.isNotEmpty(createTimeEnd)) {
                    sb.append(" AND voicLog.create_time < '" + createTimeEnd + "'");
                }
            }
            //type 0 查詢全部   1查詢<=3  2、3s-6s 3.6s-12s  4.12s-30s 5.30s-60s 6.>60s
            if (calledDuration == 1) {
                sb.append(" AND voicLog.called_duration<=3");
            } else if (calledDuration == 2) {
                sb.append(" AND voicLog.called_duration>3 AND voicLog.called_duration<=6");
            } else if (calledDuration == 3) {
                sb.append(" AND voicLog.called_duration>6 AND voicLog.called_duration<=12");
            } else if (calledDuration == 4) {
                sb.append(" AND voicLog.called_duration>12 AND voicLog.called_duration<=30");
            } else if (calledDuration == 5) {
                sb.append(" AND voicLog.called_duration>30 AND voicLog.called_duration<=60");
            } else if (calledDuration == 6) {
                sb.append(" AND voicLog.called_duration>60");
            }
            // 处理机器人外呼任务营销记录
            if (3 == taskType) {
                // 处理机器人外呼的意向度
                if (StringUtil.isNotEmpty(level)) {
                    String levelLike = "\"level\":\"" + level + "\"";
                    sb.append(" AND voicLog.call_data LIKE '%" + levelLike + "%'");
                }
                // 处理按照操作人搜索营销记录时机器人外呼任务记录可以搜到
                if (user != null) {
                    sb.append(" AND (voicLog.user_id = '" + user.getId() + "' OR voicLog.call_data LIKE '%level%')");
                }
                // 处理人工审核搜索条件
                if (StringUtil.isNotEmpty(auditingStatus)) {
                    sb.append(" AND voicLog.clue_audit_status = " + auditingStatus);
                }
            }
            // 处理组长权限
            if (UserService.OPERATOR_USER_TYPE.equals(userQueryParam.getUserType())) {
                // 组长查组员列表
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    // 处理组长下有员工的情况
                    if (customerUserDTOList.size() > 0) {
                        Set<String> userIds = new HashSet<>();
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sb.append(" AND (voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR voicLog.call_data LIKE '%level%')");
                            } else {
                                sb.append(" AND voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sb.append(" AND (voicLog.user_id = '" + userQueryParam.getUserId() + "' OR voicLog.call_data LIKE '%level%')");
                        } else {
                            sb.append(" AND voicLog.user_id = '" + userQueryParam.getUserId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        sb.append(" AND (voicLog.user_id = '" + userQueryParam.getUserId() + "' OR voicLog.call_data LIKE '%level%')");
                    } else {
                        sb.append(" AND voicLog.user_id = '" + userQueryParam.getUserId() + "'");
                    }
                }
            }
            // 根据公海ID查询通话记录
            if (StringUtil.isNotEmpty(seaId)) {
                sb.append(" AND voicLog.customer_sea_id = '" + seaId + "'");
            }
            sb.append(" order by voicLog.create_time DESC");
            page = this.marketResourceDao.sqlPageQuery0(sb.toString(), userQueryParam.getPageNum(), userQueryParam.getPageSize());
            CustomerUser customerUser;
            if (page.getData() != null && page.getData().size() > 0) {
                //处理用户信息和录音文件
                String monthYear = endTime.format(YYYYMM);
                VoiceLogCallDataDTO voiceLogCallDataDTO;
                MarketTask task;
                List<Map<String, Object>> list = page.getData();
                for (Map<String, Object> map : list) {
                    if (StringUtil.isNotEmpty(String.valueOf(map.get("create_time")))) {
                        monthYear = LocalDateTime.parse(String.valueOf(map.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                    }
                    map.put("recordurl", CallUtil.generateRecordNameToMp3(monthYear, map.get("touchId")));
                    // 处理通话call_data json数据
                    if (map.get("call_data") != null) {
                        voiceLogCallDataDTO = JSON.parseObject(String.valueOf(map.get("call_data")), VoiceLogCallDataDTO.class);
                        if (voiceLogCallDataDTO != null) {
                            // 处理通话时长
                            if (StringUtil.isNotEmpty(voiceLogCallDataDTO.getCalledDuration())
                                    && !"null".equals(voiceLogCallDataDTO.getCalledDuration())) {
                                map.put("Callerduration", voiceLogCallDataDTO.getCalledDuration());
                            } else {
                                map.put("Callerduration", "");
                            }
                            // 处理机器人外呼的意向度
                            if (3 == taskType) {
                                map.put("intentLevel", voiceLogCallDataDTO.getLevel());
                            } else {
                                map.put("intentLevel", "");
                            }
                        }
                    }
                    // 处理机器人外呼的操作人为robot+机器人id
                    if (3 == taskType) {
                        customerUser = customerUserDao.get(NumberConvertUtil.parseLong(String.valueOf(map.get("user_id"))));
                        if (customerUser == null) {
                            if (map.get("user_id") == null) {
                                map.put("name", "");
                                map.put("realname", "");
                            } else {
                                if ("0".equals(String.valueOf(map.get("user_id")))) {
                                    map.put("name", "");
                                    map.put("realname", "");
                                } else {
                                    map.put("name", "robot" + map.get("user_id"));
                                    map.put("realname", "robot" + map.get("user_id"));
                                }
                            }
                        } else {
                            map.put("name", customerUser.getAccount());
                            map.put("realname", customerUser.getAccount());
                        }
                    } else {
                        map.put("name", customerUserDao.getLoginName(String.valueOf(map.get("user_id"))));
                        map.put("realname", customerUserDao.getLoginName(String.valueOf(map.get("user_id"))));
                    }
                    // 处理呼叫中心外呼无意向度
                    if (map.get("intentLevel") == null) {
                        map.put("intentLevel", "");
                    }
                    //不返回通话自定义json字段
                    map.remove("call_data");
                    // 营销任务名称处理
                    if (map.get("marketTaskId") != null) {
                        task = marketTaskDao.get(String.valueOf(map.get("marketTaskId")));
                        map.put("marketTaskName", task != null ? task.getName() : "");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("查询通话记录失败,", e);
        }
        return page;
    }


    public String setWorkNum(String workNum, String userid, String custId) {
        JSONObject map = new JSONObject();

        CustomerUser cu = customerUserDao.get(Long.parseLong(userid));
        if (cu == null || cu.getCust_id() == null || !cu.getCust_id().equals(custId)) {
            map.put("code", "1");
            map.put("message", "失败");
            return map.toJSONString();
        }

        CustomerUserPropertyDO work_num = new CustomerUserPropertyDO(userid, "work_num", workNum, new Timestamp(System.currentTimeMillis()));
        CustomerUserPropertyDO work_num_status = new CustomerUserPropertyDO(userid, "work_num_status", "0", new Timestamp(System.currentTimeMillis()));
        this.customerUserDao.saveOrUpdate(work_num);
        this.customerUserDao.saveOrUpdate(work_num_status);

        map.put("code", "0");
        map.put("message", "成功");
        return map.toJSONString();
    }


    public String setPrice(String resouceId, Double costPrice, Double salePrice, String type) {
        // 资源类型（1.voice 2.SMS 3.email）
        JSONObject map = new JSONObject();
        String sql = "update t_market_resource set sale_price=?,cost_price=? where resource_id=? AND type_code=? ";
        int flag = this.marketResourceDao.executeUpdateSQL(sql, salePrice, costPrice, resouceId, type);
        if (flag == 1) {
            map.put("code", "0");
            map.put("message", "成功");
        } else {
            map.put("code", "1");
            map.put("message", "失败");
        }
        return map.toJSONString();
    }

    /**
     * 号码审批
     *
     * @param status  外呼号码状态（1.可用 2.不可用）
     * @param userid  客户id
     * @param workNum 外呼号码
     * @return
     */

    public Map<String, Object> approvePhone(String status, String userid, String workNum, String remark) {
        Map<String, Object> map = new HashMap<String, Object>();
        CustomerUserPropertyDO work_num_status = customerUserDao.getProperty(userid, "work_num_status");
        if (work_num_status != null) {
            if (status.equals("1")) {
                work_num_status.setPropertyValue("1");
                this.marketResourceDao.saveOrUpdate(work_num_status);
               /* // 审核通话添加联通主叫号码
                unicomService.saveUpdateUserExtensionByUserId(userid, "", 0);*/
                map.put("code", "0");
                map.put("messa", "成功");
            } else if (status.equals("2")) {
                work_num_status.setPropertyValue("2");
                this.marketResourceDao.saveOrUpdate(work_num_status);
                map.put("code", "0");
                map.put("messa", "成功");
            } else {
                map.put("code", "1");
                map.put("messa", "失败");
            }
        } else {
            map.put("code", "1");
            map.put("messa", "失败");
        }
        return map;
    }


    public List<Map<String, Object>> queryWorkNumList(String cust_id, String workNumStatus, String username, Integer pageNum, Integer pageSize) {
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if (cust_id == null || "".equals(cust_id)) {
            if (username == null || "".equals(username)) {
                String sql = "select account AS userName, realname as name,CAST(id AS CHAR)id from t_customer_user m where m.id in (select user_id from t_customer_user_property where property_name='work_num_status' and property_value='" + workNumStatus + "') ";
                sql += " ORDER BY m.account ASC LIMIT " + pageNum + "," + pageSize;
                list = this.marketResourceDao.sqlQuery(sql);
            } else {
                String sql = "select account AS userName, realname as name,CAST(id AS CHAR)id from t_customer_user m where m.id in (select user_id from t_customer_user_property where property_name='work_num_status' and property_value='" + workNumStatus + "') and realname like '%" + username + "%' ";
                sql += " ORDER BY m.account ASC LIMIT " + pageNum + "," + pageSize;
                list = this.marketResourceDao.sqlQuery(sql);
            }
        } else {
            if (username == null || "".equals(username)) {
                String sql = "select account AS userName, realname as name,CAST(id AS CHAR)id from t_customer_user m where cust_id='" + cust_id + "' ";
                sql += " ORDER BY m.account ASC LIMIT " + pageNum + "," + pageSize;
                list = this.marketResourceDao.sqlQuery(sql);
            } else {
                String sql = "select account AS userName, realname as name,CAST(id AS CHAR)id from t_customer_user m where cust_id='" + cust_id + "' and realname like '%" + username + "%' ";
                sql += " ORDER BY m.account ASC LIMIT " + pageNum + "," + pageSize;
                list = this.marketResourceDao.sqlQuery(sql);
            }
        }

        for (int i = 0; i < list.size(); i++) {
            Map u = (Map) list.get(i);
            CustomerUserPropertyDO work_num = customerUserDao.getProperty(String.valueOf(u.get("id")), "work_num");
            CustomerUserPropertyDO work_num_status = customerUserDao.getProperty(String.valueOf(u.get("id")), "work_num_status");
            CustomerUserPropertyDO active_time = customerUserDao.getProperty(String.valueOf(u.get("id")), "active_time");
            if (work_num != null && StringUtil.isNotEmpty(work_num.getPropertyValue())) {
                u.put("workNum", work_num.getPropertyValue());
            } else {
                u.put("workNum", "");
            }
            if (work_num_status != null && StringUtil.isNotEmpty(work_num_status.getPropertyValue())) {
                u.put("workNumStatus", work_num_status.getPropertyValue());
            } else {
                u.put("workNumStatus", "");
            }
            if (active_time != null && StringUtil.isNotEmpty(active_time.getPropertyValue())) {
                u.put("activeTime", active_time.getPropertyValue());
            } else {
                u.put("activeTime", "");
            }
        }

        return list;
    }

    public com.bdaim.common.dto.Page queryWorkNumListV1(String cust_id, String workNumStatus, String username, Integer pageNum, Integer pageSize) {
        com.bdaim.common.dto.Page page = null;
        String sql = "";
        if (cust_id == null || "".equals(cust_id)) {
            if (username == null || "".equals(username)) {
                sql = "select t1.account AS userName, t1.realname as name,CAST(t1.id AS CHAR)id from t_customer_user t1 JOIN t_customer_user_property t2 ON t1.id = t2.user_id AND t2.property_name='work_num_status' WHERE t2.property_value='" + workNumStatus + "'";
                sql += " ORDER BY t2.create_time DESC ";
            } else {
                sql = "select t1.account AS userName, t1.realname as name, CAST(t1.id AS CHAR)id from t_customer_user t1 JOIN t_customer_user_property t2 ON t1.id = t2.user_id AND t2.property_name='work_num_status' WHERE t2.property_value='" + workNumStatus + "' and t1.realname like '%" + username + "%' ";
                sql += " ORDER BY t2.create_time DESC ";
            }
        } else {
            if (username == null || "".equals(username)) {
                sql = "select account AS userName, realname as name,CAST(id AS CHAR)id from t_customer_user m where cust_id='" + cust_id + "' ";
                sql += " ORDER BY m.account ASC ";
            } else {
                sql = "select account AS userName, realname as name,CAST(id AS CHAR)id from t_customer_user m where cust_id='" + cust_id + "' and realname like '%" + username + "%' ";
                sql += " ORDER BY m.account ASC ";
            }
        }
        page = marketResourceDao.sqlPageQuery0(sql, pageNum, pageSize, null);
        if (page.getData() != null) {
            for (int i = 0; i < page.getData().size(); i++) {
                Map u = (Map) page.getData().get(i);
                CustomerUserPropertyDO work_num = customerUserDao.getProperty(String.valueOf(u.get("id")), "work_num");
                CustomerUserPropertyDO work_num_status = customerUserDao.getProperty(String.valueOf(u.get("id")), "work_num_status");
                CustomerUserPropertyDO active_time = customerUserDao.getProperty(String.valueOf(u.get("id")), "active_time");
                if (work_num != null && StringUtil.isNotEmpty(work_num.getPropertyValue())) {
                    u.put("workNum", work_num.getPropertyValue());
                } else {
                    u.put("workNum", "");
                }
                if (work_num_status != null && StringUtil.isNotEmpty(work_num_status.getPropertyValue())) {
                    u.put("workNumStatus", work_num_status.getPropertyValue());
                } else {
                    u.put("workNumStatus", "");
                }
                if (active_time != null && StringUtil.isNotEmpty(active_time.getPropertyValue())) {
                    u.put("activeTime", active_time.getPropertyValue());
                } else {
                    u.put("activeTime", "");
                }
            }
        }
        return page;
    }


    public long queryWorkNumAllCount(String cust_id, String workNumStatus, String username) {
        long total = 0;
        List<Map<String, Object>> list;
        if (cust_id == null || "".equals(cust_id)) {
            if (username == null || "".equals(username)) {
                String sql = "select count(*) count  from t_customer_user m where m.id in (select user_id from t_customer_user_property where property_name='work_num_status' and property_value='" + workNumStatus + "') ";
                list = this.marketResourceDao.sqlQuery(sql);
            } else {
                String sql = "select count(*) count  from t_customer_user m where m.id in (select user_id from t_customer_user_property where property_name='work_num_status' and property_value='" + workNumStatus + "') and realname like '%" + username + "%' ";
                list = this.marketResourceDao.sqlQuery(sql);
            }
        } else {
            if (username == null || "".equals(username)) {
                String sql = "select count(*) count  from t_customer_user m where cust_id='" + cust_id + "' ";
                list = this.marketResourceDao.sqlQuery(sql);
            } else {
                String sql = "select count(*) count from t_customer_user m where cust_id='" + cust_id + "' and realname like '%" + username + "%' ";
                list = this.marketResourceDao.sqlQuery(sql);
            }
        }
        if (list != null && list.size() > 0) {
            total = Long.parseLong(String.valueOf(list.get(0).get("count")));
        }
        return total;
    }

    public List<Map<String, Object>> queryRecordAllVoicelogCount(String cust_id, Long userid, String user_type,
                                                                 String superId, String realName, String createTimeStart, String createTimeEnd) {

        StringBuffer sb = new StringBuffer();
        List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        if ("1".equals(user_type)) {
            if ("".equals(superId)) {
                sb.append("  select count(*) count")
                        .append("  from t_touch_voice_log voicLog")
                        .append("  LEFT JOIN t_callback_info backInfo ")
                        .append("  ON voicLog.callSid = backInfo.callSid")
                        .append("  LEFT JOIN t_customer_user  tUser ")
                        .append("  ON tUser.id =  voicLog.user_id")
                        .append("  where voicLog.cust_id=?");

                if (!"".equals(realName) && null != realName) {
                    sb.append(" AND   tUser.realname LIKE '%" + realName + "%'");
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
                list = this.marketResourceDao.sqlQuery(sb.toString(), cust_id);
            } else {
                sb.append("  select count(*) count")
                        .append("  from t_touch_voice_log voicLog")
                        .append("  LEFT JOIN t_callback_info backInfo ")
                        .append("  ON voicLog.callSid = backInfo.callSid")
                        .append("  LEFT JOIN t_customer_user  tUser ")
                        .append("  ON tUser.id =  voicLog.user_id")
                        .append("  where voicLog.cust_id=?")
                        .append("  and voicLog.superid=?");


                list = this.marketResourceDao.sqlQuery(sb.toString(), cust_id, superId);
            }

        }
        if ("2".equals(user_type)) {
            if ("".equals(superId)) {
                sb.append("  select count(*) count")
                        .append("  from t_touch_voice_log voicLog")
                        .append("  LEFT JOIN t_callback_info backInfo ")
                        .append("  ON voicLog.callSid = backInfo.callSid")
                        .append("  LEFT JOIN t_customer_user  tUser ")
                        .append("  ON tUser.id =  voicLog.user_id")
                        .append("  where voicLog.user_id=?");
                if (!"".equals(realName) && null != realName) {
                    sb.append(" AND   tUser.realname LIKE '%" + realName + "%'");
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


                list = this.marketResourceDao.sqlQuery(sb.toString(), userid);
            } else {
                // String sql = "select count(*) count from t_touch_voice_log
                // where user_id=? and superid=?";
                sb.append("  select count(*) count")
                        .append("  from t_touch_voice_log voicLog")
                        .append("  LEFT JOIN t_callback_info backInfo ")
                        .append("  ON voicLog.callSid = backInfo.callSid")
                        .append("  LEFT JOIN t_customer_user  tUser ")
                        .append("  ON tUser.id =  voicLog.user_id")
                        .append("  where voicLog.user_id=?")
                        .append("  and voicLog.superid=?");
                list = this.marketResourceDao.sqlQuery(sb.toString(), userid, superId);
            }
        }

        return list;
    }


    public long queryRecordAllVoiceLogCountV3(LoginUser loginUser, String customerGroupId,
                                              String superId, String realName, String createTimeStart, String createTimeEnd,
                                              String remark, String callStatus, String level, String auditingStatus) {
        List<Map<String, Object>> list;
        int taskType = -1;
        try {
            StringBuffer sb = new StringBuffer();
            // 开始时间和结束时间为空时默认查当天
            if (StringUtil.isEmpty(createTimeStart)) {
                createTimeStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(YYYYMMDDHHMMSS);
            }
            if (StringUtil.isEmpty(createTimeEnd)) {
                createTimeEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(YYYYMMDDHHMMSS);
            }

            LocalDate endTime = LocalDate.now();
            if (StringUtil.isNotEmpty(createTimeEnd)) {
                endTime = LocalDate.parse(createTimeEnd, YYYYMMDDHHMMSS);
            }

            // 检查通话记录月表是否存在
            marketResourceDao.createVoiceLogTableNotExist(endTime.format(YYYYMM));

            sb.append("select count(*) count ")
                    .append("  from " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + endTime.format(YYYYMM) + " voicLog")
                    .append("  where 1=1 ");
            if (StringUtil.isNotEmpty(loginUser.getCustId())) {
                sb.append(" AND voicLog.cust_id='" + loginUser.getCustId() + "'");
            }
            // 处理根据登陆账号或者用户姓名搜索
            CustomerUser user = null;
            if (StringUtil.isNotEmpty(realName)) {
                user = this.customerUserDao.getCustomerUserByName(realName.trim());
                if (user != null) {
                    sb.append(" AND  voicLog.user_id = '" + user.getId() + "'");
                } else {
                    // 穿透查询一次登陆名称
                    user = this.customerUserDao.getCustomerUserByLoginName(realName.trim());
                    if (user != null) {
                        sb.append(" AND  voicLog.user_id = '" + user.getId() + "'");
                    } else {
                        return 0;
                    }
                }
            }

            if (StringUtil.isNotEmpty(remark)) {
                sb.append(" AND  voicLog.remark LIKE '%" + remark.trim() + "%'");
            }
            if (StringUtil.isNotEmpty(customerGroupId)) {
                // 处理任务类型
                CustomGroup customGroup = customGroupDao.get(Integer.parseInt(customerGroupId));
                if (customGroup != null && customGroup.getTaskType() != null) {
                    taskType = customGroup.getTaskType();
                }
                sb.append(" AND  voicLog.customer_group_id =" + customerGroupId);
            }
            if (StringUtil.isNotEmpty(superId)) {
                sb.append(" AND voicLog.superid ='" + superId.trim() + "'");
            }
            // 处理通话状态查询
            if (StringUtil.isNotEmpty(callStatus)) {
                // 成功
                if ("1".equals(callStatus)) {
                    sb.append(" AND voicLog.status = 1001");
                    //失败
                } else if ("2".equals(callStatus)) {
                    sb.append(" AND voicLog.status = 1002");
                } else {
                    sb.append(" AND voicLog.status = " + CallStatusEnum.getByType(NumberConvertUtil.parseInt(callStatus)));
                }
            }
            // 处理开始和结束时间查询
            if (StringUtil.isNotEmpty(createTimeStart) && StringUtil.isNotEmpty(createTimeEnd)) {
                sb.append(" AND voicLog.create_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
            } else {
                if (StringUtil.isNotEmpty(createTimeStart)) {
                    sb.append(" AND voicLog.create_time > '" + createTimeStart + "'");
                }
                if (StringUtil.isNotEmpty(createTimeEnd)) {
                    sb.append(" AND voicLog.create_time < '" + createTimeEnd + "'");
                }
            }
            // 处理机器人外呼任务营销记录
            if (3 == taskType) {
                // 处理机器人外呼的意向度
                if (StringUtil.isNotEmpty(level)) {
                    String levelLike = "\"level\":\"" + level + "\"";
                    sb.append(" AND voicLog.call_data LIKE '%" + levelLike + "%'");
                }
                // 处理按照操作人搜索营销记录时机器人外呼任务记录可以搜到
                if (user != null) {
                    sb.append(" AND (voicLog.user_id = '" + user.getId() + "' OR voicLog.call_data LIKE '%level%') ");
                }
                // 处理人工审核搜索条件
                if (StringUtil.isNotEmpty(auditingStatus)) {
                    sb.append(" AND voicLog.clue_audit_status = " + auditingStatus);
                }
            }
            // 处理组长权限
            if (UserService.OPERATOR_USER_TYPE.equals(loginUser.getUserType())) {
                // 组长查组员列表
                if ("1".equals(loginUser.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                    // 处理组长下有员工的情况
                    if (customerUserDTOList.size() > 0) {
                        Set<String> userIds = new HashSet<>();
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sb.append(" AND (voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR voicLog.call_data LIKE '%level%')");
                            } else {
                                sb.append(" AND voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sb.append(" AND (voicLog.user_id = '" + loginUser.getId() + "' OR voicLog.call_data LIKE '%level%')");
                        } else {
                            sb.append(" AND voicLog.user_id = '" + loginUser.getId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        sb.append(" AND (voicLog.user_id = '" + loginUser.getId() + "' OR voicLog.call_data LIKE '%level%')");
                    } else {
                        sb.append(" AND voicLog.user_id = '" + loginUser.getId() + "'");
                    }
                }
            }
            // 处理机器人外呼的意向度
            if (StringUtil.isNotEmpty(level)) {
                String levelLike = "\"level\":\"" + level + "\"";
                sb.append(" AND voicLog.call_data LIKE '%" + levelLike + "%'");
            }
            list = this.marketResourceDao.sqlQuery(sb.toString());
            if (list.size() > 0) {
                return Long.parseLong(String.valueOf(list.get(0).get("count")));
            } else {
                return 0;
            }
        } catch (Exception e) {
            LOG.error("获取通话记录总数失败,", e);
            return 0;
        }
    }


    public String getSendMessage(String templateId) {
        String sql = "select mould_content from t_template where id=?";
        String str = null;
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(sql, templateId);
        if (list.size() > 0) str = String.valueOf(list.get(0).get("mould_content"));

        String sms_signatures_sql = "select sms_signatures from t_template where id=?";
        String sms_signatures = null;
        List list2 = marketResourceDao.sqlQuery(sms_signatures_sql, templateId);
        if (list2.size() > 0) sms_signatures = String.valueOf(list2.get(0));
        sms_signatures = "【" + sms_signatures + "】" + str;
        return sms_signatures;
    }


    public boolean updateCallBack(Map<String, String> mapCallerCdr, Map<String, String> mapCalledCdr, Map<String, String> maprecordurl,
                                  String returnXml) {
        String updateSql = "update t_callback_info set returnXml=?,flag=2, remark=?, userData=?, subId=?, Callercaller=?, Callercalled=?, Callerstarttime=?, Callerendtime=?, Callerduration=?, CallerdurationBym=?, CallerbeginCallTime=?, CallerringingBeginTime=?, CallerringingEndTime=?, Callerbyetype=?, Calledcaller=?, Calledcalled=?, Calledstarttime=?, Calledendtime=?, Calledduration=?, CalleddurationBym=?, CalledbeginCallTime=?, CalledringingBeginTime=?, CalledringingEndTime=?, superId=?, userId=?,recordurl=? where callSid=?";
        String superId = mapCallerCdr.get("userData").split("_")[0];
        String cust_id = mapCallerCdr.get("userData").split("_")[3];
        // 唯一标识
        String tranOrderId = mapCallerCdr.get("userData").split("_")[2];
        String userId = mapCallerCdr.get("userData").split("_")[1];
        final String ThreaduserId = userId;
        String userData = mapCallerCdr.get("userData");
        String subId = mapCallerCdr.get("subId");
        String Callercaller = mapCallerCdr.get("caller");
        String Callercalled = mapCallerCdr.get("called");
        String Callerstarttime = mapCallerCdr.get("starttime");
        String recordurl = maprecordurl.get("recordurl");
        final String Threadrecordurl = recordurl;
        SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
        SimpleDateFormat dfnew = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");

        try {
            if (Callerstarttime == null || "".equals(Callerstarttime)) {
                Callerstarttime = null;
            } else {
                Date date = df.parse(Callerstarttime);
                Callerstarttime = dfnew.format(date);
            }
            String Callerendtime = mapCallerCdr.get("endtime");
            if (Callerendtime == null || "".equals(Callerendtime)) {
                Callerendtime = null;
            } else {
                Date date = df.parse(Callerendtime);
                Callerendtime = dfnew.format(date);
            }
            String Callerduration = mapCallerCdr.get("duration");
            String CallerdurationBym = "0";
            if (Callerduration != null && !"".equals(Callerduration)) {
                if (Integer.parseInt(Callerduration) % 60 == 0) {
                    CallerdurationBym = "" + Integer.parseInt(Callerduration) / 60;
                } else {
                    CallerdurationBym = "" + ((Integer.parseInt(Callerduration) / 60) + 1);
                }
            }

            String CallerbeginCallTime = mapCallerCdr.get("beginCallTime");
            if (CallerbeginCallTime == null || "".equals(CallerbeginCallTime)) {
                CallerbeginCallTime = null;
            } else {
                Date date = df.parse(CallerbeginCallTime);
                CallerbeginCallTime = dfnew.format(date);
            }
            String CallerringingBeginTime = mapCallerCdr.get("ringingBeginTime");
            if (CallerringingBeginTime == null || "".equals(CallerringingBeginTime)) {
                CallerringingBeginTime = null;
            } else {
                Date date = df.parse(CallerringingBeginTime);
                CallerringingBeginTime = dfnew.format(date);
            }
            String CallerringingEndTime = mapCallerCdr.get("ringingEndTime");
            if (CallerringingEndTime == null || "".equals(CallerringingEndTime)) {
                CallerringingEndTime = null;
            } else {
                Date date = df.parse(CallerringingEndTime);
                CallerringingEndTime = dfnew.format(date);
            }
            String Callerbyetype = mapCallerCdr.get("byetype");
            String Calledcaller = mapCalledCdr.get("caller");
            String Calledcalled = mapCalledCdr.get("called");
            String Calledstarttime = mapCalledCdr.get("starttime");
            if (Calledstarttime == null || "".equals(Calledstarttime)) {
                Calledstarttime = null;
            } else {
                Date date = df.parse(Calledstarttime);
                Calledstarttime = dfnew.format(date);
            }
            String Calledendtime = mapCalledCdr.get("endtime");
            if (Calledendtime == null || "".equals(Calledendtime)) {
                Calledendtime = null;
            } else {
                Date date = df.parse(Calledendtime);
                Calledendtime = dfnew.format(date);
            }
            String Calledduration = mapCalledCdr.get("duration");
            String CalleddurationBym = "0";
            if (Calledduration != null && !"".equals(Calledduration)) {
                if (Integer.parseInt(Calledduration) % 60 == 0) {
                    CalleddurationBym = "" + Integer.parseInt(Calledduration) / 60;
                } else {
                    CalleddurationBym = "" + ((Integer.parseInt(Calledduration) / 60) + 1);
                }
            }

            String CalledbeginCallTime = mapCalledCdr.get("beginCallTime");
            if (CalledbeginCallTime == null || "".equals(CalledbeginCallTime)) {
                CalledbeginCallTime = null;
            } else {
                Date date = df.parse(CalledbeginCallTime);
                CalledbeginCallTime = dfnew.format(date);
            }
            String CalledringingBeginTime = mapCalledCdr.get("ringingBeginTime");
            if (CalledringingBeginTime == null || "".equals(CalledringingBeginTime)) {
                CalledringingBeginTime = null;
            } else {
                Date date = df.parse(CalledringingBeginTime);
                CalledringingBeginTime = dfnew.format(date);
            }
            String CalledringingEndTime = mapCalledCdr.get("ringingEndTime");
            if (CalledringingEndTime == null || "".equals(CalledringingEndTime)) {
                CalledringingEndTime = null;
            } else {
                Date date = df.parse(CalledringingEndTime);
                CalledringingEndTime = dfnew.format(date);
            }
            if (recordurl == null || "".equals(recordurl)) {
                recordurl = null;
            } else {
                new Thread() {
                    public void run() {
                        try {
                            sleep(10000);
                        } catch (InterruptedException e) {
                            System.out.println("下载音频时暂停10秒异常");
                        }
                        String fileName = Threadrecordurl.substring(Threadrecordurl.lastIndexOf("/"));
                        String filePath = PropertiesUtil.getStringValue("audiolocation") + ThreaduserId;
                        File file = new File(filePath);
                        if (!file.exists()) {
                            file.mkdir();
                        }
                        boolean flag = saveUrlAs(Threadrecordurl, filePath + File.separator + fileName);
                        if (flag) {
                            //做音频同步
                            String src = filePath + File.separator + fileName; // 本地文件名
                            String dst = PropertiesUtil.getStringValue("destaudiolocation") + ThreaduserId + File.separator + fileName; // 目标文件名
                            SFTPChannel channel = new SFTPChannel();
                            try {

                                ChannelSftp chSftp = channel.getChannel(filePath + "/", 60000);
                                chSftp.put(src, dst, ChannelSftp.OVERWRITE); // 代码段2sPath
                                chSftp.quit();
                                channel.closeChannel();
                            } catch (Exception e) {
                                System.out.println("同步音频服务器失败：" + src);
                            }
                        }
                        System.out.println("Run ok!/n<BR>Get URL file " + flag);
                    }
                }.start();
            }
            String callSid = mapCallerCdr.get("callSid");
            int flag = this.marketResourceDao.executeUpdateSQL(updateSql, returnXml, "成功", userData, subId, Callercaller, Callercalled,
                    Callerstarttime, Callerendtime, Callerduration, CallerdurationBym, CallerbeginCallTime,
                    CallerringingBeginTime, CallerringingEndTime, Callerbyetype, Calledcaller, Calledcalled,
                    Calledstarttime, Calledendtime, Calledduration, CalleddurationBym, CalledbeginCallTime,
                    CalledringingBeginTime, CalledringingEndTime, superId, userId, recordurl, callSid);

        } catch (ParseException e) {
            // 异常处理
            String errorUpdatreSql = "update t_callback_info set  returnXml=?,flag=3,remark='返回参数异常或入库异常' where callSid=?";
            int flag = this.marketResourceDao.executeUpdateSQL(errorUpdatreSql, returnXml, mapCallerCdr.get("callSid"));
        }
        return true;
    }

    public static boolean saveUrlAs(String photoUrl, String fileName) {
        //此方法只能用户HTTP协议
        try {
            URL url = new URL(photoUrl);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            DataInputStream in = new DataInputStream(connection.getInputStream());
            DataOutputStream out = new DataOutputStream(new FileOutputStream(fileName));
            byte[] buffer = new byte[4096];
            int count = 0;
            while ((count = in.read(buffer)) > 0) {
                out.write(buffer, 0, count);
            }
            out.close();
            in.close();
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    public String updateResource(String type, String code, String cust_id, String user_id, String status, String sms_content,
                                 String superid, String templateId, String enpterprise_name, Long batch_number, int size) {
        JSONObject JSON = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        int flagUpdate = 0;
        int flagInsert = 0;
        // 判断金额
        if (type.equals("3")) {
            // 根据资源信息计算所需要的money
            String moneySql = "SELECT resname,sale_price,cost_price,supplier_id FROM t_market_resource where  resource_id=1 and type_code=2 ";
            List<Map<String, Object>> groupConfigList = this.marketResourceDao.sqlQuery(moneySql);
            Double sale_price = Double.parseDouble(groupConfigList.get(0).get("sale_price").toString());
            BigDecimal money = new BigDecimal(sale_price);
            money = money.multiply(new BigDecimal(1));// 默认先一对一计算
            // 账户扣钱（余额支付）查询账户余额
            String amountSql = "SELECT remain_amount FROM	 t_account where cust_id=?";
            Double remain_amount = Double.parseDouble(marketResourceDao.queryForObject(amountSql, cust_id));
            BigDecimal amount = new BigDecimal(remain_amount);
            if (amount.subtract(money).compareTo(new BigDecimal(0)) < 0) {
                // 失败
                return "1";
            } else {
                return "0";
            }
        }
        if (type.equals("1")) {
            // 根据资源信息计算所需要的money
            String moneySql = "SELECT resname,sale_price,cost_price,supplier_id FROM t_market_resource where  resource_id=1 and type_code=2 ";
            List<Map<String, Object>> groupConfigList = this.marketResourceDao.sqlQuery(moneySql);
            String resname = groupConfigList.get(0).get("resname").toString();
            Double sale_price = Double.parseDouble(groupConfigList.get(0).get("sale_price").toString());
            Double cost_price = Double.parseDouble(groupConfigList.get(0).get("cost_price").toString());
            int supplier_id = Integer.parseInt(groupConfigList.get(0).get("supplier_id").toString());
            BigDecimal money = new BigDecimal(sale_price);
            money = money.multiply(new BigDecimal(size));// 默认先一对一计算
            // 账户扣钱（余额支付）查询账户余额
            String amountSql = "SELECT remain_amount FROM	 t_account where cust_id=?";
            Double remain_amount = Double.parseDouble(marketResourceDao.queryForObject(amountSql, cust_id));

            DecimalFormat df = new DecimalFormat("0.00");
            BigDecimal amount = new BigDecimal(remain_amount);
            String now_money = df.format(amount.subtract(money));
            // 更新账户信息（余额扣款）
            String amountUpdateSql = "update t_account  set remain_amount =?,modify_time=now()  where cust_id=?";
            this.marketResourceDao.executeUpdateSQL(amountUpdateSql, now_money, cust_id);
            // 查询账户ID
            String accountId = "SELECT acct_id FROM  t_account WHERE  cust_id = ?";
            accountId = null;
            List list2 = marketResourceDao.sqlQuery(accountId, cust_id);
            if (list2.size() > 0) accountId = String.valueOf(list2.get(0));
            // t_transaction
            // dto.getTransaction_code() '交易类型（1.充值 2.扣减 3.消费）'
            // dto.getPay_type() 支付类型（1.余额 2.第三方 3.线下）'
            Long transaction_id = IDHelper.getTransactionId();
            Long orderNo = IDHelper.getTransactionId();
            String transactionSql = "insert into t_transaction set transaction_id=?,acct_id=?,cust_id=?,type=2,pay_mode=1,amount=?,remark='短信扣费',create_time=now(),order_id =?";
            marketResourceDao.executeUpdateSQL(transactionSql, transaction_id, accountId, cust_id, money, orderNo);
            // 生成订单
            String orderSql = "insert into t_order set order_id=?,cust_id=?,pay_type=1,create_time=now(),pay_time=now(),quantity=?,amount=?,order_state=2,supplier_id=?,remarks='短信扣费',order_type='2',product_name=?,enpterprise_name=?,cost_price=?,pay_amount=? ";
            marketResourceDao.executeUpdateSQL(orderSql, orderNo, cust_id, size, money, supplier_id, resname, enpterprise_name, cost_price * size, money);
        }
        // 记录log
        if (type.equals("0")) {
            String insertSql = "insert into t_touch_sms_log set cust_id=?,user_id=?,create_time=now(),remark=?,STATUS=?,sms_content=?,superid=?,templateId=?,batch_number=?";
            flagInsert = this.marketResourceDao.executeUpdateSQL(insertSql, cust_id, user_id, status, code, sms_content, superid, templateId, batch_number);
        }

        return JSON.toJSONString();
    }


    public boolean insertTouchInfo(TouchInfoDTO dto) {
        StringBuffer sql = new StringBuffer();

        boolean judge = false;
        try {
            sql.append("INSERT INTO `t_touch_voice_info` (`voice_info_id`, `cust_id`, `user_id`, `cust_group_id`, `super_id`, `create_time`, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`," +
                    " `super_address_province_city`, `super_address_street`) VALUES ( ");
            sql.append("'" + dto.getVoice_info_id() + "',");
            sql.append("'" + dto.getCust_id() + "',");
            sql.append("'" + dto.getUser_id() + "',");
            sql.append("'" + dto.getCust_group_id() + "',");
            sql.append("'" + dto.getSuper_id() + "',");

            sql.append("now(),");

            sql.append("'" + dto.getSuper_name() + "',");
            sql.append("'" + dto.getSuper_age() + "',");
            sql.append("'" + dto.getSuper_sex() + "',");
            sql.append("'" + dto.getSuper_telphone() + "',");
            sql.append("'" + dto.getSuper_phone() + "',");
            sql.append("'" + dto.getSuper_address_province_city() + "',");
            sql.append("'" + dto.getSuper_address_street() + "')");

            marketResourceDao.insertLog(sql.toString());
            judge = true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return judge;
    }


    public boolean updateTouchInfo(TouchInfoDTO dto) {
        StringBuffer sql = new StringBuffer();
        boolean judge = false;
        try {
            sql.append("SELECT voice_info_id FROM t_touch_voice_info WHERE voice_info_id = ?");
            List<Map<String, Object>> list = this.marketResourceDao.sqlQuery(sql.toString(), dto.getVoice_info_id());
            if (list != null && list.size() > 0) {
                sql.setLength(0);
                sql.append(
                        "UPDATE t_touch_voice_info SET ");
                sql.append(" voice_info_id = ?, ");
                sql.append(" cust_id= ?, ");
                sql.append(" user_id= ?, ");
                sql.append(" cust_group_id= ?, ");
                sql.append(" super_id= ?, ");
                sql.append(" super_name= ?, ");
                sql.append(" super_age= ?, ");
                sql.append(" super_sex= ?, ");
                sql.append(" super_telphone= ?, ");
                sql.append(" super_phone= ?, ");
                sql.append(" super_address_province_city= ?, ");
                sql.append(" super_address_street= ? ");
                sql.append(" WHERE voice_info_id = ?");
                this.marketResourceDao.executeUpdateSQL(sql.toString(), dto.getVoice_info_id(), dto.getCust_id(), dto.getUser_id(), dto.getCust_group_id(),
                        dto.getSuper_id(), dto.getSuper_name(), dto.getSuper_age(), dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                        dto.getSuper_address_province_city(), dto.getSuper_address_street(), dto.getVoice_info_id());
            } else {
                insertTouchInfo(dto);
            }
            judge = true;
        } catch (Exception e) {
            judge = false;
            LOG.error("更新t_touch_voice_info的信息失败", e);
        }
        return judge;
    }

    /**
     * 更新客户群数据表的客户基本信息
     *
     * @param dto
     * @return
     */
    public boolean updateTouchInfoV3(TouchInfoDTO dto) {
        StringBuffer sql = new StringBuffer();
        boolean judge = false;
        try {
            LOG.info("开始更新客户群数据表个人信息:" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + ",数据:" + dto.toString());
            sql.append("UPDATE " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + " SET ");
            sql.append(" super_name= ?, ");
            sql.append(" super_age= ?, ");
            sql.append(" super_sex= ?, ");
            sql.append(" super_telphone= ?, ");
            sql.append(" super_phone= ?, ");
            sql.append(" super_address_province_city= ?, ");
            sql.append(" super_address_street = ? ");
            sql.append(" WHERE id = ? ");

            this.marketResourceDao.executeUpdateSQL(sql.toString(), dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), dto.getSuper_id());

            if (StringUtil.isNotEmpty(dto.getMarket_task_id())) {
                LOG.info("开始更新营销任务 " + dto.getMarket_task_id() + " 数据表个人信息:" + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + dto.getMarket_task_id() + ",数据:" + dto.toString());
                sql = new StringBuffer();
                sql.append("UPDATE " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + dto.getMarket_task_id() + " SET ");
                sql.append(" super_name= ?, ");
                sql.append(" super_age= ?, ");
                sql.append(" super_sex= ?, ");
                sql.append(" super_telphone= ?, ");
                sql.append(" super_phone= ?, ");
                sql.append(" super_address_province_city= ?, ");
                sql.append(" super_address_street = ? ");
                sql.append(" WHERE id = ? ");

                this.marketResourceDao.executeUpdateSQL(sql.toString(), dto.getSuper_name(), dto.getSuper_age(),
                        dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                        dto.getSuper_address_province_city(), dto.getSuper_address_street(), dto.getSuper_id());
            }
            judge = true;
        } catch (Exception e) {
            judge = false;
            LOG.error("更新客户群数据表个人信息" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + "的个人信息信息失败", e);
        }
        return judge;
    }

    /**
     * 更新客户群数据表的所选自建属性信息
     *
     * @param custGroupId
     * @param superId
     * @param labelData
     * @return
     */
    public boolean updateTouchLabelDataV3(String userId, String custGroupId, String superId, Map<String, Object> labelData, String taskId) {
        StringBuffer sql = new StringBuffer();
        boolean judge = false;
        try {
            LOG.info("开始更新客户群数据表个人信息:" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + custGroupId);
            sql.setLength(0);
            sql.append("UPDATE " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + custGroupId + " SET ");
            sql.append(" super_data = ? ");
            sql.append(" WHERE id = ? ");
            this.marketResourceDao.executeUpdateSQL(sql.toString(), JSON.toJSONString(labelData), superId);

            if (StringUtil.isNotEmpty(taskId)) {
                LOG.info("开始更新营销任务数据表个人信息:" + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + taskId);
                sql.setLength(0);
                sql.append("UPDATE " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + taskId + " SET ");
                sql.append(" super_data = ? ");
                sql.append(" WHERE id = ? ");
                this.marketResourceDao.executeUpdateSQL(sql.toString(), JSON.toJSONString(labelData), superId);
            }

            judge = true;
        } catch (Exception e) {
            judge = false;
            LOG.error("更新客户群数据表个人信息" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + custGroupId + "的个人信息信息失败", e);
        }
        return judge;
    }


    public String addTransaction(String code, String cust_id, Integer callTime, String tranOrderId, String userId) {
        JSONObject JSON = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        int flagUpdate = 0;
        int flagInsert = 0;

        // 回调成功
        Integer status = 2001;

        String transaction_id = Long.toString(IDHelper.getTransactionId());

        //查询企业名称
//        String enNameSql = "SELECT enterprise_name  FROM  t_user WHERE  id = ? ";
//        String enName = marketResourceDao.queryForObject(enNameSql, userId);\
        Customer c = customerDao.get(cust_id);


        // 更新日志表
        LOG.info("==callback成功，更新日志表====");
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE  t_touch_voice_log SET ");
        sb.append(" STATUS=?");
        sb.append(" where touch_id = ?");
        this.marketResourceDao.executeUpdateSQL(sb.toString(), status, tranOrderId);

        if (code.equals("0")) {
            // 根据资源信息计算所需要的售价
            String moneySql = "SELECT resname,sale_price,cost_price,supplier_id FROM t_market_resource where  resource_id=2 and type_code=1 ";
            List<Map<String, Object>> groupConfigList = this.marketResourceDao.sqlQuery(moneySql);
            String resname =
                    groupConfigList.get(0).get("resname").toString();
            // 供应商ID
            String supplier_id = groupConfigList.get(0).get("supplier_id").toString();
            Double cost_price = Double.parseDouble(groupConfigList.get(0).get("cost_price").toString());
            Double sale_price = Double.parseDouble(groupConfigList.get(0).get("sale_price").toString());

            BigDecimal moneyCost = new BigDecimal(cost_price * callTime);
            moneyCost = moneyCost.multiply(new BigDecimal(1));

            BigDecimal moneySale = new BigDecimal(sale_price * callTime);
            moneySale = moneySale.multiply(new BigDecimal(1));

            // 查询账户ID
            String accountId = "SELECT acct_id FROM  t_account WHERE  cust_id = ?";
            List list1 = marketResourceDao.sqlQuery(accountId, cust_id);
            if (list1.size() > 0) accountId = String.valueOf(list1.get(0));


            // 账户扣钱（余额支付）查询账户余额
            String amountSql = "SELECT remain_amount FROM t_account where cust_id=?";
            Double remain_amount = Double.parseDouble(marketResourceDao.queryForObject(amountSql, cust_id));
            DecimalFormat df = new DecimalFormat("0.00");
            BigDecimal amount = new BigDecimal(remain_amount);
            // if (amount.subtract(money).compareTo(new BigDecimal(0)) < 0) {
            // map.put("code", "3");
            // map.put("message", "余额不足支付！");
            // }
            String now_money = df.format(amount.subtract(moneySale));
            // 更新账户信息（余额扣款）
            String amountUpdateSql = "update t_account  set remain_amount =?,modify_time=now()  where cust_id=?";
            flagUpdate = this.marketResourceDao.executeUpdateSQL(amountUpdateSql, now_money, cust_id);
            if (flagUpdate == 0) {
                map.put("code", "4");
                map.put("message", "支付失败！");
                LOG.info("==支付失败====");
            }

            LOG.info("==从账户余额扣款====" + amountUpdateSql);

            // 向交易表里面增加一条记录
            String insertSql = "INSERT INTO t_transaction(transaction_id,acct_id,cust_id,TYPE,pay_mode,amount,create_time,remark,order_id)VALUES(?,?,?,2,1,?,NOW(),'电话扣费',?)";
            flagInsert = this.marketResourceDao.executeUpdateSQL(insertSql, transaction_id, accountId, cust_id, moneySale, tranOrderId);
            LOG.info("==向交易表添加记录====" + insertSql);

            // 向t_order表增加交易记录
            Object[] objsOrder = {tranOrderId, cust_id, callTime, moneySale, moneyCost, supplier_id, moneySale, c == null ? "" : c.getEnterpriseName(), resname};
            String orderSql = "insert into t_order set order_id=?,cust_id=?,order_type=2,pay_type=1,create_time=now(),quantity=?,amount=?,order_state=2,cost_price=?,supplier_id=?,pay_amount=?,enpterprise_name=?,product_name =?,remarks='电话扣费',pay_time = NOW()";
            marketResourceDao.insertOrder(orderSql, objsOrder);
            LOG.info("==向t_order表添加记录====" + objsOrder);
        }

        if (flagInsert == 1 && flagUpdate == 1) {
            map.put("code", "0");
            map.put("message", "成功");
            LOG.info("====添加到交易表更新日志表成功====");
        } else {
            map.put("code", "1");
            map.put("message", "失败");
            LOG.info("======添加到交易表更新日志表失败====");
        }
        JSON.put("data", map);
        return JSON.toJSONString();
    }


    public int addTransactionByCallData(int type, String customerId, long callTime, String tranOrderId, String userId, String resourceId) {
        JSONObject JSON = new JSONObject();
        Map<Object, Object> map = new HashMap<>(16);
        int flagUpdate = 0;
        int flagInsert = 0;
        // 回调成功
        Integer status = 2001;
        String transactionId = Long.toString(IDHelper.getTransactionId());
        //查询企业名称
        String enNameSql = "SELECT enterprise_name  FROM  t_user WHERE  id = ? ";
        String enName = marketResourceDao.queryForObject(enNameSql, userId);
        // 更新日志表
        LOG.info("==callback成功，更新日志表====");
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE  t_touch_voice_log SET ");
        sb.append(" STATUS=?");
        sb.append(" where touch_id = ?");
        this.marketResourceDao.executeUpdateSQL(sb.toString(), status, tranOrderId);
        if (type == 1) {
            // 根据资源信息计算所需要的售价
            String moneySql = "SELECT resname,sale_price,cost_price,supplier_id FROM t_market_resource where  resource_id=? and type_code=1 ";
            List<Map<String, Object>> groupConfigList = this.marketResourceDao.sqlQuery(moneySql, resourceId);
            String resName =
                    groupConfigList.get(0).get("resname").toString();
            // 供应商ID
            String supplier_id = groupConfigList.get(0).get("supplier_id").toString();
            Double cost_price = Double.parseDouble(groupConfigList.get(0).get("cost_price").toString());
            Double sale_price = Double.parseDouble(groupConfigList.get(0).get("sale_price").toString());

            BigDecimal moneyCost = new BigDecimal(cost_price * callTime);
            moneyCost = moneyCost.multiply(new BigDecimal(1));

            BigDecimal moneySale = new BigDecimal(sale_price * callTime);
            moneySale = moneySale.multiply(new BigDecimal(1));

            // 查询账户ID
            String accountId = "SELECT acct_id FROM  t_account WHERE  cust_id = ?";
            accountId = marketResourceDao.queryForObject(accountId, customerId);

            // 账户扣钱（余额支付）查询账户余额
            String amountSql = "SELECT remain_amount FROM t_account where cust_id=?";
            Double remain_amount = Double.parseDouble(marketResourceDao.queryForObject(amountSql, customerId));
            DecimalFormat df = new DecimalFormat("0.00");
            BigDecimal amount = new BigDecimal(remain_amount);
            String now_money = df.format(amount.subtract(moneySale));
            // 更新账户信息（余额扣款）
            String amountUpdateSql = "update t_account  set remain_amount =?,modify_time=now()  where cust_id=?";
            flagUpdate = this.marketResourceDao.executeUpdateSQL(amountUpdateSql, now_money, customerId);
            if (flagUpdate == 0) {
                map.put("code", "4");
                map.put("message", "支付失败！");
                LOG.info("==支付失败====");
            }

            LOG.info("==从账户余额扣款====" + amountUpdateSql);

            // 向交易表里面增加一条记录
            String insertSql = "INSERT INTO t_transaction(transaction_id,acct_id,cust_id,TYPE,pay_mode,amount,create_time,remark,order_id)VALUES(?,?,?,2,1,?,NOW(),'电话扣费',?)";
            flagInsert = this.marketResourceDao.executeUpdateSQL(insertSql, transactionId, accountId, customerId, moneySale, tranOrderId);
            LOG.info("==向交易表添加记录====" + insertSql);

            // 向t_order表增加交易记录
            Object[] objsOrder = {tranOrderId, customerId, callTime, moneySale, moneyCost, supplier_id, moneySale, enName, resName};
            String orderSql = "insert into t_order set order_id=?,cust_id=?,order_type=2,pay_type=1,create_time=now(),quantity=?,amount=?,order_state=2,cost_price=?,supplier_id=?,pay_amount=?,enpterprise_name=?,product_name =?,remarks='电话扣费',pay_time = NOW()";
            marketResourceDao.insertOrder(orderSql, objsOrder);
            LOG.info("==向t_order表添加记录====" + objsOrder);
        }

        if (flagInsert == 1 && flagUpdate == 1) {
            LOG.info("====添加到交易表更新日志表成功====");
            return 1;
        } else {
            LOG.info("======添加到交易表更新日志表失败====");
            return 0;
        }
    }

    public String updateVoiceLog(String touchId, String remark) {
        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE  t_touch_voice_log SET ");
        sb.append(" remark=?");
        sb.append(" where touch_id = ?");
        this.marketResourceDao.executeUpdateSQL(sb.toString(), remark, touchId);
        return null;
    }

    public String updateVoiceLogV3(String touchId, String remark) {
        // 检查通话记录月表是否存在
        marketResourceDao.createVoiceLogTableNotExist(DateUtil.getNowMonthToYYYYMM());

        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + DateUtil.getNowMonthToYYYYMM() + " SET ");
        sb.append(" remark=?");
        sb.append(" where touch_id = ?");
        this.marketResourceDao.executeUpdateSQL(sb.toString(), remark, touchId);
        return null;
    }


    public int updateVoiceLogStatusV3(String touchId, Integer status, String callSid) {
        // 检查通话记录月表是否存在
        marketResourceDao.createVoiceLogTableNotExist(DateUtil.getNowMonthToYYYYMM());

        StringBuffer sb = new StringBuffer();
        sb.append("UPDATE " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + DateUtil.getNowMonthToYYYYMM() + " SET ");
        sb.append(" STATUS=?, ");
        sb.append(" callSid=?");
        sb.append(" where touch_id = ?");
        return this.marketResourceDao.executeUpdateSQL(sb.toString(), status, callSid, touchId);
    }


    public String selectPhoneSet(long userId) {
        String workNumSet = "000";
        //CustomerUserProperty workNumStatus = customerUserDao.getProperty(String.valueOf(userId), "work_num_status");
        // 主叫号码已经设置并且审核通过
        //if (workNumStatus != null && "1".equals(workNumStatus.getPropertyValue())) {
        CustomerUserPropertyDO workNum = customerUserDao.getProperty(String.valueOf(userId), "work_num");
        if (workNum != null) {
            workNumSet = workNum.getPropertyValue();
        }
        // }
        return workNumSet;
    }

    /**
     * 查询企业外显号码
     *
     * @param custId
     * @return
     */
    public String selectCustCallBackApparentNumber(String custId) {
        String apparentNumber = "";
        CustomerProperty callBackApparentNumber = customerDao.getProperty(custId, "call_back_apparent_number");
        if (callBackApparentNumber != null) {
            apparentNumber = callBackApparentNumber.getPropertyValue();
        }
        return apparentNumber;
    }


    public boolean checkAmount(String cust_id, int size, String resource_id, String type_code, String supplier_id) {
        String moneySql = "SELECT sale_price FROM t_market_resource where  resource_id=? and type_code=? and supplier_id=?";
        String sale_price = marketResourceDao.queryForObject(moneySql, resource_id, type_code, supplier_id);
        BigDecimal money = new BigDecimal(sale_price);
        money = money.multiply(new BigDecimal(size));// 默认先一对一计算
        String acctsql = "SELECT acct_id FROM	 t_account where cust_id=?";
        String acct_id = marketResourceDao.queryForObject(acctsql, cust_id);
        // 账户扣钱（余额支付）查询账户余额
        String amountSql = "SELECT remain_amount FROM t_account where acct_id=?";
        String remain_amount = marketResourceDao.queryForObject(amountSql, acct_id);
        BigDecimal amount = new BigDecimal(remain_amount);
        if (amount.subtract(money).compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        return true;
    }

    public boolean checkAmount0(String custId, int size, String templateId) {
        LOG.info("开始发送短信并且扣费,templateId:" + templateId);
        // 查询短信模板
        MarketTemplate marketTemplate = marketResourceDao.getMarketTemplate(NumberConvertUtil.parseInt(templateId), 1, custId);
        if (marketTemplate == null) {
            LOG.info("发送短信marketTemplate为空:" + marketTemplate);
            return false;
        }
        LOG.info("发送短信marketTemplate:" + marketTemplate);
        String resourceId = marketTemplate.getResourceId();
        LOG.info("发送短信resourceId:" + resourceId);
        if (StringUtil.isEmpty(resourceId)) {
            LOG.warn("发送短信未查询到模板对应的resourceId,templateId:" + templateId + ",custId:" + custId);
            return false;
        }

        // 查询客户配置的短信渠道
        CustomerProperty sms_config = customerDao.getProperty(custId, "sms_config");
        LOG.info("验证码发送记录客户售价配置:" + sms_config);
        if (sms_config == null || (sms_config != null && StringUtil.isEmpty(sms_config.getPropertyValue()))) {
            LOG.warn("发送短信未查询客户配置的短信资源resourceId" + resourceId + ",templateId:" + templateId + ",custId:" + custId);
            return false;
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
            LOG.warn("发送短信未查询到对应的客户短信配置,resourceId:" + resourceId + ",sms_config:" + sms_config);
            return false;
        }
        ResourcePropertyEntity marketResourceProperty = marketResourceDao.getProperty(resourceId, "price_config");
        if (marketResourceProperty != null && StringUtil.isNotEmpty(marketResourceProperty.getPropertyValue())) {
            supplierConfig = JSONObject.parseObject(marketResourceProperty.getPropertyValue());
        }
        String supplierId = null;
        // 供应商短信扣费
        List<Map<String, Object>> marketResource = customerDao.sqlQuery("SELECT * FROM t_market_resource WHERE resource_id = ? AND `status` = 1", resourceId);
        if (marketResource != null && marketResource.size() > 0) {
            supplierId = String.valueOf(marketResource.get(0).get("supplier_id"));
        }

        //计算批量发送短信需要的金额
        BigDecimal money = new BigDecimal(custSmsConfig.getDoubleValue("price"));
        money = money.multiply(new BigDecimal(size));// 默认先一对一计算
        // 账户余额
        CustomerProperty remainAmount = customerDao.getProperty(custId, "remain_amount");

        BigDecimal amount = new BigDecimal(remainAmount.getPropertyValue());
        if (amount.subtract(money).compareTo(new BigDecimal(0)) < 0) {
            return false;
        }
        return true;
    }


    public String getMarketResourceInfoV1(String userType, String cust_id, String user_id) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> userList = null;
        List<Map<String, Object>> resourceList = null;
        String smsList = null;
        String emialList = null;
        String telList = null;
        List<Map<String, Object>> moneyList = null;
        String teamCount = null;
        long voiceCount = 0;
        String emailCount = null;
        String smsCount = null;
        try {
            // 查询客户概览
            StringBuffer userSql = new StringBuffer();
            userList = new ArrayList<>();
            userSql.append(" SELECT COUNT(id) AS totalGroup, SUM(user_count) AS toatlUser ");
            userSql.append(" FROM customer_group");
            userSql.append(" WHERE cust_id =? AND `STATUS`=1");
            userSql.append(" GROUP BY cust_id;");
            userList = this.marketResourceDao.sqlQuery(userSql.toString(), cust_id);

            // 营销资源信息
            String resourceSql = "select tt.type_code type,tt.resname name, t.remain quantity from t_resource_account t left join t_market_resource tt on t.resource_id=tt.resource_id  where cust_id=? ORDER BY tt.type_code";
            // 营销设置
            String smsResource = "SELECT COUNT(*) as countSms from t_template where cust_id=? AND `STATUS`=2 AND type_code=1";
            String emailResource = "SELECT COUNT(*) as countEmail from t_template where cust_id=? AND `STATUS`=2 AND type_code=2";
            String telResource = "select count(*) as countTel from t_user where work_num_status is null and cust_id=?";
            // 资金信息
            //String moneySql = "SELECT FORMAT(remain_amount / 100,2) AS remain_amount,FORMAT(credit_limit / 100,2) AS credit_limit from t_account where cust_id=?";
            //团队管理
            String teamSql = "SELECT count(*)-1 as teamCount  from t_customer_user where cust_id=? AND `STATUS`=0 ";
            //营销记录查询
            String sms = "";
            String email = "";

            //企业用户
            email = "SELECT count(*) as emailCount from t_template where cust_id='" + cust_id + "' AND type_code = 2";
            sms = "SELECT count(*) as smsCount from t_template where cust_id='" + cust_id + "' AND type_code = 1 ";

            // 查询
            resourceList = this.marketResourceDao.sqlQuery(resourceSql, cust_id);
            smsList = marketResourceDao.queryForObject(smsResource, cust_id);
            emialList = marketResourceDao.queryForObject(emailResource, cust_id);
            telList = marketResourceDao.queryForObject(telResource, cust_id);
            //余额查询分
            Double remainAmount = 0.0;
            CustomerProperty ra = customerDao.getProperty(cust_id, "remain_amount");
            try {
                if (ra != null) {
                    remainAmount = Double.parseDouble(ra.getPropertyValue());
                }
            } catch (Exception e) {
                LOG.error("get balance error", e);
            }
            moneyList = new ArrayList<>();
            Map<String, Object> remainAmoutMap = new HashMap<>();
            DecimalFormat df = new DecimalFormat("######0.00");
            remainAmoutMap.put("remain_amount", df.format(remainAmount / 100));
            moneyList.add(remainAmoutMap);
            teamCount = "";
            if ("1".equals(userType)) {
                teamCount = marketResourceDao.queryForObject(teamSql, cust_id);
            }
            String sql = "select CAST(id AS CHAR) id from t_customer_user m where cust_id='" + cust_id + "' ";
            List<Map<String, Object>> customerUserList = this.marketResourceDao.sqlQuery(sql);
            CustomerUserPropertyDO workNum;
            voiceCount = customerUserList.size();
            for (int i = 0; i < customerUserList.size(); i++) {
                workNum = customerUserDao.getProperty(String.valueOf(customerUserList.get(i).get("id")), "work_num");
                if (workNum != null && StringUtil.isNotEmpty(workNum.getPropertyValue())) {
                    voiceCount--;
                }
            }
            emailCount = marketResourceDao.queryForObject(email);
            smsCount = marketResourceDao.queryForObject(sms);
        } catch (Exception e) {
            LOG.error("获取营销统计数据出错,", e);
        } finally {
            // setMap
            map.put("userList", userList);
            map.put("resourceList", resourceList);
            map.put("telList", telList);
            map.put("emialList", emialList);
            map.put("smsList", smsList);
            map.put("moneyList", moneyList);
            map.put("teamCount", teamCount);
            map.put("voiceCountRes", voiceCount);
            map.put("emailCountRes", emailCount);
            map.put("smsCountRes", smsCount);
            json.put("data", map);
        }

        return json.toJSONString();
    }


    public Map<String, Object> countMarketDataV3(int timeType, LoginUser loginUser, String startTime, String endTime, String userGroupId, String userId) {
        Map<String, Object> data = new HashMap<>();
        // 处理时间
        if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
            LocalDate localDate = null;
            //今天
            if (timeType == 1) {
                localDate = LocalDate.now();
                //昨天
            } else if (timeType == 2) {
                localDate = LocalDate.now().minusDays(1);
            }
            startTime = LocalDateTime.of(localDate, LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            endTime = LocalDateTime.of(localDate, LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        long callSum = 0;
        long calledSum = 0;
        long calledSumTime = 0;
        String calledPercent = "0";
        String calledAvgTime = "0";
        long intenCustomerSum = 0;
        int userGroupSum = 0;
        int userSum = 0;
        int historyMarketTask = 0;
        int newMarketTask = 0;
        double accountBalance = 0;

        data.put("callSum", callSum);
        data.put("calledSum", calledSum);
        data.put("calledPercent", calledPercent);
        data.put("calledSumTime", 0);
        data.put("calledAvgTime", 0);
        data.put("intenCustomerSum", 0);

        // 历史营销任务
        String historyMarketSql = "SELECT COUNT(t1.id) count FROM customer_group t1 RIGHT JOIN t_order t2 ON t1.order_id = t2.order_id WHERE t1.status = 1 AND t1.task_type IS NOT NULL AND t1.cust_id = ?";
        List<Map<String, Object>> historyMarketList = marketResourceDao.sqlQuery(historyMarketSql, loginUser.getCustId());
        if (historyMarketList.size() > 0) {
            historyMarketTask = NumberConvertUtil.parseInt(String.valueOf(historyMarketList.get(0).get("count")));
        }
        // 待创建营销任务
        String newMarketTaskSql = "SELECT COUNT(t1.id) count FROM customer_group t1 RIGHT JOIN t_order t2 ON t1.order_id = t2.order_id WHERE t1.status = 1 AND t1.task_type IS NULL AND t1.cust_id = ?";
        List<Map<String, Object>> newMarketTaskList = marketResourceDao.sqlQuery(newMarketTaskSql, loginUser.getCustId());
        if (newMarketTaskList.size() > 0) {
            newMarketTask = NumberConvertUtil.parseInt(String.valueOf(newMarketTaskList.get(0).get("count")));
        }

        // 查询分组数量
        if ("1".equals(loginUser.getUserType())) {
            CustomerUserGroup userGroupEntity = new CustomerUserGroup();
            userGroupEntity.setCustId(loginUser.getCustId());
            List<CustomerUserGroup> customerUserGroupList = customerUserDao.listCustomerUserGroup(userGroupEntity);
            userGroupSum = customerUserGroupList.size();
        }

        // 查询员工数量
        if ("1".equals(loginUser.getUserType())) {
            String teamSql = "SELECT count(*) as count from t_customer_user where cust_id=? AND `STATUS`=0 AND user_type = 2";
            List<Map<String, Object>> teamList = marketResourceDao.sqlQuery(teamSql, loginUser.getCustId());
            if (teamList.size() > 0) {
                userSum = NumberConvertUtil.parseInt(String.valueOf(teamList.get(0).get("count")));
            }
        } else {
            if ("1".equals(loginUser.getUserGroupRole())) {
                List<CustomerUserDTO> userGroupList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                if (userGroupList.size() > 0) {
                    userSum = userGroupList.size();
                }
            }
        }

        DecimalFormat df = new DecimalFormat("######0.00");
        if ("1".equals(loginUser.getUserType())) {
            //余额查询分
            CustomerProperty ra = customerDao.getProperty(loginUser.getCustId(), "remain_amount");
            accountBalance = NumberConvertUtil.parseDouble(ra.getPropertyValue());
        }
        data.put("userGroupSum", userGroupSum);
        data.put("userSum", userSum);
        data.put("historyMarketTask", historyMarketTask);
        data.put("newMarketTask", newMarketTask);
        data.put("accountBalance", df.format(accountBalance / 100));

        // 查询呼叫统计类数据
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SUM(call_sum) call_sum, SUM(called_sum) called_sum, SUM(called_duration) called_duration FROM stat_call_day WHERE cust_id = ? AND create_time BETWEEN ? AND ? ");
        List<Map<String, Object>> list = null;
        List<CustomerUserDTO> selectUserList = null;
        Set<String> groupUserIds = null;
        // 处理管理员权限
        if ("1".equals(loginUser.getUserType())) {
            if (StringUtil.isNotEmpty(userId)) {
                sql.append(" AND user_id = '" + StringEscapeUtils.escapeSql(userId) + "'");
            } else if (StringUtil.isNotEmpty(userGroupId)) {
                selectUserList = customerUserDao.listSelectCustomerUserByUserGroupId(userGroupId, loginUser.getCustId());
                if (selectUserList.size() > 0) {
                    groupUserIds = new HashSet<>();
                    for (CustomerUserDTO customerUserDTO : selectUserList) {
                        groupUserIds.add(customerUserDTO.getId());
                    }
                    sql.append(" AND user_id IN (" + SqlAppendUtil.sqlAppendWhereIn(groupUserIds) + ")");
                } else {
                    return data;
                }

            }
        } else {
            // 处理组长权限
            if ("1".equals(loginUser.getUserGroupRole())) {
                // 查询单个用户
                if (StringUtil.isNotEmpty(userId)) {
                    sql.append(" AND user_id = '" + StringEscapeUtils.escapeSql(userId) + "'");
                } else { // 查询组下所有人员数据
                    selectUserList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                    if (selectUserList.size() > 0) {
                        groupUserIds = new HashSet<>();
                        for (CustomerUserDTO customerUserDTO : selectUserList) {
                            groupUserIds.add(customerUserDTO.getId());
                        }
                        sql.append(" AND user_id IN (" + SqlAppendUtil.sqlAppendWhereIn(groupUserIds) + ")");
                    } else {
                        return data;
                    }

                }
            } else if ("2".equals(loginUser.getUserGroupRole())) {// 处理普通用户权限
                sql.append(" AND user_id = '" + StringEscapeUtils.escapeSql(String.valueOf(loginUser.getId())) + "'");
            } else {
                // 处理未配置组员的情况
                sql.append(" AND user_id = '" + StringEscapeUtils.escapeSql(String.valueOf(loginUser.getId())) + "'");
            }
        }
        list = marketResourceDao.sqlQuery(sql.toString(), loginUser.getCustId(), startTime, endTime);
        if (list.size() > 0) {
            for (Map<String, Object> map : list) {
                if (map != null) {
                    if (map.get("call_sum") != null) {
                        callSum = NumberConvertUtil.parseLong(String.valueOf(map.get("call_sum")));
                    }
                    if (map.get("called_sum") != null) {
                        calledSum = NumberConvertUtil.parseLong(String.valueOf(map.get("called_sum")));
                    }
                    if (map.get("called_duration") != null) {
                        calledSumTime = NumberConvertUtil.parseLong(String.valueOf(map.get("called_duration")));
                    }
                }
            }
        }

        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        // 接通率
        if (callSum > 0) {
            calledPercent = numberFormat.format((float) calledSum / (float) callSum * 100);
        }
        // 总通话时长(小时)
        String calledSumTimeHour = numberFormat.format((float) calledSumTime / 3600);
        // 平均通话时长(分钟)
        long calledAvgSecond = 0L;
        BigDecimal bigDecimal;
        if (calledSum > 0) {
            bigDecimal = new BigDecimal((double) calledSumTime / calledSum);
            calledAvgSecond = bigDecimal.setScale(0, RoundingMode.CEILING).longValue();
        }

        data.put("callSum", callSum);
        data.put("calledSum", calledSum);
        data.put("calledPercent", calledPercent);
        data.put("calledSumTime", calledSumTimeHour);
        data.put("calledAvgTime", calledAvgSecond);
        data.put("intenCustomerSum", intenCustomerSum);
        return data;
    }

    /**
     * 项目管理员负责的历史营销任务数
     *
     * @param loginUser
     * @return
     */
    private int countHistoryMarket(LoginUser loginUser) {
        List<Map<String, Object>> historyMarketList;
        // 历史营销任务
        String historyMarketSql = "SELECT COUNT(t1.id) count FROM t_market_task t1 WHERE t1.cust_id = ?";
        // 项目管理人
        if ("3".equals(loginUser.getUserType())) {
            List<String> cGroupIds = customerUserDao.listCustGroupByUserId(loginUser.getId());
            if (cGroupIds == null || cGroupIds.size() == 0) {
                return 0;
            } else {
                historyMarketSql += " AND t1.customer_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(cGroupIds) + ")";
            }
        }
        int historyMarketTask = 0;
        historyMarketList = marketResourceDao.sqlQuery(historyMarketSql, loginUser.getCustId());
        if (historyMarketList.size() > 0) {
            historyMarketTask = NumberConvertUtil.parseInt(String.valueOf(historyMarketList.get(0).get("count")));
        }
        return historyMarketTask;
    }

    /**
     * 项目管理员负责的待创建营销任务数
     *
     * @param loginUser
     * @return
     */
    private int countNewMarket(LoginUser loginUser) {
        // 待创建营销任务
        String newMarketTaskSql = "SELECT COUNT(t1.id) count FROM customer_group t1 WHERE t1.id NOT IN(SELECT customer_group_id FROM t_market_task WHERE task_type IS NULL AND cust_id = ? ) AND t1.cust_id = ?";
        // 项目管理人
        if ("3".equals(loginUser.getUserType())) {
            List<String> cGroupIds = customerUserDao.listCustGroupByUserId(loginUser.getId());
            if (cGroupIds == null || cGroupIds.size() == 0) {
                return 0;
            } else {
                newMarketTaskSql += " AND t1.id IN (" + SqlAppendUtil.sqlAppendWhereIn(cGroupIds) + ")";
            }
        }
        int newMarketTask = 0;
        List<Map<String, Object>> newMarketTaskList = marketResourceDao.sqlQuery(newMarketTaskSql, loginUser.getCustId(), loginUser.getCustId());
        if (newMarketTaskList.size() > 0) {
            newMarketTask = NumberConvertUtil.parseInt(String.valueOf(newMarketTaskList.get(0).get("count")));
        }
        return newMarketTask;
    }

    /**
     * 首页外呼数据统计
     *
     * @param timeType
     * @param loginUser
     * @param startTime
     * @param endTime
     * @param userGroupId
     * @param userId
     * @return
     */
    public Map<String, Object> countMarketDataV5(int timeType, LoginUser loginUser, String startTime, String endTime, String userGroupId, String userId) {
        Map<String, Object> data = new HashMap<>();
        // 处理时间
        if (StringUtil.isEmpty(startTime) || StringUtil.isEmpty(endTime)) {
            LocalDate localDate = null;
            //今天
            if (timeType == 1) {
                localDate = LocalDate.now();
                //昨天
            } else if (timeType == 2) {
                localDate = LocalDate.now().minusDays(1);
            }
            startTime = LocalDateTime.of(localDate, LocalTime.MIN).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            endTime = LocalDateTime.of(localDate, LocalTime.MAX).format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
        long callSum = 0;
        long calledSum = 0;
        long calledSumTime = 0;
        String calledPercent = "0";
        long intentCustomerSum = 0;
        int userGroupSum = 0;
        int userSum = 0;
        int historyMarketTask = 0;
        int newMarketTask = 0;
        double accountBalance = 0;

        data.put("callSum", callSum);
        data.put("calledSum", calledSum);
        data.put("calledPercent", calledPercent);
        data.put("calledSumTime", 0);
        data.put("calledAvgTime", 0);
        data.put("intenCustomerSum", 0);

        // 历史营销任务
        historyMarketTask = countHistoryMarket(loginUser);
        // 待创建营销任务
        newMarketTask = countNewMarket(loginUser);

        // 查询分组数量
        if ("1".equals(loginUser.getUserType())) {
            CustomerUserGroup userGroupEntity = new CustomerUserGroup();
            userGroupEntity.setCustId(loginUser.getCustId());
            List<CustomerUserGroup> customerUserGroupList = customerUserDao.listCustomerUserGroup(userGroupEntity);
            userGroupSum = customerUserGroupList.size();
        }

        // 查询员工数量
        if ("1".equals(loginUser.getUserType())) {
            String teamSql = "SELECT count(*) as count from t_customer_user where cust_id=? AND `STATUS`=0 AND user_type = 2";
            List<Map<String, Object>> teamList = marketResourceDao.sqlQuery(teamSql, loginUser.getCustId());
            if (teamList.size() > 0) {
                userSum = NumberConvertUtil.parseInt(String.valueOf(teamList.get(0).get("count")));
            }
        } else {
            if ("1".equals(loginUser.getUserGroupRole())) {
                List<CustomerUserDTO> userGroupList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                if (userGroupList.size() > 0) {
                    userSum = userGroupList.size();
                }
            }
        }

        DecimalFormat df = new DecimalFormat("######0.00");
        if ("1".equals(loginUser.getUserType())) {
            //余额查询分
            CustomerProperty ra = customerDao.getProperty(loginUser.getCustId(), "remain_amount");
            accountBalance = NumberConvertUtil.parseDouble(ra.getPropertyValue());
        }
        data.put("userGroupSum", userGroupSum);
        data.put("userSum", userSum);
        data.put("historyMarketTask", historyMarketTask);
        data.put("newMarketTask", newMarketTask);
        data.put("accountBalance", df.format(accountBalance / 100));

        // 查询呼叫统计类数据
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SUM(caller_sum) call_sum, SUM(called_sum) called_sum, SUM(called_duration) called_duration, SUM(order_sum) order_sum FROM stat_c_g_u_d  WHERE cust_id = ? AND create_time >= ? AND create_time <= ? ");
        List<Map<String, Object>> list = null;
        List<CustomerUserDTO> selectUserList = null;
        Set<String> groupUserIds = null;
        // 处理管理员权限
        if ("1".equals(loginUser.getUserType())) {
            if (StringUtil.isNotEmpty(userId)) {
                sql.append(" AND user_id = '" + StringEscapeUtils.escapeSql(userId) + "'");
            } else if (StringUtil.isNotEmpty(userGroupId)) {
                selectUserList = customerUserDao.listSelectCustomerUserByUserGroupId(userGroupId, loginUser.getCustId());
                if (selectUserList.size() > 0) {
                    groupUserIds = new HashSet<>();
                    for (CustomerUserDTO customerUserDTO : selectUserList) {
                        groupUserIds.add(customerUserDTO.getId());
                    }
                    sql.append(" AND user_id IN (" + SqlAppendUtil.sqlAppendWhereIn(groupUserIds) + ")");
                } else {
                    return data;
                }

            }
        } else if ("2".equals(loginUser.getUserType())) {
            // 处理组长权限
            if ("1".equals(loginUser.getUserGroupRole())) {
                // 查询单个用户
                if (StringUtil.isNotEmpty(userId)) {
                    sql.append(" AND user_id = '" + StringEscapeUtils.escapeSql(userId) + "'");
                } else { // 查询组下所有人员数据
                    selectUserList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                    if (selectUserList.size() > 0) {
                        groupUserIds = new HashSet<>();
                        for (CustomerUserDTO customerUserDTO : selectUserList) {
                            groupUserIds.add(customerUserDTO.getId());
                        }
                        sql.append(" AND user_id IN (" + SqlAppendUtil.sqlAppendWhereIn(groupUserIds) + ")");
                    } else {
                        return data;
                    }

                }
            } else if ("2".equals(loginUser.getUserGroupRole())) {// 处理普通用户权限
                sql.append(" AND user_id = '" + StringEscapeUtils.escapeSql(String.valueOf(loginUser.getId())) + "'");
            } else {
                // 处理未配置组员的情况
                sql.append(" AND user_id = '" + StringEscapeUtils.escapeSql(String.valueOf(loginUser.getId())) + "'");
            }
        }
        // 项目管理人
        if ("3".equals(loginUser.getUserType())) {
            List<String> cGroupIds = customerUserDao.listCustGroupByUserId(loginUser.getId());
            if (cGroupIds == null || cGroupIds.size() == 0) {
                return data;
            }
            sql.append(" AND customer_group_id IN (" + SqlAppendUtil.sqlAppendWhereIn(cGroupIds) + ")");
        }
        list = marketResourceDao.sqlQuery(sql.toString(), loginUser.getCustId(), startTime, endTime);
        if (list.size() > 0) {
            for (Map<String, Object> map : list) {
                if (map != null) {
                    if (map.get("call_sum") != null) {
                        callSum += NumberConvertUtil.parseLong(String.valueOf(map.get("call_sum")));
                    }
                    if (map.get("called_sum") != null) {
                        calledSum += NumberConvertUtil.parseLong(String.valueOf(map.get("called_sum")));
                    }
                    if (map.get("called_duration") != null) {
                        calledSumTime += NumberConvertUtil.parseLong(String.valueOf(map.get("called_duration")));
                    }
                    if (map.get("order_sum") != null) {
                        intentCustomerSum += NumberConvertUtil.parseLong(String.valueOf(map.get("order_sum")));
                    }
                }
            }
        }

        NumberFormat numberFormat = NumberFormat.getInstance();
        // 设置精确到小数点后2位
        numberFormat.setMaximumFractionDigits(2);
        // 接通率
        if (callSum > 0) {
            calledPercent = numberFormat.format((float) calledSum / (float) callSum * 100);
        }
        // 总通话时长(小时)
        String calledSumTimeHour = numberFormat.format((float) calledSumTime / 3600);
        // 平均通话时长(分钟)
        long calledAvgSecond = 0L;
        BigDecimal bigDecimal;
        if (calledSum > 0) {
            bigDecimal = new BigDecimal((double) calledSumTime / calledSum);
            calledAvgSecond = bigDecimal.setScale(0, RoundingMode.CEILING).longValue();
        }

        data.put("callSum", callSum);
        data.put("calledSum", calledSum);
        data.put("calledPercent", calledPercent);
        data.put("calledSumTime", calledSumTimeHour);
        data.put("calledAvgTime", calledAvgSecond);
        data.put("intenCustomerSum", intentCustomerSum);
        return data;
    }


    public com.bdaim.common.dto.Page querySmsHistoryV1(String user_id, String userType, String cust_id, Integer pageNum, Integer pageSize, String superId,
                                                       String startTime, String endTime, String userName, String groupId, String marketTaskId, String seaId,
                                                       String activeSTime, String activeETime, String status) {
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT count(*) as total, sms.superid, sms.create_time, sms.STATUS, tem.title, us.account realname, sms.templateId, sms.touch_id batch_number, sms.active_time ");
        sb.append(" FROM t_touch_sms_log sms ");
        sb.append(" LEFT JOIN t_customer_user us on sms.user_id =us.id ");
        sb.append(" LEFT JOIN t_template tem on tem.id=sms.templateId ");
        sb.append(" where 1=1 ");
        //企业管理员
        if ("1".equals(userType)) {
            sb.append(" and sms.cust_id='" + cust_id + "'");
        } else {
            if (StringUtil.isNotEmpty(user_id)) {
                sb.append(" and sms.user_id='" + user_id + "'");
            }

        }
        if (null != superId && !"".equals(superId)) {
            sb.append(" and sms.superid='" + superId + "'");
        }
        if (null != startTime && !"".equals(startTime)) {
            sb.append(" and sms.create_time >='" + startTime + "'");
        }
        if (null != endTime && !"".equals(endTime)) {
            sb.append(" and sms.create_time <='" + endTime + "'");
        }
        if (null != userName && !"".equals(userName)) {
            sb.append(" and us.`account` = '" + userName + "'");
        }
        if (StringUtil.isNotEmpty(groupId)) {
            sb.append(" and sms.`customer_group_id` = '" + groupId + "'");
        }
        if (StringUtil.isNotEmpty(marketTaskId)) {
            sb.append(" and sms.`market_task_id` = '" + marketTaskId + "'");
        }
        if (StringUtil.isNotEmpty(seaId)) {
            sb.append(" and sms.`customer_sea_id` = '" + seaId + "'");
        }
        if (StringUtil.isNotEmpty(activeSTime)) {
            sb.append(" and sms.active_time >='" + activeSTime + "'");
        }
        if (StringUtil.isNotEmpty(activeETime)) {
            sb.append(" and sms.active_time <='" + activeETime + "'");
        }
        if (StringUtil.isNotEmpty(status)) {
            sb.append(" and sms.status ='" + status + "'");
        }
        sb.append(" GROUP BY sms.touch_id ");
        sb.append(" ORDER BY sms.create_time DESC ");
        com.bdaim.common.dto.Page page = null;
        try {
            page = this.marketResourceDao.sqlPageQuery0(sb.toString(), pageNum, pageSize);
        } catch (Exception e) {
            LOG.error("查询短信营销记录失败,", e);
            page = new com.bdaim.common.dto.Page();
        }
        return page;
    }


    public List<Map<String, Object>> queryEmailHistory(String user_id, String userType, String cust_id, Integer pageNum, Integer pageSize, String superId, String startTime, String endTime, String userName) {
        JSONObject json = new JSONObject();
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT count(*) as total, email.create_time, email.STATUS, tem.title, us.`realname`, email.templateId,email.email_content,email.batch_number ");
        sb.append(" FROM t_touch_email_log email ");
        sb.append(" LEFT JOIN t_user us on email.user_id =us.id ");
        sb.append(" LEFT JOIN t_template tem on tem.id=email.templateId ");
        sb.append(" where 1=1 ");
        if ("1".equals(userType)) { //企业管理员
            sb.append(" and email.cust_id='" + cust_id + "'");
        } else {
            sb.append(" and email.user_id='" + user_id + "'");
        }
        if (null != superId && !"".equals(superId)) {
            sb.append(" and email.superid='" + superId + "'");
        }
        if (null != startTime && !"".equals(startTime)) {
            sb.append(" and email.create_time >='" + startTime + "'");
        }
        if (null != endTime && !"".equals(endTime)) {
            sb.append(" and email.create_time <='" + endTime + "'");
        }
        if (null != userName && !"".equals(userName)) {
            sb.append(" and us.`realname` like'%" + userName + "%'");
        }
        sb.append(" GROUP BY email.batch_number ");
        sb.append(" ORDER BY email.create_time DESC ");
        sb.append(" LIMIT " + pageNum + "," + pageSize);
        List<Map<String, Object>> list = this.marketResourceDao.sqlQuery(sb.toString());
        return list;
    }


    public List<Map<String, Object>> queryPotentialCusGroup(Integer pageNum, Integer pageSize, String custId,
                                                            Integer cusGroupId, String cusGroupName, String userType, Long userId) {
        Map<String, Object> map = new HashMap<>();
        StringBuffer sql = new StringBuffer();
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            sql.append("SELECT " + " t1.order_id as orderId , "
                    + "  IFNULL (t2.NAME,'') AS groupName, " + "  t2.id AS groupId, "
                    + "  IFNULL(t1.quantity,0) AS quantity , " + "  t2.industry_pool_name AS industryPoolName , "
                    + "  t1.create_time, " + "  IFNULL (t2.remark,'') AS remark , "
                    + "  IFNULL (t2.group_source,'') AS source , " + "  t2.status as groupStatus , ");

            sql.append("   (SELECT count(DISTINCT t4.id) FROM  ")
                    .append("   t_customer_group_list_" + custId + "  t4")
                    .append("   LEFT JOIN t_super_label t3 ")
                    .append("  ON  (t4.customer_group_id= t3.cust_group_id and t3.super_id = t4.id)")
                    .append("  WHERE t4.customer_group_id  = t2.id ")
                    .append("  and t3.cust_group_id IS NOT NULL ");
            if ("2".equals(userType)) {
                sql.append("  AND t4.user_id = " + userId);
            }
            sql.append(" ) as potalCount");

            sql.append("   FROM t_order t1 " + "  RIGHT JOIN customer_group t2 "
                    + "   ON t1.order_id = t2.order_id  "
                    + "   where 1=1 and t1.order_type=1");

            sql.append(" and t1.cust_id ='").append(StringEscapeUtils.escapeSql(custId)).append("'");
            sql.append(" and t1.order_state= 2");
            //添加userid
            if ("2".equals(userType)) {
                if (null != userId && !"".equals(userId)) {
                    sql.append(" AND t2.id in (SELECT DISTINCT");
                    sql.append(" 	custG.customer_group_id");
                    sql.append(" FROM");
                    sql.append(" 	t_customer_group_list_" + custId + " custG");
                    sql.append(" LEFT JOIN t_customer_user t ON custG.user_id = t.id");
                    sql.append(" WHERE");
                    sql.append(" custG.user_id='" + userId + "')");
                }
            }

            if (StringUtil.isNotEmpty(cusGroupName)) {
                sql.append(" and t2.name like'%").append(StringEscapeUtils.escapeSql(cusGroupName)).append("%'");
            }
            if (cusGroupId != null) {
                sql.append(" and t2.id='" + cusGroupId + "'");
            }

            sql.append("  GROUP BY t2.id  ");
            sql.append("  ORDER BY t1.create_time desc ");
            map.put("total", marketResourceDao.getSQLQuery(sql.toString()).list().size());
            map.put("cusGroupList",
                    marketResourceDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                            .setFirstResult(pageNum).setMaxResults(pageSize).list());

        } catch (Exception e) {
            map.put("total", 0);
        }

        list.add(map);
        return list;
    }

    public List<Map<String, Object>> queryPotentialCusGroupV3(Integer pageNum, Integer pageSize, Integer cusGroupId, String cusGroupName, LoginUser loginUser) {
        StringBuffer sql = new StringBuffer();

        sql.append("SELECT t1.order_id as orderId ,"
                + "  IFNULL (t2.NAME,'') AS groupName, t2.id AS groupId, "
                + "  t2.industry_pool_name AS industryPoolName ,"
                + "  t1.create_time, IFNULL (t2.remark,'') AS remark ,"
                + "  IFNULL (t2.group_source,'') AS source ,  t2.status as groupStatus ,")
                //客户群实际用户数量
                .append(" t2.user_count quantity ")
                .append("   FROM t_order t1  RIGHT JOIN customer_group t2"
                        + "   ON t1.order_id = t2.order_id  "
                        + "   where 1=1 and t1.order_type=1")
                .append(" AND t2.`status` = 1 ")
                .append(" and t1.cust_id ='").append(StringEscapeUtils.escapeSql(loginUser.getCustId())).append("'")
                .append(" and t1.order_state= 2");
        if (StringUtil.isNotEmpty(cusGroupName)) {
            sql.append(" and t2.name like'%").append(StringEscapeUtils.escapeSql(cusGroupName)).append("%'");
        }
        if (cusGroupId != null) {
            sql.append(" and t2.id='" + cusGroupId + "'");
        }

        // 坐席或者组长区分权限,只查询组负责的数据
        /*if ("2".equals(loginUser.getUserType())) {
            CustomerUserGroupRelDTO customerUserGroupRelDTO = customerUserDao.getCustomerUserGroupByUserId(loginUser.getId());
            sql.append(" AND FIND_IN_SET('" + StringEscapeUtils.escapeSql(customerUserGroupRelDTO.getGroupId()) + "', t2.user_group_id) ");
        }*/

        sql.append("  GROUP BY t2.id  ");
        sql.append("  ORDER BY t1.create_time desc ");
        List<Map<String, Object>> result = marketResourceDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .setFirstResult(pageNum).setMaxResults(pageSize).list();
        if (result != null && result.size() > 0) {
            List<Map<String, Object>> superLabelList;
            for (Map<String, Object> resultMap : result) {
                if (resultMap != null && resultMap.get("groupId") != null) {
                    try {
                        superLabelList = this.marketResourceDao.sqlQuery("SELECT COUNT(*) count FROM t_customer_group_list_" + resultMap.get("groupId") + " WHERE super_data IS NOT NULL AND super_data <> '' AND super_data <> '{}' ;");
                        if (superLabelList.size() > 0 && superLabelList.get(0).get("count") != null) {
                            resultMap.put("potalCount", superLabelList.get(0).get("count"));
                        } else {
                            resultMap.put("potalCount", 0);
                        }
                    } catch (Exception e) {
                        resultMap.put("potalCount", 0);
                        LOG.error("查询客户群潜客数量异常,", e);
                        continue;
                    }
                }
            }
        }
        return result;
    }

    public long countQueryPotentialCusGroupV3(Integer cusGroupId, String cusGroupName, LoginUser loginUser) {
        StringBuffer sql = new StringBuffer();
        List<Map<String, Object>> list = new ArrayList<>();

        sql.append(" SELECT COUNT(0) count ")
                .append("   FROM t_order t1  RIGHT JOIN customer_group t2"
                        + "   ON t1.order_id = t2.order_id  "
                        + "   where 1=1 and t1.order_type=1")
                .append(" AND t2.`status` = 1 ")
                .append(" AND t1.cust_id ='").append(StringEscapeUtils.escapeSql(loginUser.getCustId())).append("'")
                .append(" AND t1.order_state= 2");
        if (StringUtil.isNotEmpty(cusGroupName)) {
            sql.append(" AND t2.name like'%").append(StringEscapeUtils.escapeSql(cusGroupName)).append("%'");
        }
        if (cusGroupId != null) {
            sql.append(" AND t2.id='" + cusGroupId + "'");
        }
        // 坐席或者组长区分权限,只查询组负责的数据
        /*if ("2".equals(loginUser.getUserType())) {
            CustomerUserGroupRelDTO customerUserGroupRelDTO = customerUserDao.getCustomerUserGroupByUserId(loginUser.getId());
            if(customerUserGroupRelDTO != null){
                sql.append(" AND FIND_IN_SET('" + StringEscapeUtils.escapeSql(customerUserGroupRelDTO.getGroupId()) + "', t2.user_group_id) ");
            }
        }*/
        List<Map<String, Object>> result = marketResourceDao.sqlQuery(sql.toString());
        if (result != null && result.size() > 0) {
            return NumberConvertUtil.parseLong(String.valueOf(result.get(0).get("count")));
        }
        return 0L;

    }


    public List<Map<String, Object>> queryPotentialDetail(Integer pageNum, Integer pageSize, String custGroupId,
                                                          JSONArray custProperty, String superId, String cust_id, Long userId, String userType) {

        Map<String, Object> map = new HashMap<>();
        StringBuffer sql = new StringBuffer();
        List<Map<String, Object>> list = new ArrayList<>();
        sql.append(
                " SELECT custLabel.* FROM ( SELECT t1.id AS superId,GROUP_CONCAT(t2.label_id) as labelId,GROUP_CONCAT(t3.label_name) as labelName,t4.* ")
                .append("  from t_customer_group_list_" + cust_id + " t1")
                .append("  LEFT JOIN t_super_label t2 ")
                .append("  ON  (t1.id = t2.super_id  and t1.customer_group_id = t2.cust_group_id)")
                .append("  LEFT JOIN t_customer_label t3")
                .append("  ON t2.label_id = t3.label_id")

                .append("  LEFT JOIN t_touch_voice_info t4")
                .append("  ON t1.id = t4.super_id ")
                .append("  and t1.customer_group_id = t4.cust_group_id")
                // .append(" LEFT JOIN u u ON u.id = t1.id")

                .append(" where  1=1  and t3.status = 1 ");
        sql.append(" and t2.cust_group_id  ='").append(StringEscapeUtils.escapeSql(custGroupId)).append("'");
        if (userType != null && !"".equals(userType) && "2".equals(userType)) {
            sql.append(" and t1.user_id =" + userId);
        }
        //sql.append(" and t2.label_id is NOT NULL");

        if (StringUtil.isNotEmpty(superId)) {
            sql.append(" and t1.id like'%").append(StringEscapeUtils.escapeSql(superId)).append("%'");
        }

        sql.append("  GROUP BY t1.id ");
        sql.append(" ) custLabel ");
        if (custProperty != null && custProperty.size() != 0) {
            sql.append(" where (custLabel.labelName like '%");
            for (int i = 0; i < custProperty.size(); i++) {
                String labelId = custProperty.get(i).toString();
                String labelIs = null;
                labelIs = labelId + "%' OR  custLabel.labelName LIKE '%";
                if (i == custProperty.size() - 1) {
                    labelIs = labelId + "%'  ";
                }
                sql.append(labelIs);
            }
            sql.append(")");
        }

        // sql.append(" ORDER BY t1.create_time desc ");
        map.put("total", marketResourceDao.getSQLQuery(sql.toString()).list().size());
        List<Map<String, Object>> superLabelList = marketResourceDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .setFirstResult(pageNum).setMaxResults(pageSize).list();
        // 处理自定义属性和标签的对应关系
        String[] labelIds;
        String superLabelSql = "SELECT option_value FROM t_super_label WHERE label_id = ? AND super_id = ? AND cust_group_id = ? ";
        List<Map<String, Object>> superLabelOptionList;
        List<Map<String, Object>> labelsMapList;
        Map<String, Object> labelMap;
        for (Map<String, Object> superLabel : superLabelList) {
            if (superLabel.get("labelId") != null) {
                labelsMapList = new ArrayList<>();
                labelIds = String.valueOf(superLabel.get("labelId")).split(",");
                for (String labelId : labelIds) {
                    labelMap = new HashMap<>();
                    superLabelOptionList = this.marketResourceDao.sqlQuery(superLabelSql, labelId, superLabel.get("superId"), superLabel.get("cust_group_id"));
                    if (superLabelOptionList.size() > 0 && superLabelOptionList.get(0).get("option_value") != null) {
                        labelMap.put("labelId", labelId);
                        labelMap.put("optionValue", String.valueOf(superLabelOptionList.get(0).get("option_value")).split(","));
                        labelsMapList.add(labelMap);
                    }
                }
                superLabel.put("labelIds", labelsMapList);
            }
            if (superLabel != null) {
                superLabel.put("phone", phoneService.getPhoneBySuperId(String.valueOf(superLabel.get("id"))));
            }
        }
        map.put("custGroupOrders", superLabelList);
        list.add(map);
        return list;
    }

    public List<Map<String, Object>> queryPotentialDetailV3(Integer pageNum, Integer pageSize, String custGroupId,
                                                            JSONArray custProperty, String superId, LoginUser loginUser) {
        List<Map<String, Object>> superLabelList = null;
        try {
            StringBuffer sql = new StringBuffer();
            sql.append(" SELECT t.id superId, t.user_id, t.update_time createTime, t.super_data, t.super_age, t.super_name, t.super_sex")
                    .append("  FROM t_customer_group_list_" + custGroupId + " t  WHERE 1=1 ");

            if ("2".equals(loginUser.getUserType())) {
                // 组长查组员列表
                if ("1".equals(loginUser.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        if (userIds.size() > 0) {
                            sql.append(" AND t.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }
                    }
                } else {
                    sql.append(" and t.user_id =" + loginUser.getId());
                }
            }

            if (StringUtil.isNotEmpty(superId)) {
                sql.append(" and t.id like'%").append(StringEscapeUtils.escapeSql(superId)).append("%'");
            }

            if (custProperty != null && custProperty.size() != 0) {
                JSONObject jsonObject;
                String labelId, optionValue, likeValue;
                for (int i = 0; i < custProperty.size(); i++) {
                    jsonObject = custProperty.getJSONObject(i);
                    if (jsonObject != null) {
                        labelId = jsonObject.getString("labelId");
                        optionValue = jsonObject.getString("optionValue");
                        likeValue = "\"" + labelId + "\":\"" + optionValue + "\"";
                        sql.append(" AND t.super_data LIKE '%" + likeValue + "%' ");
                    }
                }
            }

            sql.append(" and t.super_data IS NOT NULL AND t.super_data <> '' AND t.super_data <> '{}'");
            sql.append(" GROUP BY t.id ");
            sql.append(" ORDER BY t.update_time DESC ");

            superLabelList = marketResourceDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                    .setFirstResult(pageNum).setMaxResults(pageSize).list();
            // 处理自建属性的名称和选项值
            if (superLabelList.size() > 0) {
                //GROUP_CONCAT(t.labelId) AS labelId, GROUP_CONCAT(t3.label_name) as labelName, GROUP_CONCAT(t.optionValue) AS optionValue, t.createTime " +
                StringBuffer labelIds, labelNames, optionValues;
                for (Map<String, Object> superLabelMap : superLabelList) {
                    labelIds = new StringBuffer("");
                    labelNames = new StringBuffer("");
                    optionValues = new StringBuffer("");
                    Map<String, Object> labelData;
                    CustomerLabel customerLabel;
                    if (superLabelMap != null && superLabelMap.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(superLabelMap.get("super_data")))) {
                        labelData = JSON.parseObject(String.valueOf(superLabelMap.get("super_data")), Map.class);
                        if (labelData != null && labelData.size() > 0) {
                            for (Map.Entry<String, Object> key : labelData.entrySet()) {
                                labelIds.append(key.getKey()).append(",");
                                customerLabel = customerLabelDao.getCustomerLabel(key.getKey());
                                if (customerLabel != null) {
                                    labelNames.append(customerLabel.getLabelName()).append(",");
                                }
                                optionValues.append(key.getValue()).append(",");
                            }
                        }
                    }

                    superLabelMap.put("labelId", labelIds.toString());
                    superLabelMap.put("labelName", labelNames.toString());
                    superLabelMap.put("optionValue", optionValues.toString());

                    // 查询潜客责任人账号
                    if (superLabelMap.get("user_id") != null) {
                        superLabelMap.put("account", customerUserDao.getLoginName(String.valueOf(superLabelMap.get("user_id"))));
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("获取潜客详情列表异常,", e);
            superLabelList = new ArrayList<>();
        }
        return superLabelList;
    }


    public long countQueryPotentialDetailV3(String custGroupId, JSONArray custProperty, String superId, LoginUser loginUser) {
        StringBuffer sql = new StringBuffer();

        sql.append(" SELECT COUNT(t1.id) count ")
                .append("  FROM t_customer_group_list_" + custGroupId + " t1  WHERE 1=1 ");

        if ("2".equals(loginUser.getUserType())) {
            // 组长查组员列表
            if ("1".equals(loginUser.getUserGroupRole())) {
                List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                Set<String> userIds = new HashSet<>();
                if (customerUserDTOList.size() > 0) {
                    for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                        userIds.add(customerUserDTO.getId());
                    }
                    if (userIds.size() > 0) {
                        sql.append(" AND t1.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                    }
                }
            } else {
                sql.append(" and t1.user_id =" + loginUser.getId());
            }
        }

        if (StringUtil.isNotEmpty(superId)) {
            sql.append(" and t1.id like'%").append(StringEscapeUtils.escapeSql(superId)).append("%'");
        }

        if (custProperty != null && custProperty.size() != 0) {
            JSONObject jsonObject;
            String labelId, optionValue, likeValue;
            for (int i = 0; i < custProperty.size(); i++) {
                jsonObject = custProperty.getJSONObject(i);
                if (jsonObject != null) {
                    labelId = jsonObject.getString("labelId");
                    optionValue = jsonObject.getString("optionValue");
                    likeValue = "\"" + labelId + "\":\"" + optionValue + "\"";
                    sql.append(" AND t1.super_data LIKE '%" + likeValue + "%' ");
                }
            }
        }
        sql.append(" AND t1.super_data IS NOT NULL AND t1.super_data <> '' AND t1.super_data <> '{}'");
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(sql.toString());
        if (list != null && list.size() > 0) {
            return NumberConvertUtil.parseLong(String.valueOf(list.get(0).get("count")));
        }
        return 0;
    }


    public List<Map<String, Object>> listCustGroupOrders(String customerId, CustomerGrpOrdParam param) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();

        try {
            StringBuilder sql = new StringBuilder("SELECT " + " t1.order_id as orderId , "
                    + "  IFNULL (t2.NAME,'') AS groupName, " + "  t2.id AS groupId, "
                    + "  IFNULL(t1.quantity,0) AS account , " + "  t2.industry_pool_name AS industryPoolName , "
                    + "  t1.create_time, " + "   (SELECT COUNT(customer_group_id) FROM t_customer_group_list_" + customerId
                    + "  " + "   WHERE STATUS=1 AND  customer_group_id = t2.id) AS quantity , " + "  IFNULL (t2.remark,'') AS remark , "
                    + "  IFNULL (t2.group_source,'') AS source , " + "  t2.status as groupStatus  " + "FROM "
                    + "  t_order t1 " + "  RIGHT JOIN customer_group t2 "
                    + "    ON t1.order_id = t2.order_id where 1=1 and t1.order_type=1");
            sql.append(" and t1.cust_id ='").append(StringEscapeUtils.escapeSql(customerId)).append("'");
            if (StringUtil.isNotEmpty(param.getGroupName())) {
                sql.append(" and t2.name like'%").append(StringEscapeUtils.escapeSql(param.getGroupName())).append("%'");
            }

            sql.append(" and t1.order_state= 2");

            if (StringUtil.isNotEmpty(param.getGroupId()) && Integer.valueOf(param.getGroupId()) > 0) {
                sql.append(" and t2.id=").append(param.getGroupId());
            }
            if (StringUtil.isNotEmpty(param.getStartTime()) || StringUtil.isNotEmpty(param.getEndTime())) {
                sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime()))
                        .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
            }
            sql.append("  GROUP BY t2.id  ");
            sql.append("  ORDER BY t1.create_time desc ");
            map.put("total", marketResourceDao.getSQLQuery(sql.toString()).list().size());
            map.put("custGroupOrders",
                    marketResourceDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                            .setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list());

        } catch (Exception e) {
            map.put("total", 0);
        }
        list.add(map);
        return list;
    }


    public List<Map<String, Object>> listCustGroupOrdersV2(String customerId, CustomerGrpOrdParam param) {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        try {
            StringBuilder sql = new StringBuilder("SELECT t1.order_id as orderId ,"
                    + "  IFNULL (t2.NAME,'') AS groupName, t2.id AS groupId,"
                    + "  IFNULL(t1.quantity,0) AS account ,  t2.industry_pool_name AS industryPoolName ,"
                    + "  t1.create_time, t2.user_count quantity ,t2.status as groupStatus  FROM"
                    + "  t_order t1 RIGHT JOIN customer_group t2"
                    + "    ON t1.order_id = t2.order_id where 1=1 and t1.order_type=1");
            sql.append(" and t1.cust_id ='").append(StringEscapeUtils.escapeSql(customerId)).append("'");
            if (StringUtil.isNotEmpty(param.getGroupName())) {
                sql.append(" and t2.name like'%").append(StringEscapeUtils.escapeSql(param.getGroupName())).append("%'");
            }
            sql.append(" and t1.order_state= 2");
            if (StringUtil.isNotEmpty(param.getGroupId()) && Integer.valueOf(param.getGroupId()) > 0) {
                sql.append(" and t2.id=").append(param.getGroupId());
            }
            if (StringUtil.isNotEmpty(param.getStartTime()) || StringUtil.isNotEmpty(param.getEndTime())) {
                sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime()))
                        .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
            }
            sql.append("  GROUP BY t2.id  ");
            sql.append("  ORDER BY t1.create_time desc ");
            map.put("total", marketResourceDao.getSQLQuery(sql.toString()).list().size());
            List<Map<String, Object>> customerGroupOrders =
                    marketResourceDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                            .setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list();
            // 查询客户群未分配的数量
            if (customerGroupOrders.size() > 0) {
                List<Map<String, Object>> unAssignedUsers;
                for (Map<String, Object> custGroupOrder : customerGroupOrders) {
                    custGroupOrder.put("quantity", 0);
                    sql.setLength(0);
                    sql.append("SELECT IFNULL(COUNT(id), 0) count FROM t_customer_group_list_" + custGroupOrder.get("groupId"))
                            .append(" WHERE  status = 1 ");
                    unAssignedUsers = marketResourceDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
                    if (unAssignedUsers != null && unAssignedUsers.size() > 0) {
                        custGroupOrder.put("quantity", unAssignedUsers.get(0).get("count"));
                    }
                }
            }
            map.put("custGroupOrders", customerGroupOrders);

        } catch (Exception e) {
            map.put("total", 0);
        }
        list.add(map);
        return list;
    }


    public String updateAssignedOne(String id, Long userId, String custId, Integer custGroupId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sb = new StringBuffer();

        try {

            sb.append("update t_customer_group_list_" + custId + " set");
            sb.append(" update_time = NOW(), ");
            sb.append(" STATUS = '0', ");
            sb.append(" user_id =? ");

            sb.append(" where id = ?");
            sb.append(" AND customer_group_id = ?");
            int code = this.marketResourceDao.executeUpdateSQL(sb.toString(), userId, id, custGroupId);

            LOG.info("分配管理员，sql：" + sb.toString());
            map.put("code", code);
            map.put("message", "分配负责人成功");
            json.put("data", map);

        } catch (Exception e) {
            map.put("code", 000);
            map.put("message", "分配负责人失败");
            json.put("data", map);
        }

        return json.toJSONString();
    }

    public String updateAssignedOne0(String id, Long userId, Integer custGroupId, String userGroupId, String marketTaskId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sb = new StringBuffer();
        try {
            if (StringUtil.isNotEmpty(marketTaskId)) {
                sb.append("update " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " set");
            } else {
                sb.append("update t_customer_group_list_" + custGroupId + " set");
            }
            sb.append(" update_time = NOW(), ");
            sb.append(" STATUS = '0', ");
            if (userId != null) {
                sb.append(" user_id = " + userId);
            } else if (StringUtil.isNotEmpty(userGroupId)) {
                sb.append(" user_group_id = " + userGroupId);
            }
            sb.append(" where id = ?");
            int code = this.marketResourceDao.executeUpdateSQL(sb.toString(), id);
            map.put("code", code);
            map.put("message", "分配负责人成功");
            json.put("data", map);

        } catch (Exception e) {
            map.put("code", 000);
            map.put("message", "分配负责人失败");
            json.put("data", map);
        }

        return json.toJSONString();
    }


    public String updateAssignedMany(final List list, String custId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();

        String sql = "update t_customer_group_list_" + custId
                + "  SET STATUS = 0,  update_time = NOW(), user_id = ?  WHERE id = ?"
                + "  AND customer_group_id = ?";
        try {

//            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
//                public int getBatchSize() {
//                    return list.size();
//                }
//
//                public void setValues(PreparedStatement ps, int i) throws SQLException {
//                }
//
//
//                public void setValues(java.sql.PreparedStatement ps, int i) throws SQLException {
//                    PeopleAssignedDTO peopleAssigned = (PeopleAssignedDTO) list.get(i);
//
//                    ps.setLong(1, peopleAssigned.getUserId());
//                    ps.setString(2, peopleAssigned.getId());
//                    ps.setInt(3, peopleAssigned.getCustGroupId());
//                }
//            });

            map.put("code", 1);
            map.put("message", "分配负责人成功");
            json.put("data", map);
        } catch (Exception e) {
            map.put("code", 0);
            map.put("message", "分配负责人失败");
            json.put("data", map);

        }

        return json.toJSONString();
    }

    /**
     * @param list
     * @param custGroupId
     * @param type        1-给用户分配 2-给用户组分配
     * @return java.lang.String
     * @author chengning@salescomm.net
     * @date 2018/9/27 19:00
     */
    public String updateAssignedMany0(final List list, int custGroupId, int type, String marketTaskId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sb = new StringBuffer();
        if (StringUtil.isNotEmpty(marketTaskId)) {
            sb.append("update " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " set");
        } else {
            sb.append("update t_customer_group_list_" + custGroupId + " set");
        }
        sb.append(" update_time = NOW(), ");
        sb.append(" STATUS = '0', ");
        if (type == 1) {
            sb.append(" user_id = ? ");
        } else if (type == 2) {
            sb.append(" user_group_id = ? ");
        }
        sb.append(" where id = ?");
        try {
            int[] values = jdbcTemplate.batchUpdate(sb.toString(), new BatchPreparedStatementSetter() {
                public int getBatchSize() {
                    return list.size();
                }

                public void setValues(java.sql.PreparedStatement ps, int i) throws SQLException {
                    PeopleAssignedDTO peopleAssigned = (PeopleAssignedDTO) list.get(i);
                    if (type == 1) {
                        ps.setLong(1, peopleAssigned.getUserId());
                    } else if (type == 2) {
                        ps.setString(1, peopleAssigned.getUserGroupId());
                    }
                    ps.setString(2, peopleAssigned.getId());
                }
            });
            System.out.println(values);
            map.put("code", 1);
            map.put("message", "分配负责人成功");
            json.put("data", map);
        } catch (Exception e) {
            map.put("code", 0);
            map.put("message", "分配负责人失败");
            json.put("data", map);
        }
        return json.toJSONString();
    }

    public String updateAssignedManyByCount(final long count, int custGroupId, int type, String condition,
                                            String intentLevel, String marketTaskId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        StringBuffer sb = new StringBuffer();
        if (StringUtil.isNotEmpty(marketTaskId)) {
            sb.append("update " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " set");
        } else {
            sb.append("update t_customer_group_list_" + custGroupId + " set");
        }
        sb.append(" update_time = NOW(), ");
        sb.append(" STATUS = '0', ");
        if (type == 1) {
            sb.append(" user_id = ? ");
        } else if (type == 2) {
            sb.append(" user_group_id = ? ");
        }
        sb.append(" WHERE (STATUS = '1' or STATUS IS NULL) ");
        if (StringUtil.isNotEmpty(intentLevel)) {
            sb.append(" AND intent_level = '" + intentLevel + "'");
        }
        sb.append(" LIMIT " + count);
        LOG.info("updateAssignedManyByCount.sql:" + sb.toString());
        try {
            jdbcTemplate.update(sb.toString(), condition);
            map.put("code", 1);
            map.put("message", "分配负责人成功");
            json.put("data", map);
        } catch (Exception e) {
            map.put("code", 0);
            map.put("message", "分配负责人失败");
            json.put("data", map);
        }
        return json.toJSONString();
    }

    /**
     * 根据指定数量变更负责人
     *
     * @param count
     * @param custGroupId
     * @param type
     * @param condition    新负责人
     * @param leaderUserId 当前负责人
     * @param intentLevel
     * @param marketTaskId
     * @return
     */
    public String updateAssignedManyByCount(final long count, int custGroupId, int type, String condition, long leaderUserId,
                                            String intentLevel, String marketTaskId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        StringBuffer sb = new StringBuffer();
        if (StringUtil.isNotEmpty(marketTaskId)) {
            sb.append("update " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " set");
        } else {
            sb.append("update t_customer_group_list_" + custGroupId + " set");
        }
        sb.append(" update_time = NOW(), ");
        sb.append(" STATUS = '0', ");
        if (type == 1) {
            sb.append(" user_id = ? ");
        } else if (type == 2) {
            sb.append(" user_group_id = ? ");
        }
        sb.append(" WHERE user_id = ?  ");
        if (StringUtil.isNotEmpty(intentLevel)) {
            sb.append(" AND intent_level = '" + intentLevel + "'");
        }
        sb.append(" LIMIT " + count);
        LOG.info("updateAssignedManyByCount.sql:" + sb.toString());
        try {
            jdbcTemplate.update(sb.toString(), condition, leaderUserId);
            map.put("code", 1);
            map.put("message", "分配负责人成功");
            json.put("data", map);
        } catch (Exception e) {
            map.put("code", 0);
            map.put("message", "分配负责人失败");
            json.put("data", map);
        }
        return json.toJSONString();
    }

    public int updateCustomerGroupAllDataAssignedV4(int custGroupId, String userId, String intentLevel,
                                                    String sourceUserName, String status, String marketTaskId) {
        StringBuffer sb = new StringBuffer();
        if (StringUtil.isNotEmpty(marketTaskId)) {
            sb.append("update " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " set");
        } else {
            sb.append("update t_customer_group_list_" + custGroupId + " set");
        }
        sb.append(" update_time = NOW(), ");
        sb.append(" STATUS = '0', ");
        sb.append(" user_id = ? WHERE 1=1 ");
        if (StringUtil.isNotEmpty(intentLevel)) {
            sb.append(" AND intent_level = '" + intentLevel + "'");
        }
        if (StringUtil.isNotEmpty(sourceUserName)) {
            CustomerUser customerUser = customerUserDao.getCustomerUserByName(sourceUserName);
            if (customerUser != null) {
                sb.append(" AND user_id = '" + customerUser.getId() + "'");
            } else {
                LOG.warn("分配全部责任人失败,sourceUserName" + sourceUserName + "未查询到指定用户");
                return 0;
            }
        }
        if (StringUtil.isNotEmpty(status)) {
            sb.append(" AND status = " + status + "");
        }
        return marketResourceDao.executeUpdateSQL(sb.toString(), userId);
    }


    public List<Map<String, Object>> queryNoAssigned(String custId, Integer custGroupId) {

        StringBuilder sql = new StringBuilder("SELECT id FROM t_customer_group_list_" + custId + ""
                + " WHERE STATUS = 1 and customer_group_id = " + custGroupId);
        List list = this.marketResourceDao.sqlQuery(sql.toString());

        return list;
    }


    public String getStaffName_V3(LoginUser loginUser) {
        Map<String, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        List<Map<String, Object>> list = new ArrayList<>();
        // 管理员查全部
        if ("1".equals(loginUser.getUserType()) || "3".equals(loginUser.getUserType())) {
            StringBuilder sql = new StringBuilder("SELECT CAST(t.id AS CHAR) id,t.cust_id,t.realname,t.account from  t_customer_user t  WHERE  t.cust_id = ? AND STATUS = 0 AND user_type = 2");
            list = this.marketResourceDao.sqlQuery(sql.toString(), loginUser.getCustId());
        } else if ("2".equals(loginUser.getUserType())) {
            // 组长查组员列表
            if ("1".equals(loginUser.getUserGroupRole())) {
                List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                Map<String, Object> userMap;
                if (customerUserDTOList.size() > 0) {
                    for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                        userMap = new HashMap<>();
                        userMap.put("id", customerUserDTO.getId());
                        userMap.put("cust_id", loginUser.getCustId());
                        userMap.put("account", customerUserDTO.getAccount());
                        userMap.put("realname", customerUserDTO.getRealname());
                        list.add(userMap);
                    }
                }
            }
        }

        map.put("staff", list);
        json.put("staffJson", map);
        return json.toJSONString();
    }


    public com.bdaim.common.dto.Page getSmsSuperIdListV1(String cust_id, String touch_id, Integer pageNum, Integer pageSize) {
        String sql = "SELECT `STATUS`,superid,sms_content FROM `t_touch_sms_log` WHERE touch_id=?  ";
        if (StringUtil.isNotEmpty(cust_id)) {
            sql += " AND cust_id = '" + cust_id + "'";
        }
        com.bdaim.common.dto.Page page = null;
        try {
            page = this.marketResourceDao.sqlPageQuery0(sql.toString(), pageNum, pageSize, touch_id);
        } catch (Exception e) {
            page = new com.bdaim.common.dto.Page();
            LOG.error("查询短信详细发送内容失败,", e);
        }
        return page;
    }


    public List<Map<String, Object>> getEmailSuperIdList(String cust_id, String batch_number, Integer pageNum, Integer pageSize) {
        String sql = "SELECT `STATUS`,superid,email_content FROM `t_touch_email_log` WHERE cust_id=? and batch_number=? LIMIT ?,?";
        List<Map<String, Object>> list = this.marketResourceDao.sqlQuery(sql.toString(), cust_id, batch_number, pageNum, pageSize);

        String sqlB = "SELECT DISTINCT  email_content FROM `t_touch_email_log` WHERE cust_id=? and batch_number=? ";
        String email_mould_content = marketResourceDao.queryForObject(sqlB, cust_id, batch_number);
        // 
        list.get(0).put("email_content", email_mould_content);
        return list;
    }


    public String getEmailSuperIdListTotal(String cust_id, String batch_number) {
        String sql = "SELECT count(*) as total FROM `t_touch_email_log` WHERE cust_id=? and batch_number=?";
        String total = marketResourceDao.queryForObject(sql.toString(), cust_id, batch_number);
        return total;
    }


    /**
     * 查询客户群客户数据
     *
     * @param superId
     * @param custGroupId
     * @return
     */
    public Map<String, Object> getSuperData(String superId, String custGroupId) {
        List<Map<String, Object>> superIds = null;
        try {
            superIds = marketResourceDao.sqlQuery("SELECT * FROM t_customer_group_list_" + custGroupId + " WHERE id = ?", superId);
        } catch (Exception e) {
            LOG.error(superId + "查询客户群数据失败,", e);
            return null;
        }
        if (superIds.size() > 0) {
            return superIds.get(0);
        }
        return null;
    }


    /**
     * 根据客户群和身份ID获取手机号
     *
     * @param customerId
     * @param customerGroupId
     * @param superId
     * @return
     */
    public Map<String, Object> getSuperInfoV3(String customerId, int customerGroupId, String superId) {
        Map<String, Object> data = new HashMap<>();
        CustomGroup customGroup = null;
        String hql = "FROM CustomGroup m WHERE m.custId = ? AND m.id = ?";
        List list = customGroupDao.find(hql, customerId, customerGroupId);
        if (list.size() > 0)
            customGroup = (CustomGroup) list.get(0);
        if (customGroup != null) {
            List<Map<String, Object>> superIds = null;
            try {
                superIds = marketResourceDao.sqlQuery("SELECT id, remark, super_name, super_age, super_sex, super_telphone, super_phone,super_address_province_city,super_address_street" +
                        " FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + customerGroupId + " WHERE id = ?", superId);
            } catch (Exception e) {
                LOG.error(superId + "查询客户群数据失败,", e);
                return data;
            }
            if (superIds.size() > 0) {
                for (Map.Entry<String, Object> key : superIds.get(0).entrySet()) {
                    data.put(key.getKey(), key.getValue());
                }
                String phone = phoneService.getPhoneBySuperId(superId);
                data.put("phone", phone);
            }
        }
        return data;
    }


    public String updateResourceForEmail(String code, String cust_id, String user_id, String status, String email_content,
                                         String superid, String templateId, String enpterprise_name, String batch_number, Integer quantity) {
        JSONObject JSON = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        int flagUpdate = 0;
        int flagInsert = 0;
        // 判断金额
        if (code.equals("3")) {
            // 根据资源信息计算所需要的money
            String moneySql = "SELECT resname,sale_price,cost_price,supplier_id FROM t_market_resource where  resource_id=3 and type_code=3 ";
            List<Map<String, Object>> groupConfigList = this.marketResourceDao.sqlQuery(moneySql);
            Double sale_price = Double.parseDouble(groupConfigList.get(0).get("sale_price").toString());
            BigDecimal money = new BigDecimal(sale_price);
            money = money.multiply(new BigDecimal(1));// 默认先一对一计算
            // 账户扣钱（余额支付）查询账户余额
            String amountSql = "SELECT remain_amount FROM	 t_account where cust_id=?";
            Double remain_amount = Double.parseDouble(marketResourceDao.queryForObject(amountSql, cust_id));
            BigDecimal amount = new BigDecimal(remain_amount);
            if (amount.subtract(money).compareTo(new BigDecimal(0)) < 0) {
                // 失败
                return "1";
            } else {
                return "0";
            }
        }
        if (code.equals("1001")) {
            // 根据资源信息计算所需要的money
            String moneySql = "SELECT resname,sale_price,cost_price,supplier_id FROM t_market_resource where  resource_id=3 and type_code=3 ";
            List<Map<String, Object>> groupConfigList = this.marketResourceDao.sqlQuery(moneySql);
            String resname =
                    groupConfigList.get(0).get("resname").toString();
            Double sale_price = Double.parseDouble(groupConfigList.get(0).get("sale_price").toString());
            Double cost_price =
                    Double.parseDouble(groupConfigList.get(0).get("cost_price").toString());

            BigDecimal moneyCost = new BigDecimal(cost_price * quantity);
            moneyCost = moneyCost.multiply(new BigDecimal(1));

            BigDecimal moneySale = new BigDecimal(sale_price * quantity);
            moneySale = moneySale.multiply(new BigDecimal(1));

            int supplier_id =
                    Integer.parseInt(groupConfigList.get(0).get("supplier_id").toString());

            // 账户扣钱（余额支付）查询账户余额
            String amountSql = "SELECT remain_amount FROM	 t_account where cust_id=?";
            Double remain_amount = Double.parseDouble(marketResourceDao.queryForObject(amountSql, cust_id));
            DecimalFormat df = new DecimalFormat("0.00");
            BigDecimal amount = new BigDecimal(remain_amount);
            String now_money = df.format(amount.subtract(moneySale));
            // 更新账户信息（余额扣款）
            String amountUpdateSql = "update t_account  set remain_amount =?,modify_time=now()  where cust_id=?";
            flagUpdate = this.marketResourceDao.executeUpdateSQL(amountUpdateSql, now_money, cust_id);
            if (flagUpdate == 0) {
                map.put("code", "4");
                map.put("message", "支付失败！");
            }
            // 查询账户ID
            String accountId = "SELECT acct_id FROM  t_account WHERE  cust_id = ?";
            accountId = marketResourceDao.queryForObject(accountId, cust_id);
            // t_transaction
            // dto.getTransaction_code() '交易类型（1.充值 2.扣减 3.消费）'
            // dto.getPay_type() 支付类型（1.余额 2.第三方 3.线下）'
            Long transaction_id = IDHelper.getTransactionId();
            Long order_id = IDHelper.getTransactionId();
            Object[] objsTrans = {transaction_id, accountId, cust_id, moneySale, order_id};
            String transactionSql = "insert into t_transaction set transaction_id=?,acct_id=?,cust_id=?,type=2,pay_mode=1,amount=?,remark='邮件扣费',create_time=now(),order_id=?";
            marketResourceDao.insertOrder(transactionSql, objsTrans);

            LOG.info("==向交易表添加记录====" + transactionSql);

            // 向t_order表增加交易记录
            Object[] objsOrder = {order_id, cust_id, quantity, moneySale, moneyCost, supplier_id, moneySale, enpterprise_name, resname};
            String orderSql = "insert into t_order set order_id=?,cust_id=?,order_type=2,pay_type=1,create_time=now(),quantity=?,amount=?,order_state=2,cost_price=?,supplier_id=?,pay_amount=?,enpterprise_name=?,product_name = ?,remarks='邮件扣费',pay_time = NOW()";

            marketResourceDao.insertOrder(orderSql, objsOrder);
            LOG.info("==向t_order表添加记录====" + objsOrder);

//			// 记录log
//			String insertSql = "insert into t_touch_email_log set cust_id=?,user_id=?,create_time=now(),STATUS=?,email_content=?,superid=?,templateId=?,batch_number = ?";
//			
//			flagInsert = this.marketResourceDao.executeUpdateSQL(insertSql,
//					new Object[] { cust_id, user_id, status,email_content, superid, templateId,batch_number});
//			if (flagUpdate == 1 && flagInsert == 1) {
//				map.put("code", "0");
//				map.put("message", "成功");
//			} else {
//				map.put("code", "1");
//				map.put("message", "失败");
//			}

        }


        JSON.put("data", map);
        return JSON.toJSONString();
    }

    public static void main(String[] args) {
        ///
        String sql = "{'primInduName':'租赁和商务服务业','secList':[{'secnduName':'商务服务业','secInduCode':'1901'},{'secnduName':'租赁业','secInduCode':'1900'}],'primInduCode':'19'}#{'primInduName':'水利、环境和公共设施管理业','secList':[{'secnduName':'公共设施管理业','secInduCode':'1702'},{'secnduName':'生态保护和环境治理业','secInduCode':'1700'}],'primInduCode':'17'}#{'primInduName':'采矿业','secList':[{'secnduName':'黑色金属矿采选业','secInduCode':'1606'},{'secnduName':'煤炭开采和洗选业','secInduCode':'1605'},{'secnduName':'非金属矿采选业','secInduCode':'1603'},{'secnduName':'有色金属矿采选业','secInduCode':'1601'}],'primInduCode':'16'}#{'primInduName':'批发和零售业','secList':[{'secnduName':'零售业','secInduCode':'1501'},{'secnduName':'批发业','secInduCode':'1500'}],'primInduCode':'15'}#{'primInduName':'交通运输、仓储和邮政业','secList':[{'secnduName':'装卸搬运和运输代理业','secInduCode':'1407'},{'secnduName':'水上运输业','secInduCode':'1405'},{'secnduName':'道路运输业','secInduCode':'1403'}],'primInduCode':'14'}#{'primInduName':'建筑业','secList':[{'secnduName':'建筑装饰和其他建筑业','secInduCode':'1303'},{'secnduName':'建筑安装业','secInduCode':'1302'},{'secnduName':'土木工程建筑业','secInduCode':'1300'}],'primInduCode':'13'}#{'primInduName':'金融业','secList':[{'secnduName':'保险业','secInduCode':'1200'}],'primInduCode':'12'}#{'primInduName':'电力、热力、燃气及水生产和供应业','secList':[{'secnduName':'燃气生产和供应业','secInduCode':'1102'},{'secnduName':'水的生产和供应业','secInduCode':'1101'},{'secnduName':'电力、热力生产和供应业','secInduCode':'1100'}],'primInduCode':'11'}#{'primInduName':'住宿和餐饮业','secList':[{'secnduName':'餐饮业','secInduCode':'1001'},{'secnduName':'住宿业','secInduCode':'1000'}],'primInduCode':'10'}#{'primInduName':'制造业','secList':[{'secnduName':'有色金属冶炼和压延加工业','secInduCode':'2929'},{'secnduName':'铁路、船舶、航空航天和其他运输设备制造业','secInduCode':'2928'},{'secnduName':'家具制造业','secInduCode':'2927'},{'secnduName':'其他制造业','secInduCode':'2926'},{'secnduName':'木材加工和木、竹、藤、棕、草制品业','secInduCode':'2925'},{'secnduName':'仪器仪表制造业','secInduCode':'2922'},{'secnduName':'皮革、毛皮、羽毛及其制品和制鞋业','secInduCode':'2920'},{'secnduName':'农副食品加工业','secInduCode':'2919'},{'secnduName':'医药制造业','secInduCode':'2918'},{'secnduName':'金属制品业','secInduCode':'2917'},{'secnduName':'汽车制造业','secInduCode':'2916'},{'secnduName':'食品制造业','secInduCode':'2915'},{'secnduName':'黑色金属冶炼和压延加工业','secInduCode':'2914'},{'secnduName':'非金属矿物制品业','secInduCode':'2913'},{'secnduName':'化学原料和化学制品制造业','secInduCode':'2911'},{'secnduName':'废弃资源综合利用业','secInduCode':'2910'},{'secnduName':'文教、工美、体育和娱乐用品制造业','secInduCode':'2909'},{'secnduName':'通用设备制造业','secInduCode':'2908'},{'secnduName':'专用设备制造业','secInduCode':'2907'},{'secnduName':'纺织业','secInduCode':'2906'},{'secnduName':'造纸和纸制品业','secInduCode':'2905'},{'secnduName':'橡胶和塑料制品业','secInduCode':'2904'},{'secnduName':'石油加工、炼焦和核燃料加工业','secInduCode':'2903'},{'secnduName':'印刷和记录媒介复制业','secInduCode':'2902'},{'secnduName':'电气机械和器材制造业','secInduCode':'2901'},{'secnduName':'酒、饮料和精制茶制造业','secInduCode':'2900'},{'secnduName':'计算机、通信和其他电子设备制造业','secInduCode':'2930'}],'primInduCode':'29'}#{'primInduName':'房地产业','secList':[{'secnduName':'房地产业','secInduCode':'2800'}],'primInduCode':'28'}#{'primInduName':'文化、体育和娱乐业','secList':[{'secnduName':'广播、电视、电影和影视录音制作业','secInduCode':'2704'},{'secnduName':'娱乐业','secInduCode':'2703'},{'secnduName':'文化艺术业','secInduCode':'2702'},{'secnduName':'新闻和出版业','secInduCode':'2701'},{'secnduName':'体育','secInduCode':'2700'}],'primInduCode':'27'}#{'primInduName':'科学研究和技术服务业','secList':[{'secnduName':'专业技术服务业','secInduCode':'2602'},{'secnduName':'研究和试验发展','secInduCode':'2601'},{'secnduName':'科技推广和应用服务业','secInduCode':'2600'}],'primInduCode':'26'}#{'primInduName':'教育','secList':[{'secnduName':'教育','secInduCode':'2500'}],'primInduCode':'25'}#{'primInduName':'卫生和社会工作','secList':[{'secnduName':'卫生','secInduCode':'2401'}],'primInduCode':'24'}#{'primInduName':'农、林、牧、渔业','secList':[{'secnduName':'农业','secInduCode':'2204'},{'secnduName':'渔业','secInduCode':'2203'},{'secnduName':'林业','secInduCode':'2202'},{'secnduName':'畜牧业','secInduCode':'2201'},{'secnduName':'农、林、牧、渔服务业','secInduCode':'2200'}],'primInduCode':'22'}#{'primInduName':'信息传输、软件和信息技术服务业','secList':[{'secnduName':'电信、广播电视和卫星传输服务','secInduCode':'2102'},{'secnduName':'互联网和相关服务','secInduCode':'2100'}],'primInduCode':'21'}#{'primInduName':'居民服务、修理和其他服务业','secList':[{'secnduName':'机动车、电子产品和日用产品修理业','secInduCode':'2002'},{'secnduName':'其他服务业','secInduCode':'2001'},{'secnduName':'居民服务业','secInduCode':'2000'}],'primInduCode':'20'}";
        JSONObject json = new JSONObject();
        json.put("data", sql);
        String[] ss = json.get("data").toString().split("#");
        for (int i = 0; i < ss.length; i++) {
            String[] s2 = ss[i].split(",");
            JSONObject jasonObject = JSONObject.parseObject(ss[i]);
            Map map = (Map) jasonObject;
            System.out.println(map.get("primInduCode"));
            //
            List list = (List) map.get("secList");
            for (int y = 0; y < list.size(); y++) {
                Map map2 = (Map) list.get(y);
                System.out.println(map2.get("secInduCode"));
            }
        }
    }


    public List<Map<String, Object>> getCustomerList(String customer_group_id, String cust_id, String user_id, String userType, String id) {
        //判断当前用户的user_type
        StringBuffer sb = new StringBuffer();
        sb.append("select custG.id as superid  from t_customer_group_list_" + cust_id + " custG ");
        sb.append(" LEFT JOIN t_customer_user t  ON custG.user_id = t.id");
        sb.append(" LEFT JOIN t_touch_voice_info t4 ON custG.id = t4.super_id and custG.customer_group_id = t4.cust_group_id");

        sb.append(" where 1=1 ");

        if (null != customer_group_id && !"".equals(customer_group_id)) {
            sb.append(" and custG.customer_group_id=" + customer_group_id);
        }
        if (null != id && !"".equals(id)) {
            sb.append(" and custG.id like '%" + id + "%'");
        }

        if (!"1".equals(userType)) {
            sb.append(" AND custG.user_id= " + user_id);
        }
        return this.marketResourceDao.sqlQuery(sb.toString());
    }


    public boolean updateCallCountAndCallTime(String customerId, String customerGroupId, String[] superIds) {
        boolean status = false;
        if (StringUtil.isNotEmpty(customerGroupId) && StringUtil.isNotEmpty(customerId)
                && superIds != null && superIds.length > 0) {
            StringBuffer sb = new StringBuffer();
            sb.append("SELECT id, call_count FROM t_customer_group_list_" + customerId);
            sb.append(" WHERE 1=1 ");
            sb.append(" AND cust_id = ? ");
            sb.append(" AND id = ? ");
            sb.append(" AND customer_group_id = ? ");
            List<Map<String, Object>> list;
            long callCount;
            for (String superId : superIds) {
                list = this.marketResourceDao.sqlQuery(sb.toString(), customerId, superId, customerGroupId);
                if (list != null && list.size() > 0) {
                    callCount = 0L;
                    if (list.get(0).get("call_count") != null) {
                        callCount = (long) list.get(0).get("call_count");
                    }
                    sb.setLength(0);
                    sb.append("UPDATE t_customer_group_list_" + customerId);
                    sb.append(" SET call_count = ? ,");
                    sb.append(" last_call_time = ? ");
                    sb.append(" WHERE cust_id = ? ");
                    sb.append(" AND id = ? ");
                    sb.append(" AND customer_group_id = ? ");
                    this.marketResourceDao.executeUpdateSQL(sb.toString(), callCount + 1, new Timestamp(System.currentTimeMillis()), customerId, superId, customerGroupId);
                }
            }
            status = true;
        }
        return status;
    }


    public int updateCallCountV2(String customerGroupId, String superId) {
        try {
            String nowTime = LocalDateTime.of(LocalDate.now(), LocalTime.now()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String sql = "UPDATE t_customer_group_list_" + customerGroupId + " SET call_count = IFNULL(call_count,0) +1 , last_call_time = ? WHERE id =?";
            return this.marketResourceDao.executeUpdateSQL(sql, nowTime, superId);
        } catch (Exception e) {
            LOG.error("更新客户群通话次数异常", e);
            return 0;
        }
    }

    public int updateSeaCallCount(String seaId, String superId) {
        try {
            String nowTime = LocalDateTime.of(LocalDate.now(), LocalTime.now()).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            String sql = "UPDATE t_customer_sea_list_" + seaId + " SET call_count = IFNULL(call_count,0) +1 , last_call_time = ? WHERE id =?";
            return this.marketResourceDao.executeUpdateSQL(sql, nowTime, superId);
        } catch (Exception e) {
            LOG.error("更新私海通话次数异常", e);
            return 0;
        }
    }


    public String getMarketResourceList() {
        StringBuffer sb = new StringBuffer();
        JSONObject json = new JSONObject();
        sb.append(" SELECT");
        sb.append(" 	resource_id,");
        sb.append(" 	resname,");
        sb.append(" 	type_code,");
        sb.append(" 	(");
        sb.append(" 		SELECT");
        sb.append(" 			`NAME`");
        sb.append(" 		FROM");
        sb.append(" 			t_supplier ss");
        sb.append(" 		WHERE");
        sb.append(" 			ss.supplier_id = tt.supplier_id");
        sb.append(" 	)as name,");
        sb.append(" 	supplier_id,");
        sb.append(" 	FORMAT(sale_price/100,2)as sale_price,");
        sb.append(" 	FORMAT(cost_price/100,2)as cost_price,");
        sb.append(" 	`STATUS`");
        sb.append(" FROM");
        sb.append(" 	`t_market_resource` tt where tt.`STATUS`=1");
        List<Map<String, Object>> list = this.marketResourceDao.sqlQuery(sb.toString());
        json.put("data", list);
        return json.toJSONString();
    }


    public String updateMarketResource(String type_code, String resource_id, String price_code, Double price, String operator) {
        StringBuffer sb = new StringBuffer();
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        //查询旧价格
        //销售价格
        String priceOld = "";
        String price_type = "";
        if (price_code.equals("sale")) {
            price_type = "2";
            priceOld = "SELECT sale_price from t_market_resource  where resource_id=? and type_code=?";
        }
        //成本价格
        if (price_code.equals("cost")) {
            price_type = "1";
            priceOld = "SELECT cost_price from t_market_resource  where resource_id=? and type_code=?";
        }
        String oldPrice = marketResourceDao.queryForObject(priceOld, resource_id, type_code);
        //更新价格
        //销售价格
        if (price_code.equals("sale")) {
            sb.append("UPDATE t_market_resource set sale_price=?,modify_time=NOW() where resource_id=? and type_code=?");
        }
        //成本价格
        if (price_code.equals("cost")) {
            sb.append("UPDATE t_market_resource set cost_price=?,modify_time=NOW() where resource_id=? and type_code=?");
        }
        int flag = this.marketResourceDao.executeUpdateSQL(sb.toString(), price * 100, resource_id, type_code);
        //记录log
        String sql = "insert into  t_market_resource_log SET resource_id=?,old_price=?,new_price=?,create_time=NOW(),operator=?,type_code=?,price_code=?";
        int flagLog = this.marketResourceDao.executeUpdateSQL(sql, resource_id, oldPrice, price * 100, operator, type_code, price_type);
        if (flagLog == 1 && flag == 1) {
            map.put("code", "0");
            map.put("message", "成功");
        } else {
            map.put("code", "1");
            map.put("message", "失败");
        }
        json.put("data", map);
        return json.toJSONString();
    }


    public List<Map<String, Object>> getMarketResourceLog(String price_code, String type_code, String resource_id, Integer pageNum, Integer pageSize) {
        StringBuffer sb = new StringBuffer();
        //1成本价 2销售价
        //成本价格
        if (price_code.equals("cost")) {
            sb.append("SELECT FORMAT(old_price/100,2)as old_price,FORMAT(new_price/100,2)as new_price,create_time,operator from t_market_resource_log where resource_id=? and type_code=? and price_code=1 ORDER BY create_time DESC LIMIT ?,? ");
        }
        //销售价格
        if (price_code.equals("sale")) {
            sb.append("SELECT FORMAT(old_price/100,2)as old_price,FORMAT(new_price/100,2)as new_price,create_time,operator from t_market_resource_log where resource_id=? and type_code=? and price_code=2 ORDER BY create_time DESC LIMIT ?,?");
        }
        List<Map<String, Object>> list = this.marketResourceDao.sqlQuery(sb.toString(), resource_id, type_code, pageNum, pageSize);
        return list;
    }


    public String getMarketResourceLogTotal(String price_code, String type_code, String resource_id) {
        StringBuffer sb = new StringBuffer();
        //1成本价 2销售价
        //成本价格
        if (price_code.equals("cost")) {
            sb.append("SELECT count(*) as total from t_market_resource_log where resource_id=? and type_code=? and price_code=1 ");
        }
        //销售价格
        if (price_code.equals("sale")) {
            sb.append("SELECT count(*) as total from t_market_resource_log where resource_id=? and type_code=? and price_code=2 ");
        }
        String total = marketResourceDao.queryForObject(sb.toString(), resource_id, type_code);
        return total;
    }


    public String getPhoneAttributionAreaV1(int type, String uuid, String custGroupId) {
        String area = "";
        // type为1时uuid则为手机号
        LOG.info("获取手机号归属地type:" + type + ",uuid:" + uuid + ",custGroupId:" + custGroupId);
        List<Map<String, Object>> list;

        // type为1时为手机号
        if (1 == type) {
            area = customGroupService.getPhoneAreaByPhone(custGroupId, uuid);
        } else if (2 == type) {
            // type为2为身份ID
            area = customGroupService.getPhoneAreaBySuperId(custGroupId, uuid);
        }
        LOG.info("查询客户群数据表归属地,type:" + type + ",uuid:" + uuid + ",custGroupId:" + custGroupId + "归属地:" + area);
        if (StringUtil.isEmpty(area) || "null".equals(area)) {
            area = "未知";
        }
        return area;
    }

    public boolean saveVoiceIntention(List<String> touchIds, int intentStatus, String time) {
        LOG.info("开始更新人工审核意向度,touchIds:" + JSON.toJSONString(touchIds) + ",intentStatus:" + intentStatus + ",time:" + time);
        int updateCount = marketResourceDao.batchVoiceIntentStatus(touchIds, intentStatus, LocalDateTime.parse(time, DFT).format(YYYYMM));
        LOG.info("人工审核意向度更新成功条数:" + updateCount);
        if (updateCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * 通话记录审核
     *
     * @param touchIds
     * @param intentStatus
     * @param time
     * @param clueAuditReason
     * @return
     */
    public boolean saveVoiceIntention(List<String> touchIds, int intentStatus, String time, String clueAuditReason) {
        LOG.info("开始更新人工审核意向度,touchIds:" + JSON.toJSONString(touchIds) + ",intentStatus:" + intentStatus + ",time:" + time);
        int updateCount = marketResourceDao.batchVoiceIntentStatus(touchIds, intentStatus, LocalDateTime.parse(time, DFT).format(YYYYMM), clueAuditReason);
        LOG.info("人工审核意向度更新成功条数:" + updateCount);
        if (updateCount > 0) {
            return true;
        }
        return false;
    }

    public boolean saveBatchVoiceIntention(VoiceLogQueryParam param) {
        LOG.info("开始批量更新人工审核意向度,param:" + param);
        CustomerUser user = null;
        if (StringUtil.isNotEmpty(param.getUserName())) {
            user = this.customerUserDao.getCustomerUserByName(param.getUserName().trim());
            if (user == null) {
                // 穿透查询一次登陆名称
                user = this.customerUserDao.getCustomerUserByLoginName(param.getUserName().trim());
            }
            if (user == null) {
                LOG.warn("批量更新人工审核用户未查询到,user:" + user + ",userName:" + param.getUserName());
                return false;
            } else {
                param.setUserId(String.valueOf(user.getId()));
            }
        }

        int updateCount = marketResourceDao.batchVoiceIntentStatus(param, LocalDateTime.parse(param.getEndTime(), DFT).format(YYYYMM));
        LOG.info("人工审核意向度更新成功条数:" + updateCount);
        if (updateCount > 0) {
            return true;
        }
        return false;
    }

    /**
     * 根据检索条件批量审核通话记录
     *
     * @param param
     * @return
     */
    public boolean saveBatchVoiceIntention0(VoiceLogQueryParam param) {
        LOG.info("开始批量更新人工审核意向度,param:" + param);
        CustomerUser user = null;
        if (StringUtil.isNotEmpty(param.getUserName())) {
            user = this.customerUserDao.getCustomerUserByName(param.getUserName().trim());
            if (user == null) {
                // 穿透查询一次登陆名称
                user = this.customerUserDao.getCustomerUserByLoginName(param.getUserName().trim());
            }
            if (user == null) {
                LOG.warn("批量更新人工审核用户未查询到,user:" + user + ",userName:" + param.getUserName());
                return false;
            } else {
                param.setUserId(String.valueOf(user.getId()));
            }
        }

        int updateCount = marketResourceDao.batchVoiceIntentStatus0(param, LocalDateTime.parse(param.getEndTime(), DFT).format(YYYYMM));
        LOG.info("人工审核意向度更新成功条数:" + updateCount);
        if (updateCount > 0) {
            return true;
        }
        return false;
    }


    public void getVoiceFile(String userId, String touchId, HttpServletRequest request, HttpServletResponse response) {
        // 设置响应头
        response.addHeader("Accept-Ranges", "bytes");
        response.addHeader("Content-Type", "audio/mpeg;charset=UTF-8");
        String range = request.getHeader("Range");
        FileInputStream fis = null;
        ByteArrayInputStream hBaseInputStream = null;
        try {
            if (StringUtil.isEmpty(touchId)) {
                LOG.warn("获取录音文件touchId为空:" + touchId);
                return;
            }
            String yearMonth = touchId.substring(0, 6);
            String realTouchId = null;
            if (touchId.indexOf(".") > 0) {
                realTouchId = touchId.substring(6, touchId.length() - 4);
            } else {
                realTouchId = touchId.substring(6);
            }
            Map<String, Object> voiceLog = getRecordVoiceLogByTouchId(realTouchId, yearMonth);
            if (voiceLog == null || voiceLog.size() == 0) {
                LOG.warn("获取录音文件未查询到通话日志touchId:" + touchId + ",realTouchId:" + realTouchId + ",yearMonth:" + yearMonth);
                return;
            }
            String recordUrl = String.valueOf(voiceLog.getOrDefault("recordurl", ""));
            String fileName = "";
            if (StringUtil.isNotEmpty(recordUrl)) {
                fileName = recordUrl.substring(recordUrl.lastIndexOf("/") + 1, recordUrl.length());
            }
            if (StringUtil.isEmpty(fileName)) {
                LOG.warn("获取录音文件未查询文件名称touchId:" + touchId + ",fileName:" + fileName);
                return;
            }

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
     * 批量发送营销类短信
     *
     * @param custGroupId
     * @param templateId
     * @param superIds
     * @param userId
     * @param callId      如果是挂机短信,则需要传递通话唯一标识参数来确定是哪通电话
     */
    public void sendBatchMarketSms(String custGroupId, String templateId, Set<String> superIds, String userId, String callId) {
        // 查询客户群信息
        CustomGroup cg = customGroupDao.get(NumberConvertUtil.parseInt(custGroupId));
        if (cg == null) {
            LOG.warn("批量发送营销短信未查询到客户群:" + cg);
            return;
        }
        String sendResult;
        LOG.info("开始发送营销短信,custId:" + cg.getCustId() + ",custGroupId:" + custGroupId + ",templateId:" + templateId + ",callId:" + callId + ",time:" + LocalDateTime.now() + ",数量:" + superIds.size());
        for (String superId : superIds) {
            sendResult = sendSmsService.sendMarketSms(cg.getCustId(), custGroupId, superId, userId, templateId, callId);
            LOG.info("发送营销短信记录,custId:" + cg.getCustId() + ",custGroupId:" + custGroupId + ",superId:" + superId +
                    ",userId:" + userId + ",templateId:" + templateId + ",sendResult:" + sendResult);
        }
        LOG.info("结束发送营销短信,custId:" + cg.getCustId() + ",custGroupId:" + custGroupId + ",templateId:" + templateId + ",callId:" + callId + ",time:" + LocalDateTime.now());
    }


    /**
     * 创建营销模板
     *
     * @param m
     * @return
     */
    public int saveMarketTemplate(MarketTemplate m) {
        // 审核中
        m.setStatus(1);
        m.setCreateTime(new Timestamp(System.currentTimeMillis()));
        return (int) marketTemplateDao.saveReturnPk(m);
    }

    /**
     * 根据触达ID查询通话记录
     *
     * @param touchId
     * @param custId
     * @return
     */
    public Map<String, Object> getRecordVoiceLogByTouchId(String touchId, String custId, LocalDateTime yearMonth, String startTime, String endTime) {
        List<Map<String, Object>> list = null;
        try {
            StringBuffer sb = new StringBuffer();
            String monthTableName = ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + yearMonth.format(YYYYMM);
            sb.append("select voicLog.touch_id touchId, voicLog.callSid, voicLog.superid,voicLog.create_time, voicLog.status, CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,")
                    .append(" voicLog.call_data, voicLog.recordurl, voicLog.called_duration ")
                    .append(" from " + monthTableName + " voicLog ")
                    .append(" WHERE voicLog.cust_id= ? ")
                    .append(" AND voicLog.touch_id= ? ");
            // 处理开始和结束数据搜索
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                sb.append(" AND voicLog.create_time BETWEEN '" + startTime + "' and '" + endTime + "' ");
            } else {
                if (StringUtil.isNotEmpty(startTime)) {
                    sb.append(" AND voicLog.create_time >= '" + startTime + "'");
                }
                if (StringUtil.isNotEmpty(endTime)) {
                    sb.append(" AND voicLog.create_time <= '" + endTime + "'");
                }
            }
            list = this.marketResourceDao.sqlQuery(sb.toString(), custId, touchId);
        } catch (Exception e) {
            LOG.error("根据触达ID查询通话记录失败,touchId:" + touchId, e);
        }
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public Map<String, Object> getRecordVoiceLogByTouchId(String touchId, String yearMonth) {
        List<Map<String, Object>> list = null;
        try {
            StringBuffer sb = new StringBuffer();
            String monthTableName = ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + yearMonth;
            sb.append("select voicLog.touch_id touchId, voicLog.callSid, voicLog.superid,voicLog.create_time, voicLog.status, CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,")
                    .append(" voicLog.call_data, voicLog.recordurl, voicLog.called_duration ")
                    .append(" from " + monthTableName + " voicLog ")
                    .append(" WHERE voicLog.touch_id= ? ");
            list = this.marketResourceDao.sqlQuery(sb.toString(), touchId);
        } catch (Exception e) {
            LOG.error("根据触达ID查询通话记录失败,touchId:" + touchId, e);
        }
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 根据触达ID获取录音文件Base64字符串
     *
     * @param userId
     * @param recordUrl
     * @return
     */
    public String getVoiceBase64ByTouchId(String userId, String recordUrl) {
        LOG.info("根据触达ID查询通话录音,userId:" + userId + ",recordurl:" + recordUrl);
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(recordUrl)) {
            LOG.warn("根据触达ID查询通话录音地址参数异常,userId:" + userId + ",recordurl:" + recordUrl);
            return null;
        }
        String fileName = recordUrl.substring(recordUrl.lastIndexOf("/") + 1);
        String filePath = PropertiesUtil.getStringValue("audiolocation") + userId + File.separator + fileName;
        LOG.info("根据触达ID查询通话录音,userId:" + userId + ",filePath:" + filePath);
        FileInputStream fis = null;
        InputStream ins = null;
        try {
            File file = new File(filePath);
            byte[] bytes = null;
            if (file.exists()) {
                fis = new FileInputStream(file);
                bytes = new byte[(int) file.length()];
                fis.read(bytes);
            } else {
                // 直接查询呼叫中心的录音文件
                ins = HttpUtil.getInputStream(recordUrl);
                // 穿透查询HBase录音文件
                if (ins == null) {
                    String base64 = CallUtil.getVoiceBase64Data(fileName);
                    return base64;
                } else {
                    bytes = IOUtils.toByteArray(ins);
                }
            }
            if (bytes != null && bytes.length > 0) {
                return Base64.encodeBase64String(bytes);
            }
        } catch (Exception e) {
            LOG.error("根据触达ID查询通话录音读取文件异常", e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (ins != null) {
                    ins.close();
                }
            } catch (Exception e) {
                LOG.error("根据触达ID查询通话录音读取文件异常", e);
            }
        }
        LOG.warn("根据触达ID未找到通话录音文件,userId:" + userId + ",filePath:" + filePath);
        return null;
    }

    /**
     * 获取录音流
     *
     * @param userId
     * @param recordUrl
     * @return
     */
    public InputStream getVoiceInputStream(String userId, String recordUrl) {
        LOG.info("根据触达ID查询通话录音,userId:" + userId + ",recordurl:" + recordUrl);
        if (StringUtil.isEmpty(userId) || StringUtil.isEmpty(recordUrl)) {
            LOG.warn("根据触达ID查询通话录音地址参数异常,userId:" + userId + ",recordurl:" + recordUrl);
            return null;
        }
        String fileName = recordUrl.substring(recordUrl.lastIndexOf("/") + 1);
        String filePath = PropertiesUtil.getStringValue("audiolocation") + userId + File.separator + fileName;
        LOG.info("根据触达ID查询通话录音,userId:" + userId + ",filePath:" + filePath);
        FileInputStream fis = null;
        InputStream ins = null;
        try {
            File file = new File(filePath);
            if (file.exists()) {
                fis = new FileInputStream(file);
                return fis;
            } else {
                // 直接查询呼叫中心的录音文件
                ins = HttpUtil.getInputStream(recordUrl);
                if (ins != null) {
                    return ins;
                } else {
                    // 穿透查询HBase录音文件
                    String base64 = CallUtil.getVoiceBase64Data(fileName);
                    if (StringUtil.isNotEmpty(base64)) {
                        byte[] bytes = org.apache.commons.codec.binary.Base64.decodeBase64(base64);
                        ByteArrayInputStream hBaseInputStream = new ByteArrayInputStream(bytes);
//                      int length = hBaseInputStream.available();
                        return hBaseInputStream;
                    } else {
                        LOG.warn("通过HBase读取录音文件base64字符串为空,userId:" + userId + ",fileName:" + fileName + ",base64Str:" + base64);
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("根据触达ID查询通话录音读取文件异常", e);
        }
        LOG.warn("根据触达ID未找到通话录音文件,userId:" + userId + ",filePath:" + filePath);
        return null;
    }

    /**
     * 通话记录接口(企业名称查询)
     *
     * @param userQueryParam
     * @param customerGroupId
     * @param superId
     * @param realName
     * @param createTimeStart
     * @param createTimeEnd
     * @param remark
     * @param callStatus
     * @param level
     * @param auditingStatus
     * @return
     */
    public com.bdaim.common.dto.Page queryRecordVoiceLogV3(UserQueryParam userQueryParam, String customerGroupId, String superId, String realName,
                                                           String createTimeStart, String createTimeEnd, String remark, String callStatus, String level,
                                                           String auditingStatus, String marketTaskId, int calledDuration, String custProperty, String seaId, String custName) {
        com.bdaim.common.dto.Page page = null;
        int taskType = -1;
        try {
            StringBuffer sb = new StringBuffer();
            LocalDate endTime = LocalDate.now();
            // 开始时间和结束时间为空时默认查当天
            if (StringUtil.isEmpty(createTimeStart)) {
                createTimeStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(YYYYMMDDHHMMSS);
            }
            if (StringUtil.isEmpty(createTimeEnd)) {
                createTimeEnd = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(YYYYMMDDHHMMSS);
            } else {
                endTime = LocalDate.parse(createTimeEnd, YYYYMMDDHHMMSS);
            }

            // 检查通话记录月表是否存在
            marketResourceDao.createVoiceLogTableNotExist(endTime.format(YYYYMM));

            String monthTableName = ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + endTime.format(YYYYMM);
            // 处理任务类型
            MarketTask marketTask = null;
            if (StringUtil.isNotEmpty(marketTaskId)) {
                marketTask = marketTaskDao.get(marketTaskId);
                if (marketTask != null && marketTask.getTaskType() != null) {
                    taskType = marketTask.getTaskType();
                }
            }
            if (marketTask == null) {
                marketTask = new MarketTask();
            }

            sb.append("select voicLog.touch_id touchId, voicLog.callSid, voicLog.superid,voicLog.create_time create_time,voicLog.status, CAST(voicLog.user_id AS CHAR) user_id,voicLog.remark,")
                    .append(" voicLog.call_data, voicLog.recordurl, voicLog.clue_audit_status auditingStatus, voicLog.market_task_id marketTaskId, voicLog.clue_audit_reason reason ,cust.enterprise_name custName")
                    .append("  from " + monthTableName + " voicLog ")
                    .append(" left join t_customer cust on voicLog.cust_id=cust.cust_id ");
            // 处理自建属性搜索
            if (StringUtil.isNotEmpty(custProperty) && StringUtil.isNotEmpty(marketTaskId) && !"[]".equals(custProperty)) {
                // 查询所有自建属性
                List<CustomerLabel> customerLabels = customerLabelDao.listCustomerLabel(marketTask.getCustId());
                Map<String, CustomerLabel> cacheLabel = new HashMap<>();
                for (CustomerLabel c : customerLabels) {
                    cacheLabel.put(c.getLabelId(), c);
                }
                sb.append(" INNER JOIN " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " t2 ON t2.id = voicLog.superid ");
                JSONObject jsonObject;
                String labelId, optionValue, likeValue;
                JSONArray jsonArray = JSON.parseArray(custProperty);
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    if (jsonObject != null) {
                        labelId = jsonObject.getString("labelId");
                        optionValue = jsonObject.getString("optionValue");
                        // 文本和多选支持模糊搜索
                        if (cacheLabel.get(labelId) != null && cacheLabel.get(labelId).getType() != null
                                && (cacheLabel.get(labelId).getType() == 1 || cacheLabel.get(labelId).getType() == 3)) {
                            likeValue = "'$." + labelId + "'" + "like '%" + optionValue + "%'";
                        } else {
                            likeValue = "'$." + labelId + "'" + "like '%" + optionValue + "%'";
                        }
                        sb.append(" AND t2.super_data -> " + likeValue + " ");
                    }
                }
            }

            sb.append(" WHERE 1=1");
            if ("-1".equals(userQueryParam.getCustId())) {
                sb.append(" AND voicLog.cust_id IS NOT NULL ");
            } else {
                sb.append(" AND voicLog.cust_id='" + userQueryParam.getCustId() + "'");
            }
            // 处理根据登陆账号或者用户姓名搜索
            CustomerUser user = null;
            if (StringUtil.isNotEmpty(realName)) {
                user = this.customerUserDao.getCustomerUserByName(realName.trim());
                if (user != null) {
                    sb.append(" AND  voicLog.user_id = '" + user.getId() + "'");
                } else {
                    // 穿透查询一次登陆名称
                    user = this.customerUserDao.getCustomerUserByLoginName(realName.trim());
                    if (user != null) {
                        sb.append(" AND  voicLog.user_id = '" + user.getId() + "'");
                    } else {
                        return new com.bdaim.common.dto.Page();
                    }
                }
            }
            if (StringUtil.isNotEmpty(customerGroupId)) {
                sb.append(" AND voicLog.customer_group_id =" + customerGroupId.trim());
            }
            if (StringUtil.isNotEmpty(remark)) {
                sb.append(" AND voicLog.remark LIKE '%" + remark.trim() + "%'");
            }
            if (StringUtil.isNotEmpty(superId)) {
                sb.append(" AND voicLog.superid='" + superId.trim() + "'");
            }
            if (StringUtil.isNotEmpty(marketTaskId)) {
                sb.append(" AND voicLog.market_task_id='" + marketTaskId.trim() + "'");
            }
            if (StringUtil.isNotEmpty(custName)) {
                sb.append(" AND cust.enterprise_name like '%" + custName + "%'");
            }
            //　处理通话状态查询
            if (StringUtil.isNotEmpty(callStatus)) {
                // 成功
                if ("1".equals(callStatus)) {
                    sb.append(" AND voicLog.status = 1001");
                    //失败
                } else if ("2".equals(callStatus)) {
                    sb.append(" AND voicLog.status = 1002");
                } else {
                    sb.append(" AND voicLog.status = " + CallStatusEnum.getByType(NumberConvertUtil.parseInt(callStatus)).getStatus());
                }
            }
            // 处理开始和结束数据搜索
            if (StringUtil.isNotEmpty(createTimeStart) && StringUtil.isNotEmpty(createTimeEnd)) {
                sb.append(" AND voicLog.create_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
            } else {
                if (StringUtil.isNotEmpty(createTimeStart)) {
                    sb.append(" AND voicLog.create_time > '" + createTimeStart + "'");
                }
                if (StringUtil.isNotEmpty(createTimeEnd)) {
                    sb.append(" AND voicLog.create_time < '" + createTimeEnd + "'");
                }
            }
            //type 0 查詢全部   1查詢<=3  2、3s-6s 3.6s-12s  4.12s-30s 5.30s-60s 6.>60s
            if (calledDuration == 1) {
                sb.append(" AND voicLog.called_duration<=3");
            } else if (calledDuration == 2) {
                sb.append(" AND voicLog.called_duration>3 AND voicLog.called_duration<=6");
            } else if (calledDuration == 3) {
                sb.append(" AND voicLog.called_duration>6 AND voicLog.called_duration<=12");
            } else if (calledDuration == 4) {
                sb.append(" AND voicLog.called_duration>12 AND voicLog.called_duration<=30");
            } else if (calledDuration == 5) {
                sb.append(" AND voicLog.called_duration>30 AND voicLog.called_duration<=60");
            } else if (calledDuration == 6) {
                sb.append(" AND voicLog.called_duration>60");
            }
            // 处理机器人外呼任务营销记录
            if (3 == taskType) {
                // 处理机器人外呼的意向度
                if (StringUtil.isNotEmpty(level)) {
                    String levelLike = "\"level\":\"" + level + "\"";
                    sb.append(" AND voicLog.call_data LIKE '%" + levelLike + "%'");
                }
                // 处理按照操作人搜索营销记录时机器人外呼任务记录可以搜到
                if (user != null) {
                    sb.append(" AND (voicLog.user_id = '" + user.getId() + "' OR voicLog.call_data LIKE '%level%')");
                }
                // 处理人工审核搜索条件
                if (StringUtil.isNotEmpty(auditingStatus)) {
                    sb.append(" AND voicLog.clue_audit_status = " + auditingStatus);
                }
            }
            // 处理组长权限
            if (UserService.OPERATOR_USER_TYPE.equals(userQueryParam.getUserType())) {
                // 组长查组员列表
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    // 处理组长下有员工的情况
                    if (customerUserDTOList.size() > 0) {
                        Set<String> userIds = new HashSet<>();
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sb.append(" AND (voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR voicLog.call_data LIKE '%level%')");
                            } else {
                                sb.append(" AND voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sb.append(" AND (voicLog.user_id = '" + userQueryParam.getUserId() + "' OR voicLog.call_data LIKE '%level%')");
                        } else {
                            sb.append(" AND voicLog.user_id = '" + userQueryParam.getUserId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        sb.append(" AND (voicLog.user_id = '" + userQueryParam.getUserId() + "' OR voicLog.call_data LIKE '%level%')");
                    } else {
                        sb.append(" AND voicLog.user_id = '" + userQueryParam.getUserId() + "'");
                    }
                }
            }
            // 根据公海ID查询通话记录
            if (StringUtil.isNotEmpty(seaId)) {
                sb.append(" AND voicLog.customer_sea_id = '" + seaId + "'");
            }
            sb.append(" order by voicLog.create_time DESC");
            page = this.marketResourceDao.sqlPageQuery0(sb.toString(), userQueryParam.getPageNum(), userQueryParam.getPageSize());
            CustomerUser customerUser;
            if (page.getData() != null && page.getData().size() > 0) {
                //处理用户信息和录音文件
                String monthYear = endTime.format(YYYYMM);
                VoiceLogCallDataDTO voiceLogCallDataDTO;
                MarketTask task;
                List<Map<String, Object>> list = page.getData();
                for (Map<String, Object> map : list) {
                    if (StringUtil.isNotEmpty(String.valueOf(map.get("create_time")))) {
                        monthYear = LocalDateTime.parse(String.valueOf(map.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                    }
                    map.put("recordurl", CallUtil.generateRecordNameToMp3(monthYear, map.get("touchId")));
                    // 处理通话call_data json数据
                    if (map.get("call_data") != null) {
                        voiceLogCallDataDTO = JSON.parseObject(String.valueOf(map.get("call_data")), VoiceLogCallDataDTO.class);
                        if (voiceLogCallDataDTO != null) {
                            // 处理通话时长
                            if (StringUtil.isNotEmpty(voiceLogCallDataDTO.getCalledDuration())
                                    && !"null".equals(voiceLogCallDataDTO.getCalledDuration())) {
                                map.put("Callerduration", voiceLogCallDataDTO.getCalledDuration());
                            } else {
                                map.put("Callerduration", "");
                            }
                            // 处理机器人外呼的意向度
                            if (3 == taskType) {
                                map.put("intentLevel", voiceLogCallDataDTO.getLevel());
                            } else {
                                map.put("intentLevel", "");
                            }
                        }
                    }
                    // 处理机器人外呼的操作人为robot+机器人id
                    if (3 == taskType) {
                        customerUser = customerUserDao.get(NumberConvertUtil.parseLong(String.valueOf(map.get("user_id"))));
                        if (customerUser == null) {
                            if (map.get("user_id") == null) {
                                map.put("name", "");
                                map.put("realname", "");
                            } else {
                                if ("0".equals(String.valueOf(map.get("user_id")))) {
                                    map.put("name", "");
                                    map.put("realname", "");
                                } else {
                                    map.put("name", "robot" + map.get("user_id"));
                                    map.put("realname", "robot" + map.get("user_id"));
                                }
                            }
                        } else {
                            map.put("name", customerUser.getAccount());
                            map.put("realname", customerUser.getAccount());
                        }
                    } else {
                        map.put("name", customerUserDao.getLoginName(String.valueOf(map.get("user_id"))));
                        map.put("realname", customerUserDao.getLoginName(String.valueOf(map.get("user_id"))));
                    }
                    // 处理呼叫中心外呼无意向度
                    if (map.get("intentLevel") == null) {
                        map.put("intentLevel", "");
                    }
                    //不返回通话自定义json字段
                    map.remove("call_data");
                    // 营销任务名称处理
                    if (map.get("marketTaskId") != null) {
                        task = marketTaskDao.get(String.valueOf(map.get("marketTaskId")));
                        map.put("marketTaskName", task != null ? task.getName() : "");
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("查询通话记录失败,", e);
        }
        return page;
    }

    /**
     * 根据条件检索资源列表
     *
     * @param custId
     * @param param
     * @return
     */
    public List<MarketResourceDTO> listResource(String custId, JSONObject param) {
        List<MarketResourceDTO> list = marketResourceDao.listMarketResource(param.getString("supplierId"), param.getIntValue("busiType"), param);
        ResourcePropertyEntity marketResourceProperty;
        JSONObject jsonObject;
        SupplierEntity supplierDO;
        List<MarketResourceDTO> result = new ArrayList<>();
        for (MarketResourceDTO m : list) {
            supplierDO = supplierDao.getSupplier(NumberConvertUtil.parseInt(m.getSupplierId()));
            if (supplierDO != null && supplierDO.getStatus() != null && 1 == supplierDO.getStatus()) {
                m.setSupplierName(supplierDO.getName());
                marketResourceProperty = marketResourceDao.getProperty(String.valueOf(m.getResourceId()), "price_config");
                if (marketResourceProperty != null) {
                    m.setResourceProperty(marketResourceProperty.getPropertyValue());
                }
                result.add(m);
            }
        }
        return result;
    }

    public int saveMarketResource(String name, Integer supplierId, String price, Integer type) {
        String sql = "INSERT into t_market_resource(supplier_id,type_code,resname,sale_price,create_time) VALUES(?,?,?,?,?)";
        return jdbcTemplate.update(sql, new Object[]{supplierId, type, name, price, DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)});
    }

    public int updateMarketResource(String name, Integer supplierId, String price, Integer type, Integer resource_id) {
        double price1 = 0.0;
        StringUtil.isNotEmpty(price);
        price1 = Double.valueOf(price);
        price1 = price1 * 10000;
//        BigDecimal b2 = new BigDecimal(Double.valueOf(price));
        String sql = "select resource_id,create_time from t_market_resource where resource_id=" + resource_id;
        List<Map<String, Object>> maps = jdbcTemplate.queryForList(sql);
        Object createTime=DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss);
        if(maps.size()>0){
            Map<String, Object> map = maps.get(0);
            createTime=  map.get("create_time");
        }
        String sql1 = "REPLACE  into t_market_resource(resource_id,supplier_id,type_code,resname,sale_price,create_time) VALUES(?,?,?,?,?,?)";

        int update = jdbcTemplate.update(sql1, new Object[]{resource_id, supplierId, type, name, Double.valueOf(price1).intValue(),createTime });
        return update;
    }


    /**
     * 根据条件检索资源列表
     *
     * @param
     * @param param
     * @return
     */
    public Map<String, Object> listResource1(PageParam page, JSONObject param) {

        List dataList = new ArrayList();
        StringBuffer sql = new StringBuffer();
        Map<String, Object> map = new HashMap<>();
        sql.append("select re.resource_id as resourceId, re.supplier_id as supplierId , re.resname as resname , re.type_code as typeCode, re.sale_price as salePrice , re.create_time as createTime");
        sql.append(",su.name as supplierName ");
        sql.append(" from t_market_resource re left join t_supplier su on re.supplier_id=su.supplier_id where 1=1");
        if (StringUtil.isNotEmpty(param.getString("supplierId"))) {
            sql.append(" and re.supplier_id =" + param.getString("supplierId"));
        }
        if (StringUtil.isNotEmpty(param.getString("resname"))) {
            sql.append(" and re.resname like '%" + param.getString("resname") + "%'");
        }
        if (StringUtil.isNotEmpty(param.getString("resourceId"))) {
            sql.append(" and re.resource_id =" + param.getInteger("resourceId"));
        }
        sql.append(" order by re.create_time desc");
        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        List<ApiProperty> rsIds = apiDao.getPropertyAll("rsIds");

        Map<String, List<String>> propertyMap = new HashMap<>();

        rsIds.stream().forEach(pro -> {
            JSONArray.parseArray(pro.getPropertyValue()).stream().forEach(e -> {
                JSONObject jsonObject = JSONObject.parseObject(e.toString());
                log.info("1111:" + e.toString());
                Arrays.stream(jsonObject.getString("rsId").split(",")).forEach(reid -> {
                    log.info("2222:" + reid);
                    if (!propertyMap.containsKey(reid)) {
                        propertyMap.put(reid, new ArrayList<String>());
                    }
                    List<String> apiIds = propertyMap.get(reid);
                    apiIds.add(pro.getApiId());
                    propertyMap.put(reid, apiIds);
                });
            });
        });

        list.getList().stream().forEach(m -> {
            Map dataMap = (Map) m;
            StringBuffer apiName = new StringBuffer();
            if (propertyMap.containsKey(dataMap.get("resourceId").toString())) {
                propertyMap.get(dataMap.get("resourceId").toString()).stream().forEach(apiId -> {
                    ApiEntity apiEntity = apiDao.get(Integer.valueOf(apiId));
                    apiName.append(apiEntity.getName()).append(",");
                });
                apiName.deleteCharAt(apiName.length() - 1);
            }
            if (!dataMap.containsKey("salePrice")) {
                dataMap.put("salePrice", 0);
            } else {
                dataMap.put("salePrice", Double.valueOf(dataMap.get("salePrice").toString()) / 10000);
            }
            if (!dataMap.containsKey("resname"))
                dataMap.put("resname", "");

            dataMap.put("apiName", apiName);
            dataList.add(dataMap);
        });
        map.put("total", list.getTotal());
        map.put("data", dataList);
        return map;
    }


    public Map<String, Object> getResourceById(int resourceId) throws Exception {
        MarketResourceEntity marketResource = marketResourceDao.getMarketResource(resourceId);
        if (marketResource == null) {
            log.info("资源" + resourceId + "不存在");
            throw new Exception("资源" + resourceId + "不存在");
        }
        Map<String, Object> map = new HashMap<>();
        map.put("resourceId", marketResource.getResourceId());
        map.put("name", marketResource.getResname());
        map.put("supplierId", marketResource.getSupplierId());
        map.put("salePrice", marketResource.getSalePrice());
        map.put("type", marketResource.getTypeCode());
        map.put("createTime", marketResource.getCreateTime());
        map.put("apiName", "");

        return map;
    }

}