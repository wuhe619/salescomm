package com.bdaim.bill.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.auth.LoginUser;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.dto.SupplierBillQueryParam;
import com.bdaim.bill.service.impl.BillServiceImpl;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.Constant;
import com.bdaim.common.util.StringUtil;
import com.bdaim.rbac.dto.Page;
import com.github.crab2died.ExcelUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/10/8 10:34
 */
@Controller
@RequestMapping(value = "/bill")
public class BillAction extends BasicAction {
    private static Log logger = LogFactory.getLog(BillAction.class);
   /* @Resource
    BillService billService;*/

    @Resource
    BillServiceImpl billService;

    /*
     *
     * 后台 账单管理--企业账单
     * */
    @RequestMapping(value = "/customerBill/query", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo customerBillQuery(@Valid PageParam page, BindingResult error, CustomerBillQueryParam param) {
        if (error.hasFieldErrors()) {
            return new ResponseInfoAssemble().failure(-1, "缺少必要参数");
        }
        LoginUser lu = opUser();
        Page list = null;
        String billDate = param.getBillDate();
        Map<String, Object> resultMap = new HashMap<>();
        BigDecimal profitSumBigDecimal = new BigDecimal("0");
        BigDecimal custSumBigDecimal = new BigDecimal("0");
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = billService.queryCustomerBill(page, param);
            DecimalFormat df = new DecimalFormat("0.00");
            Map<String, String> map;

            if (list != null) {
                for (int i = 0; i < list.getData().size(); i++) {
                    map = (Map<String, String>) list.getData().get(i);
                    if (map != null && map.get("cust_id") != null) {
                        String custId = map.get("cust_id");
                        if (StringUtil.isNotEmpty(custId)) {
                            Map<String, String> amountMap = billService.queryCustomerConsumeTotal(custId, billDate);
                            String amountSum = null, profitAmount = null, supAmountSum = null;
                            if (amountMap != null && amountMap.size() > 0) {
                                amountSum = amountMap.get("amountSum");
                                profitAmount = amountMap.get("profitAmount");
                                supAmountSum = amountMap.get("supAmountSum");
                                map.put("amountSum", amountSum);
                                map.put("profit", profitAmount);
                                map.put("supAmountSum", supAmountSum);
                            }

                        }
                    }
                }
            }
            //查询利润和总消费金额
            String profitSumTotal = null, custSumAmount = null;
            Map<String, String> profitMap = billService.queryAllAmount(billDate);
            if (profitMap.size() > 0) {
                profitSumTotal = df.format(new BigDecimal(profitMap.get("profitAmount")));
                custSumAmount = df.format(new BigDecimal(profitMap.get("amountSum")));
            }
            String basePath = "/pic";
            resultMap.put("basePath", basePath);
            resultMap.put("datalist", list);
            resultMap.put("profitSumTotal", profitSumTotal);
            resultMap.put("custSumAmount", custSumAmount);
        }
        return new ResponseInfoAssemble().success(resultMap);
    }

