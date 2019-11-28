package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.common.dto.Page;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.util.ExcelUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class HBillService {
    private static Logger logger = LoggerFactory.getLogger(HBillService.class);

    @Resource
    CustomerDao customerDao;

    public Map<String, Object> getCustomerBill(CustomerBillQueryParam param) throws Exception {
        StringBuffer querySql = new StringBuffer("SELECT batch_id batchId, busi_type busiType,cust_id custId,create_time createTime,COUNT(id) number, IFNULL(amount/ 100,0) unitPrice,content, IFNULL(SUM(amount) / 100,0) amount ,IFNULL(SUM(prod_amount) / 100,0) prodAmount ,resource_id FROM t_resource_log WHERE 1=1 ");
        if (StringUtil.isNotEmpty(param.getCustomerId())) {
            querySql.append("AND cust_id = '" + param.getCustomerId() + "' ");
        }
        if (StringUtil.isNotEmpty(param.getBillDate())) {
            querySql.append("AND create_time LIKE '" + param.getBillDate() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getStatus())) {
            querySql.append(" AND JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.status') =" + param.getStatus());
        } else {
            querySql.append(" AND JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.status')>0");
        }
        querySql.append(" GROUP BY batch_id,resource_id order by create_time desc ");
        Page data = customerDao.sqlPageQuery(querySql.toString(), param.getPageNum(), param.getPageSize());
        Map<String, Object> map = new HashMap<>();
        if (data != null) {
            List<Map<String, Object>> list = data.getData();
            BigDecimal custSumAmount = new BigDecimal("0");
            for (int i = 0; i < list.size(); i++) {
                logger.info("企业消费金额是：" + String.valueOf(list.get(i).get("amount")) + "成本费用是：" + String.valueOf(list.get(i).get("prodAmount")));
                String profitAmount = new BigDecimal(String.valueOf(list.get(i).get("amount"))).subtract(new BigDecimal(String.valueOf(list.get(i).get("prodAmount")))).setScale(2, BigDecimal.ROUND_DOWN).toString();
                list.get(i).put("profitAmount", profitAmount);
                custSumAmount = custSumAmount.add(new BigDecimal(String.valueOf(list.get(i).get("amount"))));
                //根据批次id查询企业名称
                String custId = String.valueOf(list.get(i).get("custId"));
                String enterpriseName = customerDao.getEnterpriseName(custId);
                list.get(i).put("custName", enterpriseName);
                String content = String.valueOf(list.get(i).get("content"));
                if (StringUtil.isNotEmpty(content)) {
                    JSONObject jsonObject = JSON.parseObject(content);
                    list.get(i).put("mainId", jsonObject.getString("main_bill_no"));
                }
            }
            map.put("custSumAmount", custSumAmount.doubleValue());
            map.put("data", data.getData());
            map.put("total", data.getTotal());
        }

        return map;
    }


    /**
     * 查询账单详情页
     *
     * @param param
     * @return
     */
    public Page getBillDetail(CustomerBillQueryParam param) {
        Page page = null;
        try {
            StringBuffer querySql = new StringBuffer("SELECT id, busi_type busiType, cust_id custId, create_time createTime, IFNULL(amount / 100, 0)  unitPrice, content, IFNULL(amount / 100, 0) amount, IFNULL(prod_amount / 100, 0) prodAmount FROM t_resource_log WHERE 1=1");
            if (param.getMainId() != null) {
                querySql.append(" AND batch_id='" + param.getMainId()+"'");
            }
            if (StringUtil.isNotEmpty(param.getTransactionId())) {
                querySql.append(" AND id ='" + param.getTransactionId() + "'");
            }
            if (StringUtil.isNotEmpty(param.getStatus())) {
                querySql.append(" AND JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.status') =" + param.getStatus());
            } else {
                querySql.append(" AND JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.status')>0");
            }
            if (StringUtil.isNotEmpty(param.getStartTime())) {
                querySql.append(" AND create_time >= '" + param.getStartTime() + "'");
            }
            if (StringUtil.isNotEmpty(param.getEndTime())) {
                querySql.append(" AND create_time <='" + param.getEndTime() + "'");
            }
            querySql.append(" order by create_time desc");
            page = customerDao.sqlPageQuery(querySql.toString(), param.getPageNum(), param.getPageSize());
            if (page != null) {
                List<Map<String, Object>> list = page.getData();
                for (int i = 0; i < list.size(); i++) {
                    //查询身份证信息
                    String content = String.valueOf(list.get(i).get("content"));
                    JSONObject jsonObject = JSON.parseObject(content);
                    if (jsonObject != null) {
                        int status = jsonObject.getIntValue("status");
                        list.get(i).put("status", status);
                        list.get(i).put("checkStatus", status == 1 ? "成功" : "失败");
                        JSONObject input = jsonObject.getJSONObject("input");
                        if (input != null) {
                            String idCard = input.getString("idCard");
                            list.get(i).put("idCard", idCard);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询账单详情异常", e);
        }
        return page;
    }

    /**
     * 企业账单页导出
     *
     * @param param
     * @return
     */
    public void customerBillExport(CustomerBillQueryParam param, String exportType, HttpServletResponse response) throws IOException, IllegalAccessException {
        List<Map<String, Object>> list = exportBillDetail(param);
        ExcelUtil.exportExcelByList(list,  exportType, response);
    }


    /**
     * 查询账单详情页
     *
     * @param param
     * @return
     */
    public List<Map<String, Object>> exportBillDetail(CustomerBillQueryParam param) {
        List<Map<String, Object>> list = null;
        try {
            StringBuffer querySql = new StringBuffer("SELECT id, busi_type busiType, cust_id custId, create_time createTime, IFNULL(amount / 100, 0)  unitPrice, content, IFNULL(amount / 100, 0) amount, IFNULL(prod_amount / 100, 0) prodAmount FROM t_resource_log WHERE 1=1");
            if (param.getMainId() != null) {
                querySql.append(" AND CASE WHEN JSON_VALID(content) THEN batch_id ='" + param.getMainId() + "' ELSE null END");
            }
            if (StringUtil.isNotEmpty(param.getTransactionId())) {
                querySql.append(" AND id ='" + param.getTransactionId() + "'");
            }
            if (StringUtil.isNotEmpty(param.getStatus())) {
                querySql.append(" AND CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.status') =" + param.getStatus() + " ELSE null END ");
            } else {
                querySql.append(" AND CASE WHEN JSON_VALID(content) THEN JSON_EXTRACT(REPLACE(REPLACE(REPLACE(content,'\t', ''),CHAR(13),'') ,CHAR(10),''), '$.status')>0" + " ELSE null END");
            }
            if (StringUtil.isNotEmpty(param.getStartTime())) {
                querySql.append(" AND create_time >= '" + param.getStartTime() + "'");
            }
            if (StringUtil.isNotEmpty(param.getEndTime())) {
                querySql.append(" AND create_time <='" + param.getEndTime() + "'");
            }

            list = customerDao.sqlQuery(querySql.toString());
            for (int i = 0; i < list.size(); i++) {
                //查询身份证信息
                String content = String.valueOf(list.get(i).get("content"));
                JSONObject jsonObject = JSON.parseObject(content);
                if (jsonObject != null) {
                    int status = jsonObject.getIntValue("status");
                    list.get(i).put("status", status);
                    list.get(i).put("checkStatus", status == 1 ? "成功" : "失败");
                    JSONObject input = jsonObject.getJSONObject("input");
                    if (input != null) {
                        String idCard = input.getString("idCard");
                        list.get(i).put("idCard", idCard);
                    }
                }
            }
        } catch (Exception e) {
            logger.error("查询账单详情异常", e);
        }
        return list;
    }
}

