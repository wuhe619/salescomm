package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.config.cache.CaffeineCache;
import com.bdaim.crm.dao.LkCrmAdminFieldDao;
import com.bdaim.crm.dao.LkCrmAdminFieldvDao;
import com.bdaim.crm.dao.LkCrmSqlViewDao;
import com.bdaim.crm.entity.LkCrmAdminFieldEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldSortEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldStyleEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldvEntity;
import com.bdaim.crm.erp.admin.entity.AdminFieldSort;
import com.bdaim.crm.utils.*;
import com.bdaim.customer.dao.CustomerLabelDao;
import com.bdaim.customer.dto.CustomerLabelDTO;
import com.bdaim.customer.entity.CustomerLabel;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.util.ConstantsUtil;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional
public class AdminFieldService {

    @Resource
    private LkCrmAdminFieldDao crmAdminFieldDao;

    @Resource
    private LkCrmAdminFieldvDao crmAdminFieldvDao;

    @Resource
    private CustomerLabelDao customerLabelDao;

    @Autowired
    private LkCrmSqlViewDao crmSqlViewDao;

    private static String[] SHOW_PRI_SEA_FIELD = new String[]{"线索名称", "公司名称", "客户级别", "跟进状态", "当前负责人", "添加时间", "最新跟进时间", "下次联系时间", "手机", "电话", "微信", "备注", "线索来源"};
    private static String[] SHOW_PUB_SEA_FIELD = new String[]{"线索名称", "公司名称", "部门名称", "职位", "客户级别", "前负责人", "进入公海时间", "退回原因", "手机", "电话", "微信", "备注", "线索来源"
    };

    /**
     * @author wyq
     * 查询新增字段列表
     */
    public List<Record> queryAddField(Integer label) {
        List<Map<String, Object>> maps = crmAdminFieldDao.sqlQuery("select field_id,field_name,name,type,input_tips,options,is_unique,is_null,'' as value,field_type from lkcrm_admin_field where label = ? AND cust_id = ? AND (add_hidden is NULL OR add_hidden =2) order by add_sort", label, BaseUtil.getCustId());
        List<Record> fieldList = JavaBeanUtil.mapToRecords(maps);
        recordToFormType(fieldList);
        if (label == 2) {
            Record map = new Record();
            fieldList.add(map.set("field_name", "map_address").set("name", "地区定位").set("form_type", "map_address").set("is_null", 0));
        } else if (label == 5) {
            fieldList.add(new Record().set("field_name", "type_id").set("name", "商机状态组").set("value", "").set("form_type", "business_type").set("setting", new String[0]).set("is_null", 1).set("field_type", 1));
            fieldList.add(new Record().set("field_name", "status_id").set("name", "商机阶段").set("value", "").set("form_type", "business_status").set("setting", new String[0]).set("is_null", 1).set("field_type", 1));
        } else if (label == 4) {
            fieldList.forEach(record -> {
                if (record.getStr("field_name").equals("category")) {
                    record.set("value", new String[0]);
                }
            });
        }
        if (label == 5 || label == 6) {
            Record record = new Record();
            fieldList.add(record.set("field_name", "product").set("name", "产品").set("value", Kv.by("discount_rate", "").set("product", new ArrayList<>()).set("total_price", "")).set("formType", "product").set("setting", new String[]{}).set("is_null", 0).set("field_type", 1));
        }
        return fieldList;
    }

    /**
     * @author wyq
     * 查询编辑字段列表
     */
    public List<Record> queryUpdateField(Integer label, Record record) {
        List<Map<String, Object>> maps = crmAdminFieldDao.sqlQuery("select field_id,field_name,name,type,input_tips,options,is_unique,is_null,'' as value,field_type from lkcrm_admin_field where label = ? AND cust_id = ? AND (add_hidden is NULL OR add_hidden =2)  order by sorting", label, BaseUtil.getCustId());
        List<Record> recordList = JavaBeanUtil.mapToRecords(maps);
        recordList.forEach(r -> {
            if (r.getInt("type") == 10 || r.getInt("type") == 12) {
                r.set("value", crmAdminFieldDao.queryForObject("select value from lkcrm_admin_fieldv where field_id = ? and batch_id = ? AND cust_id = ?", r.getInt("field_id"), record.getStr("batch_id"), BaseUtil.getCustId()));
            } else {
                r.set("value", record.get(r.getStr("field_name")) != null ? record.get(r.getStr("field_name")) : "");
            }
        });
        recordList.forEach(field -> {
            if (field.getInt("type") == 8) {
                field.set("value", crmAdminFieldDao.sqlQuery("select * from lkcrm_admin_file where batch_id = ?", StrUtil.isNotEmpty(field.getStr("value")) ? field.getStr("value") : ""));
            }
            if (field.getInt("type") == 10) {
                field.set("value", crmAdminFieldDao.sqlQuery("select user_id,realname from lkcrm_admin_user where find_in_set(user_id,ifnull(?,0))", field.getStr("value")));
            }
            if (field.getInt("type") == 12) {
                field.set("value", crmAdminFieldDao.sqlQuery("select dept_id,name from lkcrm_admin_dept where find_in_set(dept_id,ifnull(?,0))", field.getStr("value")));
            }
        });
        recordToFormType(recordList);
        return recordList;
    }

