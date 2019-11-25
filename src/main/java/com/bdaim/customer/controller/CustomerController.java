package com.bdaim.customer.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.service.CustomerAppService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.validation.Valid;

@RestController
@RequestMapping("/customer")
public class CustomerController extends BasicAction {

    private static Logger logger = LoggerFactory.getLogger(CustomerController.class);
    @Resource
    CustomerAppService customerAppService;

    /**
     * @Title: regist
     * @Description: 保存企业客户
     */
    @RequestMapping(value = "/info/{custId}", method = RequestMethod.POST)
    public ResponseInfo info(@RequestBody CustomerRegistDTO customerRegistDTO, @PathVariable(name = "custId") String id) {
        try {
            LoginUser lu = opUser();
            if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
                //前台创建的createId是当前登陆企业id
                customerRegistDTO.setCreateId(lu.getCustId());
            }
            customerRegistDTO.setCustId(id);
            String code = customerAppService.registerOrUpdateCustomer(customerRegistDTO, lu);
            if ("001".equals(code)) {
                return new ResponseInfoAssemble().failure(-1, customerRegistDTO.getName() + "账号已经存在");
            }
            return new ResponseInfoAssemble().success(null);
        } catch (Exception e) {
            logger.error("创建企业信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "创建企业失败");
        }

    }

    @PostMapping("/infos")
    public ResponseInfo queryList(@Valid PageParam page, String account, String name, String contactPerson, String salePerson) {
        ResponseInfo resp = new ResponseInfo();
        String customerId = opUser().getCustId();
        customerAppService.getUser(page, customerId, account, name, contactPerson, salePerson);
        return resp;
    }

}
