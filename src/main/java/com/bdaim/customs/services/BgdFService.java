package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.util.BaoguandanXmlEXP301;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.PageList;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerPropertyDTO;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.customs.utils.ServiceUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/***
 * 报关单.分单
 */
@Service("busi_bgd_f")
public class BgdFService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(BgdFService.class);

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ElasticSearchService elasticSearchService;

    @Autowired
    private CustomerDao customerDao;

    @Autowired
    private SequenceService sequenceService;

    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;

    @Autowired
    private ServiceUtils serviceUtils;

    @Autowired
    private BaoguandanXmlEXP301 baoguandanXmlEXP301;

    @Autowired
    private CustomerService customerService;

    @Autowired
    private CustomerUserDao customerUserDao;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        // TODO Auto-generated method stub
        Integer pid = info.getInteger("pid");
        String billNo = info.getString("bill_no");
        if (pid == null) {
            log.error("主单id不能为空");
            throw new TouchException("主单id不能为空");
        }
        if (StringUtil.isEmpty(billNo)) {
            log.error("分单号不能为空");
            throw new TouchException("分单号不能为空");
        }
        HBusiDataManager bgdzd = serviceUtils.getObjectByIdAndType(cust_id,pid.longValue(), BusiTypeEnum.BZ.getType());
        if(bgdzd==null){
            throw new TouchException("无权操作");
        }
        List<HBusiDataManager> list = serviceUtils.listDataByPid(cust_id,busiType, pid.longValue(),BusiTypeEnum.BZ.getType());
        if (list != null && list.size() > 0) {
            for (HBusiDataManager hBusiDataManager : list) {
                if(billNo.equals(hBusiDataManager.getExt_3())){
                    log.error("分单号【" + billNo + "】在主单【" + bgdzd.getExt_3() + "】中已经存在");
                    throw new TouchException("分单号【" + billNo + "】在主单【" + bgdzd.getExt_3() + "】中已经存在");
                }
            }
        }
        info.put("type", BusiTypeEnum.BF.getType());
        info.put("check_status", "0");
        info.put("idcard_pic_flag", "0");
        info.put("main_gname", "");
        info.put("low_price_goods", 0);
        info.put("id", id);
        info.put("pid", pid);
        info.put("opt_type", "APD");
        info.put("ext_3",billNo);
        info.put("ext_4",bgdzd.getExt_3());
        info.put("main_bill_no",bgdzd.getExt_3());
        serviceUtils.addDataToES(id.toString(), busiType, info);
        JSONObject jsonObject = JSONObject.parseObject(bgdzd.getContent());
        if (info.containsKey("weight") && info.getString("weight") != null) {
            if (jsonObject.containsKey("weight_total")) {
                String weight_total = jsonObject.getString("weight_total");
                if (StringUtil.isNotEmpty(weight_total)) {
                    weight_total = String.valueOf(Float.valueOf(weight_total) + Float.valueOf(info.getString("weight")));
                    jsonObject.put("weight_total", weight_total);//总重量
                }
            }
        }
        int value = 1;
        if (jsonObject.containsKey("party_total")) {
            value = jsonObject.getInteger("party_total") + value;
        }
        jsonObject.put("party_total", value);//分单总数

        bgdzd.setContent(jsonObject.toJSONString());
        hBusiDataManagerDao.saveOrUpdate(bgdzd);
        serviceUtils.updateDataToES(BusiTypeEnum.BZ.getType(), bgdzd.getId().toString(), jsonObject);

    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        {
            HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id,id, busiType);
            if(dbManager==null){
                throw new TouchException("无权操作");
            }
            String content = dbManager.getContent();
            JSONObject json = JSONObject.parseObject(content);
            Iterator keys = info.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                json.put(key, info.get(key));
            }
            serviceUtils.updateDataToES(busiType, id.toString(), json);
        }

    }


    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) throws TouchException {
        // 提交至海关平台
        if ("HAIGUAN".equals(param.getString("_rule_"))) {
            String sql = "select id, content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from "+ HMetaDataDef.getTable(busiType,"")+" where type=? and id=? ";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(sql, busiType, id);
            if (list.size() == 0) {
                log.warn("报关单分单数据不存在[" + busiType + "]" + id);
                throw new TouchException("2000", "报关单分单数据不存在");
            }
            Map m = list.get(0);

            String cdContent = String.valueOf(m.get("content"));
            if ("B1".equals(String.valueOf(m.get("ext_1"))) && StringUtil.isNotEmpty(cdContent)
                    && "B1".equals(JSON.parseObject(cdContent).getString("send_status"))) {
                log.warn("报关单分单:[" + id + "]已提交至海关");
                throw new TouchException("报关单分单:[" + id + "]已提交至海关");
            }
            // 更新报关单主单信息
            String content = (String) m.get("content");
            JSONObject jo = JSONObject.parseObject(content);

            //start to create xml
            String mainsql = "select id,content, cust_id, cust_group_id, cust_user_id, create_id, create_date ,ext_1, ext_2, ext_3, ext_4, ext_5 from "+ HMetaDataDef.getTable(BusiTypeEnum.BZ.getType(),"")+" where type=? and id=? ";
            list = jdbcTemplate.queryForList(mainsql, BusiTypeEnum.BZ.getType(), jo.getString("pid"));
            Map<String,Object> mainMap = list.get(0);
            List<HBusiDataManager> list2 = serviceUtils.listSdByBillNo(cust_id,BusiTypeEnum.BS.getType(),mainMap.get("ext_3").toString(),jo.getString("bill_no"));
            Map<String,Object> customerInfo = getCustomerInfo(cust_id);
            CustomerUserPropertyDO propertyDO = customerUserDao.getProperty(cust_user_id.toString(),"declare_no");
            CustomerProperty iObj = customerDao.getProperty(cust_id,"i");
            String sendId = "";
            log.info("userid="+cust_user_id+";custid=" + cust_id);
            log.info("iObj="+iObj);
            if(iObj != null) {
                String value = iObj.getPropertyValue();
                log.info("jJson="+value);
                JSONObject iJson = JSONObject.parseObject(value);
                sendId = iJson.getString("sender_id");
            }
            customerInfo.put("sender_id",sendId);
            CustomerUser customerUser = customerUserDao.get(cust_user_id);
            customerInfo.put("input_name","");
            customerInfo.put("declare_no","");
            if(customerUser!=null){
                customerInfo.put("input_name",customerUser.getRealname());
            }
            if(propertyDO!=null){
                customerInfo.put("declare_no",propertyDO.getPropertyValue());
            }
            log.info("分单 "+m.get("ext_3")+"; 商品量："+list2.size());

            bgdCheck(mainMap,m,list2,customerInfo);
            String xmlString = baoguandanXmlEXP301.createXml(mainMap,m,list2,customerInfo);
            log.info("xmlString:"+xmlString);
            info.put("xml",xmlString);
            if(StringUtil.isEmpty(xmlString)){
                throw new TouchException("2000","生成xml报文出错");
            }
            // 更新报关单主单信息
            jo.put("ext_1", "B1");
            jo.put("send_status", "B1");
            info.put("ext_1", "B1");
            info.put("send_status", "B1");
            jo.put("id", m.get("id"));
            jo.put("cust_id", m.get("cust_id"));
            jo.put("cust_group_id", m.get("cust_group_id"));
            jo.put("cust_user_id", m.get("cust_user_id"));
            jo.put("create_id", m.get("create_id"));
            jo.put("create_date", m.get("create_date"));
            jo.put("update_id", m.get("update_id"));
            jo.put("update_date", m.get("update_date"));
            if (m.get("ext_1") != null && !"".equals(m.get("ext_1")))
                jo.put("ext_1", m.get("ext_1"));
            if (m.get("ext_2") != null && !"".equals(m.get("ext_2")))
                jo.put("ext_2", m.get("ext_2"));
            if (m.get("ext_3") != null && !"".equals(m.get("ext_3")))
                jo.put("ext_3", m.get("ext_3"));
            if (m.get("ext_4") != null && !"".equals(m.get("ext_4")))
                jo.put("ext_4", m.get("ext_4"));
            if (m.get("ext_5") != null && !"".equals(m.get("ext_5")))
                jo.put("ext_5", m.get("ext_5"));
            String d = DateUtil.fmtDateToStr(new Date(),"yyyy-MM-dd HH:mm:ss");
            jo.put("decl_time",d);
            info.put("decl_time",d);
            sql = "UPDATE "+HMetaDataDef.getTable(busiType,"")+" SET ext_1 = 'B1', ext_date1 = NOW(), content=? WHERE id = ? AND type = ? AND IFNULL(ext_1,'') <>'B1' ";
            jdbcTemplate.update(sql, jo.toJSONString(), id, busiType);
            serviceUtils.updateDataToES(BusiTypeEnum.BF.getType(), id.toString(), jo);

        }else {
            // 查询报关单主单数据,合并到分单中
            long pid = info.getLong("pid");
            HBusiDataManager dbManager = serviceUtils.getObjectByIdAndType(cust_id,pid, BusiTypeEnum.BZ.getType());
            String content = null;
            if (dbManager != null) {
                content = dbManager.getContent();
            }
            if (StringUtil.isNotEmpty(content)) {
                JSONObject mainData = JSONObject.parseObject(content);
                Iterator keys = mainData.keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (!info.containsKey(key)) {
                        info.put(key, mainData.get(key));
                    }
                }
            }
        }

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
        //报关单主单赋值到分单中
        HBusiDataManager bgdZ = serviceUtils.getObjectByIdAndType(cust_id,info.getLongValue("pid"), BusiTypeEnum.BZ.getType());
        if (bgdZ != null) {
            JSONObject jo = JSONObject.parseObject(bgdZ.getContent());
            Iterator keys = jo.keySet().iterator();
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (!info.containsKey(key)) {
                    info.put(key, jo.get(key));
                }
            }
        }
    }


    private Map<String,Object> getCustomerInfo(String custId){
        PageParam pageParam = new PageParam();
        pageParam.setPageNum(1);
        pageParam.setPageSize(1);
        CustomerRegistDTO customerRegistDTO = new CustomerRegistDTO();
        customerRegistDTO.setCustId(custId);
        PageList pageList = customerService.getCustomerInfo(pageParam,customerRegistDTO);
        List list = pageList.getList();
        return (Map<String, Object>) list.get(0);
    }


    private String bgdCheck(Map<String,Object> mainMap,Map<String,Object> map, List<HBusiDataManager> ds,Map<String,Object>customerInfo) throws TouchException {
        if(!customerInfo.containsKey("sender_id") || null==customerInfo.get("sender_id") || "".equals(customerInfo.get("sender_id"))){
                throw new TouchException("2001","核心字段sender_id缺失");
        }
        String bdmessage = "报单%s，缺失%s";
        String filedName = "";
        String content = (String) mainMap.get("content");
        JSONObject mainjson = JSONObject.parseObject(content);
        String partyContent = (String) map.get("content");
        JSONObject json = JSON.parseObject(partyContent);
        if(StringUtil.isEmpty(mainjson.getString("i_e_flag"))){
            filedName += "," + BGDReportEnum.IEFlag.getName();
        }
        if(StringUtil.isEmpty(mainjson.getString("i_e_port"))){
            filedName += "," + BGDReportEnum.IEPort.getName();
        }
        if(StringUtil.isEmpty(mainjson.getString("i_e_date"))){
            filedName += "," + BGDReportEnum.IEDate.getName();
        }
        if(StringUtil.isEmpty(mainjson.getString("i_d_date"))){
            filedName += "," + BGDReportEnum.DDate.getName();
        }
        if(StringUtil.isEmpty(mainjson.getString("depart_arrival_port"))){
            filedName += "," + BGDReportEnum.DestinationPort.getName();
        }
        if(StringUtil.isEmpty(mainjson.getString("traf_name"))){
            filedName += "," + BGDReportEnum.TrafName.getName();
        }

        if(StringUtil.isEmpty(mainjson.getString("voyage_no"))){
            filedName += "," + BGDReportEnum.VoyageNo.getName();
        }
        if(StringUtil.isEmpty(mainjson.getString("traf_mode"))){
            filedName += "," + BGDReportEnum.TrafMode.getName();
        }
//        if(StringUtil.isEmpty(mainjson.getString("shipper_unit_name"))){
//            filedName += "," + BGDReportEnum.OwnerName.getName();
//        }
        if(StringUtil.isEmpty(mainjson.getString("agent_type"))){
            filedName += "," + BGDReportEnum.AgentType.getName();
        }
        if(StringUtil.isEmpty(customerInfo.get("agent_code")==null?"":customerInfo.get("agent_code").toString())){
            filedName += "," + BGDReportEnum.AgentCode.getName();
        }

        if(StringUtil.isEmpty(customerInfo.get("enterpriseName")==null?"":customerInfo.get("enterpriseName").toString())){
            filedName += "," + BGDReportEnum.AgentName.getName();
        }

        if(StringUtil.isEmpty(mainjson.getString("bill_no"))){
            filedName += "," + BGDReportEnum.BillNo.getName();
        }
        if(StringUtil.isEmpty(json.getString("bill_no"))){
            filedName += "," + BGDReportEnum.AssBillNo.getName();
        }
        if(StringUtil.isEmpty(mainjson.getString("trade_country"))){
            filedName += "," + BGDReportEnum.TradeCountry.getName();
        }
        if(StringUtil.isEmpty(json.getString("pack_no"))){
            filedName += "," + BGDReportEnum.PackNo.getName();
        }
        if(StringUtil.isEmpty(json.getString("weight"))){
            filedName += "," + BGDReportEnum.GrossWt.getName();
        }
        if(StringUtil.isEmpty(json.getString("weight"))){
            filedName += "," + BGDReportEnum.NetWt.getName();
        }
        if(StringUtil.isEmpty(mainjson.getString("wrap_class"))){
            filedName += "," + BGDReportEnum.WrapType.getName();
        }
        if(StringUtil.isEmpty(mainjson.getString("decl_port"))){
            filedName += "," + BGDReportEnum.DeclPort.getName();
        }
        if(customerInfo.get("agent_code")==null
                || customerInfo.get("agent_code").toString().length()<6){
            filedName += "," + BGDReportEnum.CoOwner.getName();//经营单位性质，取报关单位编码第6位
        }
        if(StringUtil.isEmpty(customerInfo.getOrDefault("input_name","").toString())){
            filedName += "," + BGDReportEnum.InputNo.getName();
        }

        if(StringUtil.isEmpty(customerInfo.get("agent_code")==null?"":customerInfo.get("agent_code").toString())){
            filedName += "," + BGDReportEnum.InputCompanyCo.getName();
        }

        if(StringUtil.isEmpty(customerInfo.get("enterpriseName")==null?"":customerInfo.get("enterpriseName").toString())){
            filedName += "," + BGDReportEnum.InputCompanyName.getName();
        }

        if(StringUtil.isEmpty(customerInfo.get("declare_no")==null?"":customerInfo.get("declare_no").toString())){
            filedName += "," + BGDReportEnum.DeclareNo.getName();
        }
        if(StringUtil.isEmpty(mainjson.get("warehouse_code")==null?"":mainjson.get("warehouse_code").toString())){
            filedName += "," + BGDReportEnum.CustomsField.getName();
        }
        if(StringUtil.isEmpty(mainjson.get("send_name")==null?"":mainjson.get("send_name").toString())){
            filedName += "," + BGDReportEnum.SendName.getName();
        }
        if(StringUtil.isEmpty(json.get("receive_name")==null?"":json.get("receive_name").toString())){
            filedName += "," + BGDReportEnum.ReceiveName.getName();
        }

        if(StringUtil.isEmpty(mainjson.get("send_country")==null?"":mainjson.get("send_country").toString())){
            filedName += "," + BGDReportEnum.SendCountry.getName();
        }
        if(StringUtil.isEmpty(mainjson.get("send_city_en")==null?"":mainjson.get("send_city_en").toString())){
            filedName += "," + BGDReportEnum.SendCity.getName();
        }
        if(StringUtil.isEmpty(json.get("id_no")==null?"":json.get("id_no").toString())){
            filedName += "," + BGDReportEnum.SendId.getName();
        }

        if(StringUtil.isEmpty(json.get("total_value")==null?"":json.get("total_value").toString())){
            filedName += "," + BGDReportEnum.TotalValue.getName();
        }

        if(StringUtil.isEmpty(json.get("curr_code")==null?"":json.get("curr_code").toString())){
            filedName += "," + BGDReportEnum.CurrCode.getName();
        }
        if(StringUtil.isEmpty(json.get("main_gname")==null?"":json.get("main_gname").toString())){
            filedName += "," + BGDReportEnum.MainGName.getName();
        }
        if(StringUtil.isEmpty(mainjson.get("entry_type")==null?"":mainjson.get("entry_type").toString())){
            filedName += "," + BGDReportEnum.EntryType.getName();
        }
        if(StringUtil.isEmpty(json.get("id_type")==null?"":json.get("id_type").toString())){
            filedName += "," + BGDReportEnum.SendIdType.getName();
        }

        filedName = bgdsdCheck(ds,filedName);
        if(StringUtil.isNotEmpty(filedName)){
            String message = String.format(bdmessage,json.getString("bill_no"),filedName);
            throw new TouchException("2000",message);
        }

        return "success";
    }

    private String bgdsdCheck(List<HBusiDataManager> ds,String filedName) {

        for (HBusiDataManager d : ds) {
            String content = d.getContent();
            JSONObject json = JSONObject.parseObject(content);
            String gno = d.getExt_5();
            String msg = "商品序号"+gno+"，缺失，";

            Boolean hasError = false;

            if (StringUtil.isEmpty(d.getExt_3())) {
                hasError = true;
                msg += "商品编号,";
            }
            if (!json.containsKey("g_name") || StringUtil.isEmpty(json.getString("g_name"))) {
                msg += "商品名称,";
                hasError = true;
            }
            if (!json.containsKey("g_model") || StringUtil.isEmpty(json.getString("g_model"))) {
                msg += "商品规格、型号,";
                hasError = true;
            }
            if (!json.containsKey("origin_country") || StringUtil.isEmpty(json.getString("origin_country"))) {
                msg += "产销国,";
                hasError = true;
            }
            if (!json.containsKey("curr_code") || StringUtil.isEmpty(json.getString("curr_code"))) {
                msg += "成交币制,";
                hasError = true;
            }
            if (!json.containsKey("total_price") || StringUtil.isEmpty(json.getString("total_price"))) {
                msg += "成交总价,";
                hasError = true;
            }
            if (!json.containsKey("decl_price") || StringUtil.isEmpty(json.getString("decl_price"))) {
                msg += "申报单价,";
                hasError = true;
            }
            if (!json.containsKey("decl_total") || StringUtil.isEmpty(json.getString("decl_total"))) {
                msg += "申报总价,";
            }
            if (!json.containsKey("g_unit") || StringUtil.isEmpty(json.getString("g_unit"))) {
                msg += "申报计量单位,";
                hasError = true;
            }
            if (!json.containsKey("qty_1") || StringUtil.isEmpty(json.getString("qty_1"))) {
                msg += "第一(法定)数量,";
                hasError = true;
            }
            if (!json.containsKey("unit_1") || StringUtil.isEmpty(json.getString("unit_1"))) {
                msg += "第一(法定)计量单位,";
                hasError = true;
            }
            if (!json.containsKey("ggrosswt") || StringUtil.isEmpty(json.getString("ggrosswt"))) {
                msg += "商品毛重";
                hasError = true;
            }

            if (!hasError) {
                continue;
            } else {
                filedName += "\r\n" + msg;
            }
        }
        return filedName;
    }

    /**
     * 报关单报文必填字段枚举
     */
    public enum BGDReportEnum{
        sender_id("sender_id","sender_id"),
        PreEntryId("PreEntryId","数据中心统一编号"),
        EntryId("EntryId","海关编号"),
        IEFlag("IEFlag","进出口标志"),
        IEPort("IEPort","进出口岸代码"),
        IEDate("IEDate","进出口日期"),
        DDate("DDate","申报时间"),
        DestinationPort("DestinationPort","指运港(抵运港)"),
        TrafName("TrafName","运输工具名称"),
        VoyageNo("VoyageNo","运输工具航次(班)号"),
        TrafMode("TrafMode","运输方式代码"),
//        TradeCo("TradeCo","经营单位编号"),
        OwnerName("OwnerName","货主单位名称"),
        AgentType("AgentType","申报单位类别"),
        AgentCode("AgentCode","申报单位代码"),
        AgentName("AgentName","申报单位名称"),
        BillNo("BillNo","总运单号"),
        AssBillNo("AssBillNo","分运单号"),
        TradeCountry("TradeCountry","贸易国别(起/抵运地)"),
        PackNo("PackNo","件数"),
        GrossWt("GrossWt","毛重"),
        NetWt("NetWt","净重"),
        WrapType("WrapType","包装种类"),
        DeclPort("DeclPort","申报口岸代码"),
        CoOwner("CoOwner","经营单位性质"),
        InputNo("InputNo","录入人"),
        InputCompanyCo("InputCompanyCo","录入单位代码"),
        InputCompanyName("InputCompanyName","录入单位名称"),
        DeclareNo("DeclareNo","报关员代码"),
        CustomsField("CustomsField","码头/货场代码"),
        SendName("SendName","发件人"),
        ReceiveName("ReceiveName","收件人"),
        SendCountry("SendCountry","发件人国别"),
        SendCity("SendCity","发件人城市"),
        SendId("SendId","收发件人证件号码"),
        TotalValue("TotalValue","价值"),
        CurrCode("CurrCode","币制"),
        MainGName("MainGName","主要商品名称"),
        EntryType("EntryType","报关类别"),
        SendIdType("SendIdType","收发件人证件类型");

        private String key;
        private String name;

        BGDReportEnum(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public String getKey() {
            return key;
        }



        public String getName() {
            return name;
        }


    }

    /**
     * 报关单税单报文必填字段枚举
     */
    public enum BGDSDReportEnum{
        CodeTS("CodeTS","商品编号"),
        GName("GName","物品名称"),
        GModel("GModel","商品规格、型号"),
        OriginCountry("OriginCountry","产销国"),
        TradeCurr("TradeCurr","成交币制"),
        TradeTotal("TradeTotal","成交总价"),
        DeclPrice("DeclPrice","申报单价"),
        DeclTotal("DeclTotal","申报总价"),
        GQty("GQty","申报数量"),
        VoyageNo("VoyageNo","申报计量单位"),
        GUnit("GUnit","运输方式代码"),
        Qty1("Qty1","第一(法定)数量"),
        Unit1("Unit1","第一(法定)计量单位"),
        GGrossWt("GGrossWt","商品毛重");

        private String key;
        private String name;

        BGDSDReportEnum(String key, String name) {
            this.key = key;
            this.name = name;
        }

        public String getKey() {
            return key;
        }



        public String getName() {
            return name;
        }


    }
}
