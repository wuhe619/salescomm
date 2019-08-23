package com.bdaim.customgroup.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.ConstantsUtil;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customgroup.dto.CustomGroupDTO;
import com.bdaim.customgroup.entity.CustomGroupDO;
import com.bdaim.customgroup.entity.CustomerGroupPropertyDO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class CustomGroupDao extends SimpleHibernateDao<CustomGroupDO, Serializable> {

    private static Logger LOG = LoggerFactory.getLogger(CustomGroupDao.class);

    public CustomerGroupPropertyDO getProperty(int customerGroupId, String propertyName) {
        CustomerGroupPropertyDO cp = null;
        String hql = "from CustomerGroupPropertyDO m where m.customerGroupId=? and m.propertyName=?";
        List<CustomerGroupPropertyDO> list = this.find(hql, customerGroupId, propertyName);
        if (list.size() > 0) {
            cp = list.get(0);
        }
        return cp;
    }

    public CustomerGroupPropertyDO getProperty(String propertyName, String propertyValue) {
        CustomerGroupPropertyDO cp = null;
        String hql = " FROM CustomerGroupPropertyDO m WHERE m.propertyName = ? AND m.propertyValue = ? ";
        List<CustomerGroupPropertyDO> list = this.find(hql, propertyName, propertyValue);
        if (list.size() > 0) {
            cp = list.get(0);
        }
        return cp;
    }

    /**
     * 判断客户下是否购买过相同标签条件的客户群
     *
     * @param custId
     * @param groupCondition
     * @param status
     * @return
     */
    public int selectCGoupOrderStatus(String custId, String groupCondition, int status) {
        //判断当前用户是否存在相同条件的待支付的客户群
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT count(*) as count ");
        sb.append(" FROM `customer_group` c ");
        sb.append(" LEFT JOIN t_order t ON t.order_id = c.order_id ");
        sb.append(" WHERE t.order_state = 1 ");
        sb.append(" AND c.`STATUS` =? ");
        sb.append(" AND c.cust_id= ? ");
        sb.append(" AND c.group_condition=? ");
        List<Map<String, Object>> list = this.sqlQuery(sb.toString(), status, custId, groupCondition);
        if (list.size() > 0) {
            return NumberConvertUtil.parseInt(list.get(0).get("count"));
        }
        return 0;
    }

    /**
     * 查询客户群数据总数
     *
     * @param groupId
     * @return
     */
    public long getCustomerGroupListDataCount(int groupId) {
        String countSql = "select count(0) count from t_customer_group_list_" + groupId;
        List<Map<String, Object>> total = this.sqlQuery(countSql);
        if (total != null && total.size() > 0) {
            return NumberConvertUtil.parseLong(total.get(0).get("count"));
        }
        return 0;
    }

    /**
     * 客户群数据平移
     * 更改客户群所属客户
     *
     * @param
     * @return int
     * @author chengning@salescomm.net
     * @date 2019/4/1 17:22
     */
    public int customerGroupTransferByCustId(int groupId, String custId) {
        String sql = " UPDATE t_order SET cust_id = ? WHERE order_id = (SELECT order_id FROM customer_group WHERE id = ?);";
        int oStatus = this.executeUpdateSQL(sql, custId, groupId);
        sql = "UPDATE customer_group SET cust_id = ? WHERE id = ?";
        int cStatus = this.executeUpdateSQL(sql, custId, groupId);
        LOG.info("更改订单返回状态:" + oStatus + ",客群返回状态:" + cStatus);
        if (cStatus == 1 && oStatus == 1) {
            return 1;
        }
        return 0;
    }

    /**
     * 客户群检索
     *
     * @param param
     * @return
     */
    public List<CustomGroupDTO> listCustomGroup(CustomGroupDO param) {
        StringBuilder hql = new StringBuilder();
        hql.append(" FROM CustomGroupDO m WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (param.getMarketProjectId() != null) {
            hql.append(" AND m.marketProjectId = ? ");
            params.add(param.getMarketProjectId());
        }
        if (StringUtil.isNotEmpty(param.getCustId())) {
            hql.append(" AND m.custId = ? ");
            params.add(param.getCustId());
        }
        List<CustomGroupDTO> result = new ArrayList<>();
        List<CustomGroupDO> list = this.find(hql.toString(), params);
        for (CustomGroupDO c : list) {
            result.add(new CustomGroupDTO(c));
        }
        return result;
    }

    /**
     * 创建客群明细数据表
     *
     * @param customGroupId
     * @return
     */
    public int createCgDataTable(int customGroupId) {
        StringBuffer sb = new StringBuffer();
        // 创建客群明细表
        sb.append(" create table IF NOT EXISTS ").append(ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX);
        sb.append(customGroupId);
        sb.append(" like t_customer_group_list");
        int status = this.executeUpdateSQL(sb.toString());
        return status;
    }

    /**
     * 查询客群明细数据表客户数量
     *
     * @param customGroupId
     * @return
     */
    public long countCgData(int customGroupId) {
        StringBuffer sb = new StringBuffer();
        // 创建客群明细表
        sb.append(" SELECT COUNT(0) count FROM ").append(ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX).append(customGroupId);
        List<Map<String, Object>> list = null;
        try {
            list = this.sqlQuery(sb.toString());
        } catch (Exception e) {
            logger.error("查询客群[" + customGroupId + "]明细数据表客户数量失败", e);
            return 0L;
        }
        if (list != null && list.size() > 0) {
            return NumberConvertUtil.parseLong(list.get(0).get("count"));
        }
        return 0L;
    }
}
