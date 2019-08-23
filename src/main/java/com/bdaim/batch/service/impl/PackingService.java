package com.bdaim.batch.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.dao.BatchDetailDao;
import com.bdaim.batch.express.ZopClient;
import com.bdaim.batch.express.ZopPublicRequest;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerPropertyDO;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.service.MarketResourceService;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/1/25
 * @description
 */
@Service("packingService")
@Transactional
public class PackingService {
    private static Logger logger = LoggerFactory.getLogger(PackingService.class);
    @Resource
    private BatchDetailDao batchDetailDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private SourceDao sourceDao;
    @Resource
    private MarketResourceService marketResourceService;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> getMacResoult(String batchId) {
        String querySql = "SELECT * FROM tmp_nl_batch_detail b  WHERE batch_id = 1542005544857";
        List<Map<String, Object>> list = batchDetailDao.sqlQuery(querySql);
        return list;
    }


    public List<Map<String, Object>> getImeiResoult() {
        String querySql = "SELECT * FROM tmp_nl_batch_detail b  WHERE batch_id = 1547293213042";
        List<Map<String, Object>> list = batchDetailDao.sqlQuery(querySql);
        return list;
    }

    /**
     * 获取企业发送快递单价
     *
     * @param custId
     * @return
     */
    public double getCustExpressPrice(String custId) {
        CustomerPropertyDO jdFailPrice = customerDao.getProperty(custId, "jd_fail_price");
        if (jdFailPrice != null && StringUtil.isNotEmpty(jdFailPrice.getPropertyValue())) {
            return Double.parseDouble(jdFailPrice.getPropertyValue());
        }
        return 0;
    }

    /**
     * 获取供应商发送快递单价
     *
     * @param custId
     * @return
     */
    public double getsupplierExpressPrice(String custId, String supplierId) {
        Map<String, Object> smsPrice = sourceDao.querySupplierPrice(custId, supplierId);
        if (smsPrice != null && smsPrice.get("rejectionPrice") != null) {
            DecimalFormat df = new DecimalFormat("0.00");
            String rejectionPrice = df.format(Double.parseDouble(String.valueOf(smsPrice.get("rejectionPrice"))));
            return Double.parseDouble(rejectionPrice);
        }
        return 0;
    }

    /**
     * 确认发件/批量发件
     *
     * @param map batchId、addressId、isBatch、senderId
     * @return
     * @auther Chacker
     * @date 2019/8/8 14:42
     */
    public void sendExpress(Map<String, Object> map) {
        try {
            //map中包括批次ID和地址ID，以及isBatch状态判断，
            int isBatch = Integer.parseInt(String.valueOf(map.get("isBatch")));
            String batchId = String.valueOf(map.get("batchId"));
            String addressId = String.valueOf(map.get("addressId"));
            //发件人ID
            String senderId = String.valueOf(map.get("senderId"));
            String updateBatchStatus = "UPDATE nl_batch SET status='5' WHERE id='" + batchId + "'";
            logger.info("执行更新状态的SQL :" + updateBatchStatus);
            if (isBatch == 1) {
                //批量发送、将批次状态status修改为【5】【待取件】
                jdbcTemplate.update(updateBatchStatus);
                toSendExpress(isBatch, batchId, addressId, senderId);
                //将(有效的)批次详情的状态label_seven 修改为 【3】【待取件】
                String updateDetail = "UPDATE nl_batch_detail SET label_seven='3' WHERE batch_id='" + batchId + "' AND status='1'";
                jdbcTemplate.update(updateDetail);
            } else if (isBatch == 0) {
                //如果该批次下(有效数据)已没有待申请发件的 快递信息，则把该批次更新为 【5】【待取件】
                String countSql = "SELECT COUNT(*) AS count FROM nl_batch_detail WHERE label_seven='2' AND status='1' AND batch_id='" + batchId + "'";
                Map<String, Object> result = jdbcTemplate.queryForMap(countSql);
                int count = Integer.parseInt(String.valueOf(result.get("count")));
                if (count == 0) {
                    jdbcTemplate.update(updateBatchStatus);
                }
                toSendExpress(isBatch, batchId, addressId, senderId);
                //单个发送 将批次详情的状态 label_seven 状态修改为 【3】【待取件】
                StringBuffer stringBuffer = new StringBuffer("UPDATE nl_batch_detail SET label_seven='3' WHERE batch_id='");
                stringBuffer.append(batchId).append("' AND id='").append(addressId).append("'");
                jdbcTemplate.update(stringBuffer.toString());
            }
        } catch (Exception e) {
            logger.info("发送快递出错，异常信息为" + e.getMessage());
        }
    }

