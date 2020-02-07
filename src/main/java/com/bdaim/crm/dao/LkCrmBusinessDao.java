package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmBusinessEntity;
import org.springframework.stereotype.Component;

@Component
public class LkCrmBusinessDao extends SimpleHibernateDao<LkCrmBusinessEntity,Integer> {
    public int deleteMember(String memberId, int id) {
        String sql = " update 72crm_crm_business set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where business_id = ?";
        return executeUpdateSQL(sql, memberId, memberId, id);
    }

}
