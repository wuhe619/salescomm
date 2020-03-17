package com.bdaim.crm.erp.oa.service;

import com.bdaim.crm.dao.LkCrmTaskCommentDao;
import com.bdaim.crm.entity.LkCrmTaskCommentEntity;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.bdaim.crm.erp.work.entity.TaskComment;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Transactional
public class OaCommentService {
    @Autowired
    private LkCrmTaskCommentDao commentDao;

    public R setComment(LkCrmTaskCommentEntity comment) {
        boolean bol = true;
        if (comment.getCommentId() == null) {
            comment.setCreateTime(new Timestamp(System.currentTimeMillis()));
            comment.setUserId(BaseUtil.getUser().getUserId());
//            bol = comment.save();
            commentDao.save(comment);
        } else {
//            bol = comment.update ();
            commentDao.update(comment);
        }
        if (comment.getMainId() == null) {
            comment.setMainId(0);
        }
        return bol ? R.ok().put("data", comment) : R.error();
    }

    @Before(Tx.class)
    public R deleteComment(Integer commentId) {
        String delSql = "delete from `lkcrm_task_comment` where main_id = ?";
        commentDao.executeUpdateSQL(delSql, commentId);
        return new TaskComment().dao().deleteById(commentId) ? R.ok() : R.error();
    }

    public List<Record> queryCommentList(String typeId, String type) {
        String sql = "select a.comment_id,a.content,a.user_id,a.create_time,a.type_id,a.type,a.favour,a.pid,a.main_id" +
                " from lkcrm_task_comment a  where a.type_id = ? and a.type = ?";
        List<Record> recordList = JavaBeanUtil.mapToRecords(commentDao.sqlQuery(sql, typeId, type));
        if (recordList == null || recordList.size() == 0) {
            return new ArrayList<>();
        }
        recordList.forEach(record -> {
            if (record.getStr("user_id") != null && !"".equals(record.getStr("user_id"))) {
                String sql1 = "select  user_id,realname,img from lkcrm_admin_user where user_id = ?";
                record.set("user", JavaBeanUtil.mapToRecord(commentDao.queryUniqueSql(sql1, record.getStr("user_id"))));
            }
            if (record.getStr("pid") != null && !"0".equals(record.getStr("pid")) && !"".equals(record.getStr("pid"))) {
                String sql2 = "select user_id,realname  from lkcrm_admin_user where user_id = ?";
                record.set("replyUser", JavaBeanUtil.mapToRecord(commentDao.queryUniqueSql(sql2, record.getStr("pid"))));
            }
        });
        Map<Integer, List<Record>> pMap = recordList.stream().collect(Collectors.groupingBy(record -> record.getInt("main_id")));
        recordList = pMap.get(0);
        recordList.forEach(record -> {
            Integer commentId = record.getInt("comment_id");
            if (pMap.get(commentId) != null) {
                record.set("childCommentList", pMap.get(commentId));
            } else {
                record.set("childCommentList", new ArrayList<>());
            }
        });
        return recordList;
    }
}
