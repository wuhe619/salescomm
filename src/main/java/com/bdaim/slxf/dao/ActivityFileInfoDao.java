package com.bdaim.slxf.dao;

import com.bdaim.batch.entity.ActiveFileInfo;
import com.bdaim.common.dao.SimpleHibernateDao;

import org.springframework.stereotype.Component;

import java.io.Serializable;


@Component
public class ActivityFileInfoDao extends SimpleHibernateDao<ActiveFileInfo,Serializable>{
}
