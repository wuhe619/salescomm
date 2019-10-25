package com.bdaim.customer.account.dao;


import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.account.entity.AccountDO;

import org.springframework.stereotype.Component;

/**
 */
@Component
public class AccountDao extends SimpleHibernateDao<AccountDO,String>{
}
