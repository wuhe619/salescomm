package com.bdaim.crm.erp.oa.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.*;
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
import java.util.*;

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
        Long userId = BaseUtil.getUserId();
//        List<Record> recordList = Db.find(Db.getSql("oa.event.queryList"), endTime, startTime, userId, userId);
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmOaEventDao.queryList(endTime, startTime, userId));
        if (recordList != null) {
            for (Record record : recordList) {
//                record.set("createUser", Db.findFirst("select user_id,realname,img from lkcrm_admin_user where user_id = ?",
//                        record.getInt("create_user_id")));
                String sql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
                record.set("createUser",
                        JavaBeanUtil.mapToRecord(crmOaEventDao.queryUniqueSql(sql, record.getLong("create_user_id"))));
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
        LoginUser user = BaseUtil.getUser();
        LkCrmOaEventRelationEntity oaEventRelation = new LkCrmOaEventRelationEntity();
        oaEventRelation.setCustomerIds(TagUtil.fromString(oaEvent.getCustomerIds()));
        oaEventRelation.setContactsIds(TagUtil.fromString(oaEvent.getContactsIds()));
        oaEventRelation.setBusinessIds(TagUtil.fromString(oaEvent.getBusinessIds()));
        oaEventRelation.setContractIds(TagUtil.fromString(oaEvent.getContractIds()));
        oaEvent.setCustId(user.getCustId());
        oaEvent.setCreateUserId(user.getUserId());
        oaEvent.setCreateTime(new Timestamp(System.currentTimeMillis()));
        oaEvent.setOwnerUserIds(TagUtil.fromString(oaEvent.getOwnerUserIds()));
        oaEventRelation.setCreateTime(new Timestamp(System.currentTimeMillis()));
        //return Db.tx(() -> {
//            oaEvent.save();
        crmOaEventDao.save(oaEvent);
        oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(), 1, oaActionRecordService.getJoinIds(user.getUserId().intValue(), oaEvent.getOwnerUserIds()), "");
        oaEventRelation.setEventId(oaEvent.getEventId());
//            oaEventRelation.save();
        relationDao.save(oaEventRelation);
        return R.ok();
        //}) ? R.ok() : R.error();
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
        LoginUser user = BaseUtil.getUser();
        oaEvent.setCustId(user.getCustId());
        oaEvent.setUpdateTime(new Timestamp(System.currentTimeMillis()));
        oaEvent.setOwnerUserIds(TagUtil.fromString(oaEvent.getOwnerUserIds()));
        //return Db.tx(() -> {
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
            return R.ok();
        //}) ? R.ok() : R.error();
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
        List<Record> list = JavaBeanUtil.mapToRecords(recordPage.getData());
        for (Record record : list) {
            record.set("createUser", Kv.by("user_id", record.get("create_user_id")).set("realname", record.get("realname")).set("img", record.get("img")));
            queryRelateList(record);
        }
        recordPage.setData(list);
        return R.ok().put("data", BaseUtil.crmPage(recordPage));
    }

    public void queryRelateList(Record record) {
        if (record.getLong("create_user_id") == BaseUtil.getUser().getUserId()) {
            record.set("permission", Kv.by("is_update", 1).set("is_delete", 1));
        } else {
            record.set("permission", Kv.by("is_update", 0).set("is_delete", 0));
        }
        List<Map<String, Object>> adminUserList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("owner_user_ids"))) {
            String[] ownerUserIdsArr = record.getStr("owner_user_ids").split(",");
            adminUserList = crmOaEventDao.queryOwnerList(Arrays.asList(ownerUserIdsArr));
            //adminUserList = AdminUser.dao.find(Db.getSqlPara("oa.event.queryOwnerList", Kv.by("ids", ownerUserIdsArr)));
        }
        record.set("ownerList", adminUserList);
        List<Map<String, Object>> customerList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("customer_ids"))) {
            String[] customerIdsArr = record.getStr("customer_ids").split(",");
            customerList = crmOaEventDao.queryCustomerList(Arrays.asList(customerIdsArr));
           // customerList = CrmCustomer.dao.find(Db.getSqlPara("oa.event.queryCustomerList", Kv.by("ids", customerIdsArr)));
        }
        record.set("customerList", customerList);
        List<Map<String, Object>> contactsList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("contacts_ids"))) {
            String[] contactsIdsArr = record.getStr("contacts_ids").split(",");
            contactsList = crmOaEventDao.queryContactsList(Arrays.asList(contactsIdsArr));
            //contactsList = CrmContacts.dao.find(Db.getSqlPara("oa.event.queryContactsList", Kv.by("ids", contactsIdsArr)));
        }
        record.set("contactsList", contactsList);
        List<Map<String, Object>> businessList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("business_ids"))) {
            String[] businessIdsArr = record.getStr("business_ids").split(",");
            businessList = crmOaEventDao.queryBusinessList(Arrays.asList(businessIdsArr));
            //businessList = CrmBusiness.dao.find(Db.getSqlPara("oa.event.queryBusinessList", Kv.by("ids", businessIdsArr)));
        }
        record.set("businessList", businessList);
        List<Map<String, Object>> contractList = new ArrayList<>();
        if (StrUtil.isNotEmpty(record.getStr("contract_ids"))) {
            String[] contractIdsArr = record.getStr("contract_ids").split(",");
            contractList = crmOaEventDao.queryContractList(Arrays.asList(contractIdsArr));
            //contractList = CrmContract.dao.find(Db.getSqlPara("oa.event.queryContractList", Kv.by("ids", contractIdsArr)));
        }
        record.set("contractList", contractList);
    }

    public Record queryById(Integer eventId) {
        Record record = JavaBeanUtil.mapToRecord(crmOaEventDao.queryById(eventId));
        String sql = "select user_id,realname,img from lkcrm_admin_user where user_id = ?";
        Record createUser = JavaBeanUtil.mapToRecord(crmOaEventDao.queryUniqueSql(sql,
                record.getInt("create_user_id")));
        record.set("createUser", createUser);
        queryRelateList(record);
        return record;
    }

}
