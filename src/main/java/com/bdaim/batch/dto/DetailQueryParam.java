package com.bdaim.batch.dto;

import java.util.List;

/**
 * @author duanliying
 * @date 2018/9/6
 * @description 用于获取查询批次下的客户信息
 */
public class DetailQueryParam {
    /**
     * 客户id
     */
    private String id;
    /**
     * 批次d
     */
    private String batchId;
    /**
     * 企业自带id
     */
    private String enterpriseId;
    /**
     * 唯一标识（身份证号码）
     */
    private String idCard;
    /**
     * 标签一
     */
    private String labelOne;
    /**
     * 标签二
     */
    private String labelTwo;
    /**
     * 标签三
     */
    private String labelThree;
    /**
     * 状态
     */
    private Integer status;
    /**
     * 负责人
     */
    private String userId;
    /**
     * 是否分配了负责人
     */
    private int allocation;
    /**
     * 姓名
     */
    private String realname;
    /**
     * 年龄
     */
    private Integer age;
    /**
     * 性别
     */
    private String sex;
    /**
     * 手机号码
     */
    private String phoneNum;
    /**
     * 电话号码
     */
    private String telephone;
    /**
     * 地址
     */
    private String address;
    /**
     * 通话备注
     */
    private String remarks;
    /**
     * 通话备注
     */
    List<String> lableIds;
    private Integer pageNum;
    private Integer pageSize;


    public List<String> getLableIds() {
        return lableIds;
    }

    public void setLableIds(List<String> lableIds) {
        this.lableIds = lableIds;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public String getIdCard() {
        return idCard;
    }

    public void setIdCard(String idCard) {
        this.idCard = idCard;
    }

    public String getLabelOne() {
        return labelOne;
    }

    public void setLabelOne(String labelOne) {
        this.labelOne = labelOne;
    }

    public String getLabelTwo() {
        return labelTwo;
    }

    public void setLabelTwo(String labelTwo) {
        this.labelTwo = labelTwo;
    }

    public String getLabelThree() {
        return labelThree;
    }

    public void setLabelThree(String labelThree) {
        this.labelThree = labelThree;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public int getAllocation() {
        return allocation;
    }

    public void setAllocation(int allocation) {
        this.allocation = allocation;
    }


    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public String getPhoneNum() {
        return phoneNum;
    }

    public void setPhoneNum(String phoneNum) {
        this.phoneNum = phoneNum;
    }

    public String getTelephone() {
        return telephone;
    }

    public void setTelephone(String telephone) {
        this.telephone = telephone;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRemarks() {
        return remarks;
    }

    public void setRemarks(String remarks) {
        this.remarks = remarks;
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

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    @Override
    public String toString() {
        return "DetailQueryParam{" +
                "id='" + id + '\'' +
                ", batchId='" + batchId + '\'' +
                ", enterpriseId='" + enterpriseId + '\'' +
                ", idCard='" + idCard + '\'' +
                ", labelOne='" + labelOne + '\'' +
                ", labelTwo='" + labelTwo + '\'' +
                ", labelThree='" + labelThree + '\'' +
                ", status=" + status +
                ", userId='" + userId + '\'' +
                ", allocation=" + allocation +
                ", age=" + age +
                ", sex='" + sex + '\'' +
                ", phoneNum='" + phoneNum + '\'' +
                ", telephone='" + telephone + '\'' +
                ", address='" + address + '\'' +
                ", remarks='" + remarks + '\'' +
                ", lableIds=" + lableIds +
                ", pageNum=" + pageNum +
                ", pageSize=" + pageSize +
                '}';
    }
}
