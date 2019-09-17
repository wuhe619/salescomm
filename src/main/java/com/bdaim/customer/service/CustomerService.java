package com.bdaim.customer.service;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.account.dao.AccountDao;
import com.bdaim.auth.LoginUser;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.dao.BatchDao;
import com.bdaim.batch.entity.BatchListEntity;
import com.bdaim.bill.dao.BillDao;
import com.bdaim.bill.dto.TransactionTypeEnum;
import com.bdaim.bill.service.TransactionService;
import com.bdaim.callcenter.dto.*;
import com.bdaim.callcenter.service.impl.SeatsService;
import com.bdaim.common.CommonInfoCodeEnum;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.util.*;
import com.bdaim.common.util.page.PageList;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dao.EnterpriseDao;
import com.bdaim.customer.dto.*;
import com.bdaim.customer.entity.*;
import com.bdaim.customeruser.dto.UserCallConfigDTO;
import com.bdaim.customeruser.service.UserGroupService;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.entity.CustomerGroupProperty;
import com.bdaim.customs.dao.StationDao;
import com.bdaim.customs.entity.Station;
import com.bdaim.industry.dto.MarketResourceTypeEnum;
import com.bdaim.label.dao.IndustryInfoDao;
import com.bdaim.label.dao.IndustryPoolDao;
import com.bdaim.label.entity.IndustryPool;
import com.bdaim.price.entity.CommonInfoEntity;
import com.bdaim.price.entity.CommonInfoPropertyEntity;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dao.UserInfoDao;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.supplier.dao.SupplierDao;
import com.bdaim.supplier.entity.SupplierEntity;
import com.bdaim.supplier.service.SupplierService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.regex.Pattern;


@Service("customerService")
@Transactional
public class CustomerService {
    private static Logger logger = LoggerFactory.getLogger(CustomerService.class);

    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final static DateTimeFormatter DATE_TIME_FORMATTER_SSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");
    @Resource
    UserDao userDao;
    @Resource
    JdbcTemplate jdbcTemplate;
    @Resource
    CustomerDao customerDao;
    @Resource
    CustomerUserDao customerUserDao;
    @Resource
    SourceDao sourceDao;
    @Resource
    SupplierService supplierService;
    @Resource
    BatchDao batchDao;


    @Resource
    UserInfoDao userInfoDao;
    @Resource
    EnterpriseDao enterpriseDao;
    @Resource
    AccountDao accountDao;
    @Resource
    IndustryInfoDao industryInfoDao;
    @Resource
    BillDao billDao;
    @Resource
    StationDao stationDao;
    @Resource
    SupplierDao supplierDao;
    @Resource
    MarketResourceDao marketResourceDao;
    @Resource
    IndustryPoolDao industryPoolDao;
    @Resource
    CustomGroupDao customGroupDao;
    @Resource
    SeatsService seatsService;

    @Resource
    TransactionService transactionService;

    @Resource
    private UserGroupService userGroupService;


    public String getEnterpriseName(String custId) {
        try {
            Customer cu = customerDao.get(custId);
            if (cu != null)
                return cu.getEnterpriseName();
        } catch (Exception e) {

        }
        return "";
    }

    public Integer deleteUser(String userName, Integer status) {
        //todo 1.有权限   2.在自己企业下存在
        StringBuffer sb = new StringBuffer();

        sb.append("UPDATE t_customer_user SET ");
        sb.append(" status = ? ");
        sb.append(" where  account= ?");
        int code = jdbcTemplate.update(sb.toString(),
                new Object[]{status, userName});

        return code;

    }

    public Integer updateuser(UserDTO userDTO) {
        Long Id = userDTO.getId();
        String realName = userDTO.getRealName();
        String password = CipherUtil.generatePassword(userDTO.getPassword());
        String mobileNumber = userDTO.getMobileNumber();
        String mainNumber = userDTO.getMainNumber();

        int code = 0;
        if (StringUtil.isNotEmpty(password)) {
            StringBuffer sb = new StringBuffer();
            sb.append("UPDATE t_customer_user SET ");
            sb.append(" password = ? ");
            sb.append(" where  id= ?");
            code = jdbcTemplate.update(sb.toString(),
                    new Object[]{password, Id});
            logger.info("更新操作员账户或密码=======" + sb.toString());
        }
        if (StringUtil.isNotEmpty(realName)) {
            StringBuffer sbpro = new StringBuffer();
            String realNameSql = "SELECT * from t_customer_user_property p WHERE p.user_id=" + Id + " and p.property_name='realname'";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(realNameSql);
            if (list != null && list.size() > 0) {
                sbpro.append("UPDATE t_customer_user_property SET ");
                sbpro.append(" property_value = ? ");
                sbpro.append(" where  user_id= ? and property_name= ?");
                code = jdbcTemplate.update(sbpro.toString(),
                        new Object[]{realName, Id, "realname"});
                logger.info("更新操作员真是姓名=======" + sbpro.toString());
            } else {
                sbpro.append("INSERT into t_customer_user_property(user_id,property_name,property_value,create_time)VALUES(?,?,?,?) ");
                code = jdbcTemplate.update(sbpro.toString(),
                        new Object[]{Id, "realname", realName, new Timestamp(new Date().getTime())});
                logger.info("新增操作员真是姓名=======" + sbpro.toString());
            }
        }
        if (StringUtil.isNotEmpty(mobileNumber)) {
            StringBuffer sbpro = new StringBuffer();
            String mobileNumSql = "SELECT * from t_customer_user_property p WHERE p.user_id=" + Id + " and p.property_name='mobile_num'";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(mobileNumSql);
            if (list != null && list.size() > 0) {
                sbpro.append("UPDATE t_customer_user_property SET ");
                sbpro.append(" property_value = ? ");
                sbpro.append(" where  user_id= ? and property_name= ?");
                code = jdbcTemplate.update(sbpro.toString(),
                        new Object[]{mobileNumber, Id, "mobile_num"});
                logger.info("更新操作员手机号=======" + sbpro.toString());
            } else {
                sbpro.append("INSERT into t_customer_user_property(user_id,property_name,property_value,create_time)VALUES(?,?,?,?) ");
                code = jdbcTemplate.update(sbpro.toString(),
                        new Object[]{Id, "mobile_num", mobileNumber, new Timestamp(new Date().getTime())});
                logger.info("新增操作员手机号=======" + sbpro.toString());
            }

        }

        if (StringUtil.isNotEmpty(mainNumber)) {
            StringBuffer sbmain = new StringBuffer();
            String mainNumSql = "SELECT * from t_customer_user_property p WHERE p.user_id=" + Id + " and p.property_name='main_num'";
            List<Map<String, Object>> list = jdbcTemplate.queryForList(mainNumSql);
            if (list != null && list.size() > 0) {
                sbmain.append("UPDATE t_customer_user_property SET ");
                sbmain.append(" property_value = ? ");
                sbmain.append(" where  user_id= ? and property_name= ?");
                code = jdbcTemplate.update(sbmain.toString(),
                        new Object[]{mainNumber, Id, "main_num"});
                logger.info("更新操作员主叫号=======" + sbmain.toString());
            } else {
                sbmain.append("INSERT into t_customer_user_property(user_id,property_name,property_value,create_time)VALUES(?,?,?,?) ");
                code = jdbcTemplate.update(sbmain.toString(),
                        new Object[]{Id, "main_num", mainNumber, new Timestamp(new Date().getTime())});
                logger.info("新增操作员主叫号=======" + sbmain.toString());
            }

        }
        return code;
    }

    public PageList getUser(PageParam page, String customerId, String name, String realName, String mobileNum) {
        JSONObject json = new JSONObject();
        StringBuffer sql = new StringBuffer();


        sql.append("  SELECT  CAST(s.id AS CHAR) id,s.cust_id,s.user_type, s.account AS name,s.password AS PASSWORD,s.realname AS realname,cjc.cuc_minute seatMinute,\n" +
                "s.status STATUS,cjc.mobile_num AS mobile_num,cjc.cuc_seat AS cuc_seat,cjc.xz_seat AS xz_seat FROM t_customer_user s\n" +
                " LEFT JOIN (SELECT user_id, \n" +
                " MAX(CASE property_name WHEN 'mobile_num'  THEN property_value ELSE '' END ) mobile_num, \n" +
                " MAX(CASE property_name WHEN 'cuc_seat'    THEN property_value ELSE '' END ) cuc_seat,\n" +
                " MAX(CASE property_name WHEN 'xz_seat'    THEN property_value ELSE '' END ) xz_seat, \n" +
                " MAX(CASE property_name WHEN 'cuc_minute'  THEN property_value ELSE '0' END ) cuc_minute \n" +
                " FROM t_customer_user_property p GROUP BY user_id \n" +
                ") cjc ON s.id = cjc.user_id WHERE 1=1 AND user_type = 2  AND STATUS <> 3 ");
        sql.append(" AND cust_id = '" + customerId + "'");
        if (null != name && !"".equals(name)) {
            sql.append(" AND s.account like '%" + name + "%'");
        }
        if (null != realName && !"".equals(realName)) {
            sql.append(" AND s.realname like '%" + realName + "%'");
        }
        if (null != mobileNum && !"".equals(mobileNum)) {
            sql.append(" AND cjc.mobile_num like '%" + mobileNum + "%'");
        }

        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);

        if (list != null && list.getList() != null && list.getList().size() > 0) {
            Map<String, Object> map;
            for (int i = 0; i < list.getList().size(); i++) {
                map = (Map) list.getList().get(i);
                if (map != null && map.get("cuc_seat") != null) {
                    String cuc_seat = String.valueOf(map.get("cuc_seat"));
                    com.alibaba.fastjson.JSONObject json1 = JSON.parseObject(cuc_seat);
                    if (json1 != null) {
                        String mainNumber = json1.getString("mainNumber");
                        map.put("cucMainNumber", mainNumber);
                    }
                }
                if (map != null && map.get("xz_seat") != null) {
                    String cmc_seat = String.valueOf(map.get("xz_seat"));
                    com.alibaba.fastjson.JSONObject json1 = JSON.parseObject(cmc_seat);
                    if (json1 != null) {
                        String mainNumber1 = json1.getString("mainNumber");
                        map.put("xzMainNumber", mainNumber1);
                    }
                }
            }
        }

