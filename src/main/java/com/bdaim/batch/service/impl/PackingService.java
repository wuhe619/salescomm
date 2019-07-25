package com.bdaim.batch.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.dao.BatchDetailDao;
import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.common.util.ConstantsUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.supplier.dto.SupplierEnum;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
    private static Logger logger = Logger.getLogger(PackingService.class);
    @Resource
    private BatchDetailDao batchDetailDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private SourceDao sourceDao;
    @Resource
    private MarketResourceService marketResourceService;

    public List<Map<String, Object>> getMacResoult(String batchId) {
        String querySql = "SELECT * FROM tmp_nl_batch_detail b  WHERE batch_id = 1542005544857";
        List<Map<String, Object>> list = batchDetailDao.sqlQuery(querySql);
        return list;
    }

    public List<Map<String, Object>> getAddressResoult(String batchId) {
        String querySql = "SELECT * FROM tmp_nl_batch_detail b  WHERE batch_id = 1542099439991";
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
        CustomerProperty jdFailPrice = customerDao.getProperty(custId, "jd_fail_price");
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

    public void sendExpress(JSONObject param, Long userId, String customerId) throws Exception {
        //默认发送成功
        String batchId = String.valueOf(param.get("batchId"));
        String channel = String.valueOf(param.get("channel"));
        String id = String.valueOf(param.get("id"));
        String fileName = String.valueOf(param.get("fileName"));
        String sendId = String.valueOf(param.get("sendId"));
        //保存快递记录参数
        String name = null, senderMessage = null;
        double custExpressPrice = 0.0, suppperExpressPrice;
        BigDecimal custExpressAmount, sourceSmsAmount;
        boolean accountDeductionStatus, supperExpressStatu;
        String touchId = Long.toString(IDHelper.getTransactionId());
        //根据id和batchId查询收件人姓名
        BatchDetail batchDetail = batchDetailDao.getBatchDetail(id, batchId);
        String resourceId = marketResourceService.queryResourceId(String.valueOf(SupplierEnum.JD.getSupplierId()), ConstantsUtil.EXPRESS_TYPE);
        if (batchDetail != null) {
            //收件人姓名
            name = batchDetail.getName();
        }
        //根据sendId查询发件人信息
        String querySql = "SELECT sender_name,phone,province,city,district,address,postcodes FROM t_sender_info WHERE id  =" + sendId;
        List<Map<String, Object>> list = batchDetailDao.sqlQuery(querySql);
        if (list.size() > 0) {
            senderMessage = JSON.toJSONString(list.get(0));
        }
        //查询发送快递的成本价和销售价进行扣费
        custExpressPrice = getCustExpressPrice(customerId);
        logger.info("发送快递扣费客户:" + customerId + "发送快递单价:" + custExpressPrice);
        custExpressAmount = new BigDecimal(custExpressPrice);
        logger.info("发送快递扣费客户:" + customerId + ",开始扣费,金额:" + custExpressAmount.doubleValue());
        accountDeductionStatus = customerDao.accountDeductions(customerId, custExpressAmount);
        logger.info("发送快递扣费客户:" + customerId + ",扣费状态:" + accountDeductionStatus);
        //供应商扣费
        suppperExpressPrice = getsupplierExpressPrice(customerId, SupplierEnum.JD.getSupplierId());
        logger.info("发送快递供应商:" + customerId + "发送快递单价:" + suppperExpressPrice);
        logger.info("发送快递供应商:" + customerId + "发送快递扣费,金额:" + suppperExpressPrice);
        //供应商扣费需要转换为分进行扣减
        sourceSmsAmount = new BigDecimal(suppperExpressPrice * 100);
        supperExpressStatu = sourceDao.supplierAccountDuctions(SupplierEnum.JD.getSupplierId(), sourceSmsAmount);
        logger.info("发送快递扣费供应商:" + customerId + "发送快递扣费状态:" + supperExpressStatu);
        logger.info("发送快递参数是：" + "batchId\t" + batchId + "地址id\t" + id + "发送快递文件名字" + fileName + "channel:" + channel + "发件地址Id" + sendId);
        //保存快递记录
        String insertLogSql = "INSERT INTO t_touch_express_log (touch_id,batch_id,address_id,receive_name,sender_message,create_time,STATUS,cust_id,user_id,file_path,amount,prod_amount,resource_id) VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?)";
        sourceDao.executeUpdateSQL(insertLogSql, new Object[]{touchId, batchId, id, name, senderMessage, LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")), 1, customerId, userId, fileName, custExpressPrice, suppperExpressPrice, resourceId});
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
                sendExpress(json, userId, custId);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
