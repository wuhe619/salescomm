package com.bdaim.crm.erp.work.service;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.common.spring.DataConverter;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.LkCrmTaskDao;
import com.bdaim.crm.dao.LkCrmWorkDao;
import com.bdaim.crm.dao.LkCrmWorkTaskClassDao;
import com.bdaim.crm.dao.LkCrmWorkUserDao;
import com.bdaim.crm.entity.*;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.erp.admin.service.AdminMenuService;
import com.bdaim.crm.erp.work.entity.WorkUser;
import com.bdaim.crm.utils.*;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Service
@Transactional
public class WorkService {

    @Resource
    private AdminFileService adminFileService;

    @Resource
    private WorkbenchService workbenchService;

    @Resource
    private AdminMenuService adminMenuService;

    @Autowired
    private LkCrmWorkDao workDao;
    @Autowired
    private LkCrmTaskDao taskDao;
    @Autowired
    private LkCrmWorkUserDao userDao;
    @Autowired
    private LkCrmWorkTaskClassDao workTaskClassDao;

    @Before(Tx.class)
    public R setWork(LkCrmWorkEntity work) throws IllegalAccessException {
        Long userId = BaseUtil.getUser().getUserId();
        boolean bol;
        if (work.getWorkId() == null) {
            if (!AuthUtil.isWorkAdmin()) {
                return R.noAuth();
            }
            Set<Long> ownerUserIds = new HashSet<>();
            ownerUserIds.add(userId);
            if (work.getOwnerUserId() != null) {
                ownerUserIds.addAll(TagUtil.toLongSet(work.getOwnerUserId()));
            }
            if (work.getIsOpen() == 1) {
                //公开项目删除负责人
                ownerUserIds.clear();
            }
            work.setOwnerUserId(TagUtil.fromLongSet(ownerUserIds));
            work.setCreateUserId(userId.longValue());
            work.setCreateTime(new Timestamp(System.currentTimeMillis()));
//            bol = work.save();
            workDao.saveOrUpdate(work);
            LkCrmWorkTaskClassEntity workTaskClass = new LkCrmWorkTaskClassEntity();
            workTaskClass.setClassId(0);
            workTaskClass.setName("要做");
            workTaskClass.setCreateTime(new Timestamp(System.currentTimeMillis()));
            workTaskClass.setCreateUserId(userId);
            workTaskClass.setWorkId(work.getWorkId());
            workTaskClass.setOrderNum(1);
//            workTaskClass.save();
            workTaskClassDao.save(workTaskClass);
            workTaskClass.setName("在做");
            workTaskClass.setOrderNum(2);
//            workTaskClass.save();
            workTaskClassDao.save(workTaskClass);
            workTaskClass.setName("待定");
            workTaskClass.setOrderNum(3);
//            workTaskClass.save();
            workTaskClassDao.saveOrUpdate(workTaskClass);
            ownerUserIds.forEach(ownerUserId -> {
                LkCrmWorkUserEntity workUser = new LkCrmWorkUserEntity();
                workUser.setWorkId(work.getWorkId());
                workUser.setUserId(ownerUserId.longValue());
                if (ownerUserId.equals(userId)) {
                    workUser.setRoleId(BaseConstant.SMALL_WORK_ADMIN_ROLE_ID);
//                    workUser.save();
                    userDao.save(workUser);
                } else {
                    workUser.setRoleId(BaseConstant.SMALL_WORK_EDIT_ROLE_ID);
//                    workUser.save();
                    userDao.save(workUser);
                }
            });
        } else {
            if (!AuthUtil.isWorkAuth(work.getWorkId().toString(), "work:update")) {
                return R.noAuth();
            }
            Integer workId = work.getWorkId();
//            Map<String, Object> columns = work.toRecord().getColumns();
            Map<String, Object> columns = JavaBeanUtil.mapToRecord(DataConverter.objectToMap(work)).getColumns();
            if (columns.keySet().contains("owner_user_id")) {
                if (!ObjectUtil.isNull(columns.get("owner_user_id"))) {
//                    Work oldWork = new Work().findById(workId);
                    LkCrmWorkEntity oldWork = workDao.get(workId);
                    work.setOwnerUserId(TagUtil.fromString(work.getOwnerUserId()));
                    Set<Long> oldOwnerUserIds = TagUtil.toLongSet(oldWork.getOwnerUserId());
                    Set<Long> ownerUserIds = TagUtil.toLongSet(work.getOwnerUserId());
                    Collection<Long> intersection = CollectionUtil.intersection(oldOwnerUserIds, ownerUserIds);
                    oldOwnerUserIds.removeAll(intersection);
                    ownerUserIds.removeAll(intersection);
                    for (Long next : oldOwnerUserIds) {
                        leave(work.getWorkId().toString(), next);
                        Db.delete("delete from `lkcrm_work_user` where work_id = ? and user_id = ?", workId, next);
                    }
                    for (Long ownerUserId : ownerUserIds) {
                        LkCrmWorkUserEntity workUser = new LkCrmWorkUserEntity();
                        workUser.setWorkId(work.getWorkId());
                        workUser.setUserId(ownerUserId);
                        workUser.setRoleId(BaseConstant.SMALL_WORK_EDIT_ROLE_ID);
//                        workUser.save();
                        userDao.save(workUser);
                    }
                } else {
//                    Db.delete("delete from `lkcrm_work_user` where work_id = ?", workId);
                    String delSql = "delete from `lkcrm_work_user` where work_id = ?";
                    workDao.executeUpdateSQL(delSql, workId);
                    work.setOwnerUserId("," + userId + ",");
                    LkCrmWorkUserEntity workUser = new LkCrmWorkUserEntity();
                    workUser.setWorkId(work.getWorkId());
                    workUser.setUserId(userId);
                    workUser.setRoleId(BaseConstant.SMALL_WORK_ADMIN_ROLE_ID);
//                    workUser.save();
                    userDao.save(workUser);
                }
            }
            if (work.getIsOpen() != null) {
                if (work.getIsOpen() == 1) {
                    //公开项目删除负责人
                    work.setOwnerUserId(null);
//                    Db.delete("delete from `lkcrm_work_user` where work_id = ?", workId);
                    String delSql = "delete from `lkcrm_work_user` where work_id = ?";
                    userDao.executeUpdateSQL(delSql, workId);
                } else if (work.getIsOpen() == 0) {
                    List<Long> userList = Db.query("select user_id from `lkcrm_admin_user` where status != 0");
                    userList.remove(Long.valueOf(userId));
                    List<LkCrmWorkUserEntity> workUserList = new ArrayList<>();
                    LkCrmWorkUserEntity nowWorkUser = new LkCrmWorkUserEntity();
                    nowWorkUser.setWorkId(work.getWorkId());
                    nowWorkUser.setUserId(userId);
                    nowWorkUser.setRoleId(BaseConstant.SMALL_WORK_ADMIN_ROLE_ID);
                    workUserList.add(nowWorkUser);
                    userList.forEach(id -> {
                        LkCrmWorkUserEntity workUser = new LkCrmWorkUserEntity();
                        workUser.setWorkId(work.getWorkId());
                        workUser.setUserId(id);
                        workUser.setRoleId(BaseConstant.SMALL_WORK_EDIT_ROLE_ID);
                        workUserList.add(workUser);
                    });
//                    Db.batchSave(workUserList, 500);
                    userDao.batchSaveOrUpdate(workUserList);
                    userList.add(Long.valueOf(userId));
                    work.setOwnerUserId(TagUtil.fromSet(userList.stream().map(Long::intValue).collect(Collectors.toList())));
                }
            }
            if (work.getStatus() != null && work.getStatus() == 3) {
                work.setArchiveTime(new Timestamp(System.currentTimeMillis()));
            }
//            bol = work.update();
            workDao.saveOrUpdate(work);
        }
//        return bol ? queryOwnerRoleList(work.getWorkId()).put("work", work) : R.error();
        return queryOwnerRoleList(work.getWorkId()).put("work", work);
    }

