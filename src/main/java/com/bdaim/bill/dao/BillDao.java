package com.bdaim.bill.dao;

import com.bdaim.bill.dto.TransactionTypeEnum;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/12/20
 * @description
 */
@Component
public class BillDao extends SimpleHibernateDao {

    /**
     * 查询供应商单个月份的消费金额
     *
     * @param supplierId
     * @param yearMonth
     * @return
     */
    public double sumSupplierMonthAmount(String supplierId, String yearMonth) {
        double amount = 0.0;
        String sql = "SELECT SUM(prod_amount)/1000 amount FROM stat_bill_month WHERE resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = ?) AND stat_time = ?";
        List<Map<String, Object>> supplierAmount = this.sqlQuery(sql, supplierId, yearMonth);
        if (supplierAmount.size() > 0 && supplierAmount.get(0).get("amount") != null) {
            amount = NumberConvertUtil.parseDouble(String.valueOf(supplierAmount.get(0).get("amount")));
        }
        return amount;
    }

    /**
     * 查询客户单个月份的消费金额
     *
     * @param custId
     * @param yearMonth
     * @return
     */
    public double sumCustomerMonthAmount(String custId, String yearMonth) {
        double amount = 0.0;
        String sql = "SELECT SUM(amount)/1000 amount FROM stat_bill_month WHERE cust_id = ? AND stat_time = ?";
        List<Map<String, Object>> supplierAmount = this.sqlQuery(sql, custId, yearMonth);
        if (supplierAmount.size() > 0 && supplierAmount.get(0).get("amount") != null) {
            amount = NumberConvertUtil.parseDouble(String.valueOf(supplierAmount.get(0).get("amount")));
        }
        return amount;
    }

