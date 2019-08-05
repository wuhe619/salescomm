package com.bdaim.common.response;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @description: 返回值对象
 * @auther: Chacker
 * @date: 2019/8/2 08:49
 */
public class ResponseInfo<T> implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 当前时间戳
     */
    private String timeStamp = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date());
    /**
     * status的内容与Http状态码内容相同，这个字段自动包含了错误信息的状态码
     * 客户端只需要解析HTTP相应的body部分，就可以获取跟这次出错相关的信息
     */
    private int code = 200;
    /**
     * 接口返回的数据对象
     */
    private T data;
    /**
     * 消息内容
     */
    private String message = "操作成功";

    public String getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(String timeStamp) {
        this.timeStamp = timeStamp;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
