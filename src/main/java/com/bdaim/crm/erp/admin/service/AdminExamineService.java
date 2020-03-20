package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.LkCrmAdminDeptDao;
import com.bdaim.crm.dao.LkCrmAdminExamineDao;
import com.bdaim.crm.dao.LkCrmAdminExamineStepDao;
import com.bdaim.crm.dao.LkCrmAdminUserDao;
import com.bdaim.crm.entity.LkCrmAdminExamineEntity;
import com.bdaim.crm.entity.LkCrmAdminExamineStepEntity;
import com.bdaim.crm.entity.LkCrmAdminUserEntity;
import com.bdaim.crm.erp.admin.entity.AdminExamine;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class AdminExamineService {

    @Autowired
    private LkCrmAdminExamineDao crmAdminExamineDao;

    @Autowired
    private LkCrmAdminExamineStepDao crmAdminExamineStepDao;

    @Autowired
    private LkCrmAdminUserDao crmAdminUserDao;

    @Autowired
    LkCrmAdminDeptDao crmAdminDeptDao;

    /**
     * 添加审批流程
     */
    public R saveExamine(JSONObject jsonObject) {
        LkCrmAdminExamineEntity adminExamine = jsonObject.toJavaObject(LkCrmAdminExamineEntity.class);
        List<Long> deptIds = jsonObject.getJSONArray("deptIds").toJavaList(Long.class);
        adminExamine.setDeptIds(getIds(deptIds));
        List<Long> userIds = jsonObject.getJSONArray("userIds").toJavaList(Long.class);
        adminExamine.setUserIds(getIds(userIds));
        String custId = BaseUtil.getCustId();
        Boolean flag;
        adminExamine.setCustId(BaseUtil.getCustId());
        if (adminExamine.getExamineId() == null) {
            //添加
            LkCrmAdminExamineEntity examine = crmAdminExamineDao.getExamineByCategoryType(adminExamine.getCategoryType(), custId);
            if (examine != null) {
                //判断有未删除的审批流程，不能添加
                examine.setStatus(0);
                examine.setUpdateUserId(BaseUtil.getUser().getUserId());
                examine.setUpdateTime(DateUtil.date().toTimestamp());
                crmAdminExamineDao.update(examine);
            }
            adminExamine.setCreateUserId(BaseUtil.getUser().getUserId());
            adminExamine.setCreateTime(DateUtil.date().toTimestamp());
            adminExamine.setUpdateUserId(BaseUtil.getUser().getUserId());
            adminExamine.setUpdateTime(DateUtil.date().toTimestamp());
            adminExamine.setStatus(1);
            adminExamine.setCustId(custId);
            flag = (int) crmAdminExamineDao.saveReturnPk(adminExamine) > 0;

        } else {
            //更新 把旧的更新成删除状态 并添加一条新的
            LkCrmAdminExamineEntity examine = crmAdminExamineDao.get(adminExamine.getExamineId());
            examine.setStatus(2);
            crmAdminExamineDao.update(examine);
            adminExamine.setCreateUserId(examine.getCreateUserId());
            adminExamine.setCreateTime(examine.getCreateTime());
            adminExamine.setUpdateUserId(BaseUtil.getUser().getUserId());
            adminExamine.setUpdateTime(DateUtil.date().toTimestamp());
            //adminExamine.setExamineId(null);
            adminExamine.setStatus(1);
            flag = (int) crmAdminExamineDao.saveReturnPk(adminExamine) > 0;


        }
        if (adminExamine.getExamineType() == 1) {
            //如果是固定审批，添加审批步骤
            int i = 1;
            List<JSONObject> jsonArray = jsonObject.getJSONArray("step").toJavaList(JSONObject.class);
            for (JSONObject e : jsonArray) {
                LkCrmAdminExamineStepEntity examineStep = e.toJavaObject(LkCrmAdminExamineStepEntity.class);
                examineStep.setExamineId(adminExamine.getExamineId());
                examineStep.setCreateTime(DateUtil.date().toTimestamp());
                examineStep.setStepNum(i++);
                List<Long> list = e.getJSONArray("checkUserId").toJavaList(Long.class);
                examineStep.setCheckUserId(getIds(list));
                crmAdminExamineStepDao.save(examineStep);
            }
        }
        return flag ? R.ok().put("status", 1) : R.error();

    }

    /**
     * 查询所有启用审批流程
     */
    public R queryAllExamine(BasePageRequest<AdminExamine> basePageRequest) {
        Page page = crmAdminExamineDao.queryExaminePage(basePageRequest.getPage(), basePageRequest.getLimit());
        List<Record> recordList = new ArrayList<>();
        page.getData().forEach(s -> {
            Record record = JavaBeanUtil.mapToRecord((Map<String, Object>) s);
            List<LkCrmAdminExamineStepEntity> data = crmAdminExamineStepDao.queryExamineStepByExamineId(record.getInt("examine_id"));
            List<Map<String, Object>> list = new ArrayList<>();
            data.forEach(d -> list.add(BeanUtil.beanToMap(d, true, false)));
            List<Record> stepList = JavaBeanUtil.mapToRecords(list);
            if (stepList != null) {
                stepList.forEach(step -> {
                    if (step.getStr("check_user_id") != null && step.getStr("check_user_id").split(",").length > 0) {
                        List<Record> userList = JavaBeanUtil.mapToRecords(crmAdminUserDao.queryByIds(Arrays.asList(step.getStr("check_user_id").split(","))));
                        //List<Record> userList = JavaBeanUtil.mapToRecords(customerUserDao.sqlQuery("SELECT * FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(step.getStr("check_user_id").split(",")) + ");"));
                        //List<Record> userList = Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", step.getStr("check_user_id").split(","))));
                        step.set("userList", userList);
                    } else {
                        step.set("userList", new ArrayList<>());
                    }
                });
                record.set("stepList", stepList);
            }
            if (record.getStr("user_ids") != null) {
                int length = record.getStr("user_ids").split(",").length;
                if (record.getStr("user_ids").split(",").length > 0) {
                    List<Record> userList = JavaBeanUtil.mapToRecords(crmAdminUserDao.queryByIds(Arrays.asList(record.getStr("user_ids").split(","))));
                    //List<Record> userList = JavaBeanUtil.mapToRecords(customerUserDao.sqlQuery("SELECT * FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(record.getStr("user_ids").split(",")) + ");"));
                    record.set("userIds", userList);
                }
            } else {
                record.set("userIds", new ArrayList<>());
            }
            if (record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0) {
                // TODO 用户角色权限需要修改
                List<Record> deptList = JavaBeanUtil.mapToRecords(crmAdminDeptDao.queryByIds(Arrays.asList(record.getStr("dept_ids").split(","))));
                //List<Record> deptList = JavaBeanUtil.mapToRecords(customerUserDao.sqlQuery("SELECT * FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(record.getStr("dept_ids").split(",")) + ");"));
                //List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
                record.set("deptIds", deptList);
            } else {
                record.set("deptIds", new ArrayList<>());
            }
            recordList.add(record);
        });
        page.setData(recordList);
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    /**
     * 根据id查询审批流程
     */
    public R queryExamineById(Integer examineId) {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("examine", crmAdminExamineDao.get(examineId));
        List<LkCrmAdminExamineStepEntity> examineSteps = crmAdminExamineStepDao.queryExamineStepByExamineId(examineId);
        //List<AdminExamineStep> examineSteps = AdminExamineStep.dao.find(Db.getSql("admin.examineStep.queryExamineStepByExamineId"), examineId);
        jsonObject.put("step", examineSteps);
        return R.ok().put("data", jsonObject);
    }

    /**
     * 停用或删除审批流程
     */
    public R updateStatus(LkCrmAdminExamineEntity adminExamine) {
        LkCrmAdminExamineEntity dbEntity = crmAdminExamineDao.get(adminExamine.getExamineId());
        adminExamine.setUpdateUserId(BaseUtil.getUser().getUserId());
        adminExamine.setUpdateTime(DateUtil.date().toTimestamp());
        if (adminExamine.getStatus() == null) {
            adminExamine.setStatus(2);
        }
        BeanUtils.copyProperties(adminExamine, dbEntity, JavaBeanUtil.getNullPropertyNames(adminExamine));
        crmAdminExamineDao.update(dbEntity);
        return R.ok();
    }

    private String getIds(List<Long> ids) {
        if (ids == null || ids.size() == 0) {
            return null;
        } else {
            StringBuffer idss = new StringBuffer();
            for (Long id : ids) {
                if (idss.length() == 0) {
                    idss.append(",").append(id).append(",");
                } else {
                    idss.append(id).append(",");
                }
            }
            return idss.toString();
        }
    }

    /**
     * 查询当前启用审核流程步骤
     */
    public R queryExaminStep(Integer categoryType) {
        String custId = BaseUtil.getCustId();
        Record record = JavaBeanUtil.mapToRecord(BeanUtil.beanToMap(crmAdminExamineDao.getExamineByCategoryType(categoryType, custId),
                true, true));
        if (record != null) {
            if (record.getInt("examine_type") == 1) {
                List data = crmAdminExamineStepDao.queryExamineStepByExamineId(record.getInt("examine_id"));
                List<Map<String, Object>> result = new ArrayList<>();
                data.forEach(d -> result.add(BeanUtil.beanToMap(d, true, false)));
                List<Record> list = JavaBeanUtil.mapToRecords(result);
                list.forEach(r -> {
                    //根据审核人id查询审核问信息
                    List<Record> userList = new ArrayList<>();
                    if (r.getStr("check_user_id") != null) {
                        String[] userIds = r.getStr("check_user_id").split(",");
                        for (String userId : userIds) {
                            if (StrUtil.isNotEmpty(userId)) {
                                LkCrmAdminUserEntity lkCrmAdminUserEntity = crmAdminUserDao.get(NumberConvertUtil.parseLong(userId));
                                //CustomerUser customerUser = customerUserDao.get(NumberConvertUtil.parseLong(userId));
                                Record r1 = JavaBeanUtil.mapToRecord(BeanUtil.beanToMap(lkCrmAdminUserEntity));
                                if (r1 != null) {
                                    userList.add(r1);
                                }
                            }
                        }
                    }
                    r.set("userList", userList);
                });
                record.set("examineSteps", list);
            }
            return R.ok().put("data", record);
        }
        return null;
    }
}
