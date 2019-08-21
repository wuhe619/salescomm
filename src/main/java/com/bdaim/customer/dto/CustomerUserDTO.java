package com.bdaim.customer.dto;


import com.bdaim.customer.entity.CustomerUserDO;

public class CustomerUserDTO {

    private String userPwdLevel;
    private String mobileNum;
    private String email;
    private String acctPwdLevel;
    private String id;
    private String account;
    private String realname;
    private String seatsAccount;

    public CustomerUserDTO() {
    }

    public CustomerUserDTO(long id, String realname, String account) {
        this.id = String.valueOf(id);
        this.realname = realname;
        this.account = account;
    }

    public CustomerUserDTO(String id, String realname, String account) {
        this.id = id;
        this.realname = realname;
        this.account = account;
    }

    public CustomerUserDTO(long id, String realname, String account, String seatsAccount) {
        this.id = String.valueOf(id);
        this.realname = realname;
        this.account = account;
        this.seatsAccount = seatsAccount;
    }

    public CustomerUserDTO(String id, String realname, String account, String seatsAccount) {
        this.id = id;
        this.realname = realname;
        this.account = account;
        this.seatsAccount = seatsAccount;
    }

    public CustomerUserDTO(long id, String realname) {
        this.id = String.valueOf(id);
        this.realname = realname;
    }

    public CustomerUserDTO(CustomerUserDO customerUser) {
        this.setId(String.valueOf(customerUser.getId()));
        this.setAccount(customerUser.getAccount());
        this.setRealname(customerUser.getRealname());
    }

    public String getUserPwdLevel() {
        return userPwdLevel;
    }

    public void setUserPwdLevel(String userPwdLevel) {
        this.userPwdLevel = userPwdLevel;
    }

    public String getMobileNum() {
        return mobileNum;
    }

    public void setMobileNum(String mobileNum) {
        this.mobileNum = mobileNum;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAcctPwdLevel() {
        return acctPwdLevel;
    }

    public void setAcctPwdLevel(String acctPwdLevel) {
        this.acctPwdLevel = acctPwdLevel;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getRealname() {
        return realname;
    }

    public void setRealname(String realname) {
        this.realname = realname;
    }

    public String getSeatsAccount() {
        return seatsAccount;
    }

    public void setSeatsAccount(String seatsAccount) {
        this.seatsAccount = seatsAccount;
    }

    @Override
    public String toString() {
        return "CustomerUserDTO{" +
                "userPwdLevel='" + userPwdLevel + '\'' +
                ", mobileNum='" + mobileNum + '\'' +
                ", email='" + email + '\'' +
                ", acctPwdLevel='" + acctPwdLevel + '\'' +
                ", id='" + id + '\'' +
                ", account='" + account + '\'' +
                ", realname='" + realname + '\'' +
                ", seatsAccount='" + seatsAccount + '\'' +
                '}';
    }
}
