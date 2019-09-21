package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

/***
 * 申报单.分单
 */
@Service("busi_sbd_f")
public class SbdFService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(SbdFService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;
    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private SequenceService sequenceService;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        Integer pid = info.getInteger("pid");
        String billNo = info.getString("bill_no");
        if(pid==null){
            log.error("主单id不能为空");
            throw new Exception("主单id不能为空");
        }
        if(StringUtil.isEmpty(billNo)){
            log.error("分单号不能为空");
            throw new Exception("分单号不能为空");
        }
        HBusiDataManager sbdzd = getObjectByIdAndType(pid.longValue(),BusiTypeEnum.SZ.getType());
        List<HBusiDataManager> list = getDataList(pid.longValue());
        if(list!=null && list.size()>0){
            for(HBusiDataManager hBusiDataManager:list){
                if(billNo.equals(hBusiDataManager.getExt_3())){
                    log.error("分单号【"+billNo+"】在主单【"+pid+"】中已经存在");
                    throw new Exception("分单号【"+billNo+"】在主单【"+pid+"】中已经存在");
                }
            }
        }
        info.put("type", BusiTypeEnum.SF.getType());
        info.put("check_status", "0");
        info.put("idcard_pic_flag", "0");
        info.put("main_gname","");
        info.put("low_price_goods",0);
        info.put("id",id);
        info.put("pid",pid);
        addDataToES(id.toString(),busiType,info);
        JSONObject jsonObject = JSONObject.parseObject(sbdzd.getContent());
        if(info.containsKey("weight") && info.getString("weight")!=null){
          if(jsonObject.containsKey("weight_total")) {
              String  weight_total = jsonObject.getString("weight_total");
              if(StringUtil.isNotEmpty(weight_total)){
                  weight_total=String.valueOf(Float.valueOf(weight_total)+Float.valueOf(info.getString("weight")));
                  jsonObject.put("weight_total", weight_total);//总重量
              }
          }
        }
        int value = 1;
        if(jsonObject.containsKey("party_total")){
            value = jsonObject.getInteger("party_total")+value;
        }
        jsonObject.put("party_total", value);//分单总数

        sbdzd.setContent(jsonObject.toJSONString());
        hBusiDataManagerDao.saveOrUpdate(sbdzd);
        updateDataToES(BusiTypeEnum.SZ.getType(),sbdzd.getId().toString(),jsonObject);
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
        // 身份核验
        if ("verification".equals(info.getString("rule.do"))) {
            StringBuffer sql = new StringBuffer("select id from h_data_manager where type=?")
                    .append(" and cust_id='").append(cust_id).append("'")
                    .append(" and id =? ");
            List sqlParams = new ArrayList();
            sqlParams.add(busiType);
            sqlParams.add(id);
            Map<String, Object> map = jdbcTemplate.queryForMap(sql.toString(), sqlParams.toArray());
            if (map != null && map.size() > 0) {
                String updateSql = "UPDATE h_data_manager SET ext_7 = 3 WHERE id =? ";
                jdbcTemplate.update(updateSql, map.get("id"));
            }

        }else if ("clear_verify".equals(info.getString("rule.do"))) {
            // 清空身份证件图片
            List ids = info.getJSONArray("ids");
            List<HBusiDataManager> hBusiDataManagers = hBusiDataManagerDao.listHBusiDataManager(ids, BusiTypeEnum.SF.getType());
            if (hBusiDataManagers != null) {
                JSONObject jsonObject;
                String picKey = "ID_NO_PIC";
                HBusiDataManager mainD = null;
                for (HBusiDataManager d : hBusiDataManagers) {
                    d.setExt_6("");
                    jsonObject = JSON.parseObject(d.getContent());
                    if (jsonObject != null) {
                        // 身份证照片存储对象ID
                        jsonObject.put(picKey, "");
                        d.setContent(jsonObject.toJSONString());
                    }
                    hBusiDataManagerDao.saveOrUpdate(d);
                    elasticSearchService.update(d, d.getId());
                    mainD = hBusiDataManagerDao.getHBusiDataManager("ext_3", d.getExt_4());
                }
                if (mainD != null) {
                    updateMainDanIdCardNumber(mainD.getId());
                }
            }

        }
    }

    /**
     * 更新申报单主单身份证照片数量
     *
     * @param mainId
     * @return
     */
    private int updateMainDanIdCardNumber(int mainId) {
        int idCardNumber = hBusiDataManagerDao.countMainDIdCardNum(mainId, BusiTypeEnum.SF.getType());
        log.info("开始更新主单:{}的身份证照片数量:{}", mainId, idCardNumber);
        int code = 0;

        JSONObject mainDetail = elasticSearchService.getDocumentById(Constants.SF_INFO_INDEX, "haiguan", String.valueOf(mainId));
        if (mainDetail == null) {
            HBusiDataManager param = new HBusiDataManager();
            param.setId(NumberConvertUtil.parseInt(mainId));
            param.setType(BusiTypeEnum.SZ.getType());
            HBusiDataManager h = hBusiDataManagerDao.get(param);
            if (h != null && h.getContent() != null) {
                mainDetail = JSON.parseObject(h.getContent());
            }
        }
        if (mainDetail != null) {
            mainDetail.put("id", mainId);
        }
        if (mainDetail != null && mainDetail.containsKey("id")) {
            mainDetail.put("idCardNumber", idCardNumber);
            HBusiDataManager param = new HBusiDataManager();
            param.setId(mainId);
            param.setType(BusiTypeEnum.SZ.getType());
            HBusiDataManager mainD = hBusiDataManagerDao.get(param);
            if (mainD != null) {
                mainD.setContent(mainDetail.toJSONString());
                hBusiDataManagerDao.update(mainD);
                elasticSearchService.update(mainD, mainId);
            }
            code = 1;
        }
        return code;
    }

    @Override
    public void getInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {
        String sql="select id,type,content,ext_1,ext_2,ext_3,ext_4 from h_data_manager where id="+id +" and type='"+busiType+"'";
        HBusiDataManager manager = jdbcTemplate.queryForObject(sql,HBusiDataManager.class);
        if (manager.getCust_id() == null || (!cust_id.equals(manager.getCust_id().toString()))) {
            throw new Exception("无权删除");
        }
        List<HBusiDataManager> list = getDataList(id);
        for (HBusiDataManager manager2 : list) {
            deleteDatafromES(manager2.getType(), manager2.getId().toString());
        }
        delDataListByPid(id);
        deleteDatafromES(manager.getType(), manager.getId().toString());
        JSONObject json = JSONObject.parseObject(manager.getContent());
        Integer zid=json.getInteger("pid");
        totalPartDanToMainDan(zid, BusiTypeEnum.SZ.getType(),id);

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        String sql = null;
        //查询主列表
        if ("main".equals(params.getString("rule.do"))) {
            sqlParams.clear();
            StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from h_data_manager where type=?");
            if (!"all".equals(cust_id))
                sqlstr.append(" and cust_id='").append(cust_id).append("'");

            sqlParams.add(busiType);

            Iterator keys = params.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if ("pageNum".equals(key) || "pageSize".equals(key) || "pid1".equals(key) || "pid2".equals(key))
                    continue;
                if ("cust_id".equals(key)) {
                    sqlstr.append(" and cust_id=?");
                } else if (key.endsWith(".c")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 2) + "') like '%?%'");
                } else if (key.endsWith(".start")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 6) + "') >= ?");
                } else if (key.endsWith(".end")) {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key.substring(0, key.length() - 6) + "') <= ?");
                } else {
                    sqlstr.append(" and JSON_EXTRACT(content, '$." + key + "')=?");
                }

                sqlParams.add(params.get(key));
            }
            String verify_status = params.getString("verify_status");
            String verify_photo = params.getString("verify_photo");
            // 身份校验状态
            if (StringUtil.isNotEmpty(verify_status)) {
                if ("3".equals(verify_status)) {
                    sqlstr.append(" and ( ext_7 IS NULL OR ext_7='' OR ext_7 =3 ");
                }
            }
            //身份图片状态
            if (StringUtil.isNotEmpty(verify_photo)) {
                if ("1".equals(verify_photo)) {
                    sqlstr.append(" and ext_6 IS NOT NULL ");
                } else if ("2".equals(verify_photo)) {
                    sqlstr.append(" and (ext_6 IS NULL OR ext_6='') ");
                }

            }
            return sqlstr.toString();
        }
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }

    private void addDataToES(String id,String type,JSONObject content) {
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

    public HBusiDataManager getObjectByIdAndType(Long id,String type){
        String sql="select * from h_data_manager where id="+id+" and type='"+type+"'";
        return jdbcTemplate.queryForObject(sql,HBusiDataManager.class);
    }

    public void delDataListByPid(Long pid){
        String sql="delete from h_data_manager where JSON_EXTRACT(content, '$.pid')="+pid;
        jdbcTemplate.execute(sql);
    }

    public List<HBusiDataManager> getDataList(Long pid){
        String sql2 = "select type,id,content from h_data_manager where  JSON_EXTRACT(content, '$.pid')="+pid;
        return jdbcTemplate.queryForList(sql2,HBusiDataManager.class);
    }

    /**
     * 重新统计主单content
     * @param zid
     * @param type
     * @param id
     */
    public void totalPartDanToMainDan(Integer zid, String type,Long id) {

        List<HBusiDataManager> data = getDataList(zid.longValue());
        Float weightTotal = 0f;
        Integer low_price_goods=0;
        for (HBusiDataManager d : data) {
            if(d.getId()==id.intValue())continue;

            String content = d.getContent();
            JSONObject json = JSONObject.parseObject(content);
            Integer s=json.getInteger("low_price_goods");
            if(s==null)s=0;
            low_price_goods += s;
            String WEIGHT = json.getString("weight");
            if (StringUtil.isEmpty(WEIGHT)) {
                WEIGHT = "0";
            }
            weightTotal += Float.valueOf(WEIGHT);
        }

        String sql="select id,type,content,ext_1,ext_2,ext_3,ext_4 from h_data_manager where id="+zid +" and type='"+type+"'";
        HBusiDataManager manager = jdbcTemplate.queryForObject(sql,HBusiDataManager.class);

        String hcontent = manager.getContent();
        JSONObject jsonObject = JSONObject.parseObject(hcontent);
        jsonObject.put("weight_total", weightTotal);//总重量
        jsonObject.put("party_total", data.size()-1<0?0:data.size()-1);//分单总数
        Integer s=jsonObject.getInteger("low_price_goods");
        if(s==null){
            s=0;
        }
        jsonObject.put("low_price_goods",s + low_price_goods);
        manager.setContent(jsonObject.toJSONString());
        sql = " update h_data_manager set content='"+jsonObject.toJSONString()+"' where id="+zid+" and type='"+type+"'";
        jdbcTemplate.update(sql);
        updateDataToES(BusiTypeEnum.SZ.getType(), zid.toString(),jsonObject);

    }


    /**
     * 更新es
     * @param type
     * @param id
     * @param content
     */
    private void updateDataToES(String type,String id,JSONObject content) {
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
    private void deleteDatafromES(String type, String id) {
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

}
