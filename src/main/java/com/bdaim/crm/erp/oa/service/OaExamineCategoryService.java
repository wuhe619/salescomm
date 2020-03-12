package com.bdaim.crm.erp.oa.service;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.*;
import com.bdaim.crm.entity.LkCrmAdminFieldEntity;
import com.bdaim.crm.entity.LkCrmOaExamineCategoryEntity;
import com.bdaim.crm.entity.LkCrmOaExamineStepEntity;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.FieldUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;

@Service
@Transactional
public class OaExamineCategoryService {

    @Resource
    private FieldUtil fieldUtil;

    @Resource
    private AdminFieldService adminFieldService;

    @Autowired
    private LkCrmOaExamineCategoryDao categoryDao;
    @Autowired
    private LkCrmAdminFieldDao fieldDao;
    @Autowired
    private LkCrmOaExamineStepDao stepDao;
    @Autowired
    private LkCrmAdminUserDao crmAdminUserDao;
    @Autowired
    private LkCrmAdminDeptDao crmAdminDeptDao;

    @Before(Tx.class)
    public R setExamineCategory(LkCrmOaExamineCategoryEntity oaExamineCategory, List<LkCrmOaExamineStepEntity> examineStepList) {
        boolean bol;
        Integer categoryId;
        if (oaExamineCategory.getCategoryId() == null) {
            oaExamineCategory.setCreateUserId(BaseUtil.getUser().getUserId());
            oaExamineCategory.setCreateTime(new Timestamp(System.currentTimeMillis()));
            oaExamineCategory.setUpdateTime(new Timestamp(System.currentTimeMillis()));
//            bol = oaExamineCategory.save();
            categoryDao.save(oaExamineCategory);
            categoryId = oaExamineCategory.getCategoryId();
            LkCrmAdminFieldEntity content = new LkCrmAdminFieldEntity();
            content.setName("审批事由");
            content.setFieldName("content");
            content.setMaxLength(0);
            content.setType(2);
            content.setLabel(10);
            content.setIsNull(1);
            content.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            content.setOperating(1);
            content.setFieldType(1);
            content.setExamineCategoryId(categoryId);
//            content.save();
            fieldDao.save(content);
            content.setFieldId(null);
            content.setFieldName("remark");
            content.setIsNull(0);
            content.setName("备注");
//            content.save();
            fieldDao.save(content);
        } else {
            oaExamineCategory.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            categoryId = oaExamineCategory.getCategoryId();
//            bol = oaExamineCategory.update();
            categoryDao.save(oaExamineCategory);
        }
        //设置审批步骤
        if (oaExamineCategory.getExamineType() == 1) {
            if (examineStepList.size() != 0) {
                String delSql = "delete from lkcrm_oa_examine_step where category_id = ?";
                categoryDao.executeUpdateSQL(delSql, categoryId);
                examineStepList.forEach(x -> {
                    x.setCategoryId(categoryId);
                    stepDao.save(x);
                });
            }
        }
        return R.ok().put("data", Kv.by("categoryId", oaExamineCategory.getCategoryId()));
    }

    public R queryExamineCategoryList(BasePageRequest basePageRequest) {
        //Page<Record> paginate = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select * ", "from lkcrm_oa_examine_category where is_deleted = 0 AND cust_id = ? ");
        Page paginate = categoryDao.sqlPageQuery("select * from lkcrm_oa_examine_category where is_deleted = 0 AND cust_id = ? ", basePageRequest.getPage(), basePageRequest.getLimit(), BaseUtil.getCustId());
        paginate.getData().forEach(m -> {
                    Record record = JavaBeanUtil.mapToRecord((Map<String, Object>) m);
                    String stepListSql = "select * from lkcrm_oa_examine_step where category_id = ?";
                    List<Record> stepList = JavaBeanUtil.mapToRecords(categoryDao.queryListBySql(stepListSql,
                            record.getStr("category_id")));
                    stepList.forEach(step -> {
                        if (step.getStr("check_user_id") != null && step.getStr("check_user_id").split(",").length > 0) {
                            List userList = crmAdminUserDao.queryByIds(Arrays.asList(step.getStr("check_user_id").split(",")));
                            //List<Record> userList = Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", step.getStr("check_user_id").split(","))));
                            step.set("userList", userList);
                        } else {
                            step.set("userList", new ArrayList<>());
                        }
                    });
                    record.set("stepList", stepList);
                    if (record.getStr("user_ids") != null && record.getStr("user_ids").split(",").length > 0) {
                        //List<Record> userList = Db.find(Db.getSqlPara("admin.user.queryByIds", Kv.by("ids", record.getStr("user_ids").split(","))));
                        List userList = crmAdminUserDao.queryByIds(Arrays.asList(record.getStr("user_ids").split(",")));
                        record.set("userIds", userList);
                    } else {
                        record.set("userIds", new ArrayList<>());
                    }
                    if (record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0) {
                        //List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
                        List deptList = crmAdminDeptDao.queryByIds(Arrays.asList(record.getStr("dept_ids").split(",")));
                        record.set("deptIds", deptList);
                    } else {
                        record.set("deptIds", new ArrayList<>());
                    }
                }
        );
        return R.ok().put("data", paginate);
    }

