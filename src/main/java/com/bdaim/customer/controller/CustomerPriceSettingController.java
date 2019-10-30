package com.bdaim.customer.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.customer.service.CustomerPriceSettingService;
import com.bdaim.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/priceSetting")
public class CustomerPriceSettingController extends BasicAction {

    @Autowired
    private CustomerPriceSettingService customerPriceSettingService;
    @GetMapping("/queryPrice")
    public String queryPrice(String custId){
        return  customerPriceSettingService.queryPrice(custId);
    }

    @PutMapping("/updatePrice")
    public String updatePrice(String custId,String price){
        return  customerPriceSettingService.updatePrice(custId,price);
    }
}
