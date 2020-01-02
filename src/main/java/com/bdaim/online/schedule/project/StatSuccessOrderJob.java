package com.bdaim.online.schedule.project;


import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.util.IDHelper;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

/**
 * 精准营销项目和职场成功单统计
 *
 * @author ningcheng
 * @date 2019/3/15 10:01
 */
@Component
public class StatSuccessOrderJob {

    private final static DateTimeFormatter YYYYMM = DateTimeFormatter.ofPattern("yyyyMM");

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 当日接通电话的客户群sql
     */
    public static final String NOW_DAY_CALL_CUST_GROUP_SQL = "SELECT customer_group_id FROM t_touch_voice_log_{0} WHERE create_time BETWEEN ? AND ? GROUP BY customer_group_id;";

    /**
     * 客户群ID 需求量 拨打量 项目ID 客群创建时间sql
     */
    public static final String CUST_GROUP_SQL = " SELECT t1.id, t1.cust_id, t1.market_project_id FROM customer_group t1 WHERE t1.id = ?;";

    /**
     * 客户下自建属性为邀约状态的自建属性ID
     */
    public static final String LABEL_ID_SQL = "SELECT label_id FROM t_customer_label WHERE label_name = ? AND cust_id = ?;";


    private final static String INSERT_COMMON_INFO = "INSERT INTO `t_common_info` (`id`, `service_code`, `service_desc`, `status`, `create_time`) VALUES (?, ?, ?, ?, ?);";

    private final static String INSERT_COMMON_PROPERTY_INFO = "INSERT INTO `t_common_info_property` (`zid`, `service_code`, `property_name`, `property_value`, `create_time`) VALUES (?, ?, ?, ?, ?);";

    private final static String UPDATE_COMMON_PROPERTY_INFO = "UPDATE `t_common_info_property` SET `property_value`=?, `create_time`=? WHERE `zid`=? AND `service_code`=? AND `property_name`=?;";

    /**
     * 根据service_code和property_name查询关联的zid
     */
    private final static String SELECT_COMMON_PROPERTY_INFO = "SELECT zid FROM t_common_info_property WHERE zid IN(SELECT id FROM t_common_info WHERE service_code =?) AND property_name= ? AND property_value = ? ";

    /**
     * 职场结算单管理业务code
     */
    private final static String JOB_SETTLEMENT_MANAGE_CODE = "10003";
    /**
     * 项目结算单业务code
     */
    private final static String PROJECT_SETTLEMENT_MANAGE_CODE = "10005";


    private final static String JOB_PROPERTY = "jobId";

    private final static String CUST_PROPERTY = "custId";
    /**
     * 标记量 适用于职场和项目
     */
    private final static String JOB_SIGN_NUM_PROPERTY = "jobSignNum";


    private final static String PROJECT_PROPERTY = "projectId";
    /**
     * 统计年月 201901
     */
    private final static String BILLDATE_PROPERTY_ID = "billDate";


    private static final Logger log = LoggerFactory.getLogger(StatSuccessOrderJob.class);

    @Autowired
    private MarketResourceDao marketResourceDao;

    /**
     * 执行job的方法
     */

