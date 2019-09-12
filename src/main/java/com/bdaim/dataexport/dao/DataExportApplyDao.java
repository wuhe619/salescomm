package com.bdaim.dataexport.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.dataexport.entity.DataExportApply;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class DataExportApplyDao extends SimpleHibernateDao<DataExportApply, Serializable> {

}
