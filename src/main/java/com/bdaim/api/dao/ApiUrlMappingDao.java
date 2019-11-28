package com.bdaim.api.dao;

import com.bdaim.api.entity.ApiUrlMappingEntity;
import com.bdaim.common.dao.SimpleHibernateDao;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiUrlMappingDao extends SimpleHibernateDao<ApiUrlMappingEntity, Integer> {

    public ApiUrlMappingEntity getApiUrlMapping(int id) {
        String hql = "from ApiUrlMappingEntity m where m.id=? ";
        List<ApiUrlMappingEntity> list = this.find(hql, id);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

}
