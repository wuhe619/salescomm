package com.bdaim.resource.dao;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.dao.SimpleHibernateDao;
import com.bdaim.common.util.ConstantsUtil;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.SqlAppendUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.customer.dao.CustomerLabelDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerUserDTO;
import com.bdaim.customer.entity.CustomerLabel;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customersea.dao.CustomerSeaDao;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.markettask.entity.MarketTask;
import com.bdaim.rbac.service.UserService;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.dto.VoiceLogQueryParam;
import com.bdaim.resource.entity.MarketResourceEntity;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.template.entity.MarketTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.*;

/**
 * 营销资源DAO服务
 */
@Component
public class MarketResourceDao extends SimpleHibernateDao<MarketResourceEntity, Integer> {

    @Autowired
    private CustomerLabelDao customerLabelDao;
    @Autowired
    private MarketTaskDao marketTaskDao;
    @Autowired
    private CustomerUserDao customerUserDao;
    @Autowired
    private CustomerSeaDao customerSeaDao;

    public String getResourceName(int resourceId) {
        String hql = "from MarketResourceEntity m where m.resourceId=?";
        List<MarketResourceEntity> list = this.find(hql, resourceId);
        if (list.size() > 0) {
            return list.get(0).getResname();
        }
        return null;
    }

    public MarketResourceEntity getMarketResource(int resourceId) {
        String hql = "from MarketResourceEntity m where m.resourceId=?";
        List<MarketResourceEntity> list = this.find(hql, resourceId);
        if (list.size() > 0) {
            return list.get(0);
        }
        return null;
    }

