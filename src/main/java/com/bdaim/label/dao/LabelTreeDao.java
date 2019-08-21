package com.bdaim.label.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.LabelTree;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class LabelTreeDao extends SimpleHibernateDao<LabelTree, Serializable> {
}
