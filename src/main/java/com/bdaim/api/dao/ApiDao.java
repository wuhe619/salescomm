package com.bdaim.api.dao;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.api.entity.ApiEntity;
import com.bdaim.api.entity.ApiProperty;
import com.bdaim.api.entity.CustomerApiResourcePrecent;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.supplier.entity.SupplierEntity;
import com.bdaim.util.redis.RedisUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class ApiDao extends SimpleHibernateDao<ApiEntity, Integer> {

    @Resource
    private RedisUtil redisUtil;

    public ApiEntity getApi(int apiId) {
        String hql = "from ApiEntity m where m.apiId=?";
        List<ApiEntity> list = this.find(hql, apiId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 企业属性编辑与新增
     */
    public void dealCustomerInfo(String apiId, String propertyName, String propertyValue) throws Exception{
        ApiProperty propertyInfo = this.getProperty(apiId, propertyName);
        if (propertyInfo == null) {
            propertyInfo = new ApiProperty();
            propertyInfo.setCreateTime(new Timestamp(new Date().getTime()));
            propertyInfo.setApiId(apiId);
            propertyInfo.setPropertyValue(propertyValue);
            logger.info(apiId + " 属性不存在，新建该属性" + "\tpropertyName:" + propertyName + "\tpropertyValue:" + propertyValue);
            propertyInfo.setPropertyName(propertyName);

        } else {
            propertyInfo.setPropertyValue(propertyValue);
        }

        if (propertyName.equals("rsIds")) {
            JSONArray jsonArray = JSONArray.parseArray(propertyValue);
            List<Map<String, Object>> mapList = this.sqlQuery("select cpr.resounse_id id from customer_api_resouse_precent cpr,am_subscription am where cpr.api_id=am.API_ID and am.API_ID=?", apiId);
            List<String> list = new ArrayList<>();
            for (Map map : mapList) {
                {
                    list.add(map.get("id").toString());
                }

            }

            List<String> proid = new ArrayList<>();
                for (int i = 0; i < jsonArray.size(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    if (StringUtils.isNotEmpty(jsonObject.getString("rsId"))) {

                        String rsId = jsonObject.getString("rsId");
                        logger.info("ddid"+list.contains(rsId));
                        if (list.size() > 0 && !list.contains(rsId)) {//如果原来没有这个资源说明是新增
                            String sqlCustId = "select customer_Id id from customer_api_resouse_precent cpr,am_subscription am where cpr.api_id=am.API_ID and am.API_ID=? group by  customer_Id";

                            List<Map<String, Object>> list1 = this.queryMapsListBySql(sqlCustId, apiId);

                            if (list1 != null && list1.size() > 0) {
                                for (Map<String, Object> smap : list1) {
                                    CustomerApiResourcePrecent customerApiResourcePrecent = new CustomerApiResourcePrecent();
                                    customerApiResourcePrecent.setCustomerId(smap.get("id").toString());
                                    customerApiResourcePrecent.setApiId(apiId);

                                    customerApiResourcePrecent.setResounseId(rsId);
                                    customerApiResourcePrecent.setBeginPercent("0");
                                    customerApiResourcePrecent.setEndPercent("0");
                                    customerApiResourcePrecent.setCreatedBy("0");
                                    customerApiResourcePrecent.setPercent("0");
                                    this.saveOrUpdate(customerApiResourcePrecent);
                                }

                            }

                        }
                        proid.add(rsId);
                    }
                }

                for (String s : list) {

                    if (!proid.contains(s)) {//说明资源被删除

                        String sql = " select percent_content content,SUBSCRIPTION_ID id,APPLICATION_ID cusId from  am_subscription  where API_ID=?";

                        List<Map<String, Object>> mapList1 = this.queryMapsListBySql(sql, apiId);
                        for (Map map2 : mapList1) {
                            if (StringUtils.isNotEmpty(map2.get("content").toString())) {
                                String content = map2.get("content").toString();
                                String suid = map2.get("id").toString();
                                String cusId = map2.get("cusId").toString();
                                List parms = new ArrayList();

                                List<String> strings = JSONArray.parseArray(content, String.class);
                                if(strings.contains(s)){
                                    logger.info("被删除id"+s);
                                    throw new Exception("只有资源调用百分比为零时才能删除");
                                }




                                String s1 = JSONArray.toJSONString(strings);
                                parms.add(s1);
                                parms.add(suid);
                                String usql = "update am_subscription  set percent_content=? where SUBSCRIPTION_ID=? ";
                                jdbcTemplate.update(usql, parms.toArray());
                                redisUtil.set(cusId + ":" + apiId, jsonArray.toJSONString());

                            }
                        }

                        String upsql = "delete from customer_api_resouse_precent  where resounse_id='"+s+"' and  api_id='"+apiId+"'";
                        logger.info("被删除id"+upsql);
                        jdbcTemplate.update(upsql);
                    }
                }

        }
        this.saveOrUpdate(propertyInfo);
    }

    public ApiProperty getProperty(String apiId, String propertyName) {
        ApiProperty cp = null;
        String hql = "from ApiProperty m where m.apiId=? and m.propertyName=?";
        List<ApiProperty> list = this.find(hql, apiId, propertyName);
        if (list.size() > 0)
            cp = (ApiProperty) list.get(0);
        return cp;
    }

    public List<ApiProperty> getPropertyAll(String propertyName) {
        ApiProperty cp = null;
        String hql = "from ApiProperty m where m.propertyName=?";
        List<ApiProperty> list = this.find(hql, propertyName);
        return list;
    }

}
