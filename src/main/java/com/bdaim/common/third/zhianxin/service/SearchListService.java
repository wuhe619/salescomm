package com.bdaim.common.third.zhianxin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
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

        put("1001", "C1001_test");
        put("1002", "C1002_test");
        put("1003", "C1003_test");
        put("1004", "C1004_test");
        put("1005", "C1005_test");
        put("1006", "C1006_test");
        put("1007", "C1007_test");
        put("1008", "C1008_test");
        put("1009", "C1009_test");
        put("1010", "C1010_test");
        put("1011", "C1011_test");
        put("1012", "C1012_test");
        put("1013", "C1013_test");
        put("1014", "C1014_test");
        put("1015", "C1015_test");
        put("1016", "C1016_test");
        put("1017", "C1017_test");
        put("1018", "C1018_test");
        put("1019", "C1019_test");
        put("1020", "C1020_test");
        put("1021", "C1021_test");
        put("1022", "C1022_test");
        put("1023", "C1023_test");
        put("1024", "C1024_test");
        put("1025", "C1025_test");
        put("1026", "C1026_test");
        put("1027", "C1027_test");
        put("1028", "C1028_test");
        put("1029", "C1029_test");
        put("1030", "C1030_test");
        put("1031", "C1031_test");
        put("1032", "C1032_test");
        put("1033", "C1033_test");
        put("1034", "C1034_test");
        put("1035", "C1035_test");
        put("1036", "C1036_test");
        put("1037", "C1037_test");
        put("1038", "C1038_test");
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
        BaseResult baseResult = JSON.parseObject(result, BaseResult.class);
        // TODO 处理企业领取标志
        if (baseResult != null && baseResult.getData() != null) {
            JSONObject data = (JSONObject) baseResult.getData();
            JSONArray list = data.getJSONArray("list");
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    list.getJSONObject(i).put("_receivingStatus", 2);
                }
            }
        }
        return baseResult;
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
