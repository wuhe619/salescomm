package com.bdaim.crm.ent.service;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @description 企业数据条件组保存
 * @date 2020/4/15 16:35
 */
@Service("busi_ent_search_condition")
@Transactional
public class EntSearchConditionService implements BusiService {


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
    public String formatQuery(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject params, List sqlParams) throws Exception {
        return null;
    }

    @Override
    public void formatInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, JSONObject info) {

    }
}
