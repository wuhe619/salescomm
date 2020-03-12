package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.interceptor.ClassTypeCheck;
import com.bdaim.crm.entity.LkCrmBusinessTypeEntity;
import com.bdaim.crm.erp.admin.service.AdminBusinessTypeService;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;

/**
 * 商机组设置
 *
 */
@RestController
@RequestMapping("/businessType")
public class AdminBusinessTypeController extends BasicAction {

    @Resource
    private AdminBusinessTypeService adminBusinessTypeService;

    /**
     * @author hmb
     * 设置商机组
     */
    @Permissions("manage:crm")
    @RequestMapping(value = "/setBusinessType", method = RequestMethod.POST)
    public R setBusinessType(@RequestBody JSONObject jsonObject) {
        LkCrmBusinessTypeEntity crmBusinessType = jsonObject.getObject("crmBusinessType", LkCrmBusinessTypeEntity.class);
        if (jsonObject.getJSONArray("deptIds") != null) {
            List<Integer> deptIds = jsonObject.getJSONArray("deptIds").toJavaList(Integer.class);
            crmBusinessType.setDeptIds(TagUtil.fromSet(new HashSet<>(deptIds)));
        }
        JSONArray crmBusinessStatus = jsonObject.getJSONArray("crmBusinessStatus");
        adminBusinessTypeService.addBusinessType(crmBusinessType, crmBusinessStatus);
        return(R.ok());
    }

    /**
     * @author hmb
     * @param basePageRequest 分页对象
     * 查询商机组列表
     */

    @Permissions("manage:crm")
    @RequestMapping(value = "/queryBusinessTypeList", method = RequestMethod.POST)
    @ClassTypeCheck(classType = Void.class)
    public R queryBusinessTypeList(BasePageRequest<Void> basePageRequest) {
        return(R.ok().put("data", adminBusinessTypeService.queryBusinessTypeList(basePageRequest)));
    }

    /**
     * @author hmb
     * 获取详细信息
     */
    @Permissions("manage:crm")
    @RequestMapping(value = "/getBusinessType", method = RequestMethod.POST)
    public R getBusinessType() {
        String typeId = getPara("id");
        return(adminBusinessTypeService.getBusinessType(typeId));
    }

    /**
     * @author hmb
     * 删除商机状态组
     */
    @Permissions("manage:crm")
    @RequestMapping(value = "/deleteById", method = RequestMethod.POST)
    public R deleteById() {
        String typeId = getPara("id");
        return(adminBusinessTypeService.deleteById(typeId));
    }


}
