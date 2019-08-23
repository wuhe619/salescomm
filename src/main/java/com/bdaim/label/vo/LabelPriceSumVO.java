package com.bdaim.label.vo;

import java.math.BigDecimal;

public class LabelPriceSumVO {
	private BigDecimal salePrice;

	private BigDecimal sourcePrice;

	public BigDecimal getSalePrice() {
		return salePrice;
	}

	public void setSalePrice(BigDecimal salePrice) {
		this.salePrice = salePrice;
	}

	public BigDecimal getSourcePrice() {
		return sourcePrice;
	}

	public void setSourcePrice(BigDecimal sourcePrice) {
		this.sourcePrice = sourcePrice;
	}

}
