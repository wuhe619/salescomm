package com.bdaim.batch.service;

import com.bdaim.batch.entity.BatchInfo;
import com.bdaim.common.response.ResponseInfo;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

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
     * @param custId        企业ID
     * @return
     * @auther Chacker
     * @date 2019/7/31 14:54
     */
    ResponseInfo receiverInfoImport(MultipartFile multipartFile, String batchName, int batchType, String custId) throws IOException;

    /**
     * 分页查询批次列表
     *
     * @param map pageNum、pageSize、custId 包括分页参数和企业ID
     * @return
     * @auther Chacker
     * @date 2019/8/1 16:34
     */
    Map<String, Object> batchList(Map<String, Object> map) throws IllegalAccessException;

    /**
     * 查询批次详情
     *
     * @param map batch_id
     * @return
     * @auther Chacker
     * @date 2019/8/2 14:38
     */
    Map<String, Object> batchDetail(Map<String, Object> map) throws IllegalAccessException;


}
