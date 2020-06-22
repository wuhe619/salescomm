package com.bdaim.rbac.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.rbac.dto.RegisterDTO;
import com.bdaim.rbac.service.RegisterService;
import com.bdaim.smscenter.service.SendSmsService;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册
 */
@Controller
@RequestMapping("/registerUser")
public class RegisterAction extends BasicAction {
    private static Log log = LogFactory.getLog(RegisterAction.class);

    @Resource
    private RegisterService registerService;

    @Resource
    private SendSmsService sendSmsService;

    /**
     * 注册用户
     *
     * @param userName
     * @param passWord
     * @param phone
     * @param vcode
     * @return
     */
    @RequestMapping(value = "/saveNew", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String registerUser(String userName, String passWord, String phone, String vCode) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        //错误提示
        map.put("UserNameError", "");
        map.put("PhoneError", "");
        map.put("SmsError", "");
        String countName = registerService.validationUserName(userName);
        if (countName.equals("1")) {
            map.put("UserNameError", "1");
        }
        String countPhone = registerService.validationPhone(phone);
        if (countPhone.equals("1")) {
            map.put("PhoneError", "1");
        }
        int theCode = sendSmsService.verificationCode(phone, 1, vCode);
        if (theCode == -1) {
            map.put("SmsError", theCode);
        }
        if (theCode == 0) {
            map.put("SmsError", theCode);
        }
        if (theCode == 2) {
            map.put("SmsError", theCode);
        }
        if (countName.equals("0") && countPhone.equals("0") && theCode == 1) {
            // 没问题可以增加新用户
            RegisterDTO registerDTO = new RegisterDTO();
            registerDTO.setUserName(userName);
            registerDTO.setPassword(passWord);
            registerDTO.setMobile(phone);
            registerService.saveNewUser(registerDTO);
            map.put("code", "0");
            map.put("message", "注册成功");
        } else {
            map.put("code", "1");
            map.put("message", "注册失败");
        }
        json.put("data", map);
        return json.toString();
    }

    /**
     * 验证用户名是否存在
     *
     * @param userName
     * @return
     */
    @RequestMapping(value = "/validationUserName", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String validationUserName(String userName) {
        JSONObject json = new JSONObject();
        Map<String, String> map = new HashMap<String, String>();
        // 不为0是存在
        String count = registerService.validationUserName(userName);
        if (count.equals("0")) {
            map.put("code", "0");
            map.put("message", "用户名可用");
        } else {
            map.put("code", "1");
            map.put("message", "用户名已存在");
        }
        json.put("data", map);
        return json.toString();
    }

    /**
     * 验证手机号是否存在 发送验证码
     *
     * @param phone
     * @return
     */
    @RequestMapping(value = "/validationPhone", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String validationPhone(String phone) {
        JSONObject json = new JSONObject();
        Map<String, String> map = new HashMap<>();
        String count = registerService.validationPhone(phone);
        if (count.equals("0")) {
            // 发送短信
            sendSmsService.sendSmsVcCodeByCommChinaAPI(phone, 1, "");
            map.put("code", "0");
            map.put("message", "已发送");
        } else {
            map.put("code", "1");
            map.put("message", "手机号已存在");
        }
        json.put("data", map);
        return json.toString();
    }

    /**
     * 验证验证码是否正确
     *
     * @param vCode
     * @return
     */
    @RequestMapping(value = "/validationCode", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String validationCode(String vCode, String phone) {
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        //* return -1：系统错误；0：验证码错误了；1：验证码正确；2：过期了
        int code = sendSmsService.verificationCode(phone, 1, vCode);
        if (code == -1) {
            map.put("code", code);
            map.put("message", "系统错误");
        }
        if (code == 0) {
            map.put("code", code);
            map.put("message", "验证码错误了");
        }
        if (code == 1) {
            map.put("code", code);
            map.put("message", "验证码正确");
        }
        if (code == 2) {
            map.put("code", code);
            map.put("message", "验证码过期了");
        }
        json.put("data", map);
        return json.toJSONString();
    }

    /**
     * 企业信息添加
     *
     * @param registerDTO
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/registCustomer", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public Object regist(RegisterDTO registerDTO) throws Exception {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        String cust_id = opUser().getCustId();
        Long user_id = opUser().getId();
        registerService.CustomerRegist(registerDTO, cust_id, user_id);
        resultMap.put("code", "0");
        resultMap.put("_message", "客户创建成功");
        resultMap.put("data", new JSONArray());
        return JSONObject.toJSON(resultMap);
    }

    /**
     * 企业认证状态查询
     *
     * @param user_id
     * @return
     */
    @RequestMapping(value = "/getCustomerStatus", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String getCustomerStatus() {
        Long user_id = opUser().getId();
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        List<Map<String, Object>> list = registerService.getCustomerStatus(user_id.toString());
        map.put("date", list);
        map.put("user_id", String.valueOf(user_id));
        json.put("data", map);
        return json.toJSONString();
    }
}
