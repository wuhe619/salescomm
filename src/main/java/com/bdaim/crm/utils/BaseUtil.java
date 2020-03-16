package com.bdaim.crm.utils;

import cn.hutool.core.date.DateUtil;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.common.config.JfinalConfig;
import com.bdaim.crm.dao.LkCrmAdminRoleDao;
import com.bdaim.crm.dao.LkCrmAdminUserDao;
import com.bdaim.crm.dao.LkCrmSqlViewDao;
import com.bdaim.crm.entity.LkCrmAdminUserEntity;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.admin.service.LkAdminRoleService;
import com.bdaim.crm.erp.admin.service.LkAdminUserService;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.log.Log;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.net.InetAddress;
import java.util.Date;

@Service
public class BaseUtil {

    private static ThreadLocal<HttpServletRequest> threadLocal = new ThreadLocal<>();


    private static TokenCacheService<LoginUser> tokenCacheService;

    private static LkCrmAdminUserDao crmAdminUserDao;

    private static LkAdminRoleService adminRoleService;

    private static LkCrmSqlViewDao crmSqlViewDao;

    private static AdminFieldService adminFieldService;

    public TokenCacheService<LoginUser> getTokenCacheService() {
        return tokenCacheService;
    }

    @Resource
    public void setTokenCacheService(TokenCacheService<LoginUser> tokenCacheService) {
        this.tokenCacheService = tokenCacheService;
    }

    @Resource
    public void setCrmAdminUserDao(LkCrmAdminUserDao crmAdminUserDao) {
        BaseUtil.crmAdminUserDao = crmAdminUserDao;
    }

    @Resource
    public void setAdminRoleService(LkAdminRoleService adminRoleService) {
        BaseUtil.adminRoleService = adminRoleService;
    }

    @Resource
    public void setCrmSqlViewDao(LkCrmSqlViewDao crmSqlViewDao) {
        BaseUtil.crmSqlViewDao = crmSqlViewDao;
    }

    @Resource
    public void setAdminFieldService(AdminFieldService adminFieldService) {
        BaseUtil.adminFieldService = adminFieldService;
    }

    public static String getViewSql(String name) {
        String viewSql = crmSqlViewDao.getViewSql(BaseUtil.getCustId(), name);
        if (StringUtil.isNotEmpty(name) && !name.startsWith("field")) {
            String fieldViewSql = crmSqlViewDao.getViewSql(BaseUtil.getCustId(), "field" + name);
            viewSql = viewSql.replace("?", fieldViewSql);
        }
        if (StringUtil.isEmpty(viewSql)) {
            for (int label = 1; label < 8; label++) {
                adminFieldService.createView(label, BaseUtil.getCustId());
            }
            viewSql = getViewSql(name);
        }
        return "( " + viewSql + " ) temp1 ";
    }

    public static String getViewSqlNotASName(String name) {
        String viewSql = crmSqlViewDao.getViewSql(BaseUtil.getCustId(), name);
        if (StringUtil.isNotEmpty(name) && !name.startsWith("field")) {
            String fieldViewSql = crmSqlViewDao.getViewSql(BaseUtil.getCustId(), "field" + name);
            viewSql = viewSql.replace("?", fieldViewSql);
        }
        if (StringUtil.isEmpty(viewSql)) {
            for (int label = 1; label < 8; label++) {
                adminFieldService.createView(label, BaseUtil.getCustId());
            }
            viewSql = getViewSqlNotASName(name);
        }
        return "( " + viewSql + " ) ";
    }

    /**
     * 获取当前系统是开发开始正式
     *
     * @return true代表为真
     */
    public static boolean isDevelop() {
        return JfinalConfig.prop.getBoolean("jfinal.devMode", Boolean.TRUE);
    }

    /**
     * 获取当前是否是windows系统
     *
     * @return true代表为真
     */
    public static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("windows");
    }

    /**
     * 签名数据
     *
     * @param key  key
     * @param salt 盐
     * @return 加密后的字符串
     */
    public static String sign(String key, String salt) {
        return DigestUtils.md5Hex((key + "erp" + salt).getBytes());
    }

    /**
     * 验证签名是否正确
     *
     * @param key  key
     * @param salt 盐
     * @param sign 签名
     * @return 是否正确 true为正确
     */
    public static boolean verify(String key, String salt, String sign) {
        return sign.equals(sign(key, salt));
    }

    /**
     * 获取当前年月的字符串
     *
     * @return yyyyMMdd
     */
    public static String getDate() {
        return DateUtil.format(new Date(), "yyyyMMdd");
    }

    public static String getIpAddress() {
        Prop prop = PropKit.use("config/undertow.txt");
        try {
            if (isDevelop()) {
                return "http://" + InetAddress.getLocalHost().getHostAddress() + ":" + prop.get("undertow.port", "8080") + "/";
            }
        } catch (Exception e) {
            // TODO Auto-generated catch block
            Log.getLog(BaseUtil.class).error("", e);
        }
        HttpServletRequest request = getRequest();
        /**
         * TODO nginx反向代理下手动增加一个请求头 proxy_set_header proxy_url "代理映射路径";
         * 如 location /api/ {
         *     proxy_set_header proxy_url "api"
         *     proxy_redirect off;
         * 	   proxy_set_header Host $host:$server_port;
         *     proxy_set_header X-Real-IP $remote_addr;
         * 	   proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
         * 	   proxy_set_header X-Forwarded-Proto  $scheme;
         * 	   proxy_connect_timeout 60;
         * 	   proxy_send_timeout 120;
         * 	   proxy_read_timeout 120;
         *     proxy_pass http://127.0.0.1:8080/;
         *    }
         */
        String proxy = request.getHeader("proxy_url") != null ? "/" + request.getHeader("proxy_url") : "";
        return "http://" + request.getServerName() + ":" + request.getServerPort() + request.getContextPath() + proxy + "/";
    }

    public static String getLoginAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip.contains(",")) {
            return ip.split(",")[0];
        } else {
            return ip;
        }
    }

    public static String getLoginAddress() {
        return getLoginAddress(getRequest());
    }

    public static void setRequest(HttpServletRequest request) {
        threadLocal.set(request);
    }

    public static HttpServletRequest getRequest() {
        return threadLocal.get();
    }


    public static LoginUser getUser() {
        LoginUser user = tokenCacheService.getToken(getToken(), LoginUser.class);
        LkCrmAdminUserEntity userEntity = crmAdminUserDao.get(user.getId());
        if (userEntity != null) {
            user.setDeptId(userEntity.getDeptId());
            user.setRoles(adminRoleService.queryRoleIdsByUserId(user.getId()));
        }
        return user;
    }

    public static Long getUserId() {
        return getUser().getId();
    }

    public static String getCustId() {
        return getUser().getCustId();
    }

    public static int getUserType() {
        return NumberConvertUtil.parseInt(getUser().getUserType());
    }

    public static void removeThreadLocal() {
        threadLocal.remove();
    }

    public static String getToken() {
        return getToken(getRequest());
    }

    public static String getToken(HttpServletRequest request) {
        return request.getHeader("Authorization") != null ? request.getHeader("Authorization") : "";
    }

    public static CrmPage crmPage(Page recordPage) {
        return new CrmPage(recordPage);
    }

}
