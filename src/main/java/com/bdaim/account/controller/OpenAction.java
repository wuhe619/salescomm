package com.bdaim.account.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.account.service.OpenService;
import com.bdaim.batch.dao.BatchDetailDao;
import com.bdaim.batch.dto.FixInfo;
import com.bdaim.batch.entity.BatchDetail;
import com.bdaim.callcenter.dto.RecordVoiceQueryParam;
import com.bdaim.common.annotation.CacheAnnotation;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.util.AuthPassport;
import com.bdaim.common.util.ConfigUtil;
import com.bdaim.common.util.IDHelper;
import com.bdaim.common.util.StringUtil;
import com.bdaim.customer.entity.CustomerUserDO;
import com.bdaim.rbac.dto.Page;
import com.bdaim.resource.service.MarketResourceService;
import com.bdaim.template.dto.TemplateParam;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/3/25
 * 对外接口Action
 */
@RestController
@RequestMapping("/open")
public class OpenAction extends BasicAction {

    private final static Logger log = LoggerFactory.getLogger(OpenAction.class);
    @Resource
    private OpenService openService;
    @Resource
    private MarketResourceService marketResourceService;
    @Resource
    private BatchDetailDao batchDetailDao;

    /**
     * 账户余额查询
     *
     * @author:duanliying
     * @date: 2019/3/25 9:31
     */
    @AuthPassport
    @RequestMapping(value = "/getBalance", method = RequestMethod.POST)
    @ResponseBody
    public String updateMainNumber() {
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        log.info("当前登录的企业id是 ： " + custId);
        //根据当前企业id查询企业余额
        Map<String, Object> map = null;
        try {
            map = openService.queryCustBalance(custId);
            map.put("status", "000");
        } catch (Exception e) {
            log.error("查询企业余额异常 ： " + e);
            map.put("status", "001");
        }
        return returnJsonData(map);
    }

    /**
     * 刷新token
     *
     * @author:duanliying
     * @method
     * @date: 2019/3/25 11:10
     */
    @RequestMapping(value = "/refreshToken", method = RequestMethod.POST)
    @ResponseBody
    public Object refreshToken(@RequestBody JSONObject param) {
        String oldtoken = param.getString("oldtoken");
        String username = param.getString("username");
        String refreshToken = openService.refreshToken(oldtoken, username);
        return refreshToken;
    }

    /**
     * 根据坐席账号查询坐席详情
     *
     * @author:duanliying
     * @date: 2019/3/25 11:10
     */
    @AuthPassport
    @RequestMapping(value = "/getSeatMessage", method = RequestMethod.POST)
    @ResponseBody
    public String getSeatMessage(@RequestBody JSONObject param) {
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        Map<String, Object> map = new HashMap<>();
        try {
            String seatAccount = param.getString("seatAccount");
            if (StringUtil.isEmpty(seatAccount)) {
                map.put("status", "001");
                // map.put("msg", "坐席账号不能为空");
                return returnJsonData(map);
            }
            map = openService.getSeatMessage(seatAccount, custId);
        } catch (Exception e) {
            map.put("status", "004");
            // map.put("msg", "坐席详细信息查询失败");
            log.error("坐席详细信息查询异常 ： " + e);
        }
        return returnJsonData(map);
    }

    /**
     * 根据账号修改坐席主叫号码
     *
     * @author:duanliying
     * @date: 2019/3/25 11:10
     */
    @AuthPassport
    @RequestMapping(value = "/updateMainNumber", method = RequestMethod.POST)
    @ResponseBody
    public String updateMainNumber(@RequestBody JSONObject param) {
        Map<String, Object> map = new HashMap<>();
        String seatAccount = param.getString("seatAccount");
        String mainNumber = param.getString("mainNumber");
        if (StringUtil.isEmpty(seatAccount) || StringUtil.isEmpty(mainNumber)) {
            map.put("status", "001");
            //   map.put("msg", "缺少必要参数");
            return returnJsonData(map);
        }
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        try {
            map = openService.updateMainNumber(seatAccount, custId, mainNumber);
        } catch (Exception e) {
            map.put("status", "005");
            //   map.put("msg", "修改坐席主叫号码失败");
            log.error("修改坐席主叫号码异常 ： " + e);
        }
        return returnJsonData(map);
    }

