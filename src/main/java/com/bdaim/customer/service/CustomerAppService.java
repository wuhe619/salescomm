package com.bdaim.customer.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.customer.dao.AmApplicationDao;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.AmApplicationEntity;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.util.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;

@Service
public class CustomerAppService {
    private static Logger logger = LoggerFactory.getLogger(CustomerAppService.class);
    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    CustomerDao customerDao;
    @Resource
    CustomerUserDao customerUserDao;
    @Resource
    AmApplicationDao amApplicationDao;

    public synchronized String registerOrUpdateCustomer(CustomerRegistDTO vo, String id, LoginUser lu) throws Exception {
        String code = "000";
        //编辑或创建客户
        CustomerUser customerUserDO;
        String customerId = IDHelper.getID().toString();
        if (StringUtil.isNotEmpty(id)) {
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
                if (user != null) return code = "001";
                customerUserDO = new CustomerUser();
                //1企业客户 2 操作员
                customerUserDO.setUserType(1);
                customerUserDO.setId(IDHelper.getUserID());
                customerUserDO.setCust_id(customerId);
                customerUserDO.setAccount(vo.getName());
                customerUserDO.setPassword(CipherUtil.generatePassword(vo.getPassword()));
                customerUserDO.setRealname(vo.getRealName());
                customerUserDO.setStatus(Constant.USER_ACTIVE_STATUS);
                customerUserDao.saveOrUpdate(customerUserDO);
                saveAmApplication(vo, lu);
            }
        }

        Customer customer;
        if (StringUtil.isNotEmpty(vo.getCustId())) {
            //更新 客户信息
            customer = customerDao.findUniqueBy("custId", vo.getCustId());
            customer.setRealName(vo.getRealName());
            //职位/职级
            customer.setTitle(vo.getTitle());
            customer.setEnterpriseName(vo.getEnterpriseName());
        } else {
            //创建客户信息
            customer = new Customer();
            customer.setCustId(customerId);
            customer.setRealName(vo.getRealName());
            //职位/职级
            customer.setTitle(vo.getTitle());
            customer.setEnterpriseName(vo.getEnterpriseName());
            customer.setStatus(Constant.USER_ACTIVE_STATUS);
            customer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
        }
        customerDao.saveOrUpdate(customer);


