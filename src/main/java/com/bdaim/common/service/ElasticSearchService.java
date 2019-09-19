package com.bdaim.common.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.common.util.ESUtil;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.RestUtil;
import com.bdaim.common.util.http.HttpUtil;
import com.bdaim.customs.entity.Constants;
import com.bdaim.customs.entity.MainDan;
import com.bdaim.customs.entity.PartyDan;
import com.bdaim.customs.entity.Product;
import io.searchbox.client.JestClient;
import io.searchbox.core.Search;
import io.searchbox.core.SearchResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/29
 * @description
 */
@Service
public class ElasticSearchService {

    public static final Logger LOG = LoggerFactory.getLogger(ElasticSearchService.class);

    public static final String CUSTOMER_SEA_INDEX_PREFIX = "customer_sea_";

    public static final String CUSTOMER_SEA_TYPE = "data";

    @Resource
    private RestTemplate restTemplate;
    @Autowired
    private JestClient jestClient;

    /**
     * 查询记录
     *
     * @param index
     * @param type
     * @param id
     * @return
     */
    public static JSONObject getDocument(String index, String type, String id) {
        JSONObject result = null;
        try {
            LOG.info("向es查询记录:index[" + index + "],type[" + type + "],id:[" + id + "]");
            String httpResult = HttpUtil.httpGet(ESUtil.getUrl(index, type) + id, null, null);
            JSONObject jsonObject = JSON.parseObject(httpResult);
            if (jsonObject == null || !jsonObject.getBoolean("found")) {
                return new JSONObject();
            }
            result = jsonObject.getJSONObject("_source");
            LOG.info("向es查询记录返回结果:[" + jsonObject + "]");
        } catch (Exception e) {
            LOG.error("向es查询记录异常:", e);
        }
        return result;
    }

    /**
     * 新增记录
     *
     * @param index
     * @param type
     * @param id
     * @param jsonObject
     * @return
     */
    public JSONObject addDocumentToType(String index, String type, String id, JSONObject jsonObject) {
        JSONObject result = null;
        try {
            LOG.info("向es新增记录:index[" + index + "],type[" + type + "],id:[" + id + "],data:[" + jsonObject + "]");
            /*JSONObject document = getDocument(index, type, id);
            if (document.size() > 0) {
                LOG.warn("id:[" + id + "]已经存在,不可再次添加");
                return new JSONObject();
            }*/
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<JSONObject> entity = new HttpEntity<>(jsonObject, headers);
            ResponseEntity<JSONObject> resultEntity = restTemplate.exchange(ESUtil.getUrl(index, type) + id, HttpMethod.PUT, entity, JSONObject.class);
            result = resultEntity.getBody();
            LOG.info("向es新增记录返回结果:[" + result + "]");
        } catch (Exception e) {
            LOG.error("向es新增记录异常:" + e.getMessage());
        }
        return result;
    }

    /**
     * 删除文档
     *
     * @param index
     * @param type
     * @param id
     * @return
     */
    public JSONObject deleteDocumentFromType(String index, String type, String id) {
        JSONObject result = null;
        try {
            LOG.info("从es删除记录:index[" + index + "],type[" + type + "],id:[" + id + "]");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<JSONObject> entity = new HttpEntity<>(headers);
            ResponseEntity<JSONObject> resultEntity = restTemplate.exchange(ESUtil.getUrl(index, type) + id, HttpMethod.DELETE, entity, JSONObject.class);
            result = resultEntity.getBody();
            LOG.info("从es删除记录返回结果:[" + result + "]");
        } catch (Exception e) {
            LOG.error("从es删除记录异常:" + e.getMessage());
        }
        return result;
    }

    public JSONObject getDocumentById(String index, String type, String id) {
        JSONObject result = null;
        try {
            LOG.info("从es查询数据:index[" + index + "],type[" + type + "],id:[" + id + "]");
            String httpResult = HttpUtil.httpGet(ESUtil.getUrl(index, type) + id, null, null);
            result = JSON.parseObject(httpResult);
            result = result.getJSONObject("hits");
            if (result.containsKey("_source")) {
                result = result.getJSONObject("_source");
            }
            LOG.info("从es查询记录返回结果:[" + httpResult + "]");
        } catch (Exception e) {
            LOG.error("从es查询记录异常:" + e.getMessage());
        }
        return result;
    }


    /**
     * 修改记录
     *
     * @param index
     * @param type
     * @param id
     * @param jsonObject
     * @return
     */
    public JSONObject updateDocumentToType(String index, String type, String id, JSONObject jsonObject) {
        JSONObject result = null;
        try {
            LOG.info("向es修改记录:index[" + index + "],type[" + type + "],id:[" + id + "],data:[" + jsonObject + "]");
            JSONObject data = new JSONObject();
            data.put("doc", jsonObject);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<JSONObject> entity = new HttpEntity<>(data, headers);
            result = restTemplate.postForObject(ESUtil.getUrl(index, type) + id + "/_update/", entity, JSONObject.class);
            System.out.println(result);
            LOG.info("向es修改记录返回结果:[" + result + "]");
        } catch (Exception e) {
            LOG.error("向es修改记录异常:" + e.getMessage());
        }
        return result;
    }


