package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminFieldEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldStyleEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminFieldDao extends SimpleHibernateDao<LkCrmAdminFieldEntity, Integer> {

    public List<LkCrmAdminFieldEntity> queryDefaultCustomerFieldList() {
        String sql = "from LkCrmAdminFieldEntity where custId is null ";
        return find(sql);
    }

    public List customerFieldList(String label) {
        String sql = "select field_id,field_name,name,type,options from lkcrm_admin_field where field_type = 0 and label = ? AND cust_id = ? ";
        return sqlQuery(sql, label, BaseUtil.getCustId());
    }

    public int deleteByChooseId(List<Integer> field_ids, int label, Integer categoryId) {
        List param = new ArrayList();
        param.add(BaseUtil.getCustId());
        param.add(label);
        String sql = "DELETE FROM lkcrm_admin_field WHERE cust_id = ? AND field_id not in(" + SqlAppendUtil.sqlAppendWhereIn(field_ids.toArray()) + ") and (operating = '0' or operating = '2') and label= ?  ";
        if (10 == label) {
            sql += " and examine_category_id=?";
            param.add(categoryId);
        }
        return executeUpdateSQL(sql, param.toArray());
    }

    public int deleteByFieldValue(List<Integer> field_ids, int label, Integer categoryId) {
        List param = new ArrayList();
        String custId = BaseUtil.getCustId();
        param.add(custId);
        param.add(custId);
        param.add(label);
        String sql = "DELETE FROM lkcrm_admin_fieldv WHERE cust_id = ? AND field_id in( SELECT field_id FROM lkcrm_admin_field WHERE field_id not in(" + SqlAppendUtil.sqlAppendWhereIn(field_ids.toArray()) + " AND cust_id = ?  ) and (operating = '0' or operating = '2') and label=? ";
        if (10 == label) {
            sql += " and examine_category_id=? ";
            param.add(categoryId);
        }
        sql += " ) ";
        return executeUpdateSQL(sql, param.toArray());
    }

    public int deleteFieldSort(List<Integer> fieldIds, int label) {
        List param = new ArrayList();
        param.add(label);
        String sql = " delete from lkcrm_admin_field_sort where label = ? and field_id in(" + SqlAppendUtil.sqlAppendWhereIn(fieldIds) + ") ";
        return executeUpdateSQL(sql, param.toArray());
    }

    public int deleteFieldSortByNames(List<String> names, int label) {
        List param = new ArrayList();
        param.add(label);
        String sql = " delete from lkcrm_admin_field_sort where label = ? and name in(" + SqlAppendUtil.sqlAppendWhereIn(names) + ") ";
        return executeUpdateSQL(sql, param.toArray());
    }

    public List queryFields() {
        String custId = BaseUtil.getCustId();
        String sql = "SELECT IFNULL(label,'1') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'线索管理' as name,'crm_leads' as types FROM lkcrm_admin_field WHERE label='1' AND cust_id = '" + custId + "' " +
                "      union all\n" +
                "      SELECT IFNULL(label,'2') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'客户管理' as name,'crm_customer' as types FROM lkcrm_admin_field WHERE label='2' AND cust_id = '" + custId + "'" +
                "      union all\n" +
                "      SELECT IFNULL(label,'3') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'联系人管理' as name,'crm_contacts' as types FROM lkcrm_admin_field WHERE label='3' AND cust_id = '" + custId + "'" +
                "      union all\n" +
                "      SELECT IFNULL(label,'4') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'产品管理' as name,'crm_product' as types FROM lkcrm_admin_field WHERE label='4' AND cust_id = '" + custId + "' " +
                "      union all\n" +
                "      SELECT IFNULL(label,'5') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'商机管理' as name,'crm_business' as types FROM lkcrm_admin_field WHERE label='5' AND cust_id = '" + custId + "'" +
                "      union all\n" +
                "      SELECT IFNULL(label,'6') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'合同管理' as name,'crm_contract' as types FROM lkcrm_admin_field WHERE label='6' AND cust_id = '" + custId + "'" +
                "      union all\n" +
                "      SELECT IFNULL(label,'7') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'回款管理' as name,'crm_receivables' as types FROM lkcrm_admin_field WHERE label='7' AND cust_id = '" + custId + "'" +
                "      union all\n" +
                "      SELECT IFNULL(label,'11') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'线索公海' as name,'crm_cluePublic' as types FROM lkcrm_admin_field WHERE label='11' AND cust_id = '" + custId + "'";
        return sqlQuery(sql);
    }

    public List<LkCrmAdminFieldStyleEntity> queryFieldStyle(int type, String field, long userId) {
        String sql = "SELECT * FROM lkcrm_admin_field_style WHERE type=? and field_name =? and user_id=? limit 1";
        return queryListBySql(sql, LkCrmAdminFieldStyleEntity.class, type, field, userId);
    }

    public List<Map<String, Object>> queryFieldConfig(int isHide, int label, long userId) {
        String sql = "select id,name from lkcrm_admin_field_sort where is_hide = ? and label = ? and user_id = ? order by sort asc ";
        return sqlQuery(sql, isHide, label, userId);
    }

    public List<Map<String, Object>> queryCustomField(String batchId) {
        String sql = "select a.name,a.value,b.type from lkcrm_admin_fieldv as a left join lkcrm_admin_field as b on a.field_id = b.field_id where batch_id = ?";
        return sqlQuery(sql, batchId);
    }

    public int updateFieldSortName(String name, int field_id) {
        List param = new ArrayList();
        param.add(name);
        param.add(name);
        param.add(field_id);
        String sql = "update lkcrm_admin_field_sort set field_name = ?,name = ? where field_id = ? ";
        return executeUpdateSQL(sql, param.toArray());
    }
    public int updateFieldSortName(String name, String fieldName,int field_id) {
        List param = new ArrayList();
        param.add(fieldName);
        param.add(name);
        param.add(field_id);
        String sql = "update lkcrm_admin_field_sort set field_name = ?,name = ? where field_id = ? ";
        return executeUpdateSQL(sql, param.toArray());
    }

    public List<Map<String, Object>> queryFieldsByBatchId(String batchId, String... name) {
        String sql = "SELECT a.`name` as fieldName,a.`name`,a.type,a.label,a.remark,a.input_tips,a.max_length,a.default_value,a.is_unique as isUnique,a.is_null as isNull,a.sorting,a.`options`,b.`value`,a.operating\n" +
                "      FROM lkcrm_admin_field as a left join lkcrm_admin_fieldv as b on a.field_id = b.field_id WHERE b.batch_id = ？ ";
        if (name.length > 0) {
            sql += " and a.name in (" + SqlAppendUtil.sqlAppendWhereIn(name) + ")";
        }
        sql += " union all SELECT a.`name` AS fieldName,a.`name`,a.type,a.label,a.remark,a.input_tips,a.max_length,a.default_value,a.is_unique AS isUnique,a.is_null AS isNull,a.sorting,a.`options`,b.`value`,a.operating\n" +
                "      FROM lkcrm_admin_field AS a left join lkcrm_admin_fieldv as b on a.field_id = b.field_id\n" +
                "      WHERE a.label = (SELECT a.label FROM lkcrm_admin_field AS a left join lkcrm_admin_fieldv as b on a.field_id = b.field_id WHERE b.batch_id =? limit 1) and a.field_id not in (SELECT field_id FROM lkcrm_admin_fieldv WHERE batch_id =?)";
        if (name.length > 0) {
            sql += " and a.name in (" + SqlAppendUtil.sqlAppendWhereIn(name) + ")";
        }
        sql += " AND a.cust_id = ? ";
        return sqlQuery(sql, batchId, batchId, batchId, BaseUtil.getCustId());
    }

}
