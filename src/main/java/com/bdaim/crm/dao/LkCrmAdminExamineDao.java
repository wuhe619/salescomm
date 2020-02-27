package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmAdminExamineEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminExamineDao extends SimpleHibernateDao<LkCrmAdminExamineEntity, Integer> {

    /*public LkCrmAdminExamineEntity getExamineByCategoryType(int category_type) {
        String sql = "from LkCrmAdminExamineEntity where categoryType = ? AND status = 1 order by updateTime desc ";
        List<LkCrmAdminExamineEntity> maps = find(sql, category_type);
        if (maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }*/

    public Map getExamineByCategoryType(int category_type) {
        String sql = " select * from lkcrm_admin_examine where  category_type = ? AND status = 1 order by update_time desc limit 1 ";
        List<Map<String, Object>> maps = sqlQuery(sql, category_type);
        if (maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }

    public Page queryExaminePage(int pageNum, int pageSize) {
        String sql = " select adminExamine.*,adminUser.realname as updateUserName,createUser.realname as createUserName\n" +
                "     from lkcrm_admin_examine as adminExamine " +
                "    LEFT JOIN lkcrm_admin_user as adminUser on adminUser.user_id = adminExamine.update_user_id\n" +
                "    LEFT JOIN lkcrm_admin_user as createUser on createUser.user_id = adminExamine.create_user_id\n" +
                "    where  adminExamine.status != 2 ";
        Page maps = sqlPageQuery(sql, pageNum, pageSize);
        return maps;
    }

}
