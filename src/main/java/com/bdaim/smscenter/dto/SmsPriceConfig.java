package com.bdaim.smscenter.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/28
 * @description
 */
public class SmsPriceConfig {

    private String supplierId;
    private String resourceId;
    /**
     * 1 按条单一收费
     */
    private String type;

    private String name;
    /**
     * 价格(元)
     */
    private String price;

    public String getSupplierId() {
        return supplierId;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }
}
