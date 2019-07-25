package com.bdaim.account.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.account.service.impl.AccountService;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.service.BillService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.AuthPassport;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.slxf.dto.LoginUser;
import com.bdaim.slxf.entity.TransactionQryParam;
import com.bdaim.slxf.service.impl.TransactionService;
import com.bdaim.supplier.dto.SupplierEnum;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.HashMap;
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

    private static Log logger = LogFactory.getLog(AccountAction.class);

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
    public String queryAccountsByCondition(@Valid PageParam page, BindingResult error, CustomerBillQueryParam queryParam) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
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
        return JSON.toJSONString(list);
    }


    /**
     * 后台 资金管理--企业资金/供应商资金--充值扣减
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/balance/operate", method = RequestMethod.POST)
    @ResponseBody
    public Object banlanceChange(@RequestBody CustomerBillQueryParam param) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
                if (opUser().getId() != null) {
                    param.setUserId(opUser().getId());
                }
                Integer action = param.getAction();
                //action 0 充值   1扣减
                if (action != null && action.equals(1)) {
                    accountService.changeBalance(param);
                    resultMap.put("result", "0");
                    resultMap.put("_message", "账户扣减成功！");
                } else if (action != null && action.equals(0)) {
                    if (StringUtil.isNotEmpty(param.getDealType()) && param.getDealType().equals("0")) {
                        param.setCustomerId("2");//暂时供应商联通资金添加
                    }
                    //企业充值  或  供应商资金添加
                    accountService.changeBalance(param);
                    resultMap.put("result", "0");
                    resultMap.put("_message", "账户充值成功！");
                } else if (action == null) {
                    resultMap.put("result", "0");
                    resultMap.put("_message", "账户充值扣减失败！");
                }
            }
            return JSONObject.toJSON(resultMap);
        } catch (Exception e) {
            logger.error("账户充值扣减失败!\t" + e.getMessage());
            resultMap.put("result", "0");
            resultMap.put("_message", "账户充值扣减失败！");
            return JSONObject.toJSON(resultMap);
        }
    }

    /*
     *
     * 后台 资金管理--企业资金--充值记录
     *
     * */
    @RequestMapping(value = "/queryCustomerRecords", method = RequestMethod.GET)
    @ResponseBody
    public String queryAccountsRecordsByCondition(@Valid PageParam page, BindingResult error, CustomerBillQueryParam queryParam) throws Exception {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        LoginUser lu = opUser();
        Page list = null;
        JSONObject json = new JSONObject();
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
        json.put("data", map);
        return json.toJSONString();
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
            return getErrors(error);
        }
        LoginUser lu = opUser();
        Page list = null;
        Map<String, Object> resultMap = new HashMap<>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            String custID = lu.getCustId();
            queryParam.setCustomerId("0");
            list = accountService.querySupplierAcctsByCondition(page, queryParam);
            String basePath = "/pic";
            resultMap.put("basePath", basePath);
            resultMap.put("list", list);
        }
        return JSON.toJSONString(resultMap);
    }

    /*

     *
     *后台 资金管理--供应商资金-可用余额
     * */
    @RequestMapping(value = "/supplierRemainMoney", method = RequestMethod.GET)
    @ResponseBody
    public Object supplierRemainMoney(String supplierId) {
        Map<String, Object> remainMap = new HashMap<>();
        if (StringUtil.isNotEmpty(supplierId)){
            Double remainAmount = customerService.getSourceRemainMoney(supplierId);
            remainMap.put("cucRemainMoney", remainAmount);
        }else {
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

