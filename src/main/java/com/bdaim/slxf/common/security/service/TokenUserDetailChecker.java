package com.bdaim.slxf.common.security.service;

import java.util.ArrayList;

import org.springframework.security.authentication.AccountStatusUserDetailsChecker;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bdaim.slxf.dto.LoginUser;

public class TokenUserDetailChecker extends AccountStatusUserDetailsChecker{

	@Override
	public void check(UserDetails user) {
		// TODO Auto-generated method stub
        if(user==null) {
        	user = new LoginUser("guest", "", new ArrayList<GrantedAuthority>());
        }
		
		super.check(user);
	}

}
