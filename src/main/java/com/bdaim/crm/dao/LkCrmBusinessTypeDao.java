package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmBusinessTypeEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmBusinessTypeDao extends SimpleHibernateDao<LkCrmBusinessTypeEntity, Integer> {

    public int deleteBusinessStatus(int typeId) {
        return executeUpdateSQL("  delete from lkcrm_crm_business_status where type_id = ?", typeId);
    }

    public Page queryBusinessTypeList(int page, int limit) {
        String sql = "select a.type_id,a.name,a.create_time,a.dept_ids,a.create_user_id,(select c.username from lkcrm_admin_user c where c.user_id = a.create_user_id) as createName from lkcrm_crm_business_type a";
        return sqlPageQuery(sql, page, limit);
    }

    public Map getBusinessType(String type_id) {
        String sql = "select a.type_id,a.name,a.dept_ids from lkcrm_crm_business_type a where type_id = ?";
        List<Map<String, Object>> maps = sqlQuery(sql, type_id);
        return maps.size() > 0 ? maps.get(0) : null;
    }

    public List<Map<String, Object>> queryBusinessStatus(String type_id) {
        String sql = "select * from lkcrm_crm_business_status where type_id = ? order by order_num";
        List<Map<String, Object>> maps = sqlQuery(sql, type_id);
        return maps;
    }
}
