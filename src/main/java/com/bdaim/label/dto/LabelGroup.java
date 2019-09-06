package com.bdaim.label.dto;


import com.bdaim.label.entity.LabelInfo;

import java.util.ArrayList;
import java.util.List;

public class LabelGroup {

	private Integer id = 0;
	private String labelName = null;
	private String labelId = null;
	private Integer dataFormat = null;
	private Integer status = 3;
	
	private List<LabelGroup> children = null;

	public LabelGroup() {
		
	}
	public LabelGroup(LabelInfo li) {
		this.id = li.getId();
		if(li.getType()!=null && li.getType()==3)
			this.labelId = String.valueOf(this.id);
		else if(li.getType()!=null && li.getType()==4)
			this.labelId = String.valueOf(this.id);
		else
			this.labelId = String.valueOf(this.id);
		
		this.labelName = li.getLabelName();
		this.dataFormat = li.getDataFormat();
		if(this.dataFormat==null || (this.dataFormat!=0 && this.dataFormat!=1))
			this.dataFormat=-1;
		
		this.status = li.getStatus();
	}
	
	
	public void addChile(LabelGroup lg) {
		if(this.children==null)
			this.children = new ArrayList();
		this.children.add(lg);
	}
	
	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
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

	public Integer getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(Integer dataFormat) {
		this.dataFormat = dataFormat;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public List<LabelGroup> getChildren() {
		return children;
	}

}
