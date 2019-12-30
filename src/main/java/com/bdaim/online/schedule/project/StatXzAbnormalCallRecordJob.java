package com.bdaim.online.schedule.project;


import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.dto.SeatCallCenterConfig;
import com.bdaim.callcenter.service.impl.XzCallCenterService;
import com.bdaim.customersea.dao.CustomerSeaDao;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.online.schedule.dto.XzAbnormalCallRecord;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.SaleApiUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 统计讯众异常话单数量至 stat_c_g_u_d
 *
 * @author chengning@salescomm.net
 * @date 2019/7/10 17:24
 */
@Component
public class StatXzAbnormalCallRecordJob {

    private final static String LAST_TIME_SUFFIX = "_xzLastTaskTimeQueue";

    private final static String STAT_TABLE_NAME = "stat_c_g_u_d";

    private final static String INSERT_SQL = "INSERT INTO  " + STAT_TABLE_NAME + " (stat_time, customer_group_id, market_task_id, cust_id, user_id, caller_sum, called_sum,  busy_sum, no_service_area_sum, phone_overdue_sum, phone_shutdown_sum, space_phone_sum, other_sum, create_time, customer_sea_id) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";

    private final static String UPDATE_SQL = "UPDATE " + STAT_TABLE_NAME + " SET stat_time=?, customer_group_id=?, market_task_id = ? ,cust_id=?, user_id=?, caller_sum = IFNULL(caller_sum,0) +?,called_sum = IFNULL(called_sum,0) +?, busy_sum= IFNULL(busy_sum,0) +?, no_service_area_sum= IFNULL(no_service_area_sum,0) +?, " +
            "phone_overdue_sum=IFNULL(phone_overdue_sum,0) +?, phone_shutdown_sum=IFNULL(phone_shutdown_sum,0) +?, space_phone_sum=IFNULL(space_phone_sum,0) +?, other_sum=IFNULL(other_sum,0) +? WHERE customer_group_id=? AND user_id= ? AND create_time= ? AND market_task_id = ? AND customer_sea_id = ? ";

    private final static String SELECT_SQL = "SELECT stat_time FROM " + STAT_TABLE_NAME + " WHERE customer_group_id = ? AND user_id= ? AND create_time = ? AND market_task_id = ? AND customer_sea_id = ?";

    public static final String MARKET_SOURCE_ID = "1";

    private static final Logger log = LoggerFactory.getLogger(StatXzAbnormalCallRecordJob.class);

    @Autowired
    private MarketResourceDao marketResourceDao;
    @Autowired
    private XzCallCenterService xzCallCenterService;
    @Autowired
    private CustomerSeaDao customerSeaDao;


    /**
     * 执行job的方法
     */
    public void run() {
        log.info("统计讯众异常话单数量开始执行");
        this.execute();
        log.info("统计讯众异常话单数量结束执行");
    }


    public String getXzLastCallId(String callCenterId, String sourceId) throws Exception {
        List<Map<String, Object>> list = marketResourceDao.sqlQuery("SELECT * FROM t_source_property WHERE source_id = ? AND property_key = ?", sourceId, callCenterId + LAST_TIME_SUFFIX);
        String lastTaskTime = null;
        if (list != null && list.size() > 0) {
            lastTaskTime = String.valueOf(list.get(0).get("property_value"));
        }
        log.info("企业ID:" + callCenterId + ",sourceId:" + sourceId + "返回任务的最大CallId:" + lastTaskTime);
        return lastTaskTime;
    }

    private Map<String, Object> selectSeaData(String seaId, String superId) {
        try {
            List<Map<String, Object>> list = marketResourceDao.sqlQuery("SELECT id, batch_id FROM t_customer_sea_list_" + seaId + " WHERE id=?", superId);
            if (list == null || list.size() == 0) {
                return Collections.emptyMap();
            }
            Map<String, Object> m = list.get(0);
            return m;
        } catch (Exception e) {
            log.error("查询公海线索详情异常", e);
        }
        return null;
    }

