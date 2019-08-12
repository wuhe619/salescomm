package com.bdaim.bill.service;

import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.dto.SupplierBillQueryParam;
import com.bdaim.common.dto.PageParam;
import com.bdaim.rbac.dto.Page;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/10/8 10:37
 */
public interface BillService {
    Page queryCustomerBill(PageParam page, CustomerBillQueryParam param);

    Page querySupplierBill(PageParam page, SupplierBillQueryParam param);

    Page listBillDetail(PageParam page, CustomerBillQueryParam param) throws Exception;

    /**
     * 账单三级页导出功能
     */
    Object exportCustomerBill(CustomerBillQueryParam param, HttpServletResponse response);

    Object exportSupperlierBill(SupplierBillQueryParam param, HttpServletResponse response);

    Page listSupplierBillDetail(PageParam page, SupplierBillQueryParam param);

    Map<String, String> queryCustomerConsumeTotal(String custId, String billDate);


    /**
     * 账单列表（企业前后台）
     *
     * @param param
     */
    Map<String, Object> listBillMessage(CustomerBillQueryParam param, String userType);

    /**
     * 供应商账单展示
     *
     * @param param
     */
    Map<String, Object> listBillSupplier(SupplierBillQueryParam param);

    /**
     * 查询企业和供应商月份账单金额
     */
    List<Map<String, Object>> queryBillList(String billDate, String custId, String type);

    /**
     * 查询企业账单，不带分页
     * @param param
     * @return
     */
    List<Map<String, Object>> queryCustomerBill(CustomerBillQueryParam param);

    /**
     * 利润明细导出
     *
     * @param param
     * @return
     */
    List<Map<String,Object>> getBillDetailExport(CustomerBillQueryParam param);
}
