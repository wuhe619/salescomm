package com.bdaim.crm.erp.crm.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.LoginFormCookie;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.entity.LkCrmAdminRecordEntity;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.entity.CrmContacts;
import com.bdaim.crm.erp.crm.service.CrmContactsService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.BaseUtil;
import com.bdaim.crm.utils.R;
import com.bdaim.util.JavaBeanUtil;
import com.jfinal.core.paragetter.Para;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Record;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 联系人
 */
@RestController
@RequestMapping("/CrmContacts")
public class CrmContactsController extends BasicAction {

    @Resource
    private CrmContactsService crmContactsService;

    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:contacts:index"})
    @RequestMapping(value = "/queryPageList", method = RequestMethod.POST)
    public R queryPageList(@RequestBody JSONObject jsonObject) {
        jsonObject.fluentPut("type", 3);
        BasePageRequest basePageRequest = new BasePageRequest(jsonObject.getIntValue("page"), jsonObject.getIntValue("limit"));
        basePageRequest.setJsonObject(jsonObject);
        basePageRequest.setJsonObject(jsonObject);
        return (adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * @author wyq
     * 分页条件查询联系人
     */
    @RequestMapping(value = "/queryList", method = RequestMethod.POST)
    public R queryList(BasePageRequest<CrmContacts> basePageRequest, CrmContacts crmContacts) {
        basePageRequest.setData(crmContacts);
        return (R.ok().put("data", crmContactsService.queryList(basePageRequest)));
    }

    /**
     * @author wyq
     * 根据id查询联系人
     */
    @Permissions("crm:contacts:read")
    @RequestMapping(value = "/queryById", method = RequestMethod.POST)
    public R queryById(@Para("contactsId") Integer contactsId) {
        return (R.ok().put("data", crmContactsService.queryById(contactsId)));
    }

    /**
     * @author wyq
     * 根据联系人名称查询
     */
    @RequestMapping(value = "/queryByName", method = RequestMethod.POST)
    public R queryByName(@Para("name") String name) {
        return (R.ok().put("data", crmContactsService.queryByName(name)));
    }

    /**
     * @author wyq
     * 根据联系人id查询商机
     */
    @RequestMapping(value = "/queryBusiness", method = RequestMethod.POST)
    public R queryBusiness(BasePageRequest<CrmContacts> basePageRequest,CrmContacts crmContacts) {
        basePageRequest.setData(crmContacts);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTACTS_TYPE_KEY.getSign()), basePageRequest.getData().getContactsId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmContactsService.queryBusiness(basePageRequest));
    }

    /**
     * @author wyq
     * 联系人关联商机
     */
    @RequestMapping(value = "/relateBusiness", method = RequestMethod.POST)
    public R relateBusiness(@Para("contactsId") Integer contactsId, @Para("businessIds") String businessIds) {
        return (crmContactsService.relateBusiness(contactsId, businessIds));
    }

    /**
     * @author wyq
     * 联系人解除关联商机
     */
    @RequestMapping(value = "/unrelateBusiness", method = RequestMethod.POST)
    public R unrelateBusiness(@Para("contactsId") Integer contactsId, @Para("businessIds") String businessIds) {
        return (crmContactsService.unrelateBusiness(contactsId, businessIds));
    }

    /**
     * @author wyq
     * 新建或更新联系人
     */
    @Permissions({"crm:contacts:save", "crm:contacts:update"})
    @RequestMapping(value = "/addOrUpdate", method = RequestMethod.POST)
    public R addOrUpdate(@RequestBody JSONObject jsonObject) {
        // JSONObject jsonObject = JSON.parseObject(getRawData());
        return (crmContactsService.addOrUpdate(jsonObject));
    }

    /**
     * 批量添加联系人
     *
     * @param jsonArray
     * @return
     */
    @Permissions({"crm:contacts:batchAddContacts"})
    @RequestMapping(value = "/batchAddContacts", method = RequestMethod.POST)
    public R batchAddContacts(@org.springframework.web.bind.annotation.RequestBody JSONArray jsonArray) {
        // JSONObject jsonObject = JSON.parseObject(getRawData());
        return (crmContactsService.batchAddContacts(jsonArray));
    }

    /**
     * @author wyq
     * 根据id删除联系人
     */
    @Permissions("crm:contacts:delete")
    @RequestMapping(value = "/deleteByIds", method = RequestMethod.POST)
    public R deleteByIds(@Para("contactsIds") String contactsIds) {
        return (crmContactsService.deleteByIds(contactsIds));
    }

    /**
     * @author wyq
     * 联系人转移
     */
    @Permissions("crm:contacts:transfer")
    @NotNullValidate(value = "contactsIds", message = "联系人id不能为空")
    @NotNullValidate(value = "newOwnerUserId", message = "新负责人不能为空")
    @RequestMapping(value = "/transfer", method = RequestMethod.POST)
    public R transfer(@Para("") CrmContacts crmContacts) {
        return (crmContactsService.transfer(crmContacts));
    }


