package com.bdaim.crm.erp.admin.service;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.util.TypeUtils;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.LkCrmAdminDeptDao;
import com.bdaim.crm.dao.LkCrmBusinessStatusDao;
import com.bdaim.crm.dao.LkCrmBusinessTypeDao;
import com.bdaim.crm.entity.LkCrmBusinessStatusEntity;
import com.bdaim.crm.entity.LkCrmBusinessTypeEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.CrmPage;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.jfinal.aop.Before;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
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
public class AdminBusinessTypeService {

    @Autowired
    LkCrmBusinessTypeDao crmBusinessTypeDao;
    @Autowired
    LkCrmBusinessStatusDao crmBusinessStatusDao;
    @Autowired
    LkCrmAdminDeptDao crmAdminDeptDao;

    @Before(Tx.class)
    public void addBusinessType(LkCrmBusinessTypeEntity crmBusinessType, JSONArray crmBusinessStatusList) {
        LoginUser user = BaseUtil.getUser();
        if (crmBusinessType.getTypeId() == null) {
            crmBusinessType.setCustId(user.getCustId());
            crmBusinessType.setCreateTime(DateUtil.date().toTimestamp());
            crmBusinessType.setCreateUserId(user.getUserId());
            crmBusinessType.setStatus(1);
            crmBusinessTypeDao.save(crmBusinessType);
        } else {
            crmBusinessType.setStatus(1);
            crmBusinessType.setCustId(user.getCustId());
            crmBusinessType.setUpdateTime(DateUtil.date().toTimestamp());
            LkCrmBusinessTypeEntity dbEntity = crmBusinessTypeDao.get(crmBusinessType.getTypeId());
            BeanUtils.copyProperties(crmBusinessType, dbEntity, JavaBeanUtil.getNullPropertyNames(crmBusinessType));
            crmBusinessTypeDao.update(dbEntity);
            crmBusinessTypeDao.deleteBusinessStatus(crmBusinessType.getTypeId());
            //Db.delete(Db.getSql("admin.businessType.deleteBusinessStatus"), crmBusinessType.getTypeId());
        }
        Integer typeId = crmBusinessType.getTypeId();
        for (int i = 0; i < crmBusinessStatusList.size(); i++) {
            LkCrmBusinessStatusEntity crmBusinessStatus = TypeUtils.castToJavaBean(crmBusinessStatusList.getJSONObject(i), LkCrmBusinessStatusEntity.class);
            //crmBusinessStatus.setStatusId(null);
            crmBusinessStatus.setTypeId(typeId);
            crmBusinessStatus.setOrderNum(i + 1);
            crmBusinessStatusDao.save(crmBusinessStatus);
        }
    }

    public CrmPage queryBusinessTypeList(BasePageRequest request) {
        com.bdaim.common.dto.Page paginate = crmBusinessTypeDao.queryBusinessTypeList(request.getPage(), request.getLimit());
        //Page<Record> paginate = Db.paginate(request.getPage(), request.getLimit(), Db.getSqlPara("admin.businessType.queryBusinessTypeList"));
        List<Record> list = new ArrayList<>();
        paginate.getData().forEach(s -> {
            Record record = JavaBeanUtil.mapToRecord((Map<String, Object>) s);
            if (record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0) {
                List deptList = crmAdminDeptDao.queryByIds(Arrays.asList(record.getStr("dept_ids").split(",")));
                //List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
                record.set("deptIds", deptList);
            } else {
                record.set("deptIds", new ArrayList<>());
            }
            list.add(record);
        });
        paginate.setData(list);
        return BaseUtil.crmPage(paginate);
    }

    public R getBusinessType(String typeId) {
        Record record = JavaBeanUtil.mapToRecord(crmBusinessTypeDao.getBusinessType(typeId));
        if (record.getStr("dept_ids") != null && record.getStr("dept_ids").split(",").length > 0) {
            List<Record> deptList = JavaBeanUtil.mapToRecords(crmAdminDeptDao.queryByIds(Arrays.asList(record.getStr("dept_ids").split(","))));
            //List<Record> deptList = Db.find(Db.getSqlPara("admin.dept.queryByIds", Kv.by("ids", record.getStr("dept_ids").split(","))));
            record.set("deptIds", deptList);
        } else {
            record.set("deptIds", new ArrayList<>());
        }
        List<Record> statusList = JavaBeanUtil.mapToRecords(crmBusinessTypeDao.queryBusinessStatus(typeId));
        record.set("statusList", statusList);
        record.remove("dept_ids");
        return R.ok().put("data", record);
    }

    @Before(Tx.class)
    public R deleteById(String typeId) {
        Integer count = crmBusinessTypeDao.queryForInt("select count(*) from lkcrm_crm_business where type_id = ?", typeId);
        if (count > 0) {
            return R.error("使用中的商机组不可以删除");
        }
        crmBusinessTypeDao.delete(NumberConvertUtil.parseInt(typeId));
        crmBusinessStatusDao.executeUpdateSQL("delete from lkcrm_crm_business_status where type_id=?", typeId);
        //Db.deleteById("lkcrm_crm_business_type", "type_id", typeId);
        //Db.deleteById("lkcrm_crm_business_status", "type_id", typeId);
        return R.ok();
    }

}
