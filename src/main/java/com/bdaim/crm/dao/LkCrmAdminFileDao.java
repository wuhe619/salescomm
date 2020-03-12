package com.bdaim.crm.dao;

import cn.hutool.core.bean.BeanUtil;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.dto.LkCrmAdminFileDTO;
import com.bdaim.crm.entity.LkCrmAdminFileEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminFileDao extends SimpleHibernateDao<LkCrmAdminFileEntity, Integer> {

    public List<LkCrmAdminFileDTO> queryByBatchId(String batchId) {
        String sql = " SELECT a.file_id,a.name, CONCAT(FLOOR(a.size/1000),\"KB\") as size,a.create_user_id,b.realname as create_user_name,a.create_time,a.file_path,a.file_type,a.batch_id\n" +
                "      FROM `lkcrm_admin_file` as a inner join `lkcrm_admin_user` as b on a.create_user_id = b.user_id where a.batch_id=? ORDER BY a.create_time DESC ";
        List<Map<String, Object>> list = this.sqlQuery(sql, batchId);
        List<LkCrmAdminFileDTO> result = new ArrayList<>();
        list.forEach(s -> result.add(BeanUtil.mapToBean(s, LkCrmAdminFileDTO.class, true)));
        return result;
    }

    public List<String> queryPathByBatchId(String batchId) {
        String sql = " SELECT path FROM `lkcrm_admin_file` as a where a.batch_id=?";
        List<Map<String, Object>> list = this.sqlQuery(sql, batchId);
        List<String> result = new ArrayList<>();
        list.forEach(s -> result.add(String.valueOf(s.get("path"))));
        return result;
    }
}
