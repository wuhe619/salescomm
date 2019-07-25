package com.bdaim.slxf.entity;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/9/13 19:17
 */
public class ActivFileAllDetail {
    private String activityId;
    private String activityBeginDate;
    private String activityName;
    private String activityEndDate;
    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }
    public String getActivityId() {
        return activityId;
    }

    public void setActivityBeginDate(String activityBeginDate) {
        this.activityBeginDate = activityBeginDate;
    }
    public String getActivityBeginDate() {
        return activityBeginDate;
    }

    public void setActivityName(String activityName) {
        this.activityName = activityName;
    }
    public String getActivityName() {
        return activityName;
    }

    public void setActivityEndDate(String activityEndDate) {
        this.activityEndDate = activityEndDate;
    }
    public String getActivityEndDate() {
        return activityEndDate;
    }
}
