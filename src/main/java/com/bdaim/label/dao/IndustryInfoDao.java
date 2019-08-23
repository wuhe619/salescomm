package com.bdaim.label.dao;


import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.Industry;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 *
 */
@Component
public class IndustryInfoDao extends SimpleHibernateDao<Industry, Integer> {

    public String getIndustryName(int industryInfoId) {
        String hql = "FROM Industry m WHERE m.industryInfoId = ?";
        List<Industry> list = this.find(hql, industryInfoId);
        String industryName = "";
        if (list.size() > 0) {
            industryName = list.get(0).getIndustryName();
        }
        return industryName;
    }
}
