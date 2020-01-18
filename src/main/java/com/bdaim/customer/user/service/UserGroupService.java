package com.bdaim.customer.user.service;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dao.UserGroupDao;
import com.bdaim.customer.dto.*;
import com.bdaim.customer.entity.CommonInfoDO;
import com.bdaim.customer.entity.CommonInfoProperty;
import com.bdaim.customer.entity.CustomerUserGroup;
import com.bdaim.customer.entity.CustomerUserGroupRel;
import com.bdaim.marketproject.dao.MarketProjectDao;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.rbac.dao.RoleDao;
import com.bdaim.rbac.dao.RoleResourceDao;
import com.bdaim.rbac.dto.RoleDataPermissonDTO;
import com.bdaim.util.DateUtil;
import com.bdaim.util.IDHelper;
import com.bdaim.util.LogUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/27
 * @description
 */
@Service("userGroupService")
public class UserGroupService {

    private final static Logger LOG = LoggerFactory.getLogger(UserGroupService.class);

    private final static String GROUP_LEADER_PROPERTY_KEY = "group_leader";

    @Resource
    private CustomerUserDao customerUserDao;

    @Resource
    private CustomerDao customerDao;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private UserGroupDao userGroupDao;

    @Resource
    private RoleResourceDao roleResourceDao;

    @Resource
    private RoleDao roleDao;

    @Resource
    private MarketProjectDao marketProjectDao;


    /**
     * @param userGroupEntity
     * @param userIds           所有用户ID
     * @param groupLeaderUserId 组长ID
     * @return
     */
    public int addUserGroup(CustomerUserGroup userGroupEntity, List<String> userIds, String groupLeaderUserId) {
        try {
            userGroupEntity.setId(String.valueOf(IDHelper.getID()));
            userGroupEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
            userGroupEntity.setStatus(1);
            if ("1".equals(userGroupEntity.getLeavel().toString())) {
                // 设置组长
                userGroupEntity.setGroupLeaderId(groupLeaderUserId);
                // 设置用户群组员工数量
                userGroupEntity.setUserCount(userIds.size() + 1);
                // 给用户组分配组员
                //distributionGroupUser(userGroupEntity.getId(), userIds);
                addGroupRel(userGroupEntity.getId(), userIds, groupLeaderUserId);
                // 添加组长
                addGroupLeader(userGroupEntity.getId(), groupLeaderUserId);
            }
            customerUserDao.saveOrUpdate(userGroupEntity);
        } catch (Exception e) {
            LOG.error("添加用户群组失败,", e);
            return 0;
        }
        return 1;
    }

    /**
     * 设置组长
     *
     * @param groupId
     * @param groupLeaderUserId
     * @return
     * @throws TouchException
     */
    public int addGroupLeader(String groupId, String groupLeaderUserId) throws TouchException {
        try {
            if (StringUtil.isEmpty(groupLeaderUserId)) {
                throw new TouchException("groupLeaderUserId为空");
            }
            if (StringUtil.isEmpty(groupId)) {
                throw new TouchException("groupId为空");
            }
            // 查询原来的组长信息
            CustomerUserGroupRel userGroupLeader = customerUserDao.getCustomerUserGroupLeader(groupId);
            if (userGroupLeader != null) {
                // 更新原来的组长为普通坐席
                userGroupLeader.setType(2);
                customerUserDao.saveOrUpdate(userGroupLeader);
            }

            CustomerUserGroupRel userGroupRelEntity = customerUserDao.getCustomerUserGroupRel(groupId, groupLeaderUserId);
            if (userGroupRelEntity != null) {
                userGroupRelEntity.setType(1);
            } else {
                // 不存在则创建组长
                userGroupRelEntity = new CustomerUserGroupRel();
                userGroupRelEntity.setGroupId(groupId);
                userGroupRelEntity.setUserId(groupLeaderUserId);
                userGroupRelEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
                userGroupRelEntity.setType(1);
                userGroupRelEntity.setStatus(1);
            }
            customerUserDao.saveOrUpdate(userGroupRelEntity);
            // 更新主表组长
            CustomerUserGroup customerUserGroup = customerUserDao.getCustomerUserGroup(groupId);
            if (customerUserGroup != null) {
                customerUserGroup.setGroupLeaderId(groupLeaderUserId);
                customerUserDao.saveOrUpdate(customerUserGroup);
                return 1;
            }
        } catch (TouchException e) {
            LOG.error("添加群组组长失败,", e);
            return 0;
        }
        return 0;
    }


    /**
     * 更新用户群组
     *
     * @param userGroupEntity
     * @return int
     * @author chengning@salescomm.net
     * @date 2018/9/27 14:14
     */
    public int updateUserGroup(CustomerUserGroup userGroupEntity) {
        try {
            return customerUserDao.updateCustomerUserGroup(userGroupEntity);
        } catch (Exception e) {
            LOG.error("更新用户群组失败,", e);
            return 0;
        }
    }

    /**
     * 删除用户群组
     *
     * @param userGroupEntity
     * @return int
     * @author chengning@salescomm.net
     * @date 2018/9/27 14:12
     */
    public int deleteUserGroup(CustomerUserGroup userGroupEntity) {
        try {
            return customerUserDao.deleteCustomerUserGroup(userGroupEntity);
        } catch (Exception e) {
            LOG.error("删除用户群组失败,", e);
            return 0;
        }
    }

    public Page searchList(int pageNum, int pageSize, CustomerUserGroup userGroupEntity, LoginUser loginUser) {
        Page page = null;
        try {
            if ("2".equals(loginUser.getUserType())) {
                // 组长角色只查询负责的组
                if ("1".equals(loginUser.getUserGroupRole())) {
                    CustomerUserGroupRelDTO customerUserGroupRelDTO = customerUserDao.getCustomerUserGroupByUserId(loginUser.getId());
                    if (customerUserGroupRelDTO != null) {
                        userGroupEntity.setId(customerUserGroupRelDTO.getGroupId());
                    }
                } else if ("2".equals(loginUser.getUserGroupRole())) {
                    // 组员角色返回空
                    return new Page();
                }
            }

            page = customerUserDao.pageCustomerUserGroup(pageNum, pageSize, userGroupEntity);
            List<CustomerUserGroupDTO> list = new ArrayList<>();
            if (page.getData() != null && page.getData().size() > 0) {
                CustomerUserGroup model;
                CustomerUserGroupDTO customerUserGroupDTO;
                for (int i = 0; i < page.getData().size(); i++) {
                    model = (CustomerUserGroup) page.getData().get(i);
                    customerUserGroupDTO = new CustomerUserGroupDTO(model);
                    if (1 == model.getLeavel() && StringUtil.isNotEmpty(model.getPid())) {
                        CustomerUserGroup customerUserGroup = customerUserDao.getCustomerUserGroup(model.getPid());
                        if (customerUserGroup != null) {
                            customerUserGroupDTO.setJobName(customerUserGroup.getName());
                        }
                    }
                    customerUserGroupDTO.setUserCount(customerUserDao.countSelectCustomerUserByUserGroupId(model.getId(), model.getCustId()));
                    list.add(customerUserGroupDTO);
                }
            }
            // 处理组长登录账号
            for (CustomerUserGroupDTO customerUserGroupDTO : list) {
                if (customerUserGroupDTO != null) {
                    customerUserGroupDTO.setGroupLeaderName(customerUserDao.getLoginName(customerUserGroupDTO.getGroupLeaderId()));
                }
            }
            page.setData(list);
        } catch (Exception e) {
            LOG.error("用户群组搜索失败,", e);
            page = new Page();
        }
        return page;
    }

    public List<CustomerUserGroupDTO> listCustomerUserGroup(CustomerUserGroup userGroupEntity, LoginUser loginUser) {
        List<CustomerUserGroupDTO> result = null;
        try {
            if ("2".equals(loginUser.getUserType())) {
                // 组长角色只查询负责的组
                if ("1".equals(loginUser.getUserGroupRole())) {
                    CustomerUserGroupRelDTO customerUserGroupRelDTO = customerUserDao.getCustomerUserGroupByUserId(loginUser.getId());
                    if (customerUserGroupRelDTO != null) {
                        userGroupEntity.setId(customerUserGroupRelDTO.getGroupId());
                    }
                } else if ("2".equals(loginUser.getUserGroupRole())) {
                    // 组员角色返回空
                    return new ArrayList<>();
                }
            }

            List<CustomerUserGroup> list = customerUserDao.listCustomerUserGroup(userGroupEntity);
            result = new ArrayList<>();
            if (list != null && list.size() > 0) {
                CustomerUserGroup model;
                CustomerUserGroupDTO customerUserGroupDTO;
                int userCount = 0;
                for (int i = 0; i < list.size(); i++) {
                    model = list.get(i);
                    customerUserGroupDTO = new CustomerUserGroupDTO(model);
                    // 查询实际组员数量
                    userCount = customerUserDao.countSelectCustomerUserByUserGroupId(model.getId(), model.getCustId());
                    customerUserGroupDTO.setUserCount(userCount);
                    result.add(customerUserGroupDTO);
                }
            }
        } catch (Exception e) {
            LOG.error("获取用户群组列表失败,", e);
            result = new ArrayList<>();
        }
        return result;
    }

    public Map<String, Object> listUserByGroupId(String groupId, String customerId) {
        Map<String, Object> result = null;
        try {
            if (StringUtil.isEmpty(groupId)) {
                throw new NullPointerException("groupId不能为空");
            }
            result = new HashMap<>();
            List<CustomerUserDTO> selectUserList = customerUserDao.listSelectCustomerUserByUserGroupId(groupId, customerId);

            List<CustomerUserDTO> customerUserList = customerUserDao.listUserByCustomerId(customerId);
            Iterator<CustomerUserDTO> customerIterator = customerUserList.iterator();
            for (CustomerUserDTO selectUser : selectUserList) {
                while (customerIterator.hasNext()) {
                    if (Objects.equals(selectUser.getId(), String.valueOf(customerIterator.next().getId()))) {
                        customerIterator.remove();
                    }
                }
            }
            result.put("selectList", selectUserList);
            result.put("allList", customerUserList);
        } catch (Exception e) {
            LOG.error("获取客户和群组ID获取群组员工列表失败,", e);
        }
        return result;
    }

