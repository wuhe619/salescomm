package com.bdaim.label.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.dataexport.service.DataPermissionService;
import com.bdaim.label.service.LabelCategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("category")
public class LabelCategoryAction extends BasicAction {
	@Resource
	private LabelCategoryService labelCategoryServiceImpl;
	@Resource
	private DataPermissionService dataPermissionService;

	
	public LabelCategoryAction()
	{
		super.pageName = "标签品类";
	}
	/**
	 * 根据父id获取所有子品类
	 * 
	 * @param pid
	 * @return
	 */
	@RequestMapping(value = "loadLabelCategoryChildrenByPid")
	@ResponseBody
	@CacheAnnotation
	public String loadLabelCategoryChildrenByPid(Integer pid) {
		String result = null;
		List<Map<String, Object>> list = labelCategoryServiceImpl
				.getChildrenById(pid);
		result = JSONArray.toJSONString(list);
		return result;
	}
	
	/**
	 * 根据分类获取所有品类
	 * 
	 * @param id
	 * @return
	 */
	@RequestMapping(value = "loadAllLabelCategoryByClassId")
	@ResponseBody
	@CacheAnnotation
	public String loadAllLabelCategoryByClassId(Integer id) {
		String result = null;
		List<Map<String, Object>> list = labelCategoryServiceImpl.getAllLabelCategoryByClassId(id);
		result = JSONArray.toJSONString(list);
		return result;
	}
	
	/**
	 * 获取叶子标签品类
	 * @return
	 */
	@RequestMapping("loadAllLabelCategoryByIsLeaf")
	@ResponseBody
	@CacheAnnotation
	public String loadAllLabelCategoryByIsLeaf() {
		JSONObject json = new JSONObject();
		//List<Map<String, Object>> list = labelCategoryServiceImpl.getAllLabelCategoryByIsLeaf(user.get().getId());
		List<Integer> ids = dataPermissionService.getLeafCategoryList(request);
		List<Map<String, Object>> list = labelCategoryServiceImpl.getLeafLabelCategory(ids);
		json.put("stores", list);
		return JSONArray.toJSONString(json);
	}
	
	/**
	 * 获取叶子标签品类
	 * @return
	 */
	@RequestMapping("getCategoryTreeById")
	@ResponseBody
	@CacheAnnotation
	public String getCategoryTreeById(Integer id) {
		Map<String,Object> map = new HashMap<String,Object>();
//		UserManager manager = new UserManagerImpl();
		LoginUser user= opUser();
		map.put("id", id);
		map.put("user", user);
		String result = labelCategoryServiceImpl.getCategoryTreeByMap(map);
		return result;
	}
	
}
