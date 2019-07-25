package com.bdaim.slxf.common.security.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import com.bdaim.common.util.CipherUtil;
import com.bdaim.slxf.dto.LoginUser;

public class TokenManager {

	private static Map tokens = new HashMap();
	private static Map id2name = new HashMap();
	private static Long tokenTimeout = 7200000L;
	/**
     * Creates a new token for the user and returns its {@link TokenInfo}.
     * It may add it to the token list or replace the previous one for the user. Never returns {@code null}.
     */
	public static String createNewToken(LoginUser userDetails) {
    	try {
	    	LoginUser lu = (LoginUser)tokens.get(String.valueOf(userDetails.getId()));	
	    	if(lu!=null) {
	    		if(System.currentTimeMillis() - lu.getTokentime() < tokenTimeout) {
	    			userDetails.setTokenid(lu.getTokenid());  //token复用
	    		}else {
	    			id2name.remove(lu.getTokenid());
	    			userDetails.setTokenid(CipherUtil.encodeByMD5(userDetails.getId()+""+System.currentTimeMillis()));  //new token
	    		}
	    		
	    		userDetails.setTokentime(System.currentTimeMillis());
	    		tokens.put(String.valueOf(userDetails.getId()), userDetails);
	    	}else {
	    		userDetails.setTokenid(CipherUtil.encodeByMD5(userDetails.getId()+""+System.currentTimeMillis()));  //new token
	    		userDetails.setTokentime(System.currentTimeMillis());
	    	}
	
			tokens.put(String.valueOf(userDetails.getId()), userDetails);
			id2name.put(userDetails.getTokenid(), String.valueOf(userDetails.getId()));
    	}catch(Exception e) {
    		e.printStackTrace();
    		id2name.remove(userDetails.getTokenid());
    		userDetails.setTokenid("");
    	}
    	return userDetails.getTokenid();
    }

    /** Removes all tokens for user. */
    //void removeUserDetails(UserDetails userDetails);

    /** Removes a single token. */
    public static UserDetails removeToken(String token) {
    	return null;
    }

    /** Returns user details for a token. */
    public static UserDetails getUserDetails(String token) {
    	String userid = (String)id2name.get(token);
    	if(userid==null)
    		return null;
    	LoginUser ud = (LoginUser)tokens.get(userid);
    	if(ud==null) {
    		id2name.remove(token);
    		return null;
    	}
    	if(System.currentTimeMillis() - ud.getTokentime() >= tokenTimeout) {
    		ud=null;
    		id2name.remove(token);
    		tokens.remove(userid);
    	}else {
    		ud.setTokentime(System.currentTimeMillis());
    	}
    	return ud;
    }
    
    /** Returns user details for a username. */
    public static UserDetails getUserDetailsByUsername(String username) {
    	return null;
    }

    /** Returns a collection with token information for a particular user. */
    public static Collection<String> getUserTokens(UserDetails userDetails){
    	return null;
    }
    
    
    public static Boolean validateToken(String token) {
    	return true;
    }
}
