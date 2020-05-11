package com.bdaim.rbac.dto;

import com.alibaba.excel.annotation.ExcelProperty;
import com.alibaba.excel.metadata.BaseRowModel;

import java.math.BigDecimal;

public class AgentDTO extends BaseRowModel {

    private String custId;//企业id

    private String userId;//用户id

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }
    @ExcelProperty(value="企业名称",index = 1)
    private String customName;//企业名称
    @ExcelProperty(value="企业账号",index = 0)
    private String customAcocunt;//企业账户
    @ExcelProperty(value="账期",index = 2)
    private String statTime;//账期
    @ExcelProperty(value="佣金率",index = 3)
    private String commision;//佣金比例

    private String commisionAmount;//佣金金额;
    @ExcelProperty(value="数据佣金金额",index = 4)
    private BigDecimal dataAmcount;//数据佣金金额
    @ExcelProperty(value="线路佣金金额",index = 5)
    private BigDecimal callAmcount;//线路佣金金额
    @ExcelProperty(value="短信佣金金额",index = 6)
    private BigDecimal messageAmcount;//短信佣金金额

    private String statTimeStart;//查询条件中的日期起始时间

    public BigDecimal getDataAmcount() {
        return dataAmcount;
    }

    public void setDataAmcount(BigDecimal dataAmcount) {
        this.dataAmcount = dataAmcount;
    }

    public BigDecimal getCallAmcount() {
        return callAmcount;
    }

    public void setCallAmcount(BigDecimal callAmcount) {
        this.callAmcount = callAmcount;
    }

    public BigDecimal getMessageAmcount() {
        return messageAmcount;
    }

    public void setMessageAmcount(BigDecimal messageAmcount) {
        this.messageAmcount = messageAmcount;
    }

    public String getStatTimeStart() {
        return statTimeStart;
    }

    public void setStatTimeStart(String statTimeStart) {
        this.statTimeStart = statTimeStart;
    }

    public String getStatTimeEnd() {
        return statTimeEnd;
    }

    public void setStatTimeEnd(String statTimeEnd) {
        this.statTimeEnd = statTimeEnd;
    }

    private String statTimeEnd;//查询条件中的日期起始时间

    public String getCustomName() {
        return customName;
    }

    public void setCustomName(String customName) {
        this.customName = customName;
    }

    public String getCustomAcocunt() {
        return customAcocunt;
    }

    public void setCustomAcocunt(String customAcocunt) {
        this.customAcocunt = customAcocunt;
    }

    public String getStatTime() {
        return statTime;
    }

    public void setStatTime(String statTime) {
        this.statTime = statTime;
    }

    public String getCommision() {
        return commision;
    }

    public void setCommision(String commision) {
        this.commision = commision;
    }

    public String getCommisionAmount() {
        return commisionAmount;
    }

    public void setCommisionAmount(String commisionAmount) {
        this.commisionAmount = commisionAmount;
    }
}
