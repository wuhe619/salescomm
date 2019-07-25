package com.bdaim.batch.dao;

import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.template.entity.MarketTemplate;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @author duanliying
 * @date 2018/9/6
 * @description
 */
@Component
public class BatchDetailDao extends SimpleHibernateDao<BatchDetail, Serializable> {

    public BatchDetail getBatchDetail(String id, String batchId) {
        List<BatchDetail> list = this.find("FROM BatchDetail t WHERE t.id = ? AND t.batchId = ? ", id, batchId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }
}
