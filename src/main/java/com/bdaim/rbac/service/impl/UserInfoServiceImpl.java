package com.bdaim.rbac.service.impl;

import com.bdaim.common.exception.TouchException;
import com.bdaim.common.util.CipherUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.rbac.dao.UserInfoDao;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.rbac.service.UserInfoService;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.transform.Transformers;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.transaction.Transactional;
import java.sql.Types;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Mr.YinXin on 2017/2/21.
 */
@Service("userInfoService")
@Transactional
public class UserInfoServiceImpl implements UserInfoService {
    @Resource
    UserInfoDao userInfoDao;
    @Resource
    JdbcTemplate jdbcTemplate;

    @Resource
    CustomerUserDao customerUserDao;

    @Override
    public UserDO getUserByUserName(String userName) {
        return userInfoDao.findUniqueBy("name", userName);
    }

    @Override
    public UserDO getUserById(Long uid) {
        return userInfoDao.get(uid);
    }

    @Override
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

    @Override
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

    @Override
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

    @Override
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
    @Override
    public void updateFrontPwd(long uid, String oldPwd, String newPwd, int pwdLevel) throws Exception {

        CustomerUserDO customerUserDo = new CustomerUserDO();
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

    @Override
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
                "WHERE t1.id=" + id);
        return userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
    }

    @Override
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

    @Override
    public void updateRegistInfo(String oldValue, String newValue) throws Exception {
        UserDO userDO = userInfoDao.findUniqueBy("mobile_num", oldValue);
        if (null != userDO) {
            userDO.setMobileNum(newValue);
            userInfoDao.saveOrUpdate(userDO);
        }else {
            throw new TouchException("20004", "系统异常，用户查询失败");
        }
    }

    @Override
    public void updateFrontRegistInfo(String userId, String oldValue, String newValue) throws Exception {
        CustomerUserPropertyDO customerUserProperty = customerUserDao.getProperty(userId,"mobile_num");
        if (null != customerUserProperty) {
            customerUserProperty.setPropertyValue(newValue);
            customerUserDao.saveOrUpdate(customerUserProperty);
        }else {
            throw new TouchException("20004", "系统异常，前台用户查询失败");
        }
    }

    @Override
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

    @Override
    public Map<String, Object> queryIndustryPoolByCondition(HttpServletRequest param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        String userName = param.getParameter("userName");
        String userId = param.getParameter("userId");
        String enterpriseName = param.getParameter("enterpriseName");
        int pageNum = Integer.valueOf(param.getParameter("pageNum"));
        int pageSize = Integer.valueOf(param.getParameter("pageSize"));

        StringBuilder sql = new StringBuilder("SELECT *,\n" +
                "\tCOUNT(*) as industrypoolNum\n" +
                "FROM\n" +
                "\t(\n" +
                "\t\tSELECT\n" +
                "\t\t\tt2. NAME AS userName,\n" +
                "\t\t\tt2.id AS userId,\n" +
                "\t\t\tt2.cust_id AS custId,\n" +
                "\t\t\tt2.mobile_num AS mobileNum,\n" +
                "\t\t\tt2.enterprise_name AS enterpriseName,\n" +
                "\t\t\tt2.status as status\n" +
                "\t\tFROM\n" +
                "\t\t\tt_cust_industry t1\n" +
                "\t\tLEFT JOIN t_user t2 ON t1.cust_id = t2.cust_id  where t2.user_type=1 \n" +
                "\t) t3 where 1=1  ");
        if (StringUtil.isNotEmpty(userName)) {
            sql.append(" and t3.userName='").append(StringEscapeUtils.escapeSql(userName)).append("'");
        }
        if (StringUtil.isNotEmpty(userId)) {
            sql.append(" and t3.userId='").append(StringEscapeUtils.escapeSql(userId)).append("'");
        }
        if (StringUtil.isNotEmpty(enterpriseName)) {
            sql.append(" and t3.enterpriseName='").append(StringEscapeUtils.escapeSql(enterpriseName)).append("'");
        }
        sql.append(" group by t3.custId");
        List list = userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
        map.put("total", list.size());
        map.put("userIndustryPoolList", list);
        return map;
    }

    @Override
    public Map<String, Object> listIndustryPoolByCustomerId(HttpServletRequest param) throws Exception {
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
                "LEFT JOIN t_cust_industry t2 ON t1.industry_pool_id = t2.industry_pool_id where 1=1 ");
        sql.append(" and t2.cust_id='").append(StringEscapeUtils.escapeSql(customerId)).append("'");
        List list = userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
        map.put("total", list.size());
        map.put("industryPoolList", list);
        return map;
    }

    @Override
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
                "\t(case when t3.cust_id is NULL THEN 2 ELSE 1 END) as status\n" +
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
                ") t7 ON t7.industryPoolId = t1.industry_pool_id");
        List list = userInfoDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
        map.put("total", list.size());
        map.put("industryPoolStatusList", list);
        return map;
    }

    @Override
    public UserDO getUserByName(String name) {
        return userInfoDao.findUniqueBy("name", name);
    }

    @Override
    public UserDO getUserBymobileNum(String mobileNum) {
        return userInfoDao.findUniqueBy("mobileNum", mobileNum);
    }

    @Override
    public UserDO getUserByEmail(String email) {
        return userInfoDao.findUniqueBy("email", email);
    }


    @Override
    public Integer getUserByLoginPassWord(String payPassWord, Long userId) {

        Integer code = null;
        String loginPassWord = jdbcTemplate.queryForObject(
                "SELECT PASSWORD FROM t_user t WHERE t.id =" + userId, String.class);

        if (CipherUtil.generatePassword(payPassWord).equals(loginPassWord)) {
            code = 1;
        } else {
            code = 0;
        }

        return code;
    }

    @Override
    public UserDO getUserByEnterpriseName(String enterpriseName) {
        return userInfoDao.findUniqueBy("enterpriseName", enterpriseName);
    }

    @Override
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
}

