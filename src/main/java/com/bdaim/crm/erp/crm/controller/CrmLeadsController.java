package com.bdaim.crm.erp.crm.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseCommon;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.common.annotation.ClassTypeCheck;
import com.bdaim.crm.dao.LkCrmAdminFieldDao;
import com.bdaim.crm.dto.LkCrmAdminRecordDTO;
import com.bdaim.crm.entity.LkCrmAdminRecordEntity;
import com.bdaim.crm.entity.LkCrmLeadsEntity;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.entity.CrmLeads;
import com.bdaim.crm.erp.crm.service.CrmLeadsService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.customersea.dto.CustomSeaTouchInfoDTO;
import com.bdaim.customersea.dto.CustomerSeaParam;
import com.bdaim.customersea.dto.CustomerSeaSearch;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.util.IDHelper;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.MD5Util;
import com.bdaim.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.http.MediaType;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.IOException;
import java.text.ParseException;
import java.util.*;
import java.util.concurrent.ExecutionException;

/**
 * 线索公海/私海
 */
@RestController
@RequestMapping("/CrmLeads")
public class CrmLeadsController extends BasicAction {

    public static final Logger LOG = LoggerFactory.getLogger(CrmLeadsController.class);

    @Resource
    private CrmLeadsService crmLeadsService;

    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private AdminSceneService adminSceneService;

    @Resource
    private CustomerSeaService seaService;

    /*@Resource
    private LkCrmAdminFieldDao crmAdminFieldDao;*/


    /**
     * 公海内线索分页
     *
     * @return
     * @RequestParamm seaId
     * @RequestParamm jsonObject
     */
    @RequestMapping(value = "/page/cluesea/{seaId}", method = RequestMethod.POST)
    public ResponseInfo pageClueById(@PathVariable(value = "seaId") Long seaId, @RequestBody JSONObject jsonObject) {
        BasePageRequest<CrmLeads> basePageRequest = new BasePageRequest<>(jsonObject.getIntValue("page"), jsonObject.getIntValue("limit"));
        jsonObject.fluentPut("type", 1);
        basePageRequest.setJsonObject(jsonObject);
        ResponseInfo responseInfo = new ResponseInfo();
        R r = crmLeadsService.pageCluePublicSea(basePageRequest, seaId, BaseUtil.getUser().getCustId());
        responseInfo.setCode((int) r.get("code"));
        responseInfo.setMessage(String.valueOf(r.get("msg")));
        if (r.isSuccess()) {
            responseInfo.setData(r.get("data"));
        }
        return responseInfo;
    }

    /**
     * 添加线索
     *
     * @return
     * @RequestParamm jsonO
     */
    @RequestMapping(value = "/cluesea/addClueData", method = RequestMethod.POST)
    public ResponseCommon addClueData(@RequestBody JSONObject jsonO) {
        ResponseCommon responseJson = new ResponseCommon();
        LoginUser user = BaseUtil.getUser();
        String customerId = user.getCustId();
        Long userId = user.getId();
        String seaId = jsonO.getString("seaId");
        try {
            JSONArray labelIdArray = jsonO.getJSONArray("labelIds");
            Map<String, Object> superData = new HashMap<>(16);
            // 处理自建属性
            if (labelIdArray != null && labelIdArray.size() > 0) {
                for (int i = 0; i < labelIdArray.size(); i++) {
                    if ("company".equals(labelIdArray.getJSONObject(i).getString("labelId"))) {
                        String optionValue = labelIdArray.getJSONObject(i).getString("optionValue");
                        superData.put(labelIdArray.getJSONObject(i).getString("labelId"), optionValue);
                    } else {
                        superData.put(labelIdArray.getJSONObject(i).getString("labelId"), labelIdArray.getJSONObject(i).getString("optionValue"));
                    }
                }
                superData.put("SYS007", "未跟进");
            }
            superData.put("SYS014", MD5Util.encode32Bit(jsonO.getString("company")));
            CustomSeaTouchInfoDTO dto = new CustomSeaTouchInfoDTO("", customerId, String.valueOf(userId), "", "",
                    jsonO.getString("leads_name"), jsonO.getString("super_age"), jsonO.getString("super_sex"), jsonO.getString("super_telphone"),
                    jsonO.getString("super_phone"), jsonO.getString("super_address_province_city"), jsonO.getString("super_address_street"),
                    seaId, superData, jsonO.getString("qq"), jsonO.getString("email"), jsonO.getString("profession"), jsonO.getString("weChat"),
                    jsonO.getString("followStatus"), jsonO.getString("invalidReason"), jsonO.getString("company"));
            // 保存标记信息
            for (int i = 0; i < jsonO.getJSONArray("field").size(); i++) {
                // 处理线索名称
                if ("leads_name".equals(jsonO.getJSONArray("field").getJSONObject(i).getString("fieldName"))) {
                    dto.setSuper_name(jsonO.getJSONArray("field").getJSONObject(i).getString("value"));
                    //break;
                }
                if ("super_phone".equals(jsonO.getJSONArray("field").getJSONObject(i).getString("fieldName"))) {
                    dto.setSuper_phone(jsonO.getJSONArray("field").getJSONObject(i).getString("value"));
                    //break;
                }
            }
            int status = crmLeadsService.addClueData0(dto, jsonO);
            if (status == 1) {
                responseJson.setCode(200);
                responseJson.setMsg("添加成功");
            } else if (status == -1) {
                responseJson.setCode(-1);
                responseJson.setMsg("线索已经存在");
            } else {
                responseJson.setCode(-1);
                responseJson.setMsg("添加成功");
            }
        } catch (Exception e) {
            LOG.error("添加线索失败,", e);
            responseJson.setCode(-1);
            responseJson.setMsg("添加线索失败");
        }
        return responseJson;
    }

