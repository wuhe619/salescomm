package com.bdaim.crm.erp.crm.controller;

import cn.hutool.core.date.DateUtil;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.interceptor.ClassTypeCheck;
import com.bdaim.crm.dto.LkCrmAdminRecordDTO;
import com.bdaim.crm.entity.LkCrmAdminRecordEntity;
import com.bdaim.crm.entity.LkCrmContractEntity;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.entity.CrmContract;
import com.bdaim.crm.erp.crm.entity.CrmContractProduct;
import com.bdaim.crm.erp.crm.entity.CrmReceivables;
import com.bdaim.crm.erp.crm.service.CrmContractService;
import com.bdaim.crm.erp.crm.service.CrmReceivablesPlanService;
import com.bdaim.crm.erp.crm.service.CrmReceivablesService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.StringUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 合同
 */
@RestController
@RequestMapping("/CrmContract")
public class CrmContractController extends BasicAction {
    @Resource
    private CrmContractService crmContractService;
    @Resource
    private CrmReceivablesService receivablesService;
    @Resource
    private CrmReceivablesPlanService receivablesPlanService;

    @Resource
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:contract:index"})
    @RequestMapping(value = "/queryPageList", method = RequestMethod.POST)
    public R queryPageList(@RequestBody JSONObject jsonObject) {
        jsonObject.fluentPut("type", 6);
        BasePageRequest<Void> basePageRequest = new BasePageRequest<>(jsonObject.getIntValue("page"), jsonObject.getIntValue("limit"));
        basePageRequest.setJsonObject(jsonObject);
        return (adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * 分页条件查询合同
     *
     * @author zxy
     */
    @RequestMapping(value = "/queryPage", method = RequestMethod.POST)
    @ClassTypeCheck(classType = CrmContract.class)
    public R queryPage(BasePageRequest<CrmContract> basePageRequest) {
        return (R.ok().put("data", crmContractService.queryPage(basePageRequest)));
    }

    /**
     * 根据id查询合同
     *
     * @author zxy
     */
    @Permissions("crm:contract:read")
    @NotNullValidate(value = "contractId", message = "合同id不能为空")
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public R queryById(@RequestParam("contractId") Integer id) {
        return (crmContractService.queryById(id));
    }

    /**
     * 根据id删除合同
     *
     * @author zxy
     */
    @Permissions("crm:contract:delete")
    @NotNullValidate(value = "contractIds", message = "合同id不能为空")
    @RequestMapping(value = "/deleteByIds", method = RequestMethod.POST)
    public R deleteByIds(@RequestParam("contractIds") String contractIds) {
        return (crmContractService.deleteByIds(contractIds));
    }

    /**
     * @author wyq
     * 合同转移
     */
    @Permissions("crm:contract:transfer")
    @NotNullValidate(value = "contractIds", message = "合同id不能为空")
    @NotNullValidate(value = "newOwnerUserId", message = "负责人id不能为空")
    @NotNullValidate(value = "transferType", message = "移除方式不能为空")
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public R transfer(LkCrmContractEntity crmContract) {
        return (crmContractService.transfer(crmContract));
    }

    /**
     * 添加或修改
     *
     * @author zxy
     */
    @Permissions({"crm:contract:save", "crm:contract:update"})
    @RequestMapping(value = "/saveAndUpdate", method = RequestMethod.POST)
    public R saveAndUpdate(@RequestBody JSONObject jsonObject) {
        //String data = getRawData();
        //JSONObject jsonObject = JSON.parseObject(data);
        return (crmContractService.saveAndUpdate(jsonObject));
    }

    /**
     * 根据条件查询合同
     *
     * @author zxy
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public R queryList(LkCrmContractEntity crmContract) {
        return (R.ok().put("data", crmContractService.queryList(crmContract)));
    }

    /**
     * 根据条件查询合同
     *
     * @author zxy
     */
    @NotNullValidate(value = "id", message = "id不能为空")
    @NotNullValidate(value = "type", message = "类型不能为空")
    @RequestMapping(value = "/queryListByType", method = RequestMethod.POST)
    public R queryListByType(@RequestParam("type") String type, @RequestParam("id") Integer id) {
        return (R.ok().put("data", crmContractService.queryListByType(type, id)));
    }

    /**
     * 根据合同批次查询产品
     *
     * @RequestParamm batchId
     * @author zxy
     */
    @RequestMapping(value = "/queryProductById", method = RequestMethod.POST)
    public R queryProductById(@RequestParam("batchId") String batchId) {
        return (R.ok().put("data", crmContractService.queryProductById(batchId)));
    }

    /**
     * 根据合同id查询回款
     *
     * @author zxy
     */
    @RequestMapping(value = "/queryReceivablesById", method = RequestMethod.POST)
    public R queryReceivablesById(@RequestParam("id") Integer id) {
        return (R.ok().put("data", crmContractService.queryReceivablesById(id)));
    }

    /**
     * 根据合同id查询回款计划
     *
     * @author zxy
     */
    @RequestMapping(value = "/queryReceivablesPlanById", method = RequestMethod.POST)
    public R queryReceivablesPlanById(@RequestParam("id") Integer id) {
        return (R.ok().put("data", crmContractService.queryReceivablesPlanById(id)));
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    @RequestMapping(value = "/getMembers", method = RequestMethod.POST)
    public R getMembers(@RequestParam("contractId") Integer contractId) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), contractId);
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (R.ok().put("data", JavaBeanUtil.recordToMap(crmContractService.getMembers(contractId))));
    }

