package com.bdaim.customgroup.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 */
public class CustomerGroupListDOPK implements Serializable {
    private String id;
    private String groupConditionMd5;

    @Column(name = "id")
    @Id
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Column(name = "group_condition_md5")
    @Id
    public String getGroupConditionMd5() {
        return groupConditionMd5;
    }

    public void setGroupConditionMd5(String groupConditionMd5) {
        this.groupConditionMd5 = groupConditionMd5;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CustomerGroupListDOPK that = (CustomerGroupListDOPK) o;

        if (id != null ? !id.equals(that.id) : that.id != null) return false;
        if (groupConditionMd5 != null ? !groupConditionMd5.equals(that.groupConditionMd5) : that.groupConditionMd5 != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (groupConditionMd5 != null ? groupConditionMd5.hashCode() : 0);
        return result;
    }
}
