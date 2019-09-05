package com.bdaim.dataexport.service;

import com.bdaim.dataexport.dao.DataExportOptionalDao;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.List;
import java.util.Map;

@Service("dataExportOptionalService")
@Transactional
public class DataExportOptionalService {
	
	@Resource
	private DataExportOptionalDao dataExportOptionalDao;

	public List<Map<String, Object>> getDataExportOptionalByType(Integer type) {
		return dataExportOptionalDao.createQuery("From DataExportOptional where type=?", new Object[]{type}).list();
	}

}
