package com.bdaim.account.dao;

import com.bdaim.account.entity.TransactionDO;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr.YinXin on 2017/2/24.
 */
@Component
public class TransactionDao extends SimpleHibernateDao<TransactionDO, String> {
    private final static Logger LOG = LoggerFactory.getLogger(TransactionDao.class);
    /**
     * 联通包月分钟数配置key
     */
    private final static String SEAT_MONTH_PACKAGE_MINUTE_KEY = "cuc_minute";


    /**
     * 企业账户扣款
     *
     * @param custId
     * @param amount
     * @return
     * @throws Exception
     */
    public boolean accountDeductionsDev(String custId, BigDecimal amount) throws Exception {
        String sql = "SELECT * FROM t_customer_property m where m.cust_id=? and m.property_name=?";
        List<Map<String, Object>> list = this.sqlQuery(sql, custId, "remain_amount");
        String remainAmount = null;
        if (list.size() > 0) {
            remainAmount = String.valueOf(list.get(0).get("property_value"));
        }
        // 处理账户不存在
        if (remainAmount == null) {
            remainAmount = "0";
            LOG.info(custId + " 账户不存在开始新建账户信息");
            String insertSql = "INSERT INTO `t_customer_property` (`cust_id`, `property_name`, `property_value`, `create_time`) VALUES (?, ?, ?, ?);";
            int status = this.executeUpdateSQL(insertSql, custId, "remain_amount", remainAmount, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            LOG.info(custId + " 账户创建结果:" + status);
        }
        // 处理累计消费金额不存在
        List<Map<String, Object>> usedAmountList = this.sqlQuery(sql, custId, "used_amount");
        String usedAmount = null;
        if (usedAmountList.size() > 0) {
            usedAmount = String.valueOf(usedAmountList.get(0).get("property_value"));
        }

        if (usedAmount == null) {
            // 累计消费 处理账户不存在
            usedAmount = "0";
            LOG.info(custId + " 账户累计消费不存在开始新建账户信息");
            String insertSql = "INSERT INTO `t_customer_property` (`cust_id`, `property_name`, `property_value`, `create_time`) VALUES (?, ?, ?, ?);";
            int status = this.executeUpdateSQL(insertSql, custId, "used_amount", usedAmount, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
            LOG.info(custId + " 账户累计消费创建结果:" + status);

        }
        if (StringUtil.isNotEmpty(remainAmount)) {
            if (Double.parseDouble(remainAmount) <= 0) {
                LOG.info(custId + " 账户余额:" + remainAmount + ",先执行扣减");
            }
            DecimalFormat df = new DecimalFormat("#");
            BigDecimal remainAmountBigDecimal = new BigDecimal(remainAmount);
            String nowMoney = df.format(remainAmountBigDecimal.subtract(amount));
            String updateSql = "UPDATE t_customer_property SET property_value = ? WHERE cust_id = ? AND property_name = ?";
            this.executeUpdateSQL(updateSql, nowMoney, custId, "remain_amount");

            // 处理累计消费累加
            BigDecimal usedAmountBigDecimal = new BigDecimal(usedAmount);
            String usedAmountMoney = df.format(usedAmountBigDecimal.add(amount));
            this.executeUpdateSQL(updateSql, usedAmountMoney, custId, "used_amount");
            return true;

        }
        return false;
    }


    /**
     * @description 查询坐席扣费金额
     * @author:duanliying
     * @method
     * @date: 2018/12/12 17:18
     */
    public int querySeatsMoney(String customerId, String propertyName, int callTime) {
        int salePrice = 0;
        // 查询联通渠道的通话销售定价
        String moneySql = "SELECT property_value FROM t_customer_property WHERE cust_id = ? AND property_name = ?; ";
        List<Map<String, Object>> moneyList = this.sqlQuery(moneySql, customerId, propertyName);
        if (moneyList != null && moneyList.size() > 0) {
            salePrice = Integer.parseInt(String.valueOf(moneyList.get(0).get("property_value")));
            LOG.info("联通坐席扣费开始从账户customerId:" + customerId + "余额扣款,sale_price:" + salePrice);
            salePrice = salePrice * callTime;
        }
        return salePrice;
    }

    /**
     * 更新企业坐席剩余包月分钟数
     *
     * @param userId
     * @param minute
     * @return
     * @throws Exception
     */
    public int updateSeatMinute(String userId, int minute) {
        logger.info("坐席:" + userId + "扣除分钟数:" + minute);
        String sql = "SELECT property_value FROM t_customer_user_property WHERE user_id = ? AND property_name = ?";
        List<Map<String, Object>> seatSurplusMinuteList = this.sqlQuery(sql, userId, SEAT_MONTH_PACKAGE_MINUTE_KEY);
        int seatSurplusMinute = 0;
        if (seatSurplusMinuteList != null && seatSurplusMinuteList.size() > 0 && StringUtil.isNotEmpty(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")))) {
            seatSurplusMinute = Integer.parseInt(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")));
        }
        if (seatSurplusMinute <= 0) {
            logger.warn("userId:" + userId + "剩余分钟数为:" + seatSurplusMinute);
            return 0;
        }
        String updateSql = "UPDATE t_customer_user_property SET property_value=? WHERE user_id = ? AND property_name = ?";
        logger.info("坐席:" + userId + "剩余分钟数:" + (seatSurplusMinute - minute));
        return this.executeUpdateSQL(updateSql, seatSurplusMinute - minute, userId, SEAT_MONTH_PACKAGE_MINUTE_KEY);
    }

    /**
     * 账户交易
     *
     * @param custId
     * @param type        交易类型（1.充值 2.用户群扣费 3.短信扣费 4.通话扣费 5.座席扣费 6.修复扣费.）
     * @param amount      金额(分)
     * @param payMode     支付类型（1.余额 2.第三方 3.线下 4.包月分钟）
     * @param supplierId  供应商ID
     * @param metaData    第三方信息
     * @param remark
     * @param userId
     * @param certificate
     * @return
     * @throws Exception
     */
    public boolean saveTransactionLog(String custId, int type, double amount, int payMode, String supplierId, String metaData, String remark, long userId, String certificate) throws Exception {
        String nowYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 创建交易记录年月分表
        StringBuilder sql = new StringBuilder();
        sql.append("create table IF NOT EXISTS t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" like t_transaction");
        this.executeUpdateSQL(sql.toString());

        sql.setLength(0);
        sql.append("insert into t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" (transaction_id, cust_id, type, pay_mode, meta_data, amount, remark, create_time, supplier_id, user_id, certificate)values(?,?,?,?,?,?,?,?,?,?,?)");
        String transactionId = Long.toString(IDHelper.getTransactionId());
        this.executeUpdateSQL(sql.toString(), new Object[]{transactionId, custId, type,
                payMode, metaData, Math.abs(amount), remark, new Timestamp(System.currentTimeMillis()), supplierId, userId, certificate});

        return true;
    }


    public boolean saveTransactionLog(String custId, int type, int amount, int payMode, String supplierId, String metaData, String remark, long userId, String certificate, String resourceId) throws Exception {
        String nowYearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        // 创建交易记录年月分表
        StringBuilder sql = new StringBuilder();
        sql.append("create table IF NOT EXISTS t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" like t_transaction");
        this.executeUpdateSQL(sql.toString());

        sql.setLength(0);
        sql.append("insert into t_transaction_");
        sql.append(nowYearMonth);
        sql.append(" (transaction_id, cust_id, type, pay_mode, meta_data, amount, remark, create_time, supplier_id, user_id, certificate)values(?,?,?,?,?,?,?,?,?,?,?)");
        String transactionId = Long.toString(IDHelper.getTransactionId());
        this.executeUpdateSQL(sql.toString(), new Object[]{transactionId, custId, type,
                payMode, metaData, Math.abs(amount), remark, new Timestamp(System.currentTimeMillis()), supplierId, userId, certificate});

        return true;
    }

    /**
     * 获取客户的消费总金额
     *
     * @param customerId
     * @param nowMonth
     * @return
     * @throws Exception
     */
    public double totalCustomerConsumptionAmount(String customerId, LocalDateTime nowMonth, int type) {
        double result = 0.0;
        try {
            // 检查交易记录月表是否存在,不存在则创建
            checkTransactionLogMonthTableNotExist(nowMonth.format(DateTimeFormatter.ofPattern("yyyyMM")));

            StringBuilder sql = new StringBuilder("SELECT FORMAT(sum(t.amount)/1000,2) as amount")
                    .append(" FROM t_transaction_" + nowMonth.format(DateTimeFormatter.ofPattern("yyyyMM")) + " t where cust_id = ?");
            if (type > 0) {
                sql.append(" and t.type=").append(type);
            }
            List<Map<String, Object>> list = this.sqlQuery(sql.toString(), customerId);
            if (list != null && list.size() > 0 && list.get(0).get("amount") != null) {
                result = NumberConvertUtil.parseDouble(String.valueOf(list.get(0).get("amount")));
            }
        } catch (Exception e) {
            logger.error("获取客户的消费总金额失败,", e);
            result = 0;
        }
        return result;
    }

    /**
     * 检查某个月份表是否存在,不存在则执行创建语句
     *
     * @param month
     * @return
     * @throws Exception
     */
    public int checkTransactionLogMonthTableNotExist(String month) throws Exception {
        StringBuffer sql = new StringBuffer();
        // 创建通话记录年月分表
        sql.append("create table IF NOT EXISTS t_transaction_");
        sql.append(month);
        sql.append(" like t_transaction");
        int code = this.executeUpdateSQL(sql.toString());
        return code;
    }

}
