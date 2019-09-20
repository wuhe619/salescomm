package com.bdaim.common.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.service.BusiEntityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

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
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	if(cust_id==null || "".equals(cust_id))
        		cust_id="-1";
        	if(lu.getRole().contains("admin") || lu.getRole().contains("ROLE_USER"))
        		cust_id="all";
        	
        	resp.setData(busiEntityService.query(cust_id, cust_group_id, cust_user_id, busiType, params));
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
    public ResponseInfo saveInfo(@PathVariable(name = "id", required=false) Long id, @RequestBody(required=false) String body, @PathVariable(name = "busiType") String busiType) {
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
        	if(lu.getCustId()==null || "".equals(lu.getCustId()))
        		return new ResponseInfoAssemble().failure(-1, "无归属企业，不能保存记录:["+busiType+"]");
        	
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	
            id = busiEntityService.saveInfo(cust_id, cust_group_id, cust_user_id, busiType, id, info);
            resp.setData(id);
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
    public ResponseInfo getInfo(@PathVariable(name = "id") Long id, @PathVariable(name = "busiType") String busiType) {
    	ResponseInfo resp = new ResponseInfo();
    	
        try {
        	LoginUser lu = opUser();
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	
        	JSONObject jo = busiEntityService.getInfo(cust_id, cust_group_id, cust_user_id, busiType, id);
        	resp.setData(jo);
        } catch (Exception e) {
            logger.error("获取记录异常:"+id+" "+e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询记录异常["+busiType+"]");
        }
        return resp;
    }
    
    /**
     * 根据id唯一标识删除记录
     *
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.DELETE)
    public ResponseInfo deleteInfo(@PathVariable(name = "id") Long id, @PathVariable(name = "busiType") String busiType) {
    	ResponseInfo resp = new ResponseInfo();
        try {
        	LoginUser lu = opUser();
        	if(lu.getCustId()==null || "".equals(lu.getCustId()))
        		return new ResponseInfoAssemble().failure(-1, "无归属企业，不能删除记录:["+busiType+"]");
        	
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	String cust_user_id = lu.getUser_id();
        	
        	busiEntityService.deleteInfo(cust_id, cust_group_id, cust_user_id, busiType, id);
        } catch (Exception e) {
            logger.error("删除记录异常:"+id+" "+ e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "删除记录异常["+busiType+"]");
        }
        return resp;
    }
    
}
