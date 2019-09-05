package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.CustomerCallCenterAccount;
import org.springframework.stereotype.Component;


@Component
public class CustomerCallCenterAccountDao extends SimpleHibernateDao<CustomerCallCenterAccount, Long> {

}
