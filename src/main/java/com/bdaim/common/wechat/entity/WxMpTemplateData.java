package com.bdaim.common.wechat.entity;

import java.io.Serializable;

/**
 * @author Daniel Qian
 */
public class WxMpTemplateData implements Serializable {
    private static final long serialVersionUID = 6301835292940277870L;

    private String name;
    private String value;
    private String color;

    public WxMpTemplateData() {
    }

    public WxMpTemplateData(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public WxMpTemplateData(String name, String value, String color) {
        this.name = name;
        this.value = value;
        this.color = color;
    }

    public static long getSerialVersionUID() {
        return serialVersionUID;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }
}