package com.bdaim.label.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * Created by Mr.YinXin on 2017/2/28.
 */
@Entity
@Table(name = "t_source", schema = "", catalog = "")
public class SourceDO {
    private int sourceId;
    private String sourceName;
    private Integer status;
    private Integer labelNum;
    private String description;
    private String tagTree;
    private Timestamp createTime;
    private Timestamp modifyTime;
    private Integer sourcePrice;
    private Integer type;

    @Id
    @Column(name = "source_id")
    public int getSourceId() {
        return sourceId;
    }

    public void setSourceId(int sourceId) {
        this.sourceId = sourceId;
    }

    @Basic
    @Column(name = "source_name")
    public String getSourceName() {
        return sourceName;
    }

    public void setSourceName(String sourceName) {
        this.sourceName = sourceName;
    }

    @Basic
    @Column(name = "STATUS")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Basic
    @Column(name = "label_num")
    public Integer getLabelNum() {
        return labelNum;
    }

    public void setLabelNum(Integer labelNum) {
        this.labelNum = labelNum;
    }

    @Basic
    @Column(name = "description")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Basic
    @Column(name = "tag_tree")
    public String getTagTree() {
        return tagTree;
    }

    public void setTagTree(String tagTree) {
        this.tagTree = tagTree;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "modify_time")
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

    @Basic
    @Column(name = "source_price")
    public Integer getSourcePrice() {
        return sourcePrice;
    }

    public void setSourcePrice(Integer sourcePrice) {
        this.sourcePrice = sourcePrice;
    }

    @Basic
    @Column(name = "type")
    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        SourceDO sourceDO = (SourceDO) o;

        if (sourceId != sourceDO.sourceId) return false;
        if (sourceName != null ? !sourceName.equals(sourceDO.sourceName) : sourceDO.sourceName != null) return false;
        if (status != null ? !status.equals(sourceDO.status) : sourceDO.status != null) return false;
        if (labelNum != null ? !labelNum.equals(sourceDO.labelNum) : sourceDO.labelNum != null) return false;
        if (description != null ? !description.equals(sourceDO.description) : sourceDO.description != null)
            return false;
        if (tagTree != null ? !tagTree.equals(sourceDO.tagTree) : sourceDO.tagTree != null) return false;
        if (createTime != null ? !createTime.equals(sourceDO.createTime) : sourceDO.createTime != null) return false;
        if (modifyTime != null ? !modifyTime.equals(sourceDO.modifyTime) : sourceDO.modifyTime != null) return false;
        if (sourcePrice != null ? !sourcePrice.equals(sourceDO.sourcePrice) : sourceDO.sourcePrice != null)
            return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = sourceId;
        result = 31 * result + (sourceName != null ? sourceName.hashCode() : 0);
        result = 31 * result + (status != null ? status.hashCode() : 0);
        result = 31 * result + (labelNum != null ? labelNum.hashCode() : 0);
        result = 31 * result + (description != null ? description.hashCode() : 0);
        result = 31 * result + (tagTree != null ? tagTree.hashCode() : 0);
        result = 31 * result + (createTime != null ? createTime.hashCode() : 0);
        result = 31 * result + (modifyTime != null ? modifyTime.hashCode() : 0);
        result = 31 * result + (sourcePrice != null ? sourcePrice.hashCode() : 0);
        return result;
    }
}
