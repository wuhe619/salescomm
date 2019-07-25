package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.entity.CustomerUserProperty;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

@Component
public class CustomerUserDao extends SimpleHibernateDao<CustomerUserDO, Serializable> {
    public CustomerUserProperty getProperty(String userId, String propertyName) {
        CustomerUserProperty cp = null;
        String hql = "from CustomerUserProperty m where m.userId=? and m.propertyName=?";
        List<CustomerUserProperty> list = this.find(hql, userId, propertyName);
        if (list.size() > 0)
            cp = (CustomerUserProperty) list.get(0);
        return cp;
    }

    public List<CustomerUserProperty> getAllProperty(String userId) {
        String hql = "from CustomerUserProperty m where m.userId=? ";
        List<CustomerUserProperty> list = this.find(hql, userId);
        return list;
    }

    public List<CustomerUserDO> getPropertyByType(int userType, String custId) {
        CustomerUserDO cp = null;
        String hql = "from CustomerUserDO m where m.userType=? and m.cust_id=?";
        List<CustomerUserDO> list = this.find(hql, userType, custId);
        return list;
    }

    public CustomerUserDO getPropertyByCustId(String custId) {
        CustomerUserDO cp = null;
        String hql = "from CustomerUserDO m where m.userType=1 and m.cust_id=?";
        List<CustomerUserDO> list = this.find(hql, custId);
        if (list.size() > 0)
            cp = (CustomerUserDO) list.get(0);
        return cp;
    }
    public CustomerUserDO getCustomer(String account,String custId) {
        CustomerUserDO cp = null;
        String hql = "from CustomerUserDO m where m.account=? AND m.cust_id = ?";
        List<CustomerUserDO> list = this.find(hql,account,custId);
        if (list.size() > 0)
            cp = (CustomerUserDO) list.get(0);
        return cp;
    }

    public List<CustomerUserDO> getAllByCustId(String custId) {
        CustomerUserDO cp = null;
        String hql = "from CustomerUserDO m where m.cust_id=?";
        List<CustomerUserDO> list = this.find(hql, custId);
        return list;
    }

    public String getName(String userId) {
        try {
            CustomerUserDO cu = this.get(Long.parseLong(userId));
            if (cu != null)
                return cu.getRealname();
        } catch (Exception e) {

        }
        return "";
    }

    public String getLoginName(String userId) {
        try {
            CustomerUserDO cu = this.get(Long.parseLong(userId));
            if (cu != null)
                return cu.getAccount();
        } catch (Exception e) {

        }
        return "";
    }
}
