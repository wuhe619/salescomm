package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminFieldEntity;
import com.bdaim.crm.entity.LkCrmAdminFieldStyleEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminFieldDao extends SimpleHibernateDao<LkCrmAdminFieldEntity, Integer> {

    public List customerFieldList(String label) {
        String sql = "select field_id,field_name,name,type,options from lkcrm_admin_field where field_type = 0 and label = ?";
        return sqlQuery(sql, label);
    }

    public int deleteByChooseId(List<Integer> field_ids, int label, int categoryId) {
        List param = new ArrayList();
        param.add(field_ids);
        param.add(label);
        String sql = "DELETE FROM lkcrm_admin_field WHERE field_id not in(? ) and (operating = '0' or operating = '2') and label= ?  ";
        if (10 == label) {
            sql += " and examine_category_id=？";
            param.add(categoryId);
        }
        return executeUpdateSQL(sql, param.toArray());
    }

    public int deleteByFieldValue(List<Integer> field_ids, int label, int categoryId) {
        List param = new ArrayList();
        param.add(field_ids);
        param.add(label);
        String sql = "DELETE FROM lkcrm_admin_fieldv WHERE field_id in( SELECT field_id FROM lkcrm_admin_field WHERE field_id not in(?) and (operating = '0' or operating = '2') and label=? ";
        if (10 == label) {
            sql += " and examine_category_id=？";
            param.add(categoryId);
        }
        return executeUpdateSQL(sql, param.toArray());
    }

    public int deleteFieldSort(List<String> names, int label) {
        List param = new ArrayList();
        param.add(names);
        param.add(label);
        String sql = " delete from lkcrm_admin_field_sort where label = ? and name in(?)";
        return executeUpdateSQL(sql, param.toArray());
    }

    public List queryFields() {
        String sql = "SELECT IFNULL(label,'1') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'线索管理' as name,'crm_leads' as types FROM lkcrm_admin_field WHERE label='1'\n" +
                "      union all\n" +
                "      SELECT IFNULL(label,'2') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'客户管理' as name,'crm_customer' as types FROM lkcrm_admin_field WHERE label='2'\n" +
                "      union all\n" +
                "      SELECT IFNULL(label,'3') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'联系人管理' as name,'crm_contacts' as types FROM lkcrm_admin_field WHERE label='3'\n" +
                "      union all\n" +
                "      SELECT IFNULL(label,'4') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'产品管理' as name,'crm_product' as types FROM lkcrm_admin_field WHERE label='4'\n" +
                "      union all\n" +
                "      SELECT IFNULL(label,'5') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'商机管理' as name,'crm_business' as types FROM lkcrm_admin_field WHERE label='5'\n" +
                "      union all\n" +
                "      SELECT IFNULL(label,'6') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'合同管理' as name,'crm_contract' as types FROM lkcrm_admin_field WHERE label='6'\n" +
                "      union all\n" +
                "      SELECT IFNULL(label,'7') as label,IFNULL(MAX(update_time),'2000-01-01 00:00:00') as update_time,'回款管理' as name,'crm_receivables' as types FROM lkcrm_admin_field WHERE label='7'";
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
        String sql = "select a.name,a.value,b.type from 72crm_admin_fieldv as a left join 72crm_admin_field as b on a.field_id = b.field_id where batch_id = ?";
        return sqlQuery(sql, batchId);
    }
}
