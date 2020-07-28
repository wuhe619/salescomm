package com.bdaim.customer.account.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONException;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.dao.BatchDao;
import com.bdaim.batch.dto.FixInfo;
import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.batch.service.BatchListService;
import com.bdaim.batch.service.BatchService;
import com.bdaim.callcenter.service.impl.CallCenterService;
import com.bdaim.common.dto.Page;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.service.PhoneService;
import com.bdaim.customer.account.dto.Fixentity;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customer.user.service.CustomerUserService;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.supplier.dto.SupplierEnum;
import com.bdaim.supplier.service.SupplierService;
import com.bdaim.template.dao.MarketTemplateDao;
import com.bdaim.template.entity.MarketTemplate;
import com.bdaim.util.*;
import com.bdaim.util.http.HttpUtil;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;

import static com.bdaim.util.JwtUtil.generToken;
import static com.bdaim.util.JwtUtil.verifyToken;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;

/**
 * @author duanliying
 * @date 2019/3/25
 * @description
 */
@Service("openService")
@Transactional
public class OpenService {
    public static final Logger log = LoggerFactory.getLogger(SupplierService.class);
    @Resource
    private CustomerDao customerDao;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomerService customerService;
    @Resource
    private SourceDao sourceDao;
    @Resource
    private MarketResourceService marketResourceServiceImpl;
    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private BatchService batchService;
    @Resource
    private BatchListService batchListService;
    @Resource
    private BatchDao batchDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Resource
    private MarketTemplateDao marketTemplateDao;
    @Autowired
    private PhoneService phoneService;

    ExecutorService executorService = Executors.newFixedThreadPool(10);

    /**
     * 查询企业余额接口
     *
     * @param custId
     * @return
     */
    public Map<String, Object> queryCustBalance(String custId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        CustomerProperty remainAmoutProperty = customerDao.getProperty(custId, "remain_amount");
        DecimalFormat df = new DecimalFormat("######0.00");
        if (remainAmoutProperty != null) {
            Double remainAmout = Double.parseDouble(remainAmoutProperty.getPropertyValue());
            resultMap.put("remainAmout", df.format(remainAmout / 100));
        }

        return resultMap;
    }

    /**
     * 刷新token接口
     *
     * @param
     * @return
     */
    public String refreshToken(String oldtoken, String username) {
        log.info("旧的token是 ： " + oldtoken + "用户名字是 ： " + username);
        JSONObject json = new JSONObject();
        Map<String, Object> resultMap = new HashMap<>();
        String token = null;
        try {
            if (StringUtil.isNotEmpty(oldtoken) && StringUtil.isNotEmpty(username)) {
                CustomerUser u = customerService.getUserByName(username);
                if (u != null) {
                    String custId = u.getCust_id();
                    String password = u.getPassword();
                    if (StringUtil.isNotEmpty(custId)) {
                        //根据企业id查询当前企业是否有效
                        Customer custMessage = customerDao.getCustMessage(custId);
                        if (custMessage == null) {
                            resultMap.put("status", "005");
                            json.put("data", resultMap);
                            return json.toJSONString();
                        }
                        CustomerProperty customerProperty = customerDao.getProperty(custId, "token");
                        if (customerProperty != null) {
                            token = customerProperty.getPropertyValue();
                            if (token.equals(oldtoken)) {
                                username = "customer." + username;
                                token = generToken(custId, username, password);
                                log.info("刷新token,新token：" + token + "\ttoken长度：" + token.length());
                                customerDao.dealCustomerInfo(custId, "token", token);
                            } else {
                                //resultMap.put("msg", "旧的token不存在");
                                resultMap.put("status", "004");
                                json.put("data", resultMap);
                                return json.toJSONString();
                            }
                        } else {
                            // resultMap.put("msg", "请先获取token,再刷新token");
                            resultMap.put("status", "002");
                            json.put("data", resultMap);
                            return json.toJSONString();
                        }
                    }
                }
            } else {
                //resultMap.put("msg", "缺少必要参数");
                resultMap.put("status", "001");
                json.put("data", resultMap);
                return json.toJSONString();
            }
            resultMap.put("token", token);
            resultMap.put("status", "000");
            json.put("data", resultMap);
            return json.toJSONString();
        } catch (Exception e) {
            log.error(e.getMessage());
            //resultMap.put("msg", "刷新token失败");
            resultMap.put("status", "003");
            json.put("data", resultMap);
            return json.toJSONString();
        }
    }

    /**
     * 根据坐席账号查询坐席信息
     *
     * @param seatAccount
     */
    public Map<String, Object> getSeatMessage(String seatAccount, String custId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        log.info("当前登录的企业id是 ： " + custId + "坐席账号是：" + seatAccount);
        //查询当前企业账号是否存在
        Customer custMessage = customerDao.getCustMessage(custId);
        if (custMessage != null) {
            //根据坐席账号和企业id查询坐席信息
            StringBuffer sql = new StringBuffer("SELECT u.id id, p.property_value propertyValue, p.property_name propertyName\n");
            sql.append("FROM t_customer_user u LEFT JOIN t_customer_user_property p ON u.id = p.user_id\n");
            sql.append("WHERE p.property_name =  'cuc_seat' AND  u.cust_id =? ");
            sql.append(" AND u.account=?");
            List<Map<String, Object>> list = customerDao.sqlQuery(sql.toString(), custId, seatAccount);
            if (list.size() > 0) {
                String propertyValue = String.valueOf(list.get(0).get("propertyValue"));
                if (StringUtil.isNotEmpty(propertyValue)) {
                    JSONObject jsonObject = JSONObject.parseObject(propertyValue);
                    if (jsonObject != null) {
                        map.put("mainNumber", jsonObject.getString("mainNumber"));
                        map.put("seatName", jsonObject.getString("seatName"));
                        map.put("seatId", jsonObject.getString("seatId"));
                        map.put("status", "000");
                        map.put("msg", "坐席信息查询成功");
                    }
                }
            } else {
                map.put("status", "002");
                // map.put("msg", "账号是：" + seatAccount + "的坐席不存在");
            }
        } else {
            map.put("status", "003");
            // map.put("msg", "企业账号异常");
        }
        return map;
    }


    /**
     * 根据账号修改坐席主叫号码
     *
     * @param seatAccount
     */
    public Map<String, Object> updateMainNumber(String seatAccount, String custId, String mainNumber) throws Exception {
        log.info("坐席账号是：" + seatAccount + " 企业id是 ： " + custId + " 主叫号码是：" + mainNumber);
        //根据供应商id和资源类型查询resourceId
        MarketResourceEntity resourceId = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType());
        //根据resourceId查询企业呼叫中心id
        String callCenterId = null;
        if (resourceId != null) {
            JSONObject customerMarketResource = marketResourceServiceImpl.getCustomerMarketResource(custId, String.valueOf(resourceId.getResourceId()));
            if (customerMarketResource != null) {
                callCenterId = customerMarketResource.getString(ResourceEnum.CALL.getCallCenterId());
            }
        }

