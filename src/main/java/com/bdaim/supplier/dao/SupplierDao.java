package com.bdaim.supplier.dao;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.dto.Page;
import com.bdaim.price.dto.ResourcesPriceDto;
import com.bdaim.resource.dto.MarketResourceLogDTO;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.supplier.dto.SupplierDTO;
import com.bdaim.supplier.entity.SupplierEntity;
import com.bdaim.supplier.entity.SupplierPropertyEntity;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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
     * 根据资源类型获取供应商列表
     *
     * @param type
     * @return
     */
    public List<SupplierDTO> listOnlineAllSupplierByResourceType(int type) {
        // 查询所有数据供应商
        StringBuffer hql = new StringBuffer(" FROM SupplierEntity m WHERE status=1 AND m.supplierId IN(SELECT supplierId FROM MarketResourceEntity WHERE typeCode = ? AND status = 1)");
        hql.append(" ORDER BY m.createTime DESC");
        List<SupplierEntity> supplierDOList = this.find(hql.toString(), type);
        List<SupplierDTO> result = new ArrayList<>();
        for (SupplierEntity m : supplierDOList) {
            result.add(new SupplierDTO(m));
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
    /**
     * @description 获取供应商属性信息
     * @author:duanliying
     * @method
     * @date: 2019/1/3 20:35
     */
    public SupplierPropertyEntity getSupplierProperty(String supplier, String propertyKey) {
        SupplierPropertyEntity cp = null;
        String hql = "from SupplierPropertyEntity m where m.supplierId=? and m.propertyName=?";
        List<SupplierPropertyEntity> list = this.find(hql, supplier, propertyKey);
        if (list.size() > 0)
            cp = (SupplierPropertyEntity) list.get(0);
        return cp;
    }

    public SupplierPropertyEntity getProperty(String supplierId, String propertyName) {
        SupplierPropertyEntity cp = null;
        String hql = "from SupplierPropertyEntity m where m.supplierId=? and m.propertyName=?";
        List<SupplierPropertyEntity> list = this.find(hql, supplierId, propertyName);
        if (list.size() > 0)
            cp = list.get(0);
        return cp;
    }

    public String getSupplierName(int supplierId) {
        String hql = "from SupplierEntity m where m.supplierId=?";
        List<SupplierEntity> list = this.find(hql, supplierId);
        if (list.size() > 0) {
            return list.get(0).getName();
        }
        return null;
    }

    public SupplierEntity getSupplier(int supplierId) {
        String hql = "from SupplierEntity m where m.supplierId=? AND m.status = 1";
        List<SupplierEntity> list = this.find(hql, supplierId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 保存供应商资源配置
     *
     * @param marketResource
     * @return
     */
    public void saveSuppliertResourceConfig(MarketResourceEntity marketResource) {
        this.saveOrUpdate(marketResource);
    }

    public Page pageSupplier(int pageIndex, int pageSize, String supplierName, String supplierId, String supplierType) {
        StringBuffer hql = new StringBuffer("FROM SupplierEntity m WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (StringUtil.isNotEmpty(supplierName)) {
            hql.append(" AND m.name LIKE ?");
            params.add("%" + supplierName + "%");
        }
        if (StringUtil.isNotEmpty(supplierId)) {
            hql.append(" AND m.supplierId = ?");
            params.add(NumberConvertUtil.parseInt(supplierId));
        }
        if (StringUtil.isNotEmpty(supplierType)) {
            hql.append(" AND m.supplierId IN(SELECT supplierId FROM MarketResourceEntity WHERE typeCode = ?)");
            params.add(Integer.parseInt(supplierType));
        }
        hql.append(" ORDER BY m.createTime DESC");
        Page page = this.page(hql.toString(), params, pageIndex, pageSize);
        return page;
    }

    public boolean supplierAccountDeductions(String supplierId, BigDecimal amount) throws Exception {
        logger.info("供应商:" + supplierId + "开始扣费,金额:" + amount);
        String sql = "SELECT * FROM t_supplier_property m where m.supplier_id=? and m.property_name=?";
        List<Map<String, Object>> list = this.sqlQuery(sql, supplierId, "remain_amount");

        // 查询是否有授信额度
        List<Map<String, Object>> st = this.sqlQuery(sql, supplierId, "settlement_type");
        int settlementType = 1;
        if (st != null && st.size() > 0 && st.get(0).get("property_value") != null) {
            settlementType = NumberConvertUtil.parseInt(String.valueOf(st.get(0).get("property_value")));
        }
        String remainAmount = null, creditAmount = null;
        // 查询授信额度
        if (2 == settlementType) {
            List<Map<String, Object>> credit = this.sqlQuery(sql, supplierId, "creditAmount");
            if (credit != null && credit.size() > 0) {
                creditAmount = String.valueOf(credit.get(0).get("property_value"));
            }
        }

        if (list.size() > 0) {
            remainAmount = String.valueOf(list.get(0).get("property_value"));
        }
        // 处理账户不存在
        if (remainAmount == null) {
            remainAmount = "0";
            logger.info("供应商:" + supplierId + "账户不存在开始新建账户信息");
            String insertSql = "INSERT INTO `t_supplier_property` (`supplier_id`, `property_name`, `property_value`, `create_time`) VALUES (?, ?, ?, ?);";
            int status = this.executeUpdateSQL(insertSql, supplierId, "remain_amount", remainAmount, new Timestamp(System.currentTimeMillis()));
            logger.info("供应商:" + supplierId + "账户创建结果:" + status);
        }
        logger.info("供应商:" + supplierId + "结算类型:" + settlementType + "授信额度:" + creditAmount);

        // 处理累计消费金额不存在
        List<Map<String, Object>> usedAmountList = this.sqlQuery(sql, supplierId, "used_amount");
        String usedAmount = null;
        if (usedAmountList.size() > 0) {
            usedAmount = String.valueOf(usedAmountList.get(0).get("property_value"));
        }

        if (usedAmount == null) {
            // 累计消费 处理账户不存在
            usedAmount = "0";
            logger.info("供应商:" + supplierId + "账户累计消费不存在开始新建账户信息");
            String insertSql = "INSERT INTO `t_supplier_property` (`supplier_id`, `property_name`, `property_value`, `create_time`) VALUES (?, ?, ?, ?);";
            int status = this.executeUpdateSQL(insertSql, supplierId, "used_amount", usedAmount, new Timestamp(System.currentTimeMillis()));
            logger.info("供应商:" + supplierId + "账户累计消费创建结果:" + status);

        }
        if (StringUtil.isNotEmpty(remainAmount)) {
            if (Double.parseDouble(remainAmount) <= 0) {
                logger.info("供应商:" + supplierId + "账户余额:" + remainAmount + ",先执行扣减");
                // 如果授信额度小于透支额度
                if (StringUtil.isNotEmpty(creditAmount) && NumberConvertUtil.changeY2L(NumberConvertUtil.parseDouble(creditAmount)) < Math.abs(NumberConvertUtil.parseDouble(remainAmount) + amount.doubleValue())) {
                    logger.warn("供应商:" + supplierId + "账户余额:" + remainAmount + "授信额度:" + creditAmount);
                }
            }
            DecimalFormat df = new DecimalFormat("0.00");
            BigDecimal remainAmountBigDecimal = new BigDecimal(remainAmount);
            String nowMoney = df.format(remainAmountBigDecimal.subtract(amount));
            String updateSql = "UPDATE t_supplier_property SET property_value = ? WHERE supplier_id = ? AND property_name = ?";
            this.executeUpdateSQL(updateSql, nowMoney, supplierId, "remain_amount");

            // 处理累计消费累加
            BigDecimal usedAmountBigDecimal = new BigDecimal(usedAmount);
            String usedAmountMoney = df.format(usedAmountBigDecimal.add(amount));
            this.executeUpdateSQL(updateSql, usedAmountMoney, supplierId, "used_amount");
            logger.info("供应商::" + supplierId + "扣减后的余额:" + nowMoney + "累计消费:" + usedAmountMoney);
            return true;
        }
        return false;
    }


}
