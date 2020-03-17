package com.bdaim.crm.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

@Entity
@DynamicUpdate
@DynamicInsert
@Table(name = "lkcrm_admin_field", schema = "", catalog = "")
public class LkCrmAdminFieldEntity {
    private Integer fieldId;
    private String custId;
    private String fieldName;
    private String name;
    private int type;
    private int label;
    private String remark;
    private String inputTips;
    private Integer maxLength;
    private String defaultValue;
    private Integer isUnique;
    private Integer isNull;
    private Integer sorting;
    private String options;
    private Integer operating;
    private Timestamp updateTime;
    private Integer examineCategoryId;
    private Integer fieldType;
    private Integer relevant;

    private Integer addSort;
    private Integer addHidden;

    @Id
    @Column(name = "field_id")
    @GeneratedValue
    public Integer getFieldId() {
        return fieldId;
    }

    public void setFieldId(Integer fieldId) {
        this.fieldId = fieldId;
    }

    @Basic
    @Column(name = "cust_id")
    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Basic
    @Column(name = "field_name")
    public String getFieldName() {
        return fieldName;
    }

    public void setFieldName(String fieldName) {
        this.fieldName = fieldName;
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
    @Column(name = "type")
    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    @Basic
    @Column(name = "label")
    public int getLabel() {
        return label;
    }

    public void setLabel(int label) {
        this.label = label;
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
    @Column(name = "input_tips")
    public String getInputTips() {
        return inputTips;
    }

    public void setInputTips(String inputTips) {
        this.inputTips = inputTips;
    }

    @Basic
    @Column(name = "max_length")
    public Integer getMaxLength() {
        return maxLength;
    }

    public void setMaxLength(Integer maxLength) {
        this.maxLength = maxLength;
    }

    @Basic
    @Column(name = "default_value")
    public String getDefaultValue() {
        return defaultValue;
    }

    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }

    @Basic
    @Column(name = "is_unique")
    public Integer getIsUnique() {
        return isUnique;
    }

    public void setIsUnique(Integer isUnique) {
        this.isUnique = isUnique;
    }

    @Basic
    @Column(name = "is_null")
    public Integer getIsNull() {
        return isNull;
    }

    public void setIsNull(Integer isNull) {
        this.isNull = isNull;
    }

    @Basic
    @Column(name = "sorting")
    public Integer getSorting() {
        return sorting;
    }

    public void setSorting(Integer sorting) {
        this.sorting = sorting;
    }

    @Basic
    @Column(name = "options")
    public String getOptions() {
        return options;
    }

    public void setOptions(String options) {
        this.options = options;
    }

    @Basic
    @Column(name = "operating")
    public Integer getOperating() {
        return operating;
    }

    public void setOperating(Integer operating) {
        this.operating = operating;
    }

    @Basic
    @Column(name = "update_time")
    public Timestamp getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Timestamp updateTime) {
        this.updateTime = updateTime;
    }

    @Basic
    @Column(name = "examine_category_id")
    public Integer getExamineCategoryId() {
        return examineCategoryId;
    }

    public void setExamineCategoryId(Integer examineCategoryId) {
        this.examineCategoryId = examineCategoryId;
    }

    @Basic
    @Column(name = "field_type")
    public Integer getFieldType() {
        return fieldType;
    }

    public void setFieldType(Integer fieldType) {
        this.fieldType = fieldType;
    }

    @Basic
    @Column(name = "relevant")
    public Integer getRelevant() {
        return relevant;
    }

    public void setRelevant(Integer relevant) {
        this.relevant = relevant;
    }

    @Basic
    @Column(name = "add_sort")
    public Integer getAddSort() {
        return addSort;
    }

    public void setAddSort(Integer addSort) {
        this.addSort = addSort;
    }

    @Basic
    @Column(name = "add_hidden")
    public Integer getAddHidden() {
        return addHidden;
    }

    public void setAddHidden(Integer addHidden) {
        this.addHidden = addHidden;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAdminFieldEntity that = (LkCrmAdminFieldEntity) o;
        return fieldId == that.fieldId &&
                type == that.type &&
                label == that.label &&
                fieldType == that.fieldType &&
                Objects.equals(fieldName, that.fieldName) &&
                Objects.equals(name, that.name) &&
                Objects.equals(remark, that.remark) &&
                Objects.equals(inputTips, that.inputTips) &&
                Objects.equals(maxLength, that.maxLength) &&
                Objects.equals(defaultValue, that.defaultValue) &&
                Objects.equals(isUnique, that.isUnique) &&
                Objects.equals(isNull, that.isNull) &&
                Objects.equals(sorting, that.sorting) &&
                Objects.equals(options, that.options) &&
                Objects.equals(operating, that.operating) &&
                Objects.equals(updateTime, that.updateTime) &&
                Objects.equals(examineCategoryId, that.examineCategoryId) &&
                Objects.equals(relevant, that.relevant);
    }

    @Override
    public int hashCode() {
        return Objects.hash(fieldId, fieldName, name, type, label, remark, inputTips, maxLength, defaultValue, isUnique, isNull, sorting, options, operating, updateTime, examineCategoryId, fieldType, relevant);
    }
}