    /**
     * 拉取讯众呼叫中心话单结果保存至数据库
     */
    public void execute() {
        try {
            LocalDateTime statTime = LocalDateTime.now(), startTime;
            Map<String, String> taskIdCustData = Collections.synchronizedMap(new HashMap<>());
            Map<String, Long> custGroupCallData = Collections.synchronizedMap(new HashMap<>());
            Map<String, String> taskIdSeaData = Collections.synchronizedMap(new HashMap<>());
            Map<String, Long> seaCallData = Collections.synchronizedMap(new HashMap<>());
            // 获取配置了新版呼叫渠道信息
            List<SeatCallCenterConfig> callConfigs = marketResourceDao.listAllResourceCallConfigs(1, 2);
            // 处理通话数据
            String maxCallId, result, customerGroupId, marketTaskId, callId, custId = null, dataKey, seaId = null;
            int flag = 0, updateTaskLastTimeStatus;
            JSONObject jsonObject;
            List<XzAbnormalCallRecord> list;
            CustomerSea cacheSeaInfo;
            Map<String, Object> customGroupData;
            for (SeatCallCenterConfig c : callConfigs) {
                long autoId;
                if (StringUtil.isEmpty(c.getCallCenterId())) {
                    log.warn("呼叫中心企业ID为空:" + c.getCallCenterId());
                    continue;
                }
                // 获取上次最大的callId
                maxCallId = getXzLastCallId(c.getCallCenterId(), MARKET_SOURCE_ID);
                if (StringUtil.isEmpty(maxCallId)) {
                    maxCallId = "0";
                }
                if (NumberConvertUtil.parseLong(maxCallId) < 22245026L) {
                    maxCallId = "22245026";
                }
                log.info("呼叫中心:" + c.getCallCenterId() + ",maxCallId:" + maxCallId);
                result = SaleApiUtil.getCdrAnyCallRecord(1, 2, c.getCallCenterId(), maxCallId, String.valueOf(SaleApiUtil.XZ_PULL_LIMIT), "", "");
                log.info("呼叫中心:" + c.getCallCenterId() + ",获取呼叫中心话单接口返回数据:" + result);
                autoId = NumberConvertUtil.parseLong(maxCallId);
                if (StringUtil.isNotEmpty(result)) {
                    jsonObject = JSON.parseObject(result);
                    if (jsonObject.getIntValue("total") == 0) {
                        log.warn("呼叫中心:" + c.getCallCenterId() + ",maxCallId:" + maxCallId + ",话单数据为空");
                        continue;
                    }
                    list = JSON.parseArray(jsonObject.getString("data"), XzAbnormalCallRecord.class);
                    // 处理通话记录
                    for (XzAbnormalCallRecord dto : list) {
                        autoId = dto.getId();
                        // 只处理自动外呼
                        if (dto.getCalltype() != 2) {
                            continue;
                        }
                        if (dto.getCdrtype() != 2) {
                            continue;
                        }
                        if (dto.getGrouptype() != 2) {
                            continue;
                        }
                        if (StringUtil.isEmpty(dto.getRequestid())) {
                            log.warn("callId:[" + dto.getCallid() + "]随路参数为空,跳过");
                            continue;
                        }
                        // 通话状态
                        int callStatus = SaleApiUtil.judgeXZCallStatus(dto);
                        if (callStatus == 0) {
                            continue;
                        }
                        callId = dto.getCallid();
                        boolean sea = false;
                        if (taskIdSeaData.get(dto.getTaskId()) == null) {
                            cacheSeaInfo = customerSeaDao.getCustomerSeaByTaskId(dto.getTaskId());
                            if (cacheSeaInfo != null) {
                                seaId = cacheSeaInfo.getId().toString();
                                // 营销任务所属的客户ID
                                custId = String.valueOf(cacheSeaInfo.getCustId());
                                taskIdSeaData.put(dto.getTaskId(), seaId + "|" + custId);
                                sea = true;
                            } else {
                                log.warn("callId:" + callId + "通话状态:" + callStatus + ",taskId:" + dto.getTaskId() + "未查询到对应的公海");
                            }
                        } else {
                            seaId = String.valueOf(taskIdSeaData.get(dto.getTaskId()).split("\\|")[0]);
                            custId = String.valueOf(taskIdSeaData.get(dto.getTaskId()).split("\\|")[1]);
                            sea = true;
                        }
                        if (StringUtil.isNotEmpty(c.getCustId()) && StringUtil.isNotEmpty(custId) && !custId.equals(c.getCustId())) {
                            log.warn("配置了相同呼叫中心ID:" + c.getCallCenterId() + ",呼叫中心custId:" + c.getCustId() + ",呼叫记录custId:" + custId + "话单不属于非本客户,跳过");
                            continue;
                        }
                        log.info("callId:" + callId + "未接通通话状态:" + callStatus);
                        if (sea) {
                            try {
                                startTime = LocalDateTime.parse(dto.getStime(), DateTimeFormatter.ofPattern("M/d/yyyy HH:mm:ss"));
                            } catch (Exception e) {
                                log.warn("呼叫开始时间转换异常,", e);
                                startTime = LocalDateTime.now();
                            }
                            customerSeaDao.updateCustomSeaCallCount(startTime, seaId, dto.getRequestid(), SaleApiUtil.judgeXZCallStatus(dto), dto.getDuration_time());
                            // 缓存客户-客户群未接通数据
                            Map<String, Object> data = selectSeaData(seaId, dto.getRequestid());
                            if (data != null && data.size() > 0) {
                                customerGroupId = String.valueOf(data.get("batch_id"));
                            } else {
                                customerGroupId = "0";
                            }
                            dataKey = seaId + "|" + custId + "|" + customerGroupId + "|" + callStatus;
                            if (seaCallData.get(dataKey) == null) {
                                seaCallData.put(dataKey, 1L);
                            } else {
                                seaCallData.put(dataKey, seaCallData.get(dataKey) + 1L);
                            }
                        } else {
                            // 缓存taskId和客户的对应关系
                            if (taskIdCustData.get(dto.getTaskId()) == null) {
                                customGroupData = xzCallCenterService.getMarketTaskInfoByXZTaskIdPrd(dto);
                                if (customGroupData != null && customGroupData.size() > 0) {
                                    customerGroupId = String.valueOf(customGroupData.get("customer_group_id"));
                                    marketTaskId = String.valueOf(customGroupData.get("market_task_id"));
                                    // 营销任务所属的客户ID
                                    custId = String.valueOf(customGroupData.get("cust_id"));
                                    taskIdCustData.put(dto.getTaskId(), customerGroupId + "|" + custId + "|" + marketTaskId);
                                } else {
                                    log.warn("callId:" + callId + "通话状态:" + callStatus + ",taskId:" + dto.getTaskId() + "未查询到对应的客户群或者营销任务");
                                    continue;
                                }
                            } else {
                                customerGroupId = String.valueOf(taskIdCustData.get(dto.getTaskId()).split("\\|")[0]);
                                custId = String.valueOf(taskIdCustData.get(dto.getTaskId()).split("\\|")[1]);
                                marketTaskId = String.valueOf(taskIdCustData.get(dto.getTaskId()).split("\\|")[2]);
                            }
                            if (StringUtil.isNotEmpty(c.getCustId()) && !custId.equals(c.getCustId())) {
                                log.warn("配置了相同呼叫中心ID:" + c.getCallCenterId() + ",呼叫中心custId:" + c.getCustId() + ",呼叫记录custId:" + custId + "话单不属于非本客户,跳过");
                                continue;
                            }
                            log.info("callId:[" + callId + "]未接通通话状态:" + callStatus);
                            // 缓存客户-客户群未接通数据
                            dataKey = customerGroupId + "|" + marketTaskId + "|" + custId + "|" + callStatus;
                            if (custGroupCallData.get(dataKey) == null) {
                                custGroupCallData.put(dataKey, 1L);
                            } else {
                                custGroupCallData.put(dataKey, custGroupCallData.get(dataKey) + 1L);
                            }
                        }
                        flag++;
                        /*// 更新任务的最后执行时间
                        updateTaskLastTimeStatus = updateXzLastCallId(c.getCallCenterId(), Constant.MARKET_SOURCE_ID, String.valueOf(dto.getId()), log);
                        log.info("讯众企业ID:[" + c.getCallCenterId() + "]任务更新最后ID:[" + dto.getId() + "]状态:" + updateTaskLastTimeStatus);*/
                    }
                }
                // 更新任务的最后执行时间
                updateTaskLastTimeStatus = updateXzLastCallId(c.getCallCenterId(), MARKET_SOURCE_ID, String.valueOf(autoId));
                log.info("讯众企业ID:[" + c.getCallCenterId() + "]任务更新最后ID:[" + autoId + "]状态:" + updateTaskLastTimeStatus);
            }
            // 处理客群和营销任务统计数据
            for (Map.Entry<String, Long> m : custGroupCallData.entrySet()) {
                //customerGroupId + "|" + marketTaskId + "|" + custId + "|" + callStatus;
                updateVolceStatus(statTime, m.getKey().split("\\|")[2], m.getKey().split("\\|")[0],
                        m.getKey().split("\\|")[1], "",
                        NumberConvertUtil.parseInt(m.getKey().split("\\|")[3]), m.getValue(), "");
            }
            // 公海统计数据
            for (Map.Entry<String, Long> m : seaCallData.entrySet()) {
                //seaId + "|" + custId + "|" + customerGroupId + "|" + callStatus
                updateVolceStatus(statTime, m.getKey().split("\\|")[1], m.getKey().split("\\|")[2],
                        "", "",
                        NumberConvertUtil.parseInt(m.getKey().split("\\|")[3]), m.getValue(), m.getKey().split("\\|")[0]);
            }
            log.info("统计讯众未通话成功话单数量条数:" + flag);
        } catch (Exception e) {
            log.error("统计讯众未通话成功话单统计异常:", e);
        }
    }

