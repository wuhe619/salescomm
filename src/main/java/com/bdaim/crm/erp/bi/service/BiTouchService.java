package com.bdaim.crm.erp.bi.service;

import com.bdaim.common.dto.Page;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.dao.LkCrmBiDao;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.ConfigUtil;
import com.jfinal.plugin.activerecord.Record;
import org.apache.poi.util.ArrayUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
@Transactional(rollbackFor = Exception.class)
public class BiTouchService {
    @Autowired
    private BiTimeUtil biTimeUtil;
    @Autowired
    private LkCrmBiDao biDao;

    /**
     * 电话触达接通率分析
     *
     * @return
     * @author Chacker
     */
    public ResponseInfo phone(Integer deptId, Long userId, String type, String startTime, String endTime) {
        //proportion
        //type
        //phoneNum
        //phoneEnd
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type)
                .set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        Integer beginTime = record.getInt("beginTime");
        List<Map<String, Object>> resultList = new ArrayList<>();
        Random ran = new Random();
        for (int i = 0; i < cycleNum; i++) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("type", String.valueOf(beginTime));
            beginTime = biTimeUtil.estimateTime(beginTime);
            int phoneNum = ran.nextInt(10) + 5;
            int phoneEnd = phoneNum - ran.nextInt(5);
            resultMap.put("phoneNum", phoneNum);
            resultMap.put("phoneEnd", phoneEnd);

            BigDecimal phoneNumD = new BigDecimal(phoneNum);
            BigDecimal phoneEndD = new BigDecimal(phoneEnd);
            BigDecimal proportionD = phoneEndD.divide(phoneNumD, 4, BigDecimal.ROUND_HALF_UP);
            resultMap.put("proportion", String.valueOf(proportionD));
            resultList.add(resultMap);
        }
        return new ResponseInfoAssemble().success(resultList);
    }

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
        List<Map<String, Object>> resultList = new ArrayList<>();
        Random ran = new Random();
        for (int i = 0; i < cycleNum; i++) {
            Map<String, Object> resultMap = new HashMap<>();
            resultMap.put("type", String.valueOf(beginTime));
            beginTime = biTimeUtil.estimateTime(beginTime);
            int messageNum = ran.nextInt(10) + 5;
            int messageEnd = messageNum - ran.nextInt(5);
            resultMap.put("messageNum", messageNum);
            resultMap.put("messageEnd", messageEnd);

            BigDecimal messageNumD = new BigDecimal(messageNum);
            BigDecimal messageEndD = new BigDecimal(messageEnd);
            BigDecimal proportionD = messageEndD.divide(messageNumD, 4, BigDecimal.ROUND_HALF_UP);
            resultMap.put("proportion", String.valueOf(proportionD));
            resultList.add(resultMap);
        }
        return new ResponseInfoAssemble().success(resultList);

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
        String tableNamesStr = record.getStr("tableNames");
        String[] tableNamesArr = tableNamesStr.split(",");
        String custId = BaseUtil.getCustId();
        String relationTable = "obj_u_" + custId.substring(custId.length() - 1);

        Map<String, Object> result = new HashMap<>();
        StringBuffer sqlBuffer = new StringBuffer();
        Integer status = biTimeUtil.analyzeType(type);
        List<Object> params = new ArrayList<>();
        //obj_type 1、线索 2、客户 3、联系人
        for (int i = 0; i < tableNamesArr.length; i++) {
            sqlBuffer.append("SELECT ")
                    .append("  ( SELECT realname FROM t_customer_user WHERE id = t1.user_id ) realname,")
                    .append("  t1.create_time create_time,t1.call_data ->> '$.called' AS custNumber,")
                    .append("  t1.callSid callSid,t1.user_id userId,t1.status status,")
                    .append("  t1.called_duration Callerduration,t1.recordurl recordurl,t2.obj_id, ")
                    .append(" CASE WHEN  t1.obj_type='1' THEN x1.leads_name  ")
                    .append(" WHEN t1.obj_type='2' THEN x2.customer_name")
                    .append(" WHEN t1.obj_type='3' THEN x3.name")
                    .append(" END AS custName, ")
                    .append(" CASE WHEN t1.obj_type='1' THEN x1.company")
                    .append(" WHEN t1.obj_type='2' THEN x2.company ")
                    .append(" END AS companyName ")
                    .append("FROM ").append(tableNamesArr[i])
                    .append(" t1 LEFT JOIN ").append(relationTable).append(" t2 ON t1.superid = t2.u_id ")
                    .append(" AND t1.obj_type = t2.type ")
                    .append(" LEFT JOIN lkcrm_crm_leads x1 ON t2.obj_id = x1.leads_id ")
                    .append(" LEFT JOIN lkcrm_crm_customer x2 ON t2.obj_id = x2.customer_id ")
                    .append(" LEFT JOIN lkcrm_crm_contacts x3 ON t2.obj_id = x3.contacts_id ")
                    .append(" WHERE t1.obj_type IS NOT NULL ");
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
        Page page = biDao.sqlPageQuery(sqlBuffer.toString(), pageNum, pageSize, params.toArray());
        result.put("data", page.getData());
        result.put("total", page.getTotal());
        return new ResponseInfoAssemble().success(result);
//        Map<String, Object> result = new HashMap<>();
//        List<Map<String, Object>> resultList = new ArrayList<>();
//        int totalCount = 0;
//        if (pageNum == 1) {
//            totalCount = 15;
//        } else if (pageNum == 2) {
//            totalCount = 1;
//        }
//        for (int i = 0; i < totalCount; i++) {
//            Map<String, Object> testMap = new HashMap<>();
////            testMap.put("realname", "程宁测试");
////            testMap.put("create_time", "2020-3-10 13:20:00");
//            testMap.put("custName", "我是被叫名称");
//            testMap.put("custNumber", "被叫号码");
////            testMap.put("status", "1001");
////            testMap.put("Callerduration", "我是通话时长");
////            testMap.put("callSid", "我是呼叫ID");
//            testMap.put("companyName", "北京某公司名称");
////            testMap.put("recordurl", "20200420031904523400007200402112716000001.mp3");
////            testMap.put("userId", "20031904523400007");
//            resultList.add(testMap);
//        }
//        result.put("data", resultList);
//        result.put("total", 16);
        // 录音路径
//        String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
//        result.put("audioUrl", audioUrl);
//        return new ResponseInfoAssemble().success(result);
    }

    /**
     * 短信记录 列表
     *
     * @author Chacker
     * @date 2020/4/2
     */
    public ResponseInfo messageList(Integer pageNum, Integer pageSize, Integer deptId, Long userId,
                                    String type, String startTime, String endTime) {
        //默认传进来的参数有 pageNum、pageSize、deptId、type
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> resultList = new ArrayList<>();
        int totalCount = 0;
        if (pageNum == 1) {
            totalCount = 15;
        } else if (pageNum == 2) {
            totalCount = 1;
        }
        for (int i = 0; i < totalCount; i++) {
            Map<String, Object> testMap = new HashMap<>();
            testMap.put("realname", "我是员工名称");
            testMap.put("sendTime", "2020-3-10 13:20:00");
            testMap.put("batchName", "我是批次名称");
            testMap.put("sendNumber", "1338890899");
            testMap.put("status", "1001");
            testMap.put("templateName", "我是模板名称");
            testMap.put("sendId", "我是发送ID");
            resultList.add(testMap);
        }
        result.put("data", resultList);
        result.put("total", 16);
        return new ResponseInfoAssemble().success(result);
    }
}


