package com.bdaim.account.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.account.dto.Fixentity;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.dao.BatchDao;
import com.bdaim.batch.dto.FixInfo;
import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.batch.service.BatchListService;
import com.bdaim.batch.service.BatchService;
import com.bdaim.callcenter.service.impl.CallCenterServiceImpl;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.StringHelper;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerDO;
import com.bdaim.customer.entity.CustomerPropertyDO;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.rbac.dto.Page;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.service.impl.MarketResourceServiceImpl;
import com.bdaim.supplier.dto.SupplierEnum;
import com.bdaim.supplier.service.SupplierService;
import com.bdaim.template.entity.MarketTemplate;

//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.ExpiredJwtException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;

//import static com.bdaim.common.util.JwtUtil.generToken;
//import static com.bdaim.common.util.JwtUtil.verifyToken;

import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private MarketResourceServiceImpl marketResourceServiceImpl;
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

    /**
     * 查询企业余额接口
     *
     * @param custId
     * @return
     */
    public Map<String, Object> queryCustBalance(String custId) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        CustomerPropertyDO remainAmoutProperty = customerDao.getProperty(custId, "remain_amount");
        DecimalFormat df = new DecimalFormat("######0.00");
        if (remainAmoutProperty != null) {
            Double remainAmout = Double.parseDouble(remainAmoutProperty.getPropertyValue());
            resultMap.put("remainAmout", df.format(remainAmout / 100));
        }

        return resultMap;
    }