    /**
     * 创建与修改短信邮件模板内容
     *
     * @author:duanliying
     * @date: 2019/3/25 16:11
     */
    @AuthPassport
    @RequestMapping(value = "/addSmsTemplate", method = RequestMethod.POST)
    @ResponseBody
    public String insertSmsTemplate(@RequestBody TemplateParam templateParam) {
        Map<String, Object> map = new HashMap<>();
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        //模板名字
        String templateName = templateParam.getTemplateName();
        //模板内容
        String templateContent = templateParam.getTemplateContent();
        //模板签名
        String smsSignatures = templateParam.getSmsSignatures();
        if (StringUtil.isEmpty(templateName) || StringUtil.isEmpty(templateContent) || StringUtil.isEmpty(smsSignatures)) {
            map.put("status", "001");
            // map.put("msg", "缺少必要参数");
            return returnJsonData(map);
        }
        String custId = u.getCust_id();
        Integer id = null;
        try {
            id = openService.insertSmsTemplate(templateName, templateContent, smsSignatures, custId);
            map.put("status", "000");
            // map.put("msg", "模板创建成功");
            map.put("id", id);
        } catch (Exception e) {
            map.put("status", "002");
            //map.put("msg", "短信模板创建失败");
            log.error("短信模板创建异常 ： " + e);
        }

        return returnJsonData(map);
    }

    /**
     * 根据模板id查询内容
     *
     * @author:duanliying
     * @date: 2019/3/25 16:11
     */
    @AuthPassport
    @RequestMapping(value = "/getSmsTemplate", method = RequestMethod.POST)
    @ResponseBody
    public String querySmsTemplate(@RequestBody TemplateParam templateParam) {
        Map<String, Object> map = new HashMap<>();
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String templateId = templateParam.getTemplateId();
        if (StringUtil.isEmpty(templateId)) {
            map.put("status", "001");
            //map.put("msg", "缺少必要参数");
            return returnJsonData(map);
        }
        String custId = u.getCust_id();
        try {
            List<Map<String, Object>> list = openService.querySmsTemplate(templateId, custId);
            if (list.size() > 0) {
                map.put("status", "000");
                // map.put("msg", "短信模板查询成功");
                map.put("list", list.get(0));
            }
        } catch (Exception e) {
            map.put("status", "002");
            //map.put("msg", "查询失败");
            log.error("短信模板查询异常 ： " + e);
        }

        return returnJsonData(map);
    }

    /**
     * 获取token
     *
     * @author:duanliying
     * @method
     * @date: 2019/3/27 16:04
     */
    @RequestMapping(value = "/tokenInfoGet", method = RequestMethod.POST)
    @ResponseBody
    public Object tokenInfoGet(@RequestBody JSONObject param) {
        Map<String, Object> resultMap = new HashMap<>();
        String username = param.getString("username");
        String password = param.getString("password");
        resultMap = openService.getTokenInfo(username, password);
        return returnJsonData(resultMap);
    }

    /**
     * 失联修复数据上传接口
     */
    @AuthPassport
    @RequestMapping(value = "/uploadData", method = RequestMethod.POST)
    @ResponseBody
    public String FixParse(@RequestBody FixInfo fixInfo, HttpServletRequest request) {
        Map<String, Object> resultMap = new HashMap<>();
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String compId = u.getCust_id();
        Long id = u.getId();
        String realname = u.getRealname();
        resultMap = openService.insertFixData(fixInfo, compId, id, realname);
        return returnJsonData(resultMap);
    }

