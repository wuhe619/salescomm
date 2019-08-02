package com.bdaim.batch.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @description: 批次详情
 * @auther: Chacker
 * @date: 2019/8/1 09:59
 */
@Entity(name = "nl_batch_detail")
@Table(name = "nl_batch_detail")
public class BatchDetailInfo implements Serializable {
    /**
     * 批次详情ID(信函模块中代指收件人ID)
     */
    @Id
    @Column(name = "id")
    private String id;

    /**
     * 批次ID
     */
    @Column(name = "batch_id")
    private String batchId;
    /**
     * 姓名
     */
    @Column(name = "label_one")
    private String name;
    /**
     * 电话
     */
    @Column(name = "label_two")
    private String phone;
    /**
     * 校验结果(信函模块中代指 校验结果)
     */
    @Column(name = "label_seven")
    private String checkingResult;
    /**
     * 地址
     */
    @Column(name = "label_four")
    private String labelFour;
    /**
     * 状态(信函模块中代指 快件状态)
     */
    @Column(name = "status")
    private String status;

    /**
     * 文件编码
     */
    @Column(name = "label_six")
    private String labelSix;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getLabelFour() {
        return labelFour;
    }

    public void setLabelFour(String labelFour) {
        this.labelFour = labelFour;
    }

    public String getCheckingResult() {
        return checkingResult;
    }

    public void setCheckingResult(String checkingResult) {
        this.checkingResult = checkingResult;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getLabelSix() {
        return labelSix;
    }

    public void setLabelSix(String labelSix) {
        this.labelSix = labelSix;
    }


}
