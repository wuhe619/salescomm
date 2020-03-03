package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.crm.entity.LkCrmAdminExamineLogEntity;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmAdminExamineLogDao extends SimpleHibernateDao<LkCrmAdminExamineLogEntity, Integer> {
    public int updateExamineLogIsRecheckByRecordId(Integer record_id) {
        return super.executeUpdateSQL(" UPDATE lkcrm_admin_examine_log SET is_recheck = 1 WHERE record_id = ?", record_id);
    }

    public List<Map<String, Object>> queryUserByUserId(Long userId) {
        String sql = " SELECT DISTINCT saud.user_id, saud.realname , 0 as examine_status from lkcrm_admin_user as sau\n" +
                "    LEFT JOIN lkcrm_admin_user as saud on saud.user_id = sau.parent_id WHERE sau.user_id = ?";
        return sqlQuery(sql, userId);
    }

    public List<Map<String, Object>> queryUserByRecordId(Integer record_id) {
        String sql = " select sael.examine_time  as examineTime , sael.examine_status,sau.realname ,sau.user_id,sau.img from lkcrm_admin_examine_log as sael\n" +
                "    LEFT JOIN lkcrm_admin_user as sau on sau.user_id = sael.examine_user where sael.record_id = ? and  sael.examine_status = 4 and sael.is_recheck != 1";
        return sqlQuery(sql, record_id);
    }

    public Map<String, Object> queryRecordAndId(Integer record_id) {
        String sql = "  select create_user, sau.realname as examineUserName , sau.user_id , sau.img, aser.create_time as examineTime, 5 as examine_status from lkcrm_admin_examine_record  as aser\n" +
                "    LEFT JOIN lkcrm_admin_user as sau on sau.user_id = aser.create_user where record_id = ?";
        List<Map<String, Object>> maps = sqlQuery(sql, record_id);
        return maps.size() > 0 ? maps.get(0) : null;
    }

    public Map<String, Object> queryRecordByUserIdAndStatus(Date examineTime, Long user_id) {
        String sql = "  SELECT DISTINCT user_id, realname  ,'' AS img, 5 as examine_status, 5 as stepType, ? as examineTime from lkcrm_admin_user WHERE user_id =?";
        List<Map<String, Object>> maps = sqlQuery(sql, examineTime, user_id);
        return maps.size() > 0 ? maps.get(0) : null;
    }

    public List<Map<String, Object>> queryExamineLogAndUserByRecordId(Integer record_id) {
        String sql = " select sael.examine_time  as examineTime , sael.examine_status,sau.realname ,sau.img , sael.log_id from lkcrm_admin_examine_log as sael\n" +
                "    LEFT JOIN lkcrm_admin_user as sau on sau.user_id = sael.examine_user where sael.record_id = ? and sael.is_recheck != 1 order by sael.create_time";
        return sqlQuery(sql, record_id);
    }

    public Map<String, Object> queryExamineLogAndUserByLogId(int log_id) {
        String sql = " select sael.examine_time  as examineTime , sael.examine_status,sau.realname ,sau.img , sael.log_id from lkcrm_admin_examine_log as sael\n" +
                "    LEFT JOIN lkcrm_admin_user as sau on sau.user_id = sael.examine_user where sael.log_id = ? and sael.is_recheck != 1 order by sael.create_time";
        List<Map<String, Object>> maps = sqlQuery(sql, log_id);
        return maps.size() > 0 ? maps.get(0) : null;
    }

    public Map<String, Object> queryExamineLog(int recordId, long auditUserId, String stepId) {
        String sql = "  SELECT * FROM lkcrm_admin_examine_log\n" +
                "      WHERE examine_status = 0 and is_recheck != 1\n" +
                "      and record_id = ?" +
                "      and examine_user = ?";
        List param = new ArrayList();
        param.add(recordId);
        param.add(auditUserId);
        if (StringUtil.isNotEmpty(stepId)) {
            sql += "and examine_step_id = ?";
            param.add(stepId);
        }

        List<Map<String, Object>> maps = sqlQuery(sql, param.toArray());
        return maps.size() > 0 ? maps.get(0) : null;
    }

    public List<Map<String, Object>> queryUserByRecordIdAndStepIdAndStatus(Integer record_id, int step_id) {
        String sql = "  select sael.examine_time  as examineTime , sael.examine_status,sau.realname as realname , sau.user_id , sau.img from lkcrm_admin_examine_log as sael\n" +
                "    LEFT JOIN lkcrm_admin_user as sau on sau.user_id = sael.examine_user where record_id = ? and examine_step_id = ? and sael.is_recheck != 1  order by sael.create_time";
        return sqlQuery(sql, record_id, step_id);
    }

    public List<Map<String, Object>> queryUserByUserIdAnd(Long user_id) {
        String sql = "SELECT DISTINCT user_id, realname ,'' AS img, 0 as examine_status from lkcrm_admin_user WHERE user_id = ?";
        List<Map<String, Object>> maps = sqlQuery(sql, user_id);
        return maps;
    }

    public Map<String, Object> queryUserByUserIdAndStatus(Long user_id) {
        String sql = "  SELECT DISTINCT user_id, realname  , 0 as examine_status , img from lkcrm_admin_user WHERE user_id = ?";
        List<Map<String, Object>> maps = sqlQuery(sql, user_id);
        return maps.size() > 0 ? maps.get(0) : null;
    }

    public List<Map<String, Object>> queryExamineLogByRecordIdByStep(Integer recordId) {
        String sql = "   select sael.order_id as order_id , sau.user_id , sau.realname , sau.img ,sael.examine_status,sael.examine_time,sael.remarks from lkcrm_admin_examine_log as sael\n" +
                "LEFT JOIN lkcrm_admin_user as sau on sau.user_id = sael.examine_user where sael.record_id = ? AND sael.examine_status != 0 order by sael.create_time";
        List<Map<String, Object>> maps = sqlQuery(sql, recordId);
        return maps;
    }

    public List<Map<String, Object>> queryExamineLogByRecordIdByStep1(Integer recordId) {
        String sql = " select sael.order_id as order_id, sau.user_id , sau.realname , sau.img,sael.examine_status,sael.examine_time,sael.remarks from lkcrm_admin_examine_log as sael\n" +
                " LEFT JOIN lkcrm_admin_user as sau on sau.user_id = sael.examine_user where sael.record_id = ? AND sael.examine_status != 0 order by sael.create_time";
        List<Map<String, Object>> maps = sqlQuery(sql, recordId);
        return maps;
    }

    public LkCrmAdminExamineLogEntity queryNowadayExamineLogByRecordIdAndStepId(Integer record_id, Long examineStepId, Long auditUserId) {
        String sql = " select * from 72crm_admin_examine_log where record_id = ? and examine_step_id = ? and examine_user = ? and is_recheck = 0";
        List<LkCrmAdminExamineLogEntity> objects = queryListBySql(sql, LkCrmAdminExamineLogEntity.class, record_id, examineStepId, auditUserId);
        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

    public LkCrmAdminExamineLogEntity queryNowadayExamineLogByRecordIdAndStatus(Integer record_id, Long auditUserId) {
        String sql = "  select * from 72crm_admin_examine_log where record_id = ? and examine_status = 0 and examine_user = ? and is_recheck = 0";
        List<LkCrmAdminExamineLogEntity> objects = queryListBySql(sql, LkCrmAdminExamineLogEntity.class, record_id, auditUserId);
        if (objects.size() > 0) {
            return objects.get(0);
        }
        return null;
    }

    public Map<String, Object> queryCountByStepId(Integer record_id, long stepId) {
        String sql = " SELECT  DISTINCT ((SELECT COUNT(log_id) FROM 72crm_admin_examine_log WHERE record_id =? and examine_step_id = ?)- (SELECT COUNT(log_id) FROM 72crm_admin_examine_log WHERE record_id = ? \n" +
                "    and examine_step_id = ? and examine_status = 2 )) as toCount FROM 72crm_admin_examine_log";
        List<Map<String, Object>> maps = sqlQuery(sql, record_id, stepId, record_id, stepId);
        return maps.size() > 0 ? maps.get(0) : null;
    }
}
