package com.bdaim.crm.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_menu", schema = "", catalog = "")
public class LkCrmAdminMenuEntity {
    private Integer menuId;
    private Integer parentId;
    private String menuName;
    private String realm;
    private Integer menuType;
    private Integer sort;
    private Integer status;
    private String remarks;

    @Id
    @Column(name = "menu_id")
    @GeneratedValue
    public Integer getMenuId() {
        return menuId;
    }

    public void setMenuId(Integer menuId) {
        this.menuId = menuId;
    }

    @Basic
    @Column(name = "parent_id")
    public Integer getParentId() {
        return parentId;
    }

    public void setParentId(Integer parentId) {
        this.parentId = parentId;
    }

    @Basic
    @Column(name = "menu_name")
    public String getMenuName() {
        return menuName;
    }

    public void setMenuName(String menuName) {
        this.menuName = menuName;
    }

    @Basic
    @Column(name = "realm")
    public String getRealm() {
        return realm;
    }

    public void setRealm(String realm) {
        this.realm = realm;
    }

    @Basic
    @Column(name = "menu_type")
    public Integer getMenuType() {
        return menuType;
    }

    public void setMenuType(Integer menuType) {
        this.menuType = menuType;
    }

    @Basic
    @Column(name = "sort")
    public Integer getSort() {
        return sort;
    }

    public void setSort(Integer sort) {
        this.sort = sort;
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
    @Column(name = "remarks")
    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAdminMenuEntity that = (LkCrmAdminMenuEntity) o;
        return menuId == that.menuId &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(menuName, that.menuName) &&
                Objects.equals(realm, that.realm) &&
                Objects.equals(menuType, that.menuType) &&
                Objects.equals(sort, that.sort) &&
                Objects.equals(status, that.status) &&
                Objects.equals(remarks, that.remarks);
    }

    @Override
    public int hashCode() {
        return Objects.hash(menuId, parentId, menuName, realm, menuType, sort, status, remarks);
    }
}
