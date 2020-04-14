package com.bdaim.crm.erp.bi.service;


import cn.hutool.core.util.StrUtil;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.dao.LkCrmBiClueDao;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.crm.erp.work.entity.XStats;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@Transactional(rollbackFor = Exception.class)
public class BiClueService {
    private static final Logger LOGGER = LoggerFactory.getLogger(BiClueService.class);


    @Autowired
    private LkCrmBiClueDao lkCrmBiClueDao;

    @Autowired
    private BiTimeUtil biTimeUtil;

    /**
     * 线索总量统计图表
     *
     * @param xStats
     * @return
     */
    public ResponseInfo totalClueStats(XStats xStats) {
        return null;
    }

    /**
     * 线索详情统计列表  根据userId和开始、截止时间查询 分为is_transform【0】未转化 和【1】已转化
     *
     * @param xStats
     * @return
     */
    @SuppressWarnings("all")
    public ResponseInfo totalClueTable(XStats xStats) {
        Record record = new Record();
        record.set("deptId", xStats.getDeptId()).set("userId", xStats.getUserId()).set("type", xStats.getType())
                .set("startTime", xStats.getStartTime()).set("endTime", xStats.getEndTime());
        biTimeUtil.analyzeType(record);

        String userIds = record.getStr("userIds");
        if (StringUtil.isEmpty(userIds)) {
            return new ResponseInfoAssemble().success(new ArrayList<>());
        }
        String sqlDateFormat = record.getStr("sqlDateFormat");
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        String[] userIdsArr = userIds.split(",");
        StringBuffer sqlBuffer = new StringBuffer();
        String leadsview = BaseUtil.getViewSqlNotASName("leadsview");
        for (int i = 1; i <= userIdsArr.length; i++) {
            sqlBuffer.append("SELECT (SELECT realname FROM lkcrm_admin_user WHERE user_id='").append(userIdsArr[i - 1])
                    .append("') realname,COUNT(a.leads_id) AS leadsNum,IFNULL(SUM(a.is_transform),0) AS transformNum FROM ")
                    .append(leadsview).append(" AS a WHERE owner_user_id = ")
                    .append(userIdsArr[i - 1]).append(" AND DATE_FORMAT(create_time,'").append(sqlDateFormat)
                    .append("') BETWEEN '").append(beginTime).append("' AND '").append(finalTime).append("' ");
            if (i != userIdsArr.length) {
                sqlBuffer.append(" UNION ALL ");
            }
        }
        LOGGER.info("SQL is {}", sqlBuffer.toString());
        List<Map<String, Object>> result = lkCrmBiClueDao.queryListBySql(sqlBuffer.toString());
        result.forEach(r -> {
            int leadsNum = Integer.parseInt(String.valueOf(r.get("leadsNum")));
            int transformNum = Integer.parseInt(String.valueOf(r.get("transformNum")));
            r.put("transformedRate", leadsNum != 0 ? transformNum * 100 / leadsNum : 0);
        });
        return new ResponseInfoAssemble().success(result);
    }

