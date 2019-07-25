package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.CustomerPropertyEntity;

import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/7
 * @description
 */
@Component
public class CustomerPropertyDao extends SimpleHibernateDao<CustomerPropertyEntity, Serializable>{
}
