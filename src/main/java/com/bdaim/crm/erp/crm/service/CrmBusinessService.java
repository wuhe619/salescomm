package com.bdaim.crm.erp.crm.service;


import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.entity.*;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.entity.CrmBusiness;
import com.bdaim.crm.erp.crm.entity.CrmContacts;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.service.OaActionRecordService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.FieldUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
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
public class CrmBusinessService {
    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private FieldUtil fieldUtil;

    @Resource
    private CrmRecordService crmRecordService;

    @Resource
    private AdminFileService adminFileService;

    @Resource
    private OaActionRecordService oaActionRecordService;

    @Resource
    private AuthUtil authUtil;

    @Resource
    private LkCrmBusinessDao crmBusinessDao;

    @Resource
    private LkCrmProductDao crmProductDao;

    @Resource
    private LkCrmCustomerDao crmCustomerDao;

    @Resource
    private LkCrmOaEventDao crmOaEventDao;

    @Resource
    private LkCrmAdminRecordDao crmAdminRecordDao;

    @Resource
    private LkCrmContactsDao crmContactsDao;
    /**
     * @author wyq
     * 分页条件查询商机
     */
    public Page getBusinessPageList(BasePageRequest basePageRequest) {
        return crmBusinessDao.sqlPageQuery("select * from businessview", basePageRequest.getPage(), basePageRequest.getLimit());
        //return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql("select * from businessview"));
    }

    /**
     * @author wyq
     * 新增或更新商机
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject jsonObject) {
        LkCrmBusinessEntity crmBusiness = jsonObject.getObject("entity", LkCrmBusinessEntity.class);
        JSONArray jsonArray = jsonObject.getJSONArray("product");
        List<LkCrmBusinessProductEntity> businessProductList = jsonArray.toJavaList(LkCrmBusinessProductEntity.class);
        //Db.delete(Db.getSql("crm.business.clearBusinessProduct"), crmBusiness.getBusinessId());
        crmBusinessDao.clearBusinessProduct(crmBusiness.getBusinessId());
        String batchId = StrUtil.isNotEmpty(crmBusiness.getBatchId()) ? crmBusiness.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        boolean saveOrUpdate;
        if (crmBusiness.getBusinessId() != null) {
            crmBusiness.setUpdateTime(DateUtil.date().toTimestamp());
            crmRecordService.updateRecord(crmBusinessDao.get(crmBusiness.getBusinessId()), crmBusiness, CrmEnum.BUSINESS_TYPE_KEY.getTypes());
            CrmBusiness oldBusiness = CrmBusiness.dao.findById(crmBusiness.getBusinessId());
            if (!oldBusiness.getStatusId().equals(crmBusiness.getStatusId())) {
                LkCrmBusinessChangeEntity change = new LkCrmBusinessChangeEntity();
                change.setBusinessId(crmBusiness.getBusinessId());
                change.setStatusId(crmBusiness.getStatusId());
                change.setCreateTime(DateUtil.date().toTimestamp());
                change.setCreateUserId(BaseUtil.getUserId().intValue());
                crmBusinessDao.saveOrUpdate(change);
            }
            crmBusinessDao.update(crmBusiness);
            saveOrUpdate = true;
        } else {
            crmBusiness.setCreateTime(DateUtil.date().toTimestamp());
            crmBusiness.setUpdateTime(DateUtil.date().toTimestamp());
            crmBusiness.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmBusiness.setOwnerUserId(BaseUtil.getUser().getUserId().intValue());
            crmBusiness.setBatchId(batchId);
            crmBusiness.setRwUserId(",");
            crmBusiness.setRoUserId(",");
            saveOrUpdate = (int) crmBusinessDao.saveReturnPk(crmBusiness) > 0 ? true : false;
            crmRecordService.addRecord(crmBusiness.getBusinessId(), CrmEnum.BUSINESS_TYPE_KEY.getTypes());
        }
        if (businessProductList != null) {
            for (LkCrmBusinessProductEntity crmBusinessProduct : businessProductList) {
                crmBusinessProduct.setBusinessId(crmBusiness.getBusinessId());
                crmBusinessDao.saveOrUpdate(crmBusinessProduct);
            }
        }
        return saveOrUpdate ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 根据商机id查询
     */
    public R queryById(Integer businessId) {
        // R.ok().put("data", CrmBusiness.dao.findFirst(Db.getSql("crm.business.queryById"), businessId));
        return R.ok().put("data", crmBusinessDao.queryById(businessId).get(0));
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer busienssId) {
        Record record = Db.findFirst(Db.getSql("crm.business.queryById"), busienssId);
        if (null == record) {
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("商机名称", record.getStr("business_name")).set("商机状态组", record.getStr("type_name")).set("商机阶段", record.getStr("status_name"))
                .set("预计成交日期", DateUtil.formatDateTime(record.get("deal_date"))).set("客户名称", record.getStr("customer_name"))
                .set("商机金额", record.getStr("money")).set("备注", record.getStr("remark"));
        List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"), record.getStr("batch_id"));
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * @author wyq
     * 根据商机名称查询
     */
    public Record queryByName(String name) {
        //return Db.findFirst(Db.getSql("crm.business.queryByName"), name);
        return JavaBeanUtil.mapToRecord(crmBusinessDao.queryByName(name).get(0));
    }

    /**
     * @author wyq
     * 根据商机id查询产品
     */
    public R queryProduct(BasePageRequest<CrmBusiness> basePageRequest) {
        Integer businessId = basePageRequest.getData().getBusinessId();
        Integer pageType = basePageRequest.getPageType();
        Record record = JavaBeanUtil.mapToRecord(crmProductDao.querySubtotalByBusinessId(businessId).get(0));
        if (record.getStr("money") == null) {
            record.set("money", 0);
        }
        if (0 == pageType) {
            //record.set("list", Db.find(Db.getSql("crm.business.queryProduct"), businessId));
            record.set("list", crmBusinessDao.queryProduct(businessId));
        } else {
            //record.set("list", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryProduct")).addPara(businessId)));
            record.set("list", crmBusinessDao.pageQueryProduct(basePageRequest.getPage(), basePageRequest.getLimit(), businessId));
        }
        return R.ok().put("data", record);
    }

    /**
     * @author wyq
     * 根据商机id查询合同
     */
    public R queryContract(BasePageRequest<CrmBusiness> basePageRequest) {
        Integer businessId = basePageRequest.getData().getBusinessId();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            //return R.ok().put("data", Db.find(Db.getSql("crm.business.queryContract"), businessId));
            return R.ok().put("data", crmBusinessDao.queryContract(businessId));
        } else {
            return R.ok().put("data", crmBusinessDao.pageQueryContract(basePageRequest.getPage(), basePageRequest.getLimit(), businessId));
        }
    }

    /**
     * @author wyq
     * 根据商机id查询联系人
     */
    public R queryContacts(BasePageRequest<CrmBusiness> basePageRequest) {
        Integer businessId = basePageRequest.getData().getBusinessId();
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            //return R.ok().put("data", Db.find(Db.getSql("crm.business.queryContacts"), businessId));
            return R.ok().put("data", crmBusinessDao.queryContacts(businessId));
        } else {
            return R.ok().put("data", crmBusinessDao.pageQueryContacts(basePageRequest.getPage(), basePageRequest.getLimit(), businessId));
            //return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), new SqlPara().setSql(Db.getSql("crm.business.queryContacts")).addPara(businessId)));
        }
    }

