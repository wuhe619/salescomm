package com.bdaim.common.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.service.BusiEntityService;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.services.ExportExcelService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

/**
 * 业务实体服务
 */
@RestController
@RequestMapping("/be/{busiType}")
public class BusiEntityController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(BusiEntityController.class);

    @Autowired
    private BusiEntityService busiEntityService;

    @Autowired
    private ExportExcelService exportExcelService;

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
        	Long cust_user_id = lu.getId();
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
        	Long cust_user_id = lu.getId();
        	
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
     */
    @ResponseBody
    @RequestMapping(value = "/{id}", method = RequestMethod.GET)
    public ResponseInfo getInfo(@PathVariable(name = "id") Long id, @RequestBody(required = false) String body, @PathVariable(name = "busiType") String busiType, HttpServletResponse response) {
        ResponseInfo resp = new ResponseInfo();
        JSONObject param = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";

            param = JSONObject.parseObject(body);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "记录解析异常:[" + busiType + "]");
        }
        try {
        	LoginUser lu = opUser();
        	String cust_id = lu.getCustId();
        	String cust_group_id = lu.getUserGroupId();
        	Long cust_user_id = lu.getId();
            JSONObject jo = busiEntityService.getInfo(cust_id, cust_group_id, cust_user_id, busiType, id, param);
            // 导出直接下载文件
            if (StringUtil.isNotEmpty(param.getString("_rule_")) && param.getString("_rule_").startsWith("_export_")) {
                List list = null;
                if (jo.getInteger("export_type") != null && jo.getInteger("export_type").intValue() == 2) {
                    list = new ArrayList();
                    list.add(jo);
                }
                // 低价商品
                if("_export_low_product".equals(param.getString("_rule_"))){
                    list = jo.getJSONArray("low_price_goods");
                }
                exportExcelService.exportExcel(jo.getInteger("id"), list, param.getString("_rule_"), response);
                return null;
            }
            resp.setData(jo);
        } catch (Exception e) {
            logger.error("获取记录异常:" + id + " " + e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "查询记录异常[" + busiType + "]");
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
        	Long cust_user_id = lu.getId();
        	
        	busiEntityService.deleteInfo(cust_id, cust_group_id, cust_user_id, busiType, id);
        } catch (Exception e) {
            logger.error("删除记录异常:"+id+" "+ e.getMessage());
            return new ResponseInfoAssemble().failure(-1, "删除记录异常["+busiType+"]");
        }
        return resp;
    }
    
}
