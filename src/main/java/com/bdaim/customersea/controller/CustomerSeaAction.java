package com.bdaim.customersea.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.dto.XzAutoTaskMonitor;
import com.bdaim.callcenter.dto.XzPullPhoneDTO;
import com.bdaim.callcenter.service.impl.XzCallCenterService;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseCommon;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customersea.dto.CustomSeaTouchInfoDTO;
import com.bdaim.customersea.dto.CustomerSeaDTO;
import com.bdaim.customersea.dto.CustomerSeaParam;
import com.bdaim.customersea.dto.CustomerSeaSearch;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.log.dto.SuperDataOperLogQuery;
import com.bdaim.rbac.dto.RoleEnum;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.util.IDHelper;
import com.bdaim.util.NumberConvertUtil;
import com.bdaim.util.StringUtil;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019/6/22
 * @description
 */
@RestController
@RequestMapping("/customerSea")
public class CustomerSeaAction extends BasicAction {

    public static final Logger LOG = LoggerFactory.getLogger(CustomerSeaAction.class);

    @Resource
    private CustomerSeaService seaService;

    @Resource
    private MarketResourceService marketResourceService;

    @Resource
    private XzCallCenterService xzCallCenterService;

    /**
     * 公海基础信息分页查询
     *
     * @return
     */
    @RequestMapping(value = "/page/query", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseJson page(@RequestBody @Valid CustomerSeaParam param, BindingResult error) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasFieldErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        param.setCustId(opUser().getCustId());
        param.setUserId(opUser().getId());
        param.setUserType(opUser().getUserType());
        Page page = seaService.page(param, param.getPageNum(), param.getPageSize());
        responseJson.setData(getPageData(page));
        responseJson.setCode(200);
        return responseJson;
    }

    @RequestMapping(value = "/page/query1", method = RequestMethod.POST)
    public ResponseJson page2(@RequestBody @Valid CustomerSeaParam param, BindingResult error) {
        LOG.info("SDFSDAFLAJFDLLSAFJLAFL");
        ResponseJson responseJson = new ResponseJson();
        if (error.hasFieldErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        param.setCustId(opUser().getCustId());
        param.setUserId(opUser().getId());
        param.setUserType(opUser().getUserType());
        Page page = seaService.page(param, param.getPageNum(), param.getPageSize());
        responseJson.setData(getPageData(page));
        responseJson.setCode(200);
        return responseJson;
    }


    /**
     * 保存公海信息
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseJson save(@RequestBody CustomerSeaParam param) {
        param.setCustId(opUser().getCustId());
        param.setCreateUid(opUser().getId());
        param.setUpdateUid(opUser().getId());
        ResponseJson responseJson = new ResponseJson();
        int code = 0;
        try {
            code = seaService.save(param);
            responseJson.setData(code);
            responseJson.setCode(200);
        } catch (Exception e) {
            LOG.error("保存公海异常,", e);
            responseJson.setData(e.getMessage());
            responseJson.setCode(-1);
        }
        return responseJson;
    }

    /**
     * 客群数据导入公海
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/importData", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseJson importCGroupDataToCustomerSea(@RequestBody JSONObject param) {
        String seaId = param.getString("seaId");
        Integer customerGroupId = param.getInteger("customerGroupId");
        // 线索来源 1-购买 2-导入 3-添加 4-回收
        Integer dataSource = param.getInteger("dataSource");
        ResponseJson responseJson = new ResponseJson();
        if (StringUtil.isEmpty(seaId) || customerGroupId == null) {
            responseJson.setMessage("参数错误");
            responseJson.setCode(-1);
            return responseJson;
        }
        int count = 0;
        try {
            count = seaService.importCGroupDataToCustomerSea(opUser().getCustId(), customerGroupId, seaId, dataSource);
            responseJson.setData(count);
            responseJson.setCode(200);
        } catch (TouchException e) {
            LOG.error("导入公海数据异常,", e);
            responseJson.setData(e.getErrMsg());
            responseJson.setCode(-1);
        }
        return responseJson;
    }

    /**
     * 公海内线索分页
     *
     * @param param
     * @param error
     * @return
     */
    @RequestMapping(value = "/pageClue", method = RequestMethod.POST)
    @CacheAnnotation
    public ResponseJson pageClue(@RequestBody @Valid CustomerSeaSearch param, BindingResult error) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasFieldErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        Page data = null;
        try {
            param.setUserId(opUser().getId());
            param.setUserType(opUser().getUserType());
            param.setUserGroupRole(opUser().getUserGroupRole());
            param.setUserGroupId(opUser().getUserGroupId());
            param.setCustId(opUser().getCustId());
            // 查询公海线索
            if (param.getSeaType() == null || param.getSeaType() == 1) {
                data = seaService.pagePublicClueData(param);
            } else if (param.getSeaType() == 2) {
                // 查询私海线索
                data = seaService.pagePrivateClueData(param);
            }
            responseJson.setCode(200);
        } catch (Exception e) {
            responseJson.setCode(-1);
            LOG.error("查询公海详情列表异常,", e);
        }
        if (data == null) {
            data = new Page();
        }
        responseJson.setData(getPageData(data));
        return responseJson;
    }

