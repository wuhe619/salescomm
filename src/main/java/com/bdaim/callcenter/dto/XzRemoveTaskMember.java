package com.bdaim.callcenter.dto;

/**
 * 讯众自动外呼任务添加成员
 *
 * @author chengning@salescomm.net
 * @date 2019/4/28
 * @description
 */
public class XzRemoveTaskMember {

    private String taskidentity;
    private String compid;
    /**
     * "members":"1001,1002,1003,1004",
     */
    private String members;

    public String getTaskidentity() {
        return taskidentity;
    }

    public void setTaskidentity(String taskidentity) {
        this.taskidentity = taskidentity;
    }

    public String getCompid() {
        return compid;
    }

    public void setCompid(String compid) {
        this.compid = compid;
    }

    public String getMembers() {
        return members;
    }

    public void setMembers(String members) {
        this.members = members;
    }

    @Override
    public String toString() {
        return "XzRemoveTaskMember{" +
                "taskidentity='" + taskidentity + '\'' +
                ", compid='" + compid + '\'' +
                ", members='" + members + '\'' +
                '}';
    }
}
