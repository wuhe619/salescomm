package com.bdaim.crm.erp.crm.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.crm.common.annotation.RequestBody;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.service.CrmRecordService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import com.jfinal.core.Controller;
import com.jfinal.core.paragetter.Para;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.List;

@RestController
@RequestMapping("/CrmRecord")
public class CrmRecordController extends Controller {

    @Resource
    private CrmRecordService crmRecordService;

    /**
     * @author hmb
     * 查询操作记录列表
     */
    @RequestMapping(value = "/queryRecordList", method = RequestMethod.POST)
    public R queryRecordList(String actionId, String types) {
        //String actionId = getPara("actionId");
        //String types = getPara("types");
        if(!"11".equals(types)){
            boolean auth = AuthUtil.isCrmAuth(AuthUtil.getCrmTablePara(CrmEnum.getSign(Integer.valueOf(types))), actionId);
            if (auth) {
                return (R.noAuth());
            }
        }
        return (crmRecordService.queryRecordList(actionId, types));
    }

    /**
     * @author wyq
     * 删除跟进记录
     */
    @RequestMapping(value = "/deleteFollowRecord", method = RequestMethod.POST)
    public R deleteFollowRecord(@Para("recordId") Integer recordId) {
        return(crmRecordService.deleteFollowRecord(recordId));
    }

    /**
     * @author wyq
     * 跟进记录类型设置
     */
    @RequestMapping(value = "/queryRecordOptions", method = RequestMethod.POST)
    public R queryRecordOptions() {
        return (crmRecordService.queryRecordOptions());
    }

    /**
     * @author wyq
     * 设置跟进记录类型
     */
    @RequestMapping(value = "/setRecordOptions", method = RequestMethod.POST)
    public R setRecordOptions(@RequestBody JSONObject jsonObject) {
        //JSONObject jsonObject = JSONObject.parseObject(getRawData());
        JSONArray jsonArray = JSONArray.parseArray(jsonObject.getString("value"));
        List<String> list = jsonArray.toJavaList(String.class);
        return(crmRecordService.setRecordOptions(list));
    }
}
