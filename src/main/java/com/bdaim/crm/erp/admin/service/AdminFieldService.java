package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.util.TypeUtils;
import com.bdaim.crm.common.config.cache.CaffeineCache;
import com.bdaim.crm.dao.LkCrmAdminFieldDao;
import com.bdaim.crm.dao.LkCrmAdminFieldvDao;
import com.bdaim.crm.entity.LkCrmAdminFieldEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldSortEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldStyleEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldvEntity;
import com.bdaim.crm.erp.admin.entity.AdminField;
import com.bdaim.crm.erp.admin.entity.AdminFieldSort;
import com.bdaim.crm.utils.*;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
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

    /**
     * @author wyq
     * 查询新增字段列表
     */
    public List<Record> queryAddField(Integer label) {
        List<Map<String, Object>> maps = crmAdminFieldDao.sqlQuery("select field_id,field_name,name,type,input_tips,options,is_unique,is_null,'' as value,field_type from lkcrm_admin_field where label = ? order by sorting", label);
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
        List<Map<String, Object>> maps = crmAdminFieldDao.sqlQuery("select field_id,field_name,name,type,input_tips,options,is_unique,is_null,'' as value,field_type from lkcrm_admin_field where label = ? order by sorting", label);
        List<Record> recordList = JavaBeanUtil.mapToRecords(maps);
        recordList.forEach(r -> {
            if (r.getInt("type") == 10 || r.getInt("type") == 12) {
                r.set("value", crmAdminFieldDao.queryForObject("select value from lkcrm_admin_fieldv where field_id = ? and batch_id = ?", r.getInt("field_id"), record.getStr("batch_id")));
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
     * author zhangzhiwei
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
        List<LkCrmAdminFieldEntity> fieldSorts = crmAdminFieldDao.queryListBySql("select name from lkcrm_admin_field where label = ?", LkCrmAdminFieldEntity.class, label);
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
                crmAdminFieldDao.update(entity);
                if (entity.getFieldType() == 0) {
                    Db.update(Db.getSqlPara("admin.field.updateFieldSortName", entity));
                } else if (entity.getFieldType() == 1) {
                    Db.update("update lkcrm_admin_field_sort set name = ? where field_id = ?", entity.getName(), entity.getFieldId());
                }
            } else {
                //entity.save();
                crmAdminFieldDao.saveOrUpdate(entity);
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
                    "      WHERE a.label=? and a.name=? and b.value=?";
            //SqlPara sqlPara = Db.getSqlPara("admin.field.queryFieldIsExist",kv);
            number = crmAdminFieldDao.queryForInt(sql, kv.get("types"), kv.get("fieldName"), kv.get("val"));
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
                default:
                    return R.error("type不符合要求");
            }
            if (!ParamsUtil.isValid(kv.get("fieldName").toString())) {
                return R.error("参数包含非法字段");
            }
            number = crmAdminFieldDao.queryForInt("select count(*) from lkcrm_crm_" + tableName + " where " + kv.get("fieldName").toString() + " = ? and " + primaryKey + " != ?", kv.get("val").toString(), StringUtil.isNotEmpty(String.valueOf(kv.get("id"))) ? Integer.valueOf(kv.get("id").toString()) : 0);
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
            fieldv.setCreateTime(new Timestamp(System.currentTimeMillis()));
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
            fieldv.setId(null);
            fieldv.setCreateTime(new Timestamp(System.currentTimeMillis()));
            fieldv.setBatchId(batchId);
            crmAdminFieldvDao.save(fieldv);
        });
        return true;
    }

    public synchronized void createView(Integer label) {
        List<Record> fieldNameList = Db.find("select name,type from lkcrm_admin_field WHERE label=? and field_type = 0 ORDER BY sorting asc", label);
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
                filedCreate = String.format(Db.getSql("admin.field.fieldleadsview"), sql, userJoin.append(deptJoin), label);
                create = Db.getSql("admin.field.leadsview");
                break;
            case 2:
                filedCreate = String.format(Db.getSql("admin.field.fieldcustomerview"), sql, userJoin.append(deptJoin), label);
                create = Db.getSql("admin.field.customerview");
                break;
            case 3:
                filedCreate = String.format(Db.getSql("admin.field.fieldcontactsview"), sql, userJoin.append(deptJoin), label);
                create = Db.getSql("admin.field.contactsview");
                break;
            case 4:
                filedCreate = String.format(Db.getSql("admin.field.fieldproductview"), sql, userJoin.append(deptJoin), label);
                create = Db.getSql("admin.field.productview");
                break;
            case 5:
                filedCreate = String.format(Db.getSql("admin.field.fieldbusinessview"), sql, userJoin.append(deptJoin), label);
                create = Db.getSql("admin.field.businessview");
                break;
            case 6:
                filedCreate = String.format(Db.getSql("admin.field.fieldcontractview"), sql, userJoin.append(deptJoin), label);
                create = Db.getSql("admin.field.contractview");
                break;
            case 7:
                filedCreate = String.format(Db.getSql("admin.field.fieldreceivablesview"), sql, userJoin.append(deptJoin), label);
                create = Db.getSql("admin.field.receivablesview");
                break;
            default:
                create = "";
                filedCreate = "";
                break;
        }
        if (StrUtil.isNotBlank(filedCreate)) {
            Db.update(filedCreate);
        }
        if (StrUtil.isNotBlank(create)) {
            Db.update(create);
        }
    }

    public List<Record> queryFieldsByBatchId(String batchId, String... name) {
        if (StrUtil.isEmpty(batchId)) {
            return new ArrayList<>();
        }
        return Db.find(AdminField.dao.getSqlPara("admin.field.queryFieldsByBatchId", Kv.by("batchId", batchId).set("names", name)));
    }

    public List<Record> queryByBatchId(String batchId, Integer label) {
        if (StrUtil.isEmpty(batchId)) {
            return new ArrayList<>();
        }
        List<Record> recordList = Db.find(AdminField.dao.getSqlPara("admin.field.queryFieldsByBatchId", Kv.by("batchId", batchId).set("label", label)));
        recordList.forEach(record -> {
            if (record.getInt("type") == 10) {
                if (StrUtil.isNotEmpty(record.getStr("value"))) {
                    List<Record> userList = Db.find("select user_id,realname from lkcrm_admin_user where user_id in (" + record.getStr("value") + ")");
                    record.set("value", userList);
                } else {
                    record.set("value", new ArrayList<>());
                }
                record.set("default_value", new ArrayList<>(0));
            } else if (record.getInt("type") == 12) {
                if (StrUtil.isNotEmpty(record.getStr("value"))) {
                    List<Record> deptList = Db.find("select dept_id,name from lkcrm_admin_dept where dept_id in (" + record.getStr("value") + ")");
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
        List<Record> records = JavaBeanUtil.mapToRecords(crmAdminFieldDao.queryFields());
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
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.customerFieldList(label));
        recordToFormType(recordList);
        return recordList;
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
        sql.append(" ORDER BY sorting asc");
        List<Map<String, Object>> maps = crmAdminFieldDao.sqlQuery(sql.toString(), params);
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
    @Before(Tx.class)
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
            for (int i = 0; i < sortList.size(); i++) {
                LkCrmAdminFieldSortEntity newUserFieldSort = sortList.get(i);
                newUserFieldSort.setSort(i);
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
    @Before(Tx.class)
    public R queryFieldConfig(AdminFieldSort adminFieldSort) {
        Long userId = BaseUtil.getUser().getUserId();
        //查出自定义字段，查看顺序表是否存在该字段，没有则插入，设为隐藏
        List<Record> fieldList = customFieldList(adminFieldSort.getLabel().toString());
        for (Record record : fieldList) {
            String fieldName = record.getStr("name");
            Integer number = Db.queryInt("select count(*) as number from lkcrm_admin_field_sort where user_id = ? and label = ? and field_name = ?", userId, adminFieldSort.getLabel(), fieldName);
            if (number.equals(0)) {
                AdminFieldSort newField = new AdminFieldSort();
                newField.setFieldName(fieldName).setName(fieldName).setLabel(adminFieldSort.getLabel()).setIsHide(1).setUserId(userId).setSort(1);
                newField.save();
            }
        }
        List<Record> noHideList = Db.find(Db.getSql("admin.field.queryFieldConfig"), 0, adminFieldSort.getLabel(), userId);
        List<Record> hideList = Db.find(Db.getSql("admin.field.queryFieldConfig"), 1, adminFieldSort.getLabel(), userId);
        return R.ok().put("data", Kv.by("value", noHideList).set("hide_value", hideList));
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
            crmAdminFieldDao.executeUpdateSQL("  update lkcrm_admin_field_sort set is_hide = 1,sort = 0 where id in (?) and label = ? and user_id = ?", Arrays.asList(hideIdsArr), adminFieldSort.getLabel(), userId);
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
                        record.set("value", Db.queryStr("select group_concat(realname) from `lkcrm_admin_user` where user_id in (" + record.getStr("value") + ")"));
                    }
                } else if (12 == dataType) {
                    if (StrUtil.isNotEmpty(record.getStr("value"))) {
                        record.set("value", Db.queryStr("select group_concat(name) from `lkcrm_admin_dept` where dept_id in (" + record.getStr("value") + ")"));
                    }
                }
            }
            if (dataType == 8) {
                if (StrUtil.isNotEmpty(record.getStr("value"))) {
                    record.set("value", Db.find("select * from `lkcrm_admin_file` where batch_id = ?", record.getStr("value")));
                }
            }
        });
    }
}
