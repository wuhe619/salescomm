package com.bdaim.rbac.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.rbac.service.UserLabelCategoryService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/userLabelCategory")
public class UserLabelCategoryAction extends BasicAction {

	@Resource
	private UserLabelCategoryService userLabelCategoryService;
	
	/**
	 * 设置用户默认标签品类
	 * @param labelCategoryId
	 * @param isDefault
	 * @return
	 */
	@RequestMapping("/updateIsDefault")
	@ResponseBody
	public String updateIsDefault(Integer labelCategoryId){
		JSONObject json = new JSONObject();
		Map<String, Object> map = new HashMap<String, Object>();
		try{
			userLabelCategoryService.updateIsDefault(opUser().getId(), labelCategoryId);
			map.put("result", 1);
			map.put("_message", "设置默认品类成功！");
		}catch(Exception e){
			map.put("result", 0);
			map.put("_message", "设置默认品类失败！");
			e.printStackTrace();
		}
		json.put("stores", map);
		return JSONArray.toJSONString(json);
	}
	
}
