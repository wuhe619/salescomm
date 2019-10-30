package com.bdaim.customer.dto;

import com.bdaim.customer.entity.CustomerProperty;

public class PriceDTO
{
    private String custId;
    private String price;

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public PriceDTO() {
    }

    public PriceDTO(   CustomerProperty property) {

        this.custId = property.getCustId();
        this.price = property.getPropertyValue();
    }
}
