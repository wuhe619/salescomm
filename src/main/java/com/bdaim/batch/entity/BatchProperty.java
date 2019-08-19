package com.bdaim.batch.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @description: 批次属性表 - 实体类
 * @auther: Chacker
 * @date: 2019/8/1 19:21
 */
@Entity
@Table(name = "nl_batch_property")
public class BatchProperty implements Serializable {
    /**
     * 批次ID
     */
    @Id
    @Column(name = "batch_id")
    private String batchId;
    /**
     * 批次名称
     */
    @Id
    @Column(name = "property_name")
    private String propertyName;
    /**
     * 批次属性
     */
    @Column(name = "property_value")
    private String propertyValue;
    /**
     * 创建时间
     */
    @Column(name = "create_time")
    private String createTime;

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
