package com.bdaim.slxf.service.impl;

import com.alibaba.druid.support.logging.Log;
import com.alibaba.druid.support.logging.LogFactory;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.slxf.dto.RegisterDTO;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.Constant;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dao.UserRoleRelDao;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.rbac.entity.UserRoleRelDO;
import com.bdaim.slxf.dao.*;
import com.bdaim.slxf.dto.RegisterDTO;
import com.bdaim.slxf.entity.*;
import com.bdaim.slxf.service.RegisterService;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 注册
 * 
 * @author lanxq@bdcsdk.com
 * @version v1.0
 * @date 2017年5月22日
 */
@Service("registerService")
@Transactional
public class RegisterServiceImpl implements RegisterService {

	private static Log log = LogFactory.getLog(RegisterServiceImpl.class);
	/*@Resource
    CustomerDao customerDao;*/
//	@Resource
//	EnterpriseDao enterpriseDao;
	/*@Resource
    AccountDao accountDao;*/
	@Resource
    UserDao userDao;
	@Resource
    UserRoleRelDao userRoleRelDao;
	@Resource
	private JdbcTemplate jdbcTemplate;
	@Resource
    RegisterDao registerDao;

	@Override
	public String validationUserName(String userName) {
		String sql = "SELECT count(0) as count from t_user where `STATUS`=0 and `name`=? ";
		return jdbcTemplate.queryForObject(sql, String.class, new Object[] { userName });
	}

	@Override
	public String validationPhone(String phone) {
		String sql = "SELECT count(0) as count from t_user where `STATUS`=0 and mobile_num=? ";
		return jdbcTemplate.queryForObject(sql, String.class, new Object[] { phone });
	}

	@Override
	public void saveNewUser(RegisterDTO value) {
		String customerId = com.bdaim.common.util.IDHelper.getID().toString();
		Long UserId = com.bdaim.common.util.IDHelper.getID();
		// 创建客户登陆信息
		UserDO userDO = new UserDO();
		// 1企业客户 2 操作员
		userDO.setUserType(1);
		userDO.setId(UserId);
		userDO.setCustId(customerId);
		userDO.setName(value.getUserName());
		userDO.setPassword(CipherUtil.generatePassword(value.getPassword()));
		userDO.setMobileNum(value.getMobile());
		userDO.setStatus(2);
		userDO.setSource(1);
		userDO.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
		registerDao.save(userDO);
		// 开户默认权限为
		UserRoleRelDO userRoleRelDO = new UserRoleRelDO();
		userRoleRelDO.setCreateTime(new java.sql.Date(System.currentTimeMillis()));
		userRoleRelDO.setId(UserId.toString());
		userRoleRelDO.setOptuser("system");
		userRoleRelDO.setRole(38888L);
		userRoleRelDO.setLevel(0);
		userRoleRelDao.save(userRoleRelDO);
	}

	@Override
	public void CustomerRegist(RegisterDTO value, String customerId, Long userId) throws Exception {
		//CustomerDO customer = new CustomerDO();
		// String customerId = com.bdaim.slxf.util.IDHelper.getID().toString();
		// 创建默认账户信息
		/*AccountDO accountDO = new AccountDO();
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
		accountDao.save(accountDO);*/
		// 创建客户登陆信息
		// 1企业客户 2 操作员
		// 更新t_user
		StringBuffer sb = new StringBuffer();
		sb.append(
				"UPDATE t_user set realname=?,enterprise_name=?,email=?,title=?,user_pwd_level=?,`STATUS`=4 where cust_id=? and id=?");
		jdbcTemplate.update(sb.toString(), new Object[] { value.getRealName(),
				value.getEnterpriseName(), value.getEmail(), value.getTitle(), value.getUserPwdLevel(),customerId,userId});
		// 创建客户信息
		/*customer.setCustId(customerId);
		customer.setRealName(value.getRealName());
		customer.setTitle(value.getTitle());
		customer.setEnterpriseName(value.getEnterpriseName());
		customer.setStatus(Constant.USER_ACTIVE_STATUS);
		customer.setCreateTime(
				DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
		customerDao.save(customer);*/
		// 创建企业信息
		/*EnterpriseDO enterpriseDO = new EnterpriseDO();
		enterpriseDO.setCustId(customerId);
		enterpriseDO.setRegAddress(value.getAddress());
		enterpriseDO.setEnterpriseId(IDHelper.getID().toString());
		enterpriseDO.setName(value.getEnterpriseName());
		enterpriseDO.setProvince(value.getProvince());
		enterpriseDO.setCity(value.getCity());
		enterpriseDO.setCounty(value.getCounty());
		enterpriseDO.setBliNumber(value.getBliNumber());
		enterpriseDO.setBliPath(value.getBliPath());
		enterpriseDO.setTaxpayerId(value.getTaxPayerId());
		enterpriseDO.setTaxpayerCertificatePath(value.getTaxpayerCertificatePath());
		enterpriseDO.setBank(value.getBank());
		enterpriseDO.setBankAccount(value.getBankAccount());
		enterpriseDO.setBankAccountCertificate(value.getBankAccountCertificate());
		enterpriseDO.setStatus(Constant.ENTERPRISE_ACTIVE_STATUS);*/
		//enterpriseDao.save(enterpriseDO);
	}

	@Override
	public List<Map<String, Object>> getCustomerStatus(String user_id) {
		JSONObject json = new JSONObject();
		List<Map<String, Object>> list= new ArrayList();
		String sql="select property_name,property_value from t_customer_user_property where user_id="+user_id;
		List ds = this.registerDao.getSQLQuery(sql).list();
		for(int i=0;i<ds.size();i++) {
			Object[] d = (Object[])ds.get(i);
			Map m = new HashMap();
			m.put((String)d[0], d[1]);
			list.add(m);
		}
		
		Map m = new HashMap();
		m.put("STATUS", 0);
		list.add(m);
		
		return list;
		
	}
}
