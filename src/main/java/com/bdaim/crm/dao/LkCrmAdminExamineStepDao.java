package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminExamineStepEntity;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminExamineStepDao extends SimpleHibernateDao<LkCrmAdminExamineStepEntity,Integer> {

    public LkCrmAdminExamineStepEntity queryExamineStepByExamineIdOrderByStepNum(Integer examineId) {
        String sql = " from LkCrmAdminExamineStepEntity WHERE examineId = ? ORDER BY step_num ";
        List<LkCrmAdminExamineStepEntity> objects = find(sql, examineId);
        if(objects.size()>0){
            return objects.get(0);
        }
        return null;
    }

    public List<Map<String, Object>> queryExamineStepByExamineId(Integer examineId) {
        String sql = "  select * from lkcrm_admin_examine_step where  examine_id = ?";
        List<Map<String, Object>> objects = sqlQuery(sql, examineId);
        return objects;
    }
}