    public R deleteExamineCategory(String id) {
        String updateSql = "update lkcrm_oa_examine_category set is_deleted = 1,delete_user_id = ?,delete_time = now() where category_id = ?";
        int update = categoryDao.executeUpdateSQL(updateSql, BaseUtil.getUser().getUserId(), id);
        return update > 0 ? R.ok() : R.error();
    }


    public R queryUserList() {
        String sql = "select a.user_id,a.realname,a.username,a.img,b.name as deptName from lkcrm_admin_user a " +
                "left join lkcrm_admin_dept b on a.dept_id = b.dept_id";
        List<Record> recordList = JavaBeanUtil.mapToRecords(categoryDao.queryListBySql(sql));
        return R.ok().put("data", recordList);
    }

    public R queryDeptList() {
        String listSql = "select * from lkcrm_admin_dept";
        List<Record> recordList = JavaBeanUtil.mapToRecords(categoryDao.queryListBySql(listSql));
        return R.ok().put("data", recordList);
    }

    public R queryExamineCategoryById(String id) {
        String sql1 = "select * from lkcrm_oa_examine_category where category_id = ?";
        Record examineCategory = JavaBeanUtil.mapToRecord(categoryDao.queryUniqueSql(sql1, id));
        String sql2 = "select a.*,b.realname,b.img from lkcrm_oa_examine_step a left join lkcrm_admin_user b on a.user_id = b.user_id where category_id = ?";
        List<Record> stepList = JavaBeanUtil.mapToRecords(categoryDao.queryListBySql(sql2, id));
        examineCategory.set("stepList", stepList);
        return R.ok().put("data", examineCategory);
    }

    public R queryAllExamineCategoryList() {
        LoginUser user = BaseUtil.getUser();
        List<Record> recordList = JavaBeanUtil.mapToRecords(categoryDao.queryAllExamineCategoryList(user.getUserId(), user.getDeptId()));
        //List<Record> recordList = Db.find(Db.getSqlPara("oa.examine.queryAllExamineCategoryList", Kv.by("userId", user.getUserId()).set("deptId", user.getDeptId())));
        return R.ok().put("data", recordList);
    }

    public List<Record> queryField(Integer id) {
        List<Record> list = JavaBeanUtil.mapToRecords(categoryDao.sqlQuery("select * from `lkcrm_admin_field` where examine_category_id = ?", id));
        adminFieldService.recordToFormType(list);
        return list;
    }

    public List<Record> queryField() {
        List<Record> fieldList = new LinkedList<>();
        String[] settingArr = new String[]{};
        List<Record> fixedFieldList = adminFieldService.list("10");
        fieldUtil.getFixedField(fieldList, "title", "审批内容", "", "text", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "text", settingArr, 1);
        fieldList.addAll(fixedFieldList);
        return fieldList;
    }

    public List<Record> queryFieldList() {
        List<Record> fieldList = new ArrayList<>();
        fieldUtil.addListHead(fieldList, "title", "审批内容");
        fieldUtil.addListHead(fieldList, "remark", "备注");
        fieldList.addAll(adminFieldService.list("10"));
        return fieldList;
    }

    public R updateStatus(String id) {
        String updateSql = "update lkcrm_oa_examine_category set status = abs(status-1) where category_id = ?";
        int update = categoryDao.executeUpdateSQL(updateSql, id);
        return update > 0 ? R.ok() : R.error();
    }


}
