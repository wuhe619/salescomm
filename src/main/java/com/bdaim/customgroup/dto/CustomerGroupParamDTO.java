package com.bdaim.customgroup.dto;


public class CustomerGroupParamDTO {

	private String label;
	private String name;
	private String purpose;
	private Integer total;
	private String num;
	private String poolId;
	private String industryPoolName;
	private String custId;
	private String createUserId;
	private String updateUserId;
	private String enterpriseName;
	private String filter;
	private String start_date;
	private String end_date;
	private String touchType;

	/**
	 * 项目ID
	 */
	private String projectId;


	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public Integer getTotal() {
		return total;
	}

	public void setTotal(Integer total) {
		this.total = total;
	}

	public String getNum() {
		return num;
	}

	public void setNum(String num) {
		this.num = num;
	}

	public String getPoolId() {
		return poolId;
	}

	public void setPoolId(String poolId) {
		this.poolId = poolId;
	}

	public String getIndustryPoolName() {
		return industryPoolName;
	}

	public void setIndustryPoolName(String industryPoolName) {
		this.industryPoolName = industryPoolName;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getUpdateUserId() {
		return updateUserId;
	}

	public void setUpdateUserId(String updateUserId) {
		this.updateUserId = updateUserId;
	}

	public String getEnterpriseName() {
		return enterpriseName;
	}

	public void setEnterpriseName(String enterpriseName) {
		this.enterpriseName = enterpriseName;
	}

	public String getFilter() {
		return filter;
	}

	public void setFilter(String filter) {
		this.filter = filter;
	}

	public String getStart_date() {
		return start_date;
	}

	public void setStart_date(String start_date) {
		this.start_date = start_date;
	}

	public String getEnd_date() {
		return end_date;
	}

	public void setEnd_date(String end_date) {
		this.end_date = end_date;
	}

	public String getTouchType() {
		return touchType;
	}

	public void setTouchType(String touchType) {
		this.touchType = touchType;
	}

	public String getProjectId() {
		return projectId;
	}

	public void setProjectId(String projectId) {
		this.projectId = projectId;
	}

	@Override
	public String toString() {
		return "CustomerGroupParamDTO{" +
				"label='" + label + '\'' +
				", name='" + name + '\'' +
				", purpose='" + purpose + '\'' +
				", total=" + total +
				", num='" + num + '\'' +
				", poolId='" + poolId + '\'' +
				", industryPoolName='" + industryPoolName + '\'' +
				", custId='" + custId + '\'' +
				", createUserId='" + createUserId + '\'' +
				", updateUserId='" + updateUserId + '\'' +
				", enterpriseName='" + enterpriseName + '\'' +
				", filter='" + filter + '\'' +
				", start_date='" + start_date + '\'' +
				", end_date='" + end_date + '\'' +
				", touchType='" + touchType + '\'' +
				", projectId='" + projectId + '\'' +
				'}';
	}
}
