package com.bdaim.customs.services;

import com.alibaba.fastjson.JSON;
import com.bdaim.common.util.excel.EasyExcelUtil;
import com.bdaim.customs.dao.HBusiDataManagerDao;
import com.bdaim.customs.dto.excel.IdCardNoVerify;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.customs.entity.HBusiDataManager;
import com.bdaim.customs.entity.PartyDan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

/**
 * @author chengning@salescomm.net
 * @date 2019/9/19
 * @description
 */
@Service
@Transactional
public class ExportExcelService {

    private final static Logger LOG = LoggerFactory.getLogger(ExportExcelService.class);

    @Autowired
    private HBusiDataManagerDao hBusiDataManagerDao;

    /**
     * 导出excel
     * @param id
     * @param type
     * @param response
     * @throws UnsupportedEncodingException
     */
    public void exportExcel(int id, int type, HttpServletResponse response) throws UnsupportedEncodingException {
        final String fileType = ".xlsx";
        response.setHeader("Content-Disposition", "attachment; filename=" + System.currentTimeMillis() + fileType);
        response.setContentType("application/vnd.ms-excel;charset=utf-8");
        switch (type) {
            case 1:
                // 导出分单身份图片缺失
                exportIdCardNoPhoto(id, response);
        }
    }

    private void exportIdCardNoPhoto(int id, HttpServletResponse response) {
        LOG.info("开始导出分单身份图片缺失,主单:{}", id);
        List<HBusiDataManager> list = hBusiDataManagerDao.listFDIdCard(id, BusiTypeEnum.SF.getKey(), 2, 0);
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
                idCardNoVerify.setIdCard(partyDan.getId_NO());
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
