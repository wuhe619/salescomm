package com.bdaim.crm.erp.crm.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.annotation.ClassTypeCheck;
import com.bdaim.crm.erp.crm.service.CrmBackLogService;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.*;

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
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R todayCustomer(BasePageRequest basePageRequest){
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
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R followLeads(BasePageRequest basePageRequest){
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
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R followCustomer(BasePageRequest basePageRequest){
        return(crmBackLogService.followCustomer(basePageRequest));
    }

    /**
     *待审核合同
     */
    @RequestMapping(value = "/checkContract", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R checkContract(BasePageRequest basePageRequest){
        return(crmBackLogService.checkContract(basePageRequest));
    }

    /**
     *待审核回款
     */
    @RequestMapping(value = "/checkReceivables", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R checkReceivables(BasePageRequest basePageRequest){
        return(crmBackLogService.checkReceivables(basePageRequest));
    }

    /**
     *待回款提醒
     */
    @RequestMapping(value = "/remindReceivables", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R remindReceivables(BasePageRequest basePageRequest){
        return(crmBackLogService.remindReceivables(basePageRequest));
    }

    /**
     *即将到期的合同
     */
    @RequestMapping(value = "/endContract", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R endContract(BasePageRequest basePageRequest){
        return(crmBackLogService.endContract(basePageRequest));
    }
}
