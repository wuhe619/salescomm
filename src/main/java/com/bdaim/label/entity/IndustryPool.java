package com.bdaim.label.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 *  行业标签池
 */
@Entity
@Table(name = "t_industry_pool", schema = "", catalog = "")
public class IndustryPool {
	//行业标签池ID
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "industry_pool_id")
    private Integer industryPoolId;
    //标签池类型
	@Basic
    @Column(name = "industry_pool_type")
    private Integer industryPoolType;
    @Column(name = "source_id")
    private Integer sourceId;
    @Column(name = "source_name")
    private String sourceName;
    @Column(name = "status")
    private Integer status;
    @Column(name = "approvel_status")
    private Integer approvelStatus;
    @Column(name = "label_num")
    private Integer labelNum;
    @Column(name = "name")
    private String name;
    @Column(name = "description")
    private String description;
    @Column(name = "tag_tree")
    private String tagTree;
    @Column(name = "creator")
    private String creator;
    @Column(name = "operator")
    private String operator;
    @Column(name = "remark")
    private String remark;
    @Column(name = "update_time")
    private Timestamp updateTime;
    @Column(name = "create_time")
    private Timestamp createTime;

    @Column(name = "auto_extraction")
	private Integer autoExtraction;


	public Integer getAutoExtraction() {
		return autoExtraction;
	}

	public void setAutoExtraction(Integer autoExtraction) {
		this.autoExtraction = autoExtraction;
	}

	public Integer getIndustryPoolId() {
		return industryPoolId;
	}

	public void setIndustryPoolId(Integer industryPoolId) {
		this.industryPoolId = industryPoolId;
	}

	public Integer getIndustryPoolType() {
		return industryPoolType;
	}

	public void setIndustryPoolType(Integer industryPoolType) {
		this.industryPoolType = industryPoolType;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public Integer getApprovelStatus() {
		return approvelStatus;
	}

	public void setApprovelStatus(Integer approvelStatus) {
		this.approvelStatus = approvelStatus;
	}

	public Integer getLabelNum() {
		return labelNum;
	}

	public void setLabelNum(Integer labelNum) {
		this.labelNum = labelNum;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getTagTree() {
		return tagTree;
	}

	public void setTagTree(String tagTree) {
		this.tagTree = tagTree;
	}

	public String getCreator() {
		return creator;
	}

	public void setCreator(String creator) {
		this.creator = creator;
	}

	public String getOperator() {
		return operator;
	}

	public void setOperator(String operator) {
		this.operator = operator;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public Timestamp getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Timestamp updateTime) {
		this.updateTime = updateTime;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public Timestamp getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}

	
}
