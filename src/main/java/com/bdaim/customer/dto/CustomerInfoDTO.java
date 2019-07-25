package com.bdaim.customer.dto;

import java.io.Serializable;

/**失联人员详细信息
 * @author duanliying
 * @date 2018/9/25
 * @description
 */
public class CustomerInfoDTO implements Serializable {
    private String superId;
    private String superName;
    private String superAge;
    private String superSex;
    private String superTelphone;
    private String superPhone;
    private String superAddressProvinceCity;
    private String superAddressStreet;
    private String batchid;

    public String getSuperId() {
        return superId;
    }

    public void setSuperId(String superId) {
        this.superId = superId;
    }

    public String getBatchid() {
        return batchid;
    }

    public void setBatchid(String batchid) {
        this.batchid = batchid;
    }

    public String getSuperName() {
        return superName;
    }

    public void setSuperName(String superName) {
        this.superName = superName;
    }

    public String getSuperAge() {
        return superAge;
    }

    public void setSuperAge(String superAge) {
        this.superAge = superAge;
    }

    public String getSuperSex() {
        return superSex;
    }

    public void setSuperSex(String superSex) {
        this.superSex = superSex;
    }

    public String getSuperTelphone() {
        return superTelphone;
    }

    public void setSuperTelphone(String superTelphone) {
        this.superTelphone = superTelphone;
    }

    public String getSuperPhone() {
        return superPhone;
    }

    public void setSuperPhone(String superPhone) {
        this.superPhone = superPhone;
    }

    public String getSuperAddressProvinceCity() {
        return superAddressProvinceCity;
    }

    public void setSuperAddressProvinceCity(String superAddressProvinceCity) {
        this.superAddressProvinceCity = superAddressProvinceCity;
    }

    public String getSuperAddressStreet() {
        return superAddressStreet;
    }

    public void setSuperAddressStreet(String superAddressStreet) {
        this.superAddressStreet = superAddressStreet;
    }

    @Override
    public String toString() {
        return "CustomerInfoDTO{" +
                "superName='" + superName + '\'' +
                ", superAge='" + superAge + '\'' +
                ", superSex='" + superSex + '\'' +
                ", superTelphone='" + superTelphone + '\'' +
                ", superPhone='" + superPhone + '\'' +
                ", superAddressProvinceCity='" + superAddressProvinceCity + '\'' +
                ", superAddressStreet='" + superAddressStreet + '\'' +
                '}';
    }
}
