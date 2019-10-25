package com.bdaim.auth.service.impl;

import com.alibaba.fastjson.JSON;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.auth.Token;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.auth.service.TokenService;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dao.CustomerUserPropertyDao;
import com.bdaim.customer.dto.*;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customer.user.service.CustomerUserService;
import com.bdaim.rbac.dao.RoleDao;
import com.bdaim.rbac.dto.ResourceDTO;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.rbac.service.ResourceService;
import com.bdaim.rbac.service.impl.UserInfoService;
import com.bdaim.util.CipherUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.bdaim.util.wechat.WeChatUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class TokenServiceImpl implements TokenService {
    private static Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Resource
    private CustomerService customerService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private TokenCacheService tokenCacheService;
    @Resource
    private RoleDao roleDao;
    @Resource
    private ResourceService resourceService;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomerUserService customerUserService;
    @Resource
    private WeChatUtil weChatUtil;
    @Resource
    private CustomerUserPropertyDao customerUserPropertyDao;

    private static Map name2token = new HashMap();

    public static Map listTokens() {
        return name2token;
    }

    @Override
    public Token createToken(String username, String password) {
        if (username == null || password == null || "".equals(username) || "".equals(password)) {
            logger.warn("username or password is null");
            return new LoginUser("guest", "", new ArrayList<>(), "用户名密码不能为空", "402");
        }

        LoginUser userdetail = null;


        List<GrantedAuthority> auths = new ArrayList<GrantedAuthority>();

        if (username.startsWith("backend.")) {
            long type = 0;
            UserDO u = userInfoService.getUserByName(username.substring(8));
            if (u != null && CipherUtil.generatePassword(password).equals(u.getPassword())) {
                List<Map<String, Object>> roleInfo = roleDao.getRoleInfoByUserId(String.valueOf(u.getId()));
                if (roleInfo != null && roleInfo.size() > 0) {
                    type = NumberConvertUtil.parseLong(String.valueOf(roleInfo.get(0).get("type")));
                }
                //寻找登录账号已有的token, 需重构
                String tokenid = (String) name2token.get(username);
                if (tokenid != null && !"".equals(tokenid)) {
                    userdetail = (LoginUser) tokenCacheService.getToken(tokenid);
                    if (userdetail != null) {
                        userdetail.setType(type);
                        return userdetail;
                    } else
                        name2token.remove(username);
                }


                auths.add(new SimpleGrantedAuthority("ROLE_USER"));
                String role = "ROLE_USER";

                if ("admin".equals(u.getName())) {
                    auths.add(new SimpleGrantedAuthority("admin"));
                    role = "admin";
                }

                userdetail = new LoginUser(u.getId(), u.getName(), CipherUtil.encodeByMD5(u.getId() + "" + System.currentTimeMillis()), auths);
                userdetail.setCustId("0");
                userdetail.setId(u.getId());
                userdetail.setUserType(String.valueOf(u.getUserType()));
                userdetail.setRole(role);
                userdetail.setType(type);
                userdetail.setName(u.getName());

                String defaultUrl = "";
                if ("admin".equals(u.getName())) {
                    auths.add(new SimpleGrantedAuthority("admin"));
                    role = "admin";
                    defaultUrl = "/backend/customerGroupManagement/customerGroup.html";
                } else {
                    auths.add(new SimpleGrantedAuthority("ROLE_USER"));
                    // 查询用户关联的所有资源
                    List<ResourceDTO> list = resourceService.queryResource(u.getId(), 0L, 1, false);
                    for (int i = 0; i < list.size(); i++) {
                        // 处理登录成功后的默认页
                        if (StringUtil.isNotEmpty(list.get(i).getUri())) {
                            defaultUrl = list.get(i).getUri();
                            break;
                        }
                    }
                }

                userdetail.setStateCode("200");
                userdetail.setMsg("SUCCESS");
                userdetail.setAuth(userdetail.getAuthorities().toArray()[0].toString());
                userdetail.setUserName(userdetail.getUsername());
                userdetail.setCustId(userdetail.getCustId());
                userdetail.setUserType(userdetail.getUserType());
                userdetail.setUser_id(userdetail.getId().toString());
                userdetail.setTokenid(userdetail.getTokenid());
                userdetail.setDefaultUrl(defaultUrl);
                userdetail.setStatus(u.getStatus().toString());
            } else {
                logger.warn("username or password is error");
                return new LoginUser("guest", "", new ArrayList<>(), "用户名密码错误", "401");
            }
        } else if (username.startsWith("wx.")) {
            // 用户名+密码+微信code 绑定+登录,username前3位固定为wx. password使用.拆分,前半部分为微信code,后半部分为实际用户密码
            CustomerUser u = customerService.getUserByName(username.substring(3));
            String userPwd = password.substring(password.indexOf(".") + 1);
            String code = password.substring(0, password.indexOf("."));
            String md5Password = CipherUtil.generatePassword(userPwd);
            if (u != null && md5Password.equals(u.getPassword())) {
                logger.info("微信绑定用户:" + u.getAccount() + "状态:" + u.getStatus());
                // 绑定微信
                boolean bindStatus = customerUserService.saveBindWx(String.valueOf(u.getId()), code);
                if (!bindStatus) {
                    return new LoginUser("guest", "", new ArrayList<>(), "绑定失败", "401");
                }
                // 组装用户数据(分组等信息)
                userdetail = getUserData(u, username, auths);
            } else {
                logger.warn("username or password is error");
                return new LoginUser("guest", "", new ArrayList<>(), "用户名密码错误", "401");
            }
        } else if ("wx".equals(username)) {
            // 微信code登录,username固定为wx,password为微信的code
            String openId = weChatUtil.getWeChatOpenId(password);
            logger.info("微信code登录openId:{},code:{}", openId, password);
            //openId = "2f39c1632fff7219a508b4ef9d14f870";
            if (StringUtil.isEmpty(openId)) {
                return new LoginUser("guest", "", new ArrayList<>(), "未查询到绑定用户", "401");
            }
            CustomerUserPropertyDO userProper = customerUserPropertyDao.getPropertyByName("openid", openId);
            logger.info("微信code登录openId:{},code:{},用户属性数据:{}", openId, password, JSON.toJSONString(userProper));
            if (userProper == null) {
                return new LoginUser("guest", "", new ArrayList<>(), "未查询到绑定用户", "401");
            }
            CustomerUser u = customerService.getUserByName(customerUserDao.getLoginName(userProper.getUserId()));
            if (u != null) {
                // 组装用户数据(分组等信息)
                username = u.getAccount();
                userdetail = getUserData(u, username, auths);
            } else {
                logger.warn("username or password is error");
                return new LoginUser("guest", "", new ArrayList<>(), "用户名密码错误", "401");
            }
        } else {
            CustomerUser u = customerService.getUserByName(username);
            String md5Password = CipherUtil.generatePassword(password);
            if (u != null && md5Password.equals(u.getPassword())) {
                logger.info("登陆用户:" + u.getAccount() + " 状态:" + u.getStatus());
                //寻找登录账号已有的token
                String tokenid = (String) name2token.get(username);
                if (tokenid != null && !"".equals(tokenid)) {
                    userdetail = (LoginUser) tokenCacheService.getToken(tokenid);
                    if (userdetail != null) {
                        //前台用户权限信息
                        CustomerUserPropertyDO userProperty = customerUserDao.getProperty(String.valueOf(u.getId()), CustomerUserPropertyEnum.RESOURCE_MENU.getKey());
                        if (userProperty != null && StringUtil.isNotEmpty(userProperty.getPropertyValue())) {
                            userdetail.setResourceMenu(userProperty.getPropertyValue());
                            userdetail.setStatus(u.getStatus().toString());
                            return userdetail;
                        }
                    } else
                        name2token.remove(username);
                }

                //userdetail = new LoginUser(u.getId(), u.getAccount(), CipherUtil.encodeByMD5(u.getId()+""+System.currentTimeMillis()), auths);
                if (1 == u.getStatus()) {
                    auths.add(new SimpleGrantedAuthority("USER_FREEZE"));
                } else if (3 == u.getStatus()) {
                    auths.add(new SimpleGrantedAuthority("USER_NOT_EXIST"));
                } else if (0 == u.getStatus()) {
                    //user_type: 1=管理员 2=普通员工
                    auths.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
                }
                userdetail = new LoginUser(u.getId(), u.getAccount(), CipherUtil.encodeByMD5(u.getId() + "" + System.currentTimeMillis()), auths);
                userdetail.setCustId(u.getCust_id());
                userdetail.setId(u.getId());
                userdetail.setUserType(String.valueOf(u.getUserType()));
                userdetail.setRole(auths.size() > 0 ? auths.toArray()[0].toString() : "");

                userdetail.setStatus(u.getStatus().toString());
                userdetail.setStateCode("200");
                userdetail.setMsg("SUCCESS");
                userdetail.setAuth(userdetail.getAuthorities().toArray()[0].toString());
                userdetail.setUserName(userdetail.getUsername());
                userdetail.setUser_id(userdetail.getId().toString());
                // 处理服务权限
                userdetail.setServiceMode(ServiceModeEnum.MARKET_TASK.getCode());
                CustomerPropertyDTO cpd = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.SERVICE_MODE.getKey());
                if (cpd != null && StringUtil.isNotEmpty(cpd.getPropertyValue())) {
                    userdetail.setServiceMode(cpd.getPropertyValue());
                }
                CustomerPropertyDTO industry = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.INTEN_INDUCTRY.getKey());
                if (industry != null && StringUtil.isNotEmpty(industry.getPropertyValue())) {
                    userdetail.setInten_industry(industry.getPropertyValue());
                }
                CustomerPropertyDTO apiToken = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.API_TOKEN.getKey());
                if (apiToken != null && StringUtil.isNotEmpty(apiToken.getPropertyValue())) {
                    userdetail.setApi_token(apiToken.getPropertyValue());
                }
                //前台用户权限信息
                CustomerUserPropertyDO userProperty = customerUserDao.getProperty(String.valueOf(u.getId()), CustomerUserPropertyEnum.RESOURCE_MENU.getKey());
                if (userProperty != null && StringUtil.isNotEmpty(userProperty.getPropertyValue())) {
                    userdetail.setResourceMenu(userProperty.getPropertyValue());
                }
                CustomerUserPropertyDO mobile_num = customerUserDao.getProperty(u.getId().toString(), "mobile_num");
                if (mobile_num != null && StringUtil.isNotEmpty(mobile_num.getPropertyValue())) {
                    userdetail.setMobile_num(mobile_num.getPropertyValue());
                } else {
                    userdetail.setMobile_num("");
                }
                // 查询用户组信息
                CustomerUserGroupRelDTO cug = customerUserDao.getCustomerUserGroupByUserId(u.getId());
                userdetail.setUserGroupId("");
                userdetail.setUserGroupRole("");
                userdetail.setJobMarketId("");
                if (cug != null) {
                    userdetail.setUserGroupId(cug.getGroupId());
                    userdetail.setUserGroupRole(String.valueOf(cug.getType()));
                    userdetail.setJobMarketId(cug.getJobMarketId());
                }

            } else {
                logger.warn("username or password is error");
                return new LoginUser("guest", "", new ArrayList<>(), "用户名密码错误", "401");
            }
        }

        if (userdetail != null)
            name2token.put(username, userdetail.getTokenid());
        return userdetail;
    }

    @Override
    public Token removeToken(String username) {
        // 移除缓存token
        name2token.remove(username);
        // 移除token
        LoginUser token = (LoginUser) tokenCacheService.getToken(opUser().getTokenid());

        return token;
    }

    public LoginUser opUser() {
        Token u = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (u instanceof LoginUser)
            return (LoginUser) u;
        else
            return new LoginUser(0L, "", "", null);
    }

    /**
     * 组装用户数据
     *
     * @param u
     * @param username
     * @param auths
     * @return
     */
    public LoginUser getUserData(CustomerUser u, String username, List<GrantedAuthority> auths) {
        // 寻找登录账号已有的token
        LoginUser userdetail = null;
        String tokenId = (String) name2token.get(username);
        // 读取token缓存
        if (StringUtil.isNotEmpty(tokenId) && tokenCacheService.getToken(tokenId) == null) {
            name2token.remove(username);
            tokenId = null;
        }
        if (StringUtil.isEmpty(tokenId)) {
            tokenId = CipherUtil.encodeByMD5(u.getId() + "" + System.currentTimeMillis());
        }
        if (1 == u.getStatus()) {
            auths.add(new SimpleGrantedAuthority("USER_FREEZE"));
        } else if (3 == u.getStatus()) {
            auths.add(new SimpleGrantedAuthority("USER_NOT_EXIST"));
        } else if (0 == u.getStatus()) {
            //user_type: 1=管理员 2=普通员工
            auths.add(new SimpleGrantedAuthority("ROLE_CUSTOMER"));
        }
        userdetail = new LoginUser(u.getId(), u.getAccount(), tokenId, auths);
        userdetail.setCustId(u.getCust_id());
        userdetail.setId(u.getId());
        userdetail.setUserType(String.valueOf(u.getUserType()));
        userdetail.setRole(auths.size() > 0 ? auths.toArray()[0].toString() : "");

        userdetail.setStatus(u.getStatus().toString());
        userdetail.setStateCode("200");
        userdetail.setMsg("SUCCESS");
        userdetail.setAuth(userdetail.getAuthorities().toArray()[0].toString());
        userdetail.setUserName(userdetail.getUsername());
        userdetail.setUser_id(userdetail.getId().toString());
        // 处理服务权限
        userdetail.setServiceMode(ServiceModeEnum.MARKET_TASK.getCode());
        CustomerPropertyDTO cpd = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.SERVICE_MODE.getKey());
        if (cpd != null && StringUtil.isNotEmpty(cpd.getPropertyValue())) {
            userdetail.setServiceMode(cpd.getPropertyValue());
        }
        CustomerPropertyDTO industry = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.INTEN_INDUCTRY.getKey());
        if (industry != null && StringUtil.isNotEmpty(industry.getPropertyValue())) {
            userdetail.setInten_industry(industry.getPropertyValue());
        }
        CustomerPropertyDTO apiToken = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.API_TOKEN.getKey());
        if (apiToken != null && StringUtil.isNotEmpty(apiToken.getPropertyValue())) {
            userdetail.setApi_token(apiToken.getPropertyValue());
        }
        //前台用户权限信息
        CustomerUserPropertyDO userProperty = customerUserDao.getProperty(String.valueOf(u.getId()), CustomerUserPropertyEnum.RESOURCE_MENU.getKey());
        if (userProperty != null && StringUtil.isNotEmpty(userProperty.getPropertyValue())) {
            userdetail.setResourceMenu(userProperty.getPropertyValue());
        }
        CustomerUserPropertyDO mobile_num = customerUserDao.getProperty(u.getId().toString(), "mobile_num");
        if (mobile_num != null && StringUtil.isNotEmpty(mobile_num.getPropertyValue())) {
            userdetail.setMobile_num(mobile_num.getPropertyValue());
        } else {
            userdetail.setMobile_num("");
        }
        // 查询用户组信息
        CustomerUserGroupRelDTO cug = customerUserDao.getCustomerUserGroupByUserId(u.getId());
        userdetail.setUserGroupId("");
        userdetail.setUserGroupRole("");
        userdetail.setJobMarketId("");
        if (cug != null) {
            userdetail.setUserGroupId(cug.getGroupId());
            userdetail.setUserGroupRole(String.valueOf(cug.getType()));
            userdetail.setJobMarketId(cug.getJobMarketId());
        }
        return userdetail;
    }
}
