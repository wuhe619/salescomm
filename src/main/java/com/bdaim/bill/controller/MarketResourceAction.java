package com.bdaim.bill.controller;

import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.batch.service.BatchService;
import com.bdaim.bill.dto.CallBackInfoParam;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.AuthPassport;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.PageList;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.smscenter.dto.SmsqueryParam;
import com.bdaim.supplier.dto.SupplierListParam;
import com.bdaim.template.dto.TemplateParam;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * @author yanls@bdaim.com
 * @Description: 触达记录
 * @date 2018/9/10 9:26
 */
@Controller
@RequestMapping("/marketResource")
public class MarketResourceAction extends BasicAction {

    private final static Logger LOG = LoggerFactory.getLogger(MarketResourceAction.class);

    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private BatchService batchService;

    /**
     * 通话历史
     */
    @RequestMapping(value = "/queryRecordVoicelog", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryRecordVoicelog(@Valid PageParam page, BindingResult error, String superId, Integer pageNum, Integer pageSize, String realName, String createTimeStart,
                                      String createTimeEnd, String enterpriseId, String batchId, String touchStatus, String enterpriseName) {
        if (error.hasFieldErrors()) {
            page.setPageNum(1);
            page.setPageSize(20);
        }
        if (page.getPageSize() > 100) {
            page.setPageSize(100);
        }
        String cust_id = "";
        Long userid = null;
        String user_type = "";
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        int touchStatu = 0;
        LoginUser lu = opUser();
        PageList list = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            LOG.info("后台用户查询通话历史记录");
        } else {
            cust_id = opUser().getCustId();
            userid = opUser().getId();
            user_type = String.valueOf(opUser().getUserType());
        }
        if (StringUtil.isNotEmpty(touchStatus)) {
            touchStatu = Integer.parseInt(touchStatus);
        }
        try{
            list = marketResourceService.queryRecordVoicelog(page, cust_id, userid, user_type, superId,
                    realName, createTimeStart, createTimeEnd, enterpriseId, batchId, touchStatu, enterpriseName);

            Map<String, Object> mapObj;
            for (int i = 0; i < list.getList().size(); i++) {
                mapObj = (Map<String, Object>) list.getList().get(i);
                if (mapObj != null && mapObj.get("remark") != null) {
                    String remark = mapObj.get("remark").toString();
                    String remarkArg[] = remark.split("\\{\\}");
                    String realname = "";
                    String enterprisename = "";
                    String account = "";
                    if (remarkArg != null && remarkArg.length >= 3) {
                        remark = remarkArg[0];
                        realname = remarkArg[1];
                        enterprisename = remarkArg[2];
                    }
                    if (StringUtil.isEmpty(realname)) {
                        if (mapObj.get("user_id") != null) {
                            String user_id = mapObj.get("user_id").toString();
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
                    mapObj.put("enterpriseName", enterprisename);
                    mapObj.put("account", account);
                    mapObj.put("realname", realname);
                    mapObj.put("remark", remark);
                }
            }
            map.put("data", list);
            //录音路径
            String audioUrl = "http://nolose.service.dev.datau.top/marketResource/getVoice/";
            map.put("audioUrl", audioUrl);
            json.put("data", map);

        } catch (Exception e) {
            LOG.info("查询通话记录出错 》》》》》 " + e);
        }
        return json.toJSONString();
    }


    /**
     * @description 获取失联人员被叫记录(根据唯一标识还有批次id)
     * @author:duanliying
     * @method 根据客户id查询
     * @date: 2018/9/10 9:45
     */
    @RequestMapping(value = "/callInfo.do", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String LostContactCallHistory(String batchId, String superId) {
        String custId = opUser().getCustId();
        return marketResourceService.queryCallHistory(batchId, superId, custId);
    }

    /**
     * 短信历史查询
     */
    @RequestMapping(value = "/querySmsHistory", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String querySmsHistory(@Valid PageParam page, BindingResult error, SmsqueryParam smsqueryParm) {
        if (error.hasFieldErrors()) {
            page.setPageNum(1);
            page.setPageSize(20);
        }
        if (page.getPageSize() > 100) {
            page.setPageSize(100);
        }
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        PageList list;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            smsqueryParm.setCompId(null);
            LOG.info("后台用户查询短信历史记录");
        } else {
            String cust_id = opUser().getCustId();
            smsqueryParm.setCompId(cust_id);
        }
        list = marketResourceService.querySmsHistory(page, smsqueryParm);
        Map<String, Object> map;
        for (int i = 0; i < list.getList().size(); i++) {
            map = (Map<String, Object>) list.getList().get(i);
            if (map != null && map.get("remark") != null) {
                String remark = map.get("remark").toString();
                String smsCustID = null;
                String smsBatchId = null;
                if (map.get("cust_id") != null) {
                    smsCustID = map.get("cust_id").toString();
                }
                if (map.get("batch_id") != null) {
                    smsBatchId = map.get("batch_id").toString();
                }
                LOG.info("查询短信历史纪录，短信记录表remark：" + remark);
                String remarkArg[] = remark.split("\\{\\}");
                String realname = "";
                String batchname = "";
                String teamplatename = "";
                String enterprisename = "";
                if (remarkArg != null && remarkArg.length >= 4) {
                    realname = remarkArg[0];
                    batchname = remarkArg[1];
                    teamplatename = remarkArg[2];
                    enterprisename = remarkArg[3];
                }
                if (StringUtil.isEmpty(enterprisename)) {
                    LOG.info("调用接口获取企业名称，企业id:" + smsCustID);
                    if (map.get("enterprise_id") != null) {
                        enterprisename = customerDao.getEnterpriseName(smsCustID);
                    }
                }
                if (StringUtil.isEmpty(batchname)) {
                    LOG.info("调用接口获取批次名称，批次id:" + smsBatchId);
                    if (map.get("batch_id") != null) {
                        batchname = batchService.batchNameGet(smsBatchId);
                    }
                }
                map.put("realName", realname);
                map.put("batchName", batchname);
                map.put("teamplateName", teamplatename);
                map.put("enterpriseName", enterprisename);
            }
        }

        json.put("data", list);
        return json.toJSONString();
    }

    @RequestMapping(value = "/useCallCenter", method = RequestMethod.POST)
    @ResponseBody
    public String useCallCenter(@RequestBody Map<String, Object> params) {
      /*  Map<Object, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        Long userId = opUser().getId();
        String customerId = opUser().getCustId();
        String userType = opUser().getCustId();
        //触达Id
        String touchId = Long.toString(IDHelper.getTransactionId());
        String id = String.valueOf(params.get("id"));
        String batchId = String.valueOf(params.get("batchId"));
        marketResourceService.callCustomer(params, userId, customerId, userType);
        if (StringUtil.isEmpty(id) || StringUtil.isEmpty(batchId)) {
            map.put("msg", "请求参数异常");
            map.put("code", 0);
            json.put("data", map);
            return json.toJSONString();
        }
        // 判断是余额是否充足
        boolean judge = marketResourceService.judRemainAmount(customerId);
        if (!judge) {
            map.put("msg", "余额不足");
            map.put("code", 1003);
            json.put("data", map);
            return json.toJSONString();
        }
        int code = 0;
        String message = "";
        //根据batchId查询certify_type判断外呼渠道
        Integer certifyType = 0;//默认使用联通外呼
        BatchListEntity batchMessage = batchDao.getBatchMessage(batchId);
        if (batchMessage != null) {
            certifyType = batchMessage.getCertifyType();
        }
        if (certifyType == 0) {
            //使用联通外呼
            boolean success = false;
            // 唯一请求ID
            String callId = null, activityId = null, enterpriseId = null;
            Map<String, Object> callResult = null;
            try {
                LOG.info("调用外呼接口参数:" + "customerId" + customerId + "userId" + String.valueOf(opUser().getId()) + "id:" + id + "batchId:" + batchId);
                callResult = marketResourceService.seatMakeCallEx(customerId, String.valueOf(opUser().getId()), id, batchId);
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
            String resourceId = marketResourceService.queryResourceId(ConstantsUtil.SUPPLIERID__CUC, ConstantsUtil.CALL_TYPE);
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

            String userName = customerService.getUserRealName(String.valueOf(opUser().getId()));
            LOG.info("打电话获取到的userName:" + userName);

            String remark = "{}" + userName + "{}" + customerService.getEnterpriseName(opUser().getCustId());

            marketResourceLogDTO.setRemark(remark);
            LOG.info("通话初次保存备注touchId:" + touchId + ",remark:" + remark);

            // 判断是否管理员进行的外呼
            if ("1".equals(opUser().getUserType())) {
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
        } else {
            LOG.info("使用外呼方式是讯众外呼" + "客户id是：" + id + "批次id" + batchId);
            map = marketResourceService.xZCallResource(opUser().getUserType(), batchId, userId, id, customerId, certifyType);
        }
        json.put("data", map);
        return json.toJSONString();
    }*/

        JSONObject json = new JSONObject();
        Long userId = opUser().getId();
        String customerId = opUser().getCustId();
        String userType = opUser().getCustId();
        Map<Object, Object> map = marketResourceService.callCustomer(params, userId, customerId, userType);
        return json.toJSONString(map);
    }

    @RequestMapping(value = "/useSendSms", method = RequestMethod.POST)
    @ResponseBody
    public String useSendSms(@RequestBody Map<String, Object> params) {
        Long userId = opUser().getId();
        String custId = opUser().getCustId();

        Map<String, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        String batchId = String.valueOf(params.get("batchId"));
        String templateId = String.valueOf(params.get("templateId"));
        String customerIds = String.valueOf(params.get("customerIds"));
        String variables = String.valueOf(params.get("variables"));
        try {
            if (StringUtil.isEmpty(batchId) || StringUtil.isEmpty(templateId) || StringUtil.isEmpty(customerIds)) {
                map.put("msg", "请求参数异常");
                map.put("code", 001);
                json.put("data", map);
                return json.toJSONString();
            }
            map = marketResourceService.sendBatchSms(variables, custId, String.valueOf(userId), Integer.parseInt(templateId), batchId, customerIds, 1, 2);
        } catch (Exception e) {
            LOG.error("发送短信异常" + e);
        }
        json.put("data", map);
        return json.toJSONString();
    }

    @RequestMapping(value = "/seatAgentReset", method = RequestMethod.GET)
    @ResponseBody
    public String seatAgentReset(String userName) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = marketResourceService.seatAgentReset(opUser().getCustId(), opUser().getId().toString(), 1);
        json.put("data", map);
        return json.toJSONString();
    }

    @RequestMapping(value = "/getSeatCurrentStatus", method = RequestMethod.GET)
    @ResponseBody
    public String getSeatCurrentStatus(String userName) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = marketResourceService.seatGetCurrentStatus(opUser().getCustId(), opUser().getId().toString(), 1);
        json.put("data", map);
        return json.toJSONString();
    }

    @RequestMapping(value = "/setWorkPhoneNum", method = RequestMethod.POST)
    @ResponseBody
    public String setWorkNum(String workNum, String userId) {
        return marketResourceService.setWorkPhoneNum(workNum, userId);
    }

    /**
     * 供应商列表查询接口
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getSupplierList.do", method = RequestMethod.GET)
    public String searchPage(@Valid PageParam page, BindingResult error, SupplierListParam supplierListParam) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        LoginUser lu = opUser();
        PageList list = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = marketResourceService.getSupplierList(page, supplierListParam);
        }
        return JSON.toJSONString(list);
    }


    /**
     * 供应商成本价查询接口
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getSupplierPrice", method = RequestMethod.GET)
    public String searchSupplierPrice(SupplierListParam supplierListParam) {
        LoginUser lu = opUser();
        Map<String, Object> resultMap = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            resultMap = marketResourceService.searchSupplierPrice(supplierListParam);
        }
        return JSON.toJSONString(resultMap);
    }

    /**
     * 渠道管理设置定价
     *
     * @return
     */
    @RequestMapping(value = "/setPrice.do", method = RequestMethod.POST)
    @ResponseBody
    public String updateMarketResource(@RequestBody SupplierListParam supplierListParam) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<>();
        LoginUser lu = opUser();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            try {
                String supplierId = supplierListParam.getSupplierId();
                if (StringUtil.isNotEmpty(supplierId)) {
                    marketResourceService.setPrice(supplierListParam);
                    map.put("code", "0");
                    map.put("_message", "设置定价更新成功");
                } else {
                    map.put("code", "1");
                    map.put("_message", "供应商ID不能未空，设置定价更新失败");
                }

            } catch (Exception e) {
                e.printStackTrace();
                LOG.error("更新成本价异常" + e);
                map.put("code", "1");
                map.put("_message", "设置定价更新失败");
            }
        } else {
            map.put("code", "1");
            map.put("_message", "没权限，设置定价更新失败");
        }
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 首页信息(前端、后台的首页统计图表信息)
     *
     * @param customerId 企业客户ID，企业登录后获取，如果为空则说明是后端
     * @return
     * @auther Chacker
     * @date 2019/8/6 10:26
     */
    @RequestMapping(value = "/countMarketData", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public ResponseInfo countMarketData(String customerId) {
        Map<String, Object> marketData = marketResourceService.countMarketData(customerId);
        return new ResponseInfoAssemble().success(marketData);
    }

    /**
     * 后台首页的统计图信息
     *
     * @param
     * @return
     * @auther Chacker
     * @date
     */
    @RequestMapping(value = "/countMarketDataBackend", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public ResponseInfo countMarketDataBackend() {
        Map<String, Object> marketData = marketResourceService.countMarketDataBackend();
        return new ResponseInfoAssemble().success(marketData);
    }

    /**
     * 短信模板列表
     */
    @ResponseBody
    @RequestMapping(value = "/querySmsTemplateList.do", method = RequestMethod.GET)
    public String querySmsTemplateList(@Valid PageParam page, BindingResult error, TemplateParam templateParam) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        LoginUser lu = opUser();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            templateParam.setCompId(null);
        } else {
            templateParam.setCompId(opUser().getCustId());
        }
        PageList list = marketResourceService.getSmsTemplateList(page, templateParam);
        return JSON.toJSONString(list);
    }

