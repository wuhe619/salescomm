package com.bdaim.customer.service;

import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.PageList;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.customer.controller.CustomerPropertyAction;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerPropertyDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerPropertyEntity;
import com.bdaim.customer.entity.CustomerPropertyParam;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.supplier.entity.SupplierEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/7
 * @description
 */
@Service("customerPropertyService")
@Transactional
public class CustomerPropertyService {
    private static Logger logger = LoggerFactory.getLogger(CustomerPropertyAction.class);

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private CustomerPropertyDao customerPropertyDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private SupplierDao supplierDao;


    private final static DateTimeFormatter YYYYMMDDHHMMSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    
    public int save(CustomerPropertyParam customerPropertyParam) {
        CustomerPropertyEntity customerPropertyEntity = new CustomerPropertyEntity();
        customerPropertyEntity.setCustId(customerPropertyParam.getCustomerId());
        customerPropertyEntity.setUserId(customerPropertyParam.getUserId());
        customerPropertyEntity.setLabelId(Long.toString(IDHelper.getID()));
        customerPropertyEntity.setLabelName(customerPropertyParam.getLabelName());
        customerPropertyEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        customerPropertyEntity.setStatus("1");
        customerPropertyEntity.setLabelDesc(customerPropertyParam.getLabelDesc());
        customerPropertyEntity.setType(customerPropertyParam.getType());
        customerPropertyEntity.setOption(customerPropertyParam.getOption());
        if ((Long) customerPropertyDao.saveReturnPk(customerPropertyEntity) > 0) {
            return 1;
        }
        return 0;
    }

    
    public int update(CustomerPropertyParam customerPropertyParam) {
        logger.info("modify 自建属性 "+customerPropertyParam.toString());
        if (customerPropertyParam.getLabelId() != null) {
            CustomerPropertyEntity customerPropertyEntity = customerPropertyDao.findUniqueBy("labelId", customerPropertyParam.getLabelId());
            logger.info("查询结果"+customerPropertyEntity.toString());
            if (customerPropertyEntity != null) {
                customerPropertyEntity.setCustId(customerPropertyParam.getCustomerId());
                customerPropertyEntity.setUserId(customerPropertyParam.getUserId());
                if (StringUtil.isNotEmpty(customerPropertyParam.getLabelName())) {
                    customerPropertyEntity.setLabelName(customerPropertyParam.getLabelName());
                }
                if (StringUtil.isNotEmpty(customerPropertyParam.getStatus())) {
                    customerPropertyEntity.setStatus(customerPropertyParam.getStatus());
                }
                if (StringUtil.isNotEmpty(customerPropertyParam.getLabelDesc())) {
                    customerPropertyEntity.setLabelDesc(customerPropertyParam.getLabelDesc());
                }
                if (customerPropertyParam.getType() != null) {
                    customerPropertyEntity.setType(customerPropertyParam.getType());
                }
                if (StringUtil.isNotEmpty(customerPropertyParam.getOption())) {
                    customerPropertyEntity.setOption(customerPropertyParam.getOption());
                }
                customerPropertyDao.update(customerPropertyEntity);
                return 1;
            }
        }
        return 0;
    }

    
    public PageList pageList(PageParam page, CustomerPropertyParam customerPropertyParam) {
        StringBuilder sqlBuilder = new StringBuilder();
        sqlBuilder.append("  SELECT t1.id, t1.cust_id, t1.user_id userId, t1.label_id labelId, t1.status, t1.label_name labelName,")
                .append("  t1.create_time createTime, t1.label_desc labelDesc, t1.type, t1.`option`, COUNT(t2.super_id) AS useCount")
                .append("  FROM  t_customer_label t1")
                .append("  LEFT JOIN t_super_label  t2  ON t1.label_id = t2.label_id ")
                .append("  WHERE 1=1");
        if (customerPropertyParam.getCustomerId() != null) {
            sqlBuilder.append(" AND t1.cust_id = '" + customerPropertyParam.getCustomerId() + "'");
        }
        if (customerPropertyParam.getUserId() != null) {
            sqlBuilder.append(" AND t1.user_id = " + customerPropertyParam.getUserId());
        }
        if (StringUtil.isNotEmpty(customerPropertyParam.getLabelId())) {
            sqlBuilder.append(" AND t1.label_id = " + customerPropertyParam.getLabelId());
        }
        if (StringUtil.isNotEmpty(customerPropertyParam.getLabelName())) {
            sqlBuilder.append(" AND t1.label_name LIKE '%" + customerPropertyParam.getLabelId() + "%'");
        }
        sqlBuilder.append("  GROUP BY t1.id");
        sqlBuilder.append("  ORDER BY t1.create_time DESC");

        return new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
    }

    
    public List<Map<String, Object>> getSelectedLabelsBySuperId(String superId, String customerGroupId) {
        StringBuffer sb = new StringBuffer();
        sb.append("  SELECT  CAST(t1.label_id AS CHAR) label_id, t2.type FROM t_super_label t1")
                .append("  LEFT JOIN t_customer_label t2")
                .append("  ON t1.label_id = t2.label_id")
                .append("  WHERE 1=1  AND t2.status =1")
                .append("  and  t1.super_id  = ? ")
                .append("  AND  t1.cust_group_id = ?");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString(), superId, customerGroupId);
        // 处理自定义属性和标签的对应关系
        String superLabelSql = "SELECT option_value FROM t_super_label WHERE label_id = ? AND super_id = ? AND cust_group_id = ? ";
        List<Map<String, Object>> superLabelOptionList;
        for (Map<String, Object> superLabel : list) {
            if (superLabel.get("label_id") != null) {
                superLabelOptionList = jdbcTemplate.queryForList(superLabelSql, new Object[]{superLabel.get("label_id"), superId, customerGroupId});
                if (superLabelOptionList.size() > 0 && superLabelOptionList.get(0).get("option_value") != null) {
                    //文本框不拆分为数组
                    if ("1".equals(String.valueOf(superLabel.get("type")))) {
                        superLabel.put("optionValue", superLabelOptionList.get(0).get("option_value"));
                    } else {
                        superLabel.put("optionValue", String.valueOf(superLabelOptionList.get(0).get("option_value")).split(","));
                    }
                }
            }
        }
        return list;
    }

    
    public String getListenterprise(String custId, String channel) {
        Map<String, Object> map = new HashMap<>();
        String channelProperty = null;
        String propertyValue=null;
        //cust_id 和 渠道
        if (StringUtil.isNotEmpty(channel)) {
            if (channel.equals("2")) {
                channelProperty = "cuc";
            }
            if (channel.equals("4")) {
                channelProperty = "cmc";
            }
            if (channel.equals("3")) {
                channelProperty = "ctc";
            }
        }
        CustomerProperty enterprisepassword = customerDao.getProperty(custId, channelProperty + "_enterprise_password");
      if (enterprisepassword!=null){
          propertyValue = enterprisepassword.getPropertyValue();
      }

        return propertyValue;
    }

    
    public void addenterprise(CustomerProperty customerProperty) throws Exception {

        CustomerProperty enterprisepassword = customerDao.getProperty(customerProperty.getCustId(), customerProperty.getPropertyName());
        if (enterprisepassword == null) {
            CustomerProperty updateenterprise = new CustomerProperty();
            updateenterprise.setCustId(customerProperty.getCustId());
            updateenterprise.setPropertyName(customerProperty.getPropertyName());
            updateenterprise.setPropertyValue(customerProperty.getPropertyValue());
            updateenterprise.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            customerDao.saveOrUpdate(updateenterprise);
        } else {
            enterprisepassword.setPropertyValue(customerProperty.getPropertyValue());
            customerDao.saveOrUpdate(enterprisepassword);
        }
    }
    
    public List<Map<String, Object>> listCustSupplier(String custId) {
        List<Map<String, Object>> channellList = new ArrayList<>();
        if (StringUtil.isNotEmpty(custId)) {
            CustomerProperty channelProperty = customerDao.getProperty(custId, "channel");
            String channel = channelProperty.getPropertyValue();
            if (StringUtil.isNotEmpty(channel)) {
                String[] channels = channel.split(",");
                if (channels.length > 0) {
                    for (int i = 0; i < channels.length; i++) {
                        Map<String, Object> map = new HashMap<>();
                        //根据供应商id查询供应商名字
                        SupplierEntity supplier = supplierDao.getSupplierList(Integer.parseInt(channels[i]));
                        map.put("supplierId", channels[i]);
                        if (supplier != null) {
                            map.put("supplierName", supplier.getName());
                        }
                        channellList.add(map);
                    }

                }
            }
        }
        return channellList;
    }

}
