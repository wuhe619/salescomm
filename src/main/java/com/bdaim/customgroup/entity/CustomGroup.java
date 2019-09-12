package com.bdaim.customgroup.entity;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.Date;

@Entity
@Table(name = "customer_group")
@DynamicUpdate(true)
public class CustomGroup {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column
	private String name;
	@Column(name = "description")
	private String desc;
	@Column
	private Integer type;
	@Column(name = "user_count")
	private Long userCount;
	@Column
	private Long total;
	@Column
	private Integer status;
	@Column(name = "create_time")
	private Date createTime;
	@Column(name = "update_time")
	private Date updateTime;
//	@OneToMany(mappedBy = "customGroup", fetch = FetchType.LAZY)
//	private List<LabelAudit> labelAudits;
//	@JoinColumn(name = "create_uid")
	@Column(name = "create_uid")
	private String createUserId;
//	@JoinColumn(name = "update_uid")
	@Column(name = "update_uid")
	private String updateUserId;
//	@ManyToMany(fetch = FetchType.LAZY)
//	@JoinTable(name = "custom_rel", joinColumns = { @JoinColumn(name = "cid", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "lid") })
//	private List<LabelInfo> labels;
	@Column
	private Integer availably;
	@Column
	private String purpose;
	@Column(name = "update_cycle")
	private Integer updateCycle;
	@Column(name = "group_condition")
	private String groupCondition;
	@Column
	private String api;
	@Column(name = "start_time")
	private Date startTime;
	@Column(name = "end_time")
	private Date endTime;
	@Column(name = "download_count")
	private Integer downloadCount;
	@Column
	// 时间周期0、全部 1、7天 2、15天 3、30天
	private Integer cycle;
//	@OneToMany(mappedBy = "customerGroup", fetch = FetchType.LAZY)
//	private List<CustomerLabelAndCategory> customerLabelAndCategorys;
	@Transient
	private String grouping;
	@Transient
	private String lids;
	@Column(name = "file_path")
	private String filePath;
	@Column(name = "download_status")
	private Integer downloadStatus;
	@Column(name = "cust_id")
	private String custId;
	@Column(name = "enterprise_name")
	private String enterpriseName;
	@Column(name = "industry_pool_id")
	private Integer industryPoolId;
	@Column(name = "industry_pool_name")
	private String industryPoolName;
	@Column(name = "order_id")
	private String orderId;
	@Column
	private Integer amount;
	@Column
	private Integer quantity;
	@Column
	private String remark;
	@Column(name = "group_source")
	private String groupSource;
	@Column(name = "task_id")
	private String taskId;
	@Column(name = "task_phone_index")
	private Integer taskPhoneIndex;

	@Column(name = "task_type")
	private Integer taskType;
	@Column(name = "user_group_id")
	private String userGroupId;
	@Column(name = "task_end_time")
	private Timestamp taskEndTime;
	@Column(name = "task_create_time")
	private Timestamp taskCreateTime;
	/**
	 * 营销项目ID
	 */
	@Column(name = "market_project_id")
	private Integer marketProjectId;

	@Column(name = "data_source")
	private Integer dataSource;

    @Column(name = "extract_time")
    private Timestamp extractTime;


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

	public String getDesc() {
		return desc;
	}

