package com.bdaim.markettask.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.dto.CustomerUserDTO;
import com.bdaim.customgroup.entity.CustomGroupDO;
import com.bdaim.markettask.entity.MarketTask;
import com.bdaim.markettask.entity.MarketTaskProperty;
import com.bdaim.markettask.entity.MarketTaskUserRel;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/23
 * @description
 */
@Component
public class MarketTaskDao extends SimpleHibernateDao<MarketTask, String> {

    public MarketTaskProperty getProperty(String marketTaskId, String propertyName) {
        MarketTaskProperty cp = null;
        String hql = "from MarketTaskProperty m where m.marketTaskId=? and m.propertyName=?";
        List<MarketTaskProperty> list = this.find(hql, marketTaskId, propertyName);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    public MarketTaskUserRel getMarketTaskUserRel(String marketTaskId, String userId) {
        MarketTaskUserRel cp = null;
        String hql = "from MarketTaskUserRel m where m.marketTaskId=? and m.userId=? AND status = 1";
        List<MarketTaskUserRel> list = this.find(hql, marketTaskId, userId);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    /**
     * 根据第三方任务ID查询单个营销任务(按照创建时间倒叙获取第一条)
     *
     * @param taskId
     * @return
     */
    public MarketTask getMarketTaskByTaskId(String taskId) {
        MarketTask cp = null;
        String hql = "from MarketTask m where m.taskId=? ORDER BY m.createTime DESC";
        List<MarketTask> list = this.find(hql, taskId);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    /**
     * 根据客群获取营销任务列表
     *
     * @param customerGroupId
     * @return
     */
    public List<MarketTask> listMarketTaskByCgId(int customerGroupId) {
        MarketTask cp = null;
        String hql = "from MarketTask m where m.customerGroupId=? ORDER BY m.createTime DESC";
        List<MarketTask> list = this.find(hql, customerGroupId);
        return list;
    }

    /**
     * 根据项目查询营销任务列表
     * @param projectId
     * @return
     */
    public List<MarketTask> listMarketTaskByProjectId(int projectId) {
        String hql = "from MarketTask m where m.customerGroupId IN (SELECT id FROM CustomGroupDO WHERE marketProjectId = ?) ORDER BY m.createTime DESC";
        List<MarketTask> list = this.find(hql, projectId);
        return list;
    }


    public MarketTask getMarketTaskById(String id) {
        MarketTask cp = null;
        String hql = "from MarketTask m where m.id=? ";
        List<MarketTask> list = this.find(hql, id);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    /**
     * 根据营销任务ID获取客群信息
     *
     * @param marketTaskId
     * @return
     */
    public CustomGroupDO getCustomGroupByMarketTaskId(String marketTaskId) {
        CustomGroupDO cp = null;
        String hql = "from CustomGroupDO m where m.id = (SELECT customerGroupId FROM MarketTask WHERE id = ?) ";
        List<CustomGroupDO> list = this.find(hql, marketTaskId);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    /**
     * 获取客群下营销任务数量
     *
     * @param customGroupId
     * @return
     */
    public int countMarketTaskByCGroup(int customGroupId) {
        String hql = "SELECT count(*) FROM MarketTask m WHERE m.customerGroupId = ? ";
        return findCount(hql, customGroupId);
    }

    /**
     * 获取营销任务下的用户列表
     *
     * @param marketTaskId
     * @param status
     * @return
     */
    public List<MarketTaskUserRel> listMarketTaskUser(String marketTaskId, int status) {
        String hql = "from MarketTaskUserRel m where m.marketTaskId=? AND status = ?";
        List<MarketTaskUserRel> list = this.find(hql, marketTaskId, status);
        return list;
    }

    public int deleteMarketTaskUser(String marketTaskId, String userId) {
        String hql = "DELETE FROM MarketTaskUserRel m where m.marketTaskId=? AND userId = ?";
        return this.batchExecute(hql, marketTaskId, userId);
    }

    /**
     * 查询营销任务下配置的用户列表
     *
     * @param marketTaskId
     * @param status
     * @param custId
     * @return
     */
    public List<CustomerUserDTO> listMarketTaskUser(String marketTaskId, int status, String custId, String startAccount, String endAccount) {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT new com.bdaim.sale.dto.CustomerUserDTO(t.userId AS id, user.realname, user.account, p.propertyValue) FROM MarketTaskUserRel t, CustomerUser user, CustomerUserPropertyDO p ");
        hql.append(" WHERE user.id = t.userId AND user.id = p.userId AND p.propertyName = 'seats_account' AND t.marketTaskId = ? AND t.status = ? AND user.cust_id = ? ");
        // 开始搜索不为空
        if (StringUtil.isNotEmpty(startAccount) && StringUtil.isNotEmpty(endAccount)) {
            hql.append(" AND cast(p.propertyValue AS integer) >= " + startAccount + " AND cast(p.propertyValue AS integer) <= " + endAccount);
        } else if (StringUtil.isNotEmpty(startAccount)) {
            hql.append(" AND p.propertyValue LIKE '%" + startAccount + "%'");
        } else if (StringUtil.isNotEmpty(endAccount)) {
            hql.append(" AND p.propertyValue LIKE '%" + endAccount + "%'");
        }
        List<CustomerUserDTO> list = find(hql.toString(), marketTaskId, status, custId);
        return list;
    }

    /**
     * 查询客户下配置的指定呼叫渠道,但不属于该营销任务的用户列表
     *
     * @param resourceId
     * @param marketTaskId
     * @param custId
     * @param startAccount
     * @param endAccount
     * @return
     */
    public List<CustomerUserDTO> listNotInUserByResourceId(String resourceId, String marketTaskId, String custId, String startAccount, String endAccount) {
        StringBuilder hql = new StringBuilder();
        hql.append(" SELECT new com.bdaim.sale.dto.CustomerUserDTO(user.id, user.realname, user.account, p.propertyValue) FROM CustomerUser user, CustomerUserPropertyDO p ")
                .append(" WHERE user.id = p.userId AND user.userType=2 AND p.propertyName = 'seats_account' AND user.cust_id = ? ")
                .append(" AND user.id IN (SELECT userId FROM CustomerUserPropertyDO WHERE propertyName = 'call_channel' AND propertyValue = ? ) ");
        if (StringUtil.isNotEmpty(marketTaskId)) {
            hql.append(" AND user.id NOT IN (SELECT cast(userId AS integer) FROM MarketTaskUserRel WHERE status = 1 AND marketTaskId = '" + marketTaskId + "' ) ");
        }
        // 都不为空
        if (StringUtil.isNotEmpty(startAccount) && StringUtil.isNotEmpty(endAccount)) {
            hql.append(" AND cast(p.propertyValue AS integer) >= " + startAccount + " AND cast(p.propertyValue AS integer) <= " + endAccount);
        } else if (StringUtil.isNotEmpty(startAccount)) {
            hql.append(" AND p.propertyValue LIKE '%" + startAccount + "%'");
        } else if (StringUtil.isNotEmpty(endAccount)) {
            hql.append(" AND p.propertyValue LIKE '%" + endAccount + "%'");
        }
        List<CustomerUserDTO> list = find(hql.toString(), custId, resourceId);
        return list;
    }
}
