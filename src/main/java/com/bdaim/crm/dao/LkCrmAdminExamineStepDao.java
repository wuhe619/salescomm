package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminExamineStepEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmAdminExamineStepDao extends SimpleHibernateDao<LkCrmAdminExamineStepEntity, Long> {

    public LkCrmAdminExamineStepEntity queryExamineStepByExamineIdOrderByStepNum(Integer examineId) {
        String sql = " from LkCrmAdminExamineStepEntity WHERE examineId = ? ORDER BY stepNum ";
        List<LkCrmAdminExamineStepEntity> objects = find(sql, examineId);
        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

    public List<LkCrmAdminExamineStepEntity> queryExamineStepByExamineId(Integer examineId) {
        String hql = "from LkCrmAdminExamineStepEntity where examineId = ?";
        List<LkCrmAdminExamineStepEntity> objects = find(hql, examineId);
        /*if (objects.size() > 0) {
            return objects.get(0);
        }*/
        return objects;
    }

    public LkCrmAdminExamineStepEntity queryExamineStepByNextExamineIdOrderByStepId(Integer examineId, Long stepId) {
        String sql = " FROM LkCrmAdminExamineStepEntity WHERE examineId = ? and stepNum =  (SELECT stepNum FROM LkCrmAdminExamineStepEntity where stepId = ?) + 1 ";
        List<LkCrmAdminExamineStepEntity> objects = find(sql, examineId, stepId);
        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

}
