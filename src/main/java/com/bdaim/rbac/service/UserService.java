package com.bdaim.rbac.service;

import com.bdaim.auth.LoginUser;
import com.bdaim.common.cache.BeanCache;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.helper.JDBCHelper;
import com.bdaim.common.service.DaoService;
import com.bdaim.common.spring.ConfigPropertiesHolder;
import com.bdaim.customer.dto.CustomerRegistDTO;
import com.bdaim.customs.dao.StationDao;
import com.bdaim.customs.entity.Station;
import com.bdaim.rbac.DataFromEnum;
import com.bdaim.rbac.dao.RoleDao;
import com.bdaim.rbac.dao.UserDao;
import com.bdaim.rbac.dao.UserRoleDao;
import com.bdaim.rbac.dto.AgentDTO;
import com.bdaim.rbac.dto.UserDTO;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.rbac.dto.UserRoles;
import com.bdaim.rbac.entity.User;
import com.bdaim.rbac.entity.UserDO;
import com.bdaim.rbac.entity.UserProperty;
import com.bdaim.rbac.vo.QueryDataParam;
import com.bdaim.rbac.vo.UserInfo;
import com.bdaim.util.*;

import com.bdaim.util.excel.EasyExcelUtil;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.xssf.usermodel.XSSFCell;
import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.transaction.Transactional;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service("userService")
@Transactional
public class UserService {
    private static Logger logger = LoggerFactory.getLogger(UserService.class);
    @Resource
    private UserDao userDao;
    @Resource
    private RoleDao roleDao;
    @Resource
    private StationDao stationDao;

    /**
     * 管理员类型
     */
    public final static String ADMIN_USER_TYPE = "1";

    /**
     * 操作员类型
     */
    public final static String OPERATOR_USER_TYPE = "2";


    @SuppressWarnings("unchecked")
    public List<User> getAllUsers() {
        return userDao.createQuery("From User").list();
    }

    @SuppressWarnings("unchecked")
    public List<User> getUsersByCondition(Map<String, Object> map,
                                          Map<String, Object> likeMap) {
        String hql = "From User t";
        return userDao.getHqlQuery(hql, map, likeMap, null).list();
    }

    public User getUserById(Long uid) {
        return userDao.get(uid);
    }


    public Boolean saveUserMessage(String loginUserName, Long loginId, boolean isAdminOperate, UserDTO userDTO) {
        Boolean flag = true;
        try {
            //构造用户信息
            userDTO.setStatus(0);
            userDTO.setSource(DataFromEnum.SYSTEM.getValue());
            //加密密码
            if (StringUtil.isNotEmpty(userDTO.getPassword())) {
                String passwordMd5 = CipherUtil.generatePassword(userDTO.getPassword());
                userDTO.setPassword(passwordMd5);
            }
            userDTO.setOptuser(loginUserName);
            userDTO.setCreateTime(new Date());
            userDTO.setUserType(2);
            //根据id查询user对象
            //判断id是否为空，空做新增，非空修改
            if (userDTO.getId() == null) {
                Long userId = IDHelper.getID();
                userDTO.setId(userId);
                //添加用户信息
                userDao.insertUser(userDTO);

            } else {
                //修改用户基本信息
                userDao.updateUserMessage(userDTO);
                if (isAdminOperate) {
                    userDao.deleteByUserId(userDTO.getId());
                } else {
                    userDao.deleteRoleByUserId(loginId, userDTO.getId());
                }
            }
            //添加场站信息
            if (StringUtil.isNotEmpty(userDTO.getStationId())) {
                log.info("场站id是：" + userDTO.getStationId());
                UserProperty userProperty;
                userProperty = userDao.getProperty(userDTO.getId(), "station_id");
                if (userProperty == null) {
                    userProperty = new UserProperty();
                    userProperty.setUserId(userDTO.getId());
                    userProperty.setPropertyName("station_id");
                    userProperty.setPropertyValue(userDTO.getStationId());
                    userProperty.setCreateTime(new Timestamp(System.currentTimeMillis()));
                } else {
                    userProperty.setPropertyValue(userDTO.getStationId());
                }
                userDao.saveOrUpdate(userProperty);
            }
            //添加用户职位信息
            insertUserRole(userDTO.getId(), userDTO.getRoles(), loginUserName);
        } catch (Exception e) {
            logger.error("员工信息编辑异常" + e);
            flag = false;
        }
        return flag;
    }

    /**
     * 添加用户职位信息
     *
     * @param
     */
    public void insertUserRole(Long id, String roleId, String loginUserName) {
        if (StringUtil.isNotEmpty(roleId)) {
            String[] roleIds = roleId.split(",");
            if (roleIds.length > 0) {
                for (int i = 0; i < roleIds.length; i++) {
                    int insertNum = userDao.executeUpdateSQL("insert into t_user_role_rel(ID,ROLE,OPTUSER,CREATE_TIME)" +
                            " VALUES(?,?,?,now())", id, roleIds[i], loginUserName);
                    logger.info("添加职位信息数量是：" + insertNum + "用户id是：" + id);
                }
            }
        }
    }

    /**
     * 检查用户名是否唯一
     *
     * @return
     */

    public boolean checkUsernameUnique(String userName, Long id) {
        String sql = "";
        List<Object> params = new ArrayList<>();
        if (id == null) {
            sql = "select count(*) as COUNT from t_user where name = ?";
            params.add(userName);
        } else {
            sql = "select count(*) as COUNT from t_user where name = ? and id<>?";
            params.add(userName);
            params.add(id);
        }
        List<Map<String, Object>> list = userDao.sqlQuery(sql, params.toArray());
        if (list != null && !list.isEmpty()) {
            Map<String, Object> map = list.get(0);
            int count = NumberConvertUtil.everythingToInt(map.get("COUNT"));
            if (count > 0) {
                return false;
            }
        }
        return true;
    }

    /**
     * 删除用户信息（逻辑删除）
     *
     * @return
     */
    public boolean deleteUser(Long userId, int status) {
        //查看当前删除的用户是否是admin
        UserDO userDo = userDao.getUserMessage(userId);
        //如果待删除的用户是管理员，则不可删除
        if ("admin".equals(userDo.getName())) return false;
        try {
            //删除用户信息
            userDao.updateUserStatus(userId, status);
            //删除用户权限信息
            roleDao.deleteByUserId(userId);
        } catch (Exception e) {
            log.error("删除用户信息异常" + e);
            return false;
        } finally {
        }
        return true;
    }

    /**
     * 更改用户状态
     *
     * @param userId
     * @param status
     */
    public boolean updateUserStatus(Long userId, Integer status) {
        Boolean flag = false;
        try {
            //查看当前删除的用户是否是admin
            UserDO userDo = userDao.getUserMessage(userId);
            //如果待修改状态的用户是管理员，则不可修改状态
            if ("admin".equals(userDo.getName())) return false;
            userDao.updateUserStatus(userId, status);
            flag = true;
        } catch (Exception e) {
            logger.error("修改用户状态异常" + e);
        }
        return flag;
    }

    public Page queryUserList(PageParam page, UserDTO userDTO, LoginUser loginUser) {
        Long loginId = loginUser.getId();
        boolean ifAdmin = loginUser.isAdmin();
        List<Object> params = new ArrayList<>();
        StringBuffer sql = new StringBuffer("SELECT cast(u.ID as char) id,u.PASSWORD password,u.REALNAME realName,u.name account," +
                "u.mobile_num phone,d.`NAME` deptName,GROUP_CONCAT(r.`NAME`) roles ,cast(r.ID as char) roleId,cast(d.ID as char)deptId," +
                "u.`STATUS` ");
        sql.append("FROM t_user u LEFT JOIN t_user_role_rel p ON u.ID = p.ID\n");
        sql.append("LEFT JOIN t_role r ON p.ROLE = r.ID ");
        sql.append("LEFT JOIN t_dept d ON u.DEPTID = d.ID ");
        sql.append("WHERE 1=1 ");
        //admin可以查询所有部门信息  普通用户只能查本部门的
        if (ifAdmin == false) {
            sql.append(" and d.id in (SELECT u.DEPTID FROM t_user u WHERE u.ID = ?)");
            params.add(loginId);
        }
        if (userDTO.getId() != null) {
            sql.append(" and u.id = ?");
            params.add(userDTO.getId());
        }
        if (StringUtil.isNotEmpty(userDTO.getRealName())) {
            sql.append(" and u.REALNAME like ? ");
            params.add("%" + userDTO.getRealName() + "%");
        }
        if (StringUtil.isNotEmpty(userDTO.getUserName())) {
            sql.append(" and u.name = ?");
            params.add(userDTO.getUserName());
        }
        if (StringUtil.isNotEmpty(userDTO.getMobileNumber())) {
            sql.append(" and u.mobile_num = ?");
            params.add(userDTO.getMobileNumber());
        }
        if (userDTO.getDeptId() != null) {
            sql.append(" and u.DEPTID = ?");
            params.add(userDTO.getDeptId());
        }
        if (userDTO.getStatus() != null) {
            sql.append(" and u.status = ?");
            params.add(userDTO.getStatus());
        }
        sql.append(" and u.status!=2 ");
        sql.append(" GROUP BY u.ID ORDER BY u.CREATE_TIME DESC ");
        Page dataPage = userDao.sqlPageQuery(sql.toString(), page.getPageNum(), page.getPageSize(), params.toArray());
        List<Map<String, Object>> data = dataPage.getData();
        //添加场站信息
        if (data.size() > 0) {
            for (int i = 0; i < data.size(); i++) {
                String stationName = "";
                long userId = NumberConvertUtil.parseLong(data.get(i).get("id"));
                log.info("用戶id是：" + userId);
                UserProperty userProperty = userDao.getProperty(userId, "station_id");
                if (userProperty != null) {
                    String propertyValue = userProperty.getPropertyValue();
                    data.get(i).put("stationId", propertyValue);
                    log.info("场站id是：" + propertyValue);
                    List<String> stationIdList = Arrays.asList(propertyValue.split(","));
                    for (int j = 0; j < stationIdList.size(); j++) {
                        Station station = stationDao.getStationById(NumberConvertUtil.parseInt(stationIdList.get(j)));
                        if (station != null && StringUtil.isNotEmpty(station.getName())) {
                            stationName += station.getName() + ",";
                        }
                    }
                }
                if (StringUtil.isNotEmpty(stationName)) {
                    stationName = stationName.substring(0, stationName.length() - 1);
                }
                data.get(i).put("stationName", stationName);
            }
        }
        return dataPage;
    }

