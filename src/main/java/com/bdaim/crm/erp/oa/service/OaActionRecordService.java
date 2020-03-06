package com.bdaim.crm.erp.oa.service;

import cn.hutool.core.date.DateTime;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.LkCrmOaActionRecordDao;
import com.bdaim.crm.dao.LkCrmOaEventDao;
import com.bdaim.crm.entity.LkCrmOaActionRecordEntity;
import com.bdaim.crm.erp.admin.service.AdminUserService;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.entity.OaActionRecord;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.CrmPage;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.SqlPara;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OaActionRecordService {

    @Resource
    private OaLogService oaLogService;

    @Resource
    private AdminUserService adminUserService;

    @Resource
    private OaEventService oaEventService;
    @Autowired
    private LkCrmOaEventDao eventDao;

    @Autowired
    private LkCrmOaActionRecordDao recordDao;

    /**
     * 添加日志记录
     *
     * @param actionId 操作对象id
     * @param types    操作类型
     * @param status   1 添加 2 更新
     */
    public void addRecord(Integer actionId, Integer types, Integer status, String joinUserIds, String deptIds) {
        LkCrmOaActionRecordEntity oaActionRecord = new LkCrmOaActionRecordEntity();
        oaActionRecord.setUserId(BaseUtil.getUser().getUserId());
        oaActionRecord.setType(types);
        oaActionRecord.setActionId(actionId);
        oaActionRecord.setCreateTime(DateUtil.date().toTimestamp());
        oaActionRecord.setJoinUserIds(joinUserIds);
        oaActionRecord.setDeptIds(deptIds);
        if (status == 1) {
            oaActionRecord.setContent("添加了" + OaEnum.getName(types));
        } else if (status == 2) {
            oaActionRecord.setContent("更新了" + OaEnum.getName(types));
        }
        recordDao.save(oaActionRecord);
    }

    public String getJoinIds(Integer id, String ids) {
        StringBuilder joinIds = new StringBuilder(",").append(id);
        if (StrUtil.isNotEmpty(ids)) {
            joinIds.append(ids);
        } else {
            joinIds.append(",");
        }
        return TagUtil.fromString(joinIds.toString());
    }


    public R getOaRecordPageList(BasePageRequest<OaActionRecord> pageRequest) {
        Integer type = pageRequest.getData().getType();
        LoginUser user = BaseUtil.getUser();
        SqlPara sqlPara;
        List<Long> userIdList;
        if (user.getRoles() != null && user.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
            String sql = "SELECT user_id FROM `lkcrm_admin_user` where user_id != ?";
            userIdList = recordDao.queryListForLong(sql, user.getUserId());
        } else {
            userIdList = adminUserService.queryUserByParentUser(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
        }
        userIdList.add(user.getUserId());
        Page page;
        if (type.equals(OaEnum.ALL_TYPE_KEY.getTypes())) {
            page = recordDao.queryList(pageRequest.getPage(),pageRequest.getLimit(),
                    userIdList,user.getUserId(),user.getDeptId(),null);
        } else {
            page = recordDao.queryList(pageRequest.getPage(),pageRequest.getLimit(),
                    userIdList,user.getUserId(),user.getDeptId(),type);
        }
        List<Record> recordList = page.getData();
        recordList.forEach(record -> {
            record.set("type_name", OaEnum.getName(record.getInt("type")));
            Integer userId = record.getInt("user_id");
            String userSql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
            record.set("createUser", JavaBeanUtil.mapToRecord(recordDao.queryUniqueSql(userSql, userId)));
            Record info = new Record();
            Integer actionId = record.getInt("action_id");
            Integer recordType = record.getInt("type");
            if (recordType.equals(OaEnum.LOG_TYPE_KEY.getTypes())) {
                info = Db.findFirst(Db.getSqlPara("oa.log.queryList", Kv.by("logId", actionId)));
                if (info != null) {
                    oaLogService.queryLogDetail(info, BaseUtil.getUser().getUserId());
                }
            } else if (recordType.equals(OaEnum.EXAMINE_TYPE_KEY.getTypes())) {
                String infoSql = "select content as title from lkcrm_oa_examine where examine_id = ?";
                info = JavaBeanUtil.mapToRecord(recordDao.queryUniqueSql(infoSql, actionId));
            } else if (recordType.equals(OaEnum.TASK_TYPE_KEY.getTypes())) {
                String infoSql = "select name as title from lkcrm_task where task_id = ?";
                info = JavaBeanUtil.mapToRecord(recordDao.queryUniqueSql(infoSql, actionId));
            } else if (recordType.equals(OaEnum.EVENT_TYPE_KEY.getTypes())) {
                String infoSql = "select title  from lkcrm_oa_event where event_id = ?";
                info = JavaBeanUtil.mapToRecord(recordDao.queryUniqueSql(infoSql, actionId));
                if (info != null) {
                    Record first = JavaBeanUtil.mapToRecord(eventDao.queryById(actionId));
                    first.remove("type");
                    oaEventService.queryRelateList(first);
                    info.setColumns(first);
                }
            } else if (recordType.equals(OaEnum.ANNOUNCEMENT_TYPE_KEY.getTypes())) {
                String infoSql = "select title,content as annContent from lkcrm_oa_announcement where announcement_id = ?";
                info = JavaBeanUtil.mapToRecord(recordDao.queryUniqueSql(infoSql, actionId));
            }
            if (info != null) {
                Date createTime = record.getDate("create_time");
                record.setColumns(info).set("create_time", createTime);
            }
        });
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    public R queryEvent(String month) {
        DateTime dateTime;
        if (month == null) {
            dateTime = DateUtil.beginOfMonth(new Date());
        } else {
            dateTime = DateUtil.parse(month, "yyyy-MM");
        }
        Long userId = BaseUtil.getUser().getUserId();
        int nowMonth = dateTime.month();
        StringBuilder sql = new StringBuilder();
        do {
            sql.append(" select (select '").append(dateTime.toSqlDate()).append("' )as date,if(count(*)>0,1,0) as status from lkcrm_oa_event where (create_user_id = ").append(userId).append(" or owner_user_ids like concat('%',").append(userId).append(",'%')) and '").append(dateTime.toSqlDate()).append("' between date_format(start_time,'%Y-%m-%d') and date_format(end_time,'%Y-%m-%d') ").append("union all");
            dateTime = DateUtil.offsetDay(dateTime, 1);
        } while (dateTime.month() == nowMonth);
        sql.delete(sql.length() - 9, sql.length());
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordDao.queryListBySql(sql.toString()));
        return R.ok().put("data", recordList);
    }

    public R queryEventByDay(String day) {
        Long userId = BaseUtil.getUser().getUserId();
        String sql = "SELECT " +
                " event_id, " +
                " title, " +
                " date_format( start_time, '%Y-%m-%d' ) AS start_time, " +
                " date_format( end_time, '%Y-%m-%d' ) AS end_time, " +
                " owner_user_ids  " +
                "FROM " +
                " lkcrm_oa_event  " +
                "WHERE " +
                " ( create_user_id = ? OR owner_user_ids LIKE concat( '%', ?, '%' ) )  " +
                " AND ? BETWEEN date_format( start_time, '%Y-%m-%d' )  " +
                " AND date_format( end_time, '%Y-%m-%d' )";
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordDao.queryListBySql(sql, userId, userId, day));

        recordList.forEach(record -> {
            StringBuilder realnames = new StringBuilder();
            if (StrUtil.isNotEmpty(record.getStr("owner_user_ids"))) {
                String[] ownerUserIds = record.getStr("owner_user_ids").split(",");
                for (String ownerUserId : ownerUserIds) {
                    if (StrUtil.isNotBlank(ownerUserId)) {
                        String realNameSql = "select realname from lkcrm_admin_user where user_id = ?";
                        String realname = recordDao.queryForObject(realNameSql, ownerUserId);
                        realnames.append(realname).append("、");
                    }
                }
                realnames.delete(realnames.length() - 1, realnames.length());
            }
            record.set("realnames", realnames.toString());

        });
        return R.ok().put("data", recordList);
    }


    public R queryTask() {
        Long userId = BaseUtil.getUser().getUserId();
        String sql = "SELECT " +
                " task_id,NAME,create_time,stop_time,priority  " +
                "FROM " +
                " lkcrm_task  " +
                "WHERE " +
                " ishidden = 0  " +
                " AND ( main_user_id = ? OR owner_user_id LIKE concat( '%',?, '%' ) )  " +
                " AND pid = 0  " +
                "ORDER BY " +
                " create_time DESC";
        List<Record> recordList = JavaBeanUtil.mapToRecords(recordDao.queryListBySql(sql, userId, userId));
        return R.ok().put("data", recordList);
    }

    public void deleteRecord(Integer type, Integer id) {
        String sql = "delete from lkcrm_oa_action_record where type = ? and action_id = ?";
        recordDao.executeUpdateSQL(sql, type, id);
    }


}