    /**
     * @description 根据批次ID查询批次下的客户集合(对外接口)
     * @author:duanliying
     * @method
     * @date: 2018/9/6 14:08
     */
    @AuthPassport
    @RequestMapping(value = "/customerList", method = RequestMethod.POST)
    @ResponseBody
    public Object queryCustomerListById(@RequestBody JSONObject param) {
        Map<Object, Object> map = new HashMap<>();
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String compId = u.getCust_id();
        String batchId = param.getString("batchId");
        Integer pageSize = param.getInteger("pageSize");
        Integer pageNumber = param.getInteger("pageNum");
        if (StringUtil.isEmpty(batchId)) {
            map.put("msg", "batchId不能为空");
            map.put("status", "001");
            return returnJsonData(map);
        }
        Page list = openService.getDetailListById(pageNumber, pageSize, batchId, compId);
        return returnJsonData(getPageData(list));
    }

    /**
     * @description 查询单条通话记录（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/21 9:06
     */
    @AuthPassport
    @RequestMapping(value = "/getCallLog", method = RequestMethod.POST)
    @ResponseBody
    public String getSingleVoicelog(@RequestBody Map<String, Object> params) {
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        Map<String, Object> map = new HashMap<>();
        String touchId = String.valueOf(params.get("touchId"));
        log.info("查询单条通话记录接口touchId是：" + touchId);
        if (StringUtil.isEmpty(touchId)) {
            // map.put("msg", "touchId不能为空");
            map.put("status", "001");
            return returnJsonData(map);
        }
        try {
            map = openService.querySingleVoicelog(touchId, custId);
            map.put("status", "000");
        } catch (Exception e) {
            log.error("查询单条通话记录异常" + e);
            map.put("status", "002");
        }
        return returnJsonData(map);
    }


    /**
     * @description 坐席外呼接口（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/22 16:41
     */
    @AuthPassport
    @RequestMapping(value = "/callCenter", method = RequestMethod.POST)
    @ResponseBody
    public String unicomSeatMakeCall(@RequestBody Map<String, Object> params) {
        Map<String, Object> map = new HashMap<>();
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        log.info("对外接口---外呼接口调用参数是：" + params.toString());
        String id = String.valueOf(params.get("superId"));
        String batchId = String.valueOf(params.get("batchId"));
        String mainNumber = String.valueOf(params.get("mainNumber"));
        String apparentNumber = String.valueOf(params.get("apparentNumber"));
        String channel = String.valueOf(params.get("channel"));
        String seatAccount = String.valueOf(params.get("seatAccount"));
        map = openService.seatCallCenter(id, batchId, mainNumber, apparentNumber, channel, seatAccount, custId);
        return returnJsonData(map);
    }

    /**
     * @description 查询单条短信记录（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/21 9:06
     */
    @AuthPassport
    @RequestMapping(value = "/getSingleSmslog", method = RequestMethod.POST)
    @ResponseBody
    public String getSingleSmslog(@RequestBody Map<String, Object> params) {
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        Map<String, Object> map = new HashMap<>();
        String touchId = String.valueOf(params.get("touchId"));
        if (StringUtil.isEmpty(touchId)) {
            // map.put("msg", "touchId不能为空");
            map.put("status", "001");
            return returnJsonData(map);
        }
        try {
            map = openService.querySingleSmslog(touchId, custId);
        } catch (Exception e) {
            log.error("查询单条短信记录异常" + e);
            // map.put("msg", "查询失败");
            map.put("status", "002");
        }
        //map.put("msg", "查询成功");
        map.put("status", "000");
        return returnJsonData(map);
    }

    /**
     * @description 根据查询企业下批次列表（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/22 17:46
     */
    @AuthPassport
    @ResponseBody
    @RequestMapping(value = "/getBatchList", method = RequestMethod.POST)
    public String getBatchList(@RequestBody JSONObject param) {
        int pageNum = param.getIntValue("pageNum");
        int pageSize = param.getIntValue("pageSize");
        Map<String, Object> map = new HashMap<>();
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        Page list = null;
        try {
            list = openService.queryList(pageNum, pageSize, custId);
        } catch (Exception e) {
            log.error("获取批次列表异常" + e);
        }
        return returnJsonData(getPageData(list));
    }