    /**
     * 公海线索状态修改
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/updateClueStatus", method = RequestMethod.POST)
    public ResponseJson updateClueStatus(@RequestBody JSONObject jsonObject) {
        ResponseJson responseJson = new ResponseJson();
        CustomerSeaSearch param = JSON.parseObject(jsonObject.toJSONString(), CustomerSeaSearch.class);
        if (StringUtil.isEmpty(param.getSeaId())) {
            responseJson.setData("参数异常");
            responseJson.setCode(-1);
            return responseJson;
        }
        int operate = jsonObject.getIntValue("operate");
        int data = 0;
        try {
            param.setUserId(opUser().getId());
            param.setUserType(opUser().getUserType());
            param.setUserGroupRole(opUser().getUserGroupRole());
            param.setUserGroupId(opUser().getUserGroupId());
            param.setCustId(opUser().getCustId());
            data = seaService.updateClueStatus(param, operate);
            responseJson.setCode(200);
        } catch (Exception e) {
            LOG.error("公海线索状态修改异常,", e);
            responseJson.setCode(-1);
        }
        responseJson.setData(data);
        return responseJson;
    }

    /**
     * 线索分配
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/distributionClue", method = RequestMethod.POST)
    public ResponseJson distributionClue(@RequestBody JSONObject jsonObject) {
        ResponseJson responseJson = new ResponseJson();
        Integer operate = jsonObject.getInteger("operate");
        if (operate == null) {
            responseJson.setData("operate参数必填");
            responseJson.setCode(-1);
            return responseJson;
        }
        CustomerSeaSearch param = JSON.parseObject(jsonObject.toJSONString(), CustomerSeaSearch.class);
        if (StringUtil.isEmpty(param.getSeaId())) {
            responseJson.setData("seaId参数必填");
            responseJson.setCode(-1);
        }
        if (param.getUserIds() == null || param.getUserIds().size() == 0) {
            responseJson.setData("userIds参数必填");
            responseJson.setCode(-1);
        }
        // 员工和组长领取线索处理
        if ("2".equals(opUser().getUserType())) {
            List<String> userIds = new ArrayList<>();
            userIds.add(String.valueOf(opUser().getId()));
            param.setUserIds(userIds);
        }
        // 快速分配时用户和数量数组
        JSONArray assignedList = jsonObject.getJSONArray("assignedlist");
        int data = 0;
        try {
            param.setUserId(opUser().getId());
            param.setUserType(opUser().getUserType());
            param.setUserGroupRole(opUser().getUserGroupRole());
            param.setUserGroupId(opUser().getUserGroupId());
            param.setCustId(opUser().getCustId());
            // 同步操作
            synchronized (this) {
                data = seaService.distributionClue(param, operate, assignedList);
            }
            responseJson.setCode(200);
        } catch (TouchException e) {
            responseJson.setCode(-1);
            responseJson.setMessage(e.getErrMsg());
            LOG.error("线索分配异常,", e);
        }
        responseJson.setData(data);
        return responseJson;
    }

    /**
     * 查询公海下坐席可领取线索量
     *
     * @param param
     * @return
     */
    @RequestMapping(value = "/selectUserGetQuantity", method = RequestMethod.POST)
    @CacheAnnotation
    public ResponseJson selectUserGetQuantity(@RequestBody CustomerSeaSearch param) {
        ResponseJson responseJson = new ResponseJson();
        long data = 0;
        try {
            param.setUserId(opUser().getId());
            data = seaService.getUserReceivableQuantity(param.getSeaId(), String.valueOf(opUser().getId()));
            responseJson.setCode(200);
        } catch (Exception e) {
            responseJson.setCode(0);
            responseJson.setMessage(e.getMessage());
            LOG.error("查询公海下坐席可领取线索量异常,", e);
        }
        responseJson.setData(data);
        return responseJson;
    }

