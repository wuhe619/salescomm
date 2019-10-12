package com.bdaim.common.util;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.nio.charset.Charset;

/**
 * 生成xml文件demo
 */
public class DomXml {


//    public static void main(String[] args) {
//        test();
//    }

    public  static void test(){
        Long start = System.currentTimeMillis();
        createXml();
        System.out.println("运行时间："+ (System.currentTimeMillis() - start));
    }

    /**
     * 生成xml方法
     */
    public static void createXml(){
        try {
            // 创建解析器工厂
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder db = factory.newDocumentBuilder();
            Document document = db.newDocument();
            // 不显示standalone="no"
            document.setXmlStandalone(true);
            Element bookstore = document.createElement("bookstore");
            // 向bookstore根节点中添加子节点book
            Element book = document.createElement("book");

            Element name = document.createElement("name");
            // 不显示内容 name.setNodeValue("不好使");
            name.setTextContent("雷神");
            book.appendChild(name);
            // 为book节点添加属性
            book.setAttribute("id", "1");
            // 将book节点添加到bookstore根节点中
            bookstore.appendChild(book);
            // 将bookstore节点（已包含book）添加到dom树中
            document.appendChild(bookstore);

            // 创建TransformerFactory对象
            TransformerFactory tff = TransformerFactory.newInstance();
            // 创建 Transformer对象
            Transformer tf = tff.newTransformer();
            tf.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, "");
            // 输出内容是否使用换行
            tf.setOutputProperty(OutputKeys.INDENT, "yes");
            // 创建xml文件并写入内容
            //tf.transform(new DOMSource(document), new StreamResult(new File("E:\\book1.xml")));
            System.out.println("生成book1.xml成功");


            DOMSource domSource = new DOMSource(document);
            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // 使用Transformer的transform()方法将DOM树转换成XML
            tf.transform(domSource, new StreamResult(bos));
            String xmlString = bos.toString();
            System.out.println(xmlString);

        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("生成book1.xml失败");
        }
    }


       public static String  xmlString="<Package>" +
                "<EnvelopInfo>" +
                "<version>1.0</version>" +
                "<message_id>E0100000000000000000020062017030116002400000509609</message_id>" +
                "<file_name>E0100000000000000000020062017030116002400000509609.EXP</file_name>" +
                "<message_type>EXP312</message_type>" +
                "<sender_id>E010000</sender_id>" +
                "<receiver_id>000000000000002006</receiver_id>" +
                "<send_time>2017-03-01T15:27:51</send_time>" +
                "</EnvelopInfo>" +
                "<DataInfo>" +
                "<SignedData>" +
                "<Data>" +
                "<EXP312 xmlns=\"http://www.w3.org/2000/09/xmldsig#\">" +
                "<ExpMftHead>" +
                "<BillNo>BillNoBillNoBillNoBil20170301003</BillNo>" +
                "<VoyageNo>VoNo20170301002</VoyageNo>" +
                "<EntryDate>20170301152751</EntryDate>" +
                "<RtnFlag>DY</RtnFlag>" +
                "<Notes/>" +
                "</ExpMftHead>" +
                "</EXP312>" +
                "</Data>" +
                "<HashSign/>" +
                "<SignerInfo/>" +
                "</SignedData>" +
                "</DataInfo>" +
                "</Package>";


}