package com.bdaim.batch.entity;

import javax.persistence.*;
import java.io.Serializable;
import java.sql.Timestamp;
import java.util.Objects;

/**
 * @author duanliying
 * @date 2018/9/6
 * @description 批次表明细
 */
@Entity
@Table(name = "nl_batch_detail", schema = "", catalog = "")
public class BatchDetail implements Serializable {
    //交易id
    private String touch_id;
    //客户id
    private String id;
    //批次id
    private String batchId;
    //企业自带id
    private String enterpriseId;
    //身份证号（唯一标识）
    private String idCard;
    //标签一
    private String labelOne;
    //标签二
    private String labelTwo;
    //标签三
    private String labelThree;
    //修复状态 1 - 修复成功  2- 修复失败
    private int status;
    //渠道/供应商 2-联通 3-电信 4-移动
    private int channel;
    //通话次数
    private Integer callNumber;
    //负责人id
    private String userId;
    //最后通话时间
    private Timestamp lastCallTime;
    //地址id
    //private String areaId;
    //是否分配负责人 0 -未分配  1-分配
    private int allocation;
    //資源id
    private Integer resourceId;
    //姓氏
    //private String lastName;
    //联通返回的数据id
    //private String dataId;
    //省份id
    private String provideId;

    //性别
    //private String sex;
    //手机号码
    private String phoneId;
    //快递地址
    private String site;
    //收件人姓名
    private String name;

    //活动ID
    private String activityId;

    //上传时间
    private Timestamp uploadTime;

    //修复时间

    private Timestamp fixTime;


    private String labelFour;
    private String labelFive;
    private String labelSix;

    @Basic
    @Column(name = "label_four")
    public String getLabelFour() {
        return labelFour;
    }

    public void setLabelFour(String labelFour) {
        this.labelFour = labelFour;
    }

    @Basic
    @Column(name = "label_five")
    public String getLabelFive() {
        return labelFive;
    }

    public void setLabelFive(String labelFive) {
        this.labelFive = labelFive;
    }
    @Basic
    @Column(name = "label_six")
    public String getLabelSix() {
        return labelSix;
    }

    public void setLabelSix(String labelSix) {
        this.labelSix = labelSix;
    }

    //标记信息
    private String remark;

    private String express_path;


    @Basic
    @Column(name = "touch_id")
    public String getTouch_id() {
        return touch_id;
    }

    public void setTouch_id(String touch_id) {
        this.touch_id = touch_id;
    }

    @Basic
    @Column(name = "express_path")
    public String getExpress_path() {
        return express_path;
    }

    public void setExpress_path(String express_path) {
        this.express_path = express_path;
    }


