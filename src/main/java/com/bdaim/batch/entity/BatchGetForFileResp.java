package com.bdaim.batch.entity;

import com.alibaba.fastjson.annotation.JSONType;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/9/10 14:58
 */
@JSONType(orders = { "code", "msg", "result"})
public class BatchGetForFileResp {
    private static final long serialVersionUID = -4821318907897139601L;

    private String code;
    private String msg;
    private String result;

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }


}
