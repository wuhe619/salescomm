package com.bdaim.slxf.dto;

import java.io.Serializable;

/**
 * @author duanliying
 * @date 2018/10/15
 * @description   联通短信发送
 */
public class SendSmsDTO implements Serializable {
    //企业ID
    private String entId;
    //活动ID
    private String activityId;
    //变量标识
    private String variableOne;
    private String variableTwo;
    private String variableThree;
    private String variableFour;
    private String variableFive;
    private String messageWord;

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

    public String getMessageWord() {
        return messageWord;
    }

    public void setMessageWord(String messageWord) {
        this.messageWord = messageWord;
    }

    @Override
    public String toString() {
        return "SendSmsDTO{" +
                "entId='" + entId + '\'' +
                ", activityId='" + activityId + '\'' +
                ", variableOne='" + variableOne + '\'' +
                ", variableTwo='" + variableTwo + '\'' +
                ", variableThree='" + variableThree + '\'' +
                ", variableFour='" + variableFour + '\'' +
                ", variableFive='" + variableFive + '\'' +
                ", messageWord='" + messageWord + '\'' +
                '}';
    }
}