    @Basic
    @Column(name = "remark")
    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }

    @Basic
    @Column(name = "resource_id")
    public Integer getResourceId() {
        return resourceId;
    }

    public void setResourceId(Integer resourceId) {
        this.resourceId = resourceId;
    }


    @Basic
    @Column(name = "upload_time")
    public Timestamp getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Timestamp uploadTime) {
        this.uploadTime = uploadTime;
    }

    @Basic
    @Column(name = "fix_time")
    public Timestamp getFixTime() {
        return fixTime;
    }

    public void setFixTime(Timestamp fixTime) {
        this.fixTime = fixTime;
    }


    @Basic
    @Column(name = "activity_id")
    public String getActivityId() {
        return activityId;
    }

    public void setActivityId(String activityId) {
        this.activityId = activityId;
    }


    /*@Basic
    @Column(name = "last_name")
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    @Basic
    @Column(name = "data_id")
    public String getDataId() {
        return dataId;
    }

    public void setDataId(String dataId) {
        this.dataId = dataId;
    }*/

    @Basic
    @Column(name = "provide_id")
    public String getProvideId() {
        return provideId;
    }

    public void setProvideId(String provideId) {
        this.provideId = provideId;
    }

    /*@Basic
    @Column(name = "area_id")
    public String getAreaId() {
        return areaId;
    }


    public void setAreaId(String areaId) {
        this.areaId = areaId;
    }*/
    @Basic
    @Column(name = "id")
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Basic
    @Column(name = "batch_id")
    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    @Id
    @Column(name = "enterprise_id")
    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    @Basic
    @Column(name = "id_card")
    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    @Basic
    @Column(name = "label_one")
    public String getLabelOne() {
        return labelOne;
    }

    public void setLabelOne(String labelOne) {
        this.labelOne = labelOne;
    }

    @Basic
    @Column(name = "last_call_time")
    public Timestamp getLastCallTime() {
        return lastCallTime;
    }

    public void setLastCallTime(Timestamp lastCallTime) {
        this.lastCallTime = lastCallTime;
    }

    @Basic
    @Column(name = "label_two")
    public String getLabelTwo() {
        return labelTwo;
    }

    public void setLabelTwo(String labelTwo) {
        this.labelTwo = labelTwo;
    }

    @Basic
    @Column(name = "label_three")
    public String getLabelThree() {
        return labelThree;
    }

    public void setLabelThree(String labelThree) {
        this.labelThree = labelThree;
    }

    @Basic
    @Column(name = "status")
    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    @Basic
    @Column(name = "channel")
    public int getChannel() {
        return channel;
    }

    public void setChannel(int channel) {
        this.channel = channel;
    }


    @Basic
    @Column(name = "allocation")
    public int getAllocation() {
        return allocation;
    }

    public void setAllocation(int allocation) {
        this.allocation = allocation;
    }

    @Basic
    @Column(name = "call_number")
    public Integer getCallNumber() {
        return callNumber;
    }

    public void setCallNumber(Integer callNumber) {
        this.callNumber = callNumber;
    }

    @Basic
    @Column(name = "user_id")
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    /*@Basic
    @Column(name = "sex")
    public String getSex() {
        return sex;
    }
    public void setSex(String sex) {
        this.sex = sex;
    }

    @Basic
    @Column(name = "phonenum")
    public String getPhoneNum() {
        return phoneNum;
    }
    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }*/
    @Basic
    @Column(name = "phoneid")
    public String getPhoneId() {
        return phoneId;
    }

    public void setPhoneId(String phoneId) {
        this.phoneId = phoneId;
    }

    @Basic
    @Column(name = "site")
    public String getSite() {
        return site;
    }

    public void setSite(String site) {
        this.site = site;
    }

    @Basic
    @Column(name = "name")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BatchDetail that = (BatchDetail) o;
        return id == that.id &&
                batchId == that.batchId &&
                status == that.status &&
                channel == that.channel &&
                allocation == that.allocation &&
                callNumber == that.callNumber &&
                userId == that.userId &&
                Objects.equals(enterpriseId, that.enterpriseId) &&
                Objects.equals(idCard, that.idCard) &&
                Objects.equals(labelOne, that.labelOne) &&
                Objects.equals(labelTwo, that.labelTwo) &&
                Objects.equals(labelThree, that.labelThree) &&
                Objects.equals(lastCallTime, that.lastCallTime) &&
                Objects.equals(phoneId, that.phoneId) &&
                Objects.equals(site, that.site) &&
                Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(id, batchId, enterpriseId, idCard, labelOne, labelTwo, labelThree, status, channel, allocation, callNumber, userId, lastCallTime, phoneId, site, name);
    }






    /*  @Override
    public String toString() {
        return "BatchDetail{" +
                "id=" + id +
                ", batchId=" + batchId +
                ", enterpriseId='" + enterpriseId + '\'' +
                ", idCard='" + idCard + '\'' +
                ", labelOne='" + labelOne + '\'' +
                ", labelTwo='" + labelTwo + '\'' +
                ", labelThree='" + labelThree + '\'' +
                ", status=" + status +
                ", channel=" + channel +
                ", allocation=" + allocation +
                ", callNumber=" + callNumber +
                ", userId=" + userId +
                ", lastCallTime=" + lastCallTime+
                '}';
    }*/


    @Override
    public String toString() {
        return "BatchDetail{" +
                "touch_id='" + touch_id + '\'' +
                ", id='" + id + '\'' +
                ", batchId='" + batchId + '\'' +
                ", enterpriseId='" + enterpriseId + '\'' +
                ", idCard='" + idCard + '\'' +
                ", labelOne='" + labelOne + '\'' +
                ", labelTwo='" + labelTwo + '\'' +
                ", labelThree='" + labelThree + '\'' +
                ", status=" + status +
                ", channel=" + channel +
                ", callNumber=" + callNumber +
                ", userId='" + userId + '\'' +
                ", lastCallTime=" + lastCallTime +
                ", allocation=" + allocation +
                ", provideId='" + provideId + '\'' +
                ", phoneId='" + phoneId + '\'' +
                ", site='" + site + '\'' +
                ", name='" + name + '\'' +
                ", activityId='" + activityId + '\'' +
                ", uploadTime=" + uploadTime +
                ", fixTime=" + fixTime +
                ", remark='" + remark + '\'' +
                ", Express_content='" + express_path + '\'' +
                '}';
    }
}
