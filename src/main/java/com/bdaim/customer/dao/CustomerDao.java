package com.bdaim.customer.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.entity.BatchListEntity;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customer.dto.ApparentNumberQueryParam;
import com.bdaim.customer.dto.CustomerDTO;
import com.bdaim.customer.entity.ApparentNumber;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customgroup.dto.CGroupImportParam;
import com.bdaim.price.dto.ResourcesPriceDto;
import com.bdaim.common.dto.Page;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.MessageFormat;
import java.util.*;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
@Component
public class CustomerDao extends SimpleHibernateDao<Customer, String> {

    private final static String U_INSERT_SQL = "replace INTO u (id,phone) VALUES(?,?)";

    private final static String CG_INSERT_SQL = " replace INTO  t_customer_group_list_{0} (id,super_data,status) VALUES(?,?,?)";


    public CustomerProperty getProperty(String custId, String propertyName) {
        CustomerProperty cp = null;
        String hql = "from CustomerProperty m where m.custId=? and m.propertyName=?";
        List<CustomerProperty> list = this.find(hql, custId, propertyName);
        if (list.size() > 0)
            cp = (CustomerProperty) list.get(0);
        return cp;
    }

    public Customer getCustMessage(String custId) {
        Customer cp = null;
        String hql = "from Customer m where m.custId=? and status =0 ";
        List<Customer> list = this.find(hql, custId);
        if (list.size() > 0)
            cp = (Customer) list.get(0);
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
    public void dealCustomerInfo(String custId, String propertyName, String propertyValue) {
        CustomerProperty propertyInfo = this.getProperty(custId, propertyName);
        if (propertyInfo == null) {
            propertyInfo = new CustomerProperty();
            propertyInfo.setCreateTime(new Timestamp(new Date().getTime()));
            propertyInfo.setCustId(custId);
            propertyInfo.setPropertyValue(propertyValue);
            logger.info(custId + " 属性不存在，新建该属性" + "\tpropertyName:" + propertyName + "\tpropertyValue:" + propertyValue);
            propertyInfo.setPropertyName(propertyName);
        } else {
                propertyInfo.setPropertyValue(propertyValue);
        }
        this.saveOrUpdate(propertyInfo);
    }

    public String getEnterpriseName(String custId) {
        try {
            Customer cu = this.get(custId);
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
    public List<Map<String, Object>> getCustStatBillMonth(String supplierId, int resourceType, String custId, String billDate, int billType) {
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
            CustStatBillMonthList = this.sqlQuery(sql, custId, billDate, resourceId, billType);
            logger.info("查询账单信息" + JSON.toJSONString(CustStatBillMonthList));
        } catch (Exception e) {
            logger.error("获取资源信息异常" + e);
        }
        return CustStatBillMonthList;
    }

    /**
     * 根据属性信查询企业信息
     *
     * @date: 2019/4/4 18:06
     */
    public List<Map<String, Object>> getCustIdByPropertyValue(String propertyName, String propertyValue) {
        String sql = "SELECT c.cust_id FROM t_customer c LEFT JOIN t_customer_property p ON c.cust_id = p.cust_id " +
                "WHERE c.`status` = 0 AND  p.property_name = ? AND p.property_value = ?";
        List<Map<String, Object>> list = null;
        try {
            list = this.sqlQuery(sql, propertyName, propertyValue);
        } catch (Exception e) {
            logger.error("获取资源信息异常" + e);
        }
        return list;
    }

    public Customer getName(String name) {
        Customer cp = null;
        String hql = "from Customer m where m.enterpriseName=? and status = 0";
        List<Customer> list = this.find(hql, name);
        if (list.size() > 0)
            cp = (Customer) list.get(0);
        return cp;
    }

    public List<CustomerProperty> getPropertyList(String custId, String propertyName) {
        CustomerProperty cp = null;
        String hql = "from CustomerProperty m where m.custId=? and m.propertyName like ?";
        List<CustomerProperty> list = this.find(hql, custId, propertyName);

        return list;
    }


    public List<CustomerProperty> getPropertyAllList(String custId) {
        CustomerProperty cp = null;
        String hql = "from CustomerProperty m where m.custId=?";
        List<CustomerProperty> list = this.find(hql, custId);
        return list;
    }

    /**
     * 根据状态获取客户列表
     *
     * @param status 状态（0正常 1.冻结 2资质未认证3.删除4.审核中5.审核失败)
     * @return
     */
    public Page listCustomer(int status, int pageNum, int pageSize) {
        String hql = "from Customer m where m.status=? ORDER BY m.createTime DESC ";
        List<Object> params = new ArrayList<>();
        params.add(status);
        return page(hql, params, pageNum, pageSize);
    }

    /**
     * 客户列表
     *
     * @param param
     * @return
     */
    public List<CustomerDTO> listCustomer(Customer param, String propertyName, String propertyValue) {
        StringBuffer hql = new StringBuffer("from Customer m where 1=1");
        List values = new ArrayList();
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            hql.append(" and m.enterpriseName like ?");
            values.add("%" + param.getEnterpriseName() + "%");
        }
        if (param.getStatus() >= 0) {
            hql.append(" and m.status = ?");
            values.add(param.getStatus());
        }
        if (StringUtil.isNotEmpty(propertyName) && StringUtil.isNotEmpty(propertyValue)) {
            hql.append(" and m.id IN (SELECT custId FROM CustomerProperty m where m.propertyName=? AND m.propertyValue = ? ) ");
            values.add(propertyName);
            values.add(propertyValue);
        } else if (StringUtil.isNotEmpty(propertyName)) {
            hql.append(" and m.id IN (SELECT custId FROM CustomerProperty m where m.propertyName=? ) ");
            values.add(propertyName);
        }
        List<Customer> list = this.find(hql.toString(), values);
        List<CustomerDTO> result = new ArrayList<>();
        if (list.size() > 0) {
            for (Customer m : list) {
                result.add(new CustomerDTO(m));
            }
        }
        return result;
    }


    /**
     * 查询单个外显号
     *
     * @param id
     * @return com.bdaim.sale.dto.ApparentNumber
     * @author chengning@salescomm.net
     * @date 2019/2/13 14:44
     */
    public ApparentNumber selectApparentNumber(int id) {
        StringBuffer hql = new StringBuffer();
        List<Object> params = new ArrayList<>();
        hql.append("from ApparentNumber m where m.id = ? ");
        params.add(id);
        List<ApparentNumber> list = this.find(hql.toString(), params);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }


    /**
     * 企业外显号列表
     *
     * @param model
     * @return com.bdaim.sale.dto.Page
     * @author chengning@salescomm.net
     * @date 2019/2/13 14:50
     */
    public List<ApparentNumber> listApparentNumber(ApparentNumberQueryParam model) {
        StringBuffer hql = new StringBuffer();
        List<Object> params = new ArrayList<>();
        hql.append("from ApparentNumber m where 1=1 ");
        if (StringUtil.isNotEmpty(model.getCustId())) {
            hql.append(" AND m.custId = ? ");
            params.add(model.getCustId());
        }
        if (StringUtil.isNotEmpty(model.getApparentNumber())) {
            hql.append(" AND m.apparentNumber = ? ");
            params.add(model.getApparentNumber());
        }
        if (StringUtil.isNotEmpty(model.getProvince())) {
            hql.append(" AND m.province = ?");
            params.add(model.getProvince());
        }
        if (StringUtil.isNotEmpty(model.getOperator())) {
            hql.append(" AND m.operator = ?");
            params.add(model.getOperator());
        }
        if (model.getStatus() != null) {
            hql.append(" AND m.status = ?");
            params.add(model.getStatus());
        }
        if (model.getStopStatus() != null) {
            hql.append(" AND m.stopStatus = ?");
            params.add(model.getStopStatus());
        }
        if (StringUtil.isNotEmpty(model.getCallChannel())) {
            hql.append(" AND m.callChannel = ?");
            params.add(model.getCallChannel());
        }
        if (StringUtil.isNotEmpty(model.getCallType())) {
            hql.append(" AND m.callType = ?");
            params.add(model.getCallType());
        }
        hql.append(" ORDER BY m.createTime DESC ");
        return this.find(hql.toString(), params);
    }

    public Page pageApparentNumber(ApparentNumberQueryParam model) {
        StringBuffer hql = new StringBuffer();
        List<Object> params = new ArrayList<>();
        hql.append("from ApparentNumber m where 1=1 ");
        if (StringUtil.isNotEmpty(model.getCustId())) {
            hql.append(" AND m.custId = ? ");
            params.add(model.getCustId());
        }
        if (StringUtil.isNotEmpty(model.getApparentNumber())) {
            hql.append(" AND m.apparentNumber = ? ");
            params.add(model.getApparentNumber());
        }
        if (StringUtil.isNotEmpty(model.getProvince())) {
            hql.append(" AND m.province = ?");
            params.add(model.getProvince());
        }
        if (StringUtil.isNotEmpty(model.getOperator())) {
            hql.append(" AND m.operator = ?");
            params.add(model.getOperator());
        }
        if (model.getStatus() != null) {
            hql.append(" AND m.status = ?");
            params.add(model.getStatus());
        }
        if (model.getStopStatus() != null) {
            hql.append(" AND m.stopStatus = ?");
            params.add(model.getStopStatus());
        }
        if (StringUtil.isNotEmpty(model.getCallChannel())) {
            hql.append(" AND m.callChannel = ?");
            params.add(model.getCallChannel());
        }
        if (StringUtil.isNotEmpty(model.getCallType())) {
            hql.append(" AND m.callType = ?");
            params.add(model.getCallType());
        }
        hql.append(" ORDER BY m.createTime DESC ");
        return this.page(hql.toString(), params, model.getPageNum(), model.getPageSize());
    }

    /**
     * 批量保存客户群数据表数据
     *
     * @param custGroupId
     * @param list
     * @return
     * @throws Exception
     */
    public int insertBatchDataGroupData(String custGroupId, List<CGroupImportParam> list) throws Exception {
        int[] status = jdbcTemplate.batchUpdate(MessageFormat.format(CG_INSERT_SQL, custGroupId), new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setString(1, list.get(i).getMd5Phone());
                preparedStatement.setString(2, list.get(i).getSuperData());
                preparedStatement.setInt(3, list.get(i).getStatus());
            }

            @Override
            public int getBatchSize() {
                return list.size();
            }
        });
        return status.length;
    }

    /**
     * 批量保存手机号
     *
     * @param list
     * @return
     * @throws Exception
     */
    public int insertBatchUData(List<CGroupImportParam> list) throws Exception {
        int[] status = jdbcTemplate.batchUpdate(U_INSERT_SQL, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement preparedStatement, int i) throws SQLException {
                preparedStatement.setString(1, list.get(i).getMd5Phone());
                preparedStatement.setString(2, list.get(i).getPhone());
            }

            @Override
            public int getBatchSize() {
                return list.size();
            }
        });
        return status.length;
    }

    /**
     * 查询讯众呼叫中心企业ID
     *
     * @param custId
     * @return
     */
    public Map<String, Object> selectXzCallCenterInfo(String custId) {
        Map<String, Object> data = new HashMap<>();
        String sql = "SELECT cust_id, property_value FROM t_customer_property where property_name = 'xz_callcenterid' AND cust_id = ? LIMIT 1";
        List<Map<String, Object>> list = this.sqlQuery(sql, custId);
        if (list != null && list.size() > 0) {
            data.put("cust_id", list.get(0).get("cust_id"));
            data.put("id", list.get(0).get("property_value"));
        }
        return data;
    }

    /**
     * 根据客户ID和资源ID查询客户短信售价
     *
     * @param custId
     * @param resourceId
     * @return
     * @throws Exception
     */
    public String selectCustSmsPrice(String custId, String resourceId) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT property_value FROM t_customer_property WHERE property_name='sms_config' AND cust_id = ? ");
        List<Map<String, Object>> list = this.sqlQuery(sql.toString(), custId);
        if (list.size() > 0) {
            String configs = String.valueOf(list.get(0).get("property_value"));
            JSONArray jsonArray = JSON.parseArray(configs);
            JSONObject jsonObject;
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (StringUtil.isNotEmpty(resourceId) && resourceId.equals(jsonObject.getString("resourceId"))) {
                    logger.info("客户ID:" + custId + ",资源ID:" + resourceId + "配置的短信资源:" + jsonObject);
                    return String.valueOf(NumberConvertUtil.changeY2L(jsonObject.getDouble("price")));
                }
            }
        }
        logger.warn("客户ID:" + custId + ",资源ID:" + resourceId + "未配置短信资源!");
        return null;
    }

    /**
     * 根据资源ID获取供应商短信定价
     *
     * @param resourceId
     * @return
     * @throws Exception
     */
    public String selectSupplierSmsPrice(String resourceId) throws Exception {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t2.property_value FROM t_market_resource t1 JOIN t_market_resource_property t2 ON t1.resource_id = t2.resource_id ");
        sql.append(" WHERE t1.`status` = 1 AND t1.type_code = 2 AND t2.property_name='price_config' AND t1.resource_id=?");
        List<Map<String, Object>> list = this.sqlQuery(sql.toString(), resourceId);
        if (list.size() > 0) {
            String config = String.valueOf(list.get(0).get("property_value"));
            JSONObject jsonObject = JSON.parseObject(config);
            logger.info("资源ID:" + resourceId + "配置的短信资源:" + jsonObject);
            return String.valueOf(NumberConvertUtil.changeY2L(jsonObject.getDouble("price")));
        }
        logger.warn("资源ID:" + resourceId + "未配置短信资源!");
        return null;
    }

}
