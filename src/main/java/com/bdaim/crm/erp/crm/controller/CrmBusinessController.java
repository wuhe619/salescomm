package com.bdaim.crm.erp.crm.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.interceptor.ClassTypeCheck;
import com.bdaim.crm.dto.LkCrmAdminRecordDTO;
import com.bdaim.crm.entity.LkCrmAdminRecordEntity;
import com.bdaim.crm.entity.LkCrmBusinessEntity;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.entity.CrmBusiness;
import com.bdaim.crm.erp.crm.service.CrmBusinessService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

/**
 * 商机
 */
@RestController
@RequestMapping("/CrmBusiness")
public class CrmBusinessController extends BasicAction {
    @Resource
    private CrmBusinessService crmBusinessService;

    @Resource
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:business:index"})
    @RequestMapping(value = "/queryPageList", method = RequestMethod.POST)
    public R queryPageList(@RequestBody JSONObject jsonObject) {
        BasePageRequest<Void> basePageRequest = new BasePageRequest<>(jsonObject.getIntValue("page"),jsonObject.getIntValue("limit"));
        //JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type",5);
        jsonObject.fluentPut("type", 5);
        basePageRequest.setJsonObject(jsonObject);
        return (adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * @author wyq
     * 全局搜索查询商机
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public R queryList(BasePageRequest basePageRequest) {
        return (R.ok().put("data", crmBusinessService.getBusinessPageList(basePageRequest)));
    }

    /**
     * @author wyq
     * 新增或更新商机
     */
    @Permissions({"crm:business:save", "crm:business:update"})
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public R addOrUpdate(@RequestBody JSONObject jsonObject) {
        //JSONObject jsonObject = JSON.parseObject(getRawData());
        return (crmBusinessService.addOrUpdate(jsonObject));
    }

    /**
     * @author wyq
     * 根据商机id查询
     */
    @Permissions("crm:business:read")
    @NotNullValidate(value = "businessId", message = "商机id不能为空")
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public R queryById(@RequestParam("businessId") Integer businessId) {
        return (crmBusinessService.queryById(businessId));
    }

    /**
     * @author wyq
     * 根据商机名称查询
     */
    @NotNullValidate(value = "name", message = "名称不能为空")
    @RequestMapping(value = "/queryByName", method = RequestMethod.POST)
    public R queryByName(@RequestParam("name") String name) {
        return (R.ok().put("data", crmBusinessService.queryByName(name).getColumns()));
    }

    /**
     * @author wyq
     * 根据商机id查询产品
     */
    @RequestMapping(value = "/queryProduct", method = RequestMethod.POST)
    @ClassTypeCheck(classType = CrmBusiness.class)
    public R queryProduct(BasePageRequest<CrmBusiness> basePageRequest) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), basePageRequest.getData().getBusinessId());
        if (auth) {
            return (R.noAuth()); //return;
        }
        return (crmBusinessService.queryProduct(basePageRequest));
    }


    /**
     * @author wyq
     * 根据商机id查询合同
     */
    @RequestMapping(value = "/queryContract", method = RequestMethod.POST)
    @ClassTypeCheck(classType = CrmBusiness.class)
    public R queryContract(BasePageRequest<CrmBusiness> basePageRequest) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), basePageRequest.getData().getBusinessId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmBusinessService.queryContract(basePageRequest));
    }

    /**
     * @author wyq
     * 根据商机id查询联系人
     */
    @RequestMapping(value = "/queryContacts", method = RequestMethod.POST)
    @ClassTypeCheck(classType = CrmBusiness.class)
    public R queryContacts(BasePageRequest<CrmBusiness> basePageRequest) {
//        basePageRequest.setData(crmBusiness);
        return (crmBusinessService.queryContacts(basePageRequest));
    }

    /**
     * @author wyq
     * 商机关联联系人
     */
    @RequestMapping(value = "/relateContacts", method = RequestMethod.POST)
    public R relateContacts(@RequestParam("businessId") Integer businessId, @RequestParam("contactsIds") String contactsIds) {
        return(crmBusinessService.relateContacts(businessId, contactsIds));
    }

    /**
     * @author wyq
     * 商机解除关联联系人
     */
    @RequestMapping(value = "/unrelateContacts", method = RequestMethod.POST)
    public R unrelateContacts(@RequestParam("businessId") Integer businessId, @RequestParam("contactsIds") String contactsIds) {
        return(crmBusinessService.unrelateContacts(businessId, contactsIds));
    }

    /**
     * @author wyq
     * 根据id删除商机
     */
    @Permissions("crm:business:delete")
    @NotNullValidate(value = "businessIds", message = "商机id不能为空")
    @RequestMapping(value = "/deleteByIds", method = RequestMethod.POST)
    public R deleteByIds(@RequestParam("businessIds") String businessIds) {
        return(crmBusinessService.deleteByIds(businessIds));
    }

    /**
     * @author wyq
     * 根据商机id变更负责人
     */
    @Permissions("crm:business:transfer")
    @NotNullValidate(value = "businessIds", message = "商机id不能为空")
    @NotNullValidate(value = "newOwnerUserId", message = "负责人id不能为空")
    @NotNullValidate(value = "transferType", message = "移除方式不能为空")
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public R transfer(LkCrmBusinessEntity crmBusiness) {
        return(crmBusinessService.transfer(crmBusiness));
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    @NotNullValidate(value = "businessId", message = "商机id不能为空")
    @RequestMapping(value = "/getMembers", method = RequestMethod.POST)
    public R getMembers(@RequestParam("businessId") Integer businessId) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), businessId);
        if (auth) {
            return(R.noAuth());
            //return;
        }
        return(R.ok().put("data", crmBusinessService.getMembers(businessId)));
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Permissions("crm:business:teamsave")
    @NotNullValidate(value = "ids", message = "商机id不能为空")
    @NotNullValidate(value = "memberIds", message = "成员id不能为空")
    @NotNullValidate(value = "power", message = "读写权限不能为空")
    @RequestMapping(value = "/addMembers", method = RequestMethod.POST)
    public R addMembers(CrmBusiness crmBusiness) {
        return(crmBusinessService.addMember(crmBusiness));
    }

    /**
     * @author wyq
     * 编辑团队成员
     */
    @NotNullValidate(value = "ids", message = "商机id不能为空")
    @NotNullValidate(value = "memberIds", message = "成员id不能为空")
    @NotNullValidate(value = "power", message = "读写权限不能为空")
    @RequestMapping(value = "/updateMembers", method = RequestMethod.POST)
    public R updateMembers(CrmBusiness crmBusiness) {
        return(crmBusinessService.addMember(crmBusiness));
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    @NotNullValidate(value = "ids", message = "商机id不能为空")
    @NotNullValidate(value = "memberIds", message = "成员id不能为空")
    @RequestMapping(value = "/deleteMembers", method = RequestMethod.POST)
    public R deleteMembers(CrmBusiness crmBusiness) {
        return(crmBusinessService.deleteMembers(crmBusiness));
    }

    /**
     * @author 商机状态组展示
     */
    @NotNullValidate(value = "businessId", message = "商机id不能为空")
    @RequestMapping(value = "/queryBusinessStatus", method = RequestMethod.POST)
    public R queryBusinessStatus(@RequestParam("businessId") Integer businessId) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), businessId);
        if (auth) {
            return(R.noAuth());
            //return;
        }
        return(R.ok().put("data", crmBusinessService.queryBusinessStatus(businessId)));
    }

    /**
     * @author wyq
     * 商机状态组推进
     */
    @NotNullValidate(value = "businessId", message = "商机id不能为空")
    @RequestMapping(value = "/boostBusinessStatus", method = RequestMethod.POST)
    public R boostBusinessStatus(LkCrmBusinessEntity crmBusiness) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), crmBusiness.getBusinessId());
        if (auth) {
            return(R.noAuth());
            //return;
        }
        return(crmBusinessService.boostBusinessStatus(crmBusiness));
    }

    /**
     * @author wyq
     * 查询商机状态组及商机状态
     */
    @RequestMapping(value = "/queryBusinessStatusOptions", method = RequestMethod.POST)
    public R queryBusinessStatusOptions() {
        return(R.ok().put("data", JavaBeanUtil.recordToMap(crmBusinessService.queryBusinessStatusOptions(null))));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId", message = "商机id不能为空")
    @NotNullValidate(value = "content", message = "内容不能为空")
    @NotNullValidate(value = "category", message = "跟进类型不能为空")
    @RequestMapping(value = "/addRecord", method = RequestMethod.POST)
    public R addRecord(LkCrmAdminRecordDTO adminRecord) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), adminRecord.getTypesId());
        if (auth) {
            return(R.noAuth());
            //return;
        }
        LkCrmAdminRecordEntity lkCrmAdminRecordEntity = new LkCrmAdminRecordEntity();
        BeanUtils.copyProperties(adminRecord, lkCrmAdminRecordEntity, JavaBeanUtil.getNullPropertyNames(adminRecord));
        return(crmBusinessService.addRecord(lkCrmAdminRecordEntity));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @RequestMapping(value = "/getRecord", method = RequestMethod.POST)
    public R getRecord(BasePageRequest basePageRequest, CrmBusiness crmBusiness) {
        basePageRequest.setData(crmBusiness);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.BUSINESS_TYPE_KEY.getSign()), crmBusiness.getBusinessId());
        if (auth) {
            return(R.noAuth());
            //return;
        }
        return(R.ok().put("data", JavaBeanUtil.recordToMap(crmBusinessService.getRecord(basePageRequest))));
    }

}
