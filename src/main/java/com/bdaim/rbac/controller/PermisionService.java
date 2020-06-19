package com.bdaim.rbac.controller;

import com.bdaim.rbac.dao.ResourceDao;
import com.bdaim.rbac.dto.AbstractTreeResource;
import com.bdaim.rbac.dto.CommonTreeResource;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.service.ResourceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 */
@Controller
@RequestMapping("/service")
public class PermisionService {

	@Resource
	private ResourceService resourceService;
	
    @RequestMapping("/queryPersionByID/{userid}/{pid}/{type}")
    @ResponseBody
    public String queryUserPermisionByID(@PathVariable Long userid, @PathVariable long pid, @PathVariable String type) {
        AbstractTreeResource abstractTreeResource = new CommonTreeResource(pid);

        AbstractTreeResource all = null;
        UserDTO user = new UserDTO();
        user.setId(userid);
        if ("all".equals(type)) {
            all = resourceService.queryUserTree(userid, abstractTreeResource, null);
        } else {
            all = resourceService.queryUserTree(userid, abstractTreeResource, new String[]{type});
        }
        return all.toJArray(all.getNotes()).toString();
    }

    @RequestMapping("/queryPersionByUri/{userid}/{uri}/{type}")
    @ResponseBody
    public String queryUserPermisionByUri(@PathVariable Long userid, @PathVariable String uri, @PathVariable String type) {
        CommonTreeResource resource = new CommonTreeResource();
        resource.setUri(uri);
        AbstractTreeResource all = null;
        UserDTO user = new UserDTO();
        user.setId(userid);
        if ("all".equals(type)) {
            all = resourceService.queryUserTree(userid, resourceService.getObj(resource), null);
        } else {
            all = resourceService.queryUserTree(userid, resourceService.getObj(resource), new String[]{type});
        }
        return all.toJArray(all.getNotes()).toString();
    }

    @RequestMapping("/queryLoginUser.do")
    @ResponseBody
    public String queryLoginUser(HttpServletRequest request) {
        ObjectMapper mapper = new ObjectMapper();
        ObjectNode node = mapper.createObjectNode();
        AttributePrincipal userP = (AttributePrincipal) request.getUserPrincipal();
        node.put("id", userP.getAttributes().get("id").toString());
        node.put("name", userP.getAttributes().get("name").toString());
        node.put("realname", userP.getAttributes().get("realname").toString());
        return node.toString();
    }

    @RequestMapping("/queryPersion/{userid}")
    @ResponseBody
    public String queryUserPermision(@PathVariable Long userid) {
        AbstractTreeResource abstractTreeResource = new CommonTreeResource(0L);
        UserDTO user = new UserDTO();
        user.setId(userid);
        JSONObject root=new JSONObject();
        
        return query(abstractTreeResource,user,null).toString();
    }

    public JSONArray query(AbstractTreeResource abstractTreeResource, UserDTO user, String type){
        AbstractTreeResource all = null;
        if (type==null){
            all = resourceService.queryUserTree(user.getId(), abstractTreeResource, null);
        }else {
            all = resourceService.queryUserTree(user.getId(), abstractTreeResource, new String[]{type});
        }
        JSONArray array=new JSONArray();
        if(all!=null&&all.getNotes()!=null){
            for (AbstractTreeResource resource:all.getNotes()){
                JSONObject item=new JSONObject();
                item.put("name",resource.getName());
                item.put("id",resource.getID());
                item.put("uri",resource.getUri());
                item.put("order",resource.getSn());
                item.put("type",resource.getType());
                AbstractTreeResource children=resourceService.queryUserTree(user.getId(),resource,null);
                if (children!=null&&children.getNotes()!=null){
                    JSONArray childrenJson=new JSONArray();
                    for (AbstractTreeResource child:children.getNotes()){
                        JSONObject childJson=new JSONObject();
                        childJson.put("name",child.getName());
                        childJson.put("id",child.getID());
                        childJson.put("uri",child.getUri());
                        childJson.put("order",child.getSn());
                        childJson.put("type",child.getType());
                        childrenJson.add(childJson);
                    }
                    item.put("child",childrenJson);
                }
                array.add(item);
            }
        }
        return array;
    }

}
