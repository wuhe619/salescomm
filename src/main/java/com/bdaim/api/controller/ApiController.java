package com.bdaim.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.api.Dto.ApiData;
import com.bdaim.api.service.ApiService;
import com.bdaim.auth.LoginUser;
import com.bdaim.auth.service.impl.TokenServiceImpl;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.util.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/api")
public class ApiController {
    @Resource
    private TokenServiceImpl tokenService;

    @Autowired
    private ApiService apiService;

    /**
     * Query Api Infos
     **/
    @PostMapping("/infos")
    public ResponseInfo apis(@RequestBody JSONObject params) {
        ResponseInfo resp = new ResponseInfo();
        PageParam page = new PageParam();
        page.setPageSize(params.getInteger("pageSize") == null ? 0 : params.getIntValue("pageSize"));
        page.setPageNum(params.getInteger("pageNum") == null ? 10 : params.getIntValue("pageNum"));
        resp.setData(apiService.apis(page, params));
        return resp;
    }

    /**
     * Save Api
     **/
    @PostMapping("/info/{apiId}")
    public ResponseInfo saveApi(@RequestBody ApiData apiData, @PathVariable(name = "apiId") String apiId) {
        ResponseInfo resp = new ResponseInfo();

        LoginUser lu = tokenService.opUser();
        try {
            resp.setData(apiService.saveApiProperty(apiData, apiId, lu));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseInfoAssemble().failure(-1, "Api创建失败:");
        }

        return resp;
    }

    /**
     * Get Api
     **/
    @GetMapping("/info/{apiId}")
    public ResponseInfo getApi(@PathVariable(name = "apiId") Integer apiId) {
        ResponseInfo resp = new ResponseInfo();
        if (apiId == null || apiId == 0) {
            return new ResponseInfoAssemble().failure(-1, "Api创建失败:");
        }
        try {
            resp.setData(apiService.getApiById(apiId));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseInfoAssemble().failure(-1, "查询异常:");
        }
        return resp;
    }

    /**
     * Delete Api
     **/
    @DeleteMapping("/info/{apiId}")
    public ResponseInfo deleteApi(@PathVariable(name = "apiId") String apiId, int status) {
        ResponseInfo resp = new ResponseInfo();
        LoginUser lu = tokenService.opUser();
        try {
            apiService.updateStatusApiById(apiId, lu, status);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "Api删除失败:失败原因apiId不存在");
        }

        return resp;
    }


    /**
     * Subscribe Api
     **/
    @PostMapping("/subscription/{apiId}")
    public ResponseInfo subApi(@RequestBody JSONObject params, @PathVariable(name = "apiId") String apiId) {
        ResponseInfo info = new ResponseInfo();
        if (StringUtil.isEmpty(params.getString("custId"))) {
            return new ResponseInfoAssemble().failure(-1, "企业id不能为空");
        }

        LoginUser lu = tokenService.opUser();
        try {
            info.setData(apiService.subApi(params, apiId, lu));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseInfoAssemble().failure(-1, "订阅失败");
        }
        return info;
    }

    /**
     * No Subscribe Api
     **/
    @DeleteMapping("/subscription/{apiId}")
    public ResponseInfo subApiUpdate(@RequestBody JSONObject params, @PathVariable(name = "apiId") String apiId) {
        ResponseInfo info = new ResponseInfo();
        if (StringUtil.isEmpty(params.getString("custId"))) {
            return new ResponseInfoAssemble().failure(-1, "企业id不能为空");
        }
        LoginUser lu = tokenService.opUser();
        try {
            info.setData(apiService.subApiUpdate(params, apiId, lu));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseInfoAssemble().failure(-1, "取消订阅失败");
        }
        return info;
    }

    /**
     * Set Api Price
     **/
    @PostMapping("/price/{apiId}")
    public ResponseInfo priceApi(@RequestBody JSONObject params, @PathVariable(name = "apiId") String apiId) {
        ResponseInfo info = new ResponseInfo();
        if (StringUtil.isEmpty(params.getString("price"))) {
            return new ResponseInfoAssemble().failure(-1, "价格不能为空");
        }
        LoginUser lu = tokenService.opUser();
        try {
            info.setData(apiService.priceApi(params, apiId, lu));
        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseInfoAssemble().failure(-1, "销售定价设置失败");
        }
        return info;
    }

    /**
     * api列表（已订阅、未订阅）
     **/
    @PostMapping("/subscribe")
    public ResponseInfo subApiList(@RequestBody JSONObject params) {
        ResponseInfo info = new ResponseInfo();
        LoginUser lu = tokenService.opUser();
        PageParam page = new PageParam();
        if (StringUtil.isEmpty(params.getString("custId"))) {
            return new ResponseInfoAssemble().failure(-1, "企业id不能为空");
        }
        try {
            page.setPageSize(params.getInteger("pageSize") == null ? 0 : params.getIntValue("pageSize"));
            page.setPageNum(params.getInteger("pageNum") == null ? 10 : params.getIntValue("pageNum"));
            if (StringUtil.isNotEmpty(params.getString("code")) && params.getString("code").equals("Subscribe")) {
                info.setData(apiService.subApiSubscribeList(page, params.getString("custId"), params.getString("apiName")));
            } else {
                info.setData(apiService.subApiNoSubscribeList(page, params.getString("custId"), params.getString("apiName")));
            }

        } catch (Exception e) {
            e.printStackTrace();
            return new ResponseInfoAssemble().failure(-1, "获取列表失败");
        }
        return info;
    }


}
