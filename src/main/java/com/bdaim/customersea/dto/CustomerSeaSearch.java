package com.bdaim.customersea.dto;

import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;

/**
 * 公海基础表
 *
 * @author chengning@salescomm.net
 * @date 2019/6/22
 * @description
 */
public class CustomerSeaSearch {

    @NotNull(message = "pageNum参数必填")
    @Min(value = 0, message = "pageNum最小值为0")
    private Integer pageNum;
    @NotNull(message = "pageSize参数必填")
    @Min(value = 1, message = "pageSize最小值为1")
    @Max(value = 100, message = "pageSize最大值为100")
    private Integer pageSize;

    @NotNull(message = "seaId参数必填")
    private String seaId;

    private String superId;
    private String custId;
    /**
     * 意向等级 A-F
     */
    private String intentLevel;

    private String labelProperty;
    private String userType;
    private Long userId;
    /**
     * 线索来源 1-购买 2-导入 3-添加 4-回收
     */
    private Integer dataSource;
    private String batchId;
    private String superName;
    private String superPhone;
    private String superTelphone;
    private String lastUserId;
    private String lastUserName;
    private String lastCallTime;
    private String lastCallResult;
    private Integer calledDuration;
    private String action;
    private String userName;
    private Integer status;
    private Integer callCount;
    private String userGroupRole;
    private String userGroupId;
    private String addStartTime;
    private String addEndTime;
    private String callStartTime;
    private String callEndTime;

    private String userGetStartTime;
    private String userGetEndTime;
    private String lastMarkStartTime;
    private String lastMarkEndTime;

    private List<String> superIds;
    private List<String> userIds;

    /**
     * 1-未呼通 2-已呼通
     */
    private String callStatus;

    /**
     * 获取线索数量
     */
    private Integer getClueNumber;

    /**
     * 线索被转交人
     */
    private String clueToUserId;

    private Integer callSuccessCount;
    /**
     * 搜索的跟进状态
     */
    private String followStatus;

    /**
     * 无效原因
     */
    private String invalidReason;

    /**
     * 搜索的跟进状态值
     */
    private String followValue;

    /**
     * 变更的跟进状态
     */
    private String toFollowStatus;

    /**
     * 变更的跟进状态值
     */
    private String toFollowValue;

    /**
     * 退回原因
     */
    private String backReason;
    /**
     * 退回备注
     */
    private String backRemark;

    /**
     * 1-公海 2-私海
     */
    private Integer seaType;

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

    public String getSuperId() {
        return superId;
    }

