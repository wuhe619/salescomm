package com.bdaim.crm.erp.crm.service;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * @author chengning@salescomm.net
 * @description TODO
 * @date 2020/4/13 9:45
 */
@Service
@Transactional
public class CrmCommonService {

    @Autowired
    private MarketResourceService marketResourceService;
    @Autowired
    private CustomerUserDao customerUserDao;

    /**
     * 判断用户能否致电
     * 1.判断余额
     */
    public Boolean isValidAccount(LoginUser lu) throws TouchException {
        boolean has_remain = marketResourceService.judRemainAmount(lu.getCustId());
        if (!has_remain && "2".equals(lu.getUserType())) {
            throw new TouchException("余额不足,请先充值");
        }
        if ("2".equals(lu.getUserType())) {
            CustomerUserPropertyDO call_channel = customerUserDao.getProperty(lu.getId().toString(), "call_channel");
            if (call_channel == null || StringUtil.isEmpty(call_channel.getPropertyValue())) {
                throw new TouchException("坐席未配置外呼参数");
            }
        }
        return true;
    }
}
