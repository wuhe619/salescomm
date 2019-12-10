package com.bdaim.customer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.auth.util.ResponseResult;
import com.bdaim.callcenter.dto.CustomCallConfigDTO;
import com.bdaim.callcenter.dto.SeatCallCenterConfig;
import com.bdaim.callcenter.dto.SeatPropertyDTO;
import com.bdaim.callcenter.dto.XFCallCenterConfig;
import com.bdaim.callcenter.service.impl.SeatsService;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.page.PageList;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.*;
import com.bdaim.customer.entity.ApparentNumber;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.B2BTcbService;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customer.user.dto.UserCallConfigDTO;
import com.bdaim.customer.user.service.CustomerUserService;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.marketproject.dto.MarketProjectDTO;
import com.bdaim.marketproject.service.MarketProjectService;
import com.bdaim.rbac.dto.RoleEnum;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.rbac.service.UserService;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.supplier.service.SupplierService;
import com.bdaim.util.CipherUtil;
import com.bdaim.util.IDHelper;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author yanls@bdaim.com
 * @Description: 团队管理
 * @date 2018/9/710:27
 */


@Controller
@RequestMapping("/customer")
public class CustomerAction extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(CustomerAction.class);
    @Resource
    CustomerService customerService;
    @Resource
    UserService userService;
    @Resource
    SendSmsService sendSmsService;
    @Resource
    SeatsService seatsService;
    @Resource
    CustomerUserDao customerUserDao;
    @Resource
    SupplierService supplierService;
    @Resource
    MarketProjectService marketProjectService;
    @Resource
    private CustomerUserService customerUserService;
    @Resource
    private CustomerSeaService customerSeaService;
    @Resource
    private TokenCacheService<LoginUser> tokenCacheService;
    @Resource
    private B2BTcbService b2BTcbService;

    private static Map name2token = new HashMap();

    /**
     * 团队管理列表以及搜索
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/query", method = RequestMethod.GET)
    @CacheAnnotation
    public String getUserList(@Valid PageParam page, BindingResult error, String name, String realName, String mobileNum) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        String customerId = opUser().getCustId();

        PageList list = customerService.getUser(page, customerId, name, realName, mobileNum);
        //String customerId ="1702210227030000";
        return JSON.toJSONString(list);
    }


    /**
     * @param {userDTO
     * @return String 返回类型
     * @throws
     * @Title: updateUser
     * @Description: 团队管理列表冻结操作员
     */
    @ResponseBody
    @RequestMapping(value = "/user/delete", method = RequestMethod.POST)
    @CacheAnnotation
    public String deleteUser(@RequestBody Map<String, Object> params) {
        String userName = String.valueOf(params.get("userName"));
        Integer status = (Integer) params.get("status");
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (opUser().getRole().equals("ROLE_CUSTOMER") && opUser().getUserType().equals("1")) {
            Integer code = customerService.deleteUser(userName, status);
            if (code >= 0) {
                resultMap.put("code", "0");
                resultMap.put("_message", "成功");

            } else {
                resultMap.put("code", "1");
                resultMap.put("_message", "失败");

            }
        }
        return JSONObject.toJSONString(resultMap);
    }


    /**
     * @Title: updateUser
     * @Description: 团队管理列表编辑操作员
     */
    @ResponseBody
    @RequestMapping(value = "/user/update", method = RequestMethod.PUT)
    @CacheAnnotation
    public String updateUser(@RequestBody UserDTO userDTO) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (opUser().getRole().equals("ROLE_CUSTOMER") && opUser().getUserType().equals("1")) {
            Integer code = customerService.updateuser(userDTO);
            if (code >= 0) {
                resultMap.put("code", "0");
                resultMap.put("_message", "更新操作员成功");

            } else {
                resultMap.put("code", "1");
                resultMap.put("_message", "更新操作员失败");

            }
        }
        return JSONObject.toJSONString(resultMap);
    }


    /**
     * @Title: regist
     * @Description: 企业管理 创建客户
     */
    @RequestMapping(value = "/regist", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public ResponseInfo regist(@RequestBody CustomerRegistDTO customerRegistDTO) {
        try {
            LoginUser lu = opUser();
            if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
                //前台创建的createId是当前登陆企业id
                customerRegistDTO.setCreateId(lu.getCustId());
            }
            String code = customerService.registerOrUpdateCustomer(customerRegistDTO);
            if ("001".equals(code)) {
                return new ResponseInfoAssemble().failure(-1, customerRegistDTO.getName() + "账号已经存在");
            }
            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            logger.error("创建企业信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "创建企业失败");
        }

    }

    /**
     * @Title:
     * @Description: 企业员工信息编辑
     */
    @RequestMapping(value = "/updateUser", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public ResponseInfo customerAddUser(@RequestBody CustomerRegistDTO customerRegistDTO) {
        try {
            LoginUser lu = opUser();
            customerRegistDTO.setCustId(lu.getCustId());
            String code = customerService.customerAddUser(customerRegistDTO);
            if ("001".equals(code)) {
                return new ResponseInfoAssemble().failure(-1, customerRegistDTO.getName() + "账号已经存在");
            }
            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            logger.error("创建企业信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "用户编辑失败");
        }

    }

    /**
     * @Title: queryCustomer
     * @Description: 企业管理 客户详情
     */
    @RequestMapping(value = "/query/list", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo queryCustomer(@Valid PageParam page, BindingResult error, CustomerRegistDTO customerRegistDTO) {
        if (error.hasFieldErrors()) {
            return new ResponseInfoAssemble().failure(-1, "缺少必要参数");
        }
        LoginUser lu = opUser();
        PageList list = null;
        Map<Object, Object> map = new HashMap<Object, Object>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = customerService.getCustomerInfo(page, customerRegistDTO);
        } else {
            customerRegistDTO.setCreateId(lu.getCustId());
            list = customerService.getCustomerInfo(page, customerRegistDTO);
        }
        map.put("list", list);
        //图片根路径
        String preUrl = "/pic";
        map.put("preUrl", preUrl);
        return new ResponseInfoAssemble().success(map);
    }

    /**
     * @Title: queryCustomer
     * @Description: 海关客户列表
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo queryCustomerList(@Valid PageParam page, BindingResult error, CustomerRegistDTO customerRegistDTO) {
        if (error.hasFieldErrors()) {
            return new ResponseInfoAssemble().failure(-1, "缺少必要参数");
        }
        LoginUser lu = opUser();
        PageList list = null;
        Map<Object, Object> map = new HashMap<Object, Object>();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = customerService.getCustomerList(page, customerRegistDTO);
        } else {
            customerRegistDTO.setCreateId(lu.getCustId());
            list = customerService.getCustomerList(page, customerRegistDTO);
        }
        map.put("list", list);
        //图片根路径
        String preUrl = "/pic";
        map.put("preUrl", preUrl);
        return new ResponseInfoAssemble().success(map);
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo updateCustomer(@RequestBody CustomerRegistDTO customerRegistDTO) {
        try {
            customerService.registerOrUpdateCustomer(customerRegistDTO);
            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            logger.error("修改企业信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "修改企业信息异常");
        }
    }


    /**
     * 查询操作员
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/heartbeat", method = RequestMethod.POST)
    @CacheAnnotation
    public String heartbeat(Integer pageNum, Integer pageSize, String name, String realName) {
        LoginUser lu = opUser();
        customerService.heartbeat(lu.getId());
        return "success";
    }

    /**
     * @description 查询企业配置信息
     * @author:duanliying
     * @method
     * @date: 2019/2/28 9:53
     */
    @RequestMapping(value = "/selectCustomerConfig", method = RequestMethod.GET)
    @ResponseBody
    public String selectCustomerConfig(String custId, String callType) {
        Map<String, Object> data = null;
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                data = customerService.selectCustomerConfig(custId, callType);
            }
        } catch (Exception e) {
            logger.error("查询单个客户售价详情失败,", e);
        }
        return returnJsonData(data);
    }

    /**
     * @description 修改企业配置信息（定价+资源）
     * @author:duanliying
     * @method
     * @date: 2019/2/28 9:53
     */
    @RequestMapping(value = "/updateCustConfig", method = RequestMethod.POST)
    @ResponseBody
    public String saveCustSetting(@RequestBody JSONObject json) {
        Map<String, Object> data = new HashMap<>();
        logger.info("修改企业配置信息传递参数是：" + json.toString());
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                json.put("optUserId", lu.getId());
                customerService.saveCustSetting(json);
                data.put("code", 1);
                data.put("message", "修改企业配置信息成功");
            }
        } catch (Exception e) {
            logger.error("保存客户配置信息失败,", e);
            data.put("code", 0);
            data.put("message", "修改企业配置信息失败");
        }
        return returnJsonData(data);
    }


    /**
     * @description 修改企业销售定价记录
     * @author:duanliying
     * @method
     * @date: 2019/2/28 9:53
     */
    @RequestMapping(value = "/salePriceLog", method = RequestMethod.GET)
    @ResponseBody
    public String getSalePriceLog(@Valid PageParam page, String zid, BindingResult error, String custId, String name, String startTime, String endTime) {
        if (error.hasErrors()) {
            return getErrors(error);
        }
        Map<String, Object> data = new HashMap<>();
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                List<Map<String, Object>> salePriceLog = customerService.getSalePriceLog(page, zid, custId, name, startTime, endTime);
                data.put("code", 1);
                data.put("list", salePriceLog);
                data.put("message", "查询企业销售定价记录列表成功");
            }
        } catch (Exception e) {
            logger.error("查询企业销售定价记录列表异常,", e);
            data.put("code", 0);
            data.put("message", "查询企业销售定价记录列表失败");
        }
        return returnJsonData(data);
    }

    /**
     * @description 修改企业服务费
     * @metho
     */
    @RequestMapping(value = "/updatePrice", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo updateServicePrice(String custId, String price) {
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                customerService.updateServicePrice(custId, price);
            }
        } catch (Exception e) {
            logger.error("保存企业服务费用失败失败,", e);
            return new ResponseInfoAssemble().failure(-1, "保存企业服务费用失败失败");
        }
        return new ResponseInfoAssemble().success(null);
    }

    @ResponseBody
    @RequestMapping(value = "/token", method = RequestMethod.POST)
    @CacheAnnotation
    public String token(String username, String password, String code) throws Exception{
        ResponseResult responseResult = new ResponseResult();
        responseResult.setStateCode("401");  //login fail
        if (username == null || password == null || "".equals(username) || "".equals(password)) {
            responseResult.setMsg("username or password is null");
            return JSONObject.toJSONString(responseResult);
        }

        CustomerUser u = customerUserService.getUserByName(username);
        LoginUser userdetail = null;

        if (u != null && CipherUtil.generatePassword(password).equals(u.getPassword())) {
            //寻找登录账号已有的token, 需重构
            String tokenid = (String) name2token.get(username);
            if (tokenid != null && !"".equals(tokenid)) {
                userdetail = tokenCacheService.getToken(tokenid, LoginUser.class);
            }

            if (userdetail == null) {
                name2token.remove(username);
                userdetail = new LoginUser(u.getId(), u.getAccount(), CipherUtil.encodeByMD5(u.getId() + "" + System.currentTimeMillis()));
            }

            if (1 == u.getStatus()) {
            	userdetail.addAuth("USER_FREEZE");
            } else if (3 == u.getStatus()) {
            	userdetail.addAuth("USER_NOT_EXIST");
            } else if (0 == u.getStatus()) {
                //user_type: 1=管理员 2=普通员工 3=项目管理员
            	userdetail.addAuth("ROLE_CUSTOMER");
            }
            
            userdetail.setCustId(u.getCust_id());
            userdetail.setId(u.getId());
            userdetail.setUserType(String.valueOf(u.getUserType()));
            userdetail.setRole("ROLE_CUSTOMER");

            CustomerUserPropertyDO mobile_num = customerUserDao.getProperty(u.getId().toString(), "mobile_num");
            if (mobile_num != null && StringUtil.isNotEmpty(mobile_num.getPropertyValue())) {
                responseResult.setMobile_num(mobile_num.getPropertyValue());
            } else {
                responseResult.setMobile_num("");
            }
            // 查询用户组信息
            CustomerUserGroupRelDTO cug = customerUserDao.getCustomerUserGroupByUserId(u.getId());
            if (cug != null) {
                userdetail.setUserGroupId(cug.getGroupId());
                userdetail.setUserGroupRole(String.valueOf(cug.getType()));
                userdetail.setJobMarketId(cug.getJobMarketId());
                responseResult.setUserGroupRole(String.valueOf(cug.getType()));

            }
            responseResult.setStatus(u.getStatus().toString());
            responseResult.setStateCode("200");
            responseResult.setMsg("SUCCESS");
            responseResult.setAuth(userdetail.getAuths().size()>0 ? userdetail.getAuths().get(0):"");
            responseResult.setUserName(userdetail.getUsername());
            responseResult.setCustId(userdetail.getCustId());
            responseResult.setUserType(userdetail.getUserType());
            responseResult.setUser_id(userdetail.getId().toString());
            responseResult.setTokenid(userdetail.getTokenid());
            // 处理服务权限
            responseResult.setServiceMode(ServiceModeEnum.MARKET_TASK.getCode());
            CustomerPropertyDTO cpd = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.SERVICE_MODE.getKey());
            if (cpd != null && StringUtil.isNotEmpty(cpd.getPropertyValue())) {
                responseResult.setServiceMode(cpd.getPropertyValue());
            }
            if (userdetail != null) {
                this.tokenCacheService.saveToken(userdetail);
            }
        } else {
            responseResult.setMsg("username or password is error");
        }
        return JSONObject.toJSONString(responseResult);
    }


    @ResponseBody
    @RequestMapping(value = "/m/login", method = RequestMethod.POST)
    @CacheAnnotation
    public String login(String username, String password, String realName, String code, String type,
                        String client, String area, String channel, String registerSource, String touchType) throws Exception{
        ResponseResult responseResult = new ResponseResult();
        responseResult.setStateCode("401");
        if (StringUtil.isEmpty(username) || StringUtil.isEmpty(code) || StringUtil.isEmpty(type)) {
            responseResult.setMsg("用户名/验证码不能为空");
            return JSONObject.toJSONString(responseResult);
        }
        // 校验手机验证码
        boolean success = sendSmsService.verificationCode(username, NumberConvertUtil.parseInt(type), code) == 1 ? true : false;
        if (!success) {
            responseResult.setMsg("验证码错误");
            responseResult.setStateCode("402");
            return JSONObject.toJSONString(responseResult);
        }
        CustomerUser u = customerUserService.getUserByName(username);
        if (u == null) {
            // 保存用户
            u = customerUserService.saveCustomerUser(username, "", "0", 1, realName, username, client, area, channel, registerSource, touchType);
        }
        LoginUser userDetail = null;
        if (u != null) {
            // 更新realName
            if (StringUtil.isNotEmpty(realName)) {
                UserCallConfigDTO userDto = new UserCallConfigDTO();
                userDto.setId(String.valueOf(u.getId()));
                userDto.setRealName(realName);
                customerUserService.updateuser(userDto);
            }
            //寻找登录账号已有的token, 需重构
            String tokenid = (String) name2token.get(username);
            if (tokenid != null && !"".equals(tokenid)) {
                userDetail = tokenCacheService.getToken(tokenid, LoginUser.class);
            }
            
            if (userDetail == null) {
                name2token.remove(username);
                userDetail = new LoginUser(u.getId(), u.getAccount(), CipherUtil.encodeByMD5(u.getId() + "" + System.currentTimeMillis()));
            }
            if (1 == u.getStatus()) {
            	userDetail.addAuth("USER_FREEZE");
            } else if (3 == u.getStatus()) {
            	userDetail.addAuth("USER_NOT_EXIST");
            } else if (0 == u.getStatus()) {
            	userDetail.addAuth("ROLE_CUSTOMER");
            }
            
            userDetail.setCustId(u.getCust_id());
            userDetail.setId(u.getId());
            userDetail.setUserType(String.valueOf(u.getUserType()));
            userDetail.setRole("ROLE_CUSTOMER");

            CustomerUserPropertyDO mobile_num = customerUserDao.getProperty(u.getId().toString(), "mobile_num");
            if (mobile_num != null && StringUtil.isNotEmpty(mobile_num.getPropertyValue())) {
                responseResult.setMobile_num(mobile_num.getPropertyValue());
            } else {
                responseResult.setMobile_num("");
            }
            // 查询用户组信息
            CustomerUserGroupRelDTO cug = customerUserDao.getCustomerUserGroupByUserId(u.getId());
            if (cug != null) {
                userDetail.setUserGroupId(cug.getGroupId());
                userDetail.setUserGroupRole(String.valueOf(cug.getType()));
                userDetail.setJobMarketId(cug.getJobMarketId());
                responseResult.setUserGroupRole(String.valueOf(cug.getType()));
            }
            responseResult.setStatus(u.getStatus().toString());
            responseResult.setStateCode("200");
            responseResult.setMsg("SUCCESS");
            responseResult.setAuth(userDetail.getAuths().size()>0 ? userDetail.getAuths().get(0):"");
            responseResult.setUserName(userDetail.getUsername());
            responseResult.setCustId(userDetail.getCustId());
            responseResult.setUserType(userDetail.getUserType());
            responseResult.setUser_id(userDetail.getId().toString());
            responseResult.setTokenid(userDetail.getTokenid());
            if (userDetail != null) {
                this.tokenCacheService.saveToken(userDetail);
            }
            // 记录用户行为
            saveUserOperlog(u.getId(), 1, "", "", "", client, channel, registerSource, "", userDetail.getTokenid(), "");
        } else {
            // 记录用户行为
            saveUserOperlog(0, 1, "", "", "", client, channel, registerSource, "", "", "");
            responseResult.setMsg("username or password is error");
        }
        return JSONObject.toJSONString(responseResult);
    }

    @ResponseBody
    @RequestMapping(value = "/userPage", method = RequestMethod.POST)
    @CacheAnnotation
    public String userPage(@Valid PageParam page, BindingResult error, String phone, String client, String startTime,
                           String endTime, String channel, String registerSource, String touchType, String custId) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        LoginUser lu = opUser();
        // 员工无权限
        if ("2".equals(lu.getUserType()) && "2".equals(lu.getUserGroupRole())) {
            return "";
        }
        String customerId = lu.getCustId();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            customerId = custId;
        }
        if (StringUtil.isEmpty(customerId)) {
            customerId = "0";
        }
        Page data = customerUserService.pageRegisterUser(page.getPageNum(), page.getPageSize(), customerId, phone, client, startTime, endTime, channel, registerSource, touchType);
        return returnJsonData(getPageData(data));
    }

    @ResponseBody
    @RequestMapping(value = "/selectRegisterUser/{userId}", method = RequestMethod.GET)
    @CacheAnnotation
    public String userPage(@PathVariable("userId") Long userId) {
        Map<String, Object> data = customerUserService.selectRegisterUser(userId);
        return returnJsonData(data);
    }


    @RequestMapping(value = "/verifyUserIdUniqueness/{userName}", method = RequestMethod.GET)
    @ResponseBody
    public Object verifyUserIdUniqueness(@PathVariable String userName) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (null != userService.getUserByUserName(userName)) {
            resultMap.put("code", "0");
            resultMap.put("_message", "用户名可用");
        }
        resultMap.put("data", new JSONArray());
        return JSONObject.toJSON(resultMap);
    }


    /**
     * 创建客户信息
     **/
    @RequestMapping(value = "/regist0", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public Object regist0(CustomerRegistDTO customerRegistDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        customerUserService.customerRegister(customerRegistDTO);
        resultMap.put("code", "0");
        resultMap.put("_message", "客户创建成功");
        resultMap.put("data", new JSONArray());
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @return
     * @throws Exception
     * @Description客户登录后查看信息
     */
    @RequestMapping(value = "/query/detail", method = RequestMethod.GET)
    @ResponseBody
    public Object queryCustomerById() throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("data", JSONObject.toJSON(customerService.getCustomerInfoById(opUser().getCustId())));
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @param
     * @return
     * @throws Exception
     * @Description运营登录后查看客户信息
     */
    @RequestMapping(value = "/query/detail/{uid}", method = RequestMethod.GET)
    @ResponseBody
    public Object queryCustomerById(@PathVariable String uid) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        JSONObject jsonObject = new JSONObject();
        try {
            CustomerDTO customerDTO = customerService.getCustomerInfoById(uid);
            jsonObject.put("customerInfo", customerDTO);
        } catch (Exception e) {
            logger.error("获取客户信息失败,", e);
            jsonObject.put("customerInfo", new CustomerDTO());
        }
        resultMap.put("data", jsonObject);
        return JSONObject.toJSON(resultMap);
    }

    @RequestMapping(value = "/query", method = RequestMethod.GET)
    @ResponseBody
    public Object queryAllCustomer(UserQueryParam param) throws Exception {
        return JSONObject.toJSON(customerService.getUsersByCondition(param, opUser()));
    }

    @RequestMapping(value = "/queryAll", method = RequestMethod.GET)
    @ResponseBody
    public Object queryAll(UserQueryParam param) throws Exception {
        List data = customerService.getUsersByCondition0(param, opUser());
        Map<String, Object> map = new HashMap<>();
        map.put("data", data);
        return JSONObject.toJSON(map);
    }

    @RequestMapping(value = "/update0", method = RequestMethod.POST)
    @ResponseBody
    public Object updateCustomer(@RequestBody CustomerInfoVO customerInfoVO) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            customerService.updateCustomer(customerInfoVO);
        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 已废弃
     *
     * @param jsonO
     * @return Object 返回类型
     * @throws Exception
     * @throws
     * @Title: addUser
     * @Description: 企业客户添加操作员
     */
    @Deprecated
    @RequestMapping(value = "/user/add", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public Object addUser(@RequestBody JSONObject jsonO) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        LoginUser lu = opUser();
        if (!"1".equals(lu.getUserType()) && "2".equals(lu.getUserGroupRole())) {
            resultMap.put("code", "1");
            resultMap.put("_message", "添加操作员失败");
        } else {
            UserCallConfigDTO userDTO = new UserCallConfigDTO();

            Long id = IDHelper.getUserID();
            String custId = lu.getCustId(); //"1702220241300006";
            String enpterprise_name = lu.getEnterpriseName();

            String userName = jsonO.getString("userName");
            String realName = jsonO.getString("realName");
            String title = jsonO.getString("title");
            //String remark = jsonO.getString("remark");
            String passwordBe = jsonO.getString("password");
            String password = CipherUtil.generatePassword(passwordBe);
            String mobileNumber = jsonO.getString("mobileNumber");
            String email = jsonO.getString("email");

            userDTO.setId(String.valueOf(id));
            userDTO.setCustomerId(custId);
            userDTO.setUserType(2);
            userDTO.setUserName(userName);
            userDTO.setRealName(realName);
            userDTO.setTitle(title);
            //userDTO.setRemark(remark);
            userDTO.setPassword(password);
            userDTO.setMobileNumber(mobileNumber);
            userDTO.setEmail(email);

            try {
                customerUserService.addUser(userDTO, enpterprise_name);
            } catch (Exception e) {
                logger.error("保存用户异常:", e);
                resultMap.put("code", "1");
                resultMap.put("_message", e.getMessage());
                return JSONObject.toJSONString(resultMap);
            }

            resultMap.put("code", "0");
            resultMap.put("_message", "添加操作员成功");
        }
        return JSONObject.toJSON(resultMap);
    }


    /**
     * @param
     * @return String 返回类型
     * @throws
     * @Title: updateUser
     * @Description: 删除操作员
     */
    @ResponseBody
    @RequestMapping(value = "/user/delete0", method = RequestMethod.POST)
    @CacheAnnotation
    public String deleteUser(String userName, Integer status, String customerId) throws TouchException {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LoginUser lu = opUser();
        if (!"1".equals(lu.getUserType()) && "2".equals(lu.getUserGroupRole())) {
            resultMap.put("code", "1");
            resultMap.put("_message", "无权操作");
            return JSONObject.toJSONString(resultMap);
        }
        if (StringUtil.isEmpty(userName)) {
            throw new ParamException("userName参数不能为空");
        }
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            if (StringUtil.isEmpty(customerId)) {
                throw new ParamException("customerId参数不能为空");
            }
        } else {
            customerId = lu.getCustId();
        }
        if (null == status) {
            throw new ParamException("status参数不能为空");
        }
        Integer code = customerUserService.deleteUser(String.valueOf(customerId), userName, Integer.valueOf(status));
        if (code == 1) {
            resultMap.put("code", "0");
            resultMap.put("_message", "成功");
        } else {
            resultMap.put("code", "1");
            resultMap.put("_message", "失败");
        }
        return JSONObject.toJSONString(resultMap);
    }


    /**
     * @param userDTO
     * @return String 返回类型
     * @throws
     * @Title: updateUser
     * @Description: 更新操作员
     */
    @Deprecated
    @ResponseBody
    @RequestMapping(value = "/user/update", method = {RequestMethod.PUT, RequestMethod.POST})
    @CacheAnnotation
    public String updateUser(@RequestBody UserCallConfigDTO userDTO) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LoginUser lu = opUser();
        if (!"1".equals(lu.getUserType()) && "2".equals(lu.getUserGroupRole())) {
            resultMap.put("code", "1");
            resultMap.put("_message", "更新操作员失败");
        } else {
            Integer code = customerUserService.updateuser(userDTO);
            if (code == 1) {
                resultMap.put("code", "0");
                resultMap.put("_message", "更新操作员成功");

            } else {
                resultMap.put("code", "1");
                resultMap.put("_message", "更新操作员失败");

            }
        }
        return JSONObject.toJSONString(resultMap);
    }

    /**
     * 添加/编辑/冻结/解冻用户
     *
     * @param userDTO
     * @return
     */
    @RequestMapping(value = "/user/save", method = RequestMethod.POST)
    @ResponseBody
    public String saveUser(@RequestBody UserCallConfigDTO userDTO, String opcode) throws TouchException {
        if (StringUtil.isEmpty(opcode)) {
            throw new ParamException("opcode参数不能为空");
        }
        Map<String, Object> resultMap = new HashMap<>();
        LoginUser lu = opUser();

        /*if (!"1".equals(lu.getUserType()) && "2".equals(lu.getUserGroupRole())) {
            resultMap.put("code", "1");
            resultMap.put("_message", "无权操作");
            return JSONObject.toJSONString(resultMap);
        }*/
        if ("ROLE_CUSTOMER".equals(lu.getRole()) && !"1".equals(lu.getUserType())) {
            resultMap.put("code", "1");
            resultMap.put("_message", "无权操作");
            return JSONObject.toJSONString(resultMap);
        }
        logger.info("saveUser:" + userDTO.toString());
        Integer code = null;
        if ("1".equals(opcode)) { //add
            if ("ROLE_USER".equals(lu.getRole())
                    || "admin".equals(lu.getRole())
                    || ("ROLE_CUSTOMER".equals(lu.getRole()) && "1".equals(lu.getUserType()))) {
                if (StringUtil.isEmpty(userDTO.getUserName())) {
                    throw new ParamException("userName参数不能为空");
                }
                Long id = IDHelper.getUserID();
                userDTO.setId(String.valueOf(id));
                if ("ROLE_CUSTOMER".equals(lu.getRole())) {
                    userDTO.setCustomerId(lu.getCustId());
                }
                try {
                    code = customerUserService.addUser(userDTO, null);
                } catch (Exception e) {
                    logger.error("保存用户异常:", e);
                    resultMap.put("code", "1");
                    resultMap.put("_message", e.getMessage());
                    return JSONObject.toJSONString(resultMap);
                }
            } else {
                resultMap.put("code", "1");
                resultMap.put("_message", "无权操作");
                return JSONObject.toJSONString(resultMap);
            }
        } else if ("2".equals(opcode)) { //update
            if (userDTO.getId() == null) {
                throw new ParamException("id参数不能为空");
            }
            code = customerUserService.updateuser(userDTO);
        } else if ("3".equals(opcode)) { //updatestatus
            if (StringUtil.isEmpty(userDTO.getUserName())) {
                throw new ParamException("userName参数不能为空");
            }
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                if (StringUtil.isEmpty(userDTO.getCustomerId())) {
                    throw new ParamException("customerId参数不能为空");
                }
            } else {
                userDTO.setCustomerId(lu.getCustId());
            }
            if (null == userDTO.getStatus()) {
                throw new ParamException("status参数不能为空");
            }
            code = customerUserService.deleteUser(String.valueOf(userDTO.getCustomerId()), userDTO.getUserName(), Integer.valueOf(userDTO.getStatus()));
        }

        if (code == 1) {
            resultMap.put("code", "0");
            resultMap.put("_message", "操作成功");
        } else {
            resultMap.put("code", "1");
            resultMap.put("_message", "操作失败");
        }

        return JSONObject.toJSONString(resultMap);
    }


    /**
     * 查询操作员
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/query0", method = RequestMethod.GET)
    @CacheAnnotation
    public String getUserList(Integer pageNum, Integer pageSize, String name, String realName) {
        LoginUser lu = opUser();
        if (!"1".equals(lu.getUserType()) && "2".equals(lu.getUserGroupRole())) {
            return "";
        }

        //String customerId ="1702210227030000";
        return customerUserService.getUser(pageNum, pageSize, lu.getCustId(), name, realName, "2");
    }


    /**
     * 查询操作员
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/user/queryV1", method = RequestMethod.GET)
    @CacheAnnotation
    public String getUserList(@Valid PageParam page, BindingResult error, String name, String realName,
                              String userGroupId, String groupRoleType, String custId, String uid, String seatId,
                              String endAccount, String endSeatId, String callType, String callChannel, String jobId,
                              String queryall, String userType, String projectId, String status, String notGroupRoleType) {
        LoginUser lu = opUser();
        // 员工不能看团队管理
        if ("2".equals(lu.getUserType()) && "2".equals(lu.getUserGroupRole())) {
            logger.warn("员工:[{}]为普通员工,无权查询员工列表", lu.getId());
            return "";
        }

        if (StringUtil.isEmpty(queryall) || !"all".equals(queryall)) {
            if (error.hasFieldErrors()) {
                return getErrors(error);
            }
        }

        String customerId = lu.getCustId();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            customerId = custId;
        }
        return customerUserService.getUser_V1(page.getPageNum(), page.getPageSize(), customerId, name, realName,
                opUser(), userGroupId, groupRoleType, uid, seatId, endAccount, endSeatId, callType, callChannel,
                jobId, userType, projectId, status, notGroupRoleType);
    }

    /*@ResponseBody
    @RequestMapping(value = "/user/heartbeat", method = RequestMethod.POST)
    @CacheAnnotation
    public String heartbeat(Integer pageNum, Integer pageSize, String name, String realName) {
        LoginUser lu = opUser();
        customerService.heartbeat(lu.getId());
        return "success";
    }*/

    @ResponseBody
    @RequestMapping(value = "/admin/heartbeat", method = RequestMethod.POST)
    @CacheAnnotation
    public String adminHeartbeat() {
        LoginUser lu = opUser();
        customerService.heartbeat(lu.getId());
        return "success";
    }

    /**
     * 查询操作员
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/userList", method = RequestMethod.GET)
    @CacheAnnotation
    public String getCustomerUserList(String name, String realName) {
        String customerId = opUser().getCustId();
        return customerUserService.getCustomerUserList(opUser().getUserType(), String.valueOf(opUser().getId()), customerId, name, realName);
    }


    @RequestMapping(value = "/user/verifyUniqueness/{identifyType}", method = RequestMethod.GET)
    @ResponseBody
    public Object verifyUniqueness(@PathVariable String identifyType, String identifyValue) {
        Map<String, Object> resultMap = new HashMap<String, Object>();

        Object code = null;
        Integer codePass = null;

        Long userId = opUser().getId();
        //Long userId  = Long.parseLong(("17042005512700000"));
        if (identifyType.equals("1")) {
            code = customerUserService.getUserByName(identifyValue);
        } else if (identifyType.equals("2")) {
            code = customerUserService.getUserBymobileNum(identifyValue);
        } else if (identifyType.equals("3")) {
            code = customerUserService.getUserByEmail(identifyValue);
        } else if (identifyType.equals("4")) {
            code = customerService.getUserByEnterpriseName(identifyValue);
        } else if (identifyType.equals("5")) {
            //验证支付密码和登陆密码是否一致
            codePass = userService.getUserByLoginPassWord(identifyValue, userId);
        }

        if (!"5".equals(identifyType)) {
            if (null == code) {
                resultMap.put("code", "0");
                resultMap.put("_message", "信息可用");
            } else {
                resultMap.put("code", "20008");
                resultMap.put("_message", "已经被注册，请重新输入");
            }
        } else {
            if (codePass != null && codePass == 0) {
                resultMap.put("code", "0");
                resultMap.put("_message", "密码可用");
            } else {
                resultMap.put("code", "20008");
                resultMap.put("_message", "支付密码和登陆密码相同，请重新输入");
            }
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * TODO 不在使用
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/customerCallCenterType", method = RequestMethod.GET)
    @CacheAnnotation
    public String getCustomerCallCenterType() {
        LoginUser u = opUser();
        Map<String, Object> data = customerService.getCustomerCallCenterType(u.getCustId(), u.getId().toString());
        return JSON.toJSONString(data);
    }

    @ResponseBody
    @RequestMapping(value = "/customerCallCenterType_V1", method = RequestMethod.GET)
    @CacheAnnotation
    public String getCustomerCallCenterType_V1() {
        LoginUser u = opUser();
        SeatCallCenterConfig data;
        try {
            data = seatsService.selectUserSeatConfig(u.getId().toString(), u.getCustId());
        } catch (Exception e) {
            data = new SeatCallCenterConfig();
            logger.error("获取坐席呼叫中心配置失败,", e);
        }
        return JSON.toJSONString(data);
    }

    @ResponseBody
    @RequestMapping(value = "/getCustomerGroupCallCenterType", method = RequestMethod.GET)
    @CacheAnnotation
    public String getCustomerGroupCallCenterType(String customerGroupId, String resourceId) {
        LoginUser u = opUser();
        SeatCallCenterConfig data = customerService.getCustomerGroupCallCenterType(u.getCustId(), customerGroupId, String.valueOf(u.getId()), resourceId);
        return JSON.toJSONString(data);
    }

    @ResponseBody
    @RequestMapping(value = "/saveUpdateCustomerCallCenterType", method = RequestMethod.POST)
    public String saveUpdateCustomerCallCenterType(@RequestBody XFCallCenterConfig xfCallCenterConfig) {
        LoginUser lu = opUser();
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject json = new JSONObject();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            if (StringUtil.isEmpty(xfCallCenterConfig.getCustId())) {
                map.put("result", 0);
                map.put("message", "custId必填");
                json.put("data", map);
                return json.toJSONString();
            }
            int code = customerService.saveUpdateCustomerCallCenterType(xfCallCenterConfig);
            if (code == 1) {
                map.put("result", 1);
                map.put("message", "成功");
            } else {
                map.put("result", 0);
                map.put("message", "失败");
            }
        } else {
            map.put("result", 0);
            map.put("message", "权限不足");
        }
        json.put("data", map);
        return json.toJSONString();
    }

    @RequestMapping(value = "/securityCenter/", method = RequestMethod.GET)
    @ResponseBody
    public Object securityCenter() throws Exception {
        LoginUser lu = opUser();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("data", JSONObject.toJSON(customerService.getSecurityCenterInfo(lu.getId(), lu.getCustId())));
        return com.alibaba.fastjson.JSONObject.toJSON(resultMap);
    }

    /**
     * @description 添加修改主信息（管理员账号密码和 外显号）
     * @author:duanliying
     * @method
     * @date: 2018/8/22 17:32
     */
    @RequestMapping(value = "/MainMessage", method = RequestMethod.POST)
    @ResponseBody
    public Object MainMessage(@RequestBody SeatPropertyDTO customerPropertyDTO) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                seatsService.saveMainMessage(customerPropertyDTO);
                map.put("result", 1);
                map.put("message", "坐席主信息添加成功");
            }
        } catch (Exception e) {
            logger.error("坐席主信息信息添加" + e);
            map.put("result", 0);
            map.put("message", "坐席主信息添加失败");
        }
        json.put("data", map);
        return json.toJSONString();
    }

    @RequestMapping(value = "/saveCallCenterData", method = RequestMethod.POST)
    @ResponseBody
    public Object saveCallCenterData(@RequestBody SeatPropertyDTO customerPropertyDTO) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                seatsService.saveMainMessageV4(customerPropertyDTO);
                map.put("result", 1);
                map.put("message", "坐席主信息添加成功");
            }
        } catch (Exception e) {
            logger.error("坐席主信息信息添加" + e);
            map.put("result", 0);
            map.put("message", "坐席主信息添加失败");
        }
        json.put("data", map);
        return json.toJSONString();
    }


    /**
     * @description 添加坐席信息（坐席集合）
     * @author:duanliying
     * @method
     * @date: 2018/8/20 20:17
     */
    @RequestMapping(value = "/addSeatsMessage", method = RequestMethod.POST)
    @ResponseBody
    public Object addSeatsMessage(@RequestBody SeatPropertyDTO customerPropertyDTO) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                seatsService.addSeatsList(customerPropertyDTO);
                map.put("result", 1);
                map.put("message", "坐席添加成功");
            }
        } catch (Exception e) {
            logger.error("坐席信息添加" + e);
            map.put("result", 0);
            map.put("message", "坐席添加失败");
        }
        json.put("data", map);
        return json.toJSONString();
    }

    @RequestMapping(value = "/addSeatsMessageV1", method = RequestMethod.POST)
    @ResponseBody
    public Object addSeatsMessageV1(@RequestBody SeatPropertyDTO customerPropertyDTO) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                seatsService.addSeatsListV1(customerPropertyDTO);
                map.put("result", 1);
                map.put("message", "坐席添加成功");
            }
        } catch (Exception e) {
            logger.error("坐席信息添加" + e);
            map.put("result", 0);
            map.put("message", "坐席添加失败");
        }
        json.put("data", map);
        return json.toJSONString();
    }


    /**
     * @description 修改坐席的有效状态
     * @author:duanliying
     * @method
     * @date: 2018/8/23 17:04
     */
    @ResponseBody
    @RequestMapping(value = "/setStatus", method = RequestMethod.GET)
    public Object updateSeatsStatus(String status, String userId) {
        Map<String, Object> map = new HashMap<String, Object>();
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                seatsService.updateSeatsList(status, userId);
                map.put("result", 1);
                map.put("message", "坐席状态修改成功");
            }
        } catch (Exception e) {
            logger.error("坐席状态修改" + e);
            map.put("result", 0);
            map.put("message", "坐席状态修改失败");
        }
        json.put("data", map);
        return json.toJSONString();
    }

    @ResponseBody
    @RequestMapping(value = "/updateCustCallBackApparentNumber", method = RequestMethod.GET)
    public String updateCustCallBackApparentNumber(String apparentNumber, String custId) {
        int result = 0;
        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
            result = customerService.updateCustCallBackApparentNumber(custId, apparentNumber);
        } else {
            return returnError("权限不足");
        }
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();
    }

    /**
     * 项目添加
     *
     * @param jsonObject
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/saveMarketProject", method = RequestMethod.POST)
    public String saveMarketProject(@RequestBody JSONObject jsonObject) {
        int result = 0;
        MarketProjectDTO dto = new MarketProjectDTO();
        dto.setIndustryId(jsonObject.getInteger("industryId"));
        dto.setName(jsonObject.getString("name"));
        String custId = jsonObject.getString("custId");
        if (StringUtil.isEmpty(custId)) {
            custId = opUser().getCustId();
        }
        result = marketProjectService.saveMarketProjectAndSea(dto, custId, opUser().getId());
        // 处理项目管理员
        String projectUserId = jsonObject.getString("projectUserId");
        if (StringUtil.isNotEmpty(projectUserId)) {
            result = marketProjectService.updateMarketProject(dto, 2, projectUserId, opUser().getCustId());
        }
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();
    }

    /**
     * 修改项目信息
     *
     * @param jsonObject
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateMarketProject", method = RequestMethod.POST)
    public String updateMarketProject(@RequestBody JSONObject jsonObject) {
        int result = 0;
        MarketProjectDTO marketProjectDTO = new MarketProjectDTO();
        marketProjectDTO.setId(jsonObject.getInteger("id"));
        marketProjectDTO.setIndustryId(jsonObject.getInteger("industryId"));
        marketProjectDTO.setName(jsonObject.getString("name"));
        marketProjectDTO.setStatus(jsonObject.getInteger("status"));
        int operation = 0;
        if (jsonObject.getInteger("operation") != null) {
            operation = jsonObject.getInteger("operation");
        }
        String projectUserId = jsonObject.getString("projectUserId");
        result = marketProjectService.updateMarketProject(marketProjectDTO, operation, projectUserId, opUser().getCustId());
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();
    }

    /**
     * 关闭项目
     *
     * @param jsonObject
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/closeMarketProject", method = RequestMethod.POST)
    public String closeMarketProject(@RequestBody JSONObject jsonObject) {
        int result = 0;
        MarketProjectDTO marketProjectDTO = new MarketProjectDTO();
        marketProjectDTO.setId(jsonObject.getInteger("id"));
        marketProjectDTO.setStatus(jsonObject.getInteger("status"));
        result = marketProjectService.closeMarketProject(marketProjectDTO);
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();
    }

    @ResponseBody
    @RequestMapping(value = "/pageMarketProject", method = RequestMethod.POST)
    public String pageMarketProject(@Valid PageParam pageParam, BindingResult error, MarketProjectDTO marketProject) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasErrors()) {
            responseJson.setData(getErrors(error));
            return JSON.toJSONString(responseJson);
        }
        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
            Page page = marketProjectService.pageMarketProject(pageParam.getPageNum(), pageParam.getPageSize(), marketProject);
            responseJson.setData(getPageData(page));
        } else {
            responseJson.fail(0, "权限不足");
        }
        return JSON.toJSONString(responseJson);
    }

    @ResponseBody
    @RequestMapping(value = "/listMarketProject", method = RequestMethod.POST)
    public String listMarketProject(MarketProjectDTO marketProject) {
        ResponseJson responseJson = new ResponseJson();
        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
            List<MarketProjectDTO> list = marketProjectService.listMarketProject(marketProject);
            responseJson.setData(list);
        } else {
            responseJson.fail(0, "权限不足");
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 查询项目下关联的企业
     *
     * @param marketProjectId
     * @param enterpriseName
     * @return 返回选中的企业和未选择的企业
     */
    @RequestMapping(value = "/listSelectCustomerByMarketProjectId", method = RequestMethod.POST)
    @ValidatePermission(role = "admin,ROLE_USER")
    @ResponseBody
    public String listUserByGroupId(String marketProjectId, String enterpriseName) {
        ResponseJson responseJson = new ResponseJson();
        Map<String, Object> result = new HashMap<>();
        try {
            result = marketProjectService.listSelectCustomerByMarketProjectId(marketProjectId, enterpriseName, opUser());
        } catch (Exception e) {
            logger.error("查询项目下已选择和未选择企业列表失败,", e);
            result.put("selected", new ArrayList<>());
            result.put("unselected", new ArrayList<>());
        }
        responseJson.setData(result);
        return JSON.toJSONString(responseJson);
    }

    /**
     * 存项目和企业的关联关系
     *
     * @param params
     * @return com.bdaim.sale.action.util.ResponseCommon
     */
    @RequestMapping(value = "/saveMarketProjectRelationEnterprises", method = RequestMethod.POST)
    @ValidatePermission(role = "admin,ROLE_USER")
    @ResponseBody
    public String saveMarketProjectRelationEnterprises(@RequestBody Map<String, Object> params) {
        int result = 0;
        try {
            //项目Id
            String marketProjectId = String.valueOf(params.get("marketProjectId"));
            List<String> custIds = (List<String>) params.get("custIds");
            result = marketProjectService.saveMarketProjectRelationEnterprises(marketProjectId, custIds);
        } catch (Exception e) {
            logger.error("查询项目下已选择和未选择企业列表失败,", e);
            return returnError();
        }
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();
    }


    @RequestMapping(value = "/listCustomerMarketProject", method = RequestMethod.POST)
    @ResponseBody
    public String listCustomerMarketProject(String custId) {
        ResponseJson responseJson = new ResponseJson();
        List<MarketProjectDTO> list = null;
        try {
            String projectUserId = "";
            if (RoleEnum.ROLE_CUSTOMER.getRole().equals(opUser().getRole())) {
                custId = opUser().getCustId();
                // 普通用户不能访问项目列表
                if ("2".equals(opUser().getUserType())) {
                    list = new ArrayList<>();
                    responseJson.setData(list);
                    return JSON.toJSONString(responseJson);
                } else if ("3".equals(opUser().getUserType())) {
                    projectUserId = String.valueOf(opUser().getId());
                }
            }
            if (StringUtil.isEmpty(custId)) {
                throw new ParamException("参数custId不能为空");
            }

            list = marketProjectService.listCustomerMarketProject(custId, projectUserId);
        } catch (Exception e) {
            logger.error("查询企业关联项目失败,", e);
        }
        responseJson.setData(list);
        return JSON.toJSONString(responseJson);
    }

    @ResponseBody
    @RequestMapping(value = "/updateSettlementType", method = RequestMethod.POST)
    @ValidatePermission(role = "admin,ROLE_USER")
    public String updateSettlementType(String custId, int settlementType, String creditAmount) {
        ResponseJson responseJson = new ResponseJson();
        int code = 0;
        try {
            code = customerService.updateSettlementType(custId, settlementType, creditAmount);
        } catch (Exception e) {
            logger.error("更改客户的结算类型失败,custId:" + custId + ",settlementType:" + settlementType + ",creditAmount:" + creditAmount);
        }
        if (code == 1) {
            responseJson.success();
        } else {
            responseJson.fail();
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 保存客户售价设置
     *
     * @param param
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/savePriceSetting", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public Object savePriceSetting(@RequestBody CustomerPriceConfigDTO param) {
        ResponseJson responseJson = new ResponseJson();
        int code = 0;
        try {
            code = customerService.savePriceSetting(param);
        } catch (Exception e) {
            logger.error("保存客户售价失败,", e);
            responseJson.setData("保存客户售价失败");
        }
        if (code == 1) {
            responseJson.success();
        } else {
            responseJson.fail();
        }
        return JSON.toJSONString(responseJson);
    }

    @RequestMapping(value = "/selectCustomerPriceDetail", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String selectCustomerPriceDetail(String custId, String callType) {
        Map<String, Object> data = null;
        try {
            data = customerService.selectCustomerPriceAndSupplierList(custId, callType);
        } catch (Exception e) {
            logger.error("查询单个客户售价详情失败,", e);
        }
        return returnJsonData(data);
    }

    @RequestMapping(value = "/listMonthBill", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String listCustomerMonthBill(@Valid PageParam page, BindingResult error, String yearMonth, String custId, String custName) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        if (StringUtil.isEmpty(yearMonth)) {
            throw new ParamException("yearMonth参数不能为空");
        }
        Map<String, Object> data = null;
        try {
            data = customerService.listCustomerMonthBill(yearMonth, page.getPageNum(), page.getPageSize(), custName, custId);
        } catch (Exception e) {
            logger.error("查询客户月账单失败,", e);
        }
        return returnJsonData(data);
    }

    @RequestMapping(value = "/listBill", method = RequestMethod.POST)
    @ResponseBody
    public String listBill(@Valid PageParam page, BindingResult error, String custId, String orderNo, String startTime, String endTime, String type, String resourceId) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        if (StringUtil.isEmpty(startTime)) {
            throw new ParamException("startTime参数不能为空");
        }
        if (StringUtil.isEmpty(endTime)) {
            throw new ParamException("endTime参数不能为空");
        }
        int typeCode = -1;
        if (StringUtil.isEmpty(type)) {
            throw new ParamException("type参数不能为空");
        }
        typeCode = NumberConvertUtil.parseInt(type);
        Map<String, Object> data = null;
        // 处理前台客户权限
        if (RoleEnum.ROLE_CUSTOMER.getRole().equals(opUser().getRole())) {
            custId = opUser().getCustId();
        }
        try {
            data = customerService.listCustomerBill(custId, startTime, endTime, orderNo, typeCode, page.getPageNum(), page.getPageSize(), resourceId);
        } catch (Exception e) {
            logger.error("查询客户月账单失败,", e);
        }
        return returnJsonData(data);
    }

    @RequestMapping(value = "/listCustomerUseSupplier", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String listCustomerCheckedSupplier(String custId, String type) {
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId参数不能为空");
        }
        if (StringUtil.isEmpty(type)) {
            throw new ParamException("type参数不能为空");
        }
        List<Map<String, Object>> data = null;
        try {
            data = customerService.listUseSupplier(custId, Integer.parseInt(type));
        } catch (Exception e) {
            logger.error("查询客户所选择的渠道列表异常,", e);
        }
        return returnJsonData(data);
    }

    @RequestMapping(value = "/listCustomerUseResource", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String listCustomerCheckedResource(String custId, String supplierId, String type) {
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId参数不能为空");
        }
        if (StringUtil.isEmpty(supplierId)) {
            throw new ParamException("supplierId参数不能为空");
        }
        if (StringUtil.isEmpty(type)) {
            throw new ParamException("type参数不能为空");
        }
        List<Map<String, Object>> data = null;
        try {
            data = customerService.listUseSupplierResource(custId, supplierId, Integer.parseInt(type));
        } catch (Exception e) {
            logger.error("查询客户所选择的渠道列表异常,", e);
        }
        return returnJsonData(data);
    }


    @RequestMapping(value = "/exportExcelListBillByType", method = RequestMethod.GET)
    @ValidatePermission(role = "admin,ROLE_USER")
    public void exportExcelListBillByType(HttpServletResponse response, String resourceId, String type, String orderNo, String custId, String startTime, String endTime) {
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId参数不能为空");
        }
        if (StringUtil.isEmpty(type)) {
            throw new ParamException("type参数不能为空");
        }
        if (StringUtil.isEmpty(startTime)) {
            throw new ParamException("startTime参数不能为空");
        }
        if (StringUtil.isEmpty(endTime)) {
            throw new ParamException("endTime参数不能为空");
        }
        int typeCode = -1;
        if (StringUtil.isEmpty(type)) {
            throw new ParamException("type参数不能为空");
        }
        typeCode = NumberConvertUtil.parseInt(type);
        response.setContentType("application/json;charset=utf-8");
        try {
            customerService.exportExcelListBillByType(response, "", resourceId, typeCode, orderNo, custId, startTime, endTime);
        } catch (Exception e) {
            logger.error("客户月账单详情导出失败,", e);
        }
    }

    @RequestMapping(value = "/exportExcelRechargeDeduction", method = RequestMethod.GET)
    @ValidatePermission(role = "admin,ROLE_USER")
    public void exportExcelRechargeDeduction(HttpServletResponse response, String type, String orderNo, String custId, String startTime, String endTime) {
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId参数不能为空");
        }
        if (StringUtil.isEmpty(startTime)) {
            throw new ParamException("startTime参数不能为空");
        }
        if (StringUtil.isEmpty(endTime)) {
            throw new ParamException("endTime参数不能为空");
        }
        int typeCode = 0;
        if (StringUtil.isNotEmpty(type)) {
            typeCode = NumberConvertUtil.parseInt(type);
        }
        response.setContentType("application/json;charset=utf-8");
        try {
            customerService.exportExcelRechargeDeduction(response, typeCode, orderNo, custId, startTime, endTime);
        } catch (Exception e) {
            logger.error("客户月账单详情导出失败,", e);
        }
    }


    @RequestMapping(value = "/exportExcelMonthBill", method = RequestMethod.GET)
    @ValidatePermission(role = "admin,ROLE_USER")
    public void exportExcelMonthBill(HttpServletResponse response, String custId, String year, String month) {
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId参数不能为空");
        }
        if (StringUtil.isEmpty(year)) {
            throw new ParamException("year参数不能为空");
        }
        if (StringUtil.isEmpty(month)) {
            throw new ParamException("month参数不能为空");
        }
        response.setContentType("application/json;charset=utf-8");
        try {
            DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            LocalDateTime startTime = LocalDateTime.of(Integer.parseInt(year), Integer.parseInt(month), 1, 0, 0, 0, 0);
            LocalDateTime endTime = startTime.plusMonths(1);
            customerService.exportExcelMonthBill(response, custId, startTime.format(dateTimeFormatter), endTime.format(dateTimeFormatter));
        } catch (Exception e) {
            logger.error("客户月账单总数据导出失败,", e);
        }
    }

    @RequestMapping(value = "/saveCustExportPermission", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String saveCustExportPermission(String custId, String propertyValue) {
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId参数不能为空");
        }
        if (StringUtil.isEmpty(propertyValue)) {
            throw new ParamException("propertyValue参数不能为空");
        }
        int code = 0;
        try {
            code = customerService.saveCustExportPermission(custId, propertyValue);
        } catch (Exception e) {
            code = 0;
        }
        if (code == 1) {
            return returnSuccess();
        } else {
            return returnError();
        }
    }

    @RequestMapping(value = "/saveApparentNumber", method = RequestMethod.POST)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String saveOrUpdateApparentNumber(@RequestBody ApparentNumber model) {
        int code = 0;
        try {
            code = customerService.saveApparentNumber(model);
        } catch (Exception e) {
            logger.error("保存企业外显号失败,参数:" + model, e);
            code = 0;
        }
        if (code == 1) {
            return returnSuccess();
        } else {
            return returnError();
        }
    }


    @RequestMapping(value = "/pageApparentNumber", method = RequestMethod.POST)
    @ResponseBody
    public String pageApparentNumber(@Valid PageParam pageParam, BindingResult error, ApparentNumberQueryParam model) {
        if (error.hasErrors()) {
            return getErrors(error);
        }
        String custId = null;
        if (StringUtil.isEmpty(model.getCustId())) {
            //沒有传custid证明是前台查询
            custId = opUser().getCustId();
            model.setCustId(custId);
        }
        Page page = null;
        try {
            model.setPageNum(pageParam.getPageNum());
            model.setPageSize(pageParam.getPageSize());
            page = customerService.pageApparentNumber(model);
        } catch (Exception e) {
            logger.error("查询企业外显号分页失败,参数:" + model, e);
        }
        return returnJsonData(getPageData(page));
    }

    @RequestMapping(value = "/listApparentNumber", method = RequestMethod.POST)
    @ResponseBody
    public String pageApparentNumber(ApparentNumberQueryParam model) {
        List<ApparentNumberDTO> list = null;
        try {
            if (!isBackendUser()) {
                model.setCustId(opUser().getCustId());
            }
            list = customerService.listApparentNumber(model);
        } catch (Exception e) {
            logger.error("查询企业外显号列表失败,参数:" + model, e);
        }
        return returnJsonData(list);
    }

    @RequestMapping(value = "/saveCustomerProperty", method = RequestMethod.POST)
    @ResponseBody
    public String saveCustomerProperty(@RequestBody CustomerProperty param) {
        int code = 0;
        try {
            if (StringUtil.isEmpty(param.getCustId())) {
                param.setCustId(opUser().getCustId());
            }
            code = customerService.saveCustomerProperty(param);
        } catch (Exception e) {
            logger.error("保存客户属性失败,参数:" + param, e);
            code = 0;
        }
        if (code == 1) {
            return returnSuccess();
        } else {
            return returnError();
        }
    }

    @RequestMapping(value = "/getCustomerProperty", method = RequestMethod.GET)
    @ResponseBody
    public String getCustomerProperty(CustomerProperty param) {
        CustomerPropertyDTO data = null;
        try {
            if (StringUtil.isEmpty(param.getCustId())) {
                param.setCustId(opUser().getCustId());
            }
            data = customerService.getCustomerProperty(param);
        } catch (Exception e) {
            logger.error("获取客户属性失败,参数:" + param, e);

        }
        return returnJsonData(data);
    }

    /**
     * 获取客户资源配置信息
     */
    @RequestMapping(value = "/getCustomerResourceConfig", method = RequestMethod.GET)
    @ValidatePermission(role = "admin,ROLE_USER")
    @CacheAnnotation
    @ResponseBody
    public String getCustomerResourceConfig(String custId) {
//        LoginUser u = opUser();
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId参数不能为空");
        }
        Map<String, String> data = customerService.getCustomerResourceConfig(custId);
        return JSON.toJSONString(data);
    }


    /**
     * 根据用户名区间，模糊查询用户
     */
    @RequestMapping(value = "/getUserByRegionName", method = RequestMethod.GET)
    @CacheAnnotation
    @ResponseBody
    public String getSeatsByUserNameNoPage(String custId, String start, String end) {
//        LoginUser u = opUser();
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId参数不能为空");
        }
        if (StringUtil.isEmpty(start) && StringUtil.isEmpty(end)) {
            throw new ParamException("start参数和end参数不能全为空");
        }
