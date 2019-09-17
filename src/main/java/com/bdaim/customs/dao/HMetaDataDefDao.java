package com.bdaim.customs.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.dataexport.entity.DataExportApply;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class HMetaDataDefDao extends SimpleHibernateDao<HMetaDataDef, Serializable> {

}
