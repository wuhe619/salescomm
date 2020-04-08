package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
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
import java.util.*;

/***
 * 巡检任务api资源
 */
@Service("busi_patrol_task_apis")
@Transactional
public class PatrolTaskApisService implements BusiService {
    private static Logger log = LoggerFactory.getLogger(PatrolTaskApisService.class);

    @Autowired
    private CustomerDao customerDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private ServiceUtils serviceUtils;

    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        String apiId = info.getString("apiId");
        info.put("ext_1",apiId);
        String taskId = info.getString("taskId");
        info.put("ext_2",taskId);

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws TouchException {

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
        //查询列表
        sqlParams.clear();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from " + HMetaDataDef.getTable(busiType, "") + " where type=?");

        sqlParams.add(busiType);

        Iterator keys = params.keySet().iterator();
        while(keys.hasNext()) {
            String key = (String) keys.next();
            if (StringUtil.isEmpty(String.valueOf(params.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key)  || "_sort_".equals(key) || "_orderby_".equals(key))
                continue;
            if ("cust_id".equals(key) && !"all".equals(cust_id)) {
                sqlstr.append(" and cust_id=?");
            }else if("taskId".equals(key)){
                sqlstr.append(" and ext_2 =? ");
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

        return sqlstr.toString();

    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        String apiId = info.getString("apiId");
        info.put("apiName","");
        try {
            if (StringUtil.isNotEmpty(apiId)) {
                String sql = "select api_name from am_api where id=?";
                List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, apiId);
                if (list != null && list.size() > 0) {
                    Map<String, Object> map = list.get(0);
                    if (map != null && map.containsKey("api_name")) {
                        info.put("apiName", map.get("api_name"));
                    }
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
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
