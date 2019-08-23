package com.bdaim.customer.dto;

import java.util.List;
import java.util.Map;

/**
 * 职场结算价DTO
 */
public class CommonInfoDTO {
    private String zid;
    private String projectId;
    private String custId;
    private String jobId;
    private List<Map<String,String>> properties;
    private String serviceCode;
    private String status;

    public List<Map<String, String>> getProperties() {
        return properties;
    }

    public void setProperties(List<Map<String, String>> properties) {
        this.properties = properties;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getZid() {
        return zid;
    }

    public void setZid(String zid) {
        this.zid = zid;
    }

    public String getProjectId() {
        return projectId;
    }

    public void setProjectId(String projectId) {
        this.projectId = projectId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getJobId() {
        return jobId;
    }

    public void setJobId(String jobId) {
        this.jobId = jobId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }
}