	public void setDesc(String desc) {
		this.desc = desc;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
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



//	public List<LabelInfo> getLabels() {
//		return labels;
//	}
//
//	public void setLabels(List<LabelInfo> labels) {
//		this.labels = labels;
//	}

	public String getCreateUserId() {
		return createUserId;
	}

	public void setCreateUserId(String createUserId) {
		this.createUserId = createUserId;
	}

	public String getUpdateUserId() {
		return updateUserId;
	}

	public void setUpdateUserId(String updateUserId) {
		this.updateUserId = updateUserId;
	}

	public Integer getAvailably() {
		return availably;
	}

	public void setAvailably(Integer availably) {
		this.availably = availably;
	}

	public String getPurpose() {
		return purpose;
	}

	public void setPurpose(String purpose) {
		this.purpose = purpose;
	}

	public Integer getUpdateCycle() {
		return updateCycle;
	}

	public void setUpdateCycle(Integer updateCycle) {
		this.updateCycle = updateCycle;
	}

	public String getApi() {
		return api;
	}

	public void setApi(String api) {
		this.api = api;
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

	public Long getUserCount() {
		return userCount;
	}

	public void setUserCount(Long userCount) {
		this.userCount = userCount;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Integer getDownloadCount() {
		return downloadCount;
	}

	public void setDownloadCount(Integer downloadCount) {
		this.downloadCount = downloadCount;
	}

//	public List<LabelAudit> getLabelAudits() {
//		return labelAudits;
//	}
//
//	public void setLabelAudits(List<LabelAudit> labelAudits) {
//		this.labelAudits = labelAudits;
//	}

	public String getGroupCondition() {
		return groupCondition;
	}

	public void setGroupCondition(String groupCondition) {
		this.groupCondition = groupCondition;
	}

	public String getGrouping() {
		return grouping;
	}

	public void setGrouping(String grouping) {
		this.grouping = grouping;
	}

	public String getLids() {
		return lids;
	}

	public void setLids(String lids) {
		this.lids = lids;
	}

	public Integer getCycle() {
		return cycle;
	}

	public void setCycle(Integer cycle) {
		this.cycle = cycle;
	}

//	public List<CustomerLabelAndCategory> getCustomerLabelAndCategorys() {
//		return customerLabelAndCategorys;
//	}
//
//	public void setCustomerLabelAndCategorys(
//			List<CustomerLabelAndCategory> customerLabelAndCategorys) {
//		this.customerLabelAndCategorys = customerLabelAndCategorys;
//	}

	public String getFilePath() {
		return filePath;
	}

	public void setFilePath(String filePath) {
		this.filePath = filePath;
	}

	public Integer getDownloadStatus() {
		return downloadStatus;
	}

	public void setDownloadStatus(Integer downloadStatus) {
		this.downloadStatus = downloadStatus;
	}

	public String getRemark() {
		return remark;
	}

	public void setRemark(String remark) {
		this.remark = remark;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getEnterpriseName() {
		return enterpriseName;
	}

	public void setEnterpriseName(String enterpriseName) {
		this.enterpriseName = enterpriseName;
	}

	public Integer getIndustryPoolId() {
		return industryPoolId;
	}

	public void setIndustryPoolId(Integer industryPoolId) {
		this.industryPoolId = industryPoolId;
	}

	public String getIndustryPoolName() {
		return industryPoolName;
	}

	public void setIndustryPoolName(String industryPoolName) {
		this.industryPoolName = industryPoolName;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getGroupSource() {
		return groupSource;
	}

	public void setGroupSource(String groupSource) {
		this.groupSource = groupSource;
	}

	public String getTaskId() {
		return taskId;
	}

	public void setTaskId(String taskId) {
		this.taskId = taskId;
	}

	public Integer getTaskPhoneIndex() {
		return taskPhoneIndex;
	}

	public void setTaskPhoneIndex(Integer taskPhoneIndex) {
		this.taskPhoneIndex = taskPhoneIndex;
	}

	public Integer getTaskType() {
		return taskType;
	}

	public void setTaskType(Integer taskType) {
		this.taskType = taskType;
	}

	public String getUserGroupId() {
		return userGroupId;
	}

	public void setUserGroupId(String userGroupId) {
		this.userGroupId = userGroupId;
	}

	public Timestamp getTaskEndTime() {
		return taskEndTime;
	}

	public void setTaskEndTime(Timestamp taskEndTime) {
		this.taskEndTime = taskEndTime;
	}

	public Timestamp getTaskCreateTime() {
		return taskCreateTime;
	}

	public void setTaskCreateTime(Timestamp taskCreateTime) {
		this.taskCreateTime = taskCreateTime;
	}

	public Integer getMarketProjectId() {
		return marketProjectId;
	}

	public void setMarketProjectId(Integer marketProjectId) {
		this.marketProjectId = marketProjectId;
	}

	public Integer getDataSource() {
		return dataSource;
	}

	public void setDataSource(Integer dataSource) {
		this.dataSource = dataSource;
	}

    public Timestamp getExtractTime() {
        return extractTime;
    }

    public void setExtractTime(Timestamp extractTime) {
        this.extractTime = extractTime;
    }

    @Override
    public String toString() {
        return "CustomGroupDO{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", desc='" + desc + '\'' +
                ", type=" + type +
                ", userCount=" + userCount +
                ", total=" + total +
                ", status=" + status +
                ", createTime=" + createTime +
                ", updateTime=" + updateTime +
                ", createUserId='" + createUserId + '\'' +
                ", updateUserId='" + updateUserId + '\'' +
                ", availably=" + availably +
                ", purpose='" + purpose + '\'' +
                ", updateCycle=" + updateCycle +
                ", groupCondition='" + groupCondition + '\'' +
                ", api='" + api + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", downloadCount=" + downloadCount +
                ", cycle=" + cycle +
                ", grouping='" + grouping + '\'' +
                ", lids='" + lids + '\'' +
                ", filePath='" + filePath + '\'' +
                ", downloadStatus=" + downloadStatus +
                ", custId='" + custId + '\'' +
                ", enterpriseName='" + enterpriseName + '\'' +
                ", industryPoolId=" + industryPoolId +
                ", industryPoolName='" + industryPoolName + '\'' +
                ", orderId='" + orderId + '\'' +
                ", amount=" + amount +
                ", quantity=" + quantity +
                ", remark='" + remark + '\'' +
                ", groupSource='" + groupSource + '\'' +
                ", taskId='" + taskId + '\'' +
                ", taskPhoneIndex=" + taskPhoneIndex +
                ", taskType=" + taskType +
                ", userGroupId='" + userGroupId + '\'' +
                ", taskEndTime=" + taskEndTime +
                ", taskCreateTime=" + taskCreateTime +
                ", marketProjectId=" + marketProjectId +
                ", dataSource=" + dataSource +
                ", extractTime=" + extractTime +
                '}';
    }
}
