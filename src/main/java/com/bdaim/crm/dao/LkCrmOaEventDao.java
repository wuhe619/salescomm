package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmOaEventEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
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
                "    where 1=2 AND a.cust_id = ? ";
        List param = new ArrayList();
        param.add(BaseUtil.getCustId());
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
//        String sql = "    select a.*,b.*,c.realname,GROUP_CONCAT(d.realname) as 'owner_user_name' " +
//                "    from lkcrm_oa_event as a left join lkcrm_oa_event_relation as b on a.event_id = b.event_id " +
//                "    left join lkcrm_admin_user as c on a.create_user_id = c.user_id " +
//                "    left join lkcrm_admin_user as d on FIND_IN_SET(d.user_id,IFNULL(a.owner_user_ids, 0)) " +
//                "    where a.event_id = ?";
        String sql = "SELECT " +
                " a.event_id,a.cust_id,a.title,a.content,a.start_time,a.end_time,a.create_user_id, " +
                " a.create_time,a.update_time,a.type,a.owner_user_ids,a.address,a.remark,a.color, " +
                " a.remind_type,b.eventrelation_id,b.customer_ids,b.contacts_ids,b.business_ids, " +
                " b.contract_ids,b.status,c.realname,GROUP_CONCAT( d.realname ) AS 'owner_user_name'  " +
                "FROM " +
                " lkcrm_oa_event AS a " +
                " LEFT JOIN lkcrm_oa_event_relation AS b ON a.event_id = b.event_id " +
                " LEFT JOIN lkcrm_admin_user AS c ON a.create_user_id = c.user_id " +
                " LEFT JOIN lkcrm_admin_user AS d ON FIND_IN_SET( d.user_id, IFNULL( a.owner_user_ids, 0 ) ) "+
                " WHERE a.event_id = ?";
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

    public List<Map<String, Object>> queryOwnerList(List userIds) {
        String sql = " select user_id, username,img, create_time, realname, num, mobile, email, sex, dept_id, post from lkcrm_admin_user " +
                "    where user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")";
        return super.sqlQuery(sql);
    }

    public List<Map<String, Object>> queryCustomerList(List ids) {
        String sql = "  select * from lkcrm_crm_customer where customer_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ")";
        return super.sqlQuery(sql);
    }

    public List<Map<String, Object>> queryContactsList(List userIds) {
        String sql = " select * from lkcrm_crm_contacts where contacts_id in  (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")";
        return super.sqlQuery(sql);
    }

    public List<Map<String, Object>> queryBusinessList(List userIds) {
        String sql = "  select * from lkcrm_crm_business where business_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")";
        return super.sqlQuery(sql);
    }

    public List<Map<String, Object>> queryContractList(List userIds) {
        String sql = "  select * from lkcrm_crm_contract where contract_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")";
        return super.sqlQuery(sql);
    }
}
