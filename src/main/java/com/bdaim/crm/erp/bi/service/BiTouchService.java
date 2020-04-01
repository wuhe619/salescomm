package com.bdaim.crm.erp.bi.service;

import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
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
    public ResponseInfo phone(Integer deptId, Long userId, String type) {
        //proportion
        //type
        //phoneNum
        //phoneEnd
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type)
                .set("startTime", null).set("endTime", null);
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
            BigDecimal proportionD = phoneEndD.divide(phoneNumD,4,BigDecimal.ROUND_HALF_UP);
            resultMap.put("proportion", String.valueOf(proportionD));
            resultList.add(resultMap);
        }
        return new ResponseInfoAssemble().success(resultList);
    }
}


