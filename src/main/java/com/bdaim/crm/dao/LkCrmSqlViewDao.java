package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmSqlViewEntity;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.List;

@Component
public class LkCrmSqlViewDao extends SimpleHibernateDao<LkCrmSqlViewEntity, Integer> {

    public int saveSqlView(String custId, String name, String sql) {
        List<LkCrmSqlViewEntity> list = find("FROM LkCrmSqlViewEntity WHERE custId = ? AND name = ?", custId, name);
        LkCrmSqlViewEntity entity = null;
        boolean update = false;
        if (list.size() > 0) {
            entity = list.get(0);
            update = true;
        } else {
            entity = new LkCrmSqlViewEntity();
            entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        }
        entity.setCustId(custId);
        entity.setName(name);
        entity.setSqlContent(sql);
        if (update) {
            update(entity);
            return 1;
        } else {
            return (int) this.saveReturnPk(entity) > 0 ? 1 : 0;
        }
    }

    public String getViewSql(String custId, String name) {
        List<LkCrmSqlViewEntity> objects = this.find(" FROM LkCrmSqlViewEntity WHERE custId = ? AND name = ? ", custId, name);
        if (objects.size() > 0) {
            return objects.get(0).getSqlContent();
        }
        return "";
    }
}
