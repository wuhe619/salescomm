package com.bdaim.label.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.LabelTask;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class LabelTaskDao extends SimpleHibernateDao<LabelTask, Serializable> {
}