    public R deleteWork(String workId) {
//        Db.delete("delete from `lkcrm_task_relation` where task_id in (select `lkcrm_task`.task_id from `lkcrm_task` where work_id = ?)", workId);
        String delSql = "delete from `lkcrm_task_relation` where task_id in " +
                "(select `lkcrm_task`.task_id from `lkcrm_task` where work_id = ?)";
        userDao.executeUpdateSQL(delSql, workId);
//        Db.delete("delete from `lkcrm_task` where work_id = ?", workId);
        String delSql2 = "delete from `lkcrm_task` where work_id = ?";
        userDao.executeUpdateSQL(delSql2, workId);
//        Db.delete("delete from `lkcrm_work_user` where work_id = ?", workId);
        String delSql3 = "delete from `lkcrm_work_user` where work_id = ?";
        userDao.executeUpdateSQL(delSql3, workId);
//        int update = Db.delete("delete from `lkcrm_work` where work_id = ?", workId);
        String delSql4 = "delete from `lkcrm_work` where work_id = ?";
        int update = userDao.executeUpdateSQL(delSql4, workId);
        return update > 0 ? R.ok() : R.error();
    }

    public R queryWorkNameList() {
        List<Record> recordList;
        if (AuthUtil.isWorkAdmin()) {
//            recordList = Db.find(Db.getSqlPara("work.queryWorkNameList"));
            recordList = JavaBeanUtil.mapToRecords(userDao.queryWorkNameList());
        } else {
//            recordList = Db.find(Db.getSqlPara("work.queryWorkNameList", Kv.by("userId", BaseUtil.getUser().getUserId().intValue())));
            recordList = JavaBeanUtil.mapToRecords(userDao.queryWorkNameListByUserId(BaseUtil.getUser().getUserId()));
        }
        return R.ok().put("data", recordList);
    }

