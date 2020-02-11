package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmBusinessEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmBusinessDao extends SimpleHibernateDao<LkCrmBusinessEntity, Integer> {
    public int deleteMember(String memberId, int id) {
        String sql = " update 72crm_crm_business set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where business_id = ?";
        return executeUpdateSQL(sql, memberId, memberId, id);
    }

    public int clearBusinessProduct(int business_id) {
        String sql = " delete from 72crm_crm_business_product where business_id = ?";
        return executeUpdateSQL(sql, business_id);
    }

    public List<Map<String, Object>> queryById(int business_id) {
        String sql = "select * from businessview where business_id = ?";
        return sqlQuery(sql, business_id);
    }

    public List<Map<String, Object>> queryByName(String business_name) {
        String sql = " select * from businessview where business_name = ?";
        return sqlQuery(sql, business_name);
    }

    public List queryProduct(Integer businessId) {
        String sql = " select b.product_id,b.name,b.name as productName,c.name as category_name,b.单位 as unit,a.price,a.sales_price,a.num,a.discount,a.subtotal,b.是否上下架\n" +
                "    from 72crm_crm_business_product as a inner join productview as b inner join 72crm_crm_product_category as c\n" +
                "    where a.product_id = b.product_id and b.category_id = c.category_id and a.business_id = ?  ";
        List param = new ArrayList();
        param.add(businessId);
        return sqlQuery(sql, param);
    }

    public Page pageQueryProduct(int pageNum, int pageSize, Integer businessId) {
        String sql = " select b.product_id,b.name,b.name as productName,c.name as category_name,b.单位 as unit,a.price,a.sales_price,a.num,a.discount,a.subtotal,b.是否上下架\n" +
                "    from 72crm_crm_business_product as a inner join productview as b inner join 72crm_crm_product_category as c\n" +
                "    where a.product_id = b.product_id and b.category_id = c.category_id and a.business_id = ?  ";
        List param = new ArrayList();
        param.add(businessId);
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public List queryContract(Integer businessId) {
        String sql = "  select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time\n" +
                "    from 72crm_crm_contract as a left join 72crm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "    where a.business_id = ?  ";
        List param = new ArrayList();
        param.add(businessId);
        return sqlQuery(sql, param);
    }

    public Page pageQueryContract(int pageNum, int pageSize, Integer businessId) {
        String sql = "  select a.contract_id,a.num,a.name as contract_name,b.customer_name,a.money,a.start_time,a.end_time\n" +
                "    from 72crm_crm_contract as a left join 72crm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "    where a.business_id = ?";
        List param = new ArrayList();
        param.add(businessId);
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public List queryContacts(Integer businessId) {
        String sql = "   select a.contacts_id,a.name,a.mobile,a.post\n" +
                "    from 72crm_crm_contacts as a inner join 72crm_crm_contacts_business as b\n" +
                "    where a.contacts_id = b.contacts_id and b.business_id = ? ";
        List param = new ArrayList();
        param.add(businessId);
        return sqlQuery(sql, param);
    }

    public Page pageQueryContacts(int pageNum, int pageSize, Integer businessId) {
        String sql = "   select a.contacts_id,a.name,a.mobile,a.post\n" +
                "    from 72crm_crm_contacts as a inner join 72crm_crm_contacts_business as b\n" +
                "    where a.contacts_id = b.contacts_id and b.business_id = ?";
        List param = new ArrayList();
        param.add(businessId);
        return sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public int unrelateContacts(Integer businessId, List ids) {
        String sql = "delete from 72crm_crm_contacts_business where business_id = ? and contacts_id in (?)";
        return executeUpdateSQL(sql, businessId, ids);
    }

    public int queryContractNumber(List ids) {
        String sql = " select count(*)  from 72crm_crm_contract as a left join 72crm_crm_customer as b on a.customer_id = b.customer_id\n" +
                "    where a.business_id in (?)";
        return queryForInt(sql, ids);
    }

    public List queryBatchIdByIds(List ids) {
        String sql = "select batch_id from 72crm_crm_business where business_id in (?) ";
        return sqlQuery(sql, ids);
    }

    public int deleteByIds(List ids) {
        String sql = "  delete from 72crm_crm_business where business_id IN(?)";
        return executeUpdateSQL(sql, ids);
    }

    public List queryBusinessStatus(Integer businessId) {
        String sql = "select b.status_id,b.name,b.rate,b.order_num,a.status_id as current_status_id,a.is_end\n" +
                "    from 72crm_crm_business as a inner join 72crm_crm_business_status as b on a.type_id = b.type_id\n" +
                "    where a.business_id = ?";
        List param = new ArrayList();
        param.add(businessId);
        return sqlQuery(sql, param);
    }

    public List getRecord(Integer businessId) {
        String sql = " select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id,a.contacts_ids,a.business_ids\n" +
                "    from 72crm_admin_record as a inner join 72crm_admin_user as b\n" +
                "    where a.create_user_id = b.user_id and ((types = 'crm_business' and types_id = ?) or\n" +
                "    (types = 'crm_customer' and FIND_IN_SET(?,IFNULL(a.business_ids, 0)))) order by a.create_time desc";
        List param = new ArrayList();
        param.add(businessId);
        return sqlQuery(sql, param);
    }
}