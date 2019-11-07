package com.bdaim.customer.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.customs.entity.HMetaDataDef;
import com.bdaim.util.StringUtil;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service("busi_express_order")
@Transactional
public class ExpressOrderService implements BusiService {

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
    }

    @Override
    public void updateInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {

    }

    @Override
    public void doInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info, JSONObject param) throws Exception {

    }

    @Override
    public void deleteInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id) throws Exception {

    }

    @Override
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) {
        StringBuffer sqlstr = new StringBuffer("select id, content , cust_id, create_id, create_date,ext_1, ext_2, ext_3, ext_4, ext_5 from "
                + HMetaDataDef.getTable(busiType, "") + " where type = '").append(busiType).append("'");
        if (!"all".equals(cust_id))
            sqlstr.append(" and  cust_id='").append(cust_id).append("'");

        String txLogisticID = params.getString("txLogisticID");
        String mailNo = params.getString("mailNo");
        String _orderby_ = params.getString("_orderby_");
        String _sort_ = params.getString("_sort_");
        if (StringUtil.isNotEmpty(txLogisticID)) {
            sqlstr.append(" and ext_2 = '").append(txLogisticID).append("'");
        }
        if (StringUtil.isNotEmpty(mailNo)) {
            sqlstr.append(" and ext_1 = '").append(mailNo).append("'");
        }
        if (StringUtil.isNotEmpty(_orderby_)) {
            sqlstr.append(" order by  ").append(_orderby_).append(" ");
        }

        if (StringUtil.isNotEmpty(_sort_)) {
            sqlstr.append(_sort_);
        }
        return sqlstr.toString();
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {

    }
}