    /**
     * 更新务的最大CallId
     *
     * @param callCenterId
     * @param sourceId
     * @param callId
     * @return
     * @throws Exception
     */
    public int updateXzLastCallId(String callCenterId, String sourceId, String callId) throws Exception {
        int code = 0;
        String result = getXzLastCallId(callCenterId, sourceId);
        if (result != null) {
            code = marketResourceDao.executeUpdateSQL("UPDATE t_source_property SET property_value = ?  WHERE source_id = ? AND property_key = ?", callId, sourceId, callCenterId + LAST_TIME_SUFFIX);
            log.info("企业ID:" + callCenterId + ",sourceId:" + sourceId + ",callId:" + callId + "更新任务的最大CallId状态:" + code);
        } else {
            code = marketResourceDao.executeUpdateSQL("INSERT INTO `t_source_property` (`source_id`,`property_key`, `property_value`) VALUES (?,?,?);", sourceId, callCenterId + LAST_TIME_SUFFIX, callId);
            log.info("企业ID:" + callCenterId + ",sourceId:" + sourceId + ",callId:" + callId + "插入任务的最大CallId状态:" + code);
        }

        return code;
    }

    public void updateVolceStatus(LocalDateTime statTime, String custId, String customerGroupId, String marketTaskId, String userId, int callStatus, long count, String seaId) {
        try {
            if (callStatus == 1001) {
                saveOrUpdateStatData(statTime, custId, customerGroupId, marketTaskId, userId, count, count, 0, 0, 0, 0, 0, 0, seaId);
            } else if (callStatus == 1002) {
                saveOrUpdateStatData(statTime, custId, customerGroupId, marketTaskId, userId, count, 0, 0, 0, 0, 0, 0, count, seaId);
            } else if (callStatus == 1003) {
                saveOrUpdateStatData(statTime, custId, customerGroupId, marketTaskId, userId, count, 0, 0, count, 0, 0, 0, 0, seaId);
            } else if (callStatus == 1004) {
                saveOrUpdateStatData(statTime, custId, customerGroupId, marketTaskId, userId, count, 0, 0, 0, 0, count, 0, 0, seaId);
            } else if (callStatus == 1005) {
                saveOrUpdateStatData(statTime, custId, customerGroupId, marketTaskId, userId, count, 0, 0, 0, 0, 0, count, 0, seaId);
            } else if (callStatus == 1006) {
                saveOrUpdateStatData(statTime, custId, customerGroupId, marketTaskId, userId, count, 0, 0, 0, count, 0, 0, 0, seaId);
            } else if (callStatus == 1007) {
                saveOrUpdateStatData(statTime, custId, customerGroupId, marketTaskId, userId, count, 0, count, 0, 0, 0, 0, 0, seaId);
            } else if (callStatus == 1008) {
                saveOrUpdateStatData(statTime, custId, customerGroupId, marketTaskId, userId, count, 0, 0, 0, 0, 0, 0, count, seaId);
            }
        } catch (Exception e) {
            log.error("更新未接通号码计数异常", e);
        }
    }


