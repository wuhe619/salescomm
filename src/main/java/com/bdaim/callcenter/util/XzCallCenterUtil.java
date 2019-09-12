package com.bdaim.callcenter.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.callcenter.dto.*;
import com.bdaim.common.util.JavaBeanUtil;
import com.bdaim.common.util.SaleApiUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * 讯众呼叫中心API接口
 *
 * @author chengning@salescomm.net
 * @date 2019/4/22
 * @description
 */
public class XzCallCenterUtil {

    public static final Logger LOG = LoggerFactory.getLogger(XzCallCenterUtil.class);

    /**
     * 添加自动外呼任务URL
     */
    private final static String ADD_TASK_URL = "/api/CallOut/AddAutoTask";

    /**
     * 修改自动外呼任务URL
     */
    private final static String EDIT_TASK_URL = "/api/CallOut/EditAutoTask";

    /**
     * 查询自动外呼任务URL
     */
    private final static String QUERY_TASK_URL = "/api/CallOut/GetTaskByAutoID";

    /**
     * 添加自动外呼任务成员URL
     */
    private final static String ADD_TASK_MEMBER_URL = "/api/CallOut/AddTaskMember";

    /**
     * 删除自动外呼任务成员URL
     */
    private final static String REMOVE_TASK_MEMBER_URL = "/api/CallOut/RemoveTaskMember";

    /**
     * 获取自动外呼任务成员URL
     */
    private final static String GET_TASK_MEMBER_URL = "/api/CallOut/GetTaskMember";

    /**
     * 开启自动外呼任务
     */
    private final static String START_TASK_MEMBER_URL = "/api/CallOut/StartAutoTask";

    /**
     * 关闭自动外呼任务
     */
    private final static String STOP_TASK_MEMBER_URL = "/api/CallOut/StopAutoTask";

    /**
     * 查询单个企业信息
     */
    private final static String QUERY_SINGLE_COMPANY_URL = "/api/company/single";

    /**
     * 添加呼叫中心企业账号
     */
    private final static String ADD_COMPANY_URL = "/api/company/add";

    /**
     * 更新呼叫中心企业账号
     */
    private final static String UPDATE_COMPANY_URL = "/api/company/update";

    /**
     * 添加拉取号码URL
     */
    private final static String ADD_PHONE_URL = "/api/CallOut/AddPhoneUrl";

    /**
     * 查询企业最大并发数
     */
    private final static String QUERY_MAX_CALLOUT_NUMBER_URL = "/api/CallOut/GetMaxCallOutNumber";

    /**
     * 添加座席
     */
    private final static String ADD_AGENT_URL = "/api/agent/add";

    /**
     * 更新座席
     */
    private final static String MODIFY_AGENT_URL = "/api/agent/modifyagent";

    /**
     * 更新分机密码
     */
    private final static String MODIFY_EXTPWD_URL = "/api/agent/modifyextpwd";

    /**
     * 删除座席
     */
    private final static String DEL_AGENT_URL = "/api/agent/del";

    /**
     * 自动任务监控详情url
     */
    private final static String GET_AUTO_MONITOR_URL = "/api/CallOut/GetAutoMonitor";

