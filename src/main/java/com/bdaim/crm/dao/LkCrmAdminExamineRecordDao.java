package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminExamineEntity;
import com.bdaim.crm.entity.LkCrmAdminExamineRecordEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmAdminExamineRecordDao extends SimpleHibernateDao<LkCrmAdminExamineRecordEntity, Integer> {

    public LkCrmAdminExamineEntity getExamineByCategoryType(Integer type) {
        String sql = " from LkCrmAdminExamineEntity where  categoryType = ? AND status = 1 order by updateTime desc ";
        List<LkCrmAdminExamineEntity> objects = find(sql, type);
        if(objects.size()>0){
            return objects.get(0);
        }
        return null;
    }
}
