package com.bdaim.label.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 *  行业标签池的标签
 */
@Entity
@Table(name = "t_industry_label", schema = "", catalog = "")
public class IndustryPoolLabel {
	//企业ID
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "industry_label_id")
    private Integer industryPoolLabelId;
    //企业名称
	@Column(name = "industry_pool_id")
	private Integer industryPoolId;
	@Column(name = "label_id")
    private String labelId;
	@Column(name = "status")
    private Integer status;
	@Column(name = "modify_time")
    private Timestamp modifyTime;
	@Column(name = "create_time")
    private Timestamp createTime;
	
	@Column(name = "price")
    private Integer price;
	
	

	public Integer getIndustryPoolLabelId() {
		return industryPoolLabelId;
	}
	public void setIndustryPoolLabelId(Integer industryPoolLabelId) {
		this.industryPoolLabelId = industryPoolLabelId;
	}
	public Integer getIndustryPoolId() {
		return industryPoolId;
	}
	public void setIndustryPoolId(Integer industryPoolId) {
		this.industryPoolId = industryPoolId;
	}
	public String getLabelId() {
		return labelId;
	}
	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Timestamp getModifyTime() {
		return modifyTime;
	}
	public void setModifyTime(Timestamp modifyTime) {
		this.modifyTime = modifyTime;
	}
	public Timestamp getCreateTime() {
		return createTime;
	}
	public void setCreateTime(Timestamp createTime) {
		this.createTime = createTime;
	}
	public Integer getPrice() {
		return price;
	}
	public void setPrice(Integer price) {
		this.price = price;
	}
    
    

    
}
