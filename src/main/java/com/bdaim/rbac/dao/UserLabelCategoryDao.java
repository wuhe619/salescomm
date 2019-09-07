package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.UserLabelCategory;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class UserLabelCategoryDao extends SimpleHibernateDao<UserLabelCategory, Serializable> {

}