    @RequestMapping(value = "/customerBill/queryExport", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo billExport(CustomerBillQueryParam param, String export_type,HttpServletResponse response) throws IOException {
        LoginUser loginUser = opUser();
        String billDate = param.getBillDate();
        Map<String, Object> resultMap = new HashMap<>(16);
        if (Constant.ROLE_USER.equals(loginUser.getRole()) || Constant.ADMIN.equals(loginUser.getRole())) {
            List<Map<String,Object>> list = billService.queryCustomerBill(param);
            Map<String,Object> map;
            if (list != null) {
                for (int i = 0; i < list.size(); i++) {
                    map = list.get(i);
                    if (map != null && map.get("cust_id") != null) {
                        String custId = String.valueOf(map.get("cust_id"));
                        if (StringUtil.isNotEmpty(custId)) {
                            Map<String, String> amountMap = billService.queryCustomerConsumeTotal(custId, billDate);
                            String amountSum = null, profitAmount = null, supAmountSum = null;
                            if (amountMap != null && amountMap.size() > 0) {
                                amountSum = amountMap.get("amountSum");
                                profitAmount = amountMap.get("profitAmount");
                                supAmountSum = amountMap.get("supAmountSum");
                                map.put("amountSum", amountSum);
                                map.put("profit", profitAmount);
                                map.put("supAmountSum", supAmountSum);
                            }

                        }
                    }
                }
            }
            //将列表信息导出为excel
            List<String> header = new ArrayList<>();
            List<List<Object>> data = new ArrayList<>();
            header.add("企业ID");
            header.add("企业名称");
            header.add("企业账号");
            header.add("账号状态");
            header.add("交易金额(元)");
            if("2".equals(export_type)){
                header.add("交易成本(元)");
                header.add("利润(元)");
            }
            List<Map<String,Object>> dataList = list;
            List<Object> rowList;
            for (Map<String, Object> column : dataList) {
                rowList = new ArrayList<>();
                rowList.add(column.get("cust_id") != null ? column.get("cust_id") : "");
                rowList.add(column.get("enterprise_name") != null ? column.get("enterprise_name") : "");
                rowList.add(column.get("account") != null ? column.get("account") : "");
                String status = String.valueOf(column.get("status"));
                if("0".equals(status)){
                    rowList.add("正常");
                }else {
                    rowList.add("冻结");
                }
                rowList.add(column.get("amountSum") != null ? column.get("amountSum") : "");
                if("2".equals(export_type)){
                    rowList.add(column.get("supAmountSum") != null ? column.get("supAmountSum") : "");
                    rowList.add(column.get("profit") != null ? column.get("profit") : "");
                }
                data.add(rowList);
            }
            //下载的response属性设置
            response.setCharacterEncoding("utf-8");
//        response.setContentType("application/force-download");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "账单.xlsx";
            ////保存的文件名,必须和页面编码一致,否则乱码
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            OutputStream outputStream = response.getOutputStream();


            ExcelUtils.getInstance().exportObjects2Excel(data, header, outputStream);
            outputStream.flush();
            outputStream.close();

            return new ResponseInfoAssemble().success(null);
        } else {
            return new ResponseInfoAssemble().failure(-1, "无权限导出");
        }
    }

    /*
     *
     * 后台 账单管理--供应商账单首页
     * */
    @RequestMapping(value = "/supplierBill/query", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo supplierBillQuery(@Valid PageParam page, BindingResult error, SupplierBillQueryParam param) {
        if (error.hasFieldErrors()) {
            return new ResponseInfoAssemble().failure(-1, "缺少必要参数");
        }
        LoginUser lu = opUser();
        Map<String, Object> resultMap = new HashMap<>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            Page data = billService.querySupplierBill(page, param);
            BigDecimal amountSumBigDecimal = new BigDecimal("0");
            BigDecimal amountBigDecimal;
            List<Map<String, Object>> supplierList = null;
            DecimalFormat df = new DecimalFormat("0.00");
            if (data != null) {
                supplierList = data.getData();
                if (supplierList.size() > 0) {
                    for (int i = 0; i < supplierList.size(); i++) {
                        if (StringUtil.isNotEmpty(String.valueOf(supplierList.get(i).get("amountSum"))) && !"null".equals(String.valueOf(supplierList.get(i).get("amountSum")))) {
                            amountBigDecimal = new BigDecimal(String.valueOf(supplierList.get(i).get("amountSum")));
                            amountSumBigDecimal = amountSumBigDecimal.add(amountBigDecimal);
                        }
                    }
                }
            }
            String amountSumTotal = df.format(amountSumBigDecimal);
            resultMap.put("list", data);
            resultMap.put("amountSumTotal", amountSumTotal);
        }
        return new ResponseInfoAssemble().success(resultMap);
    }

    /*
     *
     * 账单管理--企业账单明细
     * */
    @RequestMapping(value = "/customerBilldetail", method = RequestMethod.GET)
    @ResponseBody
    public Object listBillDetails(@Valid PageParam page, BindingResult error, CustomerBillQueryParam param) throws Exception {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        LoginUser lu = opUser();
        Page list = null;
        Page listChannel = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = billService.listBillDetail(page, param);
        } else {
            String custId = opUser().getCustId();
            param.setCustomerId(custId);
            list = billService.listBillDetail(page, param);
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("basePath", "/pic");
        resultMap.put("datalist", list);
        return JSON.toJSONString(resultMap);
    }


    /*
     *
     *后台 账单管理--供应商账单明细
     * */
    @RequestMapping(value = "/supplierBilldetail", method = RequestMethod.GET)
    @ResponseBody
    public Object listSupplierBillDetails(@Valid PageParam page, BindingResult error, SupplierBillQueryParam param) throws Exception {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        Map<String, Object> resultMap = new HashMap<>();
        LoginUser lu = opUser();
        Page pageData = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            pageData = billService.listSupplierBillDetail(page, param);
        }
        resultMap.put("list", pageData.getData());
        resultMap.put("total", pageData.getTotal());
        resultMap.put("basePath", "/pic");
        return JSON.toJSONString(resultMap);
    }


