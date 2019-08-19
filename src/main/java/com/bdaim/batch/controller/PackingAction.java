package com.bdaim.batch.controller;

import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.auth.LoginUser;
import com.bdaim.batch.entity.BatchSendToFile;
import com.bdaim.batch.service.impl.PackingService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.util.StringUtil;

import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Map;

/**
 * @author duanliying
 * @date 2019/1/21
 * 修复接口模拟封装（mac，imei，地址修复）
 * @description
 */
@RestController
@RequestMapping("/receive")
public class PackingAction extends BasicAction {
    @Resource
    PackingService packingService;
    private final static Logger LOG = LoggerFactory.getLogger(PackingAction.class);

    /**
     * 发送mac修复信息
     */
    @RequestMapping(value = "/sendFixfile", method = RequestMethod.POST)
    public String receivefixFile(BatchSendToFile batchSendToFile) {
        JSONObject json = new JSONObject();
        LOG.info(String.valueOf(batchSendToFile));
        if (batchSendToFile.getBatchId() != null && batchSendToFile.getCertList() != null) {
            json.put("errorCode", "00");
            json.put("message", "发送修复数据成功");
            return json.toJSONString();
        }
        json.put("errorCode", "01");
        return json.toJSONString();
    }

    /**
     * 获取mac修复结果
     */
    @RequestMapping(value = "/getMacFixResult", method = RequestMethod.POST)
    public String receivefixFile(String batchId) {
        JSONObject json = new JSONObject();
        LOG.info("获取修复数据的batchId是：" + batchId);
        batchId = "1542005544857";
        List<Map<String, Object>> list = packingService.getMacResoult(batchId);
        if (list.size() > 0) {
            json.put("code", "001");
            json.put("list", list);
        }
        return json.toJSONString();
    }

    /**
     * 发送快递(确认发件)
     *
     * @param map
     * @return
     * @auther Chacker
     * @date 2019/8/7 13:52
     */
    @RequestMapping(value = "/sendExpress", method = RequestMethod.POST)
    public ResponseInfo sendExpress(@RequestParam Map<String, Object> map) {
        LOG.info("进入发送快递的接口 sendExpress 入参为 "+map.toString());
        packingService.sendExpress(map);
        return new ResponseInfoAssemble().success(null);
    }

    /**
     * 批量发送快递接口
     */
    @RequestMapping(value = "/sendAllExpress", method = RequestMethod.GET)
    public String sendExpress(String batchId) {
        JSONObject json = new JSONObject();
        LoginUser lu = opUser();
        String custId = opUser().getCustId();
        Long userId = opUser().getId();
        LOG.info("发送快递参数是：" + "batchId\t" + batchId);
        if (StringUtil.isEmpty(batchId)) {
            json.put("code", "001");
            json.put("message", "缺少必要参數");
            return json.toJSONString();
        } else {
            packingService.sendAllExpress(batchId, custId, userId);
            json.put("code", "000");
            json.put("message", "批量快递发送成功");
            return json.toJSONString();
        }
    }

    /**
     * 接收地址修复发送结果
     *
     * @param batchSendToFile
     * @return
     */
    @RequestMapping(value = "/sendAddressFixfile", method = RequestMethod.POST)
    public String sendAddressfixFile(BatchSendToFile batchSendToFile) {
        JSONObject json = new JSONObject();
        LOG.info(String.valueOf(batchSendToFile));
        if (batchSendToFile.getBatchId() != null && batchSendToFile.getCertList() != null) {
            json.put("errorCode", "00");
            json.put("message", "发送修复数据成功");
            return json.toJSONString();
        }
        json.put("errorCode", "01");
        return json.toJSONString();
    }

    /**
     * 接收imei发送结果
     *
     * @param batchSendToFile
     * @return
     */
    @RequestMapping(value = "/sendImeiFixfile", method = RequestMethod.POST)
    public String receiveImeifixFile(BatchSendToFile batchSendToFile) {
        JSONObject json = new JSONObject();
        LOG.info(String.valueOf(batchSendToFile));
        if (batchSendToFile.getBatchId() != null && batchSendToFile.getCertList() != null) {
            json.put("errorCode", "00");
            json.put("message", "发送修复数据成功");
            return json.toJSONString();
        }
        json.put("errorCode", "01");
        return json.toJSONString();
    }

    /**
     * 获取imei修复数据
     */
    @RequestMapping(value = "/getImeiResult", method = RequestMethod.POST)
    public String getImeiFixFile(String batchId) {

        JSONObject json = new JSONObject();
        if (batchId != null && StringUtil.isNotEmpty(batchId)) {
            json.put("errorCode", "00");
            json.put("message", "发送修复数据成功");
            return json.toJSONString();
        }
        List<Map<String, Object>> list = packingService.getImeiResoult();
        if (list.size() > 0) {
            json.put("code", "001");
            json.put("list", list);
        }
        return json.toJSONString();
    }

    /**
     * 统计可以发送快递个数
     */
    @RequestMapping(value = "/getSendNum", method = RequestMethod.GET)
    public String countSendNumber(String batchId) {
        JSONObject json = new JSONObject();
        int countNumber = 0;
        try {
            countNumber = packingService.countNumber(batchId);
            json.put("code", "000");
            json.put("count", countNumber);
        } catch (Exception e) {
            LOG.info("查询发送数量异常" + e);
            json.put("code", "001");
        }
        return json.toJSONString();
    }

    //查询resource_id
    @RequestMapping(value = "/query", method = RequestMethod.GET)
    public List<Map<String, Object>> query(String supplier_id, int type_code) {
        List<Map<String, Object>> list = packingService.query(supplier_id, type_code);
        return list;
    }
}
