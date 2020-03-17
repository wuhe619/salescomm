package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmBiDao extends SimpleHibernateDao<LkCrmBiDao, Integer> {
    public List<Map<String, Object>> queryLogByUser(Object sqlDateFormat, Object beginTime, Object finalTime, String uid) {
        String sql = "    SELECT b.realname,b.username,b.img,b.user_id,(SELECT COUNT(1) FROM lkcrm_task_comment" +
                " WHERE type='1' and type_id=a.log_id) as sum,a.send_user_ids,a.read_user_ids " +
                "    FROM lkcrm_oa_log as a LEFT JOIN lkcrm_admin_user as b on a.create_user_id=b.user_id " +
                "    WHERE DATE_FORMAT(a.create_time,?) between ? and" +
                " ? and a.create_user_id =? and a.cust_id=? and b.cust_id=?";
        return super.queryListBySql(sql, sqlDateFormat, beginTime, finalTime, uid,
                BaseUtil.getCustId(), BaseUtil.getCustId());
    }

    public List<Map<String, Object>> examineStatistics(List<Record> categoryList, List<String> users,
                                                       Object sqlDateFormat, Object beginTime, Object finalTime) {
        String sql = "SELECT " +
                " a.user_id,a.username,a.realname,a.img ";
        for (Record record : categoryList) {
            sql += ",COUNT(case when b.category_id=#para(x.category_id) then 1 end) as count_" + record.get("category_id");
        }
        sql += " FROM " +
                " lkcrm_admin_user AS a " +
                " LEFT JOIN lkcrm_oa_examine AS b ON a.user_id = b.create_user_id  " +
                " AND ( DATE_FORMAT( b.create_time,? ) BETWEEN ? AND ? )  " +
                "WHERE " +
                " a.user_id IN (";
        sql += SqlAppendUtil.sqlAppendWhereIn(users);
        sql += " ) AND a.cust_id=? AND b.cust_id=? GROUP BY a.user_id";
        return super.queryListBySql(sql, sqlDateFormat, beginTime, finalTime, BaseUtil.getCustId(), BaseUtil.getCustId());
    }

    public Map<String, Object> queryExamineCount(Object categoryId, Object beginDate, Object endDate, Object userId) {
        String sql = "SELECT " +
                " IFNULL( SUM( money ), 0 ) AS money, " +
                " IFNULL( SUM( duration ), 0 ) AS duration  " +
                "FROM " +
                " lkcrm_oa_examine AS a " +
                " LEFT JOIN lkcrm_oa_examine_record AS b ON a.examine_id = b.examine_id  " +
                " AND b.examine_status = '1'  " +
                "WHERE " +
                " a.category_id = ?  " +
                " AND ( a.create_time BETWEEN ? AND ? )  " +
                " AND a.create_user_id = ? AND a.cust_id=? AND b.cust_id=?";
        return super.queryUniqueSql(sql, categoryId, beginDate, endDate, userId, BaseUtil.getCustId(), BaseUtil.getCustId());
    }

    public List<Map<String, Object>> queryCrmBusinessStatistics(Long userId, Integer productId,
                                                                Date startTime, Date endTime, Integer deptId) {
        String sql = "    SELECT DISTINCT (SELECT COUNT(*) from lkcrm_crm_business WHERE scb.status_id = status_id) as  businessNum, " +
                "    scb.status_id,IFNULL((SELECT sum(money)  from lkcrm_crm_business WHERE scb.status_id = status_id),0)as  total_price, " +
                "    scbs.`name` " +
                "    from lkcrm_crm_business as scb " +
                "    LEFT JOIN lkcrm_crm_business_product as scbp on scbp.business_id = scb.business_id " +
                "    LEFT JOIN lkcrm_crm_business_status as scbs on scbs.status_id = scb.status_id " +
                "    LEFT JOIN lkcrm_admin_user as sau on sau.user_id = scb.owner_user_id " +
                "    where  1 = 1 and scb.cust_id=? ";
        List<Object> params = new ArrayList<>();
        params.add(BaseUtil.getCustId());
        if (userId != null) {
            sql += " and scb.owner_user_id = ? ";
            params.add(userId);
        }
        if (productId != null) {
            sql += " and scbp.product_id = ? ";
            params.add(productId);
        }
        if (deptId != null) {
            sql += " and sau.dept_id = ? ";
            params.add(deptId);
        }
        if (startTime != null) {
            sql += " and unix_timestamp(?) - unix_timestamp(scb.create_time) < 0 ";
            params.add(startTime);
        }
        if (endTime != null) {
            sql += " and unix_timestamp(?) - unix_timestamp(scb.create_time) > 0 ";
            params.add(endTime);
        }
        sql += "      group by status_id";
        return super.queryListBySql(sql, params.toArray());
    }

    public List<Map<String, Object>> queryProductSell(Date startTime, Date endTime, Integer userId, Integer deptId) {
        String sql = "      SELECT scp.product_id,scpc.name as categoryName , scpc.category_id, " +
                "      scp.name as productName, scc.num as contracNum , " +
                "      sau.realname as ownerUserName,sccu.customer_name as customerName , " +
                "      sccp.sales_price as productPrice ,sccp.num as productNum, " +
                "      sccp.subtotal as productSubtotal, " +
                "      scc.contract_id, " +
                "      sccu.customer_id " +
                "      FROM lkcrm_crm_product as scp " +
                "      LEFT JOIN lkcrm_crm_contract_product as sccp on sccp.product_id = scp.product_id " +
                "      LEFT JOIN lkcrm_crm_contract as scc on scc.contract_id = sccp.contract_id " +
                "      LEFT JOIN lkcrm_admin_user as sau on sau.user_id = scc.owner_user_id " +
                "      LEFT JOIN lkcrm_crm_customer as sccu on sccu.customer_id = scc.customer_id " +
                "      LEFT JOIN lkcrm_crm_product_category as scpc on scpc.category_id = scp.category_id " +
                "      where 1 = 1 and scp.cust_id=? ";
        List<Object> params = new ArrayList<>();
        params.add(BaseUtil.getCustId());
        if (startTime != null) {
            sql += " and unix_timestamp(?) - unix_timestamp(scc.order_date) < 0 ";
            params.add(startTime);
        }
        if (endTime != null) {
            sql += " and unix_timestamp(?) - unix_timestamp(scc.order_date) > 0 ";
            params.add(endTime);
        }
        if (userId != null) {
            sql += "  and sau.user_id = ? ";
            params.add(userId);
        }
        if (deptId != null) {
            sql += "  and sau.dept_id = ? ";
            params.add(deptId);
        }
        return super.queryListBySql(sql, params.toArray());
    }

    public List<Map<String, Object>> queryByUserIdOrYear(String year, String month, Integer userId, Integer deptId) {
        String contractview = BaseUtil.getViewSqlNotASName("contractview");
        String sql = "      select co.* from " + contractview + " as co LEFT JOIN lkcrm_admin_user as sau on co.owner_user_id = sau.user_id " +
                "      where 1 = 1 and sau.cust_id=? ";
        List<Object> params = new ArrayList<>();
        params.add(BaseUtil.getCustId());
        if (StringUtil.isNotEmpty(year)) {
            sql += " and YEAR(co.create_time) = ? ";
            params.add(year);
        }
        if (StringUtil.isNotEmpty(month)) {
            sql += " and month(co.create_time) = ? ";
            params.add(month);
        }
        if (userId != null) {
            sql += " and sau.owner_user_id = ? ";
            params.add(userId);
        }
        if (deptId != null) {
            sql += " and sau.dept_id = ?  ";
            params.add(deptId);
        }

        return super.queryListBySql(sql, params.toArray());
    }

    public List<Map<String, Object>> queryContractByDeptId(String year, Integer deptId) {
        String sql = "select '一月' as month,IFNULL(january,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','01') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','01') and check_status = 2)/IFNULL(january,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' " +
                "    union all " +
                "    select '二月' as month,IFNULL(february,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','02') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','02') and check_status = 2)/IFNULL(february,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' " +
                "    union all " +
                "    select '三月' as month,IFNULL(march,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','03') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','03') and check_status = 2)/IFNULL(march,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' " +
                "    union all " +
                "    select '四月' as month,IFNULL(april,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','04') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','04') and check_status = 2)/IFNULL(april,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' " +
                "    union all " +
                "    select '五月' as month,IFNULL(may,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','05') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','05') and check_status = 2)/IFNULL(may,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' " +
                "    union all " +
                "    select '六月' as month,IFNULL(june,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','06') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','06') and check_status = 2)/IFNULL(june,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "'  " +
                "    union all " +
                "    select '七月' as month,IFNULL(july,0) as achievement,(select IFNULL(SUM(a.money),0) from " +
                "    lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','07') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','07') and check_status = 2)/IFNULL(july,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "'  " +
                "    union all " +
                "    select '八月' as month,IFNULL(august,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','08') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','08') and check_status = 2)/IFNULL(august,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "'  " +
                "    union all " +
                "    select '九月' as month,IFNULL(september,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','09') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','09') and check_status = 2)/IFNULL(september,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "'  " +
                "    union all " +
                "    select '十月' as month,IFNULL(october,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','10') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','10') and check_status = 2)/IFNULL(october,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "'  " +
                "    union all " +
                "    select '十一月' as month,IFNULL(november,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','11') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','11') and check_status = 2)/IFNULL(november,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "'  " +
                "    union all " +
                "    select '十二月' as month,IFNULL(december,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id = '" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','12') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_contract as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and b.dept_id = '" + deptId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','12') and check_status = 2)/IFNULL(december,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "'  ";
        logger.info("statistics SQL is {}", sql);
        return super.queryListBySql(sql);
    }

    public List<Map<String, Object>> queryContractByUserId(String year, Long userId) {
        String sql = "select '一月' as month,IFNULL(january,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where  cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','01') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','01') and check_status = 2)/IFNULL(january,0)*100,2),0) as rate from lkcrm_crm_achievement where  obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '二月' as month,IFNULL(february,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','02') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','02') and check_status = 2)/IFNULL(february,0)*100,2),0) as rate from lkcrm_crm_achievement where   obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '三月' as month,IFNULL(march,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','03') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','03') and check_status = 2)/IFNULL(march,0)*100,2),0) as rate from lkcrm_crm_achievement where   obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '四月' as month,IFNULL(april,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','04') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','04') and check_status = 2)/IFNULL(april,0)*100,2),0) as rate from lkcrm_crm_achievement where   obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '五月' as month,IFNULL(may,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','05') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','05') and check_status = 2)/IFNULL(may,0)*100,2),0) as rate from lkcrm_crm_achievement where   obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '六月' as month,IFNULL(june,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','06') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','06') and check_status = 2)/IFNULL(june,0)*100,2),0) as rate from lkcrm_crm_achievement where   obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '七月' as month,IFNULL(july,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','07') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','07') and check_status = 2)/IFNULL(july,0)*100,2),0) as rate from lkcrm_crm_achievement where   obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '八月' as month,IFNULL(august,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','08') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','08') and check_status = 2)/IFNULL(august,0)*100,2),0) as rate from lkcrm_crm_achievement where   obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '九月' as month,IFNULL(september,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','09') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','09') and check_status = 2)/IFNULL(september,0)*100,2),0) as rate from lkcrm_crm_achievement where   obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '十月' as month,IFNULL(october,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','10') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','10') and check_status = 2)/IFNULL(october,0)*100,2),0) as rate from lkcrm_crm_achievement where   obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '十一月' as month,IFNULL(november,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','11') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','11') and check_status = 2)/IFNULL(november,0)*100,2),0) as rate from lkcrm_crm_achievement where  obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '十二月' as month,IFNULL(december,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id = '" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','12') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_contract where cust_id='" + BaseUtil.getCustId() + "' and owner_user_id = '" + userId + "' and DATE_FORMAT(order_date,'%Y%m') = CONCAT('" + year + "','12') and check_status = 2)/IFNULL(december,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                " ";
        return super.queryListBySql(sql);
    }

    public List<Map<String, Object>> queryReceivablesByDeptId(String year, Integer deptId) {
        String sql = "    select '一月' as month,IFNULL(january,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','01') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','01') and check_status = 2)/IFNULL(january,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '二月' as month,IFNULL(february,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','02') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','02') and check_status = 2)/IFNULL(february,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '三月' as month,IFNULL(march,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','03') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','03') and check_status = 2)/IFNULL(march,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '四月' as month,IFNULL(april,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','04') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','04') and check_status = 2)/IFNULL(april,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '五月' as month,IFNULL(may,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','05') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','05') and check_status = 2)/IFNULL(may,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '六月' as month,IFNULL(june,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','06') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','06') and check_status = 2)/IFNULL(june,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '七月' as month,IFNULL(july,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','07') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','07') and check_status = 2)/IFNULL(july,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '八月' as month,IFNULL(august,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','08') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','08') and check_status = 2)/IFNULL(august,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '九月' as month,IFNULL(september,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','09') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','09') and check_status = 2)/IFNULL(september,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '十月' as month,IFNULL(october,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','10') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','10') and check_status = 2)/IFNULL(october,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '十一月' as month,IFNULL(november,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','11') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','11') and check_status = 2)/IFNULL(november,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "    union all " +
                "    select '十二月' as month,IFNULL(december,0) as achievement,(select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' and b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','12') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(a.money),0) from lkcrm_crm_receivables as a inner join lkcrm_admin_user as b where a.owner_user_id = b.user_id and a.cust_id='" + BaseUtil.getCustId() + "' b.dept_id = '" + deptId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','12') and check_status = 2)/IFNULL(december,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + deptId + "' and type = 2 and year = '" + year + "' and cust_id='" + BaseUtil.getCustId() + "' " +
                "  ";
        return super.queryListBySql(sql);

    }

    public List<Map<String, Object>> queryReceivablesByUserId(String year, Long userId) {
        String sql = "    select '一月' as month,IFNULL(january,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','01') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','01') and check_status = 2)/IFNULL(january,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '二月' as month,IFNULL(february,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','02') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','02') and check_status = 2)/IFNULL(february,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '三月' as month,IFNULL(march,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','03') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','03') and check_status = 2)/IFNULL(march,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '四月' as month,IFNULL(april,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','04') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','04') and check_status = 2)/IFNULL(april,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '五月' as month,IFNULL(may,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','05') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','05') and check_status = 2)/IFNULL(may,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '六月' as month,IFNULL(june,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','06') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','06') and check_status = 2)/IFNULL(june,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '七月' as month,IFNULL(july,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','07') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','07') and check_status = 2)/IFNULL(july,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '八月' as month,IFNULL(august,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','08') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','08') and check_status = 2)/IFNULL(august,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '九月' as month,IFNULL(september,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','09') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','09') and check_status = 2)/IFNULL(september,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '十月' as month,IFNULL(october,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','10') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','10') and check_status = 2)/IFNULL(october,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '十一月' as month,IFNULL(november,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','11') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','11') and check_status = 2)/IFNULL(november,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "    union all " +
                "    select '十二月' as month,IFNULL(december,0) as achievement,(select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','12') and check_status = 2) as receivables,IFNULL(ROUND((select IFNULL(SUM(money),0) from lkcrm_crm_receivables where owner_user_id = '" + userId + "' and DATE_FORMAT(return_time,'%Y%m') = CONCAT('" + year + "','12') and check_status = 2)/IFNULL(december,0)*100,2),0) as rate from lkcrm_crm_achievement where obj_id = '" + userId + "' and type = 3 and year = '" + year + "' " +
                "  ";
        return super.queryListBySql(sql);
    }

    public List<Record> contractRanKing(String[] userIds, Integer type, String startTime, String endTime) {
        String sql = "SELECT " +
                " IFNULL( SUM( cct.money ), 0 ) AS money, " +
                " cau.realname, " +
                " cct.owner_user_id, " +
                " cad.NAME AS structureName  " +
                "FROM " +
                " lkcrm_crm_contract AS cct " +
                " LEFT JOIN lkcrm_admin_user AS cau ON cau.user_id = cct.owner_user_id " +
                " LEFT JOIN lkcrm_admin_dept AS cad ON cad.dept_id = cau.dept_id  " +
                "WHERE " +
                " check_status = 2  " +
                " AND cct.owner_user_id IN (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIds);
        sql += ") and cct.cust_id='" + BaseUtil.getCustId() + "' ";
        if (type == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(order_date) ";

        }
        if (type == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(order_date) = 1 ";
        }
        if (type == 3) {
            sql += " and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (type == 4) {
            sql += " and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (type == 5) {
            sql += "  and date_format(order_date,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (type == 6) {
            sql += " and date_format(order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (type == 7) {
            sql += " and QUARTER(order_date)=QUARTER(now()) AND YEAR(order_date)=YEAR(NOW()) ";
        }
        if (type == 8) {
            sql += " and QUARTER(order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) ";
        }
        if (type == 9) {
            sql += " and YEAR(order_date)=YEAR(NOW()) ";
        }
        if (type == 10) {
            sql += " and YEAR(order_date)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (type == 11) {
            sql += " and  TO_DAYS(order_date) >= TO_DAYS(?) ";
            sql += " and  TO_DAYS(order_date) <= TO_DAYS(?) ";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "        GROUP BY cct.owner_user_id " +
                "        ORDER BY IFNULL(SUM(cct.money), 0) DESC";

        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public List<Record> receivablesRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "      SELECT IFNULL(SUM(cct.money), 0) as money,cau.realname,cad.name as structureName " +
                "      FROM " +
                "        lkcrm_crm_receivables as cct " +
                "      LEFT JOIN lkcrm_admin_user as cau on cau.user_id = cct.owner_user_id " +
                "      left join lkcrm_admin_dept as cad on cad.dept_id = cau.dept_id " +
                "      WHERE " +
                "        check_status = 2 " +
                "        and  cct.owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and cct.cust_id='" + BaseUtil.getCustId() + "' ";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(return_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(return_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(return_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(return_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(return_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(return_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(return_time)=QUARTER(now()) AND YEAR(return_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(return_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(return_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(return_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(return_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(return_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(return_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "        GROUP BY cct.owner_user_id " +
                "        ORDER BY IFNULL(SUM(cct.money), 0) DESC";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params));

    }

    public List<Record> contractCountRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "      SELECT count(1) as `count`,cau.realname,cct.owner_user_id,cad.name as structureName " +
                "      FROM " +
                "        lkcrm_crm_contract as cct " +
                "      LEFT JOIN lkcrm_admin_user as cau on cau.user_id = cct.owner_user_id " +
                "       left join lkcrm_admin_dept as cad on cad.dept_id = cau.dept_id " +
                "      WHERE " +
                "        check_status = 2 " +
                "        and  cct.owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and cct.cust_id='" + BaseUtil.getCustId() + "' ";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(order_date) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(order_date) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(order_date,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(order_date)=QUARTER(now()) AND YEAR(order_date)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(order_date)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(order_date)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(order_date) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(order_date) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "        GROUP BY cct.owner_user_id " +
                "        ORDER BY count(1) DESC";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params));
    }

    public List<Record> productCountRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "     SELECT count(1) as `count`,cau.realname,cct.owner_user_id,cad.name as structureName " +
                "      FROM " +
                "        lkcrm_crm_contract_product as ccp " +
                "      LEFT JOIN lkcrm_crm_contract as cct on ccp.contract_id = cct.contract_id " +
                "      LEFT JOIN lkcrm_admin_user as cau on cau.user_id = cct.owner_user_id " +
                "       left join lkcrm_admin_dept as cad on cad.dept_id = cau.dept_id " +
                "      WHERE " +
                "        check_status = 2 " +
                "        and  cct.owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and cct.cust_id='" + BaseUtil.getCustId() + "' ";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(order_date) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(order_date) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(order_date,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(order_date)=QUARTER(now()) AND YEAR(order_date)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(order_date)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(order_date)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(order_date) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(order_date) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "        GROUP BY cct.owner_user_id " +
                "        ORDER BY count(1) DESC";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public List<Record> customerCountRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "      SELECT count(1) as `count`,cau.realname,cct.create_user_id,cad.name as structureName " +
                "      FROM " +
                "        lkcrm_crm_customer as cct " +
                "      LEFT JOIN lkcrm_admin_user as cau on cau.user_id = cct.create_user_id " +
                "       left join lkcrm_admin_dept as cad on cad.dept_id = cau.dept_id " +
                "      WHERE " +
                "         cct.create_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and cct.cust_id='" + BaseUtil.getCustId() + "' ";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(cct.create_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(cct.create_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(cct.create_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(cct.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(cct.create_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(cct.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(cct.create_time)=QUARTER(now()) AND YEAR(cct.create_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(cct.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(cct.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(cct.create_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(cct.create_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(cct.create_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(cct.create_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "       GROUP BY cct.create_user_id " +
                "        ORDER BY count(1) DESC";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));


    }

    public List<Record> portraitSource(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "        SELECT (SELECT COUNT(1) FROM customerview WHERE `客户来源` =ccc.`客户来源`) as allCustomer, " +
                "          (SELECT COUNT(1) FROM customerview where deal_status = '已成交' and `客户来源` =ccc.`客户来源`) as dealCustomer, " +
                "          CASE " +
                "          when  ccc.`客户来源` = '' then  '未知' " +
                "          ELSE ccc.`客户来源` end " +
                "          as source " +
                "           FROM customerview as ccc " +
                "          where   ccc.owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and ccc.cust_id='" + BaseUtil.getCustId() + "'";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(ccc.create_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(ccc.create_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(ccc.create_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(ccc.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(ccc.create_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(ccc.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(ccc.create_time)=QUARTER(now()) AND YEAR(ccc.create_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(ccc.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(ccc.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(ccc.create_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(ccc.create_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(ccc.create_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(ccc.create_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "  GROUP BY ccc.`客户来源`";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));

    }

    public List<Record> portraitLevel(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "         SELECT (SELECT COUNT(1) FROM customerview WHERE `客户级别` =ccc.`客户级别`) as allCustomer, " +
                "          (SELECT COUNT(1) FROM customerview where deal_status = '已成交' and `客户级别` =ccc.`客户级别`) as dealCustomer, " +
                "          CASE " +
                "          when  ccc.`客户级别` = '' then  '未知' " +
                "          ELSE ccc.`客户级别` end " +
                "          as level " +
                "          FROM customerview as ccc " +
                "          where   ccc.owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and ccc.cust_id='" + BaseUtil.getCustId() + "' ";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(ccc.create_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(ccc.create_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(ccc.create_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(ccc.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(ccc.create_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(ccc.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(ccc.create_time)=QUARTER(now()) AND YEAR(ccc.create_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(ccc.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(ccc.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(ccc.create_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(ccc.create_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(ccc.create_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(ccc.create_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += " GROUP BY ccc.`客户级别`";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public List<Record> portrait(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "          SELECT (SELECT COUNT(1) FROM customerview WHERE `客户行业` =ccc.`客户行业`) as allCustomer, " +
                "        (SELECT COUNT(1) FROM customerview where deal_status = '已成交' and `客户行业` =ccc.`客户行业`) as dealCustomer, " +
                "        CASE " +
                "        when  ccc.`客户行业` = '' then  '未知' " +
                "        ELSE ccc.`客户行业` end " +
                "        as industry " +
                "         FROM customerview as ccc " +
                "         where   ccc.owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and ccc.cust_id='" + BaseUtil.getCustId() + "' ";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(ccc.create_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(ccc.create_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(ccc.create_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(ccc.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(ccc.create_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(ccc.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(ccc.create_time)=QUARTER(now()) AND YEAR(ccc.create_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(ccc.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(ccc.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(ccc.create_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(ccc.create_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(ccc.create_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(ccc.create_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += " GROUP BY ccc.`客户行业`";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public Record addressAnalyse(String addRess) {
        String sql = "        SELECT  COUNT(1) as allCustomer, " +
                "        ifnull((SELECT COUNT(1) FROM lkcrm_crm_customer where deal_status = '已成交'" +
                " and left(address,INSTR(address, ',') - 1) = left(ccc.address,INSTR(ccc.address, ',') - 1)) ,0)as dealCustomer, " +
                "        ? as address " +
                "        FROM lkcrm_crm_customer as ccc " +
                "        where  left(ccc.address,INSTR(ccc.address, ',') - 1) like concat('%',?,'%') and ccc.cust_id=?";
        return JavaBeanUtil.mapToRecord(super.queryUniqueSql(sql, addRess, addRess, BaseUtil.getCustId()));
    }

    public List<Record> productSellRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "    SELECT cct.contract_id,ccs.customer_id,ccp.product_id,ccpc.category_id,cct.`name` as contractName, " +
                "      ccpc.`name` as categoryName,ccs.customer_name as customerName,cccp.num as num,cccp.subtotal ,cccp.sales_price as salesPrice, " +
                "      cct.num as contractNum,cct.owner_user_id  ,cau.realname,cccp.unit,cccp.discount,cccp.price " +
                "      FROM lkcrm_crm_contract as cct " +
                "      LEFT JOIN lkcrm_crm_contract_product as cccp ON cccp.contract_id = cct.contract_id " +
                "      LEFT JOIN lkcrm_crm_customer as ccs on ccs.customer_id = cct.customer_id " +
                "      LEFT JOIN lkcrm_crm_product as ccp on ccp.product_id = cccp.product_id " +
                "      LEFT JOIN lkcrm_crm_product_category as ccpc ON ccp.category_id = ccpc.category_id " +
                "      LEFT JOIN lkcrm_admin_user as cau on cau.user_id = cct.owner_user_id " +
                "      where " +
                "          cct.check_status = 2 " +
                "        and  cct.owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and cct.cust_id='" + BaseUtil.getCustId() + "'";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(cct.order_date) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(cct.order_date) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(cct.order_date,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(cct.order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(cct.order_date,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(cct.order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(cct.order_date)=QUARTER(now()) AND YEAR(cct.order_date)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(cct.order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(cct.order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(cct.order_date)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(cct.order_date)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(cct.order_date) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(cct.order_date) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public List<Record> travelCountRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "      SELECT count(1) as `count`,cau.realname,coe.create_user_id,cad.name as structureName " +
                "      FROM " +
                "        lkcrm_oa_examine_travel as coet " +
                "         LEFT JOIN lkcrm_oa_examine as coe on coe.examine_id = coet.examine_id " +
                "      LEFT JOIN lkcrm_admin_user as cau on cau.user_id = coe.create_user_id " +
                "       left join lkcrm_admin_dept as cad on cad.dept_id = cau.dept_id " +
                "      WHERE " +
                "         coe.create_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and coe.cust_id='" + BaseUtil.getCustId() + "'";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(coet.start_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(coet.start_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(coet.start_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(coet.start_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(coet.start_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(coet.start_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(coet.start_time)=QUARTER(now()) AND YEAR(coet.start_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(coet.start_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(coet.start_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(coet.start_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(coet.start_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(coet.start_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(coet.start_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "        GROUP BY coe.create_user_id " +
                "        ORDER BY count(1) DESC";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public List<Record> contractProductRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "     SELECT IFNULL(SUM(cccp.num),0) as num ,ccpc.`name` as categoryName,ccp.`name` as productName,ccpc.category_id , ccp.product_id " +
                "      FROM lkcrm_crm_contract_product  as cccp " +
                "      LEFT JOIN lkcrm_crm_contract as cct on cct.contract_id = cccp.contract_id " +
                "      LEFT JOIN lkcrm_crm_product as ccp on ccp.product_id = cccp.product_id " +
                "      LEFT JOIN lkcrm_crm_product_category as ccpc ON ccpc.category_id = ccp.category_id " +
                "      WHERE " +
                "        check_status = 2 " +
                "        and  cct.owner_user_id in (";

        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and cct.cust_id='" + BaseUtil.getCustId() + "'";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(cct.order_date) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(cct.order_date) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(cct.order_date,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(cct.order_date,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(cct.order_date,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(cct.order_date,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(cct.order_date)=QUARTER(now()) AND YEAR(cct.order_date)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(cct.order_date)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(cct.order_date,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(cct.order_date)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(cct.order_date)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(cct.order_date) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(cct.order_date) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "   GROUP BY cccp.product_id";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public List<Record> recordCountRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "      SELECT count(1) as `count`,cau.realname,ccr.create_user_id,cad.name as structureName " +
                "      FROM " +
                "        lkcrm_admin_record as ccr " +
                "      LEFT JOIN lkcrm_admin_user as cau on cau.user_id = ccr.create_user_id " +
                "       left join lkcrm_admin_dept as cad on cad.dept_id = cau.dept_id " +
                "      WHERE " +
                "         ccr.create_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and ccr.cust_id='" + BaseUtil.getCustId() + "'";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(ccr.create_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(ccr.create_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(ccr.create_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(ccr.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(ccr.create_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(ccr.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(ccr.create_time)=QUARTER(now()) AND YEAR(ccr.create_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(ccr.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(ccr.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(ccr.create_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(ccr.create_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(ccr.create_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(ccr.create_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "        GROUP BY ccr.create_user_id " +
                "        ORDER BY count(1) DESC";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public List<Record> customerGenjinCountRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "      SELECT count(1) as `count`,cau.realname,cct.owner_user_id,cad.name as structureName " +
                "      FROM " +
                "        lkcrm_crm_customer as cct " +
                "      LEFT JOIN lkcrm_admin_user as cau on cau.user_id = cct.owner_user_id " +
                "       left join lkcrm_admin_dept as cad on cad.dept_id = cau.dept_id " +
                "      WHERE " +
                "         cct.owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and cct.cust_id='" + BaseUtil.getCustId() + "'";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(update_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(update_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(update_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(update_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(update_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(update_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(update_time)=QUARTER(now()) AND YEAR(update_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(update_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(update_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(update_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(update_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(update_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(update_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "       GROUP BY cct.owner_user_id " +
                "        ORDER BY count(1) DESC";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public List<Record> contactsCountRanKing(String[] userIdsArr, Integer status, String startTime, String endTime) {
        String sql = "      SELECT count(1) as `count`,cau.realname,cct.create_user_id,cad.name as structureName " +
                "      FROM " +
                "        lkcrm_crm_contacts as cct " +
                "      LEFT JOIN lkcrm_admin_user as cau on cau.user_id = cct.create_user_id " +
                "       left join lkcrm_admin_dept as cad on cad.dept_id = cau.dept_id " +
                "      WHERE " +
                "         cct.create_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdsArr);
        sql += ") and cct.cust_id='" + BaseUtil.getCustId() + "'";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(cct.create_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(cct.create_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(cct.create_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(cct.create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(cct.create_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(cct.create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(cct.create_time)=QUARTER(now()) AND YEAR(cct.create_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(cct.create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(cct.create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(cct.create_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(cct.create_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(cct.create_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(cct.create_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "        GROUP BY cct.create_user_id " +
                "        ORDER BY count(1) DESC";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, params.toArray()));
    }

    public List<Record> sellFunnelList(String[] userIdss, Integer status, String startTime, String endTime) {
        String sql = "        SELECT " +
                "         business_id,business_name,create_time,create_user_id, " +
                "        create_user_name,customer_id,customer_name, " +
                "        deal_date,money,owner_user_id,owner_user_name, " +
                "        status_id,status_name,type_id,type_name " +
                "        FROM businessview " +
                "        where " +
                "        owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdss);
        sql += ") and cust_id='" + BaseUtil.getCustId() + "' ";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(create_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(create_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(create_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        List<Object> params = new ArrayList<>();
        if (status == 11) {
            sql += "            and  TO_DAYS(create_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(create_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        return JavaBeanUtil.mapToRecords(super.queryMapsListBySql(sql, params.toArray()));
    }

    public List<Record> sellFunnel(String[] userIdss, Integer status, String startTime, String endTime, Integer typeId) {
        List<Object> params = new ArrayList<>();
        String sql = "      SELECT COUNT(1) as count, " +
                "      ccbs.`name`, " +
                "      ccbs.order_num,IFNULL(SUM(ccb.money),0) as money, " +
                "      ccb.type_id " +
                "      FROM lkcrm_crm_business as ccb " +
                "      LEFT JOIN lkcrm_crm_business_status as ccbs ON ccbs.status_id = ccb.status_id " +
                "      where " +
                "        ccbs.type_id = ? and ccb.cust_id=? " +
                "        and  ccb.owner_user_id in (";
        params.add(typeId);
        params.add(BaseUtil.getCustId());
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdss);
        sql += ") ";
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(create_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(create_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(create_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        if (status == 11) {
            sql += "            and  TO_DAYS(create_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(create_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += "      GROUP BY ccbs.`name`,ccbs.order_num,ccb.type_id";
        return JavaBeanUtil.mapToRecords(super.queryMapsListBySql(sql, params.toArray()));
    }

    public Map sellFunnelSum(Integer isEnd, String[] userIdss, Integer status, String startTime, String endTime, Integer typeId) {
        List<Object> params = new ArrayList<>();
        String sql = " SELECT IFNULL(SUM(ccb.money),0) as money" +
                "      FROM lkcrm_crm_business as ccb " +
                "      LEFT JOIN lkcrm_crm_business_status as ccbs ON ccbs.status_id = ccb.status_id " +
                "      where  ccbs.type_id = ? and ccb.cust_id=? and  ccb.owner_user_id in (";
        sql += SqlAppendUtil.sqlAppendWhereIn(userIdss);
        sql += ") ";
        params.add(typeId);
        params.add(BaseUtil.getCustId());
        if (isEnd != null) {
            sql += "  and ccb.is_end = ? ";
            params.add(isEnd);
        }
        if (status == 1) {
            sql += " and to_days(NOW()) = TO_DAYS(create_time) ";
        }
        if (status == 2) {
            sql += " and to_days(NOW()) - TO_DAYS(create_time) = 1 ";
        }
        if (status == 3) {
            sql += " and YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) ";
        }
        if (status == 4) {
            sql += " and YEARWEEK(date_format(create_time,'%Y-%m-%d')) = YEARWEEK(now()) -1 ";
        }
        if (status == 5) {
            sql += " and date_format(create_time,'%Y-%m')=date_format(now(),'%Y-%m') ";
        }
        if (status == 6) {
            sql += "  and date_format(create_time,'%Y-%m')=date_format(DATE_SUB(curdate(), INTERVAL 1 MONTH),'%Y-%m') ";
        }
        if (status == 7) {
            sql += " and QUARTER(create_time)=QUARTER(now()) AND YEAR(create_time)=YEAR(NOW()) ";
        }
        if (status == 8) {
            sql += " and QUARTER(create_time)=QUARTER(DATE_SUB(now(),interval 1 QUARTER)) and YEAR(DATE_SUB(create_time,interval 1 QUARTER)) = YEAR(DATE_SUB(NOW(),interval 1 QUARTER)) " +
                    "           ";
        }
        if (status == 9) {
            sql += " and YEAR(create_time)=YEAR(NOW()) ";
        }
        if (status == 10) {
            sql += " and YEAR(create_time)=YEAR(date_sub(now(),interval 1 year)) ";
        }
        if (status == 11) {
            sql += "            and  TO_DAYS(create_time) >= TO_DAYS(?) " +
                    "            and  TO_DAYS(create_time) <= TO_DAYS(?)";
            params.add(startTime);
            params.add(endTime);
        }
        sql += " GROUP BY ccbs.`name`";
        return queryUniqueSql(sql, params.toArray());
    }

    public Page myInitiate(Object userId, Object categoryId, Object beginDate, Object endDate,
                           Integer page, Integer limit) {
        StringBuffer sqlBuffer = new StringBuffer();
        List<Object> params = new ArrayList<>();
        sqlBuffer.append("    select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id ,c.category_id,c.title as categoryTitle\n" +
                "    from lkcrm_oa_examine a left join lkcrm_oa_examine_record b on a.examine_id = b.examine_id left join lkcrm_oa_examine_category c on a.category_id = c.category_id\n" +
                "    where a.create_user_id = ? and a.cust_id=? and b.cust_id=? and c.cust_id=?");
        params.add(userId);
        if (categoryId != null && categoryId != "") {
            sqlBuffer.append(" and a.category_id = ? ");
            params.add(categoryId);
        }
        if (beginDate != null && endDate != null) {
            sqlBuffer.append(" and a.create_time between #para(startTime) and  #para(endTime) ");
            params.add(beginDate);
            params.add(endDate);
        }
        sqlBuffer.append(" group by a.examine_id,b.record_id order by  a.create_time desc ");
        params.add(BaseUtil.getCustId());
        params.add(BaseUtil.getCustId());
        params.add(BaseUtil.getCustId());
        return sqlPageQuery(sqlBuffer.toString(), page, limit, params.toArray());
    }

    public List<Map<String, Object>> salesTrend(Object sqlDateFormat, Object beginTime, String[] userIds) {
        String sql = "select '" + beginTime + "' as type,IFNULL(SUM(money),0) as contractMoneys, (SELECT IFNULL(SUM(money),0) FROM lkcrm_crm_receivables WHERE DATE_FORMAT( return_time,? ) = ?  and check_status = 2 AND owner_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")) as receivablesMoneys FROM lkcrm_crm_contract as ccco where DATE_FORMAT(ccco.order_date,?)=? and ccco.check_status = 2 AND owner_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")";
        return super.sqlQuery(sql, sqlDateFormat, beginTime, sqlDateFormat, beginTime);
    }
}
