package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmLeadsEntity;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmLeadsDao extends SimpleHibernateDao<LkCrmLeadsEntity, Integer> {

    public static final Logger LOG = LoggerFactory.getLogger(LkCrmLeadsDao.class);

    public List getRecord(String leadsId) {
        String sql = "select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id " +
                "    from lkcrm_admin_record as a inner join lkcrm_admin_user as b " +
                "    where a.create_user_id = b.user_id and types = 'crm_leads' and types_id = ? order by a.create_time desc";
        return this.sqlQuery(sql, leadsId);
    }

    public Map queryById(int leadsId) {
        String sql = "select *,leads_name as name from leadsview where leads_id = ? ";
        List<Map<String, Object>> maps = this.sqlQuery(sql, leadsId);
        if (maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }

    public int updateOwnerUserId(String ownerUserId, List<String> ids) {
        String sql = "update lkcrm_crm_leads " +
                "    set owner_user_id = ?,followup = 0 " +
                "    where leads_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ") ";
        int maps = this.executeUpdateSQL(sql, ownerUserId);
        return maps;
    }

    public List queryBatchIdByIds(List<String> ids) {
        String sql = "select batch_id from lkcrm_crm_leads where leads_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ")";
        return this.sqlQuery(sql);
    }

    public int deleteByIds(List<String> ids) {
        String sql = "delete from lkcrm_crm_leads where leads_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ")";
        int maps = this.executeUpdateSQL(sql);
        return maps;
    }

    public int setLeadsFollowup(List<String> ids) {
        String sql = "update lkcrm_crm_leads set followup = 1 where leads_id in (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ")";
        int maps = this.executeUpdateSQL(sql);
        return maps;
    }

    public Page pageCluePublicSea(int pageNum, int pageSize, long seaId, String search) {
        StringBuffer conditions = new StringBuffer("SELECT custG.id, custG.user_id, custG.status, custG.call_count callCount, DATE_FORMAT(custG.last_call_time,'%Y-%m-%d %H:%i:%s') lastCallTime, custG.intent_level intentLevel,");
        conditions.append(" custG.super_name leads_name , custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street, custG.super_data, ");
        conditions.append(" custG.batch_id, custG.last_call_status, custG.data_source, DATE_FORMAT(custG.user_get_time,'%Y-%m-%d %H:%i:%s') user_get_time, DATE_FORMAT(custG.create_time,'%Y-%m-%d %H:%i:%s') create_time, custG.pre_user_id, custG.last_called_duration, DATE_FORMAT(custG.last_mark_time,'%Y-%m-%d %H:%i:%s') last_mark_time, ");
        conditions.append(" custG.call_success_count, custG.call_fail_count, custG.sms_success_count ,custG.super_data ->> '$.SYS014 ' as custType, ");
        conditions.append(" z.* FROM t_customer_sea_list_" + seaId + " AS custG LEFT JOIN fieldleadsview AS z ON custG.id = z.field_batch_id WHERE custG.status = 1 ");
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(search)) {
            param.add(search);
            param.add(search);
            param.add(search);
            param.add(search);
            conditions.append(" and (super_name like '%?%' or super_telphone like '%?%' or super_phone like '%?%' or super_data like '%?%')");
        }
        //conditions.append(" GROUP By custType ORDER BY custG.create_time DESC ");
        LOG.info("公海sql:" + conditions);
        return sqlPageQuery(conditions.toString(), pageNum, pageSize, param.toArray());
    }

    public List<Map<String, Object>> listCluePublicSea(long seaId, String search) {
        StringBuffer conditions = new StringBuffer("SELECT custG.id, custG.user_id, custG.status, custG.call_count callCount, DATE_FORMAT(custG.last_call_time,'%Y-%m-%d %H:%i:%s') lastCallTime, custG.intent_level intentLevel,");
        conditions.append(" custG.super_name leads_name , custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street, custG.super_data, ");
        conditions.append(" custG.batch_id, custG.last_call_status, custG.data_source, DATE_FORMAT(custG.user_get_time,'%Y-%m-%d %H:%i:%s') user_get_time, DATE_FORMAT(custG.create_time,'%Y-%m-%d %H:%i:%s') create_time, custG.pre_user_id, custG.last_called_duration, DATE_FORMAT(custG.last_mark_time,'%Y-%m-%d %H:%i:%s') last_mark_time, ");
        conditions.append(" custG.call_success_count, custG.call_fail_count, custG.sms_success_count ,custG.super_data ->> '$.SYS014 ' as custType, ");
        conditions.append(" z.* FROM t_customer_sea_list_" + seaId + " AS custG LEFT JOIN fieldleadsview AS z ON custG.id = z.field_batch_id WHERE custG.status = 1 ");
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(search)) {
            param.add(search);
            param.add(search);
            param.add(search);
            param.add(search);
            conditions.append(" and (super_name like '%?%' or super_telphone like '%?%' or super_phone like '%?%' or super_data like '%?%')");
        }
        //conditions.append(" GROUP By custType ORDER BY custG.create_time DESC ");
        LOG.info("公海sql:" + conditions);
        return sqlQuery(conditions.toString(), param.toArray());

    }

    public List<Map<String, Object>> getPublicSeaClue(long seaId, String id) {
        String fieldSql = "SELECT max( IF ( (`a`.`name` = '线索来源'), `a`.`value`, NULL ) ) AS `线索来源`, max( IF ( (`a`.`name` = '客户行业'), `a`.`value`, NULL ) ) AS `客户行业`, max( IF ( (`a`.`name` = '客户级别'), `a`.`value`, NULL ) ) AS `客户级别`, `a`.`batch_id` AS `field_batch_id` FROM ( `lkcrm_admin_fieldv` `a` JOIN `lkcrm_admin_field` `d` ON ( ( `a`.`field_id` = `d`.`field_id` ) ) ) WHERE ( (`d`.`label` = 11) AND (`a`.`batch_id` IS NOT NULL) AND (`a`.`batch_id` <> '') AND (`d`.`field_type` = 0) ) GROUP BY `a`.`batch_id` ";
        StringBuffer conditions = new StringBuffer("SELECT a.*,z.* FROM t_customer_sea_list_" + seaId + " AS a LEFT JOIN (" + fieldSql + ") AS z ON a.id = z.field_batch_id WHERE id = ? ");
        List param = new ArrayList();
        param.add(id);
        return sqlQuery(conditions.toString(), param.toArray());
    }

    public Page pageLeadsList(int pageNum, int pageSize, String leadsName, String telephone, String mobile) {
        StringBuffer conditions = new StringBuffer("select leads_id,leads_name,owner_user_name from leadsview where 1=1 ");
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(leadsName)) {
            param.add(leadsName);
            conditions.append("and leads_name like CONCAT('%',?,'%')");
        }
        if (StringUtil.isNotEmpty(telephone)) {
            param.add(telephone);
            conditions.append(" and telephone = ? ");
        }
        if (StringUtil.isNotEmpty(mobile)) {
            param.add(mobile);
            conditions.append("and mobile = ?");
        }
        return sqlPageQuery(conditions.toString(), pageNum, pageSize, param.toArray());
    }

    public Map<String, Object> queryByName(String leads_name) {
        StringBuffer conditions = new StringBuffer("select * from leadsview where leads_name = ?");
        List param = new ArrayList();
        param.add(leads_name);
        List<Map<String, Object>> maps = this.sqlQuery(conditions.toString(), param.toArray());
        if (maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }

    public List<Map<String, Object>> excelExport(List leadsIds) {
        StringBuffer conditions = new StringBuffer("select leads_name,线索来源,客户行业,客户级别,next_time,telephone,mobile,address,remark,create_user_name,owner_user_name,create_time,update_time from leadsview where leads_id in (" + SqlAppendUtil.sqlAppendWhereIn(leadsIds) + ") order by update_time desc");
        return sqlQuery(conditions.toString());
    }


    public List<Map<String, Object>> excelPublicSeaExport(long seaId, List ids) {
        String fieldSql = "SELECT max( IF ( (`a`.`name` = '线索来源'), `a`.`value`, NULL ) ) AS `线索来源`, max( IF ( (`a`.`name` = '客户行业'), `a`.`value`, NULL ) ) AS `客户行业`, max( IF ( (`a`.`name` = '客户级别'), `a`.`value`, NULL ) ) AS `客户级别`, `a`.`batch_id` AS `field_batch_id` FROM ( `lkcrm_admin_fieldv` `a` JOIN `lkcrm_admin_field` `d` ON ( ( `a`.`field_id` = `d`.`field_id` ) ) ) WHERE ( (`d`.`label` = 11) AND (`a`.`batch_id` IS NOT NULL) AND (`a`.`batch_id` <> '') AND (`d`.`field_type` = 0) ) GROUP BY `a`.`batch_id` ";
        StringBuffer conditions = new StringBuffer("SELECT a.*,z.* FROM t_customer_sea_list_" + seaId + " AS a LEFT JOIN (" + fieldSql + ") AS z ON a.id = z.field_batch_id WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(ids) + ") ");
        return sqlQuery(conditions.toString());
    }

}
