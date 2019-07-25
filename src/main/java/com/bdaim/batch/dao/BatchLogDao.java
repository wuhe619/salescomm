package com.bdaim.batch.dao;

import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.batch.entity.BatchLogEntity;
import com.bdaim.common.dao.SimpleHibernateDao;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @author duanliying
 * @date 2018/9/6
 * @description
 */
@Component
public class BatchLogDao extends SimpleHibernateDao<BatchLogEntity, Serializable> {

}
