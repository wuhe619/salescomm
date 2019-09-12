package com.bdaim.smscenter.util;

import com.alibaba.fastjson.annotation.JSONType;

import java.io.Serializable;

@JSONType(orders = { "msgId", "areaId", "mobile", "spCode", "content", "templateId", "cbUrl", "atTime" })
public class SmsSubmit implements Serializable {
	private static final long serialVersionUID = 2008078624387236695L;
	private String msgId=String.valueOf(System.currentTimeMillis());
	private String areaId="86";
	private String mobile;
	private String spCode;
	private String content;
	private String templateId;
	private String cbUrl;
	private String atTime; // yyyyMMddHHmmss 状态报告时间

	public String getMsgId() {
		return msgId;
	}

	public void setMsgId(String msgId) {
		this.msgId = msgId;
	}

	public String getAreaId() {
		return areaId;
	}

	public void setAreaId(String areaId) {
		this.areaId = areaId;
	}

	public String getMobile() {
		return mobile;
	}

	public void setMobile(String mobile) {
		this.mobile = mobile;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getTemplateId() {
		return templateId;
	}

	public void setTemplateId(String templateId) {
		this.templateId = templateId;
	}

	public String getCbUrl() {
		return cbUrl;
	}

	public void setCbUrl(String cbUrl) {
		this.cbUrl = cbUrl;
	}

	public String getSpCode() {
		return spCode;
	}

	public void setSpCode(String spCode) {
		this.spCode = spCode;
	}

	public String getAtTime() {
		return atTime;
	}

	public void setAtTime(String atTime) {
		this.atTime = atTime;
	}
}