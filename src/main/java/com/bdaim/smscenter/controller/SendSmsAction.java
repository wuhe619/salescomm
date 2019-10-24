package com.bdaim.smscenter.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.bdaim.callcenter.dto.SeatPropertyDTO;
import com.bdaim.callcenter.dto.SeatsInfo;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.smscenter.dto.CallBackSmsDTO;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.util.StringUtil;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发送短息Action请求
 * 2017/2/21
 */
@Controller
@RequestMapping("/sms")
public class SendSmsAction extends BasicAction {

    private final static Logger LOG = Logger.getLogger(SendSmsAction.class);

    @Resource
    private SendSmsService sendSmsService;

    /**
     * 短信信息编辑发送验证码
     */
    @RequestMapping(value = "/sendSms", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public Object sendSmsVcode(@RequestBody JSONObject jsonObject) throws Exception {
        String phone = jsonObject.getString("phone");
        int type = Integer.parseInt(jsonObject.getString("state"));
        String username = jsonObject.getString("username");
        Object result = sendSmsService.sendSmsVcCodeByCommChinaAPI(phone, type, username);
        return JSONObject.toJSON(result);

    }

    /**
     * @description 云讯短信回调
     * @author:duanliying
     * @method
     * @date: 2018/10/8 14:03
     */
    @RequestMapping(value = "/ytxCallbackSms", method = RequestMethod.POST)
    @ResponseBody
    public Object ytxCallbackSms(HttpServletRequest request) throws IOException {
        BufferedReader reader = request.getReader();
        char[] buf = new char[512];
        int len = 0;
        StringBuffer contentBuffer = new StringBuffer();
        while ((len = reader.read(buf)) != -1) {
            contentBuffer.append(buf, 0, len);
        }
        String content = contentBuffer.toString();
        if (content == null) {
            content = "";
        }
        LOG.info("云讯短信回调参数:" + content);
        LOG.info("云讯短信回调类型:" + request.getQueryString());

        String method = request.getQueryString();
        int updateNum = 0;
        // 只处理短信回调
        if (StringUtil.isNotEmpty(method) && "SMS".equals(method)) {
            List<CallBackSmsDTO> dto = JSON.parseArray(content, CallBackSmsDTO.class);
            for (CallBackSmsDTO d : dto) {
                updateNum += sendSmsService.ytxCallbackSms(d);
            }
        }
        Map<String, Object> resultMap = new HashMap<>();
        if (updateNum > 0) {
            resultMap.put("result", 1);
            resultMap.put("_message", "回调成功！");
        }
        if (updateNum == 0) {
            resultMap.put("result", -1);
            resultMap.put("_message", "回调失败！");
        }
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description zw短信推送接口
     * @author:duanliying
     * @method
     * @date: 2018/12/5 19:15
     */
    @RequestMapping(value = "/getSmsStatus", method = RequestMethod.POST)
    public void getCallBackInfo(String report, HttpServletResponse response) {
        LOG.info("获取短信发送状态推送结果,推送数据是" + report);
        //report = "21298921,18241890199,DELIVRD,2019-05-31 18:28:06^21297914,18241890199,DELIVRD,2019-05-31 18:28:06";
        //处理返回数据进行DB操作
        Map<Object, Object> data = new HashMap<>();
        try {
            sendSmsService.insertSmsLogBackInfo(report);
            response.getOutputStream().write(JSON.toJSONString(data, SerializerFeature.WriteMapNullValue).getBytes("UTF-8"));
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("获取筑望短信发送状态推送结果异常", e);
        }
    }


    public static void main(String[] args) {
        JSONObject json = new JSONObject();
        SeatPropertyDTO customerPropertyDTO = new SeatPropertyDTO();
        ArrayList<SeatsInfo> list = new ArrayList<>();

        //讯众
        //已经添加到800
        //String seatsPassword = "aa123456";
        String extensionPassword = "aa123456";
        //String extensionNumber = "821008";
        //String custId = "1808280511280195";
        //新方
        //xf_xzcc
        //已经添加到500
        //int seatsAccount = 1550;
        String extensionNumber = "821005";
        //String custId = "1810260157380004";
        //xf_xzcc1
        //已经添加到600
        //int seatsAccount = 2700;
        // String extensionNumber = "821008";
        // String custId = "1810300535182106";
        //xf_xzcc2
        //已经添加到3350
        //String extensionNumber = "820052";
        //String custId = "1811060140598497";
        //xf_xzcc2
        //已经添加到3300
        //String extensionNumber = "821008";
        //String custId = "1807310926310004";

        //xinqidian
        //已经添加到005
        //String extensionNumber = "";
        // String custId = "1811151038454829";
        //String seatsPassword = "123456";

        //机器人
        //已经添加到005
        //String extensionNumber = "";
        String custId = "1812050529120004";
        String seatsPassword = "xunzhong";

        //yunxun
        //已经添加到003
        // String extensionNumber = "820260";
        // String custId = "1812050529120004";
        int startStr = 0;
        int number = 1;
        int seatsAccount = 002;

        for (int i = startStr; i < number; i++) {
            SeatsInfo seatsInfo = new SeatsInfo();
            seatsInfo.setSeatsAccount(String.valueOf(seatsAccount));
            seatsInfo.setSeatsPassword(seatsPassword);
            seatsInfo.setExtensionNumber(extensionNumber + String.valueOf(seatsAccount));
            seatsInfo.setExtensionPassword(extensionPassword);
            list.add(seatsInfo);
            ++seatsAccount;
        }
        customerPropertyDTO.setSeatsInfoList(list);
        customerPropertyDTO.setCustId(custId);
        System.out.println(list.size());
        System.out.println(JSON.toJSONString(customerPropertyDTO));
        System.out.println(new BigDecimal(0));
    }

}
