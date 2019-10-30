package com.bdaim.customer.dao;

import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerPropertyPK;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerPropertyRepository extends JpaRepository<CustomerProperty,CustomerPropertyPK> {

    CustomerProperty findByCustIdAndPropertyName(String custId,String propertyName);
}
