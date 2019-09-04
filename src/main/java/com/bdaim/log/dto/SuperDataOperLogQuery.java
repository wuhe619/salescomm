package com.bdaim.log.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2019/7/9
 * @description
 */
public class SuperDataOperLogQuery {
    @NotNull(message = "pageNum参数必填")
    @Min(value = 0, message = "pageNum最小值为0")
    private Integer pageNum;
    @NotNull(message = "pageSize参数必填")
    @Min(value = 1, message = "pageSize最小值为1")
    @Max(value = 100, message = "pageSize最大值为100")
    private Integer pageSize;


    private Long userId;
    private String userName;
    private String dataId;
    @NotNull(message = "seaId参数必填")
    private String seaId;
    private String customerGroupId;
    private Integer eventType;
    private String objId;
    private String objName;
    private Timestamp createTime;
    private String reason;
    private String remark;
    private String superId;

    public SuperDataOperLogQuery() {
    }

    public String getSuperId() {
        return superId;
    }

    public void setSuperId(String superId) {
        this.superId = superId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    public String getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(String customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    public Integer getEventType() {
        return eventType;
    }

    public void setEventType(Integer eventType) {
        this.eventType = eventType;
    }

    public String getObjId() {
        return objId;
    }

    public void setObjId(String objId) {
        this.objId = objId;
    }

    public String getObjName() {
        return objName;
    }

    public void setObjName(String objName) {
        this.objName = objName;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    public String getSeaId() {
        return seaId;
    }

    public void setSeaId(String seaId) {
        this.seaId = seaId;
    }

    @Override
    public String toString() {
        return "SuperDataOperLogQuery{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", userId=" + userId +
                ", userName='" + userName + '\'' +
                ", dataId='" + dataId + '\'' +
                ", seaId='" + seaId + '\'' +
                ", customerGroupId='" + customerGroupId + '\'' +
                ", eventType=" + eventType +
                ", objId='" + objId + '\'' +
                ", objName='" + objName + '\'' +
                ", createTime=" + createTime +
                ", reason='" + reason + '\'' +
                ", remark='" + remark + '\'' +
                '}';
    }
}
