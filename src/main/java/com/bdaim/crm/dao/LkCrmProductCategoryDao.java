package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmProductCategoryEntity;
import org.springframework.stereotype.Component;

@Component
public class LkCrmProductCategoryDao extends SimpleHibernateDao<LkCrmProductCategoryEntity, Integer> {

    public int queryByCategoryId(int category_id) {
        String sql = "select count(*) from lkcrm_crm_product where category_id = ?";
        return queryForInt(sql, category_id);
    }

    public int queryCategoryByParentId(int pid) {
        String sql = " select count(*) from lkcrm_crm_product_category where pid = ?";
        return queryForInt(sql, pid);
    }
}
