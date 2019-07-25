package com.bdaim.slxf.filter;


import java.io.IOException;

import javax.annotation.Resource;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import com.bdaim.rbac.entity.User;
import com.bdaim.slxf.service.LoginService;
import com.bdaim.slxf.service.LoginService;
public class LoginFilter implements Filter {
	@Resource
	private LoginService loginServiceImpl;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {
		HttpServletRequest req = (HttpServletRequest) request;
		HttpServletResponse rsp = (HttpServletResponse) response;
		HttpSession session = req.getSession(true);
		User user = (User) session.getAttribute("user");
		if (!req.getRequestURI().equals(req.getContextPath() + "/user/login.do")
			&&!req.getRequestURI().equals(req.getContextPath() + "/login.html")) {
			if (user == null) {
//				rsp.sendRedirect(req.getContextPath() + "/login.html");
				rsp.getWriter().write("code:401");
				return;
			} else if (loginServiceImpl.login(user.getName(),
					user.getPassword()) == null) {
//				rsp.sendRedirect(req.getContextPath() + "/login.html");
				rsp.getWriter().write("{code:401}");
				return;
			}
		}
		chain.doFilter(request, response);
		return;
	}

	@Override
	public void destroy() {

	}

}
