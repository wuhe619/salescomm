package com.bdaim.crm.erp.crm.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseCommon;
import com.bdaim.common.exception.TouchException;
import com.bdaim.crm.erp.crm.service.CrmCommonService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author chengning@salescomm.net
 * @description 工具controller
 * @date 2020/4/13 9:43
 */
@RestController
@RequestMapping("/crm/common")
public class CrmCommonController extends BasicAction {

    public static final Logger LOG = LoggerFactory.getLogger(CrmCommonController.class);

    @Autowired
    private CrmCommonService crmCommonService;

    @PostMapping(value = "/call/validSeat")
    public ResponseCommon validSeat() {
        LoginUser lu = opUser();
        ResponseCommon response = new ResponseCommon();
        boolean result;
        try {
            result = crmCommonService.isValidAccount(lu);
        } catch (TouchException e) {
            LOG.error("CRM判断用户方能否致电发短信异常,", e);
            response.setMessage(e.getMessage());
            result = false;
        }
        response.setCode(result ? 1 : -1);
        response.setMsg(response.getMessage());
        return response;
    }
}
