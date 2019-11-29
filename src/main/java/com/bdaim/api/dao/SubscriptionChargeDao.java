package com.bdaim.api.dao;

import com.bdaim.api.entity.SubscriptionChargeEntity;
import com.bdaim.common.dao.SimpleHibernateDao;
import org.springframework.stereotype.Component;

@Component
public class SubscriptionChargeDao extends SimpleHibernateDao<SubscriptionChargeEntity, Integer> {
}
