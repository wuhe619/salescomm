package com.bdaim.online.schedule.project;


import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 精准营销营销任务/坐席当天0点到23点59分59的坐席成单、坐席通话时长统计、呼叫数、接通数、其他原因数
 *
 * @author chengning@salescomm.net
 * @date 2019/4/28 10:29
 */
@Component
public class StatMarketTaskUserCallJob {



    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final static String STAT_TABLE_NAME = "stat_c_g_u_d";

    private final static String CALLED_INSERT_SQL = "INSERT INTO  " + STAT_TABLE_NAME + " (stat_time, customer_group_id, market_task_id, cust_id, user_id, caller_sum, called_sum, called_duration, order_sum, other_sum, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private final static String CALLED_UPDATE_SQL = "UPDATE " + STAT_TABLE_NAME + " SET stat_time=?, customer_group_id=?, market_task_id = ? ,cust_id=?, user_id=?, caller_sum = ?, called_sum=?, called_duration=?, order_sum=?, other_sum= ? WHERE customer_group_id=? AND market_task_id = ? AND user_id= ? AND create_time= ? ";

    private final static String SELECT_SQL = "SELECT stat_time FROM " + STAT_TABLE_NAME + " WHERE customer_group_id = ? AND market_task_id = ? AND user_id= ? AND create_time = ?";

    /**
     * 当日接通电话的营销任务sql
     */
    public static final String NOW_DAY_CALL_CG_VOICE_SQL = "SELECT user_id, cust_id, customer_group_id, market_task_id,  " +
            " COUNT(*) caller_sum, " +
            " IFNULL(SUM(IF(`status` = 1001, `called_duration`, 0)), 0) called_duration, " +
            " count(`status` = 1001 OR null) AS called_sum,  " +
            " count(`status` = 1002 OR null) AS other_sum " +
            " FROM t_touch_voice_log_{0} " +
            " WHERE create_time BETWEEN ? AND ? " +
            " GROUP BY user_id, customer_group_id, market_task_id ";

    /**
     * 客户下自建属性为邀约状态的自建属性ID
     */
    public static final String LABEL_ID_SQL = "SELECT label_id FROM t_customer_label WHERE label_name = ? AND cust_id = ?;";

    /**
     * 营销任务ID 需求量 拨打量 项目ID 客群创建时间sql
     */
    public static final String MARKET_TASK_SQL = " SELECT t1.id, t1.customer_group_id, t1.cust_id, t1.task_type FROM t_market_task t1 WHERE t1.id = ?;";

    private static final Logger log = LoggerFactory.getLogger(StatMarketTaskUserCallJob.class);
    
    @Autowired
    private MarketResourceDao marketResourceDao;
    

