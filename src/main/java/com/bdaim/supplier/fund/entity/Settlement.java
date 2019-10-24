package com.bdaim.supplier.fund.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * 结算
 */
@Entity
@Table(name = "t_settlement", schema = "", catalog = "")
public class Settlement implements Serializable {

    private Long id;
    //结算对象id
    private String productId;
    //结算时间
    private String settlementTime;
    //备注
    private String remark;
    private Date createTime;
    private String createUser;

    @Id
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "product_id")
    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }


    @Basic
    @Column(name = "settlement_time")

    public String getSettlementTime() {
        return settlementTime;
    }

    public void setSettlementTime(String settlementTime) {
        this.settlementTime = settlementTime;
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
    @Column(name = "create_user")

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    @Basic
    @Column(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "Settlement{" +
                "id=" + id +
                ", productId='" + productId + '\'' +
                ", settlementTime='" + settlementTime + '\'' +
                ", remark='" + remark + '\'' +
                ", createTime=" + createTime +
                ", createUser='" + createUser + '\'' +
                '}';
    }
}
