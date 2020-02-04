package com.bdaim.util;

/**
 * 验证码实体类
 *
 * @author Chacker
 */
public class VerifyCode {
    private String verifyCode;
    private String uuid;
    private long verifyTime = 0L;

    public String getVerifyCode() {
        return verifyCode;
    }

    public void setVerifyCode(String verifyCode) {
        this.verifyCode = verifyCode;
    }

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public long getVerifyTime() {
        return verifyTime;
    }

    public void setVerifyTime(long verifyTime) {
        this.verifyTime = verifyTime;
    }

    @Override
    public String toString() {
        return "VerifyCode{" +
                "verifyCode='" + verifyCode + '\'' +
                ", uuid='" + uuid + '\'' +
                ", verifyTime=" + verifyTime +
                '}';
    }
}