    /**
     * 通话记录查询接口（对外接口）
     */
    @AuthPassport
    @RequestMapping(value = "/queryVoicelog", method = RequestMethod.POST)
    @ResponseBody
    @CacheAnnotation
    public String queryRecordVoicelog(@RequestBody JSONObject param) {
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        Map<String, Object> map = new HashMap<>();
        int pageNum = param.getInteger("pageNum");
        int pageSize = param.getInteger("pageSize");
        Page list = null;
        try {
            list = openService.queryRecordVoicelog(pageNum, pageSize, custId);
        } catch (Exception e) {
            log.error("通话记录查询接口异常" + e);
        }
        return returnJsonData(getPageData(list));
    }


    /**
     * 短信历史查询(对外接口)
     */
    @AuthPassport
    @RequestMapping(value = "/getSmslog", method = RequestMethod.POST)
    @ResponseBody
    public String getSmslog(@RequestBody JSONObject param) {
        int pageNum = param.getInteger("pageNum");
        int pageSize = param.getInteger("pageSize");
        Map<String, Object> map = new HashMap<>();
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        JSONObject json = new JSONObject();
        Page list = null;
        try {
            list = openService.openSmsHistory(pageNum, pageSize, custId);
            map.put("msg", "查询成功");
            map.put("status", "000");
        } catch (Exception e) {
            log.error("短信历史查询异常" + e);
            map.put("msg", "查询失败");
            map.put("status", "001");
        }
        json.put("data", list);
        return returnJsonData(getPageData(list));
    }

    /**
     * @description 发送短信（对外接口）
     * @author:duanliying
     * @method
     * @date: 2018/11/19 10:06
     */
    @AuthPassport
    @RequestMapping(value = "/sendSms", method = RequestMethod.POST)
    @ResponseBody
    public String sendMessageData(@RequestBody Map<String, Object> params) {
        CustomerUserDO u = (CustomerUserDO) request.getAttribute("customerUserDO");
        String custId = u.getCust_id();
        Map<String, Object> map = new HashMap<>();
        String batchId = String.valueOf(params.get("batchId"));
        String templateId = String.valueOf(params.get("templateId"));
        String channel = String.valueOf(params.get("channel"));
        //模板里面占位的值
        String variables = String.valueOf(params.get("variables"));
        String customerId = String.valueOf(params.get("superId"));
        //坐席账号用于获取userId
        String seatAccount = String.valueOf(params.get("seatAccount"));
        if (StringUtil.isEmpty(seatAccount) || StringUtil.isEmpty(batchId) || StringUtil.isEmpty(templateId) || StringUtil.isEmpty(channel) || StringUtil.isEmpty(customerId)) {
            map.put("status", "003");
            return returnJsonData(map);
        }
        map = openService.sendSmsMessage(batchId, templateId, channel, variables, customerId, seatAccount, custId);
        return returnJsonData(map);
    }
    @RequestMapping(value = "/testBatchDetail")
    public ResponseInfo testBatchDetail(@RequestBody Map map){
        String id = String.valueOf(map.get("id"));
        String batchId = String.valueOf(map.get("batch_id"));
        BatchDetail batchDetail = batchDetailDao.getBatchDetail(id,batchId);
        return new ResponseInfoAssemble().success(batchDetail);
    }
    /**
     * 录音文件地址查询
     */
    @AuthPassport
    @RequestMapping(value = "/getRecordUrl", method = RequestMethod.POST)
    @ResponseBody
    public String downloadSound(@RequestBody JSONObject param) {
        Map<String, Object> map = new HashMap<>();
        String basePath = ConfigUtil.getInstance().get("audio_server_url") + "/";
        RecordVoiceQueryParam recordVoiceQueryParam = new RecordVoiceQueryParam();
        String touchId = param.getString("touchId");
        recordVoiceQueryParam.setTouchId(touchId);
        List<Map<String, Object>> resultList = marketResourceService.soundUrl(recordVoiceQueryParam);
        String userId = "", targetPath = "", fileName = "";
        try {
            if (resultList != null && resultList.size() > 0) {
                if (resultList.get(0).get("recordurl") != null) {
                    fileName = resultList.get(0).get("recordurl").toString();
                    if (StringUtil.isNotEmpty(fileName)) {
                        fileName = fileName.substring((fileName.lastIndexOf("/") + 1), (fileName.length()));
                        log.info("文件名是：" + fileName);
                    }
                }
                if (resultList.get(0).get("user_id") != null) {
                    userId = resultList.get(0).get("user_id").toString();
                    log.info("客户id是：" + userId);
                }
            }
            if (StringUtil.isNotEmpty(fileName) && StringUtil.isNotEmpty(userId)) {
                targetPath = basePath + userId + "/" + fileName;
                map.put("url", targetPath);
                map.put("status", "000");
            } else {
                map.put("url", targetPath);
                map.put("status", "001");
            }
        } catch (Exception e) {
            log.error("查询录音地址异常" + e);
            map.put("url", targetPath);
            map.put("status", "002");
        }
        //本地   online.datau.top/audio + 用户id + 回调地址最后一个分割符
        //String targetPath = "http://online.datau.top/audio/1810090141300001/TEL-18630016545_BJHKK_zx1_20181010192854.wav";
        log.info("文件名：" + fileName + "文件路径：" + targetPath);
        return returnJsonData(map);
    }

