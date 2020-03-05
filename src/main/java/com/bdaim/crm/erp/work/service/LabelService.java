package com.bdaim.crm.erp.work.service;

import com.bdaim.crm.dao.LkCrmWorkTaskLabelDao;
import com.bdaim.crm.entity.LkCrmWorkTaskLabelEntity;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("crmLabelService")
@Transactional
public class LabelService {
    @Resource
    private WorkbenchService workbenchService;

    @Resource
    private WorkService workService;
    @Resource
    private LkCrmWorkTaskLabelDao crmWorkTaskLabelDao;

    public R setLabel(LkCrmWorkTaskLabelEntity taskLabel) {
//        boolean bol;
        if (taskLabel.getLabelId() == null) {
            taskLabel.setCreateTime(new Timestamp(System.currentTimeMillis()));
            taskLabel.setCreateUserId(BaseUtil.getUser().getUserId());
//            bol = taskLable.save();
            crmWorkTaskLabelDao.save(taskLabel);
        } else {

//            bol = taskLable.update();
            crmWorkTaskLabelDao.update(taskLabel);
        }
        return R.ok();
    }

    public R deleteLabel(String labelId) {
//        Integer count = Db.queryInt("select count(*) from lkcrm_task where label_id like concat('%,',?,',%');", labelId);
        String sql = "select count(*) from lkcrm_task where label_id like concat('%,',?,',%')";
        int count = crmWorkTaskLabelDao.queryForInt(sql, labelId);
        if (count > 0) {
            return R.error("使用中的标签不能删除");
        }
        String delSql = "delete from lkcrm_work_task_label where label_id = ?";
//        Db.delete("delete from lkcrm_work_task_label where label_id = ?", labelId);
        crmWorkTaskLabelDao.executeUpdateSQL(delSql, labelId);
        return R.ok();
    }

    public R getLabelList() {
//        List<WorkTaskLabel> all = new WorkTaskLabel().dao().findAll();
        List<LkCrmWorkTaskLabelEntity> all = crmWorkTaskLabelDao.getAll();
        return R.ok().put("data", all);
    }

    public R queryById(Integer labelId) {
//        return R.ok().put("data", WorkTaskLabel.dao.findById(labelId));
        return R.ok().put("data", crmWorkTaskLabelDao.get(labelId));
    }

    /**
     * 标签任务列表
     */
    public R getTaskList(Integer labelId) {
//        List<Record> taskList = Db.find(Db.getSqlPara("work.label.queryTaskList", Kv.by("labelId", labelId).set("userId", BaseUtil.getUserId().intValue())));
        List<Record> taskList = JavaBeanUtil.mapToRecords(crmWorkTaskLabelDao.queryTaskList(labelId, BaseUtil.getUserId()));
        workbenchService.taskListTransfer(taskList);
        Map<Integer, List<Record>> map = taskList.stream().collect(Collectors.groupingBy(record -> record.getInt("work_id")));
//        List<Record> workList = Db.find(Db.getSqlPara("work.label.queryWorkList", Kv.by("labelId", labelId).set("userId", BaseUtil.getUserId().intValue())));
        List<Record> workList = JavaBeanUtil.mapToRecords(crmWorkTaskLabelDao.queryWorkList(labelId, BaseUtil.getUserId()));
        workList.forEach(work -> work.set("list", map.get(work.getInt("work_id"))));
        return R.ok().put("data", workList);
    }

    public R getLabelListByOwn() {
        Long userId = BaseUtil.getUserId();
//        List<String> labelIdList = Db.query("select label_id from `lkcrm_task` where create_user_id = ? or main_user_id = ? or owner_user_id like concat('%,',?,',%') and ishidden = 0", userId, userId, userId);
        String labelIdsSql = "select label_id from `lkcrm_task` where " +
                "create_user_id = ? or main_user_id = ? or owner_user_id like concat('%,',?,',%') and ishidden = 0";
        List<String> labelIdList = crmWorkTaskLabelDao.queryForList(labelIdsSql, userId, userId, userId);
        List<Integer> list = workService.toList(labelIdList);
//        List<String> collect = list.stream().map(Object::Integer).collect(Collectors.toList());
        List<LkCrmWorkTaskLabelEntity> resultList;
        if (AuthUtil.isWorkAdmin()) {
//            resultList = new WorkTaskLabel().dao().findAll();
            resultList = crmWorkTaskLabelDao.getAll();
        } else {
//            resultList = new WorkTaskLabel().find("select * from `lkcrm_work_task_label` where label_id in (?)", String.join(",", collect));
            resultList = crmWorkTaskLabelDao.findByIds(list);
        }
        return R.ok().put("data", resultList);
    }

}
