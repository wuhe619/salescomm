package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.AmApplicationEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class AmApplicationDao extends SimpleHibernateDao<AmApplicationEntity, Integer> {
    public AmApplicationEntity getByCustId(String subscriberId) {
        AmApplicationEntity cp = null;
        String hql = "from AmApplicationEntity m where m.subscriberId=? ";
        List<AmApplicationEntity> list = this.find(hql, Long.valueOf(subscriberId));
        if (list.size() > 0)
            cp = (AmApplicationEntity) list.get(0);
        return cp;
    }

}
