package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminExamineLogEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminExamineLogDao extends SimpleHibernateDao<LkCrmAdminExamineLogEntity, Integer> {
    public int updateExamineLogIsRecheckByRecordId(Integer record_id) {
        return super.executeUpdateSQL(" UPDATE lkcrm_admin_examine_log SET is_recheck = 1 WHERE record_id = ?", record_id);
    }

    public List<Map<String,Object>> queryUserByUserId(Long userId) {
        String sql = " SELECT DISTINCT saud.user_id, saud.realname , 0 as examine_status from lkcrm_admin_user as sau\n" +
                "    LEFT JOIN lkcrm_admin_user as saud on saud.user_id = sau.parent_id WHERE sau.user_id = ?";
        return sqlQuery(sql, userId);
    }
}