    public R queryTaskByWorkId(JSONObject jsonObject) {
        Integer workId = jsonObject.getInteger("workId");
//        List<Record> classList = Db.find("select class_id as classId, name as className from `lkcrm_work_task_class` where work_id = ? order by order_num", workId);
        String classListSql = "select class_id as classId, name as className from " +
                "`lkcrm_work_task_class` where work_id = ? order by order_num";
        List<Map<String, Object>> classListMap = userDao.sqlQuery(classListSql, workId);
        List<Record> classList = JavaBeanUtil.mapToRecords(classListMap);
        LinkedList<Record> linkedList = new LinkedList<>(classList);
        Record item = new Record();
        item.set("className", "(未分组)");
        item.set("classId", -1);
        linkedList.addFirst(item);
        List<Record> finalClassList = new CopyOnWriteArrayList<>(linkedList);
        finalClassList.forEach(workClass -> {
//            List<Record> recordList = Db.find(Db.getSqlPara("work.queryTaskByWorkId",
//                    Kv.by("workId", workId)
//                            .set("stopTimeType", jsonObject.getInteger("stopTimeType"))
//                            .set("userIds", jsonObject.getJSONArray("mainUserId"))
//                            .set("labelIds", jsonObject.getJSONArray("labelId"))
//                            .set("classId", workClass.getInt("classId"))));
            List<Record> recordList = JavaBeanUtil.mapToRecords(workDao.queryTaskByWorkId(workId,
                    jsonObject.getInteger("stopTimeType"),
                    jsonObject.getJSONArray("mainUserId"),
                    jsonObject.getJSONArray("labelId"),
                    workClass.getInt("classId")));
            workClass.set("count", recordList.size());
            if (recordList.size() == 0) {
                if (workClass.getInt("classId") != -1) {
                    workClass.set("list", new ArrayList<>());
                } else {
                    finalClassList.remove(workClass);
                }
            } else {
                workbenchService.taskListTransfer(recordList);
                recordList.sort(Comparator.comparingInt(a -> a.getInt("order_num")));
                workClass.set("list", recordList);
            }
        });
        return R.ok().put("data", finalClassList);
    }


    public R queryTaskFileByWorkId(BasePageRequest<JSONObject> data) {
//        Page<Record> workFile = Db.paginate(data.getPage(), data.getLimit(), Db.getSqlPara("work.queryTaskFileByWorkId", Kv.by("workId", data.getData().getInteger("workId"))));
//        return R.ok().put("data", workFile);
        Page workFilePage = workDao.queryTaskFileByWorkId(data.getPage(), data.getLimit(),
                data.getData().getInteger("workId"));
        return R.ok().put("data", BaseUtil.crmPage(workFilePage));
    }

    public R queryArchiveWorkList(BasePageRequest request) {
//        Page<Record> recordPage = Db.paginate(request.getPage(), request.getLimit(),
//                "select work_id,archive_time,name,color ", "from lkcrm_work where status = 3");
        String sql = "select work_id,archive_time,name,color" +
                " from lkcrm_work where status = 3";
        CrmPage recordPage = BaseUtil.crmPage(workDao.sqlPageQuery(sql, request.getPage(), request.getLimit()));
        return R.ok().put("data", recordPage);
    }

