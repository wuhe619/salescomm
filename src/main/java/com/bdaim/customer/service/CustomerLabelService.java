package com.bdaim.customer.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerLabelDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerLabelDTO;
import com.bdaim.customer.entity.CustomerLabel;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.customgroup.entity.CustomerGroupProperty;
import com.bdaim.marketproject.dao.MarketProjectDao;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.markettask.entity.MarketTaskProperty;
import com.bdaim.util.*;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

@Service("/customerLabelService")
@Transactional
public class CustomerLabelService {

    public static final Logger log = LoggerFactory.getLogger(CustomerLabelService.class);

    @Autowired
    private CustomerDao customerDao;

    @Resource
    private MarketProjectDao marketProjectDao;

    @Resource
    private MarketTaskDao marketTaskDao;

    @Resource
    private CustomGroupDao customGroupDao;

    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomerLabelDao customerLabelDao;

    /**
     * 查询客户所有自建属性和系统自建属性
     *
     * @param custId
     * @return
     */
    public Map<Object, Object> getCustomAndSystemLabel(String custId) {
        Map<Object, Object> singleLabel = new HashMap<>();
        // 查询客户下的自建属性
        List<CustomerLabel> labelNames = customerLabelDao.listCustomerLabel(custId);
        for (CustomerLabel c : labelNames) {
            singleLabel.put(c.getLabelId(), c.getLabelName());
        }
        //查询系统自建属性
        labelNames = customerLabelDao.listSystemCustomerLabel();
        for (CustomerLabel c : labelNames) {
            singleLabel.put(c.getLabelId(), c.getLabelName());
        }
        return singleLabel;
    }


    /**
     * 查询客户下已选属性
     *
     * @param custId
     * @return
     */
    public Map<String, CustomerLabel> getCustomLabel(String custId) {
        Map<String, CustomerLabel> singleLabel = new HashMap<>();
        // 查询客户下的自建属性
        List<CustomerLabel> labelNames = customerLabelDao.listCustomerLabel(custId);
        for (CustomerLabel c : labelNames) {
            singleLabel.put(c.getLabelId(), c);
        }
        return singleLabel;
    }


    /**
     * 查询客户所有自建属性和系统自建属性
     *
     * @param custId
     * @return
     */
    public Map<String, CustomerLabel> getCacheCustomAndSystemLabel(String custId) {
        List<CustomerLabel> customerLabels = listCustomAndSystemLabel(custId);
        Map<String, CustomerLabel> cacheLabel = new HashMap<>();
        for (CustomerLabel c : customerLabels) {
            cacheLabel.put(c.getLabelId(), c);
        }
        return cacheLabel;
    }

    /**
     * 获取客户下和系统级别的自建属性
     *
     * @param custId
     * @return
     */
    public List<CustomerLabel> listCustomAndSystemLabel(String custId) {
        List<CustomerLabel> labelList = new ArrayList<>();
        // 查询客户下的自建属性
        List<CustomerLabel> labelNames = customerLabelDao.listCustomerLabel(custId);
        if (labelNames != null && labelNames.size() > 0) {
            labelList.addAll(labelNames);
        }
        //查询系统自建属性
        labelNames = customerLabelDao.listSystemCustomerLabel();
        if (labelNames != null && labelNames.size() > 0) {
            labelList.addAll(labelNames);
        }
        return labelList;
    }


    /**
     * 更新自建属性
     *
     * @param customerLabel
     * @return
     */
    public int modifyCustomLabel(CustomerLabel customerLabel) {
        int code = 0;
        try {
            CustomerLabel dbCstomerLabel = customerLabelDao.getCustomerLabel(customerLabel.getLabelId());
            if (dbCstomerLabel != null && customerLabel.getCustId().equals(dbCstomerLabel.getCustId())) {
                dbCstomerLabel.setType(customerLabel.getType());
                dbCstomerLabel.setOption(customerLabel.getOption());
                dbCstomerLabel.setLabelName(customerLabel.getLabelName());
                dbCstomerLabel.setLabelDesc(customerLabel.getLabelDesc());
                dbCstomerLabel.setLabelId(customerLabel.getLabelId());
                dbCstomerLabel.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                dbCstomerLabel.setMarketProjectId(customerLabel.getMarketProjectId());
                customerDao.saveOrUpdate(dbCstomerLabel);
                code = 1;
            } else {
                log.warn("更新自建属性为空或权限不足," + customerLabel);
            }
        } catch (Exception e) {
            log.error("更新自建属性异常,", e);
            code = 0;
        }

        return code;
    }

    /**
     * 自建属性修改
     *
     * @param customerLabel
     * @return
     */
    public int modifyCustomLabel0(CustomerLabel customerLabel) {
        int code = 0;
        List<CustomerLabel> labelList = customerLabelDao.listCustomerLabel(customerLabel.getLabelId(), 3);
        List<String> addProjects = new ArrayList<>(), updateProjects = new ArrayList<>();
        List<String> dbProjects = new ArrayList<>();
        for (CustomerLabel c : labelList) {
            dbProjects.add(c.getMarketProjectId());
        }
        String[] projectIds = customerLabel.getMarketProjectId().split(",");
        for (String p : projectIds) {
            if (dbProjects.contains(p)) {
                updateProjects.add(p);
            } else {
                addProjects.add(p);
            }
        }
        // 增量添加
        CustomerLabel label;
        for (String p : addProjects) {
            label = new CustomerLabel();
            label.setLabelId(customerLabel.getLabelId());
            label.setCreateTime(new Timestamp(System.currentTimeMillis()));
            label.setOption(customerLabel.getOption());
            label.setMarketProjectId(p);
            label.setType(customerLabel.getType());
            label.setRequired(customerLabel.getRequired());
            label.setSort(customerLabel.getSort());
            label.setLabelName(customerLabel.getLabelName());
            label.setCustId(customerLabel.getCustId());
            label.setStatus("1");
            customerDao.saveOrUpdate(label);
        }
        // 修改已经存在的
        for (String p : updateProjects) {
            CustomerLabel dbLabel = customerLabelDao.getCustomerLabel(customerLabel.getLabelId(), p, 3);
            dbLabel.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            dbLabel.setOption(customerLabel.getOption());
            dbLabel.setType(customerLabel.getType());
            dbLabel.setRequired(customerLabel.getRequired());
            dbLabel.setSort(customerLabel.getSort());
            dbLabel.setLabelName(customerLabel.getLabelName());
            customerDao.saveOrUpdate(dbLabel);
        }
        // 删除取消的自建属性
        dbProjects.removeAll(updateProjects);
        CustomerLabel dbLabel;
        for (String p : dbProjects) {
            dbLabel = customerLabelDao.getCustomerLabel(customerLabel.getLabelId(), p, 3);
            dbLabel.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            dbLabel.setStatus("3");
            customerDao.saveOrUpdate(dbLabel);
        }
        code = 1;

        return code;
    }


