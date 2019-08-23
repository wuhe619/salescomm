package com.bdaim.industry.dto;

public class IndustryLabelsDTO {
	
	Integer pageNum;
	Integer pageSize;
	Integer industryPoolId;
	Integer sourceId ;
	Integer  secondCategory;
	
	String  labelName;
	String  labelId;
	String  createTimeStart;
	String  createTimeEnd;
	
	
	
	
	public IndustryLabelsDTO() {
		super();
		// TODO Auto-generated constructor stub
	}
	public IndustryLabelsDTO(Integer pageNum, Integer pageSize, Integer industryPoolId, Integer sourceId,
			Integer secondCategory, String labelName, String labelId, String createTimeStart, String createTimeEnd) {
		super();
		this.pageNum = pageNum;
		this.pageSize = pageSize;
		this.industryPoolId = industryPoolId;
		this.sourceId = sourceId;
		this.secondCategory = secondCategory;
		this.labelName = labelName;
		this.labelId = labelId;
		this.createTimeStart = createTimeStart;
		this.createTimeEnd = createTimeEnd;
	}
	public Integer getPageNum() {
		return pageNum;
	}
	public void setPageNum(Integer pageNum) {
		this.pageNum = pageNum;
	}
	public Integer getPageSize() {
		return pageSize;
	}
	public void setPageSize(Integer pageSize) {
		this.pageSize = pageSize;
	}
	public Integer getIndustryPoolId() {
		return industryPoolId;
	}
	public void setIndustryPoolId(Integer industryPoolId) {
		this.industryPoolId = industryPoolId;
	}
	public Integer getSourceId() {
		return sourceId;
	}
	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}
	public Integer getSecondCategory() {
		return secondCategory;
	}
	public void setSecondCategory(Integer secondCategory) {
		this.secondCategory = secondCategory;
	}
	public String getLabelName() {
		return labelName;
	}
	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}
	public String getLabelId() {
		return labelId;
	}
	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}
	public String getCreateTimeStart() {
		return createTimeStart;
	}
	public void setCreateTimeStart(String createTimeStart) {
		this.createTimeStart = createTimeStart;
	}
	public String getCreateTimeEnd() {
		return createTimeEnd;
	}
	public void setCreateTimeEnd(String createTimeEnd) {
		this.createTimeEnd = createTimeEnd;
	}
	
	
	
	
	
     

}
