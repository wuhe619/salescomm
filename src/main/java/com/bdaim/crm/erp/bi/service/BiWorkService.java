package com.bdaim.crm.erp.bi.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.dao.LkCrmBiDao;
import com.bdaim.crm.dao.LkCrmOaExamineCategoryDao;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.bi.common.BiTimeUtil;
import com.bdaim.crm.erp.oa.service.OaExamineService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class BiWorkService {
    @Resource
    private BiTimeUtil biTimeUtil;
    @Autowired
    private LkCrmOaExamineCategoryDao categoryDao;
    @Autowired
    private LkCrmBiDao biDao;
    @Autowired
    private OaExamineService oaExamineService;


    /**
     * 查询日志统计信息
     *
     * @author Chacker
     */
    public List<Record> logStatistics(Integer deptId, Long userId, String type) {
        Record record = new Record().set("deptId", deptId).set("userId", userId).set("type", type);
        biTimeUtil.analyzeType(record);
        List<Record> records = new ArrayList<>();
        for (String uid : StrUtil.splitTrim(record.getStr("userIds"), ",")) {
            List<Map<String, Object>> recordMaps = biDao.queryLogByUser(record.get("sqlDateFormat"),
                    record.get("beginTime"), record.get("finalTime"), uid);
            List<Record> recordList = JavaBeanUtil.mapToRecords(recordMaps);
            if (recordList.size() > 0) {
                Record userRecord = new Record().setColumns(recordList.get(0)).remove("sum", "send_user_ids", "read_user_ids");
                int commentCount = 0, unCommentCount = 0, unReadCont = 0, count = recordList.size();
                for (Record task : recordList) {
                    if (task.getInt("sum") > 0) {
                        commentCount++;
                    } else {
                        unCommentCount++;
                    }
                    String sendUser = task.getStr("send_user_ids");
                    if (StrUtil.isNotEmpty(sendUser) && sendUser.split(",").length > 0) {
                        if (!isIntersection(StrUtil.splitTrim(sendUser, ","), StrUtil.splitTrim(record.getStr("read_user_ids"), ","))) {
                            unReadCont++;
                        }
                    }
                }
                userRecord.set("commentCount", commentCount)
                        .set("unCommentCount", unCommentCount)
                        .set("unReadCont", unReadCont)
                        .set("count", count);
                records.add(userRecord);
            }
        }
        return records;
    }

    /**
     * 查询审批统计信息
     *
     * @author Chacker
     */
    public JSONObject examineStatistics(Integer deptId, Long userId, String type) {
        JSONObject object = new JSONObject();
        Record record = new Record().set("deptId", deptId).set("userId", userId).set("type", type);
        biTimeUtil.analyzeType(record);
        String categoryListSql = "SELECT category_id,title FROM lkcrm_oa_examine_category WHERE 1=1 AND cust_id=?";
        List<Map<String, Object>> categoryListMap = biDao.queryListBySql(categoryListSql, BaseUtil.getCustId());
        List<Record> categoryList = JavaBeanUtil.mapToRecords(categoryListMap);
        object.put("categoryList", categoryList);
        List<String> users = StrUtil.splitTrim(record.getStr("userIds"), ",");
        if (users.size() == 0) {
            object.put("userList", users);
        } else {
            List<Map<String, Object>> userMaps = biDao.examineStatistics(categoryList, users, record.get("sqlDateFormat"),
                    record.get("beginTime"), record.get("finalTime"));
            List<Record> userList = JavaBeanUtil.mapToRecords(userMaps);
            object.put("userList", userList);
        }
        return object;
    }

    /**
     * 审批详情
     *
     * @author Chacker
     */
    public Record examineInfo(BasePageRequest request) {
        JSONObject jsonObject = request.getJsonObject();
        Record record = new Record().set("userId", jsonObject.getInteger("userId")).set("type", jsonObject.get("type"));
        biTimeUtil.analyzeType(record);
        Page page = biDao.myInitiate(jsonObject.get("userId"), jsonObject.get("categoryId"),
                record.get("beginDate"), record.get("endDate"),
                request.getPage(), request.getLimit());
        List<Record> recordList = page.getData();
        oaExamineService.transfer(recordList);
        Map<String, Object> map = biDao.queryExamineCount(jsonObject.get("categoryId"), record.get("beginDate"),
                record.get("endDate"), jsonObject.get("userId"));
        Record info = JavaBeanUtil.mapToRecord(map);
        info.set("list", page.getData()).set("totalRow", page.getTotal());
        return info;
    }


    /**
     * 判断两个数组是否有交集
     *
     * @return true为存在交集
     * @author Chacker
     */
    private static boolean isIntersection(List<String> m, List<String> n) {
        // 将较长的数组转换为set
        Set<String> set = new HashSet<>(m.size() > n.size() ? m : n);

        for (String i : m.size() > n.size() ? n : m) {
            if (set.contains(i)) {
                return true;
            }
        }
        return false;
    }
}
