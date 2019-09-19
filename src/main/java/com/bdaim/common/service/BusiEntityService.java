package com.bdaim.common.service;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dao.DicDao;
import com.bdaim.common.dao.SettlementDao;
import com.bdaim.common.dto.Page;
import com.bdaim.common.entity.Dic;
import com.bdaim.common.entity.DicProperty;
import com.bdaim.fund.entity.Settlement;
import com.bdaim.fund.entity.SettlementProperty;
import com.bdaim.log.service.OperLogService;
import com.bdaim.common.util.*;
import com.bdaim.common.util.spring.SpringContextHelper;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.rbac.dao.UserDao;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.transaction.Transactional;

/**
 *  通用业务实体服务
 */
@Service
@Transactional
public class BusiEntityService {
    private static Logger logger = LoggerFactory.getLogger(BusiEntityService.class);
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private SequenceService sequenceService;

    /*
     * 按ID获取记录
     */
    public JSONObject getInfo(String cust_id, String user_id, String busiType, String id) throws Exception{
    	JSONObject d = null;
    	
    	String sql = "select content, cust_id, create_id, create_date from h_data_manager where type=? and id=? ";
    	sql+=" and cust_id='"+cust_id+"'";
    	
    	Map data = jdbcTemplate.queryForMap(sql, busiType, id);
    	if(d==null)
    		return d;
    	String content = (String)data.get("content");
    	try {
    		d = JSONObject.parseObject(content);
    		
    		BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
			busiService.getInfo(busiType, cust_id, user_id, id, d);
    	}catch(Exception e) {
    		logger.error(e.getMessage());
    		throw new Exception("数据格式错误！");
    	}
    	
    	return d;
    }
    
    /*
     * 查询记录
     */
    public Page query(String cust_id, String user_id, String busiType, JSONObject params) throws Exception{
    	Page p = new Page();
    	
    	List sqlParams =  new ArrayList();
    	
    	BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
    	String sql = null;
    	try {
    		sql = busiService.formatQuery(busiType, cust_id, user_id, params, sqlParams);
    	}catch(Exception e) {
    		logger.error(e.getMessage());
    		throw new Exception("查询条件自定义解析异常:["+busiType+"]");
    	}
    	if(sql==null || "".equals(sql)) {
    		sqlParams.clear();
	    	StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5, ext_6, ext_7, ext_8, ext_9, ext_10 from h_data_manager where type=?");
	    	sqlstr.append(" and cust_id='").append(cust_id).append("'");
	    	
	    	sqlParams.add(busiType);
	    	
	    	Iterator keys = params.keySet().iterator();
	    	while(keys.hasNext()) {
	    		String key = (String)keys.next();
	    		if(key.contains(".op"))
	    			continue;
	    		sqlstr.append(" and JSON_EXTRACT(content, $."+key+") = ?");
	    		if(params.containsKey(key+".op") && "c".equals(params.get(key+".op")))
	    			sqlstr.append(" like '%?%'");
	    		else
	    			sqlstr.append("=?");
	
	    		sqlParams.add(params.get(key));   
	    	}
	    	sql = sqlstr.toString();
    	}
    	
    	int pageNum = 1; 
        int pageSize = 10; 
        try {
        	pageNum = params.getIntValue("pageNum");
        }catch(Exception e) {}
        try {
        	pageSize = params.getIntValue("pageSize");
        }catch(Exception e) {}
        if(pageNum<=0)
        	pageNum = 1;
        if(pageSize<=0)
        	pageSize = 10;
        if(pageSize>1000)
        	pageSize = 1000;
        
        try {
	        List<Map<String,Object>> ds = jdbcTemplate.queryForList(sql+" limit "+(pageNum-1)*pageSize+", "+pageSize, sqlParams.toArray());
	        List data = new ArrayList();
	    	for(int i=0;i<ds.size();i++) {
	    		Map m = (Map)ds.get(i);
	    		JSONObject jo = JSONObject.parseObject((String)m.get("content"));
	    		jo.put("id", m.get("id"));
	    		jo.put("create_id", m.get("create_id"));
	    		jo.put("create_date", m.get("create_date"));
	    		jo.put("cust_id", m.get("cust_id"));
	    		if(m.get("ext_1")!=null && !"".equals(m.get("ext_1")))
	    			jo.put("ext_1", m.get("ext_1"));
	    		if(m.get("ext_2")!=null && !"".equals(m.get("ext_2")))
	    			jo.put("ext_2", m.get("ext_2"));
	    		if(m.get("ext_3")!=null && !"".equals(m.get("ext_3")))
	    			jo.put("ext_3", m.get("ext_3"));
	    		if(m.get("ext_4")!=null && !"".equals(m.get("ext_4")))
	    			jo.put("ext_4", m.get("ext_4"));
	    		if(m.get("ext_5")!=null && !"".equals(m.get("ext_5")))
	    			jo.put("ext_5", m.get("ext_5"));
	    		if(m.get("ext_6")!=null && !"".equals(m.get("ext_6")))
	    			jo.put("ext_6", m.get("ext_6"));
	    		if(m.get("ext_7")!=null && !"".equals(m.get("ext_7")))
	    			jo.put("ext_7", m.get("ext_7"));
	    		if(m.get("ext_18")!=null && !"".equals(m.get("ext_8")))
	    			jo.put("ext_8", m.get("ext_8"));
	    		if(m.get("ext_9")!=null && !"".equals(m.get("ext_9")))
	    			jo.put("ext_9", m.get("ext_9"));
	    		if(m.get("ext_10")!=null && !"".equals(m.get("ext_10")))
	    			jo.put("ext_10", m.get("ext_10"));
	    		
	    		try {
	    			busiService.formatInfo(busiType, cust_id, user_id, jo);
	    		}catch(Exception e) {
	    			logger.error(e.getMessage());
	    		}
	    		
	    		data.add(jo);
	    	}
	    	p.setData(data);
	    	p.setTotal(data.size());
	    	p.setPerPageCount(pageSize);
	    	p.setStart((pageNum-1)*pageSize+1);
        }catch(Exception e) {
        	logger.error(e.getMessage());
        	throw new Exception("查询异常:["+busiType+"]");
        }
    	
    	return p;
    }
    
