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
import com.bdaim.common.service.BusiEntityService;
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
 * 业务实体服务
 */
@RestController
@RequestMapping("/be/{busiType}")
public class BusiEntityController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(BusiEntityController.class);

    @Autowired
    private BusiEntityService busiEntityService;

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
        	String user_id = lu.getUser_id();
        	
        	resp.setData(busiEntityService.query(cust_id, user_id, busiType, params));
        } catch (Exception e) {
            logger.error("查询记录异常:"+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询记录异常["+busiType+"]");
        }
        return resp;
    }

    
    /**
     * 保存记录
     */
    @ResponseBody
    @RequestMapping(value = "/info/{id}", method = RequestMethod.POST)
    public ResponseInfo saveInfo(@PathVariable(name = "id", required=false) String id, @RequestBody(required=false) String body, @PathVariable(name = "busiType") String busiType) {
    	ResponseInfo resp = new ResponseInfo();
    	JSONObject info = null;
    	try {
    		if(body==null || "".equals(body))
    			body="{}";
    		info = JSONObject.parseObject(body);
    	}catch(Exception e) {
    		return new ResponseInfoAssemble().failure(-1, "记录解析异常:["+busiType+"]");
    	}
    	
        try {
        	LoginUser lu = opUser();
        	String cust_id = lu.getCustId();
        	String user_id = lu.getUser_id();
        	
            busiEntityService.saveInfo(cust_id, user_id, busiType, id, info);
        } catch (Exception e) {
            logger.error("保存记录异常:"+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "保存记录异常:["+busiType+"]");
        }
        return resp;
    }
    
    /**
     * 根据id唯一标识获取记录
     *
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseInfo getInfo(@PathVariable(name = "id") String id, @PathVariable(name = "busiType") String busiType) {
    	ResponseInfo resp = new ResponseInfo();
    	
        try {
        	LoginUser lu = opUser();
        	String cust_id = lu.getCustId();
        	String user_id = lu.getUser_id();
        	
        	JSONObject jo = busiEntityService.getInfo(cust_id, user_id, busiType, id);
        	resp.setData(jo);
        } catch (Exception e) {
            logger.error("获取记录异常:"+id+" "+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询记录异常["+busiType+"]");
        }
        return resp;
    }
    
    /**
     * 根据id唯一标识获取记录
     *
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseInfo deleteInfo(@PathVariable(name = "id") String id, @PathVariable(name = "busiType") String busiType) {
    	ResponseInfo resp = new ResponseInfo();
        try {
        	LoginUser lu = opUser();
        	String cust_id = lu.getCustId();
        	String user_id = lu.getUser_id();
        	
        	busiEntityService.deleteInfo(cust_id, user_id, busiType, id);
        } catch (Exception e) {
            logger.error("删除记录异常:"+id+" "+ e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "删除记录异常["+busiType+"]");
        }
        return resp;
    }
    
}
