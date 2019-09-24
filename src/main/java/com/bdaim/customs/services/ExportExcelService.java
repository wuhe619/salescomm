package com.bdaim.customs.services;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.excel.EasyExcelUtil;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.dto.excel.IdCardNoVerify;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.PartyDan;
import org.apache.poi.ss.usermodel.Workbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/19
 * @description
 */
@Service
@Transactional
public class ExportExcelService {

    private final static Logger LOG = LoggerFactory.getLogger(ExportExcelService.class);

    private Map<String, String> exportTemplate = new HashMap() {{
        put("_export_low_product", "低价商品单.xlsx");
        put("_export_estimated_tax", "预估税单.xlsx");
        put("_export_declaration_form", "报检单.xlsx");
        put("_export_tally_form", "理货单.xlsx");
        put("_export_bgd_z_main_data", "报关单数据.xls");
        put("_export_cd_z_main_data", "理货单.xlsx");
    }};

    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;


    private void export(String templatePath, Map<String, Object> map, String[] sheetName, HttpServletResponse response) throws IOException {
        // 加载模板
        TemplateExportParams params = new TemplateExportParams(templatePath, true);
        if (sheetName != null && sheetName.length > 0) {
            params.setSheetName(sheetName);
        }
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);
        workbook.write(response.getOutputStream());
    }

    public void exportExcel(int id, List<JSONObject> list, JSONObject param, HttpServletResponse response) throws IllegalAccessException, IOException {
        if ("_export_v_photo".equals(param.getString("_rule_"))
                || "_export_v_nopass".equals(param.getString("_rule_"))) {
            exportIdCardExcel(id, param.getString("_rule_"), response);
            return;
        }
        if (list != null) {
            exportExcelByTemplate(list, param, response);
        }
    }

    private void exportExcelByTemplate(List<JSONObject> list, JSONObject param, HttpServletResponse response) throws IllegalAccessException, IOException {
        if (StringUtil.isEmpty(exportTemplate.get(param.getString("_rule_")))) {
            LOG.warn("导出规格:{}未找到对应模板", param.getString("_rule_"));
            return;
        }
        // 生成workbook 并导出
        String templatePath = "tp/" + exportTemplate.get(param.getString("_rule_"));
        Map<String, Object> map = new HashMap<>();
        //map.put("list", JavaBeanUtil.convertJsonObjectToMapList(list));
        map.put("list", list);

        response.setHeader("Content-Disposition", "attachment; filename=" + System.currentTimeMillis() + ExcelTypeEnum.XLSX.getValue());
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        switch (param.getString("_rule_")) {
            // 导出低价商品单,导出预估税单
            case "_export_low_product":
            case "_export_estimated_tax":
            case "_export_cd_z_main_data":
                export(templatePath, map, null, response);
                break;
            // 导出报检单,导出理货单
            case "_export_declaration_form":
            case "_export_tally_form":
                String[] sheetName = new String[]{"主单", "分单", "税单"};
                generateMainDan(list, map);
                export(templatePath, map, sheetName, response);
                break;
            case "_export_bgd_z_main_data":
                sheetName = new String[]{"报关单表头", "报关单商品"};
                generateMainDan(list, map);
                export(templatePath, map, sheetName, response);
                break;
            default:
                LOG.warn("导出未找到匹配规则");
                return;
        }
    }

    private void generateMainDan(List<JSONObject> list, Map<String, Object> map) {
        if (list != null && list.size() > 0) {
            List<Map<String, Object>> list_one = new ArrayList();
            List<Map<String, Object>> list_two = new ArrayList();
            Map<String, Object> fdData, ssData;
            JSONArray fdList, ssList;
            for (JSONObject m : list) {
                fdList = m.getJSONArray("singles");
                if (fdList == null || fdList.size() == 0) {
                    continue;
                }
                // 处理分单
                for (int i = 0; i < fdList.size(); i++) {
                    fdData = new HashMap<>();
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
                        list_two.add(ssData);
                    }
                }
            }
            map.put("list1", list_one);
            map.put("list2", list_two);
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

    private void exportIdCard(int id, int type, HttpServletResponse response) {
        LOG.info("开始导出分单身份图片缺失,主单:{}", id);
        List<HBusiDataManager> list = new ArrayList<>();
        if (1 == type) {
            list = hBusiDataManagerDao.listFDIdCard(id, BusiTypeEnum.SF.getType(), 2, 0);
        } else if (2 == type) {
            list = hBusiDataManagerDao.listFDIdCard(id, BusiTypeEnum.SF.getType(), 0, 2);
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
