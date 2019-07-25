package com.bdaim.customer.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.entity.BatchListEntity;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.entity.CustomerDO;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.price.dto.ResourcesPriceDto;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.slxf.exception.TouchException;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
@Component
public class CustomerDao extends SimpleHibernateDao<CustomerDO, String> {
    public CustomerProperty getProperty(String custId, String propertyName) {
        CustomerProperty cp = null;
        String hql = "from CustomerProperty m where m.custId=? and m.propertyName=?";
        List<CustomerProperty> list = this.find(hql, custId, propertyName);
        if (list.size() > 0)
            cp = (CustomerProperty) list.get(0);
        return cp;
    }

    public CustomerDO getCustMessage(String custId) {
        CustomerDO cp = null;
        String hql = "from CustomerDO m where m.custId=? and status =0 ";
        List<CustomerDO> list = this.find(hql, custId);
        if (list.size() > 0)
            cp = (CustomerDO) list.get(0);
        return cp;
    }

    public BatchListEntity getBatchMessage(String batchId) {
        BatchListEntity cp = null;
        String hql = "from BatchListEntity m where m.id=?";
        List<BatchListEntity> list = this.find(hql, batchId);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    /**
     * 账户扣费
     *
     * @param custId
     * @param amount 金额(分)
     * @return boolean
     * @author chengning@salescomm.net
     * @date 2018/10/9 10:58
     */
    public boolean accountDeductions(String custId, BigDecimal amount) {
        // 处理负值的情况
        if (amount.doubleValue() < 0) {
            amount = new BigDecimal(Math.abs(amount.doubleValue()));
        }
        boolean success = false;
        CustomerProperty remainAmount = this.getProperty(custId, "remain_amount");
        CustomerProperty usedAmount = this.getProperty(custId, "used_amount");
        if (remainAmount == null) {
            // 处理账户不存在
            remainAmount = new CustomerProperty();
            remainAmount.setCustId(custId);
            remainAmount.setPropertyValue("0");
            logger.info(custId + " 账户不存在开始新建账户信息");
            remainAmount.setPropertyName("remain_amount");
            this.saveOrUpdate(remainAmount);
        }
        if (usedAmount == null) {
            // 累计消费 处理账户不存在
            usedAmount = new CustomerProperty();
            usedAmount.setCustId(custId);
            usedAmount.setPropertyValue("0");
            logger.info(custId + " 账户不存在开始新建账户累计消费信息");
            usedAmount.setPropertyName("used_amount");
            this.saveOrUpdate(usedAmount);

        }
        if (StringUtil.isNotEmpty(remainAmount.getPropertyValue())) {
            if (Double.parseDouble(remainAmount.getPropertyValue()) <= 0) {
                logger.info(custId + " 账户余额:" + remainAmount + ",先执行扣减");
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
            logger.info("账户扣减,企业id：" + custId + "\t金额：" + amount.toString());
        }
        return success;
    }

    /**
     * 账户充值
     *
     * @param custId
     * @param amount 金额(分)
     * @return boolean
     * @author chengning@salescomm.net
     * @date 2018/10/9 10:58
     */
    public boolean accountRecharge(String custId, BigDecimal amount) {
        boolean success = false;
        CustomerProperty remainAmount = this.getProperty(custId, "remain_amount");
        if (remainAmount == null) {
            // 处理账户不存在,则重新创建对象保存账户
            remainAmount = new CustomerProperty();
            remainAmount.setCustId(custId);
            remainAmount.setCreateTime(new Timestamp(System.currentTimeMillis()));
            remainAmount.setPropertyValue(String.valueOf(amount.doubleValue()));
            logger.info(custId + " 账户不存在开始新建账户信息");
            remainAmount.setPropertyName("remain_amount");
            success = true;
        } else {
            if (StringUtil.isNotEmpty(remainAmount.getPropertyValue())) {
                DecimalFormat df = new DecimalFormat("#");
                BigDecimal remainAmountBigDecimal = new BigDecimal(remainAmount.getPropertyValue());
                String nowMoney = df.format(remainAmountBigDecimal.add(amount));
                remainAmount.setPropertyValue(nowMoney);
                success = true;
            }
        }
        if (success) {
            logger.info("账户充值,企业id：" + custId + "\t金额：" + amount.toString());
        }
        this.saveOrUpdate(remainAmount);
        return success;
    }

    /**
     * 获取账户余额
     *
     * @param custId
     * @date 2018/10/9 15:58
     */
    public Double remainMoney(String custId) throws TouchException {
        CustomerProperty remainAmount = this.getProperty(custId, "remain_amount");
        if (remainAmount == null) {
            logger.info(custId + " 账户不存在");
        } else {
            if (StringUtil.isNotEmpty(remainAmount.getPropertyValue())) {
                if (Double.parseDouble(remainAmount.getPropertyValue()) <= 0) {
                    throw new TouchException(custId + " 账户余额不足");
                } else {
                    DecimalFormat df = new DecimalFormat("0.00");
                    Double remainMoney = Double.valueOf(remainAmount.getPropertyValue());
                    return remainMoney;
                }
            }
        }
        return 0.00;
    }

    /**
     * 模糊查询 propertyName
     *
     * @param custId
     * @param propertyName
     * @return
     */
    public List<CustomerProperty> getPropertyLike(String custId, String propertyName) {
        CustomerProperty cp = null;
        String hql = "from CustomerProperty m where m.custId=? and m.propertyName LIKE '%" + propertyName + "'";
        List<CustomerProperty> list = this.find(hql, custId);
        return list;
    }

    /**
     * 企业属性编辑与新增
     */
    public boolean dealCustomerInfo(String custId, String propertyName, String propertyValue) {
        boolean success = false;
        CustomerProperty propertyInfo = this.getProperty(custId, propertyName);
        if (propertyInfo == null) {
            propertyInfo = new CustomerProperty();
            propertyInfo.setCreateTime(new Timestamp(new Date().getTime()));
            propertyInfo.setCustId(custId);
            propertyInfo.setPropertyValue(propertyValue);
            logger.info(custId + " 属性不存在，新建该属性" + "\tpropertyName:" + propertyName + "\tpropertyValue:" + propertyValue);
            propertyInfo.setPropertyName(propertyName);
            success = true;
        } else {
            if (StringUtil.isNotEmpty(propertyInfo.getPropertyValue())) {
                propertyInfo.setPropertyValue(propertyValue);
                success = true;
            }
        }
        this.saveOrUpdate(propertyInfo);
        return success;
    }

    public String getEnterpriseName(String custId) {
        try {
            CustomerDO cu = this.get(custId);
            if (cu != null)
                return cu.getEnterpriseName();
        } catch (Exception e) {

        }
        return "";
    }

    /**
     * 根据资源id查询企业资源配置信息
     *
     * @author:duanliying
     * @date: 2019/4/4 18:06
     */
    public ResourcesPriceDto getCustResourceMessageById(String resourceId, String custId) {
        ResourcesPriceDto resourcesPriceDto = null;
        String hql = "from CustomerProperty m where m.custId=? AND m.propertyName = ?";
        try {
            List<CustomerProperty> list = this.find(hql, custId, resourceId + "_config");
            if (list.size() > 0) {
                CustomerProperty cp = (CustomerProperty) list.get(0);
                if (StringUtil.isNotEmpty(cp.getPropertyValue())) {
                    resourcesPriceDto = JSONObject.parseObject(cp.getPropertyValue(), ResourcesPriceDto.class);
                    logger.info("获取资源数据" + resourcesPriceDto.toString() + "企业id是：" + custId + "资源id是：" + resourceId);
                }
            }
        } catch (Exception e) {
            logger.error("获取资源信息异常" + e);
        }
        return resourcesPriceDto;
    }


    /**
     * 根据供应商+资源类型+企业id查询资源配置信息
     *
     * @author:duanliying
     * @date: 2019/4/4 18:06
     */
    public ResourcesPriceDto getCustResourceMessageByIdAndType(String supplierId, int resourceType, String custId) {
        //根据供应商和资源类型查询资源id
        ResourcesPriceDto resourcesPriceDto = null;
        try {
            String resourceId = null;
            String sql = "from MarketResourceEntity m where m.supplierId=? and m.typeCode=?";
            List<MarketResourceEntity> ResourceList = this.find(sql, supplierId, resourceType);
            if (ResourceList.size() > 0) {
                resourceId = String.valueOf(ResourceList.get(0).getResourceId());
            }

            String hql = "from CustomerProperty m where m.custId=? AND m.propertyName = ?";
            List<CustomerProperty> list = this.find(hql, custId, resourceId + "_config");
            if (list.size() > 0) {
                CustomerProperty cp = (CustomerProperty) list.get(0);
                if (StringUtil.isNotEmpty(cp.getPropertyValue())) {
                    resourcesPriceDto = JSONObject.parseObject(cp.getPropertyValue(), ResourcesPriceDto.class);
                    logger.info("获取资源数据" + resourcesPriceDto.toString() + "企业id是：" + custId + "资源id是：" + resourceId);
                }
            }
        } catch (Exception e) {
            logger.error("获取资源信息异常" + e);
        }
        return resourcesPriceDto;
    }

    /**
     * 查询企业拥有的资源信息根据企业id和资源类型
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/16 11:57
     */
    public List<ResourcesPriceDto> getCustResourcesPriceDtoByIdAndType(int resourceType, String custId) {
        //根据供应商和资源类型查询资源id
        ResourcesPriceDto resourcesPriceDto = null;
        ArrayList<ResourcesPriceDto> resourcesPriceDtoList = new ArrayList<>();
        try {
            String resourceId = null;
            String sql = "from MarketResourceEntity m where m.typeCode=?";
            List<MarketResourceEntity> ResourceList = this.find(sql, resourceType);
            if (ResourceList.size() > 0) {
                for (int i = 0; i < ResourceList.size(); i++) {
                    resourceId = String.valueOf(ResourceList.get(i).getResourceId());
                    String hql = "from CustomerProperty m where m.custId=? AND m.propertyName = ?";
                    List<CustomerProperty> list = this.find(hql, custId, resourceId + "_config");
                    if (list.size() > 0) {
                        CustomerProperty cp = (CustomerProperty) list.get(0);
                        if (StringUtil.isNotEmpty(cp.getPropertyValue())) {
                            resourcesPriceDto = JSONObject.parseObject(cp.getPropertyValue(), ResourcesPriceDto.class);
                            logger.info("获取资源数据" + resourcesPriceDto.toString() + "企业id是：" + custId + "资源id是：" + resourceId);
                            if (resourcesPriceDto != null) {
                                resourcesPriceDtoList.add(resourcesPriceDto);
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error("获取资源信息异常" + e);
        }
        return resourcesPriceDtoList;
    }

    /**
     * 查询企业账单月统计信息
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/16 11:57
     */
    public List<Map<String, Object>> getCustStatBillMonth(String supplierId, int resourceType, String custId,String billDate,int billType) {
        //根据供应商和资源类型查询资源id
        List<Map<String, Object>> CustStatBillMonthList = null;
        try {
            MarketResourceEntity cp = null;
            String resourceId = null;
            String hql = "from MarketResourceEntity m where m.supplierId=? and m.typeCode=?";
            List<MarketResourceEntity> list = this.find(hql, supplierId, resourceType);
            if (list.size() > 0) {
                resourceId = String.valueOf(list.get(0).getResourceId());
            }
            logger.info("获取资源id是" + resourceId);
            String sql = "SELECT * FROM stat_bill_month WHERE cust_id =? AND stat_time =? AND resource_id =? AND bill_type =?";
            CustStatBillMonthList = this.sqlQuery(sql, custId, billDate, resourceId,billType);
            logger.info("查询账单信息" + JSON.toJSONString(CustStatBillMonthList));
        } catch (Exception e) {
            logger.error("获取资源信息异常" + e);
        }
        return CustStatBillMonthList;
    }
}
