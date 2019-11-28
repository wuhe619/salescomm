package com.bdaim.customs.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseCommon;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.customs.dto.QueryDataParams;
import com.bdaim.customs.entity.HDic;
import com.bdaim.customs.entity.MainDan;
import com.bdaim.customs.services.BgdZService;
import com.bdaim.customs.services.CustomsService;
import com.bdaim.customs.services.ExportExcelService;
import com.bdaim.util.ExcelUtil;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/customs")
public class CustomController extends BasicAction {

    private static Logger log = LoggerFactory.getLogger(CustomController.class);

    @Autowired
    private CustomsService customsService;

    @Autowired
    private ExportExcelService exportExcelService;

    @Autowired
    private BgdZService bgdZService;


    /**
     * 保存数据
     *
     * @param mainDan
     */
    @RequestMapping(value = "saveinfo", method = RequestMethod.POST)
    public ResponseCommon saveMaindan(@RequestBody MainDan mainDan) {
        log.info("saveMaindan::" + JSON.toJSONString(mainDan));
        ResponseCommon responseJson = new ResponseCommon();
        LoginUser user = opUser();
        if (user == null || user.getId() == null) {
            log.error("请先登陆");
            responseJson.setCode(-1);
            responseJson.setMessage("请先登陆");
            return responseJson;
        }
        try {
            customsService.saveinfo(mainDan, user);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
            return responseJson;
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage("保存出错：" + e.getMessage());
            return responseJson;
        }

    }


