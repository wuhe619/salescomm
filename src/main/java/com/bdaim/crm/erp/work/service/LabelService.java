package com.bdaim.crm.erp.work.service;

import com.jfinal.aop.Inject;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.bdaim.crm.erp.work.entity.WorkTaskLabel;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service("crmLabelService")
@Transactional
public class LabelService{
    @Resource
    private WorkbenchService workbenchService;

    @Resource
    private WorkService workService;

    public R setLabel(WorkTaskLabel taskLable) {
        boolean bol;
        if (taskLable.getLabelId() == null) {
            taskLable.setCreateTime(new Date());
            taskLable.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            bol = taskLable.save();
        } else {

            bol = taskLable.update();
        }
        return bol ? R.ok() : R.error();
    }

    public R deleteLabel(String labelId){
        Integer count = Db.queryInt("select count(*) from 72crm_task where label_id like concat('%,',?,',%');", labelId);
        if(count>0){
            return R.error("使用中的标签不能删除");
        }
        Db.delete("delete from 72crm_work_task_label where label_id = ?",labelId);
        return R.ok();
    }

    public R getLabelList(){
        List<WorkTaskLabel> all = new WorkTaskLabel().dao().findAll();
        return R.ok().put("data",all);
    }

    public R queryById(Integer labelId){
        return R.ok().put("data",WorkTaskLabel.dao.findById(labelId));
    }

    /**
     * 标签任务列表
     */
    public R getTaskList(Integer labelId){
        List<Record> taskList = Db.find(Db.getSqlPara("work.label.queryTaskList", Kv.by("labelId",labelId).set("userId",BaseUtil.getUserId().intValue())));
        workbenchService.taskListTransfer(taskList);
        Map<Integer, List<Record>> map = taskList.stream().collect(Collectors.groupingBy(record -> record.getInt("work_id")));
        List<Record> workList = Db.find(Db.getSqlPara("work.label.queryWorkList", Kv.by("labelId",labelId).set("userId",BaseUtil.getUserId().intValue())));
        workList.forEach(work -> work.set("list",map.get(work.getInt("work_id"))));
        return R.ok().put("data",workList);
    }

    public R getLabelListByOwn(){
        Long userId = BaseUtil.getUserId();
        List<String> labelIdList = Db.query("select label_id from `72crm_task` where create_user_id = ? or main_user_id = ? or owner_user_id like concat('%,',?,',%') and ishidden = 0", userId, userId, userId);
        List<Integer> list = workService.toList(labelIdList);
        List<String> collect = list.stream().map(Object::toString).collect(Collectors.toList());
        List<WorkTaskLabel> resultList;
        if(AuthUtil.isWorkAdmin()){
            resultList = new WorkTaskLabel().dao().findAll();
        }else {
            resultList = new WorkTaskLabel().find("select * from `72crm_work_task_label` where label_id in (?)", String.join(",", collect));
        }
        return R.ok().put("data",resultList);
    }
}
