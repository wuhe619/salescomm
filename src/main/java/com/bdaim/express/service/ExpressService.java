package com.bdaim.express.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.service.BusiService;
import com.bdaim.common.service.SequenceService;
import com.bdaim.common.spring.SpringContextHelper;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.express.dto.ExpressOrderData;
import com.bdaim.express.dto.ExpressType;
import com.bdaim.express.dto.Items;
import com.bdaim.express.dto.Param;
import com.bdaim.express.dto.yto.*;
import com.bdaim.express.dto.zto.ZTO;
import com.bdaim.express.dto.zto.ZTOReceiver;
import com.bdaim.express.dto.zto.ZTORequest;
import com.bdaim.express.dto.zto.ZTOSender;
import com.bdaim.util.StringUtil;
import com.bdaim.util.http.HttpUtil;
import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.xml.DomDriver;
import org.apache.commons.codec.binary.Base64;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.security.MessageDigest;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ExpressService {
    @Autowired
    private SequenceService sequenceService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    private static Logger logger = LoggerFactory.getLogger(ExpressService.class);
    SimpleDateFormat smft = new SimpleDateFormat("YYYY-M-d H:m:ss");

    //根据渠道区分快递公司
    public String expressOeder(ExpressOrderData orderData, LoginUser lu) {

        switch (orderData.getTradeNoType()) {
            case 0://圆通
                return YtoOrder(orderData, lu);
            case 1://中通
                return ZTOorder(orderData);
            case 2:
                return "";
        }


        return "";
    }

    //圆通快递订单创建
    public String YtoOrder(ExpressOrderData orderData, LoginUser lu) {
        if (StringUtil.isEmpty(orderData.getSender().getMobile()) && StringUtil.isEmpty(orderData.getSender().getPhone())) {
            throw new RuntimeException("用户移动电话，手机和电话至少填一项");
        }
        if (StringUtil.isNotEmpty(orderData.getSender().getPhone())) {
            int i = orderData.getSender().getPhone().indexOf("-");
            if (i < 0) {
                throw new ParamException("发件人电话号格式错误");
            }
        }
        if (StringUtil.isNotEmpty(orderData.getReceiver().getPhone())) {
            int i = orderData.getReceiver().getPhone().indexOf("-");
            if (i < 0) {
                throw new ParamException("收件人电话号格式错误");
            }
        }

        ElectronOrderResponse ytoResponse = null;
        String custId = lu.getCustId();
//        String custId = "1909180815300000";
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("当前用户不允许创建订单");
        }
        String sql1 = "select property_value from t_customer_property  where cust_id=? and property_name= ?";
        Map data;
        data = jdbcTemplate.queryForMap(sql1, custId, "yto_config");
        if (data.size() == 0) {
            throw new ParamException("当前用户不允许创建订单");
        }
        JSONObject json = JSONObject.parseObject(data.get("property_value").toString());
        String orderUrl = json.getString("orderUrl");
        YTO requestOrder = new YTO(json.getString("customer_id"), orderData.getTxLogisticID(), orderData.getOrderType(), orderData.getServiceType());
        requestOrder.setSender(new YTOSender(orderData.getSender()));
        requestOrder.setReceiver(new YTOReceiver(orderData.getReceiver()));
        String clientId = requestOrder.getClientID();
        int clientIDNum = clientId.length();
        String randomStr = getRandomStringByLength(63 - clientIDNum);
        requestOrder.setTxLogisticID(requestOrder.getClientID() + randomStr);

        String txLogisticID = requestOrder.getTxLogisticID();
        String sql = "select id,type,content,ext_1,ext_2 from h_data_manager_express_order where ext_2 =" + "'" + txLogisticID + "'";
        List<Map<String, Object>> queryList = jdbcTemplate.queryForList(sql, new ArrayList<>().toArray());
        if (queryList.size() > 0) {
            throw new ParamException("订单号重复");
        }
        List<Items> itemList = orderData.getItemList();

        String result;
        Map<String, Object> headers = new HashMap<>();
        headers.put("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        StringBuilder xmlBuilder = new StringBuilder();
        xmlBuilder.append("<RequestOrder>");
        xmlBuilder.append("    <clientID>" + clientId + "</clientID>");
        xmlBuilder.append("    <logisticProviderID>" + requestOrder.getLogisticProviderID() + "</logisticProviderID>");
        xmlBuilder.append("    <customerId>" + clientId + "</customerId>");
        xmlBuilder.append("    <txLogisticID>" + requestOrder.getTxLogisticID() + "</txLogisticID>");
        xmlBuilder.append("    <tradeNo>" + requestOrder.getTradeNo() + "</tradeNo>");
        xmlBuilder.append("    <totalServiceFee>0.0</totalServiceFee>");
        xmlBuilder.append("    <codSplitFee>0.0</codSplitFee>");
        xmlBuilder.append("    <orderType>" + requestOrder.getOrderType() + "</orderType>");
        xmlBuilder.append("    <serviceType>" + requestOrder.getServiceType() + "</serviceType>");
        xmlBuilder.append("    <flag>" + requestOrder.getFlag() + "</flag>");
        xmlBuilder.append("    <sendStartTime>2014-03-06 12:12:12</sendStartTime>");
        xmlBuilder.append("    <sendEndTime>2014-03-06 12:12:12</sendEndTime>");
        xmlBuilder.append("    <goodsValue>1</goodsValue>");
        xmlBuilder.append("    <itemsValue>1</itemsValue>");
        xmlBuilder.append("    <insuranceValue>0.0</insuranceValue>");
        xmlBuilder.append("    <special>1</special>");
        xmlBuilder.append("    <remark>1</remark>");
        xmlBuilder.append("    <deliverNo>1</deliverNo>");
        xmlBuilder.append("    <type>1</type>");
        xmlBuilder.append("    <totalValue>1</totalValue>");
        xmlBuilder.append("    <itemsWeight>1</itemsWeight>");
        xmlBuilder.append("    <packageOrNot>1</packageOrNot>");
        xmlBuilder.append("    <orderSource>1</orderSource>");
        xmlBuilder.append("    <sender>");
        xmlBuilder.append("        <name>" + requestOrder.getSender().getName() + "</name>");
        xmlBuilder.append("        <postCode>" + requestOrder.getSender().getPostCode() + "</postCode>");
        xmlBuilder.append("        <phone>" + requestOrder.getSender().getPhone() + "</phone>");
        xmlBuilder.append("        <mobile>" + requestOrder.getSender().getMobile() + "</mobile>");
        xmlBuilder.append("        <prov>" + requestOrder.getSender().getProv() + "</prov>");
        xmlBuilder.append("        <city>" + requestOrder.getSender().getCity() + "</city>");
        xmlBuilder.append("        <address>" + requestOrder.getSender().getAddress() + "</address>");
        xmlBuilder.append("    </sender>");
        xmlBuilder.append("    <receiver>");
        xmlBuilder.append("        <name>" + requestOrder.getReceiver().getName() + "</name>");
        xmlBuilder.append("        <postCode>" + requestOrder.getReceiver().getPostCode() + "</postCode>");
        xmlBuilder.append("        <phone>" + requestOrder.getReceiver().getPhone() + "</phone>");
        xmlBuilder.append("        <mobile>" + requestOrder.getReceiver().getMobile() + "</mobile>");
        xmlBuilder.append("        <prov>" + requestOrder.getReceiver().getProv() + "</prov>");
        xmlBuilder.append("        <city>" + requestOrder.getReceiver().getCity() + "</city>");
        xmlBuilder.append("        <address>" + requestOrder.getReceiver().getAddress() + "</address>");
        xmlBuilder.append("    </receiver>");
        xmlBuilder.append("    <items>");
        for (Items item : itemList) {
            xmlBuilder.append("        <item>");
            xmlBuilder.append("            <itemName>" + item.getItemName() + "</itemName>");
            xmlBuilder.append("            <number>" + item.getNumber() + "</number>");
            xmlBuilder.append("            <itemValue>" + item.getItemValue() + "</itemValue>");
            xmlBuilder.append("        </item>");
        }
        xmlBuilder.append("    </items>");
        xmlBuilder.append("</RequestOrder>");
        try {
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            messagedigest.update((xmlBuilder.toString() + "u2Z1F7Fh").getBytes("UTF-8"));
            byte[] abyte0 = messagedigest.digest();
            String data_digest = new String(Base64.encodeBase64(abyte0));
            String parameter = "logistics_interface=" + URLEncoder.encode(xmlBuilder.toString(), "UTF-8")
                    + "&data_digest=" + URLEncoder.encode(data_digest, "UTF-8")
                    + "&clientId=" + URLEncoder.encode(clientId, "UTF-8");
            result = HttpUtil.httpPost(orderUrl, parameter, headers);
            logger.info(result);
            XStream xStream = new XStream(new DomDriver());
            xStream.alias("Response", ElectronOrderResponse.class);
            xStream.alias("distributeInfo", DistributeInfo.class);
            ytoResponse = (ElectronOrderResponse) xStream.fromXML(result);
            ytoResponse.setSender(requestOrder.getSender());
            ytoResponse.setReceiver(requestOrder.getReceiver());

            ytoResponse.setItemsList(itemList);
            Map<String, String> map = new HashMap<>();
            saveExpreeOder(custId, lu.getUserGroupId(), Long.valueOf(lu.getUser_id()), "express_order",
                    0L, JSONObject.parseObject(JSONObject.toJSON(ytoResponse).toString()),   ExpressType.getExpressTypeByCode("yto"));
            return ytoResponse.getMailNo();

        } catch (Exception e) {
            logger.info("报错信息:" + e);
        }
        return null;
    }


    //获取面单信息（打印面单）
    public String queryExpressOrderData(ExpressOrderData orderData) {

        //订单列表查询,根据订单号、面单号、渠道获取唯一一条
        String str = null;
        ElectronOrderResponse electronOrderResponse = JSON.parseObject(str, ElectronOrderResponse.class);

        return null;
    }


    /**
     * 获取一定长度的随机字符串
     *
     * @param length 指定字符串长度
     * @return 一定长度的字符串
     */
    public static String getRandomStringByLength(int length) {
        String baseStr = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz-0123456789";
        Random random = new Random();
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int number = random.nextInt(baseStr.length());
            sb.append(baseStr.charAt(number));
        }

        return sb.toString() + random.nextInt(9);
    }

    public Object queryTrajectory(String number, int tradeNoType, String custId) {
        switch (tradeNoType) {
            case 0:
                return queryYTOTrajectory(number, custId);
            case 1:
                return "";

        }
        return null;

    }

    //圆通快递轨迹
    public String queryYTOTrajectory(String number, String custId) {
        String sql1 = "select property_value from t_customer_property  where cust_id=? and property_name= ?";
        Map data;

        data = jdbcTemplate.queryForMap(sql1, custId, "yto_config");
        if (data.size() == 0) {
            throw new ParamException("查询失败");
        }

        JSONObject json = JSONObject.parseObject(data.get("property_value").toString());
        Param param = new Param();
        param.setNumber(number);
        param.setWaybillNos(number);

        String app_key = json.getString("app_key");
        String user_id = json.getString("user_id");
        String format = "JSON";
        String method = json.getString("method_name");
        String secret_key = json.getString("secret_key");
        String rajectoryUrl = json.getString("rajectoryUrl");
        String dateTime = smft.format(new Date());
        Map<String, Object> headers = new HashMap<>();
        headers.put("content-type", "application/x-www-form-urlencoded");
        String result;
        try {
            String sign = encryptSignForOpen(app_key, format, method, dateTime, user_id, "1.01",
                    null, secret_key);
            Map<String, String> map = new HashMap<>();
            map.put("sign", sign);
            map.put("app_key", app_key);
            map.put("format", "JSON");
            map.put("method", method);
            map.put("timestamp", dateTime);
            map.put("user_id", user_id);
            map.put("v", "1.01");
            map.put("param", JSON.toJSONString(param));
            String date = paramsToQueryStringUrlencoded(map);
            result = HttpUtil.httpPost(rajectoryUrl, date, headers);
            System.err.println(result);
            return result;
        } catch (Exception e) {
            logger.info("报错信息:" + e.getCause());
        }
        return null;
    }

    public static String paramsToQueryStringUrlencoded(Map<String, String> params) {
        return params.entrySet().stream().map(e -> {
            try {
                return e.getKey() + "=" + URLEncoder.encode(e.getValue(), "UTF-8");
            } catch (UnsupportedEncodingException e1) {
                return e.getValue();
            }
        }).collect(Collectors.joining("&"));
    }

    public static String encryptSignForOpen(String appKey, String format, String method, String timestamp,
                                            String userId, String v, String version, String secret) {
        String sign = "";
        try {
            StringBuffer stringBuffer = new StringBuffer(secret);
            stringBuffer = stringBuffer.append("app_key").append(appKey).append("format").append(format)
                    .append("method").append(method).append("timestamp").append(timestamp).append("user_id")
                    .append(userId);
            if (StringUtil.isEmpty(version)) {
                stringBuffer.append("v").append(v);
            } else {
                stringBuffer.append("version").append(version);
            }
            MessageDigest messagedigest = MessageDigest.getInstance("MD5");
            messagedigest.update((stringBuffer.toString()).getBytes("UTF-8"));
            byte[] signByte = messagedigest.digest();

            int i;
            StringBuffer buf = new StringBuffer("");
            for (int offset = 0; offset < signByte.length; offset++) {
                i = signByte[offset];
                if (i < 0)
                    i += 256;
                if (i < 16)
                    buf.append("0");
                buf.append(Integer.toHexString(i));
            }
            sign = buf.toString().toUpperCase();
        } catch (Throwable e) {
            sign = "ERROR";
        }
        return sign;
    }

    //中通快递订单创建
    public String ZTOorder(ExpressOrderData orderData) {
        String companyId = "your_company_id";
        String key = "your_key";
        Map<String, String> parameters = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();

        parameters.put("company_id", companyId);
        parameters.put("msg_type", "NEW_TRACES");
        headers.put("Content-Type", "application/x-www-form-urlencoded");
        ZTO zto = new ZTO();
        ZTORequest ztoRequest = new ZTORequest();
        zto.setSender(new ZTOSender(orderData.getSender()));
        zto.setReceiver(new ZTOReceiver(orderData.getReceiver()));
        String dateTime = smft.format(new Date());
        ztoRequest.setContent(zto);
        ztoRequest.setDatetime(dateTime);
        ztoRequest.setPartner("test");
        ztoRequest.setVerify("ZTO123");
        parameters.put("data", JSON.toJSONString(ztoRequest));
        String strToDigest = paramsToQueryStringUrlencoded(parameters) + key;
        String result;
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            md.update(strToDigest.getBytes(Charset.forName("UTF-8")));
            byte[] abyte0 = md.digest();
            String data_digest = new String(Base64.encodeBase64(abyte0));
            headers.put("x-datadigest", data_digest);
            headers.put("x-companyid", companyId);
            result = HttpUtil.httpPost("http://58.40.16.120:9001/submitOrderCode", parameters, headers);
            System.err.println(result);
        } catch (Exception e) {
            logger.info("异常信息:" + e);
        }
        return null;
    }

    //存储订单轨迹
    public void saveExpressTrajectory(String cust_id, String cust_group_id, Long cust_user_id, String busiType, Long id, JSONObject params, String mailNo, String txLogisticID, String expressType) throws Exception {
            try {
                BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_" + busiType);
                busiService.insertInfo(busiType, cust_id, cust_group_id, cust_user_id, id, params);
                String sql1 = "insert into " + HMetaDataDef.getTable(busiType, "") + "(id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date, ext_1, ext_2, ext_3, ext_4, ext_5 ) value(?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?)";
               System.err.equals(id);
                jdbcTemplate.update(sql1, id, busiType, params.toJSONString(), cust_id, cust_group_id, cust_user_id, cust_user_id
                        , mailNo, txLogisticID, expressType, "0", "");
            } catch (Exception e) {
                logger.error("插入数据异常:[" + busiType + "]", e);
                throw new Exception("插入数据异常:[" + busiType + "]");
            }

    }

    //存储订单
    public void saveExpreeOder(String cust_id, String cust_group_id, Long cust_user_id, String busiType, Long id, JSONObject params, ExpressType expressType) throws Exception {
        if (id == 0 || id == null) {
            id = sequenceService.getSeq(busiType);
            try {
                BusiService busiService = (BusiService) SpringContextHelper.getBean("busi_" + busiType);
                busiService.insertInfo(busiType, cust_id, cust_group_id, cust_user_id, id, params);
                String sql1 = "insert into " + HMetaDataDef.getTable(busiType, "") + "(id, type, content, cust_id, cust_group_id, cust_user_id, create_id, create_date, ext_1, ext_2, ext_3, ext_4, ext_5 ) value(?, ?, ?, ?, ?, ?, ?, now(), ?, ?, ?, ?, ?)";
                jdbcTemplate.update(sql1, id, busiType, params.toJSONString(), cust_id, cust_group_id, cust_user_id, cust_user_id
                        , params.containsKey("mailNo") ? params.getString("mailNo") : ""
                        , params.containsKey("txLogisticID") ? params.getString("txLogisticID") : ""
                        , expressType.getExpressCode()//快递类型
                        , "0"
                        , expressType.getExpressName());
                saveExpressTrajectory(cust_id, cust_group_id, cust_user_id, "express_trajectory", id, JSONObject.parseObject("[]"),params.getString("mailNo") ,  params.getString("txLogisticID") , "yto");

            } catch (Exception e) {
                logger.error("插入数据异常:[" + busiType + "]", e);
                throw new Exception("插入数据异常:[" + busiType + "]");
            }
        }
    }

}

