package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmCustomerEntity;
import org.springframework.stereotype.Component;

@Component
public class LkCrmCustomerDao extends SimpleHibernateDao<LkCrmCustomerEntity, Integer> {

    public int todayCustomerNum(String userId) {
        String sql = "select count(*) from lkcrm_crm_customer\n" +
                "  where customer_id not in (IFNULL((select GROUP_CONCAT(types_id) from lkcrm_admin_record where types = 'crm_customer' and to_days(create_time) = to_days(now())),0))\n" +
                "  and to_days(next_time) = to_days(now()) and owner_user_id = ?";
        return queryForInt(sql, userId);
    }

    public int followLeadsNum(String userId) {
        String sql = "select count(*) from lkcrm_crm_leads as a\n" +
                "  where followup = 0\n" +
                "  and is_transform = 0  and owner_user_id = ?";
        return queryForInt(sql, userId);
    }

    public int followCustomerNum(String userId) {
        String sql = "select count(*) from lkcrm_crm_customer as a\n" +
                "  where followup = 0\n" +
                "  and owner_user_id = ?";
        return queryForInt(sql, userId);
    }

    public int checkReceivablesNum(String userId) {
        String sql = "select count(*)\n" +
                "  from lkcrm_crm_receivables as a inner join lkcrm_admin_examine_record as b on a.examine_record_id = b.record_id\n" +
                "  left join lkcrm_admin_examine_log as c on b.record_id = c.record_id\n" +
                "  where c.examine_user = ? and a.check_status in (0,1) and ifnull(b.examine_step_id, 1) = ifnull(c.examine_step_id, 1) and c.is_recheck != 1";
        return queryForInt(sql, userId);
    }

    public int remindReceivablesPlanNum(String userId) {
        String sql = "select count(*)\n" +
                "  from lkcrm_crm_receivables_plan as a inner join lkcrm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "  inner join lkcrm_crm_contract as c on a.contract_id = c.contract_id\n" +
                "  where to_days(a.return_date) >= to_days(now()) and to_days(a.return_date) <= to_days(now())+a.remind\n" +
                "  and receivables_id is null and c.owner_user_id = ?";
        return queryForInt(sql, userId);
    }

    public int endContractNum(String value, String userId) {
        String sql = "select count(*) from lkcrm_crm_contract as a inner join lkcrm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "  where to_days(a.end_time) >= to_days(now()) and to_days(a.end_time) <= to_days(now()) + IFNULL(?,0) and a.owner_user_id = ?";
        return queryForInt(sql, value, userId);
    }

    public int checkContractNum(String userId) {
        String sql = "select count(*)\n" +
                "  from lkcrm_crm_contract as a inner join lkcrm_admin_examine_record as b on a.examine_record_id = b.record_id\n" +
                "  left join lkcrm_admin_examine_log as c on b.record_id = c.record_id\n" +
                "  where c.examine_user = ? and a.check_status in (0,1) and ifnull(b.examine_step_id, 1) = ifnull(c.examine_step_id, 1) and c.is_recheck != 1";
        return queryForInt(sql, userId);
    }
}
