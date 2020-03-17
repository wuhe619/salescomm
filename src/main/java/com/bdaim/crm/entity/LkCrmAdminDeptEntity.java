package com.bdaim.crm.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Objects;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "lkcrm_admin_dept", schema = "", catalog = "")
public class LkCrmAdminDeptEntity {
    private Integer deptId;
    private Integer pid;
    private String name;
    private Integer num;
    private String remark;
    private String custId;

    public LkCrmAdminDeptEntity() {
    }

    public LkCrmAdminDeptEntity(Integer pid, String name, Integer num, String remark, String custId) {
        this.pid = pid;
        this.name = name;
        this.num = num;
        this.remark = remark;
        this.custId = custId;
    }

    @Id
    @Column(name = "dept_id")
    @GeneratedValue
    public Integer getDeptId() {
        return deptId;
    }

    public void setDeptId(Integer deptId) {
        this.deptId = deptId;
    }

    @Basic
    @Column(name = "pid")
    public Integer getPid() {
        return pid;
    }

    public void setPid(Integer pid) {
        this.pid = pid;
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
    @Column(name = "num")
    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
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
        LkCrmAdminDeptEntity that = (LkCrmAdminDeptEntity) o;
        return deptId == that.deptId &&
                Objects.equals(pid, that.pid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(num, that.num) &&
                Objects.equals(remark, that.remark);
    }

    @Override
    public int hashCode() {
        return Objects.hash(deptId, pid, name, num, remark);
    }
}
