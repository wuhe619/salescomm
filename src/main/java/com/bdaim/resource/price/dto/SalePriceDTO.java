package com.bdaim.resource.price.dto;

import java.io.Serializable;

/**
 * @author duanliying
 * @date 2018/10/15
 * @description
 */
public class SalePriceDTO implements Serializable {
    private String channel;
    private String custId;
    private String saleSmsPrice;
    private String saleMinute;
    private String salefixPrice;
    private String saleCallPrice;
    private String saleSeatPrice;
    private String imeiPrice;
    private String macPrice;
    private String addressPrice;
    private String failPrice;
    private String successPrice;

    public String getImeiPrice() {
        return imeiPrice;
    }

    public void setImeiPrice(String imeiPrice) {
        this.imeiPrice = imeiPrice;
    }

    public String getMacPrice() {
        return macPrice;
    }

    public void setMacPrice(String macPrice) {
        this.macPrice = macPrice;
    }

    public String getAddressPrice() {
        return addressPrice;
    }

    public void setAddressPrice(String addressPrice) {
        this.addressPrice = addressPrice;
    }

    public String getFailPrice() {
        return failPrice;
    }

    public void setFailPrice(String failPrice) {
        this.failPrice = failPrice;
    }

    public String getSuccessPrice() {
        return successPrice;
    }

    public void setSuccessPrice(String successPrice) {
        this.successPrice = successPrice;
    }

    public String getCustId() {
        return custId;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getSaleSmsPrice() {
        return saleSmsPrice;
    }

    public void setSaleSmsPrice(String saleSmsPrice) {
        this.saleSmsPrice = saleSmsPrice;
    }

    public String getSaleMinute() {
        return saleMinute;
    }

    public void setSaleMinute(String saleMinute) {
        this.saleMinute = saleMinute;
    }

    public String getSalefixPrice() {
        return salefixPrice;
    }

    public void setSalefixPrice(String salefixPrice) {
        this.salefixPrice = salefixPrice;
    }

    public String getSaleCallPrice() {
        return saleCallPrice;
    }

    public void setSaleCallPrice(String saleCallPrice) {
        this.saleCallPrice = saleCallPrice;
    }

    public String getSaleSeatPrice() {
        return saleSeatPrice;
    }

    public void setSaleSeatPrice(String saleSeatPrice) {
        this.saleSeatPrice = saleSeatPrice;
    }

    @Override
    public String toString() {
        return "SalePriceDTO{" +
                "channel='" + channel + '\'' +
                ", custId='" + custId + '\'' +
                ", saleSmsPrice='" + saleSmsPrice + '\'' +
                ", saleMinute='" + saleMinute + '\'' +
                ", salefixPrice='" + salefixPrice + '\'' +
                ", saleCallPrice='" + saleCallPrice + '\'' +
                ", saleSeatPrice='" + saleSeatPrice + '\'' +
                ", imeiPrice='" + imeiPrice + '\'' +
                ", macPrice='" + macPrice + '\'' +
                ", addressPrice='" + addressPrice + '\'' +
                ", failPrice='" + failPrice + '\'' +
                ", successPrice='" + successPrice + '\'' +
                '}';
    }
}
