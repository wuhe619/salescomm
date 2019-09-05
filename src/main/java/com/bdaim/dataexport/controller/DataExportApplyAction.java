package com.bdaim.dataexport.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.util.Constant;
import com.bdaim.dataexport.entity.DataExportApply;
import com.bdaim.dataexport.service.DataExportApplyService;
import com.bdaim.label.entity.LabelAudit;
import com.bdaim.label.service.LabelAuditService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/dataExportApply")
public class DataExportApplyAction extends BasicAction {

	@Resource
	private DataExportApplyService dataExportApplyService;
	@Resource
	private LabelAuditService labelAuditService;
	
	@ResponseBody
	@CacheAnnotation
	@RequestMapping("/addDataExportApply")
	public String addDataExportApply(DataExportApply dataExportApply){
		Map<String, Object> rstMap = new HashMap<String, Object>();
		try{
			dataExportApply.setApplyUser(opUser().getUser());
			Integer id = dataExportApplyService.addDataExportApply(dataExportApply);
			if(null!=id){
				LabelAudit audit = new LabelAudit();
				audit.setAid(id);
				audit.setName(Constant.APPLY_TYPE_EXPORT_CN);
				audit.setAuditType(Constant.AUDIT_TYPE_EXPORT);
				audit.setApplyType(Constant.APPLY_TYPE_EXPORT);
				audit.setStatus(Constant.AUDITING);
				audit.setApplyUser(opUser().getUser());
				audit.setApplyTime(new Date());
				audit.setAvailably(Constant.AVAILABLY);
				audit.setLastFlag(Constant.AUDIT_LAST_FLAG_YES);
				labelAuditService.addAuditInfo(audit);
			}
			rstMap.put("_message", "导出权限申请成功！");
		}catch(Exception e){
			e.printStackTrace();
		}
		return JSON.toJSONString(rstMap);
	}
	
}
