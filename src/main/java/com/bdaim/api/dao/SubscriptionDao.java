package com.bdaim.api.dao;

import com.bdaim.api.entity.SubscriptionEntity;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.AmApplicationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SubscriptionDao extends SimpleHibernateDao<SubscriptionEntity, Integer> {

    public SubscriptionEntity getById(int apiId, int applicationId) {
        SubscriptionEntity cp = null;
        String hql = "from SubscriptionEntity m where m.apiId=? and m.applicationId=?";
        List<SubscriptionEntity> list = this.find(hql, apiId, applicationId);
        if (list.size() > 0)
            cp = (SubscriptionEntity) list.get(0);
        return cp;
    }
}
