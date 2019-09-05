package com.bdaim.common.cache;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 */
public class MRPfilter implements Filter {
    private String uri;
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        uri=filterConfig.getInitParameter("notPermissionUri");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request= (HttpServletRequest) servletRequest;
        HttpServletResponse response= (HttpServletResponse) servletResponse;
//        UserManager manager= (UserManager) BeanCache.getBean(ConfigReader.USER_MANAGER);
        if (PermissionCheck.checkRegex(request,request.getRequestURI())||PermissionCheck.cehckURI(request,request.getRequestURI())){
            filterChain.doFilter(request,response);
        }else{
            request.getRequestDispatcher(uri).forward(request,response);
        }
    }

    @Override
    public void destroy() {
    }

}
