package com.bdaim.api.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.AppConfig;
import com.bdaim.api.Dto.ApiData;
import com.bdaim.api.service.ApiService;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.PageParam;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.util.DateUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;

@RestController
@RequestMapping("/api")
public class ApiController extends BasicAction {
    private static Logger logger = LoggerFactory.getLogger(ApiController.class);

    @Autowired
    private ApiService apiService;


    /**
     * Query Api Infos
     **/
    @PostMapping("/infos")
    public ResponseInfo apis(@RequestBody JSONObject params) {
        LoginUser lu = opUser();

        ResponseInfo resp = new ResponseInfo();
        PageParam page = new PageParam();
        page.setPageSize(params.containsKey("pageSize") ? 0 : params.getIntValue("pageSize"));
        page.setPageNum(params.containsKey("pageNum") ? 10 : params.getIntValue("pageNum"));
        resp.setData(apiService.apis(page, params));
        return resp;
    }

    /**
     * Save Api
     **/
    @PostMapping("/info/{apiId}")
    public ResponseInfo saveApi(@RequestBody(required = false) ApiData apiData, @PathVariable(name = "apiId") String apiId,Integer status) throws Exception {
        LoginUser lu = opUser();
        if (lu == null || lu.getAuths() == null || !lu.getAuths().contains("admin"))
            throw new Exception("no auth");

        ResponseInfo resp = new ResponseInfo();
        try {
            if(!"0".equals(apiId) && status!=null){
                apiService.updateStatusApiById(apiId, lu, status);
            }else{
                resp.setData(apiService.saveApiProperty(apiData, apiId, lu));
            }
        } catch (Exception e) {
            logger.error("api保存失败", e);
            return new ResponseInfoAssemble().failure(-1, "Api操作失败:");
        }

        return resp;
    }

    /**
     * Get Api
     **/
    @GetMapping("/info/{apiId}")
    public ResponseInfo getApi(@PathVariable(name = "apiId") Integer apiId) {
        LoginUser lu = opUser();

        ResponseInfo resp = new ResponseInfo();
        if (apiId == null || apiId == 0) {
            return new ResponseInfoAssemble().failure(-1, "Api创建失败:");
        }
        try {
            resp.setData(apiService.getApiById(apiId));
        } catch (Exception e) {
            logger.error("api查询失败", e);
            return new ResponseInfoAssemble().failure(-1, "api查询失败");
        }
        return resp;
    }

    /**
     * Delete Api
     **/
    @DeleteMapping("/info/{apiId}")
    public ResponseInfo deleteApi(@PathVariable(name = "apiId") String apiId, int status) throws Exception {
        LoginUser lu = opUser();
        if (lu == null || lu.getAuths() == null || !lu.getAuths().contains("admin"))
            throw new Exception("no auth");

        ResponseInfo resp = new ResponseInfo();
        try {
            apiService.updateStatusApiById(apiId, lu, status);
        } catch (Exception e) {
            logger.error("api删除失败", e);
            return new ResponseInfoAssemble().failure(-1, "Api删除失败:失败原因API不存在");
        }

        return resp;
    }


    /**
     * Subscribe Api
     **/
    @PostMapping("/subscription/{apiId}")
    public ResponseInfo subApi(@RequestBody JSONObject params, @PathVariable(name = "apiId") String apiId) throws Exception {
        LoginUser lu = opUser();
        if (lu == null || lu.getAuths() == null || !lu.getAuths().contains("admin"))
            throw new Exception("no auth");

        ResponseInfo info = new ResponseInfo();
        if (!params.containsKey("custId")) {
            return new ResponseInfoAssemble().failure(-1, "企业id不能为空");
        }
        logger.info("开始订阅");
        try {
            info.setData(apiService.subApi(params, apiId, lu));
        } catch (Exception e) {
            logger.error("订阅api失败" ,e);
            info.setCode(-1);
            info.setMessage("订阅失败");
        }
        return info;
    }

    /**
     * No Subscribe Api
     **/
    @DeleteMapping("/subscription/{apiId}")
    public ResponseInfo subApiUpdate(@RequestBody JSONObject params, @PathVariable(name = "apiId") String apiId) throws Exception {
        LoginUser lu = opUser();
        if (lu == null || lu.getAuths() == null || !lu.getAuths().contains("admin"))
            throw new Exception("no auth");

        ResponseInfo info = new ResponseInfo();
        if (!params.containsKey("custId")) {
            return new ResponseInfoAssemble().failure(-1, "企业id不能为空");
        }
        try {
            info.setData(apiService.subApiUpdate(params, apiId, lu));
        } catch (Exception e) {
            logger.error("取消订阅失败", e);
            return new ResponseInfoAssemble().failure(-1, "取消订阅失败");
        }
        return info;
    }

