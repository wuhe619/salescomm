package com.bdaim.customer.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.ResourceEnum;
import com.bdaim.batch.dao.BatchDao;
import com.bdaim.batch.entity.BatchListEntity;
import com.bdaim.common.CommonInfoCodeEnum;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.Constant;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.Page;
import com.bdaim.common.util.page.Pagination;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customer.entity.CustomerDO;
import com.bdaim.customer.entity.CustomerProperty;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.price.entity.CommonInfoEntity;
import com.bdaim.price.entity.CommonInfoPropertyEntity;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.resource.dao.SourceDao;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.supplier.service.SupplierService;
import org.apache.log4j.Logger;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.*;
import java.util.regex.Pattern;


@Service("customerService")
@Transactional
public class CustomerService {
    private static Logger logger = Logger.getLogger(CustomerService.class);
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


    public String getEnterpriseName(String custId) {
        try {
            CustomerDO cu = customerDao.get(custId);
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

    public Page getUser(PageParam page, String customerId, String name, String realName, String mobileNum) {
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

        Page list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);

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

    public CustomerUserDO getUserByName(String name) {
        //String hql="from CustomerUserDO m where m.account=? and m.status=0";
        String hql = "from CustomerUserDO m where m.account=?";
        CustomerUserDO m = userDao.findUnique(hql, name);
        return m;
    }

    public String getUserRealName(String userId) {
        return customerUserDao.getName(userId);
    }

    public synchronized void registerOrUpdateCustomer(CustomerRegistDTO vo) throws Exception{
        if (StringUtil.isNotEmpty(vo.getDealType())) {
            //编辑或创建客户
            CustomerUserDO customerUserDO;
            if (vo.getDealType().equals("1")) {
                String customerId = IDHelper.getID().toString();
                if (StringUtil.isNotEmpty(vo.getUserId())) {
                    customerUserDO = customerUserDao.findUniqueBy("id", Long.valueOf(vo.getUserId()));
                    customerUserDO.setRealname(vo.getRealName());
                } else {
                    customerUserDO = new CustomerUserDO();
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

                CustomerDO customer;
                if (StringUtil.isNotEmpty(vo.getCustId())) {
                    //更新 客户信息
                    customer = customerDao.findUniqueBy("custId", vo.getCustId());
                    customer.setRealName(vo.getRealName());
                    //职位/职级
                    customer.setTitle(vo.getTitle());
                    customer.setEnterpriseName(vo.getEnterpriseName());
                } else {
                    //创建客户信息
                    customer = new CustomerDO();
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


            } else if (vo.getDealType().equals("2")) {//冻结以及解冻
                if (StringUtil.isNotEmpty(vo.getCustId())) {
                    CustomerDO customerDO = customerDao.findUniqueBy("custId", vo.getCustId());
                    if (customerDO != null && StringUtil.isNotEmpty(vo.getStatus())) {
                        customerDO.setStatus(Integer.valueOf(vo.getStatus()));
                        customerDao.saveOrUpdate(customerDO);
                    }
                    List<CustomerUserDO> customerUserList = customerUserDao.getAllByCustId(vo.getCustId());
                    if (customerUserList != null && customerUserList.size() > 0) {
                        for (CustomerUserDO customeruser : customerUserList) {
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

    public Page getCustomerInfo(PageParam page, CustomerRegistDTO customerRegistDTO) {

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
                "t1.`status`,\n" +
                "cjc.industry,cjc.salePerson,cjc.contactAddress,\n" +
                "cjc.province,cjc.city,cjc.county,cjc.taxpayerId,\n" +
                "cjc.bli_path AS bliPic,\n" +
                "cjc.bank,cjc.bankAccount,                 \n" +
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
                "\tmax(CASE property_name WHEN 'bli_path'   THEN property_value ELSE '' END ) bli_path,\n" +
                "\tmax(CASE property_name WHEN 'bank'   THEN property_value ELSE '' END ) bank,\n" +
                "\tmax(CASE property_name WHEN 'bank_account'   THEN property_value ELSE '' END ) bankAccount,\n" +
                "\tmax(CASE property_name WHEN 'bank_account_certificate'   THEN property_value ELSE '' END ) bank_account_certificate,\n" +
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
        return new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);

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
        List<Map<String, Object>> allSupplierList = supplierService.listAllSupplier();
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
        if (StringUtil.isEmpty(suppliers.toString())){
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
    public List<Map<String, Object>> getSalePriceLog(PageParam page, String zid,String custId, String name, String startTime, String endTime) throws Exception {
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
        Page pageData = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
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

}

