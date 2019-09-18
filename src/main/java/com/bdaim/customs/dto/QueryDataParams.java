package com.bdaim.customs.dto;

import java.sql.Timestamp;

/**
 * @author duanliying
 * @date 2019/9/18
 * @description es查询参数类
 */
public class QueryDataParams {
    /**
     * 主单：sz   分单：sf  税单：ss
     */
    private String queryType;
    private Integer mainId;
    private Integer stationId;
    /**
     * 报关单位
     */
    private String custName;
    /**
     * 主单号
     */
    private String billNo;
    /**
     * 导入开始时间
     */
    private Timestamp startTime;
    private Timestamp endTime;
    /**
     * 进港开始时间
     */
    private Timestamp arrivalStartTime;
    private Timestamp arrivalEndTime;
    /**
     * 提交记录 1未提交  2舱单已提交  3保单已提交 4舱单/报单已提交
     */
    private Integer submitLog;
    /**
     * 1含低价  2无低价
     */
    private Integer lowPriceProduct;
    /**
     * 1溢装 2 短装
     */
    private Integer type;
    private Integer pageNum;
    private Integer pageSize;
    private String overWarp;

    public String getQueryType() {
        return queryType;
    }

    public void setQueryType(String queryType) {
        this.queryType = queryType;
    }

    public String getOverWarp() {
        return overWarp;
    }

    public void setOverWarp(String overWarp) {
        this.overWarp = overWarp;
    }

    public Integer getMainId() {
        return mainId;
    }

    public void setMainId(Integer mainId) {
        this.mainId = mainId;
    }

    public Integer getStationId() {
        return stationId;
    }

    public void setStationId(Integer stationId) {
        this.stationId = stationId;
    }

    public String getCustName() {
        return custName;
    }

    public void setCustName(String custName) {
        this.custName = custName;
    }

    public String getBillNo() {
        return billNo;
    }

    public void setBillNo(String billNo) {
        this.billNo = billNo;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public Timestamp getArrivalStartTime() {
        return arrivalStartTime;
    }

    public void setArrivalStartTime(Timestamp arrivalStartTime) {
        this.arrivalStartTime = arrivalStartTime;
    }

    public Timestamp getArrivalEndTime() {
        return arrivalEndTime;
    }

    public void setArrivalEndTime(Timestamp arrivalEndTime) {
        this.arrivalEndTime = arrivalEndTime;
    }

    public Integer getSubmitLog() {
        return submitLog;
    }

    public void setSubmitLog(Integer submitLog) {
        this.submitLog = submitLog;
    }

    public Integer getLowPriceProduct() {
        return lowPriceProduct;
    }

    public void setLowPriceProduct(Integer lowPriceProduct) {
        this.lowPriceProduct = lowPriceProduct;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public Integer getPageNum() {
        return pageNum;
    }

    public void setPageNum(Integer pageNum) {
        this.pageNum = pageNum;
    }

    public Integer getPageSize() {
        return pageSize;
    }

    public void setPageSize(Integer pageSize) {
        this.pageSize = pageSize;
    }

    @Override
    public String toString() {
        return "QueryDataParams{" +
                "stationId=" + stationId +
                ", custName='" + custName + '\'' +
                ", billNo='" + billNo + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                ", arrivalStartTime=" + arrivalStartTime +
                ", arrivalEndTime=" + arrivalEndTime +
                ", submitLog=" + submitLog +
                ", lowPriceProduct=" + lowPriceProduct +
                ", type=" + type +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                ", overWarp='" + overWarp + '\'' +
                '}';
    }
}