    /*
     * 保存记录
     */
    public String saveInfo(String cust_id, String user_id, String busiType, String id, JSONObject info) throws Exception{
    	Iterator ifks = info.keySet().iterator();
    	while(ifks.hasNext()) {
    		String key = (String)ifks.next();
    		if("id".equals(key) || "cust_id".equals(key) || "create_id".equals(key) || "create_date".equals(key) || key.startsWith("op.")) //关键字冲突
    			info.remove(key);
    	}
    	
    	if(id==null || "".equals(id) || "0".equals(id)) {
    		//insert
    		Long nid = sequenceService.getSeq(busiType);
    		
    		String sql2 = "insert into h_data_manager(id, type, content, cust_id, create_id, create_date) value(?, ?, ?, ?, ?, now())";
    		try {
    			BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
    			busiService.insertInfo(busiType, cust_id, user_id, id, info);
    			
    			
    			jdbcTemplate.update(sql2, nid, busiType, info.toJSONString(), cust_id, user_id);
    		}catch(Exception e) {
    			logger.error(e.getMessage());
    			throw new Exception("添加新记录异常:["+busiType+"]");
    		}
    	}else{
    		// update
    		String sql1 = "select content, cust_id, create_id, create_date from h_data_manager where type=? and cust_id=? and id=?";
        	Map data = null;
        	try {
        		data = jdbcTemplate.queryForMap(sql1, busiType, cust_id, Long.parseLong(id));
        	}catch(Exception e) {
        		throw new Exception("读取数据异常:["+busiType+"]"+id);
        	}
        	if(data==null) {
        		throw new Exception("数据不存在:["+busiType+"]"+id);
        	}
        	
        	String content = (String)data.get("content");
        	if(content==null || "".equals(content))
        		content = "{}";
        	
        	JSONObject jo = null;
        	try {
	        	jo = JSONObject.parseObject(content);
	        	Iterator keys = info.keySet().iterator();
	        	while(keys.hasNext()) {
	        		String key = (String)keys.next();
	        		jo.put(key, info.get(key));
	        	}
        	}catch(Exception e) {
        		logger.error(e.getMessage());
        		throw new Exception("解析数据异常:["+busiType+"]"+id);
        	}
        	
    		String sql2 = "update h_data_manager set content=? where type=? and cust_id=? and id=?";
    		
    		try {
    			BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
    			busiService.updateInfo(busiType, cust_id, user_id, id, jo);
    			
    			jdbcTemplate.update(sql2, jo.toJSONString(), busiType, cust_id, Long.parseLong(id));
    		}catch(Exception e) {
    			logger.error(e.getMessage());
    			throw new Exception("更新记录异常:["+busiType+"]"+id);
    		}
    	}
    	
    	return id;
    }
    
    /**
     * 删除记录
     */
    public void deleteInfo(String cust_id, String user_id, String busiType, String id) throws Exception{
    	String sql = "delete from h_data_manager where type=? and cust_id=? and id=?";
    	try {
    		BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
			busiService.deleteInfo(busiType, cust_id, user_id, id);
    		
    		jdbcTemplate.update(sql, busiType, cust_id, Long.parseLong(id));
    		
    	}catch(Exception e) {
    		logger.error(e.getMessage());
    		throw new Exception("删除记录异常:["+busiType+"]"+id);
    	}
    }

}
