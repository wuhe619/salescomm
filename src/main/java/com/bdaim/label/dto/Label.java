package com.bdaim.label.dto;


import com.bdaim.label.entity.LabelInfo;

import java.util.ArrayList;
import java.util.List;

public class Label {

	private Integer id=0;  //1
	private String labelId=null;//2
	private String labelName=null;//3
	private String businessMean=null;//4
	private Integer mutex=0;//5
	private String typeCn=null;//6
	private Integer type=0;//7
	private Integer viewStatus=1;//8 前端是否展开
	private String path=null;//9
	private String uri=null; //10
	private Integer signatureCounts=0; //11
	
	private Integer methodType=0;//12
	private String methodTypeCn=null;
	
	private Integer updateCycle=0;//13
	private String labelRule=null;//14
	private String updateCycleCn=null;//15
	private Integer prior=0;//16
	
	private String statusCn=null;//17
	private Integer level=4;//18
	
	private String createTime=null;//19
	private String createUser=null;//20
	private String updateUser=null;//21
	private String updateTime=null;//22
	
	private String methodContent=null;//23
	private Integer leafCounts=0;//24
	
	private String config=null;//25
	private Integer status=0;//26
	private Integer dimensions=0;//27
	
	private Long total=0L;//28
	private Long customerNum=0L;//29
	
	public Label() {
		
	}
	public Label(Integer id, String labelId, String lableName, String businessMean, Integer mutex, Integer type, String typeCn, Integer viewStatus, String path, String uri
			, Integer signatureCounts, Integer methodType, String methodContent, Integer updateCycle, String updatecycleCn, String labelRule, Integer prior, Integer status, String statusCn
			, Integer level, String createTime, String createUser, String updateTime, String updateUser, Integer leafCounts, String config
			, Integer dimensions, Long total, Long customerNum) {
		this.id=id;
		this.labelId=labelId;
		this.labelName=labelName;
		this.businessMean=businessMean;
		this.mutex=mutex;
		this.typeCn=typeCn;
		this.type=type;
		this.viewStatus=viewStatus;
		this.path=path;
		this.uri=uri;
		this.signatureCounts=signatureCounts;
		this.methodType=methodType;
		this.methodContent=methodContent;
		this.updateCycle=updateCycle;
		this.labelRule=labelRule;
		this.updateCycleCn=updateCycleCn;
		this.prior=prior;
		this.status=status;
		this.statusCn=statusCn;
		this.dimensions=dimensions;
		this.total=total;
		this.customerNum=customerNum;
	}
	public Label(LabelInfo li) {
		this.id=li.getId();
//		this.labelId=li.getLabelId();
		if(li.getLevel()!=null && li.getLevel()==3)
			this.labelId = String.valueOf(li.getId());
		else if(li.getLevel()!=null && li.getLevel()==4)
			this.labelId = String.valueOf(li.getId());
		else
			this.labelId = String.valueOf(li.getId());
		
		this.labelName=li.getLabelName();
		this.businessMean=li.getBusinessMean();
		this.mutex=li.getMutex();
		this.type=li.getType();
		if(li.getType()==null || li.getType()==2) {
			this.typeCn="基础标签";
		}else if(li.getType()==3){
			this.typeCn="组合标签";
		}else {
			this.typeCn="";
		}
		
		this.path=li.getPath();
		this.uri=li.getUri();
		this.signatureCounts=li.getSignatureCount();
		this.methodType=li.getMethodType();
		if(li.getLevel()!=null && li.getLevel()==3)
			if(li.getMethodType()==null || li.getMethodType()==0)
				this.methodTypeCn="统计";
			else
				this.methodTypeCn="预测";
		else
			this.methodTypeCn="";
		this.methodContent=li.getMethodContent();
		this.labelRule=li.getLabelRule();
		
		this.updateCycle=li.getUpdateCycle();
		if(li.getUpdateCycle()==null || li.getUpdateCycle()==2)
			this.updateCycleCn = "月";
		else
			this.updateCycleCn = "日";
		
		this.prior=li.getPrior();
		this.status=li.getStatus();
		if(li.getStatus()==3)
			this.statusCn="已上线";
		else
			this.statusCn=String.valueOf(li.getStatus());
		
		this.dimensions=li.getDimensions();
		this.total=li.getTotal();
		this.customerNum=li.getCustomerNum();
	}
	
	
	private List<Label> children=new ArrayList();


