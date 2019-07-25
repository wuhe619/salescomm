package com.bdaim.template.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2018/10/18
 * @description
 */
@Entity
@Table(name = "t_template", schema = "", catalog = "")
public class MarketTemplate implements Serializable {
    private int id;
    private String custId;
    private String title;
    private Integer typeCode;
    private String mouldContent;
    private Timestamp createTime;
    private Integer status;
    private Timestamp modifyTime;
    private Timestamp passTime;
    private String remark;
    private String smsSignatures;
    private String emailMouldContent;
    private String operator;
    private String templateCode;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Basic
    @Column(name = "title")
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    @Basic
    @Column(name = "type_code")
    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    @Basic
    @Column(name = "mould_content")
    public String getMouldContent() {
        return mouldContent;
    }

    public void setMouldContent(String mouldContent) {
        this.mouldContent = mouldContent;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
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
    @Column(name = "modify_time")
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Basic
    @Column(name = "pass_time")
    public Timestamp getPassTime() {
        return passTime;
    }

    public void setPassTime(Timestamp passTime) {
        this.passTime = passTime;
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
    @Column(name = "sms_signatures")
    public String getSmsSignatures() {
        return smsSignatures;
    }

    public void setSmsSignatures(String smsSignatures) {
        this.smsSignatures = smsSignatures;
    }

    @Basic
    @Column(name = "email_mould_content")
    public String getEmailMouldContent() {
        return emailMouldContent;
    }

    public void setEmailMouldContent(String emailMouldContent) {
        this.emailMouldContent = emailMouldContent;
    }

    @Basic
    @Column(name = "operator")
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Basic
    @Column(name = "template_code")
    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    @Override
    public String toString() {
        return "MarketTemplate{" +
                "id=" + id +
                ", custId='" + custId + '\'' +
                ", title='" + title + '\'' +
                ", typeCode=" + typeCode +
                ", mouldContent='" + mouldContent + '\'' +
                ", createTime=" + createTime +
                ", status=" + status +
                ", modifyTime=" + modifyTime +
                ", passTime=" + passTime +
                ", remark='" + remark + '\'' +
                ", smsSignatures='" + smsSignatures + '\'' +
                ", emailMouldContent='" + emailMouldContent + '\'' +
                ", operator='" + operator + '\'' +
                ", templateCode='" + templateCode + '\'' +
                '}';
    }
}