//        if (StringUtil.isEmpty(end)) {
//            throw new ParamException("end参数不能为空");
//        }
        String data = customerService.getSeatsByUserNameNoPage(custId, start, end);
        return data;
    }


    /**
     * 查询客户配置的通话资源
     *
     * @param type 1-呼叫中心 2-双呼 3-机器人 为空获取全部
     * @return
     */
    @RequestMapping(value = "/listVoiceResource", method = RequestMethod.GET)
    @ResponseBody
    public String listVoiceResourceByType(String type, String custId) {
        CustomCallConfigDTO result = null;
        try {
            Integer callType = null;
            if (StringUtil.isNotEmpty(type)) {
                callType = NumberConvertUtil.parseInt(type);
            }
            if (!isBackendUser()) {
                custId = opUser().getCustId();
            }
            result = supplierService.getCustomerCallPriceConfig(custId, callType);
        } catch (Exception e) {
            logger.error("查询资源列表失败,", e);
        }
        return returnJsonData(result);
    }

    /**
     * 查询客户指定渠道下配置的外显规则
     *
     * @param custId
     * @param callChannel
     * @return
     */
    @RequestMapping(value = "/selectChannelApparentNumber", method = RequestMethod.GET)
    @ValidatePermission(role = "admin,ROLE_USER")
    @ResponseBody
    public String updateExtractTime(String custId, String callChannel) {
        Map<String, Object> data = new HashMap<>();
        data.put("rule", "");
        data.put("number", "");
        // 查询客户指定渠道下配置的外显规则
        CustomerProperty p = new CustomerProperty();
        p.setCustId(custId);
        p.setPropertyName(callChannel + "_apparent_number_rule");
        CustomerPropertyDTO dto = customerService.getCustomerProperty(p);
        if (dto != null) {
            data.put("rule", dto.getPropertyValue());
            // 查询指定的外显号
            if ("specify".equals(dto.getPropertyValue())) {
                p.setPropertyName(callChannel + "_apparent_number");
                dto = customerService.getCustomerProperty(p);
                if (dto != null) {
                    data.put("number", dto.getPropertyValue());
                }
            }
        }
        return returnJsonData(data);
    }


    /**
     * 获取未被分配的项目，和uid已经分配的项目
     *
     * @param
     * @param uid
     * @return
     */
    @RequestMapping(value = "/marketproject/selected", method = RequestMethod.GET)
    @ResponseBody
    public String getMarketProject(String uid) {
        LoginUser lu = opUser();
        String custId = lu.getCustId();
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId is null");
        }
        Map<String, List<Map<String, Object>>> result = customerService.getMarketProject(custId, uid);
        return returnJsonData(result);
    }

    /**
     * 项目列表
     *
     * @param
     * @param projectName
     * @param id
     * @param status
     * @param createTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/project/query", method = RequestMethod.GET)
    public String getProjectList(String projectName, String id,
                                 Integer status, String createTime, String endTime,
                                 Integer pageNum, Integer pageSize, String projectUser, String custId) {
        LoginUser lu = opUser();
        //后台根据企业id查询所有项目
        if (StringUtil.isEmpty(custId)) {
            custId = lu.getCustId();
        }
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId is null");
        }
        if (pageNum == null || pageSize == null) {
            throw new ParamException("参数不正确");
        }
        // 项目管理员
        if ("3".equals(opUser().getUserType())) {
            projectUser = opUser().getUsername();
        }
        JSONObject r = marketProjectService.getMarketProjectList(lu, custId, projectName, id, status, createTime, endTime, pageNum, pageSize, projectUser);
        return returnJsonData(r);
    }

    /**
     * @description 号码保护设置
     * @method
     * @date: 2019/7/2 10:53
     */
    @ResponseBody
    @RequestMapping(value = "/phoneProtect", method = RequestMethod.GET)
    public String setPhoneProtect(String type, String status) {
        ResponseJson responseJson = new ResponseJson();
        try {
            if (type == null || status == null) {
                responseJson.setMessage("参数不正确");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            String custId = opUser().getCustId();
            if ("2".equals(opUser().getUserType())) {
                return returnError("权限不足");
            }
            customerService.setPhoneProtectByCust(type, status, custId);
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);

    }

    /**
     * @description 获取号码保护设置
     * @method
     * @date: 2019/7/2 10:53
     */
    @ResponseBody
    @RequestMapping(value = "/phoneStatus", method = RequestMethod.GET)
    public String getPhoneProtectStatus() {
        ResponseJson responseJson = new ResponseJson();
        try {

            String custId = opUser().getCustId();
            Map<String, Object> phoneProtectStatus = customerService.getPhoneProtectStatus(custId);
            responseJson.setData(phoneProtectStatus);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);

    }

    @ResponseBody
    @RequestMapping(value = "/getB2BTcbQuantity", method = RequestMethod.GET)
    public String getB2BTcbQuantity(String custId) {
        ResponseInfo responseJson = new ResponseInfo();
        try {
            if (!isBackendUser()) {
                custId = opUser().getCustId();
            }
            long quantity = b2BTcbService.getB2BTcbQuantity(custId);
            responseJson.setData(quantity);
        } catch (Exception e) {
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);

    }

    /**
     * @param param
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/getClueDataToSea", method = RequestMethod.POST)
    public String getClueDataToSea(@RequestBody JSONObject param) {
        ResponseInfo responseJson = new ResponseInfo();
        try {
            long quantity = b2BTcbService.doClueDataToSea(opUser().getCustId(), opUser().getId(), param.getIntValue("seaType"),
                    param.getIntValue("mode"), param.getString("seaId"), (List<String>) param.get("companyIds"),
                    param.getIntValue("number"),
                    param.getString("busiType"), param);
            responseJson.setData(quantity);
        } catch (TouchException e) {
            logger.error("企业B2B套餐领取至公海/私海失败,", e);
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }catch (Exception e) {
            logger.error("企业B2B套餐领取至公海/私海失败,", e);
            responseJson.setCode(-1);
            responseJson.setMessage("企业B2B套餐领取至公海/私海失败");
        }
        return JSON.toJSONString(responseJson);

    }
}

