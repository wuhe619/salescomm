package com.bdaim.bill.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.batch.dto.TouchInfoDTO;
import com.bdaim.batch.service.BatchService;
import com.bdaim.bill.dto.CallBackInfoParam;
import com.bdaim.bill.dto.MarketResourceBuyDTO;
import com.bdaim.callcenter.service.impl.CallCenterService;
import com.bdaim.callcenter.service.impl.SeatsService;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.page.PageList;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.service.PhoneService;
import com.bdaim.crm.entity.LkCrmAdminRecordEntity;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.crm.service.CrmContactsService;
import com.bdaim.crm.erp.crm.service.CrmCustomerService;
import com.bdaim.crm.erp.crm.service.CrmLeadsService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerLabelDTO;
import com.bdaim.customer.entity.CustomerLabel;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.service.CustomerLabelService;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customersea.dto.CustomerSeaSmsSearch;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.customgroup.dto.CustomerGrpOrdParam;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.markettask.service.MarketTaskService;
import com.bdaim.order.service.OrderService;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.resource.dto.CallBackParam;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.dto.PeopleAssignedDTO;
import com.bdaim.resource.dto.VoiceLogQueryParam;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.smscenter.dto.SmsqueryParam;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.smscenter.util.SmsAction;
import com.bdaim.supplier.dto.SupplierListParam;
import com.bdaim.template.dto.MarketTemplateDTO;
import com.bdaim.template.dto.TemplateParam;
import com.bdaim.util.*;

