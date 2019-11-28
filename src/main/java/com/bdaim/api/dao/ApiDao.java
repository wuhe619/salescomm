package com.bdaim.api.dao;

import com.bdaim.api.entity.ApiEntity;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.supplier.entity.SupplierEntity;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;

@Component
public class ApiDao extends SimpleHibernateDao<ApiEntity, Integer> {

    public ApiEntity getApi(int apiId) {
        String hql = "from ApiEntity m where m.apiId=? AND m.status = 0";
        List<ApiEntity> list = this.find(hql, apiId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
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

    public CustomerProperty getProperty(String custId, String propertyName) {
        CustomerProperty cp = null;
        String hql = "from CustomerProperty m where m.custId=? and m.propertyName=?";
        List<CustomerProperty> list = this.find(hql, custId, propertyName);
        if (list.size() > 0)
            cp = (CustomerProperty) list.get(0);
        return cp;
    }

}
