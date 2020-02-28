package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmOaExamineEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Component
public class LkCrmOaExamineDao extends SimpleHibernateDao<LkCrmOaExamineEntity, Integer> {

    public List myInitiate(Long userId, Integer categoryId, Integer status, Date startTime, Date endTime) {
        String sql = "select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id ,c.category_id,c.title as categoryTitle\n" +
                "    from 72crm_oa_examine a left join 72crm_oa_examine_record b on a.examine_id = b.examine_id left join 72crm_oa_examine_category c on a.category_id = c.category_id\n" +
                "    where a.create_user_id = ? ";
        List param = new ArrayList();
        param.add(userId);
        if (categoryId != null) {
            sql += " and a.category_id =?";
            param.add(categoryId);
        }
        if (status != null) {
            sql += "   and  b.examine_status = ? ";
            param.add(status);
        }
        if (startTime != null && endTime != null) {
            sql += "  and a.create_time between ? and  ? ";
            param.add(startTime);
            param.add(endTime);
        }
        sql += "group by a.examine_id,b.record_id order by  a.create_time desc ";
        return super.sqlQuery(sql, param.toArray());
    }

    public Page pageMyInitiate(int pageNum, int pageSize, Long userId, Integer categoryId, Integer status, Date startTime, Date endTime) {
        String sql = "select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id ,c.category_id,c.title as categoryTitle\n" +
                "    from 72crm_oa_examine a left join 72crm_oa_examine_record b on a.examine_id = b.examine_id left join 72crm_oa_examine_category c on a.category_id = c.category_id\n" +
                "    where a.create_user_id = ? ";
        List param = new ArrayList();
        param.add(userId);
        if (categoryId != null) {
            sql += " and a.category_id =?";
            param.add(categoryId);
        }
        if (status != null) {
            sql += "   and  b.examine_status = ? ";
            param.add(status);
        }
        if (startTime != null && endTime != null) {
            sql += "  and a.create_time between ? and  ? ";
            param.add(startTime);
            param.add(endTime);
        }
        sql += "group by a.examine_id,b.record_id order by  a.create_time desc ";
        return super.sqlPageQuery(sql,pageNum,pageSize, param.toArray());
    }
}
