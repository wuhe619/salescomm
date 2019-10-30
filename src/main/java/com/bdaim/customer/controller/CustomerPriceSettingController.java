package com.bdaim.customer.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.customer.dto.PriceDTO;
import com.bdaim.customer.service.CustomerPriceSettingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/priceSetting")
public class CustomerPriceSettingController extends BasicAction {

    @Autowired
    private CustomerPriceSettingService customerPriceSettingService;
    @GetMapping("/queryPrice")
    public String queryPrice(String custId){
        return  customerPriceSettingService.queryPrice(custId);
    }

    @PostMapping("/updatePrice")
    public String updatePrice(@RequestBody PriceDTO priceDto){
        return  customerPriceSettingService.updatePrice(priceDto);
    }
}
