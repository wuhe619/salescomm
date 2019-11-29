package com.bdaim.api.dao;

import com.bdaim.api.entity.SubscriptionEntity;
import com.bdaim.common.dao.SimpleHibernateDao;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionDao extends SimpleHibernateDao<SubscriptionEntity, Integer> {
}