    @RequestMapping(value = "/updateClueSignData", method = RequestMethod.POST)
    public ResponseCommon updateClueSignData(@RequestBody JSONObject jsonO) {
        ResponseCommon responseJson = new ResponseCommon();
        String customerId = opUser().getCustId();
        Long userId = opUser().getId();
        String remark = jsonO.getString("remark");
        String superId = jsonO.getString("superId");
        String touchId = jsonO.getString("touchId");
        String seaId = jsonO.getString("seaId");
        try {
            // 更新通话记录表的备注
            if (StringUtil.isNotEmpty(touchId)) {
                marketResourceService.updateVoiceLogV3(touchId, remark);
            }
            JSONArray labelIdArray = jsonO.getJSONArray("labelIds");
            Map<String, Object> superData = new HashMap<>();
            // 处理自建属性
            if (labelIdArray != null || labelIdArray.size() != 0) {
                for (int i = 0; i < labelIdArray.size(); i++) {
                    superData.put(labelIdArray.getJSONObject(i).getString("labelId"), labelIdArray.getJSONObject(i).getString("optionValue"));
                }
            }
            String voiceInfoId = jsonO.getString("voice_info_id");
            if (voiceInfoId == null || "".equals(voiceInfoId)) {
                voiceInfoId = IDHelper.getID().toString();
            }
            CustomSeaTouchInfoDTO dto = new CustomSeaTouchInfoDTO(voiceInfoId, customerId, String.valueOf(userId), jsonO.getString("cust_group_id"), superId,
                    jsonO.getString("super_name"), jsonO.getString("super_age"), jsonO.getString("super_sex"), jsonO.getString("super_telphone"),
                    jsonO.getString("super_phone"), jsonO.getString("super_address_province_city"), jsonO.getString("super_address_street"),
                    seaId, superData, jsonO.getString("qq"), jsonO.getString("email"), jsonO.getString("profession"), jsonO.getString("weChat"),
                    jsonO.getString("followStatus"), jsonO.getString("invalidReason"), jsonO.getString("company"));
            // 保存标记信息
            seaService.updateClueSignData(dto);
            responseJson.setCode(200);
            responseJson.setMessage("更新成功");
        } catch (Exception e) {
            LOG.error("更新个人信息失败,", e);
            responseJson.setCode(-1);
            responseJson.setMessage("更新失败");
        }
        return responseJson;
    }

    /**
     * 添加线索
     *
     * @param jsonO
     * @return
     */
    @RequestMapping(value = "/addClueData", method = RequestMethod.POST)
    public ResponseCommon addClueData(@RequestBody JSONObject jsonO) {
        ResponseCommon responseJson = new ResponseCommon();
        String customerId = opUser().getCustId();
        Long userId = opUser().getId();
        String seaId = jsonO.getString("seaId");
        try {
            JSONArray labelIdArray = jsonO.getJSONArray("labelIds");
            Map<String, Object> superData = new HashMap<>(16);
            // 处理自建属性
            if (labelIdArray != null || labelIdArray.size() != 0) {
                for (int i = 0; i < labelIdArray.size(); i++) {
                    superData.put(labelIdArray.getJSONObject(i).getString("labelId"), labelIdArray.getJSONObject(i).getString("optionValue"));
                }
                superData.put("SYS007", "未跟进");
            }
            CustomSeaTouchInfoDTO dto = new CustomSeaTouchInfoDTO("", customerId, String.valueOf(userId), "", "",
                    jsonO.getString("super_name"), jsonO.getString("super_age"), jsonO.getString("super_sex"), jsonO.getString("super_telphone"),
                    jsonO.getString("super_phone"), jsonO.getString("super_address_province_city"), jsonO.getString("super_address_street"),
                    seaId, superData, jsonO.getString("qq"), jsonO.getString("email"), jsonO.getString("profession"), jsonO.getString("weChat"),
                    jsonO.getString("followStatus"), jsonO.getString("invalidReason"), jsonO.getString("company"));
            // 保存标记信息
            int status = seaService.addClueData0(dto);
            if (status == 1) {
                responseJson.setCode(200);
                responseJson.setMessage("添加成功");
            } else if (status == -1) {
                responseJson.setCode(-1);
                responseJson.setMessage("线索已经存在");
            } else {
                responseJson.setCode(-1);
                responseJson.setMessage("添加成功");
            }
        } catch (Exception e) {
            LOG.error("添加线索失败,", e);
            responseJson.setCode(-1);
            responseJson.setMessage("添加线索失败");
        }
        return responseJson;
    }

