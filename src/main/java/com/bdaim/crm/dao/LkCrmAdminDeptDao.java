package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminDeptEntity;
import com.bdaim.util.SqlAppendUtil;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmAdminDeptDao extends SimpleHibernateDao<LkCrmAdminDeptEntity, Integer> {

    public List queryByIds(List deptIds) {
        return super.queryListBySql(" select dept_id as id,name from lkcrm_admin_dept where dept_id in (" + SqlAppendUtil.sqlAppendWhereIn(deptIds) + ")");
    }
}
