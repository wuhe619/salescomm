package com.bdaim.api.dao;

import com.bdaim.api.entity.ApiEntity;
import com.bdaim.api.entity.ApiProperty;
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
    public void dealCustomerInfo(String apiId, String propertyName, String propertyValue) {
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
        List<ApiProperty> list = this.find(hql,  propertyName);
        return list;
    }

}
