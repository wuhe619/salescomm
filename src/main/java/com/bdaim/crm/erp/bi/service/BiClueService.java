package com.bdaim.crm.erp.bi.service;


import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.dao.LkCrmBiClueDao;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.crm.erp.work.entity.XStats;
import com.bdaim.crm.utils.BaseUtil;
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
     * 线索详情统计列表
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
    public ResponseInfo clueRecordStats(XStats xStats) {
        return null;
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
            sqlBuffer.append("SELECT (SELECT realname FROM lkcrm_admin_user WHERE user_id='").append(userIdsArr[i - 1])
                    .append("') realname");
            if (i != userIdsArr.length) {
                sqlBuffer.append(" UNION ALL ");
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
    public ResponseInfo clueRecordCategoryStats(XStats xStats) {
        return null;
    }

    /**
     * 公海统计
     *
     * @param xStats
     * @return
     */
    public ResponseInfo poolTable(XStats xStats) {
        return null;
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
            sqlBuffer.append("SELECT (SELECT realname FROM lkcrm_admin_user WHERE user_id='").append(userIdsArr[i - 1])
                    .append("') realname");
            if (i != userIdsArr.length) {
                sqlBuffer.append(" UNION ALL ");
            }
        }
        LOGGER.info("SQL is {}", sqlBuffer.toString());
        List<Map<String, Object>> result = lkCrmBiClueDao.queryListBySql(sqlBuffer.toString());
        return new ResponseInfoAssemble().success(result);
    }
}
