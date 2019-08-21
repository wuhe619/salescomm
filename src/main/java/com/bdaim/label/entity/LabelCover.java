package com.bdaim.label.entity;

import javax.persistence.*;

/**
 * 标签当前用户覆盖数
 * 
 * 
 *
 */
@Entity
@Table(name = "label_cover")
public class LabelCover {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@ManyToOne
	@JoinColumn(name = "label_id")
	// 标签
	private LabelInfo label;
	@ManyToOne
	@JoinColumn(name = "category_id")
	// 品类
	private LabelCategory category;
	@Column(name = "cover_num")
	// 覆盖用户数
	private Long coverNum;
	@Column(name = "total")
	private Long total;
	// 标签周期
	@Column
	private Integer cycle;
	@Column
	private Integer available;

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LabelInfo getLabel() {
		return label;
	}

	public void setLabel(LabelInfo label) {
		this.label = label;
	}

	public Long getCoverNum() {
		return coverNum;
	}

	public void setCoverNum(Long coverNum) {
		this.coverNum = coverNum;
	}

	public LabelCategory getCategory() {
		return category;
	}

	public void setCategory(LabelCategory category) {
		this.category = category;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Integer getCycle() {
		return cycle;
	}

	public void setCycle(Integer cycle) {
		this.cycle = cycle;
	}

	public Integer getAvailable() {
		return available;
	}

	public void setAvailable(Integer available) {
		this.available = available;
	}

}
