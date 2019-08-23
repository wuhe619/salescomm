package com.bdaim.label.entity;

import com.bdaim.customgroup.entity.CustomGroupDO;
import com.bdaim.rbac.entity.User;
import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

@Entity
@Table(name = "label_info")
@DynamicUpdate(true)
public class LabelInfo {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	private Integer id;
	@Column(name = "label_name")
	private String labelName;
	@Column(name = "label_id")
	private String labelId;
	@Column(name = "attr_id")
	private String attrId;
	@Column
	private String path;// 标签全路径
	@Column
	private String uri;// 标签id全路径
	@Column(name = "\"LEVEL\"")
	private Integer level;
	@Column(name = "label_content")
	private String labelContent; // 组合标签的组合条件
	@Column(name = "update_cycle")
	private Integer updateCycle; // 更新周期
	@Transient
 	private String updateCycleCn;
	@Column(name = "data_format")
	private Integer dataFormat; // 数据格式
	@Transient
	private String dataFormatCn;
	@Column(name = "label_source")
	private Integer labelSource; // 标签来源
	@Transient
	private String labelSourceCn;
	@Column(name = "\"PRIOR\"")
	private Integer prior; // 是否优先
	@Column(name = "business_mean")
	private String businessMean; // 业务含义
	@Column(name = "label_rule")
	private String labelRule; // 标签规则
	@Column(name = "method_type")
	private Integer methodType; // 产出方法类型
	@Transient
	private String methodTypeCn;
	@Column(name = "method_content")
	private String methodContent; // 产出方法内容
	@Column(name = "index_status")
	private Integer indexStatus; // 索引状态
	@Column
	private Integer mutex; // 是否互斥 1：是；0：否
	@Column
	private Integer dimensions; // 维度和是否为1
	@Column
	private Integer status; // 标签状态
	@Transient
	private String statusCn;
	@Column
	private Integer config;
	@Column
	// 1:分类 、2:基础标签 3:组合标签
	private Integer type;
	@Transient
	private String typeCn;
	
	@Column
	private Integer seq; //排序

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "create_uid")
	private User labelCreateUser;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "update_uid")
	private User labelUpdateUser;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "offline_uid")
	private User labelOfflineUser;

	@Column(name = "update_time")
	private Date updateTime;
	@Column(name = "create_time")
	private Date createTime;
	@Column(name = "leaf_rule")
	private String leafRule; // 创建叶子标签规则
	@Column
	private Integer availably;// 0：无效；1：有效
	@Column(name = "order_num")
	private Integer orderNum;
	
	@Column(name = "parent_id")
	private Integer parentId;
	
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "parent_id", insertable=false, updatable=false)
	private LabelInfo parent;
	
	@OneToMany(mappedBy = "parent", fetch = FetchType.LAZY)
	private List<LabelInfo> children;
	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "category_id")
	private LabelCategory parentCategory;

//	@OneToMany(mappedBy = "labelInfo", fetch = FetchType.LAZY)
//	private List<LabelAudit> labelAudits;
	
	@ManyToMany
	@JoinTable(name = "label_rel", joinColumns = { @JoinColumn(name = "sid", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "lid") })
	private List<LabelInfo> signatures; // 组成组合标签的基础标签集合
	//
	
	@ManyToMany
	@JoinTable(name = "label_rel", joinColumns = { @JoinColumn(name = "lid", referencedColumnName = "id") }, inverseJoinColumns = { @JoinColumn(name = "sid") })
	private List<LabelInfo> labels; // 基础标签的组合次数

//	 @ManyToMany(fetch=FetchType.EAGER,mappedBy =
//	 "labels",cascade={CascadeType.MERGE,CascadeType.PERSIST})
	
