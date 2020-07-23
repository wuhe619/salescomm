package com.bdaim.batch.dto;

import java.util.List;

import com.bdaim.customer.account.dto.Fixentity;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/11/15 9:58
 */
public class FixInfo {

    private String batchname;

    private String repairMode;

    private String province;

    private String city;

    public String getProvince() {
        return province;
    }

    public void setProvince(String province) {
        this.province = province;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    private List<Fixentity> fixentity;

    public String getBatchname() {
        return batchname;
    }

    public void setBatchname(String batchname) {
        this.batchname = batchname;
    }

    public String getRepairMode() {
        return repairMode;
    }

    public void setRepairMode(String repairMode) {
        this.repairMode = repairMode;
    }

    public List<Fixentity> getFixentity() {
        return fixentity;
    }

    public void setFixentity(List<Fixentity> fixentity) {
        this.fixentity = fixentity;
    }
}
