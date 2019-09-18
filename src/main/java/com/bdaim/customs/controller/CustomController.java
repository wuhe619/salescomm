package com.bdaim.customs.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.controller.util.ResponseCommon;
import com.bdaim.common.controller.util.ResponseJson;
import com.bdaim.common.dto.Page;
import com.bdaim.common.exception.TouchException;
import com.bdaim.customs.entity.HDic;
import com.bdaim.customs.entity.MainDan;
import com.bdaim.customs.services.CustomsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.commons.CommonsMultipartResolver;

import javax.servlet.http.HttpServletRequest;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/customs")
public class CustomController extends BasicAction {

    private static Logger log = LoggerFactory.getLogger(CustomController.class);

    @Autowired
    private CustomsService customsService;

    /**
     * 保存数据
     *
     * @param mainDan
     */
    @RequestMapping(value = "saveinfo", method = RequestMethod.POST)
    @ResponseBody
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
            responseJson.setCode(-1);
            responseJson.setMessage("保存出错");
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
            JSONObject json = customsService.getMainDetailById(id, type);
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
            customsService.saveMainDetail(id, mainDan);
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
            customsService.delMainById(id, type);
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
    @ResponseBody
    public void getGoodsByPartyId(@PathVariable("id") String id) {


    }


    /**
     * 根据业务类型、字典类型查询字典数据
     *
     * @param type
     * @param propertyName
     * @return
     */
    @ResponseBody
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
     * @param pageNo
     * @return
     */
    @ResponseBody
    @RequestMapping(value = "page/diclist", method = RequestMethod.GET)
    public Page getdicPageList(String type, Integer pageSize, Integer pageNo) {
        return customsService.getdicPageList(type, pageSize, pageNo);
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
    @ResponseBody
    public ResponseJson uploadCardIdPic(HttpServletRequest request, String id, int type) {
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
        ResponseJson responseJson = new ResponseJson();
        if (f == null) {
            responseJson.setCode(0);
            responseJson.setMessage("文件为空");
            return responseJson;
        }
        try {
            int status = customsService.uploadCardIdPic(f, id, type);
            if (status == 1) {
                responseJson.setCode(1);
                responseJson.setMessage("成功");
            } else {
                responseJson.setCode(status);
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
                responseJson.setMessage(msg);
            }
        } catch (TouchException e) {
            log.error("上传分单身份证照片异常", e);
            responseJson.setCode(0);
            responseJson.setMessage(e.getMessage());
        }
        return responseJson;
    }


}