	public void addChild(Label label) {
		this.children.add(label);
		this.leafCounts++;
	}
	
	public String getBusinessMean() {
		return businessMean;
	}


	public void setBusinessMean(String businessMean) {
		this.businessMean = businessMean;
	}


	public Integer getMutex() {
		return mutex;
	}


	public void setMutex(Integer mutex) {
		this.mutex = mutex;
	}


	public String getTypeCn() {
		return typeCn;
	}


	public void setTypeCn(String typeCn) {
		this.typeCn = typeCn;
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


	public String getPath() {
		return path;
	}


	public void setPath(String path) {
		this.path = path;
	}


	public Integer getSignatureCounts() {
		return signatureCounts;
	}


	public void setSignatureCounts(Integer signatureCounts) {
		this.signatureCounts = signatureCounts;
	}


	public Long getTotal() {
		return total;
	}


	public void setTotal(Long total) {
		this.total = total;
	}


	public Long getCustomerNum() {
		return customerNum;
	}


	public void setCustomerNum(Long customerNum) {
		this.customerNum = customerNum;
	}


	public Integer getMethodType() {
		return methodType;
	}


	public void setMethodType(Integer methodType) {
		this.methodType = methodType;
	}


	public String getLabelId() {
		return labelId;
	}


	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}


	public Integer getUpdateCycle() {
		return updateCycle;
	}


	public void setUpdateCycle(Integer updateCycle) {
		this.updateCycle = updateCycle;
	}


	public String getLabelRule() {
		return labelRule;
	}


	public void setLabelRule(String labelRule) {
		this.labelRule = labelRule;
	}


	public String getUpdateCycleCn() {
		return updateCycleCn;
	}


	public void setUpdateCycleCn(String updateCycleCn) {
		this.updateCycleCn = updateCycleCn;
	}


	public Integer getPrior() {
		return prior;
	}


	public void setPrior(Integer prior) {
		this.prior = prior;
	}


	public Integer getId() {
		return id;
	}


	public void setId(Integer id) {
		this.id = id;
	}


	public String getLabelName() {
		return labelName;
	}


	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}


	public String getStatusCn() {
		return statusCn;
	}


	public void setStatusCn(String statusCn) {
		this.statusCn = statusCn;
	}


	public Integer getLevel() {
		return level;
	}


	public void setLevel(Integer level) {
		this.level = level;
	}


	public String getUpdateUser() {
		return updateUser;
	}


	public void setUpdateUser(String updateUser) {
		this.updateUser = updateUser;
	}


	public String getUpdateTime() {
		return updateTime;
	}


	public void setUpdateTime(String updateTime) {
		this.updateTime = updateTime;
	}


	public String getUri() {
		return uri;
	}


	public void setUri(String uri) {
		this.uri = uri;
	}


	public String getMethodContent() {
		return methodContent;
	}


	public void setMethodContent(String methodContent) {
		this.methodContent = methodContent;
	}


	public Integer getLeafCounts() {
		return leafCounts;
	}


	public void setLeafCounts(Integer leafCounts) {
		this.leafCounts = leafCounts;
	}


	public String getCreateTime() {
		return createTime;
	}


	public void setCreateTime(String createTime) {
		this.createTime = createTime;
	}


	public String getCreateUser() {
		return createUser;
	}


	public void setCreateUser(String createUser) {
		this.createUser = createUser;
	}


	public String getConfig() {
		return config;
	}


	public void setConfig(String config) {
		this.config = config;
	}


	public Integer getStatus() {
		return status;
	}


	public void setStatus(Integer status) {
		this.status = status;
	}


	public Integer getDimensions() {
		return dimensions;
	}


	public void setDimensions(Integer dimensions) {
		this.dimensions = dimensions;
	}


	public List<Label> getChildren() {
		return children;
	}
	public String getMethodTypeCn() {
		return methodTypeCn;
	}
	public void setMethodTypeCn(String methodTypeCn) {
		this.methodTypeCn = methodTypeCn;
	}
	
}
