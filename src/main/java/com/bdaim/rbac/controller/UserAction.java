package com.bdaim.rbac.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.auth.entity.UserVerificationCode;
import com.bdaim.auth.service.UserVerificationCodeService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.rbac.dto.Page;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.entity.User;
import com.bdaim.rbac.service.UserInfoService;
import com.bdaim.rbac.service.UserService;
//import com.bdaim.slxf.common.security.ResponseResult;
//import com.bdaim.slxf.common.security.service.TokenManager;
import com.bdaim.slxf.exception.TouchException;
//import com.bdaim.slxf.filter.FiledFilter;
//import com.bdaim.smscenter.service.SendSmsService;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
public class UserAction extends BasicAction {
    private static Log logger = LogFactory.getLog(UserAction.class);


    @Resource
    private UserService userService;
    @Resource
    UserInfoService userInfoService;
//    @Resource
//    SendSmsService sendSmsService;
    @Resource
    UserVerificationCodeService userVerificationCodeService;
    UserVerificationCode userVerificationCode = null;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private CustomerService customerService;


//    @ResponseBody
//    @RequestMapping(value = "/token", method = RequestMethod.POST)
//    @CacheAnnotation
//    public String token(String username, String password, String code) {
//        ResponseResult responseResult = new ResponseResult();
//        responseResult.setStateCode("401");  //login fail
//
//        if (username == null || password == null || "".equals(username) || "".equals(password)) {
//            responseResult.setMsg("username or password is null");
//            return JSONObject.toJSONString(responseResult);
//        }
//
//        List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
//        LoginUser userdetail = null;
//
//
//        UserDO u = userInfoService.getUserByName(username);
//        if (u != null && CipherUtil.generatePassword(password).equals(u.getPassword())) {
//            auths.add(new SimpleGrantedAuthority("ROLE_USER"));
//            String role = "ROLE_USER";
//
//            if ("admin".equals(u.getName())) {
//                auths.add(new SimpleGrantedAuthority("admin"));
//                role = "admin";
//            }
//
//            userdetail = new LoginUser(u.getName(), u.getPassword(), auths);
//            userdetail.setCustId("0");
//            userdetail.setId(u.getId());
//            userdetail.setUserType(String.valueOf(u.getUserType()));
//            userdetail.setRole(role);
//            userdetail.setName(u.getName());
//
//            TokenManager.createNewToken(userdetail);
//
//            responseResult.setStateCode("200");
//            responseResult.setMsg("SUCCESS");
//            responseResult.setAuth(userdetail.getAuthorities().toArray()[0].toString());
//            responseResult.setUserName(userdetail.getUsername());
//            responseResult.setCustId(userdetail.getCustId());
//            responseResult.setUserType(Integer.valueOf(userdetail.getUserType()));
//            responseResult.setUser_id(userdetail.getId().toString());
//            responseResult.setTokenid(userdetail.getTokenid());
//
//        } else {
//            responseResult.setMsg("username or password is error");
//        }
//
//        return JSONObject.toJSONString(responseResult);
//    }


//    /**
//     * 根据id获取用户信息
//     *
//     * @return
//     */
//    @ResponseBody
//    @CacheAnnotation
//    @RequestMapping("/getUserById")
//    public String getUserById(Integer id) {
//        User user = userService.getUserById((long) id);
//        return JSON.toJSONString(user, new FiledFilter());
//    }

//    /**
//     * 根据id获取用户信息
//     *
//     * @return
//     */
//    @ResponseBody
//    @CacheAnnotation
//    @RequestMapping("/getAllUsers")
//    public String getAllUsers() {
//        List<User> users = userService.getAllUsers();
//        Map<String, Object> map = new HashMap<String, Object>();
//        map.put("stores", users);
//        return JSON.toJSONString(map, new FiledFilter());
//    }

    @RequestMapping("/login")
    public String login(@ModelAttribute("_user") User user) {
//		User u = userService.getUserById(user.getId());
        return "default";
    }

    @RequestMapping(value = "/identify/check", method = RequestMethod.GET)
    @ResponseBody
    public Object identifyCheck(String type, String condition) throws TouchException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("data", JSONObject.toJSON(userInfoService.getUserByCondition(type, condition)));
        return JSON.toJSONString(resultMap);
    }

