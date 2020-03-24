package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.api.entity.CheckData;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.ResourceService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customs.entity.*;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.BigDecimalUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.util.*;

/***
 * api巡检任务
 */
@Service("busi_patrol_task")
@Transactional
public class PatrolTaskService implements BusiService {
    private static Logger log = LoggerFactory.getLogger(PatrolTaskService.class);

    @Autowired
    private CustomerDao customerDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ServiceUtils serviceUtils;

    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        String taskName = info.getString("taskName");
        if(StringUtil.isEmpty(taskName)){
            throw new TouchException("任务名称必填");
        }
        String taskRule = info.getString("taskRule");
        if(StringUtil.isEmpty(taskRule)){
            throw new TouchException("任务规则必填");
        }
        JSONObject taskruleObj = JSON.parseObject(taskRule);
        if(!taskruleObj.containsKey("unit") || !taskruleObj.containsKey("amount")
                || StringUtil.isEmpty(taskruleObj.getString("unit")) || StringUtil.isEmpty(taskruleObj.getString("amount"))){
            throw new TouchException("任务规则参数必填");
        }
        info.put("status",1);
        info.put("ext_1",1); //状态 0:暂停 1：运行 -1：删除

        //入到api资源库
        String apisStr = info.getString("apis");
        if(StringUtil.isNotEmpty(apisStr)){
            saveDetail(apisStr,id.toString(),cust_id,cust_user_id);
        }
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws TouchException {
        /*if("DEL".equals(info.getString("_rule_"))){//删除
            String delSql = "update "+ HMetaDataDef.getTable(busiType, "")+" set ext_1='-1' where id=? and type=?";
            customerDao.executeUpdateSQL(delSql,id,busiType);
            info.put("status","-1");
        }else if("RUNNING".equals(info.getString("_rule_"))){ //运行
            String runningSql = "update "+ HMetaDataDef.getTable(busiType, "")+" set ext_1='1' where id=? and type=?";
            customerDao.executeUpdateSQL(runningSql,id,busiType);
            info.put("status","-1");
        }else if("STOP".equals(info.getString("_rule_"))){ //停止
            String runningSql = "update "+ HMetaDataDef.getTable(busiType, "")+" set ext_1='0' where id=? and type=?";
            customerDao.executeUpdateSQL(runningSql,id,busiType);
            info.put("status","-1");
        }*/
        info.put("ext_1",info.getString("status"));
    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {
        return;
    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {
        return;
    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        //查询主列表
        if ("main".equals(params.getString("_rule_"))) {
            sqlParams.clear();
            StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");
            if (!"all".equals(cust_id)){
                sqlParams.add(cust_id);
                sqlstr.append(" and cust_id=? ");
            }
            sqlParams.add(busiType);

            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (StringUtil.isNotEmpty(String.valueOf(params.get(key)))) continue;
                if ("pageNum".equals(key) || "pageSize".equals(key) || "pid1".equals(key) || "pid2".equals(key) || "_sort_".equals(key) || "_orderby_".equals(key))
                    continue;
                if ("cust_id".equals(key)) {
                    sqlstr.append(" and cust_id=?");
                } else if (key.endsWith(".c")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(0, key.length() - 2) + "') like '%?%'");
                } else if (key.endsWith(".start")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(0, key.length() - 6) + "') >= ?");
                } else if (key.endsWith(".end")) {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key.substring(0, key.length() - 6) + "') <= ?");
                } else {
                    sqlstr.append(" and JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$." + key + "')=?");
                }
                sqlParams.add(params.get(key));
            }
            sqlstr.append(" and ext_1 in('0','1') ");

            return sqlstr.toString();
        }
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }

    /**
     * 保存api信息
     * @param apisStr
     * @param pid
     * @param cust_id
     * @param cust_user_id
     */
    public void saveDetail(String apisStr,String pid, String cust_id, Long cust_user_id){
        JSONArray array = JSON.parseArray(apisStr);
        if(array.size()>0) {
            List<HBusiDataManager> d = new ArrayList<>();
            for (int i = 0; i < array.size(); i++) {
                JSONObject obj = array.getJSONObject(i);
                HBusiDataManager hm = new HBusiDataManager();
                hm.setCust_id(Long.valueOf(cust_id));
                hm.setCust_user_id(cust_user_id.toString());
                hm.setExt_1(pid);
                hm.setContent(obj.toJSONString());
                hm.setCreateDate(new Date());
                hm.setType(BusiTypeEnum.PATROL_TASK_APIS.getType());
                hm.setCreateId(cust_user_id);
                try {
                    hm.setId(sequenceService.getSeq(BusiTypeEnum.PATROL_TASK_APIS.getType()));
                } catch (Exception e) {
                    e.printStackTrace();
                }
                d.add(hm);
            }
            if (d.size() > 0) {
                serviceUtils.batchInsert(BusiTypeEnum.PATROL_TASK_APIS.getType(), d);
            }
        }
    }

}
