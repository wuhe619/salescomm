package com.bdaim.supplier.service;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.TransactionEnum;
import com.bdaim.bill.dao.BillDao;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.bill.dto.TransactionTypeEnum;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.callcenter.dto.CustomCallConfigDTO;
import com.bdaim.common.dto.Deposit;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.MarketTypeEnum;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.user.service.UserGroupService;
import com.bdaim.customgroup.dto.CustomerGrpOrdParam;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.industry.dto.MarketResourceTypeEnum;
import com.bdaim.label.dao.IndustryPoolDao;
import com.bdaim.label.entity.IndustryPool;
import com.bdaim.order.service.OrderService;
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
import com.bdaim.util.DateUtil;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author duanliying
 * @date 2019/2/28
 * @description
 */
@Service("supplierService")
@Transactional
public class SupplierService {
    public static final Logger log = LoggerFactory.getLogger(SupplierService.class);
    @Resource
    private SupplierDao supplierDao;
    @Resource
    private SourceDao sourceDao;
    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private TransactionService transactionService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    public List<Map<String, Object>> listNoloseAllSupplier() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> data;
        List<SupplierEntity> list;
        for (ResourceEnum v : ResourceEnum.values()) {
            //根据type查询所有供应商
            list = supplierDao.listAllSupplierByResourceType(v.getType());
            data = handleNoloseSupplierResource(v.getType(), list);
            if (data != null) {
                result.add(data);
            }
        }
        return result;
    }

    public Page listSupplierByPage(PageParam page, String supplierId, String supplierName, String person, String phone,
                                   String serviceType) throws Exception {
        StringBuffer sql = new StringBuffer("SELECT s.create_time createTime,s.supplier_id supplierId, NAME supplierName, contact_person person, contact_phone phone,contact_position position, s.STATUS status ,");
        sql.append("( SELECT GROUP_CONCAT(DISTINCT type_code) FROM t_market_resource WHERE supplier_id = s.supplier_id AND s.`status` = 1 ) resourceType,");
        sql.append("( SELECT property_value FROM t_supplier_property WHERE property_name = 'priority' AND supplier_id = s.supplier_id ) AS priority, ");
        sql.append("( SELECT GROUP_CONCAT(DISTINCT r.resname) FROM t_market_resource r WHERE supplier_id = s.supplier_id AND s.`status` = 1 ) resname ");
        sql.append(" FROM t_supplier s WHERE 1 = 1 ");
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
        sql.append(" ORDER BY priority is null, priority, FIND_IN_SET(1,resourceType) desc, s.create_time DESC");
        Page supplierPage = supplierDao.sqlPageQuery(sql.toString(), page.getPageNum(), page.getPageSize());
        if (supplierPage != null && supplierPage.getData().size() > 0) {
            List<Map<String, Object>> list = supplierPage.getData();
            for (int i = 0; i < list.size(); i++) {
                String id = String.valueOf(list.get(i).get("supplierId"));
                SupplierPropertyEntity remainAmount = supplierDao.getSupplierProperty(id, "remain_amount");
                if (remainAmount != null) {
                    list.get(i).put("remainAmount", NumberConvertUtil.transformtionElement(remainAmount.getPropertyValue()));
                    log.info("供应商余额是：" + NumberConvertUtil.transformtionElement(remainAmount.getPropertyValue()));
                } else {
                    list.get(i).put("remainAmount", 0);

                }
            }
        }
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
            SupplierPropertyEntity SupplierPropertyEntity = supplierDao.getSupplierProperty(String.valueOf(supplierDTO.getSupplierId()), "express_resource");
            if (SupplierPropertyEntity != null) {
                SupplierPropertyEntity.setPropertyValue(supplierDTO.getRelationResource());
            } else {
                SupplierPropertyEntity = new SupplierPropertyEntity();
                SupplierPropertyEntity.setSupplierId(String.valueOf(supplierDTO.getSupplierId()));
                SupplierPropertyEntity.setPropertyName("express_resource");
                SupplierPropertyEntity.setPropertyValue(supplierDTO.getRelationResource());
                SupplierPropertyEntity.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            }
            supplierDao.saveOrUpdate(SupplierPropertyEntity);
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
            marketResourceDao.executeUpdateSQL(sql);
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
        SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        data.put("createTime", format.format(supplierEntity.getCreateTime()));
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
        if (priceConfig.containsKey("price")) {
            jsonObject.put("price", priceConfig.getString("price"));
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
    private Map<String, Object> handleNoloseSupplierResource(int type, List<SupplierEntity> list) {
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

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final static DateTimeFormatter DATE_TIME_FORMATTER_SSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

    @Resource
    private BillDao billDao;
    @Resource
    private IndustryPoolDao industryPoolDao;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private OrderService orderService;
    @Resource
    private UserGroupService userGroupService;
    @Resource
    private CustomGroupService customGroupService;


    public int saveSupplier(SupplierDTO supplierDTO) {
        if (supplierDTO == null) {
            throw new RuntimeException("supplierDTO参数异常");
        }
        SupplierEntity supplierDO = new SupplierEntity();
        supplierDO.setName(supplierDTO.getName());
        supplierDO.setSettlementType(supplierDTO.getSettlementType());
        supplierDO.setContactPerson(supplierDTO.getContactPerson());
        supplierDO.setContactPhone(supplierDTO.getContactPhone());
        supplierDO.setContactPosition(supplierDTO.getContactPosition());
        supplierDO.setStatus(1);
        supplierDO.setCreateTime(new Timestamp(System.currentTimeMillis()));
        int supplierId = (int) supplierDao.saveReturnPk(supplierDO);
        if (supplierId > 0) {
            // 处理授信额度supplierDao
            if (supplierDTO.getSettlementType() != null && 2 == supplierDTO.getSettlementType()) {
                SupplierPropertyEntity creditAmountProperty = new SupplierPropertyEntity(String.valueOf(supplierId), "creditAmount", supplierDTO.getCreditAmount(), new Timestamp(System.currentTimeMillis()));
                supplierDao.saveOrUpdate(creditAmountProperty);
            }
            MarketResourceEntity marketResource;
            ResourcePropertyEntity marketResourceProperty;
            int marketResourceId;
            JSONArray jsonArray;
            // 处理标签资源
            if (StringUtil.isNotEmpty(supplierDTO.getDataConfig())) {
                jsonArray = JSON.parseArray(supplierDTO.getDataConfig());
                for (int index = 0; index < jsonArray.size(); index++) {
                    if (jsonArray.getJSONObject(index) != null) {
                        marketResource = new MarketResourceEntity();
                        marketResource.setSupplierId(String.valueOf(supplierId));
                        marketResource.setResname(jsonArray.getJSONObject(index).getString("name"));
                        marketResource.setTypeCode(MarketResourceTypeEnum.LABEL.getType());
                        marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        marketResource.setStatus(1);
                        marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);

                        marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", String.valueOf(jsonArray.getJSONObject(index)), new Timestamp(System.currentTimeMillis()));
                        marketResourceDao.saveOrUpdate(marketResourceProperty);
                    }

                }
            }
            // 处理通话资源
            if (StringUtil.isNotEmpty(supplierDTO.getCallConfig())) {
                jsonArray = JSON.parseArray(supplierDTO.getCallConfig());
                JSONObject jsonObject;
                for (int index = 0; index < jsonArray.size(); index++) {
                    if (jsonArray.getJSONObject(index) != null) {
                        jsonObject = jsonArray.getJSONObject(index);
                        marketResource = new MarketResourceEntity();
                        marketResource.setSupplierId(String.valueOf(supplierId));
                        marketResource.setResname(jsonArray.getJSONObject(index).getString("name"));
                        marketResource.setTypeCode(MarketResourceTypeEnum.CALL.getType());
                        marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        marketResource.setStatus(1);
                        marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);
                        jsonObject.put("resourceId", marketResourceId);
                        marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", String.valueOf(jsonArray.getJSONObject(index)), new Timestamp(System.currentTimeMillis()));
                        marketResourceDao.saveOrUpdate(marketResourceProperty);
                        // 处理坐席和外显资源
                        jsonObject.put("callResourceId", marketResourceId);
                        marketResource = new MarketResourceEntity();
                        marketResource.setSupplierId(String.valueOf(supplierId));
                        marketResource.setResname(jsonArray.getJSONObject(index).getString("name"));
                        marketResource.setTypeCode(MarketResourceTypeEnum.SEATS.getType());
                        marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        marketResource.setStatus(1);
                        marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);

                        jsonObject.put("resourceId", marketResourceId);
                        marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", String.valueOf(jsonObject), new Timestamp(System.currentTimeMillis()));
                        marketResourceDao.saveOrUpdate(marketResourceProperty);

                        marketResource = new MarketResourceEntity();
                        marketResource.setSupplierId(String.valueOf(supplierId));
                        marketResource.setResname(jsonArray.getJSONObject(index).getString("name"));
                        marketResource.setTypeCode(MarketResourceTypeEnum.APPARENT_NUMBER.getType());
                        marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        marketResource.setStatus(1);
                        marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);
                        jsonObject.put("resourceId", marketResourceId);
                        marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", String.valueOf(jsonObject), new Timestamp(System.currentTimeMillis()));
                        marketResourceDao.saveOrUpdate(marketResourceProperty);
                    }
                }
            }
            // 处理短信资源
            if (StringUtil.isNotEmpty(supplierDTO.getSmsConfig())) {
                jsonArray = JSON.parseArray(supplierDTO.getSmsConfig());
                for (int index = 0; index < jsonArray.size(); index++) {
                    if (jsonArray.getJSONObject(index) != null) {
                        marketResource = new MarketResourceEntity();
                        marketResource.setSupplierId(String.valueOf(supplierId));
                        marketResource.setResname(jsonArray.getJSONObject(index).getString("name"));
                        marketResource.setTypeCode(MarketResourceTypeEnum.SMS.getType());
                        marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                        marketResource.setStatus(1);
                        marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);

                        marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", String.valueOf(jsonArray.getJSONObject(index)), new Timestamp(System.currentTimeMillis()));
                        marketResourceDao.saveOrUpdate(marketResourceProperty);
                    }
                }
            }
            // 处理资源
            if (supplierDTO.getResourceConfig() != null) {
                Iterator keys = supplierDTO.getResourceConfig().keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (key.endsWith("_config")) {
                        jsonArray = JSON.parseArray(String.valueOf(supplierDTO.getResourceConfig().get(key)));
                        if (jsonArray == null || jsonArray.size() == 0) {
                            continue;
                        }
                        for (int index = 0; index < jsonArray.size(); index++) {
                            if (jsonArray.getJSONObject(index) != null) {
                                marketResource = new MarketResourceEntity();
                                marketResource.setSupplierId(String.valueOf(supplierId));
                                marketResource.setResname(jsonArray.getJSONObject(index).getString("name"));
                                marketResource.setTypeCode(jsonArray.getJSONObject(index).getInteger("busiType"));
                                marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                marketResource.setStatus(1);
                                marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);

                                jsonArray.getJSONObject(index).put("key", key);
                                marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", String.valueOf(jsonArray.getJSONObject(index)), new Timestamp(System.currentTimeMillis()));
                                marketResourceDao.saveOrUpdate(marketResourceProperty);
                            }
                        }
                    }
                }
            }
            return 1;
        } else {
            throw new RuntimeException("供应商保存异常");
        }
    }

    /**
     * 单独保存资源
     *
     * @param supplierDTO
     * @return
     */
    public int saveResConfig(SupplierDTO supplierDTO) {
        // 处理资源
        if (supplierDTO.getResourceConfig() != null) {
            Iterator keys = supplierDTO.getResourceConfig().keySet().iterator();
            JSONArray jsonArray;
            while (keys.hasNext()) {
                String key = (String) keys.next();
                if (key.endsWith("_config")) {
                    jsonArray = JSON.parseArray(String.valueOf(supplierDTO.getResourceConfig().get(key)));
                    if (jsonArray == null || jsonArray.size() == 0) {
                        continue;
                    }
                    MarketResourceEntity marketResource;
                    int marketResourceId;
                    ResourcePropertyEntity marketResourceProperty;
                    for (int index = 0; index < jsonArray.size(); index++) {
                        if (jsonArray.getJSONObject(index) != null) {
                            marketResource = new MarketResourceEntity();
                            marketResourceId = jsonArray.getJSONObject(index).getIntValue("resourceId");
                            if (marketResourceId > 0) {
                                marketResource = marketResourceDao.get(marketResourceId);
                            }
                            marketResource.setSupplierId(String.valueOf(supplierDTO.getSupplierId()));
                            marketResource.setResname(jsonArray.getJSONObject(index).getString("name"));
                            marketResource.setTypeCode(jsonArray.getJSONObject(index).getInteger("busiType"));
                            marketResource.setStatus(jsonArray.getJSONObject(index).getInteger("status"));
                            if (marketResourceId == 0) {
                                marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                                marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);
                            } else {
                                marketResourceDao.saveOrUpdate(marketResource);
                            }
                            jsonArray.getJSONObject(index).put("key", key);
                            marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", String.valueOf(jsonArray.getJSONObject(index)), new Timestamp(System.currentTimeMillis()));
                            marketResourceDao.saveOrUpdate(marketResourceProperty);
                        }
                    }
                }
            }
        }
        return 1;
    }

    private void handleResourceList(JSONArray jsonArray, String supplierId, int type, Set<Integer> dbResourceCodes) {
        JSONObject jsonObject;
        int marketResourceId;
        ResourcePropertyEntity marketResourceProperty;
        MarketResourceEntity marketResource;
        for (int index = 0; index < jsonArray.size(); index++) {
            jsonObject = jsonArray.getJSONObject(index);
            if (jsonObject.get("resourceId") != null) {
                marketResourceId = jsonArray.getJSONObject(index).getInteger("resourceId");
                marketResourceProperty = marketResourceDao.getProperty(String.valueOf(marketResourceId), "price_config");
                if (marketResourceProperty == null) {
                    marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", jsonObject.toJSONString(), new Timestamp(System.currentTimeMillis()));
                } else {
                    marketResourceProperty.setPropertyValue(jsonObject.toJSONString());
                    marketResourceProperty.setCreateTime(new Timestamp(System.currentTimeMillis()));
                }
                marketResourceDao.saveOrUpdate(marketResourceProperty);
                dbResourceCodes.remove(marketResourceId);
                marketResource = marketResourceDao.getMarketResource(jsonObject.getInteger("resourceId"));
                if (marketResource != null) {
                    marketResource.setResname(jsonObject.getString("name"));
                    if (jsonObject.containsKey("status")) {
                        marketResource.setStatus(jsonObject.getIntValue("status"));
                    }
                    marketResourceDao.saveOrUpdate(marketResource);
                }

            } else {
                marketResource = new MarketResourceEntity();
                marketResource.setSupplierId(supplierId);
                marketResource.setResname(jsonObject.getString("name"));
                marketResource.setTypeCode(type);
                marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                if (jsonObject.containsKey("status")) {
                    marketResource.setStatus(jsonObject.getIntValue("status"));
                } else {
                    marketResource.setStatus(1);
                }
                marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);
                marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", jsonObject.toJSONString(), new Timestamp(System.currentTimeMillis()));
                marketResourceDao.saveOrUpdate(marketResourceProperty);
            }
            // 处理外显和坐席资源
            if (type == MarketResourceTypeEnum.CALL.getType()) {
                jsonObject = jsonArray.getJSONObject(index);
                jsonObject.put("callResourceId", marketResourceId);
                marketResource = new MarketResourceEntity();
                marketResource.setSupplierId(String.valueOf(supplierId));
                marketResource.setResname(jsonArray.getJSONObject(index).getString("name"));
                marketResource.setTypeCode(MarketResourceTypeEnum.SEATS.getType());
                marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                marketResource.setStatus(1);
                marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);
                jsonObject.put("resourceId", marketResourceId);
                marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", String.valueOf(jsonObject), new Timestamp(System.currentTimeMillis()));
                marketResourceDao.saveOrUpdate(marketResourceProperty);

                marketResource = new MarketResourceEntity();
                marketResource.setSupplierId(String.valueOf(supplierId));
                marketResource.setResname(jsonArray.getJSONObject(index).getString("name"));
                marketResource.setTypeCode(MarketResourceTypeEnum.APPARENT_NUMBER.getType());
                marketResource.setCreateTime(new Timestamp(System.currentTimeMillis()));
                marketResource.setStatus(1);
                marketResourceId = (int) marketResourceDao.saveReturnPk(marketResource);
                jsonObject.put("resourceId", marketResourceId);
                marketResourceProperty = new ResourcePropertyEntity(marketResourceId, "price_config", String.valueOf(jsonObject), new Timestamp(System.currentTimeMillis()));
                marketResourceDao.saveOrUpdate(marketResourceProperty);
            }
        }
    }

    public int updateSupplierPrice(SupplierDTO supplierDTO) {
        if (supplierDTO == null) {
            throw new RuntimeException("supplierDTO参数异常");
        }
        SupplierEntity supplierDO = supplierDao.get(supplierDTO.getSupplierId());
        if (supplierDO != null) {
            Set<Integer> dbResourceCode = new HashSet<>();
            List<MarketResourceDTO> labelResourceList = marketResourceDao.listMarketResourceBySupplierId(String.valueOf(supplierDO.getSupplierId()));
            for (MarketResourceDTO m : labelResourceList) {
                dbResourceCode.add(m.getResourceId());
            }

            MarketResourceEntity marketResource;
            ResourcePropertyEntity marketResourceProperty;
            long marketResourceId;
            JSONArray jsonArray;
            // 处理标签资源
            if (StringUtil.isNotEmpty(supplierDTO.getDataConfig())) {
                jsonArray = JSON.parseArray(supplierDTO.getDataConfig());
                handleResourceList(jsonArray, String.valueOf(supplierDTO.getSupplierId()), MarketResourceTypeEnum.LABEL.getType(), dbResourceCode);
            }
            // 处理通话资源
            if (StringUtil.isNotEmpty(supplierDTO.getCallConfig())) {
                jsonArray = JSON.parseArray(supplierDTO.getCallConfig());
                handleResourceList(jsonArray, String.valueOf(supplierDTO.getSupplierId()), MarketResourceTypeEnum.CALL.getType(), dbResourceCode);
            }
            // 处理短信资源
            if (StringUtil.isNotEmpty(supplierDTO.getSmsConfig())) {
                jsonArray = JSON.parseArray(supplierDTO.getSmsConfig());
                handleResourceList(jsonArray, String.valueOf(supplierDTO.getSupplierId()), MarketResourceTypeEnum.SMS.getType(), dbResourceCode);
            }
            // 处理资源
            if (supplierDTO.getResourceConfig() != null) {
                Iterator keys = supplierDTO.getResourceConfig().keySet().iterator();
                while (keys.hasNext()) {
                    String key = (String) keys.next();
                    if (key.endsWith("_config") && StringUtil.isNotEmpty(String.valueOf(supplierDTO.getResourceConfig().get(key)))) {
                        jsonArray = JSON.parseArray(String.valueOf(supplierDTO.getResourceConfig().get(key)));
                        if (jsonArray != null && jsonArray.size() > 0) {
                            for (int i = 0; i < jsonArray.size(); i++) {
                                jsonArray.getJSONObject(i).put("key", key);
                            }
                            handleResourceList(jsonArray, String.valueOf(supplierDTO.getSupplierId()), jsonArray.getJSONObject(0).getIntValue("busiType"), dbResourceCode);
                        }
                    }
                }
            }
            if (dbResourceCode.size() > 0) {
                for (Integer resourceId : dbResourceCode) {
                    marketResourceDao.updateMarketResourceStatus(resourceId, 2);
                }
            }
            return 1;
        } else {
            throw new RuntimeException("供应商保存异常");
        }
    }

    /**
     * 查询单个供应商以及资源配置详情
     *
     * @param supplierId
     * @return
     */
    public SupplierDTO selectSupplierAndResourceProperty(String supplierId) {
        if (StringUtil.isEmpty(supplierId)) {
            throw new RuntimeException("supplierId不能为空");
        }
        SupplierEntity supplierDO = supplierDao.get(NumberConvertUtil.parseInt(supplierId));
        SupplierDTO result = new SupplierDTO(supplierDO);
        if (result != null && result.getSupplierId() != null) {
            String serviceResource = "";
            // 查询通话营销资源
            List<MarketResourceDTO> voiceMarketResourceList = marketResourceDao.listMarketResourceBySupplierIdAndType(String.valueOf(result.getSupplierId()), MarketResourceTypeEnum.CALL.getType());
            if (voiceMarketResourceList.size() > 0) {
                serviceResource += MarketResourceTypeEnum.CALL.getType() + ",";
                JSONObject jsonObject;
                JSONArray jsonArray = new JSONArray();
                for (MarketResourceDTO m : voiceMarketResourceList) {
                    if (StringUtil.isNotEmpty(m.getResourceProperty())) {
                        jsonObject = JSON.parseObject(m.getResourceProperty());
                        if (!jsonObject.containsKey("call_center_config")) {
                            jsonObject.put("call_center_config", "{}");
                        }
                        jsonObject.put("resourceId", m.getResourceId());
                        jsonArray.add(jsonObject);
                    }
                }
                if (jsonArray.size() > 0) {
                    result.setCallConfig(jsonArray.toJSONString());
                }

            }
            // 查询短信营销资源
            List<MarketResourceDTO> smsMarketResourceList = marketResourceDao.listMarketResourceBySupplierIdAndType(String.valueOf(result.getSupplierId()), MarketResourceTypeEnum.SMS.getType());
            if (smsMarketResourceList.size() > 0) {
                serviceResource += MarketResourceTypeEnum.SMS.getType() + ",";
                JSONObject jsonObject;
                JSONArray jsonArray = new JSONArray();
                for (MarketResourceDTO m : smsMarketResourceList) {
                    if (StringUtil.isNotEmpty(m.getResourceProperty())) {
                        jsonObject = JSON.parseObject(m.getResourceProperty());
                        jsonObject.put("resourceId", m.getResourceId());
                        jsonArray.add(jsonObject);
                    }
                }
                if (jsonArray.size() > 0) {
                    result.setSmsConfig(jsonArray.toJSONString());
                }
            }
            // 查询标签营销资源
            List<MarketResourceDTO> labelMarketResourceList = marketResourceDao.listMarketResourceBySupplierIdAndType(String.valueOf(result.getSupplierId()), MarketResourceTypeEnum.LABEL.getType());
            if (labelMarketResourceList.size() > 0) {
                serviceResource += MarketResourceTypeEnum.LABEL.getType() + ",";
                JSONObject jsonObject;
                JSONArray jsonArray = new JSONArray();
                for (MarketResourceDTO m : labelMarketResourceList) {
                    if (StringUtil.isNotEmpty(m.getResourceProperty())) {
                        jsonObject = JSON.parseObject(m.getResourceProperty());
                        jsonObject.put("resourceId", m.getResourceId());
                        jsonArray.add(jsonObject);
                    }
                }
                if (jsonArray.size() > 0) {
                    result.setDataConfig(jsonArray.toJSONString());
                }
            }

            // 所使用的营销资源
            result.setServiceResource(serviceResource);
        }
        return result;
    }

    /**
     * 查询单个供应商以及资源配置详情
     *
     * @param supplierId
     * @return
     */
    public JSONObject selectSupplierAndResourceProperty0(String supplierId) {
        if (StringUtil.isEmpty(supplierId)) {
            throw new RuntimeException("supplierId不能为空");
        }
        JSONObject data = new JSONObject();
        SupplierEntity supplierDO = supplierDao.get(NumberConvertUtil.parseInt(supplierId));
        SupplierDTO result = new SupplierDTO(supplierDO);
        if (result != null && result.getSupplierId() != null) {
            String serviceResource = "";
            // 查询通话营销资源
            List<MarketResourceDTO> voiceMarketResourceList = marketResourceDao.listMarketResourceBySupplierIdAndType(String.valueOf(result.getSupplierId()), MarketResourceTypeEnum.CALL.getType());
            if (voiceMarketResourceList.size() > 0) {
                serviceResource += MarketResourceTypeEnum.CALL.getType() + ",";
                JSONObject jsonObject;
                JSONArray jsonArray = new JSONArray();
                for (MarketResourceDTO m : voiceMarketResourceList) {
                    if (StringUtil.isNotEmpty(m.getResourceProperty())) {
                        jsonObject = JSON.parseObject(m.getResourceProperty());
                        if (!jsonObject.containsKey("call_center_config")) {
                            jsonObject.put("call_center_config", "{}");
                        }
                        jsonObject.put("resourceId", m.getResourceId());
                        jsonArray.add(jsonObject);
                    }
                }
                if (jsonArray.size() > 0) {
                    result.setCallConfig(jsonArray.toJSONString());
                }

            }
            // 查询短信营销资源
            List<MarketResourceDTO> smsMarketResourceList = marketResourceDao.listMarketResourceBySupplierIdAndType(String.valueOf(result.getSupplierId()), MarketResourceTypeEnum.SMS.getType());
            if (smsMarketResourceList.size() > 0) {
                serviceResource += MarketResourceTypeEnum.SMS.getType() + ",";
                JSONObject jsonObject;
                JSONArray jsonArray = new JSONArray();
                for (MarketResourceDTO m : smsMarketResourceList) {
                    if (StringUtil.isNotEmpty(m.getResourceProperty())) {
                        jsonObject = JSON.parseObject(m.getResourceProperty());
                        jsonObject.put("resourceId", m.getResourceId());
                        jsonArray.add(jsonObject);
                    }
                }
                if (jsonArray.size() > 0) {
                    result.setSmsConfig(jsonArray.toJSONString());
                }
            }
            // 查询标签营销资源
            List<MarketResourceDTO> labelMarketResourceList = marketResourceDao.listMarketResourceBySupplierIdAndType(String.valueOf(result.getSupplierId()), MarketResourceTypeEnum.LABEL.getType());
            if (labelMarketResourceList.size() > 0) {
                serviceResource += MarketResourceTypeEnum.LABEL.getType() + ",";
                JSONObject jsonObject;
                JSONArray jsonArray = new JSONArray();
                for (MarketResourceDTO m : labelMarketResourceList) {
                    if (StringUtil.isNotEmpty(m.getResourceProperty())) {
                        jsonObject = JSON.parseObject(m.getResourceProperty());
                        jsonObject.put("resourceId", m.getResourceId());
                        jsonArray.add(jsonObject);
                    }
                }
                if (jsonArray.size() > 0) {
                    result.setDataConfig(jsonArray.toJSONString());
                }
            }

            List<MarketResourceDTO> list = marketResourceDao.listMarketResourceBySupplierId(String.valueOf(result.getSupplierId()));
            Set<Integer> types = new HashSet<>();
            if (list != null && list.size() > 0) {
                JSONObject jsonObject = null;
                JSONArray jsonArray = null;
                for (int i = 0; i < list.size(); i++) {
                    jsonObject = JSON.parseObject(list.get(i).getResourceProperty());
                    if (jsonObject.containsKey("key") && jsonObject.getString("key").endsWith("_config")) {
                        if (data.get(jsonObject.getString("key")) == null) {
                            jsonArray = new JSONArray();
                        } else {
                            jsonArray = JSON.parseArray(data.getString(jsonObject.getString("key")));
                        }
                        types.add(list.get(i).getTypeCode());
                        jsonObject.put("resourceId", list.get(i).getResourceId());
                        jsonArray.add(jsonObject);
                        data.put(jsonObject.getString("key"), jsonArray.toJSONString());
                    }
                }
            }
            serviceResource += StringUtils.join(types.toArray(), ",");
            // 所使用的营销资源
            result.setServiceResource(serviceResource);
        }
        data.putAll(JSON.parseObject(JSON.toJSONString(result)));
        return data;
    }

    /**
     * 查询供应商下的全部资源
     *
     * @param supplierId
     * @return
     */
    public List<MarketResourceDTO> listResourceBySupplierId(String supplierId) {
        if (StringUtil.isEmpty(supplierId)) {
            throw new RuntimeException("supplierId不能为空");
        }
        SupplierEntity supplierDO = supplierDao.get(NumberConvertUtil.parseInt(supplierId));
        SupplierDTO result = new SupplierDTO(supplierDO);
        List<MarketResourceDTO> voiceMarketResourceList = null;
        if (result != null && result.getSupplierId() != null) {
            voiceMarketResourceList = marketResourceDao.listMarketResourceBySupplierId(String.valueOf(result.getSupplierId()));
        }
        return voiceMarketResourceList;
    }

    public List<MarketResourceDTO> listResource(String type) {
        List<MarketResourceDTO> list = marketResourceDao.listMarketResource(type);
        ResourcePropertyEntity marketResourceProperty;
        JSONObject jsonObject;
        SupplierEntity supplierDO;
        List<MarketResourceDTO> result = new ArrayList<>();
        for (MarketResourceDTO m : list) {
            supplierDO = supplierDao.getSupplier(NumberConvertUtil.parseInt(m.getSupplierId()));
            if (supplierDO != null && supplierDO.getStatus() != null && 1 == supplierDO.getStatus()) {
                m.setSupplierName(supplierDO.getName());
                marketResourceProperty = marketResourceDao.getProperty(String.valueOf(m.getResourceId()), "price_config");
                if (marketResourceProperty != null) {
                    jsonObject = JSONObject.parseObject(marketResourceProperty.getPropertyValue());
                    m.setResourceProperty(jsonObject.toJSONString());
                    if (jsonObject != null) {
                        m.setChargingType(jsonObject.getInteger("type"));
                    }
                }
                result.add(m);
            }
        }
        return result;
    }

    /**
     * 根据类型分页查询资源
     *
     * @param type
     * @param pageNum
     * @param pageSize
     * @return Page
     */
    public Page pageResource(String type, int pageNum, int pageSize) {
        Page page = marketResourceDao.pageMarketResource(type, pageNum, pageSize);
        ResourcePropertyEntity marketResourceProperty;
        JSONObject jsonObject;
        SupplierEntity supplierDO;
        List<MarketResourceDTO> result = new ArrayList<>();
        MarketResourceDTO m = null;
        for (int i = 0; i < page.getData().size(); i++) {
            m = (MarketResourceDTO) page.getData().get(i);
            supplierDO = supplierDao.getSupplier(NumberConvertUtil.parseInt(m.getSupplierId()));
            if (supplierDO != null && supplierDO.getStatus() != null && 1 == supplierDO.getStatus()) {
                m.setSupplierName(supplierDO.getName());
                marketResourceProperty = marketResourceDao.getProperty(String.valueOf(m.getResourceId()), "price_config");
                if (marketResourceProperty != null) {
                    jsonObject = JSONObject.parseObject(marketResourceProperty.getPropertyValue());
                    m.setResourceProperty(jsonObject.toJSONString());
                    if (jsonObject != null) {
                        m.setChargingType(jsonObject.getInteger("type"));
                    }
                }
                result.add(m);
            }
        }
        return page;
    }

    public JSONObject listVoiceResourceByType(String type) throws Exception {
        List<MarketResourceDTO> list = listResource(type);
        JSONObject jsonObject = new JSONObject();
        List<MarketResourceDTO> call2way = new ArrayList<>();
        List<MarketResourceDTO> callCenter = new ArrayList<>();
        if (list != null && !list.isEmpty()) {
            for (MarketResourceDTO dto : list) {
                if ("1".equals(dto.getChargingType())) {//呼叫中心
                    callCenter.add(dto);
                } else if ("2".equals(dto.getChargingType())) {//双呼
                    call2way.add(dto);
                }
            }
            jsonObject.put("call2way", call2way);
            jsonObject.put("callCenter", callCenter);
        }
        return jsonObject;
    }


    public JSONObject getCustomerCallPriceConfig(String custId) {
        CustomerProperty voiceCustomerProperty = customerDao.getProperty(custId, MarketResourceTypeEnum.CALL.getPropertyName());
        JSONObject jsonObject = new JSONObject();
        List<MarketResourceDTO> call2way = new ArrayList<>();
        List<MarketResourceDTO> callCenter = new ArrayList<>();
        String config = voiceCustomerProperty.getPropertyValue();
        if (voiceCustomerProperty != null && StringUtil.isNotEmpty(config)) {
            if (config.startsWith("[")) {
                JSONArray array = JSON.parseArray(config);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    if (json.containsKey("status") && json.getInteger("status") == 1) {
                        buildCallConfig(json, callCenter, call2way);
                    }
                }
            } else if (config.startsWith("{")) {
                JSONObject json = JSON.parseObject(config);
                buildCallConfig(json, callCenter, call2way);
            }
        }
        jsonObject.put("call2way", call2way);
        jsonObject.put("callCenter", callCenter);
        return jsonObject;
    }

    /**
     * 获取客户配置的通话资源
     *
     * @param custId
     * @return
     */
    public JSONObject getCustomerCallPriceConfig0(String custId) {
        CustomerProperty voiceCustomerProperty = customerDao.getProperty(custId, MarketResourceTypeEnum.CALL.getPropertyName());
        JSONObject jsonObject = new JSONObject();
        List<MarketResourceDTO> call2way = new ArrayList<>(), unicomCall2way = new ArrayList<>();
        List<MarketResourceDTO> callCenter = new ArrayList<>();
        String config = voiceCustomerProperty.getPropertyValue();
        if (voiceCustomerProperty != null && StringUtil.isNotEmpty(config)) {
            if (config.startsWith("[")) {
                JSONArray array = JSON.parseArray(config);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    if (json.containsKey("status") && json.getInteger("status") == 1) {
                        buildCallConfig(json, callCenter, call2way, unicomCall2way);
                    }
                }
            } else if (config.startsWith("{")) {
                JSONObject json = JSON.parseObject(config);
                buildCallConfig(json, callCenter, call2way, unicomCall2way);
            }
        }
        jsonObject.put("call2way", call2way);
        jsonObject.put("callCenter", callCenter);
        jsonObject.put("unicomCall2way", unicomCall2way);
        return jsonObject;
    }

    /**
     * 获取客户呼叫配置
     *
     * @param custId
     * @param type   1-呼叫中心 2-双呼 3-机器人 为空获取全部
     * @return
     */
    public CustomCallConfigDTO getCustomerCallPriceConfig(String custId, Integer type) {
        CustomerProperty voiceCustomerProperty = customerDao.getProperty(custId, MarketResourceTypeEnum.CALL.getPropertyName());
        CustomCallConfigDTO data = new CustomCallConfigDTO();
        List<MarketResourceDTO> list = new ArrayList<>();
        List<MarketResourceDTO> call2way = new ArrayList<>(), unicomCall2way = new ArrayList<>();
        List<MarketResourceDTO> callCenter = new ArrayList<>();
        List<MarketResourceDTO> robot = new ArrayList<>();
        MarketResourceDTO dto;
        Set<Integer> resourceIds = new HashSet<>();
        if (voiceCustomerProperty != null && StringUtil.isNotEmpty(voiceCustomerProperty.getPropertyValue())) {
            String config = voiceCustomerProperty.getPropertyValue();
            if (config.startsWith("[")) {
                JSONArray array = JSON.parseArray(config);
                for (int i = 0; i < array.size(); i++) {
                    JSONObject json = array.getJSONObject(i);
                    if (resourceIds.contains(json.getInteger("resourceId"))) {
                        continue;
                    }
                    if (json.containsKey("status") && json.getInteger("status") == 1) {
                        dto = buildCallConfig(json);
                        list.add(dto);
                        resourceIds.add(dto.getResourceId());
                    }
                }
            } else if (config.startsWith("{")) {
                JSONObject json = JSON.parseObject(config);
                dto = buildCallConfig(json);
                list.add(dto);
            }
        }
        for (MarketResourceDTO s : list) {
            if (s.getChargingType() == null) {
                continue;
            }
            if (s.getChargingType().intValue() == 1) {
                callCenter.add(s);
            } else if (s.getChargingType().intValue() == 2) {
                call2way.add(s);
            } else if (s.getChargingType().intValue() == 3) {
                robot.add(s);
            } else if (s.getChargingType().intValue() == 4) {
                unicomCall2way.add(s);
            }
        }
        if (type == null) {
            data.setCallCenter(callCenter);
            data.setCall2way(call2way);
            data.setRobot(robot);
            data.setUnicomCall2way(unicomCall2way);
        } else {
            if (type == 1) {
                data.setCallCenter(callCenter);
            } else if (type == 2) {
                data.setCall2way(call2way);
            } else if (type == 3) {
                data.setRobot(robot);
            } else if (type == 4) {
                data.setUnicomCall2way(unicomCall2way);
            }
        }
        return data;
    }

    private void buildCallConfig(JSONObject json, List<MarketResourceDTO> callCenter, List<MarketResourceDTO> call2way) {
        if (json.containsKey("resourceId") && StringUtil.isNotEmpty(json.getString("resourceId"))) {
            Integer resourceId = json.getInteger("resourceId");
            MarketResourceDTO dto = new MarketResourceDTO();
            dto.setResourceId(resourceId);
            MarketResourceEntity resource = marketResourceDao.get(resourceId);
            if (resource == null) {
                return;
            }
            dto.setResname(resource.getResname());
            dto.setSupplierId(resource.getSupplierId());
            dto.setChargingType(json.getInteger("type"));
            SupplierEntity supplierDO = supplierDao.getSupplier(NumberConvertUtil.parseInt(resource.getSupplierId()));
            dto.setSupplierName(supplierDO.getName());
            if ("1".equals(json.getString("type")) || "3".equals(json.getString("type"))) {
                callCenter.add(dto);
            } else if ("2".equals(json.getString("type"))) {
                call2way.add(dto);
            }
        }
    }

    private void buildCallConfig(JSONObject json, List<MarketResourceDTO> callCenter, List<MarketResourceDTO> call2way,
                                 List<MarketResourceDTO> unicomCall2way) {
        if (json.containsKey("resourceId") && StringUtil.isNotEmpty(json.getString("resourceId"))) {
            Integer resourceId = json.getInteger("resourceId");
            MarketResourceDTO dto = new MarketResourceDTO();
            dto.setResourceId(resourceId);
            MarketResourceEntity resource = marketResourceDao.get(resourceId);
            if (resource == null) {
                return;
            }
            dto.setResname(resource.getResname());
            dto.setSupplierId(resource.getSupplierId());
            dto.setChargingType(json.getInteger("type"));
            SupplierEntity supplierDO = supplierDao.getSupplier(NumberConvertUtil.parseInt(resource.getSupplierId()));
            dto.setSupplierName(supplierDO.getName());
            if ("1".equals(json.getString("type")) || "3".equals(json.getString("type"))) {
                callCenter.add(dto);
            } else if ("2".equals(json.getString("type"))) {
                call2way.add(dto);
            } else if ("4".equals(json.getString("type"))) {
                unicomCall2way.add(dto);
            }
        }
    }

    private MarketResourceDTO buildCallConfig(JSONObject json) {
        if (json.containsKey("resourceId") && StringUtil.isNotEmpty(json.getString("resourceId"))) {
            Integer resourceId = json.getInteger("resourceId");
            MarketResourceDTO dto = new MarketResourceDTO();
            dto.setResourceId(resourceId);
            MarketResourceEntity resource = marketResourceDao.get(resourceId);
            if (resource == null) {
                return dto;
            }
            dto.setResname(resource.getResname());
            dto.setSupplierId(resource.getSupplierId());
            dto.setChargingType(json.getInteger("type"));
            // 处理呼叫中心单机和Saas模式
            if (dto.getChargingType() != null && dto.getChargingType() == 1) {
                ResourcePropertyEntity mrp = marketResourceDao.getProperty(String.valueOf(resource.getResourceId()), "price_config");
                if (mrp != null && StringUtil.isNotEmpty(mrp.getPropertyValue())) {
                    JSONObject property = JSON.parseObject(mrp.getPropertyValue());
                    dto.setCallCenterType(property.getInteger("call_center_type"));
                }
            }
            SupplierEntity supplierDO = supplierDao.getSupplier(NumberConvertUtil.parseInt(resource.getSupplierId()));
            dto.setSupplierName(supplierDO.getName());
            return dto;
        }
        return null;
    }

    /**
     * 更新供应商主信息
     *
     * @param supplierDTO
     * @return
     */
    public int updateSupplier(SupplierDTO supplierDTO) {
        if (supplierDTO == null) {
            throw new RuntimeException("supplierDTO参数异常");
        }
        SupplierEntity supplierDO = supplierDao.get(supplierDTO.getSupplierId());
        if (supplierDO == null) {
            throw new RuntimeException("更新供应商异常," + supplierDTO.getSupplierId() + "供应商不存在");
        }
        if (StringUtil.isNotEmpty(supplierDTO.getName())) {
            supplierDO.setName(supplierDTO.getName());
        }
        if (supplierDTO.getSettlementType() != null) {
            supplierDO.setSettlementType(supplierDTO.getSettlementType());
        }
        if (StringUtil.isNotEmpty(supplierDTO.getContactPerson())) {
            supplierDO.setContactPerson(supplierDTO.getContactPerson());
        }
        if (StringUtil.isNotEmpty(supplierDTO.getContactPosition())) {
            supplierDO.setContactPosition(supplierDTO.getContactPosition());
        }
        if (StringUtil.isNotEmpty(supplierDTO.getContactPhone())) {
            supplierDO.setContactPhone(supplierDTO.getContactPhone());
        }
        if (supplierDTO.getStatus() != null) {
            supplierDO.setStatus(supplierDTO.getStatus());
        }

        // 处理授信额度
        if (supplierDTO.getSettlementType() != null && 2 == supplierDTO.getSettlementType() && StringUtil.isNotEmpty(supplierDTO.getCreditAmount())) {
            SupplierPropertyEntity creditAmountProperty = supplierDao.getProperty(String.valueOf(supplierDO.getSupplierId()), "creditAmount");
            if (creditAmountProperty == null) {
                creditAmountProperty = new SupplierPropertyEntity(String.valueOf(supplierDO.getSupplierId()), "creditAmount", supplierDTO.getCreditAmount(), new Timestamp(System.currentTimeMillis()));
            } else {
                creditAmountProperty.setPropertyValue(supplierDTO.getCreditAmount());
            }
            supplierDao.saveOrUpdate(creditAmountProperty);
        }
        supplierDao.update(supplierDO);
        return 1;
    }

    public Page pageSupplier(int pageIndex, int pageSize, String supplierName, String supplierId, String supplierType, int status) {
        Page page = supplierDao.pageSupplier(pageIndex, pageSize, supplierName, supplierId, supplierType, status);
        if (page.getData() != null && page.getData().size() > 0) {
            SupplierEntity supplierDO;
            List<MarketResourceDTO> marketResourceDTOList;
            List<SupplierDTO> supplierDTOList = new ArrayList<>();
            SupplierDTO supplierDTO;
            Set<String> resourceType;
            SupplierPropertyEntity supplierProperty;
            for (int i = 0; i < page.getData().size(); i++) {
                resourceType = new HashSet<>();
                supplierDO = (SupplierEntity) page.getData().get(i);
                if (supplierDO != null) {
                    supplierDTO = new SupplierDTO(supplierDO);
                    marketResourceDTOList = marketResourceDao.listMarketResourceBySupplierId(String.valueOf(supplierDO.getSupplierId()));
                    for (MarketResourceDTO dto : marketResourceDTOList) {
                        if (dto == null || dto.getTypeCode() == null) {
                            continue;
                        }
                        if (1 == dto.getTypeCode()) {
                            resourceType.add("呼叫线路");
                        } else if (2 == dto.getTypeCode()) {
                            resourceType.add("短信");
                        } else if (4 == dto.getTypeCode()) {
                            resourceType.add("数据");
                        } else if (MarketResourceTypeEnum.B2B_PRICE.getType() == dto.getTypeCode()) {
                            resourceType.add("B2B数据");
                        }
                    }
                    if (resourceType.size() > 0) {
                        supplierDTO.setServiceResource(StringUtils.join(resourceType, ","));
                    }
                    // 查询授信额度
                    if (supplierDO.getSettlementType() != null && supplierDO.getSettlementType() == 2) {
                        supplierProperty = supplierDao.getProperty(String.valueOf(supplierDO.getSupplierId()), "creditAmount");
                        if (supplierProperty != null) {
                            supplierDTO.setCreditAmount(supplierProperty.getPropertyValue());
                        }
                    }
                    supplierDTOList.add(supplierDTO);
                }
            }
            page.setData(supplierDTOList);
        }
        return page;
    }

    private Map<String, Object> handleOnlineSupplierResource(int type, List<SupplierDTO> list) {
        Map<String, Object> data = null;
        if (list.size() > 0) {
            data = new HashMap<>();
            data.put("type", type);
            List<Map<String, Object>> supplierList = new ArrayList<>();
            Map<String, Object> supplierData;
            List<MarketResourceDTO> marketResourceList;
            for (SupplierDTO m : list) {
                supplierData = new HashMap<>();
                supplierData.put("supplierName", m.getName());
                supplierData.put("supplierId", m.getSupplierId());
                marketResourceList = marketResourceDao.listMarketResourceBySupplierIdAndType(String.valueOf(m.getSupplierId()), type);
                supplierData.put("resourceList", marketResourceList);
                supplierList.add(supplierData);
            }
            data.put("supplierList", supplierList);
        }
        return data;
    }

    /**
     * 获取所有可使用的供应商和供应商下的资源列表(供客户售价界面使用)
     *
     * @return
     */
    public List<Map<String, Object>> listOnlineAllSupplier() {
        List<Map<String, Object>> result = new ArrayList<>();
        Map<String, Object> data;
        // 查询所有数据供应商
        List<SupplierDTO> dataList = supplierDao.listOnlineAllSupplierByResourceType(MarketResourceTypeEnum.LABEL.getType());
        data = handleOnlineSupplierResource(MarketResourceTypeEnum.LABEL.getType(), dataList);
        if (data != null) {
            result.add(data);
        }

        // 查询所有通话供应商
        List<SupplierDTO> callList = supplierDao.listOnlineAllSupplierByResourceType(MarketResourceTypeEnum.CALL.getType());
        data = handleOnlineSupplierResource(MarketResourceTypeEnum.CALL.getType(), callList);
        if (data != null) {
            result.add(data);
        }

        // 查询所有短信供应商
        List<SupplierDTO> smsList = supplierDao.listOnlineAllSupplierByResourceType(MarketResourceTypeEnum.SMS.getType());
        data = handleOnlineSupplierResource(MarketResourceTypeEnum.SMS.getType(), smsList);
        if (data != null) {
            result.add(data);
        }
        return result;
    }


    public Map<String, Object> listSupplierMonthBill(String yearMonth, int pageIndex, int pageSize, String supplierName, String supplierId, String supplierType, int status) {
        Map<String, Object> result = new HashMap<>();
        double sumAmount = 0.0;
        Page page = supplierDao.pageSupplier(pageIndex, pageSize, supplierName, supplierId, supplierType, status);
        List<Map<String, Object>> list = new ArrayList<>();
        if (page != null && page.getData() != null && page.getData().size() > 0) {
            SupplierEntity supplierDO;
            Map<String, Object> data;
            double amount = 0.0;
            for (int i = 0; i < page.getData().size(); i++) {
                data = new HashMap<>();
                supplierDO = (SupplierEntity) page.getData().get(i);
                data.put("id", supplierDO.getSupplierId());
                data.put("name", supplierDO.getName());
                data.put("settlementType", supplierDO.getSettlementType());
                data.put("time", yearMonth);
                amount = billDao.sumSupplierMonthAmount(String.valueOf(supplierDO.getSupplierId()), yearMonth);
                data.put("amount", amount);
                sumAmount += amount;
                list.add(data);
            }
        }
        result.put("sumAmount", NumberConvertUtil.parseDecimalDouble(sumAmount, 2));
        result.put("list", list);
        result.put("total", page.getTotal());
        return result;
    }

    /**
     * @param supplierId
     * @param resourceId
     * @param type
     * @param orderNo
     * @param account    企业管理员账号
     * @param startTime
     * @param endTime
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Map<String, Object> listSupplierBill(String supplierId, String resourceId, int type, String orderNo, String account, String startTime, String endTime, int pageNum, int pageSize) {
        Map<String, Object> result = new HashMap<>();
        result.put("total", 0);
        result.put("list", new ArrayList<>());
        result.put("sumAmount", 0);
        String custId = null;
        if (StringUtil.isNotEmpty(account)) {
            CustomerUser user = customerUserDao.getCustomerUserByLoginName(account);
            if (user != null) {
                custId = user.getCust_id();
            } else {
                return result;
            }
        }
        Page page = billDao.pageSupplierBill(supplierId, resourceId, type, orderNo, custId, startTime, endTime, pageNum, pageSize);
        double sumAmount = 0.0;
        BigDecimal bigDecimal, decimal;
        long callDurationTime = 1;
        if (page.getData().size() > 0) {
            Map<String, Object> map;
            MarketResourceEntity marketResource = null;
            List<IndustryPool> industryPool;
            int resId = 0;
            List<CustomerUser> us;
            for (int i = 0; i < page.getData().size(); i++) {
                map = (Map<String, Object>) page.getData().get(i);
                if (map != null && map.get("resourceId") != null) {
                    resId = NumberConvertUtil.parseInt(map.get("resourceId"));
                    map.put("count", 1);
                    map.put("price", map.get("prodAmount"));
                    if (TransactionTypeEnum.CALL_DEDUCTION.getType() == type) {
                        if (StringUtil.isNotEmpty(String.valueOf(map.get("calledDuration"))) && !"null".equals(String.valueOf(map.get("calledDuration")))) {
                            map.put("count", DateUtil.secondToTime(NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration")))));
                            bigDecimal = new BigDecimal((double) NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration"))) / 60);
                            callDurationTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();
                        } else {
                            map.put("count", 0);
                            map.put("price", 0);
                            callDurationTime = 0;
                        }

                        if (callDurationTime > 0 && map.get("prodAmount") != null) {
                            decimal = new BigDecimal(String.valueOf(map.get("prodAmount"))).divide(new BigDecimal(callDurationTime));
                            map.put("price", NumberConvertUtil.parseDecimalDouble(decimal.doubleValue(), 3));
                        }
                    }
                } else if (map != null && map.get("industry_pool_id") != null) {
                    // 数据提取费用需要单独处理数量和价格
                    industryPool = industryPoolDao.createQuery(" FROM IndustryPool m WHERE m.industryPoolId = ?", NumberConvertUtil.parseInt(String.valueOf(map.get("industry_pool_id")))).list();
                    if (industryPool.size() > 0) {
                        resId = industryPool.get(0).getSourceId();
                    }
                    map.put("count", map.get("userCount"));
                    map.put("price", NumberConvertUtil.parseDecimalDouble(NumberConvertUtil.parseDouble(String.valueOf(map.get("cPrice"))), 3));
                    map.put("remark", map.get("id"));
                } else {
                    map.put("count", 1);
                    map.put("price", map.get("prodAmount"));
                }
                if (resId > 0) {
                    marketResource = marketResourceDao.get(resId);
                }

                us = customerUserDao.find("from CustomerUser m where m.cust_id='" + map.get("custId") + "' and m.userType=1");
                if (us.size() > 0) {
                    map.put("custId", us.get(0).getAccount());
                }
                map.put("resouceName", marketResource != null ? marketResource.getResname() : "");
                if (map.get("prodAmount") == null) {
                    map.put("prodAmount", 0);
                }
            }
        }
        result.put("total", page.getTotal());
        result.put("list", page.getData());
        // 按照类型查询供应商消费总金额
        Map<String, Object> amountData = billDao.statSupplierBillAmount(supplierId, resourceId, type, orderNo, custId, startTime, endTime, pageNum, pageSize);
        if (amountData.get("sumProdAmount") != null) {
            sumAmount = NumberConvertUtil.parseDouble(String.valueOf(amountData.get("sumProdAmount")));
        }
        result.put("sumAmount", sumAmount);
        return result;
    }

    public void exportExcelListBillByType(HttpServletResponse response, String supplierId, String resourceId, int type, String orderNo, String custId, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();

            // 处理时间
            String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DATE_TIME_FORMATTER);
            String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DATE_TIME_FORMATTER);
            if (StringUtil.isNotEmpty(startTime)) {
                startTimeStr = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            if (StringUtil.isNotEmpty(endTime)) {
                endTimeStr = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;

            head = new ArrayList<>();
            head.add("订单号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易类型");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("数量/时长");
            headers.add(head);

            head = new ArrayList<>();
            head.add("单价(元)");
            headers.add(head);

            head = new ArrayList<>();
            head.add("总金额(元)");
            headers.add(head);

            head = new ArrayList<>();
            head.add("备注");
            headers.add(head);

            List<Map<String, Object>> list = billDao.listSupplierBill(supplierId, resourceId, type, orderNo, custId, startTime, endTime);
            List<List<String>> data = new ArrayList<>();
            if (list != null && list.size() > 0) {
                Map<String, Object> map;
                MarketResourceEntity marketResource = null;
                List<IndustryPool> industryPool;

                List<String> columnList;
                BigDecimal bigDecimal, decimal;
                long callDurationTime = 1;
                for (int i = 0; i < list.size(); i++) {
                    map = list.get(i);
                    if (map != null && map.get("resourceId") != null) {
                        map.put("count", 1);
                        map.put("price", map.get("prodAmount"));

                        if (TransactionTypeEnum.CALL_DEDUCTION.getType() == type) {
                            map.put("count", DateUtil.secondToTime(NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration")))));
                            bigDecimal = new BigDecimal((double) NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration"))) / 60);
                            callDurationTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();

                            if (callDurationTime == 0) {
                                map.put("price", 0);
                            } else {
                                if (map.get("prodAmount") != null) {
                                    //map.put("price", new BigDecimal(String.valueOf(map.get("prodAmount"))).divide(new BigDecimal(callDurationTime)));
                                    decimal = new BigDecimal(String.valueOf(map.get("prodAmount"))).divide(new BigDecimal(callDurationTime));
                                    map.put("price", NumberConvertUtil.parseDecimalDouble(decimal.doubleValue(), 2));
                                }
                            }
                        }
                    } else if (map != null && map.get("industry_pool_id") != null) {
                        // 数据提取费用需要单独处理数量和价格
                        industryPool = industryPoolDao.createQuery(" FROM IndustryPool m WHERE m.industryPoolId = ?", NumberConvertUtil.parseInt(String.valueOf(map.get("industry_pool_id")))).list();
                        map.put("count", map.get("userCount"));
                        map.put("price", map.get("amount"));
                        map.put("remark", map.get("id"));
                    } else {
                        map.put("count", 1);
                        map.put("price", map.get("prodAmount"));
                    }
                    if (TransactionTypeEnum.CALL_DEDUCTION.getType() == type) {
                        map.put("count", DateUtil.secondToTime(NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration")))));
                        bigDecimal = new BigDecimal((double) NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration"))) / 60);
                        callDurationTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();
                        map.put("price", new BigDecimal(String.valueOf(map.get("prodAmount"))).divide(new BigDecimal(callDurationTime)));
                    }

                    columnList = new ArrayList<>();
                    //交易ID
                    columnList.add(String.valueOf(map.get("transactionId")));
                    //交易类型
                    columnList.add(TransactionTypeEnum.getName(type));
                    //交易时间
                    columnList.add(LocalDateTime.parse(String.valueOf(map.get("createTime")), DATE_TIME_FORMATTER_SSS).format(DATE_TIME_FORMATTER));
                    //数量/时长
                    columnList.add(String.valueOf(map.get("count")));
                    //单价(元)
                    columnList.add(String.valueOf(map.get("price")));
                    // 总金额(元)
                    columnList.add(String.valueOf(map.get("price")));
                    // 备注
                    columnList.add(String.valueOf(map.get("remark")));
                    data.add(columnList);
                }
            }

            if (data.size() > 0) {
                String fileName = "供应商-" + TransactionTypeEnum.getName(type) + LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                final String fileType = ".xlsx";
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                Sheet sheet1 = new Sheet(1, 0);
                sheet1.setHead(headers);
                sheet1.setSheetName(TransactionTypeEnum.getName(type));
                writer.write0(data, sheet1);
                writer.finish();
            } else {
                msg.put("msg", "无满足条件的数据");
                msg.put("data", String.valueOf(""));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
        } catch (Exception e) {
            log.error("下的营销数据异常,", e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                log.error("导出营销数据异常,", e);
            }
        }
    }


    public void exportExcelMonthBill(HttpServletResponse response, String supplierId, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();

            // 处理时间
            String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DATE_TIME_FORMATTER);
            String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DATE_TIME_FORMATTER);
            if (StringUtil.isNotEmpty(startTime)) {
                startTimeStr = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            if (StringUtil.isNotEmpty(endTime)) {
                endTimeStr = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;

            head = new ArrayList<>();
            head.add("订单号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易类型");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("数量/时长");
            headers.add(head);

            head = new ArrayList<>();
            head.add("单价(元)");
            headers.add(head);

            head = new ArrayList<>();
            head.add("总金额(元)");
            headers.add(head);

            head = new ArrayList<>();
            head.add("备注");
            headers.add(head);

            List<Map<String, Object>> list;
            List<List<String>> data;
            Sheet sheet;
            Map<String, Object> map;
            MarketResourceEntity marketResource = null;
            List<IndustryPool> industryPool;
            List<String> columnList;

            String fileName = "供应商" + LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyy-MM")) + "账单数据";
            final String fileType = ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
            int sheetNum = 1;
            BigDecimal bigDecimal, decimal;
            long callDurationTime = 1;
            for (TransactionTypeEnum s : TransactionTypeEnum.values()) {
                if (s.getType() <= 2) {
                    continue;
                }
                list = billDao.listSupplierBill(supplierId, "", s.getType(), "", "", startTime, endTime);
                data = new ArrayList<>();
                if (list != null && list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        map = list.get(i);
                        if (map != null && map.get("resourceId") != null) {
                            map.put("count", 1);
                            map.put("price", map.get("prodAmount"));
                            if (TransactionTypeEnum.CALL_DEDUCTION.getType() == s.getType()) {
                                map.put("count", DateUtil.secondToTime(NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration")))));
                                bigDecimal = new BigDecimal((double) NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration"))) / 60);
                                callDurationTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();

                                if (callDurationTime == 0) {
                                    map.put("price", 0);
                                } else {
                                    if (map.get("prodAmount") != null) {
                                        //map.put("price", new BigDecimal(String.valueOf(map.get("prodAmount"))).divide(new BigDecimal(callDurationTime)));
                                        decimal = new BigDecimal(String.valueOf(map.get("prodAmount"))).divide(new BigDecimal(callDurationTime));
                                        map.put("price", NumberConvertUtil.parseDecimalDouble(decimal.doubleValue(), 2));
                                    }
                                }
                            }
                        } else if (map != null && map.get("industry_pool_id") != null) {
                            // 数据提取费用需要单独处理数量和价格
                            industryPool = industryPoolDao.createQuery(" FROM IndustryPool m WHERE m.industryPoolId = ?", NumberConvertUtil.parseInt(String.valueOf(map.get("industry_pool_id")))).list();
                            map.put("count", map.get("userCount"));
                            map.put("price", map.get("amount"));
                            map.put("remark", map.get("id"));
                        } else {
                            map.put("count", 1);
                            map.put("price", map.get("prodAmount"));
                        }
                        if (TransactionTypeEnum.CALL_DEDUCTION.getType() == s.getType()) {
                            map.put("count", DateUtil.secondToTime(NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration")))));
                            bigDecimal = new BigDecimal((double) NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration"))) / 60);
                            callDurationTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();
                            map.put("price", new BigDecimal(String.valueOf(map.get("prodAmount"))).divide(new BigDecimal(callDurationTime)));
                        }

                        columnList = new ArrayList<>();
                        //交易ID
                        columnList.add(String.valueOf(map.get("transactionId")));
                        //交易类型
                        columnList.add(s.getName());
                        //交易时间
                        columnList.add(LocalDateTime.parse(String.valueOf(map.get("createTime")), DATE_TIME_FORMATTER_SSS).format(DATE_TIME_FORMATTER));
                        //数量/时长
                        columnList.add(String.valueOf(map.get("count")));
                        //单价(元)
                        columnList.add(String.valueOf(map.get("price")));
                        // 总金额(元)
                        columnList.add(String.valueOf(map.get("price")));
                        // 备注
                        columnList.add(String.valueOf(map.get("remark")));
                        data.add(columnList);
                    }
                }

                sheet = new Sheet(sheetNum, 0);
                sheetNum++;
                sheet.setHead(headers);
                sheet.setSheetName(s.getName());
                writer.write0(data, sheet);
            }
            writer.finish();
        } catch (Exception e) {
            log.error("导出数据异常,", e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                log.error("导出数据异常,", e);
            }
        }
    }

    /**
     * 供应商调用统计数据导出
     *
     * @param response
     * @param userId
     * @param param
     */
    public void exportSupplierCustGroupExcel(HttpServletResponse response, String userId, CustomerGrpOrdParam param) {
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;
            head = new ArrayList<>();
            head.add("客群ID");
            headers.add(head);
            head = new ArrayList<>();
            head.add("提取数量");
            headers.add(head);
            head = new ArrayList<>();
            head.add("提取时间");
            headers.add(head);
            head = new ArrayList<>();
            head.add("使用企业");
            headers.add(head);
            head = new ArrayList<>();
            head.add("项目");
            headers.add(head);
            head = new ArrayList<>();
            head.add("标记成功量");
            headers.add(head);
            head = new ArrayList<>();
            head.add("供应商");
            headers.add(head);
            head = new ArrayList<>();
            head.add("计费方式");
            headers.add(head);
            head = new ArrayList<>();
            head.add("总价");
            headers.add(head);

            Map<String, Object> map;
            List<String> columnList;
            Page page = customGroupService.pageSupplierCustGroup(userId, param);
            List<List<String>> data = new ArrayList<>();
            if (page.getData() != null && page.getData().size() > 0) {
                String fileName = "供应商调用统计数据";
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + ExcelTypeEnum.XLSX.getValue());
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                for (int i = 0; i < page.getData().size(); i++) {
                    map = (Map<String, Object>) page.getData().get(i);
                    columnList = new ArrayList<>();
                    columnList.add(String.valueOf(map.get("groupId")));
                    columnList.add(String.valueOf(map.get("actualSum")));
                    columnList.add(String.valueOf(map.get("extractTime")));
                    columnList.add(String.valueOf(map.get("enterpriseName")));
                    columnList.add(String.valueOf(map.get("projectName")));
                    columnList.add(String.valueOf(map.get("orderSum")));
                    columnList.add(String.valueOf(map.get("supplierName")));
                    if (StringUtil.isNotEmpty(String.valueOf(map.get("chargingType")))) {
                        columnList.add(getDataPriceType(NumberConvertUtil.parseInt(map.get("chargingType"))));
                    } else {
                        columnList.add("");
                    }
                    columnList.add(String.valueOf(map.get("amount")));
                    data.add(columnList);
                }
                Sheet sheet = new Sheet(1, 0);
                sheet.setHead(headers);
                sheet.setSheetName(fileName);
                writer.write0(data, sheet);
                writer.finish();
            }

        } catch (Exception e) {
            log.error("导出供应商调用统计数据异常,", e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                log.error("导出供应商调用统计数据异常,", e);
            }
        }
    }

    /**
     * 根据数据计费方式type获取计费方式名称
     *
     * @param type
     * @return
     */
    private String getDataPriceType(int type) {
        //1-按条单一计费 2-按条阶梯计费 3-按标签计费 4-按呼通计费 5-按效果计费
        String name = "";
        switch (type) {
            case 1:
                name = "按条单一计费";
                break;
            case 2:
                name = "按条阶梯计费";
                break;
            case 3:
                name = "按标签计费";
                break;
            case 4:
                name = "按呼通计费";
                break;
            case 5:
                name = "按效果计费";
                break;
        }
        return name;
    }

    /**
     * 统计前N个月的供应商提取数据和标记单
     *
     * @param userId
     * @param month
     * @return
     */
    public Map<String, Object> statSupplierMonthData(String userId, int month) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        DateTimeFormatter yyyyMM = DateTimeFormatter.ofPattern("yyyy-MM");
        LocalDate now = LocalDate.now();
        StringBuilder sql = new StringBuilder();
        // 获取当前时间到前N个月的统计数据
        sql.append("SELECT t1.supplier_id,t1.`name`, SUM(t.extract_sum) extract_sum, SUM(t.order_sum) order_sum, DATE_FORMAT(t.create_time,'%Y-%m') as time FROM stat_supplier_data_day t " +
                " JOIN t_supplier t1 ON t.supplier_id = t1.supplier_id " +
                " WHERE t.create_time BETWEEN ? AND ? ");
        if (StringUtil.isNotEmpty(userId)) {
            String supplierIds = userGroupService.getUserDataPermissonListByUserId(userId, 5);
            if (StringUtil.isNotEmpty(supplierIds)) {
                sql.append("and t1.supplier_id in(" + supplierIds + ")");
            } else {
                Map<String, Object> data = new HashMap<>();
                data.put("extractData", "");
                data.put("orderData", "");
                return data;
            }
        }
        sql.append(" GROUP BY t.supplier_id, time ORDER BY time ASC ");
        Set<String> times = new TreeSet<>(), supplierIds = new TreeSet<>(), supplierNames = new TreeSet<>();
        for (LocalDate start = now.minusMonths(month); start.isBefore(now) || start.isEqual(now); ) {
            times.add(start.format(yyyyMM));
            start = start.plusMonths(1);
        }
        Map<Object, Object> supplierData = new HashMap<>();
        List<Map<String, Object>> list = supplierDao.sqlQuery(sql.toString(), now.minusMonths(month).format(formatter), now.format(formatter));
        for (int i = 0; i < list.size(); i++) {
            supplierIds.add(String.valueOf(list.get(i).get("supplier_id")));
            supplierNames.add(String.valueOf(list.get(i).get("name")));
            supplierData.put(list.get(i).get("supplier_id") + "" + list.get(i).get("time") + "extract_sum", list.get(i).get("extract_sum"));
            supplierData.put(list.get(i).get("supplier_id") + "" + list.get(i).get("time") + "order_sum", list.get(i).get("order_sum"));
        }

        List<List<String>> extractSumDataList = new ArrayList<>();
        List<List<String>> orderNumDataList = new ArrayList<>();
        // 处理供应商名称
        List<String> title = new ArrayList<>();
        title.add("product");
        for (String supplierName : supplierNames) {
            title.add(supplierName);
        }
        extractSumDataList.add(title);
        List<String> titleCopy = new ArrayList<>(title);
        orderNumDataList.add(titleCopy);

        // 处理供应商id
        List<String> ids = new ArrayList<>();
        ids.add("ids");
        for (String supplierId : supplierIds) {
            ids.add(supplierId);
        }
        extractSumDataList.add(ids);
        titleCopy = new ArrayList<>(ids);
        orderNumDataList.add(titleCopy);

        // 处理月份处理
        List<String> extractData, orderNumData;
        String extractSum, orderNum;
        for (String time : times) {
            extractData = new ArrayList<>();
            orderNumData = new ArrayList<>();
            extractData.add(time);
            orderNumData.add(time);
            for (String supplierId : supplierIds) {
                // 处理提取量
                extractSum = String.valueOf(supplierData.get(supplierId + time + "extract_sum"));
                if (StringUtil.isEmpty(extractSum) || "null".equals(extractSum)) {
                    extractSum = "0";
                }
                extractData.add(extractSum);
                // 处理标记单
                orderNum = String.valueOf(supplierData.get(supplierId + time + "order_sum"));
                if (StringUtil.isEmpty(orderNum) || "null".equals(orderNum)) {
                    orderNum = "0";
                }
                orderNumData.add(orderNum);
            }
            extractSumDataList.add(extractData);
            orderNumDataList.add(orderNumData);
        }
        Map<String, Object> data = new HashMap<>();
        data.put("extractData", extractSumDataList);
        data.put("orderData", orderNumDataList);
        return data;
    }

    /**
     * 供应商数据提取量和标记成单量按天统计
     *
     * @param userId
     * @param type       1-数据提取量统计 2-标记单统计
     * @param supplierId
     * @param yearMonth  201905
     * @return
     */
    public List<String> statSupplierDayData(String userId, int type, String supplierId, String yearMonth) {
        StringBuilder sql = new StringBuilder();
        // 获取当前时间到前N个月的统计数据
        sql.append("SELECT t1.supplier_id,t1.`name`, t.extract_sum, t.order_sum, t.create_time FROM stat_supplier_data_day t " +
                " JOIN t_supplier t1 ON t.supplier_id = t1.supplier_id ");
        if (StringUtil.isNotEmpty(userId)) {
            String supplierIds = userGroupService.getUserDataPermissonListByUserId(userId, 5);
            if (StringUtil.isNotEmpty(supplierIds)) {
                sql.append("and t1.supplier_id in(" + supplierIds + ")");
            } else {
                return new ArrayList<>();
            }
        }
        sql.append(" WHERE t.create_time LIKE ? AND t1.supplier_id =? ORDER BY t.create_time ASC ");
        List<Map<String, Object>> list = supplierDao.sqlQuery(sql.toString(), yearMonth + "%", supplierId);
        if (list == null || list.size() == 0) {
            return new ArrayList<>();
        }
        Map<Object, Object> supplierData = new HashMap<>();
        for (int i = 0; i < list.size(); i++) {
            supplierData.put(list.get(i).get("create_time") + "extract_sum", list.get(i).get("extract_sum"));
            supplierData.put(list.get(i).get("create_time") + "order_sum", list.get(i).get("order_sum"));
        }
        int year = NumberConvertUtil.parseInt(yearMonth.split("-")[0]);
        int month = NumberConvertUtil.parseInt(yearMonth.split("-")[1]);
        //本月第一天
        LocalDate startDay = LocalDate.of(year, month, 1);
        //本月的最后一天
        LocalDate lastDay = startDay.with(TemporalAdjusters.lastDayOfMonth());
        List<String> data = new ArrayList<>();
        String time, value = null;
        for (LocalDate start = startDay; start.isBefore(lastDay) || start.isEqual(lastDay); ) {
            time = start.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
            if (type == 1) {
                value = String.valueOf(supplierData.get(time + "extract_sum"));
            } else if (type == 2) {
                value = String.valueOf(supplierData.get(time + "order_sum"));
            }
            if (StringUtil.isEmpty(value) || "null".equals(value)) {
                value = "0";
            }
            data.add(value);
            start = start.plusDays(1);
        }
        return data;
    }

    public int saveSupplier1(SupplierDTO supplierDTO) {
        if (supplierDTO == null) {
            throw new RuntimeException("supplierDTO参数异常");
        }
        SupplierEntity supplierDO = new SupplierEntity();
        supplierDO.setName(supplierDTO.getName());
        supplierDO.setSettlementType(supplierDTO.getSettlementType());
        supplierDO.setContactPerson(supplierDTO.getContactPerson());
        supplierDO.setContactPhone(supplierDTO.getContactPhone());
        supplierDO.setContactPosition(supplierDTO.getContactPosition());
        supplierDO.setType(5);
        supplierDO.setStatus(1);
        supplierDO.setCreateTime(new Timestamp(System.currentTimeMillis()));
        int supplierId = (int) supplierDao.saveReturnPk(supplierDO);
        if (supplierId == 0) throw new RuntimeException("供应商保存异常");
        return supplierId;
    }

    public SupplierDTO updateSupplierPrice1(SupplierDTO supplierDTO) {
        if (supplierDTO == null) {
            throw new RuntimeException("supplierDTO参数异常");
        }
        SupplierEntity supplierDO = supplierDao.get(supplierDTO.getSupplierId());
        if (supplierDO == null) {
            throw new RuntimeException(supplierDTO.getSupplierId() + "供应商不存在");
        }
        supplierDO.setName(supplierDTO.getName());
        supplierDO.setSettlementType(supplierDTO.getSettlementType());
        supplierDO.setContactPerson(supplierDTO.getContactPerson());
        supplierDO.setContactPhone(supplierDTO.getContactPhone());
        supplierDO.setContactPosition(supplierDTO.getContactPosition());
        supplierDO.setStatus(2);
        try {
            supplierDao.saveOrUpdate(supplierDO);
        } catch (Exception e) {
            throw new RuntimeException("供应商修改异常");
        }

        return supplierDTO;
    }

    public SupplierDTO getSupplierById(int id) {

        SupplierEntity supplierDO = supplierDao.get(id);
        if (supplierDO == null) {
            throw new RuntimeException(id + "供应商不存在");
        }
        SupplierDTO supplierDTO = new SupplierDTO();
        supplierDTO.setName(supplierDO.getName());
        supplierDTO.setSettlementType(supplierDO.getSettlementType());
        supplierDTO.setContactPerson(StringUtil.isEmpty(supplierDO.getContactPerson()) ? "" : supplierDO.getContactPerson());
        supplierDTO.setContactPhone(StringUtil.isEmpty(supplierDO.getContactPhone()) ? "" : supplierDO.getContactPhone());
        supplierDTO.setContactPosition(StringUtil.isEmpty(supplierDO.getContactPosition()) ? "" : supplierDO.getContactPosition());
        supplierDTO.setStatus(supplierDO.getStatus());
        supplierDTO.setCreateTime(supplierDO.getCreateTime());
        return supplierDTO;
    }

    public Map<String, Object> getSupplierList(PageParam page, String name) {

        StringBuffer sql = new StringBuffer();
        sql.append("select supplier_id,name,settlement_type,contact_person,contact_phone,contact_position,status,create_time from t_supplier where status =1 ");
        if (StringUtil.isNotEmpty(name)) {
            sql.append(" and name like '%" + name + "%'");
        }
        sql.append(" order by create_time desc");
        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        Map<String, Object> map = new HashMap<>();
        map.put("total", list.getTotal());
        Object collect = list.getList().stream().map(m -> {
            Map map1 = (Map) m;
            Map<String, Object> supplierDTOMap = new HashMap<>();
            supplierDTOMap.put("name", map1.get("name"));
            supplierDTOMap.put("settlementType", map1.get("settlement_type"));
            supplierDTOMap.put("contactPerson", map1.get("contact_person"));
            supplierDTOMap.put("contactPhone", map1.get("contact_phone"));
            supplierDTOMap.put("contactPosition", map1.get("contact_position"));
            supplierDTOMap.put("status", map1.get("status"));
            supplierDTOMap.put("createTime", map1.get("create_time"));
            supplierDTOMap.put("balance", 0);
            supplierDTOMap.put("consumption", 0);
            supplierDTOMap.put("supplierId", map1.get("supplier_id"));
            return supplierDTOMap;
        }).collect(Collectors.toList());
        map.put("list", collect);
        return map;

    }

    public int supplierDeposit(Deposit deposit, String userId) {
        int pre_money;
        int money = Integer.valueOf(deposit.getMoney()).intValue() * 10000;
        SupplierPropertyEntity supplierPropertyEntity = supplierDao.getProperty(String.valueOf(deposit.getId()), "remain_amount");
        if (supplierPropertyEntity == null) {
            pre_money = 0;
            supplierDao.dealCustomerInfo(String.valueOf(deposit.getId()), "remain_amount", deposit.getMoney());
        } else {
            pre_money = Integer.valueOf(supplierPropertyEntity.getPropertyValue()).intValue() * 10000;
            supplierDao.dealCustomerInfo(String.valueOf(deposit.getId()), "remain_amount", String.valueOf((pre_money + money)));
        }
        String sql = "INSERT INTO supplier_pay (SUBSCRIBER_ID,MONEY,PAY_TIME,pay_certificate,pre_money,user_id) VALUE (?,?,?,?,?,?) ";
        jdbcTemplate.update(sql, deposit.getId(), money, DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss), deposit.getRepaidVoucher(), pre_money, userId);
        return 1;
    }

    public Map<String, Object> depositList(PageParam page, String supplierId) {
        Map<String, Object> map = new HashMap<>();
        String sql = "select supplier_id,property_name,property_value from t_supplier_property   where supplier_id=?";

        List<Map<String, Object>> propertyList = jdbcTemplate.queryForList(sql, supplierId);
        propertyList.stream().forEach(m -> {
            switch (m.get("property_name").toString()) {
                case "bank_account":
                    map.put("bank_account", m.get("property_value"));
                    break;
                case "remain_amount":
                    map.put("remain_amount", Integer.valueOf(m.get("property_value").toString()).intValue() / 10000);
                    break;
            }
        });

        String sql1 = "select pay.pay_id,pay.SUBSCRIBER_ID,pay.MONEY,pay.PAY_TIME,pay.pay_certificate,pay.pre_money,pay.user_id ,u.realname as realname from supplier_pay pay left join  t_customer_user u  on pay.user_id=u.id  where SUBSCRIBER_ID = " + supplierId + " order by pay_time";
        PageList list = new Pagination().getPageData(sql1, null, page, jdbcTemplate);
        List<com.bdaim.customer.dto.Deposit> depositList = new ArrayList<>();
        list.getList().stream().forEach(m -> {
            com.bdaim.customer.dto.Deposit deposit = new com.bdaim.customer.dto.Deposit();
            Map depositMap = (Map) m;
            if (depositMap.get("SUBSCRIBER_ID") != null) {
                deposit.setCustId(depositMap.get("SUBSCRIBER_ID").toString());
            }
            if (depositMap.get("MONEY") != null) {
                deposit.setMoney(Integer.valueOf(depositMap.get("MONEY").toString()).intValue() / 10000 + "");
            }
            if (depositMap.get("PAY_TIME") != null) {
                deposit.setPayTime(depositMap.get("PAY_TIME").toString());
            }
            if (depositMap.get("pay_id") != null) {
                deposit.setId(Integer.valueOf(depositMap.get("pay_id").toString()));
            }
            if (depositMap.get("pay_certificate") != null) {
                deposit.setPicId(depositMap.get("pay_certificate").toString());
            }
            if (depositMap.get("pre_money") != null) {
                deposit.setPreMoney(Integer.valueOf(depositMap.get("pre_money").toString()).intValue() / 10000 + "");
            }
            if (depositMap.get("user_id") != null) {
                deposit.setPreMoney(depositMap.get("user_id").toString());
            }
            if (depositMap.get("realname") != null) {
                deposit.setPreMoney(depositMap.get("realname").toString());
            }
            depositList.add(deposit);
        });
        SupplierEntity supplierEntity = supplierDao.get(Integer.valueOf(supplierId));
        map.put("depositList", depositList);
        map.put("custName", supplierEntity.getName() == null ? "" : supplierEntity.getName());
        map.put("total", list.getTotal());
        return map;
    }

    public int delSupplierById(String supplierId) throws Exception {

        SupplierEntity supplierEntity = supplierDao.getSupplier(Integer.valueOf(supplierId));
        if (supplierEntity == null) throw new Exception("供应商不存在或已被删除");
        supplierEntity.setStatus(2);
        int pk = (int) supplierDao.saveReturnPk(supplierEntity);
        return pk;
    }

}