    /**
     * 日期格式化(毫秒)
     */
    private final static DateTimeFormatter YDMHMSS = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.S");

    /**
     * 日期格式化(秒)
     */
    private final static DateTimeFormatter YDMHMS = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss");


    private Log log = LogFactory.getLog(UserService.class);
    @Resource
    private UserRoleDao userRoleDao;


    @Deprecated
    public List<UserInfo> queryUser(QueryDataParam param) {
        StringBuilder queryData = new StringBuilder();
        StringBuilder builder = new StringBuilder();
        queryData.append("select t.* ");
        queryData.append("  from ( ");
        builder.append(" select t.id,t.status,t.username,t.realname,t.deptname,t.rolename,t.create_time,t.optuser,t.deptId ");

        builder.append(" from ( ");

        builder.append(" select u.ID,u.status,u.NAME as username,u.REALNAME,d.id as deptId,");
        builder.append(" d.NAME as deptname,");
        builder.append(" GROUP_CONCAT(r.NAME) ");

        builder.append(" as rolename,u.CREATE_TIME,u.OPTUSER ");
        builder.append(" from t_user u");
        builder.append(" left join t_dept d on u.DEPTID = d.ID ");
        builder.append(" left join t_user_role_rel rel on u.ID = rel.ID");
        builder.append(" left join t_role r on rel.ROLE = r.ID");
        builder.append(" where  u.status=0 ");
        Long deptId = param.getDeptId();
        if (deptId != null) {
            builder.append(" and d.ID = " + deptId);
        }
        Long roleId = param.getRoleId();
        if (roleId != null) {
            builder.append(" and r.ID = " + roleId);
        }
        builder.append(" group by u.ID,u.status,u.NAME,u.REALNAME,d.id,d.NAME,u.CREATE_TIME,u.OPTUSER ");
        Page page = param.getPage();
        int countPerpage = page.getCountPerPage();
        int index = page.getPageIndex();
        int start = index * countPerpage;

        builder.append(" ) t ");
        builder.append(" where 1=1 ");
        String condition = param.getCondition();
        if (!StringUtils.isEmpty(condition)) {
            builder.append(" and t.username like '%" + condition + "%' or t.realname like '%" + condition + "%'");
        }
        builder.append(" order by t.status asc,t.create_time desc ");
        queryData.append(builder);
        queryData.append(" ) t ");
        queryData.append(" limit " + start + "," + countPerpage);

        StringBuilder queryCount = new StringBuilder();
        queryCount.append(" select count(*) as COUNT from (");
        queryCount.append(builder);
        queryCount.append(" ) t ");

        //com_audit_trail
        String isLockedSql = "select AUD_USER,count(*) as COUNT from COM_AUDIT_TRAIL where AUD_ACTION='AUTHENTICATION_FAILED' group by AUD_USER";

        String queryDataSql = queryData.toString();
        String queryCountSql = queryCount.toString();
        Connection con = null;
        List dstmt = null;
        List cstmt = null;
        PreparedStatement lockstmt = null;
        List crs = null;
        List drs = null;
        List lockrs = null;
        List<UserInfo> vos = new ArrayList<UserInfo>();
        try {
            drs = this.userDao.getSQLQuery(queryDataSql).list();
            crs = this.userDao.getSQLQuery(queryCountSql).list();
            lockrs = this.userDao.getSQLQuery(isLockedSql).list();

            int allcount = 0;
            if (crs.size() > 0) {
                allcount = Integer.parseInt(String.valueOf(crs.get(0)));
            }
            page.setCount(allcount);

            SimpleDateFormat formater = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
            for (int i = 0; i < drs.size(); i++) {
                //t.id,t.status,t.username,t.realname,t.deptname,t.rolename,t.create_time,t.optuser,t.deptId
                Object[] obj = (Object[]) drs.get(i);
                long id = Long.parseLong(String.valueOf(obj[0]));
                String username = String.valueOf(obj[2]);
                String realname = String.valueOf(obj[3]);
                String deptname = String.valueOf(obj[4]);
                String rolename = String.valueOf(obj[5]);
                Timestamp stamp = new Timestamp(Long.parseLong(String.valueOf(obj[6])));
                Date createTime = stamp == null ? null : new Date(stamp.getTime());
                String optuser = String.valueOf(obj[7]);
                int status = Integer.parseInt(String.valueOf(obj[1]));
                Long deptid = Long.parseLong(String.valueOf(obj[8]));
                UserInfo vo = new UserInfo();
                vo.setId(String.valueOf(id));
                vo.setName(username);
                vo.setRealName(realname);
                vo.setDeptName(deptname);
                vo.setRoles(rolename);
                vo.setCreateTime(createTime == null ? "" : formater.format(createTime));
                vo.setOptuser(optuser);
                vo.setDeptId(String.valueOf(deptid));
                vo.setStatus(status);
                if (status == 0) {
                    vo.setStatusName("正常");
                } else {
                    vo.setStatusName("已删除");
                }
                vos.add(vo);
            }

            for (int i = 0; i < lockrs.size(); i++) {
                //AUD_USER,count(*) as COUNT
                Object[] obj = (Object[]) lockrs.get(i);
                String audUser = String.valueOf(obj[0]);
                int count = Integer.parseInt(String.valueOf(obj[1]));
                for (int j = 0; j < vos.size(); j++) {
                    String username = vos.get(j).getName();
                    if (audUser.contains(username)) {
                        if (count >= 10) {
                            vos.get(j).setIsLocked(1);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getStackTrace());
        } finally {
        }

        return vos;
    }

    public List<UserInfo> queryUserV1(QueryDataParam param) {
        StringBuilder builder = new StringBuilder();
        List<Object> params = new ArrayList<>();
        builder.append(" select t.id,t.status,t.username,t.realname,t.deptname,t.rolename,t.create_time,t.optuser,t.deptId ");

        builder.append(" from ( ");
        builder.append(" select u.ID,u.status,u.NAME as username,u.REALNAME,d.id as deptId,");
        builder.append(" d.NAME as deptname,");
        builder.append(" GROUP_CONCAT(r.NAME) ");

        builder.append(" as rolename,u.CREATE_TIME,u.OPTUSER ");
        builder.append(" from t_user u");
        builder.append(" left join t_dept d on u.DEPTID = d.ID ");
        builder.append(" left join t_user_role_rel rel on u.ID = rel.ID");
        builder.append(" left join t_role r on rel.ROLE = r.ID");
        builder.append(" where  u.status=0 ");
        if (param.getDeptId() != null) {
            builder.append(" and d.ID = ?");
            params.add(param.getDeptId());
        }
        if (param.getRoleId() != null) {
            builder.append(" and r.ID = ?");
            params.add(param.getRoleId());
        }
        builder.append(" group by u.ID,u.status,u.NAME,u.REALNAME,d.id,d.NAME,u.CREATE_TIME,u.OPTUSER ");
        Page page = param.getPage();
        int countPerpage = page.getCountPerPage();
        int start = (page.getPageIndex()+1);



        builder.append(" ) t ");
        builder.append(" where 1=1 ");
        String condition = param.getCondition();
        if (StringUtil.isNotEmpty(condition)) {
            builder.append(" and (t.username like ? or t.realname like ? or t.deptname like ? or t.rolename like ?) ");
            params.add("%" + condition + "%");
            params.add("%" + condition + "%");
            params.add("%" + condition + "%");
            params.add("%" + condition + "%");
        }
        builder.append(" order by t.status asc,t.create_time desc ");

        List<Map<String, Object>> drs, lockrs;
        List<UserInfo> userList = null;
        Page pageData;
        try {
            //分页查询用户列表
            pageData = this.userDao.sqlPageQuery(builder.toString(), start, countPerpage, params.toArray());
            param.getPage().setCount(pageData.getTotal());
            drs = pageData.getData();
            if (drs.size() > 0) {
                userList = new ArrayList<>();
            }
            //com_audit_trail
            String isLockedSql = "select AUD_USER,count(*) as COUNT from COM_AUDIT_TRAIL where AUD_ACTION='AUTHENTICATION_FAILED' " +
                    "group by AUD_USER";
            lockrs = this.userDao.sqlQuery(isLockedSql);

            UserInfo vo;
            Map<String, Object> m;
            for (int i = 0; i < drs.size(); i++) {
                m = drs.get(i);
                vo = new UserInfo();
                vo.setId(String.valueOf(m.get("id")));
                vo.setName(String.valueOf(m.get("username")));
                vo.setRealName(String.valueOf(m.get("realname")));
                vo.setDeptName(String.valueOf(m.get("deptname")));
                vo.setRoles(String.valueOf(m.get("rolename")));

                if (m.get("create_time") != null
                        && !"null".equals(String.valueOf(m.get("create_time")))) {
                    vo.setCreateTime(LocalDateTime.parse(String.valueOf(m.get("create_time")), YDMHMSS).format(YDMHMS));
                }
                vo.setOptuser(String.valueOf(m.get("optuser")));
                if (!"null".equals(String.valueOf(m.get("deptId")))) {
                    vo.setDeptId(String.valueOf(m.get("deptId")));
                    if(vo.getDeptId().toString().equals("100000")){
                        CustomerRegistDTO customerRegistDTO=new CustomerRegistDTO();
                        UserProperty  county = userDao.getProperty(Long.parseLong(vo.getId()), "customer_name");
                        if(county!=null) {
                            customerRegistDTO.setName(userDao.getProperty(Long.parseLong(vo.getId()), "customer_name").getPropertyValue());
                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "bli_number");

                        if(county!=null) {
                            customerRegistDTO.setBliNumber(userDao.getProperty(Long.parseLong(vo.getId()), "bli_number").getPropertyValue());
                        }

                        county = userDao.getProperty(Long.parseLong(vo.getId()), "province");
                        if(county!=null) {
                            customerRegistDTO.setProvince(userDao.getProperty(Long.parseLong(vo.getId()), "province").getPropertyValue());
                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "city");
                        if(county!=null) {
                            customerRegistDTO.setCity(userDao.getProperty(Long.parseLong(vo.getId()), "city").getPropertyValue());
                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "county");
                        if(county!=null){
                            customerRegistDTO.setCountry(userDao.getProperty(Long.parseLong(vo.getId()),"county").getPropertyValue());

                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "taxpayer_id");
                        if(county!=null) {
                            customerRegistDTO.setTaxPayerId(userDao.getProperty(Long.parseLong(vo.getId()), "taxpayer_id").getPropertyValue());
                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "bli_path");
                        if(county!=null) {
                            customerRegistDTO.setBliPath(userDao.getProperty(Long.parseLong(vo.getId()), "bli_path").getPropertyValue());
                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "bank");
                        if(county!=null) {
                            customerRegistDTO.setBank(userDao.getProperty(Long.parseLong(vo.getId()), "bank").getPropertyValue());
                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "bank_account");

                        if(county!=null) {
                            customerRegistDTO.setBankAccount(userDao.getProperty(Long.parseLong(vo.getId()), "bank_account").getPropertyValue());
                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "bank_account_certificate");

                        if(county!=null) {
                            customerRegistDTO.setBankAccountCertificate(userDao.getProperty(Long.parseLong(vo.getId()), "bank_account_certificate").getPropertyValue());
                        }

                        county = userDao.getProperty(Long.parseLong(vo.getId()), "reg_address");
                       if(county!=null){
                           customerRegistDTO.setAddress(userDao.getProperty(Long.parseLong(vo.getId()),"reg_address").getPropertyValue());

                       }

                        county = userDao.getProperty(Long.parseLong(vo.getId()), "taxpayerCertificatePath");
                        if(county!=null) {
                            customerRegistDTO.setTaxpayerCertificatePath(userDao.getProperty(Long.parseLong(vo.getId()), "taxpayerCertificatePath").getPropertyValue());
                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "mobile");
                        if(county!=null) {
                            customerRegistDTO.setMobile(userDao.getProperty(Long.parseLong(vo.getId()), "mobile").getPropertyValue());
                        }

                        county = userDao.getProperty(Long.parseLong(vo.getId()), "email");
                        if(county!=null) {
                            customerRegistDTO.setEmail(userDao.getProperty(Long.parseLong(vo.getId()), "email").getPropertyValue());
                        }
                        county = userDao.getProperty(Long.parseLong(vo.getId()), "title");
                        if(county!=null) {
                            customerRegistDTO.setTitle(userDao.getProperty(Long.parseLong(vo.getId()), "title").getPropertyValue());
                        }
                        vo.setCustomerRegistDTO(customerRegistDTO);
                    }
                }
                if (!"null".equals(String.valueOf(m.get("status")))) {
                    vo.setStatus(NumberConvertUtil.parseInt(m.get("status")));
                }
                if ("0".equals(String.valueOf(m.get("status")))) {
                    vo.setStatusName("正常");
                } else {
                    vo.setStatusName("已删除");
                }
                userList.add(vo);
            }

            // 判断用户是否被登录锁定
            Map<String, Object> v;
            String audUser;
            int count;
            for (int i = 0; i < lockrs.size(); i++) {
                v = lockrs.get(i);
                audUser = String.valueOf(v.get("AUD_USER"));
                count = NumberConvertUtil.parseInt(v.get("COUNT"));
                for (UserInfo user : userList) {
                    if (audUser.contains(user.getName())) {
                        if (count >= 10) {
                            user.setIsLocked(1);
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.error("查询后台用户失败,", e);
        }

        return userList;
    }

    @SuppressWarnings("unchecked")
    public boolean checkPassword(Long id, String password) {
        String sql = "select count(*) as COUNT from t_user where id=? and password=?";
        List<Map<String, Object>> list = userDao.sqlQuery(sql, id, password);
        if (list != null && !list.isEmpty()) {
            Map<String, Object> map = list.get(0);
            int count = NumberConvertUtil.everythingToInt(map.get("COUNT"));
            if (count > 0) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked")
    public boolean checkUsernameUnique(UserDTO user) {
        Long id = user.getId();
        String sql = "";
        List<Object> params = new ArrayList<>();
        if (id == null) {
            sql = "select count(*) as COUNT from t_user where name = ? and status=0";
            params.add(user.getName());
        } else {
            sql = "select count(*) as COUNT from t_user where name = ? and status=0 and id<>?";
            params.add(user.getName());
            params.add(id);
        }
        List<Map<String, Object>> list = userDao.sqlQuery(sql, params.toArray());
        if (list != null && !list.isEmpty()) {
            Map<String, Object> map = list.get(0);
            int count = NumberConvertUtil.everythingToInt(map.get("COUNT"));
            if (count > 0) {
                return false;
            }
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public User queryUserByUsername(String username) {
        String sql = "select ID,NAME from t_user where name=?";
        List<Map<String, Object>> list = userDao.sqlQuery(sql, username);
        User user = null;
        if (list != null && !list.isEmpty()) {
            Map<String, Object> map = list.get(0);
            Long id = NumberConvertUtil.everythingToLong(map.get("ID"));
            String name = (String) map.get("NAME");
            user = new User();
            user.setId(id);
            user.setName(name);
        }
        return user;
    }

    @SuppressWarnings("unchecked")
    public boolean updateUser(UserDTO user) {
        try {
            userDao.update(user);
        } catch (Exception e) {
            log.error(e.getStackTrace());
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }

    public boolean saveUser(UserRoles userRoles, Long operateUserId, boolean isAdminOperate) {
        try {
            UserDTO user = userRoles.getUser();
            Long id = user.getId();
            //如果id为null则为新增的保存造作，否则则为更新的保存操作
            if (id == null) {
                id = IDHelper.getID();
                user.setId(id);
                userDao.insert(user);
                userRoleDao.insert(userRoles);
            } else {
                //更新用户信息
                userDao.update(user);
                //删除用户和角色的对应关系，普通操作用户和超级管理员的删除方式不同。
                if (isAdminOperate) {
                    userRoleDao.deleteByUserId(id);
                } else {
                    userRoleDao.delete(operateUserId, id);
                }
                //插入用户和角色的对应关系
                userRoleDao.insert(userRoles);
            }
        } catch (SQLException e) {
            log.error(e.getStackTrace());
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }

    @SuppressWarnings("unchecked")
    public UserInfo queryUserInfo(String userName) {
        StringBuilder builder = new StringBuilder();
        List<Object> params = new ArrayList<>();
        builder.append(" select distinct u.ID,u.REALNAME,GROUP_CONCAT(r.NAME) as ROLENAME,d.NAME as DEPTNAME,t.logintime,u.NAME from t_user u");
        builder.append(" left join t_user_role_rel rel on u.ID = rel.ID");
        builder.append(" left join t_role r on rel.ROLE = r.ID");
        builder.append(" left join t_dept d on u.DEPTID = d.ID");
        builder.append(" left join (select username,logintime from t_login_log where username = ?" +
                " order by logintime desc limit 0,1) t on u.name = t.username");
        params.add(userName);
        builder.append(" where u.NAME = ? group by u.ID");
        params.add(userName);
        String sql = builder.toString();
        List<Map<String, Object>> list = userDao.sqlQuery(sql, params.toArray());
        UserInfo info = null;

        if (list != null && !list.isEmpty()) {
            info = new UserInfo();
            String roleNames = "";
            for (Map<String, Object> map : list) {
                String roleName = (String) map.get("ROLENAME");
                if (StringUtils.isEmpty(roleName)) {
                    continue;
                }
                roleNames += roleName;
                roleNames += ",";
            }
            if (!StringUtils.isEmpty(roleNames)) {
                roleNames = roleNames.substring(0, roleNames.length() - 1);
            }
            Map<String, Object> top = list.get(0);
            Long id = ((BigInteger) top.get("ID")).longValue();
            String realName = (String) top.get("REALNAME");
            String name = (String) top.get("NAME");
            String deptName = (String) top.get("DEPTNAME");
            Object last = top.get("logintime");
            String lastLogin = "";
            if (last != null) {
                lastLogin = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss").format((Date) last);
            }
            info.setDeptName(deptName == null ? "" : deptName);
            info.setLastLoginTime(lastLogin);
            info.setRoles(roleNames);
            if ("admin".equals(userName)) {
                info.setRoles("超级管理员");
            }
            info.setName(name == null ? "" : name);
            info.setRealName(realName == null ? "" : realName);
            info.setId(String.valueOf(id));
        }
        return info;
    }

    @SuppressWarnings("unchecked")
    public UserInfo queryUserInfo(Long id) {
        StringBuilder builder = new StringBuilder();
        builder.append(" select t.ID,t.USERNAME,t.REALNAME,t.DEPTNAME,t.ROLENAME,t.CREATE_TIME,t.OPTUSER,t.DEPTID,EMAIL," +
                "EMAIL_GROUP,CONNECTION_INFO from");
        builder.append(" (select u.ID,u.NAME as username,u.REALNAME,d.id as deptId,EMAIL,EMAIL_GROUP,CONNECTION_INFO,");
        builder.append(" d.NAME as deptname, ");

        builder.append(" GROUP_CONCAT(r.NAME) as rolename,");

        builder.append(" u.CREATE_TIME,u.OPTUSER");
        builder.append(" from t_user u");
        builder.append(" left join t_dept d on u.DEPTID = d.ID ");
        builder.append(" left join t_user_role_rel rel on u.ID = rel.ID");
        builder.append(" left join t_role r on rel.ROLE = r.ID");
        builder.append(" where  u.ID=?");
        builder.append(" group by u.ID,u.NAME,u.REALNAME,d.id,EMAIL,EMAIL_GROUP,CONNECTION_INFO,d.NAME,u.CREATE_TIME,u.OPTUSER) t");
        builder.append(" where 1 = 1 ");
        List<Map<String, Object>> list = userDao.sqlQuery(builder.toString(), id);
        UserInfo info = null;
        if (list != null && !list.isEmpty()) {
            info = new UserInfo();
            Map<String, Object> map = list.get(0);
            Long userId = NumberConvertUtil.everythingToLong(map.get("ID"));
            String name = (String) map.get("USERNAME");
            String realname = (String) map.get("REALNAME");
            Long deptId = NumberConvertUtil.everythingToLong(map.get("DEPTID"));
            String deptname = (String) map.get("DEPTNAME");
            String rolename = NumberConvertUtil.everythingToString(map.get("ROLENAME"));
            String optuser = (String) map.get("OPTUSER");
            Date create = (Date) map.get("CREATE_TIME");
            String create_time = DateUtil.format(create, "yyyy-MM-dd HH:mm:ss");
            info.setId(String.valueOf(userId));
            info.setName(name);
            info.setRealName(realname);
            info.setOptuser(optuser);
            info.setCreateTime(create_time);
            info.setDeptId(String.valueOf(deptId));
            info.setRoles(rolename);
            info.setDeptName(deptname);
            info.setConnectionInfo(map.get("CONNECTION_INFO") == null ? "" : map.get("CONNECTION_INFO").toString());
            info.setEmainGroup(map.get("EMAIL_GROUP") == null ? "" : map.get("EMAIL_GROUP").toString());
            info.setEmail(map.get("EMAIL") == null ? "" : map.get("EMAIL_GROUP").toString());
        }

        return info;
    }

    public UserDTO queryUserById(Long id) {
        String sql = "select ID,NAME,STATUS from t_user where id=?";
        List<Map<String, Object>> list = userDao.sqlQuery(sql, id);
        UserDTO user = null;
        if (list != null && !list.isEmpty()) {
            Map<String, Object> map = list.get(0);
            Long uid = NumberConvertUtil.everythingToLong(map.get("ID"));
            ;
            String name = (String) map.get("NAME");
            int status = NumberConvertUtil.everythingToInt(map.get("STATUS"));
            user = new UserDTO();
            user.setId(uid);
            user.setName(name);
            user.setStatus(status);
        }
        return user;
    }


    public boolean checkPassword(String username, String password) {
        String sql = "select count(*) as COUNT from t_user where name='" + username + "' and password='" + password + "' and status = 0";
        List<Map<String, Object>> list = userDao.sqlQuery(sql);
        if (list != null && !list.isEmpty()) {
            Map<String, Object> map = list.get(0);
            int count = ((BigInteger) map.get("COUNT")).intValue();
            if (count > 0) {
                return true;
            }
        }
        return false;
    }


    public boolean deleteUser(UserDTO user) {
        //如果待删除的用户是管理员，则不可删除
        if ("admin".equals(user.getName())) {
            return false;
        }
        try {
            userDao.delete(user);
            //following delete t_user_role_rel table ,relation datas...
            userRoleDao.deleteByUserId(user.getId());
        } catch (SQLException e) {
            log.error(e.getStackTrace());
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }

    public boolean updateUser(UserInfo userInfo) {
        try {

            StringBuffer sql = new StringBuffer("update t_user set ");
            List<Object> params = new ArrayList<>();
            if (userInfo.getRealName() != null) {
                sql.append(" realName=? ,");
                params.add(userInfo.getRealName());
            }
            if (!StringUtils.isEmpty(userInfo.getConnectionInfo())) {
                sql.append("CONNECTION_INFO =? ,");
                params.add(userInfo.getConnectionInfo());
            }
            if (!StringUtils.isEmpty(userInfo.getEmainGroup())) {
                sql.append(" EMAIL_GROUP=?, ");
                params.add(userInfo.getEmainGroup());
            }
            if (!StringUtils.isEmpty(userInfo.getEmail())) {
                sql.append(" EMAIL=? ,");
                params.add(userInfo.getEmail());
            }
            if (!StringUtils.isEmpty(userInfo.getPassword())) {
                sql.append(" password=?,");
                params.add(CipherUtil.generatePassword(userInfo.getPassword()));
            }
            String lastSql = sql.substring(0, sql.length() - 1) + " where ID=? and password=?";
            params.add(userInfo.getId());
            params.add(CipherUtil.generatePassword(userInfo.getOldPassword()));
            int res = this.userDao.executeUpdateSQL(lastSql, params.toArray());

            return true;
        } catch (Exception e) {
            log.error("", e);
            return false;
        }
    }

    public boolean saveUser(UserRoles userRoles, Long operateUserId, boolean isAdminOperate, String customerIds,
                            String labelIds, String categoryIds, String channelIds) {
        UserRoleDao userRoleDao = (UserRoleDao) BeanCache.getBean(UserRoleDao.class.getName());
        @SuppressWarnings("unchecked")
        DaoService<User> userDaoService = (DaoService<User>) BeanCache.getBean(UserDao.class.getName());
        Connection con = null;
        try {
            UserDTO user = userRoles.getUser();
            Long id = user.getId();
            //如果id为null则为新增的保存造作，否则则为更新的保存操作
            if (id == null) {
                id = IDHelper.getID();
                user.setId(id);
                userDao.insert(user);
                userRoleDao.insert(userRoles);
                insertCustomerPermission(customerIds, id);
                insertLabelPermission(labelIds, id);
                insertCategoryPermission(categoryIds, id);

                //渠道，产品版不用渠道
//                insertLabelChannel(channelIds, id, con);
            } else {
                //更新用户信息
                userDao.update(user);
                //删除用户和角色的对应关系，普通操作用户和超级管理员的删除方式不同。
                if (isAdminOperate) {
                    userRoleDao.deleteByUserId(id);
                    deleteCustomerPermission(id);
                    deleteLabelPermission(id);
                    deleteCategoryPermission(id);

                    //删除渠道，产品版不需要渠道
//            		deleteLabelChannel(con, id);
                } else {
                    userRoleDao.delete(operateUserId, id);
                    deleteCustomerPermission(operateUserId, id);
                    deleteLabelPermission(operateUserId, id);
                    deleteCategoryPermission(operateUserId, id);

                    //删除渠道
//            		deleteLabelChannel(con, operateUserId, id);
                }
                //插入用户和角色的对应关系
                userRoleDao.insert(userRoles);
                //插入用户与客户对应关系
                insertCustomerPermission(customerIds, id);
                //插入用户与标签对应关系
//              insertLabelPermission(labelIds, id, con);
                //插入用户与品类对应关系
//              insertCategoryPermission(categoryIds, id, con);
                //渠道
//              insertLabelChannel(channelIds, id, con);
            }
        } catch (SQLException e) {
            log.error(e.getStackTrace());
            e.printStackTrace();
            return false;
        } finally {
            JDBCHelper.close(con);
        }
        return true;
    }

    public boolean saveUserV1(UserRoles userRoles, Long operateUserId, boolean isAdminOperate, String customerIds,
                              String labelIds, String categoryIds, String channelIds) throws Exception {
        UserDTO user = userRoles.getUser();
        Long id = user.getId();
        //如果id为null则为新增的保存造作，否则则为更新的保存操作
        if (id == null) {
            id = IDHelper.getID();
            user.setId(id);
            userDao.insert(user);
            userRoleDao.insert(userRoles);
            insertCustomerPermission(customerIds, id);
            //insertLabelPermission(labelIds, id);
            //insertCategoryPermission(categoryIds, id);
           logger.info("dpedls=========="+userRoles.getUser().getDeptId().toString().equals("100000"));


        } else {
            //更新用户信息
            userDao.update(user);
            //删除用户和角色的对应关系，普通操作用户和超级管理员的删除方式不同。
            if (isAdminOperate) {
                userRoleDao.deleteByUserId(id);
                deleteCustomerPermission(id);
                deleteLabelPermission(id);
                deleteCategoryPermission(id);
            } else {
                userRoleDao.delete(operateUserId, id);
                deleteCustomerPermission(operateUserId, id);
                deleteLabelPermission(operateUserId, id);
                deleteCategoryPermission(operateUserId, id);
            }
            //插入用户和角色的对应关系
            userRoleDao.insert(userRoles);
            //插入用户与客户对应关系
            insertCustomerPermission(customerIds, id);



        }
        //新增代理商
        logger.info("Userlsnul=========="+(userRoles==null));
        logger.info("Userlsnul=========="+(userRoles.getUser()==null));
        logger.info("Userlsnul=========="+(userRoles.getUser().getDeptId()==null));

        if (userRoles.getUser().getDeptId().toString().equals("100000")) {
            logger.info("dpedlsnul=========="+(userRoles.getUser().getCustomerRegistDTO()==null));

            //代理商名字          Z乡村vbnm。/
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getName())) {
                userDao.dealUserInfo(id, "customer_name", userRoles.getUser().getCustomerRegistDTO().getName());
            }

            //营业执照注册号
            if (StringUtils.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getBliNumber())) {
                userDao.dealUserInfo(id, "bli_number", userRoles.getUser().getCustomerRegistDTO().getBliNumber());

            }
            //注册地所在省
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getProvince())) {
                userDao.dealUserInfo(id, "province", userRoles.getUser().getCustomerRegistDTO().getProvince());

            }
            //注册地所在市
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getCity())) {
                userDao.dealUserInfo(id, "city", userRoles.getUser().getCustomerRegistDTO().getCity());

            }
            //注册地所在乡镇
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getCountry())) {
                userDao.dealUserInfo(id, "county", userRoles.getUser().getCustomerRegistDTO().getCountry());

            }
            //统一社会信用代码(纳税人识别号)
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getTaxPayerId())) {
                userDao.dealUserInfo(id, "taxpayer_id", userRoles.getUser().getCustomerRegistDTO().getTaxPayerId());
            }
            //营业执照url
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getBliPath())) {
                userDao.dealUserInfo(id, "bli_path", userRoles.getUser().getCustomerRegistDTO().getBliPath());
            }
            //银行
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getBank())) {
                userDao.dealUserInfo(id, "bank", userRoles.getUser().getCustomerRegistDTO().getBank());

            }
            //银行账号
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getBankAccount())) {
                userDao.dealUserInfo(id, "bank_account", userRoles.getUser().getCustomerRegistDTO().getBankAccount());
            }
            //银行开户许可证url
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getBankAccountCertificate())) {
                userDao.dealUserInfo(id, "bank_account_certificate", userRoles.getUser().getCustomerRegistDTO().getBankAccountCertificate());
            }

            //企业注册详细街道地址
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getAddress())) {
                userDao.dealUserInfo(id, "reg_address", userRoles.getUser().getCustomerRegistDTO().getAddress());

            }
            //企业税务登记url
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getTaxpayerCertificatePath())) {
                userDao.dealUserInfo(id, "taxpayerCertificatePath", userRoles.getUser().getCustomerRegistDTO().getTaxpayerCertificatePath());

            }

            //联系人手机（联系人姓名使用用户姓名）
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getMobile())) {
                userDao.dealUserInfo(id, "mobile", userRoles.getUser().getCustomerRegistDTO().getMobile());

            }

            //联系人邮箱
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getEmail())) {
                userDao.dealUserInfo(id, "email", userRoles.getUser().getCustomerRegistDTO().getEmail());

            }

            //联系人职位
            if (StringUtil.isNotEmpty(userRoles.getUser().getCustomerRegistDTO().getTitle())) {
                userDao.dealUserInfo(id, "title", userRoles.getUser().getCustomerRegistDTO().getTitle());

            }
        }
        return true;
    }

    //用户与客户  数据权限关系
    private void insertCustomerPermission(String customerIds, Long userId) throws SQLException {
    }

    //用户与标签分类  数据权限关系 09-22
    //TODO 速度慢
    private void insertLabelPermission(String labelIds, Long userId) throws SQLException {
        List<Long> lIds = new ArrayList<Long>();
        String showDataPermisson = ConfigPropertiesHolder.getConf("show.data.permission").toString().toUpperCase();

        if (!StringUtils.isEmpty(labelIds) && userId != null && "TRUE".equals(showDataPermisson)) {
            lIds = getLabelIds(labelIds);
        }
        if ("FALSE".equals(showDataPermisson)) {
            List list = this.userDao.getSQLQuery("select id from label_info").list();
            for (int i = 0; i < list.size(); i++) {
                lIds.add(Long.parseLong(String.valueOf(list.get(i))));
            }
        }
        if (lIds.size() > 0) {
            for (int i = 0; i < lIds.size(); i++) {
                Long labelId = lIds.get(i);
                //this.userDao.executeUpdateSQL("insert into t_user_label_rel(USER_ID,LABEL_ID,OPT_TIME) values(" + userId + "," + labelId + ", now())");
            }
        }
    }

    private List<Long> getLabelIds(String labelIds) throws SQLException {
        List<Long> ids = new ArrayList<Long>();
        StringBuilder sql = new StringBuilder("select id from label_info where uri like ");
        String[] lIds = labelIds.split(",");
        for (int i = 0; i < lIds.length; i++) {
            ids.add(new Long(lIds[i]));
            sql.append("'/").append(lIds[i]).append("/%'");
            if (i < lIds.length - 1) {
                sql.append(" or ");
            }
        }
        List list = this.userDao.getSQLQuery(sql.toString()).list();
        for (int i = 0; i < list.size(); i++) {
            ids.add(Long.parseLong(String.valueOf(list.get(i))));
        }
        return ids;
    }

    //用户与品类(电商与媒体) 数据权限关系09-22
    private void insertCategoryPermission(String categoryIds, Long userId) throws SQLException {
        List<Long> cIds = new ArrayList<Long>();
        String showDataPermisson = ConfigPropertiesHolder.getConf("show.data.permission").toString().toUpperCase();

        if (!StringUtils.isEmpty(categoryIds) && userId != null && "TRUE".equals(showDataPermisson)) {
            cIds = getCategoryIds(categoryIds);
        }
        if ("FALSE".equals(showDataPermisson)) {
            List list = this.userDao.getSQLQuery("select id from label_category").list();
            for (int i = 0; i < list.size(); i++) {
                cIds.add(Long.parseLong(String.valueOf(list.get(i))));
            }
        }
        if (cIds.size() > 0) {
            for (int i = 0; i < cIds.size(); i++) {
                Long categoryId = cIds.get(i);
                //this.userDao.executeUpdateSQL("insert into t_user_category_rel(USER_ID, CATEGORY_ID, OPT_TIME) values(" + userId + "," + categoryId + ", now())");
            }
        }
    }

    private List<Long> getCategoryIds(String categoryIds) throws SQLException {
        List<Long> ret = new ArrayList<Long>();
        String[] cIds = categoryIds.split(",");
        String sql = "select id from label_category where id in (?) and level=2";
        StringBuilder sql1 = new StringBuilder("select id from label_category where uri like ");
        List list = this.userDao.sqlQuery(sql, categoryIds);
        for (int i = 0; i < list.size(); i++) {

            sql1.append("'%/").append(list.get(i)).append("/%'");
            if (i != list.size() - 1) {
                sql1.append(" or uri like ");
            }
        }
        List list_2 = this.userDao.getSQLQuery(sql1.toString()).list();
        for (int i = 0; i < list_2.size(); i++) {
            ret.add(Long.parseLong(String.valueOf(list_2.get(i))));
        }
        for (String id : cIds) {
            ret.add(new Long(id));
        }
        return ret;
    }

    private void deleteCustomerPermission(Long id) throws SQLException {
    }

    private void deleteLabelPermission(Long id) throws SQLException {
       /* String sql = "delete from t_user_label_rel where USER_ID = " + id;
        this.userDao.executeUpdateSQL(sql);*/
    }

    private void deleteCategoryPermission(Long id) throws SQLException {
       /* String sql = "delete from t_user_category_rel where USER_ID = " + id;
        this.userDao.executeUpdateSQL(sql);*/
    }

    private void deleteCustomerPermission(Long operateUserId, Long userId) throws SQLException {
    }

    private void deleteLabelPermission(Long operateUserId, Long userId) throws SQLException {
        /*StringBuilder builder = new StringBuilder();
        builder.append(" delete from t_user_label_rel where USER_ID = '" + userId + "' and LABEL_ID in (select temp.id from");
        builder.append(" (select r.id from label_info r inner join t_user_label_rel ur on ur.LABEL_ID = r.ID and ur.id = '" + operateUserId + "')temp)");
        String sql = builder.toString();
        this.userDao.executeUpdateSQL(sql);*/
    }

    private void deleteCategoryPermission(Long operateUserId, Long userId) throws SQLException {
        /*StringBuilder builder = new StringBuilder();
        builder.append(" delete from t_user_category_rel where USER_ID = '" + userId + "' and CATEGORY_ID in (select temp.id from");
        builder.append(" (select r.id from label_category r inner join t_user_category_rel ur on ur.CATEGORY_ID = r.ID and ur.id = '" + operateUserId + "')temp)");
        String sql = builder.toString();
        this.userDao.executeUpdateSQL(sql);*/
    }

    public boolean updateDataPermission(Long id, Long operateUserId, boolean isAdminOperate, String customerIds, String labelIds,
                                        String categoryIds) {
        try {
            //删除用户和角色的对应关系，普通操作用户和超级管理员的删除方式不同。
            if (isAdminOperate) {
                deleteCustomerPermission(id);
                deleteLabelPermission(id);
                deleteCategoryPermission(id);
            } else {
                deleteCustomerPermission(operateUserId, id);
                deleteLabelPermission(operateUserId, id);
                deleteCategoryPermission(operateUserId, id);
            }
            //插入用户与客户对应关系
            insertCustomerPermission(customerIds, id);
            //插入用户与标签对应关系
            insertLabelPermission(labelIds, id);
            //插入用户与品类对应关系
            insertCategoryPermission(categoryIds, id);
        } catch (SQLException e) {
            log.error(e.getStackTrace());
            e.printStackTrace();
            return false;
        } finally {
        }
        return true;
    }

    public List<Map<String, Object>> queryLabelChannel(Long userId) {
        String sql = "";
        List<Object> params = new ArrayList<>();
        if (null == userId) {
            sql = "select * from label_channel";
        } else {
            sql = "select t.*,t1.USER_ID from label_channel t left join t_user_channel_rel t1 on t.id = t1.CHANNEL_ID " +
                    "and t1.USER_ID=?";
            params.add(userId);
        }
        List<Map<String, Object>> lst = new ArrayList<Map<String, Object>>();
        try {
            List<Map<String, Object>> drs = this.userDao.sqlQuery(sql);
            for (int i = 0; i < drs.size(); i++) {
                Map dr = drs.get(i);
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("id", dr.get("id"));
                map.put("cidEn", dr.get("cid_en"));
                map.put("cidCn", dr.get("cid_cn"));
                map.put("code", dr.get("code"));
                if (null != userId) {
                    Long uId = Long.parseLong(String.valueOf(dr.get("USER_ID")));
                    if (null != uId && 0 != uId) {
                        map.put("checked", true);
                    } else {
                        map.put("checked", false);
                    }
                }
                lst.add(map);
            }
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getStackTrace());
        } finally {
        }
        return lst;
    }


    public boolean deleteByAudName(Long userId) {
        try {
            UserDTO user = queryUserById(userId);
            this.userDao.executeUpdateSQL("delete from COM_AUDIT_TRAIL where AUD_USER='[username: ?]'", user.getName());
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            log.error(e.getStackTrace());
        } finally {
        }
        return false;
    }


    public User getUserByUserName(String userName) {
        User u = userDao.findUniqueBy("name", userName);
        return u;
    }


    public Map<String, Object> getUsersByCondition(UserQueryParam param) {
        Map<String, Object> map = new HashMap<>();
        StringBuilder sql = new StringBuilder("" +
                "SELECT t1.`NAME` AS userName t1.id AS userId, t1.mobile_num AS mobileNum, t1.enterprise_name AS enterpriseName," +
                " t1.create_time AS createTime, t1.source AS source, t1.`STATUS` AS status FROM t_user t1 where 1=1 " +
                "and t1.user_type=1 ");
        if (StringUtil.isNotEmpty(param.getUserName())) {
            sql.append(" and t1.name='").append(StringEscapeUtils.escapeSql(param.getUserName())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getUserId())) {
            sql.append(" and t1.id='").append(StringEscapeUtils.escapeSql(param.getUserId())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            sql.append(" and t1.enterprise_name='").append(StringEscapeUtils.escapeSql(param.getEnterpriseName())).append("'");
        }
        map.put("total", userDao.getSQLQuery(sql.toString()).list().size());
        map.put("customerList", userDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list());
        return map;
    }

    public Map<String, Object> getUserByCondition(int type, String condition) throws TouchException {
        Map<String, Object> retMap = new HashMap<>();
        User userDO = new User();
        if (type <= 0) {
            throw new TouchException("20003", "type输入错误");
        }
        if (1 == type) {
            userDO = userDao.findUniqueBy("name", condition);
        }
        if (2 == type) {
            userDO = userDao.findUniqueBy("mobileNum", condition);
        }
        if (3 == type) {
            userDO = userDao.findUniqueBy("email", condition);
        }
        if (null == userDO) {
            throw new TouchException("20004", "查询用户失败");
        }
        retMap.put("mobileNum", userDO.getMobileNum());
        retMap.put("email", userDO.getEmail());
        return retMap;
    }

    public void resetPwd(int type, String condition, String password, int pwdLevel) throws Exception {
        User userDO = new User();
        if (1 == type) {
            userDO = userDao.findUniqueBy("mobileNum", condition);
        }
        if (2 == type) {
            userDO = userDao.findUniqueBy("email", condition);
        }
        if (null == userDO) {
            throw new TouchException("20002", "用户查询失败");
        }
        userDO.setPassword(CipherUtil.generatePassword(password));
        userDO.setUserPwdLevel(pwdLevel);
        userDao.save(userDO);
    }

    public void updatePwd(long uid, String oldPwd, String newPwd, int pwdLevel) throws Exception {

        User userDO = new User();
        try {
            userDO = userDao.findUniqueBy("id", uid);
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
        userDao.save(userDO);
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
                "WHERE t1.id=" + id);
        return userDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
    }

    public void verifyIdentifyValueUniqueness(int type, String value) throws Exception {
        List<User> list = new ArrayList<>();
        if (1 == type) {
            list = userDao.findBy("name", value);
        }
        if (2 == type) {
            list = userDao.findBy("mobileNum", value);
        }
        if (3 == type) {
            list = userDao.findBy("email", value);
        }
        if (list.size() >= 1) {
            throw new TouchException("20008", "已经被注册，请重新输入");
        }
    }

    public void updateRegistInfo(int type, String oldValue, String newValue) throws Exception {

        User userDO = new User();
        //mobile
        if (1 == type) {
            userDO = userDao.findUniqueBy("mobileNum", oldValue);
        }
        if (2 == type) {
            userDO = userDao.findUniqueBy("email", oldValue);
        }
        if (null == userDO) {
            throw new TouchException("20004", "系统异常，用户查询失败");
        }
        if (1 == type) {
            userDO.setMobileNum(newValue);
        }
        if (2 == type) {
            userDO.setEmail(newValue);
        }
        userDao.save(userDO);
    }

    public void changeUserStatus(String userId, String action) throws Exception {
        User userDO = userDao.findUniqueBy("id", Long.valueOf(userId));
        //解冻
        if ("1".equals(action)) {
            userDO.setStatus(0);
        }
        //冻结
        if ("2".equals(action)) {
            userDO.setStatus(1);
        }
        try {
            userDao.save(userDO);
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
        List list = userDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
        map.put("total", list.size());
        map.put("userIndustryPoolList", list);
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
        StringBuilder sql = new StringBuilder("SELECT\n" +
                "\tt1.NAME AS industryPoolName,\n" +
                "\tt1.industry_pool_id AS industryPoolId,\n" +
                "\tt2.create_time AS createTime,\n" +
                "\tt2.operator AS operator\n" +
                "FROM\n" +
                "\tt_industry_pool t1\n" +
                "LEFT JOIN t_cust_industry t2 ON t1.industry_pool_id = t2.industry_pool_id where 1=1 ");
        sql.append(" and t2.cust_id='").append(StringEscapeUtils.escapeSql(customerId)).append("'");
        List list = userDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
        map.put("total", list.size());
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
        List list = userDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).setFirstResult(pageNum).setMaxResults(pageSize).list();
        map.put("total", list.size());
        map.put("industryPoolStatusList", list);
        return map;
    }

    public User getUserByName(String name) {
        return userDao.findUniqueBy("name", name);
    }

    /**
     * 查询运营后台用户
     *
     * @param loginName
     * @param status
     * @return
     */
    public User getUserByLoginName(String loginName, int status) {
        List<User> list = userDao.find(" FROM User m WHERE m.name = ? AND m.status = ? ", loginName, status);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public User getUserBymobileNum(String mobileNum) {
        return userDao.findUniqueBy("mobileNum", mobileNum);
    }

    public User getUserByEmail(String email) {
        return userDao.findUniqueBy("email", email);
    }


    public Integer getUserByLoginPassWord(String payPassWord, Long userId) {

        Integer code = null;
        List list = userDao.sqlQuery("SELECT PASSWORD FROM t_user t WHERE t.id =?", userId);
        String loginPassWord = null;
        if (list.size() > 0) {
            loginPassWord = String.valueOf(list.get(0));
        }

        if (CipherUtil.generatePassword(payPassWord).equals(loginPassWord)) {
            code = 1;
        } else {
            code = 0;
        }

        return code;
    }

    public User getUserByEnterpriseName(String enterpriseName) {
        return userDao.findUniqueBy("enterpriseName", enterpriseName);
    }

    /**
     * 获取用户角色
     */
    public List<String> getAuthsByUserName(String userName) throws Exception {
        StringBuilder sql = new StringBuilder("SELECT t1.NAME FROM t_role t1 LEFT JOIN t_user_role_rel t2 ON t1.id = t2.role" +
                " LEFT JOIN t_user t3 ON t2.id = t3.id WHERE t3.`name` = '" + userName + "'");

        return userDao.getSQLQuery(sql.toString()).list();
    }

    /**
     * 校验用户属性表中是否重复
     *
     * @param propertyName  属性名
     * @param propertyValue 属性值
     * @return
     */
    public UserProperty checkProperty(String propertyName, String propertyValue) {

        return userDao.checkProperty(propertyName, propertyValue);
    }


    public Page getCustomList(PageParam pageParam, LoginUser loginUser, UserDTO userDTO) {

        Long loginId = loginUser.getId();
        boolean ifAdmin = loginUser.isAdmin();

        StringBuilder sql = new StringBuilder();
        List<Object> params=new ArrayList<>();
        sql.append("select tu.name,tu.id,tp.property_value agentName,\n" +
                " (select count(1) from t_customer_property t where t.property_name='agent_id' and t.property_value=tu.id) customerCount" +
//                " IFNULL((select sum(((case when stm.amount> stm.prod_amount then (stm.amount/1000-stm.prod_amount/1000) else 0 end )*((select tcp.property_value from t_customer_property tcp where tcp.cust_id=tp.cust_id and tcp.property_name='commission_rate'\n" +
//                "              )/100))) from stat_bill_month stm,t_customer_property tp where stm.cust_id=tp.cust_id\n" +
//                "              and tp.property_name='agent_id' and (stm.bill_type='3' or stm.bill_type='7' or stm.bill_type='4') and tp.property_value=tp.user_id),0) accountCount\n" +
                "   from t_user tu,t_user_property tp where tp.user_id=tu.id and tp.property_name='customer_name'");

        //admin可以查询所有部门信息  普通用户只能查本部门的
        if (!ifAdmin) {
            sql.append(" and  tu.DEPTID ='100000' and tu.id=?");
            params.add(loginId);
        }

        if(userDTO!=null&&StringUtils.isNotEmpty(userDTO.getUserName())){
            sql.append("and tu.name=? ");
            params.add(userDTO.getUserName());
        }

        if(userDTO!=null&&(userDTO.getId()!=null)){
            sql.append("and tu.id=? ");
            params.add(userDTO.getId());
        }

        if(userDTO!=null&&(StringUtils.isNotEmpty(userDTO.getCustomerName()))){
            sql.append("and tp.property_value like ? ");
            params.add("%"+userDTO.getCustomerName()+"%");
        }
        sql.append("   order by tp.create_time desc  ");
      Page page=userDao.sqlPageQuery(sql.toString(), pageParam.getPageNum(), pageParam.getPageSize(), params.toArray());


        List<HashMap<String,Object>> data = page.getData();
        for(HashMap<String,Object> map:data){
            String id=map.get("id").toString();



            List cs=new ArrayList();
            String csql="select  tp.cust_id cusId from t_customer_property tp where tp.property_name='agent_id'  and tp.property_value=?";
            cs.add(id);


            List<Map<String, Object>> maps = userDao.queryMapsListBySql(csql, cs.toArray());
            Double accountCount=0.000;
            for(Map<String, Object> map1:maps) {
                List list=new ArrayList();
                StringBuilder accot=new StringBuilder();
               String cuId=map1.get("cusId").toString();
                accot.append(" select round(((case when stm.amount > stm.prod_amount then (stm.amount-stm.prod_amount) else 0 end )*((select tcp.property_value from t_customer_property tcp where tcp.cust_id=? and tcp.property_name='commission_rate' " +
                        " )/100)),3) accountCount  from stat_bill_month stm  where stm.cust_id=? " +
                        "  and (stm.bill_type='4') ");
                list.add(cuId);
                list.add(cuId);
                logger.info("sqlaccot==="+accot.toString());
                Map<String, Object> datagObjectMap = userDao.queryUniqueSql(accot.toString(), list.toArray());
                accot = new StringBuilder();
                accot.append(" select round(((case when stm.amount> stm.prod_amount then (stm.amount-stm.prod_amount) else 0 end )*((select tcp.property_value from t_customer_property tcp where tcp.cust_id=? and tcp.property_name='commission_rate' " +
                        " )/100)),3) accountCount  from stat_bill_month stm,t_customer_property tp where stm.cust_id=? " +
                        " and (stm.bill_type='3')  ");


                Map<String, Object> callObjectMap = userDao.queryUniqueSql(accot.toString(), list.toArray());

                accot = new StringBuilder();
                accot.append(" select round(((case when stm.amount> stm.prod_amount then (stm.amount-stm.prod_amount) else 0 end )*((select tcp.property_value from t_customer_property tcp where tcp.cust_id=? and tcp.property_name='commission_rate' " +
                        " )/100)),3) accountCount  from stat_bill_month stm,t_customer_property tp where stm.cust_id=? " +
                        "  and (stm.bill_type='7') ");


                Map<String, Object> messageObjectMap = userDao.queryUniqueSql(accot.toString(), list.toArray());
                Double acc=(datagObjectMap==null|| (Double)datagObjectMap.get("accountCount")==null)?0:(Double) datagObjectMap.get("accountCount");
                Double accCall=(callObjectMap==null||(Double) callObjectMap.get("accountCount")==null)?0:(Double) callObjectMap.get("accountCount");
                Double accmess=(messageObjectMap==null||(Double) messageObjectMap.get("accountCount")==null)?0:(Double) messageObjectMap.get("accountCount");
                 accountCount+= (acc+accCall+accmess);
            }
            map.put("accountCount",accountCount.toString());

        }


        return page;

    }



    public HashMap<String,Object> getYjByMonth(PageParam pageParam,AgentDTO agentDTO){
        HashMap<String,Object> map=new HashMap<>();
        StringBuilder sql=new StringBuilder();
        List<Object> params=new ArrayList<>();

        StringBuilder sqlu=new StringBuilder();
        List<Object> paramsu=new ArrayList<>();

        sqlu.append("select tp.property_value agentName from  t_user_property tp where tp.user_id=? and  tp.property_name='customer_name'  ");





        paramsu.add(agentDTO.getUserId());


        Map<String, Object> stringObjectMap = userDao.queryUniqueSql(sqlu.toString(), paramsu.toArray());
        String userId = agentDTO.getUserId();




        List cs=new ArrayList();
        String csql="select  tp.cust_id cusId from t_customer_property tp where tp.property_name='agent_id'  and tp.property_value=?";
        cs.add(userId);


        List<Map<String, Object>> maps = userDao.queryMapsListBySql(csql, cs.toArray());
        Double accountCount=0.000;
        for(Map<String, Object> map1:maps) {
            List list=new ArrayList();
            StringBuilder accot=new StringBuilder();
            String cuId=map1.get("cusId").toString();
            accot.append(" select round(((case when stm.amount> stm.prod_amount then (stm.amount-stm.prod_amount) else 0 end )*((select tcp.property_value from t_customer_property tcp where tcp.cust_id=? and tcp.property_name='commission_rate' " +
                    " )/100)),3) accountCount  from stat_bill_month stm  where stm.cust_id=? " +
                    "  and (stm.bill_type='4') and stm.stat_time=? ");
            list.add(cuId);
            list.add(cuId);

            list.add(agentDTO.getYearMonth());
            if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustId())) {
                accot.append("and tc.cust_id=?");
                list.add(agentDTO.getCustId());
            }


            if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustomName())) {
                accot.append("and tc.enterprise_name like ?");
                list.add("%"+agentDTO.getCustomName()+"%");
            }

            Map<String, Object> datagObjectMap = userDao.queryUniqueSql(accot.toString(), list.toArray());
            accot = new StringBuilder();
            accot.append(" select round(((case when stm.amount> stm.prod_amount then (stm.amount-stm.prod_amount) else 0 end )*((select tcp.property_value from t_customer_property tcp where tcp.cust_id=? and tcp.property_name='commission_rate' " +
                    " )/100)),3) accountCount  from stat_bill_month stm,t_customer_property tp where stm.cust_id=? " +
                    " and (stm.bill_type='3')  and stm.stat_time=? ");
            if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustId())) {
                accot.append("and tc.cust_id=?");

            }


            if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustomName())) {
                accot.append("and tc.enterprise_name like ?");

            }


            Map<String, Object> callObjectMap = userDao.queryUniqueSql(accot.toString(), list.toArray());

            accot = new StringBuilder();
            accot.append(" select round(((case when stm.amount> stm.prod_amount then (stm.amount-stm.prod_amount) else 0 end )*((select tcp.property_value from t_customer_property tcp where tcp.cust_id=? and tcp.property_name='commission_rate' " +
                    " )/100)),3) accountCount  from stat_bill_month stm,t_customer_property tp where stm.cust_id=? " +
                    "  and (stm.bill_type='7') and stm.stat_time=? ");

            if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustId())) {
                accot.append("and tc.cust_id=?");

            }


            if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustomName())) {
                accot.append("and tc.enterprise_name like ?");

            }


            Map<String, Object> messageObjectMap = userDao.queryUniqueSql(accot.toString(), list.toArray());

            Double acc=(datagObjectMap==null|| (Double)datagObjectMap.get("accountCount")==null)?0:(Double) datagObjectMap.get("accountCount");
            Double accCall=(callObjectMap==null||(Double) callObjectMap.get("accountCount")==null)?0:(Double) callObjectMap.get("accountCount");
            Double accmess=(messageObjectMap==null||(Double) messageObjectMap.get("accountCount")==null)?0:(Double) messageObjectMap.get("accountCount");
            accountCount+= (acc+accCall+accmess);
        }
        stringObjectMap.put("account",accountCount.toString());



        sql.append("select\n" +
                " tu.account customAcocunt,tc.enterprise_name customName,? statTime," +
                "  (select tp.property_value from t_customer_property tp where tp.cust_id=tc.cust_id and tp.property_name='commission_rate')commision,"+
                "  round(IFNULL((select (case when sbm2.amount> sbm2.prod_amount then (sum(sbm2.amount)-sum(sbm2.prod_amount)) else 0 end)*((select tp.property_value from t_customer_property tp where tp.cust_id=tc.cust_id and tp.property_name='commission_rate'\n" +
                "  )/100) from stat_bill_month sbm2 where sbm2.cust_id=tc.cust_id and sbm2.stat_time=? and\n" +
                " sbm2.bill_type='7'),0),3) dataAmcount,\n" +

                "  round(IFNULL((select (case when sbm2.amount> sbm2.prod_amount then (sum(sbm2.amount)-sum(sbm2.prod_amount)) else 0 end)*((select tp.property_value from t_customer_property tp where tp.cust_id=tc.cust_id and tp.property_name='commission_rate'\n" +
                "\t)/100) from stat_bill_month sbm2 where sbm2.cust_id=tc.cust_id and sbm2.stat_time=? and\n" +
                " sbm2.bill_type='4'),0),3) callAmcount,\n" +
                "\t round(IFNULL((select(case when sbm2.amount> sbm2.prod_amount then (sum(sbm2.amount)-sum(sbm2.prod_amount)) else 0 end)*((select tp.property_value from t_customer_property tp where tp.cust_id=tc.cust_id and tp.property_name='commission_rate'\n" +
                "\t)/100) from stat_bill_month sbm2 where sbm2.cust_id=tc.cust_id and sbm2.stat_time=? and\n" +
                " sbm2.bill_type='3'),0),3) messageAmcount\n" +
                "from t_customer tc,t_customer_user tu,t_customer_property tcu  ");
        params.add(agentDTO.getYearMonth());
        params.add(agentDTO.getYearMonth());
        params.add(agentDTO.getYearMonth());
        params.add(agentDTO.getYearMonth());


        sql.append("  where\n" +
                "\ttc.cust_id=tu.cust_id and  tcu.cust_id=tc.cust_id\n" +
                "\tand tu.user_type='1'\n" +
                "\tand tcu.property_name='agent_id' and tcu.property_value=?  \n");
                  params.add(agentDTO.getUserId());
                if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustId())) {
                    sql.append("and tc.cust_id=?");
                    params.add(agentDTO.getCustId());
                }



        if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustomName())) {
            sql.append("and tc.enterprise_name like ?");
            params.add("%"+agentDTO.getCustomName()+"%");
        }
        logger.info("yjlb"+sql.toString());
        Page page = userDao.sqlPageQuery(sql.toString(), pageParam.getPageNum(), pageParam.getPageSize(), params.toArray());
        map.put("total", page.getTotal());
        map.put("count", stringObjectMap);
        map.put("list", page.getData());

       return map ;
    }

    public Map getYjCount(String userId){
        StringBuilder sql=new StringBuilder();
        List<Object> params=new ArrayList<>();

        sql.append("select IFNULL(((sum(IFNULL(stm.amount,0)/1000)-sum(IFNULL(stm.prod_amount,0)/1000))*((select tp.property_value from t_customer_property tp where tp.cust_id=tc.cust_id and tp.property_name='commission_rate'\n" +
                ")/100)),0) accout,tp.property_value agentName\n" +
                " from t_customer_property tcp,t_user_property tp,t_customer tc left join stat_bill_month stm on stm.cust_id=tc.cust_id where " +
                " tcp.cust_id=tc.cust_id and tp.user_id=tcp.property_value and tp.property_name='customer_name' and tcp.property_name='agent_id' and tcp.property_value=?");
        params.add(userId);

        Map<String, Object> stringObjectMap = userDao.queryUniqueSql(sql.toString(), params);
     return stringObjectMap;
    }

    public void exportYj(AgentDTO agentDTO,HttpServletResponse response) throws  Exception{

        StringBuilder sql=new StringBuilder();
        List<Object> params=new ArrayList<>();
        ServletOutputStream outputStream=null;
        try {
             outputStream = response.getOutputStream();
            sql.append("select\n" +
                "\ttu.account customAcocunt,tc.enterprise_name customName,? statTime,\n" +
                "(select tp.property_value from t_customer_property tp where tp.cust_id=tc.cust_id and tp.property_name='commission_rate')commision,"+
                "  round(IFNULL((select (case when sbm2.amount> sbm2.prod_amount then (sum(sbm2.amount)-sum(sbm2.prod_amount)) else 0 end)*IFNULL((select tp.property_value from t_customer_property tp where tp.cust_id=tc.cust_id and tp.property_name='commission_rate'\n" +
                "\t),0)/100 from stat_bill_month sbm2 where sbm2.cust_id=tc.cust_id and sbm2.stat_time=? and\n" +
                "\t sbm2.bill_type='7'),0),3) dataAmcount,\n" +

                "\t round(IFNULL((select (case when sbm2.amount> sbm2.prod_amount then (sum(sbm2.amount)-sum(sbm2.prod_amount)) else 0 end)*IFNULL((select tp.property_value from t_customer_property tp where tp.cust_id=tc.cust_id and tp.property_name='commission_rate'\n" +
                "\t),0)/100 from stat_bill_month sbm2 where sbm2.cust_id=tc.cust_id and sbm2.stat_time=? and\n" +
                " sbm2.bill_type='4'),0),3) callAmcount,\n" +
                "\t  round(IFNULL((select (case when sbm2.amount> sbm2.prod_amount then (sum(sbm2.amount)-sum(sbm2.prod_amount)) else 0 end)*IFNULL((select tp.property_value from t_customer_property tp where tp.cust_id=tc.cust_id and tp.property_name='commission_rate'\n" +
                "\t),0)/100 from stat_bill_month sbm2 where sbm2.cust_id=tc.cust_id and sbm2.stat_time=? and\n" +
                " sbm2.bill_type='3'),0),3) messageAmcount\n" +
                "from t_customer tc,t_customer_user tu,t_customer_property tcu  ");
            params.add(agentDTO.getYearMonth());
            params.add(agentDTO.getYearMonth());
            params.add(agentDTO.getYearMonth());
            params.add(agentDTO.getYearMonth());


            sql.append(" where\n" +
                "\ttc.cust_id=tu.cust_id and  tcu.cust_id=tc.cust_id \n" +
                "\tand tu.user_type='1'\n" +
                "\tand tcu.property_name='agent_id' and tcu.property_value=? \n");
        params.add(agentDTO.getUserId());
        EasyExcelUtil.EasyExcelParams param = new EasyExcelUtil.EasyExcelParams();

        if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustId())) {
            sql.append("and tc.cust_id=?");
            params.add(agentDTO.getCustId());
        }



        if(agentDTO!=null&&StringUtils.isNotEmpty(agentDTO.getCustomName())) {
            sql.append("and tc.enterprise_name like ?");
            params.add("%"+agentDTO.getCustomName()+"%");
        }




            XSSFWorkbook workBook =new XSSFWorkbook();
            XSSFSheet sheet = workBook.createSheet();
            int r=0;
            XSSFRow row = sheet.createRow(r++);
            int c=0;
            XSSFCell cell = row.createCell(c++);
            cell.setCellValue("企业账号");
            cell = row.createCell(c++);
            cell.setCellValue("企业名称");
            cell = row.createCell(c++);
            cell.setCellValue("账期");
            cell = row.createCell(c++);
            cell.setCellValue("佣金率");
            cell = row.createCell(c++);
            cell.setCellValue("数据佣金金额");
            cell = row.createCell(c++);
            cell.setCellValue("线路佣金金额");
            cell = row.createCell(c++);
            cell.setCellValue("短信佣金金额");
            cell = row.createCell(c++);
            cell.setCellValue("总佣金金额");

            List<Map<String, Object>> maps = userDao.queryMapsListBySql(sql.toString(), params.toArray());

            for(Map map:maps){
                c=0;
               row=sheet.createRow(r++);
                cell = row.createCell(c++);
                cell.setCellValue((String) map.get("customAcocunt"));
                cell = row.createCell(c++);
                cell.setCellValue((String) map.get("customName"));
                cell = row.createCell(c++);
                cell.setCellValue((String) map.get("statTime"));
                cell = row.createCell(c++);
                cell.setCellValue((String) map.get("commision"));
                cell = row.createCell(c++);
                Double ad=(Double)(map.get("dataAmcount")==null?0:map.get("dataAmcount"));
                cell.setCellValue((ad).toString());
                cell = row.createCell(c++);
                Double call=(Double)(map.get("callAmcount")==null?0:map.get("callAmcount"));

                cell.setCellValue((call).toString());
                cell = row.createCell(c++);
                Double messageAmcount=(Double)(map.get("messageAmcount")==null?0:map.get("messageAmcount"));

                cell.setCellValue((messageAmcount).toString());
                cell = row.createCell(c++);
                cell.setCellValue(((call+messageAmcount+ad))+"");
            }

            workBook.write(outputStream);
        } catch (Exception e) {
            log.error("导出佣金异常", e);
        }finally {

            outputStream.close();
        }
    }
}
