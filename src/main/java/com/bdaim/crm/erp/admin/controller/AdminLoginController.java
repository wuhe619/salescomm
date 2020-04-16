package com.bdaim.crm.erp.admin.controller;

import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSON;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.auth.Token;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.auth.service.TokenService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.common.config.redis.Redis;
import com.bdaim.crm.common.config.redis.RedisManager;
import com.bdaim.crm.common.constant.BaseConstant;
import com.bdaim.crm.erp.admin.entity.AdminUser;
import com.bdaim.crm.erp.admin.service.LkAdminRoleService;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.aop.Clear;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.*;

/**
 * 用户登录
 */
@RequestMapping
@RestController
public class AdminLoginController extends BasicAction {

    @Resource
    private LkAdminRoleService adminRoleService;
    @Resource
    private TokenService tokenService;
    @Resource
    private TokenCacheService tokenCacheService;

    //public final static Prop prop = PropKit.use("config/crm9-config.txt");

    public void index() {
        //redirect("/index.html");
    }

    @PostMapping(value = "/crm/login")
    public Token login(@RequestBody Map<String, String> params) throws Exception {
        Token token = adminRoleService.createTokenByPhone(params);
        if (token != null) {
            tokenCacheService.saveToken(token);
        }
        return token;
    }

    /**
     * @param username 用户名
     * @param password 密码
     * @author Chacker
     * 用户登录
     */
    //@PostMapping("/login")
    //@ResponseBody
    public String loginBak(@Para("username") String username, @Para("password") String password) {
        String key = BaseConstant.USER_LOGIN_ERROR_KEY + username;
        Redis redis = RedisManager.getRedis0();
        long beforeTime = System.currentTimeMillis() - 60 * 5 * 1000;
        if (redis.exists(key)) {
            if (redis.zcount(key, beforeTime, System.currentTimeMillis()) >= 5) {
                Set zrevrange = redis.zrevrange(key, 4, 5);
                Long time = (Long) zrevrange.iterator().next() + 60 * 5 * 1000;
                long expire = (time - System.currentTimeMillis()) / 1000;
                //renderJson(R.error("密码错误次数过多，请等" + expire + "秒后在重试！"));
                return returnError(R.error("密码错误次数过多，请等" + expire + "秒后在重试！").toString());
            }
        }
        redis.zadd(key, System.currentTimeMillis(), System.currentTimeMillis());
        if (StrUtil.isEmpty(username) || StrUtil.isEmpty(password)) {
            return returnError(R.error("请输入用户名和密码！").toString());
        }
        AdminUser user = AdminUser.dao.findFirst(Db.getSql("admin.user.queryByUserName"), username.trim());
        if (user == null) {
            return returnError(R.error("用户名或密码错误！").toString());
        }
        if (user.getStatus() == 0) {
            return returnError(R.error("账户被禁用！").toString());
        }
        if (BaseUtil.verify(username + password, user.getSalt(), user.getPassword())) {
            if (user.getStatus() == 2) {
                user.setStatus(1);
            }
            redis.del(key);
            String token = IdUtil.simpleUUID();
            //user.setLastLoginIp(BaseUtil.getLoginAddress(getRequest()));
            user.setLastLoginTime(new Date());
            user.update();
            user.setRoles(adminRoleService.queryRoleIdsByUserId(user.getUserId()));
            redis.setex(token, 3600, user);
            user.remove("password", "salt");
            //setCookie("Admin-Token", token, 3600*24,true);
            return returnError(R.ok().put("Admin-Token", token).put("user", user).put("auth", adminRoleService.auth(user.getUserId())).toString());
        } else {
            Log.getLog(getClass()).warn("用户登录失败");
            return returnError(R.error("用户名或密码错误！").toString());
        }

    }

    /**
     * 退出登录
     */
    @GetMapping("/logout")
    public R logout() throws Exception {
        LoginUser token = BaseUtil.getUser();
        if (token != null) {
            RedisManager.getRedis().del(token.getTokenid());
            //removeCookie("Admin-Token");
            tokenService.removeToken(token.getUsername());
            tokenCacheService.removeToken(token.getTokenid());
        }

        return (R.ok());
    }

    //@PostMapping("/version")
    //@ResponseBody
    public Object version() {
        return JSON.parseObject(R.ok().put("name", BaseConstant.NAME).put("version", BaseConstant.VERSION).toString());
    }


    public void ping() {
        List<String> arrays = new ArrayList<>();
        Connection connection = null;
        try {
            connection = Db.use().getConfig().getConnection();
            if (connection != null) {
                arrays.add("数据库连接成功");
            }
        } catch (Exception e) {
            arrays.add("数据库连接异常");
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (SQLException e) {
                    Log.getLog(getClass()).error("", e);
                }
            }

        }
        try {
            //String ping = RedisManager.getRedis().ping();
            String ping = "";
            if ("PONG".equals(ping)) {
                arrays.add("Redis配置成功");
            } else {
                arrays.add("Redis配置失败");
            }
        } catch (Exception e) {
            arrays.add("Redis配置失败");
        }
        //renderJson(R.ok().put("data", arrays));
    }
}
