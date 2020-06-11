package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmCustomerEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmCustomerDao extends SimpleHibernateDao<LkCrmCustomerEntity, Integer> {

    public List queryByIds(List deptIds) {
        return super.queryListBySql("  select * from  lkcrm_crm_customer\n" +
                "       where customer_id in  (" + SqlAppendUtil.sqlAppendWhereIn(deptIds) + ")");
    }

    public int todayCustomerNum(String userId) {
        String sql = "select count(*) from lkcrm_crm_customer\n" +
                "  where customer_id not in (IFNULL((select GROUP_CONCAT(types_id) from lkcrm_admin_record where types = 'crm_customer' and to_days(create_time) = to_days(now())),0))\n" +
                "  and to_days(next_time) = to_days(now()) and owner_user_id = ?";
        return queryForInt(sql, userId);
    }

    public int todayLeadsNum(String userId) {
        String sql = "select count(*) from lkcrm_crm_leads " +
                "  where leads_id not in (IFNULL((select GROUP_CONCAT(types_id) from lkcrm_admin_record where types = 'crm_leads' and to_days(create_time) = to_days(now())),0))\n" +
                "  and to_days(next_time) = to_days(now()) and owner_user_id = ?";
        return queryForInt(sql, userId);
    }

    public int followLeadsNum(String userId) {
        String sql = "select count(*) from lkcrm_crm_leads as a\n" +
                "  where followup = 0\n" +
                "  and is_transform = 0  and owner_user_id = ?";
        return queryForInt(sql, userId);
    }

    public int followCustomerNum(String userId) {
        String sql = "select count(*) from lkcrm_crm_customer as a\n" +
                "  where followup = 0\n" +
                "  and owner_user_id = ?";
        return queryForInt(sql, userId);
    }

    public int checkReceivablesNum(String userId) {
        String sql = "select count(*)\n" +
                "  from lkcrm_crm_receivables as a inner join lkcrm_admin_examine_record as b on a.examine_record_id = b.record_id\n" +
                "  left join lkcrm_admin_examine_log as c on b.record_id = c.record_id\n" +
                "  where c.examine_user = ? and a.check_status in (0,1) and ifnull(b.examine_step_id, 1) = ifnull(c.examine_step_id, 1) and c.is_recheck != 1";
        return queryForInt(sql, userId);
    }

    public int remindReceivablesPlanNum(String userId) {
        String sql = "select count(*)\n" +
                "  from lkcrm_crm_receivables_plan as a inner join lkcrm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "  inner join lkcrm_crm_contract as c on a.contract_id = c.contract_id\n" +
                "  where to_days(a.return_date) >= to_days(now()) and to_days(a.return_date) <= to_days(now())+a.remind\n" +
                "  and receivables_id is null and c.owner_user_id = ?";
        return queryForInt(sql, userId);
    }

    public int endContractNum(String value, String userId) {
        String sql = "select count(*) from lkcrm_crm_contract as a inner join lkcrm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "  where to_days(a.end_time) >= to_days(now()) and to_days(a.end_time) <= to_days(now()) + IFNULL(?,0) and a.owner_user_id = ?";
        return queryForInt(sql, value, userId);
    }


    /**
     * 即将到期的客户
     * @param followupDay 未跟进天数
     * @param dealDay 未创建商机或合同
     * @param userId
     * @return
     */
    public int endCustomerNum(int followupDay, int dealDay, String userId) {
        String sql = "SELECT COUNT(0) FROM lkcrm_crm_customer AS ccc WHERE owner_user_id != 0 AND deal_status = '未成交' AND is_lock = 0 " +
                " AND( ( to_days(now()) - to_days( IFNULL( ( SELECT car.create_time FROM lkcrm_admin_record AS car WHERE car.types = 'crm_customer' " +
                " AND car.types_id = ccc.customer_id ORDER BY car.create_time DESC LIMIT 1), ccc.create_time ) ) ) >= abs(?) " +
                " OR (( to_days(now()) - to_days(create_time) ) >= abs(?) AND ( (SELECT count(customer_id) FROM lkcrm_crm_business WHERE customer_id = ccc.customer_id  )=0 AND (SELECT count(customer_id)  FROM lkcrm_crm_contract WHERE customer_id = ccc.customer_id  )=0) )) " +
                " AND ccc.owner_user_id = ?";
        return queryForInt(sql, followupDay, dealDay, userId);
    }

    public int checkContractNum(String userId) {
        String sql = "select count(*)\n" +
                "  from lkcrm_crm_contract as a inner join lkcrm_admin_examine_record as b on a.examine_record_id = b.record_id\n" +
                "  left join lkcrm_admin_examine_log as c on b.record_id = c.record_id\n" +
                "  where c.examine_user = ? and a.check_status in (0,1) and ifnull(b.examine_step_id, 1) = ifnull(c.examine_step_id, 1) and c.is_recheck != 1";
        return queryForInt(sql, userId);
    }

    public int getCustomersByIds(Long userId, List ids, Timestamp createTime) {
        String sql = " update lkcrm_crm_customer set owner_user_id = ?,followup = 0,create_time = ? where customer_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ")";
        return executeUpdateSQL(sql, userId, createTime);
    }

    public List excelExport(List customer_id) {
        String customerview = BaseUtil.getViewSql("customerview");
        String sql = " select * from " + customerview + " where customer_id in (" + SqlAppendUtil.sqlAppendWhereIn(customer_id) + ") order by update_time desc";
        return sqlQuery(sql);
    }

    public List getRecord(Integer customerId, int pageNum, int pageSize) {
        String sql = " select a.record_id, b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.business_ids,a.contacts_ids\n" +
                "    from lkcrm_admin_record as a inner join lkcrm_admin_user as b LEFT JOIN lkcrm_task AS c ON a.task_id = c.task_id " +
                "    where a.create_user_id = b.user_id and types = 'crm_customer' and types_id = ? AND (a.task_id IS NULL OR c.`status`=5) order by a.create_time desc";
        return sqlPageQuery(sql, pageNum, pageSize, customerId).getData();
    }

    public List getRecord(String leadsId, int taskStatus, int pageNum, int pageSize) {
        String sql = " select a.record_id, b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.business_ids,a.contacts_ids, c.task_id, c.name taskName" +
                "    from lkcrm_admin_record as a inner join lkcrm_admin_user as b LEFT JOIN lkcrm_task AS c ON a.task_id = c.task_id " +
                "    where a.create_user_id = b.user_id and types = 'crm_customer' AND c.`status` = ? and types_id = ? and a.cust_id = ? order by a.create_time desc";
        return this.sqlPageQuery(sql, pageNum, pageSize, taskStatus, leadsId, BaseUtil.getUser().getCustId()).getData();
    }

    public int deleteMember(String memberId, int id) {
        String sql = " update lkcrm_crm_customer set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where customer_id = ?";
        return executeUpdateSQL(sql, memberId, memberId, id);
    }


    public Map<String, Object> getMembers(Long owner_user_id) {
       /* String sql = " select a.user_id as id,a.realname,b.name\n" +
                "    from lkcrm_admin_user as a inner join lkcrm_admin_dept as b on a.dept_id = b.dept_id\n" +
                "    where a.user_id = ?";*/
        String sql = " select a.id as id,a.realname,'默认部门' AS name  from t_customer_user as a where a.id = ? ";
        List<Map<String, Object>> maps = sqlQuery(sql, owner_user_id);
        if (maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }

    public int lock(int isLock, List ids) {
        String sql = " update lkcrm_crm_customer set is_lock = ? where customer_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ")";
        return executeUpdateSQL(sql, isLock);
    }

    public List<Map<String, Object>> queryReceivables(Integer customerId) {
        String sql = "select a.receivables_id,a.number as receivables_num,b.name as contract_name,b.money as contract_money,a.money as receivables_money,c.realname,\n" +
                "    a.check_status,a.return_time\n" +
                "    from lkcrm_crm_receivables as a inner join lkcrm_crm_contract as b inner join t_customer_user as c\n" +
                "    where a.contract_id = b.contract_id and a.owner_user_id = c.id and a.customer_id = ?";
        return sqlQuery(sql, customerId);
    }

    public Page pageQueryReceivables(int pageNum, int pageSize, Integer customerId) {
        String sql = "select a.receivables_id,a.number as receivables_num,b.name as contract_name,b.money as contract_money,a.money as receivables_money,c.realname,\n" +
                "    a.check_status,a.return_time\n" +
                "    from lkcrm_crm_receivables as a inner join lkcrm_crm_contract as b inner join lkcrm_admin_user as c\n" +
                "    where a.contract_id = b.contract_id and a.owner_user_id = c.user_id and a.customer_id = ?";
        return sqlPageQuery(sql, pageNum, pageSize, customerId);
    }

    public List<Map<String, Object>> queryReceivablesPlan(Integer customerId) {
        String sql = " select a.plan_id,a.num,b.customer_name,c.num as contract_num,a.money,a.return_date,a.return_type,a.remind,a.remark\n" +
                "    from lkcrm_crm_receivables_plan as a inner join lkcrm_crm_customer as b\n" +
                "    inner join lkcrm_crm_contract as c\n" +
                "    where a.customer_id = b.customer_id and a.contract_id = c.contract_id and b.customer_id = ?";
        return sqlQuery(sql, customerId);
    }

    public Page pageQueryReceivablesPlan(int pageNum, int pageSize, Integer customerId) {
        String sql = " select a.plan_id,a.num,b.customer_name,c.num as contract_num,a.money,a.return_date,a.return_type,a.remind,a.remark\n" +
                "    from lkcrm_crm_receivables_plan as a inner join lkcrm_crm_customer as b\n" +
                "    inner join lkcrm_crm_contract as c\n" +
                "    where a.customer_id = b.customer_id and a.contract_id = c.contract_id and b.customer_id = ?";
        return sqlPageQuery(sql, pageNum, pageSize, customerId);
    }

    public List<Map<String, Object>> queryPassContract(Integer customerId, Integer checkstatus, String search) {
        String sql = "  select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time\n" +
                "    from lkcrm_crm_contract as a inner join lkcrm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "    where a.customer_id = ? and a.check_status = ? ";
        List param = new ArrayList();
        param.add(customerId);
        param.add(checkstatus);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlQuery(sql, param.toArray());
    }

    public Page pageQueryPassContract(int pageNum, int pageSize, Integer customerId, Integer checkstatus, String search) {
        String sql = "  select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time\n" +
                "    from lkcrm_crm_contract as a inner join lkcrm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "    where a.customer_id = ? and a.check_status = ? ";
        List param = new ArrayList();
        param.add(customerId);
        param.add(checkstatus);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public List<Map<String, Object>> queryContract(Integer customerId, String search) {
        String sql = "  select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time,\n" +
                "    ifnull((select sum(c.money) from `lkcrm_crm_receivables` c where c.contract_id = a.contract_id and c.check_status = 2),0) as receivablesMoneyCount\n" +
                "    from lkcrm_crm_contract as a inner join lkcrm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "    where a.customer_id =? ";
        List param = new ArrayList();
        param.add(customerId);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlQuery(sql, param.toArray());
    }

    public Page pageQueryContract(int pageNum, int pageSize, Integer customerId, String search) {
        String sql = "  select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time,\n" +
                "    ifnull((select sum(c.money) from `lkcrm_crm_receivables` c where c.contract_id = a.contract_id and c.check_status = 2),0) as receivablesMoneyCount\n" +
                "    from lkcrm_crm_contract as a inner join lkcrm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "    where a.customer_id =? ";
        List param = new ArrayList();
        param.add(customerId);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public List<Map<String, Object>> queryBusiness(Integer customerId, String search) {
        String sql = "  select a.business_id,a.business_name,a.money,a.is_end,a.type_id,a.status_id,b.customer_name,c.name as type_name,d.name as status_name\n" +
                "    from lkcrm_crm_business as a inner join lkcrm_crm_customer as b inner join lkcrm_crm_business_type as c inner join\n" +
                "    lkcrm_crm_business_status as d\n" +
                "    where a.customer_id = b.customer_id and a.type_id = c.type_id and a.status_id = d.status_id and a.customer_id = ? ";
        List param = new ArrayList();
        param.add(customerId);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlQuery(sql, param.toArray());
    }

    public Page pageQueryBusiness(int pageNum, int pageSize, Integer customerId, String search) {
        String sql = "  select a.business_id,a.business_name,a.money,a.is_end,a.type_id,a.status_id,b.customer_name,c.name as type_name,d.name as status_name\n" +
                "    from lkcrm_crm_business as a inner join lkcrm_crm_customer as b inner join lkcrm_crm_business_type as c inner join\n" +
                "    lkcrm_crm_business_status as d\n" +
                "    where a.customer_id = b.customer_id and a.type_id = c.type_id and a.status_id = d.status_id and a.customer_id = ? ";
        List param = new ArrayList();
        param.add(customerId);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public int queryContactsNumber(String customerIds) {
        String sql = " select count(*) from lkcrm_crm_contacts where customer_id = ? ";
        return queryForInt(sql, customerIds);
    }

    public int queryBusinessNumber(String customerIds) {
        String sql = "  select count(*) from lkcrm_crm_business where customer_id = ? ";
        return queryForInt(sql, customerIds);
    }

    public List queryBatchIdByIds(List idsArr) {
        String sql = " select batch_id from lkcrm_crm_customer where customer_id in (" + SqlAppendUtil.sqlAppendWhereIn(idsArr) + ") ";
        return sqlQuery(sql);
    }

    public int deleteByIds(List customerId) {
        String sql = " delete from lkcrm_crm_customer where customer_id in (" + SqlAppendUtil.sqlAppendWhereIn(customerId) + ")  ";
        return executeUpdateSQL(sql);
    }

    public List queryContacts(Integer customerId, String search) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        String sql = " select contacts_id,name,mobile,post,telephone,是否关键决策人 from " + contactsview + " where customer_id = ?  ";
        List param = new ArrayList();
        param.add(customerId);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlQuery(sql, param.toArray());
    }

    public Page pageQueryContacts(int pageNum, int pageSize, Integer customerId, String search) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        String sql = "select contacts_id,name,mobile,post,telephone,是否关键决策人 from " + contactsview + " where customer_id = ? ";
        List param = new ArrayList();
        param.add(customerId);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public Map<String, Object> queryByName(String customer_name) {
        String customerview = BaseUtil.getViewSql("customerview");
        String sql = "select * from " + customerview + " where customer_name = ? ";
        List<Map<String, Object>> maps = sqlQuery(sql, customer_name);
        return maps.size() > 0 ? maps.get(0) : null;
    }

    public List<Map<String, Object>> queryById(Integer customer_id) {
        String customerview = BaseUtil.getViewSql("customerview");
        String sql = " select *,(IF(owner_user_id is null,1,0)) as is_pool from " + customerview + "  where customer_id = ? ";
        return sqlQuery(sql, customer_id);
    }

    public List<Map<String, Object>> queryByListId(List ids) {
        String customerview = BaseUtil.getViewSql("customerview");
        String sql = " select *,(IF(owner_user_id is null,1,0)) as is_pool from " + customerview + "  where customer_id IN(" + SqlAppendUtil.sqlAppendWhereIn(ids) + ") ";
        return sqlQuery(sql);
    }

    public Page getCustomerPageList(int pageNum, int pageSize, String customerName, String mobile, String telephone) {
        String customerview = BaseUtil.getViewSql("customerview");
        String sql = " select customer_id,customer_name,owner_user_name from " + customerview + " where 1=1";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(customerName)) {
            sql += "   and customer_name like CONCAT('%',?,'%') ";
            param.add(customerName);
        }
        if (StringUtil.isNotEmpty(mobile)) {
            sql += "  and mobile = ? ";
            param.add(mobile);
        }
        if (StringUtil.isNotEmpty(telephone)) {
            sql += "  and telephone = ? ";
            param.add(telephone);
        }
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public int setCustomerFollowup(List ids) {
        String sql = "update lkcrm_crm_customer set followup = 1 where customer_id in(" + SqlAppendUtil.sqlAppendWhereIn(ids) + ")";
        return executeUpdateSQL(sql);
    }

    public int updateDealStatusById(String deal_status, int customer_id) {
        String sql = "  update lkcrm_crm_customer set deal_status = ? where customer_id = ?";
        return executeUpdateSQL(sql, deal_status, customer_id);
    }

}