    /**
     * 编辑线索
     *
     * @return
     * @RequestParamm jsonO
     */
    @RequestMapping(value = "/cluesea/updateClueData", method = RequestMethod.POST)
    public ResponseCommon updateClueData(@RequestBody JSONObject jsonO) {
        ResponseCommon responseJson = new ResponseCommon();
        LoginUser user = BaseUtil.getUser();
        String customerId = user.getCustId();
        Long userId = user.getId();
        String remark = jsonO.getString("remark");
        String superId = jsonO.getString("superId");
        String touchId = jsonO.getString("touchId");
        String seaId = jsonO.getString("seaId");
        try {
            // 更新通话记录表的备注
            if (StringUtil.isNotEmpty(touchId)) {
                //marketResourceService.updateVoiceLogV3(touchId, remark);
            }
            JSONArray labelIdArray = jsonO.getJSONArray("labelIds");
            Map<String, Object> superData = new HashMap<>();
            // 处理自建属性
            if (labelIdArray != null && labelIdArray.size() != 0) {
                for (int i = 0; i < labelIdArray.size(); i++) {
                    superData.put(labelIdArray.getJSONObject(i).getString("labelId"), labelIdArray.getJSONObject(i).getString("optionValue"));
                }
            }
            String voiceInfoId = jsonO.getString("voice_info_id");
            if (StringUtil.isEmpty(voiceInfoId)) {
                voiceInfoId = IDHelper.getID().toString();
            }
            superData.put("SYS014", MD5Util.encode32Bit(jsonO.getString("company")));
            CustomSeaTouchInfoDTO dto = new CustomSeaTouchInfoDTO(voiceInfoId, customerId, String.valueOf(userId), jsonO.getString("cust_group_id"), superId,
                    jsonO.getString("leads_name"), jsonO.getString("super_age"), jsonO.getString("super_sex"), jsonO.getString("super_telphone"),
                    jsonO.getString("super_phone"), jsonO.getString("super_address_province_city"), jsonO.getString("super_address_street"),
                    seaId, superData, jsonO.getString("qq"), jsonO.getString("email"), jsonO.getString("profession"), jsonO.getString("weChat"),
                    jsonO.getString("followStatus"), jsonO.getString("invalidReason"), jsonO.getString("company"));
            // 保存标记信息
            crmLeadsService.updateClueSignData(dto, jsonO);
            responseJson.setCode(200);
            responseJson.setMessage("更新成功");
        } catch (Exception e) {
            LOG.error("更新个人信息失败,", e);
            responseJson.setCode(-1);
            responseJson.setMsg("更新失败");
        }
        return responseJson;
    }


    @RequestMapping(value = "/cluesea/queryById", method = RequestMethod.POST)
    public ResponseInfo clueSeaQueryById(@RequestBody JSONObject jsonO) {
        ResponseInfo responseInfo = new ResponseInfo();
        responseInfo.setCode(0);
        responseInfo.setData(crmLeadsService.queryClueById(jsonO.getLong("seaId"), jsonO.getString("id")));
        return responseInfo;
    }