        //创建企业附加属性信息
        if (StringUtil.isNotEmpty(vo.getProvince())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "province", vo.getProvince());
            } else {
                customerDao.dealCustomerInfo(customerId, "province", vo.getProvince());
            }
        }
        if (StringUtil.isNotEmpty(vo.getCity())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "city", vo.getCity());
            } else {
                customerDao.dealCustomerInfo(customerId, "city", vo.getCity());
            }
        }
        if (StringUtil.isNotEmpty(vo.getCountry())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "county", vo.getCountry());
            } else {
                customerDao.dealCustomerInfo(customerId, "county", vo.getCountry());
            }
        }
        if (StringUtil.isNotEmpty(vo.getWhiteIps())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "whiteIps", vo.getWhiteIps());
            } else {
                customerDao.dealCustomerInfo(customerId, "whiteIps", vo.getWhiteIps());
            }
        }
        //统一社会信用代码(纳税人识别号)
        if (StringUtil.isNotEmpty(vo.getTaxPayerId())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "taxpayer_id", vo.getTaxPayerId());
            } else {
                customerDao.dealCustomerInfo(customerId, "taxpayer_id", vo.getTaxPayerId());
            }
        }
        //营业执照url
        if (StringUtil.isNotEmpty(vo.getBliPath())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "bli_path", vo.getBliPath());
            } else {
                customerDao.dealCustomerInfo(customerId, "bli_path", vo.getBliPath());
            }
        }
        if (StringUtil.isNotEmpty(vo.getBank())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "bank", vo.getBank());
            } else {
                customerDao.dealCustomerInfo(customerId, "bank", vo.getBank());
            }
        }
        if (StringUtil.isNotEmpty(vo.getBankAccount())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "bank_account", vo.getBankAccount());
            } else {
                customerDao.dealCustomerInfo(customerId, "bank_account", vo.getBankAccount());
            }
        }
        //银行开户许可证url
        if (StringUtil.isNotEmpty(vo.getBankAccountCertificate())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "bank_account_certificate", vo.getBankAccountCertificate());
            } else {
                customerDao.dealCustomerInfo(customerId, "bank_account_certificate", vo.getBankAccountCertificate());
            }
        }
        //行业
        if (StringUtil.isNotEmpty(vo.getIndustry())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "industry", vo.getIndustry());
            } else {
                customerDao.dealCustomerInfo(customerId, "industry", vo.getIndustry());
            }
        }
        if (StringUtil.isNotEmpty(vo.getIndustryPicture())) {
            String custid = customerId;
            if (StringUtil.isNotEmpty(vo.getCustId())) custid = vo.getCustId();
            JSONObject jsonObject = JSONObject.parseObject(vo.getIndustryPicture());
            Iterator<String> iterator = jsonObject.keySet().iterator();
            while (iterator.hasNext()) {
                String key = iterator.next();
                customerDao.dealCustomerInfo(custid, key, jsonObject.getString(key));
            }
        }
        //预警
        if (StringUtil.isNotEmpty(vo.getBalance_warning_config())) {
            String custid = customerId;
            if (StringUtil.isNotEmpty(vo.getCustId())) custid = vo.getCustId();
            JSONObject jsonObject = JSONObject.parseObject(vo.getBalance_warning_config());
            String warning_money = jsonObject.getString("warning_money");
            if (StringUtil.isNotEmpty(warning_money)) {
                customerDao.dealCustomerInfo(custid, "warning_money", warning_money);
            }
            if (StringUtil.isNotEmpty(jsonObject.getString("email_link"))) {
                customerDao.dealCustomerInfo(custid, "email_link", jsonObject.getString("email_link"));
            }
            if (StringUtil.isNotEmpty(jsonObject.getString("short_msg_link"))) {
                customerDao.dealCustomerInfo(custid, "short_msg_link", jsonObject.getString("short_msg_link"));
            }
        }
        //销售负责人
        //if (StringUtil.isNotEmpty(vo.getSalePerson())) {
        if (StringUtil.isNotEmpty(vo.getCustId())) {
            customerDao.dealCustomerInfo(vo.getCustId(), "sale_person", vo.getSalePerson());
        } else {
            customerDao.dealCustomerInfo(customerId, "sale_person", vo.getSalePerson());
        }
        // }
        //企业注册详细街道地址
        if (StringUtil.isNotEmpty(vo.getAddress())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "reg_address", vo.getAddress());
            } else {
                customerDao.dealCustomerInfo(customerId, "reg_address", vo.getAddress());
            }
        }
        //联系人电话
        if (StringUtil.isNotEmpty(vo.getMobile())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "mobile", vo.getMobile());
            } else {
                customerDao.dealCustomerInfo(customerId, "mobile", vo.getMobile());
            }

        }
        if (StringUtil.isNotEmpty(vo.getBrand())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "brand", vo.getBrand());
            } else {
                customerDao.dealCustomerInfo(customerId, "brand", vo.getBrand());
            }

        }
        if (StringUtil.isNotEmpty(vo.getCustId())) {
            customerDao.dealCustomerInfo(vo.getCustId(), "mobile_num", vo.getMobile());
        } else {
            customerDao.dealCustomerInfo(customerId, "mobile_num", vo.getMobile());
        }
        //创建企业id
        if (StringUtil.isNotEmpty(vo.getCreateId())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "create_id", vo.getCreateId());
            } else {
                customerDao.dealCustomerInfo(customerId, "create_id", vo.getCreateId());
            }
        }

        return code;
    }

    public int saveAmApplication(CustomerRegistDTO vo, LoginUser lu) {

        AmApplicationEntity entity = new AmApplicationEntity();
        entity.setCreateBy(lu.getName());
        entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        entity.setStatus("0");
        entity.setName(vo.getEnterpriseName());

        return (int) amApplicationDao.saveReturnPk(entity);
    }
}
