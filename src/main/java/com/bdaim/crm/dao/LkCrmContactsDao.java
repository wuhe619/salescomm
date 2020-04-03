package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmContactsEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmContactsDao extends SimpleHibernateDao<LkCrmContactsEntity, Integer> {

    public List queryByIds(List deptIds) {
        return super.queryListBySql("  select * from  lkcrm_crm_contacts" +
                "       where contacts_id in  (" + SqlAppendUtil.sqlAppendWhereIn(deptIds) + ")");
    }

    public List getContactsPageList(String contactsName, String telephone, String mobile, String customerName) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        String sql = "select contacts_id,name,customer_name,owner_user_name from " + contactsview + " where 1=1";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(contactsName)) {
            param.add(contactsName);
            sql += "and name like CONCAT('%',?,'%')";
        }
        if (StringUtil.isNotEmpty(customerName)) {
            param.add(customerName);
            sql += "and customer_name like CONCAT('%',?,'%')";
        }
        if (StringUtil.isNotEmpty(telephone)) {
            param.add(telephone);
            sql += "  and telephone = ?";
        }
        if (StringUtil.isNotEmpty(mobile)) {
            param.add(mobile);
            sql += "  and telephone = ?";
        }
        return sqlQuery(sql, param.toArray());
    }

    public Page pageContactsPageList(int pageNum, int pageSize, String contactsName, String telephone, String mobile, String customerName) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        String sql = "select contacts_id,name,customer_name,owner_user_name from " + contactsview + " where 1=1";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(contactsName)) {
            param.add(contactsName);
            sql += "and name like CONCAT('%',?,'%')";
        }
        if (StringUtil.isNotEmpty(customerName)) {
            param.add(customerName);
            sql += "and customer_name like CONCAT('%',?,'%')";
        }
        if (StringUtil.isNotEmpty(telephone)) {
            param.add(telephone);
            sql += "  and telephone = ?";
        }
        if (StringUtil.isNotEmpty(mobile)) {
            param.add(mobile);
            sql += "  and telephone = ?";
        }
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public List<Map<String, Object>> queryById(int contacts_id) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        String sql = "select * from " + contactsview + " where contacts_id = ?";
        List param = new ArrayList();
        param.add(contacts_id);
        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps;
    }

    public List<Map<String, Object>> queryByListId(List ids) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        String sql = "select * from " + contactsview + " where contacts_id IN("+SqlAppendUtil.sqlAppendWhereIn(ids)+")";
        List<Map<String, Object>> maps = sqlQuery(sql);
        return maps;
    }

    public List<Map<String, Object>> queryByName(String name) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        String sql = " select * from " + contactsview + " where name = ?";
        List param = new ArrayList();
        param.add(name);
        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps;
    }

    public List<Map<String, Object>> queryBusiness(Integer contactsId) {
        String sql = "  select a.business_id,a.business_name,a.money,f.customer_name,d.name as type_name,e.name as status_name\n" +
                "    from lkcrm_crm_business as a inner join lkcrm_crm_contacts_business as b inner join lkcrm_crm_contacts as c\n" +
                "    inner join lkcrm_crm_business_type as d inner join lkcrm_crm_business_status as e\n" +
                "    inner join lkcrm_crm_customer as f on a.customer_id = f.customer_id\n" +
                "    where a.business_id = b.business_id and b.contacts_id = c.contacts_id and a.type_id = d.type_id\n" +
                "    and a.status_id = e.status_id and c.contacts_id = ?";
        List param = new ArrayList();
        param.add(contactsId);
        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps;
    }

    public Page pageQueryBusiness(int pageNum, int pageSize, Integer contactsId) {
        String sql = "  select a.business_id,a.business_name,a.money,f.customer_name,d.name as type_name,e.name as status_name\n" +
                "    from lkcrm_crm_business as a inner join lkcrm_crm_contacts_business as b inner join lkcrm_crm_contacts as c\n" +
                "    inner join lkcrm_crm_business_type as d inner join lkcrm_crm_business_status as e\n" +
                "    inner join lkcrm_crm_customer as f on a.customer_id = f.customer_id\n" +
                "    where a.business_id = b.business_id and b.contacts_id = c.contacts_id and a.type_id = d.type_id\n" +
                "    and a.status_id = e.status_id and c.contacts_id = ?";
        List param = new ArrayList();
        param.add(contactsId);
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public int unrelateBusiness(Integer contactsId, List ids) {
        String sql = "  delete from lkcrm_crm_contacts_business where contacts_id =? and business_id in ( " + SqlAppendUtil.sqlAppendWhereIn(ids) + ") ";
        List param = new ArrayList();
        param.add(contactsId);
        return executeUpdateSQL(sql, param.toArray());
    }

    public List<Map<String, Object>> queryBatchIdByIds(List ids) {
        String sql = " select batch_id from lkcrm_crm_contacts where contacts_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ") ";
        List param = new ArrayList();
        List<Map<String, Object>> maps = sqlQuery(sql);
        return maps;
    }

    public int deleteByIds(List contactsId) {
        String sql = " delete from lkcrm_crm_contacts where contacts_id in( " + SqlAppendUtil.sqlAppendWhereIn(contactsId) + ")";
        return executeUpdateSQL(sql);
    }


    public int transfer(String ownerUserId, List ids) {
        String sql = " update lkcrm_crm_contacts set owner_user_id =? where contacts_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ")";
        List param = new ArrayList();
        param.add(ownerUserId);
        return executeUpdateSQL(sql, param.toArray());
    }


    public List<Map<String, Object>> getRecord(Integer contacts_id, int pageNum, int pageSize) {
        String sql = " select a.record_id, b.img AS user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.contacts_ids,a.business_ids " +
                "    from lkcrm_admin_record as a inner join lkcrm_admin_user as b LEFT JOIN lkcrm_task AS c ON a.task_id = c.task_id " +
                "    where a.create_user_id = b.user_id and ((types = 'crm_contacts' and types_id = ?) or " +
                "    (types = 'crm_customer' and FIND_IN_SET(?,IFNULL(a.contacts_ids,0)))) AND (a.task_id IS NULL OR c.`status`=5) order by a.create_time desc";
        List param = new ArrayList();
        param.add(contacts_id);
        param.add(contacts_id);
        List<Map<String, Object>> maps = sqlPageQuery(sql, pageNum, pageSize, param.toArray()).getData();
        return maps;
    }

    public List<Map<String, Object>> getRecord(Integer contacts_id, int taskStatus, int pageNum, int pageSize) {
        String sql = " select a.record_id, b.img AS user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.contacts_ids,a.business_ids, c.task_id, c.name taskName " +
                "    from lkcrm_admin_record as a inner join lkcrm_admin_user as b LEFT JOIN lkcrm_task AS c ON a.task_id = c.task_id " +
                "    where a.create_user_id = b.user_id and ((types = 'crm_contacts' and types_id = ?) or " +
                "    (types = 'crm_customer' and FIND_IN_SET(?,IFNULL(a.contacts_ids,0)))) AND c.`status` = ?  order by a.create_time desc";
        List param = new ArrayList();
        param.add(contacts_id);
        param.add(contacts_id);
        param.add(taskStatus);
        List<Map<String, Object>> maps = sqlPageQuery(sql, pageNum, pageSize, param.toArray()).getData();
        return maps;
    }


    public List<Map<String, Object>> excelExport(List ids) {
        String contactsview = BaseUtil.getViewSql("contactsview");
        String sql = " select * from " + contactsview + " where contacts_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ") order by update_time desc";
        List param = new ArrayList();
        List<Map<String, Object>> maps = sqlQuery(sql);
        return maps;
    }


    public List<Map<String, Object>> queryRepeatFieldNumber(String contactsName, String telephone, String mobile) {
        String sql = " select count(*) as number from lkcrm_crm_contacts where 1=2 ";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(contactsName)) {
            param.add(contactsName);
            sql += "or name like CONCAT('%',?,'%')";
        }
        if (StringUtil.isNotEmpty(mobile)) {
            param.add(mobile);
            sql += "or mobile=? ";
        }
        if (StringUtil.isNotEmpty(telephone)) {
            param.add(telephone);
            sql += " or telephone = ?";
        }
        return sqlQuery(sql, param.toArray());
    }

    public List<Map<String, Object>> queryRepeatField(String contactsName, String telephone, String mobile) {
        String sql = "  select contacts_id,batch_id from lkcrm_crm_contacts where 1=2";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(contactsName)) {
            param.add(contactsName);
            sql += "or name like CONCAT('%',?,'%')";
        }
        if (StringUtil.isNotEmpty(mobile)) {
            param.add(mobile);
            sql += "or mobile=? ";
        }
        if (StringUtil.isNotEmpty(telephone)) {
            param.add(telephone);
            sql += " or telephone = ?";
        }
        return sqlQuery(sql, param.toArray());
    }


}
