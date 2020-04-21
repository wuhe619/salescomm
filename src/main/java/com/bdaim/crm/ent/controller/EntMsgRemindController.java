package com.bdaim.crm.ent.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.be.service.BusiEntityService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.crm.ent.service.EntMsgRemindService;
import com.bdaim.customs.entity.BusiTypeEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @description 消息提醒
 * @date 2020/4/20 16:44
 */
@RestController
@RequestMapping("/ent/message")
public class EntMsgRemindController extends BasicAction {

    private static Logger logger = LoggerFactory.getLogger(EntMsgRemindController.class);

    @Autowired
    EntMsgRemindService entMsgRemindService;

    @Autowired
    BusiEntityService busiEntityService;

    @PostMapping(value = "/unread")
    public ResponseInfo unread(@RequestBody(required = false) String body, HttpServletResponse response) throws Exception {
        String busiType = BusiTypeEnum.ENT_MSG_REMIND.getType();
        ResponseInfo resp = new ResponseInfo();
        JSONObject params = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";
            params = JSONObject.parseObject(body);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "查询条件解析异常[" + busiType + "]");
        }
        List<Map<String, Object>> data;
        try {
            data = entMsgRemindService.unReadMsgCount(busiType, opUser().getCustId(), opUser().getUserGroupId(), opUser().getId(), params);
        } catch (Exception e) {
            logger.error("查询记录异常,", e);
            return new ResponseInfoAssemble().failure(-1, "查询记录异常[" + busiType + "]");
        }
        resp.setData(data);
        return resp;
    }


    @PostMapping(value = "/markread")
    public ResponseInfo markRead(@RequestBody(required = false) String body) {
        String busiType = BusiTypeEnum.ENT_MSG_REMIND.getType();
        ResponseInfo resp = new ResponseInfo();
        JSONObject params = null;
        try {
            if (body == null || "".equals(body))
                body = "{}";
            params = JSONObject.parseObject(body);
        } catch (Exception e) {
            return new ResponseInfoAssemble().failure(-1, "查询条件解析异常[" + busiType + "]");
        }
        int data;
        try {
            data = entMsgRemindService.readMsg(params);
        } catch (Exception e) {
            logger.error("标记已读消息异常,", e);
            return new ResponseInfoAssemble().failure(-1, "标记已读消息异常[" + busiType + "]");
        }
        resp.setData(data);
        return resp;
    }
}
