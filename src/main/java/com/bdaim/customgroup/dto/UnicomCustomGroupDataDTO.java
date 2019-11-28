package com.bdaim.customgroup.dto;

/**
 * 联通活动文件客群DTO
 *
 * @author chengning@salescomm.net
 * @date 2019-11-22 16:34
 */
public class UnicomCustomGroupDataDTO {

    private String activityId;
    private String activityName;
    private String customGroupId;
    private String dataId;

    public UnicomCustomGroupDataDTO(String activityId, String activityName, String customGroupId, String dataId) {
        this.activityId = activityId;
        this.activityName = activityName;
        this.customGroupId = customGroupId;
        this.dataId = dataId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getActivityName() {
        return activityName;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }

    public String getCustomGroupId() {
        return customGroupId;
    }

    public void setCustomGroupId(String customGroupId) {
        this.customGroupId = customGroupId;
    }

    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }

    @Override
    public String toString() {
        return "UnicomCustomGroupDataDTO{" +
                "activityId='" + activityId + '\'' +
                ", activityName='" + activityName + '\'' +
                ", customGroupId='" + customGroupId + '\'' +
                ", dataId='" + dataId + '\'' +
                '}';
    }
}
