package com.bdaim.slxf.entity;

import java.util.List;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/11/15 9:58
 */
public class FixInfo {

    private String batchname;

    private String repairMode;

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
