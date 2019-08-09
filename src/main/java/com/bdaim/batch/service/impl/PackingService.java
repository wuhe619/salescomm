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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.persistence.criteria.CriteriaBuilder;
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
    @Autowired
    private JdbcTemplate jdbcTemplate;

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

    /**
     * 确认发件/批量发件
     *
     * @param map batchId、addressId、isBatch
     * @return
     * @auther Chacker
     * @date 2019/8/8 14:42
     */
    public void sendExpress(Map<String, Object> map) {
        //map中包括批次ID和地址ID，以及isBatch状态判断，
        int isBatch = Integer.parseInt(String.valueOf(map.get("isBatch")));
        String batchId = String.valueOf(map.get("batchId"));
        String addressId = String.valueOf(map.get("addressId"));
        String updateBatchStatus = "UPDATE nl_batch SET status='5' WHERE id='" + batchId + "'";
        if (isBatch == 1) {
            //批量发送、将批次状态status修改为【5】【待取件】
            jdbcTemplate.update(updateBatchStatus);
            //将批次详情的状态label_seven 修改为 【3】【待取件】
            String updateDetail = "UPDATE nl_batch_detail SET label_seven='3' WHERE batch_id='" + batchId + "'";
            jdbcTemplate.update(updateDetail);
        } else if (isBatch == 0) {
            //单个发送 将批次详情的状态 label_seven 状态修改为 【3】【待取件】
            StringBuffer stringBuffer = new StringBuffer("UPDATE nl_batch_detail SET label_seven='3' WHERE batch_id='");
            stringBuffer.append(batchId).append("' AND id='").append(addressId).append("'");
            jdbcTemplate.update(stringBuffer.toString());
            //如果该批次下已没有待发件的 快递信息，则把该批次更新为 【5】【待取件】
            String countSql = "SELECT COUNT(*) AS count FROM nl_batch_detail WHERE label_seven='2' AND batch_id='" + batchId + "'";
            Map<String, Object> result = jdbcTemplate.queryForMap(countSql);
            int count = Integer.parseInt(String.valueOf(result.get("count")));
            if (count == 0) {
                jdbcTemplate.update(updateBatchStatus);
            }
        }
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
