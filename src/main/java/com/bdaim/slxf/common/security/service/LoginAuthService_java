package com.bdaim.slxf.common.security.service;

import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.rbac.service.UserInfoService;
import com.bdaim.slxf.dto.LoginUser;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class LoginAuthService implements UserDetailsService {
    Logger logger = Logger.getLogger(LoginAuthService.class);
    @Resource
    UserInfoService userInfoService;
    @Resource
    CustomerService customerService;


    @Override
    public UserDetails loadUserByUsername(String name) throws UsernameNotFoundException {
        List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();
//        UserDO user = userInfoService.getUserByUserName(s);


//
//        try {
//            //已经冻结用户禁止登录
//            if (null != user && 1 == user.getStatus()) {
//                qauths.add("USER_FREEZE");
//            } else if (null != user && 3 == user.getStatus()) {
//                qauths.add("USER_NOT_EXIST");
//            } else {
//                qauths = userInfoService.getAuthsByUserName(s);
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage());
//        }
//        if (null != qauths && qauths.size() > 0) {
//            for (String a : qauths) {
//                auths.add(new SimpleGrantedAuthority(a));
//            }
//        }

        LoginUser userdetail = null;
        if (name.startsWith("customer.")) {
            CustomerUserDO u = customerService.getUserByName(name.substring(9));
            if (u != null) {
                logger.info("登陆框，用户：" + u.getAccount() + "\t状态：" + u.getStatus());
                if (1 == u.getStatus()) {
                    auths.add(new SimpleGrantedAuthority("USER_FREEZE"));
                } else if (3 == u.getStatus()) {
                    auths.add(new SimpleGrantedAuthority("USER_NOT_EXIST"));
                } else if (0 == u.getStatus()) {
                    //user_type: 1=管理员 2=普通员工
                    auths.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                }
                userdetail = new LoginUser(u.getAccount(), u.getPassword(), auths);
                userdetail.setCustId(u.getCust_id());
                userdetail.setId(u.getId());
                userdetail.setUserType(String.valueOf(u.getUserType()));
                userdetail.setRole("ROLE_CUSTOMER");
            }
        } else if (name.startsWith("operator.")) {
            UserDO u = userInfoService.getUserByName(name.substring(9));
            String role = null;
            if (u != null) {
                auths.add(new SimpleGrantedAuthority("ROLE_USER"));
                role = "ROLE_USER";
            }
            if ("admin".equals(u.getName())) {
                auths.add(new SimpleGrantedAuthority("admin"));
                role = "admin";
            }

            userdetail = new LoginUser(u.getName(), u.getPassword(), auths);
            userdetail.setCustId("0");
            userdetail.setId(u.getId());
            userdetail.setUserType(String.valueOf(1));
            userdetail.setRole(role);
            userdetail.setName(u.getName());
        }


        return userdetail;
    }


}
