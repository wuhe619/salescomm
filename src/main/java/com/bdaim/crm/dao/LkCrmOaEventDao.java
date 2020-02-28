package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmOaEventEntity;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class LkCrmOaEventDao extends SimpleHibernateDao<LkCrmOaEventEntity,Integer> {

    public Page queryEventRelation(int pageNum, int pageSize, String businessIds, String contactsIds, String contractIds, String customerIds) {
        String sql = " select a.*,b.eventrelation_id, b.customer_ids, b.contacts_ids, b.business_ids, b.contract_ids, b.status, b.create_time, c.realname,'' AS img,GROUP_CONCAT(d.realname) as 'owner_user_name'\n" +
                "    from lkcrm_oa_event as a inner join lkcrm_oa_event_relation as b on a.event_id = b.event_id\n" +
                "    left join t_customer_user as c on a.create_user_id = c.id\n" +
                "    left join t_customer_user as d on FIND_IN_SET(d.id,IFNULL(a.owner_user_ids, 0))\n" +
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
        return super.sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }
}