    public R workStatistics(String workId) {
        Long userId1 = BaseUtil.getUserId();
        if ("all".equals(workId)) {
            Record taskStatistics = new Record();
            List<Record> memberTaskStatistics = new ArrayList<>();
            if (AuthUtil.isWorkAdmin()) {
//                taskStatistics = Db.findFirst(Db.getSqlPara("work.workStatistics"));
                taskStatistics = JavaBeanUtil.mapToRecord(workDao.workStatistics(null));
//                memberTaskStatistics = Db.find("select user_id,img,realname from `lkcrm_admin_user` where user_id in (select main_user_id from `lkcrm_task` where ishidden = 0 and work_id > 0)");
                String sql = "select user_id,img,realname from `lkcrm_admin_user` where user_id in (select main_user_id" +
                        " from `lkcrm_task` where ishidden = 0 and work_id > 0)";
                memberTaskStatistics = JavaBeanUtil.mapToRecords(workDao.queryListBySql(sql));
                memberTaskStatistics.forEach(record -> {
//                    Record first = Db.findFirst(Db.getSqlPara("work.workStatistics", Kv.by("mainUserId", record.getInt("user_id"))));
                    Map<String, Object> mainUserId = new HashMap<>();
                    mainUserId.put("mainUserId", record.getInt("user_id"));
                    Record first = JavaBeanUtil.mapToRecord(workDao.workStatistics(mainUserId));
                    record.setColumns(first);
                });
            } else {
//                List<Record> recordList = Db.find(Db.getSqlPara("work.queryOwnerWorkIdList", Kv.by("userId", userId1)));
                List<Record> recordList = workDao.queryOwnerWorkIdList(userId1);
                List<Integer> workIds = recordList.stream().map(record -> record.getInt("work_id"))
                        .collect(Collectors.toList());
                if (workIds.size() == 0) {
                    taskStatistics.set("unfinished", 0).set("overdue", 0).set("complete", 0).set("archive", 0)
                            .set("completionRate", 0).set("overdueRate", 0);
                } else {
//                    taskStatistics = Db.findFirst(Db.getSqlPara("work.workStatistics", Kv.by("workIds", CollectionUtil.join(workIds, ","))));
                    Map<String, Object> workIdsMap = new HashMap<>();
                    workIdsMap.put("workIds", CollectionUtil.join(workIds, ","));
                    taskStatistics = JavaBeanUtil.mapToRecord(workDao.workStatistics(workIdsMap));
//                    memberTaskStatistics = Db.find(Db.getSql("work.getTaskOwnerOnWork"), CollectionUtil.join(workIds, ","));
                    String sql = "SELECT user_id,img,realname FROM `lkcrm_admin_user`  " +
                            "WHERE " +
                            " user_id IN ( SELECT main_user_id FROM `lkcrm_task` WHERE ishidden = 0 AND work_id IN ( ? ) )";
                    memberTaskStatistics = JavaBeanUtil.mapToRecords(workDao.sqlQuery(sql,
                            CollectionUtil.join(workIds, ",")));
                    memberTaskStatistics.forEach(record -> {
//                        Record first = Db.findFirst(Db.getSqlPara("work.workStatistics", Kv.by("mainUserId",
//                        record.getInt("user_id")).set("workIds", CollectionUtil.join(workIds, ","))));
                        Map<String, Object> firstMap = new HashMap<>();
                        firstMap.put("mainUserId", record.getInt("user_id"));
                        firstMap.put("workIds", CollectionUtil.join(workIds, ","));
                        Record first = JavaBeanUtil.mapToRecord(workDao.workStatistics(firstMap));
                        record.setColumns(first);
                    });
                }
            }

            return R.ok().put("data", Kv.by("taskStatistics", taskStatistics).set("memberTaskStatistics", memberTaskStatistics));
        }
//        Record taskStatistics = Db.findFirst(Db.getSqlPara("work.workStatistics", Kv.by("workId", workId)));
        Map<String, Object> workIdMap = new HashMap<>();
        workIdMap.put("workId", workId);
        Record taskStatistics = JavaBeanUtil.mapToRecord(workDao.workStatistics(workIdMap));
//        String ownerUserId = Db.queryStr("select owner_user_id from `lkcrm_work` where work_id = ?", workId);
        String ownerUserIdSql = "select owner_user_id from `lkcrm_work` where work_id = ?";
        String ownerUserId = userDao.queryForObject(ownerUserIdSql, workId);
        List<Record> ownerList = new ArrayList<>();
        for (Integer userId : TagUtil.toSet(ownerUserId)) {
//            Record ownerUser = Db.findFirst("select b.user_id,realname,img from `lkcrm_work_user` a left join `lkcrm_admin_user` b on " +
//                    "a.user_id = b.user_id where a.work_id = ? and a.user_id = ? and a.role_id = ?", workId, userId, BaseConstant.SMALL_WORK_ADMIN_ROLE_ID);
            String ownerUserSql = "select b.user_id,realname,img from `lkcrm_work_user` a left join `lkcrm_admin_user` b" +
                    " on a.user_id = b.user_id where a.work_id = ? and a.user_id = ? and a.role_id = ?";
            Record ownerUser = JavaBeanUtil.mapToRecord(userDao.queryUniqueSql(ownerUserSql,
                    workId, userId, BaseConstant.SMALL_WORK_ADMIN_ROLE_ID));
            if (ownerUser != null) {
                ownerList.add(ownerUser);
            }
        }
        List<Record> userList = new ArrayList<>();
        for (Integer userId : TagUtil.toSet(ownerUserId)) {
//            userList.add(Db.findFirst("select user_id,realname,username,img from `lkcrm_admin_user` where user_id = ?", userId));
            String recordSql = "select user_id,realname,username,img from `lkcrm_admin_user` where user_id = ?";
            userList.add(JavaBeanUtil.mapToRecord(userDao.queryUniqueSql(recordSql, userId)));
        }
        List<Record> classStatistics = new ArrayList<>();
        List<Record> labelStatistics = new ArrayList<>();
//        List<Record> recordList = Db.find("select class_id classId,name className from lkcrm_work_task_class a  where a.work_id = ?", workId);
        String recordListSql = "select class_id classId,name className from lkcrm_work_task_class a  where a.work_id = ?";
        List<Record> recordList = JavaBeanUtil.mapToRecords(userDao.sqlQuery(recordListSql, workId));
        Map<String, Object> classMap = new HashMap<>();
        recordList.forEach(record -> classMap.put(record.getStr("classId"), record.getStr("className")));
        classMap.forEach((classId, name) -> {
//            Record first = Db.findFirst("select count(status = 5 or null) as complete,count(status != 5 or null) as undone from lkcrm_task where class_id = ? and work_id = ? and ishidden = 0 and (is_archive = 0 or (is_archive = 1 and status = 5))", classId, workId);
            String firstRecordSql = "select count(status = 5 or null) as complete,count(status != 5 or null) as undone " +
                    "from lkcrm_task where class_id = ? and work_id = ? and ishidden = 0 " +
                    "and (is_archive = 0 or (is_archive = 1 and status = 5))";
            Record first = JavaBeanUtil.mapToRecord(userDao.queryUniqueSql(firstRecordSql, classId, workId));
            first.set("className", classMap.get(classId));
            classStatistics.add(first);
        });
//        List<Record> labelList = Db.find("select label_id,status from `lkcrm_task` where work_id  = ? and label_id is not null and ishidden = 0 and (is_archive = 0 or (is_archive = 1 and status = 5))", workId);
        String labelFirstSql = "select label_id,status from `lkcrm_task` where work_id  = ? and label_id is not null " +
                "and ishidden = 0 and (is_archive = 0 or (is_archive = 1 and status = 5))";
        List<Record> labelList = JavaBeanUtil.mapToRecords(userDao.sqlQuery(labelFirstSql, workId));
        List<String> labelIdList = labelList.stream().map(record -> record.getStr("label_id")).collect(Collectors.toList());
        Set<Integer> labelIdSet = new HashSet<>(toList(labelIdList));
        Map<Integer, Record> labelMap = new HashMap<>();
        labelIdSet.forEach(id -> {
//            Record record = Db.findFirst("select label_id,name,color from lkcrm_work_task_label where label_id = ?", id);
            String getRecordSql = "select label_id,name,color from lkcrm_work_task_label where label_id = ?";
            Record record = JavaBeanUtil.mapToRecord(userDao.queryUniqueSql(getRecordSql, id));
            labelMap.put(record.getInt("label_id"), record);
        });
        labelMap.forEach((id, record) -> {
            AtomicReference<Integer> complete = new AtomicReference<>(0);
            AtomicReference<Integer> undone = new AtomicReference<>(0);
            labelList.forEach(label -> {
                if (label.getStr("label_id").contains(id.toString())) {
                    if (label.getInt("status") == 1) {
                        undone.getAndSet(undone.get() + 1);
                    } else if (label.getInt("status") == 5) {
                        complete.getAndSet(complete.get() + 1);
                    }
                }
            });
            record.set("complete", complete.get());
            record.set("undone", undone.get());
            labelStatistics.add(record);
        });
        List<Record> memberTaskStatistics = memberTaskStatistics(workId);
        Kv result = Kv.by("taskStatistics", taskStatistics).set("classStatistics", classStatistics).set("labelStatistics", labelStatistics).set("memberTaskStatistics", memberTaskStatistics).set("userList", userList).set("ownerList", ownerList);
        return R.ok().put("data", result);
    }