    /**
     * 根据主单id查询分单列表
     *
     * @param id
     */
    @RequestMapping(value = "/parties/main/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseJson getPartiesByMainId(@PathVariable("id") String id, String type) {
        ResponseJson responseJson = new ResponseJson();
        try {
//            JSONObject json = customsService.getMainDetailById(id, type);
            responseJson.setMessage("SUCCESS");
            responseJson.setCode(200);
//            responseJson.setData(json);
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }

    /**
     * 根据主单id查询详情
     *
     * @param id
     */
    @RequestMapping(value = "main/{id}", method = RequestMethod.GET)
    @ResponseBody
    public ResponseJson getMainDetailById(@PathVariable("id") String id, String type) {
        ResponseJson responseJson = new ResponseJson();
        try {
            LoginUser user = opUser();
            JSONObject json = customsService.getMainDetailById(user.getCustId(), id, type);
            responseJson.setMessage("SUCCESS");
            responseJson.setCode(200);
            responseJson.setData(json);
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }

    /**
     * 编辑主单信息
     *
     * @param mainDan
     * @return
     */
    @RequestMapping(value = "main/{id}", method = RequestMethod.POST)
    @ResponseBody
    public ResponseJson saveMainDetail(@PathVariable("id") String id, @RequestBody MainDan mainDan) {
        ResponseJson responseJson = new ResponseJson();
        try {
            LoginUser user = opUser();
            customsService.saveMainDetail(id, mainDan, user);
            responseJson.setMessage("SUCCESS");
            responseJson.setCode(200);
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }

    /**
     * 删除主单
     *
     * @param id
     * @param type
     * @return
     */
    @RequestMapping(value = "main/{id}", method = RequestMethod.DELETE)
    @ResponseBody
    public ResponseJson deleteMain(@PathVariable("id") Long id, String type) {
        ResponseJson responseJson = new ResponseJson();
        try {
            LoginUser user = opUser();
            customsService.delMainById(id, type, user);
            responseJson.setMessage("SUCCESS");
            responseJson.setCode(200);
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }

    /**
     * 根据分单查询商品列表
     *
     * @param id
     */
    @RequestMapping(value = "party/{id}", method = RequestMethod.POST)
    public void getGoodsByPartyId(@PathVariable("id") String id) {


    }


    /**
     * 根据业务类型、字典类型查询字典数据
     *
     * @param type
     * @param propertyName
     * @return
     */
    @RequestMapping(value = "/diclist", method = RequestMethod.POST)
    public ResponseJson queryDicList(String type, String propertyName) {

        ResponseJson responseJson = new ResponseJson();
        try {
            Map<String, List<Map<String, Object>>> d = customsService.getdicList(type, propertyName);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
            responseJson.setData(d);
            return responseJson;
        } catch (Exception e) {
            log.error("获取失败");
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
            return responseJson;
        }
    }

    /**
     * 分页查询参数
     *
     * @param type
     * @param pageSize
     * @param pageNum
     * @return
     */
    @RequestMapping(value = "page/diclist", method = RequestMethod.GET)
    public ResponseJson getdicPageList(String type, Integer pageSize, Integer pageNum, String propertyName) {
        ResponseJson responseJson = new ResponseJson();
        if (type == null || pageNum == null || pageSize == null) {
            responseJson.setCode(-1);
            responseJson.setMessage("参数错误");
            return responseJson;
        }
        try {
            Page page = customsService.getdicPageList(type, pageSize, pageNum, propertyName);
            responseJson.setData(page);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }

    /**
     * 字典分页
     *
     * @param pageSize
     * @param pageNum
     * @param param
     * @return
     */
    @RequestMapping(value = "/pageDic", method = RequestMethod.POST)
    public ResponseJson pageDic(Integer pageSize, Integer pageNum, HDic param) {
        ResponseJson responseJson = new ResponseJson();
        if (pageNum == null || pageSize == null) {
            responseJson.setCode(-1);
            responseJson.setMessage("参数错误");
            return responseJson;
        }
        try {
            Page page = customsService.pageDic(pageNum, pageSize, param);
            responseJson.setData(page);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }


    /**
     * 添加/编辑参数
     *
     * @param hdic
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "savedic", method = RequestMethod.POST)
    public ResponseJson saveDic(@RequestBody HDic hdic) {
        ResponseJson responseJson = new ResponseJson();
        try {
            customsService.saveDic(hdic);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }

    @RequestMapping(value = "/uploadCardIdPic", method = RequestMethod.POST)
    public ResponseInfo uploadCardIdPic(HttpServletRequest request, String id, Integer type) {
        CommonsMultipartResolver multipartResolver = new CommonsMultipartResolver(
                request.getSession().getServletContext());
        MultipartFile f = null;
        if (multipartResolver.isMultipart(request)) {
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                f = multiRequest.getFile(iter.next());
                break;
            }
        }
        ResponseInfo resp = new ResponseInfo();
        if (f == null) {
            resp.setCode(-1);
            resp.setMessage("文件为空");
            return resp;
        }
        try {
            int status = customsService.uploadCardIdPic(f, id, type, opUser().getCustId());
            if (status == 1) {
                resp.setCode(200);
                resp.setMessage("成功");
            } else {
                resp.setCode(status);
                String msg = "文件异常";
                if (status == -1) {
                    msg = "文件格式不正确";
                } else if (status == -2) {
                    msg = "表头必须包含手机号";
                } else if (status == -3) {
                    msg = "表头为空";
                } else if (status == -4) {
                    msg = "总行数超过限制";
                } else if (status == -5) {
                    msg = "文件为空";
                }
                resp.setMessage(msg);
            }
        } catch (TouchException e) {
            log.error("上传分单身份证照片异常", e);
            resp.setCode(-1);
            resp.setMessage(e.getMessage());
        }
        return resp;
    }


    /**
     * 提交为仓单、报关单
     * <p>
     * to==HAIGUAN 时提交仓单到海关，提交报关单到海关，opType不起作用
     * <p>
     * opType和to不能同时出现
     *
     * @param id
     * @param type
     * @param to
     * @param //optType apd 追加商品时，id为分单id，type为分单类型（SF,BF）；追加分单时，id为主单id,type为主单类型（SZ,BZ）；add 同上
     * @return
     */
    @RequestMapping(value = "/busi/{type}/{id}", method = RequestMethod.POST)
    public ResponseJson commit2cangdanorbaodan(@PathVariable("id") String id, @PathVariable("type") String type,
                                               String to) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser user = opUser();
        if (user == null || user.getCustId() == null) {
            responseJson.setCode(-1);
            responseJson.setMessage("未登陆，或无权限");
            return responseJson;
        }
        try {
            customsService.commit2cangdanorbaodan(id, type, user, to);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }

    /**
     * 查询主单数据列表
     *
     * @param
     */
    @RequestMapping(value = "/be/cd_z/all", method = RequestMethod.POST)
    @ResponseBody
    public ResponseJson getDataList(@RequestBody QueryDataParams queryDataParams) {
        ResponseJson responseJson = new ResponseJson();
        try {
            LoginUser lu = opUser();
            if (!"ROLE_USER".equals(lu.getRole()) || !"admin".equals(lu.getRole())) {
                queryDataParams.setCustId(lu.getCustId());
            }
            queryDataParams.setIndex("SZ");
            Page page = customsService.queryDataPage(queryDataParams);
            // Page page = customsService.getMainList(queryDataParams);
            responseJson.setMessage("SUCCESS");
            responseJson.setCode(200);
            responseJson.setData(page);
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }

    /**
     * 清空分单身份证照片
     *
     * @param id
     * @return
     */
    /*@PostMapping(value = "/clearSFCardIdPic")
    public ResponseJson clearSFCardIdPic(@RequestBody List<Integer> id) {
        ResponseJson responseJson = new ResponseJson();
        int status = customsService.clearSFCardIdPic(id);
        if (status == 1) {
            responseJson.setCode(200);
        } else {
            responseJson.fail();
        }
        return responseJson;
    }*/
    @RequestMapping(value = "/exportExcel", method = RequestMethod.GET)
    public void exportExcel(int id, int type, HttpServletResponse response) {
        try {
            //exportExcelService.exportExcel(id, type, response);
        } catch (Exception e) {
            log.error("导出excel异常", e);
        }
    }

    /**
     * 近6个月的申报单数（按月统计）
     *
     * @param request
     * @param stationId
     * @param custId
     * @return
     */
    @RequestMapping(value = "/count/sbd/ym", method = RequestMethod.GET)
    public ResponseJson countSBD(HttpServletRequest request, String stationId, String custId) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser user = opUser();
        try {
            List<Map<String, Object>> d = customsService.countSBDNumByMonth(stationId, custId, user);
            responseJson.setData(d);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage("查询失败");
        } finally {
            return responseJson;
        }
    }


    /**
     * 最近的申报单数
     *
     * @param request
     * @param stationId
     * @param custId
     * @return
     */
    @RequestMapping(value = "/count/lastest/sbd", method = RequestMethod.GET)
    public ResponseJson sbdH(HttpServletRequest request, String stationId, String custId) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser user = opUser();
        try {
            Map<String, Object> d = customsService.sbdLastestTotal(stationId, custId, user);
            responseJson.setData(d);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage("查询失败");
        } finally {
            return responseJson;
        }
    }

    /**
     * 申报单回执
     *
     * @param request
     * @param stationId
     * @param custId
     * @return
     */
    @RequestMapping(value = "/count/hz/{busitype}", method = RequestMethod.GET)
    public ResponseJson sbdHZCount(HttpServletRequest request, @PathVariable("busitype") String busitype, String stationId, String custId) {
        ResponseJson responseJson = new ResponseJson();
        LoginUser user = opUser();
        try {
            List<Map<String, Object>> d = customsService.hzTotal(busitype, stationId, custId, user);
            responseJson.setData(d);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
        } catch (Exception e) {
            e.printStackTrace();
            responseJson.setCode(-1);
            responseJson.setMessage("查询失败");
        } finally {
            return responseJson;
        }
    }

    @RequestMapping(value = "/readExcel", method = RequestMethod.POST)
    public ResponseJson readExcel(HttpServletRequest request) {
        ResponseJson responseJson = new ResponseJson();
        try {
            Map data = null;
            MultipartHttpServletRequest multiRequest = (MultipartHttpServletRequest) request;
            Iterator<String> iter = multiRequest.getFileNames();
            while (iter.hasNext()) {
                MultipartFile multiRequestFile = multiRequest.getFile(iter.next());
                data = ExcelUtil.readExcel(multiRequestFile);
                break;
            }
            responseJson.setData(data);
            responseJson.setCode(200);
            responseJson.setMessage("SUCCESS");
        } catch (Exception e) {
            responseJson.setCode(-1);
            responseJson.setMessage("查询失败");
        } finally {
            return responseJson;
        }
    }

    /**
     * @param id
     * @param body
     * @param busiType
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "/import/{busiType}", method = RequestMethod.POST)
    public ResponseInfo saveInfo(@PathVariable(name = "id", required = false) Long id, @RequestBody(required = false) String body, @PathVariable(name = "busiType") String busiType) {
        ResponseInfo resp = new ResponseInfo();
        JSONObject info = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";

            info = JSONObject.parseObject(body);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "记录解析异常:[" + busiType + "]");
        }
        try {
            LoginUser lu = opUser();
            String cust_id = lu.getCustId();
            if (StringUtil.isEmpty(cust_id) && StringUtil.isNotEmpty(info.getString("cust_id"))) {
                // 运营后台传参客户ID处理
                cust_id = info.getString("cust_id");
            }
            if (StringUtil.isEmpty(cust_id))
                return new ResponseInfoAssemble().failure(-1, "无归属企业，不能保存记录:[" + busiType + "]");

            String cust_group_id = lu.getUserGroupId();
            Long cust_user_id = lu.getId();
            String rule = info.getString("_rule_");
            // 处理导入报关单退单
            if ("bgd_data_abnormal_handle".equals(rule)) {
                bgdZService.handleAbnormalDan(busiType, cust_id, cust_group_id, cust_user_id, id, info);
            }
            resp.setData(id);
        } catch (TouchException e) {
            return new ResponseInfoAssemble().failure(-1, e.getMessage());
        } catch (Exception e) {
            log.error("保存记录异常:", e);
            return new ResponseInfoAssemble().failure(-1, "保存记录异常:[" + busiType + "]");
        }
        return resp;
    }

    /**
     * 按条件导出excel
     * @param response
     * @param id
     * @param custId
     * @param busiType
     * @param key
     * @param value
     * @param _rule_
     * @return
     */
    @RequestMapping(value = "/exportExcel/{busiType}/{id}", method = RequestMethod.GET)
    public ResponseInfo export(HttpServletResponse response, @PathVariable(name = "id", required = false) Long id, String custId, @PathVariable(name = "busiType") String busiType, String key, String value, String _rule_) {
        ResponseInfo resp = new ResponseInfo();
        try {
            LoginUser lu = opUser();
            String cust_id = lu.getCustId();
            if (StringUtil.isEmpty(cust_id) && StringUtil.isNotEmpty(custId)) {
                // 运营后台传参客户ID处理
                cust_id = custId;
            }
            if (StringUtil.isEmpty(cust_id))
                return new ResponseInfoAssemble().failure(-1, "无归属企业，不能保存记录:[" + busiType + "]");
            String cust_group_id = lu.getUserGroupId();
            Long cust_user_id = lu.getId();
            // 处理导入报关单退单
            if ("bgd_z".equals(busiType)) {
                bgdZService.export(response, cust_id, cust_group_id, cust_user_id, id, key, value, _rule_);
                return null;
            }
            resp.setData(id);
        } catch (Exception e) {
            log.error("保存记录异常:", e);
            return new ResponseInfoAssemble().failure(-1, "保存记录异常:[" + busiType + "]");
        }
        return resp;
    }
}