    @RequestMapping(value = "/selectCustomerSea/{seaId}", method = RequestMethod.GET)
    public ResponseJson selectMarketTask(@PathVariable("seaId") String seaId) {
        ResponseJson responseJson = new ResponseJson();
        if (StringUtil.isEmpty(seaId)) {
            responseJson.setCode(-1);
            responseJson.setMessage("seaId必填");
            return responseJson;
        }
        CustomerSeaDTO data;
        try {
            // 检查公海权限
            boolean status = seaService.checkCustomerSeaPermission(opUser().getCustId(), seaId);
            if (!status) {
                responseJson.setMessage("权限不足");
                responseJson.setCode(-1);
                return responseJson;
            }
            data = seaService.selectCustomerSea(seaId);
            responseJson.setData(data);
            responseJson.setCode(200);
        } catch (Exception e) {
            LOG.error("查询公海详情异常,", e);
            responseJson.setCode(-1);
        }
        return responseJson;
    }

    /**
     * 查询线索标记记录
     *
     * @param param
     * @param error
     * @return
     */
    @RequestMapping(value = "/pageSuperDataLog", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseJson pageSuperDataLog(@RequestBody @Valid CustomerSeaSearch param, BindingResult error) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasFieldErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        long userId = 0L;
        if ("2".equals(opUser().getUserType())) {
            userId = opUser().getId();
        }
        Page data;
        try {
            data = seaService.pageClueSignLog(opUser().getCustId(), param.getSeaId(), param.getSuperId(), param.getBatchId(), userId, param.getPageNum(), param.getPageSize());
            responseJson.setData(getPageData(data));
            responseJson.setCode(200);
        } catch (Exception e) {
            LOG.error("查询线索标记记录异常,", e);
            responseJson.setCode(-1);
        }
        return responseJson;
    }

    /**
     * 查询线索转交记录
     *
     * @param dto
     * @param error
     * @return
     */
    @RequestMapping(value = "/pageSuperDataOperLog", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseJson pageSuperDataOperLog(@RequestBody @Valid SuperDataOperLogQuery dto, BindingResult error) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasFieldErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        Page data;
        try {
            data = seaService.pageClueOperLog(dto, dto.getPageNum(), dto.getPageSize());
            responseJson.setData(getPageData(data));
            responseJson.setCode(200);
        } catch (Exception e) {
            LOG.error("查询线索转交记录异常,", e);
            responseJson.setCode(-1);
        }
        return responseJson;
    }

