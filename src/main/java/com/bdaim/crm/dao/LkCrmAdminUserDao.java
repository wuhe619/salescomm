package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminUserEntity;
import com.bdaim.util.SqlAppendUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmAdminUserDao extends SimpleHibernateDao<LkCrmAdminUserEntity, Long> {

    public List queryUserIdByDeptId(List<String> deptIds) {
        String sql = " select DISTINCT user_id from lkcrm_admin_user where dept_id in (? )";
        return this.queryListBySql(sql, SqlAppendUtil.sqlAppendWhereIn(deptIds));
    }
}
