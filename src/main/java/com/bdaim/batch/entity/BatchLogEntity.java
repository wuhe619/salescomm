package com.bdaim.batch.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author yanls
 * @date 2018/11/19
 * @description 批次记录表
 */
@Entity
@Table(name = "nl_batch_log", schema = "", catalog = "")
public class BatchLogEntity implements Serializable {
    //批次id
    private String batchId;
    //企业自带id
    private String enterpriseId;
    //身份证号（唯一标识）
    private String idCard;
    //操作人id
    private String operUserId;
    //操作人姓名
    private String operName;
    //上传时间
    private Timestamp uploadTime;
    //标记信息
    private String remark;

    @Basic
    @Column(name = "oper_user_id")
    public String getOperUserId() {
        return operUserId;
    }

    public void setOperUserId(String operUserId) {
        this.operUserId = operUserId;
    }

    @Basic
    @Column(name = "oper_name")
    public String getOperName() {
        return operName;
    }
    public void setOperName(String operName) {
        this.operName = operName;
    }

    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
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
    @Column(name = "batch_id")
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    @Id
    @Column(name = "enterprise_id")
    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    @Basic
    @Column(name = "id_card")
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchLogEntity that = (BatchLogEntity) o;
        return batchId == that.batchId &&
                operUserId == that.operUserId &&
                operName == that.operName &&
                Objects.equals(enterpriseId, that.enterpriseId) &&
                Objects.equals(idCard, that.idCard);
    }

    @Override
    public int hashCode() {

        return Objects.hash(batchId, enterpriseId, idCard);
    }

    @Override
    public String toString() {
        return "BatchDetail{" +
                "batchId=" + batchId +
                ", enterpriseId='" + enterpriseId + '\'' +
                ", idCard='" + idCard + '\'' +
                ", operUserId='" + operUserId + '\'' +
                ", operName='" + operName + '\'' +
                ", uploadTime='" + uploadTime + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
