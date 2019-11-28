package com.bdaim.customer.user.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.user.service.NewCustomerUserService;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("customeruser")
public class CustomerUserController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(CustomerUserController.class);

    @Autowired
    private NewCustomerUserService customerUserService;

    @PostMapping("/info/{uId}")
    public ResponseInfo updateCustomerUser(@RequestBody CustomerRegistDTO dto, @PathVariable(name = "uId") String id) throws Exception {
        try {
            LoginUser lu = opUser();
            if (StringUtil.isEmpty(dto.getCustId())) {
                return new ResponseInfoAssemble().failure(-1, "企业用户id不能为空");
            }
            dto.setUserId(id);
            int i = customerUserService.updateCustomerUser(dto, lu);
            if (i == -1) {
                return new ResponseInfoAssemble().failure(-1, dto.getName() + "账号已经存在");
            }
            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            logger.error("创建企业信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "创建企业失败");
        }
    }

    @GetMapping("/info/{uId}")
    public ResponseInfo getByCustomerUser(@PathVariable(name = "uId") String id) {
        ResponseInfo resp = new ResponseInfo();
        if (StringUtil.isEmpty(id) || "0".equals(id)) {
            return new ResponseInfoAssemble().failure(-1, "企业用户id不能为空");
        }
        try {
            resp.setData(customerUserService.getByUserId(id));
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "查询失败");
        }
        return resp;
    }

    @PostMapping("/info")
    public ResponseInfo getCustomerUserList(@RequestBody(required = false) String body) {
        PageParam page = new PageParam();
        ResponseInfo resp = new ResponseInfo();
        JSONObject info = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";

            info = JSONObject.parseObject(body);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "记录解析异常:");
        }
        try {
            page.setPageSize(info.getInteger("pageSize") == null ? 0 : info.getIntValue("pageSize"));
            page.setPageNum(info.getInteger("pageNum") == null ? 10 : info.getIntValue("pageNum"));
            resp.setData(customerUserService.getCustomerUserList(page, info.getString("custId"), info));
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "查询失败");
        }
        return resp;
    }

    @DeleteMapping("/info/{uId}")
    public ResponseInfo delCustomerUserById(@RequestBody(required = false) String body, @PathVariable(name = "uId") String id) {
        JSONObject info = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";

            info = JSONObject.parseObject(body);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "记录解析异常:");
        }
        if (StringUtil.isEmpty(info.getString("custId")) || "0".equals(info.getString("custId"))) {
            return new ResponseInfoAssemble().failure(-1, "企业id错误");
        }
        ResponseInfo resp = new ResponseInfo();
        try {
            resp.setData(customerUserService.delCustomerUserById(info.getString("custId"), id));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseInfoAssemble().failure(-1, "删除失败，用户不存在");
        }
        return resp;
    }
}
