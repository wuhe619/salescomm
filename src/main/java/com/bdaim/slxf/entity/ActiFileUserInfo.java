package com.bdaim.slxf.entity;

import java.util.List;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/9/14 9:16
 */
public class ActiFileUserInfo {
    private String code;
    private String msg;
    private List<ActiFileUserInfoDetail> resultList;
    public void setCode(String code) {
        this.code = code;
    }
    public String getCode() {
        return code;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }
    public String getMsg() {
        return msg;
    }

    public void setResultList(List<ActiFileUserInfoDetail> resultList) {
        this.resultList = resultList;
    }
    public List<ActiFileUserInfoDetail> getResultList() {
        return resultList;
    }
}