    /**
     * 查询用户群组下所属的员工列表
     *
     * @param groupId
     * @param customerId
     * @return
     */
    public List<CustomerUserDTO> listSelectCustomerUserByUserGroupId(String groupId, String customerId, String startAccount, String endAccount) throws Exception {
        if (StringUtil.isEmpty(groupId)) {
            LOG.warn("groupId为空,返回空数据");
            return new ArrayList<>();
        }
        List<CustomerUserDTO> selectUserList = customerUserDao.listSelectCustomerUserByUserGroupId(groupId, customerId, startAccount, endAccount);
        return selectUserList;
    }

    /**
     * 查询客户下未归属任何分组的员工列表
     *
     * @param customerId
     * @return
     */
    public List<CustomerUserDTO> listNotInUserGroupByCustomerId(String customerId, String startAccount, String endAccount) {
        List<CustomerUserDTO> customerUserList = null;
        try {
            customerUserList = customerUserDao.listNotInUserGroupByCustomerId(customerId, startAccount, endAccount);
        } catch (Exception e) {
            LOG.error("查询客户下未归属任何分组的员工列表,", e);
        }
        return customerUserList;
    }

    /**
     * 分配员工
     *
     * @param groupId
     * @param userIds
     * @return int
     * @author chengning@salescomm.net
     * @date 2018/9/27 17:26
     */
    public int distributionGroupUser(String groupId, List<String> userIds) {
        int result = 0;
        try {
            if (StringUtil.isEmpty(groupId)) {
                throw new NullPointerException("groupId不能为空");
            }
            /*if (userIds == null || userIds.size() == 0) {
                throw new NullPointerException("userIds不能为空");
            }*/

            CustomerUserGroup userGroupResult = customerUserDao.getCustomerUserGroup(groupId);
            String groupLeaderId = "";
            if (userGroupResult != null) {
                // 组长ID
                groupLeaderId = userGroupResult.getGroupLeaderId();
                // 如果没有提交之前的组长ID,并且当前用户群组组长ID不为空,则删除之前的组长信息
                if (StringUtil.isNotEmpty(userGroupResult.getGroupLeaderId()) &&
                        !userIds.contains(userGroupResult.getGroupLeaderId())) {
                    userGroupResult.setGroupLeaderId(null);
                    customerUserDao.saveOrUpdate(userGroupResult);
                    groupLeaderId = null;
                }

                // 逻辑删除用户群组下的用户
                customerUserDao.deleteCustomerUserRelGroup(groupId);
                result = addGroupRel(groupId, userIds, groupLeaderId);
            }

        } catch (Exception e) {
            LOG.error("用户群组分配员工失败,", e);
            result = 0;
        }
        return result;
    }

    public int addGroupRel(String groupId, List<String> userIds, String groupLeaderId) {
        int result = 0;
        if (userIds != null) {
            try {
                List<CustomerUserGroupRel> list = new ArrayList<>();
                CustomerUserGroupRel userGroupRelEntity;
                for (String userId : userIds) {
                    userGroupRelEntity = new CustomerUserGroupRel();
                    // 如果员工管理提交包含上次的组长,角色类型为组长
                    if (StringUtil.isNotEmpty(groupLeaderId) && groupLeaderId.equals(userId)) {
                        userGroupRelEntity.setType(1);
                    } else {
                        userGroupRelEntity.setType(2);
                    }
                    userGroupRelEntity.setGroupId(groupId);
                    userGroupRelEntity.setUserId(userId);
                    userGroupRelEntity.setStatus(1);

                    userGroupRelEntity.setCreateTime(new Timestamp(System.currentTimeMillis()));
                    list.add(userGroupRelEntity);
                }

                customerUserDao.batchSaveOrUpdate(list);
                result = 1;
            } catch (Exception e) {
                e.printStackTrace();
                result = 0;
            }
        }
        return result;
    }

    public List<String> listGroupIdsByUserId(String userId) {
        List<CustomerUserGroupRel> list = customerUserDao.createQuery("from CustomerUserGroupRel where userId = ?", userId).list();
        List<String> groupIds = new ArrayList<>();
        if (list.size() > 0) {
            for (CustomerUserGroupRel entity : list) {
                groupIds.add(entity.getGroupId());
            }
        }
        return groupIds;
    }

    public CustomerUserGroupDTO getUserGroup(String groupId) {
        CustomerUserGroup customerUserGroup = customerUserDao.getCustomerUserGroup(groupId);
        if (customerUserGroup != null) {
            CustomerUserGroupDTO customerUserGroupDTO = new CustomerUserGroupDTO(customerUserGroup);
            customerUserGroupDTO.setGroupLeaderName(customerUserDao.getLoginName(customerUserGroupDTO.getGroupLeaderId()));
            return customerUserGroupDTO;
        }
        return null;
    }


    /**
     * 查询职场定价列表
     *
     * @param serviceCode
     * @param pageNum
     * @param pageSize
     * @param projectId
     * @param customerId
     * @param jobId
     * @return
     */
    public Page searchSettlementList(String serviceCode, int pageNum, int pageSize, String projectId, String customerId, String jobId, String billDate, LoginUser lu) {
        Page page = null;
        if (CommonInfoServiceCodeEnum.SETTING_JOB_SETTLEMENT_PRICE.getKey().equals(serviceCode)) {
            page = pageSettlementPrice(pageNum, pageSize, projectId, customerId, jobId, lu);
        } else if (CommonInfoServiceCodeEnum.SETTING_PROJECT_SETTLEMENT_PRICE.getKey().equals(serviceCode)) {
            page = pageProjectSettlementPrice(pageNum, pageSize, projectId, customerId, lu);
        }

        if (page != null && page.getData() != null && page.getData().size() > 0) {
            List<SettlmentDTO> list = page.getData();
            for (SettlmentDTO o : list) {
                CommonInfoProperty v = findCommonInfoProperty(serviceCode, null, "jobPrice", o.getProjectId(), o.getCustId(), o.getJobId());
                if (v != null) {
                    o.setZid(v.getZid().toString());
                    String price = v.getPropertyValue();
                    if (StringUtil.isNotEmpty(price)) {
                        String pricexx = "" + serviceCode;
                        if (o.getProjectId() != null) {
                            pricexx += o.getProjectId();
                        }
                        if (o.getCustId() != null) {
                            pricexx += o.getCustId();
                        }
                        if (o.getJobId() != null) {
                            pricexx += o.getJobId();
                        }
                        price = price.substring(pricexx.length() + 1);
                        o.setSettlementPrice(price);
                    }

                    CommonInfoProperty remarkobj = findCommonInfoProperty(serviceCode, v.getZid().toString(), "remark", o.getProjectId(), o.getCustId(), o.getJobId());
                    if (remarkobj != null) {
                        String remark = remarkobj.getPropertyValue();
                        if (StringUtil.isNotEmpty(remark)) {
                            //remark = remark.substring((serviceCode+projectId+customerId+jobId).length()+1);
                            o.setRemark(remark);
                        }
                    }

                    CommonInfoProperty log = findCommonInfoProperty(serviceCode, v.getZid().toString(), "updatePriceLog", o.getProjectId(), o.getCustId(), o.getJobId());
                    if (log != null) {
                        String logValue = log.getPropertyValue();
                        if (StringUtil.isNotEmpty(logValue)) {
                            JSONArray arr = JSONArray.parseArray(logValue);
                            JSONArray newArr = new JSONArray();
                            for (int i = arr.size() - 1; i >= 0; i--) {
                                newArr.add(arr.get(i));
                            }
                            o.setSettlementPriceLog(newArr.toJSONString());
                        }
                    }
                }
            }
        }
        return page;
    }