    public String getSelLabel(String superId, Integer custGroupId) {
        Map<String, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        StringBuffer sb = new StringBuffer();
        sb.append("  SELECT  CAST(t1.label_id AS CHAR) label_id FROM t_super_label t1")
                .append("  LEFT JOIN t_customer_label t2")
                .append("  ON t1.label_id = t2.label_id")
                .append("  WHERE 1=1  AND t2.status =1")
                .append("  and  t1.super_id  = ? ")
                .append("  AND  t1.cust_group_id = ?");
        List list = this.customerDao.sqlQuery(sb.toString(), superId, custGroupId);

        map.put("selLabel", list);
        json.put("selLabelJson", map);
        return json.toJSONString();
    }


    public String getSelLabel0(String superId, Integer custGroupId) {
        Map<String, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        StringBuffer sb = new StringBuffer();
        sb.append("  SELECT  CAST(t1.label_id AS CHAR) label_id, type FROM t_super_label t1")
                .append("  LEFT JOIN t_customer_label t2")
                .append("  ON t1.label_id = t2.label_id AND t2.status =1")
                .append("  WHERE 1=1 ")
                .append("  and  t1.super_id  = ? ")
                .append("  AND  t1.cust_group_id = ?");
        List<Map<String, Object>> list = this.customerDao.sqlQuery(sb.toString(), superId, custGroupId);
        // 处理自定义属性和标签的对应关系
        String superLabelSql = "SELECT option_value FROM t_super_label WHERE label_id = ? AND super_id = ? AND cust_group_id = ? ";
        List<Map<String, Object>> superLabelOptionList;
        Map<String, Object> labelMap;
        for (Map<String, Object> superLabel : list) {
            if (superLabel.get("label_id") != null) {
                labelMap = new HashMap<>();
                superLabelOptionList = this.customerDao.sqlQuery(superLabelSql, superLabel.get("label_id"), superId, custGroupId);
                if (superLabelOptionList.size() > 0 && superLabelOptionList.get(0).get("option_value") != null) {
                    //文本框不拆分为数组
                    if ("1".equals(String.valueOf(superLabel.get("type")))) {
                        superLabel.put("optionValue", superLabelOptionList.get(0).get("option_value"));
                    } else {
                        superLabel.put("optionValue", String.valueOf(superLabelOptionList.get(0).get("option_value")).split(","));
                    }
                } else {
                    if ("1".equals(String.valueOf(superLabel.get("type")))) {
                        superLabel.put("optionValue", "");
                    } else {
                        superLabel.put("optionValue", new ArrayList<>());
                    }
                }
            }
        }
        map.put("selLabel", list);
        json.put("selLabelJson", map);
        return json.toJSONString();
    }

    public String getSelLabelV3(String superId, Integer custGroupId, String marketTaskId) {
        log.info("superId=" + superId + ",custgroupId=" + custGroupId + ",marketTaskId=" + marketTaskId);
        Map<String, Object> map = new HashMap<>();
        JSONObject json = new JSONObject();
        StringBuffer sb = new StringBuffer();
        sb.append(" SELECT super_data");
        if (StringUtil.isEmpty(marketTaskId)) {
            sb.append(" FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + custGroupId + " custG ");
        } else {
            sb.append(" FROM " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " custG ");
        }
        sb.append(" WHERE custG.id = ? ");

        List<Map<String, Object>> list = this.customerDao.sqlQuery(sb.toString(), superId);
        if (list.size() > 0 && list.get(0) != null && !list.get(0).isEmpty()) {
            List<Map<String, Object>> selectLabelList = new ArrayList<>();
            Map<String, Object> selectLabelMap;
            // 查询自建属性主数据
            String customerLabelSql = "SELECT type FROM t_customer_label WHERE status = 1 AND label_id = ? ";
            List<Map<String, Object>> customerLabelList;
            Map<String, Object> labelDataMap;
            for (Map<String, Object> m : list) {
                if (m != null && m.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(m.get("super_data")))) {
                    labelDataMap = JSON.parseObject(String.valueOf(m.get("super_data")));
                    if (labelDataMap != null && labelDataMap.size() > 0) {
                        for (Map.Entry<String, Object> labelDataKey : labelDataMap.entrySet()) {
                            customerLabelList = this.customerDao.sqlQuery(customerLabelSql, labelDataKey.getKey());
                            if (customerLabelList.size() > 0) {
                                selectLabelMap = new HashMap<>();
                                selectLabelMap.put("label_id", labelDataKey.getKey());
                                selectLabelMap.put("type", customerLabelList.get(0).get("type"));

                                if (customerLabelList.get(0).get("type") != null && labelDataKey.getValue() != null) {
                                    if ("1".equals(String.valueOf(customerLabelList.get(0).get("type")))) {
                                        selectLabelMap.put("optionValue", labelDataKey.getValue());
                                    } else {
                                        selectLabelMap.put("optionValue", String.valueOf(labelDataKey.getValue()).split(","));
                                    }
                                } else {
                                    selectLabelMap.put("optionValue", labelDataKey.getValue());
                                }
                                selectLabelList.add(selectLabelMap);
                            }
                        }
                    }
                }
            }
            map.put("selLabel", selectLabelList);
        } else {
            map.put("selLabel", new ArrayList<>());
        }

        json.put("selLabelJson", map);
        return json.toJSONString();
    }

    /**
     * 添加自建属性
     *
     * @param custId
     * @param userId
     * @param labelId
     * @param labelName
     * @param labelDesc
     * @param type
     * @param option
     * @param marketProjectId 所属项目 多个用逗号隔开
     * @param sort            排序
     * @param required        是否必填
     * @return
     */
    public boolean insertCustomLabel_V1(String custId, Long userId, String labelId, String labelName, String labelDesc, Integer type, String option, String marketProjectId, String sort, String required) {
        try {
            String sql = " INSERT INTO t_customer_label(cust_id, user_id, label_id, label_name, create_time, label_desc, status, type, `option`, market_project_id, sort, required)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            this.customerDao.executeUpdateSQL(sql, custId, userId, labelId, labelName, new Timestamp(System.currentTimeMillis()), labelDesc, 1, type, option, marketProjectId, sort, required);
            log.info("插入客户自建标签表sql:" + sql);
        } catch (Exception e) {
            log.error("插入客户自建标签表出错", e);
            return false;
        }
        return true;
    }

    /**
     * 保存自建属性
     *
     * @param custId
     * @param userId
     * @param labelId
     * @param labelName
     * @param labelDesc
     * @param type
     * @param option
     * @param marketProjectIds
     * @param sort
     * @param required
     * @return
     */
    public boolean insertCustomLabel0(String custId, Long userId, String labelId, String labelName, String labelDesc, Integer type, String option, String[] marketProjectIds, Integer sort, String required) {
        try {
            String sql = " INSERT INTO t_customer_label(cust_id, user_id, label_id, label_name, create_time, label_desc, status, type, `option`, market_project_id, sort, required)VALUES(?,?,?,?,?,?,?,?,?,?,?,?)";
            for (String pId : marketProjectIds) {
                this.customerDao.executeUpdateSQL(sql, custId, userId, labelId, labelName, new Timestamp(System.currentTimeMillis()), labelDesc, 1, type, option, pId, sort, required);
            }
            log.info("插入客户自建标签表sql:" + sql);
        } catch (Exception e) {
            log.error("插入客户自建标签表出错", e);
            return false;
        }
        return true;
    }

