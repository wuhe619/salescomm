package com.bdaim.label.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.IndustryPoolLabel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public class IndustryLabelDao extends SimpleHibernateDao {

    public List<IndustryPoolLabel> list(Integer industryPoolId) {
        String hql = "FROM IndustryPoolLabel m WHERE m.industryPoolId = ? ";
        List list = this.find(hql, industryPoolId);
        return list;
    }

}
