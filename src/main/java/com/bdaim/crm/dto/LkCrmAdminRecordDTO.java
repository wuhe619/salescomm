package com.bdaim.crm.dto;

import java.util.Date;

public class LkCrmAdminRecordDTO {
    private Integer recordId;
    private String types;
    private String typesId;
    private String content;
    private String category;
    private Date nextTime;
    private String businessIds;
    private String contactsIds;
    private int createUserId;
    private String batchId;
    private Integer isEvent;
    private String seaId;
    private String taskName;
    private Integer isTask;

    public Integer getRecordId() {
        return recordId;
    }

    public void setRecordId(Integer recordId) {
        this.recordId = recordId;
    }

    public String getTypes() {
        return types;
    }

    public void setTypes(String types) {
        this.types = types;
    }

    public String getTypesId() {
        return typesId;
    }

    public void setTypesId(String typesId) {
        this.typesId = typesId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public Date getNextTime() {
        return nextTime;
    }

    public void setNextTime(Date nextTime) {
        this.nextTime = nextTime;
    }

    public String getBusinessIds() {
        return businessIds;
    }

    public void setBusinessIds(String businessIds) {
        this.businessIds = businessIds;
    }

    public String getContactsIds() {
        return contactsIds;
    }

    public void setContactsIds(String contactsIds) {
        this.contactsIds = contactsIds;
    }

    public int getCreateUserId() {
        return createUserId;
    }

    public void setCreateUserId(int createUserId) {
        this.createUserId = createUserId;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public Integer getIsEvent() {
        return isEvent;
    }

    public void setIsEvent(Integer isEvent) {
        this.isEvent = isEvent;
    }

    public String getSeaId() {
        return seaId;
    }

    public void setSeaId(String seaId) {
        this.seaId = seaId;
    }

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public Integer getIsTask() {
        return isTask;
    }

    public void setIsTask(Integer isTask) {
        this.isTask = isTask;
    }
}
