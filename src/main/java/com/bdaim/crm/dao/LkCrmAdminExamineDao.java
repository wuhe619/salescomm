package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminExamineEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmAdminExamineDao extends SimpleHibernateDao<LkCrmAdminExamineEntity, Integer> {

    public LkCrmAdminExamineEntity getExamineByCategoryType(int category_type) {
        String sql = "from LkCrmAdminExamineEntity where categoryType = ? AND status = 1 order by updateTime desc ";
        List<LkCrmAdminExamineEntity> maps = find(sql, category_type);
        if (maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }

}
