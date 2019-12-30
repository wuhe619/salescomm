package com.bdaim.customs.services;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.customs.dto.excel.IdCardNoVerify;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.PartyDan;
import com.bdaim.customs.utils.ServiceUtils;
import com.bdaim.util.FileUrlEntity;
import com.bdaim.util.excel.EasyExcelUtil;

import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/19
 * @description
 */
@Service
@Transactional
public class ExportExcelService {

    private final static Logger LOG = LoggerFactory.getLogger(ExportExcelService.class);

   /* private Map<String, String> exportTemplate = new HashMap() {{
        put("_export_low_product", "_export_low_product.xlsx");
        put("_export_estimated_tax", "_export_estimated_tax.xlsx");
        put("_export_declaration_form", "_export_declaration_form.xlsx");
        put("_export_tally_form", "_export_tally_form.xlsx");
        put("_export_bgd_z_main_data", "_export_bgd_z_main_data.xls");
    }};*/

//    @Autowired
//    private HBusiDataManagerDao hBusiDataManagerDao;

    @Autowired
    private FileUrlEntity fileUrlEntity;

    @Autowired
    private ServiceUtils serviceUtils;

    private void export(String templatePath, Map<String, Object> map, String[] sheetName, HttpServletResponse response) throws IOException {
        // 加载模板
        TemplateExportParams params = new TemplateExportParams(templatePath, true);
        if (sheetName != null && sheetName.length > 0) {
            params.setSheetName(sheetName);
        }
        try {
            List list = (List) map.get("list");
            LOG.info("导出sheet1行数:{}", list != null ? list.size() : 0);
            List list1 = (List) map.get("list1");
            LOG.info("导出sheet2行数:{}", list1 != null ? list1.size() : 0);
            List list2 = (List) map.get("list2");
            LOG.info("导出sheet3行数:{}", list2 != null ? list2.size() : 0);
        } catch (Exception e) {
            e.printStackTrace();
        }
        //写入本地临时文件中
        //String fileName = "/tmp/" + System.currentTimeMillis() + templatePath.substring(templatePath.lastIndexOf(File.separator) + 1);
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);
        workbook.write(response.getOutputStream());
        response.getOutputStream().flush();
        response.getOutputStream().close();

