package com.bdaim.api.dao;

import com.bdaim.api.entity.ApiEntity;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.supplier.entity.SupplierEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class ApiDao extends SimpleHibernateDao<ApiEntity, Integer> {

    public ApiEntity getApi(int apiId) {
        String hql = "from ApiEntity m where m.apiId=? AND m.status = 0";
        List<ApiEntity> list = this.find(hql, apiId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

}
