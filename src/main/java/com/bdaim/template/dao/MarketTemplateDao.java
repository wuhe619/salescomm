package com.bdaim.template.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.template.entity.MarketTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/2
 * @description
 */
@Component
public class MarketTemplateDao extends SimpleHibernateDao<MarketTemplate, Integer> {

    public MarketTemplate selectByProjectId(int projectId, int type) {
        MarketTemplate marketTemplate = null;
        StringBuilder hql = new StringBuilder();
        hql.append(" FROM MarketTemplate WHERE marketProjectId = ? AND typeCode = ? ");
        List<MarketTemplate> list = find(hql.toString(), projectId, type);
        if (list.size() > 0) {
            marketTemplate = list.get(0);
        }
        return marketTemplate;
    }
}
