package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmReceivablesEntity;
import com.bdaim.crm.entity.LkCrmReceivablesPlanEntity;
import com.bdaim.util.SqlAppendUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmReceivablesPlanDao extends SimpleHibernateDao<LkCrmReceivablesPlanEntity, Integer> {

    public List<LkCrmReceivablesPlanEntity> queryReceivablesPlanById(int contract_id) {
        return super.queryListBySql("select * from lkcrm_crm_receivables_plan where contract_id = ?", LkCrmReceivablesPlanEntity.class, contract_id);
    }

    public List<Map<String, Object>> queryListByContractId(int contract_id) {
        String sql = " select scrp.plan_id,  scrp.num,scc.customer_name ,scco.num as contract_num ,scrp.remind,\n" +
                "     scrp.money,scrp.return_date,return_type,\n" +
                "     scrp.remind,scrp.remark\n" +
                "                     from lkcrm_crm_receivables_plan as scrp\n" +
                "                    LEFT JOIN lkcrm_crm_customer as scc on scc.customer_id = scrp.customer_id\n" +
                "                     LEFT JOIN lkcrm_crm_contract as scco on scco.contract_id = scrp.contract_id\n" +
                "                    where scrp.contract_id = ?";
        return super.sqlQuery(sql, contract_id);
    }

    public Page pageListByContractId(int pageNum, int pageSize, int contract_id) {
        String sql = " select scrp.plan_id,  scrp.num,scc.customer_name ,scco.num as contract_num ,scrp.remind,\n" +
                "     scrp.money,scrp.return_date,return_type,\n" +
                "     scrp.remind,scrp.remark\n" +
                "                     from lkcrm_crm_receivables_plan as scrp\n" +
                "                    LEFT JOIN lkcrm_crm_customer as scc on scc.customer_id = scrp.customer_id\n" +
                "                     LEFT JOIN lkcrm_crm_contract as scco on scco.contract_id = scrp.contract_id\n" +
                "                    where scrp.contract_id = ?";
        return super.sqlPageQuery(sql, pageNum, pageSize, contract_id);
    }

    public List<LkCrmReceivablesEntity> queryReceivablesReceivablesId(List receivablesIds) {
        String sql = " select * from lkcrm_crm_receivables_plan where receivables_id in(" + SqlAppendUtil.sqlAppendWhereIn(receivablesIds) + ")";
        return super.queryListBySql(sql, LkCrmReceivablesEntity.class);
    }
}
