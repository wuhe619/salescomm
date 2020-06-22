package com.bdaim.batch.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.dao.BatchDetailDao;
import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.batch.entity.BatchSendToFileResp;
import com.bdaim.batch.service.BatchService;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.*;

@Service("BatchService")
public class BatchServiceImpl implements BatchService {
    private static Logger logger = LoggerFactory.getLogger(BatchServiceImpl.class);
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private BatchDetailDao batchDetailDao;


    private String createId = "unicom_bigdatabjhk";

    private String activityId = "JZYX-000-201808281730";

    private String departTypeId = "03";

    private String partnerName = "获客（北京）科技有限公司";

    private String endTime = "2019-08-26 00:00:00";

    private String sendNum = "01084285088";

    private String ivr = "1";

    private static final String urlsendtofile = "http://120.52.23.243:9001/InformationNav/rs/sending/sendServer/sendtofile";

    @Override
    public Boolean repeatIdCardStatus(String batchId) {
        Boolean status = false;
        StringBuilder sqlBuilder = new StringBuilder("select id_card,count(*) as coutNum from nl_batch_detail ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" where batch_id =? ");
            p.add(batchId);
        }
        sqlBuilder.append(" group BY id_card HAVING coutNum>1 ");

        List<Map<String, Object>> mapRepeat = jdbcTemplate.queryForList(sqlBuilder.toString(), p.toArray());
        if (mapRepeat != null && mapRepeat.size() > 0) {
            if (mapRepeat.get(0).get("coutNum") != null) status = true;
        }
        return status;
    }

    @Override
    public Boolean repeateEntrpriseIdStatus(String batchId) {
        Boolean status = false;
        StringBuilder sqlBuilder = new StringBuilder("select enterprise_id,count(*) as coutNum from nl_batch_detail ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" where batch_id = ? ");
            p.add(batchId);
        }
        sqlBuilder.append(" group BY enterprise_id HAVING coutNum>1 ");

        List<Map<String, Object>> mapRepeat = jdbcTemplate.queryForList(sqlBuilder.toString(), p.toArray());
        if (mapRepeat != null && mapRepeat.size() > 0) {
            if (mapRepeat.get(0).get("coutNum") != null) status = true;
        }
        return status;
    }


    @Override
    public String sendtofile(LinkedList<String> certilist, LinkedList<String> cususerIdlist, String repairMode, String batchId) {
        long startTime4 = System.currentTimeMillis();
        JSONObject resultJson = new JSONObject();
        BatchSendToFileResp bstfResp = new BatchSendToFileResp();
        StringBuilder sbcerti = new StringBuilder();
        StringBuilder sbcususer = new StringBuilder();
        for (int i = 0; i < certilist.size(); i++) {
            if (i == (certilist.size() - 1)) {
                sbcerti.append(certilist.get(i));
            } else {
                sbcerti.append(certilist.get(i)).append(",");
            }
        }
        for (int i = 0; i < cususerIdlist.size(); i++) {
            if (i == (cususerIdlist.size() - 1)) {
                sbcususer.append(cususerIdlist.get(i));
            } else {
                sbcususer.append(cususerIdlist.get(i)).append(",");
            }
        }

        List<NameValuePair> nvps = new ArrayList<NameValuePair>();
        nvps.add(new BasicNameValuePair("createId", createId));
        nvps.add(new BasicNameValuePair("activityId", activityId));
        nvps.add(new BasicNameValuePair("departTypeId", departTypeId));
        nvps.add(new BasicNameValuePair("batchId", batchId));
        nvps.add(new BasicNameValuePair("partnerName", partnerName));
        nvps.add(new BasicNameValuePair("startTime", formatDate(new Date())));
        nvps.add(new BasicNameValuePair("endTime", endTime));
        nvps.add(new BasicNameValuePair("sendNum", sendNum));
        nvps.add(new BasicNameValuePair("ivr", ivr));
        nvps.add(new BasicNameValuePair("certList", sbcerti.toString()));
        nvps.add(new BasicNameValuePair("kehuId", sbcususer.toString()));
        nvps.add(new BasicNameValuePair("contactType", repairMode));

        try {
            String result = httpPostType(urlsendtofile, nvps);
            logger.info("result= " + "，" + result);
            long endTime4 = System.currentTimeMillis();
            logger.info("联通上传修复耗时：" + (endTime4 - startTime4) + "ms" + "\t修复方式：" + repairMode);
            resultJson = JSONObject.parseObject(result);
        } catch (Exception e) {
            logger.error("批次修复上传异常\t" + e.getMessage());
        }
        String errorCode = resultJson.getString("errorCode");
        return errorCode;
    }

    public String formatDate(Date date) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return sdf.format(date);
    }

    private static String httpPostType(String urlsendtofile, List<NameValuePair> nvps) {
        String result = "";
        CloseableHttpClient client = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(urlsendtofile);
        try {
            httpPost.setEntity(new UrlEncodedFormEntity(nvps, "UTF-8"));
            httpPost.setHeader("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
            CloseableHttpResponse response = client.execute(httpPost);
            HttpEntity entity = response.getEntity();
            if (entity != null) {
                result = EntityUtils.toString(entity, "UTF-8");
            }
        } catch (Exception e) {
            logger.error("httpPostType: query" + "\t" + e.getMessage());
        }
        return result;
    }


    @Override
    public String batchNameGet(String batchId) {
        String batchName = "";
        StringBuilder sqlBuilder = new StringBuilder("SELECT batch_name from nl_batch  ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(batchId)) {
            sqlBuilder.append(" where id = ? ");
            p.add(batchId);
        }

        List<Map<String, Object>> mapRepeat = jdbcTemplate.queryForList(sqlBuilder.toString(), p.toArray());
        if (mapRepeat != null && mapRepeat.size() > 0) {
            if (mapRepeat.get(0).get("batch_name") != null) batchName = mapRepeat.get(0).get("batch_name").toString();
        }
        return batchName;
    }

    @Override
    public int uploadNumGet(String compId) {
        int uploadNum = 0;
        StringBuilder sqlBuilder = new StringBuilder("SELECT upload_num from nl_batch WHERE status in(2,4,5) ");
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(compId)) {
            sqlBuilder.append(" and comp_id= ? ");
            p.add(compId);
        }
        List<Map<String, Object>> mapRepeat = jdbcTemplate.queryForList(sqlBuilder.toString(), p.toArray());
        if (mapRepeat != null && mapRepeat.size() > 0) {
            for (Map map : mapRepeat) {
                if (map != null && map.get("upload_num") != null) {
                    uploadNum += Integer.valueOf(map.get("upload_num").toString());
                }
            }
        }
        return uploadNum;
    }

    public BatchDetail getBatchDetail(String id, String batchId) {
        return batchDetailDao.getBatchDetail(id, batchId);
    }

}
