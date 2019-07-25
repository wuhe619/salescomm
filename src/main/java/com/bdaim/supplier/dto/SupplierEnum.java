package com.bdaim.supplier.dto;

/**
 * @description 失联修复供应商枚举类
 * @author:duanliying
 * @date: 2019/1/11 18:19
 */
public enum SupplierEnum {
    XZ("1", "讯众", "xz", 1, "xz_seat", "xz_call_id", "xz_apparent_number", "xz_activity_id", "xz_minute", ""),
    CUC("2", "联通", "cuc", 1, "cuc_seat", "cuc_call_id", "cuc_apparent_number", "cuc_activity_id", "cuc_minute", ""),
    CTC("3", "电信", "ctc", 1, "ctc_seat", "ctc_call_id", "ctc_apparent_number", "ctc_activity_id", "ctc_minute", ""),
    CMC("4", "移动", "cmc", 1, "cmc_seat", "cmc_call_id", "cmc_apparent_number", "cmc_activity_id", "cmc_minute", ""),
    JD("5", "京东", "jd", 2, "", "", "", "", "", "jd_config_id"),
    YD("6", "韵达", "yd", 2, "", "", "", "", "", "yd_config_id");
    private String supplierId;
    private String name;
    private String supplierName;
    private int supplierType;
    private String seatName;
    private String callId;
    private String apparentNumber;
    private String activityId;
    private String seatMinute;
    private String config;

    SupplierEnum(String supplierId, String name, String supplierName, int supplierType, String seatName, String callId, String apparentNumber, String activityId, String seatMinute, String config) {
        this.supplierId = supplierId;
        this.name = name;
        this.supplierName = supplierName;
        this.supplierType = supplierType;
        this.seatName = seatName;
        this.callId = callId;
        this.apparentNumber = apparentNumber;
        this.activityId = activityId;
        this.seatMinute = seatMinute;
        this.config = config;
    }

    public static SupplierEnum getCallIdPropertyName(String supplierId) {
        for (SupplierEnum s : SupplierEnum.values()) {
            if (s.getSupplierId().equals(supplierId)) {
                return s;
            }
        }
        return null;
    }

    public String getSeatMinute() {
        return seatMinute;
    }

    public String getName() {
        return name;
    }

    public String getSupplierId() {
        return supplierId;
    }

    public int getSupplierType() {
        return supplierType;
    }

    public String getCallId() {
        return callId;
    }

    public String getApparentNumber() {
        return apparentNumber;
    }

    public String getActivityId() {
        return activityId;
    }

    public String getConfig() {
        return config;
    }

    public String getSupplierName() {
        return supplierName;
    }


    public String getSeatName() {
        return seatName;
    }
}
