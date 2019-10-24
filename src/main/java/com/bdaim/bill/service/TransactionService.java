package com.bdaim.bill.service;

import com.alibaba.fastjson.JSON;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
import com.bdaim.customer.account.dao.TransactionDao;
import com.bdaim.customer.account.dto.TransactionQryParam;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.util.IDHelper;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.github.crab2died.ExcelUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr.YinXin on 2017/2/24.
 */
@Service("transactionService")
@Transactional
public class TransactionService {
    Logger logger = LoggerFactory.getLogger(TransactionService.class);
    private final static DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private TransactionDao transactionDao;
    @Resource
    CustomerDao customerDao;
    @Resource
    CustomerUserDao customerUserDao;
    @Resource
    UserDao userDao;
    @Resource
    SupplierDao supplierDao;

    public List<Map<String, Object>> listTransactionsByCondtion(PageParam page, String customerId, TransactionQryParam param) throws Exception {
        HashMap<String, Object> ret = new HashMap<>();
        List<Object> params = new ArrayList<>();
        int type = param.getType();
        int tradeItem = param.getTradeItem();
        String startTime = param.getStartTime();


        if (tradeItem == 0 && type == 0) {
            type = 0;//全部类型
        } else if (tradeItem == 0 && type == 1) {
            type = 1;//充值
        } else if (tradeItem == 0 && type == 7) {
            type = 7;//全部扣费类型   !1
        } else {
            type = tradeItem;// 1.充值 2.用户群扣费 3.短信扣费  4.通话扣费   5.座席扣费  6修复扣费
        }
        //当前月
        String nowYearMonth;
        // 如果没有传开始时间
        if (StringUtil.isEmpty(startTime)) {
            nowYearMonth = LocalDate.now().format(YYYYMM);
        } else {
            LocalDateTime localStartDateTime = LocalDateTime.parse(startTime, YYYYMMDDHHMMSS);
            nowYearMonth = localStartDateTime.format(YYYYMM);
        }

        StringBuilder sql = new StringBuilder("SELECT\n" +
                "  c.TYPE,\n" +
                "  FORMAT(c.prod_amount/100,2) AS amount,\n" +
                " s.source_name AS supplierName \n" +
                " FROM\n" +
                "  stat_bill_month c \n" +
                "  LEFT JOIN t_source s ON c.resource_id = s.source_id  where 1=1 ");
        sqlAppendFuc(sql, customerId, type, startTime, params);
        logger.debug(sql.toString());
        System.out.println("前台企业用户账户余额页面 交易明细sql：" + sql.toString());
        List<Map<String, Object>> result = new ArrayList<>();
        PageList pageret = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        ret.put("total", pageret.getTotal());
        ret.put("transactions", pageret);

        Float sumamount = null;
        Float sumamountIn = null;
        Float sumamountOut = null;


        if (type > 0 && type != 7) {
            StringBuilder sqlSumAmount = new StringBuilder("SELECT SUM(t.amount) as sumAmount FROM stat_bill_month c where 1=1 ");
            sqlAppendFuc(sqlSumAmount, customerId, type, startTime, params);
            logger.debug(sqlSumAmount.toString());
            int sumAmount = (Integer)jdbcTemplate.queryForMap(sqlSumAmount.toString()).get("sumAmount");
            //分转化为元
            if (type == 1) {//充值
                sumamountIn = Float.valueOf(String.valueOf(sumAmount)) / 100;
                ret.put("sumamountIn", sumamountIn);
                ret.put("sumamountOut", 0.00);
            } else {//消费
                sumamount = Float.valueOf(String.valueOf(sumAmount)) / 100;
                ret.put("sumamountOut", sumamount);
                ret.put("sumamountIn", 0.00);
            }
        } else if (type == 0) {//全部
            StringBuilder sqlSumAmount = new StringBuilder("SELECT SUM(c.prod_amount) as sumAmount FROM stat_bill_month c where 1=1 ");
            sqlAppendFuc(sqlSumAmount, customerId, 1, startTime, params);
            logger.debug(sqlSumAmount.toString());
            int sumIn = (Integer)jdbcTemplate.queryForMap(sqlSumAmount.toString()).get("sumAmount");
            //分转化为元
            sumamountIn = Float.valueOf(String.valueOf(sumIn)) / 100;
            ret.put("sumamountIn", sumamountIn);

            StringBuilder sqlSumAmount1 = new StringBuilder("SELECT SUM(c.prod_amount) as sumAmount FROM stat_bill_month c where 1=1 ");
            sqlAppendFucCopy(sqlSumAmount1, customerId, 1, startTime, params);
            logger.debug(sqlSumAmount1.toString());
            int sumOut = (Integer)jdbcTemplate.queryForMap(sqlSumAmount1.toString()).get("sumAmount");
            //分转化为元
            sumamountOut = Float.valueOf(String.valueOf(sumOut)) / 100;
            ret.put("sumamountOut", sumamountOut);
        } else if (type == 7) {//总支出
            StringBuilder sqlSumAmount2 = new StringBuilder("SELECT SUM(c.prod_amount) as sumAmount FROM stat_bill_month c where 1=1 ");
            sqlAppendFucCopy(sqlSumAmount2, customerId, 1, startTime, params);
            logger.debug(sqlSumAmount2.toString());
            int sumOut = (Integer)jdbcTemplate.queryForMap(sqlSumAmount2.toString()).get("sumAmount");
            //分转化为元
            sumamountOut = Float.valueOf(String.valueOf(sumOut)) / 100;
            ret.put("sumamountOut", sumamountOut);
            ret.put("sumamountIn", 0.00);
        }

        result.add(ret);
        return result;
    }