    /**
     * Set Api Price
     **/
    @PostMapping("/price/{apiId}")
    public ResponseInfo priceApi(@RequestBody JSONObject params, @PathVariable(name = "apiId") String apiId) throws Exception {
        LoginUser lu = opUser();
        if (lu == null || lu.getAuths() == null || !lu.getAuths().contains("admin"))
            throw new Exception("no auth");

        ResponseInfo info = new ResponseInfo();
        if (!params.containsKey("price")) {
            return new ResponseInfoAssemble().failure(-1, "价格不能为空");
        }
        try {
            info.setData(apiService.priceApi(params, apiId, lu));
        } catch (Exception e) {
            logger.error("销售定价设置失败", e);
            return new ResponseInfoAssemble().failure(-1, "销售定价设置失败");
        }
        return info;
    }

    /**
     * api列表（已订阅、未订阅）
     **/
    @PostMapping("/subscribe")
    public ResponseInfo subApiList(@RequestBody JSONObject params) throws Exception {
        LoginUser lu = opUser();
       /* if (lu == null || lu.getAuths() == null || !lu.getAuths().contains("admin"))
            throw new Exception("no auth");*/

        ResponseInfo info = new ResponseInfo();
        PageParam page = new PageParam();
        try {
            String custId = null;
            String apiName = null;
            if (params.containsKey("custId"))
                custId = params.getString("custId");
            if (params.containsKey("apiName"))
                apiName = params.getString("apiName");
            page.setPageSize(!params.containsKey("pageSize") ? 0 : params.getIntValue("pageSize"));
            page.setPageNum(!params.containsKey("pageNum") ? 10 : params.getIntValue("pageNum"));
            if (params.containsKey("code") && params.getString("code").equals("Subscribe")) {
                info.setData(apiService.subApiSubscribeList(page, custId, apiName));
            } else {
                info.setData(apiService.subApiNoSubscribeList(page, custId, apiName));
            }

        } catch (Exception e) {
            logger.error("获取api列表失败", e);
            return new ResponseInfoAssemble().failure(-1, "获取列表失败");
        }
        return info;
    }

    /**
     * Query Api logs of customers
     **/
    @PostMapping("/{apiId}/logs")
    public ResponseInfo apiLogs(@RequestBody JSONObject params,@PathVariable("apiId")String apiId) {
       // JSONObject params=JSONObject.parseObject(paramsStr);
        LoginUser lu = opUser();
        params.put("apiId",apiId);
        ResponseInfo resp = new ResponseInfo();
        PageParam page = new PageParam();
        page.setPageSize(params.containsKey("pageSize") ? 0 : params.getIntValue("pageSize"));
        page.setPageNum(params.containsKey("pageNum") ? 10 : params.getIntValue("pageNum"));
        resp.setData(apiService.apiLogs(page, params));
        return resp;
    }



    /**
     * Query Api log detail of customers
     **/
    @PostMapping("/{apiId}/logs/{customerId}")
    public ResponseInfo apiCustomerLogs(@RequestBody JSONObject params,@PathVariable("apiId")String apiId,@PathVariable("customerId")String customerId) {
        LoginUser lu = opUser();
        params.put("apiId",apiId);
        params.put("customerId",customerId);
        ResponseInfo resp = new ResponseInfo();
        PageParam page = new PageParam();
        page.setPageSize(params.containsKey("pageSize") ? 0 : params.getIntValue("pageSize"));
        page.setPageNum(params.containsKey("pageNum") ? 10 : params.getIntValue("pageNum"));
        resp.setData(apiService.apiCustomerLogs(page, params));
        return resp;
    }

    /**
     * 下载api参数模板
     * @param request
     * @param response
     * @param fileName
     * @return
     */
    @GetMapping("/downloadModel/{fileName:.+}")
    public String downloadAPIMode(HttpServletRequest request, HttpServletResponse response,@PathVariable String fileName) {
        InputStream in = null;
        OutputStream bos = null;
        try {
            logger.info("fileName== "+fileName);
            String classPath = AppConfig.getFile_path();
            logger.info("hello classpath" + classPath);
            String pathF = PROPERTIES.getProperty("file.separator");
            classPath = classPath.replace("/", pathF);
            String path = classPath + pathF + "tp" + pathF + fileName;
            response.setCharacterEncoding("utf-8");
            response.setContentType("application/vnd.ms-excel;charset=utf-8");
            String returnName = response.encodeURL(new String(fileName.getBytes(), "iso8859-1"));   //保存的文件名,必须和页面编码一致,否则乱码
            response.addHeader("Content-Disposition", "attachment;filename=" + returnName);
            in = new FileInputStream(path);
            bos = response.getOutputStream();
            byte[] b = new byte[2048];
            int length;
            while ((length = in.read(b)) > 0) {
                bos.write(b, 0, length);
            }
            bos.flush();
            return "ok";
        } catch (Exception e) {
            logger.error("api模板下载异常" + "\r\n" + e.getMessage());
            return "error";
        } finally {
            try {
                in.close();
                bos.close();
                logger.info("模板文件下载成功" + "\t" + DateUtil.getTimestamp(new Date(System.currentTimeMillis()), DateUtil.YYYY_MM_DD_HH_mm_ss));
                return "ok";
            } catch (Exception e) {
                logger.error("io资源释放异常" + "\r\n" + e.getMessage());
                return "error";
            }
        }
    }

}