    /**
     * @author wyq
     * 编辑团队成员
     */
    @RequestMapping(value = "/updateMembers", method = RequestMethod.POST)
    public R updateMembers(CrmContract crmContract) {
        return (crmContractService.addMember(crmContract));
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Permissions("crm:contract:teamsave")
    @RequestMapping(value = "/addMembers", method = RequestMethod.POST)
    public R addMembers(CrmContract crmContract) {
        return (crmContractService.addMember(crmContract));
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    @RequestMapping(value = "/deleteMembers", method = RequestMethod.POST)
    public R deleteMembers(CrmContract crmContract) {
        return (crmContractService.deleteMembers(crmContract));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId", message = "合同id不能为空")
    @NotNullValidate(value = "content", message = "内容不能为空")
    @NotNullValidate(value = "category", message = "跟进类型不能为空")
    @RequestMapping(value = "/addRecord", method = RequestMethod.POST)
    public R addRecord(LkCrmAdminRecordDTO adminRecord) throws ParseException {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), adminRecord.getTypesId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        LkCrmAdminRecordEntity lkCrmAdminRecordEntity = new LkCrmAdminRecordEntity();
        BeanUtils.copyProperties(adminRecord, lkCrmAdminRecordEntity, JavaBeanUtil.getNullPropertyNames(adminRecord));
        if(StringUtil.isNotEmpty(adminRecord.getNextTime())){
            lkCrmAdminRecordEntity.setNextTime(DateUtil.parse(adminRecord.getNextTime(),"yyyy-MM-dd HH:mm:ss"));
        }
        return (crmContractService.addRecord(lkCrmAdminRecordEntity));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @RequestMapping(value = "/getRecord", method = RequestMethod.POST)
    @ClassTypeCheck(classType = CrmContract.class)
    public R getRecord(BasePageRequest<CrmContract> basePageRequest) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), basePageRequest.getData().getContractId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (R.ok().put("data", JavaBeanUtil.recordToMap(crmContractService.getRecord(basePageRequest))));
    }

    /**
     * 根据合同ID查询回款
     *
     * @author zxy
     */
    @RequestMapping(value = "/qureyReceivablesListByContractId", method = RequestMethod.POST)
    @ClassTypeCheck(classType = CrmReceivables.class)
    public R qureyReceivablesListByContractId(BasePageRequest<CrmReceivables> basePageRequest) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), basePageRequest.getData().getContractId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (receivablesService.qureyListByContractId(basePageRequest));
    }

    /**
     * 根据合同ID查询产品
     *
     * @author zxy
     */
    @RequestMapping(value = "/qureyProductListByContractId", method = RequestMethod.POST)
    @ClassTypeCheck(classType = CrmContractProduct.class)
    public R qureyProductListByContractId(BasePageRequest<CrmContractProduct> basePageRequest) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), basePageRequest.getData().getContractId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmContractService.qureyProductListByContractId(basePageRequest));
    }

    /**
     * 根据合同ID查询回款计划
     *
     * @author zxy
     */
    @RequestMapping(value = "/qureyReceivablesPlanListByContractId", method = RequestMethod.POST)
    @ClassTypeCheck(classType = CrmReceivables.class)
    public R qureyReceivablesPlanListByContractId(BasePageRequest<CrmReceivables> basePageRequest) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTRACT_TYPE_KEY.getSign()), basePageRequest.getData().getContractId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (receivablesPlanService.qureyListByContractId(basePageRequest));
    }

    /**
     * 查询合同到期提醒设置
     */
    @RequestMapping(value = "/queryContractConfig", method = RequestMethod.POST)
    public R queryContractConfig() {
        return (crmContractService.queryContractConfig());
    }

    /**
     * 修改合同到期提醒设置
     */
    @NotNullValidate(value = "status", message = "status不能为空")
    @RequestMapping(value = "/setContractConfig", method = RequestMethod.POST)
    public R setContractConfig(@RequestParam(value = "status") Integer status, @RequestParam(value = "contractDay", required = false) Integer contractDay) {
        return (crmContractService.setContractConfig(status, contractDay));
    }
}
