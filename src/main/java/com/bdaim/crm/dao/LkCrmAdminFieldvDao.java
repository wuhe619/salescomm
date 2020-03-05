package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminFieldvEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminFieldvDao extends SimpleHibernateDao<LkCrmAdminFieldvEntity, Integer> {

    public int deleteByBatchId(String batch_id) {
        String sql = "DELETE FROM lkcrm_admin_fieldv WHERE batch_id = ? ";
        return executeUpdateSQL(sql, batch_id);
    }

    public List<Map<String, Object>> queryCustomField(String batchId) {
        String sql = "select a.name,a.value,a.field_id,b.type from lkcrm_admin_fieldv as a left join lkcrm_admin_field as b on a.field_id = b.field_id where batch_id = ?";
        return sqlQuery(sql, batchId);
    }

    public List<LkCrmAdminFieldvEntity> listByBatchId(String batch_id) {
        String sql = "from LkCrmAdminFieldvEntity where batchId = ?";
        return find(sql, batch_id);
    }
}
