package com.bdaim.crm.ent.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.service.BusiService;
import com.bdaim.customer.service.B2BTcbService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * @author chengning@salescomm.net
 * @description 企业数据导出
 * @date 2020/4/20
 */
@Service("busi_ent_export_task")
@Transactional
public class EntExportTaskService implements BusiService {

    private static Logger LOG = LoggerFactory.getLogger(EntExportTaskService.class);

    @Autowired
    B2BTcbService b2BTcbService;

    @Override
    public void insertInfo(String busiType, String cust_id, String cust_group_id, Long cust_user_id, Long id, JSONObject info) throws Exception {
        //文件生成状态 1-未生成 2-已生成
        info.put("ext_1", "1");

        JSONObject useB2BTcb = b2BTcbService.getUseB2BTcb(cust_id);
        if (useB2BTcb == null) {
            LOG.error("客户:" + cust_id + ",无可用套餐包");
            throw new RuntimeException("客户:" + cust_id + ",无可用套餐包");
        }
        long b2BTcbQuantity = b2BTcbService.getB2BTcbQuantity(cust_id);
        if (b2BTcbQuantity <= 0) {
            LOG.error("客户:" + cust_id + ",套餐余量为0");
            throw new RuntimeException("客户:" + cust_id + ",套餐余量为0");
        }

        int exportType = info.getIntValue("exportType");
        JSONObject condition = info.getJSONObject("condition");
        long size = 0L;
        if (exportType == 1 || exportType == 2) {
            // 实际导出量
            size = info.getIntValue("realNum");
            // 实际导出量大于套餐余量,则导出量为套餐余量
            if (info.getIntValue("realNum") > b2BTcbQuantity) {
                size = b2BTcbQuantity;
            }
        } else if (exportType == 3) {
            size = (info.getIntValue("pageEnd") * condition.getIntValue("pageSize")) -
                    (info.getIntValue("pageStart") * condition.getIntValue("pageSize"));
            // 实际导出量大于套餐余量,则导出量为套餐余量
            if (size > b2BTcbQuantity) {
                size = b2BTcbQuantity;
            }
        } else {
            LOG.warn("导出模式未匹配到,exportType:" + exportType);
            throw new RuntimeException("导出模式未匹配到,exportType:" + exportType);
        }
        // 扣除套餐余量
        b2BTcbService.updateTbRemain(useB2BTcb.getLongValue("id"), size, "b2b_tcb");
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
