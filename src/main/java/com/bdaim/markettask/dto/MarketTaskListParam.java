package com.bdaim.markettask.dto;

import com.bdaim.customgroup.dto.CustomerGrpOrdParam;

import java.io.Serializable;

/**
 * 营销任务参数
 */
public class MarketTaskListParam extends CustomerGrpOrdParam implements Serializable {
    private String taskName;
    private String id;
    private String marketProjectId;

    public String getMarketProjectId() {
        return marketProjectId;
    }

    public void setMarketProjectId(String marketProjectId) {
        this.marketProjectId = marketProjectId;
    }

    private String callChannel;

    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCallChannel() {
        return callChannel;
    }

    public void setCallChannel(String callChannel) {
        this.callChannel = callChannel;
    }

    @Override
    public String toString() {
        return "MarketTaskListParam{" +
                "taskName='" + taskName + '\'' +
                ", id='" + id + '\'' +
                ", projectId='" + marketProjectId + '\'' +
                ", callChannel='" + callChannel + '\'' +
                '}';
    }
}
