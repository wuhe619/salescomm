package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.LkCrmReceivablesDao;
import com.bdaim.crm.dao.LkCrmReceivablesPlanDao;
import com.bdaim.crm.entity.LkCrmReceivablesPlanEntity;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.crm.entity.CrmReceivables;
import com.bdaim.crm.erp.crm.entity.CrmReceivablesPlan;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.FieldUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional
public class CrmReceivablesPlanService {
    @Resource
    private FieldUtil fieldUtil;
    @Resource
    private AdminFieldService adminFieldService;
    @Resource
    private LkCrmReceivablesDao crmReceivablesDao;
    @Resource
    private LkCrmReceivablesPlanDao crmReceivablesPlanDao;

    /**
     * 添加或修改回款计划
     */
    public R saveAndUpdate(JSONObject jsonObject) {
        LkCrmReceivablesPlanEntity crmReceivablesPlan = jsonObject.getObject("entity", LkCrmReceivablesPlanEntity.class);
        crmReceivablesPlan.setCustId(BaseUtil.getUser().getCustId());
        String batchId = StrUtil.isNotEmpty(crmReceivablesPlan.getFileBatch()) ? crmReceivablesPlan.getFileBatch() : IdUtil.simpleUUID();
        adminFieldService.save(jsonObject.getJSONArray("field"), batchId);
        if (null == crmReceivablesPlan.getPlanId()) {
            crmReceivablesPlan.setCreateTime(DateUtil.date().toTimestamp());
            crmReceivablesPlan.setCreateUserId(BaseUtil.getUser().getUserId());
            crmReceivablesPlan.setFileBatch(batchId);
            LkCrmReceivablesPlanEntity receivablesPlan = crmReceivablesPlanDao.queryByContractId(crmReceivablesPlan.getContractId());
            //LkCrmReceivablesPlanEntity receivablesPlan = CrmReceivablesPlan.dao.findFirst(Db.getSql("crm.receivablesplan.queryByContractId"), crmReceivablesPlan.getContractId());
            if (receivablesPlan == null) {
                crmReceivablesPlan.setNum("1");
            } else {
                crmReceivablesPlan.setNum(Integer.valueOf(receivablesPlan.getNum()) + 1 + "");
            }
            return (int) crmReceivablesPlanDao.saveReturnPk(crmReceivablesPlan) > 0 ? R.ok() : R.error();
        } else {
            Integer number = Db.queryInt("select count(*) from lkcrm_crm_receivables where plan_id = ?", crmReceivablesPlan.getPlanId());
            if (number > 0) {
                return R.error("该回款计划已收到回款，请勿编辑");
            }
            crmReceivablesPlan.setUpdateTime(DateUtil.date().toTimestamp());
            crmReceivablesPlanDao.saveOrUpdate(crmReceivablesPlan);
            return R.ok();
        }
    }

    /**
     * @author wyq
     * 删除回款计划
     */
    public R deleteByIds(String planIds) {
        String[] idsArr = planIds.split(",");
        List<Record> idsList = new ArrayList<>();
        List ids = new ArrayList<>();
        for (String id : idsArr) {
            Integer number = crmReceivablesPlanDao.queryForInt("select count(*) from lkcrm_crm_receivables where plan_id = ?", id);
            if (number > 0) {
                return R.error("该回款计划已关联回款，禁止删除");
            }
            Record record = new Record();
            idsList.add(record.set("plan_id", Integer.valueOf(id)));
            ids.add(id);
        }
        int i = crmReceivablesPlanDao.deleteByIds(ids);
        /*return Db.tx(() -> {
            Db.batch(Db.getSql("crm.receivablesplan.deleteByIds"), "plan_id", idsList, 100);
            return true;
        }) ? R.ok() : R.error();*/
        return i > 0 ? R.ok() : R.error();
    }

