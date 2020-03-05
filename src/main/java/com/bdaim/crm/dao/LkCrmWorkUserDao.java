package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmWorkUserEntity;
import com.bdaim.crm.erp.work.entity.WorkUser;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmWorkUserDao extends SimpleHibernateDao<LkCrmWorkUserEntity, Integer> {
    public List<Map<String, Object>> queryWorkNameList() {
        String sql = "select  a.work_id,a.name,owner_user_id " +
                "    from lkcrm_work a";
        List<Map<String, Object>> result = super.queryListBySql(sql);
        return result;
    }

    public List<Map<String, Object>> queryWorkNameListByUserId(Long userId) {
        String sql = "    select  a.work_id,a.name,owner_user_id " +
                "    from lkcrm_work a " +
                "    where 1 = 1" +
                "    and (owner_user_id like concat('%,',?,',%') and is_open = 0) or is_open = 1";
        List<Map<String, Object>> result = super.queryListBySql(sql, userId);
        return result;
    }
}
