package com.bdaim.batch.service;

import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.batch.entity.BatchListParam;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.page.PageList;

import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/6
 * @description
 */
public interface BatchListService {

    /**
     * 批次分页列表接口
     *
     * @param page
     * @param batchListParam
     * @return com.bdaim.slxf.util.page.Page
     * @author chengning@salescomm.net
     * @date 2018/9/6 16:40
     */
    PageList pageList(PageParam page, BatchListParam batchListParam, String role);

    /**
     * 批次列表接口
     *
     * @param page
     * @param batchListParam
     * @return java.util.List<java.util.Map                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               <                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               java.lang.String                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               java.lang.Object>>
     * @author chengning@salescomm.net
     * @date 2018/9/6 16:46
     */
    List<Map<String, Object>> list(PageParam page, BatchListParam batchListParam);

    /**
     * 计算外呼进度数据
     *
     * @param userId
     * @param batchId
     * @param customId
     * @return java.util.Map<java.lang.String                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               ,                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                                               java.lang.Object>
     * @author chengning@salescomm.net
     * @date 2018/8/17 15:06
     */
    Map<String, Object> countCallProgressByCondition(String userId, String userType, String batchId, String customId);

    /**
     * 修复文件信息入批次表、批次明细表
     */
    void saveBatchDetail(String certifyMd5, String kehuId, String batchId, String lalel_one, String label_two, String label_three, String channel, int certifyType) throws Exception;

    void saveBatchDetailList(List<BatchDetail> batchDetailList, String channelall, String resourceId, int certifyType, String batchId, Long operUserId, String operName);

    void saveBatch(String batchname, int uploadNum, String repairStrategy, String compId, String batchId, int certifyType, String channl,String province,String city) throws Exception;

    void cucIsreceive(String batchId, int cucIsReceived) throws Exception;

    /**
     * 修复文件信息记录
     */
    void saveBatchLog(String certifyMd5, String kehuId, String batchId, Long operUserId, String operName) throws Exception;

    /**
     * 获取修复文件信息记录
     */
    List<Map<String, Object>> batchOperlogLsit(String batchid);

    /**
     * @description 批次列表统计分析功能
     */
    Map<String, Object> queryAnalysis(PageParam page, String batchId, String custId);


    /**
     * @description 地址修复功能
     */
    /* Page pageget(PageParam page, BatchListParam batchListParam);*/

    PageList sitelist(PageParam page, BatchListParam batchListParam);


    void addressrepairupload(String certifyMd5, String name, String batchId, String phone, String compId, String channelall);


    Object ditchList(String companyid, int certify_type);

    void Batch(String batchname, int uploadNum, String repairMode, String compId, String batchId, String channelall);

    List<Map<String, Object>> getTime();

    List<Map<String, Object>> getArea(String parentId);
}
