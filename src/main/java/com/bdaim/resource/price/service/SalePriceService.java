package com.bdaim.resource.price.service;

import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.resource.price.dto.SalePriceDTO;

import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2018/10/15
 * @description
 */
public interface SalePriceService {
    //修改销售定价

    /**
     * 修改销售定价
     */
    void updateSalePrice(Map<String, List<SalePriceDTO>> salePricMap);

    /**
     * 查询旧的销售价格
     */
    // List<CustomerPropertyDO> getLabelSalePriceOld(SalePriceDTO salePriceDTO);

    //更新t_label_sale_price_modify_log  修改价格记录表
    void addLabelSalePriceModifyLog(List<CustomerProperty> oldPriceList, SalePriceDTO salePriceDTO, Long userId);

    /**
     * 查询销售定价
     */
    Map<String, Object> querySalePriceList(String custId);

    PageList getSalePriceList(PageParam page, CustomerBillQueryParam customerBillQueryParam);

    /**
     * 验证当前企业是否配置了销售定价
     */
    Map<String, String> checkSalePrice(String custId, String channel);

    String checkCustPrice(String compId, String resourceId);

    Map<String, String> SalePrice(String compId, String channel, int certifyType);
}