    /**
     * 判断公海-用户能否致电,发短信
     *
     * @param seaId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/validSeat", method = RequestMethod.GET)
    public ResponseJson isValidAccount(String seaId) {
        ResponseJson responseJson = new ResponseJson();
        boolean hasRemain = marketResourceService.judRemainAmount(opUser().getCustId());
        if (!hasRemain) {
            LOG.warn("客户[" + opUser().getCustId() + "]余额不足");
            responseJson.setData(false);
            responseJson.setMessage("余额不足");
            return responseJson;
        }
        if (StringUtil.isEmpty(seaId)) {
            responseJson.setCode(-1);
            responseJson.setData(false);
            responseJson.setMessage("参数错误");
            return responseJson;
        }
        LoginUser lu = opUser();
        boolean result;
        try {
            result = seaService.isValidAccount(lu, seaId);
        } catch (Exception e) {
            LOG.error("判断公海-用户是否可以致电异常", e);
            result = false;
        }
        responseJson.setData(result);
        return responseJson;
    }

    /**
     * 根据公海 手机号或者公海 身份id获取个人信息接口
     *
     * @param seaId
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/getCuleInfo", method = RequestMethod.GET)
    public String getCuleInfo(String seaId, String superId, int type) {
        ResponseJson responseJson = new ResponseJson();
        try {
            Map<String, Object> map = seaService.selectClueInfo(seaId, superId, type);
            responseJson.setData(map);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            LOG.error("根据公海id获取个人信息", e);
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 公海讯众取号接口
     *
     * @param action
     * @param taskId
     * @param compId
     * @param name
     * @param count
     * @param id
     * @param response
     */
    @RequestMapping(value = "/xzCustomerSeaGetPhone", method = {RequestMethod.GET, RequestMethod.POST})
    public void xzCustomerSeaGetPhone(String action, String taskId, String compId, String name, String count, String id, HttpServletResponse response) {
        Map<Object, Object> param = new HashMap<>();
        param.put("action", action);
        param.put("taskId", taskId);
        param.put("compId", compId);
        param.put("name", name);
        param.put("count", count);
        param.put("id", id);

        Map<String, Object> resp = new HashMap<>(), data;
        List<Map<String, Object>> list;
        int code = 200;
        String reason = "ok";
        if (StringUtil.isNotEmpty(taskId)) {
            // 获取总数量
            if ("sum".equals(action)) {
                data = seaService.xzCountByTaskId(taskId);
                resp.put("data", data);
            } else if ("number".equals(action)) {
                list = new ArrayList<>();
                int pageNum = 0;
                int pageSize = 10;
                // uuid为空则起始页为0
                if (StringUtil.isNotEmpty(id)) {
                    pageNum = NumberConvertUtil.parseInt(id);
                }
                if (StringUtil.isNotEmpty(count)) {
                    pageSize = NumberConvertUtil.parseInt(count);
                }
                // 获取手机号和随路参数
                List<XzPullPhoneDTO> phones = seaService.pagePhonesToXz(taskId, pageNum, pageSize);
                if (phones == null || phones.size() == 0) {
                    data = new HashMap<>();
                    data.put("id", "");
                    data.put("phone", "");
                    data.put("param", "");
                    data.put("isend", "false");
                    list.add(data);
                    resp.put("data", list);
                } else {
                    for (int i = 0; i < phones.size(); i++) {
                        if (StringUtil.isEmpty(phones.get(i).getPhone())) {
                            continue;
                        }
                        data = new HashMap<>();
                        data.put("id", pageNum + i + 1);
                        data.put("phone", phones.get(i).getPhone().split(","));
                        data.put("param", phones.get(i).getParam());
                        data.put("isend", "false");
                        list.add(data);
                    }
                    resp.put("data", list);
                }
            }
        } else {
            code = 500;
            reason = "taskId不能为空:" + taskId;
            LOG.warn("taskId不能为空:" + taskId);
        }
        resp.put("code", code);
        resp.put("reason", reason);
        LOG.info("讯众taskId:" + taskId + ",取号接口请求参数:" + param);
        LOG.info("讯众taskId:" + taskId + ",取号接口返回结果:" + JSON.toJSONString(resp));
        response.setContentType("application/json");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(JSON.toJSONString(resp).getBytes("UTF-8"));
        } catch (IOException e) {
            LOG.error("讯众取号接口异常,", e);
        }
    }

    /**
     * 查询讯众自动外呼监控信息
     *
     * @param seaId
     * @return
     */
    @RequestMapping(value = "/getAutoTaskMonitor", method = RequestMethod.GET)
    @ResponseBody
    public String getXzAutoTaskMonitor(String seaId) {
        if (StringUtil.isEmpty(seaId)) {
            throw new ParamException("seaId必填");
        }
        // 普通员工无权限
        if ("2".equals(opUser().getUserType()) && RoleEnum.ROLE_CUSTOMER.equals(opUser().getRole())) {
            return returnError("权限不足");
        }
        XzAutoTaskMonitor data = null;
        try {
            // 检查公海权限
            if (RoleEnum.ROLE_CUSTOMER.equals(opUser().getRole())) {
                boolean status = seaService.checkCustomerSeaPermission(opUser().getCustId(), seaId);
                if (!status) {
                    return returnError("权限不足");
                }
            }
            data = xzCallCenterService.getXzAutoTaskMonitor(2, seaId);
        } catch (Exception e) {
            LOG.error("公海查询讯众自动外呼监控信息异常,", e);
        }
        return returnJsonData(data);
    }

    public String page3() {


        return null;
    }

    /**
     * 公海内线索分页
     *
     * @param param
     * @param error
     * @return
     */
    @RequestMapping(value = "/pageClue/{seaId}", method = RequestMethod.POST)
    @CacheAnnotation
    public ResponseJson pageClueById(@RequestBody @Valid CustomerSeaSearch param, BindingResult error) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasFieldErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        Page data = null;
        try {
            param.setUserId(opUser().getId());
            param.setUserType(opUser().getUserType());
            param.setUserGroupRole(opUser().getUserGroupRole());
            param.setUserGroupId(opUser().getUserGroupId());
            param.setCustId(opUser().getCustId());
            // 查询公海线索
            if (param.getSeaType() == null || param.getSeaType() == 1) {
                data = seaService.pagePublicClue(param);
            } else if (param.getSeaType() == 2) {
                // 查询私海线索
                data = seaService.pagePrivateClue(param);
            }
            responseJson.setCode(200);
        } catch (Exception e) {
            responseJson.setCode(-1);
            LOG.error("查询公海详情列表异常,", e);
        }
        if (data == null) {
            data = new Page();
        }
        responseJson.setData(getPageData(data));
        return responseJson;
    }

    /**
     * 公海线索状态修改(批量)
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/updateClueStatus/list", method = RequestMethod.POST)
    public ResponseJson updateClueListStatus(@RequestBody JSONObject jsonObject) {
        System.err.println("开始");
        ResponseJson responseJson = new ResponseJson();
        CustomerSeaSearch param = JSON.parseObject(jsonObject.toJSONString(), CustomerSeaSearch.class);
        if (StringUtil.isEmpty(param.getSeaId())) {
            responseJson.setData("参数异常");
            responseJson.setCode(-1);
            return responseJson;
        }
        if (StringUtil.isEmpty(param.getCustType())) {
            responseJson.setData("企业id不能为空");
            responseJson.setCode(-1);
            return responseJson;
        }
        String operate = jsonObject.getString("operate");
        int data = 0;
        try {
            param.setUserId(opUser().getId());
            param.setUserType(opUser().getUserType());
            param.setUserGroupRole(opUser().getUserGroupRole());
            param.setUserGroupId(opUser().getUserGroupId());
            param.setCustId(opUser().getCustId());
            data = seaService.batchDeleteClue(param, operate);
            responseJson.setCode(200);
        } catch (Exception e) {
            LOG.error("公海线索状态修改异常,", e);
            responseJson.setCode(-1);
        }
        responseJson.setData(data);
        return responseJson;
    }


    @RequestMapping(value = "/saveImportClueData", method = RequestMethod.POST)
    @ResponseBody
    public String saveImportClueData(@RequestBody JSONObject jsonObject) {
        String fileName = jsonObject.getString("fileName");
        long seaId = jsonObject.getLong("seaId");
        JSONArray headers = jsonObject.getJSONArray("headers");
        int status = seaService.saveImportData(opUser().getCustId(), opUser().getId(), opUser().getUserType(), seaId, fileName, headers);
        ResponseJson responseJson = new ResponseJson();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        if (status == 1) {
            resultMap.put("code", 1);
            resultMap.put("message", "成功");
        } else {
            resultMap.put("code", status);
            resultMap.put("message", "失败");
        }
        responseJson.setData(resultMap);
        return JSON.toJSONString(responseJson);
    }

    /**
     * 线索分配
     *
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/distributionClue1", method = RequestMethod.POST)
    public ResponseJson distributionClue1(@RequestBody JSONObject jsonObject) {
        ResponseJson responseJson = new ResponseJson();
        Integer operate = jsonObject.getInteger("operate");
        if (operate == null) {
            responseJson.setData("operate参数必填");
            responseJson.setCode(-1);
            return responseJson;
        }
        CustomerSeaSearch param = JSON.parseObject(jsonObject.toJSONString(), CustomerSeaSearch.class);

        if (param.getUserIds() == null || param.getUserIds().size() == 0) {
            responseJson.setData("userIds参数必填");
            responseJson.setCode(-1);
        }
        // 员工和组长领取线索处理
        if ("2".equals(opUser().getUserType())) {
            List<String> userIds = new ArrayList<>();
            userIds.add(String.valueOf(opUser().getId()));
            param.setUserIds(userIds);
        }
        // 快速分配时用户和数量数组
        JSONArray assignedList = jsonObject.getJSONArray("assignedlist");
        int data = 0;
        try {
            param.setUserId(opUser().getId());
            param.setUserType(opUser().getUserType());
            param.setUserGroupRole(opUser().getUserGroupRole());
            param.setUserGroupId(opUser().getUserGroupId());
            param.setCustId(opUser().getCustId());
            // 同步操作
            synchronized (this) {
                data = seaService.distributionClue1(param, operate, assignedList);
            }
            responseJson.setCode(200);
        } catch (TouchException e) {
            responseJson.setCode(-1);
            responseJson.setMessage(e.getErrMsg());
            LOG.error("线索分配异常,", e);
        }
        responseJson.setData(data);
        return responseJson;
    }


}
