package com.bdaim.common.dao;

import com.bdaim.common.entity.Dic;
import com.bdaim.common.entity.DicProperty;
import com.bdaim.util.StringUtil;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
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


    public List<DicProperty> listDicNotInProperty(Long dicId, String dicPropKey, String dicPropValue, String type, String dicType, String recommendConfig) {
        StringBuilder hql = new StringBuilder("from DicProperty m, Dic a where m.dicId = a.id AND m.dicPropKey=? AND m.dicPropValue = ? AND m.dicId <> ? AND a.status = 1");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(type)) {
            p.add(type);
            hql.append(" AND a.id in (SELECT dicId from DicProperty where dicPropKey='type' and dicPropValue = ? )");
        }
        p.add(recommendConfig);
        hql.append(" AND a.id in (SELECT dicId from DicProperty where dicPropKey='recommendConfig' and dicPropValue LIKE '%?%')");
        hql.append(" AND a.dicTypeId = ?");
        List<DicProperty> list = this.find(hql.toString(), dicPropKey, dicPropValue, dicId, p.toArray(), dicType);
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

    /**
     * 检查名称是否存在
     *
     * @param dicId     不包含的id
     * @param name      名称
     * @param dicTypeId 产品类型
     * @param status    状态
     * @return
     */
    public List<Dic> listNameExist(Long dicId, String name, String dicTypeId, int status) {
        List<Object> param = new ArrayList<>();
        param.add(name);
        StringBuilder hql = new StringBuilder("FROM Dic m where m.name=? ");
        if (dicId != null) {
            hql.append(" AND m.id NOT IN(?) ");
            param.add(dicId);
        }
        if (StringUtil.isNotEmpty(dicTypeId)) {
            hql.append(" AND m.dicTypeId = ? ");
            param.add(dicTypeId);
        }
        if (status > 0) {
            hql.append(" AND m.status = ? ");
            param.add(status);
        }
        return this.find(hql.toString(), param);
    }

}
