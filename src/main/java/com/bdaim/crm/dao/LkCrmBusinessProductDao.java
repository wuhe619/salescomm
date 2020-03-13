package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmBusinessProductEntity;
import com.bdaim.crm.utils.BaseUtil;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LkCrmBusinessProductDao extends SimpleHibernateDao<LkCrmBusinessProductEntity, Integer> {

    public Map<String, Object> querySubtotalByBusinessId(int contract_id) {
        String sql = " select distinct ccc.discount_rate,\n" +
                "      round((( select SUM(subtotal) from lkcrm_crm_business_product WHERE business_id = ccc.business_id ) /100*(100 - ccc.discount_rate) ),2)as money\n" +
                "      from lkcrm_crm_business as ccc\n" +
                "      where ccc.business_id = ?";
        List<Map<String, Object>> maps = super.sqlQuery(sql, contract_id);
        return maps.size() > 0 ? maps.get(0) : new HashMap<>();
    }

    public List queryProductPageList(int contract_id) {
        String productview = BaseUtil.getViewSqlNotASName("productview");
        String sql = "  select sccp.product_id,scp.name as product_name,scp.`单位` as unit,sccp.price,\n" +
                "      sccp.sales_price,sccp.num,sccp.discount,sccp.subtotal,scp.`是否上下架` ,scp.category_name\n" +
                "      FROM lkcrm_crm_contract_product sccp\n" +
                "      LEFT JOIN "+productview+" as scp on scp.product_id = sccp.product_id\n" +
                "      where sccp.contract_id = ?";
        return super.sqlQuery(sql, contract_id);
    }

    public Page pageQueryProductPageList(int pageNum, int pageSize, int contract_id) {
        String productview = BaseUtil.getViewSqlNotASName("productview");
        String sql = "  select sccp.product_id,scp.name as product_name,scp.`单位` as unit,sccp.price,\n" +
                "      sccp.sales_price,sccp.num,sccp.discount,sccp.subtotal,scp.`是否上下架` ,scp.category_name\n" +
                "      FROM lkcrm_crm_contract_product sccp\n" +
                "      LEFT JOIN "+productview+" as scp on scp.product_id = sccp.product_id\n" +
                "      where sccp.contract_id = ?";
        return super.sqlPageQuery(sql, pageNum, pageSize, contract_id);
    }
}
