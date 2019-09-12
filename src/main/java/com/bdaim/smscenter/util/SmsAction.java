package com.bdaim.smscenter.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * 
 * @author Created by lanxq
 * @Date 2017年3月7日
 */
public class SmsAction {
	private static Log logger = LogFactory.getLog(SmsAction.class);
	private static String openid = "D2763392B08E4EE5A97EDC460BFF9E5B";
	private static String key = "E8EF5A19871F4224";
	private static String iv = "D90A3B29F94C4E22";
	private static String url = "https://cls.sms.sooware.com/csms/sms/send.do";
	private static String ztwj = "ztwj";

	/**
	 * 
	 * @param templateId
	 *            短信模板的id{【精准营销平台】：10287}
	 * @param mobile
	 *            手机号
	 * @param content
	 *            短信内容
	 * @return respCode
	 * 
	 * 			1.5.1 服务级错误码参照 错误码 说明 
	 *           2001 不允许发送的手机号码 
	 *           2002 运营商地区维护，暂不能发送 
	 *           2003 错误的手机号码 
	 *           2004 不允许发送的内容 
	 *           2005 当前账户可用余额不足
	 * 
	 *         1.5.2 系统级错误码参照 错误码 说明
	 *          1001 错误的key 
	 *          1002 错误的openid 
	 *          1003 未知的请求源 
	 *          1004 被禁止的请求源 
	 *          1005 系统内部异常 
	 *          1006 接口维护/停用 
	 *          1007 系统错误 
	 *          1008 系统繁忙
	 * 
	 */
	public String send(String templateId, String mobile, String content) {
		JSONObject json = new JSONObject();
		SmsSubmit smsSubmit = new SmsSubmit();
		smsSubmit.setTemplateId(templateId);
		smsSubmit.setMobile(mobile);
		smsSubmit.setContent(content);
		SmsResp smsResp = new SmsResp();
		// msgId string 短信编号，商户生成的唯一标识，建议使用uuid。
		smsResp.setMsgId(smsSubmit.getMsgId());
		String respCode = "1007";
		try {
			SmsHeader smsHeader = new SmsHeader();
			smsHeader.setFrom(ztwj);
			smsHeader.setOpenId(openid);
			SmsSend smsSend = new SmsSend();
			smsSend.setHeader(smsHeader);
			String value = JSON.toJSONString(smsSubmit);
			smsSend.setBody(AES128CBC.encrypt(value, key, iv));
			logger.info("send header={},body={}" + "，" + JSON.toJSONString(smsHeader) + "，" + value);
			String result = HttpUtils.httpsPost(url, JSON.toJSONString(smsSend));
			logger.info("result= " + "，" + result);
			if (StringUtils.isNotBlank(result)) {
				SmsResp resp = (SmsResp) JSON.parseObject(result, SmsResp.class);
				if (resp != null)
					respCode = resp.getRespCode();
			}
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
		smsResp.setRespCode(respCode);
		json.put("SmsResp", smsResp);
		return json.toJSONString();
	}

	public static void main(String[] args) {
		SmsAction demo = new SmsAction();
		System.out.println(demo.send("10287", "18813135134", "您正在修改/设置支付密码，验证码：哈哈这不是验证码6666，3分钟内有效，请确保是您本人在操作！"));
		;
	}
}
