package com.bdaim.batch.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/6
 * @description
 */
@Entity
@Table(name = "nl_batch", schema = "", catalog = "")
public class BatchListEntity {
    private String id;
    private String compId;
    private String compName;
    private String batchName;
    private Integer certifyType;
    private Integer status;
    private Integer uploadNum;
    private Integer successNum;
    private Timestamp uploadTime;
    private Timestamp repairTime;
    private Integer channel;
    private String repairStrategy;
    private String apparentNumber;
    private String repairMode;
    private Integer cucReceived;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "comp_id")
    public String getCompId() {
        return compId;
    }

    public void setCompId(String compId) {
        this.compId = compId;
    }

    @Basic
    @Column(name = "repair_mode")
    public String getRepairMode() {
        return repairMode;
    }

    public void setRepairMode(String repairMode) {
        this.repairMode = repairMode;
    }
    @Basic
    @Column(name = "comp_name")
    public String getCompName() {
        return compName;
    }

    public void setCompName(String compName) {
        this.compName = compName;
    }

    @Basic
    @Column(name = "batch_name")
    public String getBatchName() {
        return batchName;
    }

    public void setBatchName(String batchName) {
        this.batchName = batchName;
    }

    @Basic
    @Column(name = "certify_type")
    public Integer getCertifyType() {
        return certifyType;
    }

    public void setCertifyType(Integer certifyType) {
        this.certifyType = certifyType;
    }

    @Basic
    @Column(name = "status")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "upload_num")
    public Integer getUploadNum() {
        return uploadNum;
    }

    public void setUploadNum(Integer uploadNum) {
        this.uploadNum = uploadNum;
    }

    @Basic
    @Column(name = "success_num")
    public Integer getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(Integer successNum) {
        this.successNum = successNum;
    }

    @Basic
    @Column(name = "upload_time")
    public Timestamp getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Basic
    @Column(name = "repair_time")
    public Timestamp getRepairTime() {
        return repairTime;
    }

    public void setRepairTime(Timestamp repairTime) {
        this.repairTime = repairTime;
    }

    @Basic
    @Column(name = "channel")
    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    @Basic
    @Column(name = "repair_strategy")
    public String getRepairStrategy() {
        return repairStrategy;
    }

    public void setRepairStrategy(String repairStrategy) {
        this.repairStrategy = repairStrategy;
    }

    @Basic
    @Column(name = "apparent_number")
    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
    }

    @Basic
    @Column(name = "cuc_received")
    public Integer getCucReceived() {
        return cucReceived;
    }

    public void setCucReceived(Integer cucReceived) {
        this.cucReceived = cucReceived;
    }

    @Override
    public String toString() {
        return "BatchListEntity{" +
                "id='" + id + '\'' +
                ", compId='" + compId + '\'' +
                ", compName='" + compName + '\'' +
                ", batchName='" + batchName + '\'' +
                ", certifyType=" + certifyType +
                ", status=" + status +
                ", uploadNum=" + uploadNum +
                ", successNum=" + successNum +
                ", uploadTime=" + uploadTime +
                ", repairTime=" + repairTime +
                ", channel=" + channel +
                ", repairStrategy='" + repairStrategy + '\'' +
                ", apparentNumber='" + apparentNumber + '\'' +
                ", cucReceived='" + cucReceived + '\'' +
                ", repairMode='" + repairMode + '\'' +
                '}';
    }
}
