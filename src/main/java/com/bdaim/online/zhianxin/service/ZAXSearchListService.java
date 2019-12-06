package com.bdaim.online.zhianxin.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.PhoneService;
import com.bdaim.online.zhianxin.dto.BaseResult;
import com.bdaim.customer.service.B2BTcbLogService;
import com.bdaim.util.StringUtil;
import com.bdaim.util.http.HttpUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019-10-29 11:03
 */
@Service
@Transactional
public class ZAXSearchListService {

    private static Logger LOG = LoggerFactory.getLogger(ZAXSearchListService.class);

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
        put("1039", "C1039_test");
        put("1040", "C1040_test");
        put("1041", "C1041_test");
    }};

    @Autowired
    B2BTcbLogService b2BTcbLogService;

    @Autowired
    PhoneService phoneService;

    @Autowired
    JdbcTemplate jdbcTemplate;

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
        String result = HttpUtil.httpPost(API_URL.replace("{busiType}", BUSI_TYPE.get(busiType)), params.toJSONString(), headers, 300000);
        if(StringUtil.isEmpty(result)){
            result = "{}";
        }
        LOG.info("企业列表查询接口返回:{}", result);
        BaseResult baseResult = JSON.parseObject(result, BaseResult.class);
        // 处理企业领取标志
        if (baseResult != null && baseResult.getData() != null && !params.containsKey("fieldType")) {
            JSONObject data = (JSONObject) baseResult.getData();
            JSONArray list = data.getJSONArray("list");
            if (list != null && list.size() > 0) {
                for (int i = 0; i < list.size(); i++) {
                    list.getJSONObject(i).put("_receivingStatus", b2BTcbLogService.checkClueGetStatus(custId, list.getJSONObject(i).getString("id")));
                }
            }
        }
        return baseResult;
    }

    /**
     * 检索列表 只返回企业ID
     *
     * @param custId
     * @param custGroupId
     * @param custUserId
     * @param busiType
     * @param params
     * @return
     * @throws Exception
     */
    public BaseResult pageSearchIds(String custId, String custGroupId, Long custUserId, String busiType, JSONObject params) throws Exception {
        //领取，只返回id
        params.put("fieldType", false);
        BaseResult baseResult = pageSearch(custId, custGroupId, custUserId, busiType, params);
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
        LOG.info("企业详情地址:{},查询参数:{}", API_URL.replace("{busiType}", BUSI_TYPE.get(busiType)), params);
        String result = HttpUtil.httpPost(API_URL.replace("{busiType}", BUSI_TYPE.get(busiType)), params.toJSONString(), headers, 300000);
        LOG.info("企业详情查询接口返回:{}", result);
        return JSON.parseObject(result, BaseResult.class);
    }

    public BaseResult getCompanyDetail(String companyId, String busiType, JSONObject params) throws Exception {
        Map<String, Object> headers = new HashMap<>();
        headers.put("Authorization", TOKEN);
        //params.put("entName", entName);
        params.put("companyId", companyId);
        LOG.info("企业详情地址:{},查询参数:{}", API_URL.replace("{busiType}", BUSI_TYPE.get(busiType)), params);
        String result = HttpUtil.httpPost(API_URL.replace("{busiType}", BUSI_TYPE.get(busiType)), params.toJSONString(), headers, 300000);
        LOG.info("企业详情查询接口返回:{}", result);
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
    public BaseResult getCompanyDetail(String companyId, String entName, String busiType, long seaId) throws Exception {
        BaseResult baseResult = getCompanyDetail(companyId, entName, busiType);
        if (baseResult != null && seaId > 0) {
            //处理公司联系方式是否有意向
            handleClueFollowStatus(seaId, (JSONObject) baseResult.getData());
        }
        return baseResult;
    }

    public BaseResult getCompanyDetail(String companyId, JSONObject param, String busiType, long seaId) throws Exception {
        param.put("pageNo", param.getLongValue("pageNum"));
        BaseResult baseResult = getCompanyDetail(companyId, busiType, param);
        if (baseResult != null && seaId > 0) {
            //处理公司联系方式是否有意向
            handleClueFollowStatus(seaId, (JSONObject) baseResult.getData());
        }
        return baseResult;
    }

    /**
     * 处理公司联系方式是否有意向
     *
     * @param seaId
     * @param data
     */
    private void handleClueFollowStatus(long seaId, JSONObject data) {
        if (data.containsKey("phoneNumber") && data.getJSONArray("phoneNumber") != null
                && data.getJSONArray("phoneNumber").size() > 0) {
            JSONArray phoneNumber = data.getJSONArray("phoneNumber");
            String sql = "SELECT id from t_customer_sea_list_11 WHERE JSON_EXTRACT(super_data, '$.SYS007') = ? AND id = ?";
            JSONArray clueFollowStatus = new JSONArray();
            JSONObject jsonObject = null;
            String uid = "";
            List<Map<String, Object>> id = null;
            for (int i = 0; i < phoneNumber.size(); i++) {
                jsonObject = new JSONObject();
                uid = phoneService.savePhoneToAPI(phoneNumber.getString(i));
                id = jdbcTemplate.queryForList(sql, "意向线索", uid);
                jsonObject.put("phone", phoneNumber.getString(i));
                jsonObject.put("status", id == null || id.size() == 0 ? false : true);
                clueFollowStatus.add(jsonObject);
            }
            data.put("clueFollowStatus", clueFollowStatus);
        }
    }
}
