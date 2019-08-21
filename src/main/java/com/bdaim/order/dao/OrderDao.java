package com.bdaim.order.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.order.entity.OrderDO;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class OrderDao extends SimpleHibernateDao<OrderDO, Serializable> {

}
