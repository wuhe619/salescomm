package com.bdaim.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.auth.service.TokenCacheService;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.DicTypeEnum;
import com.bdaim.common.entity.DicProperty;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.service.BusiFileService;

import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * 业务文件服务
 */
@RestController
@RequestMapping("/bf/{busiType}")
public class BusiFileController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(BusiFileController.class);

    @Autowired
    private BusiFileService busiFileService;

    /**
     * 按多条件查询
     */
    @ResponseBody
    @RequestMapping(value = "/all", method = RequestMethod.POST)
    public ResponseInfo query(@RequestBody(required=false) String body, @PathVariable(name = "busiType") String busiType) {
    	ResponseInfo resp = new ResponseInfo();
    	
    	JSONObject params = null;
    	try {
    		if(body==null || "".equals(body))
    			body="{}";
    		params = JSONObject.parseObject(body);
    	}catch(Exception e) {
    		return new ResponseInfoAssemble().failure(-1, "查询条件解析异常["+busiType+"]");
    	}
    	
        try {
        	LoginUser lu = opUser();
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	if(cust_id==null || "".equals(cust_id))
        		cust_id="-1";
        	if(lu.getAuthorities().contains("admin") || lu.getAuthorities().contains("ROLE_USER"))
        		cust_id="all";
        	
        	resp.setData(busiFileService.query(cust_id, cust_group_id, cust_user_id, busiType, params));
        } catch (Exception e) {
            logger.error("查询文件异常:"+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询文件异常["+busiType+"]");
        }
        return resp;
    }

    
    /**
     * 保存文件
     */
    @ResponseBody
    @RequestMapping(value = "/info/{id}", method = RequestMethod.POST)
    public ResponseInfo saveInfo(@PathVariable(name = "id", required=false) Long id, @RequestBody(required=false) String body, @PathVariable(name = "busiType") String busiType, @RequestParam("file") MultipartFile file) {
    	ResponseInfo resp = new ResponseInfo();
    	JSONObject info = null;
    	try {
    		if(body==null || "".equals(body))
    			body="{}";
    		info = JSONObject.parseObject(body);
    	}catch(Exception e) {
    		return new ResponseInfoAssemble().failure(-1, "文件解析异常:["+busiType+"]");
    	}
    	
        try {
        	LoginUser lu = opUser();
        	if(lu.getCustId()==null || "".equals(lu.getCustId()))
        		return new ResponseInfoAssemble().failure(-1, "无归属企业，不能保存文件:["+busiType+"]");
        	
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	
            id = busiFileService.saveInfo(cust_id, cust_group_id, cust_user_id, busiType, id, info, file);
            resp.setData(id);
        } catch (Exception e) {
            logger.error("保存文件异常:"+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "保存文件异常:["+busiType+"]");
        }
        return resp;
    }
    
    /**
     * 根据id唯一标识获取文件
     *
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseInfo getInfo(@PathVariable(name = "id") Long id, @PathVariable(name = "busiType") String busiType) {
    	ResponseInfo resp = new ResponseInfo();
    	
        try {
        	LoginUser lu = opUser();
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	
        	JSONObject jo = busiFileService.getInfo(cust_id, cust_group_id, cust_user_id, busiType, id);
        	resp.setData(jo);
        } catch (Exception e) {
            logger.error("获取文件异常:"+id+" "+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询文件异常["+busiType+"]");
        }
        return resp;
    }
    
    /**
     * 根据id唯一标识获取文件
     *
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseInfo deleteInfo(@PathVariable(name = "id") Long id, @PathVariable(name = "busiType") String busiType) {
    	ResponseInfo resp = new ResponseInfo();
        try {
        	LoginUser lu = opUser();
        	if(lu.getCustId()==null || "".equals(lu.getCustId()))
        		return new ResponseInfoAssemble().failure(-1, "无归属企业，不能删除文件:["+busiType+"]");
        	
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	
        	busiFileService.deleteInfo(cust_id, cust_group_id, cust_user_id, busiType, id);
        } catch (Exception e) {
            logger.error("删除文件异常:"+id+" "+ e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "删除文件异常["+busiType+"]");
        }
        return resp;
    }
    
}
