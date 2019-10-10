package com.bdaim.common.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.customs.entity.HBusiDataManager;
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
import java.io.File;
import java.util.List;
import java.util.Map;

@Component
public class BaoguandanXmlEXP301 {
/*
    public static void main(String[] args) {
        createXml();
    }*/


    public  void createXml(Map<String,Object>mainMap,Map<String,Object> map, List<HBusiDataManager> ds){
        try {
            // 创建解析器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document document = db.newDocument();
            // 不显示standalone="no"
            document.setXmlStandalone(true);
            Element Package = document.createElement("Package");
            // 向bookstore根节点中添加子节点book
            Element EnvelopInfo = document.createElement("EnvelopInfo");

            createEnvelopInfoXML(document,EnvelopInfo);
            Package.appendChild(EnvelopInfo);

            Element DataInfo = document.createElement("DataInfo");

            createDataInfoXML(document,DataInfo,mainMap,map,ds);

            Package.appendChild(DataInfo);
            document.appendChild(Package);

            // 创建TransformerFactory对象
            TransformerFactory tff = TransformerFactory.newInstance();
            // 创建 Transformer对象
            Transformer tf = tff.newTransformer();

            // 输出内容是否使用换行
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            // 创建xml文件并写入内容
            tf.transform(new DOMSource(document), new StreamResult(new File("E:\\EXP301.xml")));
            System.out.println("生成EXP301.xml成功");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("生成EXP301.xml失败");
        }
    }

    private  void createEnvelopInfoXML(Document document,Element envelopInfo){
        Element version = document.createElement("version");
        Element message_id = document.createElement("message_id");
        Element file_name = document.createElement("file_name");
        Element message_type = document.createElement("message_type");
        Element sender_id = document.createElement("sender_id");
        Element receiver_id = document.createElement("receiver_id");
        Element send_time = document.createElement("send_time");

        version.setTextContent("1.0");
        message_id.setTextContent("0000000000101E0100002012100112303000000000000");
        file_name.setTextContent("0000000000101E0100002012100112303000000000000.EXP");
        message_type.setTextContent("EXP301");
        sender_id.setTextContent("000000000000002006");
        receiver_id.setTextContent("E010000");
        send_time.setTextContent("2019-09-22T11:17:13");

        envelopInfo.appendChild(version);
        envelopInfo.appendChild(message_id);
        envelopInfo.appendChild(file_name);
        envelopInfo.appendChild(message_type);
        envelopInfo.appendChild(sender_id);
        envelopInfo.appendChild(receiver_id);
        envelopInfo.appendChild(send_time);
    }

    private void createDataInfoXML(Document document,Element DataInfo,Map<String,Object> mainMap,Map<String,Object>map ,List<HBusiDataManager>ds){
        Element SignedData = document.createElement("SignedData");
        Element Data = document.createElement("Data");
        Element EXP301 = document.createElementNS("http://www.chinaport.gov.cn/Exp","EXP301");
        String content = (String) mainMap.get("content");
        JSONObject mainjson = JSONObject.parseObject(content);
        String partyContent = (String) map.get("content");
        JSONObject json = JSON.parseObject(partyContent);
        createEntryHeadXML(document,EXP301,mainjson,json);
        createEntryListXML(document,EXP301,ds);
        createEntryDocuXML(document,EXP301);

        Data.appendChild(EXP301);
        SignedData.appendChild(Data);
        DataInfo.appendChild(SignedData);
    }

