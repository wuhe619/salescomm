package com.bdaim.batch.entity;

import com.alibaba.fastjson.annotation.JSONType;

import java.io.Serializable;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/9/10 14:46
 */
@JSONType(orders = { "entId", "activityId"})
public class BatchGetForFile implements Serializable {
    private static final long serialVersionUID = 2008078609888536695L;

    private String entId;
    private String activityId;

    public String getEntId() {
        return entId;
    }

    public void setEntId(String entId) {
        this.entId = entId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

}
