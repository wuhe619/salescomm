package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmLeadsEntity;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
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

    public int updateOwnerUserId(String ownerUserId, List<String> ids) {
        String sql = "update lkcrm_crm_leads " +
                "    set owner_user_id = ?,followup = 0 " +
                "    where leads_id in (?) ";
        int maps = this.executeUpdateSQL(sql, ownerUserId, SqlAppendUtil.sqlAppendWhereIn(ids));
        return maps;
    }

    public List queryBatchIdByIds(List<String> ids) {
        String sql = "select batch_id from lkcrm_crm_leads where leads_id in (?)";
        return this.sqlQuery(sql, SqlAppendUtil.sqlAppendWhereIn(ids));
    }

    public int deleteByIds(List<String> ids) {
        String sql = "delete from lkcrm_crm_leads where leads_id in (?)";
        int maps = this.executeUpdateSQL(sql, SqlAppendUtil.sqlAppendWhereIn(ids));
        return maps;
    }

    public int setLeadsFollowup(List<String> ids) {
        String sql = "update lkcrm_crm_leads set followup = 1 where leads_id in (?)";
        int maps = this.executeUpdateSQL(sql, SqlAppendUtil.sqlAppendWhereIn(ids));
        return maps;
    }

    public Page pageCluePublicSea(int pageNum, int pageSize, long seaId, String search) {
        StringBuffer conditions = new StringBuffer("SELECT a.*,z.* FROM t_customer_sea_list_" + seaId + " AS a LEFT JOIN fieldleadsview AS z ON a.id = z.field_batch_id WHERE 1=1 ");
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(search)) {
            param.add(search);
            param.add(search);
            param.add(search);
            param.add(search);
            conditions.append(" and (super_name like '%?%' or super_telphone like '%?%' or super_phone like '%?%' or super_data like '%?%')");
        }
        return sqlPageQuery(conditions.toString(), pageNum, pageSize, param.toArray());

    }
}
