package com.bdaim.smscenter.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.smscenter.entity.TouchSmsQueue;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/7/4
 * @description
 */
@Component
public class SmsDao extends SimpleHibernateDao {

    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * 批量插入短信队列表
     *
     * @param list
     * @return
     */
    public int batchSaveSmsQueue(List<TouchSmsQueue> list) {
        final String sql = "INSERT INTO `t_touch_sms_queue` (`template_id`, `cust_id`, `customer_group_id`, `superid`, `create_time`, batch_number, user_id, market_task_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
        int[] ints = null;
        try {
            ints = jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    preparedStatement.setInt(1, list.get(i).getTemplateId());
                    preparedStatement.setString(2, list.get(i).getCustId());
                    preparedStatement.setInt(3, list.get(i).getCustomerGroupId());
                    preparedStatement.setString(4, list.get(i).getSuperid());
                    preparedStatement.setTimestamp(5, list.get(i).getCreateTime());
                    preparedStatement.setString(6, list.get(i).getBatchNumber());
                    preparedStatement.setString(7, list.get(i).getUserId());
                    preparedStatement.setString(8, list.get(i).getMarketTaskId());
                }

                @Override
                public int getBatchSize() {
                    return list.size();
                }
            });
        } catch (DataAccessException e) {
            logger.error("批量保存至短信队列表失败,", e);
        }
        return ints != null ? ints.length : 0;
    }

}
