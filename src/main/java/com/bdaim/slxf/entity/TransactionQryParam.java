package com.bdaim.slxf.entity;

import java.io.Serializable;

/**
 * Created by Mr.YinXin on 2017/2/28.
 */
public class TransactionQryParam implements Serializable{
    private String userName;
    private int type;
    private String startTime;
    private String endTime;
    private String transactionId;
    private int pageNum;
    private int pageSize;

    private int tradeItem;

    public int getTradeItem() {
        return tradeItem;
    }

    public void setTradeItem(int tradeItem) {
        this.tradeItem = tradeItem;
    }

    public String getTransactionId() {
        return transactionId;
    }

    public void setTransactionId(String transactionId) {
        this.transactionId = transactionId;
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int getPageNum() {
        return pageNum;
    }

    public void setPageNum(int pageNum) {
        this.pageNum = pageNum;
    }

    public int getPageSize() {
        return pageSize;
    }

    public void setPageSize(int pageSize) {
        this.pageSize = pageSize;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}
