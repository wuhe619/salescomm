package com.bdaim.customs.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customs.entity.HBusiDataManager;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class HBusiDataManagerDao extends SimpleHibernateDao<HBusiDataManager, Serializable> {

    /**
     * 根据字段名称和值查询单个对象
     * @param propertyName
     * @param value
     * @return
     */
    public HBusiDataManager getHBusiDataManager(String propertyName, String value) {
        return this.findUniqueBy(propertyName, value);
    }

    /**
     * 通过主单查询分单集合
     * @param pid
     * @return
     */
    public List<HBusiDataManager> listHBusiDataManager(int pid, String type) {
        String hql = "FROM HBusiDataManager WHERE type = ? AND ext_4 = (SELECT ext_3 FROM HBusiDataManager WHERE id = ?)";
        return this.find(hql, type, pid);
    }

}