    private void createEntryHeadXML(Document document, Element EXP301, JSONObject mainJson,JSONObject json){
        Element EntryHead = document.createElement("EntryHead");
        Element OpType = document.createElement("OpType");
        String optype = mainJson.getString("op_type");
        if(StringUtil.isEmpty(optype)){
            optype="ADD";
        }
        OpType.setTextContent(optype);//r
        EntryHead.appendChild(OpType);
        Element PreEntryId = document.createElement("PreEntryId");
        PreEntryId.setTextContent("");
        EntryHead.appendChild(PreEntryId);
        Element EntryId = document.createElement("EntryId");
        EntryId.setTextContent("");
        EntryHead.appendChild(EntryId);

        Element IEFlag = document.createElement("IEFlag");
        IEFlag.setTextContent(mainJson.getString("i_e_flag"));   //r
        EntryHead.appendChild(IEFlag);
        Element IEPort = document.createElement("IEPort");
        IEPort.setTextContent(mainJson.getString("i_e_port"));//r
        EntryHead.appendChild(IEPort);
        Element IEDate = document.createElement("IEDate");
        IEDate.setTextContent(mainJson.getString("i_e_date"));//r
        EntryHead.appendChild(IEDate);
        Element DDate = document.createElement("DDate");
        DDate.setTextContent(mainJson.getString("i_d_date"));//r
        EntryHead.appendChild(DDate);
        Element DestinationPort = document.createElement("DestinationPort");
        DestinationPort.setTextContent(mainJson.getString("depart_arrival_port")); //r
        EntryHead.appendChild(DestinationPort);
        Element TrafName = document.createElement("TrafName");
        TrafName.setTextContent(mainJson.getString("traf_name"));//r
        EntryHead.appendChild(TrafName);
        Element VoyageNo = document.createElement("VoyageNo");
        VoyageNo.setTextContent(mainJson.getString("voyage_no"));//r
        EntryHead.appendChild(VoyageNo);
        Element TrafMode = document.createElement("TrafMode");
        TrafMode.setTextContent(mainJson.getString("traf_mode"));//r
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
        OwnerName.setTextContent("货主单位名称");//r
        EntryHead.appendChild(OwnerName);
        Element AgentType = document.createElement("AgentType");
        AgentType.setTextContent(mainJson.getString("agent_type"));  //0：企业；1：自然人
        EntryHead.appendChild(AgentType);
        Element AgentCode = document.createElement("AgentCode");
        AgentCode.setTextContent(mainJson.containsKey("s_c_code_shipper")?mainJson.getString("s_c_code_shipper"):""); //AgentType=0时必填
        EntryHead.appendChild(AgentCode);
        Element AgentName = document.createElement("AgentName");
        AgentName.setTextContent("AgentName AgentType=0时必填");//AgentType=0时必填
        EntryHead.appendChild(AgentName);
        Element ContrNo = document.createElement("ContrNo");

        EntryHead.appendChild(ContrNo);
        Element BillNo = document.createElement("BillNo");
        BillNo.setTextContent(mainJson.getString("bill_no")); //r 总运单号
        EntryHead.appendChild(BillNo);
        Element AssBillNo = document.createElement("AssBillNo");
        AssBillNo.setTextContent(json.getString("bill_no")); //r  分运单号
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
        PackNo.setTextContent(json.getString("pack_no"));  //r
        EntryHead.appendChild(PackNo);
        Element GrossWt = document.createElement("GrossWt");
        GrossWt.setTextContent(json.getString("weight"));  //r
        EntryHead.appendChild(GrossWt);
        Element NetWt = document.createElement("NetWt");
        NetWt.setTextContent(json.containsKey("netwt")?json.getString("netwt"):"");  //r
        EntryHead.appendChild(NetWt);
        Element WrapType = document.createElement("WrapType");
        WrapType.setTextContent(mainJson.getString("wrap_type"));  //r
        EntryHead.appendChild(WrapType);
        Element NoteS = document.createElement("NoteS");

        EntryHead.appendChild(NoteS);
        Element DeclPort = document.createElement("DeclPort");
        DeclPort.setTextContent(mainJson.getString("decl_port"));  //r
        EntryHead.appendChild(DeclPort);

        Element CoOwner = document.createElement("CoOwner");
        CoOwner.setTextContent("");  //r
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
        Element TypistNo = document.createElement("TypistNo");
        TypistNo.setTextContent("");
        EntryHead.appendChild(TypistNo);
        Element InputNo = document.createElement("InputNo");
        InputNo.setTextContent("8800000026941");  //r
        EntryHead.appendChild(InputNo);
        Element InputCompanyCo = document.createElement("InputCompanyCo");
        InputCompanyCo.setTextContent("1111940112");  //r
        EntryHead.appendChild(InputCompanyCo);
        Element InputCompanyName = document.createElement("InputCompanyName");
        InputCompanyName.setTextContent("HSE华圣国际运输服务有限公司");  //r
        EntryHead.appendChild(InputCompanyName);
//        Element PDate = document.createElement("PDate");
//        EntryHead.appendChild(PDate);
        Element DeclareNo = document.createElement("DeclareNo");
        DeclareNo.setTextContent("daima");  //如果AgentType=1，必不填,否则必填
        EntryHead.appendChild(DeclareNo);
        Element CustomsField = document.createElement("CustomsField");
        CustomsField.setTextContent(mainJson.getString("wharf_ yard_code"));  //r
        EntryHead.appendChild(CustomsField);
        /*Element SpecialFlag = document.createElement("SpecialFlag");
        EntryHead.appendChild(SpecialFlag);*/
        Element KjId = document.createElement("KjId");

        EntryHead.appendChild(KjId);
        Element SendName = document.createElement("SendName");
        SendName.setTextContent(mainJson.getString("send_name")); //r
        EntryHead.appendChild(SendName);
        Element ReceiveName = document.createElement("ReceiveName");
        ReceiveName.setTextContent(json.getString("receive_name"));  //r
        EntryHead.appendChild(ReceiveName);
        Element SendCountry = document.createElement("SendCountry");
        SendCountry.setTextContent(mainJson.getString("send_country"));  //r
        EntryHead.appendChild(SendCountry);
        Element SendCity = document.createElement("SendCity");
        SendCity.setTextContent(mainJson.getString("send_city"));  //r
        EntryHead.appendChild(SendCity);
        Element SendId = document.createElement("SendId");
        SendId.setTextContent(json.getString("id_no"));  //r 收发件人证件号码
        EntryHead.appendChild(SendId);
        Element TotalValue = document.createElement("TotalValue");
        TotalValue.setTextContent(json.getString("total_value"));  //r 价值 DECIMAL(19, 4)
        EntryHead.appendChild(TotalValue);
        Element CurrCode = document.createElement("CurrCode");
        CurrCode.setTextContent(json.getString("curr_code"));  //r
        EntryHead.appendChild(CurrCode);
        /*Element ReceiveDate = document.createElement("ReceiveDate");
        ReceiveDate.setTextContent("");
        EntryHead.appendChild(ReceiveDate);*/
       /* Element ChannelEr = document.createElement("ChannelEr");
        ChannelEr.setTextContent("");
        EntryHead.appendChild(ChannelEr);*/
        Element MainGName = document.createElement("MainGName");
        MainGName.setTextContent(json.getString("main_gname"));  //r
        EntryHead.appendChild(MainGName);
        Element EntryType = document.createElement("EntryType");
        EntryType.setTextContent(mainJson.getString("entry_type"));  //r
        EntryHead.appendChild(EntryType);
        Element SendIdType = document.createElement("SendIdType");
        SendIdType.setTextContent(json.getString("id_type"));  //收发件人证件类型,B类必填
        EntryHead.appendChild(SendIdType);

        EXP301.appendChild(EntryHead);
    }

