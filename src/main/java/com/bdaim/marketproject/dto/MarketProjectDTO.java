package com.bdaim.marketproject.dto;

import com.bdaim.marketproject.entity.MarketProject;

import java.sql.Timestamp;

/**
 * 营销项目实体类
 *
 * @author chengning@salescomm.net
 * @date 2018/11/26
 * @description
 */
public class MarketProjectDTO {
    private Integer id;
    private String name;
    private Integer industryId;
    private String industryName;
    private Integer status;
    private Timestamp createTime;
    private Integer relationCustNum;
    private String enterpriseName;
    private String custId;
    //是否是全局项目  1：是  2：不是
    private String type;

    public MarketProjectDTO() {
    }

    public MarketProjectDTO(int id, String name, Integer industryId, Integer status, Timestamp createTime) {
        this.id = id;
        this.name = name;
        this.industryId = industryId;
        this.status = status;
        this.createTime = createTime;
    }

    public MarketProjectDTO(MarketProject marketProject) {
        this.id = marketProject.getId();
        this.name = marketProject.getName();
        this.industryId = marketProject.getIndustryId();
        this.status = marketProject.getStatus();
        this.createTime = marketProject.getCreateTime();
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getIndustryId() {
        return industryId;
    }

    public void setIndustryId(Integer industryId) {
        this.industryId = industryId;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getIndustryName() {
        return industryName;
    }

    public void setIndustryName(String industryName) {
        this.industryName = industryName;
    }

    public Integer getRelationCustNum() {
        return relationCustNum;
    }

    public void setRelationCustNum(Integer relationCustNum) {
        this.relationCustNum = relationCustNum;
    }

    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Override
    public String toString() {
        return "MarketProjectDTO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", industryId=" + industryId +
                ", industryName='" + industryName + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", relationCustNum=" + relationCustNum +
                ", enterpriseName='" + enterpriseName + '\'' +
                ", custId='" + custId + '\'' +
                ", type='" + type + '\'' +
                '}';
    }
}
