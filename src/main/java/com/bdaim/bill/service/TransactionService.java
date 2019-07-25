package com.bdaim.bill.service;

import com.alibaba.fastjson.JSON;
import com.bdaim.account.dao.TransactionDao;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.slxf.entity.TransactionQryParam;
import com.github.crab2died.ExcelUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.OutputStream;
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
    Logger logger = Logger.getLogger(TransactionService.class);
    private final static DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private TransactionDao transactionDao;

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
        Page pageret = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
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

}

