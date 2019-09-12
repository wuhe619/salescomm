package com.bdaim.customersea.dto;

/**
 * 公海短信参数
 *
 * @date 2019/6/22
 * @description
 */
public class CustomerSeaSmsSearch extends CustomerSeaSearch {
    private String type;
    private String templateId;
    private String superidlist;
    private String customerGroupId;
    private String marketTaskId;
    private String seaId;
    private String smsBatchName;
    private int isRecord;

    public CustomerSeaSmsSearch(String type, String templateId, String superidlist, String customerGroupId, String marketTaskId, String seaId, String smsBatchName, int isRecord) {
        this.type = type;
        this.templateId = templateId;
        this.superidlist = superidlist;
        this.customerGroupId = customerGroupId;
        this.marketTaskId = marketTaskId;
        this.seaId = seaId;
        this.smsBatchName = smsBatchName;
        this.isRecord = isRecord;
    }

    public CustomerSeaSmsSearch() {
    }

    public int getIsRecord() {
        return isRecord;
    }

    public void setIsRecord(int isRecord) {
        this.isRecord = isRecord;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getSuperidlist() {
        return superidlist;
    }

    public void setSuperidlist(String superidlist) {
        this.superidlist = superidlist;
    }

    public String getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(String customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public String getMarketTaskId() {
        return marketTaskId;
    }

    public void setMarketTaskId(String marketTaskId) {
        this.marketTaskId = marketTaskId;
    }

    @Override
    public String getSeaId() {
        return seaId;
    }

    @Override
    public void setSeaId(String seaId) {
        this.seaId = seaId;
    }

    public String getSmsBatchName() {
        return smsBatchName;
    }

    public void setSmsBatchName(String smsBatchName) {
        this.smsBatchName = smsBatchName;
    }

    @Override
    public String toString() {
        return "CustomerSeaSmsSearch{" +
                "type='" + type + '\'' +
                ", templateId='" + templateId + '\'' +
                ", superidlist='" + superidlist + '\'' +
                ", customerGroupId='" + customerGroupId + '\'' +
                ", marketTaskId='" + marketTaskId + '\'' +
                ", seaId='" + seaId + '\'' +
                ", smsBatchName='" + smsBatchName + '\'' +
                ", isRecord=" + isRecord +
                '}';
    }
}
