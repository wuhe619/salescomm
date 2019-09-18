package com.bdaim.common.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/18
 * @description
 */
@Entity
@Table(name = "file_info", schema = "", catalog = "")
public class FileInfo {
    private int id;
    private String serviceId;
    private String objectId;
    private Timestamp createTime;

    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "service_id")
    public String getServiceId() {
        return serviceId;
    }

    public void setServiceId(String serviceId) {
        this.serviceId = serviceId;
    }

    @Basic
    @Column(name = "object_id")
    public String getObjectId() {
        return objectId;
    }

    public void setObjectId(String objectId) {
        this.objectId = objectId;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        FileInfo fileInfo = (FileInfo) o;
        return id == fileInfo.id &&
                Objects.equals(serviceId, fileInfo.serviceId) &&
                Objects.equals(objectId, fileInfo.objectId) &&
                Objects.equals(createTime, fileInfo.createTime);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, serviceId, objectId, createTime);
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "id=" + id +
                ", serviceId='" + serviceId + '\'' +
                ", objectId='" + objectId + '\'' +
                ", createTime=" + createTime +
                '}';
    }
}
