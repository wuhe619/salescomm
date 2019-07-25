package com.bdaim.auth.entity;

import javax.persistence.*;
import java.io.Serializable;

/** 用户手机验证码记录
 * @author chengning@salescomm.net
 * @date 2018/8/2
 * @description
 */
@Entity
@Table(name = "user_verification_code")
public class UserVerificationCode implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long id;
    @Column(name = "user_id")
    private String userId;
    @Column(name = "phone")
    private String phone;
    @Column(name = "type")
    private Integer type;
    @Column(name = "vc_code")
    private String vcVode;
    @Column(name = "send_time")
    private Long sendTime;
    @Column(name = "send_status")
    private Integer sendStatus;
    @Column(name = "send_num")
    private Integer sendNum;
    @Column(name = "effective_time_length")
    private Integer effectiveTimeLength;
    @Column(name = "create_time")
    private Long createTime;
    @Column(name = "status")
    private Integer status;

    public Long getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public Integer getType() {
        return type;
    }

    public void setType(Integer type) {
        this.type = type;
    }

    public String getVcVode() {
        return vcVode;
    }

    public void setVcVode(String vcVode) {
        this.vcVode = vcVode;
    }

    public Long getSendTime() {
        return sendTime;
    }

    public void setSendTime(Long sendTime) {
        this.sendTime = sendTime;
    }

    public Integer getSendStatus() {
        return sendStatus;
    }

    public void setSendStatus(Integer sendStatus) {
        this.sendStatus = sendStatus;
    }

    public Integer getEffectiveTimeLength() {
        return effectiveTimeLength;
    }

    public void setEffectiveTimeLength(Integer effectiveTimeLength) {
        this.effectiveTimeLength = effectiveTimeLength;
    }

    public Long getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Long createTime) {
        this.createTime = createTime;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getSendNum() {
        return sendNum;
    }

    public void setSendNum(Integer sendNum) {
        this.sendNum = sendNum;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Override
    public String toString() {
        return "UserVerificationCode{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", phone='" + phone + '\'' +
                ", type=" + type +
                ", vcVode='" + vcVode + '\'' +
                ", sendTime=" + sendTime +
                ", sendStatus=" + sendStatus +
                ", sendNum=" + sendNum +
                ", effectiveTimeLength=" + effectiveTimeLength +
                ", createTime=" + createTime +
                ", status=" + status +
                '}';
    }
}
