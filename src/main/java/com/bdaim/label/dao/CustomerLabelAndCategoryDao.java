package com.bdaim.label.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.CustomerLabelAndCategory;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class CustomerLabelAndCategoryDao extends SimpleHibernateDao<CustomerLabelAndCategory, Serializable> {
}
