package com.bdaim.common.third.zhianxin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.third.zhianxin.dto.BaseResult;
import com.bdaim.util.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019-10-29 11:03
 */
@Service
@Transactional
public class SearchListService {

    private static Logger LOG = LoggerFactory.getLogger(SearchListService.class);

    private final static String API_URL = "https://api.bdaim.com/{busiType}/pub";

    private static final String TOKEN = "Bearer df35eaea4360348832eea2a2ec9f76c70ea9c82b";

    private static final Map<String, String> BUSI_TYPE = new HashMap() {{
        put("1", "B1001_test");
        put("2", "B1002_test");
        put("3", "B1003_test");
        put("4", "B1004_test");
        put("5", "B1005_test");
        put("6", "B1006_test");

        put("101", "C1001_test");
        put("102", "B1002_test");
        put("103", "B1003_test");
        put("104", "B1004_test");
        put("105", "B1005_test");
        put("106", "B1006_test");
    }};

    /**
     * 智侒信列表检索
     *
     * @param custId
     * @param custGroupId
     * @param custUserId
     * @param busiType
     * @param params
     * @return
     * @throws Exception
     */
    public BaseResult pageSearch(String custId, String custGroupId, Long custUserId, String busiType, JSONObject params) throws Exception {
        params.put("pageNo", params.getLongValue("pageNum"));
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", TOKEN);
        LOG.info("企业列表查询参数:{}", params);
        String result = HttpUtil.httpPost(API_URL.replace("{busiType}", BUSI_TYPE.get(busiType)), params.toJSONString(), headers);
        LOG.info("企业列表查询接口返回:{}", result);
        return JSON.parseObject(result, BaseResult.class);
    }

    /**
     * 企业详情
     *
     * @param companyId 企业ID
     * @param entName   企业名称
     * @param busiType  业务类型
     * @return
     * @throws Exception
     */
    public BaseResult getCompanyDetail(String companyId, String entName, String busiType) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", TOKEN);
        JSONObject params = new JSONObject();
        //params.put("entName", entName);
        params.put("companyId", companyId);
        LOG.info("企业详情查询参数:{}", params);
        String result = HttpUtil.httpPost(API_URL.replace("{busiType}", BUSI_TYPE.get(busiType)), params.toJSONString(), headers);
        LOG.info("企业详情查询接口返回:{}", result);
        return JSON.parseObject(result, BaseResult.class);
    }
}
