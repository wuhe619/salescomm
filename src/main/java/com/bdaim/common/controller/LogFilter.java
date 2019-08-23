package com.bdaim.common.controller;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
