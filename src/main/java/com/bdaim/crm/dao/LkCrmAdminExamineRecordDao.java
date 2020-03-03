package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminExamineEntity;
import com.bdaim.crm.entity.LkCrmAdminExamineRecordEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

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

    public Map queryExamineRecordById(Integer record_id) {
        String sql = "  SELECT saer.* ,'' AS img,sau.realname from 72crm_admin_examine_record as  saer\n" +
                "     LEFT JOIN t_customer_user as sau on sau.d = saer.create_user\n" +
                "     WHERE saer.record_id = ? ";
        List<Map<String, Object>> objects = sqlQuery(sql, record_id);
        if(objects.size()>0){
            return objects.get(0);
        }
        return null;
    }
}
