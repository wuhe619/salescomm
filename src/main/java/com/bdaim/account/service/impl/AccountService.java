package com.bdaim.account.service.impl;

import com.alibaba.fastjson.JSON;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.resource.dao.SourceDao;
import com.github.crab2died.ExcelUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service("accountService")
@Transactional
public class AccountService {
    private static Logger logger = LoggerFactory.getLogger(AccountService.class);
    @Resource
    CustomerDao customerDao;
    @Resource
    TransactionService transactionService;
    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    SourceDao sourceDao;


    private final static DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 前台账户余额
     *
     * @param customerId 企业ID
     * @return
     */
    public Map<String, Object> showAccoutCenter(String customerId) throws Exception {
        /*if (StringUtil.isEmpty(customerId)) {
            throw new TouchException("20010", "系统异常:用户信息不存在");
        }*/
        Map<String, Object> resultMap = new HashMap<>();
        CustomerProperty remainAmoutProperty = customerDao.getProperty(customerId, "remain_amount");
        CustomerProperty usedAmountProperty = customerDao.getProperty(customerId, "used_amount");
        DecimalFormat df = new DecimalFormat("######0.00");
        if (remainAmoutProperty != null) {
            Double remainAmout = Double.parseDouble(remainAmoutProperty.getPropertyValue());
            resultMap.put("remainAmout", df.format(remainAmout / 100));
        }
        if (usedAmountProperty != null) {
            Double usedAmount = Double.parseDouble(usedAmountProperty.getPropertyValue());
            resultMap.put("usedAmount", df.format(usedAmount / 100));
        }
        return resultMap;
    }

    /**
     * 账户充值,扣减
     *
     * @param param
     * @return
     */
    public Boolean changeBalance(CustomerBillQueryParam param) {
        String custId = param.getCustomerId();
        double amount = param.getAmount();
        String path = param.getPath();
        String remark = param.getRemark();
        int action = param.getAction();
        String dealType = param.getDealType();
        String supplierId = param.getSupplierId();
        boolean deductionsStatus = false;
        BigDecimal moneySale;
        int type = 0;
        try {
            moneySale = new BigDecimal(amount * 100);
            moneySale = moneySale.multiply(new BigDecimal(1));
            //充值
            if (0 == action) {
                if (StringUtil.isNotEmpty(dealType) && dealType.equals("0")) {
                    type = TransactionEnum.SUPPLIER_RECHARGE.getType();
                    //供应商资金充值
                    deductionsStatus = sourceDao.accountSupplierRecharge(supplierId, moneySale);
                } else {
                    //企业资金充值
                    type = TransactionEnum.BALANCE_RECHARGE.getType();
                    deductionsStatus = customerDao.accountRecharge(custId, moneySale);
                }
            } else if (1 == action) {
                //扣减
                if (StringUtil.isNotEmpty(dealType) && dealType.equals("0")) {
                    type = TransactionEnum.SUPPLIER_DEDUCTION.getType();
                    //供应商资金扣减
                    deductionsStatus = sourceDao.supplierAccountDuctions(supplierId, moneySale);
                } else {
                    type = TransactionEnum.BALANCE_DEDUCTION.getType();
                    //企业资金扣减
                    deductionsStatus = customerDao.accountDeductions(custId, moneySale);
                }
            }
        } catch (Exception e) {
            logger.error(custId + " 账户充值扣款失败,", e);
            throw new RuntimeException(custId + " 账户充值扣款失败");
        }
        // 扣款成功
        if (deductionsStatus) {
            // 保存交易记录
            try {
                //支付方式 1.余额 2.第三方 3.线下 4.包月分钟
                int payMode = 1;
                String transactionId = "";
                if (param.getPayMode() != null) {
                    payMode = param.getPayMode();
                }
                long userId = param.getUserId();
                if (StringUtil.isNotEmpty(param.getSupplierId())) {
                    supplierId = param.getSupplierId();
                }
                if (StringUtil.isNotEmpty(param.getTransactionId())) {
                    transactionId = param.getTransactionId();
                }
                // 保存交易记录根据supplierId查询resourceId
                transactionService.saveTransactionLog(custId, type, moneySale.intValue(), payMode, supplierId, remark, userId, path, transactionId, 0, null);
            } catch (Exception e) {
                logger.error(custId + " 保存交易记录失败,", e);
            }
        }
        return deductionsStatus;
    }