    public static void main(String[] args) {
        testAdd();
    }

    //@Test
    public static void testAdd() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "1000");
        jsonObject.put("superId", "1");
        jsonObject.put("superName", "测试");
        jsonObject.put("superPhone", "18811112222");
        jsonObject.put("superTelphone", "01088888888");
        jsonObject.put("lastUserId", "18071707123300000");
        jsonObject.put("dataSource", "1");
        jsonObject.put("batchId", "52");
        jsonObject.put("createTime", "2019-06-29 11:51:33");
        jsonObject.put("lastCallTime", "2019-06-29 11:51:33");
        jsonObject.put("lastCallResult", "1001");
        jsonObject.put("calledDuration", "50");
        jsonObject.put("intentLevel", "A");
        jsonObject.put("1810250433080000", "成功");
        jsonObject.put("1905241118460007", "地址");
        jsonObject.put("1905241126410010", "1");
        //jsonObject.put("1903180518350002", "程宁");

        testAddDocumentToType("customer_sea_190622170552000000", "data", "1", jsonObject);
    }

    //@Test
    public void testUpdate() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("id", "2");
        jsonObject.put("superId", "2");
        jsonObject.put("superName", "范冰冰");
        jsonObject.put("superPhone", "18811112222");
        jsonObject.put("superTelphone", "01088888888");
        jsonObject.put("lastUserId", "18071707123300000");
        jsonObject.put("dataSource", "1");
        jsonObject.put("batchId", "52");
        jsonObject.put("createTime", "2019-06-29 11:51:33");
        jsonObject.put("lastCallTime", "2019-06-29 11:51:33");
        jsonObject.put("lastCallResult", "1001");
        jsonObject.put("calledDuration", "50");
        jsonObject.put("intentLevel", "A");
        jsonObject.put("1810250433080000", "成功");
        jsonObject.put("1905241118460007", "地址");
        jsonObject.put("1905241126410010", "1");
        jsonObject.put("1903180518350002", "程宁");
        JSONObject data = new JSONObject();
        data.put("doc", jsonObject);
        testUpdateDocumentToType("customer_sea_190622170552000000", "data", "2", data);
    }

    public static String testAddDocumentToType(String index, String type, String id, JSONObject jsonObject) {
        String result = "";
        try {
            LOG.info("向es新增记录:index[" + index + "],type[" + type + "],id:[" + id + "],data:[" + jsonObject + "]");
//            JSONObject document = getDocument(index, type, id);
//            if (document.size() > 0) {
//                throw new TouchException(id, "id:[" + id + "]已经存在,不可再次添加");
//            }
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<JSONObject> entity = new HttpEntity<>(jsonObject, headers);
            ResponseEntity<String> resultEntity = restTemplate.exchange(ESUtil.getUrl(index, type) + id, HttpMethod.PUT, entity, String.class);
            System.out.println(resultEntity.getBody());
            result = resultEntity.getBody();
            System.out.println(result);
            LOG.info("向es新增记录返回结果:[" + result + "]");
        } catch (Exception e) {
            LOG.error("rest error:" + e.getMessage());
        }
        return result;
    }

    public JSONObject testUpdateDocumentToType(String index, String type, String id, JSONObject jsonObject) {
        JSONObject result = null;
        try {
            LOG.info("向es修改记录:index[" + index + "],type[" + type + "],id:[" + id + "],data:[" + jsonObject + "]");
            RestTemplate restTemplate = new RestTemplate();
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<JSONObject> entity = new HttpEntity<>(jsonObject, headers);
            result = restTemplate.postForObject(ESUtil.getUrl(index, type) + id + "/_update/", entity, JSONObject.class);
            System.out.println(result);
            LOG.info("向es修改记录返回结果:[" + result + "]");
        } catch (Exception e) {
            LOG.error("rest error:" + e.getMessage());
        }
        return result;
    }

    /**
     * @description 查询ES信息
     * @author:duanliying
     * @method
     * @date: 2019/9/18 11:36
     */
    public JSONObject getEsData(String index, String type, JSONObject queryStr) {
        String r = null;
        LOG.info("向es修改记录:index[" + index + "],type[" + type + "],data:[" + queryStr + "]");
        try {
            r = RestUtil.postDataWithParms(net.sf.json.JSONObject.fromObject(queryStr), ESUtil.getUrl(index, type) + "/_search/");
        } catch (Exception e) {
            LOG.error("插查询es信息异常", e);
        }
        return JSONObject.parseObject(r);
    }


    /**
     * @description 查询ES数据封装
     * @author:duanliying
     * @method
     * @date: 2019/9/18 11:36
     */
    public Page returnDataPackage(JSONObject data, String index) {
        Page page = new Page();
        LOG.info("封装数据信息是：" + data.toJSONString() + "封装对象索引是：" + index);
        try {
            JSONObject hits = data.getJSONObject("hits");
            JSONArray dataList = new JSONArray();
            int total = hits.getIntValue("total");
            if (hits != null) {
                //获取数组信息
                JSONArray jsonArray = hits.getJSONArray("hits");
                JSONObject hitsList;
                for (int i = 0; i < jsonArray.size(); i++) {
                    hitsList = jsonArray.getJSONObject(i);
                    int id = hitsList.getIntValue("_id");
                    JSONObject source = hitsList.getJSONObject("_source");
                    source.put("id", id);
                    dataList.add(source);
                }

                if (Constants.SZ_INFO_INDEX.equals(index)) {
                    //封装主单对象
                    List<MainDan> mainDanList = JSON.parseArray(dataList.toJSONString(), MainDan.class);
                    page.setData(mainDanList);
                } else if (Constants.SF_INFO_INDEX.equals(index)) {
                    //封装分单对象
                    List<PartyDan> partyDanList = JSON.parseArray(dataList.toJSONString(), PartyDan.class);
                    page.setData(partyDanList);
                } else if (Constants.SS_INFO_INDEX.equals(index)) {
                    //封装商品对象
                    List<Product> productDanList = JSON.parseArray(dataList.toJSONString(), Product.class);
                    page.setData(productDanList);
                }
                page.setTotal(total);
            }
        } catch (Exception e) {
            LOG.error("封装数据信息异常", e);
        }
        return page;
    }

    /**
     * ES执行dsl语句
     * dsl 查询语句
     * index 索引
     *
     * @return
     */
    public List<Object> searchToEsLit(String dsl, String index, String indexType) {
        List<Object> list = new ArrayList<>();
        LOG.info("ES检索语句是" + dsl);
        SearchResult result = null;
        try {
            Search search = new Search.Builder(dsl)
                    .addIndex(index)
                    .addType(indexType)
                    .build();
            result = jestClient.execute(search);
        } catch (IOException e) {
            LOG.error("ES查询异常", e);
        }
        LOG.info("ES查询结果:{}", result.getJsonString());

        if (result != null) {
            if (Constants.SZ_INFO_INDEX.equals(index)) {
                List<SearchResult.Hit<MainDan, Void>> hits = result.getHits(MainDan.class);
                for (SearchResult.Hit<MainDan, Void> hit : hits) {
                    MainDan mainDan = hit.source;
                    list.add(mainDan);
                }
            } else if (Constants.SF_INFO_INDEX.equals(index)) {
                List<SearchResult.Hit<PartyDan, Void>> hits = result.getHits(PartyDan.class);
                for (SearchResult.Hit<PartyDan, Void> hit : hits) {
                    PartyDan partyDan = hit.source;
                    list.add(partyDan);
                }
            } else if (Constants.SS_INFO_INDEX.equals(index)) {
                List<SearchResult.Hit<Product, Void>> hits = result.getHits(Product.class);
                for (SearchResult.Hit<Product, Void> hit : hits) {
                    Product product = hit.source;
                    list.add(product);
                }
            }
        }
        return list;
    }

    /**
     * ES执行dsl语句
     * dsl 查询语句
     * index 索引
     *
     * @return
     */
    public Page searchToEsPage(String dsl, String index, String indexType) {
        Page page = new Page();
        LOG.info("ES检索语句是" + dsl);
        SearchResult result = null;
        try {
            Search search = new Search.Builder(dsl)
                    .addIndex(index)
                    .addType(indexType)
                    .build();
            result = jestClient.execute(search);
        } catch (IOException e) {
            LOG.error("ES查询异常", e);
        }
        LOG.info("ES查询结果:{}", result.getJsonString());

        List<Object> list = new ArrayList<>();
        if (result != null) {
            if (Constants.SZ_INFO_INDEX.equals(index)) {
                List<SearchResult.Hit<MainDan, Void>> hits = result.getHits(MainDan.class);
                for (SearchResult.Hit<MainDan, Void> hit : hits) {
                    MainDan mainDan = hit.source;
                    list.add(mainDan);
                }
            } else if (Constants.SF_INFO_INDEX.equals(index)) {
                List<SearchResult.Hit<PartyDan, Void>> hits = result.getHits(PartyDan.class);
                for (SearchResult.Hit<PartyDan, Void> hit : hits) {
                    PartyDan partyDan = hit.source;
                    list.add(partyDan);
                }
            } else if (Constants.SS_INFO_INDEX.equals(index)) {
                List<SearchResult.Hit<Product, Void>> hits = result.getHits(Product.class);
                for (SearchResult.Hit<Product, Void> hit : hits) {
                    Product product = hit.source;
                    list.add(product);
                }
            }
            page.setData(list);
            page.setTotal(NumberConvertUtil.parseInt(result.getTotal()));
        }
        return page;
    }
}
