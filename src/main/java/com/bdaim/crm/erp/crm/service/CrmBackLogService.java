package com.bdaim.crm.erp.crm.service;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ObjectUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.dao.LkCrmAdminConfigDao;
import com.bdaim.crm.dao.LkCrmCustomerDao;
import com.bdaim.crm.dao.LkCrmLeadsDao;
import com.bdaim.crm.ent.service.EntDataService;
import com.bdaim.crm.entity.LkCrmAdminConfigEntity;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.StringUtil;
import com.jfinal.kit.Kv;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.*;

/**
 * @author wyq
 */
@Service
@Transactional
public class CrmBackLogService {
    @Resource
    AdminSceneService adminSceneService;
    @Resource
    LkCrmCustomerDao crmCustomerDao;
    @Resource
    LkCrmAdminConfigDao crmAdminConfigDao;
    @Resource
    LkCrmLeadsDao crmLeadsDao;
    @Autowired
    private EntDataService entDataService;

    /**
     * 代办事项数量统计
     */
    public R num() {
        LoginUser user = BaseUtil.getUser();
        String userId = user.getUserId().toString();
        Integer todayLeads = crmCustomerDao.todayLeadsNum(userId);
        Integer todayCustomer = crmCustomerDao.todayCustomerNum(userId);
        Integer followLeads = crmCustomerDao.followLeadsNum(userId);
        Integer followCustomer = crmCustomerDao.followCustomerNum(userId);
        Integer config = crmCustomerDao.queryForInt("select status from lkcrm_admin_config where name = 'expiringContractDays' AND cust_id = ? ", user.getCustId());
        Integer checkReceivables = crmCustomerDao.checkReceivablesNum(userId);
        Integer remindReceivablesPlan = crmCustomerDao.remindReceivablesPlanNum(userId);
        LkCrmAdminConfigEntity adminConfig = crmAdminConfigDao.get("expiringContractDays", user.getCustId());
        Integer endContract = 0;
        // 即将到期的合同
        if (1 == adminConfig.getStatus()) {
            endContract = crmCustomerDao.endContractNum(adminConfig.getValue(), userId);
        }
        //即将到期的线索(即将回收的线索)
        LkCrmAdminConfigEntity seaRule = crmAdminConfigDao.get("seaRule", user.getCustId());
        Integer endLeads = 0;
        // 即将到期的线索
        if (seaRule != null) {
            JSONObject value = JSON.parseObject(seaRule.getValue());
            // 线索回收提醒打开状态判断
            JSONObject recoveryRemind = value.getJSONObject("recoveryRemind");
            JSONObject recovery = value.getJSONObject("recovery");
            boolean remindStatus = recoveryRemind != null && recoveryRemind.getBooleanValue("status") && StringUtil.isNotEmpty(recoveryRemind.getString("advanceDay"));
            boolean recoveryStatus = recovery != null && recovery.getBooleanValue("status");
            if (remindStatus && recoveryStatus) {
                endLeads = crmLeadsDao.endLeadsNum(recovery.getIntValue("noFollowDay") + recoveryRemind.getIntValue("advanceDay"),
                        recovery.getIntValue("noBCDay") + recoveryRemind.getIntValue("advanceDay"), userId);
            }
        }

        //即将到期的客户(即将回收的客户)
        LkCrmAdminConfigEntity customerRule = crmAdminConfigDao.get("customerRule", user.getCustId());
        Integer endCustomer = 0;
        // 即将到期的线索
        if (customerRule != null) {
            JSONObject value = JSON.parseObject(customerRule.getValue());
            // 线索回收提醒打开状态判断
            JSONObject recoveryRemind = value.getJSONObject("recoveryRemind");
            JSONObject recovery = value.getJSONObject("recovery");
            boolean remindStatus = recoveryRemind != null && recoveryRemind.getBooleanValue("status") && StringUtil.isNotEmpty(recoveryRemind.getString("advanceDay"));
            boolean recoveryStatus = recovery != null && recovery.getBooleanValue("status");
            if (remindStatus && recoveryStatus) {
                endCustomer = crmCustomerDao.endCustomerNum(recovery.getIntValue("noFollowDay") + recoveryRemind.getIntValue("advanceDay"),
                        recovery.getIntValue("noBCDay") + recoveryRemind.getIntValue("advanceDay"), userId);
            }
        }

        Kv kv = Kv.by("todayLeads", todayLeads).set("todayCustomer", todayCustomer).set("followLeads", followLeads).set("followCustomer", followCustomer)
                .set("checkReceivables", checkReceivables).set("remindReceivablesPlan", remindReceivablesPlan).set("endContract", endContract)
                .set("endLeads", endLeads).set("endCustomer", endCustomer);
        //if (config == 1) {
        Integer checkContract = crmCustomerDao.checkContractNum(userId);
        kv.set("checkContract", checkContract);
        // }
        return R.ok().put("data", kv);
    }

