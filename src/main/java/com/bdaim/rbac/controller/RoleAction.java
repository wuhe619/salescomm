package com.bdaim.rbac.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.rbac.dto.*;
import com.bdaim.rbac.service.DeptService;
import com.bdaim.rbac.service.ResourceService;
import com.bdaim.rbac.service.RoleService;
import com.bdaim.rbac.vo.DeptInfo;
import com.bdaim.rbac.vo.QueryDataParam;
import com.bdaim.rbac.vo.RoleInfo;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
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
    public String getRoleDetails(Long roleId,String platform) {
        //平台 1-精准营销 2-金融超市 3-信函
        JSONArray array = null;
        try {
            LoginUser operateUser = opUser();
            Long operateUserId = operateUser.getId();
            array = roleService.queryResourceSelectStatus(operateUserId, roleId, 0L, "admin".equals(operateUser.getName()) ? true : false,platform);
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

    @Resource
    private ResourceService resourceService;
    @Resource
    private DeptService deptService;

    /**
     * 分页查询角色，但是该用户只能查询自己所在部门的角色
     *
     * @param countPerPage
     * @return
     */
    @RequestMapping(value = "/query.do")
    @ResponseBody
    public String query(@RequestParam(required = false) String condition, @RequestParam(required = false) Integer pageIndex, @RequestParam int countPerPage, HttpServletRequest request) {
        Page page = new Page();
        if (pageIndex == null) {
            page.setPageIndex(0);
        } else {
            page.setPageIndex(pageIndex);
        }
        page.setCountPerPage(countPerPage);


        QueryDataParam param = new QueryDataParam();
        param.setUserId(opUser().getId());
        param.setCondition(condition);
        param.setPage(page);
        List<RoleInfo> list = roleService.queryRoleV1(param);
        net.sf.json.JSONArray array = net.sf.json.JSONArray.fromObject(list == null ? new ArrayList<RoleInfo>() : list);
        net.sf.json.JSONObject o = new net.sf.json.JSONObject();
        o.put("roles", array);
        o.put("count", page.getCount());

        this.operlog(-1, this.pageName);

        return o.toString();
    }


    /**
     * 第一次分页查询角色，会返回部门信息
     *
     * @param deptId
     * @param pageIndex
     * @param countPerPage
     * @return
     */
    @RequestMapping(value = "/queryFirst.do")
    @ResponseBody
    public String queryFirst(@RequestParam(required = false) String condition, @RequestParam(required = false) Long deptId, @RequestParam(required = false) Integer pageIndex, @RequestParam int countPerPage) {
        Page page = new Page();
        if (pageIndex == null) page.setPageIndex(1);
        else page.setPageIndex(pageIndex);
        page.setCountPerPage(countPerPage);
        QueryDataParam param = new QueryDataParam();
        param.setCondition(condition);
        if (deptId != null && deptId != 0) {
            param.setDeptId(deptId);
        }
        //param.setDeptId(deptId);
        param.setPage(page);
        List<RoleInfo> list = roleService.queryRole(param);
        //获取所有的部门
        //List<Dept> depts = deptService.queryDept();
        List<DeptInfo> depts = deptService.queryAll();
        net.sf.json.JSONArray d = net.sf.json.JSONArray.fromObject(depts == null ? new ArrayList<DeptInfo>() : depts);
        net.sf.json.JSONArray array = net.sf.json.JSONArray.fromObject(list == null ? new ArrayList<RoleInfo>() : list);
        net.sf.json.JSONObject o = new net.sf.json.JSONObject();
        o.put("depts", d);
        o.put("roles", array);
        o.put("count", page.getCount());
        return o.toString();
    }

    /**
     * 删除角色
     *
     * @param roleId
     * @return
     */
    @RequestMapping(value = "/del.do")
    @ResponseBody
    public String delRole(HttpServletRequest request, @RequestParam Long roleId) {

        boolean flag = roleService.delRole(roleId);
        if (null != roleId) {
            super.operlog(roleId, this.pageName);
        }

        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        if (flag) {
            result.put("result", true);
            result.put("msg", "刪除成功");
        } else {
            result.put("result", false);
            result.put("msg", "现在的岗位正在被使用中，无法被删除");
        }
        return result.toString();
    }

    /**
     * 添加角色
     *
     * @param deptId
     * @param roleName
     * @param permission
     * @param request
     * @return
     */
    @RequestMapping(value = "save0.do")
    @ResponseBody
    public String save0(@RequestParam Long deptId, @RequestParam String roleName, @RequestParam(required = false) String permission, HttpServletRequest request) {
        //校验角色名称是否唯一
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        boolean unique = checkUnique(roleName, null, deptId);
        if (!unique) {
            result.put("result", false);
            result.put("code", 1);
            return result.toString();
        }
        //获取登陆用户
        LoginUser user = opUser();
        RoleDTO role = new RoleDTO();
        role.setName(roleName);
        role.setUser(user.getName());
        role.setCreateDate(new Date());
        //role.setType(ManagerType.getManagerType(type));
        RolesResource rolesResource = new RolesResource();
        rolesResource.setRole(role);
        rolesResource.setCreateDate(new Date());
        rolesResource.setUser(user.getName());
        role.setDeptId(deptId);

        if (StringUtils.isNotBlank(permission)) {
            String[] ids = permission.split(",");
            List<AbstractTreeResource> resources = new ArrayList<AbstractTreeResource>();
            for (int i = 0; i < ids.length; i++) {
                resources.add(new CommonTreeResource(Long.valueOf(ids[i])));
            }
            rolesResource.setResources(resources);
        }
        boolean success = roleService.addRole(rolesResource);
        if (success) {
            RoleInfo info = roleService.queryRoleInfo(role.getKey());
            result.put("result", true);
            result.put("data", net.sf.json.JSONObject.fromObject(info));
        } else {
            result.put("result", false);
            result.put("code", 2);
        }

        super.operlog(-1, this.pageName);

        return result.toString();
    }


    /**
     * 添加角色
     *
     * @param deptId
     * @param roleName
     * @param permission
     * @param request
     * @return
     */
    @RequestMapping(value = "save.do")
    @ResponseBody
    public String save(@RequestParam Long deptId, @RequestParam String roleName, @RequestParam(required = false) String permission, HttpServletRequest request) {
        //校验角色名称是否唯一
        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        boolean unique = checkUnique(roleName, null, deptId);
        if (!unique) {
            result.put("result", false);
            result.put("code", 1);
            return returnJsonData(result);
        }
        //获取登陆用户
        Date createTime = new Date();
        LoginUser user = opUser();
        RoleDTO role = new RoleDTO();
        role.setName(roleName);
        role.setUser(user.getName());
        role.setCreateDate(createTime);
        //role.setType(ManagerType.getManagerType(type));
        RolesResource rolesResource = new RolesResource();
        rolesResource.setRole(role);
        rolesResource.setCreateDate(createTime);
        rolesResource.setUser(user.getName());
        role.setDeptId(deptId);

        if (StringUtils.isNotBlank(permission)) {
            String[] ids = permission.split(",");
            List<AbstractTreeResource> resources = new ArrayList<>();
            for (int i = 0; i < ids.length; i++) {
                resources.add(new CommonTreeResource(Long.valueOf(ids[i])));
            }
            rolesResource.setResources(resources);
        }
        boolean success = roleService.addRole(rolesResource);
        if (success) {
            RoleInfo info = roleService.queryRoleInfo(role.getKey());
            result.put("result", true);
            result.put("data", net.sf.json.JSONObject.fromObject(info));
        } else {
            result.put("result", false);
            result.put("code", 2);
        }
        super.operlog(-1, this.pageName);
        return returnJsonData(result);
    }

    /**
     * 编辑角色，需要查询在操作人拥有的资源范围内被操作角色的资源分配情况
     *
     * @param request
     * @param roleId
     * @return
     */
    @RequestMapping(value = "edit.do")
    @ResponseBody
    public String edit(HttpServletRequest request, @RequestParam Long roleId) {
        LoginUser operateUser = opUser();
        Long operateUserId = operateUser.getId();
        net.sf.json.JSONArray array = resourceService.queryResourceSelectStatus(operateUserId, roleId, 0L, "admin".equals(operateUser.getName()) ? true : false);
        return array.toString();
    }

    /**
     * 跟新角色信息以及角色与资源的关联关系
     *
     * @param roleName
     * @param roleId
     * @param permission
     * @param request
     * @return
     */
    @RequestMapping(value = "update.do")
    @ResponseBody
    public String update(@RequestParam String roleName, @RequestParam Long roleId, @RequestParam Long deptId, @RequestParam(required = false) String permission, HttpServletRequest request) {
        com.alibaba.fastjson.JSONObject result = new com.alibaba.fastjson.JSONObject();
        //RoleInfo info = roleService.queryRoleInfo(roleId);
        boolean unique = checkUnique(roleName, roleId, deptId);
        if (!unique) {
            result.put("result", false);
            result.put("code", 1);
            return returnJsonData(result);
        }
        Date modifyDate = new Date();
        LoginUser user = opUser();
        RoleDTO role = new RoleDTO(roleId);
        role.setName(roleName);
        role.setUser(user.getName());
        role.setModifyDate(modifyDate);
        role.setDeptId(deptId);
        //view.addObject("role",role);
        RolesResource rolesResource = new RolesResource();
        rolesResource.setRole(role);
        rolesResource.setUser(user.getName());
        rolesResource.setCreateDate(modifyDate);

        if (StringUtils.isNotBlank(permission)) {
            String[] ids = permission.split(",");
            List<AbstractTreeResource> resources = new ArrayList<AbstractTreeResource>();
            for (int i = 0; i < ids.length; i++) {
                resources.add(new CommonTreeResource(Long.valueOf(ids[i])));
            }
            rolesResource.setResources(resources);
        }
        boolean success = roleService.updateRRP(rolesResource, user.getId(), "admin".equals(user.getName()) ? true : false);
        if (success) {
            result.put("result", true);
            RoleInfo info = roleService.queryRoleInfo(roleId);
            result.put("data", info);

        } else {
            result.put("result", false);
            result.put("code", 2);
        }

        if (null != roleId) {
            super.operlog(roleId, this.pageName);
        }
        return returnJsonData(result);
    }

    /**
     * 查询后台用户拥有的资源树
     */
    @RequestMapping(value = "queryResourceTreeV1.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryResourceTree(HttpServletRequest request) {

        LoginUser operateUser = opUser();
        Long operateUserId = operateUser.getId();
        String resources = resourceService.resources(operateUserId, 0L, operateUser.getRole());
        //operation logs
        super.operlog(0, pageName);

        return resources;
    }

    /**
     * 查询后台用户拥有的资源树
     */
    @RequestMapping(value = "queryResourceTree.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryResourceTreeV1(HttpServletRequest request, String type) {
        LoginUser operateUser = opUser();
        Long operateUserId = operateUser.getId();
        int platform = 1;
        if ("2".equals(operateUser.getAuthorize())) {
            platform = 2;
        }
        if (StringUtil.isNotEmpty(type)) {
            platform = NumberConvertUtil.parseInt(type);
        }
        net.sf.json.JSONArray resources = resourceService.listTreeResource(operateUserId, 0L, platform, operateUser.isAdmin());
        //operation logs
        super.operlog(0, pageName);
        return JSON.toJSONString(resources);
    }


    /**
     * 直接把数据权限添加到t_mpr_rel表
     *
     * @param roleId
     * @param type   4-企业客户  5-供应商客户
     * @param relId
     * @return
     * @throws Exception
     */
    @ValidatePermission(role = "admin")
    @RequestMapping(value = "/addRoleDataPermission")
    @ResponseBody
    public String addRoleDataPermission(String roleId, String type, String relId) throws Exception {
        if (StringUtil.isEmpty(roleId) || StringUtil.isEmpty(type) || StringUtil.isEmpty(relId)) {
            throw new TouchException("参数错误");
        }
        LoginUser lu = opUser();
        if (!"admin".equals(lu.getRole())) {
            throw new TouchException("无权限");
        }
        return roleService.insertIntoRoleDataPermission(roleId, Integer.valueOf(type), relId, lu.getName());
    }
}
