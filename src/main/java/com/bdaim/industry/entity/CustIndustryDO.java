package com.bdaim.industry.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Mr.YinXin on 2017/3/16.
 */
@Entity
@Table(name = "t_cust_industry", schema = "", catalog = "")
@IdClass(CustIndustryDOPK.class)
public class CustIndustryDO {
    private long custId;
    private String industryPoolId;
    private Integer status;
    private Timestamp createTime;
    private Timestamp modifyTime;
    private String operator;

    @Id
    @Column(name = "cust_id")
    public long getCustId() {
        return custId;
    }

    public void setCustId(long custId) {
        this.custId = custId;
    }

    @Id
    @Column(name = "industry_pool_id")
    public String getIndustryPoolId() {
        return industryPoolId;
    }

    public void setIndustryPoolId(String industryPoolId) {
        this.industryPoolId = industryPoolId;
    }

    @Basic
    @Column(name = "STATUS")
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
    @Column(name = "modify_time")
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Basic
    @Column(name = "operator")
    public String getOperator() {
        return operator;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustIndustryDO that = (CustIndustryDO) o;

        if (custId != that.custId) return false;
        if (industryPoolId != null ? !industryPoolId.equals(that.industryPoolId) : that.industryPoolId != null)
            return false;
        if (status != null ? !status.equals(that.status) : that.status != null) return false;
        if (createTime != null ? !createTime.equals(that.createTime) : that.createTime != null) return false;
        if (modifyTime != null ? !modifyTime.equals(that.modifyTime) : that.modifyTime != null) return false;
        if (operator != null ? !operator.equals(that.operator) : that.operator != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (custId ^ (custId >>> 32));
        result = 31 * result + (industryPoolId != null ? industryPoolId.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (modifyTime != null ? modifyTime.hashCode() : 0);
        result = 31 * result + (operator != null ? operator.hashCode() : 0);
        return result;
    }
}
