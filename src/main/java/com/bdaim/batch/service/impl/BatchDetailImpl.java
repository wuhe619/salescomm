package com.bdaim.batch.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.dto.DetailQueryParam;
import com.bdaim.batch.service.BatchDetaiService;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.PageList;
import com.bdaim.common.util.page.Pagination;

import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import java.util.*;

/**
 * @author duanliying
 * @date 2018/9/6
 * @description
 */
@Service
@Transactional
public class BatchDetailImpl implements BatchDetaiService {
    @Resource
    private JdbcTemplate jdbcTemplate;

    /**
     * @description 根据批次ID筛选出批次下的客户集合(对外接口)
     * @author:duanliying
     * @method 渠道/供应商 2-联通 4-移动 3-电信
     * @date: 2018/9/6 16:53
     */
    @Override
    public PageList getDetailListById(PageParam page, String batchId, String custId) {
        StringBuffer sql = new StringBuffer("SELECT  n.id superId,n.batch_id batchId,n.enterprise_id enterpriseId,n.id_card idCard,n.channel,n.label_one labelOne,n.label_two labelTwo,n.label_three labelThree,n.`status` ,n.upload_time uploadTime,n.fix_time fixTime");
        sql.append(" FROM nl_batch_detail n LEFT JOIN nl_batch nl ON n.batch_id = nl.id\n");
        if (StringUtil.isNotEmpty(batchId)) {
            sql.append("WHERE batch_id ='" + batchId + "'");
        }
        if (StringUtil.isNotEmpty(custId)) {
            sql.append(" and nl.comp_id ='" + custId + "'");
        }
        PageList list = new Pagination().getPageData(sql.toString(), null, page, jdbcTemplate);
        //查询批次下所有自建属性信息
        return list;
    }

