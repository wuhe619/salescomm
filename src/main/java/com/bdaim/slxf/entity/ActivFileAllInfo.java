package com.bdaim.slxf.entity;

import java.util.List;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/9/13 19:15
 */
public class ActivFileAllInfo {
    private int count;
    private List<ActivFileAllDetail> resultList;
    private String code;
    private String msg;
    public void setCount(int count) {
        this.count = count;
    }
    public int getCount() {
        return count;
    }

    public void setResultList(List<ActivFileAllDetail> resultList) {
        this.resultList = resultList;
    }
    public List<ActivFileAllDetail> getResultList() {
        return resultList;
    }

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
}
