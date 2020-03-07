package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
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
                "     scrp.money,scrp.return_date,return_type, scrp.remark\n" +
                "                     from lkcrm_crm_receivables_plan as scrp\n" +
                "                    LEFT JOIN lkcrm_crm_customer as scc on scc.customer_id = scrp.customer_id\n" +
                "                     LEFT JOIN lkcrm_crm_contract as scco on scco.contract_id = scrp.contract_id\n" +
                "                    where scrp.contract_id = ?";
        return super.sqlQuery(sql, contract_id);
    }

    public Page pageListByContractId(int pageNum, int pageSize, int contract_id) {
        String sql = " select scrp.plan_id,  scrp.num,scc.customer_name ,scco.num as contract_num ,scrp.remind,\n" +
                "     scrp.money,scrp.return_date,return_type,\n" +
                "     scrp.remark\n" +
                "                     from lkcrm_crm_receivables_plan as scrp\n" +
                "                    LEFT JOIN lkcrm_crm_customer as scc on scc.customer_id = scrp.customer_id\n" +
                "                     LEFT JOIN lkcrm_crm_contract as scco on scco.contract_id = scrp.contract_id\n" +
                "                    where scrp.contract_id = ?";
        return super.sqlPageQuery(sql, pageNum, pageSize, contract_id);
    }

    public List<LkCrmReceivablesPlanEntity> queryReceivablesReceivablesId(List receivablesIds) {
        String sql = " select * from lkcrm_crm_receivables_plan where receivables_id in(" + SqlAppendUtil.sqlAppendWhereIn(receivablesIds) + ")";
        return super.queryListBySql(sql, LkCrmReceivablesPlanEntity.class);
    }

    public LkCrmReceivablesPlanEntity queryByContractId(int contract_id) {
        String sql = " SELECT * FROM lkcrm_crm_receivables_plan where contract_id = ? order by num desc limit 0,1 ";
        List<LkCrmReceivablesPlanEntity> list = super.queryListBySql(sql, LkCrmReceivablesPlanEntity.class, contract_id);
        return list.size() > 0 ? list.get(0) : null;
    }

    public int deleteByIds(List planIds) {
        String sql = "delete from lkcrm_crm_receivables_plan where plan_id IN (" + SqlAppendUtil.sqlAppendWhereIn(planIds) + ")";
        return super.executeUpdateSQL(sql);
    }

    public List<LkCrmReceivablesPlanEntity> queryByCustomerIdContractId(int contract_id, int customer_id) {
        String sql = "SELECT * from lkcrm_crm_receivables_plan WHERE receivables_id is null and contract_id = ? and customer_id = ?";
        return super.queryListBySql(sql, LkCrmReceivablesPlanEntity.class, contract_id, customer_id);
    }
}