    public void setSuperId(String superId) {
        this.superId = superId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getIntentLevel() {
        return intentLevel;
    }

    public void setIntentLevel(String intentLevel) {
        this.intentLevel = intentLevel;
    }

    public String getLabelProperty() {
        return labelProperty;
    }

    public void setLabelProperty(String labelProperty) {
        this.labelProperty = labelProperty;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Integer getDataSource() {
        return dataSource;
    }

    public void setDataSource(Integer dataSource) {
        this.dataSource = dataSource;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getSuperName() {
        return superName;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public String getSuperPhone() {
        return superPhone;
    }

    public void setSuperPhone(String superPhone) {
        this.superPhone = superPhone;
    }

    public String getSuperTelphone() {
        return superTelphone;
    }

    public void setSuperTelphone(String superTelphone) {
        this.superTelphone = superTelphone;
    }

    public String getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(String lastUserId) {
        this.lastUserId = lastUserId;
    }

    public String getLastCallTime() {
        return lastCallTime;
    }

    public void setLastCallTime(String lastCallTime) {
        this.lastCallTime = lastCallTime;
    }

    public String getLastCallResult() {
        return lastCallResult;
    }

    public void setLastCallResult(String lastCallResult) {
        this.lastCallResult = lastCallResult;
    }

    public Integer getCalledDuration() {
        return calledDuration;
    }

    public void setCalledDuration(Integer calledDuration) {
        this.calledDuration = calledDuration;
    }

    public String getAction() {
        return action;
    }

    public void setAction(String action) {
        this.action = action;
    }

    public String getSeaId() {
        return seaId;
    }

    public void setSeaId(String seaId) {
        this.seaId = seaId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCallCount() {
        return callCount;
    }

    public void setCallCount(Integer callCount) {
        this.callCount = callCount;
    }

    public String getUserGroupRole() {
        return userGroupRole;
    }

    public void setUserGroupRole(String userGroupRole) {
        this.userGroupRole = userGroupRole;
    }

    public String getUserGroupId() {
        return userGroupId;
    }

    public void setUserGroupId(String userGroupId) {
        this.userGroupId = userGroupId;
    }

    public String getLastUserName() {
        return lastUserName;
    }

    public void setLastUserName(String lastUserName) {
        this.lastUserName = lastUserName;
    }

    public String getAddStartTime() {
        return addStartTime;
    }

    public void setAddStartTime(String addStartTime) {
        this.addStartTime = addStartTime;
    }

    public String getAddEndTime() {
        return addEndTime;
    }

    public void setAddEndTime(String addEndTime) {
        this.addEndTime = addEndTime;
    }

    public String getCallStartTime() {
        return callStartTime;
    }

    public void setCallStartTime(String callStartTime) {
        this.callStartTime = callStartTime;
    }

    public String getCallEndTime() {
        return callEndTime;
    }

    public void setCallEndTime(String callEndTime) {
        this.callEndTime = callEndTime;
    }

    public List<String> getSuperIds() {
        return superIds;
    }

    public void setSuperIds(List<String> superIds) {
        this.superIds = superIds;
    }

    public List<String> getUserIds() {
        return userIds;
    }

    public void setUserIds(List<String> userIds) {
        this.userIds = userIds;
    }

    public String getCallStatus() {
        return callStatus;
    }

    public void setCallStatus(String callStatus) {
        this.callStatus = callStatus;
    }

    public Integer getGetClueNumber() {
        return getClueNumber;
    }

    public void setGetClueNumber(Integer getClueNumber) {
        this.getClueNumber = getClueNumber;
    }

    public String getClueToUserId() {
        return clueToUserId;
    }

    public void setClueToUserId(String clueToUserId) {
        this.clueToUserId = clueToUserId;
    }

    public Integer getCallSuccessCount() {
        return callSuccessCount;
    }

    public void setCallSuccessCount(Integer callSuccessCount) {
        this.callSuccessCount = callSuccessCount;
    }

    public String getFollowStatus() {
        return followStatus;
    }

    public void setFollowStatus(String followStatus) {
        this.followStatus = followStatus;
    }

    public String getFollowValue() {
        return followValue;
    }

    public void setFollowValue(String followValue) {
        this.followValue = followValue;
    }

    public String getToFollowStatus() {
        return toFollowStatus;
    }

    public void setToFollowStatus(String toFollowStatus) {
        this.toFollowStatus = toFollowStatus;
    }

    public String getToFollowValue() {
        return toFollowValue;
    }

    public void setToFollowValue(String toFollowValue) {
        this.toFollowValue = toFollowValue;
    }

    public String getUserGetStartTime() {
        return userGetStartTime;
    }

    public void setUserGetStartTime(String userGetStartTime) {
        this.userGetStartTime = userGetStartTime;
    }

    public String getUserGetEndTime() {
        return userGetEndTime;
    }

    public void setUserGetEndTime(String userGetEndTime) {
        this.userGetEndTime = userGetEndTime;
    }

    public String getLastMarkStartTime() {
        return lastMarkStartTime;
    }

    public void setLastMarkStartTime(String lastMarkStartTime) {
        this.lastMarkStartTime = lastMarkStartTime;
    }

    public String getLastMarkEndTime() {
        return lastMarkEndTime;
    }

    public void setLastMarkEndTime(String lastMarkEndTime) {
        this.lastMarkEndTime = lastMarkEndTime;
    }

    public String getBackReason() {
        return backReason;
    }

    public void setBackReason(String backReason) {
        this.backReason = backReason;
    }

    public String getBackRemark() {
        return backRemark;
    }

    public void setBackRemark(String backRemark) {
        this.backRemark = backRemark;
    }

    public Integer getSeaType() {
        return seaType;
    }

    public void setSeaType(Integer seaType) {
        this.seaType = seaType;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    @Override
    public String toString() {
        return "CustomerSeaSearch{" +
                "pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", seaId='" + seaId + '\'' +
                ", superId='" + superId + '\'' +
                ", custId='" + custId + '\'' +
                ", intentLevel='" + intentLevel + '\'' +
                ", labelProperty='" + labelProperty + '\'' +
                ", userType='" + userType + '\'' +
                ", userId=" + userId +
                ", dataSource=" + dataSource +
                ", batchId='" + batchId + '\'' +
                ", superName='" + superName + '\'' +
                ", superPhone='" + superPhone + '\'' +
                ", superTelphone='" + superTelphone + '\'' +
                ", lastUserId='" + lastUserId + '\'' +
                ", lastUserName='" + lastUserName + '\'' +
                ", lastCallTime='" + lastCallTime + '\'' +
                ", lastCallResult='" + lastCallResult + '\'' +
                ", calledDuration=" + calledDuration +
                ", action='" + action + '\'' +
                ", userName='" + userName + '\'' +
                ", status=" + status +
                ", callCount=" + callCount +
                ", userGroupRole='" + userGroupRole + '\'' +
                ", userGroupId='" + userGroupId + '\'' +
                ", addStartTime='" + addStartTime + '\'' +
                ", addEndTime='" + addEndTime + '\'' +
                ", callStartTime='" + callStartTime + '\'' +
                ", callEndTime='" + callEndTime + '\'' +
                ", userGetStartTime='" + userGetStartTime + '\'' +
                ", userGetEndTime='" + userGetEndTime + '\'' +
                ", lastMarkStartTime='" + lastMarkStartTime + '\'' +
                ", lastMarkEndTime='" + lastMarkEndTime + '\'' +
                ", superIds=" + superIds +
                ", userIds=" + userIds +
                ", callStatus='" + callStatus + '\'' +
                ", getClueNumber=" + getClueNumber +
                ", clueToUserId='" + clueToUserId + '\'' +
                ", callSuccessCount=" + callSuccessCount +
                ", followStatus='" + followStatus + '\'' +
                ", invalidReason='" + invalidReason + '\'' +
                ", followValue='" + followValue + '\'' +
                ", toFollowStatus='" + toFollowStatus + '\'' +
                ", toFollowValue='" + toFollowValue + '\'' +
                ", backReason='" + backReason + '\'' +
                ", backRemark='" + backRemark + '\'' +
                ", seaType=" + seaType +
                '}';
    }
}
