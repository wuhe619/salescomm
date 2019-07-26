package com.bdaim.callcenter.dto;

/**
 * @author duanliying
 * @date 2019/1/14
 * 构造authorization和sign参数（讯众外呼）
 * @description
 */
public class SignAuthorizationResult {

    private String authorization;
    private String sign;

    public String getAuthorization() {
        return authorization;
    }

    public void setAuthorization(String authorization) {
        this.authorization = authorization;
    }

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    @Override
    public String toString() {
        return "SignAuthorizationResult{" +
                "authorization='" + authorization + '\'' +
                ", sign='" + sign + '\'' +
                '}';
    }
}
