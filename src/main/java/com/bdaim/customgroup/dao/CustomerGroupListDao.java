package com.bdaim.customgroup.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customgroup.entity.CustomerGroupListDO;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomerGroupListDao extends SimpleHibernateDao<CustomerGroupListDO, Serializable> {

}
