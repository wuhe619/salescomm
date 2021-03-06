package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmReceivablesEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmReceivablesDao extends SimpleHibernateDao<LkCrmReceivablesEntity, Integer> {

    public List<LkCrmReceivablesEntity> queryReceivablesByContractIds(List contract_id) {
        return super.queryListBySql(" select * from lkcrm_crm_receivables where contract_id in (" + SqlAppendUtil.sqlAppendWhereIn(contract_id) + ")", LkCrmReceivablesEntity.class);
    }

    public List<LkCrmReceivablesEntity> queryReceivablesByContractId(int contract_id) {
        return super.queryListBySql("  select * from lkcrm_crm_receivables where contract_id = ?", LkCrmReceivablesEntity.class, contract_id);
    }

    public List queryReceivablesPageList(int contract_id) {
        String receivablesview = BaseUtil.getViewSqlNotASName("receivablesview");
        String sql = "select  rec.receivables_id,rec.number as receivables_num,rec.contract_name as contract_name,scco.money as contract_money\n" +
                "                ,rec.owner_user_name,\n" +
                "                case rec.check_status\n" +
                "                when 1 then '审核中'\n" +
                "                when 3 then '审核未通过'\n" +
                "                when 2 then '审核通过'\n" +
                "                when 4 then '已撤回'\n" +
                "                ELSE '未审核' END\n" +
                "                as check_status,rec.return_time,rec.money as receivables_money,rec.plan_num\n" +
                "        FROM " + receivablesview + " as rec\n" +
                "        LEFT JOIN lkcrm_crm_contract as scco on scco.contract_id = rec.contract_id\n" +
                "        where rec.contract_id = ?";
        return super.sqlQuery(sql, contract_id);
    }

    public Page pageQueryReceivablesPageList(int pageNum, int pageSize, int contract_id) {
        String receivablesview = BaseUtil.getViewSqlNotASName("receivablesview");
        String sql = "select  rec.receivables_id,rec.number as receivables_num,rec.contract_name as contract_name,scco.money as contract_money\n" +
                "                ,rec.owner_user_name,\n" +
                "                case rec.check_status\n" +
                "                when 1 then '审核中'\n" +
                "                when 3 then '审核未通过'\n" +
                "                when 2 then '审核通过'\n" +
                "                when 4 then '已撤回'\n" +
                "                ELSE '未审核' END\n" +
                "                as check_status,rec.return_time,rec.money as receivables_money,rec.plan_num\n" +
                "        FROM " + receivablesview + " as rec\n" +
                "        LEFT JOIN lkcrm_crm_contract as scco on scco.contract_id = rec.contract_id\n" +
                "        where rec.contract_id = ?";
        return super.sqlPageQuery(sql, pageNum, pageSize, contract_id);
    }

    public Page getReceivablesPageList(int pageNum, int pageSize) {
        String receivablesview = BaseUtil.getViewSql("receivablesview");
        return super.sqlPageQuery("  select * from  " + receivablesview, pageNum, pageSize);
    }

    public List<Map<String, Object>> queryReceivablesById(int receivables_id) {
        String receivablesview = BaseUtil.getViewSqlNotASName("receivablesview");
        String sql = "select rb.* ,scc.money as contract_money ,saf.value as receivable_way\n" +
                "        from "+receivablesview+" as rb\n" +
                "        LEFT JOIN lkcrm_crm_contract as scc on scc.contract_id = rb.contract_id\n" +
                "        LEFT JOIN lkcrm_admin_fieldv as saf on saf.batch_id = rb.batch_id AND saf.name = '回款方式'\n" +
                "        where rb.receivables_id = ?";
        return super.sqlQuery(sql, receivables_id);
    }

    public int queryByNumber(String number) {
        String sql = " select * from lkcrm_crm_receivables where number = ?";
        return super.sqlQuery(sql, number).size();
    }

    public int updateCheckStatusById(int check_status, int receivables_id) {
        return super.executeUpdateSQL("  update lkcrm_crm_receivables set check_status = ? where receivables_id = ?", check_status, receivables_id);
    }

}
