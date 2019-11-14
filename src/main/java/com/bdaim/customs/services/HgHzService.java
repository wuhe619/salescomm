package com.bdaim.customs.services;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.be.service.BusiEntityService;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.BusiService;
import com.bdaim.util.ParseHzXml;
import com.bdaim.util.StringUtil;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/***
 * 海关回执
 */
@Service("busi_hg_hz")
@Transactional
public class HgHzService implements BusiService {

    private static Logger log = LoggerFactory.getLogger(HgHzService.class);

    private static final Map<String, String> HZ_SERVICE = new HashMap() {{
        put("EXP302", "bgd_hz");
        put("EXP312", "cd_hz");
    }};

    @Autowired
    private ParseHzXml parseHzXml;
    @Autowired
    BusiEntityService busiService;


    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        log.info("海关回执请求参数:{}", info);
        info.put("ext_1", info.getString("status"));
        String xmlString = info.getString("xmlstring");
        if (StringUtil.isEmpty(xmlString)) {
            log.warn("海关回执内容不能为空:{}", xmlString);
            return;
        }
        String xml = new String(Base64.decodeBase64(xmlString), "UTF-8");
        log.info("海关回执xml:{}", xml);
        String messageType = parseHzXml.getMessageTypeByXml(xml);
        if (StringUtil.isEmpty(HZ_SERVICE.get(messageType))) {
            log.warn("海关回执messageType未找到对应定义,messageType:{},内容:{}", messageType, xml);
            return;
        } else {
            info.put("hz_type", HZ_SERVICE.get(messageType));
            // 根据消息类型处理报关单和舱单回执
            busiService.saveInfo(cust_id, cust_group_id, cust_user_id, HZ_SERVICE.get(messageType), 0L, info);
            log.info("海关回执处理完毕,messageType:{},回执类型:{}", messageType, HZ_SERVICE.get(messageType));
        }
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

    public static void main(String[] args) throws IOException {
        System.out.println(Base64.encodeBase64String("<?xml version=\"1.0\" encoding=\"UTF-8\"?><Package><EnvelopInfo><version>1.0</version><message_id>E010000DXPESW00002132802019110100284107303304013</message_id><file_name>E010000DXPESW00002132802019110100284107303304013.EXP</file_name><message_type>EXP302</message_type><sender_id>E010000</sender_id><receiver_id>DXPESW0000213280</receiver_id><send_time>2019-11-01T00:28:39</send_time></EnvelopInfo><DataInfo><SignedData><Data><EXP302><EntryHead><PreEntryId>201900001507083001</PreEntryId><OpType>ADD</OpType><BillNo>897765azc11117</BillNo><AssBillNo>G00593691690</AssBillNo><IEFlag>I</IEFlag><EntryId></EntryId><OpTime>20191101002839995</OpTime><OpResult>D0</OpResult><Notes></Notes></EntryHead></EXP302></Data><HashSign></HashSign><SignerInfo></SignerInfo></SignedData></DataInfo></Package>".getBytes()));
        byte[] bytes = readFromByteFile("C:\\Users\\Administrator\\Documents\\WeChat Files\\CN1005266424\\FileStorage\\File\\2019-11\\Receipt_E010000DXPESW00002118002019111409185807415349267_20191114091937786298918.xml");

        System.out.println(new String(bytes));
    }

    public static byte[] readFromByteFile(String pathname) throws IOException {
        File filename = new File(pathname);
        BufferedInputStream in = new BufferedInputStream(new FileInputStream(filename));
        ByteArrayOutputStream out = new ByteArrayOutputStream(1024);
        byte[] temp = new byte[1024];
        int size = 0;
        while ((size = in.read(temp)) != -1) {
            out.write(temp, 0, size);
        }
        in.close();
        byte[] content = out.toByteArray();
        return content;
    }
}
