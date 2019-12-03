package com.bdaim.customgroup.dto;

import java.util.List;

public class CustomerGroupAddDTO {

    private String groupCondition;
    private String name;
    private String purpose;
    private Integer total;
    private Integer quantity;
    private Integer industryPoolId;
    private String industryPoolName;
    private List<QuantityDetailDTO> quantityDetail;
    private String custId;
    private String createUserId;
    private String updateUserId;
    private String enterpriseName;
    /**
     * 项目ID
     */
    private String marketProjectId;
    /**
     * 触达方式
     */
    private String touchMode;

    /**
     * 联通营销平台活动名称
     */
    private String unicomActivityName;

    private Integer dataSource;


    public String getEnterpriseName() {
        return enterpriseName;
    }

    public void setEnterpriseName(String enterpriseName) {
        this.enterpriseName = enterpriseName;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }


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

    public String getGroupCondition() {
        return groupCondition;
    }

    public void setGroupCondition(String groupCondition) {
        this.groupCondition = groupCondition;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPurpose() {
        return purpose;
    }

    public void setPurpose(String purpose) {
        this.purpose = purpose;
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

    public List<QuantityDetailDTO> getQuantityDetail() {
        return quantityDetail;
    }

    public void setQuantityDetail(List<QuantityDetailDTO> quantityDetail) {
        this.quantityDetail = quantityDetail;
    }

    public Integer getTotal() {
        return total;
    }

    public void setTotal(Integer total) {
        this.total = total;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public String getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(String marketProjectId) {
        this.marketProjectId = marketProjectId;
    }

    public String getTouchMode() {
        return touchMode;
    }

    public void setTouchMode(String touchMode) {
        this.touchMode = touchMode;
    }

    public String getUnicomActivityName() {
        return unicomActivityName;
    }

    public void setUnicomActivityName(String unicomActivityName) {
        this.unicomActivityName = unicomActivityName;
    }

	public Integer getDataSource() {
		return dataSource;
	}

	public void setDataSource(Integer dataSource) {
		this.dataSource = dataSource;
	}
}
