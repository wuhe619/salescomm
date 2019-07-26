package com.bdaim.bill.service.impl;

import com.alibaba.fastjson.JSON;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.dto.SupplierBillQueryParam;
import com.bdaim.bill.service.BillService;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.common.util.page.PaginationThrowException;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerDO;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.supplier.dto.SupplierEnum;
import com.bdaim.supplier.entity.SupplierEntity;
import com.github.crab2died.ExcelUtils;
import com.github.crab2died.sheet.wrapper.SimpleSheetWrapper;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/10/8 10:46
 */
@Service("billService")
public class BillServiceImpl implements BillService {
    private static Log logger = LogFactory.getLog(BillServiceImpl.class);

    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    CustomerDao customerDao;
    @Resource
    SourceDao sourceDao;
    @Resource
    SupplierDao supplierDao;

    private final static DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    @Override
    public Page queryCustomerBill(PageParam page, CustomerBillQueryParam param) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t.cust_id,cus.enterprise_name,cus.status,\n" +
                "cu.account,cu.realname,cjc.mobile_num\n" +
                " from stat_bill_month t\n" +
                "LEFT JOIN t_customer cus ON t.cust_id= cus.cust_id\n" +
                "LEFT JOIN t_customer_user cu ON t.cust_id = cu.cust_id \n" +
                "LEFT JOIN (SELECT cust_id, \n" +
                "\tmax(CASE property_name WHEN 'mobile_num'   THEN property_value ELSE '' END ) mobile_num\n" +
                "   FROM t_customer_property p GROUP BY cust_id \n" +
                ") cjc ON t.cust_id = cjc.cust_id \n" +
                "where 1=1 and cu.user_type=1");
        if (StringUtil.isNotEmpty(param.getCustomerId())) {
            sqlBuilder.append(" and t.cust_id= " + param.getCustomerId());
        }
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            sqlBuilder.append(" and cus.enterprise_name like '%" + param.getEnterpriseName() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getAccount())) {
            sqlBuilder.append(" and cu.account like '%" + param.getAccount() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getRealname())) {
            sqlBuilder.append(" and cu.realname like '%" + param.getRealname() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getPhone())) {
            sqlBuilder.append(" and cjc.mobile_num like '%" + param.getPhone() + "%'");
        }
        sqlBuilder.append(" GROUP BY t.cust_id ");
        logger.info("查询后台账单sql" + sqlBuilder.toString());
        try {
            return new PaginationThrowException().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> querySupplierBill(PageParam page, SupplierBillQueryParam param) {
        String billDate = param.getBillDate();
        Map<String, String> map = new HashMap<>();
        logger.info("查询账单时间范围是：" + billDate);
        StringBuffer supBillSql = new StringBuffer("SELECT IFNULL(SUM(b.prod_amount),0) /100 amountSum FROM t_market_resource r LEFT JOIN stat_bill_month b ON r.resource_id = b.resource_id WHERE supplier_id =?");
        //查询全部
        if ("0".equals(billDate) || StringUtil.isEmpty(billDate)) {
            supBillSql = new StringBuffer("SELECT IFNULL(SUM(b.prod_amount),0) /100 amountSum FROM t_market_resource r LEFT JOIN stat_bill_month b ON r.resource_id = b.resource_id WHERE supplier_id =?");
            //查看一年
        } else if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            supBillSql.append(" AND stat_time>=" + billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            supBillSql.append(" AND stat_time>=" + billDate);
        } else {
            supBillSql.append(" AND stat_time=" + billDate);
        }
        //查询当前所有供应商
        String querySql = "SELECT s.supplier_id supplierId,s.`name` supplierName,s.create_time,s.contact_person person,s.contact_phone phone FROM t_supplier s";
        List<Map<String, Object>> supplierList = sourceDao.sqlQuery(querySql);
        if (supplierList.size() > 0) {
            for (int i = 0; i < supplierList.size(); i++) {
                String supplierId = String.valueOf(supplierList.get(i).get("supplierId"));
                if (StringUtil.isNotEmpty(supplierId)) {
                    //根据supplierId查询出消费金额
                    logger.info("查询供应商消费金额sql是：" + supBillSql.toString());
                    List<Map<String, Object>> countMoneyList = sourceDao.sqlQuery(supBillSql.toString(), supplierId);
                    if (countMoneyList.size() > 0) {
                        supplierList.get(i).put("amountSum", countMoneyList.get(0).get("amountSum"));
                    }
                }
            }
        }
        return supplierList;

    }


    @Override
    public Page listBillDetail(PageParam page, CustomerBillQueryParam param) {
        //查询账单sql
        String logListSql = getBillType(param.getType(), param.getBillDate(), param.getCustomerId(), param.getSupplierId(), param.getTransactionId(), param.getBatchId(), param.getEnterpriseName(), param.getStartTime(), param.getEndTime());
        Page pageData = new Pagination().getPageData(logListSql, null, page, jdbcTemplate);
        List<Map<String, Object>> list = pageData.getList();
        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
        LocalDateTime localDateTime;
        for (int i = 0; i < list.size(); i++) {
            if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("billDate"))) && list.get(i).get("billDate") != null) {
                localDateTime = LocalDateTime.parse(String.valueOf(list.get(i).get("billDate")), dateTimeFormatter);
                list.get(i).put("billDate", localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            }
            if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("supplier_id"))) && !"null".equals(String.valueOf(list.get(i).get("supplier_id")))) {
                //查询供应商名称
                SupplierEntity supplierEntity = supplierDao.getSupplierList(Integer.parseInt(String.valueOf(list.get(i).get("supplier_id"))));
                if (supplierEntity != null) {
                    list.get(i).put("source_name", supplierEntity.getName());
                }
            }
        }
        return pageData;
    }


    @Override
    public Object exportCustomerBill(CustomerBillQueryParam param, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 生成sheet数据
            List<SimpleSheetWrapper> list = new ArrayList<>();
            CustomerDO customer = customerDao.findUniqueBy("custId", param.getCustomerId());
            String enterprisename = "";
            if (customer != null) {
                enterprisename = customer.getEnterpriseName();
            }
            // 设置标题
            List<String> titles = new ArrayList<String>();
            titles.add("流水号");
            titles.add("企业名称");
            titles.add("批次ID");
            titles.add("交易事项");
            titles.add("交易时间");
            titles.add("消费金额");
            titles.add("操作人");
            String billName = "";
            //需要导出的账单类型 用，隔开
            String type = param.getType();
            if (StringUtil.isNotEmpty(type)) {
                String[] billTypes = type.split(",");
                if (billTypes.length > 0) {
                    for (int j = 0; j < billTypes.length; j++) {
                        //对象转换
                        String logListSql = getBillType(billTypes[j], param.getBillDate(), param.getCustomerId(), param.getSupplierId(), param.getTransactionId(), param.getBatchId(), param.getEnterpriseName(), param.getStartTime(), param.getEndTime());
                        List<Map<String, Object>> billlist = jdbcTemplate.queryForList(logListSql);
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
                        LocalDateTime localDateTime;
                        for (int i = 0; i < billlist.size(); i++) {
                            if (StringUtil.isNotEmpty(String.valueOf(billlist.get(i).get("billDate")))) {
                                localDateTime = LocalDateTime.parse(String.valueOf(billlist.get(i).get("billDate")), dateTimeFormatter);
                                billlist.get(i).put("billDate", localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            }
                        }
                        List<List<Object>> data = new ArrayList<>();

                        //根据type查询账单名字
                        billName = TransactionEnum.getName(Integer.parseInt(billTypes[j]));

                        List<Object> rowList;
                        for (Map<String, Object> column : billlist) {
                            rowList = new ArrayList<>();
                            rowList.add(column.get("transactionId") != null ? column.get("transactionId") : "");
                            rowList.add(enterprisename);
                            //批次ID
                            rowList.add(column.get("batchId") != null ? column.get("batchId") : "");
                            rowList.add(billName);
                            rowList.add(column.get("billDate") != null ? column.get("billDate") : "");
                            rowList.add(column.get("totalAmount") != null ? column.get("totalAmount") : "");
                            rowList.add(column.get("account") != null ? column.get("account") : "");
                            data.add(rowList);
                        }

                        list.add(new SimpleSheetWrapper(data, titles, billName));

                    }
                    if (list.size() > 0) {
                        String fileName = enterprisename + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
                        String fileType = ".xlsx";
                        response.setCharacterEncoding("utf-8");
                        response.setContentType("application/vnd.ms-excel;charset=utf-8");
                        response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                        OutputStream outputStream;
                        outputStream = response.getOutputStream();
                        ExcelUtils.getInstance().simpleSheet2Excel(list, outputStream);
                        outputStream.flush();
                        response.flushBuffer();
                        outputStream.close();
                        logger.info("供应商账单导出成功");
                    } else {
                        resultMap.put("code", "001");
                        resultMap.put("_message", "供应商账单无数据导出！");
                        return JSON.toJSONString(resultMap);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.error("供应商账单导出失败\t" + e.getMessage());
            resultMap.put("code", "002");
            resultMap.put("_message", "供应商账单导出失败！");
        }
        return JSON.toJSONString(resultMap);
    }

    @Override
    public Object exportSupperlierBill(SupplierBillQueryParam param, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 生成sheet数据
            List<SimpleSheetWrapper> list = new ArrayList<>();
            String supplierName = "";
            // 设置标题
            List<String> titles = new ArrayList<String>();
            titles.add("流水号");
            titles.add("企业名称");
            titles.add("批次ID");
            titles.add("交易事项");
            titles.add("交易时间");
            titles.add("消费金额");
            titles.add("操作人");
            String billName = "";
            //需要导出的账单类型 用，隔开
            String type = param.getType();
            if (StringUtil.isNotEmpty(type)) {
                String[] billTypes = type.split(",");
                if (billTypes.length > 0) {
                    for (int j = 0; j < billTypes.length; j++) {
                        //对象转换
                        String logListSql = getBillType(billTypes[j], param.getBillDate(), param.getCustId(), param.getSupplierId(), param.getTransActionId(), param.getBatchId(), param.getEnterpriseName(), param.getStartTime(), param.getEndTime());
                        List<Map<String, Object>> billlist = jdbcTemplate.queryForList(logListSql);
                        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
                        LocalDateTime localDateTime;
                        for (int i = 0; i < billlist.size(); i++) {
                            if (StringUtil.isNotEmpty(String.valueOf(billlist.get(i).get("billDate")))) {
                                localDateTime = LocalDateTime.parse(String.valueOf(billlist.get(i).get("billDate")), dateTimeFormatter);
                                billlist.get(i).put("billDate", localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                            }
                        }
                        List<List<Object>> data = new ArrayList<>();

                        //根据type查询账单名字
                        billName = TransactionEnum.getName(Integer.parseInt(billTypes[j]));
                        String supplierId = param.getSupplierId();

                        //查询供应商名称
                        SupplierEnum callIdPropertyName = SupplierEnum.getCallIdPropertyName(supplierId);
                        if (callIdPropertyName != null) {
                            supplierName = callIdPropertyName.getName();
                        }
                        List<Object> rowList;
                        for (Map<String, Object> column : billlist) {
                            rowList = new ArrayList<>();
                            rowList.add(column.get("transactionId") != null ? column.get("transactionId") : "");
                            rowList.add(column.get("enterprise_name") != null ? column.get("enterprise_name") : "");
                            //批次ID
                            rowList.add(column.get("batchId") != null ? column.get("batchId") : "");
                            rowList.add(billName);
                            rowList.add(column.get("billDate") != null ? column.get("billDate") : "");
                            rowList.add(column.get("prodAmount") != null ? column.get("prodAmount") : "");
                            rowList.add(column.get("account") != null ? column.get("account") : "");
                            data.add(rowList);
                        }

                        list.add(new SimpleSheetWrapper(data, titles, billName));

                    }
                    if (list.size() > 0) {
                        String fileName = supplierName + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
                        String fileType = ".xlsx";
                        response.setCharacterEncoding("utf-8");
                        response.setContentType("application/vnd.ms-excel;charset=utf-8");
                        response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                        OutputStream outputStream;
                        outputStream = response.getOutputStream();
                        ExcelUtils.getInstance().simpleSheet2Excel(list, outputStream);
                        outputStream.flush();
                        response.flushBuffer();
                        outputStream.close();
                        logger.info("供应商账单导出成功");
                    } else {
                        resultMap.put("code", "001");
                        resultMap.put("_message", "供应商账单无数据导出！");
                        return JSON.toJSONString(resultMap);
                    }
                }
            }


        } catch (Exception e) {
            e.printStackTrace();
            logger.error("供应商账单导出失败\t" + e.getMessage());
            resultMap.put("code", "002");
            resultMap.put("_message", "供应商账单导出失败！");
        }
        return JSON.toJSONString(resultMap);
    }

    @Override
    public Page listSupplierBillDetail(PageParam page, SupplierBillQueryParam param) {
        //获取账单查询sql
        String logListSql = getBillType(param.getType(), param.getBillDate(), param.getCustId(), param.getSupplierId(), param.getTransActionId(), param.getBatchId(), param.getEnterpriseName(), param.getStartTime(), param.getEndTime());
        return new Pagination().getPageData(logListSql, null, page, jdbcTemplate);
    }

    @Override
    public Map<String, String> queryCustomerConsumeTotal(String custId, String billDate) {
        Map<String, String> map = new HashMap<>();
        logger.info("查询账单的企业id是：" + custId + "查询账单时间范围是：" + billDate);
        StringBuffer customerConsumeTotalSql = new StringBuffer("SELECT IFNULL(SUM(s.amount) / 100,0) amountSum,IFNULL(SUM(s.prod_amount) / 100,0) supAmountSum  from stat_bill_month s\n" +
                "where s.cust_id =?");
        //查询全部
        if ("0".equals(billDate) || StringUtil.isEmpty(billDate)) {
            customerConsumeTotalSql = new StringBuffer("SELECT IFNULL(SUM(s.amount) / 100,0)amountSum,IFNULL(SUM(s.prod_amount) / 100,0) supAmountSum from stat_bill_month s\n" +
                    "where s.cust_id =?");
            //查看一年
        } else if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            customerConsumeTotalSql.append(" AND stat_time>=" + billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            customerConsumeTotalSql.append(" AND stat_time>=" + billDate);
        } else {
            customerConsumeTotalSql.append(" AND stat_time=" + billDate);
        }
        List<Map<String, Object>> consumeTotalsCount = sourceDao.sqlQuery(customerConsumeTotalSql.toString(), custId);
        String consumeTotal = null, supAmountSum = null, profitAmount = null;
        if (consumeTotalsCount != null && consumeTotalsCount.size() > 0) {
            //企业消费金额
            consumeTotal = new BigDecimal(String.valueOf(consumeTotalsCount.get(0).get("amountSum"))).setScale(2, BigDecimal.ROUND_DOWN).toString();
            //供应商成本价格
            supAmountSum = String.valueOf(consumeTotalsCount.get(0).get("supAmountSum"));
            //利润
            profitAmount = new BigDecimal(consumeTotal).subtract(new BigDecimal(supAmountSum)).setScale(2, BigDecimal.ROUND_DOWN).toString();
        }
        map.put("amountSum", consumeTotal);
        map.put("profitAmount", profitAmount);
        return map;
    }

    /**
     * @description 年月日处理（返回年和月）
     * @author:duanliying
     * @method
     * @date: 2018/12/14 13:53
     */
    public Map<String, Integer> getYearMessage(String billDate) {
        //判断参数是否为空
        int year = 0;
        int month = 0;
        if (StringUtil.isNotEmpty(billDate)) {
            //对日期进行拆分
            year = Integer.parseInt(billDate.substring(0, 4));
            month = Integer.parseInt(billDate.substring(4, 6));
        } else {
            year = LocalDate.now().getYear();
            month = LocalDate.now().getMonthValue();
        }
        Map<String, Integer> map = new HashMap<>();
        map.put("year", year);
        map.put("month", month);
        return map;
    }

    /**
     * @description 查询短信扣费记录
     * @author:duanliying
     * @method
     * @date: 2018/12/14 13:53
     */
    public String listSmsAccountLog(String resourceId, String billDate, String custId, String supplierId, String touchId, String batchId, String enterpriseName, String startTime, String endTime) {
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId,re.supplier_id, t1.cust_id,t1.batch_id batchId,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,u.realname ,u.account ,3 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount\n");
        sqlBuilder.append("FROM t_touch_sms_log t1 LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("LEFT JOIN t_customer_user u ON t1.user_id = u.id LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id WHERE ");
        sqlBuilder.append("t1.`status`=1001 AND YEAR (t1.create_time) = " + year + " AND MONTH (t1.create_time) = " + mouth);
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= " + custId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= " + resourceId);
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and t1.batch_id= " + batchId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(startTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        sqlBuilder.append(" ORDER BY t1.create_time desc ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }


    /**
     * @description 查询打电话扣费记录
     * @author:duanliying
     * @method
     * @date: 2018/12/14 13:53
     */
    public String listCallAccountLog(String resourceId, String billDate, String custId, String supplierId, String touchId, String batchId, String enterpriseName, String startTime, String endTime) {
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId,re.supplier_id, t1.cust_id,t1.batch_id batchId,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,u.realname ,u.account ,4 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount\n");
        sqlBuilder.append("FROM t_touch_voice_log t1 LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("LEFT JOIN t_customer_user u ON t1.user_id = u.id LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id WHERE ");
        sqlBuilder.append("t1.`status`=1001 AND YEAR (t1.create_time) = " + year + " AND MONTH (t1.create_time) = " + mouth);
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= " + custId);
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= " + resourceId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and t1.batch_id= " + batchId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        sqlBuilder.append(" ORDER BY t1.create_time desc ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }

    /**
     * @description 查询坐席扣费记录
     * @author:duanliying
     * @method
     * @date: 2018/12/14 13:53
     */
    public String listSeatsAccountLog(String resourceId, String billDate, String custId, String supplierId, String touchId, String batchId, String enterpriseName, String startTime, String endTime) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.transaction_id transactionId,re.supplier_id, t1.cust_id,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,u.realname ,u.account ,5 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount\n");
        sqlBuilder.append("FROM t_transaction_bill t1 LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("LEFT JOIN t_customer_user u ON t1.user_id = u.id LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id WHERE t1.type = 5");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= " + custId);
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.transaction_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= " + resourceId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and t1.batch_id= " + batchId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
        if (StringUtil.isNotEmpty(billDate)) {
            sqlBuilder.append(" and DATE_FORMAT(t1.create_time, '%Y%m') like'" + billDate + "'");
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        sqlBuilder.append(" ORDER BY t1.create_time desc ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }

    /**
     * @description 查询批次扣费记录
     * @author:duanliying
     * @method
     * @date: 2018/12/14 13:53
     */
    public String listFixsAccountLog(String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
      /*  StringBuilder sqlBuilder = new StringBuilder("SELECT t1.transaction_id transactionId, t1.cust_id,re.supplier_id,t1.batch_id batchId,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,u.realname ,u.account ,6 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount\n");
        sqlBuilder.append("FROM t_transaction_" + billDate + " t1 LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("LEFT JOIN t_customer_user u ON t1.user_id = u.id  LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id WHERE t1.type = 6 ");*/
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId, t1.STATUS,nl.comp_id cust_id,re.supplier_id,nl.id batchId,t1.upload_time billDate,");
        sqlBuilder.append("CONVERT (t1.amount / 100,DECIMAL (15, 2)) AS totalAmount,6 AS payType,m.enterprise_name,");
        sqlBuilder.append("CONVERT (t1.prod_amount / 100,DECIMAL (15, 2)) prodAmount\t");
        sqlBuilder.append("FROM nl_batch nl LEFT JOIN  nl_batch_detail t1 ON nl.id = t1.batch_id\t");
        sqlBuilder.append("LEFT JOIN t_customer m ON nl.comp_id = m.cust_id\t");
        sqlBuilder.append("LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id\t");
        sqlBuilder.append("WHERE nl.certify_type = 0 AND nl.`status` = 0 AND t1.`status` = 1\t");
        sqlBuilder.append("AND YEAR (nl.repair_time) = " + year + "\t");
        sqlBuilder.append("AND MONTH (nl.repair_time) = " + mouth + "\t");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and nl.comp_id= " + custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and n1.id= " + batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= " + resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        sqlBuilder.append(" ORDER BY nl.repair_time desc ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }

    /**
     * @description 查询扣减记录
     * @author:duanliying
     * @method
     * @date: 2018/12/14 13:53
     */
    public String listDeductionAccountLog(String billDate, String custId, String supplierId, String touchId, String enterpriseName, String startTime, String endTime) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.transaction_id transactionId,t1.supplier_id,t1.cust_id,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,c.realname ,7 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount ,certificate imgUrl FROM t_transaction_bill t1\n");
        sqlBuilder.append(" LEFT JOIN t_customer_user c ON t1.user_id = c.id LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("WHERE 1 = 1 AND t1.type = 7 ");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= " + custId);
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.transaction_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
       /* //查询资金扣减记录
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }*/
        if (StringUtil.isNotEmpty(billDate)) {
            sqlBuilder.append(" AND DATE_FORMAT(t1.create_time, '%Y%m') like " + billDate);
        }
        sqlBuilder.append(" ORDER BY t1.create_time desc ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }

    /**
     * @description 查询充值记录
     * @author:duanliying
     * @method
     * @date: 2018/12/14 13:53
     */
    public String listRechargeAccountLog(String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.remark,t1.transaction_id transactionId, t1.cust_id,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,1 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount,certificate imgUrl FROM t_transaction_bill t1\n");
        sqlBuilder.append("LEFT JOIN t_customer m ON t1.cust_id = m.cust_id LEFT JOIN t_customer_user u ON t1.user_id = u.id ");
        sqlBuilder.append("WHERE 1 = 1 AND t1.type = 1");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= " + custId);
        }

        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and t1.id= " + batchId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.transaction_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        if (StringUtil.isNotEmpty(billDate)) {
            sqlBuilder.append(" AND DATE_FORMAT(t1.create_time, '%Y%m') like " + billDate);
        }
        sqlBuilder.append(" ORDER BY t1.create_time desc ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }

    /**
     * @description 查询调账记录
     * @author:duanliying
     * @method
     * @date: 2018/12/14 13:53
     */
    public String listAdjustAccountLog(String supplierId, String billDate, String touchId, String startTime, String endTime) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.remark,t1.transaction_id transactionId,t1.cust_id,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,9 AS payType ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount,certificate imgUrl  FROM t_transaction_bill t1\n");
        sqlBuilder.append("WHERE t1.type = 9");
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.transaction_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sqlBuilder.append(" and t1.supplier_id= " + supplierId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        if (StringUtil.isNotEmpty(billDate)) {
            sqlBuilder.append(" and DATE_FORMAT(t1.create_time, '%Y%m') like'" + billDate + "'");
        }
        sqlBuilder.append(" ORDER BY t1.create_time desc ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }


    /**
     * @description 账单类型
     * @author:duanliying
     * @method
     * @date: 2018/12/14 13:53
     */
    public String getBillType(String type, String billDate, String custId, String supplierId, String touchId, String batchId, String enterpriseName, String startTime, String endTime) {
        String logListSql = null;
        // 1.充值 2.快递 3.短信扣费  4.通话扣费   5.座席扣费  6修复扣费. 7扣减 9.调账记录
        //根据supplier和type查询resourceId
        MarketResourceEntity supplier = sourceDao.getSupplier(supplierId, Integer.parseInt(type));
        String resourceId = null;
        if (supplier != null) {
            resourceId = String.valueOf(supplier.getResourceId());
            logger.info("resourceId是：\t" + resourceId);
        }
        if ("1".equals(type)) {
            //充值扣费记录
            logListSql = listRechargeAccountLog(billDate, custId, supplierId, touchId, batchId, enterpriseName, startTime, endTime);
        }
        if ("2".equals(type)) {
            //快递扣费记录
            logListSql = listExpressAccountLog(resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        if ("3".equals(type)) {
            //短信扣费记录
            logListSql = listSmsAccountLog(resourceId, billDate, custId, supplierId, touchId, batchId, enterpriseName, startTime, endTime);
        }
        if ("4".equals(type)) {
            //通话扣费记录
            logListSql = listCallAccountLog(resourceId, billDate, custId, supplierId, touchId, batchId, enterpriseName, startTime, endTime);
        }
        if ("5".equals(type)) {
            //座席扣费记录
            logListSql = listSeatsAccountLog(resourceId, billDate, custId, supplierId, touchId, batchId, enterpriseName, startTime, endTime);
        }
        if ("6".equals(type)) {
            //修复扣费记录
            logListSql = listFixsAccountLog(resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        if ("7".equals(type)) {
            //扣减记录
            logListSql = listDeductionAccountLog(billDate, custId, supplierId, touchId, enterpriseName, startTime, endTime);
        }
        if ("9".equals(type)) {
            //调账记录
            logListSql = listAdjustAccountLog(supplierId, billDate, touchId, startTime, endTime);
        }
        //mac修复
        if ("10".equals(type)) {
            logListSql = listMacAccountLog(resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        // imei修复
        if ("11".equals(type)) {
            logListSql = listImeiAccountLog(resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        //12 地址修复
        if ("12".equals(type)) {
            logListSql = listAddressFixAccountLog(resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        return logListSql;
    }

    /**
     * @description 快递账单查询
     * @author:duanliying
     * @method
     * @date: 2019/1/3 11:11
     */
    private String listExpressAccountLog(String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId,t1.`status`,re.supplier_id, t1.cust_id,t1.batch_id batchId,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,u.realname ,u.account ,2 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount\n");
        sqlBuilder.append("FROM t_touch_express_log t1 LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("LEFT JOIN t_customer_user u ON t1.user_id = u.id LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id WHERE YEAR (t1.create_time) = " + year + " AND MONTH (t1.create_time) = " + mouth);
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= " + custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and t1.id= " + batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= " + resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        sqlBuilder.append(" ORDER BY t1.create_time desc ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }

    /**
     * @description Mac账单查询
     * @author:duanliying
     * @method
     * @date: 2019/1/3 11:11
     */
    private String listMacAccountLog(String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId,t1.STATUS,nl.comp_id cust_id,re.supplier_id,nl.id batchId,t1.upload_time billDate,");
        sqlBuilder.append("CONVERT (t1.amount / 100,DECIMAL (15, 2)) AS totalAmount,10 AS payType,m.enterprise_name,");
        sqlBuilder.append("CONVERT (t1.prod_amount / 100,DECIMAL (15, 2)) prodAmount\t");
        sqlBuilder.append("FROM nl_batch nl LEFT JOIN  nl_batch_detail t1 ON nl.id = t1.batch_id\t");
        sqlBuilder.append("LEFT JOIN t_customer m ON nl.comp_id = m.cust_id\t");
        sqlBuilder.append("LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id\t");
        sqlBuilder.append("WHERE nl.certify_type = 2 AND nl.`status` = 0 AND t1.`status` = 1\t");
        sqlBuilder.append("AND YEAR (nl.repair_time) = " + year + "\t");
        sqlBuilder.append("AND MONTH (nl.repair_time) = " + mouth + "\t");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and nl.comp_id= " + custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and nl.id= " + batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= " + resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        sqlBuilder.append(" ORDER BY nl.repair_time DESC ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }


    /**
     * @description imei账单查询
     * @author:duanliying
     * @method
     * @date: 2019/1/3 11:11
     */
    private String listImeiAccountLog(String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId, t1.STATUS,nl.comp_id cust_id,re.supplier_id,nl.id batchId,t1.upload_time billDate,");
        sqlBuilder.append("CONVERT (t1.amount / 100,DECIMAL (15, 2)) AS totalAmount,11 AS payType,m.enterprise_name,");
        sqlBuilder.append("CONVERT (t1.prod_amount / 100,DECIMAL (15, 2)) prodAmount\t");
        sqlBuilder.append("FROM nl_batch nl LEFT JOIN  nl_batch_detail t1 ON nl.id = t1.batch_id\t");
        sqlBuilder.append("LEFT JOIN t_customer m ON nl.comp_id = m.cust_id\t");
        sqlBuilder.append("LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id\t");
        sqlBuilder.append("WHERE nl.certify_type = 1 AND nl.`status` = 0 AND t1.`status` = 1\t");
        sqlBuilder.append("AND YEAR (nl.repair_time) = " + year + "\t");
        sqlBuilder.append("AND MONTH (nl.repair_time) = " + mouth + "\t");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and nl.comp_id= " + custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and nl.id= " + batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= " + resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        sqlBuilder.append(" ORDER BY nl.repair_time DESC ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }

    /**
     * @description 地址修复账单查询
     * @author:duanliying
     * @method
     * @date: 2019/1/3 11:11
     */
    private String listAddressFixAccountLog(String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId, t1.STATUS,nl.comp_id cust_id,re.supplier_id,nl.id batchId,t1.upload_time billDate,");
        sqlBuilder.append("CONVERT (t1.amount / 100,DECIMAL (15, 2)) AS totalAmount,12 AS payType,m.enterprise_name,");
        sqlBuilder.append("CONVERT (t1.prod_amount / 100,DECIMAL (15, 2)) prodAmount\t");
        sqlBuilder.append("FROM nl_batch nl LEFT JOIN  nl_batch_detail t1 ON nl.id = t1.batch_id\t");
        sqlBuilder.append("LEFT JOIN t_customer m ON nl.comp_id = m.cust_id\t");
        sqlBuilder.append("LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id\t");
        sqlBuilder.append("WHERE nl.certify_type = 3 AND nl.`status` = 0 AND t1.`status` = 1\t");
        sqlBuilder.append("AND YEAR (nl.repair_time) = " + year + "\t");
        sqlBuilder.append("AND MONTH (nl.repair_time) = " + mouth + "\t");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and nl.comp_id= " + custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and nl.id= " + batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= " + resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like '%" + enterpriseName + "%'");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= " + touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }
        sqlBuilder.append(" ORDER BY nl.repair_time DESC ");
        logger.info("某企业账单明细sql：\t" + sqlBuilder.toString());
        return sqlBuilder.toString();
    }

    /**
     * 企业账单列表(二级菜单)
     */
    @Override
    public Map<String, Object> listBillMessage(CustomerBillQueryParam param, String userType) {
        String billDate = param.getBillDate();
        String customerId = param.getCustomerId();
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> headerData = new HashMap<>();
        //根据企业id查询企业信息
        StringBuilder customerMessageSql = new StringBuilder("SELECT c.cust_id custId,c.enterprise_name enterpriseName,u.account account,p.property_value channel FROM t_customer c\n");
        customerMessageSql.append("LEFT JOIN t_customer_user u ON c.cust_id = u.cust_id\n");
        customerMessageSql.append("LEFT JOIN t_customer_property p ON c.cust_id = p.cust_id AND p.property_name ='channel'\n");
        customerMessageSql.append("WHERE c.cust_id =? AND u.user_type = 1 GROUP BY c.cust_id");
        List<Map<String, Object>> customerMessage = sourceDao.sqlQuery(customerMessageSql.toString(), customerId);
        //查询企业消费总额
        StringBuffer custSumMoneySql = new StringBuffer("SELECT IFNULL(SUM(amount), 0) /100 totalAcount FROM stat_bill_month WHERE cust_id = ?");
        //查询企业账单信息
        StringBuilder billCustomer = new StringBuilder("SELECT GROUP_CONCAT(s.resource_id) channel,s.bill_type type,SUM(s.prod_amount) /100 costAmountSum,SUM(s.amount) /100 consumeAmountsum  FROM stat_bill_month s WHERE s.cust_id = ?");
        if ("0".equals(billDate) || StringUtil.isEmpty(billDate)) {
            custSumMoneySql = new StringBuffer("SELECT IFNULL(SUM(amount), 0) /100 totalAcount FROM stat_bill_month WHERE cust_id = ?");
            //查看一年
        } else if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            custSumMoneySql.append(" AND stat_time>=" + billDate);
            billCustomer.append(" AND stat_time>=" + billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            custSumMoneySql.append(" AND stat_time>=" + billDate);
            billCustomer.append(" AND stat_time>=" + billDate);
        } else {
            custSumMoneySql.append(" AND stat_time=" + billDate);
            billCustomer.append(" AND stat_time=" + billDate);
        }
        if (customerMessage.size() > 0) {
            headerData = customerMessage.get(0);
            //查询企业总消费金额
            List<Map<String, Object>> totalAcount = sourceDao.sqlQuery(custSumMoneySql.toString(), customerId);
            if (totalAcount.size() > 0) {
                headerData.put("totalAcount", totalAcount.get(0).get("totalAcount"));
            }
        }

        //企业账单信息
        billCustomer.append(" GROUP BY s.bill_type");
        logger.info("查询企业账单，企业id是：" + customerId);
        List<Map<String, Object>> customerBillList = sourceDao.sqlQuery(billCustomer.toString(), customerId);
        if (customerBillList.size() > 0) {
            for (int i = 0; i < customerBillList.size(); i++) {
                String channel = String.valueOf(customerBillList.get(i).get("channel"));
                String[] split = channel.split(",");
                String sourceName = "";
                for (int j = 0; j < split.length; j++) {
                    //根据sourceid查询sourcename
                    String sourceNameSql = "SELECT name FROM t_market_resource re LEFT JOIN t_supplier s ON re.supplier_id = s.supplier_id WHERE resource_id = ?";
                    List<Map<String, Object>> sourceMap = sourceDao.sqlQuery(sourceNameSql, split[j]);
                    if (sourceMap.size() > 0) {
                        sourceName += String.valueOf(sourceMap.get(0).get("name")) + " ";
                    }
                }
                if (StringUtil.isNotEmpty(sourceName)) {
                    sourceName = sourceName.substring(0, sourceName.length() - 1);
                }
                customerBillList.get(i).put("channel", sourceName);
            }
        }
        //添加企业充值扣减(按月展示)
        String sql = "select type , SUM(amount)/100 consumeAmountsum from t_transaction_bill WHERE cust_id =?  AND type in (" + TransactionEnum.BALANCE_DEDUCTION.getType() + "," + TransactionEnum.BALANCE_RECHARGE.getType() + ") AND DATE_FORMAT(create_time, '%Y%m') like " + billDate + " GROUP BY type";
        List<Map<String, Object>> customerMoneyList = sourceDao.sqlQuery(sql, customerId);
        if (customerMoneyList != null && customerMoneyList.size() > 0) {
            customerBillList.addAll(customerMoneyList);
        }
        resultMap.put("customerMessage", headerData);
        resultMap.put("billMessage", customerBillList);
        resultMap.put("total", customerBillList.size());
        return resultMap;
    }

    /**
     * @description 供应商账单展示
     * @author:duanliying
     * @method
     * @date: 2018/12/18 17:36
     */
    @Override
    public Map<String, Object> listBillSupplier(SupplierBillQueryParam param) {
        Map<String, Object> resultMap = new HashMap<>();
        String supplierId = param.getSupplierId();
        String billDate = param.getBillDate();
        logger.info("查询供应商账单参数供应商id是" + supplierId + "账单日期是：" + billDate);
        //供应商账单
        StringBuilder billSupplierSql = new StringBuilder("SELECT IFNULL(SUM(prod_amount),0) /100 prodAmount,b.bill_type type,b.resource_id resourceId ");
        billSupplierSql.append("FROM stat_bill_month b WHERE 1=1");
        if ("0".equals(billDate) || StringUtil.isEmpty(billDate)) {
            billSupplierSql = billSupplierSql;
            //查看一年
        } else if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            billSupplierSql.append(" AND stat_time>=" + billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            billSupplierSql.append(" AND stat_time>=" + billDate);
        } else {
            billSupplierSql.append(" AND stat_time=" + billDate);
        }
        billSupplierSql.append(" AND b.resource_id IN (SELECT resource_id FROM t_market_resource WHERE supplier_id = ?) ");
        //消费总额
        List<Map<String, Object>> totalAmount = sourceDao.sqlQuery(billSupplierSql.toString(), supplierId);
        //查询供应商账单
        billSupplierSql.append("GROUP BY b.bill_type");
        logger.info("查询供应商账单sql" + billSupplierSql.toString());
        List<Map<String, Object>> billSupplierMessage = sourceDao.sqlQuery(billSupplierSql.toString(), supplierId);
        //查询供应商名称
        SupplierEntity supplierEntity = null;
        if (StringUtil.isNotEmpty(supplierId)) {
            supplierEntity = supplierDao.getSupplierList(Integer.parseInt(supplierId));
        }
        resultMap.put("billSupplierMessage", billSupplierMessage);
        resultMap.put("total", billSupplierMessage.size());
        if (supplierEntity != null) {
            resultMap.put("supplierName", supplierEntity.getName());
        }
        resultMap.put("totalAmount", totalAmount.get(0).get("prodAmount"));
        return resultMap;
    }


    /**
     * 查询企业和供应商月份账单金额
     */
    @Override
    public List<Map<String, Object>> queryBillList(String billDate, String custId, String type) {
        StringBuilder billSupplierSql = new StringBuilder("select * from stat_bill_month WHERE 1=1 ");
        if (StringUtil.isNotEmpty(billDate)) {
            billSupplierSql.append(" and stat_time = " + billDate);
        }
        if (StringUtil.isNotEmpty(custId)) {
            billSupplierSql.append(" and cust_id = " + custId);
        }
        if (StringUtil.isNotEmpty(type)) {
            billSupplierSql.append(" and type = " + type);
        }
        List<Map<String, Object>> billList = sourceDao.sqlQuery(billSupplierSql.toString(), null);
        return billList;
    }

    /**
     * 导出结算单
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/19 16:44
     */
    public String exportSettlementBill(HttpServletResponse response, String custId, String billDate) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            CustomerDO custMessage = customerDao.getCustMessage(custId);
            String enterpriseName = custMessage.getEnterpriseName();
            StringBuffer billSql = new StringBuffer("SELECT bill_time,cust_id,repair_num,repair_success_num,repair_phone_num,CONVERT(IFNULL(repair_price/ 100, 0),DECIMAL(15,2)) repair_price,");
            billSql.append("CONVERT(IFNULL(SUM(repair_amount) / 100, 0),DECIMAL(15,2)) repair_amount,send_sms_num,send_sms_success_num,CONVERT(IFNULL(sms_price/ 100, 0),DECIMAL(15,2)) sms_price,CONVERT(IFNULL(SUM(sms_amount) / 100, 0),DECIMAL(15,2)) sms_amount,call_minute_num,");
            billSql.append("CONVERT(IFNULL(call_price/ 100, 0),DECIMAL(15,2)) call_price, CONVERT(IFNULL(SUM(call_amount) / 100, 0),DECIMAL(15,2)) call_amount,seat_num,CONVERT(IFNULL(seat_price/ 100, 0),DECIMAL(15,2)) seat_price,CONVERT(IFNULL(SUM(seat_amount) / 100, 0),DECIMAL(15,2)) seat_amount,");
            billSql.append("CONVERT(IFNULL(sum_total/ 100, 0),DECIMAL(15,2)) sum_total FROM stat_customer_bill WHERE cust_id =? AND bill_time = ?");
            logger.info("企业id是：" + custId + "账单日期是：" + billDate);
            List<Map<String, Object>> list = customerDao.sqlQuery(billSql.toString(), new Object[]{custId, billDate});
            double smsUnitPrice = 0, idCardUnitPrice = 0, seatUnitPrice = 0, callUnitPrice = 0;
            //短信单价
            List<Map<String, Object>> custStatBillMonth = customerDao.getCustStatBillMonth(SupplierEnum.CUC.getSupplierId(), ResourceEnum.SMS.getType(), custId, billDate, TransactionEnum.SMS_DEDUCTION.getType());
            if (custStatBillMonth.size() > 0) {
                smsUnitPrice = NumberConvertUtil.transformtionElement(custStatBillMonth.get(0).get("unit_price"));
                logger.info("短信单价是：" + smsUnitPrice);
            }
            //身份证修复单价
            List<Map<String, Object>> custStatBillIdCard = customerDao.getCustStatBillMonth(SupplierEnum.CUC.getSupplierId(), ResourceEnum.IDCARD.getType(), custId, billDate, TransactionEnum.IDCARD_DEDUCTION.getType());
            if (custStatBillIdCard.size() > 0) {
                idCardUnitPrice = NumberConvertUtil.transformtionElement(custStatBillIdCard.get(0).get("unit_price"));
                logger.info("身份证修复单价是：" + idCardUnitPrice);
            }
            //坐席单价
            List<Map<String, Object>> custStatBillSeat = customerDao.getCustStatBillMonth(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType(), custId, billDate, TransactionEnum.SEAT_DEDUCTION.getType());
            if (custStatBillSeat.size() > 0) {
                seatUnitPrice = NumberConvertUtil.transformtionElement(custStatBillSeat.get(0).get("unit_price"));
                logger.info("坐席单价是：" + seatUnitPrice);
            }
            //外呼单价
            List<Map<String, Object>> custStatBillCall = customerDao.getCustStatBillMonth(SupplierEnum.CUC.getSupplierId(), ResourceEnum.CALL.getType(), custId, billDate, TransactionEnum.CALL_DEDUCTION.getType());
            if (custStatBillCall.size() > 0) {
                callUnitPrice = NumberConvertUtil.transformtionElement(custStatBillCall.get(0).get("unit_price"));
                logger.info("外呼单价是：" + callUnitPrice);
            }

            // 设置标题
            List<String> titles = new ArrayList<String>();
            titles.add("结算单时间");
            titles.add("企业id");
            titles.add("企业名称");
            titles.add("提交修复人数");
            titles.add("修复成功人数");
            titles.add("修复成功号码数");
            titles.add("修复单价");
            titles.add("修复金额");
            titles.add("发送短信数量");
            titles.add("发送短信成功数量");
            titles.add("短信单价");
            titles.add("短信消费金额");
            titles.add("拨打分钟数");
            titles.add("外呼单价");
            titles.add("外呼消费金额");
            titles.add("坐席数");
            titles.add("坐席单价");
            titles.add("坐席消费金额");
            titles.add("消费总金额");
            List<Object> rowList = new ArrayList<>();
            if (list.size() > 0) {
                rowList.add(list.get(0).get("bill_time") != null ? list.get(0).get("bill_time") : "");
                rowList.add(list.get(0).get("cust_id") != null ? list.get(0).get("cust_id") : "");
                rowList.add(enterpriseName);
                rowList.add(list.get(0).get("repair_num") != null ? list.get(0).get("repair_num") : "");
                rowList.add(list.get(0).get("repair_success_num") != null ? list.get(0).get("repair_success_num") : "");
                rowList.add(list.get(0).get("repair_phone_num") != null ? list.get(0).get("repair_phone_num") : "");
                rowList.add(idCardUnitPrice);
                rowList.add(list.get(0).get("repair_amount") != null ? list.get(0).get("repair_amount") : "");
                rowList.add(list.get(0).get("send_sms_num") != null ? list.get(0).get("send_sms_num") : "");
                rowList.add(list.get(0).get("send_sms_success_num") != null ? list.get(0).get("send_sms_success_num") : "");
                rowList.add(smsUnitPrice);
                rowList.add(list.get(0).get("sms_amount") != null ? list.get(0).get("sms_amount") : "");
                rowList.add(list.get(0).get("call_minute_num") != null ? list.get(0).get("call_minute_num") : "");
                rowList.add(callUnitPrice);
                rowList.add(list.get(0).get("call_amount") != null ? list.get(0).get("call_amount") : "");
                rowList.add(list.get(0).get("seat_num") != null ? list.get(0).get("seat_num") : "");
                rowList.add(seatUnitPrice);
                rowList.add(list.get(0).get("seat_amount") != null ? list.get(0).get("seat_amount") : "");
                rowList.add(list.get(0).get("sum_total") != null ? list.get(0).get("sum_total") : "");
            }

            List<List<Object>> data = new ArrayList<>();
            data.add(rowList);
            if (list.size() > 0) {
                String fileName = custId + "_" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
                String fileType = ".xlsx";
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                OutputStream outputStream;
                outputStream = response.getOutputStream();
                ExcelUtils.getInstance().exportObjects2Excel(data, titles, outputStream);
                outputStream.flush();
                response.flushBuffer();
                outputStream.close();
                logger.info("结算单导出成功");
                resultMap.put("enterpriseName", enterpriseName);
            } else {
                resultMap.put("code", "001");
                resultMap.put("_message", "结算单无数据导出！");
                return JSON.toJSONString(resultMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("供应商账单导出失败\t" + e.getMessage());
            resultMap.put("code", "002");
            resultMap.put("_message", "结算单导出失败！");
        }
        return JSON.toJSONString(resultMap);
    }

    /**
     * @description 消费总金额和总利润接口
     * @author:duanliying
     * @method
     * @date: 2019/4/29 10:58
     */
    public Map<String, String> queryAllAmount(String billDate) {
        if (StringUtil.isEmpty(billDate)) {
            //默认查询当月
            billDate = LocalDate.now().format(YYYYMM);
        }
        Map<String, String> map = new HashMap<>();
        logger.info("查询账单时间范围是：" + billDate);
        StringBuffer profitAmountSql = new StringBuffer("SELECT IFNULL(SUM(s.amount) / 100,0) amountSum,IFNULL(SUM(s.prod_amount) / 100,0) supAmountSum FROM stat_bill_month s where 1=1 ");
        //查看一年
        if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            profitAmountSql.append(" AND s.stat_time>='" + billDate + "'");
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            profitAmountSql.append(" AND s.stat_time>='" + billDate + "'");
        } else if (!"0".equals(billDate)) {
            profitAmountSql.append(" AND s.stat_time='" + billDate + "'");
        }
        List<Map<String, Object>> consumeTotalsCount = sourceDao.sqlQuery(profitAmountSql.toString());
        String consumeTotal = null, supAmountSum = null, profitAmount = null;
        if (consumeTotalsCount != null && consumeTotalsCount.size() > 0) {
            //企业消费金额
            consumeTotal = new BigDecimal(String.valueOf(consumeTotalsCount.get(0).get("amountSum"))).setScale(2, BigDecimal.ROUND_DOWN).toString();
            //供应商成本价格
            supAmountSum = String.valueOf(consumeTotalsCount.get(0).get("supAmountSum"));
            //利润
            profitAmount = new BigDecimal(consumeTotal).subtract(new BigDecimal(supAmountSum)).setScale(2, BigDecimal.ROUND_DOWN).toString();
        }
        map.put("amountSum", consumeTotal);
        map.put("profitAmount", profitAmount);
        return map;
    }
}

