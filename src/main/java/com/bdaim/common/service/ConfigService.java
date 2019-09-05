package com.bdaim.common.service;

import com.bdaim.common.dao.ConfigDao;
import com.bdaim.common.entity.Config;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

@Service("configService")
@Transactional
public class ConfigService{
	@Resource
	private ConfigDao configDao;
	
	public Integer addConfig(Config config) {
		return (Integer)configDao.saveReturnPk(config);
	}


	public void updateConfig(Config config) {
		configDao.update(config);
	}

	public Config getConfigById(Integer id) {
		return configDao.get(id);
	}
	
	
}
