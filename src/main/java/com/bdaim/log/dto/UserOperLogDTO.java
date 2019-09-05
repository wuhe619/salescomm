package com.bdaim.log.dto;

import com.bdaim.log.entity.UserOperLog;

import java.sql.Timestamp;
import java.util.Map;

/**
 * 用户行为记录表
 *
 * @author chengning@salescomm.net
 * @date 2019/6/13
 * @description
 */
public class UserOperLogDTO {
    private int id;
    private String userId;
    private String ip;
    private String imei;
    private String mac;
    private String browser;
    private String client;
    private String fromChannel;
    private String activityId;
    private Timestamp createTime;
    private int eventType;
    private String objectCode;
    private String objectName;
    private String refer;
    private String productType;
    private String loginArea;
    private String loginClient;
    //产品属性
    private Map<String, Object> productProperty;

    public UserOperLogDTO() {
    }

    public UserOperLogDTO(UserOperLog m) {
        this.setId(m.getId());
        this.setUserId(String.valueOf(m.getUserId()));
        this.setIp(m.getIp());
        this.setImei(m.getImei());
        this.setMac(m.getMac());
        this.setClient(m.getClient());
        this.setFromChannel(m.getFromChannel());
        this.setActivityId(m.getActivityId());
        this.setCreateTime(m.getCreateTime());
        this.setEventType(m.getEventType());
        this.setObjectCode(m.getObjectCode());
        this.setRefer(m.getRefer());
        this.setBrowser(m.getBrowser());
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    public String getFromChannel() {
        return fromChannel;
    }

    public void setFromChannel(String fromChannel) {
        this.fromChannel = fromChannel;
    }

    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    public String getRefer() {
        return refer;
    }

    public void setRefer(String refer) {
        this.refer = refer;
    }

    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String browser) {
        this.browser = browser;
    }

    public String getObjectName() {
        return objectName;
    }

    public void setObjectName(String objectName) {
        this.objectName = objectName;
    }

    public String getProductType() {
        return productType;
    }

    public void setProductType(String productType) {
        this.productType = productType;
    }

    public String getLoginArea() {
        return loginArea;
    }

    public void setLoginArea(String loginArea) {
        this.loginArea = loginArea;
    }

    public String getLoginClient() {
        return loginClient;
    }

    public void setLoginClient(String loginClient) {
        this.loginClient = loginClient;
    }

    public Map<String, Object> getProductProperty() {
        return productProperty;
    }

    public void setProductProperty(Map<String, Object> productProperty) {
        this.productProperty = productProperty;
    }
}
