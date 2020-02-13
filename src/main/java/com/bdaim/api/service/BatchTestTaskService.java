package com.bdaim.api.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.api.dao.ApiDao;
import com.bdaim.api.entity.ApiEntity;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ResourceService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customs.entity.*;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/***
 * api批量测试任务
 */
@Service("busi_b_test_task")
@Transactional
public class BatchTestTaskService implements BusiService {
    private static Logger log = LoggerFactory.getLogger(BatchTestTaskService.class);

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
        String apiId = info.getString("apiId");
        String sql = "select count(0) from " + HMetaDataDef.getTable(busiType, "")+" where cust_id=? and ext_2=?";
        List param = new ArrayList();
        param.add(cust_id);
        param.add(apiId);
        Integer num = jdbcTemplate.queryForObject(sql, param.toArray(), Integer.class);
        log.info("num="+num);
        if(num!=null && num>0){
            throw new Exception("该客户["+cust_id+"]已测试过api["+apiId+"]，不能重复创建");
        }
        //根据企业id查询企业账号和企业名称
        String enterpriseName = customerDao.getEnterpriseName(cust_id);
        CustomerUser custUser = customerUserDao.selectPropertyByType(1,cust_id );
        if (custUser!=null){
            info.put("account", custUser.getAccount());
        }
        ApiEntity apiEntity = apiDao.getApi(info.getIntValue("apiId"));
        if(apiEntity==null || apiEntity.getStatus()!=2){
            throw new Exception("API["+apiId+"]不存在或未发布");
        }else{
            info.put("apiName",apiEntity.getName());
            info.put("ext_4",apiEntity.getName());
        }
        info.put("custName", enterpriseName);
        info.put("ext_2",apiId);
        //名称
        info.put("ext_5", enterpriseName);
        //任务状态
        info.put("ext_1","0");
        info.put("status", "0");
        //
        info.put("usedNum", 0);
        //info.put("create_date",System.currentTimeMillis());

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
        //info.put("usedNum", 0);
        if("del".equals(info.getString("_rule_"))){
            info.put("ext_1",-1);//删除
            info.put("status",-1);
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
                " ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=? and ext_1 <> '-1' ");
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
                sqlParams.add(params.get(key));
            }else if("custName".equals(key)){
                sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key + "')=?");
                sqlParams.add(params.get(key));
            }else if("account".equals(key)){
                sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key + "')=?");
                sqlParams.add(params.get(key));
            }

        }
        return sqlstr.toString();
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }



}
