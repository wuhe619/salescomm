package com.bdaim.label.entity;

import javax.persistence.*;

/*@Entity
@Table(name = "audit_flow")*/
public class AuditFlow {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(name = "audit_type")
	private Integer auditType;
	@Column(name = "apply_type")
	private Integer applyType;
	@Column(name = "apply_name")
	private String applyName;
	@Column(name = "node_id")
	private Integer nodeId;
	@Column(name = "node_name")
	private String nodeName;
	@Column(name = "audit_result")
	private Integer auditResult;
	@Column(name = "audit_result_name")
	private String auditResultName;
	@Column(name = "audit_status")
	private Integer auditStatus;
	@Column(name = "audit_status_name")
	private String auditStatusName;
	@Column
	private Integer availably;
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public Integer getAuditType() {
		return auditType;
	}
	public void setAuditType(Integer auditType) {
		this.auditType = auditType;
	}
	public Integer getApplyType() {
		return applyType;
	}
	public void setApplyType(Integer applyType) {
		this.applyType = applyType;
	}
	public String getApplyName() {
		return applyName;
	}
	public void setApplyName(String applyName) {
		this.applyName = applyName;
	}
	public Integer getNodeId() {
		return nodeId;
	}
	public void setNodeId(Integer nodeId) {
		this.nodeId = nodeId;
	}
	public String getNodeName() {
		return nodeName;
	}
	public void setNodeName(String nodeName) {
		this.nodeName = nodeName;
	}
	public Integer getAuditResult() {
		return auditResult;
	}
	public void setAuditResult(Integer auditResult) {
		this.auditResult = auditResult;
	}
	public String getAuditResultName() {
		return auditResultName;
	}
	public void setAuditResultName(String auditResultName) {
		this.auditResultName = auditResultName;
	}
	public Integer getAuditStatus() {
		return auditStatus;
	}
	public void setAuditStatus(Integer auditStatus) {
		this.auditStatus = auditStatus;
	}
	public String getAuditStatusName() {
		return auditStatusName;
	}
	public void setAuditStatusName(String auditStatusName) {
		this.auditStatusName = auditStatusName;
	}
	public Integer getAvailably() {
		return availably;
	}
	public void setAvailably(Integer availably) {
		this.availably = availably;
	}
	
}
