package com.bdaim.common.controller.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

@WebFilter(filterName = "logFilter",urlPatterns = "/*")
public class LogFilter implements Filter{
	private static Logger logger = LoggerFactory.getLogger("REST");
	@Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
    	String auth = ((HttpServletRequest)request).getHeader("Authorization");
    	String uri = ((HttpServletRequest)request).getRequestURI();
//    	logger.info("["+auth+"] "+uri);
    	
    	chain.doFilter(request, response);
    }
    

    @Override
    public void destroy() {
    }
}
