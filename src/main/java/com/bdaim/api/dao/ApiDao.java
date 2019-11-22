package com.bdaim.api.dao;

import com.bdaim.api.entity.ApiEntity;
import com.bdaim.common.dao.SimpleHibernateDao;
import org.springframework.stereotype.Component;

@Component
public class ApiDao extends SimpleHibernateDao<ApiEntity, Integer> {
}
