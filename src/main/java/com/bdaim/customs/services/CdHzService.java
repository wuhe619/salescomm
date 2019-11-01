package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dao.FileDao;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.MongoFileService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.BusinessEnum;
import com.bdaim.util.DateUtil;
import com.bdaim.util.ParseHzXml;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;

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
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ParseHzXml parseHzXml;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private MongoFileService mongoFileService;

    @Autowired
    private FileDao fileDao;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        log.info(info.toJSONString());
        info.put("ext_1",info.getString("status"));
        String xmlString=info.getString("xmlstring");
        if(StringUtil.isEmpty(xmlString)){
            throw new TouchException("舱单回执内容不能为空");
        }
        byte[] s = Base64.getDecoder().decode(xmlString);
        handleHzInfo(cust_id,new String (s),info);

        log.info("舱单回执处理完毕");
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
    public void handleHzInfo(String custId,String xmlstring,JSONObject info) throws Exception {
        parseHzXml.parserCangdanHzXML(xmlstring,info);
        JSONObject envelopinfo = info.getJSONObject("envelopinfo");//信封消息
        JSONObject headData = info.getJSONObject("headData");//主单信息
        JSONArray array = info.getJSONArray("list");//分单信息

        HBusiDataManager cangdan = serviceUtils.findZhudanByBillNo(custId, BusiTypeEnum.CZ.getType(),headData.getString("billno"));
        if(cangdan==null){
            throw new TouchException();
        }
        String content = cangdan.getContent();
        JSONObject jsonObject = JSONObject.parseObject(content);
        jsonObject.put("send_status",headData.getString("rtnflag"));
        //Date d=DateUtil.fmtStrToDate(headData.getString("entrydate"),"yyyyMMddHHmmss");
//        jsonObject.put("decl_time",d.getTime());
        cangdan.setExt_2(headData.getString("rtnflag"));
        info.put("ext_3",headData.getString("billno"));
        info.put("ext_2",headData.getString("rtnflag"));
//        cangdan.setContent(jsonObject.toJSONString());
        String sql=" update "+ HMetaDataDef.getTable(BusiTypeEnum.CZ.getType(), "")+" set content='"+jsonObject.toJSONString()+"',ext_2='"+headData.getString("rtnflag")+"' where id="+cangdan.getId();
        jdbcTemplate.update(sql);

        List<HBusiDataManager> list = new ArrayList<>();
        String main_bill_no = "";
        for (int i=0;i<array.size();i++){
            JSONObject json = array.getJSONObject(i);
            HBusiDataManager b = new HBusiDataManager();
            b.setCreateId(cangdan.getCreateId());
            b.setCust_id(cangdan.getCust_id());
            b.setType(BusiTypeEnum.CDF_HZ.getType());
            b.setExt_3(json.getString("assbillno"));
            b.setExt_4(json.getString("billno"));
            main_bill_no = b.getExt_4();
            b.setCreateDate(new Date());
            b.setExt_2(json.getString("rtnflag"));
            b.setContent(json.toJSONString());
            Long fid = sequenceService.getSeq(BusiTypeEnum.CDF_HZ.getType());
            b.setId(fid);
            list.add(b);
        }
        if(list.size()>0){
            serviceUtils.batchInsert(BusiTypeEnum.CDF_HZ.getType(),list);
        }

        if(StringUtil.isNotEmpty(main_bill_no)) {
            Handle handle = new Handle(main_bill_no,array);
            Thread th = new Thread(handle);
            th.start();
        }

       /* String id = mongoFileService.saveData(xmlstring);
        fileDao.save(envelopinfo.getString("message_id"),id, BusinessEnum.CUSTOMS,null,null);*/
    }




    class Handle implements Runnable{
        private  String main_bill_no;
        private JSONArray array;

        public Handle(String main_bill_no,JSONArray array){
            this.array=array;
            this.main_bill_no=main_bill_no;
        }

        @Override
        public void run() {
            log.info("start to cangdan fendan huizhi status");
            handlecdFdStatus(this.main_bill_no,this.array);
        }


        private List<HBusiDataManager> handleCdFd(String main_bill_no){
            List<HBusiDataManager> list = serviceUtils.listFdByBillNo(BusiTypeEnum.CF.getType(),main_bill_no);
            return list;
        }

        public void handlecdFdStatus(String main_bill_no,JSONArray array){
            List<HBusiDataManager> list = handleCdFd(main_bill_no);
            List<HBusiDataManager> tmpList = new ArrayList<>();
            for (int i=0;i<array.size();i++){
                JSONObject json = array.getJSONObject(i);
                String assbillno = json.getString("assbillno");
                String rtnflag = json.getString("rtnflag");
                String billno = json.getString("billno");
                for(HBusiDataManager d:list){
                    if(d.getExt_3().equals(assbillno) && d.getExt_4().equals(billno)){
                        String content = d.getContent();
                        JSONObject obj = JSONObject.parseObject(content);
                        obj.put("send_status",rtnflag);
                        d.setContent(obj.toJSONString());
                        tmpList.add(d);
                        break;
                    }
                }
                if(tmpList.size()>0){
                    for(HBusiDataManager h:tmpList) {
                        String sql2 = "update " + HMetaDataDef.getTable(BusiTypeEnum.CF.getType(), "") + " set content='"+h.getContent()+ "' where id="+h.getId();
                        jdbcTemplate.update(sql2);
                    }
                }
            }
        }
    }

}