        return list;

    }

    public CustomerUser getUserByName(String name) {
        //String hql="from CustomerUser m where m.account=? and m.status=0";
        String hql = "from CustomerUser m where m.account=?";
        CustomerUser m = userDao.findUnique(hql, name);
        return m;
    }

    public String getUserRealName(String userId) {
        return customerUserDao.getName(userId);
    }

    public synchronized void registerOrUpdateCustomer(CustomerRegistDTO vo) throws Exception {
        if (StringUtil.isNotEmpty(vo.getDealType())) {
            //编辑或创建客户
            CustomerUser customerUserDO;
            if (vo.getDealType().equals("1")) {
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
                } else {
                    customerUserDO = new CustomerUser();
                    //1企业客户 2 操作员
                    customerUserDO.setUserType(1);
                    customerUserDO.setId(IDHelper.getUserID());
                    customerUserDO.setCust_id(customerId);
                    customerUserDO.setAccount(vo.getName());
                    customerUserDO.setPassword(CipherUtil.generatePassword(vo.getPassword()));
                    customerUserDO.setRealname(vo.getRealName());
                    customerUserDO.setStatus(Constant.USER_ACTIVE_STATUS);
                }
                customerUserDao.saveOrUpdate(customerUserDO);

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
                //销售负责人
                if (StringUtil.isNotEmpty(vo.getSalePerson())) {
                    if (StringUtil.isNotEmpty(vo.getCustId())) {
                        customerDao.dealCustomerInfo(vo.getCustId(), "sale_person", vo.getSalePerson());
                    } else {
                        customerDao.dealCustomerInfo(customerId, "sale_person", vo.getSalePerson());
                    }
                }
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
                        customerDao.dealCustomerInfo(vo.getCustId(), "mobile_num", vo.getMobile());
                    } else {
                        customerDao.dealCustomerInfo(customerId, "mobile_num", vo.getMobile());
                    }
                }
                //身份证正面url
                if (StringUtil.isNotEmpty(vo.getIdCardFront())) {
                    if (StringUtil.isNotEmpty(vo.getCustId())) {
                        customerDao.dealCustomerInfo(vo.getCustId(), "idCard_front_path", vo.getIdCardFront());
                    } else {
                        customerDao.dealCustomerInfo(customerId, "idCard_front_path", vo.getIdCardFront());
                    }
                }
                //身份证背面url
                if (StringUtil.isNotEmpty(vo.getIdCardBack())) {
                    if (StringUtil.isNotEmpty(vo.getCustId())) {
                        customerDao.dealCustomerInfo(vo.getCustId(), "idCard_back_path", vo.getIdCardBack());
                    } else {
                        customerDao.dealCustomerInfo(customerId, "idCard_back_path", vo.getIdCardBack());
                    }
                }
                //打印员
                if (StringUtil.isNotEmpty(vo.getPrinter())) {
                    if (StringUtil.isNotEmpty(vo.getCustId())) {
                        customerDao.dealCustomerInfo(vo.getCustId(), "printer", vo.getPrinter());
                    } else {
                        customerDao.dealCustomerInfo(customerId, "printer", vo.getPrinter());
                    }
                }
                //封装员
                if (StringUtil.isNotEmpty(vo.getPackager())) {
                    if (StringUtil.isNotEmpty(vo.getCustId())) {
                        customerDao.dealCustomerInfo(vo.getCustId(), "packager", vo.getPackager());
                    } else {
                        customerDao.dealCustomerInfo(customerId, "packager", vo.getPackager());
                    }
                }
                //场站信息
                if (StringUtil.isNotEmpty(vo.getStationId())) {
                    if (StringUtil.isNotEmpty(vo.getCustId())) {
                        customerDao.dealCustomerInfo(vo.getCustId(), "station_id", vo.getStationId());
                    } else {
                        customerDao.dealCustomerInfo(customerId, "station_id", vo.getStationId());
                    }
                }

            } else if (vo.getDealType().equals("2")) {//冻结以及解冻
                if (StringUtil.isNotEmpty(vo.getCustId())) {
                    Customer customerDO = customerDao.findUniqueBy("custId", vo.getCustId());
                    if (customerDO != null && StringUtil.isNotEmpty(vo.getStatus())) {
                        customerDO.setStatus(Integer.valueOf(vo.getStatus()));
                        customerDao.saveOrUpdate(customerDO);
                    }
                    List<CustomerUser> customerUserList = customerUserDao.getAllByCustId(vo.getCustId());
                    if (customerUserList != null && customerUserList.size() > 0) {
                        for (CustomerUser customeruser : customerUserList) {
                            if (StringUtil.isNotEmpty(vo.getStatus())) {
                                if (vo.getStatus().equals("0")) {//解冻或正常
                                    customeruser.setStatus(Integer.valueOf(vo.getStatus()));
                                }
                                if (vo.getStatus().equals("1")) {//冻结
                                    customeruser.setStatus(Integer.valueOf(2));
                                }
                            }
                            customerUserDao.update(customeruser);
                        }
                    }
                }
            } else if (vo.getDealType().equals("3")) {//修改密码
                if (StringUtil.isNotEmpty(vo.getUserId())) {
                    customerUserDO = customerUserDao.findUniqueBy("id", Long.valueOf(vo.getUserId()));
                    if (customerUserDO != null && StringUtil.isNotEmpty(vo.getPassword())) {
                        if (customerUserDO.getUserType() == 1) {
                            customerUserDO.setPassword(CipherUtil.generatePassword(vo.getPassword()));
                            customerUserDao.saveOrUpdate(customerUserDO);
                            logger.info("用户：" + customerUserDO.getAccount() + "\t旧加密密码：" + customerUserDO.getPassword() +
                                    "\t新密码：" + vo.getPassword() + "\t新加密密码：" + CipherUtil.generatePassword(vo.getPassword()));
                        }
                    }
                }
                //快递设置
            } else if (vo.getDealType().equals("4")) {//快递设置
                if (StringUtil.isNotEmpty(vo.getExpressConfig())) {
                    if (StringUtil.isNotEmpty(vo.getCustId())) {
                        customerDao.dealCustomerInfo(vo.getCustId(), "express_config", vo.getExpressConfig());
                    }
                }
            }
        }
    }


    /**
     * @param userDTO
     * @return void 返回类型
     * @throws
     * @Title: addUser
     * @Description: 企业客户添加操作员
     */
    public void addUser(UserDTO userDTO, String enpterprise_name) {

        Long id = userDTO.getId();
        String custId = userDTO.getCustomerId();
        Integer userType = userDTO.getUserType();
        String userName = userDTO.getUserName();
        String realName = userDTO.getRealName();
        String title = userDTO.getTitle();
        //String remark = userDTO.getRemark();
        String password = userDTO.getPassword();
        String mobileNumber = userDTO.getMobileNumber();
        String email = userDTO.getEmail();

        try {
            String sql = "INSERT INTO t_user(id,cust_id,user_type,NAME,PASSWORD,realname,title,mobile_num,create_time,modify_time,email,STATUS,enterprise_name) VALUES(?,?,?,?,?,?,?,?,NOW(),NOW(),?,0,?)";
            jdbcTemplate.update(sql, new Object[]{id, custId, userType, userName, password, realName, title, mobileNumber, email, enpterprise_name});

            String sqlRel = "INSERT INTO t_user_role_rel(ID,ROLE,LEVEL,OPTUSER,CREATE_TIME) VALUES(?,28888,0,'system',NOW())";
            jdbcTemplate.update(sqlRel, new Object[]{id});
        } catch (Exception e) {
            logger.info("企业客户添加操作员出错");
        }


    }

    public String getCustomerUserList(String customerId, String name, String realName) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sql = new StringBuffer();
        try {
            sql.append(" SELECT CAST(id AS CHAR) id, realname ")
                    .append("  FROM t_user  WHERE 1=1 AND user_type = 2  AND STATUS <> 3 ");
            sql.append(" AND   cust_id = '" + customerId + "'");
            if (null != name && !"".equals(name)) {
                sql.append(" AND   name like '%" + name + "%'");
            }
            if (null != realName && !"".equals(realName)) {
                sql.append(" AND   realname like '%" + realName + "%'");
            }

            map.put("users", jdbcTemplate.queryForList(sql.toString()));
            json.put("data", map);
            logger.info("查询操作员记录，sql：" + sql);
        } catch (Exception e) {
            logger.info("查询操作员出错");
        }
        return json.toJSONString();
    }

    public Double getRemainMoney(String customerId) {
        Double remainMony = 0.00;
        try {
            remainMony = customerDao.remainMoney(customerId);
        } catch (TouchException e) {
            logger.error(e.getMessage());
        }
        return remainMony;
    }

    public Double getSourceRemainMoney(String supplierId) {
        Double remainMony = 0.00;
        try {
            remainMony = sourceDao.remainMoney(supplierId);
        } catch (TouchException e) {
            logger.error(e.getMessage());
        }
        return remainMony;
    }

    public PageList getCustomerInfo(PageParam page, CustomerRegistDTO customerRegistDTO) {

        StringBuilder sqlBuilder = new StringBuilder("SELECT\n" +
                "t1.cust_id as custId,\n" +
                "t1.enterprise_name AS enterpriseName,\n" +
                "cast(t2.id as char) as userId ,\n" +
                "t2.account as adminAccount,\n" +
                "t2.password as passwords,\n" +
                "t2.realname as realName,  -- 属性表\n" +
                "cjc.mobile_num,  -- 属性表\n" +
                "IFNULL (t1.title,'') AS title, -- 属性表\n" +
                "t1.create_time,\n" +
                "t1.`status`,cjc.packagerId,cjc.printerId,cjc.idCardBack,cjc.idCardFront,\n" +
                "cjc.industry,cjc.salePerson,cjc.contactAddress,\n" +
                "cjc.province,cjc.city,cjc.fixPrice,cjc.county,cjc.taxpayerId,\n" +
                "cjc.bli_path AS bliPic,\n" +
                "cjc.bank,cjc.bankAccount,cjc.stationId,\n" +
                "cjc.bank_account_certificate AS bankAccountPic\n" +
                "FROM t_customer t1\n" +
                "LEFT JOIN t_customer_user t2   ON t1.cust_id = t2.cust_id \n" +
                "LEFT JOIN (SELECT cust_id, \n" +
                "\tmax(CASE property_name WHEN 'industry'   THEN property_value ELSE '' END ) industry,\n" +
                "\tmax(CASE property_name WHEN 'sale_person'   THEN property_value ELSE '' END ) salePerson,\n" +
                "\tmax(CASE property_name WHEN 'reg_address'   THEN property_value ELSE '' END ) contactAddress,\n" +
                "\tmax(CASE property_name WHEN 'province'   THEN property_value ELSE '' END ) province,\n" +
                "\tmax(CASE property_name WHEN 'city'   THEN property_value ELSE '' END ) city,\n" +
                "\tmax(CASE property_name WHEN 'county'   THEN property_value ELSE '' END ) county,\n" +
                "\tmax(CASE property_name WHEN 'taxpayer_id'   THEN property_value ELSE '' END ) taxpayerId,\n" +
                "\tmax(CASE property_name WHEN 'packager'   THEN property_value ELSE '' END ) packagerId,\n" +
                "\tmax(CASE property_name WHEN 'printer'   THEN property_value ELSE '' END ) printerId,\n" +
                "\tmax(CASE property_name WHEN 'idCard_back_path'   THEN property_value ELSE '' END ) idCardBack,\n" +
                "\tmax(CASE property_name WHEN 'idCard_front_path'   THEN property_value ELSE '' END ) idCardFront,\n" +
                "\tmax(CASE property_name WHEN 'address_fix_price'   THEN property_value ELSE '' END ) fixPrice,\n" +
                "\tmax(CASE property_name WHEN 'bli_path'   THEN property_value ELSE '' END ) bli_path,\n" +
                "\tmax(CASE property_name WHEN 'bank'   THEN property_value ELSE '' END ) bank,\n" +
                "\tmax(CASE property_name WHEN 'bank_account'   THEN property_value ELSE '' END ) bankAccount,\n" +
                "\tmax(CASE property_name WHEN 'bank_account_certificate'   THEN property_value ELSE '' END ) bank_account_certificate,\n" +
                "\tmax(CASE property_name WHEN 'station_id'   THEN property_value ELSE '' END ) stationId,\n" +
                "\tmax(CASE property_name WHEN 'mobile_num'   THEN property_value ELSE '' END ) mobile_num\n" +
                "   FROM t_customer_property p GROUP BY cust_id \n" +
                ") cjc ON t1.cust_id = cjc.cust_id \n" +
                "where 1=1 and t2.user_type=1 ");
        if (StringUtil.isNotEmpty(customerRegistDTO.getCustId())) {
            sqlBuilder.append(" AND t1.cust_id = " + customerRegistDTO.getCustId());
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getEnterpriseName())) {
            sqlBuilder.append(" AND t1.enterprise_name like '%" + customerRegistDTO.getEnterpriseName() + "%'");
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getName())) {
            sqlBuilder.append(" AND t2.account LIKE '%" + customerRegistDTO.getName() + "%'");
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getRealName())) {
            sqlBuilder.append(" AND t2.realname LIKE '%" + customerRegistDTO.getRealName() + "%'");
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getStationId())) {
            sqlBuilder.append(" AND cjc.stationId =" + customerRegistDTO.getStationId());
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getSalePerson())) {
            sqlBuilder.append(" AND cjc.salePerson LIKE '%" + customerRegistDTO.getSalePerson() + "%'");
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getStartTime())) {
            sqlBuilder.append(" AND t1.create_time >= '" + customerRegistDTO.getStartTime() + "'");
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getEndTime())) {
            sqlBuilder.append(" AND t1.create_time <= '" + customerRegistDTO.getEndTime() + "'");
        }
        if (StringUtil.isNotEmpty(customerRegistDTO.getIndustry())) {
            sqlBuilder.append(" AND cjc.industry = " + customerRegistDTO.getIndustry());
        }
        sqlBuilder.append(" order by t1.create_time desc");
        PageList pageData = new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
        List<Map<String, Object>> list = pageData.getList();
        //查询部门里面有几个职位
        if (list.size() > 0) {
            long packagerId = 0, printerId = 0;
            for (int i = 0; i < list.size(); i++) {
                if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("packagerId")))) {
                    packagerId = NumberConvertUtil.parseLong(String.valueOf(list.get(i).get("packagerId")));
                    logger.info("封装员id是：" + packagerId);
                    //根据id查询员工姓名
                    String packager = userDao.getUserRealName(packagerId);
                    list.get(i).put("packager", packager);
                    list.get(i).put("packagerId", packagerId);
                }
                if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("printerId")))) {
                    printerId = NumberConvertUtil.parseLong(String.valueOf(list.get(i).get("printerId")));
                    logger.info("打印员id是:" + printerId);
                    String printer = userDao.getUserRealName(printerId);
                    list.get(i).put("printer", printer);
                    list.get(i).put("printerId", printerId);
                }
                //场站信息
                if (StringUtil.isNotEmpty(String.valueOf(list.get(i).get("stationId")))) {
                    logger.info("场站id是：" + list.get(i).get("stationId"));
                    //根据id查询员工姓名
                    Station station = stationDao.getStationById(NumberConvertUtil.parseInt(String.valueOf(list.get(i).get("stationId"))));
                    list.get(i).put("stationName", "");
                    if (station != null) {
                        list.get(i).put("stationName", station.getName());
                    }
                }
            }
        }
        return pageData;
    }

    public void heartbeat(long customerUserId) {
        //customerUserDao.executeUpdateSQL("update t_customer_user set active_time=now() where id="+customerUserId);
    }

    /**
     * 查询企业配置信息（资源+定价）
     *
     * @author:duanliying
     * @method
     * @date: 2019/2/28 9:59
     */
    public Map<String, Object> selectCustomerConfig(String custId, String callType) throws Exception {
        Map<String, Object> result = new HashMap<>();
        Map<String, Object> map, resource;
        List<MarketResourceDTO> marketResourceList;
        List<Map<String, Object>> supplierList, resourceList = null;
        JSONObject callConfig;
        ResourcePropertyEntity marketResourceProperty;
        //查询企业所有供应商以及供应商下的资源信息
        List<Map<String, Object>> allSupplierList = supplierService.listNoloseAllSupplier();
        String custConfigMessage = null;
        for (int i = 0; i < allSupplierList.size(); i++) {
            map = allSupplierList.get(i);
            if (map != null && map.get("supplierList") != null) {
                supplierList = (List<Map<String, Object>>) map.get("supplierList");
                for (int j = 0; j < supplierList.size(); j++) {
                    if (supplierList.get(j).get("resourceList") != null) {
                        resourceList = new ArrayList<>();
                        marketResourceList = (List<MarketResourceDTO>) supplierList.get(j).get("resourceList");
                        for (MarketResourceDTO dto : marketResourceList) {
                            resource = new HashMap<>();
                            //根据supplierList获取供应商id和type
                            String resourceId = String.valueOf(dto.getResourceId());
                            CustomerProperty property = customerDao.getProperty(custId, resourceId + "_config");
                            if (property != null) {
                                custConfigMessage = String.valueOf(property.getPropertyValue());
                                resource.put("custConfig", custConfigMessage);
                            }
                            // 支持过滤呼叫类型
                           /* if (StringUtil.isNotEmpty(callType)) {
                                if (map.get("type") != null && String.valueOf(ResourceEnum.CALL.getType()).equals(String.valueOf(map.get("type")))) {
                                    marketResourceProperty = sourceDao.getResourceById(String.valueOf(dto.getResourceId()), "price_config");
                                    if (marketResourceProperty != null && StringUtil.isNotEmpty(marketResourceProperty.getPropertyValue())) {
                                        callConfig = JSON.parseObject(marketResourceProperty.getPropertyValue());
                                        if (!callType.equals(callConfig.getString("type"))) {
                                            continue;
                                        }
                                    }
                                }
                            }*/

                            resource.put("resourceId", dto.getResourceId());
                            resource.put("resname", dto.getResname());
                            //resource.put("resourceProperty", dto.getResourceProperty());
                            resourceList.add(resource);
                        }
                        supplierList.get(j).put("resourceList", resourceList);
                    }
                    // 支持过滤呼叫类型
                /*    if (StringUtil.isNotEmpty(callType)) {
                        if (resourceList != null && resourceList.size() == 0) {
                            if (map.get("type") != null && String.valueOf(ResourceEnum.CALL.getType()).equals(String.valueOf(map.get("type")))) {
                                supplierList.remove(j);
                                j -= 1;
                            }
                        }
                    }*/
                }
            }
        }
        //根据企业id查询企业名称
        String enterpriseName = customerDao.getEnterpriseName(custId);
        if (StringUtil.isNotEmpty(enterpriseName)) {
            result.put("custName", enterpriseName);
        }
        //查询当前企业配置那些类型资源
        CustomerProperty serviceResourceProperty = customerDao.getProperty(custId, "resource_type");
        if (serviceResourceProperty != null) {
            result.put("checkResourceId", serviceResourceProperty.getPropertyValue());
        }
        //如果是设置销售定价需要过滤掉没有配置的资源   type=1是配置资源  2是配置定价
      /*  if ("2".equals(type)) {
            for (int i = 0; i < allSupplierList.size(); i++) {
                String custConfig = String.valueOf(allSupplierList.get(i).get("custConfig"));
                if (custConfig == null || StringUtil.isEmpty(custConfig)) {
                    allSupplierList.remove(i);
                }
            }
        }*/
        result.put("custConfigList", allSupplierList);
        return result;
    }

    /**
     * @description 保存企业配置信息（资源+定价）
     * @author:duanliying
     * @method
     * @date: 2019/3/1 15:19
     */
    public void saveCustSetting(JSONObject json) throws Exception {
        //type=1是配置资源  2是配置定价
        String type = json.getString("type");
        //如果checkResourceId存在说明是修改渠道信息，否则修改定价信息
        String custId = json.getString("custId");
        //用户选中的类型
        String checkResourceId = json.getString("checkResourceId");
        //修改信息多个json以，隔开
        JSONArray custConfigLists = null;
        String custConfigStr = json.getString("custConfigList");
        if (StringUtil.isNotEmpty(custConfigStr)) {
            custConfigLists = JSON.parseArray(custConfigStr);
        }
        if ("1".equals(type)) {
            updateCustConfig(custConfigLists, checkResourceId, custId);
        } else if ("2".equals(type)) {
            logger.info("修改企业销售定价");
            updateCustConfigAndPrice(custId, custConfigLists);
            //保存修改记录
            String optUserId = json.getString("optUserId");
            Map<String, String> map = new HashMap<>();
            map.put("custId", custId);
            map.put("optUserId", optUserId);
            map.put("updatePriceLog", custConfigLists.toString());
            saveCommonInfo(map);
        }
    }

    /**
     * @description 更改企业的配置信息（资源）
     * @author:duanliying
     * @method
     * @date: 2019/4/14 18:13
     */
    private void updateCustConfig(JSONArray custConfigLists, String checkResourceId, String custId) throws Exception {
        String delectSql = "DELETE FROM t_customer_property WHERE cust_id =? AND property_name = ?";
        //先查询出原来所有的配置信息，将本次配置不存在的删除
        List<CustomerProperty> custConfigs = customerDao.getPropertyLike(custId, "_config");
        //遍历所有
        StringBuffer suppliers = new StringBuffer();
        if (custConfigs.size() > 0) {
            for (int i = 0; i < custConfigs.size(); i++) {
                boolean contains = false;
                String propertyName = custConfigs.get(i).getPropertyName();
                logger.info("查询出资源key" + propertyName);
                String config = propertyName.replace("_config", "");
                Pattern pattern = Pattern.compile("[0-9]*");
                boolean matches = pattern.matcher(config).matches();
                if (matches) {
                    String propertyValue = custConfigs.get(i).getPropertyValue();
                    //将企业配置信息转换成json对象，获取里面的resourceId
                    if (StringUtil.isNotEmpty(propertyValue)) {
                        logger.info("propertyValue" + propertyValue);
                        String oldResourceId = String.valueOf(JSON.parseObject(propertyValue).get("resourceId"));
                        logger.info("查询出原配置资源id" + oldResourceId);
                        //判断资源id是否在本次配置中不存在删除
                        if (custConfigLists != null && custConfigLists.size() > 0) {
                            for (int j = 0; j < custConfigLists.size(); j++) {
                                JSONObject jsonObject = custConfigLists.getJSONObject(j);
                                logger.info("企业新配置的资源信息" + jsonObject.toString());
                                String supplierId = String.valueOf(jsonObject.get("supplierId"));
                                logger.info("供应商Id是" + supplierId);
                                //添加企业配置的供应商
                                if (StringUtil.isNotEmpty(supplierId)) {
                                    boolean flag = String.valueOf(suppliers).contains(supplierId);
                                    if (!flag) {
                                        suppliers.append(supplierId).append(",");
                                    }
                                }
                                int type = jsonObject.getIntValue("type");
                                if (ResourceEnum.CALL.getType() == type) {
                                    //说明是配置外呼的资源信息需要更新批次下的外显号码
                                    String apparentNumber = jsonObject.getString(ResourceEnum.CALL.getApparentNumber());
                                    logger.info("最新的外显号码是：" + apparentNumber);
                                    updateBatchIdApperNum(supplierId, custId, apparentNumber);
                                }
                                String newResourceId = custConfigLists.getJSONObject(j).getString("resourceId");
                                if (StringUtil.isNotEmpty(newResourceId)) {
                                    if (newResourceId.equals(oldResourceId)) {
                                        contains = true;
                                        logger.info("新的配置资源包含原资源id是：" + oldResourceId);
                                        break;
                                    }
                                }
                            }
                            if (!contains) {
                                //删除老的配置信息
                                customerDao.executeUpdateSQL(delectSql, new Object[]{custId, oldResourceId + "_config"});
                                logger.info("删除原配置资源id" + oldResourceId);
                            }
                        } else {
                            //最新资源是空删除企业配置的所有信息
                            customerDao.executeUpdateSQL(delectSql, new Object[]{custId, oldResourceId + "_config"});
                        }
                    }
                }
            }
        }
        //查询出资源id看原配置是否存在，存在更新不存在删除
        if (StringUtil.isEmpty(suppliers.toString())) {
            suppliers = getCustResourceStr(custConfigLists);
            logger.info("首次配置资源" + custId + "供应商是" + suppliers.toString());
        }
        updateCustConfigAndPrice(custId, custConfigLists);

        //保存本次配置资源类型
        CustomerProperty customerProperty = customerDao.getProperty(custId, "resource_type");
        if (customerProperty != null) {
            customerProperty.setPropertyValue(checkResourceId);
            customerDao.saveOrUpdate(customerProperty);
        } else {
            CustomerProperty customerPropertyType = new CustomerProperty();
            customerPropertyType.setCustId(custId);
            customerPropertyType.setPropertyName("resource_type");
            customerPropertyType.setPropertyValue(checkResourceId);
            customerPropertyType.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            customerDao.saveOrUpdate(customerPropertyType);
        }
        //保存channel信息
        logger.info("企业配置的供应商是" + suppliers.toString());
        CustomerProperty channelOldCustomer = customerDao.getProperty(custId, "channel");
        if (channelOldCustomer == null) {
            CustomerProperty channelCustomer = new CustomerProperty();
            channelCustomer.setCustId(custId);
            channelCustomer.setPropertyName("channel");
            channelCustomer.setPropertyValue(String.valueOf(suppliers));
            channelCustomer.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            customerDao.saveOrUpdate(channelCustomer);
        } else {
            logger.info("企业配置的原供应商是" + channelOldCustomer.getPropertyValue());
            channelOldCustomer.setPropertyValue(String.valueOf(suppliers));
            customerDao.saveOrUpdate(channelOldCustomer);
        }
    }

    public StringBuffer getCustResourceStr(JSONArray custConfigLists) {
        StringBuffer suppliers = new StringBuffer();
        if (custConfigLists != null && custConfigLists.size() > 0) {
            for (int j = 0; j < custConfigLists.size(); j++) {
                JSONObject jsonObject = custConfigLists.getJSONObject(j);
                logger.info("企业新配置的资源信息" + jsonObject.toString());
                String supplierId = String.valueOf(jsonObject.get("supplierId"));
                logger.info("供应商Id是" + supplierId);
                //添加企业配置的供应商
                if (StringUtil.isNotEmpty(supplierId)) {
                    boolean flag = String.valueOf(suppliers).contains(supplierId);
                    if (!flag) {
                        suppliers.append(supplierId).append(",");
                    }
                }
            }
        }
        return suppliers;
    }

    /**
     * 保存销售定价主表信息
     *
     * @param
     * @return
     */
    private void saveCommonInfo(Map<String, String> map) throws Exception {
        CommonInfoEntity infoDO = new CommonInfoEntity();
        infoDO.setId(IDHelper.getID());
        infoDO.setStatus(0);
        infoDO.setCreateTime(new Timestamp(System.currentTimeMillis()));
        infoDO.setServiceCode(CommonInfoCodeEnum.CUST_SALES_PRICE.getKey());
        //保存主信息
        customerUserDao.saveOrUpdate(infoDO);
        if (map.size() > 0 && map != null) {
            Iterator<String> iter = map.keySet().iterator();
            while (iter.hasNext()) {
                String key = iter.next();
                String value = map.get(key);
                CommonInfoPropertyEntity commonInfoProperty = new CommonInfoPropertyEntity();
                commonInfoProperty.setZid(infoDO.getId());
                commonInfoProperty.setServiceCode(infoDO.getServiceCode());
                commonInfoProperty.setPropertyName(key);
                commonInfoProperty.setPropertyValue(value);
                commonInfoProperty.setCreateTime(new Timestamp(System.currentTimeMillis()));
                customerUserDao.saveOrUpdate(commonInfoProperty);
            }
        }
    }


    /**
     * 更新企业配置信息
     *
     * @author:duanliying
     * @method
     * @date: 2019/3/1 19:43
     */
    public void updateCustConfigAndPrice(String custId, JSONArray custConfigLists) throws Exception {
        if (custConfigLists.size() > 0) {
            for (int j = 0; j < custConfigLists.size(); j++) {
                JSONObject jsonObject = custConfigLists.getJSONObject(j);
                logger.info("企业新配置的资源信息" + jsonObject.toString());
                String resourceId = String.valueOf(jsonObject.get("resourceId"));
                //根据resourceId查询该资源的配置信息
                CustomerProperty sourceProperty = customerDao.getProperty(custId, resourceId + "_config");
                JSONObject oldCustConfig = null;
                if (sourceProperty != null) {
                    String propertyValue = sourceProperty.getPropertyValue();
                    //将原配置json串转化为对象
                    oldCustConfig = JSON.parseObject(propertyValue);
                    logger.info("企业原配置的资源信息" + oldCustConfig.toJSONString());
                    //获取新的json的key
                    Iterator<String> jsonKey = jsonObject.keySet().iterator();
                    while (jsonKey.hasNext()) {
                        // 获得key
                        String key = jsonKey.next();
                        //获取新配置的值，并且根据key修改原有配置的value值
                        String value = jsonObject.getString(key);
                        oldCustConfig.put(key, value);
                    }
                    sourceProperty.setPropertyValue(oldCustConfig.toJSONString());
                   /* if (StringUtil.isNotEmpty(oldCustConfig.getString("type")) && "4".equals(oldCustConfig.getString("type"))) {
                        //如果保存的是外呼类型的需要更新批次下的外显号码
                        String apparentNumber = oldCustConfig.getString(ResourceEnum.CALL.getApparentNumber());
                        if (StringUtil.isNotEmpty(oldCustConfig.getString("supplierId"))) {
                            updateBatchIdApperNum(oldCustConfig.getString("supplierId"), custId, apparentNumber);
                        }
                    }*/
                    //将最新的接送对象保存到
                    customerDao.saveOrUpdate(sourceProperty);
                } else {
                    //直接将json串存进数据库
                    CustomerProperty custSourceProperty = new CustomerProperty();
                    custSourceProperty.setCustId(custId);
                    custSourceProperty.setPropertyName(resourceId + "_config");
                    custSourceProperty.setPropertyValue(jsonObject.toString());
                    custSourceProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                    customerDao.saveOrUpdate(custSourceProperty);
                }
            }
        }
    }

    /**
     * 资源修改后更新批次下的外显号码
     *
     * @author:duanliying
     * @method
     * @date: 2019/4/15 10:33
     */
    private void updateBatchIdApperNum(String supplierId, String custId, String apparentNumber) {
        List<BatchListEntity> batchDetailList = null;
        if (StringUtil.isNotEmpty(supplierId)) {
            batchDetailList = batchDao.getBatchDetailListBySupplierId(custId, Integer.parseInt(supplierId));
        }
        if (batchDetailList.size() > 0) {
            for (int j = 0; j < batchDetailList.size(); j++) {
                String custApparentNumber = batchDetailList.get(j).getApparentNumber();
                if (StringUtil.isNotEmpty(custApparentNumber)) {
                    StringBuffer stringBuffer = new StringBuffer();
                    String[] split = custApparentNumber.split(",");
                    if (split.length > 0) {
                        String[] channelApparent = apparentNumber.split(",");
                        logger.info("批次下原有外显号码" + custApparentNumber);
                        for (int k = 0; k < split.length; k++) {
                            boolean contains = Arrays.asList(channelApparent).contains(split[k]);
                            if (contains) {
                                stringBuffer.append(split[k] + ",");
                            }
                        }
                        if (stringBuffer != null && StringUtil.isNotEmpty(String.valueOf(stringBuffer))) {
                            //删除最后一个，号同时把保存到批次下面的外显号码中
                            stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                        }
                        logger.info("批次下更改后的外显号码" + String.valueOf(stringBuffer));
                        //保存到批次中
                        batchDetailList.get(j).setApparentNumber(String.valueOf(stringBuffer));
                        batchDao.saveOrUpdate(batchDetailList.get(j));
                    }
                }
            }
        }
    }

    /**
     * 获取企业销售定价修改记录
     *
     * @author:duanliying
     * @method
     * @date: 2019/3/19 14:48
     */
    public List<Map<String, Object>> getSalePriceLog(PageParam page, String zid, String custId, String name, String startTime, String endTime) throws Exception {
        StringBuffer sql = new StringBuffer("select g.*,commoninfo.create_time as createTime from (\n");
        sql.append("SELECT cast(a.zid as char) zid,");
        sql.append("GROUP_CONCAT(custId order by custId separator '')custId, ");
        sql.append("GROUP_CONCAT(optUserId order by optUserId separator '')optUserId,\n");
        sql.append("GROUP_CONCAT(updatePriceLog order by updatePriceLog separator '')updatePriceLog\n");
        sql.append("from (select b.zid,b.service_code,");
        sql.append("(case b.property_name when 'custId' then b.property_value else '' end )custId, ");
        sql.append("(case b.property_name when 'optUserId' then b.property_value else '' end )optUserId, ");
        sql.append("(case b.property_name when 'updatePriceLog' then b.property_value else '' end )updatePriceLog ");
        sql.append("FROM t_common_info_property b where service_code='" + CommonInfoCodeEnum.CUST_SALES_PRICE.getKey() + "' group by b.zid,b.service_code,b.property_name ");
        sql.append(")a GROUP BY zid,service_code)g ");
        sql.append("left join t_common_info commoninfo on g.zid=commoninfo.id ");
        sql.append("LEFT JOIN t_user b ON b.id = g.optUserId where 1=1 ");
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" AND g.custId = " + custId);
        }
        if (StringUtil.isNotEmpty(startTime)) {
            sql.append(" and commoninfo.create_time >='" + startTime + "'");
        }
        if (StringUtil.isNotEmpty(endTime)) {
            sql.append(" and commoninfo.create_time <='" + endTime + "'");
        }
        if (StringUtil.isNotEmpty(name)) {
            sql.append(" and b.name='" + name + "'");
        }
        if (StringUtil.isNotEmpty(zid)) {
            sql.append(" and g.zid='" + zid + "'");
        }
        sql.append(" order by commoninfo.create_time desc");
        PageList pageData = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        List<Map<String, Object>> list = null;
        if (pageData != null) {
            list = pageData.getList();
            for (int i = 0; i < list.size(); i++) {
                String optUserId = String.valueOf(list.get(i).get("optUserId"));
                //根据optUserId查询操作人信息
                if (StringUtil.isNotEmpty(optUserId)) {
                    List<Map<String, Object>> userAllMessage = userDao.getUserAllMessage(NumberConvertUtil.parseLong(optUserId));
                    if (userAllMessage.size() > 0) {
                        list.get(i).put("userId", userAllMessage.get(0).get("id"));
                        list.get(i).put("realName", userAllMessage.get(0).get("realName"));
                        list.get(i).put("account", userAllMessage.get(0).get("account"));
                        list.get(i).put("deptName", userAllMessage.get(0).get("deptName"));
                        list.get(i).put("roleName", userAllMessage.get(0).get("roleName"));
                    }
                }
            }
        }
        return list;
    }

    public void updateServicePrice(String custId, String price) throws Exception {
        //查询企业属性表是否存在
        logger.info("查询的企业id是：" + custId);
        CustomerProperty customerProperty = customerDao.getProperty(custId, "address_fix_price");
        if (customerProperty != null) {
            customerProperty.setPropertyValue(price);
        } else {
            customerProperty = new CustomerProperty();
            customerProperty.setCustId(custId);
            customerProperty.setPropertyName("address_fix_price");
            customerProperty.setPropertyValue(price);
            customerProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));

        }
        customerDao.saveOrUpdate(customerProperty);
    }

    /**
     * @description 根据type和企业id查询号码保护状态
     * @method
     * @date: 2019/7/2 11:19
     */
    public boolean getProtectStatusByCust(int type, String custId) {
        boolean status = false;
        String propertyName = null;
        if (1 == type) {
            // 导入/添加的线索手机号
            propertyName = "cluePhone";
        } else if (2 == type) {
            propertyName = "customerPhone";
        }
        CustomerProperty cluePhone = customerDao.getProperty(custId, propertyName);
        if (cluePhone == null || StringUtil.isEmpty(cluePhone.getPropertyValue())) {
            logger.warn("custId是：" + custId + "未配置号码保护");
            return status;
        }
        if ("1".equals(cluePhone.getPropertyValue())) {
            status = true;
        }
        return status;
    }

    /**
     * @description 号码保护设置
     * @method
     * @date: 2019/7/2 11:19
     */
    public void setPhoneProtectByCust(String type, String status, String custId) throws Exception {
        String propertyName = null;
        if ("1".equals(type)) {
            // 导入/添加的线索手机号
            propertyName = "cluePhone";
        } else if ("2".equals(type)) {
            propertyName = "customerPhone";
        }
        CustomerProperty cluePhone = customerDao.getProperty(custId, propertyName);
        if (cluePhone != null) {
            cluePhone.setPropertyValue(status);
            customerDao.saveOrUpdate(cluePhone);
        } else {
            CustomerProperty customerProperty = new CustomerProperty();
            customerProperty.setCustId(custId);
            customerProperty.setPropertyName(propertyName);
            customerProperty.setPropertyValue(status);
            customerProperty.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            customerDao.saveOrUpdate(customerProperty);
        }
    }

    public boolean judRemainAmount(String cust_id) {
        boolean judge = true;
        CustomerProperty customerProperty = customerDao.getProperty(cust_id, "remain_amount");
        if (customerProperty == null) {
            judge = false;
        } else {
            if (StringUtil.isEmpty(customerProperty.getPropertyValue())) {
                judge = false;
            } else {
                double remain_amount = Double.parseDouble(customerProperty.getPropertyValue());
                // 查询是否有授信额度
                CustomerProperty st = customerDao.getProperty(cust_id, "settlement_type");
                int settlementType = 1;
                if (st != null && st.getPropertyValue() != null) {
                    settlementType = NumberConvertUtil.parseInt(st.getPropertyValue());
                }
                int creditAmount = 0;
                // 查询授信额度
                if (2 == settlementType) {
                    CustomerProperty credit = customerDao.getProperty(cust_id, "creditAmount");
                    if (credit != null && credit.getPropertyValue() != null) {
                        creditAmount = NumberConvertUtil.changeY2L(credit.getPropertyValue());
                    }
                    // 如果授信额度小于透支额度
                    if (remain_amount <= 0 && creditAmount < Math.abs(remain_amount)) {
                        logger.warn("客户:" + cust_id + "授信额度不足,授信额度:" + creditAmount + ",余额:" + remain_amount);
                        judge = false;
                    }
                } else {
                    // 标准价格
                    Integer priceStand = 500;
                    if (remain_amount < priceStand) {
                        // 余额不足
                        judge = false;
                    }
                }
            }
        }
        return judge;
    }


    public Map<String, Object> getUsersByCondition(UserQueryParam param, LoginUser lu) {
        Map<String, Object> map = new HashMap<>();

        StringBuffer hql = new StringBuffer("from Customer m where 1=1 and brand_id is null");
        List values = new ArrayList();
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            hql.append(" and m.enterpriseName like ?");
            values.add("%" + param.getEnterpriseName() + "%");
        }
        if (StringUtil.isNotEmpty(param.getStatus())) {
            hql.append(" and m.status = ?");
            values.add(Integer.parseInt(param.getStatus()));
        }
        // 处理用户ID搜索条件
        if (StringUtil.isNotEmpty(param.getUserId())) {
            CustomerUser customerUser = customerUserDao.get(Long.parseLong(param.getUserId()));
            if (customerUser != null) {
                hql.append(" and m.custId = ?");
                values.add(customerUser.getCust_id());
            } else {
                map.put("total", 0);
                map.put("customerList", new ArrayList<>());
                return map;
            }
        }
        // 处理用户名称搜索条件(不支持模糊搜索)
        if (StringUtil.isNotEmpty(param.getUserName())) {
            CustomerUser customerUser = customerUserDao.getCustomerUserByLoginName(param.getUserName());
            if (customerUser != null) {
                hql.append(" and m.custId = ?");
                values.add(customerUser.getCust_id());
            } else {
                map.put("total", 0);
                map.put("customerList", new ArrayList<>());
                return map;
            }
        }
        // 处理呼叫类型搜索
        if (StringUtil.isNotEmpty(param.getCallCenterType())) {
            hql.append(" AND m.custId IN (SELECT custId FROM CustomerProperty WHERE propertyName ='call_config' AND propertyValue LIKE ? ) ");
            values.add("%\"type\":\"" + param.getCallCenterType() + "\"%");
        }

        if ("ROLE_USER".equals(lu.getRole())) {
            String custIdsStr = userGroupService.getCustomerIdByuId(lu.getId().toString());
            if (StringUtil.isNotEmpty(custIdsStr)) {
                hql.append("and m.custId in(" + custIdsStr + ")");
            }
        }

        hql.append(" order by m.createTime DESC ");

        Page p = customerDao.page(hql.toString(), values, param.getPageNum(), param.getPageSize());

        List data = new ArrayList();
        List<CustomerUser> us;
        Customer cust;
        CustomerDTO cd;
        CustomerUserPropertyDO mobileNum;
        CustomerProperty enterpriseAddress, bliNumber, bliPic, taxPayerNum, taxPic, bankName,
                bankAccount, bankAccountPic, settlementType, creditAmountProperty, callConfig, apparentNumberRule, apparentNumber;
        CustomerUser u;
        JSONObject callConfigProperty;
        ApparentNumberQueryParam apparentNumberQuery;
        List<ApparentNumber> apparentNumbers;
        for (int i = 0; i < p.getData().size(); i++) {
            cust = (Customer) p.getData().get(i);
            cd = new CustomerDTO(cust);

            enterpriseAddress = customerDao.getProperty(cust.getCustId(), "reg_address");
            bliNumber = customerDao.getProperty(cust.getCustId(), "bli_number");
            bliPic = customerDao.getProperty(cust.getCustId(), "bli_path");
            taxPayerNum = customerDao.getProperty(cust.getCustId(), "taxPayerNum");
            taxPic = customerDao.getProperty(cust.getCustId(), "taxpayer_certificate_path");
            bankName = customerDao.getProperty(cust.getCustId(), "bankName");
            bankAccount = customerDao.getProperty(cust.getCustId(), "bankAccount");
            bankAccountPic = customerDao.getProperty(cust.getCustId(), "bank_account_certificate");
            CustomerProperty serviceMode = customerDao.getProperty(cust.getCustId(), "service_mode");
            if (enterpriseAddress != null) cd.setEnterpriseAddress(enterpriseAddress.getPropertyValue());
            if (bliNumber != null) cd.setBliNumber(bliNumber.getPropertyValue());
            if (bliPic != null)
                cd.setBliPic(ConfigUtil.getInstance().get("pic_server_url") + "/" + cust.getCustId() + "/'" + bliPic.getPropertyValue());
            if (taxPayerNum != null) cd.setTaxPayerNum(taxPayerNum.getPropertyValue());
            if (taxPic != null)
                cd.setTaxPic(ConfigUtil.getInstance().get("pic_server_url") + "/" + cust.getCustId() + "/'" + taxPic.getPropertyValue());
            if (bankName != null) cd.setBankName(bankName.getPropertyValue());
            if (bankAccount != null) cd.setBankAccount(bankAccount.getPropertyValue());
            if (bankAccountPic != null)
                cd.setBankAccountPic(ConfigUtil.getInstance().get("pic_server_url") + "/" + cust.getCustId() + "/'" + bankAccountPic.getPropertyValue());

            us = customerUserDao.find("from CustomerUser m where m.cust_id='" + cust.getCustId() + "' and m.userType='1'");
            if (us.size() > 0) {
                u = us.get(0);
                cd.setUserId(u.getId().toString());
                cd.setUsername(u.getAccount());
                mobileNum = customerUserDao.getProperty(u.getId().toString(), "mobile_num");
                cd.setMobileNum(mobileNum == null ? "" : mobileNum.getPropertyValue());
            }
            // 结算类型
            settlementType = customerDao.getProperty(cust.getCustId(), "settlement_type");
            if (settlementType != null && StringUtil.isNotEmpty(settlementType.getPropertyValue())) {
                cd.setSettlementType(NumberConvertUtil.parseInt(settlementType.getPropertyValue()));
                //查询授信额度
                if (cd.getSettlementType() != null && 2 == cd.getSettlementType()) {
                    creditAmountProperty = customerDao.getProperty(cust.getCustId(), "creditAmount");
                    cd.setCreditAmount(creditAmountProperty != null ? creditAmountProperty.getPropertyValue() : "");
                }
            }

            callConfig = customerDao.getProperty(cust.getCustId(), "call_config");
            if (callConfig != null && StringUtil.isNotEmpty(callConfig.getPropertyValue())) {
                Object object = JSON.parse(callConfig.getPropertyValue());
                // 兼容之前的数据格式
                JSONObject jsonObject;
                JSONArray callConfigs = null;
                if (object instanceof JSONObject) {
                    jsonObject = (JSONObject) object;
                    callConfigs = new JSONArray();
                    callConfigs.add(jsonObject);
                } else if (object instanceof JSONArray) {
                    callConfigs = (JSONArray) object;
                }
                if (callConfigs != null && callConfigs.size() > 0) {
                    cd.setCallType(callConfigs.getJSONObject(0).getInteger("type"));
                }
            }

            // 查询有效外显个数
            apparentNumberQuery = new ApparentNumberQueryParam();
            apparentNumberQuery.setCustId(cust.getCustId());
            apparentNumberQuery.setStatus(1);
            apparentNumberQuery.setStopStatus(1);
            apparentNumbers = customerDao.listApparentNumber(apparentNumberQuery);
            cd.setApparentNumberNum(apparentNumbers.size());
            if (serviceMode != null && StringUtil.isNotEmpty(serviceMode.getPropertyValue())) {
                cd.setServiceMode(serviceMode.getPropertyValue());
            }
            // 外显使用规则
            apparentNumberRule = customerDao.getProperty(cust.getCustId(), "apparent_number_rule");
            if (apparentNumberRule != null && StringUtil.isNotEmpty(apparentNumberRule.getPropertyValue())) {
                cd.setApparentNumberRule(apparentNumberRule.getPropertyValue());
            }
            // 指定的外显号
            apparentNumber = customerDao.getProperty(cust.getCustId(), "apparent_number");
            if (apparentNumber != null && StringUtil.isNotEmpty(apparentNumber.getPropertyValue())) {
                cd.setApparentNumber(apparentNumber.getPropertyValue());
            }


            data.add(cd);
        }
        map.put("total", p.getTotal());
        map.put("customerList", data);
        return map;


