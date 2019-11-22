package com.bdaim.customer.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.service.CustomerAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/customer")
public class CustomerController extends BasicAction {

    private static Logger logger = LoggerFactory.getLogger(CustomerController.class);
    @Resource
    CustomerAppService customerAppService;

    /**
     * @Title: regist
     * @Description: 企业管理 创建客户
     */
    @RequestMapping(value = "/info/{id}", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public ResponseInfo regist(@RequestBody CustomerRegistDTO customerRegistDTO, @PathVariable(name = "id") String id) {
        try {
            LoginUser lu = opUser();
            if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
                //前台创建的createId是当前登陆企业id
                customerRegistDTO.setCreateId(lu.getCustId());
            }
            String code = customerAppService.registerOrUpdateCustomer(customerRegistDTO, id, lu);
            if ("001".equals(code)) {
                return new ResponseInfoAssemble().failure(-1, customerRegistDTO.getName() + "账号已经存在");
            }
            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            logger.error("创建企业信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "创建企业失败");
        }

    }

}
