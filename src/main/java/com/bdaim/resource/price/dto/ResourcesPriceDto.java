package com.bdaim.resource.price.dto;

/**
 * @author duanliying
 * @date 2019/4/8
 * 资源定价Dto
 */
public class ResourcesPriceDto {
    //定价
    private String addressPrice;
    private String smsPrice;
    private String callPrice;
    private String macPrice;
    private String imeiPrice;
    //ifPartIndustry--是否区分行业 0：是 1:否
    private String ifPartIndustry;
    private String idCardPrice;
    //fixPriceRest ----其他行业价格
    private String fixPriceRest;
    private String fixPriceBank;
    private String fixPriceInsurance;
    private String fixPriceCourt;
    private String fixPriceOnline;
    private String successPrice;
    private String failPrice;
    private String seatPrice;
    private int seatMinute;
    private String apparentPrice;
    //案件id
    private String activityId;
    private String callCenterId;
    /**
     * 企业密码
     */
    private String entPassWord;
    private String apparentNumber;
    private String resourceId;
    private String resourceType;
    private String resourceName;
    //billingMode ----计费模式 0:按条计费单一价格；1：按身份证计费单一价格
    private String billingMode;
    //lineType -----线路类型 0：呼叫中心 1：双向呼叫
    private String lineType;
    //ifHalfMonth ---坐席是否半月五折 0：是 1：否
    private String ifHalfMonth;
    private String partnerName;
    private String supplierId;

    public String getSecretId() {
        return secretId;
    }

    public void setSecretId(String secretId) {
        this.secretId = secretId;
    }

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    private String secretId;

    private String secretKey;

    public ResourcesPriceDto() {
    }

    public String getSupplierId() {
        return supplierId;
    }

    public String getEntPassWord() {
        return entPassWord;
    }

    public void setEntPassWord(String entPassWord) {
        this.entPassWord = entPassWord;
    }

    public void setSupplierId(String supplierId) {
        this.supplierId = supplierId;
    }

    public int getSeatMinute() {
        return seatMinute;
    }

    public void setSeatMinute(int seatMinute) {
        this.seatMinute = seatMinute;
    }

    public String getAddressPrice() {
        return addressPrice;
    }

    public void setAddressPrice(String addressPrice) {
        this.addressPrice = addressPrice;
    }

    public String getSmsPrice() {
        return smsPrice;
    }

    public void setSmsPrice(String smsPrice) {
        this.smsPrice = smsPrice;
    }

    public String getCallPrice() {
        return callPrice;
    }

    public void setCallPrice(String callPrice) {
        this.callPrice = callPrice;
    }

    public String getMacPrice() {
        return macPrice;
    }

    public void setMacPrice(String macPrice) {
        this.macPrice = macPrice;
    }

    public String getImeiPrice() {
        return imeiPrice;
    }

    public void setImeiPrice(String imeiPrice) {
        this.imeiPrice = imeiPrice;
    }

    public String getIfPartIndustry() {
        return ifPartIndustry;
    }

    public void setIfPartIndustry(String ifPartIndustry) {
        this.ifPartIndustry = ifPartIndustry;
    }

    public String getIdCardPrice() {
        return idCardPrice;
    }

    public void setIdCardPrice(String idCardPrice) {
        this.idCardPrice = idCardPrice;
    }

    public String getFixPriceRest() {
        return fixPriceRest;
    }

    public void setFixPriceRest(String fixPriceRest) {
        this.fixPriceRest = fixPriceRest;
    }

    public String getFixPriceBank() {
        return fixPriceBank;
    }

    public void setFixPriceBank(String fixPriceBank) {
        this.fixPriceBank = fixPriceBank;
    }

    public String getFixPriceInsurance() {
        return fixPriceInsurance;
    }

    public void setFixPriceInsurance(String fixPriceInsurance) {
        this.fixPriceInsurance = fixPriceInsurance;
    }

    public String getFixPriceCourt() {
        return fixPriceCourt;
    }

    public void setFixPriceCourt(String fixPriceCourt) {
        this.fixPriceCourt = fixPriceCourt;
    }

    public String getFixPriceOnline() {
        return fixPriceOnline;
    }

    public void setFixPriceOnline(String fixPriceOnline) {
        this.fixPriceOnline = fixPriceOnline;
    }

    public String getSuccessPrice() {
        return successPrice;
    }

    public void setSuccessPrice(String successPrice) {
        this.successPrice = successPrice;
    }

    public String getFailPrice() {
        return failPrice;
    }

    public void setFailPrice(String failPrice) {
        this.failPrice = failPrice;
    }

    public String getSeatPrice() {
        return seatPrice;
    }

    public void setSeatPrice(String seatPrice) {
        this.seatPrice = seatPrice;
    }

    public String getApparentPrice() {
        return apparentPrice;
    }

    public void setApparentPrice(String apparentPrice) {
        this.apparentPrice = apparentPrice;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getCallCenterId() {
        return callCenterId;
    }

    public void setCallCenterId(String callCenterId) {
        this.callCenterId = callCenterId;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
    }

    public String getResourceId() {
        return resourceId;
    }

    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public String getResourceType() {
        return resourceType;
    }

    public void setResourceType(String resourceType) {
        this.resourceType = resourceType;
    }

    public String getResourceName() {
        return resourceName;
    }

    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    public String getBillingMode() {
        return billingMode;
    }

    public void setBillingMode(String billingMode) {
        this.billingMode = billingMode;
    }

    public String getLineType() {
        return lineType;
    }

    public void setLineType(String lineType) {
        this.lineType = lineType;
    }

    public String getIfHalfMonth() {
        return ifHalfMonth;
    }

    public void setIfHalfMonth(String ifHalfMonth) {
        this.ifHalfMonth = ifHalfMonth;
    }

    public String getPartnerName() {
        return partnerName;
    }

    public void setPartnerName(String partnerName) {
        this.partnerName = partnerName;
    }

    @Override
    public String toString() {
        return "ResourcesPriceDto{" +
                "addressPrice='" + addressPrice + '\'' +
                ", smsPrice='" + smsPrice + '\'' +
                ", callPrice='" + callPrice + '\'' +
                ", macPrice='" + macPrice + '\'' +
                ", imeiPrice='" + imeiPrice + '\'' +
                ", ifPartIndustry='" + ifPartIndustry + '\'' +
                ", idCardPrice='" + idCardPrice + '\'' +
                ", fixPriceRest='" + fixPriceRest + '\'' +
                ", fixPriceBank='" + fixPriceBank + '\'' +
                ", fixPriceInsurance='" + fixPriceInsurance + '\'' +
                ", fixPriceCourt='" + fixPriceCourt + '\'' +
                ", fixPriceOnline='" + fixPriceOnline + '\'' +
                ", successPrice='" + successPrice + '\'' +
                ", failPrice='" + failPrice + '\'' +
                ", seatPrice='" + seatPrice + '\'' +
                ", apparentPrice='" + apparentPrice + '\'' +
                ", activityId='" + activityId + '\'' +
                ", callCenterId='" + callCenterId + '\'' +
                ", apparentNumber='" + apparentNumber + '\'' +
                ", resourceId='" + resourceId + '\'' +
                ", resourceType='" + resourceType + '\'' +
                ", resourceName='" + resourceName + '\'' +
                ", billingMode='" + billingMode + '\'' +
                ", lineType='" + lineType + '\'' +
                ", ifHalfMonth='" + ifHalfMonth + '\'' +
                ", partnerName='" + partnerName + '\'' +
                '}';
    }
}
