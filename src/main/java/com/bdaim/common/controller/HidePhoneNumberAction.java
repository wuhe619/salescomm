package com.bdaim.common.controller;

import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.service.api.HidePhoneNumberImpl;
import com.bdaim.common.util.LogUtil;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Controller
@RequestMapping("/number")
public class HidePhoneNumberAction {

    @Resource
    private HidePhoneNumberImpl hidephoneNumber;


    /**
     * 通过id获取手机号码
     * @param request
     * @param response
     */
    @RequestMapping(value = "/getPhoneNumberById",method = RequestMethod.GET)
    @CacheAnnotation
    public void getPhoneNumberById(HttpServletRequest request, HttpServletResponse response) {
        String data = hidephoneNumber.getPhoneNumberById(request);
        response.setCharacterEncoding("utf-8");
        LogUtil.info("HidePhoneNumberImpl.getPhoneNumberById: resonse="+data);
        try {
            response.getWriter().write(data);
        }catch (Exception e){
            e.printStackTrace();
        }
    }
}
