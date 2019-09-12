package com.bdaim.customer.dto;

/**
 *
 *
 * @author chengning@salescomm.net
 * @date 2018/12/13 14:35
 */
public class CustomerPriceConfigDTO {

    private String custId;
    private String custName;
    /**
     * 资源类型 1-数据，2-呼叫，3-短信,多个逗号隔开
     */
    private String serviceResource;

    private String dataConfig;

    private String callConfig;

    private String smsConfig;

    public CustomerPriceConfigDTO() {
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getServiceResource() {
        return serviceResource;
    }

    public void setServiceResource(String serviceResource) {
        this.serviceResource = serviceResource;
    }

    public String getDataConfig() {
        return dataConfig;
    }

    public void setDataConfig(String dataConfig) {
        this.dataConfig = dataConfig;
    }

    public String getCallConfig() {
        return callConfig;
    }

    public void setCallConfig(String callConfig) {
        this.callConfig = callConfig;
    }

    public String getSmsConfig() {
        return smsConfig;
    }

    public void setSmsConfig(String smsConfig) {
        this.smsConfig = smsConfig;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    @Override
    public String toString() {
        return "CustomerPriceConfigDTO{" +
                "custId='" + custId + '\'' +
                ", custName='" + custName + '\'' +
                ", serviceResource='" + serviceResource + '\'' +
                ", dataConfig='" + dataConfig + '\'' +
                ", callConfig='" + callConfig + '\'' +
                ", smsConfig='" + smsConfig + '\'' +
                '}';
    }
}
