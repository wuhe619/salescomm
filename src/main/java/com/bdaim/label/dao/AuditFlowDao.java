package com.bdaim.label.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.AuditFlow;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class AuditFlowDao extends SimpleHibernateDao<AuditFlow, Serializable> {
}
