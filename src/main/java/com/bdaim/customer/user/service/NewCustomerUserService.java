package com.bdaim.customer.user.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.util.*;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Date;

@Service
public class NewCustomerUserService {
    @Resource
    CustomerUserDao customerUserDao;

    @Resource
    CustomerDao customerDao;

    public int updateCustomerUser(CustomerRegistDTO vo, LoginUser lu) throws Exception {
        CustomerUser customerUserDO;
        if (StringUtil.isNotEmpty(vo.getUserId()) && !"0".equals(vo.getUserId())) {
            customerUserDO = customerUserDao.findUniqueBy("id", Long.valueOf(vo.getUserId()));
            customerUserDO.setRealname(vo.getRealName());
            if (StringUtil.isNotEmpty(vo.getName())) {
                customerUserDO.setAccount(vo.getName());
            }
            if (StringUtil.isNotEmpty(vo.getPassword())) {
                PasswordChecker checker = new PasswordChecker();
                if(!checker.check(vo.getPassword())){
                    throw new Exception("密码不符合要求");
                }
                customerUserDO.setPassword(CipherUtil.generatePassword(vo.getPassword()));
            }
            customerUserDao.saveOrUpdate(customerUserDO);
        } else {
            if (StringUtil.isNotEmpty(vo.getName())) {
                CustomerUser user = customerUserDao.getUserByAccount(vo.getName());
                if (user != null) return -1;
                customerUserDO = new CustomerUser();
                //1企业客户 2 操作员
                customerUserDO.setUserType(1);
                customerUserDO.setId(IDHelper.getUserID());
                customerUserDO.setCust_id(vo.getCustId());
                customerUserDO.setAccount(vo.getName());
                PasswordChecker checker = new PasswordChecker();
                if(!checker.check(vo.getPassword())){
                    throw new Exception("密码不符合要求");
                }
                customerUserDO.setPassword(CipherUtil.generatePassword(vo.getPassword()));
                customerUserDO.setRealname(vo.getRealName());
                customerUserDO.setStatus(Constant.USER_ACTIVE_STATUS);
                customerUserDO.setCreateTime(String.valueOf(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss)));
                customerUserDao.saveOrUpdate(customerUserDO);
            }
        }

        //联系人电话
        if (StringUtil.isNotEmpty(vo.getMobile())) {
            customerDao.dealCustomerInfo(vo.getCustId(), "mobile", vo.getMobile());
        }

        //账号绑定手机号
        if (StringUtil.isNotEmpty(vo.getMobile_num())) {
//            CustomerUserPropertyDO customerUserPropertyDO=new CustomerUserPropertyDO();
//            customerUserPropertyDO.setPropertyName("mobile_num");
//            customerUserPropertyDO.setPropertyValue(vo.getMobile_num());
//            customerUserPropertyDO.setCreateTime(new Date().getTime());
//            customerUserDao.saveOrUpdate(customerUserPropertyDO);
            customerUserDao.dealCustomerInfo(vo.getUserId(), "mobile_num", vo.getMobile_num());
        }
        return 1;
    }


    public CustomerRegistDTO getByUserId(String userId) throws Exception {
        CustomerRegistDTO vo = new CustomerRegistDTO();
        CustomerUser customerUser = customerUserDao.findUniqueBy("id", Long.valueOf(userId));
        if (customerUser == null) throw new Exception("企业用户不存在");
        vo.setCustId(customerUser.getCust_id());
        vo.setName(customerUser.getAccount());
        vo.setRealName(customerUser.getRealname() == null ? "" : customerUser.getRealname());
        vo.setUserId(customerUser.getId() + "");
        vo.setPassword(customerUser.getPassword());
        CustomerProperty property = customerDao.getProperty(customerUser.getCust_id(), "mobile");
        if(property!=null) { //联系人电话
            vo.setMobile(property.getPropertyValue());
        }
        CustomerUserPropertyDO propertyDO =customerUserDao.getProperty(customerUser.getId()+"","mobile_num");
        if(propertyDO!=null) {//二次验证手机号
            vo.setMobile_num(propertyDO.getPropertyValue());
        }
        return vo;

    }

    public Page getCustomerUserList(PageParam page, String custId, JSONObject info) {
        Page pageData = customerUserDao.pagePropertyByType(page.getPageNum(), page.getPageSize(), Constant.USER_ACTIVE_STATUS, custId, info);
        return pageData;
    }

    public int delCustomerUserById(String custId, String userId) throws Exception {
        CustomerUser customerUser = customerUserDao.selectPropertyById(Long.valueOf(userId), custId, 1);
        if (customerUser == null) {
            throw new Exception("企业id:[" + custId + "],用户" + userId + "不存在");
        }
        customerUser.setStatus(1);
        customerUserDao.update(customerUser);
        return 1;
    }

    public int updateUserPassword(String Id, String password) throws Exception {
        CustomerUser customerUserDO = customerUserDao.findUniqueBy("account", Id);
        if (customerUserDO == null) {
            throw new Exception("用户" + Id + "不存在");
        }
        PasswordChecker checker = new PasswordChecker();
        if(!checker.check(password)){
            throw new Exception("密码不符合要求");
        }

        customerUserDO.setPassword(CipherUtil.generatePassword(password));
        customerUserDao.update(customerUserDO);
        return 1;
    }


}
