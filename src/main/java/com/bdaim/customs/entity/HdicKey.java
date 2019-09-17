package com.bdaim.customs.entity;

import java.io.Serializable;

public class HdicKey implements Serializable {

    private String type;
    private String code;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