    /**
     * @author wyq
     * 商机关联联系人
     */
    public R relateContacts(Integer businessId, String contactsIds) {
        String[] contactsIdsArr = contactsIds.split(",");
        crmBusinessDao.executeUpdateSQL("delete from 72crm_crm_contacts_business where business_id = ?", businessId);
        List<LkCrmContactsBusinessEntity> crmContactsBusinessList = new ArrayList<>();
        for (String id : contactsIdsArr) {
            LkCrmContactsBusinessEntity crmContactsBusiness = new LkCrmContactsBusinessEntity();
            crmContactsBusiness.setContactsId(Integer.valueOf(id));
            crmContactsBusiness.setBusinessId(businessId);
            crmContactsBusinessList.add(crmContactsBusiness);
        }
        //Db.batchSave(crmContactsBusinessList, 100);
        crmBusinessDao.batchSaveOrUpdate(crmContactsBusinessList);
        return R.ok();
    }

    /**
     * @author wyq
     * 商机解除关联联系人
     */
    public R unrelateContacts(Integer businessId, String contactsIds) {
        String[] idsArr = contactsIds.split(",");
        //SqlPara sqlPara = Db.getSqlPara("crm.business.unrelateContacts", Kv.by("businessId", businessId).set("ids", idsArr));
        //Db.delete(sqlPara.getSql(), sqlPara.getPara());
        crmBusinessDao.unrelateContacts(businessId, Arrays.asList(idsArr));
        return R.ok();
    }

