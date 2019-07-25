package com.bdaim.slxf.common.security.service;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.StringUtil;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Created by Mr.YinXin on 2017/4/5.
 */
public class MyLoginFilter extends UsernamePasswordAuthenticationFilter{
    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException {

        String username = obtainUsername(request);
        String password = obtainPassword(request);

        if (username == null) {
            username = "";
            if(request.getAttribute("j_username") != null){
                username = request.getAttribute("j_username").toString();
            }
        }

        boolean flag = false;
        if (password == null) {
            password = "";
            if(request.getAttribute("j_password") != null){
                password = request.getAttribute("j_password").toString();
                flag = true;
            }
        }

       /* String sessionCode = (String) request.getSession().getAttribute("imageCode");
        String requestCode = request.getParameter("codeImage");

        if(StringUtils.isBlank(requestCode) || !requestCode.equalsIgnoreCase(sessionCode)){
            throw new ImageCodeException("imageCode error");
        }*/
        String source = request.getParameter("source");
        /*if(StringUtil.isEmpty(source)){
            if(request.getAttribute("source") != null){
                source = request.getAttribute("source").toString();
            }
        }*/
        if("backend".equals(source)) {
        	username = "operator."+username.trim();
        }else {
        	username = "customer."+username.trim();
        }
        
       String newpassword =  CipherUtil.generatePassword(password);

        UsernamePasswordAuthenticationToken authRequest;
        if(flag){
            authRequest = new UsernamePasswordAuthenticationToken(username,password );
        }else{
            authRequest = new UsernamePasswordAuthenticationToken(username,newpassword );
        }

        setDetails(request, authRequest);

        return this.getAuthenticationManager().authenticate(authRequest);
    }

}
