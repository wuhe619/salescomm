package com.bdaim.slxf.common.security.service;


import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.alibaba.fastjson.JSONObject;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import com.bdaim.slxf.common.security.ResponseResult;
import com.bdaim.slxf.dto.LoginUser;

/**
 * 
 */
public class SuccessHandle implements AuthenticationSuccessHandler {
    private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
    	LoginUser loginUser = (LoginUser)authentication.getPrincipal();
        ResponseResult responseResult = new ResponseResult();
        responseResult.setStateCode("200");
        responseResult.setMsg("SUCCESS");
        responseResult.setAuth(loginUser.getAuthorities().toArray()[0].toString());
        responseResult.setUserName(loginUser.getUsername());
        responseResult.setCustId(loginUser.getCustId());
        responseResult.setUserType(Integer.valueOf(loginUser.getUserType()));
        responseResult.setUser_id(loginUser.getId().toString());
        
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(converter.getObjectMapper().writeValueAsString(responseResult));
        response.getWriter().close();
    }
}
