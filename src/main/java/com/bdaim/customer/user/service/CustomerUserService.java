package com.bdaim.customer.user.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.callcenter.dto.CallPriceConfig;
import com.bdaim.callcenter.dto.XzCallcenterSeatParam;
import com.bdaim.callcenter.dto.XzCompanyCallcenterParam;
import com.bdaim.callcenter.service.impl.SeatsService;
import com.bdaim.callcenter.util.XzCallCenterUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.common.entity.Dic;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dao.CustomerUserPropertyDao;
import com.bdaim.customer.dto.*;
import com.bdaim.customer.entity.Customer;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.user.dto.UserCallConfigDTO;
import com.bdaim.industry.dto.MarketResourceTypeEnum;
import com.bdaim.label.dto.LabelDataPriceConfig;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.smscenter.dto.SmsPriceConfig;
import com.bdaim.supplier.fund.entity.FundProductApply;
import com.bdaim.util.*;
import com.bdaim.util.wechat.WeChatUtil;
import com.bdaim.common.dao.DicDao;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.*;

@Service("customerUserService")
@Transactional
public class CustomerUserService {

    public static final Logger logger = LoggerFactory.getLogger(CustomerUserService.class);

    @Resource
    private CustomerUserDao customerUserDao;

    @Resource
    private CustomerDao customerDao;

    @Resource
    UserDao userDao;

    @Resource
    JdbcTemplate jdbcTemplate;

    @Resource
    MarketResourceDao marketResourceDao;

    @Resource
    private TransactionService transactionService;

    @Resource
    private SeatsService seatsService;

    @Resource
    private DicDao dicDao;

    @Resource
    private WeChatUtil weChatUtil;

    @Resource
    private CustomerUserPropertyDao customerUserPropertyDao;


    /**
     * 默认支付密码
     */
    private final static String DEFAULT_PAY_PASSWORD = "123456";

    /**
     * 默认支付密码等级
     */
    private final static String DEFAULT_PAY_LEVEL = "1";

    /**
     * 保存前台注册用户
     *
     * @param account
     * @param password
     * @param custId
     * @param userType
     * @param realname
     * @param phone
     * @param client
     * @param area
     * @param channel
     * @param registerSource
     * @param touchType
     * @return
     */
    public CustomerUser saveCustomerUser(String account, String password, String custId, int userType, String realname,
                                         String phone, String client, String area, String channel, String registerSource, String touchType) {
        Timestamp nowTime = new Timestamp(System.currentTimeMillis());
        CustomerUser u = new CustomerUser();
        u.setId(IDHelper.getUserID());
        u.setRealname(realname);
        u.setUserType(userType);
        if (StringUtil.isEmpty(password)) {
            password = "aa123456";
        }
        password = CipherUtil.generatePassword(password);
        u.setAccount(account);
        u.setPassword(password);
        u.setStatus(0);
        u.setCust_id(custId);
        u.setCreateTime(String.valueOf(nowTime));
        customerUserDao.saveOrUpdate(u);
        // 手机号
        if (StringUtil.isNotEmpty(phone)) {
            CustomerUserPropertyDO mobileNum = new CustomerUserPropertyDO(String.valueOf(u.getId()), "mobile_num", phone, nowTime);
            customerUserDao.saveOrUpdate(mobileNum);
        }
        // 产品端
        if (StringUtil.isNotEmpty(client)) {
            CustomerUserPropertyDO c = new CustomerUserPropertyDO(String.valueOf(u.getId()), "client", client, nowTime);
            customerUserDao.saveOrUpdate(c);
        }
        // 所在区域
        if (StringUtil.isNotEmpty(area)) {
            CustomerUserPropertyDO c = new CustomerUserPropertyDO(String.valueOf(u.getId()), "area", area, nowTime);
            customerUserDao.saveOrUpdate(c);
        }
        // 推广渠道名称
        if (StringUtil.isNotEmpty(channel)) {
            CustomerUserPropertyDO c = new CustomerUserPropertyDO(String.valueOf(u.getId()), "channel", channel, nowTime);
            customerUserDao.saveOrUpdate(c);
        }
        // 注册来源
        if (StringUtil.isNotEmpty(registerSource)) {
            CustomerUserPropertyDO c = new CustomerUserPropertyDO(String.valueOf(u.getId()), "registerSource", registerSource, nowTime);
            customerUserDao.saveOrUpdate(c);
        }
        // 触达方式
        if (StringUtil.isNotEmpty(touchType)) {
            CustomerUserPropertyDO c = new CustomerUserPropertyDO(String.valueOf(u.getId()), "touchType", touchType, nowTime);
            customerUserDao.saveOrUpdate(c);
        }
        return u;
    }

    public CustomerUser getUserByAccount(String account) {
        return customerUserDao.findUniqueBy("account", account);
    }

    public CustomerUserPropertyDO getUserBymobileNum(String mobileNum) {
        String hql = "from CustomerUserPropertyDO m where m.propertyName='mobile_num' and propertyValue=?";
        List<CustomerUserPropertyDO> list = this.customerUserDao.find(hql, mobileNum);
        if (list.size() > 0)
            return list.get(0);
        return null;
    }

    public CustomerUserPropertyDO getUserByEmail(String email) {
        String hql = "from CustomerUserPropertyDO m where m.propertyName='email' and propertyValue=?";
        List<CustomerUserPropertyDO> list = this.customerUserDao.find(hql, email);
        if (list.size() > 0)
            return list.get(0);
        return null;
    }


    public void customerRegister(CustomerRegistDTO value) throws Exception {
        Customer customer = new Customer();
        String customerId = IDHelper.getID().toString();
//        //创建默认账户信息
//        AccountDO accountDO = new AccountDO();
//        accountDO.setAcctId(IDHelper.getID().toString());
//        accountDO.setCustId(customerId);
//        //创建默认账户时余额和已经使用的默认为0
//        accountDO.setRemainAmount(0);
//        accountDO.setUsedAmount(0);
//        accountDO.setStatus(Constant.ACCOUT_ACTIVE_STATUS);
//        accountDO.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
//        accountDao.save(accountDO);
//        //创建客户登陆信息
//        User userDO = new User();
//        //1企业客户 2 操作员
//        userDO.setUser_type("1");
//        userDO.setId(IDHelper.getUserID());
//        userDO.setCustId(customerId);
//        userDO.setName(value.getUserId());
//        userDO.setPassword(CipherUtil.generatePassword(value.getPassword()));
//        userDO.setRealname(value.getRealName());
//        userDO.setEnterprise_name(value.getEnterpriseName());
//        userDO.setMobileNum(value.getMobile());
//        userDO.setEmail(value.getEmail());
//        userDO.setTitle(value.getTitle());
//        userDO.setStatus(Constant.USER_ACTIVE_STATUS);
//        userDO.setUserPwdLevel(value.getUserPwdLevel());
//        userInfoDao.save(userDO);
        //创建客户信息
        customer.setCustId(customerId);
        customer.setRealName(value.getRealName());
        customer.setTitle(value.getTitle());
        customer.setEnterpriseName(value.getEnterpriseName());
        customer.setStatus(Constant.USER_ACTIVE_STATUS);
        customer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
        customerDao.save(customer);

        CustomerUser cu = new CustomerUser();
        cu.setCust_id(customerId);
        cu.setAccount(value.getUserName());
        cu.setPassword(CipherUtil.generatePassword(value.getPassword()));
        cu.setUserType(1);
        cu.setStatus(Constant.USER_ACTIVE_STATUS);
        cu.setId(IDHelper.getUserID());
        cu.setRealname(value.getRealName());
        customerUserDao.save(cu);

        customerDao.saveOrUpdate(new CustomerProperty(customerId, "city", value.getCity()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "province", value.getProvince()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "country", value.getCounty()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "address", value.getAddress()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "bliNumber", value.getBliNumber()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "bliPath", value.getBliPath()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "taxPayerNum", value.getTaxPayerId()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "taxpayerCertificatePath", value.getTaxpayerCertificatePath()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "bank", value.getBank()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "bankAccount", value.getBankAccount()));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "bankAccountCertificate", value.getBankAccountCertificate()));
        // 默认支付密码和支付密码等级,余额为0
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "pay_password", CipherUtil.generatePassword(DEFAULT_PAY_PASSWORD), new Timestamp(System.currentTimeMillis())));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "pwd_status", DEFAULT_PAY_LEVEL, new Timestamp(System.currentTimeMillis())));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "remain_amount", "0", new Timestamp(System.currentTimeMillis())));
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "used_amount", "0", new Timestamp(System.currentTimeMillis())));
        // 所属行业
        //customerDao.saveOrUpdate(new CustomerProperty(customerId, "industryId", value.getIndustryId()));

        //服务模式 1:CRM 2:营销任务
        customerDao.saveOrUpdate(new CustomerProperty(customerId, "service_mode", value.getServiceMode()));

        // 处理用户自建属性
        customerUserDao.saveOrUpdate(new CustomerUserPropertyDO(String.valueOf(cu.getId()), "email", value.getEmail(), new Timestamp(System.currentTimeMillis())));
        customerUserDao.saveOrUpdate(new CustomerUserPropertyDO(String.valueOf(cu.getId()), "mobile_num", value.getMobile(), new Timestamp(System.currentTimeMillis())));
        customerUserDao.saveOrUpdate(new CustomerUserPropertyDO(String.valueOf(cu.getId()), "title", value.getTitle(), new Timestamp(System.currentTimeMillis())));

        // 结算类型
        customerUserDao.saveOrUpdate(new CustomerUserPropertyDO(String.valueOf(cu.getId()), "settlement_type", "1", new Timestamp(System.currentTimeMillis())));
        // 营销类型:1-B2C营销  2-B2B营销
        customerDao.saveOrUpdate(new CustomerProperty(customerId, CustomerPropertyEnum.MARKET_TYPE.getKey(), String.valueOf(value.getMarketingType())));
