package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.admin.entity.CrmBusinessType;
import com.bdaim.crm.erp.admin.service.AdminBusinessTypeService;
import com.bdaim.crm.utils.R;
import com.bdaim.crm.utils.TagUtil;

import java.util.HashSet;
import java.util.List;

/**
 * 商机组设置
 *
 * @author hmb
 */
public class AdminBusinessTypeController extends Controller {

    @Inject
    private AdminBusinessTypeService adminBusinessTypeService;

    /**
     * @author hmb
     * 设置商机组
     */
    @Permissions("manage:crm")
    public void setBusinessType() {
        JSONObject jsonObject = JSON.parseObject(getRawData());

        CrmBusinessType crmBusinessType = jsonObject.getObject("crmBusinessType", CrmBusinessType.class);
        if(jsonObject.getJSONArray("deptIds") != null){
            List<Integer> deptIds = jsonObject.getJSONArray("deptIds").toJavaList(Integer.class);
            crmBusinessType.setDeptIds(TagUtil.fromSet(new HashSet<>(deptIds)));
        }
        JSONArray crmBusinessStatus = jsonObject.getJSONArray("crmBusinessStatus");
        adminBusinessTypeService.addBusinessType(crmBusinessType,crmBusinessStatus);
        renderJson(R.ok());
    }

    /**
     * @author hmb
     * @param basePageRequest 分页对象
     * 查询商机组列表
     */

    @Permissions("manage:crm")
    public void queryBusinessTypeList(BasePageRequest<Void> basePageRequest) {
        renderJson(R.ok().put("data", adminBusinessTypeService.queryBusinessTypeList(basePageRequest)));
    }

    /**
     * @author hmb
     * 获取详细信息
     */
    @Permissions("manage:crm")
    public void getBusinessType() {
        String typeId = getPara("id");
        renderJson(adminBusinessTypeService.getBusinessType(typeId));
    }

    /**
     * @author hmb
     * 删除商机状态组
     */
    @Permissions("manage:crm")
    public void deleteById() {
        String typeId = getPara("id");
        renderJson(adminBusinessTypeService.deleteById(typeId));
    }


}
