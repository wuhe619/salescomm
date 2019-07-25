package com.bdaim.order.entity;

import org.hibernate.annotations.DynamicUpdate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Entity
@Table(name = "t_order")
@DynamicUpdate(true)
public class OrderDO {
	@Id
	@Column(name = "order_id")
	private String orderId;
	
	@Column(name = "cust_id")
	private String custId;
	
	@Column(name = "supplier_id")
	private String supplierId;
	
	@Column(name = "supplier_name")
	private String supplierName;
	
	@Column(name = "enpterprise_name")
	private String enterpriseName;
	
	@Column(name = "order_type")
	private Integer orderType;
	
	@Column(name = "old_order_code")
	private String oldOrderCode;
	
	@Column(name = "cancel_reason")
	private String cancleReason;
	
	@Column(name = "order_state")
	private Integer orderState;
	
	@Column(name = "pay_type")
	private Integer payType;
	
	@Column(name = "create_time")
	private Date createTime;
	
	@Column(name = "effect_time")
	private Date effectTime;
	
	@Column(name = "expire_time")
	private Date expireTime;
	
	@Column
	private String remarks;
	
	@Column
	private Integer amount;
	
	@Column(name = "product_name")
	private String productName;
	
	@Column
	private Integer quantity;
	
	@Column(name = "cost_price")
	private Integer costPrice;
	
	@Column(name = "pay_time")
	private Date payTime;
	
	@Column(name = "pay_amount")
	private Integer payAmount;

	public String getOrderId() {
		return orderId;
	}

	public void setOrderId(String orderId) {
		this.orderId = orderId;
	}

	public String getCustId() {
		return custId;
	}

	public void setCustId(String custId) {
		this.custId = custId;
	}

	public String getSupplierId() {
		return supplierId;
	}

	public void setSupplierId(String supplierId) {
		this.supplierId = supplierId;
	}

	public String getSupplierName() {
		return supplierName;
	}

	public void setSupplierName(String supplierName) {
		this.supplierName = supplierName;
	}

	public String getEnterpriseName() {
		return enterpriseName;
	}

	public void setEnterpriseName(String enterpriseName) {
		this.enterpriseName = enterpriseName;
	}

	public Integer getOrderType() {
		return orderType;
	}

	public void setOrderType(Integer orderType) {
		this.orderType = orderType;
	}

	public String getOldOrderCode() {
		return oldOrderCode;
	}

	public void setOldOrderCode(String oldOrderCode) {
		this.oldOrderCode = oldOrderCode;
	}

	public String getCancleReason() {
		return cancleReason;
	}

	public void setCancleReason(String cancleReason) {
		this.cancleReason = cancleReason;
	}

	public Integer getOrderState() {
		return orderState;
	}


	public void setOrderState(Integer orderState) {
		this.orderState = orderState;
	}

	public Integer getPayType() {
		return payType;
	}

	public void setPayType(Integer payType) {
		this.payType = payType;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getEffectTime() {
		return effectTime;
	}

	public void setEffectTime(Date effectTime) {
		this.effectTime = effectTime;
	}

	public Date getExpireTime() {
		return expireTime;
	}

	public void setExpireTime(Date expireTime) {
		this.expireTime = expireTime;
	}

	public String getRemarks() {
		return remarks;
	}

	public void setRemarks(String remarks) {
		this.remarks = remarks;
	}

	public Integer getAmount() {
		return amount;
	}

	public void setAmount(Integer amount) {
		this.amount = amount;
	}

	public String getProductName() {
		return productName;
	}

	public void setProductName(String productName) {
		this.productName = productName;
	}



	public Integer getQuantity() {
		return quantity;
	}

	public void setQuantity(Integer quantity) {
		this.quantity = quantity;
	}

	public Integer getCostPrice() {
		return costPrice;
	}

	public void setCostPrice(Integer costPrice) {
		this.costPrice = costPrice;
	}

	public Date getPayTime() {
		return payTime;
	}

	public void setPayTime(Date payTime) {
		this.payTime = payTime;
	}

	public Integer getPayAmount() {
		return payAmount;
	}

	public void setPayAmount(Integer payAmount) {
		this.payAmount = payAmount;
	}

}
