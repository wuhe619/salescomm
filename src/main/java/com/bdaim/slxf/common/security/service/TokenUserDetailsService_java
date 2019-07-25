package com.bdaim.slxf.common.security.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import com.bdaim.slxf.dto.LoginUser;

public class TokenUserDetailsService implements UserDetailsService{

	private TokenManager tokenManager = new TokenManager();
	
	@Override
    public UserDetails loadUserByUsername(String token)
            throws UsernameNotFoundException {
        if (token.equalsIgnoreCase("")) {
            return new LoginUser("guest", "", new ArrayList<GrantedAuthority>());
        }
        
        UserDetails ud= tokenManager != null ? tokenManager.getUserDetails(token) : null;
        if(ud==null) {
        	ud = new LoginUser("guest", "", new ArrayList<GrantedAuthority>());
        }
        return ud;
    }
    
    public void setTokenManager(TokenManager tm) {
        this.tokenManager = tm;
    }
}
