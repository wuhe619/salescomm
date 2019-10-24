package com.bdaim.open.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.util.spring.SpringContextHelper;
import com.bdaim.util.LogUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 精准营销相关接口
 *
 * @author chengning@salescomm.net
 * @date 2018/7/31
 * @description
 */
@Controller
@RequestMapping(value = "/api/sales")
public class SaleAction extends BasicAction {

    private static Map<String, String> map = new HashMap<>();


    static {
        // 提供给中科点击客户的接口
        map.put("CustomGroupService", "com.bdaim.common.service.api.CustomGroupImpl");
        map.put("ImportDataService", "com.bdaim.common.service.api.ImportDataImpl");
        map.put("CallRecordService", "com.bdaim.common.service.api.CallRecordImpl");
        // 提供给京东-尚德客户的接口
        map.put("JdSdCustomGroupService", "com.bdaim.common.service.api.JdSdCustomGroupServiceImpl");
        map.put("JdSdImportDataService", "com.bdaim.common.service.api.JdSdImportDataImpl");
        map.put("JdSdCallRecordService", "com.bdaim.common.service.api.JdSdCallRecordImpl");

        map.put("ImportEncryptionDataService", "com.bdaim.common.service.api.ImportEncryptionDataImpl");
        map.put("CreateCustomGroupService", "com.bdaim.common.service.api.CustomGroupServiceImpl");

    }

    @RequestMapping("rest")
    public void getLabelData(HttpServletRequest request, HttpServletResponse response) {
        try {
            LogUtil.warn(formatParameter(request).toString());
            String host = request.getRemoteHost();
            LogUtil.warn("当前访问host:" + host);
            String returnData = "";

            //字符编码设置只对Post提交的请求生效，所以Get请求接收中文时可能会乱码，但是考虑到要传输terms大字符串，发送方式应该是必选Post
            request.setCharacterEncoding("utf-8");
            String interfaceID = request.getParameter("interfaceID");

            //验证interfaceID
            if (interfaceID == null) {
                LogUtil.warn("未获取到interfaceID");
            } else if (map.get(interfaceID) == null) {
                LogUtil.warn("无效的interfaceID");
            } else {
                LogUtil.info("成功调用模块：" + interfaceID);
                try {
                    Class<?> serviceClass = Class.forName(map.get(interfaceID));
                    Method serviceMethod = serviceClass.getMethod("execute", HttpServletRequest.class);
                    returnData = (String) serviceMethod.invoke(SpringContextHelper.getBean(interfaceID), request);
                } catch (Exception e) {
                    LogUtil.error("对外接口处理失败,", e);
                }
            }
            returnData = returnData.equals("") ? "{\"isSuccess\":0,\"message\":\"失败\"}" : returnData;
            responseWrite(response, returnData);
        } catch (Exception e) {
            LogUtil.error("对外接口处理失败,", e);
        }
        responseWrite(response, "{\"isSuccess\":0,\"message\":\"失败\"}");
    }

    public Map<String, String> formatParameter(HttpServletRequest request) {
        Map<String, String> map = new HashMap<String, String>();
        Enumeration enu = request.getParameterNames();

        while (enu.hasMoreElements()) {
            String paraName = (String) enu.nextElement();
            String paraValue = request.getParameter(paraName);
            map.put(paraName, paraValue);
        }

        return map;
    }

    //输出文本
    protected void responseWrite(HttpServletResponse response, String content) {
        try {
            response.setCharacterEncoding("utf-8");
            response.setHeader("Content-type", "text/html;charset=utf-8");
            PrintWriter printWriter = response.getWriter();
            printWriter.print(content);
            printWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
