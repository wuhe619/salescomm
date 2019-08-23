package com.bdaim.callcenter.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/5/6
 * @description
 */
public class XfPullPhoneDTO {

    private String taskId;
    private String content;
    /**
     * 0-处理正常 1-未查询到营销任务 2-营销任务数据为空 3-处理异常
     */
    private Integer result;

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    @Override
    public String toString() {
        return "XfPullPhoneDTO{" +
                "taskId='" + taskId + '\'' +
                ", content='" + content + '\'' +
                ", result=" + result +
                '}';
    }
}
