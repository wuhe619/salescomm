package com.bdaim.crm.erp.bi.service;

import cn.hutool.core.util.StrUtil;
import com.bdaim.crm.dao.LkCrmBiDao;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class BiRankingService {

    @Resource
    BiTimeUtil biTimeUtil;
    @Autowired
    private LkCrmBiDao biDao;

    public R contractRanKing(Integer deptId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.contractRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R receivablesRanKing(Integer deptId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.receivablesRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R contractCountRanKing(Integer deptId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.contractCountRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R productCountRanKing(Integer deptId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.productCountRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R customerCountRanKing(Integer deptId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.customerCountRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R contactsCountRanKing(Integer deptId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.contactsCountRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R customerGenjinCountRanKing(Integer deptId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.customerGenjinCountRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R recordCountRanKing(Integer deptId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.recordCountRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R contractProductRanKing(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.contractProductRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R travelCountRanKing(Integer deptId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.travelCountRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R productSellRanKing(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.productSellRanKing(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R addressAnalyse() {
        String[] addResss = biTimeUtil.getAddress();
        List<Record> list = new ArrayList<>();
        for (String addRess : addResss) {
            Record record = biDao.addressAnalyse(addRess);
            list.add(record);
        }
        return R.ok().put("data", list);
    }

    public R portrait(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.portrait(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R portraitLevel(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.portraitLevel(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    public R portraitSource(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdsArr = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.portraitSource(userIdsArr, status, startTime, endTime);
        return R.ok().put("data", list);
    }
}
