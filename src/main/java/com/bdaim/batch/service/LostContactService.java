package com.bdaim.batch.service;

import com.bdaim.slxf.dto.TouchInfoDTO;

import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2018/9/10
 * @description 失联人员信息接口
 */
public interface LostContactService {
    /**
     * @description 获取失联人员详细信息
     * @author:duanliying
     * @method
     * @date: 2018/9/10 9:56
     */
    String getMessageById(String batchId, String id, String type, Integer pageNum, Integer pageSize, String custId, String userId);

    /**
     * 更新日志表坐席打完电话修改电话备注信息
     */
    String updateVoiceLog(String touchId, String remark, String userId, String custId);

    /**
     * 删除用户原来保存的自建属性信息
     */
    String deleteSuperLable(String cardId, String batchId);

    /**
     * 添加用户选择的自建属性信息
     */
    boolean insertSuperLable(String id, String cardId, String labelId, String batchId, String optionValue);

    /**
     * 修改打电话获取的用户信息
     */
    boolean updateTouchInfo(TouchInfoDTO touchInfoDTO);

    /**
     * 添加打电话获取的用户详细信息
     */
    boolean insertTouchInfo(TouchInfoDTO touchInfoDTO);

    /**
     * 查询用户已选择标签
     */
    List getSelLabel(String idCard, String batchId);

    /**
     * 查询所有自建属性信息
     */
    List<Map<String, Object>> getCustomlabel(Integer pageNum, Integer pageSize, String custId);
}