import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.client.RestClientException;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.*;

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
    @Autowired
    private CrmLeadsService crmLeadsService;
    @Autowired
    private CrmCustomerService crmCustomerService;
    @Autowired
    private CrmContactsService crmContactsService;
    @Autowired
    private AdminFieldService adminFieldService;

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
        try {
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
            String audioUrl = "/marketResource/getVoice/";
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
            map = marketResourceService.sendBatchSmsV1(variables, custId, String.valueOf(userId), Integer.parseInt(templateId), batchId, customerIds, 1, 2);
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
    public String setWorkPhoneNum(String workNum, String userId) {
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
     * @description 联通话单推送接口V1
     * @author:duanliying
     * @method
     * @date: 2018/12/5 19:15
     */
    @RequestMapping(value = "/callBack/call", method = RequestMethod.POST)
    public void getCallBackInfov1(@RequestBody CallBackInfoParam callBackInfoParam, HttpServletResponse response) {
        LOG.info("获取失联修复联通呼叫中心话单V1推送开始,推送数据是" + callBackInfoParam.toString());
        //处理返回数据进行DB操作
        try {
            response.setContentType("application/json");
            int i = marketResourceService.callBackInfoV1(callBackInfoParam);
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

    /**
     * @description 联通录音推送接口V1
     * @author:duanliying
     * @method
     * @date: 2018/12/5 19:15
     */
    @RequestMapping(value = "/callBack/record", method = RequestMethod.POST)
    public void getUnicomRecordV1(@RequestBody JSONObject param, HttpServletResponse response) {
        LOG.info("获取失联修复联通呼叫中心录音文件V1推送数据是" + param.toString());
        //处理返回数据进行DB操作
        try {
            String result = marketResourceService.getUnicomRecordfileV1(param);
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

    /**
     * @description 联通短信状态推送
     * @author:duanliying
     * @method
     * @date: 2018/12/5 19:15
     */
    @RequestMapping(value = "/callBack/sms", method = RequestMethod.POST)
    public void getUnicomSmsStautsV1(@RequestBody JSONObject param, HttpServletResponse response) {
        LOG.info("联通短信状态V1推送数据是" + param.toString());
        //处理返回数据进行DB操作
        try {
            String result = marketResourceService.getUnicomSmsStatusV1(param);
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

    /**
     * 通话结算中状态码
     */
    private final static int VOICE_SETTLEMENT_CODE = 1001;

    /**
     * 通话成功状态码
     */
    private final static int VOICE_SUCCESS_CODE = 1001;

    /**
     * 通话失败状态码
     */
    private final static int VOICE_FAIL_CODE = 1002;


    @Resource
    private CustomGroupService customGroupService;
    @Resource
    private OrderService orderService;
    @Resource
    private CallCenterService callCenterService;
    @Resource
    private CustomerService customerService;
    @Resource
    private PhoneService phoneService;
    @Resource
    private SendSmsService smsService;
    @Resource
    private SeatsService seatsService;
    @Resource
    private MarketTaskService marketTaskService;

    @Resource
    private CustomerLabelService customerLabelService;

    @Resource
    private CustomerSeaService customerSeaService;

    /**
     * 营销资源查询（短信，电话，邮件剩余量）
     *
     * @param request
     * @param response
     * @return
     */
    @RequestMapping(value = "/contactCustomer/query", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryMarketResource(HttpServletRequest request, HttpServletResponse response) {
        String cust_id = opUser().getCustId();
        // String cust_id="18888";
        List<Map<String, Object>> result = marketResourceService.queryMarketResource(cust_id);
        return JSON.toJSONString(result);
    }

    /**
     * 查看客户群明细
     *
     * @param groupId
     * @param superId
     * @param pageNum
     * @param pageSize
     * @return
     */
    @RequestMapping(value = "/customerDetail/query", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryMarketResourceDetail(String groupId, String superId, String pageNum, String pageSize) {
        String cust_id = opUser().getCustId();
        // String userid="18888";

        List<Map<String, Object>> result = marketResourceService.queryMarketResourceDetail(groupId, cust_id, superId,
                pageNum, pageSize);
        return JSON.toJSONString(result);
    }

    /**
     * 记录LOG
     *
     * @param groupId
     * @param superId
     * @param pageNum
     * @param pageSize 根据资源类型Type_code（1.voice 2.SMS 3.email）分别进行插入操作
     */
    @RequestMapping(value = "/customerDetail/insertLog", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public void inserLOG(String groupId, String superId, String pageNum, String pageSize) {
        MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
        marketResourceLogDTO.setType_code("1");
        marketResourceService.insertLog(marketResourceLogDTO);
        ;
        marketResourceLogDTO.setType_code("2");
        marketResourceService.insertLog(marketResourceLogDTO);
        ;
        marketResourceLogDTO.setType_code("3");
        marketResourceService.insertLog(marketResourceLogDTO);
        ;
    }

    /**
     * 营销资源购买
     */
    @RequestMapping(value = "/buyMarketResource", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String bugMarketResource(JSONObject jsonObject) {
        LoginUser lu = opUser();
        String cust_id = lu.getCustId();
        // 根据支付方式选择
        Map<String, String> jsonMap = null;
        if (jsonObject.getString("pay_type").equals("1")) {
            // 余额,  参数：资源id 资源类型
            jsonMap = marketResourceService.buyMarketResource(jsonObject.getString("resource_id"), jsonObject.getString("type_code"), jsonObject.getInteger("num"), lu.getId().toString(), cust_id, jsonObject.getString("pay_type"), jsonObject.getString("pay_password"), lu.getEnterpriseName(), null);
        }
        JSONObject json = new JSONObject();
        json.put("data", jsonMap);
        return json.toJSONString();
    }

    /**
     * 营销资源购买
     */
    @RequestMapping(value = "/buyMarketResourceCustmerGroup", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String bugMarketResourceCustmerGroup(final String orderNo, String num, String pay_password, String pay_type) {
        LoginUser lu = opUser();
        if (!"1".equals(lu.getUserType())) {
            return "";
        }
        String cust_id = lu.getCustId();
        String enpterprise_name = opUser().getEnterpriseName();
        MarketResourceBuyDTO dto = new MarketResourceBuyDTO();
        // 资源类型；数量；支付方式
        dto.setThird_party_num("");
        dto.setPay_type(pay_type);
        dto.setOrderNo(orderNo);
        dto.setCust_id(cust_id);
        // 支付密码
        dto.setPay_password(pay_password);
        dto.setEnpterprise_name(enpterprise_name);
        // 根据支付方式选择
        Map<String, String> jsonMap = null;
        if ("1".equals(pay_type)) {
            // 余额
            jsonMap = customGroupService.buyCustomGroup(orderNo, lu.getId(), cust_id, pay_type, pay_password, false);

            String successBuym = jsonMap.get("code");
            if (successBuym != null && "0".equals(successBuym)) {
                // 余额支付之后跑客户群 == 调用异步发送消息
                try {
                    customGroupService.addCustomGroupData0(orderNo);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            }
        }

        JSONObject json = new JSONObject();
        json.put("data", jsonMap);
        return json.toJSONString();
    }

    /**
     * 创建短信邮件模板
     *
     * @param smsemailContent 短信或邮件内容
     * @param type            参数 1.短信 2.邮件
     * @param Title           标题
     * @return
     */
    @RequestMapping(value = "/createSmsEmailTemplate", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String createSmsEmailTemplate(String smsemailContent, String type, String Title, String sms_signatures, Integer marketProjectId) {
        String cust_id = opUser().getCustId();
        JSONObject json = new JSONObject();
        Map<String, String> map = marketResourceService.createSmsEmailTemplate(smsemailContent, type, Title, cust_id, sms_signatures, marketProjectId);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 创建闪信模板
     *
     * @param content
     * @param type
     * @param title
     * @param signatures
     * @return
     */
    @RequestMapping(value = "/createFlashSmsTemplate", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String createFlashSmsTemplate(String content, String type, String title, String signatures) {
        JSONObject json = new JSONObject();
        Map<String, String> map = marketResourceService.createFlashSmsTemplate(content, type, title, opUser().getCustId(), signatures);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 查询短信邮件模板
     *
     * @param templateName 模板名称
     * @param templateId   模板ID
     * @param status       审核状态
     * @return
     */
    @RequestMapping(value = "/querySmsEmailTemplateList", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String querySmsEmailTemplateList(String type_code, String templateName, String templateId, String status,
                                            Integer pageNum, Integer pageSize, String custName, String marketProjectId) {
        LoginUser lu = opUser();
        String cust_id = null;
        if (!"admin".equals(lu.getRole()) && !"USER_ROLE".equals(lu.getRole())) {
            cust_id = opUser().getCustId();
            if (cust_id == null)
                cust_id = "0";
        }
        // String cust_id="18888";
        JSONObject json = new JSONObject();
        Map<Object, Object> mapreturn = new HashMap<Object, Object>();
        /*List<Map<String, Object>> map = marketResourceService.getSmsEmailTemplateList(type_code, templateName,
                templateId, status, cust_id, pageNum, pageSize);
        List<Map<String, Object>> mapSize = marketResourceService.getSmsEmailTemplateList(type_code, templateName,
                templateId, status, cust_id, 0, 100000);*/
        Page page = marketResourceService.getSmsEmailTemplateListV1(type_code, templateName,
                templateId, status, custName, pageNum, pageSize, cust_id, marketProjectId);
        mapreturn.put("Code", 0);
        mapreturn.put("templateList", page.getData());
        mapreturn.put("total", page.getTotal());
        json.put("data", mapreturn);
        return json.toJSONString();
    }

    @RequestMapping(value = "/queryFlashSmsTemplateList", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryFlashSmsTemplateList(String typeCode, String templateName, String templateId, String status,
                                            Integer pageNum, Integer pageSize) {
        JSONObject json = new JSONObject();
        Map<Object, Object> data = new HashMap<Object, Object>();
        List<Map<String, Object>> map = marketResourceService.getFlashSmsTemplateList(typeCode, templateName,
                templateId, status, opUser().getCustId(), pageNum, pageSize);
        List<Map<String, Object>> mapSize = marketResourceService.getFlashSmsTemplateList(typeCode, templateName,
                templateId, status, opUser().getCustId(), 0, 100000);
        data.put("code", 0);
        data.put("templateList", map);
        data.put("total", mapSize.size());
        json.put("data", data);
        return json.toJSONString();
    }

    @RequestMapping(value = "/queryEmailTemplate", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String queryEmailbyUser(String templateId) {
        // 根据模版Id查找模版主题和内容
        String emailContent = marketResourceService.getSmsEmailTemplate("2", templateId, opUser());

        return emailContent;
    }

    /**
     * 查询短信邮件模板内容
     *
     * @param templateId
     * @return
     */
    @RequestMapping(value = "/getSmsEmailTemplate", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String getSmsEmailTemplate(String type_code, String templateId) {
        MarketTemplateDTO marketTemplateDTO = marketResourceService.getSmsEmailTemplateV1(type_code, templateId, opUser());
        JSONObject json = new JSONObject();
        json.put("data", marketTemplateDTO);
        return json.toJSONString();
    }

    /**
     * 修改短信邮件模板内容
     *
     * @param templateId
     * @param smsemailContent
     * @return
     */
    @RequestMapping(value = "/updateSmsEmailTemplate", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String updateSmsEmailTemplate(String type_code, String templateId, String smsemailContent, String title, String sms_signatures, Integer marketProjectId, String status) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = marketResourceService.updateSmsEmailTemplate(type_code, templateId, smsemailContent, title, sms_signatures, marketProjectId, status);
        json.put("data", map);
        return json.toJSONString();
    }

    @RequestMapping(value = "/updateFlashSmsTemplate", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String updateFlashSmsTemplate(String type, String templateId, String content, String title, String signatures) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = marketResourceService.updateFlashSmsTemplate(type, templateId, content, title, signatures);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 短信和邮件模板审批
     *
     * @param typeCode
     * @param resourceId
     * @param Status
     * @param remark
     * @return
     */
    @RequestMapping(value = "/approve", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String approve(String typeCode, String templateId, String Status, String remark, String templateCode, String resourceId) {
        int code = 0;
        try {
            code = marketResourceService.useMarketResource(typeCode, templateId, Status, remark, templateCode, resourceId, String.valueOf(opUser().getId()));
        } catch (Exception e) {
            LOG.error("审核模板异常,", e);
        }
        if (code == 1) {
            return returnSuccess("成功");
        }
        return returnError();
    }

    @RequestMapping(value = "/approveFlashSmsTemplate", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String approveFlashSmsTemplate(String typeCode, String templateId, String status, String remark, String templateCode) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = marketResourceService.approveFlashSmsTemplate(typeCode, templateId, status, remark, templateCode);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 号码审批
     *
     * @param status 外呼号码状态（1.可用 2.不可用）
     * @param userid 客户id
     * @return
     */
    @RequestMapping(value = "/approve/phone", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String approvePhone(String status, String userid, String workNum, String remark) {
        LoginUser lu = opUser();
        if (!"admin".equals(lu.getRole()) && !"ROLE_USER".equals(lu.getRole())) {
            return "ERROR ROLE";
        }
        JSONObject json = new JSONObject();
        Map<String, Object> map = marketResourceService.approvePhone(status, userid, workNum, remark);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 通话历史(不再使用)
     *
     * @param superId
     * @return
     */
    @RequestMapping(value = "/queryRecordVoicelog0", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryRecordVoicelog(String superId, Integer pageNum, Integer pageSize, String realName, String createTimeStart,
                                      String createTimeEnd) {
        String cust_id = opUser().getCustId();
        Long userid = opUser().getId();
        String user_type = opUser().getUserType();
        // String cust_id="18888";
        // List<Map<String, Object>> result =
        // marketResourceService.queryRecordVoiceLOG(cust_id,user_type,superId);
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        List<Map<String, Object>> list = marketResourceService.queryRecordVoicelog(cust_id, userid, user_type, superId,
                pageNum, pageSize, realName, createTimeStart, createTimeEnd);
        List<Map<String, Object>> list2 = marketResourceService.queryRecordAllVoicelogCount(cust_id, userid, user_type,
                superId, realName, createTimeStart, createTimeEnd);
        map.put("data", list);
        map.put("total", list2.get(0).get("count"));
        //录音路径
        String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
        map.put("audioUrl", audioUrl);
        json.put("data", map);
        return json.toJSONString();

    }

    /**
     * 通话历史(不再使用)
     *
     * @param pageParam
     * @param error
     * @param superId
     * @param customerGroupId
     * @param realName
     * @param createTimeStart
     * @param createTimeEnd
     * @param remark
     * @param callStatus
     * @param intentLevel
     * @param auditingStatus
     * @return
     */
    @RequestMapping(value = "/queryRecordVoicelogV0", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryRecordVoicelogV0(@Valid PageParam pageParam, BindingResult error, String superId, String customerGroupId, String realName, String createTimeStart,
                                        String createTimeEnd, String remark, String callStatus, String intentLevel, String auditingStatus) {
        if (error.hasErrors()) {
            return getErrors(error);
        }
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        List<Map<String, Object>> list = null;
        long total = 0;
        list = marketResourceService.queryRecordVoiceLogV3(opUser(), customerGroupId, superId, pageParam.getPageNum(), pageParam.getPageSize(), realName, createTimeStart, createTimeEnd, remark, callStatus, intentLevel, auditingStatus);
        total = marketResourceService.queryRecordAllVoiceLogCountV3(opUser(), customerGroupId, superId, realName, createTimeStart, createTimeEnd, remark, callStatus, intentLevel, auditingStatus);
        map.put("data", list);
        map.put("total", total);
        // 录音路径
        String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
        map.put("audioUrl", audioUrl);
        json.put("data", map);
        return json.toJSONString();

    }

    /**
     * 查询通话记录
     *
     * @param pageParam
     * @param error
     * @param superId
     * @param customerGroupId
     * @param realName
     * @param createTimeStart
     * @param createTimeEnd
     * @param remark
     * @param callStatus
     * @param intentLevel
     * @param auditingStatus
     * @param marketTaskId
     * @return
     */
    @RequestMapping(value = "/queryRecordVoicelogV1", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryRecordVoicelogV2(@Valid PageParam pageParam, BindingResult error, String superId, String customerGroupId, String realName, String createTimeStart,
                                        String createTimeEnd, String remark, String callStatus, String intentLevel, String auditingStatus,
                                        String marketTaskId, String calledDuration, String labelProperty, String seaId, String group_id, String custName) {
        if (error.hasErrors()) {
            return getErrors(error);
        }
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        UserQueryParam userQueryParam = getUserQueryParam();
        userQueryParam.setPageNum(pageParam.getPageNum());
        userQueryParam.setPageSize(pageParam.getPageSize());
        // 通话时长范围检索
        int duration = 0;
        if (StringUtil.isNotEmpty(calledDuration)) {
            duration = NumberConvertUtil.parseInt(calledDuration);
        }
        Page page = null;
        if (StringUtil.isEmpty(customerGroupId)) {
            customerGroupId = group_id;
        }
        try {
            page = marketResourceService.queryRecordVoiceLogV4(userQueryParam, customerGroupId, superId, realName, createTimeStart,
                    createTimeEnd, remark, callStatus, intentLevel, auditingStatus, marketTaskId, duration, labelProperty, seaId, custName);
        } catch (Exception e) {
            page = new Page();
            LOG.error("查询通话记录分页失败,", e);
        }
        map.put("data", page.getData());
        map.put("total", page.getTotal());
        // 录音路径
        String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
        map.put("audioUrl", audioUrl);
        json.put("data", map);
        return json.toJSONString();

    }

    /**
     * 设置通话号码
     *
     * @param workNum
     * @param userid
     * @return
     */
    @RequestMapping(value = "/setWorkNum", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String setWorkNum(String workNum, String userid, String source) {
        LoginUser lu = opUser();
        if ("1".equals(lu.getUserType())) {
            return marketResourceService.setWorkNum(workNum, userid, lu.getCustId(), source);
        }
        return "";
    }

    /**
     * 营销资源定价
     *
     * @param resouceId
     * @param costPrice
     * @param salePrice
     * @param type
     * @return
     */
    @RequestMapping(value = "/price{type}", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String setPrice(String resouceId, Double costPrice, Double salePrice, String type) {
        return marketResourceService.setPrice(resouceId, costPrice, salePrice, type);
    }

    @RequestMapping(value = "/queryWorkNumList", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryWorkNumList(String username, String account, Integer pageNum, Integer pageSize) {
        String cust_id = opUser().getCustId();
        // String cust_id="18888";
        // List<Map<String, Object>> result =
        // marketResourceService.queryRecordVoiceLOG(cust_id,user_type,superId);
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        List<Map<String, Object>> list = marketResourceService.queryWorkNumList(cust_id, "", username, account, pageNum, pageSize);
        long total = marketResourceService.queryWorkNumAllCount(cust_id, "", username);
        map.put("data", list);
        map.put("total", total);
        json.put("data", map);
        return json.toJSONString();
    }


    @RequestMapping(value = "/queryWorkNumListAll", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryWorkNumListAll(String username, String workNumStatus, Integer pageNum, Integer pageSize) {
        String cust_id = "";
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        Page page = null;
        try {
            page = marketResourceService.queryWorkNumListV1(cust_id, workNumStatus, username, pageNum, pageSize);
            map.put("data", page.getData());
            map.put("total", page.getTotal());
        } catch (Exception e) {
            LOG.error("查询通话审核列表失败,", e);
            map.put("data", new ArrayList<>());
            map.put("total", 0);
        }
        json.put("data", map);
        return json.toJSONString();
    }


    /**
     * 用途:用于呼叫中心类型的手动外呼,前端调用呼叫中心js执行外呼后，
     * 拿到呼叫中心的通话回调参数(通话callId,通话状态等信息)，调用后台接口来更新当前通话记录的通话状态
     *
     * @param callId
     * @param callStatus
     * @param touchId
     * @return
     */
    @RequestMapping(value = "/saveTouchVoiceLog", method = RequestMethod.POST)
    @ResponseBody
    public String saveTouchVoiceLog(String callId, String callStatus, String touchId, String customerGroupId, String marketTaskId, String superId) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        JSONObject json = new JSONObject();
        String code = "1";
        String message = "成功";
        // 插入外呼日志表
        MarketResourceLogDTO dto = new MarketResourceLogDTO();
        dto.setStatus(1002);
        // 主叫成功
        if ("1".equals(callStatus)) {
            dto.setStatus(1001);
        }
        try {
            marketResourceService.updateVoiceLogStatusV3(touchId, dto.getStatus(), callId, customerGroupId, marketTaskId, superId);
        } catch (Exception e) {
            LOG.error("更新通话记录通话状态异常", e);
        }
        map.put("code", code);
        map.put("message", message);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 用途: 用于呼叫中心类型的自动外呼
     * 说明: 前端接收到呼叫中心的通话回调后，调用此接口，保存此次通话的状态和callId。
     * 程序流程: 通过手机号和customerGroupId查询superId，然后用superId和callId，callStatus来保存通话状态
     *
     * @param phone
     * @param customerGroupId
     * @param callId
     * @param callStatus
     * @return
     */
    @RequestMapping(value = "/saveAutoTouchVoiceLog", method = RequestMethod.POST)
    @ResponseBody
    public String saveAutoTouchVoiceLog(String phone, String customerGroupId, String callId, String callStatus, String marketTaskId, String seaId) {
        LOG.info("保存自动外呼phone:" + phone + ",customerGroupId:" + customerGroupId + ",callId:" + callId + ",callStatus:" + callStatus + ",marketTaskId:" + marketTaskId + ",seaId:" + seaId);
        if (StringUtil.isEmpty(callId)) {
            LOG.error("保存自动外呼phone:" + phone + ",customerGroupId:" + customerGroupId + ",callId:" + callId + ",callStatus:" + callStatus + "callId为空");
        }
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        String touchId = opUser().getId() + Long.toString(IDHelper.getTouchId());
        String code = "1";
        String message = "成功";
        try {
            Long userId = opUser().getId();
            String customerId = opUser().getCustId();
            String superId = phoneService.getSuperIdByPhone(customerGroupId, phone);
            if (StringUtil.isEmpty(superId) && StringUtil.isNotEmpty(seaId)) {
                superId = phoneService.getSeaSuperIdByPhone(seaId, phone);
                // 查询公海线索所属的客群ID
                if (StringUtil.isEmpty(customerGroupId)) {
                    Map<String, Object> clueData = customerSeaService.selectClueInfo(seaId, phone, 2);
                    if (clueData != null) {
                        customerGroupId = String.valueOf(clueData.get("batch_id"));
                    }
                }
            }
            // 插入外呼日志表
            MarketResourceLogDTO dto = new MarketResourceLogDTO();
            // touchId使用呼叫中心callId来赋值,后期回调使用
            dto.setTouch_id(touchId);
            dto.setType_code("1");
            dto.setResname("voice");
            dto.setUser_id(userId);
            dto.setCust_id(customerId);
            dto.setSuperId(superId);
            dto.setCallSid(callId);
            dto.setCustomerGroupId(NumberConvertUtil.parseInt(customerGroupId));
            // 判断是否管理员进行的外呼
            if ("1".equals(opUser().getUserType())) {
                dto.setCallOwner(2);
            } else {
                dto.setCallOwner(1);
            }
            // 主叫成功
            if ("1".equals(callStatus)) {
                dto.setStatus(1001);
            } else if ("2".equals(callStatus)) {
                dto.setStatus(1002);
            } else {
                LOG.warn("保存自动外呼callStatus状态未知:" + callStatus + ",默认为通话失败");
                dto.setStatus(1002);
            }
            // 当前登录人所属的职场ID
            dto.setCugId(opUser().getJobMarketId());
            dto.setMarketTaskId(StringUtil.isNotEmpty(marketTaskId) ? marketTaskId : "");
            dto.setCustomerSeaId(StringUtil.isNotEmpty(seaId) ? seaId : "");
            marketResourceService.insertLogV3(dto);
            map.put("superId", superId);
            map.put("tranOrderId", touchId);
            map.put("code", code);
            map.put("message", message);
            json.put("data", map);
        } catch (Exception e) {
            LOG.error("保存自动外呼异常:", e);
        } finally {
            map.put("tranOrderId", touchId);
            map.put("code", code);
            map.put("message", message);
            json.put("data", map);
        }
        LOG.info("保存自动外呼phone:" + phone + ",customerGroupId:" + customerGroupId + ",callId:" + callId + ",callStatus:" + callStatus + "返回数据:" + json.toJSONString());
        return json.toJSONString();
    }

    @RequestMapping(value = "/getPhoneBySuperId", method = RequestMethod.GET)
    @ResponseBody
    public String getPhoneBySuperId(String superId) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        String phone = phoneService.getPhoneBySuperId(superId);
        map.put("phone", phone);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 根据客户群和身份ID获取手机号
     *
     * @param customerGroupId
     * @param superId
     * @return
     */
    @RequestMapping(value = "/getSuperInfo", method = RequestMethod.GET)
    @ResponseBody
    public String getSuperInfo(Integer customerGroupId, String superId) {
        JSONObject json = new JSONObject();
        Map<String, Object> data = marketResourceService.getSuperInfoV3(opUser().getCustId(), customerGroupId, superId);
        json.put("data", data);
        return json.toJSONString();
    }

    /**
     * 用途:用于呼叫中心类型的手动外呼点击致电后,  前端调用后台接口，预先生成一条(结算中状态)通话记录，
     * 并且返回触达唯一标识 tranOrderId, 用于后期前端获取到呼叫中心回调信息后，更新我方通话记录的状态和通话备注使用
     *
     * @param superId
     * @param customerGroupId
     * @return
     */
    @RequestMapping(value = "/useCallCenter0", method = RequestMethod.POST)
    @ResponseBody
    public String useCallCenter(String superId, String customerGroupId, String marketTaskId, String seaId) {
        Map<Object, Object> map = new HashMap<Object, Object>();
        JSONObject json = new JSONObject();
        Long userId = opUser().getId();
        String customerId = opUser().getCustId();

        String code = "1";
        String message = "成功";
        String touchId = opUser().getId() + Long.toString(IDHelper.getTouchId());
        try {
            if (StringUtil.isNotEmpty(superId)) {
                String[] superIds = superId.split(",");
                // 插入外呼日志表
                MarketResourceLogDTO dto;
                for (String id : superIds) {
                    dto = new MarketResourceLogDTO();
                    dto.setTouch_id(touchId);
                    dto.setType_code("1");
                    dto.setResname("voice");
                    dto.setUser_id(userId);
                    dto.setCust_id(customerId);
                    dto.setSuperId(id);
                    // 查询公海线索所属的客群ID
                    if (StringUtil.isEmpty(customerGroupId)) {
                        Map<String, Object> clueData = customerSeaService.selectClueInfo(seaId, superId, 1);
                        if (clueData != null) {
                            customerGroupId = String.valueOf(clueData.get("batch_id"));
                        }
                    }
                    dto.setCustomerGroupId(NumberConvertUtil.parseInt(customerGroupId));
                    // 判断是否管理员进行的外呼
                    if ("1".equals(opUser().getUserType())) {
                        dto.setCallOwner(2);
                    } else {
                        dto.setCallOwner(1);
                    }
                    // 当前登录人所属的职场ID
                    dto.setCugId(opUser().getJobMarketId());
                    dto.setMarketTaskId(StringUtil.isNotEmpty(marketTaskId) ? marketTaskId : "");
                    dto.setCustomerSeaId(StringUtil.isNotEmpty(seaId) ? seaId : "");
                    marketResourceService.insertLogV3(dto);
                }
            }
        } catch (Exception e) {
            LOG.error("保存手动外呼通话记录异常,", e);
        }

        map.put("tranOrderId", touchId);
        map.put("code", code);
        map.put("message", message);
        json.put("data", map);
        return json.toJSONString();
    }


    @PostMapping(value = "/crmtouchlog")
    @ResponseBody
    public String saveVoiceLog(@RequestBody JSONObject param) {
        Map<Object, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        Long userId = opUser().getId();
        String customerId = opUser().getCustId();
        String objId = param.getString("objId");
        String superId = param.getString("superId");
        String field = param.getString("field");
        String objType = param.getString("objType");
        Map data = null;
        // 线索私海
        if ("1".equals(objType)) {
            data = crmLeadsService.queryById(NumberConvertUtil.parseInt(objId));
        } else if ("2".equals(objType)) {
            //客户私海
            data = crmCustomerService.queryById(NumberConvertUtil.parseInt(objId));
        } else if ("3".equals(objType)) {
            // 联系人
            data = crmContactsService.queryById(NumberConvertUtil.parseInt(objId));
        }
        // 反查uid
        if (StringUtil.isEmpty(superId) && data != null && data.get(field) != null) {
            superId = phoneService.pnu(String.valueOf(data.get(field)));
        }
        if (StringUtil.isEmpty(superId)) {
            map.put("code", -1);
            map.put("message", "superId必填");
            json.put("data", map);
            return json.toJSONString();
        }
        String customerGroupId = param.getString("customerGroupId");
        String marketTaskId = param.getString("marketTaskId");
        String seaId = param.getString("seaId");
        String callId = param.getString("callId");
        Integer callStatus = param.getInteger("callStatus");

        String code = "1";
        String message = "成功";
        String touchId = opUser().getId() + IDHelper.getTouchId().toString();
        try {
            if (StringUtil.isNotEmpty(superId)) {
                // 插入外呼日志表
                MarketResourceLogDTO dto = new MarketResourceLogDTO();
                dto.setTouch_id(touchId);
                dto.setType_code("1");
                dto.setResname("voice");
                dto.setUser_id(userId);
                dto.setCust_id(customerId);
                dto.setSuperId(superId);
                // 查询公海线索所属的客群ID
                if (StringUtil.isEmpty(customerGroupId) && StringUtil.isNotEmpty(seaId)) {
                    Map<String, Object> clueData = customerSeaService.selectClueInfo(seaId, superId, 1);
                    if (clueData != null) {
                        customerGroupId = String.valueOf(clueData.get("batch_id"));
                    }
                }
                if (StringUtil.isNotEmpty(customerGroupId)) {
                    dto.setCustomerGroupId(NumberConvertUtil.parseInt(customerGroupId));
                }
                // 判断是否管理员进行的外呼
                if ("1".equals(opUser().getUserType())) {
                    dto.setCallOwner(2);
                } else {
                    dto.setCallOwner(1);
                }
                // 当前登录人所属的职场ID
                dto.setCugId(opUser().getJobMarketId());
                dto.setMarketTaskId(StringUtil.isNotEmpty(marketTaskId) ? marketTaskId : "");
                dto.setCustomerSeaId(StringUtil.isNotEmpty(seaId) ? seaId : "");
                dto.setObjType(objId);
                dto.setCallSid(callId);
                dto.setStatus(callStatus);
                // 保存通话记录
                marketResourceService.insertCrmTouchLog(dto);
                LkCrmAdminRecordEntity record = new LkCrmAdminRecordEntity();
                record.setTypesId(objId);
                record.setContent("网络电话");
                record.setCategory("打电话");
                record.setIsEvent(0);
                // 致电时间
                if (param.get("callTime") != null) {
                    Date callTime = param.getDate("callTime");
                    record.setNextTime(callTime);
                }
                // 添加更新记录
                if ("1".equals(objType)) {
                    crmLeadsService.addRecord(record);
                } else if ("2".equals(objType)) {
                    //客户私海
                    crmCustomerService.addRecord(record);
                } else if ("3".equals(objType)) {
                    // 联系人
                    crmContactsService.addRecord(record);
                }
                // 更新致电次数
                /*data = new HashMap();
                data.put("batch_id","22a6dd6cc3ed4010963d1bcd9e002ead");*/
                adminFieldService.saveCallSmsCount(String.valueOf(data.get("batch_id")), NumberConvertUtil.parseInt(objType), 1, 1);
            }
        } catch (Exception e) {
            LOG.error("保存手动外呼通话记录异常,", e);
        }

        map.put("tranOrderId", touchId);
        map.put("code", code);
        map.put("message", message);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * @param type
     * @param templateId
     * @param superidlist
     * @param customerGroupId
     * @param id
     * @param backPhone
     * @param callId
     * @param marketTaskId
     * @return
     */
    @RequestMapping(value = "/use", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String useMarketResource(String type, String templateId, String superidlist, String customerGroupId, String id,
                                    String backPhone, String callId, String marketTaskId,
                                    String seaId) {
        // type资源类型（ 1.voice 2.SMS 3.email）
        LoginUser lu = opUser();
        Long userId = lu.getId();
        String custId = lu.getCustId();
        // 电话日志表唯一标识
        String tranOrderId = opUser().getId() + Long.toString(IDHelper.getTouchId());

        Map<Object, Object> map = new HashMap<Object, Object>();
        JSONObject json = new JSONObject();

        String code = "1";
        String message = "调用为成功";
        String superId;
        // 发送短信
        if ("2".equals(type)) {
            // 判断余额
            boolean amountStatus = marketResourceService.judRemainAmount(custId);
            if (!amountStatus) {
                map.put("message", "余额不足");
                map.put("code", "1003");
                json.put("data", map);
                return json.toJSONString();
            }

            JSONObject jsonSuperId = JSONObject.parseObject(superidlist);
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
                    list = marketTaskService.listMarketTaskData(opUser(), marketTaskId, null, null, "", "", null, "", "", "");
                } else if (StringUtil.isNotEmpty(customerGroupId)) {
                    list = customGroupService.getCustomGroupDataV3(opUser(), customerGroupId, null, null, "", "", null, "", "", "");
                }

                if (list != null && list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) != null) {
                            superIds.add(String.valueOf(list.get(i).get("id")));
                        }
                    }
                } else {
                    map.put("message", "请选择要发送的数据");
                    map.put("code", "1003");
                    json.put("data", map);
                    return json.toJSONString();
                }
            }
            Integer num = superIds.size();

            // 判断客户余额是否足够用于此次发送的短信数量
            boolean flag1 = marketResourceService.checkAmount0(custId, num, templateId);
            if (!flag1) {
                map.put("message", "余额不足");
                map.put("code", 1003);
                json.put("data", map);
                return json.toJSONString();
            } else {
                LOG.info("短信发送custId:" + custId + ",custGroupId:" + customerGroupId + ",seaId:" + seaId + ",userId:" + userId + ",templateId:" + templateId + ",superIds:" + superIds);
                boolean sendStatus = false;
                if (superIds.size() > 0) {
                    List<String> superList = new ArrayList<>(superIds);
                    String batchNumber = smsService.sendSmsToQueue(custId, customerGroupId, superList, templateId, String.valueOf(lu.getId()), marketTaskId);
                    LOG.info("发送短信customerGroupId:" + customerGroupId + ",templateId:" + templateId + ",结果:" + batchNumber);
                    if (StringUtil.isNotEmpty(batchNumber)) {
                        sendStatus = true;
                    }
                    if (sendStatus) {
                        code = "1001";
                        message = "短信发送成功";
                    } else {
                        code = "2001";
                        message = "短信发送失败";
                    }
                }
            }
        } else if ("1".equals(type)) {
            superId = superidlist;
            // 查询用户是否设置主叫号码
            String workNum = marketResourceService.selectPhoneSet(userId);
            if (null == workNum || "".equals(workNum) || "000".equals(workNum)) {
                map.put("message", "未设主叫置电话或者主叫电话状态异常");
                map.put("code", "1004");
                json.put("data", map);
                return json.toJSONString();
            }

            String resourceId = null;
            try {
                resourceId = seatsService.checkSeatConfigStatus(String.valueOf(opUser().getId()), custId);
            } catch (Exception e) {
                LOG.error("获取用户配置的呼叫渠道ID异常,", e);
            }
            // 查询企业是否设置双向呼叫外显号码
            String apparentNumber = customerService.getCustomerApparentNumber(opUser().getCustId(), "", resourceId);
            if (StringUtil.isEmpty(apparentNumber)) {
                apparentNumber = customerService.getCustomerApparentNumber(opUser().getCustId(), "");
                if (StringUtil.isEmpty(apparentNumber)) {
                    // 穿透查询一次之前配置的外显号
                    apparentNumber = marketResourceService.selectCustCallBackApparentNumber(opUser().getCustId());
                }
            }
            if (StringUtil.isEmpty(apparentNumber)) {
                map.put("message", "未申请外显号码");
                map.put("code", "1006");
                json.put("data", map);
                return json.toJSONString();
            }

            // 判断是余额是否充足
            boolean juddeg = marketResourceService.judRemainAmount(custId);
            if (juddeg == false) {
                map.put("message", "余额不足");
                map.put("code", "1003");
                json.put("data", map);
                return json.toJSONString();
            }
            boolean success = false;
            String phone;
            if (StringUtil.isNotEmpty(superId)) {
                phone = phoneService.getPhoneBySuperId(superId);
            } else if (StringUtil.isNotEmpty(backPhone)) {
                // 处理备用手机号打电话
                phone = backPhone;
            } else {
                LOG.warn("被叫为空,superId:" + superId + ",backPhone:" + backPhone);
                map.put("message", "被叫为空");
                map.put("code", 1002);
                json.put("data", map);
                return json.toJSONString();
            }
            CallBackParam callBackParams = new CallBackParam();
            // 主叫号码
            callBackParams.setSrc(workNum);
            callBackParams.setDst(phone);
            callBackParams.setSrcclid(apparentNumber);
            callBackParams.setDstclid(apparentNumber);
            callBackParams.setCustomParm(tranOrderId + "_" + customerGroupId);
            // {"statusCode":"0","statusMsg":"提交成功","requestId":"20180835472103517559193600402"}
            String callBackResult = callCenterService.handleCallBack0(callBackParams, opUser().getCustId(), String.valueOf(opUser().getId()));
            LogUtil.info("调用api双向回呼接口返回:" + callBackResult);
            // 更新通话次数(客戶群和公海的)
            if (StringUtil.isNotEmpty(customerGroupId)) {
                LOG.info("客户群id是：" + customerGroupId);
                marketResourceService.updateCallCountV2(customerGroupId, superId);
            } else {
                //根据公海id还有supplier查询客户群id
                Map<String, Object> customerSeaBysupplierId = customerSeaService.getCustomerSeaBysupplierId(seaId, superId);
                if (customerSeaBysupplierId != null) {
                    customerGroupId = String.valueOf(customerSeaBysupplierId.get("batch_id"));
                    LOG.info("公海详情的用户群id是：" + customerGroupId);
                }
            }
            if (StringUtil.isNotEmpty(seaId)) {
                LOG.info("公海id是：" + seaId);
                marketResourceService.updateSeaCallCount(seaId, superId);
            }
            success = false;
            JSONObject jsonObject = null;
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

            MarketResourceLogDTO dto = new MarketResourceLogDTO();
            dto.setTouch_id(tranOrderId);
            dto.setType_code("1");
            dto.setResname("voice");
            dto.setUser_id(userId);
            dto.setCust_id(custId);
            dto.setSuperId(superId);
            dto.setRemark("");
            // 当前登录人所属的职场ID
            dto.setCugId(opUser().getJobMarketId());
            if (StringUtil.isNotEmpty(customerGroupId)) {
                dto.setCustomerGroupId(Integer.parseInt(customerGroupId));
            }
            // 判断是否管理员进行的外呼
            if ("1".equals(opUser().getUserType())) {
                dto.setCallOwner(2);
            } else {
                dto.setCallOwner(1);
            }
            // 营销任务Id
            dto.setMarketTaskId(StringUtil.isNotEmpty(marketTaskId) ? marketTaskId : "");
            // 公海ID
            dto.setCustomerSeaId(StringUtil.isNotEmpty(seaId) ? seaId : "");
            // 执行成功
            if (success) {
                // 唯一请求ID
                String requestId = jsonObject.getString("requestId");
                dto.setCallSid(requestId);
                // 主叫成功
                dto.setStatus(1001);
                code = "10000";
                message = "电话已经拨打";
            } else {
                LOG.warn("请求发送双向呼叫失败,返回数据:" + jsonObject);
                // 主叫失败
                dto.setStatus(1002);
            }
            marketResourceService.insertLogV3(dto);
        }
        map.put("tranOrderId", tranOrderId);
        map.put("code", code);
        map.put("message", message);
        json.put("data", map);
        return json.toJSONString();

    }


    /**
     * CRM双呼接口
     *
     * @param param
     * @return
     */
    @PostMapping(value = "/crm/call2way")
    @ResponseBody
    public String crmCall2Way(@RequestBody JSONObject param) {
        LoginUser lu = opUser();
        Long userId = lu.getId();
        String custId = lu.getCustId();
        // type资源类型（ 1.voice 2.SMS 3.email）
        String type = param.getString("type");
        String touchId = opUser().getId() + IDHelper.getTouchId().toString();
        Map<Object, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        String code = "1";
        String message = "成功";
        // 判断是余额是否充足
        boolean amount = marketResourceService.judRemainAmount(custId);
        if (!amount) {
            map.put("message", "余额不足");
            map.put("code", 1003);
            json.put("data", map);
            return json.toJSONString();
        }
        // 拨打电话
        if ("1".equals(type)) {
            String customerGroupId = param.getString("customerGroupId");
            String marketTaskId = param.getString("marketTaskId");
            String seaId = param.getString("seaId");

            String objId = param.getString("objId");
            String superId = param.getString("superId");
            String field = param.getString("field");
            String objType = param.getString("objType");
            Map data = null;
            // 线索私海
            if ("1".equals(objType)) {
                data = crmLeadsService.queryById(NumberConvertUtil.parseInt(objId));
            } else if ("2".equals(objType)) {
                // 客户私海
                data = crmCustomerService.queryById(NumberConvertUtil.parseInt(objId));
            } else if ("3".equals(objType)) {
                // 联系人
                data = crmContactsService.queryById(NumberConvertUtil.parseInt(objId));
            }
            // 反查uid
            if (StringUtil.isEmpty(superId) && data != null && data.get(field) != null) {
                try {
                    superId = phoneService.pnu(String.valueOf(data.get(field)));
                } catch (Exception e) {
                    LOG.error("通过手机号获取uid异常", e);
                }
            }
            if (StringUtil.isEmpty(superId)) {
                map.put("code", -1);
                map.put("message", "superId必填");
                json.put("data", map);
                return json.toJSONString();
            }
            // 查询用户是否设置主叫号码
            String workNum = marketResourceService.selectPhoneSet(userId);
            if (null == workNum || "".equals(workNum) || "000".equals(workNum)) {
                map.put("message", "未设主叫置电话或者主叫电话状态异常");
                map.put("code", 1004);
                json.put("data", map);
                return json.toJSONString();
            }

            String resourceId = null;
            try {
                resourceId = seatsService.checkSeatConfigStatus(String.valueOf(opUser().getId()), custId);
            } catch (Exception e) {
                LOG.error("获取用户配置的呼叫渠道ID异常,", e);
            }
            // 查询企业是否设置双向呼叫外显号码
            String apparentNumber = customerService.getCustomerApparentNumber(opUser().getCustId(), "", resourceId);
            if (StringUtil.isEmpty(apparentNumber)) {
                apparentNumber = customerService.getCustomerApparentNumber(opUser().getCustId(), "");
                if (StringUtil.isEmpty(apparentNumber)) {
                    // 穿透查询一次之前配置的外显号
                    apparentNumber = marketResourceService.selectCustCallBackApparentNumber(opUser().getCustId());
                }
            }
            if (StringUtil.isEmpty(apparentNumber)) {
                map.put("message", "未申请外显号码");
                map.put("code", 1006);
                json.put("data", map);
                return json.toJSONString();
            }
            boolean success;
            // 获取手机号
            String phone = phoneService.upn(superId);
            if (StringUtil.isEmpty(phone)) {
                LOG.warn("被叫为空,superId:" + superId);
                map.put("message", "被叫为空");
                map.put("code", 1002);
                json.put("data", map);
                return json.toJSONString();
            }

            CallBackParam callBackParams = new CallBackParam();
            // 主叫号码
            callBackParams.setSrc(workNum);
            callBackParams.setDst(phone);
            callBackParams.setSrcclid(apparentNumber);
            callBackParams.setDstclid(apparentNumber);
            callBackParams.setCustomParm(touchId + "_");
            // {"statusCode":"0","statusMsg":"提交成功","requestId":"20180835472103517559193600402"}
            String callBackResult = callCenterService.handleCallBack0(callBackParams, opUser().getCustId(), String.valueOf(opUser().getId()));
            LogUtil.info("调用api双向回呼接口返回:" + callBackResult);
            success = false;
            JSONObject jsonObject = null;
            if (StringUtil.isNotEmpty(callBackResult)) {
                jsonObject = JSON.parseObject(callBackResult);
                // 发送双向呼叫请求成功
                if ("0".equals(jsonObject.getString("statusCode"))) {
                    success = true;
                } else {
                    message = jsonObject.getString("statusMsg");
                }
            }

            MarketResourceLogDTO dto = new MarketResourceLogDTO();
            dto.setTouch_id(touchId);
            dto.setType_code("1");
            dto.setResname("voice");
            dto.setUser_id(userId);
            dto.setCust_id(custId);
            dto.setSuperId(superId);
            dto.setRemark("");
            // 当前登录人所属的职场ID
            dto.setCugId(opUser().getJobMarketId());
            if (StringUtil.isNotEmpty(customerGroupId)) {
                dto.setCustomerGroupId(Integer.parseInt(customerGroupId));
            }
            // 判断是否管理员进行的外呼
            if ("1".equals(opUser().getUserType())) {
                dto.setCallOwner(2);
            } else {
                dto.setCallOwner(1);
            }
            // 营销任务Id
            dto.setMarketTaskId(StringUtil.isNotEmpty(marketTaskId) ? marketTaskId : "");
            // 公海ID
            dto.setCustomerSeaId(StringUtil.isNotEmpty(seaId) ? seaId : "");
            dto.setObjType(objId);
            // 执行成功
            if (success) {
                // 唯一请求ID
                String requestId = jsonObject.getString("requestId");
                dto.setCallSid(requestId);
                // 主叫成功
                dto.setStatus(1001);
                message = "电话已经拨打";
            } else {
                LOG.warn("请求发送双向呼叫失败,返回数据:" + jsonObject);
                // 主叫失败
                dto.setStatus(1002);
                code = "-1";
            }
            // 保存通话记录
            marketResourceService.insertCrmTouchLog(dto);
            LkCrmAdminRecordEntity record = new LkCrmAdminRecordEntity();
            record.setTypesId(objId);
            record.setContent("网络电话");
            record.setCategory("打电话");
            record.setIsEvent(0);
            // 致电时间
            if (param.get("callTime") != null) {
                Date callTime = param.getDate("callTime");
                record.setNextTime(callTime);
            }
            // 添加更新记录
            if ("1".equals(objType)) {
                crmLeadsService.addRecord(record);
            } else if ("2".equals(objType)) {
                //客户私海
                crmCustomerService.addRecord(record);
            } else if ("3".equals(objType)) {
                // 联系人
                crmContactsService.addRecord(record);
            }
            // 更新致电次数
            adminFieldService.saveCallSmsCount(String.valueOf(data.get("batch_id")), NumberConvertUtil.parseInt(objType), 1, 1);

        }
        map.put("tranOrderId", touchId);
        map.put("code", code);
        map.put("message", message);
        json.put("data", map);
        return json.toJSONString();

    }

    /**
     * 公海发送短信
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/usesms", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String useSMSMarketResource(@RequestBody CustomerSeaSmsSearch param) {
        String type = param.getType();
        String superidlist = param.getSuperidlist();
        String seaId = param.getSeaId();
        String marketTaskId = param.getMarketTaskId();
        String customerGroupId = param.getCustomerGroupId();
        String templateId = param.getTemplateId();
        String smsBatchName = param.getSmsBatchName();
        // type资源类型（ 1.voice 2.SMS 3.email）
        LoginUser lu = opUser();
        Long userId = lu.getId();
        String custId = lu.getCustId();
        // 电话日志表唯一标识
        String tranOrderId = opUser().getId() + Long.toString(IDHelper.getTouchId());

        Map<Object, Object> map = new HashMap<Object, Object>();
        JSONObject json = new JSONObject();

        String code = "1";
        String message = "调用为成功";
        String superId;
        // 发送短信
        if ("2".equals(type)) {
            // 判断余额
            boolean amountStatus = marketResourceService.judRemainAmount(custId);
            if (!amountStatus) {
                map.put("message", "余额不足");
                map.put("code", "1003");
                json.put("data", map);
                return json.toJSONString();
            }

            JSONObject jsonSuperId = JSONObject.parseObject(superidlist);
            Set<String> superIds = new HashSet<>();
            Set<Map<String, String>> seaSuperIds = new HashSet<>();
            // 部分发送
            if (jsonSuperId != null && jsonSuperId.size() > 0) {
                JSONArray superIdList = jsonSuperId.getJSONArray("data");
                JSONObject jsonItem;
                for (int i = 0; i < superIdList.size(); i++) {
                    jsonItem = superIdList.getJSONObject(i);
                    if (StringUtil.isNotEmpty(seaId)) {
                        HashMap<String, String> sea = new HashMap();
                        sea.put("supperid", jsonItem.getString("supperid"));
                        sea.put("customerGroupId", jsonItem.getString("customerGroupId"));
                        seaSuperIds.add(sea);
                    }
                }
            } else {
                // 全部发送客户群短信
                List<Map<String, Object>> list = null;
                if (StringUtil.isNotEmpty(seaId)) {
                    param.setUserId(opUser().getId());
                    param.setUserType(opUser().getUserType());
                    param.setUserGroupRole(opUser().getUserGroupRole());
                    param.setUserGroupId(opUser().getUserGroupId());
                    param.setCustId(opUser().getCustId());
                    list = customerSeaService.pagePrivateClueData(param).getData();
                    if (list.size() > 100000) {
                        map.put("message", "不能超过100000");
                        map.put("code", "1003");
                        json.put("data", map);
                        return json.toJSONString();
                    }
                } else if (StringUtil.isNotEmpty(marketTaskId)) {
                    list = marketTaskService.listMarketTaskData(opUser(), marketTaskId, null, null, "", "", null, "", "", "");
                } else if (StringUtil.isNotEmpty(customerGroupId)) {
                    list = customGroupService.getCustomGroupDataV3(opUser(), customerGroupId, null, null, "", "", null, "", "", "");
                }

                if (list != null && list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        if (list.get(i) != null) {
                            if (StringUtil.isNotEmpty(seaId)) {
                                Map<String, String> sea = new HashMap<>();
                                sea.put("supperid", String.valueOf(list.get(i).get("id")));
                                sea.put("customerGroupId", String.valueOf(list.get(i).get("batch_id")));
                                seaSuperIds.add(sea);
                            } else {
                                superIds.add(String.valueOf(list.get(i).get("id")));
                            }
                        }
                    }
                } else {
                    map.put("message", "请选择要发送的数据");
                    map.put("code", "1003");
                    json.put("data", map);
                    return json.toJSONString();
                }
            }
            Integer num = superIds.size();
            if (num == null || num == 0) {
                num = seaSuperIds.size();
            }
            // 判断客户余额是否足够用于此次发送的短信数量
            boolean flag1 = marketResourceService.checkAmount0(custId, num, templateId);
            if (!flag1) {
                map.put("message", "余额不足");
                map.put("code", 1003);
                json.put("data", map);
                return json.toJSONString();
            } else {
                LOG.info("短信发送custId:" + custId + ",custGroupId:" + customerGroupId + ",seaId:" + seaId + ",userId:" + userId + ",templateId:" + templateId + ",superIds:" + superIds);
                boolean sendStatus = false;
                if (superIds.size() > 0) {
                    List<String> superList = new ArrayList<>(superIds);
                    String batchNumber = smsService.sendSmsToQueue(custId, customerGroupId, superList, templateId, String.valueOf(lu.getId()), marketTaskId);
                    LOG.info("发送短信customerGroupId:" + customerGroupId + ",templateId:" + templateId + ",结果:" + batchNumber);
                    if (StringUtil.isNotEmpty(batchNumber)) {
                        sendStatus = true;
                    }
                    if (sendStatus) {
                        code = "1001";
                        message = "短信发送成功";
                    } else {
                        code = "2001";
                        message = "短信发送失败";
                    }
                } else if (seaSuperIds.size() > 0) {
                    List<Map<String, String>> list = new ArrayList<>(seaSuperIds);
                    String batchNumber = smsService.sendSeaSmsToQueue(custId, list, templateId, String.valueOf(lu.getId()), seaId, smsBatchName);
                    LOG.info("发送短信seaId:" + seaId + ",templateId:" + templateId + ",结果:" + batchNumber);
                    if (StringUtil.isNotEmpty(batchNumber)) {
                        sendStatus = true;
                    }
                    if (sendStatus) {
                        code = "1001";
                        message = "短信发送成功";
                    } else {
                        code = "2001";
                        message = "短信发送失败";
                    }
                }
            }
        }
        map.put("tranOrderId", tranOrderId);
        map.put("code", code);
        map.put("message", message);
        json.put("data", map);
        return json.toJSONString();

    }

    /**
     * 不再使用
     *
     * @param type
     * @param templateId
     * @param superidlist
     * @param customerGroupId
     * @param id
     * @param backPhone
     * @return
     */
    @RequestMapping(value = "/use0", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String useMarketResource0(String type, String templateId, String superidlist, String customerGroupId, String id, String backPhone) {
        // type资源类型（ 1.voice 2.SMS 3.email）
        LoginUser lu = opUser();
        Long userId = lu.getId();
        String custId = lu.getCustId();
        // 电话日志表唯一标识
        String tranOrderId = opUser().getId() + Long.toString(IDHelper.getTouchId());

        Map<Object, Object> map = new HashMap<Object, Object>();
        JSONObject json = new JSONObject();

        String code = "1";
        String message = "调用为成功";
        String superId = "";
        // 发送短信
        if ("2".equals(type)) {

            JSONObject jsonsuperid = JSONObject.parseObject(superidlist);
            // 获取短信内容
            String cust_id = opUser().getCustId();
            //判断是否全部发送
            JSONArray supidList = null;
            List<Map<String, Object>> allList = null;
            if (null != customerGroupId && !"".equals(customerGroupId)) {
                String user_id = opUser().getId().toString();
                allList = marketResourceService.getCustomerList(customerGroupId, cust_id, user_id, lu.getUserType(), id);
            } else {
                supidList = jsonsuperid.getJSONArray("data");
            }

            int size = 0;
            if (null != allList) {
                size = allList.size();
            } else {
                size = supidList.size();
            }
            boolean flag1 = marketResourceService.checkAmount(cust_id, size, "1001", "1", "001");
            if (!flag1) {
                code = "1003";
                message = "余额不足支付";
            } else {
                Long batch_number = 0L;
                String user_id = opUser().getId().toString();
                String enpterprise_name = opUser().getEnterpriseName();
                for (int i = 0; i < size; i++) {
                    if (batch_number == 0L) {
                        batch_number = IDHelper.getTransactionId();
                    }
                    if (null != allList) {
                        superId = allList.get(i).get("superid").toString();
                    } else {
                        JSONObject jsonItem = supidList.getJSONObject(i);
                        superId = jsonItem.getString("superid");
                    }
                    SmsAction demo = new SmsAction();
                    // 获取手机号
                    MultiValueMap<String, Object> urlVariables = new LinkedMultiValueMap<String, Object>();
                    urlVariables.add("interfaceID", "BQ0018");
                    urlVariables.add("superid", superId);
                    // 客户身份id
                    String result =
                            customGroupService.getMicroscopicPhone(superId,
                                    urlVariables);

                    if (result == null || "".equals(result)) {
                        continue;
                    }

                    JSONObject jsonPhone = JSONObject.parseObject(result);

                    String isSuccess = jsonPhone.getString("isSuccess");
                    if (null != isSuccess && "0".equals(isSuccess)) {
                        continue;
                    }

                    String phone =
                            JSONObject.parseObject(jsonPhone.getString("ids")).getString("cp").replaceAll("\"", "").replace("[",
                                    "")
                                    .replace("]", "");

                    if (phone == null || "".equals(phone)) {
                        continue;
                    }

                    //String phone = "13331177069";

                    String sendMessage = marketResourceService.getSendMessage(templateId);
                    String flag = marketResourceService.updateResource("3", "", cust_id, "", "", "", "", "", "", 0L, 0);
                    if (flag.equals("1")) {
                        code = "1003";
                        message = "余额不足支付";
                        break;
                    } else {
                        // 发送短信
                        String returnStr = demo.send("10287", phone, sendMessage);

                        JSONObject jsonObj = JSONObject.parseObject(returnStr);
                        // 得到指定json key对象的value对象
                        JSONObject personObj = jsonObj.getJSONObject("SmsResp");
                        // 获取之对象的所有属性
                        // 成功发送短信
                        if ("0000".equals(personObj.getString("respCode"))) {
                            code = "1001";
                            message = "短信发送成功";
                        } else {
                            code = "2001";
                            message = "短信发送失败";
                        }
                        // 记录日志
                        marketResourceService.updateResource("0", code, cust_id, user_id, message,
                                sendMessage, superId, templateId, enpterprise_name, batch_number, 0);
                    }
                }
                //添加订单更新账户余额
                marketResourceService.updateResource("1", code, cust_id, user_id, message, "", superId, templateId, enpterprise_name, batch_number, size);
            }

        }
        if ("1".equals(type)) {
            superId = superidlist;
            // 查询用户是否设置主叫号码
            String workNum = marketResourceService.selectPhoneSet(userId);
            if (null == workNum || "".equals(workNum) || "000".equals(workNum)) {
                map.put("message", "未设主叫置电话或者主叫电话状态异常");
                map.put("code", "1004");
                json.put("data", map);
                return json.toJSONString();
            }

            // 查询企业是否设置双向呼叫外显号码
            String apparentNumber = customerService.getCustomerApparentNumber(opUser().getCustId(), "", "");
            if (StringUtil.isEmpty(apparentNumber)) {
                // 穿透查询一次之前配置的外显号
                apparentNumber = marketResourceService.selectCustCallBackApparentNumber(opUser().getCustId());
            }
            if (StringUtil.isEmpty(apparentNumber)) {
                map.put("message", "未申请外显号码");
                map.put("code", "1006");
                json.put("data", map);
                return json.toJSONString();
            }

            // 判断是余额是否充足
            boolean juddeg = marketResourceService.judRemainAmount(custId);
            if (juddeg == false) {
                map.put("message", "余额不足");
                map.put("code", "1003");
                json.put("data", map);
                return json.toJSONString();
            }
            boolean success = false;
            JSONObject jsonObject = null;
            try {
                CallBackParam callBackParams = new CallBackParam();
                // 主叫号码
                callBackParams.setSrc(workNum);
                String phone;
                if (StringUtil.isNotEmpty(superId)) {
                    phone = phoneService.getPhoneBySuperId(superId);
                } else if (StringUtil.isNotEmpty(superId)) {
                    // 处理备用手机号打电话
                    phone = backPhone;
                } else {
                    LOG.warn("被叫为空,superId:" + superId + ",backPhone:" + backPhone);
                    map.put("message", "被叫为空");
                    map.put("code", 1002);
                    json.put("data", map);
                    return json.toJSONString();
                }

                callBackParams.setDst(phone);
                callBackParams.setSrcclid(apparentNumber);
                callBackParams.setDstclid(apparentNumber);
                callBackParams.setCustomParm(tranOrderId + "_" + customerGroupId);

                // {"statusCode":"0","statusMsg":"提交成功","requestId":"20180835472103517559193600402"}
                String callBackResult = callCenterService.handleCallBack(callBackParams, opUser().getCustId());
                // 更新通话次数
                marketResourceService.updateCallCountV2(customerGroupId, superId);
                LogUtil.info("调用api双向回呼接口返回:" + callBackResult);
                success = false;
                jsonObject = null;
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
            } catch (RestClientException e) {
                LOG.error("调用api双向回呼接口失败", e);
            }
            // 执行成功
            if (success) {
                // 唯一请求ID
                String requestId = jsonObject.getString("requestId");
                // 插入外呼日志表
                MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
                marketResourceLogDTO.setTouch_id(tranOrderId);
                marketResourceLogDTO.setType_code("1");
                marketResourceLogDTO.setResname("voice");
                marketResourceLogDTO.setUser_id(userId);
                marketResourceLogDTO.setCust_id(custId);
                marketResourceLogDTO.setSuperId(superId);
                marketResourceLogDTO.setCallSid(requestId);
                marketResourceLogDTO.setRemark("");
                if (StringUtil.isNotEmpty(customerGroupId)) {
                    marketResourceLogDTO.setCustomerGroupId(Integer.parseInt(customerGroupId));
                }
                // 判断是否管理员进行的外呼
                if ("1".equals(opUser().getUserType())) {
                    marketResourceLogDTO.setCallOwner(2);
                } else {
                    marketResourceLogDTO.setCallOwner(1);
                }

                // 主叫成功
                marketResourceLogDTO.setStatus(1001);
                //marketResourceService.insertLog(marketResourceLogDTO);
                marketResourceService.insertLogV3(marketResourceLogDTO);
                code = "10000";
                message = "电话已经拨打";
            } else {
                // 异常返回输出错误码和错误信息
                LOG.error("请求发送双向呼叫失败,返回数据:" + jsonObject);
                // 插入外呼日志表
                MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
                marketResourceLogDTO.setTouch_id(tranOrderId);
                marketResourceLogDTO.setType_code("1");
                marketResourceLogDTO.setResname("voice");
                marketResourceLogDTO.setUser_id(userId);
                marketResourceLogDTO.setCust_id(custId);
                marketResourceLogDTO.setSuperId(superId);
                marketResourceLogDTO.setRemark("");
                if (StringUtil.isNotEmpty(customerGroupId)) {
                    marketResourceLogDTO.setCustomerGroupId(Integer.parseInt(customerGroupId));
                }
                // 判断是否管理员进行的外呼
                if ("1".equals(opUser().getUserType())) {
                    marketResourceLogDTO.setCallOwner(2);
                } else {
                    marketResourceLogDTO.setCallOwner(1);
                }
                // 主叫失败
                marketResourceLogDTO.setStatus(1002);
                //marketResourceService.insertLog(marketResourceLogDTO);
                marketResourceService.insertLogV3(marketResourceLogDTO);
            }

        }
        map.put("tranOrderId", tranOrderId);
        map.put("code", code);
        map.put("message", message);
        json.put("data", map);
        return json.toJSONString();

    }

    @RequestMapping(value = "/callBack")
    @ResponseBody
    @CacheAnnotation
    public void callBack(HttpServletRequest request, HttpServletResponse response) throws IOException {

        request.setCharacterEncoding("utf8");
        BufferedReader br = request.getReader();
        String str, wholeStr = "";
        while ((str = br.readLine()) != null) {
            wholeStr += str;
        }
        try {
            Map<String, Map<String, String>> returnMap = parserXml(wholeStr);
            System.out.println("---------------外呼回调返回的内容开始---------------");
            System.out.println("--------wholeStr-------" + wholeStr);
            System.out.println("---------------外呼回调返回的内容开始---------------");
            Map<String, String> mapCallerCdr = returnMap.get("CallerCdr");
            Map<String, String> mapCalledCdr = returnMap.get("CalledCdr");
            Map<String, String> maprecordurl = returnMap.get("recordurl");

            for (Map.Entry entry : mapCallerCdr.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
            System.out.println("-------------------------------");
            for (Map.Entry entry : mapCalledCdr.entrySet()) {
                System.out.println(entry.getKey() + ":" + entry.getValue());
            }
            System.out.println("---------------外呼回调返回的内容结束---------------");

            // 将返回信息入库

            boolean flag1 = marketResourceService.updateCallBack(mapCallerCdr, mapCalledCdr, maprecordurl, wholeStr);

            String Callerstarttime = mapCallerCdr.get("starttime");
            SimpleDateFormat df = new SimpleDateFormat("yyyyMMddhhmmss");
            SimpleDateFormat dfnew = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
            // 打电话记录到交易表并且扣款
            String userId = mapCallerCdr.get("userData").split("_")[1];
            // 唯一标识
            String tranOrderId = mapCallerCdr.get("userData").split("_")[2];
            // 唯一标识
            String cust_id = mapCallerCdr.get("userData").split("_")[3];
            // 通话时间

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
            Integer callTime = new Integer(CallerdurationBym);
            String code = "0";
            System.out.println("====向交易表添加数据开始：======" + code + "==cust_id:" + cust_id + "===callTime:" + callTime
                    + "==tranOrderId:" + tranOrderId + "==userId:" + userId);
            marketResourceService.addTransaction(code, cust_id, callTime, tranOrderId, userId);

        } catch (Exception e) {
            response.sendError(201);
            return;
        }
        String returnXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><Response><statuscode>000000</statuscode></Response>";
        response.setContentType("text/xml");
        response.setCharacterEncoding("UTF-8");
        response.setContentLength(returnXml.length());
        ;
        PrintWriter out = response.getWriter();
        out.print(returnXml);
        out.flush();

    }

    public Map<String, Map<String, String>> parserXml(String xml) {
        Map<String, Map<String, String>> returnMap = new HashMap<String, Map<String, String>>();
        Map<String, String> mapCallerCdr = new HashMap<String, String>();
        Map<String, String> mapCalledCdr = new HashMap<String, String>();
        Map<String, String> maprecordurl = new HashMap<String, String>();
        Document doc = null;
        try {

            doc = DocumentHelper.parseText(xml); // 将字符串转为XML

            Element rootElt = doc.getRootElement(); // 获取根节点
            System.out.println("根节点：" + rootElt.getName()); // 拿到根节点的名称

            Element employee = rootElt.element("CallerCdr");

            for (Iterator j = employee.elementIterator(); j.hasNext(); ) {
                Element node = (Element) j.next();
                // System.out.println(node.getName()+":"+node.getText());
                mapCallerCdr.put(node.getName(), node.getText());
            }
            Element employeed = rootElt.element("CalledCdr");

            for (Iterator j = employeed.elementIterator(); j.hasNext(); ) {
                Element node = (Element) j.next();
                // System.out.println(node.getName()+":"+node.getText());
                mapCalledCdr.put(node.getName(), node.getText());
            }
            Element recordurl = rootElt.element("recordurl");
            if (recordurl != null) {
                if (recordurl.getText() != null && !"".equals(recordurl.getText())) {
                    maprecordurl.put("recordurl", recordurl.getText());
                }
            }
            returnMap.put("CallerCdr", mapCallerCdr);
            returnMap.put("CalledCdr", mapCalledCdr);
            returnMap.put("recordurl", maprecordurl);

        } catch (DocumentException e) {
            e.printStackTrace();

        } catch (Exception e) {
            e.printStackTrace();

        }
        return returnMap;
    }

    /**
     * @param jsonO
     * @return String
     * @Title: updateVoiceLog
     * @Description:
     */
    @ResponseBody
    @RequestMapping(value = "/updateVoiceLog", method = RequestMethod.PUT)
    @CacheAnnotation
    public String updateVoiceLog(@RequestBody JSONObject jsonO) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String customerId = opUser().getCustId();
        Long userId = opUser().getId();
        JSONArray labelIdArray = jsonO.getJSONArray("labelIds");
        String remark = jsonO.getString("remark");
        String superId = jsonO.getString("superId");
        // t_touch_voice_LOG表的touch_id
        String touchId = jsonO.getString("touchId");
        String groupId = jsonO.getString("groupId");
        try {
            // 更新日志表
            marketResourceService.updateVoiceLog(touchId, remark);
            // 删除
            customerLabelService.deleteSuperLable(superId, groupId);
            // 新增
            if (labelIdArray != null || labelIdArray.size() != 0) {
                String labelId;
                String optionValue;
                for (int i = 0; i < labelIdArray.size(); i++) {
                    labelId = labelIdArray.get(i).toString();
                    // 插入客户自建标签表
                    // marketResourceService.inserCustomlabel(custId, userId,
                    // labelId,
                    // labelName);
                    // 插入客户购买资源用户标签表
                    String idSuper = Long.toString(IDHelper.getID());
                    customerLabelService.insertSuperLable(idSuper, superId, labelId, groupId);
                }
            }

            String voiceInfoId = jsonO.getString("voice_info_id");

            if (voiceInfoId == null || "".equals(voiceInfoId)) {
                voiceInfoId = IDHelper.getID().toString();
            }

            TouchInfoDTO touchInfoDTO = new TouchInfoDTO();
            touchInfoDTO.setVoice_info_id(voiceInfoId);
            touchInfoDTO.setCust_id(customerId);
            touchInfoDTO.setUser_id(userId.toString());
            touchInfoDTO.setCust_group_id(jsonO.getString("cust_group_id"));
            touchInfoDTO.setSuper_id(superId);
            touchInfoDTO.setSuper_name(jsonO.getString("super_name"));
            touchInfoDTO.setSuper_age(jsonO.getString("super_age"));
            touchInfoDTO.setSuper_sex(jsonO.getString("super_sex"));
            touchInfoDTO.setSuper_phone(jsonO.getString("super_telphone"));
            touchInfoDTO.setSuper_telphone(jsonO.getString("super_phone"));
            touchInfoDTO.setSuper_address_province_city(jsonO.getString("super_address_province_city"));

            touchInfoDTO.setSuper_address_street(jsonO.getString("super_address_street"));
            //新增打电话获取的用户信息
            marketResourceService.updateTouchInfo(touchInfoDTO);
            resultMap.put("code", "1");
            resultMap.put("_message", "更新成功");
        } catch (Exception e) {
            LOG.error("更新个人信息失败,", e);
            resultMap.put("code", "0");
            resultMap.put("_message", "保存个人信息失败,请重试");
        }
        return JSONObject.toJSONString(resultMap);
    }

    /**
     * 更新用户基本信息，更新用户自定义属性，更新日志表
     *
     * @param jsonO
     * @return String
     * @Title: updateVoiceLog/new
     * @Description:
     */
    @ResponseBody
    @RequestMapping(value = "/updateVoiceLog/new", method = {RequestMethod.PUT, RequestMethod.POST})
    public String updateVoiceLogNew(@RequestBody JSONObject jsonO) {
        Map<String, Object> resultMap = new HashMap<>();
        String customerId = opUser().getCustId();
        Long userId = opUser().getId();
        JSONArray labelIdArray = jsonO.getJSONArray("labelIds");
        String remark = jsonO.getString("remark");
        String superId = jsonO.getString("superId");
        // t_touch_voice_LOG表的touch_id
        String touchId = jsonO.getString("touchId");
        String groupId = jsonO.getString("groupId");
        String taskId = jsonO.getString("marketTaskId");

        Map<String, Object> labelData = new HashMap<>();
        try {
            // 更新日志表
            if (StringUtil.isNotEmpty(touchId)) {
                marketResourceService.updateVoiceLogV3(touchId, remark);
            }
            // 删除
            //marketResourceService.deleteSuperLable(superId, groupId);
            // 新增
            if (labelIdArray != null || labelIdArray.size() != 0) {
                String labelId;
                String optionValue;
                for (int i = 0; i < labelIdArray.size(); i++) {
                    labelId = labelIdArray.getJSONObject(i).getString("labelId");
                    optionValue = labelIdArray.getJSONObject(i).getString("optionValue");
                    // 插入客户购买资源用户标签表
                    String idSuper = Long.toString(IDHelper.getID());
                    //marketResourceService.insertSuperLable(idSuper, superId, labelId, groupId, optionValue);
                    labelData.put(labelId, optionValue);
                }
            }

            String voiceInfoId = jsonO.getString("voice_info_id");

            if (voiceInfoId == null || "".equals(voiceInfoId)) {
                voiceInfoId = IDHelper.getID().toString();
            }

            TouchInfoDTO touchInfoDTO = new TouchInfoDTO();
            touchInfoDTO.setVoice_info_id(voiceInfoId);
            touchInfoDTO.setCust_id(customerId);
            touchInfoDTO.setUser_id(userId.toString());
            touchInfoDTO.setCust_group_id(jsonO.getString("cust_group_id"));
            touchInfoDTO.setSuper_id(superId);
            touchInfoDTO.setSuper_name(jsonO.getString("super_name"));
            touchInfoDTO.setSuper_age(jsonO.getString("super_age"));
            touchInfoDTO.setSuper_sex(jsonO.getString("super_sex"));
            touchInfoDTO.setSuper_phone(jsonO.getString("super_telphone"));
            touchInfoDTO.setSuper_telphone(jsonO.getString("super_phone"));
            touchInfoDTO.setSuper_address_province_city(jsonO.getString("super_address_province_city"));
            touchInfoDTO.setMarket_task_id(taskId);
            touchInfoDTO.setSuper_address_street(jsonO.getString("super_address_street"));
            //新增打电话获取的用户信息
            //marketResourceService.updateTouchInfo(touchInfoDTO);

            marketResourceService.updateTouchInfoV3(touchInfoDTO);
            String luId = opUser().getId().toString();
            marketResourceService.updateTouchLabelDataV3(luId, groupId, superId, labelData, taskId);

            customerLabelService.saveSuperDataLog(superId, groupId, taskId, luId, labelData, "");

            resultMap.put("code", "0");
            resultMap.put("_message", "更新成功");
        } catch (Exception e) {
            LOG.error("更新个人信息失败,", e);
            resultMap.put("code", "1");
            resultMap.put("_message", "保存个人信息失败,请重试");
        }
        return JSONObject.toJSONString(resultMap);
    }

    /**
     * 首页信息
     *
     * @return
     */
    @RequestMapping(value = "/getMarketResourceInfo", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String getMarketResourceInfo() {
        String cust_id = opUser().getCustId();
        String user_id = opUser().getId().toString();
        return marketResourceService.getMarketResourceInfoV1(opUser().getUserType(), cust_id, user_id);
    }

    @RequestMapping(value = "/countMarketData0", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String countMarketData(Integer timeType, String startTime, String endTime, String userGroupId, String userId) {
        // 默认为今天
        if (timeType == null) {
            timeType = 1;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("callSum", 0);
        data.put("calledSum", 0);
        data.put("calledPercent", Double.valueOf(0));
        data.put("calledSumTime", 0);
        data.put("calledAvgTime", 0);
        data.put("intenCustomerSum", 0);
        data.put("sendSmsSum", 0);
        data.put("sendFlashSmsSum", 0);

        Map<String, Object> marketData = null;
        try {
            marketData = marketResourceService.countMarketDataV5(timeType, opUser(), startTime, endTime, userGroupId, userId);
        } catch (Exception e) {
            LOG.error("首页统计异常:", e);
            marketData = data;
        }
        return JSON.toJSONString(marketData);
    }

    @RequestMapping(value = "/countMarketDataV3", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String countMarketDataV3(Integer timeType, String startTime, String endTime, String userGroupId, String userId) {
        // 默认为今天
        if (timeType == null) {
            timeType = 1;
        }
        Map<String, Object> data = new HashMap<>();
        data.put("callSum", 0);
        data.put("calledSum", 0);
        data.put("calledPercent", Double.valueOf(0));
        data.put("calledSumTime", 0);
        data.put("calledAvgTime", 0);
        data.put("intenCustomerSum", 0);
        data.put("sendSmsSum", 0);
        data.put("sendFlashSmsSum", 0);

        Map<String, Object> marketData = marketResourceService.countMarketDataV3(timeType, opUser(), startTime, endTime, userGroupId, userId);
        return JSON.toJSONString(marketData);
    }

    /**
     * 短信历史查询
     *
     * @return
     */
    @RequestMapping(value = "/querySmsHistory0", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String querySmsHistory(Integer pageNum, Integer pageSize, String superId, String startTime, String endTime,
                                  String userName, String group_id, String marketTaskId, String seaId, String activeSTime, String activeETime, String status) {
        LoginUser lu = opUser();
        String cust_id = lu.getCustId();
        String user_id = "";
        if ("ROLE_CUSTOMER".equals(opUser().getRole())) {
            user_id = lu.getId().toString();
        }

        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        Page page = marketResourceService.querySmsHistoryV1(user_id, lu.getUserType(), cust_id, pageNum,
                pageSize, superId, startTime, endTime, userName, group_id, marketTaskId, seaId, activeSTime, activeETime, status);
        map.put("data", page.getData());
        map.put("total", page.getTotal());

        Map<String, Object> template = new HashMap<>();
        template.put("id", "");
        template.put("createTime", "");
        template.put("title", "");
        template.put("mouldContent", "");
        map.put("templateData", template);
        // 发送时间
        map.put("sendTime", "");
        if (StringUtil.isNotEmpty(marketTaskId)) {
            MarketTemplateDTO dto = marketTaskService.getSmsEmailTemplate(marketTaskId);
            template.put("id", dto.getId());
            template.put("createTime", dto.getCreateTime());
            template.put("title", dto.getTitle() != null ? dto.getTitle() : "");
            template.put("mouldContent", dto.getMouldContent() != null ? dto.getMouldContent() : "");
            map.put("templateData", template);
            // 发送时间
            map.put("sendTime", marketTaskService.getSmsTime(marketTaskId));
        }
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 短信客戶列表查詢
     *
     * @param pageNum
     * @param pageSize
     * @param
     * @return
     */
    @RequestMapping(value = "/getSmsSuperIdList", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String getSmsSuperIdList(Integer pageNum, Integer pageSize, String batch_number) {
        String cust_id = "";
        if ("ROLE_CUSTOMER".equals(opUser().getRole())) {
            cust_id = opUser().getCustId();
        }
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        Page page = marketResourceService.getSmsSuperIdListV1(cust_id, batch_number, pageNum, pageSize);
        //String total = marketResourceService.getSmsSuperIdListTotal(cust_id, batch_number);
        map.put("data", page.getData());
        map.put("total", page.getTotal());
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 邮件历史查询
     *
     * @return
     */
    @RequestMapping(value = "/queryEmailHistory", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryEmailHistory(Integer pageNum, Integer pageSize, String superId, String startTime, String endTime, String userName) {
        LoginUser lu = opUser();
        String cust_id = lu.getCustId();
        String user_id = lu.getId().toString();
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> list = marketResourceService.queryEmailHistory(user_id, lu.getUserType(), cust_id, pageNum, pageSize, superId, startTime, endTime, userName);
        List<Map<String, Object>> listSize = marketResourceService.queryEmailHistory(user_id, lu.getUserType(), cust_id, 0, 100000, superId, startTime, endTime, userName);
        map.put("data", list);
        map.put("total", listSize.size());
        json.put("data", map);
        return json.toJSONString();
    }

    @RequestMapping(value = "/getEmailSuperIdList", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String getEmailSuperIdList(Integer pageNum, Integer pageSize, String batch_number) {
        String cust_id = opUser().getCustId();
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> list = marketResourceService.getEmailSuperIdList(cust_id, batch_number, pageNum, pageSize);
        String toatal = marketResourceService.getEmailSuperIdListTotal(cust_id, batch_number);
        map.put("data", list);
        map.put("total", toatal);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 查询潜在客户
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/queryPotentialCusGroup", method = RequestMethod.GET)
    @CacheAnnotation
    public Object queryPotentialCusGroup(Integer pageNum, Integer pageSize, Integer cusGroupId, String cusGroupName) {

        Map<String, Object> resultMap = new HashMap<>();

        String custId = opUser().getCustId();
        String userType = opUser().getUserType();
        Long userId = opUser().getId();
        // String custId = "1702210227030000";

        resultMap.put("data", JSONObject.toJSON(
                marketResourceService.queryPotentialCusGroup(pageNum, pageSize, custId, cusGroupId, cusGroupName, userType, userId)));
        return JSONObject.toJSON(resultMap);
    }

    @ResponseBody
    @RequestMapping(value = "/queryPotentialCusGroupV1", method = RequestMethod.GET)
    @CacheAnnotation
    public Object queryPotentialCusGroup0(@Valid PageParam pageParam, BindingResult error, Integer cusGroupId, String cusGroupName) {
        if (error.hasErrors()) {
            return returnError(getErrors(error));
        }
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = null;
        long total = 0;
        try {
            list = marketResourceService.queryPotentialCusGroupV3(pageParam.getPageNum(), pageParam.getPageSize(), cusGroupId, cusGroupName, opUser());
            total = marketResourceService.countQueryPotentialCusGroupV3(cusGroupId, cusGroupName, opUser());
        } catch (Exception e) {
            LOG.error("潜客列表查询失败,", e);
        }

        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        map.put("cusGroupList", list);
        map.put("total", total);
        result.add(map);
        resultMap.put("data", result);
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 查询潜在客户明细
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/queryPotentialDetail", method = RequestMethod.POST)
    @CacheAnnotation
    public Object queryPotentialDetail(@RequestBody JSONObject jsonO) {

        Map<String, Object> resultMap = new HashMap<>();
        JSONObject json = new JSONObject();

        String custId = opUser().getCustId();
        Long userId = opUser().getId();
        String userType = opUser().getUserType();
//		 String custId = "1704151033470003";
//		 Long userId = Long.parseLong("17041510334700005") ;

        Integer pageNum = Integer.parseInt(jsonO.getString("pageNum"));
        Integer pageSize = Integer.parseInt(jsonO.getString("pageSize"));

        JSONArray custProperty = jsonO.getJSONArray("custProperty");
        String superId = jsonO.getString("superId");
        String custGroupId = jsonO.getString("custGroupId");

        resultMap.put("data", JSONObject.toJSON(marketResourceService.queryPotentialDetail(pageNum, pageSize,
                custGroupId, custProperty, superId, custId, userId, userType)));
        return JSONObject.toJSON(resultMap);
    }

    @ResponseBody
    @RequestMapping(value = "/queryPotentialDetailV1", method = RequestMethod.POST)
    @CacheAnnotation
    public Object queryPotentialDetail0(@RequestBody JSONObject jsonO) {
        Map<String, Object> resultMap = new HashMap<>();

        Integer pageNum = Integer.parseInt(jsonO.getString("pageNum"));
        Integer pageSize = Integer.parseInt(jsonO.getString("pageSize"));
        List<Map<String, Object>> list = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();

        long total = 0;
        if (pageNum != null && pageSize != null) {
            try {
                if (pageNum >= 0 && pageSize > 0 && pageSize <= 100) {
                    JSONArray custProperty = jsonO.getJSONArray("custProperty");
                    String superId = jsonO.getString("superId");
                    String custGroupId = jsonO.getString("custGroupId");

                    list = marketResourceService.queryPotentialDetailV3(pageNum, pageSize,
                            custGroupId, custProperty, superId, opUser());
                    total = marketResourceService.countQueryPotentialDetailV3(custGroupId, custProperty, superId, opUser());
                } else {
                    resultMap.put("pageNum", "参数异常");
                    resultMap.put("pageSize", "参数异常");
                }
            } catch (Exception e) {
                LOG.error("潜客详情列表查询异常,", e);
            } finally {
                map.put("custGroupOrders", list);
                map.put("total", total);
                result.add(map);
                resultMap.put("data", result);
            }
        }

        return JSONObject.toJSON(resultMap);
    }

    /**
     * 管理负责人查询
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/queryAssignedGroupDetail", method = RequestMethod.POST)
    @CacheAnnotation
    public Object queryAssignedGroupDetail(@RequestBody CustomerGrpOrdParam param) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        if (StringUtil.isEmpty(opUser().getCustId())) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }
        String custId = opUser().getCustId();
        // String custId = "1702210227030000";
        resultMap.put("data", JSONObject.toJSON(marketResourceService.listCustGroupOrders(custId, param)));
        return JSONObject.toJSON(resultMap);
    }

    @ResponseBody
    @RequestMapping(value = "/queryAssignedGroupDetailV1", method = RequestMethod.POST)
    @CacheAnnotation
    public Object queryAssignedGroupDetail0(@Valid @RequestBody CustomerGrpOrdParam param, BindingResult error) throws Exception {
        if (error.hasErrors()) {
            return returnError(getErrors(error));
        }
        Map<String, Object> resultMap = new HashMap<>();
        if (StringUtil.isEmpty(opUser().getCustId())) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }
        String customerId = opUser().getCustId();
        resultMap.put("data", JSONObject.toJSON(marketResourceService.listCustGroupOrdersV2(customerId, param)));
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 分配管理人(不在使用)
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/assigned{type}", method = RequestMethod.POST)
    @CacheAnnotation
    public Object assigned(@RequestBody JSONObject jsonO, @PathVariable int type) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();

        String message = null;

        String custId = opUser().getCustId();
        //String custId = "1704151033470003";

        if (1 == type) {// 单个分配负责任人
            Long userId = jsonO.getLong("userId");// 单个userId
            String id = jsonO.getString("id");// 单个Id
            Integer custGroupId = jsonO.getInteger("custGroupId");
            Integer marketTaskId = jsonO.getInteger("marketTaskId");
            message = marketResourceService.updateAssignedOne(id, userId, custId, custGroupId);
        } else if (2 == type) {// 已选分配负责任人
            Long userId = jsonO.getLong("userId");// 单个的userId
            JSONArray peopleAssignedIds = jsonO.getJSONArray("ids");// 为已选分配责任人的已选Id
            Integer custGroupId = jsonO.getInteger("custGroupId");

            PeopleAssignedDTO peopleAssignedDTO = new PeopleAssignedDTO();

            List<PeopleAssignedDTO> list = new ArrayList<PeopleAssignedDTO>();
            for (int i = 0; i < peopleAssignedIds.size(); i++) {
                String id = peopleAssignedIds.getString(i);
                peopleAssignedDTO = new PeopleAssignedDTO();
                peopleAssignedDTO.setId(id);
                peopleAssignedDTO.setUserId(userId);
                peopleAssignedDTO.setCustGroupId(custGroupId);
                list.add(peopleAssignedDTO);
            }
            message = marketResourceService.updateAssignedMany(list, custId);
        } else if (3 == type) {// 快速分配

            JSONArray peopleAssignedList = jsonO.getJSONArray("assignedlist");// 快速分配的责任人
            Integer custGroupId = jsonO.getInteger("custGroupId");

            for (int i = 0; i < peopleAssignedList.size(); i++) {
                JSONObject jsonObject2 = peopleAssignedList.getJSONObject(i);
                Integer number = jsonObject2.getInteger("number");// 快速分配的人数
                String userId = jsonObject2.getString("userId");

                PeopleAssignedDTO peopleAssignedDTO = new PeopleAssignedDTO();
                List<PeopleAssignedDTO> list = new ArrayList<PeopleAssignedDTO>();

                // 查找未分配负责人的
                List<Map<String, Object>> noAssignedlist = marketResourceService.queryNoAssigned(custId, custGroupId);

                if (noAssignedlist.size() == 0) {
                    resultMap.put("message", "全部已分配负责人");
                    return JSONObject.toJSON(resultMap);
                }

                for (int j = 0; j < number; j++) {
                    Map<String, Object> noAssignedMap = noAssignedlist.get(j);
                    String id = (String) noAssignedMap.get("id");

                    Long userIdA = Long.parseLong(userId);
                    peopleAssignedDTO = new PeopleAssignedDTO();
                    peopleAssignedDTO.setId(id);
                    peopleAssignedDTO.setUserId(userIdA);
                    peopleAssignedDTO.setCustGroupId(custGroupId);
                    list.add(peopleAssignedDTO);

                }
                message = marketResourceService.updateAssignedMany(list, custId);

            }

        }
        resultMap.put("message", message);
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 分配管理人
     *
     * @param jsonO
     * @param type
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/assignedNew{type}", method = RequestMethod.POST)
    public Object assigned0(@RequestBody JSONObject jsonO, @PathVariable int type) {
        Map<String, Object> resultMap = new HashMap<>();
        String message = null;
        String customerId = opUser().getCustId();
        Integer ruleType = jsonO.getInteger("ruleType");
        ruleType = 1;
        // 单个分配负责任人
        if (1 == type) {
            Long userId = jsonO.getLong("userId");
            String id = jsonO.getString("id");
            Integer custGroupId = jsonO.getInteger("custGroupId");
            String marketTaskId = jsonO.getString("marketTaskId");
            // 给用户分配
            if (1 == ruleType) {
                message = marketResourceService.updateAssignedOne0(id, userId, custGroupId, "", marketTaskId);
                //给用户组分配
            } else if (2 == ruleType) {
                message = marketResourceService.updateAssignedOne0(id, null, custGroupId, String.valueOf(userId), marketTaskId);
            }
            // 已选分配负责任人
        } else if (2 == type) {
            Long userId = jsonO.getLong("userId");
            JSONArray peopleAssignedIds = jsonO.getJSONArray("ids");
            Integer custGroupId = jsonO.getInteger("custGroupId");
            String marketTaskId = jsonO.getString("marketTaskId");
            PeopleAssignedDTO peopleAssignedDTO;
            List<PeopleAssignedDTO> list = new ArrayList<PeopleAssignedDTO>();
            for (int i = 0; i < peopleAssignedIds.size(); i++) {
                String id = peopleAssignedIds.getString(i);
                peopleAssignedDTO = new PeopleAssignedDTO();
                peopleAssignedDTO.setId(id);
                if (1 == ruleType) {
                    peopleAssignedDTO.setUserId(userId);
                    //给用户组分配
                } else if (2 == ruleType) {
                    peopleAssignedDTO.setUserGroupId(String.valueOf(userId));
                }
                peopleAssignedDTO.setCustGroupId(custGroupId);
                list.add(peopleAssignedDTO);
            }
            message = marketResourceService.updateAssignedMany0(list, custGroupId, ruleType, marketTaskId);
            // 快速分配
        } else if (3 == type) {
            JSONArray peopleAssignedList = jsonO.getJSONArray("assignedlist");
            Integer custGroupId = jsonO.getInteger("custGroupId");
            String intentLevel = jsonO.getString("intentLevel");
            String marketTaskId = jsonO.getString("marketTaskId");
            for (int i = 0; i < peopleAssignedList.size(); i++) {
                JSONObject jsonObject2 = peopleAssignedList.getJSONObject(i);
                Integer number = jsonObject2.getInteger("number");
                String userId = jsonObject2.getString("userId");
                message = marketResourceService.updateAssignedManyByCount(number, custGroupId, ruleType, userId, intentLevel, marketTaskId);
            }
        } else if (4 == type) {
            // 将客户群下所有数据按照条件分配给1个责任人
            //分配状态
            String status = jsonO.getString("status");
            // 原用户名称
            String sourceUserName = jsonO.getString("sourceUserName");
            String userId = jsonO.getString("userId");
            Integer custGroupId = jsonO.getInteger("custGroupId");
            String marketTaskId = jsonO.getString("marketTaskId");
            String intentLevel = jsonO.getString("intentLevel");
            JSONObject json = new JSONObject();
            Map<Object, Object> map = new HashMap<>();
            int code = 0;
            try {
                code = marketResourceService.updateCustomerGroupAllDataAssignedV4(custGroupId, userId, intentLevel, sourceUserName, status, marketTaskId);
            } catch (Exception e) {
                code = 0;
            }
            if (code > 0) {
                map.put("code", 1);
                map.put("message", "分配负责人成功");
                json.put("data", map);
            } else {
                map.put("code", 0);
                map.put("message", "分配负责人失败");
                json.put("data", map);
            }
            message = json.toJSONString();
        } else if (5 == type) {
            JSONArray peopleAssignedList = jsonO.getJSONArray("assignedlist");
            Integer custGroupId = jsonO.getInteger("custGroupId");
            String intentLevel = jsonO.getString("intentLevel");
            String marketTaskId = jsonO.getString("marketTaskId");
            for (int i = 0; i < peopleAssignedList.size(); i++) {
                JSONObject jsonObject2 = peopleAssignedList.getJSONObject(i);
                Integer number = jsonObject2.getInteger("number");
                String userId = jsonObject2.getString("userId");
                message = marketResourceService.updateAssignedManyByCount(number, custGroupId, ruleType, userId, opUser().getId(), intentLevel, marketTaskId);
            }
        }
        resultMap.put("message", message);
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 新建自建属性
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/inserCustomlabel", method = RequestMethod.POST)
    @CacheAnnotation
    public Object inserCustomlabel(String labelName, String labelDesc) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject json = new JSONObject();
        if (StringUtil.isEmpty(opUser().getCustId())) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }
        String custId = opUser().getCustId();
        Long userId = opUser().getId();

//		String custId = "1702210227030000";
//		Long userId = (long) 170;

        String labelId = Long.toString(IDHelper.getID());
        boolean judgeSuccess = customerLabelService.inserCustomlabel(custId, userId, labelId, labelName, labelDesc);

        if (true == judgeSuccess) {
            resultMap.put("code", "1");
            resultMap.put("message", "添加成功");
            json.put("data", resultMap);
        } else {
            resultMap.put("code", "0");
            resultMap.put("message", "添加失败");
            json.put("data", resultMap);
        }
        return json;
    }

    /**
     * 保存自建属性
     *
     * @param labelName
     * @param labelDesc
     * @param type
     * @param option
     * @param marketProjectId
     * @param sort
     * @param required
     * @return
     * @throws Exception
     */
    @ResponseBody
    @RequestMapping(value = "/inserCustomlabel/new", method = RequestMethod.POST)
    @CacheAnnotation
    public Object insertCustomLabel(String labelName, String labelDesc, Integer type, String option, String marketProjectId, Integer sort, String required) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject json = new JSONObject();
        if (StringUtil.isEmpty(opUser().getCustId())) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }
       /* synchronized (this) {
            boolean nameExist = customerLabelService.checkProjectLabelNameExist("", labelName, 1, opUser().getCustId());
            if (nameExist) {
                resultMap.put("code", "0");
                resultMap.put("message", "自建属性名称已经存在");
                json.put("data", resultMap);
                return json.toJSONString();
            }*/
        String custId = opUser().getCustId();
        Long userId = opUser().getId();

        String labelId = Long.toString(IDHelper.getID());
        boolean judgeSuccess = customerLabelService.insertCustomLabel0(custId, userId, labelId, labelName, labelDesc, type, option, marketProjectId.split(","), sort, required);

        if (judgeSuccess) {
            resultMap.put("code", "1");
            resultMap.put("message", "添加自建属性成功");
            json.put("data", resultMap);
        } else {
            resultMap.put("code", "0");
            resultMap.put("message", "添加自建属性失败");
            json.put("data", resultMap);
        }
        //}
        return json;
    }

    @ResponseBody
    @RequestMapping(value = "/updateCustomlabel/new", method = RequestMethod.POST)
    @CacheAnnotation
    public Object updateCustomlabel_V1(String labelId, String labelName, String labelDesc, Integer type, String option) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject json = new JSONObject();
        if (StringUtil.isEmpty(opUser().getCustId())) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }
        String custId = opUser().getCustId();
        Long userId = opUser().getId();

        boolean judgeSuccess = customerLabelService.updateCustomLabel_V1(userId, labelId, labelName, labelDesc, type, option);

        if (true == judgeSuccess) {
            resultMap.put("code", "1");
            resultMap.put("message", "更新自建属性成功");
            json.put("data", resultMap);
        } else {
            resultMap.put("code", "0");
            resultMap.put("message", "更新自建属性失败");
            json.put("data", resultMap);
        }
        return json;
    }

    /**
     * 编辑潜客的自建属性
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateCustomlabel", method = RequestMethod.POST)
    @CacheAnnotation
    public Object updateCustomlabel(@RequestBody JSONObject jsonO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject json = new JSONObject();

        JSONArray labelIdArray = jsonO.getJSONArray("labelIds");
        String superId = jsonO.getString("superId");
        String groupId = jsonO.getString("groupId");

        // 删除
        customerLabelService.deleteSuperLable(superId, groupId);

        if (labelIdArray != null || labelIdArray.size() != 0) {
            for (int i = 0; i < labelIdArray.size(); i++) {
                String labelId = labelIdArray.get(i).toString();
                // 添加
                String idSuper = Long.toString(IDHelper.getID());
                customerLabelService.insertSuperLable(idSuper, superId, labelId, groupId);
            }
        }
        resultMap.put("message", "成功");
        resultMap.put("data", "1");
        json.put("data", resultMap);
        return json;

    }


    /**
     * 编辑潜客的自建属性
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateCustomLabel/new", method = RequestMethod.POST)
    @CacheAnnotation
    public Object updateCustomLabelNew(@RequestBody JSONObject jsonO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        JSONObject json = new JSONObject();
        JSONArray labelIdArray = jsonO.getJSONArray("labelIds");
        String superId = jsonO.getString("superId");
        String groupId = jsonO.getString("groupId");
        String taskId = jsonO.getString("marketTaskId");

        Map<String, Object> labelData = new HashMap<>();
        try {
            // 删除
            //marketResourceService.deleteSuperLable(superId, groupId);
            if (labelIdArray != null || labelIdArray.size() != 0) {
                String labelId;
                String optionValue;
                for (int i = 0; i < labelIdArray.size(); i++) {
                    labelId = labelIdArray.getJSONObject(i).getString("labelId");
                    optionValue = labelIdArray.getJSONObject(i).getString("optionValue");
                    // 添加
                    String idSuper = Long.toString(IDHelper.getID());
                    //marketResourceService.insertSuperLable(idSuper, superId, labelId, groupId, optionValue);
                    labelData.put(labelId, optionValue);
                }
            }
            marketResourceService.updateTouchLabelDataV3(opUser().getId().toString(), groupId, superId, labelData, taskId);
            resultMap.put("message", "编辑自建属性成功");
            resultMap.put("data", "1");
        } catch (Exception e) {
            resultMap.put("message", "编辑自建属性失败");
            resultMap.put("data", "0");
            LOG.error("编辑潜客的自建属性失败,", e);
        }
        json.put("data", resultMap);
        return json;

    }

    /**
     * 查询自建属性
     *
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/getCustomlabel", method = RequestMethod.POST)
    @CacheAnnotation
    public String getCustomlabel(Integer pageNum, Integer pageSize, String customerGroupId, String marketTaskId, String projectId,
                                 String labelName, String type, String status) {

        String custId = opUser().getCustId();
        //String custId = "1702210227030000";
        String projectUserId = "";
        // 处理项目管理员
        if ("3".equals(opUser().getUserType())) {
            projectUserId = String.valueOf(opUser().getId());
        }
        return customerLabelService.getCustomLabel1(custId, pageNum, pageSize, customerGroupId,
                marketTaskId, projectUserId, projectId, labelName, type, status);

    }

    /**
     * 废弃自建属性
     *
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/deleteCustomlabel", method = RequestMethod.POST)
    @CacheAnnotation
    public String deleteCustomlabel(Integer id, Integer status) {
        return customerLabelService.deleteCustomLabel0(id, status);
    }

    /**
     * 自建属性编辑
     *
     * @param customerLabel
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/modifyCustomLabel", method = RequestMethod.POST)
    public String updateCustomLabel(CustomerLabel customerLabel) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        /*synchronized (this) {
            boolean nameExist = customerLabelService.checkProjectLabelNameExist(customerLabel.getLabelId(), customerLabel.getLabelName(), 1, opUser().getCustId());
            if (nameExist) {
                map.put("code", "0");
                map.put("message", "自建属性名称已经存在");
                json.put("data", map);
                return json.toJSONString();
            }*/

        customerLabel.setCustId(opUser().getCustId());
        int code = customerLabelService.modifyCustomLabel0(customerLabel);
        if (code == 1) {
            map.put("code", 1);
            map.put("message", "成功");
            json.put("data", map);
        } else {
            map.put("code", 0);
            map.put("message", "失败");
            json.put("data", map);
        }
        //}
        return json.toJSONString();

    }

    /**
     * 自建属性启用
     *
     * @param customerLabel
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/openCustomLabel", method = RequestMethod.POST)
    public String openCustomLabel(CustomerLabel customerLabel) {
        int code = customerLabelService.openCustomLabel(customerLabel);
        if (code == 1) {
            return returnSuccess();
        } else {
            return returnError();
        }

    }

    /**
     * 查找员工姓名
     *
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/getStaffName", method = RequestMethod.POST)
    @CacheAnnotation
    public String getStaffName() {
        String custId = opUser().getCustId();
        //String custId = "1702210227030000";
        return marketResourceService.getStaffName_V3(opUser());

    }

    /**
     * 查找客户已选标签(不在使用)
     *
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/getSelLabel", method = RequestMethod.POST)
    @CacheAnnotation
    public String getSelLabel(String superId, Integer custGroupId) {

        return customerLabelService.getSelLabel(superId, custGroupId);

    }

    /**
     * type=task时，从客群表/营销任务详情表取自定义标签数据
     *
     * @param superId
     * @param customerGroupId
     * @param
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getSelLabel/new", method = RequestMethod.POST)
    @CacheAnnotation
    public String getSelLabel0(String superId, Integer customerGroupId, String marketTaskId) {
        return customerLabelService.getSelLabelV3(superId, customerGroupId, marketTaskId);
    }

    /**
     * 查找客户已选标签
     *
     * @return
     */

    @ResponseBody
    @RequestMapping(value = "/updateVoiceSuperInfo", method = RequestMethod.POST)
    @CacheAnnotation
    public String updateVoiceSuperInfo(@RequestBody JSONObject jsonO) {
        Map<String, Object> resultMap = new HashMap<>();
        String custId = opUser().getCustId();
        Long userId = opUser().getId();

        String voice_info_id = jsonO.getString("voice_info_id");

        if (voice_info_id == null || "".equals(voice_info_id)) {
            voice_info_id = IDHelper.getID().toString();
        }

        String superId = jsonO.getString("superId");
        TouchInfoDTO touchInfoDTO = new TouchInfoDTO();
        touchInfoDTO.setVoice_info_id(voice_info_id);
        touchInfoDTO.setCust_id(custId);
        touchInfoDTO.setUser_id(userId.toString());
        touchInfoDTO.setCust_group_id(jsonO.getString("cust_group_id"));
        touchInfoDTO.setSuper_id(superId);
        touchInfoDTO.setSuper_name(jsonO.getString("super_name"));
        touchInfoDTO.setSuper_age(jsonO.getString("super_age"));
        touchInfoDTO.setSuper_sex(jsonO.getString("super_sex"));
        touchInfoDTO.setSuper_phone(jsonO.getString("super_telphone"));
        touchInfoDTO.setSuper_telphone(jsonO.getString("super_phone"));
        touchInfoDTO.setSuper_address_province_city(jsonO.getString("super_address_province_city"));
        touchInfoDTO.setMarket_task_id(jsonO.getString("marketTaskId"));
        touchInfoDTO.setSuper_address_street(jsonO.getString("super_address_street"));
        //更新打电话获取的用户信息
        //boolean judegeUpdate = marketResourceService.updateTouchInfo(touchInfoDTO);
        boolean judegeUpdate = marketResourceService.updateTouchInfoV3(touchInfoDTO);

        if (judegeUpdate == true) {
            resultMap.put("code", "0");
            resultMap.put("_message", "更新客户个人信息成功");
        } else {
            resultMap.put("code", "1");
            resultMap.put("_message", "更新客户个人信息失败");
        }
        return JSONObject.toJSONString(resultMap);

    }

    @ResponseBody
    @RequestMapping(value = "/updateVoiceSuperInfoV1", method = RequestMethod.POST)
    public String updateVoiceSuperInfoV1(@RequestBody JSONObject jsonO) {
        Map<String, Object> resultMap = new HashMap<>();
        String custId = opUser().getCustId();
        Long userId = opUser().getId();

        String superId = jsonO.getString("superId");
        TouchInfoDTO touchInfoDTO = new TouchInfoDTO();
        touchInfoDTO.setCust_id(custId);
        touchInfoDTO.setUser_id(userId.toString());
        touchInfoDTO.setCust_group_id(jsonO.getString("cust_group_id"));
        touchInfoDTO.setSuper_id(superId);
        touchInfoDTO.setSuper_name(jsonO.getString("super_name"));
        touchInfoDTO.setSuper_age(jsonO.getString("super_age"));
        touchInfoDTO.setSuper_sex(jsonO.getString("super_sex"));
        touchInfoDTO.setSuper_phone(jsonO.getString("super_telphone"));
        touchInfoDTO.setSuper_telphone(jsonO.getString("super_phone"));
        touchInfoDTO.setSuper_address_province_city(jsonO.getString("super_address_province_city"));
        touchInfoDTO.setSuper_address_street(jsonO.getString("super_address_street"));

        //更新打电话获取的用户信息
        boolean judgeUpdate = marketResourceService.updateTouchInfoV3(touchInfoDTO);

        if (judgeUpdate) {
            resultMap.put("code", "0");
            resultMap.put("_message", "更新客户群数据表客户个人信息成功");
        } else {
            resultMap.put("code", "1");
            resultMap.put("_message", "更新客户群数据表客户个人信息失败");
        }
        return JSONObject.toJSONString(resultMap);

    }

    @ResponseBody
    @RequestMapping(value = "/updateCallCountAndCallTime", method = RequestMethod.POST)
    @CacheAnnotation
    public String updateCallCountAndCallTime(@RequestBody String customGroupId, @RequestBody String[] superIds) {
        Map<String, Object> resultMap = new HashMap<>(16);
        boolean status = marketResourceService.updateCallCountAndCallTime(opUser().getCustId(), customGroupId, superIds);
        if (status) {
            resultMap.put("code", "0");
            resultMap.put("_message", "更新成功");
        } else {
            resultMap.put("code", "1");
            resultMap.put("_message", "更新失败");
        }
        return JSONObject.toJSONString(resultMap);
    }


    /**
     * 营销资源定价查询
     *
     * @return
     */
    @RequestMapping(value = "/getMarketResourceList", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String getMarketResourceList() {
        return marketResourceService.getMarketResourceList();
    }

    /**
     * 营销资源定价
     *
     * @return
     */
    @RequestMapping(value = "/updateMarketResource", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String updateMarketResource(String type_code, String resource_id, String price_code, Double price) {
        String operator = opUser().getName();
        return marketResourceService.updateMarketResource(type_code, resource_id, price_code, price, operator);
    }

    /**
     * 查询营销资源定价记录
     *
     * @param type_code
     * @param resource_id
     * @param price_code
     * @param
     * @return
     */
    @RequestMapping(value = "/getMarketResourceLog", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String getMarketResourceLog(String price_code, String type_code, String resource_id, Integer pageNum, Integer pageSize) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> list = marketResourceService.getMarketResourceLog(price_code, type_code, resource_id, pageNum, pageSize);
        String toatal = marketResourceService.getMarketResourceLogTotal(price_code, type_code, resource_id);
        map.put("data", list);
        map.put("total", toatal);
        json.put("data", map);
        return json.toJSONString();
    }

   /* @RequestMapping(value = "/getPhoneAttributionAreaV1", method = RequestMethod.GET)
    @ResponseBody
    public String getMarketResourceLog(String uuid, Integer type) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        String result = marketResourceService.getPhoneAttributionArea0(type, uuid);
        map.put("data", result);
        json.put("data", map);
        return json.toJSONString();
    }*/

    @RequestMapping(value = "/getPhoneAttributionArea", method = RequestMethod.GET)
    @ResponseBody
    public String getMarketResourceLog(String uuid, Integer type, String custGroupId) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        String result = marketResourceService.getPhoneAttributionAreaV1(type, uuid, custGroupId);
        map.put("data", result);
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 机器人话单选择部分进行审核
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/saveVoiceIntention", method = RequestMethod.POST)
    @ResponseBody
    public String saveVoiceIntention(@RequestBody JSONObject param) {
        Integer intentStatus = param.getInteger("intentStatus");
        List<String> touchIds = param.getObject("touchIds", List.class);
        String endTime = param.getString("endTime");
        if (intentStatus == null) {
            throw new ParamException("intentStatus参数不能为空");
        }
        if (StringUtil.isEmpty(endTime)) {
            throw new ParamException("endTime参数不能为空");
        }
        if (touchIds == null || (touchIds != null && touchIds.size() == 0)) {
            throw new ParamException("touchIds参数不能为空");
        }
        // 审核失败原因
        String clueAuditReason = param.getString("reason");
        boolean result = marketResourceService.saveVoiceIntention(touchIds, intentStatus, endTime, clueAuditReason);
        if (result) {
            return returnSuccess();
        }
        return returnError();
    }

    /**
     * 机器人话单选择全部进行审核(支持时间条件)
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/saveBatchVoiceIntention", method = RequestMethod.POST)
    @ResponseBody
    public String saveBatchVoiceIntention(@RequestBody VoiceLogQueryParam param) {
        if (StringUtil.isEmpty(param.getStartTime())) {
            throw new ParamException("startTime参数不能为空");
        }
        if (StringUtil.isEmpty(param.getEndTime())) {
            throw new ParamException("endTime参数不能为空");
        }
        if (StringUtil.isEmpty(param.getIntentLevel())) {
            throw new ParamException("intentLevel参数不能为空");
        }
        if (param.getIntentStatus() == null) {
            throw new ParamException("intentStatus参数不能为空");
        }
        if (StringUtil.isEmpty(param.getCustomerGroupId())) {
            throw new ParamException("customerGroupId参数不能为空");
        }
        UserQueryParam userQueryParam = getUserQueryParam();
        param.setCustId(userQueryParam.getCustId());
        param.setUserId(userQueryParam.getUserId());
        param.setUserType(userQueryParam.getUserType());
        param.setUserGroupRole(userQueryParam.getUserGroupRole());
        param.setUserGroupId(userQueryParam.getUserGroupId());
        boolean result = marketResourceService.saveBatchVoiceIntention0(param);
        if (result) {
            return returnSuccess();
        }
        return returnError();
    }


    @RequestMapping("/getVoice0/{userId}/{fileName:.+}")
    public void getOnlineVoiceFile(@PathVariable String userId, @PathVariable String fileName, HttpServletResponse response) throws Exception {
        marketResourceService.getVoiceFile(userId, fileName, request, response);
    }

    @RequestMapping("/getCustomerApparentNumber")
    public void getCustomerApparentNumber() {
        String apparentNumber = customerService.getCustomerApparentNumber(opUser().getCustId(), "", "");
        System.out.println(apparentNumber);
    }

    /**
     * 获取营销任务左侧树行结构
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getCustomlabelTree", method = RequestMethod.GET)
    @CacheAnnotation
    public String buildTaskTree(String customerGroupId) throws TouchException {
        String custId = opUser().getCustId();
        if (StringUtil.isEmpty(custId)) {
            throw new TouchException("无权");
        }
        JSONArray array = customerLabelService.buildTaskTree(custId, NumberConvertUtil.parseInt(customerGroupId));
        return JSON.toJSONString(array);
    }

    /**
     * @description 获取自定义属性
     * @method
     * @date: 2019/7/1 10:29
     */
    @ResponseBody
    @RequestMapping(value = "/get", method = RequestMethod.POST)
    public String getLabelInfo(@RequestBody CustomerLabelDTO customerLabelDTO) {
        ResponseJson responseJson = new ResponseJson();
        try {
            /*  if (StringUtil.isEmpty(projectId)) {
                responseJson.setMessage("参数不正确");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }*/
            Map<String, Object> map = customerLabelService.getLabelInfoById(customerLabelDTO, opUser());
            responseJson.setData(map);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }


    /**
     * @description 保存修改自定义屬性（跟进状态设置）
     * @method
     * @date: 2019/7/1 10:29
     */
    @ResponseBody
    @RequestMapping(value = "/saveOrUpdate", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public String save(@RequestBody CustomerLabelDTO customerLabelDTO) {
        ResponseJson responseJson = new ResponseJson();
        try {
            customerLabelService.labelSaveOrUpdate(customerLabelDTO, opUser());
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }


    /**
     * 查询通话记录(企业名称查询)
     *
     * @param pageParam
     * @param error
     * @param superId
     * @param customerGroupId
     * @param realName
     * @param createTimeStart
     * @param createTimeEnd
     * @param remark
     * @param callStatus
     * @param intentLevel
     * @param auditingStatus
     * @param marketTaskId
     * @return
     */
    @RequestMapping(value = "/queryRecordVoicelogV3", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String queryRecordVoicelogV3(@Valid PageParam pageParam, BindingResult error, String superId, String customerGroupId, String realName, String createTimeStart,
                                        String createTimeEnd, String remark, String callStatus, String intentLevel, String auditingStatus,
                                        String marketTaskId, String calledDuration, String labelProperty, String seaId, String group_id, String custName) {
        if (error.hasErrors()) {
            return getErrors(error);
        }
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        UserQueryParam userQueryParam = getUserQueryParam();
        userQueryParam.setPageNum(pageParam.getPageNum());
        userQueryParam.setPageSize(pageParam.getPageSize());
        // 通话时长范围检索
        int duration = 0;
        if (StringUtil.isNotEmpty(calledDuration)) {
            duration = NumberConvertUtil.parseInt(calledDuration);
        }
        Page page = null;
        if (StringUtil.isEmpty(customerGroupId)) {
            customerGroupId = group_id;
        }
        try {
            page = marketResourceService.queryRecordVoiceLogV3(userQueryParam, customerGroupId, superId, realName, createTimeStart,
                    createTimeEnd, remark, callStatus, intentLevel, auditingStatus, marketTaskId, duration, labelProperty, seaId, custName);
        } catch (Exception e) {
            page = new Page();
            LOG.error("查询通话记录分页失败,", e);
        }
        map.put("data", page.getData());
        map.put("total", page.getTotal());
        // 录音路径
        String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
        map.put("audioUrl", audioUrl);
        json.put("data", map);
        return json.toJSONString();

    }

    @ResponseBody
    @RequestMapping(value = "/updateDisplayStatus", method = RequestMethod.POST)
    public String updateDisplayStatus(CustomerLabel customerLabel) {
        int code = customerLabelService.updateDisplayStatus(customerLabel);
        if (code == 1) {
            return returnSuccess();
        } else {
            return returnError();
        }
    }

}