    public boolean updateCustomLabel_V1(Long userId, String labelId, String labelName, String labelDesc, Integer type, String option) {
        try {
            CustomerLabel customerLabel = customerLabelDao.getCustomerLabel(labelId);
            customerLabel.setLabelDesc(labelDesc);
            customerLabel.setLabelName(labelName);
            customerLabel.setOption(option);
            customerLabel.setType(type);
            this.customerLabelDao.updateCustomerLabel(customerLabel);
        } catch (Exception e) {
            log.error("更新客户自建标签表出错", e);
            return false;
        }
        return true;
    }

    public boolean inserCustomlabel(String custId, Long userId, String labelId, String labelName, String labelDesc) {
        try {
            String sql = " INSERT INTO t_customer_label(cust_id, user_id, label_id, label_name, create_time, label_desc, status)VALUES(?,?,?,?,NOW(),?,1)";
            this.customerDao.executeUpdateSQL(sql, custId, userId, labelId, labelName, labelDesc);
            log.info("插入客户自建标签表sql:" + sql);
        } catch (Exception e) {
            log.error("插入客户自建标签表出错", e);
            return false;
        }
        return true;
    }


    public boolean insertSuperLable(String id, String superId, String labelId, String groupId) {
        try {
            String sql = "  INSERT INTO t_super_label(id,super_id,cust_group_id,label_id,create_time)VALUES(?,?,?,?,NOW())";
            this.customerDao.executeUpdateSQL(sql, id, superId, groupId, labelId);
            log.info("插入客户购买资源用户标签表sql:" + sql);
        } catch (Exception e) {
            log.error("插入客户购买资源用户标签表出错", e);
        }
        return true;
    }

    /**
     * @param superId
     * @param custGroupId
     * @param taskId
     * @param userId
     * @param labelData
     */
    public void saveSuperDataLog(String superId, String custGroupId, String taskId, String userId,
                                 Map<String, Object> labelData, String customerSeaId) {
        log.info("save custom lable to t_supperdata_log ");
        Thread thread = new Thread(new SupperDataLog(superId, custGroupId, taskId, userId,
                labelData, customerSeaId));
        thread.start();
    }

    public int updateDisplayStatus(CustomerLabel customerLabel) {
        int code = 0;
        String isShow = customerLabel.getIsShow();
        try {
            CustomerLabel dbCstomerLabel = customerLabelDao.getCustomerLabel(customerLabel.getLabelId());
            log.info("当前自建属性 {}", JSON.toJSONString(dbCstomerLabel));
            if (dbCstomerLabel != null) {
                dbCstomerLabel.setIsShow(isShow);
                this.customerDao.saveOrUpdate(dbCstomerLabel);
                code = 1;
            }
        } catch (Exception e) {
            log.error("修改显示状态异常,", e);
            code = 0;
        }

        return code;
    }

    public int updateSelectedStatus(String customerGroupId, String marketTaskId, String selectedLabels) {
        if (StringUtil.isNotEmpty(customerGroupId)) {
            //查询客群对应选中的自建属性 saveOrUpdate()方法
            CustomerGroupProperty groupProperty = new CustomerGroupProperty();
            groupProperty.setCustomerGroupId(Integer.parseInt(customerGroupId));
            groupProperty.setPropertyName("selectedLabels");
            if (StringUtil.isEmpty(selectedLabels)) {
                groupProperty.setPropertyValue(JSON.toJSONString(new ArrayList<>()));
            } else {
                String[] values = selectedLabels.split(",");
                groupProperty.setPropertyValue(JSON.toJSONString(values));
            }
            groupProperty.setCreateTime(new Timestamp(System.currentTimeMillis()));
            customGroupDao.saveOrUpdate(groupProperty);
        } else if (StringUtil.isNotEmpty(marketTaskId)) {
            //查询任务对应选中的自建属性
            MarketTaskProperty taskProperty = new MarketTaskProperty();
            taskProperty.setMarketTaskId(marketTaskId);
            taskProperty.setPropertyName("selectedLabels");
            if (StringUtil.isEmpty(selectedLabels)) {
                taskProperty.setPropertyValue(JSON.toJSONString(new ArrayList<>()));
            } else {
                String[] values = selectedLabels.split(",");
                taskProperty.setPropertyValue(JSON.toJSONString(values));
            }
            taskProperty.setCreateTime(new Timestamp(System.currentTimeMillis()));
            customGroupDao.saveOrUpdate(taskProperty);
        }
        return 1;
    }

    class SupperDataLog implements Runnable {

        String superId;
        String custGroupId;
        String marketTaskId;
        String userId;
        Map<String, Object> superData;
        String customerSeaId;

        public SupperDataLog(String superId, String custGroupId, String marketTaskId, String userId,
                             Map<String, Object> superData, String customerSeaId) {
            this.superId = superId;
            this.custGroupId = custGroupId;
            this.marketTaskId = marketTaskId;
            this.userId = userId;
            this.superData = superData;
            this.customerSeaId = customerSeaId;
        }

        @Override
        public void run() {
            log.info("另外起线程处理记录表。。。");
            insertSuperInfoLog();
        }

        public void insertSuperInfoLog() {
            JSONObject json = new JSONObject();
            StringBuffer sql2 = new StringBuffer();
            String tableName = ConstantsUtil.SUPPERDATA_LOG_TABLE_PREFIX + LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
            sql2.append(" create table IF NOT EXISTS ").append(tableName).append(" like t_supperdata_log ");
            customerDao.executeUpdateSQL(sql2.toString());

            String sql = "select custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone," +
                    " custG.super_address_province_city, custG.super_address_street " +
                    " from " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + custGroupId + " custG where id=? ";

            List<Map<String, Object>> detail = customerDao.sqlQuery(sql, superId);
            if (detail != null && detail.size() > 0) {
                Map<String, Object> map = detail.get(0);
                json.put("super_name", map.getOrDefault("super_name", ""));
                json.put("super_age", map.getOrDefault("super_age", ""));
                json.put("super_sex", map.getOrDefault("super_sex", ""));
                json.put("super_telphone", map.getOrDefault("super_telphone", ""));
                json.put("super_phone", map.getOrDefault("super_phone", ""));
                json.put("super_address_province_city", map.getOrDefault("super_address_province_city", ""));
                json.put("super_address_street", map.getOrDefault("super_address_street", ""));
            }
            json.put("supperData", superData);

            String insertSql = " insert into " + tableName +
                    " (`user_id`, `superid`, `customer_group_id`, `market_task_id`, `super_data`, `create_time`,customer_sea_id)" +
                    " VALUES ('" + userId + "','" + superId + "','" + custGroupId + "','" + marketTaskId + "','" + json.toJSONString() + "',now()" + ",'" + customerSeaId + "')";
            log.info("insertSql=" + insertSql);
            customerDao.executeUpdateSQL(insertSql);

        }
    }


