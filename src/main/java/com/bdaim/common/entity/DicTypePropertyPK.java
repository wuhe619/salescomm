package com.bdaim.common.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 *
 */
public class DicTypePropertyPK implements Serializable {
    private String dicTypeId;
    private String dicTypePropId;

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
}

