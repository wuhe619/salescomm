package com.bdaim.label.entity;

import com.bdaim.rbac.entity.User;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/**
 * 标签品类品牌
 * 
 *
 */
@Entity
@Table(name = "label_category")
public class LabelCategory {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(length = 64)
	private String name;
	@Column(name = "category_id")
	private String categoryId;
	@Column(name = "\"LEVEL\"")
	private Integer level;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private LabelCategory parent;
	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	private List<LabelCategory> children;
	@Column
	private Integer type;// 0:媒体1:电商
	@Column
	private String path;
	@Column
	private String uri;
	@Column(name = "order_num")
	private Integer orderNum;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "create_uid")
	private User createUser;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "modify_uid")
	private User modifyUser;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "class_id")
	private LabelInfo parentClass;
	@OneToMany(mappedBy = "parentCategory", fetch = FetchType.LAZY)
	private List<LabelInfo> childrenLabel;
	@Column(name = "modify_time")
	private Date modifyTime;
	@Column(name = "create_time")
	private Date createTime;

	@Column(name = "is_leaf")
	private Integer isLeaf;

	@OneToMany(mappedBy = "labelCategory", fetch = FetchType.LAZY)
	private List<UserLabelCategory> userLabelCategoryLs;


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

	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public LabelCategory getParent() {
		return parent;
	}

	public void setParent(LabelCategory parent) {
		this.parent = parent;
	}

	public List<LabelCategory> getChildren() {
		return children;
	}

	public void setChildren(List<LabelCategory> children) {
		this.children = children;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public Date getModifyTime() {
		return modifyTime;
	}

	public void setModifyTime(Date modifyTime) {
		this.modifyTime = (Date)modifyTime.clone();
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = (Date)createTime.clone();
	}

	public List<LabelInfo> getChildrenLabel() {
		return childrenLabel;
	}

	public void setChildrenLabel(List<LabelInfo> childrenLabel) {
		this.childrenLabel = childrenLabel;
	}

	public Integer getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	public User getCreateUser() {
		return createUser;
	}

	public void setCreateUser(User createUser) {
		this.createUser = createUser;
	}

	public User getModifyUser() {
		return modifyUser;
	}

	public void setModifyUser(User modifyUser) {
		this.modifyUser = modifyUser;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public String getCategoryId() {
		return categoryId;
	}

	public void setCategoryId(String categoryId) {
		this.categoryId = categoryId;
	}

	public LabelInfo getParentClass() {
		return parentClass;
	}

	public void setParentClass(LabelInfo parentClass) {
		this.parentClass = parentClass;
	}

	public Integer getIsLeaf() {
		return isLeaf;
	}

	public void setIsLeaf(Integer isLeaf) {
		this.isLeaf = isLeaf;
	}

	public List<UserLabelCategory> getUserLabelCategoryLs() {
		return userLabelCategoryLs;
	}

	public void setUserLabelCategoryLs(
			List<UserLabelCategory> userLabelCategoryLs) {
		this.userLabelCategoryLs = userLabelCategoryLs;
	}

}
