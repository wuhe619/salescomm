package com.bdaim.online.schedule.project;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.text.MessageFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author ningmeng
 * @date 2019/12/29
 * @description
 */
@Component
public class StatLabelDataDay {


    String table_name = "stat_u_label_data";
    String insert_sql = "INSERT INTO " + table_name + " (`stat_time`, `user_id`, `cust_id`, `customer_group_id`, `market_task_id`, `label_id`, `option_value`, `tag_sum`, `create_time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?);";
    String update_sql = "UPDATE " + table_name + " SET `stat_time`=?, `user_id`=?, `cust_id`=?, `customer_group_id`=?, `market_task_id`=?, `label_id`=?, `option_value`=?, `tag_sum`=?, `create_time`=? WHERE create_time = ? AND user_id = ? AND market_task_id = ? AND `label_id`=? AND `option_value`=? ";
    String select_sql = "SELECT * FROM " + table_name + " WHERE create_time = ? AND user_id = ? AND market_task_id = ? AND `label_id`=? AND `option_value`=? AND customer_group_id = ?";
    String now_day_call_sql = "SELECT user_id, cust_id, customer_group_id, market_task_id FROM t_touch_voice_log_{yyyy_mm} WHERE create_time BETWEEN ? AND ? GROUP BY user_id, customer_group_id, market_task_id ";
    String label_sql = "SELECT label_id FROM t_customer_label WHERE type = 2 ";
    String super_data_sql = "SELECT t.id, t.super_data FROM t_market_task_list_{0} t INNER JOIN t_touch_voice_log_{1} log ON t.id = log.superid WHERE t.super_data IS NOT NULL AND log.user_id = ? AND log.cust_id = ? AND log.customer_group_id = ? AND log.market_task_id = ? AND log.create_time BETWEEN ? AND ? ";

    private static final Logger log = LoggerFactory.getLogger(StatLabelDataDay.class);

    private final static DateTimeFormatter YYYY_MM = DateTimeFormatter.ofPattern("yyyyMM");

    private static Set<String> single_option = new HashSet<>();

    @Autowired
    private MarketResourceDao marketResourceDao;


    public void init() {
        List<Map<String, Object>> maps = marketResourceDao.sqlQuery(label_sql);
        for (Map<String, Object> m : maps) {
            single_option.add(String.valueOf(m.get("label_id")));
        }
        String yyyy_mm = DateTimeFormatter.ofPattern("yyyyMM").format(LocalDateTime.now());
        now_day_call_sql = now_day_call_sql.replace("{yyyy_mm}", yyyy_mm);
    }

    /**
     * 执行job的方法
     */
    public void execute() {
        init();

        LocalDateTime now = LocalDateTime.now();
        String start_time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
        String end_time = now.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        log.info("统计日期:" + start_time);
        // 查询今日呼叫过的营销任务列表
        List<Map<String, Object>> call_list = marketResourceDao.sqlQuery(now_day_call_sql, start_time, end_time);
        if (call_list.size() == 0) {
            log.info("营销任务今日无外呼数据");
            return;
        }
        //遍历今日拨打过电话的营销任务
        for (Map<String, Object> c : call_list) {
            String user_id = String.valueOf(c.get("user_id"));
            String c_id = String.valueOf(c.get("cust_id"));
            String customer_group_id = String.valueOf(c.get("customer_group_id"));
            String market_task_id = String.valueOf(c.get("market_task_id"));
            if (StringUtil.isEmpty(user_id)) {
                log.warn("user_id is None, continue");
                continue;
            }
            if (StringUtil.isEmpty(market_task_id)) {
                log.warn("market_task_id is None, continue");
                continue;
            }
            if (StringUtil.isEmpty(customer_group_id)) {
                log.warn("customer_group_id is None, continue");
                continue;
            }

            // 用户营销任务自建属性标记数据
            try {
                List<Map<String, Object>> super_data = marketResourceDao.sqlQuery(
                        MessageFormat.format(super_data_sql, market_task_id, now.format(YYYY_MM))
                        , user_id, c_id, customer_group_id, market_task_id, start_time, end_time);

                Map<String, Integer> tagData = new HashMap<>();
                for (Map<String, Object> s : super_data) {
                    String superData = String.valueOf(s.get("super_data"));
                    if (StringUtil.isNotEmpty(superData)) {
                        JSONObject jsonObject = JSON.parseObject(superData);
                        for (Map.Entry m : jsonObject.entrySet()) {
                            if (single_option.contains(String.valueOf(m.getKey()))) {
                                String key = (m.getKey() + ":" + m.getValue());
                                if (tagData.containsKey(key)) {
                                    tagData.put(key, tagData.get(key) + 1);
                                } else {
                                    tagData.put(key, 1);
                                }

                            }
                        }
                    }
                }
                // 保存统计数据
                for (Map.Entry<String, Integer> m : tagData.entrySet()) {
                    int tag_num = m.getValue();
                    String[] key = m.getKey().split(":");
                    String label_id = key[0];
                    String option_value = key[1];
                    save_stat_data(now, user_id, c_id, customer_group_id, market_task_id, label_id, option_value,
                            tag_num);
                }
            } catch (Exception e) {
                log.error("query super_data is error, continue", e);
                continue;
            }
        }

    }


    public void save_stat_data(LocalDateTime now_time, String user_id, String c_id,
                               String customer_group_id, String market_task_id, String label_id, String option_value, int tag_num) {
        String create_time = now_time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        String stat_time = now_time.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        List<Map<String, Object>> data = marketResourceDao.sqlQuery(
                select_sql, create_time, user_id, market_task_id, label_id,
                option_value, customer_group_id);
        if (data.size() > 0) {
            log.info(
                    "更新统计参数->now_time:{},user_id:{},c_id:{},customer_group_id:{},market_task_id:{},label_id:{},option_value:{},tag_num:{}",
                    stat_time, user_id, c_id, customer_group_id, market_task_id, label_id, option_value, tag_num);
            // 更新
            marketResourceDao.executeUpdateSQL(update_sql,
                    stat_time, user_id, c_id, customer_group_id, market_task_id,
                    label_id, option_value, tag_num, create_time,
                    create_time, user_id, market_task_id, label_id,
                    option_value);
        } else {
            log.info(
                    "插入统计参数->now_time:{},user_id:{},c_id:{},customer_group_id:{},market_task_id:{},label_id:{},option_value:{},tag_num:{}"
                    , now_time, user_id, c_id, customer_group_id, market_task_id, label_id, option_value, tag_num);
            marketResourceDao.executeUpdateSQL(insert_sql,
                    stat_time, user_id, c_id, customer_group_id, market_task_id,
                    label_id, option_value, tag_num, create_time);

        }
        log.info("保存统计数据成功");
    }
}