//        StringBuilder sql = new StringBuilder("" +
//                "SELECT" +
//                "t1.`NAME` AS userName," +
//                "CAST(t1.id AS CHAR) AS userId," +
//                "t1.mobile_num AS mobileNum," +
//                "t1.enterprise_name AS enterpriseName," +
//                "t1.create_time AS createTime," +
//                "ifnull(t1.source,0) AS source," +
//                "t1.`STATUS` AS status" +
//                "FROM" +
//                "t_user t1 where 1=1 and t1.user_type=1 ");
//        if (StringUtil.isNotEmpty(param.getUserName())) {
//            sql.append(" and t1.name like '%").append(StringEscapeUtils.escapeSql(param.getUserName())).append("%'");
//        }
//        if (StringUtil.isNotEmpty(param.getUserId())) {
//            sql.append(" and t1.id='").append(StringEscapeUtils.escapeSql(param.getUserId())).append("'");
//        }
//        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
//            sql.append(" and t1.enterprise_name like '%").append(StringEscapeUtils.escapeSql(param.getEnterpriseName())).append("%'");
//        }
//        if (StringUtil.isNotEmpty(param.getStatus())) {
//            sql.append(" and t1.status = '").append(StringEscapeUtils.escapeSql(param.getStatus())).append("'");
//        }
//        sql.append(" order by t1.create_time desc ");
//
//        map.put("total", userInfoDao.getSQLQuery(sql.toString()).list().size());
//        map.put("customerList", userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list());
//        return map;
    }

    public Customer getUserByEnterpriseName(String enterpriseName) {
        return customerDao.findUniqueBy("enterpriseName", enterpriseName);
    }


    public CustomerDTO getCustomerInfoById(String custId) throws Exception {


        if (StringUtil.isEmpty(custId)) {
            throw new TouchException("300", "系统异常，用户查询失败");
        }

        Customer c = customerDao.get(custId);
        if (c == null) {
            throw new TouchException("300", "系统异常，用户查询失败");
        }

        CustomerProperty enterpriseAddress = customerDao.getProperty(custId, "address");
        CustomerProperty bliNumber = customerDao.getProperty(custId, "bliNumber");
        CustomerProperty bliPic = customerDao.getProperty(custId, "bliPath");
        CustomerProperty taxPayerNum = customerDao.getProperty(custId, "taxPayerNum");
        CustomerProperty taxPic = customerDao.getProperty(custId, "taxpayerCertificatePath");
        CustomerProperty bankName = customerDao.getProperty(custId, "bank");
        CustomerProperty bankAccount = customerDao.getProperty(custId, "bankAccount");
        CustomerProperty bankAccountPic = customerDao.getProperty(custId, "bankAccountCertificate");
        // 地址
        CustomerProperty city = customerDao.getProperty(custId, "city");
        CustomerProperty province = customerDao.getProperty(custId, "province");
        CustomerProperty country = customerDao.getProperty(custId, "country");
        //CustomerProperty industryId = customerDao.getProperty(custId, "industryId");
        CustomerProperty service_mode = customerDao.getProperty(custId, "service_mode");

        CustomerDTO cd = new CustomerDTO(c);
        String picServerUrl = "";
        if (bliPic != null) {
            cd.setBliPic(picServerUrl + "upload/pic/" + bliPic.getPropertyValue());
        } else {
            cd.setBliPic("");
        }
        if (taxPic != null) {
            cd.setTaxPic(picServerUrl + "upload/pic/" + taxPic.getPropertyValue());
        } else {
            cd.setTaxPic("");
        }
        if (bankAccountPic != null) {
            cd.setBankAccountPic(picServerUrl + "upload/pic/" + bankAccountPic.getPropertyValue());
        } else {
            cd.setBankAccountPic("");
        }
        if (city != null) {
            cd.setCity(city.getPropertyValue());
        }
        if (province != null) {
            cd.setProvince(province.getPropertyValue());
        }
        if (country != null) {
            cd.setCounty(country.getPropertyValue());
        }
        if (enterpriseAddress != null) {
            cd.setEnterpriseAddress(enterpriseAddress.getPropertyValue());
        }
        if (bliNumber != null) {
            cd.setBliNumber(bliNumber.getPropertyValue());
        }
        if (taxPayerNum != null) {
            cd.setTaxPayerNum(taxPayerNum.getPropertyValue());
        }
        if (bankName != null) {
            cd.setBankName(bankName.getPropertyValue());
        }
        if (bankAccount != null) {
            cd.setBankAccount(bankAccount.getPropertyValue());
        }
        if (service_mode != null) {
            cd.setServiceMode(service_mode.getPropertyValue());
        }
        /*if (industryId != null) {
            cd.setIndustryId(industryId.getPropertyValue());
            if (StringUtil.isNotEmpty(industryId.getPropertyValue())) {
                Industry industry = industryInfoDao.get(Integer.parseInt(industryId.getPropertyValue()));
                if (industry != null) {
                    cd.setIndustryName(industry.getIndustryName());
                }
            }
        }*/
        List<CustomerUser> us = customerUserDao.find("from CustomerUser m where m.cust_id='" + custId + "' and m.userType='1'");
        if (us.size() > 0) {
            CustomerUser u = us.get(0);
            cd.setUserId(u.getId().toString());
            cd.setUsername(u.getAccount());
            cd.setRealname(u.getRealname());

            CustomerUserPropertyDO mobile_num = customerUserDao.getProperty(u.getId().toString(), "mobile_num");
            cd.setMobileNum(mobile_num == null ? "" : mobile_num.getPropertyValue());

            CustomerUserPropertyDO email = customerUserDao.getProperty(u.getId().toString(), "email");
            cd.setEmail(email == null ? "" : email.getPropertyValue());

            CustomerUserPropertyDO title = customerUserDao.getProperty(u.getId().toString(), "title");
            cd.setTitle(title == null ? "" : title.getPropertyValue());
        }

        return cd;
    }


    public void updateCustomer(CustomerInfoVO vo) throws Exception {

        // try {
        List<CustomerUser> us = customerUserDao.find("from CustomerUser m where m.cust_id='" + vo.getUserId() + "' and m.userType='1'");
        CustomerUser user = null;
        if (us.size() > 0) {
            user = us.get(0);
        } else {
            throw new TouchException("更新客户失败,custId:" + vo.getUserId() + "下无管理员账号");
        }
        String userId = String.valueOf(user.getId());
        user.setRealname(vo.getRealName());
        customerUserDao.saveOrUpdate(user);

        CustomerUserPropertyDO mobileNum = customerUserDao.getProperty(userId, "mobile_num");
        if (mobileNum == null) {
            mobileNum = new CustomerUserPropertyDO(userId, "mobile_num", vo.getMobileNum(), new Timestamp(System.currentTimeMillis()));
        } else {
            mobileNum.setPropertyValue(vo.getMobileNum());
        }
        customerUserDao.saveOrUpdate(mobileNum);

        CustomerUserPropertyDO email = customerUserDao.getProperty(userId, "email");
        if (email == null) {
            email = new CustomerUserPropertyDO(userId, "email", vo.getEmail(), new Timestamp(System.currentTimeMillis()));
        } else {
            email.setPropertyValue(vo.getEmail());
        }
        customerUserDao.saveOrUpdate(email);

        CustomerUserPropertyDO title = customerUserDao.getProperty(userId, "title");
        if (title == null) {
            title = new CustomerUserPropertyDO(userId, "title", vo.getTitle(), new Timestamp(System.currentTimeMillis()));
        } else {
            title.setPropertyValue(vo.getTitle());
        }
        customerUserDao.saveOrUpdate(title);

        CustomerProperty enterpriseAddress = customerDao.getProperty(user.getCust_id(), "address");
        if (enterpriseAddress == null) {
            enterpriseAddress = new CustomerProperty(user.getCust_id(), "address", vo.getAddress());
        } else {
            enterpriseAddress.setPropertyValue(vo.getAddress());
        }
        customerDao.saveOrUpdate(enterpriseAddress);

        CustomerProperty bliNumber = customerDao.getProperty(user.getCust_id(), "bliNumber");
        if (bliNumber == null) {
            bliNumber = new CustomerProperty(user.getCust_id(), "bliNumber", vo.getBliNumber());
        } else {
            bliNumber.setPropertyValue(vo.getBliNumber());
        }
        customerDao.saveOrUpdate(bliNumber);

        CustomerProperty bliPic = customerDao.getProperty(user.getCust_id(), "bliPath");
        if (bliPic == null) {
            bliPic = new CustomerProperty(user.getCust_id(), "bliPath", vo.getBliPath());
        } else {
            bliPic.setPropertyValue(vo.getBliPath());
        }
        customerDao.saveOrUpdate(bliPic);

        CustomerProperty taxPayerNum = customerDao.getProperty(user.getCust_id(), "taxPayerNum");
        if (taxPayerNum == null) {
            taxPayerNum = new CustomerProperty(user.getCust_id(), "taxPayerNum", vo.getTaxPayerNum());
        } else {
            taxPayerNum.setPropertyValue(vo.getTaxPayerNum());
        }
        customerDao.saveOrUpdate(taxPayerNum);

        CustomerProperty taxPic = customerDao.getProperty(user.getCust_id(), "taxpayerCertificatePath");
        if (taxPic == null) {
            taxPic = new CustomerProperty(user.getCust_id(), "taxpayerCertificatePath", vo.getTaxPayerPath());
        } else {
            taxPic.setPropertyValue(vo.getTaxPayerPath());
        }
        customerDao.saveOrUpdate(taxPic);

        CustomerProperty bankName = customerDao.getProperty(user.getCust_id(), "bank");
        if (bankName == null) {
            bankName = new CustomerProperty(user.getCust_id(), "bank", vo.getBankName());
        } else {
            bankName.setPropertyValue(vo.getBankName());
        }
        customerDao.saveOrUpdate(bankName);

        CustomerProperty bankAccount = customerDao.getProperty(user.getCust_id(), "bankAccount");
        if (bankAccount == null) {
            bankAccount = new CustomerProperty(user.getCust_id(), "bankAccount", vo.getBankAccount());
        } else {
            bankAccount.setPropertyValue(vo.getBankAccount());
        }
        customerDao.saveOrUpdate(bankAccount);
        CustomerProperty bankAccountPic = customerDao.getProperty(user.getCust_id(), "bankAccountCertificate");
        if (bankAccountPic == null) {
            bankAccountPic = new CustomerProperty(user.getCust_id(), "bankAccountCertificate", vo.getBankPath());
        } else {
            bankAccountPic.setPropertyValue(vo.getBankPath());
        }
        customerDao.saveOrUpdate(bankAccountPic);
        // 地址
        CustomerProperty city = customerDao.getProperty(user.getCust_id(), "city");
        if (city == null) {
            city = new CustomerProperty(user.getCust_id(), "city", vo.getCity());
        } else {
            city.setPropertyValue(vo.getCity());
        }
        customerDao.saveOrUpdate(city);
        CustomerProperty province = customerDao.getProperty(user.getCust_id(), "province");
        if (province == null) {
            province = new CustomerProperty(user.getCust_id(), "province", vo.getProvince());
        } else {
            province.setPropertyValue(vo.getProvince());
        }
        customerDao.saveOrUpdate(province);

        CustomerProperty country = customerDao.getProperty(user.getCust_id(), "country");
        if (country == null) {
            country = new CustomerProperty(user.getCust_id(), "country", vo.getCounty());
        } else {
            country.setPropertyValue(vo.getCounty());
        }
        customerDao.saveOrUpdate(country);

      /*  EnterpriseDO enterpriseDO = enterpriseDao.findUniqueBy("custId", user.getCustId());
        enterpriseDO.setProvince(vo.getProvince());
        enterpriseDO.setCity(vo.getCity());
        enterpriseDO.setCounty(vo.getCounty());
        enterpriseDO.setRegAddress(vo.getAddress());
        enterpriseDO.setBliNumber(vo.getBliNumber());
        enterpriseDO.setBliPath(vo.getBliPath());
        enterpriseDO.setTaxpayerId(vo.getTaxPayerNum());
        enterpriseDO.setTaxpayerCertificatePath(vo.getTaxPayerPath());
        enterpriseDO.setBank(vo.getBankName());
        enterpriseDO.setBankAccount(vo.getBankAccount());
        enterpriseDO.setBankAccountCertificate(vo.getBankPath());
        enterpriseDao.save(enterpriseDO);

        //更新用户企业
        // 状态（0正常 1.冻结 2资质未认证3.删除4.审核中5.审核失败)
        String sql = "UPDATE t_customer_user SET `STATUS`=4 where id=? ";
        jdbcTemplate.update(sql, vo.getUserId());*/
//        } catch (Exception e) {
//            throw new TouchException("300", e.getMessage());
//        }

    }


    public Map<String, Object> getCustomerCallCenterType(String customerId, String userId) {
        Map<String, Object> data = new HashMap<>(16);
        //判断当前用户的user_type
        CustomerProperty callCenterId = customerDao.getProperty(customerId, "call_center_id");
        CustomerProperty apparentNumber = customerDao.getProperty(customerId, "apparent_number");
        if (callCenterId != null && apparentNumber != null) {
            // 查询坐席的开通状态
            CustomerUserPropertyDO seatsStatus = customerUserDao.getProperty(userId, "seats_status");
            if (seatsStatus != null && "1".equals(seatsStatus.getPropertyValue())) {
                CustomerUserPropertyDO seatsAccount = customerUserDao.getProperty(userId, "seats_account");
                CustomerUserPropertyDO seatsPassword = customerUserDao.getProperty(userId, "seats_password");
                CustomerUserPropertyDO extensionNumber = customerUserDao.getProperty(userId, "extension_number");
                CustomerUserPropertyDO extensionPassword = customerUserDao.getProperty(userId, "extension_password");
                if (seatsAccount != null && seatsPassword != null && extensionNumber != null && extensionPassword != null) {
                    // 呼叫中心
                    data.put("callType", 1);
                    Map<String, Object> callData = new HashMap<>();
                    callData.put("userId", userId);
                    callData.put("callCenterId", callCenterId.getPropertyValue());
                    callData.put("apparentNumber", apparentNumber.getPropertyValue());
                    callData.put("account", seatsAccount.getPropertyValue());
                    callData.put("password", seatsPassword.getPropertyValue());
                    callData.put("extensionNumber", extensionNumber.getPropertyValue());
                    callData.put("extensionPassword", extensionPassword.getPropertyValue());
                    data.put("callData", callData);
                    return data;
                }
            }
        }
        // 双向呼叫
        data.put("callType", 2);
        return data;
    }

    public Map<String, Object> getCustomerCallCenterType_V1(String customerId, String userId) {
        Map<String, Object> data = new HashMap<>(16);
        CustomerProperty callCenterType = customerDao.getProperty(customerId, "call_center_type");

        if (callCenterType != null) {
            data.put("callCenterType", callCenterType.getPropertyValue());
            if ("xf".equals(callCenterType.getPropertyValue())) {
                //判断当前用户的user_type
                CustomerProperty callCenterId = customerDao.getProperty(customerId, "xf_call_center_id");
                CustomerProperty callCenterIp = customerDao.getProperty(customerId, "xf_call_center_ip");
                CustomerProperty xfCallSipPort = customerDao.getProperty(customerId, "xf_call_sip_port");
                CustomerProperty xfCallSipPwd = customerDao.getProperty(customerId, "xf_call_sip_pwd");
                if (callCenterId != null && callCenterIp != null && xfCallSipPort != null && xfCallSipPwd != null) {
                    // 查询坐席的开通状态
                    // TODO 先查询讯众的坐席信息,新方系统坐席一对一对应关系
                    CustomerUserPropertyDO xfSeatsAccount = customerUserDao.getProperty(userId, "seats_account");
                    if (xfSeatsAccount != null) {
                        // 呼叫中心
                        data.put("callCenterId", callCenterId.getPropertyValue());
                        data.put("callCenterIp", callCenterIp.getPropertyValue());
                        data.put("xfCallSipPort", xfCallSipPort.getPropertyValue());
                        data.put("xfCallSipPwd", xfCallSipPwd.getPropertyValue());
                        data.put("xfSeatsAccount", xfSeatsAccount.getPropertyValue());
                        return data;
                    }
                }
            }
        }
        CustomerProperty callCenterId = customerDao.getProperty(customerId, "call_center_id");
        CustomerProperty apparentNumber = customerDao.getProperty(customerId, "apparent_number");
        if (callCenterId != null && apparentNumber != null) {
            // 查询坐席的开通状态
            //CustomerUserPropertyDO seatsStatus = customerUserDao.getProperty(userId, "seats_status");
            //if (seatsStatus != null && "1".equals(seatsStatus.getPropertyValue())) {
            CustomerUserPropertyDO seatsAccount = customerUserDao.getProperty(userId, "seats_account");
            CustomerUserPropertyDO seatsPassword = customerUserDao.getProperty(userId, "seats_password");
            CustomerUserPropertyDO extensionNumber = customerUserDao.getProperty(userId, "extension_number");
            CustomerUserPropertyDO extensionPassword = customerUserDao.getProperty(userId, "extension_password");
            if (seatsAccount != null && seatsPassword != null && extensionNumber != null && extensionPassword != null) {
                // 呼叫中心
                data.put("userId", userId);
                data.put("callCenterId", callCenterId.getPropertyValue());
                data.put("apparentNumber", apparentNumber.getPropertyValue());
                data.put("account", seatsAccount.getPropertyValue());
                data.put("password", seatsPassword.getPropertyValue());
                data.put("extensionNumber", extensionNumber.getPropertyValue());
                data.put("extensionPassword", extensionPassword.getPropertyValue());

                data.put("callCenterType", "xz");
                return data;
            }
            //}
        }
        data.put("callCenterType", "twoCall");
        return data;
    }

    /**
     * 获取客户群的呼叫类型
     * 1.如果客户群属性表配置了呼叫类型,则使用客户群的呼叫类型
     * 2.如果客户群没有配置呼叫类型,则使用客户配置的呼叫类型
     * 使用上述获取到的呼叫类型,首先查询坐席有没有配置此类型的呼叫中心配置,如果没有则使用客户+坐席做为呼叫中心配置
     *
     * @param customerId      客户ID
     * @param customerGroupId 客户群ID
     * @param userId          用户ID
     * @param resourceId      资源ID
     * @return
     */
    public SeatCallCenterConfig getCustomerGroupCallCenterType(String customerId, String customerGroupId, String userId, String resourceId) {
        // 获取客户群的呼叫中心配置
        CustomerGroupProperty cgp = customGroupDao.getProperty(NumberConvertUtil.parseInt(customerGroupId), "call_center_type");
        String callCenterType = null;
        if (cgp != null) {
            callCenterType = cgp.getPropertyValue();
            logger.info("客户:" + customerId + ",客群:" + customerGroupId + "呼叫类型:" + callCenterType);
        } else {
            logger.warn("客户:" + customerId + ",客群:" + customerGroupId + "未配置呼叫类型,开始读取企业呼叫中心配置");
            // 读取客户的呼叫中心配置
            CustomerProperty custCallCenterType = customerDao.getProperty(customerId, "call_center_type");
            if (custCallCenterType != null) {
                callCenterType = custCallCenterType.getPropertyValue();
                logger.info("客户:" + customerId + "呼叫类型:" + callCenterType);
            } else {
                logger.warn("客户:" + customerId + "未进行配置呼叫类型");
                return null;
            }
        }

        // 根据呼叫类型读取用户的呼叫中心配置
        CallCenterConfig callCenterConfig = seatsService.selectUserCallCenterConfig(userId, callCenterType, resourceId);
        if (callCenterConfig == null) {
            logger.warn("客户:" + customerId + ",用户:" + userId + "未查询到呼叫中心配置,开始读取企业呼叫中心配置");
            // 根据呼叫类型读取客户的呼叫中心配置
            callCenterConfig = this.selectCustomerConfig(customerId, callCenterType, resourceId);
        }

        if (callCenterConfig == null) {
            logger.warn("客户:" + customerId + ",呼叫类型:" + callCenterType + "未查询到呼叫中心配置");
            return null;
        }
        SeatCallConfig seatConfig = seatsService.selectUserSeatConfig(userId, callCenterType, resourceId);
        if (seatConfig == null) {
            logger.warn("客户:" + customerId + ",用户" + userId + "未查询到呼叫中心坐席配置");
        }
        // 组装坐席呼叫配置数据
        SeatCallCenterConfig data = new SeatCallCenterConfig(callCenterConfig, seatConfig);
        data.setCallCenterType(callCenterType);
        return data;
    }

    /**
     * 查询企业的呼叫中心配置信息
     *
     * @param custId
     * @param callCenterType
     * @param resourceId
     * @return
     */
    public CallCenterConfig selectCustomerConfig(String custId, String callCenterType, String resourceId) {
        CallCenterConfig config = new CallCenterConfig();
        boolean configStatus = false;
        if (CallCenterTypeEnum.XF.getPropertyName().equals(callCenterType)) {
            CustomerProperty callCenterId = customerDao.getProperty(custId, "xf_call_center_id");
            CustomerProperty callCenterIp = customerDao.getProperty(custId, "xf_call_center_ip");
            CustomerProperty xfCallSipPort = customerDao.getProperty(custId, "xf_call_sip_port");
            CustomerProperty xfCallSipPwd = customerDao.getProperty(custId, "xf_call_sip_pwd");
            if (callCenterId != null && callCenterIp != null && xfCallSipPort != null && xfCallSipPwd != null) {
                // 呼叫中心
                config.setCallCenterId(callCenterId.getPropertyValue());
                config.setCallCenterIp(callCenterIp.getPropertyValue());
                config.setXfCallSipPort(xfCallSipPort.getPropertyValue());
                config.setXfCallSipPwd(xfCallSipPwd.getPropertyValue());
                configStatus = true;
            }
        } else if (CallCenterTypeEnum.XZ_CC.getPropertyName().equals(callCenterType)) {
            CustomerProperty callCenterId = customerDao.getProperty(custId, "call_center_id");
            CustomerProperty apparentNumber = customerDao.getProperty(custId, "apparent_number");
            if (callCenterId != null && apparentNumber != null) {
                config.setCallCenterId(callCenterId.getPropertyValue());
                config.setApparentNumber(apparentNumber.getPropertyValue());
                configStatus = true;
            }
        } else if (CallCenterTypeEnum.XZ_SH.getPropertyName().equals(callCenterType)) {
            CustomerProperty appId = customerDao.getProperty(custId, "appId");
            if (appId != null) {
                config.setCallCenterAppIp(appId.getPropertyValue());
                configStatus = true;
            }
        } else {
            logger.warn("未获取到客户:" + custId + ",呼叫中心类型:" + callCenterType + ",resourceId:" +
                    resourceId + "未配置:" + callCenterType + "呼叫中心配置");
            return null;
        }
        // 未读取到对应的呼叫中心配置
        if (!configStatus) {
            logger.warn("客户:" + custId + ",呼叫中心类型:" + callCenterType + ",resourceId:" +
                    resourceId + "未配置:" + callCenterType + "呼叫中心配置");
            return null;
        }
        return config;
    }

    /**
     * 获取客户配置的外显号
     * 规则分为 random-随机  poll-轮询  specify-指定 none-不用我方规则
     *
     * @param custId         客户ID
     * @param callCenterType 呼叫类型
     * @param resourceId     渠道ID
     * @return
     */
    public String getCustomerApparentNumber(String custId, String callCenterType, String resourceId) {
        CustomerProperty apparentNumberRule = customerDao.getProperty(custId, resourceId + "_apparent_number_rule");
        if (apparentNumberRule != null) {
            logger.info("客户:" + custId + ",渠道ID:" + resourceId + ",外显号规则:" + apparentNumberRule.getPropertyValue());
            if ("none".equals(apparentNumberRule.getPropertyValue())) {
                logger.warn("客户:" + custId + ",渠道ID:" + resourceId + ",外显号不用我方规则");
                return null;
            } else if ("specify".equals(apparentNumberRule.getPropertyValue())) {
                CustomerProperty apparentNumber = customerDao.getProperty(custId, resourceId + "_apparent_number");
                if (apparentNumber == null) {
                    logger.warn("客户:" + custId + ",渠道ID:" + resourceId + ",未指定外显号");
                    return null;
                }
                return apparentNumber.getPropertyValue();
                // 轮询模式
            } else if ("poll".equals(apparentNumberRule.getPropertyValue())) {
                // 查询企业有效外显号列表
                ApparentNumberQueryParam m = new ApparentNumberQueryParam();
                m.setCustId(custId);
                m.setStatus(1);
                List<ApparentNumberDTO> numberList = listApparentNumber(m);
                if (numberList.size() == 0) {
                    logger.warn("客户:" + custId + ",渠道ID:" + resourceId + ",未添加外显号");
                }
                int total = numberList.size();
                int currentIndex = total - 1;
                // 查询上次外显号轮询index
                CustomerProperty apparentNumberPollIndex = customerDao.getProperty(custId, resourceId + "_apparent_number_poll_index");
                if (apparentNumberPollIndex != null) {
                    currentIndex = NumberConvertUtil.parseInt(apparentNumberPollIndex.getPropertyValue());
                }
                currentIndex = (currentIndex + 1) % total;
                ApparentNumberDTO number = numberList.get(currentIndex);
                logger.info("客户:" + custId + ",渠道ID:" + resourceId + ",轮询外显号:" + number.getApparentNumber());
                // 保存企业外显号轮询index
                if (apparentNumberPollIndex != null) {
                    apparentNumberPollIndex.setPropertyValue(String.valueOf(currentIndex));
                } else {
                    apparentNumberPollIndex = new CustomerProperty(custId, resourceId + "_apparent_number_poll_index", String.valueOf(currentIndex));
                }
                customerDao.saveOrUpdate(apparentNumberPollIndex);
                return number.getApparentNumber();
            } else if ("random".equals(apparentNumberRule.getPropertyValue())) {  // 随机模式
                // 查询企业有效外显号列表
                ApparentNumberQueryParam m = new ApparentNumberQueryParam();
                m.setCustId(custId);
                m.setStatus(1);
                List<ApparentNumberDTO> numberList = listApparentNumber(m);
                if (numberList.size() == 0) {
                    logger.warn("客户:" + custId + ",渠道ID:" + resourceId + ",未添加外显号");
                }
                Random r = new Random();
                int num = r.nextInt(numberList.size());
                ApparentNumberDTO number = numberList.get(num);
                logger.info("客户:" + custId + ",渠道ID:" + resourceId + ",随机外显号:" + number.getApparentNumber());
                return number.getApparentNumber();
            } else {
                logger.warn("客户:" + custId + ",渠道ID:" + resourceId + ",未找到" + apparentNumberRule.getPropertyValue() + "外显规则");
            }
        } else {
            logger.warn("客户:" + custId + ",渠道ID:" + resourceId + "未配置外显号使用规则");
            return null;
        }
        return null;
    }

    /**
     * 获取客户配置的外显号
     * 规则分为 random-随机  poll-轮询  specify-指定 none-不用我方规则
     *
     * @param custId         客户ID
     * @param callCenterType 呼叫类型
     * @return
     */
    public String getCustomerApparentNumber(String custId, String callCenterType) {
        CustomerProperty apparentNumberRule = customerDao.getProperty(custId, "apparent_number_rule");
        if (apparentNumberRule != null) {
            logger.info("客户:" + custId + ",外显号规则:" + apparentNumberRule.getPropertyValue());
            if ("none".equals(apparentNumberRule.getPropertyValue())) {
                logger.warn("客户:" + custId + ",外显号不用我方规则");
                return null;
            } else if ("specify".equals(apparentNumberRule.getPropertyValue())) {
                CustomerProperty apparentNumber = customerDao.getProperty(custId, "apparent_number");
                if (apparentNumber == null) {
                    logger.warn("客户:" + custId + ",未指定外显号");
                    return null;
                }
                return apparentNumber.getPropertyValue();
                // 轮询模式
            } else if ("poll".equals(apparentNumberRule.getPropertyValue())) {
                // 查询企业有效外显号列表
                ApparentNumberQueryParam m = new ApparentNumberQueryParam();
                m.setCustId(custId);
                m.setStatus(1);
                List<ApparentNumberDTO> numberList = listApparentNumber(m);
                if (numberList.size() == 0) {
                    logger.warn("客户:" + custId + ",未添加外显号");
                }
                int total = numberList.size();
                int currentIndex = total - 1;
                // 查询上次外显号轮询index
                CustomerProperty apparentNumberPollIndex = customerDao.getProperty(custId, "apparent_number_poll_index");
                if (apparentNumberPollIndex != null) {
                    currentIndex = NumberConvertUtil.parseInt(apparentNumberPollIndex.getPropertyValue());
                }
                currentIndex = (currentIndex + 1) % total;
                ApparentNumberDTO number = numberList.get(currentIndex);
                logger.info("客户:" + custId + ",轮询外显号:" + number.getApparentNumber());
                // 保存企业外显号轮询index
                if (apparentNumberPollIndex != null) {
                    apparentNumberPollIndex.setPropertyValue(String.valueOf(currentIndex));
                } else {
                    apparentNumberPollIndex = new CustomerProperty(custId, "apparent_number_poll_index", String.valueOf(currentIndex));
                }
                customerDao.saveOrUpdate(apparentNumberPollIndex);
                return number.getApparentNumber();
            } else if ("random".equals(apparentNumberRule.getPropertyValue())) {  // 随机模式
                // 查询企业有效外显号列表
                ApparentNumberQueryParam m = new ApparentNumberQueryParam();
                m.setCustId(custId);
                m.setStatus(1);
                List<ApparentNumberDTO> numberList = listApparentNumber(m);
                if (numberList.size() == 0) {
                    logger.warn("客户:" + custId + ",未添加外显号");
                }
                Random r = new Random();
                int num = r.nextInt(numberList.size());
                ApparentNumberDTO number = numberList.get(num);
                logger.info("客户:" + custId + ",随机外显号:" + number.getApparentNumber());
                return number.getApparentNumber();
            } else {
                logger.warn("客户:" + custId + ",未找到" + apparentNumberRule.getPropertyValue() + "外显规则");
            }
        } else {
            logger.warn("客户:" + custId + ",未配置外显号使用规则");
            return null;
        }
        return null;
    }

    public int saveUpdateCustomerCallCenterType(XFCallCenterConfig xfCallCenterConfig) {
        int code = 0;
        String customerId = xfCallCenterConfig.getCustId();
        try {
            CustomerProperty callCenterType = customerDao.getProperty(customerId, "call_center_type");
            if (callCenterType != null) {
                callCenterType.setPropertyValue(xfCallCenterConfig.getCallCenterType());
            } else {
                callCenterType = new CustomerProperty(customerId, "call_center_type", xfCallCenterConfig.getCallCenterType());
            }
            customerDao.saveOrUpdate(callCenterType);

            CustomerProperty callCenterId = customerDao.getProperty(customerId, "xf_call_center_id");
            if (callCenterId != null) {
                callCenterId.setPropertyValue(xfCallCenterConfig.getXfCallCenterId());
            } else {
                callCenterId = new CustomerProperty(customerId, "xf_call_center_id", xfCallCenterConfig.getXfCallCenterId());
            }
            customerDao.saveOrUpdate(callCenterId);

            CustomerProperty callCenterIp = customerDao.getProperty(customerId, "xf_call_center_ip");
            if (callCenterIp != null) {
                callCenterIp.setPropertyValue(xfCallCenterConfig.getXfCallCenterIp());
            } else {
                callCenterIp = new CustomerProperty(customerId, "xf_call_center_ip", xfCallCenterConfig.getXfCallCenterIp());
            }
            customerDao.saveOrUpdate(callCenterIp);

            CustomerProperty xfCallSipPort = customerDao.getProperty(customerId, "xf_call_sip_port");
            if (xfCallSipPort != null) {
                xfCallSipPort.setPropertyValue(xfCallCenterConfig.getXfCallSipPort());
            } else {
                xfCallSipPort = new CustomerProperty(customerId, "xf_call_sip_port", xfCallCenterConfig.getXfCallSipPort());
            }
            customerDao.saveOrUpdate(xfCallSipPort);

            CustomerProperty xfCallSipPwd = customerDao.getProperty(customerId, "xf_call_sip_pwd");
            if (xfCallSipPwd != null) {
                xfCallSipPwd.setPropertyValue(xfCallCenterConfig.getXfCallSipPwd());
            } else {
                xfCallSipPwd = new CustomerProperty(customerId, "xf_call_sip_pwd", xfCallCenterConfig.getXfCallSipPwd());
            }
            customerDao.saveOrUpdate(xfCallSipPwd);

            CustomerProperty xfCallCenterPwd = customerDao.getProperty(customerId, "xf_call_center_pwd");
            if (xfCallCenterPwd != null) {
                xfCallCenterPwd.setPropertyValue(xfCallCenterConfig.getXfCallCenterPwd());
            } else {
                xfCallCenterPwd = new CustomerProperty(customerId, "xf_call_center_pwd", xfCallCenterConfig.getXfCallCenterPwd());
            }
            customerDao.saveOrUpdate(xfCallCenterPwd);

            CustomerProperty xfCallCenterRecordIp = customerDao.getProperty(customerId, "xf_call_center_record_ip");
            if (xfCallCenterRecordIp != null) {
                xfCallCenterRecordIp.setPropertyValue(xfCallCenterConfig.getXfCallCenterRecordIp());
            } else {
                xfCallCenterRecordIp = new CustomerProperty(customerId, "xf_call_center_record_ip", xfCallCenterConfig.getXfCallCenterRecordIp());
            }
            customerDao.saveOrUpdate(xfCallCenterRecordIp);

            CustomerProperty xfCallCenterRecordPort = customerDao.getProperty(customerId, "xf_call_center_record_port");
            if (xfCallCenterRecordPort != null) {
                xfCallCenterRecordPort.setPropertyValue(xfCallCenterConfig.getXfCallCenterRecordPort());
            } else {
                xfCallCenterRecordPort = new CustomerProperty(customerId, "xf_call_center_record_port", xfCallCenterConfig.getXfCallCenterRecordPort());
            }
            customerDao.saveOrUpdate(xfCallCenterRecordPort);

            CustomerProperty xfCallCenterVoicePort = customerDao.getProperty(customerId, "xf_call_center_voice_port");
            if (xfCallCenterVoicePort != null) {
                xfCallCenterVoicePort.setPropertyValue(xfCallCenterConfig.getXfCallCenterVoicePort());
            } else {
                xfCallCenterVoicePort = new CustomerProperty(customerId, "xf_call_center_voice_port", xfCallCenterConfig.getXfCallCenterVoicePort());
            }
            customerDao.saveOrUpdate(xfCallCenterVoicePort);

            CustomerProperty xfCallCenterVoiceServer = customerDao.getProperty(customerId, "xf_call_center_voice_server");
            if (xfCallCenterVoiceServer != null) {
                xfCallCenterVoiceServer.setPropertyValue(xfCallCenterConfig.getXfCallCenterVoiceServer());
            } else {
                xfCallCenterVoiceServer = new CustomerProperty(customerId, "xf_call_center_voice_server", xfCallCenterConfig.getXfCallCenterVoiceServer());
            }
            customerDao.saveOrUpdate(xfCallCenterVoiceServer);
            code = 1;
        } catch (Exception e) {
            code = 0;
            logger.error("更新新方呼叫中心配置失败,", e);
        }
        return code;

    }


    public CustomerUserDTO getSecurityCenterInfo(Long userId, String custId) throws Exception {
        CustomerUser cu = this.customerUserDao.get(userId);

        CustomerUserPropertyDO mobileNum = this.customerUserDao.getProperty(String.valueOf(userId), "mobile_num");
        CustomerUserPropertyDO userPwdLevel = this.customerUserDao.getProperty(String.valueOf(userId), "user_pwd_level");
        CustomerUserPropertyDO email = this.customerUserDao.getProperty(String.valueOf(userId), "email");
        CustomerProperty acctPwdLevel = this.customerDao.getProperty(custId, "pwd_status");

        CustomerUserDTO cud = new CustomerUserDTO();
        if (mobileNum != null) cud.setMobileNum(mobileNum.getPropertyValue());
        if (userPwdLevel != null) cud.setUserPwdLevel(userPwdLevel.getPropertyValue());
        if (email != null) cud.setEmail(email.getPropertyValue());
        if (acctPwdLevel != null) cud.setAcctPwdLevel(acctPwdLevel.getPropertyValue());


        return cud;
    }

    public int updateCustCallBackApparentNumber(String customerId, String apparentNumber) {
        CustomerProperty callBackApparentNumber = customerDao.getProperty(customerId, "call_back_apparent_number");
        if (callBackApparentNumber == null) {
            callBackApparentNumber = new CustomerProperty(customerId, "call_back_apparent_number", apparentNumber);
        } else {
            callBackApparentNumber.setPropertyValue(apparentNumber);
        }
        try {
            customerDao.saveOrUpdate(callBackApparentNumber);
            return 1;
        } catch (Exception e) {
            logger.error("修改企业双向呼叫外显号码失败,", e);
            return 0;
        }
    }

    /**
     * 更改客户的结算类型
     *
     * @param custId
     * @param settlementType
     * @return
     * @throws Exception
     */
    public int updateSettlementType(String custId, int settlementType, String creditAmount) throws Exception {
        int code = 0;
        Customer customer = customerDao.get(custId);
        if (customer != null) {
            CustomerProperty customerProperty = customerDao.getProperty(custId, "settlement_type");
            if (customerProperty == null) {
                customerProperty = new CustomerProperty(custId, "settlement_type", String.valueOf(settlementType), new Timestamp(System.currentTimeMillis()));
            } else {
                customerProperty.setPropertyValue(String.valueOf(settlementType));
            }
            // 处理授信额度
            if (2 == settlementType) {
                CustomerProperty creditAmountProperty = customerDao.getProperty(custId, "creditAmount");
                if (creditAmountProperty == null) {
                    creditAmountProperty = new CustomerProperty(custId, "creditAmount", creditAmount, new Timestamp(System.currentTimeMillis()));
                } else {
                    creditAmountProperty.setPropertyValue(String.valueOf(creditAmount));
                }
                customerDao.saveOrUpdate(creditAmountProperty);
            }
            customerDao.saveOrUpdate(customerProperty);
            code = 1;
        }
        return code;
    }

    public int savePriceSetting(CustomerPriceConfigDTO param) throws Exception {
        int code = 0;
        String custId = param.getCustId();
        Customer customer = customerDao.get(custId);
        if (customer != null) {
            // 保存配置的服务资源()开通服务/品类：数据 呼叫线路 短信
            CustomerProperty serviceResource = customerDao.getProperty(custId, "service_resource");
            if (serviceResource == null) {
                serviceResource = new CustomerProperty(custId, "service_resource", param.getServiceResource(), new Timestamp(System.currentTimeMillis()));
            } else {
                serviceResource.setPropertyValue(param.getServiceResource());
            }
            customerDao.saveOrUpdate(serviceResource);

            // 数据渠道配置信息
            CustomerProperty dataConfig = customerDao.getProperty(custId, "data_config");
            if (dataConfig == null) {
                dataConfig = new CustomerProperty(custId, "data_config", param.getDataConfig(), new Timestamp(System.currentTimeMillis()));
            } else {
                dataConfig.setPropertyValue(param.getDataConfig());
            }
            customerDao.saveOrUpdate(dataConfig);

            // 通话渠道配置信息
            //Object object = JSON.parse(param.getCallConfig());
            JSONObject jsonObject;
            // 兼容之前的数据格式
            JSONArray callConfigs = JsonUtil.convertJsonArray(param.getCallConfig());
           /* if (object instanceof JSONObject) {
                jsonObject = (JSONObject) object;
                callConfigs = new JSONArray();
                if (jsonObject != null && jsonObject.size() > 0) {
                    callConfigs.add(jsonObject);
                }
            } else if (object instanceof JSONArray) {
                callConfigs = (JSONArray) object;
            }*/

            if (callConfigs == null) {
                callConfigs = new JSONArray();
            }
            // 处理配置的新渠道为有效状态
            Set<String> insertResIds = new HashSet<>();
            if (callConfigs.size() > 0) {
                for (int i = 0; i < callConfigs.size(); i++) {
                    callConfigs.getJSONObject(i).put("status", 1);
                    insertResIds.add(callConfigs.getJSONObject(i).getString("resourceId"));
                }
            }
            CustomerProperty callConfig = customerDao.getProperty(custId, "call_config");
            if (callConfig == null) {
                callConfig = new CustomerProperty(custId, "call_config", callConfigs.toJSONString(), new Timestamp(System.currentTimeMillis()));
            } else {
                // 获取数据库中之前配置的渠道列表
                //Object dbObject = JSON.parse(callConfig.getPropertyValue());
                JSONObject dbJsonObject;
                // 兼容之前的数据格式
                JSONArray dbCallConfigs = JsonUtil.convertJsonArray(callConfig.getPropertyValue());

                /*if (dbObject instanceof JSONObject) {
                    dbJsonObject = (JSONObject) dbObject;
                    dbCallConfigs = new JSONArray();
                    dbCallConfigs.add(dbJsonObject);
                } else if (dbObject instanceof JSONArray) {
                    dbCallConfigs = (JSONArray) dbObject;
                }*/
                if (dbCallConfigs != null) {
                    // 数据库中原来的数据为无效
                    JSONArray jsonArray = new JSONArray();
                    for (int j = 0; j < dbCallConfigs.size(); j++) {
                        // 如果数据库中存在和新添加的资源ID相同,则只保存新添加的
                        if (insertResIds.contains(dbCallConfigs.getJSONObject(j).getString("resourceId"))) {
                            continue;
                        }
                        dbCallConfigs.getJSONObject(j).put("status", 2);
                        jsonArray.add(dbCallConfigs.getJSONObject(j));
                    }
                    callConfigs.addAll(jsonArray);
                }
                // 处理提交了resourceId字段但是值为空的情况
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < callConfigs.size(); i++) {
                    if (callConfigs.getJSONObject(i).containsKey("resourceId")
                            && StringUtil.isEmpty(callConfigs.getJSONObject(i).getString("resourceId"))) {
                        continue;
                    }
                    jsonArray.add(callConfigs.getJSONObject(i));
                }
                callConfig.setPropertyValue(jsonArray.toJSONString());
            }
            customerDao.saveOrUpdate(callConfig);

            // 短信渠道配置信息
            CustomerProperty smsConfig = customerDao.getProperty(custId, "sms_config");
            if (smsConfig == null) {
                smsConfig = new CustomerProperty(custId, "sms_config", param.getSmsConfig(), new Timestamp(System.currentTimeMillis()));
            } else {
                smsConfig.setPropertyValue(param.getSmsConfig());
            }
            customerDao.saveOrUpdate(smsConfig);

            code = 1;
        } else {
            throw new RuntimeException("未查询到该客户:" + param.getCustId());
        }
        return code;
    }

    public CustomerPriceConfigDTO selectCustomerPriceDetail(String custId) throws Exception {
        if (StringUtil.isEmpty(custId)) {
            throw new RuntimeException("custId不能为空");
        }
        Customer customer = customerDao.get(custId);
        CustomerPriceConfigDTO result = null;
        if (customer != null) {
            result = new CustomerPriceConfigDTO();
            result.setCustId(customer.getCustId());
            result.setCustName(customer.getEnterpriseName());
            CustomerProperty serviceResourceProperty = customerDao.getProperty(custId, "service_resource");
            if (serviceResourceProperty != null) {
                result.setServiceResource(serviceResourceProperty.getPropertyValue());
            }

            // 查询通话营销资源
            CustomerProperty voiceCustomerProperty = customerDao.getProperty(custId, MarketResourceTypeEnum.CALL.getPropertyName());
            if (voiceCustomerProperty != null) {
                result.setCallConfig(voiceCustomerProperty.getPropertyValue());
            }
            CustomerProperty dataCustomerProperty = customerDao.getProperty(custId, MarketResourceTypeEnum.LABEL.getPropertyName());
            if (dataCustomerProperty != null) {
                result.setDataConfig(dataCustomerProperty.getPropertyValue());
            }
            CustomerProperty smsCustomerProperty = customerDao.getProperty(custId, MarketResourceTypeEnum.SMS.getPropertyName());
            if (smsCustomerProperty != null) {
                result.setSmsConfig(smsCustomerProperty.getPropertyValue());
            }
        }
        return result;
    }

    /**
     * 查询客户售价配置和选择的供应商列表
     *
     * @param custId
     * @return
     * @throws Exception
     */
    public Map<String, Object> selectCustomerPriceAndSupplierList(String custId, String callType) throws Exception {
        Map<String, Object> result = new HashMap<>();
        CustomerPriceConfigDTO customerPriceConfigDTO = selectCustomerPriceDetail(custId);
        Set<Integer> resourceIds = new HashSet<>();
        if (customerPriceConfigDTO != null) {
            // 处理通话资源
            if (StringUtil.isNotEmpty(customerPriceConfigDTO.getCallConfig())) {
                //Object object = JSON.parse(customerPriceConfigDTO.getCallConfig());
                MarketResourceEntity marketResource = null;
                SupplierEntity supplierDO = null;
                JSONObject callConfig;
                // 兼容之前的数据格式
                JSONArray callConfigs = JsonUtil.convertJsonArray(customerPriceConfigDTO.getCallConfig());
                JSONArray jsonArray = new JSONArray();
                for (int i = 0; i < callConfigs.size(); i++) {
                    callConfig = callConfigs.getJSONObject(i);
                    // 不返回无效的数据
                    if (callConfig != null && callConfig.get("status") != null && callConfig.getIntValue("status") == 2) {
                        continue;
                    }
                    // 处理资源名称
                    if (callConfig.get("resourceId") != null) {
                        marketResource = marketResourceDao.get(callConfig.getInteger("resourceId"));
                    }
                    callConfig.put("resourceName", marketResource != null ? marketResource.getResname() : "");
                    resourceIds.add(callConfig.getInteger("resourceId"));
                    // 处理供应商名称
                    if (callConfig.get("supplierId") != null) {
                        supplierDO = supplierDao.get(callConfig.getInteger("supplierId"));
                    }
                    callConfig.put("supplierName", supplierDO != null ? supplierDO.getName() : "");
                    jsonArray.add(callConfig);
                }
                customerPriceConfigDTO.setCallConfig(jsonArray.toJSONString());
            }
            // 处理短信资源
            if (StringUtil.isNotEmpty(customerPriceConfigDTO.getSmsConfig())) {
                JSONArray jsonArray = JSON.parseArray(customerPriceConfigDTO.getSmsConfig());
                MarketResourceEntity marketResource = null;
                SupplierEntity supplierDO = null;
                JSONObject jsonObject = null;
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    resourceIds.add(jsonArray.getJSONObject(i).getInteger("resourceId"));
                    // 处理资源名称
                    if (jsonObject.get("resourceId") != null) {
                        marketResource = marketResourceDao.get(jsonObject.getInteger("resourceId"));
                    }
                    jsonObject.put("resourceName", marketResource != null ? marketResource.getResname() : "");
                    // 处理供应商名称
                    if (jsonObject.get("supplierId") != null) {
                        supplierDO = supplierDao.get(jsonObject.getInteger("supplierId"));
                    }
                    jsonObject.put("supplierName", supplierDO != null ? supplierDO.getName() : "");
                }
                customerPriceConfigDTO.setSmsConfig(jsonArray.toJSONString());
            }
            // 处理数据资源
            if (StringUtil.isNotEmpty(customerPriceConfigDTO.getDataConfig())) {
                JSONArray jsonArray = JSON.parseArray(customerPriceConfigDTO.getDataConfig());
                MarketResourceEntity marketResource = null;
                SupplierEntity supplierDO = null;
                JSONObject jsonObject = null;
                for (int i = 0; i < jsonArray.size(); i++) {
                    jsonObject = jsonArray.getJSONObject(i);
                    resourceIds.add(jsonArray.getJSONObject(i).getInteger("resourceId"));
                    // 处理资源名称
                    if (jsonObject.get("resourceId") != null) {
                        marketResource = marketResourceDao.get(jsonObject.getInteger("resourceId"));
                    }
                    jsonObject.put("resourceName", marketResource != null ? marketResource.getResname() : "");
                    // 处理供应商名称
                    if (jsonObject.get("supplierId") != null) {
                        supplierDO = supplierDao.get(jsonObject.getInteger("supplierId"));
                    }
                    jsonObject.put("supplierName", supplierDO != null ? supplierDO.getName() : "");
                }
                customerPriceConfigDTO.setDataConfig(jsonArray.toJSONString());
            }
        }

        List<Map<String, Object>> allSupplierList = supplierService.listOnlineAllSupplier();
        if (allSupplierList != null && allSupplierList.size() > 0) {
            Map<String, Object> map, resource;
            List<MarketResourceDTO> marketResourceList;
            List<Map<String, Object>> supplierList, resourceList = null;
            ResourcePropertyEntity marketResourceProperty;
            JSONObject callConfig;
            for (int i = 0; i < allSupplierList.size(); i++) {
                map = allSupplierList.get(i);
                if (map != null && map.get("supplierList") != null) {
                    supplierList = (List<Map<String, Object>>) map.get("supplierList");
                    for (int j = 0; j < supplierList.size(); j++) {
                        if (supplierList.get(j).get("resourceList") != null) {
                            resourceList = new ArrayList<>();
                            marketResourceList = (List<MarketResourceDTO>) supplierList.get(j).get("resourceList");
                            for (MarketResourceDTO dto : marketResourceList) {
                                // 支持过滤呼叫类型
                                if (StringUtil.isNotEmpty(callType)) {
                                    if (map.get("type") != null && String.valueOf(MarketResourceTypeEnum.CALL.getType()).equals(String.valueOf(map.get("type")))) {
                                        marketResourceProperty = marketResourceDao.getProperty(String.valueOf(dto.getResourceId()), "price_config");
                                        if (marketResourceProperty != null && StringUtil.isNotEmpty(marketResourceProperty.getPropertyValue())) {
                                            callConfig = JSON.parseObject(marketResourceProperty.getPropertyValue());
                                            if (!callType.equals(callConfig.getString("type"))) {
                                                continue;
                                            }
                                        }
                                    }

                                }

                                resource = new HashMap<>();
                                resource.put("resourceId", dto.getResourceId());
                                resource.put("resname", dto.getResname());
                                resource.put("resourceProperty", dto.getResourceProperty());
                                if (resourceIds.contains(dto.getResourceId())) {
                                    resource.put("checked", true);
                                } else {
                                    resource.put("checked", false);
                                }
                                resourceList.add(resource);
                            }
                            supplierList.get(j).put("resourceList", resourceList);
                        }
                        // 支持过滤呼叫类型
                        if (StringUtil.isNotEmpty(callType)) {
                            if (resourceList != null && resourceList.size() == 0) {
                                if (map.get("type") != null && String.valueOf(MarketResourceTypeEnum.CALL.getType()).equals(String.valueOf(map.get("type")))) {
                                    supplierList.remove(j);
                                    j -= 1;
                                }
                            }
                        }
                    }
                }
            }
        }
        result.put("config", customerPriceConfigDTO);
        result.put("supplierList", allSupplierList);
        return result;
    }


    /**
     * 客户月账单列表
     *
     * @param yearMonth
     * @param pageIndex
     * @param pageSize
     * @param custName
     * @param custId
     * @return
     */
    public Map<String, Object> listCustomerMonthBill(String yearMonth, int pageIndex, int pageSize, String custName, String custId) {
        Map<String, Object> result = new HashMap<>();
        double sumAmount = 0.0;
        StringBuffer hql = new StringBuffer("from Customer m where m.status=0");
        List values = new ArrayList();
        if (StringUtil.isNotEmpty(custName)) {
            hql.append(" and m.enterpriseName like ?");
            values.add("%" + custName + "%");
        }
        if (StringUtil.isNotEmpty(custId)) {
            hql.append(" and m.custId = ?");
            values.add(custId);
        }
        hql.append(" order by m.createTime DESC ");
        Page page = customerDao.page(hql.toString(), values, pageIndex, pageSize);
        List<Map<String, Object>> list = new ArrayList<>();
        if (page != null && page.getData() != null && page.getData().size() > 0) {
            Customer customer;
            Map<String, Object> data;
            double amount = 0.0;
            String settlementType = "";
            CustomerProperty settlementCustomerProperty;
            List<CustomerUser> us;
            for (int i = 0; i < page.getData().size(); i++) {
                data = new HashMap<>();
                customer = (Customer) page.getData().get(i);
                data.put("id", customer.getCustId());
                data.put("name", customer.getEnterpriseName());
                settlementCustomerProperty = customerDao.getProperty(customer.getCustId(), "settlement_type");
                if (settlementCustomerProperty != null) {
                    settlementType = settlementCustomerProperty.getPropertyValue();
                }
                data.put("settlementType", settlementType);
                data.put("time", yearMonth);
                amount = billDao.sumCustomerMonthAmount(customer.getCustId(), yearMonth);
                data.put("amount", amount);
                sumAmount += amount;

                us = customerUserDao.find("from CustomerUser m where m.cust_id='" + customer.getCustId() + "' and m.userType=1");
                if (us.size() > 0) {
                    data.put("userId", us.get(0).getId());
                    data.put("userName", us.get(0).getAccount());
                    data.put("status", us.get(0).getStatus());
                }
                list.add(data);
            }
        }
        result.put("sumAmount", NumberConvertUtil.parseDecimalDouble(sumAmount, 2));
        result.put("list", list);
        result.put("total", page.getTotal());
        return result;
    }

    public Map<String, Object> listCustomerBill(String custId, String startTime, String endTime, String orderNo, int type, int pageNum, int pageSize, String resource_id) {
        Map<String, Object> result = new HashMap<>();
        Page page = billDao.pageCustomerBill(custId, startTime, endTime, orderNo, type, pageNum, pageSize, resource_id);
        double sumAmount = 0.0;
        BigDecimal bigDecimal, decimal;
        long callDurationTime = 1;
        if (page != null && page.getData() != null && page.getData().size() > 0) {
            Map<String, Object> map;
            MarketResourceEntity marketResource = null;
            List<IndustryPool> industryPool;
            int resourceId = 0;

            Customer customer;
            Map<String, Object> data;

            String settlementType = "";
            CustomerProperty settlementCustomerProperty;
            List<Map<String, Object>> list = new ArrayList<>();

            for (int i = 0; i < page.getData().size(); i++) {
                map = (Map<String, Object>) page.getData().get(i);
                if (map != null && map.get("resourceId") != null) {
                    resourceId = NumberConvertUtil.parseInt(map.get("resourceId"));
                    map.put("count", 1);
                    map.put("price", map.get("amount"));
                } else if (map != null && map.get("industry_pool_id") != null) {
                    // 数据提取费用需要单独处理数量和价格
                    industryPool = industryPoolDao.createQuery(" FROM IndustryPool m WHERE m.industryPoolId = ?", NumberConvertUtil.parseInt(String.valueOf(map.get("industry_pool_id")))).list();
                    if (industryPool.size() > 0) {
                        resourceId = industryPool.get(0).getSourceId();
                    }
                    map.put("count", map.get("userCount"));
                    map.put("remark", map.get("id"));
                    map.put("price", NumberConvertUtil.parseDecimalDouble(NumberConvertUtil.parseDouble(String.valueOf(map.get("price"))), 3));
                } else {
                    map.put("count", 1);
                    map.put("price", map.get("amount"));
                }

                if (map.get("userId") != null) {
                    map.put("remark", customerUserDao.getLoginName(String.valueOf(map.get("userId"))));
                }

                if (TransactionTypeEnum.CALL_DEDUCTION.getType() == type) {
                    if (StringUtil.isNotEmpty(String.valueOf(map.get("calledDuration"))) && !"null".equals(String.valueOf(map.get("calledDuration")))) {
                        map.put("count", DateUtil.secondToTime(NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration")))));
                        bigDecimal = new BigDecimal((double) NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration"))) / 60);
                        callDurationTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();
                    } else {
                        map.put("count", 0);
                        map.put("price", 0);
                        callDurationTime = 0;
                    }

                    if (callDurationTime > 0 && map.get("amount") != null) {
                        decimal = new BigDecimal(String.valueOf(map.get("amount"))).divide(new BigDecimal(callDurationTime));
                        map.put("price", NumberConvertUtil.parseDecimalDouble(decimal.doubleValue(), 3));
                    }
                    if (map.get("amount") == null) {
                        map.put("amount", 0);
                    }
                }

                if (resourceId > 0) {
                    marketResource = marketResourceDao.get(resourceId);
                }
                map.put("resourceName", marketResource != null ? marketResource.getResname() : "");
            }
        }
        result.put("total", page.getTotal());
        result.put("list", page.getData());
        // 按照类型查询客户消费总金额
        Map<String, Object> amountData = billDao.statCustomerBillAmount(custId, startTime, endTime, orderNo, type, pageNum, pageSize, resource_id);
        if (amountData.get("sumAmount") != null) {
            sumAmount = NumberConvertUtil.parseDouble(String.valueOf(amountData.get("sumAmount")));
        }
        result.put("sumAmount", sumAmount);
        return result;
    }

    /**
     * 获取客户下使用的供应商列表
     *
     * @param custId
     * @param type
     * @return
     */
    public List listUseSupplier(String custId, int type) {
        String propertyName = MarketResourceTypeEnum.getType(type).getPropertyName();
        List<Map<String, Object>> checkedSupplierList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();
        CustomerProperty cp = customerDao.getProperty(custId, propertyName);
        if (cp != null && cp.getPropertyValue() != null) {
            //if (type == MarketResourceTypeEnum.CALL.getType()) {
            Object object = JSON.parse(cp.getPropertyValue());
            if (object instanceof JSONObject) {
                JSONObject jsonObject = JSON.parseObject(cp.getPropertyValue());
                jsonArray.add(jsonObject);
            }
            if (object instanceof JSONArray) {
                jsonArray = JSON.parseArray(cp.getPropertyValue());
            }
            //} else {
            //jsonArray = JSON.parseArray(cp.getPropertyValue());
            //}
        }
        if (jsonArray.size() > 0) {
            JSONObject jsonObject;
            Map<String, Object> map;
            String supplierName;
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                map = new HashMap<>();
                map.put("supplierId", jsonObject.getLongValue("supplierId"));
                supplierName = supplierDao.getSupplierName(jsonObject.getInteger("supplierId"));
                map.put("supplierName", supplierName != null ? supplierName : "");
                checkedSupplierList.add(map);
            }
        }
        return checkedSupplierList;
    }

    /**
     * 查询客户下某个供应商使用的资源列表
     *
     * @param custId
     * @param supplierId
     * @param type
     * @return
     */
    public List listUseSupplierResource(String custId, String supplierId, int type) {
        String propertyName = MarketResourceTypeEnum.getType(type).getPropertyName();
        List<Map<String, Object>> checkedResourceList = new ArrayList<>();
        JSONArray jsonArray = new JSONArray();
        CustomerProperty cp = customerDao.getProperty(custId, propertyName);
        if (cp != null && cp.getPropertyValue() != null) {
            //if (type == MarketResourceTypeEnum.CALL.getType()) {
            Object object = JSON.parse(cp.getPropertyValue());
            if (object instanceof JSONObject) {
                JSONObject jsonObject = JSON.parseObject(cp.getPropertyValue());
                jsonArray.add(jsonObject);
            }
            if (object instanceof JSONArray) {
                jsonArray = JSON.parseArray(cp.getPropertyValue());
            }
           /* } else {
                jsonArray = JSON.parseArray(cp.getPropertyValue());
            }*/
        }
        if (jsonArray.size() > 0) {
            JSONObject jsonObject;
            Map<String, Object> map;
            String resourceName;
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (supplierId.equals(jsonObject.getString("supplierId"))) {
                    map = new HashMap<>();
                    map.put("resourceId", jsonObject.getLongValue("resourceId"));
                    resourceName = marketResourceDao.getResourceName(jsonObject.getInteger("resourceId"));
                    map.put("resourceName", resourceName != null ? resourceName : "");
                    checkedResourceList.add(map);
                }
            }
        }
        return checkedResourceList;
    }

    public void exportExcelListBillByType(HttpServletResponse response, String supplierId, String resourceId, int type, String orderNo, String custId, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();

            // 处理时间
            String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DATE_TIME_FORMATTER);
            String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DATE_TIME_FORMATTER);
            if (StringUtil.isNotEmpty(startTime)) {
                startTimeStr = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            if (StringUtil.isNotEmpty(endTime)) {
                endTimeStr = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;

            head = new ArrayList<>();
            head.add("订单号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易类型");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("数量/时长");
            headers.add(head);

            head = new ArrayList<>();
            head.add("单价(元)");
            headers.add(head);

            head = new ArrayList<>();
            head.add("总金额(元)");
            headers.add(head);

            head = new ArrayList<>();
            head.add("备注");
            headers.add(head);

            List<Map<String, Object>> list = billDao.listCustomerBill(custId, startTime, endTime, orderNo, type);
            List<List<String>> data = new ArrayList<>();
            if (list != null && list.size() > 0) {
                Map<String, Object> map;
                MarketResourceEntity marketResource = null;
                List<IndustryPool> industryPool;

                List<String> columnList;
                BigDecimal bigDecimal, decimal;
                long callDurationTime = 1;
                for (int i = 0; i < list.size(); i++) {
                    map = (Map<String, Object>) list.get(i);
                    if (map != null && map.get("resourceId") != null) {
                        map.put("count", 1);
                        map.put("price", map.get("amount"));
                    } else if (map != null && map.get("industry_pool_id") != null) {
                        // 数据提取费用需要单独处理数量和价格
                        industryPool = industryPoolDao.createQuery(" FROM IndustryPool m WHERE m.industryPoolId = ?", NumberConvertUtil.parseInt(String.valueOf(map.get("industry_pool_id")))).list();

                        map.put("count", map.get("userCount"));
                        map.put("price", map.get("amount"));
                        map.put("remark", map.get("id"));
                    } else {
                        map.put("count", 1);
                        map.put("price", map.get("amount"));
                    }

                    if (map.get("userId") != null) {
                        map.put("remark", customerUserDao.getLoginName(String.valueOf(map.get("userId"))));
                    }
                    if (TransactionTypeEnum.CALL_DEDUCTION.getType() == type) {
                        map.put("count", DateUtil.secondToTime(NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration")))));
                        bigDecimal = new BigDecimal((double) NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration"))) / 60);
                        callDurationTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();
                        if (callDurationTime == 0) {
                            map.put("price", 0);
                        } else {
                            if (map.get("amount") != null) {
                                //map.put("price", new BigDecimal(String.valueOf(map.get("amount"))).divide(new BigDecimal(callDurationTime)));
                                decimal = new BigDecimal(String.valueOf(map.get("amount"))).divide(new BigDecimal(callDurationTime));
                                map.put("price", NumberConvertUtil.parseDecimalDouble(decimal.doubleValue(), 2));
                            }
                        }
                    }

                    columnList = new ArrayList<>();
                    //交易ID
                    columnList.add(String.valueOf(map.get("transactionId")));
                    //交易类型
                    columnList.add(TransactionTypeEnum.getName(type));
                    //交易时间
                    columnList.add(LocalDateTime.parse(String.valueOf(map.get("createTime")), DATE_TIME_FORMATTER_SSS).format(DATE_TIME_FORMATTER));
                    //数量/时长
                    columnList.add(String.valueOf(map.get("count")));
                    //单价(元)
                    columnList.add(String.valueOf(map.get("price")));
                    // 总金额(元)
                    columnList.add(String.valueOf(map.get("price")));
                    // 备注
                    columnList.add(String.valueOf(map.get("remark")));
                    data.add(columnList);
                }
            }

            if (data.size() > 0) {
                String fileName = "客户账单-" + LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyy-MM"));
                final String fileType = ".xlsx";
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                Sheet sheet1 = new Sheet(1, 0);
                sheet1.setHead(headers);
                sheet1.setSheetName(TransactionTypeEnum.getName(type));
                writer.write0(data, sheet1);
                writer.finish();
            } else {
                msg.put("msg", "无满足条件的数据");
                msg.put("data", String.valueOf(""));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
        } catch (Exception e) {
            logger.error("下的营销数据异常,", e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                logger.error("导出营销数据异常,", e);
            }
        }
    }


    public void exportExcelMonthBill(HttpServletResponse response, String custId, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();

            // 处理时间
            String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DATE_TIME_FORMATTER);
            String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DATE_TIME_FORMATTER);
            if (StringUtil.isNotEmpty(startTime)) {
                startTimeStr = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            if (StringUtil.isNotEmpty(endTime)) {
                endTimeStr = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;

            head = new ArrayList<>();
            head.add("订单号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易类型");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("数量/时长");
            headers.add(head);

            head = new ArrayList<>();
            head.add("单价(元)");
            headers.add(head);

            head = new ArrayList<>();
            head.add("总金额(元)");
            headers.add(head);

            head = new ArrayList<>();
            head.add("备注");
            headers.add(head);

            List<Map<String, Object>> list;
            List<List<String>> data;
            Sheet sheet;
            Map<String, Object> map;
            MarketResourceEntity marketResource = null;
            List<IndustryPool> industryPool;
            List<String> columnList;

            String fileName = "客户账单-" + LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            final String fileType = ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
            int sheetNum = 1;
            BigDecimal bigDecimal, decimal;
            long callDurationTime = 1;
            for (TransactionTypeEnum s : TransactionTypeEnum.values()) {
                list = billDao.listCustomerBill(custId, startTime, endTime, "", s.getType());
                data = new ArrayList<>();
                if (list != null && list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        map = (Map<String, Object>) list.get(i);
                        if (map != null && map.get("resourceId") != null) {
                            map.put("count", 1);
                            map.put("price", map.get("amount"));

                        } else if (map != null && map.get("industry_pool_id") != null) {
                            // 数据提取费用需要单独处理数量和价格
                            industryPool = industryPoolDao.createQuery(" FROM IndustryPool m WHERE m.industryPoolId = ?", NumberConvertUtil.parseInt(String.valueOf(map.get("industry_pool_id")))).list();
                            map.put("count", map.get("userCount"));
                            map.put("price", map.get("amount"));
                            map.put("remark", map.get("id"));
                        } else {
                            map.put("count", 1);
                            map.put("price", map.get("amount"));
                        }

                        if (map.get("userId") != null) {
                            map.put("remark", customerUserDao.getLoginName(String.valueOf(map.get("userId"))));
                        }

                        if (TransactionTypeEnum.CALL_DEDUCTION.getType() == s.getType()) {
                            map.put("count", DateUtil.secondToTime(NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration")))));
                            bigDecimal = new BigDecimal((double) NumberConvertUtil.parseLong(String.valueOf(map.get("calledDuration"))) / 60);
                            callDurationTime = bigDecimal.setScale(0, RoundingMode.CEILING).intValue();
                            if (callDurationTime == 0) {
                                map.put("price", 0);
                            } else {
                                if (map.get("amount") != null) {
                                    //map.put("price", new BigDecimal(String.valueOf(map.get("amount"))).divide(new BigDecimal(callDurationTime)));
                                    decimal = new BigDecimal(String.valueOf(map.get("amount"))).divide(new BigDecimal(callDurationTime));
                                    map.put("price", NumberConvertUtil.parseDecimalDouble(decimal.doubleValue(), 2));
                                }
                            }
                        }

                        columnList = new ArrayList<>();
                        //交易ID
                        columnList.add(String.valueOf(map.get("transactionId")));
                        //交易类型
                        columnList.add(s.getName());
                        //交易时间
                        columnList.add(LocalDateTime.parse(String.valueOf(map.get("createTime")), DATE_TIME_FORMATTER_SSS).format(DATE_TIME_FORMATTER));
                        //数量/时长
                        columnList.add(String.valueOf(map.get("count")));
                        //单价(元)
                        columnList.add(String.valueOf(map.get("price")));
                        // 总金额(元)
                        columnList.add(String.valueOf(map.get("price")));
                        // 备注
                        columnList.add(String.valueOf(map.get("remark")));
                        data.add(columnList);
                    }
                }

                sheet = new Sheet(sheetNum, 0);
                sheetNum++;
                sheet.setHead(headers);
                sheet.setSheetName(s.getName());
                writer.write0(data, sheet);
            }
            writer.finish();
        } catch (Exception e) {
            logger.error("导出数据异常,", e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                logger.error("导出数据异常,", e);
            }
        }
    }

    public void exportExcelRechargeDeduction(HttpServletResponse response, int type, String orderNo, String custId, String startTime, String endTime) {
        Map<String, String> msg = new HashMap<>();
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();

            // 处理时间
            String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DATE_TIME_FORMATTER);
            String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DATE_TIME_FORMATTER);
            if (StringUtil.isNotEmpty(startTime)) {
                startTimeStr = LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            if (StringUtil.isNotEmpty(endTime)) {
                endTimeStr = LocalDateTime.parse(endTime, DATE_TIME_FORMATTER).format(DATE_TIME_FORMATTER);
            }
            // 处理excel表头
            List<List<String>> headers = new ArrayList<>();
            List<String> head;

            head = new ArrayList<>();
            head.add("流水号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易类型");
            headers.add(head);

            head = new ArrayList<>();
            head.add("交易时间");
            headers.add(head);

            head = new ArrayList<>();
            head.add("金额(元)");
            headers.add(head);

            head = new ArrayList<>();
            head.add("操作人");
            headers.add(head);

            head = new ArrayList<>();
            head.add("备注");
            headers.add(head);

            List<Map<String, Object>> list;
            List<List<String>> data;
            Sheet sheet;
            Map<String, Object> map;
            MarketResourceEntity marketResource = null;
            List<IndustryPool> industryPool;
            List<String> columnList;

            String fileName = "客户充扣-" + LocalDateTime.parse(startTime, DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyy-MM"));
            final String fileType = ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
            int sheetNum = 1;
            int index = 1, end = 2;
            if (type > 0) {
                index = type;
                end = type;
            }
            for (int transType = index; transType <= end; transType++) {
                list = billDao.listCustomerBill(custId, startTime, endTime, orderNo, transType);
                data = new ArrayList<>();
                if (list != null && list.size() > 0) {
                    for (int i = 0; i < list.size(); i++) {
                        map = (Map<String, Object>) list.get(i);

                        columnList = new ArrayList<>();
                        //交易ID
                        columnList.add(String.valueOf(map.get("transactionId")));
                        //交易类型
                        columnList.add(TransactionTypeEnum.getName(transType));
                        //交易时间
                        columnList.add(LocalDateTime.parse(String.valueOf(map.get("createTime")), DATE_TIME_FORMATTER_SSS).format(DATE_TIME_FORMATTER));
                        //金额(元)
                        columnList.add(String.valueOf(map.get("amount")));
                        //操作人
                        columnList.add(customerUserDao.getLoginName(String.valueOf(map.get("userId"))));
                        //备注
                        columnList.add(String.valueOf(map.get("remark")));
                        data.add(columnList);
                    }
                }

                sheet = new Sheet(sheetNum, 0);
                sheetNum++;
                sheet.setHead(headers);
                sheet.setSheetName(TransactionTypeEnum.getName(transType));
                writer.write0(data, sheet);
            }
            writer.finish();
        } catch (Exception e) {
            logger.error("下的营销数据异常,", e);
        } finally {
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                logger.error("导出营销数据异常,", e);
            }
        }
    }

    /**
     * 保存客户的导出权限
     *
     * @param custId
     * @param propertyValue
     * @return
     */
    public int saveCustExportPermission(String custId, String propertyValue) {
        CustomerProperty cp = customerDao.getProperty(custId, "export_success_order");
        if (cp == null) {
            cp = new CustomerProperty(custId, "export_success_order", propertyValue);
        }
        cp.setPropertyValue(propertyValue);
        customerDao.saveOrUpdate(cp);
        return 1;
    }

    public int saveApparentNumber(ApparentNumber param) throws Exception {
        int code = 0;
        ApparentNumber m = customerDao.selectApparentNumber(param.getId());
        boolean firstSave = false;
        if (m != null) {
            if (param.getOperator() != null) {
                m.setOperator(param.getOperator());
            }
            if (param.getApparentNumber() != null) {
                m.setApparentNumber(param.getApparentNumber());
            }
            if (param.getAreaCode() != null) {
                m.setAreaCode(param.getAreaCode());
            }
            if (param.getProvince() != null) {
                m.setProvince(param.getProvince());
            }
            if (param.getStatus() != null) {
                m.setStatus(param.getStatus());
            }
            if (param.getType() != null) {
                m.setType(param.getType());
            }
            if (param.getStopStatus() != null) {
                m.setStopStatus(param.getStopStatus());
                if (2 == param.getStopStatus()) {
                    m.setStopTime(new Timestamp(System.currentTimeMillis()));
                }
            }
            if (param.getCallChannel() != null) {
                m.setCallChannel(param.getCallChannel());
            }
            if (param.getCallType() != null) {
                m.setCallType(param.getCallType());
            }
        } else {
            m = param;
            m.setStatus(1);
            m.setStopStatus(1);
            m.setCreateTime(new Timestamp(System.currentTimeMillis()));
            firstSave = true;
        }
        try {
            customerDao.saveOrUpdate(m);
            code = 1;
            if (firstSave) {
                // 外显扣费
                int deductionStatus = apparentNumberMonthDeduction(m.getCustId(), m.getCallChannel(), "", m.getApparentNumber());
                logger.info("客户ID:" + m.getCustId() + ",渠道ID:" + m.getCallChannel() + ",外显号:" + m.getApparentNumber() + "，扣费状态:" + deductionStatus);
            }
        } catch (Exception e) {
            throw e;
        }
        return code;
    }

    public int updateApparentNumber(ApparentNumber model) {
        ApparentNumber m = customerDao.selectApparentNumber(model.getId());
        if (m == null) {
            logger.warn("外显ID:" + model.getId() + ",不存在");
            return 0;
        }
        int code = 0;
        try {
            customerDao.saveOrUpdate(model);
            code = 1;
        } catch (Exception e) {
            throw e;
        }
        return code;
    }

    public Page pageApparentNumber(ApparentNumberQueryParam model) {
        Page page = customerDao.pageApparentNumber(model);
        if (page.getData() != null && page.getData().size() > 0) {
            ApparentNumber m;
            List<ApparentNumberDTO> list = new ArrayList<>();
            for (int i = 0; i < page.getData().size(); i++) {
                m = (ApparentNumber) page.getData().get(i);
                list.add(new ApparentNumberDTO(m));
            }
            for (ApparentNumberDTO d : list) {
                if (d == null) {
                    continue;
                }
                // 处理渠道名称
                if (d.getCallChannel() != null) {
                    d.setCallChannelName(marketResourceDao.getResourceName(NumberConvertUtil.parseInt(d.getCallChannel())));
                }
                if (d.getType() == null) {
                    continue;
                }
                if (2 == d.getType()) {
                    d.setAreaCode("");
                    continue;
                }
                if (StringUtil.isEmpty(d.getApparentNumber())
                        || StringUtil.isEmpty(d.getAreaCode())) {
                    continue;
                }
                if (d.getApparentNumber().length() < d.getAreaCode().length()) {
                    continue;
                }
                d.setApparentNumber(d.getApparentNumber().substring(d.getAreaCode().length()));
                //根据外显号码查询标记信息
                if (d.getSignInfo() == null) {
                    d.setSignInfo("");
                }
                logger.info("外显号码是：" + d.getApparentNumber());
                String result = SaleApiUtil.getPhoneNumberTagByXz(d.getAreaCode() + d.getApparentNumber());
                JSONObject jsonObject = JSON.parseObject(result);
                if (jsonObject != null && jsonObject.getJSONObject("data") != null && StringUtil.isNotEmpty(jsonObject.getJSONObject("data").getString("tagType"))) {
                    String signInfo = jsonObject.getJSONObject("data").getString("tagType") + ", " + jsonObject.getJSONObject("data").getInteger("tagAmount");
                    d.setSignInfo(signInfo);
                    //更新数据库外显号码标记信息
                    String updateSql = "update t_apparent_number SET sign_info=?  WHERE cust_id = ? AND id = ?";
                    int i = customerDao.executeUpdateSQL(updateSql, signInfo, model.getCustId(), d.getId());
                    logger.info("更新外显号码标注信息数量：" + i + "id是" + d.getId() + "企业id是：" + model.getCustId());
                }
            }
            page.setData(list);
        }
        return page;
    }

    public List<ApparentNumberDTO> listApparentNumber(ApparentNumberQueryParam model) {
        List<ApparentNumber> list = customerDao.listApparentNumber(model);
        List<ApparentNumberDTO> result = new ArrayList<>();
        if (list.size() > 0) {
            for (ApparentNumber m : list) {
                result.add(new ApparentNumberDTO(m));
            }
        }
        return result;
    }

    /**
     * 保存或者更新客户属性
     *
     * @param property
     * @return
     */
    public int saveCustomerProperty(CustomerProperty property) {
        logger.info("开始更新客户属性,custId:" + property.getCustId() + ",propertyName:" + property.getPropertyName() + ",propertyValue:" + property.getPropertyValue());
        CustomerProperty cp = customerDao.getProperty(property.getCustId(), property.getPropertyName());
        logger.info("客户原配置属性:" + cp);
        if (cp == null) {
            cp = new CustomerProperty(property.getCustId(), property.getPropertyName(), property.getPropertyValue());
        }
        cp.setPropertyValue(property.getPropertyValue());
        try {
            customerDao.saveOrUpdate(cp);
            return 1;
        } catch (Exception e) {
            throw e;
        }
    }

    /**
     * 获取客户属性配置
     *
     * @param property
     * @return
     */
    public CustomerPropertyDTO getCustomerProperty(CustomerProperty property) {
        logger.info("开始查询客户属性,custId:" + property.getCustId() + ",propertyName:" + property.getPropertyName());
        CustomerProperty cp = customerDao.getProperty(property.getCustId(), property.getPropertyName());
        if (cp != null) {
            logger.info("客户属性配置:" + cp);
            return new CustomerPropertyDTO(cp);
        }
        return null;
    }

    public CustomerPropertyDTO getCustomerProperty(String custId, String propertyName) {
        logger.info("开始查询客户属性,custId:" + custId + ",propertyName:" + propertyName);
        CustomerProperty cp = customerDao.getProperty(custId, propertyName);
        if (cp != null) {
            logger.info("客户属性配置:" + cp);
            return new CustomerPropertyDTO(cp);
        }
        return null;
    }

    /**
     * 获取客户资源配置信息
     *
     * @param custId
     * @return
     */
    public Map<String, String> getCustomerResourceConfig(String custId) {
        CustomerProperty call_config = this.customerDao.getProperty(custId, "call_config");
        Map<String, String> map = new HashMap<>();
        map.put("supplier", "");
        map.put("resource", "");
        map.put("typeName", "");
        map.put("type", "");
        map.put("centerId", "");
        if (call_config != null && StringUtil.isNotEmpty(call_config.getPropertyValue())) {
            MarketResourceDTO marketResourceDTO = null;
            try {
                // 读取客户下有效的呼叫线路渠道
                JSONObject jsonObject = supplierService.getCustomerCallPriceConfig(custId);
                List<MarketResourceDTO> callResList = new ArrayList<>();

                List<MarketResourceDTO> call2way = (List<MarketResourceDTO>) jsonObject.get("call2way");
                if (call2way != null || call2way.size() >= 0) {
                    callResList.addAll(call2way);
                }
                List<MarketResourceDTO> callCenter = (List<MarketResourceDTO>) jsonObject.get("callCenter");
                if (callCenter != null || callCenter.size() >= 0) {
                    callResList.addAll(callCenter);
                }
                for (MarketResourceDTO m : callResList) {
                    marketResourceDTO = m;
                }
            } catch (Exception e) {
                logger.error("获取客户资源配置信息异常,客户ID:" + custId, e);
            }
            if (marketResourceDTO == null) {
                return map;
            }
            //String configStr = call_config.getPropertyValue();
            //JSONObject jsonObj = JSON.parseObject(configStr);
            String supplierId = marketResourceDTO.getSupplierId();
            String type = marketResourceDTO.getChargingType() + "";

            if (StringUtil.isNotEmpty(supplierId)) {
                SupplierEntity s = supplierDao.getSupplier(NumberConvertUtil.parseInt(supplierId));
                if (supplierDao != null) {
                    map.put("supplier", s.getName());
                }
            }
            map.put("resource", marketResourceDTO.getResname());
            String center_id = "call_center_id";
            if (StringUtil.isNotEmpty(type)) {
                if ("1".equals(type)) {
                    map.put("typeName", "呼叫中心");
                } else {
                    map.put("typeName", "双向呼叫");
                    center_id = "app_id";
                }
                map.put("type", type);
            }
            CustomerProperty centerId = this.customerDao.getProperty(custId, center_id);
            if (centerId != null) {
                map.put("centerId", centerId.getPropertyValue());
            }
        }
        return map;
    }


    /**
     * 根据用户名区间，模糊查询用户
     *
     * @param custId
     * @param start
     * @param end
     * @return
     */
    public String getSeatsByUserNameNoPage(String custId, String start, String end) {
        JSONObject json = new JSONObject();
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT CAST(id AS CHAR) id,cust_id,user_type,account,realname,create_time,STATUS")
                .append(" FROM t_customer_user  WHERE 1=1  AND STATUS <> 3 AND user_type = 2 ")
                .append(" AND  cust_id = '" + custId + "'")
                .append(" and (");
        try {
            Integer fromInt = null;
            Integer toInt = null;
            if (!StringUtil.isEmpty(start) && StringUtil.isEmpty(end)) {
                fromInt = NumberConvertUtil.parseInt(start);
                toInt = fromInt;
            } else if (!StringUtil.isEmpty(end) && StringUtil.isEmpty(start)) {
                toInt = NumberConvertUtil.parseInt(end);
                fromInt = toInt;
            } else {
                fromInt = NumberConvertUtil.parseInt(start);
                toInt = NumberConvertUtil.parseInt(end);
            }

            for (int index = fromInt; index <= toInt; index++) {
                if (index > fromInt) {
                    sql.append(" or ");
                }
                sql.append(" account like '%" + index + "%'  ");
            }
            sql.append(")");
            List<UserCallConfigDTO> userList = new ArrayList<>();
            sql.append(" limit 1000 ");
            List<CustomerUser> users = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(CustomerUser.class));
            if (users != null && users.size() > 0) {
                for (int i = 0; i < users.size(); i++) {
                    CustomerUser u = users.get(i);
                    UserCallConfigDTO dto = new UserCallConfigDTO();
                    BeanUtils.copyProperties(u, dto);
                    dto.setUserName(u.getAccount());
                    dto.setId(u.getId().toString());
                    dto.setCustomerId(u.getCust_id());
                    List<CustomerUserPropertyDO> customerUserProperties = customerUserDao.getPropertiesByUserId(String.valueOf(u.getId()));
                    for (CustomerUserPropertyDO customerUserProperty : customerUserProperties) {
                        if ("email".equals(customerUserProperty.getPropertyName())) {
                            dto.setEmail(customerUserProperty.getPropertyValue());
                        } else if ("mobile_num".equals(customerUserProperty.getPropertyName())) {
                            dto.setMobileNumber(customerUserProperty.getPropertyValue());
                        } else if ("title".equals(customerUserProperty.getPropertyName())) {
                            dto.setTitle(customerUserProperty.getPropertyValue());
                        } else if ("seats_account".equals(customerUserProperty.getPropertyName())) {
                            dto.setSeatsAccount(customerUserProperty.getPropertyValue());
                        } else if ("seats_password".equals(customerUserProperty.getPropertyName())) {
                            dto.setSeatsPassword(customerUserProperty.getPropertyValue());
                        } else if ("work_num".equals(customerUserProperty.getPropertyName())) {
                            dto.setWorkNum(customerUserProperty.getPropertyValue());
                        } else if ("extension_number".equals(customerUserProperty.getPropertyName())) {
                            dto.setExtensionNumber(customerUserProperty.getPropertyValue());
                        } else if ("extension_password".equals(customerUserProperty.getPropertyName())) {
                            dto.setExtensionPassword(customerUserProperty.getPropertyValue());
                        }
                    }
                    userList.add(dto);
                }
//                map.put("users", userList);
            }
            json.put("data", userList);
        } catch (Exception e) {
            e.printStackTrace();
            logger.error("模糊查询操作员出错");
        }
        return json.toJSONString();
    }


    /**
     * 客户和供应商外显扣费
     *
     * @param custId
     * @param resourceId
     * @param userId
     * @param remark
     * @return
     * @throws Exception
     */
    public int apparentNumberMonthDeduction(String custId, String resourceId, String userId, String remark) throws Exception {
        int status = 0;
        logger.info("外显扣费参数,custId:" + custId + ",resourceId:" + resourceId + ",userId:" + userId);
        // 查询客户配置的呼叫资源
        CustomerProperty cp = customerDao.getProperty(custId, MarketResourceTypeEnum.CALL.getPropertyName());
        if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue())) {
            List<CallPriceConfig> list = JsonUtil.convertJsonArray(cp.getPropertyValue(), CallPriceConfig.class);
            CallPriceConfig callPriceConfig = null;
            for (CallPriceConfig c : list) {
                if (c != null && c.getResourceId().equals(resourceId)) {
                    callPriceConfig = c;
                    break;
                }
            }
            if (callPriceConfig == null) {
                throw new TouchException("客户:" + custId + ",userId:" + userId + ",resourceId:" + resourceId + ",通话售价为空");
            }
            // 客户外显月单价
            int custNumberPrice = NumberConvertUtil.changeY2L(callPriceConfig.getApparent_number_price());
            // 供应商外显月单价
            int supplierNumberPrice = getSupplierAppNumberMonthPrice(resourceId);
            status = transactionService.customerSupplierDeduction(custId, TransactionTypeEnum.APPARENT_NUM_DEDUCTION.getType(),
                    custNumberPrice, supplierNumberPrice, resourceId, remark, userId);
        }
        return status;
    }


    /**
     * 查询供应商外显单价
     *
     * @param resourceId
     * @return
     * @throws TouchException
     */
    public int getSupplierAppNumberMonthPrice(String resourceId) throws TouchException {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT t2.property_value FROM t_market_resource t1 JOIN t_market_resource_property t2 ON t1.resource_id = t2.resource_id ");
        sql.append(" WHERE t1.`status` = 1 AND t2.property_name='price_config' AND t1.resource_id=?");
        List<Map<String, Object>> resourceConfig = customerDao.sqlQuery(sql.toString(), resourceId);
        if (resourceConfig.size() > 0) {
            String config = String.valueOf(resourceConfig.get(0).get("property_value"));
            CallPriceConfig callPriceConfig = JSON.parseObject(config, CallPriceConfig.class);
            if (callPriceConfig == null || callPriceConfig.getApparent_number_price() == null) {
                throw new TouchException("资源:" + resourceId + "未配置外显包月售价");
            }
            if (callPriceConfig.getSeat_month_price() == 0) {
                logger.info("资源:" + resourceId + "外显包月售价为0");
                return 0;
            }
            logger.info("资源:" + resourceId + "外显包月售价为:" + callPriceConfig.getApparent_number_price() + "元");
            // 获取坐席包月价格
            int price = NumberConvertUtil.changeY2L(callPriceConfig.getApparent_number_price());
            return price;
        } else {
            throw new TouchException("资源:" + resourceId + "未配置通话售价");
        }
    }


    /**
     * 获取未被分配的项目，和uid已经分配的项目
     *
     * @param custId
     * @param uid
     * @return
     */
    public Map<String, List<Map<String, Object>>> getMarketProject(String custId, String uid) {
        Map<String, List<Map<String, Object>>> result = new HashMap<>();
        result.put("selected", null);
        result.put("unselected", null);
        String allProject = "";//所有开通的项目
        String uidSelected = ""; //uid被分配的
        String allSelected = "";//所有已被分配的
        String sql = "select * from t_customer_property where cust_id=" + custId + " and property_name like 'marketProject_%' and property_value is not null and property_value!=''";
        List<Map<String, Object>> properties = customerDao.queryListBySql(sql);
        if (properties == null || properties.isEmpty()) {
            return result;
        }
        for (Map<String, Object> map : properties) {
            allProject += "," + map.get("property_value");
        }
        allProject = allProject.substring(1);

        sql = "select * from t_customer_user where user_type=3 and cust_id=" + custId;
        List<Map<String, Object>> users = customerDao.queryListBySql(sql);
        if (users != null && users.size() > 0) {
            String ids = "";
            for (Map<String, Object> map : users) {
                ids += "," + map.get("id");
            }
            if (ids.length() > 0) ids = ids.substring(1);
            sql = "select * from t_customer_user_property where user_id in(" + ids + ") and property_name='hasMarketProject'";
            List<Map<String, Object>> userproperties = customerDao.queryListBySql(sql);
            if (userproperties != null && userproperties.size() > 0) {
                for (Map<String, Object> map : userproperties) {
                    String hasMarketProject = (String) map.get("property_value");
                    if (StringUtil.isNotEmpty(hasMarketProject)) {
                        if (hasMarketProject.startsWith(",")) {
                            hasMarketProject = hasMarketProject.substring(1);
                        }
                        if (hasMarketProject.endsWith(",")) {
                            hasMarketProject = hasMarketProject.substring(0, hasMarketProject.length() - 1);
                        }
                    }
                    if (StringUtil.isNotEmpty(uid) && uid.equals(map.get("user_id").toString())) {
                        uidSelected = hasMarketProject;
                    }
                    allSelected += "," + hasMarketProject;
                }
            }
        }
        List unselected = new ArrayList();
        if (StringUtil.isNotEmpty(allSelected)) {
            List allSelectedList = Arrays.asList(allSelected.split(","));
            List allList = Arrays.asList(allProject.split(","));
            for (Object s : allList) {
                if (!allSelectedList.contains(s)) {
                    unselected.add(s);
                }
            }
        } else {
            unselected = Arrays.asList(allProject.split(","));
        }
        logger.info("uidselected: " + uid + "," + uidSelected);
        if (StringUtil.isNotEmpty(uidSelected)) {
            sql = "select name,id from t_market_project where id in(" + uidSelected + ")";
            List<Map<String, Object>> projectList = customerDao.queryListBySql(sql);
            result.put("selected", projectList);
        }
        if (unselected.size() > 0) {
            String ids = "";
            for (Object s : unselected) {
                ids += "," + s;
            }
            ids = ids.substring(1);
            sql = "select name,id from t_market_project where id in(" + ids + ")";
            List<Map<String, Object>> projectList = customerDao.queryListBySql(sql);
            result.put("unselected", projectList);
        }
        return result;
    }

    /**
     * @description 根据企业id查询号码保护状态
     * @method
     * @date: 2019/7/2 11:19
     */
    public Map<String, Object> getPhoneProtectStatus(String custId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        //默认是隐藏
        map.put("cluePhone", 2);
        map.put("customerPhone", 2);
        CustomerProperty cluePhone = customerDao.getProperty(custId, "cluePhone");
        if (cluePhone != null) {
            map.put("cluePhone", cluePhone.getPropertyValue());
        }
        CustomerProperty customerPhone = customerDao.getProperty(custId, "customerPhone");
        if (customerPhone != null) {
            map.put("customerPhone", customerPhone.getPropertyValue());
        }
        return map;
    }
}

