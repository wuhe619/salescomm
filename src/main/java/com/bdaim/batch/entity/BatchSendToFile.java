package com.bdaim.batch.entity;

import com.alibaba.fastjson.annotation.JSONType;

import java.io.Serializable;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/9/10 14:46
 */
//@JSONType(orders = { "createId", "activityId", "departTypeId", "batchId", "partnerName", "startTime", "endTime", "sendNum", "ivr", "certList", "kehuId", "contactType" })
public class BatchSendToFile{
    //private static final long serialVersionUID = 2008078624343536695L;
    private String createId;
    private String activityId;
    private String departTypeId;
    private String batchId;
    private String partnerName;
    private String startTime;
    private String endTime;
    private String sendNum;
    private String ivr;
    private String certList;
    private String kehuId;
    private String repairType;


    public String getRepairType() {
        return repairType;
    }

    public void setRepairType(String repairType) {
        this.repairType = repairType;
    }

    public String getCreateId() {
        return createId;
    }

    public void setCreateId(String createId) {
        this.createId = createId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getDepartTypeId() {
        return departTypeId;
    }

    public void setDepartTypeId(String departTypeId) {
        this.departTypeId = departTypeId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public String getSendNum() {
        return sendNum;
    }

    public void setSendNum(String sendNum) {
        this.sendNum = sendNum;
    }

    public String getIvr() {
        return ivr;
    }

    public void setIvr(String ivr) {
        this.ivr = ivr;
    }

    public String getCertList() {
        return certList;
    }

    public void setCertList(String certList) {
        this.certList = certList;
    }

    public String getKehuId() {
        return kehuId;
    }

    public void setKehuId(String kehuId) {
        this.kehuId = kehuId;
    }



}
