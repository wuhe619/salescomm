package com.bdaim.common.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

/**
 *
 */
public class DicPropertyPK implements Serializable {
    private Long dicId;
    private String dicPropKey;

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
}
