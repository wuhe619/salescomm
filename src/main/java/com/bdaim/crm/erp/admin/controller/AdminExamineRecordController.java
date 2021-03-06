package com.bdaim.crm.erp.admin.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.dao.LkCrmAdminExamineRecordDao;
import com.bdaim.crm.erp.admin.service.AdminExamineRecordService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 审核合同或回款
 *
 */
@RestController
@RequestMapping("/examineRecord")
public class AdminExamineRecordController extends BasicAction {
    @Resource
    private AdminExamineRecordService examineRecordService;
   /* @Resource
    private LkCrmAdminExamineRecordDao crmAdminExamineRecordDao;*/

    /**
     * 审核合同或者回款 recordId:审核记录id status:审批状态：审核状态  1 审核通过 2 审核拒绝 4 已撤回
     * remarks:审核备注 id:审核对象的id（合同或者回款的id）
     */
    @RequestMapping(value = "/auditExamine", method = RequestMethod.POST)
    public R auditExamine() {
        Integer recordId = getInt("recordId");
        Integer status = getInt("status");
        Integer id = getInt("id");
        String remarks = get("remarks");
        Long nextUserId = getLong("nextUserId");
        Long ownerUserId = getLong("ownerUserId");
        return (examineRecordService.auditExamine(recordId, status, remarks, id, nextUserId, ownerUserId));
    }

    /**
     * 根据审核记录id，查询审核日志
     * recordId 审核记录id
     */
    @RequestMapping(value = "/queryExamineLogList", method = RequestMethod.POST)
    public R queryExamineLogList() {
        Integer recordId = getInt("recordId");
        if (getExamineObjIdByRecordId(recordId)) {
            return (R.noAuth());
        }
        return (examineRecordService.queryExamineLogList(recordId));
    }

    /**
     * 根据审核记录id，查询审核日志
     * recordId 审核记录id ownerUserId 负责人ID
     */
    @RequestMapping(value = "/queryExamineRecordList", method = RequestMethod.POST)
    public R queryExamineRecordList() {
        Integer recordId = getInt("recordId");
        long ownerUserId = getLong("ownerUserId");
        if (getExamineObjIdByRecordId(recordId)) {
            return (R.noAuth());
        }
        return (examineRecordService.queryExamineRecordList(recordId, ownerUserId));
    }

    /**
     * 根据recordId查询权限
     *
     * @param recordId
     * @return
     */
    private boolean getExamineObjIdByRecordId(Integer recordId) {
       /* boolean auth;
        int id = crmAdminExamineRecordDao.queryForInt("select contract_id from `lkcrm_crm_contract` where examine_record_id = ?", recordId);
        if (id > 0) {
            auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), id);
        } else {
            id = crmAdminExamineRecordDao.queryForInt("select receivables_id from `lkcrm_crm_receivables` where examine_record_id = ?", recordId);
            auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.RECEIVABLES_TYPE_KEY.getSign()), id);
        }*/
        return examineRecordService.getExamineObjIdByRecordId(recordId);
    }
}
