package com.bdaim.common.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

/**
 * 字典分类属性
 */
@Entity
@Table(name = "t_dic_type_property", schema = "", catalog = "")
@IdClass(DicTypePropertyPK.class)
public class DicTypeProperty implements Serializable {
    private String dicTypeId;
    private String dicTypePropId;
    private String dicTypePropName;
    private String dicTypePropType;
    private String dicTypePropValues;
    private String dicTypePropIsrequire;

    @Id
    @Column(name = "dic_type_id")
    public String getDicTypeId() {
        return dicTypeId;
    }

    public void setDicTypeId(String dicTypeId) {
        this.dicTypeId = dicTypeId;
    }

    @Id
    @Column(name = "dic_type_prop_id")
    public String getDicTypePropId() {
        return dicTypePropId;
    }

    public void setDicTypePropId(String dicTypePropId) {
        this.dicTypePropId = dicTypePropId;
    }

    @Basic
    @Column(name = "dic_type_prop_name")
    public String getDicTypePropName() {
        return dicTypePropName;
    }

    public void setDicTypePropName(String dicTypePropName) {
        this.dicTypePropName = dicTypePropName;
    }

    @Basic
    @Column(name = "dic_type_prop_type")
    public String getDicTypePropType() {
        return dicTypePropType;
    }

    public void setDicTypePropType(String dicTypePropType) {
        this.dicTypePropType = dicTypePropType;
    }

    @Basic
    @Column(name = "dic_type_prop_values")
    public String getDicTypePropValues() {
        return dicTypePropValues;
    }

    public void setDicTypePropValues(String dicTypePropValues) {
        this.dicTypePropValues = dicTypePropValues;
    }

    @Basic
    @Column(name = "dic_type_prop_isrequire")
    public String getDicTypePropIsrequire() {
        return dicTypePropIsrequire;
    }

    public void setDicTypePropIsrequire(String dicTypePropIsrequire) {
        this.dicTypePropIsrequire = dicTypePropIsrequire;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DicTypeProperty that = (DicTypeProperty) o;
        return Objects.equals(dicTypeId, that.dicTypeId) &&
                Objects.equals(dicTypePropId, that.dicTypePropId) &&
                Objects.equals(dicTypePropName, that.dicTypePropName) &&
                Objects.equals(dicTypePropType, that.dicTypePropType) &&
                Objects.equals(dicTypePropValues, that.dicTypePropValues) &&
                Objects.equals(dicTypePropIsrequire, that.dicTypePropIsrequire);
    }

    @Override
    public int hashCode() {
        return Objects.hash(dicTypeId, dicTypePropId, dicTypePropName, dicTypePropType, dicTypePropValues, dicTypePropIsrequire);
    }
}
