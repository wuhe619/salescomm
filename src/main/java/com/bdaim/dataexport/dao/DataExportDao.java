package com.bdaim.dataexport.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.dataexport.entity.DataExport;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class DataExportDao extends SimpleHibernateDao<DataExport, Serializable> {

}