    public void updateMarketResourceStatus(Integer resourceId, int status) {
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
        List<ResourcePropertyEntity> list = this.find(hql, NumberConvertUtil.parseInt(resourceId), propertyName);
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
    public MarketResourceDTO getInfoProperty(int resourceId, String propertyName) {
        MarketResourceEntity mr = this.getMarketResource(resourceId);
        if (mr != null) {
            ResourcePropertyEntity mp = null;
            String hql = "from ResourcePropertyEntity m where m.resourceId=? and m.propertyName=?";
            List<ResourcePropertyEntity> list = this.find(hql, resourceId, propertyName);
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
        int code = this.batchExecute(hql, NumberConvertUtil.parseInt(resourceId), propertyName);
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

    /**
     * 批量更新人工审核状态
     *
     * @param touchIds
     * @param intentStatus
     * @param time
     * @param clueAuditReason
     * @return
     */
    public int batchVoiceIntentStatus(List<String> touchIds, int intentStatus, String time, String clueAuditReason) {
        StringBuilder sql = new StringBuilder();
        sql.append("update t_touch_voice_log_" + time + " SET clue_audit_status = ?,clue_audit_reason=? WHERE touch_id IN(" + SqlAppendUtil.sqlAppendWhereIn(touchIds) + ") ");
        return this.executeUpdateSQL(sql.toString(), intentStatus, clueAuditReason);
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

    /**
     * 根据检索条件批量审核通话记录
     *
     * @param param
     * @param yearMonth
     * @return
     */
    public int batchVoiceIntentStatus0(VoiceLogQueryParam param, String yearMonth) {
        int taskType = -1;
        String custProperty = param.getLabelProperty();
        String marketTaskId = param.getMarketTaskId();
        String realName = param.getRealName();
        String customerGroupId = param.getCustomerGroupId();

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

        // 处理自建属性搜索
        if (StringUtil.isNotEmpty(custProperty) && !"[]".equals(custProperty)) {
            Map<String, CustomerLabel> cacheLabel = new HashMap<>();
            String custId = "";
            if (StringUtil.isNotEmpty(param.getMarketTaskId())) {
                MarketTask marketTask = marketTaskDao.get(marketTaskId);
                if (marketTask != null && marketTask.getTaskType() != null) {
                    taskType = marketTask.getTaskType();
                    custId = marketTask.getCustId();
                    sql.append(" INNER JOIN " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " t2 ON t2.id = voicLog.superid ");
                }
            } else if (StringUtil.isNotEmpty(param.getSeaId())) {
                CustomerSea customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(param.getSeaId()));
                if (customerSea != null) {
                    taskType = customerSea.getTaskType();
                    custId = customerSea.getCustId();
                    sql.append(" INNER JOIN " + ConstantsUtil.SEA_TABLE_PREFIX + param.getSeaId() + " t2 ON t2.id = voicLog.superid ");
                }
            } else {
                logger.warn("批量审核只支持营销任务和公海");
                return 0;
            }
            if (StringUtil.isEmpty(custId)) {
                logger.warn("营销任务:{}或公海:{},客户ID为空", marketTaskId, param.getSeaId());
                return 0;
            }
            // 查询所有自建属性
            List<CustomerLabel> customerLabels = customerLabelDao.listCustomerLabel(custId);
            for (CustomerLabel c : customerLabels) {
                cacheLabel.put(c.getLabelId(), c);
            }
            JSONObject jsonObject;
            String labelId, optionValue, likeValue;
            JSONArray jsonArray = JSON.parseArray(custProperty);
            for (int i = 0; i < jsonArray.size(); i++) {
                jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject != null) {
                    labelId = jsonObject.getString("labelId");
                    optionValue = jsonObject.getString("optionValue");
                    // 文本和多选支持模糊搜索
                    if (cacheLabel.get(labelId) != null && cacheLabel.get(labelId).getType() != null
                            && (cacheLabel.get(labelId).getType() == 1 || cacheLabel.get(labelId).getType() == 3)) {
                        likeValue = "%\"" + labelId + "\":\"%" + optionValue + "%";
                    } else {
                        likeValue = "%\"" + labelId + "\":\"" + optionValue + "\"%";
                    }
                    sql.append(" AND t2.super_data LIKE '" + likeValue + "' ");
                }
            }
        }

        if ("-1".equals(param.getCustId())) {
            sql.append(" AND voicLog.cust_id IS NOT NULL ");
        } else {
            sql.append(" AND voicLog.cust_id='" + param.getCustId() + "'");
        }
        // 处理根据登陆账号或者用户姓名搜索
        CustomerUser user = null;
        if (StringUtil.isNotEmpty(realName)) {
            user = this.customerUserDao.getCustomerUserByName(realName.trim());
            if (user != null) {
                sql.append(" AND voicLog.user_id = '" + user.getId() + "'");
            } else {
                // 穿透查询一次登陆名称
                user = this.customerUserDao.getCustomerUserByLoginName(realName.trim());
                if (user != null) {
                    sql.append(" AND  voicLog.user_id = '" + user.getId() + "'");
                } else {
                    return 0;
                }
            }
        }
        if (StringUtil.isNotEmpty(customerGroupId)) {
            sql.append(" AND voicLog.customer_group_id =" + customerGroupId.trim());
        }
        if (StringUtil.isNotEmpty(marketTaskId)) {
            sql.append(" AND voicLog.market_task_id='" + marketTaskId.trim() + "'");
        }
        // 根据公海ID查询通话记录
        if (StringUtil.isNotEmpty(param.getSeaId())) {
            sql.append(" AND voicLog.customer_sea_id = '" + param.getSeaId() + "'");
        }
        String createTimeStart = param.getCreateTimeStart();
        String createTimeEnd = param.getCreateTimeEnd();
        // 处理开始和结束数据搜索
        if (StringUtil.isNotEmpty(createTimeStart) && StringUtil.isNotEmpty(createTimeEnd)) {
            sql.append(" AND voicLog.create_time BETWEEN '" + createTimeStart + "' and '" + createTimeEnd + "' ");
        } else {
            if (StringUtil.isNotEmpty(createTimeStart)) {
                sql.append(" AND voicLog.create_time > '" + createTimeStart + "'");
            }
            if (StringUtil.isNotEmpty(createTimeEnd)) {
                sql.append(" AND voicLog.create_time < '" + createTimeEnd + "'");
            }
        }
        int calledDuration = 0;
        if (param.getCalledDuration() != null) {
            calledDuration = param.getCalledDuration().intValue();
        }
        //type 0 查詢全部   1查詢<=3  2、3s-6s 3.6s-12s  4.12s-30s 5.30s-60s 6.>60s
        if (calledDuration == 1) {
            sql.append(" AND voicLog.called_duration<=3");
        } else if (calledDuration == 2) {
            sql.append(" AND voicLog.called_duration>3 AND voicLog.called_duration<=6");
        } else if (calledDuration == 3) {
            sql.append(" AND voicLog.called_duration>6 AND voicLog.called_duration<=12");
        } else if (calledDuration == 4) {
            sql.append(" AND voicLog.called_duration>12 AND voicLog.called_duration<=30");
        } else if (calledDuration == 5) {
            sql.append(" AND voicLog.called_duration>30 AND voicLog.called_duration<=60");
        } else if (calledDuration == 6) {
            sql.append(" AND voicLog.called_duration>60");
        }
        // 处理机器人外呼任务营销记录
        if (3 == taskType) {
            // 处理按照操作人搜索营销记录时机器人外呼任务记录可以搜到
            if (user != null) {
                sql.append(" AND (voicLog.user_id = '" + user.getId() + "' OR voicLog.call_data LIKE '%level%')");
            }
            // 处理人工审核搜索条件
            if (StringUtil.isNotEmpty(param.getAuditingStatus())) {
                sql.append(" AND voicLog.clue_audit_status = " + param.getAuditingStatus());
            }
        }
        // 处理组长权限
        if (UserService.OPERATOR_USER_TYPE.equals(param.getUserType())) {
            // 组长查组员列表
            if ("1".equals(param.getUserGroupRole())) {
                List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(param.getUserGroupId(), param.getCustId());
                // 处理组长下有员工的情况
                if (customerUserDTOList.size() > 0) {
                    Set<String> userIds = new HashSet<>();
                    for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                        userIds.add(customerUserDTO.getId());
                    }
                    if (userIds.size() > 0) {
                        if (3 == taskType) {
                            sql.append(" AND (voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR voicLog.call_data LIKE '%level%')");
                        } else {
                            sql.append(" AND voicLog.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }
                    }
                } else {
                    // 处理组长下没有员工的情况,只查询自己的通话记录
                    if (3 == taskType) {
                        sql.append(" AND (voicLog.user_id = '" + param.getUserId() + "' OR voicLog.call_data LIKE '%level%')");
                    } else {
                        sql.append(" AND voicLog.user_id = '" + param.getUserId() + "'");
                    }
                }
            } else {
                if (3 == taskType) {
                    sql.append(" AND (voicLog.user_id = '" + param.getUserId() + "' OR voicLog.call_data LIKE '%level%')");
                } else {
                    sql.append(" AND voicLog.user_id = '" + param.getUserId() + "'");
                }
            }
        }
        return this.executeUpdateSQL(sql.toString(), param.getIntentStatus(), param.getCustomerGroupId(), param.getMarketTaskId(), param.getStartTime(), param.getEndTime());
    }
}

