package com.bdaim.common.response;

import java.io.Serializable;

/**
 * @description: 返回值中的自定义消息体
 * @auther: Chacker
 * @date: 2019/8/2 08:54
 */
public class InfoMsg implements Serializable {
    private static final long serialVersionUID = 1L;
    /**
     * 自定义错误码。自定义错误码的长度和个数都可以自己定义，这样就突破了HTTP状态码的个数限制。
     * 例子中的错误码是40483，其中404代表了请求的资源不存在，而83则制定了这次出错，具体是哪一种资源不存在。
     * 默认为0，表示正常执行
     **/
    private String code = "200";
    /**
     * 用户可理解的错误信息，应当根据用户的locale信息返回对应语言的版本。这个信息意在返回给使用客户端的用户阅读，
     * 不应该包含任何技术信息。有了这个字段，客户端的开发者在出错时，能够展示恰当的信息给最终用户。
     * 默认为success
     **/
    private String message = "操作成功";
    /**
     * 该出错的详细技术信息，提供给客户端的开发者阅读。可以包含Exception的信息、StackTrace，或者其它有用的技术信息。
     **/
    private String developMessage = "";

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getDevelopMessage() {
        return developMessage;
    }

    public void setDevelopMessage(String developMessage) {
        this.developMessage = developMessage;
    }
}