    /**
     * 创建与修改短信邮件模板内容
     * 1000 创建
     * 2000 修改
     */
    @RequestMapping(value = "/dealSmsTemplate", method = RequestMethod.POST)
    @ResponseBody
    public String dealSmsTemplate(@RequestBody TemplateParam templateParam) {
        LoginUser lu = opUser();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            LOG.info("后台用户设置短信模板");
        } else {
            templateParam.setCompId(opUser().getCustId());
        }

        Map<String, Object> map = marketResourceService.updateSmsTemplate(templateParam);
        JSONObject json = new JSONObject();
        json.put("data", map);
        return json.toJSONString();
    }


    /**
     * 短信历史查询(对外接口)
     */
    @AuthPassport
    @RequestMapping(value = "/open/getSmslog.do", method = RequestMethod.POST)
    @ResponseBody
    public String getSmslog(@RequestBody JSONObject param) {
        Integer pageNum = param.getInteger("pageNum");
        Integer pageSize = param.getInteger("pageSize");
        PageParam page = new PageParam();
        if (pageNum == null) {
            page.setPageNum(1);
        } else {
            page.setPageNum(pageNum);
        }
        if (pageSize == null) {
            page.setPageSize(20);
        } else {
            page.setPageSize(pageSize);
        }
        CustomerUser u = (CustomerUser) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        JSONObject json = new JSONObject();
        PageList list = marketResourceService.openSmsHistory(page, custId);
        json.put("data", list);
        return json.toJSONString();
    }

    /**
     * @description 联通话单推送接口
     * @author:duanliying
     * @method c
     * @date: 2018/12/5 19:15
     */
    @RequestMapping(value = "/unicomCallBack.do", method = RequestMethod.POST)
    public void getCallBackInfo(@RequestBody CallBackInfoParam callBackInfoParam, HttpServletResponse response) {
        LOG.info("获取失联修复联通呼叫中心话单推送开始,推送数据是" + callBackInfoParam.toString());
        //处理返回数据进行DB操作
        try {
            response.setContentType("application/json");
            int i = marketResourceService.addCallBackInfoMessage(callBackInfoParam);
            if (i > 0) {
                PrintWriter printWriter = response.getWriter();
                printWriter.print("{\"code\":\"0\"}");
                printWriter.flush();
                response.flushBuffer();
                return;
            }
        } catch (Exception e) {
            LOG.error("获取失联修复联通呼叫中心话单异常" + e);
        }
    }


    /**
     * @description 联通录音推送接口
     * @author:duanliying
     * @method
     * @date: 2018/12/5 19:15
     */
    @RequestMapping(value = "/unicomRecord.do", method = RequestMethod.POST)
    public void getUnicomRecord(@RequestBody JSONObject param, HttpServletResponse response) {
        LOG.info("获取失联修复联通呼叫中心录音文件推送数据是" + param.toString());
        //处理返回数据进行DB操作
        try {
            String result = marketResourceService.getUnicomRecordfile(param);
            if ("0".equals(result)) {
                response.setContentType("application/json");
                PrintWriter printWriter = response.getWriter();
                printWriter.print("{\"code\":\"0\"}");
                printWriter.flush();
                response.flushBuffer();
                return;
            }
        } catch (Exception e) {
            LOG.error("获取失联修复联通录音获取异常" + e);
        }
    }


    //后台 触达记录导出
    @RequestMapping(value = "/exportreach", method = RequestMethod.GET, produces = "application/vnd.ms-excel;charset=UTF-8")
    @ResponseBody
    public Object exportreach(String superId, String realName, String createTimeStart, String createTimeEnd, String enterpriseId, String batchId, String touchStatus, String enterpriseName, HttpServletResponse response) {
        int touchStatu = 0;
        LoginUser lu = opUser();
        LOG.info("后台用户查询通话历史记录");
        String cust_id = opUser().getCustId();
        Long userid = opUser().getId();
        String user_type = String.valueOf(opUser().getUserType());
        if (StringUtil.isNotEmpty(touchStatus)) {
            touchStatu = Integer.parseInt(touchStatus);
        }
        return marketResourceService.exportreach(cust_id, userid, user_type, superId,
                realName, createTimeStart, createTimeEnd, enterpriseId, batchId, touchStatu, enterpriseName, response);


    }

    /**
     * 获取录音文件地址（hbase）
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/12 12:48
     */
    @RequestMapping("/getVoice/{userId}/{fileName:.+}")
    public void getVoiceFile(@PathVariable String userId, @PathVariable String fileName, HttpServletResponse response) throws Exception {
        marketResourceService.getNoloseVoiceFile(userId, fileName, request, response);
    }
}




