package com.bdaim.common.response;

/**
 * @description: 接口返回结果的封装
 * @auther: Chacker
 * @date: 2019/7/31 15:00
 */
public class JsonResult {
    /**
     * 状态码
     */
    private ResultCode code;
    /**
     * 消息体
     */
    private String _message;
    /**
     * 数据对象
     */
    private Object data;

    public JsonResult() {
        this.setCode(ResultCode.SUCCESS);
        this.set_message("成功！");
    }

    public JsonResult(ResultCode code) {
        this.setCode(code);
        this.set_message(code.msg());
    }

    public JsonResult(ResultCode code, String message) {
        this.setCode(code);
        this.set_message(message);
    }

    public JsonResult(ResultCode code, String message, Object data) {

    }


    public ResultCode getCode() {
        return code;
    }

    public void setCode(ResultCode code) {
        this.code = code;
    }

    public String get_message() {
        return _message;
    }

    public void set_message(String _message) {
        this._message = _message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }
}
