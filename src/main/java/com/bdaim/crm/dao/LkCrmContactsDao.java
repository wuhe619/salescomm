package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmContactsEntity;
import org.springframework.stereotype.Component;

@Component
public class LkCrmContactsDao extends SimpleHibernateDao<LkCrmContactsEntity, Integer> {
}
