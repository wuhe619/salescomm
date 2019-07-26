package com.bdaim.callcenter.dao;

import com.bdaim.callcenter.dto.SeatsMessageParam;
import com.bdaim.common.dao.SimpleHibernateDao;

import org.springframework.stereotype.Component;

/**
 * @author duanliying
 * @date 2018/9/25
 * @description
 */
@Component
public class SeatsDao  extends SimpleHibernateDao<SeatsMessageParam, Integer>{
}
