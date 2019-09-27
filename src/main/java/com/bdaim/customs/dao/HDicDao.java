package com.bdaim.customs.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customs.entity.HDic;
import com.bdaim.customs.entity.HMetaDataDef;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class HDicDao extends SimpleHibernateDao<HMetaDataDef, Serializable> {

    public List<HDic> listHDic() {
        String hql = "FROM HDic ";
        return this.find(hql.toString());
    }

    public HDic getDic(String type, String code) {
        String hql = "FROM HDic WHERE type = ? AND code = ?";
        List<HDic> list = this.find(hql.toString(), type, code);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
