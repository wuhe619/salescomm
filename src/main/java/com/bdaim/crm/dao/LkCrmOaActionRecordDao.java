package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmOaActionRecordEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmOaActionRecordDao extends SimpleHibernateDao<LkCrmOaActionRecordEntity, Integer> {

    public Page queryList(int pageNum, int pageSize, List userIds,
                          long userId, int deptId, Integer type) {
        List param = new ArrayList();
        param.add(userId);
        param.add(deptId);
        param.add(userId);
        StringBuffer sql = new StringBuffer(" select a.log_id,a.action_id,a.content as action_content,a.create_time,a.type,a.user_id from 72crm_oa_action_record a  where 1 = 1 and " +
                " case when type = 2 or type = 3 or type = 4 or type = 5 then  (a.join_user_ids like concat('%,',?,',%') or a.dept_ids like concat('%,',?,',%') or a.user_id = ?)");
        sql.append(" else ( ");
        if (!CollectionUtils.isEmpty(userIds)) {
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
        if (type != null) {
            sql.append(" and a.type = ? ");
            param.add(type);
        }
        sql.append(" order by a.create_time desc ");
        return super.sqlPageQuery(sql.toString(), pageNum, pageSize, param.toArray());
    }
}
