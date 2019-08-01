package com.bdaim.batch.dao;

import com.bdaim.batch.entity.BatchProperty;
import com.bdaim.common.dao.SimpleHibernateDao;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 * @description:
 * @auther: Chacker
 * @date: 2019/8/1 19:28
 */
@Component
public class BatchPropertyDao extends SimpleHibernateDao<BatchProperty, Serializable> {
    public void saveBatchProperty(BatchProperty batchProperty) {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("INSERT INTO nl_batch_property (batch_id,property_name,property_value,create_time) VALUES ('");
        stringBuilder.append(batchProperty.getBatchId());
        stringBuilder.append("','");
        stringBuilder.append(batchProperty.getPropertyName());
        stringBuilder.append("','");
        stringBuilder.append(batchProperty.getPropertyValue());
        stringBuilder.append("',NOW())");
        this.executeUpdateSQL(stringBuilder.toString());
    }
}
