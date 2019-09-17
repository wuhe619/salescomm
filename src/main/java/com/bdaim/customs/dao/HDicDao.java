package com.bdaim.customs.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customs.entity.HMetaDataDef;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class HDicDao extends SimpleHibernateDao<HMetaDataDef, Serializable> {

}
