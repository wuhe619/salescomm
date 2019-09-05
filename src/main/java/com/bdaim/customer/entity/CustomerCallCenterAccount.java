package com.bdaim.customer.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * 呼叫中心企业账号管理
 */
@Entity
@Table(name = "t_customer_callcenter", schema = "", catalog = "")
public class CustomerCallCenterAccount implements Serializable {

    //企业账号id
	private Long id;
    //企业ID
    private String custId;

    //
    private Integer authorizedSeats;
    private Integer maxOccurs;

    //状态
    private Integer status;
    private Date createTime;
    private Date startTime;
    private Date endTime;
    private Date lastUpdateTime;
    private String createUser;
    private String lastUpdateUser;

    private String customerName;

    @Transient
    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Basic
    @Column(name = "authorized_seats")
    public Integer getAuthorizedSeats() {
        return authorizedSeats;
    }

    public void setAuthorizedSeats(Integer authorizedSeats) {
        this.authorizedSeats = authorizedSeats;
    }
    @Basic
    @Column(name = "max_occurs")
    public Integer getMaxOccurs() {
        return maxOccurs;
    }

    public void setMaxOccurs(Integer maxOccurs) {
        this.maxOccurs = maxOccurs;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }
    @Basic
    @Column(name = "start_time")
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }
    @Basic
    @Column(name = "end_time")
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
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
    @Column(name = "last_update_user")
    public String getLastUpdateUser() {
        return lastUpdateUser;
    }

    public void setLastUpdateUser(String lastUpdateUser) {
        this.lastUpdateUser = lastUpdateUser;
    }


    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
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
    @Column(name = "create_time")
    public Date getCreateTime() {
        return createTime;
    }

    @Basic
    @Column(name = "last_update_time")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomerCallCenterAccount that = (CustomerCallCenterAccount) o;
        return status == that.status &&
                Objects.equals(id, that.id) &&
                Objects.equals(custId, that.custId) &&
                Objects.equals(authorizedSeats, that.authorizedSeats) &&
                Objects.equals(maxOccurs, that.maxOccurs) &&
                Objects.equals(createTime, that.createTime) &&
                Objects.equals(startTime, that.startTime) &&
                Objects.equals(endTime, that.endTime) &&
                Objects.equals(lastUpdateTime, that.lastUpdateTime) &&
                Objects.equals(createUser, that.createUser) &&
                Objects.equals(lastUpdateUser, that.lastUpdateUser);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, custId, authorizedSeats, maxOccurs, status, createTime, startTime, endTime, lastUpdateTime, createUser, lastUpdateUser);
    }
}
