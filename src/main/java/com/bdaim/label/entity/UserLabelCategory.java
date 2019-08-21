package com.bdaim.label.entity;

import com.bdaim.rbac.entity.User;

import javax.persistence.*;


@Entity
@Table(name="user_label_category")
public class UserLabelCategory {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private Long id;
	@ManyToOne
	@JoinColumn(name="label_category_id")
	private LabelCategory labelCategory;
	@ManyToOne
	@JoinColumn(name="user_id")
	private User labelCategoryUser;
	@Column(name="is_default")
	private Integer isDefault;
	
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	public LabelCategory getLabelCategory() {
		return labelCategory;
	}
	public void setLabelCategory(LabelCategory labelCategory) {
		this.labelCategory = labelCategory;
	}
	public User getLabelCategoryUser() {
		return labelCategoryUser;
	}
	public void setLabelCategoryUser(User labelCategoryUser) {
		this.labelCategoryUser = labelCategoryUser;
	}
	public Integer getIsDefault() {
		return isDefault;
	}
	public void setIsDefault(Integer isDefault) {
		this.isDefault = isDefault;
	}
	
}
