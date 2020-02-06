package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminFileEntity;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class LkCrmAdminFileDao extends SimpleHibernateDao<LkCrmAdminFileEntity, Integer> {

    public List<LkCrmAdminFileEntity> queryByBatchId(String batchId) {
        String sql = " SELECT a.file_id,a.name, CONCAT(FLOOR(a.size/1000),\"KB\") as size,a.create_user_id,b.realname as create_user_name,a.create_time,a.file_path,a.file_type,a.batch_id\n" +
                "      FROM `lkcrm_admin_file` as a inner join `lkcrm_admin_user` as b on a.create_user_id = b.user_id where a.batch_id=?";
        List list = this.queryListBySql(sql, LkCrmAdminFileEntity.class, batchId);
        return list;
    }
}
