package com.bdaim.common.dao;

import com.bdaim.common.entity.Config;
import org.springframework.stereotype.Component;

import java.io.Serializable;

@Component
public class ConfigDao extends SimpleHibernateDao<Config, Serializable> {
}
