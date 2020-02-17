package com.bdaim.crm.erp.crm.controller;

import cn.hutool.core.date.DatePattern;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.NumberUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseCommon;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.exception.TouchException;
import com.bdaim.crm.common.annotation.LoginFormCookie;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.dao.LkCrmAdminFieldDao;
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
import com.bdaim.customersea.dto.CustomerSeaSearch;
import com.bdaim.customersea.service.CustomerSeaService;
import com.bdaim.util.IDHelper;
import com.bdaim.util.JavaBeanUtil;
import com.bdaim.util.MD5Util;
import com.bdaim.util.StringUtil;
import com.jfinal.aop.Before;
import com.jfinal.core.paragetter.Para;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.*;

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

    @Resource
    private LkCrmAdminFieldDao crmAdminFieldDao;

    /**
     * 公海内线索分页
     *
     * @param seaId
     * @param jsonObject
     * @return
     */
    @RequestMapping(value = "/page/cluesea/{seaId}", method = RequestMethod.POST)
    public R pageClueById(@PathVariable(value = "seaId") Long seaId, @RequestBody JSONObject jsonObject) {
        BasePageRequest<CrmLeads> basePageRequest = new BasePageRequest<>(jsonObject.getIntValue("page"), jsonObject.getIntValue("limit"));
        jsonObject.fluentPut("type", 1);
        basePageRequest.setJsonObject(jsonObject);
        return crmLeadsService.pageCluePublicSea(basePageRequest, seaId, BaseUtil.getUser().getCustId());
    }

    /**
     * 添加线索
     *
     * @param jsonO
     * @return
     */
    @RequestMapping(value = "/cluesea/addClueData", method = RequestMethod.POST)
    public ResponseCommon addClueData(@RequestBody JSONObject jsonO) {
        ResponseCommon responseJson = new ResponseCommon();
        String customerId = BaseUtil.getUser().getCustId();
        Long userId = BaseUtil.getUser().getId();
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
            int status = crmLeadsService.addClueData0(dto, jsonO);
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

    /**
     * 编辑线索
     *
     * @param jsonO
     * @return
     */
    @RequestMapping(value = "/cluesea/updateClueData", method = RequestMethod.POST)
    public ResponseCommon updateClueData(@RequestBody JSONObject jsonO) {
        ResponseCommon responseJson = new ResponseCommon();
        String customerId = BaseUtil.getUser().getCustId();
        Long userId = BaseUtil.getUser().getId();
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
            responseJson.setMessage("更新失败");
        }
        return responseJson;
    }


    @Permissions("crm:leads:read")
    @NotNullValidate(value = "leadsId", message = "线索id不能为空")
    @RequestMapping(value = "/cluesea/queryById", method = RequestMethod.POST)
    public R clueSeaQueryById(@RequestBody JSONObject jsonO) {
        return (R.ok().put("data", crmLeadsService.queryClueById(jsonO.getLong("seaId"), jsonO.getString("id"))));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @RequestMapping(value = "/cluesea/getRecord", method = RequestMethod.POST)
    public R clueGetRecord(BasePageRequest basePageRequest, CrmLeads crmLeads, Long seaId) {
        basePageRequest.setData(crmLeads);
        return (R.ok().put("data", crmLeadsService.getRecord(basePageRequest)));
    }

    /**
     * 公海线索状态修改
     *
     * @param jsonObject
     * @return
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
            param.setUserId(BaseUtil.getUser().getId());
            param.setUserType(BaseUtil.getUser().getUserType());
            param.setUserGroupRole(BaseUtil.getUser().getUserGroupRole());
            param.setUserGroupId(BaseUtil.getUser().getUserGroupId());
            param.setCustId(BaseUtil.getUser().getCustId());
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
    @RequestMapping(value = "/cluesea/distributionClue", method = RequestMethod.POST)
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
        if ("2".equals(BaseUtil.getUser().getUserType())) {
            List<String> userIds = new ArrayList<>();
            userIds.add(String.valueOf(BaseUtil.getUser().getId()));
            param.setUserIds(userIds);
        }
        // 快速分配时用户和数量数组
        JSONArray assignedList = jsonObject.getJSONArray("assignedlist");
        int data = 0;
        try {
            param.setUserId(BaseUtil.getUser().getId());
            param.setUserType(BaseUtil.getUser().getUserType());
            param.setUserGroupRole(BaseUtil.getUser().getUserGroupRole());
            param.setUserGroupId(BaseUtil.getUser().getUserGroupId());
            param.setCustId(BaseUtil.getUser().getCustId());
            // 同步操作
            synchronized (this) {
                data = crmLeadsService.distributionClue(param, operate, assignedList);
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
    @RequestMapping(value = "/cluesea/selectUserGetQuantity", method = RequestMethod.POST)
    public ResponseJson selectUserGetQuantity(@RequestBody CustomerSeaSearch param) {
        ResponseJson responseJson = new ResponseJson();
        long data = 0;
        try {
            param.setUserId(BaseUtil.getUser().getId());
            data = seaService.getUserReceivableQuantity(param.getSeaId(), String.valueOf(BaseUtil.getUser().getId()));
            responseJson.setCode(200);
        } catch (Exception e) {
            responseJson.setCode(0);
            responseJson.setMessage(e.getMessage());
            LOG.error("查询公海下坐席可领取线索量异常,", e);
        }
        responseJson.setData(data);
        return responseJson;
    }

    @RequestMapping(value = "/deleteFiled", method = RequestMethod.POST)
    public ResponseJson deleteFiled(@RequestBody CustomerSeaSearch param) {
        ResponseJson responseJson = new ResponseJson();

        /*int data = crmAdminFieldDao.executeUpdateSQL("");
        responseJson.setData(data);*/
        return responseJson;
    }


    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:leads:index"})
    @RequestMapping(value = "/queryPageList", method = RequestMethod.POST)
    public R queryPageList(@RequestBody JSONObject jsonObject) {
        BasePageRequest<Void> basePageRequest = new BasePageRequest<>();
        jsonObject.fluentPut("type", 1);
        basePageRequest.setJsonObject(jsonObject);
        //resp.setData(adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data"));
        return (adminSceneService.filterConditionAndGetPageList(basePageRequest));
        //return resp;
    }

    /**
     * @author wyq
     * 全局搜索查询线索
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public R queryList(BasePageRequest<CrmLeads> basePageRequest) {
        return (R.ok().put("data", crmLeadsService.getLeadsPageList(basePageRequest)));
    }

    /**
     * @author wyq
     * 新增或更新线索
     */
    @Permissions({"crm:leads:save", "crm:leads:update"})
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public R addOrUpdate(@RequestBody JSONObject object) {
        //JSONObject object = JSON.parseObject(getRawData());
        return crmLeadsService.addOrUpdate(object);
    }

    /**
     * @author wyq
     * 根据线索id查询
     */
    @Permissions("crm:leads:read")
    @NotNullValidate(value = "leadsId", message = "线索id不能为空")
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public R queryById(@Para("leadsId") Integer leadsId) {
        return (R.ok().put("data", crmLeadsService.queryById(leadsId).getColumns()));
    }

    /**
     * @author wyq
     * 根据线索名称查询
     */
    @RequestMapping(value = "/queryByName")
    public R queryByName(@Para("name") String name) {
        return (R.ok().put("data", crmLeadsService.queryByName(name)));
    }

    /**
     * @author wyq
     * 根据id 删除线索
     */
    @Permissions("crm:leads:delete")
    @NotNullValidate(value = "leadsIds", message = "线索id不能为空")
    @RequestMapping(value = "/leadsIds", method = RequestMethod.POST)
    public R deleteByIds(@Para("leadsIds") String leadsIds) {
        return (crmLeadsService.deleteByIds(leadsIds));
    }

    /**
     * @author wyq
     * 线索转移
     */
    @Permissions("crm:leads:transfer")
    @NotNullValidate(value = "leadsIds", message = "线索id不能为空")
    @NotNullValidate(value = "newOwnerUserId", message = "新负责人id不能为空")
    @RequestMapping(value = "/changeOwnerUser", method = RequestMethod.POST)
    public R changeOwnerUser(@Para("leadsIds") String leadsIds, @Para("newOwnerUserId") Integer newOwnerUserId) {
        return (crmLeadsService.updateOwnerUserId(leadsIds, newOwnerUserId));
    }

    /**
     * @author wyq
     * 线索转客户
     */
    @Permissions("crm:leads:transform")
    @NotNullValidate(value = "leadsIds", message = "线索id不能为空")
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public R transfer(@Para("leadsIds") String leadsIds) {
        return (crmLeadsService.translate(leadsIds));
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId", message = "线索id不能为空")
    @NotNullValidate(value = "content", message = "内容不能为空")
    @NotNullValidate(value = "category", message = "跟进类型不能为空")
    @RequestMapping(value = "/addRecord", method = RequestMethod.POST)
    public R addRecord(@Para("") LkCrmAdminRecordEntity adminRecord) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.LEADS_TYPE_KEY.getSign()), NumberUtil.parseInt(adminRecord.getTypesId()));
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmLeadsService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @RequestMapping(value = "/getRecord", method = RequestMethod.POST)
    public R getRecord(BasePageRequest basePageRequest, CrmLeads crmLeads) {
        basePageRequest.setData(crmLeads);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.LEADS_TYPE_KEY.getSign()), NumberUtil.parseInt(crmLeads.getLeadsId()));
        if (auth) {
            return (R.noAuth());
        }
        return (R.ok().put("data", crmLeadsService.getRecord(basePageRequest)));
    }

    /**
     * @author wyq
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
     * @author wyq
     * 批量导出线索
     */
    @Permissions("crm:leads:excelexport")
    @RequestMapping(value = "/cluesea/batchExportExcel", method = RequestMethod.POST)
    public void clueSeaBatchExportExcel(@RequestParam(name = "ids") String superIds, Long seaId, HttpServletResponse response) throws IOException {
        List<Record> recordList = crmLeadsService.exportPublicSeaClues(seaId, superIds);
        exportPublicSea(recordList, response, "11");
        //renderNull();
    }

    /**
     * @author wyq
     * 导出全部线索
     */
    @Permissions("crm:leads:excelexport")
    @RequestMapping(value = "/allExportExcel", method = RequestMethod.POST)
    public void allExportExcel(BasePageRequest basePageRequest, HttpServletResponse response) throws IOException {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", "1");
        List<Record> recordList = (List<Record>) adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList, response, "1");
        //renderNull();
    }

    /**
     * 导出公海全部线索
     */
    @Permissions("crm:leads:excelexport")
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
     * @param recordList
     * @param response
     * @param label
     * @throws IOException
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
                list.add(record.remove("batch_id", "is_transform", "customer_id", "leads_id", "owner_user_id", "create_user_id", "followup", "field_batch_id").getColumns());
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
     * @param recordList
     * @param response
     * @param label
     * @throws IOException
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
                record.remove("custType", "entId", "intentLevel", "lastCallTime");
                record.remove("user_id", "status", "call_empty_count", "call_success_count", "call_fail_count", "data_source", "intent_level", "last_call_time");
                record.remove("last_called_duration", "pull_status", "status", "super_age", "super_name", "super_sex", "user_get_time", "user_group_id");
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
     * @author wyq
     * 获取线索导入模板
     */
    @LoginFormCookie
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
     * @author wyq
     * 获取线索导入模板
     */
    @LoginFormCookie
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
     * @author wyq
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
     * @author wyq
     * 线索导入
     */
    @Permissions("crm:leads:excelimport")
    @NotNullValidate(value = "ownerUserId", message = "请选择负责人")
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
}
