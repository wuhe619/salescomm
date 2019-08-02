package com.bdaim.common.response;

import java.io.Serializable;
import java.util.Date;

/**
 * @description: 返回值对象
 * @auther: Chacker
 * @date: 2019/8/2 08:49
 */
public class ResponseBody<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 当前时间戳
     */
    private Date timeStamp = new Date();
    /**
     * status的内容与Http状态码内容相同，这个字段自动包含了错误信息的状态码
     * 客户端只需要解析HTTP相应的body部分，就可以获取跟这次出错相关的信息
     */
    private int status = 200;
    /**
     * 接口返回的数据对象
     */
    private T data;
    /**
     * 消息内容
     */
    private InfoMsg infoMsg;

    public Date getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(Date timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public InfoMsg getInfoMsg() {
        return infoMsg;
    }

    public void setInfoMsg(InfoMsg infoMsg) {
        this.infoMsg = infoMsg;
    }
}
