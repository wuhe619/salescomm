package com.bdaim.customs.utils;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customs.dao.HDicDao;
import com.bdaim.customs.entity.HDic;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class HDicUtil {
    public static final Logger LOG = LoggerFactory.getLogger(HDicUtil.class);

    public static Map<String, HDic> DIC_CACHE = new HashMap<>(16);

    private static Map<String, String> KEY_ALIAS = new HashMap(16) {{
        put("fee_curr", "CURR_CODE");
        put("insur_curr", "CURR_CODE");
        put("curr_code", "CURR_CODE");
        put("qiyun_port", "DECL_PORT");
        put("insur_mark", "FEE_MARK");
        put("fee_mark", "FEE_MARK");
        put("other_mark", "FEE_MARK");
        put("trade_country", "SEND_COUNTRY");
        put("receive_country", "SEND_COUNTRY");

    }};

    private String split = "$";

    @Autowired
    private HDicDao hDicDao;

    @PostConstruct
    private void init() {
        LOG.info("开始初始化缓存字典数据时间:[{}]", System.currentTimeMillis());
        List<HDic> list = hDicDao.listHDic();
        for (HDic dic : list) {
            if (dic == null || StringUtil.isEmpty(dic.getType()) || StringUtil.isEmpty(dic.getCode())) {
                continue;
            }
            saveCache(dic.getType(), dic.getCode(), dic);
        }
        initHResource();
        LOG.info("完成初始化缓存字典数据时间:[{}],size:{}", System.currentTimeMillis(), DIC_CACHE.size());
    }

    private void initHResource() {
        String stationSql = "select content, create_id, create_date, update_id, update_date from h_resource where type=?  ";
        List<Map<String, Object>> list = hDicDao.sqlQuery(stationSql, "duty_paid_rate");
        if (list != null) {
            JSONObject jsonObject;
            HDic hDic;
            for (Map<String, Object> m : list) {
                jsonObject = JSON.parseObject(String.valueOf(m.get("content")));
                if (jsonObject != null) {
                    hDic = new HDic();
                    hDic.setType("code_ts".toUpperCase());
                    hDic.setCode(jsonObject.getString("code"));
                    hDic.setName_zh(jsonObject.getString("g_model"));
                    saveCache("code_ts".toUpperCase(), jsonObject.getString("code"), hDic);
                }
            }
        }
    }

    public void saveCache(String type, String code, HDic dic) {
        DIC_CACHE.put(type + split + code, dic);
    }

    public HDic getCache(String type, String code) {
        if (KEY_ALIAS.containsKey(type.toLowerCase())) {
            type = KEY_ALIAS.get(type.toLowerCase());
        }
        HDic dic = DIC_CACHE.get(type + split + code);
        return dic;
    }
}