    /**
     * @author zxy
     * 查询回款自定义字段（新增）
     */
    public List<Record> queryField() {
        List<Record> fieldList = new ArrayList<>();
        String[] settingArr = new String[]{};
        String[] returnTypeArr = new String[]{"支票", "现金", "邮政汇款", "电汇", "网上转账", "支付宝", "微信支付", "其他"};
        fieldUtil.getFixedField(fieldList, "customer_id", "客户名称", "", "customer", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "contract_id", "合同编号", "", "contract", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "money", "计划回款金额", "", "number", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "return_date", "计划回款日期", "", "date", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "return_type", "计划回款方式", "", "select", returnTypeArr, 1);
        fieldUtil.getFixedField(fieldList, "remind", "提前几天提醒", "", "number", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", "", "textarea", settingArr, 0);
        return fieldList;
    }

    /**
     * @author wyq
     * 查询回款自定义字段（编辑）
     */
    public List<Record> queryField(Integer id) {
        String sql = "select a.customer_id,b.customer_name,a.contract_id,c.num,a.money,a.return_date,a.return_type,a.remind,a.remark\n" +
                "  from lkcrm_crm_receivables_plan as a left join lkcrm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "  left join lkcrm_crm_contract as c on a.contract_id = c.contract_id\n" +
                "  where a.plan_id = ?";
        Record receivablesPlan = JavaBeanUtil.mapToRecord(crmReceivablesDao.sqlQuery(sql, id).get(0));
        //Record receivablesPlan = Db.findFirst(Db.getSql("crm.receivablesplan.queryUpdateField"),id);
        List<Record> fieldList = new ArrayList<>();
        String[] settingArr = new String[]{};
        String[] returnTypeArr = new String[]{"支票", "现金", "邮政汇款", "电汇", "网上转账", "支付宝", "微信支付", "其他"};
        List<Record> customerList = new ArrayList<>();
        customerList.add(new Record().set("customer_id", receivablesPlan.getInt("customer_id")).set("customer_name", receivablesPlan.getStr("customer_name")));
        fieldUtil.getFixedField(fieldList, "customer_id", "客户名称", customerList, "customer", settingArr, 1);
        List<Record> contractList = new ArrayList<>();
        contractList.add(new Record().set("contract_id", receivablesPlan.getInt("contract_id")).set("num", receivablesPlan.getStr("num")));
        fieldUtil.getFixedField(fieldList, "contract_id", "合同编号", contractList, "contract", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "money", "计划回款金额", receivablesPlan.getStr("money"), "number", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "return_date", "计划回款日期", receivablesPlan.getStr("return_date"), "date", settingArr, 1);
        fieldUtil.getFixedField(fieldList, "return_type", "计划回款方式", receivablesPlan.getStr("return_type"), "select", returnTypeArr, 1);
        fieldUtil.getFixedField(fieldList, "remind", "提前几天提醒", receivablesPlan.getStr("remind"), "number", settingArr, 0);
        fieldUtil.getFixedField(fieldList, "remark", "备注", receivablesPlan.getStr("remark"), "textarea", settingArr, 0);
        return fieldList;
    }

    /**
     * 根据合同查询回款计划
     */
    public R qureyListByContractId(BasePageRequest<CrmReceivables> basePageRequest) {

        Integer pageType = basePageRequest.getPageType();
        if (pageType == null || 0 == pageType) {
            return R.ok().put("data", crmReceivablesPlanDao.queryListByContractId(basePageRequest.getData().getContractId()));
            //return R.ok().put("data", Db.find(Db.getSql("crm.receivablesplan.queryListByContractId"), basePageRequest.getData().getContractId()));
        }
        if (1 == pageType) {
            Page page = crmReceivablesPlanDao.pageListByContractId(basePageRequest.getPage(), basePageRequest.getLimit(), basePageRequest.getData().getContractId());
            return R.ok().put("data", BaseUtil.crmPage(page));
            //return R.ok().put("data", Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), Db.getSqlPara("crm.receivablesplan.queryListByContractId", basePageRequest.getData().getContractId())));
        }
        return R.error();
    }

    /**
     * 根据合同id和客户id查询未使用的回款计划
     */
    public R queryByContractAndCustomer(CrmReceivablesPlan receivablesPlan) {
        List<LkCrmReceivablesPlanEntity> plans = crmReceivablesPlanDao.queryByCustomerIdContractId(receivablesPlan.getContractId(), receivablesPlan.getCustomerId());
        return R.ok().put("data", plans);
    }
}