    /**
     * 保存自定义字段信息
     *
     * @param jsonObject 详见接口文档
     * @return R
     */
    @Before(Tx.class)
    public R save(JSONObject jsonObject) {
        JSONArray adminFields = jsonObject.getJSONArray("data");
        Map<String, List<LkCrmAdminFieldEntity>> collect = adminFields.stream().map(adminField -> TypeUtils.castToJavaBean(adminField, LkCrmAdminFieldEntity.class)).collect(Collectors.groupingBy(LkCrmAdminFieldEntity::getName));
        for (Map.Entry<String, List<LkCrmAdminFieldEntity>> entry : collect.entrySet()) {
            if (entry.getValue().size() > 1) {
                return R.error("自定义表单名称不能重复！");
            }
        }
        Integer label = jsonObject.getInteger("label");
        Integer categoryId = jsonObject.getInteger("categoryId");
        if (categoryId != null && crmAdminFieldDao.queryForInt("select ifnull(is_sys,0) from lkcrm_oa_examine_category where category_id = ?", categoryId) == 1) {
            return R.error("系统审批类型暂不支持编辑");
        }
        List<Integer> arr = new ArrayList<>();
        adminFields.forEach(object -> {
            LkCrmAdminFieldEntity field = TypeUtils.castToJavaBean(object, LkCrmAdminFieldEntity.class);
            if (field.getFieldId() != null) {
                arr.add(field.getFieldId());
            }
        });
        List<LkCrmAdminFieldEntity> fieldSorts = crmAdminFieldDao.find("from LkCrmAdminFieldEntity where label = ? AND custId = ?", label, BaseUtil.getCustId());
        List<String> nameList = fieldSorts.stream().map(LkCrmAdminFieldEntity::getName).collect(Collectors.toList());
        if (arr.size() > 0) {
           /* SqlPara sql = Db.getSqlPara("admin.field.deleteByChooseId", Kv.by("ids", arr).set("label", label).set("categoryId", categoryId));
            SqlPara sqlPara = Db.getSqlPara("admin.field.deleteByFieldValue", Kv.by("ids", arr).set("label", label).set("categoryId", categoryId));
            Db.delete(sqlPara.getSql(), sqlPara.getPara());
            Db.delete(sql.getSql(), sql.getPara());*/
            crmAdminFieldDao.deleteByChooseId(arr, label, categoryId);
            crmAdminFieldDao.deleteByFieldValue(arr, label, categoryId);
        }
        List<String> fieldList = new ArrayList<>();
        for (int i = 0; i < adminFields.size(); i++) {
            adminFields.getJSONObject(i).remove("value");
            Object defaultValue = adminFields.getJSONObject(i).get("defaultValue");
            if (defaultValue instanceof JSONArray && ((JSONArray) defaultValue).size() == 0) {
                adminFields.getJSONObject(i).remove("defaultValue");
            }
            LkCrmAdminFieldEntity entity = TypeUtils.castToJavaBean(adminFields.get(i), LkCrmAdminFieldEntity.class);
            entity.setCustId(BaseUtil.getCustId());
            entity.setUpdateTime(DateUtil.date().toTimestamp());
            if (entity.getFieldType() == null || entity.getFieldType() == 0) {
                entity.setFieldName(entity.getName());
            }
            if (label == 10) {
                entity.setExamineCategoryId(jsonObject.getInteger("categoryId"));
            }
            entity.setSorting(i);
            entity.setLabel(label);
            if (entity.getFieldId() != null) {
                ///entity.update();
                LkCrmAdminFieldEntity lkCrmAdminFieldEntity = crmAdminFieldDao.get(entity.getFieldId());
                BeanUtils.copyProperties(entity, lkCrmAdminFieldEntity, JavaBeanUtil.getNullPropertyNames(entity));
                crmAdminFieldDao.update(lkCrmAdminFieldEntity);
                if (entity.getFieldType() == 0) {
                    crmAdminFieldDao.updateFieldSortName(entity.getName(), entity.getFieldId());
                    //Db.update(Db.getSqlPara("admin.field.updateFieldSortName", entity));
                } else if (entity.getFieldType() == 1) {
                    crmAdminFieldDao.executeUpdateSQL("update lkcrm_admin_field_sort set name = ? where field_id = ?", entity.getName(), entity.getFieldId());
                }
            } else {
                //entity.save();
                if (entity.getFieldType() == null) {
                    entity.setFieldType(0);
                }
                crmAdminFieldDao.save(entity);
            }
            fieldList.add(entity.getName());
        }
        createView(label);
        nameList.removeAll(fieldList);
        if (nameList.size() != 0) {
            crmAdminFieldDao.deleteFieldSort(nameList, label);
            //Db.update(Db.getSqlPara("admin.field.deleteFieldSort", Kv.by("label", label).set("names", nameList)));
        }
        CaffeineCache.ME.removeAll("field");
        return R.ok();
    }

