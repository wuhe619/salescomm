package com.bdaim.dataexport.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.dataexport.service.DataExportOptionalService;
import com.bdaim.dataexport.service.DataPermissionService;
import com.bdaim.label.dto.QueryType;
import com.bdaim.label.entity.DataNode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/dataExportOptional")
public class DataExportOptionalAction extends BasicAction {

	@Resource
	private DataExportOptionalService dataExportOptionalService;
	@Resource
	private DataPermissionService dataPermissionService;
	
	@ResponseBody
	@RequestMapping("/getDataExportOptionalByType")
	public String getDataExportOptionalByType(Integer type){
		List<Map<String, Object>> lstMap = new ArrayList<Map<String, Object>>();
		if(type==0){
			
			return null;
		}else{
			List<DataNode> lst = dataPermissionService.getLabelList(opUser().getId(), 1, null, QueryType.PRIVILEGE);
			if(null != lst){
				for(DataNode dn : lst) {
					Map<String, Object> map = new HashMap<String,  Object>();
					map.put("code", dn.getId());
					map.put("value", dn.getName());
					lstMap.add(map);
				}
			}
			return JSON.toJSONString(lstMap);
		}
		//return JSON.toJSONString(lst);
	}
	
}
