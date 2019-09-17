package com.bdaim.customs.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customs.entity.Station;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @description 场站信息Dao
 * @author:duanliying
 * @method
 * @date: 2019/9/16 11:38
 */
@Component
public class StationDao extends SimpleHibernateDao<Station, String> {
    private static Logger logger = LoggerFactory.getLogger(StationDao.class);

    public Station getStationById(int id) {
        Station cp = null;
        String hql = "from Station m where m.id=?";
        List<Station> list = this.find(hql, id);
        if (list.size() > 0)
            cp = (Station) list.get(0);
        return cp;
    }
}
