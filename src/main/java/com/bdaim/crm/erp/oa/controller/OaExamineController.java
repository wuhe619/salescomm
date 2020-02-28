package com.bdaim.crm.erp.oa.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.oa.common.OaEnum;
import com.bdaim.crm.erp.oa.entity.OaExamine;
import com.bdaim.crm.erp.oa.entity.OaExamineLog;
import com.bdaim.crm.erp.oa.entity.OaExamineRelation;
import com.bdaim.crm.erp.oa.service.OaExamineService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Db;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 审核步骤
 */
@RestController
@RequestMapping("/OaExamine")
public class OaExamineController extends BasicAction {

    @Resource
    private OaExamineService oaExamineService;

    /**
     * @param basePageRequest 分页对象
     * @author hmb
     * 我发起的审批
     */
    @RequestMapping(value = "/myInitiate", method = RequestMethod.POST)
    public R myInitiate(BasePageRequest<Void> basePageRequest) {
        return(oaExamineService.myInitiate(basePageRequest));
    }

    /**
     * @param basePageRequest 分页对象
     * @author hmb
     * 我审批的
     */
    @RequestMapping(value = "/myOaExamine", method = RequestMethod.POST)
    public R myOaExamine(BasePageRequest<OaExamine> basePageRequest) {
        return(oaExamineService.myOaExamine(basePageRequest));
    }

    @RequestMapping(value = "/getField", method = RequestMethod.POST)
    public R getField() {
        String id = getPara("examineId");
        Integer isDetail = getInt("isDetail");//1详情 2 编辑
        boolean oaAuth = AuthUtil.isOaAuth(-1, Integer.valueOf(id));
        if (oaAuth) {
            return(R.noAuth());
            //return;
        }
        return(oaExamineService.getField(id, isDetail));
    }

    /**
     * @author hmb
     * 创建审批
     */
    @RequestMapping(value = "/setOaExamine", method = RequestMethod.POST)
    public R setOaExamine(@RequestBody JSONObject jsonObject) {
       /* String data = getRawData();
        JSONObject jsonObject = JSON.parseObject(data);*/
        return(oaExamineService.setOaExamine(jsonObject));
    }

    /**
     * @author hmb
     * 审批
     */
    @RequestMapping(value = "/auditExamine", method = RequestMethod.POST)
    public R auditExamine() {
        Integer recordId = getInt("recordId");
        Integer status = getInt("status");
        String remarks = getPara("remarks");
        Long nextUserId = getLong("nextUserId");
        OaExamineLog oaExamineLog = new OaExamineLog();
        oaExamineLog.setRecordId(recordId);
        oaExamineLog.setExamineStatus(status);
        oaExamineLog.setRemarks(remarks);
        return(oaExamineService.oaExamine(oaExamineLog, nextUserId));
    }

    /**
     * @author hmb
     * 查询审批详情
     */
    @NotNullValidate(value = "examineId", message = "审批id不能为空")
    @RequestMapping(value = "/queryOaExamineInfo", method = RequestMethod.POST)
    public R queryOaExamineInfo() {
        String id = getPara("examineId");
        boolean oaAuth = AuthUtil.isOaAuth(-1, Integer.valueOf(id));
        if (oaAuth) {
            return(R.noAuth());
            //return;
        }
        return(oaExamineService.queryOaExamineInfo(id));
    }

    /**
     * @author hmb
     * 查询审批步骤
     */
    @RequestMapping(value = "/queryExamineRecordList", method = RequestMethod.POST)
    public R queryExamineRecordList() {
        Integer recordId = getInt("recordId");
        Integer examineId = Db.queryInt("select examine_id from `72crm_oa_examine_record` where record_id = ?", recordId);
        boolean oaAuth = AuthUtil.isOaAuth(-1, examineId);
        if (oaAuth) {
            return(R.noAuth());
            //return;
        }
        return(oaExamineService.queryExamineRecordList(recordId));
    }

    /**
     * @author hmb
     * 查询审批历史
     */
    @NotNullValidate(value = "recordId", message = "记录id不能为空")
    @RequestMapping(value = "/queryExamineLogList", method = RequestMethod.POST)
    public R queryExamineLogList() {
        Integer recordId = getInt("recordId");
        Integer examineId = Db.queryInt("select examine_id from `72crm_oa_examine_record` where record_id = ?", recordId);
        boolean oaAuth = AuthUtil.isOaAuth(-1, examineId);
        if (oaAuth) {
            return(R.noAuth());
            //return;
        }
        return(oaExamineService.queryExamineLogList(recordId));
    }

    /**
     * @author hmb
     * 删除审批
     */
    @NotNullValidate(value = "examineId", message = "审批id不能为空")
    @RequestMapping(value = "/deleteOaExamine", method = RequestMethod.POST)
    public R deleteOaExamine() {
        Integer oaExamineId = getParaToInt("examineId");
        boolean oaAuth = AuthUtil.isOaAuth(OaEnum.EXAMINE_TYPE_KEY.getTypes(), oaExamineId);
        if (oaAuth) {
            return (R.noAuth());
            //return;
        }
        return (oaExamineService.deleteOaExamine(oaExamineId));
    }

    /**
     * @author hmb
     * 查询审批步骤
     */
    @RequestMapping(value = "/queryExaminStep", method = RequestMethod.POST)
    public R queryExaminStep() {
        String categoryId = getPara("categoryId");
        return (oaExamineService.queryExaminStep(categoryId));
    }

    /**
     * @author hmb
     * 查询审批关联业务
     */
    @RequestMapping(value = "/queryExamineRelation", method = RequestMethod.POST)
    public R queryExamineRelation(@Para("") BasePageRequest<OaExamineRelation> pageRequest) {
        return (oaExamineService.queryExamineRelation(pageRequest));
    }

}
