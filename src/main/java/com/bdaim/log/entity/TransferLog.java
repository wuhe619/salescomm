package com.bdaim.log.entity;

public class TransferLog {

    private String taskId;

    private int state;

    public TransferLog() {
    }

    public TransferLog(String taskId, int state) {
        this.taskId = taskId;
        this.state = state;
    }

    public TransferLog(int state) {
        this.state = state;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }
}
