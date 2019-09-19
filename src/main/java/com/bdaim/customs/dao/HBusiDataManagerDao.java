package com.bdaim.customs.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customs.entity.HBusiDataManager;
import org.hibernate.Query;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class HBusiDataManagerDao extends SimpleHibernateDao<HBusiDataManager, Serializable> {

    /**
     * 根据字段名称和值查询单个对象
     *
     * @param propertyName
     * @param value
     * @return
     */
    public HBusiDataManager getHBusiDataManager(String propertyName, String value) {
        List<HBusiDataManager> list = this.findBy(propertyName, value);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 通过主单查询分单集合
     *
     * @param pid
     * @return
     */
    public List<HBusiDataManager> listHBusiDataManager(int pid, String type) {
        String hql = "FROM HBusiDataManager WHERE type = ? AND ext_4 = (SELECT ext_3 FROM HBusiDataManager WHERE id = ?)";
        return this.find(hql, type, pid);
    }


    public List<HBusiDataManager> listHBusiDataManager(List ids, String type) {
        String hql = "FROM HBusiDataManager WHERE type = ? AND id IN (:ids)";
        Query query = super.createQuery(hql, type);
        query.setParameterList("ids", ids);
        return query.list();
    }

    /**
     * 查询主单下分单的身份证照片数量
     *
     * @param pid
     * @param type
     * @return
     */
    public int countMainDIdCardNum(int pid, String type) {
        String hql = "SELECT COUNT(0) FROM HBusiDataManager WHERE type = ? AND ext_4 = (SELECT ext_3 FROM HBusiDataManager WHERE id = ?) AND ext_6 IS NOT NULL AND ext_6 <>'' ";
        return this.findCount(hql, type, pid);
    }

}
