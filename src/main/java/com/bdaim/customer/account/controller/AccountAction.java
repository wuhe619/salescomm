package com.bdaim.customer.account.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.service.BillService;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.page.PageList;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.account.dto.TransactionQryParam;
import com.bdaim.customer.account.service.impl.AccountService;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.rbac.dto.RoleEnum;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.util.AuthPassport;
import com.bdaim.util.StringUtil;
import com.github.crab2died.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanls@bdaim.com
 * @Description: 账户余额
 * @date 2018/9/710:27
 */
@Controller
@RequestMapping(value = "/account")
public class AccountAction extends BasicAction {
    @Resource
    AccountService accountService;
    @Resource
    TransactionService transactionService;
    @Resource
    CustomerService customerService;
    @Resource
    BillService billService;

    private static Logger logger = LoggerFactory.getLogger(AccountAction.class);

    /*
     *
     *前台 账户余额---账户余额
     *
     * */
    @RequestMapping(value = "/center/show/", method = RequestMethod.GET)
    @ResponseBody
    public Object showAccountCenter() {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            resultMap.put("data", JSONObject.toJSON(accountService.showAccoutCenter(opUser().getCustId())));

        } catch (Exception e) {
            logger.info("查询账户余额出错 》》》》》" + e);
        }
        return JSONObject.toJSON(resultMap);
    }

    /*
     * 前台 账户余额---收支明细
     *
     * */
    /*@RequestMapping(value = "/transaction/query", method = RequestMethod.GET)
    @ResponseBody
    public String queryTransactions(@Valid PageParam page, BindingResult error, TransactionQryParam param) throws Exception {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        return JSON.toJSONString(transactionService.listTransactionsByCondtion(page, opUser().getCustId(), param));
    }*/

    @RequestMapping(value = "/transaction/query", method = RequestMethod.GET)
    @ResponseBody
    public String queryTransactions(@Valid PageParam page, BindingResult error, TransactionQryParam param) throws Exception {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        return JSON.toJSONString(transactionService.listTransactionsByCondtion(page, opUser().getCustId(), param));
    }


    /*
     * 前台 账户余额---收支明细导出接口
     *
     * */
    @RequestMapping(value = "/export/customerBillDetails.do", method = RequestMethod.GET)
    @ResponseBody
    public Object exportCustomerBillDetails(TransactionQryParam param, HttpServletResponse response) {
        return transactionService.exportCustomerBillDetails(opUser().getCustId(), param, response);
    }

    /*
     *
     * 后台 资金管理--企业资金
     *
     * */
    @RequestMapping(value = "/queryCustomer", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo queryAccountsByCondition(@Valid PageParam page, BindingResult error, CustomerBillQueryParam queryParam) {
        if (error.hasFieldErrors()) {
            return new ResponseInfoAssemble().failure(-1, "缺少必要參數");
        }
        LoginUser lu = opUser();
        PageList list = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = accountService.pageList(page, queryParam);
            if (list != null) {
                for (int i = 0; i < list.getList().size(); i++) {
                    Map map = (Map<String, Object>) list.getList().get(i);
                    if (map != null && map.get("cust_id") != null) {
                        String custId = map.get("cust_id").toString();
                        String billDate = queryParam.getBillDate();
                        if (StringUtil.isNotEmpty(custId) && StringUtil.isNotEmpty(billDate)) {
                            Object consumeTotal = billService.queryCustomerConsumeTotal(custId, billDate);
                            if (consumeTotal != null) {
                                map.put("amountSum", consumeTotal);
                            } else {
                                map.put("amountSum", Double.valueOf("0"));
                            }
                            Double remainAmount = customerService.getRemainMoney(custId) / 100;
                            if (remainAmount != null) {
                                map.put("remainAmount", remainAmount);
                            } else {
                                map.put("remainAmount", Double.valueOf("0"));

                            }
                        }
                    }
                }
            }
        }
        return new ResponseInfoAssemble().success(list);
    }


    /**
     * 后台 资金管理--企业资金/供应商资金--充值扣减
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/balance/operate", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo banlanceChange(@RequestBody CustomerBillQueryParam param) {
        try {
            if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
                if (opUser().getId() != null) {
                    param.setUserId(opUser().getId());
                }
                accountService.changeBalance(param,opUser());
                return new ResponseInfoAssemble().success(null);
            }
        } catch (Exception e) {
            logger.error("账户充值扣减失败!\t" + e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "账户操作失败");
        }
        return new ResponseInfoAssemble().success(null);
    }

    /*
     *
     * 后台 资金管理--企业资金--充值扣減
     *
     * */
    @RequestMapping(value = "/queryCustomerRecords", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo queryAccountsRecordsByCondition(@Valid PageParam page, BindingResult error, CustomerBillQueryParam queryParam) throws Exception {
        if (error.hasFieldErrors()) {
            return new ResponseInfoAssemble().failure(-1, "缺少必要参数");
        }
        LoginUser lu = opUser();
        PageList list = null;
        String basePath = "";
        Map<Object, Object> map = new HashMap<Object, Object>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            //if (lu.getUserType().equals("1")) {
            list = accountService.pageListRecords(page, queryParam);
            basePath = "/pic";
            // }
        }
        map.put("basePath", basePath);
        map.put("list", list);
        return new ResponseInfoAssemble().success(map);
    }

    /**
     * 后台 资金管理--企业资金--充值扣減 导出excel
     *
     * @param
     * @return
     * @auther Chacker
     * @date
     */
    @RequestMapping(value = "/queryCustomerRecordsExport", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo queryCustomerRecordsExport(CustomerBillQueryParam queryParam, HttpServletResponse response) {
        LoginUser lu = opUser();
        List<Map<String, Object>> list = null;
        String basePath = "";
        Map<Object, Object> map = new HashMap<Object, Object>();
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                if (lu.getUserType().equals("1")) {
                    list = accountService.pageListRecords(queryParam);
                    //将列表信息导出为excel
                    List<String> header = new ArrayList<>();
                    List<List<Object>> data = new ArrayList<>();
                    header.add("交易事项");
                    header.add("交易日期");
                    header.add("流水号");
                    header.add("交易总额(元)");
                    header.add("操作人");
                    header.add("备注");
                    List<Map<String, Object>> dataList = list;
                    List<Object> rowList;
                    for (Map<String, Object> column : dataList) {
                        rowList = new ArrayList<>();
                        rowList.add(column.get("typeContent") != null ? column.get("typeContent") : "");
                        rowList.add(column.get("create_time") != null ? column.get("create_time") : "");
                        rowList.add(column.get("transaction_id") != null ? column.get("transaction_id") : "");
                        rowList.add(column.get("amount") != null ? column.get("amount") : "");
                        rowList.add(column.get("realname") != null ? column.get("realname") : "");
                        rowList.add(column.get("remark") != null ? column.get("remark") : "");
                        data.add(rowList);
                    }
                    logger.info("data获取成功，值为" + data.toString());
                    //下载的response属性设置
                    response.setCharacterEncoding("utf-8");
                    response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
                    String fileName = "账单.xlsx";
                    ////保存的文件名,必须和页面编码一致,否则乱码
                    String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));
                    response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
                    OutputStream outputStream = response.getOutputStream();


                    ExcelUtils.getInstance().exportObjects2Excel(data, header, outputStream);
                    outputStream.flush();
                    outputStream.close();
                }
            }
        } catch (Exception e) {
            logger.info("导出出现异常，异常信息为" + e.getMessage());
        }
        return new ResponseInfoAssemble().success(null);
    }

    /*
     *
     *后台 资金管理--企业资金-充值记录导出
     * */
    @RequestMapping(value = "/exportCustomerAccountRecords", method = RequestMethod.GET)
    @ResponseBody
    public Object exportCustomerAccountRecharge(CustomerBillQueryParam param, HttpServletResponse response) {
        return accountService.exportCustomerAccountRecharge(param, response);
    }


    /*后台 资金管理--供应商资金列表*/
    @RequestMapping(value = "/querySupplier", method = RequestMethod.GET)
    @ResponseBody
    public Object querySuppliserAccountsByCondition(@Valid PageParam page, BindingResult error, CustomerBillQueryParam queryParam) {
        if (error.hasFieldErrors()) {
            return new ResponseInfoAssemble().failure(-1, "缺少分页参数");
        }
        LoginUser lu = opUser();
        PageList list = null;
        Map<String, Object> resultMap = new HashMap<>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = accountService.querySupplierAcctsByCondition(page, queryParam);
            String basePath = "/pic";
            resultMap.put("basePath", basePath);
            resultMap.put("list", list);
        }
        return new ResponseInfoAssemble().success(resultMap);
    }

    /**
     * 供应商充扣记录 导出excel
     *
     * @param queryParam
     * @return
     * @auther Chacker
     * @date
     */
    @RequestMapping(value = "/querySupplierExport", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo querySupplierExport(CustomerBillQueryParam queryParam, HttpServletResponse response) {
        LoginUser lu = opUser();
        List<Map<String, Object>> list = null;
        Map<String, Object> resultMap = new HashMap<>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = accountService.querySupplierAcctsExport(queryParam);
        }
        List<String> header = new ArrayList<>();
        header.add("流水号");
        header.add("交易类型");
        header.add("交易时间");
        header.add("金额");
        header.add("操作人");
        header.add("备注");
        List<List<Object>> data = new ArrayList<>();
        List<Object> rowList;
        for (Map<String, Object> column : list) {
            rowList = new ArrayList<>();
            rowList.add(column.get("transaction_id") != null ? column.get("transaction_id") : "");
            rowList.add(column.get("billType") != null ? column.get("billType") : "");
            rowList.add(column.get("create_time") != null ? column.get("create_time") : "");
            rowList.add(column.get("amount") != null ? column.get("amount") : "");
            rowList.add(column.get("realname") != null ? column.get("realname") : "");
            rowList.add(column.get("remark") != null ? column.get("remark") : "");
            data.add(rowList);
        }
        try {
            //下载的response属性设置
            response.setCharacterEncoding("utf-8");
//        response.setContentType("application/force-download");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "充值扣减记录.xlsx";
            ////保存的文件名,必须和页面编码一致,否则乱码
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            OutputStream outputStream = response.getOutputStream();


            ExcelUtils.getInstance().exportObjects2Excel(data, header, outputStream);
            outputStream.flush();
            outputStream.close();

            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            logger.info("导出充扣记录异常");
            logger.info(e.getMessage());
        }

        return new ResponseInfoAssemble().success(null);
    }

    /*

     *
     *后台 资金管理--供应商资金-可用余额
     * */
    @RequestMapping(value = "/supplierRemainMoney", method = RequestMethod.GET)
    @ResponseBody
    public Object supplierRemainMoney(String supplierId) {
        logger.info("查询供应商可用余额 ===》》》");
        Map<String, Object> remainMap = new HashMap<>();
        try {
            if (StringUtil.isNotEmpty(supplierId)) {
                Double remainAmount = customerService.getSourceRemainMoney(supplierId);
                remainMap.put("cucRemainMoney", remainAmount);
            } else {
                Double remainAmount = customerService.getSourceRemainMoney("2");
                Double remainAmount1 = customerService.getSourceRemainMoney("3");
                Double remainAmount2 = customerService.getSourceRemainMoney("4");
                remainMap.put("cucRemainMoney", remainAmount+remainAmount1+remainAmount2);
            }

        } catch (Exception e) {
            logger.info("查询发生异常 》》》" + e);
        }
        return JSONObject.toJSON(remainMap);
    }

    /*
     *
     *后台 资金管理--供应商资金-导出
     * */
    @RequestMapping(value = "/exportSupplierRecords", method = RequestMethod.GET)
    @ResponseBody
    public Object exportSupplierRecords(CustomerBillQueryParam param, HttpServletResponse response) {
        return accountService.exportSupplierRecords(param, response);
    }


    /*
     *前台 账户余额---账户余额（对外接口）
     * */
    @AuthPassport
    @RequestMapping(value = "/open/show.do", method = RequestMethod.POST)
    @ResponseBody
    public Object showAccount() {
        CustomerUser u = (CustomerUser) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", JSONObject.toJSON(accountService.queryAccoutCenter(custId)));
        return JSONObject.toJSON(resultMap);
    }

    @Resource
    SendSmsService sendSmsService;

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public Object queryAccountsByCondition(UserQueryParam queryParam) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            resultMap.put("data", JSONObject.toJSON(accountService.queryAccountByCondition(queryParam)));
        } catch (Exception e) {
            logger.error("查询客户余额列表失败", e);
            Map<String, Object> map = new HashMap<>();
            map.put("total", 0);
            map.put("list", new ArrayList<>());
            resultMap.put("data", map);
        }
        return JSONObject.toJSON(resultMap);
    }


    @RequestMapping(value = "/balance/operate0", method = {RequestMethod.PUT, RequestMethod.POST})
    @ResponseBody
    public Object banlanceChange(@RequestBody JSONObject param) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        param.put("userId", opUser().getId());
        return JSONObject.toJSON(accountService.changeBalance(param));
    }

    @RequestMapping(value = "/creditLimit/set/{accountId}/{creditLimit}")
    @ResponseBody
    public Object setCreditLimit(@PathVariable String accountId, @PathVariable int creditLimit) throws TouchException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        accountService.setCreditLimit(accountId, creditLimit);
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/update/pwd/")
    @ResponseBody
    public Object updatePayPwd(@RequestBody JSONObject param) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String mobileNum = param.get("mobileNum").toString();
        String otp = param.get("otp").toString();
        String customerId = opUser().getCustId();
        if (StringUtil.isEmpty(mobileNum)) {
            throw new TouchException("20005", "手机号码不能为空");
        }
        boolean success = sendSmsService.verificationCode(mobileNum, 7, otp) == 1 ? true : false;
        if (!success) {
            throw new TouchException("20006", "手机验证码不正确");
        }
        try {
            accountService.updatePayPassword(customerId, param);
        } catch (Exception e) {
            logger.error("更新支付密码失败,", e);
        }
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/center/show0/", method = RequestMethod.GET)
    @ResponseBody
    public Object showAccountCenter0() throws Exception {
        //Todo 需要在登录的时候将customerId加入到session
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("data", JSONObject.toJSON(accountService.showOnlineAccoutCenter(opUser().getCustId())));
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/transaction/query0", method = RequestMethod.GET)
    @ResponseBody
    public Object queryTransactions(TransactionQryParam param, String custId) throws Exception {
        //Todo 需要在登录的时候将customerId加入到session
        Map<String, Object> resultMap = new HashMap<String, Object>();
        HashMap<String, Object> ret = new HashMap<>();
        // 处理前台客户权限
        if (RoleEnum.ROLE_CUSTOMER.getRole().equals(opUser().getRole())) {
            custId = opUser().getCustId();
        }
        Page page = transactionService.listTransactionsByCondition_V2(custId, param);
        //long total = transactionService.countTransactionsByCondition_V1(custId, param);
        ret.put("transactions", page.getData());
        ret.put("total", page.getTotal());

        List<Map<String, Object>> result = new ArrayList<>();
        result.add(ret);

        resultMap.put("data", result);
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @param param
     * @return
     * @throws Exception
     * @Description平台查询所有客户的交易记录接口
     */
    @RequestMapping(value = "/transaction/query/all", method = RequestMethod.GET)
    @ResponseBody
    public Object listAllTransactions(TransactionQryParam param) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        List<Map<String, Object>> list = transactionService.listAllTransactions(param);
        long total = transactionService.countAllTransactions(param);
        resultMap.put("transactionList", list);
        resultMap.put("total", total);
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("data", resultMap);
        return jsonObject.toJSONString();
    }
}

