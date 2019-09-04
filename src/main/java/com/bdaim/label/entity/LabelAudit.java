package com.bdaim.label.entity;

import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.rbac.entity.User;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "label_audit")
@DynamicUpdate(true)
// 标签审核
public class LabelAudit {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column
	private Integer aid;
	@Column
	private String name;
	@Column
	private Integer status;
	@Column(name = "audit_time")
	private Date auditTime;
	@Column(name = "apply_time")
	private Date applyTime;
	@Column
	private Integer availably;
	@Column(name = "apply_type")
	private Integer applyType;
	@Column(name = "audit_msg")
	private String auditMsg;
	@Column(name = "offline_msg")
	private String offlineMsg;
	@Column(name = "audit_type")
	private Integer auditType;
	@Column(name = "last_flag")
	private Integer lastFlag;
	@Column(name = "audit_result")
	private Integer auditResult;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "label_id")
	private LabelInfo labelInfo;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "group_id")
	private CustomGroup customGroup;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "dev_uid")
	private User devUser;// 指定的开发人员id
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "apply_uid")
	private User applyUser;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "audit_uid")
	private User auditUser;
	@Column(name = "offline_status")
	private Integer offlineStatus;
	@Column(name = "create_time")
	private Date createTime;
	@Column(name = "node_id")
	private Integer nodeId;
	public String getAuditMsg() {
		return auditMsg;
	}

	public void setAuditMsg(String auditMsg) {
		this.auditMsg = auditMsg;
	}

	public Integer getApplyType() {
		return applyType;
	}

	public void setApplyType(Integer applyType) {
		this.applyType = applyType;
	}

	public Date getApplyTime() {
		return applyTime;
	}

	public void setApplyTime(Date applyTime) {
		this.applyTime = (Date)applyTime.clone();
	}

	public Integer getAvailably() {
		return availably;
	}

	public void setAvailably(Integer availably) {
		this.availably = availably;
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Date getAuditTime() {
		return auditTime;
	}

	public void setAuditTime(Date auditTime) {
		this.auditTime = (Date)auditTime.clone();
	}

	public Integer getAid() {
		return aid;
	}

	public void setAid(Integer aid) {
		this.aid = aid;
	}

	public String getOfflineMsg() {
		return offlineMsg;
	}

	public void setOfflineMsg(String offlineMsg) {
		this.offlineMsg = offlineMsg;
	}

	public Integer getAuditType() {
		return auditType;
	}

	public void setAuditType(Integer auditType) {
		this.auditType = auditType;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public User getDevUser() {
		return devUser;
	}

	public void setDevUser(User devUser) {
		this.devUser = devUser;
	}

	public User getApplyUser() {
		return applyUser;
	}

	public void setApplyUser(User applyUser) {
		this.applyUser = applyUser;
	}

	public User getAuditUser() {
		return auditUser;
	}

	public void setAuditUser(User auditUser) {
		this.auditUser = auditUser;
	}

	public Integer getLastFlag() {
		return lastFlag;
	}

	public void setLastFlag(Integer lastFlag) {
		this.lastFlag = lastFlag;
	}

	public Integer getAuditResult() {
		return auditResult;
	}

	public void setAuditResult(Integer auditResult) {
		this.auditResult = auditResult;
	}

	public LabelInfo getLabelInfo() {
		return labelInfo;
	}

	public void setLabelInfo(LabelInfo labelInfo) {
		this.labelInfo = labelInfo;
	}

	public CustomGroup getCustomGroup() {
		return customGroup;
	}

	public void setCustomGroup(CustomGroup customGroup) {
		this.customGroup = customGroup;
	}

	public Integer getOfflineStatus() {
		return offlineStatus;
	}

	public void setOfflineStatus(Integer offlineStatus) {
		this.offlineStatus = offlineStatus;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Integer getNodeId() {
		return nodeId;
	}

	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}

}
