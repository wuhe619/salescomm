package com.bdaim.customer.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.service.CustomerMsgService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/customerMsg")
public class CustomerMsgController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(CustomerMsgController.class);

    @Autowired
    private CustomerMsgService customerMsgService;

    /**
     * 查看列表
     * @param pageNum
     * @param pageSize
     * @return
     */
    @GetMapping("/getCustomerMsgList")
    public ResponseInfo getCustomerMsgList(Integer pageNum, Integer pageSize) {
        if (pageNum == null || pageSize == null) {
            return new ResponseInfoAssemble().failure(-1, "请填写分页参数");
        }
        ResponseInfo resp = new ResponseInfo();
        resp.setData(customerMsgService.getCustomerMsgList(pageNum, pageSize));
        return resp;
    }

    @GetMapping("/getCustomerMsgList/{id}")
    public ResponseInfo getCustomerMsgById(@PathVariable int id) {
        ResponseInfo resp = new ResponseInfo();
        resp.setData(customerMsgService.getCustomerMsgById(id));
        return resp;
    }

    /**
     * 修改level
     * @return
     */
    @PutMapping("/update/level")
    public ResponseInfo updateCustomerMsgLevel() {
        ResponseInfo resp = new ResponseInfo();
        resp.setData(customerMsgService.updateCustomerMsg());
        return resp;
    }


}
