package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminFieldvEntity;
import org.springframework.stereotype.Component;

@Component
public class LkCrmAdminFieldvDao extends SimpleHibernateDao<LkCrmAdminFieldvEntity, Integer> {

    public int deleteByBatchId(String batch_id){
        String sql = "DELETE FROM lkcrm_admin_fieldv WHERE batch_id = ? ";
        return executeUpdateSQL(sql, batch_id);
    }
}
