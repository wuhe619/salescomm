package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.entity.*;
import com.bdaim.crm.erp.admin.service.AdminExamineRecordService;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.admin.service.AdminFileService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.entity.CrmContract;
import com.bdaim.crm.erp.crm.entity.CrmContractProduct;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.service.OaActionRecordService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.CrmPage;
import com.bdaim.crm.utils.FieldUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;

@Service
@Transactional
public class CrmContractService {

    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private FieldUtil fieldUtil;

    @Resource
    private CrmRecordService crmRecordService;

    @Resource
    private AdminFileService adminFileService;

    @Resource
    private AdminExamineRecordService examineRecordService;

    @Resource
    private OaActionRecordService oaActionRecordService;

    @Resource
    private LkCrmContractDao crmContractDao;
    @Resource
    private LkCrmReceivablesDao crmReceivablesDao;
    @Resource
    private LkCrmContractProductDao crmContractProductDao;
    @Resource
    private LkCrmBusinessProductDao crmBusinessProductDao;
    @Resource
    private LkCrmReceivablesPlanDao crmReceivablesPlanDao;
    @Resource
    private LkCrmCustomerDao crmCustomerDao;
    @Resource
    private LkCrmOaEventDao crmOaEventDao;
    @Resource
    private LkCrmOaEventRelationDao crmOaEventRelationDao;
    @Resource
    private LkCrmAdminRecordDao crmAdminRecordDao;
    @Resource
    private LkCrmAdminConfigDao crmAdminConfigDao;
    @Resource
    private LkCrmAdminFieldDao crmAdminFieldDao;

    /**
     * 分页条件查询合同
     *
     * @return
     */
    public CrmPage queryPage(BasePageRequest<CrmContract> basePageRequest) {
        Page page = crmContractDao.pageProductPageList(basePageRequest.getPage(), basePageRequest.getLimit());
        return BaseUtil.crmPage(page);
        // return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.contract.getProductPageList"));
    }

    /**
     * 根据id查询合同
     */
    public R queryById(Integer id) {
        //Record record = Db.findFirst(Db.getSql("crm.contract.queryByContractId"), id);
        //Record record = JavaBeanUtil.mapToRecord(crmContractDao.queryByContractId(id).get(0));
        return R.ok().put("data", crmContractDao.queryByContractId(id));
    }

