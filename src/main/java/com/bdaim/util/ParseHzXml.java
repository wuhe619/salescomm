package com.bdaim.util;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.exception.TouchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.Base64;


@Component
public class ParseHzXml {

    private static Logger log = LoggerFactory.getLogger(ParseHzXml.class);

    /**
     * 解析舱单回执
     *
     * @param strXML
     * @param info
     * @throws Exception
     */
    public void parserCangdanHzXML(String strXML, JSONObject info) throws Exception {
        JSONObject envelopData = new JSONObject();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader sr = new StringReader(strXML);
            InputSource is = new InputSource(sr);
            Document doc = builder.parse(is);
            Element rootElement = doc.getDocumentElement();
            NodeList EnvelopInfo = rootElement.getElementsByTagName("EnvelopInfo");
            for (int i = 0; i < EnvelopInfo.getLength(); i++) {
                Node envelopInfo = EnvelopInfo.item(i);
                NodeList properties = envelopInfo.getChildNodes();
                for (int j = 0; j < properties.getLength(); j++) {
                    Node property = properties.item(j);
                    String nodeName = property.getNodeName();
                    if (nodeName.equals("message_id")) {
                        String message_id = property.getFirstChild().getNodeValue();
                        log.info("message_id=" + message_id);
                        envelopData.put("message_id", message_id);
                    } else if (nodeName.equals("file_name")) {
                        String file_name = property.getFirstChild().getNodeValue();
                        log.info("file_name=" + file_name);
                        envelopData.put("file_name", file_name);
                    } else if (nodeName.equals("message_type")) {
                        String message_type = property.getFirstChild().getNodeValue();
                        log.info("message_type=" + message_type);
                        envelopData.put("message_type", message_type);
                    } else if (nodeName.equals("sender_id")) {
                        String sender_id = property.getFirstChild().getNodeValue();
                        log.info("sender_id=" + sender_id);
                        envelopData.put("sender_id", sender_id);
                    } else if (nodeName.equals("receiver_id")) {
                        String receiver_id = property.getFirstChild().getNodeValue();
                        log.info("receiver_id=" + receiver_id);
                        envelopData.put("sender_id", receiver_id);
                    } else if (nodeName.equals("send_time")) {
                        String send_time = property.getFirstChild().getNodeValue();
                        log.info("send_time=" + send_time);
                        envelopData.put("sender_id", send_time);
                    } else if (nodeName.equals("version")) {
                        String version = property.getFirstChild().getNodeValue();
                        log.info("version=" + version);
                        envelopData.put("sender_id", version);
                    }
                }
            }
            info.put("envelopinfo", envelopData);
            JSONObject headData = new JSONObject();
            NodeList ExpMftHead = rootElement.getElementsByTagName("ExpMftHead");
            for (int i = 0; i < ExpMftHead.getLength(); i++) {
                Node d = ExpMftHead.item(i);
                //String phoneName = ((Element)type).getAttribute("name");
                NodeList properties = d.getChildNodes();
                for (int j = 0; j < properties.getLength(); j++) {
                    Node property = properties.item(j);
                    String nodeName = property.getNodeName();
                    if (nodeName.equals("BillNo")) {
                        String BillNo = property.getFirstChild().getNodeValue();
                        log.info("BillNo=" + BillNo);
                        headData.put("billno", BillNo);
                    } else if (nodeName.equals("VoyageNo")) {
                        String VoyageNo = property.getFirstChild().getNodeValue();
                        log.info("VoyageNo=" + VoyageNo);
                        headData.put("voyageno", VoyageNo);
                    } else if (nodeName.equals("EntryDate")) {
                        String EntryDate = property.getFirstChild().getNodeValue();
                        log.info("EntryDate=" + EntryDate);
                        headData.put("entrydate", EntryDate);
                    } else if (nodeName.equals("RtnFlag")) {
                        String RtnFlag = property.getFirstChild().getNodeValue();
                        log.info("RtnFlag=" + RtnFlag);
                        headData.put("rtnflag", RtnFlag);
                    } else if (nodeName.equals("Notes")) {
                        String Notes = property.getFirstChild() == null ? "" : property.getFirstChild().getNodeValue();
                        log.info("Notes=" + Notes == null ? "" : Notes);
                        headData.put("notes", Notes);
                    }
                }
            }
            info.put("headData", headData);
            JSONArray array = new JSONArray();
            NodeList ExpMftList = rootElement.getElementsByTagName("ExpMftList");
            for (int i = 0; i < ExpMftList.getLength(); i++) {
                Node d = ExpMftList.item(i);
                //String phoneName = ((Element)type).getAttribute("name");
                NodeList properties = d.getChildNodes();
                JSONObject item = new JSONObject();
                for (int j = 0; j < properties.getLength(); j++) {
                    Node property = properties.item(j);
                    String nodeName = property.getNodeName();
                    if (nodeName.equals("BillNo")) {
                        String BillNo = property.getFirstChild().getNodeValue();
                        log.info("BillNo=" + BillNo);
                        item.put("billno", BillNo);
                    } else if (nodeName.equals("AssBillNo")) {
                        String AssBillNo = property.getFirstChild().getNodeValue();
                        log.info("AssBillNo=" + AssBillNo);
                        item.put("assbillno", AssBillNo);
                    } else if (nodeName.equals("EntryDate")) {
                        String EntryDate = property.getFirstChild().getNodeValue();
                        log.info("EntryDate=" + EntryDate);
                        item.put("entrydate", EntryDate);
                    } else if (nodeName.equals("RtnFlag")) {
                        String RtnFlag = property.getFirstChild().getNodeValue();
                        log.info("RtnFlag=" + RtnFlag);
                        item.put("rtnflag", RtnFlag);
                    } else if (nodeName.equals("Notes")) {
                        String Notes = property.getFirstChild() == null ? "" : property.getFirstChild().getNodeValue();
                        log.info("Notes=" + Notes);
                        item.put("notes", Notes);
                    }
                }
                array.add(item);
            }
            info.put("list", array);
        } catch (Exception e) {
            log.info("解析舱单回执失败", e);
            throw new Exception();
        }
    }


    /**
     * 解析报单回执
     *
     * @param strXML
     * @param info
     */
    public void parserBaoguandanHzXML(String strXML, JSONObject info) throws Exception {
        JSONObject envelopData = new JSONObject();
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader sr = new StringReader(strXML);
            InputSource is = new InputSource(sr);
            Document doc = builder.parse(is);
            Element rootElement = doc.getDocumentElement();
            NodeList EnvelopInfo = rootElement.getElementsByTagName("EnvelopInfo");
            for (int i = 0; i < EnvelopInfo.getLength(); i++) {
                Node envelopInfo = EnvelopInfo.item(i);
                //String phoneName = ((Element)type).getAttribute("name");
                NodeList properties = envelopInfo.getChildNodes();
                for (int j = 0; j < properties.getLength(); j++) {
                    Node property = properties.item(j);
                    String nodeName = property.getNodeName();
                    if (nodeName.equals("message_id")) {
                        String message_id = property.getFirstChild().getNodeValue();
                        log.info("message_id=" + message_id);
                        envelopData.put("message_id", message_id);
                    } else if (nodeName.equals("file_name")) {
                        String file_name = property.getFirstChild().getNodeValue();
                        log.info("file_name=" + file_name);
                        envelopData.put("file_name", file_name);
                    } else if (nodeName.equals("message_type")) {
                        String message_type = property.getFirstChild().getNodeValue();
                        log.info("message_type=" + message_type);
                        envelopData.put("message_type", message_type);
                    } else if (nodeName.equals("sender_id")) {
                        String sender_id = property.getFirstChild().getNodeValue();
                        log.info("sender_id=" + sender_id);
                        envelopData.put("sender_id", sender_id);
                    } else if (nodeName.equals("receiver_id")) {
                        String receiver_id = property.getFirstChild().getNodeValue();
                        log.info("receiver_id=" + receiver_id);
                        envelopData.put("receiver_id", receiver_id);
                    } else if (nodeName.equals("send_time")) {
                        String send_time = property.getFirstChild().getNodeValue();
                        log.info("send_time=" + send_time);
                        envelopData.put("send_time", send_time);
                    } else if (nodeName.equals("version")) {
                        String version = property.getFirstChild().getNodeValue();
                        log.info("version=" + version);
                        envelopData.put("version", version);
                    }
                }
            }
            info.put("envelopinfo", envelopData);
            JSONObject headData = new JSONObject();
            NodeList EntryHead = rootElement.getElementsByTagName("EntryHead");
            for (int i = 0; i < EntryHead.getLength(); i++) {
                Node d = EntryHead.item(i);
                NodeList properties = d.getChildNodes();
                for (int j = 0; j < properties.getLength(); j++) {
                    Node property = properties.item(j);
                    String nodeName = property.getNodeName();
                    if (nodeName.equals("PreEntryId")) {
                        String PreEntryId = property.getFirstChild().getNodeValue();
                        log.info("PreEntryId=" + PreEntryId);
                        headData.put("pre_entryid", PreEntryId);
                    } else if (nodeName.equals("OpType")) {
                        String OpType = property.getFirstChild().getNodeValue();
                        log.info("OpType=" + OpType);
                        headData.put("op_type", OpType);
                    } else if (nodeName.equals("BillNo")) {
                        String BillNo = property.getFirstChild().getNodeValue();
                        log.info("BillNo=" + BillNo);
                        headData.put("billno", BillNo);
                    } else if (nodeName.equals("AssBillNo")) {
                        String AssBillNo = property.getFirstChild().getNodeValue();
                        log.info("AssBillNo=" + AssBillNo);
                        headData.put("ass_billno", AssBillNo);
                    } else if (nodeName.equals("IEFlag")) {
                        String IEFlag = property.getFirstChild() == null ? "" : property.getFirstChild().getNodeValue();
                        log.info("IEFlag=" + IEFlag == null ? "" : IEFlag);
                        headData.put("i_e_flag", IEFlag);
                    } else if (nodeName.equals("EntryId")) {
                        String EntryId = property.getFirstChild() == null ? "" : property.getFirstChild().getNodeValue();
                        log.info("EntryId=" + EntryId == null ? "" : EntryId);
                        headData.put("entryid", EntryId);
                    } else if (nodeName.equals("OpTime")) {
                        String OpTime = property.getFirstChild() == null ? "" : property.getFirstChild().getNodeValue();
                        log.info("OpTime=" + OpTime == null ? "" : OpTime);
                        headData.put("op_time", OpTime);
                    } else if (nodeName.equals("OpResult")) {
                        String OpResult = property.getFirstChild() == null ? "" : property.getFirstChild().getNodeValue();
                        log.info("OpResult=" + OpResult == null ? "" : OpResult);
                        headData.put("op_result", OpResult);
                    } else if (nodeName.equals("Notes")) {
                        String Notes = property.getFirstChild() == null ? "" : property.getFirstChild().getNodeValue();
                        log.info("Notes=" + Notes == null ? "" : Notes);
                        headData.put("notes", Notes);
                    }
                }
            }
            info.put("data", headData);
        } catch (Exception e) {
            log.info("解析报关单回执失败", e);
            throw new Exception("解析报关单回执失败");
        }
    }

    public String createXML() {
        String xmlStr = null;
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document document = builder.newDocument();
            document.setXmlVersion("1.0");

            Element root = document.createElement("root");
            document.appendChild(root);

            Element telephone = document.createElement("TelePhone");

            Element nokia = document.createElement("type");
            nokia.setAttribute("name", "nokia");

            Element priceNokia = document.createElement("price");
            priceNokia.setTextContent("599");
            nokia.appendChild(priceNokia);

            Element operatorNokia = document.createElement("operator");
            operatorNokia.setTextContent("CMCC");
            nokia.appendChild(operatorNokia);

            telephone.appendChild(nokia);

            Element xiaomi = document.createElement("type");
            xiaomi.setAttribute("name", "xiaomi");

            Element priceXiaoMi = document.createElement("price");
            priceXiaoMi.setTextContent("699");
            xiaomi.appendChild(priceXiaoMi);

            Element operatorXiaoMi = document.createElement("operator");
            operatorXiaoMi.setTextContent("ChinaNet");
            xiaomi.appendChild(operatorXiaoMi);

            telephone.appendChild(xiaomi);

            root.appendChild(telephone);

            TransformerFactory transFactory = TransformerFactory.newInstance();
            Transformer transFormer = transFactory.newTransformer();
            DOMSource domSource = new DOMSource(document);

            //export string
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            transFormer.transform(domSource, new StreamResult(bos));
            xmlStr = bos.toString();

            //-------
            //save as file
            File file = new File("TelePhone.xml");
            if (!file.exists()) {
                file.createNewFile();
            }
            FileOutputStream out = new FileOutputStream(file);
            StreamResult xmlResult = new StreamResult(out);
            transFormer.transform(domSource, xmlResult);
            //--------
        } catch (ParserConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerConfigurationException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (TransformerException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        return xmlStr;
    }

    /**
     * 获取xml中的消息类型
     * @param strXml
     * @return
     * @throws Exception
     */
    public String getMessageTypeByXml(String strXml) throws TouchException {
        String messageType = "";
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        try {
            DocumentBuilder builder = factory.newDocumentBuilder();
            StringReader sr = new StringReader(strXml);
            InputSource is = new InputSource(sr);
            Document doc = builder.parse(is);
            Element rootElement = doc.getDocumentElement();
            NodeList EnvelopInfo = rootElement.getElementsByTagName("EnvelopInfo");
            for (int i = 0; i < EnvelopInfo.getLength(); i++) {
                Node envelopInfo = EnvelopInfo.item(i);
                NodeList properties = envelopInfo.getChildNodes();
                for (int j = 0; j < properties.getLength(); j++) {
                    Node property = properties.item(j);
                    String nodeName = property.getNodeName();
                    // 解析消息类型
                    if ("message_type".equals(nodeName)) {
                        messageType = property.getFirstChild().getNodeValue();
                        break;
                    }
                }
            }
        } catch (Exception e) {
            log.warn("解析舱单回执失败,xml:{}, 异常:{}", strXml,e);
            throw new TouchException("解析舱单回执失败,xml:{}", strXml);
        }
        return messageType;
    }

    public static void main(String[] args) {
        //String x="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz4NCjxQYWNrYWdlPg0KICA8RW52ZWxvcEluZm8+DQogICAgPHZlcnNpb24+MS4wPC92ZXJzaW9uPg0KICAgIDxtZXNzYWdlX2lkPjAwMDAwMDAwMDAxMDFFMDEwMDAwMjAxMjEwMDExMjMwMzAwMDAwMDAwMDAwMDwvbWVzc2FnZV9pZD4NCiAgICA8ZmlsZV9uYW1lPjAwMDAwMDAwMDAxMDFFMDEwMDAwMjAxMjEwMDExMjMwMzAwMDAwMDAwMDAwMC5FWFA8L2ZpbGVfbmFtZT4NCiAgICA8bWVzc2FnZV90eXBlPkVYUDMwMTwvbWVzc2FnZV90eXBlPg0KICAgIDxzZW5kZXJfaWQ+MDAwMDAwMDAwMDAwMDAyMDA2PC9zZW5kZXJfaWQ+DQogICAgPHJlY2VpdmVyX2lkPkUwMTAwMDA8L3JlY2VpdmVyX2lkPg0KICAgIDxzZW5kX3RpbWU+MjAxOS0wOS0yMlQxMToxNzoxMzwvc2VuZF90aW1lPg0KICA8L0VudmVsb3BJbmZvPg0KICA8RGF0YUluZm8+DQogICAgPFNpZ25lZERhdGE+DQogICAgICA8RGF0YT4NCiAgICAgICAgPEVYUDMwMSB4bWxucz0iaHR0cDovL3d3dy5jaGluYXBvcnQuZ292LmNuL0V4cCI+DQogICAgICAgICAgPEVudHJ5SGVhZD4NCiAgICAgICAgICAgIDxPcFR5cGU+QUREPC9PcFR5cGU+DQogICAgICAgICAgICA8UHJlRW50cnlJZCAvPg0KICAgICAgICAgICAgPEVudHJ5SWQgLz4NCiAgICAgICAgICAgIDxJRUZsYWc+STwvSUVGbGFnPg0KICAgICAgICAgICAgPElFUG9ydD4wMjE1PC9JRVBvcnQ+DQogICAgICAgICAgICA8SUVEYXRlPjIwMTktMDktMjc8L0lFRGF0ZT4NCiAgICAgICAgICAgIDxERGF0ZT4yMDE5LTA4LTAzPC9ERGF0ZT4NCiAgICAgICAgICAgIDxEZXN0aW5hdGlvblBvcnQ+MTA2PC9EZXN0aW5hdGlvblBvcnQ+DQogICAgICAgICAgICA8VHJhZk5hbWU+aWlpPC9UcmFmTmFtZT4NCiAgICAgICAgICAgIDxWb3lhZ2VObz5RRjEwNzwvVm95YWdlTm8+DQogICAgICAgICAgICA8VHJhZk1vZGU+MTwvVHJhZk1vZGU+DQogICAgICAgICAgICA8VHJhZGVDbyAvPg0KICAgICAgICAgICAgPFRyYWRlTmFtZSAvPg0KICAgICAgICAgICAgPERpc3RyaWN0Q29kZSAvPg0KICAgICAgICAgICAgPE93bmVyQ29kZSAvPg0KICAgICAgICAgICAgPE93bmVyTmFtZT7otKfkuLvljZXkvY3lkI3np7A8L093bmVyTmFtZT4NCiAgICAgICAgICAgIDxBZ2VudFR5cGU+MDwvQWdlbnRUeXBlPg0KICAgICAgICAgICAgPEFnZW50Q29kZSAvPg0KICAgICAgICAgICAgPEFnZW50TmFtZT5BZ2VudE5hbWUgQWdlbnRUeXBlPTDml7blv4Xloas8L0FnZW50TmFtZT4NCiAgICAgICAgICAgIDxDb250ck5vIC8+DQogICAgICAgICAgICA8QmlsbE5vPjA5MjYyOTY1MDEwPC9CaWxsTm8+DQogICAgICAgICAgICA8QXNzQmlsbE5vPkcwMDU5Mzc3MDM2NTwvQXNzQmlsbE5vPg0KICAgICAgICAgICAgPFRyYWRlQ291bnRyeT42MDE8L1RyYWRlQ291bnRyeT4NCiAgICAgICAgICAgIDxUcmFkZU1vZGUgLz4NCiAgICAgICAgICAgIDxDdXRNb2RlIC8+DQogICAgICAgICAgICA8VHJhbnNNb2RlIC8+DQogICAgICAgICAgICA8RmVlTWFyayAvPg0KICAgICAgICAgICAgPEZlZUN1cnIgLz4NCiAgICAgICAgICAgIDxGZWVSYXRlIC8+DQogICAgICAgICAgICA8SW5zdXJNYXJrIC8+DQogICAgICAgICAgICA8SW5zdXJDdXJyIC8+DQogICAgICAgICAgICA8SW5zdXJSYXRlIC8+DQogICAgICAgICAgICA8T3RoZXJNYXJrIC8+DQogICAgICAgICAgICA8T3RoZXJDdXJyIC8+DQogICAgICAgICAgICA8T3RoZXJSYXRlIC8+DQogICAgICAgICAgICA8UGFja05vPjE8L1BhY2tObz4NCiAgICAgICAgICAgIDxHcm9zc1d0PjQuMTY8L0dyb3NzV3Q+DQogICAgICAgICAgICA8TmV0V3Q+NC4xNjwvTmV0V3Q+DQogICAgICAgICAgICA8V3JhcFR5cGUgLz4NCiAgICAgICAgICAgIDxOb3RlUyAvPg0KICAgICAgICAgICAgPERlY2xQb3J0PjEwNDwvRGVjbFBvcnQ+DQogICAgICAgICAgICA8Q29Pd25lciAvPg0KICAgICAgICAgICAgPFJlbGF0aXZlSWQgLz4NCiAgICAgICAgICAgIDxUeXBpc3RObyAvPg0KICAgICAgICAgICAgPElucHV0Tm8+ODgwMDAwMDAyNjk0MTwvSW5wdXRObz4NCiAgICAgICAgICAgIDxJbnB1dENvbXBhbnlDbz4xMTExOTQwMTEyPC9JbnB1dENvbXBhbnlDbz4NCiAgICAgICAgICAgIDxJbnB1dENvbXBhbnlOYW1lPkhTReWNjuWco+WbvemZhei/kOi+k+acjeWKoeaciemZkOWFrOWPuDwvSW5wdXRDb21wYW55TmFtZT4NCiAgICAgICAgICAgIDxEZWNsYXJlTm8+ZGFpbWE8L0RlY2xhcmVObz4NCiAgICAgICAgICAgIDxDdXN0b21zRmllbGQgLz4NCiAgICAgICAgICAgIDxLaklkIC8+DQogICAgICAgICAgICA8U2VuZE5hbWU+RkJBIFBUWSBMVEQ8L1NlbmROYW1lPg0KICAgICAgICAgICAgPFJlY2VpdmVOYW1lPumCk+e+juS7qjwvUmVjZWl2ZU5hbWU+DQogICAgICAgICAgICA8U2VuZENvdW50cnk+NjAxPC9TZW5kQ291bnRyeT4NCiAgICAgICAgICAgIDxTZW5kQ2l0eSAvPg0KICAgICAgICAgICAgPFNlbmRJZCAvPg0KICAgICAgICAgICAgPFRvdGFsVmFsdWUgLz4NCiAgICAgICAgICAgIDxDdXJyQ29kZSAvPg0KICAgICAgICAgICAgPE1haW5HTmFtZSAvPg0KICAgICAgICAgICAgPEVudHJ5VHlwZT5CPC9FbnRyeVR5cGU+DQogICAgICAgICAgICA8U2VuZElkVHlwZSAvPg0KICAgICAgICAgIDwvRW50cnlIZWFkPg0KICAgICAgICAgIDxFbnRyeUxpc3Q+DQogICAgICAgICAgICA8T3BUeXBlPkFERDwvT3BUeXBlPg0KICAgICAgICAgICAgPEdObz4xPC9HTm8+DQogICAgICAgICAgICA8Q29kZVRTPjAxMDEwNzAwPC9Db2RlVFM+DQogICAgICAgICAgICA8R05hbWU+6JOd6IOW5a2QTUFYSUdFTkVT6ISx6ISC5oiQ5Lq65aW257KJ572Q6KOFPC9HTmFtZT4NCiAgICAgICAgICAgIDxHTW9kZWw+6ISx6ISC5oiQ5Lq65aW257KJ572Q6KOFfOiTneiDluWtkE1BWElHRU5FU3wxMDAwRzwvR01vZGVsPg0KICAgICAgICAgICAgPE9yaWdpbkNvdW50cnkgLz4NCiAgICAgICAgICAgIDxUcmFkZUN1cnIgLz4NCiAgICAgICAgICAgIDxUcmFkZVRvdGFsIC8+DQogICAgICAgICAgICA8RGVjbFByaWNlIC8+DQogICAgICAgICAgICA8RGVjbFRvdGFsIC8+DQogICAgICAgICAgICA8VXNlVG8gLz4NCiAgICAgICAgICAgIDxEdXR5TW9kZSAvPg0KICAgICAgICAgICAgPEdRdHk+MTwvR1F0eT4NCiAgICAgICAgICAgIDxHVW5pdCAvPg0KICAgICAgICAgICAgPFF0eTEgLz4NCiAgICAgICAgICAgIDxVbml0MSAvPg0KICAgICAgICAgICAgPFF0eTIgLz4NCiAgICAgICAgICAgIDxVbml0MiAvPg0KICAgICAgICAgICAgPEdHcm9zc1d0PjEuNDwvR0dyb3NzV3Q+DQogICAgICAgICAgPC9FbnRyeUxpc3Q+DQogICAgICAgICAgPEVudHJ5TGlzdD4NCiAgICAgICAgICAgIDxPcFR5cGU+QUREPC9PcFR5cGU+DQogICAgICAgICAgICA8R05vPjI8L0dObz4NCiAgICAgICAgICAgIDxDb2RlVFM+MDEwMTA3MDA8L0NvZGVUUz4NCiAgICAgICAgICAgIDxHTmFtZT7ok53og5blrZBNQVhJR0VORVPlhajohILmiJDkurrlpbbnsonnvZDoo4U8L0dOYW1lPg0KICAgICAgICAgICAgPEdNb2RlbD7lhajohILmiJDkurrlpbbnsonnvZDoo4V86JOd6IOW5a2QTUFYSUdFTkVTfDEwMDBHPC9HTW9kZWw+DQogICAgICAgICAgICA8T3JpZ2luQ291bnRyeSAvPg0KICAgICAgICAgICAgPFRyYWRlQ3VyciAvPg0KICAgICAgICAgICAgPFRyYWRlVG90YWwgLz4NCiAgICAgICAgICAgIDxEZWNsUHJpY2UgLz4NCiAgICAgICAgICAgIDxEZWNsVG90YWwgLz4NCiAgICAgICAgICAgIDxVc2VUbyAvPg0KICAgICAgICAgICAgPER1dHlNb2RlIC8+DQogICAgICAgICAgICA8R1F0eT4yPC9HUXR5Pg0KICAgICAgICAgICAgPEdVbml0IC8+DQogICAgICAgICAgICA8UXR5MSAvPg0KICAgICAgICAgICAgPFVuaXQxIC8+DQogICAgICAgICAgICA8UXR5MiAvPg0KICAgICAgICAgICAgPFVuaXQyIC8+DQogICAgICAgICAgICA8R0dyb3NzV3Q+Mi43NjwvR0dyb3NzV3Q+DQogICAgICAgICAgPC9FbnRyeUxpc3Q+DQogICAgICAgICAgPEVudHJ5TGlzdD4NCiAgICAgICAgICAgIDxPcFR5cGU+QUREPC9PcFR5cGU+DQogICAgICAgICAgICA8R05vPjE8L0dObz4NCiAgICAgICAgICAgIDxDb2RlVFM+MDEwMTA3MDA8L0NvZGVUUz4NCiAgICAgICAgICAgIDxHTmFtZT7ok53og5blrZBNQVhJR0VORVPohLHohILmiJDkurrlpbbnsonnvZDoo4U8L0dOYW1lPg0KICAgICAgICAgICAgPEdNb2RlbD7ohLHohILmiJDkurrlpbbnsonnvZDoo4V86JOd6IOW5a2QTUFYSUdFTkVTfDEwMDBHPC9HTW9kZWw+DQogICAgICAgICAgICA8T3JpZ2luQ291bnRyeSAvPg0KICAgICAgICAgICAgPFRyYWRlQ3VyciAvPg0KICAgICAgICAgICAgPFRyYWRlVG90YWwgLz4NCiAgICAgICAgICAgIDxEZWNsUHJpY2UgLz4NCiAgICAgICAgICAgIDxEZWNsVG90YWwgLz4NCiAgICAgICAgICAgIDxVc2VUbyAvPg0KICAgICAgICAgICAgPER1dHlNb2RlIC8+DQogICAgICAgICAgICA8R1F0eT4xPC9HUXR5Pg0KICAgICAgICAgICAgPEdVbml0IC8+DQogICAgICAgICAgICA8UXR5MSAvPg0KICAgICAgICAgICAgPFVuaXQxIC8+DQogICAgICAgICAgICA8UXR5MiAvPg0KICAgICAgICAgICAgPFVuaXQyIC8+DQogICAgICAgICAgICA8R0dyb3NzV3Q+MS40PC9HR3Jvc3NXdD4NCiAgICAgICAgICA8L0VudHJ5TGlzdD4NCiAgICAgICAgICA8RW50cnlMaXN0Pg0KICAgICAgICAgICAgPE9wVHlwZT5BREQ8L09wVHlwZT4NCiAgICAgICAgICAgIDxHTm8+MjwvR05vPg0KICAgICAgICAgICAgPENvZGVUUz4wMTAxMDcwMDwvQ29kZVRTPg0KICAgICAgICAgICAgPEdOYW1lPuiTneiDluWtkE1BWElHRU5FU+WFqOiEguaIkOS6uuWltueyiee9kOijhTwvR05hbWU+DQogICAgICAgICAgICA8R01vZGVsPuWFqOiEguaIkOS6uuWltueyiee9kOijhXzok53og5blrZBNQVhJR0VORVN8MTAwMEc8L0dNb2RlbD4NCiAgICAgICAgICAgIDxPcmlnaW5Db3VudHJ5IC8+DQogICAgICAgICAgICA8VHJhZGVDdXJyIC8+DQogICAgICAgICAgICA8VHJhZGVUb3RhbCAvPg0KICAgICAgICAgICAgPERlY2xQcmljZSAvPg0KICAgICAgICAgICAgPERlY2xUb3RhbCAvPg0KICAgICAgICAgICAgPFVzZVRvIC8+DQogICAgICAgICAgICA8RHV0eU1vZGUgLz4NCiAgICAgICAgICAgIDxHUXR5PjI8L0dRdHk+DQogICAgICAgICAgICA8R1VuaXQgLz4NCiAgICAgICAgICAgIDxRdHkxIC8+DQogICAgICAgICAgICA8VW5pdDEgLz4NCiAgICAgICAgICAgIDxRdHkyIC8+DQogICAgICAgICAgICA8VW5pdDIgLz4NCiAgICAgICAgICAgIDxHR3Jvc3NXdD4yLjc2PC9HR3Jvc3NXdD4NCiAgICAgICAgICA8L0VudHJ5TGlzdD4NCiAgICAgICAgICA8RW50cnlMaXN0Pg0KICAgICAgICAgICAgPE9wVHlwZT5BREQ8L09wVHlwZT4NCiAgICAgICAgICAgIDxHTm8+MTwvR05vPg0KICAgICAgICAgICAgPENvZGVUUz4wMTAxMDcwMDwvQ29kZVRTPg0KICAgICAgICAgICAgPEdOYW1lPuiTneiDluWtkE1BWElHRU5FU+iEseiEguaIkOS6uuWltueyiee9kOijhTwvR05hbWU+DQogICAgICAgICAgICA8R01vZGVsPuiEseiEguaIkOS6uuWltueyiee9kOijhXzok53og5blrZBNQVhJR0VORVN8MTAwMEc8L0dNb2RlbD4NCiAgICAgICAgICAgIDxPcmlnaW5Db3VudHJ5IC8+DQogICAgICAgICAgICA8VHJhZGVDdXJyIC8+DQogICAgICAgICAgICA8VHJhZGVUb3RhbCAvPg0KICAgICAgICAgICAgPERlY2xQcmljZSAvPg0KICAgICAgICAgICAgPERlY2xUb3RhbCAvPg0KICAgICAgICAgICAgPFVzZVRvIC8+DQogICAgICAgICAgICA8RHV0eU1vZGUgLz4NCiAgICAgICAgICAgIDxHUXR5PjE8L0dRdHk+DQogICAgICAgICAgICA8R1VuaXQgLz4NCiAgICAgICAgICAgIDxRdHkxIC8+DQogICAgICAgICAgICA8VW5pdDEgLz4NCiAgICAgICAgICAgIDxRdHkyIC8+DQogICAgICAgICAgICA8VW5pdDIgLz4NCiAgICAgICAgICAgIDxHR3Jvc3NXdD4xLjQ8L0dHcm9zc1d0Pg0KICAgICAgICAgIDwvRW50cnlMaXN0Pg0KICAgICAgICAgIDxFbnRyeUxpc3Q+DQogICAgICAgICAgICA8T3BUeXBlPkFERDwvT3BUeXBlPg0KICAgICAgICAgICAgPEdObz4yPC9HTm8+DQogICAgICAgICAgICA8Q29kZVRTPjAxMDEwNzAwPC9Db2RlVFM+DQogICAgICAgICAgICA8R05hbWU+6JOd6IOW5a2QTUFYSUdFTkVT5YWo6ISC5oiQ5Lq65aW257KJ572Q6KOFPC9HTmFtZT4NCiAgICAgICAgICAgIDxHTW9kZWw+5YWo6ISC5oiQ5Lq65aW257KJ572Q6KOFfOiTneiDluWtkE1BWElHRU5FU3wxMDAwRzwvR01vZGVsPg0KICAgICAgICAgICAgPE9yaWdpbkNvdW50cnkgLz4NCiAgICAgICAgICAgIDxUcmFkZUN1cnIgLz4NCiAgICAgICAgICAgIDxUcmFkZVRvdGFsIC8+DQogICAgICAgICAgICA8RGVjbFByaWNlIC8+DQogICAgICAgICAgICA8RGVjbFRvdGFsIC8+DQogICAgICAgICAgICA8VXNlVG8gLz4NCiAgICAgICAgICAgIDxEdXR5TW9kZSAvPg0KICAgICAgICAgICAgPEdRdHk+MjwvR1F0eT4NCiAgICAgICAgICAgIDxHVW5pdCAvPg0KICAgICAgICAgICAgPFF0eTEgLz4NCiAgICAgICAgICAgIDxVbml0MSAvPg0KICAgICAgICAgICAgPFF0eTIgLz4NCiAgICAgICAgICAgIDxVbml0MiAvPg0KICAgICAgICAgICAgPEdHcm9zc1d0PjIuNzY8L0dHcm9zc1d0Pg0KICAgICAgICAgIDwvRW50cnlMaXN0Pg0KICAgICAgICAgIDxFbnRyeURvY3U+DQogICAgICAgICAgICA8T3BUeXBlPkFERDwvT3BUeXBlPg0KICAgICAgICAgICAgPE9yZGVyTm8+MTwvT3JkZXJObz4NCiAgICAgICAgICAgIDxEb2N1Q29kZSAvPg0KICAgICAgICAgICAgPENlcnRDb2RlIC8+DQogICAgICAgICAgPC9FbnRyeURvY3U+DQogICAgICAgIDwvRVhQMzAxPg0KICAgICAgPC9EYXRhPg0KICAgIDwvU2lnbmVkRGF0YT4NCiAgPC9EYXRhSW5mbz4NCjwvUGFja2FnZT4=";
        //String x="PD94bWwgdmVyc2lvbj0iMS4wIiBlbmNvZGluZz0iVVRGLTgiPz48UGFja2FnZT4NCjxFbnZlbG9wSW5mbz4NCjx2ZXJzaW9uPjEuMDwvdmVyc2lvbj4NCjxtZXNzYWdlX2lkPjAwMDAwMDAwMDAxMDFFMDEwMDAwMjAxMjEwMDExMjMwMzAwMDAwMDAwMDAwMDwvbWVzc2FnZV9pZD4NCjxmaWxlX25hbWU+MDAwMDAwMDAwMDEwMUUwMTAwMDAyMDEyMTAwMTEyMzAzMDAwMDAwMDAwMDAwLkVYUDwvZmlsZV9uYW1lPg0KPG1lc3NhZ2VfdHlwZT5FWFAzMTE8L21lc3NhZ2VfdHlwZT4NCjxzZW5kZXJfaWQ+MDAwMDAwMDAwMDAwMDAyMDA2PC9zZW5kZXJfaWQ+DQo8cmVjZWl2ZXJfaWQ+RTAxMDAwMDwvcmVjZWl2ZXJfaWQ+DQo8c2VuZF90aW1lPjIwMTktMDktMjJUMTE6MTc6MTM8L3NlbmRfdGltZT4NCjwvRW52ZWxvcEluZm8+DQo8RGF0YUluZm8+DQo8U2lnbmVkRGF0YT4NCjxEYXRhPg0KPEVYUDMxMSB4bWxucz0iaHR0cDovL3d3dy5jaGluYXBvcnQuZ292LmNuL0V4cCI+DQo8RXhwTWZ0SGVhZD4NCjxPcFR5cGU+QUREPC9PcFR5cGU+DQo8QmlsbE5vLz4NCjxWb3lhZ2VOby8+DQo8SUVGbGFnPkU8L0lFRmxhZz4NCjxUcmFmQ25OYW1lPjQ3MDA8L1RyYWZDbk5hbWU+DQo8VHJhZkVuTmFtZT4yMDEzMDIyMjAwMDAwMDwvVHJhZkVuTmFtZT4NCjxHcm9zc1d0PjExMTExMTExPC9Hcm9zc1d0Pg0KPFBhY2tObz4wMDAwPC9QYWNrTm8+DQo8QmlsbE51bT5CaWxsTnVtPC9CaWxsTnVtPg0KPFRyYWZNb2RlPjA8L1RyYWZNb2RlPg0KPElFRGF0ZT4yMDE3MDEyMjwvSUVEYXRlPg0KPERlc3RpbmF0aW9uUG9ydD4wMTAwMDI8L0Rlc3RpbmF0aW9uUG9ydD4NCjxJRVBvcnQ+MDEwMDwvSUVQb3J0Pg0KPFRyYWRlQ28+MTEwNTkxMDE1OTwvVHJhZGVDbz4NCjxUcmFkZU5hbWU+PC9UcmFkZU5hbWU+DQo8SW5wdXRObz44NjAwMDAwMDA5NzUzPC9JbnB1dE5vPg0KPElucHV0T3BOYW1lPjwvSW5wdXRPcE5hbWU+DQo8SW5wdXRDb21wYW55Q29kZT4xMTA1OTEwMTU5MTIzNDU2Nzg8L0lucHV0Q29tcGFueUNvZGU+DQo8SW5wdXRDb21wYW55TmFtZT48L0lucHV0Q29tcGFueU5hbWU+DQo8L0V4cE1mdEhlYWQ+DQo8RXhwTWZ0TGlzdD4NCjxCaWxsTm8+QUREPC9CaWxsTm8+DQo8QXNzQmlsbE5vPjE8L0Fzc0JpbGxObz4NCjxWb3lhZ2VOby8+DQo8TWFpbkdOYW1lPjwvTWFpbkdOYW1lPg0KPFBhY2tObz45PC9QYWNrTm8+DQo8R3Jvc3NXdD4xMDE8L0dyb3NzV3Q+DQo8VHJhZGVUb3RhbD4xMTA8L1RyYWRlVG90YWw+DQo8VHJhZGVDdXJyLz4NCjwvRXhwTWZ0TGlzdD4NCjwvRVhQMzExPg0KPC9EYXRhPg0KPC9TaWduZWREYXRhPg0KPC9EYXRhSW5mbz4NCjwvUGFja2FnZT4NCg==";
        String x = "QXLA2iG4fmEXzctxhB2PByPOn3MGN52WRNXeE6jlpE+RnkdJrUTdcztJ4ee7fizotVbgrJzywo0=";
        byte[] s = Base64.getDecoder().decode(x);
        String xml = new String(s);
        log.info(xml);
        //parserCangdanHzXML(xml);
    }


}
