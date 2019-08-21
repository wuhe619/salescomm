package com.bdaim.label.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "label_task")
public class LabelTask {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column
	private Integer lid;
	@Column(name = "label_path")
	private String labelPath;
	@Column(name = "task_status")
	private Integer taskStatus;
	@Column(name = "update_users")
	private Integer updateUsers;
	@Column(name = "update_time")
	private Date updateTime;
	@Column(name = "create_time")
	private Date createTime;
	@Column(name = "warning_status")
	private Integer warningStatus;
	@Column(name = "create_uid")
	private Long createUid;
	@Column
	private Integer availably;
	public Integer getAvailably() {
		return availably;
	}

	public void setAvailably(Integer availably) {
		this.availably = availably;
	}

	public Integer getTaskStatus() {
		return taskStatus;
	}

	public void setTaskStatus(Integer taskStatus) {
		this.taskStatus = taskStatus;
	}

	public Integer getUpdateUsers() {
		return updateUsers;
	}

	public void setUpdateUsers(Integer updateUsers) {
		this.updateUsers = updateUsers;
	}

	public Integer getWarningStatus() {
		return warningStatus;
	}

	public void setWarningStatus(Integer warningStatus) {
		this.warningStatus = warningStatus;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getLid() {
		return lid;
	}

	public void setLid(Integer lid) {
		this.lid = lid;
	}

	public String getLabelPath() {
		return labelPath;
	}

	public void setLabelPath(String labelPath) {
		this.labelPath = labelPath;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = (Date)updateTime.clone();
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = (Date)createTime.clone();
	}

	public Long getCreateUid() {
		return createUid;
	}

	public void setCreateUid(Long createUid) {
		this.createUid = createUid;
	}

}
