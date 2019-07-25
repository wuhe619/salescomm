package com.bdaim.slxf.common.security;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

public class UnauthorizedEntryPoint implements AuthenticationEntryPoint{

	@Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)  {
		try {
//	        if(isAjaxRequest(request)){
//	            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "{code:401}");
				response.setCharacterEncoding("utf-8");
	            response.getWriter().write("{\"code\":401}");
//	        }else{
//	            response.sendRedirect("/html/login.html");
//	        }
		}catch(Exception e) {
			e.printStackTrace();
		}
    }

    public static boolean isAjaxRequest(HttpServletRequest request) {
        String ajaxFlag = request.getHeader("X-Requested-With");
        return ajaxFlag != null && "XMLHttpRequest".equals(ajaxFlag);
    }
}