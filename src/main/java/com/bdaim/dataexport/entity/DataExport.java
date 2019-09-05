package com.bdaim.dataexport.entity;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name="data_export")
public class DataExport {

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(name="task_num")
	private String taskNum;
	@Column(name="export_type")
	private Integer exportType;
	@Column
	private String cid;
	@Column(name="customer_group_id")
	private Integer customGroupId;
	@Column(name="output_label")
	private String outputLabel;
	@Column(name="data_type")
	private Integer dataType;
	@Column(name="start_time")
	private Date startTime;
	@Column(name="end_time")
	private Date endTime;
	@Column(name="export_reason")
	private Integer exportReason;
	@Column(name="export_reason_detail")
	private String exportReasonDetail;
	@Column
	private Long applicant;
	@Column(name="create_time")
	private Date createTime;
	@Column(name="filename")
	private String filename;
	@Column(name="path")
	private String path;
	
	@Transient
	private String applicantStr;
	
	public Integer getId() {
		return id;
	}
	public void setId(Integer id) {
		this.id = id;
	}
	public String getTaskNum() {
		return taskNum;
	}
	public void setTaskNum(String taskNum) {
		this.taskNum = taskNum;
	}
	public Integer getExportType() {
		return exportType;
	}
	public void setExportType(Integer exportType) {
		this.exportType = exportType;
	}
	public String getCid() {
		return cid;
	}
	public void setCid(String cid) {
		this.cid = cid;
	}
	public String getOutputLabel() {
		return outputLabel;
	}
	public void setOutputLabel(String outputLabel) {
		this.outputLabel = outputLabel;
	}
	public Integer getDataType() {
		return dataType;
	}
	public void setDataType(Integer dataType) {
		this.dataType = dataType;
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
	public Integer getExportReason() {
		return exportReason;
	}
	public void setExportReason(Integer exportReason) {
		this.exportReason = exportReason;
	}
	public String getExportReasonDetail() {
		return exportReasonDetail;
	}
	public void setExportReasonDetail(String exportReasonDetail) {
		this.exportReasonDetail = exportReasonDetail;
	}
	public Date getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Date createTime) {
		this.createTime = (Date)createTime.clone();
	}
	public Integer getCustomGroupId() {
		return customGroupId;
	}
	public void setCustomGroupId(Integer customGroupId) {
		this.customGroupId = customGroupId;
	}
	public String getFilename() {
		return filename;
	}
	public void setFilename(String filename) {
		this.filename = filename;
	}
	public String getPath() {
		return path;
	}
	public void setPath(String path) {
		this.path = path;
	}
	public Long getApplicant() {
		return applicant;
	}
	public void setApplicant(Long applicant) {
		this.applicant = applicant;
	}
	public String getApplicantStr() {
		return applicantStr;
	}
	public void setApplicantStr(String applicantStr) {
		this.applicantStr = applicantStr;
	}
	
}
