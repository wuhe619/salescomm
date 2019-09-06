package com.bdaim.rbac.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.account.dao.AccountDao;
import com.bdaim.account.entity.AccountDO;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.Constant;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.EnterpriseDao;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.rbac.dao.RegisterDao;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dao.UserRoleRelDao;
import com.bdaim.rbac.dto.RegisterDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * 注册
 * 
 */
@Service("registerService")
@Transactional
public class RegisterService {

	private static Logger log = LoggerFactory.getLogger(RegisterService.class);
	@Resource
	CustomerDao customerDao;
	@Resource
	EnterpriseDao enterpriseDao;
	@Resource
	AccountDao accountDao;
	@Resource
	UserDao userDao;
	@Resource
	UserRoleRelDao userRoleRelDao;
	@Resource
	private JdbcTemplate jdbcTemplate;
	@Resource
	RegisterDao registerDao;

	public String validationUserName(String userName) {
		String sql = "SELECT count(0) as count from t_customer_user where `STATUS`=0 and `account`=? ";
		return jdbcTemplate.queryForObject(sql, String.class, new Object[] { userName });
	}

	public String validationPhone(String phone) {
		String sql = "SELECT count(0) as count from t_customer_user_property where property_name='mobile_num' and property_value=? ";
		return jdbcTemplate.queryForObject(sql, String.class, new Object[] { phone });
	}

	public void saveNewUser(RegisterDTO value) {
		String customerId = IDHelper.getID().toString();
		Long UserId = IDHelper.getID();
		// 创建客户登陆信息
		CustomerUser userDO = new CustomerUser();
		// 1企业客户 2 操作员
		userDO.setUserType(1);
		userDO.setId(UserId);
		userDO.setCust_id(customerId);
		userDO.setAccount(value.getUserName());
		userDO.setPassword(CipherUtil.generatePassword(value.getPassword()));
//		userDO.setMobileNum(value.getMobile());
		userDO.setStatus(2);
//		userDO.setSource(1);
		userDO.setCreateTime(String.valueOf(new Timestamp(System.currentTimeMillis())));
		registerDao.saveOrUpdate(userDO);
		
		CustomerUserPropertyDO mobile_num = new CustomerUserPropertyDO(userDO.getId().toString(), "mobile_num", value.getMobile(), new Timestamp(System.currentTimeMillis()));
		registerDao.saveOrUpdate(mobile_num);
		
//		// 开户默认权限为
//		UserRoleRelDO userRoleRelDO = new UserRoleRelDO();
//		userRoleRelDO.setCreateTime(new java.sql.Date(System.currentTimeMillis()));
//		userRoleRelDO.setId(UserId.toString());
//		userRoleRelDO.setOptuser("system");
//		userRoleRelDO.setRole(38888L);
//		userRoleRelDO.setLevel(0);
//		userRoleRelDao.save(userRoleRelDO);
	}


	public void CustomerRegist(RegisterDTO value, String customerId, Long userId) throws Exception {
		Customer customer = new Customer();
		// String customerId = com.bdaim.sale.util.IDHelper.getID().toString();
		// 创建默认账户信息
		AccountDO accountDO = new AccountDO();
		accountDO.setAcctId(IDHelper.getID().toString());
		accountDO.setCustId(customerId);
		// 开户时支付密码未设置为0
		accountDO.setPwdStatus(0);
		// 创建默认账户时余额和已经使用的默认为0
		accountDO.setRemainAmount(0);
		accountDO.setUsedAmount(0);
		// 1.prepaid 2.postpaid
		accountDO.setPayType(1);
		accountDO.setStatus(0);
		accountDO.setCreateTime(
		DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
		accountDao.save(accountDO);
		// 创建客户登陆信息
		// 1企业客户 2 操作员
		// 更新t_user
		StringBuffer sb = new StringBuffer();
		sb.append("UPDATE t_user set realname=?,enterprise_name=?,email=?,title=?,user_pwd_level=?,`STATUS`=4 where cust_id=? and id=?");
		jdbcTemplate.update(sb.toString(), new Object[] { value.getRealName(),
				value.getEnterpriseName(), value.getEmail(), value.getTitle(), value.getUserPwdLevel(),customerId,userId});
		// 创建客户信息
		customer.setCustId(customerId);
		customer.setRealName(value.getRealName());
		customer.setTitle(value.getTitle());
		customer.setEnterpriseName(value.getEnterpriseName());
		customer.setStatus(Constant.USER_ACTIVE_STATUS);
		customer.setCreateTime(
		DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
		customerDao.save(customer);
		
		CustomerProperty regAddress = new CustomerProperty();
		
//		// 创建企业信息
//		EnterpriseDO enterpriseDO = new EnterpriseDO();
//		enterpriseDO.setCustId(customerId);
//		enterpriseDO.setRegAddress(value.getAddress());
//		enterpriseDO.setEnterpriseId(IDHelper.getID().toString());
//		enterpriseDO.setName(value.getEnterpriseName());
//		enterpriseDO.setProvince(value.getProvince());
//		enterpriseDO.setCity(value.getCity());
//		enterpriseDO.setCounty(value.getCounty());
//		enterpriseDO.setBliNumber(value.getBliNumber());
//		enterpriseDO.setBliPath(value.getBliPath());
//		enterpriseDO.setTaxpayerId(value.getTaxPayerId());
//		enterpriseDO.setTaxpayerCertificatePath(value.getTaxpayerCertificatePath());
//		enterpriseDO.setBank(value.getBank());
//		enterpriseDO.setBankAccount(value.getBankAccount());
//		enterpriseDO.setBankAccountCertificate(value.getBankAccountCertificate());
//		enterpriseDO.setStatus(Constant.ENTERPRISE_ACTIVE_STATUS);
//		enterpriseDao.save(enterpriseDO);
	}


	public List<Map<String, Object>> getCustomerStatus(String user_id) {
		JSONObject json = new JSONObject();
//		String sb ="SELECT `STATUS`,refuse_to_reason,mobile_num  from t_user where  id=? ";
		String sb ="SELECT `STATUS`  from t_customer_user where id=? ";
		List<Map<String, Object>> list=jdbcTemplate.queryForList(sb,user_id);
		return list;
		
	}
}
