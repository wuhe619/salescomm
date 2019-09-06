package com.bdaim.rbac.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.rbac.entity.User;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * 
 */
@Component
public class RegisterDao extends SimpleHibernateDao<User,Serializable> {
}
