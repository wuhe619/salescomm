package com.bdaim.crm.erp.bi.service;

import com.bdaim.callcenter.common.CallUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.service.PhoneService;
import com.bdaim.crm.dao.LkCrmBiDao;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.ConfigUtil;
import com.bdaim.util.DatetimeUtils;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import org.apache.poi.util.ArrayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class BiTouchService {
    @Autowired
    private BiTimeUtil biTimeUtil;
    @Autowired
    private LkCrmBiDao biDao;
    @Autowired
    private PhoneService phoneService;

    /**
     * 电话触达接通率分析
     *
     * @return
     * @author Chacker
     */
    @SuppressWarnings("all")
    public ResponseInfo phone(Integer deptId, Long userId, String type, String startTime, String endTime) {
        //proportion、type、phoneNum、phoneEnd
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type)
                .set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        Integer beginTime = record.getInt("beginTime");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        String[] userIdsArr = userIds.split(",");
        //根据查询类型拼接查询SQL字符串
        StringBuffer sqlBuffer = new StringBuffer();
        for (int i = 1; i <= cycleNum; i++) {
            sqlBuffer.append("SELECT ")
                    .append("   '").append(beginTime).append("' AS type,")
                    .append("   IFNULL(SUM(total_num),0) phoneNum,")
                    .append("   IFNULL(SUM(total_end),0) phoneEnd,")
                    .append("   FORMAT(IFNULL(IFNULL(SUM(total_end),0)/IFNULL(SUM(total_num),0),0),4) proportion ")
                    .append("FROM   ")
                    .append("   lkcrm_crm_phone_sms_stats ")
                    .append("WHERE date_format( create_time,'").append(sqlDateFormat).append("')='")
                    .append(beginTime).append("' AND type = '1' AND user_id IN (")
                    .append(SqlAppendUtil.sqlAppendWhereIn(userIdsArr)).append(") ");
            if (i != cycleNum) {
                sqlBuffer.append(" UNION ALL ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> result = biDao.queryListBySql(sqlBuffer.toString());
        return new ResponseInfoAssemble().success(result);
    }

    @SuppressWarnings("all")
    public ResponseInfo textMessage(Integer deptId, Long userId, String type, String startTime, String endTime) {
        //proportion
        //type
        //messageNum
        //messageEnd
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type)
                .set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        Integer beginTime = record.getInt("beginTime");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        String[] userIdsArr = userIds.split(",");
        //根据查询类型拼接查询SQL字符串
        StringBuffer sqlBuffer = new StringBuffer();
        for (int i = 1; i <= cycleNum; i++) {
            sqlBuffer.append("SELECT ")
                    .append("   '").append(beginTime).append("' AS type,")
                    .append("   IFNULL(SUM(total_num),0) messageNum,")
                    .append("   IFNULL(SUM(total_end),0) messageEnd,")
                    .append("   FORMAT(IFNULL(IFNULL(SUM(total_end),0)/IFNULL(SUM(total_num),0),0),4) proportion ")
                    .append("FROM   ")
                    .append("   lkcrm_crm_phone_sms_stats ")
                    .append("WHERE date_format( create_time,'").append(sqlDateFormat).append("')='")
                    .append(beginTime).append("' AND type = '2' AND user_id IN (")
                    .append(SqlAppendUtil.sqlAppendWhereIn(userIdsArr)).append(") ");
            if (i != cycleNum) {
                sqlBuffer.append(" UNION ALL ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> result = biDao.queryListBySql(sqlBuffer.toString());
        return new ResponseInfoAssemble().success(result);
    }

    /**
     * 通话记录 列表 obj_type 1、线索 2、客户 3、联系人
     *
     * @author Chacker
     * @date 2020/4/2
     */
    @SuppressWarnings("all")
    public ResponseInfo phoneList(Integer pageNum, Integer pageSize, Integer deptId, Long userId,
                                  String type, String startTime, String endTime) {
        //默认传进来的参数有 pageNum、pageSize、deptId、type
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        //t_touch_voice_log_ 表名
        String tableNamesStr = record.getStr("tableNames");
        String[] tableNamesArr = tableNamesStr.split(",");
        //用户ID
        String userIds = record.getStr("userIds");
        String[] userIdsArr = userIds.split(",");
        String custId = BaseUtil.getCustId();
        String relationTable = "obj_u_" + custId.substring(custId.length() - 1);

        Map<String, Object> result = new HashMap<>();
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("SELECT * FROM (");
        Integer status = biTimeUtil.analyzeType(type);
        List<Object> params = new ArrayList<>();
        //obj_type 1、线索 2、客户 3、联系人
        for (int i = 0; i < tableNamesArr.length; i++) {
            sqlBuffer.append("SELECT ")
                    .append("  ( SELECT realname FROM t_customer_user WHERE id = t1.user_id ) realname,")
                    .append("  t1.create_time create_time,t1.call_data ->> '$.called' AS custNumber,")
                    .append("  t1.callSid callSid,CAST(t1.user_id AS char) userId,t1.status status,t1.touch_id touchId,")
                    .append("  t1.called_duration Callerduration,t1.recordurl recordurl,t2.obj_id, ")
                    .append(" CASE WHEN  t2.type='1' THEN x1.leads_name  ")
                    .append(" WHEN t2.type='2' THEN x2.customer_name")
                    .append(" WHEN t2.type='3' THEN x3.name")
                    .append(" END AS custName, ")
                    .append(" CASE WHEN t2.type='1' THEN x1.company")
                    .append(" WHEN t2.type='2' THEN x2.company ")
                    .append(" END AS companyName ")
                    .append("FROM ").append(tableNamesArr[i])
                    .append(" t1 LEFT JOIN ").append(relationTable).append(" t2 ON t1.superid = t2.u_id ")
                    .append(" AND t1.obj_type = t2.obj_id ")
                    .append(" LEFT JOIN lkcrm_crm_leads x1 ON t2.obj_id = x1.leads_id ")
                    .append(" LEFT JOIN lkcrm_crm_customer x2 ON t2.obj_id = x2.customer_id ")
                    .append(" LEFT JOIN lkcrm_crm_contacts x3 ON t2.obj_id = x3.contacts_id ")
                    .append(" WHERE t1.obj_type IS NOT NULL ")
                    .append(" AND t1.user_id IN (").append(SqlAppendUtil.sqlAppendWhereIn(userIdsArr))
                    .append(") ");
            if (status == 1) {
                sqlBuffer.append(" AND TO_DAYS(NOW()) = TO_DAYS(t1.create_time) ");
            }
            if (status == 2) {
                sqlBuffer.append(" AND TO_DAYS(NOW()) - TO_DAYS(t1.create_time) = 1 ");
            }
            if (status == 3) {
                sqlBuffer.append(" AND YEARWEEK(date_format(t1.create_time,'%Y-%m-%d')) = YEARWEEK(now()) ");
            }
            if (status == 4) {
                sqlBuffer.append(" AND YEARWEEK(date_format(t1.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ");
            }
            if (status == 5) {
                sqlBuffer.append(" AND date_format(t1.create_time,'%Y-%m')=date_format(now(),'%Y-%m')  ");
            }
            if (status == 6) {
                sqlBuffer.append(" AND date_format(t1.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ");
            }
            if (status == 7) {
                sqlBuffer.append(" AND QUARTER(t1.create_time)=QUARTER(now()) AND YEAR(t1.create_time)=YEAR(NOW()) ");
            }
            if (status == 8) {
                sqlBuffer.append(" AND QUARTER(t1.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) AND YEAR(DATE_SUB(t1.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) ");
            }
            if (status == 9) {
                sqlBuffer.append(" AND YEAR(t1.create_time)=YEAR(NOW()) ");
            }
            if (status == 10) {
                sqlBuffer.append(" AND YEAR(t1.create_time)=YEAR(date_sub(now(),interval 1 year)) ");
            }
            if (status == 11) {
                sqlBuffer.append(" AND  TO_DAYS(t1.create_time) >= TO_DAYS(?)");
                sqlBuffer.append(" AND  TO_DAYS(t1.create_time) <= TO_DAYS(?)");
                params.add(startTime);
                params.add(endTime);
            }
            if (i != tableNamesArr.length - 1) {
                sqlBuffer.append(" UNION ALL ");
            }
        }
        sqlBuffer.append(" ) tt ORDER BY tt.create_time DESC ");
        Page page = biDao.sqlPageQuery(sqlBuffer.toString(), pageNum, pageSize, params.toArray());
        List<Map<String, Object>> resultList = page.getData();
        if (!CollectionUtils.isEmpty(resultList)) {
            for (int i = 0; i < resultList.size(); i++) {
                Map<String, Object> map = resultList.get(i);
                String monthYear = LocalDateTime.parse(String.valueOf(map.get("create_time")),
                        DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                map.put("recordurl", CallUtil.generateRecordNameToMp3(monthYear, map.get("touchId")));
                String Callerduration = String.valueOf(map.get("Callerduration"));
                if (StringUtil.isEmpty(Callerduration)) {
                    map.put("recordurl", "");
                }
            }
        }
        result.put("data", resultList);
        result.put("total", page.getTotal());
        return new ResponseInfoAssemble().success(result);
    }

    /**
     * 短信记录 列表
     *
     * @author Chacker
     * @date 2020/4/2
     */
    @SuppressWarnings("all")
    public ResponseInfo messageList(Integer pageNum, Integer pageSize, Integer deptId, Long userId,
                                    String type, String startTime, String endTime) {
        //默认传进来的参数有 pageNum、pageSize、deptId、type
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        String[] userIdsArr = userIds.split(",");

        Integer status = biTimeUtil.analyzeType(type);
        //t_touch_sms_log 表名
        StringBuffer sqlBuffer = new StringBuffer();
        sqlBuffer.append("SELECT * FROM ( ");
        List<Object> params = new ArrayList<>();
        sqlBuffer.append("SELECT ")
                .append("  ( SELECT realname FROM t_customer_user WHERE id = t1.user_id ) realname,")
                .append("  t1.create_time sendTime,")
                .append("  ( SELECT NAME FROM t_market_task WHERE id = t1.batch_number ) batchName,")
                .append("  t1.status status,")
                .append("  t1.request_id sendId,")
                .append("  ( SELECT title FROM t_template WHERE id = t1.templateId ) templateName,")
                .append("  t1.superid superId ")
                .append(" FROM t_touch_sms_log t1 WHERE 1=1 ")
                .append(" AND t1.obj_type IS NOT NULL ")
                .append(" AND t1.user_id IN (").append(SqlAppendUtil.sqlAppendWhereIn(userIdsArr))
                .append(") ");
        if (status == 1) {
            sqlBuffer.append(" AND TO_DAYS(NOW()) = TO_DAYS(t1.create_time) ");
        }
        if (status == 2) {
            sqlBuffer.append(" AND TO_DAYS(NOW()) - TO_DAYS(t1.create_time) = 1 ");
        }
        if (status == 3) {
            sqlBuffer.append(" AND YEARWEEK(date_format(t1.create_time,'%Y-%m-%d')) = YEARWEEK(now()) ");
        }
        if (status == 4) {
            sqlBuffer.append(" AND YEARWEEK(date_format(t1.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ");
        }
        if (status == 5) {
            sqlBuffer.append(" AND date_format(t1.create_time,'%Y-%m')=date_format(now(),'%Y-%m')  ");
        }
        if (status == 6) {
            sqlBuffer.append(" AND date_format(t1.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ");
        }
        if (status == 7) {
            sqlBuffer.append(" AND QUARTER(t1.create_time)=QUARTER(now()) AND YEAR(t1.create_time)=YEAR(NOW()) ");
        }
        if (status == 8) {
            sqlBuffer.append(" AND QUARTER(t1.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) AND YEAR(DATE_SUB(t1.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) ");
        }
        if (status == 9) {
            sqlBuffer.append(" AND YEAR(t1.create_time)=YEAR(NOW()) ");
        }
        if (status == 10) {
            sqlBuffer.append(" AND YEAR(t1.create_time)=YEAR(date_sub(now(),interval 1 year)) ");
        }
        if (status == 11) {
            sqlBuffer.append(" AND  TO_DAYS(t1.create_time) >= TO_DAYS(?)");
            sqlBuffer.append(" AND  TO_DAYS(t1.create_time) <= TO_DAYS(?)");
            params.add(startTime);
            params.add(endTime);
        }
        sqlBuffer.append(") tt ORDER BY tt.sendTime DESC");


        Page page = biDao.sqlPageQuery(sqlBuffer.toString(), pageNum, pageSize, params.toArray());
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resultList = page.getData();
        //根据superId获取电话号码(手机或固话)
        if (!CollectionUtils.isEmpty(resultList)) {
            for (int i = 0; i < resultList.size(); i++) {
                Map<String, Object> map = resultList.get(i);
                String superId = String.valueOf(map.get("superId"));
                String sendNumber = phoneService.upn(superId);
                map.put("sendNumber", sendNumber);
            }
        }
        result.put("data", resultList);
        result.put("total", page.getTotal());
        return new ResponseInfoAssemble().success(result);
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        int totalCount = 0;
//        if (pageNum == 1) {
//            totalCount = 15;
//        } else if (pageNum == 2) {
//            totalCount = 1;
//        }
//        for (int i = 0; i < totalCount; i++) {
//            Map<String, Object> testMap = new HashMap<>();
//            testMap.put("realname", "我是员工名称");
//            testMap.put("sendTime", "2020-3-10 13:20:00");
//            testMap.put("batchName", "我是批次名称");
//            testMap.put("sendNumber", "1338890899");
//            testMap.put("status", "1001");
//            testMap.put("templateName", "我是模板名称");
//            testMap.put("sendId", "我是发送ID");
//            resultList.add(testMap);
//        }
//        result.put("data", resultList);
//        result.put("total", 16);
    }
}


