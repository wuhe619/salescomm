package com.bdaim.common.dao;

import com.bdaim.common.entity.DicType;
import com.bdaim.common.entity.DicTypeProperty;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class DicTypeDao extends SimpleHibernateDao<DicType, String> {

    public DicTypeProperty getProperty(String dicTypeId, String dicTypePropName) {
        DicTypeProperty cp = null;
        String hql = "from DicTypeProperty m where m.dicTypeId=? and m.dicTypePropId=?";
        List<DicTypeProperty> list = this.find(hql, dicTypeId, dicTypePropName);
        if (list.size() > 0)
            cp = (DicTypeProperty) list.get(0);
        return cp;
    }

    public List<DicTypeProperty> getPropertyList(String dicTypeId) {
        String hql = "from DicTypeProperty m where m.dicTypeId=?";
        List<DicTypeProperty> list = this.find(hql, dicTypeId);

        return list;
    }
}