//    @RequestMapping(value = "/otp/verify/{auType}/{type}/{condition}/{otp}")
//    @ResponseBody
//    public Object otpVerify(@PathVariable int auType, @PathVariable int type, @PathVariable String condition, @PathVariable String otp) throws Exception {
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        //验证手机是否一致
//        boolean success = sendSmsService.verificationCode(condition, type, otp) == 1 ? true : false;
//        if (!success) {
//            if (1 == auType) {
//                throw new TouchException("20006", "手机验证码错误");
//            }
//            if (2 == auType) {
//                throw new TouchException("20007", "邮箱验证码错误");
//            }
//        }
//        return JSONObject.toJSON(resultMap);
//
//    }

//    /**
//     * @param param
//     * @return
//     * @throws Exception
//     * @Description 未登录重置密码
//     */
//    @RequestMapping(value = "/reset/password", method = RequestMethod.PUT)
//    @ResponseBody
//    public Object resetPassword(@RequestBody JSONObject param) throws Exception {
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        String condition = param.getString("condition");
//        String otp = param.getString("otp");
//        String newPwd = param.getString("newPassword");
//        int pwdlevel = param.getInteger("pwdLevel");
//        int type = param.getInteger("type");
//        int auType = param.getInteger("auType");
//
//        boolean success = sendSmsService.verificationCode(condition, auType, otp) == 1 ? true : false;
//        if (!success) {
//            if (1 == type) {
//                throw new TouchException("20006", "手机验证码错误");
//            }
//            if (2 == type) {
//                throw new TouchException("20007", "邮箱验证码错误");
//            }
//        }
//        userInfoService.resetPwd(type, condition, newPwd, pwdlevel);
//        return JSONObject.toJSON(resultMap);
//    }

    @RequestMapping(value = "/update/password", method = RequestMethod.POST)
    @ResponseBody
    public Object updatePassword(@RequestBody JSONObject param) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        String oldPwd = param.getString("oldPassword");
        String newPwd = param.getString("newPassword");
        int pwdLevel = param.getIntValue("pwdLevel");
        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
            userInfoService.updateFrontPwd(opUser().getId(), oldPwd, newPwd, pwdLevel);
        } else {
            userInfoService.updatePwd(opUser().getId(), oldPwd, newPwd, pwdLevel);
        }
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/securityCenter/", method = RequestMethod.GET)
    @ResponseBody
    public Object securityCenter() throws Exception {
        long userId = opUser().getId();
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("data", JSONObject.toJSON(userInfoService.getSecurityCenterInfo(userId)));
        return JSONObject.toJSON(resultMap);
    }

//    /**
//     * 验证注册的验证码是否有效
//     *
//     * @param identifyType
//     * @param identifyValue
//     * @param code
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping(value = "/verifyUniqueness/{identifyType}/{identifyValue}/{code}")
//    @ResponseBody
//    public Object verifyIdentifyValueUniqueness(@PathVariable int identifyType, @PathVariable String identifyValue, @PathVariable String code) throws Exception {
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        boolean success = sendSmsService.verificationCode(identifyValue, identifyType, code) == 1 ? true : false;
//        if (!success) {
//            if (1 == identifyType) {
//                throw new TouchException("20006", "手机验证码错误");
//            }
//            if (2 == identifyType) {
//                throw new TouchException("20007", "邮箱验证码错误");
//            }
//        }
//        return JSONObject.toJSON(resultMap);
//    }

//    /**
//     * 更改绑定的手机号
//     *
//     * @param request
//     * @return
//     * @throws Exception
//     */
//    @RequestMapping(value = "/registinfo/update", method = RequestMethod.PUT)
//    @ResponseBody
//    public Object updateRegistInfo(@RequestBody JSONObject request) throws Exception {
//
//        Map<String, Object> resultMap = new HashMap<String, Object>();
//        String oldMobile = request.getString("oldMobile");//旧手机号
//        String code = request.getString("code");//获取到的验证码
//        String newMobile = request.getString("newMobile");//新手机号
//        //用于定义验证码类型（修改手机时等状态的验证码）
//        int type = Integer.parseInt(request.getString("type"));
//        boolean success = sendSmsService.verificationCode(newMobile, type, code) == 1 ? true : false;
//        if (!success) {
//            throw new TouchException("20006", "手机验证码错误");
//        }
//        String userId = opUser().getId().toString();
//        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
//            userInfoService.updateRegistInfo(oldMobile, newMobile);
//        } else {
//            userInfoService.updateFrontRegistInfo(userId, oldMobile, newMobile);
//        }
//        return JSONObject.toJSON(resultMap);
//    }

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
        CustomerUserDO u = customerService.getUserByName(userName);
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
}
