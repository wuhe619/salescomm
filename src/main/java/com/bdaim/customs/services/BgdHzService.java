package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dao.FileDao;
import com.bdaim.common.entity.HFile;
import com.bdaim.common.entity.HFilePK;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.MongoFileService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Base64;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 报关单回执
 */
@Service("busi_bgd_hz")
public class BgdHzService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(BgdHzService.class);

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private ParseHzXml parseHzXml;

    @Autowired
    private MongoFileService mongoFileService;

    @Autowired
    private FileDao fileDao;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        log.info(info.toJSONString());
        info.put("ext_1",info.getString("status"));
        String xmlString=info.getString("xmlstring");
        if(StringUtil.isEmpty(xmlString)){
            throw new TouchException("报关单回执内容不能为空");
        }
        byte[] s = Base64.getDecoder().decode(xmlString);
        handleHzInfo(cust_id,new String(s),info);
        log.info("报关单回执处理完毕");
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {

    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) {
        //通过报关单分单ID查询海关回执数据
        HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id,id, busiType);
        String content = null;
        if (dbManager != null) {
            content = dbManager.getContent();
        }
        if (StringUtil.isNotEmpty(content)) {
            JSONObject json = JSONObject.parseObject(content);
            Iterator keys = json.keySet().iterator();
            // 海关回执存入报关单数据中
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
     * 2.回写回执状态到报关单，添加预处理id和海关编号
     * 3.按回执状态等级写入消息表
     * 4.源字符串写入mongodb
     */
    public void handleHzInfo(String custId,String xmlstring,JSONObject info) throws Exception {
        parseHzXml.parserBaoguandanHzXML(xmlstring,info);
        JSONObject envelopinfo = info.getJSONObject("envelopinfo");//信封消息
        JSONObject data = info.getJSONObject("data");//分单信息
//        info.remove("xmlstring");
        info.put("ext_4",data.getString("billno"));//主单号
        info.put("ext_3",data.getString("ass_billno"));//分单号
        String EntryId = data.getString("entryid");//海关编码
        String PreEntryId = data.getString("pre_entryid");//预录入号

        HBusiDataManager fendan = serviceUtils.findFendanByBillNo(custId, BusiTypeEnum.BF.getType(),data.getString("billno"),data.getString("ass_billno"));

        if(fendan==null){
            throw new TouchException("分单【"+data.getString("billno")+"-"+data.getString("ass_billno")+"】不存在");
        }

        String content = fendan.getContent();
        JSONObject json = JSONObject.parseObject(content);
        json.put("pre_input_code",PreEntryId==null?"":PreEntryId);
        json.put("entryid",EntryId==null?"":EntryId);
        String opresult = data.getString("op_result");
        json.put("send_status",opresult);
        json.put("ext_1",opresult);
        info.put("ext_2",opresult);
        String op_time = data.getString("op_time");
        json.put("op_time",op_time);

//        Timestamp tm = DateUtil.getTimestamp(CalendarUtil.parseDate(decltime,"yyyyMMddHHmmsszzz"),"yyyyMMddHHmmsszzz");
        //json.put("decl_time",new Date().getTime());

        String sql=" update "+ HMetaDataDef.getTable(BusiTypeEnum.BF.getType(), "")+" set content='"+json.toJSONString()+"',ext_1='"+opresult+"' where id="+fendan.getId();
        jdbcTemplate.update(sql);

        if("03".equals(opresult)
                || "04".equals(opresult)
                || "05".equals(opresult)
                || "06".equals(opresult)
                || "07".equals(opresult)
                || "19".equals(opresult)
                || "20".equals(opresult)){
        JSONObject msg=new JSONObject();
        msg.put("op_time",data.getString("op_time"));
        msg.put("link_billno",data.getString("billno")+"-"+data.getString("ass_billno"));
        msg.put("op_result",data.getString("op_result"));
        msg.put("notes",data.getString("notes"));
        msg.put("type",BusiTypeEnum.BGD_HZ.getType());

        sql="insert into h_customer_msg(`cust_id`,`cust_user_id`,`content`,`create_time`,`status`,`level`,`msg_type`)" +
                "values ('"+custId+"',"+fendan.getCust_user_id()+",'"+msg.toJSONString()+"',now(),0,1,'"+BusiTypeEnum.BGD_HZ.getType()+"')";

          jdbcTemplate.update(sql);
        }

        JSONObject json2=new JSONObject();
        json2.put("data",xmlstring);
        json2.put("message_id",envelopinfo.getString("message_id"));
        json2.put("type",BusiTypeEnum.BGD_HZ.getType());
        json2.put("pre_entryid",info.getString("pre_entryid"));
        json2.put("optime",data.getString("op_time"));

//        String id = mongoFileService.saveData(json2.toJSONString());

//        fileDao.save(envelopinfo.getString("message_id"),id, BusinessEnum.CUSTOMS,null,null);
    }

}