    /**
     * @description 列表搜索--某批次被叫客户列表搜索
     * @author:duanliying
     * @method 渠道/供应商 2-联通 4-移动 3-电信
     * @date: 2018/9/6 11:26
     */
    @Override
    public PageList getDetailList(DetailQueryParam detailQueryParam, Long userId, String userType, JSONArray custProperty, String role) {
        String batchId = detailQueryParam.getBatchId();
        StringBuffer sqlBuilder = new StringBuffer();
        sqlBuilder.append("SELECT t.* FROM ( select custG.id,custG.batch_id,custG.enterprise_id,custG.id_card,custG.`status`,custG.channel,custG.user_id, t.realname,GROUP_CONCAT(t2.label_id) AS labelId,\n" +
                "\tGROUP_CONCAT(t3.label_name) AS labelName,GROUP_CONCAT(t2.option_value) AS optionValue");
        sqlBuilder.append("  from nl_batch_detail custG ");
        sqlBuilder.append("  LEFT JOIN t_customer_user t  ON custG.user_id = t.id");
        sqlBuilder.append("  LEFT JOIN t_touch_voice_info t4 ON custG.id= t4.super_id and custG.batch_id = t4.batch_id");
        sqlBuilder.append("  LEFT JOIN t_super_label t2 ON custG.id = t2.super_id AND t2.batch_id = '" + batchId + "'");
        sqlBuilder.append("  LEFT JOIN t_customer_label t3 ON t2.label_id = t3.label_id AND custG.batch_id = t4.batch_id");
        sqlBuilder.append(" where 1 = 1 ");
        if (userType.equals("2") && "ROLE_CUSTOMER".equals(role) ) {
            sqlBuilder.append("AND custG.user_id =" + userId);
        }
        if (StringUtil.isNotEmpty(detailQueryParam.getId())) {
            sqlBuilder.append(" and custG.id= '").append(StringEscapeUtils.escapeSql(detailQueryParam.getId())).append("'");
        }
        if (StringUtil.isNotEmpty(detailQueryParam.getIdCard())) {
            sqlBuilder.append(" and custG.id_card= '").append(StringEscapeUtils.escapeSql(detailQueryParam.getIdCard())).append("'");
        }
        if (StringUtil.isNotEmpty(detailQueryParam.getEnterpriseId())) {
            sqlBuilder.append(" and custG.enterprise_id= '").append(StringEscapeUtils.escapeSql(detailQueryParam.getEnterpriseId())).append("'");
        }
        if (StringUtil.isNotEmpty(detailQueryParam.getRealname())) {
            sqlBuilder.append(" and t.realname LIKE '%" + detailQueryParam.getRealname() + "%'");
        }
        if (detailQueryParam.getStatus() != null) {
            sqlBuilder.append(" and custG.status= '").append(StringEscapeUtils.escapeSql(String.valueOf(detailQueryParam.getStatus()))).append("'");
        }
        if (detailQueryParam.getBatchId() != null) {
            sqlBuilder.append(" and custG.batch_id= '").append(StringEscapeUtils.escapeSql(String.valueOf(detailQueryParam.getBatchId()))).append("'");
        }
        Set<String> labels = new HashSet<>();
        List<String> optionValues = new ArrayList<>();
        if (custProperty != null && custProperty.size() != 0) {
            JSONObject jsonObject;
            String optionValue;
            for (int i = 0; i < custProperty.size(); i++) {
                jsonObject = custProperty.getJSONObject(i);
                if (jsonObject != null) {
                    labels.add(jsonObject.getString("labelId"));
                    optionValue = jsonObject.getString("optionValue");
                    if (StringUtil.isNotEmpty(optionValue)) {
                        for (String key : optionValue.split(",")) {
                            optionValues.add(key);
                        }
                    }
                }
            }
        }
        sqlBuilder.append(" GROUP BY custG.id ,id_card");
        sqlBuilder.append(" ORDER BY custG.id DESC )t");
        if (labels.size() > 0) {
            sqlBuilder.append(" WHERE 1=1 ");
            for (String optionValue : optionValues) {
                sqlBuilder.append(" AND FIND_IN_SET('" + optionValue + "', t.optionValue) ");
            }
        }
        PageParam page = new PageParam();
        page.setPageNum(detailQueryParam.getPageNum());
        page.setPageSize(detailQueryParam.getPageSize());
        PageList pageData = new Pagination().getPageData(sqlBuilder.toString(), null, page, jdbcTemplate);
        List<Map> list = pageData.getList();
        for (int i = 0; i < list.size(); i++) {
            String queryCreatTime = "SELECT create_time FROM t_touch_voice_log  where batch_id =? AND superid=? ORDER BY create_time DESC LIMIT 1";
            List<Map<String, Object>> queryForList = jdbcTemplate.queryForList(queryCreatTime, list.get(i).get("batch_id"), list.get(i).get("id"));
            if (queryForList.size() > 0) {
                list.get(i).put("lastCallTime", queryForList.get(0).get("create_time"));
            }
            String CountNumSql = "SELECT COUNT(0) AS callCount FROM t_touch_voice_log where batch_id=?  AND  superid =? ORDER BY create_time DESC LIMIT 1 ";
            List<Map<String, Object>> countNumList = jdbcTemplate.queryForList(CountNumSql, list.get(i).get("batch_id"), list.get(i).get("id"));
            if (countNumList.size() > 0) {
                list.get(i).put("callCount", countNumList.get(0).get("callCount"));
            }
           /* List<CustomerUserDO> customerUserDO = customerUserDao.findBy("id", Long.valueOf(String.valueOf(list.get(i).get("user_id"))));
            if (customerUserDO.size() > 0) {
                list.get(i).put("realname", customerUserDO.get(0).getRealname());
            }*/
        }
        return pageData;
    }

    /**
     * @description 查询批次详情属性列表
     * @author:duanliying
     * @method
     * @date: 2018/10/20 17:37
     */
    @Override
    public Object getPropertyList(String batchId) {
        Map<String, Object> map = new HashMap<>();
        StringBuffer sb = new StringBuffer();
        sb.append("SELECT t1.id,t1.cust_id,t1.label_id,t1.label_name,t1.type,t1.`option` FROM t_customer_label t1\n");
        sb.append(" LEFT JOIN t_super_label t2 ON t1.label_id = t2.label_id");
        sb.append(" WHERE t2.batch_id = " + batchId);
        sb.append(" AND t1.STATUS = 1 AND t1.type != 1 GROUP BY t1.id ORDER BY t1.create_time DESC");
        List<Map<String, Object>> list = jdbcTemplate.queryForList(sb.toString());
        map.put("propertyList", list);
        return map;
    }

}