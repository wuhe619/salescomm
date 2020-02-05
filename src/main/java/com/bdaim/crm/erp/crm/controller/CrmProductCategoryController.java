package com.bdaim.crm.erp.crm.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.bdaim.crm.erp.crm.entity.CrmProductCategory;
import com.bdaim.crm.erp.crm.service.CrmProductCategoryService;
import com.bdaim.crm.utils.R;

import javax.annotation.Resource;

public class CrmProductCategoryController extends Controller {
    @Resource
    private CrmProductCategoryService crmProductCategoryService;
    /**
     * 根据pid(父级id)查询类别
     * @author zxy
     */
    public void  querylist(@Para("pid") Integer pid){
        if (pid == null){
            pid = 0;
        }
        renderJson(R.ok().put("data",crmProductCategoryService.queryListByPid(pid)));
    }
    /**
     * 根据id查询类别
     * @author zxy
     */
    public void queryById(@Para("id") Integer id){
        renderJson(R.ok().put("data",crmProductCategoryService.queryById(id)));

    }
    /**
     * 添加或修改类别
     * @author zxy
     */
    public void saveAndUpdate(@Para("") CrmProductCategory category){
        renderJson(crmProductCategoryService.saveAndUpdate(category));
    }
    /**
     * 递归查询全部产品类别
     * @author zxy
     */
    public void queryList(){
        renderJson(crmProductCategoryService.queryList());
    }
    /**
     * 根据ID删除类别
     * @author zxy
     */
    public void deleteById(@Para("id") Integer id){
        renderJson(crmProductCategoryService.deleteById(id));
    }
}