package com.bdaim.smscenter.util;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(orders = { "header", "body" })
public class SmsSend {
	private SmsHeader header;
	private String body;

	public SmsHeader getHeader() {
		return header;
	}

	public void setHeader(SmsHeader header) {
		this.header = header;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}
}