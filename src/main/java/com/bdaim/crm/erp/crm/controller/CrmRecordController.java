package com.bdaim.crm.erp.crm.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.bdaim.common.controller.BasicAction;
import com.bdaim.crm.erp.crm.common.CrmEnum;
import com.bdaim.crm.erp.crm.service.CrmRecordService;
import com.bdaim.crm.utils.AuthUtil;
import com.bdaim.crm.utils.R;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;

/**
 * crm模块操作记录
 */
@RestController
@RequestMapping("/CrmRecord")
public class CrmRecordController extends BasicAction {

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
        if(!"8".equals(types)){
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
    public R deleteFollowRecord(@RequestParam("recordId") Integer recordId) {
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
        //JSONArray jsonArray = JSONArray.parseArray(jsonObject.getString("value"));
        //List<String> list = jsonArray.toJavaList(String.class);
        List<String> list = (List<String>) jsonObject.get("value");
        return(crmRecordService.setRecordOptions(list));
    }
}
