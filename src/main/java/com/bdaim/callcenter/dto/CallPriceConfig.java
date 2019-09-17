package com.bdaim.callcenter.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/28
 * @description
 */
public class CallPriceConfig {

    private String supplierId;
    private String resourceId;
    /**
     * 1 呼叫中心 2-双呼
     */
    private String type;
    /**
     * 通话费用(元)
     */
    private String call_price;
    /**
     * 外显费用(元/号/月)
     */
    private String apparent_number_price;
    /**
     * 坐席半月5折 1-是 2-否
     */
    private Integer seat_month_discount;
    /**
     *坐席收费方式 1-单一价 2-阶梯价
     */
    private Integer seat_price_type;
    /**
     *坐席包月价格(元)
     */
    private Double seat_month_price;
    /**
     *坐席包月分钟
     */
    private Integer seat_month_minute;
    /**
     * 1-是 2-否(选择1需要传递province 和province_price字段)
     */
    private Integer province_type;
    /**
     * 本省通话单独计费选择的省份
     */
    private String province;
    /**
     * 本省费用,外省费用.逗号隔开
     */
    private String province_price;
    /**
     * 阶梯层级
     */
    private String step;
    /**
     * 阶梯层级计费-开始数量，结束数量，价格 多个用|隔开
     */
    private String step_config;

    /**
     * 配置状态 1-有效 2-无效
     */
    private Integer status;

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

    public String getCall_price() {
        return call_price;
    }

    public void setCall_price(String call_price) {
        this.call_price = call_price;
    }

    public String getApparent_number_price() {
        return apparent_number_price;
    }

    public void setApparent_number_price(String apparent_number_price) {
        this.apparent_number_price = apparent_number_price;
    }

    public Integer getSeat_month_discount() {
        return seat_month_discount;
    }

    public void setSeat_month_discount(Integer seat_month_discount) {
        this.seat_month_discount = seat_month_discount;
    }

    public Integer getSeat_price_type() {
        return seat_price_type;
    }

    public void setSeat_price_type(Integer seat_price_type) {
        this.seat_price_type = seat_price_type;
    }

    public Double getSeat_month_price() {
        return seat_month_price;
    }

    public void setSeat_month_price(Double seat_month_price) {
        this.seat_month_price = seat_month_price;
    }

    public Integer getSeat_month_minute() {
        return seat_month_minute;
    }

    public void setSeat_month_minute(Integer seat_month_minute) {
        this.seat_month_minute = seat_month_minute;
    }

    public Integer getProvince_type() {
        return province_type;
    }

    public void setProvince_type(Integer province_type) {
        this.province_type = province_type;
    }

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getProvince_price() {
        return province_price;
    }

    public void setProvince_price(String province_price) {
        this.province_price = province_price;
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

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CallPriceConfig{" +
                "supplierId='" + supplierId + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", type='" + type + '\'' +
                ", call_price='" + call_price + '\'' +
                ", apparent_number_price='" + apparent_number_price + '\'' +
                ", seat_month_discount=" + seat_month_discount +
                ", seat_price_type=" + seat_price_type +
                ", seat_month_price=" + seat_month_price +
                ", seat_month_minute=" + seat_month_minute +
                ", province_type=" + province_type +
                ", province='" + province + '\'' +
                ", province_price='" + province_price + '\'' +
                ", step='" + step + '\'' +
                ", step_config='" + step_config + '\'' +
                ", status=" + status +
                '}';
    }
}
