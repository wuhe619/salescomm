package com.bdaim.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.OperLog.OperlogUtils;
import com.bdaim.common.auth.Token;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.AccessDeniedException;
import com.bdaim.common.exception.ParamException;
import com.bdaim.label.service.CommonService;
import com.bdaim.log.entity.OperLog;
import com.bdaim.log.entity.UserOperLog;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.rbac.entity.User;
import com.bdaim.rbac.service.UserService;
import com.bdaim.util.CalendarUtil;
import com.bdaim.util.PropertiesUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.text.SimpleDateFormat;
import java.util.*;

/*import com.ztwj.mrp.core.business.UserManager;
import com.ztwj.mrp.core.service.CommonTreeOperator;
import com.ztwj.mrp.core.service.TreeResourceService;
import com.ztwj.mrp.core.treeresource.AbstractTreeResource;
import com.ztwj.mrp.core.treeresource.CommonTreeResource;*/

public class BasicAction {

    private final static Logger LOG = LoggerFactory.getLogger(BasicAction.class);
    protected final static String ADMIN_CUST_ID = "-1";
    protected HttpServletRequest request;
    //	protected HttpServletResponse response;
//    protected ThreadLocal<User> user = new ThreadLocal<User>();
    @Resource
    private UserService userService;
    @Resource
    protected CommonService commonService;
    public List<String> labelSystem = new ArrayList<String>(); //标签体系
    //	public  static String LABEL_TYPE = ""; //标签体系类型，值从cookie中获取，
    protected String pageName = "其他页面";

    protected static final Properties PROPERTIES = new Properties(System.getProperties());

