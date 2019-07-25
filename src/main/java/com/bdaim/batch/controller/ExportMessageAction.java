package com.bdaim.batch.controller;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import com.bdaim.batch.service.ExportMessageService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.smscenter.controller.SeatsMessageAction;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;

/**
 * @author duanliying
 * @date 2018/9/11
 * @description
 */
@Controller
@RequestMapping("/export")
public class ExportMessageAction extends BasicAction {
    private static Log logger = LogFactory.getLog(SeatsMessageAction.class);
    @Resource
    ExportMessageService exportMessageService;

    /**
     * @description 失联人员信息导出
     * @author:duanliying
     * @method
     * @date: 2018/9/11 8:56
     */
    @RequestMapping(value = "/lostContactMessage", method = RequestMethod.GET)
    @ResponseBody
    public void ExportPersonMessage(String labelListStr, String batchId, String realname, String enterpriseId, String id, String idCard, String status, HttpServletResponse response) {
        Long userId = opUser().getId();
        String custId = opUser().getCustId();
        String userType = opUser().getUserType();
        String role = opUser().getRole();
        try {
                exportMessageService.exportLostContactMessage(labelListStr, custId, batchId, realname, userId, userType, enterpriseId, id, idCard, status, response,role);
        } catch (Exception e) {
            logger.info("失联人员信息导出" + e.getMessage());
            e.printStackTrace();
        }
    }
}