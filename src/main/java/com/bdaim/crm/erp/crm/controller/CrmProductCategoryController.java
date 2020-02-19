package com.bdaim.crm.erp.crm.controller;

import com.bdaim.crm.entity.LkCrmProductCategoryEntity;
import com.bdaim.crm.erp.crm.service.CrmProductCategoryService;
import com.bdaim.crm.utils.R;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@RequestMapping("/CrmProductCategory")
public class CrmProductCategoryController extends Controller {

    @Resource
    private CrmProductCategoryService crmProductCategoryService;

    /**
     * 根据pid(父级id)查询类别
     *
     * @author zxy
     */
    @RequestMapping(value = "querylist", method = RequestMethod.POST)
    public R querylist(@Para("pid") Integer pid) {
        if (pid == null) {
            pid = 0;
        }
        return (R.ok().put("data", crmProductCategoryService.queryListByPid(pid)));
    }

    /**
     * 根据id查询类别
     *
     * @author zxy
     */
    @RequestMapping(value = "queryById", method = RequestMethod.POST)
    public R queryById(@Para("id") Integer id) {
        return (R.ok().put("data", crmProductCategoryService.queryById(id)));

    }

    /**
     * 添加或修改类别
     *
     * @author zxy
     */
    @RequestMapping(value = "saveAndUpdate", method = RequestMethod.POST)
    public R saveAndUpdate(@Para("") LkCrmProductCategoryEntity category) {
        return (crmProductCategoryService.saveAndUpdate(category));
    }

    /**
     * 递归查询全部产品类别
     *
     * @author zxy
     */
    @RequestMapping(value = "queryList", method = RequestMethod.POST)
    public R queryList() {
        return (crmProductCategoryService.queryList());
    }

    /**
     * 根据ID删除类别
     *
     * @author zxy
     */
    @RequestMapping(value = "deleteById", method = RequestMethod.POST)
    public R deleteById(@Para("id") Integer id) {
        return (crmProductCategoryService.deleteById(id));
    }
}