    /*
     * 获取当前登录用户的USER
     */
    protected LoginUser opUser() {
        Token u = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if(u instanceof LoginUser) 
        	return (LoginUser)u;
        else
            return new LoginUser(0L, "", "", null);
    }
//    
//    public User getCurrentUser(HttpServletRequest request) {
//        Map<String, Object> contextMap = new HashMap<String, Object>();
//        HttpSession session = request.getSession();
//        String userId = null;
////		userId = "18888";
//        userId = PropertiesUtil.getStringValue(ConstantsUtil.DEFAULT_LOGIN_USER);
//        if (null != userId && !"".equals(userId) && NumberUtils.isNumber(userId)) {
//            LogUtil.info("使用默认用户登陆，USERID:" + userId);
//        } else {
//            LogUtil.info("不能找到默认用户，是用SESSION及CAS登陆");
//            try {
//                AttributePrincipal principal = AssertionHolder.getAssertion().getPrincipal();
//                Map attributes = principal.getAttributes();
//                userId = attributes.get("id").toString();
//            } catch (Exception e) {
////				e.printStackTrace();
//                LogUtil.info("no current login user...");
//            }
//        }
//        User curruser = null;
//        curruser = (User) session.getAttribute("_USER");
//        if (curruser == null) {
//            if (null != userId && userId.matches("[0-9]+")) {
//                curruser = userService.getUserById(Long.valueOf(userId));
//                if (curruser == null)
//                    throw new NullPointerException("系统异常:用户信息不存在");
//                session.setAttribute("_USER", curruser);
//                this.user.set(curruser);
//            } else {
//                this.user.set(null);
//            }
//        } else {
//            this.user.set(curruser);
//        }
//        return curruser;
//    }
//
//    /**
//     * 绑定用户信息到_user，供前端展示使用
//     *
//     * @param
//     * @return
//     */
//    @SuppressWarnings("rawtypes")
//    @ModelAttribute(value = "contextMap")
//    public Map<String, Object> setUserContext(HttpServletRequest request) {
//
//        //commonService.init();
//        Map<String, Object> contextMap = new HashMap<String, Object>();
//        HttpSession session = request.getSession();
//
//        String userId = null;
//        userId = PropertiesUtil.getStringValue(ConstantsUtil.DEFAULT_LOGIN_USER);
//        if (null != userId && !"".equals(userId) && NumberUtils.isNumber(userId)) {
//            LogUtil.info("使用默认用户登陆，USERID:" + userId);
//        } else {
//            LogUtil.info("不能找到默认用户，是用SESSION及CAS登陆");
////			userId = "18888";
//            try {
//                LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
////				AttributePrincipal principal = AssertionHolder.getAssertion().getPrincipal();
////				Map attributes = principal.getAttributes();
//                userId = String.valueOf(user.getUserDO().getId());
//                //userId = attributes.get("id").toString();
//            } catch (Exception e) {
////				e.printStackTrace();
//                String err = "can not find current user, maybe not login...";
//                LogUtil.error(err);
//                System.out.println("[ERROR]" + err);
//            }
//        }
//        String host = PropertiesUtil.getStringValue("MAIN_HOST");
//        Integer port = PropertiesUtil.getIntegerValue("MAIN_PORT");
//        String mrpHost = PropertiesUtil.getStringValue("MRP_HOST");
//        Integer mrpPort = PropertiesUtil.getIntegerValue("MRP_PORT");
//        String ssoHost = PropertiesUtil.getStringValue("SSO_HOST");
//        Integer ssoPort = PropertiesUtil.getIntegerValue("SSO_PORT");
//        User u = null;
//        u = (User) session.getAttribute("_USER");
//        if (u == null) {
//            if (null != userId && userId.matches("[0-9]+")) {
//                u = userService.getUserById(Long.valueOf(userId));
//                if (null == u)
//                    throw new NullPointerException("系统异常:用户信息不存在");
//                session.setAttribute("_USER", u);
//                this.user.set(u);
//            } else {
//                this.user.set(null);
//            }
//        } else {
//            this.user.set(u);
//        }
//        session.setAttribute("_USER2", "sadad");
//        if (u != null)
//            contextMap.put("userName", u.getName());
//        else
//            contextMap.put("userName", "temp");
//        String projectPath = request.getContextPath();
//        if (!projectPath.endsWith("/")) {
//            projectPath += "/";
//        }
//        if (port != 80) {
//            contextMap.put("index", host + ":" + port + projectPath);
//        } else {
//            contextMap.put("index", host + projectPath);
//        }
//        if (mrpPort != 80) {
//            contextMap.put("mrpIndex", mrpHost + ":" + mrpPort + "/mrp/");
//        } else {
//            contextMap.put("mrpIndex", mrpHost + "/mrp/");
//        }
//        if (ssoPort != 80) {
//            contextMap.put("ssoIndex", ssoHost + ":" + ssoPort + "/cas/");
//        } else {
//            contextMap.put("ssoIndex", ssoHost + "/cas/");
//        }
//        contextMap.put("projectPath", request.getContextPath());
//        JSONArray authArray = (JSONArray) session.getAttribute("_USERAUTH");
//        if (null == authArray) {
//            try {
//                authArray = getAuthArray();
//                session.setAttribute(Constant.USER_AUTH_KEY, authArray);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        contextMap.put("restPath", PropertiesUtil.getStringValue("restPath"));
//        contextMap.put("permission", authArray);
//        Calendar cale = Calendar.getInstance();
//        cale.set(Calendar.MONTH, cale.get(Calendar.MONTH) - 1);
//        contextMap.put("hisTime", CalendarUtil.getDateString(cale, CalendarUtil.SHORT_DATE_FORMAT));
//        contextMap.put("startTime", CalendarUtil.getDateString(Calendar.getInstance(), CalendarUtil.SHORT_DATE_FORMAT));
//        cale = Calendar.getInstance();
//        cale.set(Calendar.MONTH, cale.get(Calendar.MONTH) + 1);
//        contextMap.put("endTime", CalendarUtil.getDateString(cale, CalendarUtil.SHORT_DATE_FORMAT));
//        contextMap.put("cpTime", Calendar.getInstance().get(Calendar.YEAR));//版权时间
//        return contextMap;
//    }
//
//    private JSONArray getAuthArray() {
//        JSONArray authArray = new JSONArray();
////		UserManager manager = new UserManagerImpl();
////		TreeResourceService source = new CommonTreeOperator();
////		AbstractTreeResource tree = source.queryUserTree(manager.getManager(request), new CommonTreeResource(0L), null);
////		if(null != tree){
////			List<AbstractTreeResource> nodes = tree.getNotes();
////			authArray = getAuthArrayFromList(nodes);
////		}
//        return authArray;
//    }
//	/*private JSONArray getAuthArrayFromList(List<AbstractTreeResource> list){
//        JSONArray rs=new JSONArray();
//        if (list==null||list.size()==0)return null;
//        JSONObject note=null;
//        for (AbstractTreeResource r:list){
//            note=new JSONObject();
//            note.put("name",r.getName());
//            note.put("id", r.getID());
//            note.put("uri", r.getUri());
//            note.put("order",r.getSn());
//            note.put("type",r.getType());
//            if (r.getNotes()!=null&&r.getNotes().size()>0)note.put("child",getAuthArrayFromList(r.getNotes()));
//            rs.add(note);
//        }
//        return rs;
//    }*/
//