//	@OneToMany(mappedBy = "labelInfo")
//	private List<CustomerLabelAndCategory> customerLabelAndCategorys;
	
	@Transient
	private List<CustomGroupDO> groups;

	@Transient
	private List<LabelCategory> cateList;
	
	@Transient
	private Integer searchCount;
	
	@Column(name = "customer_num")
	private Long customerNum; // 标签覆盖用户数
	
	@Column
	private Long total; // 总用户数
	
	@Transient
	private Integer signatureCount; // 标签组合次数
	@Transient
	private Integer viewStatus; // 标签显示状态
	@Transient
	private String ids; // 创建组合标签的基础标签id集合
	@Transient
	private Long createUid;
	@Column(name = "offline_time")
	private Date offlineTime;

	@Transient
	private Integer levelCount;

	@Transient
	private Long customerNum2;


	public Integer getLevel() {
		return level;
	}

	public void setLevel(Integer level) {
		this.level = level;
	}

	public Integer getPrior() {
		return prior;
	}

	public void setPrior(Integer prior) {
		this.prior = prior;
	}

	public Integer getOrderNum() {
		return orderNum;
	}

	public void setOrderNum(Integer orderNum) {
		this.orderNum = orderNum;
	}

	public LabelInfo() {
	}

	public LabelInfo(Integer id) {
		this.id = id;
	}

	public Integer getAvailably() {
		return availably;
	}

	public void setAvailably(Integer availably) {
		this.availably = availably;
	}

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public String getLabelContent() {
		return labelContent;
	}

	public void setLabelContent(String labelContent) {
		this.labelContent = labelContent;
	}

	public Integer getUpdateCycle() {
		return updateCycle;
	}

	public void setUpdateCycle(Integer updateCycle) {
		this.updateCycle = updateCycle;
	}

	public Integer getDataFormat() {
		return dataFormat;
	}

	public void setDataFormat(Integer dataFormat) {
		this.dataFormat = dataFormat;
	}

	public Integer getLabelSource() {
		return labelSource;
	}

	public void setLabelSource(Integer labelSource) {
		this.labelSource = labelSource;
	}

	public String getBusinessMean() {
		return businessMean;
	}

	public void setBusinessMean(String businessMean) {
		this.businessMean = businessMean;
	}

	public String getLabelRule() {
		return labelRule;
	}

	public void setLabelRule(String labelRule) {
		this.labelRule = labelRule;
	}

	public Integer getMethodType() {
		return methodType;
	}

	public void setMethodType(Integer methodType) {
		this.methodType = methodType;
	}

	public String getMethodContent() {
		return methodContent;
	}

	public void setMethodContent(String methodContent) {
		this.methodContent = methodContent;
	}

	public Integer getMutex() {
		return mutex;
	}

	public void setMutex(Integer mutex) {
		this.mutex = mutex;
	}

	public Integer getDimensions() {
		return dimensions;
	}

	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getConfig() {
		return config;
	}

	public void setConfig(Integer config) {
		this.config = config;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = (Date)updateTime.clone();
	}

	public Integer getId() {
		return id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	public LabelInfo getParent() {
		return parent;
	}

	public void setParent(LabelInfo parent) {
		this.parent = parent;
	}

	public List<LabelInfo> getChildren() {
		return children;
	}

	public void setChildren(List<LabelInfo> children) {
		this.children = children;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = (Date)createTime.clone();
	}

	public String getLabelId() {
		return labelId;
	}

	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public Long getCustomerNum() {
		return customerNum;
	}

	public void setCustomerNum(Long customerNum) {
		this.customerNum = customerNum;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}

	public Integer getSignatureCount() {
		return signatureCount;
	}

	public void setSignatureCount(Integer signatureCount) {
		this.signatureCount = signatureCount;
	}

	public Integer getIndexStatus() {
		return indexStatus;
	}

	public void setIndexStatus(Integer indexStatus) {
		this.indexStatus = indexStatus;
	}

	public String getUri() {
		return uri;
	}

	public void setUri(String uri) {
		this.uri = uri;
	}

	public LabelCategory getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(LabelCategory parentCategory) {
		this.parentCategory = parentCategory;
	}

	public Integer getType() {
		return type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	public Integer getViewStatus() {
		return viewStatus;
	}

	public void setViewStatus(Integer viewStatus) {
		this.viewStatus = viewStatus;
	}

	public String getIds() {
		return ids;
	}

	public void setIds(String ids) {
		this.ids = ids;
	}

	public List<CustomGroupDO> getGroups() {
		return groups;
	}

	public void setGroups(List<CustomGroupDO> groups) {
		this.groups = groups;
	}

	public List<LabelInfo> getSignatures() {
		return signatures;
	}

	public void setSignatures(List<LabelInfo> signatures) {
		this.signatures = signatures;
	}

	public List<LabelInfo> getLabels() {
		return labels;
	}

	public void setLabels(List<LabelInfo> labels) {
		this.labels = labels;
	}

	public Date getOfflineTime() {
		return offlineTime;
	}

	public void setOfflineTime(Date offlineTime) {
		this.offlineTime = (Date)offlineTime.clone();
	}

	public String getLeafRule() {
		return leafRule;
	}

	public void setLeafRule(String leafRule) {
		this.leafRule = leafRule;
	}

	public String getUpdateCycleCn() {
		return updateCycleCn;
	}

	public void setUpdateCycleCn(String updateCycleCn) {
		this.updateCycleCn = updateCycleCn;
	}

	public String getDataFormatCn() {
		return dataFormatCn;
	}

	public void setDataFormatCn(String dataFormatCn) {
		this.dataFormatCn = dataFormatCn;
	}

	public String getLabelSourceCn() {
		return labelSourceCn;
	}

	public void setLabelSourceCn(String labelSourceCn) {
		this.labelSourceCn = labelSourceCn;
	}

	public String getMethodTypeCn() {
		return methodTypeCn;
	}

	public void setMethodTypeCn(String methodTypeCn) {
		this.methodTypeCn = methodTypeCn;
	}

	public String getStatusCn() {
		return statusCn;
	}

	public void setStatusCn(String statusCn) {
		this.statusCn = statusCn;
	}

	public String getTypeCn() {
		return typeCn;
	}

	public void setTypeCn(String typeCn) {
		this.typeCn = typeCn;
	}

	public User getLabelCreateUser() {
		return labelCreateUser;
	}

	public void setLabelCreateUser(User labelCreateUser) {
		this.labelCreateUser = labelCreateUser;
	}

	public User getLabelUpdateUser() {
		return labelUpdateUser;
	}

	public void setLabelUpdateUser(User labelUpdateUser) {
		this.labelUpdateUser = labelUpdateUser;
	}

	public User getLabelOfflineUser() {
		return labelOfflineUser;
	}

	public void setLabelOfflineUser(User labelOfflineUser) {
		this.labelOfflineUser = labelOfflineUser;
	}

	public Long getCreateUid() {
		return createUid;
	}

	public void setCreateUid(Long createUid) {
		this.createUid = createUid;
	}

//	public List<LabelAudit> getLabelAudits() {
//		return labelAudits;
//	}
//
//	public void setLabelAudits(List<LabelAudit> labelAudits) {
//		this.labelAudits = labelAudits;
//	}

	public List<LabelCategory> getCateList() {
		return cateList;
	}

	public void setCateList(List<LabelCategory> cateList) {
		this.cateList = cateList;
	}

	public Integer getSearchCount() {
		return searchCount;
	}

	public void setSearchCount(Integer searchCount) {
		this.searchCount = searchCount;
	}

	public Integer getLevelCount() {
		return levelCount;
	}

	public void setLevelCount(Integer levelCount) {
		this.levelCount = levelCount;
	}

	public Long getCustomerNum2() {
		return customerNum2;
	}

	public void setCustomerNum2(Long customerNum2) {
		this.customerNum2 = customerNum2;
	}

	public String getAttrId() {
		return attrId;
	}

	public void setAttrId(String attrId) {
		this.attrId = attrId;
	}

//	public List<CustomerLabelAndCategory> getCustomerLabelAndCategorys() {
//		return customerLabelAndCategorys;
//	}

//	public void setCustomerLabelAndCategorys(
//			List<CustomerLabelAndCategory> customerLabelAndCategorys) {
//		this.customerLabelAndCategorys = customerLabelAndCategorys;
//	}

	public Integer getSeq() {
		return seq;
	}

	public void setSeq(Integer seq) {
		this.seq = seq;
	}

	public Integer getParentId() {
		return parentId;
	}

	public void setParentId(Integer parentId) {
		this.parentId = parentId;
	}

	
}
