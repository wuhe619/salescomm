package com.bdaim.log.entity;

import javax.persistence.*;
import java.sql.Timestamp;

/**
 * @author chengning@salescomm.net
 * @date 2019/7/1
 * @description
 */
@Entity
@Table(name = "t_customer_user_operlog", schema = "", catalog = "")
public class SuperDataOperLog {
    private int id;
    private Long userId;
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
    private String token;
    private String refer;
    private String listId;
    private String customerSeaId;
    private String customerGroupId;
    private String reason;
    private String remark;


    public SuperDataOperLog() {
    }

    public SuperDataOperLog(Long userId, String listId, String customerSeaId, String customerGroupId, int eventType, String objectCode) {
        this.userId = userId;
        this.listId = listId;
        this.customerSeaId = customerSeaId;
        this.customerGroupId = customerGroupId;
        this.eventType = eventType;
        this.objectCode = objectCode;
    }


    @Id
    @Column(name = "id")
    @GeneratedValue(strategy = GenerationType.AUTO)
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @Basic
    @Column(name = "user_id")
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Basic
    @Column(name = "ip")
    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    @Basic
    @Column(name = "imei")
    public String getImei() {
        return imei;
    }

    public void setImei(String imei) {
        this.imei = imei;
    }

    @Basic
    @Column(name = "mac")
    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    @Basic
    @Column(name = "browser")
    public String getBrowser() {
        return browser;
    }

    public void setBrowser(String brower) {
        this.browser = brower;
    }

    @Basic
    @Column(name = "client")
    public String getClient() {
        return client;
    }

    public void setClient(String client) {
        this.client = client;
    }

    @Basic
    @Column(name = "from_channel")
    public String getFromChannel() {
        return fromChannel;
    }

    public void setFromChannel(String fromChannel) {
        this.fromChannel = fromChannel;
    }

    @Basic
    @Column(name = "activity_id")
    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }

    @Basic
    @Column(name = "create_time")
    public Timestamp getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Timestamp createTime) {
        this.createTime = createTime;
    }

    @Basic
    @Column(name = "event_type")
    public int getEventType() {
        return eventType;
    }

    public void setEventType(int eventType) {
        this.eventType = eventType;
    }

    @Basic
    @Column(name = "object_code")
    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    @Basic
    @Column(name = "token")
    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    @Basic
    @Column(name = "refer")
    public String getRefer() {
        return refer;
    }

    public void setRefer(String refer) {
        this.refer = refer;
    }

    @Override
    public String toString() {
        return "UserOperLog{" +
                "id=" + id +
                ", userId=" + userId +
                ", ip='" + ip + '\'' +
                ", imei='" + imei + '\'' +
                ", mac='" + mac + '\'' +
                ", browser='" + browser + '\'' +
                ", client='" + client + '\'' +
                ", fromChannel='" + fromChannel + '\'' +
                ", activityId='" + activityId + '\'' +
                ", createTime=" + createTime +
                ", eventType=" + eventType +
                ", objectCode='" + objectCode + '\'' +
                ", token='" + token + '\'' +
                ", refer='" + refer + '\'' +
                '}';
    }

    @Basic
    @Column(name = "list_id")
    public String getListId() {
        return listId;
    }

    public void setListId(String listId) {
        this.listId = listId;
    }

    @Basic
    @Column(name = "customer_sea_id")
    public String getCustomerSeaId() {
        return customerSeaId;
    }

    public void setCustomerSeaId(String customerSeaId) {
        this.customerSeaId = customerSeaId;
    }

    @Basic
    @Column(name = "customer_group_id")
    public String getCustomerGroupId() {
        return customerGroupId;
    }

    public void setCustomerGroupId(String customerGroupId) {
        this.customerGroupId = customerGroupId;
    }

    @Basic
    @Column(name = "reason")
    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

}
