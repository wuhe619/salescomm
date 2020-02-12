package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.PhoneService;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.entity.*;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.common.CrmParamValid;
import com.bdaim.crm.erp.crm.entity.CrmLeads;
import com.bdaim.crm.utils.*;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customersea.dao.CustomerSeaDao;
import com.bdaim.customersea.dto.CustomSeaTouchInfoDTO;
import com.bdaim.customersea.dto.CustomerSeaESDTO;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.customersea.entity.CustomerSeaProperty;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.util.*;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class CrmLeadsService {

    public static final Logger LOG = LoggerFactory.getLogger(CrmLeadsService.class);
    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private FieldUtil fieldUtil;

    @Resource
    private CrmRecordService crmRecordService;

    @Resource
    private AdminFileService adminFileService;

    @Resource
    private CrmParamValid crmParamValid;

    @Resource
    private AuthUtil authUtil;

    @Resource
    private LkCrmAdminUserDao crmAdminUserDao;

    @Resource
    private LkCrmLeadsDao crmLeadsDao;

    @Resource
    private LkCrmCustomerDao crmCustomerDao;

    @Resource
    private LkCrmAdminRecordDao crmAdminRecordDao;

    @Resource
    private LkCrmOaEventDao crmOaEventDao;

    @Resource
    private CustomerSeaDao customerSeaDao;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomGroupDao customGroupDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private PhoneService phoneService;

    /**
     * 默认需要转为super_data的字段名称
     */
    public static Map<String, String> defaultLabels = new HashMap() {{
        put("qq", "SYS002");
        put("email", "SYS003");
        put("profession", "SYS004");
        put("weChat", "SYS001");
        put("company", "SYS005");
        put("followStatus", "SYS007");
        put("invalidReason", "SYS006");

        // 注册地址
        put("regLocation", "SYS009");
        // 注册资金
        put("regCapital", "SYS010");
        // 注册时间
        put("regTime", "SYS011");
        // 经营状态
        put("regStatus", "SYS012");
        // 企业联系人数量
        put("entPersonNum", "SYS013");
        // 企业ID
        put("entId", "SYS014");
        put("next_time", "next_time");
        put("remark", "remark");
    }};

    private Map<String, String> excelDefaultLabels = new HashMap() {{
        put("qq", "SYS002");
        put("QQ", "SYS002");
        put("QQ号", "SYS002");
        put("email", "SYS003");
        put("EMAIL", "SYS003");
        put("weChat", "SYS001");
        put("微信", "SYS001");
        put("职业", "SYS004");
        put("公司", "SYS005");
        put("所在公司", "SYS005");
        put("跟进状态", "SYS007");
        put("无效原因", "SYS006");
        put("姓名", "super_name");
        put("年龄", "super_age");
        put("性别", "super_sex");
        put("手机", "super_telphone");
        put("手机号", "super_telphone");
        put("电话", "super_phone");
        put("电话号码", "super_phone");
        put("邮箱", "SYS003");
        put("省市", "super_address_province_city");
        put("地址", "super_address_street");
    }};

    /**
     * @return
     * @author wyq
     * 分页条件查询线索
     */
    public R pageCluePublicSea(BasePageRequest<CrmLeads> basePageRequest, long seaId, String custId) {
        //String leadsName = basePageRequest.getData().getLeadsName();
        CustomerSea customerSea = customerSeaDao.get(seaId);
        if (ObjectUtil.notEqual(custId, customerSea.getCustId())) {
            return R.error("线索公海不属于该客户");
        }
        String search = basePageRequest.getJsonObject().getString("search");
        if (!ParamsUtil.isValid(search)) {
            return R.error("参数包含非法字段");
        }
        com.bdaim.common.dto.Page page = crmLeadsDao.pageCluePublicSea(basePageRequest.getPage(), basePageRequest.getLimit(), seaId, search);
        Page finalPage = new Page();
        finalPage.setList(page.getData());
        finalPage.setTotalRow(page.getTotal());
        return R.ok().put("data", finalPage);
    }

    /**
     * 处理qq 微信 根据状态等自建属性值存入super_data
     *
     * @param dto
     */
    private void handleDefaultLabelValue(CustomSeaTouchInfoDTO dto) {
        Map<String, Object> superData = new HashMap<>(16);
        if (dto.getSuperData() != null && dto.getSuperData().size() > 0) {
            for (Map.Entry<String, Object> m : dto.getSuperData().entrySet()) {
                superData.put(m.getKey(), m.getValue());
            }
        }
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(dto));
        if (jsonObject != null && jsonObject.size() > 0) {
            for (Map.Entry<String, Object> m : jsonObject.entrySet()) {
                if (defaultLabels.get(m.getKey()) != null && StringUtil.isNotEmpty(String.valueOf(m.getValue()))) {
                    // qq 微信等系统自建属性
                    superData.put(defaultLabels.get(m.getKey()), m.getValue());
                }
            }
        }
        dto.setSuperData(superData);
    }

    /**
     * 添加线索
     *
     * @param dto
     * @return
     */
    public int addClueData0(CustomSeaTouchInfoDTO dto, JSONObject jsonObject) {
        // 处理qq 微信等默认自建属性值
        handleDefaultLabelValue(dto);
        StringBuffer sql = new StringBuffer();
        int status = 0;
        try {
            // 查询公海下默认客群
            CustomerSeaProperty csp = customerSeaDao.getProperty(dto.getCustomerSeaId(), "defaultClueCgId");
            if (csp == null) {
                LOG.warn("公海:" + dto.getCustomerSeaId() + ",默认线索客群不存在");
            } else {
                dto.setCust_group_id(csp.getPropertyValue());
            }
            String superId = MD5Util.encode32Bit("c" + dto.getSuper_telphone());
            dto.setSuper_id(superId);
            CustomerUser user = customerUserDao.get(NumberConvertUtil.parseLong(dto.getUser_id()));
            int dataStatus = 1;
            // 组长和员工数据状态为已分配
            if (2 == user.getUserType()) {
                dataStatus = 0;
            } else {
                // 超管和项目管理员数据状态为未分配
                dto.setUser_id(null);
            }
            LOG.info("开始保存添加线索个人信息:" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + ",数据:" + dto.toString());
            try {
                customGroupDao.createCgDataTable(NumberConvertUtil.parseInt(dto.getCust_group_id()));
            } catch (HibernateException e) {
                LOG.error("创建用户群表失败,id:" + dto.getCust_group_id(), e);
            }
            List<Map<String, Object>> list = customerDao.sqlQuery("SELECT id FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + " WHERE id= ?", superId);
            if (list.size() > 0) {
                LOG.warn("客群ID:[" + dto.getCust_group_id() + "]添加线索ID:[" + superId + "]已经存在");
                return -1;
            }

            sql.append(" INSERT INTO " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id())
                    .append(" (id, user_id, status, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`, `super_address_province_city`, `super_address_street`, `super_data`,update_time) ")
                    .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ");
            this.customerSeaDao.executeUpdateSQL(sql.toString(), superId, dto.getUser_id(), dataStatus, dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), new Timestamp(System.currentTimeMillis()));

            sql = new StringBuffer();
            sql.append(" INSERT INTO " + ConstantsUtil.SEA_TABLE_PREFIX + dto.getCustomerSeaId())
                    .append(" (id, user_id, status, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`, `super_address_province_city`, `super_address_street`, `super_data`, batch_id, data_source,create_time) ")
                    .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
            this.customerSeaDao.executeUpdateSQL(sql.toString(), superId, dto.getUser_id(), dataStatus, dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), dto.getCust_group_id(), 3, new Timestamp(System.currentTimeMillis()));
            // 保存标记信息到es中
            CustomerSeaESDTO esData = new CustomerSeaESDTO(dto);
            esData.setSuper_data(JSON.toJSONString(dto.getSuperData()));
            //es暂时取消
            //saveClueInfoToES(esData);
            // 保存到redis中号码对应关系
            phoneService.setValueByIdFromRedis(superId, dto.getSuper_telphone());
            crmRecordService.updateRecord(jsonObject.getJSONArray("field"), superId);
            adminFieldService.save(jsonObject.getJSONArray("field"), superId);
            status = 1;
        } catch (Exception e) {
            status = 0;
            LOG.error("保存添加线索个人信息" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + "失败", e);
        }
        return status;
    }

    /**
     * @author wyq
     * 分页条件查询线索
     */
    public Page<Record> getLeadsPageList(BasePageRequest<CrmLeads> basePageRequest) {
        String leadsName = basePageRequest.getData().getLeadsName();
        if (!crmParamValid.isValid(leadsName)) {
            return new Page<>();
        }
        String telephone = basePageRequest.getData().getTelephone();
        String mobile = basePageRequest.getData().getMobile();
        if (StrUtil.isEmpty(leadsName) && StrUtil.isEmpty(telephone) && StrUtil.isEmpty(mobile)) {
            return new Page<>();
        }
        return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.leads.getLeadsPageList", Kv.by("leadsName", leadsName).set("telephone", telephone).set("mobile", mobile)));
    }

    /**
     * @author wyq
     * 新增或更新线索
     */
    @Before(Tx.class)
    public R addOrUpdate(JSONObject object) {
        LkCrmLeadsEntity crmLeads = object.getObject("entity", LkCrmLeadsEntity.class);
        String batchId = StrUtil.isNotEmpty(crmLeads.getBatchId()) ? crmLeads.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(object.getJSONArray("field"), batchId);
        adminFieldService.save(object.getJSONArray("field"), batchId);
        if (crmLeads.getLeadsId() != null) {
            crmLeads.setCustomerId(0);
            crmLeads.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            crmRecordService.updateRecord(crmLeadsDao.get(crmLeads.getLeadsId()), crmLeads, CrmEnum.LEADS_TYPE_KEY.getTypes());
            //return crmLeads.update() ? R.ok() : R.error();
            crmLeadsDao.saveOrUpdate(crmLeads);
            return R.ok();
        } else {
            crmLeads.setCreateTime(new Timestamp(System.currentTimeMillis()));
            crmLeads.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            crmLeads.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            if (crmLeads.getOwnerUserId() == null) {
                crmLeads.setOwnerUserId(BaseUtil.getUser().getUserId().intValue());
            }
            crmLeads.setBatchId(batchId);
            //boolean save = crmLeads.save();
            int id = (int) crmLeadsDao.saveReturnPk(crmLeads);
            crmRecordService.addRecord(crmLeads.getLeadsId(), CrmEnum.LEADS_TYPE_KEY.getTypes());
            return id > 0 ? R.ok() : R.error();
        }
    }

    /**
     * @author wyq
     * 基本信息
     */
    public List<Record> information(Integer leadsId) {
        CrmLeads crmLeads = CrmLeads.dao.findById(leadsId);
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("线索名称", crmLeads.getLeadsName()).set("电话", crmLeads.getMobile())
                .set("手机", crmLeads.getTelephone()).set("下次联系时间", DateUtil.formatDateTime(crmLeads.getNextTime()))
                .set("地址", crmLeads.getAddress()).set("备注", crmLeads.getRemark());
        List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"), crmLeads.getBatchId());
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * @author wyq
     * 根据线索id查询
     */
    public Record queryById(Integer leadsId) {
        return JavaBeanUtil.mapToRecord(crmLeadsDao.queryById(leadsId));
        //return Db.findFirst(Db.getSql("crm.leads.queryById"), leadsId);
    }

    /**
     * @author wyq
     * 根据线索名称查询
     */
    public Record queryByName(String name) {
        return Db.findFirst(Db.getSql("crm.leads.queryByName"), name);
    }

    /**
     * @author wyq
     * 根据id 删除线索
     */
    public R deleteByIds(String leadsIds) {
        String[] idsArr = leadsIds.split(",");
        List<String> idsList = new ArrayList<>();
        for (String id : idsArr) {
            //Record record = new Record();
            idsList.add(id);
        }
        List<String> batchIdList = JavaBeanUtil.mapToRecords(crmLeadsDao.queryBatchIdByIds(Arrays.asList(idsArr)));
        return Db.tx(() -> {
            //Db.batch(Db.getSql("crm.leads.deleteByIds"), "leads_id", idsList, 100);
            crmLeadsDao.deleteByIds(idsList);
            crmLeadsDao.executeUpdateSQL("delete from lkcrm_admin_fieldv where batch_id IN( ? )", SqlAppendUtil.sqlAppendWhereIn(batchIdList));
            return true;
        }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 变更负责人
     */
    public R updateOwnerUserId(String leadsIds, Integer ownerUserId) {
        String[] ids = leadsIds.split(",");
        int update = crmLeadsDao.updateOwnerUserId(ownerUserId.toString(), Arrays.asList(ids));
        //int update = Db.update(Db.getSqlPara("crm.leads.updateOwnerUserId", Kv.by("ownerUserId", ownerUserId).set("ids", ids)));
        for (String id : ids) {
            crmRecordService.addConversionRecord(Integer.valueOf(id), CrmEnum.LEADS_TYPE_KEY.getTypes(), ownerUserId);
        }
        return update > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 线索转客户
     */
    @Before(Tx.class)
    public R translate(String leadsIds) {
        String[] leadsIdsArr = leadsIds.split(",");
        for (String leadsId : leadsIdsArr) {
            List<Map<String, Object>> maps = crmLeadsDao.sqlQuery("select * from leadsview where leads_id = ?", Integer.valueOf(leadsId));
            Record crmLeads = JavaBeanUtil.mapToRecord(maps.get(0));
            if (1 == crmLeads.getInt("is_transform")) {
                return R.error("已转化线索不能再次转化");
            }
            List<Record> leadsFields = adminFieldService.list("1");
            LkCrmCustomerEntity crmCustomer = new LkCrmCustomerEntity();
            crmCustomer.setCustomerName(crmLeads.getStr("leads_name"));
            crmCustomer.setIsLock(0);
            crmCustomer.setNextTime(crmLeads.getTimestamp("next_time"));
            crmCustomer.setMobile(crmLeads.getStr("mobile"));
            crmCustomer.setTelephone(crmLeads.getStr("telephone"));
            crmCustomer.setDealStatus("未成交");
            crmCustomer.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmCustomer.setOwnerUserId(crmLeads.getInt("owner_user_id"));
            crmCustomer.setCreateTime(new Timestamp(System.currentTimeMillis()));
            crmCustomer.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            crmCustomer.setRoUserId(",");
            crmCustomer.setRwUserId(",");
            crmCustomer.setDetailAddress(crmLeads.getStr("address"));
            crmCustomer.setLocation("");
            crmCustomer.setAddress("");
            crmCustomer.setLng("");
            crmCustomer.setLat("");
            crmCustomer.setRemark("");
            String customerBatchId = IdUtil.simpleUUID();
            crmCustomer.setBatchId(customerBatchId);
            List<LkCrmAdminFieldEntity> customerFields = crmLeadsDao.queryListBySql("select field_id,name,field_name,field_type from lkcrm_admin_field where label = '2'", LkCrmAdminFieldEntity.class);
            List<LkCrmAdminFieldvEntity> adminFieldvList = new ArrayList<>();
            for (Record leadsFIeld : leadsFields) {
                for (LkCrmAdminFieldEntity customerField : customerFields) {
                    if (leadsFIeld.get("relevant") != null && customerField.getFieldId().equals(leadsFIeld.get("relevant"))) {
                        if (customerField.getFieldType().equals(1)) {
                            ReflectionUtils.setFieldValue(crmCustomer, customerField.getFieldName(), crmLeads.get(leadsFIeld.get("field_name")));
                            //crmCustomer.set(customerField.getFieldName(), crmLeads.get(leadsFIeld.get("field_name")));
                        } else {
                            LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                            adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                            adminFieldv.setFieldId(customerField.getFieldId());
                            adminFieldv.setName(customerField.getName());
                            adminFieldvList.add(adminFieldv);
                        }
                        continue;
                    }
                    if (!customerField.getFieldType().equals(0)) {
                        continue;
                    }
                    if ("客户来源".equals(customerField.getName()) && "线索来源".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户行业".equals(customerField.getName()) && "客户行业".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                    if ("客户级别".equals(customerField.getName()) && "客户级别".equals(leadsFIeld.getStr("name"))) {
                        LkCrmAdminFieldvEntity adminFieldv = new LkCrmAdminFieldvEntity();
                        adminFieldv.setValue(crmLeads.get(leadsFIeld.get("name")));
                        adminFieldv.setFieldId(customerField.getFieldId());
                        adminFieldv.setName(customerField.getName());
                        adminFieldvList.add(adminFieldv);
                    }
                }
                ;
            }
            ;
            crmCustomerDao.save(crmCustomer);
            crmRecordService.addConversionCustomerRecord(crmCustomer.getCustomerId(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes(), crmCustomer.getCustomerName());
            adminFieldService.save(adminFieldvList, customerBatchId);
            crmLeadsDao.executeUpdateSQL("update lkcrm_crm_leads set is_transform = 1,update_time = ?,customer_id = ? where leads_id = ?",
                    DateUtil.date(), crmCustomer.getCustomerId(), Integer.valueOf(leadsId));
            List<LkCrmAdminRecordEntity> adminRecordList = crmAdminUserDao.queryListBySql("select * from lkcrm_admin_record where types = 'crm_leads' and types_id = ?", Integer.valueOf(leadsId), LkCrmAdminRecordEntity.class);
            List<LkCrmAdminFileEntity> adminFileList = new ArrayList<>();
            if (adminRecordList.size() != 0) {
                adminRecordList.forEach(adminRecord -> {
                    List<LkCrmAdminFileEntity> leadsRecordFiles = crmLeadsDao.queryListBySql("select * from lkcrm_admin_file where batch_id = ?", adminRecord.getBatchId(), LkCrmAdminFileEntity.class);
                    String customerRecordBatchId = IdUtil.simpleUUID();
                    leadsRecordFiles.forEach(adminFile -> {
                        adminFile.setBatchId(customerRecordBatchId);
                        adminFile.setFileId(null);
                    });
                    adminFileList.addAll(leadsRecordFiles);
                    adminRecord.setBatchId(customerRecordBatchId);
                    adminRecord.setRecordId(null);
                    adminRecord.setTypes("crm_customer");
                    adminRecord.setTypesId(crmCustomer.getCustomerId());
                    adminRecord.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                });
                //Db.batchSave(adminRecordList, 100);
                crmLeadsDao.batchSaveOrUpdate(adminRecordList);
            }
            List<LkCrmAdminFileEntity> fileList = crmLeadsDao.queryListBySql("select * from lkcrm_admin_file where batch_id = ?", crmLeads.getStr("batch_id"), LkCrmAdminFileEntity.class);
            if (fileList.size() != 0) {
                fileList.forEach(adminFile -> {
                    adminFile.setBatchId(customerBatchId);
                    adminFile.setFileId(null);
                });
            }
            adminFileList.addAll(fileList);
            //Db.batchSave(adminFileList, 100);
            crmLeadsDao.batchSaveOrUpdate(adminFileList);
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer leadsId) {
        Record leads = JavaBeanUtil.mapToRecord(crmAdminUserDao.sqlQuery("select * from leadsview where leads_id = ?", leadsId).get(0));
        return adminFieldService.queryUpdateField(1, leads);
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(LkCrmAdminRecordEntity adminRecord) {
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        adminRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        adminRecord.setTypes("crm_leads");
        if (adminRecord.getIsEvent() != null && 1 == adminRecord.getIsEvent()) {
            LkCrmOaEventEntity oaEvent = new LkCrmOaEventEntity();
            oaEvent.setTitle(adminRecord.getContent());
            oaEvent.setCreateUserId(adminRecord.getCreateUserId());
            oaEvent.setStartTime(adminRecord.getNextTime());
            oaEvent.setEndTime(DateUtil.offsetDay(adminRecord.getNextTime(), 1).toTimestamp());
            oaEvent.setCreateTime(DateUtil.date().toTimestamp());
            crmOaEventDao.save(oaEvent);
        }
        crmAdminRecordDao.executeUpdateSQL("update lkcrm_crm_leads set followup = 1 where leads_id = ?", adminRecord.getTypesId());
        return (int) crmAdminRecordDao.saveReturnPk(adminRecord) > 0 ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public List<Record> getRecord(BasePageRequest<CrmLeads> basePageRequest) {
        CrmLeads crmLeads = basePageRequest.getData();
        //List<Record> recordList = Db.find(Db.getSql("crm.leads.getRecord"), crmLeads.getLeadsId());
        List<Record> recordList = crmLeadsDao.getRecord(crmLeads.getLeadsId());
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
        });
        return recordList;
    }

    /**
     * @author wyq
     * 线索导出
     */
    public List<Record> exportLeads(String leadsIds) {
        String[] leadsIdsArr = leadsIds.split(",");
        return Db.find(Db.getSqlPara("crm.leads.excelExport", Kv.by("ids", leadsIdsArr)));
    }

    /**
     * @author wyq
     * 获取线索导入查重字段
     */
    public R getCheckingField() {
        return R.ok().put("data", "线索名称");
    }

    /**
     * @author wyq
     * 导入线索
     */
    public R uploadExcel(UploadFile file, Integer repeatHandling, Integer ownerUserId) {
        ExcelReader reader = ExcelUtil.getReader(FileUtil.file(file.getUploadPath() + "\\" + file.getFileName()));
        AdminFieldService adminFieldService = new AdminFieldService();
        Kv kv = new Kv();
        Integer errNum = 0;
        try {
            List<List<Object>> read = reader.read();
            List<Object> list = read.get(1);
            List<Record> recordList = adminFieldService.customFieldList("1");
            recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
            List<Record> fieldList = adminFieldService.queryAddField(1);
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
                    List<Object> leadsList = read.get(i);
                    if (leadsList.size() < list.size()) {
                        for (int j = leadsList.size() - 1; j < list.size(); j++) {
                            leadsList.add(null);
                        }
                    }
                    String leadsName = leadsList.get(kv.getInt("leads_name")).toString();
                    Integer number = Db.queryInt("select count(*) from lkcrm_crm_leads where leads_name = ?", leadsName);
                    if (0 == number) {
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId));
                    } else if (number > 0 && repeatHandling == 1) {
                        Record leads = Db.findFirst("select leads_id,batch_id from lkcrm_crm_leads where leads_name = ?", leadsName);
                        object.fluentPut("entity", new JSONObject().fluentPut("leads_id", leads.getInt("leads_id"))
                                .fluentPut("leads_name", leadsName)
                                .fluentPut("telephone", leadsList.get(kv.getInt("telephone")))
                                .fluentPut("mobile", leadsList.get(kv.getInt("mobile")))
                                .fluentPut("address", leadsList.get(kv.getInt("address")))
                                .fluentPut("next_time", leadsList.get(kv.getInt("next_time")))
                                .fluentPut("remark", leadsList.get(kv.getInt("remark")))
                                .fluentPut("owner_user_id", ownerUserId)
                                .fluentPut("batch_id", leads.getStr("batch_id")));
                    } else if (number > 0 && repeatHandling == 2) {
                        continue;
                    }
                    JSONArray jsonArray = new JSONArray();
                    for (Record record : recordList) {
                        Integer columnsNum = kv.getInt(record.getStr("name")) != null ? kv.getInt(record.getStr("name")) : kv.getInt(record.getStr("name") + "(*)");
                        record.set("value", leadsList.get(columnsNum));
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
}
