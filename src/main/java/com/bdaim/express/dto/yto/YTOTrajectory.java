package com.bdaim.express.dto.yto;

import com.bdaim.express.dto.Param;

public class YTOTrajectory {
    private String sign;//请求签名
    private Param param;//请求参数 只有一个运单的参数
    private String timestamp;//请求时间戳
    private String app_key;//开放平台分配给客户的应用key
    private String user_id;//用户在开放平台注册时填写的客户标识
    private String method="yto.Marketing.WaybillTrace";//开放平台分配给客户的调用方法
    private String format="JSON";//xml/json两个类型选择
    private String version="1.01";//开放平台接口版本 建议版本使用该字段
    private String v="1.01";//version字段保持一致

    public String getSign() {
        return sign;
    }

    public void setSign(String sign) {
        this.sign = sign;
    }

    public Param getParam() {
        return param;
    }

    public void setParam(Param param) {
        this.param = param;
    }

    public String getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public String getApp_key() {
        return app_key;
    }

    public void setApp_key(String app_key) {
        this.app_key = app_key;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getV() {
        return v;
    }

    public void setV(String v) {
        this.v = v;
    }
}
