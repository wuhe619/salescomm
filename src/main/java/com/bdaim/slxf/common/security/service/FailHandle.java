package com.bdaim.slxf.common.security.service;

import com.bdaim.slxf.common.security.ResponseResult;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 
 */
public class FailHandle implements AuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException e) throws IOException, ServletException {
        ResponseResult responseResult = new ResponseResult();
        responseResult.setStateCode("400");
        responseResult.setMsg("用户名或者密码错误");
        /*if(e instanceof AuthenticationServiceException) {
            responseResult.setStateCode("404");
            responseResult.setMsg("用户已被冻结");
        }*/
        /*if( exception instanceof ImageCodeException) {
            responseResult.setMsg("image code is error");
            responseResult.setStateCode("003");
        }*/

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().print(converter.getObjectMapper().writeValueAsString(responseResult));
//        request.getRequestDispatcher("/html/login.html").forward(request,response);

    }
}
