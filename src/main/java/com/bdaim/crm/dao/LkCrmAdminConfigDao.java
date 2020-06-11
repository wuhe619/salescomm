package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminConfigEntity;
import com.bdaim.crm.utils.R;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmAdminConfigDao extends SimpleHibernateDao<LkCrmAdminConfigEntity, Integer> {

    public LkCrmAdminConfigEntity get(String name, String custId) {
        List<LkCrmAdminConfigEntity> customerRule = this.find("FROM LkCrmAdminConfigEntity where name = ? and cust_id = ?", name, custId);
        if (customerRule.size() > 0) {
            return customerRule.get(0);
        }
        return null;
    }
}
