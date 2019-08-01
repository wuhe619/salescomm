package com.bdaim.batch.service.impl;

import com.bdaim.batch.dao.BatchInfoDao;
import com.bdaim.batch.dao.BatchInfoDetailDao;
import com.bdaim.batch.entity.BatchDetailInfo;
import com.bdaim.batch.entity.BatchInfo;
import com.bdaim.batch.service.ExpressBatchService;
import com.bdaim.common.response.JsonResult;
import com.bdaim.common.util.Constant;
import com.bdaim.common.util.DateUtil;
import com.bdaim.common.util.ExcelReaderUtil;
import org.apache.commons.io.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.List;


/**
 * @description: 发件批次 信息管理 (失联修复_信函模块)
 * @auther: Chacker
 * @date: 2019/7/31 16:02
 */
@Service
public class ExpressBatchServiceImpl implements ExpressBatchService {

    @Autowired
    private BatchInfoDao batchInfoDao;
    @Autowired
    private BatchInfoDetailDao batchInfoDetailDao;

    @Override
    public JsonResult receiverInfoImport(MultipartFile multipartFile, String batchName, int batchType) throws IOException {
        long time1 = System.currentTimeMillis();
        //1. 把excel文件上传到服务器中
        String fileName = multipartFile.getOriginalFilename();
        String suffix = fileName.substring(fileName.lastIndexOf("."));
        String uploadPath = "/express/upload/";
        String batchId = String.valueOf(System.currentTimeMillis());
        // 文件路径的字符串拼接 目录 + 时间戳 + 毫秒
        uploadPath = uploadPath + batchId + suffix;
        File file = new File(uploadPath);
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            file.createNewFile();
        }
        //为保证uploadPath定义的路径 在readExcel中和在创建file中使用的路径一样，所以使用FileUtils工具类
        FileUtils.copyInputStreamToFile(multipartFile.getInputStream(), file);
        //2. 读取 & 解析excel文件(根据文件的后缀名进行区分)
        List<List<String>> contentList = ExcelReaderUtil.readExcel(uploadPath);
        //3. 把批次信息和批次详情列表存入数据库
        //3.1 设置并新增保存批次信息
        BatchInfo batchInfo = new BatchInfo();
        batchInfo.setId(String.valueOf(System.currentTimeMillis()));
        batchInfo.setBatchName(batchName);
        batchInfo.setBatchType(String.valueOf(batchType));
        batchInfo.setUploadTime(DateUtil.fmtDateToStr(new Date(), DateUtil.YYYY_MM_DD_HH_mm_ss));
        //把批次状态设置为校验中
        batchInfo.setStatus(Constant.CHECKING);
        //contentList的数量减去1，即为上传数量 (因为excel的第一行为列的标题)
        batchInfo.setUploadNum(contentList.size() - 1);
        batchInfoDao.saveBatchInfo(batchInfo);
        //3.2 获取并保存批次详情信息 (因为第一行为标题，所以从第二行开始遍历)
        for (int i = 1; i < contentList.size(); i++) {
            BatchDetailInfo batchDetailInfo = new BatchDetailInfo();
            batchDetailInfo.setId(String.valueOf(System.currentTimeMillis()));
            batchDetailInfo.setName(contentList.get(i).get(0));
            batchDetailInfo.setPhone(contentList.get(i).get(1));
            //地址填充到扩展字段 label_four
            batchDetailInfo.setLabelFour(contentList.get(i).get(2));
            //校验结果设置为校验中
            batchDetailInfo.setCheckingResult(Constant.CHECKING);
            batchDetailInfo.setBatchId(batchInfo.getId());
            batchInfoDetailDao.saveBatchInfoDetail(batchDetailInfo);
        }
        long time2 = System.currentTimeMillis();
        System.out.println(time2 - time1);
        return new JsonResult();

    }
}
