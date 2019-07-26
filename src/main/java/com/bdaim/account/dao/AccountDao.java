package com.bdaim.account.dao;


import com.bdaim.account.entity.AccountDO;
import com.bdaim.common.dao.SimpleHibernateDao;

import org.springframework.stereotype.Component;

/**
 */
@Component
public class AccountDao extends SimpleHibernateDao<AccountDO,String>{
}
