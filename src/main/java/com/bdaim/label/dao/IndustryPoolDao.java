package com.bdaim.label.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.label.entity.IndustryPool;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.supplier.dto.SupplierDTO;
import com.bdaim.supplier.entity.SupplierEntity;
import org.springframework.stereotype.Component;

import java.io.Serializable;

/**
 *
 */
@Component
public class IndustryPoolDao extends SimpleHibernateDao<IndustryPool, Serializable> {


    public SupplierDTO getSupplierInfo(int poolId) {
        IndustryPool industryPool = (IndustryPool) this.get(poolId);
        MarketResourceEntity marketResource = this.findUnique(" FROM MarketResource m where m.resourceId=?", (long) industryPool.getSourceId());
        if (marketResource != null) {
            SupplierEntity supplierDO = this.findUnique(" FROM SupplierDO m where m.supplierId=?", Long.parseLong(marketResource.getSupplierId()));
            return new SupplierDTO(supplierDO);
        }
        return null;
    }
}
