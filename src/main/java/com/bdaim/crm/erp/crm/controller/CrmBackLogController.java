package com.bdaim.crm.erp.crm.controller;

import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.crm.service.CrmBackLogService;
import com.bdaim.crm.utils.R;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * @author wyq
 */
@RestController
@RequestMapping("/CrmBackLog")
public class CrmBackLogController extends Controller {
    @Resource
    CrmBackLogService crmBackLogService;

    /**
     * 代办事项数量统计
     */
    @ResponseBody
    @RequestMapping(value = "/num", method = RequestMethod.POST)
    public R num(){
        return(crmBackLogService.num());
    }

    /**
     *今日需联系客户
     */
    public void todayCustomer(BasePageRequest basePageRequest){
        renderJson(crmBackLogService.todayCustomer(basePageRequest));
    }

    /**
     * 标记线索为已跟进
     */
    public void setLeadsFollowup(@Para("ids") String ids){
        renderJson(crmBackLogService.setLeadsFollowup(ids));
    }

    /**
     *分配给我的线索
     */
    public void followLeads(BasePageRequest basePageRequest){
        renderJson(crmBackLogService.followLeads(basePageRequest));
    }

    /**
     * 标记客户为已跟进
     */
    public void setCustomerFollowup(@Para("ids") String ids){
        renderJson(crmBackLogService.setCustomerFollowup(ids));
    }

    /**
     *分配给我的客户
     */
    public void followCustomer(BasePageRequest basePageRequest){
        renderJson(crmBackLogService.followCustomer(basePageRequest));
    }

    /**
     *待审核合同
     */
    public void checkContract(BasePageRequest basePageRequest){
        renderJson(crmBackLogService.checkContract(basePageRequest));
    }

    /**
     *待审核回款
     */
    public void checkReceivables(BasePageRequest basePageRequest){
        renderJson(crmBackLogService.checkReceivables(basePageRequest));
    }

    /**
     *待回款提醒
     */
    public void remindReceivables(BasePageRequest basePageRequest){
        renderJson(crmBackLogService.remindReceivables(basePageRequest));
    }

    /**
     *即将到期的合同
     */
    public void endContract(BasePageRequest basePageRequest){
        renderJson(crmBackLogService.endContract(basePageRequest));
    }
}