    public R verify(Map kv) {
        Integer number = 0;
        if ("0".equals(kv.get("fieldType"))) {
            String sql = " SELECT COUNT(*) FROM lkcrm_admin_field as a inner join lkcrm_admin_fieldv as b on a.field_id = b.field_id " +
                    "      WHERE a.label=? and a.name=? and b.value=? AND a.cust_id = ? ";
            //SqlPara sqlPara = Db.getSqlPara("admin.field.queryFieldIsExist",kv);
            number = crmAdminFieldDao.queryForInt(sql, kv.get("types"), kv.get("fieldName"), kv.get("val"), BaseUtil.getCustId());
        } else {
            String type = kv.get("types").toString();
            String tableName;
            String primaryKey;
            switch (type) {
                case "1":
                    tableName = "leads";
                    primaryKey = "leads_id";
                    break;
                case "2":
                    tableName = "customer";
                    primaryKey = "customer_id";
                    break;
                case "3":
                    tableName = "contacts";
                    primaryKey = "contacts_id";
                    break;
                case "4":
                    tableName = "product";
                    primaryKey = "product_id";
                    break;
                case "5":
                    tableName = "business";
                    primaryKey = "business_id";
                    break;
                case "6":
                    tableName = "contract";
                    primaryKey = "contract_id";
                    break;
                case "7":
                    tableName = "receivables";
                    primaryKey = "receivables_id";
                    break;
                case "8":
                    tableName = "receivables_plan";
                    primaryKey = "plan_id";
                    break;
                case "11":
                    // 查询客户默认公海
                    List<CustomerSea> publicSeaList = crmAdminFieldDao.find(" FROM CustomerSea WHERE custId = ? ", BaseUtil.getCustId());
                    if (publicSeaList.size() > 0) {
                        int val = crmAdminFieldDao.queryForInt(" SELECT count(*) from " + ConstantsUtil.SEA_TABLE_PREFIX + publicSeaList.get(0).getId() + " WHERE super_telphone = ? ", kv.get("val").toString());
                        return val > 0 ? R.error("参数校验错误").put("error", kv.get("fieldName").toString() + "：参数唯一") : R.ok();
                    }
                    return R.ok();
                default:
                    return R.error("type不符合要求");
            }
            if (!ParamsUtil.isValid(kv.get("fieldName").toString())) {
                return R.error("参数包含非法字段");
            }
            number = crmAdminFieldDao.queryForInt("select count(*) from lkcrm_crm_" + tableName + " where cust_id = ? AND " + kv.get("fieldName").toString() + " = ? and " + primaryKey + " != ? ", BaseUtil.getCustId(), kv.get("val").toString(), StringUtil.isNotEmpty(String.valueOf(kv.get("id"))) ? Integer.valueOf(kv.get("id").toString()) : 0);
        }
        return number > 0 ? R.error("参数校验错误").put("error", kv.get("fieldName").toString() + "：参数唯一") : R.ok();
    }

    /**
     * 保存自定义字段
     *
     * @param array   参数对象
     * @param batchId 批次ID
     * @return 操作结果
     */
    public boolean save(JSONArray array, String batchId) {
        if (array == null || StrUtil.isEmpty(batchId)) {
            return false;
        }
        //Db.deleteById("lkcrm_admin_fieldv", "batch_id", batchId);
        crmAdminFieldvDao.executeUpdateSQL(" DELETE FROM lkcrm_admin_fieldv WHERE batch_id = ? ", batchId);
        array.forEach(obj -> {
            LkCrmAdminFieldvEntity fieldv = TypeUtils.castToJavaBean(obj, LkCrmAdminFieldvEntity.class);
            //fieldv.setId(null);
            fieldv.setCreateTime(DateUtil.date().toTimestamp());
            fieldv.setBatchId(batchId);
            crmAdminFieldvDao.save(fieldv);
        });
        return true;
    }

    /**
     * 保存自定义字段
     *
     * @param array   参数对象
     * @param batchId 批次ID
     * @return 操作结果
     */
    public boolean save(List<LkCrmAdminFieldvEntity> array, String batchId) {
        if (array == null || StrUtil.isEmpty(batchId)) {
            return false;
        }
        crmAdminFieldvDao.deleteByBatchId(batchId);
        //Db.deleteById("lkcrm_admin_fieldv", "batch_id", batchId);
        array.forEach(fieldv -> {
            //fieldv.setId(null);
            fieldv.setCreateTime(new Timestamp(System.currentTimeMillis()));
            fieldv.setBatchId(batchId);
            fieldv.setCustId(BaseUtil.getCustId());
            crmAdminFieldvDao.save(fieldv);
        });
        return true;
    }