    public String deleteSuperLable(String superId, String groupId) throws TouchException {

        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sb = new StringBuffer();

        try {
            sb.append(" DELETE from t_super_label ");
            sb.append(" where  super_id = ?");
            sb.append(" and cust_group_id = ?");

            int code = this.customerDao.executeUpdateSQL(sb.toString(), superId, groupId);

            log.info("删除客户自建标签对应联系客户,sql:" + sb.toString());
            map.put("code", code);
            map.put("message", "成功");
            json.put("data", map);

        } catch (Exception e) {
            log.error("删除客户自建标签对应联系客户失败,", e);
            map.put("code", 000);
            map.put("message", "失败");
            json.put("data", map);
            throw new TouchException("删除客户自建标签对应联系客户失败", e);

        }

        return json.toJSONString();
    }


    /**
     * 获取有效的自定义标签
     *
     * @param custId
     * @param status
     * @return
     */
    public List<Map<String, Object>> getActiveCustomlabel(String custId, Integer status) {
        Map<String, Object> map = new HashMap<>();
        StringBuffer sql = new StringBuffer();
        sql.append("  SELECT t1.id,t1.cust_id,t1.label_id,t1.status,t1.label_name,")
                .append("  t1.create_time,t1.label_desc,t1.type, t1.`option` ")
                .append("  FROM  t_customer_label t1");
        sql.append("  WHERE t1.cust_id =? ");
        sql.append("  AND t1.status = ? ");
        sql.append("  and t1.type in(2,3)");
        sql.append("  GROUP BY t1.id ");
        sql.append("  ORDER BY t1.create_time DESC");

        List<Map<String, Object>> list = this.customerDao.sqlQuery(sql.toString(), custId, status);
        staticCustomerLabels(list, false);
        return list;
    }


