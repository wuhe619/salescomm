package com.bdaim.util;

import com.bdaim.AppConfig;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

public class ESUtil {

    public static JSONObject format(String groupCondition) {
        JSONObject params = new JSONObject();

        if (groupCondition == null || "".equals(groupCondition))
            return params;

        JSONArray groupTerms = JSONArray.fromObject(groupCondition);
        JSONObject query = new JSONObject();
        JSONObject constant_score = new JSONObject();
        JSONObject filter = new JSONObject();
        JSONObject bool = new JSONObject();
        JSONArray must = new JSONArray();

        if (groupTerms.size() > 0) {
            //拆解labelObj和运算符到数组

            for (int i = 0; i < groupTerms.size(); i++) {
                JSONObject element = groupTerms.getJSONObject(i);
                JSONObject terms = new JSONObject();

                JSONObject term = new JSONObject();
                JSONArray termv = new JSONArray();
                JSONArray leafs = element.getJSONArray("leafs");
                for (int k = 0; k < leafs.size(); k++) {
                    JSONObject leaf = leafs.getJSONObject(k);
                    termv.add(leaf.getString("name"));
                }

                term.put(element.getString("labelId") + ".keyword", termv);
                terms.put("terms", term);
                must.add(terms);
            }
        }
        bool.put("must", must);
        filter.put("bool", bool);
        constant_score.put("filter", filter);
        query.put("constant_score", constant_score);
        params.put("query", query);
        System.out.println(params.toString());
        return params;
    }

    public static JSONObject formatForInterface(String groupTermStr) throws Exception {
        JSONObject params = new JSONObject();

        if (StringUtil.isEmpty(groupTermStr)) return params;

        JSONObject groupTermObj = JSONObject.fromObject(groupTermStr);
        JSONArray groupTerms = groupTermObj.getJSONArray("label");
        String size = null;
        if (groupTermObj.containsKey("size")) {
            size = groupTermObj.getString("size");
        }
        JSONObject query = new JSONObject();
        JSONObject constant_score = new JSONObject();
        JSONObject filter = new JSONObject();
        JSONObject bool = new JSONObject();
        JSONArray must = new JSONArray();
        JSONObject _filter = new JSONObject();
        for (int i = 0; i < groupTerms.size(); i++) {
            JSONObject item = groupTerms.getJSONObject(i);
            if (item.containsKey("geo")) {
                String geo = item.getJSONArray("geo").getString(0);
                String[] ll = geo.split("\\|");
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("distance", ll[2]);//距离(单位m)
                JSONObject geoObj = new JSONObject();
                geoObj.put("lon", ll[0]);//精度
                geoObj.put("lat", ll[1]);//纬度
                jsonObject.put("geo", geoObj);
                _filter.put("geo_distance", jsonObject);
            } else {
                JSONObject terms = new JSONObject();
                terms.put("terms", item);
                must.add(terms);
            }
        }
        if (must.size() > 0) {
            bool.put("must", must);
        }
        if (!_filter.isEmpty()) {
            bool.put("filter", _filter);
        }
        filter.put("bool", bool);
        constant_score.put("filter", filter);
        query.put("constant_score", constant_score);
        params.put("query", query);
        if (StringUtil.isNotEmpty(size)) {
            params.put("size", size);
        }
        System.out.println(params.toString());
        return params;
    }

    public static void main(String[] args) throws Exception {
        String s = "{\"label\":[{\"0001\":[\"男\"]},{\"0002\":[\"北京\",\"上海\"]},{\"geo\":[\"112.1|123.23|5999\"]}]}";
        formatForInterface(s);
    }

    public static String getUrl() {
        int cycle = 0;
        String es_rest = AppConfig.getEs_rest();
        String esIndexName = AppConfig.getDs_es_index_0();
        String esTypeName = AppConfig.getDs_es_type_0();
        String url = es_rest + "/" + esIndexName + "/" + esTypeName + "/";
        return url;
    }

    public static String getUrl(String index, String type) {
        String es_rest = AppConfig.getEs_rest();
        String url = es_rest + "/" + index + "/" + type + "/";
        return url;
    }

}
