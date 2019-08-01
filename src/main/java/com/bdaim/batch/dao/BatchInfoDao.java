package com.bdaim.batch.dao;

import com.bdaim.batch.entity.BatchInfo;
import com.bdaim.common.dao.SimpleHibernateDao;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @description:
 * @auther: Chacker
 * @date: 2019/8/1 09:38
 */
@Component
public class BatchInfoDao extends SimpleHibernateDao<BatchInfo, Serializable> {
    public void saveBatchInfo(BatchInfo b) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO nl_batch (id,batch_name,batch_type,status,upload_num,success_num,upload_time) VALUES ('");
        stringBuilder.append(b.getId());
        stringBuilder.append("','");
        stringBuilder.append(b.getBatchName());
        stringBuilder.append("','");
        stringBuilder.append(b.getBatchType());
        stringBuilder.append("','");
        stringBuilder.append(b.getStatus());
        stringBuilder.append("','");
        stringBuilder.append(b.getUploadNum());
        stringBuilder.append("','");
        stringBuilder.append(b.getSuccessNum());
        stringBuilder.append("','");
        stringBuilder.append(b.getUploadTime());
        stringBuilder.append("')");
        this.executeUpdateSQL(stringBuilder.toString());
    }
}
