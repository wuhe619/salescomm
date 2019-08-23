package com.bdaim.industry.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/2/21
 * @description
 */
public class IndustryPoolPriceDTO {

    private Integer industryPoolId;

    private Integer price;

    /**
     * 标签池-客户资源配置
     */
    private String dataCustConfig;

    /**
     * 1-已配置价格 2-未配置价格
     */
    private Integer status;

    public Integer getIndustryPoolId() {
        return industryPoolId;
    }

    public void setIndustryPoolId(Integer industryPoolId) {
        this.industryPoolId = industryPoolId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getPrice() {
        return price;
    }

    public void setPrice(Integer price) {
        this.price = price;
    }

    public String getDataCustConfig() {
        return dataCustConfig;
    }

    public void setDataCustConfig(String dataCustConfig) {
        this.dataCustConfig = dataCustConfig;
    }
}
