package com.bdaim.crm.erp.crm.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.CrmRequestBody;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.crm.service.CrmBackLogService;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 代办事项
 */
@RestController
@RequestMapping("/CrmBackLog")
public class CrmBackLogController extends BasicAction {
    @Resource
    CrmBackLogService crmBackLogService;

    /**
     * 代办事项数量统计
     */
    @RequestMapping(value = "/num", method = RequestMethod.POST)
    public R num(){
        return(crmBackLogService.num());
    }

    /**
     *今日需联系客户
     */
    @RequestMapping(value = "/todayCustomer", method = RequestMethod.POST)
    public R todayCustomer(BasePageRequest basePageRequest, @CrmRequestBody JSONObject jsonObject){
        basePageRequest.setJsonObject(jsonObject);
        return(crmBackLogService.todayCustomer(basePageRequest));
    }

    /**
     * 标记线索为已跟进
     */
    @RequestMapping(value = "/setLeadsFollowup", method = RequestMethod.POST)
    public R setLeadsFollowup(@RequestParam("ids") String ids){
        return(crmBackLogService.setLeadsFollowup(ids));
    }

    /**
     *分配给我的线索
     */
    @RequestMapping(value = "/followLeads", method = RequestMethod.POST)
    public R followLeads(@CrmRequestBody BasePageRequest basePageRequest, @CrmRequestBody JSONObject jsonObject){
        basePageRequest.setJsonObject(jsonObject);
        return(crmBackLogService.followLeads(basePageRequest));
    }

    /**
     * 标记客户为已跟进
     */
    @RequestMapping(value = "/setCustomerFollowup", method = RequestMethod.POST)
    public R setCustomerFollowup(@RequestParam("ids") String ids){
        return(crmBackLogService.setCustomerFollowup(ids));
    }

    /**
     *分配给我的客户
     */
    @RequestMapping(value = "/followCustomer", method = RequestMethod.POST)
    public R followCustomer(BasePageRequest basePageRequest){
        return(crmBackLogService.followCustomer(basePageRequest));
    }

    /**
     *待审核合同
     */
    @RequestMapping(value = "/checkContract", method = RequestMethod.POST)
    public R checkContract(@CrmRequestBody BasePageRequest basePageRequest, @CrmRequestBody JSONObject jsonObject){
        basePageRequest.setJsonObject(jsonObject);
        return(crmBackLogService.checkContract(basePageRequest));
    }

    /**
     *待审核回款
     */
    @RequestMapping(value = "/checkReceivables", method = RequestMethod.POST)
    public R checkReceivables(@CrmRequestBody BasePageRequest basePageRequest, @CrmRequestBody JSONObject jsonObject){
        basePageRequest.setJsonObject(jsonObject);
        return(crmBackLogService.checkReceivables(basePageRequest));
    }

    /**
     *待回款提醒
     */
    @RequestMapping(value = "/remindReceivables", method = RequestMethod.POST)
    public R remindReceivables(@CrmRequestBody BasePageRequest basePageRequest, @CrmRequestBody JSONObject jsonObject){
        basePageRequest.setJsonObject(jsonObject);
        return(crmBackLogService.remindReceivables(basePageRequest));
    }

    /**
     *即将到期的合同
     */
    @RequestMapping(value = "/endContract", method = RequestMethod.POST)
    public R endContract(@CrmRequestBody BasePageRequest basePageRequest, @CrmRequestBody JSONObject jsonObject){
        basePageRequest.setJsonObject(jsonObject);
        return(crmBackLogService.endContract(basePageRequest));
    }
}
