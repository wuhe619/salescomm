package com.bdaim.express.dto.yto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import javax.xml.bind.annotation.XmlType;

@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
@XmlType(propOrder = { "consigneeBranchCode", "packageCenterCode", "packageCenterName" ,"printKeyWord","shortAddress"})
public class DistributeInfo {
    private String consigneeBranchCode;
    private String packageCenterCode;
    private String packageCenterName;
    private String printKeyWord;
    private String shortAddress;


    public String getConsigneeBranchCode() {
        return consigneeBranchCode;
    }


    public void setConsigneeBranchCode(String consigneeBranchCode) {
        this.consigneeBranchCode = consigneeBranchCode;
    }

    public String getPackageCenterCode() {
        return packageCenterCode;
    }

    public void setPackageCenterCode(String packageCenterCode) {
        this.packageCenterCode = packageCenterCode;
    }

    public String getPackageCenterName() {
        return packageCenterName;
    }

    public void setPackageCenterName(String packageCenterName) {
        this.packageCenterName = packageCenterName;
    }

    public String getPrintKeyWord() {
        return printKeyWord;
    }

    public void setPrintKeyWord(String printKeyWord) {
        this.printKeyWord = printKeyWord;
    }

    public String getShortAddress() {
        return shortAddress;
    }

    public void setShortAddress(String shortAddress) {
        this.shortAddress = shortAddress;
    }

    @Override
    public String toString() {
        return "DistributeInfo{" +
                "consigneeBranchCode='" + consigneeBranchCode + '\'' +
                ", packageCenterCode='" + packageCenterCode + '\'' +
                ", packageCenterName='" + packageCenterName + '\'' +
                ", printKeyWord='" + printKeyWord + '\'' +
                ", shortAddress='" + shortAddress + '\'' +
                '}';
    }
}
