package com.bdaim.bill.dao;

import javax.persistence.*;

/**
 * @description 消费金额月份账单统计
 * @author:duanliying
 * @method
 * @date: 2019/1/2 14:39
 */
@Entity
@Table(name = "stat_bill_month", schema = "", catalog = "")
public class StatBillMonthEntity {
    private String statTime;
    private String custId;
    private String resourceId;
    private Integer type;
    private Integer calculateSum;
    private Integer amount;
    private Integer prodAmount;

    @Basic
    @Column(name = "stat_time")
    public String getStatTime() {
        return statTime;
    }

    public void setStatTime(String statTime) {
        this.statTime = statTime;
    }


    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Id
    @Column(name = "resource_id")
    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Basic
    @Column(name = "calculate_sum")
    public Integer getCalculateSum() {
        return calculateSum;
    }

    public void setCalculateSum(Integer calculateSum) {
        this.calculateSum = calculateSum;
    }

    @Basic
    @Column(name = "amount")
    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    @Basic
    @Column(name = "prod_amount")

    public Integer getProdAmount() {
        return prodAmount;
    }

    public void setProdAmount(Integer prodAmount) {
        this.prodAmount = prodAmount;
    }


    @Override
    public String toString() {
        return "StatBillMonthEntity{" +
                "statTime='" + statTime + '\'' +
                ", custId='" + custId + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", type=" + type +
                ", calculateSum=" + calculateSum +
                ", amount=" + amount +
                ", prodAmount=" + prodAmount +
                '}';
    }
}
