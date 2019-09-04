package com.bdaim.label.entity;

import com.bdaim.customgroup.entity.CustomGroup;

import javax.persistence.*;

@Entity
@Table(name = "customer_label_category")
public class CustomerLabelAndCategory {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@ManyToOne
	@JoinColumn(name = "group_id")
	private CustomGroup customerGroup;
	@ManyToOne
	@JoinColumn(name = "label_id")
	private LabelInfo labelInfo;
	@ManyToOne
	@JoinColumn(name = "category_id")
	private LabelCategory labelCategory;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public CustomGroup getCustomerGroup() {
		return customerGroup;
	}

	public void setCustomerGroup(CustomGroup customerGroup) {
		this.customerGroup = customerGroup;
	}

	public LabelInfo getLabelInfo() {
		return labelInfo;
	}

	public void setLabelInfo(LabelInfo labelInfo) {
		this.labelInfo = labelInfo;
	}

	public LabelCategory getLabelCategory() {
		return labelCategory;
	}

	public void setLabelCategory(LabelCategory labelCategory) {
		this.labelCategory = labelCategory;
	}

}
