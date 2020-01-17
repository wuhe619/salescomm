package com.bdaim.crm;

/**
 * 邮箱数据实体类
 */
public class EmailEntity {

    /**
     * 手机号
     */
    private String email;
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

    public EmailEntity() {
    }

    public EmailEntity(String email, long time, String source, String sourceWeb) {
        this.email = email;
        this.time = time;
        this.source = source;
        this.sourceWeb = sourceWeb;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
        return "EmailSource{" +
                "email='" + email + '\'' +
                ", time=" + time +
                ", source='" + source + '\'' +
                ", sourceWeb='" + sourceWeb + '\'' +
                '}';
    }
}
