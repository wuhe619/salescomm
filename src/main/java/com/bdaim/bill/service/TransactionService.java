package com.bdaim.bill.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.bill.dto.SeatCallDeductionResult;
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
            int sumAmount = (Integer) jdbcTemplate.queryForMap(sqlSumAmount.toString()).get("sumAmount");
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
            int sumIn = (Integer) jdbcTemplate.queryForMap(sqlSumAmount.toString()).get("sumAmount");
            //分转化为元
            sumamountIn = Float.valueOf(String.valueOf(sumIn)) / 100;
            ret.put("sumamountIn", sumamountIn);

            StringBuilder sqlSumAmount1 = new StringBuilder("SELECT SUM(c.prod_amount) as sumAmount FROM stat_bill_month c where 1=1 ");
            sqlAppendFucCopy(sqlSumAmount1, customerId, 1, startTime, params);
            logger.debug(sqlSumAmount1.toString());
            int sumOut = (Integer) jdbcTemplate.queryForMap(sqlSumAmount1.toString()).get("sumAmount");
            //分转化为元
            sumamountOut = Float.valueOf(String.valueOf(sumOut)) / 100;
            ret.put("sumamountOut", sumamountOut);
        } else if (type == 7) {//总支出
            StringBuilder sqlSumAmount2 = new StringBuilder("SELECT SUM(c.prod_amount) as sumAmount FROM stat_bill_month c where 1=1 ");
            sqlAppendFucCopy(sqlSumAmount2, customerId, 1, startTime, params);
            logger.debug(sqlSumAmount2.toString());
            int sumOut = (Integer) jdbcTemplate.queryForMap(sqlSumAmount2.toString()).get("sumAmount");
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


    @Deprecated
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
            List<Object> p = new ArrayList<>();
            StringBuilder sql = new StringBuilder("SELECT\n" +
                    "  t.create_time AS transactionDate,\n" +
                    "  t.TYPE,\n" +
                    "  t.transaction_id AS transactionId,\n" +
                    "  FORMAT(t.amount/1000,2) as amount,\n" +
                    "  t.remark,\n" +
                    "  t.certificate\n" +
                    " FROM\n" +
                    "  t_transaction_" + nowYearMonth + " t where 1=1 ");
            p.add(StringEscapeUtils.escapeSql(customerId));
            sql.append(" and t.cust_id = ? ");
            if (StringUtil.isNotEmpty(transactionId)) {
                p.add(StringEscapeUtils.escapeSql(transactionId));
                sql.append(" and t.transaction_id=? ");
            }
            if (type > 0) {
                p.add(type);
                sql.append(" and t.type= ? ");
            }
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                p.add(startTime);
                p.add(endTime);
                sql.append(" and t.create_time between ? and ? ");
            }
            sql.append(" order by t.create_time desc ");
            logger.debug(sql.toString());

            Page page = transactionDao.sqlPageQuery(sql.toString(), pageNum, pageSize, p.toArray());
            List<Map<String, Object>> values = page.getData();
            try {
                //values = transactionDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
                ret.put("total", page.getTotal());
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

    @Deprecated
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
            List<Object> p = new ArrayList<>();
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
                p.add(customerId);
                sql.append(" and t.cust_id = ? ");
            }
            if (StringUtil.isNotEmpty(transactionId)) {
                p.add(transactionId);
                sql.append(" and t.transaction_id= ? ");
            }
            if (param.getType() == null) {
                sql.append(" and (type = 1 or type =2)");
            }
            if (param.getType() != null && param.getType() > 0) {
                p.add(param.getType());
                sql.append(" and t.type=? ");
            }
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                p.add(startTime);
                p.add(endTime);
                sql.append(" and t.create_time between ? and ? ");
            }
            sql.append(" order by t.create_time desc ");
            logger.debug(sql.toString());
            page = transactionDao.sqlPageQuery0(sql.toString(), pageNum, pageSize, p.toArray());
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

    @Deprecated
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
            List<Object> p = new ArrayList<>();
            if (StringUtil.isNotEmpty(customerId)) {
                p.add(customerId);
                sql.append(" and t.cust_id = ? ");
            }
            if (StringUtil.isNotEmpty(transactionId)) {
                p.add(customerId);
                sql.append(" and t.transaction_id=? ");
            }
            if (param.getType() != null && param.getType() > 0) {
                sql.append(" and t.type=? ");
                p.add(param.getType());
            }
            if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                p.add(startTime);
                p.add(endTime);
                sql.append(" and t.create_time between ? and ?");
            }
            sql.append(" order by t.create_time desc ");
            List<Map<String, Object>> list = transactionDao.sqlQuery(sql.toString(), p.toArray());
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

        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getUserName())) {
            CustomerUser customerUser = customerUserDao.getCustomerUserByName(param.getUserName());
            if (customerUser != null) {
                p.add(customerUser.getId());
                sql.append(" and t1.user_id= ? ");
            }
        }
        if (param.getType() != null && param.getType() > 0) {
            p.add(param.getType());
            sql.append(" and t1.type=? ");
        }
        if (StringUtil.isNotEmpty(param.getTransactionId())) {
            p.add(param.getTransactionId());
            sql.append(" and t1.transaction_id=? ");
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) && StringUtil.isNotEmpty(param.getEndTime())) {
            p.add(param.getStartTime());
            p.add(param.getEndTime());
            sql.append(" and t1.create_time between ? and ? ");
        }
        sql.append(" order by t1.create_time desc ");
        List<Map<String, Object>> transactionList = transactionDao.sqlPageQuery(sql.toString(), param.getPageNum(), param.getPageSize(), p.toArray()).getData();
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

        List<Object> p = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT count(*) count FROM" +
                " t_transaction_" + nowYearMonth + " t1 WHERE 1=1 ");

        if (StringUtil.isNotEmpty(param.getUserName())) {
            CustomerUser customerUser = customerUserDao.getCustomerUserByName(param.getUserName());
            if (customerUser != null) {
                sql.append(" and t1.user_id=? ");
                p.add(customerUser.getId());
            }
        }
        if (param.getType() != null && param.getType() > 0) {
            sql.append(" and t1.type=? ");
            p.add(param.getType());
        }
        if (StringUtil.isNotEmpty(param.getTransactionId())) {
            p.add(param.getTransactionId());
            sql.append(" and t1.transaction_id=? ");
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) && StringUtil.isNotEmpty(param.getEndTime())) {
            p.add(param.getStartTime());
            p.add(param.getEndTime());
            sql.append(" and t1.create_time between ? and ? ");
        }
        long total = 0;
        try {
            List<Map<String, Object>> list = transactionDao.sqlQuery(sql.toString(), p.toArray());
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
     * @param custId     客户ID
     * @param amount     客户价格
     * @param prodAmount 供应商价格
     * @param resourceId 资源ID
     * @param remark
     * @param userId     坐席ID
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
     *
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
        logger.info("扣费参数,custId:" + custId + ",amount:" + amount + ",prodAmount:" + prodAmount + ",resourceId:" + resourceId);
        String nowYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 创建交易记录年月分表
        StringBuilder sql = new StringBuilder();
        sql.append("create table IF NOT EXISTS t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" like t_transaction");
        transactionDao.executeUpdateSQL(sql.toString());

        //扣除客户费用
        customerDao.accountDeductions(custId, new BigDecimal(amount));
        //扣除供应商费用
        List<Map<String, Object>> marketResource = transactionDao.sqlQuery("SELECT * FROM t_market_resource WHERE resource_id = ? AND `status` = 1", resourceId);
        String supplierId = null;
        if (marketResource != null && marketResource.size() > 0) {
            supplierId = String.valueOf(marketResource.get(0).get("supplier_id"));
        }
        supplierDao.supplierAccountDeductions(supplierId, new BigDecimal(prodAmount));

        sql.setLength(0);
        sql.append("insert into t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" (transaction_id, cust_id, type, pay_mode, amount, remark, create_time, resource_id, user_id, prod_amount,supplier_id)values(?,?,?,?,?,?,?,?,?,?,?)");
        String transactionId = Long.toString(IDHelper.getTransactionId());
        if (StringUtil.isEmpty(userId)) {
            userId = "-1";
        }
        // 记录账单
        int status = transactionDao.executeUpdateSQL(sql.toString(), transactionId, custId, type,
                1, amount, remark, new Timestamp(System.currentTimeMillis()), resourceId, userId, prodAmount, supplierId);
        return status;
    }

    public JSONObject selectCustCallConfig(String custId) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT property_value FROM t_customer_property WHERE property_name='call_config' AND cust_id = ? ");
        List<Map<String, Object>> list = transactionDao.sqlQuery(sql.toString(), custId);
        if (list.size() > 0) {
            Map<String, Object> config = list.get(0);
            JSONObject jsonObject = null;
            String configs = String.valueOf(config.get("property_value"));
            Object object = JSON.parse(configs);
            if (object instanceof JSONObject) {
                jsonObject = (JSONObject) object;
            } else if (object instanceof JSONArray) {
                jsonObject = ((JSONArray) object).getJSONObject(0);
            }
            return jsonObject;
        }
        logger.warn("客户ID:" + custId + "未配置通话资源!");
        return null;
    }

    /**
     * 根据客户ID和资源ID查询客户通话售价
     *
     * @param custId
     * @param resourceId
     * @return
     * @throws Exception
     */
    public JSONObject selectCustCallConfig(String custId, String resourceId) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT property_value FROM t_customer_property WHERE property_name='call_config' AND cust_id = ? ");
        List<Map<String, Object>> list = transactionDao.sqlQuery(sql.toString(), custId);
        if (list.size() > 0) {
            Map<String, Object> custConfig = list.get(0);
            String configs = String.valueOf(custConfig.get("property_value"));
            JSONArray jsonArray = JSON.parseArray(configs);
            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (StringUtil.isNotEmpty(resourceId) && resourceId.equals(jsonObject.getString("resourceId"))) {
                    logger.info("客户ID:" + custId + ",资源ID:" + resourceId + "配置的通话资源:" + jsonObject);
                    return jsonObject;
                }
            }
        }
        logger.warn("客户ID:" + custId + ",资源ID:" + resourceId + "未配置通话资源!");
        return null;
    }

    /**
     * 根据资源ID获取供应商通话定价
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    public JSONObject selectSupplierCallConfig(String resourceId) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t2.property_value FROM t_market_resource t1 JOIN t_market_resource_property t2 ON t1.resource_id = t2.resource_id ");
        sql.append(" WHERE t1.`status` = 1 AND t2.property_name='price_config' AND t1.resource_id=?");
        List<Map<String, Object>> list = transactionDao.sqlQuery(sql.toString(), resourceId);
        if (list.size() > 0) {
            Map<String, Object> resourceConfig = list.get(0);
            String config = String.valueOf(resourceConfig.get("property_value"));
            JSONObject jsonObject = JSON.parseObject(config);
            logger.info("资源ID:" + resourceId + "配置的通话资源:" + jsonObject);
            return jsonObject;
        }
        logger.warn("资源ID:" + resourceId + "未配置通话资源!");
        return null;
    }

    /**
     * 客户坐席剩余包月分钟数
     *
     * @param userId
     * @param resourceId
     * @return
     * @throws Exception
     */
    public int selectCustSeatSurplusMinute(String userId, String resourceId) throws Exception {
        String sql = "SELECT property_value FROM t_customer_user_property WHERE user_id = ? AND property_name = ?";
        List<Map<String, Object>> seatSurplusMinuteList = transactionDao.sqlQuery(sql, userId, "cust_" + resourceId + "_minute");
        if (seatSurplusMinuteList != null && seatSurplusMinuteList.size() > 0 && StringUtil.isNotEmpty(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")))) {
            return Integer.parseInt(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")));
        }
        return 0;
    }

    /**
     * 供应商坐席剩余包月分钟数
     *
     * @param userId
     * @param resourceId
     * @return
     * @throws Exception
     */
    public int selectSupplierSeatSurplusMinute(String userId, String resourceId) throws Exception {
        String sql = "SELECT property_value FROM t_customer_user_property WHERE user_id = ? AND property_name = ?";
        List<Map<String, Object>> seatSurplusMinuteList = transactionDao.sqlQuery(sql, userId, "supplier_" + resourceId + "_minute");
        if (seatSurplusMinuteList != null && seatSurplusMinuteList.size() > 0 && StringUtil.isNotEmpty(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")))) {
            return Integer.parseInt(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")));
        }
        return 0;
    }

    /**
     * 扣除坐席的剩余分钟数
     *
     * @param userId
     * @param resourceId
     * @param minute
     * @param propertyName
     * @return
     * @throws Exception
     */
    private int updateSeatSurplusMinute(String userId, String resourceId, long minute, String propertyName) throws Exception {
        // 客户坐席扣费分钟数
        logger.info("坐席:" + userId + ",propertyName:" + propertyName + "侧扣除分钟数:" + minute);
        String sql = "SELECT property_value FROM t_customer_user_property WHERE user_id = ? AND property_name = ?";
        List<Map<String, Object>> seatSurplusMinuteList = transactionDao.sqlQuery(sql, userId, propertyName);
        int seatSurplusMinute = 0;
        if (seatSurplusMinuteList != null && seatSurplusMinuteList.size() > 0 && StringUtil.isNotEmpty(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")))) {
            seatSurplusMinute = Integer.parseInt(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")));
        }
        if (seatSurplusMinute <= 0) {
            logger.warn("坐席:" + userId + ",propertyName:" + propertyName + "侧剩余分钟数为:" + seatSurplusMinute);
            return 0;
        }
        String updateSql = "UPDATE t_customer_user_property SET property_value=? WHERE user_id = ? AND property_name = ?";
        logger.info("坐席:" + userId + ",propertyName:" + propertyName + "侧剩余分钟数:" + (seatSurplusMinute - minute));
        int code = transactionDao.executeUpdateSQL(updateSql, seatSurplusMinute - minute, userId, propertyName);
        logger.info("坐席:" + userId + ",propertyName:" + propertyName + "侧剩余分钟数状态:" + code);
        return code;
    }


    public SeatCallDeductionResult seatCallDeduction(String userId, String custId, String resourceId, int provinceCallType, int callMinute) throws Exception {
        JSONObject custCallPriceConfig = this.selectCustCallConfig(custId);
        if (custCallPriceConfig == null) {
            throw new RuntimeException("客户ID:" + custId + "通话定价未配置!");
        }
        int custSeatSurplusMinute, supplierSeatSurplusMinute, custCallMinutePrice, supplierCallMinutePrice;
        JSONObject supplierCallConfig;
        if (custCallPriceConfig != null) {
            // 查询供应商通话配置
            supplierCallConfig = this.selectSupplierCallConfig(resourceId);
            // 查询企业下的坐席剩余分钟数
            custSeatSurplusMinute = this.selectCustSeatSurplusMinute(userId, resourceId);
            // 查询供应商下的坐席剩余分钟数
            supplierSeatSurplusMinute = this.selectSupplierSeatSurplusMinute(userId, resourceId);
            logger.info("坐席:" + userId + "客户侧剩余分钟数:" + custSeatSurplusMinute);
            logger.info("坐席:" + userId + "供应商侧剩余分钟数:" + supplierSeatSurplusMinute);
        } else {
            logger.warn("客户ID:" + custId + "通话价格未配置!");
            throw new RuntimeException("客户ID:" + custId + "通话价格未配置!");
        }
        SeatCallDeductionResult result = new SeatCallDeductionResult();
        //客户通话扣费
        if (custSeatSurplusMinute > 0) {
            String propertyName = "cust_" + resourceId + "_minute";
            // 通话剩余分钟大于等于通话分钟
            if (custSeatSurplusMinute >= callMinute) {
                logger.info("客户坐席执行只扣除分钟数:" + userId + "扣除后剩余分钟数:" + (custSeatSurplusMinute - callMinute));
                updateSeatSurplusMinute(userId, resourceId, callMinute, propertyName);
                result.setSummMinute(callMinute);
            } else {
                result.setSummMinute(custSeatSurplusMinute);
                // 减去剩余分钟数之后的扣费分钟数
                int tmpSurplusMinute = callMinute - custSeatSurplusMinute;
                updateSeatSurplusMinute(userId, resourceId, custSeatSurplusMinute, propertyName);
                logger.info("客户坐席执行扣除分钟数和通话计费:" + userId + "扣除分钟数:" + custSeatSurplusMinute + ",减去分钟数后剩余通话时长:" + tmpSurplusMinute);
                // 查询客户通话费用
                custCallMinutePrice = NumberConvertUtil.changeY2L(custCallPriceConfig.getDoubleValue("call_price"));
                int tmpCustAmount = custCallMinutePrice * tmpSurplusMinute;
                //扣除通话时长费用
                customerDao.accountDeductions(custId, new BigDecimal(tmpCustAmount));
                result.setCustAmount(tmpCustAmount);
            }
        } else {
            custCallMinutePrice = NumberConvertUtil.changeY2L(custCallPriceConfig.getDoubleValue("call_price"));
            //扣除通话时长费用
            int tmpCustAmount = custCallMinutePrice * callMinute;
            customerDao.accountDeductions(custId, new BigDecimal(tmpCustAmount));
            result.setCustAmount(tmpCustAmount);
        }

        //供应商通话扣费
        if (supplierSeatSurplusMinute > 0) {
            String propertyName = "supplier_" + resourceId + "_minute";
            // 通话剩余分钟大于等于通话分钟
            if (supplierSeatSurplusMinute >= callMinute) {
                logger.info("供应商坐席执行只扣除分钟数:" + userId + "扣除后剩余分钟数:" + (supplierSeatSurplusMinute - callMinute));
                updateSeatSurplusMinute(userId, resourceId, callMinute, propertyName);
                result.setSummMinute(callMinute);
            } else {
                result.setSummMinute(supplierSeatSurplusMinute);
                // 减去剩余分钟数之后的扣费分钟数
                int tmpSurplusMinute = callMinute - supplierSeatSurplusMinute;
                updateSeatSurplusMinute(userId, resourceId, supplierSeatSurplusMinute, propertyName);
                logger.info("供应商坐席执行扣除分钟数和通话计费:" + userId + "扣除分钟数:" + supplierSeatSurplusMinute + ",减去分钟数后通话时长:" + tmpSurplusMinute);

                // 通用分钟费用
                supplierCallMinutePrice = NumberConvertUtil.changeY2L(supplierCallConfig.getDoubleValue("call_price"));
                // 查询是否配置了本省通话费用
                String provincePriceConfig = "";
                if (provinceCallType == 1) {
                    provincePriceConfig = String.valueOf(supplierCallConfig.get("province_price"));
                    if (StringUtil.isNotEmpty(provincePriceConfig)) {
                        supplierCallMinutePrice = NumberConvertUtil.changeY2L(Double.parseDouble(provincePriceConfig.split(",")[0]));
                    }
                } else if (provinceCallType == 2) {
                    // 外省通话
                    provincePriceConfig = String.valueOf(supplierCallConfig.get("province_price"));
                    if (StringUtil.isNotEmpty(provincePriceConfig)) {
                        supplierCallMinutePrice = NumberConvertUtil.changeY2L(Double.parseDouble(provincePriceConfig.split(",")[1]));
                    }
                }

                int tmpProdAmount = supplierCallMinutePrice * tmpSurplusMinute;
                //扣除通话时长费用
                supplierDao.supplierAccountDeductions(String.valueOf(custCallPriceConfig.get("supplierId")), new BigDecimal(tmpProdAmount));
                result.setProdAmount(tmpProdAmount);
            }
        } else {
            supplierCallMinutePrice = NumberConvertUtil.changeY2L(supplierCallConfig.getDoubleValue("call_price"));
            // 本省通话
            String provincePriceConfig = "";
            if (provinceCallType == 1) {
                provincePriceConfig = String.valueOf(supplierCallConfig.get("province_price"));
                if (StringUtil.isNotEmpty(provincePriceConfig) && provincePriceConfig.split(",").length == 2) {
                    supplierCallMinutePrice = NumberConvertUtil.changeY2L(NumberConvertUtil.parseDouble(provincePriceConfig.split(",")[0]));
                } else {
                    throw new RuntimeException("资源:" + resourceId + "省份通话配置信息错误,provincePriceConfig:" + provincePriceConfig);
                }
            } else if (provinceCallType == 2) {
                // 外省通话
                provincePriceConfig = String.valueOf(supplierCallConfig.get("province_price"));
                if (StringUtil.isNotEmpty(provincePriceConfig) && provincePriceConfig.split(",").length == 2) {
                    supplierCallMinutePrice = NumberConvertUtil.changeY2L(NumberConvertUtil.parseDouble(provincePriceConfig.split(",")[1]));
                } else {
                    throw new RuntimeException("资源:" + resourceId + "省份通话配置信息错误,provincePriceConfig:" + provincePriceConfig);
                }
            }
            int tmpProdAmount = supplierCallMinutePrice * callMinute;
            //扣除通话时长费用
            supplierDao.supplierAccountDeductions(String.valueOf(custCallPriceConfig.get("supplierId")), new BigDecimal(tmpProdAmount));
            result.setProdAmount(tmpProdAmount);
        }
        return result;
    }

    public SeatCallDeductionResult seatCallDeduction0(String userId, String custId, String resourceId, int provinceCallType, int callMinute) throws Exception {
        JSONObject custCallPriceConfig = this.selectCustCallConfig(custId, resourceId);
        if (custCallPriceConfig == null) {
            throw new RuntimeException("客户ID:" + custId + "通话定价未配置!");
        }
        int custSeatSurplusMinute, supplierSeatSurplusMinute, custCallMinutePrice, supplierCallMinutePrice;
        JSONObject supplierCallConfig;
        if (custCallPriceConfig != null) {
            // 查询供应商通话配置
            supplierCallConfig = this.selectSupplierCallConfig(resourceId);
            // 查询企业下的坐席剩余分钟数
            custSeatSurplusMinute = this.selectCustSeatSurplusMinute(userId, resourceId);
            // 查询供应商下的坐席剩余分钟数
            supplierSeatSurplusMinute = this.selectSupplierSeatSurplusMinute(userId, resourceId);
            logger.info("坐席:" + userId + ",客户侧剩余分钟数:" + custSeatSurplusMinute);
            logger.info("坐席:" + userId + ",供应商侧剩余分钟数:" + supplierSeatSurplusMinute);
        } else {
            logger.warn("客户ID:" + custId + "通话价格未配置!");
            throw new RuntimeException("客户ID:" + custId + ",通话价格未配置!");
        }
        SeatCallDeductionResult result = new SeatCallDeductionResult();
        //客户通话扣费
        if (custSeatSurplusMinute > 0) {
            String propertyName = "cust_" + resourceId + "_minute";
            // 通话剩余分钟大于等于通话分钟
            if (custSeatSurplusMinute >= callMinute) {
                logger.info("客户坐席执行只扣除分钟数:" + userId + "扣除后剩余分钟数:" + (custSeatSurplusMinute - callMinute));
                updateSeatSurplusMinute(userId, resourceId, callMinute, propertyName);
                result.setSummMinute(callMinute);
            } else {
                result.setSummMinute(custSeatSurplusMinute);
                // 减去剩余分钟数之后的扣费分钟数
                int tmpSurplusMinute = callMinute - custSeatSurplusMinute;
                updateSeatSurplusMinute(userId, resourceId, custSeatSurplusMinute, propertyName);
                logger.info("客户坐席执行扣除分钟数和通话计费:" + userId + "扣除分钟数:" + custSeatSurplusMinute + ",减去分钟数后剩余通话时长:" + tmpSurplusMinute);
                // 查询客户通话费用
                custCallMinutePrice = NumberConvertUtil.changeY2L(custCallPriceConfig.getDoubleValue("call_price"));
                int tmpCustAmount = custCallMinutePrice * tmpSurplusMinute;
                //扣除通话时长费用
                customerDao.accountDeductions(custId, new BigDecimal(tmpCustAmount));
                result.setCustAmount(tmpCustAmount);
            }
        } else {
            custCallMinutePrice = NumberConvertUtil.changeY2L(custCallPriceConfig.getDoubleValue("call_price"));
            //扣除通话时长费用
            int tmpCustAmount = custCallMinutePrice * callMinute;
            customerDao.accountDeductions(custId, new BigDecimal(tmpCustAmount));
            result.setCustAmount(tmpCustAmount);
        }

        //供应商通话扣费
        if (supplierSeatSurplusMinute > 0) {
            String propertyName = "supplier_" + resourceId + "_minute";
            // 通话剩余分钟大于等于通话分钟
            if (supplierSeatSurplusMinute >= callMinute) {
                logger.info("供应商坐席执行只扣除分钟数:" + userId + "扣除后剩余分钟数:" + (supplierSeatSurplusMinute - callMinute));
                updateSeatSurplusMinute(userId, resourceId, callMinute, propertyName);
                result.setSummMinute(callMinute);
            } else {
                result.setSummMinute(supplierSeatSurplusMinute);
                // 减去剩余分钟数之后的扣费分钟数
                int tmpSurplusMinute = callMinute - supplierSeatSurplusMinute;
                updateSeatSurplusMinute(userId, resourceId, supplierSeatSurplusMinute, propertyName);
                logger.info("供应商坐席执行扣除分钟数和通话计费:" + userId + "扣除分钟数:" + supplierSeatSurplusMinute + ",减去分钟数后通话时长:" + tmpSurplusMinute);

                // 通用分钟费用
                supplierCallMinutePrice = NumberConvertUtil.changeY2L(supplierCallConfig.getDoubleValue("call_price"));
                // 查询是否配置了本省通话费用
                String provincePriceConfig = "";
                if (provinceCallType == 1) {
                    provincePriceConfig = String.valueOf(supplierCallConfig.get("province_price"));
                    if (StringUtil.isNotEmpty(provincePriceConfig)) {
                        supplierCallMinutePrice = NumberConvertUtil.changeY2L(Double.parseDouble(provincePriceConfig.split(",")[0]));
                    }
                } else if (provinceCallType == 2) {
                    // 外省通话
                    provincePriceConfig = String.valueOf(supplierCallConfig.get("province_price"));
                    if (StringUtil.isNotEmpty(provincePriceConfig)) {
                        supplierCallMinutePrice = NumberConvertUtil.changeY2L(Double.parseDouble(provincePriceConfig.split(",")[1]));
                    }
                }

                int tmpProdAmount = supplierCallMinutePrice * tmpSurplusMinute;
                //扣除通话时长费用
                supplierDao.supplierAccountDeductions(String.valueOf(custCallPriceConfig.get("supplierId")), new BigDecimal(tmpProdAmount));
                result.setProdAmount(tmpProdAmount);
            }
        } else {
            supplierCallMinutePrice = NumberConvertUtil.changeY2L(supplierCallConfig.getDoubleValue("call_price"));
            // 本省通话
            String provincePriceConfig = "";
            if (provinceCallType == 1) {
                provincePriceConfig = String.valueOf(supplierCallConfig.get("province_price"));
                if (StringUtil.isNotEmpty(provincePriceConfig) && provincePriceConfig.split(",").length == 2) {
                    supplierCallMinutePrice = NumberConvertUtil.changeY2L(NumberConvertUtil.parseDouble(provincePriceConfig.split(",")[0]));
                } else {
                    throw new RuntimeException("资源:" + resourceId + "省份通话配置信息错误,provincePriceConfig:" + provincePriceConfig);
                }
            } else if (provinceCallType == 2) {
                // 外省通话
                provincePriceConfig = String.valueOf(supplierCallConfig.get("province_price"));
                if (StringUtil.isNotEmpty(provincePriceConfig) && provincePriceConfig.split(",").length == 2) {
                    supplierCallMinutePrice = NumberConvertUtil.changeY2L(NumberConvertUtil.parseDouble(provincePriceConfig.split(",")[1]));
                } else {
                    throw new RuntimeException("资源:" + resourceId + "省份通话配置信息错误,provincePriceConfig:" + provincePriceConfig);
                }
            }
            int tmpProdAmount = supplierCallMinutePrice * callMinute;
            //扣除通话时长费用
            supplierDao.supplierAccountDeductions(String.valueOf(custCallPriceConfig.get("supplierId")), new BigDecimal(tmpProdAmount));
            result.setProdAmount(tmpProdAmount);
        }
        return result;
    }


}

