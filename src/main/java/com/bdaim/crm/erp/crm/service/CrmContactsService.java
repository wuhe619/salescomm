package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
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
import com.bdaim.crm.erp.crm.common.CrmParamValid;
import com.bdaim.crm.erp.crm.entity.CrmContacts;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.service.OaActionRecordService;
import com.bdaim.crm.utils.*;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class CrmContactsService {

    public static final Logger LOG = LoggerFactory.getLogger(CrmContactsService.class);
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
    private CrmParamValid crmParamValid;

    @Resource
    private AuthUtil authUtil;
    @Resource
    private LkCrmAdminUserDao crmAdminUserDao;
    @Resource
    private LkCrmContactsDao crmContactsDao;
    @Resource
    private LkCrmOaEventDao crmOaEventDao;
    @Resource
    private LkCrmAdminRecordDao crmAdminRecordDao;
    @Resource
    private LkCrmBusinessDao crmBusinessDao;

    @Resource
    private LkCrmAdminFieldDao crmAdminFieldDao;
    @Resource
    private LkCrmTaskDao crmTaskDao;
    @Autowired
    private LkCrmLeadsDao crmLeadsDao;

    /**
     * @return
     * @author wyq
     * 分页条件查询联系人
     */
    public CrmPage queryList(BasePageRequest<CrmContacts> basePageRequest) {
        String contactsName = basePageRequest.getData().getName();
        String telephone = basePageRequest.getData().getTelephone();
        String mobile = basePageRequest.getData().getMobile();
        String customerName = basePageRequest.getData().getCustomerName();
        if (!crmParamValid.isValid(customerName)) {
            return new CrmPage();
        }
        if (StrUtil.isEmpty(contactsName) && StrUtil.isEmpty(telephone) && StrUtil.isEmpty(mobile) && StrUtil.isEmpty(customerName)) {
            return new CrmPage();
        }
        Page page = crmContactsDao.pageContactsPageList(basePageRequest.getPage(), basePageRequest.getLimit(), contactsName, customerName, telephone, mobile);
        return BaseUtil.crmPage(page);
        /*return Db.paginate(basePageRequest.getPage(),basePageRequest.getLimit(), Db.getSqlPara("crm.contact.getContactsPageList",
                Kv.by("contactsName",contactsName).set("customerName",customerName).set("telephone",telephone).set("mobile",mobile)));*/
    }

    /**
     * @author wyq
     * 根据id查询联系人
     */
    public Map<String, Object> queryById(Integer contactsId) {
        return crmContactsDao.queryById(contactsId).get(0);
        //return Db.findFirst(Db.getSql("crm.contact.queryById"), contactsId);
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer contactsId) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        Record record = JavaBeanUtil.mapToRecord(crmContactsDao.sqlQuery("select * from " + contactsview + " where contacts_id = ?", contactsId).get(0));
        if (null == record) {
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("姓名", record.getStr("name")).set("客户名称", record.getStr("customer_name"))
                .set("下次联系时间", DateUtil.formatDateTime(record.get("next_time"))).set("职务", record.getStr("post"))
                .set("手机", record.getStr("mobile")).set("电话", record.getStr("telephone")).set("邮箱", record.getStr("email"))
                .set("地址", record.getStr("address")).set("备注", record.getStr("remark"));
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.queryCustomField(record.getStr("batch_id")));
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * @author wyq
     * 根据联系人名称查询
     */
    public Map<String, Object> queryByName(String name) {
        return crmContactsDao.queryByName(name).get(0);
        //return Db.findFirst(Db.getSql("crm.contact.queryByName"), name);
    }

    /**
     * @author wyq
     * 根据联系人id查询商机
     */
    public R queryBusiness(BasePageRequest<CrmContacts> basePageRequest) {
        Integer contactsId = basePageRequest.getData().getContactsId();
        Integer pageType = basePageRequest.getPageType();
        if (pageType != null && 0 == pageType) {
            return R.ok().put("data", crmContactsDao.queryBusiness(contactsId));
        } else {
            return R.ok().put("data", BaseUtil.crmPage(crmContactsDao.pageQueryBusiness(basePageRequest.getPage(), basePageRequest.getLimit(), contactsId)));
        }
    }

    /**
     * @author wyq
     * 联系人关联商机
     */
    @Before(Tx.class)
    public R relateBusiness(Integer contactsId, String businessIds) {
        String[] businessIdsArr = businessIds.split(",");
        crmContactsDao.executeUpdateSQL("delete from lkcrm_crm_contacts_business where contacts_id = ?", contactsId);
        List<LkCrmContactsBusinessEntity> crmContactsBusinessList = new ArrayList<>();
        for (String id : businessIdsArr) {
            LkCrmContactsBusinessEntity crmContactsBusiness = new LkCrmContactsBusinessEntity();
            crmContactsBusiness.setContactsId(contactsId);
            crmContactsBusiness.setBusinessId(Integer.valueOf(id));
            crmContactsBusinessList.add(crmContactsBusiness);
        }
        //Db.batchSave(crmContactsBusinessList, 100);
        crmContactsDao.batchSaveOrUpdate(crmContactsBusinessList);
        return R.ok();
    }

    /**
     * @author wyq
     * 联系人解除关联商机
     */
    public R unrelateBusiness(Integer contactsId, String businessIds) {
        String[] idsArr = businessIds.split(",");
        /*SqlPara sqlPara = Db.getSqlPara("crm.contact.unrelateBusiness", Kv.by("contactsId", contactsId).set("ids", idsArr));
        Db.delete(sqlPara.getSql(), sqlPara.getPara());*/
        crmContactsDao.unrelateBusiness(contactsId, Arrays.asList(idsArr));
        return R.ok();
    }

    /**
     * @author wyq
     * 新建或更新联系人
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject jsonObject) {
        CrmContacts entity = jsonObject.getObject("entity", CrmContacts.class);
        LkCrmContactsEntity crmContacts = new LkCrmContactsEntity();
        BeanUtils.copyProperties(entity, crmContacts);
        String batchId = StrUtil.isNotEmpty(crmContacts.getBatchId()) ? crmContacts.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        crmContacts.setCustId(BaseUtil.getUser().getCustId());
        if (entity.getContactsId() != null) {
            crmContacts.setUpdateTime(DateUtil.date().toTimestamp());
            crmRecordService.updateRecord(crmContactsDao.get(crmContacts.getContactsId()), crmContacts, CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            LkCrmContactsEntity dnEntity = crmContactsDao.get(crmContacts.getContactsId());
            BeanUtils.copyProperties(crmContacts, dnEntity, JavaBeanUtil.getNullPropertyNames(crmContacts));
            crmContactsDao.saveOrUpdate(dnEntity);
            return R.ok();
        } else {
            crmContacts.setCreateTime(DateUtil.date().toTimestamp());
            crmContacts.setUpdateTime(DateUtil.date().toTimestamp());
            crmContacts.setCreateUserId(BaseUtil.getUserId());
            if (crmContacts.getOwnerUserId() == null) {
                crmContacts.setOwnerUserId(BaseUtil.getUserId());
            }
            crmContacts.setBatchId(batchId);
            boolean save = (int) crmContactsDao.saveReturnPk(crmContacts) > 0;
            crmRecordService.addRecord(crmContacts.getContactsId(), CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            return save ? R.ok() : R.error();
        }
    }

    /**
     * 批量添加联系人
     *
     * @param objects
     * @return
     */
    public R batchAddContacts(JSONArray objects) {
        int count = 0;
        for (int i = 0; i < objects.size(); i++) {
            Integer leadsId = objects.getJSONObject(i).getInteger("leadsId");
            LkCrmContactsEntity crmContacts = objects.getObject(i, LkCrmContactsEntity.class);
            crmContacts.setCustomerId(objects.getJSONObject(i).getInteger("customer_id"));
            String batchId = StrUtil.isNotEmpty(crmContacts.getBatchId()) ? crmContacts.getBatchId() : IdUtil.simpleUUID();
            //crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
            //adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
            crmContacts.setCustId(BaseUtil.getUser().getCustId());
            crmContacts.setCreateTime(DateUtil.date().toTimestamp());
            crmContacts.setUpdateTime(DateUtil.date().toTimestamp());
            crmContacts.setCreateUserId(BaseUtil.getUserId());
            if (crmContacts.getOwnerUserId() == null) {
                crmContacts.setOwnerUserId(BaseUtil.getUserId());
            }
            crmContacts.setBatchId(batchId);
            if ((int) crmContactsDao.saveReturnPk(crmContacts) > 0) {
                crmRecordService.addRecord(crmContacts.getContactsId(), CrmEnum.CONTACTS_TYPE_KEY.getTypes());
                count++;

                if (leadsId != null) {
                    LkCrmLeadsEntity lkCrmLeads = crmLeadsDao.get(leadsId);
                    crmContacts.setMobile(lkCrmLeads.getMobile());
                    crmContacts.setTelephone(lkCrmLeads.getTelephone());
                    crmContacts.setName(lkCrmLeads.getLeadsName());
                    crmContactsDao.update(crmContacts);

                    Integer customer_id = objects.getJSONObject(i).getInteger("customer_id");
                    crmContactsDao.executeUpdateSQL("update lkcrm_crm_leads set is_transform = 1,update_time = ?,customer_id = ? where leads_id = ?",
                            DateUtil.date().toTimestamp(), customer_id, leadsId);
                    List<LkCrmAdminRecordEntity> adminRecordList = crmAdminUserDao.find(" from LkCrmAdminRecordEntity where types = 'crm_leads' and typesId = ?", leadsId.toString());
                    List<LkCrmAdminFileEntity> adminFileList = new ArrayList<>();
                    if (adminRecordList.size() != 0) {
                        adminRecordList.forEach(adminRecord -> {
                            List<LkCrmAdminFileEntity> leadsRecordFiles = crmAdminUserDao.find(" from LkCrmAdminFileEntity where batchId = ?", adminRecord.getBatchId());
                            leadsRecordFiles.forEach(adminFile -> {
                                adminFile.setBatchId(crmContacts.getBatchId());
                                adminFile.setFileId(null);
                            });
                            adminFileList.addAll(leadsRecordFiles);
                            adminRecord.setBatchId(crmContacts.getBatchId());
                            adminRecord.setRecordId(null);
                            adminRecord.setCustId(BaseUtil.getCustId());
                            adminRecord.setTypes("crm_contacts");
                            adminRecord.setTypesId(crmContacts.getContactsId().toString());
                            adminRecord.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                        });
                        //Db.batchSave(adminRecordList, 100);
                        crmContactsDao.getSession().clear();
                        crmContactsDao.batchSaveOrUpdate(adminRecordList);
                        crmContactsDao.getSession().clear();
                        crmContactsDao.batchSaveOrUpdate(adminFileList);
                    }
                }
            }
        }
        return count == objects.size() ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 根据id删除联系人
     */
    public R deleteByIds(String contactsIds) {
        String[] idsArr = contactsIds.split(",");
        List<Record> idsList = new ArrayList<>();
        for (String id : idsArr) {
            Record record = new Record();
            idsList.add(record.set("contacts_id", Integer.valueOf(id)));
        }
        List<String> batchIds = new ArrayList<>();
        //List<Record> batchIdList = Db.find(Db.getSqlPara("crm.contact.queryBatchIdByIds", Kv.by("ids", idsArr)));
        List<Record> batchIdList = JavaBeanUtil.mapToRecords(crmContactsDao.queryBatchIdByIds(Arrays.asList(idsArr)));
        for (int i = 0; i < batchIdList.size(); i++) {
            batchIds.add(batchIdList.get(i).getStr("batch_id"));
        }
        //return Db.tx(() -> {
        //Db.batch(Db.getSql("crm.contact.deleteByIds"), "contacts_id", idsList, 100);
        crmContactsDao.deleteByIds(Arrays.asList(idsArr));
        crmContactsDao.executeUpdateSQL("delete from lkcrm_admin_fieldv where batch_id IN( ?)", batchIds);
        return R.ok();
        //}) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 联系人转移
     */
    public R transfer(CrmContacts crmContacts) {
        String[] contactsIdsArr = crmContacts.getContactsIds().split(",");
        int update = crmContactsDao.transfer(crmContacts.getNewOwnerUserId().toString(), Arrays.asList(contactsIdsArr));
        //int update = Db.update(Db.getSqlPara("crm.contact.transfer", Kv.by("ownerUserId", crmContacts.getNewOwnerUserId()).set("ids", contactsIdsArr)));
        for (String contactsId : contactsIdsArr) {
            crmRecordService.addConversionRecord(Integer.valueOf(contactsId), CrmEnum.CONTACTS_TYPE_KEY.getTypes(), crmContacts.getNewOwnerUserId());
        }
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * 根据客户id变更负责人
     *
     * @param customerId  客户ID
     * @param ownerUserId 负责人ID
     */
    public boolean updateOwnerUserId(Integer customerId, Long ownerUserId) {
        crmAdminUserDao.executeUpdateSQL("update lkcrm_crm_contacts set owner_user_id = " + ownerUserId + " where customer_id = " + customerId);
        crmRecordService.addConversionRecord(customerId, CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), ownerUserId);
        return true;
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer contactsId) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        Record contacts = JavaBeanUtil.mapToRecord(crmAdminUserDao.sqlQuery("select * from " + contactsview + " where contacts_id = ?", contactsId).get(0));
        //Record contacts = Db.findFirst("select * from contactsview where contacts_id = ?",contactsId);
        List<Record> customerList = new ArrayList<>();
        Record customer = new Record();
        customerList.add(customer.set("customer_id", contacts.getInt("customer_id")).set("customer_name", contacts.getStr("customer_name")));
        contacts.set("customer_id", customerList);
        return adminFieldService.queryUpdateField(3, contacts);
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(LkCrmAdminRecordEntity adminRecord) {
        adminRecord.setTypes("crm_contacts");
        adminRecord.setCreateTime(DateUtil.date().toTimestamp());
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        adminRecord.setCustId(BaseUtil.getUser().getCustId());
        if (1 == adminRecord.getIsEvent()) {
            LkCrmOaEventEntity oaEvent = new LkCrmOaEventEntity();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1).toTimestamp());
            oaEvent.setCreateTime(DateUtil.date().toTimestamp());
            oaEvent.setCreateUserId(BaseUtil.getUser().getUserId());
            crmOaEventDao.save(oaEvent);

            LoginUser user = BaseUtil.getUser();
            oaActionRecordService.addRecord(oaEvent.getEventId(), OaEnum.EVENT_TYPE_KEY.getTypes(), 1, oaActionRecordService.getJoinIds(user.getUserId().intValue(), oaEvent.getOwnerUserIds()), oaActionRecordService.getJoinIds(user.getDeptId(), ""));
            LkCrmOaEventRelationEntity oaEventRelation = new LkCrmOaEventRelationEntity();
            oaEventRelation.setEventId(oaEvent.getEventId());
            oaEventRelation.setContactsIds("," + adminRecord.getTypesId().toString() + ",");
            oaEventRelation.setCreateTime(DateUtil.date().toTimestamp());
            crmOaEventDao.saveOrUpdate(oaEventRelation);
        }
        // 添加任务
        if (adminRecord.getIsTask() != null && 1 == adminRecord.getIsTask()) {
            LkCrmTaskEntity crmTaskEntity = new LkCrmTaskEntity();
            crmTaskEntity.setCustId(BaseUtil.getUser().getCustId());
            crmTaskEntity.setBatchId(IdUtil.simpleUUID());
            crmTaskEntity.setName(adminRecord.getTaskName());
            crmTaskEntity.setDescription(adminRecord.getContent());
            crmTaskEntity.setCreateUserId(adminRecord.getCreateUserId());
            crmTaskEntity.setMainUserId(adminRecord.getCreateUserId());
            crmTaskEntity.setStartTime(adminRecord.getNextTime());
            if (adminRecord.getNextTime() != null) {
                crmTaskEntity.setStopTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1).toTimestamp());
            }
            //完成状态 1正在进行2延期3归档 5结束
            crmTaskEntity.setStatus(1);
            crmTaskEntity.setCreateTime(DateUtil.date().toTimestamp());
            int taskId = (int) crmTaskDao.saveReturnPk(crmTaskEntity);
            adminRecord.setTaskId(taskId);
        }
        int code = (int) crmAdminRecordDao.saveReturnPk(adminRecord);
        return R.isSuccess(code > 0);
    }

    /**
     * @author wyq
     * 查看跟进记录
     */

    public List<Record> getRecord(BasePageRequest<CrmContacts> basePageRequest) {
        CrmContacts crmContacts = basePageRequest.getData();
        //List<Record> recordList = Db.find(Db.getSql("crm.contact.getRecord"), crmContacts.getContactsId(), crmContacts.getContactsId());
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmContactsDao.getRecord(crmContacts.getContactsId(), basePageRequest.getPage(), basePageRequest.getLimit()));
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
            String businessIds = record.getStr("business_ids");
            List<LkCrmBusinessEntity> businessList = new ArrayList<>();
            if (businessIds != null) {
                String[] businessIdsArr = businessIds.split(",");
                for (String businessId : businessIdsArr) {
                    if (StringUtil.isNotEmpty(businessId)) {
                        businessList.add(crmBusinessDao.get(NumberConvertUtil.parseInt(businessId)));
                    }
                }
            }
            String contactsIds = record.getStr("contacts_ids");
            List<LkCrmContactsEntity> contactsList = new ArrayList<>();
            if (contactsIds != null) {
                String[] contactsIdsArr = contactsIds.split(",");
                for (String contactsId : contactsIdsArr) {
                    if (StringUtil.isNotEmpty(contactsId)) {
                        contactsList.add(crmContactsDao.get(NumberConvertUtil.parseInt(contactsId)));
                    }
                }
            }
            record.set("business_list", businessList).set("contacts_list", contactsList);
        });
        return recordList;
    }

    /**
     * 查看代办事项记录
     *
     * @param basePageRequest
     * @param taskStatus
     * @param contactsId
     * @return
     */
    public List<Record> listAgency(BasePageRequest<CrmContacts> basePageRequest, Integer taskStatus, Integer contactsId) {
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmContactsDao.getRecord(contactsId, taskStatus, basePageRequest.getPage(), basePageRequest.getLimit()));
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
        });
        return recordList;
    }

    /**
     * @author wyq
     * 联系人导出
     */
    public List<Record> exportContacts(String contactsIds) {
        String[] contactsIdsArr = contactsIds.split(",");
        return JavaBeanUtil.mapToRecords(crmContactsDao.excelExport(Arrays.asList(contactsIdsArr)));
        //return Db.find(Db.getSqlPara("crm.contact.excelExport", Kv.by("ids", contactsIdsArr)));
    }

    /**
     * @author wyq
     * 获取联系人导入查重字段
     */
    public R getCheckingField() {
        return R.ok().put("data", "联系人姓名,电话,手机");
//        return R.ok().put("data",Db.getSql("crm.contacts.getCheckingField"));
    }

    /**
     * @author wyq
     * 导入联系人
     */
    public R uploadExcel(UploadFile file, Integer repeatHandling, Integer ownerUserId) {
        ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()));
        //AdminFieldService adminFieldService = new AdminFieldService();
        Kv kv = new Kv();
        Integer errNum = 0;
        try {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(2);
            List<Record> recordList = adminFieldService.customFieldList("3");
            recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            List<Record> fieldList = adminFieldService.queryAddField(3);
            fieldList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1) {
                    record.set("name", record.getStr("name") + "(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != list.size() || !nameList.containsAll(list)) {
                return R.error("请使用最新导入模板");
            }
            Kv nameMap = new Kv();
            fieldList.forEach(record -> nameMap.set(record.getStr("name"), record.getStr("field_name")));
            for (int i = 0; i < list.size(); i++) {
                kv.set(nameMap.get(list.get(i)), i);
            }
            if (read.size() > 2) {
                JSONObject object = new JSONObject();
                for (int i = 2; i < read.size(); i++) {
                    errNum = i;
                    List<Object> contactsList = read.get(i);
                    if (contactsList.size() < list.size()) {
                        for (int j = contactsList.size() - 1; j < list.size(); j++) {
                            contactsList.add(null);
                        }
                    }
                    String contactsName = contactsList.get(kv.getInt("name")).toString();
                    Object telephoneObeject = contactsList.get(kv.getInt("telephone"));
                    String telephone = null;
                    if (telephoneObeject != null) {
                        telephone = telephoneObeject.toString();
                    }
                    Object mobileObject = contactsList.get(kv.getInt("mobile"));
                    String mobile = null;
                    if (mobileObject != null) {
                        mobile = mobileObject.toString();
                    }
                    Record repeatField = JavaBeanUtil.mapToRecord(crmContactsDao.queryRepeatFieldNumber(contactsName, telephone, mobile).get(0));
                    //Record repeatField = Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatFieldNumber", Kv.by("contactsName", contactsName).set("telephone", telephone).set("mobile", mobile)));
                    Integer number = repeatField.getInt("number");
                    Integer customerId = crmContactsDao.queryForInt("select customer_id from lkcrm_crm_customer where customer_name = ?", contactsList.get(kv.getInt("customer_id")));
                    if (customerId == null) {
                        return R.error("第" + errNum + 1 + "行填写的客户不存在");
                    }
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("name", contactsName)
                                .fluentPut("customer_id", customerId)
                                .fluentPut("telephone", telephone)
                                .fluentPut("mobile", mobile)
                                .fluentPut("email", contactsList.get(kv.getInt("email")))
                                .fluentPut("post", contactsList.get(kv.getInt("post")))
                                .fluentPut("address", contactsList.get(kv.getInt("address")))
                                .fluentPut("next_time", contactsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", contactsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number == 1 && repeatHandling == 1) {
                        if (repeatHandling == 1) {
                            Record contacts = JavaBeanUtil.mapToRecord(crmContactsDao.queryRepeatField(contactsName, telephone, mobile).get(0));
                            //Record contacts = Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatField", Kv.by("contactsName", contactsName).set("telephone", telephone).set("mobile", mobile)));
                            object.fluentPut("entity", new JSONObject().fluentPut("contacts_id", contacts.getInt("contacts_id"))
                                    .fluentPut("name", contactsName)
                                    .fluentPut("customer_id", customerId)
                                    .fluentPut("telephone", telephone)
                                    .fluentPut("mobile", mobile)
                                    .fluentPut("email", contactsList.get(kv.getInt("email")))
                                    .fluentPut("post", contactsList.get(kv.getInt("post")))
                                    .fluentPut("address", contactsList.get(kv.getInt("address")))
                                    .fluentPut("next_time", contactsList.get(kv.getInt("next_time")))
                                    .fluentPut("remark", contactsList.get(kv.getInt("remark")))
                                    .fluentPut("owner_user_id", ownerUserId)
                                    .fluentPut("batch_id", contacts.getStr("batch_id")));
                        }
                    } else if (repeatHandling == 2) {
                        continue;
                    } else if (number > 1) {
                        return R.error("数据多条重复");
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name")) != null ? kv.getInt(record.getStr("name")) : kv.getInt(record.getStr("name") + "(*)");
                        record.set("value", contactsList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    addOrUpdate(object);
                }
            }
        } catch (Exception e) {
            Log.getLog(getClass()).error("", e);
            if (errNum != 0) {
                return R.error("第" + (errNum + 1) + "行错误!");
            }
            return R.error();
        } finally {
            reader.close();
        }
        return R.ok();
    }

    public R uploadExcel(MultipartFile file, Integer repeatHandling, Long ownerUserId) {
        //AdminFieldService adminFieldService = new AdminFieldService();
        Kv kv = new Kv();
        Integer errNum = 0;
        try (ExcelReader reader = ExcelUtil.getReader(file.getInputStream())) {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(1);
            List<Record> recordList = adminFieldService.customFieldList("3");
            recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            List<Record> fieldList = adminFieldService.queryAddField(3);
            fieldList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            fieldList.forEach(record -> {
                if (record.getInt("is_null") == 1) {
                    record.set("name", record.getStr("name") + "(*)");
                }
            });
            List<String> nameList = fieldList.stream().map(record -> record.getStr("name")).collect(Collectors.toList());
            if (nameList.size() != list.size() || !nameList.containsAll(list)) {
                return R.error("请使用最新导入模板");
            }
            Kv nameMap = new Kv();
            fieldList.forEach(record -> nameMap.set(record.getStr("name"), record.getStr("field_name")));
            for (int i = 0; i < list.size(); i++) {
                kv.set(nameMap.get(list.get(i)), i);
            }
            if (read.size() > 2) {
                JSONObject object = new JSONObject();
                for (int i = 2; i < read.size(); i++) {
                    errNum = i;
                    List<Object> contactsList = read.get(i);
                    if (contactsList.size() < list.size()) {
                        for (int j = contactsList.size() - 1; j < list.size(); j++) {
                            contactsList.add(null);
                        }
                    }
                    String contactsName = contactsList.get(kv.getInt("name")).toString();
                    Object telephoneObeject = contactsList.get(kv.getInt("telephone"));
                    String telephone = null;
                    if (telephoneObeject != null) {
                        telephone = telephoneObeject.toString();
                    }
                    Object mobileObject = contactsList.get(kv.getInt("mobile"));
                    String mobile = null;
                    if (mobileObject != null) {
                        mobile = mobileObject.toString();
                    }
                    Record repeatField = JavaBeanUtil.mapToRecord(crmContactsDao.queryRepeatFieldNumber(contactsName, telephone, mobile).get(0));
                    //Record repeatField = Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatFieldNumber", Kv.by("contactsName", contactsName).set("telephone", telephone).set("mobile", mobile)));
                    Integer number = repeatField.getInt("number");
                    Integer customerId = crmContactsDao.queryForInt("select customer_id from lkcrm_crm_customer where customer_name = ?", contactsList.get(kv.getInt("customer_id")));
                    if (customerId == null) {
                        return R.error("第" + errNum + 1 + "行填写的客户不存在");
                    }
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("name", contactsName)
                                .fluentPut("customer_id", customerId)
                                .fluentPut("telephone", telephone)
                                .fluentPut("mobile", mobile)
                                .fluentPut("email", contactsList.get(kv.getInt("email")))
                                .fluentPut("post", contactsList.get(kv.getInt("post")))
                                .fluentPut("address", contactsList.get(kv.getInt("address")))
                                .fluentPut("next_time", contactsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", contactsList.get(kv.getInt("备注")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number == 1 && repeatHandling == 1) {
                        if (repeatHandling == 1) {
                            Record contacts = JavaBeanUtil.mapToRecord(crmContactsDao.queryRepeatField(contactsName, telephone, mobile).get(0));
                            //Record contacts = Db.findFirst(Db.getSqlPara("crm.contact.queryRepeatField", Kv.by("contactsName", contactsName).set("telephone", telephone).set("mobile", mobile)));
                            object.fluentPut("entity", new JSONObject().fluentPut("contacts_id", contacts.getInt("contacts_id"))
                                    .fluentPut("name", contactsName)
                                    .fluentPut("customer_id", customerId)
                                    .fluentPut("telephone", telephone)
                                    .fluentPut("mobile", mobile)
                                    .fluentPut("email", contactsList.get(kv.getInt("email")))
                                    .fluentPut("post", contactsList.get(kv.getInt("post")))
                                    .fluentPut("address", contactsList.get(kv.getInt("address")))
                                    .fluentPut("next_time", contactsList.get(kv.getInt("next_time")))
                                    .fluentPut("remark", contactsList.get(kv.getInt("备注")))
                                    .fluentPut("owner_user_id", ownerUserId)
                                    .fluentPut("batch_id", contacts.getStr("batch_id")));
                        }
                    } else if (repeatHandling == 2) {
                        continue;
                    } else if (number > 1) {
                        return R.error("数据多条重复");
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name")) != null ? kv.getInt(record.getStr("name")) : kv.getInt(record.getStr("name") + "(*)");
                        record.set("value", contactsList.get(columnsNum));
                        jsonArray.add(JSONObject.parseObject(record.toJson()));
                    }
                    object.fluentPut("field", jsonArray);
                    addOrUpdate(object);
                }
            }
        } catch (Exception e) {
            LOG.error("", e);
            if (errNum != 0) {
                return R.error("第" + (errNum + 1) + "行错误!");
            }
            return R.error();
        }
        return R.ok();
    }
}
