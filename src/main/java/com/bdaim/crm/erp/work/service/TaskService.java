package com.bdaim.crm.erp.work.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.entity.*;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.service.OaActionRecordService;
import com.bdaim.crm.erp.work.entity.Task;
import com.bdaim.crm.erp.work.entity.TaskRelation;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class TaskService {
    //添加日志
    @Autowired
    private OaActionRecordService oaActionRecordService;

    @Autowired
    private AdminFileService adminFileService;

    @Autowired
    private LkCrmTaskDao taskDao;

    @Autowired
    private LkCrmWorkTaskLabelDao crmWorkTaskLabelDao;

    @Autowired
    private CustomerUserDao customerUserDao;
    @Autowired
    private LkCrmWorkDao workDao;
    @Autowired
    private LkCrmWorkTaskClassDao taskClassDao;
    @Autowired
    private LkCrmTaskRelationDao taskRelationDao;
    @Autowired
    private LkCrmActionRecordDao crmActionRecordDao;

    public R setTaskClass(LkCrmWorkTaskClassEntity taskClass) {
//        boolean bol = true;
        if (taskClass.getClassId() == null) {
//            Work work = new Work().findById(taskClass.getWorkId());
            LkCrmWorkEntity work = workDao.get(taskClass.getWorkId());
            Integer isOpen = work.getIsOpen();
            if (isOpen == 0 && !AuthUtil.isWorkAuth(taskClass.getWorkId().toString(), "taskClass:save")) {
                return R.noAuth();
            }
            String sql = "select max(order_num) from `lkcrm_work_task_class` where work_id = ?";
            Integer orderNum = workDao.queryForInt(sql, taskClass.getWorkId());
//            Integer orderNum = Db.queryInt("select max(order_num) from `lkcrm_work_task_class` where work_id = ?", taskClass.getWorkId());
            taskClass.setOrderNum(orderNum + 1);
            taskClass.setCreateUserId(BaseUtil.getUser().getUserId());
            taskClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
//            bol = taskClass.save();
            taskClassDao.save(taskClass);

        } else {
            String sql = "select work_id from `lkcrm_work_task_class` where class_id = ?";
//            Integer workId = Db.queryInt("select work_id from `lkcrm_work_task_class` where class_id = ?", taskClass.getClassId());
            Integer workId = workDao.queryForInt(sql, taskClass.getClassId());
            if (!AuthUtil.isWorkAuth(workId.toString(), "taskClass:update")) {
                return R.noAuth();
            }
//            bol = taskClass.update();
            taskClassDao.saveOrUpdate(taskClass);
        }
        return R.isSuccess(true);
    }


    public void changeOrderTaskClass(String originalClassId, String targetClassId) {
//        WorkTaskClass originalClass = WorkTaskClass.dao.findById(originalClassId);
        LkCrmWorkTaskClassEntity originalClass = taskClassDao.get(Integer.parseInt(originalClassId));
//        WorkTaskClass targetClass = WorkTaskClass.dao.findById(targetClassId);
        LkCrmWorkTaskClassEntity targetClass = taskClassDao.get(Integer.parseInt(targetClassId));
        Integer originalClassOrderId = originalClass.getOrderNum();
        Integer targetClassOrderId = targetClass.getOrderNum();
//        Db.update("update lkcrm_work_task_class setUser order_id = ? where class_id = ?", originalClassOrderId, targetClassId);
//        Db.update("update lkcrm_work_task_class setUser order_id = ? where class_id = ?", targetClassOrderId, originalClassId);
        String updateSql1 = "update lkcrm_work_task_class setUser order_id = ? where class_id = ?";
        taskClassDao.executeUpdateSQL(updateSql1, originalClassOrderId, targetClassId);
        String updateSql2 = "update lkcrm_work_task_class setUser order_id = ? where class_id = ?";
        taskClassDao.executeUpdateSQL(updateSql2, targetClassOrderId, originalClassId);
    }

    @Before(Tx.class)
    public R setTask(LkCrmTaskEntity task, LkCrmTaskRelationEntity taskRelation) {
        LoginUser user = BaseUtil.getUser();
        task.setCustId(user.getCustId());
        boolean bol;
        if (task.getLabelId() != null) {
            task.setLabelId(TagUtil.fromString(task.getLabelId()));
        }
        if (task.getTaskId() == null) {
            if (task.getMainUserId() == null) {
                task.setMainUserId(user.getUserId());
            }
            if (task.getOwnerUserId() != null) {
                Set<Integer> ownerUserId = TagUtil.toSet(task.getOwnerUserId());
                ownerUserId.add(user.getUserId().intValue());
                task.setOwnerUserId(TagUtil.fromSet(ownerUserId));
            } else {
                task.setOwnerUserId("," + user.getUserId() + ",");
            }
            task.setCreateTime(DateUtil.date().toTimestamp());
            task.setUpdateTime(DateUtil.date().toTimestamp());
            task.setCreateUserId(user.getUserId());
            if (task.getPid() == null) {
                task.setPid(0);
            }
            if (task.getIshidden() == null) {
                task.setIshidden(0);
            }
            if (task.getIsArchive() == null) {
                task.setIsArchive(0);
            }
            if (task.getStatus() == null) {
                task.setStatus(1);
            }
            task.setBatchId(IdUtil.simpleUUID());
            bol = (int) taskDao.saveReturnPk(task) > 0;
            LkCrmWorkTaskLogEntity workTaskLog = new LkCrmWorkTaskLogEntity();
            workTaskLog.setUserId(user.getUserId());
            workTaskLog.setTaskId(task.getTaskId());
            workTaskLog.setContent("添加了新任务 " + task.getName());
            saveWorkTaskLog(workTaskLog);

        } else {
            task.setUpdateTime(DateUtil.date().toTimestamp());
            bol = getWorkTaskLog(task, user.getUserId());
        }
        if (taskRelation.getBusinessIds() != null || taskRelation.getContactsIds() != null || taskRelation.getContractIds() != null || taskRelation.getCustomerIds() != null) {
//            Db.deleteById("lkcrm_task_relation", "task_id", task.getTaskId());
            taskRelationDao.executeUpdateSQL("delete from lkcrm_task_relation where task_id = ? ", task.getTaskId());
            taskRelation.setCreateTime(DateUtil.date().toTimestamp());
            taskRelation.setTaskId(task.getTaskId());
            //taskRelation.save();
            taskDao.saveOrUpdate(taskRelation);
        }
        task.getMainUserId();
        oaActionRecordService.addRecord(task.getTaskId(), OaEnum.TASK_TYPE_KEY.getTypes(), task.getUpdateTime() == null ? 1 : 2, oaActionRecordService.getJoinIds(user.getUserId().intValue(), getJoinUserIds(task)), oaActionRecordService.getJoinIds(user.getDeptId(), ""));
        return bol ? R.ok().put("data", Kv.by("task_id", task.getTaskId())) : R.error();
    }

    private String getJoinUserIds(LkCrmTaskEntity task) {
        StringBuilder joinUserIds = new StringBuilder(",");
        if (task.getMainUserId() != null) {
            joinUserIds.append(task.getMainUserId()).append(",");
        }
        if (StrUtil.isNotEmpty(task.getOwnerUserId())) {
            joinUserIds.append(task.getOwnerUserId());
        }
        return joinUserIds.toString();
    }


    public R queryTaskInfo(String taskId) {
        Record mainTask = transfer(taskId);
        adminFileService.queryByBatchId(mainTask.get("batch_id"), mainTask);
//        List<Record> recordList = Db.find("select task_id from lkcrm_task where pid = ?", taskId);
        String sql = "select task_id from lkcrm_task where pid = ?";
        List<Record> recordList = JavaBeanUtil.mapToRecords(taskClassDao.sqlQuery(sql, taskId));
        List<Record> childTaskList = new ArrayList<>();
        if (recordList != null && recordList.size() > 0) {
            recordList.forEach(childTaskRecord -> {
                String childTaskId = childTaskRecord.getStr("task_id");
                Record childTask = transfer(childTaskId);
                adminFileService.queryByBatchId(childTask.getStr("batch_id"), childTask);
                childTaskList.add(childTask);
            });
        }
        mainTask.set("childTask", childTaskList);
        return R.ok().put("data", mainTask);
    }

    private Record transfer(String taskId) {
        String sql = "select a.*,b.name as workName from lkcrm_task a left join `lkcrm_work` b on a.work_id " +
                "= b.work_id where task_id = ?";
//        Record task = Db.findFirst("select a.*,b.name as workName from lkcrm_task a left join `lkcrm_work` b on a.work_id = b.work_id where task_id = ?", taskId);
        Record task = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(sql, taskId));
        task.set("stop_time", DateUtil.formatDate(task.getDate("stop_time")));
//        task.set("mainUser", Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?", task.getInt("main_user_id")));
        String getMainUserSql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
        Record mainUser = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(getMainUserSql, task.getInt("main_user_id")));
        task.set("mainUser", mainUser);
