package com.bdaim.common.third.zhianxin.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.third.zhianxin.dto.BaseResult;
import com.bdaim.common.third.zhianxin.service.SearchListService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author chengning@salescomm.net
 * @date 2019-10-29 11:41
 */
@RestController
@RequestMapping("/zhianxin/{busiType}")
public class ZAXSearchController extends BasicAction {

    private static Logger logger = LoggerFactory.getLogger(ZAXSearchController.class);

    @Autowired
    SearchListService searchListService;

    @PostMapping(value = "/all")
    public ResponseInfo pageSearch(@RequestBody(required = false) String body, @PathVariable(name = "busiType") String busiType) {
        ResponseInfo resp = new ResponseInfo();
        JSONObject params = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";
            params = JSONObject.parseObject(body);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "查询条件解析异常[" + busiType + "]");
        }
        BaseResult baseResult = null;
        try {
            baseResult = searchListService.pageSearch(opUser().getCustId(), opUser().getUserGroupId(), opUser().getId(), busiType, params);
        } catch (Exception e) {
            logger.error("查询记录异常,", e);
            return new ResponseInfoAssemble().failure(-1, "查询记录异常[" + busiType + "]");
        }
        resp.setData(baseResult.getData());
        return resp;
    }

    @GetMapping(value = "/{id}")
    public ResponseInfo getInfo(@PathVariable(name = "id") String id, @PathVariable(name = "busiType") String busiType) {
        ResponseInfo resp = new ResponseInfo();
        BaseResult baseResult = null;
        try {
            baseResult = searchListService.getCompanyDetail(id, "", busiType);
        } catch (Exception e) {
            logger.error("查询记录异常,", e);
            return new ResponseInfoAssemble().failure(-1, "查询记录异常[" + busiType + "]");
        }
        resp.setData(baseResult.getData());
        return resp;
    }
}