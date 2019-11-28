package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.AmApplicationEntity;
import org.springframework.stereotype.Component;

@Component
public class AmApplicationDao extends SimpleHibernateDao<AmApplicationEntity, Integer> {
}
