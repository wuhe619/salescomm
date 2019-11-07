package com.bdaim.customersea.dto;

import java.util.Map;

/**
 * 公海基础表
 *
 * @author chengning@salescomm.net
 * @date 2019/6/22
 * @description
 */
public class CustomerSeaESDTO {

    private String seaId;
    private String id;
    private String custId;
    /**
     * 意向等级 A-F
     */
    private String intentLevel;

    private Map<String, Object> superData;
    /**
     * 线索来源 1-购买 2-导入 3-添加 4-回收
     */
    private Integer dataSource;
    private String batchId;
    private String super_id;
    private String super_name;
    private String super_age;
    private String super_sex;
    private String super_telphone;
    private String super_phone;
    private String super_address_province_city;
    private String super_address_street;
    private String super_data;

    private String lastUserId;
    private String lastUserName;
    private String lastCallTime;
    private String lastCallResult;
    private Integer calledDuration;
    private String userName;
    private Integer status;
    private Integer callCount;
    private String createTime;
    private String email;
    private String profession;
    private String weChat;
    private String company;
    private String qq;
    /**
     * 跟进状态
     */
    private String followStatus;
    /**
     * 无效原因
     */
    private String invalidReason;

    private String regLocation;
    private String regCapital;
    private String regTime;
    private String regStatus;
    private Integer entPersonNum;


    public CustomerSeaESDTO(CustomSeaTouchInfoDTO dto) {
        this.seaId = dto.getCustomerSeaId();
        this.id = dto.getSuper_id();
        this.custId = dto.getCust_id();
        this.batchId = dto.getCust_group_id();

        this.super_id = dto.getSuper_id();
        this.super_name = dto.getSuper_name();
        this.super_age = dto.getSuper_age();
        this.super_sex = dto.getSuper_sex();
        this.super_phone = dto.getSuper_phone();
        this.super_telphone = dto.getSuper_telphone();
        this.super_address_province_city = dto.getSuper_address_province_city();
        this.super_address_street = dto.getSuper_address_street();
        this.superData = dto.getSuperData();
        this.email = dto.getEmail();
        this.profession = dto.getProfession();
        this.weChat = dto.getWeChat();
        this.company = dto.getCompany();
        this.qq = dto.getQq();
        this.followStatus = dto.getFollowStatus();
        this.invalidReason = dto.getInvalidReason();
    }

    public String getSeaId() {
        return seaId;
    }

    public void setSeaId(String seaId) {
        this.seaId = seaId;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCustId() {
        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    public String getIntentLevel() {
        return intentLevel;
    }

    public void setIntentLevel(String intentLevel) {
        this.intentLevel = intentLevel;
    }

    public Map<String, Object> getSuperData() {
        return superData;
    }

    public void setSuperData(Map<String, Object> superData) {
        this.superData = superData;
    }

    public Integer getDataSource() {
        return dataSource;
    }

    public void setDataSource(Integer dataSource) {
        this.dataSource = dataSource;
    }

    public String getBatchId() {
        return batchId;
    }

    public void setBatchId(String batchId) {
        this.batchId = batchId;
    }

    public String getSuper_id() {
        return super_id;
    }

    public void setSuper_id(String super_id) {
        this.super_id = super_id;
    }

    public String getSuper_name() {
        return super_name;
    }

    public void setSuper_name(String super_name) {
        this.super_name = super_name;
    }

    public String getSuper_age() {
        return super_age;
    }

    public void setSuper_age(String super_age) {
        this.super_age = super_age;
    }

    public String getSuper_sex() {
        return super_sex;
    }

    public void setSuper_sex(String super_sex) {
        this.super_sex = super_sex;
    }

    public String getSuper_telphone() {
        return super_telphone;
    }

    public void setSuper_telphone(String super_telphone) {
        this.super_telphone = super_telphone;
    }

    public String getSuper_phone() {
        return super_phone;
    }

    public void setSuper_phone(String super_phone) {
        this.super_phone = super_phone;
    }

    public String getSuper_address_province_city() {
        return super_address_province_city;
    }

    public void setSuper_address_province_city(String super_address_province_city) {
        this.super_address_province_city = super_address_province_city;
    }

    public String getSuper_address_street() {
        return super_address_street;
    }

    public void setSuper_address_street(String super_address_street) {
        this.super_address_street = super_address_street;
    }

    public String getSuper_data() {
        return super_data;
    }

    public void setSuper_data(String super_data) {
        this.super_data = super_data;
    }

    public String getLastUserId() {
        return lastUserId;
    }

    public void setLastUserId(String lastUserId) {
        this.lastUserId = lastUserId;
    }

    public String getLastUserName() {
        return lastUserName;
    }

    public void setLastUserName(String lastUserName) {
        this.lastUserName = lastUserName;
    }

    public String getLastCallTime() {
        return lastCallTime;
    }

    public void setLastCallTime(String lastCallTime) {
        this.lastCallTime = lastCallTime;
    }

    public String getLastCallResult() {
        return lastCallResult;
    }

    public void setLastCallResult(String lastCallResult) {
        this.lastCallResult = lastCallResult;
    }

    public Integer getCalledDuration() {
        return calledDuration;
    }

    public void setCalledDuration(Integer calledDuration) {
        this.calledDuration = calledDuration;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public Integer getCallCount() {
        return callCount;
    }

    public void setCallCount(Integer callCount) {
        this.callCount = callCount;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfession() {
        return profession;
    }

    public void setProfession(String profession) {
        this.profession = profession;
    }

    public String getWeChat() {
        return weChat;
    }

    public void setWeChat(String weChat) {
        this.weChat = weChat;
    }

    public String getCompany() {
        return company;
    }

    public void setCompany(String company) {
        this.company = company;
    }

    public String getQq() {
        return qq;
    }

    public void setQq(String qq) {
        this.qq = qq;
    }

    public String getFollowStatus() {
        return followStatus;
    }

    public void setFollowStatus(String followStatus) {
        this.followStatus = followStatus;
    }

    public String getInvalidReason() {
        return invalidReason;
    }

    public void setInvalidReason(String invalidReason) {
        this.invalidReason = invalidReason;
    }

    public String getRegLocation() {
        return regLocation;
    }

    public void setRegLocation(String regLocation) {
        this.regLocation = regLocation;
    }

    public String getRegCapital() {
        return regCapital;
    }

    public void setRegCapital(String regCapital) {
        this.regCapital = regCapital;
    }

    public String getRegTime() {
        return regTime;
    }

    public void setRegTime(String regTime) {
        this.regTime = regTime;
    }

    public String getRegStatus() {
        return regStatus;
    }

    public void setRegStatus(String regStatus) {
        this.regStatus = regStatus;
    }

    public Integer getEntPersonNum() {
        return entPersonNum;
    }

    public void setEntPersonNum(Integer entPersonNum) {
        this.entPersonNum = entPersonNum;
    }
}
