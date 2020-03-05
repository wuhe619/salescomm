package com.bdaim.crm.erp.crm.controller;

import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.common.annotation.LoginFormCookie;
import com.bdaim.crm.common.annotation.NotNullValidate;
import com.bdaim.crm.common.annotation.Permissions;
import com.bdaim.crm.common.config.paragetter.BasePageRequest;
import com.bdaim.crm.erp.admin.service.AdminFieldService;
import com.bdaim.crm.erp.admin.service.AdminSceneService;
import com.bdaim.crm.erp.crm.entity.CrmProduct;
import com.bdaim.crm.erp.crm.service.CrmProductService;
import com.bdaim.crm.utils.R;
import com.jfinal.log.Log;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.upload.UploadFile;
import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 产品
 */
@RestController
@RequestMapping("/CrmProduct")
public class CrmProductController extends BasicAction {

    @Resource
    private CrmProductService crmProductService;

    @Resource
    private AdminFieldService adminFieldService;

    @Resource
    private AdminSceneService adminSceneService;

    /**
     * @author wyq
     * 查看列表页
     */
    @Permissions({"crm:product:index"})
    @RequestMapping(value = "queryPageList", method = RequestMethod.POST)
    public R queryPageList(@RequestBody JSONObject jsonObject) {
        BasePageRequest<Void> basePageRequest = new BasePageRequest<>(jsonObject.getIntValue("page"), jsonObject.getIntValue("limit"));
        jsonObject.fluentPut("type", 4);
        basePageRequest.setJsonObject(jsonObject);
        return (adminSceneService.filterConditionAndGetPageList(basePageRequest));
    }

    /**
     * 分页条件查询产品
     *
     * @author zxy
     */
    @RequestMapping(value = "queryList", method = RequestMethod.POST)
    public R queryList(BasePageRequest<CrmProduct> basePageRequest) {
        return (R.ok().put("data", crmProductService.queryPage(basePageRequest)));
    }

    /**
     * 添加或修改产品
     *
     * @author zxy
     */
    @Permissions({"crm:product:save", "crm:product:update"})
    @RequestMapping(value = "saveAndUpdate", method = RequestMethod.POST)
    public R saveAndUpdate(@RequestBody JSONObject jsonObject) {
        /*String data = getRawData();
        JSONObject jsonObject = JSON.parseObject(data);*/
        return (crmProductService.saveAndUpdate(jsonObject));
    }

    /**
     * 根据id查询产品
     *
     * @author zxy
     */
    @Permissions("crm:product:read")
    @NotNullValidate(value = "productId", message = "产品id不能为空")
    @RequestMapping(value = "queryById", method = RequestMethod.POST)
    public R queryById(@RequestParam("productId") Integer productId) {
        return (crmProductService.queryById(productId));
    }

    /**
     * 根据id查删除产品
     *
     * @author zxy
     */
    @Permissions("crm:product:delete")
    @NotNullValidate(value = "productId", message = "产品id不能为空")
    @RequestMapping(value = "deleteById", method = RequestMethod.POST)
    public R deleteById(@RequestParam("productId") Integer productId) {
        return (crmProductService.deleteById(productId));
    }

    /**
     * 产品上下架 status 0:下架 1：上架（默认除了0之外其他都是上架）
     *
     * @author zxy
     */
    @Permissions("crm:product:status")
    @RequestMapping(value = "updateStatus", method = RequestMethod.POST)
    public R updateStatus(@RequestParam("ids") String ids, @RequestParam("status") Integer status) {
        if (status == null) {
            status = 1;
        }
        return (crmProductService.updateStatus(ids, status));
    }

    /**
     * @author wyq
     * 批量导出产品
     */
    @Permissions("crm:product:excelexport")
    @RequestMapping(value = "batchExportExcel", method = RequestMethod.POST)
    public void batchExportExcel(@RequestParam("ids") String productIds, HttpServletResponse response) throws IOException {
        List<Record> recordList = crmProductService.exportProduct(productIds);
        export(recordList,response);
        //renderNull();
    }

    /**
     * @author wyq
     * 导出全部产品
     */
    @Permissions("crm:product:excelexport")
    @RequestMapping(value = "allExportExcel", method = RequestMethod.POST)
    public void allExportExcel(BasePageRequest basePageRequest, HttpServletResponse response) throws IOException {
        JSONObject jsonObject = basePageRequest.getJsonObject();
        jsonObject.fluentPut("excel", "yes").fluentPut("type", "4");
        AdminSceneService adminSceneService = new AdminSceneService();
        List<Record> recordList = (List<Record>) adminSceneService.filterConditionAndGetPageList(basePageRequest).get("data");
        export(recordList, response);
        //renderNull();
    }

