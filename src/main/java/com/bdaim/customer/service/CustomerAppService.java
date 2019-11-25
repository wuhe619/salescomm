package com.bdaim.customer.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
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
import java.util.Map;

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

    public synchronized String registerOrUpdateCustomer(CustomerRegistDTO vo, LoginUser lu) throws Exception {
        String code = "000";
        //编辑或创建客户
        CustomerUser customerUserDO;
        String customerId = IDHelper.getID().toString();
        if (StringUtil.isNotEmpty(vo.getUserId())) {
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
        if (StringUtil.isNotEmpty(vo.getCustId()) && !"0".equals(vo.getCustId())) {
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
        entity.setStatus("APPROVED");
        entity.setName("DefaultApplication");
        entity.setTier("Unlimited");

        return (int) amApplicationDao.saveReturnPk(entity);
    }


    public PageList getUser(PageParam page, String customerId, String account, String name, String contactPerson, String salePerson) {
        JSONObject json = new JSONObject();
        StringBuffer sql = new StringBuffer();


        sql.append("  SELECT  CAST(s.id AS CHAR) id,cjc.resource,s.cust_id,s.user_type, s.account AS adminAccount,s.password AS PASSWORD,s.realname AS realname,cjc.cuc_minute seatMinute,\n" +
                "s.status as STATUS,cjc.mobile_num AS mobile_num,cjc.cuc_seat AS cuc_seat,cjc.declare_no,cjc.input_no,cjc.xz_seat AS xz_seat ,tc.title as title, cjc.user_id as suerId ," +
                "pro.province as province ,pro.city as city,pro.country as country,pro.taxPayerId as taxPayerId,pro.bliPath as bliPath,pro.bank as bank,pro.bankAccount as bankAccount,\n" +
                "pro.bankAccountCertificate as bankAccountCertificate,pro.industry as industry,pro.salePerson as salePerson,pro.address as address,pro.brand as brand,pro.station_id as stationId,pro.api_token as apiToken" +
                " FROM t_customer_user s\n" +
                " LEFT JOIN (SELECT user_id, \n" +
                " MAX(CASE property_name WHEN 'mobile_num'  THEN property_value ELSE '' END ) mobile_num, \n" +
                " MAX(CASE property_name WHEN 'cuc_seat'    THEN property_value ELSE '' END ) cuc_seat,\n" +
                " MAX(CASE property_name WHEN 'xz_seat'    THEN property_value ELSE '' END ) xz_seat, \n" +
                " MAX(CASE property_name WHEN 'cuc_minute'  THEN property_value ELSE '0' END ) cuc_minute, \n" +
                " MAX(CASE property_name WHEN 'declare_no'  THEN property_value ELSE '0' END ) declare_no, \n" +
                " MAX(CASE property_name WHEN 'input_no'  THEN property_value ELSE '0' END ) input_no, \n" +
                " MAX(CASE property_name WHEN 'resource'    THEN property_value ELSE '' END ) resource \n" +
                " FROM t_customer_user_property p GROUP BY user_id \n" +
                ") cjc ON s.id = cjc.user_id LEFT JOIN  t_customer tc ON s.cust_id=tc.cust_id " +
                "LEFT JOIN (SELECT cust_id,\n" +
                " MAX(CASE property_name WHEN 'province'    THEN property_value ELSE '' END ) province, \n" +
                " MAX(CASE property_name WHEN 'city'    THEN property_value ELSE '' END ) city, \n" +
                " MAX(CASE property_name WHEN 'country'    THEN property_value ELSE '' END ) country, \n" +
                " MAX(CASE property_name WHEN 'taxpayer_id'    THEN property_value ELSE '' END  )taxPayerId, \n" +
                "MAX( CASE property_name WHEN 'bli_path'    THEN property_value ELSE '' END  )bliPath, \n" +
                " MAX(CASE property_name WHEN 'bank'    THEN property_value ELSE '' END ) bank, \n" +
                "MAX(CASE property_name WHEN 'bank_account'    THEN property_value ELSE '' END ) bankAccount, \n" +
                " MAX(CASE property_name WHEN 'bank_account_certificate'    THEN property_value ELSE '' END ) bankAccountCertificate, \n" +
                " MAX(CASE property_name WHEN 'industry'    THEN property_value ELSE '' END  )industry, \n" +
                " MAX(CASE property_name WHEN 'sale_person'    THEN property_value ELSE '' END ) salePerson, \n" +
                "MAX( CASE property_name WHEN 'reg_address'    THEN property_value ELSE ''END ) address, \n" +
                " MAX(CASE property_name WHEN 'brand'    THEN property_value ELSE '' END ) brand,\n" +
                " MAX(CASE property_name WHEN 'station_id'  THEN property_value ELSE '' END ) station_id, \n" +
                " MAX(CASE property_name WHEN 'api_token'  THEN property_value ELSE '' END ) api_token \n" +
                "FROM t_customer_property  )pro ON s.cust_id=pro.cust_id " +
                "WHERE 1=1 AND user_type = 2  AND s.STATUS <> 2 ");
        if (StringUtil.isNotEmpty(customerId)) {
            sql.append(" AND cust_id = '" + customerId + "'");
        }
        if (null != name && !"".equals(name)) {
            sql.append(" AND s.account like '%" + name + "%'");
        }
//        if (null != realName && !"".equals(realName)) {
//            sql.append(" AND s.realname like '%" + realName + "%'");
//        }
//        if (null != mobileNum && !"".equals(mobileNum)) {
//            sql.append(" AND cjc.mobile_num like '%" + mobileNum + "%'");
//        }

        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);

//        if (list != null && list.getList() != null && list.getList().size() > 0) {
//            Map<String, Object> map;
//            for (int i = 0; i < list.getList().size(); i++) {
//                map = (Map) list.getList().get(i);
//                if (map != null && map.get("cuc_seat") != null) {
//                    String cuc_seat = String.valueOf(map.get("cuc_seat"));
//                    com.alibaba.fastjson.JSONObject json1 = JSON.parseObject(cuc_seat);
//                    if (json1 != null) {
//                        String mainNumber = json1.getString("mainNumber");
//                        map.put("cucMainNumber", mainNumber);
//                    }
//                }
//                if (map != null && map.get("xz_seat") != null) {
//                    String cmc_seat = String.valueOf(map.get("xz_seat"));
//                    com.alibaba.fastjson.JSONObject json1 = JSON.parseObject(cmc_seat);
//                    if (json1 != null) {
//                        String mainNumber1 = json1.getString("mainNumber");
//                        map.put("xzMainNumber", mainNumber1);
//                    }
//                }
//                if (map != null && map.get("xz_seat") != null) {
//                    String cmc_seat = String.valueOf(map.get("xz_seat"));
//                    com.alibaba.fastjson.JSONObject json1 = JSON.parseObject(cmc_seat);
//                    if (json1 != null) {
//                        String mainNumber1 = json1.getString("mainNumber");
//                        map.put("xzMainNumber", mainNumber1);
//                    }
//                }
//            }
//        }
        return list;
    }

    public int delCust(long custId){
        return  1;
    }
}
