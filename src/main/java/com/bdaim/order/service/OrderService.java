package com.bdaim.order.service;


import com.bdaim.auth.LoginUser;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.util.*;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.service.UserGroupService;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.dto.CustomerGrpOrdParam;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.marketproject.dao.MarketProjectDao;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.order.dao.OrderDao;
import com.bdaim.order.dto.MarketRsOrderQueryParam;
import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 */
@Service("orderService")
public class OrderService {
    private static Logger log = LoggerFactory.getLogger(OrderService.class);
    @Resource
    OrderDao orderDao;
    @Resource
    CustomerDao customerDao;
    @Resource
    CustomGroupService customGroupService;
    @Resource
    MarketTaskDao marketTaskDao;
    @Resource
    UserGroupService userGroupService;
    @Resource
    MarketProjectDao marketProjectDao;
    @Resource
    CustomerUserDao customerUserDao;
    @Resource
    CustomGroupDao customGroupDao;


    public List<Map<String, Object>> listMarketRsOrders(String customerId, MarketRsOrderQueryParam param)
            throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT t2.order_id as orderId,"
                + "  FORMAT(IFNULL (t2.amount,0)/1000,2) as amount, t2.order_type as orderType,"
                + "  t1.NAME as resourceName, IFNULL (t2.quantity,0) as quantity,"
                + "  t2.create_time as createTime, t2.order_state as status FROM"
                + "  t_resource_order t1 LEFT JOIN t_order t2"
                + "    ON t1.order_id = t2.order_id where 1=1 and t2.order_type=2 ");
        sql.append(" and t2.cust_id='").append(customerId).append("'");
        if (StringUtil.isNotEmpty(param.getOrderId())) {
            sql.append(" and t2.order_id = '").append(StringEscapeUtils.escapeSql(param.getOrderId())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getResourceName())) {
            sql.append(" and t2.product_name = '").append(StringEscapeUtils.escapeSql(param.getResourceName()))
                    .append("'");
        }
        if (StringUtil.isNotEmpty(param.getOrderType())) {
            sql.append(" and t1.res_type = '").append(StringEscapeUtils.escapeSql(param.getOrderType())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getCreateTime()) && StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t2.create_time between '").append(StringEscapeUtils.escapeSql(param.getCreateTime()))
                    .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }
        map.put("total", orderDao.getSQLQuery(sql.toString()).list().size());
        map.put("marketResOrders",
                orderDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                        .setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list());
        list.add(map);
        return list;
    }


    public Map<String, Object> queryMarketRsOrdDetail(String customerId, String orderId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        StringBuilder sql = new StringBuilder("SELECT" + "  t2.order_id AS orderId,"
                + "  FORMAT(t2.amount/1000,2) AS amount," + "  t2.create_time AS createDate,"
                + "  t2.order_state AS STATUS," + "  t2.product_name AS prodName," + "  t2.quantity AS quantity,"
                + "  t1.res_type AS prodType," + "  t1.sale_price/100 AS unitPrice,"
                + "  IFNULL (t2.pay_type,0) AS payType," + "  t2.pay_time AS payTime" + "FROM"
                + "  t_resource_order t1" + "  LEFT JOIN t_order t2"
                + "    ON t1.order_id = t2.order_id where 1=1 and t2.order_type=2 ");
        if (StringUtil.isEmpty(orderId)) {
            throw new TouchException("300", "订单号不能为空");
        }
        sql.append(" and t2.cust_id='").append(StringEscapeUtils.escapeSql(customerId)).append("'");
        sql.append(" and t2.order_id='").append(StringEscapeUtils.escapeSql(orderId)).append("'");
        map.put("customerGroupOrdDetail", "");
        map.put("resourceOrdDetail", orderDao.getSQLQuery(sql.toString())
                .setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list().get(0));
        return map;
    }


    public List<Map<String, Object>> listCustGroupOrders0(LoginUser loginUser, String customerId, CustomerGrpOrdParam param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT t1.order_id as orderId , IFNULL (t2.NAME,'') AS groupName, t2.id AS groupId, ")
                .append(" t2.user_count AS quantity,")
                .append(" t2.industry_pool_name AS industryPoolName ,")
                .append(" t1.create_time,  FORMAT(IFNULL (t1.amount,0)/1000,2) AS amount ,  t1.order_type,")
                .append(" IFNULL (t2.remark,'') AS remark , IFNULL (t2.group_source,'') AS source ,")
                .append(" t2.industry_pool_id as industryPoolId ,")
                .append(" t1.order_state AS status ,t2.status as groupStatus, IFNULL (t2.market_project_id,'') marketProjectId, ")
                .append(" t2.task_create_time taskCreateTime, t2.task_end_time taskEndTime, t2.data_source*1 data_source, t1.pay_time ")
                .append("  FROM  t_order t1")
                .append(" RIGHT JOIN customer_group t2")
                .append("   ON t1.order_id = t2.order_id where 1=1 and t1.order_type=1")
                .append(" and t1.cust_id ='").append(StringEscapeUtils.escapeSql(customerId)).append("'");
        if (StringUtil.isNotEmpty(param.getGroupName())) {
            sql.append(" and t2.name like '%").append(StringEscapeUtils.escapeSql(param.getGroupName())).append("%'");
        }
        if (param.getOrderType() > 0) {
            sql.append(" and t1.order_state=").append(param.getOrderType());
        }
        if (StringUtil.isNotEmpty(param.getStatus())) {
            sql.append(" and t2.status=").append(param.getStatus());
        }
        if (StringUtil.isNotEmpty(param.getGroupId()) && Integer.valueOf(param.getGroupId()) > 0) {
            sql.append(" and t2.id=").append(param.getGroupId());
        }
        if ("3".equals(loginUser.getUserType())) {
            List<String> projectIds = customerUserDao.listProjectByUserId(loginUser.getId());
            if (projectIds == null || projectIds.size() == 0) {
                list.add(map);
                return list;
            }
            sql.append(" and t2.market_project_id in(" + SqlAppendUtil.sqlAppendWhereIn(projectIds) + ")");
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) || StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime()))
                    .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }
        // 客群数据来源 0:购买,1:导入
        if (StringUtil.isNotEmpty(param.getDataSource())) {
            if (param.getDataSource().equals("1")) {
                // 导入客户群不查询逻辑删除的客户群
                sql.append(" and t2.data_source = ").append(StringEscapeUtils.escapeSql(param.getDataSource()));
                sql.append(" and t2.status <> 4 ");
            } else if (param.getDataSource().equals("0")) {
                sql.append(" and (t2.data_source is null OR t2.data_source = ").append(StringEscapeUtils.escapeSql(param.getDataSource())).append(" ) ");
            }
        }
        // 项目检索
        if (StringUtil.isNotEmpty(param.getMarketProjectId())) {
            sql.append(" and t2.market_project_id = " + param.getMarketProjectId());
        }
        sql.append("  ORDER BY t2.create_time desc ");
        map.put("total", orderDao.getSQLQuery(sql.toString()).list().size());
        List<Map<String, Object>> custGroupOrders = orderDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list();
        if (custGroupOrders != null && custGroupOrders.size() > 0) {
            LocalDateTime expirationTime = null, nowTime = LocalDateTime.now();
            MarketProject marketProject;
            for (Map<String, Object> model : custGroupOrders) {
                if (model.get("taskCreateTime") == null) {
                    model.put("taskCreateTime", "");
                }
                if (model.get("taskEndTime") == null) {
                    model.put("taskEndTime", "");
                }
                if (model.get("industryPoolName") == null) {
                    model.put("industryPoolName", "");
                }
                // 购买的客户群失效日期为支付日期往后延期6个月
                if (model.get("data_source") != null
                        && NumberConvertUtil.parseInt(model.get("data_source")) == 0) {
                    if (model.get("pay_time") == null) {
                        model.put("expiryTime", "");
                        // 有效
                        model.put("expiryStatus", 1);
                    } else {
                        expirationTime = LocalDateTime.parse(String.valueOf(model.get("pay_time")),
                                DatetimeUtils.DATE_TIME_FORMATTER_SSS).plusMonths(ConstantsUtil.CUSTOMER_GROUP_EXPIRY_MONTH);
                        model.put("expiryTime", expirationTime.format(DatetimeUtils.DATE_TIME_FORMATTER));
                        // 客户群已经失效
                        if (nowTime.isAfter(expirationTime)) {
                            model.put("expiryStatus", 2);
                        } else {
                            // 有效
                            model.put("expiryStatus", 1);
                        }
                    }
                }
                if (model.get("expiryTime") == null) {
                    model.put("expiryTime", "");
                }
                model.put("marketTaskNum", marketTaskDao.countMarketTaskByCGroup(NumberConvertUtil.parseInt(model.get("groupId"))));
                // 查询项目名称
                model.put("marketProjectName", "");
                if (model.get("marketProjectId") != null && StringUtil.isNotEmpty(String.valueOf(model.get("marketProjectId")))) {
                    marketProject = marketProjectDao.selectMarketProject(NumberConvertUtil.parseInt(model.get("marketProjectId")));
                    if (marketProject != null) {
                        model.put("marketProjectName", marketProject.getName());
                    }
                }
                // 客户数量
                if (model.get("quantity") == null) {
                    model.put("quantity", customGroupDao.countCgData(NumberConvertUtil.parseInt(model.get("groupId"))));
                }
            }
        }
        map.put("custGroupOrders", custGroupOrders);
        list.add(map);
        return list;
    }

    public List<Map<String, Object>> listCustGroupOrders(Long user_id, String customerId, String user_type, CustomerGrpOrdParam param) {
        // 判断当前用户的user_type
        Map<String, Object> map = new HashMap<>();
        List<Map<String, Object>> list = new ArrayList<>();
        StringBuilder sql = new StringBuilder("SELECT t1.order_id as orderId , IFNULL (t2.NAME,'') AS groupName, t2.id AS groupId,");
        if (!user_type.equals("1")) {
            sql.append("  ( ");
            sql.append("  SELECT ");
            sql.append("  	COUNT(custG.id)");
            sql.append("  FROM");
            sql.append("  	t_customer_group_list_" + customerId + " custG");
            sql.append("  LEFT JOIN t_user t ON custG.user_id = t.id");
            sql.append("  WHERE");
            sql.append("  	custG.user_id = '" + user_id + "'");
            sql.append("  AND t2.id= custG.customer_group_id");
            sql.append("  )AS quantity,");
        } else {
            sql.append(" IFNULL(t1.quantity,0) AS quantity ,");
        }
        sql.append(" t2.industry_pool_name AS industryPoolName ,");
        sql.append(" t1.create_time,  FORMAT(IFNULL (t1.amount,0)/1000,2) AS amount ,  t1.order_type,");
        sql.append(" IFNULL (t2.remark,'') AS remark ,  IFNULL (t2.group_source,'') AS source ,");
        sql.append(" t2.industry_pool_id as industryPoolId ,");
        sql.append(" t1.order_state AS status , " + "  t2.status as groupStatus  FROM  t_order t1");
        sql.append(" RIGHT JOIN customer_group t2");
        sql.append("   ON t1.order_id = t2.order_id where 1=1 and t1.order_type=1");
        sql.append(" and t1.cust_id ='").append(StringEscapeUtils.escapeSql(customerId)).append("'");
        if (StringUtil.isNotEmpty(param.getGroupName())) {
            sql.append(" and t2.name like '%").append(StringEscapeUtils.escapeSql(param.getGroupName())).append("%'");
        }
        if (param.getOrderType() > 0) {
            sql.append(" and t1.order_state=").append(param.getOrderType());
        }
        //添加userid
        if (!user_type.equals("1")) {
            sql.append(" AND t2.id in (SELECT DISTINCT");
            sql.append(" 	custG.customer_group_id");
            sql.append(" FROM");
            sql.append(" 	t_customer_group_list_" + customerId + " custG");
            sql.append(" LEFT JOIN t_user t ON custG.user_id = t.id");
            sql.append(" WHERE");
            sql.append(" custG.user_id='" + user_id + "')");
        }
        if (param.getOrderType() > 0) {
            sql.append(" and t1.order_state=").append(param.getOrderType());
        }
        if (StringUtil.isNotEmpty(param.getGroupId()) && Integer.valueOf(param.getGroupId()) > 0) {
            sql.append(" and t2.id=").append(param.getGroupId());
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) || StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime()))
                    .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }
        sql.append("  ORDER BY t1.create_time desc ");
        map.put("total", orderDao.getSQLQuery(sql.toString()).list().size());
        map.put("custGroupOrders",
                orderDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                        .setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list());
        list.add(map);
        return list;
    }


    public Map<String, Object> queryCustomerOrdDetail(String customerId, String orderId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        StringBuilder sql = new StringBuilder("SELECT IFNULL(t1.NAME,'') AS groupName,"
                + "  IFNULL(t1.group_source,'') AS source , IFNULL(t1.description,'') AS description ,"
                + "  IFNULL(t1.industry_pool_name,'') AS industryPoolName, IFNULL(t1.purpose,'') as purpose,"
                + "  t1.id, IFNULL(t1.group_condition,'') AS groupCondition,"
                + "  IFNULL(t2.quantity,0) AS quantity, t1.create_time AS createTime,"
                + "  FORMAT(IFNULL(t2.amount,0)/1000,2) AS amount, IFNULL(t2.pay_type,0) AS payType,"
                + "  t2.pay_time AS payTime, FORMAT(IFNULL(t2.pay_amount,0)/1000,2) AS payAmount,"
                + "  IFNULL(t1.STATUS,0) AS status  FROM customer_group t1 LEFT JOIN t_order t2"
                + "    ON t1.order_id = t2.order_id  where 1=1 and t2.order_type=1 ");
        if (StringUtil.isEmpty(orderId)) {
            throw new TouchException("300", "订单号不能为空");
        }
        if (StringUtil.isNotEmpty(customerId) && !"null".equals(customerId)) {
            sql.append(" and t2.cust_id='").append(StringEscapeUtils.escapeSql(customerId)).append("'");
        }

        sql.append(" and t2.order_id ='").append(StringEscapeUtils.escapeSql(orderId)).append("'");

        List list = orderDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
        map.put("customerGroupOrdDetail", list.size() > 0 ? list.get(0) : new HashMap());
        map.put("resourceOrdDetail", "");
        return map;
    }

}
