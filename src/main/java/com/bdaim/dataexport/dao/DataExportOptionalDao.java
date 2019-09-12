package com.bdaim.dataexport.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.dataexport.entity.DataExportOptional;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class DataExportOptionalDao extends SimpleHibernateDao<DataExportOptional, Serializable> {

}