    public synchronized void createView(Integer label) {
        List<Record> fieldNameList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.sqlQuery("select name,type from lkcrm_admin_field WHERE label=? and field_type = 0 AND cust_id =? ORDER BY sorting asc", label, BaseUtil.getCustId()));
        StringBuilder sql = new StringBuilder();
        StringBuilder userJoin = new StringBuilder();
        StringBuilder deptJoin = new StringBuilder();
        fieldNameList.forEach(record -> {
            String name = record.getStr("name");
            Integer type = record.getInt("type");
            if (type == 10) {
                sql.append(String.format("GROUP_CONCAT(if(a.name = '%s',b.realname,null)) AS `%s`,", name, name));
                if (userJoin.length() == 0) {
                    userJoin.append(" left join lkcrm_admin_user b on find_in_set(user_id,ifnull(value,0))");
                }
            } else if (type == 12) {
                sql.append(String.format("GROUP_CONCAT(if(a.name = '%s',c.name,null)) AS `%s`,", name, name));
                if (deptJoin.length() == 0) {
                    deptJoin.append(" left join lkcrm_admin_dept c on find_in_set(c.dept_id,ifnull(value,0))");
                }
            } else {
                sql.append(String.format("max(if(a.name = '%s',value, null)) AS `%s`,", name, name));
            }
        });
        String createName = null;
        String create;
        String filedCreate;
        String filedCreateName = null;
        switch (label) {
            case 1:
                createName = "leadsview";
                filedCreateName = "fieldleadsview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,z.* from lkcrm_crm_leads as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 2:
                createName = "customerview";
                filedCreateName = "fieldcustomerview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,z.* from lkcrm_crm_customer as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 3:
                createName = "contactsview";
                filedCreateName = "fieldcontactsview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,a.name as contacts_name ,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,z.* from lkcrm_crm_contacts as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 4:
                createName = "productview";
                filedCreateName = "fieldproductview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.name as category_name,z.* from lkcrm_crm_product as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_product_category as d on a.category_id = d.category_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 5:
                createName = "businessview";
                filedCreateName = "fieldbusinessview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != ''and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.name as type_name,f.name as status_name,z.* from lkcrm_crm_business as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join lkcrm_crm_business_type as e on a.type_id = e.type_id left join lkcrm_crm_business_status as f on a.status_id = f.status_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 6:
                createName = "contractview";
                filedCreateName = "fieldcontractview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.business_name,f.name as contacts_name,g.realname as company_user_name,z.* from lkcrm_crm_contract as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join lkcrm_crm_business as e on a.business_id = e.business_id left join lkcrm_crm_contacts as f on a.contacts_id = f.contacts_id left join lkcrm_admin_user as g on a.company_user_id = g.user_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 7:
                createName = "receivablesview";
                filedCreateName = "fieldreceivablesview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.name as contract_name,e.num as contract_num,f.num as plan_num,z.* from lkcrm_crm_receivables as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join lkcrm_crm_contract as e on a.contract_id = e.contract_id left join lkcrm_crm_receivables_plan as f on a.plan_id = f.plan_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            default:
                create = "";
                filedCreate = "";
                break;
        }
        if (StrUtil.isNotBlank(filedCreate)) {
            crmSqlViewDao.saveSqlView(BaseUtil.getCustId(), filedCreateName, filedCreate);
        }
        if (StrUtil.isNotBlank(create)) {
            crmSqlViewDao.saveSqlView(BaseUtil.getCustId(), createName, create);
        }
    }

    public synchronized void createView(Integer label, String custId) {
        List<Record> fieldNameList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.sqlQuery("select name,type from lkcrm_admin_field WHERE label=? and field_type = 0 AND cust_id =? ORDER BY sorting asc", label, custId));
        StringBuilder sql = new StringBuilder();
        StringBuilder userJoin = new StringBuilder();
        StringBuilder deptJoin = new StringBuilder();
        fieldNameList.forEach(record -> {
            String name = record.getStr("name");
            Integer type = record.getInt("type");
            if (type == 10) {
                sql.append(String.format("GROUP_CONCAT(if(a.name = '%s',b.realname,null)) AS `%s`,", name, name));
                if (userJoin.length() == 0) {
                    userJoin.append(" left join lkcrm_admin_user b on find_in_set(user_id,ifnull(value,0))");
                }
            } else if (type == 12) {
                sql.append(String.format("GROUP_CONCAT(if(a.name = '%s',c.name,null)) AS `%s`,", name, name));
                if (deptJoin.length() == 0) {
                    deptJoin.append(" left join lkcrm_admin_dept c on find_in_set(c.dept_id,ifnull(value,0))");
                }
            } else {
                sql.append(String.format("max(if(a.name = '%s',value, null)) AS `%s`,", name, name));
            }
        });
        String createName = null;
        String create;
        String filedCreate;
        String filedCreateName = null;
        switch (label) {
            case 1:
                createName = "leadsview";
                filedCreateName = "fieldleadsview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,z.* from lkcrm_crm_leads as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 2:
                createName = "customerview";
                filedCreateName = "fieldcustomerview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,z.* from lkcrm_crm_customer as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 3:
                createName = "contactsview";
                filedCreateName = "fieldcontactsview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,a.name as contacts_name ,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,z.* from lkcrm_crm_contacts as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 4:
                createName = "productview";
                filedCreateName = "fieldproductview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.name as category_name,z.* from lkcrm_crm_product as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_product_category as d on a.category_id = d.category_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 5:
                createName = "businessview";
                filedCreateName = "fieldbusinessview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != ''and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.name as type_name,f.name as status_name,z.* from lkcrm_crm_business as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join lkcrm_crm_business_type as e on a.type_id = e.type_id left join lkcrm_crm_business_status as f on a.status_id = f.status_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 6:
                createName = "contractview";
                filedCreateName = "fieldcontractview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.business_name,f.name as contacts_name,g.realname as company_user_name,z.* from lkcrm_crm_contract as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join lkcrm_crm_business as e on a.business_id = e.business_id left join lkcrm_crm_contacts as f on a.contacts_id = f.contacts_id left join lkcrm_admin_user as g on a.company_user_id = g.user_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            case 7:
                createName = "receivablesview";
                filedCreateName = "fieldreceivablesview";
                filedCreate = String.format(" select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = " select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.name as contract_name,e.num as contract_num,f.num as plan_num,z.* from lkcrm_crm_receivables as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join lkcrm_crm_contract as e on a.contract_id = e.contract_id left join lkcrm_crm_receivables_plan as f on a.plan_id = f.plan_id left join (?) as z on a.batch_id = z.field_batch_id";
                break;
            default:
                create = "";
                filedCreate = "";
                break;
        }
        if (StrUtil.isNotBlank(filedCreate)) {
            crmSqlViewDao.saveSqlView(custId, filedCreateName, filedCreate);
        }
        if (StrUtil.isNotBlank(create)) {
            crmSqlViewDao.saveSqlView(custId, createName, create);
        }
    }

    public synchronized void createView0(Integer label) {
        List<Record> fieldNameList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.sqlQuery("select name,type from lkcrm_admin_field WHERE label=? and field_type = 0 AND cust_id =? ORDER BY sorting asc", label, BaseUtil.getCustId()));
        StringBuilder sql = new StringBuilder();
        StringBuilder userJoin = new StringBuilder();
        StringBuilder deptJoin = new StringBuilder();
        fieldNameList.forEach(record -> {
            String name = record.getStr("name");
            Integer type = record.getInt("type");
            if (type == 10) {
                sql.append(String.format("GROUP_CONCAT(if(a.name = '%s',b.realname,null)) AS `%s`,", name, name));
                if (userJoin.length() == 0) {
                    userJoin.append(" left join lkcrm_admin_user b on find_in_set(user_id,ifnull(value,0))");
                }
            } else if (type == 12) {
                sql.append(String.format("GROUP_CONCAT(if(a.name = '%s',c.name,null)) AS `%s`,", name, name));
                if (deptJoin.length() == 0) {
                    deptJoin.append(" left join lkcrm_admin_dept c on find_in_set(c.dept_id,ifnull(value,0))");
                }
            } else {
                sql.append(String.format("max(if(a.name = '%s',value, null)) AS `%s`,", name, name));
            }
        });
        String create;
        String filedCreate;
        switch (label) {
            case 1:
                filedCreate = String.format("create or replace view fieldleadsview as select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = "create or replace view leadsview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,z.* from lkcrm_crm_leads as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join fieldleadsview as z on a.batch_id = z.field_batch_id";
                break;
            case 2:
                filedCreate = String.format("create or replace view fieldcustomerview  as select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = "create or replace view customerview  as select a.*,b.realname as create_user_name,c.realname as owner_user_name,z.* from lkcrm_crm_customer as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join fieldcustomerview as z on a.batch_id = z.field_batch_id";
                break;
            case 3:
                filedCreate = String.format("create or replace view fieldcontactsview as select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = "create or replace view contactsview as select a.*,a.name as contacts_name ,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,z.* from lkcrm_crm_contacts as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join fieldcontactsview as z on a.batch_id = z.field_batch_id";
                break;
            case 4:
                filedCreate = String.format("create or replace view fieldproductview as select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = "create or replace view productview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.name as category_name,z.* from lkcrm_crm_product as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_product_category as d on a.category_id = d.category_id left join fieldproductview as z on a.batch_id = z.field_batch_id";
                break;
            case 5:
                filedCreate = String.format("create or replace view fieldbusinessview as select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != ''and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = "create or replace view businessview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.name as type_name,f.name as status_name,z.* from lkcrm_crm_business as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join lkcrm_crm_business_type as e on a.type_id = e.type_id left join lkcrm_crm_business_status as f on a.status_id = f.status_id left join fieldbusinessview as z on a.batch_id = z.field_batch_id";
                break;
            case 6:
                filedCreate = String.format("create or replace view fieldcontractview as select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = "create or replace view contractview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.business_name,f.name as contacts_name,g.realname as company_user_name,z.* from lkcrm_crm_contract as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join lkcrm_crm_business as e on a.business_id = e.business_id left join lkcrm_crm_contacts as f on a.contacts_id = f.contacts_id left join lkcrm_admin_user as g on a.company_user_id = g.user_id left join fieldcontractview as z on a.batch_id = z.field_batch_id";
                break;
            case 7:
                filedCreate = String.format("create or replace view fieldreceivablesview as select %s batch_id as field_batch_id from lkcrm_admin_fieldv as a inner join lkcrm_admin_field as d on `a`.`field_id` = `d`.`field_id` %s where d.label = %s and a.batch_id is not null and a.batch_id != '' and d.field_type = 0 group by a.batch_id", sql, userJoin.append(deptJoin), label);
                create = "create or replace view receivablesview as select a.*,b.realname as create_user_name,c.realname as owner_user_name,d.customer_name,e.name as contract_name,e.num as contract_num,f.num as plan_num,z.* from lkcrm_crm_receivables as a left join lkcrm_admin_user as b on a.create_user_id = b.user_id left join lkcrm_admin_user as c on a.owner_user_id = c.user_id left join lkcrm_crm_customer as d on a.customer_id = d.customer_id left join lkcrm_crm_contract as e on a.contract_id = e.contract_id left join lkcrm_crm_receivables_plan as f on a.plan_id = f.plan_id left join fieldreceivablesview as z on a.batch_id = z.field_batch_id";
                break;
            default:
                create = "";
                filedCreate = "";
                break;
        }
        if (StrUtil.isNotBlank(filedCreate)) {
            crmAdminFieldDao.executeUpdateSQL(filedCreate);
        }
        if (StrUtil.isNotBlank(create)) {
            crmAdminFieldDao.executeUpdateSQL(create);
        }
    }

    public List queryFieldsByBatchId(String batchId, String... name) {
        if (StrUtil.isEmpty(batchId)) {
            return new ArrayList<>();
        }
        return crmAdminFieldDao.queryFieldsByBatchId(batchId, name);
    }

    public List<Record> queryByBatchId(String batchId, Integer label) {
        if (StrUtil.isEmpty(batchId)) {
            return new ArrayList<>();
        }
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.queryFieldsByBatchId(batchId, String.valueOf(label)));
        //List<Record> recordList = Db.find(AdminField.dao.getSqlPara("admin.field.queryFieldsByBatchId", Kv.by("batchId", batchId).set("label", label)));
        recordList.forEach(record -> {
            if (record.getInt("type") == 10) {
                if (StrUtil.isNotEmpty(record.getStr("value"))) {
                    List<Record> userList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.sqlQuery("select user_id,realname from lkcrm_admin_user where user_id in (" + record.getStr("value") + ")"));
                    record.set("value", userList);
                } else {
                    record.set("value", new ArrayList<>());
                }
                record.set("default_value", new ArrayList<>(0));
            } else if (record.getInt("type") == 12) {
                if (StrUtil.isNotEmpty(record.getStr("value"))) {
                    List<Record> deptList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.sqlQuery("select dept_id,name from lkcrm_admin_dept where dept_id in (" + record.getStr("value") + ")"));
                    record.set("value", deptList);
                } else {
                    record.set("value", new ArrayList<>());
                }
                record.set("default_value", new ArrayList<>(0));
            }
        });
        recordToFormType(recordList);
        return recordList;
    }

    public List<Record> queryByBatchId(String batchId) {
        return queryByBatchId(batchId, null);
    }

    public R queryFields() {
        List records = crmAdminFieldDao.queryFields();
        return R.ok().put("data", records);
    }

    public void recordToFormType(List<Record> recordList) {
        for (Record record : recordList) {
            Integer dataType = record.getInt("type");
            if (1 == dataType) {
                record.set("formType", "text");
            } else if (2 == dataType) {
                record.set("formType", "textarea");
            } else if (3 == dataType) {
                record.set("formType", "select");
            } else if (4 == dataType) {
                record.set("formType", "date");
            } else if (5 == dataType) {
                record.set("formType", "number");
            } else if (6 == dataType) {
                record.set("formType", "floatnumber");
            } else if (7 == dataType) {
                record.set("formType", "mobile");
            } else if (8 == dataType) {
                record.set("formType", "file");
            } else if (9 == dataType) {
                record.set("formType", "checkbox");
                recordValueToArray(record);
            } else if (10 == dataType) {
                record.set("formType", "user");
                record.set("default_value", new ArrayList<>(0));
            } else if (12 == dataType) {
                record.set("formType", "structure");
                record.set("default_value", new ArrayList<>(0));
            } else if (13 == dataType) {
                record.set("formType", "datetime");
            } else if (14 == dataType) {
                record.set("formType", "email");
            } else if (15 == dataType) {
                record.set("formType", "customer");
            } else if (16 == dataType) {
                record.set("formType", "business");
            } else if (17 == dataType) {
                record.set("formType", "contacts");
            } else if (18 == dataType) {
                record.set("formType", "map_address");
            } else if (19 == dataType) {
                record.set("formType", "category");
            } else if (20 == dataType) {
                record.set("formType", "contract");
            } else if (21 == dataType) {
                record.set("formType", "receivables_plan");
            } else if (22 == dataType) {
                record.set("formType", "business_cause");
            } else if (23 == dataType) {
                record.set("formType", "examine_cause");
            }
            if (3 == dataType || 9 == dataType) {
                if (record.getStr("options") != null) {
                    record.set("setting", record.getStr("options").split(","));
                }
            } else {
                record.set("setting", new String[]{});
            }
        }
    }

    private void recordValueToArray(Record record) {
        record.set("default_value", StrUtil.isNotEmpty(record.get("default_value")) ? record.getStr("default_value").split(",") : new String[]{});
        record.set("value", StrUtil.isNotEmpty(record.getStr("value")) ? record.getStr("value").split(",") : new String[]{});
    }

    /**
     * @author wyq
     * 查询fieldType为0的字段
     */
    public List<Record> customFieldList(String label) {
        //List<Record> recordList = Db.find(Db.getSql("admin.field.customerFieldList"), label);
        try {
            List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.customerFieldList(label));
            recordToFormType(recordList);
            return recordList;
        } catch (Exception e) {
            System.out.println("出错了 " + e);
        }
        return null;
    }

    public List<Record> list(String label) {
        return list(label, null);
    }

    public List<Record> list(String label, String categoryId) {
        StringBuffer sql = new StringBuffer(" SELECT field_id,field_name,name,type,label,remark,input_tips,max_length,default_value,is_unique,is_null,options,operating,update_time,examine_category_id,field_type,relevant FROM lkcrm_admin_field WHERE label=? ");
        List<Object> params = new ArrayList<>();
        params.add(label);
        if (StringUtil.isNotEmpty(categoryId)) {
            params.add(categoryId);
            sql.append(" and examine_category_id= ? ");
        }
        sql.append(" AND cust_id = ? ");
        sql.append(" ORDER BY sorting asc");
        params.add(BaseUtil.getCustId());
        List<Map<String, Object>> maps = crmAdminFieldDao.sqlQuery(sql.toString(), params.toArray());
        List<Record> recordList = JavaBeanUtil.mapToRecords(maps);

        //List<Record> recordList = Db.find(Db.getSqlPara("admin.field.list", Kv.by("label", label).set("categoryId", categoryId)));
        recordToFormType(recordList);
        if (categoryId == null) {
            return recordList;
        }
        FieldUtil fieldUtil = new FieldUtil(recordList);
        return fieldUtil.getRecordList();
    }

    public R setFelidStyle(Kv kv) {
        int type;
        String types = kv.getStr("types");
        switch (types) {
            case "crm_leads":
                type = 1;
                break;
            case "crm_customer":
                type = 2;
                break;
            case "crm_contacts":
                type = 3;
                break;
            case "crm_product":
                type = 4;
                break;
            case "crm_business":
                type = 5;
                break;
            case "crm_contract":
                type = 6;
                break;
            case "crm_receivables":
                type = 7;
                break;
            case "crm_customer_pool":
                type = 8;
                break;
            default:
                type = 0;
                break;
        }
        List<LkCrmAdminFieldStyleEntity> adminFleldStyleList = crmAdminFieldDao.queryFieldStyle(type, kv.getStr("field"), BaseUtil.getUser().getUserId());
        LkCrmAdminFieldStyleEntity adminFleldStyle = null;
        if (adminFleldStyleList.size() > 0) {
            adminFleldStyle = adminFleldStyleList.get(0);
        }
        //AdminFieldStyle adminFleldStyle = AdminFieldStyle.dao.findFirst(AdminFieldStyle.dao.getSql("admin.field.queryFieldStyle"), type, kv.getStr("field"), BaseUtil.getUser().getUserId());
        if (adminFleldStyle != null) {
            adminFleldStyle.setStyle(new BigDecimal(kv.getStr("width")).intValue());
            //adminFleldStyle.update();
            crmAdminFieldDao.saveOrUpdate(adminFleldStyle);
        } else {
            adminFleldStyle = new LkCrmAdminFieldStyleEntity();
            adminFleldStyle.setType(type);
            adminFleldStyle.setCreateTime(DateUtil.date().toTimestamp());
            adminFleldStyle.setStyle(new BigDecimal(kv.getStr("width")).intValue());
            adminFleldStyle.setFieldName(kv.getStr("field"));
            adminFleldStyle.setUserId(BaseUtil.getUser().getUserId());
            //adminFleldStyle.save();
            crmAdminFieldDao.saveOrUpdate(adminFleldStyle);
        }
        return R.ok().put("data", "编辑成功");
    }

    public List<LkCrmAdminFieldStyleEntity> queryFieldStyle(int type) {
        Long userId = BaseUtil.getUser().getUserId();
        return crmAdminFieldDao.find("FROM LkCrmAdminFieldStyleEntity WHERE type = ? and userId = ?", type, userId);
        //return AdminFieldStyle.dao.find("select * from lkcrm_admin_field_style where type = ? and user_id = ?", type, userId);
    }

    /**
     * @author wyq
     * 查询客户管理列表页字段
     */
    public List<Record> queryListHead(LkCrmAdminFieldSortEntity adminFieldSort) {
        //查看userid是否存在于顺序表，没有则插入
        Long userId = BaseUtil.getUser().getUserId();
        Integer number = crmAdminFieldDao.queryForInt("select count(*) from lkcrm_admin_field_sort where user_id = ? and label = ?", userId, adminFieldSort.getLabel());
        if (0 == number) {
            List<Record> fieldList;
            if (adminFieldSort.getLabel() == 8) {
                fieldList = list("2");
            } else {
                fieldList = list(String.valueOf(adminFieldSort.getLabel()));
            }
            List<LkCrmAdminFieldSortEntity> sortList = new LinkedList<>();
            FieldUtil fieldUtil = new FieldUtil(sortList, userId, adminFieldSort.getLabel());
            if (null != fieldList) {
                for (Record record : fieldList) {
                    fieldUtil.add(record.getStr("field_name"), record.getStr("name"), record.getInt("field_id"));
                }
            }
            if (5 == adminFieldSort.getLabel()) {
                fieldUtil.add("typeName", "商机状态组").add("statusName", "商机阶段");
            }
            fieldUtil.add("updateTime", "更新时间").add("createTime", "创建时间")
                    .add("ownerUserName", "负责人").add("createUserName", "创建人");
            fieldUtil.getAdminFieldSortList().forEach(fieldSort -> {
                String fieldName = StrUtil.toCamelCase(fieldSort.getFieldName());
                /*if (11 == adminFieldSort.getLabel()) {
                    fieldName = fieldSort.getFieldName();
                }*/
                fieldSort.setFieldName(fieldName);
                if ("customerId".equals(fieldSort.getFieldName())) {
                    fieldSort.setFieldName("customerName");
                } else if ("categoryId".equals(fieldSort.getFieldName())) {
                    fieldSort.setFieldName("categoryName");
                } else if ("contactsId".equals(fieldSort.getFieldName())) {
                    fieldSort.setFieldName("contactsName");
                } else if ("companyUserId".equals(fieldSort.getFieldName())) {
                    fieldSort.setFieldName("companyUserName");
                } else if ("businessId".equals(fieldSort.getFieldName())) {
                    fieldSort.setFieldName("businessName");
                } else if ("contractId".equals(fieldSort.getFieldName())) {
                    fieldSort.setFieldName("contractNum");
                } else if ("planId".equals(fieldSort.getFieldName())) {
                    fieldSort.setFieldName("planNum");
                }
            });
            sortList = fieldUtil.getAdminFieldSortList();
            int label = adminFieldSort.getLabel();
            HashSet<String> showPri = new HashSet<>(Arrays.asList(SHOW_PRI_SEA_FIELD));
            HashSet<String> showPublic = new HashSet<>(Arrays.asList(SHOW_PUB_SEA_FIELD));
            for (int i = 0; i < sortList.size(); i++) {
                LkCrmAdminFieldSortEntity newUserFieldSort = sortList.get(i);
                newUserFieldSort.setSort(i);
                if (label == 1 && !showPri.contains(sortList.get(i).getName())) {
                    newUserFieldSort.setIsHide(1);
                } else if (label == 11 && !showPublic.contains(sortList.get(i).getName())) {
                    newUserFieldSort.setIsHide(1);
                }
                crmAdminFieldDao.saveOrUpdate(newUserFieldSort);
            }
        }
        String sql = "select field_name as fieldName,name from lkcrm_admin_field_sort where is_hide = 0 and label = ? and user_id = ? order by sort asc";
        List<Map<String, Object>> maps = crmAdminFieldDao.sqlQuery(sql, adminFieldSort.getLabel(), userId);
        return JavaBeanUtil.mapToRecords(maps);
        //return Db.findByCache("field", "listHead:" + adminFieldSort.getLabel() + userId, Db.getSql("admin.field.queryListHead"), adminFieldSort.getLabel(), userId);
    }

    /**
     * @author wyq
     * 查询字段排序隐藏设置
     */
    public R queryFieldConfig(AdminFieldSort adminFieldSort) {
        Long userId = BaseUtil.getUser().getUserId();
        //查出自定义字段，查看顺序表是否存在该字段，没有则插入，设为隐藏
        List<Record> fieldList = customFieldList(adminFieldSort.getLabel().toString());
        for (Record record : fieldList) {
            String fieldName = record.getStr("name");
            Integer number = crmAdminFieldDao.queryForInt("select count(*) as number from lkcrm_admin_field_sort where user_id = ? and label = ? and field_name = ?", userId, adminFieldSort.getLabel(), fieldName);
            if (number.equals(0)) {
                LkCrmAdminFieldSortEntity newField = new LkCrmAdminFieldSortEntity();
                newField.setFieldName(fieldName).setName(fieldName).setLabel(adminFieldSort.getLabel()).setIsHide(1).setUserId(userId).setSort(1);
                //newField.save();
                crmAdminFieldDao.saveOrUpdate(newField);
            }
        }
        List noHideList = crmAdminFieldDao.queryFieldConfig(0, adminFieldSort.getLabel(), userId);
        List hideList = crmAdminFieldDao.queryFieldConfig(1, adminFieldSort.getLabel(), userId);
        return R.ok().put("data", Kv.by("value", noHideList).set("hide_value", hideList).set("hideValue", hideList));
    }

    /**
     * @author wyq
     * 客户管理列表页字段是否隐藏
     */
    @Before(Tx.class)
    public R fieldConfig(AdminFieldSort adminFieldSort) {
        Long userId = BaseUtil.getUser().getUserId();
        String[] sortArr = adminFieldSort.getNoHideIds().split(",");
        if (sortArr.length < 2) {
            return R.error("至少显示2列");
        }
        for (int i = 0; i < sortArr.length; i++) {
            crmAdminFieldDao.executeUpdateSQL("update lkcrm_admin_field_sort set is_hide = 0,sort = ? where label = ? and user_id = ? and id = ?", i + 1, adminFieldSort.getLabel(), userId, sortArr[i]);
            //Db.update(Db.getSql("admin.field.sort"), i + 1, adminFieldSort.getLabel(), userId, sortArr[i]);
        }
        if (null != adminFieldSort.getHideIds()) {
            String[] hideIdsArr = adminFieldSort.getHideIds().split(",");
            for (int i = 0; i < hideIdsArr.length; i++) {
                LkCrmAdminFieldEntity lkCrmAdminFieldEntity = crmAdminFieldDao.get(NumberUtil.parseInt(hideIdsArr[i]));
                if (lkCrmAdminFieldEntity != null) {
                    crmAdminFieldDao.executeUpdateSQL("  update lkcrm_admin_field_sort set is_hide = 1,sort = 0,field_name=?,name=?  where id =? and label = ? and user_id = ?", lkCrmAdminFieldEntity.getFieldName(), lkCrmAdminFieldEntity.getName(), hideIdsArr[i], adminFieldSort.getLabel(), userId);
                } else {
                    crmAdminFieldDao.executeUpdateSQL("  update lkcrm_admin_field_sort set is_hide = 1,sort = 0 where id =? and label = ? and user_id = ?", hideIdsArr[i], adminFieldSort.getLabel(), userId);
                }
            }
            //Db.update(Db.getSqlPara("admin.field.isHide", Kv.by("ids", hideIdsArr).set("label", adminFieldSort.getLabel()).set("userId", userId)));
        }

        if (null != adminFieldSort.getNoHideIds()) {
            String[] hideIdsArr = adminFieldSort.getNoHideIds().split(",");
            for (int i = 0; i < hideIdsArr.length; i++) {
                LkCrmAdminFieldEntity lkCrmAdminFieldEntity = crmAdminFieldDao.get(NumberUtil.parseInt(hideIdsArr[i]));
                if (lkCrmAdminFieldEntity != null) {
                    crmAdminFieldDao.executeUpdateSQL("  update lkcrm_admin_field_sort set field_name=?,name=?  where id =? and label = ? and user_id = ?", lkCrmAdminFieldEntity.getFieldName(), lkCrmAdminFieldEntity.getName(), hideIdsArr[i], adminFieldSort.getLabel(), userId);
                }
            }
            //Db.update(Db.getSqlPara("admin.field.isHide", Kv.by("ids", hideIdsArr).set("label", adminFieldSort.getLabel()).set("userId", userId)));
        }
        CaffeineCache.ME.remove("field", "listHead:" + adminFieldSort.getLabel() + userId);
        return R.ok();
    }


    /**
     * 自定义字段人员和部门转换
     *
     * @param recordList 自定义字段列表
     * @param isDetail   1 value返回name字符串 2 value返回id数组字符串
     * @author hmb
     */
    public void transferFieldList(List<Record> recordList, Integer isDetail) {
        recordList.forEach(record -> {
            Integer dataType = record.getInt("type");
            if (isDetail == 2) {
                if (10 == dataType) {
                    if (StrUtil.isNotEmpty(record.getStr("value"))) {
                        record.set("value", TagUtil.toSet(record.getStr("value")));
                    }
                } else if (12 == dataType) {
                    if (StrUtil.isNotEmpty(record.getStr("value"))) {
                        record.set("value", TagUtil.toSet(record.getStr("value")));
                    }
                }
            } else {
                if (10 == dataType) {
                    if (StrUtil.isNotEmpty(record.getStr("value"))) {
                        record.set("value", crmAdminFieldDao.queryForObject("select group_concat(realname) from `lkcrm_admin_user` where user_id in (" + record.getStr("value") + ")"));
                    }
                } else if (12 == dataType) {
                    if (StrUtil.isNotEmpty(record.getStr("value"))) {
                        record.set("value", crmAdminFieldDao.queryForObject("select group_concat(name) from `lkcrm_admin_dept` where dept_id in (" + record.getStr("value") + ")"));
                    }
                }
            }
            if (dataType == 8) {
                if (StrUtil.isNotEmpty(record.getStr("value"))) {
                    record.set("value", crmAdminFieldDao.sqlQuery("select * from `lkcrm_admin_file` where batch_id = ?", record.getStr("value")));
                }
            }
        });
    }

    /**
     * @description 获取标签信息
     * @method
     * @date: 2019/7/1 11:43
     */
    public Map<String, Object> getLabelInfoById(CustomerLabelDTO customerLabelDTO, LoginUser loginUser) throws Exception {
        Map<String, Object> map = new HashMap();
        String labelName = customerLabelDTO.getLabelName();
        String projectId = customerLabelDTO.getMarketProjectId();
        //根据项目id查询标签信息
        if (StringUtil.isEmpty(labelName)) {
            labelName = "跟进状态";
        }
        //查询自定义状态
        CustomerLabel customerLabel = customerLabelDao.getLabelByProjectId(projectId, loginUser.getCustId(), labelName);
        if (customerLabel != null) {
            map.put("customLabel", customerLabel);
        }
        //查询全局状态
        CustomerLabel allLabel = customerLabelDao.getLabelByProjectId(null, "0", labelName);
        map.put("allLabel", allLabel);
        //查询无效原因标签
        CustomerLabel invalidLabel = customerLabelDao.getLabelByProjectId(null, "0", "无效原因");
        map.put("invalidLabel", invalidLabel);
        return map;
    }
}
