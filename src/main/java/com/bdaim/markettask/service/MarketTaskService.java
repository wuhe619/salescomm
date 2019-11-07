package com.bdaim.markettask.service;

import com.alibaba.excel.ExcelWriter;
import com.alibaba.excel.metadata.Sheet;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.common.CallUtil;
import com.bdaim.callcenter.common.PhoneAreaUtil;
import com.bdaim.callcenter.dto.XfPullPhoneDTO;
import com.bdaim.callcenter.service.impl.XzCallCenterService;
import com.bdaim.callcenter.util.XzCallCenterUtil;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.util.redis.RedisUtil;
import com.bdaim.common.service.PhoneService;
import com.bdaim.common.util.*;
import com.bdaim.common.util.excel.ExcelAfterWriteHandlerImpl;
import com.bdaim.customer.dao.CustomerDao;
import com.bdaim.customer.dao.CustomerLabelDao;
import com.bdaim.customer.dao.CustomerUserDao;
import com.bdaim.customer.dto.CustomerLabelDTO;
import com.bdaim.customer.dto.CustomerUserDTO;
import com.bdaim.customer.dto.CustomerUserPropertyEnum;
import com.bdaim.customer.entity.CustomerLabel;
import com.bdaim.customer.entity.CustomerUser;
import com.bdaim.customer.entity.CustomerUserPropertyDO;
import com.bdaim.customer.service.CustomerLabelService;
import com.bdaim.customer.service.CustomerService;
import com.bdaim.customeruser.dto.CustomerUserTypeEnum;
import com.bdaim.customgroup.dao.CustomGroupDao;
import com.bdaim.customgroup.dto.CustomGroupDTO;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.marketproject.dao.MarketProjectDao;
import com.bdaim.marketproject.entity.MarketProject;
import com.bdaim.markettask.dao.MarketTaskDao;
import com.bdaim.markettask.dto.MarketTaskDTO;
import com.bdaim.markettask.dto.MarketTaskListParam;
import com.bdaim.markettask.dto.MarketTaskParam;
import com.bdaim.markettask.entity.MarketTask;
import com.bdaim.markettask.entity.MarketTaskProperty;
import com.bdaim.markettask.entity.MarketTaskUserRel;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.resource.dao.MarketResourceDao;
import com.bdaim.resource.dto.MarketResourceDTO;
import com.bdaim.resource.entity.ResourcePropertyEntity;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.smscenter.service.SendSmsService;
import com.bdaim.template.dto.MarketTemplateDTO;
import com.bdaim.template.entity.MarketTemplate;
import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.exception.SQLGrammarException;
import org.hibernate.transform.Transformers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigInteger;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.text.MessageFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author chengning@salescomm.net
 * @date 2019/4/24
 * @description
 */
@Service("marketTaskService")
@Transactional
public class MarketTaskService {

    public static final Logger LOG = LoggerFactory.getLogger(MarketTaskService.class);

    private static long dataExportTime = 0;

    private static long voiceExportTime = 0;

    @Resource
    private MarketTaskDao marketTaskDao;
    @Resource
    private CustomGroupService customGroupService;
    @Resource
    private CustomerUserDao customerUserDao;
    @Resource
    private CustomerDao customerDao;
    @Resource
    private PhoneService phoneService;
    @Resource
    private MarketResourceDao marketResourceDao;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private XzCallCenterService xzCallCenterService;
    @Resource
    private CustomerService customerService;
    @Resource
    private CustomGroupDao customGroupDao;
    @Resource
    private CustomerLabelService customerLabelService;
    @Resource
    private MarketProjectDao marketProjectDao;
    @Resource
    private CustomerLabelDao customerLabelDao;
    @Resource
    private SendSmsService sendSmsService;
    @Resource
    private RedisUtil redisUtil;


    /**
     * 检查营销任务是否属于该客户
     *
     * @param customerId
     * @param marketTaskId
     * @return
     */
    public boolean checkMarketTaskPermission(String customerId, String marketTaskId) {
        MarketTask customGroup = marketTaskDao.get(marketTaskId);
        LOG.info("检查营销任务所属客户权限,customerId:" + customerId + ",marketTaskId:" + marketTaskId);
        if (customGroup != null) {
            if (StringUtil.isNotEmpty(customGroup.getCustId()) && customGroup.getCustId().equals(customerId)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 查询营销任务下已选择的用户列表
     *
     * @param marketTaskId
     * @param custId
     * @param startAccount
     * @param endAccount
     * @return
     */
    public List<CustomerUserDTO> listSelectMarketTaskUser(String marketTaskId, String custId, String startAccount, String endAccount) {
        return marketTaskDao.listMarketTaskUser(marketTaskId, 1, custId, startAccount, endAccount);
    }

    /**
     * 查询营销任务和指定渠道下的未选择用户列表
     *
     * @param marketTaskId
     * @param resourceId
     * @param custId
     * @param uStartAccount
     * @param uEndAccount
     * @return
     */
    public List<CustomerUserDTO> listNotInMarketTaskUser(String marketTaskId, String resourceId, String custId, String uStartAccount, String uEndAccount) {
        List<CustomerUserDTO> unUsers = marketTaskDao.listNotInUserByResourceId(resourceId, marketTaskId, custId, uStartAccount, uEndAccount);
        return unUsers;
    }

    public void runSendSms(Set<String> sendTaskId, String custId, LoginUser loginUser) {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                int sendCount = 0;
                MarketTask marketTask;
                for (String smsTask : sendTaskId) {
                    marketTask = marketTaskDao.get(smsTask);
                    if (marketTask != null) {
                        sendCount = sendSmsService.sendBatchSms(custId, smsTask, String.valueOf(marketTask.getCustomerGroupId()), "", marketTask.getSmsTemplateId(), loginUser);
                        if (sendCount > 0) {
                            marketTaskDao.executeUpdateSQL(" UPDATE t_market_task SET status = ? WHERE id =?", 2, marketTask.getId());
                        }
                    }
                }
            }
        });
        thread.start();

    }

    /**
     * 创建或修改短信营销任务
     *
     * @param param
     * @return
     */
    public int saveSmsTask(MarketTaskParam param, LoginUser loginUser) {
        int code = 0;
        // 添加短信营销任务
        if (StringUtil.isEmpty(param.getId())) {
            String id = String.valueOf(IDHelper.getID());
            Map<String, Object> map;
            MarketTask marketTask;
            MarketTaskProperty mtp;
            int pageIndex = 0;
            Set<String> sendTaskId = new HashSet<>();
            for (int i = 0; i < param.getSmsSendConfig().size(); i++) {
                map = (Map<String, Object>) param.getSmsSendConfig().get(i);
                marketTask = new MarketTask(param);
                marketTask.setId(id + map.get("id"));
                marketTask.setSmsTemplateId(String.valueOf(map.get("smsTemplateId")));
                marketTask.setQuantity(String.valueOf(map.get("count")));
                marketTask.setTaskType(4);
                marketTask.setCreateTime(new Timestamp(System.currentTimeMillis()));
                marketTask.setStatus(1);
                marketTaskDao.saveOrUpdate(marketTask);
                mtp = new MarketTaskProperty(marketTask.getId(), "sendTime", String.valueOf(map.get("sendTime")), new Timestamp(System.currentTimeMillis()));
                marketTaskDao.saveOrUpdate(mtp);
                // 需要立即发送的营销任务ID
                if ("1".equals(String.valueOf(map.get("sendTime")))) {
                    sendTaskId.add(marketTask.getId());
                }
                // 导入营销任务数据
                importMarketTaskData(marketTask.getId(), marketTask.getCustomerGroupId(), marketTask.getGroupCondition(), String.valueOf(pageIndex), String.valueOf(map.get("count")));
                pageIndex += NumberConvertUtil.parseInt(map.get("count"));
            }
            code = 1;
            // 立即发送短信
            runSendSms(sendTaskId, param.getCustId(), loginUser);
        } else {
            // 更新短信营销任务
            MarketTask marketTask = marketTaskDao.get(param.getId());
            if (marketTask != null) {
                if (StringUtil.isNotEmpty(param.getName())) {
                    marketTask.setName(param.getName());
                }
                if (param.getSendTime() != null) {
                    MarketTaskProperty mtp = new MarketTaskProperty(marketTask.getId(), "sendTime", param.getSendTime());
                    marketTaskDao.saveOrUpdate(mtp);
                }
                if (StringUtil.isNotEmpty(param.getSmsTemplateId())) {
                    marketTask.setSmsTemplateId(param.getSmsTemplateId());
                }
                marketTaskDao.saveOrUpdate(marketTask);
                code = 1;
            }
        }
        return code;
    }

