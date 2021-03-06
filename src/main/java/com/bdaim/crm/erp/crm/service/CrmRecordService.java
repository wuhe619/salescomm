package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.TypeUtils;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.LkCrmActionRecordDao;
import com.bdaim.crm.dao.LkCrmAdminFieldvDao;
import com.bdaim.crm.dao.LkCrmAdminRecordDao;
import com.bdaim.crm.entity.*;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.StringUtil;
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
            v.forEach(record -> {
                resultMap.put(record.getStr("COLUMN_NAME"), record.getStr("COLUMN_COMMENT"));
                resultMap.put(StringUtil.toCamelCase(record.getStr("COLUMN_NAME")), record.getStr("COLUMN_COMMENT"));
            });
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
        crmActionRecord.setCreateUserId(BaseUtil.getUser().getUserId());
        crmActionRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));

        if (crmTypes.equals(CrmEnum.PRODUCT_TYPE_KEY.getTypes())) {
            LkCrmProductEntity oldObj1 = (LkCrmProductEntity) oldObj;
            LkCrmProductEntity newObj1 = (LkCrmProductEntity) newObj;
            Map<String, Object> source = BeanUtil.beanToMap(oldObj1);
            Map<String, Object> target = BeanUtil.beanToMap(newObj1, false, true);

            searchChange(textList, source.entrySet(), target.entrySet(), CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.PRODUCT_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getProductId().toString());
        } else if (crmTypes.equals(CrmEnum.CONTACTS_TYPE_KEY.getTypes())) {
            LkCrmContactsEntity oldObj1 = (LkCrmContactsEntity) oldObj;
            LkCrmContactsEntity newObj1 = (LkCrmContactsEntity) newObj;
            Map<String, Object> source = BeanUtil.beanToMap(oldObj1);
            Map<String, Object> target = BeanUtil.beanToMap(newObj1, false, true);
            searchChange(textList, source.entrySet(), target.entrySet(), CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CONTACTS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getContactsId().toString());
        } else if (crmTypes.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            LkCrmCustomerEntity oldObj1 = (LkCrmCustomerEntity) oldObj;
            LkCrmCustomerEntity newObj1 = (LkCrmCustomerEntity) newObj;
            Map<String, Object> source = BeanUtil.beanToMap(oldObj1);
            Map<String, Object> target = BeanUtil.beanToMap(newObj1, false, true);
            searchChange(textList, source.entrySet(), target.entrySet(), CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CUSTOMER_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getCustomerId().toString());
        } else if (crmTypes.equals(CrmEnum.LEADS_TYPE_KEY.getTypes())) {
            LkCrmLeadsEntity oldObj1 = (LkCrmLeadsEntity) oldObj;
            LkCrmLeadsEntity newObj1 = (LkCrmLeadsEntity) newObj;
            Map<String, Object> source = BeanUtil.beanToMap(oldObj1);
            Map<String, Object> target = BeanUtil.beanToMap(newObj1, false, true);

            searchChange(textList, source.entrySet(), target.entrySet(), CrmEnum.LEADS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.LEADS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getLeadsId().toString());
        } else if (crmTypes.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes())) {
            LkCrmContractEntity oldObj1 = (LkCrmContractEntity) oldObj;
            LkCrmContractEntity newObj1 = (LkCrmContractEntity) newObj;
            Map<String, Object> source = BeanUtil.beanToMap(oldObj1);
            Map<String, Object> target = BeanUtil.beanToMap(newObj1, false, true);
            searchChange(textList, source.entrySet(), target.entrySet(), CrmEnum.CONTRACT_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.CONTRACT_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getContractId().toString());
        } else if (crmTypes.equals(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes())) {
            LkCrmReceivablesEntity oldObj1 = (LkCrmReceivablesEntity) oldObj;
            LkCrmReceivablesEntity newObj1 = (LkCrmReceivablesEntity) newObj;
            Map<String, Object> source = BeanUtil.beanToMap(oldObj1);
            Map<String, Object> target = BeanUtil.beanToMap(newObj1, false, true);
            searchChange(textList, source.entrySet(), target.entrySet(), CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getReceivablesId().toString());
        } else if (crmTypes.equals(CrmEnum.BUSINESS_TYPE_KEY.getTypes())) {
            LkCrmBusinessEntity oldObj1 = (LkCrmBusinessEntity) oldObj;
            LkCrmBusinessEntity newObj1 = (LkCrmBusinessEntity) newObj;
            Map<String, Object> source = BeanUtil.beanToMap(oldObj1);
            Map<String, Object> target = BeanUtil.beanToMap(newObj1, false, true);
            searchChange(textList, source.entrySet(), target.entrySet(), CrmEnum.BUSINESS_TYPE_KEY.getTypes());
            crmActionRecord.setTypes(CrmEnum.BUSINESS_TYPE_KEY.getTypes());
            crmActionRecord.setActionId(oldObj1.getBusinessId().toString());
        }
        crmActionRecord.setContent(JSON.toJSONString(textList));
        if (textList.size() > 0) {
            crmActionRecord.setCreateTime(DateUtil.date().toTimestamp());
            crmActionRecordDao.save(crmActionRecord);
        }
        textList.clear();
    }

    public void addRecord(Object actionId, String crmTypes, final Object... operationNames) {
        LkCrmActionRecordEntity crmActionRecord = new LkCrmActionRecordEntity();
        LoginUser user = BaseUtil.getUser();
        crmActionRecord.setCreateUserId(user.getUserId());
        crmActionRecord.setCustId(user.getCustId());
        crmActionRecord.setCreateTime(new Timestamp(System.currentTimeMillis()));
        crmActionRecord.setTypes(crmTypes);
        crmActionRecord.setActionId(String.valueOf(actionId));
        ArrayList<String> strings = new ArrayList<>();
        if (operationNames == null || operationNames.length == 0) {
            strings.add("新建了" + CrmEnum.getName(crmTypes));
        } else {
            strings.add(operationNames[0] + CrmEnum.getName(crmTypes));
        }
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
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmActionRecordDao.sqlQuery("select a.*,b.realname, b.img from lkcrm_crm_action_record a left join lkcrm_admin_user b on a.create_user_id = b.user_id where action_id = ? and types = ? order by create_time desc", actionId, crmTypes));
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
        LoginUser user = BaseUtil.getUser();
        crmActionRecord.setCreateUserId(user.getUserId());
        crmActionRecord.setCustId(user.getCustId());
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
        LoginUser user = BaseUtil.getUser();
        crmActionRecord.setCreateUserId(user.getUserId());
        crmActionRecord.setCustId(user.getCustId());
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
        LoginUser user = BaseUtil.getUser();
        crmActionRecord.setCustId(user.getCustId());
        crmActionRecord.setCreateUserId(user.getUserId());
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
        LoginUser user = BaseUtil.getUser();
        crmActionRecord.setCustId(user.getCustId());
        Long adminUserId = BaseUtil.getAdminUserId();
        if (BaseUtil.getRequest() == null) {
            crmActionRecord.setCreateUserId(adminUserId);
        } else {
            crmActionRecord.setCreateUserId(user.getUserId());
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
            LoginUser user = BaseUtil.getUser();
            crmActionRecord.setCustId(user.getCustId());
            crmActionRecord.setCreateUserId(user.getUserId());
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
     * 删除跟进记录
     */
    public R deleteFollowRecord(Integer recordId) {
        crmAdminRecordDao.delete(recordId);
        return R.ok();
    }

    /**
     * 查询跟进记录类型
     */
    public R queryRecordOptions() {
        List<String> list = crmActionRecordDao.queryListBySql("select value from lkcrm_admin_config where name = ? AND cust_id = ? ", "followRecordOption", BaseUtil.getCustId());
        return R.ok().put("data", list);
    }

    /**
     * 设置跟进记录类型
     */
    @Before(Tx.class)
    public R setRecordOptions(List<String> list) {
        LoginUser user = BaseUtil.getUser();
        crmActionRecordDao.executeUpdateSQL("delete from lkcrm_admin_config where name = 'followRecordOption' AND cust_id = ? ", user.getCustId());
        List<LkCrmAdminConfigEntity> adminConfigList = new ArrayList<>();
        for (int i = 0; i < list.size(); i++) {
            LkCrmAdminConfigEntity adminConfig = new LkCrmAdminConfigEntity();
            adminConfig.setCustId(user.getCustId());
            adminConfig.setName("followRecordOption");
            adminConfig.setValue(list.get(i));
            adminConfig.setDescription("跟进记录选项");
            adminConfigList.add(adminConfig);
        }
        crmActionRecordDao.batchSaveOrUpdate(adminConfigList);
        return R.ok();
    }
}
