package com.bdaim.crm.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.crm.entity.LkCrmProductEntity;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.util.SqlAppendUtil;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class LkCrmProductDao extends SimpleHibernateDao<LkCrmProductEntity, Integer> {

    public List<Map<String, Object>> querySubtotalByBusinessId(int business_id) {
        String sql = "   select distinct ccc.discount_rate,\n" +
                "      round((( select SUM(subtotal) from lkcrm_crm_business_product WHERE business_id = ccc.business_id ) /100*(100 - ccc.discount_rate) ),2)as money\n" +
                "      from lkcrm_crm_business as ccc\n" +
                "      where ccc.business_id = ?";
        return sqlQuery(sql, business_id);
    }

    public Page getProductPageList(int pageNum, int pageSize, String custId) {
        String productview = BaseUtil.getViewSql("productview");
        String sql = "select * from " + productview + " WHERE cust_id = ? ";
        return sqlPageQuery(sql, pageNum, pageSize, custId);
    }

    public int getByNum(String num) {
        String productview = BaseUtil.getViewSql("productview");
        String sql = "select COUNT(*) from " + productview + " where num = ? ";
        return queryForInt(sql, num);
    }

    public List<Map<String, Object>> excelExport(List productIds) {
        String productview = BaseUtil.getViewSql("productview");
        String sql = " select * from " + productview + " where product_id in (" + SqlAppendUtil.sqlAppendWhereIn(productIds) + ")";
        return sqlQuery(sql);
    }
}
