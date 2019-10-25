package com.bdaim.resource.dto;

import com.bdaim.resource.entity.MarketResourceEntity;

import java.io.Serializable;
import java.util.Date;

/**
 * @author Created by lanxq
 * @Date 2017年3月2日
 */
public class MarketResourceLogDTO implements Serializable {
    /**
     * id
     */
    private String touch_id;
    /**
     * 客户id
     */
    private String cust_id;
    /**
     * 用户id
     */
    private Long user_id;
    /**
     * '资源类型（1.voice 2.SMS 3.email）'
     */
    private String type_code;
    /**
     * 资源名称
     */
    private String resname;
    /**
     * 备注
     */
    private String remark;
    /**
     * 创建时间
     */
    private String create_time;
    /**
     * 接听状态/发送结果(成功,失败)/
     */
    private int status;
    /**
     * 短信内容
     */
    private String sms_content;
    /**
     * 邮件内容
     */
    private String email_content;
    /**
     * 数据唯一标识id
     */
    private String superId;

    /**
     * 数据唯一标识id
     */
    private Integer templateId;
    /**
     * 触达返回结果
     */
    private String callBackData;

    private String callSid;

    private String batchNumber;

    private Integer customerGroupId;

    private Integer callOwner;

    private String batchId;

    private String activityId;
    /**
     * 企业短信扣费金额
     */
    private Integer amount;
    /**
     * 供应商扣费金额
     */
    private Integer prodAmount;

    private Integer resourceId;
    /**
     * 渠道/供应商 2-联通 3-电信 4-移动
     */
    private Integer channel;

    /**
     * 企业自带ID
     */
    private String enterpriseId;
    private String supplierId;

    private String supplierName;

    /**
     * 资源类型
     */
    private Integer typeCode;
    /**
     * 销售单价(分)
     */
    private Integer salePrice;

    /**
     * 供应商
     */
    private Integer costPrice;

    /**
     * 资源说明
     */
    private String description;
    /**
     * 创建日期
     */
    private Date createTime;

    /**
     * 有效期
     */
    private Date validDate;

    /**
     * 失效期
     */
    private Date expireDate;

    /**
     * 资源图片path
     */
    private String resPicPath;
    private String resourceProperty;

    /**
     * 职场ID
     */
    private String cugId;

    /**
     * 营销任务ID
     */
    private String marketTaskId;

    /**
     * 接口请求状态
     */
    private Integer sendStatus;

    public Integer getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(Integer sendStatus) {
        this.sendStatus = sendStatus;
    }

    /**
     * 公海ID
     */

    private String customerSeaId;

    /**
     * 接口请求返回的唯一id
     */
    private String requestId;
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getCallBackData() {
        return callBackData;
    }

