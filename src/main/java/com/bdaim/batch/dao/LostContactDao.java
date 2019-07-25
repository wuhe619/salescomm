package com.bdaim.batch.dao;

import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.dto.CustomerInfoDTO;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.Serializable;

/**
 * @author duanliying
 * @date 2018/9/10
 * @description
 */
@Component
public class LostContactDao extends SimpleHibernateDao<CustomerInfoDTO, Integer> {
    @Resource
    private JdbcTemplate jdbcTemplate;

    public void insertLog(String sql) {
        jdbcTemplate.execute(sql);
    }
}
