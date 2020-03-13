package com.bdaim.crm.erp.admin.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.entity.LkCrmAdminRoleEntity;
import com.bdaim.crm.entity.LkCrmAdminUserRoleEntity;
import com.bdaim.crm.erp.admin.entity.AdminUserRole;
import com.bdaim.crm.erp.admin.service.LkAdminRoleService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/system/role")
public class AdminRoleController extends BasicAction {
    @Resource
    private LkAdminRoleService adminRoleService;

    /**
     * @author wyq
     * 获取全部角色列表
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/getAllRoleList", method = RequestMethod.POST)
    public R getAllRoleList(String roleName, Integer roleType) {
        return (R.ok().put("data", adminRoleService.getAllRoleList(roleName, roleType)));
    }

    /**
     * @param roleType 角色类型
     *                 根据角色类型查询关联员工
     * @author wyq
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/getRoleUser", method = RequestMethod.POST)
    public R getRoleUser(@Para("roleType") Integer roleType) {
        return (R.ok().put("data", adminRoleService.getRoleUser(roleType)));
    }

    /**
     * @author wyq
     * 新建
     */
    @Permissions("manage:permission")
    @Before(Tx.class)
    @RequestMapping(value = "/add", method = RequestMethod.POST)
    public R add(LkCrmAdminRoleEntity adminRole) {
        return (adminRoleService.save(adminRole));
    }

    /**
     * @author wyq
     * 编辑角色
     */
    @Permissions("manage:permission")
    @NotNullValidate(value = "roleId", message = "角色id不能为空")
    @NotNullValidate(value = "roleName", message = "角色名称不能为空")
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    public R update(@Para("") LkCrmAdminRoleEntity adminRole) {
        Integer number = adminRoleService.checkCustRoleExist(adminRole.getRoleName(), adminRole.getRoleType(), adminRole.getRoleId(), BaseUtil.getCustId());
        if (number > 0) {
            return (R.error("角色名已存在"));
        } else {
            return (R.ok().put("data", adminRoleService.update(adminRole)));
        }
    }

    /**
     * 修改角色菜单
     *
     * @author zhangzhiwei
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/updateRoleMenu", method = RequestMethod.POST)
    public R updateRoleMenu(@RequestBody JSONObject jsonObject) {
        adminRoleService.updateRoleMenu(jsonObject);
        return (R.ok());
    }

    /**
     * 查看当前登录人的权限
     *
     */
    @RequestMapping(value = "/auth", method = RequestMethod.POST)
    public R auth() {
        return (R.ok().put("data", adminRoleService.auth(BaseUtil.getUser().getUserId())));
    }

