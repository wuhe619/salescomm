package com.bdaim.common.util;

import com.alibaba.fastjson.JSONObject;
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
import java.util.List;
import java.util.Map;

@Component
public class CangdanXmlEXP311 {
/*
    public static void main(String[] args) {
        createXml();
    }
    */

    public  String createXml(Map<String,Object> map, List<Map<String, Object>> dsList){
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

            createDataInfoXML(document,DataInfo,map,dsList);

            Package.appendChild(DataInfo);
            document.appendChild(Package);

            // 创建TransformerFactory对象
            TransformerFactory tff = TransformerFactory.newInstance();
            // 创建 Transformer对象
            Transformer tf = tff.newTransformer();

            // 输出内容是否使用换行
            tf.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "");
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            // 创建xml文件并写入内容
           /* tf.transform(new DOMSource(document), new StreamResult(new File("E:\\EXP311.xml")));
            System.out.println("生成EXP311.xml成功");*/


            //生成xml内容字符串
            DOMSource domSource = new DOMSource(document);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // 使用Transformer的transform()方法将DOM树转换成XML
            tf.transform(domSource, new StreamResult(bos));
            String xmlString = bos.toString();
            System.out.println(xmlString);
            return xmlString;

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("生成EXP311.xml失败");
        }
        return null;
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
        message_type.setTextContent("EXP311");
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

    private  void createDataInfoXML(Document document,Element DataInfo,Map<String,Object> m,List<Map<String, Object>> dsList){
        Element SignedData = document.createElement("SignedData");
        Element Data = document.createElement("Data");
        Element EXP311 = document.createElementNS("http://www.chinaport.gov.cn/Exp","EXP311");

        String mainContent = (String) m.get("content");
        JSONObject json = JSONObject.parseObject(mainContent);

        createEntryHeadXML(document,EXP311,json);
        createExpMftListXML(document,EXP311,m.get("ext_3").toString(),json.getString("voyage_no"),dsList);

        Data.appendChild(EXP311);
        SignedData.appendChild(Data);
        DataInfo.appendChild(SignedData);
    }

    private  void createEntryHeadXML(Document document,Element EXP311,JSONObject json){

        Element ExpMftHead = document.createElement("ExpMftHead");
        Element OpType = document.createElement("OpType");
        String optype = json.getString("op_type");
        if(StringUtil.isEmpty(optype)){
            optype="ADD";
        }
        OpType.setTextContent(optype);
        ExpMftHead.appendChild(OpType);

        Element BillNo = document.createElement("BillNo");
        BillNo.setTextContent(json.getString("bill_no"));
        ExpMftHead.appendChild(BillNo);
        Element VoyageNo = document.createElement("VoyageNo");
        VoyageNo.setTextContent(json.getString("voyage_no"));
        ExpMftHead.appendChild(VoyageNo);

        Element IEFlag = document.createElement("IEFlag");
        IEFlag.setTextContent(json.getString("i_e_flag"));   //r
        ExpMftHead.appendChild(IEFlag);
        Element TrafCnName = document.createElement("TrafCnName");
        TrafCnName.setTextContent(json.getString("traf_name"));//r
        ExpMftHead.appendChild(TrafCnName);
        Element TrafEnName = document.createElement("TrafEnName");
        TrafEnName.setTextContent(json.getString("traf_name_en"));//r
        ExpMftHead.appendChild(TrafEnName);
        Element GrossWt = document.createElement("GrossWt");
        GrossWt.setTextContent(json.getString("gross_wt"));//r
        ExpMftHead.appendChild(GrossWt);
        Element PackNo = document.createElement("PackNo");
        PackNo.setTextContent(json.getString("pack_no")); //r
        ExpMftHead.appendChild(PackNo);

        Element BillNum = document.createElement("BillNum");
        BillNum.setTextContent(json.getString("single_batch_num"));//r
        ExpMftHead.appendChild(BillNum);
        Element TrafMode = document.createElement("TrafMode");
        TrafMode.setTextContent(json.getString("traf_mode"));//r
        ExpMftHead.appendChild(TrafMode);

        Element IEDate = document.createElement("IEDate");
        IEDate.setTextContent(json.getString("i_e_date"));
        ExpMftHead.appendChild(IEDate);
        Element DestinationPort = document.createElement("DestinationPort");
        DestinationPort.setTextContent(json.getString("depart_arrival_port"));
        ExpMftHead.appendChild(DestinationPort);
        Element IEPort = document.createElement("IEPort");
        IEPort.setTextContent(json.getString("i_e_port"));
        ExpMftHead.appendChild(IEPort);
        Element TradeCo = document.createElement("TradeCo");
        TradeCo.setTextContent(json.getString("s_c_code_busi_unit"));//r
        ExpMftHead.appendChild(TradeCo);
        Element TradeName = document.createElement("TradeName");
        TradeName.setTextContent(json.getString("business_unit_name"));
        ExpMftHead.appendChild(TradeName);

        Element InputNo = document.createElement("InputNo");
        InputNo.setTextContent("录入人卡号");//
        ExpMftHead.appendChild(InputNo);

        Element InputOpName = document.createElement("InputOpName");
        InputOpName.setTextContent("录入人姓名");
        ExpMftHead.appendChild(InputOpName);


        Element InputCompanyCode = document.createElement("InputCompanyCode");
        InputCompanyCode.setTextContent("录入单位代码");
        ExpMftHead.appendChild(InputCompanyCode);

        Element InputCompanyName = document.createElement("InputCompanyName");
        InputCompanyName.setTextContent("录入单位名称");
        ExpMftHead.appendChild(InputCompanyName);

        EXP311.appendChild(ExpMftHead);
    }

    private  void createExpMftListXML(Document document,Element exp311,String billNo,String VoyageNoStr,List<Map<String, Object>> dsList){
        for(Map<String,Object> m :dsList) {
            String content = (String) m.get("content");
            JSONObject json = JSONObject.parseObject(content);

            Element ExpMftList = document.createElement("ExpMftList");
            Element BillNo = document.createElement("BillNo");
            BillNo.setTextContent(billNo);
            ExpMftList.appendChild(BillNo);
            Element AssBillNo = document.createElement("AssBillNo");
            AssBillNo.setTextContent(json.getString("bill_no"));
            ExpMftList.appendChild(AssBillNo);
            Element VoyageNo = document.createElement("VoyageNo");
            VoyageNo.setTextContent(VoyageNoStr);
            ExpMftList.appendChild(VoyageNo);
            Element MainGName = document.createElement("MainGName");
            MainGName.setTextContent(json.getString("main_gname"));
            ExpMftList.appendChild(MainGName);
            Element PackNo = document.createElement("PackNo");
            PackNo.setTextContent(json.getString("pack_no"));
            ExpMftList.appendChild(PackNo);
            Element GrossWt = document.createElement("GrossWt");
            GrossWt.setTextContent(json.getString("weight"));
            ExpMftList.appendChild(GrossWt);
            Element TradeTotal = document.createElement("TradeTotal");
            TradeTotal.setTextContent(json.containsKey("total_value")?json.getString("total_value"):"");
            ExpMftList.appendChild(TradeTotal);
            Element TradeCurr = document.createElement("TradeCurr");
            TradeCurr.setTextContent(json.containsKey("curr_code")?json.getString("curr_code"):"");
            ExpMftList.appendChild(TradeCurr);

            exp311.appendChild(ExpMftList);
        }
    }

}