//    /**
//     * 刷新token接口
//     *
//     * @param
//     * @return
//     */
//    public String refreshToken(String oldtoken, String username) {
//        log.info("旧的token是 ： " + oldtoken + "用户名字是 ： " + username);
//        JSONObject json = new JSONObject();
//        Map<String, Object> resultMap = new HashMap<>();
//        String token = null;
//        try {
//            if (StringUtil.isNotEmpty(oldtoken) && StringUtil.isNotEmpty(username)) {
//                CustomerUserDO u = customerService.getUserByName(username);
//                if (u != null) {
//                    String custId = u.getCust_id();
//                    String password = u.getPassword();
//                    if (StringUtil.isNotEmpty(custId)) {
//                        //根据企业id查询当前企业是否有效
//                        CustomerDO custMessage = customerDao.getCustMessage(custId);
//                        if (custMessage==null){
//                            resultMap.put("status", "005");
//                            json.put("data", resultMap);
//                            return json.toJSONString();
//                        }
//                        CustomerPropertyDO customerProperty = customerDao.getProperty(custId, "token");
//                        if (customerProperty != null) {
//                            token = customerProperty.getPropertyValue();
//                            if (token.equals(oldtoken)) {
//                                username = "customer." + username;
//                                token = generToken(custId, username, password);
//                                log.info("刷新token,新token：" + token + "\ttoken长度：" + token.length());
//                                customerDao.dealCustomerInfo(custId, "token", token);
//                            } else {
//                                //resultMap.put("msg", "旧的token不存在");
//                                resultMap.put("status", "004");
//                                json.put("data", resultMap);
//                                return json.toJSONString();
//                            }
//                        } else {
//                            // resultMap.put("msg", "请先获取token,再刷新token");
//                            resultMap.put("status", "002");
//                            json.put("data", resultMap);
//                            return json.toJSONString();
//                        }
//                    }
//                }
//            } else {
//                //resultMap.put("msg", "缺少必要参数");
//                resultMap.put("status", "001");
//                json.put("data", resultMap);
//                return json.toJSONString();
//            }
//            resultMap.put("token", token);
//            resultMap.put("status", "000");
//            json.put("data", resultMap);
//            return json.toJSONString();
//        } catch (Exception e) {
//            log.error(e.getMessage());
//            //resultMap.put("msg", "刷新token失败");
//            resultMap.put("status", "003");
//            json.put("data", resultMap);
//            return json.toJSONString();
//        }
//    }

    /**
     * 根据坐席账号查询坐席信息
     *
     * @param seatAccount
     */
    public Map<String, Object> getSeatMessage(String seatAccount, String custId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        log.info("当前登录的企业id是 ： " + custId + "坐席账号是：" + seatAccount);
        //查询当前企业账号是否存在
        CustomerDO custMessage = customerDao.getCustMessage(custId);
        if (custMessage != null) {
            //根据坐席账号和企业id查询坐席信息
            StringBuffer sql = new StringBuffer("SELECT u.id id, p.property_value propertyValue, p.property_name propertyName\n");
            sql.append("FROM t_customer_user u LEFT JOIN t_customer_user_property p ON u.id = p.user_id\n");
            sql.append("WHERE p.property_name =  'cuc_seat' AND  u.cust_id =" + custId);
            sql.append(" AND u.account='" + seatAccount + "'");
            List<Map<String, Object>> list = customerDao.sqlQuery(sql.toString());
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
                //             callCenterId = customerMarketResource.getString(ResourceEnum.CALL.getCallCenterId());
            }
        }

        Map<String, Object> map = new HashMap<>();
        //查询当前企业账号是否存在
        CustomerDO custMessage = customerDao.getCustMessage(custId);
        Map<String, Object> result = null;
        if (custMessage != null) {
            //根据坐席账号和企业id查询坐席信息
            StringBuffer sql = new StringBuffer("SELECT u.id id, p.property_value propertyValue, p.property_name propertyName\n");
            sql.append("FROM t_customer_user u LEFT JOIN t_customer_user_property p ON u.id = p.user_id\n");
            sql.append("WHERE p.property_name =  'cuc_seat' AND  u.cust_id =" + custId);
            sql.append(" AND u.account='" + seatAccount + "'");
            List<Map<String, Object>> list = customerDao.sqlQuery(sql.toString());
            if (list.size() > 0) {
                String propertyValue = String.valueOf(list.get(0).get("propertyValue"));
                String userId = String.valueOf(list.get(0).get("id"));
                if (StringUtil.isNotEmpty(propertyValue)) {
                    JSONObject jsonObject = JSONObject.parseObject(propertyValue);
                    if (jsonObject != null) {
                        if (jsonObject.getString("mainNumber") != null) {
                            //先删除联通注册上的主叫号码（分机号码）
                            Map<String, Object> extensionDeleteResult = new CallCenterServiceImpl().unicomExtensionDelete(callCenterId, jsonObject.getString("mainNumber"));
                            log.info("坐席主叫号码删除" + ":" + extensionDeleteResult);
                        }
                        //调用联通接口进行增加主叫号码
                        result = new CallCenterServiceImpl().unicomExtensionRegister(callCenterId, mainNumber, 1);

                        log.info("坐席主叫号增加" + ":" + result);
                        if (result.get("result") != null && result.get("result").equals("0") || result.get("code").equals("211")) {
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
        int templateId = (int) marketResourceDao.saveReturnPk(marketTemplate);
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
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" and t.cust_id ='" + custId + "'");
        }
        if (StringUtil.isNotEmpty(templateId)) {
            sql.append(" and t.id  ='" + templateId + "'");
        }
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(sql.toString());
        return list;
    }

//    /**
//     * 获取token
//     *
//     * @param username
//     * @param password
//     */
//    public Map<String, Object> getTokenInfo(String username, String password) {
//        log.info("账号是：" + username + "密码是：" + password);
//        Map<String, Object> resultMap = new HashMap<>();
//        JSONObject json = new JSONObject();
//        String newpassword = CipherUtil.generatePassword(password);
//        String token = null;
//        if (StringUtil.isNotEmpty(username) && StringUtil.isNotEmpty(password)) {
//            CustomerUserDO u = customerService.getUserByName(username);
//            if (u != null) {
//                String uPassword = u.getPassword();
//                if (uPassword.equals(newpassword)) {
//                    String custId = u.getCust_id();
//                    if (StringUtil.isNotEmpty(custId)) {
//                        CustomerPropertyDO customerProperty = customerDao.getProperty(custId, "token");
//                        if (customerProperty != null) {
//                            token = customerProperty.getPropertyValue();
//                            try {
//                                Claims claims = verifyToken(token);
//                            } catch (ExpiredJwtException e) {
//                                resultMap.put("status", "003");
//                                resultMap.put("msg", "token失效");
//                                return resultMap;
//                            }
//                        } else {
//                            username = "customer." + username;
//                            token = generToken(custId, username, password);
//                            customerDao.dealCustomerInfo(custId, "token", token);
//                            log.info("获取token,第一次生成，token：" + token);
//                        }
//                    }
//                } else {
//                    resultMap.put("status", "002");
//                    resultMap.put("msg", "用户名密码不一致");
//                    return resultMap;
//                }
//            } else {
//                log.info("用户" + username + "不存在");
//                resultMap.put("status", "004");
//                resultMap.put("msg", "用户不存在");
//                return resultMap;
//            }
//        } else {
//            resultMap.put("status", "001");
//            resultMap.put("msg", "用户名或密码不能为空");
//            return resultMap;
//        }
//        resultMap.put("token", token);
//        resultMap.put("status", "000");
//        return resultMap;
//    }

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
                        //                idCardPrice = customerMarketResource.getDoubleValue(ResourceEnum.IDCARD.getPrice());
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
                batchListService.saveBatch(batchname, uploadNum, repairMode, compId, batchId, 0, SupplierEnum.CUC.getSupplierId());
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
            CustomerUserDO account = customerUserDao.getCustomer(seatAccount, custId);
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
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" AND comp_id = " + custId);
        }
        sqlBuilder.append(" ORDER BY upload_time DESC");
        Page result = batchDao.sqlPageQuery(sqlBuilder.toString(), pageNum, pageSize);
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
        if (StringUtil.isNotEmpty(batchId)) {
            sql.append("WHERE batch_id ='" + batchId + "'");
        }
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" and nl.comp_id ='" + custId + "'");
        }
        Page page = batchDao.sqlPageQuery(sql.toString(), pageNumber, pageSize);
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
        stringBuffer.append(" WHERE t.cust_id = " + custId);
        log.info("查询单条通话记录sql" + stringBuffer);
        Page page = batchDao.sqlPageQuery(stringBuffer.toString(), pageNum, pageSize);
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
        stringBuffer.append(" WHERE t.cust_id = " + custId);
        log.info("查询单条短信记录sql" + stringBuffer);
        Page page = batchDao.sqlPageQuery(stringBuffer.toString(), pageNum, pageSize);
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
        CustomerUserDO account = customerUserDao.getCustomer(seatAccount, custId);
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
        String querySql = "SELECT site address FROM  tmp_nl_batch_detail b  WHERE batch_id = 1542099439991 and id_card = '" + idCard + "'";
        List<Map<String, Object>> list = batchDao.sqlQuery(querySql);
        return list;
    }

    public ResponseInfo saveAccessChannels(Map<String, Object> map, HttpServletRequest request) {
        //1. 获取入参 mobile、channel、name、activity_code
        String mobile = map.get("mobile") == null || map.get("mobile") == "" ? "" : String.valueOf(map.get("mobile"));
        String channel = map.get("channel") == null || map.get("channel") == "" ? "" : String.valueOf(map.get("channel"));
        String name = map.get("name") == null || map.get("name") == "" ? "" : String.valueOf(map.get("name"));
        String activityCode = map.get("activity_code") == null || map.get("activity_code") == "" ? "" : String.valueOf(map.get("activity_code"));
        String channelName = map.get("channel_name") == null || map.get("channel_name") == "" ? "" : String.valueOf(map.get("channel_name"));
        String deviceType = map.get("device_type") ==null ||map.get("device_type") == "" ? "":String.valueOf(map.get("device_type"));
        log.info("入参的值为" + JSON.toJSONString(map));
        //2. 如果活动编码activity_code是ETC，且此次channel下有同样的mobile手机号，则返回新增失败
        if ("ETC".equalsIgnoreCase(activityCode)) {
            String countSql = "SELECT COUNT(*) AS count FROM t_access_channel WHERE activity_code='ETC' AND mobile='" + mobile + "' AND channel='" + channel + "'";
            Map<String, Object> countMap = jdbcTemplate.queryForMap(countSql);
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

    public Map<String,Object> saveBillNo(Map<String, Object> map) {
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
}