    public List<Integer> toList(List<String> labelList) {
        List<Integer> list = new ArrayList<>();
        if (labelList == null || labelList.size() == 0) {
            return list;
        }
        labelList.forEach(ids -> {
            if (StrUtil.isNotEmpty(ids)) {
                for (String id : ids.split(",")) {
                    if (StrUtil.isNotEmpty(id)) {
                        list.add(Integer.valueOf(id));
                    }
                }
            }
        });
        return list;
    }


    /**
     * 项目成员任务统计
     */
    public List<Record> memberTaskStatistics(String workId) {
        List<Record> list = new ArrayList<>();
//        Work work = new Work().findById(workId);
        LkCrmWorkEntity work = workDao.get(Integer.parseInt(workId));
        if (work.getIsOpen() == 1) {
//            list = Db.find(Db.getSql("work.getTaskOwnerOnWork"), workId);
            String getTaskOwnerOnWork = "select user_id,img,realname from `lkcrm_admin_user` " +
                    "where user_id in (select main_user_id from `lkcrm_task` where ishidden = 0 and work_id in (?))";
            list = JavaBeanUtil.mapToRecords(workDao.sqlQuery(getTaskOwnerOnWork, workId));
            list.forEach(record -> {
//                Record first = Db.findFirst(Db.getSqlPara("work.workStatistics",
//                        Kv.by("mainUserId", record.getInt("user_id")).set("workIds", workId)));
//                Record first = JavaBeanUtil.mapToRecord(workDao.workStatistics(record.getInt("user_id"), workId));
                Map<String, Object> mainUserIdMap = new HashMap<>();
                mainUserIdMap.put("mainUserId", record.getInt("user_id"));
                mainUserIdMap.put("workIds", workId);
                Record first = JavaBeanUtil.mapToRecord(workDao.workStatistics(mainUserIdMap));
                record.setColumns(first);
            });
        } else {
//            String ownerUserIds = Db.queryStr("select owner_user_id from lkcrm_work where work_id = ?", workId);
            String ownerUserIdsSql = "select owner_user_id from lkcrm_work where work_id = ?";
            String ownerUserIds = workDao.queryForObject(ownerUserIdsSql, workId);
            if (StrUtil.isEmpty(ownerUserIds)) {
                return list;
            }
            for (String userId : ownerUserIds.split(",")) {
                if (StrUtil.isEmpty(userId)) {
                    continue;
                }
//                Record user = Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?", userId);
                String userSql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
                Record user = JavaBeanUtil.mapToRecord(workDao.queryUniqueSql(userSql, userId));
//                Record first = Db.findFirst(Db.getSqlPara("work.workStatistics", Kv.by("workId", workId).set("userId", userId)));
//                Record first = JavaBeanUtil.mapToRecord(workDao.workStaByWorkIdAndUserId(workId, userId));
                Map<String, Object> userIdMap = new HashMap<>();
                userIdMap.put("workId", workId);
                userIdMap.put("userId", userId);
                Record first = JavaBeanUtil.mapToRecord(workDao.workStatistics(userIdMap));
                user.setColumns(first);
                list.add(user);
            }
        }
        return list;
    }

