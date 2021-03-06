package com.bdaim.util;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
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
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class CangdanXmlEXP311 {
    private static Logger log = LoggerFactory.getLogger(CangdanXmlEXP311.class);
/*
    public static void main(String[] args) {
        createXml();
    }
    */

    public  String createXml(Map<String,Object> map, List<Map<String, Object>> dsList,Map<String,Object> customerInfo) throws Exception {
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

            createEnvelopInfoXML(document,EnvelopInfo,customerInfo, String.valueOf(map.get("id")));
            Package.appendChild(EnvelopInfo);
            Element DataInfo = document.createElement("DataInfo");

            createDataInfoXML(document,DataInfo,map,dsList,customerInfo);

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
            log.info(xmlString);
            return xmlString;
        } catch (Exception e) {
            log.error("生成EXP311.xml失败",e);
        }
        return null;
    }

    private  void createEnvelopInfoXML(Document document,Element envelopInfo,Map<String,Object>custInfo,String id){
        Element version = document.createElement("version");
        Element message_id = document.createElement("message_id");
        Element file_name = document.createElement("file_name");
        Element message_type = document.createElement("message_type");
        Element sender_id = document.createElement("sender_id");
        Element receiver_id = document.createElement("receiver_id");
        Element send_time = document.createElement("send_time");

        version.setTextContent("1.0");
        String dateStr=DateUtil.fmtDateToStr(new Date(),"yyyyMMddHHmmss");
        String idstr = getId11NO(id);
        String msg_id = custInfo.get("sender_id").toString()+"E010000"+dateStr+idstr;
        message_id.setTextContent(msg_id);
        file_name.setTextContent(msg_id+".EXP");
        message_type.setTextContent("EXP311");
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

    private  void createDataInfoXML(Document document,Element DataInfo,Map<String,Object> m,List<Map<String, Object>> dsList,Map<String,Object>custInfo){
        Element SignedData = document.createElement("SignedData");
        Element Data = document.createElement("Data");
        Element EXP311 = document.createElementNS("http://www.chinaport.gov.cn/Exp","EXP311");

        String mainContent = (String) m.get("content");
        JSONObject json = JSONObject.parseObject(mainContent);

        createEntryHeadXML(document,EXP311,json,custInfo);
        createExpMftListXML(document,EXP311,m.get("ext_3").toString(),json.getString("voyage_no"),dsList);

        Data.appendChild(EXP311);
        SignedData.appendChild(Data);
        DataInfo.appendChild(SignedData);
    }

    private  void createEntryHeadXML(Document document,Element EXP311,JSONObject json,Map<String,Object> custInfo){

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
        PackNo.setTextContent(json.getString("total_pack_no")); //r
        ExpMftHead.appendChild(PackNo);

        Element BillNum = document.createElement("BillNum");
        BillNum.setTextContent(json.getString("single_batch_num"));//r
        ExpMftHead.appendChild(BillNum);
        Element TrafMode = document.createElement("TrafMode");
        TrafMode.setTextContent(json.getString("traf_mode"));//r
        ExpMftHead.appendChild(TrafMode);

        Element IEDate = document.createElement("IEDate");
        IEDate.setTextContent(json.getString("i_e_date").replace("-",""));
        ExpMftHead.appendChild(IEDate);
        Element DestinationPort = document.createElement("DestinationPort");
        DestinationPort.setTextContent(json.getString("depart_arrival_port"));
        ExpMftHead.appendChild(DestinationPort);
        Element IEPort = document.createElement("IEPort");
        IEPort.setTextContent(json.getString("decl_port"));//进出口岸取申报地海关的值
        ExpMftHead.appendChild(IEPort);
        Element TradeCo = document.createElement("TradeCo");
        TradeCo.setTextContent(custInfo.get("agent_code")==null?"":custInfo.get("agent_code").toString());//r
        ExpMftHead.appendChild(TradeCo);
        Element TradeName = document.createElement("TradeName");
        TradeName.setTextContent((String) custInfo.get("enterpriseName"));
        ExpMftHead.appendChild(TradeName);

        Element InputNo = document.createElement("InputNo");
        InputNo.setTextContent((String) custInfo.get("input_no"));//录入人卡号
        ExpMftHead.appendChild(InputNo);

        Element InputOpName = document.createElement("InputOpName");
        InputOpName.setTextContent((String) custInfo.get("input_name"));//录入人姓名
        ExpMftHead.appendChild(InputOpName);


        Element InputCompanyCode = document.createElement("InputCompanyCode");
        InputCompanyCode.setTextContent((String) custInfo.getOrDefault("agent_code",""));//录入单位代码
        ExpMftHead.appendChild(InputCompanyCode);

        Element InputCompanyName = document.createElement("InputCompanyName");
        InputCompanyName.setTextContent((String) custInfo.getOrDefault("enterpriseName",""));//录入单位名称
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
            String gname = json.getString("main_gname");
            if(gname.endsWith("|")){
                gname = gname.replace("|","");
            }
            MainGName.setTextContent(gname);
            ExpMftList.appendChild(MainGName);
            Element PackNo = document.createElement("PackNo");
            PackNo.setTextContent(json.getString("pack_no"));
            ExpMftList.appendChild(PackNo);
            Element GrossWt = document.createElement("GrossWt");
            String weight = json.getString("weight");
            if(StringUtil.isNotEmpty(weight)) {
                BigDecimal b = new BigDecimal(weight).setScale(5, RoundingMode.HALF_UP);
                GrossWt.setTextContent(b.toPlainString());
            }
            ExpMftList.appendChild(GrossWt);
            Element TradeTotal = document.createElement("TradeTotal");
            String total_value=json.containsKey("total_value")?json.getString("total_value"):"";
            if(StringUtil.isNotEmpty(total_value)) {
                BigDecimal b=new BigDecimal(total_value).setScale(2, RoundingMode.HALF_UP);
                TradeTotal.setTextContent(b.toPlainString());
            }
            ExpMftList.appendChild(TradeTotal);
            Element TradeCurr = document.createElement("TradeCurr");
            TradeCurr.setTextContent(json.containsKey("curr_code")?json.getString("curr_code"):"");
            ExpMftList.appendChild(TradeCurr);

            exp311.appendChild(ExpMftList);
        }
    }

    private  String getId11NO(String id){
        while(id.length()<11){
            id="0"+id;
        }
        return id;
    }

    public static void main(String[] args) {
        /*String total_value="123.3";
        BigDecimal b=new BigDecimal(total_value).setScale(2, RoundingMode.HALF_UP);
        System.out.println(b.toPlainString());*/
        System.out.println("撒反对".trim().length());
    }

}
