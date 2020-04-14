package com.bdaim.crm.ent.entity;

/**
 * 固话实体类
 */
public class TelPhoneEntity {

    /**
     * 手机号
     */
    private String tel;
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

    public TelPhoneEntity() {
    }

    public TelPhoneEntity(String tel, long time, String source, String sourceWeb) {
        this.tel = tel;
        this.time = time;
        this.source = source;
        this.sourceWeb = sourceWeb;
    }

    public String getTel() {
        return tel;
    }

    public void setTel(String tel) {
        this.tel = tel;
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

    @Override
    public String toString() {
        return "TelPhoneSource{" +
                "tel='" + tel + '\'' +
                ", time=" + time +
                ", source='" + source + '\'' +
                ", sourceWeb='" + sourceWeb + '\'' +
                '}';
    }
}
