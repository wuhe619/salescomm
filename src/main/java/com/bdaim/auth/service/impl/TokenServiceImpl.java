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
import com.bdaim.customer.service.CustomerAppService;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customer.user.service.CustomerUserService;
import com.bdaim.rbac.dao.RoleDao;
import com.bdaim.rbac.dto.ResourceDTO;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.rbac.service.ResourceService;
import com.bdaim.rbac.service.impl.UserInfoService;
import com.bdaim.util.*;
import com.bdaim.util.wechat.WeChatUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import sun.misc.BASE64Decoder;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class TokenServiceImpl implements TokenService {
    private static Logger logger = LoggerFactory.getLogger(TokenServiceImpl.class);

    @Resource
    private CustomerService customerService;
    @Resource
    private UserInfoService userInfoService;
    @Resource
    private TokenCacheService<LoginUser> tokenCacheService;
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

    @Resource
    private CustomerAppService customerAppService;

    private static Map name2token = new HashMap();

    public static Map listTokens() {
        return name2token;
    }

    @Override
    public Token createToken(String username, String password) {
        if (username == null || password == null || "".equals(username) || "".equals(password)) {
            logger.warn("username or password is null");
            return new LoginUser("guest", "", "用户名密码不能为空", "402");
        }

        LoginUser userdetail = null;
        BASE64Decoder decoder = new BASE64Decoder();

        if (username.startsWith("backend.")) {
            //校验 验证码
            String[] usernameArray = username.split("\\.");
            String uuid = usernameArray[1].substring(4);
            Object object = VerifyUtil.verifyCodes.get(uuid);
            if (object != null) {
                VerifyCode code = (VerifyCode) object;
                //查看验证码是否过期
                long now = System.currentTimeMillis();
                long codeTime = code.getVerifyTime();
                if (now - codeTime > VerifyUtil.verifyCodeTimeout) {
                    return new LoginUser("guest", "", "验证码已过期", "402");
                }
                //没过期，查看验证码是否正确
                String verifyCode = usernameArray[1].substring(0, 4);
                if (!verifyCode.equalsIgnoreCase(code.getVerifyCode())) {
                    return new LoginUser("guest", "", "验证码不正确", "402");
                }
                //验证码没问题，则删除
                VerifyUtil.verifyCodes.remove(uuid);
            }
            try {
                password = new String(decoder.decodeBuffer(password));
            } catch (IOException e) {
                e.printStackTrace();
            }
            long type = 0;
            String userNameWithVerify = username.substring(8);
            String userNameWithoutVerify = userNameWithVerify.substring(userNameWithVerify.lastIndexOf(".") + 1);
            UserDO u = userInfoService.getUserByName(userNameWithoutVerify);
            if (u == null || u.getStatus() != 0) {
                return new LoginUser("guest", "", "用户名密码错误", "401");
            }
            if (u != null && CipherUtil.generatePassword(password).equals(u.getPassword())) {
                List<Map<String, Object>> roleInfo = roleDao.getRoleInfoByUserId(String.valueOf(u.getId()));
                if (roleInfo != null && roleInfo.size() > 0 && roleInfo.get(0).get("type") != null) {
                    type = NumberConvertUtil.parseLong(String.valueOf(roleInfo.get(0).get("type")));
                }
                //寻找登录账号已有的token, 需重构
                String tokenid = (String) name2token.get(username);
                if (tokenid != null && !"".equals(tokenid)) {
                    try {
                        userdetail = tokenCacheService.getToken(tokenid, LoginUser.class);
                    } catch (Exception e) {
                    }
                    if (userdetail != null) {
                        userdetail.setType(type);
                        return userdetail;
                    } else {
                        name2token.remove(username);
                    }
                }


                userdetail = new LoginUser(u.getId(), u.getName(), CipherUtil.encodeByMD5(u.getId() + "" + System.currentTimeMillis()));
                userdetail.addAuth("ROLE_USER");
                String role = "ROLE_USER";

                if ("admin".equals(u.getName())) {
                    userdetail.addAuth("admin");
                    role = "admin";
                }
                userdetail.setCustId("0");
                userdetail.setId(u.getId());
                userdetail.setUserType(String.valueOf(u.getUserType()));
                userdetail.setRole(role);
                userdetail.setType(type);
                userdetail.setName(u.getName());
                userdetail.setAuthorize(StringUtil.isNotEmpty(u.getAuthorize()) ? u.getAuthorize() : "");

                String defaultUrl = "";
                if ("admin".equals(u.getName())) {
                    defaultUrl = "/backend/customerGroupManagement/customerGroup.html";
                } else {
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
                userdetail.setUserName(userdetail.getUsername());
                userdetail.setCustId(userdetail.getCustId());
                userdetail.setUserType(userdetail.getUserType());
                userdetail.setUser_id(userdetail.getId().toString());
                userdetail.setTokenid(userdetail.getTokenid());
                userdetail.setDefaultUrl(defaultUrl);
                userdetail.setStatus(u.getStatus().toString());
            } else {
                logger.warn("username or password is error");
                return new LoginUser("guest", "", "用户名密码错误", "401");
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
                    return new LoginUser("guest", "", "绑定失败", "401");
                }
                // 组装用户数据(分组等信息)
                userdetail = getUserData(u, username);
            } else {
                logger.warn("username or password is error");
                return new LoginUser("guest", "", "用户名密码错误", "401");
            }
        } else if ("wx".equals(username)) {
            // 微信code登录,username固定为wx,password为微信的code
            String openId = weChatUtil.getWeChatOpenId(password);
            logger.info("微信code登录openId:{},code:{}", openId, password);
            //openId = "2f39c1632fff7219a508b4ef9d14f870";
            if (StringUtil.isEmpty(openId)) {
                return new LoginUser("guest", "", "未查询到绑定用户", "401");
            }
            CustomerUserPropertyDO userProper = customerUserPropertyDao.getPropertyByName("openid", openId);
            logger.info("微信code登录openId:{},code:{},用户属性数据:{}", openId, password, JSON.toJSONString(userProper));
            if (userProper == null) {
                return new LoginUser("guest", "", "未查询到绑定用户", "401");
            }
            CustomerUser u = customerService.getUserByName(customerUserDao.getLoginName(userProper.getUserId()));
            if (u != null) {
                // 组装用户数据(分组等信息)
                username = u.getAccount();
                userdetail = getUserData(u, username);
            } else {
                logger.warn("username or password is error");
                return new LoginUser("guest", "", "用户名密码错误", "401");
            }
        } else {
            //校验 验证码
            String[] usernameArray = username.split("\\.");
            String uuid = usernameArray[0].substring(4);
            Object object = VerifyUtil.verifyCodes.get(uuid);
            if (object != null) {
                VerifyCode code = (VerifyCode) object;
                //查看验证码是否过期
                long now = System.currentTimeMillis();
                long codeTime = code.getVerifyTime();
                if (now - codeTime > VerifyUtil.verifyCodeTimeout) {
                    return new LoginUser("guest", "", "验证码已过期", "402");
                }
                //没过期，查看验证码是否正确
                String verifyCode = usernameArray[0].substring(0, 4);
                if (!verifyCode.equalsIgnoreCase(code.getVerifyCode())) {
                    return new LoginUser("guest", "", "验证码不正确", "402");
                }
                //验证码没问题，则删除
                VerifyUtil.verifyCodes.remove(uuid);
            }
            try {
                password = new String(decoder.decodeBuffer(password));
            } catch (IOException e) {
                e.printStackTrace();
            }
            String userNameWithoutVerify = username.substring(username.lastIndexOf(".") + 1);
            CustomerUser u = customerService.getUserByName(userNameWithoutVerify);
            String md5Password = CipherUtil.generatePassword(password);
            if (u != null && md5Password.equals(u.getPassword())) {
                logger.info("登陆用户:" + u.getAccount() + " 状态:" + u.getStatus());
                //寻找登录账号已有的token
                String tokenid = (String) name2token.get(username);
                if (tokenid != null && !"".equals(tokenid)) {
                    userdetail = tokenCacheService.getToken(tokenid, LoginUser.class);
                    if (userdetail != null) {
                        //前台用户权限信息
                        CustomerUserPropertyDO userProperty = customerUserDao.getProperty(String.valueOf(u.getId()), CustomerUserPropertyEnum.RESOURCE_MENU.getKey());
                        if (userProperty != null && StringUtil.isNotEmpty(userProperty.getPropertyValue())) {
                            userdetail.setResourceMenu(userProperty.getPropertyValue());
                            userdetail.setStatus(u.getStatus().toString());
                            return userdetail;
                        }
                    } else {
                        name2token.remove(username);
                    }
                }

                //userdetail = new LoginUser(u.getId(), u.getAccount(), CipherUtil.encodeByMD5(u.getId()+""+System.currentTimeMillis()), auths);
                userdetail = new LoginUser(u.getId(), u.getAccount(), CipherUtil.encodeByMD5(u.getId() + "" + System.currentTimeMillis()));
                if (1 == u.getStatus()) {
                    userdetail.addAuth("USER_FREEZE");
                } else if (3 == u.getStatus()) {
                    userdetail.addAuth("USER_NOT_EXIST");
                } else if (0 == u.getStatus()) {
                    //user_type: 1=管理员 2=普通员工
                    userdetail.addAuth("ROLE_CUSTOMER");
                }
                userdetail.setCustId(u.getCust_id());
                userdetail.setId(u.getId());
                userdetail.setUserType(String.valueOf(u.getUserType()));
                userdetail.setRole(userdetail.getAuths().size() > 0 ? userdetail.getAuths().get(0) : "");

                userdetail.setStatus(u.getStatus().toString());
                userdetail.setStateCode("200");
                userdetail.setMsg("SUCCESS");
                userdetail.setAuth(userdetail.getAuths().size() > 0 ? userdetail.getAuths().get(0) : "");
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

               /* List apps = customerAppService.apps(u.getCust_id());
                if(apps==null || apps.size()==0){
                    userdetail.setApi_token(null);
                }else{
                    for(int i=0;i<apps.size();i++){
                        Map<String,Object> map = (Map<String, Object>) apps.get(i);
                        Object access_token =  map.get("access_token");
                        if(access_token!=null){
                            userdetail.setApi_token(access_token.toString());
                        }
                    }
                }*/
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
                // 处理客户营销类型
                userdetail.setMarketingType(MarketTypeEnum.B2C.getCode());
                CustomerPropertyDTO marketingType = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.MARKET_TYPE.getKey());
                if (marketingType != null && StringUtil.isNotEmpty(marketingType.getPropertyValue())) {
                    userdetail.setMarketingType(NumberConvertUtil.parseInt(marketingType.getPropertyValue()));
                }

            } else {
                logger.warn("username or password is error");
                return new LoginUser("guest", "", "用户名密码错误", "401");
            }
        }

        if (userdetail != null) {
            name2token.put(username, userdetail.getTokenid());
        }
        return userdetail;
    }

    @Override
    public Token removeToken(String username) {
        // 移除缓存token
        name2token.remove(username);
        // 移除token
        LoginUser token = tokenCacheService.getToken(opUser().getTokenid(), LoginUser.class);

        return token;
    }

    public LoginUser opUser() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getRequest();
        String authorization = request.getHeader("Authorization");

        if (authorization != null && !"".equals(authorization)) {
            LoginUser u = tokenCacheService.getToken(authorization, LoginUser.class);
            if (u != null) {
                return u;
            }
        }
        return new LoginUser(0L, "", "");
    }

    /**
     * 组装用户数据
     *
     * @param u
     * @param username
     * @param
     * @return
     */
    public LoginUser getUserData(CustomerUser u, String username) {
        // 寻找登录账号已有的token
        LoginUser userdetail = null;
        String tokenId = (String) name2token.get(username);
        // 读取token缓存
        if (StringUtil.isNotEmpty(tokenId) && tokenCacheService.getToken(tokenId, LoginUser.class) == null) {
            name2token.remove(username);
            tokenId = null;
        }
        if (StringUtil.isEmpty(tokenId)) {
            tokenId = CipherUtil.encodeByMD5(u.getId() + "" + System.currentTimeMillis());
        }

        userdetail = new LoginUser(u.getId(), u.getAccount(), tokenId);
        if (1 == u.getStatus()) {
            userdetail.addAuth("USER_FREEZE");
        } else if (3 == u.getStatus()) {
            userdetail.addAuth("USER_NOT_EXIST");
        } else if (0 == u.getStatus()) {
            //user_type: 1=管理员 2=普通员工
            userdetail.addAuth("ROLE_CUSTOMER");
        }

        userdetail.setCustId(u.getCust_id());
        userdetail.setId(u.getId());
        userdetail.setUserType(String.valueOf(u.getUserType()));
        userdetail.setRole(userdetail.getAuths().size() > 0 ? userdetail.getAuths().get(0) : "");

        userdetail.setStatus(u.getStatus().toString());
        userdetail.setStateCode("200");
        userdetail.setMsg("SUCCESS");
        userdetail.setAuth(userdetail.getAuths().size() > 0 ? userdetail.getAuths().get(0) : "");
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
        // 处理客户营销类型
        userdetail.setMarketingType(MarketTypeEnum.B2C.getCode());
        CustomerPropertyDTO marketingType = customerService.getCustomerProperty(u.getCust_id(), CustomerPropertyEnum.MARKET_TYPE.getKey());
        if (marketingType != null && StringUtil.isNotEmpty(marketingType.getPropertyValue())) {
            userdetail.setMarketingType(NumberConvertUtil.parseInt(marketingType.getPropertyValue()));
        }
        return userdetail;
    }
}
