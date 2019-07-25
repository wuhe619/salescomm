package com.bdaim.resource.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;


public class ResourcePropertyDOPK implements Serializable {
    private Integer resourceId;
    private String propertyName;

    @Id
    @Column(name = "resource_id")
    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }
    @Id
    @Column(name = "property_name")
    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ResourcePropertyDOPK that = (ResourcePropertyDOPK) o;
        return Objects.equals(resourceId, that.resourceId) &&
                Objects.equals(propertyName, that.propertyName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(resourceId, propertyName);
    }

//    @Override
//    public boolean equals(Object o) {
//        if (this == o) return true;
//        if (o == null || getClass() != o.getClass()) return false;
//
//        CustIndustryDOPK that = (CustIndustryDOPK) o;
//
//        if (custId != that.custId) return false;
//        if (industryPoolId != null ? !industryPoolId.equals(that.industryPoolId) : that.industryPoolId != null)
//            return false;
//
//        return true;
//    }
//
//    @Override
//    public int hashCode() {
//        int result = (int) (custId ^ (custId >>> 32));
//        result = 31 * result + (industryPoolId != null ? industryPoolId.hashCode() : 0);
//        return result;
//    }
}
