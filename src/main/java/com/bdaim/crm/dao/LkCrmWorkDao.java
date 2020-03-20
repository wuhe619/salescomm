package com.bdaim.crm.dao;

import com.alibaba.fastjson.JSONArray;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmWorkEntity;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.plugin.activerecord.Record;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmWorkDao extends SimpleHibernateDao<LkCrmWorkEntity, Integer> {
    public List<Map<String, Object>> queryTrashList() {
        String sql = "SELECT " +
                " a.task_id,a.name,a.stop_time,a.priority,a.status, " +
                " ( SELECT count( * ) FROM lkcrm_admin_file WHERE batch_id = a.batch_id ) AS file_num, " +
                " ( SELECT count( * ) FROM lkcrm_task_comment WHERE type_id = a.task_id AND type = 1 ) AS comment_num, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id ) AS child_all_count, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id AND STATUS = 5 ) AS child_finish_count  " +
                "FROM " +
                " lkcrm_task AS a  " +
                "WHERE " +
                " pid = 0  " +
                " AND ishidden = 1  " +
                "ORDER BY " +
                " a.hidden_time DESC";
        return super.queryMapsListBySql(sql);
    }

    public List<Map<String, Object>> queryTrashListByUserId(Long userId) {
        String sql = "SELECT " +
                " a.task_id,a.name,a.stop_time,a.priority,a.status, " +
                " ( SELECT count( * ) FROM lkcrm_admin_file WHERE batch_id = a.batch_id ) AS file_num, " +
                " ( SELECT count( * ) FROM lkcrm_task_comment WHERE type_id = a.task_id AND type = 1 ) AS comment_num, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id ) AS child_all_count, " +
                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id AND STATUS = 5 ) AS child_finish_count  " +
                "FROM " +
                " lkcrm_task AS a  " +
                "WHERE " +
                " pid = 0  " +
                " AND ishidden = 1  " +
                " AND ( a.main_user_id =? OR a.owner_user_id LIKE concat( '%,',?, ',%' ) )  " +
                "ORDER BY " +
                " a.hidden_time DESC";
        return super.queryMapsListBySql(sql, userId, userId);
    }

    public Map<String, Object> workStatistics(Map<String, Object> params) {
        String sql = "SELECT " +
                " count( * ) AS allCount, " +
                " count( STATUS = 1 OR NULL ) AS unfinished, " +
                " count( STATUS = 2 OR NULL ) AS overdue, " +
                " count( STATUS = 5 OR NULL ) AS complete, " +
                " count( is_archive = 1 OR NULL ) AS archive, " +
                " ifnull( ROUND( ( count( STATUS = 5 OR NULL ) / count( * ) ) * 100, 2 ), 0 ) AS completionRate, " +
                " ifnull( ROUND( ( count( STATUS = 2 OR NULL ) / count( * ) ) * 100, 2 ), 0 ) AS overdueRate  " +
                "FROM " +
                " lkcrm_task  " +
                "WHERE " +
                " 1 = 1  " +
                " AND ishidden = 0  " +
                " AND work_id >0 ";
        List paramList = new ArrayList<>();
        if (params != null) {
            if (params.containsKey("workId")) {
                sql += " and work_id = ? ";
                paramList.add(params.get("workId"));
            }
            if (params.containsKey("workIds")) {
                sql += " and work_id in (?) ";
                paramList.add(params.get("workIds"));
            }
            if (params.containsKey("mainUserId")) {
                sql += " and main_user_id = ? ";
                paramList.add(params.get("mainUserId"));
            }
            if (params.containsKey("userId")) {
                sql += " and (main_user_id = ? or owner_user_id like concat('%,',?,',%'))  and " +
                        "(is_archive = 0 or (is_archive = 1 and status = 5)) ";
                paramList.add(params.get("userId"));
                paramList.add(params.get("userId"));
            }
        }
        return super.queryUniqueSql(sql, paramList.toArray());
    }


