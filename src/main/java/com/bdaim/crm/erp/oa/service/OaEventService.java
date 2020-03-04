package com.bdaim.crm.erp.oa.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.LkCrmOaEventDao;
import com.bdaim.crm.dao.LkCrmOaEventRelationDao;
import com.bdaim.crm.entity.LkCrmOaEventEntity;
import com.bdaim.crm.entity.LkCrmOaEventRelationEntity;
import com.bdaim.crm.erp.admin.entity.AdminUser;
import com.bdaim.crm.erp.crm.entity.CrmBusiness;
import com.bdaim.crm.erp.crm.entity.CrmContacts;
import com.bdaim.crm.erp.crm.entity.CrmContract;
import com.bdaim.crm.erp.crm.entity.CrmCustomer;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.entity.OaEvent;
import com.bdaim.crm.erp.oa.entity.OaEventRelation;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@Transactional
public class OaEventService {
    @Resource
    private OaActionRecordService oaActionRecordService;
    @Resource
    private LkCrmOaEventDao crmOaEventDao;
    @Autowired
    private LkCrmOaEventRelationDao relationDao;

    /**
     * @author Chacker
     * 查询日程列表
     */
    public List<Record> queryList(LkCrmOaEventEntity oaEvent) {
        Date startTime = oaEvent.getStartTime();
        Date endTime = oaEvent.getEndTime();
        Integer userId = BaseUtil.getUserId().intValue();
//        List<Record> recordList = Db.find(Db.getSql("oa.event.queryList"), endTime, startTime, userId, userId);
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmOaEventDao.queryList(endTime, startTime, userId));
        if (recordList != null) {
            for (Record record : recordList) {
//                record.set("createUser", Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?",
//                        record.getInt("create_user_id")));
                String sql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
                record.set("createUser",
                        JavaBeanUtil.mapToRecord(crmOaEventDao.queryUniqueSql(sql, record.getInt("create_user_id"))));
                queryRelateList(record);
            }
        }
        return recordList;
    }

    /**
     * @author Chacker
     * 新增日程
     */
    public R add(LkCrmOaEventEntity oaEvent) {
        if (oaEvent.getStartTime() != null && oaEvent.getEndTime() != null) {
            if ((oaEvent.getStartTime().compareTo(oaEvent.getEndTime())) == 1) {
                return R.error("结束时间早于开始时间");
            }
        }
        LkCrmOaEventRelationEntity oaEventRelation = new LkCrmOaEventRelationEntity();
        oaEventRelation.setCustomerIds(TagUtil.fromString(oaEvent.getCustomerIds()));
        oaEventRelation.setContactsIds(TagUtil.fromString(oaEvent.getContactsIds()));
        oaEventRelation.setBusinessIds(TagUtil.fromString(oaEvent.getBusinessIds()));
        oaEventRelation.setContractIds(TagUtil.fromString(oaEvent.getContractIds()));
        oaEvent.setCreateUserId(BaseUtil.getUser().getUserId());
        oaEvent.setCreateTime(new Timestamp(System.currentTimeMillis()));
        oaEvent.setOwnerUserIds(TagUtil.fromString(oaEvent.getOwnerUserIds()));
        oaEventRelation.setCreateTime(new Timestamp(System.currentTimeMillis()));
        LoginUser user = BaseUtil.getUser();
        return Db.tx(() -> {
//            oaEvent.save();
            crmOaEventDao.save(oaEvent);
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(), 1, oaActionRecordService.getJoinIds(user.getUserId().intValue(), oaEvent.getOwnerUserIds()), "");
            oaEventRelation.setEventId(oaEvent.getEventId());
//            oaEventRelation.save();
            relationDao.save(oaEventRelation);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author Chacker
     * 更新日程
     */
    public R update(LkCrmOaEventEntity oaEvent) {
        if (oaEvent.getStartTime() != null && oaEvent.getEndTime() != null) {
            if ((oaEvent.getStartTime().compareTo(oaEvent.getEndTime())) == 1) {
                return R.error("结束时间早于开始时间");
            }
        }
        LkCrmOaEventRelationEntity oaEventRelation = new LkCrmOaEventRelationEntity();
        oaEventRelation.setEventId(oaEvent.getEventId());
        oaEventRelation.setCustomerIds(TagUtil.fromString(oaEvent.getCustomerIds()));
        oaEventRelation.setContactsIds(TagUtil.fromString(oaEvent.getContactsIds()));
        oaEventRelation.setBusinessIds(TagUtil.fromString(oaEvent.getBusinessIds()));
        oaEventRelation.setContractIds(TagUtil.fromString(oaEvent.getContractIds()));
        oaEvent.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        oaEvent.setOwnerUserIds(TagUtil.fromString(oaEvent.getOwnerUserIds()));
        LoginUser user = BaseUtil.getUser();
        return Db.tx(() -> {
//            oaEvent.update();
            crmOaEventDao.save(oaEvent);
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(), 2, oaActionRecordService.getJoinIds(user.getUserId().intValue(), oaEvent.getOwnerUserIds()), "");
            oaEventRelation.setEventId(oaEvent.getEventId());
//            Record eventRelation = Db.findFirst("select eventrelation_id from lkcrm_oa_event_relation where event_id = ?",
//                    oaEvent.getEventId());
            String sql = "select eventrelation_id from lkcrm_oa_event_relation where event_id = ?";
            Record eventRelation = JavaBeanUtil.mapToRecord(crmOaEventDao.queryUniqueSql(sql, oaEvent.getEventId()));
            oaEventRelation.setEventrelationId(eventRelation.getInt("eventrelation_id"));
//            oaEventRelation.update();
            relationDao.update(oaEventRelation);
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author Chacker
     * 删除日程
     */
    public R delete(Integer eventId) {
        oaActionRecordService.deleteRecord(OaEnum.EVENT_TYPE_KEY.getTypes(), eventId);
//        return Db.delete(Db.getSql("oa.event.delete"), eventId) > 0 ? R.ok() : R.error();
        String delSql = "delete from lkcrm_oa_event where event_id = ?";
        int update = crmOaEventDao.executeUpdateSQL(delSql, eventId);
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * @author Chacker
     * crm查询日程
     */
    public R queryEventRelation(BasePageRequest<OaEventRelation> basePageRequest) {
        OaEventRelation relation = basePageRequest.getData();
        if (AuthUtil.oaAnth(relation.toRecord())) {
            return R.noAuth();
        }
        com.bdaim.common.dto.Page recordPage = crmOaEventDao.queryEventRelation(basePageRequest.getPage(), basePageRequest.getLimit(), relation.getBusinessIds(), relation.getContactsIds(), relation.getContractIds(), relation.getCustomerIds());
        //Page<Record> recordPage = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("oa.event.queryEventRelation", Kv.by("businessIds", relation.getBusinessIds()).set("contactsIds", relation.getContactsIds()).set("contractIds", relation.getContractIds()).set("customerIds", relation.getCustomerIds())));
        List<Record> list = JavaBeanUtil.mapToRecords(recordPage.getData());
        for (Record record : list) {
            record.set("createUser", Kv.by("user_id", record.get("create_user_id")).set("realname", record.get("realname")).set("img", record.get("img")));
            queryRelateList(record);
        }
        return R.ok().put("data", BaseUtil.crmPage(recordPage));
    }

    public void queryRelateList(Record record) {
        if (record.getInt("create_user_id") == BaseUtil.getUser().getUserId().intValue()) {
            record.set("permission", Kv.by("is_update", 1).set("is_delete", 1));
        } else {
            record.set("permission", Kv.by("is_update", 0).set("is_delete", 0));
        }
        List<AdminUser> adminUserList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("owner_user_ids"))) {
            String[] ownerUserIdsArr = record.getStr("owner_user_ids").split(",");
            adminUserList = AdminUser.dao.find(Db.getSqlPara("oa.event.queryOwnerList", Kv.by("ids", ownerUserIdsArr)));
        }
        record.set("ownerList", adminUserList);
        List<CrmCustomer> customerList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("customer_ids"))) {
            String[] customerIdsArr = record.getStr("customer_ids").split(",");
            customerList = CrmCustomer.dao.find(Db.getSqlPara("oa.event.queryCustomerList", Kv.by("ids", customerIdsArr)));
        }
        record.set("customerList", customerList);
        List<CrmContacts> contactsList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("contacts_ids"))) {
            String[] contactsIdsArr = record.getStr("contacts_ids").split(",");
            contactsList = CrmContacts.dao.find(Db.getSqlPara("oa.event.queryContactsList", Kv.by("ids", contactsIdsArr)));
        }
        record.set("contactsList", contactsList);
        List<CrmBusiness> businessList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("business_ids"))) {
            String[] businessIdsArr = record.getStr("business_ids").split(",");
            businessList = CrmBusiness.dao.find(Db.getSqlPara("oa.event.queryBusinessList", Kv.by("ids", businessIdsArr)));
        }
        record.set("businessList", businessList);
        List<CrmContract> contractList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("contract_ids"))) {
            String[] contractIdsArr = record.getStr("contract_ids").split(",");
            contractList = CrmContract.dao.find(Db.getSqlPara("oa.event.queryContractList", Kv.by("ids", contractIdsArr)));
        }
        record.set("contractList", contractList);
    }

    public Record queryById(Integer eventId) {
//        Record record = Db.findFirst(Db.getSql("oa.event.queryById"), eventId);
        Record record = JavaBeanUtil.mapToRecord(crmOaEventDao.queryById(eventId));
//        record.set("createUser", Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?",
//                record.getInt("create_user_id")));
        String sql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
        Record createUser = JavaBeanUtil.mapToRecord(crmOaEventDao.queryUniqueSql(sql,
                record.getInt("create_user_id")));
        record.set("createUser", createUser);
        queryRelateList(record);
        return record;
    }

}
