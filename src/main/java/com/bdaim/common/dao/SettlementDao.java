package com.bdaim.common.dao;

import com.bdaim.fund.entity.Settlement;
import com.bdaim.fund.entity.SettlementProperty;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
public class SettlementDao extends SimpleHibernateDao<Settlement, Long> {

    public Settlement getEntity(Long id) {
        Settlement cp = null;
        String hql = "from Settlement m where m.id=?";
        List<Settlement> list = this.find(hql, id);
        if (list.size() > 0)
            cp = (Settlement) list.get(0);
        return cp;
    }

    public SettlementProperty getProperty(Long id, String propertyName) {
        SettlementProperty cp = null;
        String hql = "from SettlementProperty m where m.settlementId=? and m.propertyName=?";
        List<SettlementProperty> list = this.find(hql, id, propertyName);
        if (list.size() > 0)
            cp = (SettlementProperty) list.get(0);
        return cp;
    }

    public Settlement getSettlementEntity(Long id) {
        Settlement cp = null;
        String hql = "from Dic m where m.id=?";
        List<Settlement> list = this.find(hql, id);
        if (list.size() > 0)
            cp = (Settlement) list.get(0);
        return cp;
    }
    public List<SettlementProperty> getPropertyList(Long dicId) {
        String hql = "from SettlementProperty m where m.settlementId=?";
        List<SettlementProperty> list = this.find(hql, dicId);

        return list;
    }
}
