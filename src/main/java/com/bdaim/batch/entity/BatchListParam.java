package com.bdaim.batch.entity;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/6
 * @description
 */
public class BatchListParam {
    private String id;
    private String compId;
    private String batchName;
    private String uploadStartTime;
    private String uploadEndTime;
    private Integer certifyType;
    private Integer status;
    private String userId;
    private String userType;
    private String comp_name;

    public String getComp_name() {
        return comp_name;
    }

    public void setComp_name(String comp_name) {
        this.comp_name = comp_name;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    public String getUploadStartTime() {
        return uploadStartTime;
    }

    public void setUploadStartTime(String uploadStartTime) {
        this.uploadStartTime = uploadStartTime;
    }

    public String getUploadEndTime() {
        return uploadEndTime;
    }

    public void setUploadEndTime(String uploadEndTime) {
        this.uploadEndTime = uploadEndTime;
    }

    public Integer getCertifyType() {
        return certifyType;
    }

    public void setCertifyType(Integer certifyType) {
        this.certifyType = certifyType;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCompId() {
        return compId;
    }

    public void setCompId(String compId) {
        this.compId = compId;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    @Override
    public String toString() {
        return "BatchListParam{" +
                "id='" + id + '\'' +
                ", compId='" + compId + '\'' +
                ", batchName='" + batchName + '\'' +
                ", uploadStartTime='" + uploadStartTime + '\'' +
                ", uploadEndTime='" + uploadEndTime + '\'' +
                ", certifyType=" + certifyType +
                ", status=" + status +
                ", userId='" + userId + '\'' +
                ", userType='" + userType + '\'' +
                ", compname='" + comp_name + '\'' +
                '}';
    }



}
