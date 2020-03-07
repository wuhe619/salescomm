package com.bdaim.crm.erp.work.service;

import com.bdaim.crm.dao.LkCrmTaskDao;
import com.bdaim.crm.dao.LkCrmWorkDao;
import com.bdaim.crm.entity.LkCrmTaskEntity;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.bdaim.crm.erp.work.entity.Task;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;

@Service
@Transactional
public class TrashService {
    @Resource
    private WorkbenchService workbenchService;
    @Autowired
    private LkCrmWorkDao workDao;
    @Autowired
    private LkCrmTaskDao taskDao;

    /**
     * 回收站列表
     */
    public R queryList() {
        List<Record> recordList;
        if (AuthUtil.isWorkAdmin()) {
//            recordList = Db.find(Db.getSqlPara("work.trash.queryList"));
            recordList = JavaBeanUtil.mapToRecords(workDao.queryTrashList());
        } else {
//            recordList = Db.find(Db.getSqlPara("work.trash.queryList", Kv.by("userId", BaseUtil.getUserId().intValue())));
            recordList = JavaBeanUtil.mapToRecords(workDao.queryTrashListByUserId(BaseUtil.getUserId()));
        }
        workbenchService.taskListTransfer(recordList);
        return R.ok().put("data", recordList);
    }

    @Before(Tx.class)
    public R deleteTask(Integer taskId) {
//        Task task = new Task().dao().findById(taskId);
        LkCrmTaskEntity task = taskDao.get(taskId);
        if (task == null) {
            return R.error("任务不存在！");
        }
//        int userId = BaseUtil.getUser().getUserId().intValue();
//        if(task.getCreateUserId() != userId && (task.getMainUserId()!=null && task.getMainUserId() != userId)){
//            return R.error("您无权删除任务！");
//        }
        if (task.getIshidden() != 1) {
            return R.error("任务不在回收站！");
        }
//        return task.delete()?R.ok():R.error();
        taskDao.delete(task);
        return R.ok();
    }

    public R restore(Integer taskId) {
//        Task task = new Task().dao().findById(taskId);
        LkCrmTaskEntity task = taskDao.get(taskId);
        if (task == null) {
            return R.error("任务不存在！");
        }
        if (task.getIshidden() != 1) {
            return R.error("任务不在回收站！");
        }
//        Integer count = Db.queryInt("select count(*) from `lkcrm_work_task_class` where class_id = ?", task.getClassId());
        String countSql = "select count(*) from `lkcrm_work_task_class` where class_id = ?";
        Integer count = taskDao.queryForInt(countSql, task.getClassId());
        int update;
        if (count > 0) {
            String updateSql = "update lkcrm_task set ishidden = 0,hidden_time = null where task_id = ?";
            update = taskDao.executeUpdateSQL(updateSql, taskId);
//            update = Db.update("update lkcrm_task set ishidden = 0,hidden_time = null where task_id = ?", taskId);
        } else {
//            update = Db.update("update lkcrm_task set ishidden = 0,class_id = null,hidden_time = null where task_id = ?", taskId);
            String updateSql = "update lkcrm_task set ishidden = 0,class_id = null,hidden_time = null where task_id = ?";
            update = taskDao.executeUpdateSQL(updateSql, taskId);
        }
        return update > 0 ? R.ok() : R.error();
    }
}
