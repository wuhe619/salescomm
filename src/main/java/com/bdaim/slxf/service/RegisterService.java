package com.bdaim.slxf.service;

import java.util.List;
import java.util.Map;

import com.bdaim.slxf.dto.RegisterDTO;
import com.bdaim.slxf.dto.RegisterDTO;

/**
 * 注册
 * @author lanxq@bdcsdk.com
 * @version v1.0
 * @date 2017年5月22日
 */
public interface RegisterService {
	
	/**
	 * 验证用户名
	 * @param userName
	 * @return
	 */
	public String validationUserName(String userName);
	
	/**
	 * 验证手机号
	 * @param phone
	 * @return
	 */
	public String validationPhone(String phone);
	
	/**
	 * 增加新用户
	 * @param phone
	 * @return
	 */
	public void saveNewUser(RegisterDTO registerDTO);
	
	/**
	 * 企业信息添加
	 * @param registerDTO
	 * @throws Exception
	 */
	public void CustomerRegist(RegisterDTO registerDTO,String customerId,Long userId) throws Exception;
	
	/**
	 * 查询当前用户的企业认证状态
	 * @param user_id
	 */
	public List<Map<String, Object>> getCustomerStatus(String user_id);
	
}
