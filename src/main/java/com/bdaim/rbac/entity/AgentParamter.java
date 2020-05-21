package com.bdaim.rbac.entity;

import com.alibaba.fastjson.annotation.JSONField;

public class AgentParamter {


    private String UniqueID;
    private String BusinessType;
    private String AffirmState;

    @JSONField(name = "UniqueID")
    public String getUniqueID() {
        return UniqueID;
    }

    public void setUniqueID(String uniqueID) {
        UniqueID = uniqueID;
    }
    @JSONField(name = "BusinessType")
    public String getBusinessType() {
        return BusinessType;
    }

    public void setBusinessType(String businessType) {
        BusinessType = businessType;
    }
    @JSONField(name = "AffirmState")
    public String getAffirmState() {
        return AffirmState;
    }

    public void setAffirmState(String affirmState) {
        AffirmState = affirmState;
    }
}
