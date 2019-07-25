package com.bdaim.template.dto;

/**
 * @author yanls@salescomm.net
 * @date 2018/10/15
 * @description
 */
public class TemplateParam {
    private String compId;
    //资源类型（ 1.SMS 2.email 3.闪信）
    private String typeCode;
    //模板名称
    private String templateName;
    //模板ID
    private String templateId;
    //模板内容
    private String templateContent;
    //状态（1.审核中2.审批通过 3.审批未通过）
    private String status;
    //短信签名
    private String smsSignatures;
    //处理类型  1000创建  2000修改  3000审核    4000无效与恢复
    private String dealType;
    private int pageNum;
    private int pageSize;
    private String templateCode;
    private String remark;
    private String enterPriseName;

    public String getEnterPriseName() {
        return enterPriseName;
    }

    public void setEnterPriseName(String enterPriseName) {
        this.enterPriseName = enterPriseName;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getDealType() {
        return dealType;
    }

    public void setDealType(String dealType) {
        this.dealType = dealType;
    }

    public String getTemplateContent() {
        return templateContent;
    }

    public void setTemplateContent(String templateContent) {
        this.templateContent = templateContent;
    }

    public String getCompId() {
        return compId;
    }

    public void setCompId(String compId) {
        this.compId = compId;
    }

    public String getTypeCode() {
        return typeCode;
    }

    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getSmsSignatures() {
        return smsSignatures;
    }

    public void setSmsSignatures(String smsSignatures) {
        this.smsSignatures = smsSignatures;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }
}
