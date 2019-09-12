package com.bdaim.customersea.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.SqlAppendUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerUserGroupRelDTO;
import com.bdaim.customersea.dto.CustomerSeaParam;
import com.bdaim.common.dto.Page;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.customersea.entity.CustomerSeaProperty;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/19
 * @description
 */
@Component
public class CustomerSeaDao extends SimpleHibernateDao<CustomerSea, Long> {

    @Resource
    private CustomerUserDao customerUserDao;

    public CustomerSeaProperty getProperty(String customerSeaId, String propertyName) {
        CustomerSeaProperty cp = null;
        String hql = "from CustomerSeaProperty m where m.customerSeaId=? and m.propertyName=?";
        List<CustomerSeaProperty> list = this.find(hql, customerSeaId, propertyName);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    /**
     * 公海下的所有属性
     *
     * @param customerSeaId
     * @return
     */
    public List<CustomerSeaProperty> listProperty(String customerSeaId) {
        String hql = "from CustomerSeaProperty m where m.customerSeaId=?";
        List<CustomerSeaProperty> list = this.find(hql, customerSeaId);
        return list;
    }

    /**
     * 公海分页
     *
     * @param param
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page pageCustomerSea(CustomerSeaParam param, int pageNum, int pageSize) {
        StringBuilder hql = new StringBuilder("from CustomerSea m where 1=1");
        List<Object> params = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getCustId())) {
            hql.append(" AND m.custId = ?");
            params.add(param.getCustId());
        }
        if (StringUtil.isNotEmpty(param.getName())) {
            hql.append(" AND m.name LIKE %?% ");
            params.add(param.getName());
        }
        if (StringUtil.isNotEmpty(param.getMarketProjectName())) {
            hql.append(" AND m.marketProjectId = (SELECT id FROM MarketProject WHERE name LIKE %?%)");
            params.add(param.getMarketProjectName());
        }
        if (param.getMarketProjectId() != null) {
            hql.append(" AND m.marketProjectId =?");
            params.add(param.getMarketProjectId());
        }
        // 项目管理员
        if ("3".equals(param.getUserType())) {
            List<String> projects = customerUserDao.listProjectByUserId(param.getUserId());
            hql.append(" AND m.marketProjectId IN (").append(SqlAppendUtil.sqlAppendWhereIn(projects)).append(")");
        }
        // 员工 组长只查询手动领取、机器人外呼类型,状态有效,并且所在该执行组
        if ("2".equals(param.getUserType())) {
            hql.append(" AND m.status = 1 ");
            CustomerUserGroupRelDTO group = customerUserDao.getCustomerUserGroupByUserId(param.getUserId());
            hql.append(" AND FIND_IN_SET(?,(SELECT propertyValue from MarketProjectProperty WHERE marketProjectId = m.marketProjectId)) >0 ");
            if (StringUtil.isNotEmpty(param.getType()) && "2".equals(param.getType())) {
                hql.append(" AND (m.taskType = 2 OR m.taskType = 1 OR (m.taskType = 3 AND m.id = (SELECT customerSeaId from CustomerSeaProperty WHERE customerSeaId = m.id AND propertyName=? AND propertyValue = ?)))");
            } else {
                hql.append(" AND (m.taskType = 2 OR (m.taskType = 3 AND m.id = (SELECT customerSeaId from CustomerSeaProperty WHERE customerSeaId = m.id AND propertyName=? AND propertyValue = ?)))");
            }
            if (group != null) {
                params.add(group.getGroupId());
            } else {
                params.add("");
            }
            params.add("isPersonFollow");
            params.add("1");
        }
        hql.append(" ORDER BY m.createTime DESC ");
        return this.page(hql.toString(), params, pageNum, pageSize);
    }

    /**
     * 公海列表
     *
     * @param param
     * @return
     */
    public List<CustomerSea> listCustomerSea(CustomerSeaParam param) {
        StringBuilder hql = new StringBuilder("from CustomerSea m where 1=1");
        List<Object> params = new ArrayList<>();
        if (StringUtil.isNotEmpty(param.getCustId())) {
            hql.append(" AND m.custId = ?");
            params.add(param.getCustId());
        }
        if (StringUtil.isNotEmpty(param.getName())) {
            hql.append(" AND m.name LIKE %?% ");
            params.add(param.getName());
        }
        if (StringUtil.isNotEmpty(param.getMarketProjectName())) {
            hql.append(" AND m.marketProjectId = (SELECT id FROM MarketProject WHERE name LIKE %?%)");
            params.add(param.getMarketProjectName());
        }
        if (param.getMarketProjectId() != null) {
            hql.append(" AND m.marketProjectId =?");
            params.add(param.getMarketProjectId());
        }
        if (param.getTaskType() != null) {
            hql.append(" AND m.taskType =?");
            params.add(param.getTaskType());
        }
        // 项目管理员
        if ("3".equals(param.getUserType())) {
            List<String> projects = customerUserDao.listProjectByUserId(param.getUserId());
            hql.append(" AND m.marketProjectId IN (").append(SqlAppendUtil.sqlAppendWhereIn(projects)).append(")");
        }
        // 员工 组长只查询手动领取、机器人外呼类型,状态有效,并且所在该执行组
        if ("2".equals(param.getUserType())) {
            hql.append(" AND m.taskType IN(2,3)");
            hql.append(" AND m.status = 1");
            CustomerUserGroupRelDTO group = customerUserDao.getCustomerUserGroupByUserId(param.getUserId());
            hql.append(" AND FIND_IN_SET(?,(SELECT propertyValue from MarketProjectProperty WHERE marketProjectId = m.marketProjectId)) >0");
            params.add(group.getGroupId());
        }
        return this.find(hql.toString(), params);
    }

    /**
     * 更改公海状态
     *
     * @param custId
     * @param marketProjectId
     * @param status
     * @return
     */
    public int updateCustomerSea(String custId, int marketProjectId, int status) {
        StringBuilder hql = new StringBuilder();
        hql.append(" UPDATE FROM CustomerSea SET status = ? WHERE custId =? AND marketProjectId=?");
        return this.batchExecute(hql.toString(), status, custId, marketProjectId);
    }

    /**
     * 根据第三方任务ID查询单个公海(按照创建时间倒叙获取第一条)
     *
     * @param taskId
     * @return
     */
    public CustomerSea getCustomerSeaByTaskId(String taskId) {
        CustomerSea cp = null;
        String hql = "from CustomerSea m where m.taskId=? ORDER BY m.createTime DESC";
        List<CustomerSea> list = this.find(hql, taskId);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

}
