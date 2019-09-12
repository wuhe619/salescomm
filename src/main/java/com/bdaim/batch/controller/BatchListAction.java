package com.bdaim.batch.controller;

import com.alibaba.fastjson.JSON;
import com.bdaim.auth.LoginUser;
import com.bdaim.batch.entity.BatchListParam;
import com.bdaim.batch.service.BatchListService;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.util.page.PageList;

import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;
import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @date 2018/9/6
 * @description
 */
@Controller
@RequestMapping("/batch")
public class BatchListAction extends BasicAction {

    @Resource
    private BatchListService batchListService;

    @ResponseBody
    @RequestMapping(value = "/list.do", method = RequestMethod.GET)
    public String listPage(@Valid PageParam page, BindingResult error) {
        if (error.hasFieldErrors()) {
            page.setPageNum(1);
            page.setPageSize(20);
        }
        if (page.getPageSize() > 100) {
            page.setPageSize(100);
        }
        BatchListParam batchListParam = new BatchListParam();
        batchListParam.setCompId(opUser().getCustId());
        batchListParam.setUserId(String.valueOf(opUser().getId()));
        batchListParam.setUserType(opUser().getUserType());
        PageList list = batchListService.pageList(page, batchListParam, opUser().getRole());
        return JSON.toJSONString(list);
    }

    @ResponseBody
    @RequestMapping(value = "/search.do", method = RequestMethod.GET)
    public String searchPage(@Valid PageParam page, BindingResult error, BatchListParam batchListParam) {
        if (error.hasFieldErrors()) {
            page.setPageNum(1);
            page.setPageSize(20);
        }
        if (page.getPageSize() > 100) {
            page.setPageSize(100);
        }
        LoginUser lu = opUser();
        String role = lu.getRole();
        batchListParam.setCompId(opUser().getCustId());
        batchListParam.setUserId(String.valueOf(opUser().getId()));
        batchListParam.setUserType(opUser().getUserType());
        PageList list = batchListService.pageList(page, batchListParam, role);
        return JSON.toJSONString(list);
    }

    /*地址修复批次页面*/
    @ResponseBody
    @RequestMapping(value = "/site.do", method = RequestMethod.GET)
    public String site(@Valid PageParam page, BindingResult error, BatchListParam batchListParam) {
        if (error.hasFieldErrors()) {
            page.setPageNum(1);
            page.setPageSize(20);
        }
        if (page.getPageSize() > 100) {
            page.setPageSize(100);
        }

        LoginUser lu = opUser();
        batchListParam.setCompId(opUser().getCustId());
        batchListParam.setUserId(String.valueOf(opUser().getId()));
        batchListParam.setUserType(opUser().getUserType());
        PageList list = batchListService.sitelist(page, batchListParam);
        return JSON.toJSONString(list);
    }


    @ResponseBody
    @CacheAnnotation
    @RequestMapping(value = "/countCallProgressByCondition", method = RequestMethod.GET)
    public String countCallProgressByCondition(String batchId) {
        Map<Object, Object> map = new HashMap<>(16);
        Map<String, Object> list = batchListService.countCallProgressByCondition(opUser().getId().toString(), String.valueOf(opUser().getUserType()), batchId, opUser().getCustId());
        map.put("data", list);
        return JSON.toJSONString(map);
    }


    /**
     * @description 批次列表统计分析功能
     */
    @ResponseBody
    @RequestMapping(value = "/analysis", method = RequestMethod.GET)
    public String searchAnalysis(@Valid PageParam page, BindingResult error, String batchId, String custId) {
        Map<String, Object> list = null;
        if (error.hasFieldErrors()) {
            return getErrors(error);
        }
        list = batchListService.queryAnalysis(page, batchId, custId);
        return JSON.toJSONString(list);
    }
}
