package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmProductEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmProductDao extends SimpleHibernateDao<LkCrmProductEntity, Integer> {

    public List<Map<String, Object>> querySubtotalByBusinessId(int business_id) {
        String sql = "   select distinct ccc.discount_rate,\n" +
                "      round((( select SUM(subtotal) from 72crm_crm_business_product WHERE business_id = ccc.business_id ) /100*(100 - ccc.discount_rate) ),2)as money\n" +
                "      from 72crm_crm_business as ccc\n" +
                "      where ccc.business_id = ?";
        return sqlQuery(sql, business_id);
    }
}
