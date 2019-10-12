package com.bdaim.customeruser.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.wechat.WeChatUtil;
import com.bdaim.customer.controller.CustomerAction;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customeruser.service.CustomerUserService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Map;

/*
 * C端用户
 */
@Controller
@RequestMapping("/custuser")
public class CustomerUserAction extends BasicAction {
    private static Logger logger = Logger.getLogger(CustomerAction.class);
    @Resource
    CustomerUserService customerUserService;
    @Resource
    WeChatUtil weChatUtil;

    /**
     * @description 自定义显示字段列
     * @author:duanliying
     * @method
     * @date: 2019/7/9 10:08
     */
    @RequestMapping(value = "/updateShowRow", method = RequestMethod.POST)
    @ResponseBody
    public Object updateShowRowByUser(String id, String showRow) {
        ResponseJson responseJson = new ResponseJson();
        if (showRow == null || id == null) {
            responseJson.setMessage("参数错误");
            responseJson.setCode(-1);
            return JSON.toJSONString(responseJson);
        }
        LoginUser lu = opUser();
        Long userId = lu.getId();
        logger.info("操作用户是：" + id);
        try {
            customerUserService.updateShowRowByUser(id, userId, showRow);
            responseJson.setCode(200);
            return JSON.toJSONString(responseJson);
        } catch (Exception e) {
            logger.error("自定义显示字段列保存异常:", e);
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }


    /**
     * @description 查询客户公海已选字段
     * @author:duanliying
     * @method
     * @date: 2019/7/9 10:08
     */
    @RequestMapping(value = "/getShowRow", method = RequestMethod.GET)
    @ResponseBody
    public Object getShowRowByUser(String id) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser lu = opUser();
        Long userId = lu.getId();
        logger.info("操作用户是：" + userId);
        try {
            Map<String, Object> showRowByUser = customerUserService.getShowRowByUser(id, userId);
            responseJson.setData(showRowByUser);
            responseJson.setCode(200);
            return JSON.toJSONString(responseJson);
        } catch (Exception e) {
            logger.error("查询客户公海已选字段:", e);
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }


    @RequestMapping(value = "/bindUserOpenId", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo bindUserOpenId(@RequestBody JSONObject body) {
        ResponseInfo responseJson = new ResponseInfo();
        String username = body.getString("username");
        String password = body.getString("password");
        String code = body.getString("code");
        if (StringUtil.isEmpty(username) || StringUtil.isEmpty(password)) {
            responseJson.setCode(0);
            responseJson.setMessage("用户名密码错误");
            return responseJson;
        }
        //校验用户名密码
        CustomerUser u = customerUserService.getUserByName(username);
        String md5Password = CipherUtil.generatePassword(password);
        if (u == null || !md5Password.equals(u.getPassword())) {
            responseJson.setCode(0);
            responseJson.setMessage("用户名密码错误");
            return responseJson;
        }
        //获取微信用户openid
        String openId = weChatUtil.getWeChatOpenId(code);
        if (StringUtil.isEmpty(openId)) {
            responseJson.setCode(0);
            responseJson.setMessage("绑定失败");
            return responseJson;
        }
        CustomerUserPropertyDO propertyDO = new CustomerUserPropertyDO();
        propertyDO.setUserId(String.valueOf(u.getId()));
        propertyDO.setPropertyValue(openId);
        propertyDO.setPropertyName("openid");
        customerUserService.saveCustomerUserProperty(propertyDO);
        responseJson.setCode(1);
        responseJson.setMessage("绑定成功");
        return responseJson;

    }

    /**
     * 保存或更新用户属性
     *
     * @param body
     * @return
     */
    @RequestMapping(value = "/saveProperty", method = RequestMethod.POST)
    @ResponseBody
    public Object saveCustomerUserProperty(@RequestBody CustomerUserPropertyDO body) {
        ResponseJson responseJson = new ResponseJson();
        body.setUserId(String.valueOf(opUser().getId()));
        try {
            customerUserService.saveCustomerUserProperty(body);
            responseJson.setCode(200);
        } catch (Exception e) {
            logger.error("保存或更新用户属性异常:", e);
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }
}