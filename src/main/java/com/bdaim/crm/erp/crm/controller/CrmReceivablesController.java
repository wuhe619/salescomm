package com.bdaim.crm.erp.crm.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.erp.crm.entity.CrmReceivables;
import com.bdaim.crm.erp.crm.service.CrmReceivablesService;
import com.bdaim.crm.utils.R;

import javax.annotation.Resource;

public class CrmReceivablesController extends Controller {

    @Resource
    private CrmReceivablesService crmReceivablesService;

    @Resource
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:receivables:index"})
    public void queryPageList(BasePageRequest basePageRequest){
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type",7);
        basePageRequest.setJsonObject(jsonObject);
        renderJson(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**@author zxy
     * 分页查询回款
     */
    public void  queryPage(BasePageRequest<CrmReceivables> basePageRequest){
        renderJson(R.ok().put("data",crmReceivablesService.queryPage(basePageRequest)));
    }
    /**
     * @author zxy
     * 添加或者修改
     */
    @Permissions({"crm:receivables:save","crm:receivables:update"})
    public void  saveOrUpdate(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(crmReceivablesService.saveOrUpdate(jsonObject));
    }
    /**
     * @author zxy
     * 根据回款id查询
     */
    @Permissions("crm:receivables:read")
    @NotNullValidate(value = "receivablesId",message = "回款id不能为空")
    public void  queryById(@Para("receivablesId") Integer receivablesId){
        renderJson(crmReceivablesService.queryById(receivablesId));
    }
    /**
     * @author zxy
     * 根据回款id删除
     */
    @Permissions("crm:receivables:delete")
    @NotNullValidate(value = "receivablesIds",message = "回款id不能为空")
    public void  deleteByIds(@Para("receivablesIds") String receivablesIds){
        renderJson(crmReceivablesService.deleteByIds(receivablesIds));
    }

    /**
     * 根据条件查询回款
     * @author zxy
     */
    public void queryListByType(@Para("type") String type, @Para("id") Integer id ){
        renderJson(R.ok().put("data",crmReceivablesService.queryListByType(type,id)));
    }
    /**
     * 根据条件查询回款
     * @author zxy
     */
    public void queryList(@Para("") CrmReceivables receivables){
        renderJson(R.ok().put("data",crmReceivablesService.queryList(receivables)));
    }
}
