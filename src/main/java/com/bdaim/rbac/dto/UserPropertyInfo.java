package com.bdaim.rbac.dto;

import java.util.Map;

/**
 * @author duanliying
 * @date 2018/9/21
 * @description
 */
public class UserPropertyInfo {

    private Integer id;
    //用户id
    private String userId;
    //属性Key
    private String propertyName;
    //属性值（坐席账号+密码+分机号）
    private Map<String, String> propertyValue;
    //创建时间
    private String createTime;
    //渠道 1-移动  2-联通  3- 电信
    private Integer channel;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public Map<String, String> getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(Map<String, String> propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    public Integer getChannel() {
        return channel;
    }

    public void setChannel(Integer channel) {
        this.channel = channel;
    }

    @Override
    public String toString() {
        return "UserPropertyInfo{" +
                "id=" + id +
                ", userId='" + userId + '\'' +
                ", propertyName='" + propertyName + '\'' +
                ", propertyValue=" + propertyValue +
                ", createTime='" + createTime + '\'' +
                ", channel=" + channel +
                '}';
    }
}
