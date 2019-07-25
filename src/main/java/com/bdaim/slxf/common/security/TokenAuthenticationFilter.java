package com.bdaim.slxf.common.security;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.web.WebAttributes;
import org.springframework.security.web.authentication.preauth.AbstractPreAuthenticatedProcessingFilter;
import org.springframework.util.Assert;

public class TokenAuthenticationFilter extends AbstractPreAuthenticatedProcessingFilter{


	private String principalRequestHeader = "Authorization";
    private String credentialsRequestHeader;
    private boolean exceptionIfHeaderMissing = true;
    
    @Override
    protected Object getPreAuthenticatedCredentials(HttpServletRequest request) {
    	System.out.println( ((HttpServletRequest)request).getHeader("Authorization") + "....1" );
    	/*获取principal信息*/
        String tokenid = request.getHeader("Authorization");

        if (tokenid == null && exceptionIfHeaderMissing) {
            // 对于request进行BadException处理
            request.setAttribute(WebAttributes.AUTHENTICATION_EXCEPTION, new BadCredentialsException("No token found in request."));
           
            return "";
        }
        if(tokenid==null)
        	tokenid="";

        return tokenid;
    }

    @Override
    protected Object getPreAuthenticatedPrincipal(HttpServletRequest request) {
    	System.out.println( ((HttpServletRequest)request).getHeader("Authorization") + "....2" );
    	
    	String tokenid = request.getHeader("Authorization");
    	if(tokenid==null)
    		tokenid="";
    		
        return tokenid;    
    }

    public void setPrincipalRequestHeader(String principalRequestHeader) {
        Assert.hasText(principalRequestHeader,
                "principalRequestHeader must not be empty or null");
        this.principalRequestHeader = principalRequestHeader;
    }

    public void setCredentialsRequestHeader(String credentialsRequestHeader) {
        Assert.hasText(credentialsRequestHeader,
                "credentialsRequestHeader must not be empty or null");
        this.credentialsRequestHeader = credentialsRequestHeader;
    }

    /**
     * Defines whether an exception should be raised if the principal header is missing.
     * Defaults to {@code true}.
     *
     * @param exceptionIfHeaderMissing set to {@code false} to override the default
     * behaviour and allow the request to proceed if no header is found.
     */
    public void setExceptionIfHeaderMissing(boolean exceptionIfHeaderMissing) {
        this.exceptionIfHeaderMissing = exceptionIfHeaderMissing;
    }
    
}