    /**
     * 执行job的方法
     */
//    @Scheduled(cron = "0 0/5 * * * ? ")
    public void run() {
        log.info("精准营销营销任务用户呼叫数据统计job开始执行");
        try {
            this.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("精准营销营销任务用户呼叫数据统计job结束执行");
    }
    

    public void execute() throws Exception {
        DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");
        LocalDateTime nowTime = LocalDateTime.now();
        LocalDateTime startTime = nowTime.withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endTime = nowTime.withHour(23).withMinute(59).withSecond(59);

        Map<String, Object> marketTaskMap;
        int taskType;
        List<Map<String, Object>> cgList, orderList = null;
        String custId, customerGroupId, marketTaskId, userId, labelLike = "%成功%";
        StringBuilder sql;
        long callerCount, calledCount, calledDuration, otherSum, successOrderCount;
        log.info("精准营销营销任务用户呼叫数据统计日期:" + startTime);
        // 查询今日呼叫过的营销任务列表
        cgList = marketResourceDao.sqlQuery(MessageFormat.format(NOW_DAY_CALL_CG_VOICE_SQL, startTime.format(YYYYMM)), startTime.format(DATE_TIME_FORMATTER), endTime.format(DATE_TIME_FORMATTER));
        for (Map<String, Object> m : cgList) {
            successOrderCount = 0L;
            custId = String.valueOf(m.get("cust_id"));
            customerGroupId = String.valueOf(m.get("customer_group_id"));
            marketTaskId = String.valueOf(m.get("market_task_id"));

            marketTaskMap = marketResourceDao.queryUniqueSql(MARKET_TASK_SQL, marketTaskId);
            if (marketTaskMap.size() == 0) {
                log.warn("营销任务不存在,Id:" + marketTaskId);
                continue;
            }
            // 查询任务类型
            taskType = NumberConvertUtil.parseInt(marketTaskMap.get("task_type"));

            userId = String.valueOf(m.get("user_id"));
            if (StringUtil.isEmpty(userId)) {
                log.warn("用户ID为空,userId:" + userId);
                userId = "";
            }
            // 呼叫量
            callerCount = NumberConvertUtil.parseLong(m.get("caller_sum"));
            // 接通量
            calledCount = NumberConvertUtil.parseLong(m.get("called_sum"));
            // 通话时长
            calledDuration = NumberConvertUtil.parseLong(m.get("called_duration"));
            // 失败数据
            otherSum = NumberConvertUtil.parseLong(m.get("other_sum"));

            //处理成功单
            sql = new StringBuilder();
            if (3 == taskType) {
                // 查询机器人意向度为A或者审核通过的话单
                sql.append("SELECT COUNT(*) count ")
                        .append(" FROM t_touch_voice_log_" + startTime.format(YYYYMM) + " voice ")
                        .append(" JOIN t_market_task_list_" + marketTaskId + " t ON t.id = voice.superid ")
                        .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? AND voice.market_task_id = ?")
                        .append(" AND voice.create_time BETWEEN ? AND ?  ")
                        .append(" AND voice.user_id = ? ")
                        .append(" AND (t.intent_level ='A' OR voice.clue_audit_status = 2) ")
                        .append(" GROUP BY voice.market_task_id, voice.superid ");
                // 今日用户拨打成单数
                try {
                    orderList = marketResourceDao.sqlQuery(sql.toString(), custId, customerGroupId, marketTaskId, startTime, endTime, userId);
                } catch (Exception e) {
                    log.error("查询机器人今日营销任务意向A并且审核通过数失败,", e);
                }
            } else {
                // 获取邀约成功,拨打电话成功用户的通话记录
                sql.append("SELECT COUNT(*) count ")
                        .append(" FROM t_touch_voice_log_" + startTime.format(YYYYMM) + " voice ")
                        .append(" JOIN t_market_task_list_" + marketTaskId + " t ON t.id = voice.superid ")
                        .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? AND voice.market_task_id = ? ")
                        .append(" AND voice.create_time BETWEEN ? AND ?  ")
                        .append(" AND voice.user_id = ? ")
                        .append(" AND t.super_data LIKE ? ")
                        .append(" GROUP BY voice.market_task_id, voice.superid ");
                // 今日用户拨打成单数
                try {
                    orderList = marketResourceDao.sqlQuery(sql.toString(), custId, customerGroupId, marketTaskId, startTime, endTime, userId, labelLike);
                } catch (Exception e) {
                    log.error("查询坐席今日用户成单数失败,", e);
                }
            }
            if (orderList != null && orderList.size() > 0) {
                for (int i = 0; i < orderList.size(); i++) {
                    // 一个身份ID只计算一个成功单
                    if (NumberConvertUtil.parseLong(orderList.get(i).get("count")) > 0) {
                        successOrderCount++;
                    }
                }
            }
            // 保存或者更新统计数据
            saveOrUpdateByCalledData(nowTime, custId, customerGroupId, marketTaskId, userId, calledDuration, successOrderCount, callerCount, calledCount, otherSum);
        }
    }


    public int saveOrUpdateByCalledData(LocalDateTime statTime, String custId, String customerGroupId, String marketTaskId, String userId,
                                        long calledDuration, long orderSum, long callSum, long calledSum, long otherSum) throws Exception {
        log.info("统计参数statTime:" + statTime + ",custId:" + custId + ",customerGroupId:" + customerGroupId + ",marketTaskId:" + marketTaskId +
                ",userId:" + userId + ",calledDuration:" + calledDuration + ",orderSum:" + orderSum +
                ",otherSum:" + otherSum + ",callSum:" + callSum + ",calledSum:" + calledSum);
        int count = 0;
        String createTime = statTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        Map<String, Object> map = marketResourceDao.queryUniqueSql(SELECT_SQL, customerGroupId, marketTaskId, userId, createTime);
        if (map != null && map.size() > 0) {
            //stat_time=?, customer_group_id=?, cust_id=?, user_id=?, caller_sum = ?, called_sum=?, called_duration=?, order_sum=?, other_sum= ? WHERE customer_group_id=? AND user_id= ? AND create_time= ?
            count = marketResourceDao.executeUpdateSQL(CALLED_UPDATE_SQL, statTime.format(DATE_TIME_FORMATTER), customerGroupId, marketTaskId, custId, userId, callSum, calledSum, calledDuration, orderSum, otherSum, customerGroupId, marketTaskId, userId, createTime);
        } else {
            //(stat_time, customer_group_id, cust_id, user_id, caller_sum, called_sum, called_duration, order_sum, other_sum, create_time) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
            count = marketResourceDao.executeUpdateSQL(CALLED_INSERT_SQL, statTime.format(DATE_TIME_FORMATTER), customerGroupId, marketTaskId, custId, userId, callSum, calledSum, calledDuration, orderSum, otherSum, createTime);
        }
        log.info("营销任务统计保存状态:" + count);
        return count;
    }
    

}