    /**
     * @author wyq
     * 根据id删除商机
     */
    public R deleteByIds(String businessIds) {
        String[] idsArr = businessIds.split(",");
        Integer number = crmBusinessDao.queryContractNumber(Arrays.asList(idsArr));
        if (number > 0) {
            return R.error("该条数据与其他数据有必要关联，请勿删除");
        }
        List<Record> idsList = new ArrayList<>();
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("business_id", Integer.valueOf(id)));
        }
        //List<Record> batchIdList = Db.find(Db.getSqlPara("crm.business.queryBatchIdByIds", Kv.by("ids", idsArr)));
        List<Record> batchIdList = JavaBeanUtil.mapToRecords(crmBusinessDao.queryBatchIdByIds(Arrays.asList(idsArr)));
        return Db.tx(() -> {
            crmBusinessDao.deleteByIds(Arrays.asList(idsArr));
            //Db.batch(Db.getSql("crm.business.deleteByIds"), "business_id", idsList, 100);
            crmBusinessDao.executeUpdateSQL("delete from 72crm_admin_fieldv where batch_id IN( ?)", Arrays.asList(idsArr));
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * 根据客户id变更负责人
     *
     * @author wyq
     */
    public R updateOwnerUserId(LkCrmCustomerEntity crmCustomer) {
        LkCrmBusinessEntity crmBusiness = new LkCrmBusinessEntity();
        crmBusiness.setNewOwnerUserId(crmCustomer.getNewOwnerUserId());
        crmBusiness.setTransferType(crmCustomer.getTransferType());
        crmBusiness.setPower(crmCustomer.getPower());
        String businessIds = crmBusinessDao.queryForObject("select GROUP_CONCAT(business_id) from 72crm_crm_business where customer_id in (" + crmCustomer.getCustomerIds() + ")");
        if (StrUtil.isEmpty(businessIds)) {
            return R.ok();
        }
        crmBusiness.setBusinessIds(businessIds);
        return transfer(crmBusiness);
    }

    /**
     * @author wyq
     * 根据商机id变更负责人
     */
    public R transfer(LkCrmBusinessEntity crmBusiness) {
        String[] businessIdsArr = crmBusiness.getBusinessIds().split(",");
        return Db.tx(() -> {
            for (String businessId : businessIdsArr) {
                String memberId = "," + crmBusiness.getNewOwnerUserId() + ",";
                crmBusinessDao.deleteMember(memberId, Integer.valueOf(businessId));
                LkCrmBusinessEntity oldBusiness = crmBusinessDao.get(Integer.valueOf(businessId));
                if (2 == crmBusiness.getTransferType()) {
                    if (1 == crmBusiness.getPower()) {
                        crmBusiness.setRoUserId(oldBusiness.getRoUserId() + oldBusiness.getOwnerUserId() + ",");
                    }
                    if (2 == crmBusiness.getPower()) {
                        crmBusiness.setRwUserId(oldBusiness.getRwUserId() + oldBusiness.getOwnerUserId() + ",");
                    }
                }
                crmBusiness.setBusinessId(Integer.valueOf(businessId));
                crmBusiness.setOwnerUserId(crmBusiness.getNewOwnerUserId());
                crmBusinessDao.update(crmBusiness);
                crmRecordService.addConversionRecord(Integer.valueOf(businessId), CrmEnum.BUSINESS_TYPE_KEY.getTypes(), crmBusiness.getNewOwnerUserId());
            }
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    public List<Record> getMembers(Integer businessId) {
        LkCrmBusinessEntity crmBusiness = crmBusinessDao.get(businessId);
        List<Record> recordList = new ArrayList<>();
        if (crmBusiness.getOwnerUserId() != null) {
            Record ownerUser = JavaBeanUtil.mapToRecord(crmCustomerDao.getMembers(crmBusiness.getOwnerUserId()).get(0));
            recordList.add(ownerUser.set("power", "负责人权限").set("groupRole", "负责人"));
        }
        String roUserId = crmBusiness.getRoUserId();
        String rwUserId = crmBusiness.getRwUserId();
        String memberIds = roUserId + rwUserId.substring(1);
        if (",".equals(memberIds)) {
            return recordList;
        }
        String[] memberIdsArr = memberIds.substring(1, memberIds.length() - 1).split(",");
        Set<String> memberIdsSet = new HashSet<>(Arrays.asList(memberIdsArr));
        for (String memberId : memberIdsSet) {
            //Record record = Db.findFirst(Db.getSql("crm.customer.getMembers"), memberId);
            Record record = JavaBeanUtil.mapToRecord(crmCustomerDao.getMembers(NumberConvertUtil.parseInt(memberId)).get(0));
            if (roUserId.contains(memberId)) {
                record.set("power", "只读").set("groupRole", "普通成员");
            }
            if (rwUserId.contains(memberId)) {
                record.set("power", "读写").set("groupRole", "普通成员");
            }
            recordList.add(record);
        }
        return recordList;
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Before(Tx.class)
    public R addMember(CrmBusiness crmBusiness) {
        String[] businessIdsArr = crmBusiness.getIds().split(",");
        String[] memberArr = crmBusiness.getMemberIds().split(",");
        StringBuffer stringBuffer = new StringBuffer();
        for (String id : businessIdsArr) {
            if (StrUtil.isNotEmpty(id)) {
                Integer ownerUserId = crmBusinessDao.get(Integer.valueOf(id)).getOwnerUserId();
                for (String memberId : memberArr) {
                    if (ownerUserId.equals(Integer.valueOf(memberId))) {
                        return R.error("负责人不能重复选为团队成员");
                    }
                    crmBusinessDao.deleteMember("," + memberId + ",", Integer.valueOf(id));
                }
                if (1 == crmBusiness.getPower()) {
                    stringBuffer.setLength(0);
                    String roUserId = stringBuffer.append(crmBusinessDao.get(Integer.valueOf(id)).getRoUserId()).append(crmBusiness.getMemberIds()).append(",").toString();
                    crmBusinessDao.executeUpdateSQL("update 72crm_crm_business set ro_user_id = ? where business_id = ?", roUserId, Integer.valueOf(id));
                }
                if (2 == crmBusiness.getPower()) {
                    stringBuffer.setLength(0);
                    String rwUserId = stringBuffer.append(crmBusinessDao.get(Integer.valueOf(id)).getRwUserId()).append(crmBusiness.getMemberIds()).append(",").toString();
                    crmBusinessDao.executeUpdateSQL("update 72crm_crm_business set rw_user_id = ? where business_id = ?", rwUserId, Integer.valueOf(id));
                }
            }

        }
        return R.ok();
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    public R deleteMembers(CrmBusiness crmBusiness) {
        String[] businessIdsArr = crmBusiness.getIds().split(",");
        String[] memberArr = crmBusiness.getMemberIds().split(",");
        return Db.tx(() -> {
            for (String id : businessIdsArr) {
                for (String memberId : memberArr) {
                    crmBusinessDao.deleteMember("," + memberId + ",", Integer.valueOf(id));
                    // Db.update(Db.getSql("crm.business.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
                }
            }
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 商机状态组展示
     */
    public List<Record> queryBusinessStatus(Integer businessId) {
        return JavaBeanUtil.mapToRecords(crmBusinessDao.queryBusinessStatus(businessId));
        //return Db.find(Db.getSql("crm.business.queryBusinessStatus"), businessId);
    }

    /**
     * @author wyq
     * 商机状态组推进
     */
    @Before(Tx.class)
    public R boostBusinessStatus(LkCrmBusinessEntity crmBusiness) {
        if (crmBusiness.getStatusId() != null) {
            LkCrmBusinessChangeEntity change = new LkCrmBusinessChangeEntity();
            change.setBusinessId(crmBusiness.getBusinessId());
            change.setStatusId(crmBusiness.getStatusId());
            change.setCreateTime(DateUtil.date().toTimestamp());
            change.setCreateUserId(BaseUtil.getUserId().intValue());
            crmBusinessDao.saveOrUpdate(change);
        }
        crmBusinessDao.update(crmBusiness);
        return R.ok();
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer businessId) {
        Record business = JavaBeanUtil.mapToRecord(crmBusinessDao.sqlQuery("select * from businessview where business_id = ?", businessId).get(0));
        //Record business = Db.findFirst("select * from businessview where business_id = ?",businessId);
        List<Record> customerList = new ArrayList<>();
        Record customer = new Record();
        customerList.add(customer.set("customer_id", business.getInt("customer_id")).set("customer_name", business.getStr("customer_name")));
        business.set("customer_id", customerList);
        List<Record> fieldList = adminFieldService.queryUpdateField(5, business);
        fieldList.add(new Record().set("field_name", "type_id").set("name", "商机状态组").set("value", business.getInt("type_id")).set("form_type", "business_type").set("setting", new String[0]).set("is_null", 1).set("field_type", 1));
        fieldList.add(new Record().set("field_name", "status_id").set("name", "商机阶段").set("value", business.getInt("status_id")).set("form_type", "business_status").set("setting", new String[0]).set("is_null", 1).set("field_type", 1));
        String sql = " select b.product_id,c.name as name,d.name as category_name,b.unit,b.price,b.sales_price,b.num,b.discount,b.subtotal\n" +
                "    from lkcrm_crm_business as a inner join lkcrm_crm_business_product as b on a.business_id = b.business_id\n" +
                "    inner join 72crm_crm_product as c on b.product_id = c.product_id inner join lkcrm_crm_product_category as d\n" +
                "    on c.category_id = d.category_id\n" +
                "    where a.business_id = ?";
        List<Record> productList = JavaBeanUtil.mapToRecords(crmBusinessDao.sqlQuery(sql, businessId));
        //List<Record> productList = Db.find(Db.getSql("crm.business.queryBusinessProduct"), businessId);
        Kv kv = Kv.by("discount_rate", business.getBigDecimal("discount_rate")).set("product", productList).set("total_price", business.getStr("total_price"));
        fieldList.add(new Record().set("field_name", "product").set("name", "产品").set("value", kv).set("form_type", "product").set("setting", new String[]{}).set("is_null", 0).set("field_type", 1));
        return fieldList;
    }

    /**
     * @author wyq
     * 查询商机状态组及商机状态
     */
    public List<Record> queryBusinessStatusOptions(String type) {
        List<Record> businessTypeList = JavaBeanUtil.mapToRecords(crmBusinessDao.sqlQuery("select * from 72crm_crm_business_type where status = 1"));
        for (Record record : businessTypeList) {
            Integer typeId = record.getInt("type_id");
            List<Record> businessStatusList = JavaBeanUtil.mapToRecords(crmBusinessDao.sqlQuery("select * from 72crm_crm_business_status where type_id = ?", typeId));
            if ("condition".equals(type)) {
                Record win = new Record();
                win.set("name", "赢单").set("typeId", typeId).set("statusId", "win");
                businessStatusList.add(win);
                Record lose = new Record();
                lose.set("name", "输单").set("typeId", typeId).set("statusId", "lose");
                businessStatusList.add(lose);
                Record invalid = new Record();
                invalid.set("name", "无效").set("typeId", typeId).set("statusId", "invalid");
                businessStatusList.add(invalid);
            }
            record.set("statusList", businessStatusList);
        }
        return businessTypeList;
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(LkCrmAdminRecordEntity adminRecord) {
        adminRecord.setTypes("crm_business");
        adminRecord.setCreateTime(DateUtil.date().toTimestamp());
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        if (1 == adminRecord.getIsEvent()) {
            LkCrmOaEventEntity oaEvent = new LkCrmOaEventEntity();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1).toTimestamp());
            oaEvent.setCreateTime(DateUtil.date().toTimestamp());
            oaEvent.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmOaEventDao.save(oaEvent);
            LoginUser user = BaseUtil.getUser();
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(), 1, oaActionRecordService.getJoinIds(user.getUserId().intValue(), oaEvent.getOwnerUserIds()), oaActionRecordService.getJoinIds(user.getDeptId(), ""));
            LkCrmOaEventRelationEntity oaEventRelation = new LkCrmOaEventRelationEntity();
            oaEventRelation.setEventId(oaEvent.getEventId());
            oaEventRelation.setBusinessIds("," + adminRecord.getTypesId().toString() + ",");
            oaEventRelation.setCreateTime(DateUtil.date().toTimestamp());
            crmOaEventDao.saveOrUpdate(oaEventRelation);
        }
        return (int) crmAdminRecordDao.saveReturnPk(adminRecord) > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public List<Record> getRecord(BasePageRequest<CrmBusiness> basePageRequest) {
        CrmBusiness crmBusiness = basePageRequest.getData();
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmBusinessDao.getRecord(crmBusiness.getBusinessId()));
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
            String businessIds = record.getStr("business_ids");
            List<LkCrmBusinessEntity> businessList = new ArrayList<>();
            if (businessIds != null) {
                String[] businessIdsArr = businessIds.split(",");
                for (String businessId : businessIdsArr) {
                    businessList.add(crmBusinessDao.get(Integer.valueOf(businessId)));
                }
            }
            String contactsIds = record.getStr("contacts_ids");
            List<LkCrmContactsEntity> contactsList = new ArrayList<>();
            if (contactsIds != null) {
                String[] contactsIdsArr = contactsIds.split(",");
                for (String contactsId : contactsIdsArr) {
                    contactsList.add(crmContactsDao.get(Integer.valueOf(contactsId)));
                }
            }
            record.set("business_list", businessList).set("contacts_list", contactsList);
        });
        return recordList;
    }
}
