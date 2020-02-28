package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmOaLogEntity;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

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
        return super.sqlQuery(sql, param.toArray());
    }

    public Page pageQueryLogRelation(int pageNum, int pageSize, String businessIds, String contactsIds, String contractIds, String customerIds) {
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
        return super.sqlPageQuery(sql, pageNum, pageSize, param.toArray());
    }
}
