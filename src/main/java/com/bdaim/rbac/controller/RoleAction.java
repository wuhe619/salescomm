package com.bdaim.rbac.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.rbac.dto.Page;
import com.bdaim.rbac.dto.RoleDTO;
import com.bdaim.rbac.dto.RolesResourceDto;
import com.bdaim.rbac.service.RoleService;
import com.alibaba.fastjson.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.*;

/**
 * @author duanliying
 * @date 2019/3/14
 * @description
 */
@Controller
@RequestMapping("/role")
public class RoleAction extends BasicAction {
    public static final Logger log = LoggerFactory.getLogger(RoleAction.class);
    @Resource
    private RoleService roleService;

    /**
     * 查询职位管理列表
     */
    @RequestMapping(value = "/getRoleList", method = RequestMethod.GET)
    @ResponseBody
    public Object getRoleList(@Valid PageParam page, Long id) {
        Map<String, Object> resultMap = new HashMap<>();
        log.info("部门id参数是：" + id);
        Page list = null;
        try {
            list = roleService.getRoleList(page, id);
        } catch (Exception e) {
            log.error("查询职位管理列表异常" + e);
        }
        resultMap.put("total", list.getTotal());
        resultMap.put("list", list.getData());
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 查询职位详情页面（资源树）
     */
    @RequestMapping(value = "/getRoleDetails", method = RequestMethod.GET)
    @ResponseBody
    public String getRoleDetails(Long roleId) {
        JSONArray array = null;
        try {
            LoginUser operateUser = opUser();
            Long operateUserId = operateUser.getId();
            array = roleService.queryResourceSelectStatus(operateUserId, roleId, 0L, "admin".equals(operateUser.getName()) ? true : false);
        } catch (Exception e) {
            log.error("查询职位管理列表异常" + e);
        }
        return array.toString();
    }


    /**
     * 更新角色信息以及角色与资源的关联关系
     *
     * @return
     */
    @RequestMapping(value = "/updateRole", method = RequestMethod.POST)
    @ResponseBody
    public String updateRoleMessage(@RequestBody JSONObject json) {
        String roleName = json.getString("roleName");
        Long roleId = json.getLongValue("roleId");
        Long deptId = json.getLongValue("deptId");
        String permission = json.getString("permission");
        log.info("部门id是：" + deptId + "职位名称是：" + roleName + "权限id是：" + permission + "角色id是：" + roleId);
        boolean unique = checkUnique(roleName, roleId, deptId);
        JSONObject result = new JSONObject();
        if (!unique) {
            result.put("result", roleName + "岗位已经存在");
            result.put("code", 0);
            return result.toString();
        }
        Date modifyDate = new Date();
        LoginUser user = opUser();
        RoleDTO role = new RoleDTO(roleId);
        role.setName(roleName);
        role.setUser(user.getName());
        role.setModifyDate(modifyDate);
        role.setDeptId(deptId);
        RolesResourceDto rolesResource = new RolesResourceDto();
        rolesResource.setRole(role);
        rolesResource.setUser(user.getName());
        rolesResource.setCreateDate(modifyDate);

        if (StringUtils.isNotBlank(permission)) {
            String[] ids = permission.split(",");
            List resources = new ArrayList<>();
            for (int i = 0; i < ids.length; i++) {
                resources.add(Long.valueOf(ids[i]));
            }
            rolesResource.setResources(resources);
        }
        boolean success = roleService.updateRoleTree(rolesResource, user.getId(), "admin".equals(user.getName()) ? true : false);
        if (success) {
            result.put("result", "权限设置成功");
            result.put("code", 1);
        } else {
            result.put("result", "权限设置失败");
            result.put("code", 0);
        }
        return result.toString();
    }

    /**
     * 验证岗位名称在同一个部门是否存在
     */
    public boolean checkUnique(String roleName, Long roleId, Long deptId) {

        boolean flag = roleService.isUniqueName(roleName, roleId, deptId);
        return flag;
    }


    /**
     * 添加角色
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/saveRole", method = RequestMethod.POST)
    @ResponseBody
    public String saveRoleMessage(@RequestBody JSONObject json) {
        String deptId = json.getString("deptId");
        String roleName = json.getString("roleName");
        String permission = json.getString("permission");
        //校验角色名称是否唯一
        JSONObject result = new JSONObject();
        log.info("部门id是：" + deptId + "职位名称是：" + roleName + "权限id是：" + permission);
        boolean unique = checkUnique(roleName, null, Long.parseLong(deptId));
        if (!unique) {
            result.put("result", roleName + "岗位已经存在");
            result.put("code", 0);
            return result.toString();
        }
        //获取登陆用户
        Date modifyDate = new Date();
        LoginUser user = opUser();
        RoleDTO role = new RoleDTO();
        role.setName(roleName);
        role.setUser(user.getName());
        role.setModifyDate(modifyDate);
        role.setDeptId(Long.parseLong(deptId));
        RolesResourceDto rolesResource = new RolesResourceDto();
        rolesResource.setRole(role);
        rolesResource.setUser(user.getName());
        rolesResource.setCreateDate(modifyDate);
        if (StringUtils.isNotBlank(permission)) {
            String[] ids = permission.split(",");
            List resources = new ArrayList<>();
            for (int i = 0; i < ids.length; i++) {
                resources.add(Long.valueOf(ids[i]));
            }
            rolesResource.setResources(resources);
        }
        boolean success = roleService.addRoleMessage(rolesResource);
        if (success) {
            result.put("result", "职位设置成功");
            result.put("code", 1);
        } else {
            result.put("result", "职位设置失败");
            result.put("code", 0);
        }
        return returnJsonData(result);
    }


    /**
     * 查询被操作用户拥有的部门角色树根据部门id
     *
     * @return
     */
    @RequestMapping(value = "/queryRoleTree", method = RequestMethod.GET)
    @ResponseBody
    public String queryRoleTree(Long deptId, Long userId) {
        List<RoleDTO> roles = roleService.queryRoleByDept(deptId, userId);
        if (roles == null || roles.size() == 0) {
            return "[]";
        } else {
            JSONArray array = new JSONArray();
            JSONObject item = null;
            for (RoleDTO info : roles) {
                item = new JSONObject();
                item.put("id", info.getId());
                item.put("name", info.getName());
                array.add(item);
            }
            return array.toString();
        }
    }

    /**
     * 查询后台用户拥有的资源树
     */
    @RequestMapping(value = "queryResourceTree", method = RequestMethod.GET)
    @ResponseBody
    public String queryResourceTree(String platform) {
        //平台 1-精准营销 2-金融超市 3-信函
        LoginUser operateUser = opUser();
        Long userId = operateUser.getId();
        //根据用户角色查询当前用户拥有的资源树
        JSONArray jsonArray = null;
        try {
            jsonArray = roleService.queryResourceTreeByRole(userId, 0L,platform);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("查询当前登录用户资源树异常" + e);
        }
        return jsonArray.toString();
    }

    /**
     * 根据角色id查询用户列表
     */
    @RequestMapping(value = "/queryUserList", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo queryUserListByRoleId(String id) {

        try {
            List<Map<String, Object>> list = roleService.queryUserListByRoleId(id);
            return new ResponseInfoAssemble().success(list);
        } catch (Exception e) {
            log.error("根据角色id查询用户列表异常", e);
            return new ResponseInfoAssemble().failure(-1, "根据角色id查询用户列表失败");
        }
    }
}