    /**
     * @author wyq
     * 添加跟进记录
     */
    @NotNullValidate(value = "typesId", message = "联系人id不能为空")
    @NotNullValidate(value = "content", message = "内容不能为空")
    @NotNullValidate(value = "category", message = "跟进类型不能为空")
    @RequestMapping(value = "/addRecord", method = RequestMethod.POST)
    public R addRecord(@Para("") LkCrmAdminRecordEntity adminRecord) {
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTACTS_TYPE_KEY.getSign()), adminRecord.getTypesId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (crmContactsService.addRecord(adminRecord));
    }

    /**
     * @author wyq
     * 查看跟进记录
     */
    @RequestMapping(value = "/getRecord", method = RequestMethod.POST)
    public R getRecord(BasePageRequest<CrmContacts> basePageRequest, CrmContacts crmContacts) {
        basePageRequest.setData(crmContacts);
        boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.CONTACTS_TYPE_KEY.getSign()), basePageRequest.getData().getContactsId());
        if (auth) {
            return (R.noAuth());
            //return;
        }
        return (R.ok().put("data", JavaBeanUtil.recordToMap(crmContactsService.getRecord(basePageRequest))));
    }

    /**
     * @author wyq
     * 批量导出线索
     */
    @Permissions("crm:contacts:excelexport")
    @RequestMapping(value = "/batchExportExcel")
    public void batchExportExcel(@RequestParam("ids") String contactsIds, HttpServletResponse response) throws IOException {
        List<Record> recordList = crmContactsService.exportContacts(contactsIds);
        export(recordList, response);
        //renderNull();
    }

    /**
     * @author wyq
     * 导出全部联系人
     */
    @Permissions("crm:contacts:excelexport")
    @RequestMapping(value = "/allExportExcel")
    public void allExportExcel(@RequestBody BasePageRequest basePageRequest, @RequestBody JSONObject jsonObject, HttpServletResponse response) throws IOException {
        //JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", "3");
        basePageRequest.setJsonObject(jsonObject);
        AdminSceneService adminSceneService = new AdminSceneService();
        List<Record> recordList = (List<Record>) adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList, response);
        //renderNull();
    }

    @RequestMapping(value = "/export")
    private void export(List<Record> recordList, HttpServletResponse response) throws IOException {
        ExcelWriter writer = null;
        try {
            writer = ExcelUtil.getWriter();
            //AdminFieldService adminFieldService = new AdminFieldService();
            List<Record> fieldList = adminFieldService.customFieldList("3");
            writer.addHeaderAlias("name", "姓名");
            writer.addHeaderAlias("customer_name", "客户名称");
            writer.addHeaderAlias("next_time", "下次联系时间");
            writer.addHeaderAlias("telephone", "电话");
            writer.addHeaderAlias("mobile", "手机号");
            writer.addHeaderAlias("email", "电子邮箱");
            writer.addHeaderAlias("post", "职务");
            writer.addHeaderAlias("address", "地址");
            writer.addHeaderAlias("remark", "备注");
            writer.addHeaderAlias("create_user_name", "创建人");
            writer.addHeaderAlias("owner_user_name", "负责人");
            writer.addHeaderAlias("create_time", "创建时间");
            writer.addHeaderAlias("update_time", "更新时间");
            for (Record field : fieldList) {
                writer.addHeaderAlias(field.getStr("name"), field.getStr("name"));
            }
            writer.merge(12 + fieldList.size(), "联系人信息");
            List<Map<String, Object>> list = new ArrayList<>();
            for (Record record : recordList) {
                list.add(record.remove("batch_id", "contacts_name", "customer_id", "contacts_id", "owner_user_id", "create_user_id", "field_batch_id").getColumns());
            }
            writer.write(list, true);
            writer.setRowHeight(0, 20);
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
            response.setHeader("Content-Disposition", "attachment;filename=contacts.xls");
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
     * 获取联系人导入模板
     */
    @LoginFormCookie
    @RequestMapping(value = "/downloadExcel")
    public void downloadExcel(HttpServletResponse response) {
        List<Record> recordList = adminFieldService.queryAddField(3);
        recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("联系人导入表");
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
        titleRow.createCell(0).setCellValue("联系人导入模板(*)为必填项");
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
            response.setHeader("Content-Disposition", "attachment;filename=contacts_import.xls");
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
        //renderNull();
    }


   /* public void uploadExcel(@Para("file") UploadFile file, @Para("repeatHandling") Integer repeatHandling, @Para("ownerUserId") Integer ownerUserId) {
        Db.tx(() -> {
            R result = crmContactsService.uploadExcel(file, repeatHandling, ownerUserId);
            renderJson(result);
            if (result.get("code").equals(500)) {
                return false;
            }
            return true;
        });
    }*/

    /**
     * 联系人导入
     */
    @Permissions("crm:contacts:excelimport")
    @NotNullValidate(value = "ownerUserId", message = "请选择负责人")
    @RequestMapping(value = "/uploadExcel", method = RequestMethod.POST)
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
        R result = crmContactsService.uploadExcel(file, repeatHandling, ownerUserId);
        return (result);
        //return !result.get("code").equals(500);
        //});
    }
}
