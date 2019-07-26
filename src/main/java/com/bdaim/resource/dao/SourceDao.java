package com.bdaim.resource.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.util.ConstantsUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.resource.entity.SourcePropertyEntity;
import com.bdaim.supplier.entity.SupplierPropertyEntity;
import com.bdaim.template.entity.MarketTemplate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SourceDao extends SimpleHibernateDao<Object, String> {
    private final static Logger LOG = LoggerFactory.getLogger(SourceDao.class);
    @Resource
    CustomerDao customerDao;

    /**
     * @description 获取供应商属性信息
     * @author:duanliying
     * @method
     * @date: 2019/1/3 20:35
     */
    public SupplierPropertyEntity getSupplierProperty(String supplier, String propertyKey) {
        SupplierPropertyEntity cp = null;
        String hql = "from SupplierPropertyEntity m where m.supplierId=? and m.propertyName=?";
        List<SupplierPropertyEntity> list = this.find(hql, supplier, propertyKey);
        if (list.size() > 0)
            cp = (SupplierPropertyEntity) list.get(0);
        return cp;
    }

    /**
     * @description 获取供应商属性信息
     * @author:duanliying
     * @method
     * @date: 2019/1/3 20:35
     */
    public List<MarketResourceEntity> getSResourceIdList(int type) {
        String hql = "from MarketResourceEntity m where m.typeCode=?";
        List<MarketResourceEntity> list = this.find(hql, type);
        return list;
    }

    /**
     * @description 获取资源id
     * @author:duanliying
     * @method
     * @date: 2019/1/8 9:09
     */
    public MarketResourceEntity getResourceId(String supplier, int type) {
        MarketResourceEntity cp = null;
        String hql = "from MarketResourceEntity m where m.supplierId=? and m.typeCode=?";
        List<MarketResourceEntity> list = this.find(hql, supplier, type);
        if (list.size() > 0)
            cp = (MarketResourceEntity) list.get(0);
        return cp;
    }

    public SourcePropertyEntity getProperty(String sourceId, String propertyKey) {
        SourcePropertyEntity cp = null;
        String hql = "from SourcePropertyEntity m where m.sourceId=? and m.propertyKey=?";
        List<SourcePropertyEntity> list = this.find(hql, sourceId, propertyKey);
        if (list.size() > 0)
            cp = (SourcePropertyEntity) list.get(0);
        return cp;
    }

    public MarketResourceEntity getSupplier(String supplierId, int typeCode) {
        MarketResourceEntity cp = null;
        String hql = "from MarketResourceEntity m where m.supplierId=? and m.typeCode=?";
        List<MarketResourceEntity> list = this.find(hql, supplierId, typeCode);
        if (list.size() > 0)
            cp = (MarketResourceEntity) list.get(0);
        return cp;
    }

    public ResourcePropertyEntity getSourceProperty(int resourceId) {
        ResourcePropertyEntity cp = null;
        String hql = "from ResourcePropertyEntity m where m.resourceId=? and propertyName ='price_config'";
        List<ResourcePropertyEntity> list = this.find(hql, resourceId);
        if (list.size() > 0)
            cp = (ResourcePropertyEntity) list.get(0);
        return cp;
    }


    /**
     * 获取资源的属性
     *
     * @param resourceId
     * @param propertyName
     * @return
     */
    public ResourcePropertyEntity getResourceById(String resourceId, String propertyName) {
        ResourcePropertyEntity mp = null;
        String hql = "from ResourcePropertyEntity m where m.resourceId=? and m.propertyName=?";
        List<ResourcePropertyEntity> list = this.find(hql, Integer.parseInt(resourceId), propertyName);
        if (list.size() > 0) {
            mp = list.get(0);
        }
        return mp;
    }

    /**
     * 根据资源id查询资源对象
     */
    public MarketResourceEntity getSourceMessage(int resourceId) {
        MarketResourceEntity cp = null;
        String hql = "from MarketResourceEntity m where m.resourceId=?";
        List<MarketResourceEntity> list = this.find(hql, resourceId);
        if (list.size() > 0)
            cp = (MarketResourceEntity) list.get(0);
        return cp;
    }

    /**
     * v1使用
     *
     * @param resourceId
     * @return
     */
    public ResourcePropertyEntity getSourceProperty(String resourceId) {
        ResourcePropertyEntity cp = null;
        String hql = "from ResourcePropertyEntity m where m.resourceId=?";
        List<ResourcePropertyEntity> list = this.find(hql, resourceId);
        if (list.size() > 0)
            cp = (ResourcePropertyEntity) list.get(0);
        return cp;
    }

    /**
     * 供应商扣费
     *
     * @param supplierId
     * @param amount     金额(分)
     * @return boolean
     * @author yanls@salescomm.net
     * @date 2018/10/9 10:58
     */
    public boolean supplierAccountDuctions(String supplierId, BigDecimal amount) {
        // 处理负值的情况
        if (amount.doubleValue() < 0) {
            amount = new BigDecimal(Math.abs(amount.doubleValue()));
        }
        boolean success = false;
        SupplierPropertyEntity remainAmount = this.getSupplierProperty(supplierId, "remain_amount");
        SupplierPropertyEntity usedAmount = this.getSupplierProperty(supplierId, "used_amount");
        if (remainAmount == null) {
            // 处理账户不存在
            remainAmount = new SupplierPropertyEntity();
            remainAmount.setSupplierId(supplierId);
            remainAmount.setPropertyName("remain_amount");
            remainAmount.setPropertyValue("0");
            logger.info(supplierId + " 资源不存在开始新建资源账户余额信息");
            this.saveOrUpdate(remainAmount);
        }
        if (usedAmount == null) {
            // 累计消费 处理账户不存在
            usedAmount = new SupplierPropertyEntity();
            usedAmount.setSupplierId(supplierId);
            usedAmount.setPropertyName("used_amount");
            usedAmount.setPropertyValue("0");
            logger.info(supplierId + "资源不存在开始新建资源累计消费信息");
            this.saveOrUpdate(usedAmount);

        }
        if (StringUtil.isNotEmpty(remainAmount.getPropertyValue())) {
            if (Double.parseDouble(remainAmount.getPropertyValue()) <= 0) {
                logger.info(supplierId + " 资源账户余额:" + remainAmount + ",先执行扣减");
            }
            // 扣减余额
            DecimalFormat df = new DecimalFormat("#");
            BigDecimal remainAmountBigDecimal = new BigDecimal(remainAmount.getPropertyValue());
            String nowMoney, useSumMoney;
            nowMoney = df.format(remainAmountBigDecimal.subtract(amount));
            remainAmount.setPropertyValue(nowMoney);
            this.saveOrUpdate(remainAmount);

            // 处理累计消费累加
            BigDecimal usedAmountBigDecimal = new BigDecimal(usedAmount.getPropertyValue() == null ? "0" : usedAmount.getPropertyValue());
            useSumMoney = df.format(usedAmountBigDecimal.add(amount));
            usedAmount.setPropertyValue(useSumMoney);
            this.saveOrUpdate(usedAmount);
            success = true;
        }
        if (success) {
            logger.info("账户扣减,供应商id：" + supplierId + "\t金额：" + amount.toString());
        }
        return success;
    }

    /**
     * 账户充值
     *
     * @param
     * @param amount 金额(分)
     * @return boolean
     * @author chengning@salescomm.net
     * @date 2018/10/9 10:58
     */
    public boolean accountSupplierRecharge(String supplierId, BigDecimal amount) {
        boolean success = false;
        SupplierPropertyEntity remainAmount = this.getSupplierProperty(supplierId, "remain_amount");
        if (remainAmount == null) {
            // 处理账户不存在,则重新创建对象保存账户
            remainAmount = new SupplierPropertyEntity();
            remainAmount.setCreateTime(new Timestamp(System.currentTimeMillis()));
            remainAmount.setSupplierId(supplierId);
            remainAmount.setPropertyValue(String.valueOf(amount.doubleValue()));
            logger.info(supplierId + " 账户不存在开始新建账户信息");
            remainAmount.setPropertyName("remain_amount");
            this.save(remainAmount);
            success = true;
        } else {
            if (StringUtil.isNotEmpty(remainAmount.getPropertyValue())) {
                BigDecimal remainAmountBigDecimal = new BigDecimal(remainAmount.getPropertyValue());
                DecimalFormat df = new DecimalFormat("#");
                String nowMoney = df.format(remainAmountBigDecimal.add(amount));
                remainAmount.setPropertyValue(nowMoney);
                this.update(remainAmount);
                success = true;
            }
        }
        if (success) {
            logger.info("账户充值,资源id：" + supplierId + "\t金额：" + amount.toString());
        }
        //this.saveOrUpdate(remainAmount);
        return success;
    }

    /**
     * 获取账户余额
     *
     * @param supplierId
     * @date 2018/10/9 15:58
     */
    public Double remainMoney(String supplierId) throws TouchException {
        SupplierPropertyEntity remainAmount = this.getSupplierProperty(supplierId, "remain_amount");
        if (remainAmount == null) {
            throw new TouchException(supplierId + " 账户不存在");
        }
        logger.info("供应商余额信息是：" + remainAmount);
        if (StringUtil.isNotEmpty(remainAmount.getPropertyValue())) {
            Double remainMoney = Double.valueOf(remainAmount.getPropertyValue()) / 100;
            return remainMoney;
        }
        return 0.00;
    }

    /**
     * 获取供应商定价集合
     *
     * @param
     * @param custId
     * @return 单位为分
     */
    public Map<String, Object> querySupplierPrice(String custId, String supplierId) {
        Map<String, Object> supplierPriceMap = new HashMap<>();
        StringBuilder sqlBuilder = new StringBuilder("SELECT property_value FROM t_market_resource re\n");
        sqlBuilder.append("LEFT JOIN t_market_resource_property m ON re.resource_id = m.resource_id\n");
        sqlBuilder.append("WHERE re.supplier_id = ? AND re.type_code = ?");
        try {
            //定价集合
            List<Map<String, Object>> queryPricelist = null;
            if (StringUtil.isNotEmpty(supplierId)) {
                //通过resourceID查询销售定价
                List<Map<String, Object>> queryCallPricelist = this.sqlQuery(sqlBuilder.toString(), supplierId, ConstantsUtil.CALL_TYPE);
                if (queryCallPricelist != null && queryCallPricelist.size() > 0) {
                    if (queryCallPricelist.get(0).get("property_value") != null) {
                        supplierPriceMap.put("callprice", String.valueOf(queryCallPricelist.get(0).get("property_value")));
                    }
                }
                //通过resourceID查询销售定价
                List<Map<String, Object>> querySmsPricelist = this.sqlQuery(sqlBuilder.toString(), supplierId, ConstantsUtil.SMS_TYPE);
                if (querySmsPricelist != null && querySmsPricelist.size() > 0) {
                    if (querySmsPricelist.get(0).get("property_value") != null) {
                        if (querySmsPricelist.get(0).get("property_value") != null) {
                            supplierPriceMap.put("smsprice", String.valueOf(querySmsPricelist.get(0).get("property_value")));
                        }
                    }
                }
                //通过resourceID查询销售定价
                List<Map<String, Object>> querySeatPricelist = this.sqlQuery(sqlBuilder.toString(), supplierId, ConstantsUtil.SEATS_TYPE);
                if (querySeatPricelist != null && querySeatPricelist.size() > 0) {
                    if (querySeatPricelist.get(0).get("property_value") != null) {
                        JSONObject jsonObject = JSON.parseObject(String.valueOf(querySeatPricelist.get(0).get("property_value")));
                        if (jsonObject != null) {
                            supplierPriceMap.put("seatprice", jsonObject.getDouble("seatPrice"));
                            supplierPriceMap.put("seatminutes", jsonObject.getDouble("seatMinutes"));
                        }
                    }
                }
                //通过resourceID查询快递销售定价
                List<Map<String, Object>> queryExpressPricelist = this.sqlQuery(sqlBuilder.toString(), supplierId, ConstantsUtil.EXPRESS_TYPE);
                if (queryExpressPricelist != null && queryExpressPricelist.size() > 0) {
                    if (queryExpressPricelist.get(0).get("property_value") != null) {
                        JSONObject jsonObject = JSON.parseObject(String.valueOf(queryExpressPricelist.get(0).get("property_value")));
                        if (jsonObject != null) {
                            supplierPriceMap.put("receivedPrice", jsonObject.getDouble("receivedPrice"));
                            supplierPriceMap.put("rejectionPrice", jsonObject.getDouble("rejectionPrice"));
                        }
                    }
                }
                //通过resourceID查询快递销售定价
                List<Map<String, Object>> queryJdFixPricelist = this.sqlQuery(sqlBuilder.toString(), supplierId, ConstantsUtil.JD_FIX_TYPE);
                if (queryJdFixPricelist != null && queryJdFixPricelist.size() > 0) {
                    if (queryJdFixPricelist.get(0).get("property_value") != null) {
                        JSONObject jsonObject = JSON.parseObject(String.valueOf(queryJdFixPricelist.get(0).get("property_value")));
                        if (jsonObject != null) {
                            supplierPriceMap.put("jdFixPrice", jsonObject.getDouble("jdFixPrice"));
                        }
                    }
                }
                if (StringUtil.isNotEmpty(custId)) {
                    List<Map<String, Object>> queryFixPricelist = this.sqlQuery(sqlBuilder.toString(), supplierId, ConstantsUtil.IDCARD_FIX_TYPE);
                    if (queryFixPricelist != null && queryFixPricelist.size() > 0) {
                        JSONObject jsonObject = JSON.parseObject(String.valueOf(queryFixPricelist.get(0).get("property_value")));
                        CustomerProperty customerProperty = customerDao.getProperty(custId, "industry");
                        if (jsonObject != null && customerProperty != null) {
                            if (customerProperty.getPropertyValue().equals("1")) {
                                supplierPriceMap.put("fixpriceBank", jsonObject.getDouble("fixpriceBank"));
                            }
                            if (customerProperty.getPropertyValue().equals("2")) {
                                supplierPriceMap.put("fixpriceInsurance", jsonObject.getDouble("fixpriceInsurance"));
                            }
                            if (customerProperty.getPropertyValue().equals("3")) {
                                supplierPriceMap.put("fixpriceCourt", jsonObject.getDouble("fixpriceCourt"));
                            }
                            if (customerProperty.getPropertyValue().equals("4")) {
                                supplierPriceMap.put("fixpriceOnline", jsonObject.getDouble("fixpriceOnline"));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            LOG.error("获取供应商定价集合error" + e.getMessage());
        }
        return supplierPriceMap;
    }

    /**
     * 查询供应商剩余包月分钟数
     *
     * @param userId
     * @param resourceId
     * @return
     * @throws Exception
     */
    public int getSourceSeatMinute(String userId, String resourceId) throws Exception {
        String sql = "SELECT property_value FROM t_customer_user_property WHERE user_id = ? AND property_name = ?";
        List<Map<String, Object>> seatSurplusMinuteList = this.sqlQuery(sql, userId, resourceId + "_minute");
        if (seatSurplusMinuteList != null && seatSurplusMinuteList.size() > 0 && StringUtil.isNotEmpty(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")))) {
            if (!"0".equals(String.valueOf(seatSurplusMinuteList.get(0).get("property_value"))) && !"0.0".equals(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")))) {
                return Integer.parseInt(String.valueOf(seatSurplusMinuteList.get(0).get("property_value")));
            }
        }
        return 0;
    }

    /**
     * 查询供应商通话单价
     *
     * @param custId
     * @param sourceId
     * @return
     * @throws Exception
     */
    public double getSourceCallPrice(String custId, String sourceId) throws Exception {
        Map<String, Object> callPrice = querySupplierPrice(custId, sourceId);
        if (callPrice != null && callPrice.get("callprice") != null) {
            return Double.parseDouble(String.valueOf(callPrice.get("callprice")));
        }
        return 0;
    }

    /**
     * 供应商坐席分钟数扣减
     *
     * @param resourceId
     * @param minutes
     * @return
     */
    public boolean accountSupplierMinuts(String resourceId, String supplierId, int minutes, String userId) throws Exception {
        //查询出坐席供应商剩余分钟数
        String supplierMinute = "SELECT property_value FROM t_customer_user_property WHERE property_name =? AND user_id = ?";
        List<Map<String, Object>> supplierMinuteList = this.sqlQuery(supplierMinute, new Object[]{resourceId + "_minute", userId});
        String seatMinutes = null;
        if (supplierMinuteList.size() > 0) {
            seatMinutes = String.valueOf(supplierMinuteList.get(0).get("property_value"));
        }
        // 处理供应商坐席分钟数属性不存在
        if (seatMinutes == null) {
            seatMinutes = "0";
            LOG.info(supplierId + " 供应商坐席分钟数属性不存在，开始新建");
            String insertSql = "INSERT INTO t_customer_user_property  (user_id, property_name, property_value,create_time) VALUES (?, ?, ?,NOW());";
            int status = this.executeUpdateSQL(insertSql, new Object[]{userId, resourceId + "_minute", seatMinutes});
            LOG.info("供应商id：" + supplierId + "\t供应商商坐席分钟数属性创建结果:" + status);
        }

        if (StringUtil.isNotEmpty(seatMinutes)) {
            String nowMinute = String.valueOf(Integer.valueOf(seatMinutes) - minutes);
            String updateSql = "UPDATE t_customer_user_property SET property_value = ? WHERE user_id = ? AND property_name = ?";
            this.executeUpdateSQL(updateSql, new Object[]{nowMinute, userId, resourceId + "_minute"});
            return true;
        }
        return false;
    }

    /**
     * 根据模板ID和资源类型获取营销模板
     *
     * @param templateId 模板ID
     * @param typeCode   资源类型（ 1.SMS 2.email 3.闪信）
     * @param custId
     * @return
     */
    public MarketTemplate getMarketTemplate(int templateId, int typeCode, String custId) {
        List<MarketTemplate> list = this.find("FROM MarketTemplate t WHERE t.id = ? AND t.typeCode = ? AND t.custId = ?", templateId, typeCode, custId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    /**
     * 获取资源的属性
     *
     * @param resourceId
     * @param propertyName
     * @return
     */
    public ResourcePropertyEntity getResourceProperty(String resourceId, String propertyName) {
        ResourcePropertyEntity mp = null;
        String hql = "from ResourcePropertyEntity m where m.resourceId=? and m.propertyName=?";
        List<ResourcePropertyEntity> list = this.find(hql, Integer.parseInt(resourceId), propertyName);
        if (list.size() > 0) {
            mp = list.get(0);
        }
        return mp;
    }

}
