package com.bdaim.rbac.controller;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.rbac.dto.AbstractTreeResource;
import com.bdaim.rbac.dto.CommonTreeResource;
import com.bdaim.rbac.service.ResourceService;
import com.bdaim.rbac.service.UserService;
import com.bdaim.rbac.vo.UserInfo;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;


@Controller
@RequestMapping(value = "/userinfo")
public class UserInfoAction extends BasicAction {
	
	@Resource
	private UserService userService;
	@Resource
	private ResourceService resourceService;
	
	/**查询用户的基本信息，包括部门，岗位，最后登录时间
	 * @param request
	 * @return
	 */
	@RequestMapping("/index.do")
	@ResponseBody
	public String index(HttpServletRequest request){
		LoginUser user= opUser();
		String userName = user.getName();
		UserInfo info = userService.queryUserInfo(userName);
		return JSONObject.fromObject(info).toString();
	}
	

	/**查询用户拥有的pid下的资源
	 * @param request
	 * @param pid
	 * @return
	 */
	@RequestMapping("/authority.do")
	@ResponseBody
	public String queryUserAuthority(HttpServletRequest request, @RequestParam Long pid){
		LoginUser user= opUser();
		Long operateUserId = user.getId();
		JSONArray array = resourceService.queryResource(operateUserId, pid, user.isAdmin());
		return array.toString();
	}
	
	@RequestMapping("/querySystem.do")
	@ResponseBody
	public String queryUserSystem(HttpServletRequest request){
		LoginUser user= opUser();
		//Long operateUserId = operateUser.getId();
		List<CommonTreeResource> list = resourceService.queryUserSystem(user.getName(),user.isAdmin());
		JSONArray array = JSONArray.fromObject(list);
		return array.toString();
	}
	
	/**
	 * 查询用户
	 * @param request
	 * @param response
	 * @param pid
	 * @return 
	 */
	@RequestMapping("/allauthority.do")
	@ResponseBody
	public String queryUserAllAuthority(HttpServletRequest request, @RequestParam String type, @PathVariable Long pid){
		AbstractTreeResource abstractTreeResource = new CommonTreeResource(pid);
		LoginUser user= opUser();
		AbstractTreeResource all = null;
		if(user.isAdmin()){
			all = (CommonTreeResource) resourceService.queryAllTree(abstractTreeResource,new String[] { type });
		}else{
			all = (CommonTreeResource) resourceService.queryUserTree(user.getId(), abstractTreeResource,new String[] { type });	
		}
		List<AbstractTreeResource> list = all.getNotes();
		JSONArray array = JSONArray.fromObject(list);
		array = array==null?new JSONArray():array;
		return array.toString();
	}

}
