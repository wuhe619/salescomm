package com.bdaim.emailcenter.service;

import com.bdaim.common.exception.TouchException;
import com.bdaim.emailcenter.dto.MailBean;

import javax.mail.internet.MimeMessage;

/**
 * 发送邮件service服务接口
 *
 */
public interface SendMailService {
	 public MimeMessage createMimeMessage(MailBean mailBean);
	 public void sendMail(MailBean mailBean);
	 public Object sendVerifyCode(String mail, String state) throws TouchException;
}
