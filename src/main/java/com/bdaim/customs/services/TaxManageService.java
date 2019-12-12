package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.ParseHzXml;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.List;

/**
 * 纳税单汇总
 */
@Service("busi_tax_manage")
public class TaxManageService implements BusiService {
    private static Logger log = LoggerFactory.getLogger(TaxManageService.class);
    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private ParseHzXml parseHzXml;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        log.info(info.toJSONString());
        info.put("ext_1", info.getString("status"));
        String xmlString = info.getString("xmlstring");
        if (StringUtil.isEmpty(xmlString)) {
            throw new TouchException("纳税单汇总回执内容不能为空");
        }
        byte[] s = Base64.getDecoder().decode(xmlString);
        handleHzInfo(cust_id, new String(s), info, id);
        log.info("纳税单汇总回执处理完毕");
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {

    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {

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
     * 处理回执纳税详情
     * 1.解析回执到回执表，把xml文件解析成json，存入回执表，字段: 主单号，分单号，备注
     * 2.回写回执状态到报关单，添加预处理id和海关编号
     * 3.按回执状态等级写入消息表
     * 4.源字符串写入mongodb
     */
    public void handleHzInfo(String custId, String xmlstring, JSONObject info, Long id) throws Exception {

        parseHzXml.parserTaxManageXML(xmlstring, info);
        JSONObject envelopData = info.getJSONObject("envelopinfo");//分单信息
        JSONObject data = info.getJSONObject("data");//分单信息
        JSONArray dutyjsonList =  info.getJSONArray("dutyjson");//分单信息
        JSONObject entryjson = info.getJSONObject("entryjson");//分单信息
        info.clear();
        info.putAll(envelopData);
        info.putAll(data);
        info.putAll(entryjson);
        info.put("dutyjson",dutyjsonList);

//        String sql = "insert into h_data_manager_tax_detail (id,type,content,cust_id,create_date,ext_1,ext_2) " +
//                "value(" + id + ",'" + BusiTypeEnum.TAX_DETAIL.getType() + "','" + data.toJSONString() + "','" + custId + "',now(),'" + data.getString("billno") + "','" + data.getString("ass_billno") + "')";
//        jdbcTemplate.update(sql);
    }
}
