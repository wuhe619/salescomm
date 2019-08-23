package com.bdaim.smscenter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.dto.SeatsInfo;
import com.bdaim.callcenter.dto.SeatsMessageParam;
import com.bdaim.callcenter.service.SeatsService;
import com.bdaim.callcenter.service.impl.SeatsServiceImpl;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.CustomerUserPropertyDO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying  坐席信息管理
 * @date 2018/9/19
 * @description
 */
@Controller
@RequestMapping("/seats")
public class SeatsMessageAction extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(SeatsMessageAction.class);
    @Resource
    SeatsService seatsService;
    @Resource
    SeatsServiceImpl seatsServiceImpl;

    /**
     * @description 配置渠道信息（企业中心id 外显号）
     * @author:duanliying
     * @method
     * @date: 2018/9/19 17:51
     */
    @RequestMapping(value = "/updateSeatsMain.do", method = RequestMethod.POST)
    @ResponseBody
    public Object updateMainMessage(@RequestBody List<SeatsMessageParam> seatsList) {
        Map<String, String> resultMap = new HashMap<String, String>();
        LoginUser lu = opUser();
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                seatsService.updateMainMessage(seatsList);
                resultMap.put("result", "1");
                resultMap.put("_message", "更新成功！");
            }
        } catch (Exception e) {
            logger.info("修改渠道信息" + e.getMessage());
            resultMap.put("result", "0");
            resultMap.put("_message", "更新失败！");
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description 添加坐席信息
     * @author:duanliying
     * @method
     * @date: 2018/9/19 18:50
     */
    @RequestMapping(value = "/addSeatsMessage.do", method = RequestMethod.POST)
    @ResponseBody
    public Object addSeatsMessage(@RequestBody SeatsMessageParam seatsMessageParam) {
        Map<String, String> resultMap = null;
        LoginUser lu = opUser();
        Long accountUserId = opUser().getId();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            try {
                resultMap = seatsServiceImpl.addSeatsList(seatsMessageParam, accountUserId);
            } catch (Exception e) {
                logger.error("批量添加坐席异常" + e);
                resultMap.put("result", "0");
                resultMap.put("_message", "批量添加坐席异常");
            }
        } else {
            resultMap.put("result", "0");
            resultMap.put("_message", "您暂时没有权限,请联系管理员");
        }

        return JSONObject.toJSON(resultMap);
    }


    /**
     * @description 添加坐席时验证账号不可重复添加
     * @author:duanliying
     * @method
     * @date: 2018/11/2 13:59
     */
    @RequestMapping(value = "/chenkAccount.do", method = RequestMethod.GET)
    @ResponseBody
    public Object updateSeatsMessage(String account, String seatId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        int code = seatsService.checkAccount(account, seatId);
        if (code == 0) {
            resultMap.put("result", code);
        }
        if (code == 1) {
            resultMap.put("result", code);
            resultMap.put("message", account + "账号已经存在");
        }
        if (code == 2) {
            resultMap.put("result", code);
            resultMap.put("message", seatId + "账号与登陆账号不可以重复");
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description 单独编辑坐席
     * @author:duanliying
     * @method
     * @date: 2018/9/27 13:41
     */
    @RequestMapping(value = "/updateSeatMessage.do", method = RequestMethod.POST)
    @ResponseBody
    public Object updateSeatsMessage(@RequestBody SeatsInfo SeatsInfo) {
        Map<String, String> resultMap = new HashMap<String, String>();
        int codeNum = 0;
        LoginUser lu = opUser();
        //type=0 是后台用户可以修改坐席密码  1 是前台用户  不可以修改坐席密码   只可以修改主叫号码
        try {
            codeNum = seatsService.updateSeatMessage(SeatsInfo);
            if (codeNum > 0) {
                logger.info("修改坐席信息个数是" + codeNum);
                resultMap.put("result", "1");
                resultMap.put("_message", "编辑成功！");
            } else {
                logger.info("修改坐席信息个数是" + codeNum);
                resultMap.put("result", "0");
                resultMap.put("_message", "编辑失败！");
            }

        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改坐席信息异常" + e);
            resultMap.put("result", "0");
            resultMap.put("_message", "编辑失败！");
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description 修改平台信息
     * @author:duanliying
     * @method
     * @date: 2018/9/27 14:46
     */
    @RequestMapping(value = "/updatePlatform.do", method = RequestMethod.POST)
    @ResponseBody
    public Object updatePlatform(@RequestBody SeatsInfo SeatsInfo) {
        Map<String, String> resultMap = new HashMap<String, String>();
        String code = seatsService.updatePlatformMessage(SeatsInfo);
        if ("1".equals(code)) {
            resultMap.put("result", "1");
            resultMap.put("_message", "修改成功");
        }
        if ("3".equals(code)) {
            resultMap.put("result", "3");
            resultMap.put("_message", "修改平台信息失败");
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description 修改坐席有效状态
     * @author:duanliying
     * @method
     * @date: 2018/9/21 10:49
     */
    @RequestMapping(value = "/updateUserStstus.do", method = RequestMethod.GET)
    @ResponseBody
    public Object updateUserStstus(int status, String userId, String channel, String custId) {
        Map<String, String> resultMap = new HashMap<String, String>();
        LoginUser lu = opUser();
        try {
            if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
                seatsService.updateSeatsStatus(userId, status, channel, custId);
                resultMap.put("result", "1");
                resultMap.put("message", "坐席状态修改成功");
            }
        } catch (Exception e) {
            logger.error("坐席主信息信息添加" + e);
            resultMap.put("result", "0");
            resultMap.put("message", "坐席状态修改失败");
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description 配置坐席展示列表
     * @author:duanliying
     * @method
     * @date: 2018/9/21 10:49
     */
    @ResponseBody
    @RequestMapping(value = "/getUserList.do", method = RequestMethod.GET)
    public Object getUserMessageList(String custId, Integer pageNum, Integer pageSize) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        List<SeatsMessageParam> seatsList;
        Map<String, Object> seatsMessage = null;
        LoginUser lu = opUser();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            seatsMessage = seatsService.getSeatsMessage(custId, pageNum, pageSize);
        }
        if (seatsMessage.size() > 0) {
            resultMap.put("seatsList", seatsMessage);
            resultMap.put("result", "1");
            resultMap.put("total", seatsMessage.get("total"));
            resultMap.put("_message", "获取坐席信息列表成功！");
        } else {
            resultMap.put("result", "0");
            resultMap.put("_message", "获取坐席信息列表失败！");
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description 企业坐席列表
     * @author:duanliying
     * @method
     * @date: 2018/10/19 10:30
     */
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    public Object queryCustomer(@Valid PageParam page, BindingResult error, CustomerRegistDTO customerRegistDTO) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        Page list = null;
        list = seatsService.getCustomerInfo(page, customerRegistDTO);
        return JSON.toJSONString(list);
    }

    /**
     * @description 查询配置渠道的配置信息
     * @author:duanliying
     * @method
     * @date: 2018/10/22 14:37
     */
    @RequestMapping(value = "/searchChannel.do", method = RequestMethod.GET)
    @ResponseBody
    public Object searchChannelList(String custId) {
        return JSONObject.toJSON(seatsService.getChannelList(custId));
    }

    /**
     * @description 查询外显号码根据批次ID和企业ID
     * @author:duanliying
     * @method
     * @date: 2018/10/24 12:04
     */
    @ResponseBody
    @RequestMapping(value = "/queryApparent", method = RequestMethod.GET)
    public Object queryApparent(String batchId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        //获取当前企业id
        String cust_id = opUser().getCustId();
        Map map = seatsService.getApparentNum(batchId, cust_id);
        if (map.size() > 0) {
            resultMap.put("data", map);
            resultMap.put("result", "1");
            resultMap.put("_message", "查询成功！");
        } else {
            resultMap.put("result", "0");
            resultMap.put("_message", "查询失败！");
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description 设置外显号根据batchId
     * @author:duanliying
     * @method
     * @date: 2018/10/24 12:02
     */
    @ResponseBody
    @RequestMapping(value = "/updateApparent", method = RequestMethod.GET)
    public String installExtension(String batchId, String apparentNums) {
        Map<String, Object> map = new HashMap<>(16);
        try {
            seatsService.updateApparentNum(batchId, apparentNums);
            map.put("result", 1);
            map.put("message", "修改外显号码成功");
        } catch (Exception e) {
            logger.error("修改外显号码失败" + e);
            map.put("result", 0);
            map.put("message", "修改外显号码失败");
        }
        return JSON.toJSONString(map);
    }

    @ResponseBody
    @RequestMapping(value = "/getUserAllProperty", method = RequestMethod.GET)
    public String getUserAllProperty(String userId) {
        Map<String, Object> map = new HashMap<>(16);
        List<CustomerUserPropertyDO> list = seatsService.getUserAllProperty(userId);
        map.put("list", list);
        return JSON.toJSONString(map);
    }

    /**
     * @description 主叫号码查询接口（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/20 15:53
     */
    @ResponseBody
    @RequestMapping(value = "/open/getExtensionNum.do", method = RequestMethod.POST)
    public String getExtensionNum(@RequestBody JSONObject param) {
        String seatAccount = param.getString("seatAccount");
        String channel = param.getString("channel");
        Map<Object, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        String cust_id = opUser().getCustId();
        if (StringUtil.isEmpty(seatAccount) || StringUtil.isEmpty(channel)) {
            map.put("msg", "请求参数异常");
            map.put("code", "004");
            json.put("data", map);
            return json.toJSONString();
        }
        Map<String, String> resultMap = seatsService.getExtensionNum(seatAccount, cust_id, channel);
        return JSON.toJSONString(resultMap);
    }


    @RequestMapping(value = "/updateCallerID.do", method = RequestMethod.POST)
    @ResponseBody
    public Object updateCallerID(@RequestBody SeatsMessageParam seatsMessageParam) {
        Map<String, String> resultMap = new HashMap<String, String>();
        int codeNum = 0;
        LoginUser lu = opUser();
        //type=0 是后台用户可以修改坐席密码  1 是前台用户  不可以修改坐席密码   只可以修改主叫号码
        try {
            String custId = lu.getCustId();
            codeNum = seatsService.updateCallerID(seatsMessageParam, custId);
            logger.info("修改坐席信息个数是" + codeNum);
            resultMap.put("result", "1");
            resultMap.put("_message", "编辑成功！");
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("修改坐席信息异常" + e);
            resultMap.put("result", "0");
            resultMap.put("_message", "编辑失败！");
        }
        return JSONObject.toJSON(resultMap);
    }


    /**
     * @description 单独添加坐席信息
     */
    @RequestMapping(value = "/addSeat", method = RequestMethod.POST)
    @ResponseBody
    public Object addSeat(@RequestBody SeatsInfo seatsInfo) {
        Map<String, String> resultMap = new HashMap<>();
        LoginUser lu = opUser();
        Long accountUserId = opUser().getId();
        if ("ROLE_USER".equals(lu.getRole()) || "admin".equals(lu.getRole())) {
            try {
                resultMap = seatsServiceImpl.addSeatMessage(seatsInfo, accountUserId);
                return JSONObject.toJSON(resultMap);
            } catch (Exception e) {
                e.printStackTrace();
                resultMap.put("result", "0");
                resultMap.put("_message", "坐席添加失败");
            }
        } else {
            resultMap.put("result", "0");
            resultMap.put("_message", "您暂时没有权限,请联系管理员");
        }

        return JSONObject.toJSON(resultMap);
    }
}
