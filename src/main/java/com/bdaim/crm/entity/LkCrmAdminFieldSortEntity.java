package com.bdaim.crm.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_admin_field_sort", schema = "", catalog = "")
public class LkCrmAdminFieldSortEntity {
    private int id;
    private int label;
    private String fieldName;
    private String name;
    private int sort;
    private long userId;
    private int isHide;
    private Integer fieldId;

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
    @Column(name = "label")
    public int getLabel() {
        return label;
    }

    public LkCrmAdminFieldSortEntity setLabel(int label) {
        this.label = label;
        return this;
    }

    @Basic
    @Column(name = "field_name")
    public String getFieldName() {
        return fieldName;
    }

    public LkCrmAdminFieldSortEntity setFieldName(String fieldName) {
        this.fieldName = fieldName;
        return this;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public LkCrmAdminFieldSortEntity setName(String name) {
        this.name = name;
        return this;
    }

    @Basic
    @Column(name = "sort")
    public int getSort() {
        return sort;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    @Basic
    @Column(name = "user_id")
    public long getUserId() {
        return userId;
    }

    public LkCrmAdminFieldSortEntity setUserId(long userId) {
        this.userId = userId;
        return this;
    }

    @Basic
    @Column(name = "is_hide")
    public int getIsHide() {
        return isHide;
    }

    public LkCrmAdminFieldSortEntity setIsHide(int isHide) {
        this.isHide = isHide;
        return this;
    }

    @Basic
    @Column(name = "field_id")
    public Integer getFieldId() {
        return fieldId;
    }

    public void setFieldId(Integer fieldId) {
        this.fieldId = fieldId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAdminFieldSortEntity that = (LkCrmAdminFieldSortEntity) o;
        return id == that.id &&
                label == that.label &&
                sort == that.sort &&
                userId == that.userId &&
                isHide == that.isHide &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(name, that.name) &&
                Objects.equals(fieldId, that.fieldId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, label, fieldName, name, sort, userId, isHide, fieldId);
    }
}