    public Page pageSettlementPrice(int pageNum, int pageSize, String projectId, String customerId, String jobId, LoginUser lu) {
        Page page = new Page();
        StringBuffer sql = new StringBuffer("");

        List<Object> p = new ArrayList<>();
        if (StringUtil.isEmpty(projectId) && StringUtil.isEmpty(customerId) && StringUtil.isEmpty(jobId)) {

            sql.append("select m.`name` as projectName,m.id as projectId,a.enterprise_name as enterpriseName,")
                    .append(" a.cust_id custId,b.name as jobName,b.id as jobId ")
                    .append(" from(select c.name,c.id,c.cust_id as custId,p.cust_id,p.property_value from")
                    .append(" (select t.`name`,t.id,t.cust_id from t_customer_user_group t where t.leavel=0 and t.status=1 ");
            if ("ROLE_USER".equals(lu.getRole())) {
                String uidStr = getCustomerIdByuId(lu.getId().toString());
                if (StringUtil.isNotEmpty(uidStr)) {
                    p.add(uidStr);
                    sql.append(" and t.cust_id in (?)");
                }
            }
            sql.append(")c")
                    .append(" LEFT JOIN t_customer_property p on c.cust_id=p.cust_id ")
                    .append(" where p.property_name like 'marketProjectId_%' ")
                    .append(" and (p.property_value <> null or p.property_value is not null or p.property_value<>''))b")
                    .append(" LEFT JOIN t_customer a on b.custId=a.cust_id ")
                    .append(" LEFT JOIN t_market_project m on b.property_value=m.id ")
                    .append(" where a.status=0")
                    .append(" and m.`status`=1 ")
                    .append("ORDER BY m.id desc");
        } else if (StringUtil.isNotEmpty(projectId) && StringUtil.isEmpty(customerId) && StringUtil.isEmpty(jobId)) {
            p.add(projectId);
            sql.append(" select a.projectId,f.`name` as projectName,a.cust_id as custId," +
                    " a.enterprise_name as enterpriseName,a.jobId,a.jobName from (" +
                    " select b.*,g.id as jobId,g.name as jobName,? as projectId from (" +
                    " select t.cust_id,t.enterprise_name from t_customer t " +
                    " where t.`status`=0");
            if ("ROLE_USER".equals(lu.getRole())) {
                String uidStr = getCustomerIdByuId(lu.getId().toString());
                if (StringUtil.isNotEmpty(uidStr)) {
                    p.add(uidStr);
                    sql.append(" and t.cust_id in (?)");
                }
            }
            p.add("marketProjectId_" + projectId);
            p.add(projectId);
            sql.append(" and t.cust_id in(" +
                    " select distinct cust_id from t_customer_property p " +
                    " where p.property_name = ? and p.property_value = ? " +
                    ")) b" +
                    " left join t_customer_user_group g on b.cust_id=g.cust_id and g.status=1 and g.leavel=0" +
                    ")a " +
                    " LEFT JOIN t_market_project f on f.id = a.projectId ");
        } else if (StringUtil.isNotEmpty(projectId) && StringUtil.isNotEmpty(customerId) && StringUtil.isEmpty(jobId)) {
            p.add(projectId);
            p.add(customerId);
            p.add("marketProjectId_" + projectId);
            p.add(projectId);
            sql.append("select a.projectId,b.`name` as projectName,a.cust_id as custId,a.enterprise_name as enterpriseName," +
                    " a.id as jobId,a.`name` as jobName from (" +
                    " select t.cust_id,t.enterprise_name,g.id,g.`name`, ? as projectId from t_customer_user_group g " +
                    " LEFT JOIN t_customer t on t.cust_id=g.cust_id" +
                    " where g.`status`=1 and t.`status`=0 and g.leavel=0 " +
//                    " and g.cust_id='"+customerId+"'" +
                    " and g.cust_id in(SELECT" +
                    " distinct cust_id FROM t_customer_property where cust_id=? and property_name =? and property_value=?)" +
                    "  ) a left join t_market_project b on a.projectId=b.id and b.`status`=1 order by jobId desc ");
        } else if (StringUtil.isNotEmpty(projectId) && StringUtil.isNotEmpty(customerId) && StringUtil.isNotEmpty(jobId)) {
            p.add(projectId);
            p.add(jobId);
            p.add(customerId);
            p.add("marketProjectId_" + projectId);
            p.add(projectId);
            sql.append("select a.jobId,a.jobName,a.enterpriseName,a.custId,a.projectId,b.name as projectName from (" +
                    " select g.id as jobId,g.`name` as jobName,t.cust_id as custId,t.enterprise_name as enterpriseName, " +
                    " ? as 'projectId'" +
                    " from t_customer_user_group g" +
                    " left JOIN t_customer t" +
                    " on t.cust_id=g.cust_id " +
                    " where g.`status`=1 and t.`status`=0 and g.leavel=0 and g.id=? " +
//                    " and g.cust_id='"+customerId+"'" +
                    " and g.cust_id in(SELECT" +
                    " distinct cust_id FROM t_customer_property where cust_id=? and property_name =?  and property_value=? )" +
                    ")a" +
                    " LEFT JOIN t_market_project b on a.projectId=b.id and b.`status`=1 order by a.jobId desc ");
        }

        String countSql = "select count(0) from (" + sql.toString() + ")a";
        LOG.info("pageSettlementPrice: " + countSql);
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, p.toArray());
        if (totalCount == 0) {
            page.setData(null);
            page.setTotal(0);
            return page;
        }
        page.setTotal(totalCount);
        if (pageNum < 0) pageNum = 0;
        Integer startIndex = pageNum;//*pageSize;
        sql.append(" limit ").append(startIndex).append("," + pageSize);
        List<SettlmentDTO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(SettlmentDTO.class));
        if (list != null && list.size() > 0) {
            page.setData(list);
//            page.setTotal(totalCount);
        }
        return page;
    }


    public Page pageProjectSettlementPrice(int pageNum, int pageSize, String projectId, String customerId, LoginUser lu) {
        Page page = new Page();
        String itemsql = "";
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(projectId) && StringUtil.isEmpty(customerId)) {
            p.add(projectId);
            itemsql = " and t.property_value=? ";
            if ("ROLE_USER".equals(lu.getRole())) {
                String uidStr = getCustomerIdByuId(lu.getId().toString());
                if (StringUtil.isNotEmpty(uidStr)) {
                    p.add(uidStr);
                    itemsql += " and t.cust_id in (?)";
                }
            }
        } else if (StringUtil.isNotEmpty(projectId) && StringUtil.isNotEmpty(customerId)) {
            p.add(customerId);
            p.add(projectId);
            itemsql = " and t.cust_id=? and t.property_value=? ";
        }
        StringBuffer sql = new StringBuffer("select * from ( " +
                " select c.cust_id as custId,c.enterprise_name as enterpriseName," +
                " p.id as projectId,p.`name` as projectName from t_customer c " +
                " left join " +
                " t_customer_property t ON c.cust_id = t.cust_id and c.`status`=0 " +
                " left JOIN " +
                " t_market_project p on p.id = t.property_value and p.`status`=1 " +
                " where t.property_name like 'marketProject%' and t.property_value <>'' " +
                itemsql +
                " )a where projectId is not NULL " +
                " order by projectId desc");

        String countSql = "select count(0) from (" + sql.toString() + ")a";
        LOG.info("pageSettlementPrice: " + countSql);
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, p.toArray());
        if (totalCount == 0) {
            page.setData(null);
            page.setTotal(0);
            return page;
        }
        page.setTotal(totalCount);
        if (pageNum < 0) pageNum = 0;
        Integer startIndex = pageNum;//*pageSize;
        sql.append(" limit ").append(startIndex).append("," + pageSize);
        List<SettlmentDTO> list = jdbcTemplate.query(sql.toString(), new BeanPropertyRowMapper<>(SettlmentDTO.class), p.toArray());
        if (list != null && list.size() > 0) {
            page.setData(list);
//            page.setTotal(totalCount);
        }
        return page;
    }

    /**
     * 结算单管理/客审单管理
     *
     * @param serviceCode
     * @param pageNum
     * @param pageSize
     * @param projectId
     * @param customerId
     * @param jobId
     * @param billDate
     * @return
     */
    public Page pageCommonInfoSearch(String serviceCode, int pageNum, int pageSize, String projectId, String customerId,
                                     String jobId, String billDate, String operator, String status, LoginUser lu) {

        Page page = getPageCommonInfo(serviceCode, pageNum, pageSize, projectId, customerId, jobId, billDate, operator, status, lu);
        List<SettlmentDTO> dtoList = new ArrayList<>();
        if (page != null && page.getData() != null && page.getData().size() > 0) {
            List<CommonInfoProperty> list = page.getData();
            for (CommonInfoProperty infoPropertyDO : list) {
                SettlmentDTO dto = new SettlmentDTO();
                if (CommonInfoServiceCodeEnum.JOB_SETTLEMENT_MANAGE.getKey().equals(serviceCode)
                        || CommonInfoServiceCodeEnum.JOB_SETTLEMENT_APPLY_MANAGE.getKey().equals(serviceCode)
                        || CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_MANAGE.getKey().equals(serviceCode)
                        || CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_APPLY_MANAGE.getKey().equals(serviceCode)) {
                    List<CommonInfoProperty> doList = findCommonInfoPropertyByzid(infoPropertyDO.getZid().toString());
                    if (doList == null) {
                        return page;
                    }
                    dto.setZid(infoPropertyDO.getZid().toString());
                    dto.setServiceCode(serviceCode);
                    for (CommonInfoProperty doObj : doList) {
                        if ("custId".equals(doObj.getPropertyName()) && StringUtil.isNotEmpty(doObj.getPropertyValue())) {
                            dto.setEnterpriseName(customerDao.getEnterpriseName(doObj.getPropertyValue()));
                            dto.setCustId(doObj.getPropertyValue());
                        } else if ("jobId".equals(doObj.getPropertyName()) && StringUtil.isNotEmpty(doObj.getPropertyValue())) {
                            CustomerUserGroup customerUserGroup = customerUserDao.findCustomerUserGroup(doObj.getPropertyValue());
                            if (customerUserGroup != null) {
                                dto.setJobName(customerUserGroup.getName());
                            }
                            dto.setJobId(doObj.getPropertyValue());
                        } else if ("billDate".equals(doObj.getPropertyName())) {
                            dto.setBillDate(doObj.getPropertyValue());
                        } else if ("jobSignNum".equals(doObj.getPropertyName())) {
                            dto.setJobSignNum(doObj.getPropertyValue());
                        } else if ("projectId".equals(doObj.getPropertyName()) && StringUtil.isNotEmpty(doObj.getPropertyValue())) {
                            MarketProject p = marketProjectDao.selectMarketProject(Long.valueOf(doObj.getPropertyValue()).intValue());
                            if (p != null) {
                                dto.setProjectName(p.getName());
                            }
                            dto.setProjectId(doObj.getPropertyValue());
                        } else if ("operator".equals(doObj.getPropertyName()) && StringUtil.isNotEmpty(doObj.getPropertyValue())) {
                            dto.setOperator(doObj.getPropertyValue());
                        } else if ("batchNo".equals(doObj.getPropertyName())) {
                            dto.setBatchNo(doObj.getPropertyValue());
                        } else if ("totalPrice".equals(doObj.getPropertyName())) {
                            dto.setTotalPrice(doObj.getPropertyValue());
                        } else if ("totalSettlementNum".equals(doObj.getPropertyName())) {
                            dto.setTotalSettlementNum(doObj.getPropertyValue());
                        } else if ("confirmPerson".equals(doObj.getPropertyName())) {
                            dto.setConfirmPerson(doObj.getPropertyValue());
                        } else if ("confirmTime".equals(doObj.getPropertyName())) {
                            dto.setConfirmTime(doObj.getPropertyValue());
                        } else if ("paymentTime".equals(doObj.getPropertyName())) {
                            dto.setPaymentTime(doObj.getPropertyValue());
                        } else if ("confirmRemark".equals(doObj.getPropertyName())) {
                            dto.setConfirmRemark(doObj.getPropertyValue());
                        } else if ("remark".equals(doObj.getPropertyName())) {
                            dto.setRemark(doObj.getPropertyValue());
                        }
                    }
                    if (!serviceCode.equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_APPLY_MANAGE.getKey())
                            && !serviceCode.equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_APPLY_MANAGE.getKey())) {
                        Integer total = 0;
                        if (serviceCode.equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_MANAGE.getKey())) {
                            total = getCountJobSettlementNumbyPid(infoPropertyDO.getZid().toString(), CommonInfoServiceCodeEnum.JOB_SETTLEMENT_DETAIL.getKey());
                        } else if (serviceCode.equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_MANAGE.getKey())) {
                            total = getCountJobSettlementNumbyPid(infoPropertyDO.getZid().toString(), CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_DETAIL.getKey());
                        }
                        dto.setTotalSettlementNum(total == null ? "" : total.toString());
                    }
                    if (serviceCode.equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_APPLY_MANAGE.getKey())
                            || serviceCode.equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_APPLY_MANAGE.getKey())) {
                        CommonInfoDO infoDO = findCommonInfoById(infoPropertyDO.getZid());
                        if (infoDO != null) {
                            dto.setStatus(infoDO.getStatus() == null ? "" : infoDO.getStatus().toString());
                            dto.setCreateTime(String.valueOf(infoDO.getCreateTime()));
                        }
                    }
                }
                dtoList.add(dto);
            }
            page.setData(dtoList);
        }
        return page;
    }

    /**
     * 职场结算单详情列表
     *
     * @param pageNum
     * @param pageSize
     * @param pid
     * @param serviceCode
     * @param projectId
     * @param type
     * @param status
     * @param operator
     * @param startTime
     * @param endTime
     * @param batchNo
     * @param hasPage     是否分页
     * @return
     */
    public Page jobSettlementDetailList(Integer pageNum, Integer pageSize, String pid, String serviceCode,
                                        String projectId, String type, String status, String operator, String startTime,
                                        String endTime, String batchNo, Boolean hasPage) {
        Page page = new Page();

        String sql = "select g.*,project.name as projectName,commoninfo.status,commoninfo.create_time as createTime from ( " +
                " select a.zid,a.service_code as serviceCode," +
                " GROUP_CONCAT(custId order by custId separator '')custId," +
                " GROUP_CONCAT(unitPrice order by unitPrice separator '')unitPrice," +
                " GROUP_CONCAT(projectId order by projectId separator '')projectId," +
                " GROUP_CONCAT(jobId order by jobId separator '')jobId," +
                " GROUP_CONCAT(jobSettlementNum order by jobSettlementNum separator '')jobSettlementNum," +
                " GROUP_CONCAT(billDate order by billDate separator '')billDate," +
                " GROUP_CONCAT(type order by type separator '')type," +
                " GROUP_CONCAT(pid order by pid separator '')pid," +
                " GROUP_CONCAT(batchNo order by batchNo separator '')batchNo," +
                " GROUP_CONCAT(remark order by remark separator '')remark," +
                " GROUP_CONCAT(operator order by operator separator '')operator" +
                " from (" +
                " select b.zid,b.service_code," +
                " (case b.property_name when 'custId' then b.property_value else '' end )custId," +
                " (case b.property_name when 'jobId' then b.property_value else '' end )jobId," +
                " (case b.property_name when 'pid' then b.property_value else '' end )pid," +
                " (case b.property_name when 'batchNo' then b.property_value else '' end )batchNo," +
                " (case b.property_name when 'remark' then b.property_value else '' end )remark," +
                " (case b.property_name when 'projectId' then b.property_value else '' end )projectId," +
                " (case b.property_name when 'jobSettlementNum' then b.property_value else '' end )jobSettlementNum," +
                " (case b.property_name when 'billDate' then b.property_value else '' end )billDate," +
                " (case b.property_name when 'unitPrice' then b.property_value else '' end )unitPrice," +
                " (case b.property_name when 'type' then b.property_value else '' end )type," +
                " (case b.property_name when 'operator' then b.property_value else '' end )operator " +
                " FROM " +
                "  t_common_info_property b where service_code='" + serviceCode + "' group by b.zid,b.service_code,b.property_name" +
                " )a GROUP BY zid,service_code )g" +
                " left join t_common_info commoninfo on g.zid=commoninfo.id" +
                " left join t_market_project project on g.projectId=project.id" +
                " where 1=1 ";
        List<Object> p = new ArrayList<>();
        if (StringUtil.isNotEmpty(pid)) {
            p.add(pid);
            sql += " and g.pid=? ";
        }//and billDate='"+billDate+"'";
        if (StringUtil.isNotEmpty(batchNo)) {
            p.add(batchNo);
            sql += " and g.batchNo=? ";
        }
        if (StringUtil.isNotEmpty(projectId)) {
            p.add(projectId);
            sql += " and g.projectId=? ";
        }
        if (StringUtil.isNotEmpty(type)) {
            p.add(type);
            sql += " and g.type=? ";
        }
        if (StringUtil.isNotEmpty(operator)) {
            p.add(operator);
            sql += " and g.operator=? ";
        }
        if (StringUtil.isNotEmpty(status)) {
            p.add(status);
            sql += " and commoninfo.status=? ";
        }
        if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
            p.add(startTime);
            p.add(endTime);
            sql += " and commoninfo.create_time>=? and commoninfo.create_time<=? ";
        }

        sql += " order by createTime desc ";

        String countSql = "select count(0) from (" + sql + ")a";
        LOG.info("jobSettlementDetailList: " + countSql);
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, p.toArray());
        if (totalCount == 0) {
            page.setData(null);
            page.setTotal(0);
            return page;
        }
        page.setTotal(totalCount);
        if (hasPage) {
            if (pageNum < 0) pageNum = 0;
            Integer startIndex = pageNum;
            sql += " limit " + startIndex + "," + pageSize;
        }
        List<SettlmentDTO> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(SettlmentDTO.class));
        if (list != null && list.size() > 0) {
            page.setData(list);
        }
        return page;

    }


    private Integer getCountJobSettlementNumbyPid(String pid, String serviceCode) {
        List<CommonInfoProperty> list = getJobSettlementListbyPid(pid, serviceCode);
        if (list == null || list.isEmpty()) {
            return null;
        }
        Integer total = 0;
        for (CommonInfoProperty infoPropertyDO : list) {
            CommonInfoProperty obj = findCommonInfoProperty(serviceCode, infoPropertyDO.getZid().toString(), "jobSettlementNum", null, null, null);
            if (obj != null) {
                total += Integer.valueOf(obj.getPropertyValue());
            }
        }
        return total;
    }

    /**
     * 根据pid查找添加的职场结算单
     *
     * @param pid
     * @param serviceCode
     * @return
     */
    private List<CommonInfoProperty> getJobSettlementListbyPid(String pid, String serviceCode) {
        String sql = "select * from t_common_info_property where property_name='pid' " +
                "   and property_value=? and service_code=? ";
        List<CommonInfoProperty> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CommonInfoProperty.class), pid, serviceCode);

        return list;
    }

    public Page getPageCommonInfo(String serviceCode, int pageNum, int pageSize, String projectId, String customerId,
                                  String jobId, String billDate, String operator, String status, LoginUser lu) {
        Page page = new Page();
        List<Object> p = new ArrayList<>();
        String sql = "";
        if (serviceCode.equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_MANAGE.getKey())) {
            sql = buildJobSettmentSql(p, serviceCode, projectId, customerId, jobId, billDate, lu);
        } else if (serviceCode.equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_MANAGE.getKey())) {
            sql = buildProjectCustomCheckSql(p, serviceCode, projectId, customerId, billDate, lu);
        } else if (serviceCode.equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_APPLY_MANAGE.getKey())) {
            sql = buildJobPaySql(p, serviceCode, customerId, jobId, billDate, operator, status, lu);
        } else if (serviceCode.equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_APPLY_MANAGE.getKey())) {
            sql = buildProjectReceivablesSql(p, serviceCode, projectId, customerId, billDate, operator, status, lu);
        }

        String countSql = "select count(0) from (" + sql + ")a";
        LOG.info("pageCommonInfoSearch: " + countSql);
        int totalCount = jdbcTemplate.queryForObject(countSql, Integer.class, p.toArray());
        if (totalCount == 0) {
            page.setData(null);
            page.setTotal(0);
            return page;
        }
        page.setTotal(totalCount);

        if (pageNum < 0) pageNum = 0;
        Integer startIndex = pageNum;//*pageSize;

        sql += " limit " + startIndex + "," + pageSize;

        List<CommonInfoProperty> list = jdbcTemplate.query(sql, new BeanPropertyRowMapper<>(CommonInfoProperty.class), p.toArray());
        if (list != null && list.size() > 0) {
            page.setData(list);
//            page.setTotal(totalCount);
        }
        return page;
    }

    /**
     * 职场结算单汇总页面列表
     *
     * @param serviceCode
     * @param projectId
     * @param customerId
     * @param jobId
     * @param billDate
     * @return
     */
    private String buildJobSettmentSql(List<Object> p, String serviceCode, String projectId, String customerId, String jobId,
                                       String billDate, LoginUser lu) {
        String itemSql = "";
        if (StringUtil.isEmpty(billDate)) {
            billDate = DateUtil.getPastNMonthDate(new Date(), "yyyyMM", -5);
            itemSql = " and property_value>? ";
            p.add(billDate);
        } else {
            itemSql = " and property_value=? ";
            p.add(billDate);
        }
        String sql = " select distinct zid from t_common_info_property " +
                " where service_code=? and property_name='billDate' " + itemSql;
        p.add(serviceCode);
        if (StringUtil.isNotEmpty(customerId)) {
            sql = "select distinct zid from t_common_info_property where service_code=? " +
                    " and property_name='custId' and property_value=? and zid in(" + sql + ")";
            p.add(serviceCode);
            p.add(customerId);
        } else {
            if ("ROLE_USER".equals(lu.getRole())) {
                String uidStr = getCustomerIdByuId(lu.getId().toString());
                if (StringUtil.isNotEmpty(uidStr)) {
                    p.add(serviceCode);
                    p.add(uidStr);
                    sql = "select distinct zid from t_common_info_property where service_code=?" +
                            " and property_name='custId' and property_value in(?) and zid in(" + sql + ")";
                }
            }
        }
        if (StringUtil.isNotEmpty(jobId)) {
            p.add(serviceCode);
            p.add(jobId);
            sql = "select distinct zid from t_common_info_property where service_code=? " +
                    " and property_name='jobId' and property_value=? and zid in(" + sql + ")";
        }
        return sql + " order by zid desc ";
    }

    /**
     * 项目客审单汇总列表查询
     *
     * @param serviceCode
     * @param projectId
     * @param customerId
     * @param billDate
     * @return
     */
    private String buildProjectCustomCheckSql(List<Object> p, String serviceCode, String projectId, String customerId, String billDate, LoginUser lu) {
        String itemSql = "";
        if (StringUtil.isEmpty(billDate)) {
            billDate = DateUtil.getPastNMonthDate(new Date(), "yyyyMM", 0);
            itemSql = " and property_value=? ";
            p.add(billDate);
        } else {
            itemSql = " and property_value=? ";
            p.add(billDate);
        }

        p.add(serviceCode);
        String sql = " select distinct zid from t_common_info_property " +
                " where service_code=? and property_name='billDate' " + itemSql;

        if (StringUtil.isNotEmpty(projectId)) {
            p.add(serviceCode);
            p.add(projectId);
            sql = "select distinct zid from t_common_info_property where service_code=? " +
                    " and property_name='projectId' and property_value= ? and zid in(" + sql + ") ";
        }

        if (StringUtil.isNotEmpty(customerId)) {
            p.add(serviceCode);
            p.add(customerId);
            sql = "select distinct zid from t_common_info_property where service_code=?" +
                    " and property_name='custId' and property_value=? and zid in(" + sql + ")";
        } else {
            if ("ROLE_USER".equals(lu.getRole())) {
                String uidStr = getCustomerIdByuId(lu.getId().toString());
                if (StringUtil.isNotEmpty(uidStr)) {
                    p.add(serviceCode);
                    p.add(uidStr);
                    sql = "select distinct zid from t_common_info_property where service_code=? " +
                            " and property_name='custId' and property_value in(?) and zid in(" + sql + ")";
                }
            }
        }

        return sql;
    }


    private String buildJobPaySql(List<Object> p, String serviceCode, String customerId, String jobId, String billDate, String operator,
                                  String status, LoginUser lu) {
        String itemSql = "";
        if (StringUtil.isEmpty(billDate)) {
            billDate = DateUtil.getPastNMonthDate(new Date(), "yyyyMM", -5);
            itemSql = " and property_value>? ";
            p.add(billDate);
        } else {
            itemSql = " and property_value=? ";
            p.add(billDate);
        }
        p.add(serviceCode);
        String sql = " select distinct zid from t_common_info_property " +
                " where service_code=? and property_name='billDate' " + itemSql;
        if (StringUtil.isNotEmpty(customerId)) {
            p.add(serviceCode);
            p.add(customerId);
            sql = "select distinct zid from t_common_info_property where service_code=?" +
                    " and property_name='custId' and property_value=? and zid in(" + sql + ")";
        } else {
            if ("ROLE_USER".equals(lu.getRole())) {
                String uidStr = getCustomerIdByuId(lu.getId().toString());
                if (StringUtil.isNotEmpty(uidStr)) {
                    p.add(serviceCode);
                    p.add(uidStr);
                    sql = "select distinct zid from t_common_info_property where service_code=? " +
                            " and property_name='custId' and property_value in(?) and zid in(" + sql + ")";
                }
            }
        }
        if (StringUtil.isNotEmpty(jobId)) {
            p.add(serviceCode);
            p.add(jobId);
            sql = "select distinct zid from t_common_info_property where service_code=? " +
                    " and property_name='jobId' and property_value=? and zid in(" + sql + ")";
        }
        if (StringUtil.isNotEmpty(operator)) {
            p.add(serviceCode);
            p.add(operator);
            sql = "select distinct zid from t_common_info_property where service_code=?" +
                    " and property_name='operator' and property_value=? and zid in(" + sql + ")";
        }
        String _sql = " select zid from (" + sql + ")a left join t_common_info info  on a.zid=info.id ";
        if (StringUtil.isNotEmpty(status)) {
            p.add(status);
            _sql += " where info.status=?";
        }
        return _sql + " order by zid desc ";
    }

    /**
     * 项目收款sql
     *
     * @param serviceCode
     * @param customerId
     * @param projectId
     * @param billDate
     * @param operator
     * @param status
     * @return
     */
    private String buildProjectReceivablesSql(List<Object> p, String serviceCode, String projectId, String customerId, String billDate,
                                              String operator, String status, LoginUser lu) {
        String itemSql = "";
        if (StringUtil.isEmpty(billDate)) {
            billDate = DateUtil.getPastNMonthDate(new Date(), "yyyyMM", 0);
            itemSql = " and property_value=? ";
            p.add(billDate);
        } else {
            itemSql = " and property_value=? ";
            p.add(billDate);
        }
        p.add(serviceCode);
        String sql = " select distinct zid from t_common_info_property " +
                " where service_code=? and property_name='billDate' " + itemSql;

        if (StringUtil.isNotEmpty(projectId)) {
            p.add(serviceCode);
            p.add(projectId);
            sql = "select distinct zid from t_common_info_property where service_code=? " +
                    " and property_name='projectId' and property_value=? and zid in(" + sql + ")";
        }

        if (StringUtil.isNotEmpty(customerId)) {
            p.add(serviceCode);
            p.add(customerId);
            sql = "select distinct zid from t_common_info_property where service_code=? " +
                    " and property_name='custId' and property_value=? and zid in(" + sql + ")";
        } else {
            if ("ROLE_USER".equals(lu.getRole())) {
                String uidStr = getCustomerIdByuId(lu.getId().toString());
                if (StringUtil.isNotEmpty(uidStr)) {
                    p.add(serviceCode);
                    p.add(uidStr);
                    sql = "select distinct zid from t_common_info_property where service_code=? " +
                            " and property_name='custId' and property_value in(?) and zid in(" + sql + ")";
                }
            }
        }

        if (StringUtil.isNotEmpty(operator)) {
            p.add(serviceCode);
            p.add(operator);
            sql = "select distinct zid from t_common_info_property where service_code=? " +
                    " and property_name='operator' and property_value=? and zid in(" + sql + ")";
        }
        String _sql = " select zid from (" + sql + ")a left join t_common_info info  on a.zid=info.id ";
        if (StringUtil.isNotEmpty(status)) {
            p.add(status);
            _sql += " where info.status=? ";
        }
        return _sql + " order by zid desc ";
    }


    public String getCustomerIdByuId(String uid) {
        String result = "";
        List<RoleDataPermissonDTO> list = roleResourceDao.getUserDataPermissonListByRoleId(uid, "4");//查询配置的客户
        if (list != null && list.size() > 0) {
            for (RoleDataPermissonDTO d : list) {
                result += ",'" + d.getrId() + "'";
            }
        }
        if (result.length() > 0) {
            return result.substring(1);
        }
        return result;
    }

    /**
     * 获取用户数据权限 1-返回供应商ID 2-返回客户ID
     *
     * @param userId 用户ID
     * @param type   1-供应商 2-客户
     * @return
     */
    public String getUserDataPermissonListByUserId(String userId, int type) {
        String result = "";
        List<RoleDataPermissonDTO> list = roleResourceDao.getUserDataPermissonListByRoleId(userId, String.valueOf(type));
        if (list != null && list.size() > 0) {
            for (RoleDataPermissonDTO d : list) {
                result += ",'" + d.getrId() + "'";
            }
        }
        if (result.length() > 0) {
            return result.substring(1);
        }
        return result;
    }


    /**
     * 查找通用属性
     *
     * @param serviceCode
     * @param propertyName
     * @param projectId
     * @param custId
     * @param jobId
     * @return
     */
    public CommonInfoProperty findCommonInfoProperty(String serviceCode, String zid, String propertyName, String projectId, String custId, String jobId) {
        String sql = "";
        List<Object> p = new ArrayList<>();
        if (StringUtil.isEmpty(zid)) {
            String propertyValue = serviceCode;
            if (StringUtil.isNotEmpty(projectId)) {
                propertyValue += projectId;
            }
            if (StringUtil.isNotEmpty(custId)) {
                propertyValue += custId;
            }
            if (StringUtil.isNotEmpty(jobId)) {
                propertyValue += jobId;
            }
            p.add(serviceCode);
            p.add(propertyName);
            p.add(propertyValue + "%");
            sql = "select * from t_common_info_property where service_code=? " +
                    " and property_name=? and property_value like ? ";
        } else {
            p.add(zid);
            p.add(serviceCode);
            p.add(propertyName);
            sql = "select * from t_common_info_property " +
                    "where zid=? and service_code=? and property_name=? ";
        }
        LOG.info("findCommonInfoProperty: " + sql);
        RowMapper<CommonInfoProperty> rowMapper = new BeanPropertyRowMapper<>(CommonInfoProperty.class);
//        List<Map<String, Object>>List<CommonInfoPropertyDO> infoPropertyDOList = jdbcTemplate.queryForList(sql, CommonInfoPropertyDO.class);
        List<CommonInfoProperty> infoPropertyDOList = jdbcTemplate.query(sql, rowMapper, p.toArray());
        if (infoPropertyDOList != null && infoPropertyDOList.size() > 0) {
            return infoPropertyDOList.get(0);
        }
        return null;
    }

    public List<CommonInfoProperty> findCommonInfoPropertyByzid(String zid) {
        String sql = "select * from t_common_info_property where zid=? ";

        LOG.info("findCommonInfoPropertyByzid: " + sql);

        RowMapper<CommonInfoProperty> rowMapper = new BeanPropertyRowMapper<>(CommonInfoProperty.class);
        List<CommonInfoProperty> infoPropertyDOList = jdbcTemplate.query(sql, rowMapper, zid);
        if (infoPropertyDOList != null && infoPropertyDOList.size() > 0) {
            return infoPropertyDOList;
        }
        return null;
    }

    public List<CommonInfoProperty> findCommonInfoPropertyByProperty(String serviceCode, String propertyName, String propertyValue) {
        String sql = "select * from t_common_info_property where service_code=? and  property_name=? and property_value=? ";

        LOG.info("findCommonInfoPropertyBypid: " + sql);

        RowMapper<CommonInfoProperty> rowMapper = new BeanPropertyRowMapper<>(CommonInfoProperty.class);
        List<CommonInfoProperty> infoPropertyDOList = jdbcTemplate.query(sql, rowMapper, serviceCode, propertyName, propertyValue);
        if (infoPropertyDOList != null && infoPropertyDOList.size() > 0) {
            return infoPropertyDOList;
        }
        return null;
    }

    /**
     * 设置售价/结算单录入/项目审单录入
     *
     * @param settlmentDTO
     */
    public void settingSettlementPrice(CommonInfoDTO settlmentDTO, LoginUser lu) throws Exception {
        CommonInfoProperty commonInfoPropertyDO = null;
        CommonInfoDO infoDO = null;
        List<Map<String, String>> properties = settlmentDTO.getProperties();
        if (properties == null || properties.size() == 0) {
            return;
        }

        List<CommonInfoProperty> infoPropertyDOS = new ArrayList<>();
        //新建
        if (StringUtil.isEmpty(settlmentDTO.getZid())) {
            infoDO = saveCommonInfo(settlmentDTO.getServiceCode(), settlmentDTO.getStatus());
            for (Map<String, String> o : properties) {
                commonInfoPropertyDO = new CommonInfoProperty();
                commonInfoPropertyDO.setServiceCode(settlmentDTO.getServiceCode());
                commonInfoPropertyDO.setZid(infoDO.getId());

                String propertyName = o.getOrDefault("propertyName", "");
                String propertyValue = o.getOrDefault("propertyValue", "");
                if (propertyName.equals("jobPrice")) {
                    String _propertyValue = settlmentDTO.getServiceCode();
                    if (StringUtil.isNotEmpty(settlmentDTO.getProjectId())) {
                        _propertyValue += settlmentDTO.getProjectId();
                    }
                    if (StringUtil.isNotEmpty(settlmentDTO.getCustId())) {
                        _propertyValue += settlmentDTO.getCustId();
                    }
                    if (StringUtil.isNotEmpty(settlmentDTO.getJobId())) {
                        _propertyValue += settlmentDTO.getJobId();
                    }
                    propertyValue = _propertyValue + "#" + propertyValue;
                    commonInfoPropertyDO.setPropertyName(propertyName);
                    commonInfoPropertyDO.setPropertyValue(propertyValue);
                } else {
                    commonInfoPropertyDO.setPropertyName(propertyName);
                    commonInfoPropertyDO.setPropertyValue(propertyValue);
                }
                commonInfoPropertyDO.setCreateTime(new Date());
                infoPropertyDOS.add(commonInfoPropertyDO);
            }
            saveCommonPropertyObj(settlmentDTO, infoDO.getId(), infoPropertyDOS, lu);
            //设置售价才有设置历史
            if (settlmentDTO.getServiceCode().equals(CommonInfoServiceCodeEnum.SETTING_JOB_SETTLEMENT_PRICE.getKey())
                    || settlmentDTO.getServiceCode().equals(CommonInfoServiceCodeEnum.SETTING_PROJECT_SETTLEMENT_PRICE.getKey())) {
                addUpdateLog(lu, settlmentDTO, infoDO.getId(), infoPropertyDOS);
            }


            if (settlmentDTO.getServiceCode().equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_DETAIL.getKey())
                    || settlmentDTO.getServiceCode().equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_DETAIL.getKey())) {


                //添加项目审单/结算单时先查询是否设置过售价
                String serviceCode = "";
                if (settlmentDTO.getServiceCode().equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_DETAIL.getKey())) {
                    serviceCode = CommonInfoServiceCodeEnum.SETTING_JOB_SETTLEMENT_PRICE.getKey();
                } else if (settlmentDTO.getServiceCode().equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_DETAIL.getKey())) {
                    serviceCode = CommonInfoServiceCodeEnum.SETTING_PROJECT_SETTLEMENT_PRICE.getKey();
                }
                CommonInfoProperty propertyDO = findCommonInfoProperty(serviceCode,
                        null, "jobPrice", settlmentDTO.getProjectId(), settlmentDTO.getCustId(), settlmentDTO.getJobId());
                if (propertyDO == null || StringUtil.isEmpty(propertyDO.getPropertyValue())) {
                    throw new ParamException("请先设置价格");
                } else {
                    String price = propertyDO.getPropertyValue();
                    String pricexx = serviceCode;
                    if (settlmentDTO.getProjectId() != null) {
                        pricexx += settlmentDTO.getProjectId();
                    }
                    if (settlmentDTO.getCustId() != null) {
                        pricexx += settlmentDTO.getCustId();
                    }
                    if (settlmentDTO.getJobId() != null) {
                        pricexx += settlmentDTO.getJobId();
                    }
                    price = price.substring(pricexx.length() + 1);
                    commonInfoPropertyDO = new CommonInfoProperty();
                    commonInfoPropertyDO.setZid(infoDO.getId());
                    commonInfoPropertyDO.setServiceCode(settlmentDTO.getServiceCode());
                    commonInfoPropertyDO.setPropertyName("unitPrice");
                    commonInfoPropertyDO.setPropertyValue(price);
                    commonInfoPropertyDO.setCreateTime(new Date());
                    infoPropertyDOS.add(commonInfoPropertyDO);

                }
            }
            customerUserDao.batchSaveOrUpdate(infoPropertyDOS);
        } else {
            commonInfoPropertyDO = findCommonInfoProperty(settlmentDTO.getServiceCode(),
                    settlmentDTO.getZid().toString(), "jobPrice", null, null, null);
            for (Map<String, String> o : properties) {
                String _propertyValue = settlmentDTO.getServiceCode();
                String propertyName = o.getOrDefault("propertyName", "");
                String propertyValue = o.getOrDefault("propertyValue", "");
                if ("jobPrice".equals(propertyName)) {
                    if (StringUtil.isNotEmpty(settlmentDTO.getProjectId())) {
                        _propertyValue += settlmentDTO.getProjectId();
                    }
                    if (StringUtil.isNotEmpty(settlmentDTO.getCustId())) {
                        _propertyValue += settlmentDTO.getCustId();
                    }
                    if (StringUtil.isNotEmpty(settlmentDTO.getJobId())) {
                        _propertyValue += settlmentDTO.getJobId();
                    }
                    propertyValue = _propertyValue + "#" + propertyValue;
                    commonInfoPropertyDO.setPropertyValue(propertyValue);
                    commonInfoPropertyDO.setPropertyName(propertyName);
                    commonInfoPropertyDO.setCreateTime(new Date());
                } else {
                    commonInfoPropertyDO = new CommonInfoProperty();
                    commonInfoPropertyDO.setServiceCode(settlmentDTO.getServiceCode());
                    commonInfoPropertyDO.setZid(Long.valueOf(settlmentDTO.getZid()));
                    commonInfoPropertyDO.setPropertyName(propertyName);
                    commonInfoPropertyDO.setPropertyValue(propertyValue);
                    commonInfoPropertyDO.setCreateTime(new Date());
                }
                infoPropertyDOS.add(commonInfoPropertyDO);
            }
            addUpdateLog(lu, settlmentDTO, Long.valueOf(settlmentDTO.getZid()), infoPropertyDOS);
            customerUserDao.batchSaveOrUpdate(infoPropertyDOS);
        }
    }

    private CommonInfoDO saveCommonInfo(String serviceCode, String status) {
        CommonInfoDO infoDO = new CommonInfoDO();
        infoDO.setId(IDHelper.getID());
        infoDO.setCreateTime(new Timestamp(System.currentTimeMillis()));
        infoDO.setServiceCode(serviceCode);
        if (StringUtil.isNotEmpty(status)) {
            infoDO.setStatus(Integer.valueOf(status));
        }
        customerUserDao.saveOrUpdate(infoDO);
        return infoDO;
    }


    private void saveCommonPropertyObj(CommonInfoDTO settlmentDTO, Long zid,
                                       List<CommonInfoProperty> infoPropertyDOS, LoginUser lu) {

        CommonInfoProperty commonInfoPropertyDO = null;
        if (StringUtil.isNotEmpty(settlmentDTO.getProjectId())) {
            commonInfoPropertyDO = new CommonInfoProperty();
            commonInfoPropertyDO.setServiceCode(settlmentDTO.getServiceCode());
            commonInfoPropertyDO.setZid(zid);
            commonInfoPropertyDO.setPropertyName("projectId");
            commonInfoPropertyDO.setCreateTime(new Date());
            commonInfoPropertyDO.setPropertyValue(settlmentDTO.getProjectId());
            infoPropertyDOS.add(commonInfoPropertyDO);
        }
        if (StringUtil.isNotEmpty(settlmentDTO.getCustId())) {
            commonInfoPropertyDO = new CommonInfoProperty();
            commonInfoPropertyDO.setServiceCode(settlmentDTO.getServiceCode());
            commonInfoPropertyDO.setZid(zid);
            commonInfoPropertyDO.setPropertyName("custId");
            commonInfoPropertyDO.setPropertyValue(settlmentDTO.getCustId());
            commonInfoPropertyDO.setCreateTime(new Date());
            infoPropertyDOS.add(commonInfoPropertyDO);
        }
        if (StringUtil.isNotEmpty(settlmentDTO.getJobId())) {
            commonInfoPropertyDO = new CommonInfoProperty();
            commonInfoPropertyDO.setServiceCode(settlmentDTO.getServiceCode());
            commonInfoPropertyDO.setZid(zid);
            commonInfoPropertyDO.setPropertyName("jobId");
            commonInfoPropertyDO.setPropertyValue(settlmentDTO.getJobId());
            commonInfoPropertyDO.setCreateTime(new Date());
            infoPropertyDOS.add(commonInfoPropertyDO);
        }

        commonInfoPropertyDO = new CommonInfoProperty();
        commonInfoPropertyDO.setZid(zid);
        commonInfoPropertyDO.setServiceCode(settlmentDTO.getServiceCode());
        commonInfoPropertyDO.setPropertyName("operator");
        commonInfoPropertyDO.setPropertyValue(lu.getUsername());
        commonInfoPropertyDO.setCreateTime(new Date());
        infoPropertyDOS.add(commonInfoPropertyDO);
    }

    /**
     * 修改记录
     *
     * @param lu
     * @param settlmentDTO
     * @param zid
     * @param infoPropertyDOS
     */
    private void addUpdateLog(LoginUser lu, CommonInfoDTO settlmentDTO, Long zid,
                              List<CommonInfoProperty> infoPropertyDOS) {
        JSONObject jsonObject = new JSONObject();
        CommonInfoProperty infoPropertyDO = findCommonInfoProperty(settlmentDTO.getServiceCode(), zid.toString(), "updatePriceLog", null, null, null);
        JSONArray arr = null;
        jsonObject.put("operatorTime", System.currentTimeMillis());
        jsonObject.put("operator", lu.getUsername());
        if (infoPropertyDO != null) {
            String logStr = infoPropertyDO.getPropertyValue();
            arr = JSONObject.parseArray(logStr);
            buildLogArr(settlmentDTO, jsonObject);
            arr.add(jsonObject);
        } else {
            infoPropertyDO = new CommonInfoProperty();
            infoPropertyDO.setZid(zid);
            infoPropertyDO.setServiceCode(settlmentDTO.getServiceCode());
            infoPropertyDO.setPropertyName("updatePriceLog");
            buildLogArr(settlmentDTO, jsonObject);
            arr = new JSONArray();
            arr.add(jsonObject);
        }
        infoPropertyDO.setPropertyValue(arr.toJSONString());
        infoPropertyDOS.add(infoPropertyDO);
    }

    private void buildLogArr(CommonInfoDTO settlmentDTO, JSONObject jsonObject) {
        for (Map<String, String> o : settlmentDTO.getProperties()) {
            String propertyName = o.getOrDefault("propertyName", "");
            String propertyValue = o.getOrDefault("propertyValue", "");
            if ("jobPrice".equals(propertyName)) {
                jsonObject.put("price", propertyValue);
            }
            if ("remark".equals(propertyName)) {
                jsonObject.put("remark", propertyValue);
            }
        }
    }

    /**
     * 确认提交付款，修改职场确认单状态，生成职场付款申请单
     *
     * @param pid
     * @param isall
     * @param commitStr
     */
    public void jobSelementCommit(String serviceCode, String pid, String isall, String commitStr, LoginUser lu, String remark) throws Exception {
        if (StringUtil.isNotEmpty(isall) && "Y".equals(isall)) {
            List<CommonInfoProperty> list = findCommonInfoPropertyByProperty(serviceCode, "pid", pid);
            for (CommonInfoProperty propertyDO : list) {
                commitStr += "," + propertyDO.getZid();
            }
            if (commitStr != null && commitStr.length() > 0) {
                commitStr = commitStr.substring(1);
            }
        }
        if (StringUtil.isEmpty(commitStr)) {
            LogUtil.error("no commitStr");
            return;
        }
        String sql = " select * from t_common_info where id in(?) and status=0";
        RowMapper<CommonInfoDO> rowMapper = new BeanPropertyRowMapper<>(CommonInfoDO.class);
        List<CommonInfoDO> commonInfoDOSb = jdbcTemplate.query(sql, rowMapper, commitStr);
        if (commonInfoDOSb != null && commonInfoDOSb.size() > 0) {
            BigDecimal totalPrice = new BigDecimal("0");
            Integer totalSettlement = 0;
            CommonInfoDTO commonInfoDTO = new CommonInfoDTO();
            List<Map<String, String>> applyList = new ArrayList<>();
            String billDateStr = null;
            for (CommonInfoDO infoDO : commonInfoDOSb) {
                CommonInfoProperty jobSettlementNumDo = findCommonInfoProperty(serviceCode, infoDO.getId().toString(), "jobSettlementNum", null, null, null);
                CommonInfoProperty typeObj = findCommonInfoProperty(serviceCode, infoDO.getId().toString(), "type", null, null, null);
                if (jobSettlementNumDo != null && jobSettlementNumDo.getPropertyValue() != null) {
                    totalSettlement += (Integer.valueOf(jobSettlementNumDo.getPropertyValue()) * (Integer.valueOf(typeObj.getPropertyValue())));
                }
                CommonInfoProperty unitPrice = findCommonInfoProperty(serviceCode, infoDO.getId().toString(), "unitPrice", null, null, null);
                if (typeObj != null && unitPrice != null && jobSettlementNumDo != null) {
                    BigDecimal num = new BigDecimal(jobSettlementNumDo.getPropertyValue());
                    BigDecimal typeB = new BigDecimal(typeObj.getPropertyValue());
                    BigDecimal unitPriceB = new BigDecimal(unitPrice.getPropertyValue());
                    BigDecimal flag = typeB.multiply(unitPriceB);
                    BigDecimal mul = num.multiply(flag);
                    BigDecimal temp = totalPrice.add(mul);
                    totalPrice = temp;

                }
                if (StringUtil.isEmpty(commonInfoDTO.getJobId())) {
                    CommonInfoProperty jobIdDo = findCommonInfoProperty(serviceCode, infoDO.getId().toString(), "jobId", null, null, null);
                    if (jobIdDo != null) commonInfoDTO.setJobId(jobIdDo.getPropertyValue());
                }
                if (StringUtil.isEmpty(commonInfoDTO.getCustId())) {
                    CommonInfoProperty custIdDo = findCommonInfoProperty(serviceCode, infoDO.getId().toString(), "custId", null, null, null);
                    if (custIdDo != null) commonInfoDTO.setCustId(custIdDo.getPropertyValue());
                }
                if (serviceCode.equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_DETAIL.getKey())) {
                    if (StringUtil.isEmpty(commonInfoDTO.getProjectId())) {
                        CommonInfoProperty projectIdDo = findCommonInfoProperty(serviceCode, infoDO.getId().toString(), "projectId", null, null, null);
                        if (projectIdDo != null) commonInfoDTO.setProjectId(projectIdDo.getPropertyValue());
                    }
                }
                if (StringUtil.isEmpty(billDateStr)) {
                    CommonInfoProperty billDate = findCommonInfoProperty(serviceCode, infoDO.getId().toString(), "billDate", null, null, null);
                    if (billDate != null) {
                        billDateStr = billDate.getPropertyValue();
                    }
                }
            }

            if (StringUtil.isNotEmpty(billDateStr)) {
                Map<String, String> map = new HashMap<>();
                map.put("propertyName", "billDate");
                map.put("propertyValue", billDateStr);
                applyList.add(map);
            }
            if (serviceCode.equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_DETAIL.getKey())) {
                commonInfoDTO.setServiceCode(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_APPLY_MANAGE.getKey());
            } else if (serviceCode.equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_DETAIL.getKey())) {
                commonInfoDTO.setServiceCode(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_APPLY_MANAGE.getKey());
            }
            commonInfoDTO.setStatus("0");
            Map<String, String> map = new HashMap<>();
            map.put("propertyName", "totalSettlementNum");
            map.put("propertyValue", totalSettlement.toString());
