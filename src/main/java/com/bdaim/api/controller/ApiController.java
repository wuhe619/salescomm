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
    public ResponseInfo getApi(@PathVariable(name = "apiId") String apiId) {
        ResponseInfo info = new ResponseInfo();


        return info;
    }

    /**
     * Delete Api
     **/
    @DeleteMapping("/info/{apiId}")
    public ResponseInfo deleteApi(@PathVariable(name = "apiId") String apiId, int status) {
        ResponseInfo resp = new ResponseInfo();
        if (StringUtil.isEmpty(apiId) || "0".equals(apiId)) {
            return new ResponseInfoAssemble().failure(-1, "Api删除失败:失败原因apiId无效");
        }
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

        LoginUser lu = tokenService.opUser();

        return info;
    }

    /**
     * No Subscribe Api
     **/
    @DeleteMapping("/subscription/{apiId}")
    public ResponseInfo subApi(@PathVariable(name = "apiId") String apiId) {
        ResponseInfo info = new ResponseInfo();

        LoginUser lu = tokenService.opUser();

        return info;
    }

    /**
     * Set Api Price
     **/
    @PostMapping("/price/{apiId}")
    public ResponseInfo priceApi(@RequestBody JSONObject params, @PathVariable(name = "apiId") String apiId) {
        ResponseInfo info = new ResponseInfo();

        LoginUser lu = tokenService.opUser();

        return info;
    }
}
