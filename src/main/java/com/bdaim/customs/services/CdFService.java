package com.bdaim.customs.services;

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
import java.util.Iterator;
import java.util.List;

/***
 * 舱单.分单
 */
@Service("busi_cd_f")
@Transactional
public class CdFService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(CdFService.class);

    @Autowired
    private CustomerDao customerDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private SequenceService sequenceService;


    @Autowired
    private ServiceUtils serviceUtils;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) {
        // TODO Auto-generated method stub

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        // TODO Auto-generated method stub
        HBusiDataManager fM = serviceUtils.getObjectByIdAndType(cust_id,id, busiType);
        if (fM == null) {
            throw new TouchException("仓单分单[" + id + "] 不存在");
        }
        JSONObject json = JSONObject.parseObject(fM.getContent());

        Iterator keys = info.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            json.put(key, info.get(key));
        }
        serviceUtils.updateDataToES(busiType, id.toString(), json);
        List<HBusiDataManager> list = null;
        try {
            list = serviceUtils.listDataByParentBillNo(cust_id,busiType,fM.getExt_4());
        } catch (Exception e) {
            log.error("查询舱单分单异常", e);
            throw new Exception();
        }
        int packNo = 0;
        float weight = 0;
        JSONObject json2 = JSONObject.parseObject(fM.getContent());
        for (HBusiDataManager hBusiDataManager : list) {
            if (hBusiDataManager.getId() == id.intValue()) {
                if (info.containsKey("pack_no") && StringUtil.isNotEmpty(info.getString("pack_no"))) {
                    packNo += info.getInteger("pack_no");
                }
                if (info.containsKey("weight") && StringUtil.isNotEmpty(info.getString("weight"))) {
                    weight += info.getFloatValue("weight");
                }
            } else {
                if (json2.containsKey("pack_no") && StringUtil.isNotEmpty("pack_no")) {
                    System.out.println("pack_no: " + json2.getString("pack_no"));
                    packNo += json2.getInteger("pack_no");
                }
                if (json2.containsKey("weight_total") && StringUtil.isNotEmpty(json2.getString("weight_total"))) {
                    System.out.println("weight_total: " + json2.getString("weight_total"));
                    weight += json2.getFloatValue("weight_total");
                }
            }

        }
        log.info("jieshu...");
        HBusiDataManager cangdanz = serviceUtils.getObjectByIdAndType(cust_id,json.getLong("pid"), BusiTypeEnum.CZ.getType());
        if(cangdanz==null){
            throw new TouchException("无权操作");
        }
        JSONObject superObj = JSONObject.parseObject(cangdanz.getContent());
        superObj.put("weight", weight);
        superObj.put("total_pack_no", packNo);
        cangdanz.setContent(superObj.toJSONString());
        String sql = "update "+ HMetaDataDef.getTable(cangdanz.getType(),"")+" set " +
                " content='"+superObj.toJSONString()+"'"+
                " where id="+cangdanz.getId()+" and type='"+cangdanz.getType()+"'";
        jdbcTemplate.update(sql);
        //hBusiDataManagerDao.saveOrUpdate(cangdanz);

        serviceUtils.updateDataToES(BusiTypeEnum.CZ.getType(), cangdanz.getId().toString(), superObj);
    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {
        // TODO Auto-generated method stub

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) {
        // TODO Auto-generated method stub

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {
        // TODO Auto-generated method stub

    }


}
