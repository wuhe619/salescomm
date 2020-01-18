package com.bdaim.rbac.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.auth.entity.UserVerificationCode;
import com.bdaim.auth.service.UserVerificationCodeService;
import com.bdaim.auth.util.ResponseResult;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.industry.service.CustomerIndustryPoolService;
import com.bdaim.rbac.DataFromEnum;
import com.bdaim.rbac.dto.*;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.rbac.service.DeptService;
import com.bdaim.rbac.service.ResourceService;
import com.bdaim.rbac.service.RoleService;
import com.bdaim.rbac.service.UserService;
import com.bdaim.rbac.service.impl.UserInfoService;
import com.bdaim.rbac.vo.QueryDataParam;
import com.bdaim.rbac.vo.RoleInfo;
import com.bdaim.rbac.vo.UserInfo;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.util.*;

import net.sf.json.JSONArray;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import sun.net.util.IPAddressUtil;

import javax.annotation.Resource;
import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.math.BigInteger;
import java.util.*;

@Controller
@RequestMapping("/user")
public class UserAction extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(UserAction.class);

    @Resource
    private RoleService roleService;

    @Resource
    private DeptService deptService;

    String defaultPassword = "tag123456";

    @Resource
    CustomerIndustryPoolService customerIndustryPoolService;

    @Resource
    private SendSmsService sendSmsService;
    @Resource
    private ResourceService resourceService;


    @Resource
    private UserService userService;
    @Resource
    UserInfoService userInfoService;
    @Resource
    UserVerificationCodeService userVerificationCodeService;
    UserVerificationCode userVerificationCode = null;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private CustomerService customerService;
    @Resource
    private TokenCacheService<LoginUser> tokenCacheService;

    @RequestMapping(value = "/identify/check", method = RequestMethod.GET)
    @ResponseBody
    public Object identifyCheck(String type, String condition) throws TouchException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("data", JSONObject.toJSON(userInfoService.getUserByCondition(type, condition)));
        return JSON.toJSONString(resultMap);
    }

    @RequestMapping(value = "/verify/code")
    public void getLoginVerifyCode(HttpServletRequest request, HttpServletResponse response) {
        String uuid = request.getParameter("uuid");
        if (StringUtil.isEmpty(uuid)) {
            return;
        }
        BufferedImage image = VerifyUtil.getLoginVerifyCode(100, 38,uuid);
        try {
            ImageIO.write(image, "PNG", response.getOutputStream());
        } catch (IOException e) {
            logger.error("生成登录图片验证码错误：" + e);
            e.printStackTrace();
        }
    }


    @RequestMapping(value = "/update/password", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo updatePassword(@RequestBody JSONObject param) {
        String oldPwd = param.getString("oldPassword");
        String newPwd = param.getString("newPassword");
        int pwdLevel = param.getIntValue("pwdLevel");
        try {
            if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
                //userInfoService.updateFrontPwd(opUser().getId(), oldPwd, newPwd, pwdLevel);
                userInfoService.updatePwd(opUser().getId(), oldPwd, newPwd, pwdLevel);
            } else {
                //userInfoService.updatePwd(opUser().getId(), oldPwd, newPwd, pwdLevel);
                userInfoService.updateFrontPwd(opUser().getId(), oldPwd, newPwd, pwdLevel);
            }
        } catch (Exception e) {
            logger.error("修改密码异常");
            return new ResponseInfoAssemble().failure(-1, "修改密码失败");
        }
        return new ResponseInfoAssemble().success(null);
    }

    @RequestMapping(value = "/securityCenter/", method = RequestMethod.GET)
    @ResponseBody
    public Object securityCenter() throws Exception {
        long userId = opUser().getId();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", JSONObject.toJSON(userInfoService.getSecurityCenterInfo(userId)));
        return JSONObject.toJSON(resultMap);
    }


    @RequestMapping(value = "/status/change", method = RequestMethod.PUT)
    @ResponseBody
    public Object changeUserStatus(@RequestBody JSONObject param) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String userId = param.getString("userId");
        String action = param.getString("action");
        if (StringUtil.isEmpty(userId)) {
            throw new TouchException("300", "用户id不能为空");
        }
        try {
            userInfoService.changeUserStatus(userId, action);
        } catch (TouchException e) {
            throw new TouchException(e.getCode(), e.getMessage());
        }
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/industrypool/list", method = RequestMethod.GET)
    @ResponseBody
    public Object queryIndustryPoolByCondition(HttpServletRequest param) throws Exception {
        return JSONObject.toJSON(userInfoService.queryIndustryPoolByCondition(param));
    }

    @RequestMapping(value = "/industrypool/detail", method = RequestMethod.GET)
    @ResponseBody
    public Object listIndustryPoolByCustomerId(HttpServletRequest param) throws Exception {
        return JSONObject.toJSON(userInfoService.listIndustryPoolByCustomerId(param));
    }

    @RequestMapping(value = "/industrypool/status", method = RequestMethod.GET)
    @ResponseBody
    public Object showCustIndustryPoolStatusPage(HttpServletRequest request) throws Exception {
        return JSONObject.toJSON(userInfoService.showCustIndustryPoolStatus(request));
    }


    @RequestMapping(value = "/generateUserId", method = RequestMethod.GET)
    @ResponseBody
    public Object generateUserId() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf(IDHelper.getUserID()));
        return JSONObject.toJSON(map);
    }

    @RequestMapping(value = "/getUserToken", method = RequestMethod.GET)
    @ResponseBody
    public Object getUserToken(String userName) throws Exception {
        CustomerUser u = customerService.getUserByName(userName);
        CustomerProperty customerProperty = customerDao.getProperty(u.getCust_id(), "token");
        String token = customerProperty.getPropertyValue();


        Map<String, String> map = new HashMap<>();
        map.put("token", token);
        return JSONObject.toJSON(map);
    }

    /**
     * 编辑用户信息(新增+编辑+修改状态)
     *
     * @return
     */
    @RequestMapping(value = "/saveUser", method = RequestMethod.POST)
    @ResponseBody
    public String saveUserMessage(@RequestBody UserDTO userDTO) {
        LoginUser loginUser = opUser();
        JSONObject result = new JSONObject();
        boolean flag = false;
        Integer status = userDTO.getStatus();
        Long userId = userDTO.getId();
        String userName = userDTO.getUserName();
        logger.info("保存用户信息传递参数是 ： " + userDTO.toString());
        try {
            if (status != null) {
                if (loginUser.getId().longValue() == userId) return "{\"code\":1}";
                //如果有status则是修改用户状态
                flag = userService.updateUserStatus(userId, status);
            } else {
                //校验用户名是否唯一
                boolean checkUsernameUnique = userService.checkUsernameUnique(userName, userId);
                if (!checkUsernameUnique) {
                    result.put("message", userName + "该用户名已经存在");
                    result.put("code", 0);
                    return returnJsonData(result);
                }
                flag = userService.saveUserMessage(loginUser.getName(), loginUser.getId(), loginUser.isAdmin(), userDTO);
            }

        } catch (Exception e) {
            logger.error("编辑用户信息失败,", e);
            flag = false;
        }
        if (flag) {
            result.put("code", 1);
            result.put("message", "用户信息编辑成功");
        } else {
            result.put("message", "用户信息编辑失败");
            result.put("code", 0);
        }
        return returnJsonData(result);
    }


    /**
     * 删除用户（逻辑删除）
     *
     * @param
     * @return
     */
    @RequestMapping(value = "/delUser", method = RequestMethod.GET)
    @ResponseBody
    public String delUserMessage(Long userId) {
        LoginUser loginUser = opUser();
        if (loginUser.getId().longValue() == userId) return "{\"result\":1}";
        boolean flag = userService.deleteUser(userId, 2);
        JSONObject result = new JSONObject();
        if (flag) {
            result.put("code", 1);
            result.put("message", "删除成功");
        } else {
            result.put("code", 0);
            result.put("message", "删除失败");
        }
        return result.toString();
    }


    /**
     * 获取用户列表
     *
     * @return
     */
    @RequestMapping(value = "/getUserList", method = RequestMethod.GET)
    @ResponseBody
    public Object getUserList(@Valid PageParam page, BindingResult error, UserDTO userDTO) {
        if (error.hasErrors()) {
            return getErrors(error);
        }
        logger.info("查询员工列表页面传递参数是：" + userDTO.toString());
        JSONObject result = new JSONObject();
        LoginUser loginUser = opUser();
        Page userListPage = userService.queryUserList(page, userDTO, loginUser);
        result.put("total", userListPage.getTotal());
        result.put("list", userListPage.getData());
        return JSONObject.toJSON(result);
    }


    @ResponseBody
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    @CacheAnnotation
    public String token(String username, String password, String code, String authorize) throws Exception {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setStateCode("401");  //login fail
        if (username == null || password == null || "".equals(username) || "".equals(password)) {
            responseResult.setMsg("username or password is null");
            return com.alibaba.fastjson.JSONObject.toJSONString(responseResult);
        }
        List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
        LoginUser userdetail = null;
        UserDO u = null;
        if (StringUtil.isNotEmpty(authorize) && !"admin".equals(username)) {
            u = userInfoService.getUserByNameAuthorize(username, NumberConvertUtil.parseInt(authorize));
        } else {
            u = userInfoService.getUserByName(username);
        }
        if (u != null && CipherUtil.generatePassword(password).equals(u.getPassword())) {
            String role = "ROLE_USER";
            String defaultUrl = "";
            if ("admin".equals(u.getName())) {
                auths.add(new SimpleGrantedAuthority("admin"));
                role = "admin";
                defaultUrl = "/backend/customerGroupManagement/customerGroup.html";
            } else {
                auths.add(new SimpleGrantedAuthority("ROLE_USER"));
                // 查询用户关联的所有资源
                List<ResourceDTO> list = resourceService.queryResource(u.getId(), 0L, 1, false);
                for (int i = 0; i < list.size(); i++) {
                    // 处理登录成功后的默认页
                    if (StringUtil.isNotEmpty(list.get(i).getUri())) {
                        defaultUrl = list.get(i).getUri();
                        break;
                    }
                }
            }

            userdetail = new LoginUser(u.getId(), u.getName(), CipherUtil.encodeByMD5(u.getId() + "" + System.currentTimeMillis()));
            userdetail.setCustId("0");
            userdetail.setId(u.getId());
            userdetail.setUserType(String.valueOf(1));
            userdetail.setRole(role);
            userdetail.setName(u.getName());
            // 授权平台 1-精准营销 2-金融超市
            userdetail.setAuthorize(authorize);

            responseResult.setStateCode("200");
            responseResult.setMsg("SUCCESS");
            responseResult.setAuth(userdetail.getAuths().size() > 0 ? userdetail.getAuths().get(0) : "");
            responseResult.setUserName(userdetail.getUsername());
            responseResult.setCustId(userdetail.getCustId());
            responseResult.setUserType(userdetail.getUserType());
            responseResult.setUser_id(userdetail.getId().toString());
            responseResult.setTokenid(userdetail.getTokenid());
            responseResult.setDefaultUrl(defaultUrl);
            if (userdetail != null) {
                this.tokenCacheService.saveToken(userdetail);
            }

        } else {
            responseResult.setMsg("用户名或密码错误，请检查后重新输入");
        }

        return JSONObject.toJSONString(responseResult);
    }

    /**
     * 根据前台界面传递的部门，岗位，条件分页查询用户，如果没有查询限制，则查询所有用户
     *
     * @param condition
     * @param deptId
     * @param roleId
     * @param pageIndex
     * @param countPerPage
     * @return
     */
    @RequestMapping(value = "/query.do")
    @ResponseBody
    public String query(HttpServletRequest request, @RequestParam(required = false) String condition, @RequestParam(required = false) String deptId,
                        @RequestParam(required = false) String roleId, @RequestParam(required = false) Integer pageIndex, @RequestParam int countPerPage) {
        LoginUser user = opUser();
        Page page = new Page();
        if (pageIndex == null) {
            page.setPageIndex(0);
        } else {
            page.setPageIndex(pageIndex);
        }
        page.setCountPerPage(countPerPage);

        QueryDataParam param = new QueryDataParam();
        //param.setCondition(decodeName);
        if (StringUtils.isNotBlank(deptId) && !"undefined".equalsIgnoreCase(deptId) && !"0".equals(deptId)) {
            param.setDeptId(Long.parseLong(deptId));
        }
        if (StringUtils.isNotBlank(roleId) && !"undefined".equalsIgnoreCase(roleId) && !"0".equals(roleId)) {
            param.setRoleId(Long.parseLong(roleId));
        }
        if (StringUtils.isNotBlank(condition) && !"undefined".equalsIgnoreCase(condition)) {
            param.setCondition(condition);
        }

        param.setPage(page);
        List<UserInfo> list = userService.queryUserV1(param);
        JSONArray array = JSONArray.fromObject(list == null ? new ArrayList<RoleInfo>() : list);

        //operation logs
//        UserDTO user = UserHelper.getUser(request);
//        OperlogAppender.operlog(request, user, this.pageName, -1);

        //最后将所有数据封装为一个JSON对象
        net.sf.json.JSONObject o = new net.sf.json.JSONObject();
        o.put("users", array);
        o.put("count", page.getCount());
        o.put("currentUserId", user.getId());
        return o.toString();
    }

    /**
     * 第一次查詢分頁查詢用戶，需要返回用戶信息，部門信息，角色信息
     *
     * @param condition
     * @param deptId
     * @param roleId
     * @param pageIndex
     * @param countPerPage
     * @return
     */
    @RequestMapping(value = "/queryFirst.do")
    @ResponseBody
    public String queryFirst(HttpServletRequest request, @RequestParam(required = false) String condition, @RequestParam(required = false) String deptId,
                             @RequestParam(required = false) String roleId, @RequestParam(required = false) Integer pageIndex, @RequestParam int countPerPage) {
        Page page = new Page();
        if (pageIndex == null) page.setPageIndex(0);
        else page.setPageIndex(pageIndex);
        page.setCountPerPage(countPerPage);


        QueryDataParam param = new QueryDataParam();
        //param.setCondition(decodeName);
        if (StringUtils.isNotBlank(deptId) && !"undefined".equalsIgnoreCase(deptId) && !"0".equals(deptId)) {
            param.setDeptId(Long.parseLong(deptId));
        }
        if (StringUtils.isNotBlank(roleId) && !"undefined".equalsIgnoreCase(roleId) && !"0".equals(roleId)) {
            param.setRoleId(Long.parseLong(roleId));
        }
        if (StringUtils.isNotBlank(condition) && !"undefined".equalsIgnoreCase(condition)) {
            param.setCondition(condition);
        }
        //param.setDeptId(deptId);
        //param.setRoleId(roleId);
        param.setPage(page);
        List<UserInfo> list = userService.queryUserV1(param);
        JSONArray array = JSONArray.fromObject(list == null ? new ArrayList<RoleInfo>() : list);

        //获取所有的部门
        List<DeptDTO> depts = deptService.queryDept();
        JSONArray d = JSONArray.fromObject(depts == null ? new ArrayList<DeptDTO>() : depts);
        //获取所有角色
        List<RoleDTO> roles = roleService.queryAll();
        JSONArray r = JSONArray.fromObject(roles == null ? new ArrayList<RoleDTO>() : roles);

        LoginUser user = opUser();
        //operation logs
//        OperlogAppender.operlog(request, user, this.pageName, -1);

        //最后将所有数据封装为一个JSON对象
        net.sf.json.JSONObject o = new net.sf.json.JSONObject();
        o.put("depts", d);
        o.put("roles", r);
        o.put("users", array);
        o.put("count", page.getCount());
        o.put("currentUserId", user.getId());
        return o.toString();
    }

    /**
     * 查询操作用户拥有的部门角色树
     *
     * @param request
     * @return
     */
    @RequestMapping(value = "/queryRoleTree.do")
    @ResponseBody
    public String queryRoleTree(HttpServletRequest request, @RequestParam Long deptId, @RequestParam String userId) {
        LoginUser user = opUser();
        Long operateUserId = user.getId();
        JSONArray array = new JSONArray();
        Long uId = null;
        if (StringUtil.isNotEmpty(userId)) {
            uId = NumberConvertUtil.parseLong(userId);
        }
        if (uId == null) {
            List<RoleDTO> roles = roleService.queryRoleByDept(deptId, uId);
            if (roles == null || roles.size() == 0) {
                return "[]";
            } else {
                net.sf.json.JSONObject item;
                for (RoleDTO info : roles) {
                    item = new net.sf.json.JSONObject();
                    item.put("id", info.getId());
                    item.put("name", info.getName());
                    array.add(item);
                }
                return array.toString();
            }
        } else {
            List<Map<String, Object>> list = roleService.queryRoleSelectStatus(operateUserId, uId, user.isAdmin());
            if (list != null && !list.isEmpty()) {
                net.sf.json.JSONObject item;
                int checked;
                Long tmpDeptId, id;
                String name;
                for (Map<String, Object> map : list) {
                    tmpDeptId = ((BigInteger) map.get("deptid")).longValue();
                    if (deptId != null && tmpDeptId != null && deptId.equals(tmpDeptId)) {
                        checked = (Integer) map.get("checked");
                        id = ((BigInteger) map.get("id")).longValue();
                        name = (String) map.get("name");
                        item = new net.sf.json.JSONObject();
                        item.put("id", id);
                        item.put("name", name);
                        item.put("checked", checked == 1 ? false : true);
                        array.add(item);
                    }
                }
                return array.toString();
            }
        }
        return array.toString();
    }

    /**
     * 编辑用户信息，需要查询出岗位的分配情况
     *
     * @param request
     * @param userId
     * @return
     */
    @RequestMapping(value = "/edit.do")
    @ResponseBody
    public String edit(HttpServletRequest request, @RequestParam Long userId) {
        //获取操作用户
        LoginUser user = opUser();
        Long operateUserId = user.getId();
        List<Map<String, Object>> list = roleService.queryRoleSelectStatus(operateUserId, userId, user.isAdmin());
        //组装部门，角色树结构
        Map<Long, net.sf.json.JSONObject> deptMap = new HashMap<Long, net.sf.json.JSONObject>();
        if (list != null && !list.isEmpty()) {
            for (Map<String, Object> map : list) {
                Long deptId = ((BigInteger) map.get("deptid")).longValue();
                String deptname = (String) map.get("deptname");
                net.sf.json.JSONObject deptObject = deptMap.get(deptId);
                if (deptObject == null) {
                    deptObject = new net.sf.json.JSONObject();
                    deptObject.put("id", deptId);
                    deptObject.put("name", deptname);
                    JSONArray children = new JSONArray();
                    deptObject.put("children", children);
                    deptMap.put(deptId, deptObject);
                }
                Long id = ((BigInteger) map.get("id")).longValue();
                String name = (String) map.get("name");
                int checked = (Integer) map.get("checked");
                net.sf.json.JSONObject object = new net.sf.json.JSONObject();
                object.put("id", id);
                object.put("name", name);
                object.put("checked", checked == 1 ? false : true);
                deptObject.getJSONArray("children").add(object);
            }
        }
        JSONArray array = new JSONArray();
        if (!deptMap.isEmpty()) {
            Set<Long> keySet = deptMap.keySet();
            for (Long key : keySet) {
                net.sf.json.JSONObject object = deptMap.get(key);
                array.add(object);
            }
        }
        return array.toString();
    }


    /**
     * 保存新添加的用户
     *
     * @param username
     * @param realname
     * @param deptId
     * @param roleIds
     * @param password
     * @return
     */
    @RequestMapping(value = "/saveNew0.do")
    @ResponseBody
    public String saveNew(@RequestParam String username, @RequestParam String realname,
                          @RequestParam Long deptId, @RequestParam String roleIds,
                          @RequestParam(required = false) String password, @RequestParam(required = false) String customerIds,
                          @RequestParam(required = false) String labelIds, @RequestParam(required = false) String categoryIds,
                          @RequestParam String channelIds,
                          HttpServletRequest request) {
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        //校验用户名是否唯一
        boolean checkUsernameUnique = checkUsernameUnique(username, null);
        if (!checkUsernameUnique) {
            result.put("result", false);
            result.put("code", 1);
            return result.toString();
        }
        String[] ids = roleIds.split(",");
        List<RoleDTO> roles = null;
        if (ids != null && ids.length > 0) {
            roles = new ArrayList<RoleDTO>();
            for (int i = 0; i < ids.length; i++) {
                RoleDTO role = new RoleDTO();
                role.setKey(Long.parseLong(ids[i]));
                roles.add(role);
            }
        }
        LoginUser loginUser = opUser();
        UserDTO user = new UserDTO();
        user.setName(username);
        user.setRealName(realname);
        user.setDeptId(deptId);
        user.setRoleList(roles);
        user.setStatus(0);
        user.setSource(DataFromEnum.SYSTEM.getValue());
        if (StringUtils.isEmpty(password)) {
            password = defaultPassword;
        }
        //加密密码
        password = CipherUtil.generatePassword(password);
        user.setPassword(password);
        user.setOptuser(loginUser.getName());
        user.setCreateTime(new Date());
        //封装用户与角色的关系
        UserRoles userRoles = new UserRoles();
        userRoles.setCreateDate(new Date());
        userRoles.setOptUser(loginUser.getName());
        userRoles.setRoles(roles);
        userRoles.setUser(user);
        // boolean flag = userService.saveUser(userRoles, loginUser.getId(), UserHelper.isAdmin(request));
        //boolean flag = userService.saveUser(userRoles, loginUser.getId(), UserHelper.isAdmin(request),customerIds, labelIds, categoryIds);
        boolean flag = userService.saveUser(userRoles, loginUser.getId(), loginUser.isAdmin(), customerIds, labelIds, categoryIds, channelIds);
        Long id = user.getId();

        if (flag) {
            UserInfo info = userService.queryUserInfo(id);
            result.put("result", true);
            result.put("data", net.sf.json.JSONObject.fromObject(info));
            //operation logs
//            OperlogAppender.operlog(request, user, this.pageName, info.getId());
        } else {
            result.put("result", false);
            result.put("code", 2);
        }
        return result.toString();
        //return view;
    }


    /**
     * 保存新添加的用户
     *
     * @param username
     * @param realname
     * @param deptId
     * @param roleIds
     * @param password
     * @return
     */
    @RequestMapping(value = "/saveNew.do")
    @ResponseBody
    public String saveNewV1(@RequestParam String username, @RequestParam String realname,
                            @RequestParam Long deptId, @RequestParam String roleIds,
                            @RequestParam(required = false) String password, @RequestParam(required = false) String customerIds,
                            @RequestParam(required = false) String labelIds, @RequestParam(required = false) String categoryIds,
                            @RequestParam String channelIds,
                            HttpServletRequest request) {
//        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        JSONObject result = new JSONObject();
        //校验用户名是否唯一
        boolean checkUsernameUnique = checkUsernameUnique(username, null);
        if (!checkUsernameUnique) {
            result.put("result", false);
            result.put("code", 1);
            return returnJsonData(result);
        }
        String[] ids = roleIds.split(",");
        List<RoleDTO> roles = null;
        if (ids != null && ids.length > 0) {
            roles = new ArrayList<RoleDTO>();
            for (int i = 0; i < ids.length; i++) {
                RoleDTO role = new RoleDTO();
                role.setKey(Long.parseLong(ids[i]));
                roles.add(role);
            }
        }
        LoginUser loginUser = opUser();
        UserDTO user = new UserDTO();
        user.setName(username);
        user.setUserName(username);
        user.setRealName(realname);
        user.setDeptId(deptId);
        user.setRoleList(roles);
        user.setStatus(0);
        user.setSource(DataFromEnum.SYSTEM.getValue());
        if (StringUtils.isEmpty(password)) {
            password = defaultPassword;
        }
        //加密密码
        password = CipherUtil.generatePassword(password);
        user.setPassword(password);
        user.setOptuser(loginUser.getName());
        user.setCreateTime(new Date());
        user.setAuthorize(opUser().getAuthorize());
        //user.setAuthorize("1");
        //封装用户与角色的关系
        UserRoles userRoles = new UserRoles();
        userRoles.setCreateDate(new Date());
        userRoles.setOptUser(loginUser.getName());
        userRoles.setRoles(roles);
        userRoles.setUser(user);
        boolean flag;
        try {
            flag = userService.saveUserV1(userRoles, loginUser.getId(), loginUser.isAdmin(), customerIds, labelIds, categoryIds, channelIds);
        } catch (Exception e) {
            logger.error("添加用户失败,", e);
            flag = false;
        }
        Long id = user.getId();
        if (flag) {
            UserInfo info = userService.queryUserInfo(id);
            result.put("result", true);
            result.put("data", net.sf.json.JSONObject.fromObject(info));
        } else {
            result.put("result", false);
            result.put("code", 2);
        }
        return returnJsonData(result);
    }


    /**
     * 更新用户信息
     *
     * @param id
     * @param roleIds
     * @param password
     * @return
     */
    @RequestMapping(value = "/saveUpdate.do")
    @ResponseBody
    public String saveUpdate(@RequestParam Long id,
                             @RequestParam(required = false) String roleIds, @RequestParam(required = false) String password,
                             @RequestParam(required = false) String customerIds, @RequestParam(required = false) String labelIds, @RequestParam(required = false) String categoryIds, @RequestParam String channelIds, HttpServletRequest request) {

        List<RoleDTO> roles = null;
        if (StringUtils.isNotBlank(roleIds)) {
            String[] ids = roleIds.split(",");
            if (ids != null && ids.length > 0) {
                roles = new ArrayList<RoleDTO>();
                for (int i = 0; i < ids.length; i++) {
                    RoleDTO role = new RoleDTO();
                    role.setKey(Long.parseLong(ids[i]));
                    roles.add(role);
                }
            }
        }
        LoginUser loginUser = opUser();
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setRoleList(roles);
        if (StringUtils.isNotBlank(password)) {
            password = defaultPassword;
            //加密密码
            password = CipherUtil.generatePassword(password);
            user.setPassword(password);
        }

        user.setOptuser(loginUser.getName());
        user.setModifyTime(new Date());

        //封装用户与角色的关系
        UserRoles userRoles = new UserRoles();
        userRoles.setCreateDate(new Date());
        userRoles.setOptUser(loginUser.getName());
        userRoles.setRoles(roles);
        userRoles.setUser(user);
        //boolean flag = userService.saveUser(userRoles, loginUser.getId(), UserHelper.isAdmin(request),customerIds, labelIds, categoryIds);
        boolean flag = false;
        try {
            flag = userService.saveUserV1(userRoles, loginUser.getId(), loginUser.isAdmin(), customerIds, labelIds, categoryIds, channelIds);
        } catch (Exception e) {
            flag = false;
            logger.error("更新用户信息异常,", e);
        }
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        if (flag) {
            UserInfo info = userService.queryUserInfo(id);
            result.put("result", true);
            result.put("data", net.sf.json.JSONObject.fromObject(info));
//            OperlogAppender.operlog(request, loginUser, this.pageName, info.getId());
        } else {
            result.put("result", false);
        }
        return returnJsonData(result);
    }

    /**
     * 用户自己修改密码
     *
     * @param oldPassword
     * @param newPassword
     * @return
     */
    @RequestMapping(value = "/changePassword.do")
    @ResponseBody
    public String changePassWord(HttpServletRequest request, @RequestParam Long id, @RequestParam String oldPassword, @RequestParam String newPassword) {
        //先加密密码
        oldPassword = CipherUtil.generatePassword(oldPassword);
        boolean flag = userService.checkPassword(id, oldPassword);
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        if (flag) {
            UserDTO user = userService.queryUserById(id);
            //加密新密码
            newPassword = CipherUtil.generatePassword(newPassword);
            user.setPassword(newPassword);
            boolean boo = userService.updateUser(user);
//            OperlogAppender.operlog(request, user, this.pageName, user.getId());
            if (boo) {
                result.put("result", true);
            } else {
                result.put("result", false);
                result.put("msg", "更新失败！");
            }
        } else {
            result.put("result", false);
            result.put("code", 1);
            result.put("msg", "原密码输入有误！");
        }
        return result.toString();
    }

    /**
     * 检查用户名是否唯一
     *
     * @param username
     * @param id
     * @return
     */
    public boolean checkUsernameUnique(String username, Long id) {
        UserDTO user = new UserDTO();
        user.setId(id);
        user.setName(username);
        boolean flag = userService.checkUsernameUnique(user);
        return flag;
    }

    /**
     * 用于重置用户密码
     *
     * @param userId
     * @return
     */
    @RequestMapping(value = "/resetPass.do")
    @ResponseBody
    public String resetPass(HttpServletRequest request, @RequestParam Long userId) {
        UserDTO user = new UserDTO();
        user.setId(userId);
        user.setPassword(CipherUtil.generatePassword(defaultPassword));
        boolean flag = userService.updateUser(user);
//        if (null != userId)
//            OperlogAppender.operlog(request, user, this.pageName, userId);
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        if (flag) {
            result.put("result", true);
        } else {
            result.put("result", false);
        }
        return result.toString();
    }


    @RequestMapping(value = "/queryDept.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryDept() {
        List<DeptDTO> depts = deptService.queryDept();
        JSONArray d = new JSONArray();
        if (depts != null && !depts.isEmpty()) {
            d = JSONArray.fromObject(depts);
        }

        return d.toString();
    }


    @RequestMapping(value = "/del.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String del(@RequestParam Long userId, HttpServletRequest request) {
        LoginUser loginUser = opUser();
        if (loginUser.getId().longValue() == userId) return "{\"result\":false}";
        UserDTO user = new UserDTO();
        user.setId(userId);
        user.setStatus(1);
        boolean flag = userService.deleteUser(user);
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
//        if (null != userId)
//            OperlogAppender.operlog(request, user, this.pageName, userId);
        if (flag) {
            result.put("result", true);
        } else {
            result.put("result", false);
        }
        return result.toString();
    }

    @RequestMapping(value = "/setup.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String setup(@RequestParam Long userId) {
        UserDTO user = userService.queryUserById(userId);
        if (user.getStatus() == 1) {
            user.setStatus(0);
        } else {
            user.setStatus(1);
        }
        boolean flag = userService.updateUser(user);
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        if (flag) {
            result.put("result", true);
        } else {
            result.put("result", false);
        }
        return result.toString();
    }

    /*---------为现在程序赶回一个查询接口------------*/

    /**
     * 查询用户的ID查询用户的信息
     *
     * @return
     */
    @RequestMapping(value = "/queryUInfo.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryUserInfo(HttpServletRequest request) {
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        LoginUser loginUser = opUser();
        UserInfo info = userService.queryUserInfo(loginUser.getId());
        result.put("result", true);
        result.put("data", net.sf.json.JSONObject.fromObject(info));
//        if (null != info)
//            OperlogAppender.operlog(request, user, "我的账户", info.getId());
        return result.toString();
    }

    /**
     * 这个方法是更新用户的属性
     *
     * @return
     */
    @RequestMapping(value = "/updateInfo.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String update(@RequestParam(required = false) String realName, @RequestParam(required = false) String email, @RequestParam(required = false) String emailGroup, @RequestParam(required = false) String connectionInfo, @RequestParam(required = false) String oldPassword, @RequestParam(required = false) String newPassword, HttpServletRequest request) {
        UserInfo userInfo = new UserInfo();
        LoginUser loginUser = opUser();
        userInfo.setId(String.valueOf(loginUser.getId()));
        if (realName != null) userInfo.setRealName(realName);
        if (email != null) userInfo.setEmail(email);
        if (emailGroup != null) userInfo.setEmainGroup(emailGroup);
        if (connectionInfo != null) userInfo.setConnectionInfo(connectionInfo);
        if (newPassword != null) userInfo.setPassword(newPassword);
        if (oldPassword != null) userInfo.setOldPassword(oldPassword);
        boolean result = userService.updateUser(userInfo);
//        OperlogAppender.operlog(request, user, this.pageName, user.getId());
        if (result) {
            return "{\"result\":true}";
        } else {
            return "{\"result\":false}";
        }
    }

    @RequestMapping(value = "/updateDataPermission.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String updateDatePermission(HttpServletRequest request, @RequestParam Long userId, @RequestParam String customerIds, @RequestParam String labelIds, @RequestParam String categoryIds) {
        LoginUser loginUser = opUser();
        boolean flag = userService.updateDataPermission(userId, loginUser.getId(), loginUser.isAdmin(), customerIds, labelIds, categoryIds);
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        if (flag) {
            result.put("result", true);
        } else {
            result.put("result", false);
        }
        return result.toString();
    }

    //长虹渠道
    @RequestMapping(value = "queryLabelChannel", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String queryLabelChannel(Long userId) {
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        List<Map<String, Object>> ret = userService.queryLabelChannel(userId);
        result.put("data", ret);
        return result.toString();
    }

    @RequestMapping(value = "/unlockUser.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String unlockUser(HttpServletRequest request, @RequestParam Long userId) {
        boolean flag = userService.deleteByAudName(userId);
//        if (null != userId)
//            OperlogAppender.operlog(request, this.pageName, userId);
        net.sf.json.JSONObject result = new net.sf.json.JSONObject();
        if (flag) {
            result.put("result", true);
        } else {
            result.put("result", false);
        }
        return result.toString();
    }


   /* @RequestMapping(value = "/industrypool/list", method = RequestMethod.GET)
    @ResponseBody
    public Object queryIndustryPoolByCondition(HttpServletRequest param) throws Exception {
        return com.alibaba.fastjson.JSONObject.toJSON(userInfoService.queryIndustryPoolByCondition(param));
    }

    @RequestMapping(value = "/industrypool/detail", method = RequestMethod.GET)
    @ResponseBody
    public Object listIndustryPoolByCustomerId(HttpServletRequest param) throws Exception {
        return com.alibaba.fastjson.JSONObject.toJSON(userInfoService.listIndustryPoolByCustomerId(param));
    }

    @RequestMapping(value = "/industrypool/status", method = RequestMethod.GET)
    @ResponseBody
    public Object showCustIndustryPoolStatusPage(HttpServletRequest request) throws Exception {
        return com.alibaba.fastjson.JSONObject.toJSON(userInfoService.showCustIndustryPoolStatus(request));
    }*/

    @RequestMapping(value = "/industrypool/open", method = {RequestMethod.PUT, RequestMethod.POST})
    @ResponseBody
    public Object openIndustryPool(@RequestBody com.alibaba.fastjson.JSONObject param) throws Exception {
        String customerId = param.getString("customerId");
        String industryPoolId = param.getString("industryPoolId");
        int action = param.getInteger("action");
        //String operator = user.get().getName();
        String operator = "admin";

        if (StringUtil.isEmpty(customerId)) {
            throw new TouchException("300", "CustomerId can't be empty");
        }
        if (StringUtil.isEmpty(industryPoolId)) {
            throw new TouchException("300", "IndustryPoolId can't be empty");
        }
        //开通
        if (1 == action) {
            customerIndustryPoolService.addCustomerIndustryPool(customerId, industryPoolId, operator);
        }
        //禁用
        if (2 == action) {
            customerIndustryPoolService.deleteCustomerIndustryPool(customerId, industryPoolId, operator);
        }
        return com.alibaba.fastjson.JSONObject.toJSON(new HashMap<>());
    }


  /*  @RequestMapping(value = "/generateUserId", method = RequestMethod.GET)
    @ResponseBody
    public Object generateUserId() throws Exception {
        Map<String, String> map = new HashMap<>();
        map.put("userId", String.valueOf(IDHelper.getUserID()));
        return com.alibaba.fastjson.JSONObject.toJSON(map);
    }*/

    @RequestMapping(value = "/otp/verify/{auType}/{type}/{condition}/{otp}")
    @ResponseBody
    public Object otpVerify(@PathVariable int auType, @PathVariable int type, @PathVariable String condition, @PathVariable String otp) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //验证手机是否一致
        boolean success = sendSmsService.verificationCode(condition, type, otp) == 1 ? true : false;
        if (!success) {
            if (1 == auType) {
                throw new TouchException("20006", "手机验证码错误");
            }
            if (2 == auType) {
                throw new TouchException("20007", "邮箱验证码错误");
            }
        }
        return net.sf.json.JSONObject.fromObject(resultMap);

    }

    @RequestMapping(value = "/status/change", method = {RequestMethod.PUT, RequestMethod.POST})
    @ResponseBody
    public Object changeUserStatus(@RequestBody net.sf.json.JSONObject param) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        String userId = param.getString("userId");
        String action = param.getString("action");
        if (StringUtil.isEmpty(userId)) {
            throw new TouchException("300", "用户id不能为空");
        }
        try {
            userInfoService.changeUserStatus(userId, action);
        } catch (TouchException e) {
            throw new TouchException(e.getCode(), e.getMessage());
        }
        return com.alibaba.fastjson.JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/update/password0", method = RequestMethod.POST)
    @ResponseBody
    public Object updatePassword0(@RequestBody com.alibaba.fastjson.JSONObject param) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        String oldPwd = param.getString("oldPassword");
        String newPwd = param.getString("newPassword");
        int pwdLevel = param.getIntValue("pwdLevel");
        logger.info("用户:{}修改密码oldPassword:{},newPassword:{}", opUser().getId(), oldPwd, newPwd);
        // 记录操作日志
        super.operlog(opUser().getId(), "用户修改密码oldPassword:" + oldPwd + ",newPassword:" + newPwd);
        userInfoService.updateCustomerUserPwd(opUser().getId(), oldPwd, newPwd, pwdLevel);
        return com.alibaba.fastjson.JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/registinfo/update/", method = RequestMethod.POST)
    @ResponseBody
    public Object updateRegistInfo(@RequestBody net.sf.json.JSONObject request) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        int identifyType = request.getInt("identifyType");
        String identifyValue = request.getString("identifyValue");
        String code = request.getString("otp");
        String newValue = request.getString("newValue");
        //用于定义验证码类型（修改手机时等状态的验证码）
        int type = Integer.parseInt(request.getString("type"));
        boolean success = sendSmsService.verificationCode(newValue, type, code) == 1 ? true : false;
        if (!success) {
            if (1 == identifyType) {
                throw new TouchException("20006", "手机验证码错误");
            }
            if (2 == identifyType) {
                throw new TouchException("20007", "邮箱验证码错误");
            }
        }
        userInfoService.updateRegistInfo(identifyType, identifyValue, newValue, String.valueOf(opUser().getId()));
        return JSON.toJSON(resultMap);
    }
}
