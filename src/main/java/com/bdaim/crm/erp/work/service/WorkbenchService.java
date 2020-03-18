package com.bdaim.crm.erp.work.service;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.dao.LkCrmTaskDao;
import com.bdaim.crm.dao.LkCrmTaskRelationDao;
import com.bdaim.crm.dao.LkCrmWorkbenchDao;
import com.bdaim.crm.entity.LkCrmTaskRelationEntity;
import com.bdaim.crm.erp.work.entity.Task;
import com.bdaim.crm.erp.work.entity.TaskRelation;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class WorkbenchService {

    @Resource
    private TaskService taskService;
    @Resource
    private LkCrmWorkbenchDao workbenchDao;
    @Resource
    private LkCrmTaskDao taskDao;

    @Resource
    private LkCrmTaskRelationDao crmTaskRelationDao;

    public R myTask(Long userId) {
        List<Record> result = new ArrayList<>();
        result.add(new Record().set("title", "任务池").set("is_top", 0).set("count", 0).set("list", new ArrayList<>()));
        result.add(new Record().set("title", "今日任务").set("is_top", 1).set("count", 0).set("list", new ArrayList<>()));
        result.add(new Record().set("title", "进行中").set("is_top", 2).set("count", 0).set("list", new ArrayList<>()));
        result.add(new Record().set("title", "已完成").set("is_top", 3).set("count", 0).set("list", new ArrayList<>()));
        result.forEach(record -> {
            Integer isTop = record.getInt("is_top");
//            List<Record> resultist = Db.find(Db.getSqlPara("work.workbench.myTask", Kv.by("userId", userId).set("isTop", isTop)));
            List<Record> resultist = JavaBeanUtil.mapToRecords(workbenchDao.myTask(userId, isTop));
            record.set("count", resultist.size());
            if (resultist.size() != 0) {
                resultist.sort(Comparator.comparingInt(o -> o.getInt("top_order_num")));
                taskListTransfer(resultist);
                record.set("list", resultist);
            }

        });
        return R.ok().put("data", result);
    }

    public void taskListTransfer(List<Record> taskList) {
        taskList.forEach(task -> {
            Date stopTime = task.getDate("stop_time");
            if (stopTime != null) {
                if (stopTime.getTime() < System.currentTimeMillis()) {
                    task.set("isEnd", 1);
                } else {
                    task.set("isEnd", 0);
                }
            } else {
                task.set("isEnd", 0);
            }
            Integer taskId = task.getInt("task_id");
//            task.set("mainUser", Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?", task.getInt("main_user_id")));
            task.set("mainUser", workbenchDao.getMainUser(task.getLong("main_user_id")));
//            task.set("createUser", Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?", task.getInt("create_user_id")));
            task.set("createUser", workbenchDao.getCreateUser(task.getLong("create_user_id")));
            ArrayList<Record> labelList = new ArrayList<>();
            if (StrUtil.isNotBlank(task.getStr("label_id"))) {
                String[] lableIds = task.getStr("label_id").split(",");
                for (String lableId : lableIds) {
                    if (StrUtil.isNotBlank(lableId)) {
//                        Record lable = Db.findFirst("select label_id,name as labelName,color from lkcrm_work_task_label where label_id = ?", lableId);
                        Record lable = JavaBeanUtil.mapToRecord(workbenchDao.getLableById(lableId));
                        labelList.add(lable);
                    }
                }
            }
            task.set("labelList", labelList);
            List<LkCrmTaskRelationEntity> list = crmTaskRelationDao.find("FROM LkCrmTaskRelationEntity WHERE taskId = ? ", taskId);
            int relationCount = 0;
            if (list.size() > 0) {
                LkCrmTaskRelationEntity taskRelation = list.get(0);
                relationCount += TagUtil.toSet(taskRelation.getBusinessIds()).size();
                relationCount += TagUtil.toSet(taskRelation.getContactsIds()).size();
                relationCount += TagUtil.toSet(taskRelation.getCustomerIds()).size();
                relationCount += TagUtil.toSet(taskRelation.getContractIds()).size();
                relationCount += TagUtil.toSet(taskRelation.getLeadsIds()).size();
            }
            task.set("relationCount", relationCount);
        });
    }

    public R dateList(String startTime, String endTime) {
//        List<Task> taskList = Task.dao.find(Db.getSqlPara("work.workbench.dateList", Kv.by("userId", BaseUtil.getUser().getUserId()).set("startTime", startTime).set("endTime", endTime)));
        List<Map<String, Object>> taskList = taskDao.dateList(BaseUtil.getUser().getUserId(), startTime, endTime);
        return R.ok().put("data", taskList);
    }

    @Before(Tx.class)
    public R updateTop(JSONObject jsonObject) {
        String updateSql = "update `lkcrm_task` set is_top = ?,top_order_num = ? where task_id = ?";
        if (jsonObject.containsKey("fromList")) {
            JSONArray fromlist = jsonObject.getJSONArray("fromList");
            Integer fromTopId = jsonObject.getInteger("fromTopId");
            for (int i = 1; i <= fromlist.size(); i++) {
//                Db.update(updateSql, fromTopId, i, fromlist.get(i - 1));
                workbenchDao.updateFromToTop(updateSql, fromTopId, i, fromlist.get(i - 1));
            }
        }
        if (jsonObject.containsKey("toList")) {
            JSONArray tolist = jsonObject.getJSONArray("toList");
            Integer toTopId = jsonObject.getInteger("toTopId");
            for (int i = 1; i <= tolist.size(); i++) {
//                Db.update(updateSql, toTopId, i, tolist.get(i - 1));
                workbenchDao.updateFromToTop(updateSql, toTopId, i, tolist.get(i - 1));
            }
        }
        return R.ok();
    }
}
