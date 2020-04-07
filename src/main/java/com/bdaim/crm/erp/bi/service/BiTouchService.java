package com.bdaim.crm.erp.bi.service;

import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.util.ConfigUtil;
import com.jfinal.plugin.activerecord.Record;
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
     * 通话记录 列表
     *
     * @author Chacker
     * @date 2020/4/2
     */
    public ResponseInfo phoneList(Integer pageNum, Integer pageSize, Integer deptId, Long userId,
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
            testMap.put("realname", "程宁测试");
            testMap.put("create_time", "2020-3-10 13:20:00");
            testMap.put("custName", "我是被叫名称");
            testMap.put("custNumber", "被叫号码");
            testMap.put("status", "1001");
            testMap.put("Callerduration", "我是通话时长");
            testMap.put("callSid", "我是呼叫ID");
            testMap.put("companyName", "北京某公司名称");
            testMap.put("recordurl", "20200420031904523400007200402112716000001.mp3");
            testMap.put("userId", "20031904523400007");
            resultList.add(testMap);
        }
        result.put("data", resultList);
        result.put("total", 16);
        // 录音路径
        String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
        result.put("audioUrl", audioUrl);
        return new ResponseInfoAssemble().success(result);
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


