package com.bdaim.rs.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.be.service.BusiEntityService;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.DicTypeEnum;
import com.bdaim.common.entity.DicProperty;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.service.ResourceService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 公共资源服务
 */
@RestController
@RequestMapping("/rs/{resourceType}")
public class ResourceController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(ResourceController.class);

    @Autowired
    private ResourceService resourceService;

    /**
     * 按多条件查询
     */
    @ResponseBody
    @RequestMapping(value = "/all", method = RequestMethod.POST)
    public ResponseInfo query(@RequestBody(required=false) String body, @PathVariable(name = "resourceType") String resourceType) {
    	ResponseInfo resp = new ResponseInfo();
    	
    	JSONObject params = null;
    	try {
    		if(body==null || "".equals(body))
    			body="{}";
    		params = JSONObject.parseObject(body);
    	}catch(Exception e) {
    		return new ResponseInfoAssemble().failure(-1, "查询条件解析异常["+resourceType+"]");
    	}
    	
        try {
        	LoginUser lu = opUser();
        	String user_id = lu.getUser_id();
            if ("admin".equals(lu.getRole())) {
                user_id = "";
            }
        	resp.setData(resourceService.query(user_id, resourceType, params));
        } catch (Exception e) {
            logger.error("查询资源异常:"+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询资源异常["+resourceType+"]");
        }
        return resp;
    }

    
    /**
     * 保存资源，目前只开放给运营管理员
     */
    @ResponseBody
    @RequestMapping(value = "/info/{id}", method = RequestMethod.POST)
    public ResponseInfo saveInfo(@PathVariable(name = "id", required=false) Long id, @RequestBody(required=false) String body, @PathVariable(name = "resourceType") String resourceType) {
    	ResponseInfo resp = new ResponseInfo();
    	JSONObject info = null;
    	try {
    		if(body==null || "".equals(body))
    			body="{}";
    		info = JSONObject.parseObject(body);
    	}catch(Exception e) {
    		return new ResponseInfoAssemble().failure(-1, "资源解析异常:["+resourceType+"]");
    	}
    	
        try {
        	LoginUser lu = opUser();
        	//if(!lu.getAuthorities().contains("admin"))
            if(!lu.getRole().contains("admin"))
        		return new ResponseInfoAssemble().failure(-1, "无权限保存资源:["+resourceType+"]");

        	String user_id = lu.getUser_id();
        	
        	id = resourceService.saveInfo(user_id, resourceType, id, info);
        	resp.setData(id);
        } catch (Exception e) {
            logger.error("保存资源异常:"+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "保存资源异常:["+resourceType+"]");
        }
        return resp;
    }
    
    /**
     * 根据id唯一标识获取资源
     *
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseInfo getInfo(@PathVariable(name = "id") Long id, @PathVariable(name = "resourceType") String resourceType) {
    	ResponseInfo resp = new ResponseInfo();
    	
        try {
        	LoginUser lu = opUser();
        	String user_id = lu.getUser_id();
        	
        	JSONObject jo = resourceService.getInfo(user_id, resourceType, id);
        	resp.setData(jo);
        } catch (Exception e) {
            logger.error("获取资源异常:"+id+" "+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询资源异常["+resourceType+"]");
        }
        return resp;
    }
    
    /**
     * 根据id唯一标识删除资源
     *
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseInfo deleteInfo(@PathVariable(name = "id") Long id, @PathVariable(name = "resourceType") String resourceType) {
    	ResponseInfo resp = new ResponseInfo();
        try {
        	LoginUser lu = opUser();
        	if(!lu.getRole().contains("admin"))
        		return new ResponseInfoAssemble().failure(-1, "无权限保存资源:["+resourceType+"]");
        	
        	String user_id = lu.getUser_id();
        	
        	resourceService.deleteInfo(user_id, resourceType, id);
        } catch (Exception e) {
            logger.error("删除资源异常:"+id+" "+ e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "删除资源异常["+resourceType+"]");
        }
        return resp;
    }
    
}