    public R queryWorkOwnerList(String workId) {
//        String ownerUserId = Db.queryStr("select owner_user_id from `lkcrm_work` where work_id = ?", workId);
        String ownerUserSql = "select owner_user_id from `lkcrm_work` where work_id = ?";
        String ownerUserId = workDao.queryForObject(ownerUserSql, workId);
        List<Record> userList = new ArrayList<>();
        for (Integer userId : TagUtil.toSet(ownerUserId)) {
//            userList.add(Db.findFirst("select user_id,realname,username,img from `lkcrm_admin_user` where user_id = ?", userId));
            String getSql = "select user_id,realname,username,img from `lkcrm_admin_user` where user_id = ?";
            Record record = JavaBeanUtil.mapToRecord(workDao.queryUniqueSql(getSql, userId));
            userList.add(record);
        }
        return R.ok().put("data", userList);
    }

    public R updateOrder(JSONObject jsonObject) {
        String updateSql = "update `lkcrm_task` set class_id = ?,order_num = ? where task_id = ?";
        if (jsonObject.containsKey("toList")) {
            JSONArray tolist = jsonObject.getJSONArray("toList");
            Integer toId = jsonObject.getInteger("toId");
            for (int i = 1; i <= tolist.size(); i++) {
//                Db.update(updateSql, toId, i, tolist.get(i - 1));
                workDao.executeUpdateSQL(updateSql, toId, i, tolist.get(i - 1));
            }
        }
        if (jsonObject.containsKey("fromList")) {
            JSONArray fromList = jsonObject.getJSONArray("fromList");
            Integer fromId = jsonObject.getInteger("fromId");
            for (int i = 1; i <= fromList.size(); i++) {
//                Db.update(updateSql, fromId, i, fromList.get(i - 1));
                workDao.executeUpdateSQL(updateSql, fromId, i, fromList.get(i - 1));
            }
        }
        return R.ok();

    }

    public R leave(String workId, Long userId) {
//        Work work = new Work().findById(workId);
        LkCrmWorkEntity work = workDao.get(Integer.parseInt(workId));
        if (work.getCreateUserId().equals(userId)) {
            return R.error("项目创建人不可以退出");
        }
//        Db.update(Db.getSqlPara("work.leave", Kv.by("workId", workId).set("userId", userId)));
        String updateSql1 = "update `lkcrm_task` set main_user_id = null where work_id =  ? and main_user_id = ?";
        workDao.executeUpdateSQL(updateSql1, workId, userId);
//        Db.update(Db.getSqlPara("work.leave1", Kv.by("workId", workId).set("userId", userId)));
        String updateSql2 = "update `lkcrm_task` set owner_user_id = replace(owner_user_id,concat(','," +
                "?,','),',') where work_id = ? and  owner_user_id like concat(',',?,',')";
        workDao.executeUpdateSQL(updateSql2, userId, workId, userId);
        Set<Integer> ownerUserIds = TagUtil.toSet(work.getOwnerUserId());
        ownerUserIds.remove(userId);
        work.setOwnerUserId(TagUtil.fromSet(ownerUserIds));
//        boolean update = work.update();
        workDao.update(work);
//        Db.update("delete from `lkcrm_work_user` where work_id = ? and user_id = ?", workId, userId);
        String updateSql3 = "delete from `lkcrm_work_user` where work_id = ? and user_id = ?";
        workDao.executeUpdateSQL(updateSql3, workId, userId);
//        return update ? R.ok() : R.error();
        return R.ok();
    }

