package com.bdaim.account.dto;

import java.math.BigDecimal;

public class RemainSourceDTO {
	private Long count;
	
	private Long total;
	
	private Long remain;
	
	private Integer sourceId;
	
	private String sourceName;
	
	private BigDecimal salePrice;
	/**
	 * 标签池-客户资源配置
	 */
	private String dataCustConfig;
	
	public BigDecimal getSalePrice() {
		return salePrice;
	}
	public void setSalePrice(BigDecimal salePrice) {
		this.salePrice = salePrice;
	}
	public Long getCount() {
		return count;
	}
	public void setCount(Long count) {
		this.count = count;
	}
	public Long getTotal() {
		return total;
	}
	public void setTotal(Long total) {
		this.total = total;
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
	public Long getRemain() {
		return remain;
	}
	public void setRemain(Long remain) {
		this.remain = remain;
	}

	public String getDataCustConfig() {
		return dataCustConfig;
	}

	public void setDataCustConfig(String dataCustConfig) {
		this.dataCustConfig = dataCustConfig;
	}
}
