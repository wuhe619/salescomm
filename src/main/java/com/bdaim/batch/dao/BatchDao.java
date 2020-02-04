package com.bdaim.batch.dao;

import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.batch.entity.BatchListEntity;
import com.bdaim.common.dao.SimpleHibernateDao;

import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/6
 * @description
 */
@Component
public class BatchDao extends SimpleHibernateDao<BatchListEntity, Serializable> {


    public BatchDetail getBatchDetail(String id, String batchId) {
        List<BatchDetail> list = this.find("FROM BatchDetail t WHERE t.id = ? AND t.batchId = ? ", id, batchId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public BatchListEntity getBatchMessage(String batchId) {
        List<BatchListEntity> list = this.find("FROM BatchListEntity t WHERE t.id = ?", batchId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public String getBatchName(String batchId) {
        try {
            BatchListEntity cu = this.get(batchId);
            if (cu != null)
                return cu.getBatchName();
        } catch (Exception e) {

        }
        return "";
    }

    public List<BatchListEntity> getBatchDetailList(String custId, int certifyType) {
        List<BatchListEntity> list = null;
        if (certifyType == 0) {
            list = this.find("FROM BatchListEntity t WHERE comp_id =? and certify_type = 0", custId);
        } else {
            list = this.find("FROM BatchListEntity t WHERE comp_id =? and certify_type != 0", custId);
        }
        return list;
    }

    public List<BatchListEntity> getBatchDetailListBySupplierId(String custId, int supplierId) {
        List<BatchListEntity> list = this.find("FROM BatchListEntity  WHERE compId =? and channel =?", custId, supplierId);
        return list;
    }
}
