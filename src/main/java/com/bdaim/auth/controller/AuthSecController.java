package com.bdaim.auth.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.service.impl.TokenServiceImpl;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.user.service.CustomerUserService;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/sec_auth")
public class AuthSecController extends BasicAction {

	public static final Logger logger = LoggerFactory.getLogger(AuthSecController.class);

	@Resource
	private CustomerUserDao customerUserdao;

	@Resource
	private SendSmsService sendSmsService;

	@Resource
	private CustomerUserService customerUserService;

	/**
	 * 二次核验
	 * @param uid
	 * @param code
	 * @param type
	 * @return
	 * @throws Exception
	 */
	@PostMapping("/checkin/{uid}")
	public ResponseInfo checkin(@PathVariable("uid") String uid, String code, String type,String verCodeStr) throws Exception{
		//todo 1.验证动态码
		ResponseInfoAssemble responseInfoAssemble = new ResponseInfoAssemble();
		if(StringUtil.isEmpty(code) || StringUtil.isEmpty(type)){
			return responseInfoAssemble.failure(0,"参数不正确");
		}
		CustomerUser user = customerUserService.getUserById(Long.valueOf(uid));
		if(user==null){
			return responseInfoAssemble.failure(0,"请确认用户参数正确");
		}
		CustomerUserPropertyDO mobile_num = customerUserdao.getProperty(uid, "mobile_num");
		if(mobile_num==null){
			return responseInfoAssemble.failure(0,"请确认手机号参数正确");
		}
		boolean success = sendSmsService.verificationCode(mobile_num.getPropertyValue(), NumberConvertUtil.parseInt(type), code) == 1 ? true : false;
		if (!success) {
			return responseInfoAssemble.failure(0,"验证错误");
		}else{
			logger.info(""+TokenServiceImpl.listTokens());
			logger.info("account: "+user.getAccount()+"; token:"+TokenServiceImpl.name2token.get(verCodeStr+"."+user.getAccount()));
			return responseInfoAssemble.success(TokenServiceImpl.name2token.get(verCodeStr+"."+user.getAccount()));
		}
	}


	/**
	 * 发送验证动态码
	 */
	@PostMapping("/sendSms/{uid}")
	public ResponseInfo sendsms(@PathVariable("uid") String uid) throws Exception {
		ResponseInfoAssemble responseInfoAssemble = new ResponseInfoAssemble();
		CustomerUserPropertyDO mobile_num = customerUserdao.getProperty(uid, "mobile_num");
		if (mobile_num == null || StringUtil.isEmpty(mobile_num.getPropertyValue())) {
			return responseInfoAssemble.failure(0,"未设置手机号，请联系管理员");
		}else{
			CustomerUser user = customerUserService.getUserById(Long.valueOf(uid));
			if(user==null){
				return responseInfoAssemble.failure(0,"请确认用户参数正确");
			}
			String phone = mobile_num.getPropertyValue();
			JSONObject r = (JSONObject) sendSmsService.sendSmsVcCodeByCommChinaAPI(phone, 15, user.getAccount());
			if(r.getInteger("code")==1){
				return responseInfoAssemble.success("验证码发送成功");
			}else{
				return responseInfoAssemble.failure(0,r.getString("message"));
			}
		}
	}



}