//    public Map<String, Object> workStatistics(Integer mainUserId, String workIds) {
//        String sql = "    select count(*) as allCount, " +
//                "      count(status=1 or null) as unfinished, " +
//                "      count(status=2 or null) as overdue, " +
//                "      count(status=5 or null) as complete, " +
//                "      count(is_archive=1 or null) as archive, " +
//                "      ifnull(ROUND((count(status=5 or null)/count(*))*100,2),0) as completionRate, " +
//                "      ifnull(ROUND((count(status=2 or null)/count(*))*100,2),0) as overdueRate " +
//                "      from lkcrm_task where 1 = 1 and ishidden = 0 and work_id >0 " +
//                "   and main_user_id = ? " +
//                " and work_id in (?)";
//        return super.queryUniqueSql(sql, mainUserId, workIds);
//    }
//
//    public Map<String, Object> workStaByWorkIdAndUserId(String workId, String userId) {
//        String sql = "SELECT " +
//                " count( * ) AS allCount,count( STATUS = 1 OR NULL ) AS unfinished, " +
//                " count( STATUS = 2 OR NULL ) AS overdue,count( STATUS = 5 OR NULL ) AS complete, " +
//                " count( is_archive = 1 OR NULL ) AS archive, " +
//                " ifnull( ROUND( ( count( STATUS = 5 OR NULL ) / count( * ) ) * 100, 2 ), 0 ) AS completionRate, " +
//                " ifnull( ROUND( ( count( STATUS = 2 OR NULL ) / count( * ) ) * 100, 2 ), 0 ) AS overdueRate  " +
//                "FROM " +
//                " lkcrm_task  " +
//                "WHERE " +
//                " 1 = 1  " +
//                " AND ishidden = 0  " +
//                " AND work_id > 0  " +
//                " AND work_id = ?  " +
//                " AND ( main_user_id = ? OR owner_user_id LIKE concat( '%,',?, ',%' ) )  " +
//                " AND ( is_archive = 0 OR ( is_archive = 1 AND STATUS = 5 ) )";
//        return super.queryUniqueSql(sql, workId, userId, userId);
//    }

    public List<Map<String, Object>> queryWorkMenuByRoleId(Integer roleId) {
        String sql = "    SELECT  c.realm,c.menu_id,c.parent_id from " +
                "      lkcrm_admin_role_menu as b " +
                "        LEFT JOIN lkcrm_admin_menu as c on b.menu_id=c.menu_id " +
                "    WHERE b.role_id = ?";
        return super.queryListBySql(sql, roleId);
    }

    public List<Map<String, Object>> queryOwnerRoleList(Integer workId) {
        String sql = " select a.user_id,b.realname,a.role_id,c.role_name,b.img  " +
                "  from lkcrm_work_user as a left join lkcrm_admin_user as b on a.user_id = b.user_id  " +
                "  left join lkcrm_admin_role as c on a.role_id = c.role_id  " +
                "  where a.work_id = ?";
        return super.queryListBySql(sql, workId);
    }

    public List<Map<String, Object>> archList(String workId) {
        String sql = "  select a.*,b.name as workName,(select count(*) from lkcrm_task_comment where type_id = a.task_id and type = 1) as commentCount, " +
                "         (select count(*) from lkcrm_task where pid = a.task_id and status = 5) as childWCCount, " +
                "         (select count(*) from lkcrm_task where pid = a.task_id) as childAllCount, " +
                "         (select count(*) from lkcrm_admin_file where batch_id = a.batch_id) as fileCount " +
                "  from lkcrm_task a left join lkcrm_work b on a.work_id = b.work_id where a.work_id = ? and a.is_archive = 1 and a.ishidden = 0";
        return super.queryListBySql(sql, workId);
    }

    public List<Map<String, Object>> queryTaskByWorkId(Integer workId, Integer stopTimeType, JSONArray userIds,
                                                       JSONArray labelIds, Integer classId) {
//        List params = new ArrayList();
//        String sql = "SELECT " +
//                " a.*, " +
//                " ifnull( b.NAME, '未分组' ) AS className, " +
//                " ifnull( b.class_id, 0 ) AS classId, " +
//                " b.order_num AS classOrder, " +
//                " ( SELECT count( * ) FROM lkcrm_task_comment WHERE type_id = a.task_id AND type = 1 ) AS commentCount, " +
//                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id AND STATUS = 5 ) AS childWCCount, " +
//                " ( SELECT count( * ) FROM lkcrm_task WHERE pid = a.task_id ) AS childAllCount, " +
//                " ( SELECT count( * ) FROM lkcrm_admin_file WHERE batch_id = a.batch_id ) AS fileCount  " +
//                "FROM " +
//                " lkcrm_task a " +
//                " LEFT JOIN lkcrm_work_task_class b ON a.class_id = b.class_id  " +
//                "WHERE " +
//                " a.work_id = ? " +
//                "  " +
//                " AND a.STATUS != 3  " +
//                " AND a.ishidden = 0  " +
//                " AND is_archive = 0 ";
//        params.add(workId);
//        if (classId == -1) {
//            sql += " and a.class_id is null ";
//        } else {
//            sql += " and a.class_id = ? ";
//            params.add(classId);
//        }
//        if(!CollectionUtils.isEmpty(userIds)){
//            sql +=" and ( ";
//            for(int i=0;i<userIds.size();i++){
//                sql+="a.owner_user_id like concat('%,',?,',%') or  a.main_user_id = ? "
//
//            }
//            sql+="  ) ";
//        }
        //TODO
        return null;
    }

    public Page queryTaskFileByWorkId(int page, int limit, Integer workId) {
        String sql = "SELECT " +
                " a.file_id,a.name,CONCAT( FLOOR( a.size / 1000 ), 'KB' ) AS size, " +
                " a.create_user_id,b.realname AS create_user_name, " +
                " a.create_time,a.file_path,a.file_type,a.batch_id  " +
                "FROM " +
                " `lkcrm_admin_file` AS a " +
                " INNER JOIN `lkcrm_admin_user` AS b ON a.create_user_id = b.user_id  " +
                "WHERE " +
                " a.batch_id IN ( " +
                "SELECT " +
                " batch_id  " +
                "FROM " +
                " `lkcrm_task`  " +
                "WHERE " +
                " work_id = ?";
        return super.sqlPageQuery(sql, page, limit, workId);
    }

    public List<Record> queryOwnerWorkIdList(Long userId1) {
        String sql = "SELECT a.work_id FROM lkcrm_work a WHERE 1 = 1  " +
                "AND ( owner_user_id LIKE concat( '%,',?, ',%' ) AND is_open = 0 )  " +
                " OR is_open = 1";
        return JavaBeanUtil.mapToRecords(super.queryListBySql(sql, userId1));
    }
}