    private void createEntryListXML(Document document,Element exp301,List<HBusiDataManager> ds){
        for(HBusiDataManager map:ds) {
            Element EntryList = document.createElement("EntryList");
            Element OpType = document.createElement("OpType");
            String content =  map.getContent();
            JSONObject json = JSON.parseObject(content);
            String optype=json.getString("op_type");
            if(StringUtil.isEmpty(optype)){
                optype="ADD";
            }
            OpType.setTextContent(optype);
            EntryList.appendChild(OpType);
            Element GNo = document.createElement("GNo");
            GNo.setTextContent("1");
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
            OriginCountry.setTextContent(json.containsKey("origin_country")?json.getString("origin_country"):"");
            EntryList.appendChild(OriginCountry);
            Element TradeCurr = document.createElement("TradeCurr");
            TradeCurr.setTextContent(json.containsKey("trade_curr")?json.getString("trade_curr"):"");
            EntryList.appendChild(TradeCurr);
//            Element ExchangeRate = document.createElement("ExchangeRate");
//
//            EntryList.appendChild(ExchangeRate);
            Element TradeTotal = document.createElement("TradeTotal");
            TradeTotal.setTextContent(json.getString("trade_total"));
            EntryList.appendChild(TradeTotal);
            Element DeclPrice = document.createElement("DeclPrice");
            DeclPrice.setTextContent(json.containsKey("decl_price")?json.getString("decl_price"):"");
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
            GUnit.setTextContent(json.containsKey("g_unit")?json.getString("g_unit"):"");
            EntryList.appendChild(GUnit);
            Element Qty1 = document.createElement("Qty1");
            Qty1.setTextContent("");
            EntryList.appendChild(Qty1);
            Element Unit1 = document.createElement("Unit1");
            Unit1.setTextContent("");
            EntryList.appendChild(Unit1);
            Element Qty2 = document.createElement("Qty2");
            Qty2.setTextContent("");
            EntryList.appendChild(Qty2);
            Element Unit2 = document.createElement("Unit2");
            Unit2.setTextContent("");
            EntryList.appendChild(Unit2);
            /*Element ClassMark = document.createElement("ClassMark");
            ClassMark.setTextContent("");
            EntryList.appendChild(ClassMark);*/
            Element GGrossWt = document.createElement("GGrossWt");
            GGrossWt.setTextContent(json.getString("ggrosswt"));
            EntryList.appendChild(GGrossWt);

            exp301.appendChild(EntryList);
        }
    }

    private void createEntryDocuXML(Document document,Element exp301){
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
