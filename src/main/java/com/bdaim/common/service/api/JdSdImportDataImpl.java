package com.bdaim.common.service.api;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.redis.RedisUtil;
import com.bdaim.common.util.LogUtil;
import com.bdaim.common.util.MD5Util;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerLabelDao;
import com.bdaim.customer.entity.CustomerLabel;
import com.bdaim.resource.dao.MarketResourceDao;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

/**
 * 导入客户群数据(京东-尚德)
 *
 * @author chengning@salescomm.net
 * @date 2019/2/14 16:17
 */
@Service("JdSdImportDataService")
@Transactional
public class JdSdImportDataImpl {

    @Resource
    private MarketResourceDao marketResourceDao;

    @Resource
    private CustomerDao customerDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private CustomerLabelDao customerLabelDao;

    @Resource
    private RedisUtil redisUtil;

    private final static String CUST_ID = "1903120348469162";

    //客户人群数据上限
    private final static Integer CUSTOMER_DATA_UP_LIMIT = 500000;

    //private final static String DB_NAME = "label_dev";
    private final static String DB_NAME = " ";

    private final static String U_INSERT_SQL = "replace INTO " + DB_NAME + " u (id,phone) VALUES(?,?)";

    public String execute(HttpServletRequest request) {
        JSONObject json = new JSONObject();
        //客户群id
        String groupId = request.getParameter("id");
        String data = request.getParameter("data");
        String custId = request.getParameter("custId");

        JSONArray phones;
        if(StringUtil.isEmpty(custId)){
            LogUtil.error("参数custId不能为空" );
            json.put("code", 4);
            json.put("message", "custId不能为空");
            return json.toJSONString();
        }
        try {
            phones = JSON.parseArray(data);
        } catch (Exception e) {
            LogUtil.error("数据转换异常:" + e);
            json.put("code", -3);
            json.put("message", "参数格式错误");
            return json.toJSONString();
        }
        if (phones == null) {
            LogUtil.warn("数据异常:" + JSON.toJSONString(phones));
            json.put("code", -4);
            json.put("message", "参数异常");
            return json.toJSONString();
        }
        if (phones.size() > 10000) {
            LogUtil.warn("单次最大上传条数超过10000,大小" + phones.size());
            json.put("code", 2);
            json.put("message", "单次最大上传条数不可以超过10000");
            return json.toJSONString();
        }
        int total = getCustomerGroupListDataCount(groupId);
        if (total >= CUSTOMER_DATA_UP_LIMIT) {
            LogUtil.warn("客群数据量超过了上限");
            json.put("code", 1);
            json.put("message", "客群数据超过了上限");
            return json.toJSONString();
        }

        // 检查客户群权限
        String querySql = "SELECT * FROM " + DB_NAME + " customer_group WHERE cust_id = '" + CUST_ID + "' AND id = '" + groupId + "'";
        List cGroup = marketResourceDao.sqlQuery(querySql);
        if (cGroup == null || (cGroup != null && cGroup.size() == 0)) {
            json.put("code", -5);
            json.put("message", "操作异常");
            return json.toJSONString();
        }

        if (phones.size() > 0) {
            String phone, md5Phone;
            int updateQuantity;
            List<Map<String, String>> groupValueList = new ArrayList<>();
//            List<Map<String, String>> uValueList = new ArrayList<>();
            // 构造客户群数据表保存sql
            StringBuffer sb = new StringBuffer();
            sb.append(" replace INTO " + DB_NAME + " t_customer_group_list_" + groupId + " (id,super_data) VALUES(?,?)");
            Map<String,String> uMap = new HashMap<>();
            for (int i = 0; i < phones.size(); i++) {
                phone = String.valueOf(phones.getJSONObject(i).get("phone"));
                //手机号码通过c+手机号  进行MD5加密 作为id  同时存入u表
                md5Phone = MD5Util.encode32Bit("c" + phone);
                //自定义标签
                JSONObject superData = new JSONObject();
                //将数据存储进数据库
                LogUtil.info("插入用户群表的sql:" + sb.toString());

                Set<String> set = phones.getJSONObject(i).keySet();
                String[] keys = set.toArray(new String[0]);
                for (int j = 0; j < keys.length; j++) {
                    String k = keys[j];
                    if ("phone".equals(k)) continue;
                    CustomerLabel label = customerLabelDao.getCustomerLabelByName(k, CUST_ID);
                    String v = phones.getJSONObject(i).getString(String.valueOf(k));
                    if (label != null) {
                        superData.put(label.getLabelId(), v);
                    } else {
                        superData.put(k, v);
                    }
                }
                Map<String, String> map = new TreeMap<>();
                map.put("md5Phone", md5Phone);
                map.put("superData", superData.toJSONString());
                groupValueList.add(map);
/*
                Map<String, String> umap = new TreeMap<>();
                umap.put("md5Phone", md5Phone);
                umap.put("phone", phone);
                uValueList.add(umap);*/
                // status = marketResourceDao.executeUpdateSQL(sb.toString(), new Object[]{md5Phone,superData.toJSONString()});

                uMap.put(md5Phone,phone);
                if (groupValueList.size() % 1000 == 0) {
                    total = getCustomerGroupListDataCount(groupId);
                    if (total >= CUSTOMER_DATA_UP_LIMIT) {
                        LogUtil.warn("客群数据量超过了上限");
                        json.put("code", 1);
                        json.put("message", "客群数据超过了上限");
                        return json.toJSONString();
                    }
                    insertData2GroupDB(sb.toString(), groupValueList);
                    //insertData2U(U_INSERT_SQL, uValueList);
                    groupValueList = new ArrayList<>();
                   // uValueList = new ArrayList<>();
                }
            }
            if(!uMap.isEmpty()) {
                redisUtil.batchSet(uMap);
            }
            if (groupValueList.size() > 0) {
                total = getCustomerGroupListDataCount(groupId);
                if (total >= CUSTOMER_DATA_UP_LIMIT) {
                    LogUtil.warn("客群数据量超过了上限");
                    json.put("code", 1);
                    json.put("message", "客群数据超过了上限");
                    return json.toJSONString();
                }
                insertData2GroupDB(sb.toString(), groupValueList);
                //insertData2U(U_INSERT_SQL, uValueList);
            }
            total = getCustomerGroupListDataCount(groupId);
            // 更新客户群客户数量
            updateQuantity = marketResourceDao.executeUpdateSQL("UPDATE customer_group SET user_count =  ? WHERE id = ?", new Object[]{total, groupId});
            LogUtil.info("更新客户群客户数量状态:" + updateQuantity + ",数量:" + total);
        }

        json.put("code", 0);
        json.put("message", "成功");

        return json.toJSONString();
    }


    private void insertData2GroupDB(String sql, List<Map<String, String>> mapList) {
        try {
            jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                    String md5Phone = mapList.get(i).get("md5Phone");
                    String superData = mapList.get(i).get("superData");
                    preparedStatement.setString(1, md5Phone);
                    preparedStatement.setString(2, superData);
                }

                @Override
                public int getBatchSize() {
                    return mapList.size();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void insertData2U(String sql, List<Map<String, String>> mapList) {
        jdbcTemplate.batchUpdate(sql, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                String md5Phone = mapList.get(i).get("md5Phone");
                String phone = mapList.get(i).get("phone");
                preparedStatement.setString(1, md5Phone);
                preparedStatement.setString(2, phone);
            }

            @Override
            public int getBatchSize() {
                return mapList.size();
            }
        });

    }

    private void updateUserCount() {

    }

    /**
     * 查询客户群数据总数
     *
     * @param groupId
     * @return
     */
    private int getCustomerGroupListDataCount(String groupId) {
        String countSql = "select count(0) from t_customer_group_list_" + groupId;
        int total = customerDao.queryForInt(countSql);
        return total;
    }
}
