package com.bdaim.resource.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.resource.entity.ResourcePropertyDOPK;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import org.springframework.stereotype.Component;

@Component
public class ResourcePropertyDao extends SimpleHibernateDao<ResourcePropertyEntity, ResourcePropertyDOPK> {
}
