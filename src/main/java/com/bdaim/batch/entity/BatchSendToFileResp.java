package com.bdaim.batch.entity;

import com.alibaba.fastjson.annotation.JSONType;

/**
 * @author yanls@bdaim.com
 * @Description: TODO
 * @date 2018/9/10 14:58
 */
@JSONType(orders = { "msgId", "respCode"})
public class BatchSendToFileResp {
    private static final long serialVersionUID = -4821307247897139601L;

    private String status;
    private String errorCode;

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getErrorCode() {
        return errorCode;
    }

    public void setErrorCode(String errorCode) {
        this.errorCode = errorCode;
    }


}
