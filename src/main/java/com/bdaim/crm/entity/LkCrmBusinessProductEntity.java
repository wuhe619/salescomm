package com.bdaim.crm.entity;

import javax.persistence.*;
import java.math.BigDecimal;
import java.util.Objects;

@Entity
@Table(name = "lkcrm_crm_business_product", schema = "", catalog = "")
public class LkCrmBusinessProductEntity {
    private int rId;
    private int businessId;
    private int productId;
    private BigDecimal price;
    private BigDecimal salesPrice;
    private int num;
    private int discount;
    private BigDecimal subtotal;
    private String unit;

    @Id
    @Column(name = "r_id")
    @GeneratedValue
    public int getrId() {
        return rId;
    }

    public void setrId(int rId) {
        this.rId = rId;
    }

    @Basic
    @Column(name = "business_id")
    public int getBusinessId() {
        return businessId;
    }

    public void setBusinessId(int businessId) {
        this.businessId = businessId;
    }

    @Basic
    @Column(name = "product_id")
    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    @Basic
    @Column(name = "price")
    public BigDecimal getPrice() {
        return price;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    @Basic
    @Column(name = "sales_price")
    public BigDecimal getSalesPrice() {
        return salesPrice;
    }

    public void setSalesPrice(BigDecimal salesPrice) {
        this.salesPrice = salesPrice;
    }

    @Basic
    @Column(name = "num")
    public int getNum() {
        return num;
    }

    public void setNum(int num) {
        this.num = num;
    }

    @Basic
    @Column(name = "discount")
    public int getDiscount() {
        return discount;
    }

    public void setDiscount(int discount) {
        this.discount = discount;
    }

    @Basic
    @Column(name = "subtotal")
    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public void setSubtotal(BigDecimal subtotal) {
        this.subtotal = subtotal;
    }

    @Basic
    @Column(name = "unit")
    public String getUnit() {
        return unit;
    }

    public void setUnit(String unit) {
        this.unit = unit;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LkCrmBusinessProductEntity that = (LkCrmBusinessProductEntity) o;
        return rId == that.rId &&
                businessId == that.businessId &&
                productId == that.productId &&
                num == that.num &&
                discount == that.discount &&
                Objects.equals(price, that.price) &&
                Objects.equals(salesPrice, that.salesPrice) &&
                Objects.equals(subtotal, that.subtotal) &&
                Objects.equals(unit, that.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(rId, businessId, productId, price, salesPrice, num, discount, subtotal, unit);
    }
}