    /**
     * 企业资金
     *
     * @return
     */
    public Page pageList(PageParam page, CustomerBillQueryParam queryParam) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT cus.cust_id,cus.create_time createTime,cus.enterprise_name,cus.status,\n" +
                "t2.account,t2.realname,cjc.mobile_num,CONVERT(cjc.remainAmount/100,DECIMAL(15,2)) as remainAmount\n" +
                " from t_customer cus\n" +
                "LEFT JOIN t_customer_user t2   ON cus.cust_id = t2.cust_id\n" +
                "LEFT JOIN (\n" +
                "  SELECT cust_id, \n" +
                "   max(CASE property_name WHEN 'account'   THEN property_value ELSE '' END ) account,\n" +
                "   max(CASE property_name WHEN 'person'   THEN property_value ELSE '' END ) person,\n" +
                "   max(CASE property_name WHEN 'mobile_num'   THEN property_value ELSE '' END ) mobile_num,\n" +
                "\t max(CASE property_name WHEN 'remain_amount'   THEN property_value ELSE '' END ) remainAmount\n" +
                "   FROM t_customer_property p GROUP BY cust_id \n" +
                ") cjc ON cus.cust_id = cjc.cust_id \n" +
                "where 1=1 and t2.user_type=1 ");
        if (StringUtil.isNotEmpty(queryParam.getCustomerId())) {
            sqlBuilder.append(" and cus.cust_id= " + queryParam.getCustomerId());
        }
        if (StringUtil.isNotEmpty(queryParam.getEnterpriseName())) {
            sqlBuilder.append(" and cus.enterprise_name like '%" + queryParam.getEnterpriseName() + "%'");
        }
        if (StringUtil.isNotEmpty(queryParam.getAccount())) {
            sqlBuilder.append(" and cjc.account LIKE '%" + queryParam.getAccount() + "%'");
        }
        if (StringUtil.isNotEmpty(queryParam.getRealname())) {
            sqlBuilder.append(" and cjc.person LIKE '%" + queryParam.getRealname() + "%'");
        }
        if (StringUtil.isNotEmpty(queryParam.getPhone())) {
            sqlBuilder.append(" and cjc.phone LIKE '%" + queryParam.getPhone() + "%'");
        }
        sqlBuilder.append(" ORDER BY cus.create_time desc ");
        return new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
    }

    /**
     * 企业资金--充值记录
     *
     * @return
     */
    public Page pageListRecords(PageParam page, CustomerBillQueryParam queryParam) {
        // 如果没有传开始时间
        StringBuilder sqlBuilder = new StringBuilder("SELECT t.type,t.create_time,t.transaction_id,t.amount/100 as amount ,cu.realname," +
                "t.cust_id,t.certificate,t.remark from t_transaction_bill t \n" +
                "LEFT JOIN t_customer_user cu on t.cust_id=cu.cust_id\n" +
                " where 1=1 and cu.user_type=1");
        if (StringUtil.isNotEmpty(queryParam.getCustomerId())) {
            sqlBuilder.append(" and t.cust_id= " + queryParam.getCustomerId());
        }
        if (StringUtil.isNotEmpty(queryParam.getTransactionId())) {
            sqlBuilder.append(" and t.transaction_id= " + queryParam.getTransactionId());
        }
        if (StringUtil.isNotEmpty(queryParam.getType())) {
            sqlBuilder.append(" and t.type=" + queryParam.getType());
        }
        if (StringUtil.isNotEmpty(queryParam.getRealname())) {
            sqlBuilder.append(" and cu.realname like '%" + queryParam.getRealname() + "%'");
        }
        if (StringUtil.isNotEmpty(queryParam.getStartTime())) {
            sqlBuilder.append(" AND t.create_time >= '" + queryParam.getStartTime() + "'");
        }
        if (StringUtil.isNotEmpty(queryParam.getEndTime())) {
            sqlBuilder.append(" AND t.create_time <='" + queryParam.getEndTime() + "'");
        }
        sqlBuilder.append(" ORDER BY t.create_time desc ");
        logger.info("企业充值扣减记录sql:" + sqlBuilder.toString());
        return new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
    }

    /**
     * 后台供应商资金
     *
     * @return
     */
    public Page querySupplierAcctsByCondition(PageParam page, CustomerBillQueryParam queryParam) {
        // 如果没有传开始时间
        StringBuilder sqlBuilder = new StringBuilder("SELECT p.`name` source_name, t.create_time,t.transaction_id,p.supplier_id,t.amount/100 as amount,u.REALNAME realname , t.certificate ,t.remark ,case t.type when 8 then '充值' when 13 then '扣減'  end  type ");
        sqlBuilder.append("FROM t_transaction_bill t");
        sqlBuilder.append(" LEFT JOIN t_supplier p ON t.supplier_id = p.supplier_id\n");
        sqlBuilder.append("LEFT JOIN t_user u ON t.user_id = u.ID WHERE 1=1\n");
        if (StringUtil.isNotEmpty(queryParam.getTransactionId())) {
            sqlBuilder.append(" and t.transaction_id= " + queryParam.getTransactionId());
        }
        if (StringUtil.isNotEmpty(queryParam.getType())) {
            sqlBuilder.append(" and t.type= " + queryParam.getType());
        }
        if (StringUtil.isNotEmpty(queryParam.getSupplierId())) {
            sqlBuilder.append(" and t.supplier_id= " + queryParam.getSupplierId());
        }
        if (StringUtil.isNotEmpty(queryParam.getStartTime())) {
            sqlBuilder.append(" AND t.create_time >= '" + queryParam.getStartTime() + "'");
        }
        if (StringUtil.isNotEmpty(queryParam.getEndTime())) {
            sqlBuilder.append(" AND t.create_time <='" + queryParam.getEndTime() + "'");
        }
        if (StringUtil.isNotEmpty(queryParam.getBillDate())) {
            sqlBuilder.append(" and DATE_FORMAT(t.create_time, '%Y%m') like'" + queryParam.getBillDate() + "'");
        }
        sqlBuilder.append(" ORDER BY t.create_time desc ");
        return new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
    }
    public List<Map<String,Object>> querySupplierAcctsExport(CustomerBillQueryParam queryParam) {
        // 如果没有传开始时间
        StringBuilder sqlBuilder = new StringBuilder("SELECT p.`name` source_name, t.create_time,t.transaction_id,p.supplier_id," +
                "t.amount/100 as amount,u.REALNAME realname , t.certificate,t.remark,CASE t.type WHEN '1' THEN '充值' WHEN '7' THEN '扣减' END AS billType ");
        sqlBuilder.append("FROM t_transaction_bill t");
        sqlBuilder.append(" LEFT JOIN t_supplier p ON t.supplier_id = p.supplier_id\n");
        sqlBuilder.append("LEFT JOIN t_user u ON t.user_id = u.ID WHERE 1=1\n");
        if (StringUtil.isNotEmpty(queryParam.getTransactionId())) {
            sqlBuilder.append(" and t.transaction_id= " + queryParam.getTransactionId());
        }
        if (StringUtil.isNotEmpty(queryParam.getType())) {
            sqlBuilder.append(" and t.type= " + queryParam.getType());
        }
        if (StringUtil.isNotEmpty(queryParam.getSupplierId())) {
            sqlBuilder.append(" and t.supplier_id= " + queryParam.getSupplierId());
        }
        if (StringUtil.isNotEmpty(queryParam.getStartTime())) {
            sqlBuilder.append(" AND t.create_time >= '" + queryParam.getStartTime() + "'");
        }
        if (StringUtil.isNotEmpty(queryParam.getEndTime())) {
            sqlBuilder.append(" AND t.create_time <='" + queryParam.getEndTime() + "'");
        }
        if (StringUtil.isNotEmpty(queryParam.getBillDate())) {
            sqlBuilder.append(" and DATE_FORMAT(t.create_time, '%Y%m') like'" + queryParam.getBillDate() + "'");
        }
        sqlBuilder.append(" ORDER BY t.create_time desc ");
        List<Map<String,Object>> resultList = jdbcTemplate.queryForList(sqlBuilder.toString());
        return resultList;
    }

    public Object exportCustomerAccountRecharge(CustomerBillQueryParam param, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String startTime = param.getStartTime();
            //当前月
            String nowYearMonth;
            // 如果没有传开始时间
            if (StringUtil.isEmpty(startTime)) {
                nowYearMonth = LocalDate.now().format(YYYYMM);
            } else {
                LocalDateTime localStartDateTime = LocalDateTime.parse(startTime, YYYYMMDDHHMMSS);
                nowYearMonth = localStartDateTime.format(YYYYMM);
            }
            StringBuilder sqlBuilder = new StringBuilder("SELECT t.type,t.create_time,t.transaction_id,t.amount,cu.realname,t.remark,t.cust_id \n" +
                    "from t_transaction_" + nowYearMonth + "  t \n" +
                    "LEFT JOIN t_customer_user cu on t.cust_id=cu.cust_id\n" +
                    "WHERE 1=1 and cu.user_type=1 ");
            if (StringUtil.isNotEmpty(param.getCustomerId())) {
                sqlBuilder.append(" and t.cust_id= " + param.getCustomerId());
            }
            if (StringUtil.isNotEmpty(param.getTransactionId())) {
                sqlBuilder.append(" and t.transaction_id= " + param.getTransactionId());
            }
            if (StringUtil.isNotEmpty(param.getRealname())) {
                sqlBuilder.append(" and cu.realname like '% " + param.getRealname() + "%'");
            }
            if (StringUtil.isNotEmpty(param.getType())) {
                sqlBuilder.append(" and t.type= " + param.getType());
            }
            sqlBuilder.append(" ORDER BY t.create_time desc ");

            List<Map<String, Object>> customerAccountRechargeList = jdbcTemplate.queryForList(sqlBuilder.toString());
            List<List<Object>> data = new ArrayList<>();
            // 设置标题
            List<String> titles = new ArrayList<String>();
            titles.add("交易事项");//1
            titles.add("交易日期");
            titles.add("流水号");
            titles.add("交易总额(元)");//6
            titles.add("操作人");
            titles.add("备注");

            String fileName = "企业资金充值记录" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
            String fileType = ".xlsx";

            List<Object> rowList;
            for (Map<String, Object> column : customerAccountRechargeList) {
                rowList = new ArrayList<>();
                if (column.get("type") != null) {
                    rowList.add(TransactionEnum.getName(Integer.parseInt(String.valueOf(column.get("type")))));
                }
                rowList.add(column.get("create_time") != null ? column.get("create_time") : "");
                rowList.add(column.get("transaction_id") != null ? column.get("transaction_id") : "");
                rowList.add(column.get("amount") != null ? column.get("amount") : "");
                rowList.add(column.get("realname") != null ? column.get("realname") : "");
                rowList.add(column.get("remark") != null ? column.get("remark") : "");
                data.add(rowList);
            }
            if (data.size() > 0) {
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                OutputStream outputStream = null;
                outputStream = response.getOutputStream();
                ExcelUtils.getInstance().exportObjects2Excel(data, titles, outputStream);
                outputStream.flush();
                response.flushBuffer();
                outputStream.close();
                logger.info("企业资金充值记录导出成功");
                resultMap.put("code", "000");
                resultMap.put("_message", "企业资金充值记录导出成功！");
            } else {
                resultMap.put("code", "001");
                resultMap.put("_message", "企业资金充值记录无数据导出！");
                return JSON.toJSONString(resultMap);
            }
        } catch (Exception e) {
            logger.error("企业资金收支明細记录导出失败\t" + e.getMessage());
            resultMap.put("code", "002");
            resultMap.put("_message", "企业资金收支明細记录导出失败！");
        }
        return JSON.toJSONString(resultMap);
    }

    public Object exportSupplierRecords(CustomerBillQueryParam queryParam, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            String startTime = queryParam.getStartTime();
            //当前月
            String nowYearMonth;
            // 如果没有传开始时间
            if (StringUtil.isEmpty(startTime)) {
                nowYearMonth = LocalDate.now().format(YYYYMM);
            } else {
                LocalDateTime localStartDateTime = LocalDateTime.parse(startTime, YYYYMMDDHHMMSS);
                nowYearMonth = localStartDateTime.format(YYYYMM);
            }
            StringBuilder sqlBuilder = new StringBuilder("SELECT t.create_time,t.transaction_id,t.amount/100 as amount,\n" +
                    " cu.person as realname,\n" +
                    "s.source_name,t.supplier_id \n" +
                    "from t_transaction_" + nowYearMonth + " t\n" +
                    "LEFT JOIN t_source s on t.supplier_id=s.source_id\n" +
                    "LEFT JOIN (\n" +
                    "SELECT\n" +
                    "source_id,\n" +
                    "max(CASE property_key WHEN 'person'      THEN property_value  ELSE '' END ) person\n" +
                    "FROM t_source_property p GROUP BY source_id\n" +
                    ") cu ON t.supplier_id = cu.source_id\n" +
                    "WHERE 1=1  and t.type = 8");
            if (StringUtil.isNotEmpty(queryParam.getTransactionId())) {
                sqlBuilder.append(" and t.transaction_id= " + queryParam.getTransactionId());
            }
            if (StringUtil.isNotEmpty(queryParam.getSupplierId())) {
                sqlBuilder.append(" and t.supplier_id= " + queryParam.getSupplierId());
            }

            sqlBuilder.append(" ORDER BY t.create_time desc ");

            List<Map<String, Object>> supplierAccoutRecordlist = jdbcTemplate.queryForList(sqlBuilder.toString());
            List<List<Object>> data = new ArrayList<>();
            // 设置标题
            List<String> titles = new ArrayList<String>();
            titles.add("供应商名称");
            titles.add("交易日期");
            titles.add("流水单号");
            titles.add("交易金额（元）");
            titles.add("操作人");

            String fileName = "供应商资金信息" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd hh:mm:ss"));
            String fileType = ".xlsx";
            List<Object> rowList;
            for (Map<String, Object> column : supplierAccoutRecordlist) {
                rowList = new ArrayList<>();
                rowList.add(column.get("source_name") != null ? column.get("source_name") : "");
                rowList.add(column.get("create_time") != null ? column.get("create_time") : "");
                rowList.add(column.get("transaction_id") != null ? column.get("transaction_id") : "");
                rowList.add(column.get("amount") != null ? column.get("amount") : "");
                rowList.add(column.get("realname") != null ? column.get("realname") : "");
                data.add(rowList);
            }
            if (data.size() > 0) {
                response.setCharacterEncoding("utf-8");
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                OutputStream outputStream;
                outputStream = response.getOutputStream();
                ExcelUtils.getInstance().exportObjects2Excel(data, titles, outputStream);
                outputStream.flush();
                response.flushBuffer();
                outputStream.close();
                logger.info("供应商资金导出成功");
                resultMap.put("code", "000");
                resultMap.put("_message", "供应商资金导出成功！");
            } else {
                resultMap.put("code", "001");
                resultMap.put("_message", "供应商资金无数据导出！");
                return JSON.toJSONString(resultMap);
            }
        } catch (Exception e) {
            logger.error("供应商资金导出失败\t" + e.getMessage());
            resultMap.put("code", "002");
            resultMap.put("_message", "供应商资金导出失败！");
        }
        return JSON.toJSONString(resultMap);
    }

    /**
     * @description 查询账户余额（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/26 16:22
     */
    public Object queryAccoutCenter(String custId) {
        Map<String, Object> resultMap = new HashMap<>();
        CustomerProperty remainAmoutProperty = customerDao.getProperty(custId, "remain_amount");
        //CustomerProperty usedAmountProperty = customerDao.getProperty(custId, "used_amount");
        DecimalFormat df = new DecimalFormat("######0.00");
        if (remainAmoutProperty != null) {
            Double remainAmout = Double.parseDouble(remainAmoutProperty.getPropertyValue());
            resultMap.put("remainAmout", df.format(remainAmout / 100));
        }
      /*  if (usedAmountProperty != null) {
            Double usedAmount = Double.parseDouble(usedAmountProperty.getPropertyValue());
            resultMap.put("usedAmount", df.format(usedAmount / 100));
        }*/
        return resultMap;
    }


}
