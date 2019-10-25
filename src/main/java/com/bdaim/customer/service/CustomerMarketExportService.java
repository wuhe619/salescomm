package com.bdaim.customer.service;

import com.bdaim.customer.dao.CustomerMarketExportDao;
import com.bdaim.customer.dto.CustomerMarketExport;
import com.bdaim.customer.entity.CustomerMarketExportDO;
import com.bdaim.util.StringUtil;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;


/**
 * @author chengning@salescomm.net
 * @date 2018/9/4
 * @description
 */
@Service("customerMarketExportService")
@Transactional
public class CustomerMarketExportService {

    @Resource
    private CustomerMarketExportDao customerMarketExportDao;
    @Resource
    private JdbcTemplate jdbcTemplate;


    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final static DateTimeFormatter DTF_YMDHMS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * 类型 1-营销记录
     */
    private final static int MARKET_TYPE = 1;

    /**
     * 审核中
     */
    private final static int APPLYING = 1;

    /**
     * 审核通过
     */
    private final static int APPLY_SUCCESS = 2;

    /**
     * 审核未通过(失败)
     */
    private final static int APPLY_FAIL = 3;

    private String generateFileName(CustomerMarketExport customerMarketExport) {
        StringBuilder fileName = new StringBuilder();
        fileName.append(customerMarketExport.getEnterpriseName());
        fileName.append(LocalDateTime.now().format(DATE_TIME_FORMATTER));
        String startTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DTF_YMDHMS);
        String endTime = LocalDateTime.of(LocalDate.now(), LocalTime.now()).format(DTF_YMDHMS);
        List<Map<String, Object>> fileNames = jdbcTemplate.queryForList("SELECT file_name FROM customer_market_export  WHERE cust_id = ? " +
                "AND apply_time >? AND apply_time<?  ORDER BY file_name DESC LIMIT 1 ", new Object[]{customerMarketExport.getCustomerId(), startTime, endTime});
        if (fileNames.size() == 0) {
            fileName.append("0001");
        } else {
            if (fileNames.get(0).get("file_name") == null) {
                fileName.append("0001");
            } else {
                String lastFileName = String.valueOf(fileNames.get(0).get("file_name"));
                int serialNumber = Integer.parseInt(lastFileName.substring(lastFileName.length() - 4, lastFileName.length()));
                serialNumber++;
                if (String.valueOf(serialNumber).length() == 1) {
                    fileName.append("000" + serialNumber);
                } else if (String.valueOf(serialNumber).length() == 2) {
                    fileName.append("00" + serialNumber);
                } else if (String.valueOf(serialNumber).length() == 3) {
                    fileName.append("0" + serialNumber);
                } else {
                    fileName.append(serialNumber);
                }
            }
        }
        return fileName.toString();
    }


    
    public int customerMarketExportApply(CustomerMarketExport customerMarketExport) {
        Date currentDate = new Date();
        CustomerMarketExportDO customerMarketExportDO = new CustomerMarketExportDO();
        customerMarketExportDO.setCustomerId(customerMarketExport.getCustomerId());
        customerMarketExportDO.setCustomerGroupId(customerMarketExport.getCustomerGroupId());
        customerMarketExportDO.setType(MARKET_TYPE);
        customerMarketExportDO.setFileName(generateFileName(customerMarketExport));
        customerMarketExportDO.setApplyTime(currentDate);
        customerMarketExportDO.setCreateTime(currentDate);
        customerMarketExportDO.setStatus(APPLYING);
        customerMarketExportDO.setOperator(customerMarketExport.getOperator());
        Long id = (Long) customerMarketExportDao.saveReturnPk(customerMarketExportDO);
        if (id > 0) {
            return 1;
        }
        return 0;
    }

    
    public int customerMarketExportApproval(CustomerMarketExport customerMarketExport) {
        if (customerMarketExport.getStatus() != null) {
            if (customerMarketExport.getStatus().intValue() == 1) {
                return jdbcTemplate.update("UPDATE customer_market_export SET `status` = 2 WHERE id = ?", new Object[]{customerMarketExport.getId()});
            } else if (customerMarketExport.getStatus().intValue() == 2) {
                return jdbcTemplate.update("UPDATE customer_market_export SET `status` = 3, remark = ? WHERE id = ?", new Object[]{customerMarketExport.getRemark(), customerMarketExport.getId()});
            }
        }
        return 0;
    }

    
    public int countCustomerMarketExport(String customerId, Date startTime, Date endTime) {
        String localDateTimeStart;
        String localDateTimeEnd;
        if (startTime == null) {
            localDateTimeStart = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DTF_YMDHMS);
        } else {
            localDateTimeStart = startTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DTF_YMDHMS);
        }
        if (endTime == null) {
            localDateTimeEnd = LocalDateTime.of(LocalDate.now(), LocalTime.now()).format(DTF_YMDHMS);
        } else {
            localDateTimeEnd = endTime.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime().format(DTF_YMDHMS);
        }
        List<Map<String, Object>> fileNames = jdbcTemplate.queryForList("SELECT id FROM customer_market_export  WHERE cust_id = ? " +
                "AND apply_time >? AND apply_time<? ", new Object[]{customerId, localDateTimeStart, localDateTimeEnd});
        return fileNames.size();
    }

    
    public List<Map<String, Object>> list(String customerId, String status, String customerName, String createTimeStart, String createTimeEnd) {
        List<Map<String, Object>> list;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT count(*) count FROM customer_market_export ");
        sql.append(" JOIN t_customer customer ON customer.cust_id = customer_market_export.cust_id ");
        sql.append("WHERE customer_market_export.cust_id = ?");
        if (StringUtil.isNotEmpty(status)) {
            sql.append(" AND status = " + status);
        }
        if (StringUtil.isNotEmpty(createTimeStart) && StringUtil.isNotEmpty(createTimeEnd)) {
            sql.append(" AND apply_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
        }
        if (StringUtil.isNotEmpty(customerName)) {
            sql.append(" AND customer.enterprise_name LIKE '%" + customerName + "%' ");
        }
        sql.append(" order by customer_market_export.create_time DESC");
        list = jdbcTemplate.queryForList(sql.toString(), new Object[]{customerId});
        return list;
    }

    
    public List<Map<String, Object>> listPage(String customerId, String status, Integer pageNum, Integer pageSize, String customerName, String createTimeStart, String createTimeEnd) {
        List<Map<String, Object>> list;
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT customer_market_export.*, customer.enterprise_name FROM customer_market_export ");
        sql.append(" JOIN t_customer customer ON customer.cust_id = customer_market_export.cust_id ");
        sql.append("WHERE customer_market_export.cust_id = ?");
        if (StringUtil.isNotEmpty(status)) {
            sql.append(" AND status = " + status);
        }
        if (StringUtil.isNotEmpty(createTimeStart) && StringUtil.isNotEmpty(createTimeEnd)) {
            sql.append(" AND apply_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
        }
        if (StringUtil.isNotEmpty(customerName)) {
            sql.append(" AND customer.enterprise_name LIKE '%" + customerName + "%' ");
        }
        sql.append(" order by customer_market_export.create_time DESC");
        sql.append(" LIMIT " + pageNum + "," + pageSize);
        list = jdbcTemplate.queryForList(sql.toString(), new Object[]{customerId});
        // 处理未审核状态下的记录条数
        if (list.size() > 0) {
            long intentionCustomerCount = 0;
            LocalDateTime startLocalDateTime;
            LocalDateTime endLocalDateTime;
            Date startTime;
            for (Map<String, Object> map : list) {
                startTime = (Date) map.get("apply_time");
                startLocalDateTime = LocalDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault());
                // 开始时间为申请当天的0点0分0秒
                startLocalDateTime = startLocalDateTime.withHour(0).withMinute(0).withSecond(0);
                // 结束时间为申请当天的23点59分59秒
                endLocalDateTime = startLocalDateTime.withHour(23).withMinute(59).withSecond(59);
                // 审核中
                if (map.get("status") != null && "1".equals(String.valueOf(map.get("status")))) {
                    intentionCustomerCount = countIntentionCustomer(customerId, String.valueOf(map.get("customer_group_id")), "1", null, "是",
                            Date.from(startLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()), Date.from(endLocalDateTime.atZone(ZoneId.systemDefault()).toInstant()));
                }
                map.put("potential_person_sum", intentionCustomerCount);
            }
        }
        return list;
    }

    
    public long countIntentionCustomer(String customerId, String customerGroupId, String invitationLabelId, String invitationLabelName, String invitationLabelValue, Date startTime, Date endTime) {
        long returnCode = 0L;
        // 处理时间
        String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DTF_YMDHMS);
        String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DTF_YMDHMS);
        if (startTime != null) {
            startTimeStr = LocalDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault()).format(DTF_YMDHMS);
        }
        if (endTime != null) {
            endTimeStr = LocalDateTime.ofInstant(endTime.toInstant(), ZoneId.systemDefault()).format(DTF_YMDHMS);
        }
        // 处理自定义属性
        if (StringUtil.isEmpty(invitationLabelId)) {
            List<Map<String, Object>> labelNames = jdbcTemplate.queryForList("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    if (StringUtil.isNotEmpty(invitationLabelName) && invitationLabelName.equals(String.valueOf(map.get("label_name")))) {
                        invitationLabelId = String.valueOf(map.get("label_id"));
                        break;
                    }
                }
            }
        }
        if (StringUtil.isNotEmpty(invitationLabelId)) {
            // 获取邀约成功的用户
            List<String> superIds = new ArrayList<>();
            List<Map<String, Object>> invitationLabels = jdbcTemplate.queryForList("SELECT t1.option_value, t1.super_id superid FROM t_super_label t1  WHERE t1.super_id IN (SELECT id FROM t_customer_group_list_" + customerId + ") AND t1.cust_group_id = ?" +
                    " AND t1.label_id = ? AND FIND_IN_SET('" + invitationLabelValue + "', option_value) ", new Object[]{customerGroupId, invitationLabelId});
            for (Map<String, Object> map : invitationLabels) {
                superIds.add(String.valueOf(map.get("superid")));
            }
            if (superIds.size() > 0) {
                // 获取邀约成功,拨打电话成功用户的通话记录
                List<Map<String, Object>> callLogList = jdbcTemplate.queryForList("SELECT IFNULL(COUNT(id), 0) count FROM t_touch_voice_log voice " +
                        " LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid " +
                        " LEFT JOIN t_user tuser ON tuser.id = voice.user_id " +
                        " WHERE voice.cust_id = ? " +
                        " AND voice.type_code = 1 " +
                        " AND voice.status = 1001 " +
                        " AND voice.create_time > ? AND voice.create_time < ? " +
                        " AND voice.superid IN  ( " + org.apache.commons.lang.StringUtils.join(superIds, ",") + " ) ", new Object[]{customerId, startTimeStr, endTimeStr});
                returnCode = (long) callLogList.get(0).get("count");
            }
        }
        return returnCode;
    }

    
    public List<Map<String, Object>> listPageIntentionCustomer(String customerMarketExportId, Integer pageNum, Integer pageSize, String superId, String invitationLabelId, String invitationLabelName, String invitationLabelValue) {
        List<Map<String, Object>> list = Collections.emptyList();
        List<Map<String, Object>> customerMarketList = jdbcTemplate.queryForList("SELECT * FROM customer_market_export WHERE id = ?", new Object[]{customerMarketExportId});
        if(customerMarketList.size() > 0){
            String customerId = String.valueOf(customerMarketList.get(0).get("cust_id"));
            String customerGroupId = String.valueOf(customerMarketList.get(0).get("customer_group_id"));
            Date startTime = (Date) customerMarketList.get(0).get("apply_time");
            LocalDateTime startLocalDateTime = LocalDateTime.ofInstant(startTime.toInstant(), ZoneId.systemDefault());
            // 开始时间为申请当天的0点0分0秒
            startLocalDateTime = startLocalDateTime.withHour(0).withMinute(0).withSecond(0);
            // 结束时间为申请当天的23点59分59秒
            LocalDateTime endLocalDateTime = startLocalDateTime.withHour(23).withMinute(59).withSecond(59);
            String startTimeStr = startLocalDateTime.format(DTF_YMDHMS);
            String endTimeStr = endLocalDateTime.format(DTF_YMDHMS);
            // 处理自定义属性
            if (StringUtil.isEmpty(invitationLabelId)) {
                List<Map<String, Object>> labelNames = jdbcTemplate.queryForList("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? ", new Object[]{customerId});
                for (Map<String, Object> map : labelNames) {
                    if (map != null && map.get("label_name") != null) {
                        if (StringUtil.isNotEmpty(invitationLabelName) && invitationLabelName.equals(String.valueOf(map.get("label_name")))) {
                            invitationLabelId = String.valueOf(map.get("label_id"));
                            break;
                        }
                    }
                }
            }
            if (StringUtil.isNotEmpty(invitationLabelId)) {
                // 获取邀约成功的用户
                List<String> superIds = new ArrayList<>();
                List<Map<String, Object>> invitationLabels = jdbcTemplate.queryForList("SELECT t1.option_value, t1.super_id superid FROM t_super_label t1  WHERE t1.super_id IN (SELECT id FROM t_customer_group_list_" + customerId + ") AND t1.cust_group_id = ?" +
                        " AND t1.label_id = ? AND FIND_IN_SET('" + invitationLabelValue + "', option_value) ", new Object[]{customerGroupId, invitationLabelId});
                for (Map<String, Object> map : invitationLabels) {
                    superIds.add(String.valueOf(map.get("superid")));
                }
                // 处理根据superId搜索
                if(superIds.contains(superId)){
                    superIds = new ArrayList<>();
                    superIds.add(superId);
                }
                if (superIds.size() > 0) {
                    StringBuffer sql = new StringBuffer();
                    if(pageNum !=null && pageSize != null){
                        sql.append("SELECT voice.superid, voice.create_time create_time,voice.status,CAST(voice.user_id AS CHAR) user_id,voice.remark,callback.Callerduration, tuser.account name,tuser.realname,substring_index( callback.recordurl ,'/' , -1 ) as recordurl  FROM t_touch_voice_log voice " +
                                " LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid " +
                                " LEFT JOIN t_customer_user tuser ON tuser.id = voice.user_id " +
                                " WHERE voice.cust_id = ? " +
                                " AND voice.type_code = 1 " +
                                " AND voice.status = 1001 " +
                                " AND voice.create_time > ? AND voice.create_time < ? " +
                                " AND voice.superid IN  ( " + org.apache.commons.lang.StringUtils.join(superIds, ",") + " ) ");
                        sql.append(" order by voice.create_time DESC");
                        sql.append(" LIMIT " + pageNum + "," + pageSize);
                    }else{
                        sql.append("SELECT count(*) count FROM t_touch_voice_log voice " +
                                " LEFT JOIN t_callback_info callback ON voice.callSid = callback.callSid " +
                                " LEFT JOIN t_customer_user tuser ON tuser.id = voice.user_id " +
                                " WHERE voice.cust_id = ? " +
                                " AND voice.type_code = 1 " +
                                " AND voice.status = 1001 " +
                                " AND voice.create_time > ? AND voice.create_time < ? " +
                                " AND voice.superid IN  ( " + org.apache.commons.lang.StringUtils.join(superIds, ",") + " ) ");
                        sql.append(" order by voice.create_time DESC");
                    }
                    // 获取邀约成功,拨打电话成功用户的通话记录
                    list = jdbcTemplate.queryForList(sql.toString(), new Object[]{customerId, startTimeStr, endTimeStr});
                }
            }
        }
        return list;
    }
}
