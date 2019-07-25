package com.bdaim.account.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.StringUtil;
import com.bdaim.slxf.entity.TransactionDO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.text.DecimalFormat;
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

}