    /**
     * 查看跟进记录
     */
    @RequestMapping(value = "/cluesea/getRecord", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R clueGetRecord(BasePageRequest basePageRequest, CrmLeads crmLeads, Long seaId) {
        basePageRequest.setData(crmLeads);
        return (R.ok().put("data", crmLeadsService.getRecord(basePageRequest)));
    }

    /**
     * 公海线索状态修改
     *
     * @return
     * @RequestParamm jsonObject
     */
    @RequestMapping(value = "/cluesea/updateClueStatus", method = RequestMethod.POST)
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
            LoginUser user = BaseUtil.getUser();
            param.setUserId(user.getId());
            param.setUserType(user.getUserType());
            param.setUserGroupRole(user.getUserGroupRole());
            param.setUserGroupId(user.getUserGroupId());
            param.setCustId(user.getCustId());
            if (1 == operate) {
                // 删除公海线索
                crmLeadsService.deletePublicClue(param.getSuperIds(), param.getSeaId());
            }
            if (3 == operate) {
                crmLeadsService.batchClueBackToSea(param.getUserId(), param.getUserType(), param.getSeaId(), param.getSuperIds(), param.getBackReason(), param.getBackRemark());
                // 指定ID退回公海时删除私海线索
                //crmLeadsService.deleteByBatchIds(param.getSuperIds());
            } else {
                data = seaService.updateClueStatus(param, operate);
            }
            responseJson.setCode(200);
        } catch (Exception e) {
            LOG.error("公海线索状态修改异常,", e);
            responseJson.setCode(-1);
            responseJson.setMsg("公海线索状态修改异常");
        }
        responseJson.setData(data);
        return responseJson;
    }

    /**
     * 线索分配
     *
     * @return
     * @RequestParamm jsonObject
     */
    @RequestMapping(value = "/cluesea/distributionClue", method = RequestMethod.POST)
    public ResponseJson distributionClue(@RequestBody JSONObject jsonObject) {
        ResponseJson responseJson = new ResponseJson();
        Integer operate = jsonObject.getInteger("operate");
        if (operate == null) {
            responseJson.setData("operate参数必填");
            responseJson.setMsg("operate参数必填");
            responseJson.setCode(-1);
            return responseJson;
        }
        CustomerSeaSearch param = JSON.parseObject(jsonObject.toJSONString(), CustomerSeaSearch.class);
        if (StringUtil.isEmpty(param.getSeaId())) {
            responseJson.setData("seaId参数必填");
            responseJson.setMsg("seaId参数必填");
            responseJson.setCode(-1);
            return responseJson;
        }
        /*if (param.getUserIds() == null || param.getUserIds().size() == 0) {
            responseJson.setData("userIds参数必填");
            responseJson.setMsg("userIds参数必填");
            responseJson.setCode(-1);
        }*/
        LoginUser user = BaseUtil.getUser();
        // 员工和组长领取线索处理
        if ("2".equals(user.getUserType())) {
            List<String> userIds = new ArrayList<>();
            userIds.add(String.valueOf(user.getId()));
            param.setUserIds(userIds);
        }
        if (1 == BaseUtil.getUserType() && (param.getUserIds() == null || param.getUserIds().size() == 0)) {
            List<String> userIds = new ArrayList<>();
            userIds.add(String.valueOf(user.getId()));
            param.setUserIds(userIds);
        }
        // 快速分配时用户和数量数组
        JSONArray assignedList = jsonObject.getJSONArray("assignedlist");
        int data = 0;
        try {
            param.setUserId(user.getId());
            param.setUserType(user.getUserType());
            param.setUserGroupRole(user.getUserGroupRole());
            param.setUserGroupId(user.getUserGroupId());
            param.setCustId(user.getCustId());
            // 同步操作
            synchronized (this) {
                data = crmLeadsService.distributionClue(param, operate, assignedList);
            }
            responseJson.setCode(200);
        } catch (TouchException e) {
            responseJson.setCode(-1);
            responseJson.setMsg(e.getMessage());
            LOG.error("线索分配异常,", e);
        }
        responseJson.setData(data);
        return responseJson;
    }

    /**
     * 公海基础信息分页查询
     *
     * @return
     */
    @RequestMapping(value = "/cluesea/page/query", method = RequestMethod.POST, consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseJson page(@RequestBody @Valid CustomerSeaParam param, BindingResult error) {
        ResponseJson responseJson = new ResponseJson();
        if (error.hasFieldErrors()) {
            responseJson.setData(getErrors(error));
            return responseJson;
        }
        param.setCustId(opUser().getCustId());
        param.setUserId(opUser().getId());
        param.setUserType(opUser().getUserType());
        Page page = crmLeadsService.listPublicSea(param, param.getPageNum(), param.getPageSize());
        responseJson.setData(getPageData(page));
        responseJson.setCode(200);
        return responseJson;
    }

    /**
     * 查询公海下坐席可领取线索量
     *
     * @return
     * @RequestParamm param
     */
    @RequestMapping(value = "/cluesea/selectUserGetQuantity", method = RequestMethod.POST)
    public ResponseJson selectUserGetQuantity(@RequestBody CustomerSeaSearch param) {
        ResponseJson responseJson = new ResponseJson();
        long data = 0;
        try {
            LoginUser user = BaseUtil.getUser();
            param.setUserId(user.getId());
            data = crmLeadsService.getUserReceivableQuantity(param.getSeaId(), String.valueOf(user.getId()));
            responseJson.setCode(200);
        } catch (Exception e) {
            responseJson.setCode(0);
            responseJson.setMsg(e.getMessage());
            LOG.error("查询公海下坐席可领取线索量异常,", e);
        }
        responseJson.setData(data);
        return responseJson;
    }

    @RequestMapping(value = "/deleteFiled", method = RequestMethod.POST)
    public ResponseJson deleteFiled(@RequestBody CustomerSeaSearch param) {
        ResponseJson responseJson = new ResponseJson();
        String sql = " DELETE from lkcrm_admin_field_sort;";
        /*int data = crmAdminFieldDao.executeUpdateSQL(sql);
        crmAdminFieldDao.executeUpdateSQL("DELETE FROM lkcrm_admin_field WHERE cust_id ='2005141017020043' AND (label =1 OR label =11);");
        crmAdminFieldDao.executeUpdateSQL("INSERT INTO `lkcrm_admin_field` ( `field_name`, `cust_id`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `update_time`, `examine_category_id`, `field_type`, `relevant`, `add_sort`, `add_hidden` ) SELECT `field_name`, '2005141017020043', `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `update_time`, `examine_category_id`, `field_type`, `relevant`, `add_sort`, `add_hidden` FROM lkcrm_admin_field WHERE cust_id is null AND (label =1 OR label =11);");
        crmAdminFieldDao.executeUpdateSQL("DELETE FROM lkcrm_admin_field WHERE cust_id ='2005140944070037' AND (label =1 OR label =11);");
        crmAdminFieldDao.executeUpdateSQL("INSERT INTO `lkcrm_admin_field` ( `field_name`, `cust_id`, `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `update_time`, `examine_category_id`, `field_type`, `relevant`, `add_sort`, `add_hidden` ) SELECT `field_name`, '2005140944070037', `name`, `type`, `label`, `remark`, `input_tips`, `max_length`, `default_value`, `is_unique`, `is_null`, `sorting`, `options`, `operating`, `update_time`, `examine_category_id`, `field_type`, `relevant`, `add_sort`, `add_hidden` FROM lkcrm_admin_field WHERE cust_id is null AND (label =1 OR label =11);");
        responseJson.setData(data);*/
        return responseJson;
    }


    /**
     * 查看列表页
     */
    @Permissions({"crm:leads:index"})
    @RequestMapping(value = "/queryPageList", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R queryPageList(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type", 1);
        basePageRequest.setJsonObject(jsonObject);
        return adminSceneService.filterConditionAndGetPageList(basePageRequest);
    }

    /**
     * 全局搜索查询线索
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public R queryList(BasePageRequest<CrmLeads> basePageRequest) {
        return (R.ok().put("data", crmLeadsService.getLeadsPageList(basePageRequest)));
    }

    /**
     * 新增或更新线索
     */
    @Permissions({"crm:leads:save", "crm:leads:update"})
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public R addOrUpdate(@RequestBody JSONObject object) {
        //JSONObject object = JSON.parseObject(getRawData());
        return crmLeadsService.addOrUpdate(object);
    }

    /**
     * 根据线索id查询
     */
    @Permissions("crm:leads:read")
    @NotNullValidate(value = "leadsId", message = "线索id不能为空")
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public R queryById(@RequestParam("leadsId") Integer leadsId) {
        return (R.ok().put("data", crmLeadsService.queryById(leadsId)));
    }

    /**
     * 根据线索名称查询
     */
    @RequestMapping(value = "/queryByName")
    public R queryByName(@RequestParam("name") String name) {
        return (R.ok().put("data", crmLeadsService.queryByName(name)));
    }

    /**
     * 根据id 删除线索
     */
    @Permissions("crm:leads:delete")
    @NotNullValidate(value = "leadsIds", message = "线索id不能为空")
    @RequestMapping(value = "/deleteByIds", method = RequestMethod.POST)
    public R deleteByIds(@RequestParam("leadsIds") String leadsIds) {
        return (crmLeadsService.deleteByIds(leadsIds));
    }

    /**
     * 线索转移
     */
    @Permissions("crm:leads:transfer")
    @NotNullValidate(value = "leadsIds", message = "线索id不能为空")
    @NotNullValidate(value = "newOwnerUserId", message = "新负责人id不能为空")
    @RequestMapping(value = "/changeOwnerUser", method = RequestMethod.POST)
    public R changeOwnerUser(@RequestParam("leadsIds") String leadsIds, @RequestParam("newOwnerUserId") Long newOwnerUserId) {
        return (crmLeadsService.updateOwnerUserId(leadsIds, newOwnerUserId));
    }

    /**
     * 线索转客户
     */
    @Permissions("crm:leads:transform")
    @NotNullValidate(value = "leadsIds", message = "线索id不能为空")
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public R transfer(@RequestParam("leadsIds") String leadsIds) {
        return (crmLeadsService.translate(leadsIds));
    }

    @RequestMapping(value = "/leads/company", method = RequestMethod.POST)
    public R listLeadByCompany(String company, String notInLeadsIds) {
        return R.ok().put("data", crmLeadsService.listLeadByCompany(BaseUtil.getUser().getCustId(), company, notInLeadsIds));
    }

    /**
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId", message = "线索id不能为空")
    @NotNullValidate(value = "content", message = "内容不能为空")
    @NotNullValidate(value = "category", message = "跟进类型不能为空")
    @RequestMapping(value = "/addRecord", method = RequestMethod.POST)
    public R addRecord(LkCrmAdminRecordDTO adminRecord) throws ParseException {
        String sign = CrmEnum.LEADS_TYPE_KEY.getSign();
        String typesId = adminRecord.getTypesId();
        if (StringUtil.isNotEmpty(adminRecord.getSeaId())) {
            sign = CrmEnum.PUBLIC_SEA_TYPE_KEY.getSign();
            typesId = adminRecord.getSeaId();
        }
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(sign), typesId);
        if (auth) {
            return (R.noAuth());
            //return;
        }
        LkCrmAdminRecordEntity lkCrmAdminRecordEntity = new LkCrmAdminRecordEntity();
        BeanUtils.copyProperties(adminRecord, lkCrmAdminRecordEntity, JavaBeanUtil.getNullPropertyNames(adminRecord));
        if (StringUtil.isNotEmpty(adminRecord.getNextTime())) {
            lkCrmAdminRecordEntity.setNextTime(DateUtil.parse(adminRecord.getNextTime(), "yyyy-MM-dd HH:mm:ss"));
        }
        return (crmLeadsService.addRecord(lkCrmAdminRecordEntity));
    }

    /**
     * 查看跟进记录
     */
    @RequestMapping(value = "/getRecord", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R getRecord(BasePageRequest basePageRequest, CrmLeads crmLeads) {
        basePageRequest.setData(crmLeads);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.LEADS_TYPE_KEY.getSign()), NumberUtil.parseInt(crmLeads.getLeadsId()));
        if (auth) {
            return (R.noAuth());
        }
        return (R.ok().put("data", JavaBeanUtil.recordToMap(crmLeadsService.getRecord(basePageRequest))));
    }

    /**
     * 代办事项列表
     *
     * @return
     * @RequestParamm basePageRequest
     * @RequestParamm taskStatus
     * @RequestParamm leadsId
     */
    @RequestMapping(value = "/agency/list", method = RequestMethod.POST)
    @ClassTypeCheck(classType = BasePageRequest.class)
    public R listAgency(BasePageRequest basePageRequest, Integer taskStatus, Integer leadsId) {
        basePageRequest.setData(taskStatus);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.LEADS_TYPE_KEY.getSign()), leadsId);
        if (auth) {
            return (R.noAuth());
        }
        return (R.ok().put("data", JavaBeanUtil.recordToMap(crmLeadsService.listAgency(basePageRequest, taskStatus, leadsId))));
    }

    /**
     * 批量导出线索
     */
    @Permissions("crm:leads:excelexport")
    @RequestMapping(value = "/batchExportExcel", method = RequestMethod.POST)
    public void batchExportExcel(@RequestParam(name = "ids") String leadsIds, HttpServletResponse response) throws IOException {
        List<Record> recordList = crmLeadsService.exportLeads(leadsIds);
        export(recordList, response, "1");
        //renderNull();
    }

    /**
     * 批量导出线索
     */
    @RequestMapping(value = "/cluesea/batchExportExcel", method = RequestMethod.POST)
    public void clueSeaBatchExportExcel(@RequestParam(name = "ids") String superIds, Long seaId, HttpServletResponse response) throws IOException {
        List<Record> recordList = crmLeadsService.exportPublicSeaClues(seaId, superIds);
        exportPublicSea(recordList, response, "11");
        //renderNull();
    }

    /**
     * 导出全部线索
     */
    @Permissions("crm:leads:excelexport")
    @RequestMapping(value = "/allExportExcel", method = RequestMethod.POST)
    public void allExportExcel(String search, HttpServletResponse response) throws IOException {
        //JSONObject jsonObject = basePageRequest.getJsonObject();
        BasePageRequest<Void> basePageRequest = new BasePageRequest<>();
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("search", search);
        jsonObject.fluentPut("excel", "yes").fluentPut("type", "1");
        basePageRequest.setJsonObject(jsonObject);
        List<Record> recordList = (List<Record>) adminSceneService.filterConditionAndGetPageList(basePageRequest).get("excel");
        export(recordList, response, "1");
        //renderNull();
    }

    /**
     * 导出公海全部线索
     */
    @RequestMapping(value = "/cluesea/allExportExcel", method = RequestMethod.POST)
    public void clueSeaAllExportExcel(Long seaId, String search, HttpServletResponse response) throws IOException, TouchException {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("search", search);
        jsonObject.fluentPut("excel", "yes").fluentPut("type", "1");
        BasePageRequest basePageRequest = new BasePageRequest();
        basePageRequest.setJsonObject(jsonObject);
        List<Record> recordList = JavaBeanUtil.mapToRecords(crmLeadsService.listCluePublicSea(basePageRequest, seaId, BaseUtil.getUser().getCustId()));
        exportPublicSea(recordList, response, "11");
        //renderNull();
    }

    /**
     * 线索私海导出
     *
     * @throws IOException
     * @RequestParamm recordList
     * @RequestParamm response
     * @RequestParamm label
     */
    private void export(List<Record> recordList, HttpServletResponse response, String label) throws IOException {
        ExcelWriter writer = null;
        try {
            writer = ExcelUtil.getWriter();
            //AdminFieldService adminFieldService = new AdminFieldService();
            List<Record> fieldList = adminFieldService.customFieldList(label);
            writer.addHeaderAlias("leads_name", "线索名称");
            writer.addHeaderAlias("next_time", "下次联系时间");
            writer.addHeaderAlias("telephone", "电话");
            writer.addHeaderAlias("mobile", "手机号");
            writer.addHeaderAlias("address", "地址");
            writer.addHeaderAlias("remark", "备注");
            writer.addHeaderAlias("create_user_name", "创建人");
            writer.addHeaderAlias("owner_user_name", "负责人");
            writer.addHeaderAlias("create_time", "创建时间");
            writer.addHeaderAlias("update_time", "更新时间");
            for (Record field : fieldList) {
                writer.addHeaderAlias(field.getStr("name"), field.getStr("name"));
            }
            writer.merge(fieldList.size() + 1, "线索信息");
            //HttpServletResponse response = getResponse();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Record record : recordList) {
                list.add(record.remove("company", "is_lock", "sea_id", "cust_id", "batch_id", "is_transform", "customer_id", "leads_id", "owner_user_id", "create_user_id", "followup", "field_batch_id").getColumns());
            }
            writer.write(list, true);
            writer.setRowHeight(0, 30);
            writer.setRowHeight(1, 20);
            for (int i = 0; i < fieldList.size() + 15; i++) {
                writer.setColumnWidth(i, 20);
            }
            Cell cell = writer.getCell(0, 0);
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = writer.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 16);
            cellStyle.setFont(font);
            cell.setCellStyle(cellStyle);
            //自定义标题别名
            //response为HttpServletResponse对象
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=leads.xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭writer，释放内存
            writer.close();
        }
    }

    /**
     * 线索公海导出
     *
     * @throws IOException
     * @RequestParamm recordList
     * @RequestParamm response
     * @RequestParamm label
     */
    private void exportPublicSea(List<Record> recordList, HttpServletResponse response, String label) throws IOException {
        ExcelWriter writer = null;
        try {
            writer = ExcelUtil.getWriter();
            //AdminFieldService adminFieldService = new AdminFieldService();
            List<Record> fieldList = adminFieldService.customFieldList(label);
            writer.addHeaderAlias("id", "线索唯一标识");
            writer.addHeaderAlias("leads_name", "线索名称");
            writer.addHeaderAlias("super_phone", "电话");
            writer.addHeaderAlias("super_telphone", "手机号");
            writer.addHeaderAlias("super_address_street", "地址");
            writer.addHeaderAlias("next_time", "下次联系时间");
            writer.addHeaderAlias("remark", "备注");
            for (Record field : fieldList) {
                writer.addHeaderAlias(field.getStr("field_name"), field.getStr("name"));
            }

            writer.addHeaderAlias("create_user_name", "创建人");
            writer.addHeaderAlias("owner_user_name", "负责人");
            writer.addHeaderAlias("create_time", "创建时间");
            writer.addHeaderAlias("update_time", "更新时间");
            writer.addHeaderAlias("call_count", "呼叫次数");
            writer.addHeaderAlias("last_call_time", "最后通话时间");
            writer.addHeaderAlias("last_call_status", "最后呼叫状态");

            writer.merge(fieldList.size() + 1, "线索信息");
            //HttpServletResponse response = getResponse();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Record record : recordList) {
                record.remove("custType", "entId", "intentLevel", "lastCallTime", "n_id");
                record.remove("user_id", "status", "call_empty_count", "call_success_count", "call_fail_count", "data_source", "intent_level", "last_call_time");
                record.remove("last_called_duration", "pull_status", "status", "super_age", "super_name", "super_sex", "user_get_time", "user_group_id");
                record.remove("callCount", "last_mark_time", "sms_success_count", "pre_user_id", "super_address_province_city");
                list.add(record.remove("super_data", "batch_id", "is_transform", "customer_id", "leads_id", "owner_user_id", "create_user_id", "followup", "field_batch_id").getColumns());
            }
            writer.write(list, true);
            writer.setRowHeight(0, 30);
            writer.setRowHeight(1, 20);
            for (int i = 0; i < fieldList.size() + 15; i++) {
                writer.setColumnWidth(i, 20);
            }
            Cell cell = writer.getCell(0, 0);
            CellStyle cellStyle = cell.getCellStyle();
            cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            Font font = writer.createFont();
            font.setBold(true);
            font.setFontHeightInPoints((short) 16);
            cellStyle.setFont(font);
            cell.setCellStyle(cellStyle);
            //自定义标题别名
            //response为HttpServletResponse对象
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=sea_list" + DateUtil.format(new Date(), DatePattern.PURE_DATETIME_PATTERN) + ".xls");
            ServletOutputStream out = response.getOutputStream();
            writer.flush(out);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭writer，释放内存
            writer.close();
        }
    }

    /**
     * 获取线索导入模板
     */
    @RequestMapping(value = "/downloadExcel")
    public void downloadExcel(HttpServletResponse response) {
        List<Record> recordList = adminFieldService.queryAddField(1);
        recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("线索导入表");
        sheet.setDefaultColumnWidth(12);
        sheet.setDefaultRowHeight((short) 400);
        HSSFRow titleRow = sheet.createRow(0);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        cellStyle.setFont(font);
        titleRow.createCell(0).setCellValue("线索导入模板(*)为必填项");
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(0).setCellStyle(cellStyle);
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, recordList.size() - 1);
        sheet.addMergedRegion(region);
        try {
            HSSFRow row = sheet.createRow(1);
            for (int i = 0; i < recordList.size(); i++) {
                Record record = recordList.get(i);
                String[] setting = record.get("setting");
                // 在第一行第一个单元格，插入选项
                HSSFCell cell = row.createCell(i);
                // 普通写入操作
                if (record.getInt("is_null") == 1) {
                    cell.setCellValue(record.getStr("name") + "(*)");
                } else {
                    cell.setCellValue(record.getStr("name"));
                }
                if (setting.length != 0) {
                    // 生成下拉列表
                    CellRangeAddressList regions = new CellRangeAddressList(2, Integer.MAX_VALUE, i, i);
                    // 生成下拉框内容
                    DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                    // 绑定下拉框和作用区域
                    HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                    // 对sheet页生效
                    sheet.addValidationData(dataValidation);
                }
            }
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=leads_import.xls");
            wb.write(response.getOutputStream());
        } catch (Exception e) {
            LOG.error("error", e);
        } finally {
            try {
                wb.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //renderNull();
    }

    /**
     * 获取线索导入模板
     */
    @RequestMapping(value = "/cluesea/downloadExcel")
    public void clueSeaDownloadExcel(HttpServletResponse response) {
        List<Record> recordList = adminFieldService.queryAddField(11);
        recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("线索导入表");
        sheet.setDefaultColumnWidth(12);
        sheet.setDefaultRowHeight((short) 400);
        HSSFRow titleRow = sheet.createRow(0);
        CellStyle cellStyle = wb.createCellStyle();
        cellStyle.setFillForegroundColor(IndexedColors.SKY_BLUE.getIndex());
        cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        Font font = wb.createFont();
        font.setBold(true);
        font.setFontHeightInPoints((short) 16);
        cellStyle.setFont(font);
        titleRow.createCell(0).setCellValue("线索导入模板(*)为必填项");
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(0).setCellStyle(cellStyle);
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, recordList.size() - 1);
        sheet.addMergedRegion(region);
        try {
            HSSFRow row = sheet.createRow(1);
            for (int i = 0; i < recordList.size(); i++) {
                Record record = recordList.get(i);
                String[] setting = record.get("setting");
                // 在第一行第一个单元格，插入选项
                HSSFCell cell = row.createCell(i);
                // 普通写入操作
                if (record.getInt("is_null") == 1) {
                    cell.setCellValue(record.getStr("name") + "(*)");
                } else {
                    cell.setCellValue(record.getStr("name"));
                }
                if (setting.length != 0) {
                    // 生成下拉列表
                    CellRangeAddressList regions = new CellRangeAddressList(2, Integer.MAX_VALUE, i, i);
                    // 生成下拉框内容
                    DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                    // 绑定下拉框和作用区域
                    HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                    // 对sheet页生效
                    sheet.addValidationData(dataValidation);
                }
            }
            //HttpServletResponse response = getResponse();
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=public_sea_import.xls");
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            LOG.error("error", e);
        } finally {
            try {
                wb.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
        //renderNull();
    }

    /**
     * 线索导入
     */
    @Permissions("crm:leads:excelimport")
    @NotNullValidate(value = "ownerUserId", message = "请选择负责人")
    @Before(Tx.class)
    @RequestMapping(value = "/uploadExcel")
    public R uploadExcel(Integer repeatHandling, Long ownerUserId) {
        //Db.tx(() -> {
        MultipartFile file = null;
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                BaseUtil.getRequest().getSession().getServletContext());
        if (multipartResolver.isMultipart(BaseUtil.getRequest())) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) BaseUtil.getRequest();
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                MultipartFile multiRequestFile = multiRequest.getFile(iter.next());
                if (multiRequestFile != null) {
                    file = multiRequestFile;
                    break;
                }
            }
        }
        R result = crmLeadsService.uploadExcel(file, repeatHandling, ownerUserId);
        return (result);
        //return !result.get("code").equals(500);
        //});
    }

    /**
     * 线索导入
     */
    @Before(Tx.class)
    @RequestMapping(value = "/cluesea/uploadExcel")
    public R clueSeaUploadExcel(Integer repeatHandling, Long ownerUserId, Long seaId) {
        //Db.tx(() -> {
        MultipartFile file = null;
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                BaseUtil.getRequest().getSession().getServletContext());
        if (multipartResolver.isMultipart(BaseUtil.getRequest())) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) BaseUtil.getRequest();
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                MultipartFile multiRequestFile = multiRequest.getFile(iter.next());
                if (multiRequestFile != null) {
                    file = multiRequestFile;
                    break;
                }
            }
        }
        if (ownerUserId == null) {
            ownerUserId = BaseUtil.getUserId();
        }
        R result = crmLeadsService.uploadExcelPublicSea(file, repeatHandling, ownerUserId, seaId);
        return (result);
        //return !result.get("code").equals(500);
        //});
    }

    /**
     * @author wyq
     * 客户锁定
     */
    @Permissions("crm:customer:lock")
    @NotNullValidate(value = "ids", message = "客户id不能为空")
    @NotNullValidate(value = "isLock", message = "锁定状态不能为空")
    @RequestMapping(value = "/lock", method = RequestMethod.POST)
    public R lock(LkCrmLeadsEntity crmCustomer, String ids) {
        return (crmLeadsService.lock(crmCustomer, ids));
    }

    /**
     * 跟进记录类型设置
     */
    @RequestMapping(value = "/queryRecordOptions", method = RequestMethod.POST)
    public R queryRecordOptions() {
        return (crmLeadsService.queryRecordOptions());
    }

    /**
     * 设置线索跟进状态
     */
    @RequestMapping(value = "/setRecordOptions", method = RequestMethod.POST)
    public R setRecordOptions(@RequestBody JSONObject jsonObject) {
        //JSONObject jsonObject = JSONObject.parseObject(getRawData());
        //JSONArray jsonArray = JSONArray.parseArray(jsonObject.getString("value"));
        //List<String> list = jsonArray.toJavaList(String.class);
        List<String> list = (List<String>) jsonObject.get("value");
        return (crmLeadsService.setRecordOptions(list));
    }
}
