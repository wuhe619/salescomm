package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.CustomerMarketExportDO;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/4
 * @description
 */
@Component
public class CustomerMarketExportDao extends SimpleHibernateDao<CustomerMarketExportDO, Serializable> {
}
