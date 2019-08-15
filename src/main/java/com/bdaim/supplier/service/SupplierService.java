package com.bdaim.supplier.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.rbac.dto.Page;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.supplier.dto.SupplierDTO;
import com.bdaim.supplier.entity.SupplierEntity;
import com.bdaim.supplier.entity.SupplierPropertyEntity;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author duanliying
 * @date 2019/2/28
 * @description
 */
@Service("supplierService")
@Transactional
public class SupplierService {
    public static final Logger log = Logger.getLogger(SupplierService.class);
    @Resource
    private SupplierDao supplierDao;
    @Resource
    private SourceDao sourceDao;
    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private TransactionService transactionService;

    public List<Map<String, Object>> listAllSupplier() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> data;
        List<SupplierEntity> list;
        for (ResourceEnum v : ResourceEnum.values()) {
            //根据type查询所有供应商
            list = supplierDao.listAllSupplierByResourceType(v.getType());
            data = handleSupplierResource(v.getType(), list);
            if (data != null) {
                result.add(data);
            }
        }
        return result;
    }

    public Page listSupplierByPage(PageParam page, String supplierId, String supplierName, String person, String phone,
                                   String serviceType) throws Exception {
        StringBuffer sql = new StringBuffer("SELECT s.create_time createTime,s.supplier_id supplierId, NAME supplierName, contact_person person, contact_phone phone,contact_position position, s. STATUS status, GROUP_CONCAT( DISTINCT r.type_code) resourceType ,GROUP_CONCAT(DISTINCT r.resname) resname, ");
        sql.append("( SELECT property_value FROM t_supplier_property WHERE property_name = 'priority' AND supplier_id = s.supplier_id ) AS priority");
        sql.append(" FROM t_supplier s LEFT JOIN t_market_resource r ON s.supplier_id = r.supplier_id ");
        sql.append("WHERE 1=1 and s.`status` = 1 AND r.`status` = 1");
        if (StringUtil.isNotEmpty(serviceType)) {
            sql.append(" and s.supplier_id IN (SELECT supplier_id FROM t_market_resource WHERE type_code = " + serviceType + " GROUP BY supplier_id)");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            sql.append(" and s.supplier_id=").append(supplierId);
        }
        if (StringUtil.isNotEmpty(supplierName)) {
            sql.append(" and s.name like '%").append(supplierName).append("%'");
        }
        if (StringUtil.isNotEmpty(person)) {
            sql.append(" and s.contact_person = '").append(person).append("'");
        }
        if (StringUtil.isNotEmpty(phone)) {
            sql.append(" and s.contact_phone ='").append(phone).append("'");
        }
        sql.append(" GROUP BY s.supplier_id");
        sql.append(" ORDER BY priority is null, priority, s.create_time DESC");
        Page supplierPage = supplierDao.sqlPageQuery(sql.toString(), page.getPageNum(), page.getPageSize());

        return supplierPage;

    }


    /**
     * 修改供应商状态
     */
    public void supplierStatus(SupplierDTO supplierDTO) throws Exception {
        int supplierId = supplierDTO.getSupplierId();
        int status = supplierDTO.getStatus();

        StringBuilder cancelDefault = new StringBuilder("update t_supplier set status=? where supplier_id=?");
        supplierDao.executeUpdateSQL(cancelDefault.toString(), status, supplierId);

    }

    /**
     * 编辑供应商
     */
    public void updatesupplier(SupplierDTO supplierDTO) throws Exception {
        if (null != supplierDTO.getSupplierId()) {
            //修改操作,查询出供应商信息修改
            SupplierEntity supplierEntity = supplierDao.getSupplierList(supplierDTO.getSupplierId());
            if (supplierEntity != null) {
                supplierEntity.setName(supplierDTO.getName());
                supplierEntity.setContactPhone(supplierDTO.getPhone());
                supplierEntity.setContactPerson(supplierDTO.getPerson());
                supplierEntity.setContactPosition(supplierDTO.getPosition());
            }
            sourceDao.saveOrUpdate(supplierEntity);
        } else {
            //新增供应商操作
            SupplierEntity supplierEntity = new SupplierEntity();
            supplierEntity.setStatus(1);
            supplierEntity.setName(supplierDTO.getName());
            supplierEntity.setContactPhone(supplierDTO.getPhone());
            supplierEntity.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            supplierEntity.setContactPerson(supplierDTO.getPerson());
            supplierEntity.setContactPosition(supplierDTO.getPosition());
            sourceDao.saveOrUpdate(supplierEntity);
        }
    }

    /**
     * 设置供应商价格
     */
    public void updatePrice(SupplierDTO supplierDTO) throws Exception {
        if (supplierDTO == null) {
            throw new RuntimeException("supplierPrice参数异常");
        }

        SupplierEntity supplierEntity = supplierDao.get(supplierDTO.getSupplierId());
        if (supplierEntity == null) {
            throw new Exception("供应商 " + supplierDTO.getName() + " 不存在");
        }

        //获取供应商所有可用的资源
        List<MarketResourceLogDTO> labelResourceList = supplierDao.listMarketResourceBySupplierId(String.valueOf(supplierDTO.getSupplierId()));
        List<Integer> resourceIdList = null;
        if (labelResourceList != null && labelResourceList.size() > 0) {
            resourceIdList = new LinkedList<>();
            for (MarketResourceLogDTO marketResourceLogDTO : labelResourceList) {
                resourceIdList.add(marketResourceLogDTO.getResourceId());
            }
        }

        if (StringUtil.isNotEmpty(supplierDTO.getConfig())) {
            JSONArray resouArray = JSON.parseArray(supplierDTO.getConfig());
            handleResourceList(resouArray, resourceIdList);
        }
        //关联资源信息保存
        if (StringUtil.isNotEmpty(supplierDTO.getRelationResource())) {
            log.info("供应商id是：" + supplierDTO.getSupplierId() + "关联的资源是：" + supplierDTO.getRelationResource());
            //根据供应商信息查询关联信息
            SupplierPropertyEntity SupplierProperty = supplierDao.getSupplierProperty(String.valueOf(supplierDTO.getSupplierId()), "express_resource");
            if (SupplierProperty != null) {
                SupplierProperty.setPropertyValue(supplierDTO.getRelationResource());
            } else {
                SupplierProperty = new SupplierPropertyEntity();
                SupplierProperty.setSupplierId(String.valueOf(supplierDTO.getSupplierId()));
                SupplierProperty.setPropertyName("express_resource");
                SupplierProperty.setPropertyValue(supplierDTO.getRelationResource());
                SupplierProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            }
            supplierDao.saveOrUpdate(SupplierProperty);
        }
    }


    /**
     * @param resourceArray 资源配置
     */
    private void handleResourceList(JSONArray resourceArray, List<Integer> resourceIdList) {
        ResourcePropertyEntity resourcePropertyEntity;
        //保存最新的价格设置
        for (int index = 0; index < resourceArray.size(); index++) {
            String resourceStr = resourceArray.getString(index);
            JSONObject resource = JSON.parseObject(resourceStr);
            if (resource.get("resourceId") != null) {
                Integer marketResourceId = resource.getInteger("resourceId");
                resourcePropertyEntity = new ResourcePropertyEntity(marketResourceId, "price_config", resource.toJSONString(), new Timestamp(System.currentTimeMillis()));
                marketResourceDao.saveOrUpdate(resourcePropertyEntity);
                if (resourceIdList != null && resourceIdList.contains(marketResourceId)) {
                    resourceIdList.remove(marketResourceId);
                }
            }
        }
        //删除以前的设置
        if (resourceIdList != null && resourceIdList.size() > 0) {
            String resourceIds = StringUtils.join(resourceIdList.toArray(), ",");
            String sql = "delete from t_market_resource_property where resource_id in (" + resourceIds + ") and property_name='price_config'";
            marketResourceDao.executeUpdateSQLV1(sql);
        }
    }

    /**
     * 供应商信息查询+定价回显
     *
     * @author:duanliying
     * @date: 2019/3/28 11:49
     */
    public JSONObject searchSupplierPrice(String supplierId) {
        //查询供应商拥有的资源信息
        SupplierEntity supplierEntity = supplierDao.get(Integer.valueOf(supplierId));
        if (supplierEntity == null) return null;
        String sql = "SELECT type_code type,resource_id resourceId,resname name FROM t_market_resource where status = 1 and supplier_id = " + supplierId;
        List<Map<String, Object>> resourceList = supplierDao.sqlQuery(sql);
        if (resourceList == null || resourceList.isEmpty()) return null;
        JSONObject data = new JSONObject();

        JSONArray smsArray = new JSONArray();
        JSONArray idcardArray = new JSONArray();
        JSONArray callArray = new JSONArray();
        JSONArray imeiArray = new JSONArray();
        JSONArray macArray = new JSONArray();
        JSONArray addressArray = new JSONArray();
        JSONArray expressArray = new JSONArray();
        for (Map<String, Object> resource : resourceList) {
            Integer type = Integer.valueOf(resource.get("type").toString());
            String resourceId = String.valueOf(resource.get("resourceId"));
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("resourceId", resourceId);
            jsonObject.put("name", resource.get("name"));
            jsonObject.put("resourceType", resource.get("type"));

            JSONObject priceConfig = selectResourcePriceConfig(resourceId.toString());
            if (ResourceEnum.IDCARD.getType() == type) {
                setIDcardPriceData(priceConfig, jsonObject, "idCardPrice");
                idcardArray.add(jsonObject);
            } else if (ResourceEnum.SMS.getType() == type) {
                setSMSPriceConfig(priceConfig, jsonObject);
                smsArray.add(jsonObject);
            } else if (ResourceEnum.CALL.getType() == type) {
                setCallPriceData(priceConfig, jsonObject);
                callArray.add(jsonObject);
            } else if (ResourceEnum.IMEI.getType() == type) {
                setIDcardPriceData(priceConfig, jsonObject, "imeiPrice");
                imeiArray.add(jsonObject);
            } else if (ResourceEnum.MAC.getType() == type) {
                setIDcardPriceData(priceConfig, jsonObject, "macPrice");
                macArray.add(jsonObject);
            } else if (ResourceEnum.ADDRESS.getType() == type) {
                setAddressPriceData(priceConfig, jsonObject);
                addressArray.add(jsonObject);
            } else if (ResourceEnum.EXPRESS.getType() == type) {
                setExpressPriceData(priceConfig, jsonObject);
                expressArray.add(jsonObject);
            }
        }
        data.put("sms", smsArray);
        data.put("idCard", idcardArray);
        data.put("imei", imeiArray);
        data.put("call", callArray);
        data.put("mac", macArray);
        data.put("address", addressArray);
        data.put("express", expressArray);
        data.put("supplierId", supplierId);
        data.put("name", supplierEntity.getName());
        data.put("createTime", supplierEntity.getCreateTime());
        data.put("person", supplierEntity.getContactPosition());
        data.put("phone", supplierEntity.getContactPhone());
        data.put("position", supplierEntity.getContactPosition());
        data.put("status", supplierEntity.getStatus());
        return data;
    }

    /**
     * 设置身份证修复价格、mac修复、imei修复
     *
     * @param priceConfig
     * @param jsonObject
     */
    private void setIDcardPriceData(JSONObject priceConfig, JSONObject jsonObject, String fixPrice) {
        if (priceConfig != null) {
            if (priceConfig.containsKey("billingMode")) {
                jsonObject.put("billingMode", priceConfig.getString("billingMode"));
            }
            if (priceConfig.containsKey("ifPartIndustry")) {
                jsonObject.put("ifPartIndustry", priceConfig.getString("ifPartIndustry"));
            }
            if (priceConfig.containsKey(fixPrice)) {
                jsonObject.put(fixPrice, priceConfig.getString(fixPrice));
            }
            String industry = "";
            if (priceConfig.containsKey("fixPriceBank")) {
                jsonObject.put("fixPriceBank", priceConfig.getString("fixPriceBank"));
                industry += "bank";
            }
            if (priceConfig.containsKey("fixPriceInsurance")) {
                jsonObject.put("fixPriceInsurance", priceConfig.getString("fixPriceInsurance"));
                industry += ",insurance";
            }
            if (priceConfig.containsKey("fixPriceCourt")) {
                jsonObject.put("fixPriceCourt", priceConfig.getString("fixPriceCourt"));
                industry += ",court";
            }
            if (priceConfig.containsKey("fixPriceOnline")) {
                jsonObject.put("fixPriceOnline", priceConfig.getString("fixPriceOnline"));
                industry += ",online";
            }
            if (priceConfig.containsKey("fixPriceRest")) {
                jsonObject.put("fixPriceRest", priceConfig.getString("fixPriceRest"));
                industry += ",rest";
            }
            jsonObject.put("industryType", industry);
        }
    }

    /**
     * 设置呼叫线路修复价格
     *
     * @param priceConfig
     * @param jsonObject
     */
    private void setCallPriceData(JSONObject priceConfig, JSONObject jsonObject) {
        if (priceConfig != null) {
            if (priceConfig.containsKey("lineType")) {
                jsonObject.put("lineType", priceConfig.getString("lineType"));
            }
            if (priceConfig.containsKey("seatPrice")) {
                jsonObject.put("seatPrice", priceConfig.getString("seatPrice"));
            }
            if (priceConfig.containsKey("ifHalfMonth")) {
                jsonObject.put("ifHalfMonth", priceConfig.getString("ifHalfMonth"));
            }
            if (priceConfig.containsKey("seatMinute")) {
                jsonObject.put("seatMinute", priceConfig.getString("seatMinute"));
            }
            if (priceConfig.containsKey("callPrice")) {
                jsonObject.put("callPrice", priceConfig.getString("callPrice"));
            }
            if (priceConfig.containsKey("apparentPrice")) {
                jsonObject.put("apparentPrice", priceConfig.getString("apparentPrice"));
            }
        }
    }

    /**
     * 发短信费用设置
     *
     * @param priceConfig
     * @param jsonObject
     */
    private void setSMSPriceConfig(JSONObject priceConfig, JSONObject jsonObject) {
        if (priceConfig == null) return;
        if (priceConfig.containsKey("billingMode")) {
            jsonObject.put("billingMode", priceConfig.getString("billingMode"));
        }
        if (priceConfig.containsKey("smsPrice")) {
            jsonObject.put("smsPrice", priceConfig.getString("smsPrice"));
        }
    }

    /**
     * 设置地址修复价格
     *
     * @param priceConfig
     * @param jsonObject
     */
    private void setAddressPriceData(JSONObject priceConfig, JSONObject jsonObject) {
        if (priceConfig == null) return;
        if (priceConfig.containsKey("billingMode")) {
            jsonObject.put("billingMode", priceConfig.getString("billingMode"));
        }
        if (priceConfig.containsKey("addressPrice")) {
            jsonObject.put("addressPrice", priceConfig.getString("addressPrice"));
        }
    }

    /**
     * 设置快递价格
     *
     * @param priceConfig
     * @param jsonObject
     */
    private void setExpressPriceData(JSONObject priceConfig, JSONObject jsonObject) {
        if (priceConfig == null) return;
        //是否区分地域  1 区分  2 不区分
        if (priceConfig.containsKey("districtMode")) {
            jsonObject.put("districtMode", priceConfig.getString("districtMode"));
        }
        if (priceConfig.containsKey("districtNum")) {
            jsonObject.put("districtNum", priceConfig.getString("districtNum"));
        }
        if (priceConfig.containsKey("express")) {
            jsonObject.put("express", priceConfig.getString("express"));
        }
        if (priceConfig.containsKey("place")) {
            jsonObject.put("place", priceConfig.getString("place"));
        }
    }


    private JSONObject selectResourcePriceConfig(String resourceId) {

        String rsql = " select * from t_market_resource_property where resource_id=" + resourceId + " and property_name='price_config' ";
        List<Map<String, Object>> list = supplierDao.sqlQuery(rsql);
        if (list.size() > 0 && StringUtil.isNotEmpty(String.valueOf(list.get(0).get("property_value")))) {
            String priceConfig = (String) list.get(0).get("property_value");
            JSONObject json = JSON.parseObject(priceConfig);
            return json;
        }
        return null;

    }

    /**
     * 供应商调账
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/1 19:03
     */
    public Boolean changeSupplierBalance(CustomerBillQueryParam param) {
        //dealType=9  供应商调账
        String supplierId = param.getSupplierId();
        double amount = param.getAmount();
        String path = param.getPath();
        String remark = param.getRemark();
        boolean deductionsStatus = false;
        BigDecimal moneySale;
        try {
            moneySale = new BigDecimal(amount * 100);
            moneySale = moneySale.multiply(new BigDecimal(1));
            //调账 正数充值  负数扣减  sourceId
            //param供应商的是2  对应的是联通
            deductionsStatus = sourceDao.accountSupplierRecharge(supplierId, moneySale);
            log.info("供应商调账状态" + deductionsStatus);
        } catch (Exception e) {
            log.error(" 供应商调账失败,", e);
            throw new RuntimeException(" 供应商调账失败");
        }
        // 扣款成功
        if (deductionsStatus) {
            // 保存交易记录
            try {
                //支付方式 1.余额 2.第三方 3.线下 4.包月分钟
                int payMode = 1;
                if (param.getPayMode() != null) {
                    payMode = param.getPayMode();
                }
                long userId = param.getUserId();
                // 保存交易记录
                transactionService.saveTransactionLog(supplierId, TransactionEnum.SUPPLIER_ADJUSTMENT.getType(), 0, payMode, supplierId, remark, userId, path, null, moneySale.intValue(), null);
            } catch (Exception e) {
                log.error(" 保存交易记录失败,", e);
            }
        }
        return deductionsStatus;
    }

    /**
     * @description 根据供应商查询企业配置信息
     * @author:duanliying
     * @method
     * @date: 2019/2/28 15:58
     */
    private Map<String, Object> handleSupplierResource(int type, List<SupplierEntity> list) {
        log.info("根据供应商和type查询供应商信息" + "type" + type + "供应商集合是：" + String.valueOf(list));
        Map<String, Object> data = null;
        if (list.size() > 0) {
            data = new HashMap<>();
            data.put("type", type);
            List<Map<String, Object>> supplierList = new ArrayList<>();
            Map<String, Object> supplierData;
            List<MarketResourceDTO> marketResourceList;
            for (SupplierEntity m : list) {
                supplierData = new HashMap<>();
                supplierData.put("supplierName", m.getName());
                supplierData.put("supplierId", m.getSupplierId());
                marketResourceList = listMarketResourceBySupplierIdAndType(String.valueOf(m.getSupplierId()), type);
                supplierData.put("resourceList", marketResourceList);
                supplierList.add(supplierData);
            }
            data.put("supplierList", supplierList);
        }
        log.info("返回供应商信息结果是：" + String.valueOf(data));
        return data;
    }

    /**
     * 查询供应商下的所有营销资源列表
     *
     * @param supplierId
     * @param type
     * @return
     */
    public List<MarketResourceDTO> listMarketResourceBySupplierIdAndType(String supplierId, int type) {
        String hql = "from MarketResourceEntity m where m.supplierId=? and m.typeCode=? ORDER BY create_time ASC ";
        List<MarketResourceEntity> list = supplierDao.find(hql, supplierId, type);
        List<MarketResourceDTO> result = new ArrayList<>();
        if (list.size() > 0) {
            MarketResourceDTO marketResourceDTO;
            ResourcePropertyEntity property;
            for (int i = 0; i < list.size(); i++) {
                marketResourceDTO = new MarketResourceDTO(list.get(i));
                if (marketResourceDTO != null && marketResourceDTO.getResourceId() != null) {
                    property = sourceDao.getResourceProperty(String.valueOf(marketResourceDTO.getResourceId()), "price_config");
                    marketResourceDTO.setResourceProperty(property != null ? property.getPropertyValue() : "");
                }
                result.add(marketResourceDTO);
            }
        }
        return result;
    }

    /**
     * 设置服务优先级
     *
     * @param
     */
    public void setSupplierPriority(List<Map<String, Object>> list) {
        for (int i = 0; i < list.size(); i++) {
            String supplierId = String.valueOf(list.get(i).get("supplierId"));
            String priority = String.valueOf(list.get(i).get("priority"));
            log.info("供应商id" + supplierId + "优先级是：" + priority);
            SupplierPropertyEntity supplierProperty = supplierDao.getSupplierProperty(supplierId, "priority");
            if (supplierProperty != null) {
                supplierProperty.setPropertyValue(priority);
            } else {
                supplierProperty = new SupplierPropertyEntity();
                supplierProperty.setSupplierId(supplierId);
                supplierProperty.setPropertyName("priority");
                supplierProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                supplierProperty.setPropertyValue(priority);
            }
            supplierDao.saveOrUpdate(supplierProperty);
        }
    }

    /**
     * 根据供应商id查询服务资源
     *
     * @param supplierId
     */
    public List<MarketResourceLogDTO> getSupResourceBySupplierId(String supplierId) {
        List<MarketResourceLogDTO> supplierResources = supplierDao.listMarketResourceBySupplierId(supplierId);
        if (supplierResources.size() > 0) {
            for (int i = 0; i < supplierResources.size(); i++) {
                if (supplierResources.get(i).getTypeCode() > 0) {
                    log.info("资源类型是：" + supplierResources.get(i).getTypeCode());
                    String name = ResourceEnum.getName(supplierResources.get(i).getTypeCode());
                    supplierResources.get(i).setResname(name);
                }
            }
        }
        return supplierResources;
    }
}

