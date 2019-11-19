package com.bdaim.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customs.entity.HBusiDataManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class BaoguandanXmlEXP301 {

    private static Logger log = LoggerFactory.getLogger(BaoguandanXmlEXP301.class);
/*
    public static void main(String[] args) {
        createXml();
    }*/


    public String createXml(Map<String, Object> mainMap, Map<String, Object> map, List<HBusiDataManager> ds, Map<String, Object> customerInfo) {
        try {
            String partyContent = (String) map.get("content");
            JSONObject json = JSON.parseObject(partyContent);
            if (StringUtil.isEmpty(json.getString("send_name")) || StringUtil.isEmpty(json.getString("send_address")) || StringUtil.isEmpty(json.getString("send_tel"))
                    || StringUtil.isEmpty(json.getString("send_name_en")) || StringUtil.isEmpty("send_address_en") || StringUtil.isEmpty(json.getString("send_city_en"))
                    || StringUtil.isEmpty(json.getString("send_country"))) {
                return null;
            }
            // 创建解析器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document document = db.newDocument();
            // 不显示standalone="no"
            document.setXmlStandalone(true);
            Element Package = document.createElement("Package");
            // 向bookstore根节点中添加子节点book
            Element EnvelopInfo = document.createElement("EnvelopInfo");

            createEnvelopInfoXML(document, EnvelopInfo, customerInfo, String.valueOf(map.get("id")));
            Package.appendChild(EnvelopInfo);

            Element DataInfo = document.createElement("DataInfo");

            createDataInfoXML(document, DataInfo, mainMap, map, ds, customerInfo);

            Package.appendChild(DataInfo);
            document.appendChild(Package);

            // 创建TransformerFactory对象
            TransformerFactory tff = TransformerFactory.newInstance();
            // 创建 Transformer对象
            Transformer tf = tff.newTransformer();

            // 输出内容是否使用换行
            tf.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "");//
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            // 创建xml文件并写入内容
          /*生成xml文件
            tf.transform(new DOMSource(document), new StreamResult(new File("E:\\EXP301.xml")));
            System.out.println("生成EXP301.xml成功");
          */
            //生成xml内容字符串
            DOMSource domSource = new DOMSource(document);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // 使用Transformer的transform()方法将DOM树转换成XML
            tf.transform(domSource, new StreamResult(bos));
            String xmlString = bos.toString();
            System.out.println(xmlString);
            return xmlString;
        } catch (Exception e) {
            log.error("生成EXP301.xml失败", e);
        }
        return null;
    }

    private String getId11NO(String id) {
        while (id.length() < 11) {
            id = "0" + id;
        }
        return id;
    }

    private void createEnvelopInfoXML(Document document, Element envelopInfo, Map<String, Object> custInfo, String id) {
        Element version = document.createElement("version");
        Element message_id = document.createElement("message_id");
        Element file_name = document.createElement("file_name");
        Element message_type = document.createElement("message_type");
        Element sender_id = document.createElement("sender_id");
        Element receiver_id = document.createElement("receiver_id");
        Element send_time = document.createElement("send_time");

        version.setTextContent("1.0");
        String dateStr = DateUtil.fmtDateToStr(new Date(), "yyyyMMddHHmmss");
        String idstr = getId11NO(id);
        String msg_id = custInfo.get("sender_id").toString() + "E010000" + dateStr + idstr;
        message_id.setTextContent(msg_id);
        file_name.setTextContent(msg_id + ".EXP");
        message_type.setTextContent("EXP301");
        sender_id.setTextContent((String) custInfo.get("sender_id"));
        receiver_id.setTextContent("E010000");
        send_time.setTextContent(dateStr);

        envelopInfo.appendChild(version);
        envelopInfo.appendChild(message_id);
        envelopInfo.appendChild(file_name);
        envelopInfo.appendChild(message_type);
        envelopInfo.appendChild(sender_id);
        envelopInfo.appendChild(receiver_id);
        envelopInfo.appendChild(send_time);
    }

    private void createDataInfoXML(Document document, Element DataInfo, Map<String, Object> mainMap, Map<String, Object> map, List<HBusiDataManager> ds, Map<String, Object> customerInfo) throws Exception {
        Element SignedData = document.createElement("SignedData");
        Element Data = document.createElement("Data");
        Element EXP301 = document.createElementNS("http://www.chinaport.gov.cn/Exp", "EXP301");
        String content = (String) mainMap.get("content");
        JSONObject mainjson = JSONObject.parseObject(content);

        String partyContent = (String) map.get("content");
        JSONObject json = JSON.parseObject(partyContent);
        createEntryHeadXML(document, EXP301, mainjson, json, customerInfo);
        createEntryListXML(document, EXP301, ds);
//        createEntryDocuXML(document,EXP301);

        Data.appendChild(EXP301);
        SignedData.appendChild(Data);
        DataInfo.appendChild(SignedData);
    }

    private void createEntryHeadXML(Document document, Element EXP301, JSONObject mainJson, JSONObject json, Map<String, Object> customerInfo) {
        Element EntryHead = document.createElement("EntryHead");
        Element OpType = document.createElement("OpType");
        String optype = mainJson.getString("op_type");
        if (StringUtil.isEmpty(optype)) {
            optype = "ADD";
        }
        OpType.setTextContent(optype);//r
        EntryHead.appendChild(OpType);
        Element PreEntryId = document.createElement("PreEntryId");
        PreEntryId.setTextContent(json.containsKey("pre_input_code") ? json.getString("pre_input_code") : "");
        EntryHead.appendChild(PreEntryId);
        Element EntryId = document.createElement("EntryId");
        EntryId.setTextContent(json.containsKey("entryid") ? json.getString("entryid").trim() : "");
        EntryHead.appendChild(EntryId);

        Element IEFlag = document.createElement("IEFlag");
        IEFlag.setTextContent(mainJson.getString("i_e_flag"));   //r
        EntryHead.appendChild(IEFlag);
        Element IEPort = document.createElement("IEPort");
        IEPort.setTextContent(mainJson.getString("i_e_port"));//r
        EntryHead.appendChild(IEPort);
        Element IEDate = document.createElement("IEDate");
        IEDate.setTextContent(mainJson.getString("i_e_date").replace("-", ""));//r
        EntryHead.appendChild(IEDate);
        Element DDate = document.createElement("DDate");
        DDate.setTextContent(mainJson.getString("i_d_date").replace("-", ""));//r
        EntryHead.appendChild(DDate);
        Element DestinationPort = document.createElement("DestinationPort");
        DestinationPort.setTextContent(mainJson.getString("depart_arrival_port")); //r
        EntryHead.appendChild(DestinationPort);
        Element TrafName = document.createElement("TrafName");
        TrafName.setTextContent(mainJson.getString("trans_traf_name").trim());//r 转关运输工具
        EntryHead.appendChild(TrafName);
        Element VoyageNo = document.createElement("VoyageNo");
        VoyageNo.setTextContent(mainJson.getString("voyage_no").trim());//r
        EntryHead.appendChild(VoyageNo);
        Element TrafMode = document.createElement("TrafMode");
        TrafMode.setTextContent(mainJson.getString("traf_mode").trim());//r
        EntryHead.appendChild(TrafMode);
        Element TradeCo = document.createElement("TradeCo");
        EntryHead.appendChild(TradeCo);
        Element TradeName = document.createElement("TradeName");
        EntryHead.appendChild(TradeName);
        Element DistrictCode = document.createElement("DistrictCode");
        EntryHead.appendChild(DistrictCode);
        Element OwnerCode = document.createElement("OwnerCode");
        EntryHead.appendChild(OwnerCode);
        Element OwnerName = document.createElement("OwnerName");
        OwnerName.setTextContent(json.getString("receive_name").trim());//r 货主单位名称取收件人名称
        EntryHead.appendChild(OwnerName);
        Element AgentType = document.createElement("AgentType");
        AgentType.setTextContent(mainJson.getString("agent_type").trim());  //0：企业；1：自然人
        EntryHead.appendChild(AgentType);
        Element AgentCode = document.createElement("AgentCode");//申报单位代码
        AgentCode.setTextContent((String) customerInfo.getOrDefault("agent_code", "")); //AgentType=0时必填
        EntryHead.appendChild(AgentCode);
        Element AgentName = document.createElement("AgentName");//申报单位名称
        AgentName.setTextContent((String) customerInfo.getOrDefault("enterpriseName", ""));//AgentType=0时必填
        EntryHead.appendChild(AgentName);
        Element ContrNo = document.createElement("ContrNo");

        EntryHead.appendChild(ContrNo);
        Element BillNo = document.createElement("BillNo");
        BillNo.setTextContent(mainJson.getString("bill_no").trim()); //r 总运单号
        EntryHead.appendChild(BillNo);
        Element AssBillNo = document.createElement("AssBillNo");
        AssBillNo.setTextContent(json.getString("bill_no").trim()); //r  分运单号
        EntryHead.appendChild(AssBillNo);
        Element TradeCountry = document.createElement("TradeCountry");
        TradeCountry.setTextContent(mainJson.getString("trade_country"));  //r
        EntryHead.appendChild(TradeCountry);
        Element TradeMode = document.createElement("TradeMode");

        EntryHead.appendChild(TradeMode);
        Element CutMode = document.createElement("CutMode");
        CutMode.setTextContent("");
        EntryHead.appendChild(CutMode);
        Element TransMode = document.createElement("TransMode");
//        TransMode.setTextContent("1");
        EntryHead.appendChild(TransMode);
        Element FeeMark = document.createElement("FeeMark");
//        FeeMark.setTextContent("2");
        EntryHead.appendChild(FeeMark);
        Element FeeCurr = document.createElement("FeeCurr");
//        FeeCurr.setTextContent("110");
        EntryHead.appendChild(FeeCurr);
        Element FeeRate = document.createElement("FeeRate");
//        FeeRate.setTextContent("0.1");
        EntryHead.appendChild(FeeRate);
        Element InsurMark = document.createElement("InsurMark");
//        InsurMark.setTextContent("3");
        EntryHead.appendChild(InsurMark);
        Element InsurCurr = document.createElement("InsurCurr");
//        InsurCurr.setTextContent("110");
        EntryHead.appendChild(InsurCurr);
        Element InsurRate = document.createElement("InsurRate");
//        InsurRate.setTextContent("999");
        EntryHead.appendChild(InsurRate);
        Element OtherMark = document.createElement("OtherMark");
//        OtherMark.setTextContent("3");
        EntryHead.appendChild(OtherMark);
        Element OtherCurr = document.createElement("OtherCurr");
//        OtherCurr.setTextContent("110");
        EntryHead.appendChild(OtherCurr);
        Element OtherRate = document.createElement("OtherRate");
//        OtherRate.setTextContent("1.2");
        EntryHead.appendChild(OtherRate);
        Element PackNo = document.createElement("PackNo");
        PackNo.setTextContent(json.getString("pack_no").trim());  //r
        EntryHead.appendChild(PackNo);
        Element GrossWt = document.createElement("GrossWt");
        String weight = json.getString("weight").trim();
        if (StringUtil.isNotEmpty(weight)) {
            BigDecimal b = new BigDecimal(weight).setScale(5, RoundingMode.HALF_UP);
            GrossWt.setTextContent(b.toPlainString());//r
        }
        EntryHead.appendChild(GrossWt);
        Element NetWt = document.createElement("NetWt");
        String net_weight = json.getString("net_weight").trim();
        if (StringUtil.isNotEmpty(net_weight)) {
            BigDecimal b = new BigDecimal(net_weight).setScale(5, RoundingMode.HALF_UP);
            NetWt.setTextContent(b.toPlainString());//r
        }
//        NetWt.setTextContent(json.containsKey("net_weight")?json.getString("net_weight").trim():"");  //r
        EntryHead.appendChild(NetWt);
        Element WrapType = document.createElement("WrapType");
        WrapType.setTextContent(mainJson.getString("wrap_class").trim());  //r
        EntryHead.appendChild(WrapType);
        Element NoteS = document.createElement("NoteS");

        EntryHead.appendChild(NoteS);
        Element DeclPort = document.createElement("DeclPort");
        DeclPort.setTextContent(mainJson.getString("decl_port"));  //r
        EntryHead.appendChild(DeclPort);

        Element CoOwner = document.createElement("CoOwner");//经营单位性质,取报关单位代码
       /* String unit = (String) customerInfo.getOrDefault("agent_code","");
        unit = unit.substring(0,6);
        unit = unit.substring(unit.length()-1);
        CoOwner.setTextContent(unit);  //r*/
        EntryHead.appendChild(CoOwner);
        /*Element MnlJgfFlag = document.createElement("MnlJgfFlag");
        MnlJgfFlag.setTextContent("");
        EntryHead.appendChild(MnlJgfFlag);
        Element ServiceRate = document.createElement("ServiceRate");
        ServiceRate.setTextContent("");
        EntryHead.appendChild(ServiceRate);
        Element ServiceFee = document.createElement("ServiceFee");
        ServiceFee.setTextContent("");
        EntryHead.appendChild(ServiceFee);*/
        Element RelativeId = document.createElement("RelativeId");
        RelativeId.setTextContent("");
        EntryHead.appendChild(RelativeId);
//        Element TypistNo = document.createElement("TypistNo");
//        TypistNo.setTextContent("");
//        EntryHead.appendChild(TypistNo);
        Element InputNo = document.createElement("InputNo");//录入人
        InputNo.setTextContent(String.valueOf(customerInfo.getOrDefault("input_no", "")));  //r
        EntryHead.appendChild(InputNo);
        Element InputCompanyCo = document.createElement("InputCompanyCo");//录入单位代码
        InputCompanyCo.setTextContent((String) ((String) customerInfo.getOrDefault("agent_code", "")).trim());  //r
        EntryHead.appendChild(InputCompanyCo);
        Element InputCompanyName = document.createElement("InputCompanyName");//录入单位名称
        InputCompanyName.setTextContent((String) customerInfo.getOrDefault("enterpriseName", ""));  //r
        EntryHead.appendChild(InputCompanyName);
//        Element PDate = document.createElement("PDate");
//        EntryHead.appendChild(PDate);
        Element DeclareNo = document.createElement("DeclareNo");//报关员代码
        DeclareNo.setTextContent((String) customerInfo.getOrDefault("declare_no", ""));  //如果AgentType=1，必不填,否则必填
        EntryHead.appendChild(DeclareNo);
        Element CustomsField = document.createElement("CustomsField");
        String customsField = mainJson.containsKey("warehouse_code") ? mainJson.getString("warehouse_code") : "";
        CustomsField.setTextContent(customsField);
        EntryHead.appendChild(CustomsField);
        /*Element SpecialFlag = document.createElement("SpecialFlag");
        EntryHead.appendChild(SpecialFlag);*/
        Element KjId = document.createElement("KjId");

        EntryHead.appendChild(KjId);
        Element SendName = document.createElement("SendName");
        SendName.setTextContent(json.getString("send_name").trim()); //r
        EntryHead.appendChild(SendName);
        Element ReceiveName = document.createElement("ReceiveName");
        ReceiveName.setTextContent(json.getString("receive_name").trim());  //r
        EntryHead.appendChild(ReceiveName);
        Element SendCountry = document.createElement("SendCountry");
        SendCountry.setTextContent(json.getString("send_country").trim());  //r
        EntryHead.appendChild(SendCountry);
        Element SendCity = document.createElement("SendCity");
//        SendCity.setTextContent(mainJson.getString("send_city"));  //
        EntryHead.appendChild(SendCity);
        Element SendId = document.createElement("SendId");
        SendId.setTextContent(json.getString("id_no").trim());  //r 收发件人证件号码
        EntryHead.appendChild(SendId);
        Element TotalValue = document.createElement("TotalValue");
        TotalValue.setTextContent(json.getString("total_value").trim());  //r 价值 DECIMAL(19, 4)
        EntryHead.appendChild(TotalValue);
        Element CurrCode = document.createElement("CurrCode");
        CurrCode.setTextContent(json.getString("curr_code").trim());  //r
        EntryHead.appendChild(CurrCode);
        /*Element ReceiveDate = document.createElement("ReceiveDate");
        ReceiveDate.setTextContent("");
        EntryHead.appendChild(ReceiveDate);*/
       /* Element ChannelEr = document.createElement("ChannelEr");
        ChannelEr.setTextContent("");
        EntryHead.appendChild(ChannelEr);*/
        Element MainGName = document.createElement("MainGName");
        String gname = json.getString("main_gname");
        if (gname.endsWith("|")) {
            gname = gname.replace("|", "");
        }
        MainGName.setTextContent(gname);  //r
        EntryHead.appendChild(MainGName);
        Element EntryType = document.createElement("EntryType");
        EntryType.setTextContent(mainJson.getString("entry_type").trim());  //r
        EntryHead.appendChild(EntryType);
        Element SendIdType = document.createElement("SendIdType");
        SendIdType.setTextContent(json.getString("id_type"));  //收发件人证件类型,B类必填
        EntryHead.appendChild(SendIdType);
        Element TradeScc = document.createElement("TradeScc");
        TradeScc.setTextContent("");//收发货人统一社会信用代码
        EntryHead.appendChild(TradeScc);

        Element OwnerScc = document.createElement("OwnerScc");
        OwnerScc.setTextContent("");//货主单位统一社会信用代码
        EntryHead.appendChild(OwnerScc);
        Element AgentScc = document.createElement("AgentScc");
        AgentScc.setTextContent("");//申报单位统一社会信用代码
        EntryHead.appendChild(AgentScc);
        Element SendAddress = document.createElement("SendAddress");
        SendAddress.setTextContent(json.getString("send_address"));//发件人地址 出口必填，进口起运地为港、澳、台的地区必填。只填写区县、街道级别及以下详细地址
        EntryHead.appendChild(SendAddress);
        Element SendTelNo = document.createElement("SendTelNo");//r
        SendTelNo.setTextContent(json.getString("send_tel").trim());//发件人号码 仅能填写阿拉伯数字、“-”（短横线）、“∣”3种字符，均为半角字符。“-”（短横线）用于区号-座机号-分机号的分隔；“∣”用于两个不同号码之间的分隔
        EntryHead.appendChild(SendTelNo);
        Element ReceiveAddress = document.createElement("ReceiveAddress");//r
        String r_city = json.getString("receive_city");
        String r_address = json.getString("receive_address");
        String address = r_address.replace(r_city, "");
        ReceiveAddress.setTextContent(address);//收件人地址 进口必填，出口抵运地为港、澳、台地区的必填。只填写区县、街道级别及以下详细地址
        EntryHead.appendChild(ReceiveAddress);
        Element ReceiveTelNo = document.createElement("ReceiveTelNo");//r
        ReceiveTelNo.setTextContent(json.getString("receive_tel").trim());//收件人号码 必填，仅能填写阿拉伯数字、“-”（短横线）、“∣”3种字符，均为半角字符。“-”（短横线）用于区号-座机号-分机号的分隔；“∣”用于两个不同号码之间的分隔
        EntryHead.appendChild(ReceiveTelNo);
        Element ReceiveCountry = document.createElement("ReceiveCountry");//r
        ReceiveCountry.setTextContent("142");//收件人国别 进口只能是中国，且不能修改
        EntryHead.appendChild(ReceiveCountry);
        Element ReceiveCity = document.createElement("ReceiveCity");//r
        ReceiveCity.setTextContent(json.getString("receive_city"));//收件人城市
        EntryHead.appendChild(ReceiveCity);
        Element StopCityEn = document.createElement("StopCityEn");//r
        StopCityEn.setTextContent(mainJson.getString("stop_address_en"));//英文经停城市  进口必填，仅限英文及符号，没有填“n”或“N”
        EntryHead.appendChild(StopCityEn);
        Element SendNameEn = document.createElement("SendNameEn");//r
        SendNameEn.setTextContent(json.getString("send_name_en"));//英文发件人  进口必填
        EntryHead.appendChild(SendNameEn);
        Element SendAddressEn = document.createElement("SendAddressEn");//r
        SendAddressEn.setTextContent(json.getString("send_address_en"));//英文发件人地址  进口为非港、澳、台地区的必填，只填写区县、街道级别及以下详细地址
        EntryHead.appendChild(SendAddressEn);
        Element SendCityEn = document.createElement("SendCityEn");//r
        SendCityEn.setTextContent(json.getString("send_city_en"));//英文发件人城市进口必填,必须包含英文或拼音，可包含符号
        EntryHead.appendChild(SendCityEn);
        Element ReceiveNameEn = document.createElement("ReceiveNameEn");//
        ReceiveNameEn.setTextContent("");//英文收件人 出口必填
        EntryHead.appendChild(ReceiveNameEn);
        Element ReceiveAddressEn = document.createElement("ReceiveAddressEn");//
        ReceiveAddressEn.setTextContent(json.getString("receive_address_en"));//英文收件人地址,出口抵运地为非港、澳、台地区的必填
        EntryHead.appendChild(ReceiveAddressEn);
        Element WoodWrap = document.createElement("WoodWrap");//r
        WoodWrap.setTextContent(mainJson.getString("wrap_wood"));//是否含木质包装 必填0：不含；  1：含有
        EntryHead.appendChild(WoodWrap);
        Element MainGNameEn = document.createElement("MainGNameEn");//r
        MainGNameEn.setTextContent(json.getString("main_gname_en"));//主要货物英文名称   必填
        EntryHead.appendChild(MainGNameEn);

        Element GoodsUsed = document.createElement("GoodsUsed");//r
        GoodsUsed.setTextContent(mainJson.getString("is_old"));//是否为旧物品   必填0：否；  1：是
        EntryHead.appendChild(GoodsUsed);

        Element LowTempTrans = document.createElement("LowTempTrans");//r
        LowTempTrans.setTextContent(mainJson.getString("l_t_trans"));//是否为低温运输  必填0：否；  1：是
        EntryHead.appendChild(LowTempTrans);

        EXP301.appendChild(EntryHead);
    }

    private void createEntryListXML(Document document, Element exp301, List<HBusiDataManager> ds) {
        for (HBusiDataManager map : ds) {
            Element EntryList = document.createElement("EntryList");
            Element OpType = document.createElement("OpType");
            String content = map.getContent();
            JSONObject json = JSON.parseObject(content);
            String optype = json.getString("op_type");
            if (StringUtil.isEmpty(optype)) {
                optype = "ADD";
            }
            OpType.setTextContent(optype);
            EntryList.appendChild(OpType);
            Element GNo = document.createElement("GNo");
            String gno = map.getExt_5();
            GNo.setTextContent(gno);
            EntryList.appendChild(GNo);
            Element CodeTS = document.createElement("CodeTS");
            CodeTS.setTextContent(json.getString("code_ts"));
            EntryList.appendChild(CodeTS);
            Element GName = document.createElement("GName");
            GName.setTextContent(json.getString("g_name"));
            EntryList.appendChild(GName);
            Element GModel = document.createElement("GModel");
            GModel.setTextContent(json.getString("g_model"));
            EntryList.appendChild(GModel);
            Element OriginCountry = document.createElement("OriginCountry");
            OriginCountry.setTextContent(json.containsKey("origin_country") ? json.getString("origin_country") : "");
            EntryList.appendChild(OriginCountry);
            Element TradeCurr = document.createElement("TradeCurr");
            TradeCurr.setTextContent(json.containsKey("curr_code") ? json.getString("curr_code") : "");
            EntryList.appendChild(TradeCurr);
//            Element ExchangeRate = document.createElement("ExchangeRate");
//
//            EntryList.appendChild(ExchangeRate);
            Element TradeTotal = document.createElement("TradeTotal");
            TradeTotal.setTextContent(json.getString("total_price"));
            EntryList.appendChild(TradeTotal);
            Element DeclPrice = document.createElement("DeclPrice");
            DeclPrice.setTextContent(json.containsKey("decl_price") ? json.getString("decl_price") : "");
            EntryList.appendChild(DeclPrice);
            Element DeclTotal = document.createElement("DeclTotal");
            DeclTotal.setTextContent(json.getString("decl_total"));
            EntryList.appendChild(DeclTotal);
            Element UseTo = document.createElement("UseTo");

            EntryList.appendChild(UseTo);
            Element DutyMode = document.createElement("DutyMode");
            DutyMode.setTextContent("");
            EntryList.appendChild(DutyMode);
            Element GQty = document.createElement("GQty");
            GQty.setTextContent(json.getString("g_qty"));
            EntryList.appendChild(GQty);
            Element GUnit = document.createElement("GUnit");
            GUnit.setTextContent(json.containsKey("g_unit") ? json.getString("g_unit") : "");
            EntryList.appendChild(GUnit);
            Element Qty1 = document.createElement("Qty1");
//            Qty1.setTextContent(json.getString("qty_1"));
            EntryList.appendChild(Qty1);
            Element Unit1 = document.createElement("Unit1");
//            Unit1.setTextContent(json.getString("unit_1"));
            EntryList.appendChild(Unit1);
            Element Qty2 = document.createElement("Qty2");
            Qty2.setTextContent("");
            EntryList.appendChild(Qty2);
            Element Unit2 = document.createElement("Unit2");
            Unit2.setTextContent("");
            EntryList.appendChild(Unit2);

            Element GGrossWt = document.createElement("GGrossWt");
            GGrossWt.setTextContent(json.getString("ggrosswt"));
            EntryList.appendChild(GGrossWt);

            Element MName = document.createElement("MName");//生产厂商
            MName.setTextContent("");
            EntryList.appendChild(MName);

            Element OriginCity = document.createElement("OriginCity");//产销城市
            OriginCity.setTextContent("");
            EntryList.appendChild(OriginCity);

            Element GNameEn = document.createElement("GNameEn");//r
            GNameEn.setTextContent(json.getString("g_name_en"));
            EntryList.appendChild(GNameEn);

            exp301.appendChild(EntryList);
        }
    }

    private void createEntryDocuXML(Document document, Element exp301) {
        Element EntryDocu = document.createElement("EntryDocu");
        Element OpType = document.createElement("OpType");
        Element OrderNo = document.createElement("OrderNo");
        Element DocuCode = document.createElement("DocuCode");
        Element CertCode = document.createElement("CertCode");
        OpType.setTextContent("ADD");
        OrderNo.setTextContent("1");
//        DocuCode.setTextContent("01");
//        CertCode.setTextContent("bianhao");

        EntryDocu.appendChild(OpType);
        EntryDocu.appendChild(OrderNo);
        EntryDocu.appendChild(DocuCode);
        EntryDocu.appendChild(CertCode);

        exp301.appendChild(EntryDocu);

    }
}
