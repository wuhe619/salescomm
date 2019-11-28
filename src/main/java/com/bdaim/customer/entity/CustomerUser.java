package com.bdaim.customer.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

/**
 * @author duanliying
 * @date 2018/9/20
 * @description
 */
@Entity
@Table(name = "t_customer_user")
public class CustomerUser {
    //用户id
    @Id
    @Column(name = "id")
    private Long id;
    //登陆账号
    @Column(name = "account")
    private String account;
    //登陆密码
    @Column(name = "password")
    private String password;
    //企业id
    @Column(name = "cust_id")
    private String cust_id;
    //状态
    @Column(name = "status")
    private Integer status;
    //属性Key
    @Column(name = "user_type")
    private Integer userType;
    @Column(name = "create_time")
    private Date createTime;
    //企业联系人
    @Column(name = "realname")
    private String realname;

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getCust_id() {
        return cust_id;
    }

    public void setCust_id(String cust_id) {
        this.cust_id = cust_id;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getUserType() {
        return userType;
    }

    public void setUserType(Integer userType) {
        this.userType = userType;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    @Override
    public String toString() {
        return "CustomerUserDO{" +
                "id=" + id +
                ", account='" + account + '\'' +
                ", password='" + password + '\'' +
                ", cust_id='" + cust_id + '\'' +
                ", status=" + status +
                ", userType=" + userType +
                ", createTime='" + createTime + '\'' +
                '}';
    }
}