        Map<String, Object> map = new HashMap<>();
        //查询当前企业账号是否存在
        Customer custMessage = customerDao.getCustMessage(custId);
        Map<String, Object> result = null;
        if (custMessage != null) {
            //根据坐席账号和企业id查询坐席信息
            StringBuffer sql = new StringBuffer("SELECT u.id id, p.property_value propertyValue, p.property_name propertyName\n");
            sql.append("FROM t_customer_user u LEFT JOIN t_customer_user_property p ON u.id = p.user_id\n");
            sql.append("WHERE p.property_name =  'cuc_seat' AND  u.cust_id =? ");
            sql.append(" AND u.account=? ");
            List<Map<String, Object>> list = customerDao.sqlQuery(sql.toString(), custId, seatAccount);
            log.info("query SQL is : " + sql.toString());
            log.info("SQL result is : " + JSON.toJSONString(list.get(0)));
            if (list.size() > 0) {
                String propertyValue = String.valueOf(list.get(0).get("propertyValue"));
                String userId = String.valueOf(list.get(0).get("id"));
                if (StringUtil.isNotEmpty(propertyValue)) {
                    JSONObject jsonObject = JSONObject.parseObject(propertyValue);
                    if (jsonObject != null) {
                        if (jsonObject.getString("mainNumber") != null) {
                            //先删除联通注册上的主叫号码（分机号码）
//                            Map<String, Object> extensionDeleteResult = new CallCenterService().unicomExtensionDelete(callCenterId, jsonObject.getString("mainNumber"));
//                            log.info("坐席主叫号码删除" + ":" + extensionDeleteResult);
                        }
                        //调用联通接口进行增加主叫号码
                        result = new CallCenterService().unicomExtensionRegister(callCenterId, mainNumber, 1);

                        log.info("坐席主叫号增加" + ":" + result);
                        if (result.get("result") != null && result.get("result").equals("0") || result.get("code").equals("211") || result.get("code").equals("213")) {
                            jsonObject.put("mainNumber", mainNumber);
                            String updateSql = "update t_customer_user_property SET property_value= ? where property_name=? AND user_id= ? ";
                            int update = customerDao.executeUpdateSQL(updateSql, new Object[]{JSON.toJSONString(jsonObject), "cuc_seat", userId});
                            if (update > 0) {
                                map.put("status", "000");
                                //   map.put("msg", "主叫号码修改成功");
                            }
                        } else {
                            map.put("status", "004");
                            // map.put("msg", "主叫号码修改失败");
                        }
                    }
                }
            } else {
                map.put("status", "002");
                //map.put("msg", "账号是：" + seatAccount + "的坐席不存在");
            }
        } else {
            map.put("status", "003");
            //  map.put("msg", "企业账号无效");
        }
        return map;
    }

    /**
     * 创建短信模板
     *
     * @return
     */
    public int insertSmsTemplate(String templateName, String templateContent, String smsSignatures, String custId) throws Exception {
        MarketTemplate marketTemplate = new MarketTemplate();
        marketTemplate.setTypeCode(1);
        marketTemplate.setCustId(custId);
        marketTemplate.setTitle(templateName);
        marketTemplate.setMouldContent(templateContent);
        marketTemplate.setSmsSignatures(smsSignatures);
        marketTemplate.setCreateTime(new Timestamp(System.currentTimeMillis()));
        marketTemplate.setStatus(1);
        int templateId = (int) marketTemplateDao.saveReturnPk(marketTemplate);
        return templateId;
    }

    /**
     * 根据模板id查询模板信息
     *
     * @param templateId
     * @param custId
     */
    public List<Map<String, Object>> querySmsTemplate(String templateId, String custId) {
        Map<String, Object> map = new HashMap<>();
        StringBuffer sql = new StringBuffer("SELECT t.title templateName, t.id templateId, t.create_time createTime, t.sms_signatures smsSignatures, CASE t.STATUS \n");
        sql.append("WHEN 1 THEN '审核中' WHEN 2 THEN '审批通过' WHEN 3 THEN '审批未通过' WHEN 4 THEN '审批通过后无效' END templateStatus,t.mould_content content\n");
        sql.append("FROM t_template t  WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(custId)) {
            p.add(custId);
            sql.append(" and t.cust_id =?");
        }
        if (StringUtil.isNotEmpty(templateId)) {
            p.add(templateId);
            sql.append(" and t.id  =?");
        }
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(sql.toString(), p.toArray());
        return list;
    }

    /**
     * 获取token
     *
     * @param username
     * @param password
     */
    public Map<String, Object> getTokenInfo(String username, String password) {
        log.info("账号是：" + username + "密码是：" + password);
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject json = new JSONObject();
        String newpassword = CipherUtil.generatePassword(password);
        String token = null;
        if (StringUtil.isNotEmpty(username) && StringUtil.isNotEmpty(password)) {
            CustomerUser u = customerService.getUserByName(username);
            if (u != null) {
                String uPassword = u.getPassword();
                if (uPassword.equals(newpassword)) {
                    String custId = u.getCust_id();
                    if (StringUtil.isNotEmpty(custId)) {
                        CustomerProperty customerProperty = customerDao.getProperty(custId, "token");
                        if (customerProperty != null) {
                            token = customerProperty.getPropertyValue();
                            try {
                                Claims claims = verifyToken(token);
                            } catch (ExpiredJwtException e) {
                                resultMap.put("status", "003");
                                resultMap.put("msg", "token失效");
                                return resultMap;
                            }
                        } else {
                            username = "customer." + username;
                            token = generToken(custId, username, password);
                            customerDao.dealCustomerInfo(custId, "token", token);
                            log.info("获取token,第一次生成，token：" + token);
                        }
                    }
                } else {
                    resultMap.put("status", "002");
                    resultMap.put("msg", "用户名密码不一致");
                    return resultMap;
                }
            } else {
                log.info("用户" + username + "不存在");
                resultMap.put("status", "004");
                resultMap.put("msg", "用户不存在");
                return resultMap;
            }
        } else {
            resultMap.put("status", "001");
            resultMap.put("msg", "用户名或密码不能为空");
            return resultMap;
        }
        resultMap.put("token", token);
        resultMap.put("status", "000");
        return resultMap;
    }

    /**
     * 获取用户上传修复数据
     *
     * @author:duanliying
     * @date: 2019/3/27 16:27
     */
    public Map<String, Object> insertFixData(FixInfo fixInfo, String compId, Long id, String realname) {
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject json = new JSONObject();
        String batchname = "", repairMode = "";
        int uploadNum = 0;
        Double useAmount = null, idCardPrice = null, remainAmount = null;
        Integer resourceId = null;
        List<BatchDetail> batchDetailList = new ArrayList<>();
        Boolean repeatIdCardStatus = false;
        Boolean repeateEntrpriseIdStatus = false;
        try {
            //根据企业id和供应商id查询企业是否设置销售定价
            MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.IDCARD.getType());

            if (marketResourceEntity != null) {
                resourceId = marketResourceEntity.getResourceId();
                log.info("查询出资源id是：" + resourceId);
                if (resourceId != null) {
                    JSONObject customerMarketResource = marketResourceServiceImpl.getCustomerMarketResource(compId, String.valueOf(resourceId));
                    if (customerMarketResource != null) {
                        idCardPrice = customerMarketResource.getDoubleValue(ResourceEnum.IDCARD.getPrice());
                        log.info("身份证修复单价是：" + idCardPrice + "企业id是：" + compId);
                        if (idCardPrice == null || idCardPrice <= 0) {
                            resultMap.put("status", "006");
                            resultMap.put("msg", "未设置销售定价，请联系管理员！");
                            return resultMap;
                        }
                    }
                }
            }

            String batchId = String.valueOf(System.currentTimeMillis());
            if (fixInfo != null) {
                batchname = fixInfo.getBatchname();
                repairMode = fixInfo.getRepairMode();
                List<Fixentity> fixlist = fixInfo.getFixentity();
                //现在数据库中未修复数量
                int uploadOnFixNum = batchService.uploadNumGet(compId);
                //本次需要修复的数量
                uploadNum = fixlist.size();
                if (uploadNum > 1000) {
                    resultMap.put("status", "003");
                    resultMap.put("msg", "上传数据超过1000条记录，上传失败！");
                    return resultMap;
                }
                //查询企业余额是否充足
                remainAmount = customerService.getRemainMoney(compId) / 100;
                //本次修复需要的总费用
                useAmount = (uploadNum + uploadOnFixNum) * idCardPrice;
                if (useAmount != null && (useAmount > remainAmount)) {
                    resultMap.put("status", "002");
                    resultMap.put("msg", "账户余额不足，上传失败！");
                    return resultMap;
                }
                String certifyMd5 = "", kehuId = "", label_one = "", label_two = "", label_three = "";
                for (Fixentity fixentity : fixlist) {
                    if (fixentity != null) {
                        if (fixentity.getCertifyMd5() != null) {
                            certifyMd5 = fixentity.getCertifyMd5();
                        }
                        if (fixentity.getKehuId() != null) {
                            kehuId = fixentity.getKehuId();
                        }
                        if (fixentity.getLabel_one() != null) {
                            label_one = fixentity.getLabel_one();
                        }
                        if (fixentity.getLabel_two() != null) {
                            label_two = fixentity.getLabel_two();
                        }
                        if (fixentity.getLabel_three() != null) {
                            label_three = fixentity.getLabel_three();
                        }
                        if (StringUtil.isNotEmpty(certifyMd5) && StringUtil.isNotEmpty(kehuId)) {
                            BatchDetail batchDetail = new BatchDetail();
                            batchDetail.setIdCard(certifyMd5);
                            batchDetail.setEnterpriseId(kehuId);
                            batchDetail.setLabelOne(label_one);
                            batchDetail.setLabelTwo(label_two);
                            batchDetail.setLabelThree(label_three);
                            batchDetailList.add(batchDetail);
                        }
                    } else {
                        log.error("失联修复失败！");
                        resultMap.put("status", "006");
                        resultMap.put("msg", "输入参数为空");
                        return resultMap;
                    }
                }
                batchListService.saveBatchDetailList(batchDetailList, SupplierEnum.CUC.getSupplierId(), String.valueOf(resourceId), 0, batchId, id, realname);
                repeatIdCardStatus = batchService.repeatIdCardStatus(batchId);
                repeateEntrpriseIdStatus = batchService.repeateEntrpriseIdStatus(batchId);
                if (repeatIdCardStatus) {
                    resultMap.put("status", "004");
                    resultMap.put("msg", "录入身份证加密数据不能重复，上传失败！");
                    return resultMap;
                }
                if (repeateEntrpriseIdStatus) {
                    resultMap.put("status", "005");
                    resultMap.put("msg", "录入企业自带id数据不能重复，上传失败！");
                    return resultMap;
                }
                batchListService.saveBatch(batchname, uploadNum, repairMode, compId, batchId, 0, SupplierEnum.CUC.getSupplierId(),fixInfo.getProvince(),fixInfo.getCity(),fixInfo.getExtNumber());
                resultMap.put("status", "000");
                resultMap.put("_message", "失联修复文件上传成功！");
                return resultMap;
            }
        } catch (Exception e) {
            log.error("失联修复文件上传失败！\t" + e);
            resultMap.put("status", "001");
            resultMap.put("msg", "服务端解析异常，失联修复文件上传失败！");
            return resultMap;
        }
        return resultMap;
    }

    /**
     * @description 查询单条通话记录（对外接口）
     * @author:duanliying
     * @date: 2018/11/21 9:06
     */
    public Map<String, Object> querySingleVoicelog(String touchId, String custId) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT t.touch_id touchId,t.remark,t.create_time createTime,t.`status` callStatus,t.superid superId,t.channel,t.batch_id batchId,t.enterprise_id enterpriseId,u.account account,backInfo.Callerduration Callerduration,backInfo.Callercaller mainNumber  ");
        stringBuffer.append(" FROM t_touch_voice_log t LEFT JOIN t_customer_user u ON t.user_id = u.id ");
        stringBuffer.append(" LEFT JOIN t_callback_info backInfo ON t.callSid = backInfo.callSid ");
        stringBuffer.append(" WHERE t.touch_id = ? AND t.cust_id = ?");
        //根据touchId和custId查询一条记录
        List<Map<String, Object>> voicelogList = marketResourceDao.sqlQuery(stringBuffer.toString(), touchId, custId);
        if (voicelogList.size() > 0) {
            return voicelogList.get(0);
        }
        return null;
    }

    /**
     * 坐席外呼接口
     *
     * @author:duanliying
     * @date: 2019/3/28 9:33
     */
    public Map<String, Object> seatCallCenter(String id, String batchId, String mainNumber, String apparentNumber, String channel, String seatAccount, String custId) {
        Map<String, Object> map = new HashMap<>();
        String code = "000", message = "成功", userId = null;
        JSONObject json = new JSONObject();
        boolean success = true;
        MarketResourceLogDTO marketResourceLogDTO = null;
        String touchId = null;
        try {
            String resourceId = marketResourceServiceImpl.queryResourceId(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType());
            marketResourceLogDTO = new MarketResourceLogDTO();
            touchId = Long.toString(IDHelper.getTransactionId());
            marketResourceLogDTO.setTouch_id(touchId);
            marketResourceLogDTO.setType_code("1");
            marketResourceLogDTO.setResname("voice");
            marketResourceLogDTO.setCust_id(custId);
            if (StringUtil.isNotEmpty(resourceId)) {
                marketResourceLogDTO.setResourceId(Integer.parseInt(resourceId));
            }
            marketResourceLogDTO.setCallOwner(2);
            marketResourceLogDTO.setSuperId(id);

            //根据登陆账号获取userId判断当前账户是否存在
            CustomerUser account = customerUserDao.getCustomer(seatAccount, custId);
            if (account != null && account.getId() != null) {
                userId = String.valueOf(account.getId());
                marketResourceLogDTO.setUser_id(Long.parseLong(userId));
            } else {
                message = seatAccount + "坐席账号不存在";
                code = "002";
                log.info(seatAccount + "坐席账号不存在");
                marketResourceLogDTO.setStatus(1002);
                marketResourceLogDTO.setRemark(message);
                return null;
            }
            //核验参数是否存在异常
            if (StringUtil.isEmpty(seatAccount) || StringUtil.isEmpty(id) ||
                    StringUtil.isEmpty(batchId) || StringUtil.isEmpty(mainNumber) ||
                    StringUtil.isEmpty(apparentNumber) || StringUtil.isEmpty(channel)) {
                message = "请求参数异常";
                code = "001";
                marketResourceLogDTO.setChannel(0);
                marketResourceLogDTO.setStatus(1002);
                marketResourceLogDTO.setRemark(message);
                return null;
            } else {
                marketResourceLogDTO.setChannel(Integer.parseInt(channel));
                // 判断是余额是否充足
                boolean judge = marketResourceServiceImpl.judRemainAmount(custId);
                if (!judge) {
                    message = "余额不足请充值";
                    code = "006";
                    log.info("余额不足请充值");
                    marketResourceLogDTO.setStatus(1002);
                    marketResourceLogDTO.setRemark(message);
                    return null;
                }
                //判断坐席配置信息是否正确
                String resultCode = marketResourceServiceImpl.checkPropertyValue(resourceId, batchId, userId, mainNumber, seatAccount, apparentNumber, custId, channel);
                if (!"000".equals(resultCode)) {
                    if ("003".equals(resultCode)) {
                        message = "坐席配置信息不存在";
                        code = "003";
                        log.info("坐席配置信息不存在");
                        marketResourceLogDTO.setStatus(1002);
                        marketResourceLogDTO.setRemark(message);
                        return null;
                    }
                    if ("004".equals(resultCode)) {
                        message = mainNumber + "分机号码不存在";
                        code = "004";
                        log.info(mainNumber + "分机号码不存在");
                        marketResourceLogDTO.setStatus(1002);
                        marketResourceLogDTO.setRemark(message);
                        return null;
                    }
                    if ("005".equals(resultCode)) {
                        message = apparentNumber + "外显号码不存在";
                        code = "005";
                        log.info(apparentNumber + "外显号码不存在");
                        marketResourceLogDTO.setStatus(1002);
                        marketResourceLogDTO.setRemark(message);
                        return null;
                    }
                    if ("009".equals(resultCode)) {
                        message = batchId + "此批次ID不存在";
                        code = "009";
                        log.info(message);
                        marketResourceLogDTO.setStatus(1002);
                        marketResourceLogDTO.setRemark(message);
                        return null;
                    }
                }
            }
            //如果参数验证没有问题调用联通进行外呼
            // 唯一请求ID
            String callId = null, activityId = null, enterpriseId = null;
            Map<String, Object> callResult = marketResourceServiceImpl.seatCallCenter(apparentNumber, custId, userId, id, batchId);
            if (callResult != null) {
                log.info("调用外呼返回数据:" + callResult.toString());
                callId = String.valueOf(callResult.get("uuid"));
                activityId = String.valueOf(callResult.get("activity_id"));
                enterpriseId = String.valueOf(callResult.get("enterprise_id"));
                // 成功
                if ("000".equals(String.valueOf(callResult.get("code")))) {
                    success = true;
                    code = "000";
                    message = "调用外呼接口成功";
                } else {
                    success = false;
                    code = String.valueOf(callResult.get("code"));
                    log.info("调用联通外呼失败,返回结果:" + callResult);
                    message = String.valueOf(callResult.get("msg"));
                    log.info("调用外呼失败,idCard:" + id + ", batchId:" + batchId);
                }
            }
            marketResourceLogDTO.setCallSid(callId);
            marketResourceLogDTO.setActivityId(activityId);
            marketResourceLogDTO.setBatchId(batchId);
            marketResourceLogDTO.setEnterpriseId(enterpriseId);
            // 成功
            if (success) {
                marketResourceLogDTO.setStatus(1001);
                marketResourceLogDTO.setRemark(message);
            } else {
                // 失败
                marketResourceLogDTO.setStatus(1002);
                marketResourceLogDTO.setRemark(message);
            }

         } catch (Exception e) {
            marketResourceLogDTO.setStatus(1002);
            log.error("外呼接口异常,", e);
            code = "009";
            message = "外呼接口异常";
        } finally {
            if (userId != null) {
                marketResourceServiceImpl.insertLog(marketResourceLogDTO);
            }
            map.put("touchId", touchId);
            map.put("status", code);
            // map.put("msg", message);
            return map;
        }
    }


    /**
     * @description 查询单条短信记录（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/21 9:13
     */
    public Map<String, Object> querySingleSmslog(String touchId, String custId) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT t.touch_id touchId,t.remark,t.create_time createTime,t.`status` sendStatus,t.sms_content smsContent,t.superid superId,t.batch_id batchId,t.channel,u.account ");
        stringBuffer.append(" FROM t_touch_sms_log t LEFT JOIN t_customer_user u ON t.user_id = u.id ");
        stringBuffer.append(" WHERE t.touch_id = ? AND t.cust_id = ?");
        log.info("查询单条短信记录sql" + stringBuffer);
        //根据touchId和custId查询一条记录
        List<Map<String, Object>> voicelogList = marketResourceDao.sqlQuery(stringBuffer.toString(), touchId, custId);
        if (voicelogList.size() > 0) {
            return voicelogList.get(0);
        } else {
            return new HashMap<>();
        }
    }

    /**
     * @description 根据企业ID查询批次列表
     * @author:duanliying
     * @method
     * @date: 2018/11/22 17:54
     */
    public Page queryList(int pageNum, int pageSize, String custId) throws Exception {
        StringBuilder sqlBuilder = new StringBuilder("SELECT id batchId, repair_mode repairMode,batch_name batchName, certify_type certifyType, status, upload_num uploadNum, success_num successNum, upload_time uploadTime, repair_time repairTime, channel, repair_strategy repairStrategy FROM nl_batch WHERE 1=1");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(custId)) {
            p.add(custId);
            sqlBuilder.append(" AND comp_id = ?");
        }
        sqlBuilder.append(" ORDER BY upload_time DESC");
        Page result = batchDao.sqlPageQuery(sqlBuilder.toString(), pageNum, pageSize, p.toArray());
        if (result != null && result.getData() != null && result.getData().size() > 0) {
            Map map;
            // 处理修复状态为 4数据待发送给联通或者5发送联通成功都为2-修复中
            for (int i = 0; i < result.getData().size(); i++) {
                map = (Map) result.getData().get(i);
                if (map != null && map.get("status") != null && (Integer.parseInt(String.valueOf(map.get("status"))) == 4 ||
                        Integer.parseInt(String.valueOf(map.get("status"))) == 5)) {
                    map.put("status", 2);
                }
            }
        }
        return result;
    }

    /**
     * @description 根据批次ID筛选出批次下的客户集合(对外接口)
     * @author:duanliying
     * @method 渠道/供应商 2-联通 4-移动 3-电信
     * @date: 2018/9/6 16:53
     */
    public Page getDetailListById(Integer pageNumber, Integer pageSize, String batchId, String custId) {
        StringBuffer sql = new StringBuffer("SELECT  n.id superId,n.batch_id batchId,n.enterprise_id enterpriseId,n.id_card idCard,n.channel,n.label_one labelOne,n.label_two labelTwo,n.label_three labelThree,n.`status` ,n.upload_time uploadTime,n.fix_time fixTime");
        sql.append(" FROM nl_batch_detail n LEFT JOIN nl_batch nl ON n.batch_id = nl.id\n");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(batchId)) {
            p.add(batchId);
            sql.append("WHERE batch_id =?");
        }
        if (StringUtil.isNotEmpty(custId)) {
            p.add(custId);
            sql.append(" and nl.comp_id =?");
        }
        Page page = batchDao.sqlPageQuery(sql.toString(), pageNumber, pageSize, p.toArray());
        //查询批次下所有自建属性信息
        return page;
    }

    /**
     * @description 查询通话记录（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/22 19:08
     */

    public Page queryRecordVoicelog(int pageNum, int pageSize, String custId) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT t.touch_id touchId,t.remark,t.create_time createTime,t.`status`,t.superid superId,t.channel,t.batch_id batchId,t.enterprise_id enterpriseId,u.account account,backInfo.Callerduration Callerduration,backInfo.Callercaller mainNumber  ");
        stringBuffer.append(" FROM t_touch_voice_log t LEFT JOIN t_customer_user u ON t.user_id = u.id ");
        stringBuffer.append(" LEFT JOIN t_callback_info backInfo ON t.callSid = backInfo.callSid ");
        stringBuffer.append(" WHERE t.cust_id = ? ORDER BY t.create_time DESC");
        log.info("查询单条通话记录sql" + stringBuffer);
        Page page = batchDao.sqlPageQuery(stringBuffer.toString(), pageNum, pageSize, custId);
        //根据touchId和custId查询一条记录
        return page;

    }

    /**
     * @description 查询短信记录（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/22 19:09
     */
    public Page openSmsHistory(int pageNum, int pageSize, String custId) throws Exception {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("SELECT t.touch_id touchId,t.remark,t.create_time createTime,t.`status`,t.sms_content smsContent,t.superid superId,t.batch_id batchId,t.channel,u.account ");
        stringBuffer.append(" FROM t_touch_sms_log t LEFT JOIN t_customer_user u ON t.user_id = u.id ");
        stringBuffer.append(" WHERE t.cust_id = ?");
        log.info("查询单条短信记录sql" + stringBuffer);
        Page page = batchDao.sqlPageQuery(stringBuffer.toString(), pageNum, pageSize, custId);
        return page;
    }

    /**
     * @description 发送短信（对外接口）
     * @author:duanliyin
     * @date: 2018/11/19 10:06
     */
    public Map<String, Object> sendSmsMessage(String batchId, String templateId, String channel, String variables, String customerId, String seatAccount, String custId) {
        Map<String, Object> map = new HashMap<>();
        //获取短信模板内容以$$分割填充模板内容
        String smsTemplateNUm = marketResourceServiceImpl.getSmsTemplateMessage(Integer.parseInt(templateId), 1, custId);
        if (smsTemplateNUm == null) {
            map.put("status", "002");
            return map;
        }
        CustomerUser account = customerUserDao.getCustomer(seatAccount, custId);
        if (account == null) {
            map.put("status", "004");
            return map;
        }
        boolean judge = marketResourceServiceImpl.judRemainAmount(custId);
        if (!judge) {
            map.put("status", "005");
            return map;
        }
        //发送短信
        Map<String, Object> result = marketResourceServiceImpl.sendSmsbyBatch(Integer.valueOf(channel), Integer.parseInt(templateId), seatAccount, custId, variables, Integer.parseInt(templateId), batchId, customerId, 1, 2);
        String status = "0";
        if (result.size() > 0) {
            log.info("发送短信结果:" + result);
            if ("000".equals(String.valueOf(result.get("code")))) {
                status = "000";
            }
            if ("009".equals(String.valueOf(result.get("code")))) {
                status = "001";
            }
            if ("008".equals(String.valueOf(result.get("code")))) {
                status = "002";
            }
            if ("006".equals(String.valueOf(result.get("code")))) {
                status = "006";
            }
        }
        if (result.get("touchId") != null) {
            map.put("touchId", String.valueOf(result.get("touchId")));
        }
        map.put("status", status);
        return map;
    }

    public void saveActionRecord(Map<String, Object> map, HttpServletRequest request) {
        //1. 获取入参（IMEI、app列表、设备地理位置、用户行为、通讯录列表）和通过request获取IP地址
        String iMei = String.valueOf(map.get("IMEI"));
        String deviceAddress = String.valueOf(map.get("device_address"));
        String action = String.valueOf(map.get("action"));
        String platform = String.valueOf(map.get("platform"));
        String IP = StringHelper.getIpAddr(request);
        //2. 转换格式并插入数据库
        List<String> appList = StringUtil.isNotEmpty(String.valueOf(map.get("apps"))) ? (List<String>) map.get("apps") : new ArrayList<String>();
        List<Map<String, Object>> contactsList =
                StringUtil.isNotEmpty(String.valueOf(map.get("device_contacts"))) ? (List<Map<String, Object>>) map.get("device_contacts") : new ArrayList<>();
        List<Object[]> batchArgs = new ArrayList<>();
        for (int i = -1; i < appList.size(); i++) {
            String appElement;
            if (i == -1 && appList.size() != 0) {
                continue;
            } else if (i == -1 && appList.size() == 0) {
                appElement = "";
            } else {
                appElement = appList.get(i);
            }
            for (int j = -1; j < contactsList.size(); j++) {
                Object[] objects;
                if (j == -1 && contactsList.size() != 0) {
                    continue;
                } else if (j == -1 && contactsList.size() == 0) {
                    objects = new Object[]{iMei, appElement, deviceAddress, "", action, IP, platform};
                } else {
                    objects = new Object[]{iMei, appElement, deviceAddress, JSON.toJSONString(contactsList.get(j)), action, IP, platform};
                }
                batchArgs.add(objects);
            }
        }
        String sql = "INSERT INTO t_behavior_record (create_time,imei,app,device_address,device_contact,action,ip,platform) VALUES (NOW(),?,?,?,?,?,?,?)";
        log.info("执行保存SQL " + sql);
        jdbcTemplate.batchUpdate(sql, batchArgs);
    }


    public List<Map<String, Object>> getAddressResoult(String idCard) {
        String querySql = "SELECT site address FROM  tmp_nl_batch_detail b  WHERE batch_id = 1542099439991 and id_card = ? ";
        List<Map<String, Object>> list = batchDao.sqlQuery(querySql, idCard);
        return list;
    }

    public ResponseInfo saveAccessChannels(Map<String, Object> map, HttpServletRequest request) {
        //1. 获取入参 mobile、channel、name、activity_code
        String mobile = map.get("mobile") == null || map.get("mobile") == "" ? "" : String.valueOf(map.get("mobile"));
        String channel = map.get("channel") == null || map.get("channel") == "" ? "" : String.valueOf(map.get("channel"));
        String name = map.get("name") == null || map.get("name") == "" ? "" : String.valueOf(map.get("name"));
        String activityCode = map.get("activity_code") == null || map.get("activity_code") == "" ? "" : String.valueOf(map.get("activity_code"));
        String channelName = map.get("channel_name") == null || map.get("channel_name") == "" ? "" : String.valueOf(map.get("channel_name"));
        String deviceType = map.get("device_type") == null || map.get("device_type") == "" ? "" : String.valueOf(map.get("device_type"));
        log.info("入参的值为" + JSON.toJSONString(map));
        //2. 如果活动编码activity_code是ETC，且此次channel下有同样的mobile手机号，则返回新增失败
        if ("ETC".equalsIgnoreCase(activityCode)) {
            String countSql = "SELECT COUNT(*) AS count FROM t_access_channel WHERE activity_code='ETC' AND mobile=? AND channel=? ";
            Map<String, Object> countMap = jdbcTemplate.queryForMap(countSql, mobile, channel);
            int count = Integer.parseInt(String.valueOf(countMap.get("count")));
            if (count > 0) {
                return new ResponseInfoAssemble().failure(HttpStatus.BAD_REQUEST.value(), "手机号已存在");
            }
        }
        //3. 插入数据库
        String IP = StringHelper.getIpAddr(request);
        StringBuffer sql = new StringBuffer("INSERT INTO t_access_channel (mobile,channel,channel_name,name,ip,create_time,activity_code,device_type) VALUES ('");
        sql.append(mobile).append("','").append(channel).append("','").append(channelName).append("','").append(name).append("','").append(IP).append("',NOW(),'")
                .append(activityCode).append("','").append(deviceType).append("')");
        log.info("执行SQL语句为" + sql.toString());
        jdbcTemplate.update(sql.toString());
        return new ResponseInfoAssemble().success(null);
    }

    public Map<String, Object> saveBillNo(Map<String, Object> map) {
        //运单号，对应t_touch_express_log中的request_id字段
        String billNo = String.valueOf(map.get("billNo"));
//        String expressCompany = String.valueOf(map.get("expressCompany")); 快递公司名称，暂时注掉，用到再从data中获取
        //商家订单号，对应t_touch_express_log和nl_batch_detail中的touch_id字段
        String orderCode = String.valueOf(map.get("orderCode"));
        //快递管家推送的所有数据，对应t_touch_express_log中的data字段
        String data = JSON.toJSONString(map);

        StringBuffer sqlBuffer = new StringBuffer("UPDATE t_touch_express_log SET data='");
        sqlBuffer.append(data).append("',request_id='").append(billNo).append("' WHERE touch_id='")
                .append(orderCode).append("'");
        log.info("===》》》执行更新快递运单号SQL " + sqlBuffer.toString());
        Map<String, Object> resultMap = new HashMap<>(10);
        try {
            jdbcTemplate.update(sqlBuffer.toString());
        } catch (Exception e) {
            resultMap.put("status", false);
            resultMap.put("message", "失败，message如下 " + e.getMessage());
            return resultMap;
        }
        resultMap.put("status", true);
        resultMap.put("message", "成功");
        return resultMap;
    }

    public void getVoiceRecordFile(String userId, String fileName, HttpServletRequest request, HttpServletResponse response) {
        // 设置响应头
        response.addHeader("Accept-Ranges", "bytes");
        response.addHeader("Content-Type", "audio/mpeg;charset=UTF-8");
        String range = request.getHeader("Range");
        FileInputStream fis = null;
        ByteArrayInputStream hBaseInputStream = null;
        try {
            // 查询本地磁盘的录音文件
            String filePath = "/home/soft/audio/" + userId + File.separator + fileName;
            File file = new File(filePath);
            if (file.exists()) {
                fis = new FileInputStream(file);
                if (StringUtil.isNotEmpty(range)) {
                    long length = file.length();
                    log.info("文件大小: " + length);
                    String[] rs = range.split("\\=");
                    range = rs[1].split("\\-")[0];
                    length -= Integer.parseInt(range);
                    response.addHeader("Content-Length", length + "");
                    response.addHeader("Content-Range", "bytes " + range + "-" + length + "/" + length);
                }
                IOUtils.copy(fis, response.getOutputStream());
            } else {
                log.warn(filePath + ",音频文件不存在,穿透查询一次录音文件");
                long length;
                // 通过HBase接口读取录音文件
                Map<String, Object> param = new HashMap<>();
                Map<String, Object> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                String result = HttpUtil.httpGet("http://ds4:1111/voice/" + fileName + "/f1:file", param, headers);
                if (StringUtil.isNotEmpty(result)) {
                    log.info("开始解析HBase返回的录音文件,userId:" + userId + ",fileName:" + fileName);
                    String base64Str = null;
                    JSONObject jsonObject;
                    try {
                        jsonObject = JSON.parseObject(result);
                    } catch (JSONException e) {
                        log.error("解析HBase录音文件返回Json出错,userId:" + userId + ",fileName:" + fileName + ",返回结果:" + result, e);
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
                        log.warn("通过HBase读取录音文件base64字符串为空,userId:" + userId + ",fileName:" + fileName + ",base64Str:" + base64Str);
                    }
                } else {
                    log.warn("通过HBase读取录音文件返回为空,userId:" + userId + ",fileName:" + fileName + ",result:" + result);
                }
            }
        } catch (Exception e) {
            log.error("获取录音文件失败,", e);
        } finally {
            try {
                if (fis != null) {
                    fis.close();
                }
                if (hBaseInputStream != null) {
                    hBaseInputStream.close();
                }
            } catch (IOException e) {
                log.error("获取录音文件失败,", e);
            }
        }
    }

    @Resource
    private CustomerUserService customerUserService;

    private final static String token_suffix = "TOKEN_";

    /**
     * 刷新token接口
     *
     * @param
     * @return
     */
    public Map<String, Object> refreshToken0(String oldtoken, String username) {
        log.info("旧的token是 ： " + oldtoken + "用户名字是 ： " + username);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", "");
        String token = null;
        try {
            if (StringUtil.isNotEmpty(oldtoken) && StringUtil.isNotEmpty(username)) {
                CustomerUser u = customerUserService.getUserByName(username);
                if (u != null) {
                    String custId = u.getCust_id();
                    String password = u.getPassword();
                    if (StringUtil.isNotEmpty(custId)) {
                        CustomerProperty customerProperty = customerDao.getProperty(custId, token_suffix + custId);
                        if (customerProperty != null) {
                            token = customerProperty.getPropertyValue();
                            if (token.equals(oldtoken)) {
                                username = "customer." + username;
                                token = generToken(custId, username, password);
                                log.info("刷新token,新token：" + token + "\ttoken长度：" + token.length());
                                customerDao.dealCustomerInfo(custId, token_suffix + custId, token);
                            } else {
                                resultMap.put("_message", "token不正确");
                                resultMap.put("code", "04");
                                return resultMap;
                            }
                        } else {
                            resultMap.put("_message", "请先获取token");
                            resultMap.put("code", "02");
                            return resultMap;
                        }
                    } else {
                        log.error("custId is null");
                    }
                } else {
                    log.error("user " + username + " is not exists or status is wrong");
                }
            } else {
                resultMap.put("_message", "参数错误");
                resultMap.put("code", "02");
                return resultMap;
            }
            resultMap.put("data", token);
            resultMap.put("code", "00");
            return resultMap;
        } catch (Exception e) {
            log.error(e.getMessage());
            resultMap.put("_message", "刷新token失败");
            resultMap.put("code", "05");
//            json.put("data", resultMap);
            return resultMap;
        }
    }



    /* *//**
     * 获取token
     *
     * @param
     * @param
     *//*
    public Map<String, Object> getTokenInfo(String username, String password) {
        log.info("账号是：" + username + "密码是：" + password);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data","");
        if(StringUtil.isEmpty(username) || StringUtil.isEmpty(password)){
            resultMap.put("code", "02");
            resultMap.put("_message", "用户名或密码不能为空");
            return resultMap;
        }
        CustomerUser u = customerUserService.getUserByName(username);
        if(u==null){
            log.error("用户" + username + "不存在");
            resultMap.put("code", "01");
            resultMap.put("_message", "用户不存在");
            return resultMap;
        }
        String token = null;
        String newpassword = CipherUtil.generatePassword(password);
        String uPassword = u.getPassword();
        if(!uPassword.equals(newpassword)){
            resultMap.put("code", "02");
            resultMap.put("_message", "用户名密码不一致");
            return resultMap;
        }
        String custId = u.getCust_id();
        if (StringUtil.isNotEmpty(custId)) {
            CustomerProperty customerProperty = customerDao.getProperty(custId, token_suffix+custId);
            if (customerProperty != null) {
                token = customerProperty.getPropertyValue();
                try {
                    Claims claims = verifyToken(token);
                } catch (ExpiredJwtException e) {
                    resultMap.put("code", "03");
                    resultMap.put("_message", "token失效");
                    return resultMap;
                }
            } else {
                username = "customer." + username;
                token = generToken(custId, username, password);
                customerDao.dealCustomerInfo(custId, token_suffix+custId, token);
                log.info("获取token,第一次生成，token：" + token);
            }
        }
        resultMap.put("data", token);
        resultMap.put("code", "00");
        return resultMap;
    }*/

    public void saveSmsuploadinfo(String body) throws Exception {
        JSONArray array = JSON.parseArray(body);
        log.info("saveSmsuploadinfo.size:{}",array.size());
        executorService.execute(new SmsUploadinfo(array));
    }

    class SmsUploadinfo implements Runnable{
        private JSONArray array;

        public SmsUploadinfo(JSONArray array ) {
            this.array = array;
        }

        public  void  saveSmsuploadinfo() throws Exception {
            List list = new ArrayList();
            String sql = "insert into t_touch_sms_upinfo(cust_group_id,cust_task_id,mobile,super_id,content,call_time,msg_id)values(?,?,?,?,?,now(),?)";
            for (int i = 0; i < array.size(); i++) {
                JSONObject json = array.getJSONObject(i);
                String mobile = null, content = null, userData = null, msgId = null;
                if (json.containsKey("id")) {
                    msgId = json.getString("id");
                }
                if (json.containsKey("mobile")) {
                    mobile = json.getString("mobile");
                }
                if (json.containsKey("content")) {
                    content = json.getString("content");
                }
                if (json.containsKey("userData")) {
                    userData = json.getString("userData");
                }
                try {
                    String cust_group_id = "";
                    String cust_task_id = "";
                    if (userData != null) {
                        if (userData.contains("-")) {
                            String[] s = userData.split("-");
                            cust_group_id = s[0];
                            cust_task_id = s[1];
                        } else {
                            cust_group_id = userData;
                        }
                    }
                    String super_id = phoneService.savePhoneToAPI(mobile);
                    String [] arr = {cust_group_id,cust_task_id,mobile,super_id,content,msgId};
                    list.add(arr);
                } catch (Exception e) {
                    throw new Exception();
                }
            }
            jdbcTemplate.batchUpdate(sql,list);
        }

        @Override
        public void run() {
            try {
                saveSmsuploadinfo();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public  void getCall(JSONObject jsonObject){

        String bindId = jsonObject.getString("bindId");//中间号绑定id
        if(StringUtil.isEmpty(bindId)){
            log.info("绑定id为空");
        }


        String secretId = jsonObject.getString("secretId");//企业id
        if(StringUtil.isEmpty(bindId)){
            log.info("企业secretId为空");
        }

        String activeNumber = jsonObject.getString("activeNumber");//主叫号
        if(StringUtil.isEmpty(bindId)){
            log.info("通话记录主叫号为空");
        }

        String callerstarttime = jsonObject.getString("callerstarttime");//通话开始时间
        if(StringUtil.isEmpty(callerstarttime)){
            log.info("通话开始时间为空");
        }

        String callerendtime = jsonObject.getString("callerendtime");//通话结束时间
        if(StringUtil.isEmpty(callerendtime)){
            log.info("通话开始时间为空");
        }

        String callSid = jsonObject.getString("callSid");//本次通话唯一标识
        if(StringUtil.isEmpty(callSid)){
            log.info("本次通话唯一标识为空");
        }

        String status = jsonObject.getString("status");//接听标识
        if(StringUtil.isEmpty(status)){
            log.info("接听标识为空");
        }

        String getBatch="select nb.batch_id,nb.comp_id,nbd.resource_id from  nl_batch_detail nbd left join nl_batch nb on nb.id=nbd.batch_id  where nb.label_seven=?";

        Map<String, Object> stringObjectMap = customerDao.queryUniqueSql(getBatch, new String[]{bindId});

        String getResource="select resname,supplier_id from t_market_resource tmr,t_market_resource_property  tmrp where  tmr.resource_id=? and tmr.resource_id=tmrp.resource_id and  tmrp.property_name='' ";

        Map<String, Object> resource_id = customerDao.queryUniqueSql(getResource, new String[]{stringObjectMap.get("resource_id").toString()});

        String insertVoiceLog="insert into t_touch_voice_log (touch_id,cust_id,type_code,resname,remark,create_time,status,callSid,batch_id,activity_id,amount,prod_amount,resource_id,active_number) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)";

        customerDao.executeUpdateSQL(insertVoiceLog,IDHelper.getID(),stringObjectMap.get("comp_id").toString(),"1",resource_id.get("resname"),"呼叫线路扣费",new Timestamp(new Date().getTime()),status,callSid,stringObjectMap.get("batch_id").toString(),secretId,0,0,stringObjectMap.get("resource_id").toString(),activeNumber);

        if(status.equals("1")){//如果是接通状态 执行扣费逻辑

            String custPrice="select property_value from t_customer tc,t_customer_property tcp where tc.cust_id=tcp.cust_id where tcp.cust_id=? and tcp.property_name=\"30_conifg\"";
            Map<String, Object> comp_id = customerDao.queryUniqueSql(custPrice, stringObjectMap.get("comp_id").toString());
            DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String price = comp_id.get("property_value").toString();
            JSONObject jsonObject1 = JSONObject.parseObject(price);
            LocalDateTime parse = LocalDateTime.parse(callerstarttime, df);
            LocalDateTime end =   LocalDateTime.parse(callerendtime,df);
            Duration between = Duration.between(parse, end);
            long time = between.toMinutes();//通话时长（分钟）
            int rantPrice = NumberConvertUtil.transformtionCent(Double.parseDouble(jsonObject1.getString("callPrice")));//月租单价（分）

            long callPric=time*rantPrice;

            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("YYYYMMDD");
            String format = LocalDate.now().format(dateTimeFormatter);

            //插入交易明细
            StringBuilder sql=new StringBuilder();
            sql.append("create table IF NOT EXISTS t_transaction_");
            sql.append(format);
            sql.append(" like t_transaction");
            customerDao.executeUpdateSQL(sql.toString());
            sql.setLength(0);
            sql.append("insert into t_transaction_");
            sql.append(format);

            sql.append(" (transaction_id, cust_id, type, pay_mode, meta_data, amount, remark, create_time, supplier_id, user_id, certificate, batch_id, prod_amount, resource_id)values(?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
            String    transactionId = Long.toString(IDHelper.getTransactionId());

            customerDao.executeUpdateSQL(sql.toString(), transactionId, stringObjectMap.get("comp_id").toString(), 4,
                    1, "", Math.abs(callPric), "通话", new Timestamp(System.currentTimeMillis()), 4, stringObjectMap.get("comp_id").toString(), "", "", 0, 30);

           String custId=stringObjectMap.get("comp_id").toString();
           String subId=resource_id.get("supplier_id").toString();
           try {
               accountDeductions(custId,new BigDecimal(callPric));
               accountSupplierDeductions(subId,new BigDecimal(callPric));
           }catch (Exception e){
               e.printStackTrace();
           }


        }
    }



    public boolean accountDeductions(String custId, BigDecimal amount) throws Exception {
        String sql = "SELECT * FROM t_customer_property m where m.cust_id=? and m.property_name=?";
        List<Map<String, Object>> list = customerDao.queryMapsListBySql(sql, custId, "remain_amount");
        String remainAmount = null;
        if (list.size() > 0) {
            remainAmount = String.valueOf(list.get(0).get("property_value"));
        }
        // 处理账户不存在
        if (remainAmount == null) {
            remainAmount = "0";
            log.info(custId + " 账户不存在开始新建账户信息");
            String insertSql = "INSERT INTO `t_customer_property` (`cust_id`, `property_name`, `property_value`, `create_time`) VALUES (?, ?, ?, ?);";
            int status = customerDao.executeUpdateSQL(insertSql, custId, "remain_amount", remainAmount, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info(custId + " 账户创建结果:" + status);
        }
        // 处理累计消费金额不存在
        List<Map<String, Object>> usedAmountList = customerDao.queryMapsListBySql(sql, custId, "used_amount");
        String usedAmount = null;
        if (usedAmountList.size() > 0) {
            usedAmount = String.valueOf(usedAmountList.get(0).get("property_value"));
        }

        if (usedAmount == null) {
            // 累计消费 处理账户不存在
            usedAmount = "0";
            log.info(custId + " 账户累计消费不存在开始新建账户信息");
            String insertSql = "INSERT INTO `t_customer_property` (`cust_id`, `property_name`, `property_value`, `create_time`) VALUES (?, ?, ?, ?);";
            int status = customerDao.executeUpdateSQL(insertSql, custId, "used_amount", usedAmount, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            log.info(custId + " 账户累计消费创建结果:" + status);

        }
        if (StringUtil.isNotEmpty(remainAmount)) {
            if (Double.parseDouble(remainAmount) <= 0) {
                log.warn(custId + " 账户余额已经透支:" + remainAmount);
            }
            log.info(custId + " 账户余额:" + remainAmount + ",先执行扣减");
            DecimalFormat df = new DecimalFormat("#");
            BigDecimal remainAmountBigDecimal = new BigDecimal(remainAmount);
            String nowMoney = df.format(remainAmountBigDecimal.subtract(amount));
            String updateSql = "UPDATE t_customer_property SET property_value = ? WHERE cust_id = ? AND property_name = ?";
            log.info("客户ID:" + custId + "扣减后的余额:" + nowMoney);
            customerDao.executeUpdateSQL(updateSql, nowMoney, custId, "remain_amount");

            // 处理累计消费累加
            BigDecimal usedAmountBigDecimal = new BigDecimal(usedAmount);
            String usedAmountMoney = df.format(usedAmountBigDecimal.add(amount));
            log.info("客户ID:" + custId + "累计消费的金额是:" + usedAmountMoney);
            customerDao.executeUpdateSQL(updateSql, usedAmountMoney, custId, "used_amount");
            return true;

        }
        return false;
    }


    /**
     * 供应商余额、累计金额扣减
     *
     * @param
     * @param amount
     * @return
     */
    public boolean accountSupplierDeductions(String supplierId, BigDecimal amount) throws Exception {
        String sql = "SELECT property_value FROM t_supplier_property m where m.supplier_id=? and m.property_name=?";
        List<Map<String, Object>> list = customerDao.queryMapsListBySql(sql, supplierId, "remain_amount");
        String remainAmount = null;
        if (list.size() > 0) {
            remainAmount = String.valueOf(list.get(0).get("property_value"));
        }
        // 处理账户不存在
        if (remainAmount == null) {
            remainAmount = "0";
            log.info(supplierId + " 供应商账户不存在开始新建账户信息");
            String insertSql = "INSERT INTO t_supplier_property (supplier_id, property_name, property_value,create_time) VALUES (?, ?, ?,NOW());";
            int status = customerDao.executeUpdateSQL(insertSql, supplierId, "remain_amount", remainAmount);
            log.info("供应商id：" + supplierId + "\t供应商账户创建结果:" + status);
        }
        // 处理累计消费金额不存在
        List<Map<String, Object>> usedAmountList = customerDao.queryMapsListBySql(sql, supplierId, "used_amount");
        String usedAmount = null;
        if (usedAmountList.size() > 0) {
            usedAmount = String.valueOf(usedAmountList.get(0).get("property_value"));
        }

        if (usedAmount == null) {
            // 累计消费 处理账户不存在
            usedAmount = "0";
            log.info(supplierId + " 供应商账户累计消费不存在开始新建账户信息");
            String insertSql = "INSERT INTO t_supplier_property (supplier_id, property_name, property_value,create_time) VALUES (?, ?, ?,NOW());";
            int status = customerDao.executeUpdateSQL(insertSql, supplierId, "used_amount", usedAmount);
            log.info("供应商id：" + supplierId + "\t供应商账户创建结果:" + status);
        }
        if (StringUtil.isNotEmpty(remainAmount)) {
            if (Double.parseDouble(remainAmount) <= 0) {
                log.info("供应商id" + supplierId + "\t账户余额:" + remainAmount + ",先执行扣减");
            }
            DecimalFormat df = new DecimalFormat("#");
            BigDecimal remainAmountBigDecimal = new BigDecimal(remainAmount);
            String nowMoney = df.format(remainAmountBigDecimal.subtract(amount));
            String updateSql = "UPDATE t_supplier_property SET property_value = ? WHERE supplier_id = ? AND property_name = ?";
            customerDao.executeUpdateSQL(updateSql, nowMoney, supplierId, "remain_amount");

            // 处理累计消费累加
            BigDecimal usedAmountBigDecimal = new BigDecimal(usedAmount);
            String usedAmountMoney = df.format(usedAmountBigDecimal.add(amount));
            customerDao.executeUpdateSQL(updateSql, usedAmountMoney, supplierId, "used_amount");
            return true;

        }
        return false;
    }
}

