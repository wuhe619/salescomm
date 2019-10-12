package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 舱单回执
 */
@Service("busi_cd_hz")
@Transactional
public class CdHzService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(BgdHzService.class);

    private ServiceUtils serviceUtils;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        log.info(info.toJSONString());
        info.put("ext_1",info.getString("status"));
        String xmlString=info.getString("xmlstring");
        Map map = serviceUtils.xmlToMap(xmlString);
        log.info("报关单回执："+map);
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {

    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) throws TouchException {
        //通过舱单主单ID查询海关回执数据
        HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id,id, busiType);
        if(dbManager==null){
            throw new TouchException("无权操作");
        }
        String content = null;
        if (dbManager != null) {
            content = dbManager.getContent();
        }
        if (StringUtil.isNotEmpty(content)) {
            JSONObject json = JSONObject.parseObject(content);
            Iterator keys = json.keySet().iterator();
            // 海关回执存入舱单数据中
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (!info.containsKey(key)) {
                    info.put(key, json.get(key));
                }
            }
        }
    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {

    }
}