    /**
     * 异常捕获处理
     *
     * @param ex
     * @param response
     * @param request
     */
    /*@ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception ex, HttpServletResponse response,
                                 HttpServletRequest request) {
        ex.printStackTrace();
    }*/

    /**
     * 数据格式
     *
     * @param binder
     */
    @InitBinder
    public void initBinder(WebDataBinder binder) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                dateFormat, false));
    }
//

    /**
     * 注入request
     *
     * @param request
     */
    @Resource
    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    //
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
//
//    protected void operlog(HttpServletRequest request, int objectid) {
//        this.operlog(request, objectid, this.pageName);
//    }
//
//    protected void operlog(HttpServletRequest request, int objectid, String pageName) {
//        try {//operation logs
//            if (OperlogUtils.isEnable()) {//to write operation log
//                User user = this.getCurrentUser(request);
//                OperLog entity = new OperLog();
//                if (null != user) {
//                    entity.setOper_uid(user.getId());
//                    entity.setOper_uname(user.getName());
//                }
//                entity.setOper_object_id(objectid);
//                entity.setOper_page_name(pageName);
//                entity.setOper_object_id(objectid);
//                entity.setOper_source_ip(request.getRemoteAddr());
//                entity.setOper_source_port(request.getRemotePort());
//                entity.setOper_target_ip(request.getLocalAddr());
//                entity.setOper_target_port(request.getLocalPort());
//                entity.setOper_project(request.getContextPath());
//                entity.setOper_uri(request.getRequestURI());
//                OperlogUtils.getInstance().insertLog(entity);
//            }
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    protected void operlog(HttpServletRequest request, List<Map<String, Object>> objectid) {
//        if (null == objectid || 0 == objectid.size())
//            return;
//        if (!OperlogUtils.isEnable())
//            return;
//        for (Map<String, Object> onemmp : objectid) {
//            if (null != onemmp.get("id")) {
//                try {//to write operation log
//                    User user = this.getCurrentUser(request);
//                    OperLog entity = new OperLog();
//                    entity.setOper_uid(user.getId());
//                    entity.setOper_uname(user.getName());
//                    entity.setOper_object_id((Integer) onemmp.get("id"));
//                    entity.setOper_page_name(this.pageName);
//                    entity.setOper_source_ip(request.getRemoteAddr());
//                    entity.setOper_source_port(request.getRemotePort());
//                    entity.setOper_target_ip(request.getLocalAddr());
//                    entity.setOper_target_port(request.getLocalPort());
//                    entity.setOper_project(request.getContextPath());
//                    entity.setOper_uri(request.getRequestURI());
//                    OperlogUtils.getInstance().insertLog(entity);
//                } catch (Exception ex) {
//                    ex.printStackTrace();
//                }
//            }
//        }
//    }

    /**
     * 返回json结构数据
     *
     * @param object
     * @return
     */
    protected String returnJsonData(Object object) {
        JSONObject json = new JSONObject();
        json.put("data", object);
        return json.toJSONString();
    }

    /**
     * 处理page对象  返回total和list
     *
     * @param page
     * @return
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
     *
     * @return
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
     *
     * @param userId
     * @return
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
//        HttpSession session = request.getSession();
//
//        String userId = null;
//        userId = PropertiesUtil.getStringValue(ConstantsUtil.DEFAULT_LOGIN_USER);
//        if (null != userId && !"".equals(userId) && NumberUtils.isNumber(userId)) {
//            LogUtil.info("使用默认用户登陆，USERID:" + userId);
//        } else {
//            LogUtil.info("不能找到默认用户，是用SESSION及CAS登陆");
//            try {
//                LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
//                userId = String.valueOf(user.getUser().getId());
//            } catch (Exception e) {
////				e.printStackTrace();
//                String err = "can not find current user, maybe not login...";
//                LogUtil.error(err);
//                System.out.println("[ERROR]" + err);
//            }
//        }
//        String host = PropertiesUtil.getStringValue("MAIN_HOST");
//        Integer port = PropertiesUtil.getIntegerValue("MAIN_PORT");
//        String mrpHost = PropertiesUtil.getStringValue("MRP_HOST");
//        Integer mrpPort = PropertiesUtil.getIntegerValue("MRP_PORT");
//        String ssoHost = PropertiesUtil.getStringValue("SSO_HOST");
//        Integer ssoPort = PropertiesUtil.getIntegerValue("SSO_PORT");
//        User u = null;
//        u = (User) session.getAttribute("_USER");
//        if (u == null) {
//            if (null != userId && userId.matches("[0-9]+")) {
//                u = userService.getUserById(Long.valueOf(userId));
//                if (null == u)
//                    throw new NullPointerException("系统异常:用户信息不存在");
//                session.setAttribute("_USER", u);
//                this.user.set(u);
//            } else {
//                this.user.set(null);
//            }
//        } else {
//            this.user.set(u);
//        }
//        session.setAttribute("_USER2", "sadad");
        /*LoginUser user = (LoginUser) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (user != null) {
            contextMap.put("userName", user.getName());
        } else {
            contextMap.put("userName", "temp");
        }*/

//        String projectPath = request.getContextPath();
//        if (!projectPath.endsWith("/")) {
//            projectPath += "/";
//        }
//        if (port != 80) {
//            contextMap.put("index", host + ":" + port + projectPath);
//        } else {
//            contextMap.put("index", host + projectPath);
//        }
//        if (mrpPort != 80) {
//            contextMap.put("mrpIndex", mrpHost + ":" + mrpPort + "/mrp/");
//        } else {
//            contextMap.put("mrpIndex", mrpHost + "/mrp/");
//        }
//        if (ssoPort != 80) {
//            contextMap.put("ssoIndex", ssoHost + ":" + ssoPort + "/cas/");
//        } else {
//            contextMap.put("ssoIndex", ssoHost + "/cas/");
//        }
        contextMap.put("projectPath", request.getContextPath());
//        JSONArray authArray = (JSONArray) session.getAttribute("_USERAUTH");
//        if (null == authArray) {
//            try {
//                authArray = getAuthArray();
//                session.setAttribute(Constant.USER_AUTH_KEY, authArray);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
        contextMap.put("restPath", PropertiesUtil.getStringValue("restPath"));
//        contextMap.put("permission", authArray);
        Calendar cale = Calendar.getInstance();
        cale.set(Calendar.MONTH, cale.get(Calendar.MONTH) - 1);
        contextMap.put("hisTime", CalendarUtil.getDateString(cale, CalendarUtil.SHORT_DATE_FORMAT));
        contextMap.put("startTime", CalendarUtil.getDateString(Calendar.getInstance(), CalendarUtil.SHORT_DATE_FORMAT));
        cale = Calendar.getInstance();
        cale.set(Calendar.MONTH, cale.get(Calendar.MONTH) + 1);
        contextMap.put("endTime", CalendarUtil.getDateString(cale, CalendarUtil.SHORT_DATE_FORMAT));
//        contextMap.put("cpTime", Calendar.getInstance().get(Calendar.YEAR));//版权时间
        return contextMap;
    }

