package com.bdaim.customgroup.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.bdaim.account.dto.RemainSourceDTO;
import com.bdaim.auth.LoginUser;
import com.bdaim.callcenter.dto.XfPullPhoneDTO;
import com.bdaim.callcenter.dto.XzPullPhoneDTO;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.annotation.ValidatePermission;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.exception.ParamException;
import com.bdaim.common.filter.FiledFilter;
import com.bdaim.common.service.PhoneService;
import com.bdaim.common.util.AuthPassport;
import com.bdaim.common.util.Constant;
import com.bdaim.common.util.NumberConvertUtil;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.customgroup.dto.CustomerGroupAddDTO;
import com.bdaim.customgroup.dto.CustomerGroupParamDTO;
import com.bdaim.customgroup.dto.CustomerGrpOrdParam;
import com.bdaim.customgroup.entity.CustomGroup;
import com.bdaim.customgroup.service.CustomGroupService;
import com.bdaim.industry.dto.IndustryPoolPriceDTO;
import com.bdaim.industry.service.IndustryPoolService;
import com.bdaim.label.dto.QueryParam;
import com.bdaim.label.entity.LabelAudit;
import com.bdaim.label.service.CommonService;
import com.bdaim.label.service.LabelAuditService;
import com.bdaim.label.service.LabelInfoService;
import com.bdaim.marketproject.service.MarketProjectService;
import com.bdaim.markettask.service.MarketTaskService;
import com.bdaim.rbac.dto.UserQueryParam;
import com.bdaim.rbac.entity.User;
import com.bdaim.resource.service.MarketResourceService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.*;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.util.*;

/**
 * 客群
 */
@Controller
@RequestMapping("/customgroup")
public class CustomGroupAction extends BasicAction {

    private static Logger log = LoggerFactory.getLogger(CustomGroupAction.class);

    @Resource
    private CustomGroupService customGroupService;
    @Resource
    private IndustryPoolService industryService;
    @Resource
    private LabelInfoService labelInfoService;
    @Resource
    private LabelAuditService labelAuditService;
    @Resource
    private JdbcTemplate jdbcTemplate;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private CommonService commonService;
    @Resource
    private PhoneService phoneService;
    @Resource
    private MarketTaskService marketTaskService;
    @Resource
    private MarketProjectService marketProjectService;
    @Resource
    private CustomerSeaService customerSeaService;


    public CustomGroupAction() {
        super.pageName = "用户群管理";
    }