    public void run(){
        log.info("精准营销职场和项目成功单标记数量统计job开始执行");
        try {
            this.execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
        log.info("精准营销职场和项目成功单标记数量统计job结束执行");
    }
    

    public int saveOrUpdateStatData(String billDate, long orderSum, String custId, String serviceCode, String propertyName, String propertyValue) throws Exception {
        log.info("================");
        log.info("统计参数billDate:" + billDate + ",orderSum:" + orderSum + ",custId:" + custId + ",serviceCode:" + serviceCode + ",propertyName:" + propertyName + ",propertyValue:" + propertyValue);
        int count = 0;
        Timestamp time = new Timestamp(System.currentTimeMillis());
        List<Map<String, Object>> list = marketResourceDao.sqlQuery(SELECT_COMMON_PROPERTY_INFO, serviceCode, propertyName, propertyValue);
        Map<String, Object> commData = null;
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                commData = marketResourceDao.queryUniqueSql("SELECT * FROM t_common_info_property WHERE zid = ? AND service_code = ? AND property_name= ? AND property_value = ? ", list.get(i).get("zid"), serviceCode, BILLDATE_PROPERTY_ID, billDate);
                if (commData != null && commData.size() > 0) {
                    break;
                }
            }
        }
        // 已存在则累计成功单
        if (commData != null && commData.size() > 0) {
            // 查询上次成功单数
            Map<String, Object> lastSuccessData = marketResourceDao.queryUniqueSql("SELECT * FROM t_common_info_property WHERE zid = ? AND service_code = ? AND property_name= ? ", commData.get("zid"), serviceCode, JOB_SIGN_NUM_PROPERTY);
            if (lastSuccessData != null && lastSuccessData.size() > 0) {
                orderSum += NumberConvertUtil.parseLong(lastSuccessData.get("property_value"));
                //`property_value`=?, `create_time`=? WHERE `zid`=? AND `service_code`=? AND `property_name`=?;";
                count += marketResourceDao.executeUpdateSQL(UPDATE_COMMON_PROPERTY_INFO, orderSum, time, commData.get("zid"), commData.get("service_code"), JOB_SIGN_NUM_PROPERTY);
            } else {
                count += marketResourceDao.executeUpdateSQL(INSERT_COMMON_PROPERTY_INFO, commData.get("zid"), serviceCode, JOB_SIGN_NUM_PROPERTY, orderSum, time);
            }
        } else {
            Long zid = IDHelper.getID();
            count += marketResourceDao.executeUpdateSQL(INSERT_COMMON_INFO, zid, serviceCode, null, null, time);
            count += marketResourceDao.executeUpdateSQL(INSERT_COMMON_PROPERTY_INFO, zid, serviceCode, BILLDATE_PROPERTY_ID, billDate, time);
            count += marketResourceDao.executeUpdateSQL(INSERT_COMMON_PROPERTY_INFO, zid, serviceCode, CUST_PROPERTY, custId, time);
            count += marketResourceDao.executeUpdateSQL(INSERT_COMMON_PROPERTY_INFO, zid, serviceCode, propertyName, propertyValue, time);
            count += marketResourceDao.executeUpdateSQL(INSERT_COMMON_PROPERTY_INFO, zid, serviceCode, JOB_SIGN_NUM_PROPERTY, orderSum, time);
        }
        log.info("统计保存状态:" + count);
        log.info("================");
        return count;
    }

    public void execute() throws Exception {
        LocalDateTime startTime = LocalDateTime.now().withHour(0).withMinute(0).withSecond(0);
        LocalDateTime endTime = LocalDateTime.now().withHour(23).withMinute(59).withSecond(59);
        List<Map<String, Object>> list, dayOrderList;
        Map<String, Object> custGroupMap, labelMap;
        String custId, customerGroupId, projectId, labelLike;
        long dayOrderCount;
        log.info("精准营销职场和项目成功单统计日期:" + startTime);
        list = marketResourceDao.sqlQuery(MessageFormat.format(NOW_DAY_CALL_CUST_GROUP_SQL, startTime.format(YYYYMM)), startTime.format(DATE_TIME_FORMATTER), endTime.format(DATE_TIME_FORMATTER));
        StringBuilder sql;
        //遍历今天拨打过电话的客户群的成功单
        for (int i = 0; i < list.size(); i++) {
            dayOrderCount = 0L;
            customerGroupId = String.valueOf(list.get(i).get("customer_group_id"));
            custGroupMap = marketResourceDao.queryUniqueSql(CUST_GROUP_SQL, customerGroupId);
            if (custGroupMap.size() == 0) {
                log.warn("客户群不存在,Id:" + customerGroupId);
                continue;
            }
            projectId = String.valueOf(custGroupMap.get("market_project_id"));
            custId = String.valueOf(custGroupMap.get("cust_id"));

            //查询客户下的自建属性
            labelMap = marketResourceDao.queryUniqueSql(LABEL_ID_SQL, "邀约状态", custId);
            if (labelMap.size() > 0) {
                labelLike = "%成功%";
                // 查询项目成单数量
                sql = new StringBuilder();
                sql.append("SELECT COUNT(DISTINCT(superid)) count ")
                        .append(" FROM t_touch_voice_log_" + startTime.format(YYYYMM) + " voice ")
                        .append(" JOIN t_customer_group_list_" + customerGroupId + " t ON t.id = voice.superid ")
                        .append(" WHERE voice.customer_group_id = ? ")
                        .append(" AND voice.create_time BETWEEN ? AND ?  ")
                        .append(" AND t.super_data LIKE ? ");
                try {
                    dayOrderList = marketResourceDao.sqlQuery(sql.toString(), customerGroupId, startTime, endTime, labelLike);
                    if (dayOrderList.size() > 0) {
                        for (Map<String, Object> o : dayOrderList) {
                            dayOrderCount += NumberConvertUtil.parseLong(o.get("count"));
                        }
                    }
                } catch (Exception e) {
                    log.error("查询今日项目成功单数失败,", e);
                }
                if (StringUtil.isNotEmpty(projectId)) {
                    // 保存项目成功单数
                    saveOrUpdateStatData(endTime.format(DateTimeFormatter.ofPattern("yyyyMM")), dayOrderCount, custId, PROJECT_SETTLEMENT_MANAGE_CODE, PROJECT_PROPERTY, projectId);
                } else {
                    log.warn("统计项目标记单项目ID为空:" + projectId);
                }

                // 查询职场成单数量
                sql = new StringBuilder();
                sql.append("SELECT COUNT(DISTINCT(superid)) count, voice.cug_id ")
                        .append(" FROM t_touch_voice_log_" + startTime.format(YYYYMM) + " voice ")
                        .append(" JOIN t_customer_group_list_" + customerGroupId + " t ON t.id = voice.superid ")
                        .append(" WHERE voice.customer_group_id = ? ")
                        .append(" AND voice.create_time BETWEEN ? AND ?  ")
                        .append(" AND t.super_data LIKE ? ")
                        .append(" GROUP BY voice.cug_id ");
                try {
                    dayOrderList = marketResourceDao.sqlQuery(sql.toString(), customerGroupId, startTime, endTime, labelLike);
                    if (dayOrderList.size() > 0) {
                        for (Map<String, Object> o : dayOrderList) {
                            if (o.get("cug_id") == null) {
                                log.warn("统计职场标记单职场ID为空:" + o.get("cug_id"));
                                continue;
                            }
                            if (StringUtil.isEmpty(String.valueOf(o.get("cug_id")))) {
                                log.warn("统计职场标记单职场ID为空:" + o.get("cug_id"));
                                continue;
                            }
                            // 保存职场成功单数
                            saveOrUpdateStatData(endTime.format(YYYYMM), NumberConvertUtil.parseLong(o.get("count")), custId, JOB_SETTLEMENT_MANAGE_CODE, JOB_PROPERTY, String.valueOf(o.get("cug_id")));
                        }
                    }
                } catch (Exception e) {
                    log.error("查询今日职场成功单数失败,", e);
                }
            }
        }
    }
}
