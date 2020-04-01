package com.bdaim.common.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dto.Page;
import com.bdaim.customs.entity.*;
import com.bdaim.util.ESUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.RestUtil;
import com.bdaim.util.StringUtil;
import com.bdaim.util.http.HttpUtil;
import io.searchbox.client.JestClient;
import io.searchbox.client.JestResult;
import io.searchbox.core.*;
import io.searchbox.indices.mapping.GetMapping;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
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

    //@Autowired
    //private TransportClient transportClient;

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
    public JSONObject getDocument(String index, String type, String id) {
        JSONObject result = null;
        try {
            LOG.info("向es查询记录:index[" + index + "],type[" + type + "],id:[" + id + "]");
            String httpResult = HttpUtil.httpGet(ESUtil.getUrl(index, type) + id, null, null);
            JSONObject jsonObject = JSON.parseObject(httpResult);
            LOG.info("向es查询记录:index[" + index + "],type[" + type + "],id:[" + id + "]返回数据:{}", jsonObject);
            if (jsonObject == null || (jsonObject != null && !jsonObject.getBoolean("found"))) {
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
     * 判断ES是否含有索引名的索引
     *
     * @param indexName
     * @return
     */
    public boolean isExistIndex(String indexName, String typeName) {
        GetMapping.Builder builder = new GetMapping.Builder();
        builder.addIndex(indexName).addType(typeName);
        try {
            JestResult result = jestClient.execute(builder.build());
            if (404 == result.getResponseCode()) {
                return false;
            }
            return true;
        } catch (Exception e) {
            LOG.error("es查询索引异常", e);
        }
        return false;
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
            if (!isExistIndex(index, type)) {
                JSONObject settings = JSON.parseObject("{\"settings\":{\"index.analysis.analyzer.default.type\":\"whitespace\"}}");
                HttpEntity<JSONObject> entity = new HttpEntity<>(settings, headers);
                ResponseEntity<JSONObject> resultEntity = restTemplate.exchange(ESUtil.getUrl(index, ""), HttpMethod.PUT, entity, JSONObject.class);
                result = resultEntity.getBody();
                LOG.info("向es添加索引返回数据:[" + result + "]");
            }
            HttpEntity<JSONObject> entity = new HttpEntity<>(jsonObject, headers);
            ResponseEntity<JSONObject> resultEntity = restTemplate.exchange(ESUtil.getUrl(index, type) + id, HttpMethod.PUT, entity, JSONObject.class);
            result = resultEntity.getBody();
            LOG.info("向es新增记录返回结果:[" + result + "]");
        } catch (Exception e) {
            LOG.error("向es新增记录异常:", e);
        }
        return result;
    }

    static List<JSONObject> list1;

    /**
     * 向ElasticSearch中批量新增
     *
     * @param indexName
     * @param typeName
     * @param list
     * @throws Exception
     */
    public void bulkInsertDocument(String indexName, String typeName, List<JSONObject> list) {
        boolean result = false;
        try {
            if (!isExistIndex(indexName, typeName)) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                JSONObject settings = JSON.parseObject("{\"settings\":{\"index.analysis.analyzer.default.type\":\"whitespace\"}}");
                HttpEntity<JSONObject> entity = new HttpEntity<>(settings, headers);
                ResponseEntity<JSONObject> resultEntity = restTemplate.exchange(ESUtil.getUrl(indexName, ""), HttpMethod.PUT, entity, JSONObject.class);
                JSONObject resultEntityBody = resultEntity.getBody();
                LOG.info("向es添加索引返回数据:[" + resultEntityBody + "]");
            }
            Bulk.Builder bulk = new Bulk.Builder().defaultIndex(indexName).defaultType(typeName);
            for (JSONObject obj : list) {
                Index index = new Index.Builder(obj).id(obj.getString("id")).build();
                bulk.addAction(index);
            }
            BulkResult br = jestClient.execute(bulk.build());
            //LOG.info("向ES中批量新增原始结果:" + br.getJsonString());
            result = br.isSucceeded();
            if (result) {
                list1 = list;
            }
            if (!result) {
                LOG.error("", list);
            }
        } catch (Exception e) {
            LOG.error("向ES中批量新增异常", e);
        }
        LOG.info("向ES中批量新增返回结果:" + result);
    }

    /**
     * 批量删除es
     *
     * @param indexName
     * @param typeName
     * @param list
     */
    public void bulkDeleteDocument(String indexName, String typeName, List<String> list) {
        boolean result = false;
        try {
            Bulk.Builder bulk = new Bulk.Builder().defaultIndex(indexName).defaultType(typeName);
            for (String id : list) {
                Delete index = new Delete.Builder(id).build();
                bulk.addAction(index);
            }
            BulkResult br = jestClient.execute(bulk.build());
            result = br.isSucceeded();
        } catch (Exception e) {
            LOG.error("向ES中批量删除异常", e);
        }
        LOG.info("向ES中批量删除返回结果:" + result);
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
            if (result != null && result.containsKey("_source")) {
                result = result.getJSONObject("_source");
            }
            LOG.info("从es查询记录返回结果:[" + httpResult + "]");
        } catch (Exception e) {
            LOG.error("从es查询记录异常:", e);
        }
        return result;
    }

    public JSONObject getDocumentById0(String index, String type, String id) {
        JSONObject result = null;
        try {
            LOG.info("向es查询记录:index[" + index + "],type[" + type + "],id:[" + id + "]");
            Get get = new Get.Builder(index, id).type(type).build();
            DocumentResult response = jestClient.execute(get);
            LOG.info("向es查询记录:index[" + index + "],type[" + type + "],id:[" + id + "]返回数据:{}", response.getJsonObject());
            if (response.isSucceeded()) {
                result = JSONObject.parseObject(response.getSourceAsString());
            }
            LOG.info("向es查询记录返回结果:[" + result + "]");
        } catch (Exception e) {
            LOG.error("向es查询记录异常:", e);
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
            if (!isExistIndex(index, type)) {
                HttpHeaders headers = new HttpHeaders();
                headers.setContentType(MediaType.APPLICATION_JSON);
                JSONObject settings = JSON.parseObject("{\"settings\":{\"index.analysis.analyzer.default.type\":\"whitespace\"}}");
                HttpEntity<JSONObject> entity = new HttpEntity<>(settings, headers);
                ResponseEntity<JSONObject> resultEntity = restTemplate.exchange(ESUtil.getUrl(index, ""), HttpMethod.PUT, entity, JSONObject.class);
                result = resultEntity.getBody();
                LOG.info("向es添加索引返回数据:[" + result + "]");
            }
            JSONObject data = new JSONObject();
            data.put("doc", jsonObject);
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<JSONObject> entity = new HttpEntity<>(data, headers);
            result = restTemplate.postForObject(ESUtil.getUrl(index, type) + id + "/_update/", entity, JSONObject.class);
            LOG.info("向es修改记录返回结果:[" + result + "]");
        } catch (Exception e) {
            LOG.error("向es修改记录异常:" + e.getMessage());
        }
        return result;
    }

    public void update(HBusiDataManager hBusiDataManager, Long id) {
        String type = "+HMetaDataDef.getTable()+";
        if (type.equals(BusiTypeEnum.SZ.getKey()) || type.equals(BusiTypeEnum.CZ.getKey()) || type.equals(BusiTypeEnum.BZ.getKey())) {
            updateDocumentToType(Constants.SZ_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        } else if (type.equals(BusiTypeEnum.SF.getKey()) || type.equals(BusiTypeEnum.CF.getKey()) || type.equals(BusiTypeEnum.BF.getKey())) {
            updateDocumentToType(Constants.SF_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        } else if (type.equals(BusiTypeEnum.SS.getKey()) || type.equals(BusiTypeEnum.CS.getKey()) || type.equals(BusiTypeEnum.BS.getKey())) {
            updateDocumentToType(Constants.SS_INFO_INDEX, "haiguan", id.toString(), JSON.parseObject(hBusiDataManager.getContent()));
        }
    }


    public static void main(String[] args) {
        testAdd();
    }

    private static void testAdd() {
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

    private void testUpdate() {
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

    private static String testAddDocumentToType(String index, String type, String id, JSONObject jsonObject) {
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

    private JSONObject testUpdateDocumentToType(String index, String type, String id, JSONObject jsonObject) {
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
    private JSONObject getEsData(String index, String type, JSONObject queryStr) {
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
    private Page returnDataPackage(JSONObject data, String index) {
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
     * ES查询
     *
     * @param dsl
     * @param index
     * @param indexType
     * @param sourceType
     * @param <T>
     * @return
     */
    public <T> List<Object> listSearch(String dsl, String index, String indexType, Class<T> sourceType) {
        List<Object> list = new ArrayList<>();
        SearchResult result = search(dsl, index, indexType);
        if (result != null) {
            List<SearchResult.Hit<T, Void>> hits = result.getHits(sourceType);
            T t;
            for (SearchResult.Hit<T, Void> hit : hits) {
                t = hit.source;
                list.add(t);
            }
        }
        return list;
    }

    /**
     * ES分页查询
     *
     * @param dsl        执行dsl语句
     * @param index      索引
     * @param indexType
     * @param sourceType 数据实体类
     * @param <T>
     * @return
     */
    public <T> Page pageSearch(String dsl, String index, String indexType, Class<T> sourceType) {
        Page page = new Page();
        SearchResult result = search(dsl, index, indexType);
        List list = new ArrayList<>();
        if (result != null) {
            List<SearchResult.Hit<T, Void>> hits = result.getHits(sourceType);
            T t;
            for (SearchResult.Hit<T, Void> hit : hits) {
                t = hit.source;
                list.add(t);
            }
            page.setData(list);
            page.setTotal(NumberConvertUtil.parseInt(result.getTotal()));
        }
        return page;
    }

    public Page pageSearch(String dsl, String index, String indexType) {
        Page page = new Page();
        SearchResult result = search(dsl, index, indexType);
        List list = new ArrayList<>();
        if (result != null) {
            List<SearchResult.Hit<JSONObject, Void>> hits = result.getHits(JSONObject.class);
            JSONObject t;
            for (SearchResult.Hit<JSONObject, Void> hit : hits) {
                t = hit.source;
                t.put("id", hit.id);
                list.add(t);
            }
            page.setData(list);
            page.setTotal(NumberConvertUtil.parseInt(result.getTotal()));
        }
        return page;
    }

    /**
     * ES检索
     *
     * @param dsl
     * @param index
     * @param indexType
     * @return
     */
    public SearchResult search(String dsl, String index, String indexType) {
        LOG.info("index:{},indexType:{},ES检索语句:\n{}", index, indexType, dsl);
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
        LOG.info("ES查询结果:\n{}", result.getJsonString());
        return result;
    }

    /*public SearchResponse searchByClient(String dsl, String index, String indexType) {
        LOG.info("index:{},indexType:{},ES检索语句:\n{}", index, indexType, dsl);
        SearchResponse result = transportClient.prepareSearch(index).setTypes(indexType).execute().actionGet();
        return result;
    }*/

    public SearchSourceBuilder queryConditionToDSL(JSONObject params) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        BoolQueryBuilder qb = QueryBuilders.boolQuery();
        QueryBuilder queryBuilder = null;
        Iterator keys = params.keySet().iterator();
        while (keys.hasNext()) {
            String key = (String) keys.next();
            if (StringUtil.isEmpty(String.valueOf(params.get(key)))) continue;
            if ("pageNum".equals(key) || "pageSize".equals(key) || "stationId".equals(key) || "cust_id".equals(key))
                continue;
            // 排序
            if (key.startsWith("_sort_")) {
                searchSourceBuilder.sort(key.substring(6), SortOrder.valueOf(params.getString(key).toUpperCase()));
                continue;
            } else if (key.startsWith("_c_")) {
                // 模糊查询
                queryBuilder = QueryBuilders.matchPhraseQuery(key.substring(3), params.getString(key));
            } else if (key.startsWith("_g_")) {
                queryBuilder = QueryBuilders.rangeQuery(key.substring(3)).gt(params.get(key));
            } else if (key.startsWith("_ge_")) {
                queryBuilder = QueryBuilders.rangeQuery(key.substring(4)).gte(params.get(key));
            } else if (key.startsWith("_l_")) {
                queryBuilder = QueryBuilders.rangeQuery(key.substring(3)).lt(params.get(key));
            } else if (key.startsWith("_le_")) {
                queryBuilder = QueryBuilders.rangeQuery(key.substring(4)).lte(params.get(key));
            } else if (key.startsWith("_range_")) {
                if ("0".equals(String.valueOf(params.get(key)))) {
                    queryBuilder = QueryBuilders.rangeQuery(key.substring(7)).lte(params.get(key));
                } else {
                    queryBuilder = QueryBuilders.rangeQuery(key.substring(7)).gte(params.get(key));
                }
            } else {
                queryBuilder = QueryBuilders.matchQuery(key, params.get(key));
            }

            qb.must(queryBuilder);
        }
        if (params.getInteger("pageNum") != null && params.getInteger("pageSize") != null) {
            int pageNum = 1;
            int pageSize = 10;
            try {
                pageNum = params.getIntValue("pageNum");
            } catch (Exception e) {
            }
            try {
                pageSize = params.getIntValue("pageSize");
            } catch (Exception e) {
            }
            if (pageNum <= 0)
                pageNum = 1;
            if (pageSize <= 0)
                pageSize = 10;
            if (pageSize > 1000)
                pageSize = 1000;
            int from = (pageNum - 1) * pageSize;
            searchSourceBuilder.from(from).size(pageSize);
        }
        searchSourceBuilder.query(qb);
        return searchSourceBuilder;
    }

    /*private TransportClient getClient() {
        if (client != null) {
            return client;
        }
        try {
            //设置集群名称
            Settings settings = Settings.builder().put("cluster.name", "elasticsearch").build();
            //创建client
            client = new TransportClient.Builder().settings(settings).build()
                    .addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
            return client;
        } catch (Exception e) {
            LOG.error("连接ES异常", e);
            return null;
        }
    }
*/
    public boolean bulkInsertDocument0(String index, String type, List<JSONObject> list) {
        /*if (!isExistIndex(index, type)) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            JSONObject settings = JSON.parseObject("{\"settings\":{\"index.analysis.analyzer.default.type\":\"whitespace\"}}");
            HttpEntity<JSONObject> entity = new HttpEntity<>(settings, headers);
            ResponseEntity<JSONObject> resultEntity = restTemplate.exchange(ESUtil.getUrl(index, ""), HttpMethod.PUT, entity, JSONObject.class);
            JSONObject resultEntityBody = resultEntity.getBody();
            LOG.info("向es添加索引返回数据:[" + resultEntityBody + "]");
        }

        TransportClient client = getClient();
        BulkRequestBuilder bulkRequest = client.prepareBulk();
        System.out.println("index=" + index + ",count =" + list.size());
        for (JSONObject jsonObject : list) {
            bulkRequest.add(client.prepareIndex(index, type)
                    .setId(jsonObject.getString("id")).setSource(jsonObject)
            );
        }
        long beginTime = System.currentTimeMillis();
        BulkResponse bulkResponse = bulkRequest.execute().actionGet();
        long endTime = System.currentTimeMillis();
        System.out.println("took =" + bulkResponse.getTookInMillis());
        System.out.println("cost = " + (endTime - beginTime) / 1000f);
        System.out.println();
        if (bulkResponse.hasFailures()) {
            System.out.println("erros->"+bulkResponse.buildFailureMessage());
        } else {
            return true;
        }*/
        return false;
    }

}
