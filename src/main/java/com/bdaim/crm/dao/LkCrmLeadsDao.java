package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmLeadsEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmLeadsDao extends SimpleHibernateDao<LkCrmLeadsEntity, Integer> {

    public List getRecord(int leadsId) {
        String sql = "select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id " +
                "    from lkcrm_admin_record as a inner join lkcrm_admin_user as b " +
                "    where a.create_user_id = b.user_id and types = 'crm_leads' and types_id = ? order by a.create_time desc";
        return this.sqlQuery(sql, leadsId);
    }

    public Map queryById(int leadsId) {
        String sql = "select *,leads_name as name from leadsview where leads_id = ? ";
        List<Map<String, Object>> maps = this.sqlQuery(sql, leadsId);
        if (maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }

}
