package com.bdaim.common.controller;


import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.service.SourceService;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.annotation.Resource;

@Controller
@RequestMapping("/source")

/**
 *
 *
 */
public class SourceAction extends BasicAction {
    @Resource
    SourceService sourceService;

    /**
     * 数据源概览
     *
     * @return
     */
    @ResponseBody
    @RequestMapping("/listDataSource")
    @CacheAnnotation
    public String listDataSource() {
        return sourceService.listDataSource();
    }

    /**
     * 数据源状态修改
     *
     * @param sourceId
     * @param status
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateSourceStatus", method = RequestMethod.PUT)
    @CacheAnnotation
    public String updateSourceStatus(Integer sourceId, Integer status) {
        return sourceService.updateSourceStatus(sourceId, status);
    }

    /**
     * 查询数据源一级标签树
     *
     * @param id
     * @param status
     * @return
     */
    @ResponseBody
    @RequestMapping("/listLabelsByCondition")
    @CacheAnnotation
    public String listLabelsByCondition(String id, String status) {
        return sourceService.listLabelsByCondition(id, status);
    }

    /**
     * 查询数据源子标签树
     *
     * @param id
     * @param status
     * @return
     */
    @ResponseBody
    @RequestMapping("/listLabelsChildrenById")
    @CacheAnnotation
    public String listLabelsChildrenById(String id, String status) {
        return sourceService.listLabelsChildrenById(id, status);
    }

    /**
     * 查询数据源标签
     *
     * @param json
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/listSourceLabelsByCondition", method = RequestMethod.POST)
    @CacheAnnotation
    public String listSourceLabelsByCondition(@RequestBody JSONObject json) {
        return sourceService.listSourceLabelsByCondition(json);
    }

    /**
     * 设置成本价
     *
     * @param json
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateLabelSourcePrice", method = RequestMethod.POST)
    @CacheAnnotation
    public String updateLabelSourcePrice(Double price, Integer priceId, Integer labelId) {
        //String cust_id=user.get().getCust_Id();
        String enpterpriseName;
        if ("admin".equals(opUser().getRole())) {
            enpterpriseName = opUser().getUsername();
        } else {
            enpterpriseName = opUser().getEnterpriseName();
        }
        return sourceService.updateLabelSourcePrice(price, labelId, enpterpriseName);
    }

    /**
     * 批量处理
     *
     * @param state
     * @param price
     * @param idList
     * @param operator
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/updateLabelSourcePriceBatch", method = RequestMethod.POST)
    @CacheAnnotation
    public String updateLabelSourcePriceBatch(String state, Double price, String[] idList) {
        //String cust_id=user.get().getCust_Id();
        String enpterprise_name = opUser().getEnterpriseName();
        return sourceService.updateLabelSourcePriceBatch(state, price, idList, enpterprise_name);
    }


    /**
     * 查询成本价定价记录
     *
     * @param price
     * @param priceId
     * @return
     */
    @ResponseBody
    @RequestMapping("/listLabelSourcePriceLog")
    @CacheAnnotation
    public String listLabelSourcePriceLog(Integer priceId, Integer labelId, Integer pageNum, Integer pageSize) {
        return sourceService.listLabelSourcePriceLog(priceId, labelId, pageNum, pageSize);
    }

    /**
     * 二级联动
     *
     * @param state
     * @param id
     * @return
     */
    @ResponseBody
    @RequestMapping("/listLabelList")
    @CacheAnnotation
    public String listLabelList(String state, String id) {
        return sourceService.listLabelList(state, id);
    }
}
