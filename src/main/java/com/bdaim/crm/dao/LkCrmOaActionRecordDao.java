package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmOaActionRecordEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmOaActionRecordDao extends SimpleHibernateDao<LkCrmOaActionRecordEntity, Integer> {

    public List<Map<String, Object>> queryList(List userIds, long userId, int deptId) {
        List param = new ArrayList();
        param.add(userId);
        param.add(deptId);
        param.add(userId);
        StringBuffer sql = new StringBuffer(" select a.log_id,a.action_id,a.content as action_content,a.create_time,a.type,a.user_id from 72crm_oa_action_record a  where 1 = 1 and " +
                " case when type = 2 or type = 3 or type = 4 or type = 5 then  (a.join_user_ids like concat('%,',?,',%') or a.dept_ids like concat('%,',?,',%') or a.user_id = ?)");
        sql.append(" else ( ");
        if (userIds != null && userIds.size() > 0) {
            for (int i = 0; i < userIds.size(); i++) {
                if (i > 0) {
                    sql.append(" or ");
                }
                sql.append("  a.join_user_ids like concat('%,',? ,',%') ");
                param.add(userIds.get(i));
            }
        }
        param.add(userId);
        sql.append(" or a.user_id = ? )");
        return super.sqlQuery(sql.toString(), param.toArray());
    }
}
