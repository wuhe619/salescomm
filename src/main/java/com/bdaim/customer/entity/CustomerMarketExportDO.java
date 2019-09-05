package com.bdaim.customer.entity;


import javax.persistence.*;
import java.util.Date;

/**
 * 客户营销资源导出
 *
 * @author chengning@salescomm.net
 * @date 2018/9/4
 * @description
 */
@Entity
@Table(name = "customer_market_export")
public class CustomerMarketExportDO {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;

    @Column
    private Integer type;

    @Column(name = "cust_id")
    private String customerId;

    @Column(name = "customer_group_id")
    private String customerGroupId;

    @Column(name = "file_name")
    private String fileName;

    @Column(name = "apply_time")
    private Date applyTime;

    @Column(name = "potential_person_sum")
    private Long potentialPersonSum;

    @Column(name = "download_sum")
    private Integer downloadSum;

    @Column(name = "download_last_time")
    private Date downloadLastTime;

    @Column(name = "download_url")
    private String downloadUrl;

    @Column(name = "create_time")
    private Date createTime;

    @Column
    private Integer status;

    @Column(name = "modify_time")
    private Date modifyTime;

    @Column(name = "pass_time")
    private Date passTime;

    @Column
    private String remark;

    @Column
    private String operator;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public Date getApplyTime() {
        return applyTime;
    }

    public void setApplyTime(Date applyTime) {
        this.applyTime = applyTime;
    }

    public Long getPotentialPersonSum() {
        return potentialPersonSum;
    }

    public void setPotentialPersonSum(Long potentialPersonSum) {
        this.potentialPersonSum = potentialPersonSum;
    }

    public Integer getDownloadSum() {
        return downloadSum;
    }

    public void setDownloadSum(Integer downloadSum) {
        this.downloadSum = downloadSum;
    }

    public Date getDownloadLastTime() {
        return downloadLastTime;
    }

    public void setDownloadLastTime(Date downloadLastTime) {
        this.downloadLastTime = downloadLastTime;
    }

    public String getDownloadUrl() {
        return downloadUrl;
    }

    public void setDownloadUrl(String downloadUrl) {
        this.downloadUrl = downloadUrl;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Date getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Date modifyTime) {
        this.modifyTime = modifyTime;
    }

    public Date getPassTime() {
        return passTime;
    }

    public void setPassTime(Date passTime) {
        this.passTime = passTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }


    public String getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(String customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    @Override
    public String toString() {
        return "CustomerMarketExportDO{" +
                "id=" + id +
                ", type=" + type +
                ", customerId='" + customerId + '\'' +
                ", customerGroupId='" + customerGroupId + '\'' +
                ", fileName='" + fileName + '\'' +
                ", applyTime=" + applyTime +
                ", potentialPersonSum=" + potentialPersonSum +
                ", downloadSum=" + downloadSum +
                ", downloadLastTime=" + downloadLastTime +
                ", downloadUrl='" + downloadUrl + '\'' +
                ", createTime=" + createTime +
                ", status=" + status +
                ", modifyTime=" + modifyTime +
                ", passTime=" + passTime +
                ", remark='" + remark + '\'' +
                ", operator='" + operator + '\'' +
                '}';
    }
}
