package com.bdaim.callcenter.dto;

import java.util.List;

public class SeatPropertyDTO {
    private String custId;
    //呼叫中心企业id
    private String callCenterId;
    private String adminAccount;
    private String adminPassword;
    private String apparentNumber;
    private List<SeatsInfo> seatsInfoList;

    public String getCallCenterId() {
        return callCenterId;
    }

    public void setCallCenterId(String callCenterId) {
        this.callCenterId = callCenterId;
    }

    public String getAdminAccount() {
        return adminAccount;
    }

    public void setAdminAccount(String adminAccount) {
        this.adminAccount = adminAccount;
    }

    public String getAdminPassword() {
        return adminPassword;
    }

    public void setAdminPassword(String adminPassword) {
        this.adminPassword = adminPassword;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public void setApparentNumber(String apparentNumber) {
        this.apparentNumber = apparentNumber;
    }

    public List<SeatsInfo> getSeatsInfoList() {
        return seatsInfoList;
    }

    public void setSeatsInfoList(List<SeatsInfo> seatsInfoList) {
        this.seatsInfoList = seatsInfoList;
    }

    public String getCustId() {

        return custId;
    }

    public void setCustId(String custId) {
        this.custId = custId;
    }

    @Override
    public String toString() {
        return "SeatPropertyDTO{" +
                "custId='" + custId + '\'' +
                ", callCenterId='" + callCenterId + '\'' +
                ", adminAccount='" + adminAccount + '\'' +
                ", adminPassword='" + adminPassword + '\'' +
                ", apperentNumber='" + apparentNumber + '\'' +
                ", seatsInfoList=" + seatsInfoList +
                '}';
    }
}
