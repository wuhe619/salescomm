package com.bdaim.common.dao;

import com.bdaim.common.entity.ChartConfig;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class ChartConfigDao extends SimpleHibernateDao<ChartConfig, Serializable> {
}
