package com.bdaim.slxf.common.security.service;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

public class CustomAccessDeniedHandler implements AccessDeniedHandler{

	@Override
	public void handle(HttpServletRequest arg0, HttpServletResponse resp, AccessDeniedException arg2)
			throws IOException, ServletException {
		// TODO Auto-generated method stub
		resp.getWriter().write("{code:401}");
		
	}

}
