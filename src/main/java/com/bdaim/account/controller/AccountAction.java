package com.bdaim.account.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.account.dto.TransactionQryParam;
import com.bdaim.account.service.impl.AccountService;
import com.bdaim.auth.LoginUser;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.service.BillService;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.AuthPassport;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.service.CustomerService;
import com.github.crab2died.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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
    public Object showAccountCenter() throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", JSONObject.toJSON(accountService.showAccoutCenter(opUser().getCustId())));
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
        Page list = null;
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
                    accountService.changeBalance(param);
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
        Page list = null;
        String basePath = "";
        Map<Object, Object> map = new HashMap<Object, Object>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            if (lu.getUserType().equals("1")) {
                list = accountService.pageListRecords(page, queryParam);
                basePath = "/pic";
            }
        }
        map.put("basePath", basePath);
        map.put("list", list);
        return new ResponseInfoAssemble().success(map);
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
            return new ResponseInfoAssemble().failure(-1,"缺少分页参数");
        }
        LoginUser lu = opUser();
        Page list = null;
        Map<String, Object> resultMap = new HashMap<>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = accountService.querySupplierAcctsByCondition(page, queryParam);
            String basePath = "/pic";
            resultMap.put("basePath", basePath);
            resultMap.put("list", list);
        }
        return new ResponseInfoAssemble().success(resultMap);
    }
    @RequestMapping(value = "/querySupplierExport",method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo querySupplierExport(CustomerBillQueryParam queryParam,HttpServletResponse response){
        LoginUser lu = opUser();
        List<Map<String,Object>> list = null;
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
        }catch (Exception e){
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
        Map<String, Object> remainMap = new HashMap<>();
        if (StringUtil.isNotEmpty(supplierId)) {
            Double remainAmount = customerService.getSourceRemainMoney(supplierId);
            remainMap.put("cucRemainMoney", remainAmount);
        } else {
            throw new RuntimeException("参数错误");
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
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", JSONObject.toJSON(accountService.queryAccoutCenter(custId)));
        return JSONObject.toJSON(resultMap);
    }
}

