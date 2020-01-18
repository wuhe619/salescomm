package com.bdaim.resource.price.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.bill.dto.CustomerBillQueryParam;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.price.dto.SalePriceDTO;
import com.bdaim.resource.price.service.SalePriceService;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.supplier.dto.SupplierEnum;
import com.bdaim.util.ConstantsUtil;
import com.bdaim.util.DateUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.util.*;

/**
 * @author duanliying
 * @date 2018/10/15
 * @description
 */
@Service("SalePriceService")
@Transactional
public class SalePriceServiceImpl implements SalePriceService {
    private final static Logger LOG = LoggerFactory.getLogger(SalePriceServiceImpl.class);
    @Autowired
    CustomerDao customerDao;
    @Autowired
    CustomerUserDao customerUserDao;
    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    SourceDao sourceDao;
    @Resource
    MarketResourceService marketResourceServiceImpl;

    /**
     * 修改销售定价
     */
    @Override
    public void updateSalePrice(Map<String, List<SalePriceDTO>> salePricMap) {
        //获取修改的销售定价集合
        List<SalePriceDTO> salePricList = salePricMap.get("salePricList");
        String custId = salePricList.get(0).getCustId();
        String PropertyName = null;
        for (int i = 0; i < salePricList.size(); i++) {
            String channel = salePricList.get(i).getChannel();
            //根据渠道信息查询渠道枚举类
            SupplierEnum callIdPropertyName = SupplierEnum.getCallIdPropertyName(channel);
            PropertyName = callIdPropertyName.getSupplierName();
            if (channel.equals(ConstantsUtil.SUPPLIERID__CUC) || channel.equals(ConstantsUtil.SUPPLIERID__CMC) || channel.equals(ConstantsUtil.SUPPLIERID__CTC)) {
                CustomerProperty customer = customerDao.getProperty(custId, PropertyName + "_sms_price");
                if (salePricList.get(i).getSaleSmsPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getSaleSmsPrice())) {
                    if (customer == null) {
                        CustomerProperty cucSmsCustomer = new CustomerProperty();
                        cucSmsCustomer.setCustId(custId);
                        cucSmsCustomer.setPropertyName(PropertyName + "_sms_price");
                        cucSmsCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleSmsPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        cucSmsCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(cucSmsCustomer);
                    } else {
                        customer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleSmsPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        customer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(customer);
                    }
                }
                CustomerProperty callMinuteCustomer = customerDao.getProperty(custId, PropertyName + "_minute");
                if (salePricList.get(i).getSaleMinute() != null && StringUtil.isNotEmpty(salePricList.get(i).getSaleMinute())) {
                    if (callMinuteCustomer == null) {
                        CustomerProperty callMinute = new CustomerProperty();
                        callMinute.setCustId(custId);
                        callMinute.setPropertyName(PropertyName + "_minute");
                        callMinute.setPropertyValue(salePricList.get(i).getSaleMinute());
                        callMinute.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(callMinute);
                    } else {
                        callMinuteCustomer.setPropertyValue(salePricList.get(i).getSaleMinute());
                        customerDao.saveOrUpdate(callMinuteCustomer);
                    }
                    /*
                    //查询当前企业所有的坐席信息   更新坐席配置的打电话分钟数
                    String querySeats = "SELECT * from t_customer_user WHERE cust_id=? AND user_type='2'";
                    List<Map<String, Object>> seatsList = customerDao.sqlQuery(querySeats, custId);
                    if (seatsList.size() > 0) {
                        for (int j = 0; j < seatsList.size(); j++) {
                            //根据id查询cuc_minute
                            CustomerUserPropertyDO seatMinute = customerUserDao.getProperty(String.valueOf(seatsList.get(j).get("id")), PropertyName + "_minute");
                            if (seatMinute == null) {
                                CustomerUserPropertyDO customerUserProperty = new CustomerUserPropertyDO();
                                customerUserProperty.setUserId(String.valueOf(seatsList.get(j).get("id")));
                                customerUserProperty.setPropertyName(PropertyName + "_minute");
                                customerUserProperty.setPropertyValue(salePricList.get(i).getSaleMinute());
                                customerUserProperty.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
                                customerUserDao.saveOrUpdate(customerUserProperty);
                            } else {
                                seatMinute.setPropertyValue(salePricList.get(i).getSaleMinute());
                                customerUserDao.saveOrUpdate(seatMinute);
                            }
                        }
                    }*/

                }
                CustomerProperty fixCustomer = customerDao.getProperty(custId, PropertyName + "_fix_price");
                if (salePricList.get(i).getSalefixPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getSalefixPrice())) {
                    if (fixCustomer == null) {
                        CustomerProperty fixPriceCustomer = new CustomerProperty();
                        fixPriceCustomer.setCustId(custId);
                        fixPriceCustomer.setPropertyName(PropertyName + "_fix_price");
                        fixPriceCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSalefixPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        fixPriceCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(fixPriceCustomer);
                    } else {
                        fixCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSalefixPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        fixCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(fixCustomer);
                    }
                }
                CustomerProperty CallCustomer = customerDao.getProperty(custId, PropertyName + "_call_price");
                if (salePricList.get(i).getSaleCallPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getSaleCallPrice())) {
                    if (CallCustomer == null) {
                        CustomerProperty callPriceCustomer = new CustomerProperty();
                        callPriceCustomer.setCustId(custId);
                        callPriceCustomer.setPropertyName(PropertyName + "_call_price");
                        callPriceCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleCallPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        callPriceCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(callPriceCustomer);
                    } else {
                        CallCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleCallPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        CallCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(CallCustomer);
                    }
                }
                CustomerProperty seatCustomer = customerDao.getProperty(custId, PropertyName + "_seat_price");
                if (salePricList.get(i).getSaleSeatPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getSaleSeatPrice())) {
                    if (seatCustomer == null) {
                        CustomerProperty callPriceCustomer = new CustomerProperty();
                        callPriceCustomer.setCustId(custId);
                        callPriceCustomer.setPropertyName(PropertyName + "_seat_price");
                        callPriceCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleSeatPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        callPriceCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(callPriceCustomer);
                    } else {
                        seatCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleSeatPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        seatCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(seatCustomer);
                    }
                }
                CustomerProperty imeiCustomer = customerDao.getProperty(custId, PropertyName + "_imei_price");
                if (salePricList.get(i).getImeiPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getImeiPrice())) {
                    if (imeiCustomer == null) {
                        CustomerProperty imeiCustomerProperty = new CustomerProperty();
                        imeiCustomerProperty.setCustId(custId);
                        imeiCustomerProperty.setPropertyName(PropertyName + "_imei_price");
                        imeiCustomerProperty.setPropertyValue(new BigDecimal(salePricList.get(i).getImeiPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        imeiCustomerProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(imeiCustomerProperty);
                    } else {
                        imeiCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getImeiPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        imeiCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(imeiCustomer);
                    }
                }
                CustomerProperty macCustomer = customerDao.getProperty(custId, PropertyName + "_mac_price");
                if (salePricList.get(i).getMacPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getMacPrice())) {
                    if (macCustomer == null) {
                        CustomerProperty macCustomerProperty = new CustomerProperty();
                        macCustomerProperty.setCustId(custId);
                        macCustomerProperty.setPropertyName(PropertyName + "_mac_price");
                        macCustomerProperty.setPropertyValue(new BigDecimal(salePricList.get(i).getMacPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        macCustomerProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(macCustomerProperty);
                    } else {
                        macCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getMacPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        macCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(macCustomer);
                    }
                }
            } else if (channel.equals(SupplierEnum.XZ.getSupplierId())) {
                CustomerProperty CallCustomer = customerDao.getProperty(custId, PropertyName + "_call_price");
                if (salePricList.get(i).getSaleCallPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getSaleCallPrice())) {
                    if (CallCustomer == null) {
                        CustomerProperty callPriceCustomer = new CustomerProperty();
                        callPriceCustomer.setCustId(custId);
                        callPriceCustomer.setPropertyName(PropertyName + "_call_price");
                        if (salePricList.get(i).getSaleCallPrice() != null)
                            callPriceCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleCallPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        callPriceCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(callPriceCustomer);
                    } else {
                        CallCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleCallPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        CallCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(CallCustomer);
                    }
                }
                CustomerProperty seatCustomer = customerDao.getProperty(custId, PropertyName + "_seat_price");
                if (salePricList.get(i).getSaleSeatPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getSaleSeatPrice())) {
                    if (seatCustomer == null) {
                        CustomerProperty callPriceCustomer = new CustomerProperty();
                        callPriceCustomer.setCustId(custId);
                        callPriceCustomer.setPropertyName(PropertyName + "_seat_price");
                        callPriceCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleSeatPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        callPriceCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(callPriceCustomer);
                    } else {
                        seatCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSaleSeatPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        seatCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(seatCustomer);
                    }
                }
                CustomerProperty callMinuteCustomer = customerDao.getProperty(custId, PropertyName + "_minute");
                if (salePricList.get(i).getSaleMinute() != null && StringUtil.isNotEmpty(salePricList.get(i).getSaleMinute())) {
                    if (callMinuteCustomer == null) {
                        CustomerProperty callMinute = new CustomerProperty();
                        callMinute.setCustId(custId);
                        callMinute.setPropertyName(PropertyName + "_minute");
                        callMinute.setPropertyValue(salePricList.get(i).getSaleMinute());
                        callMinute.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(callMinute);
                    } else {
                        callMinuteCustomer.setPropertyValue(salePricList.get(i).getSaleMinute());
                        customerDao.saveOrUpdate(callMinuteCustomer);
                    }
                }
            }
            if (channel.equals(ConstantsUtil.SUPPLIERID__JD)) {
                PropertyName = "jd";
                CustomerProperty macCustomer = customerDao.getProperty(custId, PropertyName + "_address_price");
                if (salePricList.get(i).getAddressPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getAddressPrice())) {
                    if (macCustomer == null) {
                        CustomerProperty customerProperty = new CustomerProperty();
                        customerProperty.setCustId(custId);
                        customerProperty.setPropertyName(PropertyName + "_address_price");
                        customerProperty.setPropertyValue(new BigDecimal(salePricList.get(i).getAddressPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        customerProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(customerProperty);
                    } else {
                        macCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getAddressPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        macCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(macCustomer);
                    }
                }

                CustomerProperty failCustomer = customerDao.getProperty(custId, PropertyName + "_fail_price");
                if (salePricList.get(i).getFailPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getFailPrice())) {
                    if (failCustomer == null) {
                        CustomerProperty failCustomerProperty = new CustomerProperty();
                        failCustomerProperty.setCustId(custId);
                        failCustomerProperty.setPropertyName(PropertyName + "_fail_price");
                        failCustomerProperty.setPropertyValue(new BigDecimal(salePricList.get(i).getFailPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        failCustomerProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(failCustomerProperty);
                    } else {
                        failCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getFailPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        failCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(failCustomer);
                    }
                }
                CustomerProperty successCustomer = customerDao.getProperty(custId, PropertyName + "_success_price");
                if (salePricList.get(i).getSuccessPrice() != null && StringUtil.isNotEmpty(salePricList.get(i).getSuccessPrice())) {
                    if (successCustomer == null) {
                        CustomerProperty successCustomerProperty = new CustomerProperty();
                        successCustomerProperty.setCustId(custId);
                        successCustomerProperty.setPropertyName(PropertyName + "_success_price");
                        successCustomerProperty.setPropertyValue(new BigDecimal(salePricList.get(i).getSuccessPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        successCustomerProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(successCustomerProperty);
                    } else {
                        successCustomer.setPropertyValue(new BigDecimal(salePricList.get(i).getSuccessPrice()).multiply(new BigDecimal("100")).stripTrailingZeros().toPlainString());
                        successCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                        customerDao.saveOrUpdate(successCustomer);
                    }
                }
            }
        }
    }

  /*


    /**
     * @description 查询旧的销售价格
     * @author:duanliying
     * @method
     * @date: 2018/10/15 15:21
     *//*
    @Override
    public List<CustomerPropertyDO> getLabelSalePriceOld(SalePriceDTO salePriceDTO) {
        List<CustomerPropertyDO> priceList = salePriceDTO.getPriceList();
        String custId = salePriceDTO.getCustId();
        ArrayList<CustomerPropertyDO> oldPriceList = new ArrayList<>();
        if (priceList.size() > 0) {
            for (int i = 0; i < priceList.size(); i++) {
                String propertyName = priceList.get(i).getPropertyName();
                //根据属性名查询出属性值
                CustomerPropertyDO customerProperty = customerDao.getProperty(custId, propertyName);
                oldPriceList.add(customerProperty);
            }
        }
        return oldPriceList;
    }*/

    //将修改记录添加t_label_sale_price_modify_log表中
    @Override
    public void addLabelSalePriceModifyLog(List<CustomerProperty> oldPriceList, SalePriceDTO salePriceDTO, Long userId) {

    }

    /**
     * 查询销售定价
     */
    @Override
    public Map<String, Object> querySalePriceList(String custId) {
        Map<String, Object> map = new HashMap<>();
        //根据企业Id查询当前企业配置的渠道
        CustomerProperty channelProperty = customerDao.getProperty(custId, "channel");
        //根据不同渠道展示不同定价，使用，分割
        if (channelProperty != null) {
            String channel = channelProperty.getPropertyValue();
            String supplierName = null;
            String[] channels = channel.split(",");
            if (channels.length > 0) {
                for (int i = 0; i < channels.length; i++) {
                    if (channels[i].equals(SupplierEnum.CUC.getSupplierId())) {
                        supplierName = SupplierEnum.CUC.getSupplierName();
                    }
                    if (channels[i].equals(SupplierEnum.CMC.getSupplierId())) {
                        supplierName = SupplierEnum.CMC.getSupplierName();
                    }
                    if (channels[i].equals(SupplierEnum.CTC.getSupplierId())) {
                        supplierName = SupplierEnum.CTC.getSupplierName();
                    }
                    SalePriceDTO salePriceDTO = new SalePriceDTO();
                    if (channels[i].equals(SupplierEnum.CUC.getSupplierId()) || channels[i].equals(ConstantsUtil.SUPPLIERID__CMC) || channels[i].equals(ConstantsUtil.SUPPLIERID__CTC)) {
                        CustomerProperty saleSmsPrice = customerDao.getProperty(custId, supplierName + "_sms_price");
                        CustomerProperty saleMinute = customerDao.getProperty(custId, supplierName + "_minute");
                        CustomerProperty saleFixPrice = customerDao.getProperty(custId, supplierName + "_fix_price");
                        CustomerProperty saleCallPrice = customerDao.getProperty(custId, supplierName + "_call_price");
                        CustomerProperty saleSeatPrice = customerDao.getProperty(custId, supplierName + "_seat_price");
                        CustomerProperty saleImeiPrice = customerDao.getProperty(custId, supplierName + "_imei_price");
                        CustomerProperty saleMacPrice = customerDao.getProperty(custId, supplierName + "_mac_price");
                        salePriceDTO.setChannel(channels[i]);
                        if (saleSmsPrice != null) {
                            if (!saleSmsPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setSaleSmsPrice(String.valueOf(new BigDecimal(saleSmsPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setSaleSmsPrice("");
                        }
                        if (saleMinute != null) {
                            if (!saleMinute.getPropertyValue().equals("")) {
                                salePriceDTO.setSaleMinute(saleMinute.getPropertyValue());
                            }
                        } else {
                            salePriceDTO.setSaleMinute("");
                        }
                        if (saleFixPrice != null) {
                            if (!saleFixPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setSalefixPrice(String.valueOf(new BigDecimal(saleFixPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setSalefixPrice("");
                        }
                        if (saleCallPrice != null) {
                            if (!saleCallPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setSaleCallPrice(String.valueOf(new BigDecimal(saleCallPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setSaleCallPrice("");
                        }
                        if (saleSeatPrice != null) {
                            if (!saleSeatPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setSaleSeatPrice(String.valueOf(new BigDecimal(saleSeatPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setSaleSeatPrice("");
                        }
                        if (saleImeiPrice != null) {
                            if (!saleImeiPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setImeiPrice(String.valueOf(new BigDecimal(saleImeiPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setImeiPrice("");
                        }
                        if (saleMacPrice != null) {
                            if (!saleMacPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setMacPrice(String.valueOf(new BigDecimal(saleMacPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setMacPrice("");
                        }
                        map.put(supplierName + "SalePriceDTO", salePriceDTO);
                    }
                    if (channels[i].equals(SupplierEnum.JD.getSupplierId())) {
                        supplierName = SupplierEnum.JD.getSupplierName();
                        CustomerProperty saleAddressFixPrice = customerDao.getProperty(custId, supplierName + "_address_price");
                        CustomerProperty salefailPrice = customerDao.getProperty(custId, supplierName + "_fail_price");
                        CustomerProperty salesuccessprice = customerDao.getProperty(custId, supplierName + "_success_price");
                        salePriceDTO.setChannel(channels[i]);
                        if (saleAddressFixPrice != null) {
                            if (!saleAddressFixPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setAddressPrice(String.valueOf(new BigDecimal(saleAddressFixPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setAddressPrice("");
                        }
                        if (salefailPrice != null) {
                            if (!salefailPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setFailPrice(String.valueOf(new BigDecimal(salefailPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setFailPrice("");
                        }
                        if (salesuccessprice != null) {
                            if (!salesuccessprice.getPropertyValue().equals("")) {
                                salePriceDTO.setSuccessPrice(String.valueOf(new BigDecimal(salesuccessprice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setSuccessPrice("");
                        }
                        map.put(supplierName + "SalePriceDTO", salePriceDTO);
                    }

                    if (channels[i].equals(SupplierEnum.YD.getSupplierId())) {
                        supplierName = SupplierEnum.YD.getSupplierName();
                        CustomerProperty saleAddressFixPrice = customerDao.getProperty(custId, supplierName + "_address_price");
                        CustomerProperty salefailPrice = customerDao.getProperty(custId, supplierName + "_fail_price");
                        CustomerProperty salesuccessprice = customerDao.getProperty(custId, supplierName + "_success_price");
                        salePriceDTO.setChannel(channels[i]);
                        if (saleAddressFixPrice != null) {
                            if (!saleAddressFixPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setAddressPrice(String.valueOf(new BigDecimal(saleAddressFixPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setAddressPrice("");
                        }
                        if (salefailPrice != null) {
                            if (!salefailPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setFailPrice(String.valueOf(new BigDecimal(salefailPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setFailPrice("");
                        }
                        if (salesuccessprice != null) {
                            if (!salesuccessprice.getPropertyValue().equals("")) {
                                salePriceDTO.setSuccessPrice(String.valueOf(new BigDecimal(salesuccessprice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setSuccessPrice("");
                        }
                        map.put(supplierName + "SalePriceDTO", salePriceDTO);
                    }
                    if (channels[i].equals(SupplierEnum.XZ.getSupplierId())) {
                        supplierName = SupplierEnum.XZ.getSupplierName();
                        CustomerProperty saleCallPrice = customerDao.getProperty(custId, supplierName + "_call_price");
                        CustomerProperty saleMinute = customerDao.getProperty(custId, supplierName + "_minute");
                        CustomerProperty saleSeatPrice = customerDao.getProperty(custId, supplierName + "_seat_price");

                        salePriceDTO.setChannel(channels[i]);
                        if (saleCallPrice != null) {
                            if (!saleCallPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setSaleCallPrice(String.valueOf(new BigDecimal(saleCallPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setSaleCallPrice("");
                        }
                        if (saleMinute != null) {
                            if (!saleMinute.getPropertyValue().equals("")) {
                                salePriceDTO.setSaleMinute(String.valueOf(saleMinute.getPropertyValue()));
                            }
                        } else {
                            salePriceDTO.setSaleMinute("");
                        }
                        if (saleSeatPrice != null) {
                            if (!saleSeatPrice.getPropertyValue().equals("")) {
                                salePriceDTO.setSaleSeatPrice(String.valueOf(new BigDecimal(saleSeatPrice.getPropertyValue()).divide(new BigDecimal("100"))));
                            }
                        } else {
                            salePriceDTO.setSaleSeatPrice("");
                        }
                        map.put(supplierName + "SalePriceDTO", salePriceDTO);
                    }
                }
            }
            map.put("_message", "查询成功");
            map.put("result", "1");
        } else {
            map.put("_message", "未配置渠道信息");
            map.put("result", "0");
        }
        return map;
    }

    @Override
    public PageList getSalePriceList(PageParam page, CustomerBillQueryParam customerBillQueryParam) {
        StringBuilder sqlBuilder = new StringBuilder("SELECT t.cust_id,t.create_time,t.enterprise_name,t.status,\n" +
                "t2.REALNAME as realName,\n" +
                "cjc.mobile_num,\n" +
                "cjc.industry,\n" +
                "cjc.cuc_sms_price,cjc.cuc_minute,cjc.cuc_fix_price,cjc.cuc_call_price,cjc.cuc_seat_price,\n" +
                "cjc.ctc_sms_price,cjc.ctc_minute,cjc.ctc_fix_price,cjc.ctc_call_price,cjc.ctc_seat_price,\n" +
                "cjc.cmc_sms_price,cjc.cmc_minute,cjc.cmc_fix_price,cjc.cmc_call_price,cjc.cmc_seat_price\n" +
                " from t_customer t\n" +
                " LEFT JOIN t_customer_user t2   ON t.cust_id = t2.cust_id\n" +
                "LEFT JOIN (\n" +
                "  SELECT cust_id, \n" +
                "\tmax(CASE property_name WHEN 'cuc_sms_price'   THEN property_value ELSE '' END ) cuc_sms_price,\n" +
                "\tmax(CASE property_name WHEN 'cuc_minute'   THEN property_value ELSE '' END ) cuc_minute,\n" +
                "\tmax(CASE property_name WHEN 'cuc_fix_price'   THEN property_value ELSE '' END ) cuc_fix_price,\n" +
                "\tmax(CASE property_name WHEN 'cuc_call_price'   THEN property_value ELSE '' END ) cuc_call_price,\n" +
                "\tmax(CASE property_name WHEN 'cuc_seat_price'   THEN property_value ELSE '' END ) cuc_seat_price,\n" +
                "\tmax(CASE property_name WHEN 'ctc_sms_price'   THEN property_value ELSE '' END ) ctc_sms_price,\n" +
                "\tmax(CASE property_name WHEN 'ctc_minute'   THEN property_value ELSE '' END ) ctc_minute,\n" +
                "\tmax(CASE property_name WHEN 'ctc_fix_price'   THEN property_value ELSE '' END ) ctc_fix_price,\n" +
                "\tmax(CASE property_name WHEN 'ctc_call_price'   THEN property_value ELSE '' END ) ctc_call_price,\n" +
                "\tmax(CASE property_name WHEN 'ctc_seat_price'   THEN property_value ELSE '' END ) ctc_seat_price,\n" +
                "  max(CASE property_name WHEN 'cmc_sms_price'   THEN property_value ELSE '' END ) cmc_sms_price,\n" +
                "\tmax(CASE property_name WHEN 'cmc_minute'   THEN property_value ELSE '' END ) cmc_minute,\n" +
                "\tmax(CASE property_name WHEN 'cmc_fix_price'   THEN property_value ELSE '' END ) cmc_fix_price,\n" +
                "\tmax(CASE property_name WHEN 'cmc_call_price'   THEN property_value ELSE '' END ) cmc_call_price,\n" +
                "\tmax(CASE property_name WHEN 'cmc_seat_price'   THEN property_value ELSE '' END ) cmc_seat_price,\n" +
                "\tmax(CASE property_name WHEN 'industry'   THEN property_value ELSE '' END ) industry,\n" +
                "\tmax(CASE property_name WHEN 'mobile_num'   THEN property_value ELSE '' END ) mobile_num\n" +
                "   FROM t_customer_property p GROUP BY cust_id \n" +
                ") cjc ON t.cust_id = cjc.cust_id  where 1=1 and t2.user_type=1  ");
        List<Object> params = new ArrayList<>();
        if (StringUtil.isNotEmpty(customerBillQueryParam.getCustomerId())) {
            sqlBuilder.append(" AND t.cust_id LIKE ?");
            params.add("%" + customerBillQueryParam.getCustomerId() + "%");
        }
        if (StringUtil.isNotEmpty(customerBillQueryParam.getEnterpriseName())) {
            sqlBuilder.append(" AND t.enterprise_name LIKE ? ");
            params.add("%" + customerBillQueryParam.getEnterpriseName() + "%");
        }
        if (StringUtil.isNotEmpty(customerBillQueryParam.getRealname())) {
            sqlBuilder.append(" AND t2.REALNAME LIKE ? ");
            params.add("%" + customerBillQueryParam.getRealname() + "%");
        }
        if (StringUtil.isNotEmpty(customerBillQueryParam.getPhone())) {
            sqlBuilder.append(" AND cjc.mobile_num LIKE ? ");
            params.add("%" + customerBillQueryParam.getPhone() + "%");
        }
        if (StringUtil.isNotEmpty(customerBillQueryParam.getIndustry())) {
            sqlBuilder.append(" AND cjc.industry= ?");
            params.add(customerBillQueryParam.getIndustry());
        }
        sqlBuilder.append(" GROUP BY t.cust_id ");
        sqlBuilder.append(" ORDER BY t.create_time DESC ");
        return new Pagination().getPageData(sqlBuilder.toString(), params.toArray(), page, jdbcTemplate);
    }

    /**
     * @description 验证是否添加了销售定价公共方法（坐席价格和修复价格）
     * @author:duanliying
     * @method
     * @date: 2018/11/6 10:14
     */
    @Override
    public Map<String, String> checkSalePrice(String custId, String channel) {
        String code = "1";
        String propertyName = null;
        String propertyValue = null;
        HashMap<String, String> map = new HashMap<>();
        List<String> channels = Arrays.asList(channel.split(","));
        if (channels.size() > 0) {
            for (int i = 0; i < channels.size(); i++) {
                if (channels.get(i).equals("2")) {
                    propertyValue = "cuc";
                    propertyName = "联通";
                }
                if (channels.get(i).equals("3")) {
                    propertyValue = "ctc";
                    propertyName = "电信";
                }
                if (channels.get(i).equals("4")) {
                    propertyValue = "cmc";
                    propertyName = "移动";
                }

                CustomerProperty fixPrice = customerDao.getProperty(custId, propertyValue + "_fix_price");
                if (fixPrice != null) {
                    code = "0";
                } else {
                    //没有配置销售定价
                    code = "1";
                    break;
                }
            }
        }
        if (code.equals("1")) {
            map.put("code", code);
            map.put("message", propertyName + "未配置销售定价，请联系管理员进行设置");
        } else {
            map.put("code", code);
            map.put("message", propertyName + "已配置销售定价");
        }

        return map;
    }

    @Override
    public String checkCustPrice(String compId, String resourceId) {
        String code = "0";
        HashMap<String, String> map = new HashMap<>();
        JSONObject customerMarketResource = null;
        try {
            //查询企业销售定价
            customerMarketResource = marketResourceServiceImpl.getCustomerMarketResource(compId, resourceId);
            if (customerMarketResource != null) {
                LOG.info("获取企业配置参数信息是:" + customerMarketResource.toJSONString());
                if (customerMarketResource.get("price") != null) {
                    code = "1";
                }
            }
        } catch (Exception e) {
            LOG.error("获取企业配置参数异常:", e);
            throw new RuntimeException("获取企业配置参数异常", e);
        }
        return code;
    }

     /*   String code = "0";
        HashMap<String, String> map = new HashMap<>();
        JSONObject customerMarketResource = null;
        try {
            Integer typeCode = null;
            if (StringUtil.isNotEmpty(resourceId)) {
                //根据resourceId查询type
                MarketResourceEntity sourceMessage = sourceDao.getSourceMessage(Integer.parseInt(resourceId));
                if (sourceMessage != null) {
                    typeCode = sourceMessage.getTypeCode();
                }
            }
            //查询企业销售定价
            customerMarketResource = marketResourceServiceImpl.getCustomerMarketResource(compId, resourceId);
            if (customerMarketResource != null) {
                LOG.info("获取企业配置参数信息是:" + customerMarketResource.toJSONString());
                if (customerMarketResource.get(ResourceEnum.getResource(typeCode).getPrice()) != null) {
                    code = "1";
                }
            }
        } catch (Exception e) {
            LOG.error("获取企业配置参数异常:", e);
            throw new RuntimeException("获取企业配置参数异常", e);
        }
        return code;
    }*/

    @Override
    public Map<String, String> SalePrice(String compId, String channel, int certifyType) {
        String code = "1";
        String propertyName = null;
        String propertyValue = null;
        HashMap<String, String> map = new HashMap<>();
        List<String> channels = Arrays.asList(channel.split(","));
        if (channels.size() > 0) {
            for (int i = 0; i < channels.size(); i++) {
                if (channels.get(i).equals("2")) {
                    propertyValue = "cuc";
                    propertyName = "联通";
                }
                if (channels.get(i).equals("3")) {
                    propertyValue = "ctc";
                    propertyName = "电信";
                }
                if (channels.get(i).equals("4")) {
                    propertyValue = "cmc";
                    propertyName = "移动";
                }

                if (certifyType == 0) {
                    CustomerProperty fixPrice = customerDao.getProperty(compId, propertyValue + "_fix_price");
                    if (fixPrice != null) {
                        code = "0";
                    } else {
                        //没有配置销售定价
                        code = "1";
                        break;
                    }
                }
                if (certifyType == 1) {
                    CustomerProperty fixPrice = customerDao.getProperty(compId, propertyValue + "_imei_price");
                    if (fixPrice != null) {
                        code = "0";
                    } else {
                        //没有配置销售定价
                        code = "1";
                        break;
                    }
                }
                if (certifyType == 2) {
                    CustomerProperty fixPrice = customerDao.getProperty(compId, propertyValue + "_mac_price");
                    if (fixPrice != null) {
                        code = "0";
                    } else {
                        //没有配置销售定价
                        code = "1";
                        break;
                    }
                }


            }
        }
        if (code.equals("1")) {
            map.put("code", code);
            map.put("message", propertyName + "未配置销售定价，请联系管理员进行设置");
        } else {
            map.put("code", code);
            map.put("message", propertyName + "已配置销售定价");
        }

        return map;
    }
}
