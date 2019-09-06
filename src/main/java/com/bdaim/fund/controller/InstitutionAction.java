package com.bdaim.fund.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.dto.PageParam;
import com.bdaim.fund.service.InstitutionService;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/5/31
 * @description 机构
 */

@Controller
@RequestMapping("/institution")
public class InstitutionAction extends BasicAction {
    private static Logger logger = Logger.getLogger(InstitutionAction.class);
    @Resource
    InstitutionService institutionService;

    /**
     * 新增和编辑机构信息
     *
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateInstitutionInfo", method = RequestMethod.POST)
    @CacheAnnotation
    public String addIndustryInfo(@RequestBody JSONObject json) {
        logger.info("接收参数是：" + json.toJSONString());
        JSONObject returnJson = new JSONObject();
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            String type = json.getString("type");
            //新增验证机构名称是否存在
            if ("1".equals(type)) {
                String institutionName = json.getString("institutionName");
                boolean b = institutionService.checkExistInstitutionName(institutionName);
                if (b) {
                    map.put("code", 0);
                    map.put("message", "已存在相同名称");
                    returnJson.put("data", map);
                    return returnJson.toJSONString();
                }
            }
            institutionService.addInstitutionInfo(json);
            map.put("code", 1);
            map.put("message", "成功");
        } catch (Exception e) {
            logger.info("编辑机构信息异常：" + e);
            map.put("code", 0);
            map.put("message", "失败");
        }
        returnJson.put("data", map);
        return returnJson.toJSONString();
    }

    /**
     * @description
     * @author:duanliying
     * @method 机构列表
     * @date: 2019/5/31 13:16
     */
    @ResponseBody
    @RequestMapping(value = "/getInstitutionList", method = RequestMethod.GET)
    @CacheAnnotation
    public Object getInstitutionList(@Valid PageParam pageParam, BindingResult error, String dicType, String institutionName, String brandId) {
        Map<String, Object> resultMap = new HashMap<>();
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        Page list = institutionService.getInstitutionList(dicType, institutionName, brandId, pageParam.getPageNum(), pageParam.getPageSize());
        resultMap.put("total", list.getTotal());
        resultMap.put("list", list.getData());
        return resultMap;
    }

    /**
     * @description
     * @author:duanliying
     * @method 机构删除逻辑删除
     * @date: 2019/5/31 13:16
     */
    @ResponseBody
    @RequestMapping(value = "/deleteInstitution", method = RequestMethod.GET)
    @CacheAnnotation
    public String getInstitutionList(String id) {
        logger.info("需要删除的机构id是：" + id);
        Map<String, Object> map = institutionService.deleteInstitution(id);
        return returnJsonData(map);
    }


    /**
     * @description
     * @author:duanliying
     * @method 机构申请产品排名
     * @date: 2019/5/31 13:16
     */
    @ResponseBody
    @RequestMapping(value = "/applyRank", method = RequestMethod.GET)
    @CacheAnnotation
    public String getUserRankingList(Integer pageNum, Integer pageSize, String dicType, Integer protectNum) {
        ResponseJson responseJson = new ResponseJson();
        if (pageSize == null || pageNum == null || dicType == null) {
            responseJson.setMessage("参数错误");
            responseJson.setCode(-1);
            return JSON.toJSONString(responseJson);
        }
        try {
            Page userRankingList = institutionService.getUserRankingList(pageNum, pageSize, dicType, protectNum);
            responseJson.setData(userRankingList);
            responseJson.setCode(200);
            responseJson.setMessage("success");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return JSON.toJSONString(responseJson);
    }

    /**
     * @description
     * @author:duanliying
     * @method 机构详情
     * @date: 2019/5/31 13:16
     */
    @ResponseBody
    @RequestMapping(value = "/getInstitutionInfo", method = RequestMethod.GET)
    @CacheAnnotation
    public String getInstitutionInfo(String id) {
        Map<String, Object> map = institutionService.getInstitutionInfo(id);
        return returnJsonData(map);
    }
}
