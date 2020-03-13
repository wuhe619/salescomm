package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.entity.LkCrmAdminSceneDefaultEntity;
import com.bdaim.crm.entity.LkCrmAdminSceneEntity;
import com.bdaim.crm.entity.LkCrmCustomerEntity;
import com.bdaim.crm.entity.LkCrmLeadsEntity;
import com.bdaim.crm.erp.admin.entity.AdminScene;
import com.bdaim.crm.erp.crm.service.CrmBusinessService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.FieldUtil;
import com.bdaim.crm.utils.ParamsUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class AdminSceneService {
    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private CrmBusinessService crmBusinessService;

    @Resource
    private LkCrmAdminSceneDao crmAdminSceneDao;

    @Resource
    private LkAdminUserService adminUserService;

    @Resource
    private LkCrmLeadsDao crmLeadsDao;

    @Resource
    private LkCrmCustomerDao crmCustomerDao;

    @Resource
    private LkCrmContactsDao crmContactsDao;

    @Resource
    private LkCrmContractDao crmContractDao;


    /**
     * @author wyq
     * 查询场景字段
     */
    public R queryField(int label) {
        List<Record> recordList = new LinkedList<>();
        FieldUtil fieldUtil = new FieldUtil(recordList);
        String[] settingArr = new String[]{};
        if (1 == label) {
            fieldUtil.add("leads_name", "线索名称", "text", settingArr)
                    .add("telephone", "电话", "text", settingArr)
                    .add("mobile", "手机", "mobile", settingArr)
                    .add("address", "地址", "text", settingArr)
                    .add("next_time", "下次联系时间", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (2 == label) {
            String[] dealStatusArr = new String[]{"未成交", "已成交"};
            fieldUtil.add("customer_name", "客户名称", "text", settingArr)
                    .add("mobile", "手机", "text", settingArr)
                    .add("telephone", "电话", "text", settingArr)
                    .add("website", "网址", "text", settingArr)
                    .add("next_time", "下次联系时间", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("deal_status", "成交状态", "select", dealStatusArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr)
                    .add("address", "地区定位", "map_address", settingArr);
        } else if (3 == label) {
            fieldUtil.add("name", "姓名", "text", settingArr)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("mobile", "手机", "mobile", settingArr)
                    .add("telephone", "电话", "text", settingArr)
                    .add("email", "电子邮箱", "email", settingArr)
                    .add("post", "职务", "text", settingArr)
                    .add("address", "地址", "text", settingArr)
                    .add("next_time", "下次联系时间", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (4 == label) {
            fieldUtil.add("name", "产品名称", "text", settingArr)
                    .add("category_id", "产品类别", "category", settingArr)
                    .add("num", "产品编码", "number", settingArr)
                    .add("price", "价格", "floatnumber", settingArr)
                    .add("description", "产品描述", "text", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (5 == label) {
            fieldUtil.add("business_name", "商机名称", "text", settingArr)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("type_id", "商机状态组", "business_type", crmBusinessService.queryBusinessStatusOptions("condition"))
                    .add("money", "商机金额", "floatnumber", settingArr)
                    .add("deal_date", "预计成交日期", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("product", "产品", "product", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (6 == label) {
            List<Map<String, Object>> checkList = new ArrayList<>();
            checkList.add(new JSONObject().fluentPut("name", "待审核").fluentPut("value", 0));
            checkList.add(new JSONObject().fluentPut("name", "审核中").fluentPut("value", 1));
            checkList.add(new JSONObject().fluentPut("name", "审核通过").fluentPut("value", 2));
            checkList.add(new JSONObject().fluentPut("name", "审核未通过").fluentPut("value", 3));
            checkList.add(new JSONObject().fluentPut("name", "已撤回").fluentPut("value", 4));
            fieldUtil.add("num", "合同编号", "number", settingArr)
                    .add("name", "合同名称", "text", settingArr)
                    .add("check_status", "审核状态", "checkStatus", checkList)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("business_name", "商机名称", "business", settingArr)
                    .add("order_date", "下单时间", "date", settingArr)
                    .add("money", "合同金额", "floatnumber", settingArr)
                    .add("start_time", "合同开始时间", "datetime", settingArr)
                    .add("end_time", "合同结束时间", "datetime", settingArr)
                    .add("contacts_name", "客户签约人", "contacts", settingArr)
                    .add("company_user_id", "公司签约人", "user", settingArr)
                    .add("remark", "备注", "number", settingArr)
                    .add("product", "产品", "product", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (7 == label) {
            List<Map<String, Object>> checkList = new ArrayList<>();
            checkList.add(new JSONObject().fluentPut("name", "待审核").fluentPut("value", 0));
            checkList.add(new JSONObject().fluentPut("name", "审核中").fluentPut("value", 1));
            checkList.add(new JSONObject().fluentPut("name", "审核通过").fluentPut("value", 2));
            checkList.add(new JSONObject().fluentPut("name", "审核未通过").fluentPut("value", 3));
            checkList.add(new JSONObject().fluentPut("name", "已撤回").fluentPut("value", 4));
            fieldUtil.add("number", "回款编号", "number", settingArr)
                    .add("check_status", "审核状态", "checkStatus", checkList)
                    .add("customer_name", "客户名称", "customer", settingArr)
                    .add("contract_num", "合同编号", "contract", settingArr)
                    .add("return_time", "回款日期", "date", settingArr)
                    .add("money", "回款金额", "floatnumber", settingArr)
                    .add("remark", "备注", "textarea", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else if (8 == label) {
            fieldUtil.add("leads_name", "线索名称", "text", settingArr)
                    .add("super_phone", "电话", "text", settingArr)
                    .add("super_telphone", "手机", "mobile", settingArr)
                    .add("super_address_street", "地址", "text", settingArr)
                    .add("next_time", "下次联系时间", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr)

                    .add("user_get_time", "线索领取时间", "datetime", settingArr)
                    .add("sms_success_count", "短信营销数量", "text", settingArr)
                    .add("call_success_count", "电话营销数量", "text", settingArr)
                    .add("email_success_count", "邮件营销数量", "text", settingArr);
        } else if (label == 11) {
            fieldUtil.add("leads_name", "线索名称", "text", settingArr)
                    .add("telephone", "电话", "text", settingArr)
                    .add("mobile", "手机", "mobile", settingArr)
                    .add("address", "地址", "text", settingArr)
                    .add("next_time", "下次联系时间", "datetime", settingArr)
                    .add("remark", "备注", "text", settingArr)
                    .add("owner_user_id", "负责人", "user", settingArr)
                    .add("create_user_id", "创建人", "user", settingArr)
                    .add("update_time", "更新时间", "datetime", settingArr)
                    .add("create_time", "创建时间", "datetime", settingArr);
        } else {
            return R.error("场景label不符合要求！");
        }
        recordList = fieldUtil.getRecordList();
        List<Record> records = adminFieldService.customFieldList(String.valueOf(label));
        if (recordList != null && records != null) {
            for (Record r : records) {
                r.set("field_name", r.getStr("name"));
            }
            recordList.addAll(records);
        }
        return R.ok().put("data", JavaBeanUtil.recordToMap(recordList));
    }

    /**
     * @author wyq
     * 增加场景
     */
    public R addScene(LkCrmAdminSceneEntity adminScene) {
        Long userId = BaseUtil.getUser().getUserId();
        adminScene.setCustId(BaseUtil.getCustId());
        adminScene.setIsHide(0).setSort(99999).setIsSystem(0).setCreateTime(DateUtil.date().toTimestamp()).setUserId(userId);
        crmAdminSceneDao.save(adminScene);
        if (1 == adminScene.getIsDefault()) {
            LkCrmAdminSceneDefaultEntity adminSceneDefault = new LkCrmAdminSceneDefaultEntity();
            adminSceneDefault.setSceneId(adminScene.getSceneId()).setType(adminScene.getType()).setUserId(userId);
            crmAdminSceneDao.saveOrUpdate(adminSceneDefault);
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 更新场景
     */
    public R updateScene(LkCrmAdminSceneEntity adminScene) {
        Long userId = BaseUtil.getUser().getUserId();
        adminScene.setCustId(BaseUtil.getCustId());
        LkCrmAdminSceneEntity oldAdminScene = crmAdminSceneDao.get(adminScene.getSceneId());
        if (1 == adminScene.getIsDefault()) {
            crmAdminSceneDao.executeUpdateSQL("update lkcrm_admin_scene_default set scene_id = ? where user_id = ? and type = ?", adminScene.getSceneId(), userId, oldAdminScene.getType());
        }
        adminScene.setUserId(userId).setType(oldAdminScene.getType()).setSort(oldAdminScene.getSort()).setIsSystem(oldAdminScene.getIsSystem()).setUpdateTime(DateUtil.date().toTimestamp());
        BeanUtils.copyProperties(adminScene, oldAdminScene, JavaBeanUtil.getNullPropertyNames(adminScene));
        crmAdminSceneDao.update(oldAdminScene);
        return R.isSuccess(true);
    }

    /**
     * @author wyq
     * 设置默认场景
     */
    public R setDefaultScene(Integer sceneId) {
        Long userId = BaseUtil.getUser().getUserId();
        LkCrmAdminSceneEntity oldAdminScene = crmAdminSceneDao.get(sceneId);
        crmAdminSceneDao.executeUpdateSQL("delete from lkcrm_admin_scene_default where user_id = ? and type = ?", userId, oldAdminScene.getType());
        LkCrmAdminSceneDefaultEntity adminSceneDefault = new LkCrmAdminSceneDefaultEntity();
        adminSceneDefault.setSceneId(sceneId).setType(oldAdminScene.getType()).setUserId(userId);
        adminSceneDefault.setCustId(BaseUtil.getCustId());
        crmAdminSceneDao.saveOrUpdate(adminSceneDefault);
        return R.ok();
    }

    /**
     * @author wyq
     * 删除场景
     */
    public R deleteScene(AdminScene adminScene) {
        if (1 == crmAdminSceneDao.get(adminScene.getSceneId()).getIsSystem()) {
            return R.error("系统场景不能删除");
        }
        crmAdminSceneDao.delete(adminScene.getSceneId());
        return R.ok();
    }

    /**
     * @author wyq
     * 查询场景
     */
    public R queryScene(Integer type) {
        Long userId = BaseUtil.getUser().getUserId();
        //查询userId下是否有系统场景，没有则插入
        String sql = "select count(*) from lkcrm_admin_scene where is_system = 1 and type = ? and user_id = ? AND cust_id = ?";
        //Integer number = Db.queryInt(Db.getSql("admin.scene.querySystemNumber"), type, userId);
        int number = crmAdminSceneDao.queryForInt(sql, type, userId, BaseUtil.getCustId());
        type = type != null ? type : -1;
        if (number == 0) {
            //AdminScene systemScene = new AdminScene();
            LkCrmAdminSceneEntity systemScene = new LkCrmAdminSceneEntity();
            systemScene.setCustId(BaseUtil.getCustId());
            systemScene.setUserId(userId).setSort(0).setData("").setIsHide(0).setIsSystem(1).setCreateTime(new Timestamp(System.currentTimeMillis())).setType(type);
            JSONObject ownerObject = new JSONObject();
            ownerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "is").fluentPut("value", userId));
            JSONObject subOwnerObject = new JSONObject();
            subOwnerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "in").fluentPut("value", getSubUserId(userId.intValue(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1)));
            if (1 == type) {
                systemScene.setName("全部线索").setData(new JSONObject().fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", 0)).toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                ownerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "is").fluentPut("value", userId)).fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", 0));
                systemScene.setSceneId(null).setName("我负责的线索").setData(ownerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                subOwnerObject.fluentPut("owner_user_id", new JSONObject().fluentPut("name", "owner_user_id").fluentPut("condition", "in").fluentPut("value", getSubUserId(userId.intValue(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1))).fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", 0));
                systemScene.setSceneId(null).setName("下属负责的线索").setData(subOwnerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", "1"));
                systemScene.setSceneId(null).setName("已转化的线索").setData(jsonObject.toString()).setBydata("transform");
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
            } else if (2 == type) {
                systemScene.setName("全部客户");
                systemScene.setSceneId(null).setName("我负责的客户").setData(ownerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                systemScene.setSceneId(null).setName("下属负责的客户").setData(subOwnerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("ro_user_id", new JSONObject().fluentPut("name", "ro_user_id").fluentPut("condition", "takePart").fluentPut("value", userId));
                systemScene.setSceneId(null).setName("我参与的客户").setData(jsonObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
            } else if (3 == type) {
                systemScene.setName("全部联系人");
                systemScene.setSceneId(null).setName("我负责的联系人").setData(ownerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                systemScene.setSceneId(null).setName("下属负责的联系人").setData(subOwnerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
            } else if (4 == type) {
                systemScene.setName("全部产品");
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                systemScene.setName("上架的产品").setData(new JSONObject().fluentPut("是否上下架", new JSONObject().fluentPut("name", "是否上下架").fluentPut("condition", "is").fluentPut("value", "上架")).toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("是否上下架", new JSONObject().fluentPut("name", "是否上下架").fluentPut("condition", "is").fluentPut("value", "下架"));
                systemScene.setSceneId(null).setName("下架的产品").setData(jsonObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
            } else if (5 == type) {
                systemScene.setName("全部商机");
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                systemScene.setSceneId(null).setName("我负责的商机").setData(ownerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                systemScene.setSceneId(null).setName("下属负责的商机").setData(subOwnerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("ro_user_id", new JSONObject().fluentPut("name", "ro_user_id").fluentPut("condition", "takePart").fluentPut("value", userId));
                systemScene.setSceneId(null).setName("我参与的商机").setData(jsonObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
            } else if (6 == type) {
                systemScene.setName("全部合同");
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                systemScene.setSceneId(null).setName("我负责的合同").setData(ownerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                systemScene.setSceneId(null).setName("下属负责的合同").setData(subOwnerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                JSONObject jsonObject = new JSONObject();
                jsonObject.fluentPut("ro_user_id", new JSONObject().fluentPut("name", "ro_user_id").fluentPut("condition", "takePart").fluentPut("value", userId));
                systemScene.setSceneId(null).setName("我参与的合同").setData(jsonObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
            } else if (7 == type) {
                systemScene.setName("全部回款");
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                systemScene.setSceneId(null).setName("我负责的回款").setData(ownerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
                systemScene.setSceneId(null).setName("下属负责的回款").setData(subOwnerObject.toString());
                crmAdminSceneDao.getSession().clear();
                crmAdminSceneDao.save(systemScene);
            }
            //crmAdminSceneDao.save(systemScene);
        }
        sql = "select a.scene_id,a.data,a.name,if(b.default_id is null,0,1) as is_default,a.is_system,a.bydata " +
                "    from lkcrm_admin_scene as a left join lkcrm_admin_scene_default as b on a.scene_id = b.scene_id " +
                "    where a.type = ? and a.user_id = ? and is_hide = 0 AND a.cust_id = ?  order by a.sort asc";
        //return R.ok().put("data", Db.find(Db.getSql("admin.scene.queryScene"), type, userId));
        return R.ok().put("data", crmAdminSceneDao.sqlQuery(sql, type, userId, BaseUtil.getCustId()));
    }

    /**
     * 递归查询下属id
     */
    public String getSubUserId(Integer userId, Integer deepness) {
        StringBuilder ids = new StringBuilder();
        if (deepness > 0) {
            List<Long> list = new ArrayList<>();
            List<Map<String, Object>> data = crmAdminSceneDao.sqlQuery("select user_id from lkcrm_admin_user where parent_id = ?", userId);
            data.forEach(s -> list.add(NumberConvertUtil.parseLong(s.get("user_id"))));
            if (list != null && list.size() > 0) {
                for (Long l : list) {
                    ids.append(",").append(l).append(getSubUserId(l.intValue(), deepness - 1));
                }
            }
        }
        return StrUtil.isNotEmpty(ids.toString()) ? ids.toString() : " ";
    }

    /**
     * @author wyq
     * 查询场景设置
     */
    public R querySceneConfig(AdminScene adminScene) {
        Long userId = BaseUtil.getUser().getUserId();
        List<Record> valueList = JavaBeanUtil.mapToRecords(crmAdminSceneDao.queryScene(adminScene.getType(), userId));
        //List<Record> valueList = Db.find(Db.getSql("admin.scene.queryScene"), adminScene.getType(), userId);
        for (Record scene : valueList) {
            if (StrUtil.isNotEmpty(scene.getStr("data"))) {
                JSONObject jsonObject = JSON.parseObject(scene.getStr("data"));
                scene.set("data", jsonObject);
            }
        }
        //List<Record> hideValueList = Db.find(Db.getSql("admin.scene.queryHideScene"), adminScene.getType(), userId);
        List<Record> hideValueList = JavaBeanUtil.mapToRecords(crmAdminSceneDao.queryHideScene(adminScene.getType(), userId));
        for (Record hideScene : hideValueList) {
            if (StrUtil.isNotEmpty(hideScene.getStr("data"))) {
                JSONObject jsonObject = JSON.parseObject(hideScene.getStr("data"));
                hideScene.set("data", jsonObject);
            }
        }
        return R.ok().put("data", Kv.by("value", JavaBeanUtil.recordToMap(valueList)).set("hide_value", JavaBeanUtil.recordToMap(hideValueList)));
    }

    /**
     * @author wyq
     * 设置场景
     */
    public R sceneConfig(AdminScene adminScene) {
        Long userId = BaseUtil.getUser().getUserId();
        String[] sortArr = adminScene.getNoHideIds().split(",");
        for (int i = 0; i < sortArr.length; i++) {
            crmAdminSceneDao.executeUpdateSQL("update lkcrm_admin_scene set is_hide = 0,sort = ? where type = ? and user_id = ? and scene_id = ?", i + 1, adminScene.getType(), userId, sortArr[i]);
            //Db.update(Db.getSql("admin.scene.sort"), i + 1, adminScene.getType(), userId, sortArr[i]);
        }
        if (null != adminScene.getHideIds()) {
            String[] hideIdsArr = adminScene.getHideIds().split(",");
            Record number = JavaBeanUtil.mapToRecord(crmAdminSceneDao.queryIsHideSystem(Arrays.asList(hideIdsArr)));
            if (number.getInt("number") > 0) {
                return R.error("系统场景不能隐藏");
            }
            crmAdminSceneDao.executeUpdateSQL(" update lkcrm_admin_scene set is_hide = 1,sort = 0 where scene_id in (?) and type = ? and user_id = ? ", hideIdsArr, adminScene.getType(), userId);
            //Db.update(Db.getSqlPara("admin.scene.isHide", Kv.by("ids", hideIdsArr).set("type", adminScene.getType()).set("userId", userId)));
        }
        return R.ok();
    }

    public R filterConditionAndGetPageList(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer sceneId = jsonObject.getInteger("sceneId");
        JSONObject data = new JSONObject();
        if (sceneId != null && sceneId != 0) {
            LkCrmAdminSceneEntity lkCrmAdminSceneEntity = crmAdminSceneDao.get(sceneId);
            if (lkCrmAdminSceneEntity != null) {
                data = JSON.parseObject(lkCrmAdminSceneEntity.getData());
            }
            //data = JSON.parseObject(AdminScene.dao.findById(sceneId).getData());
        }
        if (sceneId == null && jsonObject.getInteger("type") == 1) {
            data = new JSONObject().fluentPut("is_transform", new JSONObject().fluentPut("name", "is_transform").fluentPut("condition", "is").fluentPut("value", "0"));
        }
        if (sceneId == null && jsonObject.getInteger("type") == 4) {
            data = new JSONObject().fluentPut("是否上下架", new JSONObject().fluentPut("name", "是否上下架").fluentPut("condition", "is").fluentPut("value", "上架"));
        }
        if (jsonObject.getJSONObject("data") != null) {
            if (data != null) {
                jsonObject.getJSONObject("data").putAll(data);
            }
        } else {
            jsonObject.put("data", data);
        }
        basePageRequest.setJsonObject(jsonObject);
        return getCrmPageList(basePageRequest);
    }

    /**
     * @author wyq
     * Crm列表页查询
     */
    public R getCrmPageList(BasePageRequest basePageRequest) {
        Integer type = basePageRequest.getJsonObject().getInteger("type");
        String viewName;
        //操作地址
        String realm;
        switch (type) {
            case 1:
                viewName = "leadsview";
                viewName = BaseUtil.getViewSql("leadsview");
                realm = "leads";
                break;
            case 2:
                viewName = "customerview";
                viewName = BaseUtil.getViewSql("customerview");
                realm = "customer";
                break;
            case 3:
                viewName = "contactsview";
                viewName = BaseUtil.getViewSql("contactsview");
                realm = "contacts";
                break;
            case 4:
                viewName = "productview";
                viewName = BaseUtil.getViewSql("productview");
                realm = "product";
                break;
            case 5:
                viewName = "businessview";
                viewName = BaseUtil.getViewSql("businessview");
                realm = "business";
                break;
            case 6:
                viewName = "contractview";
                viewName = BaseUtil.getViewSql("contractview");
                realm = "contract";
                break;
            case 7:
                viewName = "receivablesview";
                viewName = BaseUtil.getViewSql("receivablesview");
                realm = "receivables";
                break;
            case 8:
                viewName = "customerview";
                viewName = BaseUtil.getViewSql("customerview");
                realm = "customer";
                break;
            default:
                return R.error("type不符合要求");
        }
        JSONObject data = basePageRequest.getJsonObject().getJSONObject("data");
        List<JSONObject> jsonObjectList = new ArrayList<>();
        if (data != null) {
            data.forEach((k, v) -> jsonObjectList.add(JSON.parseObject(v.toString())));
        }
        StringBuilder conditions = new StringBuilder(" where 1=1");
        for (JSONObject jsonObject : jsonObjectList) {
            String condition = jsonObject.getString("condition");
            String value = jsonObject.getString("value");
            String name = jsonObject.getString("name");
            if (!ParamsUtil.isValid(name)) {
                return R.error("参数包含非法字段");
            }
            if (StrUtil.isNotEmpty(value) && !ParamsUtil.isValid(value)) {
                return R.error("参数包含非法字段");
            }
            if (StrUtil.isNotEmpty(jsonObject.getString("start")) && !ParamsUtil.isValid(jsonObject.getString("start"))) {
                return R.error("参数包含非法字段");
            }
            if (StrUtil.isNotEmpty(jsonObject.getString("end")) && !ParamsUtil.isValid(jsonObject.getString("end"))) {
                return R.error("参数包含非法字段");
            }
            String formType = jsonObject.getString("formType");
            if ("business_type".equals(formType)) {
                conditions.append(" and ").append(name).append(" = ").append(jsonObject.getInteger("typeId"));
                if (StrUtil.isNotEmpty(jsonObject.getString("statusId"))) {
                    if ("win".equals(jsonObject.getString("statusId"))) {
                        conditions.append(" and is_end = 1");
                    } else if ("lose".equals(jsonObject.getString("statusId"))) {
                        conditions.append(" and is_end = 2");
                    } else if ("invalid".equals(jsonObject.getString("statusId"))) {
                        conditions.append(" and is_end = 3");
                    } else {
                        conditions.append(" and status_id = ").append(jsonObject.getString("statusId"));
                    }
                }
                continue;
            } else if ("map_address".equals(formType)) {
                String address = value.substring(0, value.length() - 1);
                conditions.append(" and ").append(name).append(" like '%").append(address).append("%'");
                continue;
            }
            if (StrUtil.isNotEmpty(value) || StrUtil.isNotEmpty(jsonObject.getString("start")) || StrUtil.isNotEmpty(jsonObject.getString("end"))) {
                if ("takePart".equals(condition)) {
                    conditions.append(" and (ro_user_id like '%,").append(value).append(",%' or rw_user_id like '%,").append(value).append(",%')");
                } else {
                    conditions.append(" and ").append(jsonObject.getString("name"));
                    if ("is".equals(condition)) {
                        conditions.append(" = '").append(value).append("'");
                    } else if ("isNot".equals(condition)) {
                        conditions.append(" != '").append(value).append("'");
                    } else if ("contains".equals(condition)) {
                        conditions.append(" like '%").append(value).append("%'");
                    } else if ("notContains".equals(condition)) {
                        conditions.append(" not like '%").append(value).append("%'");
                    } else if ("isNull".equals(condition)) {
                        conditions.append(" is null");
                    } else if ("isNotNull".equals(condition)) {
                        conditions.append(" is not null");
                    } else if ("gt".equals(condition)) {
                        conditions.append(" > ").append(value);
                    } else if ("egt".equals(condition)) {
                        conditions.append(" >= ").append(value);
                    } else if ("lt".equals(condition)) {
                        conditions.append(" < ").append(value);
                    } else if ("elt".equals(condition)) {
                        conditions.append(" <= ").append(value);
                    } else if ("in".equals(condition)) {
                        conditions.append(" in (").append(value).append(")");
                    }
                    if ("datetime".equals(formType)) {
                        conditions.append(" between '").append(jsonObject.getString("start")).append("' and '").append(jsonObject.getString("end")).append("'");
                    }
                    if ("date".equals(formType)) {
                        conditions.append(" between '").append(jsonObject.getString("startDate")).append("' and '").append(jsonObject.getString("endDate")).append("'");
                    }
                }
            }
        }
        String search = basePageRequest.getJsonObject().getString("search");
        if (StrUtil.isNotEmpty(search)) {
            if (!ParamsUtil.isValid(search)) {
                return R.error("参数包含非法字段");
            }
            if (type == 1) {
                conditions.append(" and (leads_name like '%").append(search).append("%' or telephone like '%")
                        .append(search).append("%' or mobile like '%").append(search).append("%')");
            } else if (type == 2 || type == 8) {
                conditions.append(" and (customer_name like '%").append(search).append("%' or telephone like '%")
                        .append(search).append("%')");
            } else if (type == 3) {
                conditions.append(" and (name like '%").append(search).append("%' or telephone like '%")
                        .append(search).append("%' or mobile like '%").append(search).append("%')");
            } else if (type == 4 || type == 6) {
                conditions.append(" and (name like '%").append(search).append("%')");
            } else if (type == 5) {
                conditions.append(" and (business_name like '%").append(search).append("%')");
            } else {
                conditions.append(" and (number like '%").append(search).append("%')");
            }
        }

        conditions.append(" and cust_id= ? ");

        String camelField = basePageRequest.getJsonObject().getString("sortField");
        String sortField = StrUtil.toUnderlineCase(camelField);
        String orderNum = basePageRequest.getJsonObject().getString("order");
        if (StrUtil.isEmpty(sortField) || StrUtil.isEmpty(orderNum)) {
            sortField = "update_time";
            orderNum = "desc";
        } else {
            if (!ParamsUtil.isValid(sortField)) {
                return R.error("参数包含非法字段");
            }
            orderNum = "2".equals(orderNum) ? "asc" : "desc";
        }
        if (2 == type) {
            conditions.append(" and owner_user_id is not null");
        } else if (8 == type) {
            conditions.append(" and owner_user_id is null");
        }
        Long userId = BaseUtil.getUserId();
        if (!type.equals(8) && !type.equals(4) && !BaseConstant.SUPER_ADMIN_USER_ID.equals(userId)) {
            List<Long> longs = adminUserService.queryUserByAuth(userId, realm);
            if (longs != null && longs.size() > 0) {
                conditions.append(" and owner_user_id in (").append(StrUtil.join(",", longs)).append(")");
                if (type.equals(2) || type.equals(6) || type.equals(5)) {
                    conditions.append(" or ro_user_id like CONCAT('%,','").append(userId).append("',',%')").append(" or rw_user_id like CONCAT('%,','").append(userId).append("',',%')");
                }
            }
        }
        conditions.insert(0, " from " + viewName);
        //conditions.append(" order by ").append(viewName).append(".").append(sortField).append(" ").append(orderNum);
        conditions.append(" order by ").append(sortField).append(" ").append(orderNum);
        if (StrUtil.isNotEmpty(basePageRequest.getJsonObject().getString("excel"))) {
            return R.ok().put("excel", JavaBeanUtil.mapToRecords(crmAdminSceneDao.sqlQuery("select * " + conditions.toString(), BaseUtil.getUser().getCustId())));
        }
        if (2 == type || 8 == type) {
            Integer configType = crmAdminSceneDao.queryForInt("select status from lkcrm_admin_config where name = 'customerPoolSetting' AND cust_id = ? ", BaseUtil.getCustId());
            if (1 == configType && 2 == type) {
                String customerview = BaseUtil.getViewSql("customerview");
                String sql = " select *,(TO_DAYS(IFNULL((SELECT car.create_time FROM lkcrm_admin_record as car where car.types = 'crm_customer' and car.types_id = " + customerview + ".customer_id ORDER BY car.create_time DESC LIMIT 1),create_time))\n" +
                        "      + CAST((SELECT value FROM lkcrm_admin_config WHERE name= 'customerPoolSettingFollowupDays') as SIGNED) - TO_DAYS(NOW())\n" +
                        "    ) as pool_day," +
                        "    (select count(*) from lkcrm_crm_business as a where a.customer_id = " + customerview + ".customer_id) as business_count";

                return R.ok().put("data", BaseUtil.crmPage(crmAdminSceneDao.sqlPageQuery(sql + conditions.toString(), basePageRequest.getPage(), basePageRequest.getLimit(), BaseUtil.getUser().getCustId())));
            } else {
                return R.ok().put("data", BaseUtil.crmPage(crmAdminSceneDao.sqlPageQuery("select *,(select count(*) from lkcrm_crm_business as a where a.customer_id = " + viewName + ".customer_id) as business_count " + conditions.toString(), basePageRequest.getPage(), basePageRequest.getLimit(), BaseUtil.getUser().getCustId())));
            }

        } else if (6 == type) {
            Record totalMoney = JavaBeanUtil.mapToRecord(crmAdminSceneDao.sqlQuery("select SUM(money) as contractMoney,GROUP_CONCAT(contract_id) as contractIds " + conditions.toString(), BaseUtil.getUser().getCustId()).get(0));
            Page page = crmAdminSceneDao.sqlPageQuery("select *,IFNULL((select SUM(a.money) from lkcrm_crm_receivables as a where a.contract_id = contractview.contract_id),0) as receivedMoney" + conditions.toString(), basePageRequest.getPage(), basePageRequest.getLimit(), BaseUtil.getUser().getCustId());

            String receivedMoney = crmAdminSceneDao.queryForObject("select SUM(money) from lkcrm_crm_receivables where receivables_id in (" + totalMoney.getStr("contractIds") + ")");
            JSONObject jsonObject = JSONObject.parseObject(JSON.toJSONString(BaseUtil.crmPage(page)), JSONObject.class);
            return R.ok().put("data", jsonObject.fluentPut("money", new JSONObject().fluentPut("contractMoney", totalMoney.getStr("contractMoney") != null ? totalMoney.getStr("contractMoney") : "0").fluentPut("receivedMoney", StringUtil.isNotEmpty(receivedMoney) ? receivedMoney : "0")));
        }
        com.bdaim.common.dto.Page recordPage = crmAdminSceneDao.sqlPageQuery("select *" + conditions.toString(), basePageRequest.getPage(), basePageRequest.getLimit(), BaseUtil.getUser().getCustId());
        if (type == 5) {
            recordPage.getData().forEach(record -> {
                Map map = (Map) record;
                if (NumberConvertUtil.parseInt(map.get("is_end")) == 1) {
                    map.put("status_name", "赢单");
                } else if (NumberConvertUtil.parseInt(map.get("is_end")) == 2) {
                    map.put("status_name", "输单");
                } else if (NumberConvertUtil.parseInt(map.get("is_end")) == 3) {
                    map.put("status_name", "无效");
                }
            });
            setBusinessStatus(JavaBeanUtil.mapToRecords(recordPage.getData()));
        }
      /*  com.jfinal.plugin.activerecord.Page finalPage = new com.jfinal.plugin.activerecord.Page();
        finalPage.setList(recordPage.getData());
        finalPage.setTotalRow(recordPage.getTotal());*/
        Map map;
        for (int i = 0; i < recordPage.getData().size(); i++) {
            map = (Map) recordPage.getData().get(i);
            handleCompany(type, map);
        }
        return R.ok().put("data", BaseUtil.crmPage(recordPage));
    }

    private void handleCompany(int type, Map map) {
        int id;
        String viewName, realm;
        switch (type) {
            case 1:
                id = NumberConvertUtil.parseInt(map.get("leads_id"));
                LkCrmLeadsEntity lkCrmLeadsEntity = crmLeadsDao.get(id);
                if (lkCrmLeadsEntity != null) {
                    map.put("company", lkCrmLeadsEntity.getCompany());
                    map.put("公司名称", lkCrmLeadsEntity.getCompany());
                }
                break;
            case 2:
                id = NumberConvertUtil.parseInt(map.get("customer_id"));
                LkCrmCustomerEntity lkCrmCustomerEntity = crmCustomerDao.get(id);
                if (lkCrmCustomerEntity != null) {
                    map.put("company", lkCrmCustomerEntity.getCompany());
                    map.put("公司名称", lkCrmCustomerEntity.getCompany());
                }
                break;
            case 3:
                /*id = NumberConvertUtil.parseInt(map.get("customer_id"));
                LkCrmContactsEntity lkCrmContactsEntity = crmContactsDao.get(id);
                if (lkCrmContactsEntity != null) {
                    map.put("company", lkCrmContactsEntity.getCompany());
                }
                viewName = "contractview";
                realm = "contract";*/
                break;
            case 8:
                id = NumberConvertUtil.parseInt(map.get("customer_id"));
                lkCrmCustomerEntity = crmCustomerDao.get(id);
                if (lkCrmCustomerEntity != null) {
                    map.put("company", lkCrmCustomerEntity.getCompany());
                }
                break;
        }
    }

    public void setBusinessStatus(List<Record> list) {
        list.forEach(record -> {
            if (record.getInt("is_end") == 0) {
                Integer sortNum = crmAdminSceneDao.queryForInt("select order_num from lkcrm_crm_business_status where status_id = ?", record.getInt("status_id"));
                int totalStatsNum = crmAdminSceneDao.queryForInt("select count(*) from lkcrm_crm_business_status where type_id = ?", record.getInt("type_id")) + 1;
                if (sortNum == null) {
                    sortNum = 0;
                }
                record.set("progressBar", sortNum + "/" + totalStatsNum);
            } else if (record.getInt("is_end") == 1) {
                int totalStatsNum = crmAdminSceneDao.queryForInt("select count(*) from lkcrm_crm_business_status where type_id = ?", record.getInt("type_id")) + 1;
                record.set("progressBar", totalStatsNum + "/" + totalStatsNum);
            } else if (record.getInt("is_end") == 2) {
                int totalStatsNum = crmAdminSceneDao.queryForInt("select count(*) from lkcrm_crm_business_status where type_id = ?", record.getInt("type_id")) + 1;
                record.set("progressBar", "0/" + totalStatsNum);
            } else if (record.getInt("is_end") == 3) {
                record.set("progressBar", "0/0");
            }
        });
    }
}
