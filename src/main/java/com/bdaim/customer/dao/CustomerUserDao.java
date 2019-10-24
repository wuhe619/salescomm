package com.bdaim.customer.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.customer.dto.CustomerUserDTO;
import com.bdaim.customer.dto.CustomerUserGroupRelDTO;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserGroup;
import com.bdaim.customer.entity.CustomerUserGroupRel;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CustomerUserDao extends SimpleHibernateDao<CustomerUser, Serializable> {
    public CustomerUserPropertyDO getProperty(String userId, String propertyName) {
        CustomerUserPropertyDO cp = null;
        String hql = "from CustomerUserPropertyDO m where m.userId=? and m.propertyName=?";
        List<CustomerUserPropertyDO> list = this.find(hql, userId, propertyName);
        if (list.size() > 0)
            cp = (CustomerUserPropertyDO) list.get(0);
        return cp;
    }

    public CustomerUser getUserByAccount(String account) {
        CustomerUser cp = null;
        String hql = "from CustomerUser m where m.account=? and m.status<>2";
        List<CustomerUser> list = this.find(hql, account);
        if (list.size() > 0)
            cp = (CustomerUser) list.get(0);
        return cp;
    }

    public List<CustomerUserPropertyDO> getAllProperty(String userId) {
        String hql = "from CustomerUserPropertyDO m where m.userId=? ";
        List<CustomerUserPropertyDO> list = this.find(hql, userId);
        return list;
    }

    public List<CustomerUser> getPropertyByType(int userType, String custId) {
        CustomerUser cp = null;
        String hql = "from CustomerUser m where m.userType=? and m.cust_id=?";
        List<CustomerUser> list = this.find(hql, userType, custId);
        return list;
    }

    public CustomerUser selectPropertyByType(int userType, String custId) {
        CustomerUser cp = null;
        String hql = "from CustomerUser m where m.userType=? and m.cust_id=?";
        List<CustomerUser> list = this.find(hql, userType, custId);
        if (list.size() > 0)
            cp = (CustomerUser) list.get(0);
        return cp;
    }

    public CustomerUser getPropertyByCustId(String custId) {
        CustomerUser cp = null;
        String hql = "from CustomerUser m where m.userType=1 and m.cust_id=?";
        List<CustomerUser> list = this.find(hql, custId);
        if (list.size() > 0)
            cp = (CustomerUser) list.get(0);
        return cp;
    }

    public CustomerUser getCustomer(String account, String custId) {
        CustomerUser cp = null;
        String hql = "from CustomerUser m where m.account=? AND m.cust_id = ?";
        List<CustomerUser> list = this.find(hql, account, custId);
        if (list.size() > 0)
            cp = (CustomerUser) list.get(0);
        return cp;
    }

    public List<CustomerUser> getAllByCustId(String custId) {
        CustomerUser cp = null;
        String hql = "from CustomerUser m where m.cust_id=? and m.status!=2 ";
        List<CustomerUser> list = this.find(hql, custId);
        return list;
    }

    public String getName(String userId) {
        try {
            CustomerUser cu = this.get(Long.parseLong(userId));
            if (cu != null)
                return cu.getRealname();
        } catch (Exception e) {

        }
        return "";
    }

    public String getLoginName(String userId) {
        try {
            CustomerUser cu = this.get(Long.parseLong(userId));
            if (cu != null)
                return cu.getAccount();
        } catch (Exception e) {

        }
        return "";
    }


    public CustomerUser getCustomerUserByLoginName(String loginName) {
        CustomerUser cp = null;
        String hql = "from CustomerUser m where m.account=?";
        List<CustomerUser> list = this.find(hql, loginName);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    public CustomerUser getCustomerUserByName(String realName) {
        CustomerUser cp = null;
        String hql = "from CustomerUser m where m.realname=?";
        List<CustomerUser> list = this.find(hql, realName);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    public CustomerUser getCustomerAdminUser(String custId) {
        CustomerUser cp = null;
        String hql = "from CustomerUser m where m.cust_id=? AND userType =1 ";
        List<CustomerUser> list = this.find(hql, custId);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    /**
     * 获取客户下的用户(包含管理员)
     *
     * @param customerId
     * @return
     */
    public List<CustomerUserDTO> listUserByCustomerId(String customerId) {
        if (StringUtil.isEmpty(customerId)) {
            throw new NullPointerException("customerId不能为空");
        }
        String hql = "FROM CustomerUser WHERE cust_id = ? AND STATUS = 0 ";
        List<CustomerUserDTO> list = new ArrayList<>();
        List<CustomerUser> customerUserList = this.find(hql, customerId);
        for (CustomerUser customerUser : customerUserList) {
            list.add(new CustomerUserDTO(customerUser));
        }
        return list;
    }

    /**
     * 获取用户组分页列表
     *
     * @param pageNum
     * @param pageSize
     * @param userGroupEntity
     * @return
     */
    public Page pageCustomerUserGroup(int pageNum, int pageSize, CustomerUserGroup userGroupEntity) {
        List<Object> params = new ArrayList<>();
        params.add(userGroupEntity.getCustId());
        StringBuilder sql = new StringBuilder();
        sql.append(" FROM CustomerUserGroup t WHERE t.status = 1 AND t.custId = ? ");
        if (StringUtil.isNotEmpty(userGroupEntity.getId())) {
            sql.append(" AND t.id = ? ");
            params.add(userGroupEntity.getId());
        }
        if (StringUtil.isNotEmpty(userGroupEntity.getName())) {
            sql.append(" AND t.name = ? ");
            params.add(userGroupEntity.getName());
        }
        if (StringUtil.isNotEmpty(userGroupEntity.getPid())) {
            sql.append(" AND t.pid = ? ");
            params.add(userGroupEntity.getPid());
        }
        if (userGroupEntity.getLeavel() != null) {
            sql.append(" AND t.leavel = ? ");
            params.add(userGroupEntity.getLeavel());
        }
        if ("0".equals(userGroupEntity.getLeavel().toString())) {
            if (StringUtil.isNotEmpty(userGroupEntity.getProvince())) {
                sql.append(" AND t.province = ? ");
                params.add(userGroupEntity.getProvince());
            }
            if (StringUtil.isNotEmpty(userGroupEntity.getCity())) {
                sql.append(" AND t.city = ? ");
                params.add(userGroupEntity.getCity());
            }
        }
        if (userGroupEntity.getStatus() != null) {
            sql.append(" AND t.status = ? ");
            params.add(userGroupEntity.getStatus());
        }
        sql.append(" ORDER BY t.createTime DESC ");
        return this.page(sql.toString(), params, pageNum, pageSize);
    }

    public List<CustomerUserGroup> listCustomerUserGroup(CustomerUserGroup userGroupEntity) {
        List<Object> params = new ArrayList<>();
        params.add(userGroupEntity.getCustId());
        StringBuilder sql = new StringBuilder();
        sql.append(" FROM CustomerUserGroup t WHERE t.status = 1 AND t.custId = ? ");
        if (StringUtil.isNotEmpty(userGroupEntity.getId())) {
            sql.append(" AND t.id = ? ");
            params.add(userGroupEntity.getId());
        }
        if (StringUtil.isNotEmpty(userGroupEntity.getName())) {
            sql.append(" AND t.name = ? ");
            params.add(userGroupEntity.getName());
        }
        if (userGroupEntity.getLeavel() != null) {
            sql.append(" AND t.leavel = ? ");
            params.add(userGroupEntity.getLeavel());
        }
        return this.find(sql.toString(), params.toArray());
    }

    public CustomerUserGroup getCustomerUserGroup(String groupId) {
        List<CustomerUserGroup> list = this.find(" FROM CustomerUserGroup t WHERE t.status = 1 AND t.id = ? ", groupId);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public CustomerUserGroup findCustomerUserGroup(String groupId) {
        List<CustomerUserGroup> list = this.find(" FROM CustomerUserGroup t WHERE t.id = ? and status = 1", groupId);
        if (list != null && list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 更新用户组
     *
     * @param customerUserGroup
     * @return
     */
    public int updateCustomerUserGroup(CustomerUserGroup customerUserGroup) {
        if (StringUtil.isEmpty(customerUserGroup.getId())) {
            throw new NullPointerException("Id不能为空");
        }
        if (null == customerUserGroup.getLeavel() || "".equals(customerUserGroup.getLeavel().toString())) {
            throw new NullPointerException("leavel不能为空");
        }
        if (null != customerUserGroup.getLeavel() && "1".equals(customerUserGroup.getLeavel().toString())) {
            if (StringUtil.isEmpty(customerUserGroup.getPid())) {
                throw new NullPointerException("pid不能为空");
            }
        }
        List<CustomerUserGroup> list = this.find("FROM CustomerUserGroup t WHERE t.status = 1 AND t.id = ?", customerUserGroup.getId());
        if (list != null && list.size() > 0) {
            CustomerUserGroup model = list.get(0);
            if (StringUtil.isNotEmpty(customerUserGroup.getName())) {
                model.setName(customerUserGroup.getName());
            }
            if (null != customerUserGroup.getLeavel() && 0 == customerUserGroup.getLeavel()) {
                model.setLeavel(customerUserGroup.getLeavel());
            }
            if (StringUtil.isNotEmpty(customerUserGroup.getPid())) {
                model.setPid(customerUserGroup.getPid());
            }
            if (null != customerUserGroup.getStatus()) {
                model.setStatus(customerUserGroup.getStatus());
            }
            try {
                this.saveOrUpdate(model);
                return 1;
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }

    /**
     * 删除用户组
     *
     * @param customerUserGroup
     * @return
     */
    public int deleteCustomerUserGroup(CustomerUserGroup customerUserGroup) {
        if (StringUtil.isEmpty(customerUserGroup.getId())) {
            throw new NullPointerException("用户群组Id不能为空");
        }
        List<CustomerUserGroup> list = this.find("FROM CustomerUserGroup t WHERE t.status = 1 AND t.id = ? ", customerUserGroup.getId());
        if (list != null && list.size() > 0) {
            try {
                CustomerUserGroup model = list.get(0);
                //用户群组状态改为无效
                model.setStatus(2);
                this.saveOrUpdate(model);
                if (null != model.getLeavel() && model.getLeavel() == 1) {
                    //逻辑删除用户群组关联表
                    this.deleteCustomerUserRelGroup(customerUserGroup.getId());
                }
                return 1;
            } catch (Exception e) {
                return 0;
            }
        }
        return 0;
    }


    /**
     * 获取用户组下的用户
     *
     * @param groupId
     * @param customerId
     * @return
     */
    public List<CustomerUserDTO> listSelectCustomerUserByUserGroupId(String groupId, String customerId, String startAccount, String endAccount) {
        if (StringUtil.isEmpty(groupId)) {
            throw new NullPointerException("groupId不能为空");
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT new com.bdaim.customer.dto.CustomerUserDTO(t.userId AS id, user.realname, user.account) FROM CustomerUserGroupRel t, CustomerUser user");
        sql.append(" WHERE user.id = t.userId AND t.groupId = (SELECT id FROM CustomerUserGroup WHERE status = 1 AND custId = ? AND id= ?) AND t.status = 1 ");
        // 开始搜索不为空
        if (StringUtil.isNotEmpty(startAccount) && StringUtil.isEmpty(endAccount)) {
            sql.append(" AND user.account LIKE '%" + startAccount + "%'");
        } else if (StringUtil.isEmpty(startAccount) && StringUtil.isNotEmpty(endAccount)) {
            // 结束搜索不为空
            sql.append(" AND user.account LIKE '%" + endAccount + "%'");
        } else if (StringUtil.isNotEmpty(startAccount) && StringUtil.isNotEmpty(endAccount)) {
            // 都不为空
            int start = NumberConvertUtil.parseInt(startAccount);
            int end = NumberConvertUtil.parseInt(endAccount);
            if (start >= 0 && end > 0) {
                sql.append(" AND ( ");
                for (int index = start; index <= end; index++) {
                    sql.append(" user.account LIKE '%" + index + "%' OR");
                }
                sql.delete(sql.length() - 2, sql.length());
                sql.append(")");
            }
        }
        List<CustomerUserDTO> customerUserList = this.find(sql.toString(), customerId, groupId);
        return customerUserList;
    }

    /**
     * 获取用户组下的用户
     *
     * @param groupId
     * @param customerId
     * @return
     */
    public List<CustomerUserDTO> listSelectCustomerUserByUserGroupId(String groupId, String customerId) {
        if (StringUtil.isEmpty(groupId)) {
            throw new NullPointerException("groupId不能为空");
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT new com.bdaim.customer.dto.CustomerUserDTO(t.userId AS id, user.realname, user.account) FROM CustomerUserGroupRel t, CustomerUser user");
        sql.append(" WHERE user.id = t.userId AND t.groupId = (SELECT id FROM CustomerUserGroup WHERE status = 1 AND custId = ? AND id= ?) AND t.status = 1");
        List<CustomerUserDTO> customerUserList = this.find(sql.toString(), customerId, groupId);
        return customerUserList;
    }

    /**
     * 查询客户下未归属任何分组的员工
     *
     * @param customerId
     * @return
     */
    public List<CustomerUserDTO> listNotInUserGroupByCustomerId(String customerId, String startAccount, String endAccount) {
        if (StringUtil.isEmpty(customerId)) {
            throw new NullPointerException("customerId不能为空");
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT new com.bdaim.customer.dto.CustomerUserDTO( user.id, user.realname, user.account) FROM CustomerUser user");
        sql.append(" WHERE user.cust_id = ? AND user.userType = '2' AND user.id NOT IN " +
                "    (SELECT cast(rel.userId AS integer) FROM CustomerUserGroupRel rel WHERE rel.status = 1 AND rel.groupId IN(SELECT t2.id FROM CustomerUserGroup t2 WHERE t2.custId = user.cust_id AND t2.status = 1) )");
        // 开始搜索不为空
        if (StringUtil.isNotEmpty(startAccount) && StringUtil.isEmpty(endAccount)) {
            sql.append(" AND user.account LIKE '%" + startAccount + "%'");
        } else if (StringUtil.isEmpty(startAccount) && StringUtil.isNotEmpty(endAccount)) {
            // 结束搜索不为空
            sql.append(" AND user.account LIKE '%" + endAccount + "%'");
        } else if (StringUtil.isNotEmpty(startAccount) && StringUtil.isNotEmpty(endAccount)) {
            // 都不为空
            int start = NumberConvertUtil.parseInt(startAccount);
            int end = NumberConvertUtil.parseInt(endAccount);
            if (start >= 0 && end > 0) {
                sql.append(" AND ( ");
                for (int index = start; index <= end; index++) {
                    sql.append(" user.account LIKE '%" + index + "%' OR");
                }
                sql.delete(sql.length() - 2, sql.length());
                sql.append(")");
            }
        }
        List<CustomerUserDTO> customerUserList = this.find(sql.toString(), customerId);
        return customerUserList;
    }

    public List<CustomerUserDTO> listNotInUserGroupByCustomerId(String customerId) {
        if (StringUtil.isEmpty(customerId)) {
            throw new NullPointerException("customerId不能为空");
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT new com.bdaim.customer.dto.CustomerUserDTO( user.id, user.realname, user.account) FROM CustomerUser user");
        sql.append(" WHERE user.cust_id = ? AND user.userType = '2' AND user.id NOT IN (SELECT cast(rel.userId AS integer) FROM CustomerUserGroupRel rel WHERE rel.status = 1 AND rel.groupId IN(SELECT t2.id FROM CustomerUserGroup t2 WHERE t2.custId = user.cust_id AND t2.status = 1) )");
        List<CustomerUserDTO> customerUserList = this.find(sql.toString(), customerId);
        return customerUserList;
    }

    /**
     * 获取分组下的员工总数
     *
     * @param groupId
     * @param customerId
     * @return
     */
    public int countSelectCustomerUserByUserGroupId(String groupId, String customerId) {
        if (StringUtil.isEmpty(groupId)) {
            throw new NullPointerException("groupId不能为空");
        }
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT count(*) FROM CustomerUserGroupRel t, CustomerUser user");
        sql.append(" WHERE user.id = t.userId AND t.groupId = (SELECT id FROM CustomerUserGroup WHERE status = 1 AND custId = ? AND id= ?) AND t.status = 1");
        return this.findCount(sql.toString(), customerId, groupId);
    }

    /**
     * 删除用户组下的所有用户
     *
     * @param groupId
     * @return
     */
    public int deleteCustomerUserRelGroup(String groupId) {
        if (StringUtil.isEmpty(groupId)) {
            throw new NullPointerException("用户群组Id不能为空");
        }
        return this.batchExecute("DELETE FROM CustomerUserGroupRel t WHERE t.groupId = ?", groupId);
    }

    public CustomerUserGroupRel getCustomerUserGroupRel(String groupId, String userId) {
        CustomerUserGroupRel customerUserGroupRel = null;
        List<CustomerUserGroupRel> list = this.find(" FROM CustomerUserGroupRel t WHERE t.groupId = ? AND t.userId = ? ", groupId, userId);
        if (list.size() > 0)
            customerUserGroupRel = list.get(0);
        return customerUserGroupRel;
    }

    /**
     * 查询用户组的组长
     *
     * @param groupId
     * @return
     */
    public CustomerUserGroupRel getCustomerUserGroupLeader(String groupId) {
        CustomerUserGroupRel customerUserGroupRel = null;
        List<CustomerUserGroupRel> list = this.find(" FROM CustomerUserGroupRel t WHERE t.groupId = ? AND t.type =1", groupId);
        if (list.size() > 0)
            customerUserGroupRel = list.get(0);
        return customerUserGroupRel;
    }

    /**
     * 根据用户ID查询所属用户组信息
     *
     * @param userId
     * @return
     */
    public CustomerUserGroupRelDTO getCustomerUserGroupByUserId(long userId) {
        CustomerUserGroupRelDTO customerUserGroupRelDTO = null;
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT new com.bdaim.customer.dto.CustomerUserGroupRelDTO(t.groupId, t2.name, t.type, t2.pid) FROM CustomerUserGroupRel t, CustomerUserGroup t2");
        sql.append(" WHERE t.groupId = t2.id AND t.userId = ? AND t.status = 1 AND t2.status = 1");
        List<CustomerUserGroupRelDTO> list = this.find(sql.toString(), String.valueOf(userId));
        if (list.size() > 0) {
            customerUserGroupRelDTO = list.get(0);
        }
        return customerUserGroupRelDTO;
    }


    public List<CustomerUserPropertyDO> getPropertiesByUserId(String userId) {
        String hql = "from CustomerUserPropertyDO m where m.userId=? ";
        List<CustomerUserPropertyDO> list = this.find(hql, userId);
        return list;
    }

    /**
     * 根据用户查询负责项目下的所有客群ID
     *
     * @param userId
     * @return
     */
    public List<String> listCustGroupByUserId(Long userId) {
        CustomerUser user = this.get(userId);
        if (user == null) {
            return null;
        }
        CustomerUserPropertyDO property = this.getProperty(String.valueOf(userId), "hasMarketProject");
        if (property == null || StringUtil.isEmpty(property.getPropertyValue())) {
            return null;
        }
        List<String> projectIds = new ArrayList<>();
        for (String p : property.getPropertyValue().split(",")) {
            if (StringUtil.isNotEmpty(p)) {
                projectIds.add(p);
            }
        }
        String sql = "SELECT id FROM customer_group t where t.market_project_id in (" + SqlAppendUtil.sqlAppendWhereIn(projectIds) + ") AND t.cust_id = ? ";
        List<Map<String, Object>> cgs = this.sqlQuery(sql, user.getCust_id());
        List<String> ids = new ArrayList<>();
        for (Map<String, Object> c : cgs) {
            ids.add(String.valueOf(c.get("id")));
        }
        return ids;
    }

    /**
     * 根据用户查询负责项目ID
     *
     * @param userId
     * @return
     */
    public List<String> listProjectByUserId(Long userId) {
        CustomerUser user = this.get(userId);
        if (user == null) {
            return null;
        }
        CustomerUserPropertyDO property = this.getProperty(String.valueOf(userId), "hasMarketProject");
        if (property == null || StringUtil.isEmpty(property.getPropertyValue())) {
            return null;
        }
        List<String> ids = new ArrayList<>();
        for (String p : property.getPropertyValue().split(",")) {
            if (StringUtil.isNotEmpty(p)) {
                ids.add(p);
            }
        }
        return ids;
    }

    /**
     * 查询职场下的所有用户ID
     *
     * @param workPlaceId
     * @return
     */
    public List<String> listUserIdByWorkPlaceId(String workPlaceId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" SELECT new com.bdaim.customer.dto.CustomerUserGroupRelDTO(t.groupId, t.userId) FROM CustomerUserGroupRel t, CustomerUserGroup t2");
        sql.append(" WHERE t.groupId = t2.id AND t.status = 1 AND t2.status = 1 AND t2.pid =? ");
        List<CustomerUserGroupRelDTO> list = this.find(sql.toString(), workPlaceId);
        List<String> ids = new ArrayList<>();
        if (list.size() > 0) {
            for (CustomerUserGroupRelDTO dto : list) {
                ids.add(dto.getUserId());
            }
        }
        return ids;
    }
}