    private void export(List<Record> recordList, HttpServletResponse response) throws IOException {
        ExcelWriter writer = null;
        try {
            writer = ExcelUtil.getWriter();
            AdminFieldService adminFieldService = new AdminFieldService();
            List<Record> fieldList = adminFieldService.customFieldList("4");
            writer.addHeaderAlias("name", "产品名称");
            writer.addHeaderAlias("num", "产品编码");
            writer.addHeaderAlias("category_name", "产品类别");
            writer.addHeaderAlias("price", "价格");
            writer.addHeaderAlias("description", "产品描述");
            writer.addHeaderAlias("create_user_name", "创建人");
            writer.addHeaderAlias("owner_user_name", "负责人");
            writer.addHeaderAlias("create_time", "创建时间");
            writer.addHeaderAlias("update_time", "更新时间");
            for (Record field : fieldList) {
                writer.addHeaderAlias(field.getStr("name"), field.getStr("name"));
            }
            writer.merge(8 + fieldList.size(), "产品信息");
            List<Map<String, Object>> list = new ArrayList<>();
            for (Record record : recordList) {
                list.add(record.remove("batch_id", "status", "unit", "category_id", "product_id", "owner_user_id", "create_user_id", "field_batch_id", "multi_spec", "using_sn").getColumns());
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
            response.setHeader("Content-Disposition", "attachment;filename=product.xls");
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
     * @author zxy
     * 获取导入模板
     */
    @LoginFormCookie
    @RequestMapping(value = "downloadExcel", method = RequestMethod.POST)
    public void downloadExcel(HttpServletResponse response) {
        List<Record> recordList = adminFieldService.queryAddField(4);
        recordList.removeIf(record -> "file".equals(record.getStr("formType")) || "checkbox".equals(record.getStr("formType")) || "user".equals(record.getStr("formType")) || "structure".equals(record.getStr("formType")));
        HSSFWorkbook wb = new HSSFWorkbook();
        HSSFSheet sheet = wb.createSheet("产品导入表");
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
        titleRow.createCell(0).setCellValue("产品导入模板(*)为必填项");
        cellStyle.setAlignment(HorizontalAlignment.CENTER);
        titleRow.getCell(0).setCellStyle(cellStyle);
        CellRangeAddress region = new CellRangeAddress(0, 0, 0, recordList.size() - 1);
        sheet.addMergedRegion(region);
        List<String> categoryList = Db.query("select name from 72crm_crm_product_category");
        try {
            HSSFRow row = sheet.createRow(1);
            for (int i = 0; i < recordList.size(); i++) {
                Record record = recordList.get(i);
                String[] setting = record.get("setting");
                HSSFCell cell = row.createCell(i);
                if (record.getInt("is_null") == 1) {
                    cell.setCellValue(record.getStr("name") + "(*)");
                } else {
                    cell.setCellValue(record.getStr("name"));
                }
                if ("产品类型".equals(record.getStr("name"))) {
                    setting = categoryList.toArray(new String[categoryList.size()]);
                }
                if (setting.length != 0) {
                    CellRangeAddressList regions = new CellRangeAddressList(2, Integer.MAX_VALUE, i, i);
                    DVConstraint constraint = DVConstraint.createExplicitListConstraint(setting);
                    HSSFDataValidation dataValidation = new HSSFDataValidation(regions, constraint);
                    sheet.addValidationData(dataValidation);
                }
            }
            //HttpServletResponse response = getResponse();
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            response.setCharacterEncoding("UTF-8");
            //test.xls是弹出下载对话框的文件名，不能为中文，中文请自行编码
            response.setHeader("Content-Disposition", "attachment;filename=product_import.xls");
            wb.write(response.getOutputStream());
        } catch (Exception e) {
            Log.getLog(getClass()).error("error:", e);
        } finally {
            try {
                wb.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * @author zxy
     * 导入产品
     */
    @Permissions("crm:product:excelimport")
    @RequestMapping(value = "uploadExcel", method = RequestMethod.POST)
    public R uploadExcel(@RequestParam("file") UploadFile file, @RequestParam("repeatHandling") Integer repeatHandling, @RequestParam("ownerUserId") Integer ownerUserId) {
        //Db.tx(() -> {
        R result = crmProductService.uploadExcel(file, repeatHandling, ownerUserId);
        return (result);
            /*if (result.get("code").equals(500)) {
                return false;
            }
            return true;
        });*/
    }

    /**
     * @author zxy
     * 获取上架商品
     */
    @RequestMapping(value = "queryByStatus", method = RequestMethod.POST)
    public R queryByStatus(BasePageRequest<CrmProduct> basePageRequest) {
        return (crmProductService.queryByStatus(basePageRequest));
    }

}
