package com.bdaim.customer.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.auth.service.impl.TokenServiceImpl;
import com.bdaim.auth.util.ResponseResult;
import com.bdaim.callcenter.dto.CustomCallConfigDTO;
import com.bdaim.callcenter.dto.SeatCallCenterConfig;
import com.bdaim.callcenter.dto.SeatPropertyDTO;
import com.bdaim.callcenter.dto.XFCallCenterConfig;
import com.bdaim.callcenter.service.impl.SeatsService;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.auth.service.TokenService;
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
import com.bdaim.customer.service.CustomerAppService;
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
 */


@Controller
@RequestMapping("/customer")
public class CustomerInfoAction extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(CustomerInfoAction.class);
    @Resource
    CustomerService customerService;
    @Resource
    TokenServiceImpl tokenService;
    @Resource
    CustomerAppService customerAppService;

    /**
     */
    @GetMapping(value = "/info/{customerId}")
    @ResponseBody
    public CustomerDTO info(@PathVariable String customerId) throws Exception{
    	LoginUser lu = opUser();
    	if(lu==null || lu.getAuths()==null || !lu.getAuths().contains("admin"))
    		throw new Exception("no auth");
    	 
    	CustomerDTO data = this.customerService.getCustomerInfoById(customerId);
    		 
        return data;
    }
    
    @GetMapping(value = "/info/{customerId}/app")
    @ResponseBody
    public List app(@PathVariable String customerId) throws Exception{
    	LoginUser lu = opUser();
    	if(lu==null || lu.getAuths()==null || !lu.getAuths().contains("admin"))
    		throw new Exception("no auth");
    		
    	List data = this.customerAppService.apps(customerId);
    	
        return data;
    }


}

