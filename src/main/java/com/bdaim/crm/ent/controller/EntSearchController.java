package com.bdaim.crm.ent.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.ent.service.EntDataService;
import com.bdaim.online.zhianxin.dto.BaseResult;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author chengning@salescomm.net
 */
@RestController
@RequestMapping("/ent/{busiType}")
public class EntSearchController extends BasicAction {

    private static Logger logger = LoggerFactory.getLogger(EntSearchController.class);

    @Autowired
    EntDataService entDataService;

    @PostMapping(value = "/search")
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
        Page page = null;
        try {
            page = entDataService.pageSearch(opUser().getCustId(), opUser().getUserGroupId(), opUser().getId(), busiType, params);
        } catch (Exception e) {
            logger.error("查询记录异常,", e);
            return new ResponseInfoAssemble().failure(-1, "查询记录异常[" + busiType + "]");
        }
        resp.setData(getPageData(page));
        return resp;
    }

    @PostMapping(value = "/{id}")
    public ResponseInfo getInfo(@PathVariable(name = "id") String id, @PathVariable(name = "busiType") String busiType, @RequestBody(required = false) String body) {
        ResponseInfo resp = new ResponseInfo();
        BaseResult baseResult = null;
        JSONObject param = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";
            param = JSONObject.parseObject(body);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "记录解析异常:[" + busiType + "]");
        }
        try {
            long sId = 0L;
            if (StringUtil.isNotEmpty(param.getString("seaId"))) {
                sId = param.getLongValue("seaId");
            }
            //baseResult = searchListService.getCompanyDetail(id, param, busiType, sId);
        } catch (Exception e) {
            logger.error("查询记录异常,", e);
            return new ResponseInfoAssemble().failure(-1, "查询记录异常[" + busiType + "]");
        }
        resp.setData(baseResult.getData());
        resp.setMessage(baseResult.getMessage());
        return resp;
    }
}
