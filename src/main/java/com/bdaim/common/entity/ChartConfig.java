package com.bdaim.common.entity;

import com.alibaba.fastjson.annotation.JSONField;
import com.bdaim.common.dto.Page;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "chart_config")
public class ChartConfig extends Page {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column
	private String title;
	@Column
	private String content;
	@Column(name="show_channel")
	private Integer showChannel = 0; //0 不显示  1显示
	@Column(name="show_cycle")
	private Integer showCycle = 1;
	@Column
	private Integer type = 0;
	@Transient
	List<ChartConfig> children = new ArrayList<ChartConfig>();
	@Transient
	private String name;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	@JSONField
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
	public Integer getShowChannel() {
		return showChannel;
	}
	public void setShowChannel(Integer showChannel) {
		this.showChannel = showChannel;
	}
	public List<ChartConfig> getChildren() {
		return children;
	}
	public void setChildren(List<ChartConfig> children) {
		this.children = children;
	}
	public Integer getShowCycle() {
		return showCycle;
	}
	public void setShowCycle(Integer showCycle) {
		this.showCycle = showCycle;
	}
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}

}
