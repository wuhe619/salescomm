package com.bdaim.crm.erp.crm.controller;

import cn.hutool.core.util.StrUtil;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.common.annotation.LoginFormCookie;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.annotation.RequestBody;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.entity.LkCrmAdminRecordEntity;
import com.bdaim.crm.entity.LkCrmCustomerEntity;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.entity.CrmBusiness;
import com.bdaim.crm.erp.crm.entity.CrmContract;
import com.bdaim.crm.erp.crm.entity.CrmCustomer;
import com.bdaim.crm.erp.crm.service.CrmBusinessService;
import com.bdaim.crm.erp.crm.service.CrmContactsService;
import com.bdaim.crm.erp.crm.service.CrmContractService;
import com.bdaim.crm.erp.crm.service.CrmCustomerService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import com.jfinal.kit.Kv;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.upload.UploadFile;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/CrmCustomer")
public class CrmCustomerController extends Controller {

    @Resource
    private CrmCustomerService crmCustomerService;

    @Resource
    private CrmContactsService crmContactsService;//联系人

    @Resource
    private CrmBusinessService crmBusinessService;//商机

    @Resource
    private CrmContractService crmContractService;//合同

    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:customer:index"})
    @RequestMapping(value = "/queryPageList", method = RequestMethod.POST)
    public R queryPageList(BasePageRequest basePageRequest) {
        JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type", 2);
        basePageRequest.setJsonObject(jsonObject);
        return(adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * @author wyq
     * 查看公海列表页
     */
    @Permissions({"crm:pool:index"})
    @RequestMapping(value = "/queryPoolPageList", method = RequestMethod.POST)
    public R queryPoolPageList(BasePageRequest basePageRequest,@RequestBody JSONObject jsonObject) {
        //JSONObject jsonObject = basePageRequest.getJsonObject().fluentPut("type", 8);
        basePageRequest.setJsonObject(jsonObject);
        return (adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * @author wyq
     * 全局搜索查询客户
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public R queryList(BasePageRequest<CrmCustomer> basePageRequest,CrmCustomer crmCustomer) {
        basePageRequest.setData(crmCustomer);
        return(R.ok().put("data", crmCustomerService.getCustomerPageList(basePageRequest)));
    }

    /**
     * @author wyq
     * 新增或更新客户
     */
    @Permissions({"crm:customer:save", "crm:customer:update"})
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public R addOrUpdate(@org.springframework.web.bind.annotation.RequestBody JSONObject jsonObject) {
        //JSONObject jsonObject = JSON.parseObject(getRawData());
        return(crmCustomerService.addOrUpdate(jsonObject, "noImport"));
    }

    /**
     * @author wyq
     * 根据客户id查询
     */
    @Permissions("crm:customer:read")
    @NotNullValidate(value = "customerId", message = "客户id不能为空")
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public R queryById(@Para("customerId") Integer customerId) {
        return (R.ok().put("data", crmCustomerService.queryById(customerId)));
    }

    /**
     * @author wyq
     * 根据客户名称查询
     */
    @NotNullValidate(value = "name", message = "客户名称不能为空")
    @RequestMapping(value = "/queryByName", method = RequestMethod.POST)
    public R queryByName(@Para("name") String name) {
        return (R.ok().put("data", crmCustomerService.queryByName(name)));
    }

    /**
     * @author wyq
     * 根据客户id查询联系人
     */
    @RequestMapping(value = "/queryContacts", method = RequestMethod.POST)
    public R queryContacts(BasePageRequest<CrmCustomer> basePageRequest, CrmCustomer crmCustomer) {
        basePageRequest.setData(crmCustomer);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()), basePageRequest.getData().getCustomerId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmCustomerService.queryContacts(basePageRequest));
    }

    /**
     * @author wyq
     * 根据id删除客户
     */
    @Permissions("crm:customer:delete")
    @NotNullValidate(value = "customerIds", message = "客户id不能为空")
    @RequestMapping(value = "/deleteByIds", method = RequestMethod.POST)
    public R deleteByIds(@Para("customerIds") String customerIds) {
        return (crmCustomerService.deleteByIds(customerIds));
    }

    /**
     * @author wyq
     * 根据客户id查找商机
     */
    @RequestMapping(value = "/queryBusiness", method = RequestMethod.POST)
    public R queryBusiness(BasePageRequest<CrmCustomer> basePageRequest) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()), basePageRequest.getData().getCustomerId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmCustomerService.queryBusiness(basePageRequest));
    }

    /**
     * @author wyq
     * 根据客户id查询合同
     */
    @RequestMapping(value = "/queryContract", method = RequestMethod.POST)
    public R queryContract(BasePageRequest<CrmCustomer> basePageRequest, CrmCustomer crmCustomer, @RequestBody JSONObject JSONObject) {
        basePageRequest.setData(crmCustomer);
        basePageRequest.setJsonObject(JSONObject);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()), basePageRequest.getData().getCustomerId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmCustomerService.queryContract(basePageRequest));
    }

    /**
     * @author zxy
     * 条件查询客户公海
     */
    @RequestMapping(value = "/queryPageGH", method = RequestMethod.POST)
    public R queryPageGH(BasePageRequest basePageRequest) {
        return (R.ok().put("data", crmCustomerService.queryPageGH(basePageRequest)));
    }

    /**
     * @author wyq
     * 根据客户id查询回款计划
     */
    @RequestMapping(value = "/queryReceivablesPlan", method = RequestMethod.POST)
    public R queryReceivablesPlan(BasePageRequest<CrmCustomer> basePageRequest, CrmCustomer crmCustomer) {
        basePageRequest.setData(crmCustomer);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()), basePageRequest.getData().getCustomerId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmCustomerService.queryReceivablesPlan(basePageRequest));
    }

    /**
     * @author zxy
     * 根据客户id查询回款
     */
    @RequestMapping(value = "/queryReceivables", method = RequestMethod.POST)
    public R queryReceivables(BasePageRequest<CrmCustomer> basePageRequest, CrmCustomer crmCustomer) {
        basePageRequest.setData(crmCustomer);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()), basePageRequest.getData().getCustomerId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmCustomerService.queryReceivables(basePageRequest));
    }

    /**
     * @author wyq
     * 客户锁定
     */
    @Permissions("crm:customer:lock")
    @NotNullValidate(value = "ids", message = "客户id不能为空")
    @NotNullValidate(value = "isLock", message = "锁定状态不能为空")
    @RequestMapping(value = "/lock", method = RequestMethod.POST)
    public R lock(@Para("") CrmCustomer crmCustomer) {
        return (crmCustomerService.lock(crmCustomer));
    }

    /**
     * 客户转移
     *
     * @author wyq
     */
    @Permissions("crm:customer:transfer")
    @NotNullValidate(value = "customerIds", message = "客户id不能为空")
    @NotNullValidate(value = "newOwnerUserId", message = "新负责人不能为空")
    @NotNullValidate(value = "transferType", message = "移除方式不能为空")
    @Before(Tx.class)
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public R transfer(@Para("") LkCrmCustomerEntity crmCustomer) {
        String[] customerIdsArr = crmCustomer.getCustomerIds().split(",");
        for (String customerId : customerIdsArr) {
            crmCustomer.setCustomerId(Integer.valueOf(customerId));
            renderJson(crmCustomerService.updateOwnerUserId(crmCustomer));
            String changeType = crmCustomer.getChangeType();
            if (StrUtil.isNotEmpty(changeType)) {
                String[] changeTypeArr = changeType.split(",");
                for (String type : changeTypeArr) {
                    if ("1".equals(type)) {//更新联系人负责人
                        return (crmContactsService.updateOwnerUserId(crmCustomer.getCustomerId(), crmCustomer.getNewOwnerUserId()) ? R.ok() : R.error());
                    }
                    if ("2".equals(type)) {//更新商机负责人
                        return (crmBusinessService.updateOwnerUserId(crmCustomer));
                    }
                    if ("3".equals(type)) {//更新合同负责人
                        return (crmContractService.updateOwnerUserId(crmCustomer));
                    }
                }
            }
        }
        return (R.ok());
    }

    /**
     * @author wyq
     * 查询团队成员
     */
    @NotNullValidate(value = "customerId", message = "客户id不能为空")
    @RequestMapping(value = "/getMembers", method = RequestMethod.POST)
    public R getMembers(@Para("customerId") Integer customerId) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()), customerId);
        if (auth) {
            return (R.noAuth());
            // return;
        }
        return (R.ok().put("data", crmCustomerService.getMembers(customerId)));
    }

    /**
     * @author wyq
     * 添加团队成员
     */
    @Permissions("crm:customer:teamsave")
    @NotNullValidate(value = "ids", message = "客户id不能为空")
    @NotNullValidate(value = "memberIds", message = "成员id不能为空")
    @NotNullValidate(value = "power", message = "读写权限不能为空")
    @Before(Tx.class)
    @RequestMapping(value = "/addMembers", method = RequestMethod.POST)
    public R addMembers(@Para("") CrmCustomer crmCustomer) {
        String changeType = crmCustomer.getChangeType();
        if (StrUtil.isNotEmpty(changeType)) {
            String[] changeTypeArr = changeType.split(",");
            for (String type : changeTypeArr) {
                if ("2".equals(type)) {//更新商机
                    CrmBusiness crmBusiness = new CrmBusiness();
                    crmBusiness.setIds(crmCustomerService.getBusinessIdsByCustomerIds(crmCustomer.getIds()));
                    crmBusiness.setMemberIds(crmCustomer.getMemberIds());
                    crmBusiness.setPower(crmCustomer.getPower());
                    crmBusiness.setTransferType(crmCustomer.getTransferType());
                    crmBusinessService.addMember(crmBusiness);
                }
                if ("3".equals(type)) {//更新合同
                    CrmContract crmContract = new CrmContract();
                    crmContract.setIds(crmCustomerService.getContractIdsByCustomerIds(crmCustomer.getIds()));
                    crmContract.setMemberIds(crmCustomer.getMemberIds());
                    crmContract.setPower(crmCustomer.getPower());
                    crmContract.setTransferType(crmCustomer.getTransferType());
                    crmCustomerService.addMember(crmCustomer);
                }
            }
            crmCustomerService.addMember(crmCustomer);
        }
        return (crmCustomerService.addMember(crmCustomer));
    }

    /**
     * @author wyq
     * 编辑团队成员
     */
    @NotNullValidate(value = "ids", message = "商机id不能为空")
    @NotNullValidate(value = "memberIds", message = "成员id不能为空")
    @NotNullValidate(value = "power", message = "读写权限不能为空")
    @RequestMapping(value = "/updateMembers", method = RequestMethod.POST)
    public R updateMembers(@Para("") CrmCustomer crmCustomer) {
        return (crmCustomerService.addMember(crmCustomer));
    }

    /**
     * @author wyq
     * 删除团队成员
     */
    @NotNullValidate(value = "ids", message = "客户id不能为空")
    @NotNullValidate(value = "memberIds", message = "成员id不能为空")
    @RequestMapping(value = "/deleteMembers", method = RequestMethod.POST)
    public R deleteMembers(@Para("") CrmCustomer crmCustomer) {
        return (crmCustomerService.deleteMembers(crmCustomer));
    }

    /**
     * @author zxy
     * 客户保护规则设置
     */
    @Permissions("manage:crm")
    @NotNullValidate(value = "followupDay", message = "跟进天数不能为空")
    @NotNullValidate(value = "dealDay", message = "成交天数不能为空")
    @NotNullValidate(value = "type", message = "启用状态不能为空")
    @RequestMapping(value = "/updateRulesSetting", method = RequestMethod.POST)
    public R updateRulesSetting(Integer followupDay, Integer dealDay, Integer type) {
        //跟进天数
        //Integer followupDay = getParaToInt("followupDay");
        //成交天数
        //Integer dealDay = getParaToInt("dealDay");
        //启用状态
        //Integer type = getParaToInt("type");
        return (crmCustomerService.updateRulesSetting(dealDay, followupDay, type));
    }

    /**
     * @author zxy
     * 获取客户保护规则设置
     */
    @RequestMapping(value = "/getRulesSetting", method = RequestMethod.POST)
    public R getRulesSetting() {
        return (crmCustomerService.getRulesSetting());
    }

    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId", message = "客户id不能为空")
    @NotNullValidate(value = "content", message = "内容不能为空")
    @NotNullValidate(value = "category", message = "跟进类型不能为空")
    @RequestMapping(value = "/addRecord", method = RequestMethod.POST)
    public R addRecord(@Para("") LkCrmAdminRecordEntity adminRecord) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()), adminRecord.getTypesId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmCustomerService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @RequestMapping(value = "/getRecord", method = RequestMethod.POST)
    public R getRecord(BasePageRequest<CrmCustomer> basePageRequest, CrmCustomer crmCustomer) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CUSTOMER_TYPE_KEY.getSign()), basePageRequest.getData().getCustomerId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        basePageRequest.setData(crmCustomer);
        return (R.ok().put("data", crmCustomerService.getRecord(basePageRequest)));
    }

    /**
     * @author wyq
     * 客户批量导出
     */
    @Permissions("crm:customer:excelexport")
    @RequestMapping(value = "/batchExportExcel")
    public void batchExportExcel(@Para("ids") String customerIds) throws IOException {
        List<Record> recordList = crmCustomerService.exportCustomer(customerIds);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 全部导出
     */
    @Permissions("crm:customer:excelexport")
    @RequestMapping(value = "/allExportExcel")
    public void allExportExcel(BasePageRequest basePageRequest, @RequestBody JSONObject jsonObject) throws IOException {
        //JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", 2);
        basePageRequest.setJsonObject(jsonObject);
        //AdminSceneService adminSceneService = new AdminSceneService();
        List<Record> recordList = (List<Record>) adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 公海批量导出
     */
    @Permissions("crm:pool:excelexport")
    @RequestMapping(value = "/poolBatchExportExcel")
    public void poolBatchExportExcel(@Para("ids") String customerIds) throws IOException {
        List<Record> recordList = crmCustomerService.exportCustomer(customerIds);
        export(recordList);
        renderNull();
    }

    /**
     * @author wyq
     * 公海全部导出
     */
    @Permissions("crm:pool:excelexport")
    @RequestMapping(value = "/poolAllExportExcel")
    public void poolAllExportExcel(BasePageRequest basePageRequest, @RequestBody JSONObject jsonObject) throws IOException {
        //JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", 8);
        //AdminSceneService adminSceneService = new AdminSceneService();
        basePageRequest.setJsonObject(jsonObject);
        List<Record> recordList = (List<Record>) adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList);
        //renderNull();
    }

    @RequestMapping(value = "/export")
    private void export(List<Record> recordList) throws IOException {
        ExcelWriter writer = null;
        try {
            writer = ExcelUtil.getWriter();
            //AdminFieldService adminFieldService = new AdminFieldService();
            List<Record> fieldList = adminFieldService.customFieldList("2");
            List<Record> customerFields = adminFieldService.list("2");
            Kv kv = new Kv();
            customerFields.forEach(customerField -> kv.set(customerField.getStr("field_name"), customerField.getStr("name")));
            writer.addHeaderAlias("customer_name", kv.getStr("customer_name"));
            writer.addHeaderAlias("telephone", kv.getStr("telephone"));
            writer.addHeaderAlias("mobile", kv.getStr("mobile"));
            writer.addHeaderAlias("website", kv.getStr("website"));
            writer.addHeaderAlias("next_time", kv.getStr("next_time"));
            writer.addHeaderAlias("deal_status", kv.getStr("deal_status"));
            writer.addHeaderAlias("create_user_name", "创建人");
            writer.addHeaderAlias("owner_user_name", "负责人");
            writer.addHeaderAlias("address", "省市区");
            writer.addHeaderAlias("location", "定位信息");
            writer.addHeaderAlias("detail_address", "详细地址");
            writer.addHeaderAlias("lng", "地理位置经度");
            writer.addHeaderAlias("lat", "地理位置维度");
            writer.addHeaderAlias("create_time", "创建时间");
            writer.addHeaderAlias("update_time", "更新时间");
            writer.addHeaderAlias("remark", kv.getStr("remark"));
            for (Record field : fieldList) {
                writer.addHeaderAlias(field.getStr("name"), field.getStr("name"));
            }
            writer.merge(fieldList.size() + 15, "客户信息");
            HttpServletResponse response = getResponse();
            List<Map<String, Object>> list = new ArrayList<>();
            for (Record record : recordList) {
                list.add(record.remove("batch_id", "create_user_id", "customer_id", "is_lock", "owner_user_id", "ro_user_id", "rw_user_id", "followup", "field_batch_id").getColumns());
            }
            writer.write(list, true);
            writer.setRowHeight(0, 20);
            writer.setRowHeight(1, 20);
            for (int i = 0; i < fieldList.size() + 16; i++) {
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
            response.setHeader("Content-Disposition", "attachment;filename=customer.xls");
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
     * 客户放入公海
     *
     * @author zxy
     */
    @Permissions("crm:customer:putinpool")
    @RequestMapping(value = "/updateCustomerByIds", method = RequestMethod.POST)
    public R updateCustomerByIds(String ids) {
        //String ids = get("ids");
        return (crmCustomerService.updateCustomerByIds(ids));
    }

    /**
     * 领取或分配客户
     *
     * @author zxy
     */
    @Permissions("crm:customer:distribute")
    @RequestMapping(value = "/getCustomersByIds", method = RequestMethod.POST)
    public R getCustomersByIds(String ids, Long userId) {
        return (crmCustomerService.getCustomersByIds(ids, userId));
    }

    /**
     * 公海分配客户
     *
     * @author zxy
     */
    @Permissions("crm:pool:distribute")
    @RequestMapping(value = "/distributeByIds", method = RequestMethod.POST)
    public R distributeByIds(String ids, Long userId) {
        /*String ids = get("ids");
        Long userId = getLong("userId");*/
        return (crmCustomerService.getCustomersByIds(ids, userId));
    }

    /**
     * 公海领取客户
     *
     * @author zxy
     */
    @Permissions("crm:pool:receive")
    @RequestMapping(value = "/receiveByIds", method = RequestMethod.POST)
    public R receiveByIds(String ids, Long userId) {
        /*String ids = get("ids");
        Long userId = getLong("userId");*/
        return (crmCustomerService.getCustomersByIds(ids, userId));
    }

    /**
     * @author wyq
     * 获取导入模板
     */
    @LoginFormCookie
    @RequestMapping(value = "/downloadExcel")
    public void downloadExcel() {
        List<Record> recordList = adminFieldService.queryAddField(2);
        recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("客户导入表");
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
        titleRow.createCell(0).setCellValue("客户导入模板(*)为必填项");
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(0).setCellStyle(cellStyle);
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, recordList.size() - 1);
        sheet.addMergedRegion(region);
        try {
            HSSFRow row = sheet.createRow(1);
            for (int i = 0; i < recordList.size(); i++) {
                Record record = recordList.get(i);
                if ("map_address".equals(record.getStr("field_name"))) {
                    record.set("name", "详细地址").set("setting", new String[]{});
                }
                String[] setting = record.get("setting");
                HSSFCell cell = row.createCell(i);
                if (record.getInt("is_null") == 1) {
                    cell.setCellValue(record.getStr("name") + "(*)");
                } else {
                    cell.setCellValue(record.getStr("name"));
                }
                if (setting != null && setting.length != 0) {
                    CellRangeAddressList regions = new CellRangeAddressList(2, Integer.MAX_VALUE, i, i);
                    DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                    HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                    sheet.addValidationData(dataValidation);
                }
            }
            HttpServletResponse response = getResponse();

            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=customer_import.xls");
            wb.write(response.getOutputStream());

        } catch (Exception e) {
            Log.getLog(getClass()).error("error", e);
        } finally {
            try {
                wb.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }

        }
        renderNull();
    }

    /**
     * @author wyq
     * 导入客户
     */
    @Permissions("crm:customer:excelimport")
    @NotNullValidate(value = "ownerUserId", message = "请选择负责人")
    @RequestMapping(value = "/uploadExcel", method = RequestMethod.POST)
    public void uploadExcel(@Para("file") UploadFile file, @Para("repeatHandling") Integer repeatHandling, @Para("ownerUserId") Integer ownerUserId) {
        Db.tx(() -> {
            R result = crmCustomerService.uploadExcel(file, repeatHandling, ownerUserId);
            renderJson(result);
            if (result.get("code").equals(500)) {
                return false;
            }
            return true;
        });
    }
}
