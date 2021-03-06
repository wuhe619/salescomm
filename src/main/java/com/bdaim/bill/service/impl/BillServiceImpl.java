package com.bdaim.bill.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.bill.dto.BillDetailQueryParam;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.dto.SupplierBillQueryParam;
import com.bdaim.bill.service.BillService;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.customer.account.dao.TransactionDao;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.order.dao.OrderDao;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.supplier.dto.SupplierEnum;
import com.bdaim.supplier.entity.SupplierEntity;
import com.bdaim.util.DateUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.github.crab2died.ExcelUtils;
import com.github.crab2died.sheet.wrapper.SimpleSheetWrapper;
import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/10/8 10:46
 */
@Service("billService")
public class BillServiceImpl implements BillService {
    private static Logger logger = LoggerFactory.getLogger(BillServiceImpl.class);

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
        List<Object> p = new ArrayList<>();
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
            p.add(param.getCustomerId());
            sqlBuilder.append(" and t.cust_id= ?");
        }
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            p.add("%" + param.getEnterpriseName() + "%");
            sqlBuilder.append(" and cus.enterprise_name like ?");
        }
        if (StringUtil.isNotEmpty(param.getAccount())) {
            p.add("%" + param.getAccount() + "%");
            sqlBuilder.append(" and cu.account like ? ");
        }
        if (StringUtil.isNotEmpty(param.getRealname())) {
            p.add("%" + param.getRealname() + "%");
            sqlBuilder.append(" and cu.realname like ? ");
        }
        if (StringUtil.isNotEmpty(param.getPhone())) {
            p.add("%" + param.getPhone() + "%");
            sqlBuilder.append(" and cjc.mobile_num like ? ");
        }
        sqlBuilder.append(" GROUP BY t.cust_id ");
        logger.info("查询后台账单sql" + sqlBuilder.toString());
        try {
            return customerDao.sqlPageQuery(sqlBuilder.toString(), page.getPageNum(), page.getPageSize(), p.toArray());
            // return new PaginationThrowException().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> queryCustomerBill(CustomerBillQueryParam param) {
        List<Object> p = new ArrayList<>();
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
            p.add(param.getCustomerId());
            sqlBuilder.append(" and t.cust_id= ? ");
        }
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            p.add("%" + param.getEnterpriseName() + "%");
            sqlBuilder.append(" and cus.enterprise_name like ? ");
        }
        if (StringUtil.isNotEmpty(param.getAccount())) {
            p.add("%" + param.getAccount() + "%");
            sqlBuilder.append(" and cu.account like ? ");
        }
        if (StringUtil.isNotEmpty(param.getRealname())) {
            p.add("%" + param.getRealname() + "%");
            sqlBuilder.append(" and cu.realname like ? ");
        }
        if (StringUtil.isNotEmpty(param.getPhone())) {
            p.add("%" + param.getPhone() + "%");
            sqlBuilder.append(" and cjc.mobile_num like ? ");
        }
        sqlBuilder.append(" GROUP BY t.cust_id ");
        logger.info("查询后台账单sql" + sqlBuilder.toString());
        try {
            return jdbcTemplate.queryForList(sqlBuilder.toString(), p.toArray());
            // return new PaginationThrowException().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public List<Map<String, Object>> querySupplierBill(SupplierBillQueryParam param) {
        String billDate = param.getBillDate();
        Map<String, String> map = new HashMap<>();
        logger.info("查询账单时间范围是：" + billDate);
        StringBuffer supBillSql = new StringBuffer("SELECT IFNULL(SUM(b.prod_amount),0) /100 amountSum FROM t_market_resource r LEFT JOIN stat_bill_month b ON r.resource_id = b.resource_id WHERE supplier_id =?");
        //查询全部
        List<Object> p = new ArrayList<>();

        if ("0".equals(billDate) || StringUtil.isEmpty(billDate)) {
            supBillSql = new StringBuffer("SELECT IFNULL(SUM(b.prod_amount),0) /100 amountSum FROM t_market_resource r LEFT JOIN stat_bill_month b ON r.resource_id = b.resource_id WHERE supplier_id =?");
            //查看一年
        } else if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            supBillSql.append(" AND stat_time>= ? ");
            p.add(billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            supBillSql.append(" AND stat_time>= ? ");
            p.add(billDate);
        } else {
            supBillSql.append(" AND stat_time=?");
            p.add(billDate);
        }
        //查询当前所有供应商
        List<Object> supplierParam = new ArrayList<>();
        StringBuffer querySql = new StringBuffer("SELECT s.supplier_id supplierId,s.`name` supplierName,s.create_time,s.contact_person person,s.contact_phone phone,s.status,GROUP_CONCAT(DISTINCT r.type_code) resourceType ");
        querySql.append("FROM t_supplier s LEFT JOIN t_market_resource r on s.supplier_id = r.supplier_id where 1=1 ");
        if (StringUtil.isNotEmpty(param.getSupplierId())) {
            querySql.append("AND s.supplier_id = ? ");
            supplierParam.add(param.getSupplierId());
        }
        querySql.append("GROUP BY s.supplier_id");
        List<Map<String, Object>> data = jdbcTemplate.queryForList(querySql.toString(), supplierParam.toArray());
        if (data != null) {
            List<Map<String, Object>> supplierList = data;
            if (supplierList.size() > 0) {
                for (int i = 0; i < supplierList.size(); i++) {
                    String supplierId = String.valueOf(supplierList.get(i).get("supplierId"));
                    if (StringUtil.isNotEmpty(supplierId)) {
                        //根据supplierId查询出消费金额
                        logger.info("查询供应商消费金额sql是：" + supBillSql.toString());
                        List<Map<String, Object>> countMoneyList = sourceDao.sqlQuery(supBillSql.toString(), supplierId, p.toArray());
                        if (countMoneyList.size() > 0) {
                            supplierList.get(i).put("amountSum", countMoneyList.get(0).get("amountSum"));
                        }
                    }
                    //查询供应商资源名称
                    String resourceType = String.valueOf(supplierList.get(i).get("resourceType"));
                    logger.info("获取到的资源类型是：" + resourceType);
                    if (StringUtil.isNotEmpty(resourceType)) {
                        String[] split = resourceType.split(",");
                        String name = "";
                        if (split.length > 0) {
                            for (int j = 0; j < split.length; j++) {
                                if (ResourceEnum.getName(NumberConvertUtil.parseInt(split[j])) != null) {
                                    name += ResourceEnum.getName(NumberConvertUtil.parseInt(split[j])) + " ";
                                }
                            }
                        }
                        supplierList.get(i).put("resourceName", name);
                    }
                }
            }
        }
        return data;
    }

    @Override
    public Page querySupplierBill(PageParam page, SupplierBillQueryParam param) {
        String billDate = param.getBillDate();
        Map<String, String> map = new HashMap<>();
        logger.info("查询账单时间范围是：" + billDate);
        StringBuffer supBillSql = new StringBuffer("SELECT IFNULL(SUM(b.prod_amount),0) /100 amountSum FROM t_market_resource r LEFT JOIN stat_bill_month b ON r.resource_id = b.resource_id WHERE supplier_id =?");
        //查询全部
        List<Object> p = new ArrayList<>();
        if ("0".equals(billDate) || StringUtil.isEmpty(billDate)) {
            supBillSql = new StringBuffer("SELECT IFNULL(SUM(b.prod_amount),0) /100 amountSum FROM t_market_resource r LEFT JOIN stat_bill_month b ON r.resource_id = b.resource_id WHERE supplier_id =?");
            //查看一年
        } else if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            supBillSql.append(" AND stat_time>=? ");
            p.add(billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            supBillSql.append(" AND stat_time>= ? ");
            p.add(billDate);
        } else {
            supBillSql.append(" AND stat_time=? ");
            p.add(billDate);
        }
        //查询当前所有供应商
        StringBuffer querySql = new StringBuffer("SELECT s.supplier_id supplierId,s.`name` supplierName,s.create_time,s.contact_person person,s.contact_phone phone,s.status,GROUP_CONCAT(DISTINCT r.type_code) resourceType ");
        querySql.append("FROM t_supplier s LEFT JOIN t_market_resource r on s.supplier_id = r.supplier_id where 1=1 ");
        List<Object> qParam = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getSupplierId())) {
            qParam.add(param.getSupplierId());
            querySql.append("AND s.supplier_id = ? ");
        }
        querySql.append("GROUP BY s.supplier_id");
        Page data = sourceDao.sqlPageQuery(querySql.toString(), param.getPageNum(), param.getPageSize(), qParam.toArray());
        if (data != null) {
            List<Map<String, Object>> supplierList = data.getData();
            if (supplierList.size() > 0) {
                for (int i = 0; i < supplierList.size(); i++) {
                    String supplierId = String.valueOf(supplierList.get(i).get("supplierId"));
                    if (StringUtil.isNotEmpty(supplierId)) {
                        //根据supplierId查询出消费金额
                        logger.info("查询供应商消费金额sql是：" + supBillSql.toString());
                        List<Map<String, Object>> countMoneyList = sourceDao.sqlQuery(supBillSql.toString(), supplierId, p.toArray());
                        if (countMoneyList.size() > 0) {
                            supplierList.get(i).put("amountSum", countMoneyList.get(0).get("amountSum"));
                        }
                    }
                    //查询供应商资源名称
                    String resourceType = String.valueOf(supplierList.get(i).get("resourceType"));
                    logger.info("获取到的资源类型是：" + resourceType);
                    if (StringUtil.isNotEmpty(resourceType)) {
                        String[] split = resourceType.split(",");
                        String name = "";
                        if (split.length > 0) {
                            for (int j = 0; j < split.length; j++) {
                                if (ResourceEnum.getName(NumberConvertUtil.parseInt(split[j])) != null) {
                                    name += ResourceEnum.getName(NumberConvertUtil.parseInt(split[j])) + " ";
                                }
                            }
                        }
                        supplierList.get(i).put("resourceName", name);
                    }
                }
            }
        }
        return data;

    }


    @Override
    public Page listBillDetail(PageParam page, CustomerBillQueryParam param) {
        //查询账单sql
        List<Object> p = new ArrayList<>();
        String logListSql = getBillType(p, param.getType(), param.getBillDate(), param.getCustomerId(), param.getSupplierId(), param.getTransactionId(), param.getBatchId(), param.getEnterpriseName(), param.getStartTime(), param.getEndTime());
        Page data = customerDao.sqlPageQuery(logListSql, page.getPageNum(), page.getPageSize(), p.toArray());
        logger.info("查询结果为" + data.toString());
        //Page pageData = new Pagination().getPageData(logListSql, null, page, jdbcTemplate);
        List<Map<String, Object>> list = data.getData();
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
        return data;
    }


    @Override
    public Object exportCustomerBill(CustomerBillQueryParam param, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 生成sheet数据
            List<SimpleSheetWrapper> list = new ArrayList<>();
            Customer customer = customerDao.findUniqueBy("custId", param.getCustomerId());
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
                        List<Object> p = new ArrayList<>();
                        String logListSql = getBillType(p, billTypes[j], param.getBillDate(), param.getCustomerId(), param.getSupplierId(), param.getTransactionId(), param.getBatchId(), param.getEnterpriseName(), param.getStartTime(), param.getEndTime());
                        List<Map<String, Object>> billlist = jdbcTemplate.queryForList(logListSql, p);
                        logger.info("查询SQL为 >>> " + logListSql);
                        logger.info("查询结果为 >>> " + billlist);
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
                        logger.info("data is >>> " + data);
                        logger.info("titles are >>> " + titles);
                        logger.info("billName is >>> " + billName);
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
                        List<Object> p = new ArrayList<>();
                        String logListSql = getBillType(p, billTypes[j], param.getBillDate(), param.getCustId(), param.getSupplierId(), param.getTransActionId(), param.getBatchId(), param.getEnterpriseName(), param.getStartTime(), param.getEndTime());
                        List<Map<String, Object>> billlist = jdbcTemplate.queryForList(logListSql, p);
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
        List<Object> p = new ArrayList<>();
        String logListSql = getBillType(p, param.getType(), param.getBillDate(), param.getCustId(), param.getSupplierId(), param.getTransActionId(), param.getBatchId(), param.getEnterpriseName(), param.getStartTime(), param.getEndTime());
        //return new Pagination().getPageData(logListSql, null, page, jdbcTemplate);
        return customerDao.sqlPageQuery(logListSql, page.getPageNum(), page.getPageSize(),p.toArray());
    }

    @Override
    public Map<String, String> queryCustomerConsumeTotal(String custId, String billDate) {
        Map<String, String> map = new HashMap<>();
        logger.info("查询账单的企业id是：" + custId + "查询账单时间范围是：" + billDate);
        StringBuffer customerConsumeTotalSql = new StringBuffer("SELECT IFNULL(SUM(s.amount) / 100,0) amountSum,IFNULL(SUM(s.prod_amount) / 100,0) supAmountSum  from stat_bill_month s\n" +
                "where s.cust_id =?");
        List<Object> customerParam = new ArrayList<>();
        //查询全部
        if ("0".equals(billDate) || StringUtil.isEmpty(billDate)) {
            customerConsumeTotalSql = new StringBuffer("SELECT IFNULL(SUM(s.amount) / 100,0)amountSum,IFNULL(SUM(s.prod_amount) / 100,0) supAmountSum from stat_bill_month s\n" +
                    "where s.cust_id =?");
            //查看一年
        } else if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            customerConsumeTotalSql.append(" AND stat_time>= ? ");
            customerParam.add(billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            customerConsumeTotalSql.append(" AND stat_time>= ? ");
            customerParam.add(billDate);
        } else {
            customerConsumeTotalSql.append(" AND stat_time= ? ");
            customerParam.add(billDate);
        }
        List<Map<String, Object>> consumeTotalsCount = sourceDao.sqlQuery(customerConsumeTotalSql.toString(), custId, customerParam.toArray());
        String consumeTotal = null, supAmountSum = null, profitAmount = null;
        if (consumeTotalsCount != null && consumeTotalsCount.size() > 0) {
            //企业消费金额
            consumeTotal = new BigDecimal(String.valueOf(consumeTotalsCount.get(0).get("amountSum"))).setScale(2, BigDecimal.ROUND_DOWN).toString();
            //供应商成本价格
            supAmountSum = new BigDecimal(String.valueOf(consumeTotalsCount.get(0).get("supAmountSum"))).setScale(2, BigDecimal.ROUND_DOWN).toString();
            //利润
            profitAmount = new BigDecimal(consumeTotal).subtract(new BigDecimal(supAmountSum)).setScale(2, BigDecimal.ROUND_DOWN).toString();
        }
        map.put("amountSum", consumeTotal);
        map.put("supAmountSum", supAmountSum);
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
    public String listSmsAccountLog(List<Object> p, String resourceId, String billDate, String custId, String supplierId, String touchId, String batchId, String enterpriseName, String startTime, String endTime) {
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        p.add(year);
        p.add(mouth);
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId,re.supplier_id, t1.cust_id,t1.batch_id batchId,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,u.realname ,u.account ,3 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount\n");
        sqlBuilder.append("FROM t_touch_sms_log t1 LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("LEFT JOIN t_customer_user u ON t1.user_id = u.id LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id WHERE ");
        sqlBuilder.append("t1.`status`=1001 AND YEAR (t1.create_time) = ? AND MONTH (t1.create_time) = ? ");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= ?");
            p.add(custId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= ? ");
            p.add(resourceId);
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= ?");
            p.add(touchId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and t1.batch_id= ?");
            p.add(batchId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like ? ");
            p.add("%" + enterpriseName + "%");
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sqlBuilder.append(" and t1.create_time between ? and ? ");
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
    public String listCallAccountLog(List<Object> p, String resourceId, String billDate, String custId, String supplierId, String touchId, String batchId, String enterpriseName, String startTime, String endTime) {
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        p.add(year);
        p.add(mouth);
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId,re.supplier_id, t1.cust_id,t1.batch_id batchId,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,u.realname ,u.account ,4 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount\n");
        sqlBuilder.append("FROM t_touch_voice_log t1 LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("LEFT JOIN t_customer_user u ON t1.user_id = u.id LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id WHERE ");
        sqlBuilder.append("t1.`status`=1001 AND YEAR (t1.create_time) = ? AND MONTH (t1.create_time) = ?");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= ?");
            p.add(custId);
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= ?");
            p.add(touchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= ?");
            p.add(resourceId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and t1.batch_id= ? ");
            p.add(batchId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like ? ");
            p.add("%" + enterpriseName + "%");
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sqlBuilder.append(" and t1.create_time between ? and ? ");
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
    public String listSeatsAccountLog(List<Object> p, String resourceId, String billDate, String custId, String supplierId, String touchId, String batchId, String enterpriseName, String startTime, String endTime) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.transaction_id transactionId,re.supplier_id, t1.cust_id,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,u.realname ,u.account ,5 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount\n");
        sqlBuilder.append("FROM t_transaction_bill t1 LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("LEFT JOIN t_customer_user u ON t1.user_id = u.id LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id WHERE t1.type = 5");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= ? ");
            p.add(custId);
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.transaction_id= ? ");
            p.add(touchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= ?");
            p.add(resourceId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and t1.batch_id=? ");
            p.add(batchId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like ? ");
            p.add("%" + enterpriseName + "%");
        }
        if (StringUtil.isNotEmpty(billDate)) {
            sqlBuilder.append(" and DATE_FORMAT(t1.create_time, '%Y%m') like ?");
            p.add(billDate);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sqlBuilder.append(" and t1.create_time between ? AND ?");
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
    public String listFixsAccountLog(List<Object> p, String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
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
        sqlBuilder.append("AND YEAR (nl.repair_time) = ?");
        sqlBuilder.append("AND MONTH (nl.repair_time) = ?");
        p.add(year);
        p.add(mouth);
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and nl.comp_id= ? ");
            p.add(custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and n1.id= ?");
            p.add(batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= ?");
            p.add(resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like ? ");
            p.add("%" + enterpriseName + "%");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= ?");
            p.add(touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ? AND ? ");
            p.add(startTime);
            p.add(endTime);
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
    public String listDeductionAccountLog(List<Object> p, String billDate, String custId, String supplierId, String touchId, String enterpriseName, String startTime, String endTime) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.transaction_id transactionId,t1.supplier_id,t1.cust_id,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,c.realname ,7 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount ,certificate imgUrl FROM t_transaction_bill t1\n");
        sqlBuilder.append(" LEFT JOIN t_customer_user c ON t1.user_id = c.id LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("WHERE 1 = 1 AND t1.type = 7 ");
        if (StringUtil.isNotEmpty(custId)) {
            p.add(custId);
            sqlBuilder.append(" and t1.cust_id= ? ");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            p.add(touchId);
            sqlBuilder.append(" and t1.transaction_id= ?");
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            p.add("%" + enterpriseName + "%");
            sqlBuilder.append(" and m.enterprise_name like ? ");
        }
       /* //查询资金扣减记录
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sqlBuilder.append(" and t1.create_time between ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
        }*/
        if (StringUtil.isNotEmpty(billDate)) {
            p.add(billDate);
            sqlBuilder.append(" AND DATE_FORMAT(t1.create_time, '%Y%m') like ? ");
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
    public String listRechargeAccountLog(List<Object> p, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.remark,t1.transaction_id transactionId, t1.cust_id,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,1 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount,certificate imgUrl FROM t_transaction_bill t1\n");
        sqlBuilder.append("LEFT JOIN t_customer m ON t1.cust_id = m.cust_id LEFT JOIN t_customer_user u ON t1.user_id = u.id ");
        sqlBuilder.append("WHERE 1 = 1 AND t1.type = 1");
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= ?");
            p.add(custId);
        }

        if (StringUtil.isNotEmpty(batchId)) {
            p.add(batchId);
            sqlBuilder.append(" and t1.id= ? ");
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            p.add("%" + enterpriseName + "%");
            sqlBuilder.append(" and m.enterprise_name like ? ");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            p.add(touchId);
            sqlBuilder.append(" and t1.transaction_id= ?");
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sqlBuilder.append(" and t1.create_time between ? and ? ");
        }
        if (StringUtil.isNotEmpty(billDate)) {
            p.add(billDate);
            sqlBuilder.append(" AND DATE_FORMAT(t1.create_time, '%Y%m') like  ?");
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
    public String listAdjustAccountLog(List<Object> p, String supplierId, String billDate, String touchId, String startTime, String endTime) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.remark,t1.transaction_id transactionId,t1.cust_id,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,9 AS payType ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount,certificate imgUrl  FROM t_transaction_bill t1\n");
        sqlBuilder.append("WHERE t1.type = 9");
        if (StringUtil.isNotEmpty(touchId)) {
            p.add(touchId);
            sqlBuilder.append(" and t1.transaction_id= ?");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            p.add(supplierId);
            sqlBuilder.append(" and t1.supplier_id= ? ");
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sqlBuilder.append(" and t1.create_time between ? and ? ");
        }
        if (StringUtil.isNotEmpty(billDate)) {
            p.add(billDate);
            sqlBuilder.append(" and DATE_FORMAT(t1.create_time, '%Y%m') like ?");
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
    public String getBillType(List<Object> p, String type, String billDate, String custId, String supplierId, String touchId, String batchId, String enterpriseName, String startTime, String endTime) {
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
            logListSql = listRechargeAccountLog(p, billDate, custId, supplierId, touchId, batchId, enterpriseName, startTime, endTime);
        }
        if ("2".equals(type)) {
            //快递扣费记录
            logListSql = listExpressAccountLog(p, resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        if ("3".equals(type)) {
            //短信扣费记录
            logListSql = listSmsAccountLog(p, resourceId, billDate, custId, supplierId, touchId, batchId, enterpriseName, startTime, endTime);
        }
        if ("4".equals(type)) {
            //通话扣费记录
            logListSql = listCallAccountLog(p, resourceId, billDate, custId, supplierId, touchId, batchId, enterpriseName, startTime, endTime);
        }
        if ("5".equals(type)) {
            //座席扣费记录
            logListSql = listSeatsAccountLog(p, resourceId, billDate, custId, supplierId, touchId, batchId, enterpriseName, startTime, endTime);
        }
        if ("6".equals(type)) {
            //修复扣费记录
            logListSql = listFixsAccountLog(p, resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        if ("7".equals(type)) {
            //扣减记录
            logListSql = listDeductionAccountLog(p, billDate, custId, supplierId, touchId, enterpriseName, startTime, endTime);
        }
        if ("9".equals(type)) {
            //调账记录
            logListSql = listAdjustAccountLog(p, supplierId, billDate, touchId, startTime, endTime);
        }
        //mac修复
        if ("10".equals(type)) {
            logListSql = listMacAccountLog(p, resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        // imei修复
        if ("11".equals(type)) {
            logListSql = listImeiAccountLog(p, resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        //12 地址修复
        if ("12".equals(type)) {
            logListSql = listAddressFixAccountLog(p, resourceId, billDate, custId, supplierId, batchId, enterpriseName, startTime, endTime, touchId);
        }
        return logListSql;
    }

    /**
     * @description 快递账单查询
     * @author:duanliying
     * @method
     * @date: 2019/1/3 11:11
     */
    private String listExpressAccountLog(List<Object> p, String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
        Integer year = null;
        Integer mouth = null;
        Map<String, Integer> yearMessage = getYearMessage(billDate);
        if (yearMessage.size() > 0) {
            year = yearMessage.get("year");
            mouth = yearMessage.get("month");
        }
        StringBuilder sqlBuilder = new StringBuilder("SELECT t1.touch_id transactionId,t1.`status`,re.supplier_id, t1.cust_id,t1.batch_id batchId,t1.create_time billDate,CONVERT(t1.amount / 100,DECIMAL(15,2)) AS totalAmount,u.realname ,u.account ,2 AS payType,m.enterprise_name ,CONVERT(t1.prod_amount / 100,DECIMAL(15,2)) prodAmount\n");
        sqlBuilder.append("FROM t_touch_express_log t1 LEFT JOIN t_customer m ON t1.cust_id = m.cust_id\n");
        sqlBuilder.append("LEFT JOIN t_customer_user u ON t1.user_id = u.id LEFT JOIN t_market_resource re ON re.resource_id = t1.resource_id WHERE YEAR (t1.create_time) = ? AND MONTH (t1.create_time) = ? ");
        p.add(year);
        p.add(mouth);
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and t1.cust_id= ? ");
            p.add(custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and t1.id= ? ");
            p.add(batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= ? ");
            p.add(resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like ? ");
            p.add("%" + enterpriseName + "%");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= ? ");
            p.add(touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sqlBuilder.append(" and t1.create_time between ? and ? ");
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
    private String listMacAccountLog(List<Object> p, String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
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
        sqlBuilder.append("AND YEAR (nl.repair_time) = ? ");
        sqlBuilder.append("AND MONTH (nl.repair_time) = ? ");
        p.add(year);
        p.add(mouth);
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and nl.comp_id= ? ");
            p.add(custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and nl.id= ? ");
            p.add(batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= ? ");
            p.add(resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like ? ");
            p.add("%" + enterpriseName + "%");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= ? ");
            p.add(touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sqlBuilder.append(" and t1.create_time between ? and ? ");
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
    private String listImeiAccountLog(List<Object> p, String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
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
        sqlBuilder.append("AND YEAR (nl.repair_time) = ? ");
        sqlBuilder.append("AND MONTH (nl.repair_time) = ? ");
        p.add(year);
        p.add(mouth);
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and nl.comp_id= ? ");
            p.add(custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and nl.id= ? ");
            p.add(batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= ? ");
            p.add(resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like ? ");
            p.add("%" + enterpriseName + "%");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= ? ");
            p.add(touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sqlBuilder.append(" and t1.create_time between ? and ? ");
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
    private String listAddressFixAccountLog(List<Object> p, String resourceId, String billDate, String custId, String supplierId, String batchId, String enterpriseName, String startTime, String endTime, String touchId) {
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
        sqlBuilder.append("AND YEAR (nl.repair_time) = ? ");
        sqlBuilder.append("AND MONTH (nl.repair_time) = ? ");
        p.add(year);
        p.add(mouth);
        if (StringUtil.isNotEmpty(custId)) {
            sqlBuilder.append(" and nl.comp_id= ?");
            p.add(custId);
        }
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" and nl.id= ? ");
            p.add(batchId);
        }
        if (StringUtil.isNotEmpty(resourceId)) {
            sqlBuilder.append(" and t1.resource_id= ? ");
            p.add(resourceId);
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sqlBuilder.append(" and m.enterprise_name like ? ");
            p.add("%" + enterpriseName + "%");
        }
        if (StringUtil.isNotEmpty(touchId)) {
            sqlBuilder.append(" and t1.touch_id= ? ");
            p.add(touchId);
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sqlBuilder.append(" and t1.create_time between ? and ? ");
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
        List<Object> p = new ArrayList<>();
        StringBuilder billCustomer = new StringBuilder("SELECT GROUP_CONCAT(s.resource_id) channel,s.bill_type type,SUM(s.prod_amount) /100 costAmountSum,SUM(s.amount) /100 consumeAmountsum  FROM stat_bill_month s WHERE s.cust_id = ?");
        if ("0".equals(billDate) || StringUtil.isEmpty(billDate)) {
            custSumMoneySql = new StringBuffer("SELECT IFNULL(SUM(amount), 0) /100 totalAcount FROM stat_bill_month WHERE cust_id = ?");
            //查看一年
        } else if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            custSumMoneySql.append(" AND stat_time>= ? ");
            p.add(billDate);
            billCustomer.append(" AND stat_time>= ? ");
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            custSumMoneySql.append(" AND stat_time>=? ");
            billCustomer.append(" AND stat_time>= ? ");
            p.add(billDate);
        } else {
            custSumMoneySql.append(" AND stat_time=? ");
            billCustomer.append(" AND stat_time= ? ");
            p.add(billDate);
        }
        if (customerMessage.size() > 0) {
            headerData = customerMessage.get(0);
            //查询企业总消费金额
            List<Map<String, Object>> totalAcount = sourceDao.sqlQuery(custSumMoneySql.toString(), customerId, p.toArray());
            if (totalAcount.size() > 0) {
                headerData.put("totalAcount", totalAcount.get(0).get("totalAcount"));
            }
        }

        //企业账单信息
        billCustomer.append(" GROUP BY s.bill_type");
        logger.info("查询企业账单，企业id是：" + customerId);
        List<Map<String, Object>> customerBillList = sourceDao.sqlQuery(billCustomer.toString(), customerId, p.toArray());
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
        //失联修复 总收入和总支出
        Map<String, Object> totalIncomeAndExpenditure = new HashMap<>(16);
        BigDecimal totalPay = BigDecimal.ZERO;
        if (customerBillList != null && customerBillList.size() != 0) {
            for (Map<String, Object> e : customerBillList) {
                BigDecimal pay = new BigDecimal(String.valueOf(e.get("consumeAmountsum")));
                totalPay = totalPay.add(pay);
            }
        }
        totalIncomeAndExpenditure.put("totalPay", totalPay);
        //添加企业充值扣减(按月展示)
        String sql = "select type , SUM(amount)/100 consumeAmountsum from t_transaction_bill WHERE cust_id =?  AND type in (" + TransactionEnum.BALANCE_DEDUCTION.getType() + "," + TransactionEnum.BALANCE_RECHARGE.getType() + ") AND DATE_FORMAT(create_time, '%Y%m') like " + billDate + " GROUP BY type";
        List<Map<String, Object>> customerMoneyList = sourceDao.sqlQuery(sql, customerId);
        if (customerMoneyList != null && customerMoneyList.size() > 0) {
            customerBillList.addAll(customerMoneyList);
        }
        if (customerMoneyList != null && customerMoneyList.size() > 0) {
            BigDecimal totalIncome;
            for (Map<String, Object> e : customerMoneyList) {
                if ("1".equals(String.valueOf(e.get("type")))) {
                    totalIncome = new BigDecimal(String.valueOf(e.get("consumeAmountsum")));
                    totalIncomeAndExpenditure.put("totalIncome", totalIncome);
                }
            }
        }
        resultMap.put("customerMessage", headerData);
        resultMap.put("billMessage", customerBillList);
        resultMap.put("total", customerBillList.size());
        resultMap.put("totalMoneyInfo", totalIncomeAndExpenditure);
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
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(billDate)) {
            p.add(billDate);
            billSupplierSql.append(" and stat_time = ?");
        }
        if (StringUtil.isNotEmpty(custId)) {
            p.add(custId);
            billSupplierSql.append(" and cust_id = ?");
        }
        if (StringUtil.isNotEmpty(type)) {
            p.add(type);
            billSupplierSql.append(" and type = ? ");
        }
        List<Map<String, Object>> billList = sourceDao.sqlQuery(billSupplierSql.toString(), p.toArray());
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
            Customer custMessage = customerDao.getCustMessage(custId);
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
        List<Object> p = new ArrayList<>();
        if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            profitAmountSql.append(" AND s.stat_time>=? ");
            p.add(billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            profitAmountSql.append(" AND s.stat_time>=?");
            p.add(billDate);
        } else if (!"0".equals(billDate)) {
            profitAmountSql.append(" AND s.stat_time=?");
            p.add(billDate);
        }
        List<Map<String, Object>> consumeTotalsCount = sourceDao.sqlQuery(profitAmountSql.toString(), p.toArray());
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

    public Map<String, Object> getListCustomerBill(CustomerBillQueryParam param) throws Exception {
        StringBuffer querySql = new StringBuffer("SELECT n.comp_id custId,b.batch_id batchId,n.batch_name batchName,DATE_FORMAT(n.upload_time,'%Y-%m-%d %H:%i:%s') AS uploadTime,IFNULL(SUM(b.amount) / 100,0) amount ,IFNULL(SUM(b.prod_amount) / 100,0) prodAmount , ");
        querySql.append("( SELECT COUNT(id) FROM nl_batch_detail WHERE batch_id = b.batch_id ) fixNumber ");
        querySql.append("FROM stat_bill_month b LEFT JOIN nl_batch n ON b.batch_id = n.id  LEFT JOIN t_customer c ON  n.comp_id = c.cust_id ");
        querySql.append("WHERE n.certify_type = 3 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getCustomerId())) {
            p.add(param.getCustomerId());
            querySql.append("AND n.comp_id = ? ");
        }
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            p.add("%" + param.getEnterpriseName() + "%");
            querySql.append("AND c.enterprise_name like ? ");
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            p.add(param.getBatchId());
            querySql.append("AND n.id =? ");
        }
        if (StringUtil.isNotEmpty(param.getBatchName())) {
            p.add("%" + param.getBatchName() + "%");
            querySql.append("AND n.batch_name like ? ");
        }
        String billDate = param.getBillDate();
        //0查詢全部 1查詢1年 2 查看近半年 201901查詢具体某月账单
        if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            querySql.append(" AND stat_time>= ? ");
            p.add(billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            querySql.append(" AND stat_time>=? ");
            p.add(billDate);
        } else if (!"0".equals(billDate)) {
            querySql.append(" AND stat_time=? ");
            p.add(billDate);
        }
        querySql.append(" GROUP BY b.batch_id order by n.repair_time desc ");
        Page data = customerDao.sqlPageQuery(querySql.toString(), param.getPageNum(), param.getPageSize(), p.toArray());
        Map<String, Object> map = new HashMap<>();
        if (data != null) {
            List<Map<String, Object>> list = data.getData();
            double custSumAmount = 0;
            for (int i = 0; i < list.size(); i++) {
                logger.info("企业消费金额是：" + String.valueOf(list.get(i).get("amount")) + "成本费用是：" + String.valueOf(list.get(i).get("prodAmount")));
                String profitAmount = new BigDecimal(String.valueOf(list.get(i).get("amount"))).subtract(new BigDecimal(String.valueOf(list.get(i).get("prodAmount")))).setScale(2, BigDecimal.ROUND_DOWN).toString();
                list.get(i).put("profitAmount", profitAmount);
                custSumAmount += new BigDecimal(String.valueOf(list.get(i).get("amount"))).doubleValue();
                //根据批次id查询企业名称
                String custId = String.valueOf(list.get(i).get("custId"));
                String enterpriseName = customerDao.getEnterpriseName(custId);
                list.get(i).put("custName", enterpriseName);
            }
            map.put("custSumAmount", custSumAmount);
            map.put("data", data.getData());
            map.put("total", data.getTotal());
        }

        return map;
    }

    public List<Map<String, Object>> listCustomerBillExport(CustomerBillQueryParam param) throws Exception {
        StringBuffer querySql = new StringBuffer("SELECT n.comp_id custId,b.batch_id batchId,n.batch_name batchName,n.upload_time uploadTime,IFNULL(SUM(b.amount) / 100,0) amount ,IFNULL(SUM(b.prod_amount) / 100,0) prodAmount , ");
        querySql.append("( SELECT COUNT(id) FROM nl_batch_detail WHERE batch_id = b.batch_id ) fixNumber ");
        querySql.append("FROM stat_bill_month b LEFT JOIN nl_batch n ON b.batch_id = n.id  LEFT JOIN t_customer c ON  n.comp_id = c.cust_id ");
        querySql.append("WHERE n.certify_type = 3 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getCustomerId())) {
            p.add(param.getCustomerId());
            querySql.append("AND n.comp_id =?");
        }
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            p.add("%" + param.getEnterpriseName() + "%");
            querySql.append("AND c.enterprise_name like ? ");
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            p.add(param.getBatchId());
            querySql.append("AND n.id =?");
        }
        if (StringUtil.isNotEmpty(param.getBatchName())) {
            p.add("%" + param.getBatchName() + "%");
            querySql.append("AND n.batch_name like ? ");
        }
        String billDate = param.getBillDate();
        //0查詢全部 1查詢1年 2 查看近半年 201901查詢具体某月账单
        if ("1".equals(billDate)) {
            p.add(billDate);
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            querySql.append(" AND stat_time>=?");
            //查看近半年
        } else if ("2".equals(billDate)) {
            p.add(billDate);
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            querySql.append(" AND stat_time>=?");
        } else if (!"0".equals(billDate)) {
            p.add(billDate);
            querySql.append(" AND stat_time=?");
        }
        querySql.append(" GROUP BY b.batch_id ORDER BY n.upload_time DESC");
        List<Map<String, Object>> data = jdbcTemplate.queryForList(querySql.toString(), p.toArray());
        if (data != null) {
            List<Map<String, Object>> list = data;
            for (int i = 0; i < list.size(); i++) {
                logger.info("企业消费金额是：" + String.valueOf(list.get(i).get("amount")) + "成本费用是：" + String.valueOf(list.get(i).get("prodAmount")));
                String profitAmount = new BigDecimal(String.valueOf(list.get(i).get("amount"))).subtract(new BigDecimal(String.valueOf(list.get(i).get("prodAmount")))).setScale(2, BigDecimal.ROUND_DOWN).toString();
                list.get(i).put("profitAmount", profitAmount);
                //根据批次id查询企业名称
                String custId = String.valueOf(list.get(i).get("custId"));
                String enterpriseName = customerDao.getEnterpriseName(custId);
                list.get(i).put("custName", enterpriseName);
            }

        }
        return data;
    }

    /**
     * 查询账单详情页
     *
     * @param param
     * @return
     */
    public Page getBillDetailList(CustomerBillQueryParam param) {
        StringBuffer querySql = new StringBuffer("SELECT d.batch_id batchId, DATE_FORMAT(d.fix_time ,'%Y-%m-%d %H:%i:%s') AS fixTime,d.label_four address,l.touch_id expressId,l.resource_id expressResource, d.resource_id fixResource,l.create_time sendTime,d.label_one name,d.label_two phone, d.label_five peopleId, IFNULL(d.amount / 100, 0) amount, IFNULL(d.prod_amount / 100, 0) prodAmount, IFNULL(l.amount / 100, 0) expressAmount, IFNULL(l.prod_amount / 100, 0) exProdAmount,");
        querySql.append("(SELECT s.`name` FROM t_market_resource r LEFT JOIN t_supplier s ON s.supplier_id = r.supplier_id WHERE r.resource_id = l.resource_id) expressSupplier,");
        querySql.append("(SELECT s.`name` FROM t_market_resource r LEFT JOIN t_supplier s ON s.supplier_id = r.supplier_id WHERE r.resource_id = d.resource_id) fixSupplier");
        querySql.append(" FROM nl_batch_detail d LEFT JOIN t_touch_express_log l ON d.touch_id = l.touch_id ");
        querySql.append("WHERE 1=1 AND d.`status`=1 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            p.add(param.getBatchId());
            querySql.append(" AND d.batch_id =?");
        }
        if (StringUtil.isNotEmpty(param.getExpressId())) {
            p.add(param.getExpressId());
            querySql.append("AND l.id =?");
        }
        if (StringUtil.isNotEmpty(param.getPeopleId())) {
            p.add(param.getPeopleId());
            querySql.append("AND d.label_five =?");
        }
        if (StringUtil.isNotEmpty(param.getName())) {
            p.add("%" + param.getName() + "%");
            querySql.append("AND d.label_one like ? ");
        }
        if (StringUtil.isNotEmpty(param.getPhone())) {
            p.add(param.getPhone());
            querySql.append("AND d.label_one =?");
        }
        Page page = customerDao.sqlPageQuery(querySql.toString(), param.getPageNum(), param.getPageSize(), p.toArray());
        if (page != null) {
            List<Map<String, Object>> list = page.getData();
            for (int i = 0; i < list.size(); i++) {
                //根据资源id查询所属渠道
                if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("amount"))) && StringUtil.isNotEmpty(String.valueOf(list.get(i).get("expressAmount")))) {
                    logger.info("amount" + String.valueOf(list.get(i).get("amount")));
                    String sumAmount = new BigDecimal(String.valueOf(list.get(i).get("amount"))).add(new BigDecimal(String.valueOf(list.get(i).get("expressAmount")))).setScale(2, BigDecimal.ROUND_DOWN).toString();
                    list.get(i).put("sumAmount", sumAmount);

                }
                if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("amount"))) && StringUtil.isNotEmpty(String.valueOf(list.get(i).get("prodAmount")))) {
                    String profit = new BigDecimal(String.valueOf(list.get(i).get("amount"))).subtract(new BigDecimal(String.valueOf(list.get(i).get("prodAmount")))).setScale(2, BigDecimal.ROUND_DOWN).toString();
                    list.get(i).put("profit", profit);
                }
                //拼接地址展示结构
                if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("address")))) {
                    JSONObject jsonObject = JSON.parseObject(String.valueOf(list.get(i).get("address")));
                    String prov = jsonObject.getString("prov");
                    String city = jsonObject.getString("city");
                    String address = "";
                    if (StringUtil.isNotEmpty(prov)) {
                        address += prov;
                    }
                    if (StringUtil.isNotEmpty(city)) {
                        address += city;
                    }
                    list.get(i).put("address", address);
                }
            }
        }
        return page;
    }

    @Override
    public List<Map<String, Object>> getBillDetailExport(CustomerBillQueryParam param) {
        StringBuffer querySql = new StringBuffer("SELECT d.batch_id batchId, d.site address,l.id expressId,l.resource_id expressResource, d.resource_id fixResource,l.create_time sendTime,d.label_one name,d.label_two phone, d.label_five peopleId, IFNULL(d.amount / 100, 0) amount, IFNULL(d.prod_amount / 100, 0) prodAmount, IFNULL(l.amount / 100, 0) expressAmount, ");
        querySql.append("(SELECT s.`name` FROM t_market_resource r LEFT JOIN t_supplier s ON s.supplier_id = r.supplier_id WHERE r.resource_id = l.resource_id) expressSupplier,");
        querySql.append("(SELECT s.`name` FROM t_market_resource r LEFT JOIN t_supplier s ON s.supplier_id = r.supplier_id WHERE r.resource_id = d.resource_id) fixSupplier");
        querySql.append(" FROM nl_batch_detail d LEFT JOIN t_touch_express_log l ON d.touch_id = l.touch_id ");
        querySql.append("WHERE 1=1 AND d.`status`=1 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            p.add(param.getBatchId());
            querySql.append(" AND d.batch_id =? ");
        }
        if (StringUtil.isNotEmpty(param.getExpressId())) {
            p.add(param.getExpressId());
            querySql.append("AND l.id =?");
        }
        if (StringUtil.isNotEmpty(param.getPeopleId())) {
            p.add(param.getPeopleId());
            querySql.append("AND d.label_five =?");
        }
        if (StringUtil.isNotEmpty(param.getName())) {
            p.add("%" + param.getName() + "%");
            querySql.append("AND d.label_one like ? ");
        }
        if (StringUtil.isNotEmpty(param.getPhone())) {
            p.add(param.getPhone());
            querySql.append("AND d.label_one =? ");
        }
        List<Map<String, Object>> page = jdbcTemplate.queryForList(querySql.toString(), p.toArray());
        if (page != null) {
            List<Map<String, Object>> list = page;
            for (int i = 0; i < list.size(); i++) {
                //根据资源id查询所属渠道
                if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("amount"))) && StringUtil.isNotEmpty(String.valueOf(list.get(i).get("expressAmount")))) {
                    logger.info("amount" + String.valueOf(list.get(i).get("amount")));
                    String sumAmount = new BigDecimal(String.valueOf(list.get(i).get("amount"))).add(new BigDecimal(String.valueOf(list.get(i).get("expressAmount")))).setScale(2, BigDecimal.ROUND_DOWN).toString();
                    list.get(i).put("sumAmount", sumAmount);

                }
                if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("amount"))) && StringUtil.isNotEmpty(String.valueOf(list.get(i).get("prodAmount")))) {
                    String profit = new BigDecimal(String.valueOf(list.get(i).get("amount"))).subtract(new BigDecimal(String.valueOf(list.get(i).get("prodAmount")))).setScale(2, BigDecimal.ROUND_DOWN).toString();
                    list.get(i).put("profit", profit);
                }
            }
        }
        return page;
    }

    public Page getSupBillDetailList(CustomerBillQueryParam param) {
        StringBuffer querySql = new StringBuffer("SELECT d.batch_id batchId, d.site address,l.id expressId,l.resource_id expressResource, d.resource_id fixResource,l.create_time sendTime,d.fix_time fixTime,d.label_one name,d.label_two phone, d.label_five peopleId, IFNULL(d.amount / 100, 0) amount, IFNULL(d.prod_amount / 100, 0) prodAmount, IFNULL(l.prod_amount / 100, 0) expressAmount ");
        querySql.append(" FROM nl_batch_detail d LEFT JOIN t_touch_express_log l ON d.touch_id = l.touch_id ");
        querySql.append("WHERE 1=1 AND d.`status`=1 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            querySql.append(" AND d.batch_id =? ");
            p.add(param.getBatchId());
        }
        if (StringUtil.isNotEmpty(param.getExpressId())) {
            querySql.append("AND l.id =? ");
            p.add(param.getExpressId());
        }
        if (StringUtil.isNotEmpty(param.getPeopleId())) {
            querySql.append("AND d.label_five =? ");
            p.add(param.getPeopleId());
        }
        if (StringUtil.isNotEmpty(param.getName())) {
            querySql.append("AND d.label_one like ? ");
            p.add("%" + param.getName() + "%");
        }
        //type 1 数据  2快递
        MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(param.getSupplierId(), NumberConvertUtil.parseInt(param.getType()));
        if (marketResourceEntity != null) {
            Integer resourceId = marketResourceEntity.getResourceId();
            if (StringUtil.isNotEmpty(param.getSupplierId()) && "1".equals(param.getType())) {
                querySql.append("AND d.resource_id = ?");
                p.add(resourceId);
            } else if (StringUtil.isNotEmpty(param.getResourceId()) && "2".equals(param.getType())) {
                querySql.append("AND l.resource_id = ?");
                p.add(resourceId);
            }
            if (StringUtil.isNotEmpty(param.getPhone())) {
                querySql.append("AND d.label_one =? ");
                p.add(param.getPhone());
            }
        }
        querySql.append(" GROUP BY d.touch_id");
        return customerDao.sqlPageQuery(querySql.toString(), param.getPageNum(), param.getPageSize(), p.toArray());
    }

    /**
     * 供应商账单三级页面(信函)
     *
     * @param param
     * @return
     */
    public Page getSupBillDetailList(SupplierBillQueryParam param) {
        //type 1 数据  2快递
        MarketResourceEntity marketResourceEntity = sourceDao.getResourceId(param.getSupplierId(), NumberConvertUtil.parseInt(param.getType()));
        if (marketResourceEntity == null) {
            return new Page();
        }
        StringBuffer querySql = new StringBuffer("SELECT d.batch_id batchId, d.label_four address,l.touch_id expressId,l.resource_id expressResource, d.resource_id fixResource,DATE_FORMAT(l.create_time ,'%Y-%m-%d %H:%i:%s') AS sendTime,DATE_FORMAT(d.fix_time ,'%Y-%m-%d %H:%i:%s') AS fixTime,d.label_one name,d.label_two phone, d.label_five peopleId, IFNULL(d.amount / 100, 0) amount, IFNULL(d.prod_amount / 100, 0) prodAmount, IFNULL(l.prod_amount / 100, 0) expressAmount ");
        querySql.append(" FROM nl_batch_detail d left JOIN t_touch_express_log l ON d.touch_id = l.touch_id ");
        querySql.append("WHERE 1=1 AND d.`status`=1 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            p.add(param.getBatchId());
            querySql.append(" AND d.batch_id =?");
        }
        if (StringUtil.isNotEmpty(param.getExpressId())) {
            p.add(param.getExpressId());
            querySql.append(" AND l.id =?");
        }
        if (StringUtil.isNotEmpty(param.getPeopleId())) {
            p.add(param.getPeopleId());
            querySql.append(" AND d.label_five =?");
        }
        if (StringUtil.isNotEmpty(param.getName())) {
            p.add("%" + param.getName() + "%");
            querySql.append(" AND d.label_one like ? ");
        }
        //type 1 数据  2快递
        Integer resourceId = marketResourceEntity.getResourceId();
        if ("1".equals(param.getType())) {
            p.add(resourceId);
            querySql.append(" AND d.resource_id = ?");
        } else if ("2".equals(param.getType())) {
            p.add(resourceId);
            querySql.append(" AND l.resource_id = ?");
        }
        if (StringUtil.isNotEmpty(param.getPhone())) {
            p.add(param.getPhone());
            querySql.append(" AND d.label_two =?");
        }
        querySql.append(" GROUP BY d.touch_id");
        Page page = customerDao.sqlPageQuery(querySql.toString(), param.getPageNum(), param.getPageSize(), p.toArray());
        List<Map<String, Object>> list = page.getData();
        if (page != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                //拼接地址展示结构
                if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("address")))) {
                    JSONObject jsonObject = JSON.parseObject(String.valueOf(list.get(i).get("address")));
                    String prov = jsonObject.getString("prov");
                    String city = jsonObject.getString("city");
                    String address = "";
                    if (StringUtil.isNotEmpty(prov)) {
                        address += prov;
                    }
                    if (StringUtil.isNotEmpty(city)) {
                        address += city;
                    }
                    list.get(i).put("address", address);
                }
            }
        }
        return page;
    }

    /**
     * 供应商账单二级页面（信函）
     *
     * @param param
     * @return
     */
    public Page getListSupplierBill(SupplierBillQueryParam param) {
        //查询供应商下面所有的资源
        List<MarketResourceLogDTO> marketResourceLogDTOS = supplierDao.listMarketResourceBySupplierId(String.valueOf(param.getSupplierId()));
        String resourceIds = "";
        if (marketResourceLogDTOS != null && marketResourceLogDTOS.size() > 0) {
            for (int i = 0; i < marketResourceLogDTOS.size(); i++) {
                resourceIds += marketResourceLogDTOS.get(i).getResourceId() + ",";
            }
            resourceIds = resourceIds.substring(0, resourceIds.length() - 1);
        }
        StringBuffer querySql = new StringBuffer("SELECT n.comp_id custId,b.batch_id batchId,n.batch_name batchName,DATE_FORMAT(n.upload_time  ,'%Y-%m-%d %H:%i:%s') AS uploadTime,DATE_FORMAT(n.repair_time  ,'%Y-%m-%d %H:%i:%s') AS repairTime,IFNULL(SUM(b.amount) / 100,0) amount ,IFNULL(SUM(b.prod_amount) / 100,0) prodAmount , ");
        querySql.append("( SELECT COUNT(id) FROM nl_batch_detail WHERE batch_id = b.batch_id ) fixNumber ");
        querySql.append("FROM stat_bill_month b LEFT JOIN nl_batch n ON b.batch_id = n.id  LEFT JOIN t_customer c ON  n.comp_id = c.cust_id ");
        querySql.append("WHERE n.certify_type = 3 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            p.add("%" + param.getEnterpriseName() + "%");
            querySql.append("AND c.enterprise_name like ? ");
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            p.add(param.getBatchId());
            querySql.append("AND n.id =? ");
        }
        if (StringUtil.isNotEmpty(param.getBatchName())) {
            p.add("%" + param.getBatchName() + "%");
            querySql.append("AND n.batch_name like ?");
        }
        if (StringUtil.isNotEmpty(resourceIds)) {
            querySql.append("AND b.resource_id in  (?)");
            p.add(resourceIds);
        }
        String billDate = param.getBillDate();
        //0查詢全部 1查詢1年 2 查看近半年 201901查詢具体某月账单
        if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            querySql.append(" AND stat_time>= ? ");
            p.add(billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            querySql.append(" AND stat_time>= ? ");
            p.add(billDate);
        } else if (!"0".equals(billDate)) {
            querySql.append(" AND stat_time=? ");
            p.add(billDate);
        }
        querySql.append(" GROUP BY b.batch_id order by n.repair_time desc");
        Page data = customerDao.sqlPageQuery(querySql.toString(), param.getPageNum(), param.getPageSize(), p.toArray());
        if (data != null) {
            List<Map<String, Object>> list = data.getData();
            for (int i = 0; i < list.size(); i++) {
                //根据批次id查询企业名称
                String custId = String.valueOf(list.get(i).get("custId"));
                String enterpriseName = customerDao.getEnterpriseName(custId);
                list.get(i).put("custName", enterpriseName);
            }
        }
        return data;
    }

    @Override
    public List<Map<String, Object>> getListSupplierBillExport(SupplierBillQueryParam param) {
        //查询供应商下面所有的资源
        List<MarketResourceLogDTO> marketResourceLogDTOS = supplierDao.listMarketResourceBySupplierId(String.valueOf(param.getSupplierId()));
        String resourceIds = "";
        if (marketResourceLogDTOS != null && marketResourceLogDTOS.size() > 0) {
            for (int i = 0; i < marketResourceLogDTOS.size(); i++) {
                resourceIds += marketResourceLogDTOS.get(i).getResourceId() + ",";
            }
            resourceIds = resourceIds.substring(0, resourceIds.length() - 1);
        }
        StringBuffer querySql = new StringBuffer("SELECT n.comp_id custId,b.batch_id batchId,n.batch_name batchName,n.upload_time uploadTime,n.repair_time repairTime,IFNULL(SUM(b.amount) / 100,0) amount ,IFNULL(SUM(b.prod_amount) / 100,0) prodAmount , ");
        querySql.append("( SELECT COUNT(id) FROM nl_batch_detail WHERE batch_id = b.batch_id ) fixNumber ");
        querySql.append("FROM stat_bill_month b LEFT JOIN nl_batch n ON b.batch_id = n.id  LEFT JOIN t_customer c ON  n.comp_id = c.cust_id ");
        querySql.append("WHERE n.certify_type = 3 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            p.add("%" + param.getEnterpriseName() + "%");
            querySql.append("AND c.enterprise_name like ? ");
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            p.add(param.getBatchId());
            querySql.append("AND n.id = ? ");
        }
        if (StringUtil.isNotEmpty(param.getBatchName())) {
            p.add("%" + param.getBatchName() + "%");
            querySql.append("AND n.batch_name like ? ");
        }
        if (StringUtil.isNotEmpty(resourceIds)) {
            querySql.append("AND b.resource_id in  (" + resourceIds + ")");
        }
        String billDate = param.getBillDate();
        //0查詢全部 1查詢1年 2 查看近半年 201901查詢具体某月账单
        if ("1".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(12).format(DateTimeFormatter.ofPattern("yyyyMM"));
            querySql.append(" AND stat_time>= ?");
            p.add(billDate);
            //查看近半年
        } else if ("2".equals(billDate)) {
            billDate = LocalDateTime.now().minusMonths(6).format(DateTimeFormatter.ofPattern("yyyyMM"));
            querySql.append(" AND stat_time>=?");
            p.add(billDate);
        } else if (!"0".equals(billDate)) {
            querySql.append(" AND stat_time= ? ");
            p.add(billDate);
        }
        querySql.append(" GROUP BY b.batch_id ");
//        Page data = customerDao.sqlPageQuery(querySql.toString(), param.getPageNum(), param.getPageSize());
        List<Map<String, Object>> data = jdbcTemplate.queryForList(querySql.toString(), p.toArray());
        if (data != null) {
            for (int i = 0; i < data.size(); i++) {
                //根据批次id查询企业名称
                String custId = String.valueOf(data.get(i).get("custId"));
                String enterpriseName = customerDao.getEnterpriseName(custId);
                data.get(i).put("custName", enterpriseName);
            }
        }
        return data;
    }

    @Resource
    OrderDao orderDao;
    @Resource
    TransactionDao transactionDao;

    public Map<String, Object> queryOnlineCustomerBill(CustomerBillQueryParam param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String currentDate = DateUtil.fmtDateToStr(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM);

        StringBuilder sumSql = new StringBuilder("select sum(amount)/1000 as totalAmount from t_order t1 where t1.order_state=2 ");

        StringBuilder sql = new StringBuilder("SELECT " +
                "t1.enpterprise_name AS enterpriseName, " +
                "t1.cust_id AS customerId, " +
                "t1.pay_type AS payType, " +
                "DATE_FORMAT(now(),'%Y-%m') as billDate, " +
                "1 as billCycle, " +
                "sum(t1.amount)/1000 AS totalAmount " +
                " FROM " +
                "t_order t1 where t1.order_state=2 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            p.add(param.getEnterpriseName());
            sql.append(" and t1.enpterprise_name=?");
            sumSql.append(" and t1.enpterprise_name=?");
        }
        if (StringUtil.isNotEmpty(param.getCustomerId())) {
            p.add(param.getCustomerId());
            sql.append(" and t1.cust_id=?");
            sumSql.append(" and t1.cust_id=?");
        }
        if (StringUtil.isNotEmpty(param.getType())) {
            p.add(param.getType());
            sql.append(" and t1.pay_type=?");
            sumSql.append(" and t1.pay_type=?");
        }
//        if (StringUtil.isNotEmpty(param.getBillDate())) {
//            if (currentDate.equals(param.getBillDate())) {
//                sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getBillDate())).append("-01 00:00:00").append("'");
//                sql.append(" and '").append(DateUtil.fmtDateToStr(new Date(), DateUtil.YYYY_MM_DD_HH_mm_ss)).append("'");
//                sumSql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getBillDate())).append("-01 00:00:00").append("'");
//                sumSql.append(" and '").append(DateUtil.fmtDateToStr(new Date(), DateUtil.YYYY_MM_DD_HH_mm_ss)).append("'");
//            } else {
//                sql.append(" and date_format(t1.create_time,'%Y-%m')='").append(StringEscapeUtils.escapeSql(param.getBillDate())).append("'");
//                sumSql.append(" and date_format(t1.create_time,'%Y-%m')='").append(StringEscapeUtils.escapeSql(param.getBillDate())).append("'");
//            }
//        }
        sql.append(" GROUP BY t1.cust_id ");
        List list = orderDao.sqlQuery(sumSql.toString(), p.toArray());
        map.put("totalAmount", list.size() > 0 && list.get(0) != null ? list.get(0).toString() : 0);
        Page page = orderDao.sqlPageQuery(sql.toString(), param.getPageNum(), param.getPageSize(), p.toArray());
        map.put("total", page.getTotal());
        map.put("customerBillList", page.getData());
        return map;
    }

    public Map<String, Object> queryCustomerBill_V1(CustomerBillQueryParam param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        Page page = customerDao.listCustomer(0, param.getPageNum(), param.getPageSize());

        String currentDate = DateUtil.fmtDateToStr(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM);
        double totalAmount = 0.0;
        List<Map<String, Object>> list = new ArrayList<>();
        if (page != null && page.getData() != null) {
            LocalDateTime now = LocalDateTime.now();
            Map<String, Object> dataMap;
            List<Map<String, Object>> dataList = new ArrayList<>();
            Customer customer;
            double customerAmount = 0;
            for (int i = 0; i < page.getData().size(); i++) {
                customer = (Customer) page.getData().get(i);
                dataMap = new HashMap<>();
                customerAmount = transactionDao.totalCustomerConsumptionAmount(customer.getCustId(), now, -1);
                totalAmount += customerAmount;

                dataMap.put("totalAmount", customerAmount);
                dataMap.put("customerId", customer.getCustId());
                dataMap.put("enterpriseName", customer.getEnterpriseName());

                dataMap.put("payType", 1);
                dataMap.put("billDate", currentDate);
                dataMap.put("billCycle", 1);
                dataList.add(dataMap);

            }
            page.setData(dataList);
        }

        map.put("totalAmount", totalAmount);
        map.put("total", page.getTotal());
        map.put("customerBillList", page.getData());
        return map;
    }

    public Map<String, Object> queryOnlineSupplierBill(SupplierBillQueryParam param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String supplierName = StringEscapeUtils.escapeSql(param.getSupplierName());
        String supplierId = StringEscapeUtils.escapeSql(param.getSupplierId());
        String type = StringEscapeUtils.escapeSql(param.getType());
        String billDate = StringEscapeUtils.escapeSql(param.getBillDate());
        String currentDate = DateUtil.fmtDateToStr(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM);
        int pageNum = param.getPageNum();
        int pageSize = param.getPageSize();
        StringBuilder fsql = new StringBuilder("select t5.supplierName as supplierName,t5.supplierId as supplierId,t5.type as type,t5.billCycle as billCycle,t5.billDate as billDate,t5.totalAmount/100 as totalAmount from ( ");
        //查询数据源账单
        StringBuilder dsql = new StringBuilder("(SELECT " +
                "  t2.`NAME` as supplierName, " +
                "t2.supplier_id as supplierId, " +
                "t2.TYPE AS type, " +
                "t2.bill_cycle as billCycle, " +
                "DATE_FORMAT(now(),'%Y-%m') as billDate, " +
                "(" +
                "sum(t1.quantity) * t1.cost_price" +
                ") AS totalAmount " +
                "FROM " +
                "t_supplier_settlement t1 " +
                "LEFT JOIN t_supplier t2 ON t1.source_id = t2.supplier_id where 1=1 ");

        //查询营销资源账单
        StringBuilder rsql = new StringBuilder("(SELECT " +
                "t4.`NAME` AS supplierName, " +
                "t4.supplier_id AS supplierId, " +
                "t4.TYPE AS supplierType, " +
                "t4.bill_cycle AS billCycle, " +
                "DATE_FORMAT(now(), '%Y-%m') AS billDate, " +
                "(" +
                "SUM(t1.quantity) * t3.cost_price " +
                ") AS totalAmount " +
                "FROM " +
                "t_order t1 " +
                "LEFT JOIN t_resource_order t2 ON t1.order_id = t2.order_id " +
                "LEFT JOIN t_market_resource t3 ON t2.resource_id = t3.resource_id " +
                "LEFT JOIN t_supplier t4 ON t4.supplier_id = t3.supplier_id " +
                "WHERE " +
                "t1.order_type = 2 " +
                "AND t1.order_state = 2 ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(supplierName)) {
            p.add(supplierName);
            dsql.append(" and t2.name=?");
            rsql.append(" and t4.name=?");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            p.add(supplierId);
            dsql.append(" and t2.supplier_id=?");
            rsql.append(" and t4.supplier_id=?");
        }
        if (StringUtil.isNotEmpty(type)) {
            p.add(type);
            dsql.append(" and t2.type=?");
            rsql.append(" and t4.type=?");
        }
//        if (StringUtil.isNotEmpty(billDate)) {
//            if (currentDate.equals(billDate)) {
//                dsql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getBillDate())).append("-01 00:00:00").append("'");
//                dsql.append(" and '").append(DateUtil.fmtDateToStr(new Date(), DateUtil.YYYY_MM_DD_HH_mm_ss)).append("'");
//                rsql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getBillDate())).append("-01 00:00:00").append("'");
//                rsql.append(" and '").append(DateUtil.fmtDateToStr(new Date(), DateUtil.YYYY_MM_DD_HH_mm_ss)).append("'");
//            } else {
//                dsql.append(" and date_format(t1.create_time,'%Y-%m')='").append(StringEscapeUtils.escapeSql(param.getBillDate())).append("'");
//                rsql.append(" and date_format(t1.create_time,'%Y-%m')='").append(StringEscapeUtils.escapeSql(param.getBillDate())).append("'");
//            }
//        }
        dsql.append("GROUP BY " +
                "source_id, " +
                "label_id) ");
        rsql.append("GROUP BY " +
                "t2.resource_id) ");
        fsql.append(dsql).append(" UNION ALL ").append(rsql).append(") t5 ");
        Page page = orderDao.sqlPageQuery(fsql.toString(), param.getPageNum(), param.getPageSize(), p.toArray());

        //List list = orderDao.getSQLQuery(fsql.toString()).list();
        map.put("total", page.getTotal());
        map.put("supplierBillList", page.getData());
        return map;
    }

    public Map<String, Object> listBillDetail(BillDetailQueryParam param) throws Exception {
        List<Object> p = new ArrayList<>();
        Map<String, Object> retMap = new HashMap<>();
        String qryType = param.getType();
        String condtion = StringEscapeUtils.escapeSql(param.getCondition());
        String orderId = StringEscapeUtils.escapeSql(param.getOrderId());
        String orderType = StringEscapeUtils.escapeSql(param.getOrderType());
        String productName = StringEscapeUtils.escapeSql(param.getProductName());
        String productId = StringEscapeUtils.escapeSql(param.getProductId());
        String startTime = StringEscapeUtils.escapeSql(param.getStartTime());
        String endTime = StringEscapeUtils.escapeSql(param.getEndTime());
        String status = StringEscapeUtils.escapeSql(param.getStatus());
        int pageNum = param.getPageNum();
        int pageSize = param.getPageSize();

        StringBuilder sql = new StringBuilder("SELECT\n" +
                "\tt1.order_id AS orderId,\n" +
                "\tt1.amount AS amount,\n" +
                "\tt1.cost_price as costAmount,\n" +
                "\tt1.order_type,\n" +
                "\tt1.product_name AS productName,\n" +
                "\tt1.quantity AS quantity,\n" +
                "\tt1.create_time AS createTime,\n" +
                "\tt1.order_state as status\n" +
                "FROM\n" +
                "\tt_order t1 where 1=1");
        if (StringUtil.isNotEmpty(orderId)) {
            sql.append(" and t1.order_id=? ");
            p.add(orderId);
        }
        if (StringUtil.isNotEmpty(orderType)) {
            p.add(orderType);
            sql.append(" and t1.order_type=? ");
        }
        if (StringUtil.isNotEmpty(productName)) {
            p.add(productName);
            sql.append(" and t1.product_name=? ");
        }
        if (StringUtil.isNotEmpty(status)) {
            p.add(status);
            sql.append(" and t1.order_state=? ");
        }

        if (StringUtil.isNotEmpty(productId)) {
            //todo 库中无产品id字段
        }
        if (StringUtil.isNotEmpty(qryType)) {
            //客户类详单
            if ("1".equals(qryType)) {
                p.add(condtion);
                sql.append(" and t1.cust_id=? ");
            }
            //数据源类账单
            if ("2".equals(qryType)) {
                p.add(condtion);
                sql.append(" and t1.supplier_id=? ");
                sql.append(" and t1.order_type=1 ");
            }
            //营销资源类账单
            if ("3".equals(qryType)) {
                p.add(condtion);
                sql.append(" and t1.supplier_id=? ");
                sql.append(" and t1.order_type=2 ");
            }
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sql.append(" and t1.create_time between ? and ? ");
        }
        Page list = orderDao.sqlPageQuery(sql.toString(), pageNum, pageSize, p.toArray());
        retMap.put("total", list.getTotal());
        retMap.put("billDetailList", list.getData());
        return retMap;
    }
}

