package com.bdaim.customersea.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.dto.XfPullPhoneDTO;
import com.bdaim.callcenter.dto.XzPullPhoneDTO;
import com.bdaim.callcenter.service.impl.XzCallCenterService;
import com.bdaim.callcenter.util.XzCallCenterUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.service.ElasticSearchService;
import com.bdaim.common.service.PhoneService;
import com.bdaim.common.util.*;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerUserDTO;
import com.bdaim.customer.entity.CustomerLabel;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserGroup;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.CustomerLabelService;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customersea.dao.CustomerSeaDao;
import com.bdaim.customersea.dto.*;
import com.bdaim.customersea.entity.CustomerSea;
import com.bdaim.customersea.entity.CustomerSeaProperty;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.log.dao.SuperDataOperLogDao;
import com.bdaim.log.dto.SuperDataOperLogDTO;
import com.bdaim.log.dto.SuperDataOperLogQuery;
import com.bdaim.log.entity.SuperDataOperLog;
import com.bdaim.log.util.SuperDataOperLogUtil;
import com.bdaim.marketproject.dao.MarketProjectDao;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.marketproject.entity.MarketProjectProperty;
import com.bdaim.marketproject.service.MarketProjectService;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import org.hibernate.HibernateException;
import org.hibernate.exception.SQLGrammarException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import javax.transaction.Transactional;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * 公海业务处理
 *
 * @author chengning@salescomm.net
 * @date 2019/6/19
 * @description
 */
@Service("customerSeaService")
@Transactional
public class CustomerSeaService {

    public static final Logger LOG = LoggerFactory.getLogger(CustomerSeaService.class);


    private final static String CREATE_SEA_SQL = "CREATE TABLE IF NOT EXISTS " + ConstantsUtil.SEA_TABLE_PREFIX + "{0} LIKE t_customer_group_list";

    @Resource
    private CustomerSeaDao customerSeaDao;
    @Resource
    private CustomerDao customerDao;

    @Resource
    private MarketProjectDao marketProjectDao;

    @Resource
    private CustomGroupDao customGroupDao;

    @Resource
    private XzCallCenterService xzCallCenterService;

    @Resource
    private MarketProjectService projectService;

    @Resource
    private CustomerUserDao customerUserDao;

    @Resource
    private CustomerLabelService labelService;

    @Resource
    private MarketResourceDao marketResourceDao;

    @Resource
    private ElasticSearchService elasticSearchService;

    @Resource
    private CustomerLabelService customerLabelService;

    @Resource
    private PhoneService phoneService;

    @Resource
    private CustomerService customerService;

    @Resource
    private JdbcTemplate jdbcTemplate;

    @Resource
    private SuperDataOperLogDao superDataOperLogDao;

    /**
     * 默认需要转为super_data的字段名称
     */
    private Map<String, String> defaultLabels = new HashMap() {{
        put("qq", "SYS002");
        put("email", "SYS003");
        put("profession", "SYS004");
        put("weChat", "SYS001");
        put("company", "SYS005");
        put("followStatus", "SYS007");
        put("invalidReason", "SYS006");
    }};

    /**
     * 创建公海数据表
     *
     * @param seaId
     * @return
     */
    private int createCustomerSeaDataTable(String seaId) {
        if (StringUtil.isEmpty(seaId)) {
            throw new ParamException("seaId必填");
        }
        return customerSeaDao.executeUpdateSQL(MessageFormat.format(CREATE_SEA_SQL, seaId));
    }

    /**
     * 客群数据导入公海
     *
     * @param custId
     * @param cGroupId
     * @param seaId
     * @return
     */
    public int importCGroupDataToCustomerSea(String custId, int cGroupId, String seaId, int dataSource) throws TouchException {
        CustomGroup customGroup = customGroupDao.get(cGroupId);
        CustomerSea customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(seaId));
        if (customGroup == null || customerSea == null) {
            throw new TouchException("-1", "客群/公海不存在");
        }
        if (!Objects.equals(custId, customerSea.getCustId())) {
            throw new TouchException("-2", "公海不属于该客户");
        }
        if (!Objects.equals(custId, customGroup.getCustId())) {
            throw new TouchException("-2", "客群不属于该客户");
        }
        LOG.info("客群:" + cGroupId + ",开始导入公海:" + seaId);
        // 创建公海数据表
        createCustomerSeaDataTable(seaId);
        StringBuffer sql = new StringBuffer();
        // 查询客群总数据量条数
        long cgCount = customGroupDao.getCustomerGroupListDataCount(cGroupId);
        sql.append("SELECT COUNT(0) count FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX + seaId)
                .append(" WHERE id IN (SELECT id FROM ").append(ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + cGroupId).append(" )");
        int existCount = 0;
        List<Map<String, Object>> list = marketProjectDao.sqlQuery(sql.toString());
        if (list != null && list.size() > 0) {
            existCount = NumberConvertUtil.parseInt(list.get(0).get("count"));
        }
        // 客群数量等于公海存在数据不执行导入逻辑
        if (cgCount == existCount) {
            LOG.warn("客群:" + cGroupId + "数据数量:" + cgCount + ",在公海:" + seaId + "存在数量:" + existCount);
            return 0;
        }
        // 导入公海数据
        Timestamp createTime = new Timestamp(System.currentTimeMillis());
        sql.setLength(0);
        sql.append("INSERT INTO ").append(ConstantsUtil.SEA_TABLE_PREFIX + seaId)
                .append(" (id, status, batch_id, create_time, data_source, remark, super_data) SELECT id, '1', ")
                // batch_id等于客群ID
                .append(cGroupId).append(",'").append(createTime).append("',").append(dataSource)
                .append(", remark").append(", '{\"SYS007\":\"未跟进\"}' FROM ")
                .append(ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + cGroupId)
                .append(" ON DUPLICATE KEY UPDATE id = VALUES(id)");
        int count = marketProjectDao.executeUpdateSQL(sql.toString());
        LOG.info("客群:" + cGroupId + ",导入公海完成:" + seaId + ",条数:" + (count - existCount));
        // 客群导入公海后更新客群状态
        customGroup.setStatus(6);
        customGroupDao.update(customGroup);
        return count - existCount;
    }

    /**
     * 保存公海属性
     *
     * @param param
     * @return
     */
    private int saveCustomerSeaProperty(CustomerSeaParam param) {
        Timestamp createTime = new Timestamp(System.currentTimeMillis());
        int code = 0;
        // 保存呼叫模式
        if (param.getCallType() != null) {
            CustomerSeaProperty callType = new CustomerSeaProperty(param.getId(), "callType", String.valueOf(param.getCallType()), createTime);
            customerSeaDao.saveOrUpdate(callType);
        }
        // 保存渠道
        if (StringUtil.isNotEmpty(param.getCallChannel())) {
            CustomerSeaProperty callChannel = customerSeaDao.getProperty(String.valueOf(param.getId()), "callChannel");
            if (callChannel == null) {
                callChannel = new CustomerSeaProperty(param.getId(), "callChannel", param.getCallChannel(), createTime);
            }
            callChannel.setPropertyValue(param.getCallChannel());
            customerSeaDao.saveOrUpdate(callChannel);
        }

        // 线索领取方式
        if (param.getClueGetMode() != null) {
            CustomerSeaProperty clueGetMode = new CustomerSeaProperty(param.getId(), "clueGetMode", String.valueOf(param.getClueGetMode()), createTime);
            customerSeaDao.saveOrUpdate(clueGetMode);
        }
        // 线索领取限制
        if (param.getClueGetRestrict() != null) {
            CustomerSeaProperty clueGetRestrict = new CustomerSeaProperty(param.getId(), "clueGetRestrict", String.valueOf(param.getClueGetRestrict()), createTime);
            customerSeaDao.saveOrUpdate(clueGetRestrict);
        }
        // 线索领取限制值
        if (StringUtil.isNotEmpty(param.getClueGetRestrictValue())) {
            CustomerSeaProperty clueGetRestrictValue = new CustomerSeaProperty(param.getId(), "clueGetRestrictValue", String.valueOf(param.getClueGetRestrictValue()), createTime);
            customerSeaDao.saveOrUpdate(clueGetRestrictValue);
        }
        // 人工跟进
        if (param.getIsPersonFollow() != null) {
            CustomerSeaProperty isPersonFollow = new CustomerSeaProperty(param.getId(), "isPersonFollow", String.valueOf(param.getIsPersonFollow()), createTime);
            customerSeaDao.saveOrUpdate(isPersonFollow);
        }
        // 意向度
        if (StringUtil.isNotEmpty(param.getIntentLevel())) {
            CustomerSeaProperty intentLevel = new CustomerSeaProperty(param.getId(), "intentLevel", String.valueOf(param.getIntentLevel()), createTime);
            customerSeaDao.saveOrUpdate(intentLevel);
        }
        // 保存外显
        if (StringUtil.isNotEmpty(param.getApparentNumber())) {
            CustomerSeaProperty apparentNumber = new CustomerSeaProperty(param.getId(), "apparentNumber", String.valueOf(param.getApparentNumber()), createTime);
            customerSeaDao.saveOrUpdate(apparentNumber);
        }
        // 保存呼叫速度
        if (param.getCallSpeed() != null) {
            CustomerSeaProperty callSpeed = new CustomerSeaProperty(param.getId(), "callSpeed", String.valueOf(param.getCallSpeed()), createTime);
            customerSeaDao.saveOrUpdate(callSpeed);
        }
        // 保存呼叫次数
        if (param.getCallCount() != null) {
            CustomerSeaProperty callCount = new CustomerSeaProperty(param.getId(), "callCount", String.valueOf(param.getCallCount()), createTime);
            customerSeaDao.saveOrUpdate(callCount);
        }
        code = 1;
        return code;
    }

    /**
     * 创建公海默认线索客群
     *
     * @param customerSeaId
     * @param customerSeaName
     * @param custId
     */
    private void createDefaultClueCGroup(long customerSeaId, String customerSeaName, String custId) {
        CustomerSeaProperty csp = customerSeaDao.getProperty(String.valueOf(customerSeaId), "defaultClueCgId");
        if (csp != null) {
            LOG.warn("公海:" + customerSeaId + ",默认线索客群已经存在,客群ID:" + csp.getPropertyValue());
            return;
        }
        CustomerSea customerSea = customerSeaDao.get(customerSeaId);
        //插入订单表
        StringBuffer insertOrder = new StringBuffer();
        String orderId = String.valueOf(IDHelper.getTransactionId());
        insertOrder.append("INSERT INTO t_order (`order_id`, `cust_id`, `order_type`, `create_time`,  `remarks`, `amount`, order_state, `cost_price`) ");
        insertOrder.append(" VALUES ('" + orderId + "','" + custId + "','1','" + new Timestamp(System.currentTimeMillis()) + "','客户群创建','0','2','0')");
        int b = customGroupDao.executeUpdateSQL(insertOrder.toString());
        LOG.info("创建公海默认客群存入order表状态:" + b);
        CustomGroup cg = new CustomGroup();
        cg.setName(customerSeaName);
        cg.setDesc(customerSeaName);
        cg.setOrderId(orderId);
        cg.setStatus(6);
        cg.setUserCount(0L);
        cg.setQuantity(0);
        cg.setCustId(custId);
        cg.setCreateTime(new Timestamp(System.currentTimeMillis()));
        if (customerSea != null) {
            cg.setMarketProjectId(customerSea.getMarketProjectId());
        }
        LOG.info("创建公海默认客群插入customer_group表的数据:" + cg);
        int id = (int) customGroupDao.saveReturnPk(cg);
        LOG.info("创建公海默认客群返回主键id是:" + id);
        if (id > 0) {
            // 创建客群明细表
            try {
                int status = customGroupDao.createCgDataTable(id);
                LOG.info("创建公海默认客群明细表状态:" + status);
                // 保存默认客群
                csp = new CustomerSeaProperty(customerSeaId, "defaultClueCgId", String.valueOf(id), new Timestamp(System.currentTimeMillis()));
                customerSeaDao.saveOrUpdate(csp);
            } catch (HibernateException e) {
                LOG.error("创建公海默认客群明细表失败,", e);
            }
        }
    }

