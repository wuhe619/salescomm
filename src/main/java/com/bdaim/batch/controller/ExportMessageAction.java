package com.bdaim.batch.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.batch.service.ExportMessageService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.smscenter.controller.SeatsMessageAction;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    private static Logger logger = LoggerFactory.getLogger(SeatsMessageAction.class);
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

    /**
     * @description 批次详情信息导出
     * @author:duanliying
     * @method
     * @date: 2018/9/11 8:56
     */
    @RequestMapping(value = "/detail", method = RequestMethod.GET)
    @ResponseBody
    public ResponseInfo ExportPersonMessage(String batchId, String detailId, Integer status, String exportType, HttpServletResponse response) {
        try {
            if (StringUtil.isEmpty(batchId) || StringUtil.isEmpty(exportType)){
                return new ResponseInfoAssemble().failure(-1, "缺少必要参数");
            }
            String custId = "";
            LoginUser lu = opUser();
            if ("ROLE_CUSTOMER".equals(lu.getRole())) {
                custId = lu.getCustId();
            }
            exportMessageService.exportDetailInfo(batchId, detailId, status, response, exportType,custId);
        } catch (Exception e) {
            logger.info("失联人员信息导出", e);
            return new ResponseInfoAssemble().failure(-1, "批次详情信息导出失败");
        }
        return new ResponseInfoAssemble().success("批次详情信息导出成功");
    }
}