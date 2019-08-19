package com.bdaim.batch.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * @description: 批次 nl_batch 实体类
 * @auther: Chacker
 * @date: 2019/8/1 09:03
 */
@Entity(name = "nl_batch")
@Table(name = "nl_batch")
public class BatchInfo {
    /**
     * 批次id(批次编号)
     */
    @Id
    @Column(name = "id")
    private String id;
    /**
     * 客户ID(失联修复模块表示企业ID)
     */
    @Column(name = "comp_id")
    private String custId;
    /**
     * 批次名称
     */
    @Column(name = "batch_name")
    private String batchName;
    /**
     * 状态 1.校验中 2.校验失败 3.待上传 4.待发件 5.待取件 6.已发件
     */
    @Column(name = "status")
    private String status;
    /**
     * 上传数量 (信函模块中代指地址数量)
     */
    @Column(name = "upload_num")
    private int uploadNum;
    /**
     * 成功数量 (信函模块中代指有效数量)
     */
    @Column(name = "success_num")
    private int successNum;
    /**
     * 上传时间
     */
    @Column(name = "upload_time")
    private String uploadTime;

    /**
     * 证件类型: 0-身份证 1-imei 2-mac 3 -地址修复
     */
    @Column(name = "certify_type")
    private String certifyType;
    /**
     * 运营渠道 1-讯众 2-联通  4-移动  3-电信
     */
    @Column(name = "channel")
    private String channel;

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public int getUploadNum() {
        return uploadNum;
    }

    public void setUploadNum(int uploadNum) {
        this.uploadNum = uploadNum;
    }

    public int getSuccessNum() {
        return successNum;
    }

    public void setSuccessNum(int successNum) {
        this.successNum = successNum;
    }

    public String getUploadTime() {
        return uploadTime.substring(0,19);
    }

    public void setUploadTime(String uploadTime) {
        this.uploadTime = uploadTime;
    }

    public String getCertifyType() {
        return certifyType;
    }

    public void setCertifyType(String certifyType) {
        this.certifyType = certifyType;
    }

    public String getChannel() {
        return channel;
    }

    public void setChannel(String channel) {
        this.channel = channel;
    }
}