    /**
     * 根据id获取客户群信息
     *
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getCustomGroupById")
    public String getCustomGroupById(HttpServletRequest request, Integer id) {
        CustomGroup custom = customGroupService.getCustomGroupById(id);
        Map<String, Object> map = commonService.getCustomGroupMap(custom);

        //operation log
//        super.operlog(request, (Integer) map.get("id"));

        return JSON.toJSONString(map);
    }

    /**
     * 根据条件查询群内微观画像信息
     *
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getCustomGroupByCondition")
    public String getCustomGroupByCondition(HttpServletRequest request, CustomGroup group,
                                            QueryParam param, Page page) {
        String queryType = param.getQueryType();
        Integer dayType = param.getDayType();
        Map<String, Object> map = new HashMap<String, Object>();
        Map<String, Object> likeMap = new HashMap<String, Object>();
        if (null != dayType) {
            if (dayType.equals(Constant.DAYTYPE_DAY)) {
                map.put(Constant.FILTER_KEY_PREFIX + "dayType", 1);
            } else if (dayType.equals(Constant.DAYTYPE_WEEK)) {
                map.put(Constant.FILTER_KEY_PREFIX + "dayType", 7);
            } else if (dayType.equals(Constant.DAYTYPE_MONTH)) {
                map.put(Constant.FILTER_KEY_PREFIX + "dayType", 30);
            }
        }
        User u = opUser().getUser();
        if (null != queryType & null != u) {
            if (queryType.equals("dept")) {
                map.put("createUser.department.id", u.getDepartment().getId());
            } else if (queryType.equals("mine")) {
                map.put("createUser.id", u.getId());
            }
        }
        if (null != group) {
            String name = group.getName();
            if (null != name && (!name.isEmpty())) {
                likeMap.put("name", name);
            }
            Integer status = group.getStatus();
            if (null != status) {
                map.put("status", status);
            }
        }
        List<CustomGroup> groups = customGroupService.getListByCondition(map,
                likeMap, page);
        Integer count = customGroupService.getCountByCondition(map, likeMap,
                null);
        Map<String, Object> resultMap = new HashMap<String, Object>();
        resultMap.put("total", count);
        resultMap.put("stores", commonService.getCustomGroupMapList(groups));

        //operation log,循环吧所有查询的CUSTOMERGROUP都插进来
//        super.operlog(request, -1);

        return JSON.toJSONString(resultMap, new FiledFilter());
    }

    /**
     * 增加用户群信息
     *
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/addCustomGroup")
    public String addCustomGroup(CustomGroup customGroup) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        LoginUser lu = opUser();
        customGroup.setCreateUserId(lu.getId().toString());
        customGroup.setUpdateUserId(lu.getId().toString());
        customGroup.setEnterpriseName(lu.getEnterpriseName());
        Integer id = customGroupService.addCustomGroup(customGroup);
        LabelAudit audit = new LabelAudit();
        audit.setAid(id);
        audit.setApplyType(Constant.APPLY_TYPE_CUSTOM_GROUP_CREATE);
        audit.setApplyUser(opUser().getUser());
        audit.setAuditType(Constant.AUDIT_TYPE_GROUP);
        audit.setAuditUser(opUser().getUser());
        audit.setName(customGroup.getName());
        audit.setStatus(Constant.AUDITING);
        audit = labelAuditService.getLabelAudit(audit, null);
        labelAuditService.addAuditInfo(audit);
        resultMap.put("id", id);

        //operation logs
//        super.operlog(request, id);

        return JSON.toJSONString(resultMap);
    }

    /**
     * 预览用户群信息,  从ES搜索
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/previewByGroupCondition")
    public String previewByGroupCondition(String name, Integer cycle, String groupCondition) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            Map<String, Long> map = customGroupService.previewByGroupCondition(name, cycle, groupCondition);
            resultMap.putAll(map);
        } catch (Exception e) {
            log.error("通过es预览标签覆盖人数异常,", e);
        }
        return JSON.toJSONString(resultMap);
    }

    /**
     * 根据客群ID和自建属性值预览满足条件的人数
     *
     * @param jsonObject
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "/previewCustomGroupInfo", method = RequestMethod.POST)
    public String previewCustomGroupInfo(@RequestBody JSONObject jsonObject) {
        int customGroupId = jsonObject.getIntValue("customGroupId");
        String groupCondition = jsonObject.getString("groupCondition");
        Map<String, Object> resultMap = new HashMap<>();
        boolean status = customGroupService.checkCustomerGroupPermission(opUser().getCustId(), customGroupId);
        if (!status) {
            return returnError("权限不足");
        }
        try {
            resultMap = customGroupService.previewCustomGroupInfo(customGroupId, groupCondition);
        } catch (Exception e) {
            log.error("根据客群ID和自建属性值查询数据库满足条件的人数异常,", e);
        }
        return returnJsonData(resultMap);
    }

    /**
     * 获取群内微观画像用户信息列表
     *
     * @param customGroupId
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getMicroscopicPictureList")
    public String getMicroscopicPictureList(HttpServletRequest request, Integer customGroupId, Page page,
                                            String queryType, String key) {
        Map<String, Object> result = new HashMap<String, Object>();
        Map<String, Object> map = customGroupService
                .getUserGroupGid(customGroupId, page.getStart(),
                        page.getLimit(), queryType, key);
        result.putAll(map);

        //operation log
//        super.operlog(request, customGroupId);

        return JSON.toJSONString(result);
    }

    /**
     * 特征发现报告
     *
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getCharacteristic")
    public String getCharacteristic(CustomGroup group) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        Map<String, Object> map = customGroupService.getCharacteristic(group);
        resultMap.putAll(map);
        return JSON.toJSONString(resultMap);
    }

    /**
     * 下载申请
     *
     * @param groupId
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/applyDownloadUserProfileByGroup")
    public String applyDownloadUserProfileByGroup(Integer groupId) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        int status = customGroupService
                .applyDownloadUserProfileByGroup(groupId);
        if (status == Constant.DOWNLOAD_APPLY) {
            resultMap.put("downloadStatus", status);
            resultMap.put("downloadStatusCn",
                    Constant.DOWNLOAD_STATUS.get(status));
            return JSON.toJSONString(resultMap);
        } else {
            throw new RuntimeException("用户群下载状态异常！");
        }
    }

    /**
     * 下载
     *
     * @param response
     */
    @CacheAnnotation
    @RequestMapping(value = "downloadUserProfileByGroup")
    public String download(Integer groupId, HttpServletResponse response,
                           Model model) {
        boolean error = false;
        File f = null;
        CustomGroup group = customGroupService.getCustomGroupById(groupId);
        if (group == null) {
            log.error("用户群为空:" + groupId);
            model.addAttribute("用户群为空:" + groupId);
            model.addAttribute("dir", "error");
            return "error";
        }
        String filePath = group.getFilePath();
        if (filePath == null) {
            log.error("filePath为空:" + groupId);
            error = true;
        } else {
            f = new File(filePath);
            if (!f.exists()) {
                log.error("File为空:" + groupId);
                error = true;
            }
        }
        if (error) {
            group.setDownloadStatus(Constant.DOWNLOAD_NOTAPPLY);
            customGroupService.updateCustomGroup(group);
            log.error("用户群" + group.getName() + "下载异常，请重新申请下载！");
            model.addAttribute("message", "用户群" + group.getName()
                    + "下载异常，请重新申请下载！");
            model.addAttribute("dir", "error");
            return "error";
        } else {
            response.setHeader("Content-Type", "application/vnd.ms-excel");
            try {
                response.setHeader(
                        "Content-Disposition",
                        "attachment;filename="
                                + URLEncoder.encode(group.getName(), "utf-8").replace("+", "%20")
                                + ".zip");

            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        InputStream is = null;
        int len = 0;
        try {
            is = new FileInputStream(f);
            byte[] b = new byte[1024];
            try {
                while ((len = is.read(b)) > 0) {
                    response.getOutputStream().write(b, 0, len);
                }
                group.setDownloadStatus(Constant.DOWNLOAD_NOTAPPLY);
                customGroupService.updateCustomGroup(group);
            } catch (IOException e) {
                log.error("下载失败！", e);
            }
        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } finally {
            try {
                if (is != null)
                    is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
//		InputStream is = null;
//		try {
//			is = new FileInputStream(f);
//			long l = Files.copy(Paths.get(filePath), response.getOutputStream());
//			System.out.println(l);
//			group.setDownloadStatus(Constant.DOWNLOAD_NOTAPPLY);
//			customGroupService.updateCustomGroup(group);
//		} catch (FileNotFoundException e1) {
//			e1.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
//		} finally {
//			try {
//				if (is != null)
//					is.close();
//			} catch (IOException e) {
//				e.printStackTrace();
//			}
//		}
        return null;
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/customerGroupInfoCoverNum")
    public String customerGroupInfoCoverNum(Integer groupId, HttpServletResponse response) {
        Map<String, Object> resultMap = new HashMap<String, Object>();
        try {
            CustomGroup customGroup = customGroupService.getCustomGroupById(groupId);
            Map<String, Object> map = commonService.getCustomGroupMap(customGroup);
            Map<String, Long> mapg = customGroupService.previewByGroupCondition(customGroup.getName(), customGroup.getCycle(), customGroup.getGroupCondition());
//            super.operlog(request, (null != customGroup.getId()) ? customGroup.getId() : -1);
            resultMap.putAll(map);
            resultMap.putAll(mapg);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return JSON.toJSONString(resultMap);
    }

    Float a;

    @ResponseBody
    @RequestMapping("/test1")
    public String test1() {

        System.out.println(a.doubleValue());
        return "success";
    }

    /**
     * 查询剩余可购买量
     *
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "/getRemainSourceGroupCondition", method = RequestMethod.POST)
    public String getRemainSourceGroupCondition(String groupCondition, Integer industryPoolId) {
        List<RemainSourceDTO> list = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            //判断当前用户是否存在相同条件的待支付的客户群
            boolean status = customGroupService.checkCGoupOrderStatus(opUser().getCustId(), groupCondition, 2);
            if (status) {
                resultMap.put("remainSource", null);
                resultMap.put("message", "您拥有相同条件待支付的客户群,请先支付!");
                return JSON.toJSONString(resultMap);
            }
            RemainSourceDTO rsd = new RemainSourceDTO();
            rsd.setCount(2000L);
            rsd.setRemain(500000L);
            rsd.setTotal(1200L);
            rsd.setSourceName("获客");
            rsd.setSourceId(0);
            IndustryPoolPriceDTO salePrice = industryService.getIndustryPoolPrice(groupCondition, industryPoolId, opUser().getCustId());
            // 计费价格/人
            rsd.setSalePrice(new BigDecimal(salePrice.getPrice()).divide(new BigDecimal(100)));
            // 标签池-客户资源配置
            rsd.setDataCustConfig(salePrice.getDataCustConfig());
            list.add(rsd);
            resultMap.put("priceStatus", salePrice.getStatus());
        } catch (Exception e) {
            log.error("创建客户群异常,", e);
            resultMap.put("priceStatus", 2);
        }
        resultMap.put("remainSource", list);
        return JSON.toJSONString(resultMap);
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getRemainSourceGroupConditionV1")
    public String getRemainSourceGroupConditionV1(CustomGroup customGroup, Integer industryPoolId) {
        LoginUser lu = opUser();
        customGroup.setCreateUserId(lu.getId().toString());
        List<RemainSourceDTO> list = new ArrayList<>();
        Map<String, Object> resultMap = new HashMap<>();
        try {
            //判断当前用户是否存在相同条件的待支付的客户群
            StringBuffer sb = new StringBuffer();
            sb.append(" SELECT ");
            sb.append(" 	count(*) as count ");
            sb.append(" FROM ");
            sb.append(" 	`customer_group` c ");
            sb.append(" LEFT JOIN t_order t ON t.order_id = c.order_id ");
            sb.append(" WHERE ");
            sb.append(" 	t.order_state = 1 ");
            sb.append(" AND c.`STATUS` =2 ");
            sb.append(" AND c.cust_id='" + lu.getCustId() + "'");
            sb.append(" AND c.group_condition='" + customGroup.getGroupCondition() + "'");
            Integer count = jdbcTemplate.queryForObject(sb.toString(), Integer.class);
            if (count > 0) {
                resultMap.put("remainSource", null);
                resultMap.put("message", "您拥有相同条件待支付的客户群,请先支付!");
                return JSON.toJSONString(resultMap);
            }
            // 根据用户群条件查询可购买量
            list = customGroupService.getRemainSourceByGroupConditionV1(customGroup, industryPoolId);
            if (list != null && list.size() > 0) {
                // 处理销售价格
                for (RemainSourceDTO remainSourceDTO : list) {
                    if (remainSourceDTO.getSalePrice() != null) {
                        remainSourceDTO.setSalePrice(remainSourceDTO.getSalePrice().divide(new BigDecimal(100)));
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        resultMap.put("remainSource", list);
        return JSON.toJSONString(resultMap);
    }

    /**
     * 增加用户群信息
     *
     * @return
     * @throws Exception
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "/saveCustomGroup", method = RequestMethod.POST)
    public String saveCustomGroup(@RequestBody CustomerGroupAddDTO customGroupDTO) throws Exception {
        LoginUser lu = opUser();
        Map<String, Object> resultMap = new HashMap<String, Object>();
        customGroupDTO.setCustId(lu.getCustId());
        customGroupDTO.setCreateUserId(lu.getId().toString());
        customGroupDTO.setUpdateUserId(lu.getId().toString());
        resultMap.put("data", JSONObject.toJSON(customGroupService.addCustomGroupV1(customGroupDTO)));
        //operation logs
//		super.operlog(request, id);
        return JSON.toJSONString(resultMap);
    }

    /**
     * 回调测试
     *
     * @return
     * @throws TouchException
     */
//	@ResponseBody
//	@CacheAnnotation
//	@RequestMapping("/callBackCustomGroup")
//	public String callBackCustomGroup(String orderId) throws TouchException {
//		customGroupService.addCustomGroupData(orderId);
//		return JSON.toJSONString("完成");
//	}

    /**
     * 客户群用户列表
     *
     * @param customer_group_id
     * @param
     * @return
     * @throws TouchException
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getCustomGroup")
    public String getCustomGroup(String customer_group_id, Integer pageNum, Integer pageSize, String id,
                                 String customer_name, Integer states, String callType, String dateStart, String dateEnd,
                                 String enterprise_name, String marketProjectId) {
        LoginUser u = opUser();
        String cust_id = u.getCustId();
        String user_id = u.getId().toString();
        JSONObject json = new JSONObject();

        Page p = customGroupService.page(customer_group_id, cust_id, user_id, pageNum, pageSize,
                id, customer_name, states, callType, dateStart, dateEnd, enterprise_name, marketProjectId);

        json.put("data", p);
        return json.toJSONString();
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "/getCustomGroupV1")
    public String getCustomGroup0(@Valid PageParam page, BindingResult error, String customer_group_id, String id, String userName, Integer status,
                                  String callType, String action, String intentLevel, String labelProperty) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        JSONArray custProperty = JSON.parseArray(labelProperty);
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<>();
        Page data = null;
        try {
            data = customGroupService.getCustomGroupDataV4(opUser(), customer_group_id, page.getPageNum(), page.getPageSize(), id, userName, status, callType, action, intentLevel, custProperty);
        } catch (Exception e) {
            log.error("查询客户群数据列表异常,", e);
        }
        if (data == null) {
            data = new Page();
        }
        map.put("data", data.getData());
        map.put("total", data.getTotal());
        json.put("data", map);
        return json.toJSONString();
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "/getCustomGroupCountV1", method = RequestMethod.GET)
    public String getCustomGroup0(String customer_group_id, String id, String userName, Integer status, String callType) {
        JSONObject json = new JSONObject();
        Map<Object, Object> map = new HashMap<Object, Object>();
        long total = customGroupService.countCustomGroupDataV2(opUser(), customer_group_id, id, userName, status, callType, "");
        map.put("total", total);
        json.put("data", map);
        return json.toJSONString();
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "/countCallProgressByCondition")
    public String countCallProgressByCondition(String customerGroupId) {
        Map<Object, Object> map = new HashMap<>(16);
        Map<String, Object> list = customGroupService.countCallProgressByCondition0(opUser().getId().toString(), opUser().getUserType(), customerGroupId, opUser().getCustId());
        map.put("data", list);
        return JSON.toJSONString(map);
    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "/countCallProgressByCondition0")
    public String countCallProgressByCondition0(String customerGroupId) {
        Map<Object, Object> map = new HashMap<>(16);
        Map<String, Object> list = customGroupService.countCallProgressByConditionV3(opUser(), customerGroupId);
        map.put("data", list);
        return JSON.toJSONString(map);
    }


    @ResponseBody
    @RequestMapping(value = "/getCustomerGroupInfo", method = RequestMethod.GET)
    public String getCustomerGroupInfo(String customerGroupId) {
        Map<Object, Object> map = new HashMap<>(16);
        Map<String, Object> data = customGroupService.getCustomerGroupInfo(customerGroupId);
        map.put("data", data);
        return JSON.toJSONString(map);
    }

    @ResponseBody
    @RequestMapping(value = "/getPersonInfoByPhone", method = RequestMethod.GET)
    public String getPersonInfoByPhone(String phone, String customerGroupId, String marketTaskId) {
        log.info("根据手机号获取个人信息:" + phone + ",customerGroupId:" + customerGroupId + ",marketTaskId:" + marketTaskId);
        JSONObject json = new JSONObject();
        Map<String, Object> map = new HashMap<>();
        Map<String, Object> selectedLabels = new HashMap<>();
        Map<String, Object> personInfo = new HashMap<>();
        try {
            String superId = phoneService.getSuperIdByPhone(customerGroupId, phone);
            // 更新客户群和营销任务责任人为当前接通用户的id
            customGroupService.updateCustomerGroupAssigned(customerGroupId, marketTaskId, superId, String.valueOf(opUser().getId()));
            // 获取已选属性
            //selectedLabels = marketResourceService.getSelLabelByPhone(superId, customerGroupId);
            // 获取客户群下的客户基本信息
            personInfo = customGroupService.getCustomerGroupPersonInfoV4(customerGroupId, marketTaskId, superId);
            selectedLabels.put("selLabel", personInfo.get("selLabel"));
        } catch (Exception e) {
            log.error("根据手机号获取个人信息异常,", e);
        } finally {
            map.put("selectedLabels", selectedLabels);
            map.put("personInfo", personInfo);
            personInfo.remove("selLabel");
            personInfo.remove("super_data");
            json.put("data", map);
        }
        log.info("根据手机号获取个人信息:" + phone + ",customerGroupId:" + customerGroupId + ",marketTaskId:" + marketTaskId + ",返回数据:" + json.toJSONString());
        return json.toJSONString();
    }

    @RequestMapping(value = "/taskphone", method = {RequestMethod.POST, RequestMethod.GET})
    public void getTaskPhones(String action, String requestid, String uuid, String count, String customParam, HttpServletResponse response) throws IOException {
        Map<Object, Object> params = new HashMap<>();
        Map<Object, Object> data = new HashMap<>();
        Map map = request.getParameterMap();
        Set keSet = map.entrySet();
        for (Iterator itr = keSet.iterator(); itr.hasNext(); ) {
            Map.Entry me = (Map.Entry) itr.next();
            Object key = me.getKey();
            Object ov = me.getValue();
            String[] value = new String[1];
            if (ov instanceof String[]) {
                value = (String[]) ov;
            } else {
                value[0] = ov.toString();
            }
            for (int k = 0; k < value.length; k++) {
                params.put(key, value[k]);
            }
        }
        data.put("action", action);
        data.put("requestid", requestid);
        if (StringUtil.isNotEmpty(customParam)) {
            // 获取总数量
            if ("sum".equals(action)) {
                long sum = customGroupService.countMarketTaskUsersByTaskId(customParam);
                data.put("sum", sum);
            } else if ("numbers".equals(action)) {
                int pageNum = 0;
                int pageSize = 10;
                // uuid为空则起始页为0
                if (StringUtil.isNotEmpty(uuid)) {
                    pageNum = Integer.parseInt(uuid);
                }
                if (StringUtil.isNotEmpty(count)) {
                    pageSize = Integer.parseInt(count);
                }
                // 返回空数组 标识结束
                if (StringUtil.isNotEmpty(uuid) && Integer.parseInt(uuid) == 0) {
                    data.put("numbers", new ArrayList<>());
                    data.put("uuid", 0);
                } else {
                    List<String> phones = customGroupService.listMarketTaskPhoneByTaskId(customParam, pageNum, pageSize);
                    // 如果结果大于0,并且页数量大于返回结果数量
                    if (phones.size() > 0) {
                        data.put("uuid", phones.size() + pageNum);
                    } else {
                        data.put("uuid", 0);
                    }
                    data.put("numbers", phones);
                }
            }
            data.put("msg", "成功");
        } else {
            data.put("msg", "customParam不能为空");
        }
        response.setContentType("application/json");
        log.info("参数:" + JSON.toJSONString(params, SerializerFeature.WriteMapNullValue));
        log.info(JSON.toJSONString(data, SerializerFeature.WriteMapNullValue));
        response.getOutputStream().write(JSON.toJSONString(data, SerializerFeature.WriteMapNullValue).getBytes("UTF-8"));
    }

    /**
     * 讯众自动任务拉取号码接口
     *
     * @param action
     * @param taskId
     * @param compId
     * @param name
     * @param count
     * @param id
     * @param response
     */
    @RequestMapping(value = "/xzTaskGetPhone", method = {RequestMethod.GET, RequestMethod.POST})
    public void getTaskPhones(String action, String taskId, String compId, String name, String count, String id, HttpServletResponse response) {
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
                data = customGroupService.xzCountMarketTaskUsersByTaskId(taskId);
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
                List<XzPullPhoneDTO> phones = customGroupService.pageMarketTaskPhoneByTaskId(taskId, pageNum, pageSize);
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
            log.warn("taskId不能为空:" + taskId);
        }
        resp.put("code", code);
        resp.put("reason", reason);
        log.info("讯众taskId:" + taskId + ",取号接口请求参数:" + param);
        log.info("讯众taskId:" + taskId + ",取号接口返回结果:" + JSON.toJSONString(resp));
        response.setContentType("application/json");
        try (ServletOutputStream outputStream = response.getOutputStream()) {
            outputStream.write(JSON.toJSONString(resp).getBytes("UTF-8"));
        } catch (IOException e) {
            log.error("讯众取号接口异常,", e);
        }
    }

    @RequestMapping(value = "/xfTaskPhones", method = RequestMethod.GET)
    public void getXFTaskPhones(String taskId, String telCnt, HttpServletResponse response) throws IOException {
        Map<Object, Object> params = new HashMap<>();
        Map map = request.getParameterMap();
        Set keSet = map.entrySet();
        for (Iterator itr = keSet.iterator(); itr.hasNext(); ) {
            Map.Entry me = (Map.Entry) itr.next();
            Object key = me.getKey();
            Object ov = me.getValue();
            String[] value = new String[1];
            if (ov instanceof String[]) {
                value = (String[]) ov;
            } else {
                value[0] = ov.toString();
            }
            for (int k = 0; k < value.length; k++) {
                params.put(key, value[k]);
            }
        }
        log.info("新方自动外呼获取手机号接口参数打印:" + params.toString());
        long telCntLong = 10;
        if (StringUtil.isNotEmpty(telCnt) && StringUtil.isNotEmpty(taskId)) {
            telCntLong = Long.parseLong(telCnt);
        } else {
            log.error("新方自动外呼参数异常telCnt:" + telCnt + "taskId:" + taskId);
            return;
        }
        String content = "";
        // 先查询公海
        XfPullPhoneDTO result = customerSeaService.pagePhonesToXf(taskId, telCntLong);
        if (result.getResult().intValue() == 1) {
            // 再查营销任务
            result = marketTaskService.getMarketTaskPhonesToXf(taskId, telCntLong);
            // 再查下客户群
            if (result.getResult().intValue() == 1) {
                content = customGroupService.getCustomGroupPhoneListByTaskId(taskId, telCntLong);
            }
        }
        // 处理号码内容
        if (StringUtil.isEmpty(content) && result.getResult().intValue() == 0) {
            content = result.getContent();
        }
      /*  XfPullPhoneDTO result = marketTaskService.getMarketTaskPhonesToXf(taskId, telCntLong);
        // 未查询到营销任务
        String content = "";
        if (result.getResult() != null && result.getResult().intValue() == 1) {
            content = customGroupService.getCustomGroupPhoneListByTaskId(taskId, telCntLong);
        } else {
            content = result.getContent();
        }*/
        log.info("新方自动外呼telCnt:" + telCnt + ",taskId:" + taskId + "获取手机号返回数据content:" + content);
        response.getOutputStream().write(content.getBytes("UTF-8"));
    }


    /**
     * 客户列表
     *
     * @param customer_group_id
     * @param
     * @return
     * @throws TouchException
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/getCustomGroupList")
    public String getCustomGroupList(String customer_group_id, Integer pageNum, Integer pageSize, String id) {
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        if (!"ROLE_USER".equals(lu.getRole()) && !"admin".equals(lu.getRole())) {
            return json.toJSONString();
        }
        Page p = customGroupService.listPage(customer_group_id, pageNum, pageSize, id);
        json.put("data", p);
        return json.toJSONString();
    }

    @RequestMapping(value = "/countCustomerGroupCallDataV1", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String countCustomerGroupCallData(String customerGroupId, String startTime, String endTime, String timeType) {
        LoginUser lu = opUser();
        int type = 0;
        if (StringUtil.isNotEmpty(timeType)) {
            type = Integer.parseInt(timeType);
        }
        Map<String, Object> marketData = customGroupService.countCustomerGroupCallDataV4(type, customerGroupId, opUser(), startTime, endTime, "邀约状态", "", "成功");
        return JSON.toJSONString(marketData);
    }

    /**
     * 统计营销任务或客户群呼叫统计界面的数据
     *
     * @param customerGroupId
     * @param marketTaskId
     * @param startTime
     * @param endTime
     * @param timeType
     * @return
     */
    @RequestMapping(value = "/countCustomerGroupCallData", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String countCustomerGroupCallDataV1(String customerGroupId, String marketTaskId, String startTime, String endTime, String timeType, String workPlaceId, String marketProjectId, String seaId) {
        int type = 0;
        if (StringUtil.isNotEmpty(timeType)) {
            type = Integer.parseInt(timeType);
        }
        UserQueryParam userQueryParam = getUserQueryParam();
        Map<String, Object> marketData = null;
        if (StringUtil.isNotEmpty(marketTaskId)) {
            marketData = marketTaskService.statMarketTaskCallData(type, marketTaskId, userQueryParam, startTime, endTime, workPlaceId);
        } else if (StringUtil.isNotEmpty(customerGroupId)) {
            marketData = customGroupService.countCustomerGroupCallDataV5(type, customerGroupId, userQueryParam, startTime, endTime);
        } else if (StringUtil.isNotEmpty(marketProjectId)) {
            userQueryParam.setCustId(opUser().getCustId());
            marketData = marketProjectService.statMarketProjectCallData(type, marketProjectId, userQueryParam, startTime, endTime, workPlaceId);
        }

        return JSON.toJSONString(marketData);
    }

    /**
     * 统计营销任务或客户群用户呼叫数据
     *
     * @param page
     * @param error
     * @param customerGroupId
     * @param marketTaskId
     * @param startTime
     * @param endTime
     * @param timeType
     * @return
     */
    @RequestMapping(value = "/countCGUserCallData", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String countCGUserCallData(@Valid PageParam page, BindingResult error, String customerGroupId, String marketTaskId, String startTime, String endTime, String timeType, String workPlaceId, String marketProjectId, String seaId) {
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        int type = 0;
        if (StringUtil.isNotEmpty(timeType)) {
            type = Integer.parseInt(timeType);
        }
        UserQueryParam userQueryParam = getUserQueryParam();
        userQueryParam.setPageSize(page.getPageSize());
        userQueryParam.setPageNum(page.getPageNum());
        Map<String, Object> marketData = null;
        if (StringUtil.isNotEmpty(marketTaskId)) {
            marketData = marketTaskService.statCGUserCallData(type, marketTaskId, userQueryParam, startTime, endTime, workPlaceId);
        } else if (StringUtil.isNotEmpty(customerGroupId)) {
            marketData = customGroupService.countCGUserCallData(type, customerGroupId, userQueryParam, startTime, endTime);
        } else if (StringUtil.isNotEmpty(marketProjectId)) {
            if ("2".equals(opUser().getUserType())) {
                log.warn("普通用户无权查询项目统计分析");
                marketData = new HashMap<>();
            } else {
                userQueryParam.setCustId(opUser().getCustId());
                marketData = marketProjectService.statCGUserCallDataByProjectId(type, marketProjectId, userQueryParam, startTime, endTime);
            }
        }

        return JSON.toJSONString(marketData);
    }

    /**
     * 导出营销任务或客户群呼叫统计界面的数据
     *
     * @param customerGroupId
     * @param marketTaskId
     * @param startTime
     * @param endTime
     * @param timeType
     * @param response
     */
    @RequestMapping(value = "/exportCustomerGroupCallData", method = RequestMethod.GET)
    public void exportCustomerGroupCallData(String customerGroupId, String marketTaskId, String startTime, String endTime, String timeType, String workPlaceId, String marketProjectId, HttpServletResponse response, String seaId) {
        int type = 0;
        if (StringUtil.isNotEmpty(timeType)) {
            type = Integer.parseInt(timeType);
        }
        UserQueryParam userQueryParam = getUserQueryParam();
        if (StringUtil.isNotEmpty(marketTaskId)) {
            marketTaskService.exportMarketTaskCallData0(type, marketTaskId, userQueryParam, startTime, endTime, response, workPlaceId);
        } else if (StringUtil.isNotEmpty(customerGroupId)) {
            customGroupService.exportCustomerGroupCallData(type, customerGroupId, userQueryParam, startTime, endTime, response);
        } else if (StringUtil.isNotEmpty(marketProjectId)) {
            userQueryParam.setCustId(opUser().getCustId());
            marketProjectService.exportMarketProjectCallData0(type, marketProjectId, userQueryParam, startTime, endTime, response, workPlaceId);
        }

    }

    @ResponseBody
    @CacheAnnotation
    @RequestMapping("/updateCustomerGroupState")
    public String getCustomGroup(String id) {
        return customGroupService.updateCustomerGroupState(id);
    }

    @ResponseBody
    @RequestMapping(value = "/updateCustomerGroupState", method = RequestMethod.POST)
    public String getCustomGroup(@RequestBody JSONObject param) {
        String id = param.getString("id");
        Integer status = param.getInteger("status");
        if (StringUtil.isEmpty(id)) {
            throw new ParamException("id必填");
        }
        if (status == null) {
            throw new ParamException("status必填");
        }
        JSONObject json = new JSONObject();
        int code = customGroupService.updateCustomerGroupState(opUser().getCustId(), id, status);
        if (code == 1) {
            json.put("code", 0);
            json.put("message", "成功");
        } else {
            json.put("code", 1);
            json.put("message", "失败");
        }
        return returnJsonData(json);
    }


    @RequestMapping(value = "/updateTaskId", method = RequestMethod.POST)
    @ResponseBody
    public String updateTaskId(String customerGroupId, String taskId) {
        LoginUser lu = opUser();
        String customerId = lu.getCustId();
        int result = customGroupService.updateCustomerGroupTaskId(customerId, customerGroupId, taskId);
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();
    }

    @RequestMapping(value = "/updateCustomerGroupTaskIdAndTaskPhoneIndex", method = RequestMethod.POST)
    @ResponseBody
    public String updateCustomerGroupTaskIdAndTaskPhoneIndex(String customerGroupId, String taskId) {
        LoginUser lu = opUser();
        String customerId = lu.getCustId();
        int result = customGroupService.updateCustomerGroupTaskIdAndTaskPhoneIndex(customerId, customerGroupId, taskId);
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();

    }

    /**
     * @param param
     * @param taskTypeIsNotNull true-只查询已经创建的任务 false-查询全部
     * @return
     */
    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "/listMarketTask", method = RequestMethod.POST)
    public String listMarketTask(CustomerGrpOrdParam param, boolean taskTypeIsNotNull) {
        JSONObject json = new JSONObject();
        List<Map<String, Object>> data = null;
        try {
            if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
                data = customGroupService.adminListMarketTask(param);
            } else {
                data = customGroupService.listMarketTask(opUser(), param, taskTypeIsNotNull);
            }

        } catch (Exception e) {
            data = new ArrayList<>();
            Map<String, Object> map = new HashMap<>();
            map.put("total", 0);
            map.put("list", new ArrayList<>());
            data.add(map);
            log.error("获取营销任务列表异常,", e);
        }
        json.put("data", data);
        return json.toJSONString();
    }

    @RequestMapping(value = "/createMarketTask", method = RequestMethod.POST)
    @ResponseBody
    public String createMarketTask(@RequestBody JSONObject jsonObject) {
        LoginUser lu = opUser();
        String customerId = lu.getCustId();
        String groupId = jsonObject.getString("id");
        String taskId = jsonObject.getString("taskId");
        int taskType = jsonObject.getInteger("taskType");
        String userGroupId = jsonObject.getString("userGroupId");
        long taskEndTime = jsonObject.getLong("taskEndTime");

        int result = customGroupService.createMarketTask(customerId, groupId, taskId, taskType, userGroupId, taskEndTime);
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();

    }

    /**
     * 更新客户群的所属项目ID
     *
     * @param jsonObject
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/setMarketProjectId", method = RequestMethod.POST)
    public String updateCustomeGroupMarketProject(@RequestBody JSONObject jsonObject) {
        int result = 0;
        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
            int customerGroupId = jsonObject.getIntValue("id");
            Integer marketProjectId = jsonObject.getInteger("marketProjectId");
            result = customGroupService.updateCustomeGroupMarketProject(opUser().getCustId(), customerGroupId, marketProjectId);
        } else {
            return returnError("权限不足");
        }
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();
    }

    /**
     * 导出满足自建属性的单个营销任务或者客户群的成功单
     *
     * @param response
     * @param startTime
     * @param endTime
     * @param customerGroupId
     * @param marketTaskId
     */
    @RequestMapping(value = "/exportCustomerGroupMarketData", method = RequestMethod.GET)
    public void exportCustomerGroupMarketData(HttpServletResponse response, String startTime, String endTime, String customerGroupId, String marketTaskId, String marketProjectId) {
        response.setContentType("application/json;charset=utf-8");
        if (StringUtil.isNotEmpty(marketTaskId)) {
            marketTaskService.exportMarketTaskSuccessToExcel(response, opUser(), marketTaskId, "", "邀约状态", "成功",
                    startTime, endTime);
        } else if (StringUtil.isNotEmpty(customerGroupId)) {
            customGroupService.exportCustomerGroupMarketDataToExcelV3(response, opUser(), customerGroupId, "", "邀约状态", "成功",
                    startTime, endTime);
        } else if (StringUtil.isNotEmpty(marketProjectId)) {
            marketProjectService.exportProjectSuccessToExcel(response, opUser(), NumberConvertUtil.parseInt(marketProjectId), "", "邀约状态", "成功",
                    startTime, endTime);
        }
    }

    @RequestMapping(value = "/exportCustomerGroupMarketDataV1", method = RequestMethod.GET)
    public void exportCustomerGroupMarketDataV1(HttpServletResponse response, String startTime, String endTime, String customerGroupId) {
        response.setContentType("application/json;charset=utf-8");
        if (!"".equals(opUser().getUsername())) {
            UserQueryParam userQueryParam = getUserQueryParam();
            customGroupService.exportCustomerGroupMarketDataToExcelV4(response, userQueryParam, customerGroupId, "", "邀约状态", "成功",
                    startTime, endTime);
        } else {
            log.warn(opUser().getUsername() + "非法请求接口");
            try {
                response.getOutputStream().write("{\"msg\":\"非法请求接口\"}".getBytes("UTF-8"));
            } catch (IOException e) {
                log.error(opUser().getUsername() + "非法请求接口异常,", e);
            }
        }
    }

    @RequestMapping(value = "/exportCustomerGroupMarketAllData", method = RequestMethod.GET)
    public void exportCustomerGroupMarketDataToExcelV4(HttpServletResponse response, String startTime, String endTime, String customerGroupId) {
        response.setContentType("application/json;charset=utf-8");
        if (!"".equals(opUser().getUsername())) {
            customGroupService.exportCustomerGroupMarketAllDataToExcel(response, opUser(), customerGroupId, "", "邀约状态", "成功",
                    startTime, endTime);
        } else {
            log.warn(opUser().getUsername() + "非法请求接口");
            try {
                response.getOutputStream().write("{\"msg\":\"非法请求接口\"}".getBytes("UTF-8"));
            } catch (IOException e) {
                log.error(opUser().getUsername() + "非法请求接口异常,", e);
            }
        }
    }

    @RequestMapping(value = "/downloadMarketDataToExcelBySuperData", method = RequestMethod.GET)
    public void downloadMarketDataToExcelBySuperData(HttpServletResponse response, String startTime, String endTime, String customerGroupId, String callStatus, String labelIds, String labelValues, String intentLevel) {
        response.setContentType("application/json;charset=utf-8");
        List<Map<String, String>> labelValueList = new ArrayList<>();
        // 处理根据自建属性导出
        if (StringUtil.isNotEmpty(labelIds) && StringUtil.isNotEmpty(labelValues)) {
            String[] labelIdArr = labelIds.split(",");
            String[] labelValueArr = labelValues.split(",");
            if (labelIdArr.length == labelValueArr.length) {
                Map<String, String> m;
                for (int i = 0; i < labelIdArr.length; i++) {
                    m = new HashMap<>();
                    m.put("labelId", labelIdArr[i]);
                    m.put("labelValue", labelValueArr[i]);
                    labelValueList.add(m);
                }
            } else {
                log.warn(opUser().getUsername() + "自建属性参数异常,labelIds:" + labelIds + ",labelValues:" + labelValues);
                try {
                    response.getOutputStream().write("{\"msg\":\"自建属性参数异常\"}".getBytes("UTF-8"));
                } catch (IOException e) {
                    log.error(opUser().getUsername() + "自建属性参数异常,", e);
                } finally {
                    return;
                }
            }
        }
        customGroupService.exportRobotCustomerGroupIntentLevelToExcel(response, opUser(), customerGroupId, startTime, endTime, callStatus, intentLevel, labelValueList);
    }

    @RequestMapping(value = "/exportCustomerGroupRecordFile", method = RequestMethod.GET)
    public void exportCustomerGroupRecordFile(HttpServletResponse response, String startTime, String endTime, String customerGroupId) {
        response.setContentType("application/json;charset=utf-8");
        customGroupService.exportCustomerGroupRecordFile(response, opUser(), null, "邀约状态", "成功",
                startTime, endTime, customerGroupId);
    }

    @ResponseBody
    @RequestMapping(value = "/updateMarketTaskTime", method = RequestMethod.POST)
    public String updateMarketTaskTime(@RequestBody JSONObject jsonObject) {
        int result = 0;
        if ("ROLE_USER".equals(opUser().getRole()) || "admin".equals(opUser().getRole())) {
            int customerGroupId = jsonObject.getIntValue("id");
            long endTime = jsonObject.getLongValue("endTime");
            result = customGroupService.updateMarketTaskTime(customerGroupId, endTime);
        } else {
            return returnError("权限不足");
        }
        if (result == 1) {
            return returnSuccess();
        }
        return returnError();
    }

    @RequestMapping(value = "/exportCustGroupStatData", method = RequestMethod.GET)
    @ValidatePermission(role = "admin,ROLE_USER")
    public void exportCustGroupStatData(HttpServletResponse response) {
        customGroupService.exportCustGroupStatData(response);
    }


    @RequestMapping(value = "/saveImportCustGroupInfo", method = RequestMethod.POST)
    @ResponseBody
    public String saveImportCustGroupInfo(@RequestBody JSONObject jsonObject) {
        String name = jsonObject.getString("name");
        String fileName = jsonObject.getString("fileName");
        Integer projectId = jsonObject.getInteger("projectId");
        JSONArray headers = jsonObject.getJSONArray("headers");
        // 触达方式
        String touchModes = jsonObject.getString("touchMode");
        int status = customGroupService.saveImportCustGroupData(opUser().getCustId(), name, fileName, headers, projectId, touchModes);
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

    @RequestMapping(value = "/uploadCustGroupData", method = RequestMethod.POST)
    @ResponseBody
    public String importCustGroupData(HttpServletRequest request) {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        MultipartFile f = null;
        if (multipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                f = multiRequest.getFile(iter.next());
                break;
            }
        }
        ResponseJson responseJson = new ResponseJson();
        Map<String, Object> resultMap = new HashMap<>();
        if (f == null) {
            resultMap.put("code", 0);
            resultMap.put("message", "文件为空");
            responseJson.setData(resultMap);
            return JSON.toJSONString(responseJson);
        }
        int status = customGroupService.checkUploadCustGroupData(f, null);
        if (status == 1) {
            String fileName = customGroupService.uploadCustGroupData(f, null);
            resultMap.put("code", 1);
            resultMap.put("message", "成功");
            resultMap.put("url", fileName);
        } else {
            resultMap.put("code", status);
            String msg = "文件异常";
            if (status == -1) {
                msg = "文件格式不正确";
            } else if (status == -2) {
                msg = "表头必须包含手机号";
            } else if (status == -3) {
                msg = "表头为空";
            } else if (status == -4) {
                msg = "总行数超过限制";
            } else if (status == -5) {
                msg = "文件为空";
            }
            resultMap.put("message", msg);
        }
        responseJson.setData(resultMap);
        return JSON.toJSONString(responseJson);
    }

    @RequestMapping(value = "/updateCGroupTransfer", method = RequestMethod.GET)
    @ResponseBody
    @ValidatePermission(role = "admin,ROLE_USER")
    public String customerGroupTransferByCustId(String custGroupId, String custId) {
        if (StringUtil.isEmpty(custGroupId)) {
            throw new ParamException("custGroupId必填");
        }
        if (StringUtil.isEmpty(custId)) {
            throw new ParamException("custId必填");
        }
        int status = customGroupService.customerGroupTransferByCustId(NumberConvertUtil.parseInt(custGroupId), custId);
        if (status >= 1) {
            return returnSuccess();
        }
        return returnError();
    }

    /**
     * 对外接口-提取客户群
     *
     * @param groupId
     * @param pageNum
     * @param pageSize
     * @param response
     */
    @AuthPassport
    @CacheAnnotation
    @RequestMapping(value = "/superids", method = RequestMethod.POST)
    public void getCustomGroupList(String groupId, Integer pageNum, Integer pageSize, HttpServletResponse response) {
        JSONObject json = new JSONObject();
        response.setContentType("application/json; charset=utf-8");
        if (StringUtil.isEmpty(groupId)) {
            json.put("errorDesc", "03");
            try {
                response.getWriter().write(json.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        LoginUser lu = opUser();
        if (lu == null || StringUtil.isEmpty(lu.getCustId())) {
            json.put("errorDesc", "04");
            try {
                response.getWriter().write(json.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        json = customGroupService.getCustomGroupList(lu.getCustId(), groupId, pageNum, pageSize);
        try {
            response.getWriter().write(json.toJSONString());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对外接口-创建客户群
     *
     * @param customGroupDTO
     * @param response
     * @throws Exception
     */
    @AuthPassport
    @RequestMapping(value = "/create", method = RequestMethod.POST)
    public void createCustomGroup(@RequestBody CustomerGroupParamDTO customGroupDTO, HttpServletResponse response) {
        log.info("interface createCustomGroup:" + customGroupDTO.toString());
        JSONObject json = new JSONObject();
        response.setContentType("application/json; charset=utf-8");
        if (StringUtil.isEmpty(customGroupDTO.getLabel())
                || StringUtil.isEmpty(customGroupDTO.getName())
                || StringUtil.isEmpty(customGroupDTO.getEnd_date())
                || StringUtil.isEmpty(customGroupDTO.getName())
                || StringUtil.isEmpty(customGroupDTO.getStart_date())
                || StringUtil.isEmpty(customGroupDTO.getProjectId())
                || StringUtil.isEmpty(customGroupDTO.getTouchType())
                || StringUtil.isEmpty(customGroupDTO.getNum())
                || StringUtil.isEmpty(customGroupDTO.getPoolId())) {
            json.put("errorDesc", "02");
            try {
                response.getWriter().write(json.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }

        LoginUser lu = opUser();
        if (lu == null || StringUtil.isEmpty(lu.getCustId())) {
            json.put("errorDesc", "04");
            try {
                response.getWriter().write(json.toJSONString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return;
        }
        customGroupDTO.setCustId(lu.getCustId());
        customGroupDTO.setCreateUserId(lu.getId().toString());
        customGroupDTO.setUpdateUserId(lu.getId().toString());

        try {
            json = customGroupService.addCustomGroupV2(customGroupDTO);
            response.getWriter().write(json.toJSONString());
        } catch (Exception e) {
            e.printStackTrace();
            log.error("创建客户群异常",e);
            json.put("errorDesc", "05");
            try {
                response.getWriter().write(json.toJSONString());
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
    }
}
