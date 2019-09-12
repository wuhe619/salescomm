package com.bdaim.common;

import com.bdaim.common.util.AuthPassport;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.service.CustomerService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.SignatureException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

import static com.bdaim.common.util.JwtUtil.verifyToken;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/11/15 15:32
 */

@Configuration
@Aspect
public class AuthInterceptor implements HandlerInterceptor {

    private static Log logger = LogFactory.getLog(AuthInterceptor.class);
    @Resource
    CustomerService customerService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (handler.getClass().isAssignableFrom(HandlerMethod.class)) {
            AuthPassport authPassport = ((HandlerMethod) handler).getMethodAnnotation(AuthPassport.class);

            //没有声明需要权限,或者声明不验证权限
            if (authPassport == null || authPassport.validate() == false) {
                return true;
            } else {
                String requestURI = request.getRequestURI();
                //if(requestURI.contains("/open/")){
                //从header中得到token
                String authorization = request.getHeader("Authorization");
                if (StringUtil.isEmpty(authorization)) {
                    logger.info("real token:======================is null");
                    String str = "{\n" +
                            "    \"_message\": \"接口调用成功\",\n" +
                            "    \"code\": \"200\",\n" +
                            "    \"data\": {\n" +
                            "        \"code\": \"04\",\n" +
                            "        \"msg\": \"token为空\"\n" +
                            "    }\n" +
                            "}";
                    dealErrorReturn(request, response, str);
                    return false;
                }

                logger.info("authorization = " + authorization);
                request.setAttribute("Authorization",authorization);
                try {
                    Claims claims = verifyToken(authorization);
                    String compId = claims.getId();
                    String issue = claims.getIssuer();
                    request.setAttribute("j_username",issue.substring(9));
                    String subject = claims.getSubject();
                    request.setAttribute("j_password",subject);
                    logger.info("compId:" + compId + "\tissue:" + issue + "\tsubject;" + subject);
                    CustomerUser u = customerService.getUserByName(issue.substring(9));
                    request.setAttribute("customerUserDO",u);
                    return true;
                }catch (ExpiredJwtException e){
                    String str = "{\n" +
                            "    \"_message\": \"接口调用成功\",\n" +
                            "    \"code\": \"200\",\n" +
                            "    \"data\": {\n" +
                            "        \"code\": \"03\",\n" +
                            "        \"msg\": \"token失效\"\n" +
                            "    }\n" +
                            "}";
                    dealErrorReturn(request, response, str);
                    return false;
                }catch (SignatureException e){
                    String str = "{\n" +
                            "    \"_message\": \"接口调用成功\",\n" +
                            "    \"code\": \"200\",\n" +
                            "    \"data\": {\n" +
                            "        \"code\": \"05\",\n" +
                            "        \"msg\": \"token非法\"\n" +
                            "    }\n" +
                            "}";
                    dealErrorReturn(request, response, str);
                    return false;
                }catch (MalformedJwtException e){
                    String str = "{\n" +
                            "    \"_message\": \"接口调用成功\",\n" +
                            "    \"code\": \"200\",\n" +
                            "    \"data\": {\n" +
                            "        \"code\": \"05\",\n" +
                            "        \"msg\": \"token非法\"\n" +
                            "    }\n" +
                            "}";
                    dealErrorReturn(request, response, str);
                    return false;
                }
            }
        } else {
            return true;
        }
    }


    public void dealErrorReturn(HttpServletRequest request, HttpServletResponse response, Object obj) {
        String json = (String) obj;
        PrintWriter writer = null;
        response.setCharacterEncoding("UTF-8");
        response.setContentType("text/html; charset=utf-8");
        try {
            writer = response.getWriter();
            writer.print(json);

        } catch (IOException ex) {
            logger.error("response error", ex);
        } finally {
            if (writer != null)
                writer.close();
        }
    }
}
