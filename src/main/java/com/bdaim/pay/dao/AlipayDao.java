package com.bdaim.pay.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 阿里支付 DAO服务
 *
 */
@Component
public class AlipayDao extends SimpleHibernateDao<Object, Integer> {
	
	@Resource
	private JdbcTemplate jdbcTemplate;
	
	
}
