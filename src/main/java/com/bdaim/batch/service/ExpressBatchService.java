package com.bdaim.batch.service;

import com.bdaim.common.response.JsonResult;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @description: 发件批次 信息管理 (失联修复_信函模块)
 * @auther: Chacker
 * @date: 2019/7/31 16:01
 */
public interface ExpressBatchService {
    /**
     * 上传发件信息(上传excel文件,对文件中的收件人信息进行批量导入)
     *
     * @param multipartFile excel文件
     * @param batchName     批次名称
     * @param batchType     快递内容形式 1.电子版 2.打印版
     * @return
     * @auther Chacker
     * @date 2019/7/31 14:54
     */
    JsonResult receiverInfoImport(MultipartFile multipartFile, String batchName, int batchType) throws IOException;
}