    /**
     * 保存用户的行为记录
     *
     * @param map
     * @return
     * @auther Chacker
     * @date
     */
    @RequestMapping(value = "/saveActionRecord", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo saveActionRecord(@RequestBody Map<String, Object> map,HttpServletRequest request) {
        log.info("进入保存用户的行为记录接口 saveActionRecord");
        log.info("入参值为"+map.toString());
        openService.saveActionRecord(map,request);
        return new ResponseInfoAssemble().success(null);
    }

    /**
     * 保存用户的来访渠道 ETC/XIAOXIONG
     *
     * @param
     * @return
     * @auther Chacker
     * @date
     */
    @RequestMapping(value = "/saveAccessChannels", method = RequestMethod.POST)
    @ResponseBody
    public ResponseInfo saveAccessChannels(@RequestBody Map<String, Object> map, HttpServletRequest request) {
        log.info("进入保存用户来访渠道接口 saveAccessChannels");
        log.info("入参值为"+map.toString());
        ResponseInfo result = openService.saveAccessChannels(map, request);
        return result;
    }

    /**
     * 获取地址修复数据
     */
    @RequestMapping(value = "/addressFixMessage", method = RequestMethod.POST)
    public ResponseInfo addressfixFile(String idcard) {
        List<Map<String, Object>> list = openService.getAddressResoult(idcard);
        if (list.size() > 0) {
            for (int i = 0; i < list.size(); i++) {
                list.get(i).put("addressid", String.valueOf(IDHelper.getTransactionId()));
                list.get(i).put("prov", "北京");
            }
        }
        return new ResponseInfoAssemble().success(list);
    }

    /**
     * 对中通快递提供此接口，接收快递管家返回的运单号和快递公司名称
     *
     * @param map billNo 运单号 expressCompany 快递公司名称 orderCode 商家订单号
     * @return
     * @auther Chacker
     * @date
     */
    @RequestMapping(value = "/saveBillNo", method = RequestMethod.POST)
    @ResponseBody
    public Map<String,Object> saveBillNo(@RequestParam Map<String, Object> map) {
        log.info("进入saveBillNo 接收运单号接口，入参为" + map.toString());
        Map<String, Object> result = openService.saveBillNo(map);
        return result;
    }
}

