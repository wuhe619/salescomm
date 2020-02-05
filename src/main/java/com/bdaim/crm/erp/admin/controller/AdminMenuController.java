package com.bdaim.crm.erp.admin.controller;

import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Db;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.erp.admin.entity.AdminMenu;
import com.bdaim.crm.erp.admin.service.AdminMenuService;
import com.bdaim.crm.utils.R;

import javax.annotation.Resource;

public class AdminMenuController extends Controller {
    @Resource
    private AdminMenuService adminMenuService;

    /**
     * @author wyq
     * @param roleId 角色id
     * 根据角色id查询菜单id
     */
    @Permissions("manage:permission")
    public void getRoleMenu(@Para("roleId") Integer roleId){
        renderJson(R.ok().put("data",adminMenuService.getMenuIdByRoleId(roleId)));
    }

    /**
     * @author wyq
     * 展示全部菜单
     */
    @Permissions("manage:permission")
    public void getAllMenuList(){
        renderJson(R.ok().put("data",adminMenuService.getAllMenuList(0,20)));
    }

    /**
     * @author hmb
     * 展示全部菜单
     */
    @Permissions("manage:permission")
    public void getWorkMenuList(){
        Integer workMenuId = Db.queryInt("select menu_id from `72crm_admin_menu` where parent_id = 0 and realm = 'work'");
        AdminMenu root = new AdminMenu().findById(workMenuId);
        root.put("childMenu",adminMenuService.getWorkMenuList(root.getMenuId(),20));
        renderJson(R.ok().put("data",root));
    }
}