    /**
     * 添加讯众自动外呼任务
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static Map<String, Object> addAutoTask(XzAddAutoTask param) throws Exception {
        LOG.info("添加讯众自动外呼任务请求参数:" + param);
        Map<String, Object> map = JavaBeanUtil.convertBeanToMap(param);
        String result = SaleApiUtil.callAgentCallOut(ADD_TASK_URL, map, SaleApiUtil.getCallCenterToken(param.getCompid()));
        LOG.info("添加讯众自动外呼任务返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 修改讯众自动外呼任务
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static Map<String, Object> editAutoTask(XzEditAutoTask param) throws Exception {
        LOG.info("修改讯众自动外呼任务请求参数:" + param);
        Map<String, Object> map = JavaBeanUtil.convertBeanToMap(param);
        String result = SaleApiUtil.callAgentCallOut(EDIT_TASK_URL, map, SaleApiUtil.getCallCenterToken(param.getCompid()));
        LOG.info("修改讯众自动外呼任务返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 查询讯众自动外呼任务
     *
     * @param callCenterId
     * @param taskId
     * @return
     * @throws Exception
     */
    public static Map<String, Object> queryAutoTask(String callCenterId, String taskId) throws Exception {
        Map<String, Object> param = new HashMap<>(16);
        param.put("taskid", taskId);
        param.put("compid", callCenterId);
        LOG.info("查询讯众自动外呼任务请求参数:" + param);
        String result = SaleApiUtil.callAgentCallOut(QUERY_TASK_URL, param, SaleApiUtil.getCallCenterToken(callCenterId));
        LOG.info("查询讯众自动外呼任务返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 讯众自动外呼任务添加成员
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static Map<String, Object> addTaskMembers(XzAddTaskMember param) throws Exception {
        LOG.info("讯众自动外呼任务添加成员请求参数:" + param);
        Map<String, Object> map = JavaBeanUtil.convertBeanToMap(param);
        String result = SaleApiUtil.callAgentCallOut(ADD_TASK_MEMBER_URL, map, SaleApiUtil.getCallCenterToken(param.getCompid()));
        LOG.info("讯众自动外呼任务添加成员返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 讯众自动外呼任务删除成员
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static Map<String, Object> removeTaskMembers(XzRemoveTaskMember param) throws Exception {
        LOG.info("讯众自动外呼任务删除成员任务请求参数:" + param);
        Map<String, Object> map = JavaBeanUtil.convertBeanToMap(param);
        String result = SaleApiUtil.callAgentCallOut(REMOVE_TASK_MEMBER_URL, map, SaleApiUtil.getCallCenterToken(param.getCompid()));
        LOG.info("讯众自动外呼任务删除成员返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 获取讯众自动外呼成员
     *
     * @param taskidentity
     * @param type
     * @param compid
     * @return
     * @throws Exception
     */
    public static JSONObject getTaskMembers(String taskidentity, int type, String compid) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("taskidentity", taskidentity);
        map.put("compid", compid);
        map.put("type", type);
        LOG.info("讯众获取自动外呼成员请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(GET_TASK_MEMBER_URL, map, SaleApiUtil.getCallCenterToken(compid));
        LOG.info("讯众获取自动外呼成员返回结果:" + result);
        return JSON.parseObject(result);
    }

    public static Map<String, Object> queryCompanyInfo(String callCenterId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("compid", callCenterId);
        LOG.info("讯众查询单个企业请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(QUERY_SINGLE_COMPANY_URL, map, SaleApiUtil.getCallCenterToken(callCenterId));
        LOG.info("讯众查询单个企业返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 讯众自动外呼添加拉取号码url
     *
     * @param callCenterId
     * @param userId
     * @param taskIdentity
     * @param url
     * @return
     * @throws Exception
     */
    public static String addPhoneUrl(String callCenterId, String userId, String taskIdentity, String url) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("compid", callCenterId);
        param.put("userid", userId);
        param.put("taskidentity", taskIdentity);
        param.put("phoneurl", url);
        LOG.info("讯众自动外呼任务添加拉取号码url请求参数:" + param);
        String result = SaleApiUtil.callAgentCallOut(ADD_PHONE_URL, param, SaleApiUtil.getCallCenterToken(callCenterId));
        LOG.info("讯众自动外呼任务添加拉取号码url返回结果:" + result);
        return result;
    }

    /**
     * 查询企业最大呼叫并发数
     *
     * @param callCenterId
     * @return
     * @throws Exception
     */
    public static String queryMaxCallOutNumber(String callCenterId) throws Exception {
        Map<String, Object> param = new HashMap<>();
        param.put("compid", callCenterId);
        LOG.info("讯众查询企业最大并发数请求参数:" + param);
        String result = SaleApiUtil.callAgentCallOut(QUERY_MAX_CALLOUT_NUMBER_URL, param, SaleApiUtil.getCallCenterToken(callCenterId));
        LOG.info("讯众查询企业最大并发数返回结果:" + result);
        return result;
    }

    /**
     * 添加企业账号信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static JSONObject addCompanytoXzCallCenter(XzCompanyCallcenterParam param) throws Exception {
        Map<String, Object> map = JavaBeanUtil.convertBeanToMap(param);
        ;
        LOG.info("讯众添加单个企业请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(ADD_COMPANY_URL, map, SaleApiUtil.getCallCenterToken(param.getCompid()));
        LOG.info("讯众添加单个企业返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 更新企业账号信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static JSONObject updateCompanytoXzCallCenter(XzCompanyCallcenterParam param) throws Exception {
        Map<String, Object> map = JavaBeanUtil.convertBeanToMap(param);
        LOG.info("讯众更新单个企业请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(UPDATE_COMPANY_URL, map, SaleApiUtil.getCallCenterToken(param.getCompid()));
        LOG.info("讯众更新单个企业返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 添加座席
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static JSONObject addAgent(XzCallcenterSeatParam param) throws Exception {
        param.setIstoafterstate(2);
        Map<String, Object> map = JavaBeanUtil.convertBeanToMap(param);
        LOG.info("讯众添加坐席请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(ADD_AGENT_URL, map, SaleApiUtil.getCallCenterToken(param.getCompid()));
        LOG.info("讯众添加坐席返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 修改座席信息
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static JSONObject modifyAgent(XzCallcenterSeatParam param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("compid", param.getCompid());
        map.put("agentid", param.getAgentid());
        map.put("agentrole", 2);
        map.put("pwd", param.getAgentpwd());
        map.put("shownumber", param.getShownumber());
        LOG.info("讯众更新坐席请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(MODIFY_AGENT_URL, map, SaleApiUtil.getCallCenterToken(param.getCompid()));
        LOG.info("讯众更新坐席返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 修改分机密码
     *
     * @param param
     * @return
     * @throws Exception
     */
    public static JSONObject modifyextAgent(XzCallcenterSeatParam param) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("compid", param.getCompid());
        map.put("agentid", param.getAgentid());
        map.put("pwd", param.getExtpwd());
        LOG.info("讯众更新坐席分机密码请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(MODIFY_EXTPWD_URL, map, SaleApiUtil.getCallCenterToken(param.getCompid()));
        LOG.info("讯众更新坐席分机密码返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 删除座席
     *
     * @param compid
     * @param agentid
     * @return
     * @throws Exception
     */
    public static JSONObject delAgent(String compid, String agentid) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("compid", compid);
        map.put("agentid", agentid);
        LOG.info("讯众删除坐席请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(DEL_AGENT_URL, map, SaleApiUtil.getCallCenterToken(compid));
        LOG.info("讯众删除坐席返回结果:" + result);
        return JSON.parseObject(result);
    }

    /**
     * 获取讯众自动外呼监控详情
     * @param compid
     * @param taskId
     * @return
     * @throws Exception
     */
    public static JSONObject getAutoMonitorData(String compid, String taskId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("compid", compid);
        map.put("taskidentity", taskId);
        LOG.info("讯众自动外呼监控详情请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(GET_AUTO_MONITOR_URL, map, SaleApiUtil.getCallCenterToken(compid));
        LOG.info("讯众自动外呼监控详情返回结果:" + result);
        return JSON.parseObject(result);
    }

    public static JSONObject startAutoTask(String compid, String taskId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("compid", compid);
        map.put("taskidentity", taskId);
        LOG.info("讯众开启自动外呼请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(START_TASK_MEMBER_URL, map, SaleApiUtil.getCallCenterToken(compid));
        LOG.info("讯众开启自动外呼返回结果:" + result);
        return JSON.parseObject(result);
    }

    public static JSONObject stopAutoTask(String compid, String taskId) throws Exception {
        Map<String, Object> map = new HashMap<>();
        map.put("compid", compid);
        map.put("taskidentity", taskId);
        LOG.info("讯众关闭自动外呼请求参数:" + map);
        String result = SaleApiUtil.callAgentCallOut(STOP_TASK_MEMBER_URL, map, SaleApiUtil.getCallCenterToken(compid));
        LOG.info("讯众关闭自动外呼返回结果:" + result);
        return JSON.parseObject(result);
    }


    public static void main(String[] args) throws Exception {
        JSONObject jsonObject = stopAutoTask("933333", "2019081220343134315");
        System.out.println(jsonObject);
    }

    public void testQueryCompanyInfo() throws Exception {
        System.out.println(queryCompanyInfo("933333"));

    }

    public void testAddAutoTask() throws Exception {
        XzAddAutoTask map = new XzAddAutoTask();
        map.setTaskname("huoke10005");
        map.setExpirdatebegin("2018-12-05 15:47:32");
        map.setExpirdateend("2019-12-05 15:47:32");
        map.setShownum("01053182579");
        map.setDailmodel("3");

        map.setCalloutspeed("1");
        map.setCounttype("1");
        map.setCallinterval("30");
        map.setRingingduration("30");

        map.setTimeruleid("");
        map.setWaitvoiceid("");
        map.setSeatallocationmodel("1");
        map.setMaxconcurrentnumber("10000");
        map.setIntelligence_num("");

        map.setCreator("admin");
        map.setCompid("933333");
        map.setNoticeurl("http://192.168.188.53:5500/api/TaskHandle");

        Map<String, Object> result = addAutoTask(map);
        System.out.println(result);
    }

    public void testEditAutoTask() throws Exception {
        XzEditAutoTask map = new XzEditAutoTask();
        map.setTaskname("huoke10005修改");
        map.setExpirdatebegin("2018-12-05 15:47:32");
        map.setExpirdateend("2019-12-05 15:47:32");
        map.setShownum("01053182579");
        map.setDailmodel("3");

        map.setCalloutspeed("1");
        map.setCounttype("1");
        map.setCallinterval("30");
        map.setRingingduration("30");

        map.setTimeruleid("");
        map.setWaitvoiceid("");
        map.setSeatallocationmodel("1");
        map.setMaxconcurrentnumber("10");
        map.setIntelligence_num("");

        map.setModifier("admin");
        map.setCompid("933333");
        map.setAutoid("12050");
        map.setNoticeurl("http://192.168.188.53:5500/api/TaskHandle");


        Map<String, Object> result = editAutoTask(map);
        System.out.println(result);
    }

    public void testAddTaskMember() throws Exception {
        XzAddTaskMember map = new XzAddTaskMember();
        map.setCreator("admin");
        map.setCompid("933333");
        map.setTaskidentity("2019042911414241424");
        map.setMembers("9999,9998,9996,9995,9994,9993");

        Map<String, Object> result = addTaskMembers(map);
        System.out.println(result);
    }

    public void testRemoveTaskMember() throws Exception {
        XzRemoveTaskMember map = new XzRemoveTaskMember();
        map.setCompid("933333");
        map.setTaskidentity("2019042911414241424");
        map.setMembers("9996,9995");

        Map<String, Object> result = removeTaskMembers(map);
        System.out.println(result);
    }


}