//    private JSONArray getAuthArray() {
//        JSONArray authArray = new JSONArray();
//        UserManager manager = new UserManagerImpl();
//        AbstractTreeResource tree = resourceService.queryUserTree(manager.getManager(request), new CommonTreeResource(0L), null);
//        if (null != tree) {
//            List<AbstractTreeResource> nodes = tree.getNotes();
//            authArray = getAuthArrayFromList(nodes);
//        }
//        return authArray;
//    }

//    private JSONArray getAuthArrayFromList(List<AbstractTreeResource> list) {
//        JSONArray rs = new JSONArray();
//        if (list == null || list.size() == 0) return null;
//        JSONObject note = null;
//        for (AbstractTreeResource r : list) {
//            note = new JSONObject();
//            note.put("name", r.getName());
//            note.put("id", r.getID());
//            note.put("uri", r.getUri());
//            note.put("order", r.getSn());
//            note.put("type", r.getType());
//            if (r.getNotes() != null && r.getNotes().size() > 0) note.put("child", getAuthArrayFromList(r.getNotes()));
//            rs.add(note);
//        }
//        return rs;
//    }

    /**
     * 异常捕获处理
     *
     * @param ex
     * @param response
     * @param request
     */
    @ExceptionHandler(Exception.class)
    public void exceptionHandler(Exception ex, HttpServletResponse response,
                                 HttpServletRequest request) {
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
                    entity.setOper_uname(user.getName());
                }
                entity.setOper_object_id((int)objectid);
                entity.setOper_page_name(pageName);
                entity.setOper_object_id((int)objectid);
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
        if(ip ==null || ip.length() ==0 ||"unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if(ip ==null || ip.length() ==0 ||"unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if(ip ==null || ip.length() ==0 ||"unknown".equalsIgnoreCase(ip)) {
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

}
