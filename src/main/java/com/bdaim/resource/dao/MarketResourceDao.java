package com.bdaim.resource.dao;

import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.SqlAppendUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.dto.VoiceLogQueryParam;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.template.entity.MarketTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 营销资源DAO服务
 */
@Component
public class MarketResourceDao extends SimpleHibernateDao<Object, Integer> {
    public String getResourceName(Long resourceId) {
        String hql = "from MarketResourceEntity m where m.resourceId=?";
        List<MarketResourceEntity> list = this.find(hql, resourceId);
        if (list.size() > 0) {
            return list.get(0).getResname();
        }
        return null;
    }

    public MarketResourceEntity getMarketResource(Long resourceId) {
        String hql = "from MarketResourceEntity m where m.resourceId=?";
        List<MarketResourceEntity> list = this.find(hql, resourceId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public void updateMarketResourceStatus(Long resourceId, int status) {
        String hql = "UPDATE MarketResourceEntity SET status = ? where resourceId=?";
        this.batchExecute(hql, status, resourceId);
    }

    public List<Map<String, Object>> queryMarketResource(String cust_id, String sql) {
        Object[] objs = {cust_id};
        //String sql = "select tt.type_code type,tt.resname name, t.remain quantity from t_resource_account t left join t_market_resource tt on t.resource_id=tt.resource_id  where cust_id=?";
        List<Map<String, Object>> groupConfigList = this.sqlQuery(sql, objs);
        return groupConfigList;
    }

    //	public List<Map<String, Object>> queryMarketResourceDetail(String sql, String groupId, String superId,
//			String pageNum, String pageSize) {
//		Object[] objs = { cust_id };
//		//String sql = "select tt.type_code type,tt.resname name, t.remain quantity from t_resource_account t left join t_market_resource tt on t.resource_id=tt.resource_id  where cust_id=?";
//		List<Map<String, Object>> groupConfigList = jdbcTemplate.queryForList(sql, objs);
//		return groupConfigList;
////		return null;
//	}
    public void insertLog(String sql) {
        this.executeUpdateSQL(sql);
    }

    /**
     * 查询资源使用量表
     */
    public int queryResource(String sql, String cust_id, String resource_id) {
        int count = 0;
        Object[] objs = {cust_id, resource_id};
        List<Map<String, Object>> list = this.sqlQuery(sql, objs);
        if (list.size() == 0) {
            return count;
        }
        count = Integer.parseInt(list.get(0).get("remain").toString());
        return count;
    }

    /**
     * 资源使用量表 插入客户购买信息
     */
    public void insertResource(String sql, Object[] objs) {
        this.executeUpdateSQL(sql, objs);
    }

    /**
     * 资源使用量表 更新客户购买信息
     */
    public void updateResource(String sql, Object[] objs) {
        this.executeUpdateSQL(sql, objs);
    }

    /**
     * 为订单表增加订单
     */
    public void insertOrder(String sql, Object[] objs) {
        this.executeUpdateSQL(sql, objs);
    }

    /**
     * 根据模板ID和资源类型获取营销模板
     *
     * @param templateId 模板ID
     * @param typeCode   资源类型（ 1.SMS 2.email 3.闪信）
     * @param custId
     * @return
     */
    public MarketTemplate getMarketTemplate(int templateId, int typeCode, String custId) {
        List<MarketTemplate> list = this.find("FROM MarketTemplate t WHERE t.id = ? AND t.typeCode = ? AND t.custId = ?", templateId, typeCode, custId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public MarketTemplate getMarketTemplate(String supplierTemplateCode, String custId) {
        List<MarketTemplate> list = this.find("FROM MarketTemplate t WHERE t.templateCode = ? AND t.custId = ?", supplierTemplateCode, custId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    /**
     * 根据模板ID查询模板
     *
     * @param templateId
     * @return
     */
    public MarketTemplate getMarketTemplate(int templateId) {
        List<MarketTemplate> list = this.find("FROM MarketTemplate t WHERE t.id = ?", templateId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public Page pageMarketTemplate(int pageNum, int pageSize, String templateName, String templateId, String custName, String status, String type, String custId,String marketProjectId) {
        StringBuffer hql = new StringBuffer();
        hql.append("FROM MarketTemplate t WHERE 1=1 ");
        List<Object> params = new ArrayList<>();
        if (StringUtil.isNotEmpty(type)) {
            hql.append(" AND t.typeCode = ? ");
            params.add(Integer.parseInt(type));
        }
        if (StringUtil.isNotEmpty(custId)) {
            hql.append(" AND t.custId = ? ");
            params.add(custId);
        }
        if (StringUtil.isNotEmpty(marketProjectId)) {
            hql.append(" AND t.marketProjectId = ? ");
            params.add(Integer.parseInt(marketProjectId));
        }
        if (StringUtil.isNotEmpty(templateName)) {
            hql.append(" AND t.title LIKE ?");
            params.add("%" + templateName + "%");
        }
        if (StringUtil.isNotEmpty(templateId)) {
            hql.append(" AND t.id = ?");
            params.add(Integer.parseInt(templateId));
        }
        if (StringUtil.isNotEmpty(custName)) {
            hql.append(" AND t.custId IN(SELECT custId from Customer where enterpriseName LIKE ? )");
            params.add("%" + custName + "%");
        }
        if (StringUtil.isNotEmpty(status)) {
            hql.append(" AND t.status = ?");
            params.add(Integer.parseInt(status));
        }
        hql.append(" ORDER BY t.createTime DESC");
        Page page = this.page(hql.toString(), params, pageNum, pageSize);
        return page;
    }

    /**
     * 获取资源的属性
     *
     * @param resourceId
     * @param propertyName
     * @return
     */
    public ResourcePropertyEntity getProperty(String resourceId, String propertyName) {
        ResourcePropertyEntity mp = null;
        String hql = "from ResourcePropertyEntity m where m.resourceId=? and m.propertyName=?";
        List<ResourcePropertyEntity> list = this.find(hql, resourceId, propertyName);
        if (list.size() > 0) {
            mp = list.get(0);
        }
        return mp;
    }

    /**
     * 获取资源的属性
     *
     * @param resourceId
     * @param propertyName
     * @return
     */
    public MarketResourceDTO getInfoProperty(long resourceId, String propertyName) {
        MarketResourceEntity mr = this.getMarketResource(resourceId);
        if (mr != null) {
            ResourcePropertyEntity mp = null;
            String hql = "from ResourcePropertyEntity m where m.resourceId=? and m.propertyName=?";
            List<ResourcePropertyEntity> list = this.find(hql, String.valueOf(resourceId), propertyName);
            if (list.size() > 0) {
                mp = list.get(0);
                MarketResourceDTO dto = new MarketResourceDTO(mr);
                dto.setResourceProperty(String.valueOf(mp.getPropertyValue()));
                return dto;
            }
        }
        return null;
    }

    /**
     * 删除资源属性
     *
     * @param resourceId
     * @param propertyName
     * @return
     */
    public int deleteProperty(String resourceId, String propertyName) {
        String hql = "Delete FROM MarketResourceEntity m where m.resourceId=? and m.propertyName=?";
        int code = this.batchExecute(hql, resourceId, propertyName);
        return code;
    }

    /**
     * 查询供应商下的所有营销资源列表
     *
     * @param supplierId
     * @param type
     * @return
     */
    public List<MarketResourceDTO> listMarketResourceBySupplierIdAndType(String supplierId, int type) {
        String hql = "from MarketResourceEntity m where m.supplierId=? and m.typeCode=? AND m.status = 1 ORDER BY create_time ASC ";
        List<MarketResourceEntity> list = this.find(hql, supplierId, type);
        List<MarketResourceDTO> result = new ArrayList<>();
        if (list.size() > 0) {
            MarketResourceDTO marketResourceDTO;
            ResourcePropertyEntity property;
            for (int i = 0; i < list.size(); i++) {
                marketResourceDTO = new MarketResourceDTO(list.get(i));
                if (marketResourceDTO != null && marketResourceDTO.getResourceId() != null) {
                    property = getProperty(String.valueOf(marketResourceDTO.getResourceId()), "price_config");
                    marketResourceDTO.setResourceProperty(property != null ? property.getPropertyValue() : "{}");
                }
                result.add(marketResourceDTO);
            }
        }
        return result;
    }

    public List<MarketResourceDTO> listMarketResourceBySupplierId(String supplierId) {
        String hql = "from MarketResourceEntity m where m.supplierId=? AND m.status = 1 ORDER BY create_time ASC ";
        List<MarketResourceEntity> list = this.find(hql, supplierId);
        List<MarketResourceDTO> result = new ArrayList<>();
        if (list.size() > 0) {
            MarketResourceDTO marketResourceDTO;
            for (int i = 0; i < list.size(); i++) {
                marketResourceDTO = new MarketResourceDTO(list.get(i));
                result.add(marketResourceDTO);
            }
        }
        return result;
    }

    public List<MarketResourceDTO> listMarketResource(String type) {
        StringBuilder hql = new StringBuilder();
        hql.append(" from MarketResourceEntity m where 1=1");
        if (StringUtil.isNotEmpty(type)) {
            hql.append(" AND m.typeCode = " + type);
        }
        hql.append(" ORDER BY create_time ASC");
        List<MarketResourceEntity> list = this.find(hql.toString());
        List<MarketResourceDTO> result = new ArrayList<>();
        if (list.size() > 0) {
            MarketResourceDTO marketResourceDTO;
            for (int i = 0; i < list.size(); i++) {
                marketResourceDTO = new MarketResourceDTO(list.get(i));
                result.add(marketResourceDTO);
            }
        }
        return result;
    }

    /**
     * 检查通话记录月表是否存在,不存在则创建月表
     *
     * @param nowYearMonth
     * @return
     */
    public int createVoiceLogTableNotExist(String nowYearMonth) {
        /*StringBuffer sql = new StringBuffer();
        // 创建通话记录年月分表
        sql.append("CREATE TABLE IF NOT EXISTS ");
        sql.append(ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX);
        sql.append(nowYearMonth);
        sql.append(" LIKE t_touch_voice_log_new ");
        logger.info("通话记录月表创建语句:" + sql.toString());
        return this.executeUpdateSQL(sql.toString());*/
        return 1;
    }

    /**
     * 批量更新人工审核状态
     *
     * @param touchIds
     * @param intentStatus
     * @param time
     * @return
     */
    public int batchVoiceIntentStatus(List<String> touchIds, int intentStatus, String time) {
        StringBuilder sql = new StringBuilder();
        sql.append("update t_touch_voice_log_" + time + " SET clue_audit_status = ? WHERE touch_id IN(" + SqlAppendUtil.sqlAppendWhereIn(touchIds) + ") ");
        return this.executeUpdateSQL(sql.toString(), intentStatus);
    }

    public int batchVoiceIntentStatus(VoiceLogQueryParam param, String yearMonth) {
        StringBuilder sql = new StringBuilder();
        sql.append("update t_touch_voice_log_" + yearMonth + " SET clue_audit_status = ? WHERE customer_group_id = ? AND market_task_id = ? AND create_time BETWEEN ? AND ? ");
        String levelLike = "\"level\":\"" + param.getIntentLevel() + "\"";
        sql.append(" AND call_data LIKE '%" + levelLike + "%'");

        if (StringUtil.isNotEmpty(param.getSuperId())) {
            sql.append(" AND superid = '" + param.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getRemark())) {
            sql.append(" AND remark = '" + param.getRemark() + "'");
        }
        if (StringUtil.isNotEmpty(param.getUserId())) {
            sql.append(" AND user_id = '" + param.getUserId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getCallStatus())) {
            sql.append(" AND status = " + param.getCallStatus());
        }
        return this.executeUpdateSQL(sql.toString(), param.getIntentStatus(), param.getCustomerGroupId(), param.getMarketTaskId(), param.getStartTime(), param.getEndTime());
    }
}

