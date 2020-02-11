package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmContactsEntity;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmContactsDao extends SimpleHibernateDao<LkCrmContactsEntity, Integer> {

    public List getContactsPageList(String contactsName, String telephone, String mobile, String customerName) {
        String sql = "select contacts_id,name,customer_name,owner_user_name from contactsview where 1=1";
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
        String sql = "select contacts_id,name,customer_name,owner_user_name from contactsview where 1=1";
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
        String sql = "select * from contactsview where contacts_id = ?";
        List param = new ArrayList();
        param.add(contacts_id);
        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps;
    }

    public List<Map<String, Object>> queryByName(String name) {
        String sql = " select * from contactsview where name = ?";
        List param = new ArrayList();
        param.add(name);
        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps;
    }

    public List<Map<String, Object>> queryBusiness(Integer contactsId) {
        String sql = "  select a.business_id,a.business_name,a.money,f.customer_name,d.name as type_name,e.name as status_name\n" +
                "    from 72crm_crm_business as a inner join 72crm_crm_contacts_business as b inner join 72crm_crm_contacts as c\n" +
                "    inner join 72crm_crm_business_type as d inner join 72crm_crm_business_status as e\n" +
                "    inner join 72crm_crm_customer as f on a.customer_id = f.customer_id\n" +
                "    where a.business_id = b.business_id and b.contacts_id = c.contacts_id and a.type_id = d.type_id\n" +
                "    and a.status_id = e.status_id and c.contacts_id = ?";
        List param = new ArrayList();
        param.add(contactsId);
        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps;
    }

    public Page pageQueryBusiness(int pageNum, int pageSize, Integer contactsId) {
        String sql = "  select a.business_id,a.business_name,a.money,f.customer_name,d.name as type_name,e.name as status_name\n" +
                "    from 72crm_crm_business as a inner join 72crm_crm_contacts_business as b inner join 72crm_crm_contacts as c\n" +
                "    inner join 72crm_crm_business_type as d inner join 72crm_crm_business_status as e\n" +
                "    inner join 72crm_crm_customer as f on a.customer_id = f.customer_id\n" +
                "    where a.business_id = b.business_id and b.contacts_id = c.contacts_id and a.type_id = d.type_id\n" +
                "    and a.status_id = e.status_id and c.contacts_id = ?";
        List param = new ArrayList();
        param.add(contactsId);
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public int unrelateBusiness(Integer contactsId, List ids) {
        String sql = "  delete from 72crm_crm_contacts_business where contacts_id =?and business_id in ( ?) ";
        List param = new ArrayList();
        param.add(contactsId);
        param.add(ids);
        return executeUpdateSQL(sql, param.toArray());
    }

    public List<Map<String, Object>> queryBatchIdByIds(List ids) {
        String sql = " select batch_id from 72crm_crm_contacts where contacts_id in (?) ";
        List param = new ArrayList();
        param.add(ids);
        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps;
    }

    public int deleteByIds(List contactsId) {
        String sql = " delete from 72crm_crm_contacts where contacts_id in( ?)";
        List param = new ArrayList();
        param.add(contactsId);
        return executeUpdateSQL(sql, param.toArray());
    }


    public int transfer(String ownerUserId, List ids) {
        String sql = " update 72crm_crm_contacts set owner_user_id =? where contacts_id in ( ?)";
        List param = new ArrayList();
        param.add(ownerUserId);
        param.add(ids);
        return executeUpdateSQL(sql, param.toArray());
    }


    public List<Map<String, Object>> getRecord(Integer contacts_id) {
        String sql = " select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.contacts_ids,a.business_ids\n" +
                "    from 72crm_admin_record as a inner join 72crm_admin_user as b\n" +
                "    where a.create_user_id = b.user_id and ((types = 'crm_contacts' and types_id = ?) or\n" +
                "    (types = 'crm_customer' and FIND_IN_SET(?,IFNULL(a.contacts_ids,0)))) order by a.create_time desc";
        List param = new ArrayList();
        param.add(contacts_id);
        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps;
    }

    public List<Map<String, Object>> excelExport(List ids) {
        String sql = " select * from contactsview where contacts_id in (?) order by update_time desc";
        List param = new ArrayList();
        param.add(ids);
        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps;
    }


    public List<Map<String, Object>> queryRepeatFieldNumber(String contactsName, String telephone, String mobile) {
        String sql = " select count(*) as number from 72crm_crm_contacts where 1=2";
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
        String sql = "  select contacts_id,batch_id from 72crm_crm_contacts where 1=2";
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