    @RequestMapping(value = "/auth0", method = RequestMethod.POST)
    public R auth0() {
        JSONObject jsonObject = JSON.parseObject("{\"work\":{\"task\":{\"save\":true},\"work\":{\"update\":true},\"taskClass\":{\"save\":true,\"update\":true,\"delete\":true}},\"bi\":{\"product\":{\"read\":true},\"oa\":{\"read\":true},\"performance\":{\"read\":true},\"business\":{\"read\":true},\"funnel\":{\"read\":true},\"achievement\":{\"read\":true},\"employe\":{\"read\":true},\"receivables\":{\"read\":true},\"ranking\":{\"read\":true},\"portrait\":{\"read\":true},\"customer\":{\"read\":true}},\"crm\":{\"product\":{\"read\":true,\"excelexport\":false,\"save\":true,\"update\":true,\"index\":true,\"excelimport\":false,\"status\":true},\"business\":{\"read\":true,\"transfer\":true,\"teamsave\":true,\"save\":true,\"update\":true,\"index\":true,\"delete\":true},\"leads\":{\"transform\":true,\"read\":true,\"transfer\":true,\"excelexport\":true,\"save\":true,\"update\":true,\"index\":true,\"excelimport\":true,\"delete\":true,\"lock\":true},\"publicsea\":{\"distribute\":true,\"fastdistribute\":true,\"getselect\":true,\"fastget\":true,\"read\":true,\"excelexport\":true,\"save\":true,\"update\":true,\"index\":true,\"excelimport\":true,\"delete\":true,\"lock\":true},\"contract\":{\"read\":true,\"transfer\":true,\"teamsave\":true,\"save\":true,\"update\":true,\"index\":true,\"delete\":true},\"pool\":{\"receive\":true,\"excelexport\":true,\"index\":true,\"distribute\":true},\"receivables\":{\"read\":true,\"save\":true,\"update\":true,\"index\":true,\"delete\":true},\"contacts\":{\"read\":true,\"transfer\":true,\"excelexport\":true,\"save\":true,\"update\":true,\"index\":true,\"excelimport\":true,\"delete\":true},\"customer\":{\"receive\":true,\"read\":true,\"teamsave\":true,\"save\":true,\"pool\":true,\"update\":true,\"index\":true,\"excelimport\":true,\"putinpool\":true,\"delete\":true,\"transfer\":true,\"excelexport\":true,\"lock\":true,\"distribute\":true}},\"manage\":{\"oa\":true,\"system\":true,\"examineFlow\":true,\"permission\":true,\"user\":true,\"crm\":true}}");
        // 管理员
        JSONObject crm = jsonObject.getJSONObject("crm");
        if (BaseUtil.getUserType() == 1) {
            crm.getJSONObject("pool").put("get", false);
            crm.getJSONObject("pool").put("receive", false);
            crm.getJSONObject("publicsea").put("getselect", false);
            crm.getJSONObject("publicsea").put("fastget", false);
        } else if (BaseUtil.getUserType() == 2) {
            crm.getJSONObject("pool").put("get", true);
            crm.getJSONObject("pool").put("receive", true);
            crm.getJSONObject("pool").put("distribute", false);
            crm.getJSONObject("pool").put("delete", false);
            crm.getJSONObject("pool").put("excelexport", false);
            crm.getJSONObject("publicsea").put("distribute", false);
            crm.getJSONObject("publicsea").put("fastdistribute", false);
        }
        jsonObject.put("crm", crm);
        return (R.ok().put("data", jsonObject));
    }

    /**
     * @param roleId 角色id
     *               复制
     * @author wyq
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/copy", method = RequestMethod.POST)
    public R copy(@Para("roleId") Integer roleId) {
        adminRoleService.copy(roleId);
        return (R.ok());
    }

    /**
     * @param roleId 角色id
     *               删除
     * @author wyq
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/delete", method = RequestMethod.POST)
    public R delete(@Para("roleId") Integer roleId) {
        return (adminRoleService.delete(roleId) ? R.ok() : R.error());
    }

    /**
     * @param roleId 角色项目管理角色id
     *               删除
     * @author wyq
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/deleteWorkRole", method = RequestMethod.POST)
    public R deleteWorkRole(@Para("roleId") Integer roleId) {
        return (adminRoleService.deleteWorkRole(roleId) ? R.ok() : R.error());
    }

    /**
     * @author wyq
     * 关联员工
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/relatedUser", method = RequestMethod.POST)
    public R relatedUser(LkCrmAdminUserRoleEntity adminUserRole) {
        return (adminRoleService.relatedUser(adminUserRole));
    }

    /**
     * @author wyq
     * 解除角色关联员工
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/unbindingUser", method = RequestMethod.POST)
    public R unbindingUser(@Para("") AdminUserRole adminUserRole) {
        return (adminRoleService.unbindingUser(adminUserRole));
    }

    /**
     * 项目管理角色列表
     *
     * @author wyq
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/queryProjectRoleList", method = RequestMethod.POST)
    public R queryProjectRoleList() {
        return (adminRoleService.queryProjectRoleList());
    }


    /**
     * 设置项目管理角色
     *
     * @author wyq
     */
    @Permissions("manage:permission")
    @RequestMapping(value = "/setWorkRole", method = RequestMethod.POST)
    public R setWorkRole(@RequestBody JSONObject jsonObject) {
        //JSONObject jsonObject = JSON.parseObject(getRawData());
        return (adminRoleService.setWorkRole(jsonObject));
    }
}
