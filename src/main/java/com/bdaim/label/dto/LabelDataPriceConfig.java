package com.bdaim.label.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/28
 * @description
 */
public class LabelDataPriceConfig {

    private String supplierId;
    private String resourceId;
    private String name;
    /**
     * 1-按条单一计费 2-按条阶梯计费 3-按标签计费 4-按呼通计费
     */
    private Integer type;
    /**
     * 价格(元)
     */
    private String price;
    /**
     * 阶梯层级
     */
    private String step;
    /**
     * 阶梯层级计费-开始数量，结束数量，价格 多个用|隔开
     */
    private String step_config;
    /**
     * 1-单独计费 2-不单独计费
     */
    private Integer smsDataType;
    /**
     * 短信数据费单价
     */
    private String smsDataPrice;

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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getPrice() {
        return price;
    }

    public void setPrice(String price) {
        this.price = price;
    }

    public String getStep() {
        return step;
    }

    public void setStep(String step) {
        this.step = step;
    }

    public String getStep_config() {
        return step_config;
    }

    public void setStep_config(String step_config) {
        this.step_config = step_config;
    }

    public Integer getSmsDataType() {
        return smsDataType;
    }

    public void setSmsDataType(Integer smsDataType) {
        this.smsDataType = smsDataType;
    }

    public String getSmsDataPrice() {
        return smsDataPrice;
    }

    public void setSmsDataPrice(String smsDataPrice) {
        this.smsDataPrice = smsDataPrice;
    }
}
