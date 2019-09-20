package com.bdaim.common.entity;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/20
 * @description
 */
public class HFilePK implements Serializable {
    private long id;
    private String type;

    @Column(name = "id")
    @Id
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "type")
    @Id
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HFilePK hFilePK = (HFilePK) o;
        return id == hFilePK.id &&
                Objects.equals(type, hFilePK.type);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, type);
    }
}
