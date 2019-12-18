package com.bdaim.customer.service;

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
import com.bdaim.util.Constant;
import com.bdaim.util.DateUtil;
import com.bdaim.util.IDHelper;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
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
            saveAmApplication(vo, lu, customerId);
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
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "balance_warning_config", vo.getBalance_warning_config());
            } else {
                customerDao.dealCustomerInfo(customerId, "balance_warning_config", vo.getBalance_warning_config());
            }
        }
        //销售负责人
        //if (StringUtil.isNotEmpty(vo.getSalePerson())) {
        if (StringUtil.isNotEmpty(vo.getSalePerson())) {
            if (StringUtil.isNotEmpty(vo.getCustId())) {
                customerDao.dealCustomerInfo(vo.getCustId(), "sale_person", vo.getSalePerson());
            } else {
                customerDao.dealCustomerInfo(customerId, "sale_person", vo.getSalePerson());
            }
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

    public void saveAmApplication(CustomerRegistDTO vo, LoginUser lu, String customerId) {
        AmApplicationEntity entity = new AmApplicationEntity();
        entity.setCreateBy(lu.getName());
        entity.setCreateTime(new Timestamp(System.currentTimeMillis()));
        entity.setStatus("APPROVED");
        entity.setName("DefaultApplication");
        entity.setTier("Unlimited");
        entity.setSubscriberId(Long.valueOf(customerId));
        amApplicationDao.saveOrUpdate(entity);
    }


    public Map<String, Object> getUser(PageParam page, String customerId, String account, String name, String contactPerson, String salePerson) {
        StringBuffer sql = new StringBuffer();
        sql.append("SELECT  CAST(s.id AS CHAR) id,s.cust_id as custId,s.user_type, s.account AS account,s.password AS PASSWORD,tc.real_name AS contactPerson,tc.title as title,tc.enterprise_name as name" +
                " FROM t_customer_user s LEFT JOIN t_customer tc ON s.cust_id=tc.cust_id  " +
                " WHERE 1=1  AND s.STATUS <> 2");
        if (StringUtil.isNotEmpty(account)) {
            logger.info(account);
            sql.append(" AND s.account = '" + account + "'");
        }
        if (StringUtil.isNotEmpty(contactPerson)) {
            sql.append(" AND tc.real_name like '%" + contactPerson + "%'");
        }
        if (StringUtil.isNotEmpty(name)) {
            sql.append(" AND tc.enterprise_name like '%" + name + "%'");
        }
        sql.append(" order by s.create_time desc");
        logger.info("sql:{" + sql + "}");
        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        Object collect = list.getList().stream().map(m -> {
            Map map = (Map) m;
            logger.info("Map:{" + map + "}");
            if (StringUtil.isEmpty(map.get("custId").toString())) {
                return map;
            }
            String cust_id = map.get("custId").toString();
            CustomerProperty mobile = customerDao.getProperty(cust_id, "mobile");
            if (mobile != null) {
                logger.info("mobile:{"+mobile+"}");
                map.put("mobile", mobile.getPropertyValue());
            }
            CustomerProperty sale_person = customerDao.getProperty(cust_id, "sale_person");
            if (sale_person != null) {
                logger.info("sale_person:{"+sale_person+"}");
                map.put("salePerson", sale_person.getPropertyValue());
            }
            CustomerProperty remain_amount = customerDao.getProperty(cust_id, "remain_amount");
            CustomerProperty used_amount = customerDao.getProperty(cust_id, "used_amount");
            if (remain_amount != null) {
                logger.info("remain_amount:{"+remain_amount+"}");
                map.put("remainAmount", StringUtil.isEmpty(remain_amount.getPropertyValue()) ? "0" : String.valueOf(Integer.valueOf(remain_amount.getPropertyValue()) / 10000));
            }
            if (used_amount != null) {
                logger.info("used_amount:{"+used_amount+"}");
                map.put("userAmount", StringUtil.isEmpty(used_amount.getPropertyValue()) ? "0" : String.valueOf(Integer.valueOf(used_amount.getPropertyValue()) / 10000));
            }
            return map;
        }).collect(Collectors.toList());
        Map<String, Object> map = new HashMap<>();
        map.put("data", collect);
        map.put("total", list.getTotal());
        return map;
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
            String property_value = ObjectFormStr(map.get("property_value"));
            switch (map.get("property_name").toString()) {
                case "province":
                    vo.setProvince(property_value);
                    break;
                case "city":
                    vo.setCity(property_value);
                case "county":
                    vo.setCountry(property_value);
                case "whiteIps":
                    vo.setWhiteIps(property_value);
                    break;
                case "taxpayer_id":
                    vo.setTaxPayerId(property_value);
                    break;
                case "bli_path":
                    vo.setBliPath(property_value);
                    break;
                case "bank":
                    vo.setBank(property_value);
                    break;
                case "bank_account":
                    vo.setBankAccount(property_value);
                    break;
                case "bank_account_certificate":
                    vo.setBankAccountCertificate(property_value);
                    break;
                case "sale_person":
                    vo.setAddress(property_value);
                    break;
                case "reg_address":
                    vo.setSalePerson(property_value);
                    break;
                case "mobile":
                    vo.setMobile(property_value);
                    break;
                case "brand":
                    vo.setBrand(property_value);
                    break;
                case "create_id":
                    vo.setCreateId(property_value);
                    break;
                case "balance_warning_config":
                    vo.setBalance_warning_config(property_value);
                    break;
                case "inten_industry":
                    vo.setIntenIndustry(property_value);
                    break;
                case "industry_picture_value":
                    vo.setIndustryPictureValue(property_value);
                    break;
                case "industry":
                    vo.setIndustry(property_value);
                    break;
            }
        }
        return vo;
    }

    public String ObjectFormStr(Object obj) {
        if (obj != null) {
            return obj.toString();
        }
        return "";
    }

    public int saveDeposit(Deposit deposit, String id, String userId) {
//        BigDecimal b = new BigDecimal(10000);
//        BigDecimal pre_money;
////        int money = Integer.valueOf((Double.valueOf(deposit.getMoney())* 10000) + "".trim()).intValue();
//        BigDecimal bigDecimal = BigDecimal.valueOf(Double.valueOf(deposit.getMoney()));
//        BigDecimal money = bigDecimal.multiply(b);
        int money = Integer.valueOf(deposit.getMoney()) * 10000;
        int pre_money = 0;
        CustomerProperty customerProperty = customerDao.getProperty(id, "remain_amount");
        if (customerProperty == null) {
            customerDao.dealCustomerInfo(id, "remain_amount", String.valueOf(money));
        } else {
//            BigDecimal bigDecimal1 = new BigDecimal(Double.valueOf(customerProperty.getPropertyValue()));
            pre_money = Integer.valueOf(customerProperty.getPropertyValue());
            customerDao.dealCustomerInfo(id, "remain_amount", String.valueOf(pre_money + money));
        }
        String sql = "INSERT INTO am_pay (SUBSCRIBER_ID,MONEY,PAY_TIME,pay_certificate,pre_money,user_id) VALUE (?,?,?,?,?,?) ";

        jdbcTemplate.update(sql, Long.valueOf(id), money, DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss), deposit.getPicId(), pre_money, userId);
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

        String sql1 = "select pay.pay_id,pay.SUBSCRIBER_ID,pay.MONEY,pay.PAY_TIME,pay.pay_certificate,pay.pre_money,pay.user_id ,u.name as account from am_pay pay left join  t_user u  on pay.user_id=u.id  where SUBSCRIBER_ID = " + custId;
        page.setSort("pay.pay_time");
        page.setDir(" desc");
        PageList list = new Pagination().getPageData(sql1, null, page, jdbcTemplate);
        List<Deposit> depositList = new ArrayList<>();
        list.getList().stream().forEach(m -> {
            Map depositMap = (Map) m;
            Deposit deposit = new Deposit();
            if (depositMap.get("SUBSCRIBER_ID") != null) {
                deposit.setCustId(depositMap.get("SUBSCRIBER_ID").toString());
            }
            if (depositMap.get("MONEY") != null) {
                deposit.setMoney(Integer.valueOf(depositMap.get("MONEY").toString()).intValue() / 10000 + "");
            }
            if (depositMap.get("PAY_TIME") != null) {
                deposit.setPayTime(depositMap.get("PAY_TIME").toString());
            }
            if (depositMap.get("pay_id") != null) {
                deposit.setId(Integer.valueOf(depositMap.get("pay_id").toString()));
            }
            if (depositMap.get("pay_certificate") != null) {
                deposit.setPicId(depositMap.get("pay_certificate").toString());
            }
            if (depositMap.get("pre_money") != null) {
                deposit.setPreMoney(Integer.valueOf(depositMap.get("pre_money").toString()).intValue() / 10000 + "");
            }
            if (depositMap.get("user_id") != null) {
                deposit.setUserId(depositMap.get("user_id").toString());
            }
            if (depositMap.get("realname") != null) {
                deposit.setRealname(depositMap.get("realname").toString());
            }
            if (depositMap.get("account") != null) {
                deposit.setAccount(depositMap.get("account").toString());
            }
            depositList.add(deposit);
        });
        Customer customer = customerDao.get(custId);
        map.put("depositList", depositList);
        map.put("custName", customer.getEnterpriseName() == null ? "" : customer.getEnterpriseName());
        map.put("total", list.getTotal());
        return map;
    }

    public List apps(String customerId) {
    	List data = jdbcTemplate.queryForList("select name,application_id as appId,access_token as token,VALIDITY_PERIOD as tokenPeriod,date_format(TOKEN_TIME_CREATED,'%Y-%m-%d %H:%i:%s') as tokenTime from am_application where SUBSCRIBER_ID=? ", customerId);
    	return data;
    }
    public Map getApp(String appId) {
    	Map app = null;
    	List<Map<String,Object>> data = jdbcTemplate.queryForList("select name,subscriber_id as customerId,access_token as token,VALIDITY_PERIOD as tokenPeriod,date_format(TOKEN_TIME_CREATED,'%Y-%m-%d %H:%i:%s') as tokenTime from am_application where application_id=? ", appId);
    	if(data.size()>0)
    		app = data.get(0);
    	return app;
    }
    public String reAppToken(String appId) {
    	String token = UUID.randomUUID().toString().replace("-", "");
    	jdbcTemplate.update("update am_application set access_token=? where application_id=?", token, appId);
    	
    	return token;
    }
}