    public R getWorkById(String workId) throws IllegalAccessException {
//        Work work = new Work().findById(workId);
        LkCrmWorkEntity work = workDao.get(Integer.parseInt(workId));
        if (work == null) {
            return R.error("项目不存在！");
        }
        int isUpdate = 0;
        if (AuthUtil.isWorkAdmin() || BaseUtil.getUser().getRoles().contains(BaseConstant.SMALL_WORK_ADMIN_ROLE_ID)) {
            isUpdate = 1;
        }
        LoginUser user = BaseUtil.getUser();
        Long userId = BaseUtil.getUserId();
//        Integer roleId = Db.queryInt("select role_id from `lkcrm_work_user` where work_id = ? and user_id = ?", workId, userId);
        String roleIdSql = "select role_id from `lkcrm_work_user` where work_id = ? and user_id = ?";
        Integer roleId = workDao.queryForInt(roleIdSql, workId, userId);
        JSONObject root = new JSONObject();
//        List<Record> menuRecords = Db.find(Db.getSql("admin.menu.queryWorkMenuByRoleId"), roleId);
        List<Record> menuRecords = JavaBeanUtil.mapToRecords(workDao.queryWorkMenuByRoleId(roleId));
//        Integer workMenuId = Db.queryInt("select menu_id from `lkcrm_admin_menu` where parent_id = 0 and realm = 'work'");
        String workMenuIdSql = "select menu_id from `lkcrm_admin_menu` where parent_id = 0 and realm = 'work'";
        Integer workMenuId = workDao.queryForInt(workMenuIdSql);
        List<LkCrmAdminMenuEntity> adminMenus = adminMenuService.queryMenuByParentId(workMenuId);
        JSONObject object = new JSONObject();
        Long adminUserId = BaseUtil.getAdminUserId();
        adminMenus.forEach(menu -> {
            JSONObject authObject = new JSONObject();
            List<LkCrmAdminMenuEntity> chlidMenus = adminMenuService.queryMenuByParentId(menu.getMenuId());
            if ((roleId != null && roleId.equals(BaseConstant.SMALL_WORK_ADMIN_ROLE_ID)) || userId.equals(adminUserId) || user.getRoles().contains(BaseConstant.WORK_ADMIN_ROLE_ID) || user.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                chlidMenus.forEach(child -> {
                    authObject.put(child.getRealm(), true);
                });
            } else {
                if (work.getIsOpen() == 1) {
                    chlidMenus.forEach(child -> {
                        if ("update".equals(child.getRealm()) && !work.getCreateUserId().equals(userId.intValue())) {
                            return;
                        }
                        authObject.put(child.getRealm(), true);
                    });
                } else {
                    menuRecords.forEach(record -> {
                        if (menu.getMenuId().equals(record.getInt("parent_id"))) {
                            authObject.put(record.getStr("realm"), true);
                        }
                    });
                }
            }
            if (!authObject.isEmpty()) {
                object.put(menu.getRealm(), authObject);
            }
        });
        if (!object.isEmpty()) {
            root.put("work", object);
        }
//        work.put("permission", Kv.by("isUpdate", isUpdate)).put("authList", root);
//        return R.ok().put("data", work);
        Map<String, Object> result = DataConverter.objectToMap(work);

        Map<String, Object> isUpdateMap = new HashMap<>();
        isUpdateMap.put("isUpdate", isUpdate);
        result.put("permission", isUpdateMap);

        result.put("authList", root);
        return R.ok().put("data", result);
    }

    /**
     * 查询项目角色类型列表
     *
     * @author wyq
     */
    public R queryRoleList() {
//        return R.ok().put("data", Db.find(Db.getSql("work.queryRoleList")));
        String queryRoleList = "select role_id,role_name,remark from lkcrm_admin_role " +
                "where role_type in (5,6) and status = 1";
        List<Map<String, Object>> result = workDao.queryListBySql(queryRoleList);
        return R.ok().put("data", result);
    }

    /**
     * @author wyq
     * 查询项目成员所属角色列表
     */
    public R queryOwnerRoleList(Integer workId) {
//        Integer isOpen = Db.queryInt("select is_open from lkcrm_work where work_id = ?", workId);
        String isOpenSql = "select is_open from lkcrm_work where work_id = ?";
        Integer isOpen = workDao.queryForInt(isOpenSql, workId);
        if (workId == 0 || isOpen == 1) {
//            return R.ok().put("data", Db.find("select user_id,realname from lkcrm_admin_user"));
            String recordListSql = "select user_id,realname from lkcrm_admin_user";
            List<Map<String, Object>> recordList = workDao.queryListBySql(recordListSql);
            return R.ok().put("data", JavaBeanUtil.mapToRecords(recordList));
        }
//        return R.ok().put("data", Db.find(Db.getSql("work.queryOwnerRoleList"), workId));
        List<Map<String, Object>> result = workDao.queryOwnerRoleList(workId);
        return R.ok().put("data", result);
    }

    /**
     * @author wyq
     * 保存项目成员角色设置
     */
    @Before(Tx.class)
    public R setOwnerRole(JSONObject jsonObject) {
        List<WorkUser> workUserList = jsonObject.getJSONArray("list").toJavaList(WorkUser.class);
        Integer workId = jsonObject.getInteger("workId");
        workUserList.forEach(workUser -> workUser.setWorkId(workId));
//        Db.delete("delete from lkcrm_work_user where work_id = ?", workId);
        String delSql = "delete from lkcrm_work_user where work_id = ?";
        workDao.executeUpdateSQL(delSql, workId);
        Db.batchSave(workUserList, 100);
//        List<Record> recordList = Db.find(Db.getSql("work.queryOwnerRoleList"), workId);
        List<Map<String, Object>> recordList = workDao.queryOwnerRoleList(workId);
        return R.ok().put("data", recordList);
    }

    @Before(Tx.class)
    public R deleteTaskList(String workId, String classId) {
//        Db.update("update  `lkcrm_task` set ishidden = 1,class_id = null,hidden_time = now() where class_id = ? and work_id = ? and is_archive != 1", classId, workId);
        String updateSql = "update  `lkcrm_task` set ishidden = 1,class_id = null,hidden_time = now() " +
                "where class_id = ? and work_id = ? and is_archive != 1";
        workDao.executeUpdateSQL(updateSql, classId, workId);
//        boolean delete = new WorkTaskClass().deleteById(classId);
        workTaskClassDao.delete(Integer.parseInt(classId));
//        return delete ? R.ok() : R.error();
        return R.ok();
    }

