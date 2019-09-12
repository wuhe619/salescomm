package com.bdaim.industry.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 * Created by Mr.YinXin on 2017/3/16.
 */
public class CustIndustryDOPK implements Serializable {
    private long custId;
    private String industryPoolId;

    @Column(name = "cust_id")
    @Id
    public long getCustId() {
        return custId;
    }

    public void setCustId(long custId) {
        this.custId = custId;
    }

    @Column(name = "industry_pool_id")
    @Id
    public String getIndustryPoolId() {
        return industryPoolId;
    }

    public void setIndustryPoolId(String industryPoolId) {
        this.industryPoolId = industryPoolId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustIndustryDOPK that = (CustIndustryDOPK) o;

        if (custId != that.custId) return false;
        if (industryPoolId != null ? !industryPoolId.equals(that.industryPoolId) : that.industryPoolId != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (custId ^ (custId >>> 32));
        result = 31 * result + (industryPoolId != null ? industryPoolId.hashCode() : 0);
        return result;
    }
}
