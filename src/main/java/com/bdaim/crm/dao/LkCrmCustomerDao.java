package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmCustomerEntity;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmCustomerDao extends SimpleHibernateDao<LkCrmCustomerEntity, Integer> {

    public int todayCustomerNum(String userId) {
        String sql = "select count(*) from lkcrm_crm_customer\n" +
                "  where customer_id not in (IFNULL((select GROUP_CONCAT(types_id) from lkcrm_admin_record where types = 'crm_customer' and to_days(create_time) = to_days(now())),0))\n" +
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

    public int checkContractNum(String userId) {
        String sql = "select count(*)\n" +
                "  from lkcrm_crm_contract as a inner join lkcrm_admin_examine_record as b on a.examine_record_id = b.record_id\n" +
                "  left join lkcrm_admin_examine_log as c on b.record_id = c.record_id\n" +
                "  where c.examine_user = ? and a.check_status in (0,1) and ifnull(b.examine_step_id, 1) = ifnull(c.examine_step_id, 1) and c.is_recheck != 1";
        return queryForInt(sql, userId);
    }

    public int getCustomersByIds(Long userId, List ids, Timestamp createTime) {
        String sql = " update 72crm_crm_customer set owner_user_id = ?,followup = 0,create_time = ? where customer_id in (?)";
        return executeUpdateSQL(sql, userId, createTime, ids);
    }

    public List excelExport(List customer_id) {
        String sql = " select * from customerview where customer_id in (? ) order by update_time desc";
        return sqlQuery(sql, customer_id);
    }

    public List getRecord(Integer customerId) {
        String sql = " select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.business_ids,a.contacts_ids\n" +
                "    from 72crm_admin_record as a inner join 72crm_admin_user as b\n" +
                "    where a.create_user_id = b.user_id and types = 'crm_customer' and types_id = ? order by a.create_time desc";
        return sqlQuery(sql, customerId);
    }

    public int deleteMember(String memberId, int id) {
        String sql = " update 72crm_crm_customer set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where customer_id = ?";
        return executeUpdateSQL(sql, memberId, memberId, id);
    }


    public List<Map<String, Object>> getMembers(Integer owner_user_id) {
        String sql = " select a.user_id as id,a.realname,b.name\n" +
                "    from 72crm_admin_user as a inner join 72crm_admin_dept as b on a.dept_id = b.dept_id\n" +
                "    where a.user_id = ?";
        return sqlQuery(sql, owner_user_id);
    }

    public int lock(int isLock, List ids) {
        String sql = " update 72crm_crm_customer set is_lock = ? where customer_id in (?)";
        return queryForInt(sql, isLock, ids);
    }

    public List<Map<String, Object>> queryReceivables(Integer customerId) {
        String sql = "select a.receivables_id,a.number as receivables_num,b.name as contract_name,b.money as contract_money,a.money as receivables_money,c.realname,\n" +
                "    a.check_status,a.return_time\n" +
                "    from 72crm_crm_receivables as a inner join 72crm_crm_contract as b inner join 72crm_admin_user as c\n" +
                "    where a.contract_id = b.contract_id and a.owner_user_id = c.user_id and a.customer_id = ?";
        return sqlQuery(sql, customerId);
    }

    public Page pageQueryReceivables(int pageNum, int pageSize, Integer customerId) {
        String sql = "select a.receivables_id,a.number as receivables_num,b.name as contract_name,b.money as contract_money,a.money as receivables_money,c.realname,\n" +
                "    a.check_status,a.return_time\n" +
                "    from 72crm_crm_receivables as a inner join 72crm_crm_contract as b inner join 72crm_admin_user as c\n" +
                "    where a.contract_id = b.contract_id and a.owner_user_id = c.user_id and a.customer_id = ?";
        return sqlPageQuery(sql, pageNum, pageSize, customerId);
    }

    public List<Map<String, Object>> queryReceivablesPlan(Integer customerId) {
        String sql = " select a.plan_id,a.num,b.customer_name,c.num as contract_num,a.money,a.return_date,a.return_type,a.remind,a.remark\n" +
                "    from 72crm_crm_receivables_plan as a inner join 72crm_crm_customer as b\n" +
                "    inner join 72crm_crm_contract as c\n" +
                "    where a.customer_id = b.customer_id and a.contract_id = c.contract_id and b.customer_id = ?";
        return sqlQuery(sql, customerId);
    }

    public Page pageQueryReceivablesPlan(int pageNum, int pageSize, Integer customerId) {
        String sql = " select a.plan_id,a.num,b.customer_name,c.num as contract_num,a.money,a.return_date,a.return_type,a.remind,a.remark\n" +
                "    from 72crm_crm_receivables_plan as a inner join 72crm_crm_customer as b\n" +
                "    inner join 72crm_crm_contract as c\n" +
                "    where a.customer_id = b.customer_id and a.contract_id = c.contract_id and b.customer_id = ?";
        return sqlPageQuery(sql, pageNum, pageSize, customerId);
    }

    public List<Map<String, Object>> queryPassContract(Integer customerId, Integer checkstatus, String search) {
        String sql = "  select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time\n" +
                "    from 72crm_crm_contract as a inner join 72crm_crm_customer as b on a.customer_id = b.customer_id\n" +
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
                "    from 72crm_crm_contract as a inner join 72crm_crm_customer as b on a.customer_id = b.customer_id\n" +
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
                "    ifnull((select sum(c.money) from `72crm_crm_receivables` c where c.contract_id = a.contract_id and c.check_status = 2),0) as receivablesMoneyCount\n" +
                "    from 72crm_crm_contract as a inner join 72crm_crm_customer as b on a.customer_id = b.customer_id\n" +
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
                "    ifnull((select sum(c.money) from `72crm_crm_receivables` c where c.contract_id = a.contract_id and c.check_status = 2),0) as receivablesMoneyCount\n" +
                "    from 72crm_crm_contract as a inner join 72crm_crm_customer as b on a.customer_id = b.customer_id\n" +
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
                "    from 72crm_crm_business as a inner join 72crm_crm_customer as b inner join 72crm_crm_business_type as c inner join\n" +
                "    72crm_crm_business_status as d\n" +
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
                "    from 72crm_crm_business as a inner join 72crm_crm_customer as b inner join 72crm_crm_business_type as c inner join\n" +
                "    72crm_crm_business_status as d\n" +
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
        String sql = " select count(*) from 72crm_crm_contacts where customer_id = ? ";
        return queryForInt(sql, customerIds);
    }

    public int queryBusinessNumber(String customerIds) {
        String sql = "  select count(*) from 72crm_crm_business where customer_id = ? ";
        return queryForInt(sql, customerIds);
    }

    public List queryBatchIdByIds(List idsArr) {
        String sql = " select batch_id from 72crm_crm_customer where customer_id in (?) ";
        return sqlQuery(sql, idsArr);
    }

    public int deleteByIds(List customerId) {
        String sql = " delete from 72crm_crm_customer where customer_id in (?)  ";
        return executeUpdateSQL(sql, customerId);
    }

    public List queryContacts(Integer customerId, String search) {
        String sql = " select contacts_id,name,mobile,post,telephone,是否关键决策人 from contactsview where customer_id = #para(customerId)  ";
        List param = new ArrayList();
        param.add(customerId);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlQuery(sql, param);
    }

    public Page pageQueryContacts(int pageNum, int pageSize, Integer customerId, String search) {
        String sql = "select contacts_id,name,mobile,post,telephone,是否关键决策人 from contactsview where customer_id = #para(customerId)  ";
        List param = new ArrayList();
        param.add(customerId);
        if (StringUtil.isNotEmpty(search)) {
            sql += "  and a.name like CONCAT('%',?,'%')";
            param.add(search);
        }
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public List<Map<String, Object>> queryByName(String customer_name) {
        String sql = "select * from customerview where customer_name = ? ";
        return sqlQuery(sql, customer_name);
    }

    public List<Map<String, Object>> queryById(Integer customer_id) {
        String sql = " select *,(IF(owner_user_id is null,1,0)) as is_pool from customerview  where customer_id = ? ";
        return sqlQuery(sql, customer_id);
    }

    public Page getCustomerPageList(int pageNum, int pageSize, String customerName,String mobile,String telephone) {
        String sql = " select customer_id,customer_name,owner_user_name from customerview where 1=1";
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
        String sql = "update 72crm_crm_customer set followup = 1 where customer_id in(?)";
        return executeUpdateSQL(sql, ids);
    }

}