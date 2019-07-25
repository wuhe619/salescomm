package com.bdaim.batch.service;

import java.util.List;
import java.util.Map;

import com.bdaim.batch.entity.BatchDetailDTO;

/**
 * @author duanliying
 * @date 2018/9/7
 * @description -- 为客户分配负责人
 */
public interface AllocationService {


    /**
     * @description 查找员工用户
     * @author:duanliying
     * @method
     * @date: 2018/9/7 14:18
     */
    String getStaffName(String custId);

    /**
     * @description 为批次下单个客户进行分配负责人
     * @author:duanliying
     * @method
     * @date: 2018/9/7 15:49
     */
    String updateAssignedOne(String id, Long userId, String batchId);

    /**
     * 为批次下已选客户进行分配负责人 和 快速分配负责人
     *
     * @description
     * @author:duanliying
     * @method
     * @date: 2018/9/7 16:02
     */
    String updateAssignedMany(List<BatchDetailDTO> list);

    /**
     * @description 查找未分配负责人的客户信息
     * @author:duanliying
     * @method
     * @date: 2018/9/7 16:15
     */
    List<Map<String, Object>> queryNoAssigned(String batchId);
}
