package com.bdaim.dataexport.controller;

import com.bdaim.dataexport.service.DataPermissionService;
import com.bdaim.label.dto.CategoryType;
import com.bdaim.label.dto.QueryType;
import com.bdaim.label.entity.DataNode;
import com.bdaim.rbac.dto.UserDTO;
import net.sf.json.JSONArray;
import net.sf.json.JsonConfig;
import net.sf.json.util.PropertyFilter;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * 数据权限
 *
 */
@Controller
@RequestMapping("/dataPrivilege")
public class DataPrivilegeAction {

	@Resource
	private DataPermissionService dataPermissionService;
	
    @RequestMapping(value = "/getLabelList.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getLabelList(Long userId, Integer rootId, Integer deep, QueryType type) {
    	UserDTO user = new UserDTO();
    	user.setId(userId);
    	DataNode dataNode = new DataNode();
    	dataNode.setId(rootId);
    	List<DataNode> vos = dataPermissionService.getLabelList(user, dataNode, deep, type);
    	JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.setJsonPropertyFilter(new PropertyFilter(){

			@Override
			public boolean apply(Object source, String name, Object value) {
				if(name.equals("children")){
					ArrayList<DataNode> lst = (ArrayList<DataNode>)value;
					if(lst.size()==0){
						return true;
					}
				}
				return false;
			}
    		
    	});
        JSONArray array = JSONArray.fromObject(vos, jsonConfig);
        return array.toString();
    }
	
    @RequestMapping(value = "/getCategoryList.do", produces = "application/json;charset=UTF-8")
    @ResponseBody
    public String getCategoryList(Long userId, Integer rootId, Integer deep, QueryType type, CategoryType categoryType) {
    	UserDTO user = new UserDTO();
    	user.setId(userId);
    	DataNode dataNode = new DataNode();
    	dataNode.setId(rootId);
    	List<DataNode> vos = dataPermissionService.getCategoryList(user, dataNode, deep, type, categoryType);
    	JsonConfig jsonConfig = new JsonConfig();
    	jsonConfig.setJsonPropertyFilter(new PropertyFilter(){

			@Override
			public boolean apply(Object source, String name, Object value) {
				if(name.equals("children")){
					ArrayList<DataNode> lst = (ArrayList<DataNode>)value;
					if(lst.size()==0){
						return true;
					}
				}
				return false;
			}
    		
    	});
        JSONArray array = JSONArray.fromObject(vos, jsonConfig);
        return array.toString();
    }
    
}