    /**
     * 发送快递的接口
     *
     * @param isBatch   是否是批量发送 1、是 0、否
     * @param batchId   批次编号
     * @param addressId 地址ID
     * @param senderId  发件人ID
     * @return
     * @auther Chacker
     * @date 2019/8/9 15:02
     */
    private void toSendExpress(int isBatch, String batchId, String addressId, String senderId) {
        //查询出发件人信息，并转化为json串，存入 t_touch_express_log的 sender_message 中
        String senderSql = "SELECT id AS senderId,sender_name AS senderName,phone,province,city,district,address FROM t_sender_info WHERE id='"
                + senderId + "'";
        Map<String, Object> senderInfo = jdbcTemplate.queryForMap(senderSql);
        if (isBatch == 1) {
            //批量发送，根据批次ID batchId 找出地址ID、姓名、手机号  (batch_id、status为"1"有效、且label_seven为"2"待申请发件的)
            StringBuffer stringBuffer = new StringBuffer("SELECT id AS addressId,touch_id,label_four AS address,label_one AS name,label_two AS phone,label_five AS receiverId,label_eight AS pdfPath FROM nl_batch_detail WHERE batch_id='");
            stringBuffer.append(batchId).append("' AND status='1' AND label_seven='2'");
            List<Map<String, Object>> resultList = jdbcTemplate.queryForList(stringBuffer.toString());
            for (Map<String, Object> tempMap : resultList) {
                updateExpressInfo(tempMap, senderInfo);
                String addressIdNew =String.valueOf(tempMap.get("addressId"));
                sendExpressByZTO(batchId, addressIdNew, senderInfo, tempMap);
            }
        } else if (isBatch == 0) {
            //单个发送，根据地址ID 找到 地址ID、姓名、手机号
            StringBuffer stringBuffer = new StringBuffer("SELECT id AS addressId,touch_id,label_four AS address,label_one AS name,label_two AS phone,label_five AS receiverId,label_eight AS pdfPath FROM nl_batch_detail WHERE id='");
            stringBuffer.append(addressId).append("'");
            Map<String, Object> tempMap = jdbcTemplate.queryForMap(stringBuffer.toString());
            updateExpressInfo(tempMap, senderInfo);
            //调用发送快递的接口
            sendExpressByZTO(batchId, addressId, senderInfo, tempMap);
        }
    }

    /**
     * 调用中通快递的接口
     * 1. 创建快递订单【API文档中的订单服务 -> 预约寄件 -订单创建】
     *
     * @param batchId      批次编号
     * @param addressId    修复地址ID
     * @param senderInfo   发件人信息
     * @param receiverInfo 收件人信息
     */
    private void sendExpressByZTO(String batchId, String addressId, Map<String, Object> senderInfo, Map<String, Object> receiverInfo) {
        logger.info(" ===== 》》》开始调用中通快递接口");
        logger.info("批次编号" + batchId + "修复地址ID" + addressId + "发件人信息" + senderInfo + "收件人信息" + receiverInfo);
        String ztoConfigSQL = "SELECT resource_id,property_name,property_value,create_time FROM t_market_resource_property WHERE resource_id='29' AND property_name='zto_config'";
        Map<String,Object> ztoConfig = jdbcTemplate.queryForMap(ztoConfigSQL);
        String propertyValue = String.valueOf(ztoConfig.get("property_value"));
        Map<String,Object> zto = (Map<String, Object>)JSON.parse(propertyValue);
        ZopClient client = new ZopClient(String.valueOf(zto.get("company_id")), String.valueOf(zto.get("key")));
        ZopPublicRequest request = new ZopPublicRequest();
        request.setUrl(String.valueOf(zto.get("url")));
        request.addParam("company_id", String.valueOf(zto.get("company_id")));
        Map<String, Object> data = new HashMap<>(32);
        data.put("orderId", receiverInfo.get("touch_id"));
        data.put("shopKey", String.valueOf(zto.get("shopKey")));
        //订单类型 0代表普通订单 1代表代收货款
        data.put("orderType", "0");
        //收件人信息
        data.put("receiveMan", receiverInfo.get("name"));
        data.put("receivePhone", receiverInfo.get("phone"));
        //收件人的省市区地址 ，从 label_four中获取
        String address= String.valueOf(receiverInfo.get("address"));
        Map<String,Object> addressMap = (Map<String, Object>)JSON.parse(address);
        data.put("receiveProvince", StringUtil.isNotEmpty(String.valueOf(addressMap.get("prov")))?String.valueOf(addressMap.get("prov")):" ");
        data.put("receiveCity", StringUtil.isNotEmpty(String.valueOf(addressMap.get("city")))?String.valueOf(addressMap.get("city")):" ");
        data.put("receiveCounty", StringUtil.isNotEmpty(String.valueOf(addressMap.get("dist")))?String.valueOf(addressMap.get("dist")):" ");
        data.put("receiveAddress", StringUtil.isNotEmpty(String.valueOf(addressMap.get("address")))?String.valueOf(addressMap.get("address")):" ");
//        data.put("receiveProvince","北京");
//        data.put("receiveCity","北京市");
//        data.put("receiveCounty","朝阳区");
//        data.put("receiveAddress","广顺南大街16号");
        //发件人信息
        data.put("sendMan", senderInfo.get("senderName"));
        data.put("sendMobile", senderInfo.get("phone"));
        data.put("sendProvince", senderInfo.get("province"));
        data.put("sendCity", senderInfo.get("city"));
        data.put("sendCounty", senderInfo.get("district"));
        data.put("sendAddress", senderInfo.get("address"));
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        data.put("orderDate", format.format(new Date()));
        //备注 收件人id  放到收件人备注(对应买家备注)，  批次id放到发件人备注(对应卖家备注)
        data.put("sellerMessage", batchId);
        data.put("buyerMessage", addressId);
        request.addParam("data", JSON.toJSONString(data));
        try {
            logger.info("订单创建成功，入参值为"+JSON.toJSONString(data)+" 返回值为");
            logger.info(client.execute(request));
            //执行扣费逻辑
            String touch_id = String.valueOf(receiverInfo.get("touch_id"));
            String sql = "SELECT t1.amount,t2.supplier_id FROM t_touch_express_log t1 LEFT JOIN t_market_resource t2 ON t1.resource_id=t2.resource_id " +
                    "WHERE t1.touch_id='" + touch_id + "' LIMIT 1";
            Map<String, Object> amountMap = jdbcTemplate.queryForMap(sql);
            BigDecimal amount = new BigDecimal(String.valueOf(amountMap.get("amount")));
            String supplierId = String.valueOf(amountMap.get("supplier_id"));
            sourceDao.supplierAccountDuctions(supplierId, amount);
            //更新prod_amount 资源金额 字段
            String prodAmount = "UPDATE t_touch_express_log SET prod_amount=amount WHERE touch_id='" + touch_id + "'";
            jdbcTemplate.update(prodAmount);
        } catch (Exception e) {
            logger.info("订单创建失败，返回值为");
            logger.info(e.getMessage());
        }


    }

