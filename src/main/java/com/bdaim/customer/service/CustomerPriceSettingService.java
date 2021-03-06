package com.bdaim.customer.service;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.exception.ParamException;
import com.bdaim.customer.dao.CustomerPropertyRepository;
import com.bdaim.customer.dto.PriceDTO;
import com.bdaim.customer.entity.CustomerProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class CustomerPriceSettingService {
    @Autowired
    private CustomerPropertyRepository customerPropertyRepository;

    public PriceDTO queryPrice(String custId) {
        CustomerProperty property =
                customerPropertyRepository.findByCustIdAndPropertyName(custId, "31_config");
        if (property == null) {
            property = savePrice(custId);
        }
        PriceDTO priceDto = new PriceDTO(property);
        return priceDto;
    }

    public String updatePrice(PriceDTO priceDto) {
        Pattern pattern = Pattern.compile("-?[0-9]+\\.?[0-9]*");

        Matcher isNum = pattern.matcher(priceDto.getPrice());
        if (!isNum.matches()) {
            throw new ParamException("设置售价必须为数字");
        }
        CustomerProperty property =
                customerPropertyRepository.findByCustIdAndPropertyName(priceDto.getCustId(), "31_config");
        if (property == null) {
            property = savePrice(priceDto.getCustId());
        }
        property.setPropertyValue(priceDto.getPrice());
        customerPropertyRepository.save(property);
        return "";
    }

    public CustomerProperty savePrice(String custId) {
        CustomerProperty property = new CustomerProperty();
        property.setCustId(custId);
        property.setPropertyName("31_config");
        property.setPropertyValue("");
        property.setCreateTime(new Timestamp(new Date().getTime()));
        customerPropertyRepository.save(property);

        return property;
    }
}
