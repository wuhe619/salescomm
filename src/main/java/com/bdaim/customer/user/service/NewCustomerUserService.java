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
import com.bdaim.util.CipherUtil;
import com.bdaim.util.Constant;
import com.bdaim.util.IDHelper;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;

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
                customerUserDO.setPassword(CipherUtil.generatePassword(vo.getPassword()));
                customerUserDO.setRealname(vo.getRealName());
                customerUserDO.setStatus(Constant.USER_ACTIVE_STATUS);
                customerUserDao.saveOrUpdate(customerUserDO);

            }
        }

        //联系人电话
        if (StringUtil.isNotEmpty(vo.getMobile())) {
            customerDao.dealCustomerInfo(vo.getCustId(), "mobile", vo.getMobile());
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
        vo.setMobile(property.getPropertyValue());
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


}
