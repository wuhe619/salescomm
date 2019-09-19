package com.bdaim.customs.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 低价商品单导出excel实体类
 * @author chengning@salescomm.net
 * @date 2019/9/19
 * @description
 */
public class LowPriceProductForm {

    @ExcelProperty(value = "分单号", index = 0)
    private String fdId;

    @ExcelProperty(value = "商品编码", index = 1)
    private String productNo;

    @ExcelProperty(value = "物品名称", index = 2)
    private String productName;

    @ExcelProperty(value = "申报单价", index = 3)
    private String declaredPrice;

    @ExcelProperty(value = "数量", index = 4)
    private String quantity;

    @ExcelProperty(value = "商品重量", index = 5)
    private String weight;

    @ExcelProperty(value = "商品限价", index = 6)
    private String priceFixing;

    public String getFdId() {
        return fdId;
    }

    public void setFdId(String fdId) {
        this.fdId = fdId;
    }

    public String getProductNo() {
        return productNo;
    }

    public void setProductNo(String productNo) {
        this.productNo = productNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getDeclaredPrice() {
        return declaredPrice;
    }

    public void setDeclaredPrice(String declaredPrice) {
        this.declaredPrice = declaredPrice;
    }

    public String getQuantity() {
        return quantity;
    }

    public void setQuantity(String quantity) {
        this.quantity = quantity;
    }

    public String getWeight() {
        return weight;
    }

    public void setWeight(String weight) {
        this.weight = weight;
    }

    public String getPriceFixing() {
        return priceFixing;
    }

    public void setPriceFixing(String priceFixing) {
        this.priceFixing = priceFixing;
    }
}
