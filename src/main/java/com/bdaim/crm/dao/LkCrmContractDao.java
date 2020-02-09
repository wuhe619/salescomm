package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmContractEntity;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmContractDao extends SimpleHibernateDao<LkCrmContractEntity, Integer> {

    public List getProductPageList() {
        return super.sqlQuery(" select * from contractview");
    }

    public Page pageProductPageList(int pageNum, int pageSize) {
        return super.sqlPageQuery(" select * from contractview", pageNum, pageSize);
    }

    public List<Map<String, Object>> queryByContractId(Integer id) {
        return super.sqlQuery(" select *,\n" +
                "    ( select IFNULL(sum(money),0) from 72crm_crm_receivables where contract_id =  crt.contract_id and check_status = 2) as receivablesMoney\n" +
                "    from contractview as crt where crt.contract_id = ?", id);
    }

    public int deleteMember(String memberId, Integer contractId) {
        return super.executeUpdateSQL("update 72crm_crm_contract set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where contract_id = ?", memberId, memberId, contractId);
    }

    public int queryByNum(String num) {
        return super.queryForInt("  select count(*) from 72crm_crm_contract where num = ?", num);
    }


    public int deleteByContractId(int contractId) {
        return super.queryForInt("delete from 72crm_crm_contract_product where contract_id = ?", contractId);
    }

    public List<Map<String, Object>> queryProductById(String batchId) {
        return super.sqlQuery(" select * from 72crm_crm_product where batch_id = ?", batchId);
    }


    public List<Map<String, Object>> getRecord(int contract_id) {
        return super.sqlQuery("select a.record_id,b.img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id\n" +
                "    from 72crm_admin_record as a inner join 72crm_admin_user as b\n" +
                "    where a.create_user_id = b.user_id and types = 'crm_contract' and types_id = ? order by a.create_time desc", contract_id);
    }

    public int setContractConfig(Integer status, Integer contractDay) {
        String sql = "  update 72crm_admin_config set status = ? ";
        List param = new ArrayList();
        param.add(status);
        if (contractDay != null) {
            sql += " ,value = ? ";
            param.add(contractDay);
        }
        sql += " where name = 'expiringContractDays'";
        return super.executeUpdateSQL(sql, param.toArray());
    }


}
