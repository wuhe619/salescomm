package com.bdaim.rbac.service.impl;

import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.rbac.dao.UserInfoDao;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.util.CipherUtil;
import com.bdaim.util.LogUtil;
import com.bdaim.util.StringUtil;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.transform.Transformers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.Types;
import java.util.*;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
@Service("userInfoService")
@Transactional
public class UserInfoService {
    @Resource
    UserInfoDao userInfoDao;
    @Resource
    JdbcTemplate jdbcTemplate;

    @Resource
    CustomerUserDao customerUserDao;
    @Resource
    CustomerDao customerDao;


    public UserDO getUserByUserName(String userName) {
        return userInfoDao.findUniqueBy("name", userName);
    }


    public UserDO getUserById(Long uid) {
        return userInfoDao.get(uid);
    }


    public Map<String, Object> getUsersByCondition(UserQueryParam param) {
        Map<String, Object> map = new HashMap<>();
        StringBuilder sql = new StringBuilder("\n" +
                "SELECT\n" +
                "\tt1.`NAME` AS userName,\n" +
                "\tt1.id AS userId,\n" +
                "\tt1.mobile_num AS mobileNum,\n" +
                "\tt1.enterprise_name AS enterpriseName,\n" +
                "\tt1.create_time AS createTime,\n" +
                "\tt1.source AS source,\n" +
                "\tt1.`STATUS` AS status\n" +
                "FROM\n" +
                "\tt_user t1 where 1=1 and t1.user_type=1 ");
        if (StringUtil.isNotEmpty(param.getUserName())) {
            sql.append(" and t1.name='").append(StringEscapeUtils.escapeSql(param.getUserName())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getUserId())) {
            sql.append(" and t1.id='").append(StringEscapeUtils.escapeSql(param.getUserId())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            sql.append(" and t1.enterprise_name='").append(StringEscapeUtils.escapeSql(param.getEnterpriseName())).append("'");
        }
        map.put("total", userInfoDao.getSQLQuery(sql.toString()).list().size());
        map.put("customerList", userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list());
        return map;
    }


    public Map<String, Object> getUserByCondition(String typestr, String condition) throws TouchException {
        Map<String, Object> retMap = new HashMap<>();
        UserDO userDO = new UserDO();
        int type = Integer.parseInt(typestr);
        if (type <= 0) {
            throw new TouchException("20003", "type输入错误");
        }
        if (1 == type) {
            userDO = userInfoDao.findUniqueBy("name", condition);
        }
        if (2 == type) {
            userDO = userInfoDao.findUniqueBy("mobileNum", condition);
        }
        if (3 == type) {
            userDO = userInfoDao.findUniqueBy("email", condition);
        }
        if (null == userDO) {
            throw new TouchException("20004", "查询用户失败");
        }
        retMap.put("mobileNum", userDO.getMobileNum());
        retMap.put("email", userDO.getEmail());
        return retMap;
    }


    public void resetPwd(int type, String condition, String password, int pwdLevel) throws Exception {
        UserDO userDO = new UserDO();
        if (1 == type) {
            userDO = userInfoDao.findUniqueBy("mobileNum", condition);
        }
        if (2 == type) {
            userDO = userInfoDao.findUniqueBy("email", condition);
        }
        if (null == userDO) {
            throw new TouchException("20002", "用户查询失败");
        }
        userDO.setPassword(CipherUtil.generatePassword(password));
        userDO.setUserPwdLevel(pwdLevel);
        userInfoDao.save(userDO);
    }


    public void updatePwd(long uid, String oldPwd, String newPwd, int pwdLevel) throws Exception {

        UserDO userDO = new UserDO();
        try {
            userDO = userInfoDao.findUniqueBy("id", uid);
        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }
        if (null == userDO) {
            throw new TouchException("20004", "用户查询失败");
        }
        if (!CipherUtil.generatePassword(oldPwd).equals(userDO.getPassword())) {
            throw new TouchException("20009", "原始密码错误");
        }
        userDO.setPassword(CipherUtil.generatePassword(newPwd));
        userDO.setUserPwdLevel(pwdLevel);
        userInfoDao.saveOrUpdate(userDO);
    }

    public void updateFrontPwd(long uid, String oldPwd, String newPwd, int pwdLevel) throws Exception {

        CustomerUser customerUserDo = new CustomerUser();
        try {
            customerUserDo = customerUserDao.findUniqueBy("id", uid);
        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }
        if (null == customerUserDo) {
            throw new TouchException("20004", "后台用户查询失败");
        }
        if (!CipherUtil.generatePassword(oldPwd).equals(customerUserDo.getPassword())) {
            throw new TouchException("20009", "原始密码错误");
        }
        customerUserDo.setPassword(CipherUtil.generatePassword(newPwd));
        customerUserDao.saveOrUpdate(customerUserDo);
    }


    public List<Map<String, Object>> getSecurityCenterInfo(long id) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT\n" +
                "  t1.user_pwd_level AS userPwdLevel,\n" +
                "  t1.mobile_num AS mobilenNum,\n" +
                "  t1.email,\n" +
                "  t2.pwd_status AS acctPwdLevel\n" +
                "FROM\n" +
                "  t_user t1\n" +
                "  LEFT JOIN t_account t2\n" +
                "    ON t1.cust_id = t2.cust_id\n" +
                "WHERE t1.id=?");
        return userInfoDao.sqlQuery(sql.toString(), id);
    }


    public void verifyIdentifyValueUniqueness(int type, String value) throws Exception {
        List<UserDO> list = new ArrayList<>();
        if (1 == type) {
            list = userInfoDao.findBy("name", value);
        }
        if (2 == type) {
            list = userInfoDao.findBy("mobileNum", value);
        }
        if (3 == type) {
            list = userInfoDao.findBy("email", value);
        }
        if (list.size() >= 1) {
            throw new TouchException("20008", "已经被注册，请重新输入");
        }
    }


    public void updateRegistInfo(String oldValue, String newValue) throws Exception {
        UserDO userDO = userInfoDao.findUniqueBy("mobile_num", oldValue);
        if (null != userDO) {
            userDO.setMobileNum(newValue);
            userInfoDao.saveOrUpdate(userDO);
        } else {
            throw new TouchException("20004", "系统异常，用户查询失败");
        }
    }


    public void updateFrontRegistInfo(String userId, String oldValue, String newValue) throws Exception {
        CustomerUserPropertyDO customerUserProperty = customerUserDao.getProperty(userId, "mobile_num");
        if (null != customerUserProperty) {
            customerUserProperty.setPropertyValue(newValue);
            customerUserDao.saveOrUpdate(customerUserProperty);
        } else {
            throw new TouchException("20004", "系统异常，前台用户查询失败");
        }
    }


    public void changeUserStatus(String userId, String action) throws Exception {
        UserDO userDO = userInfoDao.findUniqueBy("id", Long.valueOf(userId));
        //解冻
        if ("1".equals(action)) {
            userDO.setStatus(0);
        }
        //冻结
        if ("2".equals(action)) {
            userDO.setStatus(1);
        }
        try {
            userInfoDao.save(userDO);
        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }
    }


    public Map<String, Object> queryIndustryPoolByCondition(HttpServletRequest param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String userName = param.getParameter("userName");
        String userId = param.getParameter("userId");
        String enterpriseName = param.getParameter("enterpriseName");
        int pageNum = Integer.valueOf(param.getParameter("pageNum"));
        int pageSize = Integer.valueOf(param.getParameter("pageSize"));

        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT " +
                " t3.account AS userName," +
                " cast(t3.id as char) AS userId," +
                " t3.cust_id AS custId," +
                " t3. STATUS AS status," +
                " IFNULL(industrypoolNum,0) AS industrypoolNum, t5.enterprise_name enterpriseName" +
                " FROM t_customer_user t3" +
                " LEFT JOIN (" +
                " SELECT t1.*, COUNT(*) AS industrypoolNum FROM t_cust_industry t1" +
                " LEFT JOIN t_industry_pool t4 ON t1.industry_pool_id =t4.industry_pool_id" +
                " WHERE  t1.`STATUS` = 1 AND t4.STATUS =3" +
                " GROUP BY t1.cust_id) t2 ON t3.cust_id = t2.cust_id" +
                " JOIN t_customer t5 ON t3.cust_id = t5.cust_id" +
                " WHERE t3.user_type = 1 AND t3.`STATUS` = 0 ");
        if (StringUtil.isNotEmpty(userName)) {
            sql.append(" and t3.account=?");
            params.add(StringEscapeUtils.escapeSql(userName));
        }
        if (StringUtil.isNotEmpty(userId)) {
            sql.append(" and t3.id=?");
            params.add(StringEscapeUtils.escapeSql(userId));
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sql.append(" and t5.enterprise_name=?");
            params.add(StringEscapeUtils.escapeSql(enterpriseName));
        }
        sql.append(" order by t5.create_time desc ");
        Page page = userInfoDao.sqlPageQuery(sql.toString(), pageNum, pageSize, params.toArray());
        map.put("total", page.getTotal());
        List userIndustryPoolList = page.getData();
        Map customerProperty;
        CustomerUserPropertyDO mobileNum;
        for (int i = 0; i < userIndustryPoolList.size(); i++) {
            customerProperty = (Map) userIndustryPoolList.get(i);
            mobileNum = customerUserDao.getProperty(String.valueOf(customerProperty.get("userId")), "mobile_num");
            customerProperty.put("mobileNum", mobileNum == null ? "" : mobileNum.getPropertyValue());
        }
        map.put("userIndustryPoolList", userIndustryPoolList);
        return map;
    }


    public Map<String, Object> listIndustryPoolByCustomerId(HttpServletRequest param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String customerId = param.getParameter("customerId");
        int pageNum = Integer.valueOf(param.getParameter("pageNum"));
        int pageSize = Integer.valueOf(param.getParameter("pageSize"));
        if (StringUtil.isEmpty(customerId)) {
            throw new TouchException("300", "客户id不能为空");
        }
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT\n" +
                "\tt1.NAME AS industryPoolName,\n" +
                "\tt1.industry_pool_id AS industryPoolId,\n" +
                "\tt2.create_time AS createTime,\n" +
                "\tt2.operator AS operator\n" +
                "FROM\n" +
                "\tt_industry_pool t1\n" +
                "LEFT JOIN t_cust_industry t2 ON t1.industry_pool_id = t2.industry_pool_id where 1=1 ");
        sql.append(" and t2.cust_id=? ");
        params.add(StringEscapeUtils.escapeSql(customerId));
        List list = userInfoDao.sqlPageQuery(sql.toString(), pageNum, pageSize, params.toArray()).getData();
        map.put("total", userInfoDao.sqlPageQuery(sql.toString(), pageNum, pageSize, params.toArray()).getTotal());
        map.put("industryPoolList", list);
        return map;
    }


    public Map<String, Object> showCustIndustryPoolStatus(HttpServletRequest param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String customerId = param.getParameter("customerId");
        int pageNum = Integer.valueOf(param.getParameter("pageNum"));
        int pageSize = Integer.valueOf(param.getParameter("pageSize"));

        if (StringUtil.isEmpty(customerId)) {
            throw new TouchException("300", "客户id为空");
        }
        List<Object> params = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT\n" +
                "\tt1.`NAME` as industryPoolName,\n" +
                "\tt1.industry_pool_id as industryPoolId,\n" +
                "\tIFNULL(t7.industryName,'') as industryName,\n" +
                "\t(case when t3.status is NULL THEN 2 ELSE t3.status END) as status\n" +
                "FROM\n" +
                "\tt_industry_pool t1\n" +
                "LEFT JOIN (\n" +
                "\tSELECT\n" +
                "\t\t*\n" +
                "\tFROM\n" +
                "\t\tt_cust_industry t2\n" +
                "\tWHERE 1=1 and ");
        sql.append("t2.cust_id=? ");
        params.add(StringEscapeUtils.escapeSql(customerId));
        sql.append(") t3 ON t1.industry_pool_id = t3.industry_pool_id\n" +
                "LEFT JOIN (\n" +
                "\tSELECT\n" +
                "\t\tt6.industry_pool_id AS industryPoolId,\n" +
                "\t\tGROUP_CONCAT(t6.industy_name) AS industryName\n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tt4.industry_pool_id,\n" +
                "\t\t\t\tt5.industy_name\n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tt_industry_info_rel t4\n" +
                "\t\t\tLEFT JOIN t_industry_info t5 ON t4.industry_info_id = t5.industry_info_id\n" +
                "\t\t) t6\n" +
                "\tGROUP BY\n" +
                "\t\tt6.industry_pool_id\n" +
                ") t7 ON t7.industryPoolId = t1.industry_pool_id where t1.`STATUS`= 3 order by t1.create_time DESC, status ");
        Page page = userInfoDao.sqlPageQuery(sql.toString(), pageNum, pageSize, params);
        map.put("total", page.getTotal());
        map.put("industryPoolStatusList", page.getData());
        return map;
    }


    public UserDO getUserByName(String name) {
        return userInfoDao.findUniqueBy("name", name);
    }


    public UserDO getUserBymobileNum(String mobileNum) {
        return userInfoDao.findUniqueBy("mobileNum", mobileNum);
    }


    public UserDO getUserByEmail(String email) {
        return userInfoDao.findUniqueBy("email", email);
    }


    public Integer getUserByLoginPassWord(String payPassWord, Long userId) {

        Integer code = null;
        String loginPassWord = jdbcTemplate.queryForObject(
                "SELECT PASSWORD FROM t_user t WHERE t.id =?", String.class, userId);

        if (CipherUtil.generatePassword(payPassWord).equals(loginPassWord)) {
            code = 1;
        } else {
            code = 0;
        }

        return code;
    }


    public UserDO getUserByEnterpriseName(String enterpriseName) {
        return userInfoDao.findUniqueBy("enterpriseName", enterpriseName);
    }


    public List<String> getAuthsByUserName(String userName) throws Exception {
        StringBuilder sql = new StringBuilder("\n" +
                "SELECT\n" +
                "\tt1. NAME\n" +
                "FROM\n" +
                "\tt_role t1\n" +
                "LEFT JOIN t_user_role_rel t2 ON t1.id = t2.role\n" +
                "LEFT JOIN t_user t3 ON t2.id = t3.id\n" +
                "WHERE\n" +
                "\tt3.`name` = ?");

        return jdbcTemplate.queryForList(sql.toString(), new Object[]{userName}, new int[]{Types.VARCHAR}, String.class);
    }

    /*public UserDO getUserByUserName(String userName) {
        return userInfoDao.findUniqueBy("name", userName);
    }

    public UserDO getUserById(Long uid) {
        return userInfoDao.get(uid);
    }

    public Map<String, Object> getUsersByCondition(UserQueryParam param) {
        Map<String, Object> map = new HashMap<>();
        StringBuilder sql = new StringBuilder("" +
                "SELECT" +
                "t1.`NAME` AS userName," +
                "CAST(t1.id AS CHAR) AS userId," +
                "t1.mobile_num AS mobileNum," +
                "t1.enterprise_name AS enterpriseName," +
                "t1.create_time AS createTime," +
                "ifnull(t1.source,0) AS source," +
                "t1.`STATUS` AS status" +
                "FROM" +
                "t_user t1 where 1=1 and t1.user_type=1 ");
        if (StringUtil.isNotEmpty(param.getUserName())) {
            sql.append(" and t1.name like '%").append(StringEscapeUtils.escapeSql(param.getUserName())).append("%'");
        }
        if (StringUtil.isNotEmpty(param.getUserId())) {
            sql.append(" and t1.id='").append(StringEscapeUtils.escapeSql(param.getUserId())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            sql.append(" and t1.enterprise_name like '%").append(StringEscapeUtils.escapeSql(param.getEnterpriseName())).append("%'");
        }
        if (StringUtil.isNotEmpty(param.getStatus())) {
            sql.append(" and t1.status = '").append(StringEscapeUtils.escapeSql(param.getStatus())).append("'");
        }
        sql.append(" order by t1.create_time desc ");
        map.put("total", userInfoDao.getSQLQuery(sql.toString()).list().size());
        map.put("customerList", userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list());
        return map;
    }*/

    public Map<String, Object> getUserByCondition(int type, String condition) throws TouchException {
        Map<String, Object> retMap = new HashMap<>();
        UserDO User = new UserDO();
        if (type <= 0) {
            throw new TouchException("20003", "type输入错误");
        }
        if (1 == type) {
            User = userInfoDao.findUniqueBy("name", condition);
        }
        if (2 == type) {
            User = userInfoDao.findUniqueBy("mobileNum", condition);
        }
        if (3 == type) {
            User = userInfoDao.findUniqueBy("email", condition);
        }
        if (null == User) {
            throw new TouchException("20004", "查询用户失败");
        }
        retMap.put("mobileNum", User.getMobileNum());
        retMap.put("email", User.getEmail());
        return retMap;
    }

    /*public void resetPwd(int type, String condition, String password, int pwdLevel) throws Exception {
        UserDO User = new UserDO();
        if (1 == type) {
            User = userInfoDao.findUniqueBy("mobileNum", condition);
        }
        if (2 == type) {
            User = userInfoDao.findUniqueBy("email", condition);
        }
        if (null == User) {
            throw new TouchException("20002", "用户查询失败");
        }
        User.setPassword(CipherUtil.generatePassword(password));
        User.setUserPwdLevel(pwdLevel);
        userInfoDao.save(User);
    }*/

    /*public void updatePwd(long uid, String oldPwd, String newPwd, int pwdLevel) throws Exception {

        UserDO User = new UserDO();
        try {
            User = userInfoDao.findUniqueBy("id", uid);
        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }
        if (null == User) {
            throw new TouchException("20004", "用户查询失败");
        }
        if (!CipherUtil.generatePassword(oldPwd).equals(User.getPassword())) {
            throw new TouchException("20009", "原始密码错误");
        }
        User.setPassword(CipherUtil.generatePassword(newPwd));
        User.setUserPwdLevel(pwdLevel);
        userInfoDao.save(User);
    }*/

    public void updateCustomerUserPwd(long uid, String oldPwd, String newPwd, int pwdLevel) throws Exception {
        CustomerUser customerUser;
        try {
            customerUser = customerUserDao.findUniqueBy("id", uid);
        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }
        if (null == customerUser) {
            throw new TouchException("20004", "用户查询失败");
        }
        if (!CipherUtil.generatePassword(oldPwd).equals(customerUser.getPassword())) {
            throw new TouchException("20009", "原始密码错误");
        }
        customerUser.setPassword(CipherUtil.generatePassword(newPwd));
        customerUserDao.save(customerUser);
    }

    /*public List<Map<String, Object>> getSecurityCenterInfo(long id) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT\n" +
                "  t1.user_pwd_level AS userPwdLevel,\n" +
                "  t1.mobile_num AS mobilenNum,\n" +
                "  t1.email,\n" +
                "  t2.pwd_status AS acctPwdLevel\n" +
                "FROM\n" +
                "  t_user t1\n" +
                "  LEFT JOIN t_account t2\n" +
                "    ON t1.cust_id = t2.cust_id\n" +
                "WHERE t1.id=" + id);
        return userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
    }*/

    /* public void verifyIdentifyValueUniqueness(int type, String value) throws Exception {
         List<UserDO> list = new ArrayList<>();
         if (1 == type) {
             list = userInfoDao.findBy("name", value);
         }
         if (2 == type) {
             list = userInfoDao.findBy("mobileNum", value);
         }
         if (3 == type) {
             list = userInfoDao.findBy("email", value);
         }
         if (list.size() >= 1) {
             throw new TouchException("20008", "已经被注册，请重新输入");
         }
     }
 */
    public void updateRegistInfo(int type, String oldValue, String newValue, String userId) throws Exception {
        CustomerUserPropertyDO cp = null;
        //mobile
        if (1 == type) {
            cp = customerUserDao.getProperty(userId, "mobile_num");
        }
        if (2 == type) {
            cp = customerUserDao.getProperty(userId, "email");
        }
        if (null == cp) {
            throw new TouchException("20004", "系统异常，用户查询失败");
        }
        if (!Objects.equals(cp.getPropertyValue(), oldValue)) {
            LogUtil.warn("改绑手机号或者邮箱失败,userId:" + userId + ",type:" + type + ",提交数据:" + oldValue + ",数据库数据:" + cp.getPropertyValue());
            throw new TouchException("20004", "系统异常,原手机号或邮箱不正确");
        }
        //if (1 == type) {
        cp.setPropertyValue(newValue);
        /*}
        if (2 == type) {
            cp.setEmail(newValue);
        }*/
        customerUserDao.saveOrUpdate(cp);
    }

   /* public void changeUserStatus(String custId, String action) throws Exception {
        Customer customer = customerDao.get(custId);
        CustomerUser customerUser = customerUserDao.findUnique(" FROM CustomerUser m WHERE m.cust_id = ? AND userType = 1", custId);
        //解冻
        if ("1".equals(action)) {
            if (customer != null) {
                customer.setStatus(0);
            }
            if (customerUser != null) {
                customerUser.setStatus(0);
            }
        }
        //冻结
        if ("2".equals(action)) {
            if (customer != null) {
                customer.setStatus(1);
            }
            if (customerUser != null) {
                customerUser.setStatus(1);
            }
        }
        try {
            if (customer != null) {
                customerDao.update(customer);
            }
            if (customerUser != null) {
                customerUserDao.update(customerUser);
            }

        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }
    }*/

    /*public Map<String, Object> queryIndustryPoolByCondition(HttpServletRequest param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String userName = param.getParameter("userName");
        String userId = param.getParameter("userId");
        String enterpriseName = param.getParameter("enterpriseName");
        int pageNum = Integer.valueOf(param.getParameter("pageNum"));
        int pageSize = Integer.valueOf(param.getParameter("pageSize"));

        StringBuilder sql = new StringBuilder("SELECT " +
                " t3.account AS userName," +
                " cast(t3.id as char) AS userId," +
                " t3.cust_id AS custId," +
                " t3. STATUS AS status," +
                " IFNULL(industrypoolNum,0) AS industrypoolNum, t5.enterprise_name enterpriseName" +
                " FROM t_customer_user t3" +
                " LEFT JOIN (" +
                " SELECT t1.*, COUNT(*) AS industrypoolNum FROM t_cust_industry t1" +
                " LEFT JOIN t_industry_pool t4 ON t1.industry_pool_id =t4.industry_pool_id" +
                " WHERE  t1.`STATUS` = 1 AND t4.STATUS =3" +
                " GROUP BY t1.cust_id) t2 ON t3.cust_id = t2.cust_id" +
                " JOIN t_customer t5 ON t3.cust_id = t5.cust_id" +
                " WHERE t3.user_type = 1 AND t3.`STATUS` = 0 ");
        if (StringUtil.isNotEmpty(userName)) {
            sql.append(" and t3.account='").append(StringEscapeUtils.escapeSql(userName)).append("'");
        }
        if (StringUtil.isNotEmpty(userId)) {
            sql.append(" and t3.id='").append(StringEscapeUtils.escapeSql(userId)).append("'");
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sql.append(" and t5.enterprise_name='").append(StringEscapeUtils.escapeSql(enterpriseName)).append("'");
        }
        sql.append(" order by t5.create_time desc ");

        map.put("total", userInfoDao.queryListBySql(sql.toString()).size());
        List userIndustryPoolList = userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
        Map customerProperty;
        CustomerUserPropertyDO mobileNum;
        for (int i = 0; i < userIndustryPoolList.size(); i++) {
            customerProperty = (Map) userIndustryPoolList.get(i);
            mobileNum = customerUserDao.getProperty(String.valueOf(customerProperty.get("userId")), "mobile_num");
            customerProperty.put("mobileNum", mobileNum == null ? "" : mobileNum.getPropertyValue());
        }
        map.put("userIndustryPoolList", userIndustryPoolList);
        return map;
    }*/

    /*public Map<String, Object> listIndustryPoolByCustomerId(HttpServletRequest param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String customerId = param.getParameter("customerId");
        int pageNum = Integer.valueOf(param.getParameter("pageNum"));
        int pageSize = Integer.valueOf(param.getParameter("pageSize"));
        if (StringUtil.isEmpty(customerId)) {
            throw new TouchException("300", "客户id不能为空");
        }
        StringBuilder sql = new StringBuilder("SELECT\n" +
                "\tt1.NAME AS industryPoolName,\n" +
                "\tt1.industry_pool_id AS industryPoolId,\n" +
                "\tt2.create_time AS createTime,\n" +
                "\tt2.operator AS operator\n" +
                "FROM\n" +
                "\tt_industry_pool t1\n" +
                "LEFT JOIN t_cust_industry t2 ON t1.industry_pool_id = t2.industry_pool_id where 1=1 and t2.status=1 AND   t1.status =3");
        sql.append(" and t2.cust_id='").append(StringEscapeUtils.escapeSql(customerId)).append("'");
        List list = userInfoDao.getSQLQuery(sql.toString()).list();
        map.put("total", list.size());
        map.put("industryPoolList", userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list());
        return map;
    }

    public Map<String, Object> showCustIndustryPoolStatus(HttpServletRequest param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String customerId = param.getParameter("customerId");
        int pageNum = Integer.valueOf(param.getParameter("pageNum"));
        int pageSize = Integer.valueOf(param.getParameter("pageSize"));

        if (StringUtil.isEmpty(customerId)) {
            throw new TouchException("300", "客户id为空");
        }

        StringBuilder sql = new StringBuilder("SELECT\n" +
                "\tt1.`NAME` as industryPoolName,\n" +
                "\tt1.industry_pool_id as industryPoolId,\n" +
                "\tIFNULL(t7.industryName,'') as industryName,\n" +
                "\t(case when t3.status is NULL THEN 2 ELSE t3.status END) as status\n" +
                "FROM\n" +
                "\tt_industry_pool t1\n" +
                "LEFT JOIN (\n" +
                "\tSELECT\n" +
                "\t\t*\n" +
                "\tFROM\n" +
                "\t\tt_cust_industry t2\n" +
                "\tWHERE 1=1 and ");
        sql.append("t2.cust_id='").append(StringEscapeUtils.escapeSql(customerId)).append("' ");
        sql.append(") t3 ON t1.industry_pool_id = t3.industry_pool_id\n" +
                "LEFT JOIN (\n" +
                "\tSELECT\n" +
                "\t\tt6.industry_pool_id AS industryPoolId,\n" +
                "\t\tGROUP_CONCAT(t6.industy_name) AS industryName\n" +
                "\tFROM\n" +
                "\t\t(\n" +
                "\t\t\tSELECT\n" +
                "\t\t\t\tt4.industry_pool_id,\n" +
                "\t\t\t\tt5.industy_name\n" +
                "\t\t\tFROM\n" +
                "\t\t\t\tt_industry_info_rel t4\n" +
                "\t\t\tLEFT JOIN t_industry_info t5 ON t4.industry_info_id = t5.industry_info_id\n" +
                "\t\t) t6\n" +
                "\tGROUP BY\n" +
                "\t\tt6.industry_pool_id\n" +
                ") t7 ON t7.industryPoolId = t1.industry_pool_id where t1.`STATUS`= 3 order by t1.create_time DESC, status ");
        List list = userInfoDao.getSQLQuery(sql.toString()).list();
        map.put("total", list.size());
        map.put("industryPoolStatusList", userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list());
        return map;
    }

    public User getUserByName(String name) {
        return userInfoDao.findUniqueBy("name", name);
    }

    public User getUserBymobileNum(String mobileNum) {
        return userInfoDao.findUniqueBy("mobileNum", mobileNum);
    }

    public User getUserByEmail(String email) {
        return userInfoDao.findUniqueBy("email", email);
    }

    public User getUserByEnterpriseName(String enterpriseName) {
        return userInfoDao.findUniqueBy("enterpriseName", enterpriseName);
    }*/

    /**
     * 根据登录用户名和授权来查询登录用户
     *
     * @param loginName
     * @param source
     * @return
     */
    public UserDO getUserByNameAuthorize(String loginName, int source) {
        return userInfoDao.getUserByNameAuthorize(loginName, source);
    }
}

