package com.bdaim.crm.erp.bi.service;

import cn.hutool.core.util.StrUtil;
import com.bdaim.crm.dao.LkCrmBiDao;
import com.bdaim.crm.dao.LkCrmOaExamineCategoryDao;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.crm.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class BiFunnelService {
    @Resource
    BiTimeUtil biTimeUtil;
    @Autowired
    private LkCrmOaExamineCategoryDao categoryDao;
    @Autowired
    private LkCrmBiDao biDao;

    /**
     * 销售漏斗
     *
     * @author Chacker
     */
    public R sellFunnel(Integer deptId, Long userId, String type, String startTime, String endTime, Integer typeId) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        List<Record> list = new ArrayList<>();
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdss = userIds.split(",");
        Integer ststus = biTimeUtil.analyzeType(type);
        list = biDao.sellFunnel(userIdss, ststus, startTime, endTime, typeId);
        return R.ok().put("data", list);
    }

    /**
     * 新增商机分析
     *
     * @author zxy
     */
    public R addBusinessAnalyze(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        String businessview = BaseUtil.getViewSql("businessview");
        for (int i = 1; i <= cycleNum; i++) {
            sqlStringBuffer.append("select '").append(beginTime).append("' as type,IFNULL((select count(1) from " + businessview + " where DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and owner_user_id in (").append(userIds)
                    .append(")),0) as businessNum,IFNULL(sum(money),0) as businessMoney from lkcrm_crm_business  where DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and owner_user_id in (").append(userIds).append(")");
            if (i != cycleNum) {
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }

    public R sellFunnelList(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        String[] userIdss = userIds.split(",");
        Integer status = biTimeUtil.analyzeType(type);
        list = biDao.sellFunnelList(userIdss, status, startTime, endTime);
        return R.ok().put("data", list);
    }

    /**
     * 商机转化率分析
     *
     * @author Chacker
     */
    public R win(Integer deptId, Long userId, String type, String startTime, String endTime) {
        Record record = new Record();
        record.set("deptId", deptId).set("userId", userId).set("type", type).set("startTime", startTime).set("endTime", endTime);
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        List<Record> list = new ArrayList<>();
        if (StrUtil.isEmpty(userIds)) {
            return R.ok().put("data", list);
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        String businessview = BaseUtil.getViewSql("businessview");
        for (int i = 1; i <= cycleNum; i++) {
            sqlStringBuffer.append("select '").append(beginTime).append("' as type,IFNULL((select count(1) from " + businessview + " where DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("'and is_end = 1 and owner_user_id in (").append(userIds)
                    .append(")),0) as businessEnd,COUNT(1) as businessNum,").append(" IFNULL((select count(1) from " + businessview + " where DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("'and is_end = 1 and owner_user_id in (").append(userIds).
                    append(")) / COUNT(1),0 )").append(" as proportion ").
                    append(" from lkcrm_crm_business  where DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and owner_user_id in (").append(userIds).append(")");
            if (i != cycleNum) {
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> recordListMap = categoryDao.queryListBySql(sqlStringBuffer.toString());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordListMap);
        return R.ok().put("data", recordList);
    }
}
