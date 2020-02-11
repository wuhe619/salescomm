package com.bdaim.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.api.dao.ApiDao;
import com.bdaim.api.entity.ApiEntity;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * api批量测试详情
 */
@Service("busi_b_test_task_x")
@Transactional
public class BatchTestTaskXService implements BusiService {
    private static Logger log = LoggerFactory.getLogger(BatchTestTaskXService.class);

    @Autowired
    private CustomerDao customerDao;
    @Autowired
    private CustomerUserDao customerUserDao;

    @Autowired
    private ApiDao apiDao;

    @Resource
    private JdbcTemplate jdbcTemplate;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
        String sql = " select content from "+HMetaDataDef.getTable(busiType, "")+" where id=?";
        Map<String,Object> detailObj = jdbcTemplate.queryForMap(sql,id);
        if(detailObj != null) {
            String contentStr = (String) detailObj.get("content");
            if (StringUtil.isNotEmpty(contentStr)) {
                JSONObject json = JSON.parseObject(contentStr);
                json.put("response",info.getString("response"));
                info = json;
                info.put("ext_2",1);
                String batchid = info.getString("batchId");
                sql = "select content from "+HMetaDataDef.getTable(BusiTypeEnum.BATCH_TEST_TASK_Z.getType(), "")+" where id=?";
                Map<String,Object> batchObj = jdbcTemplate.queryForMap(sql,batchid);
                if(batchObj!=null) {
                    String batchObjStr = (String) batchObj.get("content");
                    if (StringUtil.isNotEmpty(batchObjStr)) {
                        JSONObject json2 = JSON.parseObject(batchObjStr);
                        Integer successNum = json2.getInteger("successNum");
                        Integer totalNum = json2.getInteger("totalNum");
                        if(successNum == null){
                            successNum = 0;
                        }
                        successNum += 1;
                        json2.put("successNum",successNum);
                        String updateSql = "update "+HMetaDataDef.getTable(BusiTypeEnum.BATCH_TEST_TASK_Z.getType(), "") +" set content=? where id=?";
                        if(successNum == totalNum){
                            json2.put("status",1);
                            updateSql = "update "+HMetaDataDef.getTable(BusiTypeEnum.BATCH_TEST_TASK_Z.getType(), "") +" set content=?,ext_date1=now() where id=?";
                        }
                        jdbcTemplate.update(updateSql,json2.toJSONString(),batchid);
                    }
                }
            }
        }
    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        sqlParams.clear();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3," +
                " ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=? ");
        sqlParams.add(busiType);
        if (!"all".equals(cust_id)){
            sqlParams.add(cust_id);
            sqlstr.append(" and cust_id=? ");
        }
        Iterator keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (StringUtil.isNotEmpty(String.valueOf(params.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key) || "pid1".equals(key) || "pid2".equals(key))
                continue;
            if ("cust_id".equals(key)) {
                sqlstr.append(" and cust_id=?");
            }
            if("batch_id".equals(key)){
                sqlstr.append(" and ext_4=?");
            }
            if("status".equals(key)){
                sqlstr.append(" and ext_2=?");
            }
            sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key + "')=?");
            sqlParams.add(params.get(key));
        }
        return sqlstr.toString();
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }



}