        /*FileOutputStream fos = new FileOutputStream(fileName);
        workbook.write(fos);
        fos.close();
        LOG.info("导出excel:{}本地磁盘写入成功", fileName);
        InputStream in = new BufferedInputStream(new FileInputStream(fileName), 4096);
        OutputStream os = new BufferedOutputStream(response.getOutputStream());
        byte[] bytes = new byte[4096];
        int i = 0;
        while ((i = in.read(bytes)) > 0) {
            os.write(bytes, 0, i);
        }
        os.flush();
        os.close();
        in.close();
        LOG.info("导出excel:{}完成", fileName);
        File file = new File(fileName);
        boolean delete = file.delete();
        LOG.info("删除excel:{}状态", delete);*/
    }

    public void exportExcel(int id, List<JSONObject> list, JSONObject param, HttpServletResponse response) throws IllegalAccessException, IOException {
        if ("_export_v_photo".equals(param.getString("_rule_"))
                || "_export_v_nopass".equals(param.getString("_rule_"))) {
            exportIdCardExcel(id, param.getString("_rule_"), response);
            return;
        }
        if (list != null && id == 0 && ("sub".equals(param.getString("type")) || "res".equals(param.getString("type"))
                || "extension".equals(param.getString("type")))) {
            exportExcelResourceLog(list, param, response);
        }
        if (list != null) {
            exportExcelByTemplate(list, param, response);
        }

    }

    private void exportExcelResourceLog(List<JSONObject> list, JSONObject param, HttpServletResponse response) throws IllegalAccessException, IOException {
        // 生成workbook 并导出
        String classPath = fileUrlEntity.getFileUrl();
        String pathF = File.separator;
        classPath = classPath.replace("/", pathF);
        String templatePath = classPath + pathF + "tp" + pathF + param.getString("_rule_") + ".xlsx";
        //String templatePath = "tp/" + param.getString("_rule_") + ".xlsx";
        File file = new File(templatePath);
        LOG.info("excel模板文件路径:{},文件状态:{}", file.getPath(), file.exists());
        LOG.info("开始导出excel:{}", templatePath);
        Map<String, Object> map = new HashMap<>();
        //map.put("list", JavaBeanUtil.convertJsonObjectToMapList(list));
        map.put("list", list);
        if (list == null || list.size() == 0) {
            LOG.info("导出excel为空:{}", param);
            return;
        }
        response.setHeader("Content-Disposition", "attachment; filename=" + System.currentTimeMillis() + ExcelTypeEnum.XLSX.getValue());
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        switch (param.getString("type")) {
            case "sub":
            case "res":
                export(templatePath, map, null, response);
                break;
            case "extension":
                export(templatePath, map, null, response);
                break;

        }
    }

    private void exportExcelByTemplate(List<JSONObject> list, JSONObject param, HttpServletResponse response) throws IllegalAccessException, IOException {
        // 生成workbook 并导出
        String classPath = fileUrlEntity.getFileUrl();
        String pathF = File.separator;
        classPath = classPath.replace("/", pathF);
        String templatePath = classPath + pathF + "tp" + pathF + param.getString("_rule_") + ".xlsx";
        //String templatePath = "tp/" + param.getString("_rule_") + ".xlsx";
        File file = new File(templatePath);
        LOG.info("excel模板文件路径:{},文件状态:{}", file.getPath(), file.exists());
        LOG.info("开始导出excel:{}", templatePath);
        Map<String, Object> map = new HashMap<>();
        //map.put("list", JavaBeanUtil.convertJsonObjectToMapList(list));
        map.put("list", list);
        if (list == null || list.size() == 0) {
            LOG.info("导出excel为空:{}", param);
            throw new IOException();
        }

        response.setHeader("Content-Disposition", "attachment; filename=" + System.currentTimeMillis() + ExcelTypeEnum.XLSX.getValue());
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        switch (param.getString("_rule_")) {
            // 舱单导出txt
            //case "_export_cd_z_main_data":
            // 导出低价商品单,导出预估税单
            case "_export_low_product":
            case "_export_estimated_tax":
                export(templatePath, map, null, response);
                break;
            // 导出报检单,导出理货单
            case "_export_declaration_form":
            case "_export_tally_form":
                String[] sheetName = new String[]{"主单", "分单", "税单"};
                generateMainDan(list, map);
                export(templatePath, map, sheetName, response);
                break;
            case "_export_bgd_z_main_data_inspection":
                sheetName = new String[]{"查验报关单表头", "查验报关单商品"};
                generateMainDan(list, map);
//                List list1 = (List) map.get("list1");
//                if (list1.size() == 0) throw new IOException();
                export(templatePath, map, sheetName, response);
                break;
            case "_export_bgd_z_main_data":
                sheetName = new String[]{"报关单表头", "报关单商品"};
                generateMainDan(list, map);
                export(templatePath, map, sheetName, response);
                break;
            case "_export_verification_result":
                sheetName = new String[]{"身份核验结果"};
                generateMainDan(list, map);
                export(templatePath, map, sheetName, response);
                break;
            default:
                sheetName = new String[]{"主单", "分单", "税单"};
                generateMainDan(list, map);
                if (map.get("list1") != null) {
                    List lis = (List) map.get("list1");
                    if (lis.size() == 0) {
                        map.put("list", null);
                    }
                }
                export(templatePath, map, sheetName, response);
                break;
        }
    }

    private void generateMainDan0(List<JSONObject> list, Map<String, Object> map) {
        if (list != null && list.size() > 0) {
            List<Map<String, Object>> list_one = new ArrayList();
            List<Map<String, Object>> list_two = new ArrayList();
            Map<String, Object> fdData, ssData;
            JSONArray fdList, ssList;
            int index = 1, fIndex = 1, sIndex = 1;
            for (JSONObject m : list) {
                if (!m.containsKey("index")) {
                    m.put("index", index);
                }
                index++;
                fdList = m.getJSONArray("singles");
                if (fdList == null || fdList.size() == 0) {
                    continue;
                }
                // 处理分单
                for (int i = 0; i < fdList.size(); i++) {
                    fdData = new HashMap<>();
                    fdData.put("index", fIndex);
                    fIndex++;
                    fdData.putAll(fdList.getJSONObject(i));
                    list_one.add(fdData);
                    if (fdList.getJSONObject(i) == null) {
                        continue;
                    }
                    ssList = fdList.getJSONObject(i).getJSONArray("products");
                    // 处理商品
                    for (int j = 0; j < ssList.size(); j++) {
                        ssData = new HashMap<>();
                        ssData.putAll(ssList.getJSONObject(j));
                        ssData.put("index", sIndex);
                        sIndex++;
                        list_two.add(ssData);
                    }
                }
            }
            map.put("list1", list_one);
            map.put("list2", list_two);
        }
    }

    private void generateMainDan(List<JSONObject> list, Map<String, Object> map) throws IOException {
        if (list != null && list.size() > 0) {
            List<Map<String, Object>> list_one = new ArrayList();
            List<Map<String, Object>> list_two = new ArrayList();
            Map<String, Object> fdData, ssData;
            JSONArray fdList, ssList;
            int index = 1, fIndex = 1, sIndex = 1;
            for (JSONObject m : list) {
                if (!m.containsKey("index")) {
                    m.put("index", index);
                }
                index++;
                fdList = m.getJSONArray("singles");
                if (fdList == null || fdList.size() == 0) {
                    continue;
                }

                // 处理分单
                for (int i = 0; i < fdList.size(); i++) {
                    fdData = new HashMap<>();
                    fdData.put("index", fIndex);
                    fIndex++;
                    fdData.putAll(fdList.getJSONObject(i));
                    list_one.add(fdData);
                    if (fdList.getJSONObject(i) == null) {
                        continue;
                    }
                }

                ssList = m.getJSONArray("products");
                if (ssList != null && ssList.size() > 0) {
                    // 处理商品
                    for (int j = 0; j < ssList.size(); j++) {
                        ssData = new HashMap<>();
                        ssData.putAll(ssList.getJSONObject(j));
                        ssData.put("index", sIndex);
                        sIndex++;
                        list_two.add(ssData);
                    }
                }
            }
            map.put("list1", list_one);
            map.put("list2", list_two);
        } else {
            throw new IOException();
        }
    }


    /**
     * 导出excel
     *
     * @param id
     * @param rule
     * @param response
     * @throws UnsupportedEncodingException
     */
    private void exportIdCardExcel(int id, String rule, HttpServletResponse response) {
        response.setHeader("Content-Disposition", "attachment; filename=" + System.currentTimeMillis() + ExcelTypeEnum.XLSX.getValue());
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        switch (rule) {
            case "_export_v_photo":
                // 导出分单身份图片缺失
                exportIdCard(id, 1, response);
                break;
            case "_export_v_nopass":
                // 导出分单身份校验未通过
                exportIdCard(id, 2, response);
                break;
        }
    }

    /**
     * 导出身份图片为空或者身份校验状态
     *
     * @param id
     * @param type
     * @param response
     */
    private void exportIdCard(int id, int type, HttpServletResponse response) {
        LOG.info("开始导出分单身份图片缺失,主单:{}", id);
        List<HBusiDataManager> list = new ArrayList<>();
        if (1 == type) {
            list = serviceUtils.listFDIdCard(id, BusiTypeEnum.SF.getType(), BusiTypeEnum.SZ.getType(), 2, 0);
        } else if (2 == type) {
            list = serviceUtils.listFDIdCard(id, BusiTypeEnum.SF.getType(), BusiTypeEnum.SZ.getType(), 0, 2);
        }
        EasyExcelUtil.EasyExcelParams param = new EasyExcelUtil.EasyExcelParams();
        ArrayList<IdCardNoVerify> data = new ArrayList<>();
        IdCardNoVerify idCardNoVerify;
        PartyDan partyDan;
        for (HBusiDataManager h : list) {
            idCardNoVerify = new IdCardNoVerify();
            idCardNoVerify.setFdId(h.getExt_3());
            partyDan = JSON.parseObject(h.getContent(), PartyDan.class);
            if (partyDan != null) {
                idCardNoVerify.setAddressee(partyDan.getReceive_name());
                idCardNoVerify.setIdCard(partyDan.getId_no());
            }
            data.add(idCardNoVerify);
        }
        param.setData(data);
        param.setSheetName(id + "-图片缺失");
        param.setDataModelClazz(IdCardNoVerify.class);

        try {
            EasyExcelUtil.exportExcel2007Format(param, response.getOutputStream());
        } catch (IOException e) {
            LOG.error("导出分单身份图片缺失异常,主单:{}", id);
        }
    }
}
