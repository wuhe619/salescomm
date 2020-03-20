package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmOaExamineEntity;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmOaExamineDao extends SimpleHibernateDao<LkCrmOaExamineEntity, Integer> {

    public List myInitiate(Long userId, Integer categoryId, Integer status, Date startTime, Date endTime) {
        String sql = "select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id ,c.category_id,c.title as categoryTitle " +
                "    from lkcrm_oa_examine a left join lkcrm_oa_examine_record b on a.examine_id = b.examine_id left join lkcrm_oa_examine_category c on a.category_id = c.category_id " +
                "    where a.create_user_id = ? ";
        List param = new ArrayList();
        param.add(userId);
        if (categoryId != null) {
            sql += " and a.category_id =?";
            param.add(categoryId);
        }
        if (status != null) {
            sql += "   and  b.examine_status = ? ";
            param.add(status);
        }
        if (startTime != null && endTime != null) {
            sql += "  and a.create_time between ? and  ? ";
            param.add(startTime);
            param.add(endTime);
        }
        sql += "group by a.examine_id,b.record_id order by  a.create_time desc ";
        return super.sqlQuery(sql, param.toArray());
    }

    public Page pageMyInitiate(int pageNum, int pageSize, Long userId, Integer categoryId, Integer status, Date startTime, Date endTime) {
        String sql = "select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id ,c.title as categoryTitle " +
                "    from lkcrm_oa_examine a left join lkcrm_oa_examine_record b on a.examine_id = b.examine_id left join lkcrm_oa_examine_category c on a.category_id = c.category_id " +
                "    where a.create_user_id = ? ";
        List param = new ArrayList();
        param.add(userId);
        if (categoryId != null) {
            sql += " and a.category_id =?";
            param.add(categoryId);
        }
        if (status != null) {
            sql += "   and  b.examine_status = ? ";
            param.add(status);
        }
        if (startTime != null && endTime != null) {
            sql += "  and a.create_time between ? and  ? ";
            param.add(startTime);
            param.add(endTime);
        }
        sql += "group by a.examine_id,b.record_id order by  a.create_time desc ";
        return super.sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public Page queryExamineRelation(int pageNum, int pageSize, String businessIds, String contactsIds, String contractIds, String customerIds) {
        String sql = "select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id ,c.title as categoryTitle from lkcrm_oa_examine_relation h " +
                "left join lkcrm_oa_examine a on h.examine_id = a.examine_id " +
                "left join lkcrm_oa_examine_record b on a.examine_id = b.examine_id " +
                " left join lkcrm_oa_examine_category c on a.category_id = c.category_id where 1 = 2";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(businessIds)) {
            param.add(businessIds);
            sql += " or h.business_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contactsIds)) {
            param.add(contactsIds);
            sql += " or h.contacts_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contractIds)) {
            param.add(contractIds);
            sql += " or h.contract_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(customerIds)) {
            param.add(customerIds);
            sql += " or h.customer_ids like concat('%,',?,',%')";
        }
        return super.sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

    public List<Map<String, Object>> queryExamineLogByRecordIdByStep(Integer recordId) {
        String sql = "select sael.order_id,ases.step_num as order_id , sau.user_id , sau.realname , sau.img ,sael.examine_status,sael.examine_time,sael.remarks " +
                "    from lkcrm_oa_examine_log as sael LEFT JOIN lkcrm_admin_user as sau on sau.user_id = sael.examine_user LEFT JOIN lkcrm_oa_examine_step as ases on ases.step_id = sael.examine_step_id " +
                "    where sael.record_id = ? AND sael.examine_status != 0 order by sael.create_time";
        return super.sqlQuery(sql, recordId);
    }

    public List<Map<String, Object>> queryExamineLogByRecordIdByStep1(Integer recordId) {
        String sql = "   select  sael.order_id,sau.user_id , sau.realname , sau.img,sael.examine_status,sael.examine_time,sael.remarks,sael.is_recheck " +
                "    from lkcrm_oa_examine_log as sael  LEFT JOIN lkcrm_admin_user as sau on sau.user_id = sael.examine_user" +
                "    where sael.record_id = ? AND sael.examine_status != 0 order by sael.create_time";
        return super.queryListBySql(sql);
    }

    public List<Map<String, Object>> queryRecordByUserIdAndStatus(Integer create_user, Date examineTime) {
        String sql = "    SELECT DISTINCT user_id, realname  ,img, 5 as examine_status, ? as examineTime from lkcrm_admin_user " +
                "    WHERE user_id = ?";
        return super.sqlQuery(sql, examineTime, create_user);
    }

    public Page myOaExamine(Integer page, Integer limit, Long userId, Integer categoryId,
                            Integer status, Date startTime, Date endTime) {
        String sql = "select a.*,b.examine_status,b.record_id as examine_record_id,b.examine_step_id," +
                "c.title as categoryTitle  from lkcrm_oa_examine a  left join  lkcrm_oa_examine_record b on " +
                "a.examine_id = b.examine_id left join lkcrm_oa_examine_category c on a.category_id = c.category_id " +
                "left join lkcrm_oa_examine_log d on d.record_id = b.record_id " +
                "    where 1 = 1 ";
        List<Object> params = new ArrayList<>();
        if (categoryId != null) {
            sql += " and a.category_id =? ";
            params.add(categoryId);
        }
        if (status != null) {
            if (status == 1) {
                sql += " and (d.examine_user = ? and d.examine_status = 0 and ifnull(b.examine_step_id,1) = " +
                        "ifnull(d.examine_step_id,1) and d.is_recheck !=1) ";
                params.add(userId);
            }
            if (status == 2) {
                sql += " and (d.examine_user = ? and d.examine_status != 0) ";
                params.add(userId);
            }
        }
        if (startTime != null && endTime != null) {
            sql += " and a.create_time between ? and  ? ";
            params.add(startTime);
            params.add(endTime);
        }
        sql += " group by a.examine_id,b.examine_status,b.record_id ";
        if (status == 1) {
            sql += " order by  a.create_time desc ";
        }
        if (status == 2) {
            sql += " order by  d.examine_time desc ";
        }
        return sqlPageQuery(sql, page, limit, params.toArray());
    }
}
