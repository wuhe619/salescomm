package com.bdaim.label.vo;

public class LabelPriceVO {

	private Integer industryPoolId;
	
	private Integer sourceId;
	
	private String labelId;
	
	private String labelName;
	
	private Integer salePrice;
	
	private Integer sourcePrice;

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

	public String getIndustryLabelId() {
		return labelId;
	}

	public void setIndustryLabelId(String industryLabelId) {
		this.labelId = industryLabelId;
	}

	public Integer getSourcePrice() {
		return sourcePrice;
	}

	public void setSourcePrice(Integer sourcePrice) {
		this.sourcePrice = sourcePrice;
	}

	public Integer getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(Integer salePrice) {
		this.salePrice = salePrice;
	}

	public String getLabelId() {
		return labelId;
	}

	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}
	
}