//        task.set("createUser", Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?", task.getInt("create_user_id")));
        Record createUser = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(getMainUserSql, task.getInt("create_user_id")));
        task.set("createUser", createUser);
        ArrayList<Record> labelList = new ArrayList<>();
        ArrayList<Record> ownerUserList = new ArrayList<>();
        if (StrUtil.isNotBlank(task.getStr("label_id"))) {
            String[] labelIds = task.getStr("label_id").split(",");
            for (String labelId : labelIds) {
                if (StrUtil.isNotBlank(labelId)) {
//                    Record label = Db.findFirst("select label_id,name as labelName,color from lkcrm_work_task_label where label_id = ?", labelId);
                    String findTaskLabelSql = "select label_id,name as labelName,color from lkcrm_work_task_label where label_id = ?";
                    Record label = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(findTaskLabelSql, labelId));
                    labelList.add(label);
                }
            }
        }
        if (StrUtil.isNotBlank(task.getStr("owner_user_id"))) {
            String[] ownerUserIds = task.getStr("owner_user_id").split(",");
            for (String ownerUserId : ownerUserIds) {
                if (StrUtil.isNotBlank(ownerUserId)) {
//                    Record ownerUser = Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?", ownerUserId);
                    String findOwnerUserSql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
                    Record ownerUser = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(findOwnerUserSql, ownerUserId));
                    if (ownerUser != null && ownerUser.getColumns() != null && ownerUser.getColumns().size() > 0) {
                        ownerUserList.add(ownerUser);
                    }
                }
            }
        }