    public void setCallBackData(String callBackData) {
        this.callBackData = callBackData;
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

    public Integer getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(Integer typeCode) {
        this.typeCode = typeCode;
    }

    public Integer getSalePrice() {
        return salePrice;
    }

    public void setSalePrice(Integer salePrice) {
        this.salePrice = salePrice;
    }

    public Integer getCostPrice() {
        return costPrice;
    }

    public void setCostPrice(Integer costPrice) {
        this.costPrice = costPrice;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getValidDate() {
        return validDate;
    }

    public void setValidDate(Date validDate) {
        this.validDate = validDate;
    }

    public Date getExpireDate() {
        return expireDate;
    }

    public void setExpireDate(Date expireDate) {
        this.expireDate = expireDate;
    }

    public String getResPicPath() {
        return resPicPath;
    }

    public void setResPicPath(String resPicPath) {
        this.resPicPath = resPicPath;
    }

    public String getResourceProperty() {
        return resourceProperty;
    }

    public void setResourceProperty(String resourceProperty) {
        this.resourceProperty = resourceProperty;
    }

    public MarketResourceLogDTO() {
        super();
        // TODO Auto-generated constructor stub
    }

    public MarketResourceLogDTO(String touch_id, String cust_id, Long user_id, String type_code, String resname, String remark,
                                String create_time, int status, String sms_content, String email_content, String superId, Integer templateId, String callSid, String batchNumber, Integer customerGroupId, Integer callOwner) {
        this.touch_id = touch_id;
        this.cust_id = cust_id;
        this.user_id = user_id;
        this.type_code = type_code;
        this.resname = resname;
        this.remark = remark;
        this.create_time = create_time;
        this.status = status;
        this.sms_content = sms_content;
        this.email_content = email_content;
        this.superId = superId;
        this.templateId = templateId;
        this.callSid = callSid;
        this.batchNumber = batchNumber;
        this.customerGroupId = customerGroupId;
        this.callOwner = callOwner;
    }

    public MarketResourceLogDTO(String touch_id, String cust_id, Long user_id, String type_code, String resname, String remark, String create_time, int status, String sms_content, String email_content, String superId, Integer templateId, String callBackData, String callSid, String batchNumber, Integer customerGroupId, Integer callOwner, String batchId, String activityId, Integer amount, Integer prodAmount, Integer resourceId, Integer channel, String enterpriseId, String supplierId, String supplierName, Integer typeCode, Integer salePrice, Integer costPrice, String description, Date createTime, Date validDate, Date expireDate, String resPicPath, String resourceProperty) {
        this.touch_id = touch_id;
        this.cust_id = cust_id;
        this.user_id = user_id;
        this.type_code = type_code;
        this.resname = resname;
        this.remark = remark;
        this.create_time = create_time;
        this.status = status;
        this.sms_content = sms_content;
        this.email_content = email_content;
        this.superId = superId;
        this.templateId = templateId;
        this.callBackData = callBackData;
        this.callSid = callSid;
        this.batchNumber = batchNumber;
        this.customerGroupId = customerGroupId;
        this.callOwner = callOwner;
        this.batchId = batchId;
        this.activityId = activityId;
        this.amount = amount;
        this.prodAmount = prodAmount;
        this.resourceId = resourceId;
        this.channel = channel;
        this.enterpriseId = enterpriseId;
        this.supplierId = supplierId;
        this.supplierName = supplierName;
        this.typeCode = typeCode;
        this.salePrice = salePrice;
        this.costPrice = costPrice;
        this.description = description;
        this.createTime = createTime;
        this.validDate = validDate;
        this.expireDate = expireDate;
        this.resPicPath = resPicPath;
        this.resourceProperty = resourceProperty;
    }

    public MarketResourceLogDTO(MarketResourceEntity marketResourceEntity) {
        this.resourceId = marketResourceEntity.getResourceId();
        this.supplierId = marketResourceEntity.getSupplierId();
        this.typeCode = marketResourceEntity.getTypeCode();
        this.resname = marketResourceEntity.getResname();
        this.salePrice = marketResourceEntity.getSalePrice();
        this.costPrice = marketResourceEntity.getCostPrice();
        this.description = marketResourceEntity.getDescription();
        this.status = marketResourceEntity.getStatus();
        this.createTime = marketResourceEntity.getCreateTime();
        this.validDate = marketResourceEntity.getValidDate();
        this.expireDate = marketResourceEntity.getExpireDate();
        this.resPicPath = marketResourceEntity.getResPicPath();
    }

    public String getTouch_id() {
        return touch_id;
    }

    public void setTouch_id(String touch_id) {
        this.touch_id = touch_id;
    }

    public String getCust_id() {
        return cust_id;
    }

    public void setCust_id(String cust_id) {
        this.cust_id = cust_id;
    }

    public Long getUser_id() {
        return user_id;
    }

    public void setUser_id(Long user_id) {
        this.user_id = user_id;
    }

    public String getType_code() {
        return type_code;
    }

    public void setType_code(String type_code) {
        this.type_code = type_code;
    }

    public String getResname() {
        return resname;
    }

    public void setResname(String resname) {
        this.resname = resname;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getCreate_time() {
        return create_time;
    }

    public void setCreate_time(String create_time) {
        this.create_time = create_time;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getSms_content() {
        return sms_content;
    }

    public void setSms_content(String sms_content) {
        this.sms_content = sms_content;
    }

    public String getEmail_content() {
        return email_content;
    }

    public void setEmail_content(String email_content) {
        this.email_content = email_content;
    }

    public String getSuperId() {
        return superId;
    }

    public void setSuperId(String superId) {
        this.superId = superId;
    }

    public Integer getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Integer templateId) {
        this.templateId = templateId;
    }

    public String getCallSid() {
        return callSid;
    }

    public void setCallSid(String callSid) {
        this.callSid = callSid;
    }

    public String getBatchNumber() {
        return batchNumber;
    }

    public void setBatchNumber(String batchNumber) {
        this.batchNumber = batchNumber;
    }

    public Integer getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(Integer customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public Integer getCallOwner() {
        return callOwner;
    }

    public void setCallOwner(Integer callOwner) {
        this.callOwner = callOwner;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getProdAmount() {
        return prodAmount;
    }

    public void setProdAmount(Integer prodAmount) {
        this.prodAmount = prodAmount;
    }

    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getCugId() {
        return cugId;
    }

    public void setCugId(String cugId) {
        this.cugId = cugId;
    }

    public String getMarketTaskId() {
        return marketTaskId;
    }

    public void setMarketTaskId(String marketTaskId) {
        this.marketTaskId = marketTaskId;
    }

    public String getCustomerSeaId() {
        return customerSeaId;
    }

    public void setCustomerSeaId(String customerSeaId) {
        this.customerSeaId = customerSeaId;
    }

    @Override
    public String toString() {
        return "MarketResourceLogDTO{" +
                "touch_id='" + touch_id + '\'' +
                ", cust_id='" + cust_id + '\'' +
                ", user_id=" + user_id +
                ", type_code='" + type_code + '\'' +
                ", resname='" + resname + '\'' +
                ", remark='" + remark + '\'' +
                ", create_time='" + create_time + '\'' +
                ", status=" + status +
                ", sms_content='" + sms_content + '\'' +
                ", email_content='" + email_content + '\'' +
                ", superId='" + superId + '\'' +
                ", templateId=" + templateId +
                ", callBackData='" + callBackData + '\'' +
                ", callSid='" + callSid + '\'' +
                ", batchNumber='" + batchNumber + '\'' +
                ", customerGroupId=" + customerGroupId +
                ", callOwner=" + callOwner +
                ", batchId='" + batchId + '\'' +
                ", activityId='" + activityId + '\'' +
                ", amount=" + amount +
                ", prodAmount=" + prodAmount +
                ", resourceId=" + resourceId +
                ", channel=" + channel +
                ", enterpriseId='" + enterpriseId + '\'' +
                ", supplierId='" + supplierId + '\'' +
                ", supplierName='" + supplierName + '\'' +
                ", typeCode=" + typeCode +
                ", salePrice=" + salePrice +
                ", costPrice=" + costPrice +
                ", description='" + description + '\'' +
                ", createTime=" + createTime +
                ", validDate=" + validDate +
                ", expireDate=" + expireDate +
                ", resPicPath='" + resPicPath + '\'' +
                ", resourceProperty='" + resourceProperty + '\'' +
                ", cugId='" + cugId + '\'' +
                ", marketTaskId='" + marketTaskId + '\'' +
                ", customerSeaId='" + customerSeaId + '\'' +
                '}';
    }
}
