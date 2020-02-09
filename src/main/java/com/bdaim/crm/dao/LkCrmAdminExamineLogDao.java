package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminExamineLogEntity;
import org.springframework.stereotype.Component;

@Component
public class LkCrmAdminExamineLogDao extends SimpleHibernateDao<LkCrmAdminExamineLogEntity, Integer> {
    public int updateExamineLogIsRecheckByRecordId(Integer record_id) {
        return super.executeUpdateSQL(" UPDATE 72crm_admin_examine_log SET is_recheck = 1 WHERE record_id = ?", record_id);
    }
}
