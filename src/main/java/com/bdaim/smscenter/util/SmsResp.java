package com.bdaim.smscenter.util;

import com.alibaba.fastjson.annotation.JSONType;

import java.io.Serializable;

@JSONType(orders = { "msgId", "respCode" })
public class SmsResp implements Serializable {
	private static final long serialVersionUID = -4821307267577139601L;
	private String msgId;
	private String respCode="0000";

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getRespCode() {
		return respCode;
	}

	public void setRespCode(String respCode) {
		this.respCode = respCode;
	}
}