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
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.annotation.Resource;
import javax.transaction.Transactional;

/**
 *  通用业务文件服务
 */
@Service
@Transactional
public class BusiFileService {
    private static Logger logger = LoggerFactory.getLogger(BusiFileService.class);
    
    @Resource
    private JdbcTemplate jdbcTemplate;
    
    @Autowired
    private SequenceService sequenceService;

    /*
     * 按ID获取文件
     */
    public JSONObject getInfo(String cust_id, String cust_group_id, String cust_user_id, String busiType, Long id) throws Exception{
    	JSONObject jo = null;
    	
    	String sql = "select content, cust_id, cust_group_id, cust_user_id, create_id, create_date, update_id, update_date,file_type,file_name,file_size,file_id,ext_1 from f_file where type=? and id=? ";
    	if(!"all".equals(cust_id))
    		sql+=" and cust_id='"+cust_id+"'";
    	
    	Map data = jdbcTemplate.queryForMap(sql, busiType, id);
    	if(jo==null)
    		return jo;
    	String content = (String)data.get("content");
    	try {
    		jo = JSONObject.parseObject(content);
    		jo.put("id", id);
    		jo.put("cust_id", data.get("cust_id"));
    		jo.put("cust_group_id", data.get("cust_group_id"));
    		jo.put("cust_user_id", data.get("cust_user_id"));
    		jo.put("create_id", data.get("create_id"));
    		jo.put("create_date", data.get("create_date"));
    		jo.put("update_id", data.get("update_id"));
    		jo.put("update_date", data.get("update_date"));
    		jo.put("file_name", data.get("file_name"));
    		jo.put("file_type", data.get("file_type"));
    		jo.put("file_size", data.get("file_size"));
    		jo.put("file_id", data.get("file_id"));
    		if(data.get("ext_1")!=null && !"".equals(data.get("ext_1")))
    			jo.put("ext_1", data.get("ext_1"));
    		
    		//执行自定义单数据规则
    		BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
			busiService.getInfo(busiType, cust_id, cust_group_id, cust_user_id, id, jo);
    	}catch(Exception e) {
    		logger.error(e.getMessage());
    		throw new Exception("数据格式错误！");
    	}
    	
    	return jo;
    }
    
