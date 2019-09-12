package com.bdaim.customer.controller;


import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.entity.CustomerCallCenterAccount;
import com.bdaim.customer.service.CustomerCallCenterAccountService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

/**
 * 企业呼叫中心账号
 */
@RestController
@RequestMapping("/customeraccount")
public class CustomerCallCenterAccountAction extends BasicAction {
	@Resource
	private CustomerCallCenterAccountService customerCallCenterAccountService;

	/**
	 * 分页查询
	 * @param pageSize
	 * @param pageNum
	 * @param customerName
	 * @param customerAccount
	 * @param id
	 * @return
	 */
	@ValidatePermission(role = "admin,ROLE_USER")
	@RequestMapping(value = "/page/query",method = RequestMethod.GET)
	public String getAccountList(Integer pageSize,Integer pageNum,String customerName,String customerAccount,String id){
		if(pageSize==null || pageNum==null){
			throw new ParamException("参数错误");
		}
		JSONObject r = customerCallCenterAccountService.page(customerName,customerAccount,id,pageNum,pageSize);
		return returnJsonData(r);
	}

	/**
	 * 保存
	 * @param ccAcount
	 * @return
	 */
	@ValidatePermission(role = "admin,ROLE_USER")
	@RequestMapping(value = "/save",method = RequestMethod.POST)
	public String saveCustomerCallCenterAccount(@RequestBody CustomerCallCenterAccount ccAcount){
		if(StringUtil.isEmpty(ccAcount.getCustId()) || ccAcount.getStatus()==null){
			throw new ParamException("参数错误");
		}

		try {
			customerCallCenterAccountService.save(ccAcount,opUser());
		} catch (Exception e) {
			e.printStackTrace();
			return returnJsonData(e.getMessage());
		}
		return returnJsonData("success");
	}
}
