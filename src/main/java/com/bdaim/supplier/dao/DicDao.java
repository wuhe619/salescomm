package com.bdaim.supplier.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.entity.Dic;
import com.bdaim.common.entity.DicProperty;
import org.springframework.stereotype.Component;
import java.util.List;


@Component
public class DicDao extends SimpleHibernateDao<Dic, Long> {

    public DicProperty getProperty(Long dicId, String dicPropKey) {
        DicProperty cp = null;
        String hql = "from DicProperty m where m.dicId=? and m.dicPropKey=?";
        List<DicProperty> list = this.find(hql, dicId, dicPropKey);
        if (list.size() > 0)
            cp = (DicProperty) list.get(0);
        return cp;
    }

    public List<DicProperty> getPropertyList(Long dicId) {
        String hql = "from DicProperty m where m.dicId=?";
        List<DicProperty> list = this.find(hql, dicId);
        return list;
    }


    public Dic getDicEntity(Long dicId) {
        Dic cp = null;
        String hql = "from Dic m where m.id=?";
        List<Dic> list = this.find(hql, dicId);
        if (list.size() > 0)
            cp = (Dic) list.get(0);
        return cp;
    }

}
