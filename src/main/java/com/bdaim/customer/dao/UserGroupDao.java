package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.CustomerUserGroup;
import com.bdaim.util.StringUtil;
import com.bdaim.common.dto.Page;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/27
 * @description
 */
@Component
public class UserGroupDao extends SimpleHibernateDao<CustomerUserGroup, Serializable> {

    public Page listUserGroup(int pageNum, int pageSize, CustomerUserGroup userGroupEntity) {
        List<Object> params = new ArrayList<>();
        params.add(userGroupEntity.getCustId());
        StringBuilder sql = new StringBuilder();
        sql.append("FROM CustomerUserGroup t WHERE t.`status` = 1 AND t.custId = ? ");
        if (StringUtil.isNotEmpty(userGroupEntity.getId())) {
            sql.append(" AND t.id = ? ");
            params.add(userGroupEntity.getId());
        }
        if (StringUtil.isNotEmpty(userGroupEntity.getName())) {
            sql.append(" AND t.name = ? ");
            params.add(userGroupEntity.getName());
        }
        return this.page(sql.toString(), params, pageNum, pageSize);
    }

    public CustomerUserGroup get(Long id){
        return this.get(id);
    }
}