    /**
     * 获取客户下的所有自建属性列表
     *
     * @param custId
     * @param pageNum
     * @param pageSize
     * @param customerGroupId
     * @param marketTaskId
     * @param projectUserId   项目管理员ID
     * @return
     */
    public String getCustomLabel(String custId, Integer pageNum, Integer pageSize, String customerGroupId, String marketTaskId, String projectUserId, String projectId) {
        Integer marketProjectId = 0;
        boolean projectStatus = false;
        if (StringUtil.isNotEmpty(marketTaskId)) {
            projectStatus = true;
            CustomGroup cg = marketTaskDao.getCustomGroupByMarketTaskId(marketTaskId);
            if (cg != null) {
                marketProjectId = cg.getMarketProjectId();
            }
        } else if (StringUtil.isNotEmpty(customerGroupId)) {
            projectStatus = true;
            CustomGroup cg = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
            if (cg != null) {
                marketProjectId = cg.getMarketProjectId();
            }
        }
        Map<String, Object> map = new HashMap<>(16);
        List<Map<String, Object>> list;
        JSONObject json = new JSONObject();
        List<Object> p = new ArrayList<>();
        StringBuffer sql = new StringBuffer();
        sql.append("  SELECT t1.id,t1.cust_id,t1.user_id,t1.label_id,t1.status,t1.label_name, t1.market_project_id, ")
                .append("  t1.create_time,t1.update_time, t1.label_desc,t1.type, t1.`option`")
                .append("  FROM  t_customer_label t1")
                .append("  WHERE (t1.cust_id =? OR  t1.cust_id = '0')");
        p.add(custId);
        // 如果按照客户或者营销任务查询
        if (projectStatus) {
            if (marketProjectId != null) {
                p.add(marketProjectId);
                sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null OR t1.market_project_id = ? )");
            } else {
                sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null)");
            }
        }

        if (StringUtil.isNotEmpty(projectId)) {
            p.add(projectId);
            sql.append(" AND t1.market_project_id =? ");
        }
        if (pageNum == null || "".equals(pageNum) || pageSize == null || "".equals(pageSize)) {
            sql.append("  AND t1.status =1 ");
            sql.append("  GROUP BY t1.id");
            sql.append("  ORDER BY t1.update_time DESC, t1.create_time DESC ");
            list = this.customerDao.sqlQuery(sql.toString());
            if (list != null && list.size() > 0) {
                for (Map<String, Object> key : list) {
                    // 如果编辑时间为空则取创建时间
                    if (key != null) {
                        // 处理默认自建属性
                        if (key.get("cust_id") != null && "0".equals(String.valueOf(key.get("cust_id")))) {
                            key.put("defaultLabel", 1);
                        } else {
                            key.put("defaultLabel", 2);
                        }
                    }
                }
            }
            map.put("custGroupOrders", staticCustomerLabels(list, false));
            json.put("staffJson", map);
        } else {
            // 项目管理员ID
            if (StringUtil.isNotEmpty(projectUserId)) {
                List<String> projectIds = customerUserDao.listProjectByUserId(NumberConvertUtil.parseLong(projectUserId));
                sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null OR t1.market_project_id IN (" + SqlAppendUtil.sqlAppendWhereIn(projectIds) + " ) )");
            }

            sql.append("  GROUP BY t1.id");
            sql.append("  ORDER BY t1.update_time DESC, t1.create_time DESC ");
            Page page = null;
            try {
                page = customerDao.sqlPageQuery0(sql.toString(), pageNum, pageSize, p.toArray());
            } catch (Exception e) {
                page = new Page();
            }
            map.put("total", page.getTotal());
            map.put("custGroupOrders", page.getData());
            if (page.getData() != null && page.getData().size() > 0) {
                MarketProject marketProject;
                Map<String, Object> key = null;
                for (int i = 0; i < page.getData().size(); i++) {
                    key = (Map<String, Object>) page.getData().get(i);
                    key.put("acount", "");
                    // 如果编辑时间为空则取创建时间
                    if (key != null) {
                        if (key.get("update_time") == null) {
                            key.put("update_time", key.get("create_time"));
                        }
                        // 处理默认自建属性
                        if (key.get("cust_id") != null && "0".equals(String.valueOf(key.get("cust_id")))) {
                            key.put("defaultLabel", 1);
                        } else {
                            key.put("defaultLabel", 2);
                        }
                    }
                    key.put("market_project_name", "");
                    if (StringUtil.isNotEmpty(String.valueOf(key.get("market_project_id")))) {
                        marketProject = marketProjectDao.selectMarketProject(NumberConvertUtil.parseInt(key.get("market_project_id")));
                        if (marketProject != null) {
                            key.put("market_project_name", marketProject.getName());
                        }
                    }
                }
            }
            json.put("staffJson", map);
        }
        return json.toJSONString();
    }

    public String getCustomLabel0(String custId, Integer pageNum, Integer pageSize, String customerGroupId, String marketTaskId,
                                  String projectUserId, String projectId, String labelName, String type, String status) {
        Integer marketProjectId = 0;
        boolean projectStatus = false;
        if (StringUtil.isNotEmpty(marketTaskId)) {
            projectStatus = true;
            CustomGroup cg = marketTaskDao.getCustomGroupByMarketTaskId(marketTaskId);
            if (cg != null) {
                marketProjectId = cg.getMarketProjectId();
            }
        } else if (StringUtil.isNotEmpty(customerGroupId)) {
            projectStatus = true;
            CustomGroup cg = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
            if (cg != null) {
                marketProjectId = cg.getMarketProjectId();
            }
        }
        Map<String, Object> map = new HashMap<>(16);
        List<Map<String, Object>> list;
        JSONObject json = new JSONObject();
        StringBuffer sql = new StringBuffer();
        List<Object> p = new ArrayList<>();
        sql.append("  SELECT t1.id,t1.cust_id,t1.user_id,t1.label_id,t1.status,t1.label_name, GROUP_CONCAT(t1.market_project_id) market_project_id, ")
                .append("  t1.create_time,t1.update_time, t1.label_desc,t1.type, t1.`option`, t1.sort, t1.required ")
                .append("  FROM  t_customer_label t1")
                .append("  WHERE (t1.cust_id =?  OR  t1.cust_id = '0')");
        p.add(custId);
        // 如果按照客户或者营销任务查询
        if (projectStatus) {
            if (marketProjectId != null) {
                p.add(marketProjectId);
                sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null OR t1.market_project_id = ? )");
            } else {
                sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null)");
            }
        }

        if (StringUtil.isNotEmpty(projectId)) {
            p.add(projectId);
            sql.append(" AND t1.market_project_id =? ");
        }
        // 属性名称检索
        if (StringUtil.isNotEmpty(labelName)) {
            p.add("%" + labelName + "%");
            sql.append(" AND t1.label_name LIKE ? ");
        }
        // 类型检索
        if (StringUtil.isNotEmpty(type)) {
            p.add(type);
            sql.append(" AND t1.type =? ");
        }
        // 状态检索
        if (StringUtil.isNotEmpty(status)) {
            p.add(status);
            sql.append(" AND t1.status =? ");
        }
        if (pageNum == null || "".equals(pageNum) || pageSize == null || "".equals(pageSize)) {
            sql.append(" AND t1.status =1 ");
            sql.append(" GROUP BY t1.label_id");
            sql.append(" ORDER BY t1.sort IS NULL, t1.sort, t1.label_name ");
            list = this.customerDao.sqlQuery(sql.toString());
            if (list != null && list.size() > 0) {
                for (Map<String, Object> key : list) {
                    // 如果编辑时间为空则取创建时间
                    if (key != null) {
                        // 处理默认自建属性
                        if (key.get("cust_id") != null && "0".equals(String.valueOf(key.get("cust_id")))) {
                            key.put("defaultLabel", 1);
                        } else {
                            key.put("defaultLabel", 2);
                        }
                        // 为空时默认为不必填
                        if (key.get("required") == null) {
                            key.put("required", 2);
                        }
                        // 处理邀约状态必填属性
                        key.put("sysLabelId", "");
                        if (ConstantsUtil.SUCCESS_SYS_LABEL_ID.equals(String.valueOf(key.get("label_id"))) || ConstantsUtil.SUCCESS_SYS_LABEL_NAME.equals(String.valueOf(key.get("label_name")))) {
                            key.put("sysLabelId", ConstantsUtil.SUCCESS_SYS_LABEL_ID);
                        }
                    }
                }
            }
            map.put("custGroupOrders", staticCustomerLabels(list, false));
            json.put("staffJson", map);
        } else {
            List<Object> maxParam = new ArrayList<>();
            StringBuffer maxSortSql = new StringBuffer();
            maxParam.add(custId);
            maxSortSql.append("SELECT MAX(sort) maxSort FROM t_customer_label t1 WHERE (t1.cust_id =? OR  t1.cust_id = '0') ");
            // 项目管理员ID
            if (StringUtil.isNotEmpty(projectUserId)) {
                List<String> projectIds = customerUserDao.listProjectByUserId(NumberConvertUtil.parseLong(projectUserId));
                if (projectIds != null && projectIds.size() > 0) {
                    sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null OR t1.market_project_id IN (" + SqlAppendUtil.sqlAppendWhereIn(projectIds) + " ) )");
                    maxSortSql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null OR t1.market_project_id IN (" + SqlAppendUtil.sqlAppendWhereIn(projectIds) + " ) )");
                } else {
                    sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null) ");
                    maxSortSql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null) ");
                }
            }
            sql.append("  AND (t1.status =1 OR t1.status =2) ");
            maxSortSql.append("  AND (t1.status =1 OR t1.status =2) ");
            if (StringUtil.isNotEmpty(projectId)) {
                maxParam.add(projectId);
                maxSortSql.append(" AND t1.market_project_id =? ");
            }
            sql.append("  GROUP BY t1.label_id");
            sql.append("  ORDER BY t1.status, t1.create_time DESC, t1.sort ");
            Page page = null;
            try {
                page = customerDao.sqlPageQuery0(sql.toString(), pageNum, pageSize, p.toArray());
            } catch (Exception e) {
                page = new Page();
            }
            map.put("total", page.getTotal());
            map.put("custGroupOrders", page.getData());
            if (page.getData() != null && page.getData().size() > 0) {
                List<Map<String, Object>> maxSortList = customerDao.sqlQuery(maxSortSql.toString(), maxParam.toArray());
                int maxSort = maxSortList.size() > 0 && maxSortList.get(0).get("maxSort") != null ? NumberConvertUtil.parseInt(maxSortList.get(0).get("maxSort")) : 0;
                MarketProject marketProject;
                String projectIds, projectName;
                Map<String, Object> key = null;
                for (int i = 0; i < page.getData().size(); i++) {
                    key = (Map<String, Object>) page.getData().get(i);
                    // 最大排序数
                    key.put("maxSort", maxSort);
                    // 为空时默认为不必填
                    if (key.get("required") == null) {
                        key.put("required", 2);
                    }
                    key.put("acount", "");
                    // 如果编辑时间为空则取创建时间
                    if (key != null) {
                        if (key.get("update_time") == null) {
                            key.put("update_time", key.get("create_time"));
                        }
                        // 处理默认自建属性
                        if (key.get("cust_id") != null && "0".equals(String.valueOf(key.get("cust_id")))) {
                            key.put("defaultLabel", 1);
                        } else {
                            key.put("defaultLabel", 2);
                        }
                    }
                    key.put("market_project_name", "");
                    projectIds = String.valueOf(key.get("market_project_id"));
                    if (StringUtil.isNotEmpty(projectIds)) {
                        projectName = "";
                        for (String pId : projectIds.split(",")) {
                            marketProject = marketProjectDao.selectMarketProject(NumberConvertUtil.parseInt(pId));
                            if (marketProject != null) {
                                projectName += marketProject.getName() + ",";
                            }
                        }
                        if (projectName.length() > 0) {
                            projectName = projectName.substring(0, projectName.length() - 1);
                        }
                        key.put("market_project_name", projectName);
                    }
                }
            }
            json.put("staffJson", map);
        }
        return json.toJSONString();
    }

    /**
     * 自建属性列表
     *
     * @param custId
     * @param pageNum
     * @param pageSize
     * @param customerGroupId
     * @param marketTaskId
     * @param projectUserId
     * @param projectId
     * @param labelName
     * @param type
     * @param status
     * @return
     */
    public String getCustomLabel1(String custId, Integer pageNum, Integer pageSize, String customerGroupId, String marketTaskId,
                                  String projectUserId, String projectId, String labelName, String type, String status) {
        Integer marketProjectId = 0;
        boolean projectStatus = false;
        if (StringUtil.isNotEmpty(marketTaskId)) {
            projectStatus = true;
            CustomGroup cg = marketTaskDao.getCustomGroupByMarketTaskId(marketTaskId);
            if (cg != null) {
                marketProjectId = cg.getMarketProjectId();
            }
        } else if (StringUtil.isNotEmpty(customerGroupId)) {
            projectStatus = true;
            CustomGroup cg = customGroupDao.get(NumberConvertUtil.parseInt(customerGroupId));
            if (cg != null) {
                marketProjectId = cg.getMarketProjectId();
            }
        }
        Map<String, Object> map = new HashMap<>(16);
        List<Map<String, Object>> list;
        JSONObject json = new JSONObject();
        StringBuffer sql = new StringBuffer();
        List<Object> p = new ArrayList<>();
        sql.append("  SELECT t1.id,t1.cust_id,t1.user_id,t1.label_id,t1.status,t1.label_name, t1.market_project_id, ")
                .append("  t1.create_time,t1.update_time, t1.label_desc,t1.type, t1.`option`, t1.sort, t1.required,t1.is_show ")
                .append("  FROM  t_customer_label t1")
                .append("  WHERE (t1.cust_id =?  OR  t1.cust_id = '0')");
        p.add(custId);
        // 如果按照客户或者营销任务查询
        if (projectStatus) {
            if (marketProjectId != null) {
                p.add(marketProjectId);
                sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null OR t1.market_project_id = ? )");
            } else {
                sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null)");
            }
        }

        if (StringUtil.isNotEmpty(projectId)) {
            p.add(projectId);
            sql.append(" AND (t1.market_project_id =? OR t1.market_project_id = 0 OR t1.market_project_id is null) ");
        }
        // 属性名称检索
        if (StringUtil.isNotEmpty(labelName)) {
            p.add("%" + labelName + "%");
            sql.append(" AND t1.label_name LIKE ? ");
        }
        // 类型检索
        if (StringUtil.isNotEmpty(type)) {
            p.add(type);
            sql.append(" AND t1.type =? ");
        }
        // 状态检索
        if (StringUtil.isNotEmpty(status)) {
            p.add(status);
            sql.append(" AND t1.status =? ");
        }
        if (pageNum == null || "".equals(pageNum) || pageSize == null || "".equals(pageSize)) {
            sql.append(" AND t1.status =1 ");
            //sql.append(" GROUP BY t1.label_id");
            sql.append(" ORDER BY t1.sort IS NULL, t1.sort, t1.label_name ");
            list = this.customerDao.sqlQuery(sql.toString(), p.toArray());
            List<Map<String, Object>> result = new ArrayList<>();
            if (list != null && list.size() > 0) {
                for (Map<String, Object> key : list) {
                    // 如果编辑时间为空则取创建时间
                    if (key != null) {
                        if ("SYS007".equals(String.valueOf(key.get("label_id")))) {
                            continue;
                        }
                        // 处理默认自建属性
                        if (key.get("cust_id") != null && "0".equals(String.valueOf(key.get("cust_id")))) {
                            key.put("defaultLabel", 1);
                        } else {
                            key.put("defaultLabel", 2);
                        }
                        // 为空时默认为不必填
                        if (key.get("required") == null) {
                            key.put("required", 2);
                        }
                        // 处理邀约状态必填属性
                        key.put("sysLabelId", "");
                        if (ConstantsUtil.SUCCESS_SYS_LABEL_ID.equals(String.valueOf(key.get("label_id"))) || ConstantsUtil.SUCCESS_SYS_LABEL_NAME.equals(String.valueOf(key.get("label_name")))) {
                            key.put("sysLabelId", ConstantsUtil.SUCCESS_SYS_LABEL_ID);
                        }
                        result.add(key);
                    }
                }
            }
            //当按照客群或者营销任务查询时，查询该客群或营销任务被选中显示的labels
            getSelectedLabels(marketTaskId, customerGroupId, result);

            map.put("custGroupOrders", staticCustomerLabels(result, false));
            json.put("staffJson", map);
        } else {
            List<Object> maxParam = new ArrayList<>();
            StringBuffer maxSortSql = new StringBuffer();
            maxParam.add(custId);
            maxSortSql.append("SELECT MAX(sort) maxSort FROM t_customer_label t1 WHERE (t1.cust_id =? OR  t1.cust_id = '0') ");
            // 项目管理员ID
            if (StringUtil.isNotEmpty(projectUserId)) {
                List<String> projectIds = customerUserDao.listProjectByUserId(NumberConvertUtil.parseLong(projectUserId));
                if (projectIds != null && projectIds.size() > 0) {
                    sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null OR t1.market_project_id IN (" + SqlAppendUtil.sqlAppendWhereIn(projectIds) + " ) )");
                    maxSortSql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null OR t1.market_project_id IN (" + SqlAppendUtil.sqlAppendWhereIn(projectIds) + " ) )");
                } else {
                    sql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null) ");
                    maxSortSql.append(" AND (t1.market_project_id = 0 OR t1.market_project_id is null) ");
                }
            }
            sql.append("  AND (t1.status =1 OR t1.status =2) ");
            maxSortSql.append("  AND (t1.status =1 OR t1.status =2) ");
            if (StringUtil.isNotEmpty(projectId)) {
                maxParam.add(projectId);
                maxSortSql.append(" AND t1.market_project_id =? ");
            }
            //sql.append("  GROUP BY t1.label_id");
            sql.append("  ORDER BY t1.status, t1.create_time DESC, t1.sort ");
            Page page = null;
            try {
                page = customerDao.sqlPageQuery0(sql.toString(), pageNum, pageSize, p.toArray());
            } catch (Exception e) {
                page = new Page();
            }
            map.put("total", page.getTotal());
            map.put("custGroupOrders", page.getData());
            if (page.getData() != null && page.getData().size() > 0) {
                List<Map<String, Object>> maxSortList = customerDao.sqlQuery(maxSortSql.toString(), maxParam.toArray());
                int maxSort = maxSortList.size() > 0 && maxSortList.get(0).get("maxSort") != null ? NumberConvertUtil.parseInt(maxSortList.get(0).get("maxSort")) : 0;
                MarketProject marketProject;
                String projectIds, projectName;
                Map<String, Object> key = null;
                for (int i = 0; i < page.getData().size(); i++) {
                    key = (Map<String, Object>) page.getData().get(i);
                    // 最大排序数
                    key.put("maxSort", maxSort);
                    // 为空时默认为不必填
                    if (key.get("required") == null) {
                        key.put("required", 2);
                    }
                    key.put("acount", "");
                    // 如果编辑时间为空则取创建时间
                    if (key != null) {
                        if (key.get("update_time") == null) {
                            key.put("update_time", key.get("create_time"));
                        }
                        // 处理默认自建属性
                        if (key.get("cust_id") != null && "0".equals(String.valueOf(key.get("cust_id")))) {
                            key.put("defaultLabel", 1);
                        } else {
                            key.put("defaultLabel", 2);
                        }
                    }
                    key.put("market_project_name", "");
                    projectIds = String.valueOf(key.get("market_project_id"));
                    if (StringUtil.isNotEmpty(projectIds)) {
                        projectName = "";
                        for (String pId : projectIds.split(",")) {
                            marketProject = marketProjectDao.selectMarketProject(NumberConvertUtil.parseInt(pId));
                            if (marketProject != null) {
                                projectName += marketProject.getName() + ",";
                            }
                        }
                        if (projectName.length() > 0) {
                            projectName = projectName.substring(0, projectName.length() - 1);
                        }
                        key.put("market_project_name", projectName);
                    }
                }
            }
            json.put("staffJson", map);
        }
        return json.toJSONString();
    }

    private void getSelectedLabels(String marketTaskId, String customerGroupId, List<Map<String, Object>> result) {
        if (StringUtil.isNotEmpty(marketTaskId)) {
            MarketTaskProperty taskProperty = marketTaskDao.getProperty(marketTaskId, "selectedLabels");
            if (taskProperty == null) {
                result.stream().map(e -> e.put("is_selected", "0")).collect(Collectors.toList());
            }
        }
        if (StringUtil.isNotEmpty(customerGroupId)) {
            int customerGroupID = Integer.parseInt(customerGroupId);
            CustomerGroupProperty groupProperty = customGroupDao.getProperty(customerGroupID, "selectedLabels");
            if (groupProperty == null) {
                result.stream().map(e -> e.put("is_selected", "0")).collect(Collectors.toList());
            }
        }
    }

    /**
     * 营销任务左侧树
     *
     * @param custId
     */
    public JSONArray buildTaskTree(String custId, int customerGroupId) {
        JSONArray rootLabel = new JSONArray();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("labelName", "二次画像标签");
        jsonObject.put("labelId", "10000");
        JSONObject baseinfo = buildBaseInfo(customerGroupId);
        JSONArray secLabel = new JSONArray();
        secLabel.add(baseinfo);
        List<Map<String, Object>> lables = getActiveCustomlabel(custId, 1);
        JSONObject customLabels = buildCustomLabels(lables);
        secLabel.add(customLabels);
        jsonObject.put("children", secLabel);
        rootLabel.add(jsonObject);
        return rootLabel;
    }


    private JSONObject buidBaseInfo() {
        String[] childOption = {"0", "1", "2", "3", "4", "5及以上"};
        JSONObject baseInfo = new JSONObject();
        baseInfo.put("labelName", "基本信息");
        baseInfo.put("labelId", "100001000");
        JSONArray children = new JSONArray();
        JSONObject child1 = new JSONObject();
        child1.put("labelId", ConstantsUtil.CALL_COUNT_ID);
        child1.put("labelName", "呼叫次数");
        child1.put("options", childOption);
        children.add(child1);

        child1 = new JSONObject();
        child1.put("labelId", ConstantsUtil.CALL_SUCCESS_COUNT_ID);
        child1.put("labelName", "呼通次数");
        child1.put("options", childOption);
        children.add(child1);

        child1 = new JSONObject();
        child1.put("labelId", ConstantsUtil.SMS_COUNT_ID);
        child1.put("labelName", "短信次数");
        child1.put("options", childOption);
        children.add(child1);
        baseInfo.put("children", children);
        return baseInfo;
    }

    private JSONObject buildBaseInfo(int customerGroupId) {
        String[] childOption = {"0", "1", "2", "3", "4", "5及以上"};
        JSONObject baseInfo = new JSONObject();
        baseInfo.put("labelName", "基本信息");
        baseInfo.put("labelId", "100001000");
        JSONArray children = new JSONArray();
        JSONObject child;
        CustomerGroupProperty cgp = customGroupDao.getProperty(customerGroupId, "touchMode");
        if (cgp != null && StringUtil.isNotEmpty(cgp.getPropertyValue())) {
            String[] touchModes = cgp.getPropertyValue().split(",");
            if (touchModes.length == 2) {
                return buidBaseInfo();
            } else {
                if (cgp.getPropertyValue().indexOf("1") >= 0) {
                    child = new JSONObject();
                    child.put("labelId", ConstantsUtil.CALL_COUNT_ID);
                    child.put("labelName", "呼叫次数");
                    child.put("options", childOption);
                    children.add(child);

                    child = new JSONObject();
                    child.put("labelId", ConstantsUtil.CALL_SUCCESS_COUNT_ID);
                    child.put("labelName", "呼通次数");
                    child.put("options", childOption);
                    children.add(child);
                    baseInfo.put("children", children);
                } else if (cgp.getPropertyValue().indexOf("2") >= 0) {
                    child = new JSONObject();
                    child.put("labelId", ConstantsUtil.SMS_COUNT_ID);
                    child.put("labelName", "短信次数");
                    child.put("options", childOption);
                    children.add(child);
                    baseInfo.put("children", children);
                }
            }
        } else {
            return buidBaseInfo();
        }
        return baseInfo;
    }


    /**
     * 构造客户资料列表数据,处理option
     *
     * @param list
     * @param staticStatus
     * @author chengning@salescomm.net
     * @date 2018/8/29 15:12
     */
    private List<Map<String, Object>> staticCustomerLabels(List<Map<String, Object>> list, boolean staticStatus) {
        for (Map<String, Object> map : list) {
            // 单选或者多选的选项处理为数组
            if (map.get("type") != null && ("2".equals(String.valueOf(map.get("type")))
                    || "3".equals(String.valueOf(map.get("type"))))) {
                map.put("option", String.valueOf(map.get("option")).split(","));
            }
        }
        if (staticStatus) {
            Map<String, Object> data = new HashMap<>();
            data.put("label_id", 1);
            data.put("label_name", "意向客户");
            data.put("type", 2);
            data.put("option", new Object[]{"是", "否"});
            list.add(data);
            data = new HashMap<>();
            data.put("label_id", 2);
            data.put("label_name", "需要跟进");
            data.put("type", 2);
            data.put("option", new Object[]{"是", "否"});
            list.add(data);
        }
        return list;
    }


    public String deleteCustomlabel(Integer id, Integer status) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        StringBuffer sb = new StringBuffer();

        try {

            sb.append(" UPDATE t_customer_label  t ");

            sb.append(" SET t.status =?");
            sb.append(" WHERE  t.id = ?");

            int code = this.customerDao.executeUpdateSQL(sb.toString(), status, id);

            log.info("废弃客户自建标签，sql：" + sb.toString());
            map.put("code", code);
            map.put("message", "成功");
            json.put("data", map);

        } catch (Exception e) {
            map.put("code", 000);
            map.put("message", "失败");
            json.put("data", map);
        }

        return json.toJSONString();
    }

    /**
     * 自建属性废弃
     *
     * @param id
     * @param status
     * @return
     */
    public String deleteCustomLabel0(Integer id, Integer status) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        StringBuffer sb = new StringBuffer();
        CustomerLabel customerLabel = customerLabelDao.getCustomerLabel(id);
        if (customerLabel == null) {
            map.put("code", 000);
            map.put("message", "失败");
            json.put("data", map);
            return json.toJSONString();
        }
        try {
            sb.append(" UPDATE t_customer_label  t ");
            sb.append(" SET t.status =?");
            sb.append(" WHERE  t.label_id = ? AND t.cust_id=? ");
            int code = this.customerDao.executeUpdateSQL(sb.toString(), status, customerLabel.getLabelId(), customerLabel.getCustId());
            log.info("废弃客户自建标签，sql：" + sb.toString());
            map.put("code", code);
            map.put("message", "成功");
            json.put("data", map);
        } catch (Exception e) {
            map.put("code", 000);
            map.put("message", "失败");
            json.put("data", map);
        }
        return json.toJSONString();
    }

    public int openCustomLabel(CustomerLabel customerLabel) {
        int code = 0;
        try {
            CustomerLabel dbCstomerLabel = customerLabelDao.getCustomerLabel(customerLabel.getLabelId());
            if (dbCstomerLabel != null) {
                dbCstomerLabel.setStatus("1");
                this.customerDao.saveOrUpdate(dbCstomerLabel);
                code = 1;
            }
        } catch (Exception e) {
            log.error("启用自建属性异常,", e);
            code = 0;
        }

        return code;
    }

    public CustomerLabel getCustomLabelByName(String labelName, String custId) {
        return customerLabelDao.getCustomerLabelByName(labelName, custId);
    }


    private JSONObject buildCustomLabels(List<Map<String, Object>> lables) {
        JSONObject customInfo = new JSONObject();
        customInfo.put("labelName", "自定义属性信息");
        customInfo.put("labelId", "200001000");
        JSONArray children = new JSONArray();
        for (Map<String, Object> label : lables) {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("labelId", label.getOrDefault("label_id", ""));
            jsonObject.put("labelName", label.getOrDefault("label_name", ""));
            jsonObject.put("options", label.getOrDefault("option", ""));
            children.add(jsonObject);
        }
        customInfo.put("children", children);
        return customInfo;
    }

    /**
     * @description 获取标签信息
     * @method
     * @date: 2019/7/1 11:43
     */
    public Map<String, Object> getLabelInfoById(CustomerLabelDTO customerLabelDTO, LoginUser loginUser) throws Exception {
        Map<String, Object> map = new HashMap();
        String labelName = customerLabelDTO.getLabelName();
        String projectId = customerLabelDTO.getMarketProjectId();
        //根据项目id查询标签信息
        if (StringUtil.isEmpty(labelName)) {
            labelName = "跟进状态";
        }
        //查询自定义状态
        CustomerLabel customerLabel = customerLabelDao.getLabelByProjectId(projectId, loginUser.getCustId(), labelName);
        if (customerLabel != null) {
            map.put("customLabel", customerLabel);
        }
        //查询全局状态
        CustomerLabel allLabel = customerLabelDao.getLabelByProjectId(null, "0", labelName);
        map.put("allLabel", allLabel);
        //查询无效原因标签
        CustomerLabel invalidLabel = customerLabelDao.getLabelByProjectId(null, "0", "无效原因");
        map.put("invalidLabel", invalidLabel);
        return map;
    }

    /**
     * @description
     * @method
     * @date: 2019/7/1 10:33
     */
    public void labelSaveOrUpdate(CustomerLabelDTO customerLabelDTO, LoginUser loginUser) throws Exception {
        //判断当前的标签信息是否存在
        String labelName = customerLabelDTO.getLabelName();
        if (StringUtil.isEmpty(labelName)) {
            labelName = "跟进状态";
        }
        String marketProjectId = customerLabelDTO.getMarketProjectId();
        CustomerLabel labelByProjectId = customerLabelDao.getLabelByProjectId(marketProjectId, loginUser.getCustId(), labelName);
        if (labelByProjectId != null) {
            log.info("项目id是" + customerLabelDTO.getMarketProjectId() + "跟进状态标签存在,进行修改操作");
            labelByProjectId.setOption(customerLabelDTO.getOption());
            labelByProjectId.setUpdateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            customerLabelDao.saveOrUpdate(labelByProjectId);
        } else {
            CustomerLabel customerLabel = new CustomerLabel();
            customerLabel.setCustId(loginUser.getCustId());
            customerLabel.setUserId(String.valueOf(loginUser.getId()));
            customerLabel.setLabelId(Long.toString(IDHelper.getID()));
            customerLabel.setLabelName(labelName);
            customerLabel.setCreateTime(DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
            customerLabel.setStatus("1");
            customerLabel.setLabelDesc(customerLabelDTO.getLabelDesc());
            customerLabel.setType(customerLabelDTO.getType());
            customerLabel.setOption(customerLabelDTO.getOption());
            customerLabel.setMarketProjectId(customerLabelDTO.getMarketProjectId());
            customerLabelDao.saveOrUpdate(customerLabel);
        }
    }

    /**
     * 检查自建属性名称是否存在
     *
     * @param includeLabelId
     * @param name
     * @param status
     * @return
     */
    public boolean checkLabelNameExist(String includeLabelId, String name, int status, String custId) {
        List<CustomerLabel> customerLabels = customerLabelDao.listCustomerLabelNameExist(includeLabelId, name, status, custId);
        return customerLabels.size() > 0 ? true : false;
    }

    /**
     * 检查项目下自建属性名称是否存在
     *
     * @param includeLabelId
     * @param name
     * @param status
     * @param custId
     * @param projectId
     * @return
     */
    public boolean checkProjectLabelNameExist(String includeLabelId, String name, int status, String custId, String projectId) {
        List<CustomerLabel> customerLabels = customerLabelDao.listCustomerLabelNameExist(includeLabelId, name, status, custId, projectId);
        return customerLabels.size() > 0 ? true : false;
    }
}