    /**
     * 营销任务基础数据保存
     *
     * @param param
     * @return 0-失败 1-成功
     */
    public Map<String, Object> save(MarketTaskParam param, LoginUser loginUser) {
        Map<String, Object> result = new HashMap<>();
        // 处理短信营销任务
        if (param.getTaskType() != null && 4 == param.getTaskType()) {
            int code = saveSmsTask(param, loginUser);
            result.put("code", code);
            return result;
        }
        MarketTask marketTask = new MarketTask(param);
        // 判断创建的任务是否为关联已有任务
        MarketTask oldMarketTask = null;
        if (StringUtil.isNotEmpty(param.getHistoryTaskId())) {
            LOG.info("当前创建的任务为关联已有任务,关联任务ID:" + param.getHistoryTaskId());
            marketTask.setId(String.valueOf(IDHelper.getID()));
            oldMarketTask = marketTaskDao.getMarketTaskById(param.getHistoryTaskId());
            marketTask.setTaskId(oldMarketTask.getTaskId());
        } else {
            marketTask.setId(String.valueOf(IDHelper.getID()));
        }
        marketTask.setCreateTime(new Timestamp(System.currentTimeMillis()));
        marketTask.setStatus(1);
        marketTask.setTaskPhoneIndex(0);
        marketTask.setTaskSmsIndex(0);
        result.put("id", marketTask.getId());
        // 处理条件数量
        long userCount = customGroupService.previewCustomGroupCount(param.getCustomerGroupId(), param.getGroupCondition());
        marketTask.setQuantity(String.valueOf(userCount));
        // 保存渠道
        MarketTaskProperty callChannel = new MarketTaskProperty(marketTask.getId(), "callChannel", param.getCallChannel(), new Timestamp(System.currentTimeMillis()));
        marketTaskDao.saveOrUpdate(callChannel);
        // 保存外显
        if (StringUtil.isNotEmpty(param.getApparentNumber())) {
            MarketTaskProperty apparentNumber = new MarketTaskProperty(marketTask.getId(), "apparentNumber", param.getApparentNumber(), new Timestamp(System.currentTimeMillis()));
            marketTaskDao.saveOrUpdate(apparentNumber);
        }
        // 保存呼叫速度
        if (StringUtil.isNotEmpty(param.getCallSpeed())) {
            MarketTaskProperty callSpeed = new MarketTaskProperty(marketTask.getId(), "callSpeed", param.getCallSpeed(), new Timestamp(System.currentTimeMillis()));
            marketTaskDao.saveOrUpdate(callSpeed);
        }
        // 保存呼叫次数
        if (StringUtil.isNotEmpty(param.getCallCount())) {
            MarketTaskProperty callCount = new MarketTaskProperty(marketTask.getId(), "callCount", param.getCallCount(), new Timestamp(System.currentTimeMillis()));
            marketTaskDao.saveOrUpdate(callCount);
        }
        int code = 0;
        marketTaskDao.save(marketTask);
        String xzCallCenterId = null;
        // 只处理创建的自动外呼新任务
        if (StringUtil.isEmpty(param.getHistoryTaskId()) && marketTask.getTaskType() != null && marketTask.getTaskType() == 1) {
            // 添加讯众自动外呼任务
            ResourcePropertyEntity mrp = marketResourceDao.getProperty(param.getCallChannel(), "price_config");
            if (mrp != null && StringUtil.isNotEmpty(mrp.getPropertyValue())) {
                JSONObject callCenterConfig = JSON.parseObject(mrp.getPropertyValue());
                // 呼叫中心类型SaaS模式则创建讯众自动外呼任务
                if ("1".equals(callCenterConfig.getString("type")) && "2".equals(callCenterConfig.getString("call_center_type"))) {
                    if (ConstantsUtil.XZ_API_RESOURCE_ID.equals(param.getCallChannel())) {
                        // 查询讯众呼叫中心企业账号
                        Map<String, Object> map = customerDao.selectXzCallCenterInfo(param.getCustId());
                        if (map == null || map.size() == 0) {
                            LOG.warn("客户ID:" + param.getCustId() + ",未配置讯众呼叫中心企业账号");
                            throw new ParamException("客户ID:" + param.getCustId() + ",未配置讯众呼叫中心企业账号");
                        }
                        xzCallCenterId = String.valueOf(map.get("id"));
                    } else {
                        xzCallCenterId = callCenterConfig.getJSONObject("call_center_config").getString("callCenterId");
                    }
                    param.setId(marketTask.getId());
                    String taskIdentity = addXzAutoTask(param, xzCallCenterId);
                    if (StringUtil.isNotEmpty(taskIdentity)) {
                        marketTask.setTaskId(taskIdentity);
                        marketTaskDao.update(marketTask);
                        // 设置讯众自动外呼第三方取号地址
                        xzCallCenterService.addPhoneUrl(xzCallCenterId, "admin", taskIdentity, ConstantsUtil.XZ_AUTO_TASK_PHONE_URL);

                    }
                } else {
                    LOG.warn("呼叫渠道:" + param.getCallChannel() + ",非Saas模式!");
                }
            }
        }
        // 处理讯众自动外呼成员
        if (param.getUserIds() != null && param.getUserIds().size() > 0) {
            Set<String> addSeatIds = new HashSet<>();
            MarketTaskUserRel userRel;
            CustomerUserPropertyDO cp = null;
            for (String userId : param.getUserIds()) {
                userRel = new MarketTaskUserRel(marketTask.getId(), userId, 1, new Timestamp(System.currentTimeMillis()));
                marketTaskDao.saveOrUpdate(userRel);
                // 查询坐席账号
                cp = customerUserDao.getProperty(userId, "seats_account");
                if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue())) {
                    addSeatIds.add(cp.getPropertyValue());
                } else {
                    LOG.warn("用户:" + userId + ",未配置坐席ID");
                }
            }
            // 向讯众自动外呼添加成功
            xzCallCenterService.addTaskMembers(marketTask.getTaskId(), addSeatIds, xzCallCenterId);
            // 通过接口开启自动外呼任务
            try {
                JSONObject jsonObject = XzCallCenterUtil.startAutoTask(xzCallCenterId, marketTask.getTaskId());
                LOG.info("营销任务开启讯众自动外呼任务返回结果:" + jsonObject);
            } catch (Exception e) {
                LOG.error("营销任务开启讯众自动外呼任务失败", e);
            }
        }
        // 导入营销任务详情数据
        asyncImportMarketTaskData(marketTask.getId(), marketTask.getCustomerGroupId(), marketTask.getGroupCondition());
        // 复制成员
        if (StringUtil.isNotEmpty(param.getHistoryTaskId())) {
            String sql = "INSERT INTO `t_market_task_user_rel` (`market_task_id`, `user_id`, `status`, `create_time`) SELECT ?,`user_id`, `status`,? FROM t_market_task_user_rel WHERE market_task_id=?";
            marketTaskDao.executeUpdateSQL(sql, marketTask.getId(), new Timestamp(System.currentTimeMillis()), param.getHistoryTaskId());
        }
        code = 1;
        result.put("code", code);
        return result;
    }

    private String addXzAutoTask(MarketTaskParam param, String callCenterId) {
        JSONObject xzTaskConfig = null;
        // 添加讯众自动外呼任务
        LocalDateTime taskStartTime = LocalDateTime.ofEpochSecond(param.getTaskCreateTime() / 1000, 0, ZoneOffset.ofHours(8));
        LocalDateTime taskEndTime = LocalDateTime.ofEpochSecond(param.getTaskEndTime() / 1000, 0, ZoneOffset.ofHours(8));
        int maxConcurrentNumber = xzCallCenterService.queryMaxCallOutNumber(callCenterId);
        LOG.info("讯众企业ID:" + callCenterId + ",最大并发数:" + maxConcurrentNumber);
        Map<String, Object> result = xzCallCenterService.addAutoTask(param.getName() + param.getId(), taskStartTime, taskEndTime, param.getApparentNumber(),
                NumberConvertUtil.parseInt(param.getCallSpeed()), NumberConvertUtil.parseInt(param.getCallCount()), callCenterId, maxConcurrentNumber);
        if (result != null && "0".equals(String.valueOf(result.get("code")))) {
            JSONObject jsonObject = JSON.parseObject(JSON.toJSONString(result));
            xzTaskConfig = JSON.parseObject(jsonObject.getString("data"));
            // 保存营销任务讯众呼叫中心配置信息
            MarketTaskProperty taskConfig = new MarketTaskProperty(param.getId(), "xzTaskConfig", xzTaskConfig.toJSONString(), new Timestamp(System.currentTimeMillis()));
            marketTaskDao.saveOrUpdate(taskConfig);
            return xzTaskConfig.getString("taskidentity");
        }
        return null;
    }

    /**
     * @param param
     * @param operation 1-编辑营销任务基本信息 2-处理讯众自动外呼成员
     * @return 0-失败 1-成功
     */
    public int update(MarketTaskParam param, int operation, LoginUser loginUser) {
        MarketTask marketTask = marketTaskDao.get(param.getId());
        if (marketTask == null) {
            LOG.warn("营销任务:" + param.getId() + "不存在,参数:" + param);
            return 0;
        }
        // 处理短信营销任务
        if (marketTask.getTaskType() != null && 4 == marketTask.getTaskType()) {
            return saveSmsTask(param, loginUser);
        }
        // 更新外显
        if (StringUtil.isNotEmpty(param.getApparentNumber())) {
            MarketTaskProperty apparentNumber = marketTaskDao.getProperty(param.getId(), "apparentNumber");
            if (apparentNumber == null) {
                apparentNumber = new MarketTaskProperty(param.getId(), "apparentNumber", param.getApparentNumber(), new Timestamp(System.currentTimeMillis()));
            }
            apparentNumber.setPropertyValue(param.getApparentNumber());
            marketTaskDao.saveOrUpdate(apparentNumber);
        }
        // 更新呼叫速度
        if (StringUtil.isNotEmpty(param.getCallSpeed())) {
            MarketTaskProperty callSpeed = marketTaskDao.getProperty(param.getId(), "callSpeed");
            if (callSpeed == null) {
                callSpeed = new MarketTaskProperty(marketTask.getId(), "callSpeed", param.getCallSpeed(), new Timestamp(System.currentTimeMillis()));
            }
            callSpeed.setPropertyValue(param.getCallSpeed());
            marketTaskDao.saveOrUpdate(callSpeed);
        }
        // 更新呼叫次数
        if (StringUtil.isNotEmpty(param.getCallCount())) {
            MarketTaskProperty callCount = marketTaskDao.getProperty(param.getId(), "callCount");
            if (callCount == null) {
                callCount = new MarketTaskProperty(marketTask.getId(), "callCount", param.getCallCount(), new Timestamp(System.currentTimeMillis()));
            }
            callCount.setPropertyValue(param.getCallCount());
            marketTaskDao.saveOrUpdate(callCount);
        }
        if (StringUtil.isNotEmpty(param.getTaskId())) {
            marketTask.setTaskId(param.getTaskId());
        }
        if (param.getTaskEndTime() != null) {
            marketTask.setTaskEndTime(new Timestamp(param.getTaskEndTime()));
        }
        if (param.getStatus() != null) {
            marketTask.setStatus(param.getStatus());
        }
        long time = System.currentTimeMillis();
        String xzCallCenterId = null, taskIdentity = null;
        if (marketTask.getTaskType() != null && marketTask.getTaskType() == 1) {
            MarketTaskProperty mtp = marketTaskDao.getProperty(marketTask.getId(), "callChannel");
            if (mtp != null && StringUtil.isNotEmpty(mtp.getPropertyValue())) {
                ResourcePropertyEntity mrp = marketResourceDao.getProperty(mtp.getPropertyValue(), "price_config");
                if (mrp != null && StringUtil.isNotEmpty(mrp.getPropertyValue())) {
                    JSONObject callCenterConfig = JSON.parseObject(mrp.getPropertyValue());
                    if ("1".equals(callCenterConfig.getString("type")) && "2".equals(callCenterConfig.getString("call_center_type"))) {
                        if (ConstantsUtil.XZ_API_RESOURCE_ID.equals(param.getCallChannel())) {
                            // 查询讯众呼叫中心企业账号
                            Map<String, Object> map = customerDao.selectXzCallCenterInfo(param.getCustId());
                            if (map == null || map.size() == 0) {
                                LOG.warn("客户ID:" + param.getCustId() + ",未配置讯众呼叫中心企业账号");
                                throw new ParamException("客户ID:" + param.getCustId() + ",未配置讯众呼叫中心企业账号");
                            }
                            xzCallCenterId = String.valueOf(map.get("id"));
                        } else {
                            xzCallCenterId = callCenterConfig.getJSONObject("call_center_config").getString("callCenterId");
                        }
                    } else {
                        LOG.warn("呼叫渠道:" + param.getCallChannel() + ",非Saas模式!");
                    }
                }
            }
        }
        // 开启或关闭讯众自动外呼任务
        if (StringUtil.isNotEmpty(xzCallCenterId) && operation == 0) {
            if (param.getStatus() != null) {
                try {
                    // 开启
                    if (param.getStatus() == 1) {
                        JSONObject jsonObject = XzCallCenterUtil.startAutoTask(xzCallCenterId, marketTask.getTaskId());
                        LOG.info("营销任务开启讯众自动外呼任务返回结果" + jsonObject);
                    } else if (param.getStatus() == 3) {
                        // 关闭
                        JSONObject jsonObject = XzCallCenterUtil.stopAutoTask(xzCallCenterId, marketTask.getTaskId());
                        LOG.info("营销任务停止讯众自动外呼任务返回结果" + jsonObject);
                    }
                } catch (Exception e) {
                    LOG.error("营销任务开启或关闭讯众自动外呼失败", e);
                }
            }
        }

        // 处理讯众自动外呼成员
        if (operation == 2) {
            Set<String> removeSeatIds = new HashSet<>();
            Set<String> addSeatIds = new HashSet<>();
            List<MarketTaskUserRel> list = marketTaskDao.listMarketTaskUser(marketTask.getId(), 1);
            // 删除不存在的用户
            CustomerUserPropertyDO cp = null;
            for (MarketTaskUserRel rel : list) {
                if (!param.getUserIds().contains(rel.getUserId())) {
                    marketTaskDao.deleteMarketTaskUser(marketTask.getId(), rel.getUserId());
                    // 查询坐席账号
                    cp = customerUserDao.getProperty(rel.getUserId(), "seats_account");
                    if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue())) {
                        removeSeatIds.add(cp.getPropertyValue());
                    } else {
                        LOG.warn("用户:" + rel.getUserId() + ",未配置坐席ID");
                    }
                }
            }
            MarketTaskUserRel userRel;
            for (String userId : param.getUserIds()) {
                userRel = marketTaskDao.getMarketTaskUserRel(marketTask.getId(), userId);
                if (userRel == null) {
                    userRel = new MarketTaskUserRel(marketTask.getId(), userId, 1, new Timestamp(time));
                    // 查询坐席账号
                    cp = customerUserDao.getProperty(userId, "seats_account");
                    if (cp != null && StringUtil.isNotEmpty(cp.getPropertyValue())) {
                        addSeatIds.add(cp.getPropertyValue());
                    } else {
                        LOG.warn("用户:" + userId + ",未配置坐席ID");
                    }
                }
                marketTaskDao.saveOrUpdate(userRel);
            }
            if (StringUtil.isNotEmpty(xzCallCenterId)) {
                // 添加和删除自动外呼任务坐席
                xzCallCenterService.removeTaskMembers(marketTask.getTaskId(), removeSeatIds, xzCallCenterId);
                xzCallCenterService.addTaskMembers(marketTask.getTaskId(), addSeatIds, xzCallCenterId);
            }
        }
        int code;
        try {
            marketTask.setUpdateTime(new Timestamp(time));
            marketTaskDao.update(marketTask);
            // 修改讯众自动外呼任务基础信息
            if (operation == 1) {
                MarketTaskProperty xzTaskConfig = marketTaskDao.getProperty(marketTask.getId(), "xzTaskConfig");
                if (xzTaskConfig != null && StringUtil.isNotEmpty(xzTaskConfig.getPropertyValue())) {
                    JSONObject jsonObject = JSON.parseObject(xzTaskConfig.getPropertyValue());
                    String taskId = jsonObject.getString("taskid");
                    LocalDateTime taskEndTime = LocalDateTime.ofEpochSecond(param.getTaskEndTime() / 1000, 0, ZoneOffset.ofHours(8));
                    xzCallCenterService.editAutoTask(taskId, taskEndTime, param.getApparentNumber(),
                            NumberConvertUtil.parseInt(param.getCallSpeed()), NumberConvertUtil.parseInt(param.getCallCount()), xzCallCenterId);
                } else {
                    LOG.warn("营销任务:" + marketTask.getId() + ",未查询到xzTaskConfig配置信息");
                }
            }
            code = 1;
        } catch (Exception e) {
            code = 0;
            throw new RuntimeException("更新营销任务失败,", e);
        }
        return code;
    }

    /**
     * 异步导入营销任务详情表数据
     *
     * @param marketTaskId
     * @param customGroupId
     * @param groupCondition
     */
    private void asyncImportMarketTaskData(String marketTaskId, int customGroupId, String groupCondition) {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(new ImportDataThread(marketTaskDao, marketTaskId, customGroupId, groupCondition));
        pool.shutdown();
    }

    /**
     * 异步导入营销任务详情表数据,支持分页
     *
     * @param marketTaskId
     * @param customGroupId
     * @param groupCondition
     * @param pageIndex
     * @param pageSize
     */
    private void asyncImportMarketTaskData(String marketTaskId, int customGroupId, String groupCondition, int pageIndex, int pageSize) {
        ExecutorService pool = Executors.newFixedThreadPool(1);
        pool.submit(new ImportDataThread(marketTaskDao, marketTaskId, customGroupId, groupCondition, pageIndex, pageSize));
        pool.shutdown();
    }

    /**
     * 同步导入营销任务数据
     *
     * @param marketTaskId
     * @param customGroupId
     * @param groupCondition
     * @param pageIndex
     * @param pageSize
     */
    private void importMarketTaskData(String marketTaskId, int customGroupId, String groupCondition, String pageIndex, String pageSize) {
        // 创建营销任务详情表
        StringBuffer sql = new StringBuffer();
        sql.append(" create table IF NOT EXISTS ")
                .append(ConstantsUtil.MARKET_TASK_TABLE_PREFIX)
                .append(marketTaskId)
                .append(" like t_customer_group_list");
        marketTaskDao.executeUpdateSQL(sql.toString());
        //导入数据
        sql = new StringBuffer();
        sql.append("INSERT INTO " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId)
                .append(" (id,status,remark,super_name,super_age,super_sex,super_telphone,super_phone,super_address_province_city,super_address_street,super_data,intent_level) ")
                .append(" SELECT id, '1', remark,super_name,super_age,super_sex,super_telphone,super_phone,super_address_province_city,super_address_street,super_data,intent_level ")
                .append(" FROM t_customer_group_list_" + customGroupId)
                .append(" WHERE 1=1 ");
        if (StringUtil.isNotEmpty(groupCondition)) {
            JSONArray condition = JSON.parseArray(groupCondition);
            JSONArray leafs;
            JSONObject jsonObject;
            String labelId;
            String labelDataLikeValue = "%\"{0}\":%{1}%";
            for (int i = 0; i < condition.size(); i++) {
                jsonObject = condition.getJSONObject(i);
                leafs = jsonObject.getJSONArray("leafs");
                if (leafs == null || leafs.size() == 0) {
                    continue;
                }
                labelId = jsonObject.getString("labelId");
                if (jsonObject.getIntValue("type") == 1) {
                    //　呼叫次数
                    if (ConstantsUtil.CALL_COUNT_ID.equals(labelId)) {
                        sql.append(" AND ( ");
                        for (int j = 0; j < leafs.size(); j++) {
                            if (j > 0) {
                                sql.append(" OR ");
                            }
                            if (StringUtil.isNotEmpty(leafs.getJSONObject(j).getString("value")) && leafs.getJSONObject(j).getString("value").indexOf("及以上") > 0) {
                                sql.append(" call_count >= " + leafs.getJSONObject(j).getString("value").split("")[0]);
                            } else {
                                sql.append(" call_count = " + leafs.getJSONObject(j).getString("value"));
                            }
                        }
                        sql.append(" ) ");
                    } else if (ConstantsUtil.CALL_SUCCESS_COUNT_ID.equals(labelId)) {
                        // 接通次数
                        sql.append(" AND ( ");
                        for (int j = 0; j < leafs.size(); j++) {
                            if (j > 0) {
                                sql.append(" OR ");
                            }
                            if (StringUtil.isNotEmpty(leafs.getJSONObject(j).getString("value")) && leafs.getJSONObject(j).getString("value").indexOf("及以上") > 0) {
                                sql.append(" call_success_count >=" + leafs.getJSONObject(j).getString("value").split("")[0]);
                            } else {
                                sql.append(" call_success_count = " + leafs.getJSONObject(j).getString("value"));
                            }
                        }
                        sql.append(" ) ");
                    } else if (ConstantsUtil.SMS_COUNT_ID.equals(labelId)) {
                        // 短信次数
                        sql.append(" AND ( ");
                        for (int j = 0; j < leafs.size(); j++) {
                            if (j > 0) {
                                sql.append(" OR ");
                            }
                            if (StringUtil.isNotEmpty(leafs.getJSONObject(j).getString("value")) && leafs.getJSONObject(j).getString("value").indexOf("及以上") > 0) {
                                sql.append(" sms_success_count >=" + leafs.getJSONObject(j).getString("value").split("")[0]);
                            } else {
                                sql.append(" sms_success_count = " + leafs.getJSONObject(j).getString("value"));
                            }
                        }
                        sql.append(" ) ");
                    }
                }
                if (jsonObject.getIntValue("type") == 2) {
                    sql.append(" AND ( ");
                    for (int j = 0; j < leafs.size(); j++) {
                        //　自建属性
                        if (j > 0) {
                            sql.append(" OR ");
                        }
                        sql.append(" super_data LIKE '" + MessageFormat.format(labelDataLikeValue, labelId, leafs.getJSONObject(j).getString("value")) + "'");
                    }
                    sql.append(" ) ");
                }
            }
        }
        // 分页参数
        if (StringUtil.isNotEmpty(pageIndex) && StringUtil.isNotEmpty(pageSize)) {
            sql.append(" LIMIT ").append(pageIndex).append(",").append(pageSize);
        }
        marketTaskDao.executeUpdateSQL(sql.toString());

    }

    /**
     * 创建营销任务详情表
     *
     * @param marketTaskId
     * @return
     */
    private int createMarketTaskTable(String marketTaskId) {
        StringBuffer sql = new StringBuffer();
        sql.append(" create table IF NOT EXISTS ")
                .append(ConstantsUtil.MARKET_TASK_TABLE_PREFIX)
                .append(marketTaskId)
                .append(" like t_customer_group_list");
        return marketTaskDao.executeUpdateSQL(sql.toString());
    }

    /**
     * 根据条件导入营销任务数据
     *
     * @param marketTaskId
     * @param customGroupId
     * @param groupCondition
     * @return
     */
    private int importMarketTaskDataByCondition(String marketTaskId, int customGroupId, String groupCondition) {
        createMarketTaskTable(marketTaskId);
        StringBuffer sql = new StringBuffer();
        sql.append("INSERT INTO " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId)
                .append(" (id,remark,super_name,super_age,super_sex,super_telphone,super_phone,super_address_province_city,super_address_street,super_data,intent_level) ")
                .append(" SELECT id,remark,super_name,super_age,super_sex,super_telphone,super_phone,super_address_province_city,super_address_street,super_data,intent_level ")
                .append(" FROM t_customer_group_list_" + customGroupId)
                .append(" WHERE 1=1 ");
        if (StringUtil.isNotEmpty(groupCondition)) {
            //TODO 处理筛选条件
        }
        return marketTaskDao.executeUpdateSQL(sql.toString());
    }

    /**
     * 前台营销任务管理
     *
     * @param loginUser
     * @param param
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> listMarketTask(LoginUser loginUser, MarketTaskListParam param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("total", 0);
        map.put("list", new ArrayList<>());

        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT IFNULL (t2.NAME,'') AS taskName, t2.id AS id,t2.cust_id custId,")
                .append(" t2.customer_group_id customerGroupId,t2.quantity,t2.task_create_time taskCreateTime,t2.task_phone_index,")
                .append(" t2.task_end_time taskEndTime,t2.create_time createTime,t2.task_type taskType, t2.task_id taskId,t2.status, t2.create_uid, t2.sms_template_id smsTemplateId ")
                .append(" FROM  t_market_task t2 where 1=1 ")
                .append(" AND t2.cust_id ='").append(StringEscapeUtils.escapeSql(loginUser.getCustId())).append("'");
        // .append(" AND t2.`status` = 1 ")
        // 默认不查询短信营销任务
        if (StringUtil.isEmpty(param.getTaskType())) {
            sql.append(" and t2.task_type<>4 ");
        }
        //任务名称
        if (StringUtil.isNotEmpty(param.getTaskName())) {
            sql.append(" AND t2.name like '%").append(StringEscapeUtils.escapeSql(param.getTaskName())).append("%'");
        }
        //任务id
        if (StringUtil.isNotEmpty(param.getId())) {
            sql.append(" and t2.id=").append(param.getId());
        }
        //人群id
        if (StringUtil.isNotEmpty(param.getGroupId()) && Integer.valueOf(param.getGroupId()) > 0) {
            sql.append(" and t2.customer_group_id=").append(param.getGroupId());
        }

        //如果登陆人是项目管理员，只能查看自己负责的项目
        if ("3".equals(loginUser.getUserType())) {
            List<String> cgIds = customerUserDao.listCustGroupByUserId(loginUser.getId());
            if (cgIds == null || cgIds.size() == 0) {
                result.add(map);
                return result;
            }
            sql.append(" and t2.customer_group_id in(" + SqlAppendUtil.sqlAppendWhereIn(cgIds) + ")");
        }

        //组长或员工只查看呼叫渠道相同的营销任务
        if ("2".equals(loginUser.getUserType())) {
            CustomerUserPropertyDO cp = customerUserDao.getProperty(String.valueOf(loginUser.getId()), CustomerUserPropertyEnum.CALL_CHANNEL.getKey());
            if (cp == null || StringUtil.isEmpty(cp.getPropertyValue())) {
                result.add(map);
                return result;
            }
            sql.append(" AND t2.id IN (SELECT market_task_id FROM t_market_task_property where property_name = 'callChannel' AND property_value = '").append(cp.getPropertyValue()).append("')");
            List<String> taskIds = listNotIncludeXzTaskUser(loginUser.getCustId(), String.valueOf(loginUser.getId()), 1);
            if (taskIds != null && taskIds.size() > 0) {
                sql.append(" AND t2.id NOT IN (" + SqlAppendUtil.sqlAppendWhereIn(taskIds) + ") ");
            }
        }

        if (StringUtil.isNotEmpty(param.getMarketProjectId())) {
            List<CustomGroup> cgs = customGroupService.getCustomerGroupByProjectId(loginUser.getCustId(), param.getMarketProjectId());
            if (cgs == null || cgs.size() == 0) {
                result.add(map);
                return result;
            }
            String cgId = "";
            for (CustomGroup cg : cgs) {
                cgId += "," + cg.getId();
            }
            if (StringUtil.isNotEmpty(cgId)) {
                sql.append(" and t2.customer_group_id in(").append(cgId.substring(1)).append(")");
            }
        }

        // 任务类型
        if (StringUtil.isNotEmpty(param.getTaskType())) {
            sql.append(" and t2.task_type=").append(StringEscapeUtils.escapeSql(param.getTaskType()));
            // 短信任务只有超级管理员，项目管理员可见
            if ("4".equals(param.getTaskType()) && ("2".equals(loginUser.getUserType()))) {
                result.add(map);
                return result;
            }
        }
        //任务状态
        if (StringUtil.isNotEmpty(param.getStatus())) {
            sql.append(" and t2.status=").append(StringEscapeUtils.escapeSql(param.getStatus()));
        }
        //根据渠道检索
        if (StringUtil.isNotEmpty(param.getCallChannel())) {
            sql.append(" and t2.id IN (SELECT market_task_id FROM t_market_task_property where property_name = 'callChannel' AND property_value = ").append(StringEscapeUtils.escapeSql(param.getCallChannel())).append(")");
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) || StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime()))
                    .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }

        //sql.append(" ORDER BY if (t2.task_type IS NOT NULL AND t2.task_id IS NOT NULL,0,1), t2.task_create_time DESC, t2.create_time DESC ");
        sql.append(" order by t2.create_time desc ");
        LOG.info(sql.toString());
        Page page = null;
        try {
            page = marketTaskDao.sqlPageQuery0(sql.toString(), param.getPageNum(), param.getPageSize());
        } catch (Exception e) {
            LOG.error("前台营销任务列表查询异常", e);
            page = new Page();
        }
        map.put("total", page.getTotal());
        List<Map<String, Object>> list = page.getData();
        if (list != null && list.size() > 0) {
            List<Map<String, Object>> unAssignedUsers = null;
            StringBuffer unAssignedUsersSql = new StringBuffer();
            MarketTaskProperty mtp;
            MarketTemplate marketTemplate;
            for (Map<String, Object> model : list) {
                if (model != null) {
                    // 处理任务类型未配置
                    if (model.get("taskType") == null) {
                        model.put("taskType", 0);
                    }
                }
                CustomGroup group = customGroupService.getCustomGroupById((Integer) model.get("customerGroupId"));
                model.put("groupName", "");
                if (group != null) {
                    model.put("groupName", group.getName());
                }
                model.put("projectName", "");
                if (null != group.getMarketProjectId()) {
                    MarketProject project = (MarketProject) marketTaskDao.get(MarketProject.class, group.getMarketProjectId());
                    if (project != null) {
                        model.put("projectName", project.getName());
                    }
                }
                Object lastCallTime = getLastcalledTime(model.get("id").toString());

                model.put("lastCallTime", lastCallTime);
                Object lv = getMarketTaskPullLv(model.get("id").toString(), model.get("task_phone_index") == null ? 0 : (Integer) model.get("task_phone_index"));
                model.put("pullLv", lv);

                // 查询未分配数量
                model.put("unassignedQuantity", 0);
                model.put("callCount", 0);
                unAssignedUsersSql.setLength(0);
                // 手动任务查询呼叫次数和未分配数据
                if (2 == NumberConvertUtil.parseInt(String.valueOf(model.get("taskType")))) {
                    unAssignedUsersSql.append("SELECT count(IF(`status` = 1, `id`, NULL)) AS unassignedQuantity, IFNULL(SUM(call_count), 0) AS callCount FROM t_market_task_list_" + model.get("id"));
                    try {
                        unAssignedUsers = marketTaskDao.getSQLQuery(unAssignedUsersSql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP).list();
                        if (unAssignedUsers != null && unAssignedUsers.size() > 0) {
                            model.put("unassignedQuantity", unAssignedUsers.get(0).get("unassignedQuantity"));
                            model.put("callCount", unAssignedUsers.get(0).get("callCount"));
                        }
                    } catch (Exception e) {
                        LOG.error("查询营销任务未分配数量和呼叫数量失败,", e);
                        model.put("unassignedQuantity", 0);
                        model.put("callCount", 0);
                    }
                }
                // 判断是否为讯众自动任务
                model.put("callCenterType", "0");
                if ("1".equals(String.valueOf(model.get("taskType")))) {
                    // 添加讯众自动外呼任务
                    MarketTaskProperty property = marketTaskDao.getProperty(String.valueOf(model.get("id")), "callChannel");
                    if (property != null) {
                        ResourcePropertyEntity mrp = marketResourceDao.getProperty(property.getPropertyValue(), "price_config");
                        if (mrp != null && StringUtil.isNotEmpty(mrp.getPropertyValue())) {
                            JSONObject callCenterConfig = JSON.parseObject(mrp.getPropertyValue());
                            model.put("callCenterType", callCenterConfig.getString("call_center_type"));
                        }
                    }
                }
                model.put("userName", customerUserDao.getLoginName(String.valueOf(model.get("create_uid"))));
                // 统计短信发送数量
                getSmsSendStat(model);
                // 查询短信任务发送时间
                if ("4".equals(String.valueOf(model.get("taskType")))) {
                    mtp = marketTaskDao.getProperty(String.valueOf(model.get("id")), "sendTime");
                    if (mtp == null || StringUtil.isEmpty(mtp.getPropertyValue()) || "1".equals(mtp.getPropertyValue())) {
                        model.put("sendTime", model.get("createTime"));
                    } else {
                        model.put("sendTime", mtp.getPropertyValue());
                    }
                }
            }
        }
        map.put("list", list);
        result.add(map);
        return result;
    }

    /**
     * 统计短信发送数量
     *
     * @param map
     */
    private void getSmsSendStat(Map<String, Object> map) {
        String statSql = "SELECT IFNULL(COUNT(0),0) smsCount, count(`send_status` = 1001 OR null) AS smsSucCount,count(`send_status` = 1002 OR null) AS smsFailCount,count(`send_status` = 1000 OR null) AS smsUnknownCount FROM t_touch_sms_log WHERE market_task_id = ? ";
        List<Map<String, Object>> data = marketTaskDao.sqlQuery(statSql, map.get("id"));
        // 短信发送数量
        map.put("smsCount", data.get(0).get("smsCount"));
        // 短信发送成功数量
        map.put("smsSucCount", data.get(0).get("smsSucCount"));
        // 短信发送失败
        map.put("smsFailCount", data.get(0).get("smsFailCount"));
        // 短信发送未知数量
        map.put("smsUnknownCount", data.get(0).get("smsUnknownCount"));
    }

    /**
     * 获取营销任务中最近一次的呼叫时间
     *
     * @param marketTaskId
     * @return
     */
    public Object getLastcalledTime(String marketTaskId) {
        LOG.info("marketTaskId是：" + marketTaskId);
        String _sql = "select max(last_call_time)lastCallTime from t_market_task_list_" + marketTaskId;
        List _list = null;
        try {
            _list = marketTaskDao.getSQLQuery(_sql).list();
        } catch (SQLGrammarException e) {
            LOG.error("获取营销任务中最近一次的呼叫时间异常,", e);
        }
        if (_list != null && _list.size() > 0) {
            Object v = _list.get(0);
            if (v != null) {
                LOG.info("获取最后致电时间是：" + v);
                return v;
            }
            // model.put("lastCallTime", _map.get("lastCallTime"));
                   /* Date lastCallTime = (Date) _map.get("lastCallTime");
                    if (lastCallTime != null) {
                        String lastCallTimeStr = DateUtil.fmtDateToStr(lastCallTime, "yyyy-MM-dd HH:mm:ss");
                        model.put("lastCallTime",lastCallTimeStr);
                    }*/
        }
        return null;
    }

    /**
     * 获取任务拉取率
     *
     * @param marketTaskId
     * @return
     */
    public String getMarketTaskPullLv(String marketTaskId, Integer task_phone_index) {
        if (task_phone_index == null || task_phone_index == 0) {
            return "0%";
        }
        String sql = "select count(0)num from t_market_task_list_" + marketTaskId;
        List l = null;
        try {
            l = marketTaskDao.getSQLQuery(sql).list();
        } catch (Exception e) {
            LOG.error("获取任务拉取率异常,", e);
        }
        if (l != null && l.size() > 0) {
            BigInteger num = (BigInteger) l.get(0);
            if (null != num) {
                float f = task_phone_index * 100 / num.intValue();
                return f + "%";
            }
        }
        return "0%";
    }

    /**
     * 后台营销任务管理
     *
     * @param param
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> adminListMarketTask(MarketTaskListParam param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("total", 0);
        map.put("list", new ArrayList<>());

        List<Map<String, Object>> result = new ArrayList<>();
        StringBuilder sql = new StringBuilder();
        sql.append("select IFNULL(t1.name,'') taskName,t1.id,t1.customer_group_id as customerGroupId,t1.task_create_time taskCreateTime,t1.task_end_time taskEndTime,t1.quantity,t1.status," +
                " t1.cust_id custId,t1.task_type taskType,t1.create_time createTime,t2.name as groupName,t1.task_phone_index," +
                " t3.enterprise_name enterpriseName,t4.account userName " +
                " from t_market_task t1  ")
                .append(" left join customer_group t2 on t1.customer_group_id=t2.id and t1.cust_id=t2.cust_id ")
                .append(" left JOIN t_customer t3 on t1.cust_id=t3.cust_id ")
                .append(" LEFT JOIN t_customer_user t4 on t1.cust_id=t4.cust_id and t4.user_type=1 ")
                .append(" where 1=1 ");
        if (StringUtil.isNotEmpty(param.getGroupName())) {
            sql.append(" AND t2.name like '%").append(StringEscapeUtils.escapeSql(param.getGroupName())).append("%'");
        }
        if (StringUtil.isNotEmpty(param.getGroupId()) && Integer.valueOf(param.getGroupId()) > 0) {
            sql.append(" and t2.id=").append(param.getGroupId());
        }
        // 任务类型
        if (StringUtil.isNotEmpty(param.getTaskType())) {
            sql.append(" and t1.task_type=").append(StringEscapeUtils.escapeSql(param.getTaskType()));
        }
        // 默认不查询短信营销任务
        if (StringUtil.isEmpty(param.getTaskType())) {
            //sql.append(" and t1.task_type<>4 ");
        }
        // 用户名搜索
        if (StringUtil.isNotEmpty(param.getCustUserName())) {
            sql.append(" and t4.account='").append(param.getCustUserName() + "'");
        }
        // 企业名称搜索
        if (StringUtil.isNotEmpty(param.getEnterpriseName())) {
            sql.append(" and t3.enterprise_name  LIKE '%" + StringEscapeUtils.escapeSql(param.getEnterpriseName()) + "%')");
        }
        if (StringUtil.isNotEmpty(param.getStartTime()) || StringUtil.isNotEmpty(param.getEndTime())) {
            sql.append(" and t1.create_time between '").append(StringEscapeUtils.escapeSql(param.getStartTime()))
                    .append("' and '").append(StringEscapeUtils.escapeSql(param.getEndTime())).append("'");
        }
        if (StringUtil.isNotEmpty(param.getMarketProjectId())) {
            CustomGroup c = new CustomGroup();
            c.setMarketProjectId(NumberConvertUtil.parseInt(param.getMarketProjectId()));
            List<CustomGroupDTO> cgs = customGroupDao.listCustomGroup(c);
            if (cgs == null || cgs.size() == 0) {
                result.add(map);
                return result;
            }
            String cgId = "";
            for (CustomGroupDTO cg : cgs) {
                cgId += "," + cg.getId();
            }
            if (StringUtil.isNotEmpty(cgId)) {
                sql.append(" and t1.customer_group_id in(").append(cgId.substring(1)).append(")");
            }
        }

        sql.append(" ORDER BY t1.create_time DESC ");
        LOG.info("sql=" + sql.toString());
        map.put("total", marketTaskDao.getSQLQuery(sql.toString()).list().size());
        List<Map<String, Object>> list = marketTaskDao.getSQLQuery(sql.toString()).setResultTransformer(Transformers.ALIAS_TO_ENTITY_MAP)
                .setFirstResult(param.getPageNum()).setMaxResults(param.getPageSize()).list();
        if (list != null && list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                Map<String, Object> model = list.get(i);
                model.put("projectName", "");
                Object customerGroupId = (Object) model.get("customerGroupId");
                if (null != customerGroupId) {
                    try {
                        CustomGroup group = customGroupService.findUniqueById(Integer.valueOf(customerGroupId.toString()));
                        if (group.getMarketProjectId() != null) {
                            MarketProject project = (MarketProject) marketTaskDao.get(MarketProject.class, group.getMarketProjectId());
                            if (project != null) {
                                model.put("projectName", project.getName());
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                Object s = getLastcalledTime(model.get("id").toString());
                model.put("lastCallTime", s);
                Object lv = getMarketTaskPullLv(model.get("id").toString(), model.get("task_phone_index") == null ? 0 : (Integer) model.get("task_phone_index"));
                model.put("pullLv", lv);
                // 判断是否为讯众自动任务
                model.put("callCenterType", "0");
                if ("1".equals(String.valueOf(model.get("taskType")))) {
                    // 添加讯众自动外呼任务
                    MarketTaskProperty property = marketTaskDao.getProperty(String.valueOf(model.get("id")), "callChannel");
                    if (property != null) {
                        ResourcePropertyEntity mrp = marketResourceDao.getProperty(property.getPropertyValue(), "price_config");
                        if (mrp != null && StringUtil.isNotEmpty(mrp.getPropertyValue())) {
                            JSONObject callCenterConfig = JSON.parseObject(mrp.getPropertyValue());
                            model.put("callCenterType", callCenterConfig.getString("call_center_type"));
                        }
                    }
                }
            }
        }
        map.put("list", list);
        result.add(map);
        return result;
    }


    public Page getCustomGroupDataV4(LoginUser loginUser, String taskId, Integer pageNum,
                                     Integer pageSize, String id, String userName, Integer status, String callType, String action,
                                     String intentLevel, JSONArray custProperty) {
        Page page = null;
        String userId = String.valueOf(loginUser.getId());
        StringBuffer sb = new StringBuffer();
        MarketTask task = marketTaskDao.get(taskId);
        // 机器人任务查询意向度字段
        if (task.getTaskType() != null && 3 == task.getTaskType()) {
            sb.append(" select custG.id, custG.user_id, custG.STATUS, custG.call_count callCount, custG.last_call_time lastCallTime, custG.intent_level intentLevel,");
        } else {
            sb.append(" select custG.id, custG.user_id, custG.STATUS, custG.call_count callCount, custG.last_call_time lastCallTime, ");
        }

        sb.append(" custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street, custG.super_data ");
        sb.append("  from " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + taskId + " custG ");
        sb.append(" LEFT JOIN t_customer_user user ON user.ID = custG.user_id");
        sb.append(" where 1=1 ");

        if ("2".equals(loginUser.getUserType())) {
            // 组长查组员列表
            if ("1".equals(loginUser.getUserGroupRole())) {
                List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                Set<String> userIds = new HashSet<>();
                if (customerUserDTOList.size() > 0) {
                    for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                        userIds.add(customerUserDTO.getId());
                    }
                    // 分配责任人操作
                    if (userIds.size() > 0) {
                        if ("distribution".equals(action)) {
                            sb.append(" AND (user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR custG.status= 1)");
                        } else {
                            sb.append(" AND user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }

                    }
                }
            } else {
                sb.append(" AND user.id = '" + userId + "'");
            }
        }
        if (null != id && !"".equals(id)) {
            sb.append(" and custG.id like '%" + id + "%'");
        }
        if (null != status && !"".equals(status)) {
            sb.append(" and custG.STATUS = " + status);
        }
        if (StringUtil.isNotEmpty(userName)) {
            sb.append(" and user.account like '%" + userName.trim() + "%'");
        }
        if (null != callType) {
            // 未呼叫
            if ("1".equals(callType)) {
                sb.append(" AND (custG.call_count IS NULL OR custG.call_count=0)");
                //已呼叫
            } else if ("2".equals(callType)) {
                sb.append(" AND custG.call_count > 0");
            }
        }
        if (StringUtil.isNotEmpty(intentLevel)) {
            sb.append(" and custG.intent_level = '" + intentLevel + "'");
        }
        // 查询所有自建属性
        List<CustomerLabel> customerLabels = customerLabelDao.listCustomerLabel(loginUser.getCustId());
        Map<String, CustomerLabel> cacheLabel = new HashMap<>();
        for (CustomerLabel c : customerLabels) {
            cacheLabel.put(c.getLabelId(), c);
        }
        if (custProperty != null && custProperty.size() != 0) {
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

        sb.append(" ORDER BY id ASC ");
        try {
            page = marketTaskDao.sqlPageQuery0(sb.toString(), pageNum, pageSize);
        } catch (Exception e) {
            LOG.error("查询任务详情列表失败,", e);
            return new Page();
        }
        CustomerUser user;
        if (page != null && page.getData() != null) {
            Map<String, Object> map, superData, labelData;
            List<Map<String, Object>> labelList;
            for (int i = 0; i < page.getData().size(); i++) {
                map = (Map<String, Object>) page.getData().get(i);
                if (map == null) continue;
                map.remove("super_data");
                map.put("phone", "");
                // 查询用户真实姓名
                if (map.get("user_id") != null) {
                    user = customerUserDao.get(Long.parseLong(String.valueOf(map.get("user_id"))));
                    if (user != null) {
//                        map.put("realname", user.getRealname());
                        map.put("realname", user.getAccount());
                        map.put("user_id", String.valueOf(user.getId()));
                    } else {
                        map.put("realname", "");
                    }
                    map.put("user_id", String.valueOf(map.get("user_id")));
                } else {
                    // 默认机器人任务的负责人为空
                    if (task.getTaskType() != null && 3 == task.getTaskType()) {
                        map.put("realname", "");
                    }
                }
                // 处理意向度为空的情况
                if (task.getTaskType() != null && 3 == task.getTaskType() && map.get("intentLevel") == null) {
                    map.put("intentLevel", "");
                }

                //客户基本信息从客群表读取
                StringBuffer sqlSb = new StringBuffer("");
                sqlSb.append(" select  custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street, custG.super_data ");
                sqlSb.append("  from " + ConstantsUtil.CUSTOMER_GROUP_TABLE_PREFIX + task.getCustomerGroupId() + " custG where id='" + map.get("id") + "'");
                List<Map<String, Object>> groupDetailList = marketTaskDao.sqlQuery(sqlSb.toString());
                if (groupDetailList != null && groupDetailList.size() > 0) {
                    Map<String, Object> groupDetail = groupDetailList.get(0);
                    map.put("super_name", groupDetail.getOrDefault("super_name", ""));
                    map.put("super_age", groupDetail.getOrDefault("super_age", ""));
                    map.put("super_sex", groupDetail.getOrDefault("super_sex", ""));
                    map.put("super_telphone", groupDetail.getOrDefault("super_telphone", ""));
                    map.put("super_phone", groupDetail.getOrDefault("super_phone", ""));
                    map.put("super_address_province_city", groupDetail.getOrDefault("super_address_province_city", ""));

                    // 处理自建属性数据
                    if (groupDetail.get("super_data") != null
                            && StringUtil.isNotEmpty(String.valueOf(groupDetail.get("super_data")))) {
                        labelList = new ArrayList<>();
                        superData = JSON.parseObject(String.valueOf(groupDetail.get("super_data")), Map.class);
                        if (superData != null && superData.size() > 0) {
                            for (Map.Entry<String, Object> key : superData.entrySet()) {
                                labelData = new HashMap<>();
                                labelData.put("id", key.getKey());
                                labelData.put("name", cacheLabel.get(key.getKey()) != null ? cacheLabel.get(key.getKey()).getLabelName() : "");
                                labelData.put("value", key.getValue());
                                labelList.add(labelData);
                            }
                            map.put("labelList", labelList);
                        }
                    }

                }
            }
        }

        return page;
    }

    public List<Map<String, Object>> listMarketTaskData(LoginUser loginUser, String marketTaskId, Integer
            pageNum, Integer pageSize, String id, String userName, Integer status, String callType, String action, String
                                                                intentLevel) {
        List<Map<String, Object>> result = null;
        String userId = String.valueOf(loginUser.getId());
        StringBuffer sb = new StringBuffer();
        MarketTask marketTask = marketTaskDao.get(marketTaskId);
        // 机器人任务查询意向度字段
        if (marketTask.getTaskType() != null && 3 == marketTask.getTaskType()) {
            sb.append(" select custG.id, custG.user_id, custG.STATUS, custG.call_count callCount, custG.last_call_time lastCallTime, custG.intent_level intentLevel,");
        } else {
            sb.append(" select custG.id, custG.user_id, custG.STATUS, custG.call_count callCount, custG.last_call_time lastCallTime, ");
        }

        sb.append(" custG.super_name, custG.super_age, custG.super_sex, custG.super_telphone, custG.super_phone, custG.super_address_province_city, custG.super_address_street");
        sb.append("  from " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId + " custG ");
        sb.append(" LEFT JOIN t_customer_user user ON user.ID = custG.user_id");
        sb.append(" where 1=1 ");

        if ("2".equals(loginUser.getUserType())) {
            // 组长查组员列表
            if ("1".equals(loginUser.getUserGroupRole())) {
                List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), loginUser.getCustId());
                Set<String> userIds = new HashSet<>();
                if (customerUserDTOList.size() > 0) {
                    for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                        userIds.add(customerUserDTO.getId());
                    }
                    // 分配责任人操作
                    if (userIds.size() > 0) {
                        if ("distribution".equals(action)) {
                            sb.append(" AND (user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") OR custG.status= 1)");
                        } else {
                            sb.append(" AND user.id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }

                    }
                }
            } else {
                sb.append(" AND user.id = '" + userId + "'");
            }
        }
        if (null != id && !"".equals(id)) {
            sb.append(" and custG.id like '%" + id + "%'");
        }
        if (null != status && !"".equals(status)) {
            sb.append(" and custG.STATUS = " + status);
        }
        if (StringUtil.isNotEmpty(userName)) {
            sb.append(" and user.account like '%" + userName.trim() + "%'");
        }
        if (null != callType) {
            // 未呼叫
            if ("1".equals(callType)) {
                sb.append(" AND (custG.call_count IS NULL OR custG.call_count=0)");
                //已呼叫
            } else if ("2".equals(callType)) {
                sb.append(" AND custG.call_count > 0");
            }
        }
        if (StringUtil.isNotEmpty(intentLevel)) {
            sb.append(" and custG.intent_level = '" + intentLevel + "'");
        }

        sb.append(" ORDER BY id ASC ");
        if (pageNum != null && !"".equals(pageNum) && pageSize != null && !"".equals(pageSize)) {
            sb.append("  LIMIT " + pageNum + "," + pageSize);
        }
        try {
            result = marketTaskDao.sqlQuery(sb.toString());
        } catch (DataAccessException e) {
            LOG.error("查询营销任务列表失败,", e);
            return result;
        }
        CustomerUser user;
        for (Map<String, Object> map : result) {
            if (map != null) {
                map.put("phone", "");
                // 查询用户真实姓名
                if (map.get("user_id") != null) {
                    user = customerUserDao.get(Long.parseLong(String.valueOf(map.get("user_id"))));
                    if (user != null) {
                        map.put("realname", user.getRealname());
                        map.put("realname", user.getAccount());
                        map.put("user_id", String.valueOf(user.getId()));
                    } else {
                        map.put("realname", "");
                    }
                    map.put("user_id", String.valueOf(map.get("user_id")));
                } else {
                    // 默认机器人任务的负责人为空
                    if (marketTask.getTaskType() != null && 3 == marketTask.getTaskType()) {
                        map.put("realname", "");
                    }
                }
                // 处理意向度为空的情况
                if (marketTask.getTaskType() != null && 3 == marketTask.getTaskType() && map.get("intentLevel") == null) {
                    map.put("intentLevel", "");
                }
            }
        }
        return result;
    }

    /**
     * 判断用户能否致电
     * 1.判断余额
     * 2.判断坐席指定的渠道是否和任务渠道一致
     * 3.如果是任务渠道是讯众，且任务类型是自动任务，则要判断当前登录人是否为该任务成员
     */
    public Boolean isValidAccount(LoginUser lu, String marketTaskId) throws Exception {
        boolean has_remain = marketResourceService.judRemainAmount(lu.getCustId());
        if (!has_remain && "2".equals(lu.getUserType())) {
            throw new TouchException("余额不足");
        }
        if ("2".equals(lu.getUserType())) {
            CustomerUserPropertyDO call_channel = customerUserDao.getProperty(lu.getId().toString(), "call_channel");
            if (call_channel == null || StringUtil.isEmpty(call_channel.getPropertyValue())) {
                throw new TouchException("坐席未指定渠道");
            }
            MarketTaskProperty taskCallChannel = marketTaskDao.getProperty(marketTaskId, "callChannel");
            LOG.info("taskCallChannel:" + taskCallChannel == null ? null : taskCallChannel.toString());
            if (taskCallChannel == null || StringUtil.isEmpty(taskCallChannel.getPropertyValue())) {
                throw new TouchException("任务未指定渠道");
            }
            if (!call_channel.getPropertyValue().equals(taskCallChannel.getPropertyValue())) {
                throw new TouchException("坐席指定渠道和任务指定渠道不一致");
            }
            ResourcePropertyEntity priceconfig = marketResourceDao.getProperty(taskCallChannel.getPropertyValue(), "price_config");
            if (priceconfig == null || StringUtil.isEmpty(priceconfig.getPropertyValue())) {
                LOG.info("渠道 " + taskCallChannel.getPropertyValue() + " 未设置定价");
                throw new TouchException("所选渠道未设置定价");
            }
            MarketTask marketTask = marketTaskDao.get(marketTaskId);

            JSONObject config = JSON.parseObject(priceconfig.getPropertyValue());
            if (marketTask.getTaskType() == 1 && config.containsKey("call_center_type") && "2".equals(config.getString("call_center_type"))) {
                MarketTaskUserRel taskUser = marketTaskDao.getMarketTaskUserRel(marketTaskId, lu.getId().toString());
                if (taskUser == null) {
                    LOG.info(lu.getId() + " 非任务成员");
                    throw new TouchException(" 非任务成员 ");
                }
            }
            if (marketTask.getStatus() != 1) {
                LOG.info("任务" + marketTaskId + "未开启");
                throw new TouchException("任务未开启");
            }
        }
        return true;
    }

    /**
     * 生成营销任务成功单数据
     *
     * @param loginUser
     * @param customerGroupId
     * @param
     * @param custId
     * @param invitationLabelId
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     * @return
     */
    public List<List<String>> generateMarketTaskSuccessData(LoginUser loginUser, int customerGroupId, MarketTask marketTask, String custId, String invitationLabelId,
                                                            String invitationLabelValue, String startTime, String endTime) {
        List<List<String>> data = new ArrayList<>();
        // 处理时间
        String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DatetimeUtils.DATE_TIME_FORMATTER);
        String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DatetimeUtils.DATE_TIME_FORMATTER);
        if (StringUtil.isNotEmpty(startTime)) {
            startTimeStr = LocalDateTime.parse(startTime, DatetimeUtils.DATE_TIME_FORMATTER).format(DatetimeUtils.DATE_TIME_FORMATTER);
        }
        if (StringUtil.isNotEmpty(endTime)) {
            endTimeStr = LocalDateTime.parse(endTime, DatetimeUtils.DATE_TIME_FORMATTER).format(DatetimeUtils.DATE_TIME_FORMATTER);
        }
        if (StringUtil.isNotEmpty(invitationLabelId)) {
            String nowMonth = DateUtil.getNowMonthToYYYYMM();
            if (StringUtil.isNotEmpty(startTimeStr) && StringUtil.isNotEmpty(endTimeStr)) {
                nowMonth = LocalDateTime.parse(endTimeStr, DatetimeUtils.DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyyMM"));
            }
            int marketProjectId = 0;
            CustomGroup customGroup = customGroupDao.get(customerGroupId);
            if (customGroup != null && customGroup.getMarketProjectId() != null) {
                marketProjectId = customGroup.getMarketProjectId();
            }
            // 获取所有自建属性数据
            //List<String> labelIdList = customerLabelDao.listLabelIds(custId, marketProjectId, true);
            List<CustomerLabelDTO> labels = customerLabelDao.listLabelIds(custId, marketProjectId, true);
            String labelDataLikeValue = "成功";
            StringBuffer sql = new StringBuffer();
            // 获取邀约成功,拨打电话成功用户的通话记录
            sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.market_task_id, voice.superid, voice.recordurl, voice.clue_audit_status, IFNULL(voice.clue_audit_reason, '') clue_audit_reason, ")
                    .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                    .append(" t.remark phonearea, t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street, t.intent_level ")
                    .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowMonth + " voice ")
                    .append(" JOIN " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTask.getId() + " t ON t.id = voice.superid ")
                    .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? AND voice.market_task_id = ? ")
                    .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                    .append(" AND voice.status = 1001 ");
            if (marketTask.getTaskType() != null && 3 == marketTask.getTaskType().intValue()) {
                sql.append(" AND t.intent_level IS NOT NULL ");
            } else {
                sql.append(" AND t.super_data LIKE '%" + labelDataLikeValue + "%' ");
            }
            if ("2".equals(loginUser.getUserType())) {
                // 组长查组员列表
                if ("1".equals(loginUser.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), custId);
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            sql.append(" AND voice.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }
                    }
                } else {
                    sql.append(" AND voice.user_id = '" + loginUser.getId() + "'");
                }
            }

            List<Map<String, Object>> callLogList = marketTaskDao.sqlQuery(sql.toString(), custId, customerGroupId, marketTask.getId(), startTimeStr, endTimeStr);
            // 处理营销记录
            if (callLogList.size() > 0) {
                //final String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                // 组合拼装为map,方便通过label_id和super_id快速查找数据
                Map<String, Object> invitationCustGroupSuperMap = new HashMap<>(16);
                Map<String, Object> invitationSuperLabelMap = new HashMap<>(16);

                // 当前营销任务下满足条件的身份ID集合
                Set<String> superIdSets = new HashSet<>();
                Set<String> userIdSets = new HashSet<>();
                Map<String, Object> labelData;
                String recordUrl = "";
                for (Map<String, Object> map : callLogList) {
                    if (map != null) {
                        if (map.get("superid") != null) {
                            superIdSets.add(String.valueOf(map.get("superid")));
                        }
                        if (map.get("user_id") != null) {
                            userIdSets.add(String.valueOf(map.get("user_id")));
                        }
                        // 拆解用户勾选的自建属性
                        if (map.get("super_data") != null && StringUtil.isNotEmpty(String.valueOf(map.get("super_data")))) {
                            labelData = JSON.parseObject(String.valueOf(map.get("super_data")), Map.class);
                            if (labelData != null && labelData.size() > 0) {
                                for (Map.Entry<String, Object> key : labelData.entrySet()) {
                                    invitationCustGroupSuperMap.put(customerGroupId + "_" + map.get("superid"), key.getValue());
                                    invitationSuperLabelMap.put(customerGroupId + "_" + key.getKey() + "_" + map.get("superid"), key.getValue());
                                }
                            }
                        }
                    }
                }
                // 查询用户姓名
                Map<String, Object> realNameMap = new HashMap<>();
                Map<String, Object> accountMap = new HashMap<>();
                if (userIdSets.size() > 0) {
                    List<Map<String, Object>> userList = marketTaskDao.sqlQuery("SELECT id, REALNAME, account FROM t_customer_user WHERE id IN (" + SqlAppendUtil.sqlAppendWhereIn(userIdSets) + ")");
                    for (Map<String, Object> map : userList) {
                        realNameMap.put(String.valueOf(map.get("id")), map.get("REALNAME"));
                        accountMap.put(String.valueOf(map.get("id")), map.get("account"));
                    }
                }

                // 根据superId查询手机号
                Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);
                List<String> columnList;
                String monthYear = null;
                for (Map<String, Object> row : callLogList) {
                    columnList = new ArrayList<>();
                    for (CustomerLabelDTO dto : labels) {
                        if (invitationSuperLabelMap.get(customerGroupId + "_" + dto.getLabelId() + "_" + row.get("superid")) != null) {
                            columnList.add(String.valueOf(invitationSuperLabelMap.get(customerGroupId + "_" + dto.getLabelId() + "_" + row.get("superid"))));
                        } else {
                            columnList.add("");
                        }
                    }
                    columnList.add(String.valueOf(row.get("superid")));
                    columnList.add(String.valueOf(row.get("customer_group_id")));
                    columnList.add(String.valueOf(row.get("market_task_id")));
                    // 普通员工取消手机号表头
                    if(!CustomerUserTypeEnum.STAFF_USER.getType().equals(loginUser.getUserType())){
                        columnList.add(PhoneAreaUtil.replacePhone(phoneMap.get(String.valueOf(row.get("superid")))));
                    }
                    //归属地
                    columnList.add(String.valueOf(row.get("phonearea")));
                    columnList.add(String.valueOf(realNameMap.get(String.valueOf(row.get("user_id")))));
                    columnList.add(String.valueOf(accountMap.get(String.valueOf(row.get("user_id")))));
                    columnList.add(String.valueOf(row.get("create_time")));

                    if (StringUtil.isNotEmpty(String.valueOf(row.get("create_time")))) {
                        monthYear = LocalDateTime.parse(String.valueOf(row.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                    }
                    columnList.add(CallUtil.generateRecordUrlMp3(monthYear, row.get("user_id"), row.get("touchId")));
                    if (marketTask.getTaskType() != null && 3 == marketTask.getTaskType().intValue()) {
                        columnList.add(String.valueOf(row.get("intent_level")));
                    }
                    // 通话审核状态
                    columnList.add(CallUtil.getClueAuditStatusName(String.valueOf(row.get("clue_audit_status"))));
                    // 通话审核失败原因
                    columnList.add(String.valueOf(row.get("clue_audit_reason")));
                    data.add(columnList);
                }
            }
        }
        return data;
    }

    /**
     * 生成营销任务成功单数据
     *
     * @param loginUser
     * @param customerGroupId
     * @param
     * @param custId
     * @param invitationLabelId
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     * @return
     */
    public List<Map<String, String>> generateMarketTaskSuccessVoiceData(LoginUser loginUser, int customerGroupId, MarketTask marketTask, String custId, String invitationLabelId,
                                                                        String invitationLabelValue, String startTime, String endTime) {
        List<Map<String, String>> data = new ArrayList<>();
        // 处理时间
        String startTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MIN).format(DatetimeUtils.DATE_TIME_FORMATTER);
        String endTimeStr = LocalDateTime.of(LocalDate.now(), LocalTime.MAX).format(DatetimeUtils.DATE_TIME_FORMATTER);
        if (StringUtil.isNotEmpty(startTime)) {
            startTimeStr = LocalDateTime.parse(startTime, DatetimeUtils.DATE_TIME_FORMATTER).format(DatetimeUtils.DATE_TIME_FORMATTER);
        }
        if (StringUtil.isNotEmpty(endTime)) {
            endTimeStr = LocalDateTime.parse(endTime, DatetimeUtils.DATE_TIME_FORMATTER).format(DatetimeUtils.DATE_TIME_FORMATTER);
        }
        if (StringUtil.isNotEmpty(invitationLabelId)) {
            String nowMonth = DateUtil.getNowMonthToYYYYMM();
            if (StringUtil.isNotEmpty(startTimeStr) && StringUtil.isNotEmpty(endTimeStr)) {
                nowMonth = LocalDateTime.parse(endTimeStr, DatetimeUtils.DATE_TIME_FORMATTER).format(DateTimeFormatter.ofPattern("yyyyMM"));
            }
            int marketProjectId = 0;
            CustomGroup customGroup = customGroupDao.get(customerGroupId);
            if (customGroup != null && customGroup.getMarketProjectId() != null) {
                marketProjectId = customGroup.getMarketProjectId();
            }
            List<Map<String, Object>> labelNames = marketTaskDao.sqlQuery("SELECT label_name, label_id, type FROM t_customer_label WHERE status = 1 AND cust_id = ? AND (market_project_id = 0 OR market_project_id is null OR market_project_id =?) ", custId, marketProjectId);
            // 获取所有自建属性数据
            List<String> labelIdList = new ArrayList<>();
            for (Map<String, Object> map : labelNames) {
                if (map != null && map.get("label_name") != null) {
                    labelIdList.add(String.valueOf(map.get("label_id")));
                }
            }
            String labelDataLikeValue = "成功";
            StringBuffer sql = new StringBuffer();
            // 获取邀约成功,拨打电话成功用户的通话记录
            sql.append("SELECT voice.touch_id touchId, voice.user_id, voice.customer_group_id, voice.market_task_id, voice.superid, voice.recordurl, ")
                    .append(" voice.create_time, voice.callSid, t.super_data, t.super_age, t.super_name, t.super_sex, ")
                    .append(" t.remark phonearea, t.super_telphone, t.super_phone, t.super_address_province_city, t.super_address_street, t.intent_level ")
                    .append(" FROM " + ConstantsUtil.TOUCH_VOICE_TABLE_PREFIX + nowMonth + " voice ")
                    .append(" JOIN " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTask.getId() + " t ON t.id = voice.superid ")
                    .append(" WHERE voice.cust_id = ? AND voice.customer_group_id = ? AND voice.market_task_id = ? ")
                    .append(" AND voice.create_time >= ? AND voice.create_time <= ?  ")
                    .append(" AND voice.status = 1001 ");
            if (marketTask.getTaskType() != null && 3 == marketTask.getTaskType().intValue()) {
                sql.append(" AND t.intent_level IS NOT NULL ");
            } else {
                sql.append(" AND t.super_data LIKE '%" + labelDataLikeValue + "%' ");
            }
            if ("2".equals(loginUser.getUserType())) {
                // 组长查组员列表
                if ("1".equals(loginUser.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(loginUser.getUserGroupId(), custId);
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            sql.append(" AND voice.user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ")");
                        }
                    }
                } else {
                    sql.append(" AND voice.user_id = '" + loginUser.getId() + "'");
                }
            }
            LOG.info("sql=" + sql.toString());
            List<Map<String, Object>> callLogList = marketTaskDao.sqlQuery(sql.toString(), custId, customerGroupId, marketTask.getId(), startTimeStr, endTimeStr);
            // 处理营销记录
            if (callLogList.size() > 0) {
                //final String audioUrl = ConfigUtil.getInstance().get("audio_server_url") + "/";
                // 组合拼装为map,方便通过label_id和super_id快速查找数据
                // 根据superId查询手机号
                //Map<String, Object> phoneMap = phoneService.getPhoneMap(superIdSets);
                String monthYear = null;
                for (Map<String, Object> row : callLogList) {
                    Map<String, String> map = new HashMap<>();
                    if (StringUtil.isNotEmpty(String.valueOf(row.get("create_time")))) {
                        monthYear = LocalDateTime.parse(String.valueOf(row.get("create_time")), DatetimeUtils.DATE_TIME_FORMATTER_SSS).format(DatetimeUtils.YYYY_MM);
                    }
                    Object superid = row.get("superid");
                    Object touchId = row.get("touchId");
                    //String recordUrl = CallUtil.generateRecordUrlMp3(monthYear, row.get("user_id"), row.get("touchId"));
                    Object recordUrl = row.get("recordurl");
                    map.put("superid", superid.toString());
                    map.put("touchId", touchId.toString());
                    map.put("userId", row.get("user_id").toString());
                    map.put("recordUrl", recordUrl.toString());
                    data.add(map);
                }
            }
        }
        return data;
    }

    /**
     * 获取导出成功单excel表头
     *
     * @param custId
     * @return
     */
    public List<List<String>> getSuccessExcelTitle(String custId, int marketProjectId) {
        List<CustomerLabelDTO> labels = customerLabelDao.listLabelIds(custId, marketProjectId, true);
        // 处理excel表头
        List<List<String>> headers = new ArrayList<>();
        List<String> head;
        Set<String> headNames = new HashSet<>();
        headNames.add("身份ID");
        headNames.add("客户群ID");
        headNames.add("营销任务ID");
        headNames.add("手机号");
        headNames.add("归属地");
        headNames.add("操作人");
        headNames.add("登录账号");
        headNames.add("时间");
        headNames.add("录音");
        headNames.add("意向度");

        for (CustomerLabelDTO map : labels) {
            if (StringUtil.isNotEmpty(map.getLabelName())) {
                head = new ArrayList<>();
                if (headNames.contains(map.getLabelName())) {
                    head.add(map.getLabelName() + map.getLabelId());
                } else {
                    head.add(map.getLabelName());
                    headNames.add(map.getLabelName());
                }
                headers.add(head);
            }
        }
        head = new ArrayList<>();
        head.add("身份ID");
        headers.add(head);

        head = new ArrayList<>();
        head.add("客户群ID");
        headers.add(head);

        head = new ArrayList<>();
        head.add("营销任务ID");
        headers.add(head);

        head = new ArrayList<>();
        head.add("手机号");
        headers.add(head);

        head = new ArrayList<>();
        head.add("归属地");
        headers.add(head);

        head = new ArrayList<>();
        head.add("操作人");
        headers.add(head);

        head = new ArrayList<>();
        head.add("登录账号");
        headers.add(head);

        head = new ArrayList<>();
        head.add("时间");
        headers.add(head);

        head = new ArrayList<>();
        head.add("录音");
        headers.add(head);
        return headers;
    }

    /**
     * 获取导出成功单excel表头,普通员工不导出手机号表头
     * @param user
     * @param marketProjectId
     * @return
     */
    public List<List<String>> getSuccessExcelTitle(LoginUser user, int marketProjectId) {
        List<CustomerLabelDTO> labels = customerLabelDao.listLabelIds(user.getCustId(), marketProjectId, true);
        // 处理excel表头
        List<List<String>> headers = new ArrayList<>();
        List<String> head;
        Set<String> headNames = new HashSet<>();
        headNames.add("身份ID");
        headNames.add("客户群ID");
        headNames.add("营销任务ID");
        // 普通员工取消手机号表头
        if(!CustomerUserTypeEnum.STAFF_USER.getType().equals(user.getUserType())){
            headNames.add("手机号");
        }
        headNames.add("归属地");
        headNames.add("操作人");
        headNames.add("登录账号");
        headNames.add("时间");
        headNames.add("录音");
        headNames.add("意向度");
        headNames.add("人工审核");
        headNames.add("审核失败原因");

        for (CustomerLabelDTO map : labels) {
            if (StringUtil.isNotEmpty(map.getLabelName())) {
                head = new ArrayList<>();
                if (headNames.contains(map.getLabelName())) {
                    head.add(map.getLabelName() + map.getLabelId());
                } else {
                    head.add(map.getLabelName());
                    headNames.add(map.getLabelName());
                }
                headers.add(head);
            }
        }
        head = new ArrayList<>();
        head.add("身份ID");
        headers.add(head);

        head = new ArrayList<>();
        head.add("客户群ID");
        headers.add(head);

        head = new ArrayList<>();
        head.add("营销任务ID");
        headers.add(head);
        // 普通员工取消手机号表头
        if(!CustomerUserTypeEnum.STAFF_USER.getType().equals(user.getUserType())){
            head = new ArrayList<>();
            head.add("手机号");
            headers.add(head);
        }

        head = new ArrayList<>();
        head.add("归属地");
        headers.add(head);

        head = new ArrayList<>();
        head.add("操作人");
        headers.add(head);

        head = new ArrayList<>();
        head.add("登录账号");
        headers.add(head);

        head = new ArrayList<>();
        head.add("时间");
        headers.add(head);

        head = new ArrayList<>();
        head.add("录音");
        headers.add(head);

        head = new ArrayList<>();
        head.add("人工审核");
        headers.add(head);

        head = new ArrayList<>();
        head.add("审核失败原因");
        headers.add(head);
        return headers;
    }

    /**
     * 导出满足自建属性的单个营销任务的成功单
     *
     * @param response
     * @param loginUser
     * @param marketTaskId
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     */
    public void exportMarketTaskSuccessToExcel(HttpServletResponse response, LoginUser loginUser, String marketTaskId, String invitationLabelId, String invitationLabelName, String invitationLabelValue, String startTime, String endTime) {
        Map<String, Object> msg = new HashMap<>();
        OutputStream outputStream = null;
        try {
            outputStream = response.getOutputStream();
            String custId = loginUser.getCustId();
            MarketTask cg = marketTaskDao.get(marketTaskId);
            if (cg == null) {
                msg.put("msg", "未查询到该营销任务:" + marketTaskId);
                msg.put("data", dataExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            int customerGroupId = cg.getCustomerGroupId();
            // 处理管理员权限
            if (StringUtil.isEmpty(custId)) {
                custId = cg.getCustId();
            }
            // 处理导出间隔时间
            if (dataExportTime >= 0 && (System.currentTimeMillis() - dataExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", dataExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            dataExportTime = System.currentTimeMillis();
            int marketProjectId = 0;
            // 处理根据自建属性名称和属性值导出成功单
            if (StringUtil.isEmpty(invitationLabelId) && StringUtil.isNotEmpty(invitationLabelName)) {
                CustomGroup customGroup = customGroupDao.get(cg.getCustomerGroupId());
                if (customGroup != null && customGroup.getMarketProjectId() != null) {
                    marketProjectId = customGroup.getMarketProjectId();
                }
                List<CustomerLabelDTO> labels = customerLabelDao.listLabelIds(custId, marketProjectId, true);
                for (CustomerLabelDTO dto : labels) {
                    if (StringUtil.isNotEmpty(invitationLabelName) && invitationLabelName.equals(dto.getLabelName())) {
                        invitationLabelId = String.valueOf(dto.getLabelId());
                        break;
                    }
                }
            }
            if (StringUtil.isEmpty(invitationLabelId)) {
                msg.put("msg", "未查询满足条件的自建属性");
                msg.put("data", dataExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            // 获取excel表头
            List<List<String>> headers = getSuccessExcelTitle(loginUser, marketProjectId);
            if (cg.getTaskType() != null && 3 == cg.getTaskType().intValue()) {
                List<String> intent = new ArrayList();
                intent.add("意向度");
                headers.add(intent);
            }
            List<List<String>> data = generateMarketTaskSuccessData(loginUser, customerGroupId, cg, custId, invitationLabelId, invitationLabelValue, startTime, endTime);
            //构造excel返回数据
            String fileName = "营销任务" + marketTaskId + "成功单";
            if (data.size() > 0) {
                final String fileType = ".xlsx";
                response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
                response.setContentType("application/vnd.ms-excel;charset=utf-8");
                ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
                Sheet sheet1 = new Sheet(1, 0);
                sheet1.setHead(headers);
                sheet1.setSheetName("营销数据");
                writer.write0(data, sheet1);
                writer.finish();
            } else {
                msg.put("msg", "营销任务下无满足条件的客户数据,Id:" + marketTaskId);
                msg.put("data", String.valueOf(dataExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
        } catch (Exception e) {
            LOG.error("导出单个营销任务成功单异常,Id:" + marketTaskId, e);
        } finally {
            dataExportTime = 0;
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
            } catch (IOException e) {
                LOG.error("导出单个营销任务成功单异常,Id:" + marketTaskId, e);
            }
        }
    }

    /**
     * 导出营销任务成功单的录音文件
     *
     * @param response
     * @param loginUser
     * @param marketTaskId
     * @param invitationLabelId
     * @param invitationLabelName
     * @param invitationLabelValue
     * @param startTime
     * @param endTime
     */
    public void exportMarketTaskSuccessVoice(HttpServletResponse response, LoginUser loginUser, String marketTaskId, String invitationLabelId, String invitationLabelName, String invitationLabelValue, String startTime, String endTime) {
        Map<String, Object> msg = new HashMap<>();
        OutputStream outputStream = null;
        String zipFilePath = null;
        String custId = loginUser.getCustId();
        String key = "downloadMarketTaskVoice_" + custId + "_" + marketTaskId;
        try {
            outputStream = response.getOutputStream();
            String s = redisUtil.get(key);
            if (StringUtil.isNotEmpty(s)) {
                msg.put("msg", "请稍后重试");
                msg.put("data", voiceExportTime);
                LOG.warn("导出营销任务:{}录音请稍后重试,", marketTaskId);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                return;
            }
            redisUtil.setex(key, 60 * 5, "1");
            MarketTask cg = marketTaskDao.get(marketTaskId);
            if (cg == null) {
                msg.put("msg", "未查询到该营销任务:" + marketTaskId);
                msg.put("data", voiceExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                LOG.warn("未查询到该营销任务:{},", marketTaskId);
                return;
            }
            int customerGroupId = cg.getCustomerGroupId();
            // 处理管理员权限
            if (StringUtil.isEmpty(custId)) {
                custId = cg.getCustId();
            }
            // 处理导出间隔时间
            if (voiceExportTime >= 0 && (System.currentTimeMillis() - voiceExportTime) < 5 * 60 * 1000) {
                msg.put("msg", "请稍后重试");
                msg.put("data", voiceExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                redisUtil.del(key);
                LOG.warn("导出营销任务:{}录音请稍后重试,", marketTaskId);
                return;
            }
            voiceExportTime = System.currentTimeMillis();
            int marketProjectId = 0;
            // 处理根据自建属性名称和属性值导出成功单录音
            if (StringUtil.isEmpty(invitationLabelId) && StringUtil.isNotEmpty(invitationLabelName)) {
                CustomGroup customGroup = customGroupDao.get(cg.getCustomerGroupId());
                if (customGroup != null && customGroup.getMarketProjectId() != null) {
                    marketProjectId = customGroup.getMarketProjectId();
                }
                LOG.info("导出营销任务:{}开始查询自建属性,", marketTaskId);
                List<CustomerLabelDTO> labels = customerLabelDao.listLabelIds(custId, marketProjectId, true);
                LOG.info("导出营销任务:{}自建属性:{},", JSON.toJSONString(labels));
                for (CustomerLabelDTO dto : labels) {
                    if (StringUtil.isNotEmpty(invitationLabelName) && invitationLabelName.equals(dto.getLabelName())) {
                        invitationLabelId = String.valueOf(dto.getLabelId());
                        break;
                    }
                }
            }

            LOG.info("invitationLabelId==" + invitationLabelId);
            if (StringUtil.isEmpty(invitationLabelId)) {
                msg.put("msg", "未查询满足条件的自建属性");
                msg.put("data", voiceExportTime);
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                redisUtil.del(key);
                LOG.warn("导出营销任务:{}未查询满足条件的自建属性,", marketTaskId);
                return;
            }

            List<Map<String, String>> data = generateMarketTaskSuccessVoiceData(loginUser, customerGroupId, cg, custId, invitationLabelId, invitationLabelValue, startTime, endTime);
            LOG.info("data:::" + data);
            //创建zip文件输出流
            if (data.size() > 0) {
                LOG.info("data.size==" + data.size());
                //构造zip返回数据
                response.setContentType("text/html; charset=UTF-8");
                response.setContentType("application/octet-stream");
                String zipName = "营销任务录音文件-" + marketTaskId + ".zip";
                zipFilePath = "/tmp/" + loginUser.getId() + "/";
                creatFile(zipFilePath, zipName);
                LOG.info("创建空的zip包文件路径：" + zipFilePath + "zip文件名：" + zipName);
                File zip = new File(zipFilePath + zipName);
                LOG.info("zip包文件路径：" + zip.getPath());
                ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zip));
                for (Map<String, String> d : data) {
                    String recordUrl = d.get("recordUrl");
                    String superid = d.get("superid");
                    String touchId = d.get("touchId");
                    String userid = d.get("userId");
                    LOG.info("userid::" + userid + ";recordUrl=" + recordUrl);
                    InputStream inputStream = marketResourceService.getVoiceInputStream(userid, recordUrl);
                    if (inputStream != null) {
                        BufferedInputStream bis = new BufferedInputStream(inputStream);
                        //将文件写入zip内，即将文件进行打包
                        zos.putNextEntry(new ZipEntry(superid + "-" + touchId + ".mp3"));
                        int size = 0;
                        byte[] buffer = new byte[1024];
                        while ((size = bis.read(buffer)) > 0) {
                            zos.write(buffer, 0, size);
                        }
                        zos.closeEntry();
                        bis.close();
                        inputStream.close();
                    }
                }
                zos.close();
                response.setHeader("Content-disposition", "attachment;filename=" + zipName);
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(zipFilePath + zipName));
                byte[] buff = new byte[bis.available()];
                bis.read(buff);
                bis.close();
                outputStream.write(buff);
            } else {
                msg.put("msg", "营销任务下无满足条件的数据,Id:" + marketTaskId);
                msg.put("data", String.valueOf(voiceExportTime));
                outputStream.write(JSON.toJSONString(msg).getBytes("UTF-8"));
                LOG.warn("导出营销任务:{}营销任务下无满足条件的数据,", marketTaskId);
                return;
            }
        } catch (Exception e) {
            LOG.error("导出营销任务成功单" + marketTaskId + "录音异常,Id:" + marketTaskId, e);
        } finally {
            redisUtil.del(key);
            voiceExportTime = 0;
            try {
                if (outputStream != null) {
                    outputStream.flush();
                    outputStream.close();
                }
                response.flushBuffer();
                if (zipFilePath != null) {
                    File f = new File(zipFilePath);
                    if (f.exists()) {
                        f.delete();
                    }
                }
            } catch (IOException e) {
                LOG.error("导出营销任务成功单" + marketTaskId + "录音异常,Id:" + marketTaskId, e);
            }
        }
    }


    public static void creatFile(String filePath, String fileName) {
        File folder = new File(filePath);
        //文件夹路径不存在
        if (!folder.exists() && !folder.isDirectory()) {
            folder.mkdirs();
        } else {
            LOG.info("文件夹路径存在:" + filePath);
        }

        // 如果文件不存在就创建
        File file = new File(filePath + fileName);
        if (file.exists()) {
            file.delete();
        }
        try {
            file.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * base64编码写入到那个目录下，并修改文件名字
     *
     * @param base64
     * @param filePath
     * @param marketTaskId
     * @param superid
     */
    public static String decryptByBase64(String base64, String filePath, String marketTaskId, String superid) {
        if (base64 == null && filePath == null) {
            return "生成文件失败，请给出相应的数据。";
        }
        try {
            Files.write(Paths.get(filePath), Base64.getDecoder().decode(base64), StandardOpenOption.CREATE);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "指定路径下生成文件成功！";
    }

    //文件转成base64编码字符串
    public static String encryptToBase64(String filePath) {
        if (filePath == null) {
            return null;
        }
        try {
            byte[] b = Files.readAllBytes(Paths.get(filePath));
            return Base64.getEncoder().encodeToString(b);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static void main(String[] args) {
        String filepath = "C:\\Users\\dikeeee\\Downloads\\2019081811140656470000190802110849000014.mp3";
        String f = encryptToBase64(filepath);
        System.out.println(f);
        decryptByBase64(f, "C:\\Users\\dikeeee\\Downloads\\123.mp3", "1231", "432432");

    }

    /**
     * 生成zip包，并返回路径
     *
     * @param filePath
     */
    private String generateZip(String filePath) {


        return "";
    }

    /**
     * 查询营销任务详情
     *
     * @param marketTaskId
     * @return
     */
    public MarketTaskDTO selectMarketTask(String marketTaskId) {
        MarketTask marketTask = marketTaskDao.get(marketTaskId);
        if (marketTask == null) {
            LOG.warn("营销任务:" + marketTaskId + "不存在");
            return null;
        }
        // 查询外显
        String apparentNum = null;
        MarketTaskProperty apparentNumber = marketTaskDao.getProperty(marketTaskId, "apparentNumber");
        if (apparentNumber != null) {
            apparentNum = apparentNumber.getPropertyValue();
        }
        String csp = null;
        // 查询呼叫速度
        MarketTaskProperty callSpeed = marketTaskDao.getProperty(marketTaskId, "callSpeed");
        if (callSpeed != null) {
            csp = callSpeed.getPropertyValue();
        }
        // 查询呼叫次数
        String ccn = null;
        MarketTaskProperty callCount = marketTaskDao.getProperty(marketTaskId, "callCount");
        if (callCount != null) {
            ccn = callCount.getPropertyValue();
        }
        MarketTaskDTO dto = new MarketTaskDTO(marketTask, apparentNum,
                StringUtil.isNotEmpty(csp) ? NumberConvertUtil.parseInt(csp) : null,
                StringUtil.isNotEmpty(ccn) ? NumberConvertUtil.parseInt(ccn) : null);

        // 查询呼叫渠道
        MarketTaskProperty cChannel = marketTaskDao.getProperty(marketTaskId, "callChannel");
        if (cChannel != null && StringUtil.isNotEmpty(cChannel.getPropertyValue())) {
            dto.setCallChannel(cChannel.getPropertyValue());
            MarketResourceDTO mr = marketResourceDao.getInfoProperty(NumberConvertUtil.parseInt(cChannel.getPropertyValue()), "price_config");
            if (mr != null && mr.getTypeCode() != null) {
                dto.setCallChannelName(mr.getResname());
                JSONObject priceConfig = JSON.parseObject(mr.getResourceProperty());
                if (priceConfig != null && mr.getTypeCode() != null) {
                    LOG.info("TYPECODE:" + mr.getTypeCode());
                    if (mr.getTypeCode() == 1) {
                        dto.setCallType(priceConfig.getInteger("type"));
                        dto.setCallCenterType(priceConfig.getInteger("call_center_type"));
                        LOG.info(dto.getCallCenterType() + ";call_center_config: " + priceConfig.getString("call_center_config"));
                        if (dto.getCallCenterType() != null && dto.getCallCenterType() == 2) {
                            LOG.info(dto.getCallCenterType() + ";call_center_config: " + priceConfig.getString("call_center_config"));
                            String callCenterConfig = priceConfig.getString("call_center_config");
                            dto.setCallCenterConfig(callCenterConfig);
                        }
                    } else if (mr.getTypeCode() == 2) {
                        dto.setSmsType(priceConfig.getInteger("type"));
                    }
                }
            }
        }
        return dto;
    }

    /**
     * 统计单个营销任务呼叫数据
     *
     * @param timeType
     * @param marketTaskId
     * @param userQueryParam
     * @param startTime
     * @param endTime
     * @return
     */
    public Map<String, Object> statMarketTaskCallData(int timeType, String marketTaskId, UserQueryParam userQueryParam, String startTime, String endTime, String workPlaceId) {
        Map<String, Object> data = new HashMap<>();
        // 呼叫量
        data.put("callSum", 0);
        // 接通量
        data.put("calledSum", 0);
        // 成功量
        data.put("successSum", 0);
        // 未通量
        data.put("failSum", 0);
        // 参与坐席数
        data.put("callSeatSum", 0);

        data.put("labelListData", new ArrayList<>());
        //通话时长范围统计
        data.put("durationType1", 0);
        data.put("durationType2", 0);
        data.put("durationType3", 0);
        data.put("durationType4", 0);
        data.put("durationType5", 0);
        data.put("durationType6", 0);

        try {
            if (StringUtil.isEmpty(marketTaskId)) {
                LOG.warn("marketTaskId参数异常");
                return data;
            }
            int taskType = -1;
            MarketTask marketTask = marketTaskDao.get(marketTaskId);
            if (marketTask == null) {
                LOG.warn("未查询到指定营销任务:" + marketTaskId);
                return data;
            }
            userQueryParam.setCustId(marketTask.getCustId());
            if (marketTask.getTaskType() != null) {
                taskType = marketTask.getTaskType();
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    LOG.warn("查询营销任务统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return data;
                }
            } else if (2 == timeType) {
                // 查询全部统计分析
                if (marketTask.getTaskCreateTime() != null && marketTask.getTaskEndTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getTaskCreateTime().getTime());
                    localEndDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getTaskEndTime().getTime());
                } else if (marketTask.getCreateTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getCreateTime().getTime());
                    localEndDateTime = LocalDateTime.now();
                    LOG.warn("营销任务创建和结束为空默认取营销任务的创建时间:" + marketTask.getCreateTime() + ",nowTime:" + localEndDateTime);
                } else {
                    LOG.warn("查询营销任务统计分析时间营销任务开始和结束时间异常,taskCreateTime:" + marketTask.getTaskCreateTime() + ",taskEndTime:" + marketTask.getTaskEndTime());
                    return data;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append("SELECT IFNULL(SUM(caller_sum),0) caller_sum, IFNULL(SUM(called_sum),0) called_sum, " +
                    " IFNULL(SUM(busy_sum),0) busy_sum, IFNULL(SUM(no_service_area_sum),0) no_service_area_sum, IFNULL(SUM(phone_overdue_sum),0) phone_overdue_sum, " +
                    " IFNULL(SUM(phone_shutdown_sum),0) phone_shutdown_sum, IFNULL(SUM(space_phone_sum),0) space_phone_sum, IFNULL(SUM(other_sum),0) other_sum, " +
                    " IFNULL(SUM(order_sum),0) order_sum, IFNULL(SUM(called_duration_type1),0) durationType1, IFNULL(SUM(called_duration_type2),0) durationType2, IFNULL(SUM(called_duration_type3),0) durationType3, IFNULL(SUM(called_duration_type4),0) durationType4, " +
                    " IFNULL(SUM(called_duration_type5),0) durationType5, IFNULL(SUM(called_duration_type6),0) durationType6, count(distinct(user_id)) callSeatSum FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND market_task_id = ?");

            List<Map<String, Object>> statCallList;
            // 通话记录查询用户权限
            Set voiceUserIds = new HashSet();
            //普通用户权限处理
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                            voiceUserIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                        voiceUserIds.add(userQueryParam.getUserId());
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                    voiceUserIds.add(userQueryParam.getUserId());
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    return data;
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            statCallList = this.marketTaskDao.sqlQuery(sqlSb.toString(), startTime, endTime, marketTaskId);

            // 呼叫量,接通量,未通量, 成单量
            long callSum = 0L, calledSum = 0L, failSum = 0L, successSum = 0L, busySum = 0L, callSeatSum = 0L,
                    noServiceSum = 0L, phoneOverdueSum = 0L, phoneShutdownSum = 0L, spacePhoneSum = 0L, otherSum = 0L, durationType1 = 0L, durationType2 = 0L, durationType3 = 0L, durationType4 = 0L, durationType5 = 0L, durationType6 = 0L;
            if (statCallList.size() > 0) {
                callSum = NumberConvertUtil.parseLong(statCallList.get(0).get("caller_sum"));
                calledSum = NumberConvertUtil.parseLong(statCallList.get(0).get("called_sum"));
                busySum = NumberConvertUtil.parseLong(statCallList.get(0).get("busy_sum"));
                noServiceSum = NumberConvertUtil.parseLong(statCallList.get(0).get("no_service_area_sum"));
                phoneOverdueSum = NumberConvertUtil.parseLong(statCallList.get(0).get("phone_overdue_sum"));
                phoneShutdownSum = NumberConvertUtil.parseLong(statCallList.get(0).get("phone_shutdown_sum"));
                spacePhoneSum = NumberConvertUtil.parseLong(statCallList.get(0).get("space_phone_sum"));
                otherSum = NumberConvertUtil.parseLong(statCallList.get(0).get("other_sum"));
                failSum = busySum + noServiceSum + phoneOverdueSum + phoneShutdownSum + spacePhoneSum + otherSum;
                successSum = NumberConvertUtil.parseLong(statCallList.get(0).get("order_sum"));
                callSeatSum = NumberConvertUtil.parseLong(statCallList.get(0).get("callSeatSum"));
                // 通话时长范围统计
                durationType1 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType1", 0));
                durationType2 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType2", 0));
                durationType3 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType3", 0));
                durationType4 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType4", 0));
                durationType5 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType5", 0));
                durationType6 = NumberConvertUtil.parseLong(statCallList.get(0).getOrDefault("durationType6", 0));
            }

            // 呼叫量
            data.put("callSum", callSum);
            // 接通量
            data.put("calledSum", calledSum);
            // 未通量
            data.put("failSum", failSum);
            data.put("busySum", busySum);
            data.put("noServiceSum", noServiceSum);
            data.put("phoneOverdueSum", phoneOverdueSum);
            data.put("phoneShutdownSum", phoneShutdownSum);
            data.put("spacePhoneSum", spacePhoneSum);
            data.put("otherSum", otherSum);
            // 参与坐席数
            data.put("callSeatSum", callSeatSum);
            // 成功量
            data.put("successSum", successSum);
            // 通话范围统计
            data.put("durationType1", durationType1);
            data.put("durationType2", durationType2);
            data.put("durationType3", durationType3);
            data.put("durationType4", durationType4);
            data.put("durationType5", durationType5);
            data.put("durationType6", durationType6);

            // 处理自建属性标记数据
            Map<Object, Object> singleLabel = customerLabelService.getCustomAndSystemLabel(userQueryParam.getCustId());

            StringBuilder superSql = new StringBuilder();
            superSql.append("SELECT label_id, GROUP_CONCAT(option_value) option_value, GROUP_CONCAT(tag_sum) tag_sum, IFNULL(SUM(tag_sum),0) sum FROM stat_u_label_data WHERE market_task_id =? AND stat_time BETWEEN ? AND ?  ");
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    return data;
                }
                superSql.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            superSql.append(" GROUP BY label_id ");
            List<Map<String, Object>> labelList = marketTaskDao.sqlQuery(superSql.toString(), marketTaskId, startTime, endTime);
            List<Map<String, Object>> labelListData = new ArrayList<>();
            if (labelList != null && labelList.size() > 0) {
                List<String> labelOptionNameList;
                Map<String, Object> labelMapData, valueMap;
                Map<String, Integer> optionValueMap;
                List<Map<String, Object>> labelOptionListData;
                String optionValue, tagSum, percent;
                String[] options, tags;
                long sum = 0;
                for (Map<String, Object> m : labelList) {
                    labelOptionListData = new ArrayList<>();
                    labelOptionNameList = new ArrayList<>();
                    labelMapData = new HashMap<>();
                    optionValueMap = new HashMap<>();
                    labelMapData.put("labelId", m.get("label_id"));
                    labelMapData.put("title", singleLabel.get(m.get("label_id")));
                    optionValue = String.valueOf(m.get("option_value"));
                    tagSum = String.valueOf(m.get("tag_sum"));
                    // 单个自建属性标记总数
                    sum = NumberConvertUtil.parseLong(m.get("sum"));
                    if (StringUtil.isNotEmpty(optionValue) && StringUtil.isNotEmpty(tagSum)) {
                        options = optionValue.split(",");
                        tags = tagSum.split(",");
                        for (int i = 0; i < options.length; i++) {
                            if (optionValueMap.get(options[i]) != null) {
                                optionValueMap.put(options[i], optionValueMap.get(options[i]) + NumberConvertUtil.parseInt(tags[i]));
                            } else {
                                optionValueMap.put(options[i], NumberConvertUtil.parseInt(tags[i]));
                            }
                        }
                        for (Map.Entry<String, Integer> v : optionValueMap.entrySet()) {
                            valueMap = new HashMap<>();
                            valueMap.put("name", v.getKey());
                            valueMap.put("count", v.getValue());
                            percent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(v.getValue()), sum);
                            valueMap.put("percent", percent);
                            labelOptionNameList.add(v.getKey());
                            labelOptionListData.add(valueMap);
                        }
                    }
                    labelMapData.put("names", labelOptionNameList);
                    labelMapData.put("values", labelOptionListData);
                    labelListData.add(labelMapData);
                }
            }
            data.put("labelListData", labelListData);
        } catch (Exception e) {
            LOG.error("获取营销任务:" + marketTaskId + "统计分析异常,", e);
        }
        return data;
    }

    /**
     * 统计单个营销任务下所有坐席的呼叫数据
     *
     * @param timeType
     * @param marketTaskId
     * @param userQueryParam
     * @param startTime
     * @param endTime
     * @param workPlaceId    职场ID
     * @return
     */
    public Map<String, Object> statCGUserCallData(int timeType, String marketTaskId, UserQueryParam userQueryParam, String startTime, String endTime, String workPlaceId) {
        Map<String, Object> data = new HashMap<>();
        data.put("list", new ArrayList<>());
        data.put("total", 0);
        data.put("calledSum", 0);
        // 成功量
        data.put("successSum", 0);

        try {
            if (StringUtil.isEmpty(marketTaskId)) {
                LOG.warn("marketTaskId参数异常");
                return data;
            }
            int taskType = -1;
            MarketTask marketTask = marketTaskDao.get(marketTaskId);
            if (marketTask == null) {
                LOG.warn("未查询到指定营销任务:" + marketTaskId);
                return data;
            }
            userQueryParam.setCustId(marketTask.getCustId());
            if (marketTask.getTaskType() != null) {
                taskType = marketTask.getTaskType();
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    LOG.warn("查询营销任务统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return data;
                }
            } else if (2 == timeType) {
                // 查询全部统计分析
                if (marketTask.getTaskCreateTime() != null && marketTask.getTaskEndTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getTaskCreateTime().getTime());
                    localEndDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getTaskEndTime().getTime());
                } else if (marketTask.getCreateTime() != null) {
                    localEndDateTime = LocalDateTime.now();
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getCreateTime().getTime());
                    LOG.warn("营销任务创建和结束为空默认取营销任务的创建时间:" + marketTask.getCreateTime() + ",nowTime:" + localEndDateTime);
                } else {
                    LOG.warn("查询营销任务统计分析时间营销任务开始和结束时间异常,taskCreateTime:" + marketTask.getTaskCreateTime() + ",taskEndTime:" + marketTask.getTaskEndTime());
                    return data;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

            // 查询用户呼叫数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append(" SELECT customer_group_id, user_id, IFNULL(SUM(caller_sum),0) caller_sum,IFNULL(SUM(called_sum),0) called_sum, IFNULL(SUM(order_sum),0) order_sum, " +
                    " IFNULL(SUM(called_duration),0) called_duration FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ? AND market_task_id = ?");
            Page page;
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    return data;
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            sqlSb.append(" AND user_id <>'' ");
            sqlSb.append(" GROUP BY user_id ");
            // 呼叫量,接通量,未通量, 成单量
            long calledSum = 0L, successSum = 0L;

            page = this.marketTaskDao.sqlPageQuery0(sqlSb.toString(), userQueryParam.getPageNum(), userQueryParam.getPageSize(), startTime, endTime, marketTask.getCustomerGroupId(), marketTaskId);
            if (page.getData() != null && page.getData().size() > 0) {
                Map<String, Object> m;
                for (int i = 0; i < page.getData().size(); i++) {
                    m = (Map<String, Object>) page.getData().get(i);
                    m.put("userName", customerUserDao.getLoginName(String.valueOf(m.get("user_id"))));
                    calledSum += NumberConvertUtil.parseLong(m.get("called_sum"));
                    successSum += NumberConvertUtil.parseLong(m.get("order_sum"));
                }
            }

            data.put("list", page.getData());
            data.put("total", page.getTotal());
            data.put("calledSum", calledSum);
        } catch (Exception e) {
            LOG.error("获取营销任务:" + marketTaskId + "统计分析异常,", e);
        }
        return data;
    }

    /**
     * 导出营销任务统计数据
     *
     * @param timeType
     * @param marketTaskId
     * @param userQueryParam
     * @param startTime
     * @param endTime
     * @param response
     */
    public void exportMarketTaskCallData(int timeType, String marketTaskId, UserQueryParam userQueryParam, String startTime, String endTime, HttpServletResponse response, String workPlaceId) {
        try (OutputStream outputStream = response.getOutputStream()) {
            if (StringUtil.isEmpty(marketTaskId)) {
                LOG.warn("marketTaskId参数异常");
                return;
            }
            int taskType = -1;
            MarketTask marketTask = marketTaskDao.get(marketTaskId);
            if (marketTask == null) {
                LOG.warn("未查询到指定营销任务:" + marketTaskId);
                return;
            }
            userQueryParam.setCustId(marketTask.getCustId());
            if (marketTask.getTaskType() != null) {
                taskType = marketTask.getTaskType();
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    LOG.warn("查询营销任务统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return;
                }
            } else if (2 == timeType) {
                // 查询全部统计分析
                if (marketTask.getTaskCreateTime() != null && marketTask.getTaskEndTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getTaskCreateTime().getTime());
                    localEndDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getTaskEndTime().getTime());
                } else if (marketTask.getCreateTime() != null) {
                    localEndDateTime = LocalDateTime.now();
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getCreateTime().getTime());
                    LOG.warn("营销任务创建和结束为空默认取营销任务的创建时间:" + marketTask.getCreateTime() + ",nowTime:" + localEndDateTime);
                } else {
                    LOG.warn("查询营销任务统计分析时间营销任务开始和结束时间异常,taskCreateTime:" + marketTask.getTaskCreateTime() + ",taskEndTime:" + marketTask.getTaskEndTime());
                    return;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append("SELECT IFNULL(SUM(caller_sum),0) caller_sum, IFNULL(SUM(called_sum),0) called_sum, " +
                    " IFNULL(SUM(busy_sum),0) busy_sum, IFNULL(SUM(no_service_area_sum),0) no_service_area_sum, IFNULL(SUM(phone_overdue_sum),0) phone_overdue_sum, " +
                    " IFNULL(SUM(phone_shutdown_sum),0) phone_shutdown_sum, IFNULL(SUM(space_phone_sum),0) space_phone_sum, IFNULL(SUM(other_sum),0) other_sum, " +
                    " IFNULL(SUM(order_sum),0) order_sum, IFNULL(SUM(called_duration_type1),0) durationType1, IFNULL(SUM(called_duration_type2),0) durationType2, IFNULL(SUM(called_duration_type3),0) durationType3, IFNULL(SUM(called_duration_type4),0) durationType4, " +
                    " IFNULL(SUM(called_duration_type5),0) durationType5, IFNULL(SUM(called_duration_type6),0) durationType6 FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ? AND market_task_id = ?");

            List<Map<String, Object>> statList;
            // 通话记录查询用户权限
            Set voiceUserIds = new HashSet();
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                            voiceUserIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                        voiceUserIds.add(userQueryParam.getUserId());
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                    voiceUserIds.add(userQueryParam.getUserId());
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            statList = this.marketTaskDao.sqlQuery(sqlSb.toString(), startTime, endTime, marketTask.getCustomerGroupId(), marketTaskId);
            // 呼叫量,接通量,未通量, 成单量
            long callSum = 0L, calledSum = 0L, failSum = 0L, successSum = 0L, busySum = 0L,
                    noServiceSum = 0L, phoneOverdueSum = 0L, phoneShutdownSum = 0L, spacePhoneSum = 0L, otherSum = 0L, durationType1 = 0L, durationType2 = 0L, durationType3 = 0L, durationType4 = 0L, durationType5 = 0L, durationType6 = 0L;
            if (statList.size() > 0) {
                callSum = NumberConvertUtil.parseLong(statList.get(0).get("caller_sum"));
                calledSum = NumberConvertUtil.parseLong(statList.get(0).get("called_sum"));
                busySum = NumberConvertUtil.parseLong(statList.get(0).get("busy_sum"));
                noServiceSum = NumberConvertUtil.parseLong(statList.get(0).get("no_service_area_sum"));
                phoneOverdueSum = NumberConvertUtil.parseLong(statList.get(0).get("phone_overdue_sum"));
                phoneShutdownSum = NumberConvertUtil.parseLong(statList.get(0).get("phone_shutdown_sum"));
                spacePhoneSum = NumberConvertUtil.parseLong(statList.get(0).get("space_phone_sum"));
                otherSum = NumberConvertUtil.parseLong(statList.get(0).get("other_sum"));
                failSum = busySum + noServiceSum + phoneOverdueSum + phoneShutdownSum + spacePhoneSum + otherSum;
                successSum = NumberConvertUtil.parseLong(statList.get(0).get("order_sum"));
                // 通话时长范围统计
                durationType1 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType1", 0));
                durationType2 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType2", 0));
                durationType3 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType3", 0));
                durationType4 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType4", 0));
                durationType5 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType5", 0));
                durationType6 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType6", 0));
            }

            String fileName = "营销任务统计数据-" + marketTaskId + "-" + System.currentTimeMillis();
            final String fileType = ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            ExcelWriter writer = new ExcelWriter(outputStream, ExcelTypeEnum.XLSX);
            int sheetNum = 1;
            List<List<String>> data, headers;
            List<String> columnList, head;

            data = new ArrayList<>();

            headers = new ArrayList<>();
            head = new ArrayList<>();
            head.add("呼叫量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("未通量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通率");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功率");
            headers.add(head);
            //构造数据
            columnList = new ArrayList<>();
            //呼叫量
            columnList.add(String.valueOf(callSum));
            //接通量
            columnList.add(String.valueOf(calledSum));
            //未通量
            columnList.add(String.valueOf(failSum));
            //成功量
            columnList.add(String.valueOf(successSum));
            //接通率
            if (NumberConvertUtil.parseLong(callSum) == 0) {
                columnList.add(String.valueOf(0));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(calledSum, callSum)));
            }
            //成功率
            if (NumberConvertUtil.parseLong(calledSum) == 0) {
                columnList.add(String.valueOf(0));
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(successSum, calledSum)));
            }

            data.add(columnList);

            Sheet sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("外呼数据统计");
            writer.write0(data, sheet);

            // 构造未接通号码统计
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("未通总量");
            headers.add(head);

            head = new ArrayList<>();
            head.add("用户忙");
            headers.add(head);

            head = new ArrayList<>();
            head.add("不在服务区");
            headers.add(head);

            head = new ArrayList<>();
            head.add("停机");
            headers.add(head);

            head = new ArrayList<>();
            head.add("关机");
            headers.add(head);

            head = new ArrayList<>();
            head.add("空号");
            headers.add(head);

            head = new ArrayList<>();
            head.add("其他");
            headers.add(head);
            //构造数量数据
            columnList = new ArrayList<>();
            columnList.add(String.valueOf(failSum));
            columnList.add(String.valueOf(busySum));
            columnList.add(String.valueOf(noServiceSum));
            columnList.add(String.valueOf(phoneOverdueSum));
            columnList.add(String.valueOf(phoneShutdownSum));
            columnList.add(String.valueOf(spacePhoneSum));
            columnList.add(String.valueOf(otherSum));
            data.add(columnList);

            //构造占比数据
            columnList = new ArrayList<>();
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(failSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(busySum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(noServiceSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneOverdueSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneShutdownSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(spacePhoneSum, callSum)) : "0");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(otherSum, callSum)) : "0");
            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("未接通号码统计");
            writer.write0(data, sheet);

            //　构造用户呼叫列表数据
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("员工");
            headers.add(head);

            head = new ArrayList<>();
            head.add("接通数");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功数");
            headers.add(head);

            head = new ArrayList<>();
            head.add("成功率");
            headers.add(head);

            head = new ArrayList<>();
            head.add("总通话时长");
            headers.add(head);

            head = new ArrayList<>();
            head.add("平均通话时长");
            headers.add(head);

            // 查询用户呼叫数
            sqlSb = new StringBuffer();
            sqlSb.append(" SELECT customer_group_id, user_id, IFNULL(SUM(caller_sum),0) caller_sum,IFNULL(SUM(called_sum),0) called_sum, IFNULL(SUM(order_sum),0) order_sum, " +
                    "IFNULL(SUM(called_duration),0) called_duration FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ? AND market_task_id = ?");
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            sqlSb.append(" GROUP BY user_id ");
            List<Map<String, Object>> list = this.marketTaskDao.sqlQuery(sqlSb.toString(), startTime, endTime, marketTask.getCustomerGroupId(), marketTaskId);

            Map<String, Object> m;
            for (int i = 0; i < list.size(); i++) {
                m = list.get(i);
                m.put("userName", customerUserDao.getLoginName(String.valueOf(m.get("user_id"))));
                columnList = new ArrayList<>();
                columnList.add(String.valueOf(m.get("userName")));
                columnList.add(String.valueOf(m.get("called_sum")));
                columnList.add(String.valueOf(m.get("order_sum")));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add(String.valueOf(0));
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(m.get("order_sum")), NumberConvertUtil.parseLong(m.get("called_sum")))));
                }
                columnList.add(String.valueOf(m.get("called_duration")));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add("0");
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.divNumber(NumberConvertUtil.parseLong(m.get("called_duration")), NumberConvertUtil.parseLong(m.get("called_sum")))));
                }
                data.add(columnList);

            }
            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("员工统计");
            writer.write0(data, sheet);

            // 处理自建属性标记数据
            Map<Object, Object> singleLabel = customerLabelService.getCustomAndSystemLabel(userQueryParam.getCustId());

            StringBuilder superSql = new StringBuilder();
            superSql.append("SELECT label_id, GROUP_CONCAT(option_value) option_value, GROUP_CONCAT(tag_sum) tag_sum, IFNULL(SUM(tag_sum),0) sum FROM stat_u_label_data WHERE market_task_id =? AND stat_time BETWEEN ? AND ?  ");
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                superSql.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            superSql.append(" GROUP BY label_id");
            List<Map<String, Object>> labelList = marketTaskDao.sqlQuery(superSql.toString(), marketTaskId, startTime, endTime);
            String optionValue, tagSum, percent;
            long sum;
            String[] options, tags;
            Map<String, Integer> optionValueMap;
            Set<String> labelSheetName = new HashSet<>();
            for (Map<String, Object> map : labelList) {
                optionValueMap = new HashMap<>();
                data = new ArrayList<>();
                headers = new ArrayList<>();

                head = new ArrayList<>();
                head.add(String.valueOf(singleLabel.get(map.get("label_id"))));
                headers.add(head);

                head = new ArrayList<>();
                head.add("数量");
                headers.add(head);

                head = new ArrayList<>();
                head.add("占比");
                headers.add(head);

                optionValue = String.valueOf(map.get("option_value"));
                tagSum = String.valueOf(map.get("tag_sum"));
                // 单个自建属性标记总数
                sum = NumberConvertUtil.parseLong(map.get("sum"));
                if (StringUtil.isNotEmpty(optionValue) && StringUtil.isNotEmpty(tagSum)) {
                    options = optionValue.split(",");
                    tags = tagSum.split(",");
                    for (int i = 0; i < options.length; i++) {
                        if (optionValueMap.get(options[i]) != null) {
                            optionValueMap.put(options[i], optionValueMap.get(options[i]) + NumberConvertUtil.parseInt(tags[i]));
                        } else {
                            optionValueMap.put(options[i], NumberConvertUtil.parseInt(tags[i]));
                        }
                    }
                    for (Map.Entry<String, Integer> v : optionValueMap.entrySet()) {
                        percent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(v.getValue()), sum);
                        columnList = new ArrayList<>();
                        columnList.add(v.getKey());
                        columnList.add(String.valueOf(v.getValue()));
                        columnList.add(percent);
                        data.add(columnList);
                    }
                }
                sheet = new Sheet(sheetNum, 0);
                sheetNum++;
                sheet.setHead(headers);
                // Sheet已经存在
                if (labelSheetName.contains(String.valueOf(singleLabel.get(map.get("label_id"))))) {
                    sheet.setSheetName(String.valueOf(singleLabel.get(map.get("label_id"))) + map.get("label_id"));
                } else {
                    sheet.setSheetName(String.valueOf(singleLabel.get(map.get("label_id"))));
                }
                labelSheetName.add(String.valueOf(singleLabel.get(map.get("label_id"))));
                writer.write0(data, sheet);
            }
            // 通话时长分布统计
            data = new ArrayList<>();
            headers = new ArrayList<>();

            head = new ArrayList<>();
            head.add("1-3秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("4-6秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("7-12秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("13-30秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("31-60秒");
            headers.add(head);

            head = new ArrayList<>();
            head.add("60秒以上");
            headers.add(head);

            //构造数据
            columnList = new ArrayList<>();
            columnList.add(String.valueOf(durationType1));
            columnList.add(String.valueOf(durationType2));
            columnList.add(String.valueOf(durationType3));
            columnList.add(String.valueOf(durationType4));
            columnList.add(String.valueOf(durationType5));
            columnList.add(String.valueOf(durationType6));
            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("通话时长分布统计");
            writer.write0(data, sheet);

            writer.finish();
        } catch (Exception e) {
            LOG.error("导出营销任务:" + marketTaskId + "统计分析异常,", e);
        }
    }

    public void exportMarketTaskCallData0(int timeType, String marketTaskId, UserQueryParam userQueryParam, String startTime, String endTime, HttpServletResponse response, String workPlaceId) {
        try (OutputStream outputStream = response.getOutputStream()) {
            if (StringUtil.isEmpty(marketTaskId)) {
                LOG.warn("marketTaskId参数异常");
                return;
            }
            int taskType = -1;
            MarketTask marketTask = marketTaskDao.get(marketTaskId);
            if (marketTask == null) {
                LOG.warn("未查询到指定营销任务:" + marketTaskId);
                return;
            }
            userQueryParam.setCustId(marketTask.getCustId());
            if (marketTask.getTaskType() != null) {
                taskType = marketTask.getTaskType();
            }
            String cgName = "", projectName = "", cgId = "", projectId = "";
            CustomGroup cg = customGroupDao.get(marketTask.getCustomerGroupId());
            if (cg != null) {
                cgId = String.valueOf(cg.getId());
                cgName = cg.getName();
                MarketProject project = marketProjectDao.get(cg.getMarketProjectId());
                if (project != null) {
                    projectName = project.getName();
                    projectId = String.valueOf(project.getId());
                }
            }
            LocalDateTime localStartDateTime, localEndDateTime;
            // 按天查询
            if (1 == timeType) {
                // 处理时间
                if (StringUtil.isNotEmpty(startTime) && StringUtil.isNotEmpty(endTime)) {
                    localStartDateTime = LocalDateTime.parse(startTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    localEndDateTime = LocalDateTime.parse(endTime, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } else {
                    LOG.warn("查询营销任务统计分析时间参数错误,startTime:" + startTime + ",endTime:" + endTime);
                    return;
                }
            } else if (2 == timeType) {
                // 查询全部统计分析
                if (marketTask.getTaskCreateTime() != null && marketTask.getTaskEndTime() != null) {
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getTaskCreateTime().getTime());
                    localEndDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getTaskEndTime().getTime());
                } else if (marketTask.getCreateTime() != null) {
                    localEndDateTime = LocalDateTime.now();
                    localStartDateTime = DateUtil.getDateTimeOfTimestamp(marketTask.getCreateTime().getTime());
                    LOG.warn("营销任务创建和结束为空默认取营销任务的创建时间:" + marketTask.getCreateTime() + ",nowTime:" + localEndDateTime);
                } else {
                    LOG.warn("查询营销任务统计分析时间营销任务开始和结束时间异常,taskCreateTime:" + marketTask.getTaskCreateTime() + ",taskEndTime:" + marketTask.getTaskEndTime());
                    return;
                }
            } else {
                // 查询当前时间1天的统计分析
                localStartDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MIN);
                localEndDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.MAX);
            }
            // 处理查询开始和结束时间
            startTime = localStartDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 00:00:00"));
            endTime = localEndDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

            // 计算呼叫总数
            StringBuffer sqlSb = new StringBuffer();
            sqlSb.append("SELECT IFNULL(SUM(caller_sum),0) caller_sum, IFNULL(SUM(called_sum),0) called_sum, " +
                    " IFNULL(SUM(busy_sum),0) busy_sum, IFNULL(SUM(no_service_area_sum),0) no_service_area_sum, IFNULL(SUM(phone_overdue_sum),0) phone_overdue_sum, " +
                    " IFNULL(SUM(phone_shutdown_sum),0) phone_shutdown_sum, IFNULL(SUM(space_phone_sum),0) space_phone_sum, IFNULL(SUM(other_sum),0) other_sum, " +
                    " IFNULL(SUM(order_sum),0) order_sum, IFNULL(SUM(called_duration_type1),0) durationType1, IFNULL(SUM(called_duration_type2),0) durationType2, IFNULL(SUM(called_duration_type3),0) durationType3, IFNULL(SUM(called_duration_type4),0) durationType4, " +
                    " IFNULL(SUM(called_duration_type5),0) durationType5, IFNULL(SUM(called_duration_type6),0) durationType6 FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ? AND market_task_id = ?");

            List<Map<String, Object>> statList;
            // 通话记录查询用户权限
            Set voiceUserIds = new HashSet();
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                            voiceUserIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                        voiceUserIds.add(userQueryParam.getUserId());
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                    voiceUserIds.add(userQueryParam.getUserId());
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            statList = this.marketTaskDao.sqlQuery(sqlSb.toString(), startTime, endTime, marketTask.getCustomerGroupId(), marketTaskId);
            // 呼叫量,接通量,未通量, 成单量
            long callSum = 0L, calledSum = 0L, failSum = 0L, successSum = 0L, busySum = 0L,
                    noServiceSum = 0L, phoneOverdueSum = 0L, phoneShutdownSum = 0L, spacePhoneSum = 0L, otherSum = 0L, durationType1 = 0L, durationType2 = 0L, durationType3 = 0L, durationType4 = 0L, durationType5 = 0L, durationType6 = 0L;
            if (statList.size() > 0) {
                callSum = NumberConvertUtil.parseLong(statList.get(0).get("caller_sum"));
                calledSum = NumberConvertUtil.parseLong(statList.get(0).get("called_sum"));
                busySum = NumberConvertUtil.parseLong(statList.get(0).get("busy_sum"));
                noServiceSum = NumberConvertUtil.parseLong(statList.get(0).get("no_service_area_sum"));
                phoneOverdueSum = NumberConvertUtil.parseLong(statList.get(0).get("phone_overdue_sum"));
                phoneShutdownSum = NumberConvertUtil.parseLong(statList.get(0).get("phone_shutdown_sum"));
                spacePhoneSum = NumberConvertUtil.parseLong(statList.get(0).get("space_phone_sum"));
                otherSum = NumberConvertUtil.parseLong(statList.get(0).get("other_sum"));
                failSum = busySum + noServiceSum + phoneOverdueSum + phoneShutdownSum + spacePhoneSum + otherSum;
                successSum = NumberConvertUtil.parseLong(statList.get(0).get("order_sum"));
                // 通话时长范围统计
                durationType1 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType1", 0));
                durationType2 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType2", 0));
                durationType3 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType3", 0));
                durationType4 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType4", 0));
                durationType5 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType5", 0));
                durationType6 = NumberConvertUtil.parseLong(statList.get(0).getOrDefault("durationType6", 0));
            }

            Map<String, Integer> sheetMergeIndex = new HashMap<>();
            Map<String, String> sheetMergeName = new HashMap<>();

            String fileName = "营销任务统计数据-" + marketTaskId + "-" + System.currentTimeMillis();
            final String fileType = ".xlsx";
            response.setHeader("Content-Disposition", "attachment; filename=" + new String((fileName).getBytes("gb2312"), "ISO-8859-1") + fileType);
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            ExcelWriter writer = new ExcelWriter(null, outputStream, ExcelTypeEnum.XLSX, true, new ExcelAfterWriteHandlerImpl(sheetMergeIndex, sheetMergeName));
            int sheetNum = 1;
            List<List<String>> data, headers;
            List<String> columnList, head;

            data = new ArrayList<>();

            headers = new ArrayList<>();
            String[] heads = new String[]{"项目名称", "时间", "任务ID", "客户群ID", "呼叫量", "接通量", "未通量", "成功量", "接通率", "成功率"};
            for (String h : heads) {
                head = new ArrayList<>();
                head.add(h);
                headers.add(head);
            }
            sheetMergeIndex.put("外呼数据统计", 3);
            sheetMergeName.put("外呼数据统计", "汇总");
            //构造数据
            columnList = new ArrayList<>();
            columnList.add(projectName);
            columnList.add(startTime + "-" + endTime);
            columnList.add(marketTaskId);
            columnList.add(cgId);

            //呼叫量
            columnList.add(String.valueOf(callSum));
            //接通量
            columnList.add(String.valueOf(calledSum));
            //未通量
            columnList.add(String.valueOf(failSum));
            //成功量
            columnList.add(String.valueOf(successSum));
            //接通率
            if (NumberConvertUtil.parseLong(callSum) == 0) {
                columnList.add(String.valueOf(0) + "%");
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(calledSum, callSum)) + "%");
            }
            //成功率
            if (NumberConvertUtil.parseLong(calledSum) == 0) {
                columnList.add(String.valueOf(0) + "%");
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(successSum, calledSum)) + "%");
            }
            data.add(columnList);

            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add("");
            columnList.add("");
            columnList.add("");
            //呼叫量
            columnList.add(String.valueOf(callSum));
            //接通量
            columnList.add(String.valueOf(calledSum));
            //未通量
            columnList.add(String.valueOf(failSum));
            //成功量
            columnList.add(String.valueOf(successSum));
            //接通率
            if (NumberConvertUtil.parseLong(callSum) == 0) {
                columnList.add(String.valueOf(0) + "%");
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(calledSum, callSum)) + "%");
            }
            //成功率
            if (NumberConvertUtil.parseLong(calledSum) == 0) {
                columnList.add(String.valueOf(0) + "%");
            } else {
                columnList.add(String.valueOf(NumberConvertUtil.getPercent(successSum, calledSum)) + "%");
            }
            data.add(columnList);

            Sheet sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("外呼数据统计");
            writer.write0(data, sheet);

            // 构造未接通号码统计
            data = new ArrayList<>();
            headers = new ArrayList<>();
            heads = new String[]{"任务ID", "客户群ID", "未通总量", "用户忙", "不在服务区", "停机", "关机", "空号", "其他"};
            for (String h : heads) {
                head = new ArrayList<>();
                head.add(h);
                headers.add(head);
            }
            //构造未通数据
            columnList = new ArrayList<>();
            columnList.add(marketTaskId);
            columnList.add(projectId);
            columnList.add(String.valueOf(failSum));
            columnList.add(String.valueOf(busySum));
            columnList.add(String.valueOf(noServiceSum));
            columnList.add(String.valueOf(phoneOverdueSum));
            columnList.add(String.valueOf(phoneShutdownSum));
            columnList.add(String.valueOf(spacePhoneSum));
            columnList.add(String.valueOf(otherSum));
            data.add(columnList);

            //构造未通占比数据
            columnList = new ArrayList<>();
            columnList.add(marketTaskId);
            columnList.add(projectId);
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(failSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(busySum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(noServiceSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneOverdueSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneShutdownSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(spacePhoneSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(otherSum, callSum)) + "%" : "0%");
            data.add(columnList);

            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add("");
            columnList.add(String.valueOf(failSum));
            columnList.add(String.valueOf(busySum));
            columnList.add(String.valueOf(noServiceSum));
            columnList.add(String.valueOf(phoneOverdueSum));
            columnList.add(String.valueOf(phoneShutdownSum));
            columnList.add(String.valueOf(spacePhoneSum));
            columnList.add(String.valueOf(otherSum));
            data.add(columnList);
            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add("");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(failSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(busySum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(noServiceSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneOverdueSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(phoneShutdownSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(spacePhoneSum, callSum)) + "%" : "0%");
            columnList.add(NumberConvertUtil.parseLong(callSum) > 0 ? String.valueOf(NumberConvertUtil.getPercent(otherSum, callSum)) + "%" : "0%");
            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("未接通号码统计");
            sheetMergeIndex.put("未接通号码统计", 1);
            sheetMergeName.put("未接通号码统计", "汇总");
            writer.write0(data, sheet);

            //　构造用户呼叫列表数据
            data = new ArrayList<>();
            headers = new ArrayList<>();
            heads = new String[]{"任务ID", "客户群ID", "员工", "接通数", "成功数", "成功率", "总通话时长", "平均通话时长"};
            for (String h : heads) {
                head = new ArrayList<>();
                head.add(h);
                headers.add(head);
            }
            // 查询用户呼叫数
            sqlSb = new StringBuffer();
            sqlSb.append(" SELECT customer_group_id, user_id, IFNULL(SUM(caller_sum),0) caller_sum,IFNULL(SUM(called_sum),0) called_sum, IFNULL(SUM(order_sum),0) order_sum, " +
                    "IFNULL(SUM(called_duration),0) called_duration FROM stat_c_g_u_d WHERE stat_time BETWEEN ? AND ? AND customer_group_id = ? AND market_task_id = ?");
            //管理员查全部
            if ("2".equals(userQueryParam.getUserType())) {
                // 组长查整个组的外呼统计
                if ("1".equals(userQueryParam.getUserGroupRole())) {
                    List<CustomerUserDTO> customerUserDTOList = customerUserDao.listSelectCustomerUserByUserGroupId(userQueryParam.getUserGroupId(), userQueryParam.getCustId());
                    Set<String> userIds = new HashSet<>();
                    if (customerUserDTOList.size() > 0) {
                        for (CustomerUserDTO customerUserDTO : customerUserDTOList) {
                            userIds.add(customerUserDTO.getId());
                        }
                        // 分配责任人操作
                        if (userIds.size() > 0) {
                            if (3 == taskType) {
                                sqlSb.append(" AND (user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            } else {
                                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
                            }
                        }
                    } else {
                        // 处理组长下没有员工的情况,只查询自己的通话记录
                        if (3 == taskType) {
                            sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                        } else {
                            sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                        }
                    }
                } else {
                    if (3 == taskType) {
                        sqlSb.append(" AND (user_id = '" + userQueryParam.getUserId() + "')");
                    } else {
                        sqlSb.append(" AND user_id = '" + userQueryParam.getUserId() + "'");
                    }
                }
            }
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                sqlSb.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            sqlSb.append(" GROUP BY user_id ");
            List<Map<String, Object>> list = this.marketTaskDao.sqlQuery(sqlSb.toString(), startTime, endTime, marketTask.getCustomerGroupId(), marketTaskId);

            Map<String, Object> m;
            long userCalledSum = 0L, userOrderSum = 0L, userCallDuration = 0L;
            for (int i = 0; i < list.size(); i++) {
                m = list.get(i);
                m.put("userName", customerUserDao.getLoginName(String.valueOf(m.get("user_id"))));
                columnList = new ArrayList<>();
                columnList.add(marketTaskId);
                columnList.add(projectId);
                columnList.add(String.valueOf(m.get("userName")));
                userCalledSum += NumberConvertUtil.parseLong(m.get("called_sum"));
                columnList.add(String.valueOf(m.get("called_sum")));
                userOrderSum += NumberConvertUtil.parseLong(m.get("order_sum"));
                columnList.add(String.valueOf(m.get("order_sum")));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add(String.valueOf(0) + "%");
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(m.get("order_sum")), NumberConvertUtil.parseLong(m.get("called_sum")))) + "%");
                }
                columnList.add(String.valueOf(m.get("called_duration")));
                userCallDuration += NumberConvertUtil.parseLong(m.get("called_duration"));
                if (NumberConvertUtil.parseLong(m.get("called_sum")) == 0) {
                    columnList.add("0");
                } else {
                    columnList.add(String.valueOf(NumberConvertUtil.divNumber(NumberConvertUtil.parseLong(m.get("called_duration")), NumberConvertUtil.parseLong(m.get("called_sum")))));
                }
                data.add(columnList);
            }
            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add("");
            columnList.add("");
            columnList.add(String.valueOf(userCalledSum));
            columnList.add(String.valueOf(userOrderSum));
            columnList.add(String.valueOf(userOrderSum > 0 ? NumberConvertUtil.getPercent(userOrderSum, userCalledSum) + "%" : "0%"));
            columnList.add(String.valueOf(userCallDuration));
            columnList.add(String.valueOf(userCallDuration > 0 ? NumberConvertUtil.getPercent(userCallDuration, userCalledSum) + "%" : "0%"));
            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("员工统计");
            sheetMergeIndex.put("员工统计", 1);
            sheetMergeName.put("员工统计", "汇总");
            writer.write0(data, sheet);

            // 处理自建属性标记数据
            Map<Object, Object> singleLabel = customerLabelService.getCustomAndSystemLabel(userQueryParam.getCustId());

            StringBuilder superSql = new StringBuilder();
            superSql.append("SELECT label_id, GROUP_CONCAT(option_value) option_value, GROUP_CONCAT(tag_sum) tag_sum, IFNULL(SUM(tag_sum),0) sum FROM stat_u_label_data WHERE market_task_id =? AND stat_time BETWEEN ? AND ?  ");
            // 处理根据职场检索条件
            if (StringUtil.isNotEmpty(workPlaceId)) {
                List<String> userIds = customerUserDao.listUserIdByWorkPlaceId(workPlaceId);
                if (userIds == null || userIds.size() == 0) {
                    userIds = new ArrayList<>();
                    userIds.add("-1");
                }
                superSql.append(" AND user_id IN(" + SqlAppendUtil.sqlAppendWhereIn(userIds) + ") ");
            }
            superSql.append(" GROUP BY label_id");
            List<Map<String, Object>> labelList = marketTaskDao.sqlQuery(superSql.toString(), marketTaskId, startTime, endTime);
            String optionValue, tagSum, percent;
            long sum;
            String[] options, tags;
            Map<String, Integer> optionValueMap;
            Set<String> labelSheetName = new HashSet<>();
            for (Map<String, Object> map : labelList) {
                optionValueMap = new HashMap<>();
                data = new ArrayList<>();
                headers = new ArrayList<>();
                head = new ArrayList<>();
                head.add("任务ID");
                headers.add(head);

                head = new ArrayList<>();
                head.add("客户群ID");
                headers.add(head);

                head = new ArrayList<>();
                head.add(String.valueOf(singleLabel.get(String.valueOf(map.get("label_id")))));
                headers.add(head);

                head = new ArrayList<>();
                head.add("数量");
                headers.add(head);

                head = new ArrayList<>();
                head.add("占比");
                headers.add(head);

                optionValue = String.valueOf(map.get("option_value"));
                tagSum = String.valueOf(map.get("tag_sum"));
                // 单个自建属性标记总数
                sum = NumberConvertUtil.parseLong(map.get("sum"));
                if (StringUtil.isNotEmpty(optionValue) && StringUtil.isNotEmpty(tagSum)) {
                    options = optionValue.split(",");
                    tags = tagSum.split(",");
                    for (int i = 0; i < options.length; i++) {
                        if (optionValueMap.get(options[i]) != null) {
                            optionValueMap.put(options[i], optionValueMap.get(options[i]) + NumberConvertUtil.parseInt(tags[i]));
                        } else {
                            optionValueMap.put(options[i], NumberConvertUtil.parseInt(tags[i]));
                        }
                    }
                    for (Map.Entry<String, Integer> v : optionValueMap.entrySet()) {
                        percent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(v.getValue()), sum);
                        columnList = new ArrayList<>();
                        columnList.add(marketTaskId);
                        columnList.add(projectId);
                        columnList.add(v.getKey());
                        columnList.add(String.valueOf(v.getValue()));
                        columnList.add(percent + "%");
                        data.add(columnList);
                    }
                }
                for (Map.Entry<String, Integer> v : optionValueMap.entrySet()) {
                    percent = NumberConvertUtil.getPercent(NumberConvertUtil.parseLong(v.getValue()), sum);
                    columnList = new ArrayList<>();
                    columnList.add("汇总");
                    columnList.add("");
                    columnList.add(v.getKey());
                    columnList.add(String.valueOf(v.getValue()));
                    columnList.add(percent + "%");
                    data.add(columnList);
                }

                sheet = new Sheet(sheetNum, 0);
                sheetNum++;
                sheet.setHead(headers);
                // Sheet已经存在
                if (labelSheetName.contains(String.valueOf(singleLabel.get(map.get("label_id"))))) {
                    sheet.setSheetName(String.valueOf(singleLabel.get(map.get("label_id"))) + map.get("label_id"));
                } else {
                    sheet.setSheetName(String.valueOf(singleLabel.get(map.get("label_id"))));
                }
                sheetMergeIndex.put(sheet.getSheetName(), 1);
                sheetMergeName.put(sheet.getSheetName(), "汇总");
                labelSheetName.add(String.valueOf(singleLabel.get(map.get("label_id"))));
                writer.write0(data, sheet);
            }
            // 通话时长分布统计
            data = new ArrayList<>();
            headers = new ArrayList<>();
            heads = new String[]{"任务ID", "客户群ID", "1-3秒", "4-6秒", "7-12秒", "13-30秒", "31-60秒", "60秒以上"};
            for (String h : heads) {
                head = new ArrayList<>();
                head.add(h);
                headers.add(head);
            }

            //构造数据
            columnList = new ArrayList<>();
            columnList.add(marketTaskId);
            columnList.add(projectId);
            columnList.add(String.valueOf(durationType1));
            columnList.add(String.valueOf(durationType2));
            columnList.add(String.valueOf(durationType3));
            columnList.add(String.valueOf(durationType4));
            columnList.add(String.valueOf(durationType5));
            columnList.add(String.valueOf(durationType6));
            data.add(columnList);

            columnList = new ArrayList<>();
            columnList.add("汇总");
            columnList.add("");
            columnList.add(String.valueOf(durationType1));
            columnList.add(String.valueOf(durationType2));
            columnList.add(String.valueOf(durationType3));
            columnList.add(String.valueOf(durationType4));
            columnList.add(String.valueOf(durationType5));
            columnList.add(String.valueOf(durationType6));
            data.add(columnList);

            sheet = new Sheet(sheetNum, 0);
            sheetNum++;
            sheet.setHead(headers);
            sheet.setSheetName("通话时长分布统计");
            sheetMergeIndex.put("通话时长分布统计", 1);
            sheetMergeName.put("通话时长分布统计", "汇总");
            writer.write0(data, sheet);

            writer.finish();
        } catch (Exception e) {
            LOG.error("导出营销任务:" + marketTaskId + "统计分析异常,", e);
        }
    }

    /**
     * 导入营销数据详情表数据
     */
    class ImportDataThread implements Runnable {
        private MarketTaskDao marketTaskDao;
        String marketTaskId;
        int customGroupId;
        String groupCondition;
        String pageIndex;
        String pageSize;

        public ImportDataThread(MarketTaskDao marketTaskDao, String marketTaskId, int customGroupId, String groupCondition) {
            this.marketTaskDao = marketTaskDao;
            this.marketTaskId = marketTaskId;
            this.customGroupId = customGroupId;
            this.groupCondition = groupCondition;
        }

        public ImportDataThread(MarketTaskDao marketTaskDao, String marketTaskId, int customGroupId, String groupCondition, int pageIndex, int pageSize) {
            this.marketTaskDao = marketTaskDao;
            this.marketTaskId = marketTaskId;
            this.customGroupId = customGroupId;
            this.groupCondition = groupCondition;
            this.pageIndex = String.valueOf(pageIndex);
            this.pageSize = String.valueOf(pageSize);
        }

        @Override
        public void run() {
            execute();
        }

        private void execute() {
            // 创建营销任务详情表
            StringBuffer sql = new StringBuffer();
            sql.append(" create table IF NOT EXISTS ")
                    .append(ConstantsUtil.MARKET_TASK_TABLE_PREFIX)
                    .append(marketTaskId)
                    .append(" like t_customer_group_list");
            marketTaskDao.executeUpdateSQL(sql.toString());
            //导入数据
            sql = new StringBuffer();
            sql.append("INSERT INTO " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTaskId)
                    .append(" (id,status,remark,super_name,super_age,super_sex,super_telphone,super_phone,super_address_province_city,super_address_street,super_data,intent_level) ")
                    .append(" SELECT id, '1', remark,super_name,super_age,super_sex,super_telphone,super_phone,super_address_province_city,super_address_street,super_data,intent_level ")
                    .append(" FROM t_customer_group_list_" + customGroupId)
                    .append(" WHERE 1=1 ");
            if (StringUtil.isNotEmpty(groupCondition)) {
                JSONArray condition = JSON.parseArray(groupCondition);
                JSONArray leafs;
                JSONObject jsonObject;
                String labelId;
                String labelDataLikeValue = "%\"{0}\":%{1}%";
                for (int i = 0; i < condition.size(); i++) {
                    jsonObject = condition.getJSONObject(i);
                    leafs = jsonObject.getJSONArray("leafs");
                    if (leafs == null || leafs.size() == 0) {
                        continue;
                    }
                    labelId = jsonObject.getString("labelId");
                    if (jsonObject.getIntValue("type") == 1) {
                        //　呼叫次数
                        if (ConstantsUtil.CALL_COUNT_ID.equals(labelId)) {
                            sql.append(" AND ( ");
                            for (int j = 0; j < leafs.size(); j++) {
                                if (j > 0) {
                                    sql.append(" OR ");
                                }
                                if (StringUtil.isNotEmpty(leafs.getJSONObject(j).getString("value")) && leafs.getJSONObject(j).getString("value").indexOf("及以上") > 0) {
                                    sql.append(" call_count >= " + leafs.getJSONObject(j).getString("value").split("")[0]);
                                } else {
                                    sql.append(" call_count = " + leafs.getJSONObject(j).getString("value"));
                                }
                            }
                            sql.append(" ) ");
                        } else if (ConstantsUtil.CALL_SUCCESS_COUNT_ID.equals(labelId)) {
                            // 接通次数
                            sql.append(" AND ( ");
                            for (int j = 0; j < leafs.size(); j++) {
                                if (j > 0) {
                                    sql.append(" OR ");
                                }
                                if (StringUtil.isNotEmpty(leafs.getJSONObject(j).getString("value")) && leafs.getJSONObject(j).getString("value").indexOf("及以上") > 0) {
                                    sql.append(" call_success_count >=" + leafs.getJSONObject(j).getString("value").split("")[0]);
                                } else {
                                    sql.append(" call_success_count = " + leafs.getJSONObject(j).getString("value"));
                                }
                            }
                            sql.append(" ) ");
                        } else if (ConstantsUtil.SMS_COUNT_ID.equals(labelId)) {
                            // 短信次数
                            sql.append(" AND ( ");
                            for (int j = 0; j < leafs.size(); j++) {
                                if (j > 0) {
                                    sql.append(" OR ");
                                }
                                if (StringUtil.isNotEmpty(leafs.getJSONObject(j).getString("value")) && leafs.getJSONObject(j).getString("value").indexOf("及以上") > 0) {
                                    sql.append(" sms_success_count >=" + leafs.getJSONObject(j).getString("value").split("")[0]);
                                } else {
                                    sql.append(" sms_success_count = " + leafs.getJSONObject(j).getString("value"));
                                }
                            }
                            sql.append(" ) ");
                        }
                    }
                    if (jsonObject.getIntValue("type") == 2) {
                        sql.append(" AND ( ");
                        for (int j = 0; j < leafs.size(); j++) {
                            //　自建属性
                            if (j > 0) {
                                sql.append(" OR ");
                            }
                            sql.append(" super_data LIKE '" + MessageFormat.format(labelDataLikeValue, labelId, leafs.getJSONObject(j).getString("value")) + "'");
                        }
                        sql.append(" ) ");
                    }
                }
            }
            // 分页参数
            if (StringUtil.isNotEmpty(pageIndex) && StringUtil.isNotEmpty(pageSize)) {
                sql.append(" LIMIT ").append(pageIndex).append(",").append(pageSize);
            }
            marketTaskDao.executeUpdateSQL(sql.toString());
        }

    }

    /**
     * 根据新方任务ID查询营销任务数据手机号
     *
     * @param taskId
     * @param pageSize
     * @return
     */
    public XfPullPhoneDTO getMarketTaskPhonesToXf(String taskId, Long pageSize) {
        XfPullPhoneDTO result = new XfPullPhoneDTO();
        result.setContent("");
        MarketTask marketTask = marketTaskDao.getMarketTaskByTaskId(taskId);
        if (marketTask == null) {
            LOG.warn("新方自动外呼taskId:" + taskId + "未查询到对应的营销任务");
            result.setResult(1);
            return result;
        }
        int taskPhoneIndex = 0;
        if (marketTask.getTaskPhoneIndex() != null) {
            taskPhoneIndex = marketTask.getTaskPhoneIndex();
        } else {
            LOG.warn("新方自动外呼marketTaskId:" + marketTask.getId() + "的task_phone_index字段为空,设置为:" + taskPhoneIndex);
        }

        StringBuffer sb = new StringBuffer();
        sb.append(" select custG.id ");
        sb.append("  from " + ConstantsUtil.MARKET_TASK_TABLE_PREFIX + marketTask.getId() + " custG ");
        sb.append(" where 1=1 ");

        sb.append(" ORDER BY custG.n_id ASC ");
        if (pageSize != null && !"".equals(pageSize)) {
            sb.append("  LIMIT " + taskPhoneIndex + "," + pageSize);
        }
        List<Map<String, Object>> phones = null;
        StringBuffer content = new StringBuffer();
        try {
            phones = this.customGroupDao.sqlQuery(sb.toString());
            if (phones == null || phones.size() == 0) {
                LOG.info("phones=" + phones);
                result.setResult(2);
                return result;
            }
            taskPhoneIndex += phones.size();
            String u;
            for (Map<String, Object> phone : phones) {
                if (phone != null && phone.get("id") != null) {
                    LOG.info("phone: " + phone);
                    u = phoneService.getPhoneBySuperId(String.valueOf(phone.get("id")));
                    content.append(0)
                            .append(",")
                            .append(u)
                            .append(",")
                            .append(marketTask.getId() + "_" + marketTask.getCustId() + "_" + taskPhoneIndex + "_" + phone.get("id"))
                            .append("\r\n");
                    //保存营销任务和手机号对应的身份ID到redis
                    phoneService.setCGroupDataToRedis(String.valueOf(marketTask.getCustomerGroupId()), String.valueOf(phone.get("id")), u);
                }
            }
            marketTask.setTaskPhoneIndex(taskPhoneIndex);
            marketTaskDao.update(marketTask);
            LOG.info("新方自动外呼marketTaskId:" + marketTask.getId() + "更新号码最后index成功,index:" + taskPhoneIndex);
            result.setResult(0);
        } catch (Exception e) {
            result.setResult(3);
            LOG.error("新方自动外呼查询营销任务手机号失败,", e);
            // 异常后返回空数据,防止重复拉取
            content.setLength(0);
        }
        LOG.info("content===" + content.toString());
        result.setContent(content.toString());
        return result;
    }

    /**
     * 根据营销任务查询短信模板
     *
     * @param marketTaskId
     * @return
     */
    public MarketTemplateDTO getSmsEmailTemplate(String marketTaskId) {
        MarketTemplateDTO marketTemplateDTO = new MarketTemplateDTO();
        MarketTask marketTask = marketTaskDao.get(marketTaskId);
        if (marketTask != null) {
            MarketTemplate marketTemplate = marketResourceDao.getMarketTemplate(NumberConvertUtil.parseInt(marketTask.getSmsTemplateId()));
            if (marketTemplate != null) {
                marketTemplateDTO = new MarketTemplateDTO(marketTemplate);
            }
        }
        marketTemplateDTO.setCustName(customerDao.getEnterpriseName(marketTemplateDTO.getCustId()));
        return marketTemplateDTO;
    }

    /**
     * 获取营销任务属性
     *
     * @param marketTaskId
     * @param propertyName
     * @return
     */
    public MarketTaskProperty getProperty(String marketTaskId, String propertyName) {
        MarketTaskProperty property = marketTaskDao.getProperty(marketTaskId, propertyName);
        return property;
    }

    /**
     * 获取短信营销任务发送时间
     *
     * @param marketTaskId
     * @return
     */
    public String getSmsTime(String marketTaskId) {
        MarketTaskProperty property = getProperty(marketTaskId, "sendTime");
        if (property == null || StringUtil.isEmpty(property.getPropertyValue()) || "1".equals(property.getPropertyValue())) {
            MarketTask marketTask = marketTaskDao.get(marketTaskId);
            if (marketTask != null) {
                LocalDateTime dateTime = LocalDateTime.ofEpochSecond(marketTask.getCreateTime().getTime() / 1000, 0, ZoneOffset.ofHours(8));
                return dateTime.format(DatetimeUtils.DATE_TIME_FORMATTER);
            }
        } else {
            return property.getPropertyValue();
        }
        return "";
    }

    /**
     * 查询不包含成员的讯众自动外呼任务
     *
     * @param custId
     * @param userId
     * @param taskType
     * @return
     */
    private List<String> listNotIncludeXzTaskUser(String custId, String userId, int taskType) {
        List<String> taskIds = new ArrayList<>();
        String sql = "SELECT t2.property_value,t1.id FROM t_market_task t1 JOIN t_market_task_property t2 ON t1.id = t2.market_task_id WHERE t1.task_type = ? AND t2.property_name = 'callChannel' AND t1.cust_id = ?";
        List<Map<String, Object>> list = marketTaskDao.sqlQuery(sql, taskType, custId);
        MarketTaskUserRel taskUser;
        for (Map<String, Object> map : list) {
            if (map.get("property_value") == null) {
                continue;
            }
            ResourcePropertyEntity priceconfig = marketResourceDao.getProperty(String.valueOf(map.get("property_value")), "price_config");
            if (priceconfig == null) {
                continue;
            }
            JSONObject config = JSON.parseObject(priceconfig.getPropertyValue());
            if (config.containsKey("call_center_type") && "2".equals(config.getString("call_center_type"))) {
                // 查询是否为讯众自动任务成员
                taskUser = marketTaskDao.getMarketTaskUserRel(String.valueOf(map.get("id")), userId);
                if (taskUser == null) {
                    LOG.warn("[" + userId + "]非任务:[" + map.get("id") + "]成员");
                    taskIds.add(String.valueOf(map.get("id")));
                }
            }
        }
        return taskIds;
    }
}
