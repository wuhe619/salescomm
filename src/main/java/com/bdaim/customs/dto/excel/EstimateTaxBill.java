package com.bdaim.customs.dto.excel;

import com.alibaba.excel.annotation.ExcelProperty;

/**
 * 预估税单导出excel实体类
 * @author chengning@salescomm.net
 * @date 2019/9/19
 * @description
 */
public class EstimateTaxBill {
    @ExcelProperty(value = "序号", index = 0)
    private int no;
    @ExcelProperty(value = "总运单号", index = 1)
    private String zNo;
    @ExcelProperty(value = "分运单号", index = 2)
    private String fNo;
    @ExcelProperty(value = "物品名称", index = 3)
    private String productName;
    @ExcelProperty(value = "预判应税", index = 4)
    private String taxable;
    @ExcelProperty(value = "预估税金", index = 5)
    private String taxes;
    @ExcelProperty(value = "件数", index = 6)
    private String quantity;
    @ExcelProperty(value = "重量", index = 7)
    private String weight;
    @ExcelProperty(value = "净重", index = 8)
    private String netWeight;
    @ExcelProperty(value = "保价", index = 9)
    private String insuredPrice;
    /**
     * 收件人
     */
    @ExcelProperty(value = "收件人姓名", index = 10)
    private String receive_name;

    @ExcelProperty(value = "收件人身份证号", index = 11)
    private String idNo;

    @ExcelProperty(value = "城市", index = 12)
    private String receive_city;

    @ExcelProperty(value = "收件人地址", index = 13)
    private String receive_address;

    @ExcelProperty(value = "收件人电话", index = 14)
    private String receive_tel;

    @ExcelProperty(value = "邮编", index = 15)
    private String zipCode;

    public int getNo() {
        return no;
    }

    public void setNo(int no) {
        this.no = no;
    }

    public String getzNo() {
        return zNo;
    }

    public void setzNo(String zNo) {
        this.zNo = zNo;
    }

    public String getfNo() {
        return fNo;
    }

    public void setfNo(String fNo) {
        this.fNo = fNo;
    }

    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public String getTaxable() {
        return taxable;
    }

    public void setTaxable(String taxable) {
        this.taxable = taxable;
    }

    public String getTaxes() {
        return taxes;
    }

    public void setTaxes(String taxes) {
        this.taxes = taxes;
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

    public String getNetWeight() {
        return netWeight;
    }

    public void setNetWeight(String netWeight) {
        this.netWeight = netWeight;
    }

    public String getInsuredPrice() {
        return insuredPrice;
    }

    public void setInsuredPrice(String insuredPrice) {
        this.insuredPrice = insuredPrice;
    }

    public String getIdNo() {
        return idNo;
    }

    public void setIdNo(String idNo) {
        this.idNo = idNo;
    }

    public String getReceive_name() {
        return receive_name;
    }

    public void setReceive_name(String receive_name) {
        this.receive_name = receive_name;
    }

    public String getReceive_tel() {
        return receive_tel;
    }

    public void setReceive_tel(String receive_tel) {
        this.receive_tel = receive_tel;
    }

    public String getReceive_address() {
        return receive_address;
    }

    public void setReceive_address(String receive_address) {
        this.receive_address = receive_address;
    }

    public String getReceive_city() {
        return receive_city;
    }

    public void setReceive_city(String receive_city) {
        this.receive_city = receive_city;
    }

    public String getZipCode() {
        return zipCode;
    }

    public void setZipCode(String zipCode) {
        this.zipCode = zipCode;
    }
}
