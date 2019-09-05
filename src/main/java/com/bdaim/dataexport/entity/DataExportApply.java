package com.bdaim.dataexport.entity;

import com.bdaim.rbac.entity.User;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="data_export_apply")
public class DataExportApply {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(name="apply_reason")
	private Integer applyReason;
	@Column(name="apply_reason_detail")
	private String applyReasonDetail;
	@Column(name="start_time")
	private Date startTime;
	@Column(name="end_time")
	private Date endTime;
	@Column
	private Integer status;
	@Column(name="create_time")
	private Date createTime;
	@ManyToOne
	@JoinColumn(name = "apply_user")
	private User applyUser;
	@Column(name="update_time")
	private Date updateTime;
	@ManyToOne
	@JoinColumn(name = "update_user")
	private User updateUser;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getApplyReason() {
		return applyReason;
	}
	public void setApplyReason(Integer applyReason) {
		this.applyReason = applyReason;
	}
	public String getApplyReasonDetail() {
		return applyReasonDetail;
	}
	public void setApplyReasonDetail(String applyReasonDetail) {
		this.applyReasonDetail = applyReasonDetail;
	}
	public Date getStartTime() {
		return startTime;
	}
	public void setStartTime(Date startTime) {
		this.startTime = (Date)startTime.clone();
	}
	public Date getEndTime() {
		return endTime;
	}
	public void setEndTime(Date endTime) {
		this.endTime = (Date)endTime.clone();
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = (Date)createTime.clone();
	}
	public Date getUpdateTime() {
		return updateTime;
	}
	public void setUpdateTime(Date updateTime) {
		this.updateTime = (Date)updateTime.clone();
	}
	public User getApplyUser() {
		return applyUser;
	}
	public void setApplyUser(User applyUser) {
		this.applyUser = applyUser;
	}
	public User getUpdateUser() {
		return updateUser;
	}
	public void setUpdateUser(User updateUser) {
		this.updateUser = updateUser;
	}
	
}
