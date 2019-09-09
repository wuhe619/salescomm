package com.bdaim.marketproject.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * 营销项目实体类
 *
 * @author chengning@salescomm.net
 * @date 2018/11/23
 * @description
 */
@Entity
@Table(name = "t_market_project", schema = "", catalog = "")
public class MarketProject implements Serializable {
    private int id;
    private String name;
    private Integer industryId;
    private Integer status;
    private Timestamp createTime;
    private String custId;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Basic
    @Column(name = "industry_id")
    public Integer getIndustryId() {
        return industryId;
    }

    public void setIndustryId(Integer industryId) {
        this.industryId = industryId;
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
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MarketProject that = (MarketProject) o;
        return id == that.id &&
                Objects.equals(name, that.name) &&
                Objects.equals(industryId, that.industryId) &&
                Objects.equals(status, that.status) &&
                Objects.equals(createTime, that.createTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, name, industryId, status, createTime);
    }

    @Override
    public String toString() {
        return "MarketProject{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", industryId=" + industryId +
                ", status=" + status +
                ", createTime=" + createTime +
                ", custId='" + custId + '\'' +
                '}';
    }
}
