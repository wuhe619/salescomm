package com.bdaim.slxf.entity;

/**
 * @author yanls@salescomm.net
 * @date 2018/10/29
 * @description
 */
public class SmsqueryParam {
    //操作人
    private String realName;
    //用户id (批次明细表id)
    private String superId;
    //企业自带id
    private String enterpriseId;
    //批次名称
    private String batchName;
    //批次id
    private String batchId;
    //模板名称
    private String templateName;
    //企业id
    private String compId;
    //企业名称
    private String enterpriseName;
    //发送开始时间
    private String sendStartTime;
    //发送结束时间
    private String sendEndTime;
    //发送状态
    private Integer status;
    //资源id
    private Integer resourceId;

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public String getRealName() {
        return realName;
    }

    public void setRealName(String realName) {
        this.realName = realName;
    }

    public String getSuperId() {
        return superId;
    }

    public void setSuperId(String superId) {
        this.superId = superId;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCompId() {
        return compId;
    }

    public void setCompId(String compId) {
        this.compId = compId;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getSendStartTime() {
        return sendStartTime;
    }

    public void setSendStartTime(String sendStartTime) {
        this.sendStartTime = sendStartTime;
    }

    public String getSendEndTime() {
        return sendEndTime;
    }

    public void setSendEndTime(String sendEndTime) {
        this.sendEndTime = sendEndTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "SmsqueryParam{" +
                "realName='" + realName + '\'' +
                ", superId='" + superId + '\'' +
                ", enterpriseId='" + enterpriseId + '\'' +
                ", batchName='" + batchName + '\'' +
                ", batchId='" + batchId + '\'' +
                ", templateName='" + templateName + '\'' +
                ", compId='" + compId + '\'' +
                ", enterpriseName='" + enterpriseName + '\'' +
                ", sendStartTime='" + sendStartTime + '\'' +
                ", sendEndTime='" + sendEndTime + '\'' +
                ", status=" + status +
                ", resourceId=" + resourceId +
                '}';
    }
}
