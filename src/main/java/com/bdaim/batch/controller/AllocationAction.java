package com.bdaim.batch.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.entity.BatchDetailDTO;
import com.bdaim.batch.service.AllocationService;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2018/9/7
 * @description--为客户分配负责人
 */
@Controller
@RequestMapping("/batch")
public class AllocationAction extends BasicAction {

    @Resource
    private AllocationService allocationService;

    /**
     * @description 查询未分配人数
     * @author:duanliying
     * @method
     * @date: 2018/9/20 14:36
     */
    @RequestMapping(value = "/noAllocatNum.do", method = RequestMethod.GET)
    @ResponseBody
    public Object quickNoAllocatNum(String batchId) throws Exception {
        List<Map<String, Object>> noAllocationlist = allocationService.queryNoAssigned(batchId);
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("noAllocationNum",noAllocationlist.size());
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description 快速分配负责人(assignedlist责任人 （ number分配人数 + userId用户id ） + batchId批号ID)
     * @author:duanliying
     * @method
     * @date: 2018/9/7 10:35
     */
    @RequestMapping(value = "/quick_allocat_person.do", method = RequestMethod.POST)
    @ResponseBody
    public Object quickAllocatPerson(@RequestBody JSONObject jsonO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        String message = null;
        //选中的责任人集合（分配的人数+用户id  userid）
        JSONArray peopleAssignedList = jsonO.getJSONArray("assignedlist");
        String batchId = jsonO.getString("batchId");
        for (int i = 0; i < peopleAssignedList.size(); i++) {
            JSONObject jsonObject2 = peopleAssignedList.getJSONObject(i);
            Integer number = jsonObject2.getInteger("number");// 快速分配的人数
            String userId = jsonObject2.getString("userId");
            BatchDetailDTO batchDetailDTO = new BatchDetailDTO();
            List<BatchDetailDTO> list = new ArrayList<BatchDetailDTO>();
            // 查找未分配负责人的客户信息
            List<Map<String, Object>> noAssignedlist = allocationService.queryNoAssigned(batchId);
            if (noAssignedlist.size() == 0) {
                resultMap.put("message", "全部已分配负责人");
                return JSONObject.toJSON(resultMap);
            }
            //设置DTO用于修改数据库信息
            for (int j = 0; j < number; j++) {
                Map<String, Object> noAssignedMap = noAssignedlist.get(j);
                //要分配的客户唯一标识
                String id = String.valueOf(noAssignedMap.get("id"));
                Long userIdA = Long.parseLong(userId);
                batchDetailDTO = new BatchDetailDTO();
                batchDetailDTO.setId(id);
                batchDetailDTO.setUserId(userIdA);
                batchDetailDTO.setBatchid(batchId);
                batchDetailDTO.setAllocation(1);
                list.add(batchDetailDTO);
            }
            message = allocationService.updateAssignedMany(list);
        }
        resultMap.put("message", message);
        return JSONObject.toJSON(resultMap);
    }


    /**
     * @description 被叫客户修改负责人和分配负责人接口
     * @author:duanliying
     * @method
     * @date: 2018/9/7 17:09
     */
    @RequestMapping(value = "/modify_person.do", method = RequestMethod.POST)
    @ResponseBody
    public Object updateAllocatMessage(@RequestBody JSONObject jsonO) throws Exception {
        Map<String, Object> resultMap = new HashMap<>();
        String message = null;
        // 已选分配负责任人
        Long userId = jsonO.getLong("userId");// 单个的userId
        JSONArray peopleAssignedIds = jsonO.getJSONArray("ids");// 为已选分配责任人的idCrds数组
        String batchId = jsonO.getString("batchId");

        BatchDetailDTO batchDetailDTO = new BatchDetailDTO();
        List<BatchDetailDTO> list = new ArrayList<BatchDetailDTO>();
        for (int i = 0; i < peopleAssignedIds.size(); i++) {
            String id = peopleAssignedIds.getString(i);
            batchDetailDTO = new BatchDetailDTO();
            batchDetailDTO.setId(id);
            batchDetailDTO.setBatchid(batchId);
            batchDetailDTO.setUserId(userId);
            batchDetailDTO.setAllocation(1);
            list.add(batchDetailDTO);
        }
        message = allocationService.updateAssignedMany(list);

        resultMap.put("message", message);
        return JSONObject.toJSON(resultMap);
    }

    /**
     * @description 获取员工姓名
     * @author:duanliying
     * @method
     * @date: 2018/9/7 14:17
     */
    @ResponseBody
    @RequestMapping(value = "/getStaffName", method = RequestMethod.GET)
    public String getStaffName() {
        String custId = opUser().getCustId();
        return allocationService.getStaffName(custId);
    }

}

