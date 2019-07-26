package com.bdaim.auth.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.auth.Token;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.auth.service.TokenService;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.rbac.service.UserInfoService;

@Service
public class TokenServiceImpl implements TokenService{
	private static Logger logger = Logger.getLogger(TokenServiceImpl.class);
			
	@Resource
    private CustomerService customerService;
	@Resource
    private UserInfoService userInfoService;
	@Resource
	private TokenCacheService tokenCacheService;
	
	private static Map name2token = new HashMap();
	
	@Override
	public Token createToken(String username, String password) {
		// TODO Auto-generated method stub
		if(username==null || password==null || "".equals(username) || "".equals(password)) {
    		logger.info("username or password is null");
    		return null;
    	}
		
		LoginUser userdetail = null;
		
		
		List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
		
		if(username.startsWith("backend.")) {
			UserDO u = userInfoService.getUserByName(username);
	        if (u != null && CipherUtil.generatePassword(password).equals(u.getPassword())) {
	        	//寻找登录账号已有的token, 需重构
	        	String tokenid = (String)name2token.get(username);
	        	if(tokenid!=null && !"".equals(tokenid)) {
	        		userdetail=(LoginUser)tokenCacheService.getToken(tokenid);
	        		if(userdetail!=null)
	        			return userdetail;
	        		else
	        			name2token.remove(username);
	        	}
	        	
	        	
	            auths.add(new SimpleGrantedAuthority("ROLE_USER"));
	            String role = "ROLE_USER";

	            if ("admin".equals(u.getName())) {
	                auths.add(new SimpleGrantedAuthority("admin"));
	                role = "admin";
	            }

	            userdetail = new LoginUser(u.getId(), u.getName(), CipherUtil.encodeByMD5(u.getId()+""+System.currentTimeMillis()), auths);
	            userdetail.setCustId("0");
	            userdetail.setId(u.getId());
	            userdetail.setUserType(String.valueOf(u.getUserType()));
	            userdetail.setRole(role);
	            userdetail.setName(u.getName());
	        }
		}else {
			CustomerUserDO u = customerService.getUserByName(username);
	    	
	    	if (u != null && CipherUtil.generatePassword(password).equals(u.getPassword())) {
	            logger.info("登陆框，用户：" + u.getAccount() + " 状态：" + u.getStatus());
	            //寻找登录账号已有的token
	        	String tokenid = (String)name2token.get(username);
	        	if(tokenid!=null && !"".equals(tokenid)) {
	        		userdetail=(LoginUser)tokenCacheService.getToken(tokenid);
	        		if(userdetail!=null)
	        			return userdetail;
	        		else
	        			name2token.remove(username);
	        	}
	        	
	            userdetail = new LoginUser(u.getId(), u.getAccount(), CipherUtil.encodeByMD5(u.getId()+""+System.currentTimeMillis()), auths);
	            if (1 == u.getStatus()) {
	                auths.add(new SimpleGrantedAuthority("USER_FREEZE"));
	            } else if (3 == u.getStatus()) {
	                auths.add(new SimpleGrantedAuthority("USER_NOT_EXIST"));
	            } else if (0 == u.getStatus()) {
	                //user_type: 1=管理员 2=普通员工
	                auths.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
	            }
	            
	            userdetail.setCustId(u.getCust_id());
	            userdetail.setId(u.getId());
	            userdetail.setUserType(String.valueOf(u.getUserType()));
	            userdetail.setRole(auths.size()>0?auths.toArray()[0].toString():"");
	        }
		}
		
		name2token.put(username, userdetail.getTokenid());
		return userdetail;
	}

	@Override
	public Token removeToken(String username) {
		// TODO Auto-generated method stub
		return null;
	}
}
