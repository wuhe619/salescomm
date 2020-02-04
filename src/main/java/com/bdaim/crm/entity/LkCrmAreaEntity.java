package com.bdaim.crm.entity;

import javax.persistence.*;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_area", schema = "", catalog = "")
public class LkCrmAreaEntity {
    private Integer codeId;
    private Integer parentId;
    private String cityName;

    @Id
    @Basic
    @Column(name = "code_id")
    public Integer getCodeId() {
        return codeId;
    }

    public void setCodeId(Integer codeId) {
        this.codeId = codeId;
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
    @Column(name = "city_name")
    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmAreaEntity that = (LkCrmAreaEntity) o;
        return Objects.equals(codeId, that.codeId) &&
                Objects.equals(parentId, that.parentId) &&
                Objects.equals(cityName, that.cityName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(codeId, parentId, cityName);
    }
}