    /*
     * 查询文件
     */
    public Page query(String cust_id, String cust_group_id, String cust_user_id, String busiType, JSONObject params) throws Exception{
    	Page p = new Page();
    	
    	List sqlParams =  new ArrayList();
    	
    	BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
    	String sql = null;
    	try {
    		//执行自定义查询sql
    		sql = busiService.formatQuery(busiType, cust_id, cust_group_id, cust_user_id, params, sqlParams);
    	}catch(Exception e) {
    		logger.error(e.getMessage());
    		throw new Exception("查询条件自定义解析异常:["+busiType+"]");
    	}
    	if(sql==null || "".equals(sql)) {
    		sqlParams.clear();
	    	StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date, file_name, file_type, file_size, file_id, ext_1 from f_file where type=?");
	    	if(!"all".equals(cust_id))
	    		sqlstr.append(" and cust_id='").append(cust_id).append("'");
	    	
	    	sqlParams.add(busiType);
	    	
	    	Iterator keys = params.keySet().iterator();
	    	while(keys.hasNext()) {
	    		String key = (String)keys.next();
	    		if("cust_id".equals(key)) {
	    			sqlstr.append(" and cust_id=?");
	    		}else if(key.endsWith(".c")) {
	    			sqlstr.append(" and JSON_EXTRACT(content, $."+key.substring(0, key.length()-2)+") like '%?%'");
	    		}else {
	    			sqlstr.append(" and JSON_EXTRACT(content, $."+key+")=?");
	    		}
	
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
	    		JSONObject jo = null;
	    		try {
		    		if(m.containsKey("content")) {
			    		jo = JSONObject.parseObject((String)m.get("content"));
			    		jo.put("id", m.get("id"));
			    		jo.put("cust_id", m.get("cust_id"));
			    		jo.put("cust_group_id", m.get("cust_group_id"));
			    		jo.put("cust_user_id", m.get("cust_user_id"));
			    		jo.put("create_id", m.get("create_id"));
			    		jo.put("create_date", m.get("create_date"));
			    		jo.put("update_id", m.get("update_id"));
			    		jo.put("update_date", m.get("update_date"));
			    		jo.put("file_name", m.get("file_name"));
			    		jo.put("file_type", m.get("file_type"));
			    		jo.put("file_size", m.get("file_size"));
			    		jo.put("file_id", m.get("file_id"));
			    		if(m.get("ext_1")!=null && !"".equals(m.get("ext_1")))
			    			jo.put("ext_1", m.get("ext_1"));
		    		}else
		    			jo = JSONObject.parseObject(JSONObject.toJSONString(m));
	    		}catch(Exception e) {
	    			logger.error(e.getMessage());
	    		}
	    		if(jo==null) { //jo异常导致为空时，只填充id
	    			jo = new JSONObject();
	    			jo.put("id", m.get("id"));
	    		}
	    		
	    		try {
	    			//执行自定义查询结果格式化
	    			busiService.formatInfo(busiType, cust_id, cust_group_id, cust_user_id, jo);
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
     * 保存文件
     */
    public Long saveInfo(String cust_id, String cust_group_id, String cust_user_id, String busiType, Long id, JSONObject info, MultipartFile file) throws Exception{
    	String file_name = file.getName();
    	String file_type = file.getContentType();
    	Long file_size = file.getSize();
    	String file_id = "";
    	
    	if(id==null || "".equals(id) || "0".equals(id)) {
    		//insert
    		id = sequenceService.getSeq(busiType);
    		
    		String sql2 = "insert into f_file(id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date, file_name, file_type, file_size, file_id) value(?, ?, ?, ?, ?, ?, ?, now())";
    		try {
    			//执行自定义新增规则
    			BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
    			busiService.insertInfo(busiType, cust_id, cust_group_id, cust_user_id, id, info);
    			
    			Iterator ifks = info.keySet().iterator();
    	    	while(ifks.hasNext()) {
    	    		String key = (String)ifks.next();
    	    		if("id".equals(key) || "cust_id".equals(key) || "create_id".equals(key) || "create_date".equals(key) || key.startsWith("rule.")) //关键字冲突
    	    			info.remove(key);
    	    	}
    	    	
    			jdbcTemplate.update(sql2, id, busiType, info.toJSONString(), cust_id, cust_group_id, cust_user_id, cust_user_id, file_name, file_type, file_size, file_id);
    		}catch(Exception e) {
    			logger.error(e.getMessage());
    			throw new Exception("添加新文件异常:["+busiType+"]");
    		}
    	}else{
    		// update
    		String sql1 = "select content from f_file where type=? and cust_id=? and id=?";
        	Map data = null;
        	try {
        		data = jdbcTemplate.queryForMap(sql1, busiType, cust_group_id, cust_user_id, id);
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
        	
    		String sql2 = "update f_file set content=?,update_id=?,update_date=now(),file_name=?,file_type=?,file_size=?,file_id=? where type=? and cust_id=? and id=?";
    		
    		try {
    			//执行自定义更新规则
    			BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
    			busiService.updateInfo(busiType, cust_id, cust_group_id, cust_user_id, id, jo);
    			
    			Iterator ifks = jo.keySet().iterator();
    	    	while(ifks.hasNext()) {
    	    		String key = (String)ifks.next();
    	    		if("id".equals(key) || "cust_id".equals(key) || "create_id".equals(key) || "create_date".equals(key) || key.startsWith("rule.")) //关键字冲突
    	    			jo.remove(key);
    	    	}
    	    	
    			jdbcTemplate.update(sql2, jo.toJSONString(), cust_user_id, file_name, file_type, file_size, file_id,  busiType, cust_id, id);
    		}catch(Exception e) {
    			logger.error(e.getMessage());
    			throw new Exception("更新文件异常:["+busiType+"]"+id);
    		}
    	}
    	
    	return id;
    }
    
    /**
     * 删除文件
     */
    public void deleteInfo(String cust_id, String cust_group_id, String cust_user_id, String busiType, Long id) throws Exception{
    	String sql = "delete from f_file where type=? and cust_id=? and id=?";
    	try {
    		//执行自定义删除规则
    		BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+busiType);
			busiService.deleteInfo(busiType, cust_id, cust_group_id, cust_user_id, id);
    		
    		jdbcTemplate.update(sql, busiType, cust_id, id);
    		
    	}catch(Exception e) {
    		logger.error(e.getMessage());
    		throw new Exception("删除文件异常:["+busiType+"]"+id);
    	}
    }

}
