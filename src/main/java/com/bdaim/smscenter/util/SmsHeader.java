package com.bdaim.smscenter.util;

import com.alibaba.fastjson.annotation.JSONType;

@JSONType(orders = { "from", "openId" })
public class SmsHeader {
	private String from;
	private String openId;

	public String getFrom() {
		return from;
	}

	public void setFrom(String from) {
		this.from = from;
	}

	public String getOpenId() {
		return openId;
	}

	public void setOpenId(String openId) {
		this.openId = openId;
	}
}