    /**
     * 线索跟进次数统计 图表
     *
     * @param xStats
     * @return
     */
    @SuppressWarnings("all")
    public ResponseInfo clueRecordStats(XStats xStats) {
        Record record = new Record();
        record.set("deptId", xStats.getDeptId()).set("userId", xStats.getUserId()).set("type", xStats.getType())
                .set("startTime", xStats.getStartTime()).set("endTime", xStats.getEndTime());
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i = 1; i <= cycleNum; i++) {
            sqlStringBuffer.append("select '").append(beginTime).append("' as type,IFNULL((select count(record_id) from lkcrm_admin_record where DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and types = 'crm_leads' and cust_id='")
                    .append(BaseUtil.getCustId()).append("' and create_user_id in (").append(userIds)
                    .append(")),0) as recordCount,IFNULL(count(DISTINCT types_id),0) as customerCount from lkcrm_admin_record where DATE_FORMAT(create_time,'")
                    .append(sqlDateFormat).append("') = '").append(beginTime).append("' and types = 'crm_leads' and cust_id='")
                    .append(BaseUtil.getCustId()).append("' and create_user_id in (").append(userIds).append(")");
            if (i != cycleNum) {
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> resultList = lkCrmBiClueDao.queryListBySql(sqlStringBuffer.toString());
        return new ResponseInfoAssemble().success(resultList);
    }

    /**
     * 线索跟进次数统计 列表
     *
     * @param xStats
     * @return
     */
    @SuppressWarnings("all")
    public ResponseInfo clueRecordTable(XStats xStats) {
        Record record = new Record();
        record.set("deptId", xStats.getDeptId()).set("userId", xStats.getUserId()).set("type", xStats.getType())
                .set("startTime", xStats.getStartTime()).set("endTime", xStats.getEndTime());
        biTimeUtil.analyzeType(record);

        String userIds = record.getStr("userIds");
        if (StringUtil.isEmpty(userIds)) {
            return new ResponseInfoAssemble().success(new ArrayList<>());
        }
        String sqlDateFormat = record.getStr("sqlDateFormat");
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        String[] userIdsArr = userIds.split(",");
        StringBuffer sqlBuffer = new StringBuffer();
        for (int i = 1; i <= userIdsArr.length; i++) {
            sqlBuffer.append("SELECT b.realname,IFNULL(count(a.record_id),0) as recordCount,IFNULL(count(DISTINCT a.types_id),0) " +
                    "as leadsCount FROM lkcrm_admin_record as a LEFT JOIN lkcrm_admin_user as b on a.create_user_id = b.user_id where DATE_FORMAT(a.create_time,'")
                    .append(sqlDateFormat).append("') between '").append(beginTime).append("' and '").append(finalTime)
                    .append("' and a.cust_id='").append(BaseUtil.getCustId())
                    .append("' and a.types = 'crm_leads' and b.user_id = ").append(userIdsArr[i - 1]);
            if (i != userIdsArr.length) {
                sqlBuffer.append(" union all ");
            }
        }
        LOGGER.info("SQL is {}", sqlBuffer.toString());
        List<Map<String, Object>> result = lkCrmBiClueDao.queryListBySql(sqlBuffer.toString());
        return new ResponseInfoAssemble().success(result);
    }

    /**
     * 跟进方式统计
     *
     * @param xStats
     * @return
     */
    @SuppressWarnings("all")
    public ResponseInfo clueRecordCategoryStats(XStats xStats) {
        Record record = new Record();
        record.set("deptId", xStats.getDeptId()).set("userId", xStats.getUserId()).set("type", xStats.getType())
                .set("startTime", xStats.getStartTime()).set("endTime", xStats.getEndTime());
        biTimeUtil.analyzeType(record);
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        Integer finalTime = record.getInt("finalTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        sqlStringBuffer.append("select category,IFNULL(count(record_id),0) as recordNum," +
                "IFNULL(count(record_id)*100/(select count(*) from lkcrm_admin_record where cust_id='")
                .append(BaseUtil.getCustId()).append("' and DATE_FORMAT(create_time,'")
                .append(sqlDateFormat).append("') between '").append(beginTime).append("' and '")
                .append(finalTime).append("' and types='crm_leads' and create_user_id in (")
                .append(userIds).append(")),0) as proportion from lkcrm_admin_record where cust_id='")
                .append(BaseUtil.getCustId()).append("' and (DATE_FORMAT(create_time,'")
                .append(sqlDateFormat)
                .append("') between '").append(beginTime).append("' and '").append(finalTime)
                .append("') and create_user_id in (").append(userIds)
                .append(") and types='crm_leads' group by category");
        List<Map<String, Object>> resultList = lkCrmBiClueDao.queryListBySql(sqlStringBuffer.toString());
        return new ResponseInfoAssemble().success(resultList);
    }

    /**
     * 公海统计
     *
     * @param xStats
     * @return
     */
    public ResponseInfo poolTable(XStats xStats) {
        Record record = new Record();
        record.set("deptId", xStats.getDeptId()).set("userId", xStats.getUserId()).set("type", xStats.getType())
                .set("startTime", xStats.getStartTime()).set("endTime", xStats.getEndTime());
        biTimeUtil.analyzeType(record);
        List<Map<String, Object>> resultList = lkCrmBiClueDao.poolTable(record);
        return new ResponseInfoAssemble().success(resultList);
    }

    /**
     * 公海统计
     *
     * @param xStats
     * @return
     */
    @SuppressWarnings("all")
    public ResponseInfo poolStats(XStats xStats) {
        Record record = new Record();
        record.set("deptId", xStats.getDeptId()).set("userId", xStats.getUserId()).set("type", xStats.getType())
                .set("startTime", xStats.getStartTime()).set("endTime", xStats.getEndTime());
        biTimeUtil.analyzeType(record);
        Integer cycleNum = record.getInt("cycleNum");
        String sqlDateFormat = record.getStr("sqlDateFormat");
        String userIds = record.getStr("userIds");
        if (StrUtil.isEmpty(userIds)) {
            userIds = "0";
        }
        Integer beginTime = record.getInt("beginTime");
        StringBuffer sqlStringBuffer = new StringBuffer();
        for (int i = 1; i <= cycleNum; i++) {
            sqlStringBuffer.append("select '").append(beginTime).append("' as type,count(type_id) as putInNum,(select count(type_id)" +
                    " from lkcrm_crm_owner_record where DATE_FORMAT(create_time,'").append(sqlDateFormat).append("') = '")
                    .append(beginTime).append("' and type = 9 and post_owner_user_id in (").append(userIds).append(")) as receiveNum " +
                    "from lkcrm_crm_owner_record where DATE_FORMAT(create_time,'").append(sqlDateFormat).append("') = '")
                    .append(beginTime).append("' and type = 9 and pre_owner_user_id in (").append(userIds).append(")");
            if (i != cycleNum) {
                sqlStringBuffer.append(" union all ");
            }
            beginTime = biTimeUtil.estimateTime(beginTime);
        }
        List<Map<String, Object>> resultList = lkCrmBiClueDao.queryListBySql(sqlStringBuffer.toString());
        return new ResponseInfoAssemble().success(resultList);
    }
}
