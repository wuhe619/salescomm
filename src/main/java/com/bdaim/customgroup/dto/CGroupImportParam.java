package com.bdaim.customgroup.dto;

/**
 * @author chengning@salescomm.net
 * @date 2019/3/18
 * @description
 */
public class CGroupImportParam {

    private String md5Phone;
    private String superData;
    private String phone;
    private Integer status;

    public String getMd5Phone() {
        return md5Phone;
    }

    public void setMd5Phone(String md5Phone) {
        this.md5Phone = md5Phone;
    }

    public String getSuperData() {
        return superData;
    }

    public void setSuperData(String superData) {
        this.superData = superData;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return "CGroupImportParam{" +
                "md5Phone='" + md5Phone + '\'' +
                ", superData='" + superData + '\'' +
                ", phone='" + phone + '\'' +
                ", status=" + status +
                '}';
    }
}
