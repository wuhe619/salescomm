package com.bdaim.batch.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.batch.dto.DetailQueryParam;
import com.bdaim.batch.service.BatchDetaiService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.util.StringUtil;
import com.bdaim.common.util.page.PageList;

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
 * @date 2018/9/6
 * @description 获取
 */
@Controller
@RequestMapping("/batch/single")
public class BatchDetailAction extends BasicAction {

    @Resource
    private BatchDetaiService batchDetaiService;


    /**
     * @description 列表搜索--某批次被叫客户列表搜索
     * @author:duanliying
     * @method
     * @date: 2018/9/6 16:47
     */
    @RequestMapping(value = "/search.do", method = RequestMethod.POST)
    @ResponseBody
    public Object searchCustomerList(@RequestBody JSONObject jsonO) {
        Map<String, Object> resultMap = new HashMap<>();
        Long userId = opUser().getId();
        String role = opUser().getRole();
        String userType = opUser().getUserType();
        DetailQueryParam detailQueryParam = new DetailQueryParam();
        Integer pageNum = jsonO.getInteger("pageNum");
        Integer pageSize = jsonO.getInteger("pageSize");

        JSONArray custProperty = jsonO.getJSONArray("custProperty");
        String batchId = jsonO.getString("batchId");
        String enterpriseId = jsonO.getString("enterpriseId");
        String idCard = jsonO.getString("idCard");
        String realname = jsonO.getString("realname");

        Integer status = null;
        if (StringUtil.isNotEmpty(jsonO.getString("status"))) {
            status = Integer.valueOf(jsonO.getString("status"));
        }
        String id = jsonO.getString("id");
        //构造检索参数
        detailQueryParam.setBatchId(batchId);
        detailQueryParam.setEnterpriseId(enterpriseId);
        detailQueryParam.setIdCard(idCard);
        detailQueryParam.setRealname(realname);
        detailQueryParam.setId(id);
        detailQueryParam.setStatus(status);
        detailQueryParam.setPageNum(pageNum);
        detailQueryParam.setPageSize(pageSize);
        PageList list = batchDetaiService.getDetailList(detailQueryParam, userId, userType, custProperty,role);
        resultMap.put("batchDetailList", list.getList());
        resultMap.put("listLength", list.getTotal());
        return JSON.toJSONString(resultMap);
    }

    /**
     * @description 查询批次详情属性列表
     * @author:duanliying
     * @method
     * @date: 2018/9/6 16:47
     */
    @RequestMapping(value = "/searchProperty.do", method = RequestMethod.GET)
    @ResponseBody
    public Object searchPropertyList(String batchId) {
        return JSONObject.toJSON(batchDetaiService.getPropertyList(batchId));
    }
}
