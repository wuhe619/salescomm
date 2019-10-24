package com.bdaim.emailcenter.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.emailcenter.dto.MailBean;
import com.bdaim.emailcenter.service.SendMailService;
import com.bdaim.emailcenter.util.MailUtil;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.util.IDHelper;

import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 发送邮件Action请求 2017/2/21
 * 
 * 
 *
 */
@Controller
@RequestMapping("/mail")
public class SendEmailAction extends BasicAction {
	@Resource
	private SendMailService sendMailService;
	// 为了获取邮箱
	@Resource
	private CustomGroupService customGroupService;

	@Resource
	private MarketResourceService marketResourceService;

	/*
	 * 短信信息编辑
	 */
	@RequestMapping(value = "/sendEmail", method = RequestMethod.POST)
	@ResponseBody
	@CacheAnnotation
	public Object sendEmail(String phone, String state, String username) {
		// ApplicationContext ac = new
		// ClassPathXmlApplicationContext("spring-mail.xml");
		MailBean mailBean = new MailBean();
		mailBean.setFrom("");
		mailBean.setFromName("这是发件人的名字");
		mailBean.setSubject("主题是为了测试");
		mailBean.setToEmails(new String[] { "" });
		mailBean.setContext("<a href=''>邮件内容点击进入公司首页</a>");
		sendMailService.sendMail(mailBean);
		return null;
	}

	@RequestMapping(value = "/sendVerifyCode", method = RequestMethod.POST)
	@ResponseBody
	@CacheAnnotation
	public Object sendVerifyCodeByMail(@RequestBody JSONObject request) throws TouchException {
		String mailTo = request.getString("mail");
		String state = request.getString("state");
		return JSONObject.toJSON(sendMailService.sendVerifyCode(mailTo, state));

	}

	@RequestMapping(value = "/sendEmailbyUser", method = RequestMethod.POST)
	@ResponseBody
	@CacheAnnotation
	public String sendEmailbyUser(String type, String templateId, String superidlist,String customer_group_id,String id) {
		Map<String, Object> resultMap = new HashMap<>();
		JSONObject json = new JSONObject();
		LoginUser lu = opUser();
        
		String cust_id = lu.getCustId();
		Long userId = lu.getId();
		
//		 String cust_id = "1704151033470003";
//		Long userId = Long.parseLong("17041510334700005");
		// 根据模版Id查找模版主题和内容
		String emailContent = marketResourceService.getSmsEmailTemplate(type, templateId, opUser());
		JSONObject jsonEmail = JSONObject.parseObject(emailContent);
		String jsonData = jsonEmail.getString("data");
		JSONObject jsondate = JSONObject.parseObject(jsonData);
		JSONArray templateList = jsondate.getJSONArray("templateList");
		JSONObject emailList = (JSONObject) templateList.get(0);
		String title = emailList.getString("title");
		String content = emailList.getString("mould_content");
	
		String code = "1";
		String message = "调用为成功";
		String superid = "";
		JSONObject jsonsuperid = JSONObject.parseObject(superidlist);
		//全部发送邮件
		com.alibaba.fastjson.JSONArray supidList=null;
		List<Map<String, Object>>  allList=null;
		if(null!=customer_group_id&&!"".equals(customer_group_id)){
			String user_id=opUser().getId().toString();
			allList=marketResourceService.getCustomerList(customer_group_id,cust_id,user_id, lu.getUserType(), id);
		}else{
			supidList = jsonsuperid.getJSONArray("data");
		}
		String batch_number = IDHelper.getTransactionId().toString();
		//计算发送成功的数量
		Integer quantity = 0;
		int size=0;
		if(null!=allList){
			size=allList.size();
		}else{
			size=supidList.size();
		}
		boolean flag1 = marketResourceService.checkAmount(cust_id,size, "3", "3", "6");//
		if (!flag1) {
			code = "1003";
			message = "余额不足支付";
		} else {

		for (int i = 0; i < size; i++) {
			if(null!=allList){
				superid=allList.get(i).get("superid").toString();
			}else{
				JSONObject jsonItem = supidList.getJSONObject(i);
				superid = jsonItem.getString("superid");
			}
			 // 获取email
			 MultiValueMap<String, Object> urlVariables = new
                     LinkedMultiValueMap<String, Object>();
			 urlVariables.add("interfaceID", "BQ0018");
			 urlVariables.add("superid", superid);
			 String result =
			 customGroupService.getMicroscopicPhone(superid,
			 urlVariables);// 客户身份id
			
			 if (result == null || "".equals(result)) {
			 continue;
			 }
			
			 JSONObject jsonEmailA = JSONObject.parseObject(result);
			
			 String isSuccess = jsonEmailA.getString("isSuccess");
			 if (null != isSuccess && "0".equals(isSuccess)) {
			 continue;
			 }
			
			 String email =
			 JSONObject.parseObject(jsonEmailA.getString("ids")).getString("em").replaceAll("\"","").replace("[",
			 "")
			 .replace("]", "");
			
			 if (email == null || "".equals(email)) {
			 continue;
			 }

				MailUtil mail = MailUtil.getInstance();
				mail.sendout(title, content, email);
				
				code = "1001";
				message = "邮件发送成功";
				quantity = quantity+1;
				
				//将发送邮件的记录添加到记录表里面
				MarketResourceLogDTO marketResourceLogDTO = new MarketResourceLogDTO();
				
				marketResourceLogDTO.setType_code("3");
				marketResourceLogDTO.setUser_id(userId);
				marketResourceLogDTO.setCust_id(cust_id);
				marketResourceLogDTO.setSuperId(superid);
				marketResourceLogDTO.setEmail_content(content);
				// 发送成功
				marketResourceLogDTO.setStatus(1);
				marketResourceLogDTO.setBatchNumber(batch_number);
				
				Integer templateIdIn = Integer.parseInt(templateId);
				marketResourceLogDTO.setTemplateId(templateIdIn);
				marketResourceService.insertLog(marketResourceLogDTO);
				
			}
		
		

		String status = "1";
		String user_id = opUser().getId().toString();
		String enpterprise_name = opUser().getEnterpriseName();
		
		String data  = marketResourceService.updateResourceForEmail(code, cust_id, user_id, status , content, superid, templateId,enpterprise_name,batch_number,quantity);
		
		resultMap.put("message", "成功");
		resultMap.put("data", "1");
			
		//}
		}
	
		json.put("data", resultMap);
		return json.toString();
	}
	
	
	@RequestMapping(value = "/sendEmailForAsk", method = RequestMethod.POST)
	@ResponseBody
	@CacheAnnotation
	public String sendEmailbyUser(String content) {
		        Map<String, Object> resultMap = new HashMap<>();
		        JSONObject json = new JSONObject();
                String email = "";
                //String email = "";
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");//设置日期格式
                String title = "精准营销官网-合作咨询-" +df.format(new Date());
				MailUtil mail = MailUtil.getInstance();
				mail.sendout(title, content, email);
				resultMap.put("message", "成功");
				resultMap.put("data", "1");
				json.put("data", resultMap);
				return json.toString();
	}
}
