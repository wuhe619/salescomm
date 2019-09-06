package com.bdaim.emailcenter.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.mail.*;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.util.Properties;

public class MailUtil {
	private Session session; // 邮件会话对象
	private Properties props;
	private final static Log log = LogFactory.getLog(MailUtil.class);
	private static String MAIL_HOST = "";
	private static String MAIL_FROM = "";
	private static String USERNAME = "";
	private static String PASSWORD = "";
	private static MailUtil instance = new MailUtil();

	private MailUtil() {
		if (props == null)
			props = System.getProperties(); // 获得系统属性对象
		props.put("mail.smtp.host", MAIL_HOST); // 设置SMTP主机
		props.put("mail.smtp.auth", "true");
		try {
			session = Session.getDefaultInstance(props, null); // 获得邮件会话对象
		} catch (Exception e) {
			log.error("获取邮件会话对象时发生错误！" + e);
		}
	}

	public static MailUtil getInstance() {
		return instance;
	}

	public void sendout(String subject, String body, String sendTo) {
		MimeMessage mimeMsg = null;
		Multipart mp = null;
		try {
			try {
				mimeMsg = new MimeMessage(session); // 创建MIME邮件对象
				mp = new MimeMultipart();
			} catch (Exception e) {
				log.error("创建MIME邮件对象失败！" + e);
			}
			try {
				mimeMsg.setSubject(subject);
			} catch (Exception e) {
				log.error("设置邮件主题发生错误！");
			}
			try {
				BodyPart bp = new MimeBodyPart();
				bp.setContent("" + body, "text/html;charset=utf-8");
				mp.addBodyPart(bp);
			} catch (Exception e) {
				log.error("设置邮件正文时发生错误！" + e);
			}
			try {
				mimeMsg.setFrom(new InternetAddress(MAIL_FROM,"精准营销平台")); // 设置发信人
			} catch (Exception e) {
				log.error("设置发件人发生错误！");
			}
			try {
				mimeMsg.setRecipients(Message.RecipientType.TO,
						InternetAddress.parse(sendTo));
			} catch (Exception e) {
				log.error("设置发件人发生错误！" + e);
			}
			mimeMsg.setContent(mp);
			mimeMsg.saveChanges();
			Session mailSession = Session.getInstance(props, null);
			Transport transport = mailSession.getTransport("smtp");
			transport.connect((String) props.get("mail.smtp.host"), USERNAME,
					PASSWORD);
			transport.sendMessage(mimeMsg,
					mimeMsg.getRecipients(Message.RecipientType.TO));
			transport.close();
		} catch (Exception e) {
			log.error("邮件发送失败！" + e);
		}
	}

	public static void main(String[] args) {
		MailUtil mail = MailUtil.getInstance();
		mail.sendout("数据", "数据", "");
	}
}