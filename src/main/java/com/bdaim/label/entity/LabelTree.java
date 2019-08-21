package com.bdaim.label.entity;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

/*@Entity
@Table(name = "label_tree")*/
public class LabelTree {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(length = 64)
	private String name;
	@Column
	private Integer level;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id")
	private LabelTree parent;
	@OneToMany(cascade = CascadeType.ALL, mappedBy = "parent", fetch = FetchType.LAZY)
	private List<LabelTree> children;
	@Column
	private String path;
	@Column
	private String uri;
	@Column(name = "modify_time")
	private Date modifyTime;
	@Column(name = "create_time")
	private Date createTime;

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

	public void setLevel(int level) {
		this.level = level;
	}

	public LabelTree getParent() {
		return parent;
	}

	public void setParent(LabelTree parent) {
		this.parent = parent;
	}

	public List<LabelTree> getChildren() {
		return children;
	}

	public void setChildren(List<LabelTree> children) {
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

	public void setLevel(Integer level) {
		this.level = level;
	}


}
