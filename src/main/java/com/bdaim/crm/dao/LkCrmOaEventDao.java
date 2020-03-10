package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmOaEventEntity;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmOaEventDao extends SimpleHibernateDao<LkCrmOaEventEntity, Integer> {

    public Page queryEventRelation(int pageNum, int pageSize, String businessIds, String contactsIds, String contractIds, String customerIds) {
        String sql = " select a.*,b.eventrelation_id, b.customer_ids, b.contacts_ids, b.business_ids, b.contract_ids, c.realname,'' AS img,GROUP_CONCAT(d.realname) as 'owner_user_name' " +
                "    from lkcrm_oa_event as a inner join lkcrm_oa_event_relation as b on a.event_id = b.event_id " +
                "    left join t_customer_user as c on a.create_user_id = c.id " +
                "    left join t_customer_user as d on FIND_IN_SET(d.id,IFNULL(a.owner_user_ids, 0)) " +
                "    where 1=2 ";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(businessIds)) {
            param.add(businessIds);
            sql += " or b.business_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contactsIds)) {
            param.add(contactsIds);
            sql += " or b.contacts_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contractIds)) {
            param.add(contractIds);
            sql += " or b.contract_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(customerIds)) {
            param.add(customerIds);
            sql += " or b.customer_ids like concat('%,',?,',%')";
        }
        sql += " group by a.event_id,b.eventrelation_id";
        return super.sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public Map<String, Object> queryById(Integer actionId) {
        String sql = "    select a.*,b.*,c.realname,GROUP_CONCAT(d.realname) as 'owner_user_name' " +
                "    from lkcrm_oa_event as a left join lkcrm_oa_event_relation as b on a.event_id = b.event_id " +
                "    left join lkcrm_admin_user as c on a.create_user_id = c.user_id " +
                "    left join lkcrm_admin_user as d on FIND_IN_SET(d.user_id,IFNULL(a.owner_user_ids, 0)) " +
                "    where a.event_id = ?";
        return super.queryUniqueSql(sql, actionId);
    }

    public List<Map<String, Object>> queryList(Date endTime, Date startTime, Long userId) {
        String sql = "select a.*, b.eventrelation_id,b.customer_ids,b.contacts_ids,b.business_ids, b.contract_ids, b.status,c.realname,GROUP_CONCAT(d.realname) as 'owner_user_name' " +
                "    from lkcrm_oa_event as a left join lkcrm_oa_event_relation as b on a.event_id = b.event_id " +
                "    left join lkcrm_admin_user as c on a.create_user_id = c.user_id " +
                "    left join lkcrm_admin_user as d on FIND_IN_SET(d.user_id,IFNULL(a.owner_user_ids, 0)) " +
                "    where start_time < ? and end_time > ? and (a.create_user_id = ? or a.owner_user_ids like " +
                "    CONCAT('%',?,'%')) group by a.event_id,b.eventrelation_id ";
        return super.sqlQuery(sql, endTime, startTime, userId, userId);

    }
}