    public R archiveTask(String classId) {
//        Integer count = Db.queryInt("select count(*) from `lkcrm_task` where class_id = ? and status = 5 and ishidden = 0", classId);
        String sql = "select count(*) from `lkcrm_task` where class_id = ? and status = 5 and ishidden = 0";
        Integer count = workDao.queryForInt(sql, classId);
        if (count == 0) {
            return R.error("暂无已完成任务，归档失败!");
        }
//        int update = Db.update("update  `lkcrm_task` set is_archive = 1,archive_time = now() where class_id = ? and status = 5 and ishidden = 0", classId);
        String updateSql = "update  `lkcrm_task` set is_archive = 1,archive_time = now() where class_id = ? and status = 5 and ishidden = 0";
        int update = workDao.executeUpdateSQL(updateSql, classId);
        return update > 0 ? R.ok() : R.error();
    }

    public R archList(String workId) {
//        List<Record> recordList = Db.find(Db.getSql("work.archList"), workId);
        List<Record> recordList = JavaBeanUtil.mapToRecords(workDao.archList(workId));
        workbenchService.taskListTransfer(recordList);
        return R.ok().put("data", recordList);
    }

    public R remove(Integer userId, Integer workId) {
//        Integer roleId = Db.queryInt("select role_id from `lkcrm_work_user` where work_id = ? and user_id = ?", workId, userId);
        String roleIdSql = "select role_id from `lkcrm_work_user` where work_id = ? and user_id = ?";
        Integer roleId = workDao.queryForInt(roleIdSql, workId, userId);
        if (roleId.equals(BaseConstant.SMALL_WORK_ADMIN_ROLE_ID)) {
            return R.error("管理员不能被删除！");
        }
//        Work work = new Work().findById(workId);
        LkCrmWorkEntity work = workDao.get(workId);
        Set<Integer> userIds = TagUtil.toSet(work.getOwnerUserId());
        userIds.remove(userId);
        work.setOwnerUserId(TagUtil.fromSet(userIds));
//        boolean update = work.update();
        workDao.saveOrUpdate(work);
//        Db.delete("delete from `lkcrm_work_user` where work_id = ? and user_id = ?", workId, userId);
//        return update ? R.ok() : R.error();
        String delSql = "delete from `lkcrm_work_user` where work_id = ? and user_id = ?";
        workDao.executeUpdateSQL(delSql, workId, userId);
        return R.ok();
    }

    public R updateClassOrder(JSONObject jsonObject) {
        String sql = "update `lkcrm_work_task_class` set order_num = ? where work_id = ? and class_id = ?";
        Integer workId = jsonObject.getInteger("workId");
        JSONArray classIds = jsonObject.getJSONArray("classIds");
        for (int i = 0; i < classIds.size(); i++) {
//            Db.update(sql, i, workId, classIds.get(i));
            workDao.executeUpdateSQL(sql, i, workId, classIds.get(i));
        }
        return R.ok();
    }

    public R activation(Integer taskId) {
//        Task task = new Task().findById(taskId);
        LkCrmTaskEntity task = taskDao.get(taskId);
//        Integer count = Db.queryInt("select count(*) from `lkcrm_work_task_class` where class_id = ?", task.getClassId());
        String countSql = "select count(*) from `lkcrm_work_task_class` where class_id = ?";
        Integer count = taskDao.queryForInt(countSql, task.getClassId());
        int update;
        if (count > 0) {
//            update = Db.update("update  `lkcrm_task` set is_archive = 0,archive_time = null where task_id = ?", taskId);
            String updateSql = "update  `lkcrm_task` set is_archive = 0,archive_time = null where task_id = ?";
            update = taskDao.executeUpdateSQL(updateSql, taskId);
        } else {
//            update = Db.update("update  `lkcrm_task` set is_archive = 0,archive_time = null,class_id = null where task_id = ?", taskId);
            String updateSql = "update  `lkcrm_task` set is_archive = 0,archive_time = null,class_id = null where task_id = ?";
            update = taskDao.executeUpdateSQL(updateSql, task);
        }
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * 项目启动时初始化项目角色
     */
    public void initialization() {
//        BaseConstant.WORK_ADMIN_ROLE_ID = Db.queryInt("select role_id from `lkcrm_admin_role` where label = 1");
        String workAdminRoleSql = "select role_id from `lkcrm_admin_role` where label = 1";
        BaseConstant.WORK_ADMIN_ROLE_ID = taskDao.queryForInt(workAdminRoleSql);
//        BaseConstant.SMALL_WORK_ADMIN_ROLE_ID = Db.queryInt("select role_id from `lkcrm_admin_role` where label = 2");
        String smallAdminSql = "select role_id from `lkcrm_admin_role` where label = 2";
        BaseConstant.SMALL_WORK_ADMIN_ROLE_ID = taskDao.queryForInt(smallAdminSql);
//        BaseConstant.SMALL_WORK_EDIT_ROLE_ID = Db.queryInt("select role_id from `lkcrm_admin_role` where label = 3");
        String smallEditSql = "select role_id from `lkcrm_admin_role` where label = 3";
        BaseConstant.SMALL_WORK_EDIT_ROLE_ID = taskDao.queryForInt(smallEditSql);
    }
}