    /**
     * 根据项目ID添加讯众自动外呼成员
     *
     * @param projectId
     * @param custId
     * @param userGroupIds
     * @return
     */
    public int saveXzAutoMember(int projectId, String custId, List<String> userGroupIds) {
        // 查询项目下的自动外呼类型的公海
        int code = 0;
        try {
            CustomerSeaParam param = new CustomerSeaParam();
            param.setCustId(custId);
            param.setMarketProjectId(projectId);
            param.setTaskType(1);
            List<CustomerSea> customerSeas = customerSeaDao.listCustomerSea(param);
            if (customerSeas.size() == 0) {
                LOG.warn("项目[" + projectId + "]无自动外呼任务");
                return code;
            }
            ResourcePropertyEntity mrp;
            CustomerSeaProperty csp;
            JSONObject callCenterConfig;
            Set<String> seatIds, addSeatIds, delSeatIds;
            List<CustomerUserDTO> tmp;
            CustomerUserPropertyDO cp;
            String callCenterId;
            Map<String, Object> addResult;
            JSONObject jsonObject;
            for (CustomerSea sea : customerSeas) {
                seatIds = new HashSet<>();
                addSeatIds = new HashSet<>();
                delSeatIds = new HashSet<>();
                csp = customerSeaDao.getProperty(String.valueOf(sea.getId()), "callChannel");
                if (csp == null || StringUtil.isEmpty(csp.getPropertyValue())) {
                    continue;
                }
                // 判断渠道是否为讯众自动外呼
                mrp = marketResourceDao.getProperty(csp.getPropertyValue(), "price_config");
                if (mrp == null || StringUtil.isEmpty(mrp.getPropertyValue())) {
                    continue;
                }
                callCenterConfig = JSON.parseObject(mrp.getPropertyValue());
                if ("1".equals(callCenterConfig.getString("type")) && "2".equals(callCenterConfig.getString("call_center_type"))) {
                    for (String groupId : userGroupIds) {
                        tmp = customerUserDao.listSelectCustomerUserByUserGroupId(groupId, custId);
                        for (CustomerUserDTO user : tmp) {
                            // 判断坐席呼叫渠道和公海呼叫渠道是否相同
                            cp = customerUserDao.getProperty(user.getId(), "call_channel");
                            if (cp == null || !csp.getPropertyValue().equals(cp.getPropertyValue())) {
                                continue;
                            }
                            // 查询坐席ID
                            cp = customerUserDao.getProperty(user.getId(), "seats_account");
                            if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
                                continue;
                            }
                            seatIds.add(cp.getPropertyValue());
                        }
                    }
                    // 添加成员
                    if (seatIds.size() > 0) {
                        callCenterId = callCenterConfig.getJSONObject("call_center_config").getString("callCenterId");
                        // 查询任务下已经存在的成员
                        jsonObject = xzCallCenterService.getTaskMembers(sea.getTaskId(), 2, callCenterId);
                        if (jsonObject != null && jsonObject.getString("data") != null) {
                            JSONArray jsonArray = JSON.parseArray(jsonObject.getString("data"));
                            Set<String> members = new HashSet<>();
                            for (int i = 0; i < jsonArray.size(); i++) {
                                members.add(jsonArray.getJSONObject(i).getString("agentid"));
                            }
                            // 处理增量添加的坐席
                            for (String id : seatIds) {
                                if (!members.contains(id)) {
                                    addSeatIds.add(id);
                                }
                            }
                            // 处理删除坐席
                            for (String id : members) {
                                if (!seatIds.contains(id)) {
                                    delSeatIds.add(id);
                                }
                            }
                        }
                        if (addSeatIds.size() > 0) {
                            addResult = xzCallCenterService.addTaskMembers(sea.getTaskId(), addSeatIds, callCenterId);
                            if (!"0".equals(String.valueOf(addResult.get("code")))) {
                                LOG.warn("讯众自动外呼ID:[" + sea.getTaskId() + "],呼叫中心ID:[" + callCenterId + "]添加成员失败,尝试重新添加");
                                xzCallCenterService.addTaskMembers(sea.getTaskId(), addSeatIds, callCenterId);
                            }
                        }
                        // 删除成员
                        if (delSeatIds.size() > 0) {
                            xzCallCenterService.removeTaskMembers(sea.getTaskId(), delSeatIds, callCenterId);
                        }
                    }
                }
            }
            code = 1;
        } catch (Exception e) {
            code = -1;
            LOG.error("项目成员修改添加讯众坐席异常,", e);
        }
        return code;
    }

    /**
     * 线索领取方式 1-手动 2-系统自动
     *
     * @param seaId
     * @return
     */
    private int getCustomerSeaClueGetMode(String seaId) {
        CustomerSeaProperty cp = customerSeaDao.getProperty(seaId, "clueGetMode");
        if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
            return 0;
        }
        return NumberConvertUtil.parseInt(cp.getPropertyValue());
    }

    /**
     * 线索领取限制 1-无限制 2-限制数量
     *
     * @param seaId
     * @return
     */
    private int getCustomerSeaGetRestrict(String seaId) {
        CustomerSeaProperty cp = customerSeaDao.getProperty(seaId, "clueGetRestrict");
        if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
            return 0;
        }
        return NumberConvertUtil.parseInt(cp.getPropertyValue());
    }

    /**
     * 获取用户指定公海当天可领取线索数量
     *
     * @param seaId
     * @return -1 无限制领取 大于0标识可领取数量
     */
    public long getUserReceivableQuantity(String seaId, String userId) throws TouchException {
        int getMode = getCustomerSeaClueGetMode(seaId);
        int getRestrict = getCustomerSeaGetRestrict(seaId);
        // 手动领取
        if (getMode == 1) {
            //限制数量
            if (getRestrict == 2) {
                CustomerSeaProperty cp = customerSeaDao.getProperty(seaId, "clueGetRestrictValue");
                if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
                    return 0L;
                }
                long clueGetRestrictValue = NumberConvertUtil.parseLong(cp.getPropertyValue());

                LocalDateTime min = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                LocalDateTime max = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
                StringBuilder sql = new StringBuilder();
                // 查询转交记录表公海下当天用户已经领取的线索数量
                sql.append(" SELECT COUNT(0) count FROM ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" WHERE event_type = 5 AND user_id = ? AND customer_sea_id = ? AND create_time BETWEEN ? AND ?");
                List<Map<String, Object>> list = customerSeaDao.sqlQuery(sql.toString(), userId, seaId, DatetimeUtils.DATE_TIME_FORMATTER.format(min), DatetimeUtils.DATE_TIME_FORMATTER.format(max));
                long value = clueGetRestrictValue - NumberConvertUtil.parseLong(list.get(0).get("count"));
                if (value < 0) {
                    LOG.warn("公海:[" + seaId + "],用户:[" + userId + "]可领取量:[" + value + "]小于0");
                    value = 0;
                }
                return value;
            } else if (getRestrict == 1) {
                // 标识可无限制领取
                return -1;
            }
        } else {
            throw new TouchException("-1", "当前公海非手动领取模式");
        }
        return 0;
    }

    /**
     * 保存公海
     *
     * @param param
     * @return
     */
    public int save(CustomerSeaParam param) {
        Timestamp time = new Timestamp(System.currentTimeMillis());
        int status = 0;
        // 保存公海
        if (param.getId() == null) {
            CustomerSea customerSea = new CustomerSea(param);
            customerSea.setStatus(2);
            customerSea.setTaskPhoneIndex(0);
            customerSea.setTaskSmsIndex(0);
            customerSea.setQuantity("0");
            customerSea.setCreateTime(time);
            long id = (long) customerSeaDao.saveReturnPk(customerSea);
            customerSea.setId(id);
            // 创建公海数据表
            createCustomerSeaDataTable(String.valueOf(customerSea.getId()));
            param.setId(customerSea.getId());
            // 保存公海属性
            saveCustomerSeaProperty(param);

            // 创建默认线索导入客群
            createDefaultClueCGroup(customerSea.getId(), param.getName() + "(默认客群)", param.getCustId());
            status = 1;
        } else if (param.getId() > 0) {
            //更新公海
            CustomerSea customerSea = customerSeaDao.get(param.getId());
            if (customerSea == null) {
                LOG.error("公海:" + customerSea.getId() + ",不存在");
                return 0;
            }
            customerSea.setUpdateTime(time);
            // 修改公海状态
            if (1 == param.getOperation()) {
                customerSea.setStatus(param.getStatus());
                customerSeaDao.update(customerSea);
                status = 1;
                if (customerSea.getTaskType() != null && customerSea.getTaskType() == 1) {
                    // 如果编辑的呼叫渠道和上次呼叫渠道不相同,或者任务ID为空则创建讯众自动外呼任务
                    CustomerSeaProperty csp = customerSeaDao.getProperty(String.valueOf(customerSea.getId()), "callChannel");
                    if (csp == null) {
                        return status;
                    }
                    // 添加讯众自动外呼任务
                    ResourcePropertyEntity mrp = marketResourceDao.getProperty(csp.getPropertyValue(), "price_config");
                    if (mrp == null || StringUtil.isEmpty(mrp.getPropertyValue())) {
                        return status;
                    }
                    JSONObject callCenterConfig = JSON.parseObject(mrp.getPropertyValue());
                    // 呼叫中心类型SaaS模式则创建讯众自动外呼任务
                    if ("1".equals(callCenterConfig.getString("type")) && "2".equals(callCenterConfig.getString("call_center_type"))) {
                        String xzCallCenterId = callCenterConfig.getJSONObject("call_center_config").getString("callCenterId");
                        try {
                            // 开启
                            if (param.getStatus() == 1) {
                                JSONObject jsonObject = XzCallCenterUtil.startAutoTask(xzCallCenterId, customerSea.getTaskId());
                                LOG.info("公海开启讯众自动外呼任务返回结果" + jsonObject);
                            } else if (param.getStatus() == 2) {
                                // 关闭
                                JSONObject jsonObject = XzCallCenterUtil.stopAutoTask(xzCallCenterId, customerSea.getTaskId());
                                LOG.info("公海停止讯众自动外呼任务返回结果" + jsonObject);
                            }
                        } catch (Exception e) {
                            LOG.error("公海开启或关闭讯众自动外呼失败", e);
                        }
                    }
                }
            } else if (2 == param.getOperation()) {
                customerSea.setName(param.getName());
                if (param.getTaskType() != null) {
                    customerSea.setTaskType(param.getTaskType());
                }
                //customerSea.setTaskId(param.getTaskId());
                if (param.getTaskCreateTime() != null) {
                    customerSea.setTaskCreateTime(new Timestamp(param.getTaskCreateTime()));
                }
                if (param.getTaskEndTime() != null) {
                    customerSea.setTaskEndTime(new Timestamp(param.getTaskEndTime()));
                }
                if (StringUtil.isNotEmpty(param.getHistoryTaskId())) {
                    CustomerSea sea = customerSeaDao.get(Long.valueOf(param.getHistoryTaskId()));
                    if (sea != null && StringUtil.isNotEmpty(sea.getTaskId())) {
                        param.setTaskId(sea.getTaskId());
                    }
                }
                if (StringUtil.isNotEmpty(param.getTaskId())) {
                    customerSea.setTaskId(param.getTaskId());
                }

                String xzCallCenterId = null;
                // 只处理创建的自动外呼新任务
                if (param.getTaskType() != null && param.getTaskType() == 1) {
                    // 如果编辑的呼叫渠道和上次呼叫渠道不相同,或者任务ID为空则创建讯众自动外呼任务
                    CustomerSeaProperty csp = customerSeaDao.getProperty(String.valueOf(customerSea.getId()), "callChannel");
                    if (csp == null || !Objects.equals(csp.getPropertyValue(), param.getCallChannel())
                            || StringUtil.isEmpty(customerSea.getTaskId())) {
                        // 添加讯众自动外呼任务
                        ResourcePropertyEntity mrp = marketResourceDao.getProperty(param.getCallChannel(), "price_config");
                        if (mrp != null && StringUtil.isNotEmpty(mrp.getPropertyValue())) {
                            JSONObject callCenterConfig = JSON.parseObject(mrp.getPropertyValue());
                            // 呼叫中心类型SaaS模式则创建讯众自动外呼任务
                            if ("1".equals(callCenterConfig.getString("type")) && "2".equals(callCenterConfig.getString("call_center_type"))) {
                                xzCallCenterId = callCenterConfig.getJSONObject("call_center_config").getString("callCenterId");
                                param.setId(customerSea.getId());
                                JSONObject jsonObject = xzCallCenterService.addXzAutoTask(xzCallCenterId, param.getName() + IDHelper.getID(), param.getApparentNumber(),
                                        param.getCallSpeed(), param.getCallCount(), LocalDateTime.now().toEpochSecond(ZoneOffset.of("+8")),
                                        LocalDateTime.now().plusMonths(360).toEpochSecond(ZoneOffset.of("+8")), ConstantsUtil.XZ_SEA_AUTO_TASK_PHONE_URL);
                                if (jsonObject != null) {
                                    customerSea.setTaskId(jsonObject.getString("taskidentity"));
                                    customerSeaDao.update(customerSea);
                                    // 保存营销任务讯众呼叫中心配置信息
                                    CustomerSeaProperty taskConfig = new CustomerSeaProperty(param.getId(), "xzTaskConfig", jsonObject.toJSONString(), new Timestamp(System.currentTimeMillis()));
                                    customerSeaDao.saveOrUpdate(taskConfig);
                                    // 添加讯众自动外呼成员
                                    try {
                                        MarketProjectProperty mp = marketProjectDao.getProperty(String.valueOf(customerSea.getMarketProjectId()), "executionGroup");
                                        if (mp != null && StringUtil.isNotEmpty(mp.getPropertyValue())) {
                                            List<String> groupIds = Arrays.asList(mp.getPropertyValue().split(","));
                                            int code = saveXzAutoMember(customerSea.getMarketProjectId(), customerSea.getCustId(), groupIds);
                                            LOG.info("编辑公海处理项目执行组成员,项目ID[" + customerSea.getMarketProjectId() + "]更改状态成功,status:" + code);
                                            // 通过接口开启自动外呼任务
                                            try {
                                                JSONObject startResult = XzCallCenterUtil.startAutoTask(xzCallCenterId, jsonObject.getString("taskidentity"));
                                                LOG.info("公海开启讯众自动外呼任务返回结果:" + startResult);
                                            } catch (Exception e) {
                                                LOG.error("公海开启讯众自动外呼任务失败", e);
                                            }
                                        }
                                    } catch (Exception e) {
                                        LOG.error("异步处理项目执行组成员异常,", e);
                                    }
                                }
                            } else {
                                LOG.warn("呼叫渠道:" + param.getCallChannel() + ",非Saas模式!");
                            }
                        }
                    }
                }
                // 修改公海配置信息
                saveCustomerSeaProperty(param);
                status = 1;
            } else if (3 == param.getOperation()) {
                // 线索回收规则设置
                CustomerSeaProperty recoveryRule = new CustomerSeaProperty(param.getId(), "recoveryRule", String.valueOf(param.getRecoveryRule()), time);
                customerSeaDao.saveOrUpdate(recoveryRule);
                if (StringUtil.isNotEmpty(param.getRecoveryGetTimeout())) {
                    CustomerSeaProperty recoveryGetTimeout = new CustomerSeaProperty(param.getId(), "recoveryGetTimeout", param.getRecoveryGetTimeout(), time);
                    customerSeaDao.saveOrUpdate(recoveryGetTimeout);
                }
                if (StringUtil.isNotEmpty(param.getRecoveryFirstTimeout())) {
                    CustomerSeaProperty recoveryFirstTimeout = new CustomerSeaProperty(param.getId(), "recoveryFirstTimeout", param.getRecoveryFirstTimeout(), time);
                    customerSeaDao.saveOrUpdate(recoveryFirstTimeout);
                }
                // 回收规则提醒
                CustomerSeaProperty recoveryRemind = new CustomerSeaProperty(param.getId(), "recoveryRemind", String.valueOf(param.getRecoveryRemind()), time);
                customerSeaDao.saveOrUpdate(recoveryRemind);
                status = 1;
            } else if (4 == param.getOperation()) {
                // 员工可见私海线索设置
                CustomerSeaProperty visibleDataType = new CustomerSeaProperty(param.getId(), "visibleDataType", String.valueOf(param.getVisibleDataType()), time);
                customerSeaDao.saveOrUpdate(visibleDataType);
                // 员工可见私海数据时长
                CustomerSeaProperty visibleDataTimeout = new CustomerSeaProperty(param.getId(), "visibleDataTimeout", String.valueOf(param.getVisibleDataTimeout()), time);
                customerSeaDao.saveOrUpdate(visibleDataTimeout);
                status = 1;
            }
        }
        return status;
    }

    /**
     * 公海分页
     *
     * @param param
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page page(CustomerSeaParam param, int pageNum, int pageSize) {
        Page page = customerSeaDao.pageCustomerSea(param, pageNum, pageSize);
        if (page.getData() != null && page.getData().size() > 0) {
            List<CustomerSeaDTO> list = new ArrayList<>();
            CustomerSea customerSea;
            CustomerSeaDTO dto;
            MarketProject marketProject;
            List<Map<String, Object>> projectManager;
            CustomerUser user;
            List<CustomerSeaProperty> properties;
            List<Map<String, Object>> stat;
            String statSql = "SELECT COUNT(status=0 OR null) sumCount,IFNULL(COUNT(super_data like ''%\"SYS007\":\"未跟进\"%'' AND status = 0 OR null),0) AS noFollowSum, IFNULL(COUNT(`status` = 1 OR null),0) AS clueSurplusSum, IFNULL(COUNT(`call_fail_count` >= 1 OR null),0) AS failCallSum FROM " + ConstantsUtil.SEA_TABLE_PREFIX + "{0} WHERE 1=1 ";
            MarketProjectProperty executionGroup;
            StringBuilder userGroupName;
            CustomerUserGroup customerUserGroup;
            for (int i = 0; i < page.getData().size(); i++) {
                customerSea = (CustomerSea) page.getData().get(i);
                dto = new CustomerSeaDTO(customerSea);
                //根据企业id查询企业名称
                String custId = customerSea.getCustId();
                if (StringUtil.isNotEmpty(custId)) {
                    String enterpriseName = customerDao.getEnterpriseName(custId);
                    dto.setCustName(enterpriseName);
                }

                // 查询所属项目
                marketProject = marketProjectDao.selectMarketProject(dto.getMarketProjectId());
                dto.setMarketProjectName("");
                if (marketProject != null) {
                    dto.setMarketProjectName(marketProject.getName());
                }
                // 处理公海属性
                properties = customerSeaDao.listProperty(String.valueOf(customerSea.getId()));
                dto.setProperty(new HashMap<>(16));
                if (properties != null && properties.size() > 0) {
                    LOG.info("开始处理property。。。");
                    JSONObject xzinfo = getXZChannelInfo(properties);
                    LOG.info("xzinfo..." + (xzinfo == null ? "" : xzinfo.toJSONString()));
                    for (CustomerSeaProperty p : properties) {
                        LOG.info("getPropertyName:" + p.getPropertyName() + ";v=" + p.getPropertyValue());
                        //LOG.info("callChannel".equals(p.getPropertyName()));
                        if ("xzTaskConfig".equals(p.getPropertyName())) {
                            String v = p.getPropertyValue();
                            LOG.info("vvvv==" + v);
                            JSONObject xzConfig = JSON.parseObject(v);
                            if (xzinfo != null) {
                                xzConfig.put("callCenterId", xzinfo.getString("callCenterId"));
                                xzConfig.put("call_center_account", xzinfo.getString("call_center_account"));
                                xzConfig.put("call_center_pwd", xzinfo.getString("call_center_pwd"));
                            }
                            dto.getProperty().put(p.getPropertyName(), xzConfig.toJSONString());
                        } else {
                            dto.getProperty().put(p.getPropertyName(), p.getPropertyValue());
                        }
                    }
                }
                // 判断是否为讯众自动任务
                dto.getProperty().put("callCenterType", "0");
                if (dto.getTaskType() != null && 1 == dto.getTaskType()) {
                    // 添加讯众自动外呼任务
                    CustomerSeaProperty property = customerSeaDao.getProperty(dto.getId(), "callChannel");
                    if (property != null) {
                        ResourcePropertyEntity mrp = marketResourceDao.getProperty(property.getPropertyValue(), "price_config");
                        if (mrp != null && StringUtil.isNotEmpty(mrp.getPropertyValue())) {
                            JSONObject callCenterConfig = JSON.parseObject(mrp.getPropertyValue());
                            dto.getProperty().put("callCenterType", callCenterConfig.getString("call_center_type"));
                        }
                    }
                }
                // 处理线索余量和累计未通量
                try {
                    StringBuilder appSql = new StringBuilder();
                    if ("2".equals(param.getUserType())) {
                        // 组长查组员列表
                        if ("1".equals(param.getUserGroupRole())) {
                            List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(param.getUserGroupId(), param.getCustId());
                            Set<String> userIds = new HashSet<>();
                            if (customerUserDTOList.size() > 0) {
                                for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                                    userIds.add(customerUserDTO.getId());
                                }
                                // 分配责任人操作
                                if (userIds.size() > 0) {
                                    appSql.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                                }
                            } else {
                                appSql.append(" AND user_id = '" + param.getUserId() + "' ");
                            }
                        } else {
                            appSql.append(" AND user_id = '" + param.getUserId() + "' ");
                        }
                    }
                    stat = customerSeaDao.sqlQuery(MessageFormat.format(statSql, String.valueOf(customerSea.getId())));
                    dto.setClueSurplusSum(NumberConvertUtil.parseLong(stat.get(0).get("clueSurplusSum")));
                    dto.setFailCallSum(NumberConvertUtil.parseLong(stat.get(0).get("failCallSum")));
                    // 查询私海未跟进线索量和线索总量
                    stat = customerSeaDao.sqlQuery(MessageFormat.format(statSql, String.valueOf(customerSea.getId())) + appSql.toString());
                    dto.setTotalSum(NumberConvertUtil.parseLong(stat.get(0).get("sumCount")));
                    dto.setNoFollowSum(NumberConvertUtil.parseLong(stat.get(0).get("noFollowSum")));
                } catch (SQLGrammarException e) {
                    LOG.error("查询线索余量和累计未通量异常,公海ID:" + customerSea.getId(), e);
                    dto.setClueSurplusSum(0L);
                    dto.setFailCallSum(0L);
                    dto.setTotalSum(0L);
                    dto.setNoFollowSum(0L);
                }
                // 处理项目执行组
                executionGroup = marketProjectDao.getProperty(String.valueOf(dto.getMarketProjectId()), "executionGroup");
                if (executionGroup != null && StringUtil.isNotEmpty(executionGroup.getPropertyValue())) {
                    dto.setUserGroupId(executionGroup.getPropertyValue());
                    userGroupName = new StringBuilder();
                    for (String groupId : executionGroup.getPropertyValue().split(",")) {
                        customerUserGroup = customerUserDao.getCustomerUserGroup(groupId);
                        if (customerUserGroup != null) {
                            userGroupName.append(customerUserGroup.getName()).append(",");
                        }
                    }
                    dto.setUserGroupName(userGroupName.deleteCharAt(userGroupName.length() - 1).toString());
                }

                // 处理项目管理员
                projectManager = projectService.getProjectManager(param.getCustId());
                if (projectManager != null && projectManager.size() > 0) {
                    for (Map<String, Object> map : projectManager) {
                        String projectIds = (String) map.get("property_value");
                        List<String> projectIdList = Arrays.asList(projectIds.split(","));
                        projectIdList.remove(",");
                        if (projectIdList.contains(String.valueOf(dto.getMarketProjectId()))) {
                            user = customerUserDao.get(NumberConvertUtil.parseLong(map.get("user_id")));
                            if (user != null) {
                                dto.setProjectUserId(String.valueOf(user.getId()));
                                dto.setProjectUserName(user.getAccount());
                            }
                            break;
                        }
                    }
                }
                list.add(dto);
            }
            page.setData(list);
        }
        return page;
    }

    private JSONObject getXZChannelInfo(List<CustomerSeaProperty> list) {
        LOG.info("开始获取callChannel");
        String channelId = null;
        for (CustomerSeaProperty property : list) {
            LOG.info(property.getPropertyName() + ";" + ("callChannel".equals(property.getPropertyName())));
            if ("callChannel".equals(property.getPropertyName())) {
                channelId = property.getPropertyValue();
                break;
            }
        }
        LOG.info("channelId==" + channelId);
        if (channelId == null) {
            LOG.info("channelId is null");
            return null;
        }
        MarketResourceDTO mr = marketResourceDao.getInfoProperty(NumberConvertUtil.parseInt(channelId), "price_config");
        LOG.info("mr=" + JSONObject.toJSONString(mr));
        if (mr != null && mr.getTypeCode() != null) {
            JSONObject priceConfig = JSON.parseObject(mr.getResourceProperty());
            LOG.info("priceConfig::" + mr.getResourceProperty());
            if (priceConfig != null) {
                LOG.info("TYPECODE:" + mr.getTypeCode());
                if (mr.getTypeCode() == 1) {
                    LOG.info("ssafa===" + priceConfig.getInteger("call_center_type"));
                    if (priceConfig.getIntValue("call_center_type") == 2) {
                        LOG.info(priceConfig.getInteger("call_center_type") + ";call_center_config: " + priceConfig.getString("call_center_config"));
                        String callCenterConfigStr = priceConfig.getString("call_center_config");
                        LOG.info("callCenterConfigStr:" + callCenterConfigStr);
                        return JSON.parseObject(callCenterConfigStr);
                    }
                }
            }
        }
        return null;
    }


    /**
     * 公海线索列表
     *
     * @param param
     * @return
     */
    public Page pagePublicClueData(CustomerSeaSearch param) {
        Page page;
        StringBuffer sb = new StringBuffer();
        CustomerSea customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(param.getSeaId()));
        sb.append(" select custG.id, custG.user_id, custG.status, custG.call_count callCount, DATE_FORMAT(custG.last_call_time,'%Y-%m-%d %H:%i:%s') lastCallTime, custG.intent_level intentLevel,");
        sb.append(" custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street, custG.super_data, ");
        sb.append(" custG.batch_id, custG.last_call_status, custG.data_source, DATE_FORMAT(custG.user_get_time,'%Y-%m-%d %H:%i:%s') user_get_time, DATE_FORMAT(custG.create_time,'%Y-%m-%d %H:%i:%s') create_time, custG.pre_user_id, custG.last_called_duration, DATE_FORMAT(custG.last_mark_time,'%Y-%m-%d %H:%i:%s') last_mark_time, ");
        sb.append(" custG.call_success_count, custG.call_fail_count, custG.sms_success_count ");
        sb.append("  from " + ConstantsUtil.SEA_TABLE_PREFIX + param.getSeaId() + " custG ");
        sb.append(" where 1=1 ");
        if (StringUtil.isNotEmpty(param.getSuperId())) {
            sb.append(" and custG.id = '" + param.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperName())) {
            sb.append(" and custG.super_name LIKE '%" + param.getSuperName() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getSuperPhone())) {
            sb.append(" and custG.super_phone = '" + param.getSuperPhone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperTelphone())) {
            sb.append(" and custG.super_telphone = '" + param.getSuperTelphone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getLastUserName())) {
            sb.append(" and custG.pre_user_id IN(SELECT id from t_customer_user WHERE AND cust_id = '" + param.getCustId() + "' realname LIKE '%" + param.getLastUserName() + "%') ");
        }
        if (param.getDataSource() != null) {
            sb.append(" and custG.data_source =" + param.getDataSource());
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            sb.append(" and custG.batch_id =" + param.getBatchId());
        }
        if (StringUtil.isNotEmpty(param.getAddStartTime()) && StringUtil.isNotEmpty(param.getAddEndTime())) {
            sb.append(" and custG.create_time BETWEEN " + param.getAddStartTime() + " AND " + param.getAddEndTime());
        } else if (StringUtil.isNotEmpty(param.getAddStartTime())) {
            sb.append(" and custG.create_time >= " + param.getAddStartTime());
        } else if (StringUtil.isNotEmpty(param.getAddEndTime())) {
            sb.append(" and custG.create_time <= " + param.getAddEndTime());
        }

        if (StringUtil.isNotEmpty(param.getCallStartTime()) && StringUtil.isNotEmpty(param.getCallEndTime())) {
            sb.append(" and custG.last_call_time BETWEEN " + param.getCallStartTime() + " AND " + param.getCallEndTime());
        } else if (StringUtil.isNotEmpty(param.getCallStartTime())) {
            sb.append(" and custG.last_call_time >= " + param.getCallStartTime());
        } else if (StringUtil.isNotEmpty(param.getCallEndTime())) {
            sb.append(" and custG.last_call_time <= " + param.getCallEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastCallResult())) {
            sb.append(" and custG.last_call_status = '" + param.getLastCallResult() + "'");
        }
        if (StringUtil.isNotEmpty(param.getIntentLevel())) {
            sb.append(" and custG.intent_level = '" + param.getIntentLevel() + "'");
        }
        if (param.getCalledDuration() != null) {
            if (param.getCalledDuration() == 1) {
                sb.append(" AND custG.last_called_duration<=3");
            } else if (param.getCalledDuration() == 2) {
                sb.append(" AND custG.last_called_duration>3 AND voicLog.last_called_duration<=6");
            } else if (param.getCalledDuration() == 3) {
                sb.append(" AND custG.last_called_duration>6 AND voicLog.last_called_duration<=12");
            } else if (param.getCalledDuration() == 4) {
                sb.append(" AND custG.last_called_duration>12 AND voicLog.last_called_duration<=30");
            } else if (param.getCalledDuration() == 5) {
                sb.append(" AND custG.last_called_duration>30 AND voicLog.last_called_duration<=60");
            } else if (param.getCalledDuration() == 6) {
                sb.append(" AND custG.last_called_duration>60");
            }
        }
        if ("2".equals(param.getUserType())) {
            // 普通员工只能查看无负责人的线索
            sb.append(" AND custG.user_id IS NULL ");
        }

        // 查询所有自建属性
        Map<String, CustomerLabel> cacheLabel = labelService.getCacheCustomAndSystemLabel(param.getCustId());
        if (StringUtil.isNotEmpty(param.getLabelProperty())) {
            JSONObject jsonObject;
            String labelId, optionValue, likeValue;
            JSONArray custProperty = JSON.parseArray(param.getLabelProperty());
            for (int i = 0; i < custProperty.size(); i++) {
                jsonObject = custProperty.getJSONObject(i);
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
                    sb.append(" AND custG.super_data LIKE '" + likeValue + "' ");
                }
            }
        }
        //sb.append(" AND custG.status<>2 ");
        sb.append(" AND custG.status =1 ");
        // 1-未呼通 2-已呼通
        if ("1".equals(param.getCallStatus())) {
            sb.append(" AND (custG.last_call_status <> '1001' OR custG.last_call_status IS NOT NULL)");
        } else if ("2".equals(param.getCallStatus())) {
            sb.append(" AND custG.last_call_status = '1001' ");
        }
        sb.append(" ORDER BY custG.create_time DESC ");
        try {
            page = customerSeaDao.sqlPageQuery0(sb.toString(), param.getPageNum(), param.getPageSize());
        } catch (Exception e) {
            LOG.error("查询公海线索列表失败,", e);
            return new Page();
        }
        if (page != null && page.getData() != null) {
            // 处理自建属性和基本信息
            handleClueData(page.getData(), cacheLabel, customerSea);
        }

        return page;
    }

    /**
     * 私海线索列表
     *
     * @param param
     * @return
     */
    public Page pagePrivateClueData(CustomerSeaSearch param) {
        Page page;
        String userId = String.valueOf(param.getUserId());
        StringBuffer sb = new StringBuffer();
        CustomerSea customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(param.getSeaId()));
        sb.append(" select custG.id, custG.user_id, custG.status, custG.call_count callCount, DATE_FORMAT(custG.last_call_time,'%Y-%m-%d %H:%i:%s') lastCallTime, custG.intent_level intentLevel,");
        sb.append(" custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street, custG.super_data, ");
        sb.append(" custG.batch_id, custG.last_call_status, custG.data_source, DATE_FORMAT(custG.user_get_time,'%Y-%m-%d %H:%i:%s') user_get_time, DATE_FORMAT(custG.create_time,'%Y-%m-%d %H:%i:%s') create_time, custG.pre_user_id, custG.last_called_duration, DATE_FORMAT(custG.last_mark_time,'%Y-%m-%d %H:%i:%s') last_mark_time, ");
        sb.append(" custG.call_success_count, custG.call_fail_count, custG.sms_success_count ");
        sb.append("  from " + ConstantsUtil.SEA_TABLE_PREFIX + param.getSeaId() + " custG ");
        sb.append(" where 1=1 ");
        if (StringUtil.isNotEmpty(param.getSuperId())) {
            sb.append(" and custG.id = '" + param.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperName())) {
            sb.append(" and custG.super_name LIKE '%" + param.getSuperName() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getSuperPhone())) {
            sb.append(" and custG.super_phone = '" + param.getSuperPhone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperTelphone())) {
            sb.append(" and custG.super_telphone = '" + param.getSuperTelphone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getLastUserName())) {
            sb.append(" and custG.pre_user_id IN(SELECT id from t_customer_user WHERE AND cust_id = '" + param.getCustId() + "' realname LIKE '%" + param.getLastUserName() + "%') ");
        }
        if (param.getDataSource() != null) {
            sb.append(" and custG.data_source =" + param.getDataSource());
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            sb.append(" and custG.batch_id =" + param.getBatchId());
        }
        if (StringUtil.isNotEmpty(param.getAddStartTime()) && StringUtil.isNotEmpty(param.getAddEndTime())) {
            sb.append(" and custG.create_time BETWEEN " + param.getAddStartTime() + " AND " + param.getAddEndTime());
        } else if (StringUtil.isNotEmpty(param.getAddStartTime())) {
            sb.append(" and custG.create_time >= " + param.getAddStartTime());
        } else if (StringUtil.isNotEmpty(param.getAddEndTime())) {
            sb.append(" and custG.create_time <= " + param.getAddEndTime());
        }

        if (StringUtil.isNotEmpty(param.getCallStartTime()) && StringUtil.isNotEmpty(param.getCallEndTime())) {
            sb.append(" and custG.last_call_time BETWEEN " + param.getCallStartTime() + " AND " + param.getCallEndTime());
        } else if (StringUtil.isNotEmpty(param.getCallStartTime())) {
            sb.append(" and custG.last_call_time >= " + param.getCallStartTime());
        } else if (StringUtil.isNotEmpty(param.getCallEndTime())) {
            sb.append(" and custG.last_call_time <= " + param.getCallEndTime());
        }

        if (StringUtil.isNotEmpty(param.getUserGetStartTime()) && StringUtil.isNotEmpty(param.getUserGetEndTime())) {
            sb.append(" and custG.user_get_time BETWEEN " + param.getUserGetStartTime() + " AND " + param.getUserGetEndTime());
        } else if (StringUtil.isNotEmpty(param.getUserGetStartTime())) {
            sb.append(" and custG.user_get_time >= " + param.getUserGetStartTime());
        } else if (StringUtil.isNotEmpty(param.getUserGetEndTime())) {
            sb.append(" and custG.user_get_time <= " + param.getUserGetEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastMarkStartTime()) && StringUtil.isNotEmpty(param.getLastMarkEndTime())) {
            sb.append(" and custG.last_mark_time BETWEEN " + param.getLastMarkStartTime() + " AND " + param.getLastMarkEndTime());
        } else if (StringUtil.isNotEmpty(param.getLastMarkStartTime())) {
            sb.append(" and custG.last_mark_time >= " + param.getLastMarkStartTime());
        } else if (StringUtil.isNotEmpty(param.getLastMarkEndTime())) {
            sb.append(" and custG.last_mark_time <= " + param.getLastMarkEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastCallResult())) {
            sb.append(" and custG.last_call_status = '" + param.getLastCallResult() + "'");
        }
        if (StringUtil.isNotEmpty(param.getIntentLevel())) {
            sb.append(" and custG.intent_level = '" + param.getIntentLevel() + "'");
        }
        if (param.getCalledDuration() != null) {
            if (param.getCalledDuration() == 1) {
                sb.append(" AND custG.last_called_duration<=3");
            } else if (param.getCalledDuration() == 2) {
                sb.append(" AND custG.last_called_duration>3 AND voicLog.last_called_duration<=6");
            } else if (param.getCalledDuration() == 3) {
                sb.append(" AND custG.last_called_duration>6 AND voicLog.last_called_duration<=12");
            } else if (param.getCalledDuration() == 4) {
                sb.append(" AND custG.last_called_duration>12 AND voicLog.last_called_duration<=30");
            } else if (param.getCalledDuration() == 5) {
                sb.append(" AND custG.last_called_duration>30 AND voicLog.last_called_duration<=60");
            } else if (param.getCalledDuration() == 6) {
                sb.append(" AND custG.last_called_duration>60");
            }
        }
        JSONArray custProperty = JSON.parseArray(param.getLabelProperty());
        // 跟进状态为全部线索时查询组员的线索
        boolean allQuery = true;
        if (custProperty != null && custProperty.size() > 0) {
            for (int i = 0; i < custProperty.size(); i++) {
                JSONObject jsonObject = custProperty.getJSONObject(i);
                if (jsonObject != null && "SYS007".equals(jsonObject.getString("labelId"))) {
                    allQuery = false;
                    break;
                }
            }
        }

        if ("2".equals(param.getUserType())) {
            // 组长查组员列表
            if ("1".equals(param.getUserGroupRole()) && allQuery) {
                List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(param.getUserGroupId(), param.getCustId());
                Set<String> userIds = new HashSet<>();
                if (customerUserDTOList.size() > 0) {
                    for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                        userIds.add(customerUserDTO.getId());
                    }
                    // 分配责任人操作
                    if (userIds.size() > 0) {
                        if ("distribution".equals(param.getAction())) {
                            sb.append(" AND (custG.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR custG.status= 1)");
                        } else {
                            sb.append(" AND custG.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }
                    } else {
                        sb.append(" AND custG.user_id = '" + userId + "'");
                    }
                }
            } else {
                sb.append(" AND custG.user_id = '" + userId + "'");
            }
        } else {
            //管理员查看所有
            sb.append(" AND custG.status=0 ");
        }

        // 查询所有自建属性
        Map<String, CustomerLabel> cacheLabel = labelService.getCacheCustomAndSystemLabel(param.getCustId());
        if (StringUtil.isNotEmpty(param.getLabelProperty()) && custProperty != null && custProperty.size() > 0) {
            JSONObject jsonObject;
            String labelId, optionValue, likeValue;
            for (int i = 0; i < custProperty.size(); i++) {
                jsonObject = custProperty.getJSONObject(i);
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
                    sb.append(" AND custG.super_data LIKE '" + likeValue + "' ");
                }
            }
        }

        sb.append(" AND custG.status<>2 ");
        // 1-未呼通 2-已呼通
        if ("1".equals(param.getCallStatus())) {
            sb.append(" AND (custG.last_call_status <> '1001' OR custG.last_call_status IS NOT NULL)");
        } else if ("2".equals(param.getCallStatus())) {
            sb.append(" AND custG.last_call_status = '1001' ");
        }
        // 跟进状态处理
        if (StringUtil.isNotEmpty(param.getFollowStatus()) && StringUtil.isNotEmpty(param.getFollowValue())) {
            String likeValue = "%\"" + param.getFollowStatus() + "\":\"" + param.getFollowValue() + "\"%";
            sb.append(" AND custG.super_data LIKE '" + likeValue + "' ");
        }
        // 无效原因处理
        if (StringUtil.isNotEmpty(param.getInvalidReason()) && StringUtil.isNotEmpty(param.getInvalidReason())) {
            String likeValue = "%\"SYS006\":\"" + param.getFollowValue() + "\"%";
            sb.append(" AND custG.super_data LIKE '" + likeValue + "' ");
        }
        // 呼叫次数
        if (param.getCallCount() != null) {
            if (param.getCallCount() < 8) {
                sb.append(" and custG.call_count = '" + param.getCallCount() + "'");
            } else {
                sb.append(" and custG.call_count >= '" + param.getCallCount() + "'");
            }
        }
        // 接通次数
        if (param.getCallSuccessCount() != null) {
            if (param.getCallCount() < 8) {
                sb.append(" and custG.call_success_count = '" + param.getCallSuccessCount() + "'");
            } else {
                sb.append(" and custG.call_success_count >= '" + param.getCallSuccessCount() + "'");
            }
        }
        // 处理员工限制线索条件
        CustomerSeaProperty cp = customerSeaDao.getProperty(String.valueOf(customerSea.getId()), "visibleDataType");
        if (cp != null && Objects.equals(cp.getPropertyValue(), "2")) {
            cp = customerSeaDao.getProperty(String.valueOf(customerSea.getId()), "visibleDataTimeout");
            if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue())) {
                // N天前的0点时间
                LocalDateTime time = LocalDateTime.now().minusDays(NumberConvertUtil.parseLong(cp.getPropertyValue())).withHour(0).withMinute(0).withSecond(0).withNano(0);
                DateTimeFormatter ymd = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                sb.append(" AND ( update_time >= '" + ymd.format(time) + "' OR update_time is null)  ")
                        .append(" AND ( user_get_time >= '" + ymd.format(time) + "' OR user_get_time is null)  ")
                        .append(" AND ( last_call_time >= '" + ymd.format(time) + "' OR last_call_time is null)  ")
                        .append(" AND ( last_mark_time >= '" + ymd.format(time) + "' OR last_mark_time is null)  ");
            } else {
                LOG.warn("公海ID:[" + customerSea.getTaskId() + "]员工限制超时时间配置为空");
                return new Page();
            }
        }
        sb.append(" ORDER BY custG.create_time DESC ");
        try {
            page = customerSeaDao.sqlPageQueryByPageSize(sb.toString(), param.getPageNum(), param.getPageSize());
        } catch (Exception e) {
            LOG.error("查询私海线索列表失败,", e);
            return new Page();
        }
        if (page != null && page.getData() != null) {
            // 处理自建属性和基本信息
            handleClueData(page.getData(), cacheLabel, customerSea);
        }
        return page;
    }


    /**
     * 处理线索superData和基础信息
     *
     * @param list
     * @param cacheLabel
     * @param customerSea
     */
    private void handleClueData(List<Map<String, Object>> list, Map<String, CustomerLabel> cacheLabel, CustomerSea customerSea) {
        Map<String, Object> map, superData, labelData;
        List<Map<String, Object>> labelList;
        CustomerUser user;
        boolean protectStatus = customerService.getProtectStatusByCust(2, customerSea.getCustId());
        CustomerLabel label;
        for (int i = 0; i < list.size(); i++) {
            map = list.get(i);
            if (map == null) continue;
            // 处理自建属性数据
            if (map.get("super_data") != null
                    && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                labelList = new ArrayList<>();
                superData = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                if (superData != null && superData.size() > 0) {
                    for (Map.Entry<String, Object> key : superData.entrySet()) {
                        labelData = new HashMap<>();
                        label = cacheLabel.get(key.getKey());
                        labelData.put("id", key.getKey());
                        labelData.put("name", "");
                        labelData.put("type", 0);
                        labelData.put("value", "");
                        if (label != null) {
                            labelData.put("name", label.getLabelName());
                            labelData.put("type", label.getType());
                            labelData.put("value", key.getValue());
                            if (label.getType() != null && 1 != label.getType()) {
                                labelData.put("value", String.valueOf(key.getValue()).split(","));
                            }
                        }
                        labelList.add(labelData);
                    }
                    map.put("labelList", labelList);
                }
            }
            // 查询用户真实姓名
            map.put("realname", "");
            if (map.get("user_id") != null) {
                user = customerUserDao.get(Long.parseLong(String.valueOf(map.get("user_id"))));
                if (user != null) {
                    map.put("realname", user.getAccount());
                    map.put("user_id", String.valueOf(user.getId()));
                }
                map.put("user_id", String.valueOf(map.get("user_id")));
            }
            // 处理意向度为空的情况
            if (map.get("intentLevel") == null) {
                map.put("intentLevel", "");
            }
            // 解析super_data中qq 微信等属性
            getDefaultLabelValue(map);
            map.remove("super_data");
            map.put("protectStatus", protectStatus);
            // 处理手机号
          /*  if (protectStatus) {
                map.put("super_telphone", phoneService.hidePhone(String.valueOf(map.get("super_telphone"))));
            }*/
        }
    }

    /**
     * 批量根据身份ID删除线索(status=2)
     *
     * @param userId
     * @param seaId
     * @param superIds
     * @return
     */
    private int batchDeleteClue(Long userId, String userType, String seaId, List<String> superIds, String reason, String remark) {
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                .append(" SET status = 2 WHERE status <>2 AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
        // 保存转交记录
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" (`user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`,  `create_time`,reason,remark) ")
                .append(" SELECT ?, id, ?, batch_id, ?, ?, ?, ? ")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE status <>2 AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
        //员工只能处理负责人为自己的数据
        if ("2".equals(userType)) {
            sql.append(" AND user_id = ").append(userId);
            logSql.append(" AND user_id = ").append(userId);
        }
        customerSeaDao.executeUpdateSQL(logSql.toString(), userId, seaId, 8, new Timestamp(System.currentTimeMillis()), reason, remark);
        int status = customerSeaDao.executeUpdateSQL(sql.toString());
        return status;
    }

    /**
     * 根据指定条件删除线索
     *
     * @param param
     * @return
     */
    private int batchDeleteClueByCondition(CustomerSeaSearch param) {
        // 根据指定条件删除线索
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" custG SET custG.status = 2 WHERE custG.status <>2 ");
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append("( `user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`, `create_time`, reason, remark) ")
                .append(" SELECT ").append(param.getUserId()).append(" ,id,").append(param.getSeaId()).append(",batch_id,").append(8).append(",").append(new Timestamp(System.currentTimeMillis())).append(" ,? ,? ")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId()).append(" custG WHERE custG.status <>2 ");
        StringBuilder appSql = new StringBuilder();
        if (StringUtil.isNotEmpty(param.getSuperId())) {
            appSql.append(" and custG.id = '" + param.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperName())) {
            appSql.append(" and custG.super_name = '%" + param.getSuperName() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getSuperPhone())) {
            appSql.append(" and custG.super_phone = '" + param.getSuperPhone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperTelphone())) {
            appSql.append(" and custG.super_telphone = '" + param.getSuperTelphone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getLastUserName())) {
            appSql.append(" and custG.pre_user_id IN(SELECT id from t_customer_user WHERE AND cust_id = '" + param.getCustId() + "' realname LIKE '%" + param.getLastUserName() + "%') ");
        }
        if (param.getDataSource() != null) {
            appSql.append(" and custG.data_source =" + param.getDataSource());
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            appSql.append(" and custG.batch_id =" + param.getBatchId());
            logSql.append(" and batch_id =" + param.getBatchId());
        }
        if (StringUtil.isNotEmpty(param.getAddStartTime()) && StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time BETWEEN " + param.getAddStartTime() + " AND " + param.getAddEndTime());
        } else if (StringUtil.isNotEmpty(param.getAddStartTime())) {
            appSql.append(" and custG.create_time >= " + param.getAddStartTime());
        } else if (StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time <= " + param.getAddEndTime());
        }

        if (StringUtil.isNotEmpty(param.getCallStartTime()) && StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time BETWEEN " + param.getCallStartTime() + " AND " + param.getCallEndTime());
        } else if (StringUtil.isNotEmpty(param.getCallStartTime())) {
            appSql.append(" and custG.last_call_time >= " + param.getCallStartTime());
        } else if (StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time <= " + param.getCallEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastCallResult())) {
            appSql.append(" and custG.last_call_status = '" + param.getLastCallResult() + "'");
        }
        if (StringUtil.isNotEmpty(param.getIntentLevel())) {
            appSql.append(" and custG.intent_level = '" + param.getIntentLevel() + "'");
        }
        if (param.getCalledDuration() != null) {
            if (param.getCalledDuration() == 1) {
                appSql.append(" AND custG.last_called_duration<=3");
            } else if (param.getCalledDuration() == 2) {
                appSql.append(" AND custG.last_called_duration>3 AND custG.last_called_duration<=6");
            } else if (param.getCalledDuration() == 3) {
                appSql.append(" AND custG.last_called_duration>6 AND custG.last_called_duration<=12");
            } else if (param.getCalledDuration() == 4) {
                appSql.append(" AND custG.last_called_duration>12 AND custG.last_called_duration<=30");
            } else if (param.getCalledDuration() == 5) {
                appSql.append(" AND custG.last_called_duration>30 AND custG.last_called_duration<=60");
            } else if (param.getCalledDuration() == 6) {
                appSql.append(" AND custG.last_called_duration>60");
            }
        }
        // 查询所有自建属性
        Map<String, CustomerLabel> cacheLabel = labelService.getCacheCustomAndSystemLabel(param.getCustId());
        if (StringUtil.isNotEmpty(param.getLabelProperty())) {
            JSONObject jsonObject;
            String labelId, optionValue, likeValue;
            JSONArray custProperty = JSON.parseArray(param.getLabelProperty());
            for (int i = 0; i < custProperty.size(); i++) {
                jsonObject = custProperty.getJSONObject(i);
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
                    appSql.append(" AND custG.super_data LIKE '" + likeValue + "' ");
                }
            }
        }
        // 跟进状态处理
        if (StringUtil.isNotEmpty(param.getFollowStatus()) && StringUtil.isNotEmpty(param.getFollowValue())) {
            String likeValue = "%\"" + param.getFollowStatus() + "\":\"" + param.getFollowValue() + "\"%";
            appSql.append(" AND custG.super_data LIKE '" + likeValue + "' ");
        }
        // 保存转交记录
        customerSeaDao.executeUpdateSQL(logSql.toString() + appSql.toString(), param.getBackReason(), param.getBackRemark());
        int status = customerSeaDao.executeUpdateSQL(sql.toString() + appSql.toString());
        return status;
    }

    /**
     * 指定ID退回公海
     *
     * @param userId
     * @param userType
     * @param seaId
     * @param superIds
     * @return
     */
    private int batchClueBackToSea(Long userId, String userType, String seaId, List<String> superIds, String reason, String remark) {
        // 指定ID退回公海
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                .append(" SET status = 1, pre_user_id = user_id, user_id = NULL, super_data = '{\"SYS007\":\"未跟进\"}' WHERE status = 0  AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" (`user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`, object_code, `create_time`,reason,remark ) ")
                .append(" SELECT ").append(userId).append(" ,id,").append(seaId).append(",batch_id,").append(7).append(", user_id ,'").append(new Timestamp(System.currentTimeMillis())).append("'").append(" ,? ,? ")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE status = 0  AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
        //员工只能处理负责人为自己的数据
        if ("2".equals(userType)) {
            sql.append(" AND user_id = ").append(userId);
            logSql.append(" AND user_id = ").append(userId);
        }
        customerSeaDao.executeUpdateSQL(logSql.toString(), reason, remark);
        int status = customerSeaDao.executeUpdateSQL(sql.toString());
        return status;
    }

    /**
     * 指定搜索条件退回公海
     *
     * @param param
     * @return
     */
    private int batchClueBackToSeaByCondition(CustomerSeaSearch param) {
        // 指定搜索条件退回公海
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" SET status = 1, pre_user_id = user_id ,user_id = NULL, super_data = '{\"SYS007\":\"未跟进\"}'  WHERE status = 0 ");
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" (`user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`, object_code, `create_time`, reason, remark) ")
                .append(" SELECT ").append(param.getUserId()).append(" ,id,").append(param.getSeaId()).append(",batch_id,").append(7).append(", user_id ,").append(new Timestamp(System.currentTimeMillis())).append(" ,?,? ")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId()).append(" WHERE status = 0 ");
        StringBuilder appSql = new StringBuilder();
        //员工只能处理负责人为自己的数据
        if ("2".equals(param.getUserType())) {
            appSql.append(" AND custG.user_id = ").append(param.getUserId());
        }
        if (StringUtil.isNotEmpty(param.getSuperId())) {
            appSql.append(" and custG.id = '" + param.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperName())) {
            appSql.append(" and custG.super_name = '%" + param.getSuperName() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getSuperPhone())) {
            appSql.append(" and custG.super_phone = '" + param.getSuperPhone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperTelphone())) {
            appSql.append(" and custG.super_telphone = '" + param.getSuperTelphone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getLastUserName())) {
            appSql.append(" and custG.pre_user_id IN(SELECT id from t_customer_user WHERE AND cust_id = '" + param.getCustId() + "' realname LIKE '%" + param.getLastUserName() + "%') ");
        }
        if (param.getDataSource() != null) {
            appSql.append(" and custG.data_source =" + param.getDataSource());
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            appSql.append(" and custG.batch_id =" + param.getBatchId());
        }
        if (StringUtil.isNotEmpty(param.getAddStartTime()) && StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time BETWEEN " + param.getAddStartTime() + " AND " + param.getAddEndTime());
        } else if (StringUtil.isNotEmpty(param.getAddStartTime())) {
            appSql.append(" and custG.create_time >= " + param.getAddStartTime());
        } else if (StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time <= " + param.getAddEndTime());
        }

        if (StringUtil.isNotEmpty(param.getCallStartTime()) && StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time BETWEEN " + param.getCallStartTime() + " AND " + param.getCallEndTime());
        } else if (StringUtil.isNotEmpty(param.getCallStartTime())) {
            appSql.append(" and custG.last_call_time >= " + param.getCallStartTime());
        } else if (StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time <= " + param.getCallEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastCallResult())) {
            appSql.append(" and custG.last_call_status = '" + param.getLastCallResult() + "'");
        }
        if (StringUtil.isNotEmpty(param.getIntentLevel())) {
            appSql.append(" and custG.intent_level = '" + param.getIntentLevel() + "'");
        }
        if (param.getCalledDuration() != null) {
            if (param.getCalledDuration() == 1) {
                appSql.append(" AND custG.last_called_duration<=3");
            } else if (param.getCalledDuration() == 2) {
                appSql.append(" AND custG.last_called_duration>3 AND custG.last_called_duration<=6");
            } else if (param.getCalledDuration() == 3) {
                appSql.append(" AND custG.last_called_duration>6 AND custG.last_called_duration<=12");
            } else if (param.getCalledDuration() == 4) {
                appSql.append(" AND custG.last_called_duration>12 AND custG.last_called_duration<=30");
            } else if (param.getCalledDuration() == 5) {
                appSql.append(" AND custG.last_called_duration>30 AND custG.last_called_duration<=60");
            } else if (param.getCalledDuration() == 6) {
                appSql.append(" AND custG.last_called_duration>60");
            }
        }
        // 查询所有自建属性
        Map<String, CustomerLabel> cacheLabel = labelService.getCacheCustomAndSystemLabel(param.getCustId());
        if (StringUtil.isNotEmpty(param.getLabelProperty())) {
            JSONObject jsonObject;
            String labelId, optionValue, likeValue;
            JSONArray custProperty = JSON.parseArray(param.getLabelProperty());
            for (int i = 0; i < custProperty.size(); i++) {
                jsonObject = custProperty.getJSONObject(i);
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
                    appSql.append(" AND custG.super_data LIKE '" + likeValue + "' ");
                }
            }
        }
        // 跟进状态处理
        if (StringUtil.isNotEmpty(param.getFollowStatus()) && StringUtil.isNotEmpty(param.getFollowValue())) {
            String likeValue = "%\"" + param.getFollowStatus() + "\":\"" + param.getFollowValue() + "\"%";
            appSql.append(" AND custG.super_data LIKE '" + likeValue + "' ");
        }
        // 保存转交记录
        customerSeaDao.executeUpdateSQL(logSql.toString() + appSql.toString(), param.getBackReason(), param.getBackRemark());
        int status = customerSeaDao.executeUpdateSQL(sql.toString() + appSql.toString());
        return status;
    }

    /**
     * 指定身份ID转交线索
     *
     * @param fromUserId
     * @param toUserId
     * @param userType
     * @param seaId
     * @param superIds
     * @return
     */
    private int batchClueTransfer(Long fromUserId, String toUserId, String userType, String seaId, List<String> superIds) {
        // 指定身份ID转交线索
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                .append(" SET pre_user_id = user_id, user_id = ? ,status = 0, super_data = '{\"SYS007\":\"未跟进\"}' WHERE id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" (`user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`, object_code, `create_time`) ")
                .append(" SELECT ").append(fromUserId).append(" ,id,").append(seaId).append(",batch_id,").append(6).append(", ? ,'").append(new Timestamp(System.currentTimeMillis())).append("'")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
        //员工只能处理负责人为自己的数据
        if ("2".equals(userType)) {
            sql.append(" AND user_id = ").append(fromUserId);
            logSql.append(" AND user_id = ").append(fromUserId);
        }
        customerSeaDao.executeUpdateSQL(logSql.toString(), toUserId);
        int status = customerSeaDao.executeUpdateSQL(sql.toString(), toUserId);
        return status;
    }

    /**
     * 指定搜索条件转交线索
     *
     * @param param
     * @return
     */
    private int batchClueTransferByCondition(CustomerSeaSearch param) {
        // 指定搜索条件转交线索
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" SET pre_user_id = user_id ,user_id = ?, super_data = '{\"SYS007\":\"未跟进\"}' WHERE 1=1 ");
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append("( `user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`, object_code, `create_time`) ")
                .append(" SELECT ").append(param.getUserId()).append(" ,id,").append(param.getSeaId()).append(",batch_id,").append(6).append(", ? ,").append(new Timestamp(System.currentTimeMillis()))
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId()).append(" WHERE status = 0 ");
        StringBuilder appSql = new StringBuilder();
        //员工只能处理负责人为自己的数据
        if ("2".equals(param.getUserType())) {
            appSql.append(" AND custG.user_id = ").append(param.getUserId());
        }
        if (StringUtil.isNotEmpty(param.getSuperId())) {
            appSql.append(" and custG.id = '" + param.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperName())) {
            appSql.append(" and custG.super_name = '%" + param.getSuperName() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getSuperPhone())) {
            appSql.append(" and custG.super_phone = '" + param.getSuperPhone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperTelphone())) {
            appSql.append(" and custG.super_telphone = '" + param.getSuperTelphone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getLastUserName())) {
            appSql.append(" and custG.pre_user_id IN(SELECT id from t_customer_user WHERE AND cust_id = '" + param.getCustId() + "' realname LIKE '%" + param.getLastUserName() + "%') ");
        }
        if (param.getDataSource() != null) {
            appSql.append(" and custG.data_source =" + param.getDataSource());
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            appSql.append(" and custG.batch_id =" + param.getBatchId());
        }
        if (StringUtil.isNotEmpty(param.getAddStartTime()) && StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time BETWEEN " + param.getAddStartTime() + " AND " + param.getAddEndTime());
        } else if (StringUtil.isNotEmpty(param.getAddStartTime())) {
            appSql.append(" and custG.create_time >= " + param.getAddStartTime());
        } else if (StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time <= " + param.getAddEndTime());
        }

        if (StringUtil.isNotEmpty(param.getCallStartTime()) && StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time BETWEEN " + param.getCallStartTime() + " AND " + param.getCallEndTime());
        } else if (StringUtil.isNotEmpty(param.getCallStartTime())) {
            appSql.append(" and custG.last_call_time >= " + param.getCallStartTime());
        } else if (StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time <= " + param.getCallEndTime());
        }

        if (StringUtil.isNotEmpty(param.getUserGetStartTime()) && StringUtil.isNotEmpty(param.getUserGetEndTime())) {
            appSql.append(" and custG.user_get_time BETWEEN " + param.getUserGetStartTime() + " AND " + param.getUserGetEndTime());
        } else if (StringUtil.isNotEmpty(param.getUserGetStartTime())) {
            appSql.append(" and custG.user_get_time >= " + param.getUserGetStartTime());
        } else if (StringUtil.isNotEmpty(param.getUserGetEndTime())) {
            appSql.append(" and custG.user_get_time <= " + param.getUserGetEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastMarkStartTime()) && StringUtil.isNotEmpty(param.getLastMarkEndTime())) {
            appSql.append(" and custG.last_mark_time BETWEEN " + param.getLastMarkStartTime() + " AND " + param.getLastMarkEndTime());
        } else if (StringUtil.isNotEmpty(param.getLastMarkStartTime())) {
            appSql.append(" and custG.last_mark_time >= " + param.getLastMarkStartTime());
        } else if (StringUtil.isNotEmpty(param.getLastMarkEndTime())) {
            appSql.append(" and custG.last_mark_time <= " + param.getLastMarkEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastCallResult())) {
            appSql.append(" and custG.last_call_status = '" + param.getLastCallResult() + "'");
        }
        if (StringUtil.isNotEmpty(param.getIntentLevel())) {
            appSql.append(" and custG.intent_level = '" + param.getIntentLevel() + "'");
        }
        if (param.getCalledDuration() != null) {
            if (param.getCalledDuration() == 1) {
                appSql.append(" AND custG.last_called_duration<=3");
            } else if (param.getCalledDuration() == 2) {
                appSql.append(" AND custG.last_called_duration>3 AND custG.last_called_duration<=6");
            } else if (param.getCalledDuration() == 3) {
                appSql.append(" AND custG.last_called_duration>6 AND custG.last_called_duration<=12");
            } else if (param.getCalledDuration() == 4) {
                appSql.append(" AND custG.last_called_duration>12 AND custG.last_called_duration<=30");
            } else if (param.getCalledDuration() == 5) {
                appSql.append(" AND custG.last_called_duration>30 AND custG.last_called_duration<=60");
            } else if (param.getCalledDuration() == 6) {
                appSql.append(" AND custG.last_called_duration>60");
            }
        }
        if (param.getCallCount() != null) {
            appSql.append(" and custG.call_count = '" + param.getCallCount() + "'");
        }
        if (param.getCallSuccessCount() != null) {
            appSql.append(" and custG.call_success_count = '" + param.getCallSuccessCount() + "'");
        }
        // 查询所有自建属性
        Map<String, CustomerLabel> cacheLabel = labelService.getCacheCustomAndSystemLabel(param.getCustId());
        if (StringUtil.isNotEmpty(param.getLabelProperty())) {
            JSONObject jsonObject;
            String labelId, optionValue, likeValue;
            JSONArray custProperty = JSON.parseArray(param.getLabelProperty());
            for (int i = 0; i < custProperty.size(); i++) {
                jsonObject = custProperty.getJSONObject(i);
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
                    appSql.append(" AND custG.super_data LIKE '" + likeValue + "' ");
                }
            }
        }
        // 跟进状态处理
        if (StringUtil.isNotEmpty(param.getFollowStatus()) && StringUtil.isNotEmpty(param.getFollowValue())) {
            String likeValue = "%\"" + param.getFollowStatus() + "\":\"" + param.getFollowValue() + "\"%";
            appSql.append(" AND custG.super_data LIKE '" + likeValue + "' ");
        }
        // 保存转交记录
        customerSeaDao.executeUpdateSQL(logSql.toString() + appSql.toString(), param.getClueToUserId());
        int status = customerSeaDao.executeUpdateSQL(sql.toString() + appSql.toString(), param.getClueToUserId());
        return status;
    }

    /**
     * 批量更改跟进状态异常
     *
     * @param userId
     * @param labelId
     * @param labelOption
     * @param seaId
     * @param list
     * @return
     */
    private int batchClueFollowStatus(Long userId, String labelId, String labelOption, String seaId, List<Map<String, Object>> list) {
        String sql = "UPDATE " + ConstantsUtil.SEA_TABLE_PREFIX + seaId + " SET super_data = ? WHERE id = ?;";
        String superData;
        JSONObject jsonObject;
        List<Object[]> batchArgs = new ArrayList<>();
        Object[] params;
        for (int i = 0; i < list.size(); i++) {
            superData = String.valueOf(list.get(i).get("super_data"));
            jsonObject = JSON.parseObject(superData);
            if (jsonObject == null) {
                jsonObject = new JSONObject();
            }
            jsonObject.put(labelId, labelOption);
            list.get(i).put("super_data", jsonObject);
            params = new Object[]{String.valueOf(list.get(i).get("super_data")), String.valueOf(list.get(i).get("id"))};
            batchArgs.add(params);
        }
        int[] ints = null;
        try {
            ints = jdbcTemplate.batchUpdate(sql, batchArgs);
        } catch (DataAccessException e) {
            LOG.error("批量更改跟进状态异常:", e);
        }
        return ints != null ? ints.length : 0;
    }

    /**
     * 指定身份ID批量变更跟进状态
     *
     * @param userId
     * @param labelId
     * @param labelOption
     * @param userType
     * @param seaId
     * @param superIds
     * @return
     */
    private int batchClueFollowStatus(Long userId, String labelId, String labelOption, String userType, String seaId, List<String> superIds) {
        // 指定身份ID批量变更跟进状态
        StringBuilder sql = new StringBuilder()
                .append(" SELECT id,batch_id,super_data FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                .append(" WHERE id IN (").append(SqlAppendUtil.sqlAppendWhereIn(superIds)).append(")");
        //员工只能处理负责人为自己的数据
        if ("2".equals(userType)) {
            sql.append(" AND user_id = ").append(userId);
        }
        List<Map<String, Object>> list = customerSeaDao.sqlQuery(sql.toString());
        int status = batchClueFollowStatus(userId, labelId, labelOption, seaId, list);
        return status;
    }

    /**
     * 指定搜索条件批量变更跟进状态
     *
     * @param param
     * @return
     */
    private int batchClueFollowStatusByCondition(CustomerSeaSearch param) {
        // 指定搜索条件批量变更跟进状态
        StringBuilder sql = new StringBuilder()
                .append(" SELECT id,batch_id,super_data FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" WHERE 1=1 ");
        StringBuilder appSql = new StringBuilder();
        //员工只能处理负责人为自己的数据
        if ("2".equals(param.getUserType())) {
            appSql.append(" AND custG.user_id = ").append(param.getUserId());
        }
        if (StringUtil.isNotEmpty(param.getSuperId())) {
            appSql.append(" and custG.id = '" + param.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperName())) {
            appSql.append(" and custG.super_name = '%" + param.getSuperName() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getSuperPhone())) {
            appSql.append(" and custG.super_phone = '" + param.getSuperPhone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperTelphone())) {
            appSql.append(" and custG.super_telphone = '" + param.getSuperTelphone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getLastUserName())) {
            appSql.append(" and custG.pre_user_id IN(SELECT id from t_customer_user WHERE AND cust_id = '" + param.getCustId() + "' realname LIKE '%" + param.getLastUserName() + "%') ");
        }
        if (param.getDataSource() != null) {
            appSql.append(" and custG.data_source =" + param.getDataSource());
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            appSql.append(" and custG.batch_id =" + param.getBatchId());
        }
        if (StringUtil.isNotEmpty(param.getAddStartTime()) && StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time BETWEEN " + param.getAddStartTime() + " AND " + param.getAddEndTime());
        } else if (StringUtil.isNotEmpty(param.getAddStartTime())) {
            appSql.append(" and custG.create_time >= " + param.getAddStartTime());
        } else if (StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time <= " + param.getAddEndTime());
        }

        if (StringUtil.isNotEmpty(param.getCallStartTime()) && StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time BETWEEN " + param.getCallStartTime() + " AND " + param.getCallEndTime());
        } else if (StringUtil.isNotEmpty(param.getCallStartTime())) {
            appSql.append(" and custG.last_call_time >= " + param.getCallStartTime());
        } else if (StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time <= " + param.getCallEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastCallResult())) {
            appSql.append(" and custG.last_call_status = '" + param.getLastCallResult() + "'");
        }
        if (StringUtil.isNotEmpty(param.getIntentLevel())) {
            appSql.append(" and custG.intent_level = '" + param.getIntentLevel() + "'");
        }
        if (param.getCalledDuration() != null) {
            if (param.getCalledDuration() == 1) {
                appSql.append(" AND custG.last_called_duration<=3");
            } else if (param.getCalledDuration() == 2) {
                appSql.append(" AND custG.last_called_duration>3 AND custG.last_called_duration<=6");
            } else if (param.getCalledDuration() == 3) {
                appSql.append(" AND custG.last_called_duration>6 AND custG.last_called_duration<=12");
            } else if (param.getCalledDuration() == 4) {
                appSql.append(" AND custG.last_called_duration>12 AND custG.last_called_duration<=30");
            } else if (param.getCalledDuration() == 5) {
                appSql.append(" AND custG.last_called_duration>30 AND custG.last_called_duration<=60");
            } else if (param.getCalledDuration() == 6) {
                appSql.append(" AND custG.last_called_duration>60");
            }
        }
        if (param.getCallCount() != null) {
            appSql.append(" and custG.call_count = '" + param.getCallCount() + "'");
        }
        if (param.getCallSuccessCount() != null) {
            appSql.append(" and custG.call_success_count = '" + param.getCallSuccessCount() + "'");
        }
        // 查询所有自建属性
        Map<String, CustomerLabel> cacheLabel = labelService.getCacheCustomAndSystemLabel(param.getCustId());
        if (StringUtil.isNotEmpty(param.getLabelProperty())) {
            JSONObject jsonObject;
            String labelId, optionValue, likeValue;
            JSONArray custProperty = JSON.parseArray(param.getLabelProperty());
            for (int i = 0; i < custProperty.size(); i++) {
                jsonObject = custProperty.getJSONObject(i);
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
                    appSql.append(" AND custG.super_data LIKE '" + likeValue + "' ");
                }
            }
        }
        // 跟进状态处理
        if (StringUtil.isNotEmpty(param.getFollowStatus()) && StringUtil.isNotEmpty(param.getFollowValue())) {
            String likeValue = "%\"" + param.getFollowStatus() + "\":\"" + param.getFollowValue() + "\"%";
            appSql.append(" AND custG.super_data LIKE '" + likeValue + "' ");
        }
        // 分批处理
        Page page = customerSeaDao.sqlPageQuery0(sql.toString(), 0, 1);
        int status = page.getTotal();
        int size = 5000;
        LOG.info("开始批量更改跟进状态时间:" + LocalDateTime.now());
        for (int i = 0; i <= page.getTotal(); ) {
            page = customerSeaDao.sqlPageQueryByPageSize(sql.toString(), i, size);
            if (page.getData() == null || page.getData().size() == 0) {
                break;
            }
            Thread thread = new Thread(new ClueFollowStatusChange(page.getData(), param));
            thread.start();
            i += page.getData().size();
        }
        LOG.info("结束批量更改跟进状态时间:" + LocalDateTime.now());
        return status;
    }

    public Map<String, Object> getCustomerSeaBysupplierId(String seaId, String superId) {
        String sql = "select batch_id from t_customer_sea_list_" + seaId + " WHERE id =?";
        Map<String, Object> map = null;
        List<Map<String, Object>> list = customerSeaDao.sqlQuery(sql, superId);
        if (list != null && list.size() > 0) {
            map = list.get(0);
        }
        return map;
    }

    class ClueFollowStatusChange implements Runnable {
        List list;
        CustomerSeaSearch param;

        public ClueFollowStatusChange(List list, CustomerSeaSearch param) {
            this.list = list;
            this.param = param;
        }

        @Override
        public void run() {
            batchClueFollowStatus(param.getUserId(), param.getToFollowStatus(), param.getToFollowValue(), param.getSeaId(), list);
        }
    }

    /**
     * 公海线索状态修改
     *
     * @param param
     * @param operate 1-修改指定id的修改线索状态 2-根据指定条件修改线索状态
     * @return
     */
    public int updateClueStatus(CustomerSeaSearch param, int operate) {
        // 修改指定id的删除线索
        if (1 == operate) {
            return batchDeleteClue(param.getUserId(), param.getUserType(), param.getSeaId(), param.getSuperIds(), param.getBackReason(), param.getBackRemark());
        } else if (2 == operate) {
            //根据指定条件删除线索
            return batchDeleteClueByCondition(param);
        } else if (3 == operate) {
            // 指定ID退回公海
            return batchClueBackToSea(param.getUserId(), param.getUserType(), param.getSeaId(), param.getSuperIds(), param.getBackReason(), param.getBackRemark());
        } else if (4 == operate) {
            //指定搜索条件退回公海
            return batchClueBackToSeaByCondition(param);
        } else if (5 == operate) {
            // 指定身份ID转交线索
            return batchClueTransfer(param.getUserId(), param.getClueToUserId(), param.getUserType(), param.getSeaId(), param.getSuperIds());
        } else if (6 == operate) {
            // 指定搜索条件转交线索
            return batchClueTransferByCondition(param);
        } else if (7 == operate) {
            // 指定身份ID变更跟进状态
            return batchClueFollowStatus(param.getUserId(), param.getToFollowStatus(), param.getToFollowValue(), param.getUserType(), param.getSeaId(), param.getSuperIds());
        } else if (8 == operate) {
            // 指定搜索条件变更跟进状态
            return batchClueFollowStatusByCondition(param);
        }
        return 0;
    }

    /**
     * 线索分配
     *
     * @param param
     * @param operate
     * @param assignedList
     * @return
     * @throws TouchException
     */
    public int distributionClue(CustomerSeaSearch param, int operate, JSONArray assignedList) throws TouchException {
        // 判断坐席渠道和公海呼叫渠道是否一致
        if (operate != 3) {
            CustomerSeaProperty csp = customerSeaDao.getProperty(param.getSeaId(), "callChannel");
            if (csp == null || StringUtil.isEmpty(csp.getPropertyValue())) {
                throw new TouchException("-1", "请先配置公海呼叫渠道");
            }
            // 判断坐席呼叫渠道和公海呼叫渠道是否相同
            CustomerUserPropertyDO cp = customerUserDao.getProperty(param.getUserIds().get(0), "call_channel");
            if (!Objects.equals(csp.getPropertyValue(), cp.getPropertyValue())) {
                throw new TouchException("-1", "坐席呼叫渠道与公海不一致,不可领取");
            }
        }

        // 单一负责人分配线索|手动领取所选
        if (1 == operate) {
            return singleDistributionClue(param.getSeaId(), param.getUserIds().get(0), param.getSuperIds());
        } else if (2 == operate) {
            // 坐席根据检索条件批量领取线索
            return batchReceiveClue(param, param.getUserIds().get(0));
        } else if (3 == operate) {
            //根据检索条件批量给多人快速分配线索
            return batchDistributionClue(param, assignedList);
        } else if (4 == operate) {
            //坐席指定数量领取线索
            return getReceiveClueByNumber(param.getSeaId(), param.getUserIds().get(0), param.getGetClueNumber());
        }
        return 0;
    }


    /**
     * 单一负责人分配线索
     *
     * @param seaId
     * @param userId
     * @param superIds
     * @return
     * @throws TouchException
     */
    private int singleDistributionClue(String seaId, String userId, List<String> superIds) throws
            TouchException {
        LOG.info("分配的userId是：" + userId);
        if (superIds == null || superIds.size() == 0) {
            throw new TouchException("-1", "superIds必填");
        }
        long quantity = getUserReceivableQuantity(seaId, userId);
        List<String> tempList = new ArrayList<>();
        boolean limit = false;
        if (quantity == 0) {
            throw new TouchException("-1", "当天领取线索已达上限");
        } else {
            tempList.addAll(superIds);
            if (quantity < superIds.size()) {
                limit = true;
            }
        }
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" (`user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`,  `create_time`) ")
                .append(" SELECT ").append(userId).append(" ,id,").append(seaId).append(",batch_id,").append(5).append(",'").append(new Timestamp(System.currentTimeMillis())).append("'")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" WHERE status = 1 AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(tempList)).append(")");
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                //.append(" SET status = 0, user_id = ?, user_get_time = ? WHERE status = 1 AND id IN (").append(SqlAppendUtil.sqlAppendWhereIn(tempList)).append(")");
                .append(" SET status = 0, user_id = ?, user_get_time = ? WHERE id IN (").append(SqlAppendUtil.sqlAppendWhereIn(tempList)).append(")");

        if (limit && quantity >= 0) {
            sql.append(" LIMIT ").append(quantity);
            logSql.append(" LIMIT ").append(quantity);
        }
        // 保存转交记录
        customerSeaDao.executeUpdateSQL(logSql.toString());
        return customerSeaDao.executeUpdateSQL(sql.toString(), userId, new Timestamp(System.currentTimeMillis()));
    }

    /**
     * 根据检索条件批量给多人快速分配线索
     *
     * @param param
     * @param assignedList
     * @return
     */
    private int batchDistributionClue(CustomerSeaSearch param, JSONArray assignedList) throws TouchException {
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" custG SET custG.status = 0, user_id = ?, user_get_time = ?  WHERE custG.status = 1 ");
        StringBuilder appSql = new StringBuilder();
        if (StringUtil.isNotEmpty(param.getSuperId())) {
            appSql.append(" and custG.id = '" + param.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperName())) {
            appSql.append(" and custG.super_name = '%" + param.getSuperName() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getSuperPhone())) {
            appSql.append(" and custG.super_phone = '" + param.getSuperPhone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperTelphone())) {
            appSql.append(" and custG.super_telphone = '" + param.getSuperTelphone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getLastUserName())) {
            appSql.append(" and custG.pre_user_id IN(SELECT id from t_customer_user WHERE AND cust_id = '" + param.getCustId() + "' realname LIKE '%" + param.getLastUserName() + "%') ");
        }
        if (param.getDataSource() != null) {
            appSql.append(" and custG.data_source =" + param.getDataSource());
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            appSql.append(" and custG.batch_id =" + param.getBatchId());
        }
        if (StringUtil.isNotEmpty(param.getAddStartTime()) && StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time BETWEEN " + param.getAddStartTime() + " AND " + param.getAddEndTime());
        } else if (StringUtil.isNotEmpty(param.getAddStartTime())) {
            appSql.append(" and custG.create_time >= " + param.getAddStartTime());
        } else if (StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time <= " + param.getAddEndTime());
        }

        if (StringUtil.isNotEmpty(param.getCallStartTime()) && StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time BETWEEN " + param.getCallStartTime() + " AND " + param.getCallEndTime());
        } else if (StringUtil.isNotEmpty(param.getCallStartTime())) {
            appSql.append(" and custG.last_call_time >= " + param.getCallStartTime());
        } else if (StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time <= " + param.getCallEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastCallResult())) {
            appSql.append(" and custG.last_call_status = '" + param.getLastCallResult() + "'");
        }
        if (StringUtil.isNotEmpty(param.getIntentLevel())) {
            appSql.append(" and custG.intent_level = '" + param.getIntentLevel() + "'");
        }
        if (param.getCalledDuration() != null) {
            if (param.getCalledDuration() == 1) {
                appSql.append(" AND custG.last_called_duration<=3");
            } else if (param.getCalledDuration() == 2) {
                appSql.append(" AND custG.last_called_duration>3 AND voicLog.last_called_duration<=6");
            } else if (param.getCalledDuration() == 3) {
                appSql.append(" AND custG.last_called_duration>6 AND voicLog.last_called_duration<=12");
            } else if (param.getCalledDuration() == 4) {
                appSql.append(" AND custG.last_called_duration>12 AND voicLog.last_called_duration<=30");
            } else if (param.getCalledDuration() == 5) {
                appSql.append(" AND custG.last_called_duration>30 AND voicLog.last_called_duration<=60");
            } else if (param.getCalledDuration() == 6) {
                appSql.append(" AND custG.last_called_duration>60");
            }
        }
        appSql.append(" LIMIT ? ");
        int count = 0;
        // 处理多个负责人拆分多个线索分配
        long quantity = 0, number = 0;
        String userId;
        Timestamp time = new Timestamp(System.currentTimeMillis());
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" (`user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`,  `create_time`) ")
                .append(" SELECT ? ,id,").append(param.getSeaId()).append(",batch_id,").append(5).append(",'").append(new Timestamp(System.currentTimeMillis())).append("'")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId()).append(" custG WHERE status = 1 ");
        for (int i = 0; i < assignedList.size(); i++) {
            userId = assignedList.getJSONObject(i).getString("userId");
            number = assignedList.getJSONObject(i).getInteger("number");
            try {
                quantity = getUserReceivableQuantity(param.getSeaId(), userId);
            } catch (TouchException e) {
                LOG.error("批量快速分配线索异常,fromUserId:" + userId + ",number:" + number, e);
            }
            if (quantity != -1) {
                //-1表示可无限制领取
                if (quantity == 0) {
                    LOG.warn("fromUserId:[" + userId + "],number:[" + number + "]当天领取线索已达上限,quantity:" + quantity);
                    continue;
                } else if (quantity < number) {
                    LOG.warn("fromUserId:[" + userId + "],number:[" + number + "]可分配数量不足");
                    continue;
                }
            }
            customerSeaDao.executeUpdateSQL(logSql.toString() + appSql.toString(), userId, number);
            count += customerSeaDao.executeUpdateSQL(sql.toString() + appSql.toString(), userId, time, number);
        }
        return count;
    }

    /**
     * 坐席根据检索条件批量领取线索
     *
     * @param param
     * @param userId
     * @return
     */
    private int batchReceiveClue(CustomerSeaSearch param, String userId) throws TouchException {
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append(" (`user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`,  `create_time`) ")
                .append(" SELECT ").append(userId).append(" ,id,").append(param.getSeaId()).append(",batch_id,").append(5).append(",'").append(new Timestamp(System.currentTimeMillis())).append("'")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId()).append(" custG WHERE status = 1 ");
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(param.getSeaId())
                .append(" custG SET custG.status = 0, user_id = ?, user_get_time = ?  WHERE custG.status = 1 ");
        StringBuilder appSql = new StringBuilder();
        if (StringUtil.isNotEmpty(param.getSuperId())) {
            appSql.append(" and custG.id = '" + param.getSuperId() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperName())) {
            appSql.append(" and custG.super_name = '%" + param.getSuperName() + "%'");
        }
        if (StringUtil.isNotEmpty(param.getSuperPhone())) {
            appSql.append(" and custG.super_phone = '" + param.getSuperPhone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getSuperTelphone())) {
            appSql.append(" and custG.super_telphone = '" + param.getSuperTelphone() + "'");
        }
        if (StringUtil.isNotEmpty(param.getLastUserName())) {
            appSql.append(" and custG.pre_user_id IN(SELECT id from t_customer_user WHERE AND cust_id = '" + param.getCustId() + "' realname LIKE '%" + param.getLastUserName() + "%') ");
        }
        if (param.getDataSource() != null) {
            appSql.append(" and custG.data_source =" + param.getDataSource());
        }
        if (StringUtil.isNotEmpty(param.getBatchId())) {
            appSql.append(" and custG.batch_id =" + param.getBatchId());
        }
        if (StringUtil.isNotEmpty(param.getAddStartTime()) && StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time BETWEEN " + param.getAddStartTime() + " AND " + param.getAddEndTime());
        } else if (StringUtil.isNotEmpty(param.getAddStartTime())) {
            appSql.append(" and custG.create_time >= " + param.getAddStartTime());
        } else if (StringUtil.isNotEmpty(param.getAddEndTime())) {
            appSql.append(" and custG.create_time <= " + param.getAddEndTime());
        }

        if (StringUtil.isNotEmpty(param.getCallStartTime()) && StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time BETWEEN " + param.getCallStartTime() + " AND " + param.getCallEndTime());
        } else if (StringUtil.isNotEmpty(param.getCallStartTime())) {
            appSql.append(" and custG.last_call_time >= " + param.getCallStartTime());
        } else if (StringUtil.isNotEmpty(param.getCallEndTime())) {
            appSql.append(" and custG.last_call_time <= " + param.getCallEndTime());
        }

        if (StringUtil.isNotEmpty(param.getLastCallResult())) {
            appSql.append(" and custG.last_call_status = '" + param.getLastCallResult() + "'");
        }
        if (StringUtil.isNotEmpty(param.getIntentLevel())) {
            appSql.append(" and custG.intent_level = '" + param.getIntentLevel() + "'");
        }
        if (param.getCalledDuration() != null) {
            if (param.getCalledDuration() == 1) {
                appSql.append(" AND custG.last_called_duration<=3");
            } else if (param.getCalledDuration() == 2) {
                appSql.append(" AND custG.last_called_duration>3 AND custG.last_called_duration<=6");
            } else if (param.getCalledDuration() == 3) {
                appSql.append(" AND custG.last_called_duration>6 AND custG.last_called_duration<=12");
            } else if (param.getCalledDuration() == 4) {
                appSql.append(" AND custG.last_called_duration>12 AND custG.last_called_duration<=30");
            } else if (param.getCalledDuration() == 5) {
                appSql.append(" AND custG.last_called_duration>30 AND custG.last_called_duration<=60");
            } else if (param.getCalledDuration() == 6) {
                appSql.append(" AND custG.last_called_duration>60");
            }
        }
        int count = 0;
        long quantity = getUserReceivableQuantity(param.getSeaId(), userId);
        if (quantity == 0) {
            throw new TouchException("-1", "当天领取线索已达上限");
        } else if (quantity > 0) {
            appSql.append(" LIMIT ").append(quantity);
        }
        sql.append(appSql);
        logSql.append(appSql);
        // 保存转交记录
        customerSeaDao.executeUpdateSQL(logSql.toString());
        count = customerSeaDao.executeUpdateSQL(sql.toString(), userId, new Timestamp(System.currentTimeMillis()));
        return count;
    }

    /**
     * 坐席指定数量领取线索
     *
     * @param seaId
     * @param userId
     * @param number
     * @return
     * @throws TouchException
     */
    private int getReceiveClueByNumber(String seaId, String userId, int number) throws TouchException {
        StringBuilder sql = new StringBuilder()
                .append("UPDATE ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId)
                .append(" custG SET custG.status = 0, user_id = ?, user_get_time = ?  WHERE custG.status = 1 ");
        sql.append(" LIMIT ? ");
        int count = 0;
        long quantity = getUserReceivableQuantity(seaId, userId);
        LOG.info("可领取数量是：" + quantity);
        if (quantity == 0) {
            throw new TouchException("-1", "当天领取线索已达上限");
        }
        // 保存转交记录
        StringBuilder logSql = new StringBuilder()
                .append("INSERT INTO ").append(ConstantsUtil.CUSTOMER_OPER_LOG_TABLE_PREFIX).append("( `user_id`, `list_id`, `customer_sea_id`, `customer_group_id`, `event_type`,  `create_time`) ")
                .append(" SELECT ? ,id,").append(seaId).append(",batch_id,").append(5).append(",'").append(new Timestamp(System.currentTimeMillis())).append("'")
                .append(" FROM ").append(ConstantsUtil.SEA_TABLE_PREFIX).append(seaId).append(" custG WHERE status = 1 ");
        logSql.append(" LIMIT ? ");
        customerSeaDao.executeUpdateSQL(logSql.toString(), userId, number);
        count = customerSeaDao.executeUpdateSQL(sql.toString(), userId, new Timestamp(System.currentTimeMillis()), number);

        return count;
    }

    /**
     * 保存标记数据到es中
     *
     * @param dto
     */
    private void saveClueInfoToES(CustomerSeaESDTO dto) {
        JSONObject data = JSON.parseObject(JSON.toJSONString(dto));
        // 处理自建属性ID
        if (data != null && data.get("superData") != null) {
            for (Map.Entry<String, Object> m : data.getJSONObject("superData").entrySet()) {
                data.put(m.getKey(), m.getValue());
            }
        }
        JSONObject result = elasticSearchService.addDocumentToType(ElasticSearchService.CUSTOMER_SEA_INDEX_PREFIX + dto.getSeaId(),
                ElasticSearchService.CUSTOMER_SEA_TYPE, dto.getId(), data);
        if (result == null || result.size() == 0) {
            result = elasticSearchService.updateDocumentToType(ElasticSearchService.CUSTOMER_SEA_INDEX_PREFIX + dto.getSeaId(),
                    ElasticSearchService.CUSTOMER_SEA_TYPE, dto.getId(), data);
        }
    }

    /**
     * 保存线索标记的信息
     *
     * @param dto
     * @return
     */
    public boolean updateClueSignData(CustomSeaTouchInfoDTO dto) {
        // 处理qq 微信等默认自建属性值
        handleDefaultLabelValue(dto);
        StringBuffer sql = new StringBuffer();
        boolean status;
        try {
            LOG.info("开始更新客户群数据表个人信息:" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + ",数据:" + dto.toString());
            sql.append("UPDATE " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + " SET ")
                    .append(" super_name= ?, ")
                    .append(" super_age= ?, ")
                    .append(" super_sex= ?, ")
                    .append(" super_telphone= ?, ")
                    .append(" super_phone= ?, ")
                    .append(" super_address_province_city= ?, ")
                    .append(" super_address_street = ?, ")
                    .append(" super_data = ?, ")
                    .append(" update_time = ?, ")
                    .append(" STATUS = '0', ")
                    .append(" user_id = ? ")
                    .append(" WHERE id = ? ");
            this.marketResourceDao.executeUpdateSQL(sql.toString(), dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()),
                    new Timestamp(System.currentTimeMillis()), dto.getUser_id(), dto.getSuper_id());

            LOG.info("开始更新公海[" + dto.getCustomerSeaId() + "]数据表个人信息:" + ConstantsUtil.SEA_TABLE_PREFIX + dto.getCustomerSeaId() + ",数据:" + dto.toString());
            sql = new StringBuffer();
            sql.append("UPDATE " + ConstantsUtil.SEA_TABLE_PREFIX + dto.getCustomerSeaId() + " SET ")
                    .append(" super_name= ?, ")
                    .append(" super_age= ?, ")
                    .append(" super_sex= ?, ")
                    .append(" super_telphone= ?, ")
                    .append(" super_phone= ?, ")
                    .append(" super_address_province_city= ?, ")
                    .append(" super_address_street = ?, ")
                    .append(" super_data = ?, ")
                    .append(" last_mark_time = ?, ")
                    .append(" update_time = ?, ")
                    .append(" STATUS = '0', ")
                    .append(" user_id = ? ")
                    .append(" WHERE id = ? ");
            this.marketResourceDao.executeUpdateSQL(sql.toString(), dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), new Timestamp(System.currentTimeMillis()),
                    new Timestamp(System.currentTimeMillis()), dto.getUser_id(), dto.getSuper_id());
            // 保存标记信息到es中
            CustomerSeaESDTO esData = new CustomerSeaESDTO(dto);
            esData.setSuper_data(JSON.toJSONString(dto.getSuperData()));
            saveClueInfoToES(esData);
            // 保存标记记录
            customerLabelService.saveSuperDataLog(dto.getSuper_id(), dto.getCust_group_id(), "", dto.getUser_id(),
                    dto.getSuperData(), dto.getCustomerSeaId());
            status = true;
        } catch (Exception e) {
            status = false;
            LOG.error("更新数据表个人信息" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + "失败", e);
        }
        return status;
    }

    /**
     * 添加线索
     *
     * @param dto
     * @return
     */
    public boolean addClueData(CustomSeaTouchInfoDTO dto) {
        // 处理qq 微信等默认自建属性值
        handleDefaultLabelValue(dto);
        StringBuffer sql = new StringBuffer();
        boolean status;
        try {
            // 查询公海下默认客群
            CustomerSeaProperty csp = customerSeaDao.getProperty(dto.getCustomerSeaId(), "defaultClueCgId");
            if (csp == null) {
                LOG.warn("公海:" + dto.getCustomerSeaId() + ",默认线索客群不存在");
            } else {
                dto.setCust_group_id(csp.getPropertyValue());
            }
            String superId = MD5Util.encode32Bit("c" + dto.getSuper_telphone());
            dto.setSuper_id(superId);
            CustomerUser user = customerUserDao.get(NumberConvertUtil.parseLong(dto.getUser_id()));
            int dataStatus = 1;
            // 组长和员工数据状态为已分配
            if (2 == user.getUserType()) {
                dataStatus = 0;
            } else {
                // 超管和项目管理员数据状态为未分配
                dto.setUser_id(null);
            }
            LOG.info("开始保存添加线索个人信息:" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + ",数据:" + dto.toString());
            StringBuffer sb = new StringBuffer();
            sb.append(" create table IF NOT EXISTS t_customer_group_list_");
            sb.append(dto.getCust_group_id());
            sb.append(" like t_customer_group_list");
            try {
                customGroupDao.executeUpdateSQL(sb.toString());
            } catch (HibernateException e) {
                LOG.error("创建用户群表失败,id:" + dto.getCust_group_id(), e);
            }
            sql.append(" INSERT INTO " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id())
                    .append(" (id, user_id, status, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`, `super_address_province_city`, `super_address_street`, `super_data`,update_time) ")
                    .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ");
            this.customerSeaDao.executeUpdateSQL(sql.toString(), superId, dto.getUser_id(), dataStatus, dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), new Timestamp(System.currentTimeMillis()));

            sql = new StringBuffer();
            sql.append(" INSERT INTO " + ConstantsUtil.SEA_TABLE_PREFIX + dto.getCustomerSeaId())
                    .append(" (id, user_id, status, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`, `super_address_province_city`, `super_address_street`, `super_data`, batch_id, data_source,create_time) ")
                    .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
            this.customerSeaDao.executeUpdateSQL(sql.toString(), superId, dto.getUser_id(), dataStatus, dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), dto.getCust_group_id(), 3, new Timestamp(System.currentTimeMillis()));
            // 保存标记信息到es中
            CustomerSeaESDTO esData = new CustomerSeaESDTO(dto);
            esData.setSuper_data(JSON.toJSONString(dto.getSuperData()));
            //es暂时取消
            //saveClueInfoToES(esData);
            // 保存到redis中号码对应关系
            phoneService.setValueByIdFromRedis(superId, dto.getSuper_telphone());
            status = true;
        } catch (Exception e) {
            status = false;
            LOG.error("保存添加线索个人信息" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + "失败", e);
        }
        return status;
    }

    public int addClueData0(CustomSeaTouchInfoDTO dto) {
        // 处理qq 微信等默认自建属性值
        handleDefaultLabelValue(dto);
        StringBuffer sql = new StringBuffer();
        int status = 0;
        try {
            // 查询公海下默认客群
            CustomerSeaProperty csp = customerSeaDao.getProperty(dto.getCustomerSeaId(), "defaultClueCgId");
            if (csp == null) {
                LOG.warn("公海:" + dto.getCustomerSeaId() + ",默认线索客群不存在");
            } else {
                dto.setCust_group_id(csp.getPropertyValue());
            }
            String superId = MD5Util.encode32Bit("c" + dto.getSuper_telphone());
            dto.setSuper_id(superId);
            CustomerUser user = customerUserDao.get(NumberConvertUtil.parseLong(dto.getUser_id()));
            int dataStatus = 1;
            // 组长和员工数据状态为已分配
            if (2 == user.getUserType()) {
                dataStatus = 0;
            } else {
                // 超管和项目管理员数据状态为未分配
                dto.setUser_id(null);
            }
            LOG.info("开始保存添加线索个人信息:" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + ",数据:" + dto.toString());
            try {
                customGroupDao.createCgDataTable(NumberConvertUtil.parseInt(dto.getCust_group_id()));
            } catch (HibernateException e) {
                LOG.error("创建用户群表失败,id:" + dto.getCust_group_id(), e);
            }
            List<Map<String, Object>> list = customerDao.sqlQuery("SELECT id FROM " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + " WHERE id= ?", superId);
            if (list.size() > 0) {
                LOG.warn("客群ID:[" + dto.getCust_group_id() + "]添加线索ID:[" + superId + "]已经存在");
                return -1;
            }

            sql.append(" INSERT INTO " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id())
                    .append(" (id, user_id, status, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`, `super_address_province_city`, `super_address_street`, `super_data`,update_time) ")
                    .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?) ");
            this.customerSeaDao.executeUpdateSQL(sql.toString(), superId, dto.getUser_id(), dataStatus, dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), new Timestamp(System.currentTimeMillis()));

            sql = new StringBuffer();
            sql.append(" INSERT INTO " + ConstantsUtil.SEA_TABLE_PREFIX + dto.getCustomerSeaId())
                    .append(" (id, user_id, status, `super_name`, `super_age`, `super_sex`, `super_telphone`, `super_phone`, `super_address_province_city`, `super_address_street`, `super_data`, batch_id, data_source,create_time) ")
                    .append(" VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?) ");
            this.customerSeaDao.executeUpdateSQL(sql.toString(), superId, dto.getUser_id(), dataStatus, dto.getSuper_name(), dto.getSuper_age(),
                    dto.getSuper_sex(), dto.getSuper_telphone(), dto.getSuper_phone(),
                    dto.getSuper_address_province_city(), dto.getSuper_address_street(), JSON.toJSONString(dto.getSuperData()), dto.getCust_group_id(), 3, new Timestamp(System.currentTimeMillis()));
            // 保存标记信息到es中
            CustomerSeaESDTO esData = new CustomerSeaESDTO(dto);
            esData.setSuper_data(JSON.toJSONString(dto.getSuperData()));
            //es暂时取消
            //saveClueInfoToES(esData);
            // 保存到redis中号码对应关系
            phoneService.setValueByIdFromRedis(superId, dto.getSuper_telphone());
            status = 1;
        } catch (Exception e) {
            status = 0;
            LOG.error("保存添加线索个人信息" + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + dto.getCust_group_id() + "失败", e);
        }
        return status;
    }

    /**
     * 保存公海转交记录
     *
     * @param log
     */
    public void saveSuperDataOperLog(SuperDataOperLog log) {
        log.setCreateTime(new Timestamp(System.currentTimeMillis()));
        customerSeaDao.saveOrUpdate(log);
    }

    /**
     * 异步保存公海转交记录
     *
     * @param userId
     * @param superId
     * @param marketSeaId
     * @param customerGroupId
     * @param eventType
     * @param objId
     */
    public void asynSaveSuperDataOperLog(Long userId, String superId, String marketSeaId, String customerGroupId, int eventType, String objId) {
        SuperDataOperLog log = new SuperDataOperLog(userId, superId, marketSeaId, customerGroupId, eventType, objId);
        log.setCreateTime(new Timestamp(System.currentTimeMillis()));
        SuperDataOperLogUtil.getInstance().insertLog(log);
    }

    /**
     * 处理qq 微信 根据状态等自建属性值存入super_data
     *
     * @param dto
     */
    private void handleDefaultLabelValue(CustomSeaTouchInfoDTO dto) {
        Map<String, Object> superData = new HashMap<>(16);
        if (dto.getSuperData() != null && dto.getSuperData().size() > 0) {
            for (Map.Entry<String, Object> m : dto.getSuperData().entrySet()) {
                superData.put(m.getKey(), m.getValue());
            }
        }
        JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(dto));
        if (jsonObject != null && jsonObject.size() > 0) {
            for (Map.Entry<String, Object> m : jsonObject.entrySet()) {
                if (defaultLabels.get(m.getKey()) != null && StringUtil.isNotEmpty(String.valueOf(m.getValue()))) {
                    // qq 微信等系统自建属性
                    superData.put(defaultLabels.get(m.getKey()), m.getValue());
                }
            }
        }
        dto.setSuperData(superData);
    }

    /**
     * 解析super_data中qq 微信等属性
     *
     * @param data
     */
    private void getDefaultLabelValue(Map<String, Object> data) {
        if (data != null && data.get("super_data") != null) {
            JSONObject jsonObject = JSON.parseObject(String.valueOf(data.get("super_data")));
            if (jsonObject == null || jsonObject.size() == 0) {
                return;
            }
            for (Map.Entry<String, Object> m : jsonObject.entrySet()) {
                for (Map.Entry<String, String> label : defaultLabels.entrySet()) {
                    if (Objects.equals(m.getKey(), label.getValue())) {
                        data.put(label.getKey(), m.getValue());
                        break;
                    }
                }
            }
        }
    }


    /**
     * 检查公海是否属于该客户
     *
     * @param customerId
     * @param seaId
     * @return
     */
    public boolean checkCustomerSeaPermission(String customerId, String seaId) {
        CustomerSea customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(seaId));
        LOG.info("检查公海所属客户权限,customerId:" + customerId + ",seaId:" + seaId);
        if (customerSea != null) {
            if (StringUtil.isNotEmpty(customerSea.getCustId()) && customerSea.getCustId().equals(customerId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查询公海详情
     *
     * @param seaId
     * @return
     */
    public CustomerSeaDTO selectCustomerSea(String seaId) {
        CustomerSea customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(seaId));
        if (customerSea == null) {
            LOG.warn("公海:" + seaId + "不存在");
            return null;
        }
        // 查询外显
        String apparentNum = null;
        CustomerSeaProperty apparentNumber = customerSeaDao.getProperty(seaId, "apparentNumber");
        if (apparentNumber != null) {
            apparentNum = apparentNumber.getPropertyValue();
        }
        String csp = null;
        // 查询呼叫速度
        CustomerSeaProperty callSpeed = customerSeaDao.getProperty(seaId, "callSpeed");
        if (callSpeed != null) {
            csp = callSpeed.getPropertyValue();
        }
        // 查询呼叫次数
        String ccn = null;
        CustomerSeaProperty callCount = customerSeaDao.getProperty(seaId, "callCount");
        if (callCount != null) {
            ccn = callCount.getPropertyValue();
        }
        CustomerSeaDTO dto = new CustomerSeaDTO(customerSea);
        dto.setApparentNumber(apparentNum);
        dto.setCallSpeed(NumberConvertUtil.parseInt(csp));
        dto.setCallCount(NumberConvertUtil.parseInt(ccn));

        // 查询呼叫渠道
        CustomerSeaProperty cChannel = customerSeaDao.getProperty(seaId, "callChannel");
        if (cChannel != null && StringUtil.isNotEmpty(cChannel.getPropertyValue())) {
            dto.setCallChannel(cChannel.getPropertyValue());
            MarketResourceDTO mr = marketResourceDao.getInfoProperty(NumberConvertUtil.parseInt(cChannel.getPropertyValue()), "price_config");
            if (mr != null && mr.getTypeCode() != null) {
                dto.setCallChannelName(mr.getResname());
                JSONObject priceConfig = JSON.parseObject(mr.getResourceProperty());
                if (priceConfig != null) {
                    if (mr.getTypeCode() == 1) {
                        dto.setCallType(priceConfig.getInteger("type"));
                        dto.setCallCenterType(priceConfig.getInteger("call_center_type"));
                    } else if (mr.getTypeCode() == 2) {
                        dto.setSmsType(priceConfig.getInteger("type"));
                    }
                }
            }
        }
        return dto;
    }

    /**
     * 线索标记分页
     *
     * @param custId
     * @param seaId
     * @param superId
     * @param customerGroupId
     * @param userId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page pageClueSignLog(String custId, String seaId, String superId, String customerGroupId, long userId, int pageNum, int pageSize) {
        String yearMonth = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMM"));
        StringBuilder sql = new StringBuilder();
        sql.append(" SELECT user_id,superid,super_data,create_time FROM ")
                .append(ConstantsUtil.SUPPERDATA_LOG_TABLE_PREFIX).append(yearMonth).append(" WHERE 1=1 ");
        if (StringUtil.isNotEmpty(superId)) {
            sql.append(" AND superid = '").append(superId).append("' ");
        }
        if (StringUtil.isNotEmpty(seaId)) {
            sql.append(" AND customer_sea_id = '").append(seaId).append("' ");
        }
        if (StringUtil.isNotEmpty(customerGroupId)) {
            sql.append(" AND customer_group_id = '").append(customerGroupId).append("' ");
        }
        if (userId > 0) {
            sql.append(" AND user_id = '").append(userId).append("' ");
        }
        sql.append(" ORDER BY create_time DESC ");
        Page page = customerSeaDao.sqlPageQuery0(sql.toString(), pageNum, pageSize);
        if (page.getData() != null && page.getData().size() > 0) {
            Map<Object, Object> labelName = customerLabelService.getCustomAndSystemLabel(custId);
            LOG.info("labelName" + labelName);
            Map<Object, Object> data, labelData;
            String loginName, superData;
            JSONObject jsonObject;
            List list;
            for (int i = 0; i < page.getData().size(); i++) {
                data = (Map<Object, Object>) page.getData().get(i);
                // 登录名称处理
                data.put("loginName", "");
                loginName = customerUserDao.getLoginName(String.valueOf(data.get("user_id")));
                if (StringUtil.isNotEmpty(loginName)) {
                    data.put("loginName", loginName);
                }
                // 标记信息处理
                superData = String.valueOf(data.get("super_data"));
                if (StringUtil.isEmpty(superData)) {
                    continue;
                }
                superData = superData.replace("\"}\"", "\"}").replace(":\"{", ":{");
                LOG.info("superData" + superData);
                jsonObject = JSON.parseObject(superData);
                data.put("labelData", "");
                if (jsonObject == null) {
                    continue;
                }
                list = new ArrayList();
                for (Map.Entry k : jsonObject.getJSONObject("supperData").entrySet()) {
                    labelData = new HashMap<>();
                    if (labelName.get(k.getKey()) != null) {
                        labelData.put(labelName.get(k.getKey()), k.getValue());
                        list.add(labelData);
                    }
                }
                data.put("labelData", list);
                data.remove("super_data");
            }

        }
        return page;
    }

    /**
     * 线索转交操作记录
     *
     * @param p
     * @param pageNum
     * @param pageSize
     * @return
     */
    public Page pageClueOperLog(SuperDataOperLogQuery p, int pageNum, int pageSize) {
        Page page = superDataOperLogDao.pageSuperDataOperLog(p, pageNum, pageSize);
        if (page.getData() != null && page.getData().size() > 0) {
            SuperDataOperLog data;
            String loginName;
            List<SuperDataOperLogDTO> list = new ArrayList<>();
            SuperDataOperLogDTO dto;
            for (int i = 0; i < page.getData().size(); i++) {
                data = (SuperDataOperLog) page.getData().get(i);
                dto = new SuperDataOperLogDTO(data);
                // 登录名称处理
                dto.setUserName("");
                loginName = customerUserDao.getLoginName(String.valueOf(dto.getUserId()));
                if (StringUtil.isNotEmpty(loginName)) {
                    dto.setUserName(loginName);
                }
                // 转交记录查询接收人
                if (data.getEventType() == 6) {
                    dto.setObjName(customerUserDao.getLoginName(String.valueOf(data.getObjectCode())));
                }
                list.add(dto);
            }
            page.setData(list);
        }
        return page;
    }

    /**
     * 判断用户能否致电
     * 1.判断余额
     * 2.判断坐席指定的渠道是否和任务渠道一致
     * 3.如果是任务渠道是讯众，且任务类型是自动任务，则要判断当前登录人是否为该任务成员
     */
    public boolean isValidAccount(LoginUser lu, String seaId) throws Exception {
        if ("2".equals(lu.getUserType())) {
            CustomerSea customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(seaId));
            if (customerSea == null || customerSea.getStatus() == null) {
                LOG.warn("公海[" + seaId + "]状态异常");
                return false;
            }
            if (2 == customerSea.getStatus().intValue()) {
                LOG.warn("公海[" + seaId + "]状态关闭,状态:" + customerSea.getStatus());
                return false;
            }
            CustomerUserPropertyDO call_channel = customerUserDao.getProperty(lu.getId().toString(), "call_channel");
            if (call_channel == null || StringUtil.isEmpty(call_channel.getPropertyValue())) {
                throw new TouchException("坐席[" + lu.getId() + "]未指定渠道");
            }
            CustomerSeaProperty taskCallChannel = customerSeaDao.getProperty(seaId, "callChannel");
            LOG.info("taskCallChannel:" + taskCallChannel == null ? null : taskCallChannel.toString());
            if (taskCallChannel == null || StringUtil.isEmpty(taskCallChannel.getPropertyValue())) {
                throw new TouchException("公海[" + seaId + "]未指定渠道");
            }
            if (!call_channel.getPropertyValue().equals(taskCallChannel.getPropertyValue())) {
                throw new TouchException("坐席[" + lu.getId() + "]指定渠道和公海[" + seaId + "]指定渠道不一致");
            }
            ResourcePropertyEntity priceConfig = marketResourceDao.getProperty(taskCallChannel.getPropertyValue(), "price_config");
            if (priceConfig == null || StringUtil.isEmpty(priceConfig.getPropertyValue())) {
                LOG.warn("渠道[" + taskCallChannel.getPropertyValue() + "]未设置定价");
                throw new TouchException("所选渠道未设置定价");
            }
            JSONObject config = JSON.parseObject(priceConfig.getPropertyValue());
            if (customerSea.getTaskType() == 1 && config.containsKey("call_center_type") && "2".equals(config.getString("call_center_type"))) {
                MarketProjectProperty mp = marketProjectDao.getProperty(String.valueOf(customerSea.getMarketProjectId()), "executionGroup");
                if (mp == null || StringUtil.isEmpty(mp.getPropertyValue())) {
                    LOG.warn("项目[" + customerSea.getMarketProjectId() + "]未设置用户组");
                    throw new TouchException("项目[" + customerSea.getMarketProjectId() + "]未设置用户组");
                }
                Set<String> userIds = new HashSet<>();
                List<CustomerUserDTO> tmp;
                for (String groupId : mp.getPropertyValue().split(",")) {
                    tmp = customerUserDao.listSelectCustomerUserByUserGroupId(groupId, lu.getCustId());
                    for (CustomerUserDTO user : tmp) {
                        userIds.add(user.getId());
                    }
                }
                if (!userIds.contains(String.valueOf(lu.getId()))) {
                    LOG.warn("[" + lu.getId() + "]非任务成员");
                    throw new TouchException("非任务成员 ");
                }
            }
            if (customerSea.getStatus() != 1) {
                LOG.warn("公海[" + seaId + "]未开启");
                throw new TouchException("公海未开启");
            }
        } else {
            LOG.warn("用户[" + lu.getId() + "]非普通坐席,无需检查权限");
            throw new TouchException("用户[" + lu.getId() + "]非普通坐席,无需检查权限");
        }
        return true;
    }

    /**
     * 根据新方任务ID查询公海数据手机号
     *
     * @param taskId
     * @param pageSize
     * @return
     */
    public XfPullPhoneDTO pagePhonesToXf(String taskId, Long pageSize) {
        XfPullPhoneDTO result = new XfPullPhoneDTO();
        result.setContent("");
        CustomerSea customerSea = customerSeaDao.getCustomerSeaByTaskId(taskId);
        if (customerSea == null) {
            LOG.warn("新方自动外呼taskId:" + taskId + "未查询到对应的公海");
            result.setResult(1);
            return result;
        }
        int taskPhoneIndex = 0;
        if (customerSea.getTaskPhoneIndex() != null) {
            taskPhoneIndex = customerSea.getTaskPhoneIndex();
        } else {
            LOG.warn("新方自动外呼seaId:" + customerSea.getId() + "的task_phone_index字段为空,设置为:" + taskPhoneIndex);
        }
        StringBuffer sb = new StringBuffer();
        sb.append(" select custG.id, custG.batch_id ")
                .append("  from " + ConstantsUtil.SEA_TABLE_PREFIX + customerSea.getId() + " custG ")
                .append(" where pull_status = 0 ")
                .append(" ORDER BY custG.id ASC ")
                .append("  LIMIT ?,? ");

        List<Map<String, Object>> phones = null;
        StringBuffer content = new StringBuffer();
        try {
            phones = this.customerSeaDao.sqlQuery(sb.toString(), taskPhoneIndex, pageSize);
            if (phones == null || phones.size() == 0) {
                result.setResult(2);
                return result;
            }
            taskPhoneIndex += phones.size();
            String u;
            for (Map<String, Object> phone : phones) {
                if (phone != null && phone.get("id") != null) {
                    u = phoneService.getPhoneBySuperId(String.valueOf(phone.get("id")));
                    content.append(0)
                            .append(",")
                            .append(u)
                            .append(",")
                            .append(customerSea.getId() + "_" + customerSea.getCustId() + "_" + taskPhoneIndex + "_" + phone.get("id"))
                            .append("\r\n");
                    //保存客群,公海和手机号对应的身份ID到redis
                    phoneService.setCGroupDataToRedis(String.valueOf(phone.get("batch_id")), String.valueOf(phone.get("id")), u);
                    phoneService.setCGroupDataToRedis(String.valueOf(customerSea.getId()), String.valueOf(phone.get("id")), u);
                }
            }
            customerSea.setTaskPhoneIndex(taskPhoneIndex);
            customerSeaDao.update(customerSea);
            LOG.info("新方自动外呼seaId:" + customerSea.getId() + "更新号码最后index成功,index:" + taskPhoneIndex);
            result.setResult(0);
        } catch (Exception e) {
            result.setResult(3);
            LOG.error("新方自动外呼查询公海手机号失败,", e);
            // 异常后返回空数据,防止重复拉取
            content.setLength(0);
            // 拉号标志位重置
            customerSea.setTaskPhoneIndex(taskPhoneIndex - phones.size());
            customerSeaDao.update(customerSea);
        }
        result.setContent(content.toString());
        // 更新拉号标识
        if (result.getResult() != null && 0 == result.getResult()) {
            String sql = "UPDATE " + ConstantsUtil.SEA_TABLE_PREFIX + customerSea.getId() + " SET pull_status = ? WHERE id = ? ";
            for (Map<String, Object> phone : phones) {
                if (phone != null && phone.get("id") != null) {
                    customerSeaDao.executeUpdateSQL(sql, 1, phone.get("id"));
                }
            }
        }
        return result;
    }

    /**
     * 讯众呼叫中心获取号码总数(新版)
     *
     * @param taskId
     * @return
     */
    public Map<String, Object> xzCountByTaskId(String taskId) {
        CustomerSea customerSea = customerSeaDao.getCustomerSeaByTaskId(taskId);
        if (customerSea == null) {
            LOG.warn("讯众自动外呼taskId:[" + taskId + "]未查询到对应的公海");
            return null;
        }
        Map<String, Object> data = new HashMap<>();
        // 公海数据总数
        data.put("all_customer", 0);
        // 公海呼叫数
        data.put("called_customer", customerSea.getTaskPhoneIndex());
        // 公海接通数
        data.put("connected_customer", 0);
        // 号码标识,获取号码时携带
        data.put("cusid", customerSea.getTaskPhoneIndex());
        return data;
    }

    /**
     * 讯众拉号返回随路参数
     *
     * @param taskId
     * @param pageNum
     * @param pageSize
     * @return
     */
    public List<XzPullPhoneDTO> pagePhonesToXz(String taskId, Integer pageNum, Integer pageSize) {
        LOG.info("第三方任务ID:[" + taskId + "],拉取手机号,pageNum:" + pageNum + ",pageSize:" + pageSize);
        CustomerSea customerSea = customerSeaDao.getCustomerSeaByTaskId(taskId);
        if (customerSea == null) {
            return new ArrayList<>();
        }
        int phoneIndex = customerSea.getTaskPhoneIndex();
        LOG.info("公海ID:[" + customerSea.getId() + "],phoneIndex:" + phoneIndex);

        StringBuffer sb = new StringBuffer();
        sb.append("SELECT id, batch_id ");
        sb.append(" FROM " + ConstantsUtil.SEA_TABLE_PREFIX + customerSea.getId());
        sb.append(" WHERE pull_status = 0 ");
        sb.append(" ORDER BY id ASC ");

        // 如果记录的号码index大于拉取的index,则从记录号码的index开始拉取,防止重复拨打
        if (phoneIndex > pageNum) {
            LOG.warn("公海ID:[" + customerSea.getId() + "],记录的index:" + phoneIndex + ",拉取的index:" + pageNum);
            pageNum = phoneIndex;
        }
        sb.append(" LIMIT " + pageNum + "," + pageSize);

        List<Map<String, Object>> ids = null;
        List<XzPullPhoneDTO> phoneList = new ArrayList<>();
        int phoneSize = 0;
        try {
            ids = customerSeaDao.sqlQuery(sb.toString());
            if (ids == null || ids.size() == 0) {
                LOG.info("公海ID:[" + customerSea.getId() + "],手机号拉取完成,phoneIndex:" + phoneIndex);
                return phoneList;
            }
            phoneSize = ids.size();
            phoneIndex += ids.size();
            String phone;
            for (Map<String, Object> id : ids) {
                if (id != null) {
                    phone = phoneService.getPhoneBySuperId(String.valueOf(id.get("id")));
                    phoneList.add(new XzPullPhoneDTO(phone, String.valueOf(id.get("id"))));
                    //保存客群和手机号对应的身份ID到redis
                    phoneService.setCGroupDataToRedis(String.valueOf(id.get("batch_id")), String.valueOf(id.get("id")), phone);
                    phoneService.setCGroupDataToRedis(String.valueOf(customerSea.getId()), String.valueOf(id.get("id")), phone);
                }
            }
            customerSea.setTaskPhoneIndex(phoneIndex);
            customerSeaDao.update(customerSea);
        } catch (Exception e) {
            LOG.error("公海ID:[" + customerSea.getId() + "]拉取手机号失败,", e);
            phoneList = new ArrayList<>();
            // 拉号标志位重置
            customerSea.setTaskPhoneIndex(phoneIndex - phoneSize);
            customerSeaDao.update(customerSea);
        }
        if (phoneList != null && phoneList.size() > 0) {
            // 更新拉号标识
            String sql = "UPDATE " + ConstantsUtil.SEA_TABLE_PREFIX + customerSea.getId() + " SET pull_status = ? WHERE id = ? ";
            for (Map<String, Object> id : ids) {
                if (id != null && id.get("id") != null) {
                    customerSeaDao.executeUpdateSQL(sql, 1, id.get("id"));
                }
            }
        }
        return phoneList;
    }

    /**
     * 查询公海线索
     *
     * @param seaId
     * @param dataId type=1-身份id type=2-手机号
     * @param type   1-身份id 2-手机号
     * @return
     */
    public Map<String, Object> selectClueInfo(String seaId, String dataId, int type) {
        Map<String, Object> data = new HashMap<>();
        CustomerSea customerSea = customerSeaDao.get(NumberConvertUtil.parseLong(seaId));
        if (customerSea == null) {
            LOG.warn("公海[" + seaId + "]不存在");
            return data;
        }
        String superId = "";
        if (1 == type) {
            superId = dataId;
        } else if (2 == type) {
            superId = phoneService.getSeaSuperIdByPhone(seaId, dataId);
        }
        List<Map<String, Object>> superIds;
        try {
            superIds = customerSeaDao.sqlQuery("SELECT id, remark, super_name, super_age, super_sex, super_telphone, " +
                    " super_phone,super_address_province_city,super_address_street, super_data, batch_id " +
                    " FROM " + ConstantsUtil.SEA_TABLE_PREFIX + seaId + " WHERE id = ?", superId);
        } catch (Exception e) {
            LOG.error("查询公海[" + seaId + "]线索[" + superId + "]数据失败,", e);
            data.put("superList", new ArrayList<>());
            return data;
        }
        if (superIds.size() > 0) {
            data = superIds.get(0);
            String phone = phoneService.getPhoneBySuperId(superId);
            data.put("phone", phone);
            // 查询自建属性
            Map<String, CustomerLabel> cacheLabel = customerLabelService.getCacheCustomAndSystemLabel(customerSea.getCustId());
            handleClueData(superIds, cacheLabel, customerSea);
        }
        return data;
    }


}
