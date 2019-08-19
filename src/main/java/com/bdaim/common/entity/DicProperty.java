package com.bdaim.common.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 字典属性
 */
@Entity
@Table(name = "t_dic_property", schema = "", catalog = "")
@IdClass(DicPropertyPK.class)
public class DicProperty implements Serializable {


    private Long dicId;
    private String dicPropKey;

    private String dicPropValue;

    public DicProperty() {
    }

    public DicProperty(Long dicId, String dicPropKey, String dicPropValue) {
        this.dicId = dicId;
        this.dicPropKey = dicPropKey;
        this.dicPropValue = dicPropValue;
    }

    @Id
    @Column(name = "dic_id")
    public Long getDicId() {
        return dicId;
    }

    public void setDicId(Long dicId) {
        this.dicId = dicId;
    }

    @Id
    @Column(name = "dic_prop_key")
    public String getDicPropKey() {
        return dicPropKey;
    }

    public void setDicPropKey(String dicPropKey) {
        this.dicPropKey = dicPropKey;
    }

    @Column(name = "dic_prop_value")
    public String getDicPropValue() {
        return dicPropValue;
    }

    public void setDicPropValue(String dicPropValue) {
        this.dicPropValue = dicPropValue;
    }


}
