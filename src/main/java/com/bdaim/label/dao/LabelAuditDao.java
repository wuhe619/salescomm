package com.bdaim.label.dao;


import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.LabelAudit;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class LabelAuditDao extends SimpleHibernateDao<LabelAudit, Serializable> {
}
