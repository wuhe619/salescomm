package com.bdaim.customer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.rbac.dto.UserDTO;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;

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
    private static Logger logger = Logger.getLogger(CustomerAction.class);
    @Resource
    CustomerService customerService;

//    @ResponseBody
//    @RequestMapping(value = "/token", method = RequestMethod.POST)
//    @CacheAnnotation
//    public String token(String username, String password, String code) {
//    	ResponseResult responseResult = new ResponseResult();
//    	responseResult.setStateCode("401");  //login fail
//    	if(username==null || password==null || "".equals(username) || "".equals(password)) {
//    		responseResult.setMsg("username or password is null");
//    		return JSONObject.toJSONString(responseResult);
//    	}
//    	
//    	CustomerUserDO u = customerService.getUserByName(username);
//    	List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
//    	LoginUser userdetail = null;
//    	
//    	
//        if (u != null && CipherUtil.generatePassword(password).equals(u.getPassword())) {
//            logger.info("登陆框，用户：" + u.getAccount() + " 状态：" + u.getStatus());
//            if (1 == u.getStatus()) {
//                auths.add(new SimpleGrantedAuthority("USER_FREEZE"));
//            } else if (3 == u.getStatus()) {
//                auths.add(new SimpleGrantedAuthority("USER_NOT_EXIST"));
//            } else if (0 == u.getStatus()) {
//                //user_type: 1=管理员 2=普通员工
//                auths.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
//            }
//            userdetail = new LoginUser(u.getAccount(), u.getPassword(), auths);
//            userdetail.setCustId(u.getCust_id());
//            userdetail.setId(u.getId());
//            userdetail.setUserType(String.valueOf(u.getUserType()));
//            userdetail.setRole("ROLE_CUSTOMER");
//            TokenManager.createNewToken(userdetail);
//            
//            
//            responseResult.setStateCode("200");
//            responseResult.setMsg("SUCCESS");
//            responseResult.setAuth(userdetail.getAuthorities().toArray()[0].toString());
//            responseResult.setUserName(userdetail.getUsername());
//            responseResult.setCustId(userdetail.getCustId());
//            responseResult.setUserType(Integer.valueOf(userdetail.getUserType()));
//            responseResult.setUser_id(userdetail.getId().toString());
//            responseResult.setTokenid(userdetail.getTokenid());
//        }else {
//        	responseResult.setMsg("username or password is error");
//        }
//        
//        
//        return JSONObject.toJSONString(responseResult);
//    }
    
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

        Page list = customerService.getUser(page, customerId, name, realName, mobileNum);
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
    public Object regist(@RequestBody CustomerRegistDTO customerRegistDTO) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        customerService.registerOrUpdateCustomer(customerRegistDTO);
        resultMap.put("code", "0");
        resultMap.put("_message", "客户创建成功");
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @Title: queryCustomer
     * @Description: 企业管理 客户详情
     */
    @RequestMapping(value = "/query/list", method = RequestMethod.GET)
    @ResponseBody
    public Object queryCustomer(@Valid PageParam page, BindingResult error, CustomerRegistDTO customerRegistDTO) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        LoginUser lu = opUser();
        Page list = null;
        Map<Object, Object> map = new HashMap<Object, Object>();
        JSONObject json = new JSONObject();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            list = customerService.getCustomerInfo(page, customerRegistDTO);
            map.put("list", list);
            //图片根路径
            String preUrl = "/pic";
            map.put("preUrl", preUrl);
            json.put("data", map);
        }
        return json.toJSONString();
    }

    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @ResponseBody
    public Object updateCustomer(@RequestBody CustomerRegistDTO customerRegistDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            customerService.registerOrUpdateCustomer(customerRegistDTO);
        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }
        return JSONObject.toJSON(resultMap);
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
    public String getSalePriceLog(@Valid PageParam page, String zid,BindingResult error, String custId, String name, String startTime, String endTime) {
        if (error.hasErrors()) {
            return getErrors(error);
        }
        Map<String, Object> data = new HashMap<>();
        try {
            LoginUser lu = opUser();
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                List<Map<String, Object>> salePriceLog = customerService.getSalePriceLog(page,zid, custId, name, startTime, endTime);
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
}

