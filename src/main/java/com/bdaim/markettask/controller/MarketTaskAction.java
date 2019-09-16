package com.bdaim.markettask.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.dto.XzAutoTaskMonitor;
import com.bdaim.callcenter.service.impl.XzCallCenterService;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseCommon;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dto.CustomerUserDTO;
import com.bdaim.markettask.dto.MarketTaskDTO;
import com.bdaim.markettask.dto.MarketTaskListParam;
import com.bdaim.markettask.dto.MarketTaskParam;
import com.bdaim.markettask.service.MarketTaskService;
import com.bdaim.rbac.dto.RoleEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.*;

/**
 * 营销任务
 *
 * @author chengning@salescomm.net
 * @date 2019/4/24
 * @description
 */
@RequestMapping("/marketTask")
@Controller
public class MarketTaskAction extends BasicAction {

    public static final Logger LOG = LoggerFactory.getLogger(MarketTaskAction.class);

    @Resource
    private MarketTaskService marketTaskService;

    @Resource
    private XzCallCenterService xzCallCenterService;

    /**
     * 创建、编辑营销任务
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    @ResponseBody
    public String save(@RequestBody JSONObject jsonObject) {
        MarketTaskParam param = JSON.parseObject(jsonObject.toJSONString(), MarketTaskParam.class);
        // 1-更新基础信息 2-更新成员
        int operation = jsonObject.getIntValue("operation");
        param.setCustId(opUser().getCustId());
        Map<String, Object> data;
        Map<String, Object> result = new HashMap<>();
        int code = 0;
        String msg = "失败";
        try {
            if (StringUtil.isEmpty(param.getId())) {
                param.setCreateUid(opUser().getId());
                data = marketTaskService.save(param, opUser());
                code = NumberConvertUtil.parseInt(data.get("code"));
                if (code == 1) {
                    result.put("data", data.get("id"));
                }
            } else {
                param.setUpdateUid(opUser().getId());
                code = marketTaskService.update(param, operation, opUser());
            }
        } catch (ParamException e) {
            LOG.error("创建/编辑营销任务异常,", e);
            msg = "客户未配置讯众呼叫中心企业账号";
        } catch (Exception e) {
            LOG.error("创建/编辑营销任务异常,", e);
        }
        if (code == 1) {
            result.put("code", code);
            return returnJsonData(result);
        }
        return returnError(msg);
    }

    @RequestMapping(value = "/listMarketTaskUser", method = RequestMethod.GET)
    @ResponseBody
    public String listMarketTaskUser(String marketTaskId, String resourceId, String sStartAccount, String sEndAccount, String uStartAccount, String uEndAccount) {
        if (StringUtil.isEmpty(resourceId)) {
            throw new ParamException("resourceId必填");
        }
        Map<String, Object> data = new HashMap<>();
        List<CustomerUserDTO> selectUsers = null, unUsers = null;
        try {
            // 检查营销任务权限
            if (StringUtil.isNotEmpty(marketTaskId)) {
                boolean status = marketTaskService.checkMarketTaskPermission(opUser().getCustId(), marketTaskId);
                if (!status) {
                    return returnError("权限不足");
                }
                selectUsers = marketTaskService.listSelectMarketTaskUser(marketTaskId, opUser().getCustId(), sStartAccount, sEndAccount);
            }
            unUsers = marketTaskService.listNotInMarketTaskUser(marketTaskId, resourceId, opUser().getCustId(), uStartAccount, uEndAccount);
        } catch (Exception e) {
            LOG.error("查询营销任务下的成员列表失败,", e);
            unUsers = new ArrayList<>();
            selectUsers = new ArrayList<>();
        }
        data.put("left", unUsers);
        data.put("right", selectUsers);
        return returnJsonData(data);
    }

    /**
     * 营销任务列表查询
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/listMarketTask", method = RequestMethod.POST)
    @ResponseBody
    public String listMarketTask(MarketTaskListParam param) {
        JSONObject json = new JSONObject();
        List<Map<String, Object>> data = null;
        try {
            if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
                data = marketTaskService.adminListMarketTask(param);
            } else {
                data = marketTaskService.listMarketTask(opUser(), param);
            }
        } catch (Exception e) {
            data = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("total", 0);
            map.put("list", new ArrayList<>());
            data.add(map);
            LOG.error("获取营销任务列表异常,", e);
        }
        json.put("data", data);
        return json.toJSONString();
    }

    /**
     * 营销任务详情列表
     *
     * @param page
     * @param error
     * @param marketTaskId
     * @param id
     * @param userName
     * @param status
     * @param callType
     * @param action
     * @param intentLevel
     * @param labelProperty
     * @return
     */
    @RequestMapping(value = "/getTaskDetailList", method = RequestMethod.GET)
    @CacheAnnotation
    @ResponseBody
    public String getTaskDetailList(@Valid PageParam page, BindingResult error, String marketTaskId, String id, String userName, Integer status,
                                    String callType, String action, String intentLevel, String labelProperty) throws Exception {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        if (StringUtil.isEmpty(marketTaskId)) {
            throw new TouchException("参数错误");
        }
        JSONArray custProperty = JSON.parseArray(labelProperty);
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        Page data = null;
        try {
            data = marketTaskService.getCustomGroupDataV4(opUser(), marketTaskId, page.getPageNum(), page.getPageSize(), id, userName, status, callType, action, intentLevel, custProperty);
        } catch (Exception e) {
            LOG.error("查询任务详情列表异常,", e);
        }
        if (data == null) {
            data = new Page();
        }
        map.put("data", data.getData());
        map.put("total", data.getTotal());
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 导出营销任务成功单
     *
     * @param response
     * @param startTime
     * @param endTime
     * @param marketTaskId
     */
    @RequestMapping(value = "/exportMarketData", method = RequestMethod.GET)
    public void exportCustomerGroupMarketData(HttpServletResponse response, String startTime, String endTime, String marketTaskId) {
        response.setContentType("application/json;charset=utf-8");
        boolean status = marketTaskService.checkMarketTaskPermission(opUser().getCustId(), marketTaskId);
        if (!status) {
            try {
                response.getOutputStream().write(JSON.toJSONString(returnError("权限不足")).getBytes("UTF-8"));
                return;
            } catch (IOException e) {
                LOG.error("导出营销任务成功单异常,", e);
            }
        }
        marketTaskService.exportMarketTaskSuccessToExcel(response, opUser(), marketTaskId, "", "邀约状态", "成功",
                startTime, endTime);
    }

    /**
     * 判断 用户方能否致电，发短信
     *
     * @param marketTaskId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/isvalidSeat", method = RequestMethod.GET)
    @ResponseBody
    public String isValidAccount(String marketTaskId) throws Exception {
        if (StringUtil.isEmpty(marketTaskId)) {
            throw new TouchException("参数错误");
        }
        LOG.info("isValidAccount.marketTaskId::" + marketTaskId);
        LoginUser lu = opUser();
        ResponseCommon response = new ResponseCommon();
        boolean result;
        try {
            result = marketTaskService.isValidAccount(lu, marketTaskId);
        } catch (TouchException e) {
            LOG.error("判断用户方能否致电发短信异常,", e);
            response.setMessage(e.getMessage());
            result = false;
        }
        response.setCode(result ? 1 : -1);
        return returnJsonData(response);
    }

    @RequestMapping(value = "/selectMarketTask", method = RequestMethod.GET)
    @ResponseBody
    public String selectMarketTask(String marketTaskId) {
        if (StringUtil.isEmpty(marketTaskId)) {
            throw new ParamException("marketTaskId必填");
        }
        MarketTaskDTO data = null;
        try {
            // 检查营销任务权限
            boolean status = marketTaskService.checkMarketTaskPermission(opUser().getCustId(), marketTaskId);
            if (!status) {
                return returnError("权限不足");
            }
            data = marketTaskService.selectMarketTask(marketTaskId);
        } catch (Exception e) {
            LOG.error("查询营销任务详情异常,", e);
        }
        return returnJsonData(data);
    }

    @RequestMapping(value = "/xzTaskHandle", method = RequestMethod.POST)
    public void xzTaskHandle(HttpServletResponse response) {
        Map<Object, Object> params = new HashMap<>();
        Map map = request.getParameterMap();
        Set keSet = map.entrySet();
        for (Iterator itr = keSet.iterator(); itr.hasNext(); ) {
            Map.Entry me = (Map.Entry) itr.next();
            Object key = me.getKey();
            Object ov = me.getValue();
            String[] value = new String[1];
            if (ov instanceof String[]) {
                value = (String[]) ov;
            } else {
                value[0] = ov.toString();
            }
            for (int k = 0; k < value.length; k++) {
                params.put(key, value[k]);
            }
        }
        System.out.println("讯众自动外呼任务完成通知请求参数:" + params);
        Map<String, Object> result = new HashMap<>();
        result.put("code", 200);
        result.put("reason", "接收成功");
        try {
            response.setContentType("application/json;charset=utf-8");
            response.getOutputStream().write(JSON.toJSONString(result).getBytes("UTF-8"));
        } catch (IOException e) {
            LOG.error("讯众自动外呼任务完成通知返回处理结果异常,", e);
        }
    }

    /**
     * 导出营销任务成功单录音文件
     *
     * @param response
     * @param startTime
     * @param endTime
     * @param marketTaskId
     */
    @RequestMapping(value = "/exportMarketVoiceData", method = RequestMethod.GET)
    public void exportCustomerGroupMarketVoiceData(HttpServletResponse response, String startTime, String endTime, String marketTaskId) {
        response.setContentType("application/json;charset=utf-8");
        try {
            boolean status = marketTaskService.checkMarketTaskPermission(opUser().getCustId(), marketTaskId);
            if (!status) {
                response.getOutputStream().write(JSON.toJSONString(returnError("权限不足")).getBytes("UTF-8"));
                return;
            }
            marketTaskService.exportMarketTaskSuccessVoice(response, opUser(), marketTaskId, "", "邀约状态", "成功",
                    startTime, endTime);
        } catch (IOException e) {
            LOG.error("导出营销任务成功单录音异常,", e);
        }
    }


    /**
     * 查询讯众自动外呼监控信息
     *
     * @param marketTaskId
     * @return
     */
    @RequestMapping(value = "/getAutoTaskMonitor", method = RequestMethod.GET)
    @ResponseBody
    public String getXzAutoTaskMonitor(String marketTaskId) {
        if (StringUtil.isEmpty(marketTaskId)) {
            throw new ParamException("marketTaskId必填");
        }
        // 普通员工无权限
        if ("2".equals(opUser().getUserType()) && RoleEnum.ROLE_CUSTOMER.equals(opUser().getRole())) {
            return returnError("权限不足");
        }
        XzAutoTaskMonitor data = null;
        try {
            // 检查营销任务权限
            if (RoleEnum.ROLE_CUSTOMER.equals(opUser().getRole())) {
                boolean status = marketTaskService.checkMarketTaskPermission(opUser().getCustId(), marketTaskId);
                if (!status) {
                    return returnError("权限不足");
                }
            }
            data = xzCallCenterService.getXzAutoTaskMonitor(1, marketTaskId);
        } catch (Exception e) {
            LOG.error("营销任务查询讯众自动外呼监控信息异常,", e);
        }
        return returnJsonData(data);
    }

}
