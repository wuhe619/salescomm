package com.bdaim.order.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customgroup.entity.CustomGroupDO;
import com.bdaim.order.entity.OrderDO;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class OrderDao extends SimpleHibernateDao<OrderDO, Serializable> {

    public CustomGroupDO getCusomGroup(String orderId) {
        CustomGroupDO cp = null;
        String hql = "from CustomGroupDO m where m.orderId=?";
        List<CustomGroupDO> list = this.find(hql, orderId);
        if (list.size() > 0)
            cp = list.get(0);

        return cp;
    }
}
