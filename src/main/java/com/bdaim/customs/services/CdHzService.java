package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.util.ParseHzXml;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 舱单回执
 */
@Service("busi_cd_hz")
@Transactional
public class CdHzService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(CdHzService.class);

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private ParseHzXml parseHzXml;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        log.info(info.toJSONString());
        info.put("ext_1",info.getString("status"));
        String xmlString=info.getString("xmlstring");
        if(StringUtil.isEmpty(xmlString)){
            throw new TouchException("舱单回执内容不能为空");
        }
        byte[] s = Base64.getDecoder().decode(xmlString);
        handleHzInfo(new String (s),info);

        log.info("舱单回执：");
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

    /**
     * 处理回执
     * 1.解析回执到回执表，把xml文件解析成json，存入回执表，字段: 主单号，分单号，备注
     * 2.回写回执状态到舱单主单，舱单分单
     * 3.舱单回执状态等级写入消息表
     *
     */
    public void handleHzInfo(String xmlstring,JSONObject info) throws Exception {
        parseHzXml.parserCangdanHzXML(xmlstring,info);
        JSONObject envelopinfo = info.getJSONObject("envelopinfo");//信封消息
        JSONObject headData = info.getJSONObject("headData");//主单信息
        JSONArray array = info.getJSONArray("list");//分单信息




    }


}