    /*
     * 账单管理--导出企业账单明细
     * */
    @RequestMapping(value = "/exportCustomerBill", method = RequestMethod.GET, produces = "text/html;charset=UTF-8")
    @ResponseBody
    public Object exportCustomerBill(CustomerBillQueryParam param, HttpServletResponse response) {
        return billService.exportCustomerBill(param, response);
    }


    /*
     *
     *后台 账单管理--导出供应商账单明细
     * */
    @RequestMapping(value = "/exportSupperlierBill", method = RequestMethod.GET, produces = "application/vnd.ms-excel;charset=UTF-8")
    @ResponseBody
    public Object exportSupperlierBill(SupplierBillQueryParam param, HttpServletResponse response) {
        return billService.exportSupperlierBill(param, response);
    }

    /*
     *
     * 账单页(前后台)
     * */
    @RequestMapping(value = "/listBillMessage", method = RequestMethod.GET)
    @ResponseBody
    public Object listBillMessage(CustomerBillQueryParam param) {
        List<Map<String, Object>> customerBillList = null;
        LoginUser lu = opUser();
        Map<String, Object> resultMap = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            resultMap = billService.listBillMessage(param, lu.getUserType());
        } else {
            String custId = opUser().getCustId();
            param.setCustomerId(custId);
            resultMap = billService.listBillMessage(param, lu.getUserType());
        }
        resultMap.put("datalist", customerBillList);
        return JSON.toJSONString(resultMap);
    }


    /*
     *供应商账单分类列表展示
     */
    @RequestMapping(value = "/listBillsupplier", method = RequestMethod.GET)
    @ResponseBody
    public Object listBillsupplier(SupplierBillQueryParam param) {
        List<Map<String, Object>> customerBillList = null;
        LoginUser lu = opUser();
        Map<String, Object> resultMap = null;
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            resultMap = billService.listBillSupplier(param);
        }
        return JSON.toJSONString(resultMap);
    }

    /*
     *
     *查询企业和供应商月份账单金额
     * */
    @RequestMapping(value = "/queryBill", method = RequestMethod.GET)
    @ResponseBody
    public Object queryBill(String billDate, String custId, String type) {
        List<Map<String, Object>> resultMap = billService.queryBillList(billDate, custId, type);
        return JSON.toJSONString(resultMap);
    }

    /**
     * 导出结算单
     *
     * @return
     */
    @RequestMapping(value = "/exportSettlement", method = RequestMethod.GET)
    public void exportSettlementBill(HttpServletResponse response, String custId, String billDate) {
        billService.exportSettlementBill(response, custId, billDate);
    }

    /*
     *
     * 企业账单展示（根据批次进行展示）
     * */
    @RequestMapping(value = "/listCustomerBill", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo getListCustomerBill(@RequestBody CustomerBillQueryParam param) {
        LoginUser lu = opUser();
        Page page = null;
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                page = billService.getListCustomerBill(param);
            } else {
                String custId = opUser().getCustId();
                param.setCustomerId(custId);
                page = billService.getListCustomerBill(param);
            }
        } catch (Exception e) {
            logger.error("查询账单异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询账单失败");
        }
        return new ResponseInfoAssemble().success(page);
    }
    @RequestMapping(value = "/listCustomerBillExport",method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo listCustomerBillExport(CustomerBillQueryParam param,String export_type,HttpServletResponse response){
        LoginUser lu = opUser(); //TODO hello
        List<Map<String,Object>> list;
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                list = billService.listCustomerBillExport(param);
            } else {
                String custId = opUser().getCustId();
                param.setCustomerId(custId);
                list = billService.listCustomerBillExport(param);
            }

            //将列表信息导出为excel
            List<String> header = new ArrayList<>();
            List<List<Object>> data = new ArrayList<>();
            header.add("批次编号");
            header.add("批次名称");
            header.add("上传时间");
            if("1".equals(export_type)){
                header.add("发送数量");
            }
            header.add("交易金额(元)");
            if("2".equals(export_type)){
                header.add("交易成本(元)");
                header.add("利润(元)");
            }
            List<Map<String,Object>> dataList = list;
            List<Object> rowList;
            for (Map<String, Object> column : dataList) {
                rowList = new ArrayList<>();
                rowList.add(column.get("batchId") != null ? column.get("batchId") : "");
                rowList.add(column.get("batchName") != null ? column.get("batchName") : "");
                rowList.add(column.get("uploadTime") != null ? column.get("uploadTime") : "");
                if("1".equals(export_type)){
                    rowList.add(column.get("fixNumber")!=null?column.get("fixNumber"):"");
                }
                rowList.add(column.get("amount") != null ? column.get("amount") : "");
                if("2".equals(export_type)){
                    rowList.add(column.get("prodAmount") != null ? column.get("prodAmount") : "");
                    rowList.add(column.get("profitAmount") != null ? column.get("profitAmount") : "");
                }
                data.add(rowList);
            }
            //下载的response属性设置
            response.setCharacterEncoding("utf-8");
//        response.setContentType("application/force-download");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "账单.xlsx";
            ////保存的文件名,必须和页面编码一致,否则乱码
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            OutputStream outputStream = response.getOutputStream();


            ExcelUtils.getInstance().exportObjects2Excel(data, header, outputStream);
            outputStream.flush();
            outputStream.close();

            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            logger.error("查询账单异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询账单失败");
        }
    }

    /*
     *
     * 企业账单详情页
     * */
    @RequestMapping(value = "/getBillDetail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo getBillDetailList(@RequestBody CustomerBillQueryParam param) {
        LoginUser lu = opUser();
        Page page = null;
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                page = billService.getBillDetailList(param);
            } else {
                String custId = opUser().getCustId();
                param.setCustomerId(custId);
                page = billService.getBillDetailList(param);
            }
        } catch (Exception e) {
            logger.error("查询账单详情异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询账单详情失败");
        }
        return new ResponseInfoAssemble().success(page);
    }
    @RequestMapping(value = "/getBillDetailExport",method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo getBillDetailExport(CustomerBillQueryParam param,HttpServletResponse response){
        LoginUser lu = opUser();
        List<Map<String,Object>> page = null;
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                page = billService.getBillDetailExport(param);
            } else {
                String custId = opUser().getCustId();
                param.setCustomerId(custId);
                page = billService.getBillDetailExport(param);
            }

            //将列表信息导出为excel
            List<String> header = new ArrayList<>();
            List<List<Object>> data = new ArrayList<>();
            header.add("快递ID");
            header.add("收件人ID");
            header.add("收件地址");
            header.add("数据渠道");
            header.add("快递渠道");
            header.add("发送时间");
            header.add("交易金额（元）");
            header.add("数据成本（元）");
            header.add("快递成本（元）");
            header.add("交易利润（元）");
            List<Map<String,Object>> dataList = page;
            List<Object> rowList;
            for (Map<String, Object> column : dataList) {
                rowList = new ArrayList<>();
                rowList.add(column.get("expressId") != null ? column.get("expressId") : "");
                rowList.add(column.get("peopleId") != null ? column.get("peopleId") : "");
                rowList.add(column.get("address") != null ? column.get("address") : "");
                rowList.add(column.get("fixSupplier") != null ? column.get("fixSupplier") : "");
                rowList.add(column.get("expressSupplier") != null ? column.get("expressSupplier") : "");
                rowList.add(column.get("sendTime") != null ? column.get("sendTime") : "");
                rowList.add(column.get("sumAmount") != null ? column.get("sumAmount") : "");
                rowList.add(column.get("prodAmount") != null ? column.get("prodAmount") : "");
                rowList.add(column.get("expressAmount") != null ? column.get("expressAmount") : "");
                rowList.add(column.get("profit") != null ? column.get("profit") : "");
                data.add(rowList);
            }
            //下载的response属性设置
            response.setCharacterEncoding("utf-8");
//        response.setContentType("application/force-download");
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            String fileName = "账单.xlsx";
            ////保存的文件名,必须和页面编码一致,否则乱码
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            OutputStream outputStream = response.getOutputStream();


            ExcelUtils.getInstance().exportObjects2Excel(data, header, outputStream);
            outputStream.flush();
            outputStream.close();

            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            logger.error("查询账单详情异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询账单详情失败");
        }
    }


    /*
     *
     * 供应商账单详情页（名址修复系统）
     * */
    @RequestMapping(value = "/getSupBillDetail", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo getSupBillDetailList(@RequestBody CustomerBillQueryParam param) {
        LoginUser lu = opUser();
        Page page = null;
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                page = billService.getSupBillDetailList(param);
            }
        } catch (Exception e) {
            logger.error("查询账单详情异常", e);
            return new ResponseInfoAssemble().failure(-1, "查询账单详情失败");
        }
        return new ResponseInfoAssemble().success(page);
    }
}