    public int saveOrUpdateStatData(LocalDateTime statTime, String custId, String customerGroupId, String marketTaskId, String userId, long callNum, long calledNum, long busySum, long noServiceAreaSum, long overdueSum, long shutdownSum,
                                    long spacePhoneSum, long otherSum, String seaId) throws Exception {
        log.info("统计参数statTime:" + statTime + ",custId:" + custId + ",customerGroupId:" + customerGroupId + ",marketTaskId:" + marketTaskId + ",userId:" + userId + ",callNum:" + callNum + ",calledNum:" + calledNum + ",busySum:" + busySum + ",noServiceAreaSum:" + noServiceAreaSum + ",overdueSum:" + overdueSum
                + ",shutdownSum:" + shutdownSum + ",spacePhoneSum:" + spacePhoneSum + ",otherSum:" + otherSum + ",seaId:" + seaId);
        int count = 0;
        String createTime = statTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        List<Map<String, Object>> map = marketResourceDao.sqlQuery(SELECT_SQL, customerGroupId, userId, createTime, marketTaskId, seaId);
        if (map != null && map.size() > 0) {
            //stat_time=?, customer_group_id=?, cust_id=?, user_id=?, caller_sum=?, called_sum=?, called_duration=?, order_sum=?, busy_sum= ?, no_service_area_sum=?, phone_overdue_sum=?, phone_shutdown_sum=?, space_phone_sum=?, other_sum=? WHERE customer_group_id=? AND user_id= ? AND create_time= ? AND market_task_id = ? ";
            count = marketResourceDao.executeUpdateSQL(UPDATE_SQL, statTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), customerGroupId, marketTaskId, custId, userId, callNum, calledNum, busySum, noServiceAreaSum, overdueSum, shutdownSum, spacePhoneSum, otherSum, customerGroupId, userId, createTime, marketTaskId, seaId);
        } else {
            //stat_time, customer_group_id, cust_id, user_id, caller_sum, called_sum, called_duration, order_sum, busy_sum, no_service_area_sum, phone_overdue_sum, phone_shutdown_sum, space_phone_sum, other_sum, create_time
            count = marketResourceDao.executeUpdateSQL(INSERT_SQL, statTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), customerGroupId, marketTaskId, custId, userId, callNum, calledNum, busySum, noServiceAreaSum, overdueSum, shutdownSum, spacePhoneSum, otherSum, createTime, seaId);
        }
        log.info("统计保存状态:" + count);
        return count;
    }


}
