package com.bdaim.common.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 *  通用业务实体服务
 */
@Service("rsService")
@Transactional
public class ResourceService {
    private static Logger logger = LoggerFactory.getLogger(ResourceService.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;

    /*
     * 按ID获取资源
     */
    public JSONObject getInfo(String user_id, String resourceType, Long id) throws Exception{
    	JSONObject d = null;

    	String sql = "select content, create_id, create_date, update_id, update_date from h_resource where type=? and id=? ";

    	Map data = jdbcTemplate.queryForMap(sql, resourceType, id);
    	if(d==null)
    		return d;
    	String content = (String)data.get("content");
    	try {
    		d = JSONObject.parseObject(content);

    	}catch(Exception e) {
    		logger.error(e.getMessage());
    		throw new Exception("数据格式错误！");
    	}

    	return d;
    }

    /*
     * 查询资源
     */
    public Page query(String user_id, String resourceType, JSONObject params) throws Exception{
    	Page p = new Page();

    	List sqlParams =  new ArrayList();

    	//BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+resourceType);
    	String sql = null;

    	if(sql==null || "".equals(sql)) {
    		sqlParams.clear();
	    	StringBuffer sqlstr = new StringBuffer("select id, content, create_id, create_date from h_resource where type=?");

	    	sqlParams.add(resourceType);

	    	Iterator keys = params.keySet().iterator();
	    	while(keys.hasNext()) {
	    		String key = (String)keys.next();
	    		if (!"pageNum".equals(key) && !"pageSize".equals(key)){
                    if(key.endsWith(".c")) {
                        sqlstr.append(" and JSON_EXTRACT(content, '$."+key.substring(0, key.length()-2)+"') like '%?%'");
                    }else {
                        sqlstr.append(" and JSON_EXTRACT(content, '$."+key+"')=?");
                    }

                    sqlParams.add(params.get(key));
                }
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
	    		if(m.containsKey("content")) {
	    			jo =JSONObject.parseObject((String)m.get("content"));
	    			jo.put("id", m.get("id"));
		    		jo.put("create_id", m.get("create_id"));
		    		jo.put("create_date", m.get("create_date"));
	    		}else
	    			jo = JSONObject.parseObject(JSONObject.toJSONString(m));

	    		data.add(jo);
	    	}
	    	p.setData(data);
	    	p.setTotal(data.size());
	    	p.setPerPageCount(pageSize);
	    	p.setStart((pageNum-1)*pageSize+1);
        }catch(Exception e) {
        	logger.error(e.getMessage());
        	throw new Exception("查询异常:["+resourceType+"]");
        }

    	return p;
    }

    /*
     * 保存资源
     */
    public Long saveInfo(String user_id, String resourceType, Long id, JSONObject info) throws Exception{
    	if(id==null || id==0) {
    		//insert
    		id = sequenceService.getSeq(resourceType);

    		String sql2 = "insert into h_resource(id, type, content, create_id, create_date) value(?, ?, ?, ?, now())";
    		try {
//    			BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+resourceType);
//    			busiService.insertInfo(resourceType, cust_id, user_id, id, info);

    	    	Iterator ifks = info.keySet().iterator();
    	    	while(ifks.hasNext()) {
    	    		String key = (String)ifks.next();
    	    		if("id".equals(key) || "create_id".equals(key) || "create_date".equals(key) || key.startsWith("rule.")) //关键字冲突
    	    			info.remove(key);
    	    	}

    			jdbcTemplate.update(sql2, id, resourceType, info.toJSONString(), user_id);
    		}catch(Exception e) {
    			logger.error(e.getMessage());
    			throw new Exception("添加新资源异常:["+resourceType+"]");
    		}
    	}else{
    		// update
    		String sql1 = "select content from h_resource where type=? and id=?";
        	Map data = null;
        	try {
        		data = jdbcTemplate.queryForMap(sql1, resourceType, id);
        	}catch(Exception e) {
        		throw new Exception("读取数据异常:["+resourceType+"]"+id);
        	}
        	if(data==null) {
        		throw new Exception("数据不存在:["+resourceType+"]"+id);
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
        		throw new Exception("解析数据异常:["+resourceType+"]"+id);
        	}

    		String sql2 = "update h_resource set content=?, update_id=?, update_date=now() where type=? and id=?";

    		try {
//    			BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+resourceType);
//    			busiService.updateInfo(resourceType, cust_id, user_id, id, jo);

    			jdbcTemplate.update(sql2, jo.toJSONString(), user_id, resourceType, id);
    		}catch(Exception e) {
    			logger.error(e.getMessage());
    			throw new Exception("更新资源异常:["+resourceType+"]"+id);
    		}
    	}

    	return id;
    }

    /**
     * 删除资源
     */
    public void deleteInfo(String user_id, String resourceType, Long id) throws Exception{
    	String sql = "delete from h_resource where type=? and id=?";
    	try {
//    		BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_"+resourceType);
//			busiService.deleteInfo(resourceType, cust_id, user_id, id);

    		jdbcTemplate.update(sql, resourceType, id);
    	}catch(Exception e) {
    		logger.error(e.getMessage());
    		throw new Exception("删除资源异常:["+resourceType+"]"+id);
    	}
    }

}
