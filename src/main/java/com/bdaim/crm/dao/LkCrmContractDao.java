package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmContractEntity;
import com.bdaim.util.SqlAppendUtil;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmContractDao extends SimpleHibernateDao<LkCrmContractEntity, Integer> {

    public List queryByIds(List deptIds) {
        return super.queryListBySql("  select * from  lkcrm_crm_contract\n" +
                "       where contract_id in  (" + SqlAppendUtil.sqlAppendWhereIn(deptIds) + ")");
    }
    
    public List getProductPageList() {
        return super.sqlQuery(" select * from contractview");
    }

    public Page pageProductPageList(int pageNum, int pageSize) {
        return super.sqlPageQuery(" select * from contractview", pageNum, pageSize);
    }

    public Map<String, Object> queryByContractId(Integer id) {
        String sql = "select *,  ( select IFNULL(sum(money),0) from lkcrm_crm_receivables where contract_id =  crt.contract_id and check_status = 2) as receivablesMoney from contractview as crt where crt.contract_id = ?";
        List<Map<String, Object>> maps = super.sqlQuery(sql, id);
        if (maps.size() > 0) {
            return maps.get(0);
        }
        return null;
    }

    public int deleteMember(String memberId, Integer contractId) {
        return super.executeUpdateSQL("update lkcrm_crm_contract set rw_user_id = replace(rw_user_id,?,','),ro_user_id = replace(ro_user_id,?,',') where contract_id = ?", memberId, memberId, contractId);
    }

    public int queryByNum(String num) {
        return super.queryForInt("  select count(*) from lkcrm_crm_contract where num = ?", num);
    }


    public int deleteByContractId(int contractId) {
        return super.executeUpdateSQL("delete from lkcrm_crm_contract_product where contract_id = ?", contractId);
    }

    public List<Map<String, Object>> queryProductById(String batchId) {
        return super.sqlQuery(" select * from lkcrm_crm_product where batch_id = ?", batchId);
    }


    public List<Map<String, Object>> getRecord(int contract_id) {
        return super.sqlQuery("select a.record_id, '' AS img as user_img,b.realname,a.create_time,a.content,a.category,a.next_time,a.batch_id\n" +
                "    from lkcrm_admin_record as a inner join t_customer_user as b\n" +
                "    where a.create_user_id = b.id and types = 'crm_contract' and types_id = ? order by a.create_time desc", contract_id);
    }

    public int setContractConfig(Integer status, Integer contractDay) {
        String sql = "  update lkcrm_admin_config set status = ? ";
        List param = new ArrayList();
        param.add(status);
        if (contractDay != null) {
            sql += " ,value = ? ";
            param.add(contractDay);
        }
        sql += " where name = 'expiringContractDays'";
        return super.executeUpdateSQL(sql, param.toArray());
    }

    public List<Map<String, Object>> queryBusinessProduct(Integer contractId) {
        return super.sqlQuery(" select c.product_id , c.name as name,d.name as category_name,b.unit,b.price,b.sales_price,b.num,b.discount,b.subtotal\n" +
                "      from lkcrm_crm_contract as a inner join lkcrm_crm_contract_product as b on a.contract_id = b.contract_id\n" +
                "      inner join lkcrm_crm_product as c on b.product_id = c.product_id inner join lkcrm_crm_product_category as d\n" +
                "      on c.category_id = d.category_id\n" +
                "      where a.contract_id = ?", contractId);
    }


    public int updateCheckStatusById(int check_status, int contractId) {
        return super.executeUpdateSQL(" update lkcrm_crm_contract set check_status = ? where contract_id = ?", check_status, contractId);
    }


}
