package com.bdaim.batch.controller;

import com.bdaim.batch.dao.BatchInfoDao;
import com.bdaim.common.response.ResponseBody;
import com.bdaim.common.response.ResponseInfoAssemble;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @description:
 * @auther: Chacker
 * @date: 2019/8/2 11:13
 */
@RestController
public class InsertDataController {
    @Autowired
    private BatchInfoDao batchInfoDao;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @RequestMapping(value = "/insertBatch")
    public ResponseBody insertBatch(String cust_id){
        String batchId = String.valueOf(System.currentTimeMillis());
        String insertHql = "INSERT INTO nl_batch (id,certify_type,channel,status,comp_id,batch_name,upload_num,success_num,upload_time) VALUES"
                + "('" +batchId+"',-1,-1,1,'"+cust_id+"','批次名称','10','10',NOW())";
        jdbcTemplate.update(insertHql);
        String insertHql2 = "INSERT INTO nl_batch_property (batch_id,property_name,property_value,create_time) VALUES (" +
                "'"+batchId+"','express_content_type','1',NOW())";
        jdbcTemplate.update(insertHql2);
        return new ResponseInfoAssemble().success(null);
    }
}
