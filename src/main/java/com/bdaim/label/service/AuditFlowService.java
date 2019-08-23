package com.bdaim.label.service;


import com.bdaim.label.dao.AuditFlowDao;
import com.bdaim.label.entity.AuditFlow;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service("auditFlowService")
@Transactional
public class AuditFlowService {
	@Resource
	private AuditFlowDao auditFlowDao;

	public void addAuditFlow(AuditFlow auditFlow) {
		auditFlowDao.save(auditFlow);
	}

	public void updateAuditFlow(AuditFlow auditFlow) {
		auditFlowDao.update(auditFlow);
	}

	@SuppressWarnings("unchecked")
	public Integer getAuditStatus(Map<String, Object> map) {
		String hql = "From AuditFlow t where t.availably=1";
		List<AuditFlow> flows = auditFlowDao.getHqlQuery(hql, map, new HashMap<String,Object>(), null).list();
		if(null !=flows && flows.size()>0)
			return flows.get(0).getAuditStatus();
		return null;
	}


	
}