//        Record relation = Db.findFirst("select * FROM lkcrm_task_relation where task_id = ?", taskId);
        String relationSql = "select * FROM lkcrm_task_relation where task_id = ?";
        Record relation = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(relationSql, taskId));
        List<Record> customerList = new ArrayList<>();
        List<Record> contactsList = new ArrayList<>();
        List<Record> businessList = new ArrayList<>();
        List<Record> contractList = new ArrayList<>();
        List<Record> leadsList = new ArrayList<>();
        if (relation != null) {
            if (StrUtil.isNotBlank(relation.getStr("customer_ids"))) {
                String[] customerIds = relation.getStr("customer_ids").split(",");
                for (String customerId : customerIds) {
                    if (StrUtil.isNotBlank(customerId)) {
//                        Record customer = Db.findFirst("select customer_id,customer_name  from lkcrm_crm_customer where customer_id = ?", customerId);
                        String customerSql = "select customer_id,customer_name  from lkcrm_crm_customer where customer_id = ?";
                        Record customer = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(customerSql, customerId));
                        if (customer != null) {
                            customerList.add(customer);
                        }
                    }
                }
            }

            if (StrUtil.isNotBlank(relation.getStr("contacts_ids"))) {
                String[] contactsIds = relation.getStr("contacts_ids").split(",");

                for (String contactsId : contactsIds) {
                    if (StrUtil.isNotBlank(contactsId)) {
                        String contactsSql = "select contacts_id,name from lkcrm_crm_contacts  where contacts_id = ?";
                        Record contacts = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(contactsSql, contactsId));
//                        Record contacts = Db.findFirst("select contacts_id,name from lkcrm_crm_contacts  where contacts_id = ?", contactsId);
                        if (contacts != null) {
                            contactsList.add(contacts);
                        }
                    }
                }
            }
            if (StrUtil.isNotBlank(relation.getStr("business_ids"))) {
                String[] businessIds = relation.getStr("business_ids").split(",");

                for (String businessId : businessIds) {
                    if (StrUtil.isNotBlank(businessId)) {
                        String businessSql = "select business_id,business_name  from lkcrm_crm_business  where business_id = ?";
//                        Record business = Db.findFirst("select business_id,business_name  from lkcrm_crm_business  where business_id = ?", businessId);
                        Record business = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(businessSql, businessId));
                        if (business != null) {
                            businessList.add(business);
                        }
                    }
                }
            }
            if (StrUtil.isNotBlank(relation.getStr("contract_ids"))) {
                String[] contractIds = relation.getStr("contract_ids").split(",");
                for (String contractId : contractIds) {
                    if (StrUtil.isNotBlank(contractId)) {
//                        Record contract = Db.findFirst("select contract_id,name from lkcrm_crm_contract  where contract_id = ?", contractId);
                        String contractSql = "select contract_id,name from lkcrm_crm_contract  where contract_id = ?";
                        Record contract = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(contractSql, contractId));
                        if (contract != null) {
                            contractList.add(contract);
                        }
                    }
                }
                task.set("contractList", contractList);
            }

            if (StrUtil.isNotBlank(relation.getStr("leads_ids"))) {
                String[] leadsIds = relation.getStr("leads_ids").split(",");

                for (String leadsId : leadsIds) {
                    if (StrUtil.isNotBlank(leadsId)) {
                        String businessSql = "select leads_id,leads_name  from lkcrm_crm_leads  where leads_id = ?";
                        Record lead = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(businessSql, leadsId));
                        if (lead != null) {
                            leadsList.add(lead);
                        }
                    }
                }
            }
        }
        task.set("customerList", customerList);
        task.set("contactsList", contactsList);
        task.set("businessList", businessList);
        task.set("contractList", contractList);
        task.set("leadsList", leadsList);

        return task.set("labelList", labelList).set("ownerUserList", ownerUserList);
    }


    /**
     * 查询任务列表
     */
    public R getTaskList(Integer type, Integer status, Integer priority, Integer date, List<Long> userIds, BasePageRequest<Task> basePageRequest, String name) {
        Page page = new Page();
        if (userIds.size() == 0) {
            page.setData(new ArrayList<>());
            return R.ok().put("data", BaseUtil.crmPage(page));
        }
        if (basePageRequest.getPageType() != null && basePageRequest.getPageType() == 0) {
            LkCrmSqlParams sqlParams = taskDao.getTaskList(type, userIds, status,
                    priority, date, name);
            List<Map<String, Object>> maps = taskDao.sqlQuery(sqlParams.getSql(), sqlParams.getParams().toArray());
            List<Record> recordList = JavaBeanUtil.mapToRecords(maps);
            return R.ok().put("data", queryUser(recordList));
        } else {
            LkCrmSqlParams sqlParams = taskDao.getTaskList(type, userIds, status,
                    priority, date, name);
            page = taskDao.sqlPageQuery(sqlParams.getSql(),
                    basePageRequest.getPage(), basePageRequest.getLimit(), sqlParams.getParams().toArray());

            page.setData(queryUser(JavaBeanUtil.mapToRecords(page.getData())));
            return R.ok().put("data", BaseUtil.crmPage(page));
        }

    }

    private List<Record> queryUser(List<Record> tasks) {
        ArrayList<Record> labelList;
        ArrayList<Record> ownerUserList;
        for (Record task : tasks) {
            labelList = new ArrayList<>();
            ownerUserList = new ArrayList<>();
            if (StrUtil.isNotBlank(task.getStr("label_id"))) {
                String[] labelIds = task.getStr("label_id").split(",");
                for (String labelId : labelIds) {
                    if (StrUtil.isNotBlank(labelId)) {
//                        Record label = Db.findFirst("select label_id,name as labelName , color from lkcrm_work_task_label where label_id = ?", labelId);
                        String labelSql = "select label_id,name as labelName , color from lkcrm_work_task_label where label_id = ?";
                        Record label = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(labelSql, labelId));
                        labelList.add(label);
                    }
                }
            }
            if (StrUtil.isNotBlank(task.getStr("owner_user_id"))) {
                String[] ownerUserIds = task.getStr("owner_user_id").split(",");
                for (String ownerUserId : ownerUserIds) {
                    if (StrUtil.isNotBlank(ownerUserId)) {
//                        Record ownerUser = Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?", ownerUserId);
                        String ownerSql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
                        Record ownerUser = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(ownerSql, ownerUserId));
                        ownerUserList.add(ownerUser);
                    }
                }
            }
            LkCrmTaskRelationEntity taskRelation = taskDao.findUnique("from LkCrmTaskRelationEntity where taskId = ?", task.getInt("task_id"));
            Integer start = 0;
            if (taskRelation != null) {
                start = queryCount(start, taskRelation.getBusinessIds());
                start = queryCount(start, taskRelation.getContactsIds());
                start = queryCount(start, taskRelation.getContractIds());
                start = queryCount(start, taskRelation.getCustomerIds());
            }
            task.set("relationCount", start);
            if (task.getDate("stop_time") != null) {
                Calendar date = Calendar.getInstance();
                date.setTime(DateUtil.date());
                //设置开始时间
                Calendar begin = Calendar.getInstance();
                begin.setTime(task.getDate("stop_time"));
                if (date.after(begin) && task.getInt("status") != 5 && task.getInt("status") != 2) {
                    task.set("is_end", 1);
                } else {
                    task.set("is_end", 0);
                }
            } else {
                task.set("is_end", 0);
            }
            task.set("labelList", labelList).set("ownerUserList", ownerUserList);
        }
        return tasks;
    }

    private Integer queryCount(Integer start, String str) {
        // start 开始个数
        if (str != null) {
            String[] ownerUserIds = str.split(",");
            for (String ownerUserId : ownerUserIds) {
                if (StrUtil.isNotBlank(ownerUserId)) {
                    ++start;
                }
            }
        }

        return start;
    }

    public R queryWorkTaskLog(Integer taskId) {
        List<Map<String, Object>> taskList = taskClassDao.myWorkLog(taskId);
//        List<Record> recordList = Db.find(Db.getSqlPara("work.task.myWorkLog", Kv.by("taskId", taskId)));
        List<Record> recordList = JavaBeanUtil.mapToRecords(taskList);
        return R.ok().put("data", recordList);
    }

    private void saveWorkTaskLog(LkCrmWorkTaskLogEntity workTaskLog) {
        workTaskLog.setCreateTime(DateUtil.date().toTimestamp());
        taskDao.saveOrUpdate(workTaskLog);
        //workTaskLog.save();
    }

    @Before(Tx.class)
    private boolean getWorkTaskLog(LkCrmTaskEntity task, Long userId) {
        LkCrmWorkTaskLogEntity workTaskLog = new LkCrmWorkTaskLogEntity();
        workTaskLog.setUserId(userId);
        workTaskLog.setTaskId(task.getTaskId());
        LkCrmTaskEntity auldTask = taskDao.get(task.getTaskId());

        Set<Map.Entry<String, Object>> newEntries = BeanUtil.beanToMap(task, true, true).entrySet();
        Set<Map.Entry<String, Object>> oldEntries = BeanUtil.beanToMap(auldTask, true, false).entrySet();

        //判断描述是否修改
       /* if (task.getDescription() != null){
            if (auldTask.getDescription() == null || auldTask.getDescription().equals("")){
                workTaskLog.setContent("增加加了描述 " + task.getDescription());
            } else if (!auldTask.getDescription().equals(task.getDescription())){
               workTaskLog.setContent("修改了描述 " + task.getDescription());
            }
           saveWorkTaskLog(workTaskLog);
        }*/

        newEntries.forEach(x -> {
            oldEntries.forEach(y -> {
                Object oldValue = y.getValue();
                Object newValue = x.getValue();
                if (oldValue instanceof Date) {
                    oldValue = DateUtil.formatDateTime((Date) oldValue);
                }
                if (newValue instanceof Date) {
                    newValue = DateUtil.formatDateTime((Date) newValue);
                }
                if (oldValue == null || "".equals(oldValue)) {
                    oldValue = "空";
                }
                if (newValue == null || "".equals(newValue)) {
                    newValue = "空";
                }
                if (x.getKey().equals(y.getKey()) && !oldValue.equals(newValue)) {
                    if (!"update_time".equals(y.getKey()) && !"label_id".equals(y.getKey()) && !"owner_user_id".equals(y.getKey())) {
                        if ("priority".equals(y.getKey())) {
                            String value = "";
                            if (NumberUtil.isInteger(newValue.toString()) && NumberUtil.parseInt(newValue.toString()) == 1) {
                                value = "普通";
                            } else if (NumberUtil.isInteger(newValue.toString()) && NumberUtil.parseInt(newValue.toString()) == 2) {
                                value = "紧急";
                            } else if (NumberUtil.isInteger(newValue.toString()) && NumberUtil.parseInt(newValue.toString()) == 3) {
                                value = "非常紧急";
                            } else {
                                value = "无";
                            }
                            workTaskLog.setContent("修改 优先级 为：" + value + "");
                        } else {
                            workTaskLog.setContent("修改" + getTaileName(y.getKey()) + "为：" + newValue + "");
                        }
                        saveWorkTaskLog(workTaskLog);
                    }
                }
            });
        });

        BeanUtils.copyProperties(task, auldTask, JavaBeanUtil.getNullPropertyNames(task));
        taskDao.update(auldTask);

        //判断是否修改了标签
        if (task.getLabelId() != null) {
            LkCrmWorkTaskLabelEntity workTaskLabel;

            if (StrUtil.isEmpty(auldTask.getLabelId())) {
                //旧数据没有标签 直接添加
                List<String> labelName = Arrays.asList(task.getLabelId().split(","));
                for (String id : labelName) {
                    if (StrUtil.isNotBlank(id)) {
                        workTaskLabel = crmWorkTaskLabelDao.get(NumberConvertUtil.parseInt(id));
                        workTaskLog.setContent("增加了标签 " + workTaskLabel.getName());
                        saveWorkTaskLog(workTaskLog);
                    }
                }
            } else {
                //旧数据有标签 自动添加或修改
                List<String> labelName = Arrays.asList(task.getLabelId().split(","));
                for (String id : labelName) {
                    if (StrUtil.isNotBlank(id)) {
                        if (!auldTask.getLabelId().contains("," + id + ",")) {
                            workTaskLabel = crmWorkTaskLabelDao.get(NumberConvertUtil.parseInt(id));
                            workTaskLog.setContent("增加了标签 " + workTaskLabel.getName());
                            saveWorkTaskLog(workTaskLog);
                        }
                    }
                }

                List<String> auldLabelName = Arrays.asList(auldTask.getLabelId().split(","));
                for (String id : auldLabelName) {
                    if (StrUtil.isNotBlank(id)) {
                        if (!task.getLabelId().contains("," + id + ",")) {
                            workTaskLabel = crmWorkTaskLabelDao.get(NumberConvertUtil.parseInt(id));
                            workTaskLog.setContent("删除了标签 " + workTaskLabel.getName());
                            saveWorkTaskLog(workTaskLog);
                        }
                    }

                }
            }
        }
        //判断是参与人
        if (task.getOwnerUserId() != null) {
            CustomerUser adminUser;
            if (StrUtil.isEmpty(auldTask.getOwnerUserId())) {
                //判断旧数据没有参与人
                List<String> userIds = Arrays.asList(task.getOwnerUserId().split(","));
                for (String id : userIds) {
                    if (StrUtil.isNotBlank(id)) {
                        adminUser = customerUserDao.get(NumberConvertUtil.parseLong(id));
                        workTaskLog.setContent("添加 " + adminUser.getAccount() + "参与任务");
                        saveWorkTaskLog(workTaskLog);
                    }
                }
            } else {
                //判断旧数据有参与人
                List<String> userIds = Arrays.asList(task.getOwnerUserId().split(","));
                for (String id : userIds) {
                    if (StrUtil.isNotBlank(id)) {
                        if (!auldTask.getOwnerUserId().contains("," + id + ",")) {
                            adminUser = customerUserDao.get(NumberConvertUtil.parseLong(id));
                            workTaskLog.setContent("添加 " + adminUser.getAccount() + "参与任务");
                            saveWorkTaskLog(workTaskLog);
                        }
                    }
                }
                List<String> ids = Arrays.asList(auldTask.getOwnerUserId().split(","));
                for (String id : ids) {
                    if (StrUtil.isNotBlank(id)) {
                        if (!task.getOwnerUserId().contains("," + id + ",")) {
                            adminUser = customerUserDao.get(NumberConvertUtil.parseLong(id));
                            workTaskLog.setContent("将 " + adminUser.getAccount() + "从任务中移除");
                            saveWorkTaskLog(workTaskLog);
                        }
                    }
                }
            }
        }
        return true;
    }

    private String getTaileName(String key) {
        if ("name".equals(key)) {
            return "任务名称";
        } else if ("start_time".equals(key)) {
            return "开始时间";
        } else if ("stop_time".equals(key)) {
            return "结束时间";
        } else if ("description".equals(key)) {
            return "任务描述";
        }
        return "";
    }

    /**
     * 添加任务与业务关联
     */
    public R saveTaskRelation(LkCrmTaskRelationEntity taskRelation, Long userId) {
        taskRelationDao.executeUpdateSQL("delete from `lkcrm_task_relation` where task_id = ?", taskRelation.getTaskId());
        taskRelation.setCreateTime(DateUtil.date().toTimestamp());
//        return taskRelation.save() ? R.ok() : R.error();
        taskRelationDao.saveOrUpdate(taskRelation);
        return R.ok();
    }


    @Before(Tx.class)
    public R deleteTask(Integer taskId) {
//        Task task = new Task().dao().findById(taskId);
        LkCrmTaskEntity task = taskDao.get(taskId);
        if (task == null) {
            return R.error("任务不存在！");
        }
//        boolean bol;
        if (task.getPid() != 0) {
//            bol = task.delete();
            taskDao.delete(taskId);
        } else {
            String updateSql = "update lkcrm_task set ishidden = 1,hidden_time = now() where task_id = ?";
            taskDao.executeUpdateSQL(updateSql, taskId);
//            bol = Db.update("update lkcrm_task set ishidden = 1,hidden_time = now() where task_id = ?", taskId) > 0;
        }
        return R.ok();
//        return bol ? R.ok() : R.error();
    }


    /**
     * @author zxy
     * crm查询任务
     */
    public R queryTaskRelation(BasePageRequest<TaskRelation> basePageRequest) {
        TaskRelation relation = basePageRequest.getData();
        if (AuthUtil.oaAnth(relation.toRecord())) {
            return R.noAuth();
        }
        com.bdaim.common.dto.Page paginate = taskDao.queryTaskRelation(basePageRequest.getPage(), basePageRequest.getLimit(), relation.getBusinessIds(), relation.getContactsIds(), relation.getContractIds(), relation.getCustomerIds());
        //Page<Record> paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("work.task.queryTaskRelation", Kv.by("businessIds", relation.getBusinessIds()).set("contactsIds", relation.getContactsIds()).set("contractIds", relation.getContractIds()).set("customerIds", relation.getCustomerIds())));
        List<Record> list = new ArrayList<>();
        paginate.getData().forEach(s -> {
            Record r = JavaBeanUtil.mapToRecord((Map<String, Object>) s);
            composeUser(r);
            list.add(r);
        });
        paginate.setData(list);
        return R.ok().put("data", BaseUtil.crmPage(paginate));
    }

    private void composeUser(Record record) {
        Integer createUserId = record.getInt("create_user_id");
//        record.set("createUser", Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?", createUserId));
        String getUserSql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
        Record createUser = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(getUserSql, createUserId));
        record.set("createUser", createUser);
        Integer mainUserId = record.getInt("main_user_id");
