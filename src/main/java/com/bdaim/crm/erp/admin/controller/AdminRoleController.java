package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.jfinal.aop.Before;
import com.jfinal.aop.Inject;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.erp.admin.entity.AdminRole;
import com.bdaim.crm.erp.admin.entity.AdminUserRole;
import com.bdaim.crm.erp.admin.service.AdminRoleService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;

public class AdminRoleController extends Controller {
    @Inject
    private AdminRoleService adminRoleService;

    /**
     * @author wyq
     * 获取全部角色列表
     */
    @Permissions("manage:permission")
    public void getAllRoleList(){
        renderJson(R.ok().put("data",adminRoleService.getAllRoleList()));
    }

    /**
     * @author wyq
     * @param roleType 角色类型
     * 根据角色类型查询关联员工
     */
    @Permissions("manage:permission")
    public void getRoleUser(@Para("roleType") Integer roleType){
        renderJson(R.ok().put("data",adminRoleService.getRoleUser(roleType)));
    }

    /**
     * @author wyq
     * 新建
     */
    @Permissions("manage:permission")
    @Before(Tx.class)
    public void add(@Para("")AdminRole adminRole){
        renderJson(adminRoleService.save(adminRole));
    }

    /**
     * @author wyq
     * 编辑角色
     */
    @Permissions("manage:permission")
    @NotNullValidate(value = "roleId",message = "角色id不能为空")
    @NotNullValidate(value = "roleName",message = "角色名称不能为空")
    public void update(@Para("")AdminRole adminRole){
        Integer number = Db.queryInt("select count(*) from 72crm_admin_role where role_name = ? and role_type = ? and role_id != ?", adminRole.getRoleName(),adminRole.getRoleType(),adminRole.getRoleId());
        if (number > 0){
            renderJson(R.error("角色名已存在"));
        }else {
            renderJson(R.ok().put("data",adminRoleService.update(adminRole)));
        }
    }

    /**
     * 修改角色菜单
     * @author zhangzhiwei
     */
    @Permissions("manage:permission")
    public void updateRoleMenu(){
        adminRoleService.updateRoleMenu(JSON.parseObject(getRawData()));
        renderJson(R.ok());
    }

    /**
     * 查看当前登录人的权限
     * @author zhangzhiwei
     */
    public void auth(){
        renderJson(R.ok().put("data",adminRoleService.auth(BaseUtil.getUser().getUserId())));
    }
    /**
     * @author wyq
     * @param roleId 角色id
     * 复制
     */
    @Permissions("manage:permission")
    public void copy(@Para("roleId") Integer roleId){
        adminRoleService.copy(roleId);
        renderJson(R.ok());
    }

    /**
     * @author wyq
     * @param roleId 角色id
     * 删除
     */
    @Permissions("manage:permission")
    public void delete(@Para("roleId") Integer roleId){
        renderJson(adminRoleService.delete(roleId) ? R.ok() : R.error());
    }

    /**
     * @author wyq
     * @param roleId 角色项目管理角色id
     * 删除
     */
    @Permissions("manage:permission")
    public void deleteWorkRole(@Para("roleId") Integer roleId){
        renderJson(adminRoleService.deleteWorkRole(roleId) ? R.ok() : R.error());
    }

    /**
     * @author wyq
     * 关联员工
     */
    @Permissions("manage:permission")
    public void relatedUser(@Para("")AdminUserRole adminUserRole){
        renderJson(adminRoleService.relatedUser(adminUserRole));
    }

    /**
     * @author wyq
     * 解除角色关联员工
     */
    @Permissions("manage:permission")
    public void unbindingUser(@Para("") AdminUserRole adminUserRole){
        renderJson(adminRoleService.unbindingUser(adminUserRole));
    }

    /**
     * 项目管理角色列表
     * @author wyq
     */
    @Permissions("manage:permission")
    public void queryProjectRoleList(){
        renderJson(adminRoleService.queryProjectRoleList());
    }


    /**
     * 设置项目管理角色
     * @author wyq
     */
    @Permissions("manage:permission")
    public void setWorkRole(){
        JSONObject jsonObject = JSON.parseObject(getRawData());
        renderJson(adminRoleService.setWorkRole(jsonObject));
    }
}
