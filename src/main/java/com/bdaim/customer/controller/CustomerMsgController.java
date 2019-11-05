package com.bdaim.customer.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.service.CustomerMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/customerMsg")
public class CustomerMsgController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(CustomerMsgController.class);

    @Autowired
    private CustomerMsgService customerMsgService;

    @GetMapping("/getCustomerMsgList")
    public ResponseInfo getCustomerMsgList(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null) {
            return new ResponseInfoAssemble().failure(-1, "请填写分页参数");
        }
        ResponseInfo resp = new ResponseInfo();
        resp.setData(customerMsgService.getCustomerMsgList(pageNum,pageSize));
        return resp;
    }

    @GetMapping("/getCustomerMsgList/{id}")
    public ResponseInfo getCustomerMsgById(@PathVariable int id) {
        ResponseInfo resp = new ResponseInfo();
        resp.setData(customerMsgService.getCustomerMsgById(id));
        return resp;
    }


}
