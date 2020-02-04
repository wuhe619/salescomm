package com.bdaim.crm;

/**
 * 手机号实体类
 */
public class PhoneEntity {

    /**
     * 手机号
     */
    private String phone;
    /**
     * 录入时间
     */
    private long time;
    /**
     * 来源
     */
    private String source;
    /**
     * 来源网站(出处)
     */
    private String sourceWeb;

    public PhoneEntity() {
    }

    public PhoneEntity(String phone, long time, String source, String sourceWeb) {
        this.phone = phone;
        this.time = time;
        this.source = source;
        this.sourceWeb = sourceWeb;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public String getSourceWeb() {
        return sourceWeb;
    }

    public void setSourceWeb(String sourceWeb) {
        this.sourceWeb = sourceWeb;
    }
}