    private void handleCallField(com.bdaim.common.dto.Page page) {
        if (page.getData() != null && page.getData().size() > 0) {
            List fList;
            Map f, value;
            JSONObject company;
            for (int i = 0; i < page.getData().size(); i++) {
                fList = new ArrayList();
                value = (Map) page.getData().get(i);
                value.put("entId", "");
                for (Object k : value.keySet()) {
                    if ("mobile".equals(k) || "手机号".equals(k) || "手机".equals(k)) {
                        // 处理手机号类型
                        f = new HashMap();
                        f.put("field", k);
                        f.put("type", 7);
                        fList.add(f);
                    } else if ("telephone".equals(k) || "固话".equals(k) || "电话".equals(k)) {
                        // 处理电话类型
                        f = new HashMap();
                        f.put("field", k);
                        f.put("type", 22);
                        fList.add(f);
                    }
                }
                // 处理公司名称
                /*if (value.containsKey("company") && StringUtil.isNotEmpty(String.valueOf(value.get("company")))) {
                    company = entDataService.getCompanyByName(String.valueOf(value.get("company")));
                    if (company != null) {
                        value.put("entId", company.getString("id"));
                    }
                }*/
                value.put("flist", fList);
            }
        }
    }

    /**
     * <p>今日需联系客户</p>
     * <p>今日需要联系为下次联系时间是今天且没有跟进的客户</p>
     * <p>已逾期是过了下次联系时间那天的且未跟进的客户</p>
     * <p>已联系是下次联系时间是今天且已经跟进的客户</p>
     */
    public R todayCustomer(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        String customerview = BaseUtil.getViewSqlNotASName("customerview");
        StringBuffer stringBuffer = new StringBuffer("from " + customerview + " as a where ");
        if (type == 1) {
            stringBuffer.append(" a.customer_id not in (IFNULL((select GROUP_CONCAT(types_id) from lkcrm_admin_record where types = 'crm_customer' and to_days(create_time) = to_days(now())),0)) and to_days(a.next_time) = to_days(now())");
        } else if (type == 2) {
            stringBuffer.append(" a.customer_id not in (IFNULL((select GROUP_CONCAT(types_id) from lkcrm_admin_record where types = 'crm_customer' and to_days(create_time) >= to_days(a.next_time)),0)) and to_days(a.next_time) < to_days(now())");
        } else if (type == 3) {
            stringBuffer.append(" a.customer_id = any(select types_id from lkcrm_admin_record where types = 'crm_customer' and to_days(create_time) = to_days(now())) and to_days(a.next_time) = to_days(now())");
        } else {
            return R.error("type类型不正确");
        }
        LoginUser user = BaseUtil.getUser();
        if (isSub == 1) {
            stringBuffer.append(" and a.owner_user_id = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and a.owner_user_id in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            stringBuffer.append(getConditionSql(data));
        }
        //Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select * ", stringBuffer.toString());
        com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select * " + stringBuffer.toString(), basePageRequest.getPage(), basePageRequest.getLimit());
        // 处理联系方式flist字段
        handleCallField(page);
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    /**
     * 即将到期的客户列表
     */
    public R endCustomer(BasePageRequest basePageRequest) {
        LoginUser user = BaseUtil.getUser();
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        //即将到期的客户(即将回收的客户)
        LkCrmAdminConfigEntity customerRule = crmAdminConfigDao.get("customerRule", user.getCustId());
        if (customerRule == null || StringUtil.isEmpty(customerRule.getValue())) {
            return R.error("客户回收规则未设置");
        }
        // 线索回收设置状态判断
        JSONObject value = JSON.parseObject(customerRule.getValue());
        JSONObject recovery = value.getJSONObject("recovery");
        boolean recoveryStatus = recovery != null && recovery.getBooleanValue("status");
        if (!recoveryStatus) {
            return R.error("客户回收规则未设置");
        }
        JSONObject recoveryRemind = value.getJSONObject("recoveryRemind");
        boolean remindStatus = recoveryRemind != null && recoveryRemind.getBooleanValue("status");
        if (!remindStatus) {
            return R.error("客户回收提醒未设置");
        }

        String customerview = BaseUtil.getViewSqlNotASName("customerview");
        StringBuffer stringBuffer = new StringBuffer("from " + customerview + " as ccc where ");
        stringBuffer.append(" ccc.owner_user_id != 0 AND ccc.deal_status = '未成交' AND ccc.is_lock = 0 ")
                .append(" AND( ( to_days(now()) - to_days( IFNULL( ( SELECT car.create_time FROM lkcrm_admin_record AS car WHERE car.types = 'crm_customer' ")
                .append(" AND car.types_id = ccc.customer_id ORDER BY car.create_time DESC LIMIT 1), ccc.create_time ) ) ) >= abs(?) OR ( to_days(now()) - to_days(create_time) ) >= abs(?) ) ");

        if (isSub == 1) {
            stringBuffer.append(" and ccc.owner_user_id = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and ccc.owner_user_id in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            stringBuffer.append(getConditionSql(data));
        }
        com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select * " + stringBuffer.toString(),
                basePageRequest.getPage(), basePageRequest.getLimit(),
                recovery.getIntValue("noFollowDay") + recoveryRemind.getIntValue("advanceDay"),
                recovery.getIntValue("noBCDay") + recoveryRemind.getIntValue("advanceDay"));
        // 处理联系方式flist字段
        handleCallField(page);
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    /**
     * 今日需联系线索
     *
     * @param basePageRequest
     * @return
     */
    public R todayCrmLeads(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        String customerview = BaseUtil.getViewSqlNotASName("leadsview");
        StringBuffer stringBuffer = new StringBuffer("from " + customerview + " as a where ");
        if (type == 1) {
            stringBuffer.append(" a.leads_id not in (IFNULL((select GROUP_CONCAT(types_id) from lkcrm_admin_record where types = 'crm_leads' and to_days(create_time) = to_days(now())),0)) and to_days(a.next_time) = to_days(now())");
        } else if (type == 2) {
            stringBuffer.append(" a.leads_id not in (IFNULL((select GROUP_CONCAT(types_id) from lkcrm_admin_record where types = 'crm_leads' and to_days(create_time) >= to_days(a.next_time)),0)) and to_days(a.next_time) < to_days(now())");
        } else if (type == 3) {
            stringBuffer.append(" a.leads_id = any(select types_id from lkcrm_admin_record where types = 'crm_leads' and to_days(create_time) = to_days(now())) and to_days(a.next_time) = to_days(now())");
        } else {
            return R.error("type类型不正确");
        }
        LoginUser user = BaseUtil.getUser();
        if (isSub == 1) {
            stringBuffer.append(" and a.owner_user_id = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and a.owner_user_id in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            stringBuffer.append(getConditionSql(data));
        }
        //Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select * ", stringBuffer.toString());
        com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select * " + stringBuffer.toString(), basePageRequest.getPage(), basePageRequest.getLimit());
        // 处理联系方式flist字段
        handleCallField(page);
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    /**
     * 即将到期的线索列表
     *
     * @param basePageRequest
     * @return
     */
    public R endCrmLeads(BasePageRequest basePageRequest) {
        LoginUser user = BaseUtil.getUser();
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        String customerview = BaseUtil.getViewSqlNotASName("leadsview");
        StringBuffer stringBuffer = new StringBuffer("from " + customerview + " as ccc where ");

        LkCrmAdminConfigEntity seaRule = crmAdminConfigDao.get("seaRule", user.getCustId());
        if (seaRule == null || StringUtil.isEmpty(seaRule.getValue())) {
            return R.error("线索回收规则未设置");
        }
        // 线索回收设置状态判断
        JSONObject value = JSON.parseObject(seaRule.getValue());
        JSONObject recovery = value.getJSONObject("recovery");
        boolean recoveryStatus = recovery != null && recovery.getBooleanValue("status");
        if (!recoveryStatus) {
            return R.error("线索回收规则未设置");
        }

        JSONObject recoveryRemind = value.getJSONObject("recoveryRemind");
        boolean remindStatus = recoveryRemind != null && recoveryRemind.getBooleanValue("status");
        if (!remindStatus) {
            return R.error("线索回收提醒未设置");
        }

        stringBuffer.append("  ccc.owner_user_id != 0 AND(ccc.followup = 0 OR ccc.followup IS NULL) AND (ccc.is_lock = 0 OR ccc.is_lock IS NULL) ")
                .append("  AND ( ( to_days(now()) - to_days( IFNULL( ( SELECT car.create_time FROM lkcrm_admin_record AS car WHERE car.types = 'crm_leads' AND car.types_id = ccc.leads_id ORDER BY car.create_time DESC LIMIT 1), ccc.create_time ) ) ) >= abs(?) ")
                .append("  OR ((to_days(now()) - to_days(create_time) ) >= abs(?) AND ccc.is_transform = 0 )) ");

        if (isSub == 1) {
            stringBuffer.append(" and ccc.owner_user_id = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and ccc.owner_user_id in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            stringBuffer.append(getConditionSql(data));
        }
        com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select * " + stringBuffer.toString(),
                basePageRequest.getPage(), basePageRequest.getLimit(),
                recovery.getIntValue("noFollowDay") + recoveryRemind.getIntValue("advanceDay"),
                recovery.getIntValue("noBCDay") + recoveryRemind.getIntValue("advanceDay"));
        // 处理联系方式flist字段
        handleCallField(page);
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    /**
     * 标记线索为已跟进
     */
    public R setLeadsFollowup(String ids) {
        crmLeadsDao.setLeadsFollowup(Arrays.asList(ids.split(",")));
        //Db.update(Db.getSqlPara("crm.backLog.setLeadsFollowup", Kv.by("ids", ids)));
        return R.ok();
    }

    /**
     * 分配给我的线索
     */
    public R followLeads(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        String leadsview = BaseUtil.getViewSqlNotASName("leadsview");
        StringBuffer stringBuffer = new StringBuffer("from " + leadsview + " as a where");
        if (type == 1) {
            stringBuffer.append(" a.followup = 0 and a.is_transform = 0");
        } else if (type == 2) {
            stringBuffer.append(" a.followup = 1 and a.is_transform = 0");
        } else {
            return R.error("type类型不正确");
        }
        LoginUser user = BaseUtil.getUser();
        if (isSub == 1) {
            stringBuffer.append(" and a.owner_user_id = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and a.owner_user_id in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            stringBuffer.append(getConditionSql(data));
        }
        com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select * " + stringBuffer.toString(), basePageRequest.getPage(), basePageRequest.getLimit());
        //Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select *", stringBuffer.toString());
        // 处理联系方式flist字段
        handleCallField(page);
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    /**
     * 标记客户为已跟进
     */
    public R setCustomerFollowup(String ids) {
        List<String> stringList = StrUtil.splitTrim(ids, ",");
        if (stringList.size() > 0) {
            crmCustomerDao.setCustomerFollowup(stringList);
            //Db.update(Db.getSqlPara("crm.backLog.setCustomerFollowup", Kv.by("ids", stringList)));
        }
        return R.ok();
    }

    /**
     * 分配给我的客户
     */
    public R followCustomer(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        String customerview = BaseUtil.getViewSqlNotASName("customerview");
        StringBuilder stringBuffer = new StringBuilder("from " + customerview + " as a where");
        if (type == 1) {
            stringBuffer.append(" a.followup = 0");
        } else if (type == 2) {
            stringBuffer.append(" a.followup = 1");
        } else {
            return R.error("type类型不正确");
        }
        LoginUser user = BaseUtil.getUser();
        if (isSub == 1) {
            stringBuffer.append(" and a.owner_user_id = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and a.owner_user_id in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            stringBuffer.append(getConditionSql(data));
        }
        com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select * " + stringBuffer.toString(), basePageRequest.getPage(), basePageRequest.getLimit());
        //Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select *", stringBuffer.toString());
        // 处理联系方式flist字段
        handleCallField(page);
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    /**
     * 待审核合同
     */
    public R checkContract(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        StringBuffer stringBuffer = new StringBuffer("select contract_id from lkcrm_crm_contract as a inner join lkcrm_admin_examine_record as b on a.examine_record_id = b.record_id left join lkcrm_admin_examine_log as c on b.record_id = c.record_id where c.is_recheck != 1 and ifnull(b.examine_step_id, 1) = ifnull(c.examine_step_id, 1) and");
        if (type == 1) {
            stringBuffer.append(" a.check_status in (0,1)");
        } else if (type == 2) {
            stringBuffer.append(" a.check_status in (2,3)");
        } else {
            return R.error("type类型不正确");
        }
        LoginUser user = BaseUtil.getUser();
        if (isSub == 1) {
            stringBuffer.append(" and c.examine_user = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and c.examine_user in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        List<Integer> contractIdList = crmCustomerDao.queryListForInteger(stringBuffer.toString());
        if (contractIdList.size() > 0) {
            String contractIds = CollUtil.join(contractIdList, ",");
            JSONObject data = jsonObject.getJSONObject("data");
            String contractview = BaseUtil.getViewSqlNotASName("contractview");
            com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select * from " + contractview + " as a where a.contract_id in (" + contractIds + ")" + getConditionSql(data), basePageRequest.getPage(), basePageRequest.getLimit());
            //Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select *", "from contractview as a where a.contract_id in (" + contractIds + ")" + getConditionSql(data));
            return R.ok().put("data", BaseUtil.crmPage(page));
        } else {
            Page<Record> page = new Page<>();
            page.setList(new ArrayList<>());
            return R.ok().put("data", page);
        }
    }

    /**
     * 待审核回款
     */
    public R checkReceivables(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        StringBuilder stringBuffer = new StringBuilder("select receivables_id from lkcrm_crm_receivables as a inner join lkcrm_admin_examine_record as b on a.examine_record_id = b.record_id left join lkcrm_admin_examine_log as c on b.record_id = c.record_id where ifnull(b.examine_step_id, 1) = ifnull(c.examine_step_id, 1) and");
        if (type == 1) {
            stringBuffer.append(" a.check_status in (0,1)");
        } else if (type == 2) {
            stringBuffer.append(" a.check_status in (2,3)");
        } else {
            return R.error("type类型不正确");
        }
        LoginUser user = BaseUtil.getUser();
        if (isSub == 1) {
            stringBuffer.append(" and c.examine_user = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and c.examine_user in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        List<Integer> receivablesIdList = crmCustomerDao.queryListForInteger(stringBuffer.toString());
        if (receivablesIdList.size() > 0) {
            String contractIds = CollUtil.join(receivablesIdList, ",");
            JSONObject data = jsonObject.getJSONObject("data");
            String receivablesview = BaseUtil.getViewSqlNotASName("receivablesview");
            com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select * from " + receivablesview + " as a where a.receivables_id in (" + contractIds + ")" + getConditionSql(data), basePageRequest.getPage(), basePageRequest.getLimit());
            //Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select *", "from receivablesview as a where a.receivables_id in (" + contractIds + ")" + getConditionSql(data));
            return R.ok().put("data", BaseUtil.crmPage(page));
        } else {
            Page<Record> page = new Page<>();
            page.setList(new ArrayList<>());
            return R.ok().put("data", new Page<Record>());
        }
    }

    /**
     * 待回款提醒
     */
    public R remindReceivables(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        StringBuffer stringBuffer = new StringBuffer("from lkcrm_crm_receivables_plan as a inner join lkcrm_crm_customer as b on a.customer_id = b.customer_id inner join lkcrm_crm_contract as c on a.contract_id = c.contract_id where");
        if (type == 1) {
            stringBuffer.append(" to_days(a.return_date) >= to_days(now()) and to_days(a.return_date) <= to_days(now())+a.remind and receivables_id is null");
        } else if (type == 2) {
            stringBuffer.append(" receivables_id is not null");
        } else if (type == 3) {
            stringBuffer.append(" to_days(a.return_date) < to_days(now()) and receivables_id is null");
        } else {
            return R.error("type类型不正确");
        }
        LoginUser user = BaseUtil.getUser();
        if (isSub == 1) {
            stringBuffer.append(" and c.owner_user_id = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and c.owner_user_id in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            stringBuffer.append(getConditionSql(data));
        }
        com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select a.num,a.customer_id,b.customer_name,a.contract_id,c.num as contractNum,a.money,a.return_date,a.return_type,a.remind,a.remark " + stringBuffer.toString(), basePageRequest.getPage(), basePageRequest.getLimit());
        //Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select a.num,a.customer_id,b.customer_name,a.contract_id,c.num as contractNum,a.money,a.return_date,a.return_type,a.remind,a.remark", stringBuffer.toString());
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    /**
     * 即将到期的合同
     */
    public R endContract(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        Integer type = jsonObject.getInteger("type");
        Integer isSub = jsonObject.getInteger("isSub");
        LoginUser user = BaseUtil.getUser();
        LkCrmAdminConfigEntity adminConfig = crmAdminConfigDao.get("expiringContractDays", user.getCustId());
        String contractview = BaseUtil.getViewSqlNotASName("contractview");
        StringBuffer stringBuffer = new StringBuffer("from " + contractview + " as a where");
        if (type == 1) {
            if (adminConfig.getStatus() == 0 || ObjectUtil.isNull(adminConfig)) {
                return R.ok().put("data", new Page<>());
            }
            stringBuffer.append(" to_days(end_time) >= to_days(now()) and to_days(end_time) <= to_days(now())+").append(adminConfig.getValue());
        } else if (type == 2) {
            stringBuffer.append(" to_days(end_time) < to_days(now())");
        } else {
            return R.error("type类型不正确");
        }
        if (isSub == 1) {
            stringBuffer.append(" and owner_user_id = ").append(user.getUserId());
        } else if (isSub == 2) {
            String ids = adminSceneService.getSubUserId(user.getUserId(), BaseConstant.AUTH_DATA_RECURSION_NUM).substring(1);
            if (StrUtil.isEmpty(ids)) {
                ids = "0";
            }
            stringBuffer.append(" and owner_user_id in (").append(ids).append(")");
        } else {
            return R.error("isSub参数不正确");
        }
        JSONObject data = jsonObject.getJSONObject("data");
        if (data != null) {
            stringBuffer.append(getConditionSql(data));
        }
        com.bdaim.common.dto.Page page = crmCustomerDao.sqlPageQuery("select * " + stringBuffer.toString(), basePageRequest.getPage(), basePageRequest.getLimit());
        //Page<Record> page = Db.paginate(basePageRequest.getPage(), basePageRequest.getLimit(), "select *", stringBuffer.toString());
        return R.ok().put("data", BaseUtil.crmPage(page));
    }

    public String getConditionSql(JSONObject data) {
        List<JSONObject> jsonObjectList = new ArrayList<>();
        if (data != null) {
            data.forEach((k, v) -> {
                jsonObjectList.add(JSON.parseObject(v.toString()));
            });
        }
        StringBuffer conditionSqlSb = new StringBuffer();
        for (JSONObject jsonObject : jsonObjectList) {
            String condition = jsonObject.getString("condition");
            String value = jsonObject.getString("value");
            String formType = jsonObject.getString("formType");
            if (StrUtil.isNotEmpty(value) || StrUtil.isNotEmpty(jsonObject.getString("start")) || StrUtil.isNotEmpty(jsonObject.getString("end")) || "business_type".equals(jsonObject.getString("formType"))) {
                conditionSqlSb.append(" and ").append("a.").append(jsonObject.getString("name"));
                if ("is".equals(condition)) {
                    conditionSqlSb.append(" = '").append(value).append("'");
                } else if ("isNot".equals(condition)) {
                    conditionSqlSb.append(" != '").append(value).append("'");
                } else if ("contains".equals(condition)) {
                    conditionSqlSb.append(" like '%").append(value).append("%'");
                } else if ("notContains".equals(condition)) {
                    conditionSqlSb.append(" not like '%").append(value).append("%'");
                } else if ("isNull".equals(condition)) {
                    conditionSqlSb.append(" is null");
                } else if ("isNotNull".equals(condition)) {
                    conditionSqlSb.append(" is not null");
                } else if ("gt".equals(condition)) {
                    conditionSqlSb.append(" > ").append(value);
                } else if ("egt".equals(condition)) {
                    conditionSqlSb.append(" >= ").append(value);
                } else if ("lt".equals(condition)) {
                    conditionSqlSb.append(" < ").append(value);
                } else if ("elt".equals(condition)) {
                    conditionSqlSb.append(" <= ").append(value);
                } else if ("in".equals(condition)) {
                    conditionSqlSb.append(" in (").append(value).append(")");
                }
                if ("datetime".equals(formType)) {
                    conditionSqlSb.append(" between '").append(jsonObject.getString("start")).append("' and '").append(jsonObject.getString("end")).append("'");
                }
                if ("date".equals(formType)) {
                    conditionSqlSb.append(" between '").append(jsonObject.getString("startDate")).append("' and '").append(jsonObject.getString("endDate")).append("'");
                }
            }
        }
        return conditionSqlSb.toString();
    }
}