    public Page pageCustomerBill(String custId, String startTime, String endTime, String orderNo, int type, int pageNum, int pageSize, String resourceId) {
        TransactionTypeEnum transactionTypeEnum = TransactionTypeEnum.getType(type);
        Page page = null;
        switch (transactionTypeEnum) {
            // 统一查询交易表记录
            case BALANCE_RECHARGE:
            case BALANCE_DEDUCTION:
            case SEAT_DEDUCTION:
            case APPARENT_NUM_DEDUCTION:
            case B2B_TC_DEDUCTION:
                page = pageTransactionLog("", custId, resourceId, orderNo, startTime, endTime, type, pageNum, pageSize);
                break;
            // 短信扣费记录
            case SMS_DEDUCTION:
                page = pageSmsTransactionLog("", custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;
            // 通话扣费记录
            case CALL_DEDUCTION:
                page = pageCallTransactionLog("", custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;
            //客户群(数据)扣费记录
            case LABEL_DEDUCTION:
                page = pageLabelDataTransactionLog("", custId, "", orderNo, startTime, endTime, pageNum, pageSize);
                break;
            default:
                page = pageTransactionLog("", custId, resourceId, orderNo, startTime, endTime, type, pageNum, pageSize);
                break;
        }
        if (page == null) {
            page = new Page();
        }
        if (page != null && page.getData() != null) {
            Map<String, Object> m;
            for (int i = 0; i < page.getData().size(); i++) {
                m = (Map<String, Object>) page.getData().get(i);
                m.put("type", type);
            }
        }
        return page;
    }

    /**
     * 统计客户消费总金额
     *
     * @param custId
     * @param startTime
     * @param endTime
     * @param orderNo
     * @param type
     * @param pageNum
     * @param pageSize
     * @param resourceId
     * @return
     */
    public Map<String, Object> statCustomerBillAmount(String custId, String startTime, String endTime, String orderNo, int type, int pageNum, int pageSize, String resourceId) {
        TransactionTypeEnum transactionTypeEnum = TransactionTypeEnum.getType(type);
        Map<String, Object> data = null;
        switch (transactionTypeEnum) {
            // 统一查询交易表记录
            case BALANCE_RECHARGE:
            case BALANCE_DEDUCTION:
            case SEAT_DEDUCTION:
            case APPARENT_NUM_DEDUCTION:
            case B2B_TC_DEDUCTION:
                data = statTransactionAmount("", custId, resourceId, orderNo, startTime, endTime, type, pageNum, pageSize);
                break;
            // 短信扣费记录
            case SMS_DEDUCTION:
                data = statSmsTransactionAmount("", custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;
            // 通话扣费记录
            case CALL_DEDUCTION:
                data = statCallTransactionAmount("", custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;
            //客户群(数据)扣费记录
            case LABEL_DEDUCTION:
                data = statLabelDataTransactionAmount("", custId, "", orderNo, startTime, endTime, pageNum, pageSize);
                break;

        }
        return data;
    }

    public Page pageSupplierBill(String supplierId, String resourceId, int type, String orderNo, String custId, String startTime, String endTime, int pageNum, int pageSize) {
        TransactionTypeEnum transactionTypeEnum = TransactionTypeEnum.getType(type);
        Page page = null;
        switch (transactionTypeEnum) {
            // 统一查询交易表记录
            case BALANCE_RECHARGE:
            case BALANCE_DEDUCTION:
            case SEAT_DEDUCTION:
            case APPARENT_NUM_DEDUCTION:
            case B2B_TC_DEDUCTION:
                page = pageTransactionLog(supplierId, custId, resourceId, orderNo, startTime, endTime, type, pageNum, pageSize);
                break;
            // 短信扣费记录
            case SMS_DEDUCTION:
                page = pageSmsTransactionLog(supplierId, custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;
            // 通话扣费记录
            case CALL_DEDUCTION:
                page = pageCallTransactionLog(supplierId, custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;
            //客户群(数据)扣费记录
            case LABEL_DEDUCTION:
                page = pageLabelDataTransactionLog(supplierId, custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;
            default:
                page = pageTransactionLog(supplierId, custId, resourceId, orderNo, startTime, endTime, type, pageNum, pageSize);
                break;

        }
        if (page == null) {
            page = new Page();
        }
        return page;
    }

    /**
     * 统计供应商消费总金额
     *
     * @param supplierId
     * @param resourceId
     * @param type
     * @param orderNo
     * @param custId
     * @param startTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Map<String, Object> statSupplierBillAmount(String supplierId, String resourceId, int type, String orderNo, String custId, String startTime, String endTime, int pageNum, int pageSize) {
        TransactionTypeEnum transactionTypeEnum = TransactionTypeEnum.getType(type);
        Map<String, Object> data = null;
        switch (transactionTypeEnum) {
            // 统一查询交易表记录
            case BALANCE_RECHARGE:
            case BALANCE_DEDUCTION:
            case SEAT_DEDUCTION:
            case APPARENT_NUM_DEDUCTION:
            case B2B_TC_DEDUCTION:
                data = statTransactionAmount(supplierId, custId, resourceId, orderNo, startTime, endTime, type, pageNum, pageSize);
                break;
            // 短信扣费记录
            case SMS_DEDUCTION:
                data = statSmsTransactionAmount(supplierId, custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;
            // 通话扣费记录
            case CALL_DEDUCTION:
                data = statCallTransactionAmount(supplierId, custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;
            //客户群(数据)扣费记录
            case LABEL_DEDUCTION:
                data = statLabelDataTransactionAmount(supplierId, custId, resourceId, orderNo, startTime, endTime, pageNum, pageSize);
                break;

        }
        return data;
    }


    public List listCustomerBill(String custId, String startTime, String endTime, String orderNo, int type) {
        TransactionTypeEnum transactionTypeEnum = TransactionTypeEnum.getType(type);
        List list = null;
        switch (transactionTypeEnum) {
            // 统一查询交易表记录
            case BALANCE_RECHARGE:
            case BALANCE_DEDUCTION:
            case SEAT_DEDUCTION:
            case APPARENT_NUM_DEDUCTION:
            case B2B_TC_DEDUCTION:
                list = listTransactionLog("", custId, "", orderNo, startTime, endTime, type);
                break;
            // 短信扣费记录
            case SMS_DEDUCTION:
                list = listSmsTransactionLog("", custId, "", orderNo, startTime, endTime);
                break;
            // 通话扣费记录
            case CALL_DEDUCTION:
                list = listCallTransactionLog("", custId, "", orderNo, startTime, endTime);
                break;
            //客户群(数据)扣费记录
            case LABEL_DEDUCTION:
                list = listLabelDataTransactionLog("", custId, "", orderNo, startTime, endTime);
                break;

        }
        return list;
    }

    public List listSupplierBill(String supplierId, String resourceId, int type, String orderNo, String custId, String startTime, String endTime) {
        TransactionTypeEnum transactionTypeEnum = TransactionTypeEnum.getType(type);
        List page = null;
        switch (transactionTypeEnum) {
            // 统一查询交易表记录
            case BALANCE_RECHARGE:
            case BALANCE_DEDUCTION:
            case SEAT_DEDUCTION:
            case APPARENT_NUM_DEDUCTION:
            case B2B_TC_DEDUCTION:
                page = listTransactionLog(supplierId, custId, resourceId, orderNo, startTime, endTime, type);
                break;
            // 短信扣费记录
            case SMS_DEDUCTION:
                page = listSmsTransactionLog(supplierId, custId, resourceId, orderNo, startTime, endTime);
                break;
            // 通话扣费记录
            case CALL_DEDUCTION:
                page = listCallTransactionLog(supplierId, custId, resourceId, orderNo, startTime, endTime);
                break;
            //客户群(数据)扣费记录
            case LABEL_DEDUCTION:
                page = listLabelDataTransactionLog(supplierId, custId, resourceId, orderNo, startTime, endTime);
                break;

        }
        return page;
    }

    /**
     * 查询交易记录月表记录
     *
     * @param custId
     * @param startTime
     * @param endTime
     * @param type
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page pageTransactionLog(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime, int type, int pageNum, int pageSize) {
        LocalDateTime startLocalDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String yearMonth = startLocalDateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t.transaction_id transactionId, t.cust_id custId, t.type, t.pay_mode payMode, t.amount/1000 amount, t.remark, t.create_time createTime, " +
                " t.supplier_id supplierId, t.prod_amount/1000 prodAmount, t.resource_id resourceId,t.certificate ")
                .append(" FROM t_transaction_" + yearMonth + " t WHERE t.type = ? ");
        sql.append(" AND t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = '" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.supplier_id IN(SELECT supplier_id FROM t_market_resource WHERE resource_id = '" + resourceId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.transaction_id = '").append(orderNo).append("'");
        }
        sql.append(" ORDER BY t.create_time DESC ");
        Page page = this.sqlPageQuery0(sql.toString(), pageNum, pageSize, type, startTime, endTime);
        return page;
    }

    /**
     * 统计交易记录客户消费总金额和供应商总金额
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @param type
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Map<String, Object> statTransactionAmount(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime, int type, int pageNum, int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("sumAmount", 0);
        data.put("sumProdAmount", 0);
        LocalDateTime startLocalDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String yearMonth = startLocalDateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SUM(`amount`)/1000 sumAmount, SUM(`prod_amount`)/1000 sumProdAmount")
                .append(" FROM t_transaction_" + yearMonth + " t WHERE t.type = ? ");
        sql.append(" AND t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = '" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.supplier_id IN(SELECT supplier_id FROM t_market_resource WHERE resource_id = '" + resourceId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.transaction_id = '").append(orderNo).append("'");
        }
        sql.append(" ORDER BY t.create_time DESC ");
        List<Map<String, Object>> list = this.sqlQuery(sql.toString(), type, startTime, endTime);
        if (list != null && list.size() > 0) {
            data.put("sumAmount", list.get(0).get("sumAmount"));
            data.put("sumProdAmount", list.get(0).get("sumProdAmount"));
        }
        return data;
    }

    /**
     * 通话交易记录查询
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page pageCallTransactionLog(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime, int pageNum, int pageSize) {
        LocalDateTime startLocalDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String yearMonth = startLocalDateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t.touch_id transactionId, t.cust_id custId, t.user_id userId, t.pay_mode payMode, t.amount/1000 amount, t.remark, t.create_time createTime,t.prod_amount/1000 prodAmount, t.resource_id resourceId, t.called_duration calledDuration ")
                .append(" FROM t_touch_voice_log_" + yearMonth + " t WHERE ");
        sql.append(" t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.resource_id = '").append(resourceId).append("'");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = '" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.touch_id = '").append(orderNo).append("'");
        }
        sql.append(" ORDER BY t.create_time DESC ");
        Page page = this.sqlPageQuery0(sql.toString(), pageNum, pageSize, startTime, endTime);
        return page;
    }

    /**
     * 统计通话记录客户消费总金额和供应商总金额
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Map<String, Object> statCallTransactionAmount(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime, int pageNum, int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("sumAmount", 0);
        data.put("sumProdAmount", 0);
        LocalDateTime startLocalDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String yearMonth = startLocalDateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SUM(`amount`)/1000 sumAmount, SUM(`prod_amount`)/1000 sumProdAmount ")
                .append(" FROM t_touch_voice_log_" + yearMonth + " t WHERE ");
        sql.append(" t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.resource_id = '").append(resourceId).append("'");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = '" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.touch_id = '").append(orderNo).append("'");
        }
        sql.append(" ORDER BY t.create_time DESC ");
        List<Map<String, Object>> list = this.sqlQuery(sql.toString(), startTime, endTime);
        if (list != null && list.size() > 0) {
            data.put("sumAmount", list.get(0).get("sumAmount"));
            data.put("sumProdAmount", list.get(0).get("sumProdAmount"));
        }
        return data;
    }

    /**
     * 短信交易记录查询
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page pageSmsTransactionLog(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime, int pageNum, int pageSize) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t.touch_id transactionId, t.cust_id custId, t.user_id userId, t.amount/1000 amount, t.create_time createTime, t.prod_amount/1000 prodAmount, t.resource_id resourceId ")
                .append(" FROM t_touch_sms_log t WHERE ");
        sql.append(" t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.resource_id = '").append(resourceId).append("'");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = '" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.touch_id = '").append(orderNo).append("'");
        }
        sql.append(" ORDER BY t.create_time DESC ");
        Page page = this.sqlPageQuery0(sql.toString(), pageNum, pageSize, startTime, endTime);
        return page;
    }

    /**
     * 统计短信发送记录客户消费总金额和供应商总金额
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Map<String, Object> statSmsTransactionAmount(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime, int pageNum, int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("sumAmount", 0);
        data.put("sumProdAmount", 0);
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SUM(`amount`)/1000 sumAmount, SUM(`prod_amount`)/1000 sumProdAmount ")
                .append(" FROM t_touch_sms_log t WHERE ");
        sql.append(" t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.resource_id = '").append(resourceId).append("'");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = '" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.touch_id = '").append(orderNo).append("'");
        }
        sql.append(" ORDER BY t.create_time DESC ");
        List<Map<String, Object>> list = this.sqlQuery(sql.toString(), startTime, endTime);
        if (list != null && list.size() > 0) {
            data.put("sumAmount", list.get(0).get("sumAmount"));
            data.put("sumProdAmount", list.get(0).get("sumProdAmount"));
        }
        return data;
    }

    /**
     * 查询数据提取(标签)费用
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page pageLabelDataTransactionLog(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime, int pageNum, int pageSize) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t.order_id transactionId, t.id, t.cust_id custId, t2.amount/1000 amount, t2.cost_price/1000 prodAmount, t2.amount/t.user_count/1000 price,  t2.cost_price/t.user_count/1000 cPrice, t.remark, t.industry_pool_id, t.create_time createTime, t.user_count userCount ")
                .append(" FROM customer_group t  ")
                .append(" JOIN t_order t2 ON t.order_id = t2.order_id")
                .append(" WHERE t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.industry_pool_id = (SELECT industry_pool_id FROM t_industry_pool WHERE source_id = '" + resourceId + "') ");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.industry_pool_id IN(SELECT industry_pool_id FROM t_industry_pool t1 JOIN t_market_resource t2 ON t1.source_id = t2.resource_id WHERE t2.supplier_id ='" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.order_id = '").append(orderNo).append("'");
        }
        sql.append(" ORDER BY t.create_time DESC");
        Page page = this.sqlPageQuery0(sql.toString(), pageNum, pageSize, startTime, endTime);
        return page;
    }

    /**
     * 统计数据提取客户消费总金额和供应商总金额
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Map<String, Object> statLabelDataTransactionAmount(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime, int pageNum, int pageSize) {
        Map<String, Object> data = new HashMap<>();
        data.put("sumAmount", 0);
        data.put("sumProdAmount", 0);
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT SUM(t2.amount)/1000 sumAmount, SUM(t2.cost_price)/1000 sumProdAmount ")
                .append(" FROM customer_group t  ")
                .append(" JOIN t_order t2 ON t.order_id = t2.order_id")
                .append(" WHERE t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.industry_pool_id = (SELECT industry_pool_id FROM t_industry_pool WHERE source_id = '" + resourceId + "') ");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.industry_pool_id IN(SELECT industry_pool_id FROM t_industry_pool t1 JOIN t_market_resource t2 ON t1.source_id = t2.resource_id WHERE t2.supplier_id ='" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.order_id = '").append(orderNo).append("'");
        }
        sql.append(" ORDER BY t.create_time DESC");
        List<Map<String, Object>> list = this.sqlQuery(sql.toString(), startTime, endTime);
        if (list != null && list.size() > 0) {
            data.put("sumAmount", list.get(0).get("sumAmount"));
            data.put("sumProdAmount", list.get(0).get("sumProdAmount"));
        }
        return data;
    }

    /**
     * 查询交易记录月表记录
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @param type
     * @return
     */
    public List listTransactionLog(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime, int type) {
        LocalDateTime startLocalDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String yearMonth = startLocalDateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t.transaction_id transactionId, t.cust_id custId, t.type, t.pay_mode payMode, t.amount/1000 amount, t.remark, t.create_time createTime, " +
                " t.supplier_id supplierId, t.prod_amount/1000 prodAmount, t.resource_id resourceId,t.certificate, t.user_id userId")
                .append(" FROM t_transaction_" + yearMonth + " t WHERE t.type = ? ");
        sql.append(" AND t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = '" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.supplier_id IN(SELECT supplier_id FROM t_market_resource WHERE resource_id = '" + resourceId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.transaction_id = '").append(orderNo).append("'");
        }
        List list = this.sqlQuery(sql.toString(), type, startTime, endTime);
        return list;
    }

    /**
     * 通话交易记录查询
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @return
     */
    public List listCallTransactionLog(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime) {
        LocalDateTime startLocalDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        String yearMonth = startLocalDateTime.format(DateTimeFormatter.ofPattern("yyyyMM"));
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t.touch_id transactionId, t.cust_id custId, 4 type, t.pay_mode payMode, t.amount/1000 amount, t.remark, t.create_time createTime,t.prod_amount/1000 prodAmount, t.resource_id resourceId, t.called_duration calledDuration  ")
                .append(" FROM t_touch_voice_log_" + yearMonth + " t WHERE ");
        sql.append(" t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.resource_id = '").append(resourceId).append("'");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = '" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.touch_id = '").append(orderNo).append("'");
        }
        List list = this.sqlQuery(sql.toString(), startTime, endTime);
        return list;
    }

    /**
     * 短信交易记录查询
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @return
     */
    public List listSmsTransactionLog(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t.touch_id transactionId, t.cust_id custId, 3 type, t.amount/1000 amount, t.create_time createTime, t.prod_amount/1000 prodAmount, t.resource_id resourceId ")
                .append(" FROM t_touch_sms_log t WHERE ");
        sql.append(" t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.resource_id = '").append(resourceId).append("'");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.resource_id IN(SELECT resource_id FROM t_market_resource WHERE supplier_id = '" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.touch_id = '").append(orderNo).append("'");
        }
        List list = this.sqlQuery(sql.toString(), startTime, endTime);
        return list;
    }

    /**
     * 查询数据提取(标签)费用
     *
     * @param supplierId
     * @param custId
     * @param resourceId
     * @param orderNo
     * @param startTime
     * @param endTime
     * @return
     */
    public List listLabelDataTransactionLog(String supplierId, String custId, String resourceId, String orderNo, String startTime, String endTime) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t.id, t.order_id transactionId, t.cust_id custId, 7 type, t.amount/1000 amount, t.remark, t.industry_pool_id, t.create_time createTime, t.user_count userCount ")
                .append(" FROM customer_group t WHERE ");
        sql.append(" t.create_time BETWEEN ? AND ?");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND t.cust_id = '").append(custId).append("'");
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sql.append(" AND t.industry_pool_id = (SELECT industry_pool_id FROM t_industry_pool WHERE source_id = '" + resourceId + "') ");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" AND t.industry_pool_id IN(SELECT industry_pool_id FROM t_industry_pool t1 JOIN t_market_resource t2 ON t1.source_id = t2.resource_id WHERE t2.supplier_id ='" + supplierId + "') ");
        }
        if (StringUtil.isNotEmpty(orderNo)) {
            sql.append(" AND t.touch_id = '").append(orderNo).append("'");
        }
        List list = this.sqlQuery(sql.toString(), startTime, endTime);
        return list;
    }
}
