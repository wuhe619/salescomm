package com.bdaim.supplier.entity;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "t_supplier_settlement")
@DynamicUpdate(true)
public class SupplierSettlementDO {
	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@Column(name = "settlement_id")
	private Integer settlementId;
	
	@Column(name = "order_id")
	private String orderId;
	
	@Column(name = "source_id")
	private Integer sourceId;
	
	@Column(name = "source_name")
	private String sourceName;
	
	@Column(name = "cost_price")
	private Integer costPrice;
	
	@Column(name = "sale_price")
	private Integer salePrice;
	
	@Column
	private Integer quantity;
	
	@Column(name = "label_id")
	private String labelId;
	
	@Column(name = "label_name")
	private String labelName;
	
	@Column(name = "create_time")
	private Date createTime;

	public Integer getSettlementId() {
		return settlementId;
	}

	public void setSettlementId(Integer settlementId) {
		this.settlementId = settlementId;
	}

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public Integer getSourceId() {
		return sourceId;
	}

	public void setSourceId(Integer sourceId) {
		this.sourceId = sourceId;
	}

	public String getSourceName() {
		return sourceName;
	}

	public void setSourceName(String sourceName) {
		this.sourceName = sourceName;
	}

	public Integer getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(Integer costPrice) {
		this.costPrice = costPrice;
	}

	public Integer getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(Integer salePrice) {
		this.salePrice = salePrice;
	}

	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public String getLabelId() {
		return labelId;
	}

	public void setLabelId(String labelId) {
		this.labelId = labelId;
	}

	public String getLabelName() {
		return labelName;
	}

	public void setLabelName(String labelName) {
		this.labelName = labelName;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}
	
}
