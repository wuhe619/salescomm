package com.bdaim.marketproject.dto;



import com.bdaim.marketproject.entity.MarketProject;

import java.sql.Timestamp;

public class MarketProjectParam {
    private Integer id;
    private String name;
    private Integer industryId;
    private String industryName;
    private Integer status;
    private Timestamp createTime;
    private Integer relationCustNum;
    /**
     * 1:系统级别 2：客户级别
     */
    private Integer type;

    private String custId;
    /**
     * 项目管理员ID
     */
    private String projectUserId;

    private String startTime;

    private String endTime;

    private Integer pageSize;

    private Integer pageNum;


    public MarketProjectParam() {
    }

    public MarketProjectParam(int id, String name, Integer industryId, Integer status, Timestamp createTime) {
        this.id = id;
        this.name = name;
        this.industryId = industryId;
        this.status = status;
        this.createTime = createTime;
    }

    public MarketProjectParam(MarketProject marketProject) {
        this.id = marketProject.getId();
        this.name = marketProject.getName();
        this.industryId = marketProject.getIndustryId();
        this.status = marketProject.getStatus();
        this.createTime = marketProject.getCreateTime();
        //this.type = marketProject.getType();
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

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getProjectUserId() {
        return projectUserId;
    }

    public void setProjectUserId(String projectUserId) {
        this.projectUserId = projectUserId;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    @Override
    public String toString() {
        return "MarketProjectParam{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", industryId=" + industryId +
                ", industryName='" + industryName + '\'' +
                ", status=" + status +
                ", createTime=" + createTime +
                ", relationCustNum=" + relationCustNum +
                ", type=" + type +
                ", custId='" + custId + '\'' +
                ", projectUserId='" + projectUserId + '\'' +
                ", startTime='" + startTime + '\'' +
                ", endTime='" + endTime + '\'' +
                ", pageSize=" + pageSize +
                ", pageNum=" + pageNum +
                '}';
    }
}
