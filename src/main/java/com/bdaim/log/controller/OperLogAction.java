package com.bdaim.log.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.log.dto.UserOperLogDTO;
import com.bdaim.log.entity.OperLog;
import com.bdaim.log.entity.UserOperLog;
import com.bdaim.log.service.OperLogService;
import com.bdaim.rbac.dto.RoleEnum;
import com.bdaim.util.DatetimeUtils;
import com.bdaim.util.StringUtil;

import net.sf.json.JSONArray;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Controller
@RequestMapping("/operlog")
/**
 * 操作日期
 *
 */
public class OperLogAction extends BasicAction {

    public static final Logger LOG = Logger.getLogger(OperLogAction.class);

    @Resource
    private OperLogService operlogserv;
    @Resource
    private TokenCacheService tokenCacheService;


    public OperLogAction() {
        super.pageName = "操作日志";
    }

    /*
     * example:http://10.12.7.114:18080/tag/operlog/getTopnObjectIdByDateAndType?topn=10&typeuri=/tag/label/getChildrenById
     */
    @ResponseBody
    @RequestMapping("/getTopnObjectIdByDateAndType")
    public String getTopnObjectIdByDateAndType(String typeuri, int topn) {
        Date date = new Date();
        List<OperLog> lst = operlogserv.getTopnObjectIdByDateAndType(typeuri, topn, date);
        JSONArray ja = JSONArray.fromObject(lst);
//		super.operlog(request, 0);
        return ja.toString();
    }

    /*
     * example:http://10.12.7.114:18080/tag/operlog/getTopnPageByDate?topn=10
     */
    @ResponseBody
    @RequestMapping("/getTopnPageByDate")
    public String getTopnPageByDate(int topn) {
        Date date1 = DatetimeUtils.getMonthStart();
        Date date2 = DatetimeUtils.getMonthEnd();
        List<OperLog> lst = operlogserv.getTopnPageByDate(date1, date2, topn);
        JSONArray ja = JSONArray.fromObject(lst);
//		super.operlog(request, 0);
        return ja.toString();
    }

    /*
     * example:http://10.12.7.114:18080/tag/operlog/getOperLogInfo?start=0&limit=10&date1=2016-05-01&date2=2016-05-30&oper_uname=admin&oper_page_name=标签体系
     */
    @ResponseBody
    @RequestMapping("/getOperLogInfo")
    public String getOperLogInfo(OperLog entity, Page page, Date date1, Date date2, String order_field, String order_asc) {
        List<OperLog> lst = operlogserv.getOperLogInfo(entity, page, date1, date2, order_field, order_asc);
        long count = operlogserv.getOperLogInfoTotalCount(entity, date1, date2);
        Map<String, Object> mmp = new HashMap<String, Object>();
        mmp.put("stores", lst);
        mmp.put("total", count);

//		super.operlog(request, 0);
        return JSON.toJSONString(mmp);
    }

    /*
     * example:http://10.12.7.114:18080/tag/operlog/getOperLogInfoTotalCount?date1=2016-05-01&date2=2016-05-30&oper_uname=admin&oper_page_name=标签体系
     */
    @ResponseBody
    @RequestMapping("/getOperLogInfoTotalCount")
    public String getOperLogInfoTotalCount(OperLog entity, Date date1, Date date2) {
        long count = operlogserv.getOperLogInfoTotalCount(entity, date1, date2);
        JSONArray ja = JSONArray.fromObject(count);
        return ja.toString();
    }


    /**
     * 保存用户行为记录
     *
     * @param entity
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/saveUserOperateLog", method = RequestMethod.POST)
    public String saveUserOperateLog(UserOperLog entity) {
        long userId = 0L;
        LoginUser userDetail = (LoginUser) tokenCacheService.getToken(request.getHeader("Authorization"));
        if (userDetail == null) {
            LOG.warn("未获取到当前登录用户类型失败,默认为前台用户");
        } else {
            userId = userDetail.getId();
        }
        saveUserOperlog(userId, entity.getEventType(), entity.getImei(), entity.getMac(), entity.getBrowser(), entity.getClient(), entity.getFromChannel(),
                entity.getActivityId(), entity.getObjectCode(), "", entity.getRefer());
        return returnJsonData(null);
    }

    /**
     * 用户行为记录分页查询
     *
     * @param pageParam
     * @param error
     * @param entity
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/pageUserOperateLog", method = RequestMethod.POST)
    public String pageUserOperateLog(@Valid PageParam pageParam, BindingResult error, UserOperLogDTO entity) {
        if (error.hasErrors()) {
            return getErrors(error);
        }
        LoginUser userDetail = (LoginUser) tokenCacheService.getToken(request.getHeader("Authorization"));
        boolean groupBy = false;
        int beforeMonth = 0;
        // 未获取到用户ID则按照请求ip查询
        if (userDetail == null) {
            LOG.warn("未获取到当前登录用户类型失败,默认为前台用户");
            entity.setIp(getIpAddress(request));
            groupBy = true;
        }
        // 前台用户只查询近1个月的浏览记录
        if (userDetail != null && RoleEnum.ROLE_CUSTOMER.getRole().equals(userDetail.getRole())) {
            beforeMonth = 1;
            entity.setUserId(String.valueOf(userDetail.getId()));
            // 用户ID为空按照ip进行查询
            if (StringUtil.isEmpty(entity.getUserId())) {
                entity.setIp(getIpAddress(request));
                groupBy = true;
            }
        }
        Page page = operlogserv.pageUserOperlog(pageParam.getPageNum(), pageParam.getPageSize(), entity, groupBy, beforeMonth);
        return returnJsonData(getPageData(page));
    }

}
