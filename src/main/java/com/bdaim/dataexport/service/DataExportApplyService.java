package com.bdaim.dataexport.service;

import com.bdaim.dataexport.dao.DataExportApplyDao;
import com.bdaim.dataexport.entity.DataExportApply;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.Date;

@Service("dataExportApplyService")
@Transactional
public class DataExportApplyService {

	@Resource
	private DataExportApplyDao dataExportApplyDao;
	
	public Integer addDataExportApply(DataExportApply dataExportApply) {
		if(null!=dataExportApply)
			dataExportApply.setCreateTime(new Date());
		Integer id = (Integer)dataExportApplyDao.saveReturnPk(dataExportApply);
		return id;
	}

	public DataExportApply getDataExportApplyById(Integer applyId) {
		return dataExportApplyDao.get(applyId);
	}

	public void updataDataExportApply(DataExportApply apply) {
		dataExportApplyDao.update(apply);
	}

}
