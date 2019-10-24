package com.bdaim.industry.service;

import com.bdaim.common.exception.TouchException;
import com.bdaim.industry.dao.CustomerIndustryPoolDao;
import com.bdaim.industry.entity.CustIndustryDO;
import com.bdaim.util.DateUtil;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Date;
import java.util.List;

/**
 */
@Service("customerIndustryPoolService")
public class CustomerIndustryPoolService {
    @Resource
    CustomerIndustryPoolDao dao;
    @Resource
    JdbcTemplate jdbcTemplate;

    public void addCustomerIndustryPool(String customerId, String industryPoolId, String operator) throws Exception {
        StringBuilder sql = new StringBuilder(" select 1 from t_cust_industry where cust_id=? and industry_pool_id=? ");
        try {
            List result = jdbcTemplate.queryForList(sql.toString(), new Object[]{customerId, industryPoolId}, new int[]{Types.VARCHAR, Types.INTEGER});
            if (null != result && result.size() > 0) {
                StringBuilder update = new StringBuilder("update t_cust_industry set status=1,modify_time=? where cust_id=? and industry_pool_id=? ");
                jdbcTemplate.update(update.toString(), new Object[]{new Timestamp(System.currentTimeMillis()), customerId, industryPoolId}, new int[]{Types.TIMESTAMP, Types.VARCHAR, Types.INTEGER});
            } else {
                CustIndustryDO custIndustryDO = new CustIndustryDO();
                custIndustryDO.setCustId(Long.valueOf(customerId));
                custIndustryDO.setIndustryPoolId(industryPoolId);
                custIndustryDO.setOperator(operator);
                custIndustryDO.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                custIndustryDO.setModifyTime(new Timestamp(System.currentTimeMillis()));
                custIndustryDO.setStatus(1);
                dao.save(custIndustryDO);
            }
        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }

    }

    public void deleteCustomerIndustryPool(String customerId, String industryPoolId, String operator) throws Exception {
        String sql = "update t_cust_industry t1 set t1.status=2 ," + "t1.operator='" + StringEscapeUtils.escapeSql(operator) + "'" + " where t1.cust_id='" + StringEscapeUtils.escapeSql(customerId) + "' and t1.industry_pool_id='" + StringEscapeUtils.escapeSql(industryPoolId) + "'";
        try {
            dao.executeUpdateSQL(sql);
        } catch (Exception e) {
            throw new TouchException("300", e.getMessage());
        }
    }
}
