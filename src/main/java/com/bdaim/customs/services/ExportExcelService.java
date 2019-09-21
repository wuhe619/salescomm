package com.bdaim.customs.services;

import cn.afterturn.easypoi.excel.ExcelExportUtil;
import cn.afterturn.easypoi.excel.entity.TemplateExportParams;
import com.alibaba.excel.support.ExcelTypeEnum;
import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.util.JavaBeanUtil;
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

    private Map exportTemplate = new HashMap() {{
        put("_export_low_product", "低价商品单.xlsx");
        put("_export_estimated_tax", "预估税单.xlsx");
        put("_export_declaration_form", "报检单.xlsx");
        put("_export_tally_form", "理货单.xlsx");
    }};

    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;


    private void export(String templatePath, Map<String, Object> map, String[] sheetName, HttpServletResponse response) throws IOException {
        // 加载模板
        TemplateExportParams params = new TemplateExportParams(templatePath);
        if (sheetName != null && sheetName.length > 0) {
            params.setSheetName(sheetName);
        }
        Workbook workbook = ExcelExportUtil.exportExcel(params, map);
        workbook.write(response.getOutputStream());
    }

    public void exportExcel(int id, List<JSONObject> list, String rule, HttpServletResponse response) throws IllegalAccessException, IOException {
        if (list == null && id > 0) {
            exportIdCardExcel(id, rule, response);
            return;
        }
        exportExcelByTemplate(list, rule, response);
    }

    private void exportExcelByTemplate(List<JSONObject> list, String rule, HttpServletResponse response) throws IllegalAccessException, IOException {
        // 生成workbook 并导出
        String templatePath = "tp/" + exportTemplate.get(rule);
        Map<String, Object> map = new HashMap<>();
        map.put("list", JavaBeanUtil.convertBeanToMapList(list));

        response.setHeader("Content-Disposition", "attachment; filename=" + System.currentTimeMillis() + ExcelTypeEnum.XLSX.getValue());
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        switch (rule) {
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
            default:
                LOG.warn("导出未找到匹配规则");
                return;
        }
    }

    private void generateMainDan(List<JSONObject> list, Map<String, Object> map) {
        if (list != null && list.size() > 0) {
            List list_one = new ArrayList();
            List list_two = new ArrayList();
            for (JSONObject m : list) {
                list_one.addAll(m.getJSONArray("singles"));
                for (int i = 0; i < m.getJSONArray("singles").size(); i++) {
                    list_two.addAll(m.getJSONArray("singles").getJSONObject(i).getJSONArray("products"));
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
            list = hBusiDataManagerDao.listFDIdCard(id, BusiTypeEnum.SF.getKey(), 2, 0);
        } else if (2 == type) {
            list = hBusiDataManagerDao.listFDIdCard(id, BusiTypeEnum.SF.getKey(), 0, 2);
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
