package com.bdaim.batch.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.dto.TouchInfoDTO;
import com.bdaim.batch.service.LostContactService;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.util.StringUtil;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author duanliying
 * @date 2018/9/10
 * @description 被叫用户个人信息接口
 */
@Controller
@RequestMapping("/batch/coustomer")
public class LostContactAction extends BasicAction {

    private final static Logger LOG = LoggerFactory.getLogger(LostContactAction.class);
    @Resource
    private LostContactService lostContactService;

    /**
     * @description 获取失联人员信息接口(根据唯一标识还有批次id)
     * @author:duanliying
     * @method 根据客户id查询
     * @date: 2018/9/10 9:45
     */
    @RequestMapping(value = "/info", method = RequestMethod.GET)
    @ResponseBody
    @CacheAnnotation
    public String LostContactMessageById(String batchId, String custId, String id, String type, Integer pageNum, Integer pageSize) {
        if (StringUtil.isEmpty(custId)) {
            custId = opUser().getCustId();
            LOG.info("当前登录的企业id是：" + custId);
        }
        return lostContactService.getMessageById(batchId, id, type, pageNum, pageSize, custId, opUser().getId().toString());
    }

    /**
     * @description 修改某失联人员信息
     * @author:duanliying
     * @method
     * @date: 2018/9/10 12:41
     */
    @ResponseBody
    @RequestMapping(value = "/update", method = RequestMethod.POST)
    @CacheAnnotation
    public String updateVoiceLogNew(@RequestBody JSONObject jsonO) {
        Map<String, Object> resultMap = new HashMap<>();
        String customerId = opUser().getCustId();
        Long userId = opUser().getId();
        JSONArray labelIdArray = jsonO.getJSONArray("labelIds");
        String remark = jsonO.getString("remark");
        String id = jsonO.getString("id");
        // t_touch_voice_log表的touch_id
        String touchId = jsonO.getString("touchId");
        String batchId = jsonO.getString("batchId");
        LOG.info("更新个人资料updateVoiceLogNew，参数touchId：" + touchId + "\tbatchId" + batchId);
        // 更新t_touch_voice_log表
        if (StringUtil.isNotEmpty(touchId)) {
            lostContactService.updateVoiceLog(touchId, remark, String.valueOf(opUser().getId()), opUser().getCustId());
        }
        // 删除原来的自建属性信息
        lostContactService.deleteSuperLable(id, batchId);
        // 新增
        if (labelIdArray != null || labelIdArray.size() != 0) {
            String labelId;
            String optionValue;
            for (int i = 0; i < labelIdArray.size(); i++) {
                labelId = labelIdArray.getJSONObject(i).getString("labelId");
                optionValue = labelIdArray.getJSONObject(i).getString("optionValue");
                // 插入客户购买资源用户标签表
                String idSuper = Long.toString(com.bdaim.util.IDHelper.getID());
                //添加用户选择的自建属性信息
                lostContactService.insertSuperLable(idSuper, id, labelId, batchId, optionValue);
            }
        }
        //如果voice_info_id存在，数据存在做更新，否则做插入
        String voiceInfoId = jsonO.getString("voice_info_id");
        if (voiceInfoId == null || "".equals(voiceInfoId)) {
            voiceInfoId = com.bdaim.util.IDHelper.getID().toString();
        }
        TouchInfoDTO touchInfoDTO = new TouchInfoDTO();
        touchInfoDTO.setVoice_info_id(voiceInfoId);
        touchInfoDTO.setCust_id(customerId);
        touchInfoDTO.setUser_id(userId.toString());
        touchInfoDTO.setCust_group_id(jsonO.getString("batchId"));
        touchInfoDTO.setSuper_id(jsonO.getString("id"));
        touchInfoDTO.setSuper_name(jsonO.getString("super_name"));
        touchInfoDTO.setSuper_age(jsonO.getString("super_age"));
        touchInfoDTO.setSuper_sex(jsonO.getString("super_sex"));
        touchInfoDTO.setSuper_phone(jsonO.getString("super_phone"));
        touchInfoDTO.setSuper_telphone(jsonO.getString("super_telphone"));
        touchInfoDTO.setSuper_address_province_city(jsonO.getString("super_address_province_city"));
        touchInfoDTO.setSuper_address_street(jsonO.getString("super_address_street"));
        touchInfoDTO.setBantch_id(jsonO.getString("batchId"));
        //新增打电话获取的用户信息
        lostContactService.updateTouchInfo(touchInfoDTO);
        resultMap.put("code", "200");
        resultMap.put("_message", "更新成功");
        return JSONObject.toJSONString(resultMap);
    }
}
