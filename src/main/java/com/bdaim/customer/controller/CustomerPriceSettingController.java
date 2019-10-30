package com.bdaim.customer.controller;

import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.dto.PriceDTO;
import com.bdaim.customer.service.CustomerPriceSettingService;
import com.bdaim.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/priceSetting")
public class CustomerPriceSettingController extends BasicAction {

    @Autowired
    private CustomerPriceSettingService customerPriceSettingService;

    @GetMapping("/queryPrice")
    public ResponseInfo queryPrice(String custId) {
        ResponseInfo resp = new ResponseInfo();
        if (StringUtil.isEmpty(custId)) {
            return new ResponseInfoAssemble().failure(-1, "企业ID不能为空");
        }
        resp.setData(customerPriceSettingService.queryPrice(custId));
        return resp;
    }

    @PostMapping("/updatePrice")
    public ResponseInfo updatePrice(@RequestBody PriceDTO priceDto) {
        if (StringUtil.isEmpty(priceDto.getCustId())) {
            return new ResponseInfoAssemble().failure(-1, "企业ID不能为空");
        }
        ResponseInfo resp = new ResponseInfo();
        if (!"admin".equals(opUser().getRole()) ) {
            return new ResponseInfoAssemble().failure(-1, "当前用户不能修改售价");
        }
        resp.setData(customerPriceSettingService.updatePrice(priceDto));
        return resp;
    }
}
