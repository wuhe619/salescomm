package com.bdaim.label.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 *  行业
 */
/*@Entity
@Table(name = "t_industry_info", schema = "", catalog = "")*/
public class Industry {
	//企业ID
    private Integer industryInfoId;
    //企业名称
    private String industryName;
    private Integer price;
    private Integer status;
    private Timestamp modifyTime;
    private Timestamp createTime;
    private String description;

    @Id
    @Column(name = "industry_info_id")
    public Integer getIndustryInfoId() {
		return industryInfoId;
	}

	public void setIndustryInfoId(Integer industryInfoId) {
		this.industryInfoId = industryInfoId;
	}

	@Basic
    @Column(name = "industy_name")
	public String getIndustryName() {
		return industryName;
	}

	public void setIndustryName(String industryName) {
		this.industryName = industryName;
	}

	@Basic
    @Column(name = "price")
	public Integer getPrice() {
		return price;
	}

	public void setPrice(Integer price) {
		this.price = price;
	}

	@Basic
    @Column(name = "description")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Basic
    @Column(name = "status")
	public void setStatus(Integer status) {
		this.status = status;
	}

    @Basic
    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "modify_time")
    public Timestamp getModifyTime() {
        return modifyTime;
    }

    public void setModifyTime(Timestamp modifyTime) {
        this.modifyTime = modifyTime;
    }

}