//        //创建企业信息
//        EnterpriseDO enterpriseDO = new EnterpriseDO();
//        enterpriseDO.setCustId(customerId);
//        enterpriseDO.setEnterpriseId(IDHelper.getID().toString());
//        enterpriseDO.setName(value.getEnterpriseName());
//        enterpriseDO.setProvince(value.getProvince());
//        enterpriseDO.setCity(value.getCity());
//        enterpriseDO.setCounty(value.getCounty());
//        enterpriseDO.setBliNumber(value.getBliNumber());
//        enterpriseDO.setBliPath(value.getBliPath());
//        enterpriseDO.setTaxpayerId(value.getTaxPayerId());
//        enterpriseDO.setTaxpayerCertificatePath(value.getTaxpayerCertificatePath());
//        enterpriseDO.setBank(value.getBank());
//        enterpriseDO.setBankAccount(value.getBankAccount());
//        enterpriseDO.setBankAccountCertificate(value.getBankAccountCertificate());
//        enterpriseDO.setStatus(Constant.ENTERPRISE_ACTIVE_STATUS);
//        enterpriseDao.save(enterpriseDO);
    }

    public CustomerUser getUserByName(String name) {
        String hql = "from CustomerUser m where m.account=? and m.status=0";
        CustomerUser m = userDao.findUnique(hql, name);
        return m;
    }


    public synchronized Integer addUser(UserCallConfigDTO userDTO, String enpterprise_name) throws RuntimeException {

        String id = userDTO.getId();
        String custId = userDTO.getCustomerId();
        String userName = userDTO.getUserName();
        String realName = userDTO.getRealName();
        String password = userDTO.getPassword();
        //String appId = userDTO.getAppId();
//        String callCenterId = userDTO.getCallCenterId();
        String callType = userDTO.getCallType();
        String callChannel = userDTO.getCallChannel();
        Integer userType = userDTO.getUserType();
        if (userType == null) userType = 2;

        try {
            CustomerUser user = customerUserDao.getCustomerUserByLoginName(userName);
            if (user != null) {
                logger.error("账号" + userDTO.getUserName() + "已存在");
                throw new Exception("账号" + userDTO.getUserName() + "已存在");
            }

            if ("2".equals(userType.toString())) {
                if (StringUtil.isEmpty(callType)) {
                    throw new Exception("请选择呼叫类型");
                }
                if (StringUtil.isEmpty(callChannel)) {
                    throw new Exception("请选择呼叫渠道");
                }

                CustomerProperty remain_amount = customerDao.getProperty(custId, "remain_amount");
                if (remain_amount == null) {
                    logger.info("客户" + custId + " 添加坐席余额不足");
                    throw new TouchException("余额不足");
                }
                String remainAmountStr = remain_amount.getPropertyValue();
                BigDecimal remainAmount = BigDecimal.ZERO;
                if (!StringUtil.isEmpty(remainAmountStr)) {
                    remainAmount = new BigDecimal(remainAmountStr);
                }
                CallPriceConfig cpc = selectCustCallResourceConfig(custId, callChannel);
                if (cpc == null) {
                    logger.info("客户" + custId + "未配置此通话资源,callChannel:" + callChannel);
                    throw new TouchException("客户" + custId + "未配置此通话资源,callChannel:" + callChannel);
                }
                if (cpc.getSeat_month_price() == null) {
                    logger.info("客户[" + custId + "],渠道:[" + callChannel + "]未设置坐席售价");
                    throw new TouchException("客户[" + custId + "],渠道:[" + callChannel + "]未设置坐席售价");
                }
                double seatMonthPrice = cpc.getSeat_month_price();
                if (remainAmount.compareTo(new BigDecimal(seatMonthPrice)) < 0) {
                    logger.info("客户" + custId + " 添加坐席余额不足");
                    throw new TouchException("客户余额不足");
                }
                if (StringUtil.isNotEmpty(userDTO.getAddAgentMethod()) && "1".equals(userDTO.getAddAgentMethod())) { //1 api方式添加
                    CustomerProperty xz_callcenter_id = customerDao.getProperty(custId, "xz_callcenter_id");
                    if (xz_callcenter_id == null) {
                        XzCompanyCallcenterParam param = new XzCompanyCallcenterParam();
                        Customer customer = customerDao.get(custId);
                        param.setCompid(String.valueOf(850000 + customer.getCompId()));
                        param.setAmountagentauth(500);
                        param.setMaxconcurrentnumber(500);
                        param.setExpirerecord(6);
                        param.setEnable(1);
                        param.setCompanyname(customer.getEnterpriseName());
                        param.setBegintime(DateUtil.fmtDateToStr(new Date(), "yyyy-MM-dd HH:mm:ss"));
                        param.setEndtime("3099-12-31 00:00:00");
                        JSONObject json = XzCallCenterUtil.addCompanytoXzCallCenter(param);
                        if (json != null && "0".equals(json.getString("code"))) {
                            saveXZCallcenterProperty(custId, param);
                        } else {
                            throw new Exception("创建呼叫中心企业账号失败");
                        }
                    }
                    JSONObject json = sendAddagent(userDTO);
                    if (json == null || !"0".equals(json.getString("code"))) {
                        throw new TouchException("添加座席失败");
                    }
                }
            }

            CustomerUser cu = new CustomerUser();
            cu.setId(Long.valueOf(id));
            cu.setAccount(userName);
            cu.setRealname(realName);
            if (null == password || "".equals(password)) {
                password = "123456";
            }
            password = CipherUtil.generatePassword(password);
            cu.setPassword(password);
            cu.setStatus(0);
            cu.setCust_id(custId);
            cu.setUserType(userType); //2:添加普通员工 3:项目管理员
            cu.setCreateTime(String.valueOf(new Timestamp(System.currentTimeMillis())));
            this.customerUserDao.save(cu);

            List<CustomerUserPropertyDO> list = new ArrayList<>();
            CustomerUserPropertyDO mobile_num = new CustomerUserPropertyDO(cu.getId().toString(), "mobile_num", userDTO.getMobileNumber(), new Timestamp(System.currentTimeMillis()));
            CustomerUserPropertyDO email = new CustomerUserPropertyDO(cu.getId().toString(), "email", userDTO.getEmail(), new Timestamp(System.currentTimeMillis()));
            CustomerUserPropertyDO title = new CustomerUserPropertyDO(cu.getId().toString(), "title", userDTO.getTitle(), new Timestamp(System.currentTimeMillis()));
            if ("2".equals(userType.toString())) {
                CustomerUserPropertyDO work_num = new CustomerUserPropertyDO(cu.getId().toString(), "work_num", userDTO.getWorkNum(), new Timestamp(System.currentTimeMillis()));
                CustomerUserPropertyDO seats_account = new CustomerUserPropertyDO(cu.getId().toString(), "seats_account", userDTO.getSeatsAccount(), new Timestamp(System.currentTimeMillis()));
                CustomerUserPropertyDO seats_password = new CustomerUserPropertyDO(cu.getId().toString(), "seats_password", userDTO.getSeatsPassword(), new Timestamp(System.currentTimeMillis()));
                CustomerUserPropertyDO extension_number = new CustomerUserPropertyDO(cu.getId().toString(), "extension_number", userDTO.getExtensionNumber(), new Timestamp(System.currentTimeMillis()));
                CustomerUserPropertyDO extension_password = new CustomerUserPropertyDO(cu.getId().toString(), "extension_password", userDTO.getExtensionPassword(), new Timestamp(System.currentTimeMillis()));
                CustomerUserPropertyDO call_type = new CustomerUserPropertyDO(cu.getId().toString(), "call_type", callType.trim(), new Timestamp(System.currentTimeMillis()));
                CustomerUserPropertyDO call_channel = new CustomerUserPropertyDO(cu.getId().toString(), "call_channel", callChannel, new Timestamp(System.currentTimeMillis()));
                if ("1".equals(userDTO.getAddAgentMethod())) {//api方式添加座席
                    CustomerUserPropertyDO add_agent_method = new CustomerUserPropertyDO(cu.getId().toString(), "add_agent_method", userDTO.getAddAgentMethod(), new Timestamp(System.currentTimeMillis()));
                    list.add(add_agent_method);
                }
                list.add(work_num);
                list.add(seats_account);
                list.add(seats_password);
                list.add(extension_number);
                list.add(extension_password);
                list.add(call_type);
                list.add(call_channel);
            } else if ("3".equals(userType.toString())) { //项目管理员需要添加所分配的项目
                String hasMarketProjectStr = userDTO.getHasMarketProject();
                if (StringUtil.isNotEmpty(hasMarketProjectStr)) {
                    hasMarketProjectStr = "," + hasMarketProjectStr + ",";
                }
                CustomerUserPropertyDO hasMarketProject = new CustomerUserPropertyDO(cu.getId().toString(), "hasMarketProject", hasMarketProjectStr, new Timestamp(System.currentTimeMillis()));
                list.add(hasMarketProject);
            }
            list.add(mobile_num);
            list.add(email);
            list.add(title);
            /* // 坐席授权企业开通的服务权限
            CustomerProperty permission = customerDao.getProperty(custId, "service_mode");
            if (permission != null) {
                CustomerUserProperty userPermission = new CustomerUserProperty(cu.getId().toString(), "service_mode", permission.getPropertyValue(), new Timestamp(System.currentTimeMillis()));
                list.add(userPermission);
            }*/
            this.customerUserDao.batchSaveOrUpdate(list);

            if ("2".equals(userType.toString())) {
                seatMonthDeduction(custId, "", userDTO.getId());
            }
        } catch (Exception e) {
            logger.error("保存操作员出错:", e);
            if ("1".equals(userDTO.getAddAgentMethod())) {
                try {
                    delAgent(custId, userDTO.getSeatsAccount());
                } catch (Exception e1) {
                    e1.printStackTrace();
                }
            }
            throw new RuntimeException(e.getMessage(), e);
            //return 0;
        }
        return 1;
    }


    /**
     * 添加讯众户籍中心账号
     *
     * @param custId
     * @param param
     */
    public void saveXZCallcenterProperty(String custId, XzCompanyCallcenterParam param) {
        List<CustomerProperty> properties = new ArrayList<>();
        Timestamp timestamp = new Timestamp(System.currentTimeMillis());
        CustomerProperty xz_callcenterid = new CustomerProperty();
        xz_callcenterid.setCustId(custId);
        xz_callcenterid.setPropertyName("xz_callcenterid");
        xz_callcenterid.setCreateTime(timestamp);
        properties.add(xz_callcenterid);

        CustomerProperty xz_startTime = new CustomerProperty();
        xz_startTime.setPropertyName("xz_beginTime");
        xz_startTime.setPropertyValue(param.getBegintime());
        xz_startTime.setCustId(custId);
        xz_startTime.setCreateTime(timestamp);
        properties.add(xz_startTime);

        CustomerProperty xz_endTime = new CustomerProperty();
        xz_endTime.setPropertyName("xz_endTime");
        xz_endTime.setPropertyValue(param.getEndtime());
        xz_endTime.setCreateTime(timestamp);
        xz_endTime.setCustId(custId);
        properties.add(xz_endTime);

        CustomerProperty xz_account = new CustomerProperty();
        xz_account.setPropertyName("xz_account");
        xz_account.setPropertyValue(param.getAccount());
        xz_account.setCustId(custId);
        xz_account.setCreateTime(timestamp);
        properties.add(xz_account);

        CustomerProperty xz_pwd = new CustomerProperty();
        xz_pwd.setPropertyName("xz_pwd");
        xz_pwd.setPropertyValue(param.getPwd());
        xz_pwd.setCreateTime(timestamp);
        xz_pwd.setCustId(custId);
        properties.add(xz_pwd);

        CustomerProperty xz_amountagentauth = new CustomerProperty();
        xz_amountagentauth.setCustId(custId);
        xz_amountagentauth.setCreateTime(timestamp);
        xz_amountagentauth.setPropertyName("xz_amountagentauth");
        xz_amountagentauth.setPropertyValue("" + param.getAmountagentauth());
        properties.add(xz_amountagentauth);

        CustomerProperty xz_maxconcurrentnumber = new CustomerProperty();
        xz_maxconcurrentnumber.setPropertyName("xz_maxconcurrentnumber");
        xz_maxconcurrentnumber.setPropertyValue(param.getMaxconcurrentnumber() + "");
        xz_maxconcurrentnumber.setCustId(custId);
        xz_maxconcurrentnumber.setCreateTime(timestamp);
        properties.add(xz_maxconcurrentnumber);

        CustomerProperty xz_expirerecord = new CustomerProperty();
        xz_expirerecord.setPropertyName("xz_expirerecord");
        xz_expirerecord.setPropertyValue(param.getExpirerecord() + "");
        xz_expirerecord.setCreateTime(timestamp);
        xz_expirerecord.setCustId(custId);
        properties.add(xz_expirerecord);

        CustomerProperty xz_enable = new CustomerProperty();
        xz_enable.setPropertyName("xz_enable");
        xz_enable.setPropertyValue(param.getEnable() + "");
        xz_enable.setCustId(custId);
        xz_enable.setCreateTime(timestamp);
        properties.add(xz_enable);
        customerDao.batchSaveOrUpdate(properties);
    }

    private JSONObject sendAddagent(UserCallConfigDTO userDTO) throws Exception {
        XzCallcenterSeatParam param = new XzCallcenterSeatParam();
        param.setAgentid(userDTO.getSeatsAccount());
        param.setAgentpwd(userDTO.getSeatsPassword());
        param.setAgentrole(2);

        CustomerProperty xz_callcenter_id = customerDao.getProperty(userDTO.getCustomerId(), "xz_callcenter_id");
        if (xz_callcenter_id == null || StringUtil.isEmpty(xz_callcenter_id.getPropertyValue())) {
            throw new TouchException("未开通呼叫中心账号");
        }
        param.setCompid(xz_callcenter_id.getPropertyValue());
        param.setExtpwd(userDTO.getExtensionPassword());
        param.setShownumber(userDTO.getShowNumber());
        JSONObject json = null;
        try {
            json = XzCallCenterUtil.addAgent(param);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("添加座席失败");
        }
        logger.info("add agent result:" + json.toJSONString());
        if (!"0".equals(json.getString("code"))) {
            throw new ParamException("操作失败");
        }
        return json;
    }

    /**
     * 修改座席信息，修改分机密码
     *
     * @param userDTO
     * @return
     * @throws Exception
     */
    private JSONObject modifyAgent(UserCallConfigDTO userDTO) throws Exception {
        XzCallcenterSeatParam param = new XzCallcenterSeatParam();
        param.setAgentid(userDTO.getSeatsAccount());
        param.setAgentpwd(userDTO.getSeatsPassword());
        param.setAgentrole(2);


        CustomerProperty xz_callcenter_id = customerDao.getProperty(userDTO.getCustomerId(), "xz_callcenter_id");
        if (xz_callcenter_id == null || StringUtil.isEmpty(xz_callcenter_id.getPropertyValue())) {
            throw new TouchException("未开通呼叫中心账号");
        }
        param.setCompid(xz_callcenter_id.getPropertyValue());
        param.setAgentpwd(userDTO.getSeatsPassword());
        param.setShownumber(userDTO.getShowNumber());
        JSONObject json = null;
        try {
            logger.info("更新座席信息。。。");
            json = XzCallCenterUtil.modifyAgent(param);
            logger.info("modify agent result:" + json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("修改座席失败");
        }
        logger.info("更新分机密码...");
        param.setExtpwd(userDTO.getExtensionPassword());
        json = XzCallCenterUtil.modifyextAgent(param);

        logger.info("modify extpwd result:" + json.toJSONString());

        if (!"0".equals(json.getString("code"))) {
            throw new ParamException("操作失败");
        }
        return json;
    }

    public Integer deleteUser(String custId, String userName, Integer status) throws TouchException {

        StringBuffer sb = new StringBuffer();

        sb.append("UPDATE t_customer_user SET ");
        sb.append(" STATUS = ? ");
//        if (null != status && 1 == status) {
//            sb.append(", active_time =  NOW() ");
//        } else {
//            sb.append(", modify_time = NOW() ");
//        }
        if (1 == status) {
            sb.append(",locked_time=now() ");
        } else {
            sb.append(",locked_time=null");
        }
        sb.append(" where  account= ? and cust_id='" + custId + "'");

        int code = jdbcTemplate.update(sb.toString(), new Object[]{status, userName});
        /*CustomerUser user = customerUserDao.getCustomerUserByLoginName(userName);

        if(user!=null && user.getStatus()==0){//当账号有效时处理
            CustomerUserProperty add_agent_method =  customerUserDao.getProperty(user.getId().toString(),"add_agent_method");
            if(add_agent_method!=null && "1".equals(add_agent_method.getPropertyValue())){
                CustomerUserProperty seat_account = customerUserDao.getProperty(user.getId().toString(),"seats_account");
                if(seat_account!=null && StringUtil.isNotEmpty(seat_account.getPropertyValue())){
                    try {
                        delAgent(custId,seat_account.getPropertyValue());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }*/
        return code;
    }

    /**
     * @param custId  企业id
     * @param agentId 座席号
     * @throws Exception
     */
    private void delAgent(String custId, String agentId) throws Exception {
        CustomerProperty xz_callcenter_id = customerDao.getProperty(custId, "xz_callcenter_id");
        if (xz_callcenter_id == null || StringUtil.isEmpty(xz_callcenter_id.getPropertyValue())) {
            throw new TouchException("未开通呼叫中心账号");
        }
        XzCallCenterUtil.delAgent(xz_callcenter_id.getPropertyValue(), agentId);
    }


    public Integer updateuser(UserCallConfigDTO userDTO) {
        String Id = userDTO.getId();
        String realName = userDTO.getRealName();
        try {
            CustomerUser cu = this.customerUserDao.get(Long.valueOf(Id));
            if (cu != null) {
                if (StringUtil.isNotEmpty(userDTO.getPassword())) {
                    String password = CipherUtil.generatePassword(userDTO.getPassword());
                    cu.setPassword(password);
                }
                if (StringUtil.isNotEmpty(realName)) {
                    cu.setRealname(realName);
                }
                if (StringUtil.isNotEmpty(userDTO.getPassword()) || StringUtil.isNotEmpty(realName)) {
                    this.customerUserDao.save(cu);
                }
            } else {
                logger.info("用户 " + Id + " 不存在");
                return 0;
            }
            List<CustomerUserPropertyDO> list = new ArrayList<>();
            if (userDTO.getMobileNumber() != null) {
                CustomerUserPropertyDO mobile_num = this.customerUserDao.getProperty(Id, "mobile_num");
                if (mobile_num == null)
                    mobile_num = new CustomerUserPropertyDO(Id.toString(), "mobile_num", "", new Timestamp(System.currentTimeMillis()));
                mobile_num.setPropertyValue(userDTO.getMobileNumber());
                list.add(mobile_num);
            }
            if (userDTO.getTitle() != null) {
                CustomerUserPropertyDO title = this.customerUserDao.getProperty(Id, "title");
                if (title == null)
                    title = new CustomerUserPropertyDO(Id, "title", "", new Timestamp(System.currentTimeMillis()));
                title.setPropertyValue(userDTO.getTitle());
                list.add(title);
            }
            if (userDTO.getEmail() != null) {
                CustomerUserPropertyDO email = this.customerUserDao.getProperty(Id, "email");
                if (email == null)
                    email = new CustomerUserPropertyDO(Id, "email", "", new Timestamp(System.currentTimeMillis()));
                email.setPropertyValue(userDTO.getEmail());
                list.add(email);
            }

            //只有非api创建的座席才允许编辑座席相关信息
            CustomerUserPropertyDO add_agent_method = customerUserDao.getProperty(Id, "add_agent_method");
            if (add_agent_method == null || !"1".equals(add_agent_method.getPropertyValue())) {
                if ("call_center".equals(userDTO.getCallType())) {
                    if (userDTO.getSeatsAccount() != null) {
                        CustomerUserPropertyDO seats_account = customerUserDao.getProperty(Id, "seats_account");
                        if (seats_account == null)
                            seats_account = new CustomerUserPropertyDO(Id, "seats_account", "", new Timestamp(System.currentTimeMillis()));
                        seats_account.setPropertyValue(userDTO.getSeatsAccount());
                        list.add(seats_account);
                    }
                    if (userDTO.getSeatsPassword() != null) {
                        CustomerUserPropertyDO seats_password = customerUserDao.getProperty(Id, "seats_password");
                        if (seats_password == null)
                            seats_password = new CustomerUserPropertyDO(Id, "seats_password", "", new Timestamp(System.currentTimeMillis()));
                        seats_password.setPropertyValue(userDTO.getSeatsPassword());
                        list.add(seats_password);
                    }

                    if (userDTO.getExtensionNumber() != null) {
                        CustomerUserPropertyDO extension_number = customerUserDao.getProperty(Id, "extension_number");
                        if (extension_number == null)
                            extension_number = new CustomerUserPropertyDO(Id, "extension_number", "", new Timestamp(System.currentTimeMillis()));
                        extension_number.setPropertyValue(userDTO.getExtensionNumber());
                        list.add(extension_number);
                    }

                    if (userDTO.getExtensionPassword() != null) {
                        CustomerUserPropertyDO extension_password = customerUserDao.getProperty(Id, "extension_password");
                        if (extension_password == null)
                            extension_password = new CustomerUserPropertyDO(Id, "extension_password", "", new Timestamp(System.currentTimeMillis()));
                        extension_password.setPropertyValue(userDTO.getExtensionPassword());
                        list.add(extension_password);
                    }
                    //todo:删除双呼设置

                } else if ("call2way".equals(userDTO.getCallType())) {// 设置双呼号
                    if (userDTO.getWorkNum() != null) {
                        CustomerUserPropertyDO work_num = customerUserDao.getProperty(Id, "work_num");
                        if (work_num == null)
                            work_num = new CustomerUserPropertyDO(Id, "work_num", "", new Timestamp(System.currentTimeMillis()));
                        work_num.setPropertyValue(userDTO.getWorkNum());
                        list.add(work_num);
                    }
                    //todo: 删除呼叫中心配置
                }
                if (StringUtil.isNotEmpty(userDTO.getCallType())) {
                    CustomerUserPropertyDO call_type = customerUserDao.getProperty(Id, "call_type");
                    if (call_type == null) {
                        call_type = new CustomerUserPropertyDO(Id, "call_type", "", new Timestamp(System.currentTimeMillis()));
                    }
                    call_type.setPropertyValue(userDTO.getCallType());
                    list.add(call_type);
                }
                if (StringUtil.isNotEmpty(userDTO.getCallChannel())) {
                    CustomerUserPropertyDO call_channel = customerUserDao.getProperty(Id, "call_channel");
                    if (call_channel == null) {
                        call_channel = new CustomerUserPropertyDO(Id, "call_channel", "", new Timestamp(System.currentTimeMillis()));
                    }
                    call_channel.setPropertyValue(userDTO.getCallChannel());
                    list.add(call_channel);
                }
            }
            if ("3".equals(String.valueOf(userDTO.getUserType()))) {
                CustomerUserPropertyDO hasMarketProject = customerUserDao.getProperty(Id, "hasMarketProject");
                if (hasMarketProject == null) {
                    hasMarketProject = new CustomerUserPropertyDO(Id, "hasMarketProject", "", new Timestamp(System.currentTimeMillis()));
                }
                if (StringUtil.isEmpty(userDTO.getHasMarketProject())) {
                    hasMarketProject.setPropertyValue(null);
                } else {
                    String str = "," + userDTO.getHasMarketProject() + ",";
                    hasMarketProject.setPropertyValue(str);
                }
                list.add(hasMarketProject);
            }
            // 坐席授权企业开通的服务权限
            CustomerProperty permission = customerDao.getProperty(cu.getCust_id(), "servicePermissions");
            if (permission != null) {
                CustomerUserPropertyDO userPermission = customerUserDao.getProperty(Id, "servicePermissions");
                if (userPermission == null) {
                    userPermission = new CustomerUserPropertyDO(Id, "servicePermissions", permission.getPropertyValue(), new Timestamp(System.currentTimeMillis()));
                }
                userPermission.setPropertyValue(permission.getPropertyValue());
                list.add(userPermission);
            }
            if (list.size() > 0) {
                this.customerUserDao.batchSaveOrUpdate(list);
            }
        } catch (Exception e) {
            logger.error("更新用户信息失败", e);
            return 0;
        }
        return 1;
    }


    public String getUser(Integer pageNum, Integer pageSize, String customerId, String name, String realName, String userType) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sql = new StringBuffer();
        if (StringUtil.isEmpty(userType)) {
            userType = "2";
        }
        try {
            sql.append(" SELECT CAST(id AS CHAR) id,cust_id,user_type,account as name,realname,create_time,STATUS")
                    .append("  FROM t_customer_user  WHERE 1=1 AND user_type = " + userType + "  AND STATUS <> 3 ");
            sql.append(" AND   cust_id = '" + customerId + "'");
            if (null != name && !"".equals(name)) {
                sql.append(" AND   account like '%" + name + "%'");
            }
            if (null != realName && !"".equals(realName)) {
                sql.append(" AND   realname like '%" + realName + "%'");
            }
            sql.append(" ORDER by create_time ASC, id ASC");

            map.put("total", userDao.getSQLQuery(sql.toString()).list().size());
            List users = userDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
            for (int i = 0; i < users.size(); i++) {
                Map u = (Map) users.get(i);
                CustomerUserPropertyDO email = customerUserDao.getProperty(String.valueOf(u.get("id")), "email");
                CustomerUserPropertyDO mobile_num = customerUserDao.getProperty(String.valueOf(u.get("id")), "mobile_num");
                CustomerUserPropertyDO title = customerUserDao.getProperty(String.valueOf(u.get("id")), "title");
                if (email != null) u.put("email", email.getPropertyValue());
                if (mobile_num != null) u.put("mobile_num", mobile_num.getPropertyValue());
                if (title != null) u.put("title", title.getPropertyValue());
            }
            map.put("users", users);

            json.put("data", map);
            logger.info("查询操作员记录，sql：" + sql);


        } catch (Exception e) {
            logger.error("查询操作员出错", e);
        }
        return json.toJSONString();

    }

    public String getUser_V1(Integer pageNum, Integer pageSize, String customerId, String startAccount, String realName,
                             LoginUser loginUser, String userGroupId, String groupRoleType, String uid,
                             String startSeatId, String endAccount, String endSeatId, String callType, String callChannel,
                             String jobId, String userType, String projectId, String status, String notGroupRoleType) {
        if (StringUtil.isEmpty(userType)) {
            userType = "2";
        }
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sql = new StringBuffer();
        // 组员不能访问,直接返回空
        if ("2".equals(loginUser.getUserGroupRole())) {
            map.put("total", 0);
            map.put("users", new ArrayList<>());
            json.put("data", map);
            logger.warn("员工:[{}]为普通员工,无权查询员工列表", loginUser.getId());
            return json.toJSONString();
        }

        try {
            sql.append(" SELECT CAST(id AS CHAR) id,cust_id,user_type,account as name,realname,create_time,STATUS,locked_time lockedTime")
                    .append(" FROM t_customer_user  WHERE 1=1  AND STATUS <> 3 AND user_type = ").append(userType)
                    .append(" AND  cust_id = '" + customerId + "'");

            // 管理员
            if ("1".equals(loginUser.getUserType())) {
                //所属分组和角色都不为空 则关联组进行搜索
                if (StringUtil.isNotEmpty(jobId)) {
                    if (StringUtil.isNotEmpty(userGroupId)) {
                        sql.append(" and id in (SELECT cast(user_id AS signed) FROM t_customer_user_group_rel rel WHERE rel.status = 1 AND rel.group_id IN(SELECT id FROM t_customer_user_group WHERE id = '" + userGroupId + "' and p_id = '" + jobId + "' AND STATUS =1 AND cust_id = '" + customerId + "'))");
                    } else {
                        sql.append(" and id in (SELECT cast(user_id AS signed) FROM t_customer_user_group_rel rel WHERE rel.status = 1 AND rel.group_id IN(SELECT id FROM t_customer_user_group WHERE p_id = '" + jobId + "' AND STATUS =1 AND cust_id = '" + customerId + "'))");
                    }
                } else {
                    if (StringUtil.isNotEmpty(userGroupId) && StringUtil.isNotEmpty(groupRoleType)) {
                        sql.append(" AND id IN (SELECT cast(user_id AS signed) FROM t_customer_user_group_rel rel WHERE rel.status = 1 AND rel.type = " + groupRoleType + " AND rel.group_id IN(SELECT id FROM t_customer_user_group WHERE id = '" + userGroupId + "' AND STATUS =1 AND cust_id = '" + customerId + "'))");
                    } else {
                        //所属分组不为空则关联组进行搜索
                        if (StringUtil.isNotEmpty(userGroupId)) {
                            sql.append(" AND id IN (SELECT cast(user_id AS signed) FROM t_customer_user_group_rel rel WHERE rel.status = 1 AND rel.group_id IN(SELECT id FROM t_customer_user_group WHERE id = '" + userGroupId + "' AND STATUS =1 AND cust_id = '" + customerId + "'))");
                        }
                        //角色不为空则关联组进行搜索
                        if (StringUtil.isNotEmpty(groupRoleType)) {
                            sql.append(" AND id IN (SELECT cast(user_id AS signed) FROM t_customer_user_group_rel rel WHERE rel.status = 1 AND rel.type = " + groupRoleType + " AND rel.group_id IN(SELECT id FROM t_customer_user_group WHERE STATUS =1 AND cust_id = '" + customerId + "'))");
                        }
                        if (StringUtil.isNotEmpty(notGroupRoleType)) {
                            sql.append(" AND id NOT IN (SELECT cast(user_id AS signed) FROM t_customer_user_group_rel rel WHERE rel.status = 1 AND rel.type = " + notGroupRoleType + " AND rel.group_id IN(SELECT id FROM t_customer_user_group WHERE STATUS =1 AND cust_id = '" + customerId + "'))");
                        }
                    }
                }
            } else {
                // 组长只查询自己负责的用户
                if ("1".equals(loginUser.getUserGroupRole())) {
                    CustomerUserGroupRelDTO customerUserGroupRelDTO = customerUserDao.getCustomerUserGroupByUserId(loginUser.getId());
                    //角色不为空则关联组进行搜索
                    if (StringUtil.isNotEmpty(groupRoleType)) {
                        sql.append(" AND id IN (SELECT cast(user_id AS signed) FROM t_customer_user_group_rel rel WHERE rel.status = 1 AND rel.type = " + groupRoleType + " AND rel.group_id = '" + customerUserGroupRelDTO.getGroupId() + "')");
                    } else {
                        sql.append(" AND id IN (SELECT cast(user_id AS signed) FROM t_customer_user_group_rel rel WHERE rel.status = 1 AND rel.group_id = '" + customerUserGroupRelDTO.getGroupId() + "')");
                    }
                    if (StringUtil.isNotEmpty(notGroupRoleType)) {
                        sql.append(" AND id NOT IN (SELECT cast(user_id AS signed) FROM t_customer_user_group_rel rel WHERE rel.status = 1 AND rel.type = " + notGroupRoleType + " AND rel.group_id IN(SELECT id FROM t_customer_user_group WHERE STATUS =1 AND cust_id = '" + customerId + "'))");
                    }
                }
            }

            if (StringUtil.isNotEmpty(callType)) {//判断呼叫类型
                sql.append(" and id in(select user_id from t_customer_user_property p where p.property_name='call_type' and property_value='" + callType + "')");
                if (StringUtil.isNotEmpty(callChannel)) {//判断呼叫渠道
                    sql.append(" and id in(select user_id from t_customer_user_property p where p.property_name='call_channel' and property_value='" + callChannel + "')");
                }
                if (callType.equals("call_center")) {//如果是呼叫中心
                    if (StringUtil.isNotEmpty(startSeatId) && StringUtil.isNotEmpty(endSeatId)) {
                        sql.append(" and id in(select user_id from t_customer_user_property p where p.property_name='seats_account' and (CONVERT(property_value,SIGNED)>='" + startSeatId + "' and CONVERT(property_value,SIGNED)<='" + endSeatId + "'))");
                    } else if (null != startSeatId && !"".equals(startSeatId)) {
                        sql.append(" and id in(select user_id from t_customer_user_property p where p.property_name='seats_account' and property_value='" + startSeatId + "')");
                    }
                }
            }

            if (StringUtil.isNotEmpty(startAccount) && StringUtil.isNotEmpty(endAccount)) {
                sql.append(" AND account >= '" + startAccount + "' and account <='" + endAccount + "'");
            } else if (null != startAccount && !"".equals(startAccount)) {
                sql.append(" AND account like '%" + startAccount + "%'");
            }

            if (null != realName && !"".equals(realName)) {
                sql.append(" AND realname like '%" + realName + "%'");
            }
            if (null != uid && !"".equals(uid)) {
                sql.append(" AND id = " + uid);
            }
            if (null != status && !"".equals(status)) {
                sql.append(" AND status = " + status);
            }
            if (null != startSeatId && !"".equals(startSeatId)) {
                sql.append(" AND id in(select user_id from t_customer_user_property p where p.property_name='seats_account' and property_value='" + startSeatId + "')");
            }
            if (StringUtil.isNotEmpty(projectId)) {
                sql.append(" and id in(select user_id from t_customer_user_property p where p.property_name='hasMarketProject' and property_value like '%," + projectId + ",%')");
            }

            //sql.append(" ORDER by create_time ASC, id ASC");
            sql.append(" ORDER by account ASC ");
            logger.info(sql.toString());
            map.put("total", userDao.getSQLQuery(sql.toString()).list().size());
            List users = userDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
            CustomerUserGroupRelDTO customerUserGroupRelDTO;
            for (int i = 0; i < users.size(); i++) {
                Map u = (Map) users.get(i);
                List<CustomerUserPropertyDO> customerUserProperties = customerUserDao.getPropertiesByUserId(String.valueOf(u.get("id")));
                for (CustomerUserPropertyDO customerUserProperty : customerUserProperties) {
                    if ("email".equals(customerUserProperty.getPropertyName())) {
                        u.put("email", customerUserProperty.getPropertyValue());
                    } else if ("mobile_num".equals(customerUserProperty.getPropertyName())) {
                        u.put("mobile_num", customerUserProperty.getPropertyValue());
                    } else if ("title".equals(customerUserProperty.getPropertyName())) {
                        u.put("title", customerUserProperty.getPropertyValue());
                    } else if ("seats_account".equals(customerUserProperty.getPropertyName())) {
                        u.put("seats_account", customerUserProperty.getPropertyValue());
                    } else if ("seats_password".equals(customerUserProperty.getPropertyName())) {
                        u.put("seats_password", customerUserProperty.getPropertyValue());
                    } else if ("work_num".equals(customerUserProperty.getPropertyName())) {
                        u.put("work_num", customerUserProperty.getPropertyValue());
                    } else if ("extension_number".equals(customerUserProperty.getPropertyName())) {
                        u.put("extension_number", customerUserProperty.getPropertyValue());
                    } else if ("extension_password".equals(customerUserProperty.getPropertyName())) {
                        u.put("extension_password", customerUserProperty.getPropertyValue());
                    } else if ("call_type".equals(customerUserProperty.getPropertyName())) {
                        u.put("call_type", customerUserProperty.getPropertyValue());
                    } else if ("call_channel".equals(customerUserProperty.getPropertyName())) {
                        if (StringUtil.isNotEmpty(customerUserProperty.getPropertyValue())) {
                            if (StringUtil.isNumeric(customerUserProperty.getPropertyValue())) {
                                MarketResourceEntity mr = marketResourceDao.get(NumberConvertUtil.parseInt(customerUserProperty.getPropertyValue()));
                                u.put("call_channel", mr == null ? "" : mr.getResname());
                            }
                        }
                    } else if ("hasMarketProject".equals(customerUserProperty.getPropertyName())) {
                        if (StringUtil.isNotEmpty(customerUserProperty.getPropertyValue())) {
                            /*String v = customerUserProperty.getPropertyValue();
                            if (v.startsWith(",")) {
                                v = v.substring(1);
                            }
                            if (v.endsWith(",")) {
                                v = v.substring(0, v.length() - 1);
                            }*/
                            String name = "";
                            List<String> projectIds = customerUserDao.listProjectByUserId(NumberConvertUtil.parseLong(u.get("id")));
                            if (projectIds != null && projectIds.size() > 0) {
                                String _sql = " select * from t_market_project where id in(" + SqlAppendUtil.sqlAppendWhereIn(customerUserDao.listProjectByUserId(NumberConvertUtil.parseLong(u.get("id")))) + ")";
                                List<MarketProject> projectList = customerDao.queryListBySql(_sql, MarketProject.class);

                                if (projectList != null && projectList.size() > 0) {
                                    for (MarketProject project : projectList) {
                                        name += "," + project.getName();
                                    }
                                    if (name.length() > 0) {
                                        name = name.substring(1);
                                    }
                                }
                            }
                            u.put("hasMarketProject", name);
                        }
                    } else if ("add_agent_method".equals(customerUserProperty.getPropertyName())) {
                        u.put("addAgentMethod", StringUtil.isEmpty(customerUserProperty.getPropertyValue()) ? "0" : customerUserProperty.getPropertyValue());
                    }
                }

                // 查询用户所属分组
                customerUserGroupRelDTO = customerUserDao.getCustomerUserGroupByUserId(Long.parseLong(String.valueOf(u.get("id"))));
                if (customerUserGroupRelDTO != null) {
                    u.put("userGroupName", customerUserGroupRelDTO.getGroupName());
                    u.put("userGroupType", customerUserGroupRelDTO.getType());
                } else {
                    u.put("userGroupName", "");
                    u.put("userGroupType", "");
                }

            }
            map.put("users", users);

            json.put("data", map);
            logger.info("查询操作员记录，sql：" + sql);


        } catch (Exception e) {
            logger.info("查询操作员出错", e);
        }
        return json.toJSONString();

    }


    public String getCustomerUserList(String userType, String userId, String customerId, String name, String realName) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer hql = new StringBuffer();
        try {
            hql.append(" FROM CustomerUser ")
                    .append(" WHERE  status <> 3 ");
            hql.append(" AND   cust_id = ? ");
            if ("2".equals(userType)) {
                hql.append(" AND   id = " + userId);
            }
            if (null != realName && !"".equals(realName)) {
                hql.append(" AND   realName like '%" + realName + "%'");
            }
            List<CustomerUser> customerUserList = customerUserDao.find(hql.toString(), customerId);
            List<CustomerUserDTO> customerUserDTOList = new ArrayList<>();
            for (CustomerUser customerUserModel : customerUserList) {
                customerUserDTOList.add(new CustomerUserDTO(customerUserModel));
            }
            map.put("users", customerUserDTOList);
            json.put("data", map);
        } catch (Exception e) {
            logger.info("查询操作员出错");
        }
        return json.toJSONString();
    }

    /**
     * 坐席扣费
     *
     * @param custId 客户ID
     * @param remark
     * @param userId 坐席ID
     * @return
     * @throws Exception
     */
    public int seatMonthDeduction(String custId, String remark, String userId) throws Exception {
        int status = 0;
        logger.info("坐席扣费参数,custId:" + custId + ",remark:" + remark + ",userId:" + userId);
        // 查询客户配置的呼叫资源
        CustomerProperty cp = customerDao.getProperty(custId, MarketResourceTypeEnum.CALL.getPropertyName());
        String resourceId = seatsService.checkSeatConfigStatus(userId, custId);
        if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue()) && StringUtil.isNotEmpty(resourceId)) {
            // 客户坐席月单价
            int custSeatPrice = getCustSeatMonthPrice(custId, userId);
            // 供应商坐席月单价
            int supplierSeatPrice = getSupplierSeatMonthPrice(resourceId);
            status = transactionService.seatMonthDeduction(custId, custSeatPrice, supplierSeatPrice, resourceId, remark, userId);
        }
        return status;
    }

    /**
     * 查询客户坐席单价
     *
     * @param custId
     * @return 坐席包月价格(分)
     */
    public int getCustSeatMonthPrice0(String custId) throws TouchException {
        CustomerProperty cp = customerDao.getProperty(custId, MarketResourceTypeEnum.CALL.getPropertyName());
        if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue())) {
            CallPriceConfig callPriceConfig = JSON.parseObject(cp.getPropertyValue(), CallPriceConfig.class);
            if (callPriceConfig.getSeat_month_price() == null) {
                throw new TouchException("客户:" + custId + "未配置坐席包月售价");
            }
            if (callPriceConfig.getSeat_month_price() == 0) {
                logger.info("客户:" + custId + "坐席包月售价为0");
                return 0;
            }
            logger.info("客户:" + custId + "坐席包月售价为:" + callPriceConfig.getSeat_month_price() + "元");
            // 获取坐席包月价格
            int price = NumberConvertUtil.changeY2L(callPriceConfig.getSeat_month_price());
            // 半月5折
            if (callPriceConfig.getSeat_month_discount() != null && 1 == callPriceConfig.getSeat_month_discount()) {
                if (LocalDateTime.now().getDayOfMonth() > 15) {
                    return new BigDecimal(price).divide(new BigDecimal(2)).intValue();
                }
            }
            return price;
        } else {
            throw new TouchException("客户:" + custId + "未配置通话售价");
        }

    }

    /**
     * 查询客户坐席单价
     *
     * @param custId
     * @return 坐席包月价格(分)
     */
    public int getCustSeatMonthPrice(String custId, String userId) throws Exception {
        String resourceId = seatsService.checkSeatConfigStatus(userId, custId);
        CustomerProperty cp = customerDao.getProperty(custId, MarketResourceTypeEnum.CALL.getPropertyName());
        if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue()) && StringUtil.isNotEmpty(resourceId)) {
            Object object = JSON.parse(cp.getPropertyValue());
            CallPriceConfig callPriceConfig = null;
            if (object instanceof JSONObject) {
                callPriceConfig = JSON.parseObject(cp.getPropertyValue(), CallPriceConfig.class);
            } else if (object instanceof JSONArray) {
                List<CallPriceConfig> list = JSON.parseArray(cp.getPropertyValue(), CallPriceConfig.class);
                for (CallPriceConfig c : list) {
                    if (c.getResourceId().equals(resourceId)) {
                        callPriceConfig = c;
                    }
                }
            }
            if (callPriceConfig == null) {
                throw new TouchException("客户:" + custId + ",userId:" + userId + ",resourceId:" + resourceId + ",通话售价为空");
            }
            if (callPriceConfig.getSeat_month_price() == null) {
                throw new TouchException("客户:" + custId + ",resourceId:" + resourceId + ",未配置坐席包月售价");
            }
            if (callPriceConfig.getSeat_month_price() == 0) {
                logger.info("客户:" + custId + ",resourceId:" + resourceId + ",坐席包月售价为0");
                return 0;
            }
            logger.info("客户:" + custId + "坐席包月售价为:" + callPriceConfig.getSeat_month_price() + "元");
            // 获取坐席包月价格
            int price = NumberConvertUtil.changeY2L(callPriceConfig.getSeat_month_price());
            // 半月5折
            if (callPriceConfig.getSeat_month_discount() != null && 1 == callPriceConfig.getSeat_month_discount()) {
                if (LocalDateTime.now().getDayOfMonth() > 15) {
                    return new BigDecimal(price).divide(new BigDecimal(2)).intValue();
                }
            }
            return price;
        } else {
            throw new TouchException("客户:" + custId + ",userId:" + userId + ",,resourceId:" + resourceId + ",未配置通话售价");
        }

    }

    /**
     * 查询供应商坐席单价
     *
     * @param
     * @return 坐席包月价格(分)
     */
    public int getSupplierSeatMonthPrice(String resourceId) throws TouchException {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t2.property_value FROM t_market_resource t1 JOIN t_market_resource_property t2 ON t1.resource_id = t2.resource_id ");
        sql.append(" WHERE t1.`status` = 1 AND t2.property_name='price_config' AND t1.resource_id=?");
        List<Map<String, Object>> resourceConfig = customerDao.sqlQuery(sql.toString(), resourceId);
        if (resourceConfig.size() > 0) {
            String config = String.valueOf(resourceConfig.get(0).get("property_value"));
            CallPriceConfig callPriceConfig = JSON.parseObject(config, CallPriceConfig.class);
            if (callPriceConfig.getSeat_month_price() == null) {
                throw new TouchException("资源:" + resourceId + "未配置坐席包月售价");
            }
            if (callPriceConfig.getSeat_month_price() == 0) {
                logger.info("资源:" + resourceId + "坐席包月售价为0");
                return 0;
            }
            logger.info("资源:" + resourceId + "坐席包月售价为:" + callPriceConfig.getSeat_month_price() + "元");
            // 获取坐席包月价格
            int price = NumberConvertUtil.changeY2L(callPriceConfig.getSeat_month_price());
            // 半月5折
            if (callPriceConfig.getSeat_month_discount() != null && 1 == callPriceConfig.getSeat_month_discount()) {
                if (LocalDateTime.now().getDayOfMonth() > 15) {
                    return new BigDecimal(price).divide(new BigDecimal(2)).intValue();
                }
            }
            return price;
        } else {
            throw new TouchException("资源:" + resourceId + "未配置通话售价");
        }
    }

    /**
     * 查询客户通话资源配置
     *
     * @param custId
     * @param resourceId
     * @return
     */
    public CallPriceConfig selectCustCallResourceConfig(String custId, String resourceId) {
        JSONObject json = selectCustResourcePrice(custId, resourceId);
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json.toJSONString(), CallPriceConfig.class);
    }


    /**
     * 查询客户标签数据资源配置
     *
     * @param custId
     * @param resourceId
     * @return
     */
    public LabelDataPriceConfig selectCustLabelDataResourceConfig(String custId, String resourceId) {
        JSONObject json = selectCustResourcePrice(custId, resourceId);
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json.toJSONString(), LabelDataPriceConfig.class);
    }

    /**
     * 查询客户短信资源配置
     *
     * @param custId
     * @param resourceId
     * @return
     */
    public SmsPriceConfig selectCustSmsResourceConfig(String custId, String resourceId) {
        JSONObject json = selectCustResourcePrice(custId, resourceId);
        if (json == null) {
            return null;
        }
        return JSON.parseObject(json.toJSONString(), SmsPriceConfig.class);
    }


    /**
     * 获取客户对应资源ID
     *
     * @param custId
     * @param resourceId
     * @param
     * @return
     * @throws TouchException
     */
    private JSONObject selectCustResourcePrice(String custId, String resourceId) {
        MarketResourceEntity marketResource = marketResourceDao.getMarketResource(NumberConvertUtil.parseInt(resourceId));
        if (marketResource == null) {
            logger.warn("未查询到该资源,custId:" + custId + ",resourceId:" + resourceId);
            return null;
        }
        MarketResourceTypeEnum v = MarketResourceTypeEnum.getType(marketResource.getTypeCode());
        CustomerProperty cp = customerDao.getProperty(custId, v.getPropertyName());
        if (cp == null && StringUtil.isEmpty(cp.getPropertyValue())) {
            logger.warn("未查询到客户定价,custId:" + custId + ",resourceId:" + resourceId + ",type:" + marketResource.getTypeCode());
            return null;
        }
        JSONObject jsonObject = null;
        JSONArray jsonArray = null;
        //if (v.getType() == 1) {
        Object object = JSON.parse(cp.getPropertyValue());
        if (object instanceof JSONObject) {
            jsonObject = JSON.parseObject(cp.getPropertyValue());
            jsonArray = new JSONArray();
            jsonArray.add(jsonObject);
        } else if (object instanceof JSONArray) {
            jsonArray = JSON.parseArray(cp.getPropertyValue());
        }
        /*} else {
            jsonArray = JSON.parseArray(cp.getPropertyValue());
        }*/
        if (jsonArray != null && jsonArray.size() > 0) {
            for (int i = 0; i < jsonArray.size(); i++) {
                if (resourceId.equals(jsonArray.getJSONObject(i).getString("resourceId"))) {
                    jsonObject = jsonArray.getJSONObject(i);
                }
            }
        }
        if (jsonObject == null) {
            logger.warn("客户定价为空,custId:" + custId + ",resourceId:" + resourceId + ",type:" + marketResource.getTypeCode());
            return null;
        }
        return jsonObject;
    }


    /**
     * 金融超市注册用户分页
     *
     * @param pageNum
     * @param pageSize
     * @param custId
     * @param phone
     * @param client
     * @param startTime
     * @param endTime
     * @param channel
     * @param registerSource
     * @param touchType
     * @return
     */
    public Page pageRegisterUser(int pageNum, int pageSize, String custId, String phone, String client, String startTime, String endTime, String channel, String registerSource, String touchType) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT CAST(id AS CHAR) id,account,create_time createTime,status,cust_id custId,user_type userType,realname FROM t_customer_user WHERE cust_id = ? AND user_type = ? ");
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            sql.append(" AND create_time BETWEEN '" + startTime + "' and '" + endTime + "' ");
        } else {
            if (StringUtil.isNotEmpty(startTime)) {
                sql.append(" AND create_time > '" + startTime + "'");
            }
            if (StringUtil.isNotEmpty(endTime)) {
                sql.append(" AND create_time < '" + endTime + "'");
            }
        }
        if (StringUtil.isNotEmpty(phone)) {
            sql.append(" AND id IN (SELECT user_id FROM t_customer_user_property WHERE property_name='mobile_num' AND property_value = '").append(phone).append("')");
        }
        if (StringUtil.isNotEmpty(client)) {
            sql.append(" AND id IN (SELECT user_id FROM t_customer_user_property WHERE property_name='client' AND property_value = '").append(client).append("')");
        }
        if (StringUtil.isNotEmpty(channel)) {
            sql.append(" AND id IN (SELECT user_id FROM t_customer_user_property WHERE property_name='channel' AND property_value IN (SELECT id FROM t_dic WHERE name LIKE '%" + channel + "%'))");
        }
        if (StringUtil.isNotEmpty(registerSource)) {
            sql.append(" AND id IN (SELECT user_id FROM t_customer_user_property WHERE property_name='registerSource' AND property_value IN (SELECT id FROM t_dic WHERE name LIKE '%" + registerSource + "%'))");
        }
        if (StringUtil.isNotEmpty(touchType)) {
            sql.append(" AND id IN (SELECT user_id FROM t_customer_user_property WHERE property_name='touchType' AND property_value = '").append(touchType).append("')");
        }
        sql.append(" ORDER BY create_time DESC ");
        Page page = customerUserDao.sqlPageQuery0(sql.toString(), pageNum, pageSize, custId, 1);
        if (page != null && page.getData() != null) {
            Map<String, Object> m;
            CustomerUserPropertyDO userProperty;
            Dic dic;
            for (int i = 0; i < page.getData().size(); i++) {
                m = (Map<String, Object>) page.getData().get(i);
                m.put("mobileNum", "");
                m.put("client", "");
                m.put("area", "");
                m.put("channel", "");
                m.put("registerSource", "");
                m.put("touchType", "");

                userProperty = customerUserDao.getProperty(String.valueOf(m.get("id")), "mobile_num");
                if (userProperty != null) {
                    m.put("mobileNum", userProperty.getPropertyValue());
                }
                userProperty = customerUserDao.getProperty(String.valueOf(m.get("id")), "client");
                if (userProperty != null) {
                    m.put("client", userProperty.getPropertyValue());
                }
                userProperty = customerUserDao.getProperty(String.valueOf(m.get("id")), "area");
                if (userProperty != null) {
                    m.put("area", userProperty.getPropertyValue());
                }
                userProperty = customerUserDao.getProperty(String.valueOf(m.get("id")), "channel");
                if (userProperty != null) {
                    m.put("channel", "自有渠道");
                    if (!"1".equals(userProperty.getPropertyValue())) {
                        dic = dicDao.get(NumberConvertUtil.parseLong(userProperty.getPropertyValue()));
                        if (dic != null) {
                            m.put("channel", dic.getName());
                        }
                    }
                }
                userProperty = customerUserDao.getProperty(String.valueOf(m.get("id")), "registerSource");
                if (userProperty != null) {
                    dic = dicDao.get(NumberConvertUtil.parseLong(userProperty.getPropertyValue()));
                    if (dic != null) {
                        m.put("registerSource", dic.getName());
                    }
                }
                userProperty = customerUserDao.getProperty(String.valueOf(m.get("id")), "touchType");
                if (userProperty != null) {
                    m.put("touchType", userProperty.getPropertyValue());
                }
            }
        }
        return page;
    }

    /**
     * 金融超市注册用户详情
     *
     * @param userId
     * @return
     */
    public Map<String, Object> selectRegisterUser(long userId) {
        Map<String, Object> data = new HashMap<>();
        data.put("mobileNum", "");
        data.put("realName", "");
        data.put("createTime", "");
        data.put("area", "");
        data.put("client", "");
        data.put("channel", "");
        data.put("registerSource", "");
        data.put("touchType", "");
        data.put("creditValue", new HashMap<>());
        CustomerUser customerUser = customerUserDao.get(userId);
        CustomerUserPropertyDO userProperty;
        if (customerUser != null) {
            data.put("realName", customerUser.getRealname());
            data.put("createTime", customerUser.getCreateTime());
            userProperty = customerUserDao.getProperty(String.valueOf(userId), "mobile_num");
            if (userProperty != null) {
                data.put("mobileNum", userProperty.getPropertyValue());
            }
            userProperty = customerUserDao.getProperty(String.valueOf(userId), "client");
            if (userProperty != null) {
                data.put("client", userProperty.getPropertyValue());
            }
            userProperty = customerUserDao.getProperty(String.valueOf(userId), "area");
            if (userProperty != null) {
                data.put("area", userProperty.getPropertyValue());
            }
            userProperty = customerUserDao.getProperty(String.valueOf(userId), "channel");
            data.put("channel", "");
            if (userProperty != null) {
                Dic dic = dicDao.get(NumberConvertUtil.parseLong(userProperty.getPropertyValue()));
                if (dic != null) {
                    data.put("channel", dic.getName());
                }
            }
            userProperty = customerUserDao.getProperty(String.valueOf(userId), "registerSource");
            data.put("registerSource", "");
            if (userProperty != null) {
                Dic dic = dicDao.get(NumberConvertUtil.parseLong(userProperty.getPropertyValue()));
                if (dic != null) {
                    data.put("registerSource", dic.getName());
                }
            }
            userProperty = customerUserDao.getProperty(String.valueOf(userId), "touchType");
            if (userProperty != null) {
                data.put("touchType", userProperty.getPropertyValue());
            }
            StringBuilder hql = new StringBuilder();
            hql.append(" FROM FundProductApply m WHERE m.userId = ? ORDER BY applyTime DESC ");
            List<FundProductApply> list = customerDao.find(hql.toString(), userId);
            if (list != null && list.size() > 0) {
                data.put("creditValue", JSON.parseObject(list.get(0).getApplyValue()));
            }
        }
        return data;
    }

    public void updateShowRowByUser(String id, Long userIdStr, String showRow) {
        String userId = String.valueOf(userIdStr);
        logger.info("公海id是：" + id + "\nuserId是：" + userIdStr + "\n选择展示的字段是：" + showRow);
        //查询用户属性表是否存在 存在修改不存在删除
        CustomerUserPropertyDO customerUserProperty = customerUserDao.getProperty(String.valueOf(userId), id + "_row");
        if (customerUserProperty != null) {
            logger.info("userId是：" + userId + "的用户已经设置了展示字段信息");
            customerUserProperty.setPropertyValue(showRow);
        } else {
            customerUserProperty = new CustomerUserPropertyDO();
            customerUserProperty.setUserId(userId);
            customerUserProperty.setPropertyName(id + "_row");
            customerUserProperty.setCreateTime(String.valueOf(new Timestamp(System.currentTimeMillis())));
            customerUserProperty.setPropertyValue(showRow);
        }
        customerUserDao.saveOrUpdate(customerUserProperty);
    }

    public Map<String, Object> getShowRowByUser(String id, Long userId) throws Exception {
        Map<String, Object> data = new HashMap<>();
        logger.info("公海id是：" + id + "userId是：" + userId);
        //查询所有自定义列
        List<Map<String, Object>> unselectedList = CustomerShowRowEnum.getAllRow();
        //添加用户已经选择的自定义列
        List<Object> list = new ArrayList<>();
        CustomerUserPropertyDO customerUserProperty = customerUserDao.getProperty(String.valueOf(userId), id + "_row");
        if (customerUserProperty != null && StringUtil.isNotEmpty(customerUserProperty.getPropertyValue())) {
            String propertyValue = customerUserProperty.getPropertyValue();
            logger.info("userId是：" + userId + "\n已经选择的展示字段是：" + propertyValue);
            for (int i = 0; i < unselectedList.size(); i++) {
                String key = String.valueOf(unselectedList.get(i).get("key"));
                if (propertyValue.contains(key)) {
                    list.add(unselectedList.get(i));
                }
            }
        } else {
            HashMap<Object, Object> map = new HashMap<>();
            map.put("key", CustomerShowRowEnum.NAME.getKey());
            map.put("value", CustomerShowRowEnum.NAME.getValue());
            list.add(map);
            map = new HashMap<>();
            map.put("key", CustomerShowRowEnum.ID.getKey());
            map.put("value", CustomerShowRowEnum.ID.getValue());
            list.add(map);
            map = new HashMap<>();
            map.put("key", CustomerShowRowEnum.PHONE.getKey());
            map.put("value", CustomerShowRowEnum.PHONE.getValue());
            list.add(map);
        }
        unselectedList.removeAll(list);
        data.put("unselected", unselectedList);
        data.put("selected", list);
        return data;
    }

    /**
     * 保存或更新用户属性
     *
     * @param property
     * @return
     */
    public int saveCustomerUserProperty(CustomerUserPropertyDO property) {
        logger.info("开始更新用户属性,userId:" + property.getUserId() + ",propertyName:" + property.getPropertyName() + ",propertyValue:" + property.getPropertyValue());
        CustomerUserPropertyDO cp = customerUserDao.getProperty(property.getUserId(), property.getPropertyName());
        logger.info("用户原配置属性:" + cp);
        if (cp == null) {
            cp = new CustomerUserPropertyDO(property.getUserId(), property.getPropertyName(), property.getPropertyValue(), new Timestamp(System.currentTimeMillis()));
        }
        cp.setCreateTime(new Timestamp(System.currentTimeMillis()).toString());
        cp.setPropertyValue(property.getPropertyValue());
        try {
            customerDao.saveOrUpdate(cp);
            return 1;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 用户绑定微信
     *
     * @param userId
     * @param code
     * @return
     */
    public boolean saveBindWx(String userId, String code) {
        //获取微信用户openid
        String openId = weChatUtil.getWeChatOpenId(code);
        logger.info("用户ID:{},code:{},获取到的openId:{}", userId, code, openId);
        // 模拟绑定成功
        /*if (StringUtil.isEmpty(openId)) {
            openId = CipherUtil.encodeByMD5(UUID.randomUUID().toString());
        }*/
        if (StringUtil.isEmpty(openId)) {
            logger.warn("用户ID:{},code:{},openId:[{}]为空,绑定失败", userId, code, openId);
            return false;
        }
        // 清空之前的绑定关系
        List<CustomerUserPropertyDO> list = customerUserPropertyDao.getPropertyListByName("openid", openId);
        if (list != null && list.size() > 0) {
            for (CustomerUserPropertyDO propertyDO : list) {
                if (!propertyDO.getUserId().equals(userId)) {
                    propertyDO.setPropertyValue("");
                    customerUserPropertyDao.saveOrUpdate(propertyDO);
                }
            }
        }

        CustomerUserPropertyDO propertyDO = customerUserPropertyDao.getProperty(userId, "openid");
        if (propertyDO == null) {
            propertyDO = new CustomerUserPropertyDO(userId, "openid", openId, String.valueOf(new Timestamp(System.currentTimeMillis())));
        }
        propertyDO.setPropertyValue(openId);
        customerUserDao.saveOrUpdate(propertyDO);
        return true;
    }

    public Map<String, Object> getShowRowByUserPublicSea(String id, Long userId) throws Exception {
        Map<String, Object> data = new HashMap<>();
        logger.info("公海id是：" + id + "userId是：" + userId);
        //查询所有自定义列
        List<Map<String, Object>> unselectedList = CustomerPublicSeaDTO.getAllRow();
        //添加用户已经选择的自定义列
        List<Object> list = new ArrayList<>();
        CustomerUserPropertyDO customerUserProperty = customerUserDao.getProperty(String.valueOf(userId), id + "_row");
        if (customerUserProperty != null && StringUtil.isNotEmpty(customerUserProperty.getPropertyValue())) {
            String propertyValue = customerUserProperty.getPropertyValue();
            logger.info("userId是：" + userId + "\n已经选择的展示字段是：" + propertyValue);
            for (int i = 0; i < unselectedList.size(); i++) {
                String key = String.valueOf(unselectedList.get(i).get("key"));
                if (propertyValue.contains(key)) {
                    list.add(unselectedList.get(i));
                }
            }
        }
        if (list.size() == 0) {
            HashMap<Object, Object> map = new HashMap<>();
            map.put("key", CustomerPublicSeaDTO.CUST_NAME.getKey());
            map.put("value", CustomerPublicSeaDTO.CUST_NAME.getValue());
            list.add(map);
            map = new HashMap<>();
            map.put("key", CustomerPublicSeaDTO.REG_TIME.getKey());
            map.put("value", CustomerPublicSeaDTO.REG_TIME.getValue());
            list.add(map);
            map = new HashMap<>();
            map.put("key", CustomerPublicSeaDTO.REG_STATUS.getKey());
            map.put("value", CustomerPublicSeaDTO.REG_STATUS.getValue());
            list.add(map);
        }
        unselectedList.removeAll(list);
        data.put("unselected", unselectedList);
        data.put("selected", list);
        return data;
    }

    public Map<String, Object> getShowRowByUserPrivateSea(String id, Long userId) throws Exception {
        Map<String, Object> data = new HashMap<>();
        logger.info("公海id是：" + id + "userId是：" + userId);
        //查询所有自定义列
        List<Map<String, Object>> unselectedList = CustomerPrivateSeaDTO.getAllRow();
        //添加用户已经选择的自定义列
        List<Object> list = new ArrayList<>();
        CustomerUserPropertyDO customerUserProperty = customerUserDao.getProperty(String.valueOf(userId), id + "_row");
        if (customerUserProperty != null && StringUtil.isNotEmpty(customerUserProperty.getPropertyValue())) {
            String propertyValue = customerUserProperty.getPropertyValue();
            logger.info("userId是：" + userId + "\n已经选择的展示字段是：" + propertyValue);
            for (int i = 0; i < unselectedList.size(); i++) {
                String key = String.valueOf(unselectedList.get(i).get("key"));
                if (propertyValue.contains(key)) {
                    list.add(unselectedList.get(i));
                }
            }
        } else {
            HashMap<Object, Object> map = new HashMap<>();
            map.put("key", CustomerPrivateSeaDTO.NAME.getKey());
            map.put("value", CustomerPrivateSeaDTO.NAME.getValue());
            list.add(map);
            map = new HashMap<>();
            map.put("key", CustomerPrivateSeaDTO.ID.getKey());
            map.put("value", CustomerPrivateSeaDTO.ID.getValue());
            list.add(map);
            map = new HashMap<>();
            map.put("key", CustomerPrivateSeaDTO.PHONE.getKey());
            map.put("value", CustomerPrivateSeaDTO.PHONE.getValue());
            list.add(map);
        }
        unselectedList.removeAll(list);
        data.put("unselected", unselectedList);
        data.put("selected", list);
        return data;
    }

}