    public String sqlAppendFuc(StringBuilder sq, String custId, int typ, String sTime, List<Object> params) {
        sq.append(" and c.cust_id = '").append(StringEscapeUtils.escapeSql(custId)).append("'");
        params.add(custId);
        if (typ > 0 && typ == 7) {
            sq.append(" and c.type = 7");
            params.add(typ);
        }
        if (typ > 0 && typ != 7) {
            sq.append(" and c.type =").append(typ);
            params.add(typ);
        }
        if (StringUtil.isNotEmpty(sTime)) {
            sq.append(" and c.stat_time between ").append("'").append(StringEscapeUtils.escapeSql(sTime)).append("'");
            params.add(sTime);
        }
        sq.append(" order by c.stat_time desc ");
        return sq.toString();
    }

    public String sqlAppendFucCopy(StringBuilder sq, String custId, int typ, String sTime, List<Object> params) {
        sq.append(" and c.cust_id = '").append(StringEscapeUtils.escapeSql(custId)).append("'");
        params.add(custId);
        if (typ > 0) {
            sq.append(" and c.type!=").append(typ);
            sq.append(" and c.type!=").append(2);
            params.add(typ);
        }
        if (StringUtil.isNotEmpty(sTime)) {
            sq.append(" and c.stat_time between ").append("'").append(StringEscapeUtils.escapeSql(sTime)).append("'");
            params.add(sTime);

        }
        sq.append(" order by c.stat_time desc ");
        return sq.toString();
    }

