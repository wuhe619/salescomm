package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmOaLogEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmOaLogDao extends SimpleHibernateDao<LkCrmOaLogEntity, Integer> {

    public List queryLogRelation(Long userId, String businessIds, String contactsIds, String contractIds, String customerIds) {
        String sql = "SELECT a.*, b.dept_id,\n" +
                "      b.realname,\n" +
                "      '' as userImg,\n" +
                "      soal.customer_ids,\n" +
                "      soal.contacts_ids,\n" +
                "      soal.business_ids,\n" +
                "      soal.contract_ids\n" +
                "    FROM lkcrm_oa_log as a left join lkcrm_oa_log_relation as d on a.log_id = d.log_id\n" +
                "    LEFT JOIN t_customer_user as b on a.create_user_id=b.id\n" +
                "     LEFT JOIN lkcrm_oa_log_relation as soal on soal.log_id=a.log_id\n" +
                "    WHERE 1 = 2 ";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(businessIds)) {
            param.add(businessIds);
            sql += " or d.business_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contactsIds)) {
            param.add(contactsIds);
            sql += " or d.contacts_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contractIds)) {
            param.add(contractIds);
            sql += " or d.contract_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(customerIds)) {
            param.add(customerIds);
            sql += " or d.customer_ids like concat('%,',?,',%')";
        }
        sql += " AND a.cust_id = ? ";
        param.add(BaseUtil.getCustId());
        return super.sqlQuery(sql, param.toArray());
    }

    public Page pageQueryLogRelation(int pageNum, int pageSize, String businessIds, String contactsIds, String contractIds, String customerIds) {
        String sql = "SELECT a.*, \n" +
                "      b.realname,\n" +
                "      '' as userImg,\n" +
                "      soal.customer_ids,\n" +
                "      soal.contacts_ids,\n" +
                "      soal.business_ids,\n" +
                "      soal.contract_ids\n" +
                "    FROM lkcrm_oa_log as a left join lkcrm_oa_log_relation as d on a.log_id = d.log_id\n" +
                "    LEFT JOIN t_customer_user as b on a.create_user_id=b.id\n" +
                "     LEFT JOIN lkcrm_oa_log_relation as soal on soal.log_id=a.log_id\n" +
                "    WHERE 1 = 2 ";
        List param = new ArrayList();
        if (StringUtil.isNotEmpty(businessIds)) {
            param.add(businessIds);
            sql += " or d.business_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contactsIds)) {
            param.add(contactsIds);
            sql += " or d.contacts_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(contractIds)) {
            param.add(contractIds);
            sql += " or d.contract_ids like concat('%,',?,',%')";
        }
        if (StringUtil.isNotEmpty(customerIds)) {
            param.add(customerIds);
            sql += " or d.customer_ids like concat('%,',?,',%')";
        }
        sql += " AND a.cust_id = ? ";
        param.add(BaseUtil.getCustId());
        return super.sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }


    public Page queryList(int pageNum, int pageSize, Long create_user_id, Integer by, Integer send_user_ids,
                          Integer send_dept_ids, List<Long> userIds, String createTime, Integer category_id,
                          Integer logId, Long userId) {
        String sql = "SELECT\n" +
                "      a.*, b.dept_id,\n" +
                "      b.realname,\n" +
                "      b.img as userImg,\n" +
                "      soal.customer_ids,\n" +
                "      soal.contacts_ids,\n" +
                "      soal.business_ids,\n" +
                "      soal.contract_ids\n" +
                "    FROM  lkcrm_oa_log as a\n" +
                "    LEFT JOIN lkcrm_admin_user as b on a.create_user_id=b.user_id\n" +
                "    LEFT JOIN lkcrm_oa_log_relation as soal on soal.log_id=a.log_id\n" +
                "    WHERE 1 = 1";
        List param = new ArrayList();
        if (create_user_id != null) {
            param.add(create_user_id);
            sql += "  and a.create_user_id = ? ";
        }
        if (create_user_id != null && by != null) {
            param.add(send_user_ids);
            param.add(send_dept_ids);
            sql += " and (a.send_user_ids like concat(\"%,\",?,\",%\") or a.send_dept_ids like concat(\"%,\",?,\",%\")";
            if (userIds != null && userIds.size() > 0) {
                sql += "  or a.create_user_id in (" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")";
            }
            sql+=" ) ";
        }
        if (StringUtil.isNotEmpty(createTime)) {
            param.add(createTime);
            sql += "   and to_days(a.create_time) = to_days(?)";
        }
        if (category_id != null) {
            param.add(category_id);
            sql += "   and category_id = ? ";
        }
        if (logId != null) {
            param.add(logId);
            sql += "   and a.log_id = ? ";
        }
        if (by != null && by == 3) {
            param.add(userId);
            sql += "   and read_user_ids not like concat(\"%,\",?,\",%\") ";
        }
        sql += " AND a.cust_id = ? ";
        param.add(BaseUtil.getCustId());
        sql += " order by a.log_id desc";
        return super.sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }

}
