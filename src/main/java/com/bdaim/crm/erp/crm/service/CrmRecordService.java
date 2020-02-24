package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.TypeUtils;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.LkCrmActionRecordDao;
import com.bdaim.crm.dao.LkCrmAdminFieldvDao;
import com.bdaim.crm.dao.LkCrmAdminRecordDao;
import com.bdaim.crm.entity.LkCrmActionRecordEntity;
import com.bdaim.crm.entity.LkCrmAdminConfigEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldvEntity;
import com.bdaim.crm.entity.LkCrmLeadsEntity;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.entity.*;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * crm模块操作记录
 *
 * @author hmb
 */
@Service
@Transactional
public class CrmRecordService<T> {

    @Resource
    private LkCrmAdminFieldvDao crmAdminFieldvDao;

    @Resource
    private LkCrmActionRecordDao crmActionRecordDao;

    @Resource
    private LkCrmAdminRecordDao crmAdminRecordDao;
    /**
     * 属性kv
     */
    private static Map<String, Map<String, String>> propertiesMap = new HashMap<>();
    private static final String CRM_PROPERTIES_KEY = "crm:properties_map";

    @SuppressWarnings("unchecked")
    private void init() {
        StringBuffer sql = new StringBuffer();
        sql.append(" select  COLUMN_NAME , COLUMN_COMMENT,'4' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = 'lkcrm_crm_product'\n" +
                "    union all " +
                "    select  COLUMN_NAME , COLUMN_COMMENT,'3' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = 'lkcrm_crm_contacts'\n" +
                "    union all " +
                "    select  COLUMN_NAME , COLUMN_COMMENT,'2' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = 'lkcrm_crm_customer'\n" +
                "    union all " +
                "    select  COLUMN_NAME , COLUMN_COMMENT,'1' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = 'lkcrm_crm_leads'\n" +
                "    union all " +
                "    select  COLUMN_NAME , COLUMN_COMMENT,'6' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = 'lkcrm_crm_contract'\n" +
                "    union all " +
                "    select  COLUMN_NAME , COLUMN_COMMENT,'7' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = 'lkcrm_crm_receivables'\n" +
                "    union all " +
                "    select  COLUMN_NAME , COLUMN_COMMENT,'5' as type FROM INFORMATION_SCHEMA.COLUMNS  where table_name = 'lkcrm_crm_business'");

        //List<Record> recordList = Db.findByCache(CRM_PROPERTIES_KEY, CRM_PROPERTIES_KEY, Db.getSql("crm.record.getProperties"));
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldvDao.sqlQuery(sql.toString()));
        Map<String, List<Record>> pMap = recordList.stream().collect(Collectors.groupingBy(record -> record.get("type")));
        setProperties(pMap);
    }

    private void setProperties(Map<String, List<Record>> pMap) {
        pMap.forEach((k, v) -> {
            HashMap<String, String> resultMap = new HashMap<>();
            v.forEach(record -> resultMap.put(record.getStr("COLUMN_NAME"), record.getStr("COLUMN_COMMENT")));
            propertiesMap.put(k, resultMap);
        });
    }

    private static List<String> textList = new ArrayList<>();

    /**
     * 更新记录
     *
     * @param oldObj   之前对象
     * @param newObj   新对象
     * @param crmTypes 类型
     */
    void updateRecord(T oldObj, T newObj, String crmTypes) {
        init();
        LkCrmActionRecordEntity crmActionRecord = new LkCrmActionRecordEntity();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));

        if (crmTypes.equals(CrmEnum.PRODUCT_TYPE_KEY.getTypes())) {
            CrmProduct oldObj1 = (CrmProduct) oldObj;
            CrmProduct newObj1 = (CrmProduct) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getProductId().toString());
        } else if (crmTypes.equals(CrmEnum.CONTACTS_TYPE_KEY.getTypes())) {
            CrmContacts oldObj1 = (CrmContacts) oldObj;
            CrmContacts newObj1 = (CrmContacts) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getContactsId().toString());
        } else if (crmTypes.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            CrmCustomer oldObj1 = (CrmCustomer) oldObj;
            CrmCustomer newObj1 = (CrmCustomer) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getCustomerId().toString());
        } else if (crmTypes.equals(CrmEnum.LEADS_TYPE_KEY.getTypes())) {
            LkCrmLeadsEntity oldObj1 = (LkCrmLeadsEntity) oldObj;
            LkCrmLeadsEntity newObj1 = (LkCrmLeadsEntity) newObj;
            Map<String, Object> stringObjectMap = BeanUtil.beanToMap(oldObj1);
            Map<String, Object> stringObjectMap1 = BeanUtil.beanToMap(newObj1);

            searchChange(textList, stringObjectMap.entrySet(), stringObjectMap1.entrySet(), CrmEnum.LEADS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.LEADS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getLeadsId().toString());
        } else if (crmTypes.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes())) {
            CrmContract oldObj1 = (CrmContract) oldObj;
            CrmContract newObj1 = (CrmContract) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.CONTRACT_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CONTRACT_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getContractId().toString());
        } else if (crmTypes.equals(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes())) {
            CrmReceivables oldObj1 = (CrmReceivables) oldObj;
            CrmReceivables newObj1 = (CrmReceivables) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getReceivablesId().toString());
        } else if (crmTypes.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
            CrmBusiness oldObj1 = (CrmBusiness) oldObj;
            CrmBusiness newObj1 = (CrmBusiness) newObj;
            searchChange(textList, oldObj1._getAttrsEntrySet(), newObj1._getAttrsEntrySet(), CrmEnum.BUSINESS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.BUSINESS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getBusinessId().toString());
        }
        crmActionRecord.setContent(JSON.toJSONString(textList));
        if (textList.size() > 0) {
            crmActionRecordDao.save(crmActionRecord);
        }
        textList.clear();
    }

    public void addRecord(Object actionId, String crmTypes) {
        LkCrmActionRecordEntity crmActionRecord = new LkCrmActionRecordEntity();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(String.valueOf(actionId));
        ArrayList<String> strings = new ArrayList<>();
        strings.add("新建了" + CrmEnum.getName(crmTypes));
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecordDao.save(crmActionRecord);
    }

    public void updateRecord(JSONArray jsonArray, String batchId) {
        if (jsonArray == null) {
            return;
        }
        List<LkCrmAdminFieldvEntity> oldFieldList = crmAdminFieldvDao.listByBatchId(batchId);
        oldFieldList.forEach(oldField -> {
            jsonArray.forEach(json -> {
                LkCrmAdminFieldvEntity newField = TypeUtils.castToJavaBean(json, LkCrmAdminFieldvEntity.class);
                String oldFieldValue;
                String newFieldValue;
                if (oldField.getValue() == null) {
                    oldFieldValue = "空";
                } else {
                    oldFieldValue = oldField.getValue();
                }
                if (newField.getValue() == null) {
                    newFieldValue = "空";
                } else {
                    newFieldValue = newField.getValue();
                }
                if (oldField.getName().equals(newField.getName()) && !oldFieldValue.equals(newFieldValue)) {
                    textList.add("将" + oldField.getName() + " 由" + oldFieldValue + "修改为" + newFieldValue + "。");
                }
            });
        });
    }

    private void searchChange(List<String> textList, Set<Map.Entry<String, Object>> oldEntries, Set<Map.Entry<String, Object>> newEntries, String crmTypes) {
        oldEntries.forEach(x -> {
            newEntries.forEach(y -> {
                Object oldValue = x.getValue();
                Object newValue = y.getValue();
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
                    if (!"update_time".equals(x.getKey())) {
                        textList.add("将" + propertiesMap.get(crmTypes).get(x.getKey()) + " 由" + oldValue + "修改为" + newValue + "。");
                    }
                }
            });
        });
    }

    public R queryRecordList(String actionId, String crmTypes) {
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmActionRecordDao.sqlQuery("select a.*,b.realname,b.img from lkcrm_crm_action_record a left join lkcrm_admin_user b on a.create_user_id = b.user_id where action_id = ? and types = ? order by create_time desc", actionId, crmTypes));
        recordList.forEach(record -> {
            List<String> list = JSON.parseArray(record.getStr("content"), String.class);
            record.set("content", list);
        });
        return R.ok().put("data", JavaBeanUtil.recordToMap(recordList));
    }

    /**
     * 添加转移记录
     *
     * @param actionId
     * @param crmTypes
     */
    public void addConversionRecord(Integer actionId, String crmTypes, Object userId) {
        String name = crmAdminRecordDao.queryForObject("select realname from lkcrm_admin_user where user_id = ?", userId);
        LkCrmActionRecordEntity crmActionRecord = new LkCrmActionRecordEntity();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(DateUtil.date().toTimestamp());
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(actionId.toString());
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将" + CrmEnum.getName(crmTypes) + "转移给：" + name);
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecordDao.save(crmActionRecord);
    }

    /**
     * 添加(锁定/解锁)记录
     */
    public void addIsLockRecord(String[] ids, String crmTypes, Integer isLock) {
        LkCrmActionRecordEntity crmActionRecord = new LkCrmActionRecordEntity();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(DateUtil.date().toTimestamp());
        crmActionRecord.setTypes(crmTypes);
        ArrayList<String> strings = new ArrayList<>();
        if (isLock == 1) {
            strings.add("将客户锁定。");
        } else {
            strings.add("将客户解锁。");
        }
        crmActionRecord.setContent(JSON.toJSONString(strings));
        for (String actionId : ids) {
            crmActionRecord.setActionId(actionId);
            crmActionRecordDao.save(crmActionRecord);
        }
    }

    /**
     * 线索转化客户
     *
     * @param actionId
     * @param crmTypes
     */
    public void addConversionCustomerRecord(Integer actionId, String crmTypes, String name) {
        LkCrmActionRecordEntity crmActionRecord = new LkCrmActionRecordEntity();
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        crmActionRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(actionId.toString());
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将线索\"" + name + "\"转化为客户");
        crmActionRecord.setContent(JSON.toJSONString(strings));
        crmActionRecordDao.save(crmActionRecord);
    }

    /**
     * 放入公海
     *
     * @param actionIds
     * @param crmTypes
     */
    public void addPutIntoTheOpenSeaRecord(Collection actionIds, String crmTypes) {
        LkCrmActionRecordEntity crmActionRecord = new LkCrmActionRecordEntity();
        if (BaseUtil.getRequest() == null) {
            crmActionRecord.setCreateUserId(BaseConstant.SUPER_ADMIN_USER_ID.intValue());
        } else {
            crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
        }
        crmActionRecord.setCreateTime(DateUtil.date().toTimestamp());
        crmActionRecord.setTypes(crmTypes);
        ArrayList<String> strings = new ArrayList<>();
        strings.add("将客户放入公海");
        crmActionRecord.setContent(JSON.toJSONString(strings));
        for (Object actionId : actionIds) {
            //crmActionRecord.remove("id");
            crmActionRecord.setActionId(String.valueOf(actionId));
            crmActionRecordDao.save(crmActionRecord);
        }
    }


    /**
     * 添加分配客户记录
     *
     * @param actionId
     * @param crmTypes
     */
    public void addDistributionRecord(String actionId, String crmTypes, Long userId) {
        for (String id : actionId.split(",")) {
            if (StrUtil.isEmpty(id)) {
                continue;
            }
            ArrayList<String> strings = new ArrayList<>();
            String name = crmAdminFieldvDao.queryForObject("select realname from lkcrm_admin_user where user_id = ?", userId);
            LkCrmActionRecordEntity crmActionRecord = new LkCrmActionRecordEntity();
            crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId().intValue());
            crmActionRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
            crmActionRecord.setTypes(crmTypes);
            crmActionRecord.setActionId(id);
            if (userId == null) {
                //领取
                strings.add("领取了客户");
            } else {
                //管理员分配
                strings.add("将客户分配给：" + name);
            }
            crmActionRecord.setContent(JSON.toJSONString(strings));
            crmActionRecordDao.save(crmActionRecord);
        }
    }


    /**
     * @author wyq
     * 删除跟进记录
     */
    public R deleteFollowRecord(Integer recordId) {
        crmAdminRecordDao.delete(recordId);
        return R.ok();
    }

    /**
     * @author wyq
     * 查询跟进记录类型
     */
    public R queryRecordOptions() {
        List<String> list = crmActionRecordDao.queryListBySql("select value from lkcrm_admin_config where name = ? ", "followRecordOption");
        return R.ok().put("data", list);
    }

    /**
     * @author wyq
     * 设置跟进记录类型
     */
    @Before(Tx.class)
    public R setRecordOptions(List<String> list) {
        crmActionRecordDao.executeUpdateSQL("delete from lkcrm_admin_config where name = 'followRecordOption'");
        List<LkCrmAdminConfigEntity> adminConfigList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            LkCrmAdminConfigEntity adminConfig = new LkCrmAdminConfigEntity();
            adminConfig.setName("followRecordOption");
            adminConfig.setValue(list.get(i));
            adminConfig.setDescription("跟进记录选项");
            adminConfigList.add(adminConfig);
        }
        crmActionRecordDao.batchSaveOrUpdate(adminConfigList);
        return R.ok();
    }
}