    /**
     * 更新t_touch_express_log中的快递信息
     *
     * @param
     * @return
     * @auther Chacker
     * @date 2019/8/9 15:48
     */
    public void updateExpressInfo(Map<String, Object> tempMap, Map<String, Object> senderInfo) {
//        String requestId = DigestUtils.md5Hex(String.valueOf(tempMap.get("addressId"))).toUpperCase();
        //根据touch_id关联，把requestId更新到t_touch_express_log中 status更新为"2" 已发送
        StringBuffer updateRequestId = new StringBuffer("UPDATE t_touch_express_log SET create_time=NOW(),status='2',");
        String addressIdNew = String.valueOf(tempMap.get("addressId"));
        String pdfPath = String.valueOf(tempMap.get("pdfPath"));
        updateRequestId.append("sender_message='").append(senderInfo.toString())
                .append("',file_path='").append(pdfPath)
                .append("' FROM nl_batch_detail")
                .append("WHERE nl_batch_detail.touch_id=t_touch_express_log.touch_id AND nl_batch_detail.id='")
                .append(addressIdNew).append("'");
    }

    public int countNumber(String batchId) throws Exception {
        String querySql = "SELECT COUNT(id) count FROM nl_batch_detail  WHERE batch_id=? AND express_path IS NOT NULL ";
        List<Map<String, Object>> list = sourceDao.sqlQuery(querySql, new Object[]{batchId});
        int sendCount = 0;
        if (list.size() > 0) {
            String count = String.valueOf(list.get(0).get("count"));
            sendCount = Integer.parseInt(count);
        }
        return sendCount;
    }

    public List<Map<String, Object>> query(String supplier_id, int type_code) {
        List<Map<String, Object>> list = null;
        if (StringUtil.isNotEmpty(supplier_id)) {
            StringBuilder sqlBuilder = new StringBuilder("SELECT resource_id FROM t_market_resource WHERE supplier_id=? AND type_code=?");
            list = customerDao.sqlQuery(sqlBuilder.toString(), supplier_id, type_code);
        } else {
            StringBuilder sqlBuilder = new StringBuilder("SELECT * FROM t_market_resource");
            list = customerDao.sqlQuery(sqlBuilder.toString());
        }
        return list;


    }

    /**
     * 批量发送快递
     *
     * @param
     */
    public void sendAllExpress(String batchId, String custId, Long userId) {
        JSONObject json = new JSONObject();
        //根据企业查询默认发件信息
        String sendId = null;
        String queryIdSql = "select id FROM t_sender_info WHERE cust_id =? AND type = 1";
        List<Map<String, Object>> sendIdlist = customerDao.sqlQuery(queryIdSql, new Object[]{custId});
        if (sendIdlist.size() > 0) {
            sendId = String.valueOf(sendIdlist.get(0).get("id"));
        }
        //根据批次iD查询所有修复成功的数据
        String querySql = "select batch_id,channel,id,express_path FROM nl_batch_detail WHERE batch_id =? AND express_path IS NOT NULL";
        List<Map<String, Object>> list = customerDao.sqlQuery(querySql, new Object[]{batchId});
        for (int i = 0; i < list.size(); i++) {
            json.put("batchId", list.get(i).get("batch_id"));
            json.put("channel", list.get(i).get("channel"));
            json.put("id", list.get(i).get("id"));
            json.put("fileName", list.get(i).get("express_path"));
            json.put("sendId", sendId);
            //默认发送成功
            try {
                //sendExpress(json, userId, custId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
