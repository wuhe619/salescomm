package com.bdaim.slxf.entity;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/14
 * @description
 */
public class UnicomSendSmsParam {

    private String entId;
    private String userId;
    private String activityId;
    private String provideId;
    private String customerId;
    private String messageCode;
    private String variableOne;
    private String variableTwo;
    private String variableThree;
    private String variableFour;
    private String variableFive;

    public String getEntId() {
        return entId;
    }

    public void setEntId(String entId) {
        this.entId = entId;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public String getProvideId() {
        return provideId;
    }

    public void setProvideId(String provideId) {
        this.provideId = provideId;
    }

    public String getCustomerId() {
        return customerId;
    }

    public void setCustomerId(String customerId) {
        this.customerId = customerId;
    }

    public String getMessageCode() {
        return messageCode;
    }

    public void setMessageCode(String messageCode) {
        this.messageCode = messageCode;
    }

    public String getVariableOne() {
        return variableOne;
    }

    public void setVariableOne(String variableOne) {
        this.variableOne = variableOne;
    }

    public String getVariableTwo() {
        return variableTwo;
    }

    public void setVariableTwo(String variableTwo) {
        this.variableTwo = variableTwo;
    }

    public String getVariableThree() {
        return variableThree;
    }

    public void setVariableThree(String variableThree) {
        this.variableThree = variableThree;
    }

    public String getVariableFour() {
        return variableFour;
    }

    public void setVariableFour(String variableFour) {
        this.variableFour = variableFour;
    }

    public String getVariableFive() {
        return variableFive;
    }

    public void setVariableFive(String variableFive) {
        this.variableFive = variableFive;
    }

    @Override
    public String toString() {
        return "UnicomSendSmsParam{" +
                "entId='" + entId + '\'' +
                ", userId='" + userId + '\'' +
                ", activityId='" + activityId + '\'' +
                ", provideId='" + provideId + '\'' +
                ", customerId='" + customerId + '\'' +
                ", messageCode='" + messageCode + '\'' +
                ", variableOne='" + variableOne + '\'' +
                ", variableTwo='" + variableTwo + '\'' +
                ", variableThree='" + variableThree + '\'' +
                ", variableFour='" + variableFour + '\'' +
                ", variableFive='" + variableFive + '\'' +
                '}';
    }
}
