package com.bdaim.common.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.service.SettlementService;
import org.apache.log4j.Logger;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @date 2019/6/21
 * @description 相关信息
 */
@RestController
@RequestMapping("/settlement")
public class SettlementAction extends BasicAction {
    private static Logger logger = Logger.getLogger(SettlementAction.class);
    @Resource
    private SettlementService settlementService;

    /**
     * 保存结算记录( 结算方 1：机构  2：推广活动)
     *
     * @return
     */
    @RequestMapping(value = "/save", method = RequestMethod.POST)
    public String saveSettlementLogs(@RequestBody JSONObject json) {
        ResponseJson responseJson = new ResponseJson();
        String productId = json.getString("productId");
        String settlementObj = json.getString("settlementObj");
        try {
            if (productId == null || settlementObj == null) {
                responseJson.setMessage("参数错误");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            JSONObject jsonObject = settlementService.saveSettlementLogs(json, opUser());
            responseJson.setData(jsonObject.getIntValue("code"));
            responseJson.setCode(200);
            responseJson.setMessage(jsonObject.getString("message"));
        } catch (Exception e) {
            logger.error("保存结算信息异常：", e);
            e.printStackTrace();
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 品牌管理–结算管理---列表
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/list", method = RequestMethod.GET)
    public String getSettlementList(Integer pageNum, Integer pageSize, String dicType, String settlementType, String institutionName) {
        ResponseJson responseJson = new ResponseJson();
        if (pageSize == null || pageNum == null) {
            responseJson.setMessage("参数错误");
            responseJson.setCode(-1);
            return JSON.toJSONString(responseJson);
        }
        JSONObject r = null;
        try {
            r = settlementService.querySettlementList(pageNum, pageSize, dicType, settlementType, institutionName);
            responseJson.setCode(200);
            responseJson.setData(r);
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 查看结算记录接口
     * 结算管理 > 结算记录
     *
     * @return
     */
    @RequestMapping(value = "/log", method = RequestMethod.POST)
    public String getSettlement(@RequestBody JSONObject json) {
        Integer pageSize = json.getIntValue("pageSize");
        Integer pageNum = json.getIntValue("pageNum");
        String settlementObj = json.getString("settlementObj");
        String settlementTime = json.getString("settlementTime");
        String dicType = json.getString("dicType");
        String type = json.getString("type");
        String objId = json.getString("objId");
        ResponseJson responseJson = new ResponseJson();
        try {
            if (pageSize == null || pageNum == null || settlementObj == null || type == null || objId == null) {
                responseJson.setMessage("参数错误");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            JSONObject resultJson = settlementService.getSettlementInfo(pageNum, pageSize, settlementTime, objId, dicType, type, settlementObj);
            responseJson.setData(resultJson);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            logger.error("查看结算记录异常：", e);
            e.printStackTrace();
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 结算记录页面查看结算详情
     *
     * @return
     */
    @RequestMapping(value = "/logDetail", method = RequestMethod.GET)
    public String getSettlementInfo(String settlementIds) {
        ResponseJson responseJson = new ResponseJson();
        try {
            if (settlementIds == null) {
                responseJson.setMessage("参数错误");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            List<Map<String, Object>> settlementDetails = settlementService.getSettlementDetails(settlementIds);
            responseJson.setData(settlementDetails);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            logger.error("查看结算详情信息异常：", e);
            e.printStackTrace();
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 结算记录信息回显(推广活动)
     *
     * @return
     */
    @RequestMapping(value = "/getInputInfo", method = RequestMethod.GET)
    public String getInputInfo(String objId, String settlementObj, String dicType) {
        ResponseJson responseJson = new ResponseJson();
        try {
            //objId 结算对象id   settlementObj结算对象类型  1 ：机构  2：活动
            if (objId == null || settlementObj == null) {
                responseJson.setMessage("参数错误");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            JSONObject inputInfo = settlementService.getInputInfo(objId, settlementObj, dicType);
            responseJson.setData(inputInfo);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            logger.error("查询结算记录信息回显异常：", e);
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * 统计监控运营数据列表展示
     */
    @RequestMapping(value = "/getDataList", method = RequestMethod.GET)
    public String getOperateDataList(Integer pageNum, Integer pageSize, String time, String queryType, String type, String settlementObj,String productId) {
        ResponseJson responseJson = new ResponseJson();
        try {
            if (pageSize == null || pageNum == null || queryType == null || settlementObj == null) {
                responseJson.setMessage("参数错误");
                responseJson.setCode(-1);
                return JSON.toJSONString(responseJson);
            }
            JSONObject operateDataList = settlementService.getOperateDataList(pageNum, pageSize, time, queryType, settlementObj, type,productId);
            responseJson.setData(operateDataList);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            logger.error("申请用户详细信息异常：", e);
            e.printStackTrace();
            responseJson.setMessage(e.getMessage());
            responseJson.setCode(-1);
        }
        return JSON.toJSONString(responseJson);
    }

}
