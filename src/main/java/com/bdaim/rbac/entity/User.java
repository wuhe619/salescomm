package com.bdaim.rbac.entity;

import java.io.Serializable;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

@Entity
@Table(name = "t_user")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column
    private String name;
    @Column
    private String realname;
    @Column
    private String password;
    @Column
    private String cust_Id;
    @Column
    private String user_type;
    @Column
    private String enterprise_name;
    /*@OneToMany(mappedBy = "createUser", fetch = FetchType.LAZY)
    private List<LabelCategory> createCategory;
    @OneToMany(mappedBy = "modifyUser", fetch = FetchType.LAZY)
    private List<LabelCategory> modifyCategory;*/

    /*@OneToMany(mappedBy = "applyUser", fetch = FetchType.LAZY)
    private List<LabelAudit> applys;

    @OneToMany(mappedBy = "auditUser", fetch = FetchType.LAZY)
    private List<LabelAudit> audits;

    @OneToMany(mappedBy = "devUser", fetch = FetchType.LAZY)
    private List<LabelAudit> devs;*/


    /*@OneToMany(mappedBy = "createUser", fetch = FetchType.LAZY)
    private List<CustomGroupDO> createCustomGroups;

    @OneToMany(mappedBy = "updateUser", fetch = FetchType.LAZY)
    private List<CustomGroupDO> updateCustomGroups;*/


    /*@OneToMany(mappedBy = "labelCreateUser", fetch = FetchType.LAZY)
    private List<LabelInfo> createLabels;


    @OneToMany(mappedBy = "labelUpdateUser", fetch = FetchType.LAZY)
    private List<LabelInfo> updateLabels;

    @OneToMany(mappedBy = "labelOfflineUser", fetch = FetchType.LAZY)
    private List<LabelInfo> offlineLabels;*/

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "deptid")
    private Department department;

    /*@OneToMany(mappedBy = "labelCategoryUser", fetch = FetchType.LAZY)
    private List<UserLabelCategory> userLabelCategoryLs;*/

    @Transient
    private String newPassword;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getNewPassword() {
        return newPassword;
    }

    public void setNewPassword(String newPassword) {
        this.newPassword = newPassword;
    }

    /*public List<LabelCategory> getCreateCategory() {
        return createCategory;
    }

    public void setCreateCategory(List<LabelCategory> createCategory) {
        this.createCategory = createCategory;
    }

    public List<LabelCategory> getModifyCategory() {
        return modifyCategory;
    }

    public void setModifyCategory(List<LabelCategory> modifyCategory) {
        this.modifyCategory = modifyCategory;
    }
*/
    /*public List<LabelAudit> getApplys() {
        return applys;
    }

    public void setApplys(List<LabelAudit> applys) {
        this.applys = applys;
    }

    public List<LabelAudit> getAudits() {
        return audits;
    }

    public void setAudits(List<LabelAudit> audits) {
        this.audits = audits;
    }

    public List<LabelAudit> getDevs() {
        return devs;
    }

    public void setDevs(List<LabelAudit> devs) {
        this.devs = devs;
    }*/

    /*public List<CustomGroupDO> getCreateCustomGroups() {
        return createCustomGroups;
    }

    public void setCreateCustomGroups(List<CustomGroupDO> createCustomGroups) {
        this.createCustomGroups = createCustomGroups;
    }

    public List<CustomGroupDO> getUpdateCustomGroups() {
        return updateCustomGroups;
    }

    public void setUpdateCustomGroups(List<CustomGroupDO> updateCustomGroups) {
        this.updateCustomGroups = updateCustomGroups;
    }*/

    public String getEnterprise_name() {
        return enterprise_name;
    }

    public void setEnterprise_name(String enterprise_name) {
        this.enterprise_name = enterprise_name;
    }

    /*public List<LabelInfo> getCreateLabels() {
        return createLabels;
    }

    public void setCreateLabels(List<LabelInfo> createLabels) {
        this.createLabels = createLabels;
    }

    public List<LabelInfo> getUpdateLabels() {
        return updateLabels;
    }

    public void setUpdateLabels(List<LabelInfo> updateLabels) {
        this.updateLabels = updateLabels;
    }

    public List<LabelInfo> getOfflineLabels() {
        return offlineLabels;
    }

    public void setOfflineLabels(List<LabelInfo> offlineLabels) {
        this.offlineLabels = offlineLabels;
    }*/

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public Department getDepartment() {
        return department;
    }

    public void setDepartment(Department department) {
        this.department = department;
    }

    /*public List<UserLabelCategory> getUserLabelCategoryLs() {
        return userLabelCategoryLs;
    }

    public void setUserLabelCategoryLs(List<UserLabelCategory> userLabelCategoryLs) {
        this.userLabelCategoryLs = userLabelCategoryLs;
    }*/

    public String getCust_Id() {
        return cust_Id;
    }

    public void setCust_Id(String cust_Id) {
        this.cust_Id = cust_Id;
    }

    public String getUser_type() {
        return user_type;
    }

    public void setUser_type(String user_type) {
        this.user_type = user_type;
    }
}
