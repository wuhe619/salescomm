package com.bdaim.customer.service;

import com.alibaba.fastjson.JSON;
import com.bdaim.customer.dao.CustomerPropertyRepository;
import com.bdaim.customer.dto.PriceDTO;
import com.bdaim.customer.entity.CustomerProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
public class CustomerPriceSettingService {
    @Autowired
    private CustomerPropertyRepository customerPropertyRepository;

    public String queryPrice(String custId) {
        CustomerProperty property =
                customerPropertyRepository.findByCustIdAndPropertyName(custId, "10000_config");
        if (property == null) {
            property = savePrice(custId);
        }
        Map<String, Object> map = new HashMap<>();
        map.put("code", 200);
        map.put("propertyValue", property.getPropertyValue());
        return JSON.toJSONString(map);
    }

    public String updatePrice(PriceDTO priceDto) {
        CustomerProperty property =
                customerPropertyRepository.findByCustIdAndPropertyName(priceDto.getCustId(), "10000_config");
        if (property == null) {
            property = savePrice(priceDto.getCustId());
        }
        property.setPropertyValue(priceDto.getPrice());
        Map<String, Object> map = new HashMap<>();
        customerPropertyRepository.save(property);
        map.put("code", 200);
        map.put("propertyValue", property.getPropertyValue());
        return JSON.toJSONString(map);
    }

    public CustomerProperty savePrice(String custId) {
        CustomerProperty property = new CustomerProperty();
        property.setCustId(custId);
        property.setPropertyName("10000_config");
        property.setPropertyValue("0");
        property.setCreateTime(new Timestamp(new Date().getTime()));
        customerPropertyRepository.save(property);

        return property;
    }
}
