package com.bdaim.customer.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.dto.Deposit;
import com.bdaim.customer.service.CustomerAppService;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

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
    public ResponseInfo info(@RequestBody CustomerRegistDTO customerRegistDTO, @PathVariable(name = "custId") Long id) {
        ResponseInfo resp = new ResponseInfo();
        try {
            LoginUser lu = opUser();
            if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
                //前台创建的createId是当前登陆企业id
                customerRegistDTO.setCreateId(lu.getCustId());
            }
            if (0L != id) {
                customerRegistDTO.setCustId(String.valueOf(id));
            }
            String code = customerAppService.registerOrUpdateCustomer(customerRegistDTO, lu);
            if ("001".equals(code)) {
                return new ResponseInfoAssemble().failure(-1, customerRegistDTO.getName() + "账号已经存在");
            }
            resp.setData(code);
            return resp;
        } catch (Exception e) {
            logger.error("创建企业信息异常", e);
            return new ResponseInfoAssemble().failure(-1, "创建企业失败");
        }

    }

    /**
     * 查询企业客户列表
     *
     * @return
     */
    @PostMapping("/infos")
    public ResponseInfo queryList(@RequestBody(required = false) String body) {
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
        page.setPageSize(info.getInteger("pageSize") == null ? 0 : info.getIntValue("pageSize"));
        page.setPageNum(info.getInteger("pageNum") == null ? 10 : info.getIntValue("pageNum"));
        resp.setData(customerAppService.getUser(page, info.getString("customerId"), info.getString("account"), info.getString("name"), info.getString("contactPerson"), info.getString("salePerson")));
        return resp;
    }

    @DeleteMapping("/info/{custId}")
    public ResponseInfo delete(@PathVariable(name = "custId") String id) throws Exception {
        ResponseInfo resp = new ResponseInfo();
        if (StringUtil.isEmpty(id)) {
            return new ResponseInfoAssemble().failure(-1, "企业id为空");
        }
        resp.setData(customerAppService.delCust(Long.valueOf(id)));
        return resp;
    }

    /**
     * @Title: regist
     * @Description: 查看企业客户详情
     */
    @GetMapping(value = "/info/{custId}")
    public ResponseInfo query(@PathVariable(name = "custId") String id) {
        ResponseInfo resp = new ResponseInfo();
        try {
            if (StringUtil.isEmpty(id) || "0".equals(id)) {
                return new ResponseInfoAssemble().failure(-1, "企业id错误");
            }
            resp.setData(customerAppService.queryByCust(id));
            return resp;
        } catch (Exception e) {
            logger.error("企业查询失败", e);
            return new ResponseInfoAssemble().failure(-1, "企业查询失败");
        }

    }
    
    
    @GetMapping(value = "/info/{customerId}/app")
    @ResponseBody
    public List app(@PathVariable(name = "customerId") String customerId) throws Exception{
    	LoginUser lu = opUser();
    	if(lu==null || lu.getAuths()==null || !lu.getAuths().contains("admin"))
    		throw new Exception("no auth");
    		
    	List data = this.customerAppService.apps(customerId);
    	
        return data;
    }
    
    @PostMapping(value = "/info/{customerId}/app/{appId}/token")
    @ResponseBody
    public Map newAppToken(@PathVariable(name = "customerId") String customerId,@PathVariable(name = "appId") String appId) throws Exception{
    	LoginUser lu = opUser();
    	if(lu==null || lu.getAuths()==null || !lu.getAuths().contains("admin"))
    		throw new Exception("no auth");
    	if(customerId==null || "".equals(customerId))
    		throw new Exception("no customer");
    	if(appId==null || "".equals(appId))
    		throw new Exception("no app");
    	
    	Map	app = customerAppService.getApp(appId);
    	if(app==null)
    		throw new Exception("error app");
    	if(!customerId.equals(app.get("customerId")))
    		throw new Exception("error customer and app");
    	
        String newToken = customerAppService.reAppToken(appId);
        app.put("token", newToken);
    	
        return app;
    }

    @PostMapping("/deposit/{custId}")
    public ResponseInfo saveDeposit(@RequestBody @Valid Deposit deposit, @PathVariable(name = "custId") String id) {
        ResponseInfo resp = new ResponseInfo();
        String user_id = opUser().getUser_id();
        if (StringUtil.isEmpty(id) || "0".equals(id)) {
            return new ResponseInfoAssemble().failure(-1, "企业id错误");
        }
        resp.setData(customerAppService.saveDeposit(deposit, id, user_id));

        return resp;
    }

    @PostMapping("/deposits")
    public ResponseInfo deposits(@RequestBody(required = false) String body) {
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
        if (StringUtil.isEmpty(info.getString("custId"))) {
            return new ResponseInfoAssemble().failure(-1, "企业id错误");
        }
        page.setPageSize(info.getInteger("pageSize") == null ? 0 : info.getIntValue("pageSize"));
        page.setPageNum(info.getInteger("pageNum") == null ? 10 : info.getIntValue("pageNum"));
        resp.setData(customerAppService.depositList(page, info.get("custId").toString()));
        return resp;
    }

}
