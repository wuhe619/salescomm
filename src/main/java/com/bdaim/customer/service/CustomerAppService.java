package com.bdaim.customer.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;
import com.bdaim.common.page.Pagination;
import com.bdaim.customer.dao.AmApplicationDao;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.dto.Deposit;
import com.bdaim.customer.entity.AmApplicationEntity;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.util.Constant;
import com.bdaim.util.DateUtil;
import com.bdaim.util.IDHelper;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

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


    public synchronized String registerOrUpdateCustomer(CustomerRegistDTO vo, LoginUser lu) {
        String code = "000";
        //编辑或创建客户
        String customerId = IDHelper.getID().toString();

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
            vo.setCustId(customerId);
            saveAmApplication(vo, lu);
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
        if (StringUtil.isNotEmpty(vo.getMobile())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "mobile", vo.getProvince());
            } else {
                customerDao.dealCustomerInfo(customerId, "mobile", vo.getProvince());
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
        if (StringUtil.isNotEmpty(vo.getIntenIndustry())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "inten_industry", vo.getIntenIndustry());
                customerDao.dealCustomerInfo(vo.getCustId(), "industry_picture_value", vo.getIndustryPictureValue());
            } else {
                customerDao.dealCustomerInfo(customerId, "inten_industry", vo.getIntenIndustry());
                customerDao.dealCustomerInfo(customerId, "industry_picture_value", vo.getIndustryPictureValue());
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
        if (StringUtil.isNotEmpty(vo.getSalePerson())) {
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

        if (StringUtil.isNotEmpty(vo.getBrand())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "brand", vo.getBrand());
            } else {
                customerDao.dealCustomerInfo(customerId, "brand", vo.getBrand());
            }

        }
        if (StringUtil.isNotEmpty(vo.getCustId())) {
            customerDao.dealCustomerInfo(vo.getCustId(), "mobile", vo.getMobile());
        } else {
            customerDao.dealCustomerInfo(customerId, "mobile", vo.getMobile());
        }
        //创建企业id
        if (StringUtil.isNotEmpty(vo.getCreateId())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "create_id", vo.getCreateId());
            } else {
                customerDao.dealCustomerInfo(customerId, "create_id", vo.getCreateId());
            }
        }

        return customerId;
    }

    public int saveAmApplication(CustomerRegistDTO vo, LoginUser lu) {

        AmApplicationEntity entity = new AmApplicationEntity();
        entity.setCreateBy(lu.getName());
        entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        entity.setStatus("APPROVED");
        entity.setName("DefaultApplication");
        entity.setTier("Unlimited");
        entity.setSubscriberId(Long.valueOf(vo.getCustId()));
        return (int) amApplicationDao.saveReturnPk(entity);
    }


    public Object getUser(PageParam page, String customerId, String account, String name, String contactPerson, String salePerson) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT  CAST(s.id AS CHAR) id,s.cust_id,s.user_type, s.account AS account,s.password AS PASSWORD,s.realname AS contactPerson,tc.title as title,tc.enterprise_name as name" +
                " FROM t_customer_user s LEFT JOIN t_customer tc ON s.cust_id=tc.cust_id  " +
                " WHERE 1=1  AND s.STATUS <> 2");
        if (StringUtil.isNotEmpty(account)) {
            sql.append(" AND s.account = '" + account + "'");
        }
        if (StringUtil.isNotEmpty(contactPerson)) {
            sql.append(" AND s.realname = '" + contactPerson + "'");
        }
        if (StringUtil.isNotEmpty(name)) {
            sql.append(" AND tc.enterprise_name like '%" + name + "%'");
        }

        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        return list.getList().stream().map(m -> {
            Map map = (Map) m;

            if (StringUtil.isEmpty(map.get("cust_id").toString())) {
                return map;
            }
            String cust_id = map.get("cust_id").toString();
            CustomerProperty mobile = customerDao.getProperty(cust_id, "mobile");
            if (mobile != null) {
                map.put("mobile", mobile.getPropertyValue());
            }
            CustomerProperty sale_person = customerDao.getProperty(cust_id, "sale_person");
            if (sale_person != null) {
                map.put("sale_person", sale_person.getPropertyValue());
            }
            return map;
        }).collect(Collectors.toList());
    }

    public long delCust(long custId) throws Exception {
        Customer customer = customerDao.findUniqueBy("id", custId);
        if (customer == null || customer.getStatus() == 3) {
            throw new Exception("custId:[" + custId + "]已删除,或不存在");
        }
        customer.setStatus(3);
        customer.setModifyTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
        customerDao.update(customer);
        return custId;
    }

    public CustomerRegistDTO queryByCust(String custId) throws Exception {

        CustomerRegistDTO vo = new CustomerRegistDTO();

        Customer customer = customerDao.findUniqueBy("custId", custId);
        if (customer == null) {
            throw new Exception("custId:" + custId + "不存在");
        }
        vo.setRealName(customer.getRealName());
        vo.setTitle(customer.getTitle());
        vo.setCustId(custId);
        vo.setEnterpriseName(customer.getEnterpriseName());

        String sql = "select cust_id,property_name,property_value from t_customer_property where cust_id=?";

        List<Map<String, Object>> propertyList = jdbcTemplate.queryForList(sql, custId);
        for (Map<String, Object> map : propertyList) {
            switch (map.get("property_name").toString()) {
                case "province":
                    vo.setProvince(map.get("property_value").toString());
                    break;
                case "city":
                    vo.setCity(map.get("property_value").toString());
                case "county":
                    vo.setCountry(map.get("property_value").toString());
                case "whiteIps":
                    vo.setWhiteIps(map.get("property_value").toString());
                    break;
                case "taxpayer_id":
                    vo.setTaxPayerId(map.get("property_value").toString());
                    break;
                case "bli_path":
                    vo.setBliPath(map.get("property_value").toString());
                    break;
                case "bank":
                    vo.setBank(map.get("property_value").toString());
                    break;
                case "bank_account":
                    vo.setBankAccount(map.get("property_value").toString());
                    break;
                case "bank_account_certificate":
                    vo.setBankAccountCertificate(map.get("property_value").toString());
                    break;
                case "sale_person":
                    vo.setAddress(map.get("property_value").toString());
                    break;
                case "reg_address":
                    vo.setSalePerson(map.get("property_value").toString());
                    break;
                case "mobile":
                    vo.setMobile(map.get("property_value").toString());
                    break;
                case "brand":
                    vo.setBrand(map.get("property_value").toString());
                    break;
//                case "mobile_num":
//                    vo.setMobile(map.get("property_value").toString());
//                    break;
                case "create_id":
                    vo.setCreateId(map.get("property_value").toString());
                    break;
                case "warning_money":
                    vo.setWarning_money(map.get("property_value").toString());
                    break;
                case "short_msg_link":
                    vo.setShort_msg_link(map.get("property_value").toString());
                    break;
                case "email_link":
                    vo.setEmail_link(map.get("property_value").toString());
                    break;
//                case "industry_picture":
                case "inten_industry":
                    vo.setIntenIndustry(map.get("property_value")==null?"":map.getOrDefault("property_value","").toString());
                    break;
                case "industry_picture_value":
                    vo.setIndustryPictureValue(map.get("property_value").toString());
                    break;
            }
        }

        return vo;
    }

    public int saveDeposit(Deposit deposit, String id, String userId) {
        int pre_money, money;

        CustomerProperty customerProperty = customerDao.getProperty(id, "remain_amount");
        if (customerProperty == null) {
            pre_money = 0;
            money = Integer.valueOf(deposit.getMoney()).intValue() * 10000;
            customerDao.dealCustomerInfo(id, "remain_amount", String.valueOf(money));
        } else {
            pre_money = Integer.valueOf(customerProperty.getPropertyValue()).intValue();
            money = Integer.valueOf(deposit.getMoney()).intValue() * 10000;
            customerDao.dealCustomerInfo(id, "remain_amount", String.valueOf((pre_money + money)));
        }
        String sql = "INSERT INTO am_pay (SUBSCRIBER_ID,MONEY,PAY_TIME,pay_certificate,pre_money,user_id) VALUE (?,?,?,?,?,?) ";

        jdbcTemplate.update(sql, id, money, DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss), deposit.getPicId(), pre_money, userId);
        return 0;
    }

    public Map<String, Object> depositList(PageParam page, String custId) {
        Map<String, Object> map = new HashMap<>();
        String sql = "select cust_id,property_name,property_value from t_customer_property   where cust_id=?";

        List<Map<String, Object>> propertyList = jdbcTemplate.queryForList(sql, custId);
        propertyList.stream().forEach(m -> {
            switch (m.get("property_name").toString()) {
                case "bank_account":
                    map.put("bank_account", m.get("property_value"));
                    break;
                case "remain_amount":
                    map.put("remain_amount", Integer.valueOf(m.get("property_value").toString()).intValue() / 10000);
                    break;
            }
        });
        int pageNum = 1;
        int pageSize = 10;
        try {
            pageNum = page.getPageNum();
        } catch (Exception e) {
        }
        try {
            pageSize = page.getPageSize();
        } catch (Exception e) {
        }
        if (pageNum <= 0)
            pageNum = 1;
        if (pageSize <= 0)
            pageSize = 10;
        if (pageSize > 10000)
            pageSize = 10000;

        String sql1 = "select pay.pay_id,pay.SUBSCRIBER_ID,pay.MONEY,pay.PAY_TIME,pay.pay_certificate,pay.pre_money,pay.user_id ,u.realname as realname from am_pay pay left join  t_customer_user u  on pay.user_id=u.id  where SUBSCRIBER_ID=? order by pay_time";
        List<Map<String, Object>> payList = jdbcTemplate.queryForList(sql1 + " limit " + (pageNum - 1) * pageSize + ", " + pageSize, custId);
        List<Deposit> depositList = new ArrayList<>();
        payList.stream().forEach(m -> {
            Deposit deposit = new Deposit();
            deposit.setCustId(m.get("SUBSCRIBER_ID").toString());
            deposit.setMoney(Integer.valueOf(m.get("MONEY").toString()).intValue() / 10000 + "");
            deposit.setPayTime(m.get("PAY_TIME").toString());
            deposit.setId(Integer.valueOf(m.get("pay_id").toString()));
            deposit.setPicId(m.get("pay_certificate").toString());
            deposit.setPreMoney(Integer.valueOf(m.get("pre_money").toString()).intValue() / 10000 + "");
            deposit.setUserId(m.get("user_id").toString());
            deposit.setRealname(m.get("realname") == null ? "" : m.get("realname").toString());
            depositList.add(deposit);
        });
        Customer customer = customerDao.get(custId);
        map.put("depositList", depositList);
        map.put("custName", customer.getEnterpriseName() == null ? "" : customer.getEnterpriseName());
        return map;
    }

}
