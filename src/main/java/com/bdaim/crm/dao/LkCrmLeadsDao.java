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
                "    where leads_id in (?) ";
        int maps = this.executeUpdateSQL(sql, ownerUserId, SqlAppendUtil.sqlAppendWhereIn(ids));
        return maps;
    }

    public List queryBatchIdByIds(List<String> ids) {
        String sql = "select batch_id from lkcrm_crm_leads where leads_id in (?)";
        return this.sqlQuery(sql, SqlAppendUtil.sqlAppendWhereIn(ids));
    }

    public int deleteByIds(List<String> ids) {
        String sql = "delete from lkcrm_crm_leads where leads_id in (?)";
        int maps = this.executeUpdateSQL(sql, SqlAppendUtil.sqlAppendWhereIn(ids));
        return maps;
    }

    public int setLeadsFollowup(List<String> ids) {
        String sql = "update lkcrm_crm_leads set followup = 1 where leads_id in (?)";
        int maps = this.executeUpdateSQL(sql, SqlAppendUtil.sqlAppendWhereIn(ids));
        return maps;
    }

    public Page pageCluePublicSea(int pageNum, int pageSize, long seaId, String search) {
        StringBuffer conditions = new StringBuffer("SELECT custG.id, custG.user_id, custG.status, custG.call_count callCount, DATE_FORMAT(custG.last_call_time,'%Y-%m-%d %H:%i:%s') lastCallTime, custG.intent_level intentLevel,");
        conditions.append(" custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street, custG.super_data, ");
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

    public List<Map<String, Object>> getPublicSeaClue(long seaId, String id) {
        StringBuffer conditions = new StringBuffer("SELECT a.*,z.* FROM t_customer_sea_list_" + seaId + " AS a LEFT JOIN fieldleadsview AS z ON a.id = z.field_batch_id WHERE 1=1 ");
        conditions.append(" AND id = ? ");
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
}
