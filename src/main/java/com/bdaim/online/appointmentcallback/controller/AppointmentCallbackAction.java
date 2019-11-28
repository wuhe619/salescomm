package com.bdaim.online.appointmentcallback.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.online.appointmentcallback.dto.AppointmentCallbackQueryParam;
import com.bdaim.online.appointmentcallback.service.AppointmentCallbackService;
import com.bdaim.online.appointmentcallback.vo.AppointmentCallbackVO;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseCommon;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.resource.service.MarketResourceService;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/12
 * @description
 */
@RestController
@RequestMapping("/appointmentCallback")
public class AppointmentCallbackAction extends BasicAction {

    @Resource
    private AppointmentCallbackService appointmentCallbackService;
    @Resource
    private MarketResourceService marketResourceService;

    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public ResponseCommon save(@RequestBody AppointmentCallbackVO model) {
        ResponseCommon responseCommon = new ResponseCommon();
        model.setCustId(opUser().getCustId());
        model.setOperator(String.valueOf(opUser().getId()));
        // 判断当前客户群是否存在该身份ID
        Map<String, Object> data = marketResourceService.getSuperData(model.getSuperid(), String.valueOf(model.getCustomerGroupId()));
        if (data == null) {
            responseCommon.setCode(-2);
            responseCommon.setMessage("用户不存在");
            return responseCommon;
        }
        int result = appointmentCallbackService.save(model);
        if (result == 1) {
            return responseCommon.success();
        }
        return responseCommon.fail();
    }

    @RequestMapping(value = "/pageList", method = RequestMethod.POST)
    public ResponseJson pageList(@Valid PageParam pageParam, BindingResult error, AppointmentCallbackQueryParam model) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        model.setCustId(opUser().getCustId());
        model.setPageNum(pageParam.getPageNum());
        model.setPageSize(pageParam.getPageSize());
        //权限相关操作
        model.setUserId(String.valueOf(opUser().getId()));
        model.setUserType(opUser().getUserType());
        model.setUserGroupId(opUser().getUserGroupId());
        model.setUserGroupRole(opUser().getUserGroupRole());
        Page page = appointmentCallbackService.pageList(model);
        responseJson.setData(getPageData(page));
        return responseJson;
    }


    @RequestMapping(value = "/updateCallbackState", method = RequestMethod.POST)
    public ResponseCommon updateCallbackState(@RequestBody JSONObject jsonObject) {
        ResponseCommon responseCommon = new ResponseCommon();
        int code = 0;
        if (jsonObject != null) {
            String id = jsonObject.getString("id");
            int status = jsonObject.getIntValue("status");
            // 更新预约回拨状态
            code = appointmentCallbackService.updateCallbackState(id, status);
        }
        if (code == 0) {
            return responseCommon.fail();
        }
        return responseCommon.success();
    }

}
