package com.bdaim.customs.utils;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.Constants;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.TResourceLog;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.supplier.dto.SupplierEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Component
public class ServiceUtils {

    private static Logger log = LoggerFactory.getLogger(ServiceUtils.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private SequenceService sequenceService;
    @Autowired
    private SourceDao sourceDao;

    public void addDataToES(String id, String type, JSONObject content) {
        if (type.equals(BusiTypeEnum.SZ.getType())) {
            elasticSearchService.addDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.CZ.getType())){
            elasticSearchService.addDocumentToType(Constants.CZ_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.BZ.getType())){
            elasticSearchService.addDocumentToType(Constants.BZ_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.SF.getType())) {
            elasticSearchService.addDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id, content);
        }else if( type.equals(BusiTypeEnum.CF.getType())){
            elasticSearchService.addDocumentToType(Constants.CF_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.BF.getType())){
            elasticSearchService.addDocumentToType(Constants.BF_INFO_INDEX, "haiguan", id,content);
        }else if (type.equals(BusiTypeEnum.SS.getType())) {
            elasticSearchService.addDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.CS.getType())){
            elasticSearchService.addDocumentToType(Constants.CS_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.BS.getType())){
            elasticSearchService.addDocumentToType(Constants.BS_INFO_INDEX, "haiguan", id, content);
        }
    }

    /**
     * 更新es
     * @param type
     * @param id
     * @param content
     */
    public void updateDataToES(String type,String id,JSONObject content) {
        if (type.equals(BusiTypeEnum.SZ.getType())) {
            elasticSearchService.updateDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.CZ.getType())){
            elasticSearchService.updateDocumentToType(Constants.CZ_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.BZ.getType())){
            elasticSearchService.updateDocumentToType(Constants.BZ_INFO_INDEX, "haiguan", id, content);
        } else if (type.equals(BusiTypeEnum.SF.getType())) {
            elasticSearchService.updateDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id, content);
        }else if( type.equals(BusiTypeEnum.CF.getType())){
            elasticSearchService.updateDocumentToType(Constants.CF_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.BF.getType())){
            elasticSearchService.updateDocumentToType(Constants.BF_INFO_INDEX, "haiguan", id, content);
        }else if (type.equals(BusiTypeEnum.SS.getType())) {
            elasticSearchService.updateDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.CS.getType())){
            elasticSearchService.updateDocumentToType(Constants.CS_INFO_INDEX, "haiguan", id, content);
        }else if(type.equals(BusiTypeEnum.BS.getType())){
            elasticSearchService.updateDocumentToType(Constants.BS_INFO_INDEX, "haiguan", id, content);
        }

    }

    /**
     * 从es删除文档
     *
     * @param type
     * @param id
     */
    public void deleteDatafromES(String type, String id) {
        if (type.equals(BusiTypeEnum.SZ.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.SZ_INFO_INDEX, "haiguan", id);
        }else if(type.equals(BusiTypeEnum.CZ.getType())){
            elasticSearchService.deleteDocumentFromType(Constants.CZ_INFO_INDEX, "haiguan", id);
        }else if(type.equals(BusiTypeEnum.BZ.getType())){
            elasticSearchService.deleteDocumentFromType(Constants.BZ_INFO_INDEX, "haiguan", id);
        } else if (type.equals(BusiTypeEnum.SF.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.SF_INFO_INDEX, "haiguan", id);
        }else if( type.equals(BusiTypeEnum.CF.getType())){
            elasticSearchService.deleteDocumentFromType(Constants.CF_INFO_INDEX, "haiguan", id);
        }else if(type.equals(BusiTypeEnum.BF.getType())){
            elasticSearchService.deleteDocumentFromType(Constants.BF_INFO_INDEX, "haiguan", id);
        }else if (type.equals(BusiTypeEnum.SS.getType())) {
            elasticSearchService.deleteDocumentFromType(Constants.SS_INFO_INDEX, "haiguan", id);
        }else if(type.equals(BusiTypeEnum.CS.getType())){
            elasticSearchService.deleteDocumentFromType(Constants.CS_INFO_INDEX, "haiguan", id);
        }else if(type.equals(BusiTypeEnum.BS.getType())){
            elasticSearchService.deleteDocumentFromType(Constants.BS_INFO_INDEX, "haiguan", id);
        }
    }


    public HBusiDataManager getObjectByIdAndType(Long id, String type){
        String sql="select * from h_data_manager where id="+id+" and type='"+type+"'";
        RowMapper<HBusiDataManager> managerRowMapper=new BeanPropertyRowMapper<>(HBusiDataManager.class);
        return jdbcTemplate.queryForObject(sql,managerRowMapper);
    }

    public void delDataListByPid(Long pid){
        String sql="delete from h_data_manager where JSON_EXTRACT(content, '$.pid')=" + pid + " or JSON_EXTRACT(content, '$.pid')='"+pid+"'";
        jdbcTemplate.execute(sql);
    }

    public List<HBusiDataManager> getDataList(String type,Long pid){
        String sql2 = "select * from h_data_manager where  type='"+type+"' and (JSON_EXTRACT(content, '$.pid')="+pid +" or JSON_EXTRACT(content, '$.pid')='"+pid+"')";
        RowMapper<HBusiDataManager> managerRowMapper=new BeanPropertyRowMapper<>(HBusiDataManager.class);
        return jdbcTemplate.query(sql2,managerRowMapper);
    }

    public void delDataListByIdAndType(Long id,String type){
        String sql="delete from h_data_manager where type='"+type+"' and id="+id;
        jdbcTemplate.execute(sql);
    }

    public void insertSFVerifyQueue(String content, String billNo, long userId){
        TResourceLog queue = new TResourceLog();
        queue.setContent(content);
        queue.setBusiId(billNo);
        queue.setCustUserId(userId);
        queue.setBatchId(CipherUtil.encodeByMD5(IDHelper.getID().toString()));
        queue.setCreateTime(new Timestamp(System.currentTimeMillis()));
        queue.setBusiType(1);
        queue.setSupplierId(SupplierEnum.ZAX.getSupplierId());
        MarketResourceEntity resourceId = sourceDao.getResourceId(SupplierEnum.ZAX.getSupplierId(), ResourceEnum.CHECK_IDCARD.getType());
        if(resourceId!=null){
            queue.setResourceId(resourceId.getResourceId());
        }
        hBusiDataManagerDao.saveOrUpdate(queue);
    }

    public List<Map<String, Object>> listObjectByParam(String busiType, String cust_id, JSONObject params){
        List sqlParams = new ArrayList();
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=?");
        if (!"all".equals(cust_id))
            sqlstr.append(" and cust_id='").append(cust_id).append("'");

        sqlParams.add(busiType);

        Iterator keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if("".equals(String.valueOf(params.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key)) continue;
            if ("cust_id".equals(key)) {
                sqlstr.append(" and cust_id=?");
            } else if (key.startsWith("_c_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(3) + "') like concat('%',?,'%')");
            } else if (key.startsWith("_g_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(3) + "') > ?");
            } else if (key.startsWith("_ge_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(4) + "') >= ?");
            } else if (key.startsWith("_l_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(3) + "') < ?");
            } else if (key.startsWith("_le_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(4) + "') <= ?");
            }else if (key.startsWith("_eq_")) {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(4) + "') = ?");
            }else if (key.startsWith("_range_")) {
                if ("0".equals(String.valueOf(params.get(key)))) {
                    sqlstr.append(" and ( JSON_EXTRACT(content, '$." + key.substring(7) + "') <= ?")
                            .append(" OR JSON_EXTRACT(content, '$." + key.substring(7) + "') = '' ")
                            .append(" OR JSON_EXTRACT(content, '$." + key.substring(7) + "') IS NULL ) ");
                } else {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(7) + "') >= ?");
                }
            }  else {
                sqlstr.append(" and JSON_EXTRACT(content, '$." + key + "')=?");
            }

            sqlParams.add(params.get(key));
        }
        List<Map<String, Object>> ds = jdbcTemplate.queryForList(sqlstr.toString(), sqlParams.toArray());
        return ds;
    }
}
