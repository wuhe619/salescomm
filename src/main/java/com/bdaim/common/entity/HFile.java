package com.bdaim.common.entity;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/20
 * @description
 */
@Entity
@Table(name = "h_file", schema = "bp", catalog = "")
@IdClass(HFilePK.class)
public class HFile {
    private long id;
    private String type;
    private String content;
    private Integer custId;
    private String custGroupId;
    private Integer custUserId;
    private Integer createId;
    private Timestamp createDate;
    private Integer updateId;
    private Timestamp updateDate;
    private String fileType;
    private Integer fileSize;
    private String fileName;
    private String fileId;
    private String ext1;

    @Id
    @Column(name = "id")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Id
    @Column(name = "type")
    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    @Basic
    @Column(name = "content")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Basic
    @Column(name = "cust_id")
    public Integer getCustId() {
        return custId;
    }

    public void setCustId(Integer custId) {
        this.custId = custId;
    }

    @Basic
    @Column(name = "cust_group_id")
    public String getCustGroupId() {
        return custGroupId;
    }

    public void setCustGroupId(String custGroupId) {
        this.custGroupId = custGroupId;
    }

    @Basic
    @Column(name = "cust_user_id")
    public Integer getCustUserId() {
        return custUserId;
    }

    public void setCustUserId(Integer custUserId) {
        this.custUserId = custUserId;
    }

    @Basic
    @Column(name = "create_id")
    public Integer getCreateId() {
        return createId;
    }

    public void setCreateId(Integer createId) {
        this.createId = createId;
    }

    @Basic
    @Column(name = "create_date")
    public Timestamp getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Timestamp createDate) {
        this.createDate = createDate;
    }

    @Basic
    @Column(name = "update_id")
    public Integer getUpdateId() {
        return updateId;
    }

    public void setUpdateId(Integer updateId) {
        this.updateId = updateId;
    }

    @Basic
    @Column(name = "update_date")
    public Timestamp getUpdateDate() {
        return updateDate;
    }

    public void setUpdateDate(Timestamp updateDate) {
        this.updateDate = updateDate;
    }

    @Basic
    @Column(name = "file_type")
    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }

    @Basic
    @Column(name = "file_size")
    public Integer getFileSize() {
        return fileSize;
    }

    public void setFileSize(Integer fileSize) {
        this.fileSize = fileSize;
    }

    @Basic
    @Column(name = "file_name")
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Basic
    @Column(name = "file_id")
    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    @Basic
    @Column(name = "ext_1")
    public String getExt1() {
        return ext1;
    }

    public void setExt1(String ext1) {
        this.ext1 = ext1;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HFile hFile = (HFile) o;
        return id == hFile.id &&
                Objects.equals(type, hFile.type) &&
                Objects.equals(content, hFile.content) &&
                Objects.equals(custId, hFile.custId) &&
                Objects.equals(custGroupId, hFile.custGroupId) &&
                Objects.equals(custUserId, hFile.custUserId) &&
                Objects.equals(createId, hFile.createId) &&
                Objects.equals(createDate, hFile.createDate) &&
                Objects.equals(updateId, hFile.updateId) &&
                Objects.equals(updateDate, hFile.updateDate) &&
                Objects.equals(fileType, hFile.fileType) &&
                Objects.equals(fileSize, hFile.fileSize) &&
                Objects.equals(fileName, hFile.fileName) &&
                Objects.equals(fileId, hFile.fileId) &&
                Objects.equals(ext1, hFile.ext1);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, type, content, custId, custGroupId, custUserId, createId, createDate, updateId, updateDate, fileType, fileSize, fileName, fileId, ext1);
    }

    @Override
    public String toString() {
        return "HFile{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", content='" + content + '\'' +
                ", custId=" + custId +
                ", custGroupId='" + custGroupId + '\'' +
                ", custUserId=" + custUserId +
                ", createId=" + createId +
                ", createDate=" + createDate +
                ", updateId=" + updateId +
                ", updateDate=" + updateDate +
                ", fileType='" + fileType + '\'' +
                ", fileSize=" + fileSize +
                ", fileName='" + fileName + '\'' +
                ", fileId='" + fileId + '\'' +
                ", ext1='" + ext1 + '\'' +
                '}';
    }
}
