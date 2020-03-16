package com.bdaim.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.AppConfig;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.OperLog.OperlogUtils;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.AccessDeniedException;
import com.bdaim.common.exception.ParamException;
import com.bdaim.crm.common.config.json.ErpJsonFactory;
import com.bdaim.crm.utils.R;
import com.bdaim.label.service.CommonService;
import com.bdaim.log.entity.OperLog;
import com.bdaim.log.entity.UserOperLog;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.rbac.entity.User;
import com.bdaim.rbac.service.UserService;
import com.bdaim.util.CalendarUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.json.Json;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class BasicAction {

    private final static Logger LOG = LoggerFactory.getLogger(BasicAction.class);
    protected final static String ADMIN_CUST_ID = "-1";
    protected HttpServletRequest request;
    @Resource
    private UserService userService;
    @Autowired
    private TokenCacheService<LoginUser> tokenCacheService;
    @Resource
    protected CommonService commonService;
    public List<String> labelSystem = new ArrayList<String>(); //标签体系
    protected String pageName = "其他页面";

    protected static final Properties PROPERTIES = new Properties(System.getProperties());

    /*
     * 获取当前登录用户的USER
     */
    protected LoginUser opUser() {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && !"".equals(authorization)) {
            LoginUser u = tokenCacheService.getToken(authorization, LoginUser.class);
            if (u != null)
                return u;
        }
        return new LoginUser(0L, "", "");
    }

    /**
     * 数据格式
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(dateFormat, false));
    }
//

    /**
     * 注入request
     */
    @Resource
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    //
    protected String getErrors(BindingResult bindingResult) {
        Map<String, Object> errorData = new HashMap<>(16);
        for (FieldError error : bindingResult.getFieldErrors()) {
            errorData.put(error.getField(), error.getDefaultMessage());
        }
        Map<String, Object> result = new HashMap<>(16);
        result.put("data", errorData);
        return JSON.toJSONString(result);
    }

    protected String returnSuccess() {
        return returnJson(1, "成功");
    }

    protected String returnSuccess(String message) {
        return returnJson(1, message);
    }

    protected String returnSuccess(int code, String message) {
        return returnJson(code, message);
    }

    protected String returnError() {
        return returnJson(0, "失败");
    }

    protected String returnError(String message) {
        return returnJson(0, message);
    }

    protected String returnError(int code, String message) {
        return returnJson(code, message);
    }


    protected String returnJson(int code, String message) {
        Map<String, Object> result = new HashMap<>(16);
        Map<String, Object> data = new HashMap<>(16);
        data.put("code", code);
        data.put("message", message);
        result.put("data", data);
        return JSON.toJSONString(data);
    }

    /**
     * 返回json结构数据
     */
    protected String returnJsonData(Object object) {
        JSONObject json = new JSONObject();
        json.put("data", object);
        return json.toJSONString();
    }

    /**
     * 处理page对象  返回total和list
     */
    protected Map<String, Object> getPageData(Page page) {
        if (page == null) {
            page = new Page();
        }
        Map<String, Object> data = new HashMap<>();
        data.put("list", page.getData());
        data.put("total", page.getTotal());
        return data;
    }

    /**
     * 获取用户查询权限实体
     */
    protected UserQueryParam getUserQueryParam() {
        UserQueryParam userQueryParam = new UserQueryParam();
        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
            userQueryParam.setCustId(ADMIN_CUST_ID);
        } else {
            userQueryParam.setCustId(opUser().getCustId());
            userQueryParam.setUserId(String.valueOf(opUser().getId()));
            userQueryParam.setUserType(opUser().getUserType());
            userQueryParam.setUserGroupRole(opUser().getUserGroupRole());
            userQueryParam.setUserGroupId(opUser().getUserGroupId());
        }
        return userQueryParam;
    }

    /**
     * 是否为后台用户
     *
     * @return true-是 false-否
     */
    protected boolean isBackendUser() {
        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
            return true;
        }
        return false;
    }

    /**
     * 绑定用户信息到_user，供前端展示使用
     */
    @SuppressWarnings("rawtypes")
    @ModelAttribute(value = "contextMap")
    public Map<String, Object> setUserContext(HttpServletRequest request) {
        //commonService.init();
        Map<String, Object> contextMap = new HashMap<String, Object>();
        try {
            LoginUser u = opUser();
            contextMap.put("userName", u.getName());
        } catch (Exception e) {
            contextMap.put("userName", "");
        }

        contextMap.put("projectPath", request.getContextPath());
        contextMap.put("restPath", AppConfig.getRestPath());
        Calendar cale = Calendar.getInstance();
        cale.set(Calendar.MONTH, cale.get(Calendar.MONTH) - 1);
        contextMap.put("hisTime", CalendarUtil.getDateString(cale, CalendarUtil.SHORT_DATE_FORMAT));
        contextMap.put("startTime", CalendarUtil.getDateString(Calendar.getInstance(), CalendarUtil.SHORT_DATE_FORMAT));
        cale = Calendar.getInstance();
        cale.set(Calendar.MONTH, cale.get(Calendar.MONTH) + 1);
        contextMap.put("endTime", CalendarUtil.getDateString(cale, CalendarUtil.SHORT_DATE_FORMAT));
        return contextMap;
    }


    /**
     * 异常捕获处理
     */
    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception ex, HttpServletResponse response, HttpServletRequest request) {
        LOG.error("系统异常:", ex);
        PrintWriter out = null;
        try {
            response.setContentType("application/json; charset=utf-8");
            out = response.getWriter();
            if (ex instanceof AccessDeniedException) {
                out.println(returnError(ex.getMessage()));
            } else if (ex instanceof ParamException) {
                out.println(returnError(ex.getMessage()));
            } else {
                out.println(returnError("系统异常"));
            }

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (out != null) {
                out.flush();
                out.close();
            }
        }
    }

    protected void operlog(int objectid) {
        this.operlog(objectid, this.pageName);
    }

    protected void operlog(int objectid, String pageName) {
        try {//operation logs
            if (OperlogUtils.isEnable()) {//to write operation log
                OperLog entity = new OperLog();
                LoginUser user = opUser();
                if (null != user) {
                    entity.setOper_uid(user.getId());
                    entity.setOper_uname(user.getName());
                }
                entity.setOper_object_id(objectid);
                entity.setOper_page_name(pageName);
                entity.setOper_object_id(objectid);
                entity.setOper_source_ip(request.getRemoteAddr());
                entity.setOper_source_port(request.getRemotePort());
                entity.setOper_target_ip(request.getLocalAddr());
                entity.setOper_target_port(request.getLocalPort());
                entity.setOper_project(request.getContextPath());
                entity.setOper_uri(request.getRequestURI());
                OperlogUtils.getInstance().insertLog(entity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void operlog(long objectid, String pageName) {
        try {//operation logs
            if (OperlogUtils.isEnable()) {//to write operation log
                OperLog entity = new OperLog();
                LoginUser user = opUser();
                if (null != user) {
                    entity.setOper_uid(user.getId());
                    entity.setOper_uname(user.getUserName());
                }
                entity.setOper_object_id((int) objectid);
                entity.setOper_page_name(pageName);
                entity.setOper_object_id((int) objectid);
                entity.setOper_source_ip(request.getRemoteAddr());
                entity.setOper_source_port(request.getRemotePort());
                entity.setOper_target_ip(request.getLocalAddr());
                entity.setOper_target_port(request.getLocalPort());
                entity.setOper_project(request.getContextPath());
                entity.setOper_uri(request.getRequestURI());
                OperlogUtils.getInstance().insertLog(entity);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    protected void operlog(List<Map<String, Object>> objectid) {
        if (null == objectid || 0 == objectid.size())
            return;
        if (!OperlogUtils.isEnable())
            return;
        for (Map<String, Object> onemmp : objectid) {
            if (null != onemmp.get("id")) {
                try {//to write operation log
                    User user = opUser().getUser();
                    OperLog entity = new OperLog();
                    entity.setOper_uid(user.getId());
                    entity.setOper_uname(user.getName());
                    entity.setOper_object_id((Integer) onemmp.get("id"));
                    entity.setOper_page_name(this.pageName);
                    entity.setOper_source_ip(request.getRemoteAddr());
                    entity.setOper_source_port(request.getRemotePort());
                    entity.setOper_target_ip(request.getLocalAddr());
                    entity.setOper_target_port(request.getLocalPort());
                    entity.setOper_project(request.getContextPath());
                    entity.setOper_uri(request.getRequestURI());
                    OperlogUtils.getInstance().insertLog(entity);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    public static String getIpAddress(HttpServletRequest request) {
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
        return ip;
    }

    /**
     * 保存用户行为记录
     *
     * @param userId      用户ID
     * @param eventType   1-login 2-浏览产品 3-点击按钮 4-点击广告
     * @param imei
     * @param mac
     * @param browser     浏览器
     * @param client      用户端
     * @param fromChannel 渠道
     * @param activityId  活动
     * @param objectCode  产品/渠道/活动id
     * @param refer       浏览网址
     */
    protected void saveUserOperlog(long userId, int eventType, String imei, String mac, String browser, String client, String fromChannel, String activityId, String objectCode, String token, String refer) {
        try {
            UserOperLog entity = new UserOperLog();
            entity.setUserId(userId);
            entity.setIp(getIpAddress(request));
            entity.setImei(imei);
            entity.setMac(mac);
            //获取浏览器信息
            entity.setBrowser(request.getHeader("User-Agent"));
            if (StringUtil.isNotEmpty(browser)) {
                entity.setBrowser(browser);
            }
            entity.setClient(client);
            entity.setFromChannel(fromChannel);
            entity.setActivityId(activityId);
            entity.setEventType(eventType);
            entity.setObjectCode(objectCode);
            entity.setToken(token);
            entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            if (StringUtil.isEmpty(token)) {
                entity.setToken(request.getHeader("Authorization"));
            }
            entity.setRefer(refer);
            if (StringUtil.isEmpty(refer)) {
                entity.setRefer(request.getHeader("Referer"));
            }
            OperlogUtils.getInstance().insertUserOperLog(entity);
        } catch (Exception ex) {
            LOG.error("保存用户行为记录异常:", ex);
        }
    }

    public Object renderJson(Object object) {
        JSONObject json = new JSONObject();
        json.put("data", object);
        return json;
    }

    public R renderCrmJson(Object object) {
        Json json = ErpJsonFactory.me().getJson();
        return (R.ok().put("data", JSON.parse(json.toJson(object))));
    }

    public R renderCrmJson(R r) {
        Json json = ErpJsonFactory.me().getJson();
        Object object = r.get("data");
        return (R.ok().put("data", JSON.parse(json.toJson(object))));
    }

    public String get(String name) {
        return this.request.getParameter(name);
    }

    public Date getParaToDate(String name) {
        if (StringUtil.isEmpty(getPara(name))) {
            return null;
        }
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
        try {
            return format.parse(name);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getPara(String name) {
        return this.request.getParameter(name);
    }

    public Integer getParaToInt(String name) {
        if (StringUtil.isEmpty(getPara(name))) {
            return null;
        }
        return Integer.parseInt(getPara(name));
    }


    public Integer getInt(String name) {
        return getParaToInt(name);
    }


    public Long getLong(String name) {
        if (StringUtil.isEmpty(getPara(name))) {
            return null;
        }
        return NumberConvertUtil.parseLong(getPara(name));
    }

    public Map getKv() {
        Map kv = new HashMap();
        Map<String, String[]> paraMap = this.request.getParameterMap();
        Iterator var3 = paraMap.entrySet().iterator();

        while (var3.hasNext()) {
            Map.Entry<String, String[]> entry = (Map.Entry) var3.next();
            String[] values = (String[]) entry.getValue();
            String value = values != null && values.length > 0 ? values[0] : null;
            kv.put(entry.getKey(), "".equals(value) ? null : value);
        }

        return kv;
    }

}
