package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.LkCrmAdminFieldDao;
import com.bdaim.crm.dao.LkCrmReceivablesDao;
import com.bdaim.crm.dao.LkCrmReceivablesPlanDao;
import com.bdaim.crm.entity.LkCrmReceivablesEntity;
import com.bdaim.crm.entity.LkCrmReceivablesPlanEntity;
import com.bdaim.crm.erp.admin.service.AdminExamineRecordService;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.entity.CrmReceivables;
import com.bdaim.crm.utils.*;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.NumberConvertUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CrmReceivablesService {

    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private FieldUtil fieldUtil;

    @Resource
    private CrmRecordService crmRecordService;

    @Resource
    private AdminExamineRecordService examineRecordService;

    @Resource
    private AuthUtil authUtil;

    @Resource
    private LkCrmReceivablesDao crmReceivablesDao;

    @Resource
    private LkCrmReceivablesPlanDao crmReceivablesPlanDao;
    @Resource
    private LkCrmAdminFieldDao crmAdminFieldDao;

    /**
     * 获取用户审核通过和未通过的回款
     *
     * @param userId 用户ID
     * @author HJP
     */
    public List<CrmReceivables> queryListByUserId(Integer userId) {
        CrmReceivables crmReceivables = new CrmReceivables();
        String sql = "select re.receivables_id,re.contract_id,c.`name`,r.check_time,r.check_user_id,u.username,re.check_status from lkcrm_crm_receivables re"
                + "left join lkcrm_crm_contract c"
                + "on c.contract_id=re.contract_id"
                + "left join lkcrm_admin_examine_step s"
                + "on re.flow_id=s.flow_id and re.order_id=s.order_id"
                + "left join lkcrm_admin_examine_record r"
                + "on s.flow_id=r.flow_id and s.step_id=r.step_id"
                + "left join lkcrm_admin_user u"
                + "on r.check_user_id=u.id"
                + "where re.check_status=2 or re.check_status=3 and re.create_user_id= ? ";
        return crmReceivablesDao.queryListBySql(sql, CrmReceivables.class, userId);
    }

    /**
     * 分页查询回款
     *
     * @return
     */
    public CrmPage queryPage(BasePageRequest<CrmReceivables> basePageRequest) {
        return BaseUtil.crmPage(crmReceivablesDao.getReceivablesPageList(basePageRequest.getPage(), basePageRequest.getLimit()));
        //return Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.receivables.getReceivablesPageList"));
    }

    /**
     * 新建或者修改回款
     *
     * @param jsonObject
     */
    @Before(Tx.class)
    public R saveOrUpdate(JSONObject jsonObject) {
        CrmReceivables entity = jsonObject.getObject("entity", CrmReceivables.class);
        LkCrmReceivablesEntity crmReceivables = new LkCrmReceivablesEntity();
        BeanUtils.copyProperties(entity, crmReceivables);
        LoginUser user = BaseUtil.getUser();
        crmReceivables.setCreateUserId(user.getUserId());
        crmReceivables.setCustId(user.getCustId());
        String batchId = StrUtil.isNotEmpty(crmReceivables.getBatchId()) ? crmReceivables.getBatchId() : IdUtil.simpleUUID();
        crmRecordService.updateRecord(jsonObject.getJSONArray("field"), batchId);
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        if (entity.getReceivablesId() == null) {
            Integer count = crmReceivablesDao.queryByNumber(crmReceivables.getNumber());
            //Integer count = Db.queryInt(Db.getSql("crm.receivables.queryByNumber"), crmReceivables.getNumber());
            if (count != null && count > 0) {
                return R.error("回款编号已存在，请校对后再添加！");
            }
            crmReceivables.setCreateTime(DateUtil.date().toTimestamp());
            crmReceivables.setUpdateTime(DateUtil.date().toTimestamp());
            crmReceivables.setBatchId(batchId);
            crmReceivables.setCheckStatus(0);
            crmReceivables.setOwnerUserId(user.getUserId());
            Map<String, Integer> map = examineRecordService.saveExamineRecord(2, jsonObject.getLong("checkUserId"), crmReceivables.getOwnerUserId(), null);
            if (map.get("status") == 0) {
                return R.error("没有启动的审核步骤，不能添加！");
            } else {
                crmReceivables.setExamineRecordId(map.get("id"));
            }
            boolean save = (int) crmReceivablesDao.saveReturnPk(crmReceivables) > 0;
            if (crmReceivables.getPlanId() != null) {
                LkCrmReceivablesPlanEntity crmReceivablesPlan = crmReceivablesPlanDao.get(crmReceivables.getPlanId());
                if (crmReceivablesPlan != null) {
                    crmReceivablesPlan.setReceivablesId(crmReceivables.getReceivablesId());
                    crmReceivablesPlan.setUpdateTime(DateUtil.date().toTimestamp());
                    crmReceivablesPlanDao.update(crmReceivablesPlan);
                }
            }

            crmRecordService.addRecord(crmReceivables.getReceivablesId(), CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            return R.isSuccess(save);
        } else {
            crmReceivables.setReceivablesId(entity.getReceivablesId());
            LkCrmReceivablesEntity receivables = crmReceivablesDao.get(crmReceivables.getReceivablesId());
            if (receivables.getCheckStatus() != 4 && receivables.getCheckStatus() != 3) {
                //return R.error("不能编辑，请先撤回再编辑！");
                return R.error("当前审批状态不能编辑(审核未通过|已撤回可编辑)，请先撤回再编辑！");
            }
            Map<String, Integer> map = examineRecordService.saveExamineRecord(2, jsonObject.getLong("checkUserId"), receivables.getOwnerUserId(), receivables.getExamineRecordId());
            if (map.get("status") == 0) {
                return R.error("没有启动的审核步骤，不能添加！");
            } else {
                crmReceivables.setExamineRecordId(map.get("id"));
            }
            crmRecordService.updateRecord(crmReceivablesDao.get(crmReceivables.getReceivablesId()), crmReceivables, CrmEnum.RECEIVABLES_TYPE_KEY.getTypes());
            crmReceivables.setCheckStatus(0);
            crmReceivables.setUpdateTime(DateUtil.date().toTimestamp());
            LkCrmReceivablesPlanEntity crmReceivablesPlan = crmReceivablesPlanDao.get(crmReceivables.getPlanId());
            if (crmReceivablesPlan != null) {
                crmReceivablesPlan.setReceivablesId(crmReceivables.getReceivablesId());
                crmReceivablesPlan.setUpdateTime(DateUtil.date().toTimestamp());
                crmReceivablesPlanDao.update(crmReceivablesPlan);
            }
            BeanUtils.copyProperties(crmReceivables, receivables, JavaBeanUtil.getNullPropertyNames(crmReceivables));
            crmReceivablesDao.update(receivables);
            return R.ok();
        }
    }

    /**
     * 根据id查询回款
     */
    public R queryById(Integer id) {
        //Record record = Db.findFirst(Db.getSqlPara("crm.receivables.queryReceivablesById", Kv.by("id", id)));
        return R.ok().put("data", crmReceivablesDao.queryReceivablesById(id).get(0));
    }

    /**
     * 根据id查询回款基本信息
     */
    public List<Record> information(Integer id) {
        Record record = JavaBeanUtil.mapToRecord(crmReceivablesDao.queryReceivablesById(id).get(0));
        if (record == null) {
            return null;
        }
        List<Record> fieldList = new ArrayList<>();
        FieldUtil field = new FieldUtil(fieldList);
        field.set("回款编号", record.getStr("number"))
                .set("客户名称", record.getStr("customer_name"))
                .set("合同编号", record.getStr("contract_num"))
                .set("回款日期", DateUtil.formatDate(record.getDate("return_time")))
                .set("回款金额", record.getStr("money"))
                .set("期数", record.getStr("plan_num"))
                .set("备注", record.getStr("remark"));

        //List<Record> recordList = Db.find(Db.getSql("admin.field.queryCustomField"), record.getStr("batch_id"));
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmAdminFieldDao.queryCustomField(record.getStr("batch_id")));
        fieldUtil.handleType(recordList);
        fieldList.addAll(recordList);
        return fieldList;
    }

    /**
     * 根据id删除回款
     */
    @Before(Tx.class)
    public R deleteByIds(String receivablesIds) {
        String[] idsArr = receivablesIds.split(",");
        List<LkCrmReceivablesPlanEntity> list = crmReceivablesPlanDao.queryReceivablesReceivablesId(Arrays.asList(idsArr));
        //List<CrmReceivables> list = CrmReceivables.dao.find(Db.getSqlPara("crm.receivablesplan.queryReceivablesReceivablesId", Kv.by("receivablesIds", idsArr)));
        if (list.size() > 0) {
            return R.error("该数据已被其他模块引用，不能被删除！");
        }
        for (String id : idsArr) {
            LkCrmReceivablesEntity receivables = crmReceivablesDao.get(NumberConvertUtil.parseInt(id));
            if (receivables != null) {
                crmReceivablesDao.executeUpdateSQL("delete FROM lkcrm_admin_fieldv where batch_id = ?", receivables.getBatchId());
            }
            crmReceivablesDao.delete(NumberConvertUtil.parseInt(id));
            //if (!CrmReceivables.dao.deleteById(id)) {
            //return R.error();
            //}
        }
        return R.ok();
    }

    /**
     * @author wyq
     * 查询编辑字段
     */
    public List<Record> queryField(Integer receivablesId) {
        String receivablesview = BaseUtil.getViewSql("receivablesview");
        Record receivables = JavaBeanUtil.mapToRecord(crmReceivablesDao.sqlQuery("select * from " + receivablesview + " where receivables_id = ?", receivablesId).get(0));
        //Record receivables = Db.findFirst("select * from receivablesview where receivables_id = ?",receivablesId);
        List<Record> list = new ArrayList<>();
        list.add(new Record().set("customer_id", receivables.getInt("customer_id")).set("customer_name", receivables.getStr("customer_name")));
        receivables.set("customer_id", list);
        list = new ArrayList<>();
        list.add(new Record().set("contract_id", receivables.getStr("contract_id")).set("contract_num", receivables.getStr("contract_num")));
        receivables.set("contract_id", list);
        return adminFieldService.queryUpdateField(7, receivables);
    }

    /**
     * 根据条件查询回款
     */
    public List queryList(CrmReceivables receivables) {
        String sq = "select * from lkcrm_crm_receivables where 1 = 1 ";
        StringBuffer sql = new StringBuffer(sq);
        if (receivables.getCustomerId() != null) {
            sql.append(" and customer_id = ").append(receivables.getCustomerId());
        }
        if (receivables.getContractId() != null) {
            sql.append(" and contract_id = ").append(receivables.getContractId());
        }
        return crmReceivablesDao.sqlQuery(sql.toString());
    }

    /**
     * 根据条件查询回款
     */
    public List queryListByType(String type, Integer id) {
        String sq = "select * from lkcrm_crm_receivables where ";
        StringBuffer sql = new StringBuffer(sq);
        if (type.equals(CrmEnum.CUSTOMER_TYPE_KEY.getTypes())) {
            sql.append("  customer_id = ? ");
        }
        if (type.equals(CrmEnum.CONTRACT_TYPE_KEY.getTypes())) {
            sql.append("  contract_id = ? ");
        }

        return crmReceivablesDao.sqlQuery(sql.toString(), id);
    }

    /**
     * 根据合同id查询回款
     */
    public R qureyListByContractId(BasePageRequest<CrmReceivables> basePageRequest) {
        Integer pageType = basePageRequest.getPageType();
        if (0 == pageType) {
            return R.ok().put("data", crmReceivablesDao.queryReceivablesPageList(basePageRequest.getData().getContractId()));
            //return R.ok().put("data", Db.find(Db.getSql("crm.receivables.queryReceivablesPageList"),basePageRequest.getData().getContractId()));
        } else {
            com.bdaim.common.dto.Page page = crmReceivablesDao.pageQueryReceivablesPageList(basePageRequest.getPage(), basePageRequest.getLimit(), basePageRequest.getData().getContractId());
            return R.ok().put("data", BaseUtil.crmPage(page));
            //return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(),new SqlPara().setSql(Db.getSql("crm.receivables.queryReceivablesPageList")).addPara(basePageRequest.getData().getContractId())));
        }
    }
}
