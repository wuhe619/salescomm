package com.bdaim.crm.erp.oa.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.erp.admin.service.LkAdminUserService;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.entity.OaLog;
import com.bdaim.crm.erp.oa.entity.OaLogRelation;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class OaLogService {
    //添加日志
    @Resource
    private OaActionRecordService oaActionRecordService;
    @Resource
    private AdminFileService adminFileService;
    @Resource
    private OaCommentService commentService;
    @Resource
    private LkCrmOaLogDao crmOaLogDao;
    @Resource
    private LkAdminUserService adminUserService;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private LkCrmAdminDeptDao crmAdminDeptDao;
    @Resource
    private LkCrmContractDao crmContractDao;
    @Resource
    private LkCrmCustomerDao crmCustomerDao;
    @Resource
    private LkCrmBusinessDao crmBusinessDao;
    @Resource
    private LkCrmContactsDao crmContactsDao;


    /**
     * 查询日志
     *
     * @param basePageRequest 分页参数
     * @author Chacker
     */
    public Page<Record> queryList(BasePageRequest<OaLog> basePageRequest) {
        JSONObject object = basePageRequest.getJsonObject();
        LoginUser user = BaseUtil.getUser();
        Integer by = TypeUtils.castToInt(object.getOrDefault("by", 4));
        Kv kv = Kv.by("by", by);
        List<Long> userIds;
        if (user.getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
//            userIds = Db.query("SELECT user_id FROM `lkcrm_admin_user` where user_id != ? ", user.getUserId());
            String userIdsSql = "SELECT user_id FROM `lkcrm_admin_user` where user_id != ?";
            userIds = crmOaLogDao.queryListForLong(userIdsSql, user.getUserId());
        } else {
            userIds = adminUserService.queryUserByParentUser(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM);
            if (object.containsKey("createUserId")) {
                if (!userIds.contains(Long.valueOf(object.getInteger("createUserId")))) {
                    return new Page<>();
                }
            }
        }
        if (by == 1) {
            kv.set("create_user_id", user.getUserId());
        } else if (by == 2) {
            kv.set("send_user_ids", user.getUserId()).set("send_dept_ids", user.getDeptId()).set("userIds", userIds);
        } else if (by == 3) {
            kv.set("send_user_ids", user.getUserId()).set("send_dept_ids", user.getDeptId()).set("userIds", userIds).set("userId", user.getUserId());
        } else {
            userIds.add(user.getUserId());
            kv.set("send_user_ids", user.getUserId()).set("send_dept_ids", user.getDeptId()).set("userIds", userIds);
        }
        if (object.containsKey("createUserId")) {
            kv.set("create_user_id", object.get("createUserId"));
        }
        if (object.containsKey("createTime")) {
            kv.set("create_time", object.get("createTime"));
        }
        if (object.containsKey("categoryId") && !"0".equals(object.get("categoryId"))) {
            kv.set("category_id", object.get("categoryId"));
        }
        Page<Record> recordList = Db.paginate(basePageRequest.getPage(),
                basePageRequest.getLimit(), Db.getSqlPara("oa.log.queryList", kv));
        recordList.getList().forEach((record -> {
            queryLogDetail(record, user.getUserId());
        }));
        return recordList;
    }

    public void queryLogDetail(Record record, Long userId) {
        adminFileService.queryByBatchId(record.get("batch_id"), record);
        record.set("sendUserList", (StrUtil.isNotEmpty(record.getStr("send_user_ids")) && record.getStr("send_user_ids").split(",").length > 0) ? Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", record.getStr("send_user_ids").split(",")))) : new ArrayList<>());
        record.set("sendDeptList", (StrUtil.isNotEmpty(record.getStr("send_dept_ids")) && record.getStr("send_dept_ids").split(",").length > 0) ? crmAdminDeptDao.queryByIds(Arrays.asList(record.getStr("send_dept_ids").split(","))) : new ArrayList<>());
        record.set("customerList", (StrUtil.isNotEmpty(record.getStr("customer_ids")) && record.getStr("customer_ids").split(",").length > 0) ? crmCustomerDao.queryByIds(Arrays.asList(record.getStr("customer_ids").split(","))) : new ArrayList<>());
        record.set("businessList", (StrUtil.isNotEmpty(record.getStr("business_ids")) && record.getStr("business_ids").split(",").length > 0) ? crmBusinessDao.queryByIds(Arrays.asList(record.getStr("business_ids").split(","))) : new ArrayList<>());
        record.set("contactsList", (StrUtil.isNotEmpty(record.getStr("contacts_ids")) && record.getStr("contacts_ids").split(",").length > 0) ? crmContactsDao.queryByIds(Arrays.asList(record.getStr("contacts_ids").split(","))) : new ArrayList<>());
        record.set("contractList", (StrUtil.isNotEmpty(record.getStr("contract_ids")) && record.getStr("contract_ids").split(",").length > 0) ? crmContractDao.queryByIds(Arrays.asList(record.getStr("contract_ids").split(","))) : new ArrayList<>());
        record.set("createUser", customerUserDao.get(record.getLong("create_user_id")));
        Integer isRead = record.getStr("read_user_ids").contains("," + userId + ",") ? 1 : 0;
        int isEdit = userId.intValue() == record.getInt("create_user_id") ? 1 : 0;
        int isDel = 0;
        if ((System.currentTimeMillis() - 1000 * 3600 * 72) > record.getDate("create_time").getTime()) {
            if (BaseUtil.getUser().getRoles().contains(BaseConstant.SUPER_ADMIN_ROLE_ID)) {
                isDel = 1;
            }
        } else {
            if (isEdit == 1 || adminUserService.queryUserByParentUser(userId, BaseConstant.AUTH_DATA_RECURSION_NUM).contains(record.getLong("create_user_id"))) {
                isDel = 1;
            }
        }
        record.set("permission", new JSONObject().fluentPut("is_update", isEdit).fluentPut("is_delete", isDel));
        record.set("is_read", isRead);
        record.set("replyList", commentService.queryCommentList(record.get("log_id").toString(), "2"));

    }

    /**
     * @param object object
     * @return 响应结果
     * @author Chacker
     */
    @Before(Tx.class)
    public R saveAndUpdate(JSONObject object) {
        LoginUser user = BaseUtil.getUser();
        OaLog oaLog = object.toJavaObject(OaLog.class);
        OaLogRelation oaLogRelation = object.toJavaObject(OaLogRelation.class);
        oaLog.setCreateUserId(user.getUserId().intValue());
        oaLog.setCreateTime(new Date());
        oaLog.setReadUserIds(",,");
        oaLog.setSendUserIds(TagUtil.fromString(oaLog.getSendUserIds()));
        oaLog.setSendDeptIds(TagUtil.fromString(oaLog.getSendDeptIds()));
        if (oaLog.getLogId() != null) {
            boolean oaAuth = AuthUtil.isOaAuth(OaEnum.LOG_TYPE_KEY.getTypes(), oaLog.getLogId());
            if (oaAuth) {
                return R.noAuth();
            }
            oaLog.update();
            oaActionRecordService.addRecord(oaLog.getLogId(), OaEnum.LOG_TYPE_KEY.getTypes(), 2, oaActionRecordService.getJoinIds(user.getUserId().intValue(), oaLog.getSendUserIds()), oaLog.getSendDeptIds());
        } else {
            oaLog.save();
            oaActionRecordService.addRecord(oaLog.getLogId(), OaEnum.LOG_TYPE_KEY.getTypes(), 1, oaActionRecordService.getJoinIds(user.getUserId().intValue(), oaLog.getSendUserIds()), oaLog.getSendDeptIds());
        }
        if (oaLogRelation != null) {
            Db.deleteById("lkcrm_oa_log_relation", "log_id", oaLog.getLogId());
            oaLogRelation.setLogId(oaLog.getLogId());
            oaLogRelation.setBusinessIds(TagUtil.fromString(oaLogRelation.getBusinessIds()));
            oaLogRelation.setContactsIds(TagUtil.fromString(oaLogRelation.getContactsIds()));
            oaLogRelation.setContractIds(TagUtil.fromString(oaLogRelation.getContractIds()));
            oaLogRelation.setCustomerIds(TagUtil.fromString(oaLogRelation.getCustomerIds()));
            oaLogRelation.setCreateTime(DateUtil.date());
            oaLogRelation.save();
        }
        return R.ok();
    }


    /**
     * 根据id获取日志
     *
     * @param id 日志ID
     * @author Chacker
     */
    public Record queryById(Integer id) {
        String sql = "SELECT " +
                " a.*,b.dept_id,b.realname,b.img AS userImg, " +
                " soal.customer_ids,soal.contacts_ids,soal.business_ids, " +
                " soal.contract_ids  " +
                "FROM " +
                " lkcrm_oa_log AS a " +
                " LEFT JOIN lkcrm_admin_user AS b ON a.create_user_id = b.user_id " +
                " LEFT JOIN lkcrm_oa_log_relation AS soal ON soal.log_id = a.log_id  " +
                "WHERE 1 = 1 AND a.log_id = ?";
        Record record = JavaBeanUtil.mapToRecord(crmOaLogDao.queryUniqueSql(sql, id));
        queryLogDetail(record, BaseUtil.getUser().getUserId());
        return record;
    }

    /**
     * 根据id删除日志
     *
     * @param logId 日志ID
     * @author Chacker
     */
    @Before(Tx.class)
    public boolean deleteById(Integer logId) {
        OaLog oaLog = OaLog.dao.findById(logId);
        if (oaLog != null) {
            oaActionRecordService.deleteRecord(OaEnum.LOG_TYPE_KEY.getTypes(), logId);
            Db.deleteById("lkcrm_oa_log_relation", "log_id", logId);
            adminFileService.removeByBatchId(oaLog.getBatchId());
            Db.deleteById("lkcrm_oa_log", "log_id", logId);
            return true;
        }
        return false;
    }

    /**
     * TODO 目前可能会产生脏读，
     *
     * @param logId 日志ID
     * @author Chacker
     */
    public void readLog(Integer logId) {
        OaLog oaLog = OaLog.dao.findById(logId);
        HashSet<String> hashSet = new HashSet<>(StrUtil.splitTrim(oaLog.getReadUserIds(), ","));
        hashSet.add(BaseUtil.getUser().getUserId().toString());
        oaLog.setReadUserIds("," + String.join(",", hashSet) + ",");
        oaLog.update();
    }

    /**
     * 查询crm关联日志
     */
    public R queryLogRelation(BasePageRequest<OaLogRelation> basePageRequest) {
        OaLogRelation relation = basePageRequest.getData();
        if (AuthUtil.oaAnth(relation.toRecord())) {
            return R.noAuth();
        }
        com.bdaim.common.dto.Page page = crmOaLogDao.pageQueryLogRelation(basePageRequest.getPage(), basePageRequest.getLimit(), relation.getBusinessIds(), relation.getContactsIds(), relation.getContractIds(), relation.getCustomerIds());
        //Page<Record> recordPage = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("oa.log.queryLogRelation", Kv.by("businessIds", relation.getBusinessIds()).set("contactsIds", relation.getContactsIds()).set("contractIds", relation.getContractIds()).set("customerIds", relation.getCustomerIds())));
        LoginUser user = BaseUtil.getUser();
        page.getData().forEach((record -> {
            queryLogDetail(JavaBeanUtil.mapToRecord((Map<String, Object>) record), user.getUserId());
        }));
        return R.ok().put("data", BaseUtil.crmPage(page));
    }
}