//            map.put("totalSettlementNum", totalSettlement.toString());
            applyList.add(map);
            map = new HashMap<>();
            map.put("propertyName", "totalPrice");
            map.put("propertyValue", totalPrice.toString());
//            map.put("totalPrice", totalPrice.toString());
            applyList.add(map);
            map = new HashMap<>();
            String batchNo = IDHelper.getOrderNoByAtomic("");
            map.put("propertyName", "batchNo");
            map.put("propertyValue", batchNo);
            applyList.add(map);
            map = new HashMap<>();
            map.put("propertyName", "remark");
            map.put("propertyValue", remark);
            applyList.add(map);
            commonInfoDTO.setProperties(applyList);
            settingSettlementPrice(commonInfoDTO, lu);

            //结算单状态更新为待付款/项目审单更新状态为待收款
            sql = "update t_common_info set status=1 where id in(" + commitStr + ") and status=0 ";
            jdbcTemplate.execute(sql);

            //回写申请单批次到结算单/回写项目申请单批次到项目审单
            String[] idarr = commitStr.split(",");
            List<CommonInfoProperty> infoPropertyDOS = new ArrayList<>();
            for (String id : idarr) {
                CommonInfoProperty propertyDO = new CommonInfoProperty();
                propertyDO.setZid(Long.valueOf(id));
                propertyDO.setServiceCode(serviceCode);
                propertyDO.setCreateTime(new Date());
                propertyDO.setPropertyName("batchNo");
                propertyDO.setPropertyValue(batchNo);
                infoPropertyDOS.add(propertyDO);
            }
            if (infoPropertyDOS.size() > 0) {
                customerUserDao.batchSaveOrUpdate(infoPropertyDOS);
            }
        }
    }

    public JSONObject getCommitItems(String serviceCode, String pid) {
        JSONObject jsonObject = new JSONObject();
        String commitStr = "";
        List<CommonInfoProperty> list = findCommonInfoPropertyByProperty(serviceCode, "pid", pid);
        for (CommonInfoProperty propertyDO : list) {
            commitStr += "," + propertyDO.getZid();
        }
        if (commitStr != null && commitStr.length() > 0) {
            commitStr = commitStr.substring(1);
        }
        if (StringUtil.isEmpty(commitStr)) {
            jsonObject.put("rows", 0);
            jsonObject.put("total", 0);
            LogUtil.info("getCommitItems：no commitStr");
            return jsonObject;
        }
        String sql = " select * from t_common_info where id in(?) and status=0";
        RowMapper<CommonInfoDO> rowMapper = new BeanPropertyRowMapper<>(CommonInfoDO.class);
        List<CommonInfoDO> commonInfoDOSb = jdbcTemplate.query(sql, rowMapper, commitStr);
        if (commonInfoDOSb != null && commonInfoDOSb.size() > 0) {
            Integer totalSettlement = 0;
            for (CommonInfoDO infoDO : commonInfoDOSb) {
                CommonInfoProperty jobSettlementNumDo = findCommonInfoProperty(serviceCode, infoDO.getId().toString(), "jobSettlementNum", null, null, null);
                CommonInfoProperty typeObj = findCommonInfoProperty(serviceCode, infoDO.getId().toString(), "type", null, null, null);
                if (jobSettlementNumDo != null && jobSettlementNumDo.getPropertyValue() != null) {
                    totalSettlement += (Integer.valueOf(jobSettlementNumDo.getPropertyValue()) * (Integer.valueOf(typeObj.getPropertyValue())));
                }
            }
            jsonObject.put("rows", commonInfoDOSb.size());
            jsonObject.put("total", totalSettlement);
        } else {
            jsonObject.put("rows", 0);
            jsonObject.put("total", 0);
        }
        return jsonObject;
    }


    /**
     * 根据id查询commonInfo对象
     *
     * @param id
     * @return
     */
    public CommonInfoDO findCommonInfoById(Long id) {
        try {
            String sql = "select * from t_common_info where id=? ";
            RowMapper<CommonInfoDO> rowMapper = new BeanPropertyRowMapper<>(CommonInfoDO.class);
            List<CommonInfoDO> list = jdbcTemplate.query(sql, rowMapper, id);
            if (list != null && list.size() > 0) {
                return list.get(0);
            }
//            return jdbcTemplate.queryForObject(sql, CommonInfoDO.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
        return null;
    }

    /**
     * 确认 {职场付款/项目审单收款}时间
     *
     * @param serviceCode
     * @param id
     * @param paymentTime
     * @param remark
     */
    public void confirmpayJobSettlement(String serviceCode, String id, String paymentTime, String remark, LoginUser lu) throws TouchException {
        LogUtil.info("confirmpayJobSettlement:serviceCode=" + serviceCode + ",id=" + id + ",paymentTime=" + paymentTime + ",remark=" + remark + ",loginuser=" + lu.getUsername());
        CommonInfoDO commonInfoDO = findCommonInfoById(Long.valueOf(id));
        if (commonInfoDO == null || commonInfoDO.getStatus() != 0) {
            LogUtil.info("数据不存在或状态不正确");
            throw new TouchException("数据不存在或状态不正确");
        }
        commonInfoDO.setStatus(1);//修改付款状态为已付款/已收款
        customerUserDao.saveOrUpdate(commonInfoDO);

        //添加付款信息到property表
        List<CommonInfoProperty> list = new ArrayList<>();
        CommonInfoProperty propertyDO = new CommonInfoProperty();
        propertyDO.setPropertyName("confirmTime");
        propertyDO.setPropertyValue(String.valueOf(new Date().getTime()));
        propertyDO.setCreateTime(new Date());
        propertyDO.setServiceCode(serviceCode);
        propertyDO.setZid(Long.valueOf(id));
        list.add(propertyDO);

        propertyDO = new CommonInfoProperty();
        propertyDO.setPropertyName("confirmPerson");
        propertyDO.setPropertyValue(lu.getUsername());
        propertyDO.setCreateTime(new Date());
        propertyDO.setServiceCode(serviceCode);
        propertyDO.setZid(Long.valueOf(id));
        list.add(propertyDO);

        propertyDO = new CommonInfoProperty();
        propertyDO.setPropertyName("paymentTime");//到账时间
        propertyDO.setPropertyValue(paymentTime);
        propertyDO.setCreateTime(new Date());
        propertyDO.setServiceCode(serviceCode);
        propertyDO.setZid(Long.valueOf(id));
        list.add(propertyDO);

        propertyDO = new CommonInfoProperty();
        propertyDO.setPropertyName("confirmRemark");//确认备注
        propertyDO.setPropertyValue(remark);
        propertyDO.setCreateTime(new Date());
        propertyDO.setServiceCode(serviceCode);
        propertyDO.setZid(Long.valueOf(id));
        list.add(propertyDO);
        customerUserDao.batchSaveOrUpdate(list);

        //查找并修改申请单中的结算单状态为已付款
        String _servicecode = "";
        if (serviceCode.equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_APPLY_MANAGE.getKey())) {
            _servicecode = CommonInfoServiceCodeEnum.JOB_SETTLEMENT_DETAIL.getKey();
        } else if (serviceCode.equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_APPLY_MANAGE.getKey())) {
            _servicecode = CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_DETAIL.getKey();
        }
        CommonInfoProperty batchNoObj = findCommonInfoProperty(serviceCode, id, "batchNo", null, null, null);
        if (batchNoObj != null && StringUtil.isNotEmpty(batchNoObj.getPropertyValue())) {
            List<CommonInfoProperty> propertyDOList = findCommonInfoPropertyByProperty(_servicecode, "batchNo", batchNoObj.getPropertyValue());
            if (propertyDOList != null && propertyDOList.size() > 0) {
                String idStr = "";
                for (CommonInfoProperty propertyObj : propertyDOList) {
                    idStr += "," + propertyObj.getZid();
                }
                if (idStr != null && idStr.length() > 0) {
                    idStr = idStr.substring(1);

                    String sql = " update t_common_info set status=2 where id in(" + idStr + ") and status=1";
                    jdbcTemplate.execute(sql);
                }
            }
        } else {
            LogUtil.info("未找到对应批次id");
        }
    }

    public void exportDetailList(String serviceCode, String pid, String projectId, String type, String status,
                                 String startTime, String endTime, String operator, String batchNo,
                                 HttpServletResponse response) {
        OutputStream outputStream = null;
        JSONObject jsonObject = new JSONObject();
        try {
            outputStream = response.getOutputStream();

            Page page = jobSettlementDetailList(null, null, pid, serviceCode, projectId, type, status, operator, startTime, endTime, batchNo, false);

            if (page == null || page.getData() == null) {
                jsonObject.put("msg", "无满足条件的数据");
                jsonObject.put("data", "");
                outputStream.write(jsonObject.toJSONString().getBytes());
                outputStream.close();
                return;
            }
            Map<String, List<List<String>>> map = null;
            if (serviceCode.equals(CommonInfoServiceCodeEnum.JOB_SETTLEMENT_DETAIL.getKey())) {
                map = buildJobSettlenmentDetail(page.getData(), pid, batchNo);
            } else if (serviceCode.equals(CommonInfoServiceCodeEnum.PROJECT_SETTLEMENT_DETAIL.getKey())) {
                map = buildProjectDetail(page.getData(), pid, batchNo);
            }
            writerExport(map.get("head"), map.get("data"), response, "详情");
        } catch (Exception e) {
            jsonObject.put("msg", "导出数据出错");
            jsonObject.put("data", "");
            try {
                outputStream.write(jsonObject.toJSONString().getBytes());
                outputStream.close();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
            return;
        }
    }

    public void writerExport(List<List<String>> headers, List<List<String>> data, HttpServletResponse response, String sheetName) throws IOException {
        OutputStream outputStream = response.getOutputStream();
        String fileName = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        final String fileType = ExcelTypeEnum.XLSX.getValue();
        response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
        Sheet sheet = new Sheet(1, 0);
        sheet.setHead(headers);
        if (StringUtil.isNotEmpty(sheetName)) {
            sheet.setSheetName(sheetName);
        }
        writer.write0(data, sheet);
        writer.finish();
        outputStream.close();
    }

    public Map<String, List<List<String>>> buildJobSettlenmentDetail(List<SettlmentDTO> list, String pid, String batchNo) {
        List<List<String>> headers = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        Map<String, List<List<String>>> result = new HashMap<>();
        if (StringUtil.isNotEmpty(pid)) {
            String headarr[] = {"项目", "添加时间", "类型", "结算单数量", "状态", "备注", "操作人"};
            buildHeader(headarr, headers);

            if (list != null && list.size() > 0) {
                for (SettlmentDTO dto : list) {
                    List<String> cols = new ArrayList<>();
                    cols.add(dto.getProjectName() == null ? "" : dto.getProjectName());
                    cols.add(dto.getCreateTime());
                    String type = dto.getType();
                    if ("1".equals(type)) {
                        type = "增加";
                    } else {
                        type = "扣减";
                    }
                    cols.add(type);
                    cols.add(dto.getJobSettlementNum());
                    String status = dto.getStatus();
                    if ("0".equals(status)) {
                        status = "待提交";
                    } else if ("1".equals(status)) {
                        status = "待付款";
                    } else if ("2".equals(status)) {
                        status = "已付款";
                    }
                    cols.add(status);
                    cols.add(dto.getRemark());
                    cols.add(dto.getOperator());
                    data.add(cols);
                }
            }
        } else if (StringUtil.isNotEmpty(batchNo)) {
            String headarr[] = {"项目", "添加时间", "类型", "单价", "结算单数量", "总额", "备注", "操作人"};
            buildHeader(headarr, headers);
            if (list != null && list.size() > 0) {
                for (SettlmentDTO dto : list) {
                    List<String> cols = new ArrayList<>();
                    cols.add(dto.getProjectName() == null ? "" : dto.getProjectName());
                    cols.add(dto.getCreateTime());
                    String type = dto.getType();
                    if ("1".equals(type)) {
                        type = "增加";
                    } else {
                        type = "扣减";
                    }
                    cols.add(type);
                    cols.add(dto.getUnitPrice());
                    cols.add(dto.getJobSettlementNum());
                    BigDecimal total = new BigDecimal(dto.getUnitPrice()).multiply(new BigDecimal(dto.getJobSettlementNum()));
                    cols.add(total.toString());
                    cols.add(dto.getRemark() == null ? "" : dto.getRemark());
                    cols.add(dto.getOperator());
                    data.add(cols);
                }
            }
        }
        result.put("data", data);
        result.put("head", headers);
        return result;
    }

    public Map<String, List<List<String>>> buildProjectDetail(List<SettlmentDTO> list, String pid, String batchNo) {
        List<List<String>> headers = new ArrayList<>();
        List<List<String>> data = new ArrayList<>();
        Map<String, List<List<String>>> result = new HashMap<>();
        if (StringUtil.isNotEmpty(pid)) {
            String headarr[] = {"添加时间", "类型", "状态", "客审数量", "备注", "操作人"};
            buildHeader(headarr, headers);

            if (list != null && list.size() > 0) {
                for (SettlmentDTO dto : list) {
                    List<String> cols = new ArrayList<>();
                    cols.add(dto.getCreateTime());
                    String type = dto.getType();
                    if ("1".equals(type)) {
                        type = "增加";
                    } else {
                        type = "扣减";
                    }
                    cols.add(type);
                    String status = dto.getStatus();
                    if ("0".equals(status)) {
                        status = "待提交";
                    } else if ("1".equals(status)) {
                        status = "待收款";
                    } else if ("2".equals(status)) {
                        status = "已收款";
                    }
                    cols.add(status);
                    cols.add(dto.getJobSettlementNum());
                    cols.add(dto.getRemark());
                    cols.add(dto.getOperator());
                    data.add(cols);
                }
            }
        } else if (StringUtil.isNotEmpty(batchNo)) {
            String headarr[] = {"添加时间", "类型", "客审数量", "备注", "操作人"};
            buildHeader(headarr, headers);
            if (list != null && list.size() > 0) {
                for (SettlmentDTO dto : list) {
                    List<String> cols = new ArrayList<>();
                    cols.add(dto.getCreateTime());
                    String type = dto.getType();
                    if ("1".equals(type)) {
                        type = "增加";
                    } else {
                        type = "扣减";
                    }
                    cols.add(type);
                    cols.add(dto.getJobSettlementNum());
                    cols.add(dto.getRemark() == null ? "" : dto.getRemark());
                    cols.add(dto.getOperator());
                    data.add(cols);
                }
            }
        }
        result.put("data", data);
        result.put("head", headers);
        return result;
    }

    public List<List<String>> buildHeader(String[] headarr, List<List<String>> headers) {
        for (String headStr : headarr) {
            List<String> head = new ArrayList<>();
            head.add(headStr);
            headers.add(head);
        }
        return headers;
    }


}
