package com.bdaim.batch.dao;

import com.bdaim.batch.entity.BatchDetailInfo;
import com.bdaim.common.dao.SimpleHibernateDao;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @description:
 * @auther: Chacker
 * @date: 2019/8/1 10:24
 */
@Component
public class BatchInfoDetailDao extends SimpleHibernateDao<BatchDetailInfo, Serializable> {
    public void saveBatchInfoDetail(BatchDetailInfo batchDetailInfo) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO nl_batch_detail (id,batch_id,label_one,label_two,label_four,label_seven) VALUES ('");
        stringBuilder.append(batchDetailInfo.getId());
        stringBuilder.append("','");
        stringBuilder.append(batchDetailInfo.getBatchId());
        stringBuilder.append("','");
        stringBuilder.append(batchDetailInfo.getName());
        stringBuilder.append("','");
        stringBuilder.append(batchDetailInfo.getPhone());
        stringBuilder.append("','");
        stringBuilder.append(batchDetailInfo.getLabelFour());
        stringBuilder.append("','");
        stringBuilder.append(batchDetailInfo.getCheckingResult());
        stringBuilder.append("')");
        this.executeUpdateSQL(stringBuilder.toString());
    }
}