    /**
     * 根据id查询合同基本信息
     */
    public List<Record> information(Integer id) {
        Record record = JavaBeanUtil.mapToRecord(crmContractDao.queryByContractId(id));
        //Record record = Db.findFirst(Db.getSql("crm.contract.queryByContractId"), id);
        if (record == null) {
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("合同编号", record.getStr("num"))
                .set("合同名称", record.getStr("name"))
                .set("客户名称", record.getStr("customer_name"))
                .set("商机名称", record.getStr("business_name"))
                .set("下单时间", DateUtil.formatDate(record.getDate("order_date")))
                .set("合同金额", record.getStr("money"))
                .set("合同开始时间", DateUtil.formatDate(record.getDate("start_time")))
                .set("合同结束时间", DateUtil.formatDate(record.getDate("end_time")))
                .set("客户签约人", record.getStr("contacts_name"))
                .set("公司签约人", record.getStr("company_user_name"))
                .set("备注", record.getStr("remark"));
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.queryCustomField(record.getStr("batch_id")));
        //List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"), record.getStr("batch_id"));
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * 根据id删除合同
     */
    @Before(Tx.class)
    public R deleteByIds(String contractIds) {

        String[] idsArr = contractIds.split(",");
        List<LkCrmReceivablesEntity> list = crmReceivablesDao.queryReceivablesByContractIds(Arrays.asList(idsArr));
        if (list.size() > 0) {
            return R.error("该数据已被其他模块引用，不能被删除！");
        }
        for (String id : idsArr) {
            LkCrmContractEntity contract = crmContractDao.get(NumberConvertUtil.parseInt(id));
            if (contract != null) {
                crmContractDao.executeUpdateSQL("delete FROM lkcrm_admin_fieldv where batch_id = ?", contract.getBatchId());
                crmContractDao.delete(NumberConvertUtil.parseInt(id));
            }
        }
        return R.ok();
    }

    /**
     * 添加或修改
     */
    @Before(Tx.class)
    public R saveAndUpdate(JSONObject jsonObject) {
        CrmContract entity = jsonObject.getObject("entity", CrmContract.class);
        LkCrmContractEntity crmContract = new LkCrmContractEntity();
        BeanUtils.copyProperties(entity, crmContract);
        String batchId = StrUtil.isNotEmpty(crmContract.getBatchId()) ? crmContract.getBatchId() : IdUtil.simpleUUID();
        crmContract.setCustId(BaseUtil.getUser().getCustId());
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        boolean flag;
        if (entity.getContractId() == null) {
            Integer contract = crmContractDao.queryByNum(crmContract.getNum());
            if (contract != 0) {
                return R.error("合同编号已存在，请校对后再添加！");
            }
            crmContract.setCreateUserId(BaseUtil.getUser().getUserId());
            crmContract.setBatchId(batchId);
            crmContract.setCreateTime(DateUtil.date().toTimestamp());
            crmContract.setUpdateTime(DateUtil.date().toTimestamp());
            crmContract.setRoUserId(",");
            crmContract.setRwUserId(",");
            crmContract.setCheckStatus(0);
            crmContract.setOwnerUserId(BaseUtil.getUser().getUserId());

            Map<String, Integer> map = null;
            try {
                map = examineRecordService.saveExamineRecord(1, jsonObject.getLong("checkUserId"), crmContract.getOwnerUserId(), null);

            } catch (Exception e) {
                System.out.println(e);
            }

            if (map.get("status") == 0) {
                return R.error("没有启动的审核步骤，不能添加！");
            } else {
                crmContract.setExamineRecordId(map.get("id"));
            }
            flag = (int) crmContractDao.saveReturnPk(crmContract) > 0;
            crmRecordService.addRecord(crmContract.getContractId(), CrmEnum.CONTRACT_TYPE_KEY.getTypes());
        } else {
            crmContract.setContractId(entity.getContractId());
            LkCrmContractEntity contract = crmContractDao.get(crmContract.getContractId());
            if (contract.getCheckStatus() != 4 && contract.getCheckStatus() != 3) {
                return R.error("不能编辑，请先撤回再编辑！");
            }
            Map<String, Integer> map = examineRecordService.saveExamineRecord(1, jsonObject.getLong("checkUserId"), contract.getOwnerUserId(), contract.getExamineRecordId());
            if (map.get("status") == 0) {
                return R.error("没有启动的审核步骤，不能添加！");
            } else {
                crmContract.setExamineRecordId(map.get("id"));
            }
            crmContract.setCheckStatus(0);
            crmContract.setUpdateTime(DateUtil.date().toTimestamp());
            crmRecordService.updateRecord(new CrmContract().dao().findById(crmContract.getContractId()), crmContract, CrmEnum.CONTRACT_TYPE_KEY.getTypes());
            //flag = crmContract.update();
            crmContractDao.update(crmContract);
            flag = true;
        }
        JSONArray jsonArray = jsonObject.getJSONArray("product");
        if (jsonArray != null) {
            List<LkCrmContractProductEntity> contractProductList = jsonArray.toJavaList(LkCrmContractProductEntity.class);
            //删除之前的合同产品关联表
            crmContractDao.deleteByContractId(crmContract.getContractId());
            //Db.delete(Db.getSql("crm.contract.deleteByContractId"), crmContract.getContractId());
            if (crmContract.getBusinessId() != null) {
                crmContractDao.executeUpdateSQL("delete from lkcrm_crm_business_product where business_id = ?", crmContract.getBusinessId());
            }
            if (contractProductList != null) {
                for (LkCrmContractProductEntity crmContractProduct : contractProductList) {
                    crmContractProduct.setContractId(crmContract.getContractId());
                    //crmContractProduct.save();
                    crmContractProductDao.save(crmContractProduct);
                    if (crmContract.getBusinessId() != null) {
                        //CrmBusinessProducte crmBusinessProduct = new CrmBusinessProduct()._setOrPut(crmContractProduct.toRecord().getColumns());
                        LkCrmBusinessProductEntity crmBusinessProduct = new LkCrmBusinessProductEntity();
                        BeanUtils.copyProperties(crmContractProduct, crmBusinessProduct, JavaBeanUtil.getNullPropertyNames(crmContractProduct));
                        crmBusinessProduct.setRId(null);
                        crmBusinessProduct.setBusinessId(crmContract.getBusinessId());
                        //crmBusinessProduct.save();
                        crmBusinessProductDao.save(crmBusinessProduct);
                    }
                }
            }

        }

        return R.isSuccess(flag);

    }

    /**
     * 根据条件查询合同
     */
    public List<LkCrmContractEntity> queryList(LkCrmContractEntity crmContract) {
        StringBuilder sql = new StringBuilder("from LkCrmContractEntity where 1 = 1 ");
        List param = new ArrayList();

        if (crmContract.getCustomerId() != null) {
            param.add(crmContract.getCustomerId());
            sql.append(" and  customerId = ? ");
        }
        if (crmContract.getBusinessId() != null) {
            param.add(crmContract.getBusinessId());
            sql.append(" and  businessId = ? ");
        }
        return crmContractDao.find(sql.toString(), param.toArray());
    }

    /**
     * 根据条件查询合同
     */
    public List queryListByType(String type, Integer id) {
        String contractview = BaseUtil.getViewSql("contractview");
        StringBuilder sql = new StringBuilder("select * from " + contractview + " where ");
        if (type.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            sql.append("  customer_id = ? ");
        }
        if (type.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
            sql.append("  business_id = ? ");
        }
        return crmContractDao.sqlQuery(sql.toString(), id);
        //return Db.find(sql.toString(), id);
    }

    /**
     * 根据合同批次查询产品
     *
     * @param batchId 合同批次
     * @return
     */
    public List queryProductById(String batchId) {
        return crmContractDao.queryProductById(batchId);
        //return Db.find(Db.getSql("crm.contract.queryProductById"), batchId);
    }

    /**
     * 根据合同id查询回款
     *
     * @param id
     * @author HJP
     */
    public List<LkCrmReceivablesEntity> queryReceivablesById(Integer id) {
        return crmReceivablesDao.queryReceivablesByContractId(id);
        //return CrmReceivables.dao.find(Db.getSql("crm.receivables.queryReceivablesByContractId"), id);
    }

    /**
     * 根据合同id查询回款计划
     *
     * @param id
     * @author HJP
     */
    public List<LkCrmReceivablesPlanEntity> queryReceivablesPlanById(Integer id) {
        return crmReceivablesPlanDao.queryReceivablesPlanById(id);
        //return CrmReceivablesPlan.dao.find(Db.getSql("crm.receivablesplan.queryReceivablesPlanById"), id);
    }

    /**
     * 根据客户id变更负责人
     *
     * @author wyq
     */
    public R updateOwnerUserId(LkCrmCustomerEntity crmCustomer) {
        LkCrmContractEntity crmContract = new LkCrmContractEntity();
        crmContract.setNewOwnerUserId(crmCustomer.getNewOwnerUserId());
        crmContract.setTransferType(crmCustomer.getTransferType());
        crmContract.setPower(crmCustomer.getPower());
        String contractIds = crmContractDao.queryForObject("select GROUP_CONCAT(contract_id) from lkcrm_crm_contract where customer_id in (" + crmCustomer.getCustomerIds() + ")");
        if (StrUtil.isEmpty(contractIds)) {
            return R.ok();
        }
        crmContract.setContractIds(contractIds);
        return transfer(crmContract);
    }

    /**
     * @author wyq
     * 根据合同id变更负责人
     */
    public R transfer(LkCrmContractEntity crmContract) {
        String[] contractIdsArr = crmContract.getContractIds().split(",");
        //return Db.tx(() -> {
        for (String contractId : contractIdsArr) {
            String memberId = "," + crmContract.getNewOwnerUserId() + ",";
            //Db.update(Db.getSql("crm.contract.deleteMember"), memberId, memberId, Integer.valueOf(contractId));
            crmContractDao.deleteMember(memberId, Integer.valueOf(contractId));
            LkCrmContractEntity oldContract = crmContractDao.get(Integer.valueOf(contractId));
            if (2 == crmContract.getTransferType()) {
                if (1 == crmContract.getPower()) {
                    crmContract.setRoUserId(oldContract.getRoUserId() + oldContract.getOwnerUserId() + ",");
                }
                if (2 == crmContract.getPower()) {
                    crmContract.setRwUserId(oldContract.getRwUserId() + oldContract.getOwnerUserId() + ",");
                }
            }
            crmContract.setContractId(Integer.valueOf(contractId));
            crmContract.setOwnerUserId(crmContract.getNewOwnerUserId());
            BeanUtils.copyProperties(crmContract, oldContract, JavaBeanUtil.getNullPropertyNames(crmContract));
            crmContractDao.update(oldContract);
            crmRecordService.addConversionRecord(Integer.valueOf(contractId), CrmEnum.CONTRACT_TYPE_KEY.getTypes(), crmContract.getNewOwnerUserId());
        }
        return R.ok();
        // }) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    public List<Record> getMembers(Integer contractId) {
        LkCrmContractEntity crmContract = crmContractDao.get(contractId);
        List<Record> recordList = new ArrayList<>();
        if (null != crmContract.getOwnerUserId()) {
            Record ownerUser = JavaBeanUtil.mapToRecord(crmCustomerDao.getMembers(crmContract.getOwnerUserId()));
            //Record ownerUser = Db.findFirst(Db.getSql("crm.customer.getMembers"), crmContract.getOwnerUserId());
            recordList.add(ownerUser.set("power", "负责人权限").set("groupRole", "负责人"));
        }
        String roUserId = crmContract.getRoUserId();
        String rwUserId = crmContract.getRwUserId();
        String memberIds = roUserId + rwUserId.substring(1);
        if (",".equals(memberIds)) {
            return recordList;
        }
        String[] memberIdsArr = memberIds.substring(1, memberIds.length() - 1).split(",");
        Set<String> memberIdsSet = new HashSet<>(Arrays.asList(memberIdsArr));
        for (String memberId : memberIdsSet) {
            //Record record = JavaBeanUtil.mapToRecord(crmCustomerDao.getMembers(memberId).get(0));
            Record record = JavaBeanUtil.mapToRecord(crmCustomerDao.getMembers(NumberUtil.parseLong(memberId)));
            //Record record = Db.findFirst(Db.getSql("crm.customer.getMembers"), memberId);
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
    public R addMember(CrmContract crmContract) {
        String[] contractIdsArr = crmContract.getIds().split(",");
        String[] memberArr = crmContract.getMemberIds().split(",");
        StringBuilder stringBuilder = new StringBuilder();
        for (String id : contractIdsArr) {
            LkCrmContractEntity entity = crmContractDao.get(Integer.valueOf(id));
            if (entity == null) {
                return R.error("合同不存在");
            }
            Long ownerUserId = entity.getOwnerUserId();
            for (String memberId : memberArr) {
                if (ownerUserId.equals(NumberConvertUtil.parseLong(memberId))) {
                    return R.error("负责人不能重复选为团队成员");
                }
                crmContractDao.deleteMember("," + memberId + ",", Integer.valueOf(id));
                //Db.update(Db.getSql("crm.contract.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
            }
            if (1 == crmContract.getPower()) {
                stringBuilder.setLength(0);
                String roUserIdDb = crmContractDao.get(Integer.valueOf(id)).getRoUserId();
                if ((StringUtil.isNotEmpty(roUserIdDb) && !roUserIdDb.startsWith(",")) || StringUtil.isEmpty(roUserIdDb)) {
                    stringBuilder.append(",");
                }
                String roUserId = stringBuilder.append(roUserIdDb).append(crmContract.getMemberIds()).append(",").toString();
                crmContractDao.executeUpdateSQL("update lkcrm_crm_contract set ro_user_id = ? where contract_id = ?", roUserId, Integer.valueOf(id));
            }
            if (2 == crmContract.getPower()) {
                stringBuilder.setLength(0);
                String roUserIdDb = crmContractDao.get(Integer.valueOf(id)).getRwUserId();
                if ((StringUtil.isNotEmpty(roUserIdDb) && !roUserIdDb.startsWith(",")) || StringUtil.isEmpty(roUserIdDb)) {
                    stringBuilder.append(",");
                }
                String rwUserId = stringBuilder.append(roUserIdDb).append(crmContract.getMemberIds()).append(",").toString();
                crmContractDao.executeUpdateSQL("update lkcrm_crm_contract set rw_user_id = ? where contract_id = ?", rwUserId, Integer.valueOf(id));
            }
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    public R deleteMembers(CrmContract crmContract) {
        String[] contractIdsArr = crmContract.getIds().split(",");
        String[] memberArr = crmContract.getMemberIds().split(",");
        //return Db.tx(() -> {
        int code = 0;
        for (String id : contractIdsArr) {
            for (String memberId : memberArr) {
                code += crmContractDao.deleteMember("," + memberId + ",", Integer.valueOf(id));
                //Db.update(Db.getSql("crm.contract.deleteMember"), "," + memberId + ",", "," + memberId + ",", Integer.valueOf(id));
            }
        }
        return code > 0 ? R.ok() : R.error();
        //}) ? R.ok() : R.error();
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer contractId) {
        String contractview = BaseUtil.getViewSql("contractview");
        Record contract = JavaBeanUtil.mapToRecord(crmContractDao.sqlQuery("select * from " + contractview + " where contract_id = ?", contractId).get(0));
        //Record contract = Db.findFirst("select * from contractview where contract_id = ?",contractId);
        List<Record> list = new ArrayList<>();
        list.add(new Record().set("customer_id", contract.getInt("customer_id")).set("customer_name", contract.getStr("customer_name")));
        contract.set("customer_id", list);
        list = new ArrayList<>();
        if (contract.getStr("business_id") != null && contract.getInt("business_id") != 0) {
            list.add(new Record().set("business_id", contract.getInt("business_id")).set("business_name", contract.getStr("business_name")));
        }
        contract.set("business_id", list);
        list = new ArrayList<>();
        if (contract.getStr("contacts_id") != null && contract.getInt("contacts_id") != 0) {
            list.add(new Record().set("contacts_id", contract.getStr("contacts_id")).set("name", contract.getStr("contacts_name")));
        }
        contract.set("contacts_id", list);
        list = new ArrayList<>();
        if (contract.getStr("company_user_id") != null && contract.getInt("company_user_id") != 0) {
            list.add(new Record().set("company_user_id", contract.getStr("company_user_id")).set("realname", contract.getStr("company_user_name")));
        }
        contract.set("company_user_id", list);
        List<Record> fieldList = adminFieldService.queryUpdateField(6, contract);
        Kv kv = Kv.by("discount_rate", contract.getBigDecimal("discount_rate"))
                .set("product", crmContractDao.queryBusinessProduct(contractId).get(0))
                .set("total_price", contract.getStr("total_price"));
        fieldList.add(new Record().set("field_name", "product").set("name", "产品").set("value", kv).set("form_type", "product").set("setting", new String[]{}).set("is_null", 0).set("field_type", 1));
        return fieldList;
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @Before(Tx.class)
    public R addRecord(LkCrmAdminRecordEntity adminRecord) {
        adminRecord.setTypes("crm_contract");
        adminRecord.setCreateTime(DateUtil.date().toTimestamp());
        adminRecord.setCreateUserId(BaseUtil.getUser().getUserId());
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
            oaEventRelation.setContractIds("," + adminRecord.getTypesId().toString() + ",");
            oaEventRelation.setCreateTime(DateUtil.date().toTimestamp());
            crmOaEventRelationDao.save(oaEventRelation);
        }
        crmAdminRecordDao.saveReturnPk(adminRecord);
        return R.ok();
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    public List<Record> getRecord(BasePageRequest<CrmContract> basePageRequest) {
        CrmContract crmContract = basePageRequest.getData();
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmContractDao.getRecord(crmContract.getContractId()));
        //List<Record> recordList = Db.find(Db.getSql("crm.contract.getRecord"), crmContract.getContractId());
        recordList.forEach(record -> {
            adminFileService.queryByBatchId(record.getStr("batch_id"), record);
        });
        return recordList;
    }

    /**
     * 根据合同ID查询产品
     */
    public R qureyProductListByContractId(BasePageRequest<CrmContractProduct> basePageRequest) {

        Integer pageType = basePageRequest.getPageType();
        Record record = JavaBeanUtil.mapToRecord(crmBusinessProductDao.querySubtotalByBusinessId(basePageRequest.getData().getContractId()));
        //Record record = Db.findFirst(Db.getSql("crm.product.querySubtotalByContractId"), basePageRequest.getData().getContractId());
        if (record.getStr("money") == null) {
            record.set("money", 0);
        }
        if (0 == pageType) {
            record.set("list", crmBusinessProductDao.queryProductPageList(basePageRequest.getData().getContractId()));
            //record.set("list", Db.find(Db.getSql("crm.product.queryProductPageList"), basePageRequest.getData().getContractId()));
            return R.ok().put("data", record);
        } else {
            com.bdaim.common.dto.Page page = crmBusinessProductDao.pageQueryProductPageList(basePageRequest.getPage(), basePageRequest.getLimit(), basePageRequest.getData().getContractId());
            //Page<Record> page = Db.paginateByFullSql(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSql("crm.product.queryProductPagecount"), Db.getSql("crm.product.queryProductPageList"), basePageRequest.getData().getContractId());
            //record.set("pageNumber", page.get());
            //record.set("pageSize", page.getPageSize());
            //record.set("totalPage", page.getPageIndex());
            record.set("totalRow", page.getTotal());
            record.set("list", page.getData());
            return R.ok().put("data", record);
        }
    }

    /**
     * 查询合同到期提醒设置
     */
    public R queryContractConfig() {
        //LkCrmAdminConfigEntity config = crmAdminConfigDao.findUniqueBy("name", "expiringContractDays");
        LkCrmAdminConfigEntity config = crmAdminConfigDao.findUnique("FROM LkCrmAdminConfigEntity WHERE name = ? AND custId = ?", "expiringContractDays", BaseUtil.getCustId());
        if (config == null) {
            config = new LkCrmAdminConfigEntity();
            config.setCustId(BaseUtil.getCustId());
            config.setStatus(0);
            config.setName("expiringContractDays");
            config.setValue("3");
            config.setDescription("合同到期提醒");
            crmAdminConfigDao.save(config);
            //config.save();
        }
        return R.ok().put("data", config);
    }

    /**
     * 修改合同到期提醒设置
     */
    @Before(Tx.class)
    public R setContractConfig(Integer status, Integer contractDay) {
        if (status == 1 && contractDay == null) {
            return R.error("contractDay不能为空");
        }
        Integer number = crmContractDao.setContractConfig(status, contractDay);
        //Integer number = Db.update(Db.getSqlPara("crm.contract.setContractConfig", Kv.by("status", status).set("contractDay", contractDay)));
        if (0 == number) {
            LkCrmAdminConfigEntity adminConfig = new LkCrmAdminConfigEntity();
            adminConfig.setCustId(BaseUtil.getCustId());
            adminConfig.setStatus(0);
            adminConfig.setName("expiringContractDays");
            adminConfig.setValue("3");
            adminConfig.setDescription("合同到期提醒");
            crmAdminConfigDao.save(adminConfig);
        }
        return R.ok();
    }
}