//        record.set("mainUser", Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?", mainUserId));
        Record mainUser = JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(getUserSql, mainUserId));
        record.set("mainUser", mainUser);
        String ownerUserId = record.getStr("owner_user_id");
        List<Record> ownerUserList = new ArrayList<>();
        TagUtil.toSet(ownerUserId).forEach(userId -> ownerUserList.add(
                JavaBeanUtil.mapToRecord(taskClassDao.queryUniqueSql(getUserSql, userId))
        ));
        record.set("ownerUserList", ownerUserList);
    }


    public R getTaskList(BasePageRequest basePageRequest, String labelId, String ishidden) {
//        Page<Record> recordList = Db.paginate(basePageRequest.getPage(),
//                basePageRequest.getLimit(),
//                Db.getSqlPara("work.task.myTask",
//                        Kv.by("userId", BaseUtil.getUser().getUserId()).set("labelId", labelId).set("ishidden", ishidden)));
        //TODO
        return R.ok().put("data", null);
    }

    public R archiveByTaskId(Integer taskId) {
//        int update = Db.update("update  `lkcrm_task` set is_archive = 1,archive_time = now() where task_id = ?", taskId);
        String updateSql = "update  `lkcrm_task` set is_archive = 1,archive_time = now() where task_id = ?";
        int update = taskClassDao.executeUpdateSQL(updateSql, taskId);
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * 查询跟进记录类型
     */
    public R queryRecordOptions() {
        LoginUser user = BaseUtil.getUser();
        List<LkCrmAdminConfigEntity> list = crmActionRecordDao.find("from LkCrmAdminConfigEntity where name = ? AND custId = ? ", "taskType", user.getCustId());
        if (list.size() == 0) {
            List<LkCrmAdminConfigEntity> adminConfigList = new ArrayList<>();
            // 初始化数据
            String[] defaults = new String[]{"电话", "短信", "上门拜访"};
            for (String i : defaults) {
                LkCrmAdminConfigEntity adminConfig = new LkCrmAdminConfigEntity();
                adminConfig.setCustId(user.getCustId());
                adminConfig.setName("taskType");
                adminConfig.setValue(i);
                adminConfig.setDescription("任务类型选项");
                adminConfig.setIsSystem(1);
                adminConfigList.add(adminConfig);
            }
            crmActionRecordDao.batchSaveOrUpdate(adminConfigList);
            list.addAll(adminConfigList);
        }
        return R.ok().put("data", list);
    }


    public R setRecordOptions(List<String> list) {
        LoginUser user = BaseUtil.getUser();
        crmActionRecordDao.executeUpdateSQL("delete from lkcrm_admin_config where name = 'taskType' AND cust_id = ? AND is_system <> 1  ", user.getCustId());
        List<LkCrmAdminConfigEntity> adminConfigList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            LkCrmAdminConfigEntity adminConfig = new LkCrmAdminConfigEntity();
            adminConfig.setCustId(user.getCustId());
            adminConfig.setName("taskType");
            adminConfig.setValue(list.get(i));
            adminConfig.setDescription("任务类型选项");
            adminConfig.setIsSystem(2);
            adminConfigList.add(adminConfig);
        }
        crmActionRecordDao.batchSaveOrUpdate(adminConfigList);
        return R.ok();
    }

    public R setWorkTask(LkCrmTaskEntity task, String customerIds
            , String contactsIds
            , String businessIds
            , String contractIds
            , String leadsIds) {
        if (task.getWorkId() != null) {
            Integer isOpen = workDao.get(task.getWorkId()).getIsOpen();
            if (isOpen == 0 && !AuthUtil.isWorkAuth(task.getWorkId().toString(), "task:save")) {
                return (R.noAuth());
                //return;
            }
        }
        if (StrUtil.isNotEmpty(task.getOwnerUserId())) {
            task.setOwnerUserId(TagUtil.fromString(task.getOwnerUserId()));
        }
        if (task.getStartTime() != null && task.getStopTime() != null) {
            if (task.getStartTime().getTime() > task.getStopTime().getTime()) {
                return (R.error("开始时间不能大于结束时间"));
                //return;
            }
        }
       /* String customerIds = getPara("customerIds");
        String contactsIds = getPara("contactsIds");
        String businessIds = getPara("businessIds");
        String contractIds = getPara("contractIds");
        String leadsIds = getPara("leadsIds");*/
        LkCrmTaskRelationEntity taskRelation = new LkCrmTaskRelationEntity();
        if (customerIds != null || contactsIds != null || businessIds != null || contractIds != null || leadsIds != null) {

            taskRelation.setBusinessIds(TagUtil.fromString(businessIds));
            taskRelation.setContactsIds(TagUtil.fromString(contactsIds));
            taskRelation.setContractIds(TagUtil.fromString(contractIds));
            taskRelation.setCustomerIds(TagUtil.fromString(customerIds));
            taskRelation.setLeadsIds(TagUtil.fromString(leadsIds));
        }
        return (setTask(task, taskRelation));
    }
}
