package com.bdaim.slxf.entity;

import java.math.BigInteger;
import java.sql.Timestamp;
/**
 * @author wangxx@bdaim.com
 * @Description:
 * @date 2019/01/02 10:21
 */

public class ExpressLog {
    private String touch_id;
    private String batchid;
    private String addressid;
    private String receivename;
    private String sendermessage;
    private Timestamp createtime;
    private Integer status;
    private String custid;
    private BigInteger user_id;
    private Integer amount;
    private Integer prodamount;
    private Integer sourceid;
    private String  firstdelivery;
    private String Lastsendtime;

    public String getTouch_id() {
        return touch_id;
    }

    public void setTouch_id(String touch_id) {
        this.touch_id = touch_id;
    }

    public String getFirstdelivery() {
        return firstdelivery;
    }

    public void setFirstdelivery(String firstdelivery) {
        this.firstdelivery = firstdelivery;
    }

    public String getLastsendtime() {
        return Lastsendtime;
    }

    public void setLastsendtime(String lastsendtime) {
        Lastsendtime = lastsendtime;
    }


    public String getBatchid() {
        return batchid;
    }

    public void setBatchid(String batchid) {
        this.batchid = batchid;
    }

    public String getAddressid() {
        return addressid;
    }

    public void setAddressid(String addressid) {
        this.addressid = addressid;
    }

    public String getReceivename() {
        return receivename;
    }

    public void setReceivename(String receivename) {
        this.receivename = receivename;
    }

    public String getSendermessage() {
        return sendermessage;
    }

    public void setSendermessage(String sendermessage) {
        this.sendermessage = sendermessage;
    }

    public Timestamp getCreatetime() {
        return createtime;
    }

    public void setCreatetime(Timestamp createtime) {
        this.createtime = createtime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getCustid() {
        return custid;
    }

    public void setCustid(String custid) {
        this.custid = custid;
    }

    public BigInteger getUser_id() {
        return user_id;
    }

    public void setUser_id(BigInteger user_id) {
        this.user_id = user_id;
    }

    public Integer getAmount() {
        return amount;
    }

    public void setAmount(Integer amount) {
        this.amount = amount;
    }

    public Integer getProdamount() {
        return prodamount;
    }

    public void setProdamount(Integer prodamount) {
        this.prodamount = prodamount;
    }

    public Integer getSourceid() {
        return sourceid;
    }

    public void setSourceid(Integer sourceid) {
        this.sourceid = sourceid;
    }

    public ExpressLog(String touch_id, String batchid, String addressid, String receivename, String sendermessage, Timestamp createtime, Integer status, String custid, BigInteger user_id, Integer amount, Integer prodamount, Integer sourceid, String firstdelivery, String lastsendtime) {
        this.touch_id = touch_id;
        this.batchid = batchid;
        this.addressid = addressid;
        this.receivename = receivename;
        this.sendermessage = sendermessage;
        this.createtime = createtime;
        this.status = status;
        this.custid = custid;
        this.user_id = user_id;
        this.amount = amount;
        this.prodamount = prodamount;
        this.sourceid = sourceid;
        this.firstdelivery = firstdelivery;
        Lastsendtime = lastsendtime;
    }

    public ExpressLog() {
    }
}
