package com.bdaim.crm.ent.controller;

import com.alibaba.fastjson.JSONObject;
import com.bdaim.be.service.BusiEntityService;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.common.dto.Page;
import com.bdaim.common.response.ResponseInfo;
import com.bdaim.common.response.ResponseInfoAssemble;
import com.bdaim.common.service.BusiService;
import com.bdaim.crm.ent.service.EntDataService;
import com.bdaim.crm.ent.service.EntMsgRemindService;
import com.bdaim.customs.entity.BusiTypeEnum;
import com.bdaim.util.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * @author chengning@salescomm.net
 * @description 消息提醒
 * @date 2020/4/20 16:44
 */
@RestController
@RequestMapping("/entmsgremind")
public class EntMsgRemindController extends BasicAction {

    private static Logger logger = LoggerFactory.getLogger(EntMsgRemindController.class);

    @Autowired
    EntMsgRemindService entMsgRemindService;

    @Autowired
    BusiEntityService busiEntityService;

    @PostMapping(value = "/unmsg")
    public ResponseInfo unmsg(@RequestBody(required = false) String body,  HttpServletResponse response) throws Exception {
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
}