    public Object exportCustomerBillDetails(String custId, TransactionQryParam param, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<>();
        List<Object> params = new ArrayList<>();
        try {
            int type = param.getType();
            int tradeItem = param.getTradeItem();
            String startTime = param.getStartTime();


            if (tradeItem == 0 && type == 0) {
                type = 0;//全部类型
            } else if (tradeItem == 0 && type == 1) {
                type = 1;//充值
            } else if (tradeItem == 0 && type == 7) {
                type = 7;//全部扣费类型   !1
            } else {
                type = tradeItem;// 1.充值 2.用户群扣费 3.短信扣费  4.通话扣费   5.座席扣费  6修复扣费
            }
            //当前月
            String nowYearMonth;
            // 如果没有传开始时间
            if (StringUtil.isEmpty(startTime)) {
                nowYearMonth = LocalDate.now().format(YYYYMM);
            } else {
                LocalDateTime localStartDateTime = LocalDateTime.parse(startTime, YYYYMMDDHHMMSS);
                nowYearMonth = localStartDateTime.format(YYYYMM);
            }

            StringBuilder sql = new StringBuilder("SELECT\n" +
                    "  t.stat_time AS transactionDate,\n" +
                    "  t.type type,\n" +
                    "  FORMAT(t.prod_amount/100,2) as amount,\n" +
                    "  t.remark,\n" +
                    "  u.realname as operateName, \n" +
                    " s.source_name as supplierName \n" +
                    " FROM\n" +
                    "  stat_bill_month t LEFT JOIN t_customer_user u ON t.user_id=u.ID " +
                    "left join t_source s on t.supplier_id = s.source_id where 1=1 ");
            sqlAppendFuc(sql, custId, type, startTime, params);
            logger.debug(sql.toString());
            List<Map<String, Object>> billDetailList = transactionDao.sqlQuery(sql.toString());
            List<List<Object>> data = new ArrayList<>();
            //设置标题
            List<String> titles = new ArrayList<String>();
            titles.add("交易事项");
            titles.add("交易日期");
            titles.add("流水号");
            titles.add("渠道");
            titles.add("交易总额(元)");
            titles.add("操作人");
            String fileName = "企业资金交易记录" + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM"));
            String fileType = ".xlsx";

            List<Object> rowList;
            for (Map<String, Object> column : billDetailList) {
                rowList = new ArrayList<>();
                if (column.get("type") != null) {
                    rowList.add(TransactionEnum.getName(Integer.parseInt(String.valueOf(column.get("type")))));
                }
                rowList.add(column.get("transactionDate") != null ? column.get("transactionDate") : "");
                rowList.add(column.get("transactionId") != null ? column.get("transactionId") : "");
                rowList.add("联通");
                rowList.add(column.get("amount") != null ? column.get("amount") : "");
                rowList.add(column.get("operateName") != null ? column.get("operateName") : "");
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
            logger.error("企业资金充值记录导出失败\t" + e.getMessage());
            resultMap.put("code", "002");
            resultMap.put("_message", "企业资金充值记录导出失败！");
        }
        return JSON.toJSONString(resultMap);
    }

    /**
     * 账户交易记录保存
     *
     * @return
     * @throws Exception
     */
    public boolean saveTransactionLog(String custId, int type, int amount, int payMode, String supplierId, String remark, long userId, String certificate, String transactionId, int prodAmount, String resourceId) {
        // 创建交易记录年月分表
        StringBuilder sql = new StringBuilder();
        sql.append("insert into t_transaction_bill");
        sql.append(" (transaction_id, cust_id, type, pay_mode, amount, remark, create_time, supplier_id, user_id, certificate,prod_amount,resource_id)values(?,?,?,?,?,?,?,?,?,?,?,?)");
        if (StringUtil.isEmpty(transactionId)) {
            transactionId = Long.toString(IDHelper.getTransactionId());
        }
        transactionDao.executeUpdateSQL(sql.toString(), new Object[]{transactionId, custId, type,
                payMode, amount, remark, new Timestamp(System.currentTimeMillis()), supplierId, userId, certificate, prodAmount, resourceId});

        return true;
    }


    public List<Map<String, Object>> listTransactionsByCondtion(String customerId, TransactionQryParam param) throws Exception {
        HashMap<String, Object> ret = new HashMap<>();
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            int type = param.getType();
            String transactionId = param.getTransactionId();
            String startTime = param.getStartTime();
            String endTime = param.getEndTime();
            int pageNum = param.getPageNum();
            int pageSize = param.getPageSize();
            //当前月
            String nowYearMonth;
            // 如果没有传开始时间
            if (StringUtil.isEmpty(startTime)) {
                nowYearMonth = LocalDate.now().format(YYYYMM);
            } else {
                LocalDateTime localStartDateTime = LocalDateTime.parse(startTime, YYYYMMDDHHMMSS);
                nowYearMonth = localStartDateTime.format(YYYYMM);
            }

            // 检查交易记录月表是否存在,不存在则创建
            transactionDao.checkTransactionLogMonthTableNotExist(nowYearMonth);

            StringBuilder sql = new StringBuilder("SELECT\n" +
                    "  t.create_time AS transactionDate,\n" +
                    "  t.TYPE,\n" +
                    "  t.transaction_id AS transactionId,\n" +
                    "  FORMAT(t.amount/1000,2) as amount,\n" +
                    "  t.remark,\n" +
                    "  t.certificate\n" +
                    " FROM\n" +
                    "  t_transaction_" + nowYearMonth + " t where 1=1 ");
            sql.append(" and t.cust_id = '").append(StringEscapeUtils.escapeSql(customerId)).append("'");
            if (StringUtil.isNotEmpty(transactionId)) {
                sql.append(" and t.transaction_id='").append(StringEscapeUtils.escapeSql(transactionId)).append("'");
            }
            if (type > 0) {
                sql.append(" and t.type=").append(type);
            }
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                sql.append(" and t.create_time between ").append("'").append(StringEscapeUtils.escapeSql(startTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
            }
            sql.append(" order by t.create_time desc ");
            logger.debug(sql.toString());

            List<Map<String, Object>> values = null;
            try {
                values = transactionDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
                ret.put("total", transactionDao.getSQLQuery(sql.toString()).list().size());
            } catch (Exception e) {
                logger.error("获取交易记录列表失败", e);
                ret.put("total", 0);
            }
            ret.put("transactions", values);
        } catch (Exception e) {
            logger.error("获取交易日志失败,", e);
            ret.put("transactions", new ArrayList<>());
        }
        result.add(ret);
        return result;
    }

    public List<Map<String, Object>> listTransactionsByCondition_V1(String customerId, TransactionQryParam param) throws Exception {
        List<Map<String, Object>> result = new ArrayList<>();
        try {
            String transactionId = param.getTransactionId();
            String startTime = param.getStartTime();
            String endTime = param.getEndTime();
            int pageNum = param.getPageNum();
            int pageSize = param.getPageSize();
            //当前月
            String nowYearMonth;
            // 如果没有传开始时间
            if (StringUtil.isEmpty(startTime)) {
                nowYearMonth = LocalDate.now().format(YYYYMM);
            } else {
                LocalDateTime localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                nowYearMonth = localStartDateTime.format(YYYYMM);
            }

            // 检查交易记录月表是否存在,不存在则创建
            transactionDao.checkTransactionLogMonthTableNotExist(nowYearMonth);

            StringBuilder sql = new StringBuilder("SELECT" +
                    "  t.create_time AS transactionDate," +
                    "  t.TYPE, t.user_id, t.cust_id, " +
                    "  t.transaction_id AS transactionId," +
                    "  FORMAT(t.amount/1000,2) as amount," +
                    "  t.remark," +
                    "  t.certificate" +
                    " FROM" +
                    "  t_transaction_" + nowYearMonth + " t where 1=1 ");
            if (StringUtil.isNotEmpty(customerId)) {
                sql.append(" and t.cust_id = '").append(StringEscapeUtils.escapeSql(customerId)).append("'");
            }
            if (StringUtil.isNotEmpty(transactionId)) {
                sql.append(" and t.transaction_id='").append(StringEscapeUtils.escapeSql(transactionId)).append("'");
            }
            if (param.getType() != null && param.getType() > 0) {
                sql.append(" and t.type=").append(param.getType());
            }
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                sql.append(" and t.create_time between ").append("'").append(StringEscapeUtils.escapeSql(startTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
            }
            sql.append(" order by t.create_time desc ");
            logger.debug(sql.toString());
            result = transactionDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
            if (result.size() > 0) {
                Customer customer;
                for (Map<String, Object> m : result) {
                    m.put("userName", customerUserDao.getName(String.valueOf(m.get("user_id"))));
                    customer = customerDao.get(String.valueOf(m.get("cust_id")));
                    if (customer != null) {
                        m.put("enterpriseName", customer.getEnterpriseName());
                    } else {
                        m.put("enterpriseName", "");
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取交易日志失败,", e);
        }
        return result;
    }

    public Page listTransactionsByCondition_V2(String customerId, TransactionQryParam param) throws Exception {
        Page page = null;
        try {
            String transactionId = param.getTransactionId();
            String startTime = param.getStartTime();
            String endTime = param.getEndTime();
            int pageNum = param.getPageNum();
            int pageSize = param.getPageSize();
            //当前月
            String nowYearMonth;
            // 如果没有传开始时间
            if (StringUtil.isEmpty(startTime)) {
                nowYearMonth = LocalDate.now().format(YYYYMM);
            } else {
                LocalDateTime localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                nowYearMonth = localStartDateTime.format(YYYYMM);
            }

            // 检查交易记录月表是否存在,不存在则创建
            transactionDao.checkTransactionLogMonthTableNotExist(nowYearMonth);

            StringBuilder sql = new StringBuilder("SELECT" +
                    "  t.create_time AS transactionDate," +
                    "  t.TYPE, t.user_id, t.cust_id, " +
                    "  t.transaction_id AS transactionId," +
                    "  t.amount/1000 as amount," +
                    "  t.remark," +
                    "  t.certificate" +
                    " FROM" +
                    "  t_transaction_" + nowYearMonth + " t where 1=1 ");
            if (StringUtil.isNotEmpty(customerId)) {
                sql.append(" and t.cust_id = '").append(StringEscapeUtils.escapeSql(customerId)).append("'");
            }
            if (StringUtil.isNotEmpty(transactionId)) {
                sql.append(" and t.transaction_id='").append(StringEscapeUtils.escapeSql(transactionId)).append("'");
            }
            if (param.getType() == null) {
                sql.append(" and (type = 1 or type =2)");
            }
            if (param.getType() != null && param.getType() > 0) {
                sql.append(" and t.type=").append(param.getType());
            }
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                sql.append(" and t.create_time between ").append("'").append(StringEscapeUtils.escapeSql(startTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
            }
            sql.append(" order by t.create_time desc ");
            logger.debug(sql.toString());
            page = transactionDao.sqlPageQuery0(sql.toString(), pageNum, pageSize);
            if (page != null && page.getData().size() > 0) {
                //String picPath = ConfigUtil.getInstance().get("pic_server_url") + "/0/";
                String picPath = "upload/pic/0/";
                Customer customer;
                Map<String, Object> m;
                for (int i = 0; i < page.getData().size(); i++) {
                    m = (Map<String, Object>) page.getData().get(i);

                    m.put("userName", userDao.getName(String.valueOf(m.get("user_id"))));
                    customer = customerDao.get(String.valueOf(m.get("cust_id")));
                    if (customer != null) {
                        m.put("enterpriseName", customer.getEnterpriseName());
                    } else {
                        m.put("enterpriseName", "");
                    }
                    if (m.get("certificate") != null) {
                        m.put("certificate", picPath + m.get("certificate"));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取交易日志失败,", e);
        }
        return page;
    }

    public long countTransactionsByCondition_V1(String customerId, TransactionQryParam param) throws Exception {
        long total = 0;
        try {
            String transactionId = param.getTransactionId();
            String startTime = param.getStartTime();
            String endTime = param.getEndTime();
            //当前月
            String nowYearMonth;
            // 如果没有传开始时间
            if (StringUtil.isEmpty(startTime)) {
                nowYearMonth = LocalDate.now().format(YYYYMM);
            } else {
                LocalDateTime localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                nowYearMonth = localStartDateTime.format(YYYYMM);
            }

            // 检查交易记录月表是否存在,不存在则创建
            transactionDao.checkTransactionLogMonthTableNotExist(nowYearMonth);

            StringBuilder sql = new StringBuilder("SELECT COUNT(0) count FROM t_transaction_" + nowYearMonth + " t where 1=1 ");
            if (StringUtil.isNotEmpty(customerId)) {
                sql.append(" and t.cust_id = '").append(StringEscapeUtils.escapeSql(customerId)).append("'");
            }
            if (StringUtil.isNotEmpty(transactionId)) {
                sql.append(" and t.transaction_id='").append(StringEscapeUtils.escapeSql(transactionId)).append("'");
            }
            if (param.getType() != null && param.getType() > 0) {
                sql.append(" and t.type=").append(param.getType());
            }
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                sql.append(" and t.create_time between ").append("'").append(StringEscapeUtils.escapeSql(startTime)).append("' and ").append("'").append(StringEscapeUtils.escapeSql(endTime)).append("'");
            }
            sql.append(" order by t.create_time desc ");
            List<Map<String, Object>> list = transactionDao.sqlQuery(sql.toString());
            if (list != null && list.size() > 0) {
                total = NumberConvertUtil.parseLong(String.valueOf(list.get(0).get("count")));
            }
        } catch (Exception e) {
            logger.error("获取交易日志失败,", e);
            total = 0;
        }
        return total;
    }

    public List<Map<String, Object>> listAllTransactions(TransactionQryParam param) throws Exception {
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

        // 检查交易记录月表是否存在,不存在则创建
        transactionDao.checkTransactionLogMonthTableNotExist(nowYearMonth);

        StringBuilder sql = new StringBuilder("SELECT" +
                " t1.user_id, t1.cust_id, (t1.amount/1000) AS amount," +
                " t1.create_time AS transDate," +
                " t1.TYPE as transType," +
                " t1.transaction_id as transactionId," +
                " t1.remark as remark," +
                " t1.certificate as certificate FROM" +
                " t_transaction_" + nowYearMonth + " t1 where 1=1 ");

        if (StringUtil.isNotEmpty(param.getUserName())) {
            CustomerUser customerUser = customerUserDao.getCustomerUserByName(param.getUserName());
            if (customerUser != null) {
                sql.append(" and t1.user_id='").append(customerUser.getId()).append("'");
            }
        }
        if (param.getType() != null && param.getType() > 0) {
            sql.append(" and t1.type='").append(param.getType()).append("'");
        }
        if (StringUtil.isNotEmpty(param.getTransactionId())) {
            sql.append(" and t1.transaction_id='").append(StringEscapeUtils.escapeSql(param.getTransactionId())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) && StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime())).append("'");
            sql.append(" and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }
        sql.append(" order by t1.create_time desc ");
        List<Map<String, Object>> transactionList = transactionDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list();
        if (transactionList.size() > 0) {
            Customer customer;
            for (Map<String, Object> m : transactionList) {
                m.put("userName", customerUserDao.getName(String.valueOf(m.get("user_id"))));
                customer = customerDao.get(String.valueOf(m.get("cust_id")));
                if (customer != null) {
                    m.put("enterpriseName", customer.getEnterpriseName());
                } else {
                    m.put("enterpriseName", "");
                }
            }
        }
        return transactionList;
    }

    public long countAllTransactions(TransactionQryParam param) throws Exception {
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

        // 检查交易记录月表是否存在,不存在则创建
        transactionDao.checkTransactionLogMonthTableNotExist(nowYearMonth);

        StringBuilder sql = new StringBuilder("SELECT count(*) count FROM" +
                " t_transaction_" + nowYearMonth + " t1 WHERE 1=1 ");

        if (StringUtil.isNotEmpty(param.getUserName())) {
            CustomerUser customerUser = customerUserDao.getCustomerUserByName(param.getUserName());
            if (customerUser != null) {
                sql.append(" and t1.user_id='").append(customerUser.getId()).append("'");
            }
        }
        if (param.getType() != null && param.getType() > 0) {
            sql.append(" and t1.type='").append(param.getType()).append("'");
        }
        if (StringUtil.isNotEmpty(param.getTransactionId())) {
            sql.append(" and t1.transaction_id='").append(StringEscapeUtils.escapeSql(param.getTransactionId())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) && StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime())).append("'");
            sql.append(" and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }
        long total = 0;
        try {
            List<Map<String, Object>> list = transactionDao.sqlQuery(sql.toString());
            if (list.size() > 0) {
                total = NumberConvertUtil.parseLong(String.valueOf(list.get(0).get("count")));
            }
        } catch (Exception e) {
            logger.error("获取交易记录异常", e);
        }
        return total;
    }

    /**
     * 坐席扣费
     *
     * @param custId 客户ID
     * @param amount 客户价格
     * @param prodAmount 供应商价格
     * @param resourceId 资源ID
     * @param remark
     * @param userId 坐席ID
     * @return
     * @throws Exception
     */
    public int seatMonthDeduction(String custId, int amount, int prodAmount, String resourceId, String remark, String userId) throws Exception {
        logger.info("短信扣费参数,custId:" + custId + ",amount:" + amount + ",prodAmount:" + prodAmount + ",resourceId:" + resourceId);
        String nowYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 创建交易记录年月分表
        StringBuilder sql = new StringBuilder();
        sql.append("create table IF NOT EXISTS t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" like t_transaction");
        transactionDao.executeUpdateSQL(sql.toString());

        sql.setLength(0);
        sql.append("insert into t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" (transaction_id, cust_id, type, pay_mode, amount, remark, create_time, resource_id, user_id, prod_amount)values(?,?,?,?,?,?,?,?,?,?)");
        String transactionId = Long.toString(IDHelper.getTransactionId());
        if (StringUtil.isEmpty(userId)) {
            userId = "-1";
        }

        int status = transactionDao.executeUpdateSQL(sql.toString(), transactionId, custId, 5,
                1, amount, remark, new Timestamp(System.currentTimeMillis()), resourceId, userId, prodAmount);
        //扣除客户费用
        customerDao.accountDeductions(custId, new BigDecimal(amount));
        //扣除供应商费用
        List<Map<String, Object>> marketResource = transactionDao.sqlQuery("SELECT * FROM t_market_resource WHERE resource_id = ? AND `status` = 1", resourceId);
        String supplierId = null;
        if (marketResource != null && marketResource.size() > 0) {
            supplierId = String.valueOf(marketResource.get(0).get("supplier_id"));
        }
        supplierDao.supplierAccountDeductions(supplierId, new BigDecimal(prodAmount));
        return status;
    }

    /**
     * 客户和供应商资源扣费
     * @param custId
     * @param type
     * @param amount
     * @param prodAmount
     * @param resourceId
     * @param remark
     * @param userId
     * @return
     * @throws Exception
     */
    public int customerSupplierDeduction(String custId, int type, int amount, int prodAmount, String resourceId, String remark, String userId) throws Exception {
        logger.info("短信扣费参数,custId:" + custId + ",amount:" + amount + ",prodAmount:" + prodAmount + ",resourceId:" + resourceId);
        String nowYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 创建交易记录年月分表
        StringBuilder sql = new StringBuilder();
        sql.append("create table IF NOT EXISTS t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" like t_transaction");
        transactionDao.executeUpdateSQL(sql.toString());

        sql.setLength(0);
        sql.append("insert into t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" (transaction_id, cust_id, type, pay_mode, amount, remark, create_time, resource_id, user_id, prod_amount)values(?,?,?,?,?,?,?,?,?,?)");
        String transactionId = Long.toString(IDHelper.getTransactionId());
        if (StringUtil.isEmpty(userId)) {
            userId = "-1";
        }

        int status = transactionDao.executeUpdateSQL(sql.toString(), transactionId, custId, type,
                1, amount, remark, new Timestamp(System.currentTimeMillis()), resourceId, userId, prodAmount);
        //扣除客户费用
        customerDao.accountDeductions(custId, new BigDecimal(amount));
        //扣除供应商费用
        List<Map<String, Object>> marketResource = transactionDao.sqlQuery("SELECT * FROM t_market_resource WHERE resource_id = ? AND `status` = 1", resourceId);
        String supplierId = null;
        if (marketResource != null && marketResource.size() > 0) {
            supplierId = String.valueOf(marketResource.get(0).get("supplier_id"));
        }
        supplierDao.supplierAccountDeductions(supplierId, new BigDecimal(prodAmount));
        return status;
    }

}

