package com.bdaim.supplier.dao;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.StringUtil;
import com.bdaim.price.dto.ResourcesPriceDto;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.slxf.dto.MarketResourceLogDTO;
import com.bdaim.supplier.entity.SupplierEntity;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class SupplierDao extends SimpleHibernateDao<SupplierEntity, Integer> {

    public List<MarketResourceLogDTO> listMarketResourceBySupplierId(String supplierId) {
        String hql = "from MarketResourceEntity m where m.supplierId=? AND m.status = 1 ORDER BY createTime ASC ";
        List<MarketResourceEntity> list = this.find(hql, supplierId);
        List<MarketResourceLogDTO> result = new ArrayList<>();
        if (list.size() > 0) {
            MarketResourceLogDTO marketResourceDTO;
            for (int i = 0; i < list.size(); i++) {
                marketResourceDTO = new MarketResourceLogDTO(list.get(i));
                result.add(marketResourceDTO);
            }
        }
        return result;
    }

    /**
     * 根据资源类型获取供应商列表
     *
     * @param type
     * @return
     */
    public List<SupplierEntity> listAllSupplierByResourceType(int type) {
        // 查询所有数据供应商
        StringBuffer hql = new StringBuffer(" FROM SupplierEntity m WHERE status=1 AND m.supplierId IN(SELECT supplierId FROM MarketResourceEntity WHERE typeCode = ? AND status = 1)");
        hql.append(" ORDER BY m.createTime DESC");
        List<SupplierEntity> supplierDOList = this.find(hql.toString(), type);
        List<SupplierEntity> result = new ArrayList<>();
        for (SupplierEntity m : supplierDOList) {
            result.add(m);
        }
        return result;
    }

    /**
     * 查询供应商集合信息
     *
     * @return
     */
    public SupplierEntity getSupplierList(int supplier) {
        SupplierEntity cp = null;
        String hql = "from SupplierEntity m where m.supplierId=? ";
        List<SupplierEntity> list = this.find(hql, supplier);
        if (list.size() > 0)
            cp = (SupplierEntity) list.get(0);
        return cp;
    }

    /**
     * 根据资源id查询供应商资源配置信息
     *
     * @author:duanliying
     * @date: 2019/4/4 18:06
     */
    public ResourcesPriceDto getSupResourceMessageById(int resourceId, String propertyName) {
        if (StringUtil.isEmpty(propertyName)) {
            propertyName = "price_config";
        }
        ResourcesPriceDto resourcesPriceDto = null;
        String hql = "from ResourcePropertyEntity m where m.resourceId=? AND m.propertyName = ?";
        try {
            List<ResourcePropertyEntity> list = this.find(hql, resourceId, propertyName);
            if (list.size() > 0) {
                ResourcePropertyEntity cp = (ResourcePropertyEntity) list.get(0);
                if (StringUtil.isNotEmpty(cp.getPropertyValue())) {
                    resourcesPriceDto = JSONObject.parseObject(cp.getPropertyValue(), ResourcesPriceDto.class);
                    logger.info("查询供应商资源信息是：" + resourcesPriceDto.toString() + "资源d是" + resourceId);
                }
            }
        } catch (Exception e) {
            logger.error("获取资源信息异常" + e);
        }
        return resourcesPriceDto;
    }

}
