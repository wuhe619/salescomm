package com.bdaim.common.entity;

import javax.persistence.*;
import java.io.Serializable;

/**
 * 字典分类
 */
@Entity
@Table(name = "t_dic_type", schema = "", catalog = "")
public class DicType implements Serializable {

    private String id;
    private String name;
    private String description;

    @Id
